# Warehouse Employee Management System â Comprehensive Low-Level Technical Design Document

---

## Table of Contents

- [E01: Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
- [E02: Employee Master Data CRUD](#e02-employee-master-data-crud)
- [E03: Role-Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
- [E04: Time & Attendance (Clock In/Out)](#e04-time--attendance-clock-inout)
- [E05: Shift & Schedule Management](#e05-shift--schedule-management)
- [E06: Leave & Absence Management](#e06-leave--absence-management)
- [E07: Training & Certification Tracking](#e07-training--certification-tracking)
- [E08: Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
- [E09: Equipment & Asset Assignment](#e09-equipment--asset-assignment)
- [E10: Performance Reviews & Goals](#e10-performance-reviews--goals)
- [E11: Payroll Export Integration](#e11-payroll-export-integration)
- [E12: Notifications & Announcements](#e12-notifications--announcements)
- [E13: Integration Layer (HRIS/WMS APIs)](#e13-integration-layer-hriswms-apis)
- [E14: Audit Trail & Compliance](#e14-audit-trail--compliance)
- [E15: Reporting & Analytics](#e15-reporting--analytics)
- [E16: Mobile Access (PWA)](#e16-mobile-access-pwa)
- [E17: Onboarding & Offboarding Workflow](#e17-onboarding--offboarding-workflow)
- [E18: Localization & Multi-Tenant](#e18-localization--multi-tenant)
- [E19: Observability & Monitoring](#e19-observability--monitoring)
- [E20: CI/CD & Deployment](#e20-cicd--deployment)

---

## <a name="e01-project-scaffolding--domain-setup"></a>E01: Project Scaffolding & Domain Setup

### 1. EPIC OVERVIEW
Initialize Spring Boot (Maven) project; configure base packages; set up core modules (employee, scheduling, attendance, safety); add Flyway/Liquibase for DB migrations; enable Actuator.

### 2. DOMAIN MODEL DESIGN
- No business entities; focus on foundational setup.
- Core modules: `employee`, `scheduling`, `attendance`, `safety`.

### 3. PACKAGE STRUCTURE
```
com.warehousemgmt
âââ employee
âââ scheduling
âââ attendance
âââ safety
âââ config
âââ common
âââ audit
âââ notification
âââ integration
```

### 4. SERVICE LAYER DESIGN
- No business logic; only scaffolding and health checks.

### 5. REPOSITORY LAYER DESIGN
- No repositories; only baseline migration.

### 6. CONTROLLER/API DESIGN
- Health endpoint via Actuator: `/actuator/health`

### 7. SECURITY CONFIGURATION
- None for scaffolding.

### 8. INTEGRATION POINTS
- Actuator endpoints.

### 9. CONFIGURATION PROPERTIES
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse
    username: warehouse_user
    password: secret
  flyway:
    enabled: true
    locations: classpath:db/migration
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### 10. CODE SAMPLES
**Application Entry Point**
```java
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}
```
**Flyway Baseline Migration (V1__baseline.sql)**
```sql
CREATE TABLE employee (...);
CREATE TABLE shift (...);
CREATE TABLE attendance (...);
CREATE TABLE safety_incident (...);
```

---

## <a name="e02-employee-master-data-crud"></a>E02: Employee Master Data CRUD

### 1. EPIC OVERVIEW
Create Employee domain with CRUD APIs and web DTOs: name, badgeId, role, department, shiftGroup, hireDate, status.

### 2. DOMAIN MODEL DESIGN
**Employee Entity**
```java
@Entity
@Table(name = "employee", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String badgeId;
    @Enumerated(EnumType.STRING)
    private EmployeeRole role;
    @ManyToOne
    private Department department;
    @ManyToOne
    private ShiftGroup shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;
    private Boolean deleted = false; // Soft delete
    // Getters, setters, builder
}
```
**Department Entity**
```java
@Entity
public class Department {
    @Id @GeneratedValue
    private Long id;
    private String name;
}
```
**ShiftGroup Entity**
```java
@Entity
public class ShiftGroup {
    @Id @GeneratedValue
    private Long id;
    private String name;
}
```

### 3. PACKAGE STRUCTURE
```
com.warehousemgmt.employee
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### 4. SERVICE LAYER DESIGN
**EmployeeService**
```java
public interface EmployeeService {
    EmployeeDTO createEmployee(EmployeeCreateDTO dto);
    EmployeeDTO getEmployee(Long id);
    Page<EmployeeDTO> listEmployees(EmployeeFilter filter, Pageable pageable);
    EmployeeDTO updateEmployee(Long id, EmployeeUpdateDTO dto);
    void softDeleteEmployee(Long id);
}
```
- Transactional on create/update/delete.

### 5. REPOSITORY LAYER DESIGN
**EmployeeRepository**
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    @Query("SELECT e FROM Employee e WHERE e.deleted = false")
    Page<Employee> findAllActive(Pageable pageable);
}
```

### 6. CONTROLLER/API DESIGN
**EmployeeController**
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeCreateDTO dto);
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> get(@PathVariable Long id);
    @GetMapping
    public Page<EmployeeDTO> list(@RequestParam Map<String, String> filters, Pageable pageable);
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateDTO dto);
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id);
}
```
- DTOs validated with `@Valid`.

### 7. SECURITY CONFIGURATION
- Method security: Only ADMIN/HR can create/update/delete.
- SUPERVISOR can view team.
- WORKER can view self.

### 8. INTEGRATION POINTS
- OpenAPI schema generation.

### 9. CONFIGURATION PROPERTIES
```yaml
employee:
  badgeId:
    unique: true
  pagination:
    default-size: 20
```

### 10. CODE SAMPLES
**DTO Example**
```java
public class EmployeeCreateDTO {
    @NotBlank private String name;
    @NotBlank private String badgeId;
    @NotNull private EmployeeRole role;
    @NotNull private Long departmentId;
    @NotNull private Long shiftGroupId;
    @NotNull private LocalDate hireDate;
}
```
**Soft Delete Pattern**
```java
@Transactional
public void softDeleteEmployee(Long id) {
    Employee emp = employeeRepository.findById(id).orElseThrow(...);
    emp.setDeleted(true);
    employeeRepository.save(emp);
}
```

---

## <a name="e03-role-based-access-control-rbac"></a>E03: Role-Based Access Control (RBAC)

### 1. EPIC OVERVIEW
Add Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER); method/endpoint security and row-level constraints; API key/OAuth2 toggle via config.

### 2. DOMAIN MODEL DESIGN
**EmployeeRole Enum**
```java
public enum EmployeeRole { ADMIN, HR, SUPERVISOR, WORKER }
```
**User Entity**
```java
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private EmployeeRole role;
    @ManyToOne
    private Employee employee;
}
```

### 3. PACKAGE STRUCTURE
```
com.warehousemgmt.security
âââ config
âââ service
âââ model
```

### 4. SERVICE LAYER DESIGN
**UserDetailsServiceImpl**
```java
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    // Loads user by username, maps roles
}
```

### 5. REPOSITORY LAYER DESIGN
**UserRepository**
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

### 6. CONTROLLER/API DESIGN
- Authentication endpoints: `/auth/login`, `/auth/logout`
- Role-based endpoints: secured via annotations

### 7. SECURITY CONFIGURATION
**SecurityConfig**
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .antMatchers("/attendance/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            .and()
            .oauth2Login() // Toggle via config
            .and()
            .apiKeyAuthFilter(); // Custom filter for API key
    }
}
```
- Method-level security: `@PreAuthorize("hasRole('ADMIN')")`

### 8. INTEGRATION POINTS
- OAuth2, API Key, SSO

### 9. CONFIGURATION PROPERTIES
```yaml
security:
  oauth2:
    enabled: true
  api-key:
    enabled: false
```

### 10. CODE SAMPLES
**Method Security**
```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteEmployee(Long id) { ... }
```
**API Key Filter**
```java
public class ApiKeyAuthFilter extends OncePerRequestFilter { ... }
```

---

## <a name="e04-time--attendance-clock-inout"></a>E04: Time & Attendance (Clock In/Out)

### 1. EPIC OVERVIEW
Endpoints for clock-in/out events with geofence and device capture; calculate hours worked per shift; handle missed punches and corrections workflow.

### 2. DOMAIN MODEL DESIGN
**Attendance Entity**
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
    private String geoLocation;
    private Boolean correctionRequested = false;
    private AttendanceStatus status; // NORMAL, CORRECTED, MISSED
}
```

### 3. PACKAGE STRUCTURE
```
com.warehousemgmt.attendance
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### 4. SERVICE LAYER DESIGN
**AttendanceService**
```java
public interface AttendanceService {
    AttendanceDTO clockIn(ClockInDTO dto);
    AttendanceDTO clockOut(ClockOutDTO dto);
    AttendanceDTO requestCorrection(Long attendanceId, CorrectionDTO dto);
    List<AttendanceDTO> getDailyTotals(Long employeeId, LocalDate date);
}
```

### 5. REPOSITORY LAYER DESIGN
**AttendanceRepository**
```java
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEmployeeAndDate(Employee employee, LocalDate date);
}
```

### 6. CONTROLLER/API DESIGN
**AttendanceController**
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceDTO> clockIn(@Valid @RequestBody ClockInDTO dto);
    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceDTO> clockOut(@Valid @RequestBody ClockOutDTO dto);
    @PostMapping("/{id}/correction")
    public ResponseEntity<AttendanceDTO> requestCorrection(@PathVariable Long id, @Valid @RequestBody CorrectionDTO dto);
    @GetMapping("/daily-totals")
    public List<AttendanceDTO> getDailyTotals(@RequestParam Long employeeId, @RequestParam LocalDate date);
}
```

### 7. SECURITY CONFIGURATION
- Only authenticated users can clock in/out.
- Corrections require supervisor approval.

### 8. INTEGRATION POINTS
- Device ID and geofence validation (external service).

### 9. CONFIGURATION PROPERTIES
```yaml
attendance:
  geofence:
    enabled: true
    radius-meters: 50
  correction:
    approval-required: true
```

### 10. CODE SAMPLES
**Geofence Validation Strategy**
```java
public interface GeofenceValidator {
    boolean isWithinFence(String geoLocation, String warehouseLocation);
}
```
**Correction Workflow**
```java
@Transactional
public AttendanceDTO requestCorrection(Long id, CorrectionDTO dto) {
    Attendance att = attendanceRepository.findById(id).orElseThrow(...);
    att.setCorrectionRequested(true);
    att.setStatus(AttendanceStatus.CORRECTED);
    attendanceRepository.save(att);
    // Create approval task
}
```

---

## <a name="e05-shift--schedule-management"></a>E05: Shift & Schedule Management

### 1. EPIC OVERVIEW
Create recurring shift templates, rotations, overtime rules, and assignment to employees; handle blackout dates and warehouse operation calendars.

### 2. DOMAIN MODEL DESIGN
**Shift Entity**
```java
@Entity
public class Shift {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean recurring;
    @ManyToOne
    private ShiftGroup shiftGroup;
    @ManyToMany
    private List<Employee> assignedEmployees;
    private LocalDate blackoutDate;
}
```
**ShiftTemplate Entity**
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean overtimeAllowed;
}
```

### 3. PACKAGE STRUCTURE
```
com.warehousemgmt.scheduling
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### 4. SERVICE LAYER DESIGN
**ShiftService**
```java
public interface ShiftService {
    ShiftDTO createShift(ShiftCreateDTO dto);
    ShiftDTO assignEmployees(Long shiftId, List<Long> employeeIds);
    List<ShiftDTO> getUpcomingShifts(Long employeeId);
    void detectConflicts(Long employeeId, LocalDate date);
}
```

### 5. REPOSITORY LAYER DESIGN
**ShiftRepository**
```java
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findByAssignedEmployeesAndDate(Employee employee, LocalDate date);
}
```

### 6. CONTROLLER/API DESIGN
**ShiftController**
```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping
    public ResponseEntity<ShiftDTO> create(@Valid @RequestBody ShiftCreateDTO dto);
    @PostMapping("/{id}/assign")
    public ResponseEntity<Void> assignEmployees(@PathVariable Long id, @RequestBody List<Long> employeeIds);
    @GetMapping("/upcoming")
    public List<ShiftDTO> getUpcoming(@RequestParam Long employeeId);
}
```

### 7. SECURITY CONFIGURATION
- Only supervisors/admins can assign shifts.

### 8. INTEGRATION POINTS
- Calendar API for blackout dates.

### 9. CONFIGURATION PROPERTIES
```yaml
scheduling:
  blackout-dates:
    - 2024-12-25
    - 2025-01-01
```

### 10. CODE SAMPLES
**Conflict Detection Strategy**
```java
public interface ShiftConflictDetector {
    boolean hasConflict(Employee employee, LocalDate date);
}
```
**Bulk Assignment**
```java
@Transactional
public void assignEmployees(Long shiftId, List<Long> employeeIds) {
    Shift shift = shiftRepository.findById(shiftId).orElseThrow(...);
    List<Employee> employees = employeeRepository.findAllById(employeeIds);
    shift.setAssignedEmployees(employees);
    shiftRepository.save(shift);
}
```

---

## <a name="e06-leave--absence-management"></a>E06: Leave & Absence Management

### 1. EPIC OVERVIEW
Request/approve PTO, sick, unpaid leave; accrual balances and policies; integration hooks to exclude from scheduling and payroll hours.

### 2. DOMAIN MODEL DESIGN
**LeaveRequest Entity**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LeaveType type;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status; // REQUESTED, APPROVED, DENIED
    private String reason;
}
```
**LeaveBalance Entity**
```java
@Entity
public class LeaveBalance {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private Integer ptoDays;
    private Integer sickDays;
    private Integer unpaidDays;
}
```

### 3. PACKAGE STRUCTURE
```
com.warehousemgmt.leave
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### 4. SERVICE LAYER DESIGN
**LeaveService**
```java
public interface LeaveService {
    LeaveRequestDTO requestLeave(LeaveRequestCreateDTO dto);
    LeaveRequestDTO approveLeave(Long requestId);
    LeaveRequestDTO denyLeave(Long requestId);
    LeaveBalanceDTO getBalance(Long employeeId);
}
```

### 5. REPOSITORY LAYER DESIGN
**LeaveRequestRepository**
```java
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeAndStatus(Employee employee, LeaveStatus status);
}
```

### 6. CONTROLLER/API DESIGN
**LeaveController**
```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/request")
    public ResponseEntity<LeaveRequestDTO> request(@Valid @RequestBody LeaveRequestCreateDTO dto);
    @PostMapping("/{id}/approve")
    public ResponseEntity<LeaveRequestDTO> approve(@PathVariable Long id);
    @PostMapping("/{id}/deny")
    public ResponseEntity<LeaveRequestDTO> deny(@PathVariable Long id);
    @GetMapping("/balance")
    public LeaveBalanceDTO getBalance(@RequestParam Long employeeId);
}
```

### 7. SECURITY CONFIGURATION
- Employees request; supervisors approve/deny.

### 8. INTEGRATION POINTS
- Scheduling and payroll modules.

### 9. CONFIGURATION PROPERTIES
```yaml
leave:
  accrual:
    pto-per-year: 15
    sick-per-year: 10
```

### 10. CODE SAMPLES
**Accrual Policy Strategy**
```java
public interface LeaveAccrualPolicy {
    int calculatePto(Employee employee);
}
```
**Auto-Flag Scheduled Shifts**
```java
public void flagShiftsForLeave(Employee employee, LocalDate start, LocalDate end) {
    List<Shift> shifts = shiftRepository.findByAssignedEmployeesAndDateRange(employee, start, end);
    for (Shift shift : shifts) {
        shift.setFlaggedForCoverage(true);
        shiftRepository.save(shift);
    }
}
```

---

## <a name="e07-training--certification-tracking"></a>E07: Training & Certification Tracking

### 1. EPIC OVERVIEW
Track required certifications (e.g., forklift), expirations, renewals; block assignment to tasks requiring expired certs; upload proof documents.

### 2. DOMAIN MODEL DESIGN
**Certification Entity**
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
    private String proofDocumentUrl;
}
```

### 3. PACKAGE STRUCTURE
```
com.warehousemgmt.certification
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### 4. SERVICE LAYER DESIGN
**CertificationService**
```java
public interface CertificationService {
    CertificationDTO createCertification(CertificationCreateDTO dto);
    CertificationDTO renewCertification(Long id, CertificationRenewDTO dto);
    List<CertificationDTO> getExpiringCertifications(int days);
}
```

### 5. REPOSITORY LAYER DESIGN
**CertificationRepository**
```java
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByExpiryDateBetween(LocalDate start, LocalDate end);
}
```

### 6. CONTROLLER/API DESIGN
**CertificationController**
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<CertificationDTO> create(@Valid @RequestBody CertificationCreateDTO dto);
    @PostMapping("/{id}/renew")
    public ResponseEntity<CertificationDTO> renew(@PathVariable Long id, @Valid @RequestBody CertificationRenewDTO dto);
    @GetMapping("/expiring")
    public List<CertificationDTO> getExpiring(@RequestParam int days);
}
```

### 7. SECURITY CONFIGURATION
- Only supervisors/admins can create/renew.

### 8. INTEGRATION POINTS
- Document storage service.

### 9. CONFIGURATION PROPERTIES
```yaml
certification:
  alert-days:
    - 30
    - 7
```

### 10. CODE SAMPLES
**Qualification Check Strategy**
```java
public interface QualificationChecker {
    boolean isQualified(Employee employee, String certificationType);
}
```
**Alert Generation**
```java
public List<CertificationDTO> getExpiringCertifications(int days) {
    LocalDate now = LocalDate.now();
    LocalDate threshold = now.plusDays(days);
    return certificationRepository.findByExpiryDateBetween(now, threshold);
}
```

---

## <a name="e08-safety-incidents--osha-reporting"></a>E08: Safety Incidents & OSHA Reporting

### 1. EPIC OVERVIEW
Record incidents/near-misses; severity, location, description, involved employees; workflow for investigation and corrective actions; generate OSHA summary.

### 2. DOMAIN MODEL DESIGN
**SafetyIncident Entity**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String description;
    private String location;
    private IncidentSeverity severity;
    @ManyToMany
    private List<Employee> involvedEmployees;
    private IncidentStatus status; // OPEN, INVESTIGATING, RESOLVED
    private LocalDateTime reportedAt;
}
```

### 3. PACKAGE STRUCTURE
```
com.warehousemgmt.safety
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### 4. SERVICE LAYER DESIGN
**SafetyIncidentService**
```java
public interface SafetyIncidentService {
    SafetyIncidentDTO reportIncident(SafetyIncidentCreateDTO dto);
    SafetyIncidentDTO updateStatus(Long id, IncidentStatus status);
    OSHAReportDTO generateOSHAReport(LocalDate start, LocalDate end);
}
```

### 5. REPOSITORY LAYER DESIGN
**SafetyIncidentRepository**
```java
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
    List<SafetyIncident> findByReportedAtBetween(LocalDateTime start, LocalDateTime end);
}
```

### 6. CONTROLLER/API DESIGN
**SafetyIncidentController**
```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
    @PostMapping
    public ResponseEntity<SafetyIncidentDTO> report(@Valid @RequestBody SafetyIncidentCreateDTO dto);
    @PatchMapping("/{id}/status")
    public ResponseEntity<SafetyIncidentDTO> updateStatus(@PathVariable Long id, @RequestBody IncidentStatus status);
    @GetMapping("/osha-report")
    public OSHAReportDTO generateReport(@RequestParam LocalDate start, @RequestParam LocalDate end);
}
```

### 7. SECURITY CONFIGURATION
- Only supervisors/admins can update status.

### 8. INTEGRATION POINTS
- OSHA reporting API.

### 9. CONFIGURATION PROPERTIES
```yaml
safety:
  osha:
    report-fields:
      - incidentId
      - severity
      - location
      - involvedEmployees
```

### 10. CODE SAMPLES
**Investigation Workflow**
```java
public void updateStatus(Long id, IncidentStatus status) {
    SafetyIncident incident = safetyIncidentRepository.findById(id).orElseThrow(...);
    incident.setStatus(status);
    safetyIncidentRepository.save(incident);
}
```
**OSHA Report Generation**
```java
public OSHAReportDTO generateOSHAReport(LocalDate start, LocalDate end) {
    List<SafetyIncident> incidents = safetyIncidentRepository.findByReportedAtBetween(start.atStartOfDay(), end.atTime(23,59));
    // Map to OSHA fields
}
```

---

## <a name="e09-equipment--asset-assignment"></a>E09: Equipment & Asset Assignment

### 1. EPIC OVERVIEW
Assign scanners, forklifts, and PPE to employees; track checkout/return; prevent use if certification missing; maintain asset condition state.

### 2. DOMAIN MODEL DESIGN
**Asset Entity**
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private AssetCondition condition;
    @ManyToOne
    private Employee assignedTo;
    private LocalDateTime checkedOutAt;
    private LocalDateTime returnedAt;
}
```

### 3. PACKAGE STRUCTURE
```
com.warehousemgmt.asset
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### 4. SERVICE LAYER DESIGN
**AssetService**
```java
public interface AssetService {
    AssetDTO checkoutAsset(Long assetId, Long employeeId);
    AssetDTO returnAsset(Long assetId);
    List<AssetDTO> getAssetsByEmployee(Long employeeId);
}
```

### 5. REPOSITORY LAYER DESIGN
**AssetRepository**
```java
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByAssignedTo(Employee employee);
}
```

### 6. CONTROLLER/API DESIGN
**AssetController**
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/{id}/checkout")
    public ResponseEntity<AssetDTO> checkout(@PathVariable Long id, @RequestParam Long employeeId);
    @PostMapping("/{id}/return")
    public ResponseEntity<AssetDTO> returnAsset(@PathVariable Long id);
    @GetMapping("/by-employee")
    public List<AssetDTO> getByEmployee(@RequestParam Long employeeId);
}
```

### 7. SECURITY CONFIGURATION
- Certification validation before checkout.

### 8. INTEGRATION POINTS
- Certification module.

### 9. CONFIGURATION PROPERTIES
```yaml
asset:
  condition:
    allowed: [GOOD, NEEDS_REPAIR, DAMAGED]
```

### 10. CODE SAMPLES
**Certification Validation**
```java
public void checkoutAsset(Long assetId, Long employeeId) {
    Employee employee = employeeRepository.findById(employeeId).orElseThrow(...);
    Asset asset = assetRepository.findById(assetId).orElseThrow(...);
    if (!qualificationChecker.isQualified(employee, asset.getType())) {
        throw new ForbiddenException("Certification required");
    }
    asset.setAssignedTo(employee);
    asset.setCheckedOutAt(LocalDateTime.now());
    assetRepository.save(asset);
}
```

---

## <a name="e10-performance-reviews--goals"></a>E10: Performance Reviews & Goals

### 1. EPIC OVERVIEW
Create quarterly/annual review templates; track goals, competencies, ratings, comments; supervisor/employee acknowledgements.

### 2. DOMAIN MODEL DESIGN
**PerformanceReview Entity**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate reviewDate;
    private String templateName;
    private String goals;
    private String competencies;
    private Integer rating;
    private String comments;
    private Boolean acknowledgedByEmployee;
    private Boolean acknowledgedBySupervisor;
    private Boolean immutableAfterSignoff;
}
```

### 3. PACKAGE STRUCTURE
```
com.warehousemgmt.review
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### 4. SERVICE LAYER DESIGN
**PerformanceReviewService**
```java
public interface PerformanceReviewService {
    PerformanceReviewDTO createReview(PerformanceReviewCreateDTO dto);
    PerformanceReviewDTO acknowledgeReview(Long reviewId, Boolean byEmployee);
    List<PerformanceReviewDTO> getReviews(Long employeeId);
}
```

### 5. REPOSITORY LAYER DESIGN
**PerformanceReviewRepository**
```java
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    List<PerformanceReview> findByEmployee(Employee employee);
}
```

### 6. CONTROLLER/API DESIGN
**PerformanceReviewController**
```java
@RestController
@RequestMapping("/reviews")
public class PerformanceReviewController {
    @PostMapping
    public ResponseEntity<PerformanceReviewDTO> create(@Valid @RequestBody PerformanceReviewCreateDTO dto);
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<PerformanceReviewDTO> acknowledge(@PathVariable Long id, @RequestParam Boolean byEmployee);
    @GetMapping("/by-employee")
    public List<PerformanceReviewDTO> getByEmployee(@RequestParam Long employeeId);
}
```

### 7. SECURITY CONFIGURATION
- Only supervisors/admins can create; employees can acknowledge.

### 8. INTEGRATION POINTS
- PDF export service.

### 9. CONFIGURATION PROPERTIES
```yaml
review:
  templates:
    quarterly: "Quarterly Review"
    annual: "Annual Review"
```

### 10. CODE SAMPLES
**Immutable History After Signoff**
```java
public void acknowledgeReview(Long reviewId, Boolean byEmployee) {
    PerformanceReview review = performanceReviewRepository.findById(reviewId).orElseThrow(...);
    if (review.getImmutableAfterSignoff()) throw new IllegalStateException("Review is immutable");
    if (byEmployee) review.setAcknowledgedByEmployee(true);
    else review.setAcknowledgedBySupervisor(true);
    if (review.getAcknowledgedByEmployee() && review.getAcknowledgedBySupervisor()) {
        review.setImmutableAfterSignoff(true);
    }
    performanceReviewRepository.save(review);
}
```

---

## <a name="e11-payroll-export-integration"></a>E11: Payroll Export Integration

### 1. EPIC OVERVIEW
Generate payroll-ready files from approved attendance and leave; mapping to external payroll provider formats; secure delivery (SFTP/API).

### 2. DOMAIN MODEL DESIGN
**PayrollExport Entity**
```java
@Entity
public class PayrollExport {
    @Id @GeneratedValue
    private Long id;
    private LocalDate exportDate;
    private String provider;
    private String fileUrl;
    private Boolean delivered;
    private String deliveryMethod; // SFTP, API
    private String reconciliationStatus;
}
```

### 3. PACKAGE STRUCTURE
```
com.warehousemgmt.payroll
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### 4. SERVICE LAYER DESIGN
**PayrollExportService**
```java
public interface PayrollExportService {
    PayrollExportDTO generateExport(PayrollExportCreateDTO dto);
    PayrollExportDTO deliverExport(Long exportId);
    PayrollExportDTO reconcileExport(Long exportId);
}
```

### 5. REPOSITORY LAYER DESIGN
**PayrollExportRepository**
```java
public interface PayrollExportRepository extends JpaRepository<PayrollExport, Long> {
    List<PayrollExport> findByProviderAndExportDate(String provider, LocalDate date);
}
```

### 6. CONTROLLER/API DESIGN
**PayrollExportController**
```java
@RestController
@RequestMapping("/payroll/exports")
public class PayrollExportController {
    @PostMapping
    public ResponseEntity<PayrollExportDTO> generate(@Valid @RequestBody PayrollExportCreateDTO dto);
    @PostMapping("/{id}/deliver")
    public ResponseEntity<PayrollExportDTO> deliver(@PathVariable Long id);
    @PostMapping("/{id}/reconcile")
    public ResponseEntity<PayrollExportDTO> reconcile(@PathVariable Long id);
}
```

### 7. SECURITY CONFIGURATION
- Only payroll admins can export/deliver.

### 8. INTEGRATION POINTS
- SFTP, payroll provider API.

### 9. CONFIGURATION PROPERTIES
```yaml
payroll:
  provider:
    default: "ADP"
  delivery:
    method: "SFTP"
```

### 10. CODE SAMPLES
**Delivery with Backoff**
```java
public void deliverExport(Long exportId) {
    PayrollExport export = payrollExportRepository.findById(exportId).orElseThrow(...);
    try {
        payrollProviderApi.deliver(export.getFileUrl());
        export.setDelivered(true);
    } catch (Exception e) {
        // Retry with exponential backoff
    }
    payrollExportRepository.save(export);
}
```

---

## <a name="e12-notifications--announcements"></a>E12: Notifications & Announcements

### 1. EPIC OVERVIEW
In-app and email/SMS notifications for shift changes, expiring certs, approvals, announcements; quiet hours configuration.

### 2. DOMAIN MODEL DESIGN
**Notification Entity**
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee recipient;
    private String channel; // IN_APP, EMAIL, SMS
    private String message;
    private LocalDateTime sentAt;
    private Boolean delivered;
    private Boolean read;
}
```
**Announcement Entity**
```java
@Entity
public class Announcement {
    @Id @GeneratedValue
    private Long id;
    private String title;
    private String content;
    private LocalDateTime postedAt;
    private Boolean visible;
}
```

### 3. PACKAGE STRUCTURE
```
com.warehousemgmt.notification
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### 4. SERVICE LAYER DESIGN
**NotificationService**
```java
public interface NotificationService {
    NotificationDTO sendNotification(NotificationCreateDTO dto);
    List<NotificationDTO> getNotifications(Long employeeId);
}
```
**AnnouncementService**
```java
public interface AnnouncementService {
    AnnouncementDTO postAnnouncement(AnnouncementCreateDTO dto);
    List<AnnouncementDTO> getAnnouncements();
}
```

### 5. REPOSITORY LAYER DESIGN
**NotificationRepository**
```java
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipient(Employee employee);
}
```

### 6. CONTROLLER/API DESIGN
**NotificationController**
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping
    public ResponseEntity<NotificationDTO> send(@Valid @RequestBody NotificationCreateDTO dto);
    @GetMapping("/by-employee")
    public List<NotificationDTO> getByEmployee(@RequestParam Long employeeId);
}
```
**AnnouncementController**
```java
@RestController
@RequestMapping("/announcements")
public class AnnouncementController {
    @PostMapping
    public ResponseEntity<AnnouncementDTO> post(@Valid @RequestBody AnnouncementCreateDTO dto);
    @GetMapping
    public List<AnnouncementDTO> getAll();
}
```

### 7. SECURITY CONFIGURATION
- Rate limits per user/channel.

### 8. INTEGRATION POINTS
- Email/SMS gateway.

### 9. CONFIGURATION PROPERTIES
```yaml
notification:
  channels:
    - IN_APP
    - EMAIL
    - SMS
  quiet-hours:
    start: "22:00"
    end: "06:00"
```

### 10. CODE SAMPLES
**Quiet Hours Check**
```java
public boolean isWithinQuietHours(LocalDateTime now) {
    LocalTime start = LocalTime.parse(config.getQuietHoursStart());
    LocalTime end = LocalTime.parse(config.getQuietHoursEnd());
    LocalTime current = now.toLocalTime();
    return current.isAfter(start) || current.isBefore(end);
}
```

---

## <a name="e13-integration-layer-hriswms-apis"></a>E13: Integration Layer (HRIS/WMS APIs)

### 1. EPIC OVERVIEW
Expose REST APIs and connectors for HRIS (new hires/terms), WMS (location/department), and IDP for SSO; webhooks for events.

### 2. DOMAIN MODEL DESIGN
- IntegrationEvent, HRISSyncJob, WMSConnector

### 3. PACKAGE STRUCTURE
```
com.warehousemgmt.integration
âââ controller
âââ service
âââ model
âââ webhook
```

### 4. SERVICE LAYER DESIGN
**IntegrationService**
```java
public interface IntegrationService {
    void syncHRIS();
    void syncWMS();
    void handleWebhook(IntegrationEventDTO dto);
}
```

### 5. REPOSITORY LAYER DESIGN
- None; stateless connectors.

### 6. CONTROLLER/API DESIGN
**IntegrationController**
```java
@RestController
@RequestMapping("/integration")
public class IntegrationController {
    @PostMapping("/hris/sync")
    public ResponseEntity<Void> syncHRIS();
    @PostMapping("/wms/sync")
    public ResponseEntity<Void> syncWMS();
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody IntegrationEventDTO dto);
}
```

### 7. SECURITY CONFIGURATION
- JWT/OAuth2 for APIs.

### 8. INTEGRATION POINTS
- HRIS, WMS, IDP.

### 9. CONFIGURATION PROPERTIES
```yaml
integration:
  hris:
    api-url: "https://hris.example.com/api"
    token: "..."
  wms:
    api-url: "https://wms.example.com/api"
    token: "..."
