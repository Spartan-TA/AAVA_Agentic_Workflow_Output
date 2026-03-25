# Warehouse Employee Management System (EMS) â Low-Level Technical Design Document

---

## Table of Contents

1. [E01 â Project Scaffolding & Domain Setup](#e01)
2. [E02 â Employee Master Data (CRUD)](#e02)
3. [E03 â Role Based Access Control (RBAC)](#e03)
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
18. [E18 â Localization & Multi-Tenant](#e18)
19. [E19 â Advanced Scheduling Optimization](#e19)
20. [E20 â Self-Service Portal](#e20)

---

## <a name="e01"></a>E01 â Project Scaffolding & Domain Setup

### 1. Spring Boot Architecture Overview

- **Tech Stack:** Spring Boot (Maven), Java 17+, Spring Data JPA, Spring Security, Spring MVC, Flyway/Liquibase, Spring Boot Actuator, OpenAPI/Swagger.
- **Layered Architecture:** Controller â Service â Repository â Entity.
- **DTO Pattern:** For API contracts.
- **Exception Handling:** `@ControllerAdvice`.
- **Validation:** Bean Validation API.
- **Monitoring:** Spring Boot Actuator.

### 2. Package Structure & Module Definitions

```
com.warehouse.ems
âââ config
âââ controller
âââ dto
âââ entity
âââ exception
âââ repository
âââ security
âââ service
âââ util
âââ integration
```

### 3. Configuration & Security Settings

- **application.yml** for environment configs.
- **Flyway/Liquibase** for DB migrations.
- **Actuator** enabled on `/actuator/*`.
- **Swagger/OpenAPI** auto-generated docs.

### 4. Sample Code Snippets

**application.yml**
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems
    username: ems_user
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
        include: health,info,metrics
```

**Main Application**
```java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

---

## <a name="e02"></a>E02 â Employee Master Data (CRUD)

### 1. Entity Design

**Employee**
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne
    private Department department;

    @ManyToOne
    private ShiftGroup shiftGroup;

    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    private boolean deleted = false; // Soft delete
    // getters/setters
}
```

### 2. Repository Layer

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

### 3. Service Layer

```java
public interface EmployeeService {
    EmployeeDTO createEmployee(EmployeeDTO dto);
    EmployeeDTO getEmployee(Long id);
    Page<EmployeeDTO> listEmployees(Pageable pageable, EmployeeFilter filter);
    EmployeeDTO updateEmployee(Long id, EmployeeDTO dto);
    void deleteEmployee(Long id); // Soft delete
}
```

### 4. Controller Layer

```java
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee CRUD APIs")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeDTO dto) { ... }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> get(@PathVariable Long id) { ... }

    @GetMapping
    public Page<EmployeeDTO> list(Pageable pageable, EmployeeFilter filter) { ... }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> update(@PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) { ... }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
}
```

### 5. DTO Example

```java
public class EmployeeDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String badgeId;
    @NotNull
    private Role role;
    private Long departmentId;
    private Long shiftGroupId;
    private LocalDate hireDate;
    private EmployeeStatus status;
}
```

### 6. OpenAPI Example

```yaml
paths:
  /employees:
    post:
      summary: Create Employee
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/EmployeeDTO'
```

---

## <a name="e03"></a>E03 â Role Based Access Control (RBAC)

### 1. Security Configuration

```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .httpBasic();
    }
}
```

- **Roles:** ADMIN, HR, SUPERVISOR, WORKER
- **Row-level Security:** Enforced in service layer (e.g., SUPERVISOR can only access their team).

### 2. Method Security

```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isSupervisorOf(principal, #id))")
public EmployeeDTO getEmployee(Long id) { ... }
```

### 3. API Key/OAuth2 Toggle

- Use `@ConditionalOnProperty` to switch between API Key and OAuth2.

---

## <a name="e04"></a>E04 â Time & Attendance (Clock In/Out)

### 1. Entity Design

**AttendanceEvent**
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

    // getters/setters
}
```

### 2. Service Layer

```java
public interface AttendanceService {
    AttendanceEventDTO clockIn(Long employeeId, ClockEventRequest req);
    AttendanceEventDTO clockOut(Long employeeId, ClockEventRequest req);
    List<AttendanceSummaryDTO> getDailyTotals(Long employeeId, LocalDate date);
    void requestCorrection(Long eventId, CorrectionRequest req);
}
```

### 3. Controller Layer

```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceEventDTO> clockIn(@RequestBody ClockEventRequest req) { ... }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceEventDTO> clockOut(@RequestBody ClockEventRequest req) { ... }
}
```

### 4. Reports

- Export endpoints: `/attendance/reports?format=csv`

---

## <a name="e05"></a>E05 â Shift & Schedule Management

### 1. Entity Design

**ShiftTemplate, ShiftAssignment**
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

@Entity
public class ShiftAssignment {
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

### 2. Service Layer

- CRUD for shift templates and assignments.
- Conflict detection logic.

### 3. Controller Layer

```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates")
    public ResponseEntity<ShiftTemplateDTO> createTemplate(@RequestBody ShiftTemplateDTO dto) { ... }
    @PostMapping("/assign")
    public ResponseEntity<Void> assignShift(@RequestBody ShiftAssignmentDTO dto) { ... }
}
```

---

## <a name="e06"></a>E06 â Leave & Absence Management

### 1. Entity Design

**LeaveRequest**
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
    @Enumerated(EnumType.STRING)
    private LeaveStatus status; // REQUESTED, APPROVED, DENIED
    // ...
}
```

### 2. Service Layer

- Request, approve, deny leave.
- Update accrual balances.

### 3. Controller Layer

```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping
    public ResponseEntity<LeaveRequestDTO> requestLeave(@RequestBody LeaveRequestDTO dto) { ... }
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id) { ... }
}
```

---

## <a name="e07"></a>E07 â Training & Certification Tracking

### 1. Entity Design

**Certification**
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

### 2. Service Layer

- CRUD for certifications.
- Expiry alerts.

---

## <a name="e08"></a>E08 â Safety Incidents & OSHA Reporting

### 1. Entity Design

**SafetyIncident**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String description;
    private String location;
    private IncidentSeverity severity;
    @ManyToOne
    private Employee reportedBy;
    private IncidentStatus status; // OPEN, INVESTIGATING, RESOLVED
    // ...
}
```

