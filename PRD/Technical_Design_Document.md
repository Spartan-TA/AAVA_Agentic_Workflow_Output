# Warehouse Employee Management System â Comprehensive Low-Level Technical Design Document

## Overview

This document provides detailed technical specifications for all user stories across 20 epics (E01âE20) for the Warehouse Employee Management System, designed with Spring Boot 3.x, Spring Security 6.x, JPA/Hibernate, Flyway for migrations, and industry best practices. Each user story includes: section title/description, architecture overview, package/component breakdown, entity design, service/repository/controller specs, configuration/security, integration points, and code snippets/pseudo-code.

---

## E01: Project Scaffolding & Domain Setup

### Description
Initialize Spring Boot (Maven) project; configure base packages; set up core modules (employee, scheduling, attendance, safety); add Flyway for DB migrations; enable Actuator.

### Architecture Overview
- Layered architecture: Controller â Service â Repository â Domain
- Maven project structure
- Base packages: `com.wms.employee`, `com.wms.scheduling`, `com.wms.attendance`, `com.wms.safety`
- Flyway for DB migrations
- Spring Boot Actuator for health checks

### Package Structure
```
com.wms
  âââ employee
  âââ scheduling
  âââ attendance
  âââ safety
  âââ config
  âââ common
```

### Entity Design
- No entities yet; base structure for future modules

### Service Layer
- No services yet; placeholder interfaces

### Repository Layer
- No repositories yet; placeholder interfaces

### Controller Layer
- Health endpoint via Actuator

### Configuration & Security
- `application.yml` with port, DB, Flyway, Actuator
- Security: default permitAll for health

### Integration Points
- None yet

### Code Snippet
```java
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}
```

---

## E02: Employee Master Data (CRUD)

### Description
Create Employee domain with CRUD APIs and web DTOs: name, badgeId, role, department, shiftGroup, hireDate, status.

### Architecture Overview
- RESTful CRUD endpoints
- DTOs for web layer
- JPA/Hibernate for persistence

### Package Structure
```
com.wms.employee
  âââ entity
  âââ dto
  âââ repository
  âââ service
  âââ controller
```

### Entity Design
```java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    @Column(unique = true)
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status; // ACTIVE, INACTIVE, TERMINATED
    private boolean deleted = false; // Soft delete
}
```

### Service Layer
```java
public interface EmployeeService {
    EmployeeDTO create(EmployeeDTO dto);
    EmployeeDTO update(Long id, EmployeeDTO dto);
    EmployeeDTO patch(Long id, Map<String, Object> fields);
    void delete(Long id);
    EmployeeDTO get(Long id);
    Page<EmployeeDTO> list(Pageable pageable, EmployeeFilter filter);
}
```

### Repository Layer
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@RequestBody EmployeeDTO dto) { ... }
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> get(@PathVariable Long id) { ... }
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> update(@PathVariable Long id, @RequestBody EmployeeDTO dto) { ... }
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeDTO> patch(@PathVariable Long id, @RequestBody Map<String, Object> fields) { ... }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
    @GetMapping
    public ResponseEntity<Page<EmployeeDTO>> list(Pageable pageable, EmployeeFilter filter) { ... }
}
```

### Configuration & Security
- OpenAPI schemas
- Unique badgeId constraint
- Soft delete logic
- Security: role-based access (see E03)

### Integration Points
- None yet

### Code Snippet
```java
// DTO Example
public class EmployeeDTO {
    private Long id;
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}
```

---

## E03: Role-Based Access Control (RBAC)

### Description
Add Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER); method/endpoint security and row-level constraints; API key/OAuth2 toggle via config.

### Architecture Overview
- Spring Security 6.x
- Role-based endpoint/method security
- Row-level constraints in service/repository

### Package Structure
```
com.wms.config
  âââ security
com.wms.employee
  âââ service
```

### Entity Design
- Employee entity extended with role field

### Service Layer
- Row-level security checks in service methods

### Repository Layer
- Custom queries for row-level constraints

### Controller Layer
- Security annotations on endpoints

### Configuration & Security
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) {
        http
            .authorizeRequests()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .anyRequest().authenticated()
            .and()
                .oauth2ResourceServer().jwt();
    }
}
```
- API key/OAuth2 toggle via `application.yml`

