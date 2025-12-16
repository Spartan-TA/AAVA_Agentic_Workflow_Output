# Warehouse EMS - Low-Level Technical Design Document

## Overview
This document provides comprehensive low-level technical design specifications for the Warehouse Employee Management System (EMS) built with Spring Boot. Each section covers a specific epic with detailed architecture, entity design, service layers, controllers, and sample implementations.

---

## Section: E01 - Project Scaffolding & Domain Setup

**Description:** Establishes the foundational Spring Boot project structure, configures core modules, sets up database migration, and enables health monitoring.

**Design Specification:**
- Spring Boot Maven project with parent POM
- Base packages: com.warehouseems.{employee,scheduling,attendance,safety,common,config}
- Modules: employee, scheduling, attendance, safety (as packages or submodules if multi-module)
- Flyway/Liquibase for DB migration (src/main/resources/db/migration)
- Spring Boot Actuator enabled
- application.yml with server.port=8080
- README with build/run instructions

**Sample Implementation:**

```java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

application.yml:
```yaml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouseems
    username: warehouse
    password: secret
  flyway:
    enabled: true
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

Flyway migration example:
```sql
-- V1__init.sql
CREATE TABLE employee (...);
```

---

## Section: E02 - Employee Master Data CRUD

**Description:** Implements Employee domain with CRUD REST APIs, DTOs, and data validation.

**Design Specification:**
- Entity: Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService with CRUD, soft-delete, filtering, pagination
- Controller: EmployeeController with /employees endpoints
- DTOs: EmployeeDto, EmployeeCreateDto, EmployeeUpdateDto
- badgeId unique constraint
- OpenAPI annotations for schema

**Sample Implementation:**

```java
@Entity
@Table(name = "employee", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue private Long id;
    private String name;
    @Column(name = "badge_id", unique = true) private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted = false;
    // getters/setters
}
```

```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping public EmployeeDto create(@RequestBody @Valid EmployeeCreateDto dto) {...}
    @GetMapping public Page<EmployeeDto> list(Pageable pageable, @RequestParam Map<String,String> filters) {...}
    @GetMapping("/{id}") public EmployeeDto get(@PathVariable Long id) {...}
    @PutMapping("/{id}") public EmployeeDto update(@PathVariable Long id, @RequestBody @Valid EmployeeUpdateDto dto) {...}
    @PatchMapping("/{id}") public EmployeeDto patch(@PathVariable Long id, @RequestBody Map<String,Object> updates) {...}
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) {...}
}
```

---

## Section: E03 - Role Based Access Control

**Description:** Secures endpoints and methods using Spring Security with roles and row-level constraints.

**Design Specification:**
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- SecurityConfig with @EnableWebSecurity
- Method security: @PreAuthorize annotations
- API key/OAuth2 toggle via application.yml
- Row-level filtering in repositories/services

**Sample Implementation:**

```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
          .authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .anyRequest().authenticated()
          .and()
            .oauth2ResourceServer().jwt();
    }
}
```

```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isSupervisorOf(authentication, #id))")
public EmployeeDto getEmployee(Long id) {...}
```

---

## Section: E04 - Time & Attendance Clock In/Out

**Description:** Provides endpoints for clock-in/out, shift association, and attendance reporting.

**Design Specification:**
- Entity: AttendanceEvent (id, employee, type, timestamp, deviceId, location, shift, status)
- Repository: AttendanceEventRepository
- Service: AttendanceService (clockIn, clockOut, calculateTotals, corrections)
- Controller: AttendanceController (/attendance/clock-in, /clock-out)
- Geofence/device capture fields
- Correction workflow creates approval tasks

**Sample Implementation:**

```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String type; // CLOCK_IN, CLOCK_OUT
    private LocalDateTime timestamp;
    private String deviceId;
    private String location;
    @ManyToOne private Shift shift;
    private String status; // NORMAL, CORRECTION_PENDING
}
```

```java
@PostMapping("/attendance/clock-in")
public AttendanceEventDto clockIn(@RequestBody ClockInRequest req) {...}
```

---

## Section: E05 - Shift & Schedule Management

**Description:** Manages shift templates, rotations, assignments, and conflict detection.

**Design Specification:**
- Entities: ShiftTemplate, ShiftAssignment, BlackoutDate, OperationCalendar
- Services: ShiftService, ScheduleService
- Controllers: ShiftController, ScheduleController
- Bulk assignment endpoints
- Audit entries on changes

**Sample Implementation:**

```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrenceRule;
}
```

```java
@PostMapping("/shifts/assign")
public void bulkAssign(@RequestBody BulkAssignRequest req) {...}
```

