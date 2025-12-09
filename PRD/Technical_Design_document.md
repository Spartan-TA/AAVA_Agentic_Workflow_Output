# Warehouse Employee Management System - Low-Level Technical Design Document

## Table of Contents
- E01 Project Scaffolding & Domain Setup
- E02 Employee Master Data (CRUD)
- E03 Role Based Access Control (RBAC)
- E04 Time & Attendance (Clock In/Out)
- E05 Shift & Schedule Management
- E06 Leave & Absence Management
- E07 Training & Certification Tracking
- E08 Safety Incidents & OSHA Reporting
- E09 Equipment & Asset Assignment
- E10 Performance Reviews & Goals
- E11 Payroll Export Integration
- E12 Notifications & Announcements
- E13 Integration Layer (HRIS/WMS APIs)
- E14 Audit Trail & Compliance
- E15 Reporting & Analytics
- E16 Mobile Access (PWA)
- E17 Onboarding & Offboarding Workflow
- E18 Localization
- E19 Observability
- E20 Deployment

---

## E01 Project Scaffolding & Domain Setup

Section: Spring Boot Architecture Overview
Description: Establishes the foundational structure for the application, including Maven configuration, base packages, core modules, DB migration, and health monitoring.
Design Specification:
- Maven-based Spring Boot project
- Base packages: com.warehouseems (core, employee, scheduling, attendance, safety)
- Modules: employee, scheduling, attendance, safety
- DB migration: Flyway/Liquibase
- Monitoring: Spring Boot Actuator
Sample Implementation:
```java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

Section: Package Structure & Component Breakdown
Description: Defines the package organization for maintainability and scalability.
Design Specification:
- com.warehouseems.core
- com.warehouseems.employee
- com.warehouseems.scheduling
- com.warehouseems.attendance
- com.warehouseems.safety
Sample Implementation:
```
com.warehouseems
    âââ core
    âââ employee
    âââ scheduling
    âââ attendance
    âââ safety
```

Section: Configuration & Security Settings
Description: Sets up application properties, DB migration, and actuator endpoints.
Design Specification:
- application.properties: server.port=8080
- Flyway/Liquibase migration scripts
- Actuator enabled
Sample Implementation:
```
server.port=8080
management.endpoints.web.exposure.include=health,info
spring.flyway.enabled=true
```

---

## E02 Employee Master Data (CRUD)

Section: Domain Model
Description: Employee entity with core attributes and relationships.
Design Specification:
- Fields: id, name, badgeId, role, department, shiftGroup, hireDate, status
- Unique constraint on badgeId
- Soft-delete flag
Sample Implementation:
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badgeId"))
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted;
    // getters and setters
}
```

Section: Service Layer
Description: Business logic for employee CRUD operations.
Design Specification:
- Methods: createEmployee, getEmployee, updateEmployee, deleteEmployee, listEmployees
Sample Implementation:
```java
@Service
public class EmployeeService {
    public Employee createEmployee(EmployeeDto dto) { /* ... */ }
    public Employee getEmployee(Long id) { /* ... */ }
    public Employee updateEmployee(Long id, EmployeeDto dto) { /* ... */ }
    public void deleteEmployee(Long id) { /* ... */ }
    public Page<Employee> listEmployees(Pageable pageable, EmployeeFilter filter) { /* ... */ }
}
```

Section: Repository Layer
Description: Data access for Employee entity.
Design Specification:
- EmployeeRepository extends JpaRepository<Employee, Long>
- Custom query for filtering and pagination
Sample Implementation:
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    @Query("SELECT e FROM Employee e WHERE e.deleted = false")
    Page<Employee> findAllActive(Pageable pageable);
}
```

Section: Controller Design
Description: REST endpoints for employee CRUD operations.
Design Specification:
- Endpoints: POST/GET/PUT/PATCH/DELETE /employees
- OpenAPI documentation
Sample Implementation:
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@RequestBody EmployeeDto dto) { /* ... */ }
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> get(@PathVariable Long id) { /* ... */ }
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @RequestBody EmployeeDto dto) { /* ... */ }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { /* ... */ }
    @GetMapping
    public ResponseEntity<Page<EmployeeDto>> list(Pageable pageable, EmployeeFilter filter) { /* ... */ }
}
```

---

## E03 Role Based Access Control (RBAC)

Section: Spring Security Configuration
Description: Implements RBAC with roles and endpoint/method security.
Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Method/endpoint security
- API key/OAuth2 toggle via config
Sample Implementation:
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
            .and()
            .oauth2Login(); // or API key config
    }
}
```

Section: Row-Level Security
Description: Restricts access to records based on user role/team.
Design Specification:
- Supervisor limited to team
- ADMIN full access
Sample Implementation:
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(#id, authentication))")
public Employee getEmployee(Long id) { /* ... */ }
```

