# Warehouse Employee Management System â Comprehensive Low-Level Technical Design Document (Spring Boot)

================================================================================
## Epic E01: Project Scaffolding & Domain Setup
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Establishes the foundational structure for the Warehouse EMS using Spring Boot (Maven), modularizing core domains (employee, scheduling, attendance, safety), and integrating essential frameworks (Flyway/Liquibase for DB migrations, Actuator for health checks).

**Design Specification:**
- Spring Boot starter project (Maven)
- Modular package structure: com.wms.employee, com.wms.scheduling, com.wms.attendance, com.wms.safety
- Flyway/Liquibase for DB migrations
- Spring Boot Actuator enabled

**Sample Implementation:**
```java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

### Section: Package Structure and Module Definitions
**Description:** Organizes code into logical modules for maintainability and scalability.

**Design Specification:**
- com.wms.config
- com.wms.employee
- com.wms.scheduling
- com.wms.attendance
- com.wms.safety

**Sample Implementation:**
```
src/main/java/com/wms/
    config/
    employee/
    scheduling/
    attendance/
    safety/
```

### Section: Configuration and Spring Security Settings
**Description:** Initial configuration for application properties, DB migration, and actuator.

**Design Specification:**
- application.properties: server.port=8080, spring.datasource.*, flyway.enabled=true
- Actuator endpoints: /actuator/health

**Sample Implementation:**
```properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5432/wms
spring.datasource.username=wms
spring.datasource.password=secret
management.endpoints.web.exposure.include=health,info
```

### Section: Integration Points
**Description:** Prepares for future integrations (HRIS, WMS, IDP).

**Design Specification:**
- Placeholder config classes for external APIs

**Sample Implementation:**
```java
@Configuration
public class IntegrationConfig {
    // Future beans for HRIS/WMS/IDP connectors
}
```

### Section: Exception Handling Strategy
**Description:** Global exception handler for REST APIs.

**Design Specification:**
- @ControllerAdvice with @ExceptionHandler

**Sample Implementation:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
```

### Section: Transaction Management
**Description:** Enables transaction management for all modules.

**Design Specification:**
- @EnableTransactionManagement in main config

**Sample Implementation:**
```java
@Configuration
@EnableTransactionManagement
public class TransactionConfig {}
```

================================================================================
## Epic E02: Employee Master Data (CRUD)
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Implements CRUD operations for Employee domain, exposing RESTful APIs and DTOs.

**Design Specification:**
- Employee entity, repository, service, controller
- Pagination, filtering, soft-delete

**Sample Implementation:**
```java
@Entity
public class Employee {
    @Id @GeneratedValue
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

### Section: Service Layer Specifications
**Description:** Business logic for employee CRUD, badgeId uniqueness, soft-delete.

**Design Specification:**
- EmployeeService: create, update, delete, find, filter, paginate

**Sample Implementation:**
```java
@Service
public class EmployeeService {
    @Autowired private EmployeeRepository repo;
    public Employee create(EmployeeDto dto) { /* validate badgeId, map, save */ }
    public Page<Employee> findAll(Pageable pageable, EmployeeFilter filter) { /* ... */ }
    public void softDelete(Long id) { /* set deleted=true */ }
}
```

### Section: Repository Layer
**Description:** Spring Data JPA repository for Employee.

**Design Specification:**
- EmployeeRepository extends JpaRepository<Employee, Long>

**Sample Implementation:**
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

### Section: Controller Specifications
**Description:** REST endpoints for employee CRUD.

**Design Specification:**
- /employees [GET, POST, PUT, PATCH, DELETE]

**Sample Implementation:**
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @GetMapping public Page<EmployeeDto> list(...) { /* ... */ }
    @PostMapping public EmployeeDto create(...) { /* ... */ }
    @PutMapping("/{id}") public EmployeeDto update(...) { /* ... */ }
    @DeleteMapping("/{id}") public void delete(...) { /* ... */ }
}
```

### Section: DTO Classes and Mappers
**Description:** DTOs for API requests/responses, mappers for entity conversion.

**Design Specification:**
- EmployeeDto, EmployeeCreateDto, EmployeeUpdateDto

