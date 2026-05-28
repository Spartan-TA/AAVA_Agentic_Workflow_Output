Section: Project Scaffolding & Domain Setup
Description: Establishes the foundational Spring Boot project structure, configures core modules, and sets up essential infrastructure for all subsequent features.
Design Specification:
- Spring Boot (Maven) project with Java 17+.
- Base packages: com.companyname.warehouse (subpackages: employee, scheduling, attendance, safety, config, common).
- Core modules: employee, scheduling, attendance, safety.
- DB migration: Flyway or Liquibase configured in src/main/resources/db/migration.
- Actuator enabled for health checks.
- README with build/run instructions.
- Application runs on port 8080.
Sample Implementation:
```java
// pom.xml: Spring Boot Starter, Spring Data JPA, Flyway, Actuator dependencies
// src/main/java/com/companyname/warehouse/WarehouseApplication.java
@SpringBootApplication
public class WarehouseApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseApplication.class, args);
    }
}
// src/main/resources/application.yml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse
    username: warehouse
    password: secret
  flyway:
    enabled: true
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

Section: Employee Master Data (CRUD)
Description: Implements the Employee domain with full CRUD REST APIs, enforcing unique badge IDs, soft-deletes, pagination/filtering, and OpenAPI documentation.
Design Specification:
- Package: com.companyname.warehouse.employee
- Entity: Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService (CRUD, filtering, soft-delete)
- Controller: EmployeeController (REST endpoints)
- DTOs: EmployeeDto, EmployeeCreateDto, EmployeeUpdateDto
- OpenAPI annotations for schema/examples
- Soft-delete via 'deleted' boolean
- Pagination: Pageable in GET endpoints
Sample Implementation:
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue private Long id;
    private String name;
    @Column(name="badge_id", nullable=false, unique=true) private String badgeId;
    private String role, department, shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted = false;
    // getters/setters
}
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee CRUD APIs")
public class EmployeeController {
    @PostMapping public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeCreateDto dto) { ... }
    @GetMapping public Page<EmployeeDto> list(Pageable pageable, @RequestParam Map<String,String> filters) { ... }
    @GetMapping("/{id}") public EmployeeDto get(@PathVariable Long id) { ... }
    @PutMapping("/{id}") public EmployeeDto update(@PathVariable Long id, @RequestBody EmployeeUpdateDto dto) { ... }
    @PatchMapping("/{id}/soft-delete") public void softDelete(@PathVariable Long id) { ... }
    @DeleteMapping("/{id}") public void hardDelete(@PathVariable Long id) { ... }
}
```

