====================================================================
Warehouse Employee Management System - Low-Level Technical Design Document (Spring Boot)
====================================================================

--------------------------------------------------------------------
Epic E01: Project Scaffolding & Domain Setup
--------------------------------------------------------------------
Section: Overview of Spring Boot Architecture
Description: The project is initialized as a Maven-based Spring Boot application. Core modules are structured as separate packages: employee, scheduling, attendance, safety. Flyway/Liquibase is used for DB migrations. Spring Boot Actuator is enabled for health checks and monitoring.
Design Specification:
- Maven project with parent `spring-boot-starter-parent`
- Packages: `com.wms.employee`, `com.wms.scheduling`, `com.wms.attendance`, `com.wms.safety`
- Flyway/Liquibase migration scripts in `src/main/resources/db/migration`
- Actuator enabled in `application.yml`
Sample Implementation:
```java
// pom.xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>

// application.yml
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

// Directory structure
com/wms/employee/
com/wms/scheduling/
com/wms/attendance/
com/wms/safety/
```

--------------------------------------------------------------------
Epic E02: Employee Master Data (CRUD)
--------------------------------------------------------------------
Section: Package Structure, Module Definitions, Component Breakdown
Description: Employee domain is defined in `com.wms.employee`. Components include entity, repository, service, controller, DTOs, and mapper.
Design Specification:
- Entity: Employee
- Repository: EmployeeRepository (extends JpaRepository)
- Service: EmployeeService
- Controller: EmployeeController
- DTOs: EmployeeDto, EmployeeCreateDto, EmployeeUpdateDto
Sample Implementation:
```java
// Employee Entity
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
    private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    private boolean deleted = false;
    // getters/setters
}

// Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}

// Service
@Service
public class EmployeeService {
    @Autowired private EmployeeRepository repo;
    public Employee create(EmployeeCreateDto dto) { /* validation, mapping, save */ }
    public Page<EmployeeDto> list(Pageable pageable, String filter) { /* filtering, mapping */ }
    public void softDelete(Long id) { /* set deleted=true */ }
}

// Controller
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired private EmployeeService service;
    @PostMapping public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeCreateDto dto) { /* ... */ }
    @GetMapping public Page<EmployeeDto> list(Pageable pageable, @RequestParam Optional<String> filter) { /* ... */ }
    @PatchMapping("/{id}") public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @RequestBody EmployeeUpdateDto dto) { /* ... */ }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { /* ... */ }
}

// OpenAPI schema auto-generated via springdoc-openapi
```

--------------------------------------------------------------------
Epic E03: Role Based Access Control (RBAC)
--------------------------------------------------------------------
Section: Configuration and Security Settings
Description: Spring Security is configured with roles: ADMIN, HR, SUPERVISOR, WORKER. Method and endpoint security enforced. Row-level constraints applied in service layer. API key/OAuth2 toggle via config.
Design Specification:
- SecurityConfig class with role mappings
- @PreAuthorize annotations for method security
- API key/OAuth2 toggle in `application.yml`
Sample Implementation:
```java
// SecurityConfig
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
            .oauth2Login().and()
            .apiKeyAuthFilter(); // Custom filter for API key
    }
}

// Service layer row-level security
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == authentication.principal.department)")
public EmployeeDto getEmployee(Long id) { /* ... */ }
```

