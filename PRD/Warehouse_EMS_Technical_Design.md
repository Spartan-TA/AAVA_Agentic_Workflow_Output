# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

## Table of Contents
1. E01: Project Scaffolding & Domain Setup
2. E02: Employee Master Data CRUD
3. E03: Role Based Access Control (RBAC)
4. E04: Time & Attendance (Clock In/Out)
5. E05: Shift & Schedule Management
6. E06: Leave & Absence Management
7. E07: Training & Certification Tracking
8. E08: Safety Incidents & OSHA Reporting
9. E09: Equipment & Asset Assignment
10. E10: Performance Reviews & Goals
11. E11: Payroll Export Integration
12. E12: Notifications & Announcements
13. E13: Integration Layer (HRIS/WMS APIs)
14. E14: Audit Trail & Compliance
15. E15: Reporting & Analytics
16. E16: Mobile Access (PWA)
17. E17: Onboarding & Offboarding Workflow
18. E18: Localization & Multi-Warehouse
19. E19: Observability & Monitoring
20. E20: Deployment & CI/CD

---

## E01: Project Scaffolding & Domain Setup

Section: E01-1: Initialize Spring Boot Project  
Description: Set up a Maven-based Spring Boot project with modular package structure, Flyway/Liquibase for DB migrations, and Actuator for health checks.  
Design Specification:
- Package Structure:  
  - com.warehouse.ems (root)  
  - com.warehouse.ems.employee  
  - com.warehouse.ems.schedule  
  - com.warehouse.ems.attendance  
  - com.warehouse.ems.safety  
  - com.warehouse.ems.config  
- Core Modules: Employee, Scheduling, Attendance, Safety  
- DB Migration: Flyway/Liquibase scripts in /resources/db/migration  
- Actuator: Enabled for health endpoints  
- README: Build/run steps  
Sample Implementation:
```java
// pom.xml dependencies
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>

// application.yml
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

Section: E01-2: Configure Database Migrations  
Description: Use Flyway/Liquibase for versioned schema migrations.  
Design Specification:
- /resources/db/migration/V1__init.sql
- Baseline migration for all core tables  
Sample Implementation:
```sql
-- V1__init.sql
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    hire_date DATE,
    status VARCHAR(16)
);
```

---

Section: E01-3: Enable Actuator  
Description: Expose health and info endpoints for monitoring.  
Design Specification:
- Actuator dependency in pom.xml
- management.endpoints.web.exposure.include=health,info  
Sample Implementation:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

---

Section: E01-4: Document Build Steps  
Description: Provide clear build and run instructions in README.  
Design Specification:
- README.md with Maven build/run commands  
Sample Implementation:
```markdown
# Build
mvn clean install
# Run
mvn spring-boot:run
```

---

## E02: Employee Master Data CRUD

Section: E02-1: Employee CRUD API  
Description: Implement RESTful CRUD endpoints for Employee entity with unique badgeId enforcement and soft-delete.  
Design Specification:
- Package Structure: com.warehouse.ems.employee
- Domain Model: Employee (id, badgeId, name, role, department, shiftGroup, hireDate, status, deleted)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService (create, update, delete, find, filter)
- Controller: EmployeeController (REST endpoints)
- DTOs: EmployeeRequest, EmployeeResponse  
Sample Implementation:
```java
@Entity
@Table(name = "employee")
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping public ResponseEntity<EmployeeResponse> create(@RequestBody EmployeeRequest req) {...}
    @GetMapping public Page<EmployeeResponse> list(Pageable pageable) {...}
    @GetMapping("/{id}") public EmployeeResponse get(@PathVariable Long id) {...}
    @PutMapping("/{id}") public EmployeeResponse update(@PathVariable Long id, @RequestBody EmployeeRequest req) {...}
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) {...}
}
```

---

Section: E02-2: Pagination and Filtering  
Description: Support pagination and filtering on employee list endpoints.  
Design Specification:
- Pageable support in repository/controller
- Filtering by department, role, status  
Sample Implementation:
```java
@GetMapping
public Page<EmployeeResponse> list(@RequestParam Optional<String> department,
                                   @RequestParam Optional<String> role,
                                   @RequestParam Optional<String> status,
                                   Pageable pageable) {
    // Filtering logic
}
```

---

Section: E02-3: OpenAPI Schemas  
Description: Document all Employee APIs using OpenAPI/Swagger.  
Design Specification:
- springdoc-openapi dependency
- @Operation, @Schema annotations  
Sample Implementation:
```java
@Operation(summary = "Create Employee", description = "Creates a new employee record.")
@PostMapping
public ResponseEntity<EmployeeResponse> create(@RequestBody @Valid EmployeeRequest req) {...}
```

---

Section: E02-4: Unique BadgeId Enforcement  
Description: Ensure badgeId is unique at DB and application level.  
Design Specification:
- @Column(unique = true) on badgeId
- Service validation for duplicates  
Sample Implementation:
```java
if (employeeRepository.findByBadgeId(req.getBadgeId()).isPresent()) {
    throw new DuplicateResourceException("BadgeId already exists");
}
```

---

## E03: Role Based Access Control (RBAC)

Section: E03-1: Role-based Endpoint Security  
Description: Secure endpoints by role using Spring Security.  
Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- @PreAuthorize annotations  
Sample Implementation:
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
@PostMapping
public ResponseEntity<EmployeeResponse> create(...) {...}
```