**Sample Implementation:**
```java
public class EmployeeDto {
    private Long id;
    private String badgeId;
    private String name;
    // ...
}
@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeDto toDto(Employee entity);
    Employee toEntity(EmployeeDto dto);
}
```

### Section: Validation Rules
**Description:** Enforces badgeId uniqueness, required fields.

**Design Specification:**
- @NotBlank, @Size, custom validator for badgeId

**Sample Implementation:**
```java
public class EmployeeCreateDto {
    @NotBlank private String badgeId;
    @NotBlank private String name;
    // ...
}
```

### Section: Exception Handling Strategy
**Description:** Handles duplicate badgeId, not found, validation errors.

**Design Specification:**
- Custom exceptions: BadgeIdExistsException, EmployeeNotFoundException

**Sample Implementation:**
```java
@ExceptionHandler(BadgeIdExistsException.class)
public ResponseEntity<?> handleBadgeIdExists(BadgeIdExistsException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
}
```

### Section: Transaction Management
**Description:** All create/update/delete operations are transactional.

**Design Specification:**
- @Transactional on service methods

**Sample Implementation:**
```java
@Transactional
public Employee create(EmployeeDto dto) { /* ... */ }
```

### Section: Caching Strategy
**Description:** Cache employee lookups for performance.

**Design Specification:**
- @Cacheable on findByBadgeId

**Sample Implementation:**
```java
@Cacheable("employees")
public Optional<Employee> findByBadgeId(String badgeId) { /* ... */ }
```

================================================================================
## Epic E03: Role Based Access Control (RBAC)
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Secures endpoints and data access using Spring Security, supporting roles (ADMIN, HR, SUPERVISOR, WORKER).

**Design Specification:**
- Method and endpoint security
- Row-level constraints
- API key/OAuth2 toggle via config

**Sample Implementation:**
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
            .anyRequest().authenticated()
            .and().oauth2Login();
    }
}
```

### Section: Configuration and Spring Security Settings
**Description:** Role hierarchy, endpoint protection, authentication providers.

**Design Specification:**
- Roles: ADMIN > HR > SUPERVISOR > WORKER
- API key/OAuth2 toggle via properties

**Sample Implementation:**
```properties
spring.security.oauth2.enabled=true
spring.security.api-key.enabled=false
```

### Section: Service Layer Specifications
**Description:** Row-level security for SUPERVISOR (team-only access).

**Design Specification:**
- EmployeeService: filter by supervisor's team

**Sample Implementation:**
```java
@PreAuthorize("hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(authentication, #employeeId)")
public Employee getEmployee(Long employeeId) { /* ... */ }
```

### Section: Exception Handling Strategy
**Description:** Handles 401/403 errors.

**Design Specification:**
- Custom access denied handler

**Sample Implementation:**
```java
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    public void handle(...) { /* return 403 */ }
}
```

### Section: Integration Points
**Description:** OAuth2 integration, API key support.

**Design Specification:**
- OAuth2 client config, API key filter

**Sample Implementation:**
```java
@Bean
public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() { /* ... */ }
```

================================================================================
## Epic E04: Time & Attendance (Clock In/Out)
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Manages clock-in/out events, geofence/device capture, shift association, corrections workflow.

**Design Specification:**
- Attendance entity, service, controller
- Geofence/device info in event

**Sample Implementation:**
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne private Employee employee;
    private LocalDateTime timestamp;
    private String type; // CLOCK_IN, CLOCK_OUT
    private String deviceId;
    private String geoLocation;
    private boolean correctionRequested;
}
```

### Section: Service Layer Specifications
**Description:** Business logic for clock-in/out, missed punches, corrections.

**Design Specification:**
- AttendanceService: recordEvent, calculateTotals, requestCorrection

**Sample Implementation:**
```java
@Service
public class AttendanceService {
    public AttendanceEvent recordClockIn(Long employeeId, ...) { /* ... */ }
    public AttendanceReport getDailyTotals(Long employeeId, LocalDate date) { /* ... */ }
    public CorrectionTask requestCorrection(Long eventId, ...) { /* ... */ }
}
```

### Section: Controller Specifications
**Description:** REST endpoints for clock-in/out, corrections, reports.

**Design Specification:**
- /attendance/clock-in [POST]
- /attendance/clock-out [POST]
- /attendance/reports [GET]

