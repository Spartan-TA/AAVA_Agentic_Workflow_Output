# Warehouse Employee Management System - Low-Level Technical Design Document

---

## Section: Project Scaffolding & Domain Setup

**Description:**
Establishes the foundational Spring Boot Maven project structure, configures base packages, sets up core modules, integrates Flyway/Liquibase for database migrations, and enables Actuator for health monitoring.

**Design Specification:**
- Spring Boot Maven project with parent `spring-boot-starter-parent`
- Base packages: `com.company.wms` with submodules for `employee`, `scheduling`, `attendance`, `safety`
- Flyway/Liquibase for DB migrations
- Actuator enabled for health checks
- Application runs on port 8080
- README with build/run steps

**Sample Implementation:**
```java
// pom.xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>
<dependencies>
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
</dependencies>

// application.yml
server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info

// Directory structure
com.company.wms
  âââ employee
  âââ scheduling
  âââ attendance
  âââ safety
```

---

## Section: Employee Master Data (CRUD)

**Description:**
Implements CRUD APIs for employee domain, supporting pagination, filtering, soft-delete, and unique constraints on badgeId.

**Design Specification:**
- Entity: Employee (fields: id, name, badgeId, role, department, shiftGroup, hireDate, status)
- Repository: `EmployeeRepository extends JpaRepository<Employee, Long>`
- Service: `EmployeeService` with business logic
- Controller: `EmployeeController` with REST endpoints
- DTOs for request/response
- Validation annotations
- Soft-delete via `deleted` flag
- Pagination and filtering
- OpenAPI/Swagger documentation

**Sample Implementation:**
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badgeId"))
public class Employee {
    @Id @GeneratedValue
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String badgeId;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    private boolean deleted = false;
}

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}

@Service
public class EmployeeService {
    @Transactional
    public Employee createEmployee(EmployeeDto dto) { /* ... */ }
    public Page<Employee> getEmployees(Pageable pageable) { /* ... */ }
    // Other CRUD methods
}

@RestController
@RequestMapping("/employees")
@Tag(name = "Employee API")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { /* ... */ }
    @GetMapping
    public Page<EmployeeDto> list(Pageable pageable) { /* ... */ }
    // Other endpoints
}
```

---

## Section: Role-Based Access Control (RBAC)

**Description:**
Secures endpoints and methods using Spring Security, supporting roles (ADMIN, HR, SUPERVISOR, WORKER), API key/OAuth2, and row-level constraints.

**Design Specification:**
- SecurityConfig with role mappings
- Method security via `@PreAuthorize`
- Endpoint security via `HttpSecurity`
- API key/OAuth2 toggle via config
- Row-level constraints in service/repository
- Security tests for unauthorized/forbidden actions

**Sample Implementation:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
          .authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
            .anyRequest().authenticated()
          .and()
            .oauth2ResourceServer().jwt();
    }
}

@Service
public class EmployeeService {
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == principal.department)")
    public Employee updateEmployee(Employee employee) { /* ... */ }
}
```

---

## Section: Time & Attendance (Clock In/Out)

**Description:**
Provides endpoints for clock-in/out events, geofence validation, device capture, hours calculation, and corrections workflow.

**Design Specification:**
- Entity: AttendanceEvent (employeeId, timestamp, type, deviceId, location)
- Service: AttendanceService (clock-in/out, calculate hours, corrections)
- Controller: AttendanceController (REST endpoints)
- Geofence validation logic
- Approval workflow for corrections
- CSV export for reports

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
    private GeoLocation location;
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockInDto dto) { /* ... */ }
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockOutDto dto) { /* ... */ }
}
```

---

## Section: Shift & Schedule Management

**Description:**
Manages recurring shift templates, rotations, overtime rules, blackout dates, and assignment to employees.

**Design Specification:**
- Entity: ShiftTemplate, ShiftAssignment
- Service: ShiftService (CRUD, conflict detection, bulk assignment)
- Controller: ShiftController
- Audit entries for changes

**Sample Implementation:**
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isRecurring;
    // Other fields
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

@Service
public class ShiftService {
    public void assignShift(Long employeeId, Long shiftTemplateId, LocalDate date) { /* ... */ }
    public boolean detectConflict(Long employeeId, LocalDate date) { /* ... */ }
}
```

