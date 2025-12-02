# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

---

Section: E01 - Project Scaffolding & Domain Setup  
Description: Establishes the foundational Spring Boot architecture, base package structure, database migration setup, and core modules for the Warehouse EMS.  
Design Specification:
- Spring Boot Maven project with parent POM, Java 17+, and modular structure.
- Base packages: `com.wms.ems` with sub-packages for `employee`, `schedule`, `attendance`, `safety`, etc.
- Flyway/Liquibase for DB migrations; baseline migration scripts in `src/main/resources/db/migration`.
- Spring Boot Actuator enabled for health and metrics endpoints.
- README with build/run instructions.
Sample Implementation:
```java
// pom.xml (snippet)
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>

// application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ems
spring.datasource.username=ems_user
spring.datasource.password=secret
management.endpoints.web.exposure.include=health,info,metrics

// Directory structure
com/wms/ems/
    employee/
    schedule/
    attendance/
    safety/

// Flyway baseline migration
src/main/resources/db/migration/V1__baseline.sql
```

---

Section: E02 - Employee Master Data CRUD  
Description: Implements the Employee domain, CRUD REST APIs, DTOs, soft-delete, pagination, and filtering.  
Design Specification:
- Entity: `Employee` with fields: id, name, badgeId (unique), role, department, shiftGroup, hireDate, status, deleted (soft-delete).
- Repository: `EmployeeRepository` extends `JpaRepository<Employee, Long>` with custom queries for filtering/pagination.
- Service: `EmployeeService` for business logic, validation, and DTO mapping.
- Controller: `EmployeeController` exposes `/employees` endpoints (CRUD, pagination, filtering).
- OpenAPI/Swagger documentation.
Sample Implementation:
```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id @GeneratedValue private Long id;
    @Column(unique = true) private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted;
}

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping public EmployeeDto create(@RequestBody EmployeeDto dto) {...}
    @GetMapping public Page<EmployeeDto> list(Pageable pageable, @RequestParam Map<String, String> filters) {...}
    @PatchMapping("/{id}") public EmployeeDto update(@PathVariable Long id, @RequestBody EmployeeDto dto) {...}
    @DeleteMapping("/{id}") public void softDelete(@PathVariable Long id) {...}
}
```

---

Section: E03 - Role Based Access Control (RBAC)  
Description: Integrates Spring Security with role-based access, endpoint/method security, row-level constraints, and API key/OAuth2 toggle.  
Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER.
- Security config: `SecurityConfig` with `@EnableWebSecurity`, JWT/OAuth2 support, API key toggle via properties.
- Method security: `@PreAuthorize` annotations on service/controller methods.
- Row-level constraints: filter queries by user role/team.
- Unauthorized/forbidden handling.
Sample Implementation:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR")
            .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
            .anyRequest().authenticated()
            .and().oauth2ResourceServer().jwt();
    }
}

@Service
public class EmployeeService {
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public EmployeeDto createEmployee(EmployeeDto dto) {...}
}
```

---

Section: E04 - Time & Attendance (Clock In/Out)  
Description: Provides endpoints for clock-in/out events, geofence/device capture, hours calculation, and missed punch workflow.  
Design Specification:
- Entity: `AttendanceEvent` with employeeId, timestamp, type (IN/OUT), deviceId, location, shiftId.
- Service: `AttendanceService` for event validation, shift association, hours calculation, missed punch handling.
- Controller: `/attendance/clock-in`, `/attendance/clock-out`, corrections workflow.
- Geofence validation via external service.
Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private LocalDateTime timestamp;
    private String type; // IN or OUT
    private String deviceId;
    private String location;
    private Long shiftId;
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in") public ResponseEntity<?> clockIn(@RequestBody ClockEventDto dto) {...}
    @PostMapping("/clock-out") public ResponseEntity<?> clockOut(@RequestBody ClockEventDto dto) {...}
    @PostMapping("/corrections") public ResponseEntity<?> requestCorrection(@RequestBody CorrectionDto dto) {...}
}
```

---

Section: E05 - Shift & Schedule Management  
Description: Manages recurring shift templates, rotations, overtime rules, employee assignments, blackout dates, and warehouse calendars.  
Design Specification:
- Entities: `ShiftTemplate`, `ShiftAssignment`, `WarehouseCalendar`.
- Service: `ShiftService` for template CRUD, conflict detection, bulk assignment.
- Controller: `/shifts`, `/schedules`, `/calendar` endpoints.
- Audit entries for changes.
Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
}

