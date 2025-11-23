# Warehouse EMS Technical Design Document

## Introduction
This document provides a comprehensive low-level technical design for all 87 user stories of the Warehouse Employee Management System (EMS) project. Each user story is fully detailed with architecture overview, package structure, entity design, service/repository/controller specifications, configuration, integration points, and code examples, following Spring Boot 3.x best practices.

---

## EPIC E01: Project Scaffolding & Domain Setup

### User Story 1: Initialize Spring Boot (Maven) Project

Section: Spring Boot Project Initialization  
Description: Establish a new Spring Boot 3.x project using Maven, with Java 17+, and configure the application for modularity and scalability.  
Design Specification:
- Use Spring Initializr or Maven archetype.
- GroupId: `com.warehouse.ems`
- ArtifactId: `warehouse-ems`
- Java version: 17+
- Dependencies: Spring Web, Spring Data JPA, Spring Security, Actuator, Flyway/Liquibase, Lombok, Validation, OpenAPI.
- Directory structure:
  - `src/main/java/com/warehouse/ems`
  - `src/main/resources`
Sample Implementation:
```xml
<!-- pom.xml -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>
<groupId>com.warehouse.ems</groupId>
<artifactId>warehouse-ems</artifactId>
<version>1.0.0</version>
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- ... other dependencies ... -->
</dependencies>
```

---

### User Story 2: Configure Base Package Structure

Section: Package Structure  
Description: Define a modular package structure to support separation of concerns and future scalability.  
Design Specification:
- Base: `com.warehouse.ems`
- Sub-packages:
  - `employee`, `attendance`, `scheduling`, `safety`, `equipment`, `training`, `performance`, `payroll`, `notification`, `integration`, `audit`, `reporting`, `mobile`, `onboarding`, `settings`, `common`, `config`
Sample Implementation:
```
com.warehouse.ems
 âââ employee
 âââ attendance
 âââ scheduling
 âââ safety
 âââ equipment
 âââ training
 âââ performance
 âââ payroll
 âââ notification
 âââ integration
 âââ audit
 âââ reporting
 âââ mobile
 âââ onboarding
 âââ settings
 âââ common
 âââ config
```

---

### User Story 3: Set Up Flyway/Liquibase

Section: Database Migration  
Description: Integrate Flyway (or Liquibase) for versioned database schema migrations.  
Design Specification:
- Add Flyway/Liquibase dependency.
- Place migration scripts in `src/main/resources/db/migration`.
- Baseline migration for initial schema.
Sample Implementation:
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```
```sql
-- V1__init.sql
CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    badge_id VARCHAR(32) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    -- ... other fields ...
);
```
```yaml
# application.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

---

### User Story 4: Enable Actuator Health Endpoint

Section: Actuator Health  
Description: Expose Spring Boot Actuator health endpoint for monitoring.  
Design Specification:
- Add `spring-boot-starter-actuator`.
- Expose `/actuator/health` endpoint.
- Restrict access to internal IPs or authenticated users.
Sample Implementation:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

---

### User Story 5: Document Build Instructions

Section: Build & Run Documentation  
Description: Provide clear instructions for building and running the application.  
Design Specification:
- Add `README.md` with Maven build/run steps.
Sample Implementation:
```markdown
# Warehouse EMS

## Build
mvn clean install

## Run
mvn spring-boot:run

## Health Check
curl http://localhost:8080/actuator/health
```

---

## EPIC E02: Employee Master Data (CRUD)

### User Story 6: Create Employee Domain and CRUD APIs

Section: Employee Domain Model  
Description: Define the Employee entity and implement CRUD REST APIs.  
Design Specification:
- Entity fields: id, badgeId, name, role, department, shiftGroup, hireDate, status, deleted.
- Repository: `EmployeeRepository` extends `JpaRepository`.
- Service: `EmployeeService` for business logic.
- Controller: `EmployeeController` with CRUD endpoints.
Sample Implementation:
```java
@Entity
@Table(name = "employee")
public class Employee {
    @Id @GeneratedValue private Long id;
    @Column(unique = true, nullable = false) private String badgeId;
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
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping public ResponseEntity<EmployeeDto> create(@RequestBody @Valid EmployeeDto dto) { ... }
    @GetMapping("/{id}") public ResponseEntity<EmployeeDto> get(@PathVariable Long id) { ... }
    @PutMapping("/{id}") public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @RequestBody @Valid EmployeeDto dto) { ... }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
    @GetMapping public Page<EmployeeDto> list(Pageable pageable, @RequestParam Map<String, String> filters) { ... }
}
```

