# Technical Design Document: Warehouse Employee Management System

## Table of Contents
1. Epic E01: Project Scaffolding & Domain Setup
2. Epic E02: Employee Master Data CRUD
3. Epic E03: Role-Based Access Control (RBAC)
4. Epic E04: Time & Attendance (Clock In/Out)
5. Epic E05: Shift & Schedule Management
6. Epic E06: Leave & Absence Management
7. Epic E07: Training & Certification Tracking
8. Epic E08: Safety Incidents & OSHA Reporting
9. Epic E09: Equipment & Asset Assignment
10. Epic E10: Performance Reviews & Goals
11. Epic E11: Payroll Export Integration
12. Epic E12: Notifications & Announcements
13. Epic E13: Integration Layer (HRIS/WMS APIs)
14. Epic E14: Audit Trail & Compliance
15. Epic E15: Reporting & Analytics
16. Epic E16: Mobile Access (PWA)
17. Epic E17: Onboarding & Offboarding Workflow
18. Epic E18: Localization & Multi-Tenant
19. Epic E19: Observability & Monitoring
20. Epic E20: CI/CD & Deployment Automation

---

## Epic E01: Project Scaffolding & Domain Setup

### Section: Initialize Spring Boot Project

Description: Initialize a Maven-based Spring Boot project with standardized base packages, core modules, and database migration setup using Flyway/Liquibase. Enable Actuator for health monitoring.

Design Specification:
1. Architecture Overview
   - Layered architecture: Controller â Service â Repository â Entity
   - Modules: employee, scheduling, attendance, safety
   - Patterns: Factory for entity creation, DTO for API boundaries

2. Package Structure
   - Base: `com.warehouse.employee.management`
   - Sub-packages: `employee`, `scheduling`, `attendance`, `safety`, each with `controller`, `service`, `repository`, `entity`, `dto`, `config`, `exception`

3. Domain Model
   - Initial entities: Employee, Shift, Attendance, SafetyIncident
   - Audit fields: `createdAt`, `updatedAt`, `createdBy`, `updatedBy`

4. Repository Layer
   - Spring Data JPA repositories for each entity

5. Service Layer
   - Service interfaces and implementations for each module

6. Controller Layer
   - REST endpoints for health checks and basic CRUD

7. Configuration
   - `application.yml` for DB, actuator, Flyway/Liquibase

8. Integration Points
   - Actuator health endpoint

Sample Implementation:
```java
@SpringBootApplication
public class WarehouseEmployeeManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeManagementApplication.class, args);
    }
}
```
```yaml
# application.yml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse
    username: warehouse_user
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

## Epic E02: Employee Master Data CRUD

### Section: Create, Read, Update, Delete Employee Records

Description: Implement CRUD APIs for Employee entity, enforcing unique badgeId, supporting soft-delete, pagination, filtering, and OpenAPI documentation.

Design Specification:
1. Architecture Overview
   - Layered: Controller â Service â Repository â Entity
   - Patterns: DTO for API, Repository for persistence

2. Package Structure
   - `com.warehouse.employee.management.employee`
     - `controller`, `service`, `repository`, `entity`, `dto`, `exception`

3. Domain Model
   - Entity: `Employee`
     - Fields: `id`, `name`, `badgeId` (unique), `role`, `department`, `shiftGroup`, `hireDate`, `status`, `deleted` (soft-delete), audit fields

4. Repository Layer
   - Interface: `EmployeeRepository extends JpaRepository<Employee, Long>`
   - Methods: `findByBadgeId`, `findAllByStatus`, pagination

5. Service Layer
   - Interface: `EmployeeService`
   - Implementation: `EmployeeServiceImpl`
   - Methods: `createEmployee`, `getEmployee`, `updateEmployee`, `deleteEmployee`, `listEmployees`

6. Controller Layer
   - Endpoints:
     - POST `/employees`
     - GET `/employees`
     - GET `/employees/{id}`
     - PUT `/employees/{id}`
     - PATCH `/employees/{id}`
     - DELETE `/employees/{id}`
   - Request/Response: `EmployeeDTO`
   - Security: RBAC (ADMIN, HR)

7. Configuration
   - OpenAPI annotations
   - Pagination defaults

8. Integration Points
   - None for CRUD

Sample Implementation:
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badgeId"))
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String badgeId;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private EmployeeStatus status;
    private boolean deleted = false;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    // getters/setters
}
```
```java
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee API", description = "CRUD operations for employees")
public class EmployeeController {
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeDTO dto) { ... }
    @GetMapping
    public Page<EmployeeDTO> listEmployees(Pageable pageable, @RequestParam Map<String, String> filters) { ... }
    // Other endpoints
}
```

---

