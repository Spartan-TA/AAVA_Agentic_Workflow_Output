# Technical Design Document: Warehouse Employee Management System

---

Section: Project Scaffolding & Domain Setup  
Description: Establishes the foundational Spring Boot project structure, configures base packages, sets up core modules, database migrations, and enables Actuator for health monitoring. Ensures a standardized, maintainable, and scalable architecture for all subsequent modules.  
Design Specification:  
- Use Spring Boot (Maven) for project initialization.  
- Base package: `com.wms.employeesystem`.  
- Modules: `employee`, `scheduling`, `attendance`, `safety`.  
- Database migration: Integrate Flyway or Liquibase.  
- Actuator enabled for health endpoints.  
- README with build/run instructions.  
- Application runs on port 8080.  
Sample Implementation:  
```java
// pom.xml: Spring Boot starter dependencies
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>

// application.properties
server.port=8080
management.endpoints.web.exposure.include=health,info
spring.flyway.enabled=true
spring.datasource.url=jdbc:postgresql://localhost:5432/wms
spring.datasource.username=wms_user
spring.datasource.password=secret

// Directory structure
src/main/java/com/wms/employeesystem/
    âââ employee/
    âââ scheduling/
    âââ attendance/
    âââ safety/
```

---

Section: Employee Master Data (CRUD)  
Description: Implements the Employee domain model and exposes CRUD APIs with pagination, filtering, and soft-delete. Ensures unique badge IDs and supports OpenAPI documentation.  
Design Specification:  
- Entity: `Employee` (fields: id, name, badgeId, role, department, shiftGroup, hireDate, status).  
- Repository: `EmployeeRepository` (extends JpaRepository).  
- Service: `EmployeeService` (business logic, soft-delete).  
- Controller: `EmployeeController` (REST endpoints).  
- DTOs for web/API layer.  
- Pagination and filtering via Spring Data.  
- OpenAPI/Swagger documentation.  
Sample Implementation:  
```java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    private String name;
    @Column(unique = true)
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted;
}

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Page<Employee> findByDeletedFalse(Pageable pageable);
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @GetMapping
    public Page<EmployeeDto> list(Pageable pageable, @RequestParam Map<String, String> filters) {...}
    @PostMapping
    public EmployeeDto create(@RequestBody EmployeeDto dto) {...}
    // PUT, PATCH, DELETE endpoints
}
```

---

Section: Role-Based Access Control (RBAC)  
Description: Integrates Spring Security with role-based access (ADMIN, HR, SUPERVISOR, WORKER), method/endpoint security, and row-level constraints. Supports API key/OAuth2 toggle via configuration.  
Design Specification:  
- Roles: ADMIN, HR, SUPERVISOR, WORKER.  
- SecurityConfig: method security (`@PreAuthorize`), endpoint security.  
- Row-level security: restrict access to team data for SUPERVISOR.  
- API key/OAuth2 toggle via `application.properties`.  
- Security tests for coverage.  
Sample Implementation:  
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .antMatchers("/attendance/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
            .anyRequest().authenticated()
            .and().oauth2Login(); // or API key filter
    }
}

// Row-level security example
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == authentication.principal.department)")
public Employee getEmployee(Long id) {...}
```

---

Section: Time & Attendance (Clock In/Out)  
Description: Provides endpoints for clock-in/out events, supports geofence/device capture, calculates hours worked, and manages missed punches/corrections workflow.  
Design Specification:  
- Entity: `AttendanceEvent` (fields: id, employeeId, timestamp, type, deviceId, location, status).  
- Repository: `AttendanceRepository`.  
- Service: `AttendanceService` (clock-in/out logic, shift association, corrections).  
- Controller: `AttendanceController` (REST endpoints).  
- Approval workflow for corrections.  
- CSV export for reports.  
Sample Implementation:  
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private LocalDateTime timestamp;
    private String type; // CLOCK_IN, CLOCK_OUT
    private String deviceId;
    private String location;
    private String status; // NORMAL, CORRECTION_PENDING
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockEventDto dto) {...}
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockEventDto dto) {...}
    // Correction endpoints
}
```

---

Section: Shift & Schedule Management  
Description: Manages recurring shift templates, rotations, overtime rules, blackout dates, and assignment to employees. Detects and prevents scheduling conflicts.  
Design Specification:  
- Entity: `ShiftTemplate`, `ShiftAssignment`.  
- Repository: `ShiftTemplateRepository`, `ShiftAssignmentRepository`.  
- Service: `ShiftService` (conflict detection, bulk assignment).  
- Controller: `ShiftController` (CRUD endpoints).  
- Audit entries for changes.  
Sample Implementation:  
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrenceRule;
    private boolean isBlackout;
}

