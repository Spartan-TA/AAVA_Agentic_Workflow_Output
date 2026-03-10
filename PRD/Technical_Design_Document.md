# WAREHOUSE EMPLOYEE MANAGEMENT SYSTEM â LOW-LEVEL TECHNICAL DESIGN DOCUMENT

---

## USER STORY 1: Initialize Project Scaffolding

**Section: Spring Boot Architecture Overview**

Description: The project is structured as a modular Spring Boot 3.x application using Java 17+, Maven, and a layered architecture. Core modules include employee, scheduling, attendance, safety, and shared libraries. Flyway/Liquibase is used for DB migrations. Actuator is enabled on port 8080 for health/metrics.

**Design Specification:**
- Root package: `com.company.warehouse`
- Submodules: `employee`, `attendance`, `schedule`, `safety`, `asset`, `review`, `common`
- Maven multi-module or single module with feature packages
- Flyway/Liquibase for DB migration
- Actuator enabled on port 8080

**Sample Implementation:**
```java
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}
```

**application.yml**
```yaml
server:
  port: 8080

spring:
  application:
    name: warehouse-employee-mgmt
  profiles:
    active: dev
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse
    username: warehouse
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

## USER STORY 2: Configure Database Migration Tool

**Section: Package Structure**

Description: DB migration scripts are stored in `src/main/resources/db/migration` for Flyway or `db/changelog` for Liquibase.

**Design Specification:**
- Flyway: `V1__init.sql`, `V2__employee_table.sql`, etc.
- Liquibase: `db.changelog-master.xml`, incremental changesets

**Sample Implementation (Flyway):**
```
src/main/resources/db/migration/V1__init.sql
```
```sql
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    shift_group VARCHAR(32),
    hire_date DATE,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## USER STORY 3: Enable Actuator Endpoints

**Section: Configuration**

Description: Actuator endpoints for health and metrics are enabled and secured.

**Design Specification:**
- Expose `/actuator/health`, `/actuator/metrics`
- Secure with Spring Security (see US8/US10)

**Sample Implementation:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
  endpoint:
    health:
      show-details: always
```
```java
@Configuration
public class ActuatorSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
          .requestMatcher(EndpointRequest.toAnyEndpoint())
          .authorizeRequests((requests) -> requests.anyRequest().hasRole("ADMIN"))
          .httpBasic();
    }
}
```

---

## USER STORY 4: Create Employee CRUD APIs

**Section: Package Structure**

Description:
- `com.company.warehouse.employee.controller`
- `com.company.warehouse.employee.service`
- `com.company.warehouse.employee.repository`
- `com.company.warehouse.employee.dto`
- `com.company.warehouse.employee.entity`

**Domain Model Design:**
```java
@Entity
@Table(name = "employee", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    private String department;
    private String shiftGroup;
    private LocalDate hireDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status; // ACTIVE, INACTIVE, DELETED

    // getters/setters
}
```

**Repository Layer:**
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    @Query("SELECT e FROM Employee e WHERE e.status <> 'DELETED'")
    Page<Employee> findAllActive(Pageable pageable);
    boolean existsByBadgeId(String badgeId);
}
```

**Service Layer:**
```java
@Service
public class EmployeeService {
    @Transactional
    public Employee createEmployee(EmployeeDto dto) { /* ... */ }
    public Page<Employee> getEmployees(Pageable pageable, EmployeeFilter filter) { /* ... */ }
    @Transactional
    public Employee updateEmployee(Long id, EmployeeDto dto) { /* ... */ }
    @Transactional
    public void softDeleteEmployee(Long id) { /* ... */ }
}
```

**Controller Layer:**
```java
@RestController
@RequestMapping("/api/employees")
@Validated
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { /* ... */ }
    @GetMapping
    public Page<EmployeeDto> list(@PageableDefault Pageable pageable, EmployeeFilter filter) { /* ... */ }
    @GetMapping("/{id}")
    public EmployeeDto get(@PathVariable Long id) { /* ... */ }
    @PutMapping("/{id}")
    public EmployeeDto update(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) { /* ... */ }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { /* ... */ }
}
```