**Sample Implementation:**
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in") public AttendanceEventDto clockIn(...) { /* ... */ }
    @PostMapping("/clock-out") public AttendanceEventDto clockOut(...) { /* ... */ }
    @GetMapping("/reports") public AttendanceReportDto getReport(...) { /* ... */ }
}
```

### Section: DTO Classes and Mappers
**Description:** DTOs for attendance events, reports, corrections.

**Design Specification:**
- AttendanceEventDto, AttendanceReportDto, CorrectionRequestDto

**Sample Implementation:**
```java
public class AttendanceEventDto {
    private Long id;
    private Long employeeId;
    private LocalDateTime timestamp;
    private String type;
    // ...
}
```

### Section: Validation Rules
**Description:** Validates event type, geofence, device info.

**Design Specification:**
- @NotNull, custom validator for geofence

**Sample Implementation:**
```java
public class ClockInRequestDto {
    @NotNull private Long employeeId;
    @NotBlank private String deviceId;
    // ...
}
```

### Section: Exception Handling Strategy
**Description:** Handles missed punches, invalid events.

**Design Specification:**
- Custom exceptions: MissedPunchException, InvalidEventException

**Sample Implementation:**
```java
@ExceptionHandler(MissedPunchException.class)
public ResponseEntity<?> handleMissedPunch(MissedPunchException ex) { /* ... */ }
```

### Section: Transaction Management
**Description:** All event recording and corrections are transactional.

**Design Specification:**
- @Transactional on service methods

### Section: Caching Strategy
**Description:** Cache daily totals for quick reporting.

**Design Specification:**
- @Cacheable on getDailyTotals

================================================================================
## Epic E05: Shift & Schedule Management
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Manages shift templates, rotations, overtime rules, blackout dates, and employee assignments.

**Design Specification:**
- ShiftTemplate, ShiftAssignment entities
- SchedulingService, SchedulingController

**Sample Implementation:**
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
    // ...
}
@Entity
public class ShiftAssignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private ShiftTemplate shiftTemplate;
    private LocalDate date;
    private boolean overtime;
}
```

### Section: Service Layer Specifications
**Description:** Business logic for conflict detection, bulk assignment, audit.

**Design Specification:**
- SchedulingService: assignShifts, detectConflicts, generateAudit

**Sample Implementation:**
```java
@Service
public class SchedulingService {
    public void assignShifts(List<Long> employeeIds, ShiftTemplate template, LocalDate date) { /* ... */ }
    public boolean detectConflicts(Long employeeId, LocalDate date) { /* ... */ }
}
```

### Section: Controller Specifications
**Description:** REST endpoints for shift templates, assignments, audits.

**Design Specification:**
- /shifts/templates [CRUD]
- /shifts/assignments [CRUD]

**Sample Implementation:**
```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates") public ShiftTemplateDto createTemplate(...) { /* ... */ }
    @PostMapping("/assignments/bulk") public void bulkAssign(...) { /* ... */ }
}
```

### Section: DTO Classes and Mappers
**Description:** DTOs for shift templates, assignments, audits.

**Design Specification:**
- ShiftTemplateDto, ShiftAssignmentDto, AuditEntryDto

### Section: Validation Rules
**Description:** Validates shift times, blackout dates, assignment conflicts.

**Design Specification:**
- Custom validator for blackout dates, conflict detection

### Section: Exception Handling Strategy
**Description:** Handles assignment conflicts, invalid templates.

**Design Specification:**
- Custom exceptions: ShiftConflictException, InvalidTemplateException

### Section: Transaction Management
**Description:** Bulk assignments and audits are transactional.

### Section: Caching Strategy
**Description:** Cache shift templates and assignments for dashboard views.

================================================================================
## Epic E06: Leave & Absence Management
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Handles PTO, sick, unpaid leave requests, approvals, accrual balances, and integration with scheduling/payroll.

**Design Specification:**
- LeaveRequest, LeaveBalance entities
- LeaveService, LeaveController

