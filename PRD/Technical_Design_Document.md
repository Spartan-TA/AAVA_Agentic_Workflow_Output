# Warehouse Employee Management System â Comprehensive Technical Design Document

## Table of Contents
- [E01 â Project Scaffolding & Domain Setup](#e01)
- [E02 â Employee Master Data (CRUD)](#e02)
- [E03 â Role-Based Access Control (RBAC)](#e03)
- [E04 â Time & Attendance (Clock In/Out)](#e04)
- [E05 â Shift & Schedule Management](#e05)
- [E06 â Leave & Absence Management](#e06)
- [E07 â Training & Certification Tracking](#e07)
- [E08 â Safety Incidents & OSHA Reporting](#e08)
- [E09 â Equipment & Asset Assignment](#e09)
- [E10 â Performance Reviews & Goals](#e10)
- [E11 â Payroll Export Integration](#e11)
- [E12 â Notifications & Announcements](#e12)
- [E13 â Integration Layer (HRIS/WMS APIs)](#e13)
- [E14 â Audit Trail & Compliance](#e14)
- [E15 â Reporting & Analytics](#e15)
- [E16 â Mobile Access (PWA)](#e16)
- [E17 â Onboarding & Offboarding Workflow](#e17)
- [E18 â Localization](#e18)
- [E19 â Advanced Scheduling](#e19)
- [E20 â Self-Service Portal](#e20)

---

## <a name="e01"></a>E01 â Project Scaffolding & Domain Setup

### 1. Architecture Overview
- Spring Boot (Maven) project.
- Modular structure: employee, scheduling, attendance, safety.
- Flyway/Liquibase for DB migrations.
- Spring Boot Actuator for health checks.

### 2. Package Structure
```
com.wms
  âââ employee
  âââ scheduling
  âââ attendance
  âââ safety
  âââ config
  âââ common
```

### 3. Domain Model Design
- No business entities, but base entities for each module.

### 4. Repository Layer
- No repositories in scaffolding.

### 5. Service Layer
- No services in scaffolding.

### 6. Controller Layer
- No controllers in scaffolding.

### 7. Security Configuration
- No security in scaffolding.

### 8. Configuration Properties
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wms
    username: wms_user
    password: wms_pass
  flyway:
    enabled: true
  liquibase:
    enabled: false

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### 9. Integration Points
- None.

### 10. Code Samples

#### Maven pom.xml
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-core</artifactId>
</dependency>
```

#### Application Entry Point
```java
@SpringBootApplication
public class WmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WmsApplication.class, args);
    }
}
```

---

## <a name="e02"></a>E02 â Employee Master Data (CRUD)

### 1. Architecture Overview
- RESTful CRUD APIs for Employee entity.
- DTOs for web layer.
- Pagination, filtering, soft-delete.

### 2. Package Structure
```
com.wms.employee
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model Design

#### Employee Entity
```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
}
```

### 4. Repository Layer

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

### 5. Service Layer

```java
public interface EmployeeService {
    EmployeeDTO createEmployee(EmployeeDTO dto);
    EmployeeDTO getEmployee(Long id);
    Page<EmployeeDTO> listEmployees(Pageable pageable, EmployeeFilter filter);
    EmployeeDTO updateEmployee(Long id, EmployeeDTO dto);
    void deleteEmployee(Long id);
}
```

### 6. Controller Layer

```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@RequestBody @Valid EmployeeDTO dto) { ... }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> get(@PathVariable Long id) { ... }

    @GetMapping
    public ResponseEntity<Page<EmployeeDTO>> list(Pageable pageable, EmployeeFilter filter) { ... }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> update(@PathVariable Long id, @RequestBody EmployeeDTO dto) { ... }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
}
```

#### EmployeeDTO
```java
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

### 7. Security Configuration
- Endpoints protected by RBAC (see E03).

### 8. Configuration Properties
```yaml
spring:
  data:
    web:
      pageable:
        default-page-size: 20
        max-page-size: 100
```

### 9. Integration Points
- OpenAPI schema generation.

### 10. Code Samples

#### Soft Delete Implementation
```java
@Transactional
public void deleteEmployee(Long id) {
    Employee emp = repository.findById(id).orElseThrow();
    emp.setDeleted(true);
    repository.save(emp);
}
```

---

## <a name="e03"></a>E03 â Role-Based Access Control (RBAC)

### 1. Architecture Overview
- Spring Security with roles: ADMIN, HR, SUPERVISOR, WORKER.
- Method/endpoint security.
- API key/OAuth2 toggle.

### 2. Package Structure
```
com.wms.security
  âââ config
  âââ model
  âââ service
```

### 3. Domain Model Design

#### User Entity
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles;
}
```

### 4. Repository Layer

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

### 5. Service Layer

```java
public interface UserService {
    UserDetails loadUserByUsername(String username);
}
```

### 6. Controller Layer

- No direct controller; security applied to existing endpoints.

### 7. Security Configuration

```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            .and()
                .httpBasic();
    }
}
```

#### API Key/OAuth2 Toggle
```yaml
security:
  auth-type: oauth2 # or apikey
```

### 8. Configuration Properties
```yaml
spring:
  security:
    user:
      name: admin
      password: adminpass
```

### 9. Integration Points
- OAuth2 provider.

### 10. Code Samples

#### Method Security
```java
@PreAuthorize("hasRole('ADMIN')")
public void sensitiveOperation() { ... }
```

---

## <a name="e04"></a>E04 â Time & Attendance (Clock In/Out)

### 1. Architecture Overview
- REST endpoints for clock-in/out.
- Geofence/device capture.
- Shift association, corrections workflow.

### 2. Package Structure
```
com.wms.attendance
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model Design

#### AttendanceEvent Entity
```java
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    private LocalDateTime timestamp;
    private AttendanceType type; // CLOCK_IN, CLOCK_OUT
    private String deviceId;
    private String location;
    private boolean correction;
}
```

### 4. Repository Layer

```java
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeAndDate(Employee employee, LocalDate date);
}
```

### 5. Service Layer

```java
public interface AttendanceService {
    AttendanceEventDTO clockIn(Long employeeId, ClockEventDTO dto);
    AttendanceEventDTO clockOut(Long employeeId, ClockEventDTO dto);
    List<AttendanceEventDTO> getDailyAttendance(Long employeeId, LocalDate date);
    AttendanceCorrectionDTO requestCorrection(Long eventId, CorrectionDTO dto);
}
```

### 6. Controller Layer

```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceEventDTO> clockIn(@RequestBody ClockEventDTO dto) { ... }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceEventDTO> clockOut(@RequestBody ClockEventDTO dto) { ... }

    @GetMapping("/daily/{employeeId}")
    public ResponseEntity<List<AttendanceEventDTO>> getDaily(@PathVariable Long employeeId, @RequestParam LocalDate date) { ... }

    @PostMapping("/corrections/{eventId}")
    public ResponseEntity<AttendanceCorrectionDTO> requestCorrection(@PathVariable Long eventId, @RequestBody CorrectionDTO dto) { ... }
}
```

### 7. Security Configuration
- RBAC: Only WORKER/SUPERVISOR can clock-in/out.

### 8. Configuration Properties
```yaml
attendance:
  geofence:
    enabled: true
    radius: 100 # meters
```

### 9. Integration Points
- Device/location services.

### 10. Code Samples

#### Clock-In Logic
```java
public AttendanceEventDTO clockIn(Long employeeId, ClockEventDTO dto) {
    // Validate geofence, device, etc.
    // Create AttendanceEvent
}
```

---

## <a name="e05"></a>E05 â Shift & Schedule Management

### 1. Architecture Overview
- Shift templates, rotations, overtime rules.
- Assignment to employees.
- Conflict detection.

### 2. Package Structure
```
com.wms.scheduling
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model Design

#### ShiftTemplate Entity
```java
@Entity
@Table(name = "shift_templates")
public class ShiftTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    private String rotationPattern;
}
```

#### EmployeeShift Entity
```java
@Entity
@Table(name = "employee_shifts")
public class EmployeeShift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    @ManyToOne
    private ShiftTemplate shiftTemplate;

    private LocalDate shiftDate;
    private boolean overtime;
}
```

### 4. Repository Layer

```java
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {}
public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, Long> {
    List<EmployeeShift> findByEmployeeAndShiftDateBetween(Employee employee, LocalDate start, LocalDate end);
}
```

### 5. Service Layer

```java
public interface ShiftService {
    ShiftTemplateDTO createTemplate(ShiftTemplateDTO dto);
    EmployeeShiftDTO assignShift(Long employeeId, ShiftAssignmentDTO dto);
    List<EmployeeShiftDTO> getUpcomingShifts(Long employeeId);
    boolean detectConflict(Long employeeId, LocalDate shiftDate);
}
```

### 6. Controller Layer

```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates")
    public ResponseEntity<ShiftTemplateDTO> createTemplate(@RequestBody ShiftTemplateDTO dto) { ... }

    @PostMapping("/assign")
    public ResponseEntity<EmployeeShiftDTO> assignShift(@RequestBody ShiftAssignmentDTO dto) { ... }

    @GetMapping("/upcoming/{employeeId}")
    public ResponseEntity<List<EmployeeShiftDTO>> getUpcoming(@PathVariable Long employeeId) { ... }
}
```

### 7. Security Configuration
- Supervisors can bulk-assign; workers see personal shifts.

### 8. Configuration Properties
```yaml
scheduling:
  blackout-dates: [2024-12-25, 2024-01-01]
```

### 9. Integration Points
- Calendar APIs.

### 10. Code Samples

#### Conflict Detection
```java
public boolean detectConflict(Long employeeId, LocalDate shiftDate) {
    // Check for overlapping shifts, blackout dates, etc.
}
```

---

## <a name="e06"></a>E06 â Leave & Absence Management

### 1. Architecture Overview
- PTO, sick, unpaid leave requests/approvals.
- Accrual balances.
- Integration with scheduling/payroll.

### 2. Package Structure
```
com.wms.leave
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model Design

#### LeaveRequest Entity
```java
@Entity
@Table(name = "leave_requests")
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    private LeaveType type; // PTO, SICK, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveStatus status; // REQUESTED, APPROVED, DENIED
    private String reason;
}
```

#### LeaveBalance Entity
```java
@Entity
@Table(name = "leave_balances")
public class LeaveBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    private LeaveType type;
    private int balance;
}
```

### 4. Repository Layer

```java
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployee(Employee employee);
}
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    LeaveBalance findByEmployeeAndType(Employee employee, LeaveType type);
}
```

### 5. Service Layer

```java
public interface LeaveService {
    LeaveRequestDTO requestLeave(Long employeeId, LeaveRequestDTO dto);
    LeaveRequestDTO approveLeave(Long requestId);
    LeaveRequestDTO denyLeave(Long requestId);
    LeaveBalanceDTO getBalance(Long employeeId, LeaveType type);
}
```

### 6. Controller Layer

```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/request")
    public ResponseEntity<LeaveRequestDTO> request(@RequestBody LeaveRequestDTO dto) { ... }

    @PostMapping("/approve/{requestId}")
    public ResponseEntity<LeaveRequestDTO> approve(@PathVariable Long requestId) { ... }

    @PostMapping("/deny/{requestId}")
    public ResponseEntity<LeaveRequestDTO> deny(@PathVariable Long requestId) { ... }

    @GetMapping("/balance/{employeeId}")
    public ResponseEntity<LeaveBalanceDTO> getBalance(@PathVariable Long employeeId, @RequestParam LeaveType type) { ... }
}
```

### 7. Security Configuration
- Employees request; supervisors approve/deny.

### 8. Configuration Properties
```yaml
leave:
  accrual:
    pto: 10
    sick: 5
```

### 9. Integration Points
- Payroll, scheduling modules.

### 10. Code Samples

#### Leave Request Logic
```java
public LeaveRequestDTO requestLeave(Long employeeId, LeaveRequestDTO dto) {
    // Validate balance, create request, update schedule
}
```

---

## <a name="e07"></a>E07 â Training & Certification Tracking

### 1. Architecture Overview
- Track certifications, expirations, renewals.
- Block assignment if expired.
- Upload proof documents.

### 2. Package Structure
```
com.wms.certification
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model Design

#### Certification Entity
```java
@Entity
@Table(name = "certifications")
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    private String name;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
}
```

### 4. Repository Layer

```java
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByEmployee(Employee employee);
    List<Certification> findByExpiryDateBetween(LocalDate start, LocalDate end);
}
```

### 5. Service Layer

```java
public interface CertificationService {
    CertificationDTO addCertification(Long employeeId, CertificationDTO dto);
    List<CertificationDTO> getCertifications(Long employeeId);
    List<CertificationDTO> getExpiringCertifications(int days);
}
```

### 6. Controller Layer

```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping("/{employeeId}")
    public ResponseEntity<CertificationDTO> add(@PathVariable Long employeeId, @RequestBody CertificationDTO dto) { ... }

    @GetMapping("/{employeeId}")
    public ResponseEntity<List<CertificationDTO>> get(@PathVariable Long employeeId) { ... }

    @GetMapping("/expiring")
    public ResponseEntity<List<CertificationDTO>> getExpiring(@RequestParam int days) { ... }
}
```

### 7. Security Configuration
- Only supervisors/admins can add; employees view.

### 8. Configuration Properties
```yaml
certification:
  alert-days: [30, 7]
```

### 9. Integration Points
- Document storage (S3, etc.).

### 10. Code Samples

#### Expiry Alert Logic
```java
public List<CertificationDTO> getExpiringCertifications(int days) {
    LocalDate now = LocalDate.now();
    LocalDate end = now.plusDays(days);
    return repository.findByExpiryDateBetween(now, end);
}
```

---

## <a name="e08"></a>E08 â Safety Incidents & OSHA Reporting

### 1. Architecture Overview
- Record incidents/near-misses.
- Workflow for investigation/corrective actions.
- OSHA summary export.

### 2. Package Structure
```
com.wms.safety
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model Design

#### SafetyIncident Entity
```java
@Entity
@Table(name = "safety_incidents")
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String severity;
    private String location;
    private String description;

    @ManyToMany
    private List<Employee> involvedEmployees;

    private IncidentStatus status; // OPEN, INVESTIGATING, RESOLVED
    private LocalDateTime reportedAt;
}
```

### 4. Repository Layer

```java
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
    List<SafetyIncident> findByStatus(IncidentStatus status);
}
```

### 5. Service Layer

```java
public interface SafetyService {
    SafetyIncidentDTO reportIncident(SafetyIncidentDTO dto);
    SafetyIncidentDTO updateStatus(Long incidentId, IncidentStatus status);
    List<SafetyIncidentDTO> exportOSHA();
}
```

### 6. Controller Layer

```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
    @PostMapping
    public ResponseEntity<SafetyIncidentDTO> report(@RequestBody SafetyIncidentDTO dto) { ... }

    @PatchMapping("/{incidentId}/status")
    public ResponseEntity<SafetyIncidentDTO> updateStatus(@PathVariable Long incidentId, @RequestParam IncidentStatus status) { ... }

    @GetMapping("/osha-export")
    public ResponseEntity<List<SafetyIncidentDTO>> exportOSHA() { ... }
}
```

### 7. Security Configuration
- Only supervisors/admins can update status.

### 8. Configuration Properties
```yaml
safety:
  osha-export:
    fields: [severity, location, description, status]
```

### 9. Integration Points
- OSHA reporting APIs.

### 10. Code Samples

#### Status Workflow
```java
public SafetyIncidentDTO updateStatus(Long incidentId, IncidentStatus status) {
    SafetyIncident incident = repository.findById(incidentId).orElseThrow();
    incident.setStatus(status);
    repository.save(incident);
}
```

---

## <a name="e09"></a>E09 â Equipment & Asset Assignment

### 1. Architecture Overview
- Assign assets to employees.
- Track checkout/return.
- Block use if cert missing.

### 2. Package Structure
```
com.wms.asset
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model Design

#### Asset Entity
```java
@Entity
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // Scanner, Forklift, PPE
    private String serialNumber;
    private AssetCondition condition;
    private boolean assigned;
}
```

#### AssetAssignment Entity
```java
@Entity
@Table(name = "asset_assignments")
public class AssetAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Asset asset;

    @ManyToOne
    private Employee employee;

    private LocalDateTime checkoutTime;
    private LocalDateTime returnTime;
}
```

### 4. Repository Layer

```java
public interface AssetRepository extends JpaRepository<Asset, Long> {}
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Long> {
    List<AssetAssignment> findByEmployee(Employee employee);
}
```

### 5. Service Layer

```java
public interface AssetService {
    AssetDTO assignAsset(Long employeeId, AssetAssignmentDTO dto);
    AssetDTO returnAsset(Long assignmentId);
    List<AssetDTO> getAssets(Long employeeId);
}
```

### 6. Controller Layer

```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign")
    public ResponseEntity<AssetDTO> assign(@RequestBody AssetAssignmentDTO dto) { ... }

    @PostMapping("/return/{assignmentId}")
    public ResponseEntity<AssetDTO> returnAsset(@PathVariable Long assignmentId) { ... }

    @GetMapping("/{employeeId}")
    public ResponseEntity<List<AssetDTO>> getAssets(@PathVariable Long employeeId) { ... }
}
```

### 7. Security Configuration
- Block assignment if cert invalid.

### 8. Configuration Properties
```yaml
asset:
  overdue-days: 7
```

### 9. Integration Points
- Certification module.

### 10. Code Samples

#### Assignment Block Logic
```java
public AssetDTO assignAsset(Long employeeId, AssetAssignmentDTO dto) {
    // Check employee certifications before assignment
}
```

---

## <a name="e10"></a>E10 â Performance Reviews & Goals

### 1. Architecture Overview
- Review templates, goals, competencies, ratings.
- Supervisor/employee acknowledgements.

### 2. Package Structure
```
com.wms.performance
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model Design

#### PerformanceReview Entity
```java
@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    private String cycle; // Quarterly, Annual
    private String goals;
    private String competencies;
    private int rating;
    private String comments;
    private boolean acknowledgedBySupervisor;
    private boolean acknowledgedByEmployee;
    private boolean signedOff;
}
```

### 4. Repository Layer

```java
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    List<PerformanceReview> findByEmployee(Employee employee);
}
```

### 5. Service Layer

```java
public interface PerformanceService {
    PerformanceReviewDTO createReview(Long employeeId, PerformanceReviewDTO dto);
    PerformanceReviewDTO acknowledge(Long reviewId, boolean supervisor);
    List<PerformanceReviewDTO> getReviews(Long employeeId);
}
```

### 6. Controller Layer

```java
@RestController
@RequestMapping("/performance")
public class PerformanceController {
    @PostMapping("/{employeeId}")
    public ResponseEntity<PerformanceReviewDTO> create(@PathVariable Long employeeId, @RequestBody PerformanceReviewDTO dto) { ... }

    @PostMapping("/acknowledge/{reviewId}")
    public ResponseEntity<PerformanceReviewDTO> acknowledge(@PathVariable Long reviewId, @RequestParam boolean supervisor) { ... }

    @GetMapping("/{employeeId}")
    public ResponseEntity<List<PerformanceReviewDTO>> getReviews(@PathVariable Long employeeId) { ... }
}
```

### 7. Security Configuration
- Role-based visibility.

### 8. Configuration Properties
```yaml
performance:
  review-cycles: [Quarterly, Annual]
```

### 9. Integration Points
- PDF export.

### 10. Code Samples

#### Immutable History
```java
public void signOff(Long reviewId) {
    PerformanceReview review = repository.findById(reviewId).orElseThrow();
    review.setSignedOff(true);
    repository.save(review);
    // Prevent further edits
}
```

---

## <a name="e11"></a>E11 â Payroll Export Integration

### 1. Architecture Overview
- Export payroll-ready files.
- Mapping to provider formats.
- Secure delivery (SFTP/API).

### 2. Package Structure
```
com.wms.payroll
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
  âââ integration
```

### 3. Domain Model Design

#### PayrollExport Entity
```java
@Entity
@Table(name = "payroll_exports")
public class PayrollExport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate exportDate;
    private String provider;
    private String fileUrl;
    private ExportStatus status; // SUCCESS, FAILED, RETRY
}
```

### 4. Repository Layer

```java
public interface PayrollExportRepository extends JpaRepository<PayrollExport, Long> {}
```

### 5. Service Layer

```java
public interface PayrollService {
    PayrollExportDTO generateExport(LocalDate start, LocalDate end, String provider);
    PayrollExportDTO retryExport(Long exportId);
}
```

### 6. Controller Layer

```java
@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/export")
    public ResponseEntity<PayrollExportDTO> export(@RequestParam LocalDate start, @RequestParam LocalDate end, @RequestParam String provider) { ... }

    @PostMapping("/retry/{exportId}")
    public ResponseEntity<PayrollExportDTO> retry(@PathVariable Long exportId) { ... }
}
```

### 7. Security Configuration
- Only HR/admins can export.

### 8. Configuration Properties
```yaml
payroll:
  providers:
    - adp
    - paychex
  sftp:
    host: sftp.example.com
    user: payroll
    password: secret
```

### 9. Integration Points
- SFTP/API delivery.

### 10. Code Samples

#### Export Retry Logic
```java
public PayrollExportDTO retryExport(Long exportId) {
    PayrollExport export = repository.findById(exportId).orElseThrow();
    // Retry delivery with backoff
}
```

---

## <a name="e12"></a>E12 â Notifications & Announcements

### 1. Architecture Overview
- In-app, email/SMS notifications.
- Quiet hours, opt-in/out, delivery tracking.

### 2. Package Structure
```
com.wms.notification
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
  âââ integration
```

### 3. Domain Model Design

#### Notification Entity
```java
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String channel; // EMAIL, SMS, IN_APP
    private String template;
    private String recipient;
    private boolean delivered;
    private LocalDateTime sentAt;
}
```

#### Announcement Entity
```java
@Entity
@Table(name = "announcements")
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private LocalDateTime postedAt;
}
```

### 4. Repository Layer

```java
public interface NotificationRepository extends JpaRepository<Notification, Long> {}
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {}
```

### 5. Service Layer

```java
public interface NotificationService {
    NotificationDTO sendNotification(NotificationDTO dto);
    List<NotificationDTO> getNotifications(String recipient);
    AnnouncementDTO postAnnouncement(AnnouncementDTO dto);
    List<AnnouncementDTO> getAnnouncements();
}
```

### 6. Controller Layer

```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping
    public ResponseEntity<NotificationDTO> send(@RequestBody NotificationDTO dto) { ... }

    @GetMapping("/{recipient}")
    public ResponseEntity<List<NotificationDTO>> get(@PathVariable String recipient) { ... }
}