@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping public ShiftTemplateDto create(@RequestBody ShiftTemplateDto dto) {...}
    @GetMapping public List<ShiftTemplateDto> list() {...}
}
```

---

Section: E06 - Leave & Absence Management  
Description: Handles PTO/sick/unpaid leave requests, approvals, accrual balances, and scheduling integration.  
Design Specification:
- Entity: `LeaveRequest` with employeeId, type, startDate, endDate, status, accrualBalance.
- Service: `LeaveService` for request/approval, balance updates, scheduling hooks.
- Controller: `/leave/requests`, `/leave/balances` endpoints.
Sample Implementation:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type; // PTO, Sick, Unpaid
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // Requested, Approved, Denied
    private int accrualBalance;
}

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/requests") public LeaveRequestDto requestLeave(@RequestBody LeaveRequestDto dto) {...}
    @PostMapping("/approve/{id}") public void approve(@PathVariable Long id) {...}
}
```

---

Section: E07 - Training & Certification Tracking  
Description: Tracks employee certifications, expirations, renewals, blocks assignments for expired certs, and supports proof document uploads.  
Design Specification:
- Entity: `Certification` with employeeId, type, issueDate, expiryDate, proofDocumentUrl.
- Service: `CertificationService` for CRUD, expiry alerts, assignment checks.
- Controller: `/certifications` endpoints.
- Integration with scheduling for assignment blocking.
Sample Implementation:
```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String proofDocumentUrl;
}

@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping public CertificationDto add(@RequestBody CertificationDto dto) {...}
    @GetMapping("/alerts") public List<CertificationAlertDto> getAlerts() {...}
}
```

---

Section: E08 - Safety Incidents & OSHA Reporting  
Description: Records safety incidents/near-misses, severity, location, investigation workflow, and OSHA summary generation.  
Design Specification:
- Entity: `SafetyIncident` with involvedEmployees, severity, location, description, status.
- Service: `SafetyService` for incident workflow, OSHA report generation.
- Controller: `/safety/incidents`, `/safety/reports` endpoints.
Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private List<Long> involvedEmployeeIds;
    private String severity;
    private String location;
    private String description;
    private String status; // Open, Investigating, Resolved
}

@RestController
@RequestMapping("/safety")
public class SafetyController {
    @PostMapping("/incidents") public SafetyIncidentDto report(@RequestBody SafetyIncidentDto dto) {...}
    @GetMapping("/reports/osha") public OSHAReportDto getOSHAReport() {...}
}
```

---

Section: E09 - Equipment & Asset Assignment  
Description: Assigns equipment/assets to employees, tracks checkout/return, validates certifications, and maintains asset condition.  
Design Specification:
- Entity: `Asset` with assetId, type, condition, assignedEmployeeId, checkoutHistory.
- Service: `AssetService` for assignment, certification validation, history logging.
- Controller: `/assets`, `/assets/checkout`, `/assets/return` endpoints.
Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String assetId;
    private String type;
    private String condition;
    private Long assignedEmployeeId;
    @ElementCollection private List<CheckoutEvent> checkoutHistory;
}

@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/checkout") public void checkout(@RequestBody AssetCheckoutDto dto) {...}
    @PostMapping("/return") public void returnAsset(@RequestBody AssetReturnDto dto) {...}
}
```

---

Section: E10 - Performance Reviews & Goals  
Description: Manages review templates, goals, competencies, ratings, comments, and supervisor/employee acknowledgements.  
Design Specification:
- Entity: `PerformanceReview` with employeeId, period, goals, competencies, ratings, comments, status.
- Service: `ReviewService` for cycle creation, workflow, PDF export.
- Controller: `/reviews`, `/reviews/export` endpoints.
Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String period;
    @ElementCollection private List<Goal> goals;
    @ElementCollection private List<Competency> competencies;
    private String ratings;
    private String comments;
    private String status; // Draft, Submitted, Acknowledged
}

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping public PerformanceReviewDto create(@RequestBody PerformanceReviewDto dto) {...}
    @GetMapping("/export/{id}") public ResponseEntity<Resource> exportPdf(@PathVariable Long id) {...}
}
```

---

Section: E11 - Payroll Export Integration  
Description: Generates payroll files from attendance/leave, maps to provider formats, and delivers securely via SFTP/API.  
Design Specification:
- Service: `PayrollExportService` for file generation, provider mapping, delivery, retry logic.
- Controller: `/payroll/export` endpoint.
- Integration: SFTP/API client configuration.
Sample Implementation:
```java
@Service
public class PayrollExportService {
    public PayrollFile generatePayrollFile(LocalDate period) {...}
    public void deliverPayrollFile(PayrollFile file) {...}
}