**API Specification (OpenAPI):**
```yaml
paths:
  /api/employees:
    get:
      summary: List employees
      parameters:
        - in: query
          name: page
          schema: { type: integer }
        - in: query
          name: size
          schema: { type: integer }
      responses:
        '200':
          description: Paginated employee list
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/EmployeePage'
    post:
      summary: Create employee
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/EmployeeDto'
      responses:
        '201':
          description: Created
```

---

## USER STORY 5: Enforce Unique Badge ID

**Section: Domain Model Design**

Description: Enforce uniqueness at both DB and service/API layers.

**Design Specification:**
- DB: `UNIQUE` constraint on `badge_id`
- Service: Check `existsByBadgeId` before create/update

**Sample Implementation:**
```java
if (employeeRepository.existsByBadgeId(dto.getBadgeId())) {
    throw new DuplicateBadgeIdException("Badge ID already exists");
}
```

**Database Schema:**
```sql
ALTER TABLE employee ADD CONSTRAINT unique_badge_id UNIQUE (badge_id);
```

---

## USER STORY 6: Implement Soft Delete for Employees

**Section: Domain Model Design**

Description: Use a `status` field (e.g., ACTIVE, INACTIVE, DELETED) instead of physical delete.

**Design Specification:**
- All queries filter out `DELETED`
- Restore by setting status back to ACTIVE

**Sample Implementation:**
```java
public void softDeleteEmployee(Long id) {
    Employee emp = employeeRepository.findById(id).orElseThrow();
    emp.setStatus(Status.DELETED);
    employeeRepository.save(emp);
}

public void restoreEmployee(Long id) {
    Employee emp = employeeRepository.findById(id).orElseThrow();
    emp.setStatus(Status.ACTIVE);
    employeeRepository.save(emp);
}
```

---

## USER STORY 7: Provide OpenAPI Schemas with Examples

**Section: Configuration**

Description: Use `springdoc-openapi` for auto-generation.

**Design Specification:**
- Add dependency: `org.springdoc:springdoc-openapi-starter-webmvc-ui`
- Annotate DTOs with examples

**Sample Implementation:**
```java
@Schema(example = "{"badgeId":"12345","name":"Jane Doe","role":"WORKER"}")
public class EmployeeDto { ... }
```

**application.yml**
```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

---

## USER STORY 8: Implement Role-Based Access Control (RBAC)

**Section: Security Configuration**

Description: Use Spring Security with roles: ADMIN, HR, SUPERVISOR, WORKER.

**Design Specification:**
- Users have roles stored in DB or external IdP
- Method-level security with `@PreAuthorize`

**Sample Implementation:**
```java
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter { ... }

@PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
public void createEmployee(...) { ... }
```

---

## USER STORY 9: Support API Key/OAuth2 Toggle

**Section: Security Configuration**

Description: Configurable authentication mode via `application.yml` or Spring profiles.

**Design Specification:**
- API Key: Custom filter checks header
- OAuth2: Use Spring Security OAuth2

**Sample Implementation:**
```yaml
security:
  mode: apikey # or oauth2
```
```java
if ("apikey".equals(securityMode)) {
    // Register API key filter
} else {
    // Configure OAuth2
}
```

---

## USER STORY 10: Secure Endpoints and Methods

**Section: Security Configuration**

Description: Use `@PreAuthorize` for method security and row-level filtering in queries.

**Design Specification:**
- Annotate service methods
- Filter data by user/team

**Sample Implementation:**
```java
@PreAuthorize("hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(authentication, #employeeId)")
public Employee getEmployee(Long employeeId) { ... }
```

---

## USER STORY 11: Implement Time & Attendance Clock In/Out

**Section: Domain Model Design**

Description:
- Entity: `AttendanceEvent` (employee, timestamp, type, device, location, shiftId)

**Design Specification:**
- Geofencing: Validate location within allowed area
- Device capture: Store device info

**Sample Implementation:**
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime timestamp;
    @Enumerated(EnumType.STRING)
    private EventType type; // CLOCK_IN, CLOCK_OUT
    private String deviceId;
    private String location;
    private Long shiftId;
}
```

