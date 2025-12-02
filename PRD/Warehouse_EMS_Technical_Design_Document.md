# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

---

Section: E01 - Project Scaffolding & Domain Setup  
Description: Establishes the foundational structure for the Warehouse EMS Spring Boot application, ensuring modularity, maintainability, and scalability. Sets up Maven, base packages, core modules, DB migration, and monitoring.  
Design Specification:
- Spring Boot (Maven) project with parent `spring-boot-starter-parent`.
- Base package: `com.company.wems`.
- Core modules: `employee`, `scheduling`, `attendance`, `safety` as sub-packages.
- Directory structure:
  - `com.company.wems.employee`
  - `com.company.wems.scheduling`
  - `com.company.wems.attendance`
  - `com.company.wems.safety`
- DB migration: Flyway or Liquibase configured in `src/main/resources/db/migration`.
- Actuator enabled for health checks.
- Application runs on port 8080.
- README with build/run instructions.

Sample Implementation:
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```
```yaml
# application.yml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wems
    username: wems
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

Section: E02 - Employee Master Data (CRUD)  
Description: Implements the Employee domain with full CRUD REST APIs, DTOs, unique badgeId enforcement, soft-delete, pagination, filtering, and OpenAPI documentation.  
Design Specification:
- Entity: `Employee` with fields: id, name, badgeId (unique), role, department, shiftGroup, hireDate, status, deleted (soft-delete).
- Repository: `EmployeeRepository` extends `JpaRepository<Employee, Long>`.
- Service: `EmployeeService` for business logic, transaction management.
- Controller: `EmployeeController` with endpoints:
  - `POST /employees`
  - `GET /employees`
  - `GET /employees/{id}`
  - `PUT /employees/{id}`
  - `PATCH /employees/{id}`
  - `DELETE /employees/{id}` (soft-delete)
- DTOs: `EmployeeRequestDTO`, `EmployeeResponseDTO`.
- Pagination & filtering via Spring Data JPA.
- Validation with Bean Validation API.
- OpenAPI annotations for schema/examples.

Sample Implementation:
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank private String name;
    @NotBlank private String badgeId;
    @Enumerated(EnumType.STRING) private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING) private Status status;
    private boolean deleted = false;
    // getters/setters
}

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> create(@Valid @RequestBody EmployeeRequestDTO dto) { ... }
    @GetMapping
    public Page<EmployeeResponseDTO> list(Pageable pageable, @RequestParam Map<String, String> filters) { ... }
    // ... other endpoints
}
```

---

Section: E03 - Role Based Access Control (RBAC)  
Description: Secures endpoints and methods using Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER), supports API key/OAuth2 toggle, and enforces row-level constraints.  
Design Specification:
- SecurityConfig class extends `WebSecurityConfigurerAdapter` (or SecurityFilterChain for Spring Boot 3+).
- Roles: ADMIN, HR, SUPERVISOR, WORKER as enums.
- Method-level security with `@PreAuthorize`.
- Endpoint security in `SecurityConfig`.
- Row-level constraints in repository/service.
- API key/OAuth2 toggle via `application.yml`.
- Exception handling for 401/403.
- Security tests for rules.

Sample Implementation:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers(HttpMethod.POST, "/employees/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer().jwt(); // or API key config
        return http.build();
    }
}

@Service
public class EmployeeService {
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == authentication.principal.department)")
    public Employee updateEmployee(Employee employee) { ... }
}
```

---

Section: E04 - Time & Attendance (Clock In/Out)  
Description: Provides endpoints for clock-in/out with validation, geofence/device capture, shift association, missed punch handling, corrections workflow, and CSV export.  
Design Specification:
- Entity: `AttendanceEvent` (id, employee, type [IN/OUT], timestamp, deviceId, location, shift, status).
- Repository: `AttendanceEventRepository`.
- Service: `AttendanceService` for punch logic, shift association, corrections.
- Controller: `AttendanceController`:
  - `POST /attendance/clock-in`
  - `POST /attendance/clock-out`
  - `GET /attendance/report`