## Epic E03: Role-Based Access Control (RBAC)

### Section: Implement Role-Based Endpoint Security, API Key/OAuth2 Toggle

Description: Secure endpoints using Spring Security, define roles, method/endpoint security, and support API key/OAuth2 toggle via config.

Design Specification:
1. Architecture Overview
   - Security layer with Spring Security
   - Patterns: RBAC, method-level security

2. Package Structure
   - `com.warehouse.employee.management.security`
     - `config`, `service`, `exception`

3. Domain Model
   - Enum: `Role { ADMIN, HR, SUPERVISOR, WORKER }`

4. Repository Layer
   - User/Role repositories if needed

5. Service Layer
   - SecurityService for authentication/authorization

6. Controller Layer
   - Security annotations on endpoints

7. Configuration
   - `SecurityConfig.java` for HTTP security
   - API key/OAuth2 toggle in `application.yml`

8. Integration Points
   - OAuth2 provider, API key validation

Sample Implementation:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${security.mode}")
    private String securityMode; // "apikey" or "oauth2"
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if ("apikey".equals(securityMode)) {
            http.authorizeRequests().antMatchers("/employees/**").hasRole("ADMIN");
            // API key filter
        } else {
            http.oauth2Login();
            http.authorizeRequests().antMatchers("/employees/**").hasRole("ADMIN");
        }
        http.csrf().disable();
    }
}
```
```yaml
security:
  mode: oauth2
```

---

## Epic E04: Time & Attendance (Clock In/Out)

### Section: Clock-In, Clock-Out, Attendance Corrections, Attendance Reporting

Description: Endpoints for clock-in/out events, geofence/device capture, shift association, missed punch corrections, and attendance reporting.

Design Specification:
1. Architecture Overview
   - Attendance module: Controller â Service â Repository â Entity
   - Patterns: Factory for event creation, DTO for API

2. Package Structure
   - `com.warehouse.employee.management.attendance`
     - `controller`, `service`, `repository`, `entity`, `dto`, `exception`

3. Domain Model
   - Entity: `AttendanceEvent`
     - Fields: `id`, `employeeId`, `type` (CLOCK_IN/CLOCK_OUT), `timestamp`, `deviceId`, `location`, `shiftId`, `correctionRequested`, audit fields

4. Repository Layer
   - Interface: `AttendanceRepository`
   - Methods: `findByEmployeeIdAndDate`, `findCorrectionsPending`

5. Service Layer
   - Interface: `AttendanceService`
   - Implementation: `AttendanceServiceImpl`
   - Methods: `clockIn`, `clockOut`, `requestCorrection`, `getAttendanceReport`

6. Controller Layer
   - Endpoints:
     - POST `/attendance/clock-in`
     - POST `/attendance/clock-out`
     - POST `/attendance/corrections`
     - GET `/attendance/reports`
   - Request/Response: `AttendanceDTO`
   - Security: RBAC

7. Configuration
   - Geofence config, device validation

8. Integration Points
   - Reporting export (CSV)

Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private AttendanceType type;
    private Instant timestamp;
    private String deviceId;
    private String location;
    @ManyToOne
    private Shift shift;
    private boolean correctionRequested;
    // audit fields
}
```
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@Valid @RequestBody AttendanceDTO dto) { ... }
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@Valid @RequestBody AttendanceDTO dto) { ... }
    @GetMapping("/reports")
    public ResponseEntity<List<AttendanceReportDTO>> getReports(...) { ... }
}
```

---

## Epic E05: Shift & Schedule Management

### Section: Create Shift Templates, Assign Shifts, Overtime Rules, Blackout Dates

Description: Manage shift templates, rotations, overtime rules, assignments, and blackout dates for warehouse operations.

Design Specification:
1. Architecture Overview
   - Scheduling module: Controller â Service â Repository â Entity
   - Patterns: Factory for shift creation, DTO for API

2. Package Structure
   - `com.warehouse.employee.management.scheduling`
     - `controller`, `service`, `repository`, `entity`, `dto`, `exception`

3. Domain Model
   - Entity: `ShiftTemplate`, `ShiftAssignment`, `BlackoutDate`
     - Fields: `id`, `name`, `startTime`, `endTime`, `recurrence`, `overtimeRule`, `assignedEmployees`, `blackoutDates`, audit fields

4. Repository Layer
   - Interfaces: `ShiftTemplateRepository`, `ShiftAssignmentRepository`, `BlackoutDateRepository`
   - Methods: CRUD, conflict detection

5. Service Layer
   - Interface: `SchedulingService`
   - Implementation: `SchedulingServiceImpl`
   - Methods: `createTemplate`, `assignShift`, `detectConflicts`, `bulkAssign`

6. Controller Layer
   - Endpoints:
     - POST `/shifts/templates`
     - POST `/shifts/assign`
     - GET `/shifts/upcoming`
     - POST `/shifts/blackout`
   - Request/Response: `ShiftTemplateDTO`, `ShiftAssignmentDTO`
   - Security: RBAC

7. Configuration
   - Overtime rules, blackout calendar

8. Integration Points
   - Audit logging

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
    private String overtimeRule;
    @ManyToMany
    private List<Employee> assignedEmployees;
    // audit fields
}
```
```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates")
    public ResponseEntity<ShiftTemplateDTO> createTemplate(@Valid @RequestBody ShiftTemplateDTO dto) { ... }
    @PostMapping("/assign")
    public ResponseEntity<?> assignShift(@Valid @RequestBody ShiftAssignmentDTO dto) { ... }
}
```

