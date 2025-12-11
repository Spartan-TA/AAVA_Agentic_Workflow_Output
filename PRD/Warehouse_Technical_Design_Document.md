# Warehouse Employee Management System â Low-Level Technical Design Document

## Table of Contents
1. EPIC E01: Project Scaffolding (Stories 1-4)
2. EPIC E02: Employee Master Data (Stories 5-8)
3. EPIC E03: Role-Based Access Control (Stories 9-12)
4. EPIC E04: Time & Attendance (Stories 13-16)
5. EPIC E05: Shift & Schedule Management (Stories 17-20)
6. EPIC E06: Leave & Absence Management (Stories 21-24)
7. EPIC E07: Training & Certification (Stories 25-28)
8. EPIC E08: Safety Incidents & OSHA (Stories 29-32)
9. EPIC E09: Equipment & Asset Assignment (Stories 33-36)
10. EPIC E10: Performance Reviews (Stories 37-39)
11. EPIC E11: Payroll Export (Stories 40-41)
12. EPIC E12: Notifications (Stories 42-44)
13. EPIC E13: Integration Layer (Stories 45-47)
14. EPIC E14: Audit Trail (Stories 48-49)
15. EPIC E15: Reporting (Stories 50-51)
16. EPIC E16: Mobile Access (Stories 52-53)
17. EPIC E17: Onboarding/Offboarding (Stories 54-55)
18. EPIC E18-E20: Localization (Story 56)

---

## EPIC E01: Project Scaffolding

### Section: Spring Boot Project Initialization (Story 1)
Description: Initialize a Maven-based Spring Boot project with base packages and enable Actuator health endpoint.
Design Specification:
- Use Maven for dependency management.
- Base package: `com.wms`
- Modules: `employee`, `scheduling`, `attendance`, `safety`
- Enable Actuator for health monitoring.
Sample Implementation:
```java
// pom.xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

// Application.java
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}
```

### Section: Database Migration Configuration (Story 2)
Description: Configure Flyway/Liquibase for automated DB migrations.
Design Specification:
- Use Flyway or Liquibase for schema versioning.
- Place migration scripts in `src/main/resources/db/migration`
Sample Implementation:
```yaml
# application.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

### Section: Actuator Health Endpoint (Story 3)
Description: Expose `/actuator/health` for monitoring.
Design Specification:
- Actuator endpoint enabled by default.
Sample Implementation:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### Section: Build and Run Documentation (Story 4)
Description: Document build/run steps in README.
Design Specification:
- README includes Maven build, run, and health check instructions.
Sample Implementation:
```
# README.md
## Build
mvn clean install

## Run
mvn spring-boot:run

## Health Check
curl http://localhost:8080/actuator/health
```

---

## EPIC E02: Employee Master Data

### Section: Employee CRUD API (Story 5)
Description: Employee entity with CRUD operations, unique badgeId, and soft-delete.
Design Specification:
- Entity: `Employee`
- Fields: id, name, badgeId (unique), role, department, shiftGroup, hireDate, status, deleted
- Soft-delete: `deleted` boolean flag
Sample Implementation:
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badgeId"))
public class Employee {
    @Id @GeneratedValue private Long id;
    private String name;
    @Column(unique = true) private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted = false;
}
```