@RestController
@RequestMapping("/announcements")
public class AnnouncementController {
    @PostMapping
    public ResponseEntity<AnnouncementDTO> post(@RequestBody AnnouncementDTO dto) { ... }

    @GetMapping
    public ResponseEntity<List<AnnouncementDTO>> get() { ... }
}
```

### 7. Security Configuration
- Rate limits, opt-in/out.

### 8. Configuration Properties
```yaml
notification:
  quiet-hours: [22, 6]
  channels: [email, sms, in_app]
```

### 9. Integration Points
- Email/SMS gateways.

### 10. Code Samples

#### Delivery Tracking
```java
public NotificationDTO sendNotification(NotificationDTO dto) {
    // Send via channel, update delivered status
}
```

---

## <a name="e13"></a>E13 â Integration Layer (HRIS/WMS APIs)

### 1. Architecture Overview
- REST APIs/connectors for HRIS, WMS, IDP.
- Webhooks for events.

### 2. Package Structure
```
com.wms.integration
  âââ hris
  âââ wms
  âââ idp
  âââ webhook
  âââ controller
  âââ dto
```

### 3. Domain Model Design

#### HRISSyncJob Entity
```java
@Entity
@Table(name = "hris_sync_jobs")
public class HRISSyncJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime syncTime;
    private int created;
    private int updated;
    private SyncStatus status;
}
```

### 4. Repository Layer

```java
public interface HRISSyncJobRepository extends JpaRepository<HRISSyncJob, Long> {}
```

### 5. Service Layer

```java
public interface IntegrationService {
    void syncHRIS();
    void syncWMS();
    void handleWebhook(WebhookDTO dto);
}
```

### 6. Controller Layer

```java
@RestController
@RequestMapping("/integration")
public class IntegrationController {
    @PostMapping("/hris-sync")
    public ResponseEntity<Void> syncHRIS() { ... }

