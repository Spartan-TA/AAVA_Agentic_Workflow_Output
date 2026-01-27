# Warehouse Employee Management System â Low-Level Technical Design Document

---

## Table of Contents

1. [E01 â Project Scaffolding & Domain Setup](#e01)
2. [E02 â Employee Master Data (CRUD)](#e02)
3. [E03 â Role-Based Access Control (RBAC)](#e03)
4. [E04 â Time & Attendance (Clock In/Out)](#e04)
5. [E05 â Shift & Schedule Management](#e05)
6. [E06 â Leave & Absence Management](#e06)
7. [E07 â Training & Certification Tracking](#e07)
8. [E08 â Safety Incidents & OSHA Reporting](#e08)
9. [E09 â Equipment & Asset Assignment](#e09)
10. [E10 â Performance Reviews & Goals](#e10)
11. [E11 â Payroll Export Integration](#e11)
12. [E12 â Notifications & Announcements](#e12)
13. [E13 â Integration Layer (HRIS/WMS APIs)](#e13)
14. [E14 â Audit Trail & Compliance](#e14)
15. [E15 â Reporting & Analytics](#e15)
16. [E16 â Mobile Access (PWA)](#e16)
17. [E17 â Onboarding & Offboarding Workflow](#e17)

---

<a name="e01"></a>
## E01 â Project Scaffolding & Domain Setup

### 1. Overview of Spring Boot Architecture

**Description:**  
The project uses a modular, layered Spring Boot architecture (Maven multi-module recommended for large scale). Core modules: employee, scheduling, attendance, safety, etc.  
- RESTful APIs with Spring Web
- Data access via Spring Data JPA
- Database migrations with Flyway/Liquibase
- Monitoring with Spring Boot Actuator

### 2. Package Structure & Module Definitions

**Design Decisions:**  
- `com.companyname.wems` as base package  
- Submodules: `employee`, `attendance`, `shift`, `leave`, `training`, `safety`, `equipment`, `performance`, `payroll`, `notification`, `integration`, `audit`, `reporting`, `mobile`, `onboarding`
- Each module: `controller`, `service`, `repository`, `domain`, `dto`, `config`

**Sample Structure:**
```
com.companyname.wems
  âââ employee
  â    âââ controller
  â    âââ service
  â    âââ repository
  â    âââ domain
  â    âââ dto
  âââ attendance
  âââ shift
  âââ ...
  âââ config
```

### 3. Configuration & Security

- `application.yml` for environment configs
- Flyway/Liquibase for DB migrations
- Spring Boot Actuator enabled (`/actuator/health`)
- Security: Default permit all, to be hardened in E03

**Sample application.yml:**
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wems
    username: wems
    password: secret
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  flyway:
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### 4. Sample Implementation

**Main Application:**
```java
@SpringBootApplication
public class WemsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WemsApplication.class, args);
    }
}
```

**Flyway Migration Example (V1__init.sql):**
```sql
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    ...
);
```

---

<a name="e02"></a>
## E02 â Employee Master Data (CRUD)

### 1. Overview

**Description:**  
Centralized CRUD for employee records. Exposes REST APIs, uses DTOs, supports pagination/filtering, soft-delete, OpenAPI docs.

### 2. Package Structure

- `com.companyname.wems.employee`
  - `controller`
  - `service`
  - `repository`
  - `domain`
  - `dto`

### 3. Entity Design

**Employee Entity:**
```java
@Entity
@Table(name = "employee")
@SQLDelete(sql = "UPDATE employee SET deleted = true WHERE id=?")
@Where(clause = "deleted=false")
public class Employee {
    @Id @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String badgeId;

    @Column(nullable = false)
    private String name;

    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;

    private boolean deleted = false;
    // getters/setters
}
```

### 4. Service Layer

**EmployeeService:**
```java
public interface EmployeeService {
    EmployeeDTO create(EmployeeDTO dto);
    EmployeeDTO update(Long id, EmployeeDTO dto);
    EmployeeDTO get(Long id);
    Page<EmployeeDTO> list(EmployeeFilter filter, Pageable pageable);
    void delete(Long id);
}
```

### 5. Repository Layer

**EmployeeRepository:**
```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAll(Specification<Employee> spec, Pageable pageable);
}
```

### 6. Controller

**EmployeeController:**
```java
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee CRUD APIs")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeDTO dto) {...}

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> get(@PathVariable Long id) {...}

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> update(@PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) {...}

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {...}

    @GetMapping
    public Page<EmployeeDTO> list(EmployeeFilter filter, Pageable pageable) {...}
}
```

### 7. Validation & Exception Handling

- Use `@Valid` and Bean Validation annotations in DTOs.
- Global exception handler with `@ControllerAdvice`.

### 8. OpenAPI/Swagger

- Annotate controllers and DTOs.
- Example: `@Operation(summary = "Create employee", ...)`

---

<a name="e03"></a>
## E03 â Role-Based Access Control (RBAC)

### 1. Overview

**Description:**  
Spring Security-based RBAC. Roles: ADMIN, HR, SUPERVISOR, WORKER. Endpoint/method security, row-level constraints, API key/OAuth2 toggle.

### 2. Security Configuration

**SecurityConfig:**
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
            .oauth2ResourceServer().jwt(); // or API key toggle
    }
}
```

### 3. Method Security

- Use `@PreAuthorize` on service methods:
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == authentication.principal.department)")
public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {...}
```

### 4. Exception Handling

- 401 for unauthorized, 403 for forbidden.
- Custom `AccessDeniedHandler` for logging.

### 5. Tests

- Use `@WithMockUser` in tests to verify access rules.

---

<a name="e04"></a>
## E04 â Time & Attendance (Clock In/Out)

### 1. Overview

**Description:**  
Endpoints for clock-in/out, geofence/device capture, shift association, missed punch correction workflow.

### 2. Entity Design

**AttendanceEvent:**
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
    private boolean correction;
    // getters/setters
}
```

### 3. Service Layer

**AttendanceService:**
```java
public interface AttendanceService {
    AttendanceEventDTO clockIn(ClockEventRequest req);
    AttendanceEventDTO clockOut(ClockEventRequest req);
    List<AttendanceEventDTO> getDailyTotals(Long employeeId, LocalDate date);
    CorrectionTaskDTO requestCorrection(CorrectionRequest req);
}
```

### 4. Controller

```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public AttendanceEventDTO clockIn(@Valid @RequestBody ClockEventRequest req) {...}

    @PostMapping("/clock-out")
    public AttendanceEventDTO clockOut(@Valid @RequestBody ClockEventRequest req) {...}

    @PostMapping("/corrections")
    public CorrectionTaskDTO requestCorrection(@Valid @RequestBody CorrectionRequest req) {...}
}
```

### 5. Integration

- Export attendance as CSV.
- Geofence validation via external service (optional).

---

<a name="e05"></a>
## E05 â Shift & Schedule Management

### 1. Overview

**Description:**  
Manage shift templates, rotations, overtime rules, assignments, blackout dates.

### 2. Entity Design

**ShiftTemplate:**
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    // ...
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
    private ShiftTemplate shiftTemplate;
    private LocalDate date;
    // ...
}
```

### 3. Service Layer

- Detect/prevent conflicts.
- Bulk assignment for supervisors.

### 4. Controller

```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates")
    public ShiftTemplateDTO createTemplate(@Valid @RequestBody ShiftTemplateDTO dto) {...}

    @PostMapping("/assignments/bulk")
    public void bulkAssign(@RequestBody BulkAssignmentRequest req) {...}
}
```

### 5. Audit

- Generate audit entries for assignments.

---

<a name="e06"></a>
## E06 â Leave & Absence Management

### 1. Overview

**Description:**  
Request/approve PTO, sick, unpaid leave; accruals; integration with scheduling/payroll.

### 2. Entity Design

**LeaveRequest:**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LeaveType type; // PTO, SICK, UNPAID
    @Enumerated(EnumType.STRING)
    private LeaveStatus status; // REQUESTED, APPROVED, DENIED
    // ...
}
```

### 3. Service Layer

- Update balances, auto-flag shifts for coverage.

### 4. Controller

```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping
    public LeaveRequestDTO requestLeave(@Valid @RequestBody LeaveRequestDTO dto) {...}

    @PostMapping("/{id}/approve")
    public void approve(@PathVariable Long id) {...}
}
```

---

<a name="e07"></a>
## E07 â Training & Certification Tracking

### 1. Overview

**Description:**  
Track certifications, expirations, renewals, proof uploads, block assignments if expired.

### 2. Entity Design

**Certification:**
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
    private String documentUrl;
    // ...
}
```

### 3. Service Layer

- Alerts for expiring certs.
- Block unqualified assignments.

---

<a name="e08"></a>
## E08 â Safety Incidents & OSHA Reporting

### 1. Overview

**Description:**  
Record incidents, workflow for investigation, OSHA summary export.

### 2. Entity Design

**SafetyIncident:**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String description;
    private String location;
    private Severity severity;
    @ManyToMany
    private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status; // OPEN, INVESTIGATING, RESOLVED
    // ...
}
```

### 3. Controller

```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
    @PostMapping
    public SafetyIncidentDTO create(@Valid @RequestBody SafetyIncidentDTO dto) {...}
}
```

---

<a name="e09"></a>
## E09 â Equipment & Asset Assignment

### 1. Overview

**Description:**  
Assign assets to employees, track check-in/out, block if certs missing.

### 2. Entity Design

**Asset:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private AssetCondition condition;
    // ...
}
```

**AssetAssignment:**
```java
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
    // ...
}
```

---

<a name="e10"></a>
## E10 â Performance Reviews & Goals

### 1. Overview

**Description:**  
Review templates, goals, ratings, supervisor/employee acknowledgements.

### 2. Entity Design

**PerformanceReview:**
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
    private String ratings;
    private String comments;
    private boolean acknowledgedByEmployee;
    private boolean acknowledgedBySupervisor;
    // ...
}
```

---

<a name="e11"></a>
## E11 â Payroll Export Integration

### 1. Overview

**Description:**  
Generate payroll files from attendance/leave, map to provider schema, secure delivery.

### 2. Integration

- SFTP/API delivery via Spring Integration.
- Retry with exponential backoff.

**PayrollExportService:**
```java
public interface PayrollExportService {
    void exportPayroll(LocalDate periodStart, LocalDate periodEnd);
}
```

---

<a name="e12"></a>
## E12 â Notifications & Announcements

### 1. Overview

**Description:**  
In-app/email/SMS notifications, quiet hours, opt-in/out, templates.

### 2. Entity Design

**Notification:**
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private String channel; // EMAIL, SMS, IN_APP
    private String template;
    private String recipient;
    private boolean delivered;
    private LocalDateTime sentAt;
    // ...
}
```

---

<a name="e13"></a>
## E13 â Integration Layer (HRIS/WMS APIs)

### 1. Overview

**Description:**  
Expose REST APIs/connectors for HRIS, WMS, IDP; webhooks.

### 2. Security

- JWT/OAuth2 for APIs.

**IntegrationController:**
```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    @PostMapping("/hris/employees")
    public void syncEmployee(@RequestBody EmployeeSyncDTO dto) {...}
}
```

---

<a name="e14"></a>
## E14 â Audit Trail & Compliance

### 1. Overview

**Description:**  
Centralized audit logging for sensitive changes, immutable log table.

### 2. Entity Design

**AuditLog:**
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
    @Lob
    private String before;
    @Lob
    private String after;
    // ...
}
```

---

<a name="e15"></a>
## E15 â Reporting & Analytics

### 1. Overview

**Description:**  
Operational reports, CSV/PDF export, dashboards.

### 2. ReportingService

```java
public interface ReportingService {
    ReportDTO generateAttendanceReport(ReportFilter filter);
    // ...
}
```

---

<a name="e16"></a>
## E16 â Mobile Access (PWA)

### 1. Overview

**Description:**  
Responsive views for clock-in/out, schedules, leave, announcements; offline PWA.

### 2. Configuration

- Add `manifest.json`, service worker.
- Use Spring Boot with Thymeleaf or expose REST APIs for PWA frontend.

---

<a name="e17"></a>
## E17 â Onboarding & Offboarding Workflow

### 1. Overview

**Description:**  
Automate provisioning, initial schedule, training, deprovisioning.

### 2. WorkflowService

```java
public interface OnboardingService {
    void onboardEmployee(EmployeeDTO dto);
    void offboardEmployee(Long employeeId);
}
```

---

# General Best Practices

- Use DTOs for all API contracts.
- Bean Validation (`@Valid`, `@NotNull`, etc.) on DTOs.
- Exception handling via `@ControllerAdvice`.
- Spring Security for all endpoints.
- Flyway/Liquibase for DB migrations.
- OpenAPI/Swagger annotations for all APIs.
- Dependency injection via `@Autowired` or constructor injection.
- RESTful API design (`/resource`, `/resource/{id}`).
- Tests for all layers (unit, integration, security).

---

**This document provides a comprehensive, ready-to-implement low-level technical design for all 17 epics of the Warehouse Employee Management System, following Spring Boot industry standards.**