```

### 10. CODE SAMPLES
**Webhook Handler**
```java
@PostMapping("/webhook")
public ResponseEntity<Void> handleWebhook(@RequestBody IntegrationEventDTO dto) {
    // Idempotency check
    // Process event
    return ResponseEntity.ok().build();
}
```

---

## <a name="e14-audit-trail--compliance"></a>E14: Audit Trail & Compliance

### 1. EPIC OVERVIEW
Centralized audit logging for sensitive changes (employee PII, schedules, approvals, payroll); tamper-evident storage.

### 2. DOMAIN MODEL DESIGN
**AuditLog Entity**
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
    private String before;
    private String after;
    private Boolean immutable;
}
```

### 3. PACKAGE STRUCTURE
```
com.warehousemgmt.audit
âââ service
âââ repository
âââ model
```

### 4. SERVICE LAYER DESIGN
**AuditService**
```java
public interface AuditService {
    void logChange(String entityType, Long entityId, String actor, String action, String before, String after);
    List<AuditLogDTO> exportLogs(LocalDate start, LocalDate end, String entityType);
}
```

### 5. REPOSITORY LAYER DESIGN
**AuditLogRepository**
```java
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByTimestampBetweenAndEntityType(LocalDateTime start, LocalDateTime end, String entityType);
}
```

