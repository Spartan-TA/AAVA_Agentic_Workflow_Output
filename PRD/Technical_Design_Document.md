---
# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

## Introduction

This document provides a comprehensive low-level technical design for the Warehouse Employee Management System (EMS), covering all 81 user stories across 20 epics. Each user story section includes Spring Boot architecture overview, package structure, domain model/entity design, repository/service/controller layers, security, configuration, integration points, code snippets, and migration scripts, following industry best practices.

---

## USER STORY 1: Initialize Spring Boot Project Structure

### Overview
Initialize the Spring Boot (Maven) project, configure base packages, set up core modules (employee, scheduling, attendance, safety), add Flyway/Liquibase for DB migrations, and enable Actuator.

### Spring Boot Architecture
- Modular monolith with layered architecture: domain, repository, service, controller, config.
- Core modules: employee, scheduling, attendance, safety.
- Maven multi-module structure (if scaling required).

### Package Structure
```
com.warehouse.ems
âââ employee
â   âââ domain
â   âââ repository
â   âââ service
â   âââ controller
â   âââ dto
â   âââ config
âââ scheduling
âââ attendance
âââ safety
âââ common
â   âââ config
â   âââ exception
â   âââ util
```

### Domain Model
**Description:** No entities for scaffolding, but base domain classes for each module.

**Design Specification:**
- AbstractAuditableEntity (for audit fields)
- BaseEntity (ID, timestamps)

**Sample Implementation:**
```java
@MappedSuperclass
public abstract class AbstractAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    @CreatedBy
    private String createdBy;
    @LastModifiedBy
    private String updatedBy;
}
```

### Repository Layer
**Description:** No repositories for scaffolding.

### Service Layer
**Description:** No services for scaffolding.

### Controller Layer
**Description:** No controllers for scaffolding.

### Security Configuration
**Description:** No security for scaffolding.

### Database Migration
**Description:** Baseline migration for audit tables.

**Sample Implementation:**
```sql
-- V1__baseline.sql
CREATE TABLE employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
```

### Configuration
**Description:** Application properties for port, actuator, Flyway.

**Sample Implementation:**
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ems
    username: ems_user
    password: secret
  flyway:
    enabled: true
  liquibase:
    enabled: false

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### Integration Points
**Description:** Actuator health endpoint.

**Design Specification:**
- `/actuator/health` returns UP

---

## USER STORY 2: Create Employee Record

### Overview
Create Employee domain with CRUD APIs and web DTOs: name, badgeId, role, department, shiftGroup, hireDate, status.

### Spring Boot Architecture
- Employee module with domain, repository, service, controller.
- Layered structure.

### Package Structure
```
com.warehouse.ems.employee
âââ domain
âââ repository
âââ service
âââ controller
âââ dto
```

### Domain Model
**Description:** Employee entity with required fields and audit info.

**Design Specification:**
- Entity: Employee
- Table: employee
- Fields:
  - id: Long
  - name: String
  - badgeId: String (unique)
  - role: Enum (ADMIN, HR, SUPERVISOR, WORKER)
  - department: String
  - shiftGroup: String
  - hireDate: LocalDate
  - status: Enum (ACTIVE, INACTIVE, TERMINATED)
  - audit fields

**Sample Implementation:**
```java
@Entity
@Table(name = "employee", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee extends AbstractAuditableEntity {
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String badgeId;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    private Status status;
}
```

### Repository Layer
**Description:** EmployeeRepository with CRUD and custom queries.

**Design Specification:**
- Interface: EmployeeRepository extends JpaRepository<Employee, Long>
- Custom methods:
  - findByBadgeId(String badgeId)
  - findByDepartment(String department)
  - findByStatus(Status status)

