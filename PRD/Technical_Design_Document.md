# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

---

## Table of Contents

1. [Introduction](#introduction)
2. [Spring Boot Architecture Overview](#spring-boot-architecture-overview)
3. [Package Structure & Module Definitions](#package-structure--module-definitions)
4. [Epic-by-Epic Technical Design](#epic-by-epic-technical-design)
    - E01: Project Scaffolding & Domain Setup
    - E02: Employee Master Data (CRUD)
    - E03: Role Based Access Control (RBAC)
    - E04: Time & Attendance (Clock In/Out)
    - E05: Shift & Schedule Management
    - E06: Leave & Absence Management
    - E07: Training & Certification Tracking
    - E08: Safety Incidents & OSHA Reporting
    - E09: Equipment & Asset Assignment
    - E10: Performance Reviews & Goals
    - E11: Payroll Export Integration
    - E12: Notifications & Announcements
    - E13: Integration Layer (HRIS/WMS APIs)
    - E14: Audit Trail & Compliance
    - E15: Reporting & Analytics
    - E16: Mobile Access (PWA)
    - E17: Onboarding & Offboarding Workflow
    - E18: Localization & Multi-Warehouse
    - E19: Automated Testing & CI/CD
    - E20: Documentation & Runbooks
5. [Security & Configuration](#security--configuration)
6. [Integration Points](#integration-points)
7. [Appendix: Code Snippets & Patterns](#appendix-code-snippets--patterns)

---

## Introduction

This document provides a comprehensive low-level technical design for the Warehouse Employee Management System (EMS), built using Spring Boot. It covers architecture, package structure, domain models, service/repository/controller specifications, security, integration, and code samples for all 20 epics and their user stories.

---

## Spring Boot Architecture Overview

- **Layered Architecture:** Follows Controller-Service-Repository pattern.
- **Domain-Driven Design:** Each module encapsulates its domain logic.
- **RESTful APIs:** All endpoints follow REST conventions, documented via OpenAPI.
- **Security:** Spring Security with RBAC, OAuth2, and API key support.
- **Database:** PostgreSQL (default), migrations via Flyway/Liquibase.
- **Integration:** External APIs via Feign/WebClient, SFTP for payroll, webhooks.
- **Testing:** JUnit, Mockito, Testcontainers, CI/CD via GitHub Actions.
- **Mobile:** PWA support for mobile access.

---

## Package Structure & Module Definitions

```
com.warehouse.ems
âââ config
âââ employee
âââ rbac
âââ attendance
âââ shift
âââ leave
âââ training
âââ safety
âââ equipment
âââ performance
âââ payroll
âââ notification
âââ integration
âââ audit
âââ reporting
âââ mobile
âââ onboarding
âââ localization
âââ test
âââ docs
```

- Each module contains: `controller`, `service`, `repository`, `model`, `dto`, `mapper`, `exception`.

---

## Epic-by-Epic Technical Design

---

### Section: E01 - Project Scaffolding & Domain Setup

**Description:**  
Initialize Spring Boot project, configure base packages, set up core modules, enable DB migrations, and actuator.

**Design Specification:**
- Maven project with parent POM.
- Base packages as per structure above.
- Flyway/Liquibase for DB migrations.
- Spring Boot Actuator enabled.
- README with build/run steps.

**Sample Implementation:**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

```yaml
# application.yml
server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

---

### Section: E02 - Employee Master Data (CRUD)

**Description:**  
CRUD APIs for employee data: name, badgeId, role, department, shiftGroup, hireDate, status.

**Design Specification:**
- Entity: `Employee`
- Repository: `EmployeeRepository extends JpaRepository`
- Service: `EmployeeService`
- Controller: `EmployeeController`
- DTOs: `EmployeeDto`, `EmployeeCreateRequest`, `EmployeeUpdateRequest`
- Unique badgeId enforced.
- Soft-delete via `deleted` flag.
- Pagination, filtering.
- OpenAPI documentation.

**Sample Implementation:**

```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue private Long id;
    @Column(nullable = false, unique = true) private String badgeId;
    private String name;
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
    @PostMapping public ResponseEntity<EmployeeDto> create(@RequestBody EmployeeCreateRequest req) { ... }
    @GetMapping public Page<EmployeeDto> list(Pageable pageable, @RequestParam Map<String,String> filters) { ... }
    @PutMapping("/{id}") public EmployeeDto update(@PathVariable Long id, @RequestBody EmployeeUpdateRequest req) { ... }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { ... }
}
```

---

### Section: E03 - Role Based Access Control (RBAC)

**Description:**  
Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER); method/endpoint security; API key/OAuth2 toggle.

**Design Specification:**
- Roles: Enum `Role {ADMIN, HR, SUPERVISOR, WORKER}`
- SecurityConfig: configure HTTP security, method security.
- Row-level constraints in service layer.
- API key/OAuth2 toggle via properties.

**Sample Implementation:**

```java
@EnableWebSecurity
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
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isSupervisorOf(authentication, #employeeId))")
public EmployeeDto getEmployee(Long employeeId) { ... }
```

---

### Section: E04 - Time & Attendance (Clock In/Out)

**Description:**  
Endpoints for clock-in/out events, geofence/device capture, hours calculation, missed punch correction.

**Design Specification:**
- Entity: `AttendanceEvent`
- Service: `AttendanceService`
- Controller: `AttendanceController`
- Geofence/device info in DTO.
- Correction workflow via approval tasks.

**Sample Implementation:**

```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private LocalDateTime timestamp;
    private AttendanceType type; // CLOCK_IN, CLOCK_OUT
    private String deviceId;
    private GeoLocation geoLocation;
    private boolean correctionRequested;
    // getters/setters
}
```

```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in") public ResponseEntity<?> clockIn(@RequestBody ClockInRequest req) { ... }
    @PostMapping("/clock-out") public ResponseEntity<?> clockOut(@RequestBody ClockOutRequest req) { ... }
    @PostMapping("/correction") public ResponseEntity<?> requestCorrection(@RequestBody CorrectionRequest req) { ... }
}
```

---

### Section: E05 - Shift & Schedule Management

**Description:**  
Recurring shift templates, rotations, overtime rules, assignment, blackout dates, operation calendars.

**Design Specification:**
- Entities: `ShiftTemplate`, `ShiftAssignment`, `OperationCalendar`
- Service: `ShiftService`
- Controller: `ShiftController`
- Conflict detection logic.
- Bulk assignment endpoints.

**Sample Implementation:**

```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    private String recurrencePattern;
    // getters/setters
}
```

```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates") public ShiftTemplate createTemplate(@RequestBody ShiftTemplateDto dto) { ... }
    @PostMapping("/assign") public void assignShifts(@RequestBody ShiftAssignmentRequest req) { ... }
}
```

---

### Section: E06 - Leave & Absence Management

**Description:**  
Request/approve PTO, sick, unpaid leave; accrual balances; integration with scheduling/payroll.

**Design Specification:**
- Entities: `LeaveRequest`, `LeaveBalance`, `LeavePolicy`
- Service: `LeaveService`
- Controller: `LeaveController`
- Approval workflow.
- Integration hooks for scheduling/payroll.

**Sample Implementation:**

```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private LeaveType type; // PTO, SICK, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveStatus status; // REQUESTED, APPROVED, DENIED
    // getters/setters
}
```

```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping public LeaveRequest submit(@RequestBody LeaveRequestDto dto) { ... }
    @PostMapping("/{id}/approve") public void approve(@PathVariable Long id) { ... }
    @PostMapping("/{id}/deny") public void deny(@PathVariable Long id) { ... }
}
```

---

### Section: E07 - Training & Certification Tracking

**Description:**  
Track certifications, expirations, renewals; block assignments for expired certs; upload proof.

**Design Specification:**
- Entities: `Certification`, `EmployeeCertification`
- Service: `CertificationService`
- Controller: `CertificationController`
- Expiry alerts.
- Assignment checks.

**Sample Implementation:**

```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalDate expiryDate;
    private String documentUrl;
    // getters/setters
}
```

```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping public Certification add(@RequestBody CertificationDto dto) { ... }
    @GetMapping("/alerts") public List<CertificationAlert> getAlerts() { ... }
}
```

---

### Section: E08 - Safety Incidents & OSHA Reporting

**Description:**  
Record incidents, severity, location, involved employees; investigation workflow; OSHA summary.

**Design Specification:**
- Entities: `SafetyIncident`, `IncidentInvestigation`
- Service: `SafetyService`
- Controller: `SafetyController`
- Status workflow.
- OSHA export endpoints.

**Sample Implementation:**

```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private String description;
    private IncidentSeverity severity;
    private String location;
    @ManyToMany private List<Employee> involvedEmployees;
    private IncidentStatus status; // OPEN, INVESTIGATING, RESOLVED
    // getters/setters
}
```

```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyController {
    @PostMapping public SafetyIncident report(@RequestBody SafetyIncidentDto dto) { ... }
    @GetMapping("/osha/export") public ResponseEntity<Resource> exportOSHA() { ... }
}
```

---

### Section: E09 - Equipment & Asset Assignment

**Description:**  
Assign assets to employees; track checkout/return; block use if cert missing; asset condition.

**Design Specification:**
- Entities: `Asset`, `AssetAssignment`
- Service: `AssetService`
- Controller: `AssetController`
- Certification checks.
- History logs.

**Sample Implementation:**

```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String type;
    private String serialNumber;
    private AssetCondition condition;
    // getters/setters
}
```

```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign") public AssetAssignment assign(@RequestBody AssetAssignmentDto dto) { ... }
    @PostMapping("/return") public void returnAsset(@RequestBody AssetReturnDto dto) { ... }
}
```

---

### Section: E10 - Performance Reviews & Goals

**Description:**  
Review templates, goals, competencies, ratings, comments; supervisor/employee acknowledgements.

**Design Specification:**
- Entities: `PerformanceReview`, `ReviewCycle`, `Goal`
- Service: `PerformanceService`
- Controller: `PerformanceController`
- PDF export.
- Immutable history after sign-off.

**Sample Implementation:**

```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String cycle;
    private String comments;
    private int rating;
    private boolean acknowledged;
    // getters/setters
}
```

```java
@RestController
@RequestMapping("/performance")
public class PerformanceController {
    @PostMapping("/reviews") public PerformanceReview submit(@RequestBody ReviewDto dto) { ... }
    @GetMapping("/reviews/{id}/pdf") public ResponseEntity<Resource> exportPdf(@PathVariable Long id) { ... }
}
```

---

### Section: E11 - Payroll Export Integration

**Description:**  
Generate payroll files from attendance/leave; map to provider formats; secure delivery.

**Design Specification:**
- Service: `PayrollExportService`
- Integration: SFTP/API delivery.
- Audit log for exports.

**Sample Implementation:**

```java
@Service
public class PayrollExportService {
    public Resource generatePayrollExport(PayrollExportRequest req) { ... }
    public void deliverExport(Resource exportFile) { ... }
}
```

---

### Section: E12 - Notifications & Announcements

**Description:**  
In-app/email/SMS notifications for shifts, certs, approvals, announcements; quiet hours.

**Design Specification:**
- Entities: `Notification`, `Announcement`
- Service: `NotificationService`
- Controller: `NotificationController`
- Channel opt-in/out.
- Delivery status tracking.

**Sample Implementation:**

```java
@Entity
public class Notification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee recipient;
    private NotificationType type;
    private String message;
    private NotificationChannel channel;
    private boolean delivered;
    // getters/setters
}
```

```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping public void send(@RequestBody NotificationRequest req) { ... }
    @GetMapping public List<Notification> list(@RequestParam Long employeeId) { ... }
}
```

---

### Section: E13 - Integration Layer (HRIS/WMS APIs)

**Description:**  
Expose REST APIs/connectors for HRIS, WMS, IDP; webhooks for events.

**Design Specification:**
- API endpoints: `/api/hris`, `/api/wms`, `/api/idp`
- JWT/OAuth2 security.
- Feign/WebClient for outbound calls.
- Webhook controller.

**Sample Implementation:**

```java
@RestController
@RequestMapping("/api/hris")
public class HRISController {
    @PostMapping("/sync") public void syncEmployees(@RequestBody HRISSyncRequest req) { ... }
}
```

```java
@FeignClient(name = "wmsClient", url = "${wms.api.url}")
public interface WMSClient {
    @GetMapping("/departments") List<DepartmentDto> getDepartments();
}
```

---

### Section: E14 - Audit Trail & Compliance

**Description:**  
Centralized audit logging for sensitive changes; tamper-evident storage.

**Design Specification:**
- Entity: `AuditLog`
- Service: `AuditService`
- Aspect for logging changes.
- Export endpoints.

**Sample Implementation:**

```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String entity;
    private Long entityId;
    private String actor;
    private LocalDateTime timestamp;
    private String before;
    private String after;
    // getters/setters
}
```

```java
@Aspect
@Component
public class AuditAspect {
    @AfterReturning(pointcut = "execution(* com.warehouse.ems..*.save*(..))" returning = "result")
    public void logChange(JoinPoint jp, Object result) { ... }
}
```

---

### Section: E15 - Reporting & Analytics

**Description:**  
Operational reports: attendance, overtime, leave, cert status, safety KPIs; CSV/PDF export; dashboards.

**Design Specification:**
- Service: `ReportingService`
- Controller: `ReportingController`
- Role-based access.
- Metrics endpoints.

**Sample Implementation:**

```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance") public ResponseEntity<Resource> attendanceReport(@RequestParam ReportParams params) { ... }
    @GetMapping("/metrics") public MetricsDto getMetrics(@RequestParam Map<String,String> filters) { ... }
}
```

---

### Section: E16 - Mobile Access (PWA)

**Description:**  
Responsive views for clock-in/out, schedules, leave, announcements; offline-friendly PWA.

**Design Specification:**
- Frontend: PWA manifest, service worker.
- Backend: endpoints optimized for mobile.
- Offline queue for clock events.

**Sample Implementation:**

```json
// manifest.json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "icons": [ ... ]
}
```

---

### Section: E17 - Onboarding & Offboarding Workflow

**Description:**  
Automate provisioning, initial schedule, training; deprovision access/assets on termination.

**Design Specification:**
- Service: `OnboardingService`, `OffboardingService`
- Workflow engine (e.g., Spring State Machine).
- Integration with HRIS, asset, training modules.

**Sample Implementation:**

```java
@Service
public class OnboardingService {
    public void onboardEmployee(HRISEvent event) { ... }
}
```

---

### Section: E18 - Localization & Multi-Warehouse

**Description:**  
Support multiple warehouses, languages, time zones.

**Design Specification:**
- Entities: `Warehouse`, `Localization`
- Service: `LocalizationService`
- i18n resource bundles.
- Warehouse context in all queries.

**Sample Implementation:**

```java
@Entity
public class Warehouse {
    @Id @GeneratedValue private Long id;
    private String name;
    private String location;
    private String timezone;
    // getters/setters
}
```

---

### Section: E19 - Automated Testing & CI/CD

**Description:**  
JUnit/Mockito/Testcontainers; CI/CD via GitHub Actions.

**Design Specification:**
- Test classes per module.
- Integration tests with Testcontainers.
- CI/CD pipeline config.

**Sample Implementation:**

```java
@SpringBootTest
public class EmployeeServiceTest {
    @Autowired EmployeeService service;
    @Test public void testCreateEmployee() { ... }
}
```

```yaml
# .github/workflows/ci.yml
name: CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Build & Test
        run: mvn clean verify
