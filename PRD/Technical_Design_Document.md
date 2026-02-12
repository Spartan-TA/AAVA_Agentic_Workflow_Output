# Warehouse Employee Management System â Low-Level Technical Design Document

## Overview

This document provides a comprehensive low-level technical design for the Warehouse Employee Management System, covering all 22 user stories derived from 17 epics. Each user story section includes:

- Section title and description
- Spring Boot architecture overview
- Package structure, module definitions, and component breakdown
- Entity design with domain models and relationships
- Service, repository, and controller specifications
- Configuration and security settings
- Integration points (external services, APIs)
- Code snippets or pseudo-code illustrating design patterns

All designs adhere to Spring Boot industry standards and are formatted for easy consumption by Spring Boot developers.

---

## USER STORY 1: Initialize Project Scaffolding

**Description:** Initialize Spring Boot project with Maven, configure base packages, set up core modules (employee, scheduling, attendance, safety), add Flyway/Liquibase for DB migrations, enable Actuator.

### Architecture Overview

- Spring Boot (Maven)
- Modular package structure: `employee`, `scheduling`, `attendance`, `safety`
- Flyway/Liquibase for DB migrations
- Spring Boot Actuator for monitoring

### Package Structure

```
com.warehouse
âââ employee
âââ scheduling
âââ attendance
âââ safety
âââ config
âââ common
âââ Application.java
```

### Module Definitions

- **employee**: Employee CRUD, role management
- **scheduling**: Shift templates, assignments
- **attendance**: Clock in/out, corrections
- **safety**: Incidents, certifications

### Component Breakdown

- `controller`, `service`, `repository`, `model` sub-packages in each module

### Sample Implementation

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
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**Application.java**

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

---

## USER STORY 2: Database Migration Setup

**Description:** Add Flyway or Liquibase for database migrations so schema changes are versioned and repeatable.

### Design Specifications

- Use Flyway (recommended for simplicity) or Liquibase
- Place migration scripts in `src/main/resources/db/migration`
- Versioned SQL scripts (e.g., `V1__init.sql`)

### Configuration

**application.yml**

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

### Sample Migration

**V1__init.sql**

```sql
CREATE TABLE employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    badge_id VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50),
    department VARCHAR(100),
    hire_date DATE,
    status VARCHAR(20),
    deleted BOOLEAN DEFAULT FALSE
);
```

---

## USER STORY 3: Health Endpoint and Monitoring

**Description:** Enable Actuator health endpoint for application monitoring.

### Design Specifications

- Enable Actuator endpoints
- Expose `/actuator/health` and `/actuator/info`

### Configuration

**application.yml**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### Sample Usage

- Access `GET /actuator/health` returns `{"status":"UP"}`

---

## USER STORY 4: Employee CRUD API

**Description:** Create, read, update, delete employee records via API with unique badgeId, soft-delete support.

### Architecture Overview

- RESTful API for employee management
- Soft-delete via `deleted` flag

### Entity Design

**Employee.java**

```java
@Entity
public class Employee {
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String badgeId;
    
    private String name;
    private String role;
    private String department;
    private LocalDate hireDate;
    private String status;
    private boolean deleted = false;
    
    // getters/setters
}
```

### Repository

**EmployeeRepository.java**

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

### Service

**EmployeeService.java**

```java
public Employee create(EmployeeDto dto) { ... }
public Employee update(Long id, EmployeeDto dto) { ... }
public void softDelete(Long id) { ... }
public Page<Employee> list(Pageable pageable) { ... }
```

### Controller

**EmployeeController.java**

```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    
    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody @Valid EmployeeDto dto) { ... }
    
    @GetMapping
    public Page<Employee> list(Pageable pageable) { ... }
    
    @PutMapping("/{id}")
    public Employee update(@PathVariable Long id, @RequestBody EmployeeDto dto) { ... }
    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { ... }
}
```

---

## USER STORY 5: Employee Data Validation and Pagination

**Description:** Enforce unique badgeId, support pagination/filtering on employee queries.

### Design Specifications

- Unique constraint on `badgeId`
- Use `Pageable` for pagination
- Filtering by name, department, role

### Sample Repository Method

```java
Page<Employee> findByNameContainingAndDepartmentAndRoleAndDeletedFalse(
    String name, String department, String role, Pageable pageable);
```

### Controller Example

```java
@GetMapping
public Page<Employee> search(
    @RequestParam Optional<String> name,
    @RequestParam Optional<String> department,
    @RequestParam Optional<String> role,
    Pageable pageable) {
    // call service with filters
}
```

---

## USER STORY 6: Role-Based Access Control (RBAC) Implementation

**Description:** Configure RBAC with roles (ADMIN, HR, SUPERVISOR, WORKER) for endpoint security.

### Design Specifications

- Use Spring Security
- Define roles as enums or constants
- Method-level security with `@PreAuthorize`

### Security Configuration

**SecurityConfig.java**

```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR")
                .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            .and().httpBasic();
    }
}
```

### Controller Example

```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/employees")
public Employee createEmployee(@RequestBody EmployeeDto dto) { ... }
```

---