### Integration Points
- OAuth2/JWT

### Code Snippet
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
public EmployeeDTO create(EmployeeDTO dto) { ... }
```

---

## E04: Time & Attendance (Clock In/Out)

### Description
Endpoints for clock-in/out events with geofence and device capture; calculate hours worked per shift; handle missed punches and corrections workflow.

### Architecture Overview
- REST endpoints for clock-in/out
- Attendance entity
- Correction workflow

### Package Structure
```
com.wms.attendance
  âââ entity
  âââ dto
  âââ repository
  âââ service
  âââ controller
```

### Entity Design
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime timestamp;
    private String eventType; // CLOCK_IN, CLOCK_OUT
    private String deviceId;
    private String location; // geofence
    private boolean correctionRequested;
}
```

### Service Layer
- Calculate hours worked per shift
- Handle corrections

### Repository Layer
- AttendanceEventRepository with custom queries

### Controller Layer
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<Void> clockIn(@RequestBody AttendanceDTO dto) { ... }
    @PostMapping("/clock-out")
    public ResponseEntity<Void> clockOut(@RequestBody AttendanceDTO dto) { ... }
    @PostMapping("/correction")
    public ResponseEntity<Void> requestCorrection(@RequestBody CorrectionDTO dto) { ... }
    @GetMapping("/report")
    public ResponseEntity<AttendanceReportDTO> getReport(...) { ... }
}
```

### Configuration & Security
- Role-based access
- Device validation

### Integration Points
- Export CSV

### Code Snippet
```java
public class AttendanceDTO {
    private Long employeeId;
    private String deviceId;
    private String location;
    private LocalDateTime timestamp;
}
```

---

## E05: Shift & Schedule Management

### Description
Create recurring shift templates, rotations, overtime rules, and assignment to employees; handle blackout dates and warehouse operation calendars.

### Architecture Overview
- Shift template and schedule entities
- Bulk assignment logic

### Package Structure
```
com.wms.scheduling
  âââ entity
  âââ dto
  âââ repository
  âââ service
  âââ controller
```

### Entity Design
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
}
@Entity
public class ShiftAssignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ShiftTemplate shiftTemplate;
    private LocalDate date;
}
```

### Service Layer
- Conflict detection
- Bulk assignment

### Repository Layer
- Custom queries for conflicts

### Controller Layer
```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates")
    public ResponseEntity<ShiftTemplateDTO> createTemplate(...) { ... }
    @GetMapping("/assignments")
    public ResponseEntity<List<ShiftAssignmentDTO>> getAssignments(...) { ... }
    @PostMapping("/assign")
    public ResponseEntity<Void> bulkAssign(...) { ... }
}
```

### Configuration & Security
- Audit entries for assignments

### Integration Points
- Calendar export

### Code Snippet
```java
public class ShiftAssignmentDTO {
    private Long employeeId;
    private Long shiftTemplateId;
    private LocalDate date;
}
```

---

## E06: Leave & Absence Management

### Description
Request/approve PTO, sick, unpaid leave; accrual balances and policies; integration hooks to exclude from scheduling and payroll hours.

### Architecture Overview
- Leave request and balance entities
- Approval workflow

### Package Structure
```
com.wms.leave
  âââ entity
  âââ dto
  âââ repository
  âââ service
  âââ controller
```

### Entity Design
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate startDate;
    private LocalDate endDate;
    private String type; // PTO, SICK, UNPAID
    private String status; // REQUESTED, APPROVED, DENIED
}
@Entity
public class LeaveBalance {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String leaveType;
    private int balance;
}
```

### Service Layer
- Accrual calculation
- Approval workflow

### Repository Layer
- LeaveRequestRepository

### Controller Layer
```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/request")
    public ResponseEntity<Void> requestLeave(...) { ... }
    @PostMapping("/approve")
    public ResponseEntity<Void> approveLeave(...) { ... }
    @GetMapping("/balance")
    public ResponseEntity<LeaveBalanceDTO> getBalance(...) { ... }
}
```

### Configuration & Security
- Role-based access

### Integration Points
- Payroll exclusion

### Code Snippet
```java
public class LeaveRequestDTO {
    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String type;
}
```

---

## E07: Training & Certification Tracking

### Description
Track required certifications (e.g., forklift), expirations, renewals; block assignment to tasks requiring expired certs; upload proof documents.

### Architecture Overview
- Certification entity
- Expiry alerts

### Package Structure
```
com.wms.training
  âââ entity
  âââ dto
  âââ repository
  âââ service
  âââ controller
