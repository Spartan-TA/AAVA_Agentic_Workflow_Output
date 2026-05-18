Section: Project Scaffolding & Domain Setup  
Description: Establishes the foundational Spring Boot project structure, enabling core modules, database migration, and health monitoring for consistent and rapid delivery.  
Design Specification:  
- Spring Boot (Maven) project initialized  
- Base package: `com.company.warehouse`  
- Core modules: employee, scheduling, attendance, safety  
- DB migration: Flyway or Liquibase configured  
- Actuator enabled for health checks  
- Application runs on port 8080  
- README with build/run instructions  
Sample Implementation:  
```java
// pom.xml includes spring-boot-starter-web, spring-boot-starter-actuator, flyway-core
// src/main/resources/application.yml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse
    username: user
    password: pass
  flyway:
    enabled: true
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

Section: Employee Master Data (CRUD)  
Description: Implements CRUD APIs for employee records, enforcing unique badge IDs, soft-delete, pagination, filtering, and OpenAPI documentation.  
Design Specification:  
- Entity: `Employee` (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)  
- Repository: `EmployeeRepository` extends `JpaRepository<Employee, Long>`  
- Service: `EmployeeService` for business logic  
- Controller: `EmployeeController` with REST endpoints  
- Soft-delete via `deleted` flag  
- Pagination/filtering via Spring Data  
- OpenAPI (springdoc-openapi) for API docs  
Sample Implementation:  
```java
@Entity
public class Employee {
  @Id @GeneratedValue private Long id;
  @Column(unique = true) private String badgeId;
  private String name, role, department, shiftGroup;
  private LocalDate hireDate;
  private String status;
  private boolean deleted = false;
}
@RestController
@RequestMapping("/employees")
public class EmployeeController {
  @GetMapping public Page<Employee> list(Pageable pageable, @RequestParam Map<String,String> filters) { ... }
  @PostMapping public Employee create(@RequestBody EmployeeDto dto) { ... }
  // PUT, PATCH, DELETE endpoints
}
```

Section: Role-Based Access Control (RBAC)  
Description: Secures endpoints and methods using Spring Security, supporting roles (ADMIN, HR, SUPERVISOR, WORKER), with API key/OAuth2 toggle.  
Design Specification:  
- SecurityConfig with role-based access  
- Method/endpoint security via `@PreAuthorize`  
- Row-level security for SUPERVISOR  
- API key/OAuth2 toggle via config  
Sample Implementation:  
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
      .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
      .antMatchers("/admin/**").hasRole("ADMIN")
      .anyRequest().authenticated()
      .and().oauth2ResourceServer().jwt();
  }
}
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == authentication.principal.department)")
public void updateEmployee(Employee employee) { ... }
```

Section: Time & Attendance (Clock In/Out)  
Description: Provides endpoints for clock-in/out events, device/location capture, shift association, corrections workflow, and exportable reports.  
Design Specification:  
- Entity: `AttendanceEvent` (id, employee, type, timestamp, deviceId, location, shift, approved, correctionRequested)  
- Controller: `/attendance/clock-in`, `/attendance/clock-out`  
- Service: computes daily totals, handles corrections  
- Reports exportable as CSV  
Sample Implementation:  
```java
@Entity
public class AttendanceEvent {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String type; // CLOCK_IN, CLOCK_OUT
  private LocalDateTime timestamp;
  private String deviceId, location;
  @ManyToOne private Shift shift;
  private boolean approved, correctionRequested;
}
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
  @PostMapping("/clock-in") public ResponseEntity<?> clockIn(@RequestBody ClockEventDto dto) { ... }
  @PostMapping("/clock-out") public ResponseEntity<?> clockOut(@RequestBody ClockEventDto dto) { ... }
}
```