@Entity
public class ShiftAssignment {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private Long shiftTemplateId;
    private LocalDate date;
}

@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping
    public ShiftTemplate createTemplate(@RequestBody ShiftTemplateDto dto) {...}
    @PostMapping("/assign")
    public void assignShift(@RequestBody ShiftAssignmentDto dto) {...}
}
```

---

Section: Leave & Absence Management  
Description: Implements leave request/approval workflow, accrual balances, and policies. Integrates with scheduling and payroll modules to exclude approved leaves.  
Design Specification:  
- Entity: `LeaveRequest` (fields: id, employeeId, type, startDate, endDate, status, balance).  
- Repository: `LeaveRepository`.  
- Service: `LeaveService` (request, approval, balance update).  
- Controller: `LeaveController` (REST endpoints).  
- Integration hooks for scheduling/payroll.  
Sample Implementation:  
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type; // PTO, SICK, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // REQUESTED, APPROVED, DENIED
    private int balance;
}

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping
    public LeaveRequest submit(@RequestBody LeaveRequestDto dto) {...}
    @PostMapping("/approve")
    public void approve(@RequestBody ApprovalDto dto) {...}
}
```

---

Section: Training & Certification Tracking  
Description: Tracks required certifications, expirations, renewals, and blocks assignment to tasks requiring expired certifications. Supports proof document uploads and expiry alerts.  
Design Specification:  
- Entity: `Certification` (fields: id, employeeId, type, expiryDate, documentUrl, status).  
- Repository: `CertificationRepository`.  
- Service: `CertificationService` (expiry alerts, assignment checks).  
- Controller: `CertificationController` (CRUD endpoints).  
- Integration with scheduling/assignment.  
Sample Implementation:  
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type;
    private LocalDate expiryDate;
    private String documentUrl;
    private String status; // ACTIVE, EXPIRED
}

@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public Certification create(@RequestBody CertificationDto dto) {...}
    @GetMapping("/alerts")
    public List<CertificationAlertDto> getExpiryAlerts() {...}
}
```

---

Section: Safety Incidents & OSHA Reporting  
Description: Records safety incidents/near-misses, manages investigation workflow, and generates OSHA summary exports. Provides metrics dashboard endpoints.  
Design Specification:  
- Entity: `SafetyIncident` (fields: id, severity, location, description, involvedEmployeeIds, status).  
- Repository: `SafetyIncidentRepository`.  
- Service: `SafetyService` (workflow, OSHA export).  
- Controller: `SafetyController` (REST endpoints).  
- Metrics dashboard endpoints.  
Sample Implementation:  
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String severity;
    private String location;
    private String description;
    @ElementCollection
    private List<Long> involvedEmployeeIds;
    private String status; // OPEN, INVESTIGATING, RESOLVED
}

@RestController
@RequestMapping("/safety/incidents")
public class SafetyController {
    @PostMapping
    public SafetyIncident record(@RequestBody SafetyIncidentDto dto) {...}
    @GetMapping("/oshasummary")
    public ResponseEntity<Resource> exportOSHA() {...}
}
```

---

Section: Equipment & Asset Assignment  
Description: Manages asset assignment, check-in/out, certification validation, and asset condition state. Tracks history per asset and employee.  
Design Specification:  
- Entity: `Asset` (fields: id, type, condition, assignedEmployeeId, checkedOutAt, checkedInAt).  
- Repository: `AssetRepository`.  
- Service: `AssetService` (assignment, check-in/out, overdue reports).  
- Controller: `AssetController` (CRUD, check-in/out endpoints).  
- Certification validation logic.  
Sample Implementation:  
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String condition;
    private Long assignedEmployeeId;
    private LocalDateTime checkedOutAt;
    private LocalDateTime checkedInAt;
}

@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign")
    public void assign(@RequestBody AssetAssignmentDto dto) {...}
    @PostMapping("/checkin")
    public void checkIn(@RequestBody AssetCheckInDto dto) {...}
}
```

---

Section: Performance Reviews & Goals  
Description: Supports creation of review cycles, goal tracking, competencies, ratings, comments, and supervisor/employee acknowledgements. Ensures immutable history after sign-off.  
Design Specification:  
- Entity: `PerformanceReview` (fields: id, employeeId, cycle, goals, competencies, ratings, comments, status).  
- Repository: `PerformanceReviewRepository`.  
- Service: `PerformanceReviewService` (workflow, PDF export).  
- Controller: `PerformanceReviewController` (CRUD endpoints).  
- Role-based visibility.  
Sample Implementation:  
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String cycle;
    @ElementCollection
    private List<String> goals;
    @ElementCollection
    private List<String> competencies;
    @ElementCollection
    private List<Integer> ratings;
    private String comments;
    private String status; // DRAFT, SUBMITTED, SIGNED_OFF
}

@RestController
@RequestMapping("/reviews")
public class PerformanceReviewController {
    @PostMapping
    public PerformanceReview create(@RequestBody PerformanceReviewDto dto) {...}
    @GetMapping("/export/{id}")
    public ResponseEntity<Resource> exportPdf(@PathVariable Long id) {...}
}
```

