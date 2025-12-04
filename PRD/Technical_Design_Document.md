# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

---

## Table of Contents

1. [E01: Project Scaffolding & Domain Setup](#e01)
2. [E02: Employee Master Data (CRUD)](#e02)
3. [E03: Role Based Access Control (RBAC)](#e03)
4. [E04: Time & Attendance (Clock In/Out)](#e04)
5. [E05: Shift & Schedule Management](#e05)
6. [E06: Leave & Absence Management](#e06)
7. [E07: Training & Certification Tracking](#e07)
8. [E08: Safety Incidents & OSHA Reporting](#e08)
9. [E09: Equipment & Asset Assignment](#e09)
10. [E10: Performance Reviews & Goals](#e10)
11. [E11: Payroll Export Integration](#e11)
12. [E12: Notifications & Announcements](#e12)
13. [E13: Integration Layer (HRIS/WMS APIs)](#e13)
14. [E14: Audit Trail & Compliance](#e14)
15. [E15: Reporting & Analytics](#e15)
16. [E16: Mobile Access (PWA)](#e16)
17. [E17: Onboarding & Offboarding Workflow](#e17)

---

## <a name="e01"></a>Section: E01 - Project Scaffolding & Domain Setup

Description:
Establishes the foundational Spring Boot project structure, base packages, and core modules. Integrates Flyway/Liquibase for DB migrations and enables Actuator for health monitoring.

Design Specification:
- Spring Boot 3.x (Maven)
- Base package: `com.warehouse.ems`
- Modules: `employee`, `scheduling`, `attendance`, `safety`
- DB migration: Flyway/Liquibase
- Monitoring: Spring Boot Actuator
- README with build/run steps

Sample Implementation:
```java
// Maven pom.xml dependencies
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

// Application structure
com.warehouse.ems
 âââ employee
 âââ scheduling
 âââ attendance
 âââ safety
 âââ config

// application.properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5432/ems
spring.flyway.enabled=true

// Health endpoint
GET /actuator/health
```

---

## <a name="e02"></a>Section: E02 - Employee Master Data (CRUD)

Description:
Implements the Employee domain with full CRUD REST APIs, DTOs, and validation. Ensures unique badgeId, supports soft-delete, pagination, and filtering.

Design Specification:
- Entity: `Employee` (name, badgeId, role, department, shiftGroup, hireDate, status)
- Repository: `EmployeeRepository` (Spring Data JPA)
- Service: `EmployeeService` (business logic)
- Controller: `EmployeeController` (REST endpoints)
- DTOs: `EmployeeDto`, `EmployeeCreateDto`, `EmployeeUpdateDto`
- Validation: Bean Validation (JSR-380)
- Soft-delete: `deleted` flag
- Pagination: `Pageable`
- OpenAPI documentation

Sample Implementation:
```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id @GeneratedValue
    private Long id;
    @Column(unique = true, nullable = false)
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted = false;
    // getters/setters
}

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeCreateDto dto) { ... }
    @GetMapping
    public Page<EmployeeDto> list(Pageable pageable, @RequestParam Map<String, String> filters) { ... }
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateDto dto) { ... }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) { ... }
}
```

---

## <a name="e03"></a>Section: E03 - Role Based Access Control (RBAC)

Description:
Integrates Spring Security with role-based access (ADMIN, HR, SUPERVISOR, WORKER). Secures endpoints and methods, supports API key/OAuth2 toggle via config.

Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Security config: `SecurityConfig`
- Method security: `@PreAuthorize`
- Endpoint security: `HttpSecurity`
- API key/OAuth2 toggle: `application.properties`
- Row-level constraints in repositories/services

Sample Implementation:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            .and()
            .oauth2Login()
            .and()
            .httpBasic();
    }
}

@Service
public class EmployeeService {
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public EmployeeDto createEmployee(EmployeeCreateDto dto) { ... }
}
```

---

## <a name="e04"></a>Section: E04 - Time & Attendance (Clock In/Out)

Description:
Provides endpoints for clock-in/out events, geofence/device capture, shift association, missed punch correction workflow, and daily totals computation.

Design Specification:
- Entity: `AttendanceEvent` (employeeId, timestamp, type, deviceId, location)
- Controller: `AttendanceController`
- Service: `AttendanceService`
- Correction workflow: approval tasks
- Reports: CSV export

Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private LocalDateTime timestamp;
    private String type; // CLOCK_IN, CLOCK_OUT
    private String deviceId;
    private String location;
    // getters/setters
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<Void> clockIn(@RequestBody AttendanceEventDto dto) { ... }
    @PostMapping("/clock-out")
    public ResponseEntity<Void> clockOut(@RequestBody AttendanceEventDto dto) { ... }
    @GetMapping("/report")
    public ResponseEntity<Resource> exportReport(@RequestParam LocalDate date) { ... }
}
```

---

## <a name="e05"></a>Section: E05 - Shift & Schedule Management

Description:
Manages recurring shift templates, rotations, overtime rules, blackout dates, and employee assignments. Detects/prevents conflicts and supports bulk assignment.

Design Specification:
- Entity: `ShiftTemplate`, `ShiftAssignment`
- Controller: `ShiftController`
- Service: `ShiftService`
- Conflict detection logic
- Audit entries for changes

Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String rotationType;
    // getters/setters
}

@Entity
public class ShiftAssignment {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private Long shiftTemplateId;
    private LocalDate date;
    // getters/setters
}

@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates")
    public ResponseEntity<ShiftTemplateDto> createTemplate(@RequestBody ShiftTemplateDto dto) { ... }
    @PostMapping("/assign")
    public ResponseEntity<Void> assignShift(@RequestBody ShiftAssignmentDto dto) { ... }
}
```

---

## <a name="e06"></a>Section: E06 - Leave & Absence Management

Description:
Implements PTO, sick, unpaid leave request/approval, accrual balances, and integration with scheduling/payroll.

Design Specification:
- Entity: `LeaveRequest`, `LeaveBalance`
- Controller: `LeaveController`
- Service: `LeaveService`
- Approval workflow
- Integration hooks for scheduling/payroll

Sample Implementation:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type; // PTO, SICK, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // REQUESTED, APPROVED, DENIED
    // getters/setters
}

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/request")
    public ResponseEntity<LeaveRequestDto> requestLeave(@RequestBody LeaveRequestDto dto) { ... }
    @PostMapping("/approve/{id}")
    public ResponseEntity<Void> approveLeave(@PathVariable Long id) { ... }
}
```

---

## <a name="e07"></a>Section: E07 - Training & Certification Tracking

Description:
Tracks certifications, expirations, renewals, and blocks assignment to tasks requiring expired certs. Supports document uploads.

Design Specification:
- Entity: `Certification`, `EmployeeCertification`
- Controller: `CertificationController`
- Service: `CertificationService`
- Alerts for expiry
- Document upload (Spring Boot file upload)

Sample Implementation:
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalDate expiryDate;
    // getters/setters
}

@Entity
public class EmployeeCertification {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private Long certificationId;
    private LocalDate acquiredDate;
    private LocalDate expiryDate;
    private String documentUrl;
    // getters/setters
}

@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<EmployeeCertificationDto> addCertification(@RequestBody EmployeeCertificationDto dto) { ... }
    @GetMapping("/alerts")
    public List<CertificationAlertDto> getExpiringCerts() { ... }
}
```

---

## <a name="e08"></a>Section: E08 - Safety Incidents & OSHA Reporting

Description:
Records safety incidents/near-misses, manages investigation workflow, and generates OSHA summary reports.

Design Specification:
- Entity: `SafetyIncident`
- Controller: `SafetyController`
- Service: `SafetyService`
- Workflow: status transitions (Open, Investigating, Resolved)
- OSHA report export

Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String severity;
    private String location;
    private String description;
    private Long reportedBy;
    private String status; // OPEN, INVESTIGATING, RESOLVED
    // getters/setters
}

@RestController
@RequestMapping("/safety/incidents")
public class SafetyController {
    @PostMapping
    public ResponseEntity<SafetyIncidentDto> reportIncident(@RequestBody SafetyIncidentDto dto) { ... }
    @PostMapping("/{id}/resolve")
    public ResponseEntity<Void> resolveIncident(@PathVariable Long id) { ... }
    @GetMapping("/osharesport")
    public ResponseEntity<Resource> exportOSHAReport() { ... }
}
```

---

## <a name="e09"></a>Section: E09 - Equipment & Asset Assignment

Description:
Manages asset registry, assignment to employees, check-in/out, certification validation, and asset condition tracking.

Design Specification:
- Entity: `Asset`, `AssetAssignment`
- Controller: `AssetController`
- Service: `AssetService`
- Certification check before assignment
- History log per asset/employee

Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type; // Scanner, Forklift, PPE
    private String condition;
    private boolean available;
    // getters/setters
}

@Entity
public class AssetAssignment {
    @Id @GeneratedValue
    private Long id;
    private Long assetId;
    private Long employeeId;
    private LocalDateTime assignedAt;
    private LocalDateTime returnedAt;
    // getters/setters
}

@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign")
    public ResponseEntity<Void> assignAsset(@RequestBody AssetAssignmentDto dto) { ... }
    @PostMapping("/return")
    public ResponseEntity<Void> returnAsset(@RequestBody AssetAssignmentDto dto) { ... }
}
```

---

## <a name="e10"></a>Section: E10 - Performance Reviews & Goals

Description:
Implements review templates, goal tracking, competencies, ratings, comments, and supervisor/employee acknowledgements.

Design Specification:
- Entity: `PerformanceReview`, `ReviewCycle`, `Goal`
- Controller: `ReviewController`
- Service: `ReviewService`
- PDF export
- Immutable history after sign-off

Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private Long reviewCycleId;
    private String competencies;
    private String ratings;
    private String comments;
    private boolean acknowledgedByEmployee;
    private boolean acknowledgedBySupervisor;
    // getters/setters
}

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping
    public ResponseEntity<PerformanceReviewDto> createReview(@RequestBody PerformanceReviewDto dto) { ... }
    @GetMapping("/export/{id}")
    public ResponseEntity<Resource> exportReviewPdf(@PathVariable Long id) { ... }
}
```

---

## <a name="e11"></a>Section: E11 - Payroll Export Integration

Description:
Generates payroll-ready files from attendance/leave, maps to provider formats, and delivers securely via SFTP/API.

Design Specification:
- Service: `PayrollExportService`
- Integration: SFTP/API client
- Audit log for exports
- Retry/backoff logic

Sample Implementation:
```java
@Service
public class PayrollExportService {
    public void exportPayroll(LocalDate periodStart, LocalDate periodEnd) {
        // Fetch approved attendance/leave
        // Map to provider schema
        // Deliver via SFTP/API
        // Log export event
    }
}
```

---

## <a name="e12"></a>Section: E12 - Notifications & Announcements

Description:
Delivers in-app, email, and SMS notifications for shift changes, expiring certs, approvals, and announcements. Supports quiet hours and opt-in/out.

Design Specification:
- Entity: `Notification`, `Announcement`
- Controller: `NotificationController`
- Service: `NotificationService`
- Channel opt-in/out
- Delivery status tracking
- Rate limiting

Sample Implementation:
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String channel; // EMAIL, SMS, IN_APP
    private String message;
    private LocalDateTime sentAt;
    private boolean delivered;
    // getters/setters
}

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationDto dto) { ... }
}
```

---

## <a name="e13"></a>Section: E13 - Integration Layer (HRIS/WMS APIs)

Description:
Exposes REST APIs and connectors for HRIS, WMS, and IDP (SSO). Supports webhooks for events and JWT/OAuth2 security.

Design Specification:
- Controller: `IntegrationController`
- Service: `IntegrationService`
- JWT/OAuth2 security
- HRIS sync job
- Webhook endpoints
- OpenAPI documentation

Sample Implementation:
```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    @PostMapping("/hris/sync")
    public ResponseEntity<Void> syncHris(@RequestBody HrisSyncDto dto) { ... }
    @PostMapping("/wms/link")
    public ResponseEntity<Void> linkWms(@RequestBody WmsLinkDto dto) { ... }
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody WebhookEventDto dto) { ... }
}
```

---

## <a name="e14"></a>Section: E14 - Audit Trail & Compliance

Description:
Centralizes audit logging for sensitive changes, stores immutable logs, and supports export by date/user/entity.

Design Specification:
- Entity: `AuditLog`
- Service: `AuditService`
- Controller: `AuditController`
- Tamper-evident storage
- Export endpoints

Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private Long entityId;
    private String action;
    private String actor;
    private LocalDateTime timestamp;
    private String before;
    private String after;
    // getters/setters
}

@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/export")
    public ResponseEntity<Resource> exportAuditLogs(@RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
}
```

---

## <a name="e15"></a>Section: E15 - Reporting & Analytics

Description:
Provides dashboards and scheduled reports for attendance, leave, safety, and performance. Supports CSV/PDF export.

Design Specification:
- Service: `ReportingService`
- Controller: `ReportingController`
- Scheduled jobs: Spring Scheduler
- Export formats: CSV, PDF
- Caching: Redis

Sample Implementation:
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> attendanceReport(@RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
    @GetMapping("/safety")
    public ResponseEntity<Resource> safetyReport() { ... }
}
```

---

## <a name="e16"></a>Section: E16 - Mobile Access (PWA)

Description:
Delivers a Progressive Web App for clock-in/out, schedule viewing, leave requests, and notifications. Supports offline mode and push notifications.

Design Specification:
- PWA manifest
- Service worker for offline
- Push notification API
- Responsive UI
- Geolocation API

Sample Implementation:
```javascript
// manifest.json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone",
  "icons": [...]
}

// service-worker.js
self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request).then(response => response || fetch(event.request))
  );
});
```

---

## <a name="e17"></a>Section: E17 - Onboarding & Offboarding Workflow

Description:
Automates onboarding (account, training, asset assignment) and offboarding (access revocation, asset return, exit interview).

Design Specification:
- Entity: `OnboardingTask`, `OffboardingTask`
- Service: `WorkflowService`
- Controller: `WorkflowController`
- Task checklists
- Notifications per step

Sample Implementation:
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String taskName;
    private boolean completed;
    // getters/setters
}

@RestController
@RequestMapping("/workflow")
public class WorkflowController {
    @PostMapping("/onboard")
    public ResponseEntity<Void> startOnboarding(@RequestBody OnboardingDto dto) { ... }
    @PostMapping("/offboard")
    public ResponseEntity<Void> startOffboarding(@RequestBody OffboardingDto dto) { ... }
}
```

---

## End of Document