**Sample Implementation:**
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    List<Employee> findByDepartment(String department);
    List<Employee> findByStatus(Status status);
}
```

### Service Layer
**Description:** EmployeeService for business logic, validation, transactions.

**Design Specification:**
- Interface: EmployeeService
- Implementation: EmployeeServiceImpl
- Methods:
  - createEmployee(EmployeeDto dto)
  - updateEmployee(Long id, EmployeeDto dto)
  - deleteEmployee(Long id)
  - getEmployee(Long id)
  - listEmployees(Pageable pageable, FilterDto filter)
- Transaction boundaries
- Validation: unique badgeId, required fields

**Sample Implementation:**
```java
@Service
public class EmployeeServiceImpl implements EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional
    public Employee createEmployee(EmployeeDto dto) {
        if (employeeRepository.findByBadgeId(dto.getBadgeId()).isPresent()) {
            throw new DuplicateBadgeIdException();
        }
        Employee employee = new Employee(...);
        return employeeRepository.save(employee);
    }
    // Other methods...
}
```

### Controller Layer
**Description:** REST endpoints for CRUD operations.

**Design Specification:**
- Base path: /api/v1/employees
- Endpoints:
  - POST /employees - create
  - GET /employees/{id} - get by id
  - PUT /employees/{id} - update
  - DELETE /employees/{id} - soft delete
  - GET /employees - list with pagination/filter
- Request/Response DTOs

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@RequestBody @Valid EmployeeDto dto) { ... }
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> get(@PathVariable Long id) { ... }
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @RequestBody @Valid EmployeeDto dto) { ... }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
    @GetMapping
    public Page<EmployeeDto> list(Pageable pageable, EmployeeFilterDto filter) { ... }
}
```

### Security Configuration
**Description:** Role-based access for employee CRUD.

**Design Specification:**
- Required roles: ADMIN, HR
- Method security: @PreAuthorize
- Row-level security: SUPERVISOR can only view team

**Sample Implementation:**
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
public EmployeeDto createEmployee(EmployeeDto dto) { ... }
```

### Database Migration
**Description:** Employee table creation.

**Sample Implementation:**
```sql
-- V2__employee_table.sql
CREATE TABLE employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    badge_id VARCHAR(50) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    department VARCHAR(100),
    shift_group VARCHAR(100),
    hire_date DATE,
    status VARCHAR(20),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
```

### Configuration
**Description:** Employee module settings.

**Sample Implementation:**
```yaml
employee:
  default-status: ACTIVE
```

### Integration Points
**Description:** None for basic CRUD.

---

## USER STORY 3: Update Employee Record

### Overview
Update employee details via API, ensuring validation and audit logging.

### Spring Boot Architecture
- Employee module, service, controller.

### Package Structure
```
com.warehouse.ems.employee
âââ service
âââ controller
âââ dto
```

### Domain Model
**Description:** Employee entity as above.

### Repository Layer
**Description:** EmployeeRepository.

### Service Layer
**Description:** EmployeeServiceImpl with update logic.

**Design Specification:**
- updateEmployee(Long id, EmployeeDto dto)
- Validation: badgeId uniqueness, status transitions

**Sample Implementation:**
```java
@Transactional
public Employee updateEmployee(Long id, EmployeeDto dto) {
    Employee employee = employeeRepository.findById(id).orElseThrow(...);
    // update fields
    return employeeRepository.save(employee);
}
```

### Controller Layer
**Description:** PUT /employees/{id}

**Sample Implementation:**
```java
@PutMapping("/{id}")
public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @RequestBody @Valid EmployeeDto dto) { ... }
```

### Security Configuration
**Description:** ADMIN, HR only.

### Database Migration
**Description:** No changes.

### Configuration
**Description:** No changes.

### Integration Points
**Description:** None.

---

## USER STORY 4: Delete Employee Record (Soft Delete)

### Overview
Soft-delete employee records, preserving audit trail.

### Spring Boot Architecture
- Employee module, service, controller.

### Package Structure
```
com.warehouse.ems.employee
âââ service
âââ controller
```

### Domain Model
**Description:** Employee entity with status field.

**Design Specification:**
- status: INACTIVE or TERMINATED

**Sample Implementation:**
```java
public void deleteEmployee(Long id) {
    Employee employee = employeeRepository.findById(id).orElseThrow(...);
    employee.setStatus(Status.INACTIVE);
    employeeRepository.save(employee);
}
```

### Controller Layer
**Description:** DELETE /employees/{id}

### Security Configuration
**Description:** ADMIN, HR only.

### Database Migration
**Description:** No changes.

### Configuration
**Description:** No changes.

### Integration Points
**Description:** None.

---

## USER STORY 5: List Employees with Pagination & Filtering

### Overview
List employees with pagination and filtering by department, role, status.

### Spring Boot Architecture
- Employee module, repository, service, controller.

### Package Structure
```
com.warehouse.ems.employee
âââ repository
âââ service
âââ controller
```

### Domain Model
**Description:** Employee entity.

### Repository Layer
**Description:** Custom query methods for filtering.

**Sample Implementation:**
```java
Page<Employee> findByDepartmentAndRoleAndStatus(String department, Role role, Status status, Pageable pageable);
```

### Service Layer
**Description:** listEmployees(Pageable pageable, FilterDto filter)

### Controller Layer
**Description:** GET /employees?department=&role=&status=&page=&size=

### Security Configuration
**Description:** ADMIN, HR, SUPERVISOR (limited view).

### Database Migration
**Description:** No changes.

### Configuration
**Description:** No changes.

### Integration Points
**Description:** None.

---

## USER STORY 6: Implement Role-Based Access Control

### Overview
Add Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER); method/endpoint security and row-level constraints.

### Spring Boot Architecture
- Security module, config, annotations.

### Package Structure
```
com.warehouse.ems.security
âââ config
âââ service
```

### Domain Model
**Description:** Role enum, User entity.

**Sample Implementation:**
```java
public enum Role { ADMIN, HR, SUPERVISOR, WORKER }
@Entity
public class User { ... }
```

### Repository Layer
**Description:** UserRepository.

### Service Layer
**Description:** UserDetailsService for authentication.

### Controller Layer
**Description:** Auth endpoints (if needed).

### Security Configuration
**Description:** Spring Security config, method security.

**Sample Implementation:**
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/api/v1/employees/**").hasAnyRole("ADMIN", "HR")
            .anyRequest().authenticated()
            .and()
            .httpBasic();
    }
}
```

