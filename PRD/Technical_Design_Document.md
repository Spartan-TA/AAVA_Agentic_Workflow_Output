# PRD/Technical_Design_Document.md

## Warehouse Employee Management System - Low-Level Technical Design Document

---

### Epic E01: Project Scaffolding & Domain Setup

Section: Spring Boot Architecture Overview
Description: Establishes the foundational structure for the application, ensuring modularity and scalability. Core modules (employee, scheduling, attendance, safety) are separated for maintainability. Flyway/Liquibase is used for DB migrations, and Actuator for health monitoring.
Design Specification:
- Maven multi-module project structure
- Base package: `com.warehouse.employee`
- Modules: `employee`, `scheduling`, `attendance`, `safety`
- Flyway/Liquibase migration scripts in `src/main/resources/db/migration`
- Actuator enabled in `application.properties`
Sample Implementation:
```java
@SpringBootApplication
public class WarehouseEmployeeManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeManagementApplication.class, args);
    }
}
```
```properties
management.endpoints.web.exposure.include=health,info
spring.flyway.enabled=true
```

Section: Package Structure
Description: Follows Spring Boot best practices for separation of concerns and scalability.
Design Specification:
- `com.warehouse.employee.domain`
- `com.warehouse.employee.service`
- `com.warehouse.employee.repository`
- `com.warehouse.employee.controller`
- `com.warehouse.employee.dto`
- `com.warehouse.employee.config`
Sample Implementation:
```
src/main/java/com/warehouse/employee/domain/Employee.java
src/main/java/com/warehouse/employee/service/EmployeeService.java
src/main/java/com/warehouse/employee/repository/EmployeeRepository.java
src/main/java/com/warehouse/employee/controller/EmployeeController.java
src/main/java/com/warehouse/employee/dto/EmployeeDTO.java
src/main/java/com/warehouse/employee/config/SecurityConfig.java
```

---

### Epic E02: Employee Master Data (CRUD)

Section: Entity Design
Description: Employee entity is the central domain model, representing warehouse staff with all relevant attributes.
Design Specification:
- Fields: id (Long), name (String), badgeId (String, unique), role (Enum), department (String), shiftGroup (String), hireDate (LocalDate), status (Enum)
- JPA annotations for persistence
- Validation constraints for required fields
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
    private Role role;

    private String department;
    private String shiftGroup;

    @PastOrPresent
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    // getters and setters
}
```

Section: Service Layer
Description: Handles business logic for employee CRUD operations, including validation and soft-delete.
Design Specification:
- Interface: `EmployeeService`
- Methods: `createEmployee`, `getEmployee`, `updateEmployee`, `deleteEmployee`, `listEmployees`
- Soft-delete implemented via status field
- Transactional boundaries
Sample Implementation:
```java
public interface EmployeeService {
    EmployeeDTO createEmployee(EmployeeDTO dto);
    EmployeeDTO getEmployee(Long id);
    EmployeeDTO updateEmployee(Long id, EmployeeDTO dto);
    void deleteEmployee(Long id);
    Page<EmployeeDTO> listEmployees(Pageable pageable, EmployeeFilter filter);
}
```
```java
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    // Implementation details
}
```

Section: Repository Layer
Description: Uses Spring Data JPA for persistence and custom queries for filtering/pagination.
Design Specification:
- Interface: `EmployeeRepository extends JpaRepository<Employee, Long>`
- Custom query for filtering by department, role, status
Sample Implementation:
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findByDepartmentAndStatus(String department, Status status, Pageable pageable);
}
```

Section: Controller Specifications
Description: RESTful endpoints for employee CRUD, supporting pagination, filtering, and OpenAPI documentation.
Design Specification:
- Endpoints: `/employees` (GET, POST, PUT, PATCH, DELETE)
- Request/Response DTOs
- Validation annotations
- OpenAPI/Swagger annotations
Sample Implementation:
```java
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee management APIs")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeDTO dto) { ... }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployee(@PathVariable Long id) { ... }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) { ... }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) { ... }

    @GetMapping
    public ResponseEntity<Page<EmployeeDTO>> listEmployees(
        @RequestParam(required = false) String department,
        @RequestParam(required = false) Status status,
        Pageable pageable) { ... }
}
```

