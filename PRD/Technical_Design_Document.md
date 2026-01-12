# Warehouse Employee Management System â Low-Level Technical Design (Spring Boot)

---

## Table of Contents

- [E01. Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
- [E02. Employee Master Data (CRUD)](#e02-employee-master-data-crud)
- [E03. Role-Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
- [E04. Time & Attendance (Clock InOut)](#e04-time--attendance-clock-inout)
- [E05. Shift & Schedule Management](#e05-shift--schedule-management)
- [E06. Leave & Absence Management](#e06-leave--absence-management)
- [E07. Training & Certification Tracking](#e07-training--certification-tracking)
- [E08. Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
- [E09. Equipment & Asset Assignment](#e09-equipment--asset-assignment)
- [E10. Performance Reviews & Goals](#e10-performance-reviews--goals)
- [E11. Payroll Export Integration](#e11-payroll-export-integration)
- [E12. Notifications & Announcements](#e12-notifications--announcements)
- [E13. Integration Layer (HRIS/WMS APIs)](#e13-integration-layer-hriswms-apis)
- [E14. Audit Trail & Compliance](#e14-audit-trail--compliance)
- [E15. Reporting & Analytics](#e15-reporting--analytics)
- [E16. Mobile Access (PWA)](#e16-mobile-access-pwa)
- [E17. Onboarding & Offboarding Workflow](#e17-onboarding--offboarding-workflow)
- [E18. Localization](#e18-localization)
- [E19. Advanced Scheduling](#e19-advanced-scheduling)
- [E20. CI/CD](#e20-cicd)

---

## <a name="e01-project-scaffolding--domain-setup"></a>E01. Project Scaffolding & Domain Setup

### 1. Overview

- Spring Boot (Maven) project.
- Modular structure: core, employee, scheduling, attendance, safety, etc.
- Flyway/Liquibase for DB migrations.
- Spring Boot Actuator for monitoring.

### 2. Package Structure

```
com.company.warehouse
âââ config
âââ core
âââ employee
âââ scheduling
âââ attendance
âââ safety
âââ ...
```

### 3. Entity Design

- No business entities; only baseline migration for user and role tables.

### 4. Service Layer

- No business logic; only health checks.

### 5. Repository Layer

- No repositories.

### 6. Controller Specifications

- Health check endpoint via Actuator.

### 7. Configuration & Security

- `application.yml` for environment config.
- Flyway/Liquibase enabled.
- Actuator endpoints exposed on `/actuator/*`.

### 8. Integration Points

- None at this stage.

### 9. Code Snippet

```java
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}
```

---

## <a name="e02-employee-master-data-crud"></a>E02. Employee Master Data (CRUD)

### 1. Overview

- CRUD APIs for Employee entity.
- Soft delete, pagination, filtering.
- OpenAPI documentation.

### 2. Package Structure

```
com.company.warehouse.employee
âââ controller
âââ dto
âââ entity
âââ repository
âââ service
```

### 3. Entity Design

```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @Enumerated(EnumType.STRING)
    private EmployeeRole role;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "shift_group_id")
    private ShiftGroup shiftGroup;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status; // ACTIVE, INACTIVE, TERMINATED

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    // Getters, setters, equals, hashCode
}
```

### 4. Service Layer

**Interface:**
```java
public interface EmployeeService {
    EmployeeDTO create(EmployeeCreateDTO dto);
    EmployeeDTO update(Long id, EmployeeUpdateDTO dto);
    EmployeeDTO get(Long id);
    Page<EmployeeDTO> list(EmployeeFilter filter, Pageable pageable);
    void delete(Long id);
}
```

**Implementation:**
```java
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    // Implements methods, handles soft delete, validation, etc.
}
```

### 5. Repository Layer

```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    // Filtering methods
}
```

### 6. Controller Specifications

```java
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee CRUD APIs")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeCreateDTO dto);

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> get(@PathVariable Long id);

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateDTO dto);

    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeDTO> partialUpdate(@PathVariable Long id, @RequestBody Map<String, Object> updates);

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id);

    @GetMapping
    public Page<EmployeeDTO> list(EmployeeFilter filter, Pageable pageable);
}
```

**DTOs:** `EmployeeDTO`, `EmployeeCreateDTO`, `EmployeeUpdateDTO`, `EmployeeFilter`

### 7. Configuration & Security

- Unique badgeId enforced at DB and service layer.
- Validation via `@Valid` and custom validators.
- Exception handling via `@ControllerAdvice`.

### 8. Integration Points

- Department and ShiftGroup lookups.
- OpenAPI/Swagger documentation.

### 9. Code Snippet

```java
@Schema(description = "Employee DTO")
public class EmployeeDTO {
    public Long id;
    public String name;
    public String badgeId;
    public String role;
    public String department;
    public String shiftGroup;
    public LocalDate hireDate;
    public String status;
}
```

---

## <a name="e03-role-based-access-control-rbac"></a>E03. Role-Based Access Control (RBAC)

### 1. Overview

- Spring Security with roles: ADMIN, HR, SUPERVISOR, WORKER.
- Method/endpoint security.
- Row-level constraints.

### 2. Package Structure

```
com.company.warehouse.security
âââ config
âââ model
âââ service
```

### 3. Entity Design

```java
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue
    private Long id;
    private String username;
    private String password;
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;
    // ...
}

@Entity
@Table(name = "roles")
public class Role {
    @Id @GeneratedValue
    private Long id;
    private String name; // ADMIN, HR, SUPERVISOR, WORKER
}
```

### 4. Service Layer

```java
public interface UserService {
    User findByUsername(String username);
    // ...
}
```

### 5. Repository Layer

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

### 6. Controller Specifications

- Login endpoint (if not using SSO).
- User management endpoints (ADMIN only).

### 7. Configuration & Security

- `@EnableGlobalMethodSecurity(prePostEnabled = true)`
- Security config for endpoint/method security.
- API key/OAuth2 toggle via `application.yml`.

```java
@PreAuthorize("hasRole('ADMIN')")
public void adminOnlyMethod() { ... }
```

### 8. Integration Points

- SSO/IDP integration (see E13).

### 9. Code Snippet

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    // configure(HttpSecurity http), passwordEncoder(), etc.
}
```

---

## <a name="e04-time--attendance-clock-inout"></a>E04. Time & Attendance (Clock In/Out)

### 1. Overview

- Clock-in/out endpoints.
- Geofence/device capture.
- Hours calculation, missed punch correction.

### 2. Package Structure

```
com.company.warehouse.attendance
âââ controller
âââ dto
âââ entity
âââ repository
âââ service
```

### 3. Entity Design

```java
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private AttendanceType type; // CLOCK_IN, CLOCK_OUT

    private LocalDateTime timestamp;

    private String deviceId;
    private String location; // GPS or warehouse zone

    @ManyToOne
    private Shift shift;

    private boolean correctionRequested;
    private boolean approved;

    // ...
}
```

### 4. Service Layer

```java
public interface AttendanceService {
    AttendanceEventDTO clockIn(ClockInDTO dto);
    AttendanceEventDTO clockOut(ClockOutDTO dto);
    AttendanceReportDTO getDailyTotals(Long employeeId, LocalDate date);
    void requestCorrection(Long eventId, CorrectionDTO dto);
}
```

### 5. Repository Layer

```java
@Repository
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeAndDate(Employee employee, LocalDate date);
}
```

### 6. Controller Specifications

```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceEventDTO> clockIn(@Valid @RequestBody ClockInDTO dto);

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceEventDTO> clockOut(@Valid @RequestBody ClockOutDTO dto);

    @PostMapping("/corrections/{eventId}")
    public ResponseEntity<Void> requestCorrection(@PathVariable Long eventId, @RequestBody CorrectionDTO dto);

    @GetMapping("/daily-totals")
    public AttendanceReportDTO getDailyTotals(@RequestParam Long employeeId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);
}
```

### 7. Configuration & Security

- Only authenticated employees can clock in/out.
- Geofence validation (optional).
- Approval workflow for corrections.

### 8. Integration Points

- Shift management (E05).
- Payroll export (E11).

### 9. Code Snippet

```java
@PreAuthorize("hasRole('WORKER')")
public AttendanceEventDTO clockIn(ClockInDTO dto) { ... }
```

---

## <a name="e05-shift--schedule-management"></a>E05. Shift & Schedule Management

### 1. Overview

- Shift templates, rotations, overtime rules.
- Assignment to employees.
- Blackout dates, operation calendars.

### 2. Package Structure

```
com.company.warehouse.scheduling
âââ controller
âââ dto
âââ entity
âââ repository
âââ service
```

### 3. Entity Design

```java
@Entity
@Table(name = "shifts")
public class Shift {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    private String recurrencePattern; // e.g., CRON or custom
    // ...
}

@Entity
@Table(name = "shift_assignments")
public class ShiftAssignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private Shift shift;
    private LocalDate date;
    private boolean overtime;
    // ...
}
```

### 4. Service Layer

```java
public interface ShiftService {
    ShiftDTO createShift(ShiftCreateDTO dto);
    ShiftAssignmentDTO assignShift(ShiftAssignmentDTO dto);
    List<ShiftAssignmentDTO> getAssignments(Long employeeId, LocalDate from, LocalDate to);
    // ...
}
```

### 5. Repository Layer

```java
@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> { }

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    List<ShiftAssignment> findByEmployeeAndDateBetween(Employee employee, LocalDate from, LocalDate to);
}
```

### 6. Controller Specifications

```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping
    public ResponseEntity<ShiftDTO> create(@Valid @RequestBody ShiftCreateDTO dto);

    @PostMapping("/assign")
    public ResponseEntity<ShiftAssignmentDTO> assign(@Valid @RequestBody ShiftAssignmentDTO dto);

    @GetMapping("/assignments")
    public List<ShiftAssignmentDTO> getAssignments(@RequestParam Long employeeId, @RequestParam LocalDate from, @RequestParam LocalDate to);
}
```

### 7. Configuration & Security

- Conflict detection in service layer.
- Only supervisors/HR can assign shifts.

### 8. Integration Points

- Attendance (E04).
- Leave management (E06).

### 9. Code Snippet

```java
@PreAuthorize("hasAnyRole('SUPERVISOR','HR')")
public ShiftAssignmentDTO assignShift(ShiftAssignmentDTO dto) { ... }
```

---

## <a name="e06-leave--absence-management"></a>E06. Leave & Absence Management

### 1. Overview

- PTO, sick, unpaid leave requests/approvals.
- Accrual balances, policies.
- Integration with scheduling and payroll.

### 2. Package Structure

```
com.company.warehouse.leave
âââ controller
âââ dto
âââ entity
âââ repository
âââ service
```

### 3. Entity Design

```java
@Entity
@Table(name = "leaves")
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @Enumerated(EnumType.STRING)
    private LeaveType type; // PTO, SICK, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status; // REQUESTED, APPROVED, DENIED
    private String reason;
    // ...
}
```

### 4. Service Layer

```java
public interface LeaveService {
    LeaveRequestDTO requestLeave(LeaveRequestCreateDTO dto);
    LeaveRequestDTO approveLeave(Long id);
    LeaveRequestDTO denyLeave(Long id, String reason);
    List<LeaveRequestDTO> getEmployeeLeaves(Long employeeId);
}
```

### 5. Repository Layer

```java
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployee(Employee employee);
}
```

### 6. Controller Specifications

```java
@RestController
@RequestMapping("/leaves")
public class LeaveController {
    @PostMapping
    public ResponseEntity<LeaveRequestDTO> request(@Valid @RequestBody LeaveRequestCreateDTO dto);

    @PostMapping("/{id}/approve")
    public ResponseEntity<LeaveRequestDTO> approve(@PathVariable Long id);

    @PostMapping("/{id}/deny")
    public ResponseEntity<LeaveRequestDTO> deny(@PathVariable Long id, @RequestBody DenyReasonDTO dto);

    @GetMapping("/employee/{employeeId}")
    public List<LeaveRequestDTO> getEmployeeLeaves(@PathVariable Long employeeId);
}
```

### 7. Configuration & Security

- Only supervisors/HR can approve/deny.
- Leave balances updated on approval.

### 8. Integration Points

- Scheduling (E05).
- Payroll (E11).

### 9. Code Snippet

```java
@PreAuthorize("hasRole('SUPERVISOR')")
public LeaveRequestDTO approveLeave(Long id) { ... }
```

---

## <a name="e07-training--certification-tracking"></a>E07. Training & Certification Tracking

### 1. Overview

- Track certifications, expirations, renewals.
- Block assignments if expired.
- Upload proof documents.

### 2. Package Structure

```
com.company.warehouse.certification
âââ controller
âââ dto
âââ entity
âââ repository
âââ service
```

### 3. Entity Design

```java
@Entity
@Table(name = "certifications")
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
    @ManyToOne
    private Employee employee;
    // ...
}
```

### 4. Service Layer

```java
public interface CertificationService {
    CertificationDTO create(CertificationCreateDTO dto);
    CertificationDTO renew(Long id, CertificationRenewDTO dto);
    List<CertificationDTO> getExpiringCerts(int days);
}
```

### 5. Repository Layer

```java
@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByExpiryDateBetween(LocalDate from, LocalDate to);
}
```

### 6. Controller Specifications

```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<CertificationDTO> create(@Valid @RequestBody CertificationCreateDTO dto);

    @PostMapping("/{id}/renew")
    public ResponseEntity<CertificationDTO> renew(@PathVariable Long id, @Valid @RequestBody CertificationRenewDTO dto);

    @GetMapping("/expiring")
    public List<CertificationDTO> getExpiringCerts(@RequestParam int days);
}
```

### 7. Configuration & Security

- Alerts for expiring certs.
- Only HR/SUPERVISOR can create/renew.

### 8. Integration Points

- Scheduling (E05).
- Asset assignment (E09).

### 9. Code Snippet

```java
if (certification.getExpiryDate().isBefore(LocalDate.now())) {
    throw new CertificationExpiredException();
}
```

---

## <a name="e08-safety-incidents--osha-reporting"></a>E08. Safety Incidents & OSHA Reporting

### 1. Overview

- Record incidents/near-misses.
- Investigation workflow.
- OSHA summary generation.

### 2. Package Structure

```
com.company.warehouse.safety
âââ controller
âââ dto
âââ entity
âââ repository
âââ service
```

### 3. Entity Design

```java
@Entity
@Table(name = "safety_incidents")
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String description;
    private String location;
    private LocalDateTime occurredAt;
    @Enumerated(EnumType.STRING)
    private IncidentSeverity severity;
    @ManyToMany
    private Set<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status; // OPEN, INVESTIGATING, RESOLVED
    // ...
}
```

### 4. Service Layer

```java
public interface SafetyIncidentService {
    SafetyIncidentDTO reportIncident(SafetyIncidentCreateDTO dto);
    SafetyIncidentDTO updateStatus(Long id, IncidentStatus status);
    List<SafetyIncidentDTO> getIncidents(IncidentFilter filter);
    OSHAReportDTO generateOSHAReport(LocalDate from, LocalDate to);
}
```

### 5. Repository Layer

```java
@Repository
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
    List<SafetyIncident> findByOccurredAtBetween(LocalDateTime from, LocalDateTime to);
}
```

### 6. Controller Specifications

```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
    @PostMapping
    public ResponseEntity<SafetyIncidentDTO> report(@Valid @RequestBody SafetyIncidentCreateDTO dto);

    @PatchMapping("/{id}/status")
    public ResponseEntity<SafetyIncidentDTO> updateStatus(@PathVariable Long id, @RequestBody IncidentStatusDTO dto);

    @GetMapping
    public List<SafetyIncidentDTO> getIncidents(IncidentFilter filter);

    @GetMapping("/osha-report")
    public OSHAReportDTO generateOSHAReport(@RequestParam LocalDate from, @RequestParam LocalDate to);
}
```

### 7. Configuration & Security

- Only authorized roles can update status.
- Validation on required fields.

### 8. Integration Points

- Reporting (E15).

### 9. Code Snippet

```java
if (incident.getStatus() == IncidentStatus.RESOLVED) {
    // Lock further edits
}
```

---

## <a name="e09-equipment--asset-assignment"></a>E09. Equipment & Asset Assignment

### 1. Overview

- Assign assets to employees.
- Track check-in/out, condition.
- Block use if cert missing.

### 2. Package Structure

```
com.company.warehouse.asset
âââ controller
âââ dto
âââ entity
âââ repository
âââ service
```

### 3. Entity Design

```java
@Entity
@Table(name = "assets")
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type; // Scanner, Forklift, PPE
    private String serialNumber;
    private String condition;
    // ...
}

@Entity
@Table(name = "asset_assignments")
public class AssetAssignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Asset asset;
    @ManyToOne
    private Employee employee;
    private LocalDateTime checkedOutAt;
    private LocalDateTime returnedAt;
    // ...
}
```

### 4. Service Layer

```java
public interface AssetService {
    AssetDTO registerAsset(AssetCreateDTO dto);
    AssetAssignmentDTO assignAsset(AssetAssignmentDTO dto);
    void returnAsset(Long assignmentId);
    List<AssetAssignmentDTO> getAssetHistory(Long assetId);
}
```

### 5. Repository Layer

```java
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> { }

@Repository
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Long> {
    List<AssetAssignment> findByAsset(Asset asset);
}
```

### 6. Controller Specifications

```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping
    public ResponseEntity<AssetDTO> register(@Valid @RequestBody AssetCreateDTO dto);

    @PostMapping("/assign")
    public ResponseEntity<AssetAssignmentDTO> assign(@Valid @RequestBody AssetAssignmentDTO dto);

    @PostMapping("/return/{assignmentId}")
    public ResponseEntity<Void> returnAsset(@PathVariable Long assignmentId);

    @GetMapping("/{assetId}/history")
    public List<AssetAssignmentDTO> getAssetHistory(@PathVariable Long assetId);
}
```

### 7. Configuration & Security

- Certification check before assignment.
- Only supervisors/HR can assign.

### 8. Integration Points

- Certification (E07).

### 9. Code Snippet

```java
if (!employee.hasValidCertification(asset.getType())) {
    throw new AssetAssignmentException("Certification required");
}
```

---

## <a name="e10-performance-reviews--goals"></a>E10. Performance Reviews & Goals

### 1. Overview

- Review templates, goals, ratings.
- Supervisor/employee acknowledgements.

### 2. Package Structure

```
com.company.warehouse.performance
âââ controller
âââ dto
âââ entity
âââ repository
âââ service
```

### 3. Entity Design

```java
@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate reviewDate;
    private String templateName;
    private String goals;
    private String competencies;
    private String ratings;
    private String comments;
    private boolean supervisorAcknowledged;
    private boolean employeeAcknowledged;
    // ...
}
```

### 4. Service Layer

```java
public interface PerformanceReviewService {
    PerformanceReviewDTO createReview(PerformanceReviewCreateDTO dto);
    PerformanceReviewDTO acknowledge(Long id, boolean bySupervisor);
    List<PerformanceReviewDTO> getReviews(Long employeeId);
}
```

### 5. Repository Layer

```java
@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    List<PerformanceReview> findByEmployee(Employee employee);
}
```

### 6. Controller Specifications

```java
@RestController
@RequestMapping("/performance/reviews")
public class PerformanceReviewController {
    @PostMapping
    public ResponseEntity<PerformanceReviewDTO> create(@Valid @RequestBody PerformanceReviewCreateDTO dto);

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<PerformanceReviewDTO> acknowledge(@PathVariable Long id, @RequestParam boolean bySupervisor);

