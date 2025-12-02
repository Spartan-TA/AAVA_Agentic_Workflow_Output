# Warehouse Employee Management System (EMS)
## Low-Level Technical Design Document

### Document Overview
This document provides comprehensive low-level technical design specifications for all 101 user stories across 20 epics of the Warehouse Employee Management System. The design follows Spring Boot best practices and industry standards.

---

## Section: E01 - Project Scaffolding & Domain Setup

### Description
Establishes the foundational Spring Boot project structure, configures base packages, sets up core modules, integrates database migration tools, and enables health monitoring.

### Design Specification
- Spring Boot Maven project initialized with standard directory structure (src/main/java, src/main/resources)
- Base packages: com.warehouse.ems, with sub-packages for employee, scheduling, attendance, safety, etc.
- Modules: employee, scheduling, attendance, safety (as Java packages)
- Flyway/Liquibase configured for DB migrations (src/main/resources/db/migration)
- Spring Boot Actuator enabled for health checks

### Sample Implementation
```java
// src/main/java/com/warehouse/ems/WarehouseEmsApplication.java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}

// application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ems
spring.datasource.username=ems_user
spring.datasource.password=secret
spring.flyway.enabled=true
management.endpoints.web.exposure.include=health,info
```

---

## Section: E02 - Employee Master Data (CRUD)

### Description
Implements CRUD APIs for employee records, enforces unique badge IDs, supports soft deletion, pagination, filtering, and OpenAPI documentation.

### Design Specification
- Entity: Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService with CRUD methods, badgeId uniqueness check, soft delete logic
- Controller: EmployeeController exposes REST endpoints (POST/GET/PUT/PATCH/DELETE)
- DTOs: EmployeeDto, EmployeeCreateDto, EmployeeUpdateDto
- Pagination and filtering via Pageable and Specification
- OpenAPI schemas with examples

### Sample Implementation
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue private Long id;
    private String name;
    @Column(name = "badge_id", unique = true) private String badgeId;
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
    @PostMapping public ResponseEntity<EmployeeDto> create(@RequestBody EmployeeCreateDto dto) { ... }
    @GetMapping public Page<EmployeeDto> list(Pageable pageable, @RequestParam Map<String, String> filters) { ... }
    @DeleteMapping("/{id}") public ResponseEntity<Void> softDelete(@PathVariable Long id) { ... }
}
```

---

## Section: E03 - Role Based Access Control (RBAC)

### Description
Integrates Spring Security with role-based access, method and row-level security, API key/OAuth2 toggle, and security test coverage.

### Design Specification
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- SecurityConfig: configures HttpSecurity, role mappings, method security (@PreAuthorize)
- Row-level security via custom repository queries
- API key/OAuth2 toggle via application properties
- Unauthorized (401) and forbidden (403) responses
- Security tests using @WithMockUser

### Sample Implementation
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
            .anyRequest().authenticated()
            .and().oauth2Login().and().httpBasic();
    }
}

@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == authentication.principal.department)")
public Employee getEmployee(Long id) { ... }
```

---

## Section: E04 - Time & Attendance (Clock In/Out)

### Description
Provides endpoints for clock-in/out events, geofence/device capture, hours calculation, missed punch correction workflow, and report export.

### Design Specification
- Entity: AttendanceEvent (id, employeeId, type, timestamp, location, deviceId, shiftId, status)
- Controller: AttendanceController (POST /attendance/clock-in, /clock-out)
- Service: AttendanceService (validate event, associate shift, compute totals, handle corrections)
- Geofence/device capture via request payload
- Correction workflow: creates approval tasks
- Export reports as CSV

### Sample Implementation
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type; // CLOCK_IN, CLOCK_OUT
    private LocalDateTime timestamp;
    private String location;
    private String deviceId;
    private Long shiftId;
    private String status; // NORMAL, CORRECTION_PENDING
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in") public ResponseEntity<?> clockIn(@RequestBody ClockEventDto dto) { ... }
    @PostMapping("/clock-out") public ResponseEntity<?> clockOut(@RequestBody ClockEventDto dto) { ... }
}
```

---

## Section: E05 - Shift & Schedule Management

### Description
Manages shift templates, rotations, overtime rules, employee assignments, blackout dates, and operation calendars.

### Design Specification
- Entity: ShiftTemplate, ShiftAssignment, BlackoutDate, OperationCalendar
- Controller: ShiftController (CRUD for templates, assignments)
- Service: ShiftService (conflict detection, bulk assignment, audit logging)
- Workers view personal shifts; supervisors bulk-assign
- Audit entries for assignments

### Sample Implementation
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrenceRule;
}

@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates") public ResponseEntity<ShiftTemplateDto> createTemplate(@RequestBody ShiftTemplateDto dto) { ... }
    @PostMapping("/assign") public ResponseEntity<?> assignShifts(@RequestBody ShiftAssignmentDto dto) { ... }
}
```

