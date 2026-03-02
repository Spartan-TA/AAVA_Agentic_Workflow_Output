# Warehouse Employee Management System - Low-Level Technical Design Document

## Executive Summary

The Warehouse Employee Management System (WEMS) is a comprehensive platform designed to streamline and automate all aspects of warehouse workforce management, including employee master data, scheduling, time & attendance, compliance, asset assignment, and more. This Low-Level Technical Design Document bridges the gap between requirements and implementation, providing Spring Boot developers with clear, production-ready guidelines for each of the 79 user stories across 20 epics. It details architecture, package structure, domain models, repository/service/controller layers, configuration, and sample code for each feature, ensuring maintainability, scalability, and adherence to industry best practices.

---

## Table of Contents

1. [E01: Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
2. [E02: Employee Master Data CRUD](#e02-employee-master-data-crud)
3. [E03: Role-Based Access Control](#e03-role-based-access-control)
4. [E04: Time & Attendance](#e04-time--attendance)
5. [E05: Shift & Schedule Management](#e05-shift--schedule-management)
6. [E06: Leave & Absence Management](#e06-leave--absence-management)
7. [E07: Training & Certification Tracking](#e07-training--certification-tracking)
8. [E08: Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
9. [E09: Equipment & Asset Assignment](#e09-equipment--asset-assignment)
10. [E10: Performance Reviews & Goals](#e10-performance-reviews--goals)
11. [E11: Payroll Export Integration](#e11-payroll-export-integration)
12. [E12: Notifications & Announcements](#e12-notifications--announcements)
13. [E13: Integration Layer HRIS/WMS APIs](#e13-integration-layer-hriswms-apis)
14. [E14: Audit Trail & Compliance](#e14-audit-trail--compliance)
15. [E15: Reporting & Analytics](#e15-reporting--analytics)
16. [E16: Mobile Access PWA](#e16-mobile-access-pwa)
17. [E17: Onboarding & Offboarding Workflow](#e17-onboarding--offboarding-workflow)
18. [E18: Localization & Multi-Tenant](#e18-localization--multi-tenant)
19. [E19: Observability & Monitoring](#e19-observability--monitoring)
20. [E20: CI/CD & Deployment Automation](#e20-cicd--deployment-automation)

---

# E01: Project Scaffolding & Domain Setup (3 user stories)

## US01.01: Initialize Spring Boot Project Structure

### 1. Architecture Overview
- Layered architecture: Controller â Service â Repository â Domain
- Maven-based multi-module structure
- Base packages: `com.wems`, `com.wems.employee`, `com.wems.schedule`, etc.

### 2. Package Structure
```
com.wems
âââ config
âââ employee
âââ schedule
âââ attendance
âââ safety
âââ ...
```

### 3. Domain Model
_No domain entities for scaffolding._

### 4. Repository Layer
_No repositories for scaffolding._

### 5. Service Layer
_No services for scaffolding._

### 6. Controller Layer
_No controllers for scaffolding._

### 7. Configuration
- `application.yml` with default profiles
- Maven `pom.xml` with dependencies: Spring Boot Starter Web, Data JPA, Security, Actuator, Flyway/Liquibase

### 8. Sample Implementation

**pom.xml**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

**application.yml**
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
```

---

## US01.02: Integrate Database Migration Tool

### 1. Architecture Overview
- Use Flyway or Liquibase for DB migrations
- Versioned SQL scripts in `/db/migration`

### 2. Package Structure
```
src/main/resources/db/migration/
```

### 3. Domain Model
_No domain entities for migration tool._

### 4. Repository Layer
_No repositories for migration tool._

### 5. Service Layer
_No services for migration tool._

### 6. Controller Layer
_No controllers for migration tool._

### 7. Configuration
**application.yml**
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

### 8. Sample Implementation

**V1__init.sql**
```sql
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

## US01.03: Enable Actuator Health Endpoint

### 1. Architecture Overview
- Spring Boot Actuator exposes `/actuator/health` for readiness/liveness

### 2. Package Structure
```
com.wems.config
```

### 3. Domain Model
_No domain entities for actuator._

### 4. Repository Layer
_No repositories for actuator._

### 5. Service Layer
_No services for actuator._

### 6. Controller Layer
_No controllers for actuator._

### 7. Configuration
**application.yml**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### 8. Sample Implementation

**Health Check**
```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

---

# E02: Employee Master Data CRUD (4 user stories)

## US02.01: Create Employee API Endpoints

### 1. Architecture Overview
- RESTful CRUD endpoints for Employee entity
- DTO mapping for web layer

### 2. Package Structure
```
com.wems.employee
âââ controller
âââ domain
âââ dto
âââ repository
âââ service
```

### 3. Domain Model

**Employee.java**
```java
@Entity
@Table(name = "employee")
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "badge_id", unique = true, nullable = false)
    private String badgeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    private String department;
    private String shiftGroup;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(nullable = false)
    private String status;

    // getters and setters
}
```

### 4. Repository Layer

**EmployeeRepository.java**
```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    boolean existsByBadgeId(String badgeId);
}
```

### 5. Service Layer

**EmployeeService.java**
```java
public interface EmployeeService {
    Employee create(EmployeeDTO dto);
    Employee update(Long id, EmployeeDTO dto);
    void delete(Long id);
    Employee get(Long id);
    Page<Employee> list(Pageable pageable, EmployeeFilter filter);
}
```

**EmployeeServiceImpl.java**
```java
@Service
public class EmployeeServiceImpl implements EmployeeService {
    @Autowired
    private EmployeeRepository repo;

    @Override
    public Employee create(EmployeeDTO dto) {
        if (repo.existsByBadgeId(dto.getBadgeId())) {
            throw new DuplicateBadgeIdException();
        }
        Employee emp = new Employee();
        // set fields from dto
        return repo.save(emp);
    }
    // other methods...
}
```

### 6. Controller Layer

**EmployeeController.java**
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService service;

    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeDTO dto) {
        Employee emp = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(EmployeeDTO.from(emp));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(EmployeeDTO.from(service.get(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> update(@PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) {
        return ResponseEntity.ok(EmployeeDTO.from(service.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

**EmployeeDTO.java**
```java
public class EmployeeDTO {
    @NotBlank
    private String badgeId;
    @NotBlank
    private String name;
    @NotBlank
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @NotBlank
    private String status;
    // getters, setters, static from(Employee)
}
```

### 7. Configuration
- `@Validated` on controller
- Exception handler for duplicate badge

### 8. Sample Implementation

**DuplicateBadgeIdException.java**
```java
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateBadgeIdException extends RuntimeException {}
```

---

## US02.02: Enforce Unique Badge ID

### 1. Architecture Overview
- Unique constraint at DB and service layer

### 2. Package Structure
- As above

### 3. Domain Model
- `@Column(unique = true)` on `badgeId`

### 4. Repository Layer
- `existsByBadgeId` method

### 5. Service Layer
- Check for existing badgeId before create/update

### 6. Controller Layer
- Return 409 Conflict on duplicate

### 7. Configuration
- Exception handler for `DuplicateBadgeIdException`

### 8. Sample Implementation

**GlobalExceptionHandler.java**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateBadgeIdException.class)
    public ResponseEntity<String> handleDuplicateBadgeId() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Badge ID already exists.");
    }
}
```

---

## US02.03: Support Pagination and Filtering

### 1. Architecture Overview
- Pageable and filterable `/employees` endpoint

### 2. Package Structure
- Add `EmployeeFilter` DTO

### 3. Domain Model
- No changes

### 4. Repository Layer

**EmployeeRepository.java**
```java
Page<Employee> findAll(Specification<Employee> spec, Pageable pageable);
```

### 5. Service Layer

**EmployeeServiceImpl.java**
```java
public Page<Employee> list(Pageable pageable, EmployeeFilter filter) {
    Specification<Employee> spec = EmployeeSpecifications.fromFilter(filter);
    return repo.findAll(spec, pageable);
}
```

### 6. Controller Layer

**EmployeeController.java**
```java
@GetMapping
public Page<EmployeeDTO> list(
    @RequestParam Map<String, String> params,
    Pageable pageable
) {
    EmployeeFilter filter = EmployeeFilter.from(params);
    return service.list(pageable, filter).map(EmployeeDTO::from);
}
```

### 7. Configuration
- Register `PageableHandlerMethodArgumentResolver`

### 8. Sample Implementation

**EmployeeSpecifications.java**
```java
public class EmployeeSpecifications {
    public static Specification<Employee> fromFilter(EmployeeFilter filter) {
        // build dynamic predicates
    }
}
```

---

## US02.04: Provide OpenAPI Schemas

### 1. Architecture Overview
- Use Springdoc/OpenAPI for API documentation

### 2. Package Structure
```
com.wems.config
```

### 3. Domain Model
- Annotate DTOs with OpenAPI annotations

### 4. Repository Layer
- No changes

### 5. Service Layer
- No changes

### 6. Controller Layer
- Annotate endpoints with `@Operation`, `@Parameter`

### 7. Configuration

**OpenApiConfig.java**
```java
@Configuration
@OpenAPIDefinition(
    info = @Info(title = "WEMS API", version = "1.0", description = "Warehouse Employee Management APIs")
)
public class OpenApiConfig {}
```

### 8. Sample Implementation

**EmployeeDTO.java**
```java
@Schema(description = "Employee Data Transfer Object")
public class EmployeeDTO {
    @Schema(example = "B12345")
    private String badgeId;
    // ...
}
```

---

# E03: Role-Based Access Control (4 user stories)

## US03.01: Implement Spring Security Configuration

### 1. Architecture Overview
- Use Spring Security for authentication and authorization
- Roles: ADMIN, HR, SUPERVISOR, WORKER

### 2. Package Structure
```
com.wems.security
```

### 3. Domain Model

**User.java**
```java
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    private String username;
    private String password;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles;
    // getters/setters
}
```

### 4. Repository Layer

**UserRepository.java**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

### 5. Service Layer

**CustomUserDetailsService.java**
```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = repo.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(username));
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(), user.getPassword(),
            user.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet())
        );
    }
}
```

### 6. Controller Layer
- No direct controller; security handled globally

### 7. Configuration

**SecurityConfig.java**
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
            .antMatchers("/actuator/**").permitAll()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .anyRequest().authenticated()
            .and()
            .httpBasic();
    }
}
```

### 8. Sample Implementation

**application.yml**
```yaml
spring:
  security:
    user:
      name: admin
      password: admin
```

---

## US03.02: Define Role Hierarchy

### 1. Architecture Overview
- Role hierarchy: ADMIN > HR > SUPERVISOR > WORKER

### 2. Package Structure
- `com.wems.security`

### 3. Domain Model
- Roles as enum or constants

### 4. Repository Layer
- No changes

### 5. Service Layer
- No changes

### 6. Controller Layer
- No changes

### 7. Configuration

**SecurityConfig.java**
```java
@Bean
public RoleHierarchy roleHierarchy() {
    RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
    hierarchy.setHierarchy("ROLE_ADMIN > ROLE_HR > ROLE_SUPERVISOR > ROLE_WORKER");
    return hierarchy;
}
```

### 8. Sample Implementation

**Method Security**
```java
@PreAuthorize("hasRole('ADMIN')")
public void adminOnlyMethod() { ... }
```

---

## US03.03: Secure REST Endpoints

### 1. Architecture Overview
- Use `@PreAuthorize` on controller methods

### 2. Package Structure
- As above

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer
- No changes

### 6. Controller Layer

**EmployeeController.java**
```java
@PreAuthorize("hasAnyRole('ADMIN','HR')")
@PostMapping
public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeDTO dto) { ... }
```

### 7. Configuration
- Enable method security

**SecurityConfig.java**
```java
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter { ... }
```

### 8. Sample Implementation

**Test**
```java
@Test
@WithMockUser(roles = "WORKER")
public void testCreateEmployeeForbidden() throws Exception {
    mockMvc.perform(post("/employees")).andExpect(status().isForbidden());
}
```

---

## US03.04: Implement Row-Level Security

### 1. Architecture Overview
- Restrict data access based on user role/team

### 2. Package Structure
- `com.wems.security`

### 3. Domain Model
- Employee has `supervisorId` or `teamId`

### 4. Repository Layer

**EmployeeRepository.java**
```java
Page<Employee> findBySupervisorId(Long supervisorId, Pageable pageable);
```

### 5. Service Layer

**EmployeeServiceImpl.java**
```java
public Page<Employee> listForSupervisor(Long supervisorId, Pageable pageable) {
    return repo.findBySupervisorId(supervisorId, pageable);
}
```

### 6. Controller Layer

**EmployeeController.java**
```java
@PreAuthorize("hasRole('SUPERVISOR')")
@GetMapping("/my-team")
public Page<EmployeeDTO> myTeam(Pageable pageable, Authentication auth) {
    Long supervisorId = ...; // extract from auth
    return service.listForSupervisor(supervisorId, pageable).map(EmployeeDTO::from);
}
```

### 7. Configuration
- Security context extraction

### 8. Sample Implementation

**SecurityUtils.java**
```java
public static Long getCurrentUserId() {
    // extract from SecurityContextHolder
}
```

---

# E04: Time & Attendance (4 user stories)

## US04.01: Create Clock In/Out Endpoints

### 1. Architecture Overview
- REST endpoints for clock-in/out
- Associate with employee and shift

### 2. Package Structure
```
com.wems.attendance
âââ controller
âââ domain
âââ dto
âââ repository
âââ service
```

### 3. Domain Model

**AttendanceEvent.java**
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private EventType type; // CLOCK_IN, CLOCK_OUT

    private String deviceId;
    private String location; // geofence

    // getters/setters
}
```

### 4. Repository Layer

**AttendanceEventRepository.java**
```java
@Repository
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeAndTimestampBetween(Employee emp, LocalDateTime start, LocalDateTime end);
}
```

### 5. Service Layer

**AttendanceService.java**
```java
public interface AttendanceService {
    AttendanceEvent clockIn(Long employeeId, ClockEventDTO dto);
    AttendanceEvent clockOut(Long employeeId, ClockEventDTO dto);
}
```

**AttendanceServiceImpl.java**
```java
@Service
public class AttendanceServiceImpl implements AttendanceService {
    // ...
    @Override
    public AttendanceEvent clockIn(Long employeeId, ClockEventDTO dto) {
        // validate, save event
    }
}
```

### 6. Controller Layer

**AttendanceController.java**
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @Autowired
    private AttendanceService service;

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceEventDTO> clockIn(@RequestBody ClockEventDTO dto, Authentication auth) {
        Long employeeId = ...; // from auth
        return ResponseEntity.ok(AttendanceEventDTO.from(service.clockIn(employeeId, dto)));
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceEventDTO> clockOut(@RequestBody ClockEventDTO dto, Authentication auth) {
        Long employeeId = ...;
        return ResponseEntity.ok(AttendanceEventDTO.from(service.clockOut(employeeId, dto)));
    }
}
```

### 7. Configuration
- Timezone handling

### 8. Sample Implementation

**ClockEventDTO.java**
```java
public class ClockEventDTO {
    private String deviceId;
    private String location;
    // getters/setters
}
```

---

## US04.02: Implement Geofence Validation

### 1. Architecture Overview
- Validate clock event location against allowed geofence

### 2. Package Structure
- Add `GeofenceService`

### 3. Domain Model
- Add allowed geofences to Employee or Warehouse

### 4. Repository Layer
- No changes

### 5. Service Layer

**GeofenceService.java**
```java
public interface GeofenceService {
    boolean isWithinAllowedArea(String location, Employee employee);
}
```

**AttendanceServiceImpl.java**
```java
@Autowired
private GeofenceService geofenceService;

@Override
public AttendanceEvent clockIn(Long employeeId, ClockEventDTO dto) {
    Employee emp = ...;
    if (!geofenceService.isWithinAllowedArea(dto.getLocation(), emp)) {
        throw new GeofenceViolationException();
    }
    // ...
}
```

### 6. Controller Layer
- Return 400 on geofence violation

### 7. Configuration
- Geofence config in `application.yml`

### 8. Sample Implementation

**GeofenceViolationException.java**
```java
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class GeofenceViolationException extends RuntimeException {}
```

---

## US04.03: Calculate Hours Worked

### 1. Architecture Overview
- Compute total hours per shift/day

### 2. Package Structure
- Add `AttendanceCalculator` utility

### 3. Domain Model
- No changes

### 4. Repository Layer
- Query events by employee/date

### 5. Service Layer

**AttendanceServiceImpl.java**
```java
public Duration calculateHoursWorked(Long employeeId, LocalDate date) {
    List<AttendanceEvent> events = repo.findByEmployeeAndTimestampBetween(...);
    // pair clock-in/out, sum durations
}
```

### 6. Controller Layer

**AttendanceController.java**
```java
@GetMapping("/hours")
public ResponseEntity<HoursWorkedDTO> getHours(@RequestParam Long employeeId, @RequestParam LocalDate date) {
    Duration hours = service.calculateHoursWorked(employeeId, date);
    return ResponseEntity.ok(new HoursWorkedDTO(hours));
}
```

### 7. Configuration
- Timezone handling

### 8. Sample Implementation

**HoursWorkedDTO.java**
```java
public class HoursWorkedDTO {
    private long minutes;
    public HoursWorkedDTO(Duration duration) { this.minutes = duration.toMinutes(); }
}
```

---

## US04.04: Handle Missed Punch Corrections

### 1. Architecture Overview
- Correction workflow for missed punches

### 2. Package Structure
- Add `CorrectionRequest` entity

### 3. Domain Model

**CorrectionRequest.java**
```java
@Entity
public class CorrectionRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime missedTime;
    private String reason;
    @Enumerated(EnumType.STRING)
    private Status status; // PENDING, APPROVED, REJECTED
    // ...
}
```

### 4. Repository Layer

**CorrectionRequestRepository.java**
```java
@Repository
public interface CorrectionRequestRepository extends JpaRepository<CorrectionRequest, Long> {
    List<CorrectionRequest> findByStatus(Status status);
}
```

### 5. Service Layer

**CorrectionService.java**
```java
public interface CorrectionService {
    CorrectionRequest submit(CorrectionRequestDTO dto);
    CorrectionRequest approve(Long id);
    CorrectionRequest reject(Long id);
}
```

### 6. Controller Layer

**CorrectionController.java**
```java
@RestController
@RequestMapping("/attendance/corrections")
public class CorrectionController {
    @PostMapping
    public ResponseEntity<CorrectionRequestDTO> submit(@RequestBody CorrectionRequestDTO dto) { ... }
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id) { ... }
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id) { ... }
}
```

### 7. Configuration
- Approval workflow

### 8. Sample Implementation

**CorrectionRequestDTO.java**
```java
public class CorrectionRequestDTO {
    private Long employeeId;
    private LocalDateTime missedTime;
    private String reason;
    // getters/setters
}
```

---

# E05: Shift & Schedule Management (4 user stories)

## US05.01: Create Shift Template Management

### 1. Architecture Overview
- CRUD for shift templates

### 2. Package Structure
```
com.wems.schedule
âââ controller
âââ domain
âââ dto
âââ repository
âââ service
```

### 3. Domain Model

**ShiftTemplate.java**
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrence; // e.g., "WEEKLY"
    // ...
}
```

### 4. Repository Layer

**ShiftTemplateRepository.java**
```java
@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {}
```

### 5. Service Layer

**ShiftTemplateService.java**
```java
public interface ShiftTemplateService {
    ShiftTemplate create(ShiftTemplateDTO dto);
    ShiftTemplate update(Long id, ShiftTemplateDTO dto);
    void delete(Long id);
    ShiftTemplate get(Long id);
    List<ShiftTemplate> list();
}
```

### 6. Controller Layer

**ShiftTemplateController.java**
```java
@RestController
@RequestMapping("/shifts/templates")
public class ShiftTemplateController {
    @PostMapping
    public ResponseEntity<ShiftTemplateDTO> create(@RequestBody ShiftTemplateDTO dto) { ... }
    @GetMapping
    public List<ShiftTemplateDTO> list() { ... }
    // ...
}
```

### 7. Configuration
- Validation

### 8. Sample Implementation

**ShiftTemplateDTO.java**
```java
public class ShiftTemplateDTO {
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrence;
    // getters/setters
}
```

---

## US05.02: Implement Shift Assignment

### 1. Architecture Overview
- Assign shifts to employees

### 2. Package Structure
- Add `ShiftAssignment` entity

### 3. Domain Model

**ShiftAssignment.java**
```java
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

### 4. Repository Layer

**ShiftAssignmentRepository.java**
```java
@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    List<ShiftAssignment> findByEmployeeAndDateBetween(Employee emp, LocalDate start, LocalDate end);
}
```

### 5. Service Layer

**ShiftAssignmentService.java**
```java
public interface ShiftAssignmentService {
    ShiftAssignment assign(Long employeeId, Long shiftTemplateId, LocalDate date);
    List<ShiftAssignment> listAssignments(Long employeeId, LocalDate from, LocalDate to);
}
```

### 6. Controller Layer

**ShiftAssignmentController.java**
```java
@RestController
@RequestMapping("/shifts/assignments")
public class ShiftAssignmentController {
    @PostMapping
    public ResponseEntity<ShiftAssignmentDTO> assign(@RequestBody ShiftAssignmentDTO dto) { ... }
    @GetMapping
    public List<ShiftAssignmentDTO> list(@RequestParam Long employeeId, @RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
}
```

### 7. Configuration
- Validation

### 8. Sample Implementation

**ShiftAssignmentDTO.java**
```java
public class ShiftAssignmentDTO {
    private Long employeeId;
    private Long shiftTemplateId;
    private LocalDate date;
    // getters/setters
}
```

---

## US05.03: Detect Schedule Conflicts

### 1. Architecture Overview
- Prevent overlapping assignments

### 2. Package Structure
- Add conflict detection logic in service

### 3. Domain Model
- No changes

### 4. Repository Layer

**ShiftAssignmentRepository.java**
```java
@Query("SELECT sa FROM ShiftAssignment sa WHERE sa.employee = :employee AND sa.date = :date")
List<ShiftAssignment> findByEmployeeAndDate(Employee employee, LocalDate date);
```

### 5. Service Layer

**ShiftAssignmentServiceImpl.java**
```java
public ShiftAssignment assign(Long employeeId, Long shiftTemplateId, LocalDate date) {
    Employee emp = ...;
    if (!repo.findByEmployeeAndDate(emp, date).isEmpty()) {
        throw new ScheduleConflictException();
    }
    // assign
}
```

### 6. Controller Layer
- Return 409 on conflict

### 7. Configuration
- Exception handler

### 8. Sample Implementation

**ScheduleConflictException.java**
```java
@ResponseStatus(HttpStatus.CONFLICT)
public class ScheduleConflictException extends RuntimeException {}
```

---

## US05.04: Support Bulk Assignment Operations

### 1. Architecture Overview
- Bulk assign shifts to multiple employees

### 2. Package Structure
- Add bulk assignment DTO

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer

**ShiftAssignmentService.java**
```java
List<ShiftAssignment> bulkAssign(BulkAssignmentDTO dto);
```

### 6. Controller Layer

**ShiftAssignmentController.java**
```java
@PostMapping("/bulk")
public ResponseEntity<List<ShiftAssignmentDTO>> bulkAssign(@RequestBody BulkAssignmentDTO dto) { ... }
```

### 7. Configuration
- Transactional bulk operation

### 8. Sample Implementation

**BulkAssignmentDTO.java**
```java
public class BulkAssignmentDTO {
    private List<Long> employeeIds;
    private Long shiftTemplateId;
    private LocalDate date;
    // getters/setters
}
```

---

# E06: Leave & Absence Management (4 user stories)

## US06.01: Create Leave Request Workflow

### 1. Architecture Overview
- Employees request leave; supervisors approve/deny

### 2. Package Structure
```
com.wems.leave
âââ controller
âââ domain
âââ dto
âââ repository
âââ service
```

### 3. Domain Model

**LeaveRequest.java**
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
    private Status status; // PENDING, APPROVED, DENIED
    private String reason;
    // ...
}
```

### 4. Repository Layer

**LeaveRequestRepository.java**
```java
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployee(Employee employee);
}
```

### 5. Service Layer

**LeaveService.java**
```java
public interface LeaveService {
    LeaveRequest requestLeave(LeaveRequestDTO dto);
    LeaveRequest approve(Long id);
    LeaveRequest deny(Long id);
    List<LeaveRequest> list(Long employeeId);
}
```

### 6. Controller Layer

**LeaveController.java**
```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping
    public ResponseEntity<LeaveRequestDTO> request(@RequestBody LeaveRequestDTO dto) { ... }
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id) { ... }
    @PostMapping("/{id}/deny")
    public ResponseEntity<Void> deny(@PathVariable Long id) { ... }
    @GetMapping
    public List<LeaveRequestDTO> list(@RequestParam Long employeeId) { ... }
}
```

### 7. Configuration
- Approval workflow

### 8. Sample Implementation

**LeaveRequestDTO.java**
```java
public class LeaveRequestDTO {
    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String type;
    private String reason;
    // getters/setters
}
```

---

## US06.02: Implement Approval Process

### 1. Architecture Overview
- Supervisor approves/denies leave

### 2. Package Structure
- As above

### 3. Domain Model
- Status field in `LeaveRequest`

### 4. Repository Layer
- No changes

### 5. Service Layer

**LeaveServiceImpl.java**
```java
public LeaveRequest approve(Long id) {
    LeaveRequest req = repo.findById(id).orElseThrow();
    req.setStatus(Status.APPROVED);
    return repo.save(req);
}
```

### 6. Controller Layer
- As above

### 7. Configuration
- Notification on approval/denial

### 8. Sample Implementation

**Status Enum**
```java
public enum Status { PENDING, APPROVED, DENIED }
```

---

## US06.03: Track Accrual Balances

### 1. Architecture Overview
- Track leave balances per employee

### 2. Package Structure
- Add `LeaveBalance` entity

### 3. Domain Model

**LeaveBalance.java**
```java
@Entity
public class LeaveBalance {
    @Id @GeneratedValue
    private Long id;
    @OneToOne
    private Employee employee;
    private int pto;
    private int sick;
    private int unpaid;
    // ...
}
```

### 4. Repository Layer

**LeaveBalanceRepository.java**
```java
@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    LeaveBalance findByEmployee(Employee employee);
}
```

### 5. Service Layer

**LeaveServiceImpl.java**
```java
public void updateBalance(Long employeeId, LeaveType type, int days) {
    LeaveBalance bal = repo.findByEmployee(...);
    // update balance
}
```

### 6. Controller Layer

**LeaveController.java**
```java
@GetMapping("/balance")
public LeaveBalanceDTO getBalance(@RequestParam Long employeeId) { ... }
```

### 7. Configuration
- Accrual policies

### 8. Sample Implementation

**LeaveBalanceDTO.java**
```java
public class LeaveBalanceDTO {
    private int pto;
    private int sick;
    private int unpaid;
    // getters/setters
}
```

---

## US06.04: Integrate with Scheduling System

### 1. Architecture Overview
- Exclude approved leave from shift assignment

### 2. Package Structure
- Integration between leave and schedule modules

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer

**ShiftAssignmentServiceImpl.java**
```java
public boolean isAvailableForShift(Long employeeId, LocalDate date) {
    List<LeaveRequest> leaves = leaveRepo.findByEmployeeAndStatusAndDate(employeeId, Status.APPROVED, date);
    return leaves.isEmpty();
}
```

### 6. Controller Layer
- No changes

### 7. Configuration
- Integration hooks

### 8. Sample Implementation

**LeaveRepository.java**
```java
@Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.status = 'APPROVED' AND :date BETWEEN lr.startDate AND lr.endDate")
List<LeaveRequest> findByEmployeeAndStatusAndDate(Long employeeId, Status status, LocalDate date);
```

---

# E07: Training & Certification Tracking (4 user stories)

## US07.01: Create Certification Management

### 1. Architecture Overview
- CRUD for certifications

### 2. Package Structure
```
com.wems.certification
âââ controller
âââ domain
âââ dto
âââ repository
âââ service
```

### 3. Domain Model

**Certification.java**
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String name;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
    // ...
}
```

### 4. Repository Layer

**CertificationRepository.java**
```java
@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByEmployee(Employee employee);
    List<Certification> findByExpiryDateBefore(LocalDate date);
}
```

### 5. Service Layer

**CertificationService.java**
```java
public interface CertificationService {
    Certification create(CertificationDTO dto);
    Certification update(Long id, CertificationDTO dto);
    void delete(Long id);
    Certification get(Long id);
    List<Certification> list(Long employeeId);
}
```

### 6. Controller Layer

**CertificationController.java**
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<CertificationDTO> create(@RequestBody CertificationDTO dto) { ... }
    @GetMapping
    public List<CertificationDTO> list(@RequestParam Long employeeId) { ... }
    // ...
}
```

### 7. Configuration
- File upload for documents

### 8. Sample Implementation

**CertificationDTO.java**
```java
public class CertificationDTO {
    private Long employeeId;
    private String name;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
    // getters/setters
}
```

---

## US07.02: Implement Expiration Alerts

### 1. Architecture Overview
- Alert employees 30/7 days before expiry

### 2. Package Structure
- Add scheduled job

### 3. Domain Model
- No changes

### 4. Repository Layer
- Query expiring certifications

### 5. Service Layer

**CertificationServiceImpl.java**
```java
@Scheduled(cron = "0 0 9 * * ?")
public void checkExpiringCertifications() {
    LocalDate in30Days = LocalDate.now().plusDays(30);
    List<Certification> expiring = repo.findByExpiryDateBefore(in30Days);
    // send alerts
}
```

### 6. Controller Layer
- No changes

### 7. Configuration
- Enable scheduling

**Application.java**
```java
@EnableScheduling
public class Application { ... }
```

### 8. Sample Implementation

**Alert Service**
```java
public void sendExpiryAlert(Certification cert) {
    // send email/notification
}
```

---

## US07.03: Block Unqualified Assignments

### 1. Architecture Overview
- Prevent assignment if certification expired

### 2. Package Structure
- Integration with shift assignment

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer

**ShiftAssignmentServiceImpl.java**
```java
public boolean hasValidCertification(Long employeeId, String certName) {
    List<Certification> certs = certRepo.findByEmployeeAndName(employeeId, certName);
    return certs.stream().anyMatch(c -> c.getExpiryDate().isAfter(LocalDate.now()));
}
```

### 6. Controller Layer
- No changes

### 7. Configuration
- Validation rules

### 8. Sample Implementation

**CertificationRepository.java**
```java
List<Certification> findByEmployeeAndName(Long employeeId, String name);
```

---

## US07.04: Support Document Upload

### 1. Architecture Overview
- Upload certification documents

### 2. Package Structure
- Add file storage service

### 3. Domain Model
- `documentUrl` field in `Certification`

### 4. Repository Layer
- No changes

### 5. Service Layer

**FileStorageService.java**
```java
public interface FileStorageService {
    String store(MultipartFile file);
}
```

### 6. Controller Layer

**CertificationController.java**
```java
@PostMapping("/{id}/upload")
public ResponseEntity<String> uploadDocument(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
    String url = fileStorageService.store(file);
    certService.updateDocumentUrl(id, url);
    return ResponseEntity.ok(url);
}
```

### 7. Configuration
- File storage config (S3, local, etc.)

### 8. Sample Implementation

**application.yml**
```yaml
file:
  storage:
    location: /var/wems/uploads
```

---

# E08: Safety Incidents & OSHA Reporting (4 user stories)

## US08.01: Create Incident Recording System

### 1. Architecture Overview
- Record safety incidents

### 2. Package Structure
```
com.wems.safety
âââ controller
âââ domain
âââ dto
âââ repository
âââ service
```

### 3. Domain Model

**SafetyIncident.java**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime timestamp;
    private String severity;
    private String location;
    private String description;
    @Enumerated(EnumType.STRING)
    private Status status; // OPEN, INVESTIGATING, RESOLVED
    // ...
}
```

### 4. Repository Layer

**SafetyIncidentRepository.java**
```java
@Repository
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
    List<SafetyIncident> findByStatus(Status status);
}
```

### 5. Service Layer

**SafetyIncidentService.java**
```java
public interface SafetyIncidentService {
    SafetyIncident create(SafetyIncidentDTO dto);
    SafetyIncident update(Long id, SafetyIncidentDTO dto);
    SafetyIncident get(Long id);
    List<SafetyIncident> list();
}
```

### 6. Controller Layer

**SafetyIncidentController.java**
```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
    @PostMapping
    public ResponseEntity<SafetyIncidentDTO> create(@RequestBody SafetyIncidentDTO dto) { ... }
    @GetMapping
    public List<SafetyIncidentDTO> list() { ... }
    // ...
}
```

### 7. Configuration
- Validation

### 8. Sample Implementation

**SafetyIncidentDTO.java**
```java
public class SafetyIncidentDTO {
    private Long employeeId;
    private LocalDateTime timestamp;
    private String severity;
    private String location;
    private String description;
    // getters/setters
}
```

---

## US08.02: Implement Investigation Workflow

### 1. Architecture Overview
- Workflow: Open â Investigating â Resolved

### 2. Package Structure
- As above

### 3. Domain Model
- Status field in `SafetyIncident`

### 4. Repository Layer
- No changes

### 5. Service Layer

**SafetyIncidentServiceImpl.java**
```java
public SafetyIncident investigate(Long id) {
    SafetyIncident incident = repo.findById(id).orElseThrow();
    incident.setStatus(Status.INVESTIGATING);
    return repo.save(incident);
}

public SafetyIncident resolve(Long id) {
    SafetyIncident incident = repo.findById(id).orElseThrow();
    incident.setStatus(Status.RESOLVED);
    return repo.save(incident);
}
```

### 6. Controller Layer

**SafetyIncidentController.java**
```java
@PostMapping("/{id}/investigate")
public ResponseEntity<Void> investigate(@PathVariable Long id) { ... }

@PostMapping("/{id}/resolve")
public ResponseEntity<Void> resolve(@PathVariable Long id) { ... }
```

### 7. Configuration
- Workflow state machine

### 8. Sample Implementation

**Status Enum**
```java
public enum Status { OPEN, INVESTIGATING, RESOLVED }
```

---

## US08.03: Generate OSHA Reports

### 1. Architecture Overview
- Export OSHA 300/300A fields

### 2. Package Structure
- Add report generation service

### 3. Domain Model
- No changes

### 4. Repository Layer
- Query incidents by date range

### 5. Service Layer

**OshaReportService.java**
```java
public interface OshaReportService {
    byte[] generate300Report(LocalDate from, LocalDate to);
    byte[] generate300AReport(int year);
}
```

### 6. Controller Layer

**SafetyIncidentController.java**
```java
@GetMapping("/reports/osha300")
public ResponseEntity<byte[]> getOsha300(@RequestParam LocalDate from, @RequestParam LocalDate to) {
    byte[] report = oshaReportService.generate300Report(from, to);
    return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=osha300.pdf").body(report);
}
```

### 7. Configuration
- Report template

### 8. Sample Implementation

**OSHA 300 Fields**
```java
public class Osha300Record {
    private String employeeName;
    private LocalDate incidentDate;
    private String description;
    private String severity;
    // ...
}
```

---

## US08.04: Create Safety Metrics Dashboard

### 1. Architecture Overview
- Metrics endpoints for BI

### 2. Package Structure
- Add metrics service

### 3. Domain Model
- No changes

### 4. Repository Layer
- Aggregate queries

### 5. Service Layer

**SafetyMetricsService.java**
```java
public interface SafetyMetricsService {
    Map<String, Long> getIncidentsBySeverity();
    Map<String, Long> getIncidentsByMonth();
}
```

### 6. Controller Layer

**SafetyMetricsController.java**
```java
@RestController
@RequestMapping("/safety/metrics")
public class SafetyMetricsController {
    @GetMapping("/by-severity")
    public Map<String, Long> getBySeverity() { ... }
    @GetMapping("/by-month")
    public Map<String, Long> getByMonth() { ... }
}
```

### 7. Configuration
- Caching for metrics

### 8. Sample Implementation

**SafetyIncidentRepository.java**
```java
@Query("SELECT i.severity, COUNT(i) FROM SafetyIncident i GROUP BY i.severity")
List<Object[]> countBySeverity();
```

---

# E09: Equipment & Asset Assignment (4 user stories)

## US09.01: Create Asset Registry

### 1. Architecture Overview
- CRUD for assets

### 2. Package Structure
```
com.wems.asset
âââ controller
âââ domain
âââ dto
âââ repository
âââ service
```

### 3. Domain Model

**Asset.java**
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String assetId;
    private String type; // SCANNER, FORKLIFT, PPE
    private String condition;
    @ManyToOne
    private Employee assignedTo;
    // ...
}
```

### 4. Repository Layer

**AssetRepository.java**
```java
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByAssignedTo(Employee employee);
}
```

### 5. Service Layer

**AssetService.java**
```java
public interface AssetService {
    Asset create(AssetDTO dto);
    Asset update(Long id, AssetDTO dto);
    void delete(Long id);
    Asset get(Long id);
    List<Asset> list();
}
```

### 6. Controller Layer

**AssetController.java**
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping
    public ResponseEntity<AssetDTO> create(@RequestBody AssetDTO dto) { ... }
    @GetMapping
    public List<AssetDTO> list() { ... }
    // ...
}
```

### 7. Configuration
- Validation

### 8. Sample Implementation

**AssetDTO.java**
```java
public class AssetDTO {
    private String assetId;
    private String type;
    private String condition;
    private Long assignedToId;
    // getters/setters
}
```

---

## US09.02: Implement Check-In/Out System

### 1. Architecture Overview
- Track asset checkout/return

### 2. Package Structure
- Add `AssetTransaction` entity

### 3. Domain Model

**AssetTransaction.java**
```java
@Entity
public class AssetTransaction {
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

### 4. Repository Layer

**AssetTransactionRepository.java**
```java
@Repository
public interface AssetTransactionRepository extends JpaRepository<AssetTransaction, Long> {
    List<AssetTransaction> findByAsset(Asset asset);
}
```

### 5. Service Layer

**AssetService.java**
```java
AssetTransaction checkout(Long assetId, Long employeeId);
AssetTransaction checkin(Long assetId);
```

### 6. Controller Layer

**AssetController.java**
```java
@PostMapping("/{id}/checkout")
public ResponseEntity<AssetTransactionDTO> checkout(@PathVariable Long id, @RequestParam Long employeeId) { ... }

@PostMapping("/{id}/checkin")
public ResponseEntity<AssetTransactionDTO> checkin(@PathVariable Long id) { ... }
```

### 7. Configuration
- Validation

### 8. Sample Implementation

**AssetTransactionDTO.java**
```java
public class AssetTransactionDTO {
    private Long assetId;
    private Long employeeId;
    private LocalDateTime checkoutTime;
    private LocalDateTime returnTime;
    // getters/setters
}
```

---

## US09.03: Validate Certification Requirements

### 1. Architecture Overview
- Block checkout if certification missing

### 2. Package Structure
- Integration with certification module

### 3. Domain Model
- Add `requiredCertification` field to `Asset`

### 4. Repository Layer
- No changes

### 5. Service Layer

**AssetServiceImpl.java**
```java
public AssetTransaction checkout(Long assetId, Long employeeId) {
    Asset asset = repo.findById(assetId).orElseThrow();
    if (asset.getRequiredCertification() != null) {
        if (!certService.hasValidCertification(employeeId, asset.getRequiredCertification())) {
            throw new CertificationRequiredException();
        }
    }
    // checkout
}
```

### 6. Controller Layer
- Return 400 on missing certification

### 7. Configuration
- Exception handler

### 8. Sample Implementation

**CertificationRequiredException.java**
```java
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CertificationRequiredException extends RuntimeException {}
```

---

## US09.04: Track Asset History

### 1. Architecture Overview
- View asset transaction history

### 2. Package Structure
- As above

### 3. Domain Model
- No changes

### 4. Repository Layer
- Query transactions by asset

### 5. Service Layer

**AssetService.java**
```java
List<AssetTransaction> getHistory(Long assetId);
```

### 6. Controller Layer

**AssetController.java**
```java
@GetMapping("/{id}/history")
public List<AssetTransactionDTO> getHistory(@PathVariable Long id) { ... }
```

### 7. Configuration
- Pagination for history

### 8. Sample Implementation

**AssetTransactionRepository.java**
```java
Page<AssetTransaction> findByAsset(Asset asset, Pageable pageable);
```

---

# E10: Performance Reviews & Goals (4 user stories)

## US10.01: Create Review Template Management

### 1. Architecture Overview
- CRUD for review templates

### 2. Package Structure
```
com.wems.performance
âââ controller
âââ domain
âââ dto
âââ repository
âââ service
```

### 3. Domain Model

**ReviewTemplate.java**
```java
@Entity
public class ReviewTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String frequency; // QUARTERLY, ANNUAL
    @ElementCollection
    private List<String> competencies;
    // ...
}
```

### 4. Repository Layer

**ReviewTemplateRepository.java**
```java
@Repository
public interface ReviewTemplateRepository extends JpaRepository<ReviewTemplate, Long> {}
```

### 5. Service Layer

**ReviewTemplateService.java**
```java
public interface ReviewTemplateService {
    ReviewTemplate create(ReviewTemplateDTO dto);
    ReviewTemplate update(Long id, ReviewTemplateDTO dto);
    void delete(Long id);
    ReviewTemplate get(Long id);
    List<ReviewTemplate> list();
}
```

### 6. Controller Layer

**ReviewTemplateController.java**
```java
@RestController
@RequestMapping("/performance/templates")
public class ReviewTemplateController {
    @PostMapping
    public ResponseEntity<ReviewTemplateDTO> create(@RequestBody ReviewTemplateDTO dto) { ... }
    @GetMapping
    public List<ReviewTemplateDTO> list() { ... }
    // ...
}
```

### 7. Configuration
- Validation

### 8. Sample Implementation

**ReviewTemplateDTO.java**
```java
public class ReviewTemplateDTO {
    private String name;
    private String frequency;
    private List<String> competencies;
    // getters/setters
}
```

---

## US10.02: Implement Review Workflow

### 1. Architecture Overview
- Create review cycles, assign to employees

### 2. Package Structure
- Add `PerformanceReview` entity

### 3. Domain Model

**PerformanceReview.java**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ReviewTemplate template;
    private LocalDate reviewDate;
    @Enumerated(EnumType.STRING)
    private Status status; // DRAFT, SUBMITTED, ACKNOWLEDGED
    private String comments;
    // ...
}
```

### 4. Repository Layer

**PerformanceReviewRepository.java**
```java
@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    List<PerformanceReview> findByEmployee(Employee employee);
}
```

### 5. Service Layer

**PerformanceReviewService.java**
```java
public interface PerformanceReviewService {
    PerformanceReview create(PerformanceReviewDTO dto);
    PerformanceReview submit(Long id);
    PerformanceReview acknowledge(Long id);
    List<PerformanceReview> list(Long employeeId);
}
```

### 6. Controller Layer

**PerformanceReviewController.java**
```java
@RestController
@RequestMapping("/performance/reviews")
public class PerformanceReviewController {
    @PostMapping
    public ResponseEntity<PerformanceReviewDTO> create(@RequestBody PerformanceReviewDTO dto) { ... }
    @PostMapping("/{id}/submit")
    public ResponseEntity<Void> submit(@PathVariable Long id) { ... }
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<Void> acknowledge(@PathVariable Long id) { ... }
}
```

### 7. Configuration
- Workflow state machine

### 8. Sample Implementation

**PerformanceReviewDTO.java**
```java
public class PerformanceReviewDTO {
    private Long employeeId;
    private Long templateId;
    private LocalDate reviewDate;
    private String comments;
    // getters/setters
}
```

---

## US10.03: Track Goals and Competencies

### 1. Architecture Overview
- Track goals and ratings

### 2. Package Structure
- Add `Goal` entity

### 3. Domain Model

**Goal.java**
```java
@Entity
public class Goal {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private PerformanceReview review;
    private String description;
    private int rating;
    // ...
}
```

### 4. Repository Layer

**GoalRepository.java**
```java
@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByReview(PerformanceReview review);
}
```

### 5. Service Layer

**GoalService.java**
```java
public interface GoalService {
    Goal create(GoalDTO dto);
    Goal update(Long id, GoalDTO dto);
    void delete(Long id);
    List<Goal> list(Long reviewId);
}
```

### 6. Controller Layer

**GoalController.java**
```java
@RestController
@RequestMapping("/performance/goals")
public class GoalController {
    @PostMapping
    public ResponseEntity<GoalDTO> create(@RequestBody GoalDTO dto) { ... }
    @GetMapping
    public List<GoalDTO> list(@RequestParam Long reviewId) { ... }
}
```

### 7. Configuration
- Validation

### 8. Sample Implementation

**GoalDTO.java**
```java
public class GoalDTO {
    private Long reviewId;
    private String description;
    private int rating;
    // getters/setters
}
```

---

## US10.04: Generate Review Reports

### 1. Architecture Overview
- Export reviews as PDF

### 2. Package Structure
- Add report generation service

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer

**ReviewReportService.java**
```java
public interface ReviewReportService {
    byte[] generatePdf(Long reviewId);
}
```

### 6. Controller Layer

**PerformanceReviewController.java**
```java
@GetMapping("/{id}/pdf")
public ResponseEntity<byte[]> getPdf(@PathVariable Long id) {
    byte[] pdf = reportService.generatePdf(id);
    return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=review.pdf").body(pdf);
}
```

### 7. Configuration
- PDF library (iText, etc.)

### 8. Sample Implementation

**ReviewReportServiceImpl.java**
```java
public byte[] generatePdf(Long reviewId) {
    PerformanceReview review = repo.findById(reviewId).orElseThrow();
    // generate PDF
}
```

---

# E11: Payroll Export Integration (4 user stories)

## US11.01: Create Payroll Data Aggregation

### 1. Architecture Overview
- Aggregate attendance and leave data

### 2. Package Structure
```
com.wems.payroll
âââ controller
âââ dto
âââ service
```

### 3. Domain Model
- No new entities

### 4. Repository Layer
- Use existing repositories

### 5. Service Layer

**PayrollService.java**
```java
public interface PayrollService {
    List<PayrollRecord> aggregate(LocalDate from, LocalDate to);
}
```

**PayrollServiceImpl.java**
```java
public List<PayrollRecord> aggregate(LocalDate from, LocalDate to) {
    // query attendance, leave, calculate totals
}
```

### 6. Controller Layer

**PayrollController.java**
```java
@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @GetMapping("/aggregate")
    public List<PayrollRecordDTO> aggregate(@RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
}
```

### 7. Configuration
- Validation

### 8. Sample Implementation

**PayrollRecord.java**
```java
public class PayrollRecord {
    private Long employeeId;
    private String badgeId;
    private double hoursWorked;
    private int leaveDays;
    // ...
}
```

---

## US11.02: Implement Export Format Mapping

### 1. Architecture Overview
- Map to external payroll provider format

### 2. Package Structure
- Add format mapper

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer

**PayrollExportService.java**
```java
public interface PayrollExportService {
    byte[] export(List<PayrollRecord> records, String format);
}
```

### 6. Controller Layer

**PayrollController.java**
```java
@GetMapping("/export")
public ResponseEntity<byte[]> export(@RequestParam LocalDate from, @RequestParam LocalDate to, @RequestParam String format) {
    List<PayrollRecord> records = payrollService.aggregate(from, to);
    byte[] file = exportService.export(records, format);
    return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=payroll.csv").body(file);
}
```

### 7. Configuration
- Format templates

### 8. Sample Implementation

**PayrollExportServiceImpl.java**
```java
public byte[] export(List<PayrollRecord> records, String format) {
    // map to CSV/XML/JSON
}
```

---

## US11.03: Configure Secure Delivery

### 1. Architecture Overview
- Deliver via SFTP/API

### 2. Package Structure
- Add delivery service

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer

**PayrollDeliveryService.java**
```java
public interface PayrollDeliveryService {
    void deliver(byte[] file, String destination);
}
```

### 6. Controller Layer

**PayrollController.java**
```java
@PostMapping("/deliver")
public ResponseEntity<Void> deliver(@RequestParam LocalDate from, @RequestParam LocalDate to) {
    List<PayrollRecord> records = payrollService.aggregate(from, to);
    byte[] file = exportService.export(records, "CSV");
    deliveryService.deliver(file, "sftp://payroll.example.com");
    return ResponseEntity.ok().build();
}
```

### 7. Configuration

**application.yml**
```yaml
payroll:
  delivery:
    sftp:
      host: payroll.example.com
      username: wems
      password: secret
```

### 8. Sample Implementation

**SftpDeliveryService.java**
```java
public void deliver(byte[] file, String destination) {
    // SFTP upload
}
```

---

## US11.04: Implement Retry Logic

### 1. Architecture Overview
- Retry failed deliveries with backoff

### 2. Package Structure
- Add retry logic

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer

**PayrollDeliveryServiceImpl.java**
```java
@Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 5000))
public void deliver(byte[] file, String destination) {
    // SFTP upload
}
```

### 6. Controller Layer
- No changes

### 7. Configuration

**Application.java**
```java
@EnableRetry
public class Application { ... }
```

### 8. Sample Implementation

**Retry Config**
```java
@Configuration
@EnableRetry
public class RetryConfig {}
```

---

# E12: Notifications & Announcements (5 user stories)

## US12.01: Create Notification Service

### 1. Architecture Overview
- Multi-channel notification service

### 2. Package Structure
```
com.wems.notification
âââ controller
âââ domain
âââ dto
âââ repository
âââ service
```

### 3. Domain Model

**Notification.java**
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee recipient;
    private String subject;
    private String message;
    @Enumerated(EnumType.STRING)
    private Channel channel; // EMAIL, SMS, IN_APP
    private LocalDateTime sentAt;
    // ...
}
```

### 4. Repository Layer

**NotificationRepository.java**
```java
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipient(Employee recipient);
}
```

### 5. Service Layer

**NotificationService.java**
```java
public interface NotificationService {
    void send(NotificationDTO dto);
    List<Notification> list(Long recipientId);
}
```

### 6. Controller Layer

**NotificationController.java**
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping
    public ResponseEntity<Void> send(@RequestBody NotificationDTO dto) { ... }
    @GetMapping
    public List<NotificationDTO> list(@RequestParam Long recipientId) { ... }
}
```

### 7. Configuration
- Email/SMS config

### 8. Sample Implementation

**NotificationDTO.java**
```java
public class NotificationDTO {
    private Long recipientId;
    private String subject;
    private String message;
    private String channel;
    // getters/setters
}
```

---

## US12.02: Implement Multi-Channel Delivery

### 1. Architecture Overview
- Support email, SMS, in-app

### 2. Package Structure
- Add channel-specific services

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer

**EmailService.java**
```java
public interface EmailService {
    void send(String to, String subject, String body);
}
```

**SmsService.java**
```java
public interface SmsService {
    void send(String to, String message);
}
```

**NotificationServiceImpl.java**
```java
public void send(NotificationDTO dto) {
    if (dto.getChannel().equals("EMAIL")) {
        emailService.send(...);
    } else if (dto.getChannel().equals("SMS")) {
        smsService.send(...);
    }
    // save notification
}
```

### 6. Controller Layer
- No changes

### 7. Configuration

**application.yml**
```yaml
notification:
  email:
    host: smtp.example.com
    port: 587
  sms:
    provider: twilio
    apiKey: xxx
