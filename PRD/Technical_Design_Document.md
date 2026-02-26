# WAREHOUSE EMPLOYEE MANAGEMENT SYSTEM
# LOW-LEVEL TECHNICAL DESIGN DOCUMENT

## Document Overview
This document provides comprehensive low-level technical design specifications for all 20 epics of the Warehouse Employee Management System, built using Spring Boot framework and following industry best practices.

---

## Section: E01 - Project Scaffolding & Domain Setup

### Description
Establishes the foundational Spring Boot project structure, configures core modules, sets up database migration, and enables monitoring.

### Design Specification
- Spring Boot (Maven) project initialized with base packages: com.company.wms (root), .employee, .scheduling, .attendance, .safety, .common, .config, .audit, .integration, .reporting, .notification, .asset, .leave, .performance, .payroll, .mobile, .localization, .portal
- Flyway/Liquibase for DB migrations
- Spring Boot Actuator enabled
- application.yml with port 8080, DB, and actuator config
- README with build/run steps

### Sample Implementation
```java
// pom.xml: Spring Boot Starter, Data JPA, Web, Security, Actuator, Flyway
// application.yml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wms
    username: wms_user
    password: secret
  flyway:
    enabled: true
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
  âââ common
  âââ config
  âââ audit
  âââ integration
  âââ reporting
  âââ notification
  âââ asset
  âââ leave
  âââ performance
  âââ payroll
  âââ mobile
  âââ localization
  âââ portal

// Flyway migration V1__baseline.sql
CREATE TABLE employee (...);
```

---

## Section: E02 - Employee Master Data (CRUD)

### Description
Manages employee records with full CRUD, soft-delete, filtering, and OpenAPI documentation.

### Design Specification
- Entity: Employee (id, name, badgeId [unique], role, department, shiftGroup, hireDate, status, deleted)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>, with findByBadgeId, filtering, pagination
- Service: EmployeeService with transactional CRUD, soft-delete (set deleted=true)
- Controller: EmployeeController with /employees endpoints (POST, GET, PUT, PATCH, DELETE), DTOs for requests/responses
- OpenAPI annotations with examples
- Validation: badgeId unique, required fields

### Sample Implementation
```java
@Entity
@Table(name = "employee", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
  @Id @GeneratedValue private Long id;
  @Column(nullable=false) private String name;
  @Column(name="badge_id", nullable=false, unique=true) private String badgeId;
  @Enumerated(EnumType.STRING) private Role role;
  private String department;
  private String shiftGroup;
  private LocalDate hireDate;
  @Enumerated(EnumType.STRING) private Status status;
  private boolean deleted = false;
}

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  Optional<Employee> findByBadgeId(String badgeId);
  Page<Employee> findAllByDeletedFalse(Pageable pageable);
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
  @PostMapping public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) {...}
  @GetMapping public Page<EmployeeDto> list(...) {...}
  @DeleteMapping("/{id}") public void softDelete(@PathVariable Long id) {...}
}
```

---

## Section: E03 - Role-Based Access Control (RBAC)

### Description
Implements security with roles, endpoint/method security, and row-level constraints.

### Design Specification
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- SecurityConfig: Spring Security, method security, OAuth2/API key toggle via application.yml
- Row-level: SUPERVISOR can access only their team
- Unauthorized (401), forbidden (403) responses
- Security tests for all rules

### Sample Implementation
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
      .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
      .antMatchers("/admin/**").hasRole("ADMIN")
      .anyRequest().authenticated()
      .and().oauth2ResourceServer().jwt();
  }
}