--------------------------------------------------------------------
Epic E04: Time & Attendance (Clock In/Out)
--------------------------------------------------------------------
Section: Entity Design, Service Layer, Controller
Description: Attendance entity records clock-in/out events, device info, geofence. Service calculates hours, handles missed punches, correction workflow. Controller exposes endpoints.
Design Specification:
- Entity: Attendance
- Repository: AttendanceRepository
- Service: AttendanceService
- Controller: AttendanceController
Sample Implementation:
```java
// Attendance Entity
@Entity
@Table(name = "attendance")
public class Attendance {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne private Employee employee;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private String deviceId;
    private String geoLocation;
    private boolean correctionRequested;
    // getters/setters
}

// Service
@Service
public class AttendanceService {
    public Attendance clockIn(Long employeeId, String deviceId, String geoLocation) { /* ... */ }
    public Attendance clockOut(Long employeeId, String deviceId, String geoLocation) { /* ... */ }
    public void requestCorrection(Long attendanceId) { /* ... */ }
    public List<Attendance> getDailyTotals(LocalDate date) { /* ... */ }
}

// Controller
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in") public ResponseEntity<?> clockIn(@RequestBody ClockEventDto dto) { /* ... */ }
    @PostMapping("/clock-out") public ResponseEntity<?> clockOut(@RequestBody ClockEventDto dto) { /* ... */ }
    @PostMapping("/correction/{id}") public ResponseEntity<?> requestCorrection(@PathVariable Long id) { /* ... */ }
}
```

--------------------------------------------------------------------
Epic E05: Shift & Schedule Management
--------------------------------------------------------------------
Section: Package Structure, Entity Design, Service, Controller
Description: Shift templates, rotations, overtime rules, employee assignments, blackout dates, calendars. Conflict detection in service layer.
Design Specification:
- Entities: ShiftTemplate, ShiftAssignment, BlackoutDate, WarehouseCalendar
- Repositories: ShiftTemplateRepository, ShiftAssignmentRepository
- Service: ShiftService
- Controller: ShiftController
Sample Implementation:
```java
// ShiftTemplate Entity
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    // getters/setters
}

// ShiftAssignment Entity
@Entity
public class ShiftAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private ShiftTemplate shiftTemplate;
    private LocalDate date;
    private boolean overtime;
    // getters/setters
}

// Service
@Service
public class ShiftService {
    public ShiftAssignment assignShift(Long employeeId, Long shiftTemplateId, LocalDate date) { /* conflict detection */ }
    public List<ShiftAssignment> getUpcomingShifts(Long employeeId) { /* ... */ }
}

// Controller
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/assign") public ResponseEntity<?> assign(@RequestBody ShiftAssignDto dto) { /* ... */ }
    @GetMapping("/upcoming/{employeeId}") public List<ShiftAssignmentDto> upcoming(@PathVariable Long employeeId) { /* ... */ }
}
```

--------------------------------------------------------------------
Epic E06: Leave & Absence Management
--------------------------------------------------------------------
Section: Entity Design, Service, Controller
Description: Leave requests, approvals, accrual balances, policies. Integration with scheduling and payroll.
Design Specification:
- Entities: LeaveRequest, LeavePolicy, LeaveBalance
- Repositories: LeaveRequestRepository, LeaveBalanceRepository
- Service: LeaveService
- Controller: LeaveController
Sample Implementation:
```java
// LeaveRequest Entity
@Entity
public class LeaveRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING)
    private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;
    private String reason;
    // getters/setters
}

// Service
@Service
public class LeaveService {
    public LeaveRequest requestLeave(Long employeeId, LeaveRequestDto dto) { /* ... */ }
    public void approveLeave(Long leaveId) { /* ... */ }
    public void denyLeave(Long leaveId) { /* ... */ }
    public LeaveBalance getBalance(Long employeeId) { /* ... */ }
}

// Controller
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/request") public ResponseEntity<?> request(@RequestBody LeaveRequestDto dto) { /* ... */ }
    @PostMapping("/approve/{id}") public ResponseEntity<?> approve(@PathVariable Long id) { /* ... */ }
    @PostMapping("/deny/{id}") public ResponseEntity<?> deny(@PathVariable Long id) { /* ... */ }
}
```