### 6. CONTROLLER/API DESIGN
**AuditController**
```java
@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/export")
    public List<AuditLogDTO> export(@RequestParam LocalDate start, @RequestParam LocalDate end, @RequestParam String entityType);
}
```

### 7. SECURITY CONFIGURATION
- Only compliance/audit roles can export.

### 8. INTEGRATION POINTS
- Tamper-evident storage (e.g., hash chain).

### 9. CONFIGURATION PROPERTIES
```yaml
audit:
  immutable: true
```

### 10. CODE SAMPLES
**Tamper-Evident Storage**
```java
public void logChange(...) {
    // Hash previous log, chain to current
}
```

---

## <a name="e15-reporting--analytics"></a>E15: Reporting & Analytics

### 1. EPIC OVERVIEW
Operational reports: attendance, overtime, leave balances, certification status, safety KPIs; export CSV/PDF; basic role-based dashboards.

### 2. DOMAIN MODEL DESIGN
- ReportRequest, ReportResult

### 3. PACKAGE STRUCTURE
```
com.warehousemgmt.reporting
âââ controller
âââ service
âââ model
âââ dto
```

### 4. SERVICE LAYER DESIGN
**ReportingService**
```java
public interface ReportingService {
    ReportResultDTO generateReport(ReportRequestDTO dto);
    byte[] exportReport(ReportResultDTO dto, String format);
}
```