Section: Role Based Access Control (RBAC)
Description: Secures endpoints and data with Spring Security, supporting ADMIN, HR, SUPERVISOR, WORKER roles, method/endpoint security, and row-level constraints.
Design Specification:
- Package: com.companyname.warehouse.config.security
- SecurityConfig: Configures roles, endpoint/method security, API key/OAuth2 toggle via properties.
- Uses @PreAuthorize on service/controller methods.
- Row-level filtering in repositories/services.
- Unauthorized (401) and forbidden (403) handling.
Sample Implementation:
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
          .csrf().disable()
          .authorizeRequests()
            .antMatchers("/actuator/**").permitAll()
            .antMatchers(HttpMethod.POST, "/employees/**").hasRole("ADMIN")
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .anyRequest().authenticated()
          .and()
            .oauth2ResourceServer().jwt();
    }
}
@Service
public class EmployeeService {
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(#id, authentication))")
    public EmployeeDto getEmployee(Long id) { ... }
}
```

Section: Time & Attendance (Clock In/Out)
Description: Provides endpoints for clock-in/out, capturing geofence/device, calculating hours, handling missed punches/corrections, and exporting reports.
Design Specification:
- Package: com.companyname.warehouse.attendance
- Entity: AttendanceEvent (id, employee, type, timestamp, deviceId, location, shift, status)
- Repository: AttendanceRepository
- Service: AttendanceService (clock-in/out, calculate totals, corrections)
- Controller: AttendanceController
- Correction workflow: ApprovalTask entity
- CSV export endpoint
Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String type; // CLOCK_IN, CLOCK_OUT
    private Instant timestamp;
    private String deviceId, location;
    @ManyToOne private Shift shift;
    private String status; // NORMAL, CORRECTION_PENDING, APPROVED
}
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in") public ResponseEntity<?> clockIn(@RequestBody ClockInDto dto) { ... }
    @PostMapping("/clock-out") public ResponseEntity<?> clockOut(@RequestBody ClockOutDto dto) { ... }
    @GetMapping("/report") public ResponseEntity<Resource> exportCsv(@RequestParam ...) { ... }
}
```

Section: Shift & Schedule Management
Description: Manages shift templates, recurring schedules, overtime rules, employee assignments, blackout dates, and audit logging.
Design Specification:
- Package: com.companyname.warehouse.scheduling
- Entities: ShiftTemplate, Shift, Schedule, BlackoutDate, OvertimeRule, AuditEntry
- Repositories/Services/Controllers for CRUD and bulk assignment
- Conflict detection logic in service layer
- Audit logging on changes
Sample Implementation:
```java
@Entity
public class ShiftTemplate { ... }
@Entity
public class Schedule {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private Shift shift;
    private LocalDate date;
}
@Service
public class ScheduleService {
    public void assignShift(Long employeeId, Long shiftId, LocalDate date) { ... }
    public boolean hasConflict(Long employeeId, LocalDate date) { ... }
}
```

Section: Leave & Absence Management
Description: Handles PTO/sick/unpaid leave requests, approvals, accruals, and integration with scheduling/payroll.
Design Specification:
- Package: com.companyname.warehouse.leave
- Entities: LeaveRequest, LeaveBalance, LeavePolicy
- Service: LeaveService (request, approve/deny, update balances)
- Controller: LeaveController
- Integration hooks for scheduling/payroll
Sample Implementation:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String type; // PTO, SICK, UNPAID
    private LocalDate startDate, endDate;
    private String status; // PENDING, APPROVED, DENIED
}
@Service
public class LeaveService {
    public LeaveRequest requestLeave(Long employeeId, LeaveRequestDto dto) { ... }
    public void approveLeave(Long requestId) { ... }
}
```

Section: Training & Certification Tracking
Description: Tracks employee certifications, expirations, renewals, and blocks assignments if expired; supports document uploads and expiry alerts.
Design Specification:
- Package: com.companyname.warehouse.certification
- Entities: Certification, CertificationType, EmployeeCertification
- Service: CertificationService (CRUD, expiry checks, alerts)
- Controller: CertificationController
- File upload for proof documents
Sample Implementation:
```java
@Entity
public class EmployeeCertification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private CertificationType type;
    private LocalDate issueDate, expiryDate;
    private String documentUrl;
}
@Service
public class CertificationService {
    public void checkAndAlertExpiries() { ... }
}
```

Section: Safety Incidents & OSHA Reporting
Description: Records safety incidents, manages investigation workflow, and generates OSHA-compliant reports and dashboards.
Design Specification:
- Package: com.companyname.warehouse.safety
- Entities: SafetyIncident, Investigation, OSHAReport
- Service: SafetyService (incident workflow, reporting)
- Controller: SafetyController
Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private String severity, location, description, status;
    @ManyToMany private List<Employee> involvedEmployees;
}
@RestController
@RequestMapping("/safety/incidents")
public class SafetyController {
    @PostMapping public ResponseEntity<?> reportIncident(@RequestBody IncidentDto dto) { ... }
}
```

Section: Equipment & Asset Assignment
Description: Manages asset registry, assignment to employees, check-in/out, certification checks, and asset condition tracking.
Design Specification:
- Package: com.companyname.warehouse.asset
- Entities: Asset, AssetAssignment, AssetCondition
- Service: AssetService (CRUD, check-in/out, overdue reports)
- Controller: AssetController
Sample Implementation:
```java
@Entity
public class AssetAssignment {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Asset asset;
    @ManyToOne private Employee employee;
    private LocalDateTime checkoutTime, returnTime;
}
@Service
public class AssetService {
    public void assignAsset(Long assetId, Long employeeId) { ... }
    public void checkCertification(Long employeeId, String assetType) { ... }
}
```