@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/export") public ResponseEntity<?> exportPayroll(@RequestBody PayrollExportRequestDto dto) {...}
}
```

---

Section: E12 - Notifications & Announcements  
Description: Sends in-app, email/SMS notifications for shift changes, cert expirations, approvals, announcements, with quiet hours config.  
Design Specification:
- Entity: `Notification` with userId, channel, message, status, timestamp.
- Service: `NotificationService` for delivery, opt-in/out, rate limiting.
- Controller: `/notifications`, `/announcements` endpoints.
- Integration: Email/SMS provider clients.
Sample Implementation:
```java
@Entity
public class Notification {
    @Id @GeneratedValue private Long id;
    private Long userId;
    private String channel; // IN_APP, EMAIL, SMS
    private String message;
    private String status; // Sent, Failed
    private LocalDateTime timestamp;
}

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping public void send(@RequestBody NotificationDto dto) {...}
}
```

---

Section: E13 - Integration Layer (HRIS/WMS APIs)  
Description: Exposes REST APIs and connectors for HRIS, WMS, SSO, and webhooks for events.  
Design Specification:
- Service: `IntegrationService` for HRIS/WMS sync, SSO, webhook handling.
- Controller: `/api/hris`, `/api/wms`, `/api/webhooks` endpoints.
- Security: JWT/OAuth2 for external APIs.
- OpenAPI documentation.
Sample Implementation:
```java
@RestController
@RequestMapping("/api/hris")
public class HRISController {
    @PostMapping("/sync") public void syncEmployees(@RequestBody HRISSyncDto dto) {...}
}

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {
    @PostMapping public void handleEvent(@RequestBody WebhookEventDto dto) {...}
}
```

---

Section: E14 - Audit Trail & Compliance  
Description: Centralized audit logging for sensitive changes, tamper-evident storage, and export capabilities.  
Design Specification:
- Entity: `AuditLog` with actor, timestamp, entity, before/after, operation.
- Service: `AuditService` for logging, export, validation.
- Controller: `/audit/logs`, `/audit/export` endpoints.
- Immutable log table (append-only).
Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    private String operation;
    @Lob private String before;
    @Lob private String after;
}

@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/logs") public List<AuditLogDto> getLogs(@RequestParam Map<String, String> filters) {...}
    @GetMapping("/export") public ResponseEntity<Resource> exportLogs(@RequestParam Map<String, String> filters) {...}
}
```

---

Section: E15 - Reporting & Analytics  
Description: Provides operational reports for attendance, overtime, leave, certifications, safety KPIs, with CSV/PDF export and dashboards.  
Design Specification:
- Service: `ReportingService` for report generation, filtering, export.
- Controller: `/reports`, `/reports/export` endpoints.
- Role-based access to reports.
Sample Implementation:
```java
@RestController
@RequestMapping("/reports")
public class ReportsController {
    @GetMapping public List<ReportDto> getReports(@RequestParam Map<String, String> filters) {...}
    @GetMapping("/export") public ResponseEntity<Resource> exportReport(@RequestParam Map<String, String> filters) {...}
}
```

---

Section: E16 - Mobile Access (PWA)  
Description: Delivers responsive views for clock-in/out, schedules, leave requests, announcements, and offline-friendly PWA features.  
Design Specification:
- Frontend: PWA manifest, service worker, responsive UI (React/Vue/Angular recommended).
- Backend: REST endpoints for mobile flows (`/attendance`, `/schedules`, `/leave`, `/announcements`).
- Offline queue for clock events, conflict resolution logic.
- Lighthouse PWA score validation.
Sample Implementation:
```javascript
// manifest.json (snippet)
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "icons": [{ "src": "/icon-192.png", "sizes": "192x192", "type": "image/png" }]
}

// Service worker (offline queue example)
self.addEventListener('fetch', function(event) {
  // Cache-first strategy for clock events
});
```
```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @GetMapping("/schedules") public List<ScheduleDto> getSchedules(@RequestParam Long employeeId) {...}
    @PostMapping("/clock-in") public ResponseEntity<?> mobileClockIn(@RequestBody ClockEventDto dto) {...}
}
```

---

Section: E17 - Onboarding & Offboarding Workflow  
Description: Automates provisioning of accounts, initial schedules, required training; deprovision access and assets on termination.  
Design Specification:
- Service: `OnboardingService` for new hire provisioning, training/asset assignment, offboarding logic.
- Controller: `/onboarding`, `/offboarding` endpoints.
- Integration: HRIS sync, asset management, schedule updates.
Sample Implementation:
```java
@Service
public class OnboardingService {
    public void provisionNewHire(NewHireDto dto) {...}
    public void deprovisionEmployee(Long employeeId) {...}
}

@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @PostMapping public void onboard(@RequestBody NewHireDto dto) {...}
}

@RestController
@RequestMapping("/offboarding")
public class OffboardingController {
    @PostMapping public void offboard(@RequestBody OffboardDto dto) {...}
}
```

---

**Note:**  
- All entities should use JPA annotations and be placed in their respective domain packages.
- DTOs should be used for API payloads, mapped via MapStruct or manual mapping.
- Services encapsulate business logic and validation.
- Controllers expose REST endpoints with OpenAPI documentation.
- Security is enforced via Spring Security with role-based access.
- Audit logging is centralized and immutable.
- Integration points use REST APIs, SFTP, or webhooks.
- All code follows Spring Boot best practices, including dependency injection, exception handling, and testing.

---

**End of Document**