---

Section: Payroll Export Integration  
Description: Generates payroll-ready files from approved attendance and leave, maps to external provider formats, and delivers securely via SFTP/API. Implements retry and audit logging.  
Design Specification:  
- Service: `PayrollExportService` (file generation, mapping, delivery, retry).  
- Integration: SFTP/API connector.  
- Audit log for exports.  
Sample Implementation:  
```java
@Service
public class PayrollExportService {
    public void generateExport() {...}
    public void deliverExport(File exportFile) {...}
    // Retry logic, audit logging
}
```

---

Section: Notifications & Announcements  
Description: Provides in-app, email, and SMS notifications for shift changes, expiring certs, approvals, and announcements. Supports channel opt-in, localization, delivery tracking, and rate limiting.  
Design Specification:  
- Entity: `NotificationPreference`, `Announcement`.  
- Service: `NotificationService` (delivery, opt-in/out, rate limiting).  
- Controller: `NotificationController`, `AnnouncementController`.  
- Localization via message bundles.  
Sample Implementation:  
```java
@Entity
public class NotificationPreference {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private boolean emailOptIn;
    private boolean smsOptIn;
    private boolean appOptIn;
}

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping("/optin")
    public void optIn(@RequestBody NotificationOptInDto dto) {...}
}

@RestController
@RequestMapping("/announcements")
public class AnnouncementController {
    @GetMapping
    public List<AnnouncementDto> getAnnouncements() {...}
}
```

---

Section: Integration Layer (HRIS/WMS APIs)  
Description: Exposes REST APIs and connectors for HRIS, WMS, and IDP for SSO. Supports webhooks for events and JWT/OAuth2 security.  
Design Specification:  
- Service: `HRISConnector`, `WMSConnector`, `IDPService`.  
- Controller: `IntegrationController` (API endpoints, webhooks).  
- Security: JWT/OAuth2.  
- OpenAPI documentation.  
Sample Implementation:  
```java
@RestController
@RequestMapping("/integration")
public class IntegrationController {
    @PostMapping("/hris/sync")
    public void syncHRIS(@RequestBody HRISSyncDto dto) {...}
    @PostMapping("/wms/link")
    public void linkWMS(@RequestBody WMSLinkDto dto) {...}
    @PostMapping("/webhook")
    public void handleWebhook(@RequestBody WebhookEventDto dto) {...}
}
```

---

Section: Audit Trail & Compliance  
Description: Centralized audit logging for sensitive changes, with tamper-evident storage. Supports export by date/user/entity and test coverage.  
Design Specification:  
- Entity: `AuditLog` (fields: id, actor, timestamp, entity, before, after, action).  
- Repository: `AuditLogRepository`.  
- Service: `AuditService` (log creation, export).  
- Controller: `AuditController` (export endpoints).  
Sample Implementation:  
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    @Lob
    private String before;
    @Lob
    private String after;
    private String action;
}

@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/export")
    public ResponseEntity<Resource> exportAudit(@RequestParam Map<String, String> filters) {...}
}
```

---

Section: Reporting & Analytics  
Description: Generates operational reports (attendance, overtime, leave balances, certification status, safety KPIs), supports CSV/PDF export, and provides role-based dashboards.  
Design Specification:  
- Service: `ReportingService` (report generation, export).  
- Controller: `ReportingController` (report endpoints).  
- Access control for reports.  
- Metrics endpoints for BI.  
Sample Implementation:  
```java
@Service
public class ReportingService {
    public Report generateAttendanceReport(DateRange range, String department) {...}
    public Report generateSafetyKPIReport(DateRange range) {...}
    // Export logic
}

@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> getAttendanceReport(@RequestParam Map<String, String> filters) {...}
    @GetMapping("/safety")
    public ResponseEntity<Resource> getSafetyReport(@RequestParam Map<String, String> filters) {...}
}
```

---

Section: Mobile Access (PWA)  
Description: Implements responsive views for clock-in/out, schedules, leave requests, and announcements. Supports offline queueing and PWA manifest.  
Design Specification:  
- Frontend: PWA manifest, service worker for offline support.  
- API: Endpoints for mobile flows.  
- Offline queue for clock events.  
- Lighthouse PWA score â¥ 80.  
Sample Implementation:  
```javascript
// manifest.json
{
  "name": "WMS Employee PWA",
  "short_name": "WMS PWA",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "icons": [...]
}

