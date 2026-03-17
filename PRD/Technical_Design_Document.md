==================================================================
Warehouse Employee Management System (EMS) - Low-Level Technical Design Document
==================================================================

# Table of Contents
- E01: Project Scaffolding & Domain Setup
- E02: Employee Master Data CRUD
- E03: Role Based Access Control (RBAC)
- E04: Time & Attendance (Clock In/Out)
- E05: Shift & Schedule Management
- E06: Leave & Absence Management
- E07: Training & Certification Tracking
- E08: Safety Incidents & OSHA Reporting
- E09: Equipment & Asset Assignment
- E10: Performance Reviews & Goals
- E11: Payroll Export Integration
- E12: Notifications & Announcements
- E13: Integration Layer (HRIS/WMS APIs)
- E14: Audit Trail & Compliance
- E15: Reporting & Analytics
- E16: Mobile Access (PWA)
- E17: Onboarding & Offboarding Workflow
- E18: Localization & Multi-Tenant
- E19: Advanced Scheduling AI/ML
- E20: Continuous Deployment & Observability

---

## E01: Project Scaffolding & Domain Setup

### Section: Overview of Spring Boot Architecture
Description:
The EMS is structured as a modular Spring Boot application using Maven for dependency management. Core modules include employee, scheduling, attendance, and safety. The application uses Flyway/Liquibase for database migrations and Spring Boot Actuator for health and metrics endpoints.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems` (root)
  - `employee`
  - `scheduling`
  - `attendance`
  - `safety`
  - `config`
  - `common`
  - `audit`
  - `integration`
  - `notification`
  - `reporting`
- Each module contains: `controller`, `service`, `repository`, `model`, `dto`, `mapper`

### Section: Entity Design
Description:
No domain entities in this epic, but base structure is created.

### Section: Service Layer Specifications
Description:
No business logic in this epic.

### Section: Repository Layer
Description:
No repositories in this epic.

### Section: Controller Specifications
Description:
No controllers in this epic.

### Section: Configuration and Security Settings
Design Specification:
- `application.yml` with server port 8080
- Flyway/Liquibase enabled
- Actuator endpoints enabled

Sample Implementation:
```yaml
# application.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ems
    username: ems
    password: ems
  flyway:
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### Section: Integration Points
Description:
None in this epic.

### Section: Code Snippets
Sample Implementation:
```java
@SpringBootApplication
public class EmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmsApplication.class, args);
    }
}
```

---

## E02: Employee Master Data CRUD

### Section: Overview of Spring Boot Architecture
Description:
Implements the Employee domain with RESTful CRUD APIs, DTOs, and validation. Uses Spring Data JPA for persistence.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems.employee`
  - `controller`
  - `service`
  - `repository`
  - `model`
  - `dto`
  - `mapper`

### Section: Entity Design
Design Specification:
- Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)
- Unique constraint on badgeId
- Soft-delete via `deleted` flag

Sample Implementation:
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
    private Role role;

    private String department;
    private String shiftGroup;
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    private boolean deleted = false;
    // getters/setters
}
```

### Section: Service Layer Specifications
Design Specification:
- `EmployeeService` with CRUD, filtering, pagination, soft-delete

Sample Implementation:
```java
public interface EmployeeService {
    EmployeeDTO create(EmployeeDTO dto);
    EmployeeDTO update(Long id, EmployeeDTO dto);
    EmployeeDTO partialUpdate(Long id, Map<String, Object> updates);
    void delete(Long id);
    Page<EmployeeDTO> findAll(Pageable pageable, EmployeeFilter filter);
    EmployeeDTO findById(Long id);
}
```

### Section: Repository Layer
Design Specification:
- `EmployeeRepository` extends `JpaRepository<Employee, Long>`
- Custom query for filtering and soft-delete

Sample Implementation:
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

### Section: Controller Specifications
Design Specification:
- `/employees` endpoint with POST, GET, PUT, PATCH, DELETE
- OpenAPI annotations

Sample Implementation:
```java
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee CRUD APIs")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeDTO dto) { ... }

    @GetMapping
    public Page<EmployeeDTO> list(Pageable pageable, EmployeeFilter filter) { ... }

    @GetMapping("/{id}")
    public EmployeeDTO get(@PathVariable Long id) { ... }

    @PutMapping("/{id}")
    public EmployeeDTO update(@PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) { ... }

    @PatchMapping("/{id}")
    public EmployeeDTO partialUpdate(@PathVariable Long id, @RequestBody Map<String, Object> updates) { ... }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { ... }
}
```

### Section: Configuration and Security Settings
Design Specification:
- Method-level security for RBAC (see E03)
- OpenAPI config for schema examples

### Section: Integration Points
Description:
None in this epic.

### Section: Code Snippets
Sample Implementation:
```java
public enum Role { ADMIN, HR, SUPERVISOR, WORKER }
public enum Status { ACTIVE, INACTIVE, TERMINATED }
```

