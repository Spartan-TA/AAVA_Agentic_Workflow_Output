# Technical Design Document: Warehouse Employee Management System

**Location:** PRD/Technical_Design_Document.md

---

## Table of Contents

1. [E01 - Project Scaffolding & Domain Setup](#e01---project-scaffolding--domain-setup)
2. [E02 - Employee Master Data (CRUD)](#e02---employee-master-data-crud)
3. [E03 - Role-Based Access Control (RBAC)](#e03---role-based-access-control-rbac)
4. [E04 - Time & Attendance (Clock In/Out)](#e04---time--attendance-clock-inout)
5. [E05 - Shift & Schedule Management](#e05---shift--schedule-management)
6. [E06 - Leave & Absence Management](#e06---leave--absence-management)
7. [E07 - Training & Certification Tracking](#e07---training--certification-tracking)
8. [E08 - Safety Incidents & OSHA Reporting](#e08---safety-incidents--osha-reporting)
9. [E09 - Equipment & Asset Assignment](#e09---equipment--asset-assignment)
10. [E10 - Performance Reviews & Goals](#e10---performance-reviews--goals)
11. [E11 - Payroll Export Integration](#e11---payroll-export-integration)
12. [E12 - Notifications & Announcements](#e12---notifications--announcements)
13. [E13 - Integration Layer (HRIS/WMS APIs)](#e13---integration-layer-hriswms-apis)
14. [E14 - Audit Trail & Compliance](#e14---audit-trail--compliance)
15. [E15 - Reporting & Analytics](#e15---reporting--analytics)
16. [E16 - Mobile Access (PWA)](#e16---mobile-access-pwa)
17. [E17 - Onboarding & Offboarding Workflow](#e17---onboarding--offboarding-workflow)
18. [E18 - Localization & Multi-Tenant](#e18---localization--multi-tenant)
19. [E19 - Observability & Monitoring](#e19---observability--monitoring)
20. [E20 - Deployment & CI/CD](#e20---deployment--cicd)

---

## E01 - Project Scaffolding & Domain Setup

Section: Overview of Spring Boot Architecture
Description: Establishes the foundational structure for the application, leveraging Spring Boot's modularity, dependency injection, and auto-configuration. Sets up Maven for dependency management, Flyway/Liquibase for DB migrations, and Actuator for health monitoring.
Design Specification:
- Use Spring Boot 3.x (Java 17+)
- Maven multi-module project: `core`, `employee`, `attendance`, `safety`, `integration`
- Base package: `com.company.wems`
- Enable Actuator endpoints for health, metrics
- Integrate Flyway/Liquibase for DB migrations
Sample Implementation:
```java
@SpringBootApplication
public class WemsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WemsApplication.class, args);
    }
}
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Organizes code for maintainability and scalability.
Design Specification:
- `com.company.wems.core` (common config, exceptions)
- `com.company.wems.employee` (employee domain)
- `com.company.wems.attendance` (attendance domain)
- `com.company.wems.safety` (safety/OSHA domain)
- `com.company.wems.integration` (external APIs)
Sample Implementation:
```
src/
  main/
    java/
      com/
        company/
          wems/
            core/
            employee/
            attendance/
            safety/
            integration/
```

Section: Configuration and Security Settings
Description: Centralizes configuration and enables health checks.
Design Specification:
- `application.yml` for environment configs
- Expose `/actuator/health`, `/actuator/info`
Sample Implementation:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

---

## E02 - Employee Master Data (CRUD)

Section: Overview of Spring Boot Architecture
Description: Implements RESTful CRUD for employee records using Spring Web, JPA, and validation.
Design Specification:
- REST controllers for CRUD
- Service layer for business logic
- JPA entities for persistence
Sample Implementation:
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController { ... }
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Employee module encapsulates all employee-related logic.
Design Specification:
- `com.company.wems.employee.controller`
- `com.company.wems.employee.service`
- `com.company.wems.employee.repository`
- `com.company.wems.employee.model`
Sample Implementation:
```
employee/
  controller/
  service/
  repository/
  model/
```

Section: Entity Design
Description: Employee entity with relationships and constraints.
Design Specification:
- Fields: id, name, badgeId (unique), role, department, shiftGroup, hireDate, status, deleted
- Soft delete via `deleted` flag
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
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted = false;
    // getters/setters
}
```

Section: Service Layer Specifications
Description: Encapsulates business logic for employee management.
Design Specification:
- Methods: create, get, update, delete (soft), list (with pagination/filter)
Sample Implementation:
```java
public interface EmployeeService {
    EmployeeDTO create(EmployeeDTO dto);
    EmployeeDTO get(Long id);
    Page<EmployeeDTO> list(Pageable pageable, EmployeeFilter filter);
    EmployeeDTO update(Long id, EmployeeDTO dto);
    void delete(Long id);
}
```

Section: Repository Layer
Description: Uses Spring Data JPA for persistence.
Design Specification:
- `EmployeeRepository extends JpaRepository<Employee, Long>`
- Custom query for soft delete
Sample Implementation:
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

Section: Controller Specifications
Description: REST endpoints for CRUD operations.
Design Specification:
- `POST /employees`
- `GET /employees/{id}`
- `PUT /employees/{id}`
- `PATCH /employees/{id}`
- `DELETE /employees/{id}`
- Pagination and filtering via query params
Sample Implementation:
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping public ResponseEntity<EmployeeDTO> create(@RequestBody @Valid EmployeeDTO dto) { ... }
    @GetMapping("/{id}") public ResponseEntity<EmployeeDTO> get(@PathVariable Long id) { ... }
    @PutMapping("/{id}") public ResponseEntity<EmployeeDTO> update(@PathVariable Long id, @RequestBody @Valid EmployeeDTO dto) { ... }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
}
```

Section: Configuration and Security Settings
Description: Secures endpoints and validates input.
Design Specification:
- Bean validation annotations
- Security via RBAC (see E03)
Sample Implementation:
```java
public class EmployeeDTO {
    @NotBlank private String name;
    @NotBlank private String badgeId;
    // ...
}
```

Section: Integration Points
Description: Exposes OpenAPI schemas, integrates with HRIS (see E13).
Design Specification:
- OpenAPI annotations
Sample Implementation:
```java
@Operation(summary = "Create employee", ...)
```

---

## E03 - Role-Based Access Control (RBAC)

Section: Overview of Spring Boot Architecture
Description: Uses Spring Security for authentication and authorization, with roles and method/endpoint security.
Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Method-level security via `@PreAuthorize`
- API key/OAuth2 toggle via config
Sample Implementation:
```java
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter { ... }
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Security module encapsulates all security logic.
Design Specification:
- `com.company.wems.core.security`
Sample Implementation:
```
core/
  security/
    SecurityConfig.java
    JwtAuthFilter.java
    ApiKeyAuthFilter.java
```

Section: Configuration and Security Settings
Description: Configures authentication providers and security filters.
Design Specification:
- JWT/OAuth2 and API key support
- Role hierarchy
Sample Implementation:
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
      .csrf().disable()
      .authorizeRequests()
        .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
        .antMatchers("/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated()
      .and()
      .oauth2ResourceServer().jwt();
}
```

Section: Service Layer Specifications
Description: Enforces row-level security where applicable.
Design Specification:
- Service methods annotated with `@PreAuthorize`
Sample Implementation:
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(#id, authentication))")
public EmployeeDTO get(Long id) { ... }
```

Section: Integration Points
Description: Integrates with external IDP for OAuth2/JWT.
Design Specification:
- OAuth2 client config in `application.yml`
Sample Implementation:
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: https://idp.example.com/.well-known/jwks.json
```

---

## E04 - Time & Attendance (Clock In/Out)

Section: Overview of Spring Boot Architecture
Description: Provides endpoints for clock-in/out, calculates hours, and manages corrections.
Design Specification:
- Attendance module with REST endpoints
- Service for time calculations
Sample Implementation:
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController { ... }
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Attendance module encapsulates all attendance logic.
Design Specification:
- `com.company.wems.attendance.controller`
- `com.company.wems.attendance.service`
- `com.company.wems.attendance.repository`
- `com.company.wems.attendance.model`
Sample Implementation:
```
attendance/
  controller/
  service/
  repository/
  model/
```

Section: Entity Design
Description: Attendance entity with clock-in/out events.
Design Specification:
- Fields: id, employee, clockIn, clockOut, deviceId, geofence, status, correctionRequested
Sample Implementation:
```java
@Entity
public class Attendance {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private String deviceId;
    private String geofence;
    private String status; // e.g., NORMAL, MISSED_PUNCH, CORRECTION_REQUESTED
    // getters/setters
}
```

Section: Service Layer Specifications
Description: Handles clock-in/out logic, validations, and corrections.
Design Specification:
- Methods: clockIn, clockOut, requestCorrection, calculateTotals
Sample Implementation:
```java
public interface AttendanceService {
    AttendanceDTO clockIn(ClockEventDTO dto);
    AttendanceDTO clockOut(ClockEventDTO dto);
    AttendanceDTO requestCorrection(Long attendanceId, CorrectionDTO dto);
    DailyTotalsDTO getDailyTotals(Long employeeId, LocalDate date);
}
```

Section: Repository Layer
Description: Attendance persistence and queries.
Design Specification:
- `AttendanceRepository extends JpaRepository<Attendance, Long>`
Sample Implementation:
```java
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEmployeeAndClockInBetween(Employee employee, LocalDateTime start, LocalDateTime end);
}
```

Section: Controller Specifications
Description: REST endpoints for clock-in/out and corrections.
Design Specification:
- `POST /attendance/clock-in`
- `POST /attendance/clock-out`
- `POST /attendance/{id}/correction`
Sample Implementation:
```java
@PostMapping("/clock-in")
public ResponseEntity<AttendanceDTO> clockIn(@RequestBody @Valid ClockEventDTO dto) { ... }
```

Section: Integration Points
Description: Exports attendance data for payroll (see E11).
Design Specification:
- CSV export endpoint
Sample Implementation:
```java
@GetMapping("/export")
public void exportAttendance(@RequestParam ... ) { ... }
```

---

## E05 - Shift & Schedule Management

Section: Overview of Spring Boot Architecture
Description: Manages shift templates, rotations, and assignments.
Design Specification:
- Shift module with REST endpoints
- Service for conflict detection
Sample Implementation:
```java
@RestController
@RequestMapping("/shifts")
public class ShiftController { ... }
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Shift module encapsulates all scheduling logic.
Design Specification:
- `com.company.wems.shift.controller`
- `com.company.wems.shift.service`
- `com.company.wems.shift.repository`
- `com.company.wems.shift.model`
Sample Implementation:
```
shift/
  controller/
  service/
  repository/
  model/
```

Section: Entity Design
Description: ShiftTemplate, ShiftAssignment entities.
Design Specification:
- ShiftTemplate: id, name, startTime, endTime, recurrence, blackoutDates
- ShiftAssignment: id, employee, shiftTemplate, date, status
Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrence; // e.g., DAILY, WEEKLY
    @ElementCollection
    private Set<LocalDate> blackoutDates;
    // ...
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
    private String status; // e.g., ASSIGNED, COMPLETED
    // ...
}
```

Section: Service Layer Specifications
Description: Handles shift creation, assignment, and conflict detection.
Design Specification:
- Methods: createTemplate, assignShift, detectConflicts, bulkAssign
Sample Implementation:
```java
public interface ShiftService {
    ShiftTemplateDTO createTemplate(ShiftTemplateDTO dto);
    ShiftAssignmentDTO assignShift(Long employeeId, Long templateId, LocalDate date);
    List<ConflictDTO> detectConflicts(Long employeeId, LocalDate dateRange);
    void bulkAssign(List<ShiftAssignmentDTO> assignments);
}
```

Section: Repository Layer
Description: Shift persistence and queries.
Design Specification:
- `ShiftTemplateRepository`, `ShiftAssignmentRepository`
Sample Implementation:
```java
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    List<ShiftAssignment> findByEmployeeAndDateBetween(Employee employee, LocalDate start, LocalDate end);
}
```

Section: Controller Specifications
Description: REST endpoints for shift management.
Design Specification:
- `POST /shifts/templates`
- `POST /shifts/assign`
- `GET /shifts/conflicts`
Sample Implementation:
```java
@PostMapping("/assign")
public ResponseEntity<Void> assignShift(@RequestBody ShiftAssignmentDTO dto) { ... }
```

Section: Integration Points
Description: Generates audit entries (see E14).
Design Specification:
- Audit service integration
Sample Implementation:
```java
auditService.logShiftAssignment(...);
```

---

## E06 - Leave & Absence Management

Section: Overview of Spring Boot Architecture
Description: Manages leave requests, approvals, and accruals.
Design Specification:
- Leave module with REST endpoints
- Service for accrual calculations
Sample Implementation:
```java
@RestController
@RequestMapping("/leave")
public class LeaveController { ... }
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Leave module encapsulates all leave logic.
Design Specification:
- `com.company.wems.leave.controller`
- `com.company.wems.leave.service`
- `com.company.wems.leave.repository`
- `com.company.wems.leave.model`
Sample Implementation:
```
leave/
  controller/
  service/
  repository/
  model/
```

Section: Entity Design
Description: LeaveRequest, LeaveBalance entities.
Design Specification:
- LeaveRequest: id, employee, type, startDate, endDate, status, approver, comments
- LeaveBalance: id, employee, type, balance
Sample Implementation:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type; // PTO, SICK, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // REQUESTED, APPROVED, DENIED
    @ManyToOne
    private Employee approver;
    private String comments;
    // ...
}
@Entity
public class LeaveBalance {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type;
    private BigDecimal balance;
    // ...
}
```

Section: Service Layer Specifications
Description: Handles leave requests, approvals, and balance updates.
Design Specification:
- Methods: requestLeave, approveLeave, updateBalance, autoFlagShifts
Sample Implementation:
```java
public interface LeaveService {
    LeaveRequestDTO requestLeave(LeaveRequestDTO dto);
    LeaveRequestDTO approveLeave(Long requestId, boolean approve, String comments);
    void updateBalance(Long employeeId, String type, BigDecimal delta);
}
```

Section: Repository Layer
Description: Leave persistence and queries.
Design Specification:
- `LeaveRequestRepository`, `LeaveBalanceRepository`
Sample Implementation:
```java
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeAndStatus(Employee employee, String status);
}
```

Section: Controller Specifications
Description: REST endpoints for leave management.
Design Specification:
- `POST /leave/request`
- `POST /leave/{id}/approve`
Sample Implementation:
```java
@PostMapping("/request")
public ResponseEntity<LeaveRequestDTO> requestLeave(@RequestBody @Valid LeaveRequestDTO dto) { ... }
```

Section: Integration Points
Description: Excludes approved leave from scheduling/payroll (see E05, E11).
Design Specification:
- Integration hooks in service layer
Sample Implementation:
```java
shiftService.autoFlagForCoverage(...);
payrollService.excludeLeave(...);
```

---

## E07 - Training & Certification Tracking

Section: Overview of Spring Boot Architecture
Description: Tracks employee certifications, expirations, and renewals.
Design Specification:
- Certification module with REST endpoints
- Service for expiry alerts
Sample Implementation:
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController { ... }
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Certification module encapsulates all certification logic.
Design Specification:
- `com.company.wems.certification.controller`
- `com.company.wems.certification.service`
- `com.company.wems.certification.repository`
- `com.company.wems.certification.model`
Sample Implementation:
```
certification/
  controller/
  service/
  repository/
  model/
```

Section: Entity Design
Description: Certification, EmployeeCertification entities.
Design Specification:
- Certification: id, name, description, requiredForRoles
- EmployeeCertification: id, employee, certification, issueDate, expiryDate, proofDocumentUrl, status
Sample Implementation:
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String description;
    @ElementCollection
    private Set<String> requiredForRoles;
    // ...
}
@Entity
public class EmployeeCertification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private Certification certification;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String proofDocumentUrl;
    private String status; // ACTIVE, EXPIRED
    // ...
}
```

Section: Service Layer Specifications
Description: Handles certification CRUD, expiry alerts, and scheduling checks.
Design Specification:
- Methods: addCertification, renewCertification, alertExpiring, checkAssignmentEligibility
Sample Implementation:
```java
public interface CertificationService {
    EmployeeCertificationDTO addCertification(EmployeeCertificationDTO dto);
    void alertExpiringCertifications();
    boolean isEligibleForAssignment(Long employeeId, Long certificationId);
}
```

Section: Repository Layer
Description: Certification persistence and queries.
Design Specification:
- `CertificationRepository`, `EmployeeCertificationRepository`
Sample Implementation:
```java
public interface EmployeeCertificationRepository extends JpaRepository<EmployeeCertification, Long> {
    List<EmployeeCertification> findByExpiryDateBetween(LocalDate start, LocalDate end);
}
```

Section: Controller Specifications
Description: REST endpoints for certification management.
Design Specification:
- `POST /certifications`
- `GET /certifications/expiring`
Sample Implementation:
```java
@GetMapping("/expiring")
public List<EmployeeCertificationDTO> getExpiringCertifications(@RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
```

Section: Integration Points
Description: Blocks assignment to tasks if certification expired (see E05, E09).
Design Specification:
- Eligibility checks in shift/asset assignment services
Sample Implementation:
```java
if (!certificationService.isEligibleForAssignment(employeeId, certId)) { throw new ForbiddenException(); }
```

---

## E08 - Safety Incidents & OSHA Reporting

Section: Overview of Spring Boot Architecture
Description: Records safety incidents, manages investigation workflow, and generates OSHA reports.
Design Specification:
- Safety module with REST endpoints
- Service for workflow and reporting
Sample Implementation:
```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController { ... }
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Safety module encapsulates all safety logic.
Design Specification:
- `com.company.wems.safety.controller`
- `com.company.wems.safety.service`
- `com.company.wems.safety.repository`
- `com.company.wems.safety.model`
Sample Implementation:
```
safety/
  controller/
  service/
  repository/
  model/
```

Section: Entity Design
Description: SafetyIncident entity with workflow status.
Design Specification:
- Fields: id, date, severity, location, description, involvedEmployees, status, correctiveActions
Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private LocalDate date;
    private String severity;
    private String location;
    private String description;
    @ManyToMany
    private Set<Employee> involvedEmployees;
    private String status; // OPEN, INVESTIGATING, RESOLVED
    private String correctiveActions;
    // ...
}
```

Section: Service Layer Specifications
Description: Handles incident creation, workflow, and OSHA reporting.
Design Specification:
- Methods: createIncident, updateStatus, exportOSHAReport
Sample Implementation:
```java
public interface SafetyIncidentService {
    SafetyIncidentDTO createIncident(SafetyIncidentDTO dto);
    SafetyIncidentDTO updateStatus(Long id, String status);
    OSHAReportDTO exportOSHAReport(LocalDate from, LocalDate to);
}
```

Section: Repository Layer
Description: Safety incident persistence and queries.
Design Specification:
- `SafetyIncidentRepository`
Sample Implementation:
```java
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
    List<SafetyIncident> findByDateBetween(LocalDate start, LocalDate end);
}
```

Section: Controller Specifications
Description: REST endpoints for incident management and reporting.
Design Specification:
- `POST /safety/incidents`
- `PATCH /safety/incidents/{id}/status`
- `GET /safety/incidents/osha-report`
Sample Implementation:
```java
@GetMapping("/osha-report")
public OSHAReportDTO exportOSHAReport(@RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
```

Section: Integration Points
Description: Metrics dashboard endpoints for BI (see E15).
Design Specification:
- Expose metrics via `/metrics/safety`
Sample Implementation:
```java
@GetMapping("/metrics")
public SafetyMetricsDTO getSafetyMetrics() { ... }
```

---

## E09 - Equipment & Asset Assignment

Section: Overview of Spring Boot Architecture
Description: Manages assignment and tracking of equipment/assets to employees.
Design Specification:
- Asset module with REST endpoints
- Service for check-in/out and condition tracking
Sample Implementation:
```java
@RestController
@RequestMapping("/assets")
public class AssetController { ... }
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Asset module encapsulates all asset logic.
Design Specification:
- `com.company.wems.asset.controller`
- `com.company.wems.asset.service`
- `com.company.wems.asset.repository`
- `com.company.wems.asset.model`
Sample Implementation:
```
asset/
  controller/
  service/
  repository/
  model/
```

Section: Entity Design
Description: Asset, AssetAssignment entities.
Design Specification:
- Asset: id, type, serialNumber, condition, assignedTo, certificationRequired
- AssetAssignment: id, asset, employee, checkoutDate, returnDate, status
Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private String condition;
    @ManyToOne
    private Employee assignedTo;
    private Long certificationRequired;
    // ...
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
    private String status; // CHECKED_OUT, RETURNED, OVERDUE
    // ...
}
```

Section: Service Layer Specifications
Description: Handles asset check-in/out, certification checks, and overdue tracking.
Design Specification:
- Methods: assignAsset, returnAsset, checkCertification, getOverdueAssets
Sample Implementation:
```java
public interface AssetService {
    AssetAssignmentDTO assignAsset(Long assetId, Long employeeId);
    AssetAssignmentDTO returnAsset(Long assignmentId);
    List<AssetAssignmentDTO> getOverdueAssets();
}
```

Section: Repository Layer
Description: Asset persistence and queries.
Design Specification:
- `AssetRepository`, `AssetAssignmentRepository`
Sample Implementation:
```java
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Long> {
    List<AssetAssignment> findByStatus(String status);
}
```

Section: Controller Specifications
Description: REST endpoints for asset management.
Design Specification:
- `POST /assets/assign`
- `POST /assets/return`
- `GET /assets/overdue`
Sample Implementation:
```java
@GetMapping("/overdue")
public List<AssetAssignmentDTO> getOverdueAssets() { ... }
```

Section: Integration Points
Description: Certification check before assignment (see E07).
Design Specification:
- Check in service layer before assignment
Sample Implementation:
```java
if (!certificationService.isEligibleForAssignment(employeeId, asset.getCertificationRequired())) { ... }
```

---

## E10 - Performance Reviews & Goals

Section: Overview of Spring Boot Architecture
Description: Manages performance review cycles, goals, and feedback.
Design Specification:
- Review module with REST endpoints
- Service for review workflow and PDF export
Sample Implementation:
```java
@RestController
@RequestMapping("/reviews")
public class PerformanceReviewController { ... }
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Review module encapsulates all review logic.
Design Specification:
- `com.company.wems.review.controller`
- `com.company.wems.review.service`
- `com.company.wems.review.repository`
- `com.company.wems.review.model`
Sample Implementation:
```
review/
  controller/
  service/
  repository/
  model/
```

Section: Entity Design
Description: ReviewCycle, PerformanceReview entities.
Design Specification:
- ReviewCycle: id, name, periodStart, periodEnd, status
- PerformanceReview: id, employee, cycle, goals, competencies, ratings, comments, supervisor, acknowledgedByEmployee, acknowledgedBySupervisor, pdfUrl
Sample Implementation:
```java
@Entity
public class ReviewCycle {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String status; // OPEN, CLOSED
    // ...
}
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ReviewCycle cycle;
    @ElementCollection
    private List<String> goals;
    @ElementCollection
    private List<String> competencies;
    private String ratings;
    private String comments;
    @ManyToOne
    private Employee supervisor;
    private boolean acknowledgedByEmployee;
    private boolean acknowledgedBySupervisor;
    private String pdfUrl;
    // ...
}
```

Section: Service Layer Specifications
Description: Handles review creation, workflow, and export.
Design Specification:
- Methods: createReviewCycle, assignReview, submitReview, acknowledge, exportPDF
Sample Implementation:
```java
public interface ReviewService {
    ReviewCycleDTO createReviewCycle(ReviewCycleDTO dto);
    PerformanceReviewDTO assignReview(Long employeeId, Long cycleId);
    PerformanceReviewDTO submitReview(Long reviewId, PerformanceReviewDTO dto);
    void acknowledge(Long reviewId, boolean byEmployee);
    String exportPDF(Long reviewId);
}
```

Section: Repository Layer
Description: Review persistence and queries.
Design Specification:
- `ReviewCycleRepository`, `PerformanceReviewRepository`
Sample Implementation:
```java
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    List<PerformanceReview> findByEmployee(Employee employee);
}
```

Section: Controller Specifications
Description: REST endpoints for review management.
Design Specification:
- `POST /reviews/cycles`
- `POST /reviews/assign`
- `POST /reviews/{id}/submit`
- `POST /reviews/{id}/acknowledge`
- `GET /reviews/{id}/export`
Sample Implementation:
```java
@GetMapping("/{id}/export")
public ResponseEntity<Resource> exportPDF(@PathVariable Long id) { ... }
```

Section: Integration Points
Description: Role-based visibility and immutable history after sign-off.
Design Specification:
- Access checks in service/controller
Sample Implementation:
```java
@PreAuthorize("hasRole('HR') or (hasRole('SUPERVISOR') and @reviewSecurity.isSupervisor(authentication, #id))")
```

---

## E11 - Payroll Export Integration

Section: Overview of Spring Boot Architecture
Description: Exports payroll-ready files from attendance and leave data.
Design Specification:
- Payroll module with REST endpoints
- Service for file generation and delivery
Sample Implementation:
```java
@RestController
@RequestMapping("/payroll")
public class PayrollController { ... }
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Payroll module encapsulates all payroll logic.
Design Specification:
- `com.company.wems.payroll.controller`
- `com.company.wems.payroll.service`
- `com.company.wems.payroll.repository`
- `com.company.wems.payroll.model`
Sample Implementation:
```
payroll/
  controller/
  service/
  repository/
  model/
```

Section: Service Layer Specifications
Description: Handles export generation, mapping, and delivery.
Design Specification:
- Methods: generateExport, deliverExport, retryFailedExports
Sample Implementation:
```java
public interface PayrollService {
    PayrollExportDTO generateExport(LocalDate from, LocalDate to);
    void deliverExport(PayrollExportDTO export);
    void retryFailedExports();
}
```

Section: Controller Specifications
Description: REST endpoints for payroll export.
Design Specification:
- `POST /payroll/export`
Sample Implementation:
```java
@PostMapping("/export")
public ResponseEntity<Void> exportPayroll(@RequestBody PayrollExportRequestDTO dto) { ... }
```

Section: Integration Points
Description: Secure delivery via SFTP/API, audit log for every export (see E14).
Design Specification:
- SFTP client integration
- Audit service call
Sample Implementation:
```java
sftpClient.send(exportFile);
auditService.logPayrollExport(...);
```

---

## E12 - Notifications & Announcements

Section: Overview of Spring Boot Architecture
Description: Manages in-app, email, and SMS notifications for events and announcements.
Design Specification:
- Notification module with REST endpoints
- Service for delivery and tracking
Sample Implementation:
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController { ... }
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Notification module encapsulates all notification logic.
Design Specification:
- `com.company.wems.notification.controller`
- `com.company.wems.notification.service`
- `com.company.wems.notification.repository`
- `com.company.wems.notification.model`
Sample Implementation:
```
notification/
  controller/
  service/
  repository/
  model/
```

Section: Entity Design
Description: Notification, Announcement entities.
Design Specification:
- Notification: id, user, channel, type, content, status, deliveryTime
- Announcement: id, title, content, startDate, endDate, visibleToRoles
Sample Implementation:
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee user;
    private String channel; // IN_APP, EMAIL, SMS
    private String type;
    private String content;
    private String status; // SENT, FAILED, DELIVERED
    private LocalDateTime deliveryTime;
    // ...
}
@Entity
public class Announcement {
    @Id @GeneratedValue
    private Long id;
    private String title;
    private String content;
    private LocalDate startDate;
    private LocalDate endDate;
    @ElementCollection
    private Set<String> visibleToRoles;
    // ...
}
```

Section: Service Layer Specifications
Description: Handles notification delivery, opt-in/out, and tracking.
Design Specification:
- Methods: sendNotification, trackDelivery, optInOut, createAnnouncement
Sample Implementation:
```java
public interface NotificationService {
    void sendNotification(NotificationDTO dto);
    void trackDelivery(Long notificationId, String status);
    void optInOut(Long userId, String channel, boolean optIn);
    AnnouncementDTO createAnnouncement(AnnouncementDTO dto);
}
```

Section: Controller Specifications
Description: REST endpoints for notifications and announcements.
Design Specification:
- `POST /notifications/send`
- `POST /notifications/opt-in`
- `POST /announcements`
Sample Implementation:
```java
@PostMapping("/send")
public ResponseEntity<Void> sendNotification(@RequestBody NotificationDTO dto) { ... }
```

Section: Integration Points
Description: Integrates with email/SMS providers, localized templates.
Design Specification:
- Email/SMS client integration
- Template service
Sample Implementation:
```java
emailClient.send(...);
templateService.render(...);
```

---

## E13 - Integration Layer (HRIS/WMS APIs)

Section: Overview of Spring Boot Architecture
Description: Exposes and consumes REST APIs for HRIS, WMS, and SSO.
Design Specification:
- Integration module with REST endpoints and connectors
- Service for sync jobs and webhooks
Sample Implementation:
```java
@RestController
@RequestMapping("/integration")
public class IntegrationController { ... }
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Integration module encapsulates all integration logic.
Design Specification:
- `com.company.wems.integration.controller`
- `com.company.wems.integration.service`
- `com.company.wems.integration.connector`
Sample Implementation:
```
integration/
  controller/
  service/
  connector/
```

Section: Service Layer Specifications
Description: Handles HRIS/WMS sync, SSO, and webhooks.
Design Specification:
- Methods: syncHRIS, syncWMS, handleWebhook
Sample Implementation:
```java
public interface IntegrationService {
    void syncHRIS();
    void syncWMS();
    void handleWebhook(WebhookEventDTO dto);
}
```

Section: Controller Specifications
Description: REST endpoints for integration.
Design Specification:
- `POST /integration/hris/sync`
- `POST /integration/wms/sync`
- `POST /integration/webhook`
Sample Implementation:
```java
@PostMapping("/hris/sync")
public ResponseEntity<Void> syncHRIS() { ... }
```

Section: Configuration and Security Settings
Description: Secures APIs with JWT/OAuth2.
Design Specification:
- Security config as in E03
Sample Implementation:
```java
@PreAuthorize("hasRole('ADMIN')")
```

Section: Integration Points
Description: Consumes external APIs, exposes OpenAPI docs.
Design Specification:
- RestTemplate/WebClient for outbound calls
Sample Implementation:
```java
WebClient.create().post().uri(hrisUrl).body(...).retrieve().bodyToMono(...);
```

---

## E14 - Audit Trail & Compliance

Section: Overview of Spring Boot Architecture
Description: Centralized audit logging for sensitive changes.
Design Specification:
- Audit module with service for logging and export
Sample Implementation:
```java
@Service
public class AuditService { ... }
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Audit module encapsulates all audit logic.
Design Specification:
- `com.company.wems.audit.service`
- `com.company.wems.audit.repository`
- `com.company.wems.audit.model`
Sample Implementation:
```
audit/
  service/
  repository/
  model/
```

Section: Entity Design
Description: AuditLog entity with before/after state.
Design Specification:
- Fields: id, entityType, entityId, actor, timestamp, action, before, after
Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entityType;
    private Long entityId;
    private String actor;
    private LocalDateTime timestamp;
    private String action; // CREATE, UPDATE, DELETE
    @Lob
    private String before;
    @Lob
    private String after;
    // ...
}
```

Section: Service Layer Specifications
Description: Handles audit logging and export.
Design Specification:
- Methods: logChange, exportLogs
Sample Implementation:
```java
public interface AuditService {
    void logChange(String entityType, Long entityId, String action, Object before, Object after, String actor);
    List<AuditLogDTO> exportLogs(LocalDate from, LocalDate to, String entityType);
}
```

Section: Repository Layer
Description: Audit log persistence and queries.
Design Specification:
- `AuditLogRepository`
Sample Implementation:
```java
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByTimestampBetweenAndEntityType(LocalDateTime from, LocalDateTime to, String entityType);
}
```

Section: Integration Points
Description: Tamper-evident storage, export for compliance.
Design Specification:
- Hashing/signature for log entries
Sample Implementation:
```java
auditLog.setHash(calculateHash(auditLog));
```

---

## E15 - Reporting & Analytics

Section: Overview of Spring Boot Architecture
Description: Provides operational reports and dashboards.
Design Specification:
- Reporting module with REST endpoints
- Service for report generation and export
Sample Implementation:
```java
@RestController
@RequestMapping("/reports")
public class ReportController { ... }
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Reporting module encapsulates all reporting logic.
Design Specification:
- `com.company.wems.report.controller`
- `com.company.wems.report.service`
Sample Implementation:
```
report/
  controller/
  service/
```

Section: Service Layer Specifications
Description: Handles report generation, filtering, and export.
Design Specification:
- Methods: generateAttendanceReport, generateOvertimeReport, exportCSV, exportPDF
Sample Implementation:
```java
public interface ReportService {
    ReportDTO generateAttendanceReport(ReportFilter filter);
    byte[] exportCSV(ReportDTO report);
    byte[] exportPDF(ReportDTO report);
}
```

Section: Controller Specifications
Description: REST endpoints for reports.
Design Specification:
- `GET /reports/attendance`
- `GET /reports/overtime`
- `GET /reports/export`
Sample Implementation:
```java
@GetMapping("/attendance")
public ReportDTO getAttendanceReport(@RequestParam ... ) { ... }
```

Section: Integration Points
Description: Exposes metrics endpoints for BI.
Design Specification:
- `/metrics/reporting`
Sample Implementation:
```java
@GetMapping("/metrics")
public ReportingMetricsDTO getReportingMetrics() { ... }
```

---

## E16 - Mobile Access (PWA)

Section: Overview of Spring Boot Architecture
Description: Enables mobile-friendly, offline-capable access via PWA.
Design Specification:
- PWA manifest and service worker
- Responsive UI for core flows
Sample Implementation:
```html
<link rel="manifest" href="/manifest.json">
<script src="/service-worker.js"></script>
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Frontend module (React/Vue/Angular) in `mobile/` directory.
Design Specification:
- `mobile/public/manifest.json`
- `mobile/src/service-worker.js`
Sample Implementation:
```json
{
  "name": "WEMS Mobile",
  "short_name": "WEMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#fff",
  "theme_color": "#1976d2"
}
```

Section: Integration Points
Description: Offline queue for clock events, conflict resolution.
Design Specification:
- IndexedDB/localStorage for offline events
Sample Implementation:
```js
// service-worker.js
self.addEventListener('sync', function(event) {
  if (event.tag === 'sync-clock-events') {
    // send queued events to backend
  }
});
```

---

## E17 - Onboarding & Offboarding Workflow

Section: Overview of Spring Boot Architecture
Description: Automates onboarding/offboarding tasks and asset/training assignment.
Design Specification:
- Workflow module with REST endpoints
- Service for task orchestration
Sample Implementation:
```java
@RestController
@RequestMapping("/workflow")
public class WorkflowController { ... }
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Workflow module encapsulates all workflow logic.
Design Specification:
- `com.company.wems.workflow.controller`
- `com.company.wems.workflow.service`
Sample Implementation:
```
workflow/
  controller/
  service/
```

Section: Service Layer Specifications
Description: Handles provisioning, deprovisioning, and task generation.
Design Specification:
- Methods: onboardEmployee, offboardEmployee, generateTasks
Sample Implementation:
```java
public interface WorkflowService {
    void onboardEmployee(Long employeeId);
    void offboardEmployee(Long employeeId);
    List<TaskDTO> generateTasks(Long employeeId, String workflowType);
}
```

Section: Integration Points
Description: Integrates with HRIS, asset, and training modules.
Design Specification:
- Calls to integration, asset, and certification services
Sample Implementation:
```java
integrationService.syncHRIS();
assetService.assignAsset(...);
certificationService.addCertification(...);
```

---

## E18 - Localization & Multi-Tenant

Section: Overview of Spring Boot Architecture
Description: Supports multiple languages and tenants.
Design Specification:
- Message bundles for i18n
- Tenant context via filter/interceptor
Sample Implementation:
```java
@Bean
public LocaleResolver localeResolver() {
    SessionLocaleResolver slr = new SessionLocaleResolver();
    slr.setDefaultLocale(Locale.US);
    return slr;
}
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Core module enhancements for i18n and tenancy.
Design Specification:
- `core/i18n/`
- `core/tenant/`
Sample Implementation:
```
core/
  i18n/
  tenant/
```

Section: Configuration and Security Settings
Description: Loads messages and enforces tenant isolation.
Design Specification:
- `messages.properties`, `messages_es.properties`, etc.
- Tenant ID in JWT or request header
Sample Implementation:
```java
public class TenantInterceptor implements HandlerInterceptor { ... }
```

---

## E19 - Observability & Monitoring

Section: Overview of Spring Boot Architecture
Description: Enables observability via metrics, logs, and tracing.
Design Specification:
- Actuator endpoints for health, metrics, traces
- Logback/ELK integration
Sample Implementation:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,trace
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Core module enhancements for observability.
Design Specification:
- `core/observability/`
Sample Implementation:
```
core/
  observability/
```

Section: Integration Points
Description: Integrates with Prometheus, Grafana, ELK.
Design Specification:
- Prometheus scrape config
Sample Implementation:
```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
```

---

## E20 - Deployment & CI/CD

Section: Overview of Spring Boot Architecture
Description: Automates build, test, and deployment pipelines.
Design Specification:
- Dockerfile for containerization
- CI/CD pipeline (GitHub Actions/Jenkins)
Sample Implementation:
```dockerfile
FROM eclipse-temurin:17-jre
COPY target/wems.jar /app/wems.jar
ENTRYPOINT ["java", "-jar", "/app/wems.jar"]
```

Section: Package Structure, Module Definitions, and Component Breakdown
Description: Deployment scripts and pipeline configs in `deploy/`.
Design Specification:
- `deploy/Dockerfile`
- `deploy/ci.yml`
Sample Implementation:
```yaml
# .github/workflows/ci.yml
name: CI
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Build with Maven
        run: mvn clean package
      - name: Build Docker image
        run: docker build -t wems:latest .
```

---

**End of Document**