## USER STORY 7: API Security Toggle

**Description:** Toggle between API key and OAuth2 authentication via configuration.

### Design Specifications

- Use Spring Profiles (`api-key`, `oauth2`)
- Conditional beans for security config

### Configuration

**application-api-key.yml**

```yaml
security:
  mode: api-key
```

**application-oauth2.yml**

```yaml
security:
  mode: oauth2
  oauth2:
    client-id: ...
    client-secret: ...
```

### SecurityConfig Example

```java
@Profile("api-key")
@Configuration
public class ApiKeySecurityConfig extends WebSecurityConfigurerAdapter { ... }

@Profile("oauth2")
@Configuration
public class OAuth2SecurityConfig extends WebSecurityConfigurerAdapter { ... }
```

---

## USER STORY 8: Time & Attendance Clock In/Out

**Description:** Clock in/out endpoints with geofence and device info capture.

### Entity Design

**AttendanceRecord.java**

```java
@Entity
public class AttendanceRecord {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private Employee employee;
    
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private String deviceInfo;
    private String geoLocation;
    
    // getters/setters
}
```

### Controller

```java
@PostMapping("/attendance/clock-in")
public AttendanceRecord clockIn(@RequestBody ClockEventDto dto) { ... }

@PostMapping("/attendance/clock-out")
public AttendanceRecord clockOut(@RequestBody ClockEventDto dto) { ... }
```

### Service

- Validate geofence (optional)
- Capture device info

---

## USER STORY 9: Attendance Corrections Workflow

**Description:** Submit corrections for missed punches with approval workflow.

### Entity Design

**AttendanceCorrection.java**

```java
@Entity
public class AttendanceCorrection {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private Employee employee;
    
    private LocalDateTime originalTime;
    private LocalDateTime correctedTime;
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED
    
    // getters/setters
}
```

### Workflow

- Employee submits correction (`PENDING`)
- Supervisor reviews and approves/rejects

### Controller

```java
@PostMapping("/attendance/corrections")
public AttendanceCorrection submitCorrection(@RequestBody CorrectionDto dto) { ... }

@PutMapping("/attendance/corrections/{id}/approve")
public AttendanceCorrection approve(@PathVariable Long id) { ... }
```

---

## USER STORY 10: Attendance Reporting and Export

**Description:** Export daily attendance totals and corrections to CSV.

### Service

- Aggregate attendance per day
- Export as CSV

### Controller

```java
@GetMapping("/attendance/report")
public void exportAttendanceReport(HttpServletResponse response) {
    // set content type, write CSV to response
}
```

### Utility

```java
public void writeCsv(List<AttendanceRecord> records, OutputStream out) { ... }
```

---

## USER STORY 11: Shift Template Management

**Description:** Create, update, delete recurring shift templates and assign to employees.

### Entity Design

**ShiftTemplate.java**

```java
@Entity
public class ShiftTemplate {
    @Id
    @GeneratedValue
    private Long id;
    
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrencePattern; // e.g., "WEEKLY"
    
    // getters/setters
}
```

**EmployeeShiftAssignment.java**

```java
@Entity
public class EmployeeShiftAssignment {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private Employee employee;
    
    @ManyToOne
    private ShiftTemplate shiftTemplate;
    
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    
    // getters/setters
}
```

### Controller

```java
@PostMapping("/shifts/templates")
public ShiftTemplate createTemplate(@RequestBody ShiftTemplateDto dto) { ... }
```

---

## USER STORY 12: Shift Conflict Detection

**Description:** Detect and prevent scheduling conflicts and double-booking.

### Service

- On assignment, check for overlapping shifts for employee

### Sample Logic

```java
public boolean hasConflict(Employee employee, LocalDate date, LocalTime start, LocalTime end) {
    // Query assignments for overlaps
}
```

### Controller

- Return 409 Conflict if overlap detected

---

## USER STORY 13: Leave Request and Approval

**Description:** Request PTO, sick, unpaid leave with supervisor approval workflow.

### Entity Design

**LeaveRequest.java**

```java
@Entity
public class LeaveRequest {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private Employee employee;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private String type; // PTO, SICK, UNPAID
    private String status; // PENDING, APPROVED, REJECTED
    
    // getters/setters
}
```

### Controller

```java
@PostMapping("/leave/requests")
public LeaveRequest requestLeave(@RequestBody LeaveRequestDto dto) { ... }

@PutMapping("/leave/requests/{id}/approve")
public LeaveRequest approve(@PathVariable Long id) { ... }
```

---

## USER STORY 14: Leave Accrual and Balance Update

**Description:** Update leave balances and flag shifts for coverage when leave approved.

### Entity Design

**LeaveBalance.java**

```java
@Entity
public class LeaveBalance {
    @Id
    @GeneratedValue
    private Long id;
    
    @OneToOne
    private Employee employee;
    
    private int ptoBalance;
    private int sickBalance;
    
    // getters/setters
}
```

### Service

- On leave approval, decrement balance
- Mark affected shifts for coverage

---

## USER STORY 15: Certification Tracking and Alerts

**Description:** Track employee certifications, expirations, upload proof documents with alerts.

### Entity Design