    @PostMapping("/wms-sync")
    public ResponseEntity<Void> syncWMS() { ... }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody WebhookDTO dto) { ... }
}
```

### 7. Security Configuration
- JWT/OAuth2-secured APIs.

### 8. Configuration Properties
```yaml
integration:
  hris:
    url: https://hris.example.com/api
    token: secret
  wms:
    url: https://wms.example.com/api
    token: secret
```

### 9. Integration Points
- HRIS, WMS, IDP.

### 10. Code Samples

#### Webhook Handler
```java
public void handleWebhook(WebhookDTO dto) {
    // Process event, idempotency check
}
```

---

## <a name="e14"></a>E14 â Audit Trail & Compliance

### 1. Architecture Overview
- Centralized audit logging.
- Tamper-evident storage.

### 2. Package Structure
```
com.wms.audit
  âââ model
  âââ repository
  âââ service
  âââ controller
```

### 3. Domain Model Design

#### AuditLog Entity
```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entity;
    private Long entityId;
    private String actor;
    private LocalDateTime timestamp;
    private String action; // CREATE, UPDATE, DELETE
    private String before;
    private String after;
}
```

### 4. Repository Layer

```java
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityAndEntityId(String entity, Long entityId);
}
```

### 5. Service Layer

```java
public interface AuditService {
    void logChange(String entity, Long entityId, String actor, String action, String before, String after);
    List<AuditLogDTO> exportLogs(LocalDate start, LocalDate end, String actor);
}
```

### 6. Controller Layer

```java
@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/export")
    public ResponseEntity<List<AuditLogDTO>> export(@RequestParam LocalDate start, @RequestParam LocalDate end, @RequestParam String actor) { ... }
}
```

### 7. Security Configuration
- Only admins can export.

### 8. Configuration Properties
```yaml
audit:
  export:
    max-rows: 10000