@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(#id, authentication))")
public Employee getEmployee(Long id) {...}
```

---

## Section: E04 - Time & Attendance (Clock In/Out)

### Description
Handles clock-in/out events, shift association, corrections, and reporting.

### Design Specification
- Entity: AttendanceEvent (id, employee, type [IN/OUT], timestamp, deviceId, location, shiftId, approved, correctionRequested)
- Repository: AttendanceRepository with findByEmployeeAndDate, etc.
- Service: AttendanceService for event validation, shift association, correction workflow
- Controller: /attendance/clock-in, /clock-out, /corrections, /reports
- CSV export utility

### Sample Implementation
```java
@Entity
public class AttendanceEvent {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  @Enumerated(EnumType.STRING) private EventType type;
  private LocalDateTime timestamp;
  private String deviceId;
  private String location;
  private Long shiftId;
  private boolean approved;
  private boolean correctionRequested;
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
  @PostMapping("/clock-in") public ResponseEntity<?> clockIn(@RequestBody ClockInDto dto) {...}
  @PostMapping("/clock-out") public ResponseEntity<?> clockOut(@RequestBody ClockOutDto dto) {...}
  @GetMapping("/reports") public void exportCsv(...) {...}
}
```

---

## Section: E05 - Shift & Schedule Management

### Description
Manages shift templates, scheduling, conflict detection, and audit logging.

### Design Specification
- Entities: ShiftTemplate, ShiftAssignment, BlackoutDate, OperationCalendar
- Repository: ShiftTemplateRepository, ShiftAssignmentRepository
- Service: ShiftService for CRUD, conflict detection, bulk assignment, audit
- Controller: /shifts, /schedules endpoints
- Audit logging on changes

### Sample Implementation
```java
@Entity
public class ShiftTemplate {
  @Id @GeneratedValue private Long id;
  private String name;
  private LocalTime startTime;
  private LocalTime endTime;
  private boolean recurring;
  // ...
}

@RestController
@RequestMapping("/shifts")
public class ShiftController {
  @PostMapping public ShiftTemplateDto create(@RequestBody ShiftTemplateDto dto) {...}
  @PostMapping("/assign") public void bulkAssign(@RequestBody BulkAssignDto dto) {...}
}
```

---

## Section: E06 - Leave & Absence Management

### Description
Handles leave requests, approvals, accruals, and integration with scheduling/payroll.

### Design Specification
- Entities: LeaveRequest (id, employee, type, start, end, status, accrualBalance)
- Repository: LeaveRequestRepository
- Service: LeaveService for request, approval, accrual update, integration hooks
- Controller: /leave endpoints
- Exports for approved leaves

### Sample Implementation
```java
@Entity
public class LeaveRequest {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  @Enumerated(EnumType.STRING) private LeaveType type;
  private LocalDate startDate;
  private LocalDate endDate;
  @Enumerated(EnumType.STRING) private LeaveStatus status;
  private BigDecimal accrualBalance;
}

@RestController
@RequestMapping("/leave")
public class LeaveController {
  @PostMapping public LeaveRequestDto request(@RequestBody LeaveRequestDto dto) {...}
  @PostMapping("/{id}/approve") public void approve(@PathVariable Long id) {...}
}
```

---

## Section: E07 - Training & Certification Tracking

### Description
Tracks certifications, expirations, renewals, and document uploads.

### Design Specification
- Entities: Certification (id, employee, type, issueDate, expiryDate, documentUrl, status)
- Repository: CertificationRepository
- Service: CertificationService for CRUD, expiry alerts, scheduling checks
- Controller: /certifications endpoints
- Document upload integration

### Sample Implementation
```java
@Entity
public class Certification {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String type;
  private LocalDate issueDate;
  private LocalDate expiryDate;
  private String documentUrl;
  @Enumerated(EnumType.STRING) private CertificationStatus status;
}

@RestController
@RequestMapping("/certifications")
public class CertificationController {
  @PostMapping public CertificationDto create(@RequestBody CertificationDto dto) {...}
  @GetMapping("/alerts") public List<CertificationAlertDto> getAlerts() {...}
}
```

---

## Section: E08 - Safety Incidents & OSHA Reporting

### Description
Records safety incidents, manages investigation workflow, and generates OSHA reports.

### Design Specification
- Entities: SafetyIncident (id, date, severity, location, description, involvedEmployees, status, correctiveActions)
- Repository: SafetyIncidentRepository
- Service: SafetyService for workflow, OSHA export, metrics
- Controller: /safety/incidents, /safety/reports

### Sample Implementation
```java
@Entity
public class SafetyIncident {
  @Id @GeneratedValue private Long id;
  private LocalDateTime date;
  private String severity;
  private String location;
  private String description;
  @ManyToMany private List<Employee> involvedEmployees;
  @Enumerated(EnumType.STRING) private IncidentStatus status;
  private String correctiveActions;
}

@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
  @PostMapping public SafetyIncidentDto report(@RequestBody SafetyIncidentDto dto) {...}
  @GetMapping("/osharesume") public void exportOshaSummary(...) {...}
}
```

---

## Section: E09 - Equipment & Asset Assignment

### Description
Manages asset registry, assignment, check-in/out, and certification checks.

### Design Specification
- Entities: Asset (id, type, serial, condition, assignedTo, checkedOutAt, dueBack, history)
- Repository: AssetRepository
- Service: AssetService for CRUD, check-in/out, overdue, certification validation
- Controller: /assets endpoints

### Sample Implementation
```java
@Entity
public class Asset {
  @Id @GeneratedValue private Long id;
  private String type;
  private String serial;
  private String condition;
  @ManyToOne private Employee assignedTo;
  private LocalDateTime checkedOutAt;
  private LocalDateTime dueBack;
  @ElementCollection private List<AssetHistoryEntry> history;
}