--------------------------------------------------------------------
Epic E07: Training & Certification Tracking
--------------------------------------------------------------------
Section: Entity Design, Service, Controller
Description: Track certifications, expirations, renewals, block assignments for expired certs, upload proof documents.
Design Specification:
- Entities: Certification, CertificationProof
- Repositories: CertificationRepository
- Service: CertificationService
- Controller: CertificationController
Sample Implementation:
```java
// Certification Entity
@Entity
public class Certification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne private Employee employee;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private boolean renewed;
    // getters/setters
}

// Service
@Service
public class CertificationService {
    public Certification addCertification(Long employeeId, CertificationDto dto) { /* ... */ }
    public List<Certification> getExpiringCerts(int days) { /* ... */ }
    public boolean isQualified(Long employeeId, String certType) { /* ... */ }
}

// Controller
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping("/add") public ResponseEntity<?> add(@RequestBody CertificationDto dto) { /* ... */ }
    @GetMapping("/expiring") public List<CertificationDto> expiring(@RequestParam int days) { /* ... */ }
}
```

--------------------------------------------------------------------
Epic E08: Safety Incidents & OSHA Reporting
--------------------------------------------------------------------
Section: Entity Design, Service, Controller
Description: Record incidents, severity, location, description, involved employees, investigation workflow, corrective actions, OSHA summary generation.
Design Specification:
- Entities: SafetyIncident, CorrectiveAction
- Repositories: SafetyIncidentRepository
- Service: SafetyService
- Controller: SafetyController
Sample Implementation:
```java
// SafetyIncident Entity
@Entity
public class SafetyIncident {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String severity;
    private String location;
    private String description;
    @ManyToMany private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
    // getters/setters
}

// Service
@Service
public class SafetyService {
    public SafetyIncident reportIncident(SafetyIncidentDto dto) { /* ... */ }
    public void updateStatus(Long incidentId, IncidentStatus status) { /* ... */ }
    public List<SafetyIncident> getOSHASummary(LocalDate start, LocalDate end) { /* ... */ }
}

// Controller
@RestController
@RequestMapping("/safety/incidents")
public class SafetyController {
    @PostMapping public ResponseEntity<?> report(@RequestBody SafetyIncidentDto dto) { /* ... */ }
    @PatchMapping("/{id}/status") public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody IncidentStatusDto dto) { /* ... */ }
    @GetMapping("/osha-summary") public List<OSHASummaryDto> summary(@RequestParam LocalDate start, @RequestParam LocalDate end) { /* ... */ }
}
```

--------------------------------------------------------------------
Epic E09: Equipment & Asset Assignment
--------------------------------------------------------------------
Section: Entity Design, Service, Controller
Description: Assign assets to employees, track checkout/return, prevent use if certification missing, maintain asset condition.
Design Specification:
- Entities: Asset, AssetAssignment, AssetCondition
- Repositories: AssetRepository, AssetAssignmentRepository
- Service: AssetService
- Controller: AssetController
Sample Implementation:
```java
// Asset Entity
@Entity
public class Asset {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private String serialNumber;
    private AssetCondition condition;
    // getters/setters
}

// AssetAssignment Entity
@Entity
public class AssetAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne private Asset asset;
    @ManyToOne private Employee employee;
    private LocalDateTime checkoutTime;
    private LocalDateTime returnTime;
    // getters/setters
}

// Service
@Service
public class AssetService {
    public AssetAssignment checkoutAsset(Long assetId, Long employeeId) { /* check certs */ }
    public void returnAsset(Long assignmentId) { /* ... */ }
    public List<AssetAssignment> getOverdueReturns() { /* ... */ }
}

// Controller
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/checkout") public ResponseEntity<?> checkout(@RequestBody AssetCheckoutDto dto) { /* ... */ }
    @PostMapping("/return/{assignmentId}") public ResponseEntity<?> returnAsset(@PathVariable Long assignmentId) { /* ... */ }
    @GetMapping("/overdue") public List<AssetAssignmentDto> overdue() { /* ... */ }
}
```