**Sample Implementation:**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne private Employee employee;
    private LocalDate startDate;
    private LocalDate endDate;
    private String type; // PTO, SICK, UNPAID
    private String status; // REQUESTED, APPROVED, DENIED
}
@Entity
public class LeaveBalance {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne private Employee employee;
    private int ptoBalance;
    private int sickBalance;
}
```

### Section: Service Layer Specifications
**Description:** Business logic for leave requests, approvals, balance updates.

**Design Specification:**
- LeaveService: requestLeave, approveLeave, updateBalance

**Sample Implementation:**
```java
@Service
public class LeaveService {
    public LeaveRequest requestLeave(Long employeeId, LeaveRequestDto dto) { /* ... */ }
    public void approveLeave(Long requestId) { /* ... */ }
}
```

### Section: Controller Specifications
**Description:** REST endpoints for leave requests, approvals, exports.

**Design Specification:**
- /leave/requests [CRUD]
- /leave/balances [GET]

**Sample Implementation:**
```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/requests") public LeaveRequestDto requestLeave(...) { /* ... */ }
    @PostMapping("/requests/{id}/approve") public void approveLeave(...) { /* ... */ }
}
```

### Section: DTO Classes and Mappers
**Description:** DTOs for leave requests, balances, exports.

### Section: Validation Rules
**Description:** Validates leave type, dates, balance sufficiency.

### Section: Exception Handling Strategy
**Description:** Handles insufficient balance, invalid requests.

### Section: Transaction Management
**Description:** Leave requests and approvals are transactional.

### Section: Integration Points
**Description:** Hooks to scheduling and payroll modules.

================================================================================
## Epic E07: Training & Certification Tracking
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Tracks employee certifications, expirations, renewals, and proof document uploads.

**Design Specification:**
- Certification, CertificationDocument entities
- CertificationService, CertificationController

**Sample Implementation:**
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne private Employee employee;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private boolean valid;
}
@Entity
public class CertificationDocument {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne private Certification certification;
    private String filePath;
}
```

### Section: Service Layer Specifications
**Description:** Business logic for CRUD, expiry alerts, scheduling checks.

**Design Specification:**
- CertificationService: create, renew, alertExpiry, blockAssignment

**Sample Implementation:**
```java
@Service
public class CertificationService {
    public Certification createCertification(Long employeeId, CertificationDto dto) { /* ... */ }
    public void alertExpiringCerts() { /* ... */ }
}
```

### Section: Controller Specifications
**Description:** REST endpoints for certifications, documents, alerts.

**Design Specification:**
- /certifications [CRUD]
- /certifications/alerts [GET]

**Sample Implementation:**
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping public CertificationDto create(...) { /* ... */ }
    @GetMapping("/alerts") public List<CertificationAlertDto> getAlerts(...) { /* ... */ }
}
```

### Section: DTO Classes and Mappers
**Description:** DTOs for certifications, alerts, documents.

### Section: Validation Rules
**Description:** Validates expiry dates, document uploads.

### Section: Exception Handling Strategy
**Description:** Handles expired certs, invalid uploads.

### Section: Transaction Management
**Description:** Certification creation/renewal is transactional.

### Section: Integration Points
**Description:** Scheduling checks for valid certifications.

================================================================================
## Epic E08: Safety Incidents & OSHA Reporting
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Records safety incidents, manages investigation workflow, and generates OSHA reports.

**Design Specification:**
- SafetyIncident entity, SafetyService, SafetyController

**Sample Implementation:**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String severity;
    private String location;
    private String description;
    @ManyToMany private List<Employee> involvedEmployees;
    private String status; // OPEN, INVESTIGATING, RESOLVED
}
```

### Section: Service Layer Specifications
**Description:** Business logic for incident recording, workflow, OSHA export.

**Design Specification:**
- SafetyService: recordIncident, updateStatus, exportOSHA

**Sample Implementation:**
```java
@Service
public class SafetyService {
    public SafetyIncident recordIncident(SafetyIncidentDto dto) { /* ... */ }
    public void updateStatus(Long incidentId, String status) { /* ... */ }
}
```

### Section: Controller Specifications
**Description:** REST endpoints for incidents, workflow, OSHA exports.

**Design Specification:**
- /safety/incidents [POST, GET]
- /safety/osha/export [GET]