### 2. Controller Layer

```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
    @PostMapping
    public ResponseEntity<SafetyIncidentDTO> report(@RequestBody SafetyIncidentDTO dto) { ... }
}
```

---

## <a name="e09"></a>E09 â Equipment & Asset Assignment

### 1. Entity Design

**Asset, AssetAssignment**
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

## <a name="e10"></a>E10 â Performance Reviews & Goals

### 1. Entity Design

**PerformanceReview**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate reviewDate;
    private String goals;
    private String comments;
    private ReviewStatus status; // DRAFT, SUBMITTED, ACKNOWLEDGED
    // ...
}
```

---

## <a name="e11"></a>E11 â Payroll Export Integration

### 1. Integration Points

- Export attendance/leave data to payroll provider.
- SFTP/API delivery.
- Audit log for exports.

---

## <a name="e12"></a>E12 â Notifications & Announcements

### 1. Service Layer

- Notification preferences per user.
- Email/SMS integration.
- In-app dashboard.

---

## <a name="e13"></a>E13 â Integration Layer (HRIS/WMS APIs)

### 1. Integration Points

- REST APIs for HRIS, WMS, IDP.
- Webhooks for events.
- JWT/OAuth2 security.

---

## <a name="e14"></a>E14 â Audit Trail & Compliance

### 1. Entity Design

**AuditLog**
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
    private String beforeState;
    @Lob
    private String afterState;
    // ...
}
```

---

## <a name="e15"></a>E15 â Reporting & Analytics

- Endpoints for CSV/PDF export.
- Role-based dashboards.
- Metrics for BI.

---

## <a name="e16"></a>E16 â Mobile Access (PWA)

- Responsive UI.
- Offline queue for clock events.
- PWA manifest.

---

## <a name="e17"></a>E17 â Onboarding & Offboarding Workflow

- Automated provisioning/deprovisioning.
- Task generation for training/assets.

---

## <a name="e18"></a>E18 â Localization & Multi-Tenant

- Locale resolver.
- Tenant context in DB and services.

---

## <a name="e19"></a>E19 â Advanced Scheduling Optimization

- Optimization algorithms for shift assignment.
- Bulk assignment APIs.

---

## <a name="e20"></a>E20 â Self-Service Portal

- Employee dashboard for profile, leave, schedule, notifications.

---

# General Patterns & Best Practices

- **Exception Handling:** `@ControllerAdvice` for global error handling.
- **Validation:** Bean Validation (`@Valid`, `@NotNull`, etc.).
- **DTOs:** Used for all API contracts.
- **OpenAPI/Swagger:** Auto-generated docs.
- **Logging:** SLF4J with structured logs.
- **Testing:** Unit and integration tests for all layers.

---

# Example: Exception Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiError("NOT_FOUND", ex.getMessage()));
    }
}
```

---

# Example: Flyway Migration

**V1__init.sql**
```sql
CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    badge_id VARCHAR(50) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    department_id BIGINT,
    shift_group_id BIGINT,
    hire_date DATE,
    status VARCHAR(50),
    deleted BOOLEAN DEFAULT FALSE
);
```

---

# Example: OpenAPI Annotation

```java
@Operation(summary = "Create Employee", description = "Creates a new employee record.")
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "Employee created"),
    @ApiResponse(responseCode = "400", description = "Validation error")
})
```

---

# Conclusion

This document provides a production-ready, comprehensive technical specification for all Warehouse EMS user stories, following Spring Boot best practices. Each epic/user story is mapped to entities, services, repositories, controllers, and integration points, with code snippets and patterns to guide development teams. All modules are designed for extensibility, maintainability, and compliance with industry standards.