---

## E03: Role Based Access Control (RBAC)

### Section: Overview of Spring Boot Architecture
Description:
Spring Security is configured for method and endpoint security. Supports roles: ADMIN, HR, SUPERVISOR, WORKER. Row-level constraints and API key/OAuth2 toggle.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems.config.security`
- `com.wms.ems.common.security`

### Section: Entity Design
Design Specification:
- User entity (if not using external IDP)
- Role enum

Sample Implementation:
```java
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    private String username;
    private String password;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;
}
```

### Section: Service Layer Specifications
Design Specification:
- UserDetailsService for authentication
- RBAC checks in service methods

Sample Implementation:
```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    // loadUserByUsername implementation
}
```

### Section: Repository Layer
Design Specification:
- `UserRepository` for user lookup

### Section: Controller Specifications
Design Specification:
- Security exception handlers for 401/403

### Section: Configuration and Security Settings
Design Specification:
- `SecurityConfig` with role-based access
- API key/OAuth2 toggle via `application.yml`

Sample Implementation:
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .oauth2Login() // or API key filter
            .and()
            .exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler(new AccessDeniedHandlerImpl());
    }
}
```

### Section: Integration Points
Design Specification:
- Optionally integrate with external OAuth2 provider or API key management

### Section: Code Snippets
Sample Implementation:
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(#id))")
public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) { ... }
```

---

## E04: Time & Attendance (Clock In/Out)

### Section: Overview of Spring Boot Architecture
Description:
Implements attendance event tracking, geofence/device capture, shift association, missed punch correction workflow.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems.attendance`
  - `controller`
  - `service`
  - `repository`
  - `model`
  - `dto`
  - `mapper`

### Section: Entity Design
Design Specification:
- AttendanceEvent (id, employee, type, timestamp, deviceId, location, shift, status)
- CorrectionRequest (id, attendanceEvent, requestedBy, status, approver, reason)

Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private EventType type; // CLOCK_IN, CLOCK_OUT

    private LocalDateTime timestamp;
    private String deviceId;
    private String location;
    @ManyToOne
    private Shift shift;
    @Enumerated(EnumType.STRING)
    private Status status; // NORMAL, CORRECTION_PENDING, CORRECTED
}
```

### Section: Service Layer Specifications
Design Specification:
- `AttendanceService` for clock-in/out, validation, shift association, correction workflow

Sample Implementation:
```java
public interface AttendanceService {
    AttendanceEventDTO clockIn(ClockEventDTO dto);
    AttendanceEventDTO clockOut(ClockEventDTO dto);
    CorrectionRequestDTO requestCorrection(Long eventId, CorrectionRequestDTO dto);
    List<AttendanceReportDTO> getDailyTotals(LocalDate date, Long employeeId);
}
```

### Section: Repository Layer
Design Specification:
- `AttendanceEventRepository` with queries for shift association, missed punches

### Section: Controller Specifications
Design Specification:
- `/attendance/clock-in`, `/attendance/clock-out`, `/attendance/corrections`
- CSV export endpoint

Sample Implementation:
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public AttendanceEventDTO clockIn(@RequestBody @Valid ClockEventDTO dto) { ... }

    @PostMapping("/clock-out")
    public AttendanceEventDTO clockOut(@RequestBody @Valid ClockEventDTO dto) { ... }

    @PostMapping("/corrections")
    public CorrectionRequestDTO requestCorrection(@RequestBody @Valid CorrectionRequestDTO dto) { ... }

    @GetMapping("/report")
    public ResponseEntity<Resource> exportReport(@RequestParam LocalDate date) { ... }
}
```

### Section: Configuration and Security Settings
Design Specification:
- Only authenticated users can clock in/out
- Supervisor approval for corrections

### Section: Integration Points
Design Specification:
- Device/location validation (optional integration with geofencing API)

### Section: Code Snippets
Sample Implementation:
```java
public enum EventType { CLOCK_IN, CLOCK_OUT }
public enum Status { NORMAL, CORRECTION_PENDING, CORRECTED }
```

---

## E05: Shift & Schedule Management

### Section: Overview of Spring Boot Architecture
Description:
Manages shift templates, recurring schedules, overtime, blackout dates, and assignment to employees.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems.scheduling`
  - `controller`
  - `service`
  - `repository`
  - `model`
  - `dto`
  - `mapper`

### Section: Entity Design
Design Specification:
- ShiftTemplate (id, name, startTime, endTime, recurrence, overtimeRule)
- Shift (id, template, date, assignedEmployees, blackout, status)

Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrence; // CRON or custom
    private String overtimeRule;
}
@Entity
public class Shift {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private ShiftTemplate template;
    private LocalDate date;
    @ManyToMany
    private Set<Employee> assignedEmployees;
    private boolean blackout;
    @Enumerated(EnumType.STRING)
    private Status status;
}
```

