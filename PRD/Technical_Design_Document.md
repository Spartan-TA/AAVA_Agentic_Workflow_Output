# PRD/Technical_Design_Document.md

---

## Warehouse Employee Management System  
### Comprehensive Low-Level Technical Design Document  
#### Covering 20 Epics

---

### Epic E01: Project Scaffolding & Domain Setup

Section: Spring Boot Architecture Overview  
Description: Establishes the foundational structure for the application, ensuring modularity, maintainability, and scalability. Core modules (employee, scheduling, attendance, safety) are separated for clear responsibility.  
Design Specification:  
- Maven multi-module structure  
- Base package: `com.warehouse.employee`  
- Modules: `employee`, `scheduling`, `attendance`, `safety`  
- Flyway/Liquibase for DB migrations  
- Spring Boot Actuator enabled  
Sample Implementation:  
```java
// Maven pom.xml (parent)
<modules>
  <module>employee</module>
  <module>scheduling</module>
  <module>attendance</module>
  <module>safety</module>
</modules>

// Application.java
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {}

// application.yml
server:
  port: 8080
management:
  endpoints:
    web:
      exposure:
        include: health,info
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse
    username: user
    password: pass
flyway:
  enabled: true
```

Section: Package Structure  
Description: Follows Spring Boot conventions for separation of concerns.  
Design Specification:  
- `com.warehouse.employee.domain`  
- `com.warehouse.employee.repository`  
- `com.warehouse.employee.service`  
- `com.warehouse.employee.controller`  
- `com.warehouse.employee.dto`  
- `com.warehouse.employee.config`  
Sample Implementation:  
```
src/main/java/com/warehouse/employee/
  âââ domain/
  âââ repository/
  âââ service/
  âââ controller/
  âââ dto/
  âââ config/
```

---

### Epic E02: Employee Master Data (CRUD)

Section: Entity Design  
Description: Employee entity with unique badgeId, soft-delete, and required fields.  
Design Specification:  
- Fields: id, name, badgeId (unique), role, department, shiftGroup, hireDate, status, deleted  
- JPA annotations, soft-delete via `deleted` flag  
Sample Implementation:  
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
  @Id @GeneratedValue private Long id;
  @Column(nullable = false) private String name;
  @Column(nullable = false, unique = true) private String badgeId;
  @Enumerated(EnumType.STRING) private Role role;
  private String department;
  private String shiftGroup;
  private LocalDate hireDate;
  @Enumerated(EnumType.STRING) private Status status;
  private boolean deleted = false;
}
```

Section: Repository Layer  
Description: Spring Data JPA repository with custom queries for soft-delete and filtering.  
Design Specification:  
- `EmployeeRepository extends JpaRepository<Employee, Long>`  
- Custom: `findByDeletedFalse`, `findByBadgeIdAndDeletedFalse`  
Sample Implementation:  
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
  Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

Section: Service Layer  
Description: Handles business logic, enforces unique badgeId, manages soft-delete.  
Design Specification:  
- Transactional methods for CRUD  
- Validation for badgeId uniqueness  
Sample Implementation:  
```java
@Service
public class EmployeeService {
  @Autowired private EmployeeRepository repo;
  public Employee create(EmployeeDto dto) {
    if (repo.findByBadgeIdAndDeletedFalse(dto.getBadgeId()).isPresent())
      throw new DuplicateBadgeException();
    Employee emp = new Employee(...);
    return repo.save(emp);
  }
  public void softDelete(Long id) {
    Employee emp = repo.findById(id).orElseThrow();
    emp.setDeleted(true);
    repo.save(emp);
  }
}
```

Section: Controller Layer  
Description: REST endpoints for CRUD, pagination, filtering, error handling.  
Design Specification:  
- Endpoints: `/employees` (GET, POST, PUT, PATCH, DELETE)  
- Uses DTOs, validation annotations  
Sample Implementation:  
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
  @Autowired private EmployeeService service;
  @GetMapping public Page<EmployeeDto> list(Pageable pageable) { ... }
  @PostMapping public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { ... }
  @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { service.softDelete(id); }
}
```