Section: Configuration & Security
Description: Secures endpoints and configures application properties for employee module.
Design Specification:
- Security rules for employee endpoints
- Application properties for pagination defaults
Sample Implementation:
```properties
spring.data.web.pageable.default-page-size=20
spring.data.web.pageable.max-page-size=100
```

---

### Epic E03: Role-Based Access Control (RBAC)

Section: Spring Security Configuration
Description: Implements RBAC using Spring Security, supporting roles and method/endpoint security.
Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Method-level security with `@PreAuthorize`
- API key/OAuth2 toggle via config
Sample Implementation:
```java
@Configuration
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
            .oauth2Login()
            .and()
            .apiKeyAuthenticationFilter();
    }
}
```
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == authentication.principal.department)")
public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) { ... }
```

Section: Row-Level Security
Description: Restricts access to records based on user's department/team.
Design Specification:
- Custom security expressions for row-level constraints
Sample Implementation:
```java
@PreAuthorize("@securityService.canAccessEmployee(authentication, #employeeId)")
public EmployeeDTO getEmployee(Long employeeId) { ... }
```

---

### Epic E04: Time & Attendance (Clock In/Out)

Section: Entity Design
Description: Attendance entity records clock-in/out events, device info, geofence, and links to Employee.
Design Specification:
- Fields: id, employee (ManyToOne), clockInTime, clockOutTime, deviceId, location, status, correctionRequested
Sample Implementation:
```java
@Entity
public class Attendance {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private String deviceId;
    private String location;
    private AttendanceStatus status;
    private boolean correctionRequested;
}
```

Section: Controller Specifications
Description: Endpoints for clock-in/out, corrections, and attendance reports.
Design Specification:
- POST `/attendance/clock-in`
- POST `/attendance/clock-out`
- POST `/attendance/corrections`
- GET `/attendance/reports`
Sample Implementation:
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<Void> clockIn(@RequestBody ClockInDTO dto) { ... }

    @PostMapping("/clock-out")
    public ResponseEntity<Void> clockOut(@RequestBody ClockOutDTO dto) { ... }

    @PostMapping("/corrections")
    public ResponseEntity<Void> requestCorrection(@RequestBody CorrectionDTO dto) { ... }

    @GetMapping("/reports")
    public ResponseEntity<List<AttendanceReportDTO>> getReports(...) { ... }
}
```

Section: Service Layer
Description: Calculates hours worked, associates shifts, handles missed punches and corrections.
Design Specification:
- Methods: `clockIn`, `clockOut`, `requestCorrection`, `generateReport`
- Transactional boundaries
Sample Implementation:
```java
@Service
public class AttendanceService {
    public void clockIn(ClockInDTO dto) { ... }
    public void clockOut(ClockOutDTO dto) { ... }
    public void requestCorrection(CorrectionDTO dto) { ... }
    public List<AttendanceReportDTO> generateReport(...) { ... }
}
```

---

### Epic E05: Shift & Schedule Management