---

## Section: E06 - Leave & Absence Management

### Description
Handles leave requests, approvals, accrual balances, policy enforcement, and integration with scheduling/payroll.

### Design Specification
- Entity: LeaveRequest (id, employeeId, type, startDate, endDate, status, balance)
- Controller: LeaveController (request, approve/deny, export)
- Service: LeaveService (balance update, policy checks, scheduling exclusion)
- Integration hooks for payroll and scheduling

### Sample Implementation
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
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
    @PostMapping("/request") public ResponseEntity<?> requestLeave(@RequestBody LeaveRequestDto dto) { ... }
    @PostMapping("/approve") public ResponseEntity<?> approveLeave(@RequestBody ApproveLeaveDto dto) { ... }
}
```

---

## Section: E07 - Training & Certification Tracking

### Description
Tracks employee certifications, expirations, renewals, blocks assignments for expired certs, and supports proof document uploads.

### Design Specification
- Entity: Certification (id, employeeId, type, expiryDate, status, proofDocumentUrl)
- Controller: CertificationController (CRUD, upload proof)
- Service: CertificationService (expiry alerts, assignment checks)
- Alerts 30/7 days before expiry

### Sample Implementation
```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type;
    private LocalDate expiryDate;
    private String status; // ACTIVE, EXPIRED
    private String proofDocumentUrl;
}

@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping public ResponseEntity<?> addCertification(@RequestBody CertificationDto dto) { ... }
    @PostMapping("/upload-proof") public ResponseEntity<?> uploadProof(@RequestParam Long id, @RequestParam MultipartFile file) { ... }
}
```

---

## Section: E08 - Safety Incidents & OSHA Reporting

### Description
Records safety incidents, manages investigation workflow, generates OSHA summary, and provides safety metrics dashboard.

### Design Specification
- Entity: SafetyIncident (id, severity, location, description, involvedEmployees, status)
- Controller: SafetyController (record incident, workflow, export)
- Service: SafetyService (metrics, corrective actions)
- OSHA export endpoints

### Sample Implementation
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private String severity;
    private String location;
    private String description;
    @ElementCollection private List<Long> involvedEmployees;
    private String status; // OPEN, INVESTIGATING, RESOLVED
}

@RestController
@RequestMapping("/safety")
public class SafetyController {
    @PostMapping("/incidents") public ResponseEntity<?> recordIncident(@RequestBody SafetyIncidentDto dto) { ... }
    @PostMapping("/workflow") public ResponseEntity<?> updateWorkflow(@RequestBody IncidentWorkflowDto dto) { ... }
}
```

---

## Section: E09 - Equipment & Asset Assignment

### Description
Manages asset registry, check-in/out, blocks use for missing certifications, maintains asset condition, and logs history.

### Design Specification
- Entity: Asset (id, type, condition, assignedEmployeeId, checkedOutAt, checkedInAt)
- Controller: AssetController (CRUD, check-in/out)
- Service: AssetService (certification checks, overdue returns report)
- History log per asset/employee

### Sample Implementation
```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String type;
    private String condition;
    private Long assignedEmployeeId;
    private LocalDateTime checkedOutAt;
    private LocalDateTime checkedInAt;
}

@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/check-out") public ResponseEntity<?> checkOut(@RequestBody AssetCheckoutDto dto) { ... }
    @PostMapping("/check-in") public ResponseEntity<?> checkIn(@RequestBody AssetCheckinDto dto) { ... }
}
```

---

## Section: E10 - Performance Reviews & Goals

### Description
Supports review templates, goal tracking, competencies, ratings, comments, and immutable history after sign-off.