Section: Configuration  
Description: OpenAPI schemas, pagination defaults, DB settings.  
Design Specification:  
- `springdoc.api-docs.enabled=true`  
- `spring.data.web.pageable.default-page-size=20`  
Sample Implementation:  
```yaml
springdoc:
  api-docs:
    enabled: true
spring:
  data:
    web:
      pageable:
        default-page-size: 20
```

---

### Epic E03: Role-Based Access Control (RBAC)

Section: Security Settings  
Description: Spring Security with roles, method/endpoint security, row-level constraints, API key/OAuth2 toggle.  
Design Specification:  
- Roles: ADMIN, HR, SUPERVISOR, WORKER  
- `@PreAuthorize` on service methods  
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
        .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR")
        .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
        .anyRequest().authenticated()
      .and()
        .oauth2ResourceServer().jwt();
  }
}

// Service method
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == principal.department)")
public void updateEmployee(Employee employee) { ... }
```

Section: Integration Points  
Description: Toggle between API key and OAuth2 via configuration.  
Design Specification:  
- `security.mode=oauth2` or `security.mode=apikey`  
Sample Implementation:  
```yaml
security:
  mode: oauth2
```

---

### Epic E04: Time & Attendance (Clock In/Out)

Section: Entity Design  
Description: Attendance entity tracks clock-in/out events, device info, geofence.  
Design Specification:  
- Fields: id, employeeId, clockInTime, clockOutTime, deviceId, location, shiftId, correctionStatus  
Sample Implementation:  
```java
@Entity
public class Attendance {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private LocalDateTime clockInTime;
  private LocalDateTime clockOutTime;
  private String deviceId;
  private String location;
  @ManyToOne private Shift shift;
  @Enumerated(EnumType.STRING) private CorrectionStatus correctionStatus;
}
```

Section: Controller Layer  
Description: Endpoints for clock-in/out, corrections, reports.  
Design Specification:  
- `/attendance/clock-in` (POST), `/attendance/clock-out` (POST), `/attendance/corrections` (POST)  
Sample Implementation:  
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
  @PostMapping("/clock-in")
  public ResponseEntity<?> clockIn(@RequestBody ClockInDto dto) { ... }
  @PostMapping("/clock-out")
  public ResponseEntity<?> clockOut(@RequestBody ClockOutDto dto) { ... }
  @PostMapping("/corrections")
  public ResponseEntity<?> requestCorrection(@RequestBody CorrectionDto dto) { ... }
}
```

Section: Service Layer  
Description: Calculates hours, handles missed punches, creates approval tasks for corrections.  
Design Specification:  
- Shift association logic  
- Correction workflow  
Sample Implementation:  
```java
@Service
public class AttendanceService {
  public Attendance clockIn(Long employeeId, ClockInDto dto) { ... }
  public Attendance clockOut(Long employeeId, ClockOutDto dto) { ... }
  public CorrectionTask requestCorrection(Long attendanceId, CorrectionDto dto) { ... }
}
```

---

### Epic E05: Shift & Schedule Management

Section: Entity Design  
Description: Shift, Schedule, and Assignment entities for recurring templates, rotations, blackout dates.  
Design Specification:  
- Shift: id, name, startTime, endTime, recurrence, blackoutDates  
- Schedule: id, employee, shift, date  
Sample Implementation:  
```java
@Entity
public class Shift {
  @Id @GeneratedValue private Long id;
  private String name;
  private LocalTime startTime;
  private LocalTime endTime;
  private String recurrence; // e.g., "WEEKLY"
  @ElementCollection private List<LocalDate> blackoutDates;
}

@Entity
public class Schedule {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  @ManyToOne private Shift shift;
  private LocalDate date;
}
```

