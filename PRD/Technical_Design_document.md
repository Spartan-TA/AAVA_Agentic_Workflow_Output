Section: Project Scaffolding & Domain Setup  
Description: Establishes the foundational Spring Boot project structure, including core modules, database migration, and health checks. Ensures a standardized baseline for all subsequent development.  
Design Specification:  
- Spring Boot 3.x (or latest LTS) with Maven/Gradle  
- Base package: com.companyname.warehouse  
- Modules: core, employee, attendance, shift, leave, training, safety, asset, integration, audit, reporting, notification, mobile  
- DB migration: Flyway (preferred) or Liquibase, baseline migration script  
- Actuator enabled on /actuator/health  
- README with build/run instructions  
Sample Implementation:  
```java
@SpringBootApplication
public class WarehouseApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseApplication.class, args);
    }
}
// application.yml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse
    username: warehouse
    password: secret
  flyway:
    enabled: true
    locations: classpath:db/migration
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

---

Section: Employee Master Data (CRUD)  
Description: Centralized management of employee records with full CRUD support, unique badgeId enforcement, soft-delete, and OpenAPI documentation.  
Design Specification:  
- Entity: Employee (id, badgeId, firstName, lastName, email, phone, status, deleted, createdAt, updatedAt)  
- Repository: JpaRepository<Employee, Long>, custom query for badgeId uniqueness  
- Service: EmployeeService (CRUD, soft-delete, pagination/filtering)  
- Controller: EmployeeController (REST endpoints, OpenAPI annotations)  
- Soft-delete: Boolean flag, filter in queries  
- OpenAPI: springdoc-openapi integration  
Sample Implementation:  
```java
@Entity
public class Employee {
    @Id @GeneratedValue private Long id;
    @Column(unique = true, nullable = false) private String badgeId;
    private String firstName, lastName, email, phone;
    private String status;
    private boolean deleted = false;
    private LocalDateTime createdAt, updatedAt;
}
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
}
@Service
public class EmployeeService {
    // CRUD methods, soft-delete by setting deleted=true
}
@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee")
public class EmployeeController {
    // CRUD endpoints with @Operation annotations
}
```

---

Section: Role-Based Access Control (RBAC)  
Description: Secures endpoints by user roles (ADMIN, SUPERVISOR, WORKER), with API key/OAuth2 toggle and test coverage for security rules.  
Design Specification:  
- SecurityConfig: Spring Security, method-level @PreAuthorize  
- Roles: ADMIN (all), SUPERVISOR (team), WORKER (self)  
- API key/OAuth2 toggle via application.yml  
- Exception handling for 401/403  
- Security tests with @WithMockUser  
Sample Implementation:  
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
          .authorizeRequests()
            .antMatchers("/api/employees/**").hasAnyRole("ADMIN", "SUPERVISOR")
            .antMatchers("/api/**").authenticated()
            .and()
          .oauth2ResourceServer().jwt();
    }
    // API key filter bean (if enabled)
}
@PreAuthorize("hasRole('ADMIN')")
public void adminOnlyMethod() { ... }
```

---

Section: Time & Attendance (Clock In/Out)  
Description: Tracks employee clock in/out events with device/geofence capture, shift association, correction workflow, and CSV export.  
Design Specification:  
- Entity: Attendance (id, employee, clockIn, clockOut, deviceId, geofence, shift, status, correctionRequested, createdAt)  
- Service: AttendanceService (clockIn, clockOut, requestCorrection, computeTotals)  
- Controller: AttendanceController (endpoints for clock, corrections, CSV export)  
- Shift association: auto-link by time  
- Correction: creates approval task  
- Reports: CSV export endpoint  
Sample Implementation:  
```java
@Entity
public class Attendance {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private LocalDateTime clockIn, clockOut;
    private String deviceId, geofence;
    @ManyToOne private Shift shift;
    private String status; // e.g., APPROVED, PENDING
    private boolean correctionRequested;
}
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    @PostMapping("/clock-in") public ResponseEntity<?> clockIn(...) { ... }
    @PostMapping("/clock-out") public ResponseEntity<?> clockOut(...) { ... }
    @PostMapping("/correction") public ResponseEntity<?> requestCorrection(...) { ... }
    @GetMapping("/export") public void exportCsv(...) { ... }
}
```

---

Section: Shift & Schedule Management  
Description: Manages shift templates, rotations, assignments, conflict detection, and audit logging for all shift operations.  
Design Specification:  
- Entity: Shift (id, name, start, end, template, blackoutDates, assignedEmployees, createdBy, auditTrail)  
- Service: ShiftService (create, update, assign, detectConflicts, bulkAssign)  
- Controller: ShiftController (CRUD, assignment endpoints)  
- Conflict detection: time overlap, blackout dates  
- Audit: log all changes  
Sample Implementation:  
```java
@Entity
public class Shift {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalDateTime start, end;
    private boolean template;
    @ElementCollection private List<LocalDate> blackoutDates;
    @ManyToMany private List<Employee> assignedEmployees;
    @ManyToOne private User createdBy;
}
@Service
public class ShiftService {
    public boolean detectConflicts(...) { ... }
    public void bulkAssign(List<Long> employeeIds, Long shiftId) { ... }
}
```