```

### 9. Integration Points
- None.

### 10. Code Samples

#### Audit Logging
```java
public void logChange(String entity, Long entityId, String actor, String action, String before, String after) {
    AuditLog log = new AuditLog(...);
    repository.save(log);
}
```

---

## <a name="e15"></a>E15 â Reporting & Analytics

### 1. Architecture Overview
- Operational reports: attendance, overtime, leave, certifications, safety KPIs.
- CSV/PDF export, dashboards.

### 2. Package Structure
```
com.wms.reporting
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model Design

#### Report Entity
```java
@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private LocalDate generatedAt;
    private String fileUrl;
}
```

### 4. Repository Layer

```java
public interface ReportRepository extends JpaRepository<Report, Long> {}
```

### 5. Service Layer

```java
public interface ReportingService {
    ReportDTO generateReport(String type, LocalDate start, LocalDate end, String department);
    List<ReportDTO> getReports(String type);
}
```

### 6. Controller Layer

```java
@RestController
@RequestMapping("/reports")
public class ReportController {
    @PostMapping("/generate")
    public ResponseEntity<ReportDTO> generate(@RequestParam String type, @RequestParam LocalDate start, @RequestParam LocalDate end, @RequestParam String department) { ... }

    @GetMapping
    public ResponseEntity<List<ReportDTO>> get(@RequestParam String type) { ... }
}
```