@RestController
@RequestMapping("/assets")
public class AssetController {
  @PostMapping("/checkout") public void checkout(@RequestBody AssetCheckoutDto dto) {...}
  @PostMapping("/checkin") public void checkin(@RequestBody AssetCheckinDto dto) {...}
}
```

---

## Section: E10 - Performance Reviews & Goals

### Description
Manages review cycles, goals, ratings, and immutable history.

### Design Specification
- Entities: PerformanceReview (id, employee, cycle, goals, competencies, ratings, comments, status, signedOff)
- Repository: PerformanceReviewRepository
- Service: PerformanceReviewService for cycle management, workflow, PDF export
- Controller: /reviews endpoints

### Sample Implementation
```java
@Entity
public class PerformanceReview {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String cycle;
  @ElementCollection private List<Goal> goals;
  @ElementCollection private List<CompetencyRating> competencies;
  private String comments;
  @Enumerated(EnumType.STRING) private ReviewStatus status;
  private boolean signedOff;
}

@RestController
@RequestMapping("/reviews")
public class PerformanceReviewController {
  @PostMapping public PerformanceReviewDto create(@RequestBody PerformanceReviewDto dto) {...}
  @PostMapping("/{id}/signoff") public void signoff(@PathVariable Long id) {...}
}
```

---

## Section: E11 - Payroll Export Integration

### Description
Generates payroll files from attendance/leave, maps to provider schema, and delivers securely.

### Design Specification
- Service: PayrollExportService for data aggregation, mapping, file generation (CSV/XML), SFTP/API delivery, retry/backoff
- Controller: /payroll/exports endpoint
- Audit log for each export

### Sample Implementation
```java
@Service
public class PayrollExportService {
  @Transactional
  public void exportPayroll(PayrollExportRequest req) {
    // Aggregate, map, generate file, deliver via SFTP/API, log audit
  }
}

@RestController
@RequestMapping("/payroll/exports")
public class PayrollExportController {
  @PostMapping public void export(@RequestBody PayrollExportRequest req) {...}
}
```

---

## Section: E12 - Notifications & Announcements

### Description
Sends notifications via in-app, email, SMS; supports opt-in/out, templates, and rate limits.

### Design Specification
- Entities: Notification (id, user, type, channel, content, status, deliveryTime)
- Service: NotificationService for delivery, status tracking, rate limiting
- Controller: /notifications, /announcements endpoints
- Integration: Email/SMS providers

### Sample Implementation
```java
@Entity
public class Notification {
  @Id @GeneratedValue private Long id;
  @ManyToOne private User user;
  @Enumerated(EnumType.STRING) private NotificationType type;
  @Enumerated(EnumType.STRING) private Channel channel;
  private String content;
  @Enumerated(EnumType.STRING) private DeliveryStatus status;
  private LocalDateTime deliveryTime;
}