### Section: Service Layer Specifications
Design Specification:
- `ShiftService` for CRUD, conflict detection, bulk assignment, audit

Sample Implementation:
```java
public interface ShiftService {
    ShiftDTO createTemplate(ShiftTemplateDTO dto);
    ShiftDTO assignEmployees(Long shiftId, List<Long> employeeIds);
    List<ShiftDTO> findPersonalShifts(Long employeeId, LocalDate from, LocalDate to);
    void detectConflicts();
}
```

### Section: Repository Layer
Design Specification:
- `ShiftRepository` with queries for conflicts, blackout dates

### Section: Controller Specifications
Design Specification:
- `/shifts/templates`, `/shifts`, `/shifts/assign`, `/shifts/conflicts`

Sample Implementation:
```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates")
    public ShiftTemplateDTO createTemplate(@RequestBody @Valid ShiftTemplateDTO dto) { ... }

    @PostMapping("/{id}/assign")
    public ShiftDTO assignEmployees(@PathVariable Long id, @RequestBody List<Long> employeeIds) { ... }

    @GetMapping("/personal")
    public List<ShiftDTO> getPersonalShifts(@RequestParam Long employeeId) { ... }
}
```

### Section: Configuration and Security Settings
Design Specification:
- Supervisors can bulk-assign
- Audit entries generated for changes

### Section: Integration Points
Design Specification:
- Calendar integration for blackout dates

### Section: Code Snippets
Sample Implementation:
```java
public enum Status { SCHEDULED, COMPLETED, CANCELLED }
```

---

## E06: Leave & Absence Management

### Section: Overview of Spring Boot Architecture
Description:
Handles PTO, sick, unpaid leave requests, approvals, accruals, and integration with scheduling/payroll.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems.leave`
  - `controller`
  - `service`
  - `repository`
  - `model`
  - `dto`
  - `mapper`

### Section: Entity Design
Design Specification:
- LeaveRequest (id, employee, type, startDate, endDate, status, approver, accrualBalance, policy)

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
    private Status status; // REQUESTED, APPROVED, DENIED
    @ManyToOne
    private Employee approver;
    private Double accrualBalance;
    private String policy;
}
```

### Section: Service Layer Specifications
Design Specification:
- `LeaveService` for request, approve/deny, update balances, flag shifts

Sample Implementation:
```java
public interface LeaveService {
    LeaveRequestDTO requestLeave(LeaveRequestDTO dto);
    LeaveRequestDTO approveLeave(Long requestId, Long approverId);
    LeaveRequestDTO denyLeave(Long requestId, Long approverId);
    List<LeaveRequestDTO> getApprovedLeaves(Long employeeId);
}
```

### Section: Repository Layer
Design Specification:
- `LeaveRequestRepository` with queries for approved leaves

### Section: Controller Specifications
Design Specification:
- `/leaves`, `/leaves/approve`, `/leaves/deny`, `/leaves/export`

Sample Implementation:
```java
@RestController
@RequestMapping("/leaves")
public class LeaveController {
    @PostMapping
    public LeaveRequestDTO requestLeave(@RequestBody @Valid LeaveRequestDTO dto) { ... }

    @PostMapping("/{id}/approve")
    public LeaveRequestDTO approve(@PathVariable Long id, @RequestParam Long approverId) { ... }

    @PostMapping("/{id}/deny")
    public LeaveRequestDTO deny(@PathVariable Long id, @RequestParam Long approverId) { ... }
}
```

### Section: Configuration and Security Settings
Design Specification:
- Only supervisors/HR can approve/deny

### Section: Integration Points
Design Specification:
- Hooks to scheduling and payroll modules

### Section: Code Snippets
Sample Implementation:
```java
public enum LeaveType { PTO, SICK, UNPAID }
public enum Status { REQUESTED, APPROVED, DENIED }
```

---

## E07: Training & Certification Tracking

### Section: Overview of Spring Boot Architecture
Description:
Tracks certifications, expirations, renewals, and blocks unqualified assignments.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems.certification`
  - `controller`
  - `service`
  - `repository`
  - `model`
  - `dto`
  - `mapper`

### Section: Entity Design
Design Specification:
- Certification (id, employee, type, issueDate, expiryDate, status, documentUrl)

Sample Implementation:
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
    @Enumerated(EnumType.STRING)
    private Status status; // VALID, EXPIRING_SOON, EXPIRED
    private String documentUrl;
}
```

### Section: Service Layer Specifications
Design Specification:
- `CertificationService` for CRUD, expiry alerts, scheduling checks

Sample Implementation:
```java
public interface CertificationService {
    CertificationDTO create(CertificationDTO dto);
    List<CertificationDTO> findExpiringSoon(int days);
    boolean isQualified(Long employeeId, String certType);
}
```

