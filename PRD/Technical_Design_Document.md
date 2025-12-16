# Warehouse Employee Management System - Low-Level Technical Design Document

---

Section: E01 - Project Scaffolding & Domain Setup  
Description: This section covers the foundational setup of the Spring Boot project, including Maven configuration, base package structure, database migration tooling, and actuator endpoints for health monitoring.  
Design Specification:
- Spring Boot 3.x (Java 17+), Maven project
- Base package: `com.warehouse.employee`
- Sub-packages: `domain`, `service`, `repository`, `controller`, `dto`, `config`, `security`, `exception`, `integration`, `util`
- Flyway/Liquibase for DB migrations
- Spring Boot Actuator enabled
- Profiles: `dev`, `test`, `prod`
Sample Implementation:
```java
@SpringBootApplication
public class WarehouseEmployeeApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeApplication.class, args);
    }
}
```
```xml
<!-- pom.xml excerpt -->
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

---

Section: E02 - Employee Master Data (CRUD)  
Description: Implements the Employee domain with full CRUD REST APIs, enforcing unique badge IDs, supporting soft deletes, and providing pagination/filtering.  
Design Specification:
- Entity: `Employee` with fields: `id`, `name`, `badgeId`, `role`, `department`, `shiftGroup`, `hireDate`, `status`, `deleted`
- Repository: `EmployeeRepository extends JpaRepository<Employee, Long>`
- Service: `EmployeeService` with CRUD, soft-delete, filtering
- Controller: `EmployeeController` with REST endpoints, DTO mapping, validation
- OpenAPI/Swagger documentation
Sample Implementation:
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    @Column(name = "badge_id", unique = true)
    private String badgeId;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    private boolean deleted;
    // getters/setters
}
```
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```
```java
@Service
public class EmployeeService {
    @Transactional
    public Employee createEmployee(EmployeeDto dto) { /* ... */ }
    @Transactional(readOnly = true)
    public Page<Employee> listEmployees(Pageable pageable) { /* ... */ }
    @Transactional
    public void softDeleteEmployee(Long id) { /* ... */ }
}
```
```java
@RestController
@RequestMapping("/employees")
@Validated
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { /* ... */ }
    @GetMapping
    public Page<EmployeeDto> list(Pageable pageable) { /* ... */ }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { /* ... */ }
}
```

---

Section: E03 - Role-Based Access Control (RBAC)  
Description: Secures endpoints and methods using Spring Security, with roles ADMIN, HR, SUPERVISOR, WORKER. Supports API key/OAuth2 toggle.  
Design Specification:
- Security config in `com.warehouse.employee.security`
- Roles as enum: `Role { ADMIN, HR, SUPERVISOR, WORKER }`
- Method-level security: `@PreAuthorize`
- API key/OAuth2 toggle via `application.yml`
- Custom `UserDetailsService`
Sample Implementation:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${security.mode}")
    private String securityMode; // "apikey" or "oauth2"

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if ("apikey".equals(securityMode)) {
            http.addFilterBefore(new ApiKeyAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        } else {
            http.oauth2ResourceServer().jwt();
        }
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .anyRequest().authenticated();
    }
}
```
```java
@Service
public class EmployeeService {
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Employee updateEmployee(EmployeeDto dto) { /* ... */ }
}
```

---

Section: E04 - Time & Attendance (Clock In/Out)  
Description: Provides endpoints for clock-in/out events, geofence validation, device capture, and missed punch workflow.  
Design Specification:
- Entity: `AttendanceEvent` with fields: `id`, `employee`, `timestamp`, `type`, `deviceId`, `location`, `approved`
- Service: `AttendanceService` for clock-in/out, missed punch handling
- Controller: `/attendance/clock-in`, `/attendance/clock-out`
- Geofence validation utility
Sample Implementation:
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
    private boolean approved;
}
```
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<Void> clockIn(@RequestBody ClockEventDto dto) { /* ... */ }
    @PostMapping("/clock-out")
    public ResponseEntity<Void> clockOut(@RequestBody ClockEventDto dto) { /* ... */ }
}
```

---