### 7. Security Configuration
- Access controlled by role.

### 8. Configuration Properties
```yaml
reporting:
  export:
    max-rows: 50000
    timeout: 10
```

### 9. Integration Points
- BI tools.

### 10. Code Samples

#### Report Generation
```java
public ReportDTO generateReport(String type, LocalDate start, LocalDate end, String department) {
    // Aggregate data, export CSV/PDF
}
```

---

## <a name="e16"></a>E16 â Mobile Access (PWA)

### 1. Architecture Overview
- Responsive views for core flows.
- Offline-friendly via PWA.

### 2. Package Structure
```
com.wms.mobile
  âââ controller
  âââ service
  âââ dto
  âââ manifest
```

### 3. Domain Model Design
- Use DTOs for mobile endpoints.

### 4. Repository Layer
- None.

### 5. Service Layer

```java
public interface MobileService {
    List<ShiftDTO> getUpcomingShifts(Long employeeId);
    AttendanceEventDTO clockIn(ClockEventDTO dto);
    LeaveRequestDTO requestLeave(LeaveRequestDTO dto);
    List<AnnouncementDTO> getAnnouncements();
}
```

### 6. Controller Layer

```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @GetMapping("/shifts/{employeeId}")
    public ResponseEntity<List<ShiftDTO>> getShifts(@PathVariable Long employeeId) { ... }

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceEventDTO> clockIn(@RequestBody ClockEventDTO dto) { ... }

    @PostMapping("/leave")
    public ResponseEntity<LeaveRequestDTO> requestLeave(@RequestBody LeaveRequestDTO dto) { ... }

    @GetMapping("/announcements")
    public ResponseEntity<List<AnnouncementDTO>> getAnnouncements() { ... }
}
```