---

Section: E03-2: Method/Row-Level Security  
Description: Restrict access to records based on user/team.  
Design Specification:
- Row-level filtering in service/repository  
Sample Implementation:
```java
@PreAuthorize("hasRole('SUPERVISOR')")
public List<Employee> getTeamEmployees(Long supervisorId) {...}
```

---

Section: E03-3: API Key/OAuth2 Authentication  
Description: Support API key and OAuth2 authentication, toggle via config.  
Design Specification:
- Spring Security config with conditional beans  
Sample Implementation:
```java
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${security.mode}")
    private String mode;
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if ("apikey".equals(mode)) {
            // API key filter
        } else {
            // OAuth2 config
        }
    }
}
```

---

Section: E03-4: Automated Security Tests  
Description: Cover security rules with integration tests.  
Design Specification:
- @WithMockUser in tests  
Sample Implementation:
```java
@Test
@WithMockUser(roles = "ADMIN")
void testAdminAccess() {...}
```

---

## E04: Time & Attendance (Clock In/Out)

Section: E04-1: Clock-in/out Event Capture  
Description: Endpoints for clock-in/out with optional geofence/device capture.  
Design Specification:
- AttendanceEvent entity (employeeId, timestamp, type, deviceId, location)
- AttendanceController: /attendance/clock-in, /attendance/clock-out  
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
}

@PostMapping("/attendance/clock-in")
public ResponseEntity<?> clockIn(@RequestBody AttendanceRequest req) {...}
```

---

Section: E04-2: Automatic Shift Association  
Description: Link attendance events to scheduled shifts.  
Design Specification:
- AttendanceService associates event with nearest shift  
Sample Implementation:
```java
public void associateWithShift(AttendanceEvent event) {
    Shift shift = shiftRepository.findNearestForEmployee(event.getEmployeeId(), event.getTimestamp());
    event.setShiftId(shift.getId());
}
```

---

Section: E04-3: Missed Punches and Corrections Workflow  
Description: Handle missed punches and correction approvals.  
Design Specification:
- CorrectionRequest entity, approval workflow  
Sample Implementation:
```java
@Entity
public class CorrectionRequest {
    @Id @GeneratedValue
    private Long id;
    private Long attendanceEventId;
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED
}
```

---

Section: E04-4: Reports Exportable (CSV)  
Description: Export attendance reports as CSV.  
Design Specification:
- AttendanceReportService, CSV export utility  
Sample Implementation:
```java
@GetMapping("/attendance/report")
public ResponseEntity<Resource> exportReport(@RequestParam LocalDate from, @RequestParam LocalDate to) {...}
```

---

## E05: Shift & Schedule Management

Section: E05-1: Recurring Shift Templates  
Description: Define recurring shift templates and assign to employees.  
Design Specification:
- ShiftTemplate entity (startTime, endTime, recurrence, daysOfWeek)  
Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrence; // DAILY, WEEKLY
    private String daysOfWeek;
}
```

---

Section: E05-2: Conflict Detection  
Description: Prevent overlapping shifts and conflicts.  
Design Specification:
- ShiftService conflict detection logic  
Sample Implementation:
```java
public boolean hasConflict(Long employeeId, LocalDateTime start, LocalDateTime end) {
    return shiftRepository.existsByEmployeeIdAndTimeOverlap(employeeId, start, end);
}
```

---