- Correction workflow: creates approval tasks.
- CSV export endpoint.

Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private EventType type;
    private LocalDateTime timestamp;
    private String deviceId;
    private String location;
    @ManyToOne private Shift shift;
    @Enumerated(EnumType.STRING) private Status status;
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@Valid @RequestBody ClockInDTO dto) { ... }
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@Valid @RequestBody ClockOutDTO dto) { ... }
    @GetMapping("/report")
    public ResponseEntity<Resource> exportReport(@RequestParam Map<String, String> filters) { ... }
}
```

---

Section: E05 - Shift & Schedule Management  
Description: Manages shift templates, rotations, overtime, employee assignments, blackout dates, and operation calendars. Detects/prevents conflicts, supports bulk assignment, and audit logging.  
Design Specification:
- Entity: `ShiftTemplate`, `ShiftAssignment`, `BlackoutDate`.
- Repository: `ShiftTemplateRepository`, `ShiftAssignmentRepository`.
- Service: `ShiftService` for scheduling logic, conflict detection.
- Controller: `ShiftController`:
  - CRUD for shift templates/schedules
  - Bulk assignment endpoint
- Audit logging on changes.

Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    // ...
}

@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping
    public ResponseEntity<ShiftTemplateDTO> create(@Valid @RequestBody ShiftTemplateDTO dto) { ... }
    @PostMapping("/assign-bulk")
    public ResponseEntity<?> bulkAssign(@RequestBody BulkAssignDTO dto) { ... }
}
```

---

Section: E06 - Leave & Absence Management  
Description: Handles PTO/sick/unpaid leave requests, approvals, accrual policies, and integration with scheduling/payroll. Auto-flags shifts for coverage and supports exports.  
Design Specification:
- Entity: `LeaveRequest` (id, employee, type, startDate, endDate, status, approver, accrualBalance).
- Repository: `LeaveRequestRepository`.
- Service: `LeaveService` for request/approval logic, accruals.
- Controller: `LeaveController`:
  - `POST /leave/request`
  - `POST /leave/approve`
  - `GET /leave/balance`
- Integration hooks for scheduling/payroll.

Sample Implementation:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING) private Status status;
    @ManyToOne private Employee approver;
    private BigDecimal accrualBalance;
}

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/request")
    public ResponseEntity<?> requestLeave(@Valid @RequestBody LeaveRequestDTO dto) { ... }
    @PostMapping("/approve")
    public ResponseEntity<?> approveLeave(@RequestBody ApproveLeaveDTO dto) { ... }
}
```

---

Section: E07 - Training & Certification Tracking  
Description: Tracks employee certifications, expirations, renewals, proof uploads, and blocks assignments for expired certs. Alerts before expiry and exposes status on profiles.  
Design Specification:
- Entity: `Certification` (id, employee, type, issueDate, expiryDate, documentUrl, status).
- Repository: `CertificationRepository`.
- Service: `CertificationService` for CRUD, expiry checks, alerts.
- Controller: `CertificationController`:
  - CRUD endpoints
  - Status endpoint
- File upload for proof documents.

Sample Implementation:
```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
    @Enumerated(EnumType.STRING) private Status status;
}

