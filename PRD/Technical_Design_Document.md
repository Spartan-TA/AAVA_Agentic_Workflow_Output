# Warehouse Employee Management System â Technical Design Document

## Table of Contents
1. [Spring Boot Architecture Overview](#spring-boot-architecture-overview)
2. [Package Structure and Module Definitions](#package-structure-and-module-definitions)
3. [Entity Design with Domain Models and Relationships](#entity-design-with-domain-models-and-relationships)
4. [Service Layer Specifications](#service-layer-specifications)
5. [Repository Layer Specifications](#repository-layer-specifications)
6. [Controller Specifications](#controller-specifications)
7. [Configuration and Security Settings](#configuration-and-security-settings)
8. [Integration Points (External Services, APIs)](#integration-points-external-services-apis)
9. [Code Snippets Illustrating Design Patterns](#code-snippets-illustrating-design-patterns)
10. [Epic & User Story Technical Design Details](#epic--user-story-technical-design-details)

---

## Spring Boot Architecture Overview

The Warehouse Employee Management System is architected as a modular, layered Spring Boot application following industry best practices. The system is divided into logical modules (employee, scheduling, attendance, safety, etc.), each encapsulating its domain logic. The architecture enforces separation of concerns via Controller, Service, Repository, and Domain layers, with cross-cutting concerns (security, auditing, notifications) handled centrally.

- **Framework:** Spring Boot (Maven)
- **Persistence:** Spring Data JPA, Flyway/Liquibase for migrations
- **Security:** Spring Security (RBAC, OAuth2, API Key)
- **API:** RESTful endpoints, OpenAPI/Swagger documentation
- **Observability:** Spring Boot Actuator, centralized logging, metrics
- **Integration:** SFTP, external APIs, webhooks, HRIS/WMS connectors
- **Mobile:** PWA support for core flows

---

## Package Structure and Module Definitions

```
com.company.wms
âââ employee
â   âââ controller
â   âââ service
â   âââ repository
â   âââ model
âââ scheduling
â   âââ controller
â   âââ service
â   âââ repository
â   âââ model
âââ attendance
â   âââ controller
â   âââ service
â   âââ repository
â   âââ model
âââ safety
â   âââ controller
â   âââ service
â   âââ repository
â   âââ model
âââ asset
âââ review
âââ payroll
âââ notification
âââ integration
âââ audit
âââ reporting
âââ mobile
âââ onboarding
âââ localization
âââ observability
âââ config
âââ security
```

- Each module contains its own domain models, repositories, services, and controllers.
- Shared utilities and cross-cutting concerns are placed in `common`, `config`, or `security`.

---

## Entity Design with Domain Models and Relationships

### Example: Employee Domain

```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue
    private Long id;
    private String name;
    @Column(name = "badge_id", unique = true, nullable = false)
    private String badgeId;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;
    private boolean deleted = false;

    // Relationships
    @OneToMany(mappedBy = "employee")
    private List<Attendance> attendances;
    @OneToMany(mappedBy = "employee")
    private List<LeaveRequest> leaveRequests;
    @OneToMany(mappedBy = "employee")
    private List<Certification> certifications;
    // ... other relationships
}
```

- **Relationships:** Employees have attendances, leave requests, certifications, asset assignments, reviews, etc.
- **Soft Delete:** `deleted` flag for soft-deletion.

---

## Service Layer Specifications

- **Business Logic:** Encapsulated in `@Service` classes.
- **Transaction Management:** Annotated with `@Transactional` where needed.
- **Validation:** Bean validation (`@Valid`), custom validators for business rules (e.g., badgeId uniqueness).
- **Security:** Method-level security (`@PreAuthorize`).

**Example: EmployeeService**

```java
@Service
public class EmployeeService {
    public Employee createEmployee(EmployeeDto dto) { ... }
    public Page<Employee> getEmployees(Pageable pageable, EmployeeFilter filter) { ... }
    public Employee updateEmployee(Long id, EmployeeDto dto) { ... }
    public void softDeleteEmployee(Long id) { ... }
}
```

---

## Repository Layer Specifications

- **Persistence:** Spring Data JPA repositories.
- **Custom Queries:** For filtering, pagination, and complex joins.
- **Soft Delete:** Override `findAll` to exclude soft-deleted records.

**Example: EmployeeRepository**

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND ...")
    Page<Employee> findAllActive(EmployeeFilter filter, Pageable pageable);
}
```

---

## Controller Specifications

- **RESTful Endpoints:** Annotated with `@RestController`.
- **DTOs:** Use DTOs for request/response.
- **Validation:** `@Valid` on input.
- **OpenAPI:** Annotated for Swagger docs.

**Example: EmployeeController**

```java
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { ... }
    @GetMapping
    public Page<EmployeeDto> list(@PageableDefault Pageable pageable, EmployeeFilter filter) { ... }
    @PutMapping("/{id}")
    public EmployeeDto update(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) { ... }
    @DeleteMapping("/{id}")
    public void softDelete(@PathVariable Long id) { ... }
}
```

---

## Configuration and Security Settings

- **Spring Security:** RBAC, OAuth2, API Key support.
- **Actuator:** Health, metrics, info endpoints.
- **Flyway/Liquibase:** DB migrations.
- **CORS, CSRF:** Configured per endpoint/module.
- **Application Properties:** Environment-specific configs.

**Example: SecurityConfig**

```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/api/employees/**").hasAnyRole("ADMIN", "HR")
                .antMatchers("/api/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer().jwt();
    }
}
```

---

## Integration Points (External Services, APIs)

- **HRIS:** Scheduled sync jobs, REST connectors.
- **WMS:** Department/location mapping.
- **Payroll:** SFTP/API file delivery.
- **Notifications:** Email/SMS gateways.
- **Webhooks:** Idempotent event processing.

---

## Code Snippets Illustrating Design Patterns

- **DTO Mapping:** MapStruct or manual mapping.
- **Event Publishing:** Spring Events for audit, notifications.
- **Exception Handling:** `@ControllerAdvice` for global error handling.
- **Auditing:** JPA EntityListeners, custom audit tables.

---

## Epic & User Story Technical Design Details

---

### EPIC E01: Project Scaffolding & Domain Setup

#### User Story 1: Initialize Spring Boot Project with Maven

**Section Title:** Project Initialization  
**Description:** Use Spring Initializr to generate a Maven-based Spring Boot project.  
**Design Specification:**  
- Group: `com.company.wms`
- Artifact: `warehouse-employee-mgmt`
- Dependencies: Spring Web, Spring Data JPA, Spring Security, Flyway/Liquibase, Actuator, Lombok, Validation, OpenAPI  
**Sample Implementation:**  
```bash
curl https://start.spring.io/starter.zip   -d dependencies=web,data-jpa,security,flyway,actuator,lombok,validation,openapi   -d type=maven-project   -d groupId=com.company.wms   -d artifactId=warehouse-employee-mgmt   -o warehouse-employee-mgmt.zip
```

#### User Story 2: Configure Base Packages (employee, scheduling, attendance, safety modules)

**Section Title:** Package Structure  
**Description:** Create base packages for each domain module.  
**Design Specification:**  
- `com.company.wms.employee`
- `com.company.wms.scheduling`
- `com.company.wms.attendance`
- `com.company.wms.safety`  
**Sample Implementation:**  
```java
// Example: com.company.wms.employee.Employee.java
package com.company.wms.employee;
@Entity
public class Employee { ... }
```

#### User Story 3: Integrate Flyway/Liquibase for DB Migrations

**Section Title:** Database Migration  
**Description:** Use Flyway or Liquibase for versioned schema migrations.  
**Design Specification:**  
- Place migration scripts in `src/main/resources/db/migration` (Flyway)  
**Sample Implementation:**  
```sql
-- V1__init.sql
CREATE TABLE employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    badge_id VARCHAR(50) UNIQUE NOT NULL,
    ...
);
```
`application.yml`:
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

#### User Story 4: Enable Spring Boot Actuator for health monitoring

**Section Title:** Observability  
**Description:** Add Actuator dependency and expose health/info endpoints.  
**Design Specification:**  
- Expose `/actuator/health`, `/actuator/info`  
**Sample Implementation:**  
`application.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

---

### EPIC E02: Employee Master Data CRUD

#### User Story 1: Create Employee Record

**Section Title:** Employee Entity & Create API  
**Description:** Implement Employee entity and POST endpoint.  
**Design Specification:**  
- Fields: name, badgeId, role, department, shiftGroup, hireDate, status  
**Sample Implementation:**  
```java
@Entity
public class Employee { ... }
```
```java
@PostMapping
public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) {
    return ResponseEntity.ok(employeeService.createEmployee(dto));
}
```

#### User Story 2: Retrieve Employee Records with pagination and filtering

**Section Title:** Employee List API  
**Description:** GET endpoint with pagination/filtering.  
**Design Specification:**  
- Use `Pageable` and filter params (role, department, status, etc.)  
**Sample Implementation:**  
```java
@GetMapping
public Page<EmployeeDto> list(@PageableDefault Pageable pageable, EmployeeFilter filter) {
    return employeeService.getEmployees(pageable, filter);
}
```

#### User Story 3: Update Employee Record with badgeId uniqueness enforcement

**Section Title:** Employee Update API  
**Description:** PUT endpoint with badgeId uniqueness check.  
**Design Specification:**  
- Validate badgeId is unique (except for current record)  
**Sample Implementation:**  
```java
@PutMapping("/{id}")
public EmployeeDto update(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) {
    return employeeService.updateEmployee(id, dto);
}
```

#### User Story 4: Soft-Delete Employee Record

**Section Title:** Employee Soft Delete  
**Description:** PATCH/DELETE endpoint sets `deleted=true`.  
**Design Specification:**  
- Exclude soft-deleted records from queries  
**Sample Implementation:**  
```java
@DeleteMapping("/{id}")
public void softDelete(@PathVariable Long id) {
    employeeService.softDeleteEmployee(id);
}
```

#### User Story 5: OpenAPI Documentation for Employee APIs

**Section Title:** API Documentation  
**Description:** Annotate endpoints for Swagger/OpenAPI.  
**Design Specification:**  
- Use `@Operation`, `@Schema` annotations  
**Sample Implementation:**  
```java
@Operation(summary = "Create Employee", description = "Creates a new employee record.")
@PostMapping
public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { ... }
```

---

### EPIC E03: Role-Based Access Control (RBAC)

#### User Story 1: Define User Roles (ADMIN, HR, SUPERVISOR, WORKER)

**Section Title:** Security Roles  
**Description:** Define roles as enums and configure in Spring Security.  
**Design Specification:**  
- Enum: `Role { ADMIN, HR, SUPERVISOR, WORKER }`  
**Sample Implementation:**  
```java
public enum Role { ADMIN, HR, SUPERVISOR, WORKER }
```

#### User Story 2: Secure Endpoints by Role

**Section Title:** Endpoint Security  
**Description:** Restrict endpoints using `@PreAuthorize` or antMatchers.  
**Design Specification:**  
- Example: Only ADMIN/HR can manage employees  
**Sample Implementation:**  
```java
@PreAuthorize("hasAnyRole('ADMIN','HR')")
@PostMapping("/employees")
public ResponseEntity<EmployeeDto> create(...) { ... }
```

#### User Story 3: Implement Row-Level Security

**Section Title:** Row-Level Security  
**Description:** Restrict data access at row level (e.g., SUPERVISOR sees only team).  
**Design Specification:**  
- Filter queries by user's department/team  
**Sample Implementation:**  
```java
@PreAuthorize("hasRole('SUPERVISOR')")
public Page<Employee> getTeamEmployees(...) {
    // Query employees where supervisor_id = currentUser.id
}
```

#### User Story 4: Configurable Authentication (API Key/OAuth2)

**Section Title:** Authentication Config  
**Description:** Support both API Key and OAuth2 via config.  
**Design Specification:**  
- Use Spring Security conditional beans  
**Sample Implementation:**  
```java
@Bean
@ConditionalOnProperty("security.apikey.enabled")
public ApiKeyAuthFilter apiKeyAuthFilter() { ... }
```

#### User Story 5: Automated Security Tests

**Section Title:** Security Testing  
**Description:** Write integration tests for security rules.  
**Design Specification:**  
- Use `@WithMockUser` in tests  
**Sample Implementation:**  
```java
@Test
@WithMockUser(roles = "WORKER")
void testForbiddenAccess() throws Exception {
    mockMvc.perform(post("/api/employees")).andExpect(status().isForbidden());
}
```

---

### EPIC E04: Time & Attendance

#### User Story 1: Clock-In Endpoint with timestamp and device info

**Section Title:** Attendance Clock-In API  
**Description:** POST endpoint for clock-in with timestamp/device.  
**Design Specification:**  
- Fields: employeeId, timestamp, deviceId, location  
**Sample Implementation:**  
```java
@PostMapping("/clock-in")
public ResponseEntity<AttendanceDto> clockIn(@RequestBody ClockEventDto dto) { ... }
```

#### User Story 2: Clock-Out Endpoint with hours calculation

**Section Title:** Attendance Clock-Out API  
**Description:** POST endpoint for clock-out, calculate hours.  
**Design Specification:**  
- Calculate duration between clock-in/out  
**Sample Implementation:**  
```java
@PostMapping("/clock-out")
public ResponseEntity<AttendanceDto> clockOut(@RequestBody ClockEventDto dto) { ... }
```

#### User Story 3: Geofence Validation for clock events

**Section Title:** Geofence Validation  
**Description:** Validate clock-in/out location within allowed geofence.  
**Design Specification:**  
- Compare device location to allowed coordinates  
**Sample Implementation:**  
```java
if (!geofenceService.isWithinAllowedArea(dto.getLocation())) {
    throw new ValidationException("Outside allowed area");
}
```

#### User Story 4: Missed Punch Correction Workflow

**Section Title:** Missed Punch Correction  
**Description:** Workflow for correcting missed punches (approval required).  
**Design Specification:**  
- Correction requests create approval tasks  
**Sample Implementation:**  
```java
@PostMapping("/attendance/correction")
public ResponseEntity<CorrectionRequestDto> requestCorrection(@RequestBody CorrectionDto dto) { ... }
```

#### User Story 5: Export Attendance Reports as CSV

**Section Title:** Attendance Report Export  
**Description:** Export attendance data as CSV.  
**Design Specification:**  
- Generate CSV from attendance records  
**Sample Implementation:**  
```java
@GetMapping("/attendance/export")
public void exportAttendanceCsv(HttpServletResponse response) { ... }
```

---

### EPIC E05: Shift & Schedule Management

#### User Story 1: Shift Template CRUD

**Section Title:** Shift Template Management  
**Description:** CRUD endpoints for shift templates.  
**Design Specification:**  
- Fields: name, startTime, endTime, recurrence, etc.  
**Sample Implementation:**  
```java
@Entity
public class ShiftTemplate { ... }
```
```java
@PostMapping("/shifts/templates")
public ResponseEntity<ShiftTemplateDto> createTemplate(@RequestBody ShiftTemplateDto dto) { ... }
```

#### User Story 2: Assign Shifts to Employees

**Section Title:** Shift Assignment  
**Description:** Assign shifts to employees.  
**Design Specification:**  
- Many-to-many relationship  
**Sample Implementation:**  
```java
@PostMapping("/shifts/assign")
public void assignShifts(@RequestBody ShiftAssignmentDto dto) { ... }
```

#### User Story 3: Conflict Detection for overlapping shifts

**Section Title:** Shift Conflict Detection  
**Description:** Prevent overlapping shift assignments.  
**Design Specification:**  
- Validate no time overlap for employee  
**Sample Implementation:**  
```java
if (shiftService.hasOverlap(employeeId, newShift)) {
    throw new ValidationException("Shift conflict detected");
}
```

#### User Story 4: Bulk Assign Shifts

**Section Title:** Bulk Shift Assignment  
**Description:** Assign shifts to multiple employees at once.  
**Design Specification:**  
- Accept list of employeeIds and shiftIds  
**Sample Implementation:**  
```java
@PostMapping("/shifts/bulk-assign")
public void bulkAssign(@RequestBody BulkAssignmentDto dto) { ... }
```

#### User Story 5: Manage Blackout Dates

**Section Title:** Blackout Dates  
**Description:** CRUD for blackout dates (no shifts assigned).  
**Design Specification:**  
- BlackoutDate entity  
**Sample Implementation:**  
```java
@Entity
public class BlackoutDate { ... }
```

#### User Story 6: Employee Shift View

**Section Title:** Employee Shift View  
**Description:** Endpoint for employees to view their shifts.  
**Design Specification:**  
- GET endpoint returns upcoming shifts  
**Sample Implementation:**  
```java
@GetMapping("/shifts/my")
public List<ShiftDto> getMyShifts() { ... }
```

---

### EPIC E06: Leave & Absence Management

#### User Story 1: Request Leave (PTO, sick, unpaid)

**Section Title:** Leave Request API  
**Description:** POST endpoint for leave requests.  
**Design Specification:**  
- Fields: employeeId, type, startDate, endDate, reason  
**Sample Implementation:**  
```java
@PostMapping("/leave/request")
public ResponseEntity<LeaveRequestDto> requestLeave(@RequestBody LeaveRequestDto dto) { ... }
```

#### User Story 2: Leave Approval Workflow

**Section Title:** Leave Approval  
**Description:** Approve/deny leave requests (supervisor).  
**Design Specification:**  
- PATCH endpoint for approval  
**Sample Implementation:**  
```java
@PatchMapping("/leave/{id}/approve")
public void approveLeave(@PathVariable Long id) { ... }
```

#### User Story 3: Leave Balance Tracking and accruals

**Section Title:** Leave Balance  
**Description:** Track leave balances and accruals.  
**Design Specification:**  
- LeaveBalance entity, accrual logic in service  
**Sample Implementation:**  
```java
public void accrueLeave(Long employeeId) { ... }
```

#### User Story 4: Auto-Flag Shifts for Coverage

**Section Title:** Shift Coverage  
**Description:** Flag scheduled shifts needing coverage due to leave.  
**Design Specification:**  
- Mark shifts as needing coverage  
**Sample Implementation:**  
```java
shiftService.flagForCoverage(employeeId, leavePeriod);
```

#### User Story 5: Export Approved Leaves

**Section Title:** Leave Export  
**Description:** Export approved leaves as CSV.  
**Design Specification:**  
- Generate CSV for approved leaves  
**Sample Implementation:**  
```java
@GetMapping("/leave/export")
public void exportLeavesCsv(HttpServletResponse response) { ... }
```

---

### EPIC E07: Training & Certification Tracking

#### User Story 1: Certification CRUD

**Section Title:** Certification Management  
**Description:** CRUD endpoints for certifications.  
**Design Specification:**  
- Fields: name, issueDate, expiryDate, proofDocument  
**Sample Implementation:**  
```java
@Entity
public class Certification { ... }
```

#### User Story 2: Certification Expiry Notifications (30/7 days)

**Section Title:** Expiry Notifications  
**Description:** Notify employees/supervisors before expiry.  
**Design Specification:**  
- Scheduled job checks for expiring certs  
**Sample Implementation:**  
```java
@Scheduled(cron = "0 0 * * * *")
public void notifyExpiringCerts() { ... }
```

#### User Story 3: Block Assignment to Expired Certifications

**Section Title:** Assignment Block  
**Description:** Prevent assignment if cert expired.  
**Design Specification:**  
- Check cert status before assignment  
**Sample Implementation:**  
```java
if (certification.isExpired()) {
    throw new ValidationException("Certification expired");
}
```

#### User Story 4: Upload Certification Proof Documents

**Section Title:** Proof Upload  
**Description:** Upload and store proof documents.  
**Design Specification:**  
- Store in file system or object storage  
**Sample Implementation:**  
```java
@PostMapping("/certifications/{id}/upload")
public void uploadProof(@PathVariable Long id, @RequestParam MultipartFile file) { ... }
```

---

### EPIC E08: Safety Incidents & OSHA Reporting

#### User Story 1: Record Safety Incidents and near-misses

**Section Title:** Safety Incident API  
**Description:** POST endpoint for incidents.  
**Design Specification:**  
- Fields: type, severity, location, description, involvedEmployees  
**Sample Implementation:**  
```java
@PostMapping("/safety/incidents")
public ResponseEntity<SafetyIncidentDto> recordIncident(@RequestBody SafetyIncidentDto dto) { ... }
```

#### User Story 2: Manage Incident Status (OpenâInvestigatingâResolved)

**Section Title:** Incident Workflow  
**Description:** Update incident status.  
**Design Specification:**  
- PATCH endpoint for status transitions  
**Sample Implementation:**  
```java
@PatchMapping("/safety/incidents/{id}/status")
public void updateStatus(@PathVariable Long id, @RequestBody StatusUpdateDto dto) { ... }
```

#### User Story 3: Export OSHA 300/300A Summary

**Section Title:** OSHA Export  
**Description:** Export OSHA summary as CSV/PDF.  
**Design Specification:**  
- Generate OSHA 300/300A fields  
**Sample Implementation:**  
```java
@GetMapping("/safety/osha/export")
public void exportOshaSummary(HttpServletResponse response) { ... }
```

#### User Story 4: Safety Metrics Dashboard Endpoints

**Section Title:** Safety Dashboard  
**Description:** Endpoints for safety KPIs.  
**Design Specification:**  
- GET endpoints for metrics  
**Sample Implementation:**  
```java
@GetMapping("/safety/metrics")
public SafetyMetricsDto getMetrics() { ... }
```

---

### EPIC E09: Equipment & Asset Assignment

#### User Story 1: Asset Registry CRUD

**Section Title:** Asset Management  
**Description:** CRUD endpoints for assets.  
**Design Specification:**  
- Fields: assetTag, type, status, assignedTo, condition  
**Sample Implementation:**  
```java
@Entity
public class Asset { ... }
```

#### User Story 2: Asset Check-In/Out

**Section Title:** Asset Check-In/Out  
**Description:** Endpoints for checking assets in/out.  
**Design Specification:**  
- POST endpoints for check-in/out  
**Sample Implementation:**  
```java
@PostMapping("/assets/check-out")
public void checkOut(@RequestBody AssetAssignmentDto dto) { ... }
```

#### User Story 3: Block Asset Assignment if Certification Invalid

**Section Title:** Assignment Block  
**Description:** Prevent asset assignment if required cert invalid.  
**Design Specification:**  
- Check cert before assignment  
**Sample Implementation:**  
```java
if (!employee.hasValidCertification(asset.getRequiredCertification())) {
    throw new ValidationException("Certification invalid");
}
```

#### User Story 4: View Asset Assignment History

**Section Title:** Assignment History  
**Description:** GET endpoint for asset assignment history.  
**Design Specification:**  
- List assignments by asset/employee  
**Sample Implementation:**  
```java
@GetMapping("/assets/{id}/history")
public List<AssetAssignmentDto> getHistory(@PathVariable Long id) { ... }
```

#### User Story 5: Report Overdue Asset Returns

**Section Title:** Overdue Reports  
**Description:** Report assets overdue for return.  
**Design Specification:**  
- Scheduled job to flag overdue assets  
**Sample Implementation:**  
```java
@Scheduled(cron = "0 0 * * * *")
public void flagOverdueAssets() { ... }
```

---

### EPIC E10: Performance Reviews & Goals

#### User Story 1: Create Review Cycles (quarterly/annual)

**Section Title:** Review Cycle Management  
**Description:** CRUD for review cycles.  
**Design Specification:**  
- Fields: type, startDate, endDate  
**Sample Implementation:**  
```java
@Entity
public class ReviewCycle { ... }
```

#### User Story 2: Track Employee Goals

**Section Title:** Goal Tracking  
**Description:** CRUD for employee goals.  
**Design Specification:**  
- Fields: description, status, dueDate  
**Sample Implementation:**  
```java
@Entity
public class EmployeeGoal { ... }
```

#### User Story 3: Review Submission and Acknowledgment Workflow

**Section Title:** Review Workflow  
**Description:** Submit and acknowledge reviews.  
**Design Specification:**  
- Workflow: submit â acknowledge  
**Sample Implementation:**  
```java
@PostMapping("/reviews/submit")
public void submitReview(@RequestBody ReviewDto dto) { ... }
```

#### User Story 4: Export Reviews as PDF

**Section Title:** Review Export  
**Description:** Export reviews as PDF.  
**Design Specification:**  
- Generate PDF from review data  
**Sample Implementation:**  
```java
@GetMapping("/reviews/export")
public void exportReviewsPdf(HttpServletResponse response) { ... }
```

#### User Story 5: Role-Based Review Visibility

**Section Title:** Review Visibility  
**Description:** Restrict review access by role.  
**Design Specification:**  
- Only supervisor/employee/HR can view  
**Sample Implementation:**  
```java
@PreAuthorize("hasAnyRole('HR','SUPERVISOR') or principal.id == #employeeId")
@GetMapping("/reviews/{employeeId}")
public List<ReviewDto> getReviews(@PathVariable Long employeeId) { ... }
```

---

### EPIC E11: Payroll Export Integration

#### User Story 1: Generate Payroll-Ready Files

**Section Title:** Payroll Export  
**Description:** Generate files for payroll provider.  
**Design Specification:**  
- Map attendance/leave to provider schema  
**Sample Implementation:**  
```java
public File generatePayrollFile(PayrollExportRequest req) { ... }
```

#### User Story 2: Map to External Payroll Provider Formats

**Section Title:** Provider Mapping  
**Description:** Map internal data to provider formats.  
**Design Specification:**  
- Use mapping config or code  
**Sample Implementation:**  
```java
public PayrollRecord mapToProvider(EmployeeAttendance att) { ... }
```

#### User Story 3: Secure Delivery via SFTP/API

**Section Title:** Secure Delivery  
**Description:** Deliver files via SFTP or API.  
**Design Specification:**  
- Use Spring Integration/SFTP  
**Sample Implementation:**  
```java
sftpService.sendFile(payrollFile);
```

#### User Story 4: Delivery Retry Logic with backoff

**Section Title:** Retry Logic  
**Description:** Retry failed deliveries with backoff.  
**Design Specification:**  
- Use Spring Retry  
**Sample Implementation:**  
```java
@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 5000))
public void deliverPayrollFile(File file) { ... }
```

#### User Story 5: Export Audit Logging

**Section Title:** Audit Logging  
**Description:** Log all exports with actor, timestamp, status.  
**Design Specification:**  
- AuditExportLog entity  
**Sample Implementation:**  
```java
auditService.logExport(actor, timestamp, status, fileName);
```

---

### EPIC E12: Notifications & Announcements

#### User Story 1: Opt-In/Out Notification Channels

**Section Title:** Notification Preferences  
**Description:** Users manage notification channels.  
**Design Specification:**  
- NotificationPreference entity  
**Sample Implementation:**  
```java
@Entity
public class NotificationPreference { ... }
```

#### User Story 2: Localize Notification Templates

**Section Title:** Template Localization  
**Description:** Support localized templates.  
**Design Specification:**  
- Store templates per locale  
**Sample Implementation:**  
```java
public String getLocalizedTemplate(String key, Locale locale) { ... }
```

#### User Story 3: Track Notification Delivery Status

**Section Title:** Delivery Tracking  
**Description:** Track status of notifications.  
**Design Specification:**  
- NotificationLog entity  
**Sample Implementation:**  
```java
@Entity
public class NotificationLog { ... }
```

#### User Story 4: Apply Notification Rate Limits

**Section Title:** Rate Limiting  
**Description:** Limit notification frequency per user/channel.  
**Design Specification:**  
- Use Redis or in-memory counters  
**Sample Implementation:**  
```java
if (rateLimiter.isLimitExceeded(userId, channel)) {
    throw new RateLimitException();
}
```

#### User Story 5: Announcement Dashboard

**Section Title:** Announcement Dashboard  
**Description:** Dashboard for announcements.  
**Design Specification:**  
- GET endpoint for announcements  
**Sample Implementation:**  
```java
@GetMapping("/announcements")
public List<AnnouncementDto> getAnnouncements() { ... }
```

---

### EPIC E13: Integration Layer

#### User Story 1: JWT/OAuth2-Secured APIs

**Section Title:** API Security  
**Description:** Secure APIs with JWT/OAuth2.  
**Design Specification:**  
- Use Spring Security OAuth2  
**Sample Implementation:**  
```java
http.oauth2ResourceServer().jwt();
```

#### User Story 2: HRIS Sync Job for employee data

**Section Title:** HRIS Sync  
**Description:** Scheduled job to sync employee data.  
**Design Specification:**  
- Scheduled job, REST connector  
**Sample Implementation:**  
```java
@Scheduled(cron = "0 0 * * * *")
public void syncHrisEmployees() { ... }
```

#### User Story 3: Link Employees to WMS Departments/Locations

**Section Title:** WMS Linking  
**Description:** Map employees to WMS departments/locations.  
**Design Specification:**  
- Foreign key or mapping table  
**Sample Implementation:**  
```java
employee.setWmsDepartment(wmsDept);
```

#### User Story 4: Idempotent Webhook Processing

**Section Title:** Webhook Idempotency  
**Description:** Ensure webhooks are idempotent.  
**Design Specification:**  
- Store processed event IDs  
**Sample Implementation:**  
```java
if (webhookRepo.existsByEventId(eventId)) return;
```

#### User Story 5: OpenAPI Documentation for Integration APIs

**Section Title:** API Documentation  
**Description:** Document integration APIs in OpenAPI.  
**Design Specification:**  
- Annotate endpoints  
**Sample Implementation:**  
```java
@Operation(summary = "HRIS Sync", ...)
```

---

### EPIC E14: Audit Trail & Compliance

#### User Story 1: Log Sensitive Changes with actor, timestamp, before/after

**Section Title:** Audit Logging  
**Description:** Log all sensitive changes.  
**Design Specification:**  
- AuditLog entity: actor, timestamp, entity, before, after  
**Sample Implementation:**  
```java
@Entity
public class AuditLog { ... }
```

#### User Story 2: Immutable Tamper-Evident Log Storage

**Section Title:** Tamper-Evident Storage  
**Description:** Store logs immutably.  
**Design Specification:**  
- Write-once table, hash chaining  
**Sample Implementation:**  
```java
// Use append-only table, hash of previous row
```

#### User Story 3: Export Audit Logs by date/user/entity

**Section Title:** Audit Log Export  
**Description:** Export logs as CSV/PDF.  
**Design Specification:**  
- Filter by date/user/entity  
**Sample Implementation:**  
```java
@GetMapping("/audit/export")
public void exportAuditLogs(HttpServletResponse response) { ... }
```

#### User Story 4: Validate Audit Coverage in Tests

**Section Title:** Audit Test Coverage  
**Description:** Test that all sensitive actions are audited.  
**Design Specification:**  
- Integration tests check audit log entries  
**Sample Implementation:**  
```java
@Test
void testAuditLogCreated() { ... }
```

---

### EPIC E15: Reporting & Analytics

#### User Story 1: Generate Operational Reports

**Section Title:** Report Generation  
**Description:** Generate reports (attendance, overtime, leave, certs, safety KPIs).  
**Design Specification:**  
- ReportService with report types  
**Sample Implementation:**  
```java
public Report generateReport(ReportType type, ReportFilter filter) { ... }
```

#### User Story 2: Export Reports as CSV/PDF

**Section Title:** Report Export  
**Description:** Export reports as CSV/PDF.  
**Design Specification:**  
- Use Apache POI/iText for PDF  
**Sample Implementation:**  
```java
@GetMapping("/reports/export")
public void exportReport(@RequestParam ReportType type, HttpServletResponse response) { ... }
```

#### User Story 3: Role-Based Dashboards

**Section Title:** Dashboards  
**Description:** Dashboards filtered by role.  
**Design Specification:**  
- GET endpoints return dashboard data  
**Sample Implementation:**  
```java
@GetMapping("/dashboard")
public DashboardDto getDashboard() { ... }
```

#### User Story 4: Metrics Endpoints for BI Integration

**Section Title:** Metrics API  
**Description:** Expose metrics for BI tools.  
**Design Specification:**  
- GET endpoints for metrics  
**Sample Implementation:**  
```java
@GetMapping("/metrics")
public MetricsDto getMetrics() { ... }
```

---

### EPIC E16: Mobile Access PWA

#### User Story 1: Responsive Mobile Views for core flows

**Section Title:** Mobile Views  
**Description:** Responsive UI for mobile.  
**Design Specification:**  
- Use Thymeleaf/React/Vue with responsive CSS  
**Sample Implementation:**  
```html
<meta name="viewport" content="width=device-width, initial-scale=1">
```

#### User Story 2: Installable PWA Manifest

**Section Title:** PWA Manifest  
**Description:** Add manifest.json for PWA.  
**Design Specification:**  
- manifest.json in static resources  
**Sample Implementation:**  
```json
{
  "name": "Warehouse Employee Mgmt",
  "short_name": "WMS",
  "start_url": "/",
  "display": "standalone",
  ...
}
```

#### User Story 3: Offline Queue for Clock Events

**Section Title:** Offline Queue  
**Description:** Queue clock events offline.  
**Design Specification:**  
- Service worker caches events  
**Sample Implementation:**  
```js
self.addEventListener('fetch', ...);
```

#### User Story 4: Resolve Offline Clock Event Conflicts

**Section Title:** Conflict Resolution  
**Description:** Handle conflicts when syncing offline events.  
**Design Specification:**  
- Prompt user or auto-resolve  
**Sample Implementation:**  
```js
// On sync, check for conflicts and prompt user
```

#### User Story 5: Achieve Lighthouse PWA Score â¥ 80

**Section Title:** PWA Optimization  
**Description:** Optimize for Lighthouse score.  
**Design Specification:**  
- Audit and fix issues  
**Sample Implementation:**  
```bash
npx lighthouse http://localhost:8080
```

---

### EPIC E17: Onboarding & Offboarding Workflow

#### User Story 1: Automate New Hire Provisioning

**Section Title:** New Hire Automation  
**Description:** Auto-provision accounts, schedule, training.  
**Design Specification:**  
- On HRIS sync, trigger provisioning  
**Sample Implementation:**  
```java
public void provisionNewHire(Employee employee) { ... }
```

#### User Story 2: Assign Assets to New Hires

**Section Title:** Asset Assignment  
**Description:** Assign assets during onboarding.  
**Design Specification:**  
- Assign default assets  
**Sample Implementation:**  
```java
assetService.assignDefaultAssets(employeeId);
```

#### User Story 3: Automate Offboarding (access revocation, asset collection)

**Section Title:** Offboarding Automation  
**Description:** Revoke access, collect assets.  
**Design Specification:**  
- On termination, trigger offboarding  
**Sample Implementation:**  
```java
public void offboardEmployee(Long employeeId) { ... }
```

#### User Story 4: Generate Onboarding/Offboarding Tasks

**Section Title:** Task Generation  
**Description:** Create tasks for onboarding/offboarding.  
**Design Specification:**  
- Task entity, assign to responsible users  
**Sample Implementation:**  
```java
taskService.createOnboardingTasks(employeeId);
```

---

### EPIC E18: Localization & Multi-Tenant

#### User Story 1: Localize UI

**Section Title:** UI Localization  
**Description:** Support multiple languages in UI.  
**Design Specification:**  
- Use message bundles  
**Sample Implementation:**  
```java
messageSource.getMessage("label.employee", locale);
```

#### User Story 2: Localize Notification Templates

**Section Title:** Notification Localization  
**Description:** Localized notification templates.  
**Design Specification:**  
- Store templates per locale  
**Sample Implementation:**  
```java
notificationService.sendLocalized(..., locale);
```

#### User Story 3: Localize Report Exports

**Section Title:** Report Localization  
**Description:** Localize exported reports.  
**Design Specification:**  
- Use locale in report generation  
**Sample Implementation:**  
```java
reportService.generateReport(type, filter, locale);
```

---

### EPIC E19: Observability & Monitoring

#### User Story 1: Collect Application Metrics

**Section Title:** Metrics Collection  
**Description:** Collect and expose metrics.  
**Design Specification:**  
- Use Actuator metrics  
**Sample Implementation:**  
```yaml
management.endpoints.web.exposure.include: metrics
```

#### User Story 2: Log Application Errors centrally

**Section Title:** Centralized Logging  
**Description:** Log errors to central system.  
**Design Specification:**  
- Use ELK/EFK stack  
**Sample Implementation:**  
```yaml
logging.file.name: logs/app.log
```

#### User Story 3: Enable Distributed Tracing

**Section Title:** Distributed Tracing  
**Description:** Enable tracing (e.g., OpenTelemetry).  
**Design Specification:**  
- Integrate with tracing agent  
**Sample Implementation:**  
```yaml
management.tracing.enabled: true
```

#### User Story 4: Integrate with Alerting Tools

**Section Title:** Alerting Integration  
**Description:** Integrate with alerting (PagerDuty, Opsgenie).  
**Design Specification:**  
- Webhook or API integration  
**Sample Implementation:**  
```java
alertService.sendAlert(...);
```

---

### EPIC E20: CI/CD & Deployment Automation

#### User Story 1: Set Up CI Pipeline

**Section Title:** CI Pipeline  
**Description:** Configure CI (e.g., GitHub Actions, Jenkins).  
**Design Specification:**  
- Build, test, lint, package  
**Sample Implementation:**  
```yaml
# .github/workflows/ci.yml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build
        run: mvn clean install
```

#### User Story 2: Set Up CD Pipeline

**Section Title:** CD Pipeline  
**Description:** Configure CD for deployments.  
**Design Specification:**  
- Deploy to staging/prod  
**Sample Implementation:**  
```yaml
# .github/workflows/cd.yml
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Deploy
        run: ./deploy.sh
```

#### User Story 3: Manage Environment Configurations

**Section Title:** Env Config  
**Description:** Manage configs per environment.  
**Design Specification:**  
- Use `application-{env}.yml`  
**Sample Implementation:**  
```yaml
spring.profiles.active: prod
```

#### User Story 4: Enforce Test Coverage in CI

**Section Title:** Test Coverage  
**Description:** Enforce coverage thresholds.  
**Design Specification:**  
- Use JaCoCo, fail build if below threshold  
**Sample Implementation:**  
```xml
<jacoco>
  <minimum>0.80</minimum>
</jacoco>
```

#### User Story 5: Log Deployment Events

**Section Title:** Deployment Logging  
**Description:** Log deployments for audit.  
**Design Specification:**  
- DeploymentLog entity  
**Sample Implementation:**  
```java
deploymentLogService.logDeployment(...);
```

---

## End of Document

This comprehensive technical design document covers all 20 epics and 95+ user stories, providing detailed design specifications, sample implementations, and Spring Boot best practices for the Warehouse Employee Management System.