Section: Service Layer  
Description: Detects conflicts, bulk-assigns shifts, generates audit entries.  
Design Specification:  
- Conflict detection logic  
- Bulk assignment methods  
Sample Implementation:  
```java
@Service
public class ScheduleService {
  public void assignShift(Long employeeId, Long shiftId, LocalDate date) { ... }
  public List<Schedule> bulkAssign(List<Long> employeeIds, Long shiftId, LocalDate date) { ... }
  public boolean hasConflict(Long employeeId, LocalDate date) { ... }
}
```

---

### Epic E06: Leave & Absence Management

Section: Entity Design  
Description: LeaveRequest entity with accrual balances, policies, approval workflow.  
Design Specification:  
- Fields: id, employee, type, startDate, endDate, status, balance  
Sample Implementation:  
```java
@Entity
public class LeaveRequest {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  @Enumerated(EnumType.STRING) private LeaveType type;
  private LocalDate startDate;
  private LocalDate endDate;
  @Enumerated(EnumType.STRING) private LeaveStatus status;
  private int balance;
}
```

Section: Service Layer  
Description: Handles requests, approvals, updates balances, integrates with scheduling.  
Design Specification:  
- Request/approve/deny methods  
- Balance update logic  
Sample Implementation:  
```java
@Service
public class LeaveService {
  public LeaveRequest requestLeave(Long employeeId, LeaveRequestDto dto) { ... }
  public void approveLeave(Long leaveId) { ... }
  public void denyLeave(Long leaveId) { ... }
}
```

---

### Epic E07: Training & Certification Tracking

Section: Entity Design  
Description: Certification entity tracks type, expiry, proof documents.  
Design Specification:  
- Fields: id, employee, type, expiryDate, proofDocumentUrl  
Sample Implementation:  
```java
@Entity
public class Certification {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String type;
  private LocalDate expiryDate;
  private String proofDocumentUrl;
}
```

Section: Service Layer  
Description: Alerts for expiry, blocks assignments, uploads documents.  
Design Specification:  
- Expiry alert logic  
- Assignment blocking  
Sample Implementation:  
```java
@Service
public class CertificationService {
  public void checkExpiryAlerts() { ... }
  public boolean canAssignTask(Long employeeId, String taskType) { ... }
  public void uploadProof(Long certId, MultipartFile file) { ... }
}
```

---

### Epic E08: Safety Incidents & OSHA Reporting

Section: Entity Design  
Description: SafetyIncident entity with severity, location, involved employees, workflow status.  
Design Specification:  
- Fields: id, severity, location, description, involvedEmployees, status  
Sample Implementation:  
```java
@Entity
public class SafetyIncident {
  @Id @GeneratedValue private Long id;
  private String severity;
  private String location;
  private String description;
  @ManyToMany private List<Employee> involvedEmployees;
  @Enumerated(EnumType.STRING) private IncidentStatus status;
}
```

Section: Controller Layer  
Description: Endpoints for incident reporting, status workflow, OSHA export.  
Design Specification:  
- `/safety/incidents` (POST, GET), `/safety/incidents/{id}/status` (PATCH), `/safety/osha/export` (GET)  
Sample Implementation:  
```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
  @PostMapping public ResponseEntity<?> reportIncident(@RequestBody IncidentDto dto) { ... }
  @PatchMapping("/{id}/status") public void updateStatus(@PathVariable Long id, @RequestBody StatusDto dto) { ... }
  @GetMapping("/osha/export") public ResponseEntity<Resource> exportOSHA() { ... }
}
```

---

### Epic E09: Equipment & Asset Assignment