**Certification.java**

```java
@Entity
public class Certification {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private Employee employee;
    
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
    
    // getters/setters
}
```

### Service

- Scheduled job to check for expiring certs
- Send alerts (email/SMS/in-app)

---

## USER STORY 16: Safety Incident Recording and Workflow

**Description:** Record safety incidents, track investigation workflow, generate OSHA reports.

### Entity Design

**SafetyIncident.java**

```java
@Entity
public class SafetyIncident {
    @Id
    @GeneratedValue
    private Long id;
    
    private String description;
    private String severity;
    private String location;
    
    @ManyToMany
    private List<Employee> involvedEmployees;
    
    private String status; // OPEN, INVESTIGATING, RESOLVED
    private LocalDateTime reportedAt;
    
    // getters/setters
}
```

### Controller

```java
@PostMapping("/safety/incidents")
public SafetyIncident reportIncident(@RequestBody SafetyIncidentDto dto) { ... }
```

### Service

- Workflow transitions
- OSHA report export

---

## USER STORY 17: Equipment Assignment and Validation

**Description:** Assign equipment to employees, track check-in/out, validate certifications.

### Entity Design

**Equipment.java**

```java
@Entity
public class Equipment {
    @Id
    @GeneratedValue
    private Long id;
    
    private String type;
    private String serialNumber;
    private String condition;
    
    // getters/setters
}
```

**EquipmentAssignment.java**

```java
@Entity
public class EquipmentAssignment {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private Equipment equipment;
    
    @ManyToOne
    private Employee employee;
    
    private LocalDateTime assignedAt;
    private LocalDateTime returnedAt;
    
    // getters/setters
}
```

### Service

- On assignment, check employee certifications

---

## USER STORY 18: Performance Review Workflow

**Description:** Create performance review cycles, assign goals, track acknowledgements.

### Entity Design

**PerformanceReview.java**

```java
@Entity
public class PerformanceReview {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private Employee employee;
    
    private String cycle; // e.g., Q1-2024
    private String goals;
    private String ratings;
    private String comments;
    private boolean acknowledgedByEmployee;
    private boolean acknowledgedBySupervisor;
    
    // getters/setters
}
```

### Controller

```java
@PostMapping("/reviews")
public PerformanceReview createReview(@RequestBody PerformanceReviewDto dto) { ... }
```

---

## USER STORY 19: Payroll Export Integration

**Description:** Generate payroll-ready files from attendance and leave data.

### Service

- Aggregate attendance and leave
- Map to provider schema
- Export as CSV or provider-specific format

### Controller

```java
@GetMapping("/payroll/export")
public void exportPayroll(HttpServletResponse response) { ... }
```

---

## USER STORY 20: Notifications and Announcements

**Description:** In-app and email/SMS notifications for events with opt-in/out.

### Entity Design

**NotificationPreference.java**

```java
@Entity
public class NotificationPreference {
    @Id
    @GeneratedValue
    private Long id;
    
    @OneToOne
    private Employee employee;
    
    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean inAppEnabled;
    
    // getters/setters
}
```

**Announcement.java**

```java
@Entity
public class Announcement {
    @Id
    @GeneratedValue
    private Long id;
    
    private String message;
    private LocalDateTime createdAt;
    private boolean active;
    
    // getters/setters
}
```

### Service

- Send notifications via selected channels
- Track delivery status

---

## USER STORY 21: Integration Layer for HRIS/WMS

**Description:** Expose REST APIs and connectors for HRIS, WMS, and IDP with SSO.

### Integration Points

- REST APIs for HRIS (employee sync), WMS (department/location), IDP (SSO)
- JWT/OAuth2 security

### Controller

```java
@RestController
@RequestMapping("/api/integration/hris")
public class HRISIntegrationController {
    
    @PostMapping("/employees")
    public void syncEmployee(@RequestBody EmployeeSyncDto dto) { ... }
}
```

### Security

- Secure endpoints with OAuth2/JWT

---

## USER STORY 22: Audit Trail for Sensitive Changes

**Description:** Centralized audit logging for sensitive changes with immutable storage.

### Entity Design

**AuditLog.java**

```java
@Entity
public class AuditLog {
    @Id
    @GeneratedValue
    private Long id;
    
    private String entity;
    private Long entityId;
    private String action; // CREATE, UPDATE, DELETE
    private String actor;
    private LocalDateTime timestamp;
    
    @Lob
    private String beforeState;
    
    @Lob
    private String afterState;
    
    // getters/setters
}
```

### Service

- On sensitive changes, persist audit log entry
- Immutable storage (no update/delete on logs)

### Sample Aspect

```java
@Aspect
@Component
public class AuditTrailAspect {
    
    @AfterReturning(pointcut = "...", returning = "result")
    public void logChange(JoinPoint joinPoint, Object result) {
        // Serialize before/after, save AuditLog
    }
}
```

---

# Appendix

- All endpoints documented with OpenAPI/Swagger
- Exception handling via `@ControllerAdvice`
- Unit and integration tests for all modules
- CI/CD pipeline runs Flyway/Liquibase migrations

---

**End of Technical Design Document**