---

## Epic E06: Leave & Absence Management

### Section: Request Leave, Approve/Deny Leave, Leave Balance Tracking

Description: Manage PTO, sick, unpaid leave requests, approvals, accrual balances, and integration with scheduling and payroll.

Design Specification:
1. Architecture Overview
   - Leave module: Controller â Service â Repository â Entity
   - Patterns: DTO, Factory for leave requests

2. Package Structure
   - `com.warehouse.employee.management.leave`
     - `controller`, `service`, `repository`, `entity`, `dto`, `exception`

3. Domain Model
   - Entity: `LeaveRequest`, `LeaveBalance`
     - Fields: `id`, `employeeId`, `type`, `startDate`, `endDate`, `status`, `approverId`, `balance`, audit fields

4. Repository Layer
   - Interfaces: `LeaveRequestRepository`, `LeaveBalanceRepository`
   - Methods: CRUD, balance update

5. Service Layer
   - Interface: `LeaveService`
   - Implementation: `LeaveServiceImpl`
   - Methods: `requestLeave`, `approveLeave`, `denyLeave`, `trackBalance`

6. Controller Layer
   - Endpoints:
     - POST `/leave/request`
     - POST `/leave/approve`
     - POST `/leave/deny`
     - GET `/leave/balance`
   - Request/Response: `LeaveRequestDTO`, `LeaveBalanceDTO`
   - Security: RBAC

7. Configuration
   - Leave policies, accrual rates

8. Integration Points
   - Scheduling, payroll exclusion

Sample Implementation:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveStatus status;
    @ManyToOne
    private Employee approver;
    // audit fields
}
```
```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/request")
    public ResponseEntity<LeaveRequestDTO> requestLeave(@Valid @RequestBody LeaveRequestDTO dto) { ... }
    @PostMapping("/approve")
    public ResponseEntity<?> approveLeave(@RequestBody ApproveLeaveDTO dto) { ... }
}
```

---

## Epic E07: Training & Certification Tracking

### Section: Add Certification, Certification Expiry Alerts, Block Unqualified Assignments

Description: Track employee certifications, expirations, renewals, and block assignments to tasks requiring valid certifications.

Design Specification:
1. Architecture Overview
   - Certification module: Controller â Service â Repository â Entity
   - Patterns: DTO, Factory for certification creation

2. Package Structure
   - `com.warehouse.employee.management.certification`
     - `controller`, `service`, `repository`, `entity`, `dto`, `exception`

3. Domain Model
   - Entity: `Certification`
     - Fields: `id`, `employeeId`, `type`, `expiryDate`, `documentUrl`, audit fields

4. Repository Layer
   - Interface: `CertificationRepository`
   - Methods: CRUD, expiry alerts

5. Service Layer
   - Interface: `CertificationService`
   - Implementation: `CertificationServiceImpl`
   - Methods: `addCertification`, `renewCertification`, `alertExpiry`, `blockUnqualifiedAssignment`

6. Controller Layer
   - Endpoints:
     - POST `/certifications`
     - GET `/certifications/alerts`
     - GET `/certifications/status`
   - Request/Response: `CertificationDTO`
   - Security: RBAC

7. Configuration
   - Expiry alert thresholds

8. Integration Points
   - Scheduling checks

Sample Implementation:
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type;
    private LocalDate expiryDate;
    private String documentUrl;
    // audit fields
}
```
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<CertificationDTO> addCertification(@Valid @RequestBody CertificationDTO dto) { ... }
    @GetMapping("/alerts")
    public ResponseEntity<List<CertificationAlertDTO>> getAlerts() { ... }
}
```

---

## Epic E08: Safety Incidents & OSHA Reporting

### Section: Record Safety Incident, Incident Investigation Workflow, OSHA Reporting

Description: Record safety incidents, manage investigation workflow, and generate OSHA-compliant reports.

Design Specification:
1. Architecture Overview
   - Safety module: Controller â Service â Repository â Entity
   - Patterns: DTO, Factory for incident creation

2. Package Structure
   - `com.warehouse.employee.management.safety`
     - `controller`, `service`, `repository`, `entity`, `dto`, `exception`

3. Domain Model
   - Entity: `SafetyIncident`
     - Fields: `id`, `severity`, `location`, `description`, `involvedEmployees`, `status`, audit fields

4. Repository Layer
   - Interface: `SafetyIncidentRepository`
   - Methods: CRUD, status workflow

5. Service Layer
   - Interface: `SafetyService`
   - Implementation: `SafetyServiceImpl`
   - Methods: `recordIncident`, `investigateIncident`, `generateOSHAReport`

6. Controller Layer
   - Endpoints:
     - POST `/safety/incidents`
     - POST `/safety/incidents/investigate`
     - GET `/safety/incidents/osha-report`
   - Request/Response: `SafetyIncidentDTO`
   - Security: RBAC

7. Configuration
   - OSHA reporting fields

8. Integration Points
   - Metrics dashboard

Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String severity;
    private String location;
    private String description;
    @ManyToMany
    private List<Employee> involvedEmployees;
    private IncidentStatus status;
    // audit fields
}
```
```java
@RestController
@RequestMapping("/safety")
public class SafetyController {
    @PostMapping("/incidents")
    public ResponseEntity<SafetyIncidentDTO> recordIncident(@Valid @RequestBody SafetyIncidentDTO dto) { ... }
    @GetMapping("/incidents/osha-report")
    public ResponseEntity<OSHAReportDTO> generateOSHAReport() { ... }
}
```