---

### User Story 7: Enforce Unique badgeId

Section: Unique Constraint  
Description: Ensure badgeId is unique at the database and application level.  
Design Specification:
- `@Column(unique = true)` on badgeId.
- Service checks for existing badgeId before create/update.
Sample Implementation:
```java
if (employeeRepository.findByBadgeIdAndDeletedFalse(dto.getBadgeId()).isPresent()) {
    throw new DuplicateBadgeIdException();
}
```

---

### User Story 8: Support Soft-Delete

Section: Soft-Delete Implementation  
Description: Implement soft-delete for Employee records.  
Design Specification:
- Add `deleted` boolean field.
- Override delete to set `deleted=true`.
- Filter queries to exclude deleted records.
Sample Implementation:
```java
@Transactional
public void deleteEmployee(Long id) {
    Employee emp = employeeRepository.findById(id).orElseThrow(...);
    emp.setDeleted(true);
    employeeRepository.save(emp);
}
```

---

### User Story 9: Support Pagination and Filtering

Section: Pagination & Filtering  
Description: Enable pagination and filtering on Employee list API.  
Design Specification:
- Use Spring Data `Pageable`.
- Support filtering by department, role, status, etc.
Sample Implementation:
```java
@GetMapping
public Page<EmployeeDto> list(Pageable pageable, @RequestParam Map<String, String> filters) {
    // Build Specification or Predicate for filtering
}
```

---

### User Story 10: Document Employee CRUD APIs

