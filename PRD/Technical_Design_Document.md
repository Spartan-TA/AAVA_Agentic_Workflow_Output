# Warehouse EMS - Comprehensive Low-Level Technical Design Document

---

## EPIC E01: Project Scaffolding & Domain Setup

Section: Spring Boot Architecture Overview  
Description: Establishes the foundational structure for the Warehouse EMS application using Spring Boot with Maven, modularizing core domains and enabling essential infrastructure.  
Design Specification:  
- Base package: `com.warehouse.ems`  
- Modules: `employee`, `scheduling`, `attendance`, `safety`  
- DB migration: Flyway/Liquibase  
- Monitoring: Spring Boot Actuator  
- Application port: 8080  
Sample Implementation:  
```java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

Section: Package Structure  
Description: Organizes code for maintainability and scalability.  
Design Specification:  
- `com.warehouse.ems.employee`  
- `com.warehouse.ems.scheduling`  
- `com.warehouse.ems.attendance`  
- `com.warehouse.ems.safety`  
Sample Implementation:  
```
src/main/java/com/warehouse/ems/employee/EmployeeController.java
src/main/java/com/warehouse/ems/attendance/AttendanceService.java
```

Section: DB Migration  
Description: Ensures schema consistency and versioning.  
Design Specification:  
- Flyway/Liquibase configuration in `application.yml`  
- Baseline migration scripts in `src/main/resources/db/migration`  
Sample Implementation:  
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

Section: Monitoring  
Description: Enables health checks and metrics.  
Design Specification:  
- Actuator endpoints `/actuator/health`, `/actuator/metrics`  
Sample Implementation:  
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, metrics
```

---

## EPIC E02: Employee Master Data (CRUD)

Section: Domain Model  
Description: Employee entity as the central record.  
Design Specification:  
- Fields: `id`, `name`, `badgeId`, `role`, `department`, `shiftGroup`, `hireDate`, `status`  
Sample Implementation:  
```java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    private String name;
    @Column(unique = true)
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}
```

Section: Repository Layer  
Description: CRUD operations for Employee.  
Design Specification:  
- Interface: `EmployeeRepository extends JpaRepository<Employee, Long>`  
Sample Implementation:  
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
}
```

Section: Service Layer  
Description: Business logic for employee management.  
Design Specification:  
- Methods: `createEmployee`, `updateEmployee`, `deleteEmployee`, `getEmployees`  
Sample Implementation:  
```java
@Service
public class EmployeeService {
    // CRUD methods
}
```

Section: Controller  
Description: REST endpoints for Employee CRUD.  
Design Specification:  
- Endpoints: `/employees` (GET, POST, PUT, PATCH, DELETE)  
Sample Implementation:  
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    // CRUD endpoints
}
```

Section: DTOs & Validation  
Description: Data transfer and input validation.  
Design Specification:  
- DTOs for requests/responses  
- Validation annotations (`@NotNull`, `@Size`, etc.)  
Sample Implementation:  
```java
public class EmployeeDTO {
    @NotNull private String name;
    @NotNull private String badgeId;
    // ...
}
```

---

## EPIC E03: Role Based Access Control (RBAC)

Section: Security Configuration  
Description: Restricts access based on roles using Spring Security.  
Design Specification:  
- Roles: `ADMIN`, `HR`, `SUPERVISOR`, `WORKER`  
- Method/endpoint security via `@PreAuthorize`  
- Row-level constraints in service/repository  
- API key/OAuth2 toggle via config  
Sample Implementation:  
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    // configure roles and endpoints
}
```

Section: Security Rules  
Description: Enforces authorization and authentication.  
Design Specification:  
- Unauthorized: 401  
- Forbidden: 403  
- ADMIN: full access  
- SUPERVISOR: limited to team  
Sample Implementation:  
```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteEmployee(Long id) { ... }
```

Section: Security Testing  
Description: Automated tests for security rules.  
Design Specification:  
- Test cases for endpoint/method access  
Sample Implementation:  
```java
@Test
public void testAdminAccess() { ... }
```

---

## EPIC E04: Time & Attendance (Clock In/Out)

Section: Attendance Entity  
Description: Captures clock-in/out events.  
Design Specification:  
- Fields: `id`, `employeeId`, `clockInTime`, `clockOutTime`, `deviceId`, `geoLocation`, `shiftId`, `status`  
Sample Implementation:  
```java
@Entity
public class Attendance {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private String deviceId;
    private String geoLocation;
    private Long shiftId;
    private String status;
}
```

Section: Attendance Controller  
Description: REST endpoints for clock-in/out.  
Design Specification:  
- Endpoints: `/attendance/clock-in`, `/attendance/clock-out`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockInDTO dto) { ... }
}
```

