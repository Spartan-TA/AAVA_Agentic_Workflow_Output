# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

## Table of Contents
- [E01 - Project Scaffolding & Domain Setup](#e01---project-scaffolding--domain-setup)
- [E02 - Employee Master Data (CRUD)](#e02---employee-master-data-crud)
- [E03 - Role Based Access Control (RBAC)](#e03---role-based-access-control-rbac)
- [E04 - Time & Attendance (Clock In/Out)](#e04---time--attendance-clock-inout)
- [E05 - Shift & Schedule Management](#e05---shift--schedule-management)
- [E06 - Leave & Absence Management](#e06---leave--absence-management)
- [E07 - Training & Certification Tracking](#e07---training--certification-tracking)
- [E08 - Safety Incidents & OSHA Reporting](#e08---safety-incidents--osha-reporting)
- [E09 - Equipment & Asset Assignment](#e09---equipment--asset-assignment)
- [E10 - Performance Reviews & Goals](#e10---performance-reviews--goals)
- [E11 - Payroll Export Integration](#e11---payroll-export-integration)
- [E12 - Notifications & Announcements](#e12---notifications--announcements)
- [E13 - Integration Layer (HRIS/WMS APIs)](#e13---integration-layer-hriswms-apis)
- [E14 - Audit Trail & Compliance](#e14---audit-trail--compliance)
- [E15 - Reporting & Analytics](#e15---reporting--analytics)
- [E16 - Mobile Access (PWA)](#e16---mobile-access-pwa)
- [E17 - Onboarding & Offboarding Workflow](#e17---onboarding--offboarding-workflow)
- [E18 - Localization & Multi-Warehouse](#e18---localization--multi-warehouse)
- [E19 - Advanced Scheduling (AI/Optimization)](#e19---advanced-scheduling-aioptimization)
- [E20 - Self-Service Portal & Preferences](#e20---self-service-portal--preferences)

---

## E01 - Project Scaffolding & Domain Setup

### Spring Boot Architecture Overview
- Modular monolith using Maven.
- Core modules: employee, scheduling, attendance, safety.
- Layered architecture: Controller, Service, Repository, Domain.
- Flyway/Liquibase for DB migrations.
- Spring Boot Actuator for monitoring.

### Package Structure
- `com.warehouse.ems`
  - `employee`
  - `scheduling`
  - `attendance`
  - `safety`
  - `config`
  - `common`
  - `audit`
  - `integration`

### Entity Design
- No entities in this epic; domain setup only.

### Repository Layer
- No repositories in this epic.

### Service Layer
- No services in this epic.

### Controller Layer
- No controllers in this epic.

### Security Configuration
- No security in this epic.

### Configuration
- `application.properties`:
  ```
  server.port=8080
  spring.datasource.url=jdbc:postgresql://localhost:5432/ems
  spring.datasource.username=ems_user
  spring.datasource.password=secret
  spring.flyway.enabled=true
  management.endpoints.web.exposure.include=health,info
  ```

### Integration Points
- Flyway/Liquibase for DB migrations.
- Actuator for health monitoring.

### Code Samples
#### Maven POM Module Example
```xml
<modules>
  <module>employee</module>
  <module>scheduling</module>
  <module>attendance</module>
  <module>safety</module>
</modules>
```
#### Actuator Health Endpoint
```bash
curl http://localhost:8080/actuator/health
# Response: {"status":"UP"}
```
#### Flyway Baseline Migration Example
```sql
-- V1__baseline.sql
CREATE TABLE employee (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);
```

---

## E02 - Employee Master Data (CRUD)

### Spring Boot Architecture Overview
- Employee domain as a core module.
- RESTful CRUD APIs for employee management.
- DTOs for web layer.

### Package Structure
- `com.warehouse.ems.employee.domain`
- `com.warehouse.ems.employee.repository`
- `com.warehouse.ems.employee.service`
- `com.warehouse.ems.employee.web`

### Entity Design
#### Employee Entity
```java
@Entity
@Table(name = "employee", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
@SQLDelete(sql = "UPDATE employee SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
}
```

### Repository Layer
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDepartment(String department, Pageable pageable);
}
```

### Service Layer
```java
public interface EmployeeService {
    Employee create(EmployeeDto dto);
    Employee update(Long id, EmployeeDto dto);
    void softDelete(Long id);
    Page<Employee> list(Pageable pageable, EmployeeFilter filter);
}
```
```java
@Service
public class EmployeeServiceImpl implements EmployeeService {
    // Implementation with business logic and validation
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { ... }

    @GetMapping
    public Page<EmployeeDto> list(@RequestParam Map<String, String> filters, Pageable pageable) { ... }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) { ... }

    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeDto> partialUpdate(@PathVariable Long id, @RequestBody Map<String, Object> updates) { ... }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
}
```

### Security Configuration
- Secured endpoints via RBAC (see E03).

### Configuration
- OpenAPI/Swagger enabled:
  ```
  springdoc.api-docs.enabled=true
  springdoc.swagger-ui.enabled=true
  ```

### Integration Points
- Used by HRIS integration (E13).
- Employee data referenced by other modules.

### Code Samples
#### Employee DTO Example
```java
public class EmployeeDto {
    private Long id;
    private String name;
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}
```
#### OpenAPI Schema Example
```yaml
Employee:
  type: object
  properties:
    id: { type: integer }
    name: { type: string }
    badgeId: { type: string }
    role: { type: string }
    department: { type: string }
    shiftGroup: { type: string }
    hireDate: { type: string, format: date }
    status: { type: string }
```

---

## E03 - Role Based Access Control (RBAC)

### Spring Boot Architecture Overview
- Spring Security for authentication and authorization.
- Role-based endpoint and method security.

### Package Structure
- `com.warehouse.ems.security`
- `com.warehouse.ems.config`

### Entity Design
#### Role Enum
```java
public enum Role {
    ADMIN, HR, SUPERVISOR, WORKER
}
```

### Repository Layer
- No repository changes; roles stored in employee entity.

### Service Layer
- Security service for user details and role checks.

### Controller Layer
- Endpoint security via annotations.

### Security Configuration
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
            .httpBasic()
            .and()
            .csrf().disable();
    }
}
```
- API key/OAuth2 toggle via config:
  ```
  security.auth.type=oauth2 # or apikey
  ```

### Configuration
- Security properties in `application.properties`.

### Integration Points
- Used by all modules for access control.

### Code Samples
#### Method Security Example
```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteEmployee(Long id) { ... }
```
#### Unauthorized/Forbidden Response Example
```json
// 401 Unauthorized
{
  "error": "Unauthorized"
}

// 403 Forbidden
{
  "error": "Forbidden"
}
```

---

## E04 - Time & Attendance (Clock In/Out)

### Spring Boot Architecture Overview
- Attendance module for clock-in/out events.
- REST endpoints for event capture and reporting.

### Package Structure
- `com.warehouse.ems.attendance.domain`
- `com.warehouse.ems.attendance.repository`
- `com.warehouse.ems.attendance.service`
- `com.warehouse.ems.attendance.web`

### Entity Design
#### AttendanceEvent Entity
```java
@Entity
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private EventType eventType; // CLOCK_IN, CLOCK_OUT

    private LocalDateTime timestamp;

    private String deviceId;

    private String geoLocation;

    private boolean correctionRequested = false;
}
```

### Repository Layer
```java
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeAndDate(Employee employee, LocalDate date);
}
```

### Service Layer
```java
public interface AttendanceService {
    AttendanceEvent clockIn(Long employeeId, ClockEventDto dto);
    AttendanceEvent clockOut(Long employeeId, ClockEventDto dto);
    List<AttendanceEvent> getDailyEvents(Long employeeId, LocalDate date);
    void requestCorrection(Long eventId, CorrectionDto dto);
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceEventDto> clockIn(@RequestBody ClockEventDto dto) { ... }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceEventDto> clockOut(@RequestBody ClockEventDto dto) { ... }

    @PostMapping("/corrections/{eventId}")
    public ResponseEntity<Void> requestCorrection(@PathVariable Long eventId, @RequestBody CorrectionDto dto) { ... }
}
```

### Security Configuration
- Only authenticated users can clock in/out.
- Supervisors approve corrections.

### Configuration
- Geofence enabled via config:
  ```
  attendance.geofence.enabled=true
  ```

### Integration Points
- Attendance data used for payroll (E11), reporting (E15).

### Code Samples
#### ClockEvent DTO
```java
public class ClockEventDto {
    private String deviceId;
    private String geoLocation;
}
```
#### Correction Workflow Example
```java
@PreAuthorize("hasRole('SUPERVISOR')")
public void approveCorrection(Long eventId) { ... }
```

---

## E05 - Shift & Schedule Management

### Spring Boot Architecture Overview
- Scheduling module for shift templates, assignments, and calendars.

### Package Structure
- `com.warehouse.ems.scheduling.domain`
- `com.warehouse.ems.scheduling.repository`
- `com.warehouse.ems.scheduling.service`
- `com.warehouse.ems.scheduling.web`

### Entity Design
#### ShiftTemplate Entity
```java
@Entity
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
#### ShiftAssignment Entity
```java
@Entity
public class ShiftAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    @ManyToOne
    private ShiftTemplate shiftTemplate;

    private LocalDate shiftDate;
    private boolean blackout;
}
```

### Repository Layer
```java
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> { }
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    List<ShiftAssignment> findByEmployeeAndShiftDateBetween(Employee employee, LocalDate start, LocalDate end);
}
```

### Service Layer
```java
public interface SchedulingService {
    ShiftTemplate createTemplate(ShiftTemplateDto dto);
    ShiftAssignment assignShift(Long employeeId, ShiftAssignmentDto dto);
    List<ShiftAssignment> getUpcomingShifts(Long employeeId);
    void bulkAssign(List<ShiftAssignmentDto> assignments);
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/scheduling")
public class SchedulingController {
    @PostMapping("/templates")
    public ResponseEntity<ShiftTemplateDto> createTemplate(@RequestBody ShiftTemplateDto dto) { ... }

    @PostMapping("/assignments")
    public ResponseEntity<Void> assignShift(@RequestBody ShiftAssignmentDto dto) { ... }

    @GetMapping("/shifts/upcoming")
    public List<ShiftAssignmentDto> getUpcomingShifts(@RequestParam Long employeeId) { ... }
}
```

### Security Configuration
- Supervisors can bulk-assign.
- Workers see only their shifts.

### Configuration
- Warehouse calendar config:
  ```
  scheduling.blackout.dates=2024-12-25,2024-01-01
  ```

### Integration Points
- Scheduling interacts with attendance, leave, and reporting.

### Code Samples
#### Bulk Assignment Example
```java
@PreAuthorize("hasRole('SUPERVISOR')")
public void bulkAssign(List<ShiftAssignmentDto> assignments) { ... }
```
#### Audit Entry Example
```java
public class AuditEntry {
    private String actor;
    private String action;
    private LocalDateTime timestamp;
    private String entity;
    private String before;
    private String after;
}
```

---

## E06 - Leave & Absence Management

### Spring Boot Architecture Overview
- Leave management module for PTO, sick, unpaid leave.

### Package Structure
- `com.warehouse.ems.leave.domain`
- `com.warehouse.ems.leave.repository`
- `com.warehouse.ems.leave.service`
- `com.warehouse.ems.leave.web`

### Entity Design
#### LeaveRequest Entity
```java
@Entity
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private LeaveType type; // PTO, SICK, UNPAID

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status; // REQUESTED, APPROVED, DENIED

    private int accrualBalance;
}
```

### Repository Layer
```java
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeAndStatus(Employee employee, LeaveStatus status);
}
```

### Service Layer
```java
public interface LeaveService {
    LeaveRequest requestLeave(Long employeeId, LeaveRequestDto dto);
    void approveLeave(Long requestId);
    void denyLeave(Long requestId);
    List<LeaveRequest> getEmployeeLeaves(Long employeeId);
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/request")
    public ResponseEntity<LeaveRequestDto> requestLeave(@RequestBody LeaveRequestDto dto) { ... }

    @PostMapping("/approve/{id}")
    public ResponseEntity<Void> approveLeave(@PathVariable Long id) { ... }

    @PostMapping("/deny/{id}")
    public ResponseEntity<Void> denyLeave(@PathVariable Long id) { ... }
}
```

### Security Configuration
- Employees request leave.
- Supervisors approve/deny.

### Configuration
- Leave accrual policies:
  ```
  leave.accrual.pto=1.5
  leave.accrual.sick=1.0
  ```

### Integration Points
- Leave data used by scheduling and payroll.

### Code Samples
#### LeaveRequest DTO
```java
public class LeaveRequestDto {
    private Long employeeId;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
}
```
#### Scheduled Shift Coverage Example
```java
public void flagShiftForCoverage(Long shiftId) { ... }
```

---

## E07 - Training & Certification Tracking

### Spring Boot Architecture Overview
- Certification module for tracking training, expirations, and renewals.

### Package Structure
- `com.warehouse.ems.certification.domain`
- `com.warehouse.ems.certification.repository`
- `com.warehouse.ems.certification.service`
- `com.warehouse.ems.certification.web`

### Entity Design
#### Certification Entity
```java
@Entity
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    private String type; // e.g., Forklift

    private LocalDate issueDate;
    private LocalDate expiryDate;

    private String proofDocumentUrl;
}
```

### Repository Layer
```java
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByEmployeeAndExpiryDateBefore(Employee employee, LocalDate date);
}
```

### Service Layer
```java
public interface CertificationService {
    Certification addCertification(Long employeeId, CertificationDto dto);
    void uploadProof(Long certId, MultipartFile file);
    List<Certification> getExpiringCertifications(int days);
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<CertificationDto> addCertification(@RequestBody CertificationDto dto) { ... }

    @PostMapping("/{id}/upload")
    public ResponseEntity<Void> uploadProof(@PathVariable Long id, @RequestParam MultipartFile file) { ... }

    @GetMapping("/expiring")
    public List<CertificationDto> getExpiring(@RequestParam int days) { ... }
}
```

### Security Configuration
- HR can add/update certifications.
- Employees can view their certifications.

### Configuration
- Alert thresholds:
  ```
  certification.alert.days=30,7
  ```

### Integration Points
- Certification checks in scheduling and asset assignment.

### Code Samples
#### Certification Check Example
```java
public boolean isCertificationValid(Long employeeId, String certType) {
    // Check expiry date
}
```
#### Alert Job Example
```java
@Scheduled(cron = "0 0 8 * * ?")
public void sendExpirationAlerts() { ... }
```

---

## E08 - Safety Incidents & OSHA Reporting

### Spring Boot Architecture Overview
- Safety module for incident tracking and OSHA reporting.

### Package Structure
- `com.warehouse.ems.safety.domain`
- `com.warehouse.ems.safety.repository`
- `com.warehouse.ems.safety.service`
- `com.warehouse.ems.safety.web`

### Entity Design
#### SafetyIncident Entity
```java
@Entity
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Severity severity; // MINOR, MODERATE, SEVERE, FATAL

    private String location;
    private String description;

    @ManyToMany
    private List<Employee> involvedEmployees;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status; // OPEN, INVESTIGATING, RESOLVED

    private LocalDateTime incidentDate;
}
```

### Repository Layer
```java
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
    List<SafetyIncident> findByStatus(IncidentStatus status);
}
```

### Service Layer
```java
public interface SafetyService {
    SafetyIncident recordIncident(SafetyIncidentDto dto);
    void updateStatus(Long incidentId, IncidentStatus status);
    byte[] generateOSHAReport(int year);
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/safety")
public class SafetyController {
    @PostMapping("/incidents")
    public ResponseEntity<SafetyIncidentDto> recordIncident(@RequestBody SafetyIncidentDto dto) { ... }

    @PutMapping("/incidents/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam String status) { ... }

    @GetMapping("/reports/osha300")
    public ResponseEntity<byte[]> generateOSHA300(@RequestParam int year) { ... }
}
```

### Security Configuration
- Supervisors record incidents.
- Safety officers manage investigations.

### Configuration
- OSHA report templates:
  ```
  safety.osha.template.path=/templates/osha300.pdf
  ```

### Integration Points
- Safety data used in reporting and analytics.

### Code Samples
#### OSHA Report Generation Example
```java
public byte[] generateOSHA300(int year) {
    // Generate PDF report
}
```
#### Incident Workflow Example
```java
public void investigateIncident(Long incidentId) {
    // Update status to INVESTIGATING
}
```

---

## E09 - Equipment & Asset Assignment

### Spring Boot Architecture Overview
- Asset management module for equipment tracking and assignment.

### Package Structure
- `com.warehouse.ems.asset.domain`
- `com.warehouse.ems.asset.repository`
- `com.warehouse.ems.asset.service`
- `com.warehouse.ems.asset.web`

### Entity Design
#### Asset Entity
```java
@Entity
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // Scanner, Forklift, PPE
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    private AssetCondition condition; // NEW, GOOD, FAIR, POOR, RETIRED

    @ManyToOne
    private Employee assignedTo;

    private LocalDateTime checkoutDate;
}
```

### Repository Layer
```java
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByAssignedTo(Employee employee);
}
```

### Service Layer
```java
public interface AssetService {
    Asset checkout(Long assetId, Long employeeId);
    void checkin(Long assetId);
    List<Asset> getOverdueAssets();
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/{id}/checkout")
    public ResponseEntity<AssetDto> checkout(@PathVariable Long id, @RequestParam Long employeeId) { ... }

    @PostMapping("/{id}/checkin")
    public ResponseEntity<Void> checkin(@PathVariable Long id) { ... }

    @GetMapping("/overdue")
    public List<AssetDto> getOverdue() { ... }
}
```

### Security Configuration
- Certification checks before checkout.

### Configuration
- Asset checkout rules:
  ```
  asset.checkout.maxDays=30
  ```

### Integration Points
- Asset assignment checks certifications (E07).

### Code Samples
#### Certification Check Example
```java
public void checkout(Long assetId, Long employeeId) {
    if (!isCertificationValid(employeeId, asset.getType())) {
        throw new ForbiddenException("Certification required");
    }
}
```
#### Asset History Example
```java
public List<AssetHistory> getAssetHistory(Long assetId) { ... }
```

---

## E10 - Performance Reviews & Goals

### Spring Boot Architecture Overview
- Performance review module for tracking reviews and goals.

### Package Structure
- `com.warehouse.ems.performance.domain`
- `com.warehouse.ems.performance.repository`
- `com.warehouse.ems.performance.service`
- `com.warehouse.ems.performance.web`

### Entity Design
#### PerformanceReview Entity
```java
@Entity
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    private String reviewPeriod;
    private String comments;
    private int rating;

    @Enumerated(EnumType.STRING)
    private ReviewStatus status; // DRAFT, SUBMITTED, ACKNOWLEDGED

    private LocalDateTime submittedDate;
    private LocalDateTime acknowledgedDate;
}
```

### Repository Layer
```java
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    List<PerformanceReview> findByEmployee(Employee employee);
}
```

### Service Layer
```java
public interface PerformanceService {
    PerformanceReview createReview(Long employeeId, PerformanceReviewDto dto);
    void submitReview(Long reviewId);
    void acknowledgeReview(Long reviewId);
    byte[] exportReviewPdf(Long reviewId);
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/performance")
public class PerformanceController {
    @PostMapping("/reviews")
    public ResponseEntity<PerformanceReviewDto> createReview(@RequestBody PerformanceReviewDto dto) { ... }

    @PostMapping("/reviews/{id}/submit")
    public ResponseEntity<Void> submitReview(@PathVariable Long id) { ... }

    @PostMapping("/reviews/{id}/acknowledge")
    public ResponseEntity<Void> acknowledgeReview(@PathVariable Long id) { ... }

    @GetMapping("/reviews/{id}/export")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) { ... }
}
```

### Security Configuration
- Supervisors create and submit reviews.
- Employees acknowledge reviews.

### Configuration
- Review templates:
  ```
  performance.review.template.path=/templates/review.pdf
  ```

### Integration Points
- Review data used in reporting.

### Code Samples
#### Review Workflow Example
```java
public void submitReview(Long reviewId) {
    // Update status to SUBMITTED
}
```
#### Immutable History Example
```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteReview(Long reviewId) {
    throw new ForbiddenException("Reviews are immutable after acknowledgement");
}
```

---

## E11 - Payroll Export Integration

### Spring Boot Architecture Overview
- Payroll integration module for exporting attendance and leave data.

### Package Structure
- `com.warehouse.ems.payroll.domain`
- `com.warehouse.ems.payroll.repository`
- `com.warehouse.ems.payroll.service`
- `com.warehouse.ems.payroll.web`

### Entity Design
#### PayrollExport Entity
```java
@Entity
public class PayrollExport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate payPeriodStart;
    private LocalDate payPeriodEnd;

    private String exportFormat; // CSV, XML, JSON

    @Enumerated(EnumType.STRING)
    private ExportStatus status; // PENDING, COMPLETED, FAILED

    private LocalDateTime exportDate;
}
```

### Repository Layer
```java
public interface PayrollExportRepository extends JpaRepository<PayrollExport, Long> {
    List<PayrollExport> findByStatus(ExportStatus status);
}
```

### Service Layer
```java
public interface PayrollService {
    PayrollExport generateExport(LocalDate start, LocalDate end, String format);
    void deliverExport(Long exportId, String method); // SFTP, API
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/export")
    public ResponseEntity<PayrollExportDto> generateExport(@RequestBody PayrollExportDto dto) { ... }

    @PostMapping("/export/{id}/deliver")
    public ResponseEntity<Void> deliverExport(@PathVariable Long id, @RequestParam String method) { ... }
}
```

### Security Configuration
- Only HR can generate and deliver exports.

### Configuration
- Payroll provider config:
  ```
  payroll.provider.url=https://payroll.example.com/api
  payroll.provider.apiKey=secret
  payroll.sftp.host=sftp.example.com
  ```

### Integration Points
- Payroll export uses attendance (E04) and leave (E06) data.

### Code Samples
#### Export Generation Example
```java
public PayrollExport generateExport(LocalDate start, LocalDate end, String format) {
    // Aggregate attendance and leave data
    // Generate file in specified format
}
```
#### SFTP Delivery Example
```java
public void deliverViaSFTP(Long exportId) {
    // Upload file to SFTP server
}
```

---

## E12 - Notifications & Announcements

### Spring Boot Architecture Overview
- Notification module for in-app, email, and SMS notifications.

### Package Structure
- `com.warehouse.ems.notification.domain`
- `com.warehouse.ems.notification.repository`
- `com.warehouse.ems.notification.service`
- `com.warehouse.ems.notification.web`

### Entity Design
#### Notification Entity
```java
@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee recipient;

    @Enumerated(EnumType.STRING)
    private NotificationType type; // SHIFT_CHANGE, CERT_EXPIRY, APPROVAL

    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel; // EMAIL, SMS, IN_APP

    private LocalDateTime sentDate;
    private boolean read;
}
```

### Repository Layer
```java
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientAndReadFalse(Employee recipient);
}
```

### Service Layer
```java
public interface NotificationService {
    void sendNotification(Long employeeId, NotificationDto dto);
    List<Notification> getUnreadNotifications(Long employeeId);
    void markAsRead(Long notificationId);
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @GetMapping("/unread")
    public List<NotificationDto> getUnread(@RequestParam Long employeeId) { ... }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) { ... }
}
```

### Security Configuration
- Employees see only their notifications.

### Configuration
- Notification channels:
  ```
  notification.email.enabled=true
  notification.sms.enabled=true
  notification.sms.provider=twilio
  ```

### Integration Points
- Notifications triggered by scheduling, leave, certification events.

### Code Samples
#### Notification Trigger Example
```java
public void onShiftChange(ShiftAssignment assignment) {
    notificationService.sendNotification(assignment.getEmployee().getId(), new NotificationDto("Shift changed"));
}
```
#### Quiet Hours Example
```java
public boolean isQuietHours(Employee employee) {
    // Check employee preferences
}
```

---

## E13 - Integration Layer (HRIS/WMS APIs)

### Spring Boot Architecture Overview
- Integration module for HRIS, WMS, and IDP connectors.

### Package Structure
- `com.warehouse.ems.integration.hris`
- `com.warehouse.ems.integration.wms`
- `com.warehouse.ems.integration.idp`
- `com.warehouse.ems.integration.webhook`

### Entity Design
#### IntegrationLog Entity
```java
@Entity
public class IntegrationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String source; // HRIS, WMS

    @Enumerated(EnumType.STRING)
    private IntegrationStatus status; // SUCCESS, FAILED

    private LocalDateTime timestamp;
    private String errorMessage;
}
```

### Repository Layer
```java
public interface IntegrationLogRepository extends JpaRepository<IntegrationLog, Long> {
    List<IntegrationLog> findByStatus(IntegrationStatus status);
}
```

### Service Layer
```java
public interface HRISIntegrationService {
    void syncEmployees();
}

public interface WMSIntegrationService {
    void syncDepartments();
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/api")
public class IntegrationController {
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody WebhookDto dto) { ... }
}
```

### Security Configuration
- JWT/OAuth2 for API access.

### Configuration
- Integration endpoints:
  ```
  integration.hris.url=https://hris.example.com/api
  integration.wms.url=https://wms.example.com/api
  ```

### Integration Points
- HRIS sync creates/updates employees.
- WMS sync updates departments and locations.

### Code Samples
#### HRIS Sync Job Example
```java
@Scheduled(cron = "0 0 2 * * ?")
public void syncEmployees() {
    // Fetch employees from HRIS
    // Create/update in EMS
}
```
#### Webhook Example
```java
public void handleWebhook(WebhookDto dto) {
    // Process event
    // Trigger actions
}
```

---

## E14 - Audit Trail & Compliance

### Spring Boot Architecture Overview
- Audit module for centralized logging of sensitive changes.

### Package Structure
- `com.warehouse.ems.audit.domain`
- `com.warehouse.ems.audit.repository`
- `com.warehouse.ems.audit.service`
- `com.warehouse.ems.audit.web`

### Entity Design
#### AuditEntry Entity
```java
@Entity
public class AuditEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String actor;
    private String action; // CREATE, UPDATE, DELETE
    private String entity;
    private String entityId;

    @Column(columnDefinition = "TEXT")
    private String before;

    @Column(columnDefinition = "TEXT")
    private String after;

    private LocalDateTime timestamp;
}
```

### Repository Layer
```java
public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> {
    List<AuditEntry> findByActorAndTimestampBetween(String actor, LocalDateTime start, LocalDateTime end);
}
```

### Service Layer
```java
public interface AuditService {
    void logAudit(AuditEntryDto dto);
    List<AuditEntry> getAuditTrail(String entity, String entityId);
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping
    public List<AuditEntryDto> getAuditTrail(@RequestParam String entity, @RequestParam String entityId) { ... }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAudit(@RequestParam LocalDate start, @RequestParam LocalDate end) { ... }
}
```

### Security Configuration
- Only admins can view audit logs.

### Configuration
- Audit log retention:
  ```
  audit.retention.days=365
  ```

### Integration Points
- Audit logs generated by all modules.

### Code Samples
#### Audit Logging Example
```java
@AfterReturning("execution(* com.warehouse.ems.employee.service.EmployeeService.update(..))")
public void logUpdate(JoinPoint joinPoint) {
    // Log audit entry
}
```
#### Immutable Audit Log Example
```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteAuditEntry(Long id) {
    throw new ForbiddenException("Audit logs are immutable");
}
```

---

## E15 - Reporting & Analytics

### Spring Boot Architecture Overview
- Reporting module for operational reports and dashboards.

### Package Structure
- `com.warehouse.ems.reporting.domain`
- `com.warehouse.ems.reporting.repository`
- `com.warehouse.ems.reporting.service`
- `com.warehouse.ems.reporting.web`

### Entity Design
- No new entities; reports generated from existing data.

### Repository Layer
- Custom queries for report generation.

### Service Layer
```java
public interface ReportingService {
    byte[] generateAttendanceReport(LocalDate start, LocalDate end, String format);
    byte[] generateOvertimeReport(LocalDate start, LocalDate end, String format);
    byte[] generateLeaveBalanceReport(String format);
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<byte[]> generateAttendanceReport(@RequestParam LocalDate start, @RequestParam LocalDate end, @RequestParam String format) { ... }

    @GetMapping("/overtime")
    public ResponseEntity<byte[]> generateOvertimeReport(@RequestParam LocalDate start, @RequestParam LocalDate end, @RequestParam String format) { ... }

    @GetMapping("/leave-balance")
    public ResponseEntity<byte[]> generateLeaveBalanceReport(@RequestParam String format) { ... }
}
```

### Security Configuration
- Role-based access to reports.

### Configuration
- Report templates:
  ```
  reporting.template.path=/templates/reports
  ```

### Integration Points
- Reports use data from attendance, leave, certification, safety modules.

### Code Samples
#### Report Generation Example
```java
public byte[] generateAttendanceReport(LocalDate start, LocalDate end, String format) {
    // Query attendance data
    // Generate CSV/PDF
}
```
#### Dashboard Metrics Example
```java
public Map<String, Object> getDashboardMetrics() {
    // Aggregate metrics
}
```

---

## E16 - Mobile Access (PWA)

### Spring Boot Architecture Overview
- PWA support for mobile-friendly views.

### Package Structure
- `com.warehouse.ems.web.mobile`

### Entity Design
- No new entities; mobile views use existing APIs.

### Repository Layer
- No changes.

### Service Layer
- No changes.

### Controller Layer
- Mobile-optimized endpoints.

### Security Configuration
- Same as web endpoints.

### Configuration
- PWA manifest:
  ```json
  {
    "name": "Warehouse EMS",
    "short_name": "EMS",
    "start_url": "/",
    "display": "standalone",
    "icons": [...]
  }
  ```

### Integration Points
- Mobile views use attendance, scheduling, leave APIs.

### Code Samples
#### PWA Manifest Example
```json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#000000",
  "icons": [
    {
      "src": "/icon-192.png",
      "sizes": "192x192",
      "type": "image/png"
    }
  ]
}
```
#### Offline Queue Example
```javascript
if (!navigator.onLine) {
  // Queue clock-in event
  localStorage.setItem('pendingClockIn', JSON.stringify(event));
}
```

---

## E17 - Onboarding & Offboarding Workflow

### Spring Boot Architecture Overview
- Onboarding/offboarding module for automating employee lifecycle.

### Package Structure
- `com.warehouse.ems.onboarding.domain`
- `com.warehouse.ems.onboarding.repository`
- `com.warehouse.ems.onboarding.service`
- `com.warehouse.ems.onboarding.web`

### Entity Design
#### OnboardingTask Entity
```java
@Entity
public class OnboardingTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    private String taskName;

    @Enumerated(EnumType.STRING)
    private TaskStatus status; // PENDING, COMPLETED

    private LocalDateTime dueDate;
}
```

### Repository Layer
```java
public interface OnboardingTaskRepository extends JpaRepository<OnboardingTask, Long> {
    List<OnboardingTask> findByEmployeeAndStatus(Employee employee, TaskStatus status);
}
```

### Service Layer
```java
public interface OnboardingService {
    void createOnboardingTasks(Long employeeId);
    void completeTask(Long taskId);
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @PostMapping("/tasks")
    public ResponseEntity<Void> createTasks(@RequestParam Long employeeId) { ... }

    @PostMapping("/tasks/{id}/complete")
    public ResponseEntity<Void> completeTask(@PathVariable Long id) { ... }
}
```

### Security Configuration
- HR manages onboarding tasks.

### Configuration
- Onboarding task templates:
  ```
  onboarding.tasks=Account Setup,Training,Asset Assignment
  ```

### Integration Points
- Onboarding triggered by HRIS sync (E13).

### Code Samples
#### Onboarding Task Creation Example
```java
public void createOnboardingTasks(Long employeeId) {
    // Create tasks for new hire
}
```
#### Offboarding Example
```java
public void offboardEmployee(Long employeeId) {
    // Revoke access
    // Collect assets
    // Cancel shifts
}
```

---

## E18 - Localization & Multi-Warehouse

### Spring Boot Architecture Overview
- Multi-warehouse support with localization.

### Package Structure
- `com.warehouse.ems.warehouse.domain`
- `com.warehouse.ems.warehouse.repository`
- `com.warehouse.ems.warehouse.service`
- `com.warehouse.ems.warehouse.web`

### Entity Design
#### Warehouse Entity
```java
@Entity
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private String timezone;

    @OneToMany(mappedBy = "warehouse")
    private List<Employee> employees;
}
```

### Repository Layer
```java
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> { }
```

### Service Layer
```java
public interface WarehouseService {
    Warehouse createWarehouse(WarehouseDto dto);
    List<Warehouse> getAllWarehouses();
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/warehouses")
public class WarehouseController {
    @PostMapping
    public ResponseEntity<WarehouseDto> createWarehouse(@RequestBody WarehouseDto dto) { ... }

