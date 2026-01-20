# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

---

## EPIC E01 - Project Scaffolding & Domain Setup

### Section: OVERVIEW
**Description:**Establishes the foundational Spring Boot architecture, including Maven project setup, base package structure, database migration tooling (Flyway/Liquibase), and Actuator for health monitoring.

**Design Specification:**
- Spring Boot 3.x (Maven)
- Modular base packages: `employee`, `scheduling`, `attendance`, `safety`
- Flyway/Liquibase for DB migrations
- Actuator enabled on `/actuator/health`

**Sample Implementation:**
```java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

---

### Section: PACKAGE STRUCTURE
**Description:**Organizes code for scalability and maintainability.

**Design Specification:**
- `com.company.ems`
    - `employee`
    - `scheduling`
    - `attendance`
    - `safety`
    - `config`
    - `common`
    - `audit`
    - `integration`
    - `notification`
    - `reporting`
    - `mobile`
    - `onboarding`

**Sample Implementation:**`src/main/java/com/company/ems/employee/Employee.java`

---

### Section: CONFIGURATION
**Description:**Centralizes configuration for DB, Actuator, and migration tools.

**Design Specification:**
- `application.yml` for DB, server, actuator
- Flyway/Liquibase auto-run on startup

**Sample Implementation:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems
    username: ems
    password: secret
  flyway:
    enabled: true
    locations: classpath:db/migration
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

---

## EPIC E02 - Employee Master Data (CRUD)

### Section: OVERVIEW
**Description:**Implements Employee domain with CRUD APIs, DTOs, pagination, filtering, soft-delete, and OpenAPI docs.

**Design Specification:**
- Employee entity: name, badgeId (unique), role, department, shiftGroup, hireDate, status
- RESTful CRUD endpoints
- Soft-delete via `deleted` flag
- Pagination/filtering via Spring Data
- OpenAPI (Swagger) docs

**Sample Implementation:**
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank private String name;
    @NotBlank @Column(name = "badge_id") private String badgeId;
    @NotBlank private String role;
    @NotBlank private String department;
    private String shiftGroup;
    @PastOrPresent private LocalDate hireDate;
    @NotBlank private String status;
    private boolean deleted = false;
    // getters/setters
}
```

---

### Section: PACKAGE STRUCTURE
**Design Specification:**
- `com.company.ems.employee`
    - `domain`
    - `repository`
    - `service`
    - `controller`
    - `dto`
    - `mapper`

---

### Section: REPOSITORY LAYER
**Design Specification:**
- `EmployeeRepository extends JpaRepository<Employee, Long>`
- Custom: `findByBadgeIdAndDeletedFalse`, `findAllByDepartmentAndDeletedFalse`

**Sample Implementation:**
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDepartmentAndDeletedFalse(String department, Pageable pageable);
}
```

---

### Section: SERVICE LAYER
**Design Specification:**
- Interface: `EmployeeService`
- Implementation: `@Transactional`, handles business logic, soft-delete

**Sample Implementation:**
```java
public interface EmployeeService {
    EmployeeDto create(EmployeeDto dto);
    EmployeeDto update(Long id, EmployeeDto dto);
    void delete(Long id);
    Page<EmployeeDto> search(EmployeeFilter filter, Pageable pageable);
}
```

---

### Section: CONTROLLER LAYER
**Design Specification:**
- REST endpoints: `/employees`
- DTO validation, error handling via `@ControllerAdvice`

**Sample Implementation:**
```java
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee management APIs")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { ... }
    @GetMapping
    public Page<EmployeeDto> list(EmployeeFilter filter, Pageable pageable) { ... }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) { ... }
}
```

---

### Section: CONFIGURATION
**Design Specification:**
- OpenAPI config
- Soft-delete filter in JPA

**Sample Implementation:**
```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info().title("Warehouse EMS API").version("1.0"));
    }
}
```

---

### Section: SECURITY
**Design Specification:**
- Secured endpoints (see E03)

---

### Section: INTEGRATION POINTS
**Design Specification:**
- Exposed via OpenAPI for HRIS sync (see E13)

---

## EPIC E03 - Role Based Access Control (RBAC)

### Section: OVERVIEW
**Description:**Implements RBAC using Spring Security with roles: ADMIN, HR, SUPERVISOR, WORKER. Supports method/endpoint security, row-level access, and API key/OAuth2 toggle.

**Design Specification:**
- Roles as enums
- `@PreAuthorize` for method security
- Row-level filtering in repositories/services
- Configurable authentication via `application.yml`

**Sample Implementation:**
```java
public enum Role { ADMIN, HR, SUPERVISOR, WORKER }
```

---

### Section: CONFIGURATION
**Design Specification:**
- Security config class
- API key/OAuth2 toggle

**Sample Implementation:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${security.mode:oauth2}")
    private String mode;
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if ("apikey".equals(mode)) {
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

---

### Section: SERVICE LAYER
**Design Specification:**
- Row-level access: SUPERVISOR can only access team members

**Sample Implementation:**
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(principal, #id))")
public EmployeeDto getEmployee(Long id) { ... }
```

