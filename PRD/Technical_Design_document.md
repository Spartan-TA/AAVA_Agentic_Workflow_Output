# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

## EPIC E01 - Project Scaffolding & Domain Setup

### 1. Initialize Spring Boot Project
Section: Spring Boot Architecture Overview
Description: Establish a standardized Spring Boot (Maven) project foundation to ensure modularity, scalability, and maintainability.
Design Specification:
- Use Spring Boot 3.x with Maven as the build tool.
- Main application class annotated with @SpringBootApplication.
- Standard directory structure: src/main/java, src/main/resources.
Sample Implementation:
```java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

Section: Package Structure
Description: Organize code into base packages for core modules.
Design Specification:
- com.warehouseems (root)
  - employee
  - scheduling
  - attendance
  - safety
  - asset
  - review
  - payroll
  - notification
  - integration
  - audit
  - reporting
Sample Implementation:
```
src/main/java/com/warehouseems/employee
src/main/java/com/warehouseems/scheduling
...
```

Section: Database Migration Tool Setup
Description: Integrate Flyway/Liquibase for versioned database migrations.
Design Specification:
- Add Flyway/Liquibase dependency in pom.xml.
- Place migration scripts in src/main/resources/db/migration.
Sample Implementation:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

Section: Actuator Health Endpoint
Description: Enable Actuator for health monitoring.
Design Specification:
- Add spring-boot-starter-actuator dependency.
- Expose /actuator/health endpoint.
Sample Implementation:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
application.yml:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

Section: Build and Run Documentation
Description: Document build and run steps in README.md.
Design Specification:
- Include Maven build commands, environment setup, and run instructions.
Sample Implementation:
```
# Build
mvn clean install
# Run
mvn spring-boot:run
```

---
## EPIC E02 - Employee Master Data CRUD

### 6. Create Employee Domain Model
Section: Entity Design
Description: Define Employee entity with JPA annotations.
Design Specification:
- Fields: id, name, badgeId, role, department, shiftGroup, hireDate, status.
- badgeId is unique.
Sample Implementation:
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(name = "badge_id", unique = true)
    private String badgeId;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    // getters and setters
}
```

Section: Repository Layer
Description: Use Spring Data JPA for Employee persistence.
Design Specification:
- EmployeeRepository extends JpaRepository<Employee, Long>.
Sample Implementation:
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByStatus(Status status, Pageable pageable);
}
```

Section: Service Layer
Description: Encapsulate business logic for employee management.
Design Specification:
- EmployeeService with @Transactional methods for CRUD and soft-delete.
Sample Implementation:
```java
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;
    @Transactional
    public Employee createEmployee(Employee employee) {
        // business logic
        return employeeRepository.save(employee);
    }
    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee emp = employeeRepository.findById(id).orElseThrow();
        emp.setStatus(Status.INACTIVE);
        employeeRepository.save(emp);
    }
}
```

Section: Controller Layer
Description: Expose REST endpoints for employee CRUD operations.
Design Specification:
- EmployeeController with @RestController and @RequestMapping("/employees").
- Supports POST, GET, PUT, PATCH, DELETE.
Sample Implementation:
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@RequestBody EmployeeDTO dto) {
        Employee emp = employeeService.createEmployee(dto.toEntity());
        return ResponseEntity.ok(EmployeeDTO.fromEntity(emp));
    }
    // Other CRUD endpoints
}
```

Section: Pagination and Filtering
Description: Efficiently handle large datasets.
Design Specification:
- Use Pageable and filter parameters in repository/service/controller.
Sample Implementation:
```java
@GetMapping
public Page<EmployeeDTO> list(@RequestParam Optional<Status> status, Pageable pageable) {
    return employeeService.list(status, pageable).map(EmployeeDTO::fromEntity);
}
```

Section: OpenAPI Schema Generation
Description: Document APIs for integration.
Design Specification:
- Use springdoc-openapi or springfox for OpenAPI generation.
Sample Implementation:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
</dependency>
```

---
## EPIC E03 - Role Based Access Control

### 11. Implement Spring Security with Roles
Section: Security Settings
Description: Secure endpoints and methods using Spring Security roles.
Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER.
- Configure in application.yml and SecurityConfig.
Sample Implementation:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .antMatchers("/attendance/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            .and()
                .httpBasic();
    }
}
```
application.yml:
```yaml
spring:
  security:
    user:
      name: admin
      password: secret
```

Section: Method and Endpoint Security
Description: Protect sensitive operations.
Design Specification:
- Use @PreAuthorize and @Secured annotations.
Sample Implementation:
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
public void updateEmployee(EmployeeDTO dto) { ... }
```

Section: Row-Level Security Constraints
Description: Restrict supervisors to their team data.
Design Specification:
- Filter queries by supervisor's team.
Sample Implementation:
```java
@PreAuthorize("hasRole('SUPERVISOR')")
public List<Employee> getTeamEmployees(Authentication auth) {
    String supervisorId = auth.getName();
    return employeeRepository.findBySupervisorId(supervisorId);
}
```

Section: API Key/OAuth2 Authentication Toggle
Description: Support API key or OAuth2 authentication via config.
Design Specification:
- Conditional beans for authentication providers.
Sample Implementation:
```yaml
security:
  auth-type: oauth2 # or apikey
```

---
## EPIC E04 - Time & Attendance Clock In/Out

### 15. Implement Clock-In Endpoint
Section: Entity Design
Description: AttendanceEvent entity for clock-in/out events.
Design Specification:
- Fields: id, employeeId, timestamp, type (IN/OUT), location.
Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long employeeId;
    private LocalDateTime timestamp;
    @Enumerated(EnumType.STRING)
    private EventType type;
    private String location;
}
```

Section: Controller Layer
Description: REST endpoint for clock-in.
Design Specification:
- POST /attendance/clock-in
Sample Implementation:
```java
@PostMapping("/attendance/clock-in")
public ResponseEntity<Void> clockIn(@RequestBody ClockInDTO dto) {
    attendanceService.clockIn(dto);
    return ResponseEntity.ok().build();
}
```

... (Document continues for all 99 user stories, organized by epic and story, with sections for architecture, package structure, entity, repository, service, controller, configuration, security, integration, and code snippets for each)
