# Warehouse Employee Management System â Low-Level Technical Design Document (Spring Boot)

================================================================================
## Epic E01: Project Scaffolding & Domain Setup
--------------------------------------------------------------------------------
### Section: Spring Boot Architecture Overview
**Description:** Establishes the foundational structure for the application, leveraging Spring Boot's auto-configuration, Maven build, modular package organization, and core dependencies (JPA, Security, Actuator, Flyway/Liquibase).

**Design Specification:**
- Base package: com.wms (Warehouse Management System)
- Modules: employee, scheduling, attendance, safety
- Maven structure: src/main/java, src/main/resources
- DB migration: Flyway/Liquibase for schema evolution
- Monitoring: Spring Boot Actuator enabled

**Sample Implementation:**
```java
// Maven pom.xml includes spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, flyway-core, actuator
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}
```

================================================================================
## Epic E02: Employee Master Data (CRUD)
--------------------------------------------------------------------------------
### Section: Package Structure & Component Breakdown
**Description:** Employee domain with CRUD APIs, DTOs, validation, filtering, pagination, and OpenAPI documentation.

**Design Specification:**
- Package: com.wms.employee
- Entity: Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)
- Relationships: ManyToOne Department, ManyToOne ShiftGroup
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService (business logic, soft-delete, filtering)
- Controller: EmployeeController (REST endpoints, DTO mapping)
- OpenAPI: @Schema annotations, springdoc-openapi integration

**Sample Implementation:**
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue private Long id;
    @Column(nullable = false) private String name;
    @Column(nullable = false, unique = true) private String badgeId;
    @Enumerated(EnumType.STRING) private Role role;
    @ManyToOne private Department department;
    @ManyToOne private ShiftGroup shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING) private Status status;
    private boolean deleted = false;
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping public EmployeeDto create(@Valid @RequestBody EmployeeDto dto) { ... }
    @GetMapping public Page<EmployeeDto> list(Pageable pageable, @RequestParam Map<String, String> filters) { ... }
    @DeleteMapping("/{id}") public void softDelete(@PathVariable Long id) { ... }
}
```

================================================================================
## Epic E03: Role-Based Access Control (RBAC)
--------------------------------------------------------------------------------
### Section: Security Configuration
**Description:** Implements Spring Security with roles, endpoint/method security, row-level constraints, and API key/OAuth2 toggle.

**Design Specification:**
- Roles: ADMIN, HR, SUPERVISOR, WORKER (enum)
- Security: @PreAuthorize, @Secured, custom PermissionEvaluator
- API key/OAuth2: application.yml toggle, SecurityConfig conditional beans
- Row-level: EmployeeService filters by supervisor/team

**Sample Implementation:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
            .anyRequest().authenticated();
        // API key or OAuth2 toggle
    }
}

@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeService.isTeamMember(#id, authentication))")
public EmployeeDto getEmployee(Long id) { ... }
```

================================================================================
## Epic E04: Time & Attendance (Clock In/Out)
--------------------------------------------------------------------------------
### Section: Attendance Domain & Workflow
**Description:** Clock-in/out endpoints, geofence/device capture, shift association, missed punch correction workflow.

**Design Specification:**
- Entity: AttendanceEvent (id, employee, type, timestamp, location, deviceId, status)
- Service: AttendanceService (clock-in/out, shift calculation, correction requests)
- Controller: AttendanceController (REST endpoints)
- Geofence: location validation logic
- Correction: Approval workflow entity

**Sample Implementation:**
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private EventType type; // CLOCK_IN, CLOCK_OUT
    private LocalDateTime timestamp;
    private String location;
    private String deviceId;
    @Enumerated(EnumType.STRING) private Status status; // NORMAL, CORRECTION_PENDING
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in") public AttendanceDto clockIn(@RequestBody ClockInRequest req) { ... }
    @PostMapping("/clock-out") public AttendanceDto clockOut(@RequestBody ClockOutRequest req) { ... }
    @PostMapping("/correction") public CorrectionTaskDto requestCorrection(@RequestBody CorrectionRequest req) { ... }
}
```

================================================================================
## Epic E05: Shift & Schedule Management
--------------------------------------------------------------------------------
### Section: Shift Templates & Scheduling
**Description:** Recurring shift templates, rotations, overtime rules, blackout dates, assignment to employees.

**Design Specification:**
- Entity: ShiftTemplate (id, name, startTime, endTime, recurrence, overtimeRule)
- Entity: ShiftAssignment (id, employee, shiftTemplate, date)
- Service: ShiftService (conflict detection, bulk assignment)
- Controller: ShiftController (CRUD, bulk assign endpoints)
- Calendar: BlackoutDate entity

**Sample Implementation:**
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrence; // e.g., WEEKLY, ROTATION
    private String overtimeRule;
}

@Entity
public class ShiftAssignment {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private ShiftTemplate shiftTemplate;
    private LocalDate date;
}

@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates") public ShiftTemplateDto createTemplate(@RequestBody ShiftTemplateDto dto) { ... }
    @PostMapping("/assign") public void bulkAssign(@RequestBody BulkAssignRequest req) { ... }
}
```