Section: E05 - Shift & Schedule Management  
Description: Manages shift templates, rotations, assignments, blackout dates, and operation calendars.  
Design Specification:
- Entities: `ShiftTemplate`, `ShiftAssignment`, `BlackoutDate`
- Service: `ShiftService` for CRUD, conflict detection
- Controller: `/shifts`, `/schedules`
- Audit logging on changes
Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
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
}
```

---

Section: E06 - Leave & Absence Management  
Description: Handles PTO, sick, unpaid leave requests, approvals, and accrual balances.  
Design Specification:
- Entity: `LeaveRequest` with fields: `id`, `employee`, `type`, `startDate`, `endDate`, `status`, `approver`, `balance`
- Service: `LeaveService` for request, approval, accrual
- Controller: `/leave`
Sample Implementation:
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
    @ManyToOne
    private Employee approver;
    private int balance;
}
```

---

Section: E07 - Training & Certification Tracking  
Description: Tracks certifications, expirations, renewals, and blocks assignments for expired certs.  
Design Specification:
- Entity: `Certification` with fields: `id`, `employee`, `type`, `expiryDate`, `documentUrl`
- Service: `CertificationService` for CRUD, expiry alerts
- Controller: `/certifications`
Sample Implementation:
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

---

Section: E08 - Safety Incidents & OSHA Reporting  
Description: Records incidents, manages investigation workflow, and generates OSHA summaries.  
Design Specification:
- Entity: `SafetyIncident` with fields: `id`, `date`, `location`, `description`, `severity`, `status`, `involvedEmployees`
- Service: `SafetyService` for workflow, reporting
- Controller: `/safety/incidents`
Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private LocalDate date;
    private String location;
    private String description;
    @Enumerated(EnumType.STRING)
    private Severity severity;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
    @ManyToMany
    private List<Employee> involvedEmployees;
}
```

---

Section: E09 - Equipment & Asset Assignment  
Description: Assigns assets to employees, tracks check-in/out, and enforces certification checks.  
Design Specification:
- Entity: `Asset`, `AssetAssignment`
- Service: `AssetService` for assignment, history
- Controller: `/assets`, `/assets/assign`
Sample Implementation:
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

---

Section: E10 - Performance Reviews & Goals  
Description: Manages review templates, goals, ratings, and acknowledgements.  
Design Specification:
- Entity: `PerformanceReview`, `Goal`
- Service: `PerformanceService` for review cycles
- Controller: `/reviews`, `/goals`
Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate reviewDate;
    private String template;
    private String comments;
    private int rating;
    private boolean acknowledged;
}
```

---

Section: E11 - Payroll Export Integration  
Description: Generates payroll files from attendance/leave, maps to provider formats, and delivers securely.  
Design Specification:
- Service: `PayrollExportService` for file generation, SFTP/API delivery
- Integration: SFTP client, REST API client
- Audit log for exports
Sample Implementation:
```java
@Service
public class PayrollExportService {
    public void exportPayroll(LocalDate periodStart, LocalDate periodEnd) { /* ... */ }
}
```

---

Section: E12 - Notifications & Announcements  
Description: Sends in-app/email/SMS notifications for shifts, certs, approvals, and announcements.  
Design Specification:
- Entity: `Notification`, `Announcement`
- Service: `NotificationService` for delivery, opt-in/out
- Integration: Email/SMS providers
Sample Implementation:
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee recipient;
    private String channel; // EMAIL, SMS, IN_APP
    private String message;
    private boolean delivered;
}
```

---

Section: E13 - Integration Layer (HRIS/WMS APIs)  
Description: Exposes REST APIs and connectors for HRIS, WMS, and IDP SSO; supports webhooks.  
Design Specification:
- Integration: REST controllers, Feign/WebClient clients
- Security: JWT/OAuth2
- Webhook endpoints
Sample Implementation:
```java
@RestController
@RequestMapping("/api/hris")
public class HRISController {
    @PostMapping("/employees")
    public ResponseEntity<Void> syncEmployee(@RequestBody EmployeeDto dto) { /* ... */ }
}
```

---

Section: E14 - Audit Trail & Compliance  
Description: Centralized audit logging for sensitive changes, with tamper-evident storage.  
Design Specification:
- Entity: `AuditLog` with fields: `id`, `entity`, `entityId`, `actor`, `timestamp`, `before`, `after`, `operation`
- Service: `AuditService` for log creation
- Immutability enforced at DB level
Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private Long entityId;
    private String actor;
    private LocalDateTime timestamp;
    @Lob
    private String before;
    @Lob
    private String after;
    private String operation;
}
```