### Section: Repository Layer
Design Specification:
- `CertificationRepository` with queries for expiring/expired certs

### Section: Controller Specifications
Design Specification:
- `/certifications`, `/certifications/alerts`, `/certifications/status`

Sample Implementation:
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public CertificationDTO create(@RequestBody @Valid CertificationDTO dto) { ... }

    @GetMapping("/alerts")
    public List<CertificationDTO> getExpiringSoon(@RequestParam int days) { ... }
}
```

### Section: Configuration and Security Settings
Design Specification:
- Scheduling checks block unqualified assignments

### Section: Integration Points
Design Specification:
- Document storage for proof uploads

### Section: Code Snippets
Sample Implementation:
```java
public enum Status { VALID, EXPIRING_SOON, EXPIRED }
```

---

## E08: Safety Incidents & OSHA Reporting

### Section: Overview of Spring Boot Architecture
Description:
Records safety incidents, manages investigation workflow, and generates OSHA reports.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems.safety`
  - `controller`
  - `service`
  - `repository`
  - `model`
  - `dto`
  - `mapper`

### Section: Entity Design
Design Specification:
- SafetyIncident (id, date, severity, location, description, involvedEmployees, status, correctiveActions)

Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private LocalDate date;
    private String severity;
    private String location;
    private String description;
    @ManyToMany
    private Set<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private Status status; // OPEN, INVESTIGATING, RESOLVED
    private String correctiveActions;
}
```

### Section: Service Layer Specifications
Design Specification:
- `SafetyService` for incident CRUD, workflow, OSHA export

Sample Implementation:
```java
public interface SafetyService {
    SafetyIncidentDTO reportIncident(SafetyIncidentDTO dto);
    SafetyIncidentDTO updateStatus(Long id, Status status);
    Resource exportOshaReport(LocalDate from, LocalDate to);
}
```

### Section: Repository Layer
Design Specification:
- `SafetyIncidentRepository` with queries for OSHA fields

### Section: Controller Specifications
Design Specification:
- `/safety/incidents`, `/safety/incidents/export`, `/safety/metrics`

Sample Implementation:
```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
    @PostMapping
    public SafetyIncidentDTO report(@RequestBody @Valid SafetyIncidentDTO dto) { ... }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportOsha(@RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
}
```

### Section: Configuration and Security Settings
Design Specification:
- Only authorized roles can update status

### Section: Integration Points
Design Specification:
- Metrics dashboard endpoints

### Section: Code Snippets
Sample Implementation:
```java
public enum Status { OPEN, INVESTIGATING, RESOLVED }
```

---

## E09: Equipment & Asset Assignment

### Section: Overview of Spring Boot Architecture
Description:
Manages asset registry, assignment, check-in/out, and certification validation.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems.asset`
  - `controller`
  - `service`
  - `repository`
  - `model`
  - `dto`
  - `mapper`

### Section: Entity Design
Design Specification:
- Asset (id, type, serialNumber, condition, assignedTo, checkedOutAt, checkedInAt, history)
- AssetHistory (id, asset, employee, action, timestamp)

Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private String condition;
    @ManyToOne
    private Employee assignedTo;
    private LocalDateTime checkedOutAt;
    private LocalDateTime checkedInAt;
    @OneToMany(mappedBy = "asset")
    private List<AssetHistory> history;
}
@Entity
public class AssetHistory {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Asset asset;
    @ManyToOne
    private Employee employee;
    private String action; // CHECKOUT, RETURN
    private LocalDateTime timestamp;
}
```

### Section: Service Layer Specifications
Design Specification:
- `AssetService` for CRUD, check-in/out, overdue reports

Sample Implementation:
```java
public interface AssetService {
    AssetDTO assignAsset(Long assetId, Long employeeId);
    AssetDTO returnAsset(Long assetId, Long employeeId);
    List<AssetDTO> findOverdueAssets();
}
```

### Section: Repository Layer
Design Specification:
- `AssetRepository`, `AssetHistoryRepository`

### Section: Controller Specifications
Design Specification:
- `/assets`, `/assets/assign`, `/assets/return`, `/assets/overdue`

Sample Implementation:
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/{id}/assign")
    public AssetDTO assign(@PathVariable Long id, @RequestParam Long employeeId) { ... }

    @PostMapping("/{id}/return")
    public AssetDTO returnAsset(@PathVariable Long id, @RequestParam Long employeeId) { ... }
}
```

### Section: Configuration and Security Settings
Design Specification:
- Block assignment if cert invalid

### Section: Integration Points
Design Specification:
- Certification module for validation

### Section: Code Snippets
Sample Implementation:
```java
public enum Condition { GOOD, NEEDS_REPAIR, OUT_OF_SERVICE }
```

---

## E10: Performance Reviews & Goals