### 5. REPOSITORY LAYER DESIGN
- Uses other modules' repositories.

### 6. CONTROLLER/API DESIGN
**ReportingController**
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @PostMapping("/generate")
    public ReportResultDTO generate(@Valid @RequestBody ReportRequestDTO dto);
    @PostMapping("/export")
    public ResponseEntity<byte[]> export(@RequestBody ReportResultDTO dto, @RequestParam String format);
}
```

### 7. SECURITY CONFIGURATION
- Role-based access to reports.

### 8. INTEGRATION POINTS
- BI tools via metrics API.

### 9. CONFIGURATION PROPERTIES
```yaml
reporting:
  export:
    formats: [CSV, PDF]
```

### 10. CODE SAMPLES
**Metrics Endpoint**
```java
@GetMapping("/metrics")
public MetricsDTO getMetrics(@RequestParam String type);
```

---

## <a name="e16-mobile-access-pwa"></a>E16: Mobile Access (PWA)

### 1. EPIC OVERVIEW
Responsive views for workers to clock-in/out, view schedules, request leave, see announcements; offline-friendly via PWA.

### 2. DOMAIN MODEL DESIGN
- No new entities; uses existing modules.

### 3. PACKAGE STRUCTURE
```
com.warehousemgmt.pwa
âââ controller
âââ service
```

### 4. SERVICE LAYER DESIGN
- Offline queue for clock events.

### 5. REPOSITORY LAYER DESIGN
- None.

### 6. CONTROLLER/API DESIGN
**PWAController**
```java
@RestController
@RequestMapping("/pwa")
public class PWAController {
    @GetMapping("/manifest.json")
    public ManifestDTO getManifest();
    @PostMapping("/offline-events")
    public ResponseEntity<Void> syncOfflineEvents(@RequestBody List<ClockEventDTO> events);
}
```

### 7. SECURITY CONFIGURATION
- JWT for mobile endpoints.

### 8. INTEGRATION POINTS
- Lighthouse for PWA score.

### 9. CONFIGURATION PROPERTIES
```yaml
pwa:
  offline:
    enabled: true