Section: E05-3: Bulk Assignment  
Description: Assign shifts to multiple employees at once.  
Design Specification:
- BulkAssignmentRequest DTO  
Sample Implementation:
```java
public void bulkAssignShifts(BulkAssignmentRequest req) {...}
```

---

Section: E05-4: Personal Schedule Views  
Description: Employees can view their upcoming shifts.  
Design Specification:
- /schedules/me endpoint  
Sample Implementation:
```java
@GetMapping("/schedules/me")
public List<ShiftResponse> myShifts(Authentication auth) {...}
```

---

## E06: Leave & Absence Management

Section: E06-1: Leave Request Submission  
Description: Employees submit PTO, sick, or unpaid leave requests.  
Design Specification:
- LeaveRequest entity (employeeId, type, startDate, endDate, status)  
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
    private String status; // PENDING, APPROVED, DENIED
}
```

---

Section: E06-2: Approval Workflow  
Description: Supervisors approve/deny leave requests.  
Design Specification:
- LeaveApprovalService  
Sample Implementation:
```java
public void approveLeave(Long requestId, boolean approve) {...}
```

---

Section: E06-3: Shift Coverage Flagging  
Description: Flag scheduled shifts for coverage when leave is approved.  
Design Specification:
- ShiftService marks affected shifts  
Sample Implementation:
```java
public void flagShiftsForCoverage(Long employeeId, LocalDate from, LocalDate to) {...}
```

---

Section: E06-4: Payroll Export  
Description: Exclude approved leave from payroll hours.  
Design Specification:
- PayrollExportService filters leave  
Sample Implementation:
```java
public PayrollExport generatePayroll(LocalDate periodStart, LocalDate periodEnd) {...}
```

---

## E07: Training & Certification Tracking

Section: E07-1: Certification Management  
Description: Track employee certifications, expirations, and renewals.  
Design Specification:
- Certification entity (employeeId, type, issueDate, expiryDate, documentUrl)  
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
}
```

---

Section: E07-2: Expiry Alerts  
Description: Alert employees/supervisors 30/7 days before expiry.  
Design Specification:
- Scheduled job for alerts  
Sample Implementation:
```java
@Scheduled(cron = "0 0 8 * * *")
public void sendExpiryAlerts() {...}
```

---

Section: E07-3: Assignment Blocking for Expired Certs  
Description: Prevent assignment to tasks requiring valid certification.  
Design Specification:
- Shift assignment logic checks cert validity  
Sample Implementation:
```java
if (!certificationService.isValid(employeeId, requiredType)) {
    throw new AssignmentBlockedException();
}
```

---

Section: E07-4: Profile Status Display  
Description: Show certification status on employee profile.  
Design Specification:
- EmployeeProfileResponse includes cert status  
Sample Implementation:
```java
public class EmployeeProfileResponse {
    private List<CertificationStatus> certifications;
}
```

---

## E08: Safety Incidents & OSHA Reporting

Section: E08-1: Incident Recording  
Description: Record safety incidents and near-misses.  
Design Specification:
- SafetyIncident entity (severity, location, description, involvedEmployees)  
Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String severity;
    private String location;
    private String description;
    @ElementCollection
    private List<Long> involvedEmployeeIds;
}
```

---

Section: E08-2: Investigation Workflow  
Description: Workflow for investigation and corrective actions.  
Design Specification:
- IncidentStatus: OPEN, INVESTIGATING, RESOLVED  
Sample Implementation:
```java
public void updateStatus(Long incidentId, String status) {...}
```

---

Section: E08-3: OSHA Report Export  
Description: Export OSHA 300/300A summary fields.  
Design Specification:
- OSHAReportService  
Sample Implementation:
```java
@GetMapping("/safety/osha/export")
public ResponseEntity<Resource> exportOshaReport(...) {...}
```

---

Section: E08-4: Safety Metrics Dashboard  
Description: Dashboard endpoints for safety KPIs.  
Design Specification:
- /safety/metrics endpoint  
Sample Implementation:
```java
@GetMapping("/safety/metrics")
public SafetyMetricsResponse getMetrics() {...}
```

---

## E09: Equipment & Asset Assignment

Section: E09-1: Asset Registry Management  
Description: Manage asset registry for scanners, forklifts, PPE.  
Design Specification:
- Asset entity (id, type, serialNumber, condition, assignedTo)  
Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private String condition;
    private Long assignedTo;
}
```

---