```

### 8. Sample Implementation

**EmailServiceImpl.java**
```java
public void send(String to, String subject, String body) {
    // JavaMailSender
}
```

---

## US12.03: Configure User Preferences

### 1. Architecture Overview
- Users opt-in/out per channel

### 2. Package Structure
- Add `NotificationPreference` entity

### 3. Domain Model

**NotificationPreference.java**
```java
@Entity
public class NotificationPreference {
    @Id @GeneratedValue
    private Long id;
    @OneToOne
    private Employee employee;
    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean inAppEnabled;
    // ...
}
```

### 4. Repository Layer

**NotificationPreferenceRepository.java**
```java
@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    NotificationPreference findByEmployee(Employee employee);
}
```

### 5. Service Layer

**NotificationServiceImpl.java**
```java
public void send(NotificationDTO dto) {
    NotificationPreference pref = prefRepo.findByEmployee(...);
    if (dto.getChannel().equals("EMAIL") && pref.isEmailEnabled()) {
        emailService.send(...);
    }
    // ...
}
```

### 6. Controller Layer

**NotificationPreferenceController.java**
```java
@RestController
@RequestMapping("/notifications/preferences")
public class NotificationPreferenceController {
    @GetMapping
    public NotificationPreferenceDTO get(@RequestParam Long employeeId) { ... }
    @PutMapping
    public ResponseEntity<Void> update(@RequestBody NotificationPreferenceDTO dto) { ... }
}
```

### 7. Configuration
- Validation

### 8. Sample Implementation

**NotificationPreferenceDTO.java**
```java
public class NotificationPreferenceDTO {
    private Long employeeId;
    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean inAppEnabled;
    // getters/setters
}
```

---

## US12.04: Implement Rate Limiting

### 1. Architecture Overview
- Prevent notification spam

### 2. Package Structure
- Add rate limiter

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer

**NotificationServiceImpl.java**
```java
@RateLimiter(name = "notification", fallbackMethod = "rateLimitFallback")
public void send(NotificationDTO dto) {
    // send notification
}