================================================================================
## Epic E06: Leave & Absence Management
--------------------------------------------------------------------------------
### Section: Leave Requests & Accruals
**Description:** PTO/sick/unpaid leave requests, approval workflow, accrual balances, integration with scheduling/payroll.

**Design Specification:**
- Entity: LeaveRequest (id, employee, type, startDate, endDate, status, approver)
- Entity: LeaveBalance (id, employee, type, balance)
- Service: LeaveService (request, approve/deny, update balances)
- Controller: LeaveController (REST endpoints)
- Integration: hooks for scheduling/payroll exclusion

**Sample Implementation:**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING) private Status status; // REQUESTED, APPROVED, DENIED
    @ManyToOne private Employee approver;
}

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping public LeaveRequestDto requestLeave(@RequestBody LeaveRequestDto dto) { ... }
    @PatchMapping("/{id}/approve") public void approve(@PathVariable Long id) { ... }
}
```

================================================================================
## Epic E07: Training & Certification Tracking
--------------------------------------------------------------------------------
### Section: Certification Domain & Expiry Management
**Description:** Track certifications, expirations, renewals, block assignments, upload proof documents.

**Design Specification:**
- Entity: Certification (id, employee, type, issueDate, expiryDate, documentUrl)
- Service: CertificationService (CRUD, expiry alerts, assignment checks)
- Controller: CertificationController (REST endpoints)
- File upload: Multipart support for proof documents

**Sample Implementation:**
```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private CertificationType type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
}

@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping public CertificationDto addCertification(@RequestBody CertificationDto dto) { ... }
    @PatchMapping("/{id}/upload") public void uploadProof(@PathVariable Long id, @RequestParam MultipartFile file) { ... }
}
```

================================================================================
## Epic E08: Safety Incidents & OSHA Reporting
--------------------------------------------------------------------------------
### Section: Safety Incident Workflow
**Description:** Record incidents/near-misses, severity, location, involved employees, investigation workflow, OSHA summary generation.

**Design Specification:**
- Entity: SafetyIncident (id, date, severity, location, description, status, involvedEmployees)
- Service: SafetyService (incident workflow, OSHA report generation)
- Controller: SafetyController (REST endpoints, export endpoints)
- Dashboard: metrics endpoints

**Sample Implementation:**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private LocalDate date;
    @Enumerated(EnumType.STRING) private Severity severity;
    private String location;
    private String description;
    @Enumerated(EnumType.STRING) private Status status; // OPEN, INVESTIGATING, RESOLVED
    @ManyToMany private List<Employee> involvedEmployees;
}

@RestController
@RequestMapping("/safety/incidents")
public class SafetyController {
    @PostMapping public SafetyIncidentDto reportIncident(@RequestBody SafetyIncidentDto dto) { ... }
    @GetMapping("/oshasummary") public OSHAReportDto getOSHASummary(@RequestParam Year year) { ... }
}
```

================================================================================
## Epic E09: Equipment & Asset Assignment
--------------------------------------------------------------------------------
### Section: Asset Registry & Assignment
**Description:** Assign assets to employees, track check-in/out, block use if certification missing, maintain asset condition.

**Design Specification:**
- Entity: Asset (id, type, serialNumber, condition, assignedTo, checkedOutAt, checkedInAt)
- Service: AssetService (CRUD, check-in/out, certification validation)
- Controller: AssetController (REST endpoints)
- History: AssetHistory entity