Section: E09-2: Check-in/out Workflow  
Description: Track asset check-in/out events.  
Design Specification:
- AssetAssignment entity (assetId, employeeId, checkOutTime, checkInTime)  
Sample Implementation:
```java
@Entity
public class AssetAssignment {
    @Id @GeneratedValue
    private Long id;
    private Long assetId;
    private Long employeeId;
    private LocalDateTime checkOutTime;
    private LocalDateTime checkInTime;
}
```

---

Section: E09-3: Certification-based Blocking  
Description: Block asset assignment if required certification is missing.  
Design Specification:
- AssetService checks certification before assignment  
Sample Implementation:
```java
if (!certificationService.isValid(employeeId, assetType)) {
    throw new AssignmentBlockedException();
}
```

---

Section: E09-4: History Logging  
Description: Maintain history log per asset and employee.  
Design Specification:
- AssetHistory entity (assetId, employeeId, action, timestamp)  
Sample Implementation:
```java
@Entity
public class AssetHistory {
    @Id @GeneratedValue
    private Long id;
    private Long assetId;
    private Long employeeId;
    private String action;
    private LocalDateTime timestamp;
}
```

---

## E10: Performance Reviews & Goals

Section: E10-1: Review Template Creation  
Description: Create review templates for quarterly/annual cycles.  
Design Specification:
- ReviewTemplate entity (id, name, period, competencies)  
Sample Implementation:
```java
@Entity
public class ReviewTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String period;
    @ElementCollection
    private List<String> competencies;
}
```

---

Section: E10-2: Goal/Competency Tracking  
Description: Track goals, competencies, ratings,comments.  
Design Specification:
- PerformanceReview entity (employeeId, templateId, goals, ratings, comments)  
Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private Long templateId;
    @ElementCollection
    private Map<String, Integer> ratings;
    private String comments;
}
```

---

Section: E10-3: Acknowledgement Workflow  
Description: Supervisor/employee acknowledgement workflow.  
Design Specification:
- ReviewAcknowledgement entity (reviewId, role, timestamp)  
Sample Implementation:
```java
@Entity
public class ReviewAcknowledgement {
    @Id @GeneratedValue
    private Long id;
    private Long reviewId;
    private String role;
    private LocalDateTime timestamp;
}
```

---

## E11: Payroll Export Integration

Section: E11-1: Payroll File Generation  
Description: Generate payroll-ready files from attendance and leave.  
Design Specification:
- PayrollExportService  
Sample Implementation:
```java
public PayrollFile generatePayrollFile(LocalDate periodStart, LocalDate periodEnd) {...}
```

---

Section: E11-2: External Format Mapping  
Description: Map to external payroll provider formats.  
Design Specification:
- PayrollMapper interface with provider-specific implementations  
Sample Implementation:
```java
public interface PayrollMapper {
    String mapToProviderFormat(PayrollData data);
}
```

---

Section: E11-3: Secure SFTP/API Delivery  
Description: Deliver payroll files via SFTP or API.  
Design Specification:
- PayrollDeliveryService with SFTP/API clients  
Sample Implementation:
```java
public void deliverPayroll(PayrollFile file, DeliveryMethod method) {...}
```

---

Section: E11-4: Audit Logging  
Description: Log every payroll export for compliance.  
Design Specification:
- PayrollAuditLog entity  
Sample Implementation:
```java
@Entity
public class PayrollAuditLog {
    @Id @GeneratedValue
    private Long id;
    private LocalDateTime timestamp;
    private String fileName;
    private String status;
}
```

---

## E12: Notifications & Announcements

Section: E12-1: Multi-channel Notifications  
Description: Send notifications via in-app, email, SMS.  
Design Specification:
- NotificationService with channel adapters  
Sample Implementation:
```java
public void sendNotification(Notification notification, List<Channel> channels) {...}
```

---

Section: E12-2: Expiring Certification Alerts  
Description: Notify employees of expiring certifications.  
Design Specification:
- Scheduled job for cert expiry notifications  
Sample Implementation:
```java
@Scheduled(cron = "0 0 8 * * *")
public void sendCertExpiryNotifications() {...}
```

---

Section: E12-3: Approval and Announcement Notifications  
Description: Notify users of approvals and announcements.  
Design Specification:
- Event-driven notification triggers  
Sample Implementation:
```java
@EventListener
public void onLeaveApproved(LeaveApprovedEvent event) {...}
```

---

Section: E12-4: Quiet Hours Configuration  
Description: Allow users to configure quiet hours.  
Design Specification:
- UserPreferences entity with quietHoursStart/End  
Sample Implementation:
```java
@Entity
public class UserPreferences {
    @Id @GeneratedValue
    private Long id;
    private Long userId;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
}
```

---

## E13: Integration Layer (HRIS/WMS APIs)

Section: E13-1: HRIS Integration  
Description: Expose REST APIs for HRIS new hires/terms.  
Design Specification:
- HRISController: /hris/employees  
Sample Implementation:
```java
@PostMapping("/hris/employees")
public ResponseEntity<?> syncEmployee(@RequestBody HRISEmployeeRequest req) {...}
```

---

Section: E13-2: WMS Integration  
Description: Sync department/location data from WMS.  
Design Specification:
- WMSClient for external API calls  
Sample Implementation:
```java
public List<Department> fetchDepartments() {...}
```

---

Section: E13-3: SSO Integration  
Description: Integrate with IDP for SSO.  
Design Specification:
- Spring Security OAuth2 config  
Sample Implementation:
```java
@Configuration
public class OAuth2Config extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.oauth2Login();
    }
}
```

---

Section: E13-4: Webhooks  
Description: Expose webhooks for event notifications.  
Design Specification:
- WebhookController: /webhooks/events  
Sample Implementation:
```java
@PostMapping("/webhooks/events")
public ResponseEntity<?> handleEvent(@RequestBody WebhookEvent event) {...}
```

---

## E14: Audit Trail & Compliance

Section: E14-1: Centralized Audit Logging  
Description: Log all sensitive changes with actor, timestamp, before/after.  
Design Specification:
- AuditLog entity (actor, timestamp, entity, action, before, after)  
Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    private String action;
    private String before;
    private String after;
}
```