public void rateLimitFallback(NotificationDTO dto, Throwable t) {
    // log rate limit exceeded
}
```

### 6. Controller Layer
- No changes

### 7. Configuration

**application.yml**
```yaml
resilience4j:
  ratelimiter:
    instances:
      notification:
        limitForPeriod: 10
        limitRefreshPeriod: 1m
```

### 8. Sample Implementation

**Resilience4j Dependency**
```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot2</artifactId>
</dependency>
```

---

## US12.05: Create Announcement System

### 1. Architecture Overview
- Broadcast announcements to all users

### 2. Package Structure
- Add `Announcement` entity

### 3. Domain Model

**Announcement.java**
```java
@Entity
public class Announcement {
    @Id @GeneratedValue
    private Long id;
    private String title;
    private String content;
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    // ...
}
```

### 4. Repository Layer

**AnnouncementRepository.java**
```java
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByPublishedAtBeforeAndExpiresAtAfter(LocalDateTime now1, LocalDateTime now2);
}
```

### 5. Service Layer

**AnnouncementService.java**
```java
public interface AnnouncementService {
    Announcement create(AnnouncementDTO dto);
    List<Announcement> listActive();
}
```

### 6. Controller Layer

**AnnouncementController.java**
```java
@RestController
@RequestMapping("/announcements")
public class AnnouncementController {
    @PostMapping
    public ResponseEntity<AnnouncementDTO> create(@RequestBody AnnouncementDTO dto) { ... }
    @GetMapping
    public List<AnnouncementDTO> listActive() { ... }
}
```

### 7. Configuration
- Validation

### 8. Sample Implementation

**AnnouncementDTO.java**
```java
public class AnnouncementDTO {
    private String title;
    private String content;
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    // getters/setters
}
```

---

# E13: Integration Layer HRIS/WMS APIs (4 user stories)

## US13.01: Create HRIS Integration APIs

### 1. Architecture Overview
- Sync employee data from HRIS

### 2. Package Structure
```
com.wems.integration
âââ hris
âââ wms
âââ sso
```

### 3. Domain Model
- No new entities

### 4. Repository Layer
- Use existing repositories

### 5. Service Layer

**HrisIntegrationService.java**
```java
public interface HrisIntegrationService {
    void syncEmployees();
}
```

**HrisIntegrationServiceImpl.java**
```java
@Scheduled(cron = "0 0 2 * * ?")
public void syncEmployees() {
    List<HrisEmployee> hrisEmployees = hrisClient.fetchEmployees();
    // create/update employees
}
```

### 6. Controller Layer

**HrisIntegrationController.java**
```java
@RestController
@RequestMapping("/integration/hris")
public class HrisIntegrationController {
    @PostMapping("/sync")
    public ResponseEntity<Void> sync() {
        hrisIntegrationService.syncEmployees();
        return ResponseEntity.ok().build();
    }
}
```

### 7. Configuration

**application.yml**
```yaml
hris:
  apiUrl: https://hris.example.com/api
  apiKey: xxx