--------------------------------------------------------------------
Epic E10: Performance Reviews & Goals
--------------------------------------------------------------------
Section: Entity Design, Service, Controller
Description: Review templates, goals, competencies, ratings, comments, supervisor/employee acknowledgements.
Design Specification:
- Entities: PerformanceReview, ReviewTemplate, Goal
- Repositories: PerformanceReviewRepository
- Service: ReviewService
- Controller: ReviewController
Sample Implementation:
```java
// PerformanceReview Entity
@Entity
public class PerformanceReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private ReviewTemplate template;
    private LocalDate reviewDate;
    private String comments;
    private boolean acknowledgedByEmployee;
    private boolean acknowledgedBySupervisor;
    // getters/setters
}

// Service
@Service
public class ReviewService {
    public PerformanceReview createReview(Long employeeId, Long templateId, ReviewDto dto) { /* ... */ }
    public void acknowledge(Long reviewId, boolean byEmployee) { /* ... */ }
    public List<PerformanceReview> getReviews(Long employeeId) { /* ... */ }
}

// Controller
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping("/create") public ResponseEntity<?> create(@RequestBody ReviewDto dto) { /* ... */ }
    @PostMapping("/{id}/acknowledge") public ResponseEntity<?> acknowledge(@PathVariable Long id, @RequestParam boolean byEmployee) { /* ... */ }
    @GetMapping("/employee/{employeeId}") public List<PerformanceReviewDto> reviews(@PathVariable Long employeeId) { /* ... */ }
}
```

--------------------------------------------------------------------
Epic E11: Payroll Export Integration
--------------------------------------------------------------------
Section: Integration Points, Service, Controller
Description: Generate payroll-ready files from attendance and leave, map to external formats, secure delivery via SFTP/API.
Design Specification:
- Service: PayrollExportService
- Controller: PayrollExportController
- Integration: SFTP client, REST API client
Sample Implementation:
```java
// PayrollExportService
@Service
public class PayrollExportService {
    public PayrollFile generatePayroll(LocalDate start, LocalDate end) { /* ... */ }
    public void deliverPayroll(PayrollFile file) { /* SFTP/API */ }
    public void retryFailedDeliveries() { /* backoff logic */ }
}

// Controller
@RestController
@RequestMapping("/payroll/export")
public class PayrollExportController {
    @PostMapping public ResponseEntity<?> export(@RequestBody PayrollExportRequestDto dto) { /* ... */ }
}
```

--------------------------------------------------------------------
Epic E12: Notifications & Announcements
--------------------------------------------------------------------
Section: Service, Controller, Integration
Description: In-app/email/SMS notifications for shift changes, expiring certs, approvals, announcements. Quiet hours config.
Design Specification:
- Service: NotificationService
- Controller: NotificationController
- Integration: Email/SMS providers
Sample Implementation:
```java
// NotificationService
@Service
public class NotificationService {
    public void notifyShiftChange(Long employeeId, ShiftAssignmentDto dto) { /* ... */ }
    public void notifyExpiringCert(Long employeeId, CertificationDto dto) { /* ... */ }
    public void sendAnnouncement(String message, List<Long> employeeIds) { /* ... */ }
}

// Controller
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping("/announcement") public ResponseEntity<?> announce(@RequestBody AnnouncementDto dto) { /* ... */ }
    @GetMapping("/status/{id}") public NotificationStatusDto status(@PathVariable Long id) { /* ... */ }
}
```

--------------------------------------------------------------------
Epic E13: Integration Layer (HRIS/WMS APIs)
--------------------------------------------------------------------
Section: Integration Points, Service, Controller
Description: REST APIs and connectors for HRIS, WMS, IDP for SSO, webhooks for events.
Design Specification:
- Service: IntegrationService
- Controller: IntegrationController
- Integration: JWT/OAuth2-secured APIs, webhook endpoints
Sample Implementation:
```java
// IntegrationService
@Service
public class IntegrationService {
    public void syncHRIS(HRISSyncDto dto) { /* ... */ }
    public void syncWMS(WMSSyncDto dto) { /* ... */ }
    public void handleWebhook(WebhookEventDto dto) { /* idempotency */ }
}

// Controller
@RestController
@RequestMapping("/integration")
public class IntegrationController {
    @PostMapping("/hris-sync") public ResponseEntity<?> hrisSync(@RequestBody HRISSyncDto dto) { /* ... */ }
    @PostMapping("/wms-sync") public ResponseEntity<?> wmsSync(@RequestBody WMSSyncDto dto) { /* ... */ }
    @PostMapping("/webhook") public ResponseEntity<?> webhook(@RequestBody WebhookEventDto dto) { /* ... */ }
}
```