```

### Entity Design
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
}
```

### Service Layer
- Expiry alert logic
- Assignment checks

### Repository Layer
- CertificationRepository

### Controller Layer
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<CertificationDTO> create(...) { ... }
    @GetMapping("/alerts")
    public ResponseEntity<List<CertificationAlertDTO>> getAlerts(...) { ... }
}
```

### Configuration & Security
- Role-based access

### Integration Points
- Scheduling checks

### Code Snippet
```java
public class CertificationDTO {
    private Long employeeId;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
}
```

---

## E08: Safety Incidents & OSHA Reporting

### Description
Record incidents/near-misses; severity, location, description, involved employees; workflow for investigation and corrective actions; generate OSHA summary.

### Architecture Overview
- Incident entity
- Workflow status

### Package Structure
```
com.wms.safety
  âââ entity
  âââ dto
  âââ repository
  âââ service
  âââ controller
```

### Entity Design
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
    private String status; // OPEN, INVESTIGATING, RESOLVED
}
```

### Service Layer
- Workflow transitions
- OSHA export

### Repository Layer
- SafetyIncidentRepository

### Controller Layer
```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
    @PostMapping
    public ResponseEntity<SafetyIncidentDTO> create(...) { ... }
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(...) { ... }
    @GetMapping("/oshasummary")
    public ResponseEntity<OSHASummaryDTO> getSummary(...) { ... }
}
```

### Configuration & Security
- Role-based access

### Integration Points
- OSHA export

### Code Snippet
```java
public class SafetyIncidentDTO {
    private String severity;
    private String location;
    private String description;
    private List<Long> involvedEmployeeIds;
    private String status;
}
```

---

## E09: Equipment & Asset Assignment

### Description
Assign scanners, forklifts, and PPE to employees; track checkout/return; prevent use if certification missing; maintain asset condition state.

### Architecture Overview
- Asset and assignment entities
- Certification checks

### Package Structure
```
com.wms.asset
  âââ entity
  âââ dto
  âââ repository
  âââ service
  âââ controller
```

### Entity Design
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private String condition;
}
@Entity
public class AssetAssignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Asset asset;
    @ManyToOne
    private Employee employee;
    private LocalDateTime checkoutTime;
    private LocalDateTime returnTime;
}
```

### Service Layer
- Certification validation
- Overdue checks

### Repository Layer
- AssetRepository, AssetAssignmentRepository

### Controller Layer
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign")
    public ResponseEntity<Void> assignAsset(...) { ... }
    @PostMapping("/return")
    public ResponseEntity<Void> returnAsset(...) { ... }
    @GetMapping("/history")
    public ResponseEntity<List<AssetAssignmentDTO>> getHistory(...) { ... }
}
```

### Configuration & Security
- Role-based access

### Integration Points
- Certification checks

### Code Snippet
```java
public class AssetAssignmentDTO {
    private Long assetId;
    private Long employeeId;
    private LocalDateTime checkoutTime;
    private LocalDateTime returnTime;
}
```

---

## E10: Performance Reviews & Goals

### Description
Create quarterly/annual review templates; track goals, competencies, ratings, comments; supervisor/employee acknowledgements.

### Architecture Overview
- Review and goal entities
- Workflow for submission/acknowledgement

### Package Structure
```
com.wms.performance
  âââ entity
  âââ dto
  âââ repository
  âââ service
  âââ controller
```