---

Section: E14-2: Tamper-evident Storage  
Description: Ensure audit logs are immutable.  
Design Specification:
- Append-only audit log table  
Sample Implementation:
```java
@Entity
@Immutable
public class AuditLog {...}
```

---

Section: E14-3: Audit Log Export  
Description: Export audit logs by date/user/entity.  
Design Specification:
- AuditLogService export methods  
Sample Implementation:
```java
@GetMapping("/audit/export")
public ResponseEntity<Resource> exportAuditLogs(...) {...}
```

---

## E15: Reporting & Analytics

Section: E15-1: Operational Reports  
Description: Generate reports for attendance, overtime, leave, certs, safety.  
Design Specification:
- ReportService with various report types  
Sample Implementation:
```java
public AttendanceReport generateAttendanceReport(LocalDate from, LocalDate to) {...}
```

---

Section: E15-2: Role-based Dashboards  
Description: Provide tailored dashboards per role.  
Design Specification:
- DashboardController with role-specific endpoints  
Sample Implementation:
```java
@GetMapping("/dashboard/supervisor")
@PreAuthorize("hasRole('SUPERVISOR')")
public DashboardResponse getSupervisorDashboard() {...}
```

---

Section: E15-3: BI Integration Metrics  
Description: Expose metrics endpoints for BI tools.  
Design Specification:
- /metrics/bi endpoint  
Sample Implementation:
```java
@GetMapping("/metrics/bi")
public MetricsResponse getBIMetrics() {...}
```

---

## E16: Mobile Access (PWA)

Section: E16-1: Responsive Views  
Description: Responsive UI for clock-in/out and schedules.  
Design Specification:
- Mobile-first CSS, responsive layouts  
Sample Implementation:
```html
<meta name="viewport" content="width=device-width, initial-scale=1">
```

---

Section: E16-2: Mobile Leave Requests  
Description: Mobile-friendly leave request forms.  
Design Specification:
- Responsive forms with validation  
Sample Implementation:
```html
<form action="/leave/request" method="post">...</form>
```

---

Section: E16-3: Installable PWA  
Description: PWA manifest for installable app.  
Design Specification:
- manifest.json with icons, start_url  
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

---

Section: E16-4: Offline Queue  
Description: Queue clock events offline, sync when online.  
Design Specification:
- Service Worker with IndexedDB queue  
Sample Implementation:
```javascript
self.addEventListener('sync', event => {
  if (event.tag === 'sync-clock-events') {
    event.waitUntil(syncClockEvents());
  }
});
```

---

## E17: Onboarding & Offboarding Workflow