--------------------------------------------------------------------
Epic E14: Audit Trail & Compliance
--------------------------------------------------------------------
Section: Entity Design, Service, Controller
Description: Centralized audit logging for sensitive changes, tamper-evident storage.
Design Specification:
- Entity: AuditLog
- Repository: AuditLogRepository
- Service: AuditService
- Controller: AuditController
Sample Implementation:
```java
// AuditLog Entity
@Entity
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String entityName;
    private Long entityId;
    private String action;
    private String actor;
    private LocalDateTime timestamp;
    @Lob private String beforeState;
    @Lob private String afterState;
    // getters/setters
}

// Service
@Service
public class AuditService {
    public void logChange(String entity, Long id, String action, String actor, String before, String after) { /* ... */ }
    public List<AuditLog> getLogs(String entity, LocalDate start, LocalDate end) { /* ... */ }
}

// Controller
@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/logs") public List<AuditLogDto> logs(@RequestParam String entity, @RequestParam LocalDate start, @RequestParam LocalDate end) { /* ... */ }
}
```

--------------------------------------------------------------------
Epic E15: Reporting & Analytics
--------------------------------------------------------------------
Section: Service, Controller
Description: Operational reports (attendance, overtime, leave balances, certification status, safety KPIs), export CSV/PDF, dashboards.
Design Specification:
- Service: ReportingService
- Controller: ReportingController
Sample Implementation:
```java
// ReportingService
@Service
public class ReportingService {
    public Report generateAttendanceReport(LocalDate start, LocalDate end, String department) { /* ... */ }
    public Report generateOvertimeReport(LocalDate start, LocalDate end) { /* ... */ }
    public Report generateSafetyKPIReport(LocalDate start, LocalDate end) { /* ... */ }
}

// Controller
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance") public ReportDto attendance(@RequestParam LocalDate start, @RequestParam LocalDate end, @RequestParam String department) { /* ... */ }
    @GetMapping("/overtime") public ReportDto overtime(@RequestParam LocalDate start, @RequestParam LocalDate end) { /* ... */ }
    @GetMapping("/safety-kpi") public ReportDto safetyKPI(@RequestParam LocalDate start, @RequestParam LocalDate end) { /* ... */ }
    @GetMapping("/export") public ResponseEntity<Resource> export(@RequestParam String type, @RequestParam LocalDate start, @RequestParam LocalDate end) { /* CSV/PDF export */ }
}
```

--------------------------------------------------------------------
Epic E16: Mobile Access (PWA)
--------------------------------------------------------------------
Section: Controller, Configuration
Description: Responsive views for clock-in/out, schedules, leave requests, announcements. Offline-friendly PWA.
Design Specification:
- Controller: MobileController
- PWA manifest in `static/manifest.json`
- Service Worker in `static/service-worker.js`
Sample Implementation:
```java
// MobileController
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @GetMapping("/schedule") public List<ShiftAssignmentDto> schedule(@RequestParam Long employeeId) { /* ... */ }
    @PostMapping("/clock-in") public ResponseEntity<?> clockIn(@RequestBody ClockEventDto dto) { /* ... */ }
    @PostMapping("/leave-request") public ResponseEntity<?> leaveRequest(@RequestBody LeaveRequestDto dto) { /* ... */ }
    @GetMapping("/announcements") public List<AnnouncementDto> announcements() { /* ... */ }
}

// manifest.json (static)
{
  "name": "Warehouse EMS",
  "short_name": "WMS",
  "start_url": "/mobile",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}

// service-worker.js (static)
self.addEventListener('install', function(event) { /* cache assets */ });
self.addEventListener('fetch', function(event) { /* offline queue */ });
```