@RestController
@RequestMapping("/notifications")
public class NotificationController {
  @PostMapping public void send(@RequestBody NotificationDto dto) {...}
}
```

---

## Section: E13 - Integration Layer (HRIS/WMS APIs)

### Description
Exposes REST APIs/connectors for HRIS, WMS, SSO, and webhooks.

### Design Specification
- REST controllers: /api/hris, /api/wms, /api/idp, /api/webhooks
- JWT/OAuth2 security
- HRIS sync job (scheduled)
- Idempotent webhook handling
- OpenAPI documentation

### Sample Implementation
```java
@RestController
@RequestMapping("/api/hris")
public class HRISController {
  @PostMapping("/sync") public void sync(@RequestBody HRISSyncDto dto) {...}
}

@Configuration
@EnableScheduling
public class IntegrationConfig {}
```

---

## Section: E14 - Audit Trail & Compliance

### Description
Centralizes audit logging for sensitive changes with tamper-evident storage.

### Design Specification
- Entity: AuditLog (id, entity, entityId, actor, timestamp, before, after, action)
- Service: AuditService for logging on create/update/delete
- Controller: /audit/logs endpoint
- Immutable log table

### Sample Implementation
```java
@Entity
public class AuditLog {
  @Id @GeneratedValue private Long id;
  private String entity;
  private Long entityId;
  private String actor;
  private LocalDateTime timestamp;
  @Lob private String before;
  @Lob private String after;
  private String action;
}

@RestController
@RequestMapping("/audit/logs")
public class AuditLogController {
  @GetMapping public List<AuditLogDto> getLogs(...) {...}
}
```

---

## Section: E15 - Reporting & Analytics

### Description
Provides operational reports, exports, and dashboards.

### Design Specification
- Service: ReportingService for attendance, overtime, leave, certs, safety KPIs
- Controller: /reports endpoints (CSV/PDF export)
- Role-based dashboard endpoints

### Sample Implementation
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
  @GetMapping("/attendance") public void exportAttendance(...) {...}
  @GetMapping("/dashboard") public DashboardDto getDashboard(...) {...}
}
```

---

## Section: E16 - Mobile Access (PWA)

### Description
Enables responsive, offline-friendly mobile access for core flows.

### Design Specification
- Spring Boot serves PWA static assets (manifest.json, service-worker.js)
- REST endpoints for clock-in/out, schedules, leave, announcements
- Offline queue for clock events (local storage, sync on reconnect)

### Sample Implementation
```java
// resources/static/manifest.json, service-worker.js
@RestController
@RequestMapping("/mobile")
public class MobileController {
  @GetMapping("/shifts") public List<ShiftDto> getShifts(...) {...}
}
```

---

## Section: E17 - Onboarding & Offboarding Workflow

### Description
Automates provisioning, initial schedule, training, and deprovisioning.

### Design Specification
- Service: OnboardingService for new hire tasks, asset assignment, training
- OffboardingService for access revocation, asset collection, schedule update
- Integration with HRIS, asset, training modules

### Sample Implementation
```java
@Service
public class OnboardingService {
  public void onboard(Employee employee) {...}
}

@Service
public class OffboardingService {
  public void offboard(Employee employee) {...}
}
```

---

## Section: E18 - Localization & Multi-Tenant

### Description
Supports multiple tenants and locales.

### Design Specification
- TenantId in all entities/queries
- Locale from Accept-Language header
- i18n message bundles (messages_en.properties, messages_es.properties)
- Tenant-specific config

### Sample Implementation
```java
@Entity
public class Employee {
  // ...
  private String tenantId;
}

@Configuration
public class LocaleConfig extends WebMvcConfigurerAdapter {
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new LocaleChangeInterceptor());
  }
}
```

---

## Section: E19 - Advanced Scheduling Optimization

### Description
Uses rules engine/solver for constraint-based shift assignment.

### Design Specification
- Service: SchedulingOptimizerService (uses OptaPlanner or similar)
- Constraints: skills, availability, preferences, fairness
- Manual override support
- Auditable results

### Sample Implementation
```java
@Service
public class SchedulingOptimizerService {
  public List<ShiftAssignment> optimize(List<Employee> employees, List<ShiftTemplate> shifts) {...}
}
```