Section: Corrections Workflow  
Description: Handles missed punches and corrections.  
Design Specification:  
- Correction requests create approval tasks  
Sample Implementation:  
```java
public class CorrectionRequest {
    private Long attendanceId;
    private String reason;
    private ApprovalStatus status;
}
```

Section: Reporting  
Description: Attendance reports exportable as CSV.  
Design Specification:  
- Export endpoint `/attendance/report`  
Sample Implementation:  
```java
@GetMapping("/report")
public void exportAttendanceReport(...) { ... }
```

---

## EPIC E05: Shift & Schedule Management

Section: Shift Entity  
Description: Defines shift templates and assignments.  
Design Specification:  
- Fields: `id`, `name`, `startTime`, `endTime`, `recurrence`, `overtimeRules`, `blackoutDates`, `assignedEmployees`  
Sample Implementation:  
```java
@Entity
public class Shift {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrence;
    private String overtimeRules;
    private List<LocalDate> blackoutDates;
}
```

Section: Schedule Controller  
Description: CRUD for shift templates and schedules.  
Design Specification:  
- Endpoints: `/shifts`, `/schedules`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    // CRUD endpoints
}
```

Section: Conflict Detection  
Description: Prevents scheduling conflicts.  
Design Specification:  
- Service method to check for overlaps  
Sample Implementation:  
```java
public boolean hasConflict(Employee employee, LocalDateTime start, LocalDateTime end) { ... }
```

Section: Bulk Assignment  
Description: Assigns shifts to multiple employees.  
Design Specification:  
- Bulk assignment endpoint  
Sample Implementation:  
```java
@PostMapping("/bulk-assign")
public void bulkAssignShifts(@RequestBody BulkAssignDTO dto) { ... }
```

Section: Audit Entries  
Description: Logs schedule changes.  
Design Specification:  
- Audit log entity and service  
Sample Implementation:  
```java
public class AuditEntry { ... }
```

---

## EPIC E06: Leave & Absence Management

Section: Leave Entity  
Description: Tracks leave requests and balances.  
Design Specification:  
- Fields: `id`, `employeeId`, `type`, `startDate`, `endDate`, `status`, `accrualBalance`  
Sample Implementation:  
```java
@Entity
public class Leave {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Double accrualBalance;
}
```

Section: Leave Controller  
Description: Endpoints for leave requests and approvals.  
Design Specification:  
- Endpoints: `/leave/request`, `/leave/approve`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    // request/approve endpoints
}
```

Section: Integration Hooks  
Description: Excludes leave from scheduling and payroll.  
Design Specification:  
- Service hooks to update schedules and payroll  
Sample Implementation:  
```java
public void updateScheduleForLeave(Long employeeId, LocalDate start, LocalDate end) { ... }
```

Section: Reporting  
Description: Exports approved leaves.  
Design Specification:  
- Export endpoint `/leave/report`  
Sample Implementation:  
```java
@GetMapping("/report")
public void exportLeaveReport(...) { ... }
```

---

## EPIC E07: Training & Certification Tracking

Section: Certification Entity  
Description: Tracks employee certifications and expirations.  
Design Specification:  
- Fields: `id`, `employeeId`, `type`, `issueDate`, `expiryDate`, `documentUrl`, `status`  
Sample Implementation:  
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
    private String status;
}
```

Section: Certification Controller  
Description: CRUD endpoints for certifications.  
Design Specification:  
- Endpoints: `/certifications`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    // CRUD endpoints
}
```

Section: Expiry Alerts  
Description: Notifies about upcoming expirations.  
Design Specification:  
- Scheduled job for alerts (30/7 days before expiry)  
Sample Implementation:  
```java
@Scheduled(cron = "0 0 8 * * ?")
public void sendExpiryAlerts() { ... }
```

