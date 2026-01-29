==================================================================
Warehouse Employee Management System â Low-Level Technical Design
==================================================================

This document provides a comprehensive, production-ready low-level technical design for all 20 Warehouse Employee Management System epics. Each epic includes: architecture overview, package/module/component breakdown, entity design, service/repository/controller specifications, configuration/security, integration points, and code snippets. All designs follow Spring Boot best practices.

---

==================================================
Epic E01: Project Scaffolding & Domain Setup
==================================================

Section: Overview of Spring Boot Architecture
Description:
The system is structured as a modular Spring Boot (Maven) application, using layered architecture (Controller â Service â Repository) with clear separation of concerns. Core modules include employee, scheduling, attendance, and safety. Flyway/Liquibase is used for DB migrations, and Actuator is enabled for health checks and monitoring.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `com.company.wms` (root)
  - `employee`
  - `scheduling`
  - `attendance`
  - `safety`
  - `config`
  - `common`
  - `integration`
  - `audit`
  - `reporting`
- Each module contains: `controller`, `service`, `repository`, `model`, `dto`, `exception`

Sample Implementation:
```java
com.company.wms
 âââ employee
 â    âââ controller
 â    âââ service
 â    âââ repository
 â    âââ model
 â    âââ dto
 â    âââ exception
 âââ scheduling
 âââ attendance
 âââ safety
 âââ config
 âââ common
 âââ integration
 âââ audit
 âââ reporting
```

Section: Configuration and Security Settings
Design Specification:
- `application.yml` for environment configs
- Flyway/Liquibase migration scripts in `src/main/resources/db/migration`
- Spring Boot Actuator enabled (`management.endpoints.web.exposure.include=*`)
- Profiles: `dev`, `test`, `prod`

Sample Implementation:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wms
    username: wms_user
    password: secret
  flyway:
    enabled: true
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

Section: Integration Points
Design Specification:
- None for scaffolding, but integration modules stubbed for HRIS, WMS, IDP.

Section: Code Snippet
```java
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}
```

---

==================================================
Epic E02: Employee Master Data (CRUD)
==================================================

Section: Overview of Spring Boot Architecture
Description:
Implements the Employee domain with full CRUD REST APIs, using DTOs for web layer, JPA for persistence, and service layer for business logic. Supports soft-delete, pagination, filtering, and unique badgeId enforcement.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `employee/model/Employee.java`
- `employee/dto/EmployeeDTO.java`
- `employee/repository/EmployeeRepository.java`
- `employee/service/EmployeeService.java`
- `employee/controller/EmployeeController.java`

Section: Entity Design with Domain Models and Relationships
Design Specification:
- Employee fields: id, name, badgeId (unique), role, department, shiftGroup, hireDate, status, deleted (soft-delete)
- Relationships: Many employees to one department, one shiftGroup

Sample Implementation:
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;
    @Enumerated(EnumType.STRING)
    private Role role;
    @ManyToOne
    private Department department;
    @ManyToOne
    private ShiftGroup shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    private boolean deleted = false;
    // getters/setters
}
```

Section: Service Layer Specifications
Design Specification:
- Business logic for create, update, soft-delete, filter, pagination
- Transactional methods
- Validation for unique badgeId

Sample Implementation:
```java
@Service
public class EmployeeService {
    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO dto) { ... }
    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) { ... }
    @Transactional
    public void softDeleteEmployee(Long id) { ... }
    public Page<EmployeeDTO> listEmployees(EmployeeFilter filter, Pageable pageable) { ... }
}
```

Section: Repository Layer
Design Specification:
- Extends `JpaRepository<Employee, Long>`
- Custom query for filtering and soft-delete

Sample Implementation:
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

Section: Controller Specifications
Design Specification:
- REST endpoints: POST/GET/PUT/PATCH/DELETE `/employees`
- Request/response DTOs
- OpenAPI annotations

Sample Implementation:
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeDTO dto) { ... }
    @GetMapping
    public Page<EmployeeDTO> list(EmployeeFilter filter, Pageable pageable) { ... }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
}
```