```

### 10. CODE SAMPLES
**Offline Event Sync**
```java
@PostMapping("/offline-events")
public ResponseEntity<Void> syncOfflineEvents(@RequestBody List<ClockEventDTO> events) {
    // Resolve conflicts, persist events
}
```

---

## <a name="e17-onboarding--offboarding-workflow"></a>E17: Onboarding & Offboarding Workflow

### 1. EPIC OVERVIEW
Automate provisioning of accounts, initial schedule, required training; deprovision access and assets on termination.

### 2. DOMAIN MODEL DESIGN
**OnboardingTask Entity**
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String taskType;
    private Boolean completed;
}
```

### 3. PACKAGE STRUCTURE
```
com.warehousemgmt.onboarding
âââ controller
âââ service
âââ repository
âââ model
```

### 4. SERVICE LAYER DESIGN
**OnboardingService**
```java
public interface OnboardingService {
    void provisionEmployee(Long employeeId);
    void deprovisionEmployee(Long employeeId);
}
```

### 5. REPOSITORY LAYER DESIGN
**OnboardingTaskRepository**
```java
public interface OnboardingTaskRepository extends JpaRepository<OnboardingTask, Long> {
    List<OnboardingTask> findByEmployee(Employee employee);
}
```

### 6. CONTROLLER/API DESIGN
**OnboardingController**
```java
@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @PostMapping("/provision")
    public ResponseEntity<Void> provision(@RequestParam Long employeeId);
    @PostMapping("/deprovision")
    public ResponseEntity<Void> deprovision(@RequestParam Long employeeId);
}
```