### Section: Overview of Spring Boot Architecture
Description:
Manages review templates, goals, ratings, and immutable history.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems.performance`
  - `controller`
  - `service`
  - `repository`
  - `model`
  - `dto`
  - `mapper`

### Section: Entity Design
Design Specification:
- PerformanceReview (id, employee, cycle, goals, competencies, ratings, comments, supervisor, status, signedOffAt)

Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String cycle; // Q1-2024, 2024
    @ElementCollection
    private List<String> goals;
    @ElementCollection
    private List<String> competencies;
    @ElementCollection
    private List<String> ratings;
    private String comments;
    @ManyToOne
    private Employee supervisor;
    @Enumerated(EnumType.STRING)
    private Status status; // DRAFT, SUBMITTED, ACKNOWLEDGED, SIGNED_OFF
    private LocalDateTime signedOffAt;
}
```

### Section: Service Layer Specifications
Design Specification:
- `PerformanceService` for review cycles, workflow, PDF export

Sample Implementation:
```java
public interface PerformanceService {
    PerformanceReviewDTO createReview(PerformanceReviewDTO dto);
    PerformanceReviewDTO submitReview(Long id);
    PerformanceReviewDTO acknowledgeReview(Long id);
    Resource exportReviewPdf(Long id);
}
```

### Section: Repository Layer
Design Specification:
- `PerformanceReviewRepository`

### Section: Controller Specifications
Design Specification:
- `/reviews`, `/reviews/submit`, `/reviews/acknowledge`, `/reviews/export`

Sample Implementation:
```java
@RestController
@RequestMapping("/reviews")
public class PerformanceReviewController {
    @PostMapping
    public PerformanceReviewDTO create(@RequestBody @Valid PerformanceReviewDTO dto) { ... }

    @PostMapping("/{id}/submit")
    public PerformanceReviewDTO submit(@PathVariable Long id) { ... }
}
```

### Section: Configuration and Security Settings
Design Specification:
- Role-based visibility
- Immutable after sign-off

### Section: Integration Points
Design Specification:
- PDF export utility

### Section: Code Snippets
Sample Implementation:
```java
public enum Status { DRAFT, SUBMITTED, ACKNOWLEDGED, SIGNED_OFF }
```

---

## E11: Payroll Export Integration

### Section: Overview of Spring Boot Architecture
Description:
Generates payroll-ready files from attendance/leave, maps to provider formats, and delivers securely.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems.payroll`
  - `service`
  - `controller`
  - `integration`
  - `model`
  - `dto`

### Section: Entity Design
Design Specification:
- PayrollExport (id, period, fileUrl, status, attempts, lastTriedAt, auditLog)

Sample Implementation:
```java
@Entity
public class PayrollExport {
    @Id @GeneratedValue
    private Long id;
    private String period;
    private String fileUrl;
    @Enumerated(EnumType.STRING)
    private Status status; // PENDING, SUCCESS, FAILED
    private int attempts;
    private LocalDateTime lastTriedAt;
    private String auditLog;
}
```

### Section: Service Layer Specifications
Design Specification:
- `PayrollService` for export generation, delivery, retry, audit

Sample Implementation:
```java
public interface PayrollService {
    PayrollExportDTO generateExport(String period);
    void deliverExport(Long exportId);
    List<PayrollExportDTO> getExports();
}
```

### Section: Repository Layer
Design Specification:
- `PayrollExportRepository`

### Section: Controller Specifications
Design Specification:
- `/payroll/exports`, `/payroll/exports/deliver`

Sample Implementation:
```java
@RestController
@RequestMapping("/payroll/exports")
public class PayrollExportController {
    @PostMapping
    public PayrollExportDTO generate(@RequestParam String period) { ... }

    @PostMapping("/{id}/deliver")
    public void deliver(@PathVariable Long id) { ... }
}
```

### Section: Configuration and Security Settings
Design Specification:
- Secure SFTP/API credentials in config

### Section: Integration Points
Design Specification:
- Attendance, leave modules
- External payroll provider API/SFTP

### Section: Code Snippets
Sample Implementation:
```java
public enum Status { PENDING, SUCCESS, FAILED }
```

---

## E12: Notifications & Announcements

### Section: Overview of Spring Boot Architecture
Description:
Handles in-app, email, SMS notifications, templates, delivery tracking, and announcements.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems.notification`
  - `service`
  - `controller`
  - `model`
  - `dto`
  - `integration`

### Section: Entity Design
Design Specification:
- Notification (id, user, type, channel, content, status, sentAt, deliveryStatus)
- Announcement (id, title, content, visibleFrom, visibleTo, createdBy)