Section: Configuration and Security Settings
Design Specification:
- Method security for RBAC (see E03)
- OpenAPI schema generation

Section: Integration Points
Design Specification:
- HRIS sync (see E13)

---

==================================================
Epic E03: Role-Based Access Control (RBAC)
==================================================

Section: Overview of Spring Boot Architecture
Description:
Uses Spring Security for authentication and authorization. Roles: ADMIN, HR, SUPERVISOR, WORKER. Method/endpoint security with `@PreAuthorize`. Row-level constraints for supervisors. Supports API key/OAuth2 via config.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `config/SecurityConfig.java`
- `common/security/CustomUserDetailsService.java`
- `common/security/ApiKeyAuthFilter.java`

Section: Entity Design
Design Specification:
- User entity with roles
- Employee references User

Sample Implementation:
```java
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    private String username;
    private String password;
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;
}
```

Section: Service Layer Specifications
Design Specification:
- UserDetailsService for authentication
- Role checks in service methods

Sample Implementation:
```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) { ... }
}
```

Section: Repository Layer
Design Specification:
- `UserRepository extends JpaRepository<User, Long>`

Section: Controller Specifications
Design Specification:
- `/auth/login`, `/auth/logout`
- Secured endpoints with `@PreAuthorize`

Sample Implementation:
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/employees")
public ResponseEntity<EmployeeDTO> create(...) { ... }
```

Section: Configuration and Security Settings
Design Specification:
- `SecurityConfig` for HTTP security, JWT/OAuth2, API key toggle
- Row-level security in repository/service

Sample Implementation:
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR")
            .anyRequest().authenticated()
            .and()
            .oauth2Login()
            .and()
            .csrf().disable();
    }
}
```

Section: Integration Points
Design Specification:
- OAuth2 with IDP (see E13)

---

==================================================
Epic E04: Time & Attendance (Clock In/Out)
==================================================

Section: Overview of Spring Boot Architecture
Description:
Implements endpoints for clock-in/out, capturing geofence and device info. Calculates hours worked, handles missed punches, and supports corrections workflow.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `attendance/model/AttendanceRecord.java`
- `attendance/dto/ClockEventDTO.java`
- `attendance/repository/AttendanceRepository.java`
- `attendance/service/AttendanceService.java`
- `attendance/controller/AttendanceController.java`

Section: Entity Design
Design Specification:
- AttendanceRecord: id, employee, clockIn, clockOut, deviceId, geoLocation, status, correctionRequested

Sample Implementation:
```java
@Entity
public class AttendanceRecord {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private String deviceId;
    private String geoLocation;
    @Enumerated(EnumType.STRING)
    private Status status; // NORMAL, MISSED, CORRECTION_REQUESTED
}
```

Section: Service Layer Specifications
Design Specification:
- Clock-in/out logic, validation, shift association, missed punch detection, correction workflow

Sample Implementation:
```java
@Service
public class AttendanceService {
    @Transactional
    public AttendanceRecord clockIn(ClockEventDTO dto) { ... }
    @Transactional
    public AttendanceRecord clockOut(ClockEventDTO dto) { ... }
    public List<AttendanceRecord> getDailyTotals(Long employeeId, LocalDate date) { ... }
}
```

Section: Repository Layer
Design Specification:
- `AttendanceRepository extends JpaRepository<AttendanceRecord, Long>`

Section: Controller Specifications
Design Specification:
- POST `/attendance/clock-in`, `/attendance/clock-out`
- GET `/attendance/reports`