**Controller:**
```java
@PostMapping("/attendance/clock-in")
public ResponseEntity<?> clockIn(@RequestBody ClockEventDto dto) { ... }
```

---

## USER STORY 12: Handle Missed Punches and Corrections

**Section: Domain Model Design**

Description:
- Entity: `AttendanceCorrectionRequest` (employee, originalEvent, requestedChange, status, approver)

**Design Specification:**
- Workflow: SUBMITTED â APPROVED/REJECTED

**Sample Implementation:**
```java
@Entity
public class AttendanceCorrectionRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private AttendanceEvent originalEvent;
    private LocalDateTime requestedTime;
    @Enumerated(EnumType.STRING)
    private Status status; // SUBMITTED, APPROVED, REJECTED
    @ManyToOne
    private Employee approver;
}
```

---

## USER STORY 13: Calculate Daily Totals and Export Reports

**Section: Service Layer**

Description:
- Calculate hours per day per employee
- Export CSV

**Sample Implementation:**
```java
public DailyAttendanceReport calculateDailyTotals(LocalDate date) { ... }
public Resource exportAttendanceCsv(LocalDate start, LocalDate end) { ... }
```

**Controller:**
```java
@GetMapping("/attendance/report")
public ResponseEntity<Resource> exportReport(@RequestParam LocalDate start, @RequestParam LocalDate end) { ... }
```

---

## USER STORY 14: Create Shift Templates and Schedules

**Section: Domain Model Design**

Description:
- Entity: `ShiftTemplate`, `ShiftAssignment`

**Sample Implementation:**
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrenceRule; // e.g., "FREQ=WEEKLY;BYDAY=MO,WE,FR"
}

@Entity
public class ShiftAssignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ShiftTemplate template;
    private LocalDate date;
}
```

---

## USER STORY 15: Bulk Assign Shifts and Generate Audit Entries

**Section: Service Layer**

Description:
- Bulk assign shifts
- Log audit entries

**Sample Implementation:**
```java
@Transactional
public void bulkAssignShifts(List<Long> employeeIds, Long shiftTemplateId, LocalDate date) {
    // assign shifts
    // create audit entries
}
```

**Audit Entity:**
```java
@Entity
public class AuditEntry {
    @Id @GeneratedValue
    private Long id;
    private String entityType;
    private Long entityId;
    private String action;
    private String actor;
    private LocalDateTime timestamp;
    private String beforeState;
    private String afterState;
}
```

---

## USER STORY 16: View Upcoming Shifts

**Section: Controller Layer**

Description:
- Endpoint for personal/team schedule

**Sample Implementation:**
```java
@GetMapping("/schedule/upcoming")
public List<ShiftAssignmentDto> getUpcomingShifts(@RequestParam(required = false) Long employeeId) { ... }
```

---

## USER STORY 17: Request and Approve Leave

**Section: Domain Model Design**

Description:
- Entity: `LeaveRequest` (employee, type, startDate, endDate, status, approver, balance)

**Sample Implementation:**
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
    private Status status; // REQUESTED, APPROVED, DENIED
    @ManyToOne
    private Employee approver;
}
```

**Controller:**
```java
@PostMapping("/leave/request")
public LeaveRequestDto requestLeave(@RequestBody LeaveRequestDto dto) { ... }

@PutMapping("/leave/{id}/approve")
public LeaveRequestDto approveLeave(@PathVariable Long id) { ... }
```

---

## USER STORY 18: Auto-Flag Scheduled Shifts for Coverage

**Section: Service Layer**

Description:
- When leave is approved, flag affected shifts for coverage