---

## Section: E20 - Self-Service Portal

### Description
Provides employee/supervisor portal for profile, documents, pay stubs, shift swaps.

### Design Specification
- REST endpoints: /portal/profile, /portal/documents, /portal/paystubs, /portal/shift-swaps
- Supervisor endpoints for team management
- Access control via RBAC

### Sample Implementation
```java
@RestController
@RequestMapping("/portal")
public class PortalController {
  @GetMapping("/profile") public ProfileDto getProfile(...) {...}
  @PostMapping("/shift-swaps/request") public void requestSwap(...) {...}
  @PostMapping("/shift-swaps/approve") public void approveSwap(...) {...}
}
```

---

## Cross-Cutting Concerns

### Exception Handling and Validation
**Applies to all epics**

#### Design Specification
- @ControllerAdvice for global exception handling
- @Valid and custom validators for DTOs
- Error responses with codes/messages
- Standardized error format (RFC 7807 Problem Details)

#### Sample Implementation
```java
@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
    return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
  }
  
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
  }
}
```

---

### Testing Approach
**Applies to all epics**

#### Design Specification
- Unit tests for services (JUnit 5, Mockito)
- Integration tests for controllers (SpringBootTest, MockMvc)
- Security tests for RBAC
- Flyway migration tests
- API contract tests (OpenAPI)
- Test coverage minimum 80%

#### Sample Implementation
```java
@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {
  @Autowired private MockMvc mockMvc;
  
  @Test
  void testCreateEmployee() throws Exception {
    mockMvc.perform(post("/employees")
      .contentType(MediaType.APPLICATION_JSON)
      .content("{"name":"John Doe","badgeId":"12345"}"))
      .andExpect(status().isCreated());
  }
}

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
  @Mock private EmployeeRepository repository;
  @InjectMocks private EmployeeService service;
  
  @Test
  void testFindByBadgeId() {
    when(repository.findByBadgeId("12345")).thenReturn(Optional.of(new Employee()));
    assertNotNull(service.findByBadgeId("12345"));
  }
}
```

---

### Database Schema Design
**Applies to all epics**

#### Design Specification
- Each entity mapped to a table with appropriate constraints, indexes, and foreign keys
- Flyway migration scripts for each module
- Naming convention: snake_case for tables/columns
- Audit columns: created_at, updated_at, created_by, updated_by
- Soft delete support with deleted flag

#### Sample Implementation
```sql
-- V1__baseline.sql
CREATE TABLE employee (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  badge_id VARCHAR(50) NOT NULL UNIQUE,
  role VARCHAR(50) NOT NULL,
  department VARCHAR(100),
  shift_group VARCHAR(50),
  hire_date DATE,
  status VARCHAR(20) NOT NULL,
  deleted BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(100),
  updated_by VARCHAR(100)
);

CREATE INDEX idx_employee_badge_id ON employee(badge_id);
CREATE INDEX idx_employee_department ON employee(department);
CREATE INDEX idx_employee_deleted ON employee(deleted);

-- V2__attendance.sql
CREATE TABLE attendance_event (
  id BIGSERIAL PRIMARY KEY,
  employee_id BIGINT NOT NULL REFERENCES employee(id),
  event_type VARCHAR(10) NOT NULL,
  timestamp TIMESTAMP NOT NULL,
  device_id VARCHAR(100),
  location VARCHAR(255),
  shift_id BIGINT,
  approved BOOLEAN DEFAULT FALSE,
  correction_requested BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attendance_employee ON attendance_event(employee_id);
CREATE INDEX idx_attendance_timestamp ON attendance_event(timestamp);
```

---

### Security Configurations
**Applies to all epics**

#### Design Specification
- OAuth2/JWT or API key toggle via application.yml
- Method/endpoint security with @PreAuthorize
- Row-level security where needed
- CORS configuration for frontend integration
- CSRF protection for state-changing operations
- Rate limiting for API endpoints