Sample Implementation:
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceRecordDTO> clockIn(@RequestBody ClockEventDTO dto) { ... }
    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceRecordDTO> clockOut(@RequestBody ClockEventDTO dto) { ... }
}
```

Section: Configuration and Security Settings
Design Specification:
- Only authenticated employees can clock in/out

Section: Integration Points
Design Specification:
- Payroll export (see E11)

---

==================================================
Epic E05: Shift & Schedule Management
==================================================

Section: Overview of Spring Boot Architecture
Description:
Manages recurring shift templates, rotations, overtime rules, employee assignments, blackout dates, and operation calendars.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `scheduling/model/ShiftTemplate.java`
- `scheduling/model/Schedule.java`
- `scheduling/repository/ShiftTemplateRepository.java`
- `scheduling/service/SchedulingService.java`
- `scheduling/controller/SchedulingController.java`

Section: Entity Design
Design Specification:
- ShiftTemplate: id, name, startTime, endTime, recurrence, overtimeRule
- Schedule: id, employee, shiftTemplate, date, status

Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrence; // e.g., "WEEKLY"
    private String overtimeRule;
}
@Entity
public class Schedule {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ShiftTemplate shiftTemplate;
    private LocalDate date;
    @Enumerated(EnumType.STRING)
    private Status status;
}
```

Section: Service Layer Specifications
Design Specification:
- CRUD for shift templates/schedules, conflict detection, bulk assignment, audit logging

Sample Implementation:
```java
@Service
public class SchedulingService {
    @Transactional
    public ShiftTemplate createTemplate(ShiftTemplateDTO dto) { ... }
    @Transactional
    public Schedule assignShift(Long employeeId, Long templateId, LocalDate date) { ... }
    public boolean hasConflict(Long employeeId, LocalDate date) { ... }
}
```

Section: Repository Layer
Design Specification:
- `ShiftTemplateRepository`, `ScheduleRepository`

Section: Controller Specifications
Design Specification:
- CRUD endpoints for `/shifts`, `/schedules`
- Bulk assignment endpoint

Sample Implementation:
```java
@RestController
@RequestMapping("/scheduling")
public class SchedulingController {
    @PostMapping("/shifts")
    public ResponseEntity<ShiftTemplateDTO> createShift(@RequestBody ShiftTemplateDTO dto) { ... }
    @PostMapping("/assign")
    public ResponseEntity<Void> bulkAssign(@RequestBody BulkAssignDTO dto) { ... }
}
```

Section: Configuration and Security Settings
Design Specification:
- Supervisors can assign shifts to their teams only

Section: Integration Points
Design Specification:
- Calendar integration, audit trail

---

==================================================
Epic E06: Leave & Absence Management
==================================================

Section: Overview of Spring Boot Architecture
Description:
Handles PTO, sick, unpaid leave requests/approvals, accrual balances, and integration with scheduling/payroll.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `attendance/model/LeaveRequest.java`
- `attendance/service/LeaveService.java`
- `attendance/controller/LeaveController.java`

Section: Entity Design
Design Specification:
- LeaveRequest: id, employee, type, startDate, endDate, status, approver, accrualBalance

Sample Implementation:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @Enumerated(EnumType.STRING)
    private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private Status status; // REQUESTED, APPROVED, DENIED
    @ManyToOne
    private Employee approver;
    private BigDecimal accrualBalance;
}
```

Section: Service Layer Specifications
Design Specification:
- Request, approve/deny leave, update balances, auto-flag scheduled shifts

Sample Implementation:
```java
@Service
public class LeaveService {
    @Transactional
    public LeaveRequest requestLeave(LeaveRequestDTO dto) { ... }
    @Transactional
    public LeaveRequest approveLeave(Long requestId, Long approverId) { ... }
}
```

Section: Repository Layer
Design Specification:
- `LeaveRepository extends JpaRepository<LeaveRequest, Long>`

Section: Controller Specifications
Design Specification:
- POST `/leave/request`, PATCH `/leave/approve/{id}`

Sample Implementation:
```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/request")
    public ResponseEntity<LeaveRequestDTO> request(@RequestBody LeaveRequestDTO dto) { ... }
    @PatchMapping("/approve/{id}")
    public ResponseEntity<LeaveRequestDTO> approve(@PathVariable Long id) { ... }
}
```

Section: Integration Points
Design Specification:
- Scheduling (auto-flag), Payroll export

---

==================================================
Epic E07: Training & Certification Tracking
==================================================

Section: Overview of Spring Boot Architecture
Description:
Tracks employee certifications, expirations, renewals, and blocks assignments for expired certs. Supports document uploads.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `employee/model/Certification.java`
- `employee/service/CertificationService.java`
- `employee/controller/CertificationController.java`

Section: Entity Design
Design Specification:
- Certification: id, employee, type, issueDate, expiryDate, documentUrl, status

Sample Implementation:
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
    @Enumerated(EnumType.STRING)
    private Status status; // VALID, EXPIRED
}
```