---

## Section: E06 - Leave & Absence Management

**Description:** Handles leave requests, approvals, accruals, and integration with scheduling/payroll.

**Design Specification:**
- Entities: LeaveRequest, LeaveBalance, LeavePolicy
- Services: LeaveService (request, approve, update balances)
- Controller: LeaveController
- Integration hooks for scheduling/payroll

**Sample Implementation:**

```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String type; // PTO, SICK, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // PENDING, APPROVED, DENIED
}
```

```java
@PostMapping("/leave/request")
public LeaveRequestDto requestLeave(@RequestBody LeaveRequestDto dto) {...}
```

---

## Section: E07 - Training & Certification Tracking

**Description:** Tracks certifications, expirations, renewals, and blocks unqualified assignments.

**Design Specification:**
- Entities: Certification, EmployeeCertification, CertificationDocument
- Services: CertificationService
- Controller: CertificationController
- Expiry alerts (scheduled job)
- Scheduling checks for valid certs

**Sample Implementation:**

```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalDate expiryDate;
    private boolean required;
}
```

```java
@PostMapping("/certifications")
public CertificationDto addCertification(@RequestBody CertificationDto dto) {...}
```

---

## Section: E08 - Safety Incidents & OSHA Reporting

**Description:** Records safety incidents, manages investigation workflow, and generates OSHA reports.

**Design Specification:**
- Entities: SafetyIncident, IncidentStatus, Investigation, CorrectiveAction
- Services: SafetyService
- Controller: SafetyController (/safety/incidents)
- OSHA export endpoints

**Sample Implementation:**

```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private String severity;
    private String location;
    private String description;
    @ManyToMany private List<Employee> involvedEmployees;
    private String status; // OPEN, INVESTIGATING, RESOLVED
}
```

```java
@PostMapping("/safety/incidents")
public SafetyIncidentDto reportIncident(@RequestBody SafetyIncidentDto dto) {...}
```

---

## Section: E09 - Equipment & Asset Assignment

**Description:** Manages asset registry, assignment, check-in/out, and certification validation.

**Design Specification:**
- Entities: Asset, AssetAssignment, AssetHistory
- Services: AssetService
- Controller: AssetController
- Certification checks before assignment

**Sample Implementation:**

```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String type;
    private String serialNumber;
    private String condition;
    private boolean available;
}
```

```java
@PostMapping("/assets/assign")
public AssetAssignmentDto assignAsset(@RequestBody AssetAssignmentDto dto) {...}
```

---

## Section: E10 - Performance Reviews & Goals

**Description:** Supports review templates, goal tracking, and immutable review history.

**Design Specification:**
- Entities: PerformanceReview, ReviewTemplate, Goal, Acknowledgement
- Services: ReviewService
- Controller: ReviewController
- PDF export integration

**Sample Implementation:**

```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String cycle;
    private String status; // DRAFT, SUBMITTED, ACKNOWLEDGED
    private String comments;
}
```

```java
@PostMapping("/reviews")
public PerformanceReviewDto createReview(@RequestBody PerformanceReviewDto dto) {...}
```

---

## Section: E11 - Payroll Export Integration

**Description:** Generates payroll files from attendance/leave, maps to provider schema, and delivers securely.

**Design Specification:**
- Service: PayrollExportService (generate, deliver, retry)
- Integration: SFTP/API client
- Audit log for exports
- Controller: PayrollExportController

**Sample Implementation:**

```java
@Service
public class PayrollExportService {
    public void exportPayroll(LocalDate period) {...}
}
```

```java
@PostMapping("/payroll/export")
public void exportPayroll(@RequestParam LocalDate period) {...}
```

---

## Section: E12 - Notifications & Announcements

**Description:** Sends notifications via in-app, email, SMS; supports templates, opt-in/out, and rate limits.

**Design Specification:**
- Entities: Notification, Announcement, NotificationPreference
- Services: NotificationService, AnnouncementService
- Integration: Email/SMS provider
- Controller: NotificationController, AnnouncementController

**Sample Implementation:**

```java
@Entity
public class Notification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String channel; // EMAIL, SMS, IN_APP
    private String content;
    private String status; // SENT, FAILED
}
```

```java
@PostMapping("/announcements")
public AnnouncementDto createAnnouncement(@RequestBody AnnouncementDto dto) {...}
```

---

## Section: E13 - Integration Layer (HRIS/WMS APIs)

**Description:** Exposes REST APIs and connectors for HRIS, WMS, and SSO; supports webhooks and OpenAPI docs.

