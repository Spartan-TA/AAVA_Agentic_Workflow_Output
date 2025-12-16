Section: E01 - Project Scaffolding & Domain Setup
Description: Initialize Spring Boot (Maven) project; configure base packages; set up core modules (employee, scheduling, attendance, safety); add Flyway/Liquibase for DB migrations; enable Actuator.
Design Specification:
- Use Spring Initializr to scaffold Maven project with dependencies: Spring Web, Spring Data JPA, Spring Security, Actuator, Flyway/Liquibase
- Package structure: com.companyname.warehouse (with subpackages: employee, scheduling, attendance, safety, config, common)
- Core modules as separate packages for modularity
- Flyway/Liquibase for DB migrations
- Actuator enabled for health checks
Sample Implementation:
```
com.companyname.warehouse
âââ employee
âââ scheduling
âââ attendance
âââ safety
âââ config
âââ common
```
application.properties:
```
spring.datasource.url=jdbc:postgresql://localhost:5432/warehouse
spring.datasource.username=warehouse
spring.datasource.password=secret
spring.flyway.enabled=true
management.endpoints.web.exposure.include=health,info
```

Section: E02 - Employee Master Data (CRUD)
Description: Create Employee domain with CRUD APIs and web DTOs: name, badgeId, role, department, shiftGroup, hireDate, status.
Design Specification:
- Entity: Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService with CRUD methods
- Controller: EmployeeController with REST endpoints
- DTOs for input/output
- Unique badgeId enforced
- Soft-delete (deleted flag)
- Pagination and filtering
- OpenAPI schemas
Sample Implementation:
```
@Entity
public class Employee {
  @Id @GeneratedValue private Long id;
  private String name;
  @Column(unique=true) private String badgeId;
  private String role;
  private String department;
  private String shiftGroup;
  private LocalDate hireDate;
  private String status;
  private boolean deleted;
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
  @GetMapping public Page<EmployeeDto> list(...)
  @PostMapping public EmployeeDto create(...)
  ...
}
```

Section: E03 - Role-Based Access Control (RBAC)
Description: Add Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER); method/endpoint security and row-level constraints where applicable; API key/OAuth2 toggle via config.
Design Specification:
- SecurityConfig with role-based access
- @PreAuthorize annotations on service/controller methods
- API key/OAuth2 toggle via application properties
- Row-level security in repository/service
Sample Implementation:
```
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
      .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
      .anyRequest().authenticated();
  }
}

@Service
public class EmployeeService {
  @PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == authentication.principal.department)")
  public Employee updateEmployee(Employee employee) {...}
}
```

Section: E04 - Time & Attendance (Clock In/Out)
Description: Endpoints for clock-in/out events with geofence (optional) and device capture; calculate hours worked per shift; handle missed punches and corrections workflow.
Design Specification:
- Entity: AttendanceEvent (id, employee, type, timestamp, deviceId, location, shift, status)
- Controller: AttendanceController with /clock-in, /clock-out endpoints
- Service: AttendanceService for business logic
- Geofence validation (optional)
- Correction workflow (approval tasks)
Sample Implementation:
```
@Entity
public class AttendanceEvent {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String type; // CLOCK_IN, CLOCK_OUT
  private LocalDateTime timestamp;
  private String deviceId;
  private String location;
  @ManyToOne private Shift shift;
  private String status; // NORMAL, CORRECTION_PENDING
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
  @PostMapping("/clock-in") public ResponseEntity<?> clockIn(...)
  @PostMapping("/clock-out") public ResponseEntity<?> clockOut(...)
}
```

Section: E05 - Shift & Schedule Management
Description: Create recurring shift templates, rotations, overtime rules, and assignment to employees; handle blackout dates and warehouse operation calendars.
Design Specification:
- Entity: ShiftTemplate, ShiftAssignment, BlackoutDate
- CRUD endpoints for shift templates and schedules
- Conflict detection logic in service
- Bulk assignment endpoints
- Audit entries on changes
Sample Implementation:
```
@Entity
public class ShiftTemplate {
  @Id @GeneratedValue private Long id;
  private String name;
  private LocalTime startTime;
  private LocalTime endTime;
  private boolean recurring;
}

@RestController
@RequestMapping("/shifts")
public class ShiftController {
  @PostMapping public ShiftTemplate create(...)
  ...
}
```