Section: Service Layer Specifications
Design Specification:
- CRUD, expiry alerts, block scheduling if expired

Sample Implementation:
```java
@Service
public class CertificationService {
    public Certification createCertification(CertificationDTO dto) { ... }
    public void checkExpirations() { ... }
}
```

Section: Repository Layer
Design Specification:
- `CertificationRepository`

Section: Controller Specifications
Design Specification:
- CRUD endpoints `/certifications`
- Upload endpoint

Sample Implementation:
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<CertificationDTO> create(@RequestBody CertificationDTO dto) { ... }
    @PostMapping("/{id}/upload")
    public ResponseEntity<Void> uploadDocument(@PathVariable Long id, @RequestParam MultipartFile file) { ... }
}
```

Section: Integration Points
Design Specification:
- Scheduling (block assignment), Notifications (expiry alerts)

---

==================================================
Epic E08: Safety Incidents & OSHA Reporting
==================================================

Section: Overview of Spring Boot Architecture
Description:
Records safety incidents, supports investigation workflow, corrective actions, and OSHA summary generation.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `safety/model/SafetyIncident.java`
- `safety/service/SafetyService.java`
- `safety/controller/SafetyController.java`

Section: Entity Design
Design Specification:
- SafetyIncident: id, date, location, description, severity, status, involvedEmployees, correctiveActions

Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private LocalDate date;
    private String location;
    private String description;
    @Enumerated(EnumType.STRING)
    private Severity severity;
    @Enumerated(EnumType.STRING)
    private Status status; // OPEN, INVESTIGATING, RESOLVED
    @ManyToMany
    private List<Employee> involvedEmployees;
    private String correctiveActions;
}
```

Section: Service Layer Specifications
Design Specification:
- Record incident, manage workflow, generate OSHA reports

Sample Implementation:
```java
@Service
public class SafetyService {
    public SafetyIncident reportIncident(SafetyIncidentDTO dto) { ... }
    public void updateStatus(Long id, Status status) { ... }
    public List<OSHAReportDTO> generateOSHAReport(LocalDate from, LocalDate to) { ... }
}
```

Section: Repository Layer
Design Specification:
- `SafetyIncidentRepository`

Section: Controller Specifications
Design Specification:
- POST `/safety/incidents`, PATCH `/safety/incidents/{id}/status`, GET `/safety/osha-report`

Sample Implementation:
```java
@RestController
@RequestMapping("/safety")
public class SafetyController {
    @PostMapping("/incidents")
    public ResponseEntity<SafetyIncidentDTO> report(@RequestBody SafetyIncidentDTO dto) { ... }
    @PatchMapping("/incidents/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody StatusDTO status) { ... }
}
```

Section: Integration Points
Design Specification:
- Reporting, Notifications

---

==================================================
Epic E09: Equipment & Asset Assignment
==================================================

Section: Overview of Spring Boot Architecture
Description:
Manages assignment of assets (scanners, forklifts, PPE) to employees, tracks check-in/out, blocks use if certification missing, tracks asset condition.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `asset/model/Asset.java`
- `asset/model/AssetAssignment.java`
- `asset/service/AssetService.java`
- `asset/controller/AssetController.java`