#### Sample Implementation
```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
  @Value("${security.auth.type}")
  private String authType;
  
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    if ("oauth2".equals(authType)) {
      http.oauth2ResourceServer().jwt();
    } else {
      http.addFilterBefore(new ApiKeyAuthFilter(), UsernamePasswordAuthenticationFilter.class);
    }
    
    http
      .cors().and()
      .csrf().disable()
      .authorizeRequests()
        .antMatchers("/actuator/health").permitAll()
        .antMatchers("/api/public/**").permitAll()
        .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
        .antMatchers("/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated();
  }
  
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}

@Component
public class EmployeeSecurity {
  public boolean isTeamMember(Long employeeId, Authentication auth) {
    // Check if authenticated user is supervisor of the employee
    return true; // Implementation logic
  }
}
```

---

### Integration Points
**Applies to relevant epics**

#### Design Specification
- HRIS integration for employee sync
- WMS integration for location/department data
- IDP integration for SSO
- Email/SMS providers for notifications
- SFTP/API for payroll file delivery
- External reporting systems
- Document storage (S3, Azure Blob)

#### Sample Implementation
```java
@Service
public class HRISIntegrationService {
  @Autowired private RestTemplate restTemplate;
  @Autowired private EmployeeService employeeService;
  
  @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
  public void syncEmployees() {
    ResponseEntity<HRISEmployeeDto[]> response = restTemplate.exchange(
      "https://hris.company.com/api/employees",
      HttpMethod.GET,
      createAuthenticatedRequest(),
      HRISEmployeeDto[].class
    );
    
    Arrays.stream(response.getBody())
      .forEach(dto -> employeeService.createOrUpdate(dto));
  }
  
  private HttpEntity<?> createAuthenticatedRequest() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(getHRISToken());
    return new HttpEntity<>(headers);
  }
}

@Service
public class NotificationIntegrationService {
  @Autowired private JavaMailSender mailSender;
  @Autowired private TwilioClient twilioClient;
  
  public void sendEmail(String to, String subject, String body) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject(subject);
    message.setText(body);
    mailSender.send(message);
  }
  
  public void sendSMS(String phoneNumber, String message) {
    twilioClient.sendMessage(phoneNumber, message);
  }
}
```

---

## Configuration Management

### Application Configuration

#### application.yml
```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  application:
    name: warehouse-employee-management
  
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:wms}
    username: ${DB_USER:wms_user}
    password: ${DB_PASSWORD:secret}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI:https://auth.company.com}
          jwk-set-uri: ${JWT_JWK_SET_URI:https://auth.company.com/.well-known/jwks.json}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true

security:
  auth:
    type: ${AUTH_TYPE:oauth2} # oauth2 or apikey

integration:
  hris:
    url: ${HRIS_URL:https://hris.company.com/api}
    api-key: ${HRIS_API_KEY}
  wms:
    url: ${WMS_URL:https://wms.company.com/api}
    api-key: ${WMS_API_KEY}
  payroll:
    sftp:
      host: ${PAYROLL_SFTP_HOST}
      port: ${PAYROLL_SFTP_PORT:22}
      username: ${PAYROLL_SFTP_USER}
      password: ${PAYROLL_SFTP_PASSWORD}

notification:
  email:
    enabled: true
    from: noreply@company.com
  sms:
    enabled: true
    provider: twilio

logging:
  level:
    com.company.wms: INFO
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

---

## API Documentation

### OpenAPI Configuration

```java
@Configuration
public class OpenAPIConfig {
  
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
      .info(new Info()
        .title("Warehouse Employee Management API")
        .version("1.0")
        .description("Comprehensive API for managing warehouse employees, attendance, scheduling, and more")
        .contact(new Contact()
          .name("API Support")
          .email("api-support@company.com")))
      .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
      .components(new Components()
        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
          .type(SecurityScheme.Type.HTTP)
          .scheme("bearer")
          .bearerFormat("JWT")));
  }
}
```

### Sample API Documentation Annotations

```java
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee Management", description = "APIs for managing employee master data")
public class EmployeeController {
  