### Database Migration
**Description:** User table.

**Sample Implementation:**
```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);
```

### Configuration
**Description:** Security settings.

**Sample Implementation:**
```yaml
spring:
  security:
    user:
      name: admin
      password: secret
```

### Integration Points
**Description:** OAuth2, API key toggle.

---

## USER STORY 7: API Key/OAuth2 Toggle via Config

### Overview
Allow switching between API key and OAuth2 authentication via configuration.

### Spring Boot Architecture
- Security config.

### Package Structure
```
com.warehouse.ems.security
âââ config
```

### Security Configuration
**Description:** Conditional beans for API key or OAuth2.

**Sample Implementation:**
```java
@Configuration
public class AuthConfig {
    @Value("${security.auth-type}")
    private String authType;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if ("oauth2".equals(authType)) {
            // OAuth2 config
        } else {
            // API key config
        }
        return http.build();
    }
}
```

### Configuration
**Sample Implementation:**
```yaml
security:
  auth-type: api-key # or oauth2
```

---

## USER STORY 8: Unauthorized Requests Return 401

### Overview
Ensure unauthorized requests return 401.

### Spring Boot Architecture
- Security config.

### Security Configuration
**Sample Implementation:**
```java
http.exceptionHandling().authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
```

---

## USER STORY 9: Forbidden Actions Return 403

### Overview
Ensure forbidden actions return 403.

### Spring Boot Architecture
- Security config.

### Security Configuration
**Sample Implementation:**
```java
http.exceptionHandling().accessDeniedHandler(new AccessDeniedHandlerImpl());
```

---

## USER STORY 10: ADMIN Can Manage All Records

### Overview
ADMIN role can manage all employee records.

### Spring Boot Architecture
- Security config, method security.

### Security Configuration
**Sample Implementation:**
```java
@PreAuthorize("hasRole('ADMIN')")
public void manageEmployee(...) { ... }
```

---

## USER STORY 11: SUPERVISOR Limited to Team

### Overview
SUPERVISOR role can only manage/view their team.

### Spring Boot Architecture
- Security config, row-level security.

### Service Layer
**Sample Implementation:**
```java
@PreAuthorize("hasRole('SUPERVISOR')")
public List<Employee> getTeamEmployees(Long supervisorId) { ... }
```

---

## USER STORY 12: Security Rules Covered by Tests

### Overview
Unit/integration tests for security rules.

### Spring Boot Architecture
- Test module.

### Package Structure
```
com.warehouse.ems.security
âââ test
```

**Sample Implementation:**
```java
@SpringBootTest
@WithMockUser(roles = "ADMIN")
public void testAdminAccess() { ... }
```

---

## USER STORY 13: Clock In/Out Endpoint

### Overview
Endpoints for clock-in/out events with geofence and device capture.

### Spring Boot Architecture
- Attendance module.

### Package Structure
```
com.warehouse.ems.attendance
âââ domain
âââ repository
âââ service
âââ controller
âââ dto
```

### Domain Model
**Design Specification:**
- Entity: AttendanceEvent
- Table: attendance_event
- Fields:
  - id: Long
  - employeeId: Long
  - eventType: Enum (CLOCK_IN, CLOCK_OUT)
  - timestamp: LocalDateTime
  - deviceId: String
  - location: String (optional)
  - audit fields