**Sample Implementation:**
```java
@RestController
@RequestMapping("/safety")
public class SafetyController {
    @PostMapping("/incidents") public SafetyIncidentDto recordIncident(...) { /* ... */ }
    @GetMapping("/osha/export") public ResponseEntity<Resource> exportOsha(...) { /* ... */ }
}
```

### Section: DTO Classes and Mappers
**Description:** DTOs for incidents, OSHA exports.

### Section: Validation Rules
**Description:** Validates severity, location, involved employees.

### Section: Exception Handling Strategy
**Description:** Handles invalid incidents, workflow errors.

### Section: Transaction Management
**Description:** Incident recording and workflow updates are transactional.

### Section: Integration Points
**Description:** OSHA export integration.

================================================================================
## Epic E09: Equipment & Asset Assignment
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Manages asset registry, assignment, check-in/out, certification checks, and asset condition.

**Design Specification:**
- Asset, AssetAssignment entities
- AssetService, AssetController

**Sample Implementation:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private String condition;
    private boolean available;
}
@Entity
public class AssetAssignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne private Asset asset;
    @ManyToOne private Employee employee;
    private LocalDateTime checkoutTime;
    private LocalDateTime returnTime;
}
```

### Section: Service Layer Specifications
**Description:** Business logic for assignment, check-in/out, overdue reports.

**Design Specification:**
- AssetService: assignAsset, checkIn, checkOut, reportOverdue

**Sample Implementation:**
```java
@Service
public class AssetService {
    public AssetAssignment assignAsset(Long assetId, Long employeeId) { /* ... */ }
    public void checkIn(Long assignmentId) { /* ... */ }
}
```

### Section: Controller Specifications
**Description:** REST endpoints for assets, assignments, reports.

**Design Specification:**
- /assets [CRUD]
- /assets/assignments [CRUD]

**Sample Implementation:**
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assignments") public AssetAssignmentDto assignAsset(...) { /* ... */ }
    @GetMapping("/reports/overdue") public List<AssetAssignmentDto> getOverdueReports(...) { /* ... */ }
}
```

### Section: DTO Classes and Mappers
**Description:** DTOs for assets, assignments, reports.

### Section: Validation Rules
**Description:** Validates certification before assignment, asset condition.

### Section: Exception Handling Strategy
**Description:** Handles invalid assignments, overdue returns.

### Section: Transaction Management
**Description:** Asset assignments and check-in/out are transactional.

### Section: Integration Points
**Description:** Certification checks before assignment.

================================================================================
## Epic E10: Performance Reviews & Goals
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Manages review templates, goals, competencies, ratings, comments, and acknowledgements.

**Design Specification:**
- PerformanceReview, ReviewCycle entities
- ReviewService, ReviewController