    @GetMapping("/employee/{employeeId}")
    public List<PerformanceReviewDTO> getReviews(@PathVariable Long employeeId);
}
```

### 7. Configuration & Security

- Only supervisors/HR can create.
- Immutable after both acknowledgements.

### 8. Integration Points

- Reporting (E15).

### 9. Code Snippet

```java
if (review.isSupervisorAcknowledged() && review.isEmployeeAcknowledged()) {
    // Lock review
}
```

---

## <a name="e11-payroll-export-integration"></a>E11. Payroll Export Integration

### 1. Overview

- Generate payroll files from attendance/leave.
- Map to provider formats.
- Secure delivery (SFTP/API).

### 2. Package Structure

```
com.company.warehouse.payroll
âââ controller
âââ dto
âââ service
```

### 3. Entity Design

- No persistent entities; uses attendance/leave data.

### 4. Service Layer

```java
public interface PayrollExportService {
    PayrollExportDTO generateExport(LocalDate from, LocalDate to, PayrollProvider provider);
    void deliverExport(Long exportId);
}
```

### 5. Repository Layer

- None.

### 6. Controller Specifications

```java
@RestController
@RequestMapping("/payroll/exports")
public class PayrollExportController {
    @PostMapping
    public ResponseEntity<PayrollExportDTO> generate(@RequestBody PayrollExportRequestDTO dto);