  @Operation(
    summary = "Create new employee",
    description = "Creates a new employee record with the provided details"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Employee created successfully",
      content = @Content(schema = @Schema(implementation = EmployeeDto.class))),
    @ApiResponse(responseCode = "400", description = "Invalid input"),
    @ApiResponse(responseCode = "409", description = "Employee with badge ID already exists")
  })
  @PostMapping
  public ResponseEntity<EmployeeDto> createEmployee(
    @Parameter(description = "Employee details", required = true)
    @Valid @RequestBody EmployeeDto dto) {
    // Implementation
  }
}
```

---

## Performance Optimization

### Caching Strategy

```java
@Configuration
@EnableCaching
public class CacheConfig {
  
  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(Arrays.asList(
      new ConcurrentMapCache("employees"),
      new ConcurrentMapCache("shifts"),
      new ConcurrentMapCache("certifications")
    ));
    return cacheManager;
  }
}

@Service
public class EmployeeService {
  
  @Cacheable(value = "employees", key = "#badgeId")
  public Employee findByBadgeId(String badgeId) {
    return repository.findByBadgeId(badgeId)
      .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
  }
  
  @CacheEvict(value = "employees", key = "#employee.badgeId")
  public Employee updateEmployee(Employee employee) {
    return repository.save(employee);
  }
}
```

### Database Query Optimization

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  
  @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.certifications WHERE e.id = :id")
  Optional<Employee> findByIdWithCertifications(@Param("id") Long id);
  
  @Query("SELECT e FROM Employee e WHERE e.department = :dept AND e.deleted = false")
  Page<Employee> findByDepartment(@Param("dept") String department, Pageable pageable);
  
  @EntityGraph(attributePaths = {"shiftAssignments", "leaveRequests"})
  List<Employee> findAllByShiftGroup(String shiftGroup);
}
```

---

## Monitoring and Observability

### Metrics Configuration

```java
@Configuration
public class MetricsConfig {
  
  @Bean
  public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
    return registry -> registry.config().commonTags(
      "application", "warehouse-employee-management",
      "environment", System.getenv("ENVIRONMENT")
    );
  }
}

@Service
public class AttendanceService {
  
  private final Counter clockInCounter;
  private final Timer clockInTimer;
  
  public AttendanceService(MeterRegistry registry) {
    this.clockInCounter = Counter.builder("attendance.clockin.count")
      .description("Number of clock-in events")
      .register(registry);
    this.clockInTimer = Timer.builder("attendance.clockin.duration")
      .description("Clock-in processing time")
      .register(registry);
  }
  
  public void clockIn(ClockInDto dto) {
    clockInTimer.record(() -> {
      // Process clock-in
      clockInCounter.increment();
    });
  }
}
```

---

## Deployment Architecture

### Docker Configuration

```dockerfile
# Dockerfile
FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/warehouse-employee-management-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose

```yaml
# docker-compose.yml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=wms
      - DB_USER=wms_user
      - DB_PASSWORD=secret
    depends_on:
      - postgres
  
  postgres:
    image: postgres:13
    environment:
      - POSTGRES_DB=wms
      - POSTGRES_USER=wms_user
      - POSTGRES_PASSWORD=secret
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

volumes:
  postgres-data:
```

---

## Conclusion

This comprehensive low-level technical design document provides detailed specifications for implementing all 20 epics of the Warehouse Employee Management System using Spring Boot framework. The design follows industry best practices including:

- **Layered Architecture**: Clear separation of concerns with controller, service, repository layers
- **Security**: Role-based access control with OAuth2/JWT support
- **Data Integrity**: JPA entities with proper relationships and constraints
- **API Design**: RESTful endpoints with OpenAPI documentation
- **Testing**: Comprehensive unit and integration testing strategies
- **Monitoring**: Actuator endpoints and custom metrics
- **Integration**: Well-defined integration points for external systems
- **Performance**: Caching and query optimization strategies
- **Deployment**: Containerization with Docker

Each epic has been designed with:
- Domain models and entity relationships
- Repository specifications
- Service layer business logic
- Controller REST endpoints
- Security configurations
- Sample implementations
- Database schema designs

This document serves as a complete technical blueprint for development teams to implement the Warehouse Employee Management System.

---

**Document Version**: 1.0
**Last Updated**: 2024
**Status**: Ready for Implementation