Section: Entity Design
Description: ShiftTemplate and Schedule entities model recurring shifts, rotations, and assignments.
Design Specification:
- ShiftTemplate: id, name, startTime, endTime, recurrencePattern, overtimeRules
- Schedule: id, employee (ManyToOne), shiftTemplate (ManyToOne), date, status
Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrencePattern;
    private String overtimeRules;
}
@Entity
public class Schedule {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ShiftTemplate shiftTemplate;
    private LocalDate date;
    private ScheduleStatus status;
}
```

Section: Controller Specifications
Description: Endpoints for managing shift templates, schedules, and assignments.
Design Specification:
- CRUD `/shifts/templates`
- CRUD `/schedules`
- Bulk assignment `/schedules/bulk-assign`
Sample Implementation:
```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates")
    public ResponseEntity<ShiftTemplateDTO> createTemplate(@RequestBody ShiftTemplateDTO dto) { ... }
    @GetMapping("/templates")
    public ResponseEntity<List<ShiftTemplateDTO>> listTemplates() { ... }
}
@RestController
@RequestMapping("/schedules")
public class ScheduleController {
    @PostMapping
    public ResponseEntity<ScheduleDTO> createSchedule(@RequestBody ScheduleDTO dto) { ... }
    @PostMapping("/bulk-assign")
    public ResponseEntity<Void> bulkAssign(@RequestBody BulkAssignDTO dto) { ... }
}
```

---

### Epic E06: Leave & Absence Management

Section: Entity Design
Description: LeaveRequest entity tracks PTO, sick, unpaid leave, accruals, and approval workflow.
Design Specification:
- Fields: id, employee (ManyToOne), type (Enum), startDate, endDate, status (Enum), accrualBalance
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
    private BigDecimal accrualBalance;
}
```

Section: Controller Specifications
Description: Endpoints for requesting, approving, and exporting leave.
Design Specification:
- POST `/leave/requests`
- PUT `/leave/requests/{id}/approve`
- GET `/leave/exports`
Sample Implementation:
```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/requests")
    public ResponseEntity<LeaveRequestDTO> requestLeave(@RequestBody LeaveRequestDTO dto) { ... }
    @PutMapping("/requests/{id}/approve")
    public ResponseEntity<Void> approveLeave(@PathVariable Long id) { ... }
    @GetMapping("/exports")
    public ResponseEntity<Resource> exportLeaves(...) { ... }
}
```

---

### Epic E07: Training & Certification Tracking

Section: Entity Design
Description: Certification entity tracks required certifications, expirations, renewals, and proof documents.
Design Specification:
- Fields: id, employee (ManyToOne), type (Enum), issueDate, expiryDate, proofDocumentUrl, status
Sample Implementation:
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @Enumerated(EnumType.STRING)
    private CertificationType type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String proofDocumentUrl;
    private CertificationStatus status;
}
```

Section: Controller Specifications
Description: Endpoints for managing certifications, alerts, and scheduling checks.
Design Specification:
- CRUD `/certifications`
- GET `/certifications/alerts`
Sample Implementation:
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<CertificationDTO> createCertification(@RequestBody CertificationDTO dto) { ... }
    @GetMapping("/alerts")
    public ResponseEntity<List<CertificationAlertDTO>> getAlerts() { ... }
}
```

---

### Epic E08: Safety Incidents & OSHA Reporting