### 7. Security Configuration
- Mobile authentication.

### 8. Configuration Properties
```yaml
mobile:
  pwa:
    manifest: /static/manifest.json
    offline-queue: true
```

### 9. Integration Points
- None.

### 10. Code Samples

#### PWA Manifest
```json
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

## <a name="e17"></a>E17 â Onboarding & Offboarding Workflow

### 1. Architecture Overview
- Automate provisioning/deprovisioning.
- Initial schedule, training, asset assignment.

### 2. Package Structure
```
com.wms.workflow
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model Design

#### OnboardingTask Entity
```java
@Entity
@Table(name = "onboarding_tasks")
public class OnboardingTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employeeId;
    private String taskType; // ACCOUNT, SCHEDULE, TRAINING, ASSET
    private boolean completed;
}
```

### 4. Repository Layer

```java
public interface OnboardingTaskRepository extends JpaRepository<OnboardingTask, Long> {
    List<OnboardingTask> findByEmployeeId(Long employeeId);
}
```

### 5. Service Layer

```java
public interface WorkflowService {
    void provisionEmployee(Long employeeId);
    void deprovisionEmployee(Long employeeId);
    List<OnboardingTaskDTO> getTasks(Long employeeId);
}
```

### 6. Controller Layer

```java
@RestController
@RequestMapping("/workflow")
public class WorkflowController {
    @PostMapping("/provision/{employeeId}")
    public ResponseEntity<Void> provision(@PathVariable Long employeeId) { ... }

    @PostMapping("/deprovision/{employeeId}")
    public ResponseEntity<Void> deprovision(@PathVariable Long employeeId) { ... }

    @GetMapping("/tasks/{employeeId}")
    public ResponseEntity<List<OnboardingTaskDTO>> getTasks(@PathVariable Long employeeId) { ... }
}
```