Section: Entity Design
Design Specification:
- Asset: id, type, serialNumber, condition, assignedTo
- AssetAssignment: id, asset, employee, checkoutDate, returnDate, status

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
    @ManyToOne
    private Employee assignedTo;
}
@Entity
public class AssetAssignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Asset asset;
    @ManyToOne
    private Employee employee;
    private LocalDateTime checkoutDate;
    private LocalDateTime returnDate;
    @Enumerated(EnumType.STRING)
    private Status status;
}
```

Section: Service Layer Specifications
Design Specification:
- CRUD, check-in/out, block if cert invalid, history log

Sample Implementation:
```java
@Service
public class AssetService {
    public AssetAssignment checkoutAsset(Long assetId, Long employeeId) { ... }
    public void returnAsset(Long assignmentId) { ... }
}
```

Section: Repository Layer
Design Specification:
- `AssetRepository`, `AssetAssignmentRepository`

Section: Controller Specifications
Design Specification:
- POST `/assets/checkout`, `/assets/return`

Sample Implementation:
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/checkout")
    public ResponseEntity<AssetAssignmentDTO> checkout(@RequestBody CheckoutDTO dto) { ... }
    @PostMapping("/return")
    public ResponseEntity<Void> returnAsset(@RequestBody ReturnDTO dto) { ... }
}
```

Section: Integration Points
Design Specification:
- Certification check, audit log

---

==================================================
Epic E10: Performance Reviews & Goals
==================================================

Section: Overview of Spring Boot Architecture
Description:
Manages review templates, goals, competencies, ratings, comments, and supervisor/employee acknowledgements.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `employee/model/PerformanceReview.java`
- `employee/model/Goal.java`
- `employee/service/ReviewService.java`
- `employee/controller/ReviewController.java`

Section: Entity Design
Design Specification:
- PerformanceReview: id, employee, reviewer, period, template, ratings, comments, status
- Goal: id, review, description, status

Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private Employee reviewer;
    private String period;
    private String template;
    private String ratings;
    private String comments;
    @Enumerated(EnumType.STRING)
    private Status status; // DRAFT, SUBMITTED, ACKNOWLEDGED
}
@Entity
public class Goal {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private PerformanceReview review;
    private String description;
    @Enumerated(EnumType.STRING)
    private Status status;
}
```

Section: Service Layer Specifications
Design Specification:
- Create review cycles, assign, submit, acknowledge, PDF export

Sample Implementation:
```java
@Service
public class ReviewService {
    public PerformanceReview createReview(ReviewDTO dto) { ... }
    public void acknowledgeReview(Long reviewId, Long employeeId) { ... }
}
```

Section: Repository Layer
Design Specification:
- `PerformanceReviewRepository`, `GoalRepository`

Section: Controller Specifications
Design Specification:
- POST `/reviews`, PATCH `/reviews/{id}/acknowledge`

Sample Implementation:
```java
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping
    public ResponseEntity<ReviewDTO> create(@RequestBody ReviewDTO dto) { ... }
    @PatchMapping("/{id}/acknowledge")
    public ResponseEntity<Void> acknowledge(@PathVariable Long id) { ... }
}
```

Section: Integration Points
Design Specification:
- PDF export, audit log

---

==================================================
Epic E11: Payroll Export Integration
==================================================

Section: Overview of Spring Boot Architecture
Description:
Generates payroll-ready files from attendance/leave, maps to provider formats, delivers via SFTP/API.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `integration/payroll/PayrollExportService.java`
- `integration/payroll/PayrollController.java`

Section: Service Layer Specifications
Design Specification:
- Generate export, reconcile totals, retry failed deliveries, audit log

Sample Implementation:
```java
@Service
public class PayrollExportService {
    public File generatePayrollExport(LocalDate from, LocalDate to) { ... }
    public void deliverExport(File file) { ... }
}
```

Section: Controller Specifications
Design Specification:
- POST `/payroll/export`

Sample Implementation:
```java
@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/export")
    public ResponseEntity<Resource> exportPayroll(@RequestBody ExportRequestDTO dto) { ... }
}
```

Section: Integration Points
Design Specification:
- SFTP/API delivery, audit log

---

==================================================
Epic E12: Notifications & Announcements
==================================================

Section: Overview of Spring Boot Architecture
Description:
Handles in-app, email, and SMS notifications for shift changes, expiring certs, approvals, announcements, with quiet hours.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `common/notification/NotificationService.java`
- `common/notification/AnnouncementController.java`

Section: Service Layer Specifications
Design Specification:
- Send notifications, manage templates, track delivery, rate limits

Sample Implementation:
```java
@Service
public class NotificationService {
    public void sendNotification(NotificationDTO dto) { ... }
    public void sendAnnouncement(AnnouncementDTO dto) { ... }
}
```

Section: Controller Specifications
Design Specification:
- POST `/notifications`, `/announcements`

Sample Implementation:
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping
    public ResponseEntity<Void> send(@RequestBody NotificationDTO dto) { ... }
}
```