    @GetMapping
    public List<WarehouseDto> getAllWarehouses() { ... }
}
```

### Security Configuration
- Admins manage warehouses.

### Configuration
- Localization:
  ```
  spring.messages.basename=messages
  spring.messages.encoding=UTF-8
  ```

### Integration Points
- Warehouse data used in scheduling and reporting.

### Code Samples
#### Localization Example
```java
@Autowired
private MessageSource messageSource;

public String getMessage(String key, Locale locale) {
    return messageSource.getMessage(key, null, locale);
}
```
#### Timezone Handling Example
```java
public LocalDateTime convertToWarehouseTime(LocalDateTime utc, String timezone) {
    return utc.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of(timezone)).toLocalDateTime();
}
```

---

## E19 - Advanced Scheduling (AI/Optimization)

### Spring Boot Architecture Overview
- AI/optimization module for auto-generating schedules.

### Package Structure
- `com.warehouse.ems.scheduling.optimization`

### Entity Design
- No new entities; uses existing scheduling entities.

### Repository Layer
- No changes.

### Service Layer
```java
public interface OptimizationService {
    Schedule generateOptimizedSchedule(ScheduleConstraints constraints);
    List<Schedule> runWhatIfScenarios(List<ScheduleConstraints> scenarios);
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/scheduling/optimization")
public class OptimizationController {
    @PostMapping("/generate")
    public ResponseEntity<ScheduleDto> generateSchedule(@RequestBody ScheduleConstraints constraints) { ... }