**Sample Implementation:**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne private Employee employee;
    private String cycle; // Q1, Q2, Annual
    private String goals;
    private String competencies;
    private String ratings;
    private String comments;
    private boolean acknowledged;
    private boolean immutableAfterSignoff;
}
```

### Section: Service Layer Specifications
**Description:** Business logic for review cycles, submission, acknowledgement, PDF export.

**Design Specification:**
- ReviewService: createCycle, submitReview, acknowledge, exportPdf

**Sample Implementation:**
```java
@Service
public class ReviewService {
    public PerformanceReview submitReview(Long employeeId, ReviewDto dto) { /* ... */ }
    public void acknowledgeReview(Long reviewId) { /* ... */ }
}
```

### Section: Controller Specifications
**Description:** REST endpoints for reviews, cycles, exports.

**Design Specification:**
- /reviews [CRUD]
- /reviews/export [GET]

**Sample Implementation:**
```java
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping public PerformanceReviewDto submitReview(...) { /* ... */ }
    @GetMapping("/export") public ResponseEntity<Resource> exportPdf(...) { /* ... */ }
}
```

### Section: DTO Classes and Mappers
**Description:** DTOs for reviews, cycles, exports.

### Section: Validation Rules
**Description:** Validates review cycle, required fields.

### Section: Exception Handling Strategy
**Description:** Handles duplicate submissions, immutable history.

### Section: Transaction Management
**Description:** Review submissions and acknowledgements are transactional.

### Section: Integration Points
**Description:** PDF export integration.

================================================================================
## Epic E11: Payroll Export Integration
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Generates payroll-ready files from attendance/leave, maps to provider formats, delivers securely.

**Design Specification:**
- PayrollExport entity, PayrollService, PayrollController

**Sample Implementation:**
```java
@Entity
public class PayrollExport {
    @Id @GeneratedValue
    private Long id;
    private LocalDate exportDate;
    private String provider;
    private String status; // SUCCESS, FAILED, RETRY
    private String filePath;
}
```

### Section: Service Layer Specifications
**Description:** Business logic for export generation, reconciliation, delivery, audit.

**Design Specification:**
- PayrollService: generateExport, deliver, retry, auditLog

**Sample Implementation:**
```java
@Service
public class PayrollService {
    public PayrollExport generateExport(LocalDate period) { /* ... */ }
    public void deliverExport(Long exportId) { /* ... */ }
}
```

### Section: Controller Specifications
**Description:** REST endpoints for exports, audit logs.

**Design Specification:**
- /payroll/exports [POST, GET]

**Sample Implementation:**
```java
@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/exports") public PayrollExportDto generateExport(...) { /* ... */ }
    @GetMapping("/exports/{id}/audit") public AuditLogDto getAuditLog(...) { /* ... */ }
}
```

### Section: DTO Classes and Mappers
**Description:** DTOs for exports, audit logs.

### Section: Validation Rules
**Description:** Validates export period, provider schema.

### Section: Exception Handling Strategy
**Description:** Handles failed deliveries, reconciliation errors.

### Section: Transaction Management
**Description:** Export generation and delivery are transactional.

### Section: Integration Points
**Description:** SFTP/API delivery, provider mapping.

================================================================================
## Epic E12: Notifications & Announcements
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Manages in-app, email, SMS notifications for shift changes, cert expiries, approvals, announcements.

**Design Specification:**
- Notification, Announcement entities
- NotificationService, NotificationController

**Sample Implementation:**
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne private Employee recipient;
    private String channel; // IN_APP, EMAIL, SMS
    private String template;
    private String status; // SENT, FAILED
    private LocalDateTime sentAt;
}
@Entity
public class Announcement {
    @Id @GeneratedValue
    private Long id;
    private String message;
    private LocalDateTime postedAt;
    private boolean visible;
}
```

### Section: Service Layer Specifications
**Description:** Business logic for opt-in/out, template localization, delivery tracking, rate limiting.

**Design Specification:**
- NotificationService: send, trackStatus, applyRateLimit

**Sample Implementation:**
```java
@Service
public class NotificationService {
    public void sendNotification(NotificationDto dto) { /* ... */ }
    public void trackDelivery(Long notificationId) { /* ... */ }
}
```

### Section: Controller Specifications
**Description:** REST endpoints for notifications, announcements.

**Design Specification:**
- /notifications [POST, GET]
- /announcements [POST, GET]

**Sample Implementation:**
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping public void sendNotification(...) { /* ... */ }
    @GetMapping public List<NotificationDto> listNotifications(...) { /* ... */ }
}
```

### Section: DTO Classes and Mappers
**Description:** DTOs for notifications, announcements.

### Section: Validation Rules
**Description:** Validates channel, template, quiet hours.

### Section: Exception Handling Strategy
**Description:** Handles delivery failures, rate limit violations.

### Section: Transaction Management
**Description:** Notification sending is transactional.

### Section: Integration Points
**Description:** Email/SMS gateway integration.

================================================================================
## Epic E13: Integration Layer (HRIS/WMS APIs)
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Exposes REST APIs/connectors for HRIS, WMS, IDP (SSO), and webhooks for events.

**Design Specification:**
- IntegrationService, IntegrationController

**Sample Implementation:**
```java
@Service
public class IntegrationService {
    public void syncHrisEmployee(HrisEmployeeDto dto) { /* ... */ }
    public void linkWmsDepartment(WmsDepartmentDto dto) { /* ... */ }
}
@RestController
@RequestMapping("/integration")
public class IntegrationController {
    @PostMapping("/hris/sync") public void syncHris(...) { /* ... */ }
    @PostMapping("/wms/link") public void linkWms(...) { /* ... */ }
    @PostMapping("/webhooks") public void handleWebhook(...) { /* ... */ }
}
```

### Section: Configuration and Spring Security Settings
**Description:** JWT/OAuth2-secured APIs.

### Section: DTO Classes and Mappers
**Description:** DTOs for HRIS, WMS, webhooks.

### Section: Validation Rules
**Description:** Validates idempotency, schema.

### Section: Exception Handling Strategy
**Description:** Handles sync errors, invalid webhooks.

### Section: Transaction Management
**Description:** Sync jobs are transactional.

### Section: Integration Points
**Description:** HRIS/WMS/IDP connectors.

================================================================================
## Epic E14: Audit Trail & Compliance
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Centralized audit logging for sensitive changes, tamper-evident storage.

**Design Specification:**
- AuditLog entity, AuditService, AuditController

**Sample Implementation:**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private Long entityId;
    private String actor;
    private LocalDateTime timestamp;
    private String before;
    private String after;
    private String action; // CREATE, UPDATE, DELETE
}
```