---

## Epic E09: Equipment & Asset Assignment

### Section: Asset Registry CRUD, Asset Check-In/Check-Out, Asset History Log

Description: Manage asset registry, check-in/out, and track asset history with certification validation.

Design Specification:
1. Architecture Overview
   - Asset module: Controller â Service â Repository â Entity
   - Patterns: DTO, Factory for asset creation

2. Package Structure
   - `com.warehouse.employee.management.asset`
     - `controller`, `service`, `repository`, `entity`, `dto`, `exception`

3. Domain Model
   - Entity: `Asset`, `AssetAssignment`
     - Fields: `id`, `type`, `serialNumber`, `status`, `assignedEmployeeId`, `checkoutDate`, `checkinDate`, audit fields

4. Repository Layer
   - Interfaces: `AssetRepository`, `AssetAssignmentRepository`
   - Methods: CRUD, history log

5. Service Layer
   - Interface: `AssetService`
   - Implementation: `AssetServiceImpl`
   - Methods: `createAsset`, `checkoutAsset`, `checkinAsset`, `getAssetHistory`

6. Controller Layer
   - Endpoints:
     - POST `/assets`
     - POST `/assets/checkout`
     - POST `/assets/checkin`
     - GET `/assets/history`
   - Request/Response: `AssetDTO`, `AssetAssignmentDTO`
   - Security: RBAC

7. Configuration
   - Certification validation

8. Integration Points
   - Certification checks

Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private AssetStatus status;
    @ManyToOne
    private Employee assignedEmployee;
    // audit fields
}
```
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/checkout")
    public ResponseEntity<?> checkoutAsset(@Valid @RequestBody AssetCheckoutDTO dto) { ... }
    @PostMapping("/checkin")
    public ResponseEntity<?> checkinAsset(@Valid @RequestBody AssetCheckinDTO dto) { ... }
}
```

---

## Epic E10: Performance Reviews & Goals

### Section: Create Review Cycle, Assign Review, Submit and Acknowledge Review, Export Review as PDF

Description: Manage performance review cycles, assignments, submissions, acknowledgements, and PDF exports.

Design Specification:
1. Architecture Overview
   - Review module: Controller â Service â Repository â Entity
   - Patterns: DTO, Factory for review creation

2. Package Structure
   - `com.warehouse.employee.management.review`
     - `controller`, `service`, `repository`, `entity`, `dto`, `exception`

3. Domain Model
   - Entity: `ReviewCycle`, `PerformanceReview`
     - Fields: `id`, `cycleId`, `employeeId`, `supervisorId`, `ratings`, `comments`, `status`, audit fields

4. Repository Layer
   - Interfaces: `ReviewCycleRepository`, `PerformanceReviewRepository`
   - Methods: CRUD, immutable history

5. Service Layer
   - Interface: `ReviewService`
   - Implementation: `ReviewServiceImpl`
   - Methods: `createCycle`, `assignReview`, `submitReview`, `acknowledgeReview`, `exportPDF`