    @PostMapping("/what-if")
    public List<ScheduleDto> runWhatIf(@RequestBody List<ScheduleConstraints> scenarios) { ... }
}
```

### Security Configuration
- Supervisors can generate and review schedules.

### Configuration
- Optimization solver:
  ```
  optimization.solver=optaplanner
  ```

### Integration Points
- Optimization uses demand data from WMS (E13).

### Code Samples
#### Optimization Example
```java
public Schedule generateOptimizedSchedule(ScheduleConstraints constraints) {
    // Use OptaPlanner or similar solver
    // Enforce constraints (certifications, availability)
    // Minimize labor costs
}
```
#### What-If Scenario Example
```java
public List<Schedule> runWhatIfScenarios(List<ScheduleConstraints> scenarios) {
    // Generate schedules for each scenario
    // Compare metrics
}
```

---

## E20 - Self-Service Portal & Preferences

### Spring Boot Architecture Overview
- Self-service portal for employee profile and preferences.

### Package Structure
- `com.warehouse.ems.selfservice.domain`
- `com.warehouse.ems.selfservice.repository`
- `com.warehouse.ems.selfservice.service`
- `com.warehouse.ems.selfservice.web`

### Entity Design
#### EmployeePreferences Entity
```java
@Entity
public class EmployeePreferences {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Employee employee;