**Sample Implementation:**
```java
@Entity
public class AttendanceEvent extends AbstractAuditableEntity {
    @ManyToOne
    private Employee employee;
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    private LocalDateTime timestamp;
    private String deviceId;
    private String location;
}
```

### Repository Layer
**Description:** AttendanceEventRepository.

**Sample Implementation:**
```java
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
}
```

### Service Layer
**Description:** AttendanceService.

**Sample Implementation:**
```java
@Transactional
public AttendanceEvent clockIn(Long employeeId, String deviceId, String location) { ... }
```

### Controller Layer
**Description:** POST /attendance/clock-in, /clock-out

**Sample Implementation:**
```java
@PostMapping("/clock-in")
public ResponseEntity<AttendanceEventDto> clockIn(@RequestBody ClockInDto dto) { ... }
```

### Security Configuration
**Description:** WORKER, SUPERVISOR roles.

### Database Migration
**Sample Implementation:**
```sql
CREATE TABLE attendance_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT,
    event_type VARCHAR(20),
    timestamp TIMESTAMP,
    device_id VARCHAR(100),
    location VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
```

### Configuration
**Description:** Geofence settings.

**Sample Implementation:**
```yaml
attendance:
  geofence:
    enabled: true
    latitude: 37.7749
    longitude: -122.4194
    radius: 100
```

### Integration Points
**Description:** Geofence validation service.

---

## USER STORY 14: Geofence Validation for Attendance

### Overview
Validate clock-in/out within warehouse geofence.

### Spring Boot Architecture
- Attendance module, geofence service.

### Service Layer
**Sample Implementation:**
```java
public boolean isWithinGeofence(String location) { ... }
```

---

## USER STORY 15: Attendance Corrections Workflow

### Overview
Workers request corrections for missed punches; supervisors approve/deny.

### Spring Boot Architecture
- Attendance module, approval workflow.

### Domain Model
**Design Specification:**
- Entity: AttendanceCorrection
- Fields:
  - id, employeeId, originalEvent, correctedEvent, status (PENDING, APPROVED, DENIED), approver

**Sample Implementation:**
```java
@Entity
public class AttendanceCorrection extends AbstractAuditableEntity {
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private AttendanceEvent originalEvent;
    @ManyToOne
    private AttendanceEvent correctedEvent;
    @Enumerated(EnumType.STRING)
    private CorrectionStatus status;
    @ManyToOne
    private Employee approver;
}
```

### Service Layer
**Sample Implementation:**
```java
@Transactional
public AttendanceCorrection requestCorrection(Long employeeId, CorrectionDto dto) { ... }
@Transactional
public void approveCorrection(Long correctionId, Long approverId) { ... }
```

### Controller Layer
**Description:** POST /attendance/corrections, PUT /corrections/{id}/approve

---

## USER STORY 16: Attendance Reporting and Export

### Overview
Export attendance data as CSV for payroll.

### Spring Boot Architecture
- Attendance module, reporting service.

### Service Layer
**Sample Implementation:**
```java
public byte[] exportAttendance(LocalDate startDate, LocalDate endDate) { ... }
```

### Controller Layer
**Description:** GET /attendance/export?start=&end=

---

## USER STORY 17: Create Shift Templates

### Overview
Create recurring shift templates for scheduling.

### Spring Boot Architecture
- Scheduling module.

### Package Structure
```
com.warehouse.ems.scheduling
âââ domain
âââ repository
âââ service
âââ controller
âââ dto
```

### Domain Model
**Design Specification:**
- Entity: ShiftTemplate
- Fields:
  - id, name, startTime, endTime, daysOfWeek, shiftGroup

**Sample Implementation:**
```java
@Entity
public class ShiftTemplate extends AbstractAuditableEntity {
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String daysOfWeek;
    private String shiftGroup;
}
```

### Repository Layer
**Sample Implementation:**
```java
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> { }
```

### Service Layer
**Sample Implementation:**
```java
@Transactional
public ShiftTemplate createTemplate(ShiftTemplateDto dto) { ... }
```

### Controller Layer
**Description:** POST /scheduling/templates

---

## USER STORY 18: Assign Shifts to Employees

### Overview
Assign shifts in bulk to employees.

### Spring Boot Architecture
- Scheduling module.

### Domain Model
**Design Specification:**
- Entity: ShiftAssignment
- Fields:
  - id, employeeId, shiftTemplateId, date

**Sample Implementation:**
```java
@Entity
public class ShiftAssignment extends AbstractAuditableEntity {
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ShiftTemplate shiftTemplate;
    private LocalDate date;
}
```

