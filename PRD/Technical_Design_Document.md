# Warehouse Employee Management System - Comprehensive Low-Level Technical Design Document

---

## Table of Contents

- [E01: Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
- [E02: Employee Master Data CRUD](#e02-employee-master-data-crud)
- [E03: Role Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
- [E04: Time & Attendance Clock In/Out](#e04-time--attendance-clock-inout)
- [E05: Shift & Schedule Management](#e05-shift--schedule-management)
- [E06: Leave & Absence Management](#e06-leave--absence-management)
- [E07: Training & Certification Tracking](#e07-training--certification-tracking)
- [E08: Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
- [E09: Equipment & Asset Assignment](#e09-equipment--asset-assignment)
- [E10: Performance Reviews & Goals](#e10-performance-reviews--goals)
- [E11: Payroll Export Integration](#e11-payroll-export-integration)
- [E12: Notifications & Announcements](#e12-notifications--announcements)
- [E13: Integration Layer HRIS/WMS APIs](#e13-integration-layer-hriswms-apis)
- [E14: Audit Trail & Compliance](#e14-audit-trail--compliance)
- [E15: Reporting & Analytics](#e15-reporting--analytics)
- [E16: Mobile Access PWA](#e16-mobile-access-pwa)
- [E17: Onboarding & Offboarding Workflow](#e17-onboarding--offboarding-workflow)
- [E18: Localization & Multi-Warehouse](#e18-localization--multi-warehouse)
- [E19: Advanced Scheduling Optimization](#e19-advanced-scheduling-optimization)
- [E20: Self-Service Portal Enhancements](#e20-self-service-portal-enhancements)

---

## E01: Project Scaffolding & Domain Setup

### Section: Spring Boot Architecture Overview
Description: Establishes the foundational structure for the Warehouse EMS, ensuring modularity, scalability, and maintainability. Uses Maven for dependency management, Flyway/Liquibase for DB migrations, and Spring Boot Actuator for health monitoring.
Design Specification:
- Modular package structure: `com.warehouse.ems.[module]`
- Core modules: employee, scheduling, attendance, safety
- DB migration: Flyway/Liquibase
- Health monitoring: Actuator
Sample Implementation:
```java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

### Section: Package Structure
Description: Organizes code into logical modules for separation of concerns.
Design Specification:
- `com.warehouse.ems.employee`
- `com.warehouse.ems.scheduling`
- `com.warehouse.ems.attendance`
- `com.warehouse.ems.safety`
Sample Implementation:
```
src/main/java/com/warehouse/ems/employee/
src/main/java/com/warehouse/ems/scheduling/
...
```

### Section: Configuration
Description: Sets up application properties, DB migration, and actuator endpoints.
Design Specification:
- `application.yml` with DB, port, actuator settings
- Flyway/Liquibase migration scripts in `src/main/resources/db/migration`
Sample Implementation:
```yaml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems
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

---

## E02: Employee Master Data CRUD

### Section: Spring Boot Architecture Overview
Description: Implements CRUD operations for employee records, enforcing unique badge IDs and supporting soft deletes, pagination, and filtering.
Design Specification:
- RESTful APIs: `/employees`
- Domain-driven design for Employee entity
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
```

### Section: Repository Layer
Description: Provides data access with custom queries for filtering and soft deletes.
Design Specification:
- `EmployeeRepository extends JpaRepository<Employee, Long>`
- Custom query: `findByDeletedFalse`
Sample Implementation:
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

### Section: Service Layer
Description: Handles business logic, validation, and transaction management.
Design Specification:
- Employee creation, update, soft-delete, validation
Sample Implementation:
```java
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository repo;
    public Employee create(EmployeeDto dto) { /* ... */ }
    public Employee update(Long id, EmployeeDto dto) { /* ... */ }
    public void softDelete(Long id) { /* ... */ }
}
```

### Section: Controller Layer
Description: Exposes REST endpoints with DTOs and validation.
Design Specification:
- Endpoints: POST/GET/PUT/PATCH/DELETE `/employees`
- Request/response DTOs
Sample Implementation:
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService service;
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { /* ... */ }
    // Other endpoints...
}
```

### Section: Configuration
Description: OpenAPI documentation, pagination defaults.
Design Specification:
- `springdoc-openapi` integration
Sample Implementation:
```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
```

---

## E03: Role Based Access Control (RBAC)

### Section: Spring Boot Architecture Overview
Description: Secures endpoints and methods using Spring Security, supporting roles and row-level constraints.
Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Method/endpoint security
Sample Implementation:
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
            .and().httpBasic();
    }
}
```

### Section: Configuration
Description: API key/OAuth2 toggle via config.
Design Specification:
- `application.yml` toggle
Sample Implementation:
```yaml
security:
  oauth2:
    enabled: true
  api-key:
    enabled: false
```

### Section: Service Layer
Description: Row-level security enforcement.
Design Specification:
- Supervisor limited to team
Sample Implementation:
```java
@PreAuthorize("hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(authentication, #employeeId)")
public Employee getEmployee(Long employeeId) { /* ... */ }
```

---

## E04: Time & Attendance Clock In/Out

### Section: Spring Boot Architecture Overview
Description: Manages clock-in/out events, geofence/device capture, shift association, and corrections workflow.
Design Specification:
- Attendance entity, event endpoints
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
```

### Section: Controller Layer
Description: Endpoints for clock-in/out, corrections.
Design Specification:
- POST `/attendance/clock-in`
- POST `/attendance/clock-out`
Sample Implementation:
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody AttendanceDto dto) { /* ... */ }
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody AttendanceDto dto) { /* ... */ }
}
```

### Section: Service Layer
Description: Shift association, daily totals, corrections workflow.
Design Specification:
- Automatic shift lookup
- Approval tasks for corrections
Sample Implementation:
```java
@Service
public class AttendanceService {
    public void clockIn(Long employeeId, String deviceId, String location) { /* ... */ }
    public void clockOut(Long employeeId, String deviceId, String location) { /* ... */ }
    public List<AttendanceEvent> getDailyTotals(Long employeeId, LocalDate date) { /* ... */ }
}
```

---

## E05: Shift & Schedule Management

### Section: Spring Boot Architecture Overview
Description: Handles shift templates, rotations, overtime rules, blackout dates, and assignment.
Design Specification:
- ShiftTemplate, ShiftAssignment entities
Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    // getters/setters
}
```

### Section: Controller Layer
Description: CRUD endpoints for shift templates and schedules.
Design Specification:
- `/shifts/templates`
- `/shifts/assignments`
Sample Implementation:
```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates")
    public ResponseEntity<ShiftTemplateDto> createTemplate(@RequestBody ShiftTemplateDto dto) { /* ... */ }
    // Other endpoints...
}
```

### Section: Service Layer
Description: Conflict detection, bulk assignment, audit entries.
Design Specification:
- Detect overlapping shifts
- Bulk assign to employees
Sample Implementation:
```java
@Service
public class ShiftService {
    public void assignShifts(List<Long> employeeIds, ShiftAssignmentDto dto) { /* ... */ }
    public boolean hasConflict(Long employeeId, LocalDate date) { /* ... */ }
}
```

---

## E06: Leave & Absence Management

### Section: Spring Boot Architecture Overview
Description: Manages PTO, sick, unpaid leave requests, approvals, accrual balances, and integration with scheduling/payroll.
Design Specification:
- LeaveRequest entity, accrual policy
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
```

### Section: Controller Layer
Description: Endpoints for leave requests and approvals.
Design Specification:
- `/leave/requests`
Sample Implementation:
```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/requests")
    public ResponseEntity<LeaveRequestDto> requestLeave(@RequestBody LeaveRequestDto dto) { /* ... */ }
    @PostMapping("/approve")
    public ResponseEntity<?> approveLeave(@RequestParam Long requestId) { /* ... */ }
}
```

### Section: Service Layer
Description: Accrual balance updates, auto-flag for coverage.
Design Specification:
- Update balances on approval
- Flag scheduled shifts for coverage
Sample Implementation:
```java
@Service
public class LeaveService {
    public void requestLeave(Long employeeId, LeaveRequestDto dto) { /* ... */ }
    public void approveLeave(Long requestId) { /* ... */ }
}
```

---

## E07: Training & Certification Tracking

### Section: Spring Boot Architecture Overview
Description: Tracks certifications, expirations, renewals, and blocks assignment to tasks requiring expired certs.
Design Specification:
- Certification entity, alerts, proof document upload
Sample Implementation:
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type;
    private LocalDate expiryDate;
    private String documentUrl;
    // getters/setters
}
```

### Section: Controller Layer
Description: CRUD endpoints for certifications, alerts.
Design Specification:
- `/certifications`
Sample Implementation:
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<CertificationDto> addCertification(@RequestBody CertificationDto dto) { /* ... */ }
    @GetMapping("/alerts")
    public List<CertificationAlertDto> getAlerts() { /* ... */ }
}
```

### Section: Service Layer
Description: Scheduling checks, alert generation.
Design Specification:
- Block unqualified assignments
- Generate expiry alerts
Sample Implementation:
```java
@Service
public class CertificationService {
    public void checkAssignment(Long employeeId, String taskType) { /* ... */ }
    public List<CertificationAlertDto> getExpiringCertifications() { /* ... */ }
}
```

---

## E08: Safety Incidents & OSHA Reporting

### Section: Spring Boot Architecture Overview
Description: Records incidents, severity, location, involved employees, investigation workflow, and OSHA summary generation.
Design Specification:
- SafetyIncident entity, status workflow
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
```

### Section: Controller Layer
Description: Endpoints for incident reporting, status updates, OSHA export.
Design Specification:
- `/safety/incidents`
Sample Implementation:
```java
@RestController
@RequestMapping("/safety")
public class SafetyController {
    @PostMapping("/incidents")
    public ResponseEntity<SafetyIncidentDto> reportIncident(@RequestBody SafetyIncidentDto dto) { /* ... */ }
    @GetMapping("/osha/export")
    public ResponseEntity<Resource> exportOshaReport() { /* ... */ }
}
```

### Section: Service Layer
Description: Investigation workflow, metrics dashboard.
Design Specification:
- Status transitions
- Dashboard metrics
Sample Implementation:
```java
@Service
public class SafetyService {
    public void reportIncident(SafetyIncidentDto dto) { /* ... */ }
    public void updateStatus(Long incidentId, String status) { /* ... */ }
}
```

---

## E09: Equipment & Asset Assignment

### Section: Spring Boot Architecture Overview
Description: Assigns assets to employees, tracks check-in/out, blocks use if certification missing, maintains asset condition.
Design Specification:
- Asset, AssetAssignment entities
Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private String condition;
    // getters/setters
}
@Entity
public class AssetAssignment {
    @Id @GeneratedValue
    private Long id;
    private Long assetId;
    private Long employeeId;
    private LocalDateTime checkoutTime;
    private LocalDateTime returnTime;
    // getters/setters
}
```

### Section: Controller Layer
Description: Endpoints for asset registry, check-in/out, history log.
Design Specification:
- `/assets`
- `/assets/assignments`
Sample Implementation:
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping
    public ResponseEntity<AssetDto> addAsset(@RequestBody AssetDto dto) { /* ... */ }
    @PostMapping("/assign")
    public ResponseEntity<AssetAssignmentDto> assignAsset(@RequestBody AssetAssignmentDto dto) { /* ... */ }
}
```

### Section: Service Layer
Description: Certification checks, overdue reports.
Design Specification:
- Block assignment if certs invalid
- Generate overdue reports
Sample Implementation:
```java
@Service
public class AssetService {
    public void assignAsset(Long assetId, Long employeeId) { /* ... */ }
    public List<AssetAssignmentDto> getOverdueAssignments() { /* ... */ }
}
```

---

## E10: Performance Reviews & Goals

### Section: Spring Boot Architecture Overview
Description: Manages review templates, goals, competencies, ratings, comments, and acknowledgements.
Design Specification:
- PerformanceReview, Goal entities
Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private LocalDate reviewDate;
    private String template;
    private String rating;
    private String comments;
    private boolean acknowledged;
    // getters/setters
}
```

### Section: Controller Layer
Description: Endpoints for review cycles, submission, PDF export.
Design Specification:
- `/reviews`
Sample Implementation:
```java
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping
    public ResponseEntity<PerformanceReviewDto> submitReview(@RequestBody PerformanceReviewDto dto) { /* ... */ }
    @GetMapping("/export/pdf")
    public ResponseEntity<Resource> exportPdf(@RequestParam Long reviewId) { /* ... */ }
}
```

### Section: Service Layer
Description: Immutable history after sign-off, role-based visibility.
Design Specification:
- Lock review after acknowledgement
- Filter by role
Sample Implementation:
```java
@Service
public class ReviewService {
    public void submitReview(PerformanceReviewDto dto) { /* ... */ }
    public void acknowledgeReview(Long reviewId) { /* ... */ }
}
```

---

## E11: Payroll Export Integration

### Section: Spring Boot Architecture Overview
Description: Generates payroll-ready files from attendance/leave, maps to provider formats, delivers via SFTP/API.
Design Specification:
- PayrollExport entity, delivery logic
Sample Implementation:
```java
@Entity
public class PayrollExport {
    @Id @GeneratedValue
    private Long id;
    private LocalDate exportDate;
    private String provider;
    private String status; // SUCCESS, FAILED, RETRY
    private String fileUrl;
    // getters/setters
}
```

### Section: Controller Layer
Description: Endpoints for export generation, reconciliation.
Design Specification:
- `/payroll/export`
Sample Implementation:
```java
@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/export")
    public ResponseEntity<PayrollExportDto> generateExport(@RequestBody PayrollExportDto dto) { /* ... */ }
}
```

### Section: Service Layer
Description: Retry with backoff, audit log.
Design Specification:
- Exponential backoff
- Audit all attempts
Sample Implementation:
```java
@Service
public class PayrollService {
    public void generateExport(PayrollExportDto dto) { /* ... */ }
    public void retryFailedExport(Long exportId) { /* ... */ }
}
```

---

## E12: Notifications & Announcements

### Section: Spring Boot Architecture Overview
Description: Sends in-app, email, SMS notifications for shift changes, expiring certs, announcements.
Design Specification:
- Notification entity, delivery channels
Sample Implementation:
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private Long recipientId;
    private String type;
    private String message;
    private String channel; // IN_APP, EMAIL, SMS
    private String status; // SENT, FAILED
    // getters/setters
}
```

### Section: Controller Layer
Description: Endpoints for user preferences, quiet hours.
Design Specification:
- `/notifications/preferences`
Sample Implementation:
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping("/preferences")
    public ResponseEntity<NotificationPreferenceDto> updatePreferences(@RequestBody NotificationPreferenceDto dto) { /* ... */ }
}
```

### Section: Service Layer
Description: Rate limits, delivery status tracking.
Design Specification:
- Throttle notifications
- Track delivery
Sample Implementation:
```java
@Service
public class NotificationService {
    public void sendNotification(NotificationDto dto) { /* ... */ }
    public void trackDelivery(Long notificationId, String status) { /* ... */ }
}
```

---

## E13: Integration Layer HRIS/WMS APIs

### Section: Spring Boot Architecture Overview
Description: Exposes REST APIs for HRIS new hires/terminations, WMS location/department sync, JWT/OAuth2 security.
Design Specification:
- Integration endpoints, webhooks
Sample Implementation:
```java
@RestController
@RequestMapping("/integrations")
public class IntegrationController {
    @PostMapping("/hris/new-hire")
    public ResponseEntity<?> newHire(@RequestBody HrisNewHireDto dto) { /* ... */ }
    @PostMapping("/hris/termination")
    public ResponseEntity<?> termination(@RequestBody HrisTerminationDto dto) { /* ... */ }
}
```

### Section: Service Layer
Description: Idempotent webhooks, sync jobs.
Design Specification:
- Deduplicate events
- Scheduled sync
Sample Implementation:
```java
@Service
public class IntegrationService {
    public void processNewHire(HrisNewHireDto dto) { /* ... */ }
    public void syncWmsData() { /* ... */ }
}
```

### Section: Configuration
Description: JWT/OAuth2 security.
Design Specification:
- `application.yml` security settings
Sample Implementation:
```yaml
security:
  oauth2:
    resourceserver:
      jwt:
        issuer-uri: https://auth.example.com
```

---

## E14: Audit Trail & Compliance

### Section: Spring Boot Architecture Overview
Description: Centralized audit logging for PII, schedules, approvals, payroll. Immutable log table, export by date/user/entity.
Design Specification:
- AuditLog entity, immutable table
Sample Implementation:
```java
@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String actor;
    private String action;
    private String entity;
    private String before;
    private String after;
    private LocalDateTime timestamp;
    // getters/setters
}
```

### Section: Controller Layer
Description: Endpoints for audit log export.
Design Specification:
- `/audit/export`
Sample Implementation:
```java
@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/export")
    public ResponseEntity<Resource> exportAuditLog(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) { /* ... */ }
}
```

### Section: Service Layer
Description: Automated tests validate audit coverage.
Design Specification:
- Verify all sensitive actions logged
Sample Implementation:
```java
@Service
public class AuditService {
    public void logAction(String actor, String action, String entity, String before, String after) { /* ... */ }
}
```

---

## E15: Reporting & Analytics

### Section: Spring Boot Architecture Overview
Description: Operational reports for attendance, overtime, leave, certifications, safety KPIs. Export to CSV/PDF, role-based dashboards.
Design Specification:
- Report entity, metrics endpoints
Sample Implementation:
```java
@RestController
@RequestMapping("/reports")
public class ReportController {
    @GetMapping("/attendance")
    public ResponseEntity<AttendanceReportDto> getAttendanceReport(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) { /* ... */ }
    @GetMapping("/export/csv")
    public ResponseEntity<Resource> exportCsv(@RequestParam String reportType) { /* ... */ }
}
```

### Section: Service Layer
Description: Filtering by date, department, shift.
Design Specification:
- Dynamic filters
Sample Implementation:
```java
@Service
public class ReportService {
    public AttendanceReportDto generateAttendanceReport(LocalDate startDate, LocalDate endDate, String department) { /* ... */ }
}
```

---

## E16: Mobile Access PWA

### Section: Spring Boot Architecture Overview
Description: Responsive views for clock-in/out, schedules, leave requests, announcements. Installable PWA, offline queue.
Design Specification:
- PWA manifest, service worker
Sample Implementation:
```json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone",
  "icons": [...]
}
```

### Section: Controller Layer
Description: Endpoints optimized for mobile.
Design Specification:
- Lightweight responses
Sample Implementation:
```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @GetMapping("/schedule")
    public ResponseEntity<MobileScheduleDto> getSchedule(@RequestParam Long employeeId) { /* ... */ }
}
```

### Section: Service Layer
Description: Offline queue sync, conflict resolution.
Design Specification:
- Queue clock events
- Resolve conflicts on sync
Sample Implementation:
```java
@Service
public class MobileService {
    public void syncOfflineEvents(List<AttendanceEventDto> events) { /* ... */ }
}
```

---

## E17: Onboarding & Offboarding Workflow

### Section: Spring Boot Architecture Overview
Description: Automates account provisioning, initial schedule, training tasks, asset collection, access revocation.
Design Specification:
- OnboardingTask, OffboardingTask entities
Sample Implementation:
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String taskType;
    private String status; // PENDING, COMPLETED
    // getters/setters
}
```

### Section: Controller Layer
Description: Endpoints for onboarding/offboarding workflows.
Design Specification:
- `/onboarding/start`
- `/offboarding/start`
Sample Implementation:
```java
@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @PostMapping("/start")
    public ResponseEntity<?> startOnboarding(@RequestBody OnboardingDto dto) { /* ... */ }
}
```

### Section: Service Layer
Description: Task generation, HRIS sync trigger.
Design Specification:
- Generate tasks on new hire
- Revoke access on termination
Sample Implementation:
```java
@Service
public class OnboardingService {
    public void startOnboarding(Long employeeId) { /* ... */ }
    public void startOffboarding(Long employeeId) { /* ... */ }
}
```

---

## E18: Localization & Multi-Warehouse

### Section: Spring Boot Architecture Overview
Description: Supports multiple warehouses with distinct calendars, policies, UI localization, date/time formats.
Design Specification:
- Warehouse entity, locale settings
Sample Implementation:
```java
@Entity
public class Warehouse {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String locale;
    private String timezone;
    // getters/setters
}
```

### Section: Controller Layer
Description: Endpoints for warehouse management, locale toggle.
Design Specification:
- `/warehouses`
Sample Implementation:
```java
@RestController
@RequestMapping("/warehouses")
public class WarehouseController {
    @PostMapping
    public ResponseEntity<WarehouseDto> createWarehouse(@RequestBody WarehouseDto dto) { /* ... */ }
}
```

### Section: Service Layer
Description: Locale-aware formatting, multi-warehouse tests.
Design Specification:
- Format dates/times per locale
- Test multi-warehouse scenarios
Sample Implementation:
```java
@Service
public class LocalizationService {
    public String formatDate(LocalDate date, String locale) { /* ... */ }
}
```

---

## E19: Advanced Scheduling Optimization

### Section: Spring Boot Architecture Overview
Description: Constraint-based scheduling engine to minimize overtime, balance workload, respect preferences, what-if scenarios.
Design Specification:
- Scheduling engine, constraint solver
Sample Implementation:
```java
@Service
public class SchedulingOptimizationService {
    public Schedule generateOptimizedSchedule(ScheduleConstraintsDto constraints) { /* ... */ }
    public Schedule whatIfScenario(ScheduleConstraintsDto constraints) { /* ... */ }
}
```

### Section: Controller Layer
Description: Endpoints for schedule generation, what-if modeling.
Design Specification:
- `/scheduling/optimize`
Sample Implementation:
```java
@RestController
@RequestMapping("/scheduling")
public class SchedulingController {
    @PostMapping("/optimize")
    public ResponseEntity<ScheduleDto> optimizeSchedule(@RequestBody ScheduleConstraintsDto dto) { /* ... */ }
}
```

### Section: Service Layer
Description: Performance acceptable for 500 employees.
Design Specification:
- Optimize for large datasets
Sample Implementation:
```java
@Service
public class SchedulingOptimizationService {
    public Schedule generateOptimizedSchedule(ScheduleConstraintsDto constraints) {
        // Constraint solver logic
        return schedule;
    }
}
```

---

## E20: Self-Service Portal Enhancements

### Section: Spring Boot Architecture Overview
Description: Employee self-service for profile updates, document uploads, pay stubs, tax forms, benefits enrollment.
Design Specification:
- SelfServiceRequest entity, approval workflow
Sample Implementation:
```java
@Entity
public class SelfServiceRequest {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String requestType;
    private String status; // PENDING, APPROVED, REJECTED
    // getters/setters
}
```

### Section: Controller Layer
Description: Endpoints for self-service actions.
Design Specification:
- `/self-service/profile`
- `/self-service/documents`
Sample Implementation:
```java
@RestController
@RequestMapping("/self-service")
public class SelfServiceController {
    @PostMapping("/profile")
    public ResponseEntity<SelfServiceRequestDto> updateProfile(@RequestBody SelfServiceRequestDto dto) { /* ... */ }
    @PostMapping("/documents")
    public ResponseEntity<?> uploadDocument(@RequestParam MultipartFile file) { /* ... */ }
}
```

### Section: Service Layer
Description: Approval workflow, mobile-friendly UI.
Design Specification:
- Route updates for approval
- Responsive design
Sample Implementation:
```java
@Service
public class SelfServiceService {
    public void requestProfileUpdate(SelfServiceRequestDto dto) { /* ... */ }
    public void approveRequest(Long requestId) { /* ... */ }
}
```

---

## Conclusion

This comprehensive low-level technical design document covers all 95 user stories across 20 epics for the Warehouse Employee Management System. Each section provides detailed Spring Boot architecture, package structure, entity design, repository/service/controller layers, configuration, integration points, and sample code implementations. The design follows Spring Boot best practices and industry standards, ensuring scalability, maintainability, and compliance.