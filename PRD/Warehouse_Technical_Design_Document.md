# Warehouse Employee Management System - Low-Level Technical Design Document

## Overview
This document provides comprehensive low-level technical design specifications for all 20 user stories of the Warehouse Employee Management System, following Spring Boot best practices and industry standards.

---

## Section: Project Scaffolding & Domain Setup

**Description:** Establishes the foundational Spring Boot project structure, standardized packages, and core modules for maintainability and scalability. Integrates Flyway/Liquibase for DB migrations and enables Actuator for health monitoring.

**Design Specification:**
- Base package: com.wms.employee
- Sub-packages: controller, service, repository, model, config, security, dto, exception, util
- Maven/Gradle build with Spring Boot starter dependencies (web, data-jpa, security, actuator, validation, flyway/liquibase)
- application.yml: server.port=8080, actuator endpoints enabled
- Flyway/Liquibase baseline migration scripts

**Sample Implementation:**
```yaml
server:
  port: 8080
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
        include: health,info,metrics
```

---

## Section: Employee Master Data (CRUD)

**Description:** Implements CRUD APIs for employee records with unique badgeId, soft-delete, pagination, filtering, and OpenAPI documentation.

**Design Specification:**
- Entity: Employee (id, badgeId, name, role, department, shiftGroup, hireDate, status, deleted)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService with CRUD, soft-delete, filtering, pagination
- Controller: EmployeeController (REST endpoints)
- DTOs: EmployeeRequest, EmployeeResponse
- OpenAPI annotations for schema

**Sample Implementation:**
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
  @Id @GeneratedValue private Long id;
  @Column(name = "badge_id", nullable = false, unique = true) private String badgeId;
  private String name;
  private String role;
  private String department;
  private String shiftGroup;
  private LocalDate hireDate;
  private String status;
  private boolean deleted = false;
}

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
  Page<Employee> findAllByDeletedFalse(Pageable pageable);
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
  @GetMapping public Page<EmployeeResponse> list(Pageable pageable, @RequestParam Map<String,String> filters) {...}
  @PostMapping public EmployeeResponse create(@Valid @RequestBody EmployeeRequest req) {...}
  @PutMapping("/{id}") public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest req) {...}
  @DeleteMapping("/{id}") public void softDelete(@PathVariable Long id) {...}
}
```

---

## Section: Role-Based Access Control (RBAC)

**Description:** Secures endpoints and methods using Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER). Supports API key/OAuth2 toggle via config.

**Design Specification:**
- SecurityConfig: configures HttpSecurity, method security, JWT/OAuth2, API key toggle
- Role enum: ADMIN, HR, SUPERVISOR, WORKER
- UserDetailsService: loads users and roles
- Exception handling: 401 for unauthorized, 403 for forbidden
- application.yml: toggle for API key/OAuth2

**Sample Implementation:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  @Value("${security.mode}") private String mode;
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    if ("apikey".equals(mode)) {
      http.authorizeRequests().antMatchers("/employees/**").hasRole("ADMIN")
        .anyRequest().authenticated().and().httpBasic();
    } else {
      http.oauth2ResourceServer().jwt();
    }
    http.exceptionHandling().authenticationEntryPoint(...).accessDeniedHandler(...);
  }
}
```

---

## Section: Employee Clock In/Out

**Description:** Records attendance events, associates shifts, supports geofence/device capture, and corrections workflow.

**Design Specification:**
- Entity: Attendance (id, employee, clockIn, clockOut, shift, deviceId, location, status)
- Repository: AttendanceRepository
- Service: AttendanceService (clockIn, clockOut, corrections)
- Controller: AttendanceController
- Optional: Geofence validation, device capture

**Sample Implementation:**
```java
@Entity
public class Attendance {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private LocalDateTime clockIn;
  private LocalDateTime clockOut;
  @ManyToOne private Shift shift;
  private String deviceId;
  private String location;
  private String status; // NORMAL, CORRECTION_PENDING
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
  @PostMapping("/clock-in") public AttendanceResponse clockIn(@RequestBody ClockInRequest req) {...}
  @PostMapping("/clock-out") public AttendanceResponse clockOut(@RequestBody ClockOutRequest req) {...}
  @PostMapping("/correction") public CorrectionResponse requestCorrection(@RequestBody CorrectionRequest req) {...}
}
```

---

## Section: Shift & Schedule Management

**Description:** Manages shift templates, recurring schedules, blackout dates, and conflict detection.