---

Section: Leave & Absence Management  
Description: Handles leave requests, approvals, accrual balances, shift coverage, and export of approved leaves.  
Design Specification:  
- Entity: LeaveRequest (id, employee, type, start, end, status, approvedBy, createdAt)  
- Service: LeaveService (request, approve, deny, updateBalances)  
- Controller: LeaveController (request, approve/deny, export)  
- Integration: hooks for scheduling/payroll  
Sample Implementation:  
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String type; // e.g., SICK, VACATION
    private LocalDate start, end;
    private String status; // PENDING, APPROVED, DENIED
    @ManyToOne private User approvedBy;
}
@Service
public class LeaveService {
    public void updateBalances(Employee e, String type, int days) { ... }
}
```

---

Section: Training & Certification Tracking  
Description: Tracks employee certifications, expiration, proof uploads, and blocks unqualified assignments. Sends alerts before expiry.  
Design Specification:  
- Entity: Certification (id, employee, type, issueDate, expiryDate, proofUrl, status)  
- Service: CertificationService (add, expire, alert, checkAssignment)  
- Controller: CertificationController (CRUD, upload proof, alerts)  
- Alerts: scheduled job for 30/7 day notifications  
Sample Implementation:  
```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String type;
    private LocalDate issueDate, expiryDate;
    private String proofUrl;
    private String status; // VALID, EXPIRED
}
@Service
public class CertificationService {
    @Scheduled(cron = "0 0 * * * ?")
    public void sendExpiryAlerts() { ... }
}
```

---

Section: Safety Incidents & OSHA Reporting  
Description: Records safety incidents, manages investigation workflow, generates OSHA-compliant reports, and exposes metrics dashboards.  
Design Specification:  
- Entity: SafetyIncident (id, employee, date, type, description, status, oshaFields, investigation, correctiveActions, createdAt)  
- Service: SafetyService (recordIncident, updateStatus, exportOsha, metrics)  
- Controller: SafetyController (CRUD, export, dashboard endpoints)  
- Workflow: status transitions, investigation steps  
Sample Implementation:  
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private LocalDate date;
    private String type, description, status;
    @Lob private String oshaFields;
    @Lob private String investigation, correctiveActions;
}
@RestController
@RequestMapping("/api/safety")
public class SafetyController {
    @PostMapping public ResponseEntity<?> recordIncident(...) { ... }
    @GetMapping("/metrics") public ResponseEntity<?> getMetrics() { ... }
}
```

---

Section: Equipment & Asset Assignment  
Description: Assigns and tracks equipment/assets to employees, blocks use if certifications are invalid, logs history, and tracks condition state.  
Design Specification:  
- Entity: Asset (id, type, serial, assignedTo, assignedAt, condition, history, dueDate)  
- Service: AssetService (assign, checkIn, checkOut, validateCerts, logHistory)  
- Controller: AssetController (CRUD, assignment, history, overdue reports)  
Sample Implementation:  
```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String type, serial, condition;
    @ManyToOne private Employee assignedTo;
    private LocalDateTime assignedAt, dueDate;
    @ElementCollection private List<String> history;
}
@Service
public class AssetService {
    public boolean validateCerts(Employee e, String assetType) { ... }
}
```

---

