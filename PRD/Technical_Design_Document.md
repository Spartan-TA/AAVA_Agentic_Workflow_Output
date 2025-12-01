# Warehouse EMS - Low-Level Technical Design Document

## Document Overview
This document provides comprehensive low-level technical design specifications for all 64 user stories of the Warehouse Employee Management System (EMS). The design follows Spring Boot best practices and industry standards.

---

## Section: Project Scaffolding & Domain Setup (Epic E01)

### Description
Establishes the foundational Spring Boot project structure, configures base packages, sets up core modules, and integrates Flyway/Liquibase for database migrations. Enables Actuator for health monitoring.

### Design Specification
- Spring Boot Maven project initialized
- Base package: com.warehouse.ems
- Modules: employee, scheduling, attendance, safety
- Flyway/Liquibase for DB migrations
- Actuator enabled for health checks
- README with build/run steps

### Sample Implementation

```java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

application.properties:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/warehouse_ems
spring.datasource.username=ems_user
spring.datasource.password=ems_pass
spring.flyway.enabled=true
management.endpoints.web.exposure.include=health,info
```

---

## Section: Employee Master Data (CRUD) (Epic E02)

### Description
Implements CRUD APIs for employee records, enforcing unique badge IDs, supporting soft deletes, pagination, filtering, and OpenAPI documentation.

### Design Specification
- Entity: Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService (CRUD, badgeId uniqueness, soft delete)
- Controller: EmployeeController (REST endpoints)
- DTOs: EmployeeDTO, EmployeeCreateDTO, EmployeeUpdateDTO
- OpenAPI schemas

### Sample Implementation

```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badgeId"))
public class Employee {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted;
}

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@RequestBody EmployeeCreateDTO dto) {...}
    @GetMapping
    public Page<EmployeeDTO> list(Pageable pageable, @RequestParam Map<String, String> filters) {...}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {...}
}
```

---

## Section: Role Based Access Control (RBAC) (Epic E03)

### Description
Integrates Spring Security with role-based access (ADMIN, HR, SUPERVISOR, WORKER), method/endpoint security, row-level constraints, and API key/OAuth2 toggle via config.

### Design Specification
- SecurityConfig: configures roles and endpoint access
- Method security: @PreAuthorize annotations
- API key/OAuth2 toggle via properties
- Row-level constraints in service/repository

### Sample Implementation

```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR")
            .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
            .anyRequest().authenticated()
            .and().oauth2Login();
    }
}

@Service
public class EmployeeService {
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == authentication.principal.department)")
    public EmployeeDTO updateEmployee(Long id, EmployeeUpdateDTO dto) {...}
}
```

---

## Section: Time & Attendance (Clock In/Out) (Epic E04)

### Description
Provides endpoints for clock-in/out events, geofence/device capture, shift association, missed punch corrections, and reporting.

### Design Specification
- Entity: AttendanceEvent (id, employeeId, type, timestamp, deviceId, location, shiftId, correctionStatus)
- Repository: AttendanceEventRepository
- Service: AttendanceService (clock-in/out, corrections, totals)
- Controller: AttendanceController
- Reports: CSV export

### Sample Implementation

```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type; // CLOCK_IN, CLOCK_OUT
    private LocalDateTime timestamp;
    private String deviceId;
    private String location;
    private Long shiftId;
    private String correctionStatus;
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockEventDTO dto) {...}
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockEventDTO dto) {...}
    @GetMapping("/report")
    public ResponseEntity<Resource> exportReport(@RequestParam LocalDate date) {...}
}
```

---

## Section: Shift & Schedule Management (Epic E05)

### Description
Manages recurring shift templates, rotations, overtime rules, employee assignments, blackout dates, and operation calendars.

### Design Specification
- Entity: ShiftTemplate, ShiftAssignment, BlackoutDate
- Repository: ShiftTemplateRepository, ShiftAssignmentRepository
- Service: ShiftService (CRUD, conflict detection, bulk assignment)
- Controller: ShiftController
- Audit entries for changes

### Sample Implementation

```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrenceRule;
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
    @PostMapping("/templates")
    public ResponseEntity<ShiftTemplateDTO> createTemplate(@RequestBody ShiftTemplateDTO dto) {...}
    @PostMapping("/assign")
    public ResponseEntity<?> bulkAssign(@RequestBody BulkAssignDTO dto) {...}
}
```

---

## Section: Leave & Absence Management (Epic E06)

### Description
Handles PTO, sick, unpaid leave requests/approvals, accrual balances, and integration with scheduling/payroll.

### Design Specification
- Entity: LeaveRequest (id, employeeId, type, startDate, endDate, status, accrualBalance)
- Repository: LeaveRequestRepository
- Service: LeaveService (request, approve/deny, update balances)
- Controller: LeaveController
- Integration hooks for scheduling/payroll