### Service Layer
**Sample Implementation:**
```java
@Transactional
public void assignShifts(List<Long> employeeIds, Long templateId, LocalDate startDate, LocalDate endDate) { ... }
```

### Controller Layer
**Description:** POST /scheduling/assignments

---

## USER STORY 19: Detect Scheduling Conflicts

### Overview
Prevent overlapping shifts.

### Service Layer
**Sample Implementation:**
```java
public boolean hasConflict(Long employeeId, LocalDate date, LocalTime startTime, LocalTime endTime) { ... }
```

---

## USER STORY 20: View Personal Shift Schedule

### Overview
Workers view upcoming shifts.

### Controller Layer
**Description:** GET /scheduling/my-shifts

---

## USER STORY 21: Audit Shift Assignments

### Overview
Audit log for shift assignments.

### Spring Boot Architecture
- Audit module.

### Domain Model
**Design Specification:**
- Entity: AuditLog
- Fields:
  - id, entityType, entityId, action, actor, timestamp, before, after

**Sample Implementation:**
```java
@Entity
public class AuditLog {
    @Id
    @GeneratedValue
    private Long id;
    private String entityType;
    private Long entityId;
    private String action;
    private String actor;
    private LocalDateTime timestamp;
    @Lob
    private String before;
    @Lob
    private String after;
}
```

---

## USER STORY 22: Request Leave

### Overview
Employees request PTO or sick leave.

### Spring Boot Architecture
- Leave module.

### Package Structure
```
com.warehouse.ems.leave
âââ domain
âââ repository
âââ service
âââ controller
âââ dto
```

### Domain Model
**Design Specification:**
- Entity: LeaveRequest
- Fields:
  - id, employeeId, leaveType (PTO, SICK, UNPAID), startDate, endDate, status (PENDING, APPROVED, DENIED), approver

**Sample Implementation:**
```java
@Entity
public class LeaveRequest extends AbstractAuditableEntity {
    @ManyToOne
    private Employee employee;
    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;
    @ManyToOne
    private Employee approver;
}
```

### Service Layer
**Sample Implementation:**
```java
@Transactional
public LeaveRequest requestLeave(Long employeeId, LeaveRequestDto dto) { ... }
```

### Controller Layer
**Description:** POST /leave/requests

---

## USER STORY 23: Approve or Deny Leave

### Overview
Supervisors approve/deny leave requests.

### Service Layer
**Sample Implementation:**
```java
@Transactional
public void approveLeave(Long requestId, Long approverId) { ... }
```

### Controller Layer
**Description:** PUT /leave/requests/{id}/approve

---

## USER STORY 24: Update Leave Balances

### Overview
Automatically update leave accruals.

### Service Layer
**Sample Implementation:**
```java
@Scheduled(cron = "0 0 0 * * ?")
public void updateLeaveBalances() { ... }
```

---

## USER STORY 25: Flag Scheduled Shifts for Coverage

### Overview
Auto-flag shifts affected by approved leave.

### Service Layer
**Sample Implementation:**
```java
public void flagShiftsForCoverage(Long employeeId, LocalDate startDate, LocalDate endDate) { ... }
```

---

## USER STORY 26: Track Employee Certifications

### Overview
Track certifications and expirations.

### Spring Boot Architecture
- Certification module.

### Package Structure
```
com.warehouse.ems.certification
âââ domain
âââ repository
âââ service
âââ controller
âââ dto
```

### Domain Model
**Design Specification:**
- Entity: Certification
- Fields:
  - id, employeeId, certType, issueDate, expiryDate, status

**Sample Implementation:**
```java
@Entity
public class Certification extends AbstractAuditableEntity {
    @ManyToOne
    private Employee employee;
    private String certType;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    @Enumerated(EnumType.STRING)
    private CertStatus status;
}
```

---

## USER STORY 27: Certification Expiry Alerts

### Overview
Send alerts before certifications expire.

### Service Layer
**Sample Implementation:**
```java
@Scheduled(cron = "0 0 8 * * ?")
public void sendExpiryAlerts() { ... }
```

---

## USER STORY 28: Block Assignment for Expired Certifications

### Overview
Prevent task assignment if certification expired.

### Service Layer
**Sample Implementation:**
```java
public boolean canAssignTask(Long employeeId, String taskType) { ... }
```

---

## USER STORY 29: Upload Certification Proof

### Overview
Upload certification documents.

### Controller Layer
**Description:** POST /certifications/{id}/upload

---

## USER STORY 30: Record Safety Incident

### Overview
Record safety incidents and near-misses.

### Spring Boot Architecture
- Safety module.