6. Controller Layer
   - Endpoints:
     - POST `/reviews/cycles`
     - POST `/reviews/assign`
     - POST `/reviews/submit`
     - POST `/reviews/acknowledge`
     - GET `/reviews/export`
   - Request/Response: `ReviewCycleDTO`, `PerformanceReviewDTO`
   - Security: RBAC

7. Configuration
   - PDF export settings

8. Integration Points
   - PDF generation library

Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private ReviewCycle cycle;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private Employee supervisor;
    private String ratings;
    private String comments;
    private ReviewStatus status;
    // audit fields
}
```
```java
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping("/submit")
    public ResponseEntity<?> submitReview(@Valid @RequestBody PerformanceReviewDTO dto) { ... }
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportPDF(@RequestParam Long reviewId) { ... }
}
```

---

## Epic E11: Payroll Export Integration

### Section: Generate Payroll Export, Secure Delivery (SFTP/API), Payroll Export Audit Log

Description: Generate payroll-ready files from attendance and leave data, deliver securely via SFTP/API, and maintain audit logs.

Design Specification:
1. Architecture Overview
   - Payroll module: Controller â Service â Repository â Entity
   - Patterns: DTO, Factory for export creation

2. Package Structure
   - `com.warehouse.employee.management.payroll`
     - `controller`, `service`, `repository`, `entity`, `dto`, `exception`

3. Domain Model
   - Entity: `PayrollExport`
     - Fields: `id`, `exportDate`, `fileUrl`, `status`, `deliveryMethod`, audit fields

4. Repository Layer
   - Interface: `PayrollExportRepository`
   - Methods: CRUD, audit log

5. Service Layer
   - Interface: `PayrollService`
   - Implementation: `PayrollServiceImpl`
   - Methods: `generateExport`, `deliverExport`, `retryDelivery`, `getAuditLog`

6. Controller Layer
   - Endpoints:
     - POST `/payroll/export`
     - POST `/payroll/deliver`
     - GET `/payroll/audit`
   - Request/Response: `PayrollExportDTO`
   - Security: RBAC

7. Configuration
   - SFTP/API credentials, retry policy

8. Integration Points
   - SFTP client, API client

Sample Implementation:
```java
@Entity
public class PayrollExport {
    @Id @GeneratedValue
    private Long id;
    private LocalDate exportDate;
    private String fileUrl;
    private ExportStatus status;
    private String deliveryMethod;
    // audit fields
}
```
```java
@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/export")
    public ResponseEntity<PayrollExportDTO> generateExport() { ... }
    @PostMapping("/deliver")
    public ResponseEntity<?> deliverExport(@RequestBody DeliveryDTO dto) { ... }
}
```

---

## Epic E12: Notifications & Announcements

### Section: Opt-In/Out Notifications, Send Notifications, Announcements Dashboard

Description: Manage user notification preferences, send notifications for events, and display announcements on a dashboard.

Design Specification:
1. Architecture Overview
   - Notification module: Controller â Service â Repository â Entity
   - Patterns: DTO, Factory for notification creation

2. Package Structure
   - `com.warehouse.employee.management.notification`
     - `controller`, `service`, `repository`, `entity`, `dto`, `exception`

3. Domain Model
   - Entity: `NotificationPreference`, `Notification`, `Announcement`
     - Fields: `id`, `userId`, `channel`, `optIn`, `message`, `status`, audit fields

4. Repository Layer
   - Interfaces: `NotificationPreferenceRepository`, `NotificationRepository`, `AnnouncementRepository`
   - Methods: CRUD, delivery tracking

5. Service Layer
   - Interface: `NotificationService`
   - Implementation: `NotificationServiceImpl`
   - Methods: `optIn`, `optOut`, `sendNotification`, `getAnnouncements`

6. Controller Layer
   - Endpoints:
     - POST `/notifications/opt-in`
     - POST `/notifications/opt-out`
     - POST `/notifications/send`
     - GET `/notifications/announcements`
   - Request/Response: `NotificationDTO`, `AnnouncementDTO`
   - Security: RBAC

7. Configuration
   - Email/SMS providers, rate limits

8. Integration Points
   - Email/SMS services

Sample Implementation:
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee recipient;
    private String channel;
    private String message;
    private NotificationStatus status;
    // audit fields
}
```
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@Valid @RequestBody NotificationDTO dto) { ... }
    @GetMapping("/announcements")
    public ResponseEntity<List<AnnouncementDTO>> getAnnouncements() { ... }
}
```

---

## Epic E13: Integration Layer (HRIS/WMS APIs)

### Section: HRIS Sync Job, WMS Integration, Idempotent Webhooks, API Documentation

Description: Integrate with HRIS for employee data sync, WMS for location/department, support idempotent webhooks, and provide OpenAPI documentation.

Design Specification:
1. Architecture Overview
   - Integration module: Controller â Service â Repository â Entity
   - Patterns: DTO, Factory for integration events

2. Package Structure
   - `com.warehouse.employee.management.integration`
     - `controller`, `service`, `repository`, `entity`, `dto`, `exception`

3. Domain Model
   - Entity: `IntegrationEvent`
     - Fields: `id`, `source`, `eventType`, `payload`, `processed`, audit fields

4. Repository Layer
   - Interface: `IntegrationEventRepository`
   - Methods: CRUD, idempotency check

5. Service Layer
   - Interface: `IntegrationService`
   - Implementation: `IntegrationServiceImpl`
   - Methods: `syncHRIS`, `syncWMS`, `handleWebhook`

6. Controller Layer
   - Endpoints:
     - POST `/integration/hris/sync`
     - POST `/integration/wms/sync`
     - POST `/integration/webhook`
   - Request/Response: `IntegrationEventDTO`
   - Security: JWT/OAuth2

7. Configuration
   - HRIS/WMS API credentials

8. Integration Points
   - HRIS API, WMS API

Sample Implementation:
```java
@Entity
public class IntegrationEvent {
    @Id @GeneratedValue
    private Long id;
    private String source;
    private String eventType;
    private String payload;
    private boolean processed;
    // audit fields
}
```
```java
@RestController
@RequestMapping("/integration")
public class IntegrationController {
    @PostMapping("/hris/sync")
    public ResponseEntity<?> syncHRIS() { ... }
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody WebhookDTO dto) { ... }
}
```

---

## Epic E14: Audit Trail & Compliance

### Section: Centralized Audit Logging, Export Audit Logs

Description: Implement centralized audit logging for all sensitive changes and provide export functionality for compliance reporting.

Design Specification:
1. Architecture Overview
   - Audit module: Controller â Service â Repository â Entity
   - Patterns: DTO, Factory for audit log creation

2. Package Structure
   - `com.warehouse.employee.management.audit`
     - `controller`, `service`, `repository`, `entity`, `dto`, `exception`

3. Domain Model
   - Entity: `AuditLog`
     - Fields: `id`, `actor`, `action`, `entity`, `before`, `after`, `timestamp`, audit fields

4. Repository Layer
   - Interface: `AuditLogRepository`
   - Methods: CRUD, export by filters

5. Service Layer
   - Interface: `AuditService`
   - Implementation: `AuditServiceImpl`
   - Methods: `logAction`, `exportAuditLogs`

6. Controller Layer
   - Endpoints:
     - GET `/audit/logs`
     - GET `/audit/export`
   - Request/Response: `AuditLogDTO`
   - Security: RBAC

7. Configuration
   - Immutable log storage

8. Integration Points
   - All CRUD operations

Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String actor;
    private String action;
    private String entity;
    private String before;
    private String after;
    private Instant timestamp;
    // audit fields
}
```
```java
@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/logs")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogs(@RequestParam Map<String, String> filters) { ... }
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAuditLogs(@RequestParam Map<String, String> filters) { ... }
}
```