### Section: Pagination and Filtering (Story 6)
Description: Support pagination and filtering by department, role, status.
Design Specification:
- Repository: `EmployeeRepository extends JpaRepository<Employee, Long>`
- Custom queries for filtering.
Sample Implementation:
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Page<Employee> findByDepartmentAndRoleAndStatus(String department, String role, String status, Pageable pageable);
}
```

### Section: Unique Badge ID Enforcement (Story 7)
Description: Enforce badgeId uniqueness at API and DB level.
Design Specification:
- DB constraint (`@Column(unique = true)`)
- Service validation before create/update.
Sample Implementation:
```java
@Service
public class EmployeeService {
    public Employee createEmployee(EmployeeDto dto) {
        if (employeeRepository.existsByBadgeId(dto.getBadgeId())) {
            throw new DuplicateBadgeIdException();
        }
        // ... create logic
    }
}
```

### Section: Employee Soft-Delete (Story 8)
Description: Support soft-delete, preserve historical data.
Design Specification:
- Mark `deleted=true` instead of physical delete.
- Filter out deleted employees in queries.
Sample Implementation:
```java
public Page<Employee> getActiveEmployees(Pageable pageable) {
    return employeeRepository.findByDeletedFalse(pageable);
}
```

---

## EPIC E03: Role-Based Access Control

### Section: RBAC Implementation (Story 9)
Description: Implement roles: ADMIN, HR, SUPERVISOR, WORKER.
Design Specification:
- Use Spring Security.
- Roles stored in `User` entity.
Sample Implementation:
```java
@Entity
public class User {
    @Id @GeneratedValue private Long id;
    private String username;
    private String password;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles;
}
```
```java
@PreAuthorize("hasRole('ADMIN')")
public void adminOnlyOperation() { ... }
```

### Section: Row-Level Security (Story 10)
Description: Restrict employee data access by team.
Design Specification:
- Service methods filter by supervisor's team.
Sample Implementation:
```java
@PreAuthorize("hasRole('SUPERVISOR')")
public List<Employee> getTeamEmployees(Long supervisorId) {
    // Query employees where supervisor_id = supervisorId
}
```

### Section: API Key and OAuth2 Toggle (Story 11)
Description: Support API Key and OAuth2 authentication via config.
Design Specification:
- Conditional beans for API Key or OAuth2.
Sample Implementation:
```yaml
security:
  auth-type: oauth2 # or apikey
```
```java
@Configuration
public class SecurityConfig {
    @Value("${security.auth-type}")
    private String authType;
    // Conditional security beans
}
```

### Section: Security Test Coverage (Story 12)
Description: Ensure all roles/endpoints are covered by tests.
Design Specification:
- Use Spring Security Test.
Sample Implementation:
```java
@Test
@WithMockUser(roles = "ADMIN")
public void adminCanAccessEmployeeApi() { ... }
```

---

## EPIC E04: Time & Attendance

### Section: Clock In/Out Endpoint (Story 13)
Description: Endpoint for clock-in/out with timestamp and device info.
Design Specification:
- Entity: `AttendanceEvent`
- Fields: id, employeeId, type (IN/OUT), timestamp, deviceInfo
Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type; // IN or OUT
    private LocalDateTime timestamp;
    private String deviceInfo;
}
```
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockEventDto dto) { ... }
}
```

### Section: Hours Calculation (Story 14)
Description: Calculate hours worked per shift, including overnight.
Design Specification:
- Service logic to pair IN/OUT events and compute duration.
Sample Implementation:
```java
public Duration calculateShiftHours(List<AttendanceEvent> events) {
    // Pair IN/OUT, handle overnight logic
}
```

### Section: Missed Punch Correction Workflow (Story 15)
Description: Correction workflow with supervisor approval.
Design Specification:
- Entity: `AttendanceCorrectionRequest`
- Status: PENDING, APPROVED, REJECTED
Sample Implementation:
```java
@Entity
public class AttendanceCorrectionRequest {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private LocalDateTime originalTimestamp;
    private LocalDateTime correctedTimestamp;
    private String status;
    private Long supervisorId;
}
```

### Section: Attendance Report Export (Story 16)
Description: Export attendance report in CSV format.
Design Specification:
- Service to generate CSV from attendance data.
Sample Implementation:
```java
public byte[] exportAttendanceCsv(LocalDate start, LocalDate end) {
    // Generate CSV
}
```

---

## EPIC E05: Shift & Schedule Management

### Section: Shift Template CRUD (Story 17)
Description: CRUD for shift templates, rotations, overtime rules.
Design Specification:
- Entity: `ShiftTemplate`
- Fields: id, name, startTime, endTime, rotationType, overtimeRules
Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String rotationType;
    private String overtimeRules;
}
```

### Section: Conflict Detection (Story 18)
Description: Prevent double-booking of shifts.
Design Specification:
- Service checks for overlapping assignments.
Sample Implementation:
```java
public boolean hasConflict(Long employeeId, LocalDateTime start, LocalDateTime end) {
    // Query for overlapping shifts
}
```

### Section: Bulk Shift Assignment (Story 19)
Description: Assign shifts to multiple employees.
Design Specification:
- Service method for bulk assignment.
Sample Implementation:
```java
public void assignShifts(List<Long> employeeIds, Long shiftTemplateId, LocalDate date) { ... }
```