Section: Scheduling Checks  
Description: Blocks assignment if certification expired.  
Design Specification:  
- Service method to validate certification before assignment  
Sample Implementation:  
```java
public boolean isCertified(Long employeeId, String certType) { ... }
```

---

## EPIC E08: Safety Incidents & OSHA Reporting

Section: Incident Entity  
Description: Records safety incidents and near-misses.  
Design Specification:  
- Fields: `id`, `severity`, `location`, `description`, `involvedEmployees`, `status`, `createdAt`, `updatedAt`  
Sample Implementation:  
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String severity;
    private String location;
    private String description;
    private List<Long> involvedEmployees;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

Section: Incident Controller  
Description: Endpoints for incident reporting and workflow.  
Design Specification:  
- Endpoints: `/safety/incidents`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
    // CRUD and workflow endpoints
}
```

Section: Workflow  
Description: Status transitions (Open, Investigating, Resolved).  
Design Specification:  
- State machine or enum for status  
Sample Implementation:  
```java
public enum IncidentStatus { OPEN, INVESTIGATING, RESOLVED }
```

Section: OSHA Reporting  
Description: Exports OSHA 300/300A fields.  
Design Specification:  
- Export endpoint `/safety/osha-report`  
Sample Implementation:  
```java
@GetMapping("/osha-report")
public void exportOshaReport(...) { ... }
```

Section: Metrics Dashboard  
Description: Safety KPIs and metrics endpoints.  
Design Specification:  
- Dashboard endpoints `/safety/metrics`  
Sample Implementation:  
```java
@GetMapping("/metrics")
public SafetyMetrics getMetrics() { ... }
```

---

## EPIC E09: Equipment & Asset Assignment

Section: Asset Entity  
Description: Tracks equipment and PPE assignments.  
Design Specification:  
- Fields: `id`, `type`, `condition`, `assignedTo`, `checkoutDate`, `returnDate`, `certRequired`, `status`  
Sample Implementation:  
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String condition;
    private Long assignedTo;
    private LocalDateTime checkoutDate;
    private LocalDateTime returnDate;
    private String certRequired;
    private String status;
}
```

Section: Asset Controller  
Description: Endpoints for asset registry and check-in/out.  
Design Specification:  
- Endpoints: `/assets`, `/assets/check-in`, `/assets/check-out`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    // CRUD and check-in/out endpoints
}
```

Section: Certification Validation  
Description: Blocks asset use if certification missing.  
Design Specification:  
- Service method to validate certification before checkout  
Sample Implementation:  
```java
public boolean canCheckout(Long employeeId, String assetType) { ... }
```

Section: History Log  
Description: Tracks asset assignment history.  
Design Specification:  
- History entity and endpoints  
Sample Implementation:  
```java
@Entity
public class AssetHistory { ... }
```

Section: Overdue Reports  
Description: Reports overdue asset returns.  
Design Specification:  
- Reporting endpoint `/assets/overdue`  
Sample Implementation:  
```java
@GetMapping("/overdue")
public List<Asset> getOverdueAssets() { ... }
```

---

## EPIC E10: Performance Reviews & Goals

Section: Review Entity  
Description: Tracks performance reviews and goals.  
Design Specification:  
- Fields: `id`, `employeeId`, `cycle`, `goals`, `competencies`, `ratings`, `comments`, `acknowledgedBySupervisor`, `acknowledgedByEmployee`, `status`  
Sample Implementation:  
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String cycle;
    private String goals;
    private String competencies;
    private String ratings;
    private String comments;
    private Boolean acknowledgedBySupervisor;
    private Boolean acknowledgedByEmployee;
    private String status;
}
```