### Entity Design
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String cycle; // Q1, Q2, Annual
    private String status; // DRAFT, SUBMITTED, ACKNOWLEDGED
    private String comments;
    private int rating;
}
@Entity
public class Goal {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private PerformanceReview review;
    private String description;
    private boolean achieved;
}
```

### Service Layer
- Submission/acknowledgement logic

### Repository Layer
- PerformanceReviewRepository, GoalRepository

### Controller Layer
```java
@RestController
@RequestMapping("/reviews")
public class PerformanceReviewController {
    @PostMapping
    public ResponseEntity<PerformanceReviewDTO> create(...) { ... }
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<Void> acknowledge(...) { ... }
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdf(...) { ... }
}
```

### Configuration & Security
- Role-based visibility

### Integration Points
- PDF export

### Code Snippet
```java
public class PerformanceReviewDTO {
    private Long employeeId;
    private String cycle;
    private String status;
    private String comments;
    private int rating;
}
```

---

## E11: Payroll Export Integration

### Description
Generate payroll-ready files from approved attendance and leave; mapping to external payroll provider formats; secure delivery (SFTP/API).

### Architecture Overview
- Export logic
- Integration with SFTP/API

### Package Structure
```
com.wms.payroll
  âââ service
  âââ controller
```

### Entity Design
- Use Attendance and Leave entities

### Service Layer
- Export generation
- Delivery with retry/backoff

### Repository Layer
- Attendance/Leave repositories

### Controller Layer
```java
@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/export")
    public ResponseEntity<Void> exportPayroll(...) { ... }
}
```

### Configuration & Security
- Secure delivery
- Audit log

### Integration Points
- SFTP/API

### Code Snippet
```java
public void exportPayroll() {
    // Gather approved attendance/leave
    // Map to provider schema
    // Deliver via SFTP/API
    // Log audit entry
}
```

---

## E12: Notifications & Announcements

### Description
In-app and email/SMS notifications for shift changes, expiring certs, approvals, announcements; quiet hours configuration.

### Architecture Overview
- Notification entity
- Delivery logic

### Package Structure
```
com.wms.notification
  âââ entity
  âââ dto
  âââ repository
  âââ service
  âââ controller
```

### Entity Design
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String channel; // EMAIL, SMS, IN_APP
    private String template;
    private String status; // SENT, FAILED
    private LocalDateTime sentAt;
}
```

### Service Layer
- Delivery logic
- Quiet hours enforcement

### Repository Layer
- NotificationRepository

### Controller Layer
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(...) { ... }
    @GetMapping("/announcements")
    public ResponseEntity<List<AnnouncementDTO>> getAnnouncements(...) { ... }
}
```

### Configuration & Security
- Rate limits

### Integration Points
- Email/SMS providers

### Code Snippet
```java
public class NotificationDTO {
    private Long employeeId;
    private String channel;
    private String template;
}
```

---

## E13: Integration Layer (HRIS/WMS APIs)

### Description
Expose REST APIs and connectors for HRIS (new hires/terms), WMS (location/department), and IDP for SSO; webhooks for events.

### Architecture Overview
- REST APIs
- Connectors for HRIS/WMS/IDP

### Package Structure
```
com.wms.integration
  âââ hris
  âââ wms
  âââ idp
  âââ controller
```

### Entity Design
- Use Employee, Department, Location entities

### Service Layer
- Sync jobs
- Webhook handlers

### Repository Layer
- Employee/Department/Location repositories

### Controller Layer
```java
@RestController
@RequestMapping("/integration")
public class IntegrationController {
    @PostMapping("/hris/sync")
    public ResponseEntity<Void> syncHRIS(...) { ... }
    @PostMapping("/wms/link")
    public ResponseEntity<Void> linkWMS(...) { ... }
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(...) { ... }
}
```

### Configuration & Security
- JWT/OAuth2-secured APIs

### Integration Points
- HRIS/WMS/IDP

### Code Snippet
```java
public void syncHRIS() {
    // Fetch new hires/terms from HRIS
    // Create/update Employee records
}
```

---

## E14: Audit Trail & Compliance

### Description
Centralized audit logging for sensitive changes (employee PII, schedules, approvals, payroll); tamper-evident storage.

### Architecture Overview
- Audit entity
- Logging logic

### Package Structure
```
com.wms.audit
  âââ entity
  âââ repository
  âââ service
  âââ controller