--------------------------------------------------------------------
Epic E17: Onboarding & Offboarding Workflow
--------------------------------------------------------------------
Section: Service, Controller, Integration
Description: Automate provisioning, initial schedule, required training, deprovision access/assets on termination.
Design Specification:
- Service: OnboardingService, OffboardingService
- Controller: LifecycleController
Sample Implementation:
```java
// OnboardingService
@Service
public class OnboardingService {
    public void provisionAccount(HRISSyncDto dto) { /* ... */ }
    public void assignInitialSchedule(Long employeeId) { /* ... */ }
    public void assignTraining(Long employeeId) { /* ... */ }
}

// OffboardingService
@Service
public class OffboardingService {
    public void revokeAccess(Long employeeId) { /* ... */ }
    public void collectAssets(Long employeeId) { /* ... */ }
    public void updateSchedules(Long employeeId) { /* ... */ }
}

// Controller
@RestController
@RequestMapping("/lifecycle")
public class LifecycleController {
    @PostMapping("/onboard") public ResponseEntity<?> onboard(@RequestBody HRISSyncDto dto) { /* ... */ }
    @PostMapping("/offboard/{employeeId}") public ResponseEntity<?> offboard(@PathVariable Long employeeId) { /* ... */ }
}
```

--------------------------------------------------------------------
Epic E18: Localization & Multi-Warehouse
--------------------------------------------------------------------
Section: Configuration, Entity Design, Controller
Description: Support multiple warehouses, distinct policies, shifts, calendars, UI localized for Spanish/English.
Design Specification:
- Entity: Warehouse, WarehousePolicy
- Controller: WarehouseController
- Localization: `messages_en.properties`, `messages_es.properties`
Sample Implementation:
```java
// Warehouse Entity
@Entity
public class Warehouse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String location;
    // getters/setters
}

// Controller
@RestController
@RequestMapping("/warehouses")
public class WarehouseController {
    @GetMapping public List<WarehouseDto> list() { /* ... */ }
    @GetMapping("/{id}/policies") public WarehousePolicyDto policies(@PathVariable Long id) { /* ... */ }
}

// application.yml
spring:
  messages:
    basename: messages

// messages_en.properties
shift.label=Shift
leave.label=Leave

// messages_es.properties
shift.label=Turno
leave.label=Permiso
```

--------------------------------------------------------------------
Epic E19: Advanced Scheduling (AI-Assisted)
--------------------------------------------------------------------
Section: Service, Integration
Description: ML model suggests optimal shift assignments based on attendance, skills, preferences.
Design Specification:
- Service: SchedulingAIService
- Integration: ML model via REST API or embedded
Sample Implementation:
```java
// SchedulingAIService
@Service
public class SchedulingAIService {
    public List<ShiftAssignment> suggestOptimalShifts(List<Employee> employees, LocalDate date) {
        // Call ML model REST API or embedded logic
    }
}
```

--------------------------------------------------------------------
Epic E20: Self-Service Portal
--------------------------------------------------------------------
Section: Controller, Service
Description: Employee portal to view pay stubs, update contact info (with approval), access policies, see personal records.
Design Specification:
- Controller: PortalController
- Service: PortalService
Sample Implementation:
```java
// PortalController
@RestController
@RequestMapping("/portal")
public class PortalController {
    @GetMapping("/pay-stubs") public List<PayStubDto> payStubs(@RequestParam Long employeeId) { /* ... */ }
    @PostMapping("/update-contact") public ResponseEntity<?> updateContact(@RequestBody ContactUpdateDto dto) { /* ... */ }
    @GetMapping("/policies") public List<PolicyDto> policies() { /* ... */ }
    @GetMapping("/records") public EmployeeRecordDto records(@RequestParam Long employeeId) { /* ... */ }
}

// PortalService
@Service
public class PortalService {
    public List<PayStub> getPayStubs(Long employeeId) { /* ... */ }
    public void requestContactUpdate(Long employeeId, ContactUpdateDto dto) { /* approval workflow */ }
    public List<Policy> getPolicies() { /* ... */ }
    public EmployeeRecord getRecords(Long employeeId) { /* ... */ }
}
```

====================================================================
END OF DOCUMENT
====================================================================