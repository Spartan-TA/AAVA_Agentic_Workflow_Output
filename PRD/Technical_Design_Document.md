Section: EPIC E01 Project Scaffolding
Description: Establishes the foundational Spring Boot project structure, database migration setup, and core domain packages for the Warehouse EMS.
Design Specification:
- Spring Boot Maven project initialized with base modules: employee, scheduling, attendance, safety
- Package structure: com.warehouseems.{employee,scheduling,attendance,safety,common,config}
- Flyway/Liquibase configured for DB migrations
- Spring Boot Actuator enabled for health checks
- README with build/run steps
Sample Implementation:
// pom.xml dependencies
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
// application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/warehouseems
spring.flyway.enabled=true
management.endpoints.web.exposure.include=health

Section: EPIC E02 Employee Master Data (CRUD)
Description: Implements CRUD APIs for Employee domain, including filtering, pagination, soft delete, and OpenAPI documentation.
Design Specification:
- Entity: Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService with CRUD, filtering, pagination, soft delete
- Controller: EmployeeController with REST endpoints
- OpenAPI annotations for documentation
Sample Implementation:
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    private String name;
    @Column(unique=true)
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted;
}
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Page<Employee> findByDeletedFalse(Pageable pageable);
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
}
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @GetMapping
    public Page<Employee> list(Pageable pageable) {...}
    @PostMapping
    public Employee create(@RequestBody EmployeeDto dto) {...}
    @PatchMapping("/{id}")
    public Employee update(@PathVariable Long id, @RequestBody EmployeeDto dto) {...}
    @DeleteMapping("/{id}")
    public void softDelete(@PathVariable Long id) {...}
}

Section: EPIC E03 Role Based Access Control (RBAC)
Description: Adds Spring Security with roles, method/endpoint security, row-level constraints, and API key/OAuth2 config.
Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- SecurityConfig: @EnableWebSecurity, role-based access rules
- Row-level security in EmployeeService
- API key/OAuth2 toggle via application.properties
Sample Implementation:
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .antMatchers("/attendance/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
            .anyRequest().authenticated()
            .and().oauth2Login();
    }
}
@Service
public class EmployeeService {
    public List<Employee> getTeamEmployees(User user) {
        if (user.hasRole("SUPERVISOR")) {
            return employeeRepository.findByDepartment(user.getDepartment());
        }
        // ...
    }
}

Section: EPIC E04 Time & Attendance (Clock In/Out)
Description: Endpoints for clock-in/out events, geofence/device capture, hours calculation, missed punch correction workflow, and reporting.
Design Specification:
- Entity: AttendanceEvent (id, employeeId, type, timestamp, deviceId, location, approved)
- Repository: AttendanceEventRepository
- Service: AttendanceService (clockIn, clockOut, corrections, reporting)
- Controller: AttendanceController
Sample Implementation:
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type; // CLOCK_IN, CLOCK_OUT
    private LocalDateTime timestamp;
    private String deviceId;
    private String location;
    private boolean approved;
}
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public AttendanceEvent clockIn(@RequestBody ClockInDto dto) {...}
    @PostMapping("/clock-out")
    public AttendanceEvent clockOut(@RequestBody ClockOutDto dto) {...}
}

Section: EPIC E05 Shift & Schedule Management
Description: Shift templates, rotations, overtime rules, assignment, blackout dates, and conflict detection.
Design Specification:
- Entity: ShiftTemplate, ShiftAssignment
- Service: ShiftService (CRUD, conflict detection, bulk assign)
- Controller: ShiftController
Sample Implementation:
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
}
@Entity
public class ShiftAssignment {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private Long shiftTemplateId;
    private LocalDate date;
}
@Service
public class ShiftService {
    public boolean hasConflict(Long employeeId, LocalDate date) {...}
}

Section: EPIC E06 Leave & Absence Management
Description: PTO/sick/unpaid leave request/approval, accrual balances, exclusion from scheduling/payroll.
Design Specification:
- Entity: LeaveRequest (id, employeeId, type, startDate, endDate, status, balance)
- Service: LeaveService (request, approve/deny, update balances)
- Controller: LeaveController
Sample Implementation:
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private int balance;
}
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping
    public LeaveRequest requestLeave(@RequestBody LeaveRequestDto dto) {...}
    @PatchMapping("/{id}/approve")
    public LeaveRequest approve(@PathVariable Long id) {...}
}

Section: EPIC E07 Training & Certification Tracking
Description: Track certifications, expirations, renewals, block assignment for expired certs, upload proof documents.
Design Specification:
- Entity: Certification (id, employeeId, type, expiryDate, documentUrl)
- Service: CertificationService (CRUD, expiry alerts, assignment checks)
- Controller: CertificationController
Sample Implementation:
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type;
    private LocalDate expiryDate;
    private String documentUrl;
}
@Service
public class CertificationService {
    public boolean isValid(Long employeeId, String type) {...}
}

Section: EPIC E08 Safety Incidents & OSHA Reporting
Description: Record incidents, workflow, OSHA summary export, metrics dashboard.
Design Specification:
- Entity: SafetyIncident (id, severity, location, description, status, involvedEmployeeIds)
- Service: SafetyIncidentService (CRUD, workflow, export)
- Controller: SafetyIncidentController
Sample Implementation:
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String severity;
    private String location;
    private String description;
    private String status;
    @ElementCollection
    private List<Long> involvedEmployeeIds;
}
@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
    @PostMapping
    public SafetyIncident record(@RequestBody SafetyIncidentDto dto) {...}
}