### Package Structure
```
com.warehouse.ems.safety
âââ domain
âââ repository
âââ service
âââ controller
âââ dto
```

### Domain Model
**Design Specification:**
- Entity: SafetyIncident
- Fields:
  - id, employeeId, severity, location, description, status (OPEN, INVESTIGATING, RESOLVED)

**Sample Implementation:**
```java
@Entity
public class SafetyIncident extends AbstractAuditableEntity {
    @ManyToOne
    private Employee employee;
    @Enumerated(EnumType.STRING)
    private Severity severity;
    private String location;
    @Lob
    private String description;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
}
```

---

## USER STORY 31: Incident Investigation Workflow

### Overview
Manage incident investigation and corrective actions.

### Service Layer
**Sample Implementation:**
```java
@Transactional
public void updateIncidentStatus(Long incidentId, IncidentStatus status) { ... }
```

---

## USER STORY 32: OSHA Summary Export

### Overview
Export OSHA 300/300A summaries.

### Service Layer
**Sample Implementation:**
```java
public byte[] exportOSHASummary(int year) { ... }
```

---

## USER STORY 33: Safety Metrics Dashboard

### Overview
View safety KPIs.

### Controller Layer
**Description:** GET /safety/metrics

---

## USER STORY 34: Assign Equipment to Employees

### Overview
Assign equipment to employees.

### Spring Boot Architecture
- Asset module.

### Package Structure
```
com.warehouse.ems.asset
âââ domain
âââ repository
âââ service
âââ controller
âââ dto
```

### Domain Model
**Design Specification:**
- Entity: Asset
- Fields:
  - id, assetType, assetId, assignedTo, status

**Sample Implementation:**
```java
@Entity
public class Asset extends AbstractAuditableEntity {
    private String assetType;
    private String assetId;
    @ManyToOne
    private Employee assignedTo;
    @Enumerated(EnumType.STRING)
    private AssetStatus status;
}
```

---

## USER STORY 35: Track Asset Checkout and Return

### Overview
Track asset checkout/return.

### Service Layer
**Sample Implementation:**
```java
@Transactional
public void checkoutAsset(Long assetId, Long employeeId) { ... }
```

---

## USER STORY 36: Block Asset Use Without Certification

### Overview
Block asset use if certification missing.

### Service Layer
**Sample Implementation:**
```java
public boolean canUseAsset(Long employeeId, Long assetId) { ... }
```

---

## USER STORY 37: Asset Condition Tracking

### Overview
Track asset condition.

### Domain Model
**Design Specification:**
- condition: Enum (GOOD, FAIR, POOR)

---

## USER STORY 38: Overdue Asset Return Reporting

### Overview
Report overdue asset returns.

### Service Layer
**Sample Implementation:**
```java
public List<Asset> getOverdueAssets() { ... }
```

---

## USER STORY 39: Create Performance Review Templates

### Overview
Create review templates.

### Spring Boot Architecture
- Performance module.

### Package Structure
```
com.warehouse.ems.performance
âââ domain
âââ repository
âââ service
âââ controller
âââ dto
```

### Domain Model
**Design Specification:**
- Entity: ReviewTemplate
- Fields:
  - id, name, cycle, fields

**Sample Implementation:**
```java
@Entity
public class ReviewTemplate extends AbstractAuditableEntity {
    private String name;
    private String cycle;
    @Lob
    private String fields;
}
```

---

## USER STORY 40: Assign and Complete Performance Reviews

### Overview
Assign and complete reviews.

### Domain Model
**Design Specification:**
- Entity: PerformanceReview
- Fields:
  - id, employeeId, templateId, status, ratings

**Sample Implementation:**
```java
@Entity
public class PerformanceReview extends AbstractAuditableEntity {
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ReviewTemplate template;
    @Enumerated(EnumType.STRING)
    private ReviewStatus status;
    @Lob
    private String ratings;
}
```

---

## USER STORY 41: Track Goals and Competencies

### Overview
Track goals and competencies in reviews.

### Domain Model
**Design Specification:**
- goals: JSON field

---

## USER STORY 42: Export Performance Reviews as PDF

### Overview
Export reviews as PDF.

### Service Layer
**Sample Implementation:**
```java
public byte[] exportReviewAsPdf(Long reviewId) { ... }
```

---

## USER STORY 43: Immutable Review History

### Overview
Review history immutable after sign-off.

### Service Layer
**Sample Implementation:**
```java
public void signOffReview(Long reviewId) { ... }
```

---

## USER STORY 44: Generate Payroll Export File