Section: Review Controller  
Description: Endpoints for review cycles and workflow.  
Design Specification:  
- Endpoints: `/reviews`, `/reviews/acknowledge`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/reviews")
public class PerformanceReviewController {
    // CRUD and workflow endpoints
}
```

Section: PDF Export  
Description: Exports reviews as PDF.  
Design Specification:  
- Export endpoint `/reviews/export`  
Sample Implementation:  
```java
@GetMapping("/export")
public void exportReviewPdf(...) { ... }
```

Section: Role-Based Visibility  
Description: Controls access to reviews.  
Design Specification:  
- Security annotations and service checks  
Sample Implementation:  
```java
@PreAuthorize("hasRole('HR') or hasRole('SUPERVISOR')")
public PerformanceReview getReview(Long id) { ... }
```

Section: Immutable History  
Description: Locks reviews after sign-off.  
Design Specification:  
- Status field and service logic  
Sample Implementation:  
```java
public void lockReview(Long reviewId) { ... }
```

---

## EPIC E11: Payroll Export Integration

Section: Payroll Export Service  
Description: Generates payroll-ready files from attendance and leave.  
Design Specification:  
- Service to aggregate approved attendance/leave  
- Mapping to external provider formats  
- Secure delivery via SFTP/API  
Sample Implementation:  
```java
@Service
public class PayrollExportService {
    public File generatePayrollFile(...) { ... }
    public void deliverPayrollFile(File file) { ... }
}
```

Section: Export Controller  
Description: Endpoint for payroll export.  
Design Specification:  
- Endpoint: `/payroll/export`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/export")
    public void exportPayroll(...) { ... }
}
```

Section: Delivery & Retry  
Description: Handles failed deliveries with backoff.  
Design Specification:  
- Retry logic in service  
Sample Implementation:  
```java
public void deliverWithRetry(File file) { ... }
```

Section: Audit Log  
Description: Logs every export event.  
Design Specification:  
- Audit entity and service  
Sample Implementation:  
```java
public class PayrollAuditEntry { ... }
```

---

## EPIC E12: Notifications & Announcements

Section: Notification Entity  
Description: Tracks notifications and announcements.  
Design Specification:  
- Fields: `id`, `type`, `recipientId`, `channel`, `content`, `status`, `deliveryTime`  
Sample Implementation:  
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private Long recipientId;
    private String channel;
    private String content;
    private String status;
    private LocalDateTime deliveryTime;
}
```

Section: Notification Controller  
Description: Endpoints for sending and managing notifications.  
Design Specification:  
- Endpoints: `/notifications`, `/announcements`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    // CRUD and send endpoints
}
```

Section: Channel Opt-In/Out  
Description: Users manage notification preferences.  
Design Specification:  
- Preference entity and endpoints  
Sample Implementation:  
```java
@Entity
public class NotificationPreference { ... }
```

Section: Localization & Templates  
Description: Localized templates for notifications.  
Design Specification:  
- Template service with i18n support  
Sample Implementation:  
```java
public String getLocalizedTemplate(String templateId, Locale locale) { ... }
```

Section: Quiet Hours  
Description: Respects user quiet hours.  
Design Specification:  
- Service logic to check quiet hours before sending  
Sample Implementation:  
```java
public boolean isQuietHour(Long userId) { ... }
```

---

## EPIC E13: Integration Layer (HRIS/WMS APIs)

Section: HRIS Integration  
Description: Syncs employee data from HRIS.  
Design Specification:  
- REST client to HRIS API  
- Scheduled job for sync  
Sample Implementation:  
```java
@Service
public class HrisIntegrationService {
    public void syncEmployees() { ... }
}
```

Section: WMS Integration  
Description: Links location/department data from WMS.  
Design Specification:  
- REST client to WMS API  
- Caching for performance  
Sample Implementation:  
```java
@Service
public class WmsIntegrationService {
    public Department getDepartment(String deptId) { ... }
}
```

Section: Webhooks  
Description: Sends event notifications to external systems.  
Design Specification:  
- Webhook entity and delivery service  
Sample Implementation:  
```java
@Entity
public class Webhook { ... }
```

Section: OpenAPI Documentation  
Description: Documents integration APIs.  
Design Specification:  
- Swagger/OpenAPI annotations  
Sample Implementation:  
```java
@Operation(summary = "Sync employees from HRIS")
public void syncEmployees() { ... }
```

---

## EPIC E14: Audit Trail & Compliance