```

### 8. Sample Implementation

**HrisClient.java**
```java
public interface HrisClient {
    List<HrisEmployee> fetchEmployees();
}
```

---

## US13.02: Implement WMS Connector

### 1. Architecture Overview
- Link to WMS for location/department

### 2. Package Structure
- Add WMS client

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer

**WmsIntegrationService.java**
```java
public interface WmsIntegrationService {
    String getDepartment(String locationId);
}
```

### 6. Controller Layer

**WmsIntegrationController.java**
```java
@RestController
@RequestMapping("/integration/wms")
public class WmsIntegrationController {
    @GetMapping("/department")
    public String getDepartment(@RequestParam String locationId) { ... }
}
```

### 7. Configuration

**application.yml**
```yaml
wms:
  apiUrl: https://wms.example.com/api
  apiKey: xxx
```

### 8. Sample Implementation

**WmsClient.java**
```java
public interface WmsClient {
    String getDepartment(String locationId);
}
```

---

## US13.03: Configure SSO Integration

### 1. Architecture Overview
- OAuth2/SAML SSO

### 2. Package Structure
- Add SSO config

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer
- No changes

### 6. Controller Layer
- No changes

### 7. Configuration

**SecurityConfig.java**
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .oauth2Login()
        .and()
        .authorizeRequests()
        .anyRequest().authenticated();
}
```