---

## Epic E15: Reporting & Analytics

### Section: Attendance Reports, Overtime Reports, Safety KPI Dashboard

Description: Generate operational reports for attendance, overtime, leave balances, certification status, and safety KPIs with export functionality.

Design Specification:
1. Architecture Overview
   - Reporting module: Controller â Service â Repository â Entity
   - Patterns: DTO, Factory for report generation

2. Package Structure
   - `com.warehouse.employee.management.reporting`
     - `controller`, `service`, `repository`, `entity`, `dto`, `exception`

3. Domain Model
   - Entity: `Report`
     - Fields: `id`, `type`, `filters`, `generatedAt`, `fileUrl`, audit fields

4. Repository Layer
   - Interface: `ReportRepository`
   - Methods: CRUD, report generation

5. Service Layer
   - Interface: `ReportingService`
   - Implementation: `ReportingServiceImpl`
   - Methods: `generateAttendanceReport`, `generateOvertimeReport`, `generateSafetyKPIDashboard`

6. Controller Layer
   - Endpoints:
     - GET `/reports/attendance`
     - GET `/reports/overtime`
     - GET `/reports/safety-kpi`
   - Request/Response: `ReportDTO`
   - Security: RBAC

7. Configuration
   - Report export settings

8. Integration Points
   - BI tools