Section: Performance Reviews & Goals  
Description: Manages performance review cycles, supports submit/acknowledge workflow, PDF export, and immutable history after sign-off.  
Design Specification:  
- Entity: PerformanceReview (id, employee, reviewer, period, goals, feedback, status, signedOffAt, pdfUrl)  
- Service: ReviewService (create, submit, acknowledge, exportPdf)  
- Controller: ReviewController (CRUD, export, history)  
Sample Implementation:  
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private User reviewer;
    private String period, goals, feedback, status;
    private LocalDateTime signedOffAt;
    private String pdfUrl;
}
@Service
public class ReviewService {
    public void exportPdf(Long reviewId) { ... }
}
```

---

Section: Payroll Export Integration  
Description: Generates payroll-ready files from attendance and leave, matches provider schema, retries failed deliveries, and logs all exports.  
Design Specification:  
- Service: PayrollExportService (generate, deliver, retry, audit)  
- Controller: PayrollExportController (export endpoint)  
- Integration: SFTP/API delivery, audit log  
Sample Implementation:  
```java
@Service
public class PayrollExportService {
    public File generateExport(LocalDate from, LocalDate to) { ... }
    public void deliver(File exportFile) { ... }
    public void retryFailedDeliveries() { ... }
}
@RestController
@RequestMapping("/api/payroll")
public class PayrollExportController {
    @GetMapping("/export") public ResponseEntity<?> exportPayroll(...) { ... }
}
```

---

Section: Notifications & Announcements  
Description: Sends notifications/announcements, supports opt-in/out, localization, delivery tracking, rate limits, and dashboard display.  
Design Specification:  
- Entity: Notification (id, type, message, recipient, status, sentAt, template, locale)  
- Service: NotificationService (send, optInOut, trackDelivery, rateLimit)  
- Controller: NotificationController (send, opt-in/out, dashboard)  
- Quiet hours config in application.yml  
Sample Implementation:  
```java
@Entity
public class Notification {
    @Id @GeneratedValue private Long id;
    private String type, message, locale;
    @ManyToOne private Employee recipient;
    private String status;
    private LocalDateTime sentAt;
}
@Service
public class NotificationService {
    public void send(Notification n) { ... }
    public void trackDelivery(Long id, String status) { ... }
}
```

---

Section: Integration Layer (HRIS/WMS APIs)  
Description: Exposes APIs/connectors for HRIS, WMS, SSO; supports JWT/OAuth2, idempotent webhooks, and API documentation.  
Design Specification:  
- Controller: IntegrationController (HRIS sync, WMS link, SSO endpoints, webhooks)  
- Security: JWT/OAuth2  
- Service: IntegrationService (syncEmployees, handleWebhooks)  
- OpenAPI docs  
Sample Implementation:  
```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    @PostMapping("/hris/sync") public ResponseEntity<?> syncHris(...) { ... }
    @PostMapping("/wms/link") public ResponseEntity<?> linkWms(...) { ... }
    @PostMapping("/webhook") public ResponseEntity<?> handleWebhook(...) { ... }
}
@Service
public class IntegrationService {
    public void syncEmployees(List<EmployeeDto> employees) { ... }
}
```

---

Section: Audit Trail & Compliance  
Description: Centralized audit logging for sensitive changes, immutable log table, export by date/user/entity, and tamper-evident storage.  
Design Specification:  
- Entity: AuditLog (id, actor, timestamp, entity, entityId, before, after, action)  
- Service: AuditService (logChange, export)  
- Controller: AuditController (export, query)  
- Tamper-evident: append-only, hash chain  
Sample Implementation:  
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String actor, entity, action;
    private Long entityId;
    private LocalDateTime timestamp;
    @Lob private String before, after;
    private String hash, prevHash;
}
@Service
public class AuditService {
    public void logChange(String actor, String entity, Long id, Object before, Object after, String action) { ... }
}
```

---

Section: Reporting & Analytics  
Description: Provides operational reports, dashboards, filters, CSV/PDF export, and access control for large data sets.  
Design Specification:  
- Service: ReportingService (generateReport, exportCsv, exportPdf, metrics)  
- Controller: ReportingController (report endpoints, export)  
- Filters: date, department, shift  
- Performance: export â¤10s for 50k rows  
Sample Implementation:  
```java
@Service
public class ReportingService {
    public List<ReportRow> generateReport(ReportCriteria criteria) { ... }
    public File exportCsv(List<ReportRow> rows) { ... }
}
@RestController
@RequestMapping("/api/reports")
public class ReportingController {
    @GetMapping public ResponseEntity<?> getReport(...) { ... }
    @GetMapping("/export") public void exportCsv(...) { ... }
}
```

---

Section: Mobile Access (PWA)  
Description: Enables responsive mobile access, offline queue for clock events, conflict resolution, and PWA compliance.  
Design Specification:  
- Controller: MobileController (PWA endpoints, manifest, offline queue API)  
- Service: MobileService (queueClockEvent, syncOffline, resolveConflicts)  
- PWA manifest.json, service worker  
Sample Implementation:  
```java
@RestController
@RequestMapping("/api/mobile")
public class MobileController {
    @PostMapping("/clock-event") public ResponseEntity<?> queueClockEvent(...) { ... }
    @GetMapping("/manifest.json") public ResponseEntity<?> getManifest() { ... }
}
@Service
public class MobileService {
    public void queueClockEvent(ClockEventDto event) { ... }
    public void syncOfflineEvents(List<ClockEventDto> events) { ... }
}
```

---

Section: Onboarding & Offboarding Workflow  
Description: Automates onboarding/offboarding, generates tasks for training/asset assignment, revokes access, and updates schedules.  
Design Specification:  
- Service: OnboardingService (provision, deprovision, generateTasks)  
- Controller: OnboardingController (onboard, offboard endpoints)  
- Integration: hooks to training, asset, schedule, integration modules  
Sample Implementation:  
```java
@Service
public class OnboardingService {
    public void provision(Employee e) { ... }
    public void deprovision(Employee e) { ... }
    public void generateTasks(Employee e, String type) { ... }
}
@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {
    @PostMapping("/onboard") public ResponseEntity<?> onboard(...) { ... }
    @PostMapping("/offboard") public ResponseEntity<?> offboard(...) { ... }
}
```

---
