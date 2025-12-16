# Warehouse Employee Management System â Low-Level Technical Design Document

---

## Section: E01 â Project Scaffolding & Domain Setup

**Description:**  
Establishes the foundational Spring Boot (Maven) project structure, core modules, database migration strategy, and health monitoring. This epic ensures a standardized, maintainable, and extensible architecture for all subsequent features.

**Design Specification:**  
- **Package Structure:**  
  - `com.warehouse.employee` (root)  
    - `config`  
    - `domain`  
    - `repository`  
    - `service`  
    - `controller`  
    - `dto`  
    - `exception`  
    - `util`  
- **Core Modules:**  
  - Employee, Scheduling, Attendance, Safety  
- **Database Migration:**  
  - Flyway or Liquibase for schema versioning  
- **Monitoring:**  
  - Spring Boot Actuator enabled  
- **Build:**  
  - Maven, Java 17+  
- **Health Check:**  
  - `/actuator/health` endpoint

**Sample Implementation:**  
```java
// pom.xml (excerpt)
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
```yaml
# src/main/resources/application.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse
    username: warehouse_user
    password: secret
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  flyway:
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```
```java
// Example base package structure
package com.warehouse.employee;
@SpringBootApplication
public class WarehouseEmployeeApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeApplication.class, args);
    }
}
```
---

## Section: E02 â Employee Master Data (CRUD)

**Description:**  
Implements the Employee domain with full CRUD REST APIs, enforcing unique badge IDs, supporting soft deletes, and providing pagination/filtering. DTOs are used for API contracts.

**Design Specification:**  
- **Package Structure:**  
  - `com.warehouse.employee.domain`  
  - `com.warehouse.employee.repository`  
  - `com.warehouse.employee.service`  
  - `com.warehouse.employee.controller`  
  - `com.warehouse.employee.dto`  
- **Domain Model:**  
  - `Employee` entity: id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted  
- **Repository Layer:**  
  - `EmployeeRepository extends JpaRepository<Employee, Long>`  
  - Custom: `findByBadgeId`, `findAllByStatusAndDeletedFalse`  
- **Service Layer:**  
  - `EmployeeService` with CRUD, soft-delete, validation  
- **Controller Layer:**  
  - `EmployeeController` with `/employees` endpoints  
  - Supports pagination, filtering, OpenAPI annotations  
- **Configuration:**  
  - JPA, validation  
- **Security:**  
  - Method-level security for role-based access  
- **Integration:**  
  - None for core CRUD

**Sample Implementation:**  
```java
// Employee Entity
@Entity
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @NotBlank
    private String role;

    @NotBlank
    private String department;

    private String shiftGroup;

    @PastOrPresent
    private LocalDate hireDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    private boolean deleted = false;
}
```
```java
// EmployeeRepository
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByStatusAndDeletedFalse(EmployeeStatus status, Pageable pageable);
}
```
```java
// EmployeeService
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Transactional
    public Employee createEmployee(EmployeeDTO dto) {
        // Validate, map DTO, save entity
    }
    // Other CRUD methods...
}
```
```java
// EmployeeController
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Employee CRUD APIs")
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeDTO dto) { ... }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public Page<EmployeeDTO> list(@PageableDefault Pageable pageable) { ... }
    // Other endpoints...
}
```
---

## Section: E03 â Role-Based Access Control (RBAC)

**Description:**  
Secures endpoints and data using Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER). Supports API key or OAuth2 authentication, configurable via properties.

**Design Specification:**  
- **Package Structure:**  
  - `com.warehouse.employee.config.security`  
- **Security Configuration:**  
  - `WebSecurityConfig` for HTTP security  
  - Role-based method security with `@PreAuthorize`  
  - API key/OAuth2 toggle via `application.yml`  
- **Integration:**  
  - Optionally integrates with external IDP for OAuth2

**Sample Implementation:**  
```java
// SecurityConfig
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${security.mode:apikey}")
    private String securityMode;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if ("oauth2".equals(securityMode)) {
            http.oauth2ResourceServer().jwt();
        } else {
            http.addFilterBefore(new ApiKeyAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        }
        http.authorizeRequests()
            .antMatchers("/actuator/**").permitAll()
            .anyRequest().authenticated();
    }
}
```
```yaml
# application.yml
security:
  mode: apikey # or oauth2