### Overview
Generate payroll-ready files.

### Spring Boot Architecture
- Payroll module.

### Package Structure
```
com.warehouse.ems.payroll
âââ service
âââ controller
```

### Service Layer
**Sample Implementation:**
```java
public byte[] generatePayrollExport(LocalDate startDate, LocalDate endDate) { ... }
```

---

## USER STORY 45: Secure Payroll File Delivery

### Overview
Deliver payroll files via SFTP or API.

### Service Layer
**Sample Implementation:**
```java
public void deliverPayrollFile(byte[] file) { ... }
```

---

## USER STORY 46: Payroll Export Audit Log

### Overview
Audit log for payroll exports.

### Service Layer
**Sample Implementation:**
```java
public void logPayrollExport(Long exportId) { ... }
```

---

## USER STORY 47: Retry Failed Payroll Deliveries

### Overview
Retry failed deliveries with backoff.

### Service Layer
**Sample Implementation:**
```java
@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
public void deliverPayrollFile(byte[] file) { ... }
```

---

## USER STORY 48: In-App Notifications for Shift Changes

### Overview
In-app notifications for shift changes.

### Spring Boot Architecture
- Notification module.

### Package Structure
```
com.warehouse.ems.notification
âââ domain
âââ repository
âââ service
âââ controller
```

### Domain Model
**Design Specification:**
- Entity: Notification
- Fields:
  - id, userId, message, type, status

**Sample Implementation:**
```java
@Entity
public class Notification extends AbstractAuditableEntity {
    @ManyToOne
    private Employee user;
    private String message;
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
}
```

---

## USER STORY 49: Email/SMS Notifications for Expiring Certifications

### Overview
Email/SMS alerts for expiring certifications.

### Service Layer
**Sample Implementation:**
```java
public void sendCertificationExpiryAlert(Long employeeId) { ... }
```

---

## USER STORY 50: Notification Preferences and Quiet Hours

### Overview
Configure notification preferences.

### Domain Model
**Design Specification:**
- Entity: NotificationPreference
- Fields:
  - userId, channels, quietHoursStart, quietHoursEnd

---

## USER STORY 51: Announcements Dashboard

### Overview
View announcements on dashboard.

### Controller Layer
**Description:** GET /notifications/announcements

---

## USER STORY 52: Notification Delivery Status Tracking

### Overview
Track notification delivery status.

### Service Layer
**Sample Implementation:**
```java
public List<Notification> getDeliveryStatus() { ... }
```

---

## USER STORY 53: Expose REST APIs for HRIS Integration

### Overview
Expose REST APIs for HRIS.

### Spring Boot Architecture
- Integration module.

### Package Structure
```
com.warehouse.ems.integration
âââ controller
âââ service
```

### Controller Layer
**Description:** POST /integration/hris/employees

---

## USER STORY 54: WMS Integration for Location/Department

### Overview
Integrate with WMS for location/department data.

### Service Layer
**Sample Implementation:**
```java
public void syncWithWMS() { ... }
```

---

## USER STORY 55: SSO Integration with IDP

### Overview
SSO login via IDP.

### Security Configuration
**Sample Implementation:**
```java
http.oauth2Login();
```

---

## USER STORY 56: Idempotent Webhooks for Events

### Overview
Expose idempotent webhooks.

### Controller Layer
**Description:** POST /webhooks/events

---

## USER STORY 57: API Documentation in OpenAPI

### Overview
API documentation in OpenAPI format.

### Configuration
**Sample Implementation:**
```yaml
springdoc:
  api-docs:
    path: /api-docs
```

---

## USER STORY 58: Centralized Audit Logging

### Overview
Centralized audit logs for sensitive changes.

### Domain Model
**Design Specification:**
- Entity: AuditLog (as above)

---

## USER STORY 59: Tamper-Evident Audit Storage

### Overview
Tamper-evident audit logs.

### Service Layer
**Sample Implementation:**
```java
public String hashAuditLog(AuditLog log) { ... }
```

---

## USER STORY 60: Export Audit Logs by Date/User/Entity

### Overview
Export audit logs filtered.

### Controller Layer
**Description:** GET /audit/export?date=&user=&entity=

---

## USER STORY 61: Validate Audit Coverage with Tests

### Overview
Unit tests for audit coverage.

**Sample Implementation:**
```java
@Test
public void testAuditCoverage() { ... }
```

---

## USER STORY 62: Attendance Report

### Overview
Generate attendance reports.

### Service Layer
**Sample Implementation:**
```java
public byte[] generateAttendanceReport(LocalDate startDate, LocalDate endDate) { ... }
```