Sample Implementation:
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private User user;
    private String type;
    @Enumerated(EnumType.STRING)
    private Channel channel; // IN_APP, EMAIL, SMS
    private String content;
    @Enumerated(EnumType.STRING)
    private Status status; // PENDING, SENT, FAILED
    private LocalDateTime sentAt;
    private String deliveryStatus;
}
@Entity
public class Announcement {
    @Id @GeneratedValue
    private Long id;
    private String title;
    private String content;
    private LocalDateTime visibleFrom;
    private LocalDateTime visibleTo;
    @ManyToOne
    private User createdBy;
}
```

### Section: Service Layer Specifications
Design Specification:
- `NotificationService` for send, opt-in/out, rate limit, delivery tracking

Sample Implementation:
```java
public interface NotificationService {
    void sendNotification(NotificationDTO dto);
    void optIn(Long userId, Channel channel);
    void optOut(Long userId, Channel channel);
    List<AnnouncementDTO> getAnnouncements();
}
```

### Section: Repository Layer
Design Specification:
- `NotificationRepository`, `AnnouncementRepository`

### Section: Controller Specifications
Design Specification:
- `/notifications`, `/notifications/opt-in`, `/notifications/opt-out`, `/announcements`

Sample Implementation:
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping
    public void send(@RequestBody NotificationDTO dto) { ... }
}
```

### Section: Configuration and Security Settings
Design Specification:
- Quiet hours config
- Rate limiting

### Section: Integration Points
Design Specification:
- Email/SMS providers

### Section: Code Snippets
Sample Implementation:
```java
public enum Channel { IN_APP, EMAIL, SMS }
public enum Status { PENDING, SENT, FAILED }
```

---

## E13: Integration Layer (HRIS/WMS APIs)

### Section: Overview of Spring Boot Architecture
Description:
Exposes REST APIs and connectors for HRIS, WMS, IDP, and webhooks.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems.integration`
  - `controller`
  - `service`
  - `client`
  - `model`
  - `dto`

### Section: Entity Design
Design Specification:
- IntegrationEvent (id, type, payload, status, createdAt, processedAt)

Sample Implementation:
```java
@Entity
public class IntegrationEvent {
    @Id @GeneratedValue
    private Long id;
    private String type;
    @Lob
    private String payload;
    @Enumerated(EnumType.STRING)
    private Status status; // PENDING, PROCESSED, FAILED
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
```

### Section: Service Layer Specifications
Design Specification:
- `IntegrationService` for HRIS sync, WMS link, webhook handling

Sample Implementation:
```java
public interface IntegrationService {
    void syncHris();
    void syncWms();
    void handleWebhook(WebhookDTO dto);
}
```

### Section: Repository Layer
Design Specification:
- `IntegrationEventRepository`

### Section: Controller Specifications
Design Specification:
- `/api/hris`, `/api/wms`, `/api/webhooks`

Sample Implementation:
```java
@RestController
@RequestMapping("/api")
public class IntegrationController {
    @PostMapping("/webhooks")
    public void handleWebhook(@RequestBody WebhookDTO dto) { ... }
}
```

### Section: Configuration and Security Settings
Design Specification:
- JWT/OAuth2-secured APIs

### Section: Integration Points
Design Specification:
- HRIS, WMS, IDP (SSO), webhooks

### Section: Code Snippets
Sample Implementation:
```java
@PreAuthorize("hasAuthority('SCOPE_integration')")
@PostMapping("/hris/sync")
public void syncHris() { ... }
```

---

## E14: Audit Trail & Compliance

### Section: Overview of Spring Boot Architecture
Description:
Centralized audit logging for sensitive changes, tamper-evident storage.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems.audit`
  - `service`
  - `repository`
  - `model`

### Section: Entity Design
Design Specification:
- AuditLog (id, entity, entityId, action, actor, timestamp, before, after, immutable)

Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private Long entityId;
    private String action; // CREATE, UPDATE, DELETE
    private String actor;
    private LocalDateTime timestamp;
    @Lob
    private String before;
    @Lob
    private String after;
    private boolean immutable = true;
}
```

### Section: Service Layer Specifications
Design Specification:
- `AuditService` for logging, export, coverage tests

Sample Implementation:
```java
public interface AuditService {
    void logChange(String entity, Long entityId, String action, String actor, Object before, Object after);
    List<AuditLogDTO> export(LocalDate from, LocalDate to, String entity, String actor);
}
```

### Section: Repository Layer
Design Specification:
- `AuditLogRepository`

### Section: Controller Specifications
Design Specification:
- `/audit/logs`, `/audit/export`

Sample Implementation:
```java
@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/export")
    public List<AuditLogDTO> export(@RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
}
```

### Section: Configuration and Security Settings
Design Specification:
- Tamper-evident (immutable flag, append-only)

### Section: Integration Points
Design Specification:
- All modules log sensitive changes

### Section: Code Snippets
Sample Implementation:
```java
@Transactional
public void logChange(...) {
    // Save immutable audit log
}
```

---

## E15: Reporting & Analytics

### Section: Overview of Spring Boot Architecture
Description:
Provides operational reports, exports, dashboards, and metrics endpoints.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems.reporting`
  - `service`
  - `controller`
  - `model`
  - `dto`

