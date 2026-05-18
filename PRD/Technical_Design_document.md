Section: Project Scaffolding & Domain Setup
Description: Establishes the foundational Spring Boot architecture, modular structure, and DB migration tooling for warehouse employee management.
Design Specification:
- Spring Boot (Maven) project with base package: com.company.warehouse
- Modules: employee, scheduling, attendance, safety (as sub-packages)
- DB migration: Flyway or Liquibase configured in src/main/resources/db/migration
- Actuator enabled for health checks
- Application starts on port 8080
- README with build/run instructions
Sample Implementation:
```java
@SpringBootApplication
public class WarehouseApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseApplication.class, args);
    }
}
// application.properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost/warehouse
spring.flyway.enabled=true
management.endpoints.web.exposure.include=health
```

Section: Employee Master Data (CRUD)
Description: Provides a single source of truth for employee records with robust CRUD APIs, enforcing unique badgeId and supporting soft-delete.
Design Specification:
- Entity: Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)
- Repository: JpaRepository<Employee, Long>
- Service: EmployeeService with CRUD, soft-delete, pagination/filtering
- Controller: EmployeeController with REST endpoints
- OpenAPI schema generation
Sample Implementation:
```java
@Entity
public class Employee {
    @Id @GeneratedValue private Long id;
    @Column(unique=true) private String badgeId;
    private String name, role, department, shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted;
}
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping public Employee create(@RequestBody Employee e) { ... }
    @GetMapping public Page<Employee> list(...) { ... }
    @DeleteMapping("/{id}") public void softDelete(@PathVariable Long id) { ... }
}
```

Section: Role-Based Access Control (RBAC)
Description: Secures sensitive operations using Spring Security, with configurable roles and endpoint/method-level restrictions.
Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- SecurityConfig: method/endpoint security, API key/OAuth2 toggle
- Row-level constraints for SUPERVISOR
- Test coverage for security rules
Sample Implementation:
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .antMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
            .and().oauth2Login();
    }
}
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.team == authentication.principal.team)")
public void updateEmployee(Employee employee) { ... }
```

Section: Time & Attendance (Clock In/Out)
Description: Enables accurate time tracking with device/geofence capture, shift association, and correction workflows.
Design Specification:
- Entity: AttendanceEvent (id, employeeId, timestamp, type, deviceId, geofence, shiftId, correctionStatus)
- Endpoints: /attendance/clock-in, /attendance/clock-out
- Service: AttendanceService (validation, shift association, daily totals, correction workflow)
- Export: CSV report generation
Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private LocalDateTime timestamp;
    private String type; // CLOCK_IN, CLOCK_OUT
    private String deviceId, geofence;
    private Long shiftId;
    private String correctionStatus;
}
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in") public ResponseEntity<?> clockIn(...) { ... }
    @PostMapping("/clock-out") public ResponseEntity<?> clockOut(...) { ... }
}
```

Section: Shift & Schedule Management
Description: Manages shift templates, schedules, assignments, and conflict detection for optimal staffing.
Design Specification:
- Entities: ShiftTemplate, ShiftSchedule, EmployeeShiftAssignment
- Service: ShiftService (CRUD, conflict detection, bulk assignment, audit logging)
- Controller: ShiftController (REST endpoints)
- Audit: AuditEntry entity for schedule changes
Sample Implementation:
```java
@Entity
public class ShiftTemplate { ... }
@Entity
public class ShiftSchedule { ... }
@Entity
public class EmployeeShiftAssignment { ... }
@Service
public class ShiftService {
    public void assignShifts(List<Long> employeeIds, ShiftSchedule schedule) { ... }
    public boolean detectConflicts(...) { ... }
}
```

Section: Leave & Absence Management
Description: Handles leave requests, approvals, accrual balances, and integration with scheduling/payroll.
Design Specification:
- Entity: LeaveRequest (id, employeeId, type, startDate, endDate, status, accrualBalance)
- Service: LeaveService (request, approve/deny, balance update, scheduling integration)
- Controller: LeaveController (REST endpoints)
- Export: Approved leaves report
Sample Implementation:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type; // PTO, Sick, Unpaid
    private LocalDate startDate, endDate;
    private String status;
    private int accrualBalance;
}
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping public LeaveRequest requestLeave(...) { ... }
    @PutMapping("/{id}/approve") public void approveLeave(...) { ... }
}
```

Section: Training & Certification Tracking
Description: Tracks employee certifications, expirations, and blocks unqualified assignments.
Design Specification:
- Entity: Certification (id, employeeId, type, issueDate, expiryDate, proofDocument)
- Service: CertificationService (CRUD, expiry alerts, assignment checks)
- Controller: CertificationController (REST endpoints)
- Alerts: Scheduled tasks for expiry notifications
Sample Implementation:
```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type;
    private LocalDate issueDate, expiryDate;
    private String proofDocument;
}
@Service
public class CertificationService {
    public void sendExpiryAlerts() { ... }
}
```

Section: Safety Incidents & OSHA Reporting
Description: Records safety incidents, manages investigation workflow, and generates OSHA reports.
Design Specification:
- Entity: SafetyIncident (id, severity, location, description, involvedEmployees, status)
- Service: SafetyIncidentService (validation, workflow, export)
- Controller: SafetyIncidentController (REST endpoints)
- Dashboard: Metrics endpoint for safety KPIs
Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private String severity, location, description;
    @ElementCollection private List<Long> involvedEmployees;
    private String status; // Open, Investigating, Resolved
}
@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
    @PostMapping public SafetyIncident reportIncident(...) { ... }
}
```