    @PostMapping("/{exportId}/deliver")
    public ResponseEntity<Void> deliver(@PathVariable Long exportId);
}
```

### 7. Configuration & Security

- Only ADMIN/HR can export.
- Secure credentials for SFTP/API.

### 8. Integration Points

- Attendance (E04), Leave (E06).

### 9. Code Snippet

```java
if (!totalsMatch()) throw new PayrollExportException("Totals mismatch");
```

---

## <a name="e12-notifications--announcements"></a>E12. Notifications & Announcements

### 1. Overview

- In-app, email/SMS notifications.
- Templates, opt-in/out, quiet hours.

### 2. Package Structure

```
com.company.warehouse.notification
âââ controller
âââ dto
âââ entity
âââ repository
âââ service
```

### 3. Entity Design

```java
@Entity
@Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private String type; // EMAIL, SMS, IN_APP
    private String template;
    private String recipient;
    private String status; // SENT, FAILED
    private LocalDateTime sentAt;
    // ...
}
```

### 4. Service Layer

```java
public interface NotificationService {
    void sendNotification(NotificationDTO dto);
    void sendAnnouncement(AnnouncementDTO dto);
    List<NotificationDTO> getUserNotifications(Long userId);
}
```

### 5. Repository Layer

```java
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipient(String recipient);
}
```

### 6. Controller Specifications

```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping
    public ResponseEntity<Void> send(@RequestBody NotificationDTO dto);

    @PostMapping("/announcements")
    public ResponseEntity<Void> sendAnnouncement(@RequestBody AnnouncementDTO dto);

    @GetMapping("/user/{userId}")
    public List<NotificationDTO> getUserNotifications(@PathVariable Long userId);
}
```

### 7. Configuration & Security

- Rate limits, quiet hours.
- Opt-in/out preferences.

### 8. Integration Points

- All modules for event triggers.

### 9. Code Snippet

```java
if (isQuietHours(now)) {
    queueNotification(notification);
}
```

---

## <a name="e13-integration-layer-hriswms-apis"></a>E13. Integration Layer (HRIS/WMS APIs)

### 1. Overview

- REST APIs/connectors for HRIS, WMS, IDP.
- Webhooks for events.

### 2. Package Structure

```
com.company.warehouse.integration
âââ controller
âââ dto
âââ service
```

### 3. Entity Design

- Integration logs, sync jobs.

```java
@Entity
@Table(name = "integration_logs")
public class IntegrationLog {
    @Id @GeneratedValue
    private Long id;
    private String system;
    private String eventType;
    private String payload;
    private LocalDateTime timestamp;
    private boolean success;
    // ...
}
```

### 4. Service Layer

```java
public interface IntegrationService {
    void syncFromHRIS(HRISPayload payload);
    void syncFromWMS(WMSPayload payload);
    void handleWebhook(WebhookEvent event);
}
```

### 5. Repository Layer

```java
@Repository
public interface IntegrationLogRepository extends JpaRepository<IntegrationLog, Long> { }
```

### 6. Controller Specifications

```java
@RestController
@RequestMapping("/integration")
public class IntegrationController {
    @PostMapping("/hris")
    public ResponseEntity<Void> syncFromHRIS(@RequestBody HRISPayload payload);

