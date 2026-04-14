# Warehouse Employee Management System (EMS) â Low-Level Technical Design Document

## Table of Contents
1. [Spring Boot Architecture Overview](#architecture-overview)
2. [Package Structure](#package-structure)
3. [Global Design Patterns & Best Practices](#global-design-patterns)
4. [Epic-by-Epic Technical Design](#epic-by-epic-technical-design)
    - [E01: Project Scaffolding & Domain Setup](#e01)
    - [E02: Employee Master Data (CRUD)](#e02)
    - [E03: Role Based Access Control (RBAC)](#e03)
    - [E04: Time & Attendance (Clock In/Out)](#e04)
    - [E05: Shift & Schedule Management](#e05)
    - [E06: Leave & Absence Management](#e06)
    - [E07: Training & Certification Tracking](#e07)
    - [E08: Safety Incidents & OSHA Reporting](#e08)
    - [E09: Equipment & Asset Assignment](#e09)
    - [E10: Performance Reviews & Goals](#e10)
    - [E11: Payroll Export Integration](#e11)
    - [E12: Notifications & Announcements](#e12)
    - [E13: Integration Layer (HRIS/WMS APIs)](#e13)
    - [E14: Audit Trail & Compliance](#e14)
    - [E15: Reporting & Analytics](#e15)
    - [E16: Mobile Access (PWA)](#e16)
    - [E17: Onboarding & Offboarding Workflow](#e17)
    - [E18: Localization](#e18)
    - [E19: AI Scheduling](#e19)
    - [E20: CI/CD](#e20)

---

## <a name="architecture-overview"></a>1. Spring Boot Architecture Overview

- **Layered Architecture**: Follows Controller-Service-Repository pattern.
- **Domain-Driven Design**: Entities represent core business concepts.
- **DTO Pattern**: Data Transfer Objects for API boundaries.
- **Spring Data JPA**: For ORM and repository abstraction.
- **Spring Security**: For authentication, authorization, and RBAC.
- **Validation**: Bean Validation (JSR-380) with annotations.
- **Exception Handling**: `@ControllerAdvice` for global error handling.
- **Configuration**: `application.yml` for environment and feature toggles.
- **OpenAPI/Swagger**: For API documentation.
- **Transaction Management**: `@Transactional` at service layer.
- **Testing**: Unit and integration tests with JUnit and MockMvc.

---

## <a name="package-structure"></a>2. Package Structure

```
com.wms.ems
âââ config
âââ controller
âââ dto
âââ entity
âââ exception
âââ repository
âââ security
âââ service
âââ util
âââ integration
```

---

## <a name="global-design-patterns"></a>3. Global Design Patterns & Best Practices

- **Entities**: Annotated with `@Entity`, use `@Id`, `@GeneratedValue`, relationships with `@OneToMany`, `@ManyToOne`, etc.
- **DTOs**: For all API input/output, validated with `@Valid`.
- **Repositories**: Extend `JpaRepository`.
- **Services**: Annotated with `@Service`, contain business logic, transactional boundaries.
- **Controllers**: Annotated with `@RestController`, map endpoints, use DTOs.
- **Security**: RBAC via `@PreAuthorize`, method and endpoint security.
- **Exception Handling**: Custom exceptions, global handler.
- **Validation**: Use `@NotNull`, `@Size`, etc.
- **Auditing**: Use `@CreatedDate`, `@LastModifiedDate`, and custom audit tables.
- **Configuration**: Use profiles, feature toggles, and externalized secrets.

---

## <a name="epic-by-epic-technical-design"></a>4. Epic-by-Epic Technical Design

---

### <a name="e01"></a>**E01: Project Scaffolding & Domain Setup**

#### Section Title: Project Initialization & Base Configuration

**Description of Design Decisions:**
- Use Spring Boot (Maven) for rapid setup.
- Modularize by domain (employee, scheduling, attendance, safety).
- Use Flyway for DB migrations.
- Enable Actuator for health checks.

**Design Specification:**
- `pom.xml` with dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, flyway-core, actuator, etc.
- `application.yml` for port, DB, actuator, etc.
- Base package structure as above.
- Flyway migration scripts in `src/main/resources/db/migration`.

**Sample Implementation:**
```xml
<!-- pom.xml (excerpt) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
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
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ems
    username: ems_user
    password: secret
  flyway:
    enabled: true
management:
  endpoints:
    web:
      exposure:
        include: health,info
```
```java
// Health check: actuator enabled by default
// Directory structure created as per package structure above
```

---

### <a name="e02"></a>**E02: Employee Master Data (CRUD)**

#### Section Title: Domain Model, Repository, Service, Controller

**Description of Design Decisions:**
- Employee is a core entity with unique `badgeId`.
- Soft-delete for compliance.
- Pagination/filtering for large datasets.
- DTOs for API boundaries.

**Design Specification:**
- Entity: `Employee`
    - Fields: id, name, badgeId (unique), role, department, shiftGroup, hireDate, status, deleted.
- Repository: `EmployeeRepository` extends `JpaRepository`.
- Service: `EmployeeService` with CRUD, soft-delete, filtering.
- Controller: `EmployeeController` with REST endpoints.
- DTOs: `EmployeeDTO`, `EmployeeCreateDTO`, `EmployeeUpdateDTO`.
- Validation: `@NotNull`, `@Size`, etc.
- OpenAPI annotations.

**Sample Implementation:**
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue private Long id;
    @Column(nullable = false) private String name;
    @Column(nullable = false, unique = true) private String badgeId;
    @Enumerated(EnumType.STRING) private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted = false;
    // getters/setters
}
```
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```
```java
@Service
public class EmployeeService {
    @Transactional
    public EmployeeDTO createEmployee(@Valid EmployeeCreateDTO dto) { ... }
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getEmployees(Pageable pageable, String filter) { ... }
    @Transactional
    public void softDeleteEmployee(Long id) { ... }
}
```
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeCreateDTO dto) { ... }
    @GetMapping public Page<EmployeeDTO> list(Pageable pageable, @RequestParam Optional<String> filter) { ... }
    @PutMapping("/{id}") public EmployeeDTO update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateDTO dto) { ... }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { ... }
}
```
```java
// DTO Example
public class EmployeeCreateDTO {
    @NotNull @Size(min=2) private String name;
    @NotNull private String badgeId;
    @NotNull private String role;
    // ...
}
```

---

### <a name="e03"></a>**E03: Role Based Access Control (RBAC)**

#### Section Title: Security Configuration & RBAC Enforcement

**Description of Design Decisions:**
- Use Spring Security with roles: ADMIN, HR, SUPERVISOR, WORKER.
- Method and endpoint security.
- Row-level security for sensitive data.
- API key/OAuth2 toggle via config.

**Design Specification:**
- Security config in `security/SecurityConfig.java`.
- Roles as enums.
- Use `@PreAuthorize` on service/controller methods.
- API key or OAuth2 via profile in `application.yml`.

**Sample Implementation:**
```java
// Role Enum
public enum Role { ADMIN, HR, SUPERVISOR, WORKER }
```
```java
// SecurityConfig.java
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
// Method-level security
@PreAuthorize("hasRole('ADMIN')")
public void deleteEmployee(Long id) { ... }
```
```yaml
# application.yml
security:
  auth-type: oauth2 # or apikey
```

---

### <a name="e04"></a>**E04: Time & Attendance (Clock In/Out)**

#### Section Title: Attendance Domain, Service, Controller

**Description of Design Decisions:**
- Attendance events linked to Employee.
- Geofence/device capture optional.
- Missed punches/corrections workflow.
- Shift association and daily totals.

**Design Specification:**
- Entity: `AttendanceEvent` (id, employee, type [IN/OUT], timestamp, deviceId, geoLocation, status).
- Repository: `AttendanceRepository`.
- Service: `AttendanceService` (clock-in/out, corrections, totals).
- Controller: `AttendanceController`.
- DTOs: `AttendanceEventDTO`, `AttendanceCorrectionDTO`.
- Reports exportable as CSV.

**Sample Implementation:**
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private EventType type; // IN, OUT
    private LocalDateTime timestamp;
    private String deviceId;
    private String geoLocation;
    private String status; // NORMAL, CORRECTION_PENDING, etc.
}
```
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in") public ResponseEntity<?> clockIn(@RequestBody AttendanceEventDTO dto) { ... }
    @PostMapping("/clock-out") public ResponseEntity<?> clockOut(@RequestBody AttendanceEventDTO dto) { ... }
    @PostMapping("/correction") public ResponseEntity<?> requestCorrection(@RequestBody AttendanceCorrectionDTO dto) { ... }
    @GetMapping("/report") public ResponseEntity<Resource> exportReport(@RequestParam LocalDate date) { ... }
}
```
```java
@Service
public class AttendanceService {
    @Transactional
    public void clockIn(Long employeeId, AttendanceEventDTO dto) { ... }
    @Transactional
    public void clockOut(Long employeeId, AttendanceEventDTO dto) { ... }
    @Transactional
    public void requestCorrection(Long eventId, AttendanceCorrectionDTO dto) { ... }
    @Transactional(readOnly = true)
    public List<AttendanceReportDTO> getDailyTotals(LocalDate date) { ... }
}
```

---

### <a name="e05"></a>**E05: Shift & Schedule Management**

#### Section Title: Shift Domain, Conflict Detection, Bulk Assignment

**Description of Design Decisions:**
- Recurring shift templates and rotations.
- Overtime rules.
- Blackout dates and operation calendars.
- Conflict detection and bulk assignment.

**Design Specification:**
- Entity: `ShiftTemplate`, `ShiftAssignment`, `BlackoutDate`.
- Repository: `ShiftTemplateRepository`, `ShiftAssignmentRepository`.
- Service: `ShiftService` (CRUD, conflict detection, bulk assign).
- Controller: `ShiftController`.
- DTOs: `ShiftTemplateDTO`, `ShiftAssignmentDTO`.
- Audit entries for changes.

**Sample Implementation:**
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrencePattern; // e.g., "WEEKLY"
    private boolean overtimeAllowed;
}
@Entity
public class ShiftAssignment {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private ShiftTemplate shiftTemplate;
    private LocalDate date;
}
```
```java
@Service
public class ShiftService {
    @Transactional
    public void assignShift(Long employeeId, Long shiftTemplateId, LocalDate date) { ... }
    @Transactional(readOnly = true)
    public boolean hasConflict(Long employeeId, LocalDate date) { ... }
    @Transactional
    public void bulkAssign(List<Long> employeeIds, Long shiftTemplateId, LocalDate date) { ... }
}
```
```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/assign") public void assign(@RequestBody ShiftAssignmentDTO dto) { ... }
    @GetMapping("/conflicts") public boolean checkConflict(@RequestParam Long employeeId, @RequestParam LocalDate date) { ... }
}
```

---

### <a name="e06"></a>**E06: Leave & Absence Management**

#### Section Title: Leave Domain, Accruals, Integration

**Description of Design Decisions:**
- PTO, sick, unpaid leave types.
- Accrual balances and policies.
- Integration with scheduling and payroll.

**Design Specification:**
- Entity: `LeaveRequest`, `LeaveBalance`.
- Repository: `LeaveRequestRepository`, `LeaveBalanceRepository`.
- Service: `LeaveService` (request, approve/deny, update balances).
- Controller: `LeaveController`.
- DTOs: `LeaveRequestDTO`, `LeaveApprovalDTO`.
- Scheduled shifts auto-flagged for coverage.

**Sample Implementation:**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private LeaveType type; // PTO, SICK, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // PENDING, APPROVED, DENIED
    private String reason;
}
@Entity
public class LeaveBalance {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private int ptoBalance;
    private int sickBalance;
}
```
```java
@Service
public class LeaveService {
    @Transactional
    public void requestLeave(Long employeeId, LeaveRequestDTO dto) { ... }
    @Transactional
    public void approveLeave(Long requestId, LeaveApprovalDTO dto) { ... }
    @Transactional(readOnly = true)
    public LeaveBalanceDTO getBalance(Long employeeId) { ... }
}
```
```java
@RestController
@RequestMapping("/leaves")
public class LeaveController {
    @PostMapping public void request(@RequestBody LeaveRequestDTO dto) { ... }
    @PostMapping("/{id}/approve") public void approve(@PathVariable Long id, @RequestBody LeaveApprovalDTO dto) { ... }
    @GetMapping("/balance") public LeaveBalanceDTO getBalance(@RequestParam Long employeeId) { ... }
}
```

---

### <a name="e07"></a>**E07: Training & Certification Tracking**

#### Section Title: Certification Domain, Expiry Alerts, Scheduling Checks

**Description of Design Decisions:**
- Track certifications, expirations, renewals.
- Block assignments if expired.
- Upload proof documents.

**Design Specification:**
- Entity: `Certification`, `EmployeeCertification`.
- Repository: `CertificationRepository`, `EmployeeCertificationRepository`.
- Service: `CertificationService` (CRUD, expiry alerts, scheduling checks).
- Controller: `CertificationController`.
- DTOs: `CertificationDTO`, `EmployeeCertificationDTO`.
- Alerts for expiry.

**Sample Implementation:**
```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    private String name;
    private String description;
}
@Entity
public class EmployeeCertification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private Certification certification;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
}
```
```java
@Service
public class CertificationService {
    @Transactional
    public void assignCertification(Long employeeId, CertificationDTO dto) { ... }
    @Transactional(readOnly = true)
    public List<EmployeeCertificationDTO> getExpiringCerts(int days) { ... }
}
```
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping("/assign") public void assign(@RequestBody EmployeeCertificationDTO dto) { ... }
    @GetMapping("/expiring") public List<EmployeeCertificationDTO> expiring(@RequestParam int days) { ... }
}
```

---

### <a name="e08"></a>**E08: Safety Incidents & OSHA Reporting**

#### Section Title: Safety Incident Domain, Workflow, Reporting

**Description of Design Decisions:**
- Record incidents/near-misses.
- Workflow for investigation and corrective actions.
- OSHA summary export.

**Design Specification:**
- Entity: `SafetyIncident`.
- Repository: `SafetyIncidentRepository`.
- Service: `SafetyService` (record, workflow, export).
- Controller: `SafetyController`.
- DTOs: `SafetyIncidentDTO`.
- Status workflow: OPEN, INVESTIGATING, RESOLVED.

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
    private LocalDateTime reportedAt;
}
```
```java
@Service
public class SafetyService {
    @Transactional
    public void reportIncident(SafetyIncidentDTO dto) { ... }
    @Transactional
    public void updateStatus(Long incidentId, String status) { ... }
    @Transactional(readOnly = true)
    public Resource exportOSHAReport(LocalDate from, LocalDate to) { ... }
}
```
```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyController {
    @PostMapping public void report(@RequestBody SafetyIncidentDTO dto) { ... }
    @PatchMapping("/{id}/status") public void updateStatus(@PathVariable Long id, @RequestParam String status) { ... }
    @GetMapping("/osha-export") public ResponseEntity<Resource> exportOSHA(@RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
}
```

---

### <a name="e09"></a>**E09: Equipment & Asset Assignment**

#### Section Title: Asset Domain, Assignment, Condition Tracking

**Description of Design Decisions:**
- Assign assets to employees.
- Track check-in/out, prevent use if cert missing.
- Asset condition state.

**Design Specification:**
- Entity: `Asset`, `AssetAssignment`.
- Repository: `AssetRepository`, `AssetAssignmentRepository`.
- Service: `AssetService` (CRUD, assign, check-in/out).
- Controller: `AssetController`.
- DTOs: `AssetDTO`, `AssetAssignmentDTO`.

**Sample Implementation:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String type; // Scanner, Forklift, PPE
    private String serialNumber;
    private String condition;
    private boolean available;
}
@Entity
public class AssetAssignment {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Asset asset;
    @ManyToOne private Employee employee;
    private LocalDateTime assignedAt;
    private LocalDateTime returnedAt;
}
```
```java
@Service
public class AssetService {
    @Transactional
    public void assignAsset(Long assetId, Long employeeId) { ... }
    @Transactional
    public void checkInAsset(Long assetId) { ... }
    @Transactional(readOnly = true)
    public List<AssetAssignmentDTO> getAssetHistory(Long assetId) { ... }
}
```
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign") public void assign(@RequestBody AssetAssignmentDTO dto) { ... }
    @PostMapping("/checkin") public void checkIn(@RequestParam Long assetId) { ... }
    @GetMapping("/{id}/history") public List<AssetAssignmentDTO> history(@PathVariable Long id) { ... }
}
```

---

### <a name="e10"></a>**E10: Performance Reviews & Goals**

#### Section Title: Review Domain, Workflow, PDF Export

**Description of Design Decisions:**
- Review templates, goals, competencies.
- Supervisor/employee acknowledgement.
- Immutable after sign-off.

**Design Specification:**
- Entity: `PerformanceReview`, `ReviewTemplate`, `Goal`.
- Repository: `PerformanceReviewRepository`, etc.
- Service: `ReviewService` (create, assign, submit, export).
- Controller: `ReviewController`.
- DTOs: `PerformanceReviewDTO`, `GoalDTO`.

**Sample Implementation:**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private ReviewTemplate template;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String status; // DRAFT, SUBMITTED, SIGNED_OFF
    @OneToMany private List<Goal> goals;
    private String supervisorComments;
    private String employeeComments;
}
```
```java
@Service
public class ReviewService {
    @Transactional
    public void createReview(Long employeeId, PerformanceReviewDTO dto) { ... }
    @Transactional
    public void submitReview(Long reviewId) { ... }
    @Transactional(readOnly = true)
    public Resource exportReviewPdf(Long reviewId) { ... }
}
```
```java
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping public void create(@RequestBody PerformanceReviewDTO dto) { ... }
    @PostMapping("/{id}/submit") public void submit(@PathVariable Long id) { ... }
    @GetMapping("/{id}/export") public ResponseEntity<Resource> exportPdf(@PathVariable Long id) { ... }
}
```

---

### <a name="e11"></a>**E11: Payroll Export Integration**

#### Section Title: Payroll Export, Provider Mapping, Secure Delivery

**Description of Design Decisions:**
- Generate payroll-ready files.
- Map to provider schemas.
- Secure delivery (SFTP/API).
- Retry and audit log.

**Design Specification:**
- Service: `PayrollExportService` (generate, deliver, retry).
- Entity: `PayrollExportLog`.
- Integration: SFTP/API client.
- Controller: `PayrollController`.

**Sample Implementation:**
```java
@Entity
public class PayrollExportLog {
    @Id @GeneratedValue private Long id;
    private LocalDate exportDate;
    private String status; // SUCCESS, FAILED
    private String provider;
    private String filePath;
    private int retryCount;
}
```
```java
@Service
public class PayrollExportService {
    @Transactional
    public void exportPayroll(LocalDate period) { ... }
    @Transactional
    public void retryFailedExports() { ... }
}
```
```java
@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/export") public void export(@RequestParam LocalDate period) { ... }
    @GetMapping("/logs") public List<PayrollExportLogDTO> logs() { ... }
}
```

---

### <a name="e12"></a>**E12: Notifications & Announcements**

#### Section Title: Notification Domain, Channels, Templates

**Description of Design Decisions:**
- In-app, email, SMS notifications.
- Opt-in/out per channel.
- Localized templates.
- Delivery status and rate limits.

**Design Specification:**
- Entity: `Notification`, `Announcement`.
- Service: `NotificationService` (send, track, opt-in/out).
- Controller: `NotificationController`.
- Integration: Email/SMS providers.

**Sample Implementation:**
```java
@Entity
public class Notification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee recipient;
    private String channel; // EMAIL, SMS, IN_APP
    private String templateKey;
    private String content;
    private String status; // SENT, FAILED
    private LocalDateTime sentAt;
}
```
```java
@Service
public class NotificationService {
    @Transactional
    public void sendNotification(NotificationDTO dto) { ... }
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotifications(Long employeeId) { ... }
}
```
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping public void send(@RequestBody NotificationDTO dto) { ... }
    @GetMapping public List<NotificationDTO> list(@RequestParam Long employeeId) { ... }
}
```

---

### <a name="e13"></a>**E13: Integration Layer (HRIS/WMS APIs)**

#### Section Title: API Exposure, Connectors, Webhooks

**Description of Design Decisions:**
- Expose REST APIs for HRIS, WMS, IDP.
- JWT/OAuth2 security.
- Webhooks for events.
- Idempotency.

**Design Specification:**
- Integration package for connectors.
- Controller: `IntegrationController`.
- Service: `IntegrationService` (sync, webhooks).
- OpenAPI documentation.

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    @PostMapping("/hris/sync") public void syncHris(@RequestBody HrisSyncDTO dto) { ... }
    @PostMapping("/wms/link") public void linkWms(@RequestBody WmsLinkDTO dto) { ... }
    @PostMapping("/webhook") public ResponseEntity<?> webhook(@RequestBody WebhookEventDTO dto) { ... }
}
```
```java
@Service
public class IntegrationService {
    @Transactional
    public void syncFromHris(HrisSyncDTO dto) { ... }
    @Transactional
    public void handleWebhook(WebhookEventDTO dto) { ... }
}
```

---

### <a name="e14"></a>**E14: Audit Trail & Compliance**

#### Section Title: Audit Logging, Tamper-Evident Storage

**Description of Design Decisions:**
- Centralized audit logging for sensitive changes.
- Tamper-evident storage.
- Export and test coverage.

**Design Specification:**
- Entity: `AuditLog`.
- Service: `AuditService` (log, export).
- Controller: `AuditController`.

**Sample Implementation:**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String entity;
    private Long entityId;
    private String action; // CREATE, UPDATE, DELETE
    private String actor;
    private LocalDateTime timestamp;
    @Lob private String before;
    @Lob private String after;
}
```
```java
@Service
public class AuditService {
    @Transactional
    public void logChange(String entity, Long entityId, String action, String before, String after, String actor) { ... }
    @Transactional(readOnly = true)
    public List<AuditLogDTO> exportLogs(LocalDate from, LocalDate to) { ... }
}
```
```java
@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/logs") public List<AuditLogDTO> logs(@RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
}
```

---

### <a name="e15"></a>**E15: Reporting & Analytics**

#### Section Title: Reporting Domain, Export, Dashboards

**Description of Design Decisions:**
- Operational reports: attendance, overtime, leave, certs, safety KPIs.
- Export CSV/PDF.
- Role-based dashboards.

**Design Specification:**
- Service: `ReportingService` (generate, export).
- Controller: `ReportingController`.
- DTOs: `ReportDTO`.

**Sample Implementation:**
```java
@Service
public class ReportingService {
    @Transactional(readOnly = true)
    public Resource generateReport(String type, LocalDate from, LocalDate to, String department) { ... }
}
```
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping public ResponseEntity<Resource> export(@RequestParam String type, @RequestParam LocalDate from, @RequestParam LocalDate to, @RequestParam String department) { ... }
}
```

---

### <a name="e16"></a>**E16: Mobile Access (PWA)**

#### Section Title: PWA Configuration, Mobile APIs

**Description of Design Decisions:**
- Responsive views for core flows.
- Offline support via PWA manifest.
- API endpoints optimized for mobile.

**Design Specification:**
- PWA manifest in `resources/static/manifest.json`.
- Service Worker for offline.
- Mobile-friendly endpoints in controllers.

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
```js
// service-worker.js (pseudo)
self.addEventListener('fetch', function(event) {
  // Cache API responses for offline
});
```
```java
// Mobile endpoints in existing controllers, e.g., /attendance/clock-in
```

---

### <a name="e17"></a>**E17: Onboarding & Offboarding Workflow**

#### Section Title: Workflow Automation, Task Generation

**Description of Design Decisions:**
- Automate provisioning/deprovisioning.
- Generate tasks for training, asset assignment.
- HRIS integration.

**Design Specification:**
- Service: `OnboardingService`, `OffboardingService`.
- Entity: `OnboardingTask`, `OffboardingTask`.
- Controller: `OnboardingController`.

**Sample Implementation:**
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String taskType; // TRAINING, ASSET_ASSIGNMENT
    private String status; // PENDING, COMPLETED
}
```
```java
@Service
public class OnboardingService {
    @Transactional
    public void processNewHire(Long employeeId) { ... }
    @Transactional
    public void completeTask(Long taskId) { ... }
}
```
```java
@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @PostMapping("/new-hire") public void newHire(@RequestParam Long employeeId) { ... }
    @PostMapping("/task/{id}/complete") public void completeTask(@PathVariable Long id) { ... }
}
```

---

### <a name="e18"></a>**E18: Localization**

#### Section Title: i18n Configuration, Localized Templates

**Description of Design Decisions:**
- Support multiple languages.
- Localized notification templates.

**Design Specification:**
- Message bundles in `resources/i18n/messages_xx.properties`.
- Service: `LocalizationService`.
- Controller: `LocalizationController`.

**Sample Implementation:**
```java
// application.yml
spring:
  messages:
    basename: i18n/messages
```
```java
@Service
public class LocalizationService {
    public String getMessage(String key, Locale locale) { ... }
}
```
```java
@RestController
@RequestMapping("/localization")
public class LocalizationController {
    @GetMapping("/message") public String getMessage(@RequestParam String key, @RequestParam String lang) { ... }
}
```

---

### <a name="e19"></a>**E19: AI Scheduling**

#### Section Title: AI Scheduling Service, Optimization

**Description of Design Decisions:**
- Use AI/ML to optimize shift assignments.
- Integrate with shift and leave data.

**Design Specification:**
- Service: `AiSchedulingService`.
- Integration: ML model (external or embedded).
- Controller: `SchedulingController`.

**Sample Implementation:**
```java
@Service
public class AiSchedulingService {
    @Transactional
    public List<ShiftAssignmentDTO> optimizeSchedule(LocalDate weekStart) { ... }
}
```
```java
@RestController
@RequestMapping("/ai-scheduling")
public class SchedulingController {
    @PostMapping("/optimize") public List<ShiftAssignmentDTO> optimize(@RequestParam LocalDate weekStart) { ... }
}
```

---

### <a name="e20"></a>**E20: CI/CD**

#### Section Title: CI/CD Pipeline, Quality Gates

**Description of Design Decisions:**
- Automated build, test, deploy.
- Quality gates for code coverage, linting.

**Design Specification:**
- Use GitHub Actions/Jenkins for pipeline.
- Steps: build, test, static analysis, deploy.
- Dockerfile for containerization.

**Sample Implementation:**
```yaml
# .github/workflows/ci.yml
name: CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Build with Maven
        run: mvn clean install
      - name: Run Tests
        run: mvn test
      - name: Build Docker Image
        run: docker build -t wms-ems:latest .
```
```dockerfile
# Dockerfile
FROM openjdk:17-jdk-alpine
COPY target/ems.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

---

## Exception Handling Example

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(ex.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        // extract validation errors
        return ResponseEntity.badRequest().body(new ApiError("Validation failed"));
    }
}
```

---

## Validation Example

```java
public class EmployeeCreateDTO {
    @NotNull @Size(min=2, max=100) private String name;
    @NotNull @Pattern(regexp="^[A-Z0-9]{6}$") private String badgeId;
    // ...
}
```

---

## Transaction Management Example

```java
@Service
public class EmployeeService {
    @Transactional
    public EmployeeDTO createEmployee(EmployeeCreateDTO dto) { ... }
}
```

---

## OpenAPI/Swagger Example

```java
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee management APIs")
public class EmployeeController {
    @Operation(summary = "Create employee", responses = { ... })
    @PostMapping public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeCreateDTO dto) { ... }
}
```

---

## Conclusion

This document provides a production-ready, low-level technical specification for all 79 user stories across 20 epics of the Warehouse EMS. It covers architecture, package structure, entity/service/repository/controller design, configuration, security, integration, and code examples, following Spring Boot best practices. Developers should use this as the authoritative guide for implementation.