**application.yml**
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          okta:
            clientId: xxx
            clientSecret: xxx
            scope: openid,profile,email
        provider:
          okta:
            issuerUri: https://dev-xxx.okta.com/oauth2/default
```

### 8. Sample Implementation

**OAuth2 Dependency**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

---

## US13.04: Implement Webhook System

### 1. Architecture Overview
- Idempotent webhooks for events

### 2. Package Structure
- Add webhook controller

### 3. Domain Model

**WebhookEvent.java**
```java
@Entity
public class WebhookEvent {
    @Id @GeneratedValue
    private Long id;
    private String eventId;
    private String eventType;
    private String payload;
    private LocalDateTime receivedAt;
    // ...
}
```

### 4. Repository Layer

**WebhookEventRepository.java**
```java
@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    boolean existsByEventId(String eventId);
}
```

### 5. Service Layer

**WebhookService.java**
```java
public interface WebhookService {
    void process(WebhookEventDTO dto);
}
```

**WebhookServiceImpl.java**
```java
public void process(WebhookEventDTO dto) {
    if (repo.existsByEventId(dto.getEventId())) {
        return; // idempotent
    }
    // process event
}
```

### 6. Controller Layer

**WebhookController.java**
```java
@RestController
@RequestMapping("/webhooks")
public class WebhookController {
    @PostMapping("/hris")
    public ResponseEntity<Void> hrisWebhook(@RequestBody WebhookEventDTO dto) {
        webhookService.process(dto);
        return ResponseEntity.ok().build();
    }
}
```

### 7. Configuration
- Webhook signature validation

### 8. Sample Implementation

**WebhookEventDTO.java**
```java
public class WebhookEventDTO {
    private String eventId;
    private String eventType;
    private String payload;
    // getters/setters
}
```

---

# E14: Audit Trail & Compliance (4 user stories)

## US14.01: Implement Audit Logging Framework

### 1. Architecture Overview
- Centralized audit logging

### 2. Package Structure
```
com.wems.audit
âââ domain
âââ repository
âââ service
```

### 3. Domain Model

**AuditLog.java**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String actor;
    private String action;
    private String entityType;
    private Long entityId;
    @Column(columnDefinition = "TEXT")
    private String before;
    @Column(columnDefinition = "TEXT")
    private String after;
    private LocalDateTime timestamp;
    // ...
}
```