```

---

### Section: E20 - Documentation & Runbooks

**Description:**  
Comprehensive docs, OpenAPI, runbooks for ops.

**Design Specification:**
- OpenAPI auto-generated via springdoc.
- Markdown runbooks in `/docs`.
- README with build/run/test steps.

**Sample Implementation:**

```java
@OpenAPIDefinition(
    info = @Info(title = "Warehouse EMS API", version = "1.0", description = "API documentation for Warehouse EMS")
)
@SpringBootApplication
public class WarehouseEmsApplication { ... }
```

---

## Security & Configuration

- RBAC via Spring Security.
- OAuth2/JWT for API.
- API key toggle via `application.yml`.
- Sensitive endpoints require ADMIN/HR.
- Row-level security in service layer.
- CORS configured for mobile/PWA.
- Quiet hours for notifications.

---

## Integration Points

- HRIS: Feign/WebClient, JWT-secured.
- WMS: REST API, department/location sync.
- Payroll: SFTP/API, export job.
- IDP: SSO via OAuth2.
- Webhooks: Event-driven, idempotent.

---

## Appendix: Code Snippets & Patterns

- **Repository Pattern:** `JpaRepository` for all entities.
- **DTO/Mapper Pattern:** MapStruct for DTO/entity conversion.
- **Service Layer:** Business logic, transaction management.
- **Controller Layer:** REST endpoints, validation.
- **Exception Handling:** `@ControllerAdvice` for global error handling.
- **Audit Aspect:** AOP for audit logging.
- **Testing:** JUnit, Mockito, Testcontainers.
- **CI/CD:** GitHub Actions workflow.

---

## End of Document

---

This document provides a comprehensive low-level technical design for all 20 epics of the Warehouse EMS system, following Spring Boot best practices and industry standards. Each section includes architecture, design specifications, and sample implementations to guide development teams.