---

## Section: Leave & Absence Management

**Description:**
Handles PTO, sick, unpaid leave requests, approval workflow, accrual balances, and integration with scheduling/payroll.

**Design Specification:**
- Entity: LeaveRequest (employee, type, start/end, status, balance)
- Service: LeaveService (request, approve/deny, update balances)
- Controller: LeaveController
- Integration hooks for scheduling/payroll

**Sample Implementation:**
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
    private int balance;
}

@Service
public class LeaveService {
    @Transactional
    public LeaveRequest requestLeave(LeaveRequestDto dto) { /* ... */ }
    public void approveLeave(Long requestId) { /* ... */ }
}
```

---

## Section: Training & Certification Tracking

**Description:**
Tracks required certifications, expirations, renewals, blocks assignment to tasks requiring expired certs, and uploads proof documents.

**Design Specification:**
- Entity: Certification (employee, type, expiryDate, documentUrl)
- Service: CertificationService (CRUD, expiry alerts, assignment checks)
- Controller: CertificationController
- Alerts for expiry
- Document upload integration

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

@Service
public class CertificationService {
    public void checkCertificationValidity(Long employeeId, String type) { /* ... */ }
    public List<Certification> getExpiringCerts(int days) { /* ... */ }
}
```

---

## Section: Safety Incidents & OSHA Reporting

**Description:**
Records incidents/near-misses, severity, location, description, involved employees, investigation workflow, and OSHA summary generation.

**Design Specification:**
- Entity: SafetyIncident (fields for severity, location, description, status)
- Service: SafetyService (record, workflow, export)
- Controller: SafetyController
- Metrics dashboard endpoints

**Sample Implementation:**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String description;
    private String location;
    private Severity severity;
    @ManyToMany
    private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
}

@Service
public class SafetyService {
    public SafetyIncident recordIncident(SafetyIncidentDto dto) { /* ... */ }
    public void updateStatus(Long incidentId, IncidentStatus status) { /* ... */ }
}
```

---

## Section: Equipment & Asset Assignment

**Description:**
Assigns assets to employees, tracks checkout/return, prevents use if certification missing, maintains asset condition state.

**Design Specification:**
- Entity: Asset, AssetAssignment
- Service: AssetService (CRUD, check-in/out, certification validation)
- Controller: AssetController
- History log per asset/employee

**Sample Implementation:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private AssetCondition condition;
}

@Entity
public class AssetAssignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Asset asset;
    @ManyToOne
    private Employee employee;
    private LocalDateTime checkoutTime;
    private LocalDateTime returnTime;
}

@Service
public class AssetService {
    public void assignAsset(Long assetId, Long employeeId) { /* ... */ }
    public void returnAsset(Long assetId, Long employeeId) { /* ... */ }
}
```

---

## Section: Performance Reviews & Goals

**Description:**
Creates review templates, tracks goals, competencies, ratings, comments, and manages supervisor/employee acknowledgements.

**Design Specification:**
- Entity: PerformanceReview, ReviewCycle
- Service: ReviewService (create, assign, submit, acknowledge)
- Controller: ReviewController
- PDF export
- Immutable history after sign-off

**Sample Implementation:**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String goals;
    private String competencies;
    private int rating;
    private String comments;
    private boolean acknowledged;
}

@Service
public class ReviewService {
    public PerformanceReview submitReview(ReviewDto dto) { /* ... */ }
    public void acknowledgeReview(Long reviewId) { /* ... */ }
}
```

---

## Section: Payroll Export Integration

**Description:**
Generates payroll-ready files from attendance/leave, maps to external provider formats, and delivers securely via SFTP/API.

**Design Specification:**
- Service: PayrollExportService (generate, map, deliver, retry)
- Integration with SFTP/API
- Audit log for exports

**Sample Implementation:**
```java
@Service
public class PayrollExportService {
    public File generatePayrollFile(LocalDate periodStart, LocalDate periodEnd) { /* ... */ }
    public void deliverPayroll(File file) { /* ... */ }
}
```

---

## Section: Notifications & Announcements

**Description:**
Sends in-app, email/SMS notifications for shift changes, cert expirations, approvals, announcements, with quiet hours configuration.

**Design Specification:**
- Entity: Notification, Announcement
- Service: NotificationService (send, track, rate limit)
- Controller: NotificationController
- Opt-in/out per channel
- Localized templates

**Sample Implementation:**
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee recipient;
    private String message;
    private NotificationChannel channel;
    private LocalDateTime sentAt;
    private boolean delivered;
}

@Service
public class NotificationService {
    public void sendNotification(NotificationDto dto) { /* ... */ }
}
```