**Sample Implementation:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    @Enumerated(EnumType.STRING) private AssetType type;
    private String serialNumber;
    @Enumerated(EnumType.STRING) private Condition condition;
    @ManyToOne private Employee assignedTo;
    private LocalDateTime checkedOutAt;
    private LocalDateTime checkedInAt;
}

@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign") public void assignAsset(@RequestBody AssetAssignmentDto dto) { ... }
    @PostMapping("/checkin") public void checkIn(@RequestBody AssetCheckInDto dto) { ... }
}
```

================================================================================
## Epic E10: Performance Reviews & Goals
--------------------------------------------------------------------------------
### Section: Review Templates & Workflow
**Description:** Quarterly/annual review templates, goals, competencies, ratings, comments, acknowledgement workflow.

**Design Specification:**
- Entity: PerformanceReview (id, employee, period, template, goals, ratings, comments, status)
- Service: ReviewService (cycle creation, submission, acknowledgement)
- Controller: ReviewController (REST endpoints, PDF export)
- History: immutable after sign-off

**Sample Implementation:**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String period;
    @ManyToOne private ReviewTemplate template;
    @ElementCollection private List<Goal> goals;
    @ElementCollection private List<Rating> ratings;
    private String comments;
    @Enumerated(EnumType.STRING) private Status status; // DRAFT, SUBMITTED, ACKNOWLEDGED
}

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping public PerformanceReviewDto createReview(@RequestBody PerformanceReviewDto dto) { ... }
    @PostMapping("/{id}/acknowledge") public void acknowledge(@PathVariable Long id) { ... }
    @GetMapping("/{id}/export") public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) { ... }
}
```

================================================================================
## Epic E11: Payroll Export Integration
--------------------------------------------------------------------------------
### Section: Payroll Export & Delivery
**Description:** Generate payroll-ready files from attendance/leave, map to provider formats, secure SFTP/API delivery.

**Design Specification:**
- Service: PayrollExportService (data aggregation, file generation, provider mapping)
- Entity: PayrollExportLog (id, date, status, fileUrl, provider, attempts)
- Controller: PayrollController (export endpoints)
- Integration: SFTP/API client beans

**Sample Implementation:**
```java
@Service
public class PayrollExportService {
    public File generatePayrollFile(LocalDate period) { ... }
    public void deliverToProvider(File file, PayrollProvider provider) { ... }
}

@Entity
public class PayrollExportLog {
    @Id @GeneratedValue private Long id;
    private LocalDate date;
    @Enumerated(EnumType.STRING) private Status status;
    private String fileUrl;
    private String provider;
    private int attempts;
}
```

================================================================================
## Epic E12: Notifications & Announcements
--------------------------------------------------------------------------------
### Section: Notification Channels & Delivery
**Description:** In-app/email/SMS notifications for events, quiet hours, opt-in/out, delivery status tracking.

**Design Specification:**
- Entity: Notification (id, recipient, type, content, channel, status, sentAt)
- Entity: Announcement (id, title, content, visibleFrom, visibleTo)
- Service: NotificationService (send, track, rate limit)
- Controller: NotificationController (REST endpoints)
- Integration: Email/SMS provider beans

**Sample Implementation:**
```java
@Entity
public class Notification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee recipient;
    @Enumerated(EnumType.STRING) private NotificationType type;
    private String content;
    @Enumerated(EnumType.STRING) private Channel channel;
    @Enumerated(EnumType.STRING) private Status status;
    private LocalDateTime sentAt;
}

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping public void sendNotification(@RequestBody NotificationDto dto) { ... }
    @GetMapping public List<NotificationDto> getNotifications(@RequestParam Long employeeId) { ... }
}
```

================================================================================
## Epic E13: Integration Layer (HRIS/WMS APIs)
--------------------------------------------------------------------------------
### Section: External API Integration
**Description:** REST APIs/connectors for HRIS, WMS, IDP (SSO), webhooks for events, JWT/OAuth2 security.