### 7. SECURITY CONFIGURATION
- Only HR/admins can provision/deprovision.

### 8. INTEGRATION POINTS
- HRIS, asset, training modules.

### 9. CONFIGURATION PROPERTIES
```yaml
onboarding:
  tasks:
    - "Create Account"
    - "Assign Initial Schedule"
    - "Assign Training"
    - "Assign Assets"
```

### 10. CODE SAMPLES
**Provisioning Workflow**
```java
public void provisionEmployee(Long employeeId) {
    // Create onboarding tasks, assign schedule, training, assets
}
```

---

## <a name="e18-localization--multi-tenant"></a>E18: Localization & Multi-Tenant

### 1. EPIC OVERVIEW
Locale support, tenant isolation.

### 2. DOMAIN MODEL DESIGN
**Tenant Entity**
```java
@Entity
public class Tenant {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String locale;
}
```

### 3. PACKAGE STRUCTURE
```
com.warehousemgmt.tenant
âââ controller
âââ service
âââ repository
âââ model
```

### 4. SERVICE LAYER DESIGN
**TenantService**
```java
public interface TenantService {
    TenantDTO createTenant(TenantCreateDTO dto);
    void setLocale(Long tenantId, String locale);
}
```

### 5. REPOSITORY LAYER DESIGN
**TenantRepository**
```java
public interface TenantRepository extends JpaRepository<Tenant, Long> {}
```