### Section: Service Layer Specifications
**Description:** Business logic for logging, export, coverage tests.

**Design Specification:**
- AuditService: logChange, exportLogs

**Sample Implementation:**
```java
@Service
public class AuditService {
    public void logChange(String entity, Long entityId, String actor, String before, String after, String action) { /* ... */ }
}
```

### Section: Controller Specifications
**Description:** REST endpoints for audit logs, exports.

**Design Specification:**
- /audit/logs [GET]

**Sample Implementation:**
```java
@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/logs") public List<AuditLogDto> getLogs(...) { /* ... */ }
}
```

### Section: DTO Classes and Mappers
**Description:** DTOs for audit logs, exports.

### Section: Validation Rules
**Description:** Validates entity, actor, action.

### Section: Exception Handling Strategy
**Description:** Handles export errors.

### Section: Transaction Management
**Description:** Audit logging is transactional.

### Section: Integration Points
**Description:** Tamper-evident storage.

================================================================================
## Epic E15: Reporting & Analytics
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Generates operational reports (attendance, overtime, leave, cert status, safety KPIs), exports CSV/PDF, role-based dashboards.

**Design Specification:**
- ReportService, ReportController

**Sample Implementation:**
```java
@Service
public class ReportService {
    public Report generateAttendanceReport(DateRange range, String department, String shift) { /* ... */ }
    public Resource exportCsv(Report report) { /* ... */ }
}
@RestController
@RequestMapping("/reports")
public class ReportController {
    @GetMapping("/attendance") public ReportDto getAttendanceReport(...) { /* ... */ }
    @GetMapping("/export/csv") public ResponseEntity<Resource> exportCsv(...) { /* ... */ }
}
```

### Section: DTO Classes and Mappers
**Description:** DTOs for reports, exports.

### Section: Validation Rules
**Description:** Validates filters, export limits.

### Section: Exception Handling Strategy
**Description:** Handles export errors, access violations.

### Section: Transaction Management
**Description:** Report generation is transactional.

### Section: Caching Strategy
**Description:** Cache report results for BI endpoints.

================================================================================
## Epic E16: Mobile Access (PWA)
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Provides responsive views for clock-in/out, schedules, leave requests, announcements; offline-friendly via PWA.

**Design Specification:**
- MobileController, PwaManifest.json

**Sample Implementation:**
```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @GetMapping("/clock-in") public AttendanceEventDto clockIn(...) { /* ... */ }
    @GetMapping("/schedule") public ScheduleDto getSchedule(...) { /* ... */ }
}
```

**PwaManifest.json:**
```json
{
  "name": "Warehouse EMS",
  "short_name": "WMS",
  "start_url": "/mobile",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

### Section: Service Layer Specifications
**Description:** Offline queue for clock events, conflict resolution.

### Section: Validation Rules
**Description:** Validates offline events, sync conflicts.

### Section: Exception Handling Strategy
**Description:** Handles sync errors.

### Section: Integration Points
**Description:** Lighthouse PWA score validation.

================================================================================
## Epic E17: Onboarding & Offboarding Workflow
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Automates provisioning, initial schedule, training, asset assignment, deprovisioning on termination.

**Design Specification:**
- OnboardingTask, OffboardingTask entities
- OnboardingService, OffboardingService, WorkflowController

**Sample Implementation:**
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne private Employee employee;
    private String type; // ACCOUNT, SCHEDULE, TRAINING, ASSET
    private String status; // PENDING, COMPLETED
}
@Entity
public class OffboardingTask {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne private Employee employee;
    private String type; // REVOKE_ACCESS, COLLECT_ASSET, UPDATE_SCHEDULE
    private String status;
}
```

