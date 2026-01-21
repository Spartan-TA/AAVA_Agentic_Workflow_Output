# Warehouse Employee Management System â Low-Level Technical Design Document

**Version:** 1.0  
**Date:** 2024-06  
**Authors:** Senior Software Architect  
**Scope:** Covers all 80 user stories across 20 epics (E01âE20) for the Warehouse Employee Management System, following Spring Boot industry standards.

---

## Table of Contents

- [E01 Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
- [E02 Employee Master Data (CRUD)](#e02-employee-master-data-crud)
- [E03 Role-Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
- [E04 Time & Attendance (Clock In/Out)](#e04-time--attendance-clock-inout)
- [E05 Shift & Schedule Management](#e05-shift--schedule-management)
- [E06 Leave & Absence Management](#e06-leave--absence-management)
- [E07 Training & Certification Tracking](#e07-training--certification-tracking)
- [E08 Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
- [E09 Equipment & Asset Assignment](#e09-equipment--asset-assignment)
- [E10 Performance Reviews & Goals](#e10-performance-reviews--goals)
- [E11 Payroll Export Integration](#e11-payroll-export-integration)
- [E12 Notifications & Announcements](#e12-notifications--announcements)
- [E13 Integration Layer (HRIS/WMS APIs)](#e13-integration-layer-hriswms-apis)
- [E14 Audit Trail & Compliance](#e14-audit-trail--compliance)
- [E15 Reporting & Analytics](#e15-reporting--analytics)
- [E16 Mobile Access (PWA)](#e16-mobile-access-pwa)
- [E17 Onboarding & Offboarding Workflow](#e17-onboarding--offboarding-workflow)
- [E18 Document Management](#e18-document-management)
- [E19 Self-Service Portal](#e19-self-service-portal)
- [E20 System Administration](#e20-system-administration)

---

# E01 Project Scaffolding & Domain Setup

## User Story 1: Initialize Spring Boot Project

### Description
Set up a Maven-based Spring Boot project with base packages, core modules, Flyway/Liquibase for DB migrations, and Actuator for health checks.

### Design Specification

- **Spring Boot Architecture:** Layered (Controller, Service, Repository, Domain).
- **Package Structure:**  
  - `com.wms`  
    - `employee`  
    - `attendance`  
    - `safety`  
    - `config`  
    - `repository`  
    - `service`  
    - `controller`
- **Modules:** Employee, Scheduling, Attendance, Safety.
- **Configuration:**  
  - `application.yml` for environment settings.  
  - Flyway/Liquibase migration scripts in `src/main/resources/db/migration`.
- **Security:** None for scaffolding.
- **Integration:** Spring Boot Actuator enabled.
- **Sample Implementation:**
  ```java
  @SpringBootApplication
  public class WarehouseEmployeeMgmtApplication {
      public static void main(String[] args) {
          SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
      }
  }
  ```
  - `pom.xml` includes dependencies for Spring Boot Starter Web, Data JPA, Security, Actuator, Flyway/Liquibase.

---

## User Story 2: Configure Base Packages

### Description
Create base package structure and README with build/run steps.

### Design Specification

- **Package Structure:**  
  - `com.wms.employee`  
  - `com.wms.attendance`  
  - `com.wms.safety`  
  - `com.wms.config`
- **README:**  
  - Build: `mvn clean install`  
  - Run: `mvn spring-boot:run`
- **Sample Implementation:**
  ```
  src/
    main/
      java/
        com/
          wms/
            employee/
            attendance/
            safety/
            config/
      resources/
        db/
          migration/
        application.yml
  ```

---

## User Story 3: Database Migration Setup

### Description
Enable Flyway/Liquibase for baseline migration.

### Design Specification

- **Migration Scripts:**  
  - `V1__init.sql` for initial schema.
- **Configuration:**  
  - `spring.flyway.enabled=true`
- **Sample Implementation:**
  ```sql
  -- V1__init.sql
  CREATE TABLE employee (
      id BIGINT PRIMARY KEY AUTO_INCREMENT,
      name VARCHAR(255),
      badge_id VARCHAR(50) UNIQUE,
      role VARCHAR(50),
      department VARCHAR(100),
      hire_date DATE,
      status VARCHAR(20)
  );
  ```

---

## User Story 4: Health Endpoint

### Description
Enable Actuator health endpoint.

### Design Specification

- **Configuration:**  
  - `management.endpoints.web.exposure.include=health`
- **Sample Implementation:**
  ```
  GET /actuator/health
  Response: {"status":"UP"}
  ```

---

# E02 Employee Master Data (CRUD)

## User Story 1: Create Employee Domain

### Description
Define Employee entity with fields: name, badgeId, role, department, shiftGroup, hireDate, status.

### Design Specification

- **Entity Design:**
  ```java
  @Entity
  public class Employee {
      @Id @GeneratedValue
      private Long id;
      private String name;
      @Column(unique=true)
      private String badgeId;
      private String role;
      private String department;
      private String shiftGroup;
      private LocalDate hireDate;
      private String status;
      // getters/setters
  }
  ```
- **Relationships:**  
  - One-to-many with Attendance, Shift, Certification.
- **Repository:**
  ```java
  public interface EmployeeRepository extends JpaRepository<Employee, Long> {
      Optional<Employee> findByBadgeId(String badgeId);
  }
  ```
- **Service Layer:**
  ```java
  public Employee createEmployee(EmployeeDto dto);
  public Employee updateEmployee(Long id, EmployeeDto dto);
  public void deleteEmployee(Long id);
  public Page<Employee> listEmployees(Pageable pageable, EmployeeFilter filter);
  ```
- **Controller:**
  ```java
  @RestController
  @RequestMapping("/employees")
  public class EmployeeController {
      @PostMapping public ResponseEntity<EmployeeDto> create(@RequestBody EmployeeDto dto);
      @GetMapping public Page<EmployeeDto> list(...);
      @PutMapping("/{id}") public ResponseEntity<EmployeeDto> update(...);
      @DeleteMapping("/{id}") public ResponseEntity<Void> delete(...);
  }
  ```
- **OpenAPI:**  
  - Schemas for EmployeeDto, examples included.

---

## User Story 2: CRUD APIs

### Description
Implement POST/GET/PUT/PATCH/DELETE endpoints for employees.

### Design Specification

- **Endpoints:**  
  - `POST /employees`  
  - `GET /employees`  
  - `GET /employees/{id}`  
  - `PUT /employees/{id}`  
  - `PATCH /employees/{id}`  
  - `DELETE /employees/{id}` (soft-delete)
- **Pagination/Filtering:**  
  - Query params for department, role, status.
- **Soft-Delete:**  
  - `status = 'INACTIVE'`
- **Sample Implementation:**
  ```java
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> softDelete(@PathVariable Long id) {
      employeeService.softDelete(id);
      return ResponseEntity.noContent().build();
  }
  ```

---

## User Story 3: Unique badgeId Enforcement

### Description
Ensure badgeId is unique.

### Design Specification

- **Entity Constraint:**  
  - `@Column(unique=true)`
- **Service Validation:**  
  - Check existence before create/update.
- **Sample Implementation:**
  ```java
  if (employeeRepository.findByBadgeId(dto.getBadgeId()).isPresent()) {
      throw new DuplicateBadgeIdException();
  }
  ```

---

## User Story 4: API Documentation

### Description
Document APIs with OpenAPI schemas and examples.

### Design Specification

- **OpenAPI Integration:**  
  - Use Springdoc OpenAPI.
- **Sample Implementation:**
  ```java
  @Operation(summary = "Create Employee", requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = EmployeeDto.class))))
  ```

---

# E03 Role-Based Access Control (RBAC)

## User Story 1: Add Spring Security

### Description
Configure roles: ADMIN, HR, SUPERVISOR, WORKER; method/endpoint security.

### Design Specification

- **Security Config:**
  ```java
  @EnableWebSecurity
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
      @Override
      protected void configure(HttpSecurity http) throws Exception {
          http.authorizeRequests()
              .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR")
              .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
              .anyRequest().authenticated()
              .and().httpBasic();
      }
  }
  ```
- **Role Hierarchy:**  
  - ADMIN > HR > SUPERVISOR > WORKER
- **Method Security:**  
  - `@PreAuthorize("hasRole('ADMIN')")`
- **API Key/OAuth2 Toggle:**  
  - Use profiles in `application.yml`.

---

## User Story 2: Unauthorized/Forbidden Handling

### Description
Return 401 for unauthorized, 403 for forbidden actions.

### Design Specification

- **Exception Handling:**  
  - Custom `AccessDeniedHandler`.
- **Sample Implementation:**
  ```java
  @ControllerAdvice
  public class SecurityExceptionHandler {
      @ExceptionHandler(AccessDeniedException.class)
      public ResponseEntity<?> handleAccessDenied(...) {
          return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
      }
  }
  ```

---

## User Story 3: Role Permissions

### Description
ADMIN can manage all records; SUPERVISOR limited to team.

### Design Specification

- **Row-Level Security:**  
  - Filter queries by supervisor's team.
- **Sample Implementation:**
  ```java
  @PreAuthorize("hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(authentication, #id)")
  ```

---

## User Story 4: Security Tests

### Description
Security rules covered by tests.

### Design Specification

- **Test Cases:**  
  - Use Spring Security Test.
- **Sample Implementation:**
  ```java
  @WithMockUser(roles="ADMIN")
  @Test
  public void adminCanAccessEmployeeApi() { ... }
  ```

---

# E04 Time & Attendance (Clock In/Out)

## User Story 1: Clock-In/Out Endpoints

### Description
Endpoints for clock-in/out events with geofence and device capture.

### Design Specification

- **Entity Design:**
  ```java
  @Entity
  public class AttendanceEvent {
      @Id @GeneratedValue
      private Long id;
      private Long employeeId;
      private LocalDateTime timestamp;
      private String type; // CLOCK_IN, CLOCK_OUT
      private String deviceId;
      private Double latitude;
      private Double longitude;
  }
  ```
- **Controller:**
  ```java
  @PostMapping("/attendance/clock-in")
  public ResponseEntity<?> clockIn(@RequestBody ClockEventDto dto);
  @PostMapping("/attendance/clock-out")
  public ResponseEntity<?> clockOut(@RequestBody ClockEventDto dto);
  ```
- **Service Layer:**  
  - Validate geofence, device.
- **Repository:**  
  - AttendanceEventRepository.
- **Sample Implementation:**
  ```java
  if (!geofenceService.isWithinAllowedArea(dto.getLatitude(), dto.getLongitude())) {
      throw new GeofenceViolationException();
  }
  ```

---

## User Story 2: Shift Association

### Description
Automatically associate clock events with shifts.

### Design Specification

- **Service Logic:**  
  - Find active shift for employee at event time.
- **Sample Implementation:**
  ```java
  Shift shift = shiftRepository.findActiveShift(employeeId, eventTime);
  attendanceEvent.setShiftId(shift.getId());
  ```

---

## User Story 3: Corrections Workflow

### Description
Handle missed punches and corrections workflow.

### Design Specification

- **Entity:**  
  - CorrectionRequest (status: PENDING, APPROVED, REJECTED)
- **Controller:**  
  - `POST /attendance/corrections`
- **Service:**  
  - Supervisor approval flow.
- **Sample Implementation:**
  ```java
  @PostMapping("/attendance/corrections")
  public ResponseEntity<?> requestCorrection(@RequestBody CorrectionDto dto);
  ```

---

## User Story 4: Reports Export

### Description
Export attendance reports as CSV.

### Design Specification

- **Controller:**  
  - `GET /attendance/reports?format=csv`
- **Service:**  
  - Generate CSV from attendance data.
- **Sample Implementation:**
  ```java
  @GetMapping("/attendance/reports")
  public ResponseEntity<Resource> exportReport(@RequestParam String format) { ... }
  ```

---

# E05 Shift & Schedule Management

## User Story 1: Shift Templates CRUD

### Description
Create recurring shift templates, rotations, overtime rules.

### Design Specification

- **Entity:**
  ```java
  @Entity
  public class ShiftTemplate {
      @Id @GeneratedValue
      private Long id;
      private String name;
      private LocalTime startTime;
      private LocalTime endTime;
      private boolean recurring;
      private String rotationPattern;
      private boolean overtimeAllowed;
  }
  ```
- **Controller:**  
  - `POST /shifts/templates`
- **Service:**  
  - Validate conflicts.
- **Repository:**  
  - ShiftTemplateRepository.

---

## User Story 2: Conflict Detection

### Description
Detect and prevent scheduling conflicts.

### Design Specification

- **Service Logic:**  
  - Check overlapping shifts for employee.
- **Sample Implementation:**
  ```java
  boolean hasConflict = shiftService.hasConflict(employeeId, newShift);
  if (hasConflict) throw new ShiftConflictException();
  ```

---

## User Story 3: Bulk Assignment

### Description
Supervisors can bulk-assign shifts.

### Design Specification

- **Controller:**  
  - `POST /shifts/bulk-assign`
- **Service:**  
  - Assign shifts to multiple employees.
- **Sample Implementation:**
  ```java
  @PostMapping("/shifts/bulk-assign")
  public ResponseEntity<?> bulkAssign(@RequestBody BulkAssignDto dto);
  ```

---

## User Story 4: Audit Entries

### Description
Generate audit entries for schedule changes.

### Design Specification

- **Audit Entity:**  
  - AuditLog (actor, timestamp, action, before/after)
- **Service:**  
  - Log every schedule change.
- **Sample Implementation:**
  ```java
  auditService.logChange("SHIFT_ASSIGN", actor, before, after);
  ```

---

# E06 Leave & Absence Management

## User Story 1: Leave Request Workflow

### Description
Employees can request PTO, sick, unpaid leave.

### Design Specification

- **Entity:**
  ```java
  @Entity
  public class LeaveRequest {
      @Id @GeneratedValue
      private Long id;
      private Long employeeId;
      private LocalDate startDate;
      private LocalDate endDate;
      private String type; // PTO, SICK, UNPAID
      private String status; // REQUESTED, APPROVED, DENIED
      private String approver;
  }
  ```
- **Controller:**  
  - `POST /leave/requests`
- **Service:**  
  - Validate accruals, create request.

---

## User Story 2: Supervisor Approval

### Description
Supervisors approve/deny leave requests.

### Design Specification

- **Controller:**  
  - `POST /leave/requests/{id}/approve`
  - `POST /leave/requests/{id}/deny`
- **Service:**  
  - Update status, notify employee.

---

## User Story 3: Accrual Balances

### Description
Update leave balances on approval.

### Design Specification

- **Service Logic:**  
  - Deduct approved leave from balance.
- **Sample Implementation:**
  ```java
  leaveBalanceService.deduct(employeeId, leaveDays);
  ```

---

## User Story 4: Scheduling Integration

### Description
Auto-flag scheduled shifts for coverage when leave is approved.

### Design Specification

- **Service:**  
  - Mark shifts as needing coverage.
- **Sample Implementation:**
  ```java
  shiftService.flagForCoverage(employeeId, leavePeriod);
  ```

---

# E07 Training & Certification Tracking

## User Story 1: Certification CRUD

### Description
Track required certifications, expirations, renewals.

### Design Specification

- **Entity:**
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
- **Controller:**  
  - `POST /certifications`
- **Service:**  
  - Alert on expiry.

---

## User Story 2: Expiry Alerts

### Description
Alert 30/7 days before expiry.

### Design Specification

- **Service:**  
  - Scheduled job to send alerts.
- **Sample Implementation:**
  ```java
  @Scheduled(cron = "0 0 8 * * ?")
  public void sendExpiryAlerts() { ... }
  ```

---

## User Story 3: Assignment Blocking

### Description
Block assignment to tasks requiring expired certs.

### Design Specification

- **Service:**  
  - Validate certification before assignment.
- **Sample Implementation:**
  ```java
  if (certification.isExpired()) throw new CertificationExpiredException();
  ```

---

## User Story 4: Certification Status Visibility

### Description
Show certification status on employee profile.

### Design Specification

- **Controller:**  
  - `GET /employees/{id}/certifications`
- **Service:**  
  - Aggregate certification status.

---

# E08 Safety Incidents & OSHA Reporting

## User Story 1: Incident Recording

### Description
Record incidents/near-misses with severity, location, description.

### Design Specification

- **Entity:**
  ```java
  @Entity
  public class SafetyIncident {
      @Id @GeneratedValue
      private Long id;
      private String severity;
      private String location;
      private String description;
      private Long[] involvedEmployeeIds;
      private String status; // OPEN, INVESTIGATING, RESOLVED
  }
  ```
- **Controller:**  
  - `POST /safety/incidents`
- **Service:**  
  - Validate input.

---

## User Story 2: Investigation Workflow

### Description
Status workflow: Open â Investigating â Resolved.

### Design Specification

- **Controller:**  
  - `POST /safety/incidents/{id}/investigate`
  - `POST /safety/incidents/{id}/resolve`
- **Service:**  
  - Update status, log actions.

---

## User Story 3: OSHA Export

### Description
Export OSHA 300/300A fields.

### Design Specification

- **Controller:**  
  - `GET /safety/incidents/export?format=osha`
- **Service:**  
  - Map fields to OSHA schema.

---

## User Story 4: Metrics Dashboard

### Description
Provide dashboard endpoints for safety KPIs.

### Design Specification

- **Controller:**  
  - `GET /safety/metrics`
- **Service:**  
  - Aggregate incident data.

---

# E09 Equipment & Asset Assignment

## User Story 1: Asset Registry CRUD

### Description
Assign scanners, forklifts, PPE to employees.

### Design Specification

- **Entity:**
  ```java
  @Entity
  public class Asset {
      @Id @GeneratedValue
      private Long id;
      private String type;
      private String serialNumber;
      private String condition;
      private Long assignedEmployeeId;
      private LocalDate checkoutDate;
      private LocalDate returnDate;
  }
  ```
- **Controller:**  
  - `POST /assets`
- **Service:**  
  - Validate assignment.

---

## User Story 2: Check-In/Out Endpoints

### Description
Track asset checkout/return.

### Design Specification

- **Controller:**  
  - `POST /assets/{id}/checkout`
  - `POST /assets/{id}/return`
- **Service:**  
  - Update asset status.

---

## User Story 3: Certification Validation

### Description
Prevent use if certification missing.

### Design Specification

- **Service:**  
  - Check employee certification before checkout.
- **Sample Implementation:**
  ```java
  if (!certificationService.hasValidCert(employeeId, assetType)) throw new CertificationRequiredException();
  ```

---

## User Story 4: Asset History Log

### Description
Maintain history log per asset and employee.

### Design Specification

- **Entity:**  
  - AssetHistory (assetId, employeeId, action, timestamp)
- **Controller:**  
  - `GET /assets/{id}/history`

---

# E10 Performance Reviews & Goals

## User Story 1: Review Cycle Creation

### Description
Create quarterly/annual review templates.

### Design Specification

- **Entity:**
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
      private String status; // DRAFT, SUBMITTED, ACKNOWLEDGED
  }
  ```
- **Controller:**  
  - `POST /reviews`
- **Service:**  
  - Validate cycle.

---

## User Story 2: Review Assignment

### Description
Assign reviews to employees.

### Design Specification

- **Controller:**  
  - `POST /reviews/assign`
- **Service:**  
  - Assign review template.

---

## User Story 3: Submit/Acknowledge Workflow

### Description
Supervisor/employee submit and acknowledge reviews.

### Design Specification

- **Controller:**  
  - `POST /reviews/{id}/submit`
  - `POST /reviews/{id}/acknowledge`
- **Service:**  
  - Update status.

---

## User Story 4: PDF Export

### Description
Export reviews as PDF.

### Design Specification

- **Controller:**  
  - `GET /reviews/{id}/export?format=pdf`
- **Service:**  
  - Generate PDF.

---

# E11 Payroll Export Integration

## User Story 1: Payroll File Generation

### Description
Generate payroll-ready files from approved attendance and leave.

### Design Specification

- **Service:**  
  - Aggregate attendance/leave data.
- **Controller:**  
  - `GET /payroll/export`
- **Repository:**  
  - PayrollExportRepository.

---

## User Story 2: Provider Mapping

### Description
Map data to external payroll provider formats.

### Design Specification

- **Service:**  
  - Transform data to provider schema.

---

## User Story 3: Secure Delivery

### Description
Deliver files via SFTP/API.

### Design Specification

- **Integration:**  
  - SFTP client, REST API client.
- **Service:**  
  - Retry failed deliveries.

---

## User Story 4: Audit Logging

### Description
Log every export.

### Design Specification

- **Audit Entity:**  
  - PayrollExportLog (timestamp, status, details)
- **Service:**  
  - Log on every export.

---

# E12 Notifications & Announcements

## User Story 1: Notification Channels

### Description
In-app, email, SMS notifications for events.

### Design Specification

- **Entity:**
  ```java
  @Entity
  public class Notification {
      @Id @GeneratedValue
      private Long id;
      private Long userId;
      private String channel; // IN_APP, EMAIL, SMS
      private String template;
      private String status; // SENT, FAILED
      private LocalDateTime sentAt;
  }
  ```
- **Controller:**  
  - `POST /notifications`
- **Service:**  
  - Send via channel.

---

## User Story 2: Opt-In/Out

### Description
Users opt-in/out per channel.

### Design Specification

- **Entity:**  
  - NotificationPreference (userId, channel, enabled)
- **Controller:**  
  - `POST /notifications/preferences`

---

## User Story 3: Delivery Tracking

### Description
Track delivery status.

### Design Specification

- **Service:**  
  - Update status on send/receive.

---

## User Story 4: Announcements Dashboard

### Description
Announcements visible on dashboard.

### Design Specification

- **Controller:**  
  - `GET /announcements`
- **Service:**  
  - Aggregate announcements.

---

# E13 Integration Layer (HRIS/WMS APIs)

## User Story 1: HRIS Sync

### Description
Expose REST APIs and connectors for HRIS.

### Design Specification

- **Controller:**  
  - `POST /integration/hris/sync`
- **Service:**  
  - Create/update employees from HRIS payload.
- **Security:**  
  - JWT/OAuth2.

---

## User Story 2: WMS Link

### Description
Link departments/locations to WMS.

### Design Specification

- **Controller:**  
  - `GET /integration/wms/departments`
- **Service:**  
  - Fetch and map WMS data.

---

## User Story 3: Webhooks

### Description
Expose webhooks for events.

### Design Specification

- **Controller:**  
  - `POST /integration/webhooks/event`
- **Service:**  
  - Idempotent event handling.

---

## User Story 4: OpenAPI Documentation

### Description
Document APIs in OpenAPI.

### Design Specification

- **OpenAPI:**  
  - All integration endpoints documented.

---

# E14 Audit Trail & Compliance

## User Story 1: Centralized Audit Logging

### Description
Log sensitive changes (PII, schedules, approvals, payroll).

### Design Specification

- **Entity:**
  ```java
  @Entity
  public class AuditLog {
      @Id @GeneratedValue
      private Long id;
      private String entity;
      private Long entityId;
      private String action;
      private String actor;
      private LocalDateTime timestamp;
      private String before;
      private String after;
  }
  ```
- **Service:**  
  - Log on create/update/delete.

---

## User Story 2: Tamper-Evident Storage

### Description
Immutable log table.

### Design Specification

- **DB:**  
  - No update/delete on audit table.
- **Service:**  
  - Only insert allowed.

---

## User Story 3: Audit Export

### Description
Export by date/user/entity.

### Design Specification

- **Controller:**  
  - `GET /audit/export?date=...&user=...&entity=...`
- **Service:**  
  - Filter and export logs.

---

## User Story 4: Coverage Tests

### Description
Tests validate audit coverage.

### Design Specification

- **Test Cases:**  
  - Create/update/delete triggers audit log.

---

# E15 Reporting & Analytics

## User Story 1: Operational Reports

### Description
Attendance, overtime, leave balances, certification status, safety KPIs.

### Design Specification

- **Controller:**  
  - `GET /reports/attendance`
  - `GET /reports/overtime`
  - `GET /reports/leave`
  - `GET /reports/certification`
  - `GET /reports/safety`
- **Service:**  
  - Aggregate and filter data.

---

## User Story 2: Export CSV/PDF

### Description
Export reports as CSV/PDF.

### Design Specification

- **Controller:**  
  - `GET /reports/export?format=csv`
- **Service:**  
  - Generate file.

---

## User Story 3: Role-Based Dashboards

### Description
Dashboards filtered by role.

### Design Specification

- **Controller:**  
  - `GET /dashboard`
- **Service:**  
  - Filter data by user role.

---

## User Story 4: Metrics Endpoints

### Description
Metrics endpoints for BI.

### Design Specification

- **Controller:**  
  - `GET /metrics`
- **Service:**  
  - Expose metrics for BI tools.

---

# E16 Mobile Access (PWA)

## User Story 1: Responsive Views

### Description
Responsive views for clock-in/out, schedules, leave, announcements.

### Design Specification

- **Frontend:**  
  - Thymeleaf/React with responsive CSS.
- **Controller:**  
  - Mobile endpoints mirror desktop.
- **Service:**  
  - Same business logic.

---

## User Story 2: PWA Manifest

### Description
Installable PWA manifest.

### Design Specification

- **Manifest:**  
  - `manifest.json` in `resources/static`.
- **Sample Implementation:**
  ```json
  {
    "name": "Warehouse Employee Mgmt",
    "short_name": "WMS",
    "start_url": "/",
    "display": "standalone",
    "background_color": "#ffffff",
    "theme_color": "#1976d2"
  }
  ```

---

## User Story 3: Offline Queue

### Description
Offline queue for clock events.

### Design Specification

- **Frontend:**  
  - Service Worker caches clock events.
- **Backend:**  
  - Conflict resolution on sync.

---

## User Story 4: Lighthouse Score

### Description
Lighthouse PWA score â¥ 80.

### Design Specification

- **Testing:**  
  - Run Lighthouse audits.

---

# E17 Onboarding & Offboarding Workflow

## User Story 1: New Hire Provisioning

### Description
Automate provisioning of accounts, initial schedule, required training.

### Design Specification

- **Service:**  
  - On HRIS sync, create user, assign schedule, training tasks.

---

## User Story 2: Asset Assignment

### Description
Assign assets on onboarding.

### Design Specification

- **Service:**  
  - Create asset assignment tasks.

---

## User Story 3: Offboarding Access Revocation

### Description
Revoke access and collect assets on termination.

### Design Specification

- **Service:**  
  - Disable user, flag assets for return.

---

## User Story 4: Schedule Update

### Description
Update schedules on onboarding/offboarding.

### Design Specification

- **Service:**  
  - Remove/add shifts as needed.

---

# E18 Document Management

## User Story 1: Document Upload

### Description
Upload proof documents for certifications, incidents, etc.

### Design Specification

- **Entity:**
  ```java
  @Entity
  public class Document {
      @Id @GeneratedValue
      private Long id;
      private String type;
      private String url;
      private Long ownerId;
      private LocalDateTime uploadedAt;
  }
  ```
- **Controller:**  
  - `POST /documents/upload`
- **Service:**  
  - Store file, link to entity.

---

## User Story 2: Document Retrieval

### Description
Retrieve documents by entity.

### Design Specification

- **Controller:**  
  - `GET /documents?ownerId=...&type=...`

---

## User Story 3: Document Security

### Description
Restrict access to documents by role.

### Design Specification

- **Security:**  
  - RBAC on document endpoints.

---

## User Story 4: Document Expiry

### Description
Flag expired/expiring documents.

### Design Specification

- **Service:**  
  - Scheduled job to flag.

---

# E19 Self-Service Portal

## User Story 1: Profile Management

### Description
Employees manage their profile.

### Design Specification

- **Controller:**  
  - `GET /portal/profile`
  - `PUT /portal/profile`
- **Service:**  
  - Validate updates.

---

## User Story 2: Leave Requests

### Description
Employees request leave via portal.

### Design Specification

- **Controller:**  
  - `POST /portal/leave-request`

---

## User Story 3: Shift View

### Description
Employees view upcoming shifts.

### Design Specification

- **Controller:**  
  - `GET /portal/shifts`

---

## User Story 4: Notification Preferences

### Description
Manage notification preferences.

### Design Specification

- **Controller:**  
  - `PUT /portal/notifications/preferences`

---

# E20 System Administration

## User Story 1: User Management

### Description
Admins manage users and roles.

### Design Specification

- **Controller:**  
  - `GET /admin/users`
  - `POST /admin/users`
  - `PUT /admin/users/{id}`
  - `DELETE /admin/users/{id}`
- **Service:**  
  - CRUD operations.

---

## User Story 2: System Configuration

### Description
Manage system-wide settings.

### Design Specification

- **Entity:**
  ```java
  @Entity
  public class SystemConfig {
      @Id @GeneratedValue
      private Long id;
      private String key;
      private String value;
  }
  ```
- **Controller:**  
  - `GET /admin/config`
  - `PUT /admin/config/{key}`

---

## User Story 3: Maintenance Mode

### Description
Enable/disable maintenance mode.

### Design Specification

- **Controller:**  
  - `POST /admin/maintenance`
- **Service:**  
  - Toggle flag, restrict access.

---

## User Story 4: Audit & Monitoring

### Description
Monitor system health and audit logs.

### Design Specification

- **Controller:**  
  - `GET /admin/health`
  - `GET /admin/audit`
- **Service:**  
  - Aggregate health/audit data.

---

# Appendix

- **Spring Boot Best Practices:**  
  - Layered architecture, DTOs for API, JPA for persistence, OpenAPI for docs, RBAC for security, scheduled jobs for alerts, audit logging for compliance.
- **Integration Points:**  
  - HRIS, WMS, Payroll, Email/SMS, SFTP, BI tools.
- **Configuration:**  
  - `application.yml` for environment, security, integration settings.
- **Security:**  
  - Spring Security, JWT/OAuth2, RBAC, method/endpoint security.
- **Sample Patterns:**  
  - Service/Repository, Controller/DTO, Scheduled jobs, Exception handling, Audit logging.

---

**End of Document**