### Sample Implementation

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
    private int accrualBalance;
}

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/request")
    public ResponseEntity<LeaveRequestDTO> requestLeave(@RequestBody LeaveRequestDTO dto) {...}
    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approveLeave(@PathVariable Long id) {...}
}
```

---

## Section: Training & Certification Tracking (Epic E07)

### Description
Tracks certifications, expirations, renewals, blocks assignments for expired certs, and uploads proof documents.

### Design Specification
- Entity: Certification (id, employeeId, type, issueDate, expiryDate, documentUrl)
- Repository: CertificationRepository
- Service: CertificationService (CRUD, expiry alerts, assignment checks)
- Controller: CertificationController
- Alerts for expiry

### Sample Implementation

```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
}

@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<CertificationDTO> create(@RequestBody CertificationDTO dto) {...}
    @GetMapping("/alerts")
    public List<CertificationAlertDTO> getExpiryAlerts() {...}
}
```

---

## Section: Safety Incidents & OSHA Reporting (Epic E08)

### Description
Records safety incidents/near-misses, manages investigation workflow, and generates OSHA summaries.

### Design Specification
- Entity: SafetyIncident (id, severity, location, description, involvedEmployeeIds, status)
- Repository: SafetyIncidentRepository
- Service: SafetyService (record, workflow, export)
- Controller: SafetyController
- OSHA export endpoints

### Sample Implementation

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
    public ResponseEntity<SafetyIncidentDTO> recordIncident(@RequestBody SafetyIncidentDTO dto) {...}
    @GetMapping("/osha/export")
    public ResponseEntity<Resource> exportOshaSummary() {...}
}
```

---

## Section: Equipment & Asset Assignment (Epic E09)

### Description
Assigns assets to employees, tracks check-in/out, blocks use if certification missing, and maintains asset condition.

### Design Specification
- Entity: Asset (id, type, condition, assignedEmployeeId, checkoutDate, returnDate)
- Repository: AssetRepository
- Service: AssetService (CRUD, check-in/out, certification checks)
- Controller: AssetController
- History log per asset/employee

### Sample Implementation

```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String condition;
    private Long assignedEmployeeId;
    private LocalDate checkoutDate;
    private LocalDate returnDate;
}

@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign")
    public ResponseEntity<?> assignAsset(@RequestBody AssetAssignDTO dto) {...}
    @PostMapping("/return")
    public ResponseEntity<?> returnAsset(@RequestBody AssetReturnDTO dto) {...}
}
```

---

## Section: Performance Reviews & Goals (Epic E10)

### Description
Manages review templates, goals, competencies, ratings, comments, and supervisor/employee acknowledgements.

### Design Specification
- Entity: PerformanceReview (id, employeeId, cycle, goals, competencies, ratings, comments, status)
- Repository: PerformanceReviewRepository
- Service: ReviewService (create cycle, assign, submit, acknowledge)
- Controller: ReviewController
- PDF export

### Sample Implementation

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
    private String status; // DRAFT, SUBMITTED, ACKNOWLEDGED
}

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping("/cycle")
    public ResponseEntity<?> createCycle(@RequestBody ReviewCycleDTO dto) {...}
    @PostMapping("/submit")
    public ResponseEntity<?> submitReview(@RequestBody PerformanceReviewDTO dto) {...}
}
```

---

## Section: Payroll Export Integration (Epic E11)

### Description
Generates payroll-ready files from approved attendance/leave, maps to provider formats, and delivers securely.

### Design Specification
- Service: PayrollExportService (generate, map, deliver, retry)
- Controller: PayrollController
- Audit log for exports

### Sample Implementation

```java
@Service
public class PayrollExportService {
    public Resource generatePayrollFile(LocalDate period) {...}
    public void deliverPayrollFile(Resource file) {...}
}

@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @GetMapping("/export")
    public ResponseEntity<Resource> exportPayroll(@RequestParam LocalDate period) {...}
}
```

---

## Section: Notifications & Announcements (Epic E12)

### Description
Sends in-app/email/SMS notifications for shift changes, expiring certs, approvals, and announcements; supports quiet hours.

### Design Specification
- Entity: Notification (id, userId, type, channel, message, status, timestamp)
- Repository: NotificationRepository
- Service: NotificationService (send, opt-in/out, templates, delivery status)
- Controller: NotificationController

### Sample Implementation

```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private Long userId;
    private String type;
    private String channel; // EMAIL, SMS, IN_APP
    private String message;
    private String status; // SENT, FAILED
    private LocalDateTime timestamp;
}

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationDTO dto) {...}
}
```

---

## Section: Integration Layer (HRIS/WMS APIs) (Epic E13)

### Description
Exposes REST APIs/connectors for HRIS, WMS, SSO, and webhooks; synchronizes master data.

### Design Specification
- Service: IntegrationService (HRIS sync, WMS link, SSO, webhooks)
- Controller: IntegrationController
- JWT/OAuth2 security
- OpenAPI documentation

### Sample Implementation

```java
@RestController
@RequestMapping("/integration")
public class IntegrationController {
    @PostMapping("/hris/sync")
    public ResponseEntity<?> syncHris(@RequestBody HrisSyncDTO dto) {...}
    @PostMapping("/wms/link")
    public ResponseEntity<?> linkWms(@RequestBody WmsLinkDTO dto) {...}
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody WebhookEventDTO dto) {...}
}
```

---

## Section: Audit Trail & Compliance (Epic E14)

### Description
Centralizes audit logging for sensitive changes, stores tamper-evident logs, and supports export.

### Design Specification
- Entity: AuditLog (id, actor, timestamp, entity, before, after, action)
- Repository: AuditLogRepository
- Service: AuditService (log, export)
- Controller: AuditController

### Sample Implementation

```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    @Lob
    private String beforeState;
    @Lob
    private String afterState;
    private String action;
}