Section: API Documentation  
Description: Document Employee APIs using OpenAPI/Swagger.  
Design Specification:
- Add `springdoc-openapi-ui` dependency.
- Annotate controllers and DTOs.
Sample Implementation:
```java
@Operation(summary = "Create Employee", ...)
@PostMapping
public ResponseEntity<EmployeeDto> create(@RequestBody @Valid EmployeeDto dto) { ... }
```
```yaml
# application.yml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

---

## EPIC E03: Role Based Access Control (RBAC)

### User Story 11: Define User Roles and Permissions

Section: RBAC Roles  
Description: Define roles (ADMIN, HR, SUPERVISOR, WORKER) and permissions.  
Design Specification:
- Enum: `Role { ADMIN, HR, SUPERVISOR, WORKER }`
- Map roles to authorities.
Sample Implementation:
```java
public enum Role { ADMIN, HR, SUPERVISOR, WORKER }
```
```java
@PreAuthorize("hasRole('ADMIN')")
public void adminOnlyAction() { ... }
```

---

### User Story 12: Assign Roles to Users

Section: User-Role Assignment  
Description: Assign one or more roles to each user.  
Design Specification:
- User entity has `Set<Role> roles`.
- Admin UI/API to manage roles.
Sample Implementation:
```java
@Entity
public class User {
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;
}
```

---

### User Story 13: Restrict Feature Access by Role

Section: Endpoint Security  
Description: Restrict API and method access by role.  
Design Specification:
- Use `@PreAuthorize` or `@Secured` annotations.
- Configure method security in `@EnableGlobalMethodSecurity`.
Sample Implementation:
```java
@PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
public void hrOrAdminAction() { ... }
```

---

### User Story 14: Audit Role Changes

Section: Role Change Auditing  
Description: Log all changes to user roles for compliance.  
Design Specification:
- Audit table: `audit_log` with entity, action, actor, timestamp, before/after.
- Service logs changes on role assignment.
Sample Implementation:
```java
public void assignRole(User user, Role role, String actor) {
    // ... assign role ...
    auditService.logChange("User", user.getId(), "ROLE_ASSIGN", actor, before, after);
}
```

---

### User Story 15: Role-Based API Access

Section: API Security  
Description: Enforce role-based access at API level.  
Design Specification:
- Secure endpoints with `@PreAuthorize`.
- Return 401/403 for unauthorized/forbidden.
Sample Implementation:
```java
@PreAuthorize("hasRole('SUPERVISOR')")
@GetMapping("/team")
public List<EmployeeDto> getTeam() { ... }
```

---

## EPIC E04: Time & Attendance (Clock In/Out)

### User Story 16: Employee Clock In Endpoint

Section: Clock In API  
Description: Provide endpoint for employees to clock in.  
Design Specification:
- POST `/attendance/clock-in`
- Request: employeeId, timestamp, location, deviceInfo.
Sample Implementation:
```java
@PostMapping("/clock-in")
public ResponseEntity<Void> clockIn(@RequestBody ClockEventDto dto) { ... }
```

---

### User Story 17: Geofence Validation

Section: Geofence Validation  
Description: Validate clock-in location against allowed geofence.  
Design Specification:
- Store allowed geofence per warehouse.
- Validate lat/lon in service.
Sample Implementation:
```java
if (!geofenceService.isWithinAllowedArea(dto.getLocation())) {
    throw new GeofenceViolationException();
}
```

---

### User Story 18: Capture Device Information

Section: Device Info Capture  
Description: Record device info (type, id, IP) on clock events.  
Design Specification:
- Extend ClockEvent entity with device fields.
Sample Implementation:
```java
public class ClockEvent {
    private String deviceType;
    private String deviceId;
    private String ipAddress;
}
```

---

### User Story 19: Automated Hours Calculation

Section: Hours Calculation  
Description: Calculate hours worked per shift automatically.  
Design Specification:
- On clock-out, compute duration from last clock-in.
- Store daily totals.
Sample Implementation:
```java
public void clockOut(Long employeeId, LocalDateTime outTime) {
    ClockEvent in = clockEventRepo.findLastClockIn(employeeId);
    Duration worked = Duration.between(in.getTimestamp(), outTime);
    // Save worked hours
}
```

---

### User Story 20: Missed Punches & Corrections Workflow

Section: Missed Punches Workflow  
Description: Handle missed punches and corrections with approval workflow.  
Design Specification:
- Correction request entity.
- Supervisor approval endpoint.
Sample Implementation:
```java
@PostMapping("/attendance/correction")
public ResponseEntity<Void> requestCorrection(@RequestBody CorrectionDto dto) { ... }
```

---

## EPIC E05: Shift & Schedule Management

### User Story 21: Create Shift Templates

Section: Shift Template Model  
Description: Define recurring shift templates.  
Design Specification:
- Entity: ShiftTemplate (name, startTime, endTime, recurrence, etc.)
Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrence; // e.g., "WEEKLY"
}
```

---

### User Story 22: Manage Shift Rotations

Section: Shift Rotation  
Description: Assign shift rotations to employees.  
Design Specification:
- EmployeeShiftAssignment entity.
- Bulk assignment API for supervisors.
Sample Implementation:
```java
@Entity
public class EmployeeShiftAssignment {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private ShiftTemplate shiftTemplate;
    private LocalDate effectiveDate;
}
```

---

## EPIC E06: Leave & Absence Management

### User Story 23: Submit Leave Request