### 4. Repository Layer

**AuditLogRepository.java**
```java
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
}
```

### 5. Service Layer

**AuditService.java**
```java
public interface AuditService {
    void log(String actor, String action, String entityType, Long entityId, String before, String after);
}
```

### 6. Controller Layer
- No direct controller

### 7. Configuration
- AOP for automatic audit logging

**AuditAspect.java**
```java
@Aspect
@Component
public class AuditAspect {
    @AfterReturning(pointcut = "@annotation(Audited)", returning = "result")
    public void logAudit(JoinPoint joinPoint, Object result) {
        // log audit
    }
}
```

### 8. Sample Implementation

**@Audited Annotation**
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {}
```

---

## US14.02: Create Immutable Audit Store

### 1. Architecture Overview
- Immutable audit log table

### 2. Package Structure
- As above

### 3. Domain Model
- No updates/deletes on `AuditLog`

### 4. Repository Layer
- No update/delete methods

### 5. Service Layer
- Only insert operations

### 6. Controller Layer
- No changes

### 7. Configuration
- DB constraints to prevent updates

**Migration**
```sql
CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    actor VARCHAR(128) NOT NULL,
    action VARCHAR(64) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id BIGINT NOT NULL,
    before TEXT,
    after TEXT,
    timestamp TIMESTAMP NOT NULL
);
-- No UPDATE/DELETE triggers
```

### 8. Sample Implementation

**AuditLogRepository.java**
```java
// No update/delete methods
```

---

## US14.03: Implement Audit Query API

### 1. Architecture Overview
- Query audit logs by date/user/entity

### 2. Package Structure
- Add audit query controller

### 3. Domain Model
- No changes

### 4. Repository Layer

**AuditLogRepository.java**
```java
Page<AuditLog> findByActorAndTimestampBetween(String actor, LocalDateTime from, LocalDateTime to, Pageable pageable);
Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);
```

### 5. Service Layer

**AuditService.java**
```java
Page<AuditLog> query(AuditQueryDTO dto, Pageable pageable);
```

### 6. Controller Layer

**AuditController.java**
```java
@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping
    public Page<AuditLogDTO> query(AuditQueryDTO dto, Pageable pageable) { ... }
}
```

### 7. Configuration
- Access control

### 8. Sample Implementation

**AuditQueryDTO.java**
```java
public class AuditQueryDTO {
    private String actor;
    private String entityType;
    private Long entityId;
    private LocalDateTime from;
    private LocalDateTime to;
    // getters/setters
}
```

---

## US14.04: Create Compliance Reports

### 1. Architecture Overview
- Export audit logs for compliance

### 2. Package Structure
- Add report service

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer

**AuditReportService.java**
```java
public interface AuditReportService {
    byte[] generateReport(LocalDate from, LocalDate to);
}
```

### 6. Controller Layer

**AuditController.java**
```java
@GetMapping("/report")
public ResponseEntity<byte[]> getReport(@RequestParam LocalDate from, @RequestParam LocalDate to) {
    byte[] report = reportService.generateReport(from, to);
    return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=audit.csv").body(report);
}
```

### 7. Configuration
- Report format

### 8. Sample Implementation

**AuditReportServiceImpl.java**
```java
public byte[] generateReport(LocalDate from, LocalDate to) {
    List<AuditLog> logs = repo.findByTimestampBetween(from.atStartOfDay(), to.atTime(23, 59, 59));
    // generate CSV
}
```

---

# E15: Reporting & Analytics (4 user stories)

## US15.01: Create Report Generation Engine

### 1. Architecture Overview
- Flexible report generation

### 2. Package Structure
```
com.wems.reporting
âââ controller
âââ dto
âââ service
```

### 3. Domain Model
- No new entities

### 4. Repository Layer
- Use existing repositories

### 5. Service Layer

**ReportService.java**
```java
public interface ReportService {
    byte[] generate(ReportRequest request);
}
```

### 6. Controller Layer

**ReportController.java**
```java
@RestController
@RequestMapping("/reports")
public class ReportController {
    @PostMapping("/generate")
    public ResponseEntity<byte[]> generate(@RequestBody ReportRequest request) { ... }
}
```

### 7. Configuration
- Report templates

### 8. Sample Implementation

**ReportRequest.java**
```java
public class ReportRequest {
    private String reportType; // ATTENDANCE, OVERTIME, etc.
    private LocalDate from;
    private LocalDate to;
    private Map<String, String> filters;
    // getters/setters
}
```

---

## US15.02: Implement Export Functionality

### 1. Architecture Overview
- Export reports as CSV/PDF

### 2. Package Structure
- Add export service

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer

**ReportExportService.java**
```java
public interface ReportExportService {
    byte[] exportCsv(List<Map<String, Object>> data);
    byte[] exportPdf(List<Map<String, Object>> data);
}
```

### 6. Controller Layer

**ReportController.java**
```java
@GetMapping("/export")
public ResponseEntity<byte[]> export(@RequestParam String reportType, @RequestParam String format) {
    List<Map<String, Object>> data = reportService.getData(reportType);
    byte[] file = format.equals("CSV") ? exportService.exportCsv(data) : exportService.exportPdf(data);
    return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=report." + format.toLowerCase()).body(file);
}
```

### 7. Configuration
- Export templates

### 8. Sample Implementation

**ReportExportServiceImpl.java**
```java
public byte[] exportCsv(List<Map<String, Object>> data) {
    // generate CSV
}
```

---

## US15.03: Create Dashboard APIs

### 1. Architecture Overview
- Metrics endpoints for dashboards

### 2. Package Structure
- Add dashboard controller

### 3. Domain Model
- No changes

### 4. Repository Layer
- Aggregate queries

### 5. Service Layer

**DashboardService.java**
```java
public interface DashboardService {
    Map<String, Object> getMetrics();
}
```

### 6. Controller Layer

**DashboardController.java**
```java
@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() { ... }
}
```

### 7. Configuration
- Caching

### 8. Sample Implementation

**DashboardServiceImpl.java**
```java
@Cacheable("dashboardMetrics")
public Map<String, Object> getMetrics() {
    // aggregate data
}
```

---

## US15.04: Implement Report Access Control

### 1. Architecture Overview
- Role-based report access

### 2. Package Structure
- As above

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer

**ReportServiceImpl.java**
```java
public byte[] generate(ReportRequest request, Authentication auth) {
    if (!hasAccess(request.getReportType(), auth)) {
        throw new AccessDeniedException();
    }
    // generate report
}
```

### 6. Controller Layer

**ReportController.java**
```java
@PreAuthorize("hasAnyRole('ADMIN','HR')")
@PostMapping("/generate")
public ResponseEntity<byte[]> generate(@RequestBody ReportRequest request, Authentication auth) { ... }
```

### 7. Configuration
- Access rules

### 8. Sample Implementation

**Access Check**
```java
private boolean hasAccess(String reportType, Authentication auth) {
    // check role
}
```

---

# E16: Mobile Access PWA (4 user stories)

## US16.01: Create Responsive UI Components

### 1. Architecture Overview
- Responsive web UI

### 2. Package Structure
- Frontend (React/Angular/Vue)

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer
- No changes

### 6. Controller Layer
- Existing REST APIs

### 7. Configuration
- CORS config

**SecurityConfig.java**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.addAllowedOrigin("*");
    config.addAllowedMethod("*");
    config.addAllowedHeader("*");
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

### 8. Sample Implementation

**React Component**
```jsx
function ClockIn() {
    const handleClockIn = () => {
        fetch('/attendance/clock-in', { method: 'POST', body: JSON.stringify({ deviceId: 'mobile' }) });
    };
    return <button onClick={handleClockIn}>Clock In</button>;
}
```

---

## US16.02: Implement PWA Manifest

### 1. Architecture Overview
- PWA manifest for installability

### 2. Package Structure
- Frontend

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer
- No changes

### 6. Controller Layer
- No changes

### 7. Configuration
- Serve manifest.json

### 8. Sample Implementation

**manifest.json**
```json
{
  "name": "WEMS",
  "short_name": "WEMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#000000",
  "icons": [
    {
      "src": "/icon-192.png",
      "sizes": "192x192",
      "type": "image/png"
    }
  ]
}
```

---

## US16.03: Create Offline Queue System

### 1. Architecture Overview
- Queue clock events offline

### 2. Package Structure
- Frontend

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer
- No changes

### 6. Controller Layer
- No changes

### 7. Configuration
- Service worker

### 8. Sample Implementation

**service-worker.js**
```javascript
self.addEventListener('fetch', event => {
    if (event.request.url.includes('/attendance/clock-in')) {
        event.respondWith(
            fetch(event.request).catch(() => {
                // queue for later
                return caches.match('/offline.html');
            })
        );
    }
});
```

---

## US16.04: Optimize Mobile Performance

### 1. Architecture Overview
- Lighthouse PWA score â¥ 80

### 2. Package Structure
- Frontend

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer
- No changes

### 6. Controller Layer
- No changes

### 7. Configuration
- Compression, caching

### 8. Sample Implementation

**Webpack Config**
```javascript
module.exports = {
    optimization: {
        splitChunks: { chunks: 'all' }
    }
};
```

---

# E17: Onboarding & Offboarding Workflow (4 user stories)

## US17.01: Create Onboarding Workflow

### 1. Architecture Overview
- Automate new hire tasks

### 2. Package Structure
```
com.wems.workflow
âââ onboarding
âââ offboarding
```

### 3. Domain Model

**OnboardingTask.java**
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String taskName;
    @Enumerated(EnumType.STRING)
    private Status status; // PENDING, COMPLETED
    // ...
}
```