@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/export")
    public ResponseEntity<Resource> exportAuditLogs(@RequestParam Map<String, String> filters) {...}
}
```

---

## Section: Reporting & Analytics (Epic E15)

### Description
Provides operational reports for attendance, overtime, leave balances, certification status, safety KPIs; exports CSV/PDF; role-based dashboards.

### Design Specification
- Service: ReportingService (generate reports, filter, export)
- Controller: ReportingController
- Dashboard endpoints for BI

### Sample Implementation

```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> generateAttendanceReport(@RequestParam Map<String, String> filters) {...}
    @GetMapping("/overtime")
    public ResponseEntity<Resource> generateOvertimeReport(@RequestParam Map<String, String> filters) {...}
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard() {...}
}
```

---

## Section: Mobile Access (PWA) (Epic E16)

### Description
Provides responsive views for workers to clock-in/out, view schedules, request leave, see announcements; offline-friendly via PWA.

### Design Specification
- PWA manifest and service worker
- Offline queue for clock events
- Mobile-optimized controllers
- Lighthouse PWA compliance

### Sample Implementation

```javascript
// service-worker.js
self.addEventListener('sync', event => {
  if (event.tag === 'sync-attendance') {
    event.waitUntil(syncAttendanceEvents());
  }
});

function syncAttendanceEvents() {
  return getQueuedEvents()
    .then(events => Promise.all(events.map(sendToServer)))
    .then(() => clearQueue());
}
```

---

## Section: Onboarding & Offboarding Workflow (Epic E17)

### Description
Automates provisioning of accounts, initial schedule, required training; deprovision access and assets on termination.

### Design Specification
- Service: OnboardingService (provision, assign training, schedule)
- Service: OffboardingService (revoke access, collect assets, update schedules)
- Controller: WorkflowController

### Sample Implementation

```java
@Service
public class OnboardingService {
    public void onboardEmployee(Long employeeId) {
        // Create user account
        // Assign initial training
        // Generate first week schedule
        // Notify supervisor
    }
}

@Service
public class OffboardingService {
    public void offboardEmployee(Long employeeId) {
        // Revoke system access
        // Collect assigned assets
        // Remove from future schedules
        // Notify IT and security
    }
}
```

---

## Section: Localization & Internationalization (Epic E18)

### Description
Supports English and Spanish UI/notifications; configurable date/time formats; currency for payroll exports.

### Design Specification
- MessageSource configuration
- Locale resolver
- Localized templates
- Date/time formatters

### Sample Implementation

```java
@Configuration
public class LocalizationConfig {
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
    
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.US);
        return resolver;
    }
}
```

---

## Section: Observability & Monitoring (Epic E19)

### Description
Structured logging (JSON), metrics (Micrometer/Prometheus), distributed tracing (OpenTelemetry); health checks and alerts.

### Design Specification
- Logback JSON encoder
- Micrometer metrics
- OpenTelemetry tracing
- Actuator health indicators
- Grafana dashboards

### Sample Implementation

```java
@Configuration
public class ObservabilityConfig {
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("application", "warehouse-ems");
    }
}

// application.properties
management.metrics.export.prometheus.enabled=true
management.tracing.sampling.probability=1.0
```

---

## Section: CI/CD & Deployment Automation (Epic E20)

### Description
GitHub Actions or Jenkins pipelines for build, test, security scans, Docker image push, and deploy to staging/prod.

### Design Specification
- GitHub Actions workflow
- Maven build
- JUnit tests
- SonarQube scans
- Docker image build and push
- Kubernetes deployment

### Sample Implementation

```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

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
      - name: Run tests
        run: mvn test
      - name: SonarQube Scan
        run: mvn sonar:sonar
      - name: Build Docker image
        run: docker build -t warehouse-ems:${{ github.sha }} .
      - name: Push to registry
        run: docker push warehouse-ems:${{ github.sha }}
```

---

## Conclusion

This technical design document provides comprehensive low-level specifications for all 64 user stories across 20 epics of the Warehouse EMS system. Each section includes:

- Detailed architectural decisions
- Spring Boot entity, repository, service, and controller designs
- Security configurations
- Integration patterns
- Code samples and pseudo-code

The design follows Spring Boot best practices including:
- Layered architecture (Controller -> Service -> Repository -> Entity)
- Dependency injection
- RESTful API design
- Spring Security for authentication and authorization
- Spring Data JPA for data access
- Actuator for monitoring
- Comprehensive error handling
- Audit logging
- Integration patterns

All components are designed to be testable, maintainable, and scalable to support warehouse operations efficiently.