# Warehouse Employee Management System â Low-Level Technical Design

## Table of Contents

1. [Overall System Architecture Overview](#overall-system-architecture-overview)
2. [Package Structure and Module Organization](#package-structure-and-module-organization)
3. [Epic-by-Epic Technical Design](#epic-by-epic-technical-design)
    - [E01: Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
    - [E02: Employee Master Data (CRUD)](#e02-employee-master-data-crud)
    - [E03: Role-Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
    - [E04: Time & Attendance (Clock In/Out)](#e04-time--attendance-clock-inout)
    - [E05: Shift & Schedule Management](#e05-shift--schedule-management)
    - [E06: Leave & Absence Management](#e06-leave--absence-management)
    - [E07: Training & Certification Tracking](#e07-training--certification-tracking)
    - [E08: Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
    - [E09: Equipment & Asset Assignment](#e09-equipment--asset-assignment)
    - [E10: Performance Reviews & Goals](#e10-performance-reviews--goals)
    - [E11: Payroll Export Integration](#e11-payroll-export-integration)
    - [E12: Notifications & Announcements](#e12-notifications--announcements)
    - [E13: Integration Layer (HRIS/WMS APIs)](#e13-integration-layer-hriswms-apis)
    - [E14: Audit Trail & Compliance](#e14-audit-trail--compliance)
    - [E15: Reporting & Analytics](#e15-reporting--analytics)
    - [E16: Mobile Access (PWA)](#e16-mobile-access-pwa)
    - [E17: Onboarding & Offboarding Workflow](#e17-onboarding--offboarding-workflow)
    - [E18: Localization & Multi-Tenant](#e18-localization--multi-tenant)
    - [E19: Observability & Monitoring](#e19-observability--monitoring)
    - [E20: CI/CD & Deployment Automation](#e20-cicd--deployment-automation)

---

## Overall System Architecture Overview

The Warehouse Employee Management System is a modular, microservice-ready Spring Boot application designed for extensibility, security, and maintainability. It follows a layered architecture:

- **Presentation Layer**: RESTful APIs (Spring MVC, Controllers, DTOs)
- **Service Layer**: Business logic, transaction management (@Service)
- **Persistence Layer**: JPA repositories, custom queries (@Repository)
- **Domain Layer**: Entities, value objects, enums (@Entity)
- **Security Layer**: Spring Security, RBAC, OAuth2, API Key
- **Integration Layer**: External APIs (HRIS, WMS), webhooks, SFTP
- **Infrastructure Layer**: Configuration, monitoring, CI/CD, localization

Key architectural patterns:
- **Hexagonal (Ports & Adapters)** for integration
- **Domain-Driven Design** for core modules
- **Configuration-over-convention** with `application.yml`
- **Observability** via Spring Boot Actuator, centralized logging, and metrics

---

## Package Structure and Module Organization

```
com.company.warehousemgmt
âââ config                # Security, CORS, Swagger, etc.
âââ domain
â   âââ employee
â   âââ attendance
â   âââ shift
â   âââ leave
â   âââ certification
â   âââ safety
â   âââ asset
â   âââ review
â   âââ notification
â   âââ integration
â   âââ audit
â   âââ reporting
âââ repository            # JPA repositories
âââ service               # Business logic
âââ controller            # REST endpoints
âââ dto                   # Request/response objects
âââ security              # RBAC, JWT, OAuth2, API Key
âââ integration           # HRIS, WMS, SFTP, webhooks
âââ util                  # Utilities, mappers, helpers
âââ Application.java      # Main entry point
```

---

## Epic-by-Epic Technical Design

---

### E01: Project Scaffolding & Domain Setup

#### Domain Model Design

- No business entities; focus on base structure.

#### Repository Layer

- Not applicable.

#### Service Layer

- Not applicable.

#### Controller Layer

- Not applicable.

#### Security Configuration

- Not applicable.

#### Integration Points

- Flyway/Liquibase for DB migrations.
- Spring Boot Actuator for health checks.

#### Configuration Properties

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse
    username: warehouse_user
    password: secret
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

#### Sample Code Snippets

**Main Application:**
```java
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}
```

**Flyway Migration Example:**
```sql
-- V1__baseline.sql
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    shift_group VARCHAR(32),
    hire_date DATE,
    status VARCHAR(16) NOT NULL
);
```

---

### E02: Employee Master Data (CRUD)

#### Domain Model Design

```java
@Entity
@Table(name = "employee")
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String badgeId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String department;
    private String shiftGroup;

    @Column(nullable = false)
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    // Getters, setters, equals, hashCode
}
```

#### Repository Layer

```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findByDepartmentAndStatus(String department, EmployeeStatus status, Pageable pageable);
}
```

#### Service Layer

```java
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional
    public Employee createEmployee(Employee employee) {
        // Validate unique badgeId, set defaults, etc.
        return employeeRepository.save(employee);
    }

    public Page<Employee> listEmployees(String department, EmployeeStatus status, Pageable pageable) {
        return employeeRepository.findByDepartmentAndStatus(department, status, pageable);
    }

    // update, soft-delete, etc.
}
```

#### Controller Layer

```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeDto> create(@RequestBody @Valid EmployeeDto dto) {
        Employee employee = employeeService.createEmployee(dto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(EmployeeDto.fromEntity(employee));
    }

    @GetMapping
    public Page<EmployeeDto> list(@RequestParam Optional<String> department,
                                  @RequestParam Optional<EmployeeStatus> status,
                                  Pageable pageable) {
        return employeeService.listEmployees(department.orElse(null), status.orElse(null), pageable)
            .map(EmployeeDto::fromEntity);
    }

    // PUT, PATCH, DELETE endpoints
}
```

#### Security Configuration

- Only ADMIN and HR can create/update/delete employees.
- SUPERVISOR can view team.
- WORKER can view own profile.

```java
@PreAuthorize("hasAnyRole('ADMIN','HR')")
@PostMapping
public ResponseEntity<EmployeeDto> create(@RequestBody @Valid EmployeeDto dto) { ... }
```

#### Integration Points

- Expose OpenAPI schemas.
- Webhooks for employee create/update.

#### Configuration Properties

```yaml
springdoc:
  api-docs:
    enabled: true
```

#### Sample Code Snippets

**DTO Example:**
```java
public class EmployeeDto {
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    // toEntity(), fromEntity() methods
}
```

---

### E03: Role-Based Access Control (RBAC)

#### Domain Model Design

```java
public enum Role {
    ADMIN, HR, SUPERVISOR, WORKER
}
```

#### Repository Layer

- Not applicable.

#### Service Layer

- UserDetailsService for authentication.

#### Controller Layer

- Secure endpoints with `@PreAuthorize`.

#### Security Configuration

```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer().jwt();
    }
}
```

- API Key/OAuth2 toggle via config.

```yaml
security:
  auth-type: OAUTH2 # or API_KEY
```

#### Integration Points

- SSO via OAuth2/JWT.

#### Configuration Properties

- See above.

#### Sample Code Snippets

**Method Security:**
```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteEmployee(Long id) { ... }
```

---

### E04: Time & Attendance (Clock In/Out)

#### Domain Model Design

```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private AttendanceType type; // CLOCK_IN, CLOCK_OUT

    private LocalDateTime timestamp;
    private String deviceId;
    private String geoLocation; // Optional

    // Getters, setters
}
```

#### Repository Layer

```java
@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeAndDate(Employee employee, LocalDate date);
}
```

#### Service Layer

```java
@Service
public class AttendanceService {
    @Autowired
    private AttendanceRepository attendanceRepository;

    @Transactional
    public AttendanceEvent clockIn(Long employeeId, String deviceId, String geoLocation) {
        // Validate, create event, associate shift, etc.
    }

    // clockOut, corrections, daily totals, etc.
}
```

#### Controller Layer

```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceDto> clockIn(@RequestBody ClockInRequest req) { ... }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceDto> clockOut(@RequestBody ClockOutRequest req) { ... }
}
```

#### Security Configuration

- Only authenticated users can clock in/out.
- Supervisors can approve corrections.

#### Integration Points

- Export attendance reports (CSV).
- Webhooks for missed punches.

#### Configuration Properties

```yaml
attendance:
  geofence-enabled: true
```

#### Sample Code Snippets

**DTO Example:**
```java
public class ClockInRequest {
    private Long employeeId;
    private String deviceId;
    private String geoLocation;
}
```

---

### E05: Shift & Schedule Management

#### Domain Model Design

```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    // Overtime rules, blackout dates, etc.
}

@Entity
public class EmployeeShiftAssignment {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    @ManyToOne
    private ShiftTemplate shiftTemplate;

    private LocalDate date;
}
```

#### Repository Layer

```java
@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> { }

@Repository
public interface EmployeeShiftAssignmentRepository extends JpaRepository<EmployeeShiftAssignment, Long> {
    List<EmployeeShiftAssignment> findByEmployeeAndDateBetween(Employee employee, LocalDate start, LocalDate end);
}
```

#### Service Layer

```java
@Service
public class ShiftService {
    // CRUD, conflict detection, bulk assignment, audit entries
}
```

#### Controller Layer

```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    // CRUD endpoints, personal schedule, supervisor bulk-assign
}
```

#### Security Configuration

- Supervisors can assign shifts to their team.
- Workers can view own schedule.

#### Integration Points

- Calendar integration.

#### Configuration Properties

```yaml
shifts:
  blackout-dates: [2024-12-25, 2025-01-01]
```

#### Sample Code Snippets

**Conflict Detection:**
```java
public boolean hasConflict(Employee employee, LocalDate date, LocalTime start, LocalTime end) {
    // Check for overlapping assignments
}
```

---

### E06: Leave & Absence Management

#### Domain Model Design

```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private LeaveType type; // PTO, SICK, UNPAID

    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveStatus status; // REQUESTED, APPROVED, DENIED

    private String reason;
}
```

#### Repository Layer

```java
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeAndStatus(Employee employee, LeaveStatus status);
}
```

#### Service Layer

```java
@Service
public class LeaveService {
    // Request, approve/deny, update balances, auto-flag shifts
}
```

#### Controller Layer

```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    // Endpoints for request, approve, list, export
}
```

#### Security Configuration

- Employees request leave.
- Supervisors approve/deny.

#### Integration Points

- Exclude from scheduling/payroll.

#### Configuration Properties

```yaml
leave:
  accrual-policy: STANDARD
```

#### Sample Code Snippets

**Balance Update:**
```java
public void updateLeaveBalance(Employee employee, LeaveType type, int days) { ... }
```

---

### E07: Training & Certification Tracking

#### Domain Model Design

```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;

    private String name;
    private LocalDate issueDate;
    private LocalDate expiryDate;

    @ManyToOne
    private Employee employee;

    private String documentUrl; // Proof
}
```

#### Repository Layer

```java
@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByExpiryDateBefore(LocalDate date);
}
```

#### Service Layer

- CRUD, expiry alerts, scheduling checks.

#### Controller Layer

- Endpoints for CRUD, alerts, status.

#### Security Configuration

- Only HR/Supervisor can update certifications.

#### Integration Points

- Block assignments if expired.

#### Configuration Properties

```yaml
certification:
  alert-days: [30, 7]
```

#### Sample Code Snippets

**Expiry Alert:**
```java
public List<Certification> findExpiringSoon() {
    LocalDate now = LocalDate.now();
    return certificationRepository.findByExpiryDateBefore(now.plusDays(30));
}
```

---

### E08: Safety Incidents & OSHA Reporting

#### Domain Model Design

```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;

    private String description;
    private String location;
    private IncidentSeverity severity;

    @ManyToMany
    private List<Employee> involvedEmployees;

    private IncidentStatus status; // OPEN, INVESTIGATING, RESOLVED
    private LocalDateTime reportedAt;
}
```

#### Repository Layer

- Standard CRUD.

#### Service Layer

- Workflow transitions, OSHA export.

#### Controller Layer

- Endpoints for report, update, export, dashboard.

#### Security Configuration

- Only authorized roles can update status.

#### Integration Points

- OSHA 300/300A export.

#### Configuration Properties

```yaml
safety:
  osha-export-enabled: true
```

#### Sample Code Snippets

**Workflow Transition:**
```java
public void advanceStatus(Long incidentId, IncidentStatus newStatus) { ... }
```

---

### E09: Equipment & Asset Assignment

#### Domain Model Design

```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String assetTag;
    private String type; // Scanner, Forklift, PPE
    private AssetCondition condition;
}

@Entity
public class AssetAssignment {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Asset asset;

    @ManyToOne
    private Employee employee;

    private LocalDateTime checkoutTime;
    private LocalDateTime returnTime;
}
```

#### Repository Layer

- Asset and assignment CRUD.

#### Service Layer

- Check-in/out, block if certs invalid, overdue reports.

#### Controller Layer

- Endpoints for registry, assignment, history.

#### Security Configuration

- Only authorized roles can assign assets.

#### Integration Points

- Certification check.

#### Configuration Properties

```yaml
asset:
  overdue-threshold-hours: 24
```

#### Sample Code Snippets

**Check-out Logic:**
```java
public void checkoutAsset(Long assetId, Long employeeId) {
    // Validate certs, update assignment
}
```

---

### E10: Performance Reviews & Goals

#### Domain Model Design

```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    private LocalDate reviewDate;
    private String template;
    private String goals;
    private String competencies;
    private String ratings;
    private String comments;
    private boolean acknowledgedByEmployee;
    private boolean acknowledgedBySupervisor;
}
```

#### Repository Layer

- CRUD, find by employee/cycle.

#### Service Layer

- Create cycles, submit, acknowledge, PDF export.

#### Controller Layer

- Endpoints for review, workflow, export.

#### Security Configuration

- Role-based visibility.

#### Integration Points

- None.

#### Configuration Properties

```yaml
review:
  templates: [quarterly, annual]
```

#### Sample Code Snippets

**Acknowledge Review:**
```java
public void acknowledgeReview(Long reviewId, Role role) { ... }
```

---

### E11: Payroll Export Integration

#### Domain Model Design

- Not applicable (integration focus).

#### Repository Layer

- Export log.

#### Service Layer

- Generate payroll files, reconcile, retry failed deliveries.

#### Controller Layer

- Export trigger, status endpoints.

#### Security Configuration

- Only ADMIN/HR.

#### Integration Points

- SFTP/API to payroll provider.

#### Configuration Properties

```yaml
payroll:
  provider: acme
  sftp:
    host: sftp.payroll.com
    user: payroll
    password: secret
```

#### Sample Code Snippets

**Export Logic:**
```java
public void exportPayroll(LocalDate period) {
    // Generate file, SFTP upload, log result
}
```

---

### E12: Notifications & Announcements

#### Domain Model Design

```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private String channel; // EMAIL, SMS, IN_APP
    private String template;
    private String recipient;
    private String status; // SENT, FAILED
    private LocalDateTime sentAt;
}
```

#### Repository Layer

- CRUD, delivery status.

#### Service Layer

- Send, track, rate limit, opt-in/out.

#### Controller Layer

- Endpoints for announcements, preferences.

#### Security Configuration

- Role-based delivery.

#### Integration Points

- Email/SMS providers.

#### Configuration Properties

```yaml
notification:
  quiet-hours: [22, 6]
```

#### Sample Code Snippets

**Send Notification:**
```java
public void sendNotification(NotificationDto dto) { ... }
```

---

### E13: Integration Layer (HRIS/WMS APIs)

#### Domain Model Design

- Not applicable (integration focus).

#### Repository Layer

- Sync logs.

#### Service Layer

- HRIS sync, WMS link, webhook handlers.

#### Controller Layer

- Expose REST APIs, webhooks.

#### Security Configuration

- JWT/OAuth2 for APIs.

#### Integration Points

- HRIS, WMS, IDP.

#### Configuration Properties

```yaml
integration:
  hris:
    enabled: true
  wms:
    enabled: true
```

#### Sample Code Snippets

**Webhook Handler:**
```java
@PostMapping("/webhook/hris")
public ResponseEntity<?> handleHrisEvent(@RequestBody HrisEventDto event) { ... }
```

---

### E14: Audit Trail & Compliance

#### Domain Model Design

```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private Long entityId;
    private String action;
    private String actor;
    private LocalDateTime timestamp;
    private String before;
    private String after;
}
```

#### Repository Layer

- CRUD, export by date/user/entity.

#### Service Layer

- Log on create/update/delete.

#### Controller Layer

- Export endpoints.

#### Security Configuration

- Only ADMIN/HR can export.

#### Integration Points

- Tamper-evident storage.

#### Configuration Properties

```yaml
audit:
  retention-days: 365
```

#### Sample Code Snippets

**Audit Logging:**
```java
public void logChange(String entity, Long id, String action, String actor, Object before, Object after) { ... }
```

---

### E15: Reporting & Analytics

#### Domain Model Design

- Not applicable (aggregation focus).

#### Repository Layer

- Custom queries for reports.

#### Service Layer

- Generate, filter, export reports.

#### Controller Layer

- Endpoints for CSV/PDF export, dashboards.

#### Security Configuration

- Role-based access.

#### Integration Points

- BI metrics endpoints.

#### Configuration Properties

```yaml
reporting:
  max-export-rows: 50000
```

#### Sample Code Snippets

**Report Export:**
```java
@GetMapping("/reports/attendance")
public void exportAttendanceReport(@RequestParam LocalDate from, @RequestParam LocalDate to, HttpServletResponse response) { ... }
```

---

### E16: Mobile Access (PWA)

#### Domain Model Design

- Reuse attendance, shift, leave, notification entities.

#### Repository Layer

- Not applicable.

#### Service Layer

- Mobile-friendly flows, offline queue.

#### Controller Layer

- Endpoints optimized for mobile.

#### Security Configuration

- JWT for mobile clients.

#### Integration Points

- PWA manifest, service worker.

#### Configuration Properties

```yaml
mobile:
  pwa-enabled: true
```

#### Sample Code Snippets

**PWA Manifest:**
```json
{
  "name": "Warehouse Employee Mgmt",
  "short_name": "Warehouse",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

---

### E17: Onboarding & Offboarding Workflow

#### Domain Model Design

```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    private String type; // ACCOUNT, TRAINING, ASSET
    private Long employeeId;
    private String status; // PENDING, COMPLETED
}
```

#### Repository Layer

- CRUD, find by employee.

#### Service Layer

- Generate tasks, automate provisioning/deprovisioning.

#### Controller Layer

- Endpoints for workflow status.

#### Security Configuration

- Only HR/ADMIN.

#### Integration Points

- HRIS, asset, training modules.

#### Configuration Properties

```yaml
onboarding:
  auto-provision: true
```

#### Sample Code Snippets

**Provisioning:**
```java
public void provisionNewHire(Long employeeId) { ... }
```

---

### E18: Localization & Multi-Tenant

#### Domain Model Design

- Tenant, Locale entities.

```java
@Entity
public class Tenant {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String locale;
}
```

#### Repository Layer

- Tenant CRUD.

#### Service Layer

- Locale resolution, tenant isolation.

#### Controller Layer

- Endpoints for tenant management.

#### Security Configuration

- Tenant admin roles.

#### Integration Points

- Locale files, i18n.

#### Configuration Properties

```yaml
localization:
  supported-locales: [en, es, fr]
multiTenant:
  enabled: true
```

#### Sample Code Snippets

**Locale Resolver:**
```java
@Bean
public LocaleResolver localeResolver() {
    SessionLocaleResolver slr = new SessionLocaleResolver();
    slr.setDefaultLocale(Locale.ENGLISH);
    return slr;
}
```

---

### E19: Observability & Monitoring

#### Domain Model Design

- Not applicable.

#### Repository Layer

- Not applicable.

#### Service Layer

- Not applicable.

#### Controller Layer

- Not applicable.

#### Security Configuration

- Not applicable.

#### Integration Points

- Spring Boot Actuator, centralized logging, metrics.

#### Configuration Properties

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,loggers
  metrics:
    export:
      prometheus:
        enabled: true
```

#### Sample Code Snippets

**Custom Metric:**
```java
@Autowired
private MeterRegistry meterRegistry;

public void recordCustomMetric() {
    meterRegistry.counter("custom.event.count").increment();
}
```

---

### E20: CI/CD & Deployment Automation

#### Domain Model Design

- Not applicable.

#### Repository Layer

- Not applicable.

#### Service Layer

- Not applicable.

#### Controller Layer

- Not applicable.

#### Security Configuration

- Not applicable.

#### Integration Points

- GitHub Actions, Docker, Kubernetes.

#### Configuration Properties

- Not applicable.

#### Sample Code Snippets

**GitHub Actions Workflow:**
```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main ]

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
      - name: Build Docker image
        run: docker build -t warehouse-mgmt:latest .
      - name: Push to Registry
        run: echo "Push step here"
```

---

## Appendix

- **OpenAPI/Swagger**: All endpoints documented via springdoc-openapi.
- **Testing**: Use @SpringBootTest, @DataJpaTest, and MockMvc for coverage.
- **Error Handling**: Global @ControllerAdvice for consistent API errors.
- **Validation**: javax.validation annotations on DTOs and entities.
- **Transaction Management**: @Transactional on service methods.
- **Soft Delete**: Use `status` or `deletedAt` fields for logical deletes.

---

**This document serves as the blueprint for implementing the Warehouse Employee Management System in Spring Boot, ensuring consistency, security, and scalability across all modules and user stories.**