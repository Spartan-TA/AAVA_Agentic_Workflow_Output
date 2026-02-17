# Warehouse Employee Management System - Low-Level Technical Design Document

---

## Table of Contents

- [E01 Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
- [E02 Employee Master Data (CRUD)](#e02-employee-master-data-crud)
- [E03 Role-Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
- [E04 Time & Attendance (Clock In/Out)](#e04-time--attendance-clock-inout)
- [E05 Shift & Schedule Management](#e05-shift--schedule-management)
- [E06 Leave & Absence Management](#e06-leave--absence-management)
- [E07 Training & Certification Tracking](#e07-training--certification-tracking)
- [E08 Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
- [E09 Equipment & Asset Assignment](#e09-equipment--asset-assignment)
- [E10 Performance Reviews & Goals](#e10-performance-reviews--goals)
- [E11 Payroll Export Integration](#e11-payroll-export-integration)
- [E12 Notifications & Announcements](#e12-notifications--announcements)
- [E13 Integration Layer (HRIS/WMS APIs)](#e13-integration-layer-hriswms-apis)
- [E14 Audit Trail & Compliance](#e14-audit-trail--compliance)
- [E15 Reporting & Analytics](#e15-reporting--analytics)
- [E16 Mobile Access (PWA)](#e16-mobile-access-pwa)
- [E17 Onboarding & Offboarding Workflow](#e17-onboarding--offboarding-workflow)
- [E18 Localization](#e18-localization)
- [E19 Observability](#e19-observability)
- [E20 Deployment](#e20-deployment)

---

## E01 Project Scaffolding & Domain Setup

### 1. Overview of Spring Boot Architecture

- **Spring Boot (Maven) project** with modular package structure.
- Core modules: employee, scheduling, attendance, safety.
- **Flyway/Liquibase** for DB migrations.
- **Spring Boot Actuator** enabled for monitoring.

### 2. Package Structure & Component Breakdown

```
com.warehouse
âââ employee
âââ scheduling
âââ attendance
âââ safety
âââ config
âââ common
```

### 3. Entity Design

- No domain entities yet; base structure only.

### 4. Service Layer Specifications

- No business logic; placeholder services.

### 5. Repository Layer Specifications

- No repositories; placeholder interfaces.

### 6. Controller Specifications

- Health check endpoint via Actuator.

### 7. Configuration & Security Settings

- `application.yml` with port, DB, actuator, Flyway/Liquibase.

### 8. Integration Points

- None yet.

### 9. Code Snippets

**pom.xml (Spring Boot, Actuator, Flyway):**
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-core</artifactId>
</dependency>
```

**application.yml:**
```yaml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost/warehouse
  flyway:
    enabled: true
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

---

## E02 Employee Master Data (CRUD)

### 1. Overview

- CRUD APIs for Employee domain.
- DTOs for web/API transfer.

### 2. Package Structure

```
com.warehouse.employee
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### 3. Entity Design

**Employee Entity:**
```java
@Entity
public class Employee {
  @Id @GeneratedValue
  private Long id;
  @Column(unique = true)
  private String badgeId;
  private String name;
  private String role;
  private String department;
  private String shiftGroup;
  private LocalDate hireDate;
  private String status;
  private boolean deleted;
}
```

### 4. Service Layer

- Business logic for CRUD, soft-delete, filtering, pagination.

### 5. Repository Layer

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  Optional<Employee> findByBadgeId(String badgeId);
  Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

### 6. Controller

```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
  @PostMapping public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { ... }
  @GetMapping public Page<EmployeeDto> list(Pageable pageable, @RequestParam Map<String,String> filters) { ... }
  @GetMapping("/{id}") public EmployeeDto get(@PathVariable Long id) { ... }
  @PutMapping("/{id}") public EmployeeDto update(@PathVariable Long id, @RequestBody EmployeeDto dto) { ... }
  @PatchMapping("/{id}") public EmployeeDto patch(@PathVariable Long id, @RequestBody Map<String,Object> patch) { ... }
  @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { ... }
}
```

### 7. Configuration & Security

- OpenAPI enabled.
- Validation with Bean Validation API.

### 8. Integration Points

- None yet.

### 9. Code Snippets

**DTO Pattern:**
```java
public class EmployeeDto {
  @NotBlank private String name;
  @NotBlank private String badgeId;
  // ...
}
```

**Soft Delete:**
```java
@Transactional
public void delete(Long id) {
  Employee emp = repo.findById(id).orElseThrow();
  emp.setDeleted(true);
  repo.save(emp);
}
```

---

## E03 Role-Based Access Control (RBAC)

### 1. Overview

- Spring Security with roles: ADMIN, HR, SUPERVISOR, WORKER.
- Method/endpoint security.

### 2. Package Structure

```
com.warehouse.security
âââ config
âââ service
```

### 3. Entity Design

- User/Role entities if needed.

### 4. Service Layer

- UserDetailsService for authentication.

### 5. Repository Layer

- UserRepository, RoleRepository.

### 6. Controller

- Security endpoints (login, etc).

### 7. Configuration & Security

**SecurityConfig:**
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity http) {
    http
      .authorizeRequests()
        .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
        .anyRequest().authenticated()
      .and()
        .oauth2Login()
      .and()
        .apiKeyAuthFilter();
  }
}
```

### 8. Integration Points

- OAuth2, API Key toggle.

### 9. Code Snippets

**Method Security:**
```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteEmployee(Long id) { ... }
```

---

## E04 Time & Attendance (Clock In/Out)

### 1. Overview

- Clock-in/out endpoints, geofence/device capture, hours calculation.

### 2. Package Structure

```
com.warehouse.attendance
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### 3. Entity Design

**AttendanceEvent:**
```java
@Entity
public class AttendanceEvent {
  @Id @GeneratedValue
  private Long id;
  @ManyToOne
  private Employee employee;
  private LocalDateTime timestamp;
  private String eventType; // CLOCK_IN, CLOCK_OUT
  private String deviceId;
  private String location;
  private boolean correctionRequested;
}
```

### 4. Service Layer

- Calculate hours, handle corrections, approval workflow.

### 5. Repository Layer

```java
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {
  List<AttendanceEvent> findByEmployeeAndDate(Employee emp, LocalDate date);
}
```

### 6. Controller

```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
  @PostMapping("/clock-in") public ResponseEntity<?> clockIn(@RequestBody AttendanceDto dto) { ... }
  @PostMapping("/clock-out") public ResponseEntity<?> clockOut(@RequestBody AttendanceDto dto) { ... }
  @PostMapping("/correction") public ResponseEntity<?> requestCorrection(@RequestBody CorrectionDto dto) { ... }
}
```

### 7. Configuration & Security

- Validation, role checks.

### 8. Integration Points

- Export to payroll.

### 9. Code Snippets

**Hours Calculation:**
```java
public Duration calculateWorkedHours(Employee emp, LocalDate date) {
  List<AttendanceEvent> events = repo.findByEmployeeAndDate(emp, date);
  // Pair up clock-in/out, sum durations
}
```

---

## E05 Shift & Schedule Management

### 1. Overview

- Shift templates, rotations, overtime, blackout dates.

### 2. Package Structure

```
com.warehouse.scheduling
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### 3. Entity Design

**ShiftTemplate:**
```java
@Entity
public class ShiftTemplate {
  @Id @GeneratedValue
  private Long id;
  private String name;
  private LocalTime start;
  private LocalTime end;
  private boolean recurring;
}
```

**EmployeeShiftAssignment:**
```java
@Entity
public class EmployeeShiftAssignment {
  @Id @GeneratedValue
  private Long id;
  @ManyToOne
  private Employee employee;
  @ManyToOne
  private ShiftTemplate shift;
  private LocalDate date;
}
```

### 4. Service Layer

- Conflict detection, bulk assignment, audit.

### 5. Repository Layer

```java
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {}
public interface EmployeeShiftAssignmentRepository extends JpaRepository<EmployeeShiftAssignment, Long> {
  List<EmployeeShiftAssignment> findByEmployeeAndDate(Employee emp, LocalDate date);
}
```

### 6. Controller

```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
  @PostMapping("/templates") public ShiftTemplateDto createTemplate(@RequestBody ShiftTemplateDto dto) { ... }
  @PostMapping("/assign") public void assignShift(@RequestBody AssignmentDto dto) { ... }
  @GetMapping("/my") public List<ShiftAssignmentDto> myShifts(@AuthenticationPrincipal Employee emp) { ... }
}
```

### 7. Configuration & Security

- Role checks, audit logging.

### 8. Integration Points

- Attendance, leave.

### 9. Code Snippets

**Conflict Detection:**
```java
public boolean hasConflict(Employee emp, LocalDate date, LocalTime start, LocalTime end) {
  List<EmployeeShiftAssignment> assignments = repo.findByEmployeeAndDate(emp, date);
  // Check for overlap
}
```

---

## E06 Leave & Absence Management

### 1. Overview

- PTO, sick, unpaid leave; accruals; integration with scheduling/payroll.

### 2. Package Structure

```
com.warehouse.leave
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### 3. Entity Design

**LeaveRequest:**
```java
@Entity
public class LeaveRequest {
  @Id @GeneratedValue
  private Long id;
  @ManyToOne
  private Employee employee;
  private LocalDate start;
  private LocalDate end;
  private String type; // PTO, SICK, UNPAID
  private String status; // REQUESTED, APPROVED, DENIED
  private int accrualBalance;
}
```

### 4. Service Layer

- Request, approve/deny, update balances.

### 5. Repository Layer

```java
public interface LeaveRepository extends JpaRepository<LeaveRequest, Long> {
  List<LeaveRequest> findByEmployee(Employee emp);
}
```

### 6. Controller

```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
  @PostMapping("/request") public LeaveRequestDto requestLeave(@RequestBody LeaveRequestDto dto) { ... }
  @PostMapping("/approve") public void approveLeave(@RequestBody ApprovalDto dto) { ... }
  @GetMapping("/my") public List<LeaveRequestDto> myLeaves(@AuthenticationPrincipal Employee emp) { ... }
}
```

### 7. Configuration & Security

- Role checks.

### 8. Integration Points

- Scheduling, payroll.

### 9. Code Snippets

**Accrual Update:**
```java
@Transactional
public void approveLeave(Long leaveId) {
  LeaveRequest leave = repo.findById(leaveId).orElseThrow();
  leave.setStatus("APPROVED");
  leave.setAccrualBalance(leave.getAccrualBalance() - daysBetween(leave.getStart(), leave.getEnd()));
  repo.save(leave);
}
```

---

## E07 Training & Certification Tracking

### 1. Overview

- Track certifications, expirations, renewals, proof docs.

### 2. Package Structure

```
com.warehouse.certification
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### 3. Entity Design

**Certification:**
```java
@Entity
public class Certification {
  @Id @GeneratedValue
  private Long id;
  @ManyToOne
  private Employee employee;
  private String type;
  private LocalDate expiryDate;
  private String proofDocumentUrl;
}
```

### 4. Service Layer

- CRUD, alerts, scheduling checks.

### 5. Repository Layer

```java
public interface CertificationRepository extends JpaRepository<Certification, Long> {
  List<Certification> findByExpiryDateBefore(LocalDate date);
}
```

### 6. Controller

```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
  @PostMapping public CertificationDto create(@RequestBody CertificationDto dto) { ... }
  @GetMapping("/expiring") public List<CertificationDto> expiring(@RequestParam int days) { ... }
}
```

### 7. Configuration & Security

- Role checks.

### 8. Integration Points

- Scheduling, asset assignment.

### 9. Code Snippets

**Expiry Alert:**
```java
public List<Certification> getExpiringCerts(int days) {
  LocalDate threshold = LocalDate.now().plusDays(days);
  return repo.findByExpiryDateBefore(threshold);
}
```

---

## E08 Safety Incidents & OSHA Reporting

### 1. Overview

- Record incidents, workflow, OSHA summary.

### 2. Package Structure

```
com.warehouse.safety
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### 3. Entity Design

**SafetyIncident:**
```java
@Entity
public class SafetyIncident {
  @Id @GeneratedValue
  private Long id;
  private String severity;
  private String location;
  private String description;
  @ManyToMany
  private List<Employee> involvedEmployees;
  private String status; // OPEN, INVESTIGATING, RESOLVED
}
```

### 4. Service Layer

- Workflow, corrective actions.

### 5. Repository Layer

```java
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {}
```

### 6. Controller

```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
  @PostMapping public SafetyIncidentDto create(@RequestBody SafetyIncidentDto dto) { ... }
  @GetMapping public List<SafetyIncidentDto> list() { ... }
  @PostMapping("/{id}/resolve") public void resolve(@PathVariable Long id) { ... }
}
```

### 7. Configuration & Security

- Role checks.

### 8. Integration Points

- Reporting.

### 9. Code Snippets

**Status Workflow:**
```java
@Transactional
public void resolveIncident(Long id) {
  SafetyIncident incident = repo.findById(id).orElseThrow();
  incident.setStatus("RESOLVED");
  repo.save(incident);
}
```

---

## E09 Equipment & Asset Assignment

### 1. Overview

- Assign assets, track checkout/return, block if cert missing.

### 2. Package Structure

```
com.warehouse.asset
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### 3. Entity Design

**Asset:**
```java
@Entity
public class Asset {
  @Id @GeneratedValue
  private Long id;
  private String type;
  private String condition;
  private boolean checkedOut;
  @ManyToOne
  private Employee assignedTo;
}
```

### 4. Service Layer

- Assignment, check-in/out, history.

### 5. Repository Layer

```java
public interface AssetRepository extends JpaRepository<Asset, Long> {}
```

### 6. Controller

```java
@RestController
@RequestMapping("/assets")
public class AssetController {
  @PostMapping("/assign") public void assign(@RequestBody AssetAssignmentDto dto) { ... }
  @PostMapping("/return") public void returnAsset(@RequestBody AssetReturnDto dto) { ... }
}
```

### 7. Configuration & Security

- Role checks.

### 8. Integration Points

- Certification.

### 9. Code Snippets

**Assignment Block:**
```java
public void assignAsset(Long assetId, Long employeeId) {
  Employee emp = employeeRepo.findById(employeeId).orElseThrow();
  if (!certService.hasValidCert(emp, assetType)) throw new ForbiddenException();
  Asset asset = assetRepo.findById(assetId).orElseThrow();
  asset.setAssignedTo(emp);
  asset.setCheckedOut(true);
  assetRepo.save(asset);
}
```

---

## E10 Performance Reviews & Goals

### 1. Overview

- Review templates, goals, ratings, comments, workflow.

### 2. Package Structure

```
com.warehouse.performance
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### 3. Entity Design

**PerformanceReview:**
```java
@Entity
public class PerformanceReview {
  @Id @GeneratedValue
  private Long id;
  @ManyToOne
  private Employee employee;
  private String cycle; // Quarterly, Annual
  private String goals;
  private String competencies;
  private String ratings;
  private String comments;
  private boolean acknowledgedBySupervisor;
  private boolean acknowledgedByEmployee;
  private boolean signedOff;
}
```

### 4. Service Layer

- Review cycle, submit/acknowledge, PDF export.

### 5. Repository Layer

```java
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {}
```

### 6. Controller

```java
@RestController
@RequestMapping("/reviews")
public class PerformanceReviewController {
  @PostMapping public PerformanceReviewDto create(@RequestBody PerformanceReviewDto dto) { ... }
  @PostMapping("/{id}/acknowledge") public void acknowledge(@PathVariable Long id, @RequestParam String role) { ... }
}
```

### 7. Configuration & Security

- Role-based visibility.

### 8. Integration Points

- None.

### 9. Code Snippets

**Sign-off:**
```java
public void signOff(Long reviewId) {
  PerformanceReview review = repo.findById(reviewId).orElseThrow();
  review.setSignedOff(true);
  repo.save(review);
}
```

---

## E11 Payroll Export Integration

### 1. Overview

- Generate payroll files, map to provider formats, secure delivery.

### 2. Package Structure

```
com.warehouse.payroll
âââ service
âââ controller
âââ model
âââ dto
```

### 3. Entity Design

- PayrollExport entity for audit.

### 4. Service Layer

- Export generation, delivery, retry.

### 5. Repository Layer

- PayrollExportRepository.

### 6. Controller

```java
@RestController
@RequestMapping("/payroll")
public class PayrollController {
  @PostMapping("/export") public ResponseEntity<?> exportPayroll(@RequestBody PayrollExportRequestDto dto) { ... }
}
```

### 7. Configuration & Security

- Secure delivery (SFTP/API).

### 8. Integration Points

- Attendance, leave.

### 9. Code Snippets

**Retry Logic:**
```java
@Scheduled(fixedDelay = 60000)
public void retryFailedExports() {
  List<PayrollExport> failed = repo.findByStatus("FAILED");
  for (PayrollExport export : failed) {
    // retry logic
  }
}
```

---

## E12 Notifications & Announcements

### 1. Overview

- In-app/email/SMS notifications, templates, quiet hours.

### 2. Package Structure

```
com.warehouse.notification
âââ controller
âââ service
âââ model
âââ dto
```

### 3. Entity Design

**Notification:**
```java
@Entity
public class Notification {
  @Id @GeneratedValue
  private Long id;
  private String channel; // EMAIL, SMS, IN_APP
  private String template;
  private String status; // SENT, FAILED
  private LocalDateTime sentAt;
  @ManyToOne
  private Employee recipient;
}
```

### 4. Service Layer

- Delivery, opt-in/out, rate limiting.

### 5. Repository Layer

```java
public interface NotificationRepository extends JpaRepository<Notification, Long> {}
```

### 6. Controller

```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
  @PostMapping("/send") public void send(@RequestBody NotificationDto dto) { ... }
  @GetMapping("/my") public List<NotificationDto> myNotifications(@AuthenticationPrincipal Employee emp) { ... }
}
```

### 7. Configuration & Security

- Quiet hours config.

### 8. Integration Points

- Email/SMS providers.

### 9. Code Snippets

**Rate Limiting:**
```java
public void sendNotification(NotificationDto dto) {
  if (rateLimiter.isLimited(dto.getRecipient())) throw new TooManyRequestsException();
  // send logic
}
```

---

## E13 Integration Layer (HRIS/WMS APIs)

### 1. Overview

- REST APIs/connectors for HRIS, WMS, IDP; webhooks.

### 2. Package Structure

```
com.warehouse.integration
âââ controller
âââ service
âââ model
âââ dto
```

### 3. Entity Design

- IntegrationJob, WebhookEvent.

### 4. Service Layer

- Sync jobs, idempotency.

### 5. Repository Layer

- IntegrationJobRepository.

### 6. Controller

```java
@RestController
@RequestMapping("/integration")
public class IntegrationController {
  @PostMapping("/hris/sync") public void syncHRIS(@RequestBody HRISSyncDto dto) { ... }
  @PostMapping("/wms/link") public void linkWMS(@RequestBody WMSLinkDto dto) { ... }
  @PostMapping("/webhook") public void webhook(@RequestBody WebhookEventDto dto) { ... }
}
```

### 7. Configuration & Security

- JWT/OAuth2-secured APIs.

### 8. Integration Points

- HRIS, WMS, IDP.

### 9. Code Snippets

**Idempotent Webhook:**
```java
public void handleWebhook(WebhookEventDto dto) {
  if (repo.existsByEventId(dto.getEventId())) return;
  // process event
}
```

---

## E14 Audit Trail & Compliance

### 1. Overview

- Centralized audit logging, tamper-evident storage.

### 2. Package Structure

```
com.warehouse.audit
âââ service
âââ repository
âââ model
```

### 3. Entity Design

**AuditLog:**
```java
@Entity
public class AuditLog {
  @Id @GeneratedValue
  private Long id;
  private String entity;
  private String action;
  private String actor;
  private LocalDateTime timestamp;
  private String before;
  private String after;
}
```

### 4. Service Layer

- Log create/update/delete.

### 5. Repository Layer

```java
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}
```

### 6. Controller

- Export logs.

### 7. Configuration & Security

- Immutable log table.

### 8. Integration Points

- All modules.

### 9. Code Snippets

**Audit Logging:**
```java
public void logChange(String entity, String action, String actor, String before, String after) {
  AuditLog log = new AuditLog(entity, action, actor, LocalDateTime.now(), before, after);
  repo.save(log);
}
```

---

## E15 Reporting & Analytics

### 1. Overview

- Attendance, overtime, leave, cert status, safety KPIs; CSV/PDF export.

### 2. Package Structure

```
com.warehouse.reporting
âââ controller
âââ service
âââ model
âââ dto
```

### 3. Entity Design

- Report entities.

### 4. Service Layer

- Filtering, export, metrics.

### 5. Repository Layer

- Custom queries.

### 6. Controller

```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
  @GetMapping("/attendance") public List<AttendanceReportDto> attendance(@RequestParam Map<String,String> filters) { ... }
  @GetMapping("/export") public ResponseEntity<Resource> export(@RequestParam String type) { ... }
}
```

### 7. Configuration & Security

- Access control.

### 8. Integration Points

- BI tools.

### 9. Code Snippets

**CSV Export:**
```java
public Resource exportCsv(List<?> data) {
  // Write CSV to ByteArrayOutputStream, return as Resource
}
```

---

## E16 Mobile Access (PWA)

### 1. Overview

- Responsive views, offline-friendly, installable PWA.

### 2. Package Structure

```
com.warehouse.mobile
âââ controller
âââ service
```

### 3. Entity Design

- None; uses existing entities.

### 4. Service Layer

- Offline queue, conflict resolution.

### 5. Repository Layer

- None.

### 6. Controller

- Mobile endpoints.

### 7. Configuration & Security

- PWA manifest.

### 8. Integration Points

- Attendance, scheduling, leave.

### 9. Code Snippets

**PWA Manifest:**
```json
{
  "name": "Warehouse Employee Management",
  "short_name": "Warehouse",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

---

## E17 Onboarding & Offboarding Workflow

### 1. Overview

- Automate provisioning, training, asset assignment, deprovisioning.

### 2. Package Structure

```
com.warehouse.onboarding
âââ controller
âââ service
âââ model
âââ dto
```

### 3. Entity Design

**OnboardingTask:**
```java
@Entity
public class OnboardingTask {
  @Id @GeneratedValue
  private Long id;
  @ManyToOne
  private Employee employee;
  private String type; // TRAINING, ASSET_ASSIGNMENT, SCHEDULE
  private String status; // PENDING, COMPLETED
}
```

### 4. Service Layer

- Task generation, completion.

### 5. Repository Layer

```java
public interface OnboardingTaskRepository extends JpaRepository<OnboardingTask, Long> {}
```

### 6. Controller

```java
@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
  @PostMapping("/new-hire") public void newHire(@RequestBody EmployeeDto dto) { ... }
  @PostMapping("/offboard") public void offboard(@RequestBody EmployeeDto dto) { ... }
}
```

### 7. Configuration & Security

- Role checks.

### 8. Integration Points

- HRIS, asset, training.

### 9. Code Snippets

**Task Generation:**
```java
public void generateTasks(Employee emp) {
  // Create tasks for training, asset assignment, schedule
}
```

---

## E18 Localization

### 1. Overview

- Localized templates, messages, UI.

### 2. Package Structure

```
com.warehouse.localization
âââ service
```

### 3. Entity Design

- None.

### 4. Service Layer

- MessageSource for i18n.

### 5. Repository Layer

- None.

### 6. Controller

- None.

### 7. Configuration & Security

**application.yml:**
```yaml
spring:
  messages:
    basename: messages
```

### 8. Integration Points

- Notification, UI.

### 9. Code Snippets

**MessageSource Bean:**
```java
@Bean
public MessageSource messageSource() {
  ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
  source.setBasename("classpath:messages");
  source.setDefaultEncoding("UTF-8");
  return source;
}
```

---

## E19 Observability

### 1. Overview

- Monitoring, logging, metrics.

### 2. Package Structure

```
com.warehouse.observability
âââ config
```

### 3. Entity Design

- None.

### 4. Service Layer

- None.

### 5. Repository Layer

- None.

### 6. Controller

- None.

### 7. Configuration & Security

- Actuator endpoints, SLF4J logging.

### 8. Integration Points

- Monitoring tools.

### 9. Code Snippets

**Actuator Config:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, metrics, loggers
```

**SLF4J Logging:**
```java
private static final Logger logger = LoggerFactory.getLogger(MyService.class);
logger.info("Employee created: {}", employee.getId());
```

---

## E20 Deployment

### 1. Overview

- Build/run steps, containerization, CI/CD.

### 2. Package Structure

- None.

### 3. Entity Design

- None.

### 4. Service Layer

- None.

### 5. Repository Layer

- None.

### 6. Controller

- None.

### 7. Configuration & Security

- Dockerfile, CI/CD pipeline.

### 8. Integration Points

- None.

### 9. Code Snippets

**Dockerfile:**
```dockerfile
FROM openjdk:17-jdk
COPY target/warehouse-employee-mgmt.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**CI/CD (GitHub Actions):**
```yaml
name: Build & Deploy
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build
        run: mvn clean package
      - name: Docker Build
        run: docker build -t warehouse-mgmt .
```

---

# Notes

- All entities use Spring Data JPA.
- All endpoints are RESTful, DTOs used for transfer.
- Security via Spring Security, RBAC, OAuth2/API Key.
- Exception handling via `@ControllerAdvice`.
- Validation via Bean Validation API.
- Configuration via `@ConfigurationProperties`.
- Logging via SLF4J.
- Transaction management via `@Transactional`.
- Monitoring via Actuator.
- Integration points as described per epic.
- Code snippets illustrate key design patterns.

---

This document provides a complete technical specification for all user stories in the Warehouse Employee Management System, ready for implementation by Spring Boot developers.