```

### Entity Design
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
    private String beforeState;
    private String afterState;
}
```

### Service Layer
- Log creation/update/delete

### Repository Layer
- AuditLogRepository

### Controller Layer
```java
@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/logs")
    public ResponseEntity<List<AuditLogDTO>> getLogs(...) { ... }
}
```

### Configuration & Security
- Immutable log table

### Integration Points
- Export by date/user/entity

### Code Snippet
```java
public void logChange(String entity, Long entityId, String action, String actor, String before, String after) { ... }
```

---

## E15: Reporting & Analytics

### Description
Operational reports: attendance, overtime, leave balances, certification status, safety KPIs; export CSV/PDF; basic role-based dashboards.

### Architecture Overview
- Reporting endpoints
- Export logic

### Package Structure
```
com.wms.reporting
  âââ service
  âââ controller
```

### Entity Design
- Use Attendance, Leave, Certification, SafetyIncident entities

### Service Layer
- Report generation
- Export logic

### Repository Layer
- Custom queries

### Controller Layer
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<AttendanceReportDTO> getAttendanceReport(...) { ... }
    @GetMapping("/overtime")
    public ResponseEntity<OvertimeReportDTO> getOvertimeReport(...) { ... }
    @GetMapping("/leave")
    public ResponseEntity<LeaveReportDTO> getLeaveReport(...) { ... }
    @GetMapping("/certifications")
    public ResponseEntity<CertificationReportDTO> getCertificationReport(...) { ... }
    @GetMapping("/safety")
    public ResponseEntity<SafetyReportDTO> getSafetyReport(...) { ... }
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReport(...) { ... }
}
```

### Configuration & Security
- Role-based access

### Integration Points
- BI metrics endpoints

### Code Snippet
```java
public byte[] exportReport(String type, DateRange range) { ... }
```

---

## E16: Mobile Access (PWA)

### Description
Responsive views for workers to clock-in/out, view schedules, request leave, see announcements; offline-friendly via PWA.

### Architecture Overview
- PWA manifest
- Offline queue logic

### Package Structure
```
com.wms.mobile
  âââ controller
  âââ service