Section: Equipment & Asset Assignment
Description: Assigns and tracks assets, enforces certification checks, and logs asset history.
Design Specification:
- Entity: Asset (id, type, condition, assignedEmployeeId, checkoutDate, returnDate)
- Service: AssetService (CRUD, check-in/out, certification validation, overdue reports)
- Controller: AssetController (REST endpoints)
- History: AssetHistory entity for tracking
Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String type, condition;
    private Long assignedEmployeeId;
    private LocalDate checkoutDate, returnDate;
}
@Service
public class AssetService {
    public void assignAsset(Long assetId, Long employeeId) { ... }
}
```

Section: Performance Reviews & Goals
Description: Facilitates structured performance reviews, goal tracking, and immutable history.
Design Specification:
- Entity: PerformanceReview (id, employeeId, templateId, competencies, ratings, comments, status)
- Service: PerformanceReviewService (review cycle, PDF export, visibility, history)
- Controller: PerformanceReviewController (REST endpoints)
Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    private Long employeeId, templateId;
    private String competencies, ratings, comments;
    private String status; // Submitted, Acknowledged, SignedOff
}
@RestController
@RequestMapping("/reviews")
public class PerformanceReviewController {
    @PostMapping public PerformanceReview submitReview(...) { ... }
}
```

Section: Payroll Export Integration
Description: Integrates payroll export with secure delivery and audit logging.
Design Specification:
- Service: PayrollExportService (generate, reconcile, retry, audit)
- Controller: PayrollExportController (REST endpoints)
- Integration: SFTP/API delivery, provider schema mapping
- Audit: PayrollExportAudit entity
Sample Implementation:
```java
@Service
public class PayrollExportService {
    public File generatePayrollExport(...) { ... }
    public void deliverExport(File export) { ... }
}
@Entity
public class PayrollExportAudit { ... }
```

Section: Notifications & Announcements
Description: Delivers real-time notifications via multiple channels, with opt-in/out and localization.
Design Specification:
- Entity: Notification (id, employeeId, channel, templateId, status, deliveryTimestamp)
- Service: NotificationService (send, track, rate limit, localization)
- Controller: NotificationController (REST endpoints)
- Dashboard: Announcement visibility
Sample Implementation:
```java
@Entity
public class Notification {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String channel, templateId, status;
    private LocalDateTime deliveryTimestamp;
}
@Service
public class NotificationService {
    public void sendNotification(Notification n) { ... }
}
```

Section: Integration Layer (HRIS/WMS APIs)
Description: Exposes secured APIs and connectors for HRIS, WMS, and IDP integration.
Design Specification:
- REST endpoints: /api/hris, /api/wms, /api/idp
- Security: JWT/OAuth2, SSO
- Service: IntegrationService (sync jobs, webhooks, idempotency)
- OpenAPI documentation
Sample Implementation:
```java
@RestController
@RequestMapping("/api/hris")
public class HRISController {
    @PostMapping("/sync") public void syncEmployees(...) { ... }
}
@Configuration
public class ApiSecurityConfig { ... }
```

Section: Audit Trail & Compliance
Description: Centralizes audit logging for sensitive changes, ensuring tamper-evident storage and exportability.
Design Specification:
- Entity: AuditLog (id, actor, timestamp, entity, before, after)
- Service: AuditService (log, export, validate coverage)
- Controller: AuditController (REST endpoints)
Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String actor, entity;
    private LocalDateTime timestamp;
    private String before, after;
}
@Service
public class AuditService {
    public void logChange(String actor, String entity, String before, String after) { ... }
}
```

Section: Reporting & Analytics
Description: Provides operational reports, dashboards, and export functionality for BI and compliance.
Design Specification:
- Service: ReportingService (filter, export CSV/PDF, metrics endpoints)
- Controller: ReportingController (REST endpoints)
- Access control for reports
Sample Implementation:
```java
@Service
public class ReportingService {
    public File exportReport(String type, Map<String, Object> filters) { ... }
}
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping public File getReport(...) { ... }
}
```

Section: Mobile Access (PWA)
Description: Enables mobile-friendly access to core flows with offline support and PWA manifest.
Design Specification:
- Responsive views for clock-in/out, schedules, leave, announcements
- PWA manifest in public directory
- Offline queue for attendance events
- Lighthouse score â¥ 80
Sample Implementation:
```json
// manifest.json
{
  "name": "Warehouse Employee Management",
  "short_name": "Warehouse",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#fff",
  "theme_color": "#2196f3"
}
```

Section: Onboarding & Offboarding Workflow
Description: Automates employee lifecycle changes, provisioning, and deprovisioning tasks.
Design Specification:
- Service: OnboardingService (HRIS integration, task generation, asset assignment, training)
- OffboardingService (access revocation, asset collection, schedule update)
- Controller: OnboardingController (REST endpoints)
Sample Implementation:
```java
@Service
public class OnboardingService {
    public void onboardEmployee(Long employeeId) { ... }
    public void offboardEmployee(Long employeeId) { ... }
}
@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @PostMapping("/onboard") public void onboard(...) { ... }
    @PostMapping("/offboard") public void offboard(...) { ... }
}
```

---
GitHub Upload Status: Uploading technical design document to PRD/Technical_Design_document.md.
