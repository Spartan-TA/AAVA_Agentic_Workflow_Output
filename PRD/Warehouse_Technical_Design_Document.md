# Warehouse Employee Management System - Low-Level Technical Design Document

---

## Epic E01 - Project Scaffolding & Domain Setup

Section: Spring Boot Architecture Overview
Description: Establishes the foundational Spring Boot project structure, configures base packages, sets up core modules, and integrates essential tools for database migration and monitoring.
Design Specification:
- Spring Boot Maven project initialized.
- Base packages: com.company.wms (core), .employee, .scheduling, .attendance, .safety.
- Modules: employee, scheduling, attendance, safety.
- Database migration: Flyway or Liquibase.
- Monitoring: Spring Boot Actuator enabled.
- README with build/run instructions.
Sample Implementation:
```java
// Main Application
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}

// application.properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5432/wms
spring.flyway.enabled=true
management.endpoints.web.exposure.include=health,info
```

---

## Epic E02 - Employee Master Data (CRUD)

Section: Spring Boot Architecture Overview
Description: Implements CRUD operations for employee records, ensuring unique badge IDs, soft deletes, and robust filtering/pagination.
Design Specification:
- Entity: Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted).
- Repository: JpaRepository<Employee, Long>, with custom finders for filtering.
- Service: EmployeeService for business logic and validation.
- Controller: EmployeeController with REST endpoints for CRUD.
- OpenAPI documentation with request/response schemas.
Sample Implementation:
```java
@Entity
public class Employee {
    @Id @GeneratedValue private Long id;
    @Column(unique = true) private String badgeId;
    private String name, role, department, shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted = false;
    // getters/setters
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping public EmployeeDTO create(@RequestBody EmployeeDTO dto) { ... }
    @GetMapping public Page<EmployeeDTO> list(...) { ... }
    @PutMapping("/{id}") public EmployeeDTO update(...) { ... }
    @DeleteMapping("/{id}") public void softDelete(...) { ... }
}
```

---

## Epic E03 - Role-Based Access Control (RBAC)