Section: Entity Design
Description: SafetyIncident entity records incidents, severity, location, involved employees, and workflow status.
Design Specification:
- Fields: id, description, severity, location, involvedEmployees (ManyToMany), status, correctiveActions
Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String description;
    private String severity;
    private String location;
    @ManyToMany
    private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
    private String correctiveActions;
}
```

Section: Controller Specifications
Description: Endpoints for incident reporting, workflow, and OSHA export.
Design Specification:
- POST `/safety/incidents`
- PUT `/safety/incidents/{id}/status`
- GET `/safety/osha/export`
Sample Implementation:
```java
@RestController
@RequestMapping("/safety")
public class SafetyController {
    @PostMapping("/incidents")
    public ResponseEntity<SafetyIncidentDTO> reportIncident(@RequestBody SafetyIncidentDTO dto) { ... }
    @PutMapping("/incidents/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody IncidentStatusDTO dto) { ... }
    @GetMapping("/osha/export")
    public ResponseEntity<Resource> exportOSHA() { ... }
}
```

---

### Epic E09: Equipment & Asset Assignment

Section: Entity Design
Description: Asset entity tracks equipment, assignment, condition, and history.
Design Specification:
- Fields: id, type, serialNumber, assignedTo (ManyToOne), condition, checkoutTime, returnTime, history (OneToMany)
Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    @ManyToOne
    private Employee assignedTo;
    private String condition;
    private LocalDateTime checkoutTime;
    private LocalDateTime returnTime;
    @OneToMany(mappedBy = "asset")
    private List<AssetHistory> history;
}
```

Section: Controller Specifications
Description: Endpoints for asset registry, check-in/out, and history.
Design Specification:
- CRUD `/assets`
- POST `/assets/{id}/checkout`
- POST `/assets/{id}/return`
- GET `/assets/{id}/history`
Sample Implementation:
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/{id}/checkout")
    public ResponseEntity<Void> checkoutAsset(@PathVariable Long id, @RequestBody CheckoutDTO dto) { ... }
    @PostMapping("/{id}/return")
    public ResponseEntity<Void> returnAsset(@PathVariable Long id, @RequestBody ReturnDTO dto) { ... }
    @GetMapping("/{id}/history")
    public ResponseEntity<List<AssetHistoryDTO>> getHistory(@PathVariable Long id) { ... }
}
```

---

### Epic E10: Performance Reviews & Goals

Section: Entity Design
Description: PerformanceReview entity tracks review cycles, goals, competencies, ratings, comments, and acknowledgements.
Design Specification:
- Fields: id, employee (ManyToOne), cycle, goals, competencies, ratings, comments, supervisorAcknowledged, employeeAcknowledged
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
    @ElementCollection
    private List<Integer> ratings;
    private String comments;
    private boolean supervisorAcknowledged;
    private boolean employeeAcknowledged;
}
```

Section: Controller Specifications
Description: Endpoints for review cycles, submission, acknowledgement, and export.
Design Specification:
- CRUD `/reviews`
- POST `/reviews/{id}/acknowledge`
- GET `/reviews/export`
Sample Implementation:
```java
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<Void> acknowledgeReview(@PathVariable Long id, @RequestBody AcknowledgeDTO dto) { ... }
    @GetMapping("/export")
    public ResponseEntity<Resource> exportReviews() { ... }
}
```

---

### Epic E11: Payroll Export Integration

Section: Integration Points
Description: Generates payroll-ready files from attendance and leave, mapping to external provider formats, delivered via SFTP/API.
Design Specification:
- Service: `PayrollExportService`
- Methods: `generateExport`, `deliverExport`
- SFTP/API integration
Sample Implementation:
```java
@Service
public class PayrollExportService {
    public Resource generateExport(PayrollExportRequest request) { ... }
    public void deliverExport(Resource export, DeliveryMethod method) { ... }
}
```

Section: Controller Specifications
Description: Endpoints for triggering payroll export and viewing audit logs.
Design Specification:
- POST `/payroll/export`
- GET `/payroll/audit`
Sample Implementation:
```java
@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/export")
    public ResponseEntity<Void> exportPayroll(@RequestBody PayrollExportRequest request) { ... }
    @GetMapping("/audit")
    public ResponseEntity<List<PayrollAuditDTO>> getAuditLog() { ... }
}
```

---

### Epic E12: Notifications & Announcements

Section: Entity Design
Description: Notification entity tracks in-app, email, SMS notifications, and announcements.
Design Specification:
- Fields: id, recipient (ManyToOne), type, channel, message, status, sentTime
Sample Implementation:
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee recipient;
    private String type;
    private String channel;
    private String message;
    private NotificationStatus status;
    private LocalDateTime sentTime;
}
```

Section: Controller Specifications
Description: Endpoints for sending notifications, managing preferences, and viewing announcements.
Design Specification:
- POST `/notifications`
- PUT `/notifications/preferences`
- GET `/announcements`
Sample Implementation:
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationDTO dto) { ... }
    @PutMapping("/preferences")
    public ResponseEntity<Void> updatePreferences(@RequestBody PreferencesDTO dto) { ... }
}
```