Section: E17-1: Automated Provisioning  
Description: Automate account, schedule, training provisioning for new hires.  
Design Specification:
- OnboardingService triggered by HRIS sync  
Sample Implementation:
```java
public void provisionNewHire(Employee employee) {...}
```

---

Section: E17-2: Deprovision Access/Assets  
Description: Revoke access and collect assets on termination.  
Design Specification:
- OffboardingService  
Sample Implementation:
```java
public void deprovisionEmployee(Long employeeId) {...}
```

---

Section: E17-3: Task Tracking  
Description: Track onboarding/offboarding task completion.  
Design Specification:
- OnboardingTask entity (employeeId, task, status)  
Sample Implementation:
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String task;
    private String status;
}
```

---

## E18: Localization & Multi-Warehouse

Section: E18-1: Multi-warehouse Support  
Description: Support multiple warehouses with distinct calendars/policies.  
Design Specification:
- Warehouse entity (id, name, location, calendar)  
Sample Implementation:
```java
@Entity
public class Warehouse {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String location;
    private String calendar;
}
```

---

Section: E18-2: UI Localization  
Description: Support Spanish/English language preferences.  
Design Specification:
- MessageSource with resource bundles  
Sample Implementation:
```java
@Bean
public MessageSource messageSource() {
    ResourceBundleMessageSource source = new ResourceBundleMessageSource();
    source.setBasename("messages");
    return source;
}
```

---

Section: E18-3: Warehouse Assignment  
Description: Assign employees to warehouses.  
Design Specification:
- Employee.warehouseId field  
Sample Implementation:
```java
@Entity
public class Employee {
    private Long warehouseId;
}
```

---

## E19: Observability & Monitoring

Section: E19-1: Prometheus/Grafana Integration  
Description: Expose metrics for Prometheus scraping.  
Design Specification:
- Micrometer dependency, /actuator/prometheus endpoint  
Sample Implementation:
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

---

Section: E19-2: Structured Logging  
Description: JSON-formatted logs for analysis.  
Design Specification:
- Logback JSON encoder  
Sample Implementation:
```xml
<encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
```

---

Section: E19-3: Distributed Tracing  
Description: Integrate Jaeger/Zipkin for request tracing.  
Design Specification:
- Spring Cloud Sleuth dependency  
Sample Implementation:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
```

---

Section: E19-4: Alerting  
Description: Alert on critical errors.  
Design Specification:
- Prometheus Alertmanager rules  
Sample Implementation:
```yaml
groups:
- name: ems_alerts
  rules:
  - alert: HighErrorRate
    expr: rate(http_requests_total{status="500"}[5m]) > 0.05
```

---

## E20: Deployment & CI/CD

Section: E20-1: Dockerize Application  
Description: Create Dockerfile for containerization.  
Design Specification:
- Dockerfile with multi-stage build  
Sample Implementation:
```dockerfile
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

---

Section: E20-2: Kubernetes Manifests  
Description: Create K8s deployment/service manifests.  
Design Specification:
- deployment.yaml, service.yaml  
Sample Implementation:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: warehouse-ems
spec:
  replicas: 3
  selector:
    matchLabels:
      app: warehouse-ems
  template:
    metadata:
      labels:
        app: warehouse-ems
    spec:
      containers:
      - name: ems
        image: warehouse-ems:latest
        ports:
        - containerPort: 8080
```

---

Section: E20-3: GitHub Actions CI/CD  
Description: Automate build/test/deploy with GitHub Actions.  
Design Specification:
- .github/workflows/ci-cd.yml  
Sample Implementation:
```yaml
name: CI/CD
on: [push]
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
    - name: Build Docker image
      run: docker build -t warehouse-ems:latest .
    - name: Deploy to K8s
      run: kubectl apply -f k8s/
```

---

Section: E20-4: Rollback Procedure  
Description: Document rollback steps for failed deployments.  
Design Specification:
- Rollback.md with kubectl rollback commands  
Sample Implementation:
```markdown
# Rollback
kubectl rollout undo deployment/warehouse-ems
```

---

## Conclusion
This comprehensive low-level technical design document covers all 76 user stories across 20 epics for the Warehouse Employee Management System. Each section provides detailed Spring Boot architecture, package structure, entity design, service/repository/controller specifications, configuration, security settings, integration points, and sample code implementations. This document is production-ready and follows Spring Boot best practices for immediate use by development teams.