### Design Specification
- Entity: PerformanceReview (id, employeeId, cycle, goals, competencies, ratings, comments, status)
- Controller: ReviewController (create, assign, submit, export)
- Service: ReviewService (workflow, PDF export)
- Role-based visibility

### Sample Implementation
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String cycle;
    @ElementCollection private List<String> goals;
    @ElementCollection private List<String> competencies;
    private String ratings;
    private String comments;
    private String status; // DRAFT, SUBMITTED, ACKNOWLEDGED
}

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping("/create") public ResponseEntity<?> createReview(@RequestBody ReviewDto dto) { ... }
    @PostMapping("/submit") public ResponseEntity<?> submitReview(@RequestBody SubmitReviewDto dto) { ... }
}
```

---

## Section: E11 - Payroll Export Integration

### Description
Generates payroll files from attendance/leave, maps to provider formats, delivers securely, retries failed deliveries, and audits exports.

### Design Specification
- Entity: PayrollExport (id, period, fileUrl, status, provider, totals)
- Controller: PayrollController (generate, deliver, retry)
- Service: PayrollService (mapping, reconciliation, audit logging)
- Secure delivery via SFTP/API

### Sample Implementation
```java
@Entity
public class PayrollExport {
    @Id @GeneratedValue private Long id;
    private String period;
    private String fileUrl;
    private String status; // GENERATED, DELIVERED, FAILED
    private String provider;
    private BigDecimal totals;
}

@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/export") public ResponseEntity<?> exportPayroll(@RequestBody PayrollExportDto dto) { ... }
    @PostMapping("/retry") public ResponseEntity<?> retryExport(@RequestBody RetryExportDto dto) { ... }
}
```

---

## Section: E12 - Notifications & Announcements

### Description
Delivers in-app/email/SMS notifications for events, supports templates/localization, tracks delivery status, and manages dashboard announcements.

### Design Specification
- Entity: Notification (id, employeeId, type, channel, templateId, status, deliveryTimestamp)
- Controller: NotificationController (send, opt-in/out, dashboard announcements)
- Service: NotificationService (localization, rate limiting, delivery tracking)

### Sample Implementation
```java
@Entity
public class Notification {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type;
    private String channel; // IN_APP, EMAIL, SMS
    private Long templateId;
    private String status; // SENT, FAILED, DELIVERED
    private LocalDateTime deliveryTimestamp;
}

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping("/send") public ResponseEntity<?> sendNotification(@RequestBody NotificationDto dto) { ... }
    @PostMapping("/opt-in") public ResponseEntity<?> optIn(@RequestBody OptInDto dto) { ... }
}
```

---

## Section: E13 - Integration Layer (HRIS/WMS APIs)

### Description
Exposes REST APIs/connectors for HRIS, WMS, SSO, and webhooks; synchronizes master data; documents APIs in OpenAPI.

### Design Specification
- REST endpoints for HRIS (employee sync), WMS (department/location), SSO (JWT/OAuth2)
- WebhookController for event notifications
- Security via JWT/OAuth2
- OpenAPI documentation

### Sample Implementation
```java
@RestController
@RequestMapping("/integration")
public class IntegrationController {
    @PostMapping("/hris-sync") public ResponseEntity<?> syncHris(@RequestBody HrisSyncDto dto) { ... }
    @PostMapping("/wms-link") public ResponseEntity<?> linkWms(@RequestBody WmsLinkDto dto) { ... }
    @PostMapping("/webhook") public ResponseEntity<?> handleWebhook(@RequestBody WebhookEventDto dto) { ... }
}
```

---

## Section: E14 - Audit Trail & Compliance

### Description
Centralizes audit logging for sensitive changes, ensures tamper-evident storage, and supports export/filtering.

### Design Specification
- Entity: AuditLog (id, actor, timestamp, entity, before, after, action)
- Controller: AuditController (export, filter)
- Service: AuditService (immutable log, coverage tests)

### Sample Implementation
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    @Lob private String before;
    @Lob private String after;
    private String action;
}

@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/export") public ResponseEntity<Resource> exportAudit(@RequestParam Map<String, String> filters) { ... }
}
```

---

## Section: E15 - Reporting & Analytics

### Description
Provides operational reports (attendance, overtime, leave, certification, safety KPIs), supports CSV/PDF export, and role-based dashboards.