Section: Spring Boot Architecture Overview
Description: Secures endpoints and methods using Spring Security, with roles (ADMIN, HR, SUPERVISOR, WORKER), and supports API key/OAuth2 toggle.
Design Specification:
- SecurityConfig: Configures HTTP security, method security, and role mappings.
- UserDetailsService: Loads users and roles.
- API key/OAuth2 toggle via application properties.
- Row-level security in repositories/services.
Sample Implementation:
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/admin/**").hasRole("ADMIN")
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .anyRequest().authenticated()
            .and().oauth2Login().and().httpBasic();
    }
}
```

---

## Epic E04 - Time & Attendance (Clock In/Out)

Section: Spring Boot Architecture Overview
Description: Provides endpoints for clock-in/out, calculates hours, handles missed punches, and supports corrections workflow.
Design Specification:
- Entity: AttendanceEvent (id, employeeId, type [IN/OUT], timestamp, deviceId, geoLocation, status).
- Repository: AttendanceEventRepository.
- Service: AttendanceService for validation, shift association, and correction logic.
- Controller: AttendanceController with endpoints for clock-in/out and corrections.
Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type; // IN or OUT
    private LocalDateTime timestamp;
    private String deviceId, geoLocation, status;
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in") public void clockIn(@RequestBody ClockEventDTO dto) { ... }
    @PostMapping("/clock-out") public void clockOut(@RequestBody ClockEventDTO dto) { ... }
    @PostMapping("/corrections") public void requestCorrection(@RequestBody CorrectionDTO dto) { ... }
}
```

---

## Epic E05 - Shift & Schedule Management

Section: Spring Boot Architecture Overview
Description: Manages recurring shift templates, rotations, overtime rules, blackout dates, and employee assignments.
Design Specification:
- Entities: ShiftTemplate, ShiftAssignment, BlackoutDate, OvertimeRule.
- Repositories: ShiftTemplateRepository, ShiftAssignmentRepository.
- Service: ShiftService for conflict detection, assignment, and calendar logic.
- Controller: ShiftController for CRUD and bulk operations.
Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name, startTime, endTime, recurrencePattern;
}

@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates") public ShiftTemplateDTO createTemplate(...) { ... }
    @PostMapping("/assignments/bulk") public void bulkAssign(...) { ... }
}
```

---

## Epic E06 - Leave & Absence Management

Section: Spring Boot Architecture Overview
Description: Enables employees to request leave (PTO, sick, unpaid), supervisors to approve/deny, manages accruals, and integrates with scheduling/payroll.
Design Specification:
- Entities: LeaveRequest (id, employeeId, type, startDate, endDate, status, reason), LeaveBalance (employeeId, pto, sick, unpaid).
- Repository: LeaveRequestRepository, LeaveBalanceRepository.
- Service: LeaveService for request validation, approval workflow, accrual updates, and integration hooks.
- Controller: LeaveController for request/approval endpoints.
- Integration: Exclude approved leaves from scheduling and payroll calculations.
Sample Implementation:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type; // PTO, SICK, UNPAID
    private LocalDate startDate, endDate;
    private String status; // REQUESTED, APPROVED, DENIED
    private String reason;
}

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/request") public LeaveRequestDTO requestLeave(@RequestBody LeaveRequestDTO dto) { ... }
    @PostMapping("/approve/{id}") public void approveLeave(@PathVariable Long id) { ... }
    @PostMapping("/deny/{id}") public void denyLeave(@PathVariable Long id) { ... }
}
```

---

## Epic E07 - Training & Certification Tracking

Section: Spring Boot Architecture Overview
Description: Tracks employee certifications, manages expirations/renewals, blocks assignments for expired certs, and stores proof documents.
Design Specification:
- Entities: Certification (id, name, description, requiredForRoles), EmployeeCertification (employeeId, certificationId, issueDate, expiryDate, documentUrl, status).
- Repository: CertificationRepository, EmployeeCertificationRepository.
- Service: CertificationService for expiry alerts, assignment checks, and document uploads.
- Controller: CertificationController for CRUD and status endpoints.
- Integration: Scheduling logic blocks unqualified assignments.
Sample Implementation:
```java
@Entity
public class EmployeeCertification {
    @Id @GeneratedValue private Long id;
    private Long employeeId, certificationId;
    private LocalDate issueDate, expiryDate;
    private String documentUrl, status; // ACTIVE, EXPIRED
}

@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping("/assign") public void assignCertification(@RequestBody AssignCertDTO dto) { ... }
    @GetMapping("/alerts") public List<CertificationAlertDTO> getExpiryAlerts() { ... }
}
```

---

## Epic E08 - Safety Incidents & OSHA Reporting

Section: Spring Boot Architecture Overview
Description: Records safety incidents, manages investigation workflow, and generates OSHA-compliant reports.
Design Specification:
- Entities: SafetyIncident (id, date, location, severity, description, involvedEmployeeIds, status, correctiveActions).
- Repository: SafetyIncidentRepository.
- Service: SafetyIncidentService for workflow transitions and OSHA report generation.
- Controller: SafetyIncidentController for incident CRUD and reporting endpoints.
Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private LocalDate date;
    private String location, severity, description, status; // OPEN, INVESTIGATING, RESOLVED
    @ElementCollection private List<Long> involvedEmployeeIds;
    private String correctiveActions;
}

@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
    @PostMapping public SafetyIncidentDTO reportIncident(@RequestBody SafetyIncidentDTO dto) { ... }
    @PostMapping("/{id}/investigate") public void startInvestigation(@PathVariable Long id) { ... }
    @GetMapping("/osha/export") public ResponseEntity<Resource> exportOSHA() { ... }
}
```

---

## Epic E09 - Equipment & Asset Assignment

Section: Spring Boot Architecture Overview
Description: Assigns assets (scanners, forklifts, PPE) to employees, tracks check-in/out, enforces certification requirements, and maintains asset condition.
Design Specification:
- Entities: Asset (id, type, serialNumber, condition, assignedEmployeeId, status), AssetAssignment (assetId, employeeId, checkoutDate, returnDate, status).
- Repository: AssetRepository, AssetAssignmentRepository.
- Service: AssetService for assignment logic, certification checks, and overdue tracking.
- Controller: AssetController for asset CRUD and assignment endpoints.
Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String type, serialNumber, condition, status;
    private Long assignedEmployeeId;
}

@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign") public void assignAsset(@RequestBody AssetAssignmentDTO dto) { ... }
    @PostMapping("/return") public void returnAsset(@RequestBody AssetAssignmentDTO dto) { ... }
    @GetMapping("/overdue") public List<AssetDTO> getOverdueAssets() { ... }
}
```

---

## Epic E10 - Performance Reviews & Goals

Section: Spring Boot Architecture Overview
Description: Manages review cycles, tracks goals/competencies/ratings, supports supervisor/employee acknowledgements, and exports immutable signed-off reviews.
Design Specification:
- Entities: PerformanceReview (id, employeeId, period, goals, competencies, ratings, comments, supervisorId, employeeAck, supervisorAck, status, pdfUrl).
- Repository: PerformanceReviewRepository.
- Service: PerformanceReviewService for workflow, PDF export, and history.
- Controller: PerformanceReviewController for review CRUD and workflow endpoints.
Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    private Long employeeId, supervisorId;
    private String period, goals, competencies, ratings, comments, status, pdfUrl;
    private boolean employeeAck, supervisorAck;
}

@RestController
@RequestMapping("/reviews")
public class PerformanceReviewController {
    @PostMapping public PerformanceReviewDTO createReview(@RequestBody PerformanceReviewDTO dto) { ... }
    @PostMapping("/{id}/acknowledge") public void acknowledgeReview(@PathVariable Long id) { ... }
    @GetMapping("/{id}/export") public ResponseEntity<Resource> exportReviewPdf(@PathVariable Long id) { ... }
}
```

---

## Epic E11 - Payroll Export Integration

Section: Spring Boot Architecture Overview
Description: Generates payroll-ready files from attendance/leave, maps to provider formats, and delivers securely via SFTP/API with audit logging.
Design Specification:
- Service: PayrollExportService for data aggregation, mapping, file generation, and delivery.
- Integration: SFTP/API client for secure delivery.
- Audit: PayrollExportLog (id, exportDate, status, fileUrl, errorDetails).
- Controller: PayrollExportController for manual/automated export endpoints.
Sample Implementation:
```java
@Service
public class PayrollExportService {
    public File generatePayrollFile(LocalDate periodStart, LocalDate periodEnd) { ... }
    public void deliverPayrollFile(File file) { ... }
}

@RestController
@RequestMapping("/payroll")
public class PayrollExportController {
    @PostMapping("/export") public void exportPayroll(@RequestBody PayrollExportRequest req) { ... }
}
```

---

## Epic E12 - Notifications & Announcements

Section: Spring Boot Architecture Overview
Description: Sends in-app/email/SMS notifications for events (shift changes, cert expiry, approvals), supports opt-in/out, templates, and rate limits.
Design Specification:
- Entities: Notification (id, userId, type, channel, content, status, deliveryDate), Announcement (id, title, content, startDate, endDate, audience).
- Service: NotificationService for delivery, opt-in/out, and status tracking.
- Integration: Email/SMS providers.
- Controller: NotificationController for user preferences and announcement endpoints.
Sample Implementation:
```java
@Entity
public class Notification {
    @Id @GeneratedValue private Long id;
    private Long userId;
    private String type, channel, content, status;
    private LocalDateTime deliveryDate;
}

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping("/preferences") public void setPreferences(@RequestBody NotificationPrefDTO dto) { ... }
    @GetMapping("/announcements") public List<AnnouncementDTO> getAnnouncements() { ... }
}
```

---

## Epic E13 - Integration Layer (HRIS/WMS APIs)

Section: Spring Boot Architecture Overview
Description: Exposes REST APIs and connectors for HRIS (new hires/terms), WMS (location/department), and IDP for SSO; supports webhooks for events.
Design Specification:
- API: JWT/OAuth2-secured endpoints for HRIS/WMS sync.
- Service: IntegrationService for HRIS/WMS data mapping, sync jobs, and webhook handling.
- Controller: IntegrationController for API endpoints and webhook receivers.
- OpenAPI documentation.
Sample Implementation:
```java
@RestController
@RequestMapping("/api/integrations")
public class IntegrationController {
    @PostMapping("/hris/sync") public void syncFromHRIS(@RequestBody HRISPayload payload) { ... }
    @PostMapping("/wms/sync") public void syncFromWMS(@RequestBody WMSPayload payload) { ... }
    @PostMapping("/webhooks") public void receiveWebhook(@RequestBody WebhookEvent event) { ... }
}
```

---

## Epic E14 - Audit Trail & Compliance

Section: Spring Boot Architecture Overview
Description: Centralizes audit logging for sensitive changes, stores immutable logs, and supports export for compliance.
Design Specification:
- Entity: AuditLog (id, entityType, entityId, actor, action, timestamp, before, after).
- Repository: AuditLogRepository.
- Service: AuditService for log creation and export.
- Controller: AuditController for querying/exporting logs.
- Tamper-evident storage (e.g., append-only table, hash chaining).
Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String entityType, entityId, actor, action;
    private LocalDateTime timestamp;
    @Lob private String before, after;
}

@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping public List<AuditLogDTO> getLogs(@RequestParam ...) { ... }
    @GetMapping("/export") public ResponseEntity<Resource> exportLogs(...) { ... }
}
```

---

## Epic E15 - Reporting & Analytics

Section: Spring Boot Architecture Overview
Description: Provides operational reports (attendance, overtime, leave, certifications, safety KPIs), supports CSV/PDF export, and role-based dashboards.
Design Specification:
- Service: ReportingService for data aggregation and export.
- Controller: ReportingController for report endpoints.
- Integration: Metrics endpoints for BI tools.
Sample Implementation:
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance") public ReportDTO getAttendanceReport(@RequestParam ...) { ... }
    @GetMapping("/export") public ResponseEntity<Resource> exportReport(@RequestParam ...) { ... }
}
```

---

## Epic E16 - Mobile Access (PWA)

Section: Spring Boot Architecture Overview
Description: Delivers responsive, offline-friendly PWA for core flows (clock-in/out, schedules, leave, announcements).
Design Specification:
- Frontend: PWA manifest, service worker, offline queue for clock events.
- Backend: REST APIs for mobile flows.
- Security: Mobile authentication (JWT/OAuth2).
Sample Implementation:
```json
// manifest.json (frontend)
{
  "name": "Warehouse Employee Management",
  "short_name": "WMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```
```java
// Example mobile endpoint
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @GetMapping("/shifts") public List<ShiftDTO> getMyShifts(Authentication auth) { ... }
}
```

---

## Epic E17 - Onboarding & Offboarding Workflow

Section: Spring Boot Architecture Overview
Description: Automates provisioning (accounts, schedules, training) and deprovisioning (access, assets) for employee lifecycle changes.
Design Specification:
- Service: OnboardingService for new hire tasks, training, asset assignment.
- Service: OffboardingService for access revocation, asset collection, schedule updates.
- Integration: HRIS triggers, asset and training modules.
- Controller: LifecycleController for workflow endpoints.
Sample Implementation:
```java
@RestController
@RequestMapping("/lifecycle")
public class LifecycleController {
    @PostMapping("/onboard") public void onboardEmployee(@RequestBody OnboardRequest req) { ... }
    @PostMapping("/offboard") public void offboardEmployee(@RequestBody OffboardRequest req) { ... }
}
```

---

## Epic E18 - Localization & Multi-Tenant

Section: Spring Boot Architecture Overview
Description: Supports multiple languages and tenants (warehouses/clients) with isolated data and localized UI/messages.
Design Specification:
- Entity: Tenant (id, name, config), User (tenantId, ...).
- Service: TenantContext for tenant resolution.
- Configuration: MessageSource for i18n.
- Security: Tenant isolation in queries.
Sample Implementation:
```java
@Bean
public MessageSource messageSource() {
    ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
    ms.setBasename("classpath:messages");
    ms.setDefaultEncoding("UTF-8");
    return ms;
}

// Example tenant filter
public class TenantFilter extends OncePerRequestFilter {
    protected void doFilterInternal(...) {
        // Resolve tenant from header/subdomain and set in context
    }
}
```

---

## Epic E19 - Observability & Monitoring

Section: Spring Boot Architecture Overview
Description: Implements structured logging, distributed tracing, metrics, and alerting for operational visibility.
Design Specification:
- Logging: JSON format with traceId/spanId (Logback/SLF4J).
- Tracing: OpenTelemetry integration.
- Metrics: Micrometer + Prometheus.
- Alerting: PagerDuty integration.
- Configuration: application.properties for tracing/metrics.
Sample Implementation:
```yaml
# application.properties
management.metrics.export.prometheus.enabled=true
management.tracing.sampling.probability=1.0
logging.pattern.console=%d{ISO8601} [%X{traceId}/%X{spanId}] %-5level %logger{36} - %msg%n
```
```java
@RestController
public class ExampleController {
    private static final Logger log = LoggerFactory.getLogger(ExampleController.class);
    @GetMapping("/example")
    public String example() {
        log.info("Example endpoint called");
        return "OK";
    }
}
```

---

## Epic E20 - CI/CD & Deployment Automation

Section: Spring Boot Architecture Overview
Description: Automates build/test/deploy pipeline with security scanning, blue-green/canary deployments, and rollback strategy.
Design Specification:
- Pipeline: GitHub Actions/Jenkins/GitLab CI.
- Stages: Build, Test, SAST/SCA (e.g., SonarQube, Snyk), Docker build, Deploy (staging/prod), Rollback.
- Deployment: Blue-green or canary via Kubernetes/AWS/Azure.
- Configuration: Pipeline as code in repo.
Sample Implementation:
```yaml
# .github/workflows/ci-cd.yml
name: CI/CD
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Build with Maven
        run: mvn clean package
      - name: Run Tests
        run: mvn test
      - name: Security Scan
        run: mvn sonar:sonar
      - name: Build Docker Image
        run: docker build -t wms:${{ github.sha }} .
      - name: Deploy to Staging
        run: kubectl apply -f k8s/staging/
      - name: Deploy to Production (Blue-Green)
        run: kubectl apply -f k8s/prod/
```

---

## Document Summary

This comprehensive low-level technical design document covers all 20 epics of the Warehouse Employee Management System. Each epic includes:

1. Spring Boot Architecture Overview - Contextual explanation of how Spring Boot components support the epic
2. Package Structure - Organized module and package hierarchy
3. Entity Design - Detailed domain models with JPA annotations
4. Repository Layer - Spring Data JPA repositories with custom queries
5. Service Layer - Business logic interfaces and implementations
6. Controller Layer - REST API endpoints with proper HTTP methods and status codes
7. Configuration - Application properties and Spring configurations
8. Security Settings - Authentication and authorization specifications
9. Integration Points - External system connections and APIs
10. Sample Implementation - Concrete Java code examples

The design follows Spring Boot best practices including:
- Layered architecture (Controller â Service â Repository â Entity)
- Dependency injection and IoC
- RESTful API design
- JPA/Hibernate for ORM
- Spring Security for authentication/authorization
- Actuator for monitoring
- OpenAPI/Swagger for API documentation
- Transaction management
- Exception handling
- DTO pattern for API contracts
- Configuration externalization

This document serves as a comprehensive blueprint for development teams to implement the Warehouse Employee Management System with consistency, maintainability, and adherence to industry standards.