    private String preferredShift;
    private String availability;
    private String directDepositAccount;
}
```

### Repository Layer
```java
public interface EmployeePreferencesRepository extends JpaRepository<EmployeePreferences, Long> {
    Optional<EmployeePreferences> findByEmployee(Employee employee);
}
```

### Service Layer
```java
public interface SelfServiceService {
    void updateProfile(Long employeeId, ProfileDto dto);
    void updatePreferences(Long employeeId, PreferencesDto dto);
    List<PayStub> getPayStubs(Long employeeId);
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/self-service")
public class SelfServiceController {
    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileDto dto) { ... }

    @PutMapping("/preferences")
    public ResponseEntity<Void> updatePreferences(@RequestBody PreferencesDto dto) { ... }

    @GetMapping("/pay-stubs")
    public List<PayStubDto> getPayStubs() { ... }
}
```

### Security Configuration
- Employees can update their own profile and preferences.

### Configuration
- Direct deposit encryption:
  ```
  security.encryption.key=secret
  ```

### Integration Points
- Preferences influence scheduling (E05).
- Pay stubs from payroll integration (E11).

### Code Samples
#### Profile Update Example
```java
public void updateProfile(Long employeeId, ProfileDto dto) {
    // Update employee profile
    // Log audit entry
}
```
#### Direct Deposit Example
```java
public void updateDirectDeposit(Long employeeId, String accountNumber) {
    // Encrypt account number
    // Store securely
}
```

---

## Conclusion

This comprehensive low-level technical design document provides detailed specifications for all 20 epics of the Warehouse Employee Management System. Each epic includes:

- Spring Boot architecture overview
- Package structure following best practices
- Entity design with JPA annotations
- Repository, service, and controller layers
- Security configuration
- Application configuration
- Integration points
- Code samples demonstrating implementation

The design follows Spring Boot best practices, including:
- Layered architecture (Controller, Service, Repository, Domain)
- RESTful API design
- Spring Security for authentication and authorization
- Spring Data JPA for data access
- Proper use of annotations (@Entity, @Service, @RestController, etc.)
- Configuration management via application.properties
- Audit logging and compliance
- Integration with external systems

This document serves as a blueprint for development teams to implement the Warehouse EMS following industry standards and Spring Boot conventions.