```

### Entity Design
- Use Attendance, Shift, Leave, Notification entities

### Service Layer
- Offline queue handling

### Repository Layer
- Standard repositories

### Controller Layer
```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @GetMapping("/clock")
    public ResponseEntity<Void> clockInOut(...) { ... }
    @GetMapping("/schedule")
    public ResponseEntity<List<ShiftAssignmentDTO>> getSchedule(...) { ... }
    @GetMapping("/leave")
    public ResponseEntity<List<LeaveRequestDTO>> getLeaveRequests(...) { ... }
    @GetMapping("/announcements")
    public ResponseEntity<List<AnnouncementDTO>> getAnnouncements(...) { ... }
}
```

### Configuration & Security
- PWA manifest
- Lighthouse score â¥ 80

### Integration Points
- None

### Code Snippet
```java
// PWA manifest.json example
{
  "name": "Warehouse Employee Management",
  "short_name": "WMS",
  "start_url": "/mobile",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

---

## E17: Onboarding & Offboarding Workflow

### Description
Automate provisioning of accounts, initial schedule, required training; deprovision access and assets on termination.

### Architecture Overview
- Workflow logic
- Task generation

### Package Structure
```
com.wms.onboarding
  âââ service
  âââ controller
```

### Entity Design
- Use Employee, Asset, Certification entities

### Service Layer
- Task generation for onboarding/offboarding

### Repository Layer
- Standard repositories

### Controller Layer
```java
@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @PostMapping("/provision")
    public ResponseEntity<Void> provisionEmployee(...) { ... }
    @PostMapping("/deprovision")
    public ResponseEntity<Void> deprovisionEmployee(...) { ... }
}
```

### Configuration & Security
- Role-based access

### Integration Points
- HRIS sync

### Code Snippet
```java
public void provisionEmployee(Long employeeId) {
    // Create account, assign schedule, generate training tasks
}
```

---

## E18: Multi-Tenant Support

### Description
Enable multi-tenant architecture for warehouse groups; tenant isolation for data and configuration.

### Architecture Overview
- Tenant context
- Data isolation

### Package Structure
```
com.wms.tenant
  âââ entity
  âââ service
  âââ repository
  âââ controller
```

### Entity Design
```java
@Entity
public class Tenant {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String config;
}
```

### Service Layer
- Tenant context resolution

### Repository Layer
- Tenant-aware repositories

### Controller Layer
```java
@RestController
@RequestMapping("/tenant")
public class TenantController {
    @GetMapping("/current")
    public ResponseEntity<TenantDTO> getCurrentTenant(...) { ... }
}
```

### Configuration & Security
- Tenant isolation

### Integration Points
- None

### Code Snippet
```java
public class TenantContext {
    public static Tenant getCurrentTenant() { ... }
}
```

---

## E19: Advanced Scheduling

### Description
Support advanced scheduling features: shift swaps, bidding, auto-scheduling, and conflict resolution.

### Architecture Overview
- Scheduling algorithms
- Swap/bid entities

### Package Structure
```
com.wms.scheduling
  âââ swap
  âââ bid
  âââ service
  âââ controller
```

### Entity Design
```java
@Entity
public class ShiftSwap {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee requester;
    @ManyToOne
    private Employee responder;
    @ManyToOne
    private ShiftAssignment originalAssignment;
    private String status; // REQUESTED, APPROVED, DENIED
}
@Entity
public class ShiftBid {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ShiftTemplate shiftTemplate;
    private LocalDate date;
    private String status; // OPEN, WON, LOST
}
```

### Service Layer
- Swap/bid logic
- Auto-scheduling

### Repository Layer
- Swap/Bid repositories

### Controller Layer
```java
@RestController
@RequestMapping("/scheduling")
public class AdvancedSchedulingController {
    @PostMapping("/swap")
    public ResponseEntity<Void> requestSwap(...) { ... }
    @PostMapping("/bid")
    public ResponseEntity<Void> placeBid(...) { ... }
    @PostMapping("/auto")
    public ResponseEntity<Void> autoSchedule(...) { ... }
}
```

### Configuration & Security
- Conflict resolution

### Integration Points
- None

### Code Snippet
```java
public void autoSchedule() {
    // Algorithm for optimal shift assignment
}
```

---

## E20: CI/CD Pipeline

### Description
Automate build, test, and deployment using CI/CD tools; enforce code quality and security checks.

### Architecture Overview
- CI/CD pipeline (e.g., GitHub Actions, Jenkins)
- Quality gates

### Package Structure
- `.github/workflows/` or `jenkins/`

### Entity Design
- N/A

### Service Layer
- N/A

### Repository Layer
- N/A

### Controller Layer
- N/A

### Configuration & Security
- Pipeline config files
- Security scans

### Integration Points
- Build/test/deploy tools

### Code Snippet
```yaml
# GitHub Actions example
name: CI/CD Pipeline
on: [push, pull_request]
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
      - name: Run Tests
        run: mvn test
      - name: Deploy
        run: ./deploy.sh
```

---

## Notes

- All entities use JPA/Hibernate annotations.
- Flyway migrations for schema changes.
- Spring Security 6.x for authentication/authorization.
- OpenAPI/Swagger for API documentation.
- All endpoints follow RESTful conventions.
- DTOs for web layer, entities for persistence.
- Service layer encapsulates business logic.
- Repository layer uses Spring Data JPA.
- Controllers expose REST endpoints.
- Configuration via `application.yml`.
- Integration points use connectors/services.
- Code snippets illustrate key patterns.

---

## Appendix

- [Entity Relationship Diagrams (ERDs)](link-to-ERDs)
- [OpenAPI Schemas](link-to-openapi)
- [Flyway Migration Scripts](link-to-flyway)
- [CI/CD Pipeline Configs](link-to-pipeline-configs)

---

This document is intended for Spring Boot developers to ensure uniformity, high quality, and easy consumption for implementation of the warehouse employee management system.