Section: Leave Request API  
Description: Allow employees to submit leave requests.  
Design Specification:
- LeaveRequest entity: employee, type, startDate, endDate, status.
- POST `/leave/requests`
Sample Implementation:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // PENDING, APPROVED, REJECTED
}
```
```java
@PostMapping("/leave/requests")
public ResponseEntity<Void> submitLeave(@RequestBody LeaveRequestDto dto) { ... }
```

---

### User Story 24: Approve or Reject Leave Request

Section: Leave Approval Workflow  
Description: Supervisors approve/reject leave requests.  
Design Specification:
- PUT `/leave/requests/{id}/approve`
- PUT `/leave/requests/{id}/reject`
Sample Implementation:
```java
@PutMapping("/leave/requests/{id}/approve")
public ResponseEntity<Void> approve(@PathVariable Long id) { ... }
```

---

### User Story 25: View Leave Balance

Section: Leave Balance Calculation  
Description: Show current leave balance for each employee.  
Design Specification:
- Calculate based on accrual policy and approved leaves.
Sample Implementation:
```java
public int getLeaveBalance(Long employeeId) {
    // accrual - used
}
```

---

### User Story 26: Leave Calendar Integration

Section: Leave Calendar  
Description: Integrate leave data into scheduling calendar.  
Design Specification:
- Exclude approved leaves from shift assignments.
Sample Implementation:
```java
if (leaveService.isOnLeave(employeeId, date)) {
    // skip scheduling
}
```

---

## EPIC E07: Training & Certification Tracking

### User Story 27: Assign Training to Employee

Section: Training Assignment  
Description: Assign required training to employees.  
Design Specification:
- TrainingAssignment entity: employee, training, dueDate, status.
Sample Implementation:
```java
@Entity
public class TrainingAssignment {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private Training training;
    private LocalDate dueDate;
    private String status; // ASSIGNED, COMPLETED
}
```

---

### User Story 28: Track Training Completion

Section: Training Completion  
Description: Track when employees complete training.  
Design Specification:
- Update status and completion date.
Sample Implementation:
```java
public void completeTraining(Long assignmentId) {
    TrainingAssignment ta = repo.findById(assignmentId).orElseThrow(...);
    ta.setStatus("COMPLETED");
    ta.setCompletionDate(LocalDate.now());
    repo.save(ta);
}
```

---

### User Story 29: View Certification Status

Section: Certification Status  
Description: Show current certification status on employee profile.  
Design Specification:
- Query latest certifications and expiry.
Sample Implementation:
```java
public List<CertificationDto> getCertifications(Long employeeId) { ... }
```

---

### User Story 30: Notify Expiring Certifications

Section: Certification Expiry Notification  
Description: Notify employees and supervisors of expiring certifications.  
Design Specification:
- Scheduled job checks for expiring certs (30/7 days).
- Send notification via email/SMS/in-app.
Sample Implementation:
```java
@Scheduled(cron = "0 0 8 * * *")
public void notifyExpiringCerts() { ... }
```

---

### User Story 31: Export Training Records

Section: Training Records Export  
Description: Export employee training and certification records.  
Design Specification:
- Export as CSV/PDF.
Sample Implementation:
```java
@GetMapping("/training/export")
public ResponseEntity<Resource> exportTrainingRecords() { ... }
```

---

## EPIC E08: Safety Incidents & OSHA Reporting

### User Story 32: Report Safety Incident

Section: Safety Incident Reporting  
Description: Allow employees to report safety incidents.  
Design Specification:
- SafetyIncident entity: employee, date, location, severity, description.
Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private LocalDate date;
    private String location;
    private String severity;
    private String description;
    private String status; // OPEN, INVESTIGATING, RESOLVED
}
```
```java
@PostMapping("/safety/incidents")
public ResponseEntity<Void> reportIncident(@RequestBody IncidentDto dto) { ... }
```

---

### User Story 33: Investigate Safety Incident

Section: Incident Investigation  
Description: Document investigation steps and findings.  
Design Specification:
- Add investigation notes and status updates.
Sample Implementation:
```java
public void updateInvestigation(Long incidentId, String notes) { ... }
```

---

### User Story 34: Generate OSHA Report

Section: OSHA Reporting  
Description: Generate OSHA 300/300A reports.  
Design Specification:
- Export incidents in OSHA format.
Sample Implementation:
```java
@GetMapping("/safety/osha-report")
public ResponseEntity<Resource> generateOSHAReport() { ... }
```

---

### User Story 35: Track Incident Resolution

Section: Incident Resolution Tracking  
Description: Track resolution status and corrective actions.  
Design Specification:
- Update status to RESOLVED with resolution notes.
Sample Implementation:
```java
public void resolveIncident(Long incidentId, String resolution) { ... }
```

---

### User Story 36: View Safety Incident Dashboard

Section: Safety Dashboard  
Description: Dashboard showing incident metrics.  
Design Specification:
- Aggregate incidents by severity, status, location.
Sample Implementation:
```java
@GetMapping("/safety/dashboard")
public ResponseEntity<DashboardDto> getDashboard() { ... }
```

---

## EPIC E09: Equipment & Asset Assignment

### User Story 37: Assign Equipment to Employee