Section: Performance Reviews & Goals
Description: Supports review templates, goal tracking, ratings, comments, acknowledgements, PDF export, and immutable history.
Design Specification:
- Package: com.companyname.warehouse.performance
- Entities: PerformanceReview, ReviewTemplate, Goal, Acknowledgement
- Service: PerformanceService (review cycles, workflow)
- Controller: PerformanceController
Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private ReviewTemplate template;
    private String status; // DRAFT, SUBMITTED, ACKNOWLEDGED
    private LocalDateTime submittedAt;
}
```

Section: Payroll Export Integration
Description: Generates payroll-ready files from attendance/leave, maps to provider formats, delivers securely, and logs exports.
Design Specification:
- Package: com.companyname.warehouse.payroll
- Entities: PayrollExport, PayrollMapping
- Service: PayrollExportService (generate, deliver, retry, audit)
- Controller: PayrollController
- Integration: SFTP/API delivery
Sample Implementation:
```java
@Service
public class PayrollExportService {
    public File generatePayrollFile(LocalDate periodStart, LocalDate periodEnd) { ... }
    public void deliver(File file) { ... }
}
```

Section: Notifications & Announcements
Description: Delivers in-app/email/SMS notifications for events, supports opt-in/out, templates, localization, rate limiting, and dashboard display.
Design Specification:
- Package: com.companyname.warehouse.notification
- Entities: Notification, Announcement, DeliveryStatus
- Service: NotificationService (send, track, rate limit)
- Controller: NotificationController
Sample Implementation:
```java
@Service
public class NotificationService {
    public void sendNotification(Long userId, String channel, String message) { ... }
}
```

Section: Integration Layer (HRIS/WMS APIs)
Description: Exposes REST APIs/connectors for HRIS, WMS, and IDP; supports webhooks, JWT/OAuth2 security, and OpenAPI docs.
Design Specification:
- Package: com.companyname.warehouse.integration
- Entities: IntegrationJob, WebhookEvent
- Service: IntegrationService (sync, webhooks)
- Controller: IntegrationController
- Security: JWT/OAuth2
Sample Implementation:
```java
@RestController
@RequestMapping("/api/hris")
@SecurityRequirement(name = "bearerAuth")
public class HRISController {
    @PostMapping("/employees") public ResponseEntity<?> syncEmployee(@RequestBody EmployeeDto dto) { ... }
}
```

Section: Audit Trail & Compliance
Description: Centralized audit logging for sensitive changes, with immutable storage and export capabilities.
Design Specification:
- Package: com.companyname.warehouse.audit
- Entity: AuditLog (actor, timestamp, entity, before, after, action)
- Service: AuditService (log, export)
- Controller: AuditController
Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String actor, entity, action;
    private Instant timestamp;
    @Lob private String before, after;
}
```

Section: Reporting & Analytics
Description: Provides operational reports (attendance, overtime, leave, certifications, safety), CSV/PDF export, dashboards, and BI endpoints.
Design Specification:
- Package: com.companyname.warehouse.reporting
- Service: ReportingService (generate, export)
- Controller: ReportingController
Sample Implementation:
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance") public ResponseEntity<Resource> attendanceReport(@RequestParam ...) { ... }
}
```

Section: Mobile Access (PWA)
Description: Delivers responsive PWA views for core flows, offline support, and installable manifest.
Design Specification:
- Frontend: PWA (React/Vue/Angular), API endpoints as above
- Backend: Offline queue endpoints, conflict resolution logic
Sample Implementation:
```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @PostMapping("/offline/clock-events") public ResponseEntity<?> syncOfflineEvents(@RequestBody List<ClockEventDto> events) { ... }
}
```

Section: Onboarding & Offboarding Workflow
Description: Automates provisioning, initial schedule/training, and deprovisioning of access/assets on termination.
Design Specification:
- Package: com.companyname.warehouse.onboarding
- Entities: OnboardingTask, OffboardingTask
- Service: OnboardingService (provision, assign tasks), OffboardingService (revoke, collect)
- Controller: OnboardingController
Sample Implementation:
```java
@Service
public class OnboardingService {
    public void provisionNewHire(Employee employee) { ... }
    public void assignInitialTasks(Employee employee) { ... }
}
```