Section: EPIC E09 Equipment & Asset Assignment
Description: Assign assets, track checkout/return, block use if cert missing, asset condition state.
Design Specification:
- Entity: Asset (id, type, condition, assignedEmployeeId, checkedOut, history)
- Service: AssetService (CRUD, check-in/out, block on cert invalid)
- Controller: AssetController
Sample Implementation:
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String condition;
    private Long assignedEmployeeId;
    private boolean checkedOut;
    @ElementCollection
    private List<AssetHistory> history;
}

Section: EPIC E10 Performance Reviews & Goals
Description: Review templates, goals, competencies, ratings, comments, immutable history.
Design Specification:
- Entity: PerformanceReview (id, employeeId, cycle, goals, ratings, comments, signedOff)
- Service: PerformanceReviewService (CRUD, assign, submit, PDF export)
- Controller: PerformanceReviewController
Sample Implementation:
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String cycle;
    @ElementCollection
    private List<String> goals;
    @ElementCollection
    private List<String> ratings;
    private String comments;
    private boolean signedOff;
}

Section: EPIC E11 Payroll Export Integration
Description: Generate payroll files, map to provider formats, secure delivery, retry failed exports.
Design Specification:
- Service: PayrollExportService (generate, deliver, retry)
- Entity: PayrollExportLog (id, status, fileUrl, attemptCount)
Sample Implementation:
@Service
public class PayrollExportService {
    public void generateExport() {...}
    public void deliverExport(String fileUrl) {...}
}

Section: EPIC E12 Notifications & Announcements
Description: In-app/email/SMS notifications, quiet hours config, delivery status tracking.
Design Specification:
- Entity: Notification (id, employeeId, type, channel, message, delivered, timestamp)
- Service: NotificationService (send, opt-in/out, rate limit)
- Controller: NotificationController
Sample Implementation:
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type;
    private String channel;
    private String message;
    private boolean delivered;
    private LocalDateTime timestamp;
}

Section: EPIC E13 Integration Layer (HRIS/WMS APIs)
Description: Expose REST APIs/connectors for HRIS, WMS, SSO; webhooks for events.
Design Specification:
- Controller: HRISController, WMSController, SSOController
- Security: JWT/OAuth2
- Service: IntegrationService (sync, webhooks)
Sample Implementation:
@RestController
@RequestMapping("/api/hris")
public class HRISController {
    @PostMapping("/sync")
    public void syncEmployees(@RequestBody List<EmployeeDto> dtos) {...}
}

Section: EPIC E14 Audit Trail & Compliance
Description: Centralized audit logging for sensitive changes, tamper-evident storage, export/search/filter.
Design Specification:
- Entity: AuditLog (id, actor, timestamp, entity, before, after)
- Service: AuditService (log, export, search)
- Controller: AuditController
Sample Implementation:
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    private String before;
    private String after;
}

Section: EPIC E15 Reporting & Analytics
Description: Operational reports, dashboards, scheduled delivery, data visualization.
Design Specification:
- Service: ReportingService (generate, export, schedule)
- Controller: ReportingController
Sample Implementation:
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ReportDto attendanceReport(@RequestParam LocalDate start, @RequestParam LocalDate end) {...}
}

Section: EPIC E16 Mobile Access (PWA)
Description: Responsive views, offline access, push notifications, camera integration.
Design Specification:
- Frontend: PWA manifest, service worker
- Backend: PushNotificationService
Sample Implementation:
// manifest.json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone"
}

Section: EPIC E17 Onboarding & Offboarding Workflow
Description: Automate provisioning, initial schedule, training, asset assignment, deprovisioning.
Design Specification:
- Service: OnboardingService, OffboardingService
- Entity: OnboardingTask, OffboardingTask
Sample Implementation:
@Service
public class OnboardingService {
    public void provisionEmployee(EmployeeDto dto) {...}
}

Section: EPIC E18 Localization
Description: Multi-warehouse support, language localization, site-specific config, multi-site reporting.
Design Specification:
- Entity: Warehouse (id, name, location, config)
- Service: LocalizationService
Sample Implementation:
@Entity
public class Warehouse {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String location;
    private String config;
}

Section: EPIC E19 Advanced Scheduling
Description: Predictive staffing, shift optimization, conflict alerts, historical analysis.
Design Specification:
- Service: SchedulingService (predict, optimize, alert, analyze)
Sample Implementation:
@Service
public class SchedulingService {
    public List<ShiftAssignment> recommendStaffing(LocalDate date) {...}
}

Section: EPIC E20 Self-Service Portal
Description: Employee self-service profile, payslips, schedules, requests, announcements.
Design Specification:
- Controller: SelfServiceController
- Service: SelfServiceService
Sample Implementation:
@RestController
@RequestMapping("/self-service")
public class SelfServiceController {
    @GetMapping("/profile")
    public EmployeeDto getProfile() {...}
    @GetMapping("/payslips")
    public List<PayslipDto> getPayslips() {...}
    @PostMapping("/requests")
    public void submitRequest(@RequestBody RequestDto dto) {...}
}