Section: Audit Entity  
Description: Logs sensitive changes.  
Design Specification:  
- Fields: `id`, `actor`, `action`, `entity`, `entityId`, `beforeValue`, `afterValue`, `timestamp`  
Sample Implementation:  
```java
@Entity
public class AuditEntry {
    @Id @GeneratedValue
    private Long id;
    private String actor;
    private String action;
    private String entity;
    private Long entityId;
    private String beforeValue;
    private String afterValue;
    private LocalDateTime timestamp;
}
```

Section: Audit Service  
Description: Centralized audit logging.  
Design Specification:  
- Service to log changes  
Sample Implementation:  
```java
@Service
public class AuditService {
    public void logChange(...) { ... }
}
```

Section: Tamper-Evident Storage  
Description: Immutable audit logs.  
Design Specification:  
- Database constraints or append-only storage  
Sample Implementation:  
```java
// Use database triggers or immutable collections
```

Section: Export & Reporting  
Description: Exports audit logs for compliance.  
Design Specification:  
- Export endpoint `/audit/export`  
Sample Implementation:  
```java
@GetMapping("/export")
public void exportAuditLog(...) { ... }
```

Section: Coverage Tests  
Description: Validates audit coverage.  
Design Specification:  
- Automated tests for audit logging  
Sample Implementation:  
```java
@Test
public void testAuditLogging() { ... }
```

---

## EPIC E15: Reporting & Analytics

Section: Reporting Service  
Description: Generates reports for attendance, overtime, leave, certifications, safety.  
Design Specification:  
- Service methods for each report type  
Sample Implementation:  
```java
@Service
public class ReportingService {
    public AttendanceReport generateAttendanceReport(...) { ... }
    public OvertimeReport generateOvertimeReport(...) { ... }
}
```

Section: Reporting Controller  
Description: Endpoints for report generation.  
Design Specification:  
- Endpoints: `/reports/attendance`, `/reports/overtime`, etc.  
Sample Implementation:  
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    // report endpoints
}
```

Section: Export Formats  
Description: Supports CSV/PDF export.  
Design Specification:  
- Export service with format options  
Sample Implementation:  
```java
public File exportReport(Report report, String format) { ... }
```

Section: KPI Dashboard  
Description: Real-time metrics and KPIs.  
Design Specification:  
- Dashboard endpoints `/dashboard/kpis`  
Sample Implementation:  
```java
@GetMapping("/kpis")
public KpiMetrics getKpis() { ... }
```

---

## EPIC E16: Mobile Access (PWA)

Section: Responsive UI  
Description: Mobile-friendly views for core flows.  
Design Specification:  
- Responsive CSS/JS for clock-in/out, schedules, leave  
Sample Implementation:  
```html
<meta name="viewport" content="width=device-width, initial-scale=1">
```

Section: PWA Manifest  
Description: Installable PWA with manifest.  
Design Specification:  
- `manifest.json` with app metadata  
Sample Implementation:  
```json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone"
}
```

Section: Offline Queue  
Description: Queues clock events offline.  
Design Specification:  
- Service worker for offline support  
Sample Implementation:  
```javascript
self.addEventListener('sync', event => {
  if (event.tag === 'sync-clock-events') {
    event.waitUntil(syncClockEvents());
  }
});
```

Section: Lighthouse Score  
Description: Validates PWA standards.  
Design Specification:  
- Lighthouse audit in CI/CD  
Sample Implementation:  
```bash
lighthouse https://ems.example.com --output=json
```

---

## EPIC E17: Onboarding & Offboarding Workflow

Section: Onboarding Service  
Description: Automates new hire provisioning.  
Design Specification:  
- Service to create employee, assign schedule, generate tasks  
Sample Implementation:  
```java
@Service
public class OnboardingService {
    public void onboardEmployee(Employee employee) { ... }
}
```

Section: Offboarding Service  
Description: Automates termination workflow.  
Design Specification:  
- Service to revoke access, collect assets  
Sample Implementation:  
```java
@Service
public class OffboardingService {
    public void offboardEmployee(Long employeeId) { ... }
}
```

Section: Task Generation  
Description: Generates onboarding/offboarding tasks.  
Design Specification:  
- Task entity and service  
Sample Implementation:  
```java
@Entity
public class Task { ... }
```

Section: Integration Hooks  
Description: Syncs with HRIS for new hires/terminations.  
Design Specification:  
- Webhook or scheduled job  
Sample Implementation:  
```java
public void syncHrisEvents() { ... }
```

---

## EPIC E18: Localization & Multi-Warehouse

Section: Warehouse Entity  
Description: Supports multiple warehouses.  
Design Specification:  
- Fields: `id`, `name`, `location`, `calendar`, `policies`  
Sample Implementation:  
```java
@Entity
public class Warehouse {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String location;
    private String calendar;
    private String policies;
}
```

Section: Policy Scoping  
Description: Scopes policies by warehouse.  
Design Specification:  
- Policy entity with warehouse reference  
Sample Implementation:  
```java
@Entity
public class Policy {
    @Id @GeneratedValue
    private Long id;
    private Long warehouseId;
    private String policyType;
    private String rules;
}
```

Section: Localization  
Description: UI language toggle (English/Spanish).  
Design Specification:  
- i18n support with resource bundles  
Sample Implementation:  
```java
@Bean
public LocaleResolver localeResolver() {
    SessionLocaleResolver resolver = new SessionLocaleResolver();
    resolver.setDefaultLocale(Locale.US);
    return resolver;
}
```

Section: Locale Formatting  
Description: Respects locale for date/time.  
Design Specification:  
- Formatter service with locale support  
Sample Implementation:  
```java
public String formatDate(LocalDate date, Locale locale) { ... }
```

Section: Test Coverage  
Description: Tests for both languages.  
Design Specification:  
- Automated tests for English/Spanish  
Sample Implementation:  
```java
@Test
public void testSpanishLocale() { ... }
```

---

## EPIC E19: Advanced Scheduling (AI-Assisted)

Section: AI Model  
Description: Suggests optimal shift assignments.  
Design Specification:  
- ML model trained on historical data  
- Ranking and explainability  
Sample Implementation:  
```java
@Service
public class AiSchedulingService {
    public List<ShiftSuggestion> suggestShifts(...) { ... }
}
```

Section: Suggestion Controller  
Description: Endpoints for AI suggestions.  
Design Specification:  
- Endpoints: `/scheduling/suggestions`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/scheduling")
public class AiSchedulingController {
    @GetMapping("/suggestions")
    public List<ShiftSuggestion> getSuggestions(...) { ... }
}
```