Section: Integration Points
Design Specification:
- Email/SMS providers, localization

---

==================================================
Epic E13: Integration Layer (HRIS/WMS APIs)
==================================================

Section: Overview of Spring Boot Architecture
Description:
Exposes REST APIs and connectors for HRIS, WMS, and IDP for SSO. Supports webhooks for events.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `integration/hris/HRISService.java`
- `integration/wms/WMSService.java`
- `integration/idp/IDPService.java`
- `integration/webhook/WebhookController.java`

Section: Service Layer Specifications
Design Specification:
- Sync jobs, idempotent webhooks, JWT/OAuth2 security

Sample Implementation:
```java
@Service
public class HRISService {
    public void syncEmployees() { ... }
}
@RestController
@RequestMapping("/webhooks")
public class WebhookController {
    @PostMapping
    public ResponseEntity<Void> handleEvent(@RequestBody WebhookEventDTO dto) { ... }
}
```

Section: Integration Points
Design Specification:
- HRIS, WMS, IDP

---

==================================================
Epic E14: Audit Trail & Compliance
==================================================

Section: Overview of Spring Boot Architecture
Description:
Centralized audit logging for sensitive changes, tamper-evident storage, actor/timestamp/before/after tracking.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `audit/model/AuditLog.java`
- `audit/service/AuditService.java`
- `audit/controller/AuditController.java`

