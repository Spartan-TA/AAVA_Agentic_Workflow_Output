# Warehouse Employee Management System (EMS)
# Technical Design Document

---

## Table of Contents
1. [System Architecture Overview](#system-architecture-overview)
2. [Epic-by-Epic Technical Design](#epic-by-epic-technical-design)
    - [E01: Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
    - [E02: Employee Master Data (CRUD)](#e02-employee-master-data-crud)
    - [E03: Role Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
    - [E04: Time & Attendance (Clock InOut)](#e04-time--attendance-clock-inout)
    - [E05: Shift & Schedule Management](#e05-shift--schedule-management)
    - [E06: Leave & Absence Management](#e06-leave--absence-management)
    - [E07: Training & Certification Tracking](#e07-training--certification-tracking)
    - [E08: Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
    - [E09: Equipment & Asset Assignment](#e09-equipment--asset-assignment)
    - [E10: Performance Reviews & Goals](#e10-performance-reviews--goals)
    - [E11: Payroll Export Integration](#e11-payroll-export-integration)
    - [E12: Notifications & Announcements](#e12-notifications--announcements)
    - [E13: Integration Layer (HRISWMS APIs)](#e13-integration-layer-hriswms-apis)
    - [E14: Audit Trail & Compliance](#e14-audit-trail--compliance)
    - [E15: Reporting & Analytics](#e15-reporting--analytics)
    - [E16: Mobile Access (PWA)](#e16-mobile-access-pwa)
    - [E17: Onboarding & Offboarding Workflow](#e17-onboarding--offboarding-workflow)
    - [E18: Localization & Multi-Tenant](#e18-localization--multi-tenant)
    - [E19: Advanced Scheduling Optimization](#e19-advanced-scheduling-optimization)
    - [E20: Continuous Improvement & Feedback](#e20-continuous-improvement--feedback)
3. [Cross-Cutting Concerns](#cross-cutting-concerns)
4. [Deployment & Operations](#deployment--operations)

---

## System Architecture Overview

### Application Architecture
- **Spring Boot 3.x (Java 17+)**
- **Monolithic-first** approach with modular package structure (recommended for initial delivery, can be decomposed to microservices as scale/needs dictate)
- **PostgreSQL** as primary RDBMS
- **Redis** for caching and distributed locks
- **Flyway** for DB migrations
- **Spring Security** (OAuth2, JWT)
- **OpenAPI/Swagger** for API documentation
- **Actuator** for monitoring
- **Docker** for containerization

#### High-Level Diagram
```
[Client (Web/PWA/Mobile)] <-> [Spring Boot EMS API Layer]
    |-- [Security Layer (JWT/OAuth2)]
    |-- [Domain Modules: employee, attendance, scheduling, safety, ...]
    |-- [Integration Layer: HRIS, WMS, Payroll]
    |-- [Persistence: JPA (PostgreSQL), Redis]
    |-- [Cross-cutting: Audit, Notification, Reporting]
```

### Technology Stack
| Layer                | Technology                |
|----------------------|--------------------------|
| Language             | Java 17+                 |
| Framework            | Spring Boot 3.x          |
| ORM                  | Spring Data JPA (Hibernate) |
| DB                   | PostgreSQL               |
| Cache                | Redis                    |
| Security             | Spring Security, OAuth2, JWT |
| API Doc              | OpenAPI/Swagger          |
| Migration            | Flyway                   |
| Containerization     | Docker                   |
| Testing              | JUnit 5, Mockito, TestContainers |

### Security Architecture
- **Spring Security** for authentication/authorization
- **OAuth2/JWT** for stateless API security
- **Role-based access control** (ADMIN, HR, SUPERVISOR, WORKER)
- **Method-level security** (`@PreAuthorize`, `@Secured`)
- **Row-level security** for sensitive data
- **API Key/OAuth2 toggle** via config for integrations

---

## Epic-by-Epic Technical Design

### E01: Project Scaffolding & Domain Setup

#### Domain Model Design
- No business entities, but foundational base packages and configuration classes.

#### Package Structure
```
com.warehouse.ems
    âââ employee
    âââ attendance
    âââ scheduling
    âââ safety
    âââ equipment
    âââ performance
    âââ payroll
    âââ notification
    âââ integration
    âââ audit
    âââ reporting
    âââ mobile
    âââ onboarding
    âââ localization
    âââ optimization
    âââ feedback
    âââ config
    âââ security
    âââ common
```
Each module contains:
- `domain` (entities, enums)
- `repository` (JPA interfaces)
- `service` (interfaces, impl)
- `controller` (REST endpoints)
- `dto` (request/response objects)
- `config` (module configs)

#### Repository Layer
- N/A (scaffolding)

#### Service Layer
- N/A (scaffolding)

#### Controller Layer
- N/A (scaffolding)

#### Security Configuration
- Initial security config in `com.warehouse.ems.security`

#### Integration Points
- N/A

#### Code Samples
**Base Application Class:**
```java
@SpringBootApplication
public class EmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmsApplication.class, args);
    }
}
```

**Flyway Config:**
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

---

### E02: Employee Master Data (CRUD)

#### Domain Model Design
**Employee Entity:**
```java
@Entity
@Table(name = "employees")
@EntityListeners(AuditingEntityListener.class)
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank
    private String badgeId;

    @Column(nullable = false)
    @NotBlank
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_group_id")
    private ShiftGroup shiftGroup;

    @Column(nullable = false)
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status;

    @Column(nullable = false)
    private boolean deleted = false;

    @CreatedBy
    private String createdBy;
    @CreatedDate
    private Instant createdDate;
    @LastModifiedBy
    private String lastModifiedBy;
    @LastModifiedDate
    private Instant lastModifiedDate;
}
```

#### Package Structure
- `com.warehouse.ems.employee.domain.Employee`
- `com.warehouse.ems.employee.domain.Department`
- `com.warehouse.ems.employee.domain.ShiftGroup`
- `com.warehouse.ems.employee.repository.EmployeeRepository`
- `com.warehouse.ems.employee.service.EmployeeService`
- `com.warehouse.ems.employee.controller.EmployeeController`
- `com.warehouse.ems.employee.dto.EmployeeDto`

#### Repository Layer
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    @Query("SELECT e FROM Employee e WHERE e.name LIKE %:name% AND e.deleted = false")
    Page<Employee> searchByName(@Param("name") String name, Pageable pageable);
}
```

#### Service Layer
```java
public interface EmployeeService {
    EmployeeDto create(EmployeeDto dto);
    EmployeeDto update(Long id, EmployeeDto dto);
    void delete(Long id);
    EmployeeDto get(Long id);
    Page<EmployeeDto> list(Pageable pageable, String filter);
}

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    // ...implementation using repository and MapStruct mapper
}
```

#### Controller Layer
```java
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee API")
public class EmployeeController {
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { ... }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> get(@PathVariable Long id) { ... }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) { ... }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { ... }

    @GetMapping
    public Page<EmployeeDto> list(@PageableDefault Pageable pageable, @RequestParam(required = false) String filter) { ... }
}
```

#### Security Configuration
- `@PreAuthorize` on endpoints
- Only ADMIN/HR can create/update/delete
- Row-level security for SUPERVISOR (see only their team)

#### Integration Points
- HRIS sync (see E13)

#### Code Samples
**DTO Example:**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

---

### E03: Role Based Access Control (RBAC)

#### Domain Model Design
**Role Enum:**
```java
public enum EmployeeRole {
    ADMIN, HR, SUPERVISOR, WORKER
}
```

#### Package Structure
- `com.warehouse.ems.security`
- `com.warehouse.ems.employee.domain.EmployeeRole`

#### Repository Layer
- N/A (roles as enum)

#### Service Layer
- UserDetailsService implementation
- Role/permission checks

#### Controller Layer
- Security endpoints (login, token refresh)

#### Security Configuration
```java
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests()
                .antMatchers("/auth/**").permitAll()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer().jwt();
        return http.build();
    }
}
```

#### Integration Points
- SSO/IDP (see E13)

#### Code Samples
**JWT Auth Filter:**
```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // ...extracts and validates JWT, sets SecurityContext
}
```

---

### E04: Time & Attendance (Clock In/Out)

#### Domain Model Design
**AttendanceEvent Entity:**
```java
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Employee employee;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    @Enumerated(EnumType.STRING)
    private AttendanceType type; // CLOCK_IN, CLOCK_OUT

    private String deviceId;
    private String geoLocation;
    private boolean correction;
    private String correctionReason;
    // Audit fields ...
}
```

#### Package Structure
- `com.warehouse.ems.attendance.domain.AttendanceEvent`
- `com.warehouse.ems.attendance.repository.AttendanceEventRepository`
- `com.warehouse.ems.attendance.service.AttendanceService`
- `com.warehouse.ems.attendance.controller.AttendanceController`
- `com.warehouse.ems.attendance.dto.AttendanceEventDto`

#### Repository Layer
```java
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeAndEventTimeBetween(Employee e, LocalDateTime start, LocalDateTime end);
}
```

#### Service Layer
- Clock-in/out logic
- Shift association
- Correction workflow
- Transactional

#### Controller Layer
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceEventDto> clockIn(@RequestBody ClockEventRequest req) { ... }
    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceEventDto> clockOut(@RequestBody ClockEventRequest req) { ... }
}
```

#### Security Configuration
- Only authenticated employees can clock in/out
- Supervisors/HR can approve corrections

#### Integration Points
- Payroll (E11)
- Reporting (E15)

#### Code Samples
**DTO Example:**
```java
@Data
public class ClockEventRequest {
    private String deviceId;
    private String geoLocation;
}
```

---

### E05: Shift & Schedule Management

#### Domain Model Design
**ShiftTemplate, ShiftAssignment Entities:**
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

#### Package Structure
- `com.warehouse.ems.scheduling.domain.ShiftTemplate`
- `com.warehouse.ems.scheduling.domain.ShiftAssignment`
- ...

#### Repository Layer
- Find assignments by employee/date
- Detect conflicts

#### Service Layer
- Bulk assignment
- Conflict detection
- Overtime rules

#### Controller Layer
- CRUD for templates/assignments
- Bulk assign endpoint

#### Security Configuration
- Only ADMIN/HR/SUPERVISOR can assign
- Workers see own schedule

#### Integration Points
- Attendance, Reporting

#### Code Samples
**Bulk Assignment:**
```java
@PostMapping("/assignments/bulk")
public ResponseEntity<Void> bulkAssign(@RequestBody BulkAssignRequest req) { ... }
```

---

### E06: Leave & Absence Management

#### Domain Model Design
**LeaveRequest Entity:**
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
    private String reason;
    // ...
}
```

#### Package Structure
- `com.warehouse.ems.leave.domain.LeaveRequest`
- ...

#### Repository Layer
- Find by employee/date/status

#### Service Layer
- Accrual logic
- Approval workflow

#### Controller Layer
- Request/approve endpoints

#### Security Configuration
- Employees request, supervisors approve

#### Integration Points
- Scheduling, Payroll

#### Code Samples
**Approve Leave:**
```java
@PostMapping("/leave/{id}/approve")
@PreAuthorize("hasRole('SUPERVISOR')")
public ResponseEntity<Void> approve(@PathVariable Long id) { ... }
```

---

### E07: Training & Certification Tracking

#### Domain Model Design
**Certification Entity:**
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

#### Package Structure
- `com.warehouse.ems.training.domain.Certification`
- ...

#### Repository Layer
- Find expiring certs

#### Service Layer
- Expiry alerts
- Assignment checks

#### Controller Layer
- CRUD endpoints

#### Security Configuration
- Only HR/ADMIN can edit
- Employees view own

#### Integration Points
- Scheduling, Equipment

#### Code Samples
**Expiry Alert:**
```java
@Scheduled(cron = "0 0 8 * * ?")
public void sendExpiryAlerts() { ... }
```

---

### E08: Safety Incidents & OSHA Reporting

#### Domain Model Design
**SafetyIncident Entity:**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String description;
    private String location;
    @Enumerated(EnumType.STRING)
    private IncidentSeverity severity;
    @ManyToMany
    private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
    // ...
}
```

#### Package Structure
- `com.warehouse.ems.safety.domain.SafetyIncident`
- ...

#### Repository Layer
- Find by status/date

#### Service Layer
- Workflow (Open, Investigating, Resolved)
- OSHA export

#### Controller Layer
- CRUD, export endpoints

#### Security Configuration
- Only HR/ADMIN can edit
- Supervisors report

#### Integration Points
- Reporting

#### Code Samples
**Export OSHA:**
```java
@GetMapping("/safety/osha/export")
public ResponseEntity<Resource> exportOsha() { ... }
```

---

### E09: Equipment & Asset Assignment

#### Domain Model Design
**Asset, AssetAssignment Entities:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private String condition;
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
    private LocalDateTime assignedAt;
    private LocalDateTime returnedAt;
    // ...
}
```

#### Package Structure
- `com.warehouse.ems.equipment.domain.Asset`
- ...

#### Repository Layer
- Find overdue returns

#### Service Layer
- Check-in/out logic
- Certification checks

#### Controller Layer
- Assign/return endpoints

#### Security Configuration
- Only qualified employees

#### Integration Points
- Training, Reporting

#### Code Samples
**Check-out Asset:**
```java
@PostMapping("/assets/{id}/checkout")
public ResponseEntity<Void> checkout(@PathVariable Long id, @RequestBody CheckoutRequest req) { ... }
```

---

### E10: Performance Reviews & Goals

#### Domain Model Design
**PerformanceReview Entity:**
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

#### Package Structure
- `com.warehouse.ems.performance.domain.PerformanceReview`
- ...

#### Repository Layer
- Find by employee/date

#### Service Layer
- Review cycle logic

#### Controller Layer
- CRUD, acknowledge endpoints

#### Security Configuration
- Role-based visibility

#### Integration Points
- Reporting

#### Code Samples
**Acknowledge Review:**
```java
@PostMapping("/reviews/{id}/acknowledge")
public ResponseEntity<Void> acknowledge(@PathVariable Long id) { ... }
```

---

### E11: Payroll Export Integration

#### Domain Model Design
- No new entities; uses attendance/leave

#### Package Structure
- `com.warehouse.ems.payroll`

#### Repository Layer
- Attendance/leave queries

#### Service Layer
- Export logic
- SFTP/API delivery

#### Controller Layer
- Export endpoints

#### Security Configuration
- Only ADMIN/HR

#### Integration Points
- External payroll provider

#### Code Samples
**Export Payroll:**
```java
@PostMapping("/payroll/export")
public ResponseEntity<Resource> exportPayroll(@RequestBody ExportRequest req) { ... }
```

---

### E12: Notifications & Announcements

#### Domain Model Design
**Notification Entity:**
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private String channel; // EMAIL, SMS, IN_APP
    private String template;
    private String recipient;
    private String status;
    private LocalDateTime sentAt;
    // ...
}
```

#### Package Structure
- `com.warehouse.ems.notification.domain.Notification`
- ...

#### Repository Layer
- Find by recipient/status

#### Service Layer
- Delivery logic
- Rate limiting

#### Controller Layer
- Opt-in/out endpoints
- Announcement endpoints

#### Security Configuration
- Role-based

#### Integration Points
- Email/SMS providers

#### Code Samples
**Send Notification:**
```java
public void sendNotification(NotificationDto dto) { ... }
```

---

### E13: Integration Layer (HRIS/WMS APIs)

#### Domain Model Design
- N/A (uses employee, department, etc.)

#### Package Structure
- `com.warehouse.ems.integration`

#### Repository Layer
- N/A

#### Service Layer
- HRIS sync jobs
- Webhook handlers

#### Controller Layer
- API endpoints for HRIS/WMS

#### Security Configuration
- JWT/OAuth2
- API key toggle

#### Integration Points
- HRIS, WMS, IDP

#### Code Samples
**Webhook Handler:**
```java
@PostMapping("/integration/hris/webhook")
public ResponseEntity<Void> handleHrisWebhook(@RequestBody HrisEventDto dto) { ... }
```

---

### E14: Audit Trail & Compliance

#### Domain Model Design
**AuditLog Entity:**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private Long entityId;
    private String action;
    private String actor;
    private Instant timestamp;
    @Lob
    private String before;
    @Lob
    private String after;
}
```

#### Package Structure
- `com.warehouse.ems.audit.domain.AuditLog`
- ...

#### Repository Layer
- Find by entity/date/actor

#### Service Layer
- Audit logging logic

#### Controller Layer
- Export endpoints

#### Security Configuration
- ADMIN/HR only

#### Integration Points
- All modules

#### Code Samples
**Audit Aspect:**
```java
@Aspect
@Component
public class AuditAspect {
    @AfterReturning(...)
    public void logChange(...) { ... }
}
```

---

### E15: Reporting & Analytics

#### Domain Model Design
- Uses attendance, leave, certification, safety, etc.

#### Package Structure
- `com.warehouse.ems.reporting`

#### Repository Layer
- Aggregation queries

#### Service Layer
- Report generation

#### Controller Layer
- Export endpoints
- Metrics endpoints

#### Security Configuration
- Role-based

#### Integration Points
- BI tools

#### Code Samples
**Export Report:**
```java
@GetMapping("/reports/attendance")
public ResponseEntity<Resource> exportAttendance(@RequestParam ...) { ... }
```

---

### E16: Mobile Access (PWA)

#### Domain Model Design
- N/A (uses existing entities)

#### Package Structure
- `com.warehouse.ems.mobile`

#### Repository Layer
- N/A

#### Service Layer
- Mobile-specific logic

#### Controller Layer
- Mobile endpoints

#### Security Configuration
- JWT

#### Integration Points
- PWA manifest, offline queue

#### Code Samples
**PWA Manifest:**
```json
{
  "short_name": "EMS",
  "name": "Warehouse EMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

---

### E17: Onboarding & Offboarding Workflow

#### Domain Model Design
**OnboardingTask, OffboardingTask Entities:**
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String description;
    private boolean completed;
    // ...
}
```

#### Package Structure
- `com.warehouse.ems.onboarding.domain.OnboardingTask`
- ...

#### Repository Layer
- Find by employee/status

#### Service Layer
- Task generation logic

#### Controller Layer
- Workflow endpoints

#### Security Configuration
- HR/ADMIN

#### Integration Points
- HRIS, Training, Equipment

#### Code Samples
**Create Onboarding Tasks:**
```java
public void createOnboardingTasks(Employee employee) { ... }
```

---

### E18: Localization & Multi-Tenant

#### Domain Model Design
**Tenant Entity:**
```java
@Entity
public class Tenant {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String locale;
    // ...
}
```

#### Package Structure
- `com.warehouse.ems.localization.domain.Tenant`
- ...

#### Repository Layer
- Find by name

#### Service Layer
- Tenant context
- Locale resolution

#### Controller Layer
- Tenant management endpoints

#### Security Configuration
- Tenant isolation

#### Integration Points
- All modules

#### Code Samples
**Locale Resolver:**
```java
@Bean
public LocaleResolver localeResolver() {
    SessionLocaleResolver slr = new SessionLocaleResolver();
    slr.setDefaultLocale(Locale.US);
    return slr;
}
```

---

### E19: Advanced Scheduling Optimization

#### Domain Model Design
- Uses shift, employee, leave

#### Package Structure
- `com.warehouse.ems.optimization`

#### Repository Layer
- N/A

#### Service Layer
- Optimization algorithms (e.g., constraint solver)

#### Controller Layer
- Optimization endpoints

#### Security Configuration
- ADMIN/HR

#### Integration Points
- Scheduling

#### Code Samples
**Optimize Schedule:**
```java
@PostMapping("/scheduling/optimize")
public ResponseEntity<ScheduleDto> optimize(@RequestBody OptimizeRequest req) { ... }
```

---

### E20: Continuous Improvement & Feedback

#### Domain Model Design
**Feedback Entity:**
```java
@Entity
public class Feedback {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String message;
    private LocalDateTime submittedAt;
    // ...
}
```

#### Package Structure
- `com.warehouse.ems.feedback.domain.Feedback`
- ...

#### Repository Layer
- Find by employee/date

#### Service Layer
- Feedback processing

#### Controller Layer
- Submit/view endpoints

#### Security Configuration
- Role-based

#### Integration Points
- Reporting

#### Code Samples
**Submit Feedback:**
```java
@PostMapping("/feedback")
public ResponseEntity<Void> submit(@RequestBody FeedbackDto dto) { ... }
```

---

## Cross-Cutting Concerns

### Audit Trail Implementation
- Use Spring Data JPA Auditing (`@CreatedBy`, `@CreatedDate`, etc.)
- Centralized `AuditLog` entity
- Aspect for custom audit logging

### Exception Handling
- Global handler with `@ControllerAdvice`
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) { ... }
    // ...
}
```

### Logging
- SLF4J API, Logback config
- Log correlation IDs

### Caching
- Spring Cache abstraction
- Redis backend
```java
@EnableCaching
@Configuration
public class CacheConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) { ... }
}
```

### Database Migrations
- Flyway scripts in `src/main/resources/db/migration`

### Testing Strategy
- JUnit 5 for unit/integration
- Mockito for mocking
- TestContainers for DB/Redis

---

## Deployment & Operations

### Docker Containerization
- `Dockerfile` example:
```dockerfile
FROM eclipse-temurin:17-jre
COPY target/ems.jar /app/ems.jar
ENTRYPOINT ["java", "-jar", "/app/ems.jar"]
```

### Application Properties
- `application.yml` for profiles (dev, prod)
- Secrets via env vars

### Actuator Endpoints
- `/actuator/health`, `/actuator/metrics`, `/actuator/info`

### Monitoring & Observability
- Prometheus/Grafana integration
- Log aggregation (ELK/EFK)

---

# END OF DOCUMENT
