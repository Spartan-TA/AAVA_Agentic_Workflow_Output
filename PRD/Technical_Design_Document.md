# Warehouse Employee Management System (EMS)
# Technical Design Document

---

## Table of Contents
1. [Spring Boot Architecture Overview](#architecture-overview)
2. [Package Structure & Module Definitions](#package-structure)
3. [Epic/User Story Technical Designs](#epic-designs)
    - [E01 Project Scaffolding & Domain Setup](#e01)
    - [E02 Employee Master Data (CRUD)](#e02)
    - [E03 Role Based Access Control (RBAC)](#e03)
    - [E04 Time & Attendance (Clock In/Out)](#e04)
    - [E05 Shift & Schedule Management](#e05)
    - [E06 Leave & Absence Management](#e06)
    - [E07 Training & Certification Tracking](#e07)
    - [E08 Safety Incidents & OSHA Reporting](#e08)
    - [E09 Equipment & Asset Assignment](#e09)
    - [E10 Performance Reviews & Goals](#e10)
    - [E11 Payroll Export Integration](#e11)
    - [E12 Notifications & Announcements](#e12)
    - [E13 Integration Layer (HRIS/WMS APIs)](#e13)
    - [E14 Audit Trail & Compliance](#e14)
    - [E15 Reporting & Analytics](#e15)
    - [E16 Mobile Access (PWA)](#e16)
    - [E17 Onboarding & Offboarding Workflow](#e17)
    - [E18 Localization & Multi-Tenant](#e18)
    - [E19 AI Scheduling](#e19)
    - [E20 CI/CD & Monitoring](#e20)
4. [Global Configuration & Security](#global-config)
5. [Integration Points](#integration-points)
6. [Appendix: Code Snippets & Patterns](#appendix)

---

## <a name="architecture-overview"></a>1. Spring Boot Architecture Overview
- **Layered Architecture:**
    - Presentation (REST Controllers, PWA endpoints)
    - Service (Business logic, orchestration)
    - Repository (Spring Data JPA, QueryDSL)
    - Domain (Entities, Value Objects, Enums)
    - Integration (APIs, SFTP, Messaging)
    - Configuration (Security, Profiles, Multi-tenancy)
- **Patterns:**
    - DTOs for API boundaries
    - Service interfaces for testability
    - Event-driven (ApplicationEvents, IntegrationEvents)
    - Exception handling via @ControllerAdvice
    - OpenAPI/Swagger for API docs
- **Best Practices:**
    - Use of @Transactional
    - Constructor injection
    - Validation via javax.validation
    - Centralized error handling
    - Logging with SLF4J
    - Flyway/Liquibase for DB migrations
    - Actuator for health/metrics

---

## <a name="package-structure"></a>2. Package Structure & Module Definitions
```
com.warehouse.ems
âââ config
âââ domain
â   âââ employee
â   âââ attendance
â   âââ shift
â   âââ leave
â   âââ training
â   âââ safety
â   âââ equipment
â   âââ review
â   âââ payroll
â   âââ notification
â   âââ integration
â   âââ audit
â   âââ reporting
â   âââ mobile
â   âââ onboarding
â   âââ localization
â   âââ ai
âââ repository
âââ service
âââ web
â   âââ controller
â   âââ dto
âââ integration
â   âââ hris
â   âââ wms
â   âââ idp
âââ util
âââ exception
âââ PRD
```
- Each domain subpackage contains: Entity, Repository, Service, Controller, DTO, Mapper.
- Integration modules for external APIs, SFTP, webhooks.
- Config for security, multi-tenancy, profiles.

---

## <a name="epic-designs"></a>3. Epic/User Story Technical Designs

### <a name="e01"></a>E01 Project Scaffolding & Domain Setup
**Description:** Initialize Spring Boot (Maven), configure base packages, set up core modules, add Flyway/Liquibase, enable Actuator.

**Design:**
- **Spring Initializr:** Maven, Java 17+, Spring Boot 3.x, dependencies: Web, JPA, Security, Validation, Actuator, Flyway/Liquibase, Lombok, OpenAPI.
- **Base Packages:** See [Package Structure](#package-structure).
- **DB Migration:**
    - `/src/main/resources/db/migration/V1__baseline.sql`
    - Use Flyway/Liquibase for schema evolution.
- **Health Check:**
    - `management.endpoints.web.exposure.include=health,info,metrics`
- **Sample Implementation:**
```java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

---

### <a name="e02"></a>E02 Employee Master Data (CRUD)
**Description:** Employee domain with CRUD APIs and web DTOs: name, badgeId, role, department, shiftGroup, hireDate, status.

**Entity Design:**
```java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    @Column(unique = true, nullable = false)
    private String badgeId;
    private String name;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;
    private boolean deleted = false;
    // getters/setters
}
```
**Repository:**
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```
**Service:**
```java
public interface EmployeeService {
    EmployeeDto create(EmployeeDto dto);
    EmployeeDto update(Long id, EmployeeDto dto);
    void delete(Long id);
    Page<EmployeeDto> list(Pageable pageable, EmployeeFilter filter);
}
```
**Controller:**
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) {...}
    @GetMapping
    public Page<EmployeeDto> list(...) {...}
    @PutMapping("/{id}")
    public EmployeeDto update(@PathVariable Long id, @RequestBody EmployeeDto dto) {...}
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {...}
}
```
**Validation:**
- Unique badgeId, soft-delete, pagination, filtering.
- OpenAPI schemas with examples.

---

### <a name="e03"></a>E03 Role Based Access Control (RBAC)
**Description:** Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER), method/endpoint security, row-level constraints, API key/OAuth2 toggle.

**Config:**
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers(HttpMethod.POST, "/employees/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer().jwt();
    }
}
```
**Row-level Security:**
- Service methods filter by supervisor/team.
- @PreAuthorize annotations for method-level security.
**API Key/OAuth2 Toggle:**
- Profiles: `application-oauth2.yml`, `application-apikey.yml`.

---

### <a name="e04"></a>E04 Time & Attendance (Clock In/Out)
**Description:** Endpoints for clock-in/out, geofence/device capture, calculate hours, missed punches, corrections workflow.

**Entity:**
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private String deviceId;
    private String location;
    private boolean correctionPending;
    // ...
}
```
**Service:**
- Validate geofence/device.
- Calculate shift association, daily totals.
- Correction workflow: create approval tasks.
**Controller:**
```java
@PostMapping("/attendance/clock-in")
public ResponseEntity<?> clockIn(@RequestBody ClockInDto dto) {...}
```
**Reports:**
- Export CSV endpoint.

---

### <a name="e05"></a>E05 Shift & Schedule Management
**Description:** Recurring shift templates, rotations, overtime rules, assignment, blackout dates, operation calendars.

**Entities:**
- ShiftTemplate, ShiftAssignment, BlackoutDate
**Service:**
- CRUD for templates, assign to employees, detect conflicts.
- Bulk assignment for supervisors.
**Controller:**
- `/shifts`, `/schedules`
**Audit:**
- Generate audit entries on changes.

---

### <a name="e06"></a>E06 Leave & Absence Management
**Description:** PTO/sick/unpaid leave requests, approvals, accruals, integration with scheduling/payroll.

**Entities:**
- LeaveRequest, LeaveBalance, LeavePolicy
**Workflow:**
- Employee requests, supervisor approves/denies.
- Balances update, shifts auto-flagged.
- Exports for approved leaves.

---

### <a name="e07"></a>E07 Training & Certification Tracking
**Description:** Track certifications, expirations, renewals, block assignments, upload proof.

**Entities:**
- Certification, CertificationType, EmployeeCertification
**Service:**
- Alerts for expiry, scheduling checks, status on profile.

---

### <a name="e08"></a>E08 Safety Incidents & OSHA Reporting
**Description:** Record incidents, workflow for investigation, OSHA summary.

**Entities:**
- SafetyIncident, Investigation, OSHAReport
**Workflow:**
- Status: Open â Investigating â Resolved.
- Export OSHA 300/300A.
- Metrics dashboard endpoints.

---

### <a name="e09"></a>E09 Equipment & Asset Assignment
**Description:** Assign assets, track checkout/return, block if cert missing, asset condition.

**Entities:**
- Asset, AssetAssignment, AssetCondition
**Service:**
- Check-in/out, block on invalid cert, history log.
- Overdue reports.

---

### <a name="e10"></a>E10 Performance Reviews & Goals
**Description:** Review templates, goals, ratings, comments, acknowledgements.

**Entities:**
- PerformanceReview, ReviewCycle, Goal
**Workflow:**
- Assign, submit, acknowledge, PDF export, immutable after sign-off.

---

### <a name="e11"></a>E11 Payroll Export Integration
**Description:** Generate payroll files, map to provider, secure delivery, audit.

**Service:**
- Export attendance/leave, map to schema, SFTP/API delivery, retry logic.
- Audit log for every export.

---

### <a name="e12"></a>E12 Notifications & Announcements
**Description:** In-app/email/SMS notifications, templates, quiet hours.

**Entities:**
- Notification, Announcement, DeliveryStatus
**Service:**
- Opt-in/out, localized templates, rate limits, dashboard visibility.

---

### <a name="e13"></a>E13 Integration Layer (HRIS/WMS APIs)
**Description:** REST APIs/connectors for HRIS, WMS, IDP, webhooks.

**Integration:**
- JWT/OAuth2-secured APIs.
- HRIS sync job, WMS link, idempotent webhooks.
- OpenAPI docs.

---

### <a name="e14"></a>E14 Audit Trail & Compliance
**Description:** Centralized audit logging for sensitive changes, tamper-evident.

**Entities:**
- AuditLog
**Service:**
- Log actor, timestamp, before/after, immutable storage.
- Export by date/user/entity.

---

### <a name="e15"></a>E15 Reporting & Analytics
**Description:** Attendance, overtime, leave, cert status, safety KPIs, CSV/PDF export, dashboards.

**Service:**
- Reports filter by date/department/shift.
- Metrics endpoints for BI.
- Access control.

---

### <a name="e16"></a>E16 Mobile Access (PWA)
**Description:** Responsive views for clock-in/out, schedules, leave, announcements, offline PWA.

**Design:**
- REST endpoints for mobile flows.
- PWA manifest, offline queue for clock events.
- Conflict resolution logic.

---

### <a name="e17"></a>E17 Onboarding & Offboarding Workflow
**Description:** Automate provisioning, initial schedule, training, deprovision access/assets.

**Workflow:**
- HRIS triggers new hire, tasks for training/assets.
- Offboarding revokes access, collects assets, updates schedules.

---

### <a name="e18"></a>E18 Localization & Multi-Tenant
**Description:** Multi-language, multi-tenant support.

**Config:**
- MessageSource for i18n.
- TenantContext, schema separation or discriminator column.
- Tenant-aware repositories/services.

---

### <a name="e19"></a>E19 AI Scheduling
**Description:** AI-driven shift optimization.

**Service:**
- Integrate with AI engine (external/internal).
- Suggest optimal schedules, resolve conflicts.
- Expose endpoints for recommendations.

---

### <a name="e20"></a>E20 CI/CD & Monitoring
**Description:** Automated build/test/deploy, monitoring.

**Config:**
- GitHub Actions/Jenkins pipelines.
- Dockerfile, Helm charts for K8s.
- Actuator metrics, Prometheus/Grafana integration.
- Alerting on failures.

---

## <a name="global-config"></a>4. Global Configuration & Security
- **application.yml:** Profiles for dev, test, prod.
- **Security:** OAuth2/JWT, API key toggle, CORS, CSRF.
- **Exception Handling:** @ControllerAdvice, ProblemDetails.
- **Validation:** javax.validation, custom validators.
- **Logging:** SLF4J, logback.xml, trace IDs.
- **Multi-tenancy:** TenantContext, filters/interceptors.

---

## <a name="integration-points"></a>5. Integration Points
- **HRIS:** REST API, scheduled sync, webhooks.
- **WMS:** REST API, department/location sync.
- **Payroll:** SFTP/API, export jobs.
- **IDP:** OAuth2/JWT, SSO.
- **Notifications:** SMTP, SMS gateway, push notifications.
- **Reporting:** CSV/PDF export, BI endpoints.

---

## <a name="appendix"></a>6. Appendix: Code Snippets & Patterns
- **DTO Example:**
```java
public class EmployeeDto {
    private Long id;
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}
```
- **Mapper Example:**
```java
@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeDto toDto(Employee entity);
    Employee toEntity(EmployeeDto dto);
}
```
- **Exception Handling:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetails> handleValidation(MethodArgumentNotValidException ex) {...}
}
```
- **Event Publishing:**
```java
public class EmployeeCreatedEvent extends ApplicationEvent {...}

@Component
public class EmployeeEventListener {
    @EventListener
    public void handle(EmployeeCreatedEvent event) {...}
}
```
- **Testing:**
```java
@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeControllerTest {
    @Autowired MockMvc mockMvc;
    ...
}
```

---

# End of Document