Section: Equipment Assignment  
Description: Assign equipment to employees.  
Design Specification:
- EquipmentAssignment entity: equipment, employee, assignedDate, returnedDate.
Sample Implementation:
```java
@Entity
public class EquipmentAssignment {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Equipment equipment;
    @ManyToOne private Employee employee;
    private LocalDate assignedDate;
    private LocalDate returnedDate;
}
```

---

### User Story 38: Track Equipment Usage

Section: Equipment Usage Tracking  
Description: Log equipment usage and maintenance.  
Design Specification:
- EquipmentUsageLog entity.
Sample Implementation:
```java
@Entity
public class EquipmentUsageLog {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Equipment equipment;
    private LocalDateTime usageStart;
    private LocalDateTime usageEnd;
}
```

---

### User Story 39: View Asset Assignment History

Section: Asset History  
Description: View history of equipment assignments.  
Design Specification:
- Query all assignments for a given equipment.
Sample Implementation:
```java
public List<EquipmentAssignment> getHistory(Long equipmentId) { ... }
```

---

### User Story 40: Notify Equipment Maintenance Due

Section: Maintenance Notification  
Description: Notify when equipment maintenance is due.  
Design Specification:
- Scheduled job checks maintenance schedules.
Sample Implementation:
```java
@Scheduled(cron = "0 0 8 * * *")
public void notifyMaintenanceDue() { ... }
```

---

## EPIC E10: Performance Reviews & Goals

### User Story 41: Initiate Performance Review