### Section: Personal Upcoming Shifts View (Story 20)
Description: Workers view their upcoming shifts.
Design Specification:
- REST endpoint returns future shifts for logged-in user.
Sample Implementation:
```java
@GetMapping("/my-shifts")
public List<ShiftAssignmentDto> getMyShifts(Authentication auth) { ... }
```

---

## EPIC E06: Leave & Absence Management

### Section: Leave Request Submission (Story 21)
Description: Submit leave requests with accrual check.
Design Specification:
- Entity: `LeaveRequest`
- Fields: id, employeeId, type, startDate, endDate, status, accrualChecked
Sample Implementation:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type; // PTO, sick, unpaid
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private boolean accrualChecked;
}
```

### Section: Supervisor Approval/Denial (Story 22)
Description: Supervisor approves/denies leave, updates balance/schedule.
Design Specification:
- Service updates leave status and employee balance.
Sample Implementation:
```java
public void approveLeave(Long requestId, Long supervisorId) { ... }
```

### Section: Automatic Leave Balance Updates (Story 23)
Description: Leave balances auto-update on approval/denial.
Design Specification:
- Service updates accruals.
Sample Implementation:
```java
public void updateLeaveBalance(Long employeeId, String type, int days) { ... }
```

### Section: Approved Leave Export for Payroll (Story 24)
Description: Export approved leaves for payroll.
Design Specification:
- Service generates export file.
Sample Implementation:
```java
public byte[] exportApprovedLeaves(LocalDate start, LocalDate end) { ... }
```

---

## EPIC E07: Training & Certification

### Section: Certification CRUD (Story 25)
Description: CRUD for certifications with document upload.
Design Specification:
- Entity: `Certification`
- Fields: id, employeeId, type, expiryDate, documentUrl
Sample Implementation:
```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type;
    private LocalDate expiryDate;
    private String documentUrl;
}
```
```java
@PostMapping("/certifications")
public ResponseEntity<?> uploadCertification(@RequestParam MultipartFile file, ...) { ... }
```

### Section: Certification Expiry Alerts (Story 26)
Description: Alerts 30 and 7 days before expiry.
Design Specification:
- Scheduled job checks for expiring certs.
Sample Implementation:
```java
@Scheduled(cron = "0 0 8 * * *")
public void sendExpiryAlerts() { ... }
```

### Section: Block Assignment for Expired Certifications (Story 27)
Description: Block task assignment if certification expired.
Design Specification:
- Service checks certification status before assignment.
Sample Implementation:
```java
public boolean canAssignTask(Long employeeId, String taskType) {
    // Check cert expiry
}
```

### Section: Certification Status Display (Story 28)
Description: Show certification status on employee profile.
Design Specification:
- REST endpoint returns cert status for employee.
Sample Implementation:
```java
@GetMapping("/employees/{id}/certifications")
public List<CertificationDto> getCertifications(@PathVariable Long id) { ... }
```

---

## EPIC E08: Safety Incidents & OSHA

### Section: Safety Incident Recording (Story 29)
Description: Record incidents with severity, location, employees.
Design Specification:
- Entity: `SafetyIncident`
- Fields: id, severity, location, description, involvedEmployees
Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private String severity;
    private String location;
    private String description;
    @ElementCollection private List<Long> involvedEmployeeIds;
}
```

### Section: Incident Investigation Workflow (Story 30)
Description: Workflow: Open â Investigating â Resolved.
Design Specification:
- Status field, service methods for transitions.
Sample Implementation:
```java
public void updateIncidentStatus(Long incidentId, String status) { ... }
```

### Section: OSHA Summary Export (Story 31)
Description: Export OSHA 300/300A summary.
Design Specification:
- Service generates OSHA-compliant export.
Sample Implementation:
```java
public byte[] exportOshaSummary(LocalDate start, LocalDate end) { ... }
```

### Section: Safety Metrics Dashboard (Story 32)
Description: Dashboard with KPIs.
Design Specification:
- REST endpoint returns metrics.
Sample Implementation:
```java
@GetMapping("/safety/metrics")
public SafetyMetricsDto getMetrics() { ... }
```

---

## EPIC E09: Equipment & Asset Assignment