---

### Epic E13: Integration Layer (HRIS/WMS APIs)

Section: Integration Points
Description: Exposes REST APIs and connectors for HRIS, WMS, IDP, and webhooks.
Design Specification:
- JWT/OAuth2-secured APIs
- HRIS sync job for new hires/terms
- WMS link for department/location
- Idempotent webhooks
Sample Implementation:
```java
@RestController
@RequestMapping("/integrations")
public class IntegrationController {
    @PostMapping("/hris/sync")
    public ResponseEntity<Void> syncHRIS(@RequestBody HRISSyncDTO dto) { ... }
    @PostMapping("/webhooks")
    public ResponseEntity<Void> handleWebhook(@RequestBody WebhookDTO dto) { ... }
}
```

---

### Epic E14: Audit Trail & Compliance

Section: Entity Design
Description: AuditLog entity records all sensitive changes with actor, timestamp, before/after values.
Design Specification:
- Fields: id, actor, timestamp, entityType, entityId, action, beforeValue, afterValue
Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entityType;
    private Long entityId;
    private String action;
    private String beforeValue;
    private String afterValue;
}
```

Section: Controller Specifications
Description: Endpoints for querying audit logs and exporting.
Design Specification:
- GET `/audit`
- GET `/audit/export`
Sample Implementation:
```java
@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping
    public ResponseEntity<Page<AuditLogDTO>> getAuditLogs(Pageable pageable) { ... }
    @GetMapping("/export")
    public ResponseEntity<Resource> exportAuditLogs() { ... }
}
```

---

### Epic E15: Reporting & Analytics

Section: Controller Specifications
Description: Endpoints for operational reports, exports, and dashboards.
Design Specification:
- GET `/reports/attendance`
- GET `/reports/overtime`
- GET `/reports/leave-balances`
- GET `/reports/certifications`
- GET `/reports/safety-kpis`
Sample Implementation:
```java
@RestController
@RequestMapping("/reports")
public class ReportController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> getAttendanceReport(...) { ... }
    @GetMapping("/overtime")
    public ResponseEntity<Resource> getOvertimeReport(...) { ... }
}
```

---

### Epic E16: Mobile Access (PWA)

Section: Configuration & Security
Description: Responsive views for mobile, PWA manifest, offline queue for clock events.
Design Specification:
- PWA manifest in `src/main/resources/static/manifest.json`
- Service worker for offline support
- Lighthouse PWA score â¥ 80
Sample Implementation:
```json
{
  "name": "Warehouse Employee Management",
  "short_name": "WEM",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#000000",
  "icons": [...]
}
```

---

### Epic E17: Onboarding & Offboarding Workflow

Section: Service Layer
Description: Automates provisioning and deprovisioning of accounts, schedules, training, and assets.
Design Specification:
- Methods: `onboardEmployee`, `offboardEmployee`
- Integration with HRIS, training, asset modules
Sample Implementation:
```java
@Service
public class OnboardingService {
    public void onboardEmployee(Employee employee) { ... }
    public void offboardEmployee(Employee employee) { ... }
}
```

Section: Controller Specifications
Description: Endpoints for triggering onboarding/offboarding workflows.
Design Specification:
- POST `/onboarding`
- POST `/offboarding`
Sample Implementation:
```java
@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @PostMapping
    public ResponseEntity<Void> onboardEmployee(@RequestBody OnboardingDTO dto) { ... }
}
@RestController
@RequestMapping("/offboarding")
public class OffboardingController {
    @PostMapping
    public ResponseEntity<Void> offboardEmployee(@RequestBody OffboardingDTO dto) { ... }
}
```

---

## Conclusion

This document provides a comprehensive low-level technical design for all user stories in the Warehouse Employee Management System, structured according to Spring Boot best practices and industry standards. Each epic is broken down into detailed sections covering architecture, package structure, entity design, service layer, repository layer, controller specifications, configuration, security, integration points, and code snippets.