### 6. CONTROLLER/API DESIGN
**TenantController**
```java
@RestController
@RequestMapping("/tenants")
public class TenantController {
    @PostMapping
    public ResponseEntity<TenantDTO> create(@Valid @RequestBody TenantCreateDTO dto);
    @PostMapping("/{id}/locale")
    public ResponseEntity<Void> setLocale(@PathVariable Long id, @RequestParam String locale);
}
```

### 7. SECURITY CONFIGURATION
- Tenant isolation via filters/interceptors.

### 8. INTEGRATION POINTS
- Locale resolver.

### 9. CONFIGURATION PROPERTIES
```yaml
tenant:
  default-locale: "en_US"
```

### 10. CODE SAMPLES
**Locale Resolver**
```java
public class TenantLocaleResolver implements LocaleResolver { ... }
```

---

## <a name="e19-observability--monitoring"></a>E19: Observability & Monitoring

### 1. EPIC OVERVIEW
Structured logging, tracing, metrics, alerting.

### 2. DOMAIN MODEL DESIGN
- LogEvent, TraceSpan

### 3. PACKAGE STRUCTURE
```
com.warehousemgmt.observability
âââ config
âââ service
```

### 4. SERVICE LAYER DESIGN
- LoggingService, TracingService

### 5. REPOSITORY LAYER DESIGN
- None; external systems.

