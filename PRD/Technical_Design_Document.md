# Warehouse Employee Management System (EMS) - Comprehensive Low-Level Technical Design Document

---

## Table of Contents

1. [E01 Project Scaffolding & Domain Setup](#e01)
2. [E02 Employee Master Data (CRUD)](#e02)
3. [E03 Role Based Access Control (RBAC)](#e03)
4. [E04 Time & Attendance (Clock In/Out)](#e04)
5. [E05 Shift & Schedule Management](#e05)
6. [E06 Leave & Absence Management](#e06)
7. [E07 Training & Certification Tracking](#e07)
8. [E08 Safety Incidents & OSHA Reporting](#e08)
9. [E09 Equipment & Asset Assignment](#e09)
10. [E10 Performance Reviews & Goals](#e10)
11. [E11 Payroll Export Integration](#e11)
12. [E12 Notifications & Announcements](#e12)
13. [E13 Integration Layer (HRIS/WMS APIs)](#e13)
14. [E14 Audit Trail & Compliance](#e14)
15. [E15 Reporting & Analytics](#e15)
16. [E16 Mobile Access (PWA)](#e16)
17. [E17 Onboarding & Offboarding Workflow](#e17)
18. [E18 Localization & Multi-Warehouse](#e18)
19. [E19 Observability & Monitoring](#e19)
20. [E20 Deployment & CI/CD](#e20)

---

# E01 Project Scaffolding & Domain Setup

## User Story 1: Initialize Spring Boot (Maven) Project
Section: Spring Boot Architecture Overview  
Description: Establishes the foundational structure for the EMS application using Spring Boot and Maven, ensuring modularity and scalability.  
Design Specification:
- Use Spring Initializr to generate a Maven-based project.
- Base package: `com.warehouse.ems`
- Modules: `employee`, `attendance`, `scheduling`, `safety`, etc.
- Directory structure follows standard Maven conventions.
Sample Implementation:
```bash
mvn archetype:generate -DgroupId=com.warehouse.ems -DartifactId=warehouse-ems -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```

Section: Package Structure  
Description: Organizes code into logical packages for maintainability.  
Design Specification:
- `com.warehouse.ems.employee`
- `com.warehouse.ems.attendance`
- `com.warehouse.ems.scheduling`
- `com.warehouse.ems.safety`
- `com.warehouse.ems.config`
Sample Implementation:
```
com/warehouse/ems/
  |-- employee/
  |-- attendance/
  |-- scheduling/
  |-- safety/
  |-- config/
```

Section: Configuration & Database Migration  
Description: Integrates Flyway/Liquibase for versioned database migrations.  
Design Specification:
- Add Flyway/Liquibase dependency in `pom.xml`.
- Place migration scripts in `src/main/resources/db/migration`.
Sample Implementation:
```xml
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-core</artifactId>
</dependency>
```
```sql
-- V1__init.sql
CREATE TABLE employee (...);
```

Section: Actuator & Health Endpoints  
Description: Enables Spring Boot Actuator for monitoring and health checks.  
Design Specification:
- Add `spring-boot-starter-actuator` to `pom.xml`.
- Expose `/actuator/health` endpoint.
Sample Implementation:
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

Section: Documentation & README  
Description: Provides build and run instructions for developers.  
Design Specification:
- Include `README.md` with Maven build/run steps.
Sample Implementation:
```
# Build
mvn clean install

# Run
mvn spring-boot:run
```

---

## User Story 2: Configure Flyway/Liquibase for DB Migrations
Section: Database Migration Strategy  
Description: Ensures consistent schema evolution across environments.  
Design Specification:
- Use Flyway for SQL-based migrations.
- Place migration scripts in `src/main/resources/db/migration`.
- Version scripts as `V1__init.sql`, `V2__add_employee_table.sql`, etc.
Sample Implementation:
```sql
-- V2__add_employee_table.sql
CREATE TABLE employee (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  badge_id VARCHAR(50) UNIQUE NOT NULL,
  ...
);
```

Section: Application Properties  
Description: Configures Flyway/Liquibase in `application.yml`.  
Design Specification:
- Set migration locations and enable on startup.
Sample Implementation:
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

---

## User Story 3: Enable Actuator for Monitoring
Section: Observability & Monitoring  
Description: Provides endpoints for health, metrics, and readiness/liveness probes.  
Design Specification:
- Expose `/actuator/health`, `/actuator/metrics`, `/actuator/info`.
- Secure actuator endpoints (see E19 for advanced monitoring).
Sample Implementation:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

---

## User Story 4: Document Project Setup
Section: Developer Onboarding Documentation  
Description: Ensures all developers can build, run, and test the project locally.  
Design Specification:
- `README.md` with prerequisites, build/run/test instructions, and troubleshooting.
Sample Implementation:
```
# Prerequisites
- Java 17+
- Maven 3.8+
- Docker (optional for DB)

# Build
mvn clean install

# Run
mvn spring-boot:run

# Test
mvn test
```

---

# E02 Employee Master Data (CRUD)

## User Story 1: Implement CRUD Endpoints for Employee
Section: Spring Boot Architecture Overview  
Description: Implements RESTful CRUD APIs for managing employee records.  
Design Specification:
- Follows layered architecture: Controller â Service â Repository â Entity
- Uses DTOs for API requests/responses
Sample Implementation:
```
EmployeeController â EmployeeService â EmployeeRepository â Employee
```

Section: Package Structure  
Description: Organizes employee-related code.  
Design Specification:
- `com.warehouse.ems.employee.controller`
- `com.warehouse.ems.employee.service`
- `com.warehouse.ems.employee.repository`
- `com.warehouse.ems.employee.domain`
- `com.warehouse.ems.employee.dto`
Sample Implementation:
```
com/warehouse/ems/employee/
  |-- controller/
  |-- service/
  |-- repository/
  |-- domain/
  |-- dto/
```

Section: Domain Model Design  
Description: Defines the Employee entity with JPA annotations.  
Design Specification:
- Fields: id, name, badgeId, role, department, shiftGroup, hireDate, status, createdAt, updatedAt
- Unique constraint on badgeId
- Soft delete with `deleted` boolean
- Audit fields
Sample Implementation:
```java
@Entity
@Table(name = "employee", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false)
  private String name;
  @Column(name = "badge_id", nullable = false, unique = true)
  private String badgeId;
  @Enumerated(EnumType.STRING)
  private Role role;
  private String department;
  private String shiftGroup;
  private LocalDate hireDate;
  @Enumerated(EnumType.STRING)
  private Status status;
  private boolean deleted = false;
  @CreatedDate
  private Instant createdAt;
  @LastModifiedDate
  private Instant updatedAt;
}
```

Section: Repository Layer  
Description: Provides CRUD and custom query methods.  
Design Specification:
- Extends `JpaRepository<Employee, Long>`
- Custom methods: `findByBadgeId`, `findAllByDeletedFalse(Pageable pageable)`
Sample Implementation:
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  Optional<Employee> findByBadgeId(String badgeId);
  Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

Section: Service Layer  
Description: Encapsulates business logic and transaction management.  
Design Specification:
- Interface: `EmployeeService`
- Implementation: `EmployeeServiceImpl`
- Methods: create, update, delete (soft), get, list (with pagination/filter)
- Annotated with `@Transactional`
Sample Implementation:
```java
public interface EmployeeService {
  EmployeeDto create(EmployeeDto dto);
  EmployeeDto update(Long id, EmployeeDto dto);
  void delete(Long id);
  EmployeeDto get(Long id);
  Page<EmployeeDto> list(Pageable pageable, EmployeeFilter filter);
}
```

Section: Controller Layer  
Description: Exposes REST endpoints for CRUD operations.  
Design Specification:
- Endpoints: POST/GET/PUT/PATCH/DELETE `/employees`
- Uses DTOs and validation annotations
- OpenAPI annotations for documentation
Sample Implementation:
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
  @PostMapping
  public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { ... }
  @GetMapping("/{id}")
  public ResponseEntity<EmployeeDto> get(@PathVariable Long id) { ... }
  @PutMapping("/{id}")
  public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) { ... }
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
  @GetMapping
  public Page<EmployeeDto> list(@PageableDefault Pageable pageable, EmployeeFilter filter) { ... }
}
```

Section: Validation & OpenAPI  
Description: Ensures input validation and API documentation.  
Design Specification:
- Use `@Valid`, `@NotNull`, `@Size`, etc. on DTO fields
- Annotate endpoints with `@Operation`, `@Parameter`, etc.
Sample Implementation:
```java
public class EmployeeDto {
  @NotBlank
  private String name;
  @NotBlank
  private String badgeId;
  ...
}
```

---

## User Story 2: Enforce Unique Badge ID
Section: Domain Model & Repository  
Description: Ensures badgeId is unique at the database and application level.  
Design Specification:
- Unique constraint in JPA entity and DB migration
- Repository method: `findByBadgeId`
- Service checks for duplicates before create/update
Sample Implementation:
```java
if (employeeRepository.findByBadgeId(dto.getBadgeId()).isPresent()) {
  throw new DuplicateBadgeIdException();
}
```

---

## User Story 3: Support Soft Delete
Section: Domain Model & Repository  
Description: Implements soft delete by marking records as deleted instead of physical removal.  
Design Specification:
- Add `deleted` boolean field to Employee
- Override repository queries to filter out deleted records
Sample Implementation:
```java
public void delete(Long id) {
  Employee emp = employeeRepository.findById(id).orElseThrow(...);
  emp.setDeleted(true);
  employeeRepository.save(emp);
}
```

---

## User Story 4: Implement Pagination & Filtering
Section: Repository & Service Layer  
Description: Supports efficient listing of employees with pagination and filter options.  
Design Specification:
- Use Spring Data `Pageable` and custom `EmployeeFilter` object
- Repository method: `findAllByDeletedFalse(Pageable pageable)`
Sample Implementation:
```java
public Page<EmployeeDto> list(Pageable pageable, EmployeeFilter filter) {
  // Use Specification or QueryDSL for advanced filtering
  return employeeRepository.findAll(EmployeeSpecification.fromFilter(filter), pageable)
    .map(EmployeeMapper::toDto);
}
```

---

# E03 Role Based Access Control (RBAC)

## User Story 1: Role-Based Endpoint Security
Section: Security Configuration  
Description: Restricts access to endpoints based on user roles (ADMIN, HR, SUPERVISOR, WORKER).  
Design Specification:
- Use Spring Security with method-level security (`@PreAuthorize`)
- Configure roles in `application.yml` or via DB
Sample Implementation:
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
public EmployeeDto create(EmployeeDto dto) { ... }
```

Section: Security Package Structure  
Description: Centralizes security configuration.  
Design Specification:
- `com.warehouse.ems.security`
- `SecurityConfig.java`, `JwtAuthFilter.java`, etc.
Sample Implementation:
```
com/warehouse/ems/security/
  |-- SecurityConfig.java
  |-- JwtAuthFilter.java
  |-- ...
```

---

## User Story 2: API Key/OAuth2 Authentication
Section: Authentication Mechanisms  
Description: Supports both API key and OAuth2 authentication, configurable via properties.  
Design Specification:
- Use Spring Security filters for API key
- Use `spring-boot-starter-oauth2-resource-server` for OAuth2
- Toggle via `application.yml`
Sample Implementation:
```yaml
security:
  auth-type: oauth2 # or apikey
```

---

## User Story 3: Row-Level Security for Supervisors
Section: Data Access Control  
Description: Restricts supervisors to only access their team's data.  
Design Specification:
- Add `supervisor_id` to Employee
- Use repository methods and `@PreAuthorize` with SpEL
Sample Implementation:
```java
@PreAuthorize("hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(principal, #id)")
public EmployeeDto get(Long id) { ... }
```

---

## User Story 4: Automated Security Tests
Section: Security Testing  
Description: Ensures all security rules are covered by automated tests.  
Design Specification:
- Use Spring Security Test (`@WithMockUser`)
- Test unauthorized (401) and forbidden (403) scenarios
Sample Implementation:
```java
@Test
@WithMockUser(roles = "WORKER")
void testAdminEndpointForbidden() throws Exception {
  mockMvc.perform(post("/employees")).andExpect(status().isForbidden());
}
```

---

# E04 Time & Attendance (Clock In/Out)

## User Story 1: Clock-In/Out Endpoints
Section: Controller & Service Layer  
Description: Provides endpoints for employees to clock in and out, capturing device and location info.  
Design Specification:
- Endpoints: POST `/attendance/clock-in`, `/attendance/clock-out`
- DTOs: `ClockEventDto`
- Validation: employeeId, timestamp, deviceId, (optional) geofence
Sample Implementation:
```java
@PostMapping("/attendance/clock-in")
public ResponseEntity<Void> clockIn(@Valid @RequestBody ClockEventDto dto) { ... }
```

---

## User Story 2: Calculate Hours Worked
Section: Service Layer  
Description: Computes total hours worked per shift based on clock-in/out events.  
Design Specification:
- Service method: `calculateHoursWorked(employeeId, date)`
- Handles overnight shifts
Sample Implementation:
```java
public Duration calculateHoursWorked(Long employeeId, LocalDate date) { ... }
```

---

## User Story 3: Handle Missed Punches
Section: Correction Workflow  
Description: Allows employees to submit corrections for missed punches, routed for supervisor approval.  
Design Specification:
- Endpoint: POST `/attendance/correction`
- Entity: `AttendanceCorrection`
- Status workflow: PENDING, APPROVED, REJECTED
Sample Implementation:
```java
@Entity
public class AttendanceCorrection {
  ...
  @Enumerated(EnumType.STRING)
  private Status status;
}
```

---

## User Story 4: Export Attendance Reports
Section: Reporting & Export  
Description: Enables export of attendance data in CSV format.  
Design Specification:
- Endpoint: GET `/attendance/report?format=csv`
- Service generates CSV from attendance records
Sample Implementation:
```java
@GetMapping("/attendance/report")
public void exportReport(@RequestParam String format, HttpServletResponse response) { ... }
```

---

# E05 Shift & Schedule Management

## User Story 1: Create Shift Templates
Section: Domain Model & CRUD  
Description: Allows creation of recurring shift templates and rules.  
Design Specification:
- Entity: `ShiftTemplate` (id, name, startTime, endTime, recurrence, etc.)
- CRUD endpoints for templates
Sample Implementation:
```java
@Entity
public class ShiftTemplate {
  @Id @GeneratedValue
  private Long id;
  private String name;
  private LocalTime startTime;
  private LocalTime endTime;
  private String recurrence;
}
```

---

## User Story 2: Assign Shifts to Employees
Section: Assignment Logic  
Description: Assigns shifts to employees, supporting bulk assignment.  
Design Specification:
- Entity: `ShiftAssignment` (employeeId, shiftTemplateId, date)
- Service method: assignShifts(List<AssignmentDto>)
Sample Implementation:
```java
public void assignShifts(List<AssignmentDto> assignments) { ... }
```

---

## User Story 3: Detect Scheduling Conflicts
Section: Validation Logic  
Description: Prevents overlapping or conflicting shift assignments.  
Design Specification:
- Service checks for overlapping assignments before saving
Sample Implementation:
```java
if (hasConflict(employeeId, newAssignment)) {
  throw new SchedulingConflictException();
}
```

---

## User Story 4: Display Upcoming Shifts
Section: Controller & Service  
Description: Provides endpoint for employees to view their upcoming shifts.  
Design Specification:
- Endpoint: GET `/shifts/upcoming`
- Returns list of upcoming assignments for authenticated user
Sample Implementation:
```java
@GetMapping("/shifts/upcoming")
public List<ShiftAssignmentDto> getUpcomingShifts(Authentication auth) { ... }
```

---

# E06 Leave & Absence Management

## User Story 1: Request Leave
Section: Leave Request Workflow  
Description: Employees can request PTO, sick, or unpaid leave.  
Design Specification:
- Entity: `LeaveRequest` (employeeId, type, startDate, endDate, status)
- Endpoint: POST `/leave/request`
Sample Implementation:
```java
@PostMapping("/leave/request")
public ResponseEntity<Void> requestLeave(@Valid @RequestBody LeaveRequestDto dto) { ... }
```

---

## User Story 2: Approve/Deny Requests
Section: Approval Workflow  
Description: Supervisors approve or deny leave requests.  
Design Specification:
- Endpoint: PUT `/leave/{id}/approve`, `/leave/{id}/deny`
- Updates status and notifies employee
Sample Implementation:
```java
@PutMapping("/leave/{id}/approve")
public ResponseEntity<Void> approve(@PathVariable Long id) { ... }
```

---

## User Story 3: Update Balances & Exclude from Scheduling
Section: Integration with Scheduling  
Description: Approved leaves update balances and exclude employees from shifts.  
Design Specification:
- Service updates leave balance
- Scheduling service checks leave status before assignment
Sample Implementation:
```java
if (isOnLeave(employeeId, date)) {
  throw new EmployeeOnLeaveException();
}
```

---

## User Story 4: Export Leave Reports
Section: Reporting  
Description: Exports approved leave data in CSV format.  
Design Specification:
- Endpoint: GET `/leave/report?format=csv`
Sample Implementation:
```java
@GetMapping("/leave/report")
public void exportLeaveReport(HttpServletResponse response) { ... }
```

---

# E07 Training & Certification Tracking

## User Story 1: Track Certifications
Section: Domain Model & CRUD  
Description: Tracks employee certifications and expirations.  
Design Specification:
- Entity: `Certification` (employeeId, type, issueDate, expiryDate, documentUrl)
- CRUD endpoints
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

## User Story 2: Alert on Expiring Certifications
Section: Notification Service  
Description: Sends alerts 30 and 7 days before certification expiry.  
Design Specification:
- Scheduled job checks expiry dates
- Sends notifications via E12 notification service
Sample Implementation:
```java
@Scheduled(cron = "0 0 8 * * ?")
public void checkExpiringCertifications() { ... }
```

---

## User Story 3: Block Assignment with Expired Certs
Section: Validation Logic  
Description: Prevents assignment to tasks requiring valid certifications.  
Design Specification:
- Service checks certification status before assignment
Sample Implementation:
```java
if (!hasValidCertification(employeeId, requiredCert)) {
  throw new InvalidCertificationException();
}
```

---

## User Story 4: Display Certification Status
Section: Employee Profile  
Description: Shows certification status on employee profile.  
Design Specification:
- Endpoint: GET `/employees/{id}/certifications`
Sample Implementation:
```java
@GetMapping("/employees/{id}/certifications")
public List<CertificationDto> getCertifications(@PathVariable Long id) { ... }
```

---

# E08 Safety Incidents & OSHA Reporting

## User Story 1: Record Incidents
Section: Domain Model & CRUD  
Description: Records safety incidents and near-misses.  
Design Specification:
- Entity: `SafetyIncident` (id, severity, location, description, involvedEmployees, status)
- Endpoint: POST `/safety/incidents`
Sample Implementation:
```java
@Entity
public class SafetyIncident {
  @Id @GeneratedValue
  private Long id;
  @Enumerated(EnumType.STRING)
  private Severity severity;
  private String location;
  private String description;
  @ManyToMany
  private List<Employee> involvedEmployees;
  @Enumerated(EnumType.STRING)
  private Status status;
}
```

---

## User Story 2: Manage Investigation Workflow
Section: Workflow Management  
Description: Tracks incident status through Open, Investigating, Resolved.  
Design Specification:
- Service methods: updateStatus(id, status)
Sample Implementation:
```java
public void updateStatus(Long id, Status newStatus) { ... }
```

---

## User Story 3: Export OSHA Reports
Section: Reporting  
Description: Generates OSHA 300/300A summary reports.  
Design Specification:
- Endpoint: GET `/safety/osha-report`
- Formats data per OSHA guidelines
Sample Implementation:
```java
@GetMapping("/safety/osha-report")
public void exportOshaReport(HttpServletResponse response) { ... }
```

---

## User Story 4: Safety Metrics Dashboard
Section: Analytics  
Description: Provides KPIs and metrics for safety incidents.  
Design Specification:
- Endpoint: GET `/safety/metrics`
Sample Implementation:
```java
@GetMapping("/safety/metrics")
public SafetyMetricsDto getMetrics() { ... }
```

---

# E09 Equipment & Asset Assignment

## User Story 1: Manage Asset Registry
Section: Domain Model & CRUD  
Description: Maintains registry of equipment and PPE.  
Design Specification:
- Entity: `Asset` (id, type, serialNumber, condition, location)
- CRUD endpoints
Sample Implementation:
```java
@Entity
public class Asset {
  @Id @GeneratedValue
  private Long id;
  private String type;
  private String serialNumber;
  @Enumerated(EnumType.STRING)
  private Condition condition;
  private String location;
}
```

---

## User Story 2: Assign & Track Check-In/Out
Section: Assignment Logic  
Description: Tracks asset check-in/out to employees.  
Design Specification:
- Entity: `AssetAssignment` (assetId, employeeId, checkOutDate, checkInDate)
- Endpoints: POST `/assets/{id}/checkout`, `/assets/{id}/checkin`
Sample Implementation:
```java
@PostMapping("/assets/{id}/checkout")
public ResponseEntity<Void> checkout(@PathVariable Long id, @RequestBody CheckoutDto dto) { ... }
```

---

## User Story 3: Prevent Use Without Certification
Section: Validation Logic  
Description: Blocks asset checkout if required certification is missing.  
Design Specification:
- Service checks certification before checkout
Sample Implementation:
```java
if (!hasValidCertification(employeeId, asset.getRequiredCert())) {
  throw new InvalidCertificationException();
}
```

---

## User Story 4: Report Overdue Returns
Section: Reporting  
Description: Generates reports of overdue asset returns.  
Design Specification:
- Endpoint: GET `/assets/overdue`
Sample Implementation:
```java
@GetMapping("/assets/overdue")
public List<AssetAssignmentDto> getOverdueAssets() { ... }
```

---

# E10 Performance Reviews & Goals

## User Story 1: Create Review Templates
Section: Domain Model & CRUD  
Description: Defines templates for performance reviews.  
Design Specification:
- Entity: `ReviewTemplate` (id, name, sections, competencies)
- CRUD endpoints
Sample Implementation:
```java
@Entity
public class ReviewTemplate {
  @Id @GeneratedValue
  private Long id;
  private String name;
  @ElementCollection
  private List<String> sections;
}
```

---

## User Story 2: Assign Reviews
Section: Assignment Logic  
Description: Assigns reviews to employees.  
Design Specification:
- Entity: `PerformanceReview` (employeeId, templateId, dueDate, status)
- Endpoint: POST `/reviews/assign`
Sample Implementation:
```java
@PostMapping("/reviews/assign")
public ResponseEntity<Void> assignReview(@RequestBody AssignReviewDto dto) { ... }
```

---

## User Story 3: Submit & Acknowledge Reviews
Section: Workflow Management  
Description: Employees view and acknowledge completed reviews.  
Design Specification:
- Endpoint: PUT `/reviews/{id}/acknowledge`
Sample Implementation:
```java
@PutMapping("/reviews/{id}/acknowledge")
public ResponseEntity<Void> acknowledge(@PathVariable Long id) { ... }
```

---

## User Story 4: Export Review Reports
Section: Reporting  
Description: Exports performance review data in PDF format.  
Design Specification:
- Endpoint: GET `/reviews/report?format=pdf`
Sample Implementation:
```java
@GetMapping("/reviews/report")
public void exportReviewReport(HttpServletResponse response) { ... }
```

---

# E11 Payroll Export Integration

## User Story 1: Export Payroll Data
Section: Integration & Export  
Description: Exports payroll data in standardized formats (CSV, XML).  
Design Specification:
- Endpoint: GET `/payroll/export?format=csv`
- Service aggregates attendance and leave data
Sample Implementation:
```java
@GetMapping("/payroll/export")
public void exportPayroll(@RequestParam String format, HttpServletResponse response) { ... }
```

---

## User Story 2: Schedule Automated Exports
Section: Scheduled Jobs  
Description: Automates payroll export on a schedule.  
Design Specification:
- Use `@Scheduled` annotation
- Configurable schedule in `application.yml`
Sample Implementation:
```java
@Scheduled(cron = "${payroll.export.schedule}")
public void scheduledExport() { ... }
```

---

## User Story 3: Export Error Handling
Section: Error Handling & Logging  
Description: Logs errors and sends notifications on export failure.  
Design Specification:
- Try-catch in export service
- Sends alert via E12 notification service
Sample Implementation:
```java
try {
  exportPayroll();
} catch (Exception e) {
  log.error("Payroll export failed", e);
  notificationService.sendAlert(...);
}
```

---

## User Story 4: Reconcile Payroll with Attendance
Section: Validation & Reconciliation  
Description: Compares exported payroll data with attendance records.  
Design Specification:
- Service method: reconcile(payrollData, attendanceData)
- Generates variance report
Sample Implementation:
```java
public ReconciliationReport reconcile(PayrollData payroll, AttendanceData attendance) { ... }
```

---

# E12 Notifications & Announcements

## User Story 1: Send Announcements
Section: Notification Service  
Description: Sends announcements to all users via in-app and email.  
Design Specification:
- Entity: `Announcement` (id, title, content, createdAt)
- Endpoint: POST `/notifications/announcements`
Sample Implementation:
```java
@PostMapping("/notifications/announcements")
public ResponseEntity<Void> sendAnnouncement(@RequestBody AnnouncementDto dto) { ... }
```

---

## User Story 2: Targeted Notifications by Role
Section: Role-Based Notifications  
Description: Sends notifications to specific user roles.  
Design Specification:
- Endpoint: POST `/notifications/send`
- DTO includes target roles
Sample Implementation:
```java
public void sendNotification(NotificationDto dto) {
  List<User> users = userRepository.findByRoleIn(dto.getTargetRoles());
  ...
}
```

---

## User Story 3: Track Notification Reads
Section: Read Status Tracking  
Description: Tracks which users have read announcements.  
Design Specification:
- Entity: `NotificationRead` (notificationId, userId, readAt)
- Endpoint: PUT `/notifications/{id}/read`
Sample Implementation:
```java
@PutMapping("/notifications/{id}/read")
public ResponseEntity<Void> markAsRead(@PathVariable Long id, Authentication auth) { ... }
```

---

## User Story 4: Configure Quiet Hours
Section: User Preferences  
Description: Allows users to set quiet hours for notifications.  
Design Specification:
- Entity: `UserPreferences` (userId, quietHoursStart, quietHoursEnd)
- Service checks quiet hours before sending
Sample Implementation:
```java
if (isQuietHours(user)) {
  queueNotification(notification);
} else {
  sendNotification(notification);
}
```

---

# E13 Integration Layer (HRIS/WMS APIs)

## User Story 1: Integrate with HRIS
Section: External API Integration  
Description: Syncs employee data with external HRIS via API.  
Design Specification:
- Use `RestTemplate` or `WebClient`
- Scheduled job for sync
Sample Implementation:
```java
@Scheduled(cron = "0 0 2 * * ?")
public void syncWithHris() {
  List<EmployeeDto> hrisEmployees = hrisClient.getEmployees();
  ...
}
```

---

## User Story 2: WMS Integration
Section: External API Integration  
Description: Integrates with WMS for inventory updates.  
Design Specification:
- Use `WebClient` for async calls
- Handle API failures gracefully
Sample Implementation:
```java
public void updateInventory(InventoryDto dto) {
  webClient.post().uri("/inventory").bodyValue(dto).retrieve().bodyToMono(Void.class).block();
}
```

---

## User Story 3: API Error Handling & Retry
Section: Resilience & Retry Logic  
Description: Implements retry logic for transient API failures.  
Design Specification:
- Use Spring Retry (`@Retryable`)
- Configurable retry policy
Sample Implementation:
```java
@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
public void callExternalApi() { ... }
```

---

## User Story 4: Expose Webhooks
Section: Webhook Endpoints  
Description: Exposes webhooks for external systems to subscribe to events.  
Design Specification:
- Endpoint: POST `/webhooks/subscribe`
- Entity: `WebhookSubscription` (url, events)
Sample Implementation:
```java
@PostMapping("/webhooks/subscribe")
public ResponseEntity<Void> subscribe(@RequestBody WebhookSubscriptionDto dto) { ... }
```

---

# E14 Audit Trail & Compliance

## User Story 1: Maintain Audit Trail
Section: Audit Logging  
Description: Logs all user actions for compliance.  
Design Specification:
- Entity: `AuditLog` (id, userId, action, entity, timestamp, before, after)
- Use Spring AOP to intercept and log actions
Sample Implementation:
```java
@Aspect
public class AuditAspect {
  @AfterReturning("@annotation(Auditable)")
  public void logAction(JoinPoint jp) { ... }
}
```

---

## User Story 2: Export Audit Logs
Section: Reporting  
Description: Exports audit logs for compliance review.  
Design Specification:
- Endpoint: GET `/audit/export?format=csv`
Sample Implementation:
```java
@GetMapping("/audit/export")
public void exportAuditLogs(HttpServletResponse response) { ... }
```

---

## User Story 3: Alert on Suspicious Activity
Section: Security Monitoring  
Description: Detects and alerts on unusual patterns.  
Design Specification:
- Scheduled job analyzes audit logs
- Sends alerts via E12 notification service
Sample Implementation:
```java
@Scheduled(cron = "0 0 * * * ?")
public void detectSuspiciousActivity() { ... }
```

---

## User Story 4: Tamper-Evident Audit Storage
Section: Data Integrity  
Description: Ensures audit logs cannot be modified.  
Design Specification:
- Use cryptographic hashing or blockchain
- Store hash with each log entry
Sample Implementation:
```java
public void saveAuditLog(AuditLog log) {
  log.setHash(computeHash(log));
  auditLogRepository.save(log);
}
```

---

# E15 Reporting & Analytics

## User Story 1: Generate Custom Reports
Section: Reporting Engine  
Description: Allows users to generate custom reports with filters.  
Design Specification:
- Endpoint: POST `/reports/generate`
- DTO includes fields, filters, format
Sample Implementation:
```java
@PostMapping("/reports/generate")
public void generateReport(@RequestBody ReportRequestDto dto, HttpServletResponse response) { ... }
```

---

## User Story 2: Schedule Automated Report Delivery
Section: Scheduled Reporting  
Description: Automates report generation and delivery via email.  
Design Specification:
- Entity: `ScheduledReport` (reportConfig, schedule, recipients)
- Scheduled job generates and sends reports
Sample Implementation:
```java
@Scheduled(cron = "0 0 8 * * ?")
public void sendScheduledReports() { ... }
```

---

## User Story 3: KPI Dashboard
Section: Analytics Dashboard  
Description: Displays key performance indicators.  
Design Specification:
- Endpoint: GET `/reports/dashboard`
- Returns aggregated metrics
Sample Implementation:
```java
@GetMapping("/reports/dashboard")
public DashboardDto getDashboard() { ... }
```

---

## User Story 4: Export in Multiple Formats
Section: Export Formats  
Description: Supports export in CSV and PDF formats.  
Design Specification:
- Service generates reports in requested format
Sample Implementation:
```java
if ("csv".equals(format)) {
  generateCsv(response);
} else if ("pdf".equals(format)) {
  generatePdf(response);
}
```

---

# E16 Mobile Access (PWA)

## User Story 1: Access via Mobile PWA
Section: Progressive Web App  
Description: Provides mobile-friendly access to core features.  
Design Specification:
- Add `manifest.json` and service worker
- Responsive UI with Bootstrap or Material Design
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

## User Story 2: Push Notifications
Section: Push Notification Service  
Description: Sends push notifications to mobile devices.  
Design Specification:
- Use Web Push API
- Service worker handles notifications
Sample Implementation:
```javascript
self.addEventListener('push', event => {
  const data = event.data.json();
  self.registration.showNotification(data.title, { body: data.body });
});
```

---

## User Story 3: Optimize UI for Scanning
Section: Mobile UI Optimization  
Description: Optimizes UI for barcode scanning workflows.  
Design Specification:
- Use camera API for barcode scanning
- Large buttons and touch-friendly controls
Sample Implementation:
```javascript
navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })
  .then(stream => { ... });
```

---

## User Story 4: Offline Mode
Section: Offline Functionality  
Description: Supports core functions offline with sync on reconnect.  
Design Specification:
- Use IndexedDB for local storage
- Service worker caches API responses
Sample Implementation:
```javascript
self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request).then(response => response || fetch(event.request))
  );
});
```

---

# E17 Onboarding & Offboarding Workflow

## User Story 1: Automate Onboarding Tasks
Section: Workflow Automation  
Description: Automatically assigns onboarding tasks to new hires.  
Design Specification:
- Entity: `OnboardingTask` (employeeId, taskName, assignedTo, status)
- Triggered by HRIS integration
Sample Implementation:
```java
public void onboardEmployee(Employee employee) {
  List<OnboardingTask> tasks = generateOnboardingTasks(employee);
  onboardingTaskRepository.saveAll(tasks);
}
```

---

## User Story 2: Offboarding Checklist Automation
Section: Workflow Automation  
Description: Triggers offboarding checklist on employee departure.  
Design Specification:
- Entity: `OffboardingTask` (employeeId, taskName, assignedTo, status)
- Triggered by status change to TERMINATED
Sample Implementation:
```java
public void offboardEmployee(Employee employee) {
  List<OffboardingTask> tasks = generateOffboardingTasks(employee);
  offboardingTaskRepository.saveAll(tasks);
}
```

---

## User Story 3: Track Onboarding/Offboarding Progress
Section: Progress Dashboard  
Description: Displays status of onboarding/offboarding tasks.  
Design Specification:
- Endpoint: GET `/onboarding/progress/{employeeId}`
Sample Implementation:
```java
@GetMapping("/onboarding/progress/{employeeId}")
public ProgressDto getProgress(@PathVariable Long employeeId) { ... }
```

---

## User Story 4: Integrate with HRIS
Section: HRIS Integration  
Description: Automatically triggers onboarding when new hire appears in HRIS.  
Design Specification:
- Webhook or scheduled sync from HRIS
- Calls onboarding service
Sample Implementation:
```java
@PostMapping("/webhooks/hris/new-hire")
public ResponseEntity<Void> handleNewHire(@RequestBody EmployeeDto dto) {
  onboardingService.onboardEmployee(dto);
  return ResponseEntity.ok().build();
}
```

---

# E18 Localization & Multi-Warehouse

## User Story 1: Support Multiple Languages
Section: Internationalization (i18n)  
Description: Supports multiple languages (en, es, fr).  
Design Specification:
- Use Spring `MessageSource`
- Externalize strings in `messages_en.properties`, `messages_es.properties`, etc.
Sample Implementation:
```java
@Bean
public MessageSource messageSource() {
  ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
  messageSource.setBasename("messages");
  return messageSource;
}
```

---

## User Story 2: Configure Warehouse-Specific Settings
Section: Multi-Tenancy  
Description: Allows configuration per warehouse (time zone, currency, policies).  
Design Specification:
- Entity: `WarehouseConfig` (warehouseId, timeZone, currency, policies)
- Service retrieves config based on warehouse context
Sample Implementation:
```java
public WarehouseConfig getConfig(Long warehouseId) {
  return warehouseConfigRepository.findByWarehouseId(warehouseId).orElseThrow(...);
}
```

---

## User Story 3: Translate Notifications
Section: Localized Notifications  
Description: Sends notifications in user's preferred language.  
Design Specification:
- User preferences include language
- Notification service uses `MessageSource` to translate
Sample Implementation:
```java
String message = messageSource.getMessage("notification.shift.change", null, user.getLocale());
```

---

## User Story 4: Assign Employees to Warehouses
Section: Employee-Warehouse Mapping  
Description: Assigns employees to specific warehouses.  
Design Specification:
- Add `warehouseId` to Employee entity
- Filter queries by warehouse context
Sample Implementation:
```java
public List<Employee> getEmployeesByWarehouse(Long warehouseId) {
  return employeeRepository.findByWarehouseId(warehouseId);
}
```

---

# E19 Observability & Monitoring

## User Story 1: Monitor System Health
Section: Monitoring & Metrics  
Description: Monitors system health metrics (CPU, memory, uptime).  
Design Specification:
- Use Spring Boot Actuator and Micrometer
- Expose metrics to Prometheus
Sample Implementation:
```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
```

---

## User Story 2: Alert on Critical Failures
Section: Alerting  
Description: Sends alerts on critical system failures.  
Design Specification:
- Use Actuator health checks
- Integrate with alerting service (e.g., PagerDuty)
Sample Implementation:
```java
@Component
public class HealthCheckAlertService {
  @Scheduled(fixedRate = 60000)
  public void checkHealth() {
    if (!isHealthy()) {
      sendAlert();
    }
  }
}
```

---

## User Story 3: Log Application Errors
Section: Centralized Logging  
Description: Logs application errors with stack traces.  
Design Specification:
- Use SLF4J with Logback
- Send logs to centralized logging platform (e.g., ELK)
Sample Implementation:
```java
try {
  ...
} catch (Exception e) {
  log.error("Error processing request", e);
}
```

---

## User Story 4: Implement Distributed Tracing
Section: Distributed Tracing  
Description: Traces requests across services with trace IDs.  
Design Specification:
- Use Spring Cloud Sleuth and Zipkin
- Propagate trace IDs in logs
Sample Implementation:
```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
```

---

# E20 Deployment & CI/CD

## User Story 1: Automate Deployment
Section: CI/CD Pipeline  
Description: Automates deployment to staging and production.  
Design Specification:
- Use GitHub Actions or Jenkins
- Pipeline stages: build, test, deploy
Sample Implementation:
```yaml
name: CI/CD
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build
        run: mvn clean install
      - name: Deploy
        run: ./deploy.sh
```

---

## User Story 2: Run Automated Tests in CI
Section: Continuous Integration  
Description: Runs automated tests on every commit.  
Design Specification:
- Pipeline runs `mvn test`
- Blocks merge on test failure
Sample Implementation:
```yaml
- name: Test
  run: mvn test
```

---

## User Story 3: Deploy Feature Flags
Section: Feature Management  
Description: Uses feature flags for controlled rollout.  
Design Specification:
- Use library like Togglz or LaunchDarkly
- Configure flags in `application.yml`
Sample Implementation:
```java
if (featureToggle.isEnabled("new-feature")) {
  // new feature code
}
```

---

## User Story 4: Deployment Rollback on Failure
Section: Rollback Strategy  
Description: Automatically rolls back on deployment failure.  
Design Specification:
- Pipeline checks health after deployment
- Rolls back to previous version on failure
Sample Implementation:
```bash
if ! curl -f http://app/actuator/health; then
  kubectl rollout undo deployment/warehouse-ems
fi
```

---

## Conclusion

This comprehensive low-level technical design document provides detailed specifications for all 80 user stories across 20 epics of the Warehouse Employee Management System. Each section includes Spring Boot architecture overviews, package structures, domain models, service/repository/controller designs, security configurations, integration points, and sample code implementations. Development teams can use this document as a blueprint for implementing the system following industry best practices and Spring Boot conventions.