**Design Specification:**
- Service: IntegrationService (HRIS sync, WMS sync, IDP SSO)
- Controller: IntegrationController (API endpoints, webhooks)
- Security: JWT/OAuth2 configuration
- OpenAPI: documented endpoints

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    @PostMapping("/hris/sync") public void syncHRIS(@RequestBody HRISSyncRequest req) { ... }
    @PostMapping("/wms/sync") public void syncWMS(@RequestBody WMSSyncRequest req) { ... }
    @PostMapping("/webhook") public void handleWebhook(@RequestBody WebhookEventDto dto) { ... }
}
```

================================================================================
## Epic E14: Audit Trail & Compliance
--------------------------------------------------------------------------------
### Section: Audit Logging & Tamper-Evidence
**Description:** Centralized audit logging for sensitive changes, immutable log table, export, coverage tests.

**Design Specification:**
- Entity: AuditLog (id, actor, timestamp, entity, entityId, action, before, after)
- Service: AuditService (log create/update/delete, export)
- Controller: AuditController (export endpoints)
- Storage: append-only, tamper-evident (e.g., hash chain)

**Sample Implementation:**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    private Long entityId;
    private String action;
    @Lob private String before;
    @Lob private String after;
    private String hash; // for tamper-evidence
}

@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/export") public ResponseEntity<byte[]> exportAudit(@RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
}
```

================================================================================
## Epic E15: Reporting & Analytics
--------------------------------------------------------------------------------
### Section: Operational Reporting
**Description:** Attendance, overtime, leave, certification, safety KPIs, CSV/PDF export, dashboards, metrics endpoints.

**Design Specification:**
- Service: ReportingService (data aggregation, filtering, export)
- Controller: ReportingController (report endpoints, dashboard endpoints)
- Security: role-based access

**Sample Implementation:**
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance") public List<AttendanceReportDto> getAttendanceReport(@RequestParam Map<String, String> filters) { ... }
    @GetMapping("/export") public ResponseEntity<byte[]> exportCsv(@RequestParam String type) { ... }
    @GetMapping("/dashboard") public DashboardDto getDashboard(@RequestParam Long employeeId) { ... }
}
```

================================================================================
## Epic E16: Mobile Access (PWA)
--------------------------------------------------------------------------------
### Section: PWA & Mobile Views
**Description:** Responsive views for clock-in/out, schedules, leave, announcements; offline queue; PWA manifest; Lighthouse score.

**Design Specification:**
- Frontend: PWA manifest, service worker, offline queue (local storage)
- Backend: endpoints for mobile flows (attendance, schedules, leave, announcements)
- Controller: MobileController (REST endpoints)
- Security: JWT for mobile clients

**Sample Implementation:**
```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @PostMapping("/clock-in") public AttendanceDto mobileClockIn(@RequestBody ClockInRequest req) { ... }
    @GetMapping("/schedule") public List<ShiftAssignmentDto> getMobileSchedule(@RequestParam Long employeeId) { ... }
}
```

================================================================================
## Epic E17: Onboarding & Offboarding Workflow
--------------------------------------------------------------------------------
### Section: Lifecycle Automation
**Description:** Automate account provisioning, initial schedule, training tasks, asset assignment, deprovision access/assets on termination.

**Design Specification:**
- Service: LifecycleService (onboard/offboard logic, HRIS triggers)
- Entity: OnboardingTask, OffboardingTask (id, employee, type, status, dueDate)
- Controller: LifecycleController (workflow endpoints)
- Integration: HRIS sync, asset collection, schedule update

**Sample Implementation:**
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private TaskType type;
    @Enumerated(EnumType.STRING) private Status status;
    private LocalDate dueDate;
}

@RestController
@RequestMapping("/lifecycle")
public class LifecycleController {
    @PostMapping("/onboard") public void onboardEmployee(@RequestBody OnboardRequest req) { ... }
    @PostMapping("/offboard") public void offboardEmployee(@RequestBody OffboardRequest req) { ... }
}
```

================================================================================

## General Spring Boot Best Practices Applied:
- Dependency injection via @Autowired or constructor injection
- Exception handling via @ControllerAdvice
- Validation via @Valid, javax.validation annotations
- RESTful API design: proper HTTP verbs, status codes, DTOs
- Transaction management via @Transactional on service layer
- OpenAPI documentation via springdoc-openapi
- Security via Spring Security, JWT/OAuth2, method/endpoint/row-level controls
- Modular package structure for maintainability
- Integration points via @Service beans, REST clients, SFTP/API connectors
- Tamper-evident audit logs for compliance

---

**Document Status:** Production-ready, detailed technical design for all 17 epics, ensuring clarity, consistency, and adherence to Spring Boot industry standards for easy consumption by developers.