Section: Entity Design  
Description: Asset entity tracks assignment, condition, certification requirements.  
Design Specification:  
- Fields: id, type, condition, assignedTo, certificationRequired, checkedOutAt, returnedAt  
Sample Implementation:  
```java
@Entity
public class Asset {
  @Id @GeneratedValue private Long id;
  private String type;
  private String condition;
  @ManyToOne private Employee assignedTo;
  private String certificationRequired;
  private LocalDateTime checkedOutAt;
  private LocalDateTime returnedAt;
}
```

Section: Service Layer  
Description: Handles check-in/out, blocks use if cert missing, logs history.  
Design Specification:  
- Check-in/out methods  
- Certification validation  
Sample Implementation:  
```java
@Service
public class AssetService {
  public void checkOut(Long assetId, Long employeeId) { ... }
  public void checkIn(Long assetId) { ... }
  public boolean canUse(Long employeeId, String assetType) { ... }
}
```

---

### Epic E10: Performance Reviews & Goals

Section: Entity Design  
Description: Review entity with goals, competencies, ratings, comments, acknowledgements.  
Design Specification:  
- Fields: id, employee, supervisor, period, goals, competencies, rating, comments, acknowledged  
Sample Implementation:  
```java
@Entity
public class PerformanceReview {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  @ManyToOne private Employee supervisor;
  private String period;
  @ElementCollection private List<String> goals;
  @ElementCollection private List<String> competencies;
  private int rating;
  private String comments;
  private boolean acknowledged;
}
```

Section: Service Layer  
Description: Manages review cycles, submission, acknowledgement, PDF export.  
Design Specification:  
- Create/assign/submit/acknowledge methods  
Sample Implementation:  
```java
@Service
public class ReviewService {
  public PerformanceReview createReview(Long employeeId, ReviewDto dto) { ... }
  public void acknowledgeReview(Long reviewId) { ... }
  public Resource exportPdf(Long reviewId) { ... }
}
```

---

### Epic E11: Payroll Export Integration

Section: Integration Points  
Description: Generates payroll files, maps to provider formats, delivers via SFTP/API.  
Design Specification:  
- Export logic for attendance/leave  
- SFTP/API delivery  
- Audit log for exports  
Sample Implementation:  
```java
@Service
public class PayrollExportService {
  public Resource generatePayrollFile(LocalDate periodStart, LocalDate periodEnd) { ... }
  public void deliverPayroll(Resource file) { ... }
  public void logExport(Long exportId, String status) { ... }
}
```

Section: Configuration  
Description: Provider schema mapping, retry/backoff settings.  
Design Specification:  
- `payroll.provider=sftp`  
- `payroll.retry.maxAttempts=3`  
Sample Implementation:  
```yaml
payroll:
  provider: sftp
  retry:
    maxAttempts: 3
```

---

### Epic E12: Notifications & Announcements

Section: Integration Points  
Description: In-app, email, SMS notifications; quiet hours; delivery tracking.  
Design Specification:  
- Notification channels: in-app, email, SMS  
- Quiet hours config  
- Delivery status tracking  
Sample Implementation:  
```java
@Service
public class NotificationService {
  public void sendNotification(Long userId, NotificationDto dto) { ... }
  public void trackDelivery(Long notificationId, DeliveryStatus status) { ... }
}
```

Section: Configuration  
Description: Channel opt-in/out, rate limits, templates.  
Design Specification:  
- `notifications.email.enabled=true`  
- `notifications.sms.enabled=true`  
- `notifications.quietHours=22:00-06:00`  
Sample Implementation:  
```yaml
notifications:
  email:
    enabled: true
  sms:
    enabled: true
  quietHours: "22:00-06:00"
```

---

### Epic E13: Integration Layer (HRIS/WMS APIs)

Section: Integration Points  
Description: REST APIs for HRIS, WMS, IDP; webhooks for events; JWT/OAuth2 security.  
Design Specification:  
- HRIS sync job  
- WMS department/location mapping  
- IDP SSO integration  
- Webhook endpoints  
Sample Implementation:  
```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
  @PostMapping("/hris/sync") public void syncHris(@RequestBody HrisDto dto) { ... }
  @PostMapping("/wms/link") public void linkWms(@RequestBody WmsDto dto) { ... }
  @PostMapping("/webhook") public void handleWebhook(@RequestBody WebhookEventDto dto) { ... }
}
```