---

## Section: Integration Layer (HRIS/WMS APIs)

**Description:**
Exposes REST APIs and connectors for HRIS, WMS, and IDP for SSO; supports webhooks for events.

**Design Specification:**
- REST controllers for HRIS/WMS endpoints
- JWT/OAuth2 security
- Sync jobs for HRIS
- Webhook endpoints
- OpenAPI documentation

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/hris")
public class HRISController {
    @PostMapping("/sync")
    public ResponseEntity<?> syncEmployees(@RequestBody List<EmployeeDto> employees) { /* ... */ }
}

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {
    @PostMapping
    public ResponseEntity<?> receiveEvent(@RequestBody WebhookEventDto event) { /* ... */ }
}
```

---

## Section: Audit Trail & Compliance

**Description:**
Centralized audit logging for sensitive changes, tamper-evident storage, and export capabilities.

**Design Specification:**
- Entity: AuditLog (actor, timestamp, entity, before/after)
- Service: AuditService (log, export)
- Immutable log table
- Export by date/user/entity

**Sample Implementation:**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    private String before;
    private String after;
}

@Service
public class AuditService {
    public void logChange(String actor, String entity, String before, String after) { /* ... */ }
    public List<AuditLog> exportLogs(LocalDate from, LocalDate to) { /* ... */ }
}
```

---

## Section: Reporting & Analytics

**Description:**
Provides operational reports, CSV/PDF export, and role-based dashboards for attendance, overtime, leave, certifications, safety KPIs.

**Design Specification:**
- Service: ReportingService (generate, filter, export)
- Controller: ReportingController
- Metrics endpoints for BI

**Sample Implementation:**
```java
@Service
public class ReportingService {
    public Report generateAttendanceReport(LocalDate from, LocalDate to, String department) { /* ... */ }
    public File exportReport(Report report, ExportFormat format) { /* ... */ }
}

@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<ReportDto> getAttendanceReport(@RequestParam LocalDate from, @RequestParam LocalDate to) { /* ... */ }
}
```

---

## Section: Mobile Access (PWA)

**Description:**
Implements responsive views for clock-in/out, schedule viewing, leave requests, announcements, and offline support via PWA.

**Design Specification:**
- PWA manifest and service worker
- Offline queue for clock events
- REST endpoints for mobile flows
- Lighthouse PWA score â¥ 80

**Sample Implementation:**
```javascript
// manifest.json
{
  "name": "Warehouse Employee Management",
  "short_name": "WMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "icons": [ /* ... */ ]
}

// service-worker.js
self.addEventListener('fetch', function(event) {
  // Offline queue logic
});
```

---

## Section: Onboarding & Offboarding Workflow

**Description:**
Automates provisioning of accounts, initial schedule, required training, and deprovisioning of access/assets on termination.

**Design Specification:**
- Service: OnboardingService, OffboardingService
- Integration with HRIS, asset assignment, training modules
- Workflow tasks for onboarding/offboarding

**Sample Implementation:**
```java
@Service
public class OnboardingService {
    public void provisionEmployee(EmployeeDto dto) { /* ... */ }
    public void assignInitialSchedule(Long employeeId) { /* ... */ }
}

@Service
public class OffboardingService {
    public void deprovisionEmployee(Long employeeId) { /* ... */ }
    public void collectAssets(Long employeeId) { /* ... */ }
}
```

---

## Section: Multi-Tenant & Localization

**Description:**
Supports data isolation per tenant and locale-specific configuration.

**Design Specification:**
- Tenant-aware entities (tenantId field)
- Locale configuration in application.yml
- Data isolation via repository filters