// Service worker: offline queue logic
self.addEventListener('fetch', function(event) {
  // Cache and queue clock events
});
```

---

Section: Onboarding & Offboarding Workflow  
Description: Automates provisioning of accounts, initial schedule, required training, asset assignment, and access revocation. Integrates with HRIS for new hire/termination events.  
Design Specification:  
- Service: `OnboardingService`, `OffboardingService`.  
- Workflow: Task generation, asset collection, access revocation.  
- Integration with HRIS webhooks.  
Sample Implementation:  
```java
@Service
public class OnboardingService {
    public void onboardEmployee(Long employeeId) {
        // Generate training tasks
        // Assign initial schedule
        // Provision access
    }
}

@Service
public class OffboardingService {
    public void offboardEmployee(Long employeeId) {
        // Revoke access
        // Collect assets
        // Update schedules
    }
}
```

---

Section: Localization & Multi-Tenancy  
Description: Supports multiple languages and tenant isolation. Implements locale-based message bundles and tenant-scoped data access.  
Design Specification:  
- Localization: Message bundles for supported languages.  
- Multi-tenancy: Tenant ID in security context, row-level filtering.  
- Configuration: Tenant-specific settings.  
Sample Implementation:  
```java
@Configuration
public class LocalizationConfig {
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        return messageSource;
    }
}

// Tenant filter
@Component
public class TenantFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        String tenantId = request.getHeader("X-Tenant-ID");
        TenantContext.setCurrentTenant(tenantId);
        filterChain.doFilter(request, response);
    }
}
```

---

Section: Observability & Monitoring  
Description: Implements metrics, logging, and tracing for system health monitoring. Integrates with Prometheus, Grafana, and distributed tracing tools.  
Design Specification:  
- Metrics: Micrometer for custom metrics, Actuator endpoints.  
- Logging: Structured logging with correlation IDs.  
- Tracing: Spring Cloud Sleuth for distributed tracing.  
- Alerts: Configured for error rates, latency thresholds.  
Sample Implementation:  
```java
@Configuration
public class MetricsConfig {
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("application", "wms-employee-system");
    }
}

// Custom metric example
@Service
public class AttendanceService {
    private final Counter clockInCounter;
    
    public AttendanceService(MeterRegistry registry) {
        this.clockInCounter = registry.counter("attendance.clock.in");
    }
    
    public void clockIn(Long employeeId) {
        clockInCounter.increment();
        // Clock-in logic
    }
}
```

---

Section: CI/CD Pipeline & Deployment Automation  
Description: Automates build, test, and deployment processes using CI/CD pipelines. Supports containerization, infrastructure as code, and automated rollbacks.  
Design Specification:  
- CI/CD: GitHub Actions/Jenkins for pipeline automation.  
- Containerization: Docker for application packaging.  
- Infrastructure: Terraform/CloudFormation for IaC.  
- Deployment: Blue-green or canary deployment strategies.  
- Testing: Unit, integration, and end-to-end tests in pipeline.  
Sample Implementation:  
```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

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
        run: mvn clean package
      - name: Run tests
        run: mvn test
      - name: Build Docker image
        run: docker build -t wms-employee-system:${{ github.sha }} .
      - name: Push to registry
        run: docker push wms-employee-system:${{ github.sha }}
  
  deploy:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to production
        run: kubectl set image deployment/wms-employee-system wms-employee-system=wms-employee-system:${{ github.sha }}
```

---

## Summary

This technical design document provides comprehensive low-level design specifications for all 34 user stories of the Warehouse Employee Management System. Each section includes:

- **Architectural Overview**: Spring Boot-based microservices architecture with clear separation of concerns
- **Domain Models**: JPA entities with proper relationships and constraints
- **Service Layer**: Business logic implementation with transaction management
- **Repository Layer**: Spring Data JPA repositories for data access
- **Controller Layer**: RESTful API endpoints with proper HTTP methods and status codes
- **Security**: Spring Security integration with RBAC and OAuth2/API key support
- **Integration**: External system connectors with proper error handling and retry logic
- **Observability**: Metrics, logging, and tracing for production monitoring
- **CI/CD**: Automated build, test, and deployment pipelines

The design follows Spring Boot best practices, industry standards, and ensures scalability, maintainability, and security for the warehouse employee management system.

---

**Document Version**: 1.0  
**Last Updated**: 2024  
**Status**: Ready for Implementation