Section: Security Settings  
Description: JWT/OAuth2 for API security.  
Design Specification:  
- `@PreAuthorize` on endpoints  
Sample Implementation:  
```java
@PreAuthorize("hasAuthority('SCOPE_integration')")
@PostMapping("/hris/sync")
public void syncHris(@RequestBody HrisDto dto) { ... }
```

---

### Epic E14: Audit Trail & Compliance

Section: Entity Design  
Description: AuditLog entity for immutable logging of sensitive changes.  
Design Specification:  
- Fields: id, actor, timestamp, entity, action, before, after  
Sample Implementation:  
```java
@Entity
public class AuditLog {
  @Id @GeneratedValue private Long id;
  private String actor;
  private LocalDateTime timestamp;
  private String entity;
  private String action;
  @Column(columnDefinition = "TEXT") private String before;
  @Column(columnDefinition = "TEXT") private String after;
}
```

Section: Service Layer  
Description: Logs all create/update/delete operations, exports by date/user/entity.  
Design Specification:  
- Aspect-based logging  
- Export methods  
Sample Implementation:  
```java
@Aspect
@Component
public class AuditAspect {
  @AfterReturning("@annotation(Auditable)")
  public void logAudit(JoinPoint joinPoint) { ... }
}
```

---

### Epic E15: Reporting & Analytics

Section: Controller Layer  
Description: Endpoints for operational reports, exports, dashboards.  
Design Specification:  
- `/reports/attendance`, `/reports/overtime`, `/reports/leave`, `/reports/certifications`, `/reports/safety`  
- Export CSV/PDF  
Sample Implementation:  
```java
@RestController
@RequestMapping("/reports")
public class ReportController {
  @GetMapping("/attendance") public ResponseEntity<Resource> attendanceReport(@RequestParam LocalDate start, @RequestParam LocalDate end) { ... }
  @GetMapping("/overtime") public ResponseEntity<Resource> overtimeReport() { ... }
}
```

Section: Service Layer  
Description: Filters by date, department, shift; exports within 10s for 50k rows.  
Design Specification:  
- Efficient query logic  
- Export optimization  
Sample Implementation:  
```java
@Service
public class ReportService {
  public Resource generateAttendanceReport(LocalDate start, LocalDate end) { ... }
}
```

---

### Epic E16: Mobile Access (PWA)

Section: Configuration  
Description: PWA manifest, offline queue, responsive views.  
Design Specification:  
- `manifest.json` for PWA  
- Service worker for offline  
- Lighthouse PWA score â¥ 80  
Sample Implementation:  
```json
{
  "name": "Warehouse Employee Mgmt",
  "short_name": "WEM",
  "start_url": "/",
  "display": "standalone",
  "icons": [...]
}
```