### 6. CONTROLLER/API DESIGN
- Expose metrics via `/actuator/metrics`

### 7. SECURITY CONFIGURATION
- Metrics endpoint restricted to admins.

### 8. INTEGRATION POINTS
- Prometheus, Grafana, ELK.

### 9. CONFIGURATION PROPERTIES
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### 10. CODE SAMPLES
**Structured Logging**
```java
@Slf4j
public class LoggingService {
    public void logEvent(String event, Map<String, Object> details) {
        log.info("event={} details={}", event, details);
    }
}
```

---

## <a name="e20-cicd--deployment"></a>E20: CI/CD & Deployment

### 1. EPIC OVERVIEW
Automated pipeline, security scanning, Docker deployment.

### 2. DOMAIN MODEL DESIGN
- No entities; infrastructure only.

### 3. PACKAGE STRUCTURE
- None.

### 4. SERVICE LAYER DESIGN
- None.

### 5. REPOSITORY LAYER DESIGN
- None.

### 6. CONTROLLER/API DESIGN
- None.

### 7. SECURITY CONFIGURATION
- Pipeline secrets management.

### 8. INTEGRATION POINTS
- Jenkins/GitHub Actions, Docker, security scanners.

### 9. CONFIGURATION PROPERTIES
**Dockerfile**
```dockerfile
FROM eclipse-temurin:17-jre
COPY target/warehouse-employee-mgmt.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```
**CI Pipeline (GitHub Actions)**
```yaml
name: CI/CD Pipeline
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
      - name: Run Tests
        run: mvn test
      - name: Build Docker Image
        run: docker build -t warehouse-employee-mgmt .
      - name: Security Scan
        run: docker scan warehouse-employee-mgmt
      - name: Push to Registry
        run: docker push ...
```

### 10. CODE SAMPLES
- See above for Dockerfile and pipeline.

---

# Cross-Cutting Concerns

## Exception Handling
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleNotFound(EntityNotFoundException ex) { ... }
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorDTO> handleValidation(ValidationException ex) { ... }
}
```

## Validation
- Use `@Valid` on DTOs.
- Custom validators for business rules.

## Logging
- Use SLF4J and structured logs.

## Builder/Factory/Strategy Patterns
- Use Builder for DTOs/entities.
- Strategy for validation, qualification, accrual policies.

---

# Database Schema & Migration

- Use Flyway for versioned migrations.
- Baseline migration creates all core tables.
- Subsequent migrations for new features.

---

# Summary

This document provides a complete technical blueprint for implementing all 85 user stories across 20 epics in the Warehouse Employee Management System, following Spring Boot 3.x best practices, layered architecture, and production-grade standards. All modules are designed for extensibility, security, and maintainability.