### 7. Security Configuration
- Only HR/admins.

### 8. Configuration Properties
```yaml
workflow:
  onboarding:
    enabled: true
```

### 9. Integration Points
- HRIS, asset, training modules.

### 10. Code Samples

#### Provisioning Logic
```java
public void provisionEmployee(Long employeeId) {
    // Create tasks for account, schedule, training, asset assignment
}
```

---

## <a name="e18"></a>E18 â Localization

### 1. Architecture Overview
- Localized templates, UI, notifications.

### 2. Package Structure
```
com.wms.localization
  âââ service
  âââ config
  âââ dto
```

### 3. Domain Model Design
- None.

### 4. Repository Layer
- None.

### 5. Service Layer

```java
public interface LocalizationService {
    String localize(String key, String locale);
}
```

### 6. Controller Layer
- None.

### 7. Security Configuration
- None.

### 8. Configuration Properties
```yaml
localization:
  supported-locales: [en, es, fr]
```

### 9. Integration Points
- None.

### 10. Code Samples

#### Localization Logic
```java
public String localize(String key, String locale) {
    // Lookup translation from resource bundle
}
```

---

## <a name="e19"></a>E19 â Advanced Scheduling

### 1. Architecture Overview
- Complex shift patterns, demand forecasting.

### 2. Package Structure
```
com.wms.advancedscheduling
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model Design

#### AdvancedSchedule Entity
```java
@Entity
@Table(name = "advanced_schedules")
public class AdvancedSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pattern;
    private LocalDate startDate;
    private LocalDate endDate;
    private int demand;
}
```

### 4. Repository Layer

```java
public interface AdvancedScheduleRepository extends JpaRepository<AdvancedSchedule, Long> {}
```

### 5. Service Layer

```java
public interface AdvancedSchedulingService {
    AdvancedScheduleDTO createSchedule(AdvancedScheduleDTO dto);
    List<AdvancedScheduleDTO> getSchedules(LocalDate start, LocalDate end);
}
```

### 6. Controller Layer

```java
@RestController
@RequestMapping("/advanced-scheduling")
public class AdvancedSchedulingController {
    @PostMapping
    public ResponseEntity<AdvancedScheduleDTO> create(@RequestBody AdvancedScheduleDTO dto) { ... }