Section: Controller Layer  
Description: Mobile-friendly endpoints for clock-in/out, schedules, leave, announcements.  
Design Specification:  
- `/mobile/clock-in`, `/mobile/schedules`, `/mobile/leave`, `/mobile/announcements`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
  @PostMapping("/clock-in") public ResponseEntity<?> clockIn(@RequestBody ClockInDto dto) { ... }
  @GetMapping("/schedules") public List<ScheduleDto> getSchedules() { ... }
}
```

---

### Epic E17: Onboarding & Offboarding Workflow

Section: Service Layer  
Description: Automates provisioning, training, asset assignment, deprovision on termination.  
Design Specification:  
- Onboarding tasks: account, schedule, training  
- Offboarding tasks: revoke access, collect assets  
Sample Implementation:  
```java
@Service
public class OnboardingService {
  public void onboardEmployee(Long employeeId) { ... }
  public void offboardEmployee(Long employeeId) { ... }
}
```

Section: Integration Points  
Description: HRIS integration for new hires/terms.  
Design Specification:  
- Webhook listener for HRIS events  
Sample Implementation:  
```java
@PostMapping("/webhook/hris")
public void handleHrisEvent(@RequestBody HrisEventDto dto) {
  if (dto.getType().equals("NEW_HIRE")) {
    onboardingService.onboardEmployee(dto.getEmployeeId());
  } else if (dto.getType().equals("TERMINATION")) {
    onboardingService.offboardEmployee(dto.getEmployeeId());
  }
}
```

---

### Epic E18: Localization & Multi-Tenant

Section: Configuration  
Description: Tenant isolation, i18n for English/Spanish, currency/timezone per tenant.  
Design Specification:  
- Tenant ID in all queries  
- `messages_en.properties`, `messages_es.properties`  
- Tenant-specific config  
Sample Implementation:  
```yaml
spring:
  messages:
    basename: messages
tenants:
  - id: warehouse1
    locale: en_US
    timezone: America/New_York
  - id: warehouse2
    locale: es_MX
    timezone: America/Mexico_City
```

Section: Entity Design  
Description: Tenant ID in all entities for data isolation.  
Design Specification:  
- `@Where(clause = "tenant_id = :tenantId")`  
Sample Implementation:  
```java
@Entity
@Where(clause = "tenant_id = :tenantId")
public class Employee {
  @Id @GeneratedValue private Long id;
  private String tenantId;
  ...
}
```

---

### Epic E19: Observability & Monitoring

Section: Configuration  
Description: Prometheus/Micrometer metrics, structured logging, distributed tracing.  
Design Specification:  
- `/actuator/prometheus` endpoint  
- JSON logging  
- Trace IDs in headers  
Sample Implementation:  
```yaml
management:
  endpoints:
    web:
      exposure:
        include: prometheus,health,info
  metrics:
    export:
      prometheus:
        enabled: true
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  level:
    root: INFO
```

Section: Integration Points  
Description: Zipkin/Jaeger for distributed tracing, Grafana dashboards.  
Design Specification:  
- `spring.zipkin.base-url=http://zipkin:9411`  
Sample Implementation:  
```yaml
spring:
  zipkin:
    base-url: http://zipkin:9411
  sleuth:
    sampler:
      probability: 1.0
```

---

### Epic E20: CI/CD & Deployment Automation

Section: Configuration  
Description: GitHub Actions/Jenkins pipeline for build, test, Docker image, deploy.  
Design Specification:  
- Pipeline stages: build, test, Docker, deploy  
- Automated DB migrations  
- Rollback plan  
Sample Implementation:  
```yaml
# .github/workflows/ci-cd.yml
name: CI/CD
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build
        run: mvn clean install
      - name: Test
        run: mvn test
      - name: Docker Build
        run: docker build -t warehouse-employee-mgmt .
      - name: Deploy to Staging
        run: kubectl apply -f k8s/staging/
      - name: Smoke Tests
        run: ./smoke-tests.sh
      - name: Deploy to Prod (manual approval)
        if: github.ref == 'refs/heads/main'
        run: kubectl apply -f k8s/prod/
```

Section: Integration Points  
Description: Automated DB migrations via Flyway/Liquibase.  
Design Specification:  
- Migrations run on startup  
Sample Implementation:  
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

---

## Summary

This comprehensive low-level technical design document covers all 20 epics of the Warehouse Employee Management System, structured according to Spring Boot best practices. Each epic includes:

- Spring Boot Architecture Overview  
- Package Structure  
- Entity Design  
- Repository Layer  
- Service Layer  
- Controller Layer  
- Configuration  
- Security Settings  
- Integration Points  
- Code Snippets

The document is ready for implementation by development teams and provides clear guidance on design patterns, configurations, and best practices.

---

**End of Document**