Sample Implementation:
```java
@Entity
public class Report {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String filters;
    private Instant generatedAt;
    private String fileUrl;
    // audit fields
}
```
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<ReportDTO> generateAttendanceReport(@RequestParam Map<String, String> filters) { ... }
    @GetMapping("/overtime")
    public ResponseEntity<ReportDTO> generateOvertimeReport(@RequestParam Map<String, String> filters) { ... }
}
```

---

## Epic E16: Mobile Access (PWA)

### Section: Mobile Clock-In/Out, Mobile Schedule View, PWA Installation

Description: Provide mobile-friendly views for clock-in/out, schedule viewing, and PWA installation with offline support.

Design Specification:
1. Architecture Overview
   - Mobile module: Controller â Service â Repository â Entity
   - Patterns: DTO, Factory for mobile events

2. Package Structure
   - `com.warehouse.employee.management.mobile`
     - `controller`, `service`, `repository`, `entity`, `dto`, `exception`

3. Domain Model
   - Entity: `MobileEvent`
     - Fields: `id`, `userId`, `eventType`, `timestamp`, `synced`, audit fields

4. Repository Layer
   - Interface: `MobileEventRepository`
   - Methods: CRUD, offline queue

5. Service Layer
   - Interface: `MobileService`
   - Implementation: `MobileServiceImpl`
   - Methods: `clockInMobile`, `clockOutMobile`, `getScheduleMobile`, `syncOfflineEvents`

6. Controller Layer
   - Endpoints:
     - POST `/mobile/clock-in`
     - POST `/mobile/clock-out`
     - GET `/mobile/schedule`
   - Request/Response: `MobileEventDTO`
   - Security: RBAC

7. Configuration
   - PWA manifest, offline queue

8. Integration Points
   - Attendance module

Sample Implementation:
```java
@Entity
public class MobileEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee user;
    private String eventType;
    private Instant timestamp;
    private boolean synced;
    // audit fields
}
```
```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockInMobile(@Valid @RequestBody MobileEventDTO dto) { ... }
    @GetMapping("/schedule")
    public ResponseEntity<List<ShiftDTO>> getScheduleMobile() { ... }
}
```

---

## Epic E17: Onboarding & Offboarding Workflow

### Section: Automated Onboarding, Automated Offboarding

Description: Automate onboarding tasks (training, asset assignment) and offboarding tasks (access revocation, asset collection) triggered by HRIS events.

Design Specification:
1. Architecture Overview
   - Workflow module: Controller â Service â Repository â Entity
   - Patterns: DTO, Factory for workflow tasks

2. Package Structure
   - `com.warehouse.employee.management.workflow`
     - `controller`, `service`, `repository`, `entity`, `dto`, `exception`

3. Domain Model
   - Entity: `WorkflowTask`
     - Fields: `id`, `employeeId`, `taskType`, `status`, `completedAt`, audit fields

4. Repository Layer
   - Interface: `WorkflowTaskRepository`
   - Methods: CRUD, task generation

5. Service Layer
   - Interface: `WorkflowService`
   - Implementation: `WorkflowServiceImpl`
   - Methods: `onboardEmployee`, `offboardEmployee`, `completeTask`

6. Controller Layer
   - Endpoints:
     - POST `/workflow/onboard`
     - POST `/workflow/offboard`
     - POST `/workflow/complete-task`
   - Request/Response: `WorkflowTaskDTO`
   - Security: RBAC

7. Configuration
   - Task templates

8. Integration Points
   - HRIS, training, asset modules

Sample Implementation:
```java
@Entity
public class WorkflowTask {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String taskType;
    private TaskStatus status;
    private Instant completedAt;
    // audit fields
}
```
```java
@RestController
@RequestMapping("/workflow")
public class WorkflowController {
    @PostMapping("/onboard")
    public ResponseEntity<?> onboardEmployee(@RequestBody OnboardDTO dto) { ... }
    @PostMapping("/offboard")
    public ResponseEntity<?> offboardEmployee(@RequestBody OffboardDTO dto) { ... }
}
```

---

## Epic E18: Localization & Multi-Tenant

### Section: Tenant Isolation, Localization Support

Description: Implement tenant isolation for multi-warehouse deployments and localization support for UI strings and timezone-aware timestamps.

Design Specification:
1. Architecture Overview
   - Tenant module: Controller â Service â Repository â Entity
   - Patterns: DTO, Factory for tenant creation

2. Package Structure
   - `com.warehouse.employee.management.tenant`
     - `controller`, `service`, `repository`, `entity`, `dto`, `exception`

3. Domain Model
   - Entity: `Tenant`
     - Fields: `id`, `name`, `locale`, `timezone`, audit fields

4. Repository Layer
   - Interface: `TenantRepository`
   - Methods: CRUD, tenant isolation

5. Service Layer
   - Interface: `TenantService`
   - Implementation: `TenantServiceImpl`
   - Methods: `createTenant`, `getTenant`, `updateTenant`

6. Controller Layer
   - Endpoints:
     - POST `/tenants`
     - GET `/tenants/{id}`
   - Request/Response: `TenantDTO`
   - Security: RBAC

7. Configuration
   - Tenant ID in all entities, locale config

8. Integration Points
   - All modules

Sample Implementation:
```java
@Entity
public class Tenant {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String locale;
    private String timezone;
    // audit fields
}
```
```java
@RestController
@RequestMapping("/tenants")
public class TenantController {
    @PostMapping
    public ResponseEntity<TenantDTO> createTenant(@Valid @RequestBody TenantDTO dto) { ... }
    @GetMapping("/{id}")
    public ResponseEntity<TenantDTO> getTenant(@PathVariable Long id) { ... }
}
```

---

## Epic E19: Observability & Monitoring

### Section: Structured Logging, Distributed Tracing, Metrics and Alerting

Description: Implement structured logging with traceId, distributed tracing with OpenTelemetry, and metrics/alerting with Prometheus.

Design Specification:
1. Architecture Overview
   - Observability module: Controller â Service â Repository â Entity
   - Patterns: DTO, Factory for log/trace creation

2. Package Structure
   - `com.warehouse.employee.management.observability`
     - `controller`, `service`, `repository`, `entity`, `dto`, `exception`

3. Domain Model
   - Entity: `LogEntry`, `Trace`
     - Fields: `id`, `traceId`, `message`, `level`, `timestamp`, audit fields

4. Repository Layer
   - Interface: `LogEntryRepository`
   - Methods: CRUD, log search

5. Service Layer
   - Interface: `ObservabilityService`
   - Implementation: `ObservabilityServiceImpl`
   - Methods: `logMessage`, `traceRequest`, `exportMetrics`

6. Controller Layer
   - Endpoints:
     - GET `/observability/logs`
     - GET `/observability/traces`
     - GET `/observability/metrics`
   - Request/Response: `LogEntryDTO`, `TraceDTO`
   - Security: RBAC

7. Configuration
   - OpenTelemetry, Prometheus

8. Integration Points
   - All modules

Sample Implementation:
```java
@Entity
public class LogEntry {
    @Id @GeneratedValue
    private Long id;
    private String traceId;
    private String message;
    private String level;
    private Instant timestamp;
    // audit fields
}
```
```java
@RestController
@RequestMapping("/observability")
public class ObservabilityController {
    @GetMapping("/logs")
    public ResponseEntity<List<LogEntryDTO>> getLogs(@RequestParam Map<String, String> filters) { ... }
    @GetMapping("/metrics")
    public ResponseEntity<String> exportMetrics() { ... }
}
```

---

## Epic E20: CI/CD & Deployment Automation

### Section: CI Pipeline, CD Pipeline

Description: Implement CI pipeline for automated testing on PRs and CD pipeline for blue-green/canary deployments with rollback support.

Design Specification:
1. Architecture Overview
   - CI/CD module: Controller â Service â Repository â Entity
   - Patterns: DTO, Factory for pipeline creation

2. Package Structure
   - `com.warehouse.employee.management.cicd`
     - `controller`, `service`, `repository`, `entity`, `dto`, `exception`

3. Domain Model
   - Entity: `PipelineRun`
     - Fields: `id`, `type`, `status`, `startedAt`, `completedAt`, audit fields

4. Repository Layer
   - Interface: `PipelineRunRepository`
   - Methods: CRUD, pipeline history

5. Service Layer
   - Interface: `CICDService`
   - Implementation: `CICDServiceImpl`
   - Methods: `runCIPipeline`, `runCDPipeline`, `rollback`

6. Controller Layer
   - Endpoints:
     - POST `/cicd/ci/run`
     - POST `/cicd/cd/deploy`
     - POST `/cicd/cd/rollback`
   - Request/Response: `PipelineRunDTO`
   - Security: RBAC

7. Configuration
   - CI/CD tool integration

8. Integration Points
   - GitHub Actions, Jenkins

Sample Implementation:
```java
@Entity
public class PipelineRun {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private PipelineStatus status;
    private Instant startedAt;
    private Instant completedAt;
    // audit fields
}
```
```java
@RestController
@RequestMapping("/cicd")
public class CICDController {
    @PostMapping("/ci/run")
    public ResponseEntity<PipelineRunDTO> runCIPipeline() { ... }
    @PostMapping("/cd/deploy")
    public ResponseEntity<PipelineRunDTO> runCDPipeline(@RequestBody DeployDTO dto) { ... }
}
```

---

## Conclusion

This technical design document provides comprehensive low-level design specifications for all 60 user stories across 20 epics of the Warehouse Employee Management System. Each section includes detailed architecture, package structure, domain models, repository/service/controller layers, configuration, integration points, and sample implementations following Spring Boot best practices.