    @GetMapping
    public ResponseEntity<List<AdvancedScheduleDTO>> get(@RequestParam LocalDate start, @RequestParam LocalDate end) { ... }
}
```

### 7. Security Configuration
- Supervisors/admins.

### 8. Configuration Properties
```yaml
advancedscheduling:
  demand-forecasting: true
```

### 9. Integration Points
- BI tools.

### 10. Code Samples

#### Demand Forecasting
```java
public int forecastDemand(LocalDate date) {
    // Use historical data to predict demand
}
```

---

## <a name="e20"></a>E20 â Self-Service Portal

### 1. Architecture Overview
- Employee self-service for profile, leave, schedule, notifications.

### 2. Package Structure
```
com.wms.portal
  âââ controller
  âââ service
  âââ dto
```

### 3. Domain Model Design
- Use DTOs for portal endpoints.

### 4. Repository Layer
- None.

### 5. Service Layer

```java
public interface PortalService {
    EmployeeDTO getProfile(Long employeeId);
    List<LeaveRequestDTO> getLeaveRequests(Long employeeId);
    List<ShiftDTO> getShifts(Long employeeId);
    List<NotificationDTO> getNotifications(Long employeeId);
}
```

### 6. Controller Layer

```java
@RestController
@RequestMapping("/portal")
public class PortalController {
    @GetMapping("/profile/{employeeId}")
    public ResponseEntity<EmployeeDTO> getProfile(@PathVariable Long employeeId) { ... }

    @GetMapping("/leave/{employeeId}")
    public ResponseEntity<List<LeaveRequestDTO>> getLeaveRequests(@PathVariable Long employeeId) { ... }

    @GetMapping("/shifts/{employeeId}")
    public ResponseEntity<List<ShiftDTO>> getShifts(@PathVariable Long employeeId) { ... }

    @GetMapping("/notifications/{employeeId}")
    public ResponseEntity<List<NotificationDTO>> getNotifications(@PathVariable Long employeeId) { ... }
}
```

### 7. Security Configuration
- Employee authentication.

### 8. Configuration Properties
```yaml
portal:
  self-service:
    enabled: true
```

### 9. Integration Points
- Employee, leave, scheduling, notification modules.

### 10. Code Samples

#### Profile Endpoint
```java
public EmployeeDTO getProfile(Long employeeId) {
    // Fetch employee profile
}
```

---

**End of Document**