### Section: Asset Registry CRUD (Story 33)
Description: CRUD for assets with condition tracking.
Design Specification:
- Entity: `Asset`
- Fields: id, type, condition, assignedTo, lastCheckIn, lastCheckOut
Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String type;
    private String condition;
    private Long assignedTo;
    private LocalDateTime lastCheckIn;
    private LocalDateTime lastCheckOut;
}
```

### Section: Asset Check-In/Out Endpoint (Story 34)
Description: Endpoint for asset check-in/out.
Design Specification:
- REST endpoints for check-in/out.
Sample Implementation:
```java
@PostMapping("/assets/check-in")
public ResponseEntity<?> checkIn(@RequestBody AssetCheckDto dto) { ... }
```

### Section: Asset Condition Reporting (Story 35)
Description: Report asset condition during check-in.
Design Specification:
- Update asset condition on check-in.
Sample Implementation:
```java
public void reportCondition(Long assetId, String condition) { ... }
```

### Section: Overdue Asset Return Notifications (Story 36)
Description: Notify on overdue asset returns.
Design Specification:
- Scheduled job checks for overdue assets.
Sample Implementation:
```java
@Scheduled(cron = "0 0 9 * * *")
public void notifyOverdueAssets() { ... }
```

---

## EPIC E10: Performance Reviews

### Section: Review Cycle Creation (Story 37)
Description: Create review cycles (quarterly/annual).
Design Specification:
- Entity: `PerformanceReviewCycle`
- Fields: id, type, startDate, endDate
Sample Implementation:
```java
@Entity
public class PerformanceReviewCycle {
    @Id @GeneratedValue private Long id;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
}
```

### Section: Goal Assignment (Story 38)
Description: Assign goals to employees.
Design Specification:
- Entity: `PerformanceGoal`
- Fields: id, employeeId, cycleId, description, status
Sample Implementation:
```java
@Entity
public class PerformanceGoal {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private Long cycleId;
    private String description;
    private String status;
}
```

### Section: Review Submission Workflow (Story 39)
Description: Submission and acknowledgement workflow.
Design Specification:
- Status: SUBMITTED, ACKNOWLEDGED
Sample Implementation:
```java
public void submitReview(Long reviewId, ReviewDto dto) { ... }
public void acknowledgeReview(Long reviewId, Long employeeId) { ... }
```

---

## EPIC E11: Payroll Export

### Section: Payroll Export File Generation (Story 40)
Description: Generate payroll files from attendance/leave data.
Design Specification:
- Service aggregates attendance/leave, formats for provider.
Sample Implementation:
```java
public byte[] generatePayrollExport(LocalDate periodStart, LocalDate periodEnd)  { ... }
```

### Section: Payroll Export Delivery & Retry (Story 41)
Description: Retry logic for failed exports.
Design Specification:
- Scheduled job with exponential backoff.
Sample Implementation:
```java
@Scheduled(fixedDelay = 60000)
public void retryFailedExports() { ... }
```

---

## EPIC E12: Notifications

### Section: Shift Change Notification (Story 42)
Description: Notify workers of shift changes.
Design Specification:
- Event listener on shift update.
Sample Implementation:
```java
@EventListener
public void onShiftChange(ShiftChangeEvent event) {
    notificationService.send(event.getEmployeeId(), "Shift changed");
}
```

### Section: Expiring Certification Notification (Story 43)
Description: Notify workers of expiring certifications.
Design Specification:
- Scheduled job sends notifications.
Sample Implementation:
```java
@Scheduled(cron = "0 0 8 * * *")
public void notifyExpiringCerts() { ... }
```

### Section: Announcement Dashboard Display (Story 44)
Description: Display announcements on dashboard.
Design Specification:
- Entity: `Announcement`
- REST endpoint returns active announcements.
Sample Implementation:
```java
@GetMapping("/announcements")
public List<AnnouncementDto> getActiveAnnouncements() { ... }
```

---

## EPIC E13: Integration Layer

### Section: HRIS Employee Sync API (Story 45)
Description: Sync employees from HRIS (JWT-secured, idempotent).
Design Specification:
- REST endpoint for HRIS sync.
Sample Implementation:
```java
@PostMapping("/integrations/hris/sync")
@PreAuthorize("hasRole('INTEGRATION')")
public ResponseEntity<?> syncEmployees(@RequestBody List<EmployeeDto> employees) { ... }
```

### Section: WMS Department/Location Link (Story 46)
Description: Link departments/locations to WMS.
Design Specification:
- REST endpoint for WMS sync.
Sample Implementation:
```java
@PostMapping("/integrations/wms/sync")
public ResponseEntity<?> syncDepartments(@RequestBody List<DepartmentDto> departments) { ... }
```

### Section: SSO Integration with IDP (Story 47)
Description: SSO via OAuth2/SAML.
Design Specification:
- Spring Security OAuth2 client.
Sample Implementation:
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          idp:
            client-id: ...
            client-secret: ...
```