**Design Specification:**
- Entity: Shift (id, name, start, end, recurrence, blackoutDates)
- Entity: Schedule (id, employee, shift, date)
- Repository: ShiftRepository, ScheduleRepository
- Service: ShiftService, ScheduleService (conflict detection)
- Controller: ShiftController, ScheduleController

**Sample Implementation:**
```java
@Entity
public class Shift {
  @Id @GeneratedValue private Long id;
  private String name;
  private LocalTime start;
  private LocalTime end;
  private String recurrence;
  @ElementCollection private List<LocalDate> blackoutDates;
}

@Entity
public class Schedule {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  @ManyToOne private Shift shift;
  private LocalDate date;
}

@Service
public class ScheduleService {
  public boolean hasConflict(Employee emp, LocalDate date, Shift shift) {...}
}
```

---

## Section: Leave & Absence Management

**Description:** Handles PTO/sick/unpaid leave requests, approval workflow, balance updates, and shift coverage flagging.

**Design Specification:**
- Entity: LeaveRequest (id, employee, type, startDate, endDate, status, approver)
- Repository: LeaveRequestRepository
- Service: LeaveService (request, approve, update balances)
- Controller: LeaveController

**Sample Implementation:**
```java
@Entity
public class LeaveRequest {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String type; // PTO, SICK, UNPAID
  private LocalDate startDate;
  private LocalDate endDate;
  private String status; // REQUESTED, APPROVED, DENIED
  @ManyToOne private Employee approver;
}

@RestController
@RequestMapping("/leave")
public class LeaveController {
  @PostMapping public LeaveResponse requestLeave(@RequestBody LeaveRequestDto req) {...}
  @PatchMapping("/{id}/approve") public LeaveResponse approve(@PathVariable Long id) {...}
}
```

---

## Section: Training & Certification Tracking

**Description:** Tracks employee certifications, expirations, blocks unqualified assignments, and supports document uploads.

**Design Specification:**
- Entity: Certification (id, employee, type, issueDate, expiryDate, documentUrl)
- Repository: CertificationRepository
- Service: CertificationService (alerts, assignment checks)
- Controller: CertificationController

**Sample Implementation:**
```java
@Entity
public class Certification {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String type;
  private LocalDate issueDate;
  private LocalDate expiryDate;
  private String documentUrl;
}

@Service
public class CertificationService {
  public boolean isQualified(Employee emp, String certType) {...}
  public List<Certification> getExpiringCerts(int days) {...}
}
```

---

## Section: Safety Incident Reporting

**Description:** Records safety incidents, supports workflow status, OSHA export, and dashboard metrics.

**Design Specification:**
- Entity: SafetyIncident (id, date, severity, location, description, involvedEmployees, status)
- Repository: SafetyIncidentRepository
- Service: SafetyIncidentService (workflow, export)
- Controller: SafetyIncidentController

**Sample Implementation:**
```java
@Entity
public class SafetyIncident {
  @Id @GeneratedValue private Long id;
  private LocalDate date;
  private String severity;
  private String location;
  private String description;
  @ManyToMany private List<Employee> involvedEmployees;
  private String status; // OPEN, INVESTIGATING, RESOLVED
}

@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
  @PostMapping public SafetyIncidentResponse report(@RequestBody SafetyIncidentRequest req) {...}
  @GetMapping("/export/osha") public ResponseEntity<Resource> exportOSHA(...) {...}
}
```

---

## Section: Equipment & Asset Assignment

**Description:** Assigns and tracks equipment/assets, validates certifications, logs history, and tracks condition.

**Design Specification:**
- Entity: Asset (id, type, serialNumber, condition, assignedTo, checkoutDate, returnDate)
- Repository: AssetRepository
- Service: AssetService (assignment, history, overdue)
- Controller: AssetController

**Sample Implementation:**
```java
@Entity
public class Asset {
  @Id @GeneratedValue private Long id;
  private String type;
  private String serialNumber;
  private String condition;
  @ManyToOne private Employee assignedTo;
  private LocalDate checkoutDate;
  private LocalDate returnDate;
}

@RestController
@RequestMapping("/assets")
public class AssetController {
  @PostMapping("/assign") public AssetResponse assign(@RequestBody AssetAssignRequest req) {...}
  @GetMapping("/overdue") public List<AssetResponse> overdueAssets() {...}
}
```

---

## Section: Performance Reviews & Goals

**Description:** Conducts structured reviews, tracks goals, supports PDF export, and maintains immutable history.