Section: Shift & Schedule Management  
Description: Manages shift templates, assignments, conflict detection, and audit logging for staffing optimization.  
Design Specification:  
- Entity: `ShiftTemplate`, `ShiftAssignment`  
- Controller: `/shifts`, `/schedules`  
- Service: conflict detection, bulk assignment, audit logging  
Sample Implementation:  
```java
@Entity
public class ShiftTemplate {
  @Id @GeneratedValue private Long id;
  private String name;
  private LocalTime start, end;
  private String recurrenceRule;
}
@Entity
public class ShiftAssignment {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  @ManyToOne private ShiftTemplate template;
  private LocalDate date;
}
@RestController
@RequestMapping("/shifts")
public class ShiftController {
  @PostMapping public ShiftTemplate createTemplate(@RequestBody ShiftTemplateDto dto) { ... }
  @PostMapping("/assign") public void assignShift(@RequestBody ShiftAssignmentDto dto) { ... }
}
```

Section: Leave & Absence Management  
Description: Enables leave requests, approvals, balance tracking, and integration with scheduling/payroll.  
Design Specification:  
- Entity: `LeaveRequest` (id, employee, type, startDate, endDate, status, approvedBy, balanceImpact)  
- Controller: `/leave`  
- Service: approval workflow, balance update, export  
Sample Implementation:  
```java
@Entity
public class LeaveRequest {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String type; // PTO, SICK, UNPAID
  private LocalDate startDate, endDate;
  private String status; // REQUESTED, APPROVED, DENIED
  private String approvedBy;
  private int balanceImpact;
}
@RestController
@RequestMapping("/leave")
public class LeaveController {
  @PostMapping public LeaveRequest requestLeave(@RequestBody LeaveRequestDto dto) { ... }
  @PatchMapping("/{id}/approve") public LeaveRequest approve(@PathVariable Long id) { ... }
}
```

Section: Training & Certification Tracking  
Description: Tracks employee certifications, expirations, proof uploads, and blocks unqualified assignments.  
Design Specification:  
- Entity: `Certification` (id, employee, type, issueDate, expiryDate, proofDocument)  
- Alerts for expiry (30/7 days)  
- Scheduling checks for valid certifications  
Sample Implementation:  
```java
@Entity
public class Certification {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String type;
  private LocalDate issueDate, expiryDate;
  private String proofDocument; // file path or URL
}
@Service
public class CertificationService {
  public void checkAndAlertExpirations() { ... }
}
```

Section: Safety Incidents & OSHA Reporting  
Description: Records safety incidents, manages workflow, and generates OSHA-compliant reports and dashboards.  
Design Specification:  
- Entity: `SafetyIncident` (id, type, severity, location, description, status, involvedEmployees, correctiveActions)  
- Controller: `/safety/incidents`  
- Workflow: Open â Investigating â Resolved  
- OSHA export endpoints  
Sample Implementation:  
```java
@Entity
public class SafetyIncident {
  @Id @GeneratedValue private Long id;
  private String type, severity, location, description, status;
  @ManyToMany private List<Employee> involvedEmployees;
  private String correctiveActions;
}
@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
  @PostMapping public SafetyIncident report(@RequestBody SafetyIncidentDto dto) { ... }
}
```

Section: Equipment & Asset Assignment  
Description: Assigns and tracks equipment, enforces certification checks, logs history, and reports overdue returns.  
Design Specification:  
- Entity: `Asset`, `AssetAssignment`  
- Controller: `/assets`  
- Service: check-in/out, certification validation, history log  
Sample Implementation:  
```java
@Entity
public class Asset {
  @Id @GeneratedValue private Long id;
  private String name, type, condition;
}
@Entity
public class AssetAssignment {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Asset asset;
  @ManyToOne private Employee employee;
  private LocalDateTime checkedOutAt, returnedAt;
}
@RestController
@RequestMapping("/assets")
public class AssetController {
  @PostMapping("/assign") public void assignAsset(@RequestBody AssetAssignmentDto dto) { ... }
}
```

Section: Performance Reviews & Goals  
Description: Supports review cycles, goal tracking, workflow, PDF export, and immutable history post sign-off.  
Design Specification:  
- Entity: `PerformanceReview` (id, employee, cycle, goals, ratings, comments, status, signedOff)  
- Controller: `/reviews`  
- PDF export endpoint  
Sample Implementation:  
```java
@Entity
public class PerformanceReview {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String cycle, goals, ratings, comments, status;
  private boolean signedOff;
}
@RestController
@RequestMapping("/reviews")
public class ReviewController {
  @PostMapping public PerformanceReview create(@RequestBody ReviewDto dto) { ... }
  @GetMapping("/{id}/export") public ResponseEntity<Resource> exportPdf(@PathVariable Long id) { ... }
}
```