    @PostMapping("/wms")
    public ResponseEntity<Void> syncFromWMS(@RequestBody WMSPayload payload);

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody WebhookEvent event);
}
```

### 7. Configuration & Security

- JWT/OAuth2 for APIs.
- Idempotency keys for webhooks.

### 8. Integration Points

- Employee, department, location sync.

### 9. Code Snippet

```java
if (integrationLog.isDuplicate(eventId)) {
    // Ignore duplicate webhook
}
```

---

## <a name="e14-audit-trail--compliance"></a>E14. Audit Trail & Compliance

### 1. Overview

- Centralized audit logging for sensitive changes.
- Tamper-evident storage.

### 2. Package Structure

```
com.company.warehouse.audit
âââ entity
âââ repository
âââ service
```

### 3. Entity Design

```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entityName;
    private Long entityId;
    private String action; // CREATE, UPDATE, DELETE
    private String actor;
    private LocalDateTime timestamp;
    private String beforeState;
    private String afterState;
    // ...
}
```

### 4. Service Layer

```java
public interface AuditService {
    void logChange(String entity, Long id, String action, String before, String after, String actor);
    List<AuditLogDTO> getLogs(AuditLogFilter filter);
}
```

### 5. Repository Layer

```java
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> { }
```

### 6. Controller Specifications

```java
@RestController
@RequestMapping("/audit/logs")
public class AuditLogController {
    @GetMapping
    public List<AuditLogDTO> getLogs(AuditLogFilter filter);
}
```

### 7. Configuration & Security

- Only ADMIN/HR can access logs.
- Immutable log table.

### 8. Integration Points

- All modules.

### 9. Code Snippet

```java
@EventListener
public void onEntityChange(EntityChangeEvent event) {
    auditService.logChange(...);
}
```

---

## <a name="e15-reporting--analytics"></a>E15. Reporting & Analytics

### 1. Overview

- Operational reports: attendance, overtime, leave, certs, safety.
- Export CSV/PDF, dashboards.

### 2. Package Structure

```
com.company.warehouse.reporting
âââ controller
âââ dto
âââ service
```

### 3. Entity Design

- No persistent entities; uses data from other modules.

### 4. Service Layer

```java
public interface ReportingService {
    ReportDTO generateAttendanceReport(ReportFilter filter);
    ReportDTO generateOvertimeReport(ReportFilter filter);
    // ...
}
```

### 5. Repository Layer

- None.

### 6. Controller Specifications

```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ReportDTO attendance(@ModelAttribute ReportFilter filter);

    @GetMapping("/overtime")
    public ReportDTO overtime(@ModelAttribute ReportFilter filter);

    @GetMapping("/export")
    public ResponseEntity<Resource> export(@RequestParam String type, @ModelAttribute ReportFilter filter);
}
```

### 7. Configuration & Security

- Role-based access to reports.

### 8. Integration Points

- All modules.

### 9. Code Snippet

```java
if (rows > 50000) {
    throw new ReportTooLargeException();
}
```

---

## <a name="e16-mobile-access-pwa"></a>E16. Mobile Access (PWA)

### 1. Overview

- Responsive views for core flows.
- Offline support via PWA.

### 2. Package Structure

- Frontend module (not Java).
- Backend: endpoints for mobile clients.

### 3. Entity Design

- N/A.

### 4. Service Layer

- Reuse existing services.

### 5. Repository Layer

- N/A.

### 6. Controller Specifications

- Same as core APIs, with mobile-friendly DTOs if needed.

### 7. Configuration & Security

- CORS, JWT for mobile clients.

### 8. Integration Points

- All modules.

### 9. Code Snippet

```java
// PWA manifest.json and service worker (frontend)
```

---

## <a name="e17-onboarding--offboarding-workflow"></a>E17. Onboarding & Offboarding Workflow

### 1. Overview

- Automate provisioning, training, asset assignment.
- Deprovision on termination.

### 2. Package Structure

```
com.company.warehouse.onboarding
âââ controller
âââ dto
âââ service
```

### 3. Entity Design

- OnboardingTask, OffboardingTask entities.

```java
@Entity
@Table(name = "onboarding_tasks")
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String taskType; // TRAINING, ASSET_ASSIGNMENT, ACCOUNT_PROVISION
    private boolean completed;
    // ...
}
```

### 4. Service Layer

```java
public interface OnboardingService {
    void createOnboardingTasks(Employee employee);
    void completeTask(Long taskId);
    void offboardEmployee(Long employeeId);
}
```

### 5. Repository Layer

```java
@Repository
public interface OnboardingTaskRepository extends JpaRepository<OnboardingTask, Long> { }
```

### 6. Controller Specifications

```java
@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @PostMapping("/employee/{employeeId}/tasks")
    public ResponseEntity<Void> createTasks(@PathVariable Long employeeId);

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<Void> completeTask(@PathVariable Long taskId);

    @PostMapping("/employee/{employeeId}/offboard")
    public ResponseEntity<Void> offboard(@PathVariable Long employeeId);
}
```

### 7. Configuration & Security

- Only HR/ADMIN can trigger.

### 8. Integration Points

- HRIS (E13), Asset (E09), Certification (E07).

### 9. Code Snippet

```java
if (employee.getStatus() == EmployeeStatus.TERMINATED) {
    offboardingService.offboardEmployee(employee.getId());
}
```

---

## <a name="e18-localization"></a>E18. Localization

### 1. Overview

- Multi-language support for UI, notifications, templates.

### 2. Package Structure

```
com.company.warehouse.localization
âââ config
âââ service
```

### 3. Entity Design

- N/A.

### 4. Service Layer

```java
public interface LocalizationService {
    String getMessage(String key, Locale locale);
}
```

### 5. Repository Layer

- N/A.

### 6. Controller Specifications

- N/A.

### 7. Configuration & Security

- `messages_xx.properties` files.
- Accept-Language header support.

### 8. Integration Points

- Notification templates, API error messages.

### 9. Code Snippet

```java
@Autowired
private MessageSource messageSource;