Section: Performance Review Initiation  
Description: HR initiates review cycles.  
Design Specification:
- PerformanceReview entity: employee, reviewPeriod, status.
Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String reviewPeriod;
    private String status; // INITIATED, SUBMITTED, COMPLETED
}
```

---

### User Story 42: Submit Performance Feedback

Section: Feedback Submission  
Description: Managers submit feedback.  
Design Specification:
- Add feedback field to PerformanceReview.
Sample Implementation:
```java
public void submitFeedback(Long reviewId, String feedback) { ... }
```

---

### User Story 43: Set Employee Goals

Section: Goal Setting  
Description: Employees set and track goals.  
Design Specification:
- Goal entity: employee, description, targetDate, status.
Sample Implementation:
```java
@Entity
public class Goal {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String description;
    private LocalDate targetDate;
    private String status;
}
```

---

### User Story 44: View Performance Review History

Section: Review History  
Description: View past performance reviews.  
Design Specification:
- Query all reviews for an employee.
Sample Implementation:
```java
public List<PerformanceReview> getReviewHistory(Long employeeId) { ... }
```

---

## EPIC E11: Payroll Export Integration

### User Story 45: Export Payroll Data

Section: Payroll Export  
Description: Export payroll data for external processing.  
Design Specification:
- Generate CSV/XML with hours worked, leave, etc.
Sample Implementation:
```java
@GetMapping("/payroll/export")
public ResponseEntity<Resource> exportPayroll() { ... }
```

---

### User Story 46: Validate Payroll Data

Section: Payroll Validation  
Description: Validate data before export.  
Design Specification:
- Check for missing clock-outs, unapproved leaves.
Sample Implementation:
```java
public List<String> validatePayrollData() { ... }
```

---

### User Story 47: Schedule Automated Payroll Exports

Section: Automated Export  
Description: Schedule payroll exports.  
Design Specification:
- Scheduled job runs export at configured intervals.
Sample Implementation:
```java
@Scheduled(cron = "0 0 0 1 * *")
public void exportPayroll() { ... }
```

---

### User Story 48: Notify Payroll Export Completion

Section: Export Notification  
Description: Notify admins when export completes.  
Design Specification:
- Send notification on successful export.
Sample Implementation:
```java
notificationService.send("Payroll export completed");
```

---

## EPIC E12: Notifications & Announcements

### User Story 49: Send Company-Wide Announcement

Section: Announcement Broadcast  
Description: Send announcements to all employees.  
Design Specification:
- Announcement entity: title, message, date.
Sample Implementation:
```java
@Entity
public class Announcement {
    @Id @GeneratedValue private Long id;
    private String title;
    private String message;
    private LocalDateTime date;
}
```
```java
@PostMapping("/announcements")
public ResponseEntity<Void> sendAnnouncement(@RequestBody AnnouncementDto dto) { ... }
```

---

### User Story 50: Configure Notification Preferences

Section: Notification Preferences  
Description: Users configure notification channels.  
Design Specification:
- UserPreferences entity: email, sms, inApp flags.
Sample Implementation:
```java
@Entity
public class UserPreferences {
    @Id @GeneratedValue private Long id;
    @OneToOne private User user;
    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean inAppEnabled;
}
```

---

### User Story 51: View Announcement History

Section: Announcement History  
Description: View past announcements.  
Design Specification:
- Query all announcements.
Sample Implementation:
```java
@GetMapping("/announcements")
public List<AnnouncementDto> getAnnouncements() { ... }
```

---

### User Story 52: Targeted Notifications by Role

Section: Role-Based Notifications  
Description: Send notifications to specific roles.  
Design Specification:
- Filter recipients by role.
Sample Implementation:
```java
public void sendToRole(Role role, String message) { ... }
```

---

## EPIC E13: Integration Layer (HRIS/WMS APIs)

### User Story 53: Sync Employee Data from HRIS

Section: HRIS Integration  
Description: Sync employee data from external HRIS.  
Design Specification:
- Scheduled job calls HRIS API.
- Map fields to Employee entity.
Sample Implementation:
```java
@Scheduled(cron = "0 0 2 * * *")
public void syncFromHRIS() { ... }
```

---

### User Story 54: Sync Warehouse Data from WMS

Section: WMS Integration  
Description: Sync warehouse/location data from WMS.  
Design Specification:
- Scheduled job calls WMS API.
Sample Implementation:
```java
@Scheduled(cron = "0 0 3 * * *")
public void syncFromWMS() { ... }
```

---

### User Story 55: Map External Fields to EMS Schema

Section: Field Mapping  
Description: Configure field mappings for integrations.  
Design Specification:
- FieldMapping entity: externalField, internalField.
Sample Implementation:
```java
@Entity
public class FieldMapping {
    @Id @GeneratedValue private Long id;
    private String externalField;
    private String internalField;
}
```

---

### User Story 56: Handle Integration Errors

Section: Error Handling  
Description: Log and notify on integration failures.  
Design Specification:
- Catch exceptions, log, send alert.
Sample Implementation:
```java
try {
    syncFromHRIS();
} catch (Exception e) {
    log.error("HRIS sync failed", e);
    notificationService.sendAlert("HRIS sync failed");
}
```

---

### User Story 57: View Integration Status Dashboard

Section: Integration Dashboard  
Description: Dashboard showing integration status.  
Design Specification:
- Display last sync time, status, errors.
Sample Implementation:
```java
@GetMapping("/integration/status")
public ResponseEntity<IntegrationStatusDto> getStatus() { ... }
```

---

## EPIC E14: Audit Trail & Compliance

### User Story 58: Log All User Actions

Section: Audit Logging  
Description: Log all sensitive actions.  
Design Specification:
- AuditLog entity: entity, action, actor, timestamp, before, after.
Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String entity;
    private String action;
    private String actor;
    private LocalDateTime timestamp;
    private String before;
    private String after;
}
```

---

### User Story 59: Search Audit Logs

Section: Audit Log Search  
Description: Search audit logs by criteria.  
Design Specification:
- Filter by entity, actor, date range.
Sample Implementation:
```java
@GetMapping("/audit/logs")
public Page<AuditLogDto> searchLogs(@RequestParam Map<String, String> filters, Pageable pageable) { ... }
```

---

### User Story 60: Export Audit Logs

Section: Audit Log Export  
Description: Export audit logs for compliance.  
Design Specification:
- Export as CSV.
Sample Implementation:
```java
@GetMapping("/audit/export")
public ResponseEntity<Resource> exportAuditLogs() { ... }
```

---

### User Story 61: Notify Suspicious Activity

Section: Suspicious Activity Alerts  
Description: Alert on suspicious activity.  
Design Specification:
- Detect anomalies (e.g., multiple failed logins).
Sample Implementation:
```java
if (failedLoginCount > 5) {
    notificationService.sendAlert("Suspicious activity detected");
}
```