Section: Entity Design
Design Specification:
- AuditLog: id, entity, entityId, actor, timestamp, before, after, action

Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private Long entityId;
    private String actor;
    private LocalDateTime timestamp;
    @Lob
    private String before;
    @Lob
    private String after;
    private String action;
}
```

Section: Service Layer Specifications
Design Specification:
- Log create/update/delete, immutable storage

Sample Implementation:
```java
@Service
public class AuditService {
    public void logChange(String entity, Long entityId, String actor, Object before, Object after, String action) { ... }
}
```

Section: Controller Specifications
Design Specification:
- GET `/audit/logs`

---

==================================================
Epic E15: Reporting & Analytics
==================================================

Section: Overview of Spring Boot Architecture
Description:
Provides operational reports (attendance, overtime, leave, certs, safety KPIs), CSV/PDF export, dashboards.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `reporting/service/ReportingService.java`
- `reporting/controller/ReportingController.java`

Section: Service Layer Specifications
Design Specification:
- Generate reports, filter, export, metrics endpoints

Sample Implementation:
```java
@Service
public class ReportingService {
    public ReportDTO generateAttendanceReport(ReportFilter filter) { ... }
    public Resource exportReport(ReportType type, ReportFilter filter, ExportFormat format) { ... }
}
```

Section: Controller Specifications
Design Specification:
- GET `/reports/attendance`, `/reports/export`

---

==================================================
Epic E16: Mobile Access (PWA)
==================================================

Section: Overview of Spring Boot Architecture
Description:
Provides responsive views for clock-in/out, schedules, leave requests, announcements. Offline-friendly PWA.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `web/pwa/` (static resources, manifest, service worker)
- REST endpoints reused from attendance, scheduling, leave, notifications

Section: Integration Points
Design Specification:
- Service worker for offline, manifest for installability

Sample Implementation:
```json
// manifest.json
{
  "name": "Warehouse Employee Portal",
  "short_name": "WMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

---

==================================================
Epic E17: Onboarding & Offboarding Workflow
==================================================

Section: Overview of Spring Boot Architecture
Description:
Automates provisioning of accounts, initial schedule, required training, deprovisioning on termination.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `employee/service/OnboardingService.java`
- `employee/service/OffboardingService.java`

Section: Service Layer Specifications
Design Specification:
- Onboarding: create user, assign schedule, generate training tasks
- Offboarding: revoke access, collect assets, update schedules

Sample Implementation:
```java
@Service
public class OnboardingService {
    public void onboardEmployee(EmployeeDTO dto) { ... }
}
@Service
public class OffboardingService {
    public void offboardEmployee(Long employeeId) { ... }
}
```

---

==================================================
Epic E18: Localization & Multi-Site
==================================================

Section: Overview of Spring Boot Architecture
Description:
Supports multiple warehouses with site-specific shifts, holidays, policies, and UI localization.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `common/model/Site.java`
- `common/config/LocalizationConfig.java`

Section: Entity Design
Design Specification:
- Site: id, name, location, shifts, holidays, policies

Sample Implementation:
```java
@Entity
public class Site {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String location;
}
```

Section: Configuration and Security Settings
Design Specification:
- Spring `MessageSource` for i18n
- Site context in user session

Sample Implementation:
```java
@Bean
public MessageSource messageSource() {
    ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
    ms.setBasename("classpath:messages");
    ms.setDefaultEncoding("UTF-8");
    return ms;
}
```

---

==================================================
Epic E19: Advanced Scheduling (AI/Optimization)
==================================================

Section: Overview of Spring Boot Architecture
Description:
Implements ML/heuristics-based optimal shift assignments using demand forecasts, skills, preferences, constraints.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `scheduling/service/OptimizationService.java`

Section: Service Layer Specifications
Design Specification:
- Generate optimal schedules, expose API for recommendations

Sample Implementation:
```java
@Service
public class OptimizationService {
    public List<ScheduleDTO> optimizeSchedules(DemandForecastDTO forecast) { ... }
}
```

Section: Controller Specifications
Design Specification:
- POST `/scheduling/optimize`

---

==================================================
Epic E20: Self-Service Portal
==================================================

Section: Overview of Spring Boot Architecture
Description:
Provides employee portal for pay stubs, contact info updates, leave requests, schedules, training modules.

Section: Package Structure, Module Definitions, Component Breakdown
Design Specification:
- `web/portal/PortalController.java`

Section: Controller Specifications
Design Specification:
- GET `/portal/paystubs`, `/portal/profile`, `/portal/schedule`, `/portal/training`

Sample Implementation:
```java
@RestController
@RequestMapping("/portal")
public class PortalController {
    @GetMapping("/paystubs")
    public ResponseEntity<List<PayStubDTO>> getPayStubs() { ... }
    @GetMapping("/profile")
    public ResponseEntity<EmployeeProfileDTO> getProfile() { ... }
}
```

---

========================
General Best Practices
========================
- Use `@Valid` for DTO validation, `@ControllerAdvice` for exception handling
- Dependency injection via constructor
- Transaction management with `@Transactional`
- RESTful API design, OpenAPI annotations
- Proper use of `@Entity`, `@Repository`, `@Service`, `@RestController`
- Logging and error handling
- Unit and integration tests for all modules

---

END OF DOCUMENT