### Section: Entity Design
Design Specification:
- ReportRequest (id, type, params, requestedBy, requestedAt, status, fileUrl)

Sample Implementation:
```java
@Entity
public class ReportRequest {
    @Id @GeneratedValue
    private Long id;
    private String type;
    @Lob
    private String params;
    @ManyToOne
    private User requestedBy;
    private LocalDateTime requestedAt;
    @Enumerated(EnumType.STRING)
    private Status status; // PENDING, COMPLETED, FAILED
    private String fileUrl;
}
```

### Section: Service Layer Specifications
Design Specification:
- `ReportingService` for report generation, export, metrics

Sample Implementation:
```java
public interface ReportingService {
    Resource generateReport(String type, Map<String, Object> params);
    List<MetricDTO> getMetrics();
}
```

### Section: Repository Layer
Design Specification:
- `ReportRequestRepository`

### Section: Controller Specifications
Design Specification:
- `/reports`, `/reports/export`, `/metrics`

Sample Implementation:
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping
    public Resource generate(@RequestParam String type, @RequestParam Map<String, Object> params) { ... }
}
```

### Section: Configuration and Security Settings
Design Specification:
- Access controlled by role

### Section: Integration Points
Design Specification:
- BI tools via metrics endpoints

### Section: Code Snippets
Sample Implementation:
```java
public enum Status { PENDING, COMPLETED, FAILED }
```

---

## E16: Mobile Access (PWA)

### Section: Overview of Spring Boot Architecture
Description:
Provides responsive views for mobile, offline queue for clock events, PWA manifest.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems.mobile`
  - `controller`
  - `service`
  - `model`
  - `dto`
  - `pwa`

### Section: Entity Design
Design Specification:
- MobileEventQueue (id, user, eventType, payload, status, createdAt, resolvedAt)

Sample Implementation:
```java
@Entity
public class MobileEventQueue {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private User user;
    private String eventType;
    @Lob
    private String payload;
    @Enumerated(EnumType.STRING)
    private Status status; // QUEUED, SYNCED, CONFLICT
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
```

### Section: Service Layer Specifications
Design Specification:
- `MobileService` for offline queue, conflict resolution

Sample Implementation:
```java
public interface MobileService {
    void queueEvent(MobileEventDTO dto);
    void syncEvents(Long userId);
    void resolveConflict(Long eventId, ConflictResolutionDTO dto);
}
```

### Section: Repository Layer
Design Specification:
- `MobileEventQueueRepository`

### Section: Controller Specifications
Design Specification:
- `/mobile/queue`, `/mobile/sync`, `/mobile/conflicts`

Sample Implementation:
```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @PostMapping("/queue")
    public void queueEvent(@RequestBody MobileEventDTO dto) { ... }
}
```

### Section: Configuration and Security Settings
Design Specification:
- PWA manifest, HTTPS required

### Section: Integration Points
Design Specification:
- Attendance, leave, notification modules

### Section: Code Snippets
Sample Implementation:
```javascript
// manifest.json (static resource)
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

---

## E17: Onboarding & Offboarding Workflow

### Section: Overview of Spring Boot Architecture
Description:
Automates provisioning, initial schedule, training, asset assignment, and deprovisioning.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems.onboarding`
  - `service`
  - `controller`
  - `model`
  - `dto`

### Section: Entity Design
Design Specification:
- OnboardingTask (id, employee, type, status, dueDate, completedAt, assignedTo)

Sample Implementation:
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type; // ACCOUNT, SCHEDULE, TRAINING, ASSET
    @Enumerated(EnumType.STRING)
    private Status status; // PENDING, COMPLETED
    private LocalDate dueDate;
    private LocalDateTime completedAt;
    @ManyToOne
    private User assignedTo;
}
```

### Section: Service Layer Specifications
Design Specification:
- `OnboardingService` for task generation, completion, offboarding

Sample Implementation:
```java
public interface OnboardingService {
    void generateTasksForNewHire(Long employeeId);
    void completeTask(Long taskId);
    void offboardEmployee(Long employeeId);
}
```

### Section: Repository Layer
Design Specification:
- `OnboardingTaskRepository`

### Section: Controller Specifications
Design Specification:
- `/onboarding/tasks`, `/onboarding/offboard`

Sample Implementation:
```java
@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @PostMapping("/tasks")
    public void generateTasks(@RequestParam Long employeeId) { ... }
}
```

### Section: Configuration and Security Settings
Design Specification:
- Only HR/ADMIN can offboard

### Section: Integration Points
Design Specification:
- HRIS, asset, training modules

### Section: Code Snippets
Sample Implementation:
```java
public enum Status { PENDING, COMPLETED }
```

---

## E18: Localization & Multi-Tenant

### Section: Overview of Spring Boot Architecture
Description:
Supports locale-specific formats and multi-tenant isolation.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems.localization`
- `com.wms.ems.tenant`