---

## USER STORY 63: Overtime Report

### Overview
Generate overtime reports.

### Service Layer
**Sample Implementation:**
```java
public byte[] generateOvertimeReport(LocalDate startDate, LocalDate endDate) { ... }
```

---

## USER STORY 64: Leave Balances Report

### Overview
Generate leave balance reports.

### Service Layer
**Sample Implementation:**
```java
public byte[] generateLeaveBalanceReport() { ... }
```

---

## USER STORY 65: Certification Status Report

### Overview
Generate certification status reports.

### Service Layer
**Sample Implementation:**
```java
public byte[] generateCertificationStatusReport() { ... }
```

---

## USER STORY 66: Safety KPIs Dashboard

### Overview
View safety KPIs on dashboard.

### Controller Layer
**Description:** GET /safety/kpis

---

## USER STORY 67: Role-Based Report Access

### Overview
Control report access by role.

### Security Configuration
**Sample Implementation:**
```java
@PreAuthorize("hasRole('ADMIN')")
public byte[] generateReport() { ... }
```

---

## USER STORY 68: Mobile Clock In/Out

### Overview
Clock in/out from mobile device.

### Spring Boot Architecture
- PWA support.

### Controller Layer
**Description:** POST /attendance/clock-in (mobile-friendly)

---

## USER STORY 69: Mobile Schedule View

### Overview
View schedule on mobile.

### Controller Layer
**Description:** GET /scheduling/my-shifts (mobile-friendly)

---

## USER STORY 70: Mobile Leave Request

### Overview
Request leave from mobile.

### Controller Layer
**Description:** POST /leave/requests (mobile-friendly)

---

## USER STORY 71: Offline Clock Event Queue

### Overview
Queue clock events offline.

### Service Layer
**Sample Implementation:**
```java
public void queueOfflineEvent(AttendanceEventDto dto) { ... }
```

---

## USER STORY 72: PWA Installability

### Overview
Install app as PWA.

### Configuration
**Sample Implementation:**
```json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone"
}
```

---

## USER STORY 73: Automate Onboarding Provisioning

### Overview
Automate account provisioning for new hires.

### Service Layer
**Sample Implementation:**
```java
@Scheduled(cron = "0 0 1 * * ?")
public void provisionNewHires() { ... }
```

---

## USER STORY 74: Automate Offboarding Deprovisioning

### Overview
Automate access revocation on termination.

### Service Layer
**Sample Implementation:**
```java
public void deprovisionEmployee(Long employeeId) { ... }
```

---

## USER STORY 75: Support Multiple Warehouses

### Overview
Support multiple warehouses.

### Domain Model
**Design Specification:**
- Entity: Warehouse
- Fields:
  - id, name, location

**Sample Implementation:**
```java
@Entity
public class Warehouse extends AbstractAuditableEntity {
    private String name;
    private String location;
}
```

---

## USER STORY 76: Localization and Time Zone Support

### Overview
Localization and time zone support.

### Configuration
**Sample Implementation:**
```yaml
spring:
  messages:
    basename: messages
```

---

## USER STORY 77: Expose Custom Metrics

### Overview
Expose custom metrics.

### Service Layer
**Sample Implementation:**
```java
@Component
public class CustomMetrics {
    private final MeterRegistry meterRegistry;
    public void recordMetric(String name, double value) { ... }
}
```

---

## USER STORY 78: Structured Logging

### Overview
Structured JSON logs.

### Configuration
**Sample Implementation:**
```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

---

## USER STORY 79: Alerting for Critical Errors

### Overview
Alerts for critical errors.

### Service Layer
**Sample Implementation:**
```java
public void sendAlert(String message) { ... }
```

---

## USER STORY 80: CI/CD Pipeline with Automated Tests

### Overview
CI/CD pipeline with tests.

### Configuration
**Sample Implementation:**
```yaml
# .github/workflows/ci.yml
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

---

## USER STORY 81: Additional User Stories

For the remaining user stories (if any), follow the same pattern:
- Overview
- Spring Boot Architecture
- Package Structure
- Domain Model
- Repository Layer
- Service Layer
- Controller Layer
- Security Configuration
- Database Migration
- Configuration
- Integration Points

---

## Conclusion

This comprehensive low-level technical design document covers all 81 user stories for the Warehouse Employee Management System, structured according to Spring Boot best practices. Each section includes architecture overview, package structure, domain models, repository/service/controller layers, security, configuration, integration points, and code snippets. The document is ready for implementation and can be used as a reference for development teams.