### Design Specification
- Report endpoints: /reports/attendance, /reports/overtime, /reports/leave, /reports/certification, /reports/safety
- Controller: ReportController (filter, export)
- Service: ReportService (metrics, BI integration)
- Access control by role

### Sample Implementation
```java
@RestController
@RequestMapping("/reports")
public class ReportController {
    @GetMapping("/attendance") public ResponseEntity<Resource> attendanceReport(@RequestParam Map<String, String> filters) { ... }
    @GetMapping("/overtime") public ResponseEntity<Resource> overtimeReport(@RequestParam Map<String, String> filters) { ... }
}
```

---

## Section: E16 - Mobile Access (PWA)

### Description
Delivers responsive mobile views for core flows, supports installable PWA manifest, offline queue for clock events, and mobile-specific endpoints.

### Design Specification
- Frontend: Responsive UI (Thymeleaf/React), PWA manifest (manifest.json)
- Offline queue for clock events (IndexedDB/localStorage)
- Mobile endpoints for schedules, leave requests, announcements

### Sample Implementation
```json
// manifest.json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "icons": [{ "src": "/icon-192.png", "sizes": "192x192", "type": "image/png" }]
}
```

---

## Section: E17 - Onboarding & Offboarding Workflow

### Description
Automates provisioning/deprovisioning, generates onboarding tasks, revokes access, collects assets, and updates schedules.

### Design Specification
- Entity: OnboardingTask, OffboardingTask
- Controller: OnboardingController, OffboardingController
- Service: OnboardingService, OffboardingService (HRIS triggers, asset collection, access revocation)

### Sample Implementation
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String description;
    private String status; // PENDING, COMPLETED
}

@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @PostMapping("/new-hire") public ResponseEntity<?> provisionNewHire(@RequestBody NewHireDto dto) { ... }
}
```

---

## Section: E18 - Localization & Multi-Warehouse Support

### Description
Supports multiple warehouses, policy scoping, UI localization, timezone-aware scheduling, and warehouse-specific calendars.

### Design Specification
- Entity: Warehouse (id, name, timezone, calendar)
- Controller: WarehouseController (CRUD, calendar)
- Service: LocalizationService (i18n, resource bundles)
- Timezone-aware scheduling logic

### Sample Implementation
```java
@Entity
public class Warehouse {
    @Id @GeneratedValue private Long id;
    private String name;
    private String timezone;
    @OneToMany private List<BlackoutDate> calendar;
}

@RestController
@RequestMapping("/warehouses")
public class WarehouseController {
    @PostMapping public ResponseEntity<?> createWarehouse(@RequestBody WarehouseDto dto) { ... }
}
```

---

## Section: E19 - Automated Testing & CI/CD

### Description
Implements unit/integration tests, CI pipeline with quality gates, automated DB migrations, blue-green/canary deployment, and contract tests.

### Design Specification
- Test coverage: 80%+ (JUnit, Mockito, TestContainers)
- CI pipeline: GitHub Actions/Jenkins (build, test, lint, deploy)
- DB migrations: Flyway/Liquibase
- Deployment: blue-green/canary (Kubernetes/AWS)
- Contract tests: Pact/Spring Cloud Contract

### Sample Implementation
```yaml
# .github/workflows/ci.yml
name: CI
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with: { java-version: '11' }
      - name: Build
        run: mvn clean install
      - name: Test
        run: mvn test
```

---

## Section: E20 - Documentation & Runbooks

### Description
Provides OpenAPI specification, architecture diagrams, deployment guides, troubleshooting runbooks, user manuals, and developer onboarding checklist.

### Design Specification
- OpenAPI spec: /v3/api-docs (Springdoc OpenAPI)
- Architecture diagrams: docs/architecture.md (C4 model)
- Deployment guides: docs/deployment.md
- Runbooks: docs/runbooks.md
- User manuals: docs/user-manuals/ (role-specific)
- Developer onboarding: docs/onboarding.md

### Sample Implementation
```java
// Springdoc OpenAPI configuration
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("Warehouse EMS API").version("1.0"));
    }
}
```

---

## Conclusion

This comprehensive low-level technical design document covers all 101 user stories across 20 epics for the Warehouse Employee Management System. The design follows Spring Boot best practices, industry standards, and provides detailed specifications for entities, services, controllers, and integration points. Each section includes sample implementations to guide development teams in building a robust, scalable, and maintainable system.