Section: Payroll Export Integration  
Description: Generates payroll-ready files from attendance/leave, delivers securely, and logs exports.  
Design Specification:  
- Service: `PayrollExportService`  
- Secure delivery via SFTP/API  
- Audit log for exports  
Sample Implementation:  
```java
@Service
public class PayrollExportService {
  public void exportPayroll() { /* generate file, deliver via SFTP/API, log export */ }
}
```

Section: Notifications & Announcements  
Description: Sends notifications via in-app, email, SMS; supports opt-in/out, localization, delivery tracking, and dashboard visibility.  
Design Specification:  
- Entity: `Notification`, `Announcement`  
- Controller: `/notifications`, `/announcements`  
- Service: delivery, rate limiting, quiet hours  
Sample Implementation:  
```java
@Entity
public class Notification {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee recipient;
  private String channel, template, status, localizedContent;
}
@RestController
@RequestMapping("/notifications")
public class NotificationController {
  @PostMapping public void send(@RequestBody NotificationDto dto) { ... }
}
```

Section: Integration Layer (HRIS/WMS APIs)  
Description: Exposes REST APIs and connectors for HRIS, WMS, and IDP, with JWT/OAuth2 security and idempotent webhooks.  
Design Specification:  
- REST controllers for HRIS/WMS  
- JWT/OAuth2 security  
- Webhook endpoints  
- OpenAPI documentation  
Sample Implementation:  
```java
@RestController
@RequestMapping("/api/hris")
public class HRISController {
  @PostMapping("/sync") public void syncEmployees(@RequestBody List<EmployeeDto> dtos) { ... }
}
```

Section: Audit Trail & Compliance  
Description: Centralized, immutable audit logging for sensitive changes, with export and test coverage.  
Design Specification:  
- Entity: `AuditLog` (id, actor, timestamp, entity, before, after, action)  
- Service: log creation, export  
- Tamper-evident storage  
Sample Implementation:  
```java
@Entity
public class AuditLog {
  @Id @GeneratedValue private Long id;
  private String actor, entity, action;
  private LocalDateTime timestamp;
  @Lob private String before, after;
}
@Service
public class AuditService {
  public void logChange(String actor, String entity, String action, Object before, Object after) { ... }
}
```

Section: Reporting & Analytics  
Description: Provides reporting endpoints, filters, exports (CSV/PDF), and role-based dashboards for operational insights.  
Design Specification:  
- Controller: `/reports`  
- Service: report generation, export  
- Access control  
Sample Implementation:  
```java
@RestController
@RequestMapping("/reports")
public class ReportController {
  @GetMapping public List<Report> getReports(@RequestParam Map<String,String> filters) { ... }
  @GetMapping("/export") public ResponseEntity<Resource> export(@RequestParam String type) { ... }
}
```

Section: Mobile Access (PWA)  
Description: Enables mobile/PWA endpoints for core flows, offline queue for clock events, and installable manifest.  
Design Specification:  
- Controller: mobile-optimized endpoints  
- PWA manifest in `resources/static/manifest.json`  
- Offline queue for clock events  
Sample Implementation:  
```json
// resources/static/manifest.json
{
  "name": "Warehouse Employee Portal",
  "short_name": "Warehouse",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

Section: Onboarding & Offboarding Workflow  
Description: Automates onboarding/offboarding tasks, access provisioning, asset assignment, and schedule updates, integrating with HRIS.  
Design Specification:  
- Service: onboarding/offboarding workflow  
- HRIS integration  
- Task generation for training, asset assignment  
Sample Implementation:  
```java
@Service
public class OnboardingService {
  public void onboard(Employee employee) { /* create tasks, provision access, assign assets */ }
  public void offboard(Employee employee) { /* revoke access, collect assets, update schedules */ }
}
```

---

GITHUB TOOL UPLOAD STATUS:  
Attempting upload...