**Design Specification:**
- Entity: PerformanceReview (id, employee, period, goals, ratings, comments, signedOff)
- Repository: PerformanceReviewRepository
- Service: PerformanceReviewService (review, goal tracking, export)
- Controller: PerformanceReviewController

**Sample Implementation:**
```java
@Entity
public class PerformanceReview {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String period;
  @ElementCollection private List<String> goals;
  @ElementCollection private List<String> ratings;
  private String comments;
  private boolean signedOff;
}

@RestController
@RequestMapping("/reviews")
public class PerformanceReviewController {
  @PostMapping public PerformanceReviewResponse create(@RequestBody PerformanceReviewRequest req) {...}
  @GetMapping("/{id}/export") public ResponseEntity<Resource> exportPdf(@PathVariable Long id) {...}
}
```

---

## Section: Payroll Export Integration

**Description:** Generates payroll files from attendance/leave, matches provider schema, delivers via SFTP/API, and logs audits.

**Design Specification:**
- Service: PayrollExportService (generate, deliver, audit)
- Integration: SFTP/API client
- Entity: PayrollExportLog (id, timestamp, status, details)
- Controller: PayrollExportController

**Sample Implementation:**
```java
@Service
public class PayrollExportService {
  public File generatePayrollFile(LocalDate period) {...}
  public void deliverPayroll(File file) {...}
}

@Entity
public class PayrollExportLog {
  @Id @GeneratedValue private Long id;
  private LocalDateTime timestamp;
  private String status;
  private String details;
}
```

---

## Section: Notifications & Announcements

**Description:** Sends notifications via in-app, email, SMS for events, supports delivery tracking, rate limits, and quiet hours.

**Design Specification:**
- Entity: Notification (id, recipient, type, content, status, sentAt)
- Service: NotificationService (send, track, rate limit)
- Controller: NotificationController
- Config: Quiet hours, rate limits

**Sample Implementation:**
```java
@Entity
public class Notification {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee recipient;
  private String type; // IN_APP, EMAIL, SMS
  private String content;
  private String status; // SENT, FAILED
  private LocalDateTime sentAt;
}

@Service
public class NotificationService {
  public void sendNotification(NotificationRequest req) {...}
}
```

---

## Section: Integration Layer for HRIS/WMS APIs

**Description:** Exposes REST APIs for master data sync, supports JWT/OAuth2 security, idempotent webhooks, and OpenAPI docs.

**Design Specification:**
- Controller: IntegrationController (HRIS, WMS endpoints)
- Security: JWT/OAuth2
- Webhook handling: idempotency keys
- OpenAPI annotations

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
  @PostMapping("/hris/sync") public ResponseEntity<?> syncHRIS(@RequestBody HRISSyncRequest req, @RequestHeader("Idempotency-Key") String key) {...}
  @PostMapping("/wms/sync") public ResponseEntity<?> syncWMS(@RequestBody WMSSyncRequest req) {...}
}
```

---

## Section: Audit Trail & Compliance Logging

**Description:** Tracks sensitive changes in a tamper-evident, immutable log table, supports export by date/user/entity.

**Design Specification:**
- Entity: AuditLog (id, actor, timestamp, entity, before, after, action)
- Repository: AuditLogRepository
- Service: AuditLogService (log, export)
- Controller: AuditLogController

**Sample Implementation:**
```java
@Entity
public class AuditLog {
  @Id @GeneratedValue private Long id;
  private String actor;
  private LocalDateTime timestamp;
  private String entity;
  @Lob private String before;
  @Lob private String after;
  private String action; // CREATE, UPDATE, DELETE
}

@RestController
@RequestMapping("/audit")
public class AuditLogController {
  @GetMapping public List<AuditLogResponse> export(@RequestParam Map<String,String> filters) {...}
}
```

---

## Section: Reporting & Analytics Dashboard

**Description:** Provides reports on attendance, overtime, leave, certifications, safety KPIs, supports CSV/PDF exports and role-based access.

**Design Specification:**
- Service: ReportingService (generate, export)
- Controller: ReportingController
- Role-based access via @PreAuthorize

**Sample Implementation:**
```java
@Service
public class ReportingService {
  public List<ReportRow> generateAttendanceReport(DateRange range, String department) {...}
  public File exportCsv(List<ReportRow> rows) {...}
}

