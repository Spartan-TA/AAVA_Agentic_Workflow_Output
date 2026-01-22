# Warehouse Employee Management System â Low-Level Technical Design Document

---

## Table of Contents

1. [E01: Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
2. [E02: Employee Master Data (CRUD)](#e02-employee-master-data-crud)
3. [E03: Role-Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
4. [E04: Time & Attendance (Clock In/Out)](#e04-time--attendance-clock-inout)
5. [E05: Shift & Schedule Management](#e05-shift--schedule-management)
6. [E06: Leave & Absence Management](#e06-leave--absence-management)
7. [E07: Training & Certification Tracking](#e07-training--certification-tracking)
8. [E08: Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
9. [E09: Equipment & Asset Assignment](#e09-equipment--asset-assignment)
10. [E10: Performance Reviews & Goals](#e10-performance-reviews--goals)
11. [E11: Payroll Export Integration](#e11-payroll-export-integration)
12. [E12: Notifications & Announcements](#e12-notifications--announcements)
13. [E13: Integration Layer (HRIS/WMS APIs)](#e13-integration-layer-hriswms-apis)
14. [E14: Audit Trail & Compliance](#e14-audit-trail--compliance)
15. [E15: Reporting & Analytics](#e15-reporting--analytics)
16. [E16: Mobile Access (PWA)](#e16-mobile-access-pwa)
17. [E17: Onboarding & Offboarding Workflow](#e17-onboarding--offboarding-workflow)
18. [E18: Localization & Multi-Tenant](#e18-localization--multi-tenant)
19. [E19: Observability & Monitoring](#e19-observability--monitoring)
20. [E20: Deployment & CI/CD](#e20-deployment--cicd)

---

## Section: E01 - Project Scaffolding & Domain Setup

**Description:**  
Establishes the foundational Spring Boot project structure, configures base packages, sets up core modules, and integrates Flyway/Liquibase for DB migrations and Actuator for health monitoring.

**Design Specification:**
- **Architecture:** Layered (Controller, Service, Repository, Domain, Config)
- **Package Structure:**
  - `com.wms.employee` (root)
    - `config`
    - `domain`
    - `repository`
    - `service`
    - `controller`
    - `dto`
    - `exception`
    - `security`
    - `integration`
    - `audit`
    - `reporting`
    - `mobile`
- **Modules:** Employee, Scheduling, Attendance, Safety
- **DB Migration:** Flyway/Liquibase scripts in `src/main/resources/db/migration`
- **Health Monitoring:** Spring Boot Actuator enabled
- **Build Tool:** Maven (`pom.xml` with dependencies for Spring Boot Starter, Data JPA, Security, Actuator, Flyway/Liquibase, Web, Validation, Lombok)

**Sample Implementation:**
```java
// src/main/java/com/wms/employee/WmsEmployeeApplication.java
@SpringBootApplication
public class WmsEmployeeApplication {
    public static void main(String[] args) {
        SpringApplication.run(WmsEmployeeApplication.class, args);
    }
}

// src/main/resources/application.yml
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
        include: health,info
```

---

## Section: E02 - Employee Master Data (CRUD)

**Description:**  
Implements CRUD operations for Employee entities, enforcing unique badge IDs, supporting soft deletes, pagination, filtering, and OpenAPI documentation.

**Design Specification:**
- **Entity Design:**
  - `Employee` entity: `id`, `name`, `badgeId`, `role`, `department`, `shiftGroup`, `hireDate`, `status`, `deleted`
  - Relationships: Department, ShiftGroup (ManyToOne)
- **Service Layer:** Business logic for CRUD, validation, soft delete
- **Repository Layer:** Extends `JpaRepository<Employee, Long>`, custom queries for filtering/pagination
- **Controller:** REST endpoints `/employees` (CRUD), supports pagination/filtering
- **Validation:** Bean validation (`@NotNull`, `@Size`, `@Pattern`)
- **Exception Handling:** Custom exceptions, `@ControllerAdvice`
- **OpenAPI:** Swagger annotations for DTOs and endpoints

**Sample Implementation:**
```java
// domain/Employee.java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badgeId"))
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank @Column(unique = true)
    private String badgeId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne
    private Department department;

    @ManyToOne
    private ShiftGroup shiftGroup;

    @NotNull
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    private boolean deleted = false;
}

// repository/EmployeeRepository.java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}

// service/EmployeeService.java
@Service
public class EmployeeService {
    @Transactional
    public Employee createEmployee(EmployeeDto dto) { /* ... */ }
    @Transactional
    public Employee updateEmployee(Long id, EmployeeDto dto) { /* ... */ }
    @Transactional
    public void softDeleteEmployee(Long id) { /* ... */ }
}

// controller/EmployeeController.java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { /* ... */ }
    @GetMapping public Page<EmployeeDto> list(Pageable pageable, @RequestParam Map<String, String> filters) { /* ... */ }
    @PutMapping("/{id}") public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) { /* ... */ }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { /* ... */ }
}
```

---

## Section: E03 - Role-Based Access Control (RBAC)

**Description:**  
Integrates Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER), method/endpoint security, row-level constraints, and API key/OAuth2 toggle.

**Design Specification:**
- **Security Config:** `WebSecurityConfigurerAdapter` or `SecurityFilterChain` (Spring Boot 2/3)
- **Roles:** Enum `Role { ADMIN, HR, SUPERVISOR, WORKER }`
- **Method Security:** `@PreAuthorize`, `@Secured`
- **Endpoint Security:** Restrict endpoints by role
- **Row-Level Security:** Service layer checks for team/supervisor constraints
- **API Key/OAuth2:** Configurable via `application.yml`
- **Exception Handling:** 401/403 responses, custom `AccessDeniedHandler`
- **Tests:** Security integration tests

**Sample Implementation:**
```java
// security/SecurityConfig.java
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
                .httpBasic();
    }
}

// service/EmployeeService.java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isSupervisorOf(#id, authentication))")
public Employee updateEmployee(Long id, EmployeeDto dto) { /* ... */ }
```

---

## Section: E04 - Time & Attendance (Clock In/Out)

**Description:**  
Provides endpoints for clock-in/out events, geofence/device capture, calculates hours worked, handles missed punches and corrections workflow.

**Design Specification:**
- **Entity Design:** `AttendanceEvent` (`id`, `employee`, `type`, `timestamp`, `location`, `deviceId`, `approved`)
- **Service Layer:** Clock-in/out logic, shift association, corrections workflow
- **Repository Layer:** Attendance queries, daily totals
- **Controller:** `/attendance/clock-in`, `/attendance/clock-out`, `/attendance/corrections`
- **Validation:** Geofence, device checks
- **Reports:** Export attendance (CSV)
- **Workflow:** Corrections create approval tasks

**Sample Implementation:**
```java
// domain/AttendanceEvent.java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @Enumerated(EnumType.STRING)
    private EventType type; // CLOCK_IN, CLOCK_OUT
    private LocalDateTime timestamp;
    private String location;
    private String deviceId;
    private boolean approved;
}

// controller/AttendanceController.java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockInDto dto) { /* ... */ }
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockOutDto dto) { /* ... */ }
    @PostMapping("/corrections")
    public ResponseEntity<?> requestCorrection(@RequestBody CorrectionDto dto) { /* ... */ }
}
```

---

## Section: E05 - Shift & Schedule Management

**Description:**  
Manages recurring shift templates, rotations, overtime rules, assignment to employees, blackout dates, and operation calendars.

**Design Specification:**
- **Entity Design:** `ShiftTemplate`, `ShiftAssignment`, `BlackoutDate`
- **Service Layer:** CRUD for templates/schedules, conflict detection, bulk assignment
- **Repository Layer:** Shift queries, conflict checks
- **Controller:** `/shifts/templates`, `/shifts/assignments`, `/shifts/blackout-dates`
- **Audit:** Generate audit entries for changes

**Sample Implementation:**
```java
// domain/ShiftTemplate.java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    private String rotationPattern;
}

// controller/ShiftController.java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates")
    public ResponseEntity<?> createTemplate(@RequestBody ShiftTemplateDto dto) { /* ... */ }
    @PostMapping("/assignments/bulk")
    public ResponseEntity<?> bulkAssign(@RequestBody BulkAssignmentDto dto) { /* ... */ }
}
```

---

## Section: E06 - Leave & Absence Management

**Description:**  
Handles PTO, sick, unpaid leave requests/approvals, accrual balances, policies, and integration with scheduling/payroll.

**Design Specification:**
- **Entity Design:** `LeaveRequest`, `LeaveBalance`, `LeavePolicy`
- **Service Layer:** Request/approve workflow, balance updates, integration hooks
- **Repository Layer:** Leave queries, balance calculations
- **Controller:** `/leave/requests`, `/leave/balances`, `/leave/policies`
- **Integration:** Exclude from scheduling/payroll

**Sample Implementation:**
```java
// domain/LeaveRequest.java
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
}

// controller/LeaveController.java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/requests")
    public ResponseEntity<?> requestLeave(@RequestBody LeaveRequestDto dto) { /* ... */ }
    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<?> approveLeave(@PathVariable Long id) { /* ... */ }
}
```

---

## Section: E07 - Training & Certification Tracking

**Description:**  
Tracks certifications, expirations, renewals, blocks assignments for expired certs, and uploads proof documents.

**Design Specification:**
- **Entity Design:** `Certification`, `EmployeeCertification`
- **Service Layer:** CRUD, expiry alerts, assignment checks
- **Repository Layer:** Certification queries, expiry checks
- **Controller:** `/certifications`, `/certifications/alerts`
- **Integration:** Scheduling checks for qualification

**Sample Implementation:**
```java
// domain/Certification.java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalDate expiryDate;
    private String documentUrl;
}

// controller/CertificationController.java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<?> addCertification(@RequestBody CertificationDto dto) { /* ... */ }
    @GetMapping("/alerts")
    public List<CertificationAlertDto> getExpiryAlerts() { /* ... */ }
}
```

---

## Section: E08 - Safety Incidents & OSHA Reporting

**Description:**  
Records safety incidents, severity, location, involved employees, investigation workflow, and OSHA summary generation.

**Design Specification:**
- **Entity Design:** `SafetyIncident`, `IncidentStatus`
- **Service Layer:** Incident workflow, OSHA export
- **Repository Layer:** Incident queries, metrics
- **Controller:** `/safety/incidents`, `/safety/osha`
- **Reporting:** OSHA 300/300A fields, dashboard endpoints

**Sample Implementation:**
```java
// domain/SafetyIncident.java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String description;
    private String location;
    @ManyToMany
    private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private IncidentSeverity severity;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
}

// controller/SafetyController.java
@RestController
@RequestMapping("/safety")
public class SafetyController {
    @PostMapping("/incidents")
    public ResponseEntity<?> reportIncident(@RequestBody SafetyIncidentDto dto) { /* ... */ }
    @GetMapping("/osha")
    public ResponseEntity<Resource> exportOshaReport() { /* ... */ }
}
```

---

## Section: E09 - Equipment & Asset Assignment

**Description:**  
Assigns equipment/assets to employees, tracks checkout/return, blocks use if certification missing, maintains asset condition.

**Design Specification:**
- **Entity Design:** `Asset`, `AssetAssignment`, `AssetCondition`
- **Service Layer:** Asset registry, check-in/out, certification checks
- **Repository Layer:** Asset queries, history logs
- **Controller:** `/assets`, `/assets/assignments`
- **Reporting:** Overdue returns, history per asset/employee

**Sample Implementation:**
```java
// domain/Asset.java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private AssetCondition condition;
}

// controller/AssetController.java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign")
    public ResponseEntity<?> assignAsset(@RequestBody AssetAssignmentDto dto) { /* ... */ }
    @PostMapping("/return")
    public ResponseEntity<?> returnAsset(@RequestBody AssetReturnDto dto) { /* ... */ }
}
```

---

## Section: E10 - Performance Reviews & Goals

**Description:**  
Manages review templates, goals, competencies, ratings, comments, and supervisor/employee acknowledgements.

**Design Specification:**
- **Entity Design:** `PerformanceReview`, `ReviewCycle`, `Goal`
- **Service Layer:** Review cycles, submission workflow, PDF export
- **Repository Layer:** Review queries, immutable history
- **Controller:** `/reviews`, `/reviews/cycles`
- **Security:** Role-based visibility

**Sample Implementation:**
```java
// domain/PerformanceReview.java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String comments;
    private int rating;
    private boolean acknowledged;
}

// controller/ReviewController.java
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping
    public ResponseEntity<?> submitReview(@RequestBody PerformanceReviewDto dto) { /* ... */ }
    @GetMapping("/cycles")
    public List<ReviewCycleDto> getReviewCycles() { /* ... */ }
}
```

---

## Section: E11 - Payroll Export Integration

**Description:**  
Generates payroll-ready files from attendance/leave, maps to provider formats, delivers securely via SFTP/API.

**Design Specification:**
- **Service Layer:** Export generation, schema mapping, delivery/retry logic
- **Repository Layer:** Export logs, reconciliation
- **Controller:** `/payroll/exports`
- **Integration:** SFTP/API clients, audit log

**Sample Implementation:**
```java
// service/PayrollExportService.java
@Service
public class PayrollExportService {
    public File generatePayrollExport(LocalDate periodStart, LocalDate periodEnd) { /* ... */ }
    public void deliverExport(File exportFile) { /* ... */ }
}

// controller/PayrollController.java
@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/exports")
    public ResponseEntity<?> exportPayroll(@RequestBody PayrollExportRequestDto dto) { /* ... */ }
}
```

---

## Section: E12 - Notifications & Announcements

**Description:**  
Sends in-app/email/SMS notifications for shift changes, expiring certs, approvals, announcements; supports quiet hours.

**Design Specification:**
- **Entity Design:** `Notification`, `Announcement`
- **Service Layer:** Delivery logic, opt-in/out, template localization, rate limiting
- **Repository Layer:** Notification status tracking
- **Controller:** `/notifications`, `/announcements`
- **Integration:** Email/SMS providers

**Sample Implementation:**
```java
// domain/Notification.java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee recipient;
    private String channel; // EMAIL, SMS, IN_APP
    private String message;
    private boolean delivered;
}

// service/NotificationService.java
@Service
public class NotificationService {
    public void sendNotification(NotificationDto dto) { /* ... */ }
}

// controller/NotificationController.java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping
    public ResponseEntity<?> send(@RequestBody NotificationDto dto) { /* ... */ }
}
```

---

## Section: E13 - Integration Layer (HRIS/WMS APIs)

**Description:**  
Exposes REST APIs/connectors for HRIS, WMS, IDP for SSO, and webhooks for events.

**Design Specification:**
- **Service Layer:** HRIS sync jobs, WMS connectors, SSO integration
- **Controller:** `/api/hris`, `/api/wms`, `/api/idp`, `/webhooks`
- **Security:** JWT/OAuth2
- **OpenAPI:** API documentation

**Sample Implementation:**
```java
// integration/HrisIntegrationService.java
@Service
public class HrisIntegrationService {
    public void syncEmployees() { /* ... */ }
}

// controller/IntegrationController.java
@RestController
@RequestMapping("/api")
public class IntegrationController {
    @PostMapping("/hris/sync")
    public ResponseEntity<?> syncHris() { /* ... */ }
    @PostMapping("/wms/link")
    public ResponseEntity<?> linkWms(@RequestBody WmsLinkDto dto) { /* ... */ }
}
```

---

## Section: E14 - Audit Trail & Compliance

**Description:**  
Centralized audit logging for sensitive changes, tamper-evident storage, exportable logs.

**Design Specification:**
- **Entity Design:** `AuditLog`
- **Service Layer:** Audit event creation, export logic
- **Repository Layer:** Audit queries
- **Controller:** `/audit/logs`
- **Security:** Immutable log table

**Sample Implementation:**
```java
// domain/AuditLog.java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private Long entityId;
    private String actor;
    private LocalDateTime timestamp;
    private String before;
    private String after;
}

// service/AuditService.java
@Service
public class AuditService {
    public void logChange(String entity, Long entityId, String actor, Object before, Object after) { /* ... */ }
}

// controller/AuditController.java
@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/logs")
    public List<AuditLogDto> getLogs(@RequestParam Map<String, String> filters) { /* ... */ }
}
```

---

## Section: E15 - Reporting & Analytics

**Description:**  
Provides operational reports (attendance, overtime, leave, certifications, safety KPIs), CSV/PDF exports, dashboards.

**Design Specification:**
- **Service Layer:** Report generation, filtering, export logic
- **Controller:** `/reports`, `/metrics`
- **Security:** Role-based access
- **Performance:** Exports â¤10s for 50k rows

**Sample Implementation:**
```java
// service/ReportService.java
@Service
public class ReportService {
    public File generateAttendanceReport(ReportFilterDto filter) { /* ... */ }
    public File generateSafetyKpiReport(ReportFilterDto filter) { /* ... */ }
}

// controller/ReportController.java
@RestController
@RequestMapping("/reports")
public class ReportController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> exportAttendance(@RequestParam Map<String, String> filters) { /* ... */ }
}
```

---

## Section: E16 - Mobile Access (PWA)

**Description:**  
Responsive views for mobile, offline-friendly PWA for clock-in/out, schedules, leave requests, announcements.

**Design Specification:**
- **Frontend:** PWA manifest, service worker, responsive UI
- **Backend:** REST endpoints for mobile flows
- **Offline:** Queue clock events, conflict resolution
- **Performance:** Lighthouse PWA score â¥80

**Sample Implementation:**
```yaml
# public/manifest.json
{
  "name": "WMS Employee PWA",
  "short_name": "WMS PWA",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "icons": [ ... ]
}
```
```java
// controller/MobileController.java
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @PostMapping("/clock-event")
    public ResponseEntity<?> queueClockEvent(@RequestBody ClockEventDto dto) { /* ... */ }
}
```

---

## Section: E17 - Onboarding & Offboarding Workflow

**Description:**  
Automates provisioning/deprovisioning of accounts, schedules, training, asset assignment, and access.

**Design Specification:**
- **Service Layer:** Onboarding/offboarding logic, task generation
- **Controller:** `/onboarding`, `/offboarding`
- **Integration:** HRIS sync, asset collection, schedule updates

**Sample Implementation:**
```java
// service/OnboardingService.java
@Service
public class OnboardingService {
    public void onboardEmployee(Long employeeId) { /* ... */ }
    public void offboardEmployee(Long employeeId) { /* ... */ }
}

// controller/OnboardingController.java
@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @PostMapping("/{id}")
    public ResponseEntity<?> onboard(@PathVariable Long id) { /* ... */ }
}
```

---

## Section: E18 - Localization & Multi-Tenant

**Description:**  
Supports multiple languages and tenants, with isolated data and localized templates.

**Design Specification:**
- **Config:** `LocaleResolver`, message bundles in `resources/i18n`
- **Entity Design:** `Tenant`, tenant-aware entities
- **Service Layer:** Tenant context, localization logic
- **Controller:** `/localization`, `/tenants`
- **Security:** Data isolation per tenant

**Sample Implementation:**
```java
// config/LocaleConfig.java
@Configuration
public class LocaleConfig {
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.ENGLISH);
        return slr;
    }
}

// domain/Tenant.java
@Entity
public class Tenant {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String locale;
}
```

---

## Section: E19 - Observability & Monitoring

**Description:**  
Implements metrics, tracing, logging, and alerting for system health and performance.

**Design Specification:**
- **Actuator:** Expose `/actuator/health`, `/actuator/metrics`, `/actuator/loggers`
- **Tracing:** Integrate with OpenTelemetry/Zipkin
- **Logging:** Centralized logs, logback config
- **Alerting:** Health checks, custom alerts

**Sample Implementation:**
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,loggers
  tracing:
    enabled: true
```
```java
// config/ObservabilityConfig.java
@Configuration
public class ObservabilityConfig {
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("application", "wms-employee");
    }
}
```

---

## Section: E20 - Deployment & CI/CD

**Description:**  
Defines deployment pipelines, containerization, environment configs, and CI/CD automation.

**Design Specification:**
- **Docker:** `Dockerfile` for Spring Boot app
- **CI/CD:** GitHub Actions/Jenkins pipeline for build, test, deploy
- **Config:** Environment variables, secrets management
- **Health Checks:** Readiness/liveness probes

**Sample Implementation:**
```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim
COPY target/wms-employee.jar /app/wms-employee.jar
ENTRYPOINT ["java", "-jar", "/app/wms-employee.jar"]
```
```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build
        run: mvn clean package
      - name: Test
        run: mvn test
      - name: Docker Build
        run: docker build -t wms-employee .
      - name: Deploy
        run: echo "Deploy step here"
```

---

# Appendix

- **Exception Handling:** Use `@ControllerAdvice` for global error handling.
- **Validation:** Use `javax.validation` annotations and custom validators.
- **Security:** Use `@PreAuthorize`, `@Secured`, and method-level security.
- **Documentation:** OpenAPI/Swagger for all REST endpoints.
- **Testing:** Unit, integration, and security tests for all modules.

---

**End of Technical Design Document**