@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<CertificationDTO> create(@Valid @RequestBody CertificationDTO dto) { ... }
    @GetMapping("/status/{employeeId}")
    public ResponseEntity<CertificationStatusDTO> getStatus(@PathVariable Long employeeId) { ... }
}
```

---

Section: E08 - Safety Incidents & OSHA Reporting  
Description: Records safety incidents/near-misses, manages investigation workflow, and generates OSHA reports and dashboard metrics.  
Design Specification:
- Entity: `SafetyIncident` (id, date, severity, location, description, involvedEmployees, status, correctiveActions).
- Repository: `SafetyIncidentRepository`.
- Service: `SafetyService` for workflow, reporting.
- Controller: `SafetyController`:
  - `POST /safety/incidents`
  - `GET /safety/incidents`
  - Export OSHA reports
- Metrics dashboard endpoints.

Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private LocalDate date;
    private String severity;
    private String location;
    private String description;
    @ManyToMany private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING) private Status status;
    private String correctiveActions;
}

@RestController
@RequestMapping("/safety")
public class SafetyController {
    @PostMapping("/incidents")
    public ResponseEntity<SafetyIncidentDTO> report(@Valid @RequestBody SafetyIncidentDTO dto) { ... }
    @GetMapping("/incidents")
    public Page<SafetyIncidentDTO> list(Pageable pageable) { ... }
}
```

---

Section: E09 - Equipment & Asset Assignment  
Description: Manages asset registry, assignment/check-in/out, certification checks, asset condition, and overdue reports.  
Design Specification:
- Entity: `Asset` (id, type, serialNumber, condition, assignedTo, checkoutDate, returnDate, history).
- Repository: `AssetRepository`.
- Service: `AssetService` for assignment, check-in/out, history.
- Controller: `AssetController`:
  - CRUD endpoints
  - Check-in/out endpoints
  - Overdue report endpoint
- Certification validation before assignment.

Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String type;
    private String serialNumber;
    private String condition;
    @ManyToOne private Employee assignedTo;
    private LocalDateTime checkoutDate;
    private LocalDateTime returnDate;
    @OneToMany(mappedBy = "asset") private List<AssetHistory> history;
}

@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody AssetCheckoutDTO dto) { ... }
    @PostMapping("/checkin")
    public ResponseEntity<?> checkin(@RequestBody AssetCheckinDTO dto) { ... }
}
```

---

Section: E10 - Performance Reviews & Goals  
Description: Supports review templates, goal tracking, ratings, comments, acknowledgements, PDF export, and immutable history.  
Design Specification:
- Entity: `PerformanceReview` (id, employee, cycle, goals, competencies, ratings, comments, supervisorAck, employeeAck, status, pdfUrl).
- Repository: `PerformanceReviewRepository`.
- Service: `PerformanceReviewService` for workflow, PDF export.
- Controller: `PerformanceReviewController`:
  - CRUD endpoints
  - PDF export endpoint
- Immutable after sign-off.

Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String cycle;
    @ElementCollection private List<String> goals;
    @ElementCollection private List<String> competencies;
    private String ratings;
    private String comments;
    private boolean supervisorAck;
    private boolean employeeAck;
    @Enumerated(EnumType.STRING) private Status status;
    private String pdfUrl;
}

@RestController
@RequestMapping("/reviews")
public class PerformanceReviewController {
    @PostMapping
    public ResponseEntity<PerformanceReviewDTO> create(@Valid @RequestBody PerformanceReviewDTO dto) { ... }
    @GetMapping("/export/{id}")
    public ResponseEntity<Resource> exportPdf(@PathVariable Long id) { ... }
}
```

---

Section: E11 - Payroll Export Integration  
Description: Generates payroll-ready files from attendance/leave, maps to provider formats, delivers securely (SFTP/API), and logs exports.  
Design Specification:
- Service: `PayrollExportService` for file generation, mapping, delivery, retry logic.
- Integration: SFTP/API clients.
- Audit logging for each export.
- Controller: `PayrollController`:
  - `POST /payroll/export`
  - `GET /payroll/exports`

Sample Implementation:
```java
@Service
public class PayrollExportService {
    public File generatePayrollFile(LocalDate period) { ... }
    public void deliver(File file) { ... }
}

@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/export")
    public ResponseEntity<?> exportPayroll(@RequestBody PayrollExportRequestDTO dto) { ... }
}
```

---