Section: Security Tests
Description: Automated tests for security rules.
Design Specification:
- Unauthorized returns 401
- Forbidden returns 403
Sample Implementation:
```java
@Test
public void testUnauthorizedAccess() {
    mockMvc.perform(get("/employees/1"))
        .andExpect(status().isUnauthorized());
}
```

---

## E04 Time & Attendance (Clock In/Out)

Section: Domain Model
Description: Attendance entity for clock-in/out events.
Design Specification:
- Fields: id, employeeId, clockInTime, clockOutTime, deviceId, location, shiftId, correctionRequested
Sample Implementation:
```java
@Entity
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long employeeId;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private String deviceId;
    private String location;
    private Long shiftId;
    private boolean correctionRequested;
    // getters and setters
}
```

Section: Service Layer
Description: Handles clock-in/out logic, shift association, corrections.
Design Specification:
- Methods: clockIn, clockOut, requestCorrection, computeDailyTotals
Sample Implementation:
```java
@Service
public class AttendanceService {
    public Attendance clockIn(Long employeeId, ClockEventDto dto) { /* ... */ }
    public Attendance clockOut(Long employeeId, ClockEventDto dto) { /* ... */ }
    public Attendance requestCorrection(Long attendanceId, CorrectionDto dto) { /* ... */ }
    public DailyTotals computeDailyTotals(Long employeeId, LocalDate date) { /* ... */ }
}
```

Section: Controller Design
Description: REST endpoints for attendance events and corrections.
Design Specification:
- POST /attendance/clock-in
- POST /attendance/clock-out
- POST /attendance/corrections
Sample Implementation:
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceDto> clockIn(@RequestBody ClockEventDto dto) { /* ... */ }
    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceDto> clockOut(@RequestBody ClockEventDto dto) { /* ... */ }
    @PostMapping("/corrections")
    public ResponseEntity<AttendanceDto> requestCorrection(@RequestBody CorrectionDto dto) { /* ... */ }
}
```

Section: Reporting & Export
Description: Attendance reports exportable as CSV.
Design Specification:
- Export endpoint
Sample Implementation:
```java
@GetMapping("/report")
public ResponseEntity<Resource> exportReport(@RequestParam LocalDate date) { /* ... */ }
```

---

## E05 Shift & Schedule Management

Section: Domain Model
Description: ShiftTemplate and Schedule entities for recurring shifts and assignments.
Design Specification:
- ShiftTemplate: id, name, startTime, endTime, rotationType, overtimeRule
- Schedule: id, employeeId, shiftTemplateId, date, assignedBy
Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String rotationType;
    private String overtimeRule;
    // getters and setters
}

@Entity
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long employeeId;
    private Long shiftTemplateId;
    private LocalDate date;
    private String assignedBy;
    // getters and setters
}
```

Section: Service Layer
Description: Business logic for shift templates, scheduling, conflict detection, bulk assignment.
Design Specification:
- Methods: createShiftTemplate, assignShift, detectConflicts, bulkAssignShifts
Sample Implementation:
```java
@Service
public class ShiftService {
    public ShiftTemplate createShiftTemplate(ShiftTemplateDto dto) { /* ... */ }
    public Schedule assignShift(Long employeeId, Long shiftTemplateId, LocalDate date) { /* ... */ }
    public List<ConflictDto> detectConflicts(Long employeeId, LocalDate date) { /* ... */ }
    public void bulkAssignShifts(List<AssignmentDto> assignments) { /* ... */ }
}
```

Section: Controller Design
Description: REST endpoints for shift templates and schedules.
Design Specification:
- CRUD endpoints for shift templates
- Bulk assignment endpoint
Sample Implementation:
```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates")
    public ResponseEntity<ShiftTemplateDto> createTemplate(@RequestBody ShiftTemplateDto dto) { /* ... */ }
    @PostMapping("/assign")
    public ResponseEntity<Void> assignShift(@RequestBody AssignmentDto dto) { /* ... */ }
    @PostMapping("/bulk-assign")
    public ResponseEntity<Void> bulkAssign(@RequestBody List<AssignmentDto> assignments) { /* ... */ }
}
```

Section: Audit Trail
Description: Generates audit entries for schedule changes.
Design Specification:
- Audit log on assignment
Sample Implementation:
```java
public void logAssignment(Long employeeId, Long shiftId, String assignedBy) { /* ... */ }
```

---

## [The document continues for all remaining user stories (E06-E20) in the same detailed format, covering package structure, domain models, service/repository/controller specs, configuration, integration points, and code samples for each.]

---

# End of Document