---

### Section: CONTROLLER LAYER
**Design Specification:**
- 401 for unauthorized, 403 for forbidden

---

### Section: INTEGRATION POINTS
**Design Specification:**
- SSO/IDP integration (see E13)

---

## EPIC E04 - Time & Attendance

### Section: OVERVIEW
**Description:**Implements clock-in/out endpoints, geofence/device capture, shift association, missed punch correction, and CSV export.

**Design Specification:**
- Attendance entity: employee, clockIn, clockOut, deviceId, location, status
- Correction workflow
- CSV export endpoint

**Sample Implementation:**
```java
@Entity
public class Attendance {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private String deviceId;
    private String location;
    private String status; // NORMAL, CORRECTION_PENDING, CORRECTED
}
```

---

### Section: CONTROLLER LAYER
**Design Specification:**
- `/attendance/clock-in`, `/attendance/clock-out`
- `/attendance/export` (CSV)

**Sample Implementation:**
```java
@PostMapping("/attendance/clock-in")
public ResponseEntity<Void> clockIn(@RequestBody ClockEventDto dto) { ... }
```

---

### Section: SERVICE LAYER
**Design Specification:**
- Shift association logic
- Correction approval workflow

---

### Section: CONFIGURATION
**Design Specification:**
- Geofence radius, device validation in `application.yml`

---

### Section: INTEGRATION POINTS
**Design Specification:**
- Export for payroll (see E11)

---

## EPIC E05 - Shift & Schedule Management

### Section: OVERVIEW
**Description:**Manages shift templates, rotations, overtime, assignments, blackout dates, and calendars.

**Design Specification:**
- ShiftTemplate, ShiftAssignment entities
- Conflict detection logic
- Bulk assignment endpoints

**Sample Implementation:**
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean overtimeAllowed;
}
```

---

### Section: CONTROLLER LAYER
**Design Specification:**
- `/shifts/templates`, `/shifts/assignments`
- Bulk assignment endpoint

---

### Section: SERVICE LAYER
**Design Specification:**
- Conflict detection, audit trail

---

### Section: CONFIGURATION
**Design Specification:**
- Overtime rules in `application.yml`

---

## EPIC E06 - Leave & Absence Management

### Section: OVERVIEW
**Description:**Handles PTO, sick, unpaid leave requests/approvals, accruals, and policy updates.

**Design Specification:**
- LeaveRequest, LeavePolicy entities
- Approval workflow
- Accrual balance tracking

**Sample Implementation:**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private LocalDate startDate;
    private LocalDate endDate;
    private String type; // PTO, SICK, UNPAID
    private String status; // PENDING, APPROVED, DENIED
}
```

---

### Section: CONTROLLER LAYER
**Design Specification:**
- `/leave/requests`, `/leave/policies`

---

### Section: SERVICE LAYER
**Design Specification:**
- Balance update logic
- Shift coverage flagging

---

## EPIC E07 - Training & Certification Tracking

### Section: OVERVIEW
**Description:**Tracks certifications, expirations, renewals, document uploads, and blocks assignments for expired certs.

**Design Specification:**
- Certification entity: type, employee, expiryDate, documentUrl
- Renewal alert logic

**Sample Implementation:**
```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String type;
    private LocalDate expiryDate;
    private String documentUrl;
}
```

---

### Section: CONTROLLER LAYER
**Design Specification:**
- `/certifications`, `/certifications/alerts`

---

### Section: SERVICE LAYER
**Design Specification:**
- Expiry alert logic (30/7 days)
- Assignment blocking

---

## EPIC E08 - Safety Incidents & OSHA Reporting

### Section: OVERVIEW
**Description:**Records incidents/near-misses, manages investigation workflow, and generates OSHA reports.

**Design Specification:**
- SafetyIncident entity: severity, location, description, status
- Workflow: Open, Investigating, Resolved

**Sample Implementation:**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private String severity;
    private String location;
    private String description;
    private String status; // OPEN, INVESTIGATING, RESOLVED
    @ManyToMany private List<Employee> involvedEmployees;
}
```

---

### Section: CONTROLLER LAYER
**Design Specification:**
- `/safety/incidents`, `/safety/reports`

---

### Section: SERVICE LAYER
**Design Specification:**
- OSHA 300/300A report generation

---

## EPIC E09 - Equipment & Asset Assignment

### Section: OVERVIEW
**Description:**Manages asset registry, check-in/out, certification validation, and asset condition.

**Design Specification:**
- Asset, AssetAssignment entities
- Certification validation before assignment

**Sample Implementation:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String type;
    private String serialNumber;
    private String condition;
    private boolean available;
}
```

---

### Section: CONTROLLER LAYER
**Design Specification:**
- `/assets`, `/assets/assignments`

---

### Section: SERVICE LAYER
**Design Specification:**
- History log per asset/employee

---

## EPIC E10 - Performance Reviews & Goals

### Section: OVERVIEW
**Description:**Implements review cycles, goals, competencies, ratings, comments, and immutable history.