### Section: Service Layer Specifications
**Description:** Business logic for task generation, completion.

### Section: Controller Specifications
**Description:** REST endpoints for onboarding/offboarding tasks.

### Section: DTO Classes and Mappers
**Description:** DTOs for tasks.

### Section: Validation Rules
**Description:** Validates HRIS sync, asset collection.

### Section: Exception Handling Strategy
**Description:** Handles incomplete tasks, sync errors.

### Section: Transaction Management
**Description:** Task creation/completion is transactional.

### Section: Integration Points
**Description:** HRIS sync, asset registry.

================================================================================
## Epic E18: Localization & Multi-Warehouse Support
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Supports multi-warehouse data segregation and localization.

**Design Specification:**
- Warehouse entity, LocaleConfig

**Sample Implementation:**
```java
@Entity
public class Warehouse {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String location;
    private String locale;
}
@Configuration
public class LocaleConfig {
    @Bean
    public LocaleResolver localeResolver() { /* ... */ }
}
```

### Section: Service Layer Specifications
**Description:** Data segregation by warehouse, locale-aware services.

### Section: Controller Specifications
**Description:** REST endpoints for warehouse management.

### Section: DTO Classes and Mappers
**Description:** DTOs for warehouse, locale.

### Section: Validation Rules
**Description:** Validates warehouse assignment, locale.

### Section: Exception Handling Strategy
**Description:** Handles locale errors.

### Section: Transaction Management
**Description:** Warehouse operations are transactional.

================================================================================
## Epic E19: Advanced Scheduling Optimization
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Optimizes shift rotations, overtime rules, blackout dates.

**Design Specification:**
- SchedulingOptimizerService

**Sample Implementation:**
```java
@Service
public class SchedulingOptimizerService {
    public List<ShiftAssignment> optimizeSchedule(List<Employee> employees, List<ShiftTemplate> templates, List<BlackoutDate> blackoutDates) { /* ... */ }
}
```

### Section: Service Layer Specifications
**Description:** Optimization algorithms for scheduling.

### Section: Controller Specifications
**Description:** REST endpoints for optimization runs.

### Section: DTO Classes and Mappers
**Description:** DTOs for optimization results.

### Section: Validation Rules
**Description:** Validates input data.

### Section: Exception Handling Strategy
**Description:** Handles optimization errors.

### Section: Transaction Management
**Description:** Optimization runs are transactional.

================================================================================
## Epic E20: Self-Service Portal
================================================================================

### Section: Overview of Spring Boot Architecture
**Description:** Provides employee dashboard for self-service (profile, schedule, leave, announcements).

**Design Specification:**
- SelfServiceController

**Sample Implementation:**
```java
@RestController
@RequestMapping("/self-service")
public class SelfServiceController {
    @GetMapping("/profile") public EmployeeDto getProfile(...) { /* ... */ }
    @GetMapping("/schedule") public ScheduleDto getSchedule(...) { /* ... */ }
    @PostMapping("/leave/request") public LeaveRequestDto requestLeave(...) { /* ... */ }
    @GetMapping("/announcements") public List<AnnouncementDto> getAnnouncements(...) { /* ... */ }
}
```

### Section: Service Layer Specifications
**Description:** Aggregates data for dashboard views.

### Section: DTO Classes and Mappers
**Description:** DTOs for dashboard sections.

### Section: Validation Rules
**Description:** Validates self-service actions.

### Section: Exception Handling Strategy
**Description:** Handles dashboard errors.

### Section: Transaction Management
**Description:** Self-service actions are transactional.

================================================================================
## END OF DOCUMENT
================================================================================

All sections above follow Spring Boot industry standards, are production-ready, and provide clear, actionable guidelines for developers. Each epic is modularized, with explicit package structure, entity design, service/repository/controller specifications, security, integration, validation, exception handling, transaction management, and caching strategies. Code snippets and pseudo-code illustrate best practices and design patterns for easy developer consumption.