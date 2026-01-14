# Warehouse Employee Management System â Low-Level Technical Design Document

## Table of Contents

1. [E01 â Project Scaffolding & Domain Setup](#e01)
2. [E02 â Employee Master Data (CRUD)](#e02)
3. [E03 â Role-Based Access Control (RBAC)](#e03)
4. [E04 â Time & Attendance (Clock In/Out)](#e04)
5. [E05 â Shift & Schedule Management](#e05)
6. [E06 â Leave & Absence Management](#e06)
7. [E07 â Training & Certification Tracking](#e07)
8. [E08 â Safety Incidents & OSHA Reporting](#e08)
9. [E09 â Equipment & Asset Assignment](#e09)
10. [E10 â Performance Reviews & Goals](#e10)
11. [E11 â Payroll Export Integration](#e11)
12. [E12 â Notifications & Announcements](#e12)
13. [E13 â Integration Layer (HRIS/WMS APIs)](#e13)
14. [E14 â Audit Trail & Compliance](#e14)
15. [E15 â Reporting & Analytics](#e15)
16. [E16 â Mobile Access (PWA)](#e16)
17. [E17 â Onboarding & Offboarding Workflow](#e17)
18. [E18 â Localization & Internationalization](#e18)
19. [E19 â Advanced Scheduling](#e19)
20. [E20 â CI/CD Pipeline](#e20)

---

## <a name="e01"></a>E01 â Project Scaffolding & Domain Setup

### Section: Spring Boot Architecture Overview
**Description:**  
Standardized Spring Boot 3.x (Java 17+) multi-module Maven project. Core modules: employee, scheduling, attendance, safety, integration, reporting, mobile, and shared libraries.

**Design Specification:**  
- Parent POM with dependency management.
- Each domain as a separate module.
- Shared libraries for common utilities, security, and configuration.
- Flyway/Liquibase for DB migrations.
- Spring Boot Actuator enabled.

**Sample Implementation:**
```xml
<!-- Parent pom.xml -->
<modules>
  <module>employee</module>
  <module>scheduling</module>
  <module>attendance</module>
  <module>safety</module>
  <module>integration</module>
  <module>reporting</module>
  <module>mobile</module>
  <module>shared</module>
</modules>
```

---

### Section: Package Structure and Module Definitions
**Description:**  
Organized by domain-driven design (DDD) principles.

**Design Specification:**
```
com.warehouse
  âââ employee
  âââ scheduling
  âââ attendance
  âââ safety
  âââ equipment
  âââ performance
  âââ payroll
  âââ notification
  âââ integration
  âââ audit
  âââ reporting
  âââ mobile
  âââ shared
```

**Sample Implementation:**
```
src/main/java/com/warehouse/employee/EmployeeController.java
src/main/java/com/warehouse/shared/config/SecurityConfig.java
```

---

### Section: Configuration Classes
**Description:**  
Centralized configuration for application properties, DB, and security.

**Design Specification:**
- `application.yml` for environment-specific configs.
- `SecurityConfig` for Spring Security.
- `FlywayConfig` for DB migrations.

**Sample Implementation:**
```yaml
# application.yml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse
    username: warehouse
    password: secret
  flyway:
    enabled: true
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

---

## <a name="e02"></a>E02 â Employee Master Data (CRUD)

### Section: Entity Design with Domain Models and JPA Relationships
**Description:**  
Employee entity as the central domain object.

**Design Specification:**
- Fields: id, name, badgeId (unique), role, department, shiftGroup, hireDate, status, deleted.
- Relationships: ManyToOne (Department), ManyToOne (ShiftGroup), OneToMany (Certifications, Assets, Attendance).

**Sample Implementation:**
```java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    @Column(nullable=false)
    private String name;
    @Column(unique=true, nullable=false)
    private String badgeId;
    @Enumerated(EnumType.STRING)
    private Role role;
    @ManyToOne
    private Department department;
    @ManyToOne
    private ShiftGroup shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    private boolean deleted;
    // getters/setters
}
```

---

### Section: Service Layer Specifications with Business Logic
**Description:**  
Business logic for CRUD, validation, and soft-delete.

**Design Specification:**
- `EmployeeService` with methods: create, update, patch, delete (soft), findById, findAll (with pagination/filtering).

**Sample Implementation:**
```java
public Employee create(EmployeeDTO dto) { ... }
public Employee update(Long id, EmployeeDTO dto) { ... }
public void softDelete(Long id) { ... }
public Page<Employee> findAll(EmployeeFilter filter, Pageable pageable) { ... }
```

---

### Section: Repository Layer with Spring Data JPA
**Description:**  
Spring Data JPA repository for Employee.

**Design Specification:**
- `EmployeeRepository` extends `JpaRepository<Employee, Long>`
- Custom query for badgeId uniqueness and soft-delete.

**Sample Implementation:**
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

---

### Section: Controller Specifications with REST Endpoints
**Description:**  
RESTful CRUD endpoints with OpenAPI annotations.

**Design Specification:**
- `/employees` (GET, POST)
- `/employees/{id}` (GET, PUT, PATCH, DELETE)
- Pagination, filtering, and OpenAPI docs.

**Sample Implementation:**
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @GetMapping
    public Page<EmployeeDTO> list(...) { ... }
    @PostMapping
    public EmployeeDTO create(@RequestBody @Valid EmployeeDTO dto) { ... }
    @PutMapping("/{id}")
    public EmployeeDTO update(@PathVariable Long id, @RequestBody EmployeeDTO dto) { ... }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { ... }
}
```

---

## <a name="e03"></a>E03 â Role-Based Access Control (RBAC)

### Section: Security Settings (Spring Security, OAuth2, RBAC)
**Description:**  
Spring Security with method/endpoint security, role hierarchy, and OAuth2/API key toggle.

**Design Specification:**
- Roles: ADMIN, HR, SUPERVISOR, WORKER.
- Method-level security with `@PreAuthorize`.
- API key or OAuth2 via config.

**Sample Implementation:**
```java
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
          .authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR")
            .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
            .anyRequest().authenticated()
          .and()
            .oauth2ResourceServer().jwt();
    }
}
```
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(#id))")
public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) { ... }
```

---

## <a name="e04"></a>E04 â Time & Attendance (Clock In/Out)

### Section: Entity Design with Domain Models and JPA Relationships
**Description:**  
Attendance entity for clock events.

**Design Specification:**
- Fields: id, employee, clockIn, clockOut, deviceId, location, status, shift, correctionRequested.
- Relationships: ManyToOne (Employee), ManyToOne (Shift).

**Sample Implementation:**
```java
@Entity
public class Attendance {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private String deviceId;
    private String location;
    @Enumerated(EnumType.STRING)
    private Status status;
    @ManyToOne
    private Shift shift;
    private boolean correctionRequested;
}
```

---

### Section: Controller Specifications with REST Endpoints
**Description:**  
Endpoints for clock-in/out, corrections, and reports.

**Design Specification:**
- `/attendance/clock-in` (POST)
- `/attendance/clock-out` (POST)
- `/attendance/corrections` (POST)
- `/attendance/reports` (GET, CSV export)

**Sample Implementation:**
```java
@PostMapping("/clock-in")
public AttendanceDTO clockIn(@RequestBody ClockEventDTO dto) { ... }
@PostMapping("/clock-out")
public AttendanceDTO clockOut(@RequestBody ClockEventDTO dto) { ... }
```

---

## <a name="e05"></a>E05 â Shift & Schedule Management

### Section: Entity Design with Domain Models and JPA Relationships
**Description:**  
Shift and Schedule entities.

**Design Specification:**
- Shift: id, name, start, end, recurrence, blackoutDates.
- Schedule: id, employee, shift, date, status.

**Sample Implementation:**
```java
@Entity
public class Shift {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime start;
    private LocalTime end;
    private String recurrence;
    @ElementCollection
    private List<LocalDate> blackoutDates;
}
@Entity
public class Schedule {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private Shift shift;
    private LocalDate date;
    @Enumerated(EnumType.STRING)
    private Status status;
}
```

---

### Section: Service Layer Specifications with Business Logic
**Description:**  
Conflict detection, bulk assignment, audit logging.

**Design Specification:**
- `ScheduleService`: assignShifts, detectConflicts, bulkAssign, audit.

**Sample Implementation:**
```java
public void assignShift(Long employeeId, Long shiftId, LocalDate date) { ... }
public List<Conflict> detectConflicts(Long employeeId, LocalDate date) { ... }
```

---

## <a name="e06"></a>E06 â Leave & Absence Management

### Section: Entity Design with Domain Models and JPA Relationships
**Description:**  
LeaveRequest entity with approval workflow.

**Design Specification:**
- Fields: id, employee, type, startDate, endDate, status, approver, accrualBalance.

**Sample Implementation:**
```java
@Entity
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
    private Status status;
    @ManyToOne
    private Employee approver;
    private BigDecimal accrualBalance;
}
```

---

### Section: Controller Specifications with REST Endpoints
**Description:**  
Endpoints for leave requests, approvals, and exports.

**Design Specification:**
- `/leave/requests` (POST, GET)
- `/leave/requests/{id}/approve` (POST)
- `/leave/exports` (GET)

**Sample Implementation:**
```java
@PostMapping("/requests")
public LeaveRequestDTO requestLeave(@RequestBody LeaveRequestDTO dto) { ... }
@PostMapping("/requests/{id}/approve")
public LeaveRequestDTO approveLeave(@PathVariable Long id) { ... }
```

---

## <a name="e07"></a>E07 â Training & Certification Tracking

### Section: Entity Design with Domain Models and JPA Relationships
**Description:**  
Certification entity with expiry and document upload.

**Design Specification:**
- Fields: id, employee, type, issueDate, expiryDate, documentUrl, status.

**Sample Implementation:**
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
    @Enumerated(EnumType.STRING)
    private Status status;
}
```

---

### Section: Service Layer Specifications with Business Logic
**Description:**  
Expiry alerts, scheduling checks, and status visibility.

**Design Specification:**
- `CertificationService`: alertExpiring, blockAssignment, getStatus.

**Sample Implementation:**
```java
public List<Certification> alertExpiring(int days) { ... }
public boolean canAssign(Long employeeId, String certType) { ... }
```

---

## <a name="e08"></a>E08 â Safety Incidents & OSHA Reporting

### Section: Entity Design with Domain Models and JPA Relationships
**Description:**  
SafetyIncident entity with workflow.

**Design Specification:**
- Fields: id, date, location, description, severity, involvedEmployees, status, correctiveActions.

**Sample Implementation:**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private LocalDate date;
    private String location;
    private String description;
    @Enumerated(EnumType.STRING)
    private Severity severity;
    @ManyToMany
    private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private Status status;
    private String correctiveActions;
}
```

---

### Section: Controller Specifications with REST Endpoints
**Description:**  
Endpoints for incident reporting, workflow, and OSHA export.

**Design Specification:**
- `/safety/incidents` (POST, GET)
- `/safety/incidents/{id}/status` (PATCH)
- `/safety/osha/export` (GET)

**Sample Implementation:**
```java
@PostMapping("/incidents")
public SafetyIncidentDTO reportIncident(@RequestBody SafetyIncidentDTO dto) { ... }
@PatchMapping("/incidents/{id}/status")
public SafetyIncidentDTO updateStatus(@PathVariable Long id, @RequestBody StatusUpdateDTO dto) { ... }
```

---

## <a name="e09"></a>E09 â Equipment & Asset Assignment

### Section: Entity Design with Domain Models and JPA Relationships
**Description:**  
Asset entity with assignment and condition tracking.

**Design Specification:**
- Fields: id, type, serialNumber, assignedTo, checkoutDate, returnDate, condition, history.

**Sample Implementation:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    @ManyToOne
    private Employee assignedTo;
    private LocalDateTime checkoutDate;
    private LocalDateTime returnDate;
    private String condition;
    @OneToMany
    private List<AssetHistory> history;
}
```

---

### Section: Controller Specifications with REST Endpoints
**Description:**  
Endpoints for asset CRUD, check-in/out, and reports.

**Design Specification:**
- `/assets` (CRUD)
- `/assets/{id}/checkout` (POST)
- `/assets/{id}/return` (POST)

**Sample Implementation:**
```java
@PostMapping("/{id}/checkout")
public AssetDTO checkout(@PathVariable Long id, @RequestBody CheckoutDTO dto) { ... }
@PostMapping("/{id}/return")
public AssetDTO returnAsset(@PathVariable Long id) { ... }
```

---

## <a name="e10"></a>E10 â Performance Reviews & Goals

### Section: Entity Design with Domain Models and JPA Relationships
**Description:**  
PerformanceReview entity with goals and workflow.

**Design Specification:**
- Fields: id, employee, period, goals, competencies, ratings, comments, supervisor, acknowledgedByEmployee, acknowledgedBySupervisor, status.

**Sample Implementation:**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String period;
    @ElementCollection
    private List<String> goals;
    @ElementCollection
    private List<String> competencies;
    @ElementCollection
    private List<Integer> ratings;
    private String comments;
    @ManyToOne
    private Employee supervisor;
    private boolean acknowledgedByEmployee;
    private boolean acknowledgedBySupervisor;
    @Enumerated(EnumType.STRING)
    private Status status;
}
```

---

### Section: Controller Specifications with REST Endpoints
**Description:**  
Endpoints for review cycles, submission, and PDF export.

**Design Specification:**
- `/reviews` (CRUD)
- `/reviews/{id}/acknowledge` (POST)
- `/reviews/{id}/export` (GET)

**Sample Implementation:**
```java
@PostMapping("/{id}/acknowledge")
public PerformanceReviewDTO acknowledge(@PathVariable Long id) { ... }
@GetMapping("/{id}/export")
public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) { ... }
```

---

## <a name="e11"></a>E11 â Payroll Export Integration

### Section: Integration Points (External APIs, Webhooks)
**Description:**  
Payroll export to external provider via SFTP/API.

**Design Specification:**
- Scheduled job to generate payroll files.
- Mapping to provider schema.
- Secure delivery (SFTP/API).
- Retry with backoff on failure.
- Audit log for each export.

**Sample Implementation:**
```java
@Scheduled(cron = "0 0 2 * * MON")
public void exportPayroll() { ... }
public void sendToProvider(File file) { ... }
```

---

## <a name="e12"></a>E12 â Notifications & Announcements

### Section: Integration Points (External APIs, Webhooks)
**Description:**  
In-app, email, and SMS notifications with opt-in/out and localization.

**Design Specification:**
- Notification channels: in-app, email, SMS.
- User preferences.
- Localized templates.
- Delivery status tracking.
- Rate limiting.

**Sample Implementation:**
```java
public void sendNotification(Long userId, NotificationType type, String message) { ... }
public void trackDelivery(Long notificationId, DeliveryStatus status) { ... }
```

---

## <a name="e13"></a>E13 â Integration Layer (HRIS/WMS APIs)

### Section: Integration Points (External APIs, Webhooks)
**Description:**  
REST APIs and connectors for HRIS, WMS, and IDP.

**Design Specification:**
- JWT/OAuth2-secured APIs.
- HRIS sync job for new hires/terms.
- WMS integration for department/location.
- Idempotent webhooks.
- OpenAPI documentation.

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/integration/hris")
public class HRISController {
    @PostMapping("/employees")
    public ResponseEntity<?> syncEmployee(@RequestBody EmployeeDTO dto) { ... }
}
```

---

## <a name="e14"></a>E14 â Audit Trail & Compliance

### Section: Entity Design with Domain Models and JPA Relationships
**Description:**  
Centralized audit log for sensitive changes.

**Design Specification:**
- Fields: id, entity, entityId, actor, timestamp, action, before, after, immutable.

**Sample Implementation:**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private Long entityId;
    private String actor;
    private LocalDateTime timestamp;
    private String action;
    @Lob
    private String before;
    @Lob
    private String after;
    private boolean immutable;
}
```

---

### Section: Service Layer Specifications with Business Logic
**Description:**  
Automatic logging on create/update/delete.

**Design Specification:**
- `AuditService`: logChange(entity, entityId, actor, action, before, after).

**Sample Implementation:**
```java
public void logChange(String entity, Long entityId, String actor, String action, Object before, Object after) { ... }
```

---

## <a name="e15"></a>E15 â Reporting & Analytics

### Section: Controller Specifications with REST Endpoints
**Description:**  
Endpoints for operational reports and exports.

**Design Specification:**
- `/reports/attendance`, `/reports/overtime`, `/reports/leave`, `/reports/certifications`, `/reports/safety`
- Filters: date, department, shift.
- Export: CSV/PDF.

**Sample Implementation:**
```java
@GetMapping("/attendance")
public ResponseEntity<Resource> exportAttendanceReport(@RequestParam ...) { ... }
```

---

## <a name="e16"></a>E16 â Mobile Access (PWA)

### Section: Configuration Classes
**Description:**  
PWA manifest and offline support.

**Design Specification:**
- `manifest.json` for installable PWA.
- Service worker for offline queue.
- Responsive UI.

**Sample Implementation:**
```json
{
  "short_name": "WarehouseEMS",
  "name": "Warehouse Employee Management",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

---

## <a name="e17"></a>E17 â Onboarding & Offboarding Workflow

### Section: Service Layer Specifications with Business Logic
**Description:**  
Automated provisioning and deprovisioning.

**Design Specification:**
- Onboarding: create account, assign schedule, enroll training, assign assets.
- Offboarding: revoke access, collect assets, update schedules.

**Sample Implementation:**
```java
public void onboardEmployee(EmployeeDTO dto) { ... }
public void offboardEmployee(Long employeeId) { ... }
```

---

## <a name="e18"></a>E18 â Localization & Internationalization

### Section: Configuration Classes
**Description:**  
Support for multiple languages and locales.

**Design Specification:**
- `messages_{locale}.properties` for i18n.
- LocaleResolver bean.

**Sample Implementation:**
```java
@Bean
public LocaleResolver localeResolver() {
    SessionLocaleResolver slr = new SessionLocaleResolver();
    slr.setDefaultLocale(Locale.US);
    return slr;
}
```

---

## <a name="e19"></a>E19 â Advanced Scheduling

### Section: Service Layer Specifications with Business Logic
**Description:**  
Complex scheduling algorithms for rotations, overtime, and blackout handling.

**Design Specification:**
- `AdvancedScheduleService`: generateRotations, handleOvertime, enforceBlackouts.

**Sample Implementation:**
```java
public List<Schedule> generateRotations(List<Employee> employees, ShiftTemplate template) { ... }
public boolean isBlackout(LocalDate date) { ... }
```

---

## <a name="e20"></a>E20 â CI/CD Pipeline

### Section: Configuration Classes
**Description:**  
Automated build, test, and deployment pipeline.

**Design Specification:**
- GitHub Actions/Jenkins pipeline.
- Steps: build, test, lint, package, deploy.
- Docker support.

**Sample Implementation:**
```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline
on:
  push:
    branches: [ main ]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Build with Maven
        run: mvn clean install
      - name: Run Tests
        run: mvn test
      - name: Build Docker Image
        run: docker build -t warehouse-ems .
      - name: Deploy
        run: ./deploy.sh
```

---

# End of Document

This document provides a production-ready, low-level technical design for all 20 epics of the Warehouse Employee Management System, following Spring Boot 3.x and Java 17+ best practices. Each section includes architecture, package structure, entity and service design, repository and controller specs, configuration, security, integration, and code snippets for key implementations.