### 4. Repository Layer

**OnboardingTaskRepository.java**
```java
@Repository
public interface OnboardingTaskRepository extends JpaRepository<OnboardingTask, Long> {
    List<OnboardingTask> findByEmployee(Employee employee);
}
```

### 5. Service Layer

**OnboardingService.java**
```java
public interface OnboardingService {
    void initiate(Long employeeId);
    void completeTask(Long taskId);
}
```

### 6. Controller Layer

**OnboardingController.java**
```java
@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @PostMapping("/initiate")
    public ResponseEntity<Void> initiate(@RequestParam Long employeeId) { ... }
    @PostMapping("/tasks/{id}/complete")
    public ResponseEntity<Void> completeTask(@PathVariable Long id) { ... }
}
```

### 7. Configuration
- Task templates

### 8. Sample Implementation

**OnboardingServiceImpl.java**
```java
public void initiate(Long employeeId) {
    Employee emp = empRepo.findById(employeeId).orElseThrow();
    // create tasks: training, asset assignment, etc.
}
```

---

## US17.02: Automate Account Provisioning

### 1. Architecture Overview
- Create user account on hire

### 2. Package Structure
- As above

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer

**OnboardingServiceImpl.java**
```java
public void initiate(Long employeeId) {
    Employee emp = empRepo.findById(employeeId).orElseThrow();
    // create user account
    User user = new User();
    user.setUsername(emp.getBadgeId());
    user.setPassword(passwordEncoder.encode("temp123"));
    userRepo.save(user);
}
```

### 6. Controller Layer
- No changes

### 7. Configuration
- Default password policy

### 8. Sample Implementation

**Password Reset**
```java
// send password reset email
```

---

## US17.03: Create Offboarding Workflow

### 1. Architecture Overview
- Automate termination tasks

### 2. Package Structure
- Add `OffboardingTask` entity

### 3. Domain Model

**OffboardingTask.java**
```java
@Entity
public class OffboardingTask {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String taskName;
    @Enumerated(EnumType.STRING)
    private Status status;
    // ...
}
```

### 4. Repository Layer

**OffboardingTaskRepository.java**
```java
@Repository
public interface OffboardingTaskRepository extends JpaRepository<OffboardingTask, Long> {
    List<OffboardingTask> findByEmployee(Employee employee);
}
```

### 5. Service Layer

**OffboardingService.java**
```java
public interface OffboardingService {
    void initiate(Long employeeId);
    void completeTask(Long taskId);
}
```

### 6. Controller Layer