```
---

## Section: E04 â Time & Attendance (Clock In/Out)

**Description:**  
Implements endpoints for clock-in/out events, geofencing, device capture, shift association, missed punch handling, and daily totals computation.

**Design Specification:**  
- **Package Structure:**  
  - `com.warehouse.employee.attendance.domain`  
  - `com.warehouse.employee.attendance.repository`  
  - `com.warehouse.employee.attendance.service`  
  - `com.warehouse.employee.attendance.controller`  
- **Domain Model:**  
  - `AttendanceEvent`: id, employee, type (IN/OUT), timestamp, location, deviceId, status  
- **Repository Layer:**  
  - `AttendanceEventRepository`  
- **Service Layer:**  
  - `AttendanceService` with punch logic, missed punch workflow  
- **Controller Layer:**  
  - `/attendance/clock-in`, `/attendance/clock-out`  
- **Integration:**  
  - Geofence API (optional), device registry

**Sample Implementation:**  
```java
@Entity
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private AttendanceType type; // IN, OUT

    private LocalDateTime timestamp;
    private String location;
    private String deviceId;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status; // NORMAL, MISSED, CORRECTED
}
```
```java
@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    @PostMapping("/clock-in")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<Void> clockIn(@RequestBody ClockEventDTO dto) { ... }

    @PostMapping("/clock-out")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<Void> clockOut(@RequestBody ClockEventDTO dto) { ... }
}
```
---

## Section: E05 â Shift & Schedule Management

**Description:**  
Manages recurring shift templates, rotations, overtime rules, blackout dates, and employee assignments. Detects/prevents conflicts and supports bulk assignment.

**Design Specification:**  
- **Package Structure:**  
  - `com.warehouse.employee.schedule.domain`  
  - `com.warehouse.employee.schedule.repository`  
  - `com.warehouse.employee.schedule.service`  
  - `com.warehouse.employee.schedule.controller`  
- **Domain Model:**  
  - `ShiftTemplate`, `EmployeeShiftAssignment`, `OvertimeRule`, `BlackoutDate`  
- **Repository Layer:**  
  - `ShiftTemplateRepository`, etc.  
- **Service Layer:**  
  - `ScheduleService`  
- **Controller Layer:**  
  - `/shifts`, `/schedules` endpoints

**Sample Implementation:**  
```java
@Entity
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    private String recurrencePattern; // e.g., CRON or custom
}
```
---

## Section: E06 â Leave & Absence Management

**Description:**  
Handles PTO, sick, unpaid leave requests/approvals, accrual policies, and integration with scheduling/payroll.

**Design Specification:**  
- **Domain Model:**  
  - `LeaveRequest`, `LeaveBalance`, `LeavePolicy`  
- **Repository Layer:**  
  - `LeaveRequestRepository`, etc.  
- **Service Layer:**  
  - `LeaveService`  
- **Controller Layer:**  
  - `/leave/requests`, `/leave/balances`

**Sample Implementation:**  
```java
@Entity
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @Enumerated(EnumType.STRING)
    private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;
    private String reason;
}
```
---

## Section: E07 â Training & Certification Tracking

**Description:**  
Tracks required certifications, expirations, renewals, and blocks assignments for expired certs.

**Design Specification:**  
- **Domain Model:**  
  - `Certification`, `EmployeeCertification`  
- **Repository Layer:**  
  - `CertificationRepository`  
- **Service Layer:**  
  - `CertificationService`  
- **Controller Layer:**  
  - `/certifications`, `/employee-certifications`

**Sample Implementation:**  
```java
@Entity
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalDate expiryDate;
    private String documentUrl;
}
```
---

## Section: E08 â Safety Incidents & OSHA Reporting

**Description:**  
Records incidents/near-misses, manages investigation workflow, and generates OSHA reports.

**Design Specification:**  
- **Domain Model:**  
  - `SafetyIncident`, `IncidentStatus`  
- **Repository Layer:**  
  - `SafetyIncidentRepository`  
- **Service Layer:**  
  - `SafetyService`  
- **Controller Layer:**  
  - `/safety/incidents`

**Sample Implementation:**  
```java
@Entity
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String description;
    private String location;
    @ManyToMany
    private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
}
```
---

## Section: E09 â Equipment & Asset Assignment

**Description:**  
Assigns assets (scanners, forklifts, PPE) to employees, tracks check-in/out, and enforces certification requirements.

**Design Specification:**  
- **Domain Model:**  
  - `Asset`, `AssetAssignment`  
- **Repository Layer:**  
  - `AssetRepository`, `AssetAssignmentRepository`  
- **Service Layer:**  
  - `AssetService`  
- **Controller Layer:**  
  - `/assets`, `/asset-assignments`

**Sample Implementation:**  
```java
@Entity
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private AssetCondition condition;
}
```
---

## Section: E10 â Performance Reviews & Goals

**Description:**  
Manages review templates, goals, competencies, ratings, and acknowledgements.

**Design Specification:**  
- **Domain Model:**  
  - `PerformanceReview`, `Goal`, `Competency`  
- **Repository Layer:**  
  - `PerformanceReviewRepository`  
- **Service Layer:**  
  - `PerformanceService`  
- **Controller Layer:**  
  - `/reviews`, `/goals`

**Sample Implementation:**  
```java
@Entity
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate reviewDate;
    private String comments;
    private int rating;
}
```
---

## Section: E11 â Payroll Export Integration

**Description:**  
Generates payroll-ready files from attendance/leave, maps to provider formats, and delivers securely.

**Design Specification:**  
- **Domain Model:**  
  - `PayrollExport`  
- **Service Layer:**  
  - `PayrollExportService`  
- **Integration:**  
  - SFTP/API delivery, audit logging

**Sample Implementation:**  
```java
@Service
public class PayrollExportService {
    public File generatePayrollFile(LocalDate periodStart, LocalDate periodEnd) {
        // Aggregate attendance/leave, map to schema, write CSV
    }
}
```
---

## Section: E12 â Notifications & Announcements

**Description:**  
Sends in-app/email/SMS notifications for shifts, certs, approvals, and announcements.

**Design Specification:**  
- **Domain Model:**  
  - `Notification`, `Announcement`  
- **Service Layer:**  
  - `NotificationService`  
- **Integration:**  
  - Email/SMS providers

**Sample Implementation:**  
```java
@Service
public class NotificationService {
    public void sendNotification(NotificationDTO dto) {
        // Send via preferred channel
    }
}
```
---

## Section: E13 â Integration Layer (HRIS/WMS APIs)

**Description:**  
Exposes REST APIs and connectors for HRIS, WMS, and SSO (IDP). Supports webhooks.

**Design Specification:**  
- **Controller Layer:**  
  - `/api/hris`, `/api/wms`, `/api/idp`  
- **Security:**  
  - JWT/OAuth2  
- **Integration:**  
  - REST, webhooks

**Sample Implementation:**  
```java
@RestController
@RequestMapping("/api/hris")
public class HRISController {
    @PostMapping("/employees")
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<Void> syncEmployee(@RequestBody EmployeeDTO dto) { ... }
}
```
---

## Section: E14 â Audit Trail & Compliance

**Description:**  
Centralized audit logging for sensitive changes with tamper-evident storage.

**Design Specification:**  
- **Domain Model:**  
  - `AuditLog`  
- **Repository Layer:**  
  - `AuditLogRepository`  
- **Service Layer:**  
  - `AuditService`  
- **Integration:**  
  - Export endpoints

**Sample Implementation:**  
```java
@Entity
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private Long entityId;
    private String action;
    private String actor;
    private LocalDateTime timestamp;
    @Lob
    private String before;
    @Lob
    private String after;
}
```
---

## Section: E15 â Reporting & Analytics

**Description:**  
Provides operational reports (attendance, overtime, leave, certs, safety KPIs) with CSV/PDF export and dashboards.

**Design Specification:**  
- **Service Layer:**  
  - `ReportingService`  
- **Controller Layer:**  
  - `/reports` endpoints

**Sample Implementation:**  
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> exportAttendanceReport(...) { ... }
}
```
---

## Section: E16 â Mobile Access (PWA)

**Description:**  
Responsive PWA for clock-in/out, schedules, leave requests, and announcements. Offline support.

**Design Specification:**  
- **Controller Layer:**  
  - `/mobile` endpoints  
- **Integration:**  
  - PWA manifest, service worker

**Sample Implementation:**  
```java
// PWA manifest (manifest.json)
{
  "name": "Warehouse Employee Portal",
  "short_name": "Warehouse",
  "start_url": "/mobile",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```
---

## Section: E17 â Onboarding & Offboarding Workflow

**Description:**  
Automates provisioning (accounts, schedule, training) and deprovisioning (access, assets, schedules) for employee lifecycle changes.

**Design Specification:**  
- **Service Layer:**  
  - `OnboardingService`, `OffboardingService`  
- **Integration:**  
  - HRIS, asset, schedule, and training modules

**Sample Implementation:**  
```java
@Service
public class OnboardingService {
    public void onboardEmployee(Employee employee) {
        // Create account, assign initial schedule, generate training tasks
    }
}
```
---

**End of Document**