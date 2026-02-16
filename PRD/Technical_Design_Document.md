# Warehouse Employee Management System  
## Comprehensive Technical Design Document  
### Spring Boot 3.x | Java 17+ | Production Blueprint

---

## Table of Contents
1. [E01: Project Scaffolding & Domain Setup](#e01)
2. [E02: Employee Master Data (CRUD)](#e02)
3. [E03: Role-Based Access Control (RBAC)](#e03)
4. [E04: Time & Attendance (Clock In/Out)](#e04)
5. [E05: Shift & Schedule Management](#e05)
6. [E06: Leave & Absence Management](#e06)
7. [E07: Training & Certification Tracking](#e07)
8. [E08: Safety Incidents & OSHA Reporting](#e08)
9. [E09: Equipment & Asset Assignment](#e09)
10. [E10: Performance Reviews & Goals](#e10)
11. [E11: Payroll Export Integration](#e11)
12. [E12: Notifications & Announcements](#e12)
13. [E13: Integration Layer (HRIS/WMS APIs)](#e13)
14. [E14: Audit Trail & Compliance](#e14)
15. [E15: Reporting & Analytics](#e15)
16. [E16: Mobile Access (PWA)](#e16)
17. [E17: Onboarding & Offboarding Workflow](#e17)
18. [E18: Localization & Multi-Tenant](#e18)
19. [E19: Observability & Monitoring](#e19)
20. [E20: CI/CD & Deployment Automation](#e20)

---

## <a name="e01"></a>E01: Project Scaffolding & Domain Setup

### Section: OVERVIEW
Description:  
Establishes the foundational Spring Boot project structure, configures Maven, sets up base packages, and integrates core modules (employee, scheduling, attendance, safety). Enables Flyway/Liquibase for DB migrations and Spring Boot Actuator for health monitoring.

Design Specification:
- Spring Boot 3.x, Java 17+
- Maven multi-module setup
- Base packages: `com.wms.employee`, `com.wms.scheduling`, `com.wms.attendance`, `com.wms.safety`
- Flyway/Liquibase for DB migrations
- Actuator enabled for health checks

Sample Implementation:
```java
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}
```
---

### Section: PACKAGE STRUCTURE
Description:  
Defines modular package structure for scalability and separation of concerns.

Design Specification:
- `com.wms.employee` (domain, repository, service, controller)
- `com.wms.scheduling`
- `com.wms.attendance`
- `com.wms.safety`
- `com.wms.common` (config, exceptions, utils)
- `com.wms.audit`
- `com.wms.integration`
- `com.wms.reporting`
- `com.wms.notifications`
- `com.wms.mobile`
- `com.wms.localization`
- `com.wms.observability`

Sample Implementation:
```
com.wms.employee
    âââ domain
    âââ repository
    âââ service
    âââ controller
com.wms.common
    âââ config
    âââ exceptions
    âââ utils
...
```
---

### Section: CONFIGURATION
Description:  
Centralizes configuration for DB, security, actuator, and module registration.

Design Specification:
- `application.yml` for environment settings
- Flyway/Liquibase migration scripts in `/db/migration`
- Actuator endpoints enabled

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
        include: health,info
```
---

## <a name="e02"></a>E02: Employee Master Data (CRUD)

### Section: OVERVIEW
Description:  
Implements Employee domain with full CRUD APIs, enforcing unique badgeId, soft-delete, pagination, filtering, and OpenAPI documentation.

Design Specification:
- Employee entity: name, badgeId, role, department, shiftGroup, hireDate, status
- REST endpoints: POST/GET/PUT/PATCH/DELETE `/employees`
- DTOs for request/response
- OpenAPI schemas

---

### Section: DOMAIN MODEL
Description:  
Defines Employee entity with JPA annotations and validation.

Design Specification:
- Fields: id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted
- Relationships: department, shiftGroup (ManyToOne)
- Constraints: unique badgeId, not null fields

Sample Implementation:
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Column(name = "badge_id", unique = true)
    private String badgeId;

    @Enumerated(EnumType.STRING)
    private EmployeeRole role;

    @ManyToOne
    private Department department;

    @ManyToOne
    private ShiftGroup shiftGroup;

    @PastOrPresent
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    private boolean deleted = false;
}
```
---

### Section: REPOSITORY LAYER
Description:  
Spring Data JPA repository with custom queries for filtering and soft-delete.

Design Specification:
- `EmployeeRepository extends JpaRepository<Employee, Long>`
- Methods: findByBadgeId, findAllByDeletedFalse, filter by department/role

Sample Implementation:
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```
---

### Section: SERVICE LAYER
Description:  
Business logic for CRUD, validation, soft-delete, and filtering.

Design Specification:
- Interface: `EmployeeService`
- Implementation: `EmployeeServiceImpl`
- Methods: create, update, delete (soft), filter, getById

Sample Implementation:
```java
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    // Autowired repository
    public Employee create(EmployeeDTO dto) { ... }
    public Employee update(Long id, EmployeeDTO dto) { ... }
    public void softDelete(Long id) { ... }
    public Page<Employee> filter(EmployeeFilter filter, Pageable pageable) { ... }
}
```
---

### Section: CONTROLLER LAYER
Description:  
RESTful controller with endpoints, DTOs, validation, and exception handling.

Design Specification:
- `@RestController @RequestMapping("/api/v1/employees")`
- Endpoints: POST, GET (list/detail), PUT, PATCH, DELETE
- DTOs: EmployeeRequest, EmployeeResponse
- Status codes: 200, 201, 204, 400, 404

Sample Implementation:
```java
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) { ... }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getById(@PathVariable Long id) { ... }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
}
```
---

### Section: CONFIGURATION
Description:  
OpenAPI/Swagger, validation, exception handling.

Design Specification:
- `springdoc-openapi` dependency
- `@ControllerAdvice` for global exception handling

Sample Implementation:
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) { ... }
}
```
---

## <a name="e03"></a>E03: Role-Based Access Control (RBAC)

### Section: OVERVIEW
Description:  
Integrates Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER), endpoint/method security, and API key/OAuth2 toggle.

Design Specification:
- Role enums: ADMIN, HR, SUPERVISOR, WORKER
- Security filters for endpoints
- Configurable authentication method

---

### Section: SECURITY
Description:  
Authentication and authorization mechanisms.

Design Specification:
- `SecurityConfig` class
- Role-based access via `@PreAuthorize`
- API key/OAuth2 toggle via `application.yml`

Sample Implementation:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/api/v1/employees/**").hasAnyRole("ADMIN", "HR")
            .antMatchers("/api/v1/attendance/**").hasAnyRole("SUPERVISOR", "WORKER")
            .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer().jwt();
    }
}
```
---

### Section: CONFIGURATION
Description:  
Security settings, API key/OAuth2 toggle.

Design Specification:
- `application.yml` toggle
- JWT/OAuth2 settings

Sample Implementation:
```yaml
security:
  auth-method: oauth2
  oauth2:
    issuer-uri: https://idp.example.com
    client-id: wms-app
    client-secret: secret
```
---

## <a name="e04"></a>E04: Time & Attendance (Clock In/Out)

### Section: OVERVIEW
Description:  
Endpoints for clock-in/out, geofence/device capture, hours calculation, missed punch workflow.

Design Specification:
- Attendance entity: employee, clockIn, clockOut, deviceId, location, status
- REST endpoints: POST `/attendance/clock-in`, `/attendance/clock-out`
- Correction workflow

---

### Section: DOMAIN MODEL
Description:  
Attendance entity with relationships and validation.

Design Specification:
- Fields: id, employee, clockIn, clockOut, deviceId, location, status
- Relationships: employee (ManyToOne)

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
    private String location;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;
}
```
---

### Section: REPOSITORY LAYER
Description:  
Attendance repository with custom queries.

Design Specification:
- `AttendanceRepository extends JpaRepository<Attendance, Long>`
- Methods: findByEmployeeAndDate, findMissedPunches

Sample Implementation:
```java
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEmployeeAndClockInBetween(Employee employee, LocalDateTime start, LocalDateTime end);
    List<Attendance> findByStatus(AttendanceStatus status);
}
```
---

### Section: SERVICE LAYER
Description:  
Business logic for clock-in/out, hours calculation, missed punch handling.

Design Specification:
- Interface: `AttendanceService`
- Methods: clockIn, clockOut, calculateHours, handleMissedPunch

Sample Implementation:
```java
@Service
public class AttendanceServiceImpl implements AttendanceService {
    public Attendance clockIn(AttendanceRequest request) { ... }
    public Attendance clockOut(Long attendanceId, AttendanceRequest request) { ... }
    public Duration calculateHours(Long employeeId, LocalDate date) { ... }
}
```
---

### Section: CONTROLLER LAYER
Description:  
REST endpoints for attendance operations.

Design Specification:
- `@RestController @RequestMapping("/api/v1/attendance")`
- Endpoints: POST `/clock-in`, `/clock-out`, GET `/daily-totals`, POST `/corrections`
- DTOs: AttendanceRequest, AttendanceResponse

Sample Implementation:
```java
@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceResponse> clockIn(@Valid @RequestBody AttendanceRequest request) { ... }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceResponse> clockOut(@Valid @RequestBody AttendanceRequest request) { ... }
}
```
---

## <a name="e05"></a>E05: Shift & Schedule Management

### Section: OVERVIEW
Description:  
Manages shift templates, rotations, overtime rules, employee assignments, blackout dates.

Design Specification:
- ShiftTemplate, ShiftSchedule entities
- CRUD endpoints for templates/schedules
- Conflict detection logic

---

### Section: DOMAIN MODEL
Description:  
ShiftTemplate and ShiftSchedule entities.

Design Specification:
- Fields: id, name, startTime, endTime, recurrence, blackoutDates
- Relationships: employees (ManyToMany)

Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrence; // e.g., WEEKLY
    @ElementCollection
    private List<LocalDate> blackoutDates;
}
```
---

### Section: REPOSITORY LAYER
Description:  
ShiftTemplate and ShiftSchedule repositories.

Design Specification:
- `ShiftTemplateRepository`, `ShiftScheduleRepository`
- Methods: findConflicts, findByEmployee

Sample Implementation:
```java
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {
    List<ShiftTemplate> findByBlackoutDatesContains(LocalDate date);
}
```
---

### Section: SERVICE LAYER
Description:  
Business logic for shift assignment, conflict detection.

Design Specification:
- Interface: `ShiftService`
- Methods: assignShift, detectConflicts, bulkAssign

Sample Implementation:
```java
@Service
public class ShiftServiceImpl implements ShiftService {
    public void assignShift(Long employeeId, Long shiftTemplateId) { ... }
    public boolean detectConflicts(Long employeeId, LocalDate date) { ... }
}
```
---

### Section: CONTROLLER LAYER
Description:  
REST endpoints for shift management.

Design Specification:
- `@RestController @RequestMapping("/api/v1/shifts")`
- Endpoints: CRUD for templates/schedules, GET `/conflicts`, POST `/bulk-assign`
- DTOs: ShiftTemplateRequest, ShiftScheduleResponse

Sample Implementation:
```java
@RestController
@RequestMapping("/api/v1/shifts")
public class ShiftController {
    @PostMapping("/bulk-assign")
    public ResponseEntity<Void> bulkAssign(@RequestBody BulkAssignRequest request) { ... }
}
```
---

## <a name="e06"></a>E06: Leave & Absence Management

### Section: OVERVIEW
Description:  
Handles PTO, sick, unpaid leave requests, approvals, accrual balances, and integration with scheduling/payroll.

Design Specification:
- LeaveRequest, LeaveBalance entities
- Workflow for request/approval
- Integration hooks

---

### Section: DOMAIN MODEL
Description:  
LeaveRequest and LeaveBalance entities.

Design Specification:
- Fields: id, employee, type, startDate, endDate, status, balance
- Relationships: employee (ManyToOne)

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
    private LeaveStatus status;
}
```
---

### Section: REPOSITORY LAYER
Description:  
LeaveRequest repository with filtering.

Design Specification:
- `LeaveRequestRepository`
- Methods: findByEmployee, findByStatus

Sample Implementation:
```java
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployee(Employee employee);
    List<LeaveRequest> findByStatus(LeaveStatus status);
}
```
---

### Section: SERVICE LAYER
Description:  
Business logic for leave request, approval, balance update.

Design Specification:
- Interface: `LeaveService`
- Methods: requestLeave, approveLeave, updateBalance

Sample Implementation:
```java
@Service
public class LeaveServiceImpl implements LeaveService {
    public LeaveRequest requestLeave(LeaveRequestDTO dto) { ... }
    public LeaveRequest approveLeave(Long requestId) { ... }
    public void updateBalance(Long employeeId, LeaveType type, int days) { ... }
}
```
---

### Section: CONTROLLER LAYER
Description:  
REST endpoints for leave management.

Design Specification:
- `@RestController @RequestMapping("/api/v1/leaves")`
- Endpoints: POST `/request`, POST `/approve`, GET `/balances`
- DTOs: LeaveRequestDTO, LeaveBalanceDTO

Sample Implementation:
```java
@RestController
@RequestMapping("/api/v1/leaves")
public class LeaveController {
    @PostMapping("/request")
    public ResponseEntity<LeaveRequestDTO> requestLeave(@Valid @RequestBody LeaveRequestDTO dto) { ... }
}
```
---

## <a name="e07"></a>E07: Training & Certification Tracking

### Section: OVERVIEW
Description:  
Tracks certifications, expirations, renewals, blocks assignments if expired, uploads proof documents.

Design Specification:
- Certification entity: employee, type, expiryDate, documentUrl
- Alerts for expiry

---

### Section: DOMAIN MODEL
Description:  
Certification entity.

Design Specification:
- Fields: id, employee, type, expiryDate, documentUrl
- Relationships: employee (ManyToOne)

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
}
```
---

### Section: REPOSITORY LAYER
Description:  
Certification repository with expiry alerts.

Design Specification:
- `CertificationRepository`
- Methods: findByExpiryDateBetween, findByEmployee

Sample Implementation:
```java
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByExpiryDateBetween(LocalDate start, LocalDate end);
}
```
---

### Section: SERVICE LAYER
Description:  
Business logic for certification tracking, expiry alerts.

Design Specification:
- Interface: `CertificationService`
- Methods: create, renew, alertExpiry

Sample Implementation:
```java
@Service
public class CertificationServiceImpl implements CertificationService {
    public Certification create(CertificationDTO dto) { ... }
    public void alertExpiry() { ... }
}
```
---

### Section: CONTROLLER LAYER
Description:  
REST endpoints for certification management.

Design Specification:
- `@RestController @RequestMapping("/api/v1/certifications")`
- Endpoints: CRUD, GET `/alerts`
- DTOs: CertificationDTO

Sample Implementation:
```java
@RestController
@RequestMapping("/api/v1/certifications")
public class CertificationController {
    @GetMapping("/alerts")
    public ResponseEntity<List<CertificationDTO>> getExpiryAlerts() { ... }
}
```
---

## <a name="e08"></a>E08: Safety Incidents & OSHA Reporting

### Section: OVERVIEW
Description:  
Records incidents/near-misses, investigation workflow, OSHA summary generation.

Design Specification:
- SafetyIncident entity: severity, location, description, involvedEmployees, status
- Workflow: Open â Investigating â Resolved

---

### Section: DOMAIN MODEL
Description:  
SafetyIncident entity.

Design Specification:
- Fields: id, severity, location, description, involvedEmployees, status, correctiveActions
- Relationships: involvedEmployees (ManyToMany)

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
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
    private String correctiveActions;
}
```
---

### Section: REPOSITORY LAYER
Description:  
SafetyIncident repository.

Design Specification:
- `SafetyIncidentRepository`
- Methods: findByStatus, findByDateRange

Sample Implementation:
```java
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
    List<SafetyIncident> findByStatus(IncidentStatus status);
}
```
---

### Section: SERVICE LAYER
Description:  
Business logic for incident workflow, OSHA reporting.

Design Specification:
- Interface: `SafetyIncidentService`
- Methods: recordIncident, updateStatus, generateOSHAReport

Sample Implementation:
```java
@Service
public class SafetyIncidentServiceImpl implements SafetyIncidentService {
    public SafetyIncident recordIncident(SafetyIncidentDTO dto) { ... }
    public void updateStatus(Long incidentId, IncidentStatus status) { ... }
    public OSHAReport generateOSHAReport(LocalDate start, LocalDate end) { ... }
}
```
---

### Section: CONTROLLER LAYER
Description:  
REST endpoints for safety incidents.

Design Specification:
- `@RestController @RequestMapping("/api/v1/safety/incidents")`
- Endpoints: POST `/`, PATCH `/status`, GET `/osha-report`
- DTOs: SafetyIncidentDTO, OSHAReportDTO

Sample Implementation:
```java
@RestController
@RequestMapping("/api/v1/safety/incidents")
public class SafetyIncidentController {
    @PostMapping
    public ResponseEntity<SafetyIncidentDTO> recordIncident(@Valid @RequestBody SafetyIncidentDTO dto) { ... }
}
```
---

## <a name="e09"></a>E09: Equipment & Asset Assignment

### Section: OVERVIEW
Description:  
Assigns assets to employees, tracks checkout/return, blocks use if certification missing.

Design Specification:
- Asset entity: type, condition, assignedEmployee, checkoutDate, returnDate
- Certification check logic

---

### Section: DOMAIN MODEL
Description:  
Asset entity.

Design Specification:
- Fields: id, type, condition, assignedEmployee, checkoutDate, returnDate
- Relationships: assignedEmployee (ManyToOne)

Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String condition;
    @ManyToOne
    private Employee assignedEmployee;
    private LocalDateTime checkoutDate;
    private LocalDateTime returnDate;
}
```
---

### Section: REPOSITORY LAYER
Description:  
Asset repository.

Design Specification:
- `AssetRepository`
- Methods: findByAssignedEmployee, findOverdue

Sample Implementation:
```java
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByAssignedEmployee(Employee employee);
    List<Asset> findByReturnDateIsNullAndCheckoutDateBefore(LocalDateTime cutoff);
}
```
---

### Section: SERVICE LAYER
Description:  
Business logic for asset assignment, certification check.

Design Specification:
- Interface: `AssetService`
- Methods: assignAsset, returnAsset, checkCertification

Sample Implementation:
```java
@Service
public class AssetServiceImpl implements AssetService {
    public Asset assignAsset(Long employeeId, AssetDTO dto) { ... }
    public void returnAsset(Long assetId) { ... }
    public boolean checkCertification(Long employeeId, String assetType) { ... }
}
```
---

### Section: CONTROLLER LAYER
Description:  
REST endpoints for asset management.

Design Specification:
- `@RestController @RequestMapping("/api/v1/assets")`
- Endpoints: POST `/assign`, POST `/return`, GET `/overdue`
- DTOs: AssetDTO

Sample Implementation:
```java
@RestController
@RequestMapping("/api/v1/assets")
public class AssetController {
    @PostMapping("/assign")
    public ResponseEntity<AssetDTO> assignAsset(@Valid @RequestBody AssetDTO dto) { ... }
}
```
---

## <a name="e10"></a>E10: Performance Reviews & Goals

### Section: OVERVIEW
Description:  
Manages review templates, goals, competencies, ratings, comments, acknowledgement workflow.

Design Specification:
- PerformanceReview entity: employee, cycle, goals, competencies, ratings, comments, status

---

### Section: DOMAIN MODEL
Description:  
PerformanceReview entity.

Design Specification:
- Fields: id, employee, cycle, goals, competencies, ratings, comments, status
- Relationships: employee (ManyToOne)

Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String cycle;
    @ElementCollection
    private List<String> goals;
    @ElementCollection
    private List<String> competencies;
    private String ratings;
    private String comments;
    @Enumerated(EnumType.STRING)
    private ReviewStatus status;
}
```
---

### Section: REPOSITORY LAYER
Description:  
PerformanceReview repository.

Design Specification:
- `PerformanceReviewRepository`
- Methods: findByEmployee, findByCycle

Sample Implementation:
```java
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    List<PerformanceReview> findByEmployee(Employee employee);
}
```
---

### Section: SERVICE LAYER
Description:  
Business logic for review cycles, acknowledgement.

Design Specification:
- Interface: `PerformanceReviewService`
- Methods: createReview, submitReview, acknowledgeReview

Sample Implementation:
```java
@Service
public class PerformanceReviewServiceImpl implements PerformanceReviewService {
    public PerformanceReview createReview(PerformanceReviewDTO dto) { ... }
    public void submitReview(Long reviewId) { ... }
    public void acknowledgeReview(Long reviewId) { ... }
}
```
---

### Section: CONTROLLER LAYER
Description:  
REST endpoints for performance reviews.

Design Specification:
- `@RestController @RequestMapping("/api/v1/reviews")`
- Endpoints: POST `/create`, POST `/submit`, POST `/acknowledge`
- DTOs: PerformanceReviewDTO

Sample Implementation:
```java
@RestController
@RequestMapping("/api/v1/reviews")
public class PerformanceReviewController {
    @PostMapping("/create")
    public ResponseEntity<PerformanceReviewDTO> createReview(@Valid @RequestBody PerformanceReviewDTO dto) { ... }
}
```
---

## <a name="e11"></a>E11: Payroll Export Integration

### Section: OVERVIEW
Description:  
Generates payroll-ready files from attendance/leave, maps to provider formats, delivers securely.

Design Specification:
- PayrollExport entity: employee, period, hours, leave, fileUrl, status
- SFTP/API delivery logic

---

### Section: DOMAIN MODEL
Description:  
PayrollExport entity.

Design Specification:
- Fields: id, employee, period, hours, leave, fileUrl, status
- Relationships: employee (ManyToOne)

Sample Implementation:
```java
@Entity
public class PayrollExport {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String period;
    private Double hours;
    private Double leave;
    private String fileUrl;
    @Enumerated(EnumType.STRING)
    private ExportStatus status;
}
```
---

### Section: REPOSITORY LAYER
Description:  
PayrollExport repository.

Design Specification:
- `PayrollExportRepository`
- Methods: findByPeriod, findByStatus

Sample Implementation:
```java
public interface PayrollExportRepository extends JpaRepository<PayrollExport, Long> {
    List<PayrollExport> findByPeriod(String period);
}
```
---

### Section: SERVICE LAYER
Description:  
Business logic for export generation, delivery, retry.

Design Specification:
- Interface: `PayrollExportService`
- Methods: generateExport, deliverExport, retryFailed

Sample Implementation:
```java
@Service
public class PayrollExportServiceImpl implements PayrollExportService {
    public PayrollExport generateExport(String period) { ... }
    public void deliverExport(Long exportId) { ... }
    public void retryFailed(Long exportId) { ... }
}
```
---

### Section: CONTROLLER LAYER
Description:  
REST endpoints for payroll export.

Design Specification:
- `@RestController @RequestMapping("/api/v1/payroll")`
- Endpoints: POST `/generate`, POST `/deliver`, POST `/retry`
- DTOs: PayrollExportDTO

Sample Implementation:
```java
@RestController
@RequestMapping("/api/v1/payroll")
public class PayrollExportController {
    @PostMapping("/generate")
    public ResponseEntity<PayrollExportDTO> generateExport(@RequestBody PayrollExportDTO dto) { ... }
}
```
---

## <a name="e12"></a>E12: Notifications & Announcements

### Section: OVERVIEW
Description:  
Handles in-app/email/SMS notifications for shift changes, expiring certs, approvals, announcements.

Design Specification:
- Notification entity: user, channel, template, status, deliveryTime
- Announcement entity

---

### Section: DOMAIN MODEL
Description:  
Notification and Announcement entities.

Design Specification:
- Fields: id, user, channel, template, status, deliveryTime
- Relationships: user (ManyToOne)

Sample Implementation:
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee user;
    private String channel;
    private String template;
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
    private LocalDateTime deliveryTime;
}
```
---

### Section: REPOSITORY LAYER
Description:  
Notification repository.

Design Specification:
- `NotificationRepository`
- Methods: findByUser, findByStatus

Sample Implementation:
```java
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser(Employee user);
}
```
---

### Section: SERVICE LAYER
Description:  
Business logic for notification delivery, opt-in/out, rate limiting.

Design Specification:
- Interface: `NotificationService`
- Methods: sendNotification, optIn, optOut, trackDelivery

Sample Implementation:
```java
@Service
public class NotificationServiceImpl implements NotificationService {
    public void sendNotification(NotificationDTO dto) { ... }
    public void optIn(Long userId, String channel) { ... }
    public void optOut(Long userId, String channel) { ... }
}
```
---

### Section: CONTROLLER LAYER
Description:  
REST endpoints for notifications.

Design Specification:
- `@RestController @RequestMapping("/api/v1/notifications")`
- Endpoints: POST `/send`, POST `/opt-in`, POST `/opt-out`, GET `/status`
- DTOs: NotificationDTO

Sample Implementation:
```java
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationDTO dto) { ... }
}
```
---

## <a name="e13"></a>E13: Integration Layer (HRIS/WMS APIs)

### Section: OVERVIEW
Description:  
Exposes REST APIs/connectors for HRIS, WMS, IDP; webhooks for events.

Design Specification:
- Integration endpoints: `/api/v1/integration/hris`, `/api/v1/integration/wms`, `/api/v1/integration/idp`
- JWT/OAuth2 security

---

### Section: CONTROLLER LAYER
Description:  
REST endpoints for integration.

Design Specification:
- `@RestController @RequestMapping("/api/v1/integration")`
- Endpoints: POST `/hris`, POST `/wms`, POST `/idp`, POST `/webhook`
- DTOs: HRISSyncDTO, WMSSyncDTO

Sample Implementation:
```java
@RestController
@RequestMapping("/api/v1/integration")
public class IntegrationController {
    @PostMapping("/hris")
    public ResponseEntity<Void> syncHRIS(@RequestBody HRISSyncDTO dto) { ... }
}
```
---

### Section: CONFIGURATION
Description:  
OpenAPI documentation, JWT/OAuth2 security.

Design Specification:
- `springdoc-openapi`
- JWT/OAuth2 settings

Sample Implementation:
```yaml
springdoc:
  api-docs:
    path: /api-docs
```
---

## <a name="e14"></a>E14: Audit Trail & Compliance

### Section: OVERVIEW
Description:  
Centralized audit logging for sensitive changes, tamper-evident storage.

Design Specification:
- AuditLog entity: actor, timestamp, entity, before, after, action
- Immutable log table

---

### Section: DOMAIN MODEL
Description:  
AuditLog entity.

Design Specification:
- Fields: id, actor, timestamp, entity, before, after, action

Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    @Lob
    private String before;
    @Lob
    private String after;
    private String action;
}
```
---

### Section: REPOSITORY LAYER
Description:  
AuditLog repository.

Design Specification:
- `AuditLogRepository`
- Methods: findByEntity, findByActor, findByDateRange

Sample Implementation:
```java
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntity(String entity);
}
```
---

### Section: SERVICE LAYER
Description:  
Business logic for audit logging, export.

Design Specification:
- Interface: `AuditService`
- Methods: logChange, exportLogs

Sample Implementation:
```java
@Service
public class AuditServiceImpl implements AuditService {
    public void logChange(String actor, String entity, String before, String after, String action) { ... }
    public List<AuditLog> exportLogs(LocalDate start, LocalDate end) { ... }
}
```
---

### Section: CONTROLLER LAYER
Description:  
REST endpoints for audit logs.

Design Specification:
- `@RestController @RequestMapping("/api/v1/audit")`
- Endpoints: GET `/logs`, POST `/export`
- DTOs: AuditLogDTO

Sample Implementation:
```java
@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {
    @GetMapping("/logs")
    public ResponseEntity<List<AuditLogDTO>> getLogs(@RequestParam String entity) { ... }
}
```
---

## <a name="e15"></a>E15: Reporting & Analytics

### Section: OVERVIEW
Description:  
Operational reports: attendance, overtime, leave balances, certification status, safety KPIs; export CSV/PDF; dashboards.

Design Specification:
- Report endpoints: `/api/v1/reports`
- Metrics endpoints

---

### Section: CONTROLLER LAYER
Description:  
REST endpoints for reporting.

Design Specification:
- `@RestController @RequestMapping("/api/v1/reports")`
- Endpoints: GET `/attendance`, `/overtime`, `/leave-balances`, `/certifications`, `/safety-kpis`, `/export`
- DTOs: ReportDTO

Sample Implementation:
```java
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    @GetMapping("/attendance")
    public ResponseEntity<ReportDTO> getAttendanceReport(@RequestParam LocalDate start, @RequestParam LocalDate end) { ... }
}
```
---

## <a name="e16"></a>E16: Mobile Access (PWA)

### Section: OVERVIEW
Description:  
Responsive views for clock-in/out, schedules, leave requests, announcements; offline PWA.

Design Specification:
- PWA manifest
- Offline queue for clock events

---

### Section: CONFIGURATION
Description:  
PWA manifest, Lighthouse score, offline support.

Design Specification:
- `manifest.json`
- Service worker for offline queue

Sample Implementation:
```json
{
  "name": "Warehouse Employee Management",
  "short_name": "WMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```
---

## <a name="e17"></a>E17: Onboarding & Offboarding Workflow

### Section: OVERVIEW
Description:  
Automates provisioning, initial schedule, training, asset assignment, deprovisioning.

Design Specification:
- Workflow endpoints: `/api/v1/onboarding`, `/api/v1/offboarding`
- Task generation logic

---

### Section: CONTROLLER LAYER
Description:  
REST endpoints for onboarding/offboarding.

Design Specification:
- `@RestController @RequestMapping("/api/v1/onboarding")`
- Endpoints: POST `/provision`, POST `/deprovision`
- DTOs: OnboardingDTO, OffboardingDTO

Sample Implementation:
```java
@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {
    @PostMapping("/provision")
    public ResponseEntity<Void> provision(@RequestBody OnboardingDTO dto) { ... }
}
```
---

## <a name="e18"></a>E18: Localization & Multi-Tenant

### Section: OVERVIEW
Description:  
Tenant isolation for data/config, locale-specific date/time/currency/language.

Design Specification:
- Tenant entity: id, name, config
- Locale settings in `application.yml`

---

### Section: CONFIGURATION
Description:  
Multi-tenant and localization settings.

Design Specification:
- `application.yml` for tenant/locale
- Tenant resolver filter

Sample Implementation:
```yaml
tenant:
  enabled: true
  resolver: header
spring:
  messages:
    basename: messages
    encoding: UTF-8
```
---

## <a name="e19"></a>E19: Observability & Monitoring

### Section: OVERVIEW
Description:  
Structured JSON logging, distributed tracing (OpenTelemetry), Prometheus metrics.

Design Specification:
- SLF4J logging
- OpenTelemetry integration
- Prometheus actuator metrics

---

### Section: CONFIGURATION
Description:  
Logging, tracing, metrics settings.

Design Specification:
- `application.yml` for logging/tracing
- OpenTelemetry beans

Sample Implementation:
```yaml
logging:
  level:
    root: INFO
  pattern:
    console: '{"timestamp":"%d{yyyy-MM-dd HH:mm:ss}","level":"%p","logger":"%c","message":"%m"}'
management:
  metrics:
    export:
      prometheus:
        enabled: true
```
---

## <a name="e20"></a>E20: CI/CD & Deployment Automation

### Section: OVERVIEW
Description:  
CI pipeline for build/test/security scan, Docker image build, automated deployment.

Design Specification:
- GitHub Actions/Jenkins pipeline
- Dockerfile for Spring Boot app
- Automated deployment scripts

---

### Section: CONFIGURATION
Description:  
CI/CD pipeline and Docker settings.

Design Specification:
- `.github/workflows/ci.yml`
- `Dockerfile`

Sample Implementation:
```yaml
# .github/workflows/ci.yml
name: CI
on: [push]
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
      - name: Build Docker image
        run: docker build -t wms-app .
```
```dockerfile
# Dockerfile
FROM openjdk:17-jdk
COPY target/wms-app.jar /app/wms-app.jar
ENTRYPOINT ["java", "-jar", "/app/wms-app.jar"]
```
---

## General Best Practices

- Use DTOs for all API requests/responses with validation annotations
- Implement global exception handling with `@ControllerAdvice`
- Use proper transaction management with `@Transactional`
- Follow SOLID principles and clean code practices
- Use proper HTTP status codes and API versioning
- Document APIs with OpenAPI/Swagger
- Implement structured logging with SLF4J
- Secure endpoints with JWT/OAuth2 and role-based access
- Ensure tenant isolation and localization
- Monitor with Prometheus/OpenTelemetry
- Automate build/test/deploy with CI/CD

---

## End of Document

---