**Sample Implementation:**
```java
@Entity
public class Employee {
    // ... other fields ...
    private String tenantId;
}

@Configuration
public class LocaleConfig {
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.US);
        return slr;
    }
}
```

---

## Section: Observability & Monitoring

**Description:**
Exposes Prometheus metrics, structured logging, tracing, and SLO alerting.

**Design Specification:**
- Micrometer for Prometheus metrics
- Structured logging with Logback
- Distributed tracing with Sleuth/Zipkin
- Alerting configuration

**Sample Implementation:**
```java
@Configuration
public class MetricsConfig {
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("application", "wms");
    }
}

// application.yml
management:
  metrics:
    export:
      prometheus:
        enabled: true
```

---

## Section: CI/CD & Deployment Automation

**Description:**
Implements CI/CD pipeline for build/test, Docker image build/push, automated deployment to staging, and production deployment with approval.

**Design Specification:**
- GitHub Actions/Jenkins pipeline
- Docker image build
- Automated deployment scripts
- Approval gates for production

**Sample Implementation:**
```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build with Maven
        run: mvn clean install
      - name: Build Docker image
        run: docker build -t wms:latest .
      - name: Push to registry
        run: docker push wms:latest
      - name: Deploy to staging
        run: ./deploy-staging.sh
```

---

## Section: Database Schema

**Description:**
Defines tables, indexes, constraints, and relationships for all entities.

**Design Specification:**
- Flyway/Liquibase migration scripts
- Tables for Employee, AttendanceEvent, ShiftTemplate, LeaveRequest, Certification, SafetyIncident, Asset, PerformanceReview, Notification, AuditLog
- Indexes on frequently queried fields
- Foreign key constraints

**Sample Implementation:**
```sql
-- V1__create_employee_table.sql
CREATE TABLE employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    badge_id VARCHAR(50) UNIQUE NOT NULL,
    role VARCHAR(50),
    department VARCHAR(100),
    shift_group VARCHAR(50),
    hire_date DATE,
    status VARCHAR(50),
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_badge_id ON employees(badge_id);
```

---

## Section: Configuration Management

**Description:**
Manages application configuration via application.yml, profiles, and externalized configuration.

**Design Specification:**
- application.yml for default config
- application-dev.yml, application-prod.yml for profiles
- Externalized secrets via environment variables

**Sample Implementation:**
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wms
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true

# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://prod-db:5432/wms
```

---

## Section: Exception Handling

**Description:**
Centralized exception handling for REST APIs.

**Design Specification:**
- @ControllerAdvice for global exception handling
- Custom exceptions for business logic
- Proper HTTP status codes

**Sample Implementation:**
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidation(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
```

---

## Section: Validation & DTOs

**Description:**
Implements validation annotations and DTO patterns for request/response.

**Design Specification:**
- DTOs for all REST endpoints
- Validation annotations (@NotNull, @NotBlank, @Size, etc.)
- MapStruct for entity-DTO mapping

**Sample Implementation:**
```java
public class EmployeeDto {
    @NotBlank
    private String name;
    @NotBlank
    private String badgeId;
    @NotNull
    private Role role;
    // Other fields
}

@Mapper
public interface EmployeeMapper {
    EmployeeDto toDto(Employee employee);
    Employee toEntity(EmployeeDto dto);
}
```

---

## Section: Testing Strategy

**Description:**
Defines unit, integration, and end-to-end testing strategies.

**Design Specification:**
- JUnit 5 for unit tests
- MockMvc for controller tests
- Testcontainers for integration tests
- Security tests for RBAC

**Sample Implementation:**
```java
@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCreateEmployee() throws Exception {
        mockMvc.perform(post("/employees")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{"name":"John Doe","badgeId":"12345"}"))
            .andExpect(status().isCreated());
    }
}
```

---

## Conclusion

This comprehensive low-level technical design document covers all 61 user stories for the Warehouse Employee Management System, structured according to Spring Boot best practices and industry standards. Each section provides detailed design specifications, sample implementations, and code snippets to guide the development team.

---

**Document Metadata:**
- **Version:** 1.0
- **Date:** 2024
- **Author:** Senior Software Architect
- **Status:** Final
- **Coverage:** All 61 User Stories (E01-E20)