**Design Specification:**
- Integration: HRISClient, WMSClient, IDPClient
- Security: JWT/OAuth2
- Webhook endpoints
- OpenAPI documentation

**Sample Implementation:**

```java
@RestController
@RequestMapping("/api/hris")
public class HRISController {
    @PostMapping("/employees")
    public void syncEmployee(@RequestBody EmployeeDto dto) {...}
}
```

application.yml:
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://idp.example.com
```

---

## Section: E14 - Audit Trail & Compliance

**Description:** Centralizes audit logging for sensitive changes, with tamper-evident storage and export.

**Design Specification:**
- Entity: AuditLog (id, actor, timestamp, entity, before, after, action)
- Service: AuditService (logChange, export)
- Controller: AuditController
- Immutability enforced at DB level

**Sample Implementation:**

```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    @Lob private String before;
    @Lob private String after;
    private String action;
}
```

```java
@PostMapping("/audit/export")
public void exportAudit(@RequestParam Map<String,String> filters) {...}
```

---

## Section: E15 - Reporting & Analytics

**Description:** Provides operational reports, exports, and dashboards with role-based access.

**Design Specification:**
- Service: ReportingService (attendance, overtime, leave, certs, safety)
- Controller: ReportingController
- CSV/PDF export integration
- Metrics endpoints for BI

**Sample Implementation:**

```java
@GetMapping("/reports/attendance")
public ResponseEntity<Resource> exportAttendance(@RequestParam Map<String,String> filters) {...}
```

---

## Section: E16 - Mobile Access (PWA)

**Description:** Enables responsive, offline-friendly mobile access for core flows.

**Design Specification:**
- Controller: MobileController (serves PWA manifest, offline queue endpoints)
- Service: OfflineEventService (queue, sync, conflict resolution)
- Static resources: manifest.json, service-worker.js

**Sample Implementation:**

```java
@GetMapping("/pwa/manifest.json")
public ResponseEntity<Resource> getManifest() {...}
```

---

## Section: E17 - Onboarding & Offboarding Workflow

**Description:** Automates provisioning, training, asset assignment, and deprovisioning.

**Design Specification:**
- Service: OnboardingService, OffboardingService
- Integration: HRIS sync, asset/training task generation
- Controller: OnboardingController, OffboardingController

**Sample Implementation:**

```java
@PostMapping("/onboarding")
public void onboardEmployee(@RequestBody EmployeeDto dto) {...}
```

---

## Section: E18 - Localization & Multi-Warehouse

**Description:** Supports multiple warehouses, calendars, and UI localization.

**Design Specification:**
- Entity: Warehouse (id, name, locale, calendar)
- Employee assigned to warehouse
- application.yml: messageSource for i18n
- Locale selection persisted in user profile

**Sample Implementation:**

```java
@Entity
public class Warehouse {
    @Id @GeneratedValue private Long id;
    private String name;
    private String locale;
}
```

application.yml:
```yaml
spring:
  messages:
    basename: messages
```

---

## Section: E19 - Advanced Scheduling Optimization

**Description:** Implements constraint solver for shift assignments, skills, preferences, and fairness.

**Design Specification:**
- Service: SchedulingOptimizerService (runOptimizer, suggestSwaps)
- Integration: OptaPlanner or custom solver
- Manual override endpoints

**Sample Implementation:**

```java
@Service
public class SchedulingOptimizerService {
    public ScheduleResult optimize(List<Employee> employees, List<Shift> shifts) {...}
}
```

---

## Section: E20 - Document Management

**Description:** Manages document storage, versioning, access control, and e-signature workflow.

**Design Specification:**
- Entities: Document, DocumentVersion, DocumentAcknowledgement
- Service: DocumentService
- Controller: DocumentController
- File storage integration (local/S3)

**Sample Implementation:**

```java
@Entity
public class Document {
    @Id @GeneratedValue private Long id;
    private String name;
    private String path;
    private String type;
    private String accessRole;
}
```

```java
@PostMapping("/documents")
public DocumentDto uploadDocument(@RequestParam MultipartFile file, @RequestParam String name) {...}
```

---

## Conclusion

This comprehensive low-level technical design document provides detailed specifications for all 20 epics of the Warehouse EMS system. Each section includes Spring Boot best practices, entity designs with JPA annotations, service layer specifications, REST controller endpoints, and sample code implementations. The design follows industry standards for enterprise Spring Boot applications with proper separation of concerns, security considerations, and scalability patterns.

Developers can use this document as a blueprint for implementing each epic, ensuring consistency across the entire system while maintaining flexibility for future enhancements and integrations.