**OffboardingController.java**
```java
@RestController
@RequestMapping("/offboarding")
public class OffboardingController {
    @PostMapping("/initiate")
    public ResponseEntity<Void> initiate(@RequestParam Long employeeId) { ... }
}
```

### 7. Configuration
- Task templates

### 8. Sample Implementation

**OffboardingServiceImpl.java**
```java
public void initiate(Long employeeId) {
    Employee emp = empRepo.findById(employeeId).orElseThrow();
    // create tasks: revoke access, collect assets, etc.
}
```

---

## US17.04: Implement Asset Collection Process

### 1. Architecture Overview
- Track asset return on termination

### 2. Package Structure
- Integration with asset module

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer

**OffboardingServiceImpl.java**
```java
public void initiate(Long employeeId) {
    Employee emp = empRepo.findById(employeeId).orElseThrow();
    List<Asset> assets = assetRepo.findByAssignedTo(emp);
    // create tasks to collect assets
}
```

### 6. Controller Layer
- No changes

### 7. Configuration
- Asset collection checklist

### 8. Sample Implementation

**Asset Collection Task**
```java
OffboardingTask task = new OffboardingTask();
task.setEmployee(emp);
task.setTaskName("Collect asset: " + asset.getAssetId());
task.setStatus(Status.PENDING);
offboardingTaskRepo.save(task);
```

---

# E18: Localization & Multi-Tenant (3 user stories)

## US18.01: Implement Multi-Tenant Architecture

### 1. Architecture Overview
- Tenant isolation

### 2. Package Structure
```
com.wems.tenant
```

### 3. Domain Model

**Tenant.java**
```java
@Entity
public class Tenant {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String code;
    // ...
}
```

**Employee.java (updated)**
```java
@Entity
public class Employee {
    // ...
    @ManyToOne
    private Tenant tenant;
}
```

### 4. Repository Layer

**EmployeeRepository.java (updated)**
```java
Page<Employee> findByTenant(Tenant tenant, Pageable pageable);
```

### 5. Service Layer

**TenantService.java**
```java
public interface TenantService {
    Tenant getCurrentTenant();
}
```

### 6. Controller Layer
- Extract tenant from request

### 7. Configuration

**TenantInterceptor.java**
```java
@Component
public class TenantInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantCode = request.getHeader("X-Tenant-Code");
        TenantContext.setCurrentTenant(tenantCode);
        return true;
    }
}
```

### 8. Sample Implementation

**TenantContext.java**
```java
public class TenantContext {
    private static ThreadLocal<String> currentTenant = new ThreadLocal<>();
    public static void setCurrentTenant(String tenant) { currentTenant.set(tenant); }
    public static String getCurrentTenant() { return currentTenant.get(); }
}
```

---

## US18.02: Create Localization Framework

### 1. Architecture Overview
- i18n support

### 2. Package Structure
- Resources

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer
- No changes

### 6. Controller Layer
- Accept `Accept-Language` header

### 7. Configuration

**LocaleConfig.java**
```java
@Configuration
public class LocaleConfig {
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.US);
        return resolver;
    }
}
```

### 8. Sample Implementation

**messages_en.properties**
```
employee.created=Employee created successfully
```

**messages_es.properties**
```
employee.created=Empleado creado exitosamente
```

---

## US18.03: Configure Timezone Handling

### 1. Architecture Overview
- Store timestamps in UTC, display in user timezone

### 2. Package Structure
- As above

### 3. Domain Model
- Use `LocalDateTime` or `ZonedDateTime`

### 4. Repository Layer
- No changes

### 5. Service Layer
- Convert timezones

### 6. Controller Layer
- Accept timezone in request

### 7. Configuration

**application.yml**
```yaml
spring:
  jackson:
    time-zone: UTC
```

### 8. Sample Implementation

**Timezone Conversion**
```java
ZonedDateTime utc = ZonedDateTime.now(ZoneId.of("UTC"));
ZonedDateTime userTime = utc.withZoneSameInstant(ZoneId.of("America/New_York"));
```

---

# E19: Observability & Monitoring (4 user stories)

## US19.01: Integrate Prometheus Metrics

### 1. Architecture Overview
- Expose metrics at `/actuator/prometheus`

### 2. Package Structure
- Config

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer
- No changes

### 6. Controller Layer
- No changes

### 7. Configuration

**pom.xml**
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**application.yml**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: prometheus
```

### 8. Sample Implementation

**Custom Metric**
```java
@Component
public class CustomMetrics {
    private final Counter clockInCounter;

    public CustomMetrics(MeterRegistry registry) {
        this.clockInCounter = registry.counter("wems.attendance.clockin");
    }

    public void incrementClockIn() {
        clockInCounter.increment();
    }
}
```

---

## US19.02: Implement Structured Logging

### 1. Architecture Overview
- JSON logs

### 2. Package Structure
- Config

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer
- No changes

### 6. Controller Layer
- No changes

### 7. Configuration

**logback-spring.xml**
```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

### 8. Sample Implementation

**Logging**
```java
log.info("Employee created", kv("employeeId", emp.getId()), kv("badgeId", emp.getBadgeId()));
```

---

## US19.03: Configure Distributed Tracing

### 1. Architecture Overview
- Zipkin/Jaeger integration

### 2. Package Structure
- Config

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer
- No changes

### 6. Controller Layer
- No changes

### 7. Configuration

**pom.xml**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-sleuth-zipkin</artifactId>
</dependency>
```

**application.yml**
```yaml
spring:
  sleuth:
    sampler:
      probability: 1.0
  zipkin:
    base-url: http://localhost:9411
```

### 8. Sample Implementation

**Trace ID in Logs**
```
{"traceId":"abc123","spanId":"def456","message":"Employee created"}
```

---

## US19.04: Create Alerting Rules

### 1. Architecture Overview
- Prometheus alerting

### 2. Package Structure
- External (Prometheus config)

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer
- No changes

### 6. Controller Layer
- No changes

### 7. Configuration

**prometheus-alerts.yml**
```yaml
groups:
  - name: wems
    rules:
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status="500"}[5m]) > 0.05
        annotations:
          summary: High error rate detected
```

### 8. Sample Implementation

**Grafana Dashboard**
```json
{
  "dashboard": {
    "title": "WEMS Metrics",
    "panels": [
      {
        "title": "Clock-In Rate",
        "targets": [{"expr": "rate(wems_attendance_clockin_total[5m])"}]
      }
    ]
  }
}
```

---

# E20: CI/CD & Deployment Automation (4 user stories)

## US20.01: Create CI Pipeline

### 1. Architecture Overview
- GitHub Actions/Jenkins

### 2. Package Structure
- `.github/workflows`

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer
- No changes

### 6. Controller Layer
- No changes

### 7. Configuration

**.github/workflows/ci.yml**
```yaml
name: CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          java-version: '11'
      - name: Build with Maven
        run: mvn clean install
      - name: Run tests
        run: mvn test
```

### 8. Sample Implementation

**Test Coverage**
```yaml
- name: Code coverage
  run: mvn jacoco:report
```

---

## US20.02: Implement Security Scanning

### 1. Architecture Overview
- SAST/DAST scanning

### 2. Package Structure
- CI config

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer
- No changes

### 6. Controller Layer
- No changes

### 7. Configuration

**.github/workflows/ci.yml (updated)**
```yaml
- name: Security scan
  run: mvn org.owasp:dependency-check-maven:check
```

### 8. Sample Implementation

**SonarQube**
```yaml
- name: SonarQube scan
  run: mvn sonar:sonar -Dsonar.host.url=${{ secrets.SONAR_URL }} -Dsonar.login=${{ secrets.SONAR_TOKEN }}
```

---

## US20.03: Configure Container Build

### 1. Architecture Overview
- Docker image build

### 2. Package Structure
- Dockerfile

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer
- No changes

### 6. Controller Layer
- No changes

### 7. Configuration

**Dockerfile**
```dockerfile
FROM openjdk:11-jre-slim
COPY target/wems.jar /app/wems.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/wems.jar"]
```

**.github/workflows/ci.yml (updated)**
```yaml
- name: Build Docker image
  run: docker build -t wems:${{ github.sha }} .
- name: Push to registry
  run: docker push wems:${{ github.sha }}
```

### 8. Sample Implementation

**Docker Compose**
```yaml
version: '3'
services:
  wems:
    image: wems:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/wems
  db:
    image: postgres:13
    environment:
      - POSTGRES_DB=wems
```

---

## US20.04: Implement Deployment Strategy

### 1. Architecture Overview
- Blue-green or canary deployment

### 2. Package Structure
- Kubernetes manifests

### 3. Domain Model
- No changes

### 4. Repository Layer
- No changes

### 5. Service Layer
- No changes

### 6. Controller Layer
- No changes

### 7. Configuration

**k8s/deployment.yml**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: wems
spec:
  replicas: 3
  selector:
    matchLabels:
      app: wems
  template:
    metadata:
      labels:
        app: wems
    spec:
      containers:
      - name: wems
        image: wems:latest
        ports:
        - containerPort: 8080
```

**.github/workflows/deploy.yml**
```yaml
name: Deploy
on:
  workflow_dispatch:
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Kubernetes
        run: kubectl apply -f k8s/
```

### 8. Sample Implementation

**Rollback**
```bash
kubectl rollout undo deployment/wems
```

---

# Conclusion

This Low-Level Technical Design Document provides comprehensive, production-ready specifications for all 79 user stories across the 20 epics of the Warehouse Employee Management System. Each user story includes detailed architecture, package structure, domain models, repository/service/controller layers, configuration, and sample code, ensuring that development teams can implement the system following Spring Boot best practices and industry standards.

**Key Highlights:**
- **Modular Architecture**: Clear separation of concerns with layered architecture
- **Spring Boot Best Practices**: Proper use of annotations, dependency injection, and configuration
- **Security**: Role-based access control, OAuth2/SAML SSO, audit logging
- **Scalability**: Multi-tenant support, caching, pagination
- **Observability**: Prometheus metrics, structured logging, distributed tracing
- **CI/CD**: Automated pipelines, security scanning, containerization, deployment strategies

**Next Steps:**
1. Review and validate technical designs with stakeholders
2. Set up development environment and project scaffolding
3. Implement user stories iteratively following agile methodology
4. Conduct code reviews and testing at each sprint
5. Deploy to staging and production environments using CI/CD pipelines

---

**Document Version:** 1.0  
**Last Updated:** 2024  
**Author:** Senior Software Architect  
**Status:** Ready for Implementation