### Section: Entity Design
Design Specification:
- Tenant (id, name, locale, timezone, currency, active)

Sample Implementation:
```java
@Entity
public class Tenant {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String locale;
    private String timezone;
    private String currency;
    private boolean active;
}
```

### Section: Service Layer Specifications
Design Specification:
- `TenantService` for isolation, context switching

Sample Implementation:
```java
public interface TenantService {
    void setCurrentTenant(String tenantId);
    TenantDTO getCurrentTenant();
}
```

### Section: Repository Layer
Design Specification:
- `TenantRepository`

### Section: Controller Specifications
Design Specification:
- `/tenants`, `/tenants/switch`

Sample Implementation:
```java
@RestController
@RequestMapping("/tenants")
public class TenantController {
    @PostMapping("/switch")
    public void switchTenant(@RequestParam String tenantId) { ... }
}
```

### Section: Configuration and Security Settings
Design Specification:
- LocaleResolver, multi-tenant datasource config

### Section: Integration Points
Design Specification:
- All modules support tenant context

### Section: Code Snippets
Sample Implementation:
```java
@Bean
public LocaleResolver localeResolver() {
    SessionLocaleResolver slr = new SessionLocaleResolver();
    slr.setDefaultLocale(Locale.US);
    return slr;
}
```

---

## E19: Advanced Scheduling AI/ML

### Section: Overview of Spring Boot Architecture
Description:
Predicts staffing needs, auto-suggests shifts, detects anomalies.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems.aiml`
  - `service`
  - `model`
  - `dto`

### Section: Entity Design
Design Specification:
- StaffingPrediction (id, date, department, predictedCount, actualCount, anomalyScore)

Sample Implementation:
```java
@Entity
public class StaffingPrediction {
    @Id @GeneratedValue
    private Long id;
    private LocalDate date;
    private String department;
    private int predictedCount;
    private int actualCount;
    private double anomalyScore;
}
```

### Section: Service Layer Specifications
Design Specification:
- `AiMlService` for prediction, suggestion, anomaly detection

Sample Implementation:
```java
public interface AiMlService {
    StaffingPredictionDTO predictStaffing(LocalDate date, String department);
    List<ShiftDTO> suggestShifts(LocalDate date);
    List<AnomalyDTO> detectAnomalies(LocalDate from, LocalDate to);
}
```

### Section: Repository Layer
Design Specification:
- `StaffingPredictionRepository`

### Section: Controller Specifications
Design Specification:
- `/aiml/predict`, `/aiml/suggest`, `/aiml/anomalies`

Sample Implementation:
```java
@RestController
@RequestMapping("/aiml")
public class AiMlController {
    @GetMapping("/predict")
    public StaffingPredictionDTO predict(@RequestParam LocalDate date, @RequestParam String department) { ... }
}
```

### Section: Configuration and Security Settings
Design Specification:
- Access controlled by role

### Section: Integration Points
Design Specification:
- Scheduling, reporting modules

### Section: Code Snippets
Sample Implementation:
```java
public class AiMlServiceImpl implements AiMlService {
    // Use historical data for prediction
}
```

---

## E20: Continuous Deployment & Observability

### Section: Overview of Spring Boot Architecture
Description:
Implements CI/CD pipeline, blue-green deployments, centralized logging, metrics, and tracing.

### Section: Package Structure, Module Definitions, and Component Breakdown
Design Specification:
- `com.wms.ems.config.cicd`
- `com.wms.ems.config.observability`

### Section: Entity Design
Description:
No entities; configuration only.

### Section: Service Layer Specifications
Description:
No services; configuration only.

### Section: Repository Layer
Description:
No repositories; configuration only.

### Section: Controller Specifications
Description:
No controllers; configuration only.

### Section: Configuration and Security Settings
Design Specification:
- Jenkins/GitHub Actions pipeline config
- Spring Boot Actuator for metrics/tracing
- Logback/ELK for centralized logging

Sample Implementation:
```yaml
# Jenkinsfile or .github/workflows/ci.yml
steps:
  - name: Build
    run: mvn clean install
  - name: Test
    run: mvn test
  - name: Deploy
    run: ./deploy.sh
```
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,trace
  tracing:
    enabled: true
logging:
  level:
    root: INFO
    com.wms.ems: DEBUG
```

### Section: Integration Points
Design Specification:
- CI/CD tools, ELK/Prometheus/Grafana

### Section: Code Snippets
Sample Implementation:
```java
// Enable tracing and metrics
@SpringBootApplication
public class EmsApplication { ... }
```

---

# End of Document

This document provides a production-ready, low-level technical design for all 20 epics of the Warehouse EMS, following Spring Boot best practices, with clear package structure, entity design, service/repository/controller specs, configuration, integration, and code snippets for each epic.