---

## EPIC E14: Audit Trail

### Section: Centralized Audit Logging (Story 48)
Description: Immutable audit logs for sensitive changes.
Design Specification:
- Entity: `AuditLog`
- Fields: id, actor, timestamp, entity, action, beforeValue, afterValue
Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    private String action;
    private String beforeValue;
    private String afterValue;
}
```

### Section: Audit Log Export (Story 49)
Description: Export audit logs by date/user/entity.
Design Specification:
- Service generates CSV export.
Sample Implementation:
```java
public byte[] exportAuditLogs(LocalDate start, LocalDate end, String entity) { ... }
```

---

## EPIC E15: Reporting

### Section: Attendance & Overtime Report (Story 50)
Description: Generate attendance/overtime reports (CSV/PDF).
Design Specification:
- Service aggregates attendance data.
Sample Implementation:
```java
public byte[] generateAttendanceReport(LocalDate start, LocalDate end, String format) { ... }
```

### Section: Certification Status Dashboard (Story 51)
Description: Dashboard for certification status.
Design Specification:
- REST endpoint returns cert status for all employees.
Sample Implementation:
```java
@GetMapping("/reports/certifications")
public List<CertificationStatusDto> getCertificationStatus() { ... }
```

---

## EPIC E16: Mobile Access

### Section: Mobile Clock-In/Out (Story 52)
Description: Mobile clock-in/out with offline queue.
Design Specification:
- PWA with offline support.
Sample Implementation:
```javascript
// Service worker for offline queue
self.addEventListener('sync', event => {
  if (event.tag === 'sync-attendance') {
    event.waitUntil(syncAttendance());
  }
});
```

### Section: Mobile Schedule View (Story 53)
Description: Mobile schedule view (calendar/list).
Design Specification:
- Responsive UI for schedule display.
Sample Implementation:
```html
<!-- Mobile-friendly schedule view -->
<div class="schedule-view">
  <div *ngFor="let shift of shifts">{{ shift.date }} - {{ shift.time }}</div>
</div>
```

---

## EPIC E17: Onboarding/Offboarding

### Section: Onboarding Task Automation (Story 54)
Description: Auto-generate onboarding tasks for new hires.
Design Specification:
- Event listener on new employee creation.
Sample Implementation:
```java
@EventListener
public void onNewHire(EmployeeCreatedEvent event) {
    taskService.createOnboardingTasks(event.getEmployeeId());
}
```

### Section: Offboarding Asset Collection (Story 55)
Description: Auto-generate asset collection tasks on termination.
Design Specification:
- Event listener on employee termination.
Sample Implementation:
```java
@EventListener
public void onTermination(EmployeeTerminatedEvent event) {
    taskService.createAssetCollectionTasks(event.getEmployeeId());
}
```

---

## EPIC E18-E20: Localization

### Section: Localization of Notifications (Story 56)
Description: Localize notifications (English/Spanish).
Design Specification:
- Use Spring MessageSource.
Sample Implementation:
```java
@Autowired
private MessageSource messageSource;

public String getLocalizedMessage(String key, Locale locale) {
    return messageSource.getMessage(key, null, locale);
}
```
```properties
# messages_en.properties
shift.change=Your shift has been changed

# messages_es.properties
shift.change=Tu turno ha sido cambiado
```

---

## Conclusion
This document provides comprehensive low-level technical designs for all 56 user stories across 20 epics, following Spring Boot best practices and industry standards. Each section includes entity design, repository/service/controller specifications, configuration details, and code snippets to guide implementation.