@RestController
@RequestMapping("/reports")
public class ReportingController {
  @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
  @GetMapping("/attendance") public ResponseEntity<Resource> attendanceReport(...) {...}
}
```

---

## Section: Mobile Access via PWA

**Description:** Enables mobile-friendly workflows, installable PWA manifest, offline queue for clock events, Lighthouse score â¥ 80.

**Design Specification:**
- Frontend: PWA manifest, service worker
- Backend: Offline event queue API
- Controller: MobileController

**Sample Implementation:**
```javascript
// manifest.json (frontend)
{
  "name": "WMS Employee Portal",
  "short_name": "WMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
  @PostMapping("/clock-event/offline") public ResponseEntity<?> queueClockEvent(@RequestBody ClockEventRequest req) {...}
}
```

---

## Section: Onboarding & Offboarding Workflow Automation

**Description:** Automates provisioning/deprovisioning, generates training/asset tasks, integrates with HRIS.

**Design Specification:**
- Service: OnboardingService, OffboardingService
- Controller: OnboardingController, OffboardingController
- Integration: HRIS sync

**Sample Implementation:**
```java
@Service
public class OnboardingService {
  public void provisionAccount(Employee emp) {...}
  public void assignInitialTasks(Employee emp) {...}
}

@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
  @PostMapping("/start") public ResponseEntity<?> startOnboarding(@RequestBody EmployeeRequest req) {...}
}
```

---

## Section: Localization & Multi-Tenant Support

**Description:** Supports localization of UI/notifications and data isolation per tenant.

**Design Specification:**
- Entity: Tenant (id, name, config)
- Entity: LocalizedMessage (id, key, locale, value)
- Service: TenantService, LocalizationService
- Controller: TenantController, LocalizationController
- application.yml: tenant configs

**Sample Implementation:**
```java
@Entity
public class Tenant {
  @Id @GeneratedValue private Long id;
  private String name;
  private String config;
}

@Entity
public class LocalizedMessage {
  @Id @GeneratedValue private Long id;
  private String key;
  private String locale;
  private String value;
}

@Service
public class LocalizationService {
  public String getMessage(String key, String locale) {...}
}
```

---

## Section: Observability & Monitoring

**Description:** Monitors application health, metrics, logs, and configures alerts using Spring Boot Actuator, Prometheus, and Grafana.

**Design Specification:**
- Spring Boot Actuator endpoints enabled
- Prometheus metrics export
- Grafana dashboards for visualization
- Custom health indicators
- Alert configuration for critical metrics

**Sample Implementation:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
  health:
    db:
      enabled: true
```

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
  @Override
  public Health health() {
    // Custom health check logic
    return Health.up().withDetail("custom", "All systems operational").build();
  }
}
```

---

## Section: CI/CD & Deployment Automation

**Description:** Automates build, test, and deployment pipelines using GitHub Actions or Jenkins with support for staging/production environments and rollback capabilities.

**Design Specification:**
- GitHub Actions workflow or Jenkins pipeline
- Automated testing (unit, integration, e2e)
- Docker containerization
- Kubernetes deployment manifests
- Blue-green or canary deployment strategy
- Automated rollback on failure

**Sample Implementation:**
```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline
on:
  push:
    branches: [main, develop]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Build with Maven
        run: mvn clean package
      - name: Run tests
        run: mvn test
      - name: Build Docker image
        run: docker build -t wms-employee:${{ github.sha }} .
      - name: Deploy to staging
        if: github.ref == 'refs/heads/develop'
        run: kubectl apply -f k8s/staging/
      - name: Deploy to production
        if: github.ref == 'refs/heads/main'
        run: kubectl apply -f k8s/production/
```

---

## Summary

This technical design document provides comprehensive specifications for all 20 user stories of the Warehouse Employee Management System. Each section includes:

- Detailed architectural decisions aligned with Spring Boot best practices
- Complete entity models with JPA annotations
- Service layer specifications with business logic
- Repository interfaces leveraging Spring Data JPA
- REST controller designs with proper HTTP methods
- Security configurations using Spring Security
- Integration points for external systems
- Configuration examples
- Sample code implementations

**Technology Stack:**
- Spring Boot 2.7+
- Spring Data JPA
- Spring Security (JWT/OAuth2)
- PostgreSQL
- Flyway/Liquibase
- Spring Boot Actuator
- Prometheus & Grafana
- Docker & Kubernetes
- GitHub Actions/Jenkins

**Key Design Principles:**
- RESTful API design
- Layered architecture (Controller â Service â Repository)
- Domain-driven design
- SOLID principles
- Security by default
- Observability and monitoring
- CI/CD automation
- Multi-tenancy support
- Internationalization

---

**Document Version:** 1.0
**Last Updated:** 2024
**Status:** Ready for Implementation