Section: E12 - Notifications & Announcements  
Description: Delivers in-app/email/SMS notifications for events, supports opt-in/out, localization, delivery tracking, rate limits, and dashboard announcements.  
Design Specification:
- Entity: `Notification`, `Announcement`.
- Service: `NotificationService` for delivery, templates, rate limiting.
- Integration: Email/SMS providers.
- Controller: `NotificationController`, `AnnouncementController`.

Sample Implementation:
```java
@Entity
public class Notification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee recipient;
    private String channel;
    private String content;
    private boolean delivered;
    private LocalDateTime sentAt;
}

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping
    public ResponseEntity<?> send(@RequestBody NotificationDTO dto) { ... }
}
```

---

Section: E13 - Integration Layer (HRIS/WMS APIs)  
Description: Exposes REST APIs and connectors for HRIS (new hires/terms), WMS (location/department), and IDP for SSO; webhooks for events.  
Design Specification:
- REST APIs for HRIS/WMS/IDP.
- JWT/OAuth2 security.
- HRIS sync job for employee create/update.
- WMS integration for department/location.
- Idempotent webhooks.
- OpenAPI documentation.

Sample Implementation:
```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    @PostMapping("/hris/webhook")
    public ResponseEntity<?> hrisWebhook(@RequestBody HRISWebhookDTO dto) { ... }
    @GetMapping("/wms/departments")
    public ResponseEntity<List<DepartmentDTO>> getDepartments() { ... }
}
```

---

Section: E14 - Audit Trail & Compliance  
Description: Centralized audit logging for sensitive changes (employee PII, schedules, approvals, payroll); tamper-evident storage.  
Design Specification:
- Entity: `AuditLog` (id, entity, entityId, actor, timestamp, before, after, action).
- Service: `AuditService` for logging.
- Aspect for automatic audit on create/update/delete.
- Immutable log table.
- Export endpoints.

Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String entity;
    private Long entityId;
    private String actor;
    private LocalDateTime timestamp;
    @Lob private String before;
    @Lob private String after;
    private String action;
}

@Aspect
@Component
public class AuditAspect {
    @AfterReturning(...)
    public void logChange(JoinPoint joinPoint, Object result) { ... }
}
```

---

Section: E15 - Reporting & Analytics  
Description: Operational reports: attendance, overtime, leave balances, certification status, safety KPIs; export CSV/PDF; basic role-based dashboards.  
Design Specification:
- Service: `ReportingService` for report generation.
- Controller: `ReportingController`:
  - `GET /reports/attendance`
  - `GET /reports/overtime`
  - `GET /reports/leave`
  - `GET /reports/certifications`
  - `GET /reports/safety`
- Export CSV/PDF.
- Access control.

Sample Implementation:
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> attendanceReport(@RequestParam Map<String, String> filters) { ... }
}
```

---

Section: E16 - Mobile Access (PWA)  
Description: Responsive views for workers to clock-in/out, view schedules, request leave, see announcements; offline-friendly via PWA.  
Design Specification:
- PWA manifest.
- Service worker for offline support.
- Responsive UI (Thymeleaf/React/Angular).
- Offline queue for clock events.
- Conflict resolution.

Sample Implementation:
```javascript
// service-worker.js
self.addEventListener('fetch', event => {
    event.respondWith(
        caches.match(event.request).then(response => response || fetch(event.request))
    );
});
```

---

Section: E17 - Onboarding & Offboarding Workflow  
Description: Automates provisioning of accounts, initial schedule, required training; deprovision access and assets on termination.  
Design Specification:
- Service: `OnboardingService`, `OffboardingService`.
- Integration with HRIS for new hires/terms.
- Task generation for training/asset assignment.
- Access revocation on offboarding.
- Controller: `OnboardingController`, `OffboardingController`.

Sample Implementation:
```java
@Service
public class OnboardingService {
    public void onboardEmployee(Employee employee) {
        // Create account, assign schedule, generate training tasks
    }
}

@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @PostMapping
    public ResponseEntity<?> onboard(@RequestBody OnboardingDTO dto) { ... }
}
```

---

# End of Technical Design Document