public String getMessage(String key, Locale locale) {
    return messageSource.getMessage(key, null, locale);
}
```

---

## <a name="e19-advanced-scheduling"></a>E19. Advanced Scheduling

### 1. Overview

- Complex shift patterns, auto-scheduling, rule engines.

### 2. Package Structure

```
com.company.warehouse.advancedscheduling
âââ service
```

### 3. Entity Design

- Reuse Shift, ShiftAssignment.

### 4. Service Layer

```java
public interface AdvancedSchedulingService {
    void autoSchedule(LocalDate from, LocalDate to, SchedulingRules rules);
}
```

### 5. Repository Layer

- Reuse scheduling repositories.

### 6. Controller Specifications

```java
@RestController
@RequestMapping("/advanced-scheduling")
public class AdvancedSchedulingController {
    @PostMapping("/auto")
    public ResponseEntity<Void> autoSchedule(@RequestBody AutoScheduleRequestDTO dto);
}
```

### 7. Configuration & Security

- Only ADMIN/HR can trigger.

### 8. Integration Points

- Scheduling (E05).

### 9. Code Snippet

```java
// Rule engine pattern for scheduling
```

---

## <a name="e20-cicd"></a>E20. CI/CD

### 1. Overview

- Automated build, test, deploy pipelines.

### 2. Package Structure

- `.github/workflows/`, `Jenkinsfile`, etc.

### 3. Entity Design

- N/A.

### 4. Service Layer

- N/A.

### 5. Repository Layer

- N/A.

### 6. Controller Specifications

- N/A.

### 7. Configuration & Security

- Secrets management for deploy credentials.
- Quality gates (test, lint, coverage).

### 8. Integration Points

- All modules.

### 9. Code Snippet

```yaml
# .github/workflows/ci.yml
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
      - name: Run tests
        run: mvn test
```

---

# General Considerations

- **Database Schema:** Use Flyway/Liquibase for migrations. All tables have `created_at`, `updated_at`, and (where needed) `deleted` columns.
- **Validation:** Use `@Valid`, custom validators, and service-layer checks.
- **Exception Handling:** Centralized via `@ControllerAdvice`, custom exceptions per module.
- **API Documentation:** OpenAPI/Swagger annotations on all controllers and DTOs.
- **Security:** JWT/OAuth2, method-level security, row-level constraints as needed.
- **Testing:** Unit and integration tests for all service and controller layers.
- **Logging:** Use SLF4J, log all errors and audit events.
- **Extensibility:** Modular package structure, clear separation of concerns.

---

This document provides a production-ready, detailed technical design for all 20 epics of the warehouse employee management system, following Spring Boot industry standards and best practices.