---

Section: E15 - Reporting & Analytics  
Description: Provides operational reports and dashboards, with CSV/PDF export and role-based access.  
Design Specification:
- Service: `ReportingService` for report generation
- Controller: `/reports`
- Export utilities
Sample Implementation:
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> exportAttendance(@RequestParam LocalDate from, @RequestParam LocalDate to) { /* ... */ }
}
```

---

Section: E16 - Mobile Access (PWA)  
Description: Enables responsive, offline-friendly mobile access for core flows.  
Design Specification:
- Controller: `/mobile` endpoints
- PWA manifest/static resources
- Offline queue for clock events
Sample Implementation:
```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @GetMapping("/shifts")
    public List<ShiftAssignmentDto> getShifts() { /* ... */ }
}
```

---

Section: E17 - Onboarding & Offboarding Workflow  
Description: Automates provisioning, initial schedule, training, and deprovisioning on termination.  
Design Specification:
- Service: `OnboardingService`, `OffboardingService`
- Integration: HRIS sync, asset assignment, schedule update
Sample Implementation:
```java
@Service
public class OnboardingService {
    public void onboardEmployee(EmployeeDto dto) { /* ... */ }
}
```

---

Section: E18 - Localization & Multi-Tenant  
Description: Supports multiple warehouses, English/Spanish UI, and timezone-aware scheduling.  
Design Specification:
- Entity: `Warehouse`, `Tenant`
- Locale resolver in config
- Timezone handling in scheduling logic
Sample Implementation:
```java
@Entity
public class Warehouse {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String timezone;
}
```

---

Section: E19 - Observability & Monitoring  
Description: Implements Prometheus metrics, JSON logs, and OpenTelemetry tracing.  
Design Specification:
- Actuator endpoints
- Micrometer/Prometheus integration
- Logback JSON config
- OpenTelemetry tracing beans
Sample Implementation:
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
logging:
  pattern:
    console: '{"timestamp":"%d{yyyy-MM-dd HH:mm:ss}","level":"%p","logger":"%c{1}","message":"%m"}'
```

---

Section: E20 - Deployment & CI/CD  
Description: Provides Docker/Kubernetes manifests and GitHub Actions pipeline for CI/CD.  
Design Specification:
- Dockerfile for Spring Boot app
- k8s manifests: Deployment, Service, ConfigMap, Secret
- GitHub Actions workflow for build/test/deploy
Sample Implementation:
```dockerfile
# Dockerfile
FROM eclipse-temurin:17-jre
COPY target/warehouse-employee.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```
```yaml
# k8s-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: warehouse-employee
spec:
  replicas: 2
  selector:
    matchLabels:
      app: warehouse-employee
  template:
    metadata:
      labels:
        app: warehouse-employee
    spec:
      containers:
        - name: app
          image: warehouse-employee:latest
          ports:
            - containerPort: 8080
```
```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline
on:
  push:
    branches: [main]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build with Maven
        run: mvn clean package
      - name: Build Docker image
        run: docker build -t warehouse-employee:latest .
      - name: Deploy to Kubernetes
        run: kubectl apply -f k8s-deployment.yaml
```

---

**Note:**  
- All endpoints must include proper exception handling, validation, and logging.
- Security must be enforced at both endpoint and method levels.
- Entities should use JPA validation annotations and relationships.
- Services should be annotated with `@Service` and use `@Transactional` where appropriate.
- Controllers should use DTOs for request/response and be annotated with `@RestController`.
- Integration points (e.g., SFTP, email/SMS, HRIS) should use Spring's `@ConfigurationProperties` for externalized settings.
- Testing strategies: Use JUnit 5, Mockito for unit tests, and @SpringBootTest for integration tests.
- All configuration should be externalized via `application.yml` and profiles.

This document provides a production-grade, uniform, and extensible foundation for the warehouse employee management system, covering all 20 epics and their user stories in accordance with Spring Boot 3.x and Java 17+ best practices.