**Sample Implementation:**
```java
public void flagShiftsForCoverage(Long employeeId, LocalDate start, LocalDate end) {
    // find shifts, set coverageFlag = true
}
```

---

## USER STORY 19: Track Certification Expiry and Block Assignments

**Section: Domain Model Design**

Description:
- Entity: `Certification` (employee, type, expiryDate, documentUrl)

**Sample Implementation:**
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type;
    private LocalDate expiryDate;
    private String documentUrl;
}
```

**Service:**
```java
public boolean isCertificationValid(Long employeeId, String certType) { ... }
```

---

## USER STORY 20: Record Safety Incidents and Workflow

**Section: Domain Model Design**

Description:
- Entity: `SafetyIncident` (severity, location, description, involvedEmployees, status)

**Sample Implementation:**
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
    @Enumerated(EnumType.STRING)
    private Status status; // OPEN, INVESTIGATING, RESOLVED
}
```

**Controller:**
```java
@PostMapping("/safety/incidents")
public SafetyIncidentDto recordIncident(@RequestBody SafetyIncidentDto dto) { ... }
```

---

## USER STORY 21: Assign and Track Equipment & Assets

**Section: Domain Model Design**

Description:
- Entity: `Asset`, `AssetAssignment`

**Sample Implementation:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private String condition;
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
}
```

**Controller:**
```java
@PostMapping("/assets/assign")
public AssetAssignmentDto assignAsset(@RequestBody AssetAssignmentDto dto) { ... }
```

---

## USER STORY 22: Performance Review Cycle Management

**Section: Domain Model Design**

Description:
- Entity: `PerformanceReview` (employee, cycle, goals, ratings, comments, status, pdfUrl)

**Sample Implementation:**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String cycle; // e.g., "2024-Q1"
    private String goals;
    private String ratings;
    private String comments;
    @Enumerated(EnumType.STRING)
    private Status status; // DRAFT, SUBMITTED, ACKNOWLEDGED
    private String pdfUrl;
}
```

**Controller:**
```java
@PostMapping("/reviews")
public PerformanceReviewDto createReview(@RequestBody PerformanceReviewDto dto) { ... }

@GetMapping("/reviews/{id}/pdf")
public ResponseEntity<Resource> downloadPdf(@PathVariable Long id) { ... }
```

---

## GENERAL INTEGRATION POINTS

- HRIS/WMS/IDP: REST clients in `com.company.warehouse.integration`
- Notifications: Use Spring Events, email/SMS providers
- Audit: Centralized via `AuditEntry` entity and aspect

---

## GENERAL ERROR HANDLING

- Use `@ControllerAdvice` for global exception handling
- Return standardized error responses

---

## GENERAL OPENAPI/Swagger

- All endpoints documented with `@Operation`, `@Parameter`, `@Schema`
- Example:
```java
@Operation(summary = "Create employee", responses = {
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = EmployeeDto.class)))
})
```

---

## DATABASE SCHEMA

- All entities use `@Table`, `@Column`, constraints as per JPA
- Indexes on foreign keys, badgeId, etc.

---

## SPRING PROFILES & FEATURE FLAGS

- Use `@Profile` for dev/test/prod beans
- Feature toggles via `application.yml` or LaunchDarkly integration

---

## SUMMARY

This document provides a production-grade, low-level technical design for all 22 user stories, with clear package structure, domain models, repository/service/controller layers, configuration, security, integration, code snippets, database schema, and OpenAPI specs. All sections follow Spring Boot 3.x and Java 17+ best practices for rapid, maintainable, and secure development.

---

## DOCUMENT METADATA

**Document Version:** 1.0
**Created Date:** 2024
**Author:** Senior Software Architect
**Project:** Warehouse Employee Management System
**Technology Stack:** Spring Boot 3.x, Java 17+, PostgreSQL, Maven, Flyway/Liquibase
**Architecture Pattern:** Layered Architecture with Domain-Driven Design principles