**Design Specification:**
- PerformanceReview, Goal entities
- Acknowledgement workflow

**Sample Implementation:**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String cycle; // Q1-2024, 2024
    private String status; // DRAFT, SUBMITTED, ACKNOWLEDGED
    private String pdfUrl;
}
```

---

### Section: CONTROLLER LAYER
**Design Specification:**
- `/reviews`, `/reviews/export`

---

### Section: SERVICE LAYER
**Design Specification:**
- PDF export, immutable after sign-off

---

## EPIC E11 - Payroll Export Integration

### Section: OVERVIEW
**Description:**Generates payroll-ready files, maps to provider formats, delivers securely, and logs audits.

**Design Specification:**
- PayrollExport entity
- SFTP/API delivery logic
- Retry with backoff

**Sample Implementation:**
```java
@Entity
public class PayrollExport {
    @Id @GeneratedValue private Long id;
    private LocalDate exportDate;
    private String status; // SUCCESS, FAILED, RETRYING
    private String fileUrl;
}
```

---

### Section: SERVICE LAYER
**Design Specification:**
- Reconciliation logic
- Audit logging

---

## EPIC E12 - Notifications & Announcements

### Section: OVERVIEW
**Description:**Implements in-app, email, SMS notifications, opt-in/out, templates, delivery tracking, and quiet hours.

**Design Specification:**
- Notification, Announcement entities
- Channel preferences per user

**Sample Implementation:**
```java
@Entity
public class Notification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String channel; // IN_APP, EMAIL, SMS
    private String template;
    private String status; // SENT, FAILED
}
```

---

### Section: CONTROLLER LAYER
**Design Specification:**
- `/notifications`, `/announcements`

---

### Section: SERVICE LAYER
**Design Specification:**
- Rate limiting, quiet hours logic

---

## EPIC E13 - Integration Layer

### Section: OVERVIEW
**Description:**Exposes REST APIs for HRIS/WMS sync, SSO, and webhooks. Secured with JWT/OAuth2.

**Design Specification:**
- Integration endpoints
- Webhook event publisher

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    @PostMapping("/hris/sync")
    public ResponseEntity<Void> syncHris(@RequestBody HrisSyncDto dto) { ... }
}
```

---

### Section: CONFIGURATION
**Design Specification:**
- JWT/OAuth2 security
- OpenAPI docs

---

## EPIC E14 - Audit Trail & Compliance

### Section: OVERVIEW
**Description:**Centralized audit logging for sensitive changes, tamper-evident storage, export capability.

**Design Specification:**
- AuditLog entity: actor, timestamp, entity, before/after state
- Immutable log table

**Sample Implementation:**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    @Lob private String beforeState;
    @Lob private String afterState;
}
```

---

### Section: SERVICE LAYER
**Design Specification:**
- Aspect for logging changes

**Sample Implementation:**
```java
@Aspect
@Component
public class AuditAspect {
    @AfterReturning(pointcut = "execution(* com.company.ems..service.*.*(..))", returning = "result")
    public void logChange(JoinPoint joinPoint, Object result) { ... }
}
```

---

## EPIC E15 - Reporting & Analytics

### Section: OVERVIEW
**Description:**Provides operational reports, CSV/PDF export, dashboards, and metrics endpoints.

**Design Specification:**
- ReportService for attendance, overtime, leave, certifications, safety KPIs
- Export endpoints

**Sample Implementation:**
```java
@RestController
@RequestMapping("/reports")
public class ReportController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> exportAttendance(@RequestParam Map<String, String> filters) { ... }
}
```

---

## EPIC E16 - Mobile Access (PWA)

### Section: OVERVIEW
**Description:**Responsive PWA for clock-in/out, schedules, leave requests, announcements, with offline queue.

**Design Specification:**
- Spring Boot serves PWA static assets
- REST APIs for mobile flows
- Offline queue for clock events

**Sample Implementation:**
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/pwa/**")
                .addResourceLocations("classpath:/static/pwa/");
    }
}
```

---

## EPIC E17 - Onboarding & Offboarding Workflow

### Section: OVERVIEW
**Description:**Automates account provisioning, initial schedule, training tasks for new hires, and deprovisioning on termination.

**Design Specification:**
- OnboardingTask, OffboardingTask entities
- HRIS integration for new hires
- Asset collection and access revocation logic

**Sample Implementation:**
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String type; // ACCOUNT, SCHEDULE, TRAINING
    private String status; // PENDING, COMPLETED
}
```

---

## Document Summary

**Total Epics Covered:** 17
**Spring Boot Version:** 3.x
**Architecture Pattern:** Layered (Controller â Service â Repository â Entity)
**Security:** Spring Security with RBAC, JWT/OAuth2
**Database:** PostgreSQL with Flyway/Liquibase migrations
**API Documentation:** OpenAPI 3.0 (Swagger)
**Testing:** JUnit 5, MockMvc, TestContainers recommended
**Deployment:** Docker, Kubernetes ready

---

# [END OF DOCUMENT]