Section: Feedback Loop  
Description: Collects feedback for model improvement.  
Design Specification:  
- Feedback entity and retraining job  
Sample Implementation:  
```java
@Entity
public class SchedulingFeedback { ... }
```

Section: Explainability  
Description: Provides reasoning for suggestions.  
Design Specification:  
- Explainability notes in suggestion response  
Sample Implementation:  
```java
public class ShiftSuggestion {
    private String explanation;
    // ...
}
```

---

## EPIC E20: Continuous Improvement & Feedback

Section: Feedback Widget  
Description: In-app feedback submission.  
Design Specification:  
- Feedback entity and endpoints  
Sample Implementation:  
```java
@Entity
public class Feedback {
    @Id @GeneratedValue
    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime submittedAt;
}
```

Section: Feature Voting  
Description: Users vote on feature requests.  
Design Specification:  
- Feature request entity with vote count  
Sample Implementation:  
```java
@Entity
public class FeatureRequest {
    @Id @GeneratedValue
    private Long id;
    private String title;
    private Integer voteCount;
}
```

Section: Release Notes  
Description: Displays release notes on login.  
Design Specification:  
- Release notes entity and display logic  
Sample Implementation:  
```java
@Entity
public class ReleaseNote { ... }
```

Section: Health Checks  
Description: Automated health checks every 5 minutes.  
Design Specification:  
- Scheduled job for health checks  
Sample Implementation:  
```java
@Scheduled(fixedRate = 300000)
public void performHealthCheck() { ... }
```

Section: SLA Tracking  
Description: Tracks uptime and SLA metrics.  
Design Specification:  
- Metrics service and dashboard  
Sample Implementation:  
```java
@Service
public class SlaTrackingService {
    public SlaMetrics getSlaMetrics() { ... }
}
```

---

## Conclusion

This comprehensive low-level technical design document covers all 96 user stories across 20 epics for the Warehouse EMS system. Each section provides detailed design specifications, sample implementations, and integration points following Spring Boot best practices and industry standards.