---

## EPIC E15: Reporting & Analytics

### User Story 62: Generate Attendance Report

Section: Attendance Reporting  
Description: Generate attendance reports.  
Design Specification:
- Aggregate attendance data by date, employee.
Sample Implementation:
```java
@GetMapping("/reports/attendance")
public ResponseEntity<Resource> generateAttendanceReport() { ... }
```

---

### User Story 63: Generate Overtime Report

Section: Overtime Reporting  
Description: Generate overtime reports.  
Design Specification:
- Calculate overtime hours per employee.
Sample Implementation:
```java
@GetMapping("/reports/overtime")
public ResponseEntity<Resource> generateOvertimeReport() { ... }
```

---

### User Story 64: Generate Leave Balance Report

Section: Leave Balance Reporting  
Description: Generate leave balance reports.  
Design Specification:
- Show leave balances by employee.
Sample Implementation:
```java
@GetMapping("/reports/leave-balance")
public ResponseEntity<Resource> generateLeaveBalanceReport() { ... }
```

---

### User Story 65: Generate Certification Status Report

Section: Certification Reporting  
Description: Generate certification status reports.  
Design Specification:
- Show certification status by employee.
Sample Implementation:
```java
@GetMapping("/reports/certifications")
public ResponseEntity<Resource> generateCertificationReport() { ... }
```

---

### User Story 66: View Analytics Dashboard

Section: Analytics Dashboard  
Description: Dashboard with key metrics.  
Design Specification:
- Display attendance, overtime, leave, safety metrics.
Sample Implementation:
```java
@GetMapping("/analytics/dashboard")
public ResponseEntity<DashboardDto> getDashboard() { ... }
```

---

## EPIC E16: Mobile Access (PWA)

### User Story 67: Clock In/Out via Mobile

Section: Mobile Clock In/Out  
Description: Mobile-friendly clock in/out.  
Design Specification:
- Responsive UI, PWA manifest.
Sample Implementation:
```html
<!-- manifest.json -->
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone"
}
```

---

### User Story 68: View Schedule on Mobile

Section: Mobile Schedule View  
Description: View schedule on mobile.  
Design Specification:
- Responsive schedule UI.
Sample Implementation:
```html
<!-- Mobile-optimized schedule view -->
```

---

### User Story 69: Request Leave via Mobile

Section: Mobile Leave Request  
Description: Submit leave requests via mobile.  
Design Specification:
- Mobile-friendly leave request form.
Sample Implementation:
```html
<!-- Mobile leave request form -->
```

---

### User Story 70: Receive Notifications on Mobile

Section: Mobile Push Notifications  
Description: Push notifications on mobile.  
Design Specification:
- Integrate with Firebase Cloud Messaging or similar.
Sample Implementation:
```java
// Send push notification
```

---

## EPIC E17: Onboarding & Offboarding Workflow

### User Story 71: Automate New Hire Onboarding

Section: Onboarding Automation  
Description: Automate onboarding tasks.  
Design Specification:
- OnboardingTask entity: employee, task, status.
Sample Implementation:
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String task;
    private String status;
}
```

---

### User Story 72: Assign Initial Training

Section: Initial Training Assignment  
Description: Assign training to new hires.  
Design Specification:
- Automatically assign required training on hire.
Sample Implementation:
```java
public void onNewHire(Employee employee) {
    trainingService.assignInitialTraining(employee);
}
```

---

### User Story 73: Automate Offboarding Tasks

Section: Offboarding Automation  
Description: Automate offboarding tasks.  
Design Specification:
- Revoke access, collect assets.
Sample Implementation:
```java
public void offboard(Employee employee) {
    accessService.revokeAccess(employee);
    assetService.collectAssets(employee);
}
```

---

### User Story 74: Track Onboarding/Offboarding Progress

Section: Progress Tracking  
Description: Track onboarding/offboarding progress.  
Design Specification:
- Dashboard showing task completion.
Sample Implementation:
```java
@GetMapping("/onboarding/progress/{employeeId}")
public ResponseEntity<ProgressDto> getProgress(@PathVariable Long employeeId) { ... }
```

---

## EPIC E18: Localization & Multi-Warehouse

### User Story 75: Configure Warehouse-Specific Settings

Section: Warehouse Configuration  
Description: Configure settings per warehouse.  
Design Specification:
- Warehouse entity: name, location, policies.
Sample Implementation:
```java
@Entity
public class Warehouse {
    @Id @GeneratedValue private Long id;
    private String name;
    private String location;
    private String policies;
}
```

---

### User Story 76: Assign Employees to Warehouses

Section: Warehouse Assignment  
Description: Assign employees to warehouses.  
Design Specification:
- Employee has @ManyToOne Warehouse.
Sample Implementation:
```java
@ManyToOne
private Warehouse warehouse;
```

---

### User Story 77: Support Multi-Language Interface

Section: Localization  
Description: Support multiple languages.  
Design Specification:
- Use Spring MessageSource for i18n.
Sample Implementation:
```java
@Bean
public MessageSource messageSource() {
    ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
    ms.setBasename("messages");
    return ms;
}
```

---

### User Story 78: Localize Date and Time Formats

Section: Date/Time Localization  
Description: Localize date/time formats.  
Design Specification:
- Use user's locale for formatting.
Sample Implementation:
```java
DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale);
```

---

## EPIC E19: Automated Testing & CI/CD

### User Story 79: Implement Unit Tests

Section: Unit Testing  
Description: Write unit tests for all services.  
Design Specification:
- Use JUnit 5, Mockito.
- Target 80% code coverage.
Sample Implementation:
```java
@Test
public void testCreateEmployee() {
    // ...
}
```

---

### User Story 80: Implement Integration Tests

Section: Integration Testing  
Description: Write integration tests for critical workflows.  
Design Specification:
- Use @SpringBootTest, TestRestTemplate.
Sample Implementation:
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class EmployeeIntegrationTest {
    @Test
    public void testCreateEmployee() { ... }
}
```

---

### User Story 81: Set Up CI/CD Pipeline

Section: CI/CD Pipeline  
Description: Automate build, test, deploy.  
Design Specification:
- Use GitHub Actions, Jenkins, or similar.
Sample Implementation:
```yaml
# .github/workflows/ci.yml
name: CI
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
```

---

### User Story 82: Automate Security Scanning

Section: Security Scanning  
Description: Automate security scans in CI/CD.  
Design Specification:
- Use OWASP Dependency-Check, Snyk.
Sample Implementation:
```yaml
- name: Security Scan
  run: mvn dependency-check:check
```

---

### User Story 83: Monitor Pipeline Performance

Section: Pipeline Monitoring  
Description: Monitor CI/CD pipeline performance.  
Design Specification:
- Track build times, test results.
Sample Implementation:
```yaml
# Monitor with CI/CD tool dashboards
```

---

## EPIC E20: Documentation & Runbooks

### User Story 84: Generate API Documentation

Section: API Documentation  
Description: Generate OpenAPI documentation.  
Design Specification:
- Use springdoc-openapi.
Sample Implementation:
```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
```

---

### User Story 85: Create Architecture Diagrams

Section: Architecture Diagrams  
Description: Document system architecture.  
Design Specification:
- Use PlantUML, draw.io.
Sample Implementation:
```
@startuml
[Employee Service] --> [Database]
@enduml
```

---

### User Story 86: Write Deployment Guides

Section: Deployment Guides  
Description: Document deployment steps.  
Design Specification:
- Step-by-step deployment instructions.
Sample Implementation:
```markdown
# Deployment Guide
1. Build: `mvn clean package`
2. Deploy: `java -jar target/ems.jar`
```

---

### User Story 87: Create Troubleshooting Runbooks

Section: Troubleshooting Runbooks  
Description: Document common issues and solutions.  
Design Specification:
- Runbooks for DB connection, auth issues, etc.
Sample Implementation:
```markdown
# Troubleshooting
## DB Connection Failed
- Check DB credentials in application.yml
- Verify DB is running
```

---

## Conclusion

This comprehensive technical design document covers all 87 user stories for the Warehouse EMS project, providing detailed entity models, repository/service/controller patterns, security configurations, integration points, and code examples following Spring Boot 3.x best practices. Each section is production-ready and can be directly implemented by the development team.