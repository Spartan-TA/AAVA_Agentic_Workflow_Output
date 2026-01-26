# Warehouse Employee Management System â Low-Level Technical Design (Spring Boot)

---

## Table of Contents

- [E01. Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
- [E02. Employee Master Data (CRUD)](#e02-employee-master-data-crud)
- [E03. Role-Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
- [E04. Time & Attendance (Clock In/Out)](#e04-time--attendance-clock-inout)
- [E05. Shift & Schedule Management](#e05-shift--schedule-management)
- [E06. Leave & Absence Management](#e06-leave--absence-management)
- [E07. Training & Certification Tracking](#e07-training--certification-tracking)
- [E08. Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
- [E09. Equipment & Asset Assignment](#e09-equipment--asset-assignment)
- [E10. Performance Reviews & Goals](#e10-performance-reviews--goals)
- [E11. Payroll Export Integration](#e11-payroll-export-integration)
- [E12. Notifications & Announcements](#e12-notifications--announcements)
- [E13. Integration Layer (HRIS/WMS APIs)](#e13-integration-layer-hriswms-apis)
- [E14. Audit Trail & Compliance](#e14-audit-trail--compliance)
- [E15. Reporting & Analytics](#e15-reporting--analytics)
- [E16. Mobile Access (PWA)](#e16-mobile-access-pwa)
- [E17. Onboarding & Offboarding Workflow](#e17-onboarding--offboarding-workflow)
- [E18. Localization & Multi-Tenant](#e18-localization--multi-tenant)
- [E19. Observability](#e19-observability)
- [E20. Deployment & CI/CD](#e20-deployment--cicd)

---

## E01. Project Scaffolding & Domain Setup

### 1. Overview
- Spring Boot (Maven) monorepo with modular structure.
- Core modules: employee, scheduling, attendance, safety.
- Flyway/Liquibase for DB migrations.
- Spring Boot Actuator enabled for health checks.

### 2. Package Structure
```
com.wms
  âââ employee
  âââ attendance
  âââ scheduling
  âââ safety
  âââ config
  âââ audit
  âââ integration
  âââ notification
  âââ reporting
  âââ asset
  âââ leave
  âââ certification
  âââ review
  âââ payroll
  âââ mobile
  âââ onboarding
  âââ localization
  âââ observability
```

### 3. Configuration
- `application.yml` for environment configs.
- Flyway/Liquibase migration scripts in `/resources/db/migration`.
- Actuator endpoints enabled (`/actuator/health`, `/actuator/info`).

### 4. Code Snippet
```java
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}
```

---

## E02. Employee Master Data (CRUD)

### 1. Overview
- Employee entity with CRUD APIs.
- Supports soft-delete, pagination, filtering.

### 2. Package Structure
```
com.wms.employee
  âââ controller
  âââ service
  âââ repository
  âââ model
  âââ dto
```

### 3. Entity Design
```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String badgeId;

    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status; // ACTIVE, INACTIVE, TERMINATED

    private Boolean deleted = false;
}
```

### 4. Service Layer
```java
@Service
public class EmployeeService {
    @Transactional
    public Employee createEmployee(EmployeeDto dto) { ... }
    public Page<Employee> getEmployees(Pageable pageable, EmployeeFilter filter) { ... }
    public Employee updateEmployee(Long id, EmployeeDto dto) { ... }
    public void softDeleteEmployee(Long id) { ... }
}
```

### 5. Repository Layer
```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    @Query("SELECT e FROM Employee e WHERE e.deleted = false")
    Page<Employee> findAllActive(Pageable pageable);
}
```

### 6. Controller
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { ... }
    @GetMapping
    public Page<EmployeeDto> list(Pageable pageable, EmployeeFilter filter) { ... }
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) { ... }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
}
```

### 7. Validation & Exception Handling
- Use `@Valid` and custom validators for badgeId uniqueness.
- Global `@ControllerAdvice` for exception mapping.

### 8. API Spec
- `POST /employees`
- `GET /employees`
- `PUT /employees/{id}`
- `DELETE /employees/{id}` (soft delete)
- Pagination via query params.

---

## E03. Role-Based Access Control (RBAC)

### 1. Overview
- Spring Security with roles: ADMIN, HR, SUPERVISOR, WORKER.
- Method/endpoint security, row-level constraints.

### 2. Package Structure
```
com.wms.config.security
```

### 3. Configuration
```java
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
```

### 4. Service Layer
- Use `@PreAuthorize` for method-level security.
```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteEmployee(Long id) { ... }
```

### 5. API Key/OAuth2 Toggle
- Use profiles/config to switch between API key and OAuth2.

### 6. Exception Handling
- 401 for unauthorized, 403 for forbidden.

---

## E04. Time & Attendance (Clock In/Out)

### 1. Overview
- Clock-in/out endpoints, geofence/device capture, hours calculation.

### 2. Package Structure
```
com.wms.attendance
  âââ controller
  âââ service
  âââ repository
  âââ model
  âââ dto
```

### 3. Entity Design
```java
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private AttendanceType type; // CLOCK_IN, CLOCK_OUT

    private LocalDateTime timestamp;
    private String deviceId;
    private String geoLocation;
    private Boolean correctionRequested = false;
}
```

### 4. Service Layer
```java
@Service
public class AttendanceService {
    @Transactional
    public AttendanceEvent clockIn(Long employeeId, ClockInDto dto) { ... }
    @Transactional
    public AttendanceEvent clockOut(Long employeeId, ClockOutDto dto) { ... }
    public List<AttendanceEvent> getDailyTotals(Long employeeId, LocalDate date) { ... }
    public void requestCorrection(Long eventId, CorrectionDto dto) { ... }
}
```

### 5. Controller
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceEventDto> clockIn(@RequestBody ClockInDto dto) { ... }
    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceEventDto> clockOut(@RequestBody ClockOutDto dto) { ... }
    @GetMapping("/totals")
    public List<AttendanceTotalDto> getTotals(@RequestParam Long employeeId, @RequestParam LocalDate date) { ... }
}
```

### 6. Validation
- Validate geofence, deviceId, and missed punches.

### 7. API Spec
- `POST /attendance/clock-in`
- `POST /attendance/clock-out`
- `GET /attendance/totals`
- Correction workflow endpoints.

---

## E05. Shift & Schedule Management

### 1. Overview
- Shift templates, rotations, overtime, blackout dates.

### 2. Package Structure
```
com.wms.scheduling
  âââ controller
  âââ service
  âââ repository
  âââ model
  âââ dto
```

### 3. Entity Design
```java
@Entity
@Table(name = "shifts")
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean recurring;
    private String rotationPattern;
    private Boolean blackout;
    private LocalDate blackoutDate;
}
@Entity
@Table(name = "employee_shifts")
public class EmployeeShift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private Shift shift;
    private LocalDate date;
    private Boolean overtime;
}
```

### 4. Service Layer
```java
@Service
public class ShiftService {
    public Shift createShift(ShiftDto dto) { ... }
    public void assignShift(Long employeeId, Long shiftId, LocalDate date) { ... }
    public List<Shift> getEmployeeShifts(Long employeeId) { ... }
    public void detectConflicts(Long employeeId, LocalDate date) { ... }
}
```

### 5. Controller
```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping
    public ResponseEntity<ShiftDto> create(@RequestBody ShiftDto dto) { ... }
    @PostMapping("/assign")
    public ResponseEntity<Void> assign(@RequestBody ShiftAssignDto dto) { ... }
    @GetMapping("/employee/{id}")
    public List<ShiftDto> getEmployeeShifts(@PathVariable Long id) { ... }
}
```

### 6. Audit
- Generate audit entries for bulk assignments.

---

## E06. Leave & Absence Management

### 1. Overview
- PTO, sick, unpaid leave requests, approval workflow, accruals.

### 2. Package Structure
```
com.wms.leave
  âââ controller
  âââ service
  âââ repository
  âââ model
  âââ dto
```

### 3. Entity Design
```java
@Entity
@Table(name = "leave_requests")
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Employee employee;
    @Enumerated(EnumType.STRING)
    private LeaveType type; // PTO, SICK, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status; // REQUESTED, APPROVED, DENIED
    private Double accrualBalance;
}
```

### 4. Service Layer
```java
@Service
public class LeaveService {
    public LeaveRequest requestLeave(Long employeeId, LeaveRequestDto dto) { ... }
    public void approveLeave(Long requestId) { ... }
    public void denyLeave(Long requestId) { ... }
    public List<LeaveRequest> getEmployeeLeaves(Long employeeId) { ... }
}
```

### 5. Controller
```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/request")
    public ResponseEntity<LeaveRequestDto> request(@RequestBody LeaveRequestDto dto) { ... }
    @PostMapping("/approve/{id}")
    public ResponseEntity<Void> approve(@PathVariable Long id) { ... }
    @PostMapping("/deny/{id}")
    public ResponseEntity<Void> deny(@PathVariable Long id) { ... }
    @GetMapping("/employee/{id}")
    public List<LeaveRequestDto> getEmployeeLeaves(@PathVariable Long id) { ... }
}
```

### 6. Integration
- Exclude approved leaves from scheduling and payroll.

---

## E07. Training & Certification Tracking

### 1. Overview
- Track certifications, expirations, renewals, proof uploads.

### 2. Package Structure
```
com.wms.certification
  âââ controller
  âââ service
  âââ repository
  âââ model
  âââ dto
```

### 3. Entity Design
```java
@Entity
@Table(name = "certifications")
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type; // e.g., Forklift
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String proofDocumentUrl;
}
```

### 4. Service Layer
```java
@Service
public class CertificationService {
    public Certification addCertification(Long employeeId, CertificationDto dto) { ... }
    public List<Certification> getExpiringCertifications(int days) { ... }
    public void blockAssignmentIfExpired(Long employeeId, String certType) { ... }
}
```

### 5. Controller
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<CertificationDto> add(@RequestBody CertificationDto dto) { ... }
    @GetMapping("/expiring")
    public List<CertificationDto> getExpiring(@RequestParam int days) { ... }
}
```

### 6. Integration
- Block scheduling if required certs are expired.

---

## E08. Safety Incidents & OSHA Reporting

### 1. Overview
- Record incidents, workflow for investigation, OSHA summary.

### 2. Package Structure
```
com.wms.safety
  âââ controller
  âââ service
  âââ repository
  âââ model
  âââ dto
```

### 3. Entity Design
```java
@Entity
@Table(name = "safety_incidents")
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String severity;
    private String location;
    private String description;
    @ManyToMany
    private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status; // OPEN, INVESTIGATING, RESOLVED
    private LocalDateTime reportedAt;
}
```

### 4. Service Layer
```java
@Service
public class SafetyService {
    public SafetyIncident reportIncident(SafetyIncidentDto dto) { ... }
    public void updateStatus(Long incidentId, IncidentStatus status) { ... }
    public List<SafetyIncident> getIncidentsByStatus(IncidentStatus status) { ... }
}
```

### 5. Controller
```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyController {
    @PostMapping
    public ResponseEntity<SafetyIncidentDto> report(@RequestBody SafetyIncidentDto dto) { ... }
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody IncidentStatusDto dto) { ... }
    @GetMapping
    public List<SafetyIncidentDto> list(@RequestParam IncidentStatus status) { ... }
}
```

### 6. Reporting
- Export OSHA 300/300A fields, metrics dashboard endpoints.

---

## E09. Equipment & Asset Assignment

### 1. Overview
- Assign assets, track checkout/return, block use if cert missing.

### 2. Package Structure
```
com.wms.asset
  âââ controller
  âââ service
  âââ repository
  âââ model
  âââ dto
```

### 3. Entity Design
```java
@Entity
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type; // Scanner, Forklift, PPE
    private String serialNumber;
    private AssetCondition condition;
    private Boolean checkedOut;
    @ManyToOne
    private Employee assignedTo;
    private LocalDateTime checkedOutAt;
    private LocalDateTime returnedAt;
}
```

### 4. Service Layer
```java
@Service
public class AssetService {
    public Asset assignAsset(Long employeeId, AssetAssignDto dto) { ... }
    public void checkInAsset(Long assetId) { ... }
    public List<Asset> getEmployeeAssets(Long employeeId) { ... }
    public void blockIfCertMissing(Long employeeId, String assetType) { ... }
}
```

### 5. Controller
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign")
    public ResponseEntity<AssetDto> assign(@RequestBody AssetAssignDto dto) { ... }
    @PostMapping("/checkin/{id}")
    public ResponseEntity<Void> checkIn(@PathVariable Long id) { ... }
    @GetMapping("/employee/{id}")
    public List<AssetDto> getEmployeeAssets(@PathVariable Long id) { ... }
}
```

### 6. Reporting
- History log per asset/employee, overdue returns report.

---

## E10. Performance Reviews & Goals

### 1. Overview
- Review templates, goals, ratings, comments, workflow.

### 2. Package Structure
```
com.wms.review
  âââ controller
  âââ service
  âââ repository
  âââ model
  âââ dto
```

### 3. Entity Design
```java
@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Employee employee;
    private String cycle; // Q1, Q2, Annual
    private String goals;
    private String competencies;
    private Integer rating;
    private String comments;
    private Boolean acknowledgedBySupervisor;
    private Boolean acknowledgedByEmployee;
    private Boolean immutableAfterSignoff;
}
```

### 4. Service Layer
```java
@Service
public class ReviewService {
    public PerformanceReview createReview(Long employeeId, ReviewDto dto) { ... }
    public void acknowledgeReview(Long reviewId, String role) { ... }
    public List<PerformanceReview> getEmployeeReviews(Long employeeId) { ... }
}
```

### 5. Controller
```java
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping
    public ResponseEntity<ReviewDto> create(@RequestBody ReviewDto dto) { ... }
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<Void> acknowledge(@PathVariable Long id, @RequestParam String role) { ... }
    @GetMapping("/employee/{id}")
    public List<ReviewDto> getEmployeeReviews(@PathVariable Long id) { ... }
}
```

### 6. Export
- PDF export endpoint.

---

## E11. Payroll Export Integration

### 1. Overview
- Generate payroll files, map to provider formats, secure delivery.

### 2. Package Structure
```
com.wms.payroll
  âââ controller
  âââ service
  âââ integration
  âââ model
  âââ dto
```

### 3. Service Layer
```java
@Service
public class PayrollService {
    public PayrollExport exportPayroll(PayrollExportRequestDto dto) { ... }
    public void deliverExport(PayrollExport export, DeliveryMethod method) { ... }
    public void retryFailedDeliveries(Long exportId) { ... }
}
```

### 4. Controller
```java
@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/export")
    public ResponseEntity<PayrollExportDto> export(@RequestBody PayrollExportRequestDto dto) { ... }
    @PostMapping("/deliver/{id}")
    public ResponseEntity<Void> deliver(@PathVariable Long id, @RequestParam DeliveryMethod method) { ... }
}
```

### 5. Integration
- SFTP/API delivery, audit log for exports.

---

## E12. Notifications & Announcements

### 1. Overview
- In-app/email/SMS notifications, quiet hours, opt-in/out.

### 2. Package Structure
```
com.wms.notification
  âââ controller
  âââ service
  âââ integration
  âââ model
  âââ dto
```

### 3. Entity Design
```java
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String channel; // EMAIL, SMS, IN_APP
    private String template;
    private String recipient;
    private Boolean delivered;
    private LocalDateTime sentAt;
    private Boolean optIn;
    private String locale;
}
```

### 4. Service Layer
```java
@Service
public class NotificationService {
    public void sendNotification(NotificationDto dto) { ... }
    public void trackDelivery(Long notificationId) { ... }
    public void applyRateLimit(String recipient) { ... }
}
```

### 5. Controller
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping
    public ResponseEntity<Void> send(@RequestBody NotificationDto dto) { ... }
    @GetMapping("/user/{id}")
    public List<NotificationDto> getUserNotifications(@PathVariable Long id) { ... }
}
```

### 6. Integration
- Email/SMS gateway, dashboard announcements.

---

## E13. Integration Layer (HRIS/WMS APIs)

### 1. Overview
- REST APIs/connectors for HRIS, WMS, IDP (SSO), webhooks.

### 2. Package Structure
```
com.wms.integration
  âââ controller
  âââ service
  âââ client
  âââ model
  âââ dto
```

### 3. Service Layer
```java
@Service
public class IntegrationService {
    public void syncHrisEmployee(HrisEmployeeDto dto) { ... }
    public void syncWmsDepartment(WmsDepartmentDto dto) { ... }
    public void handleWebhook(WebhookEventDto dto) { ... }
}
```

### 4. Controller
```java
@RestController
@RequestMapping("/integration")
public class IntegrationController {
    @PostMapping("/hris/sync")
    public ResponseEntity<Void> syncHris(@RequestBody HrisEmployeeDto dto) { ... }
    @PostMapping("/wms/sync")
    public ResponseEntity<Void> syncWms(@RequestBody WmsDepartmentDto dto) { ... }
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody WebhookEventDto dto) { ... }
}
```

### 5. Security
- JWT/OAuth2 for APIs, idempotent webhooks.

---

## E14. Audit Trail & Compliance

### 1. Overview
- Centralized audit logging for sensitive changes.

### 2. Package Structure
```
com.wms.audit
  âââ service
  âââ repository
  âââ model
```

### 3. Entity Design
```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    private String action;
    private String before;
    private String after;
}
```

### 4. Service Layer
```java
@Service
public class AuditService {
    public void logChange(String actor, String entity, String action, Object before, Object after) { ... }
    public List<AuditLog> exportLogs(LocalDate from, LocalDate to, String entity) { ... }
}
```

---

## E15. Reporting & Analytics

### 1. Overview
- Attendance, overtime, leave, certification, safety KPIs, CSV/PDF export.

### 2. Package Structure
```
com.wms.reporting
  âââ controller
  âââ service
  âââ model
  âââ dto
```

### 3. Service Layer
```java
@Service
public class ReportingService {
    public Report generateAttendanceReport(ReportFilterDto filter) { ... }
    public Report generateSafetyKpiReport(ReportFilterDto filter) { ... }
    public byte[] exportReport(Report report, ExportFormat format) { ... }
}
```

### 4. Controller
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<ReportDto> attendance(@RequestParam ReportFilterDto filter) { ... }
    @GetMapping("/safety")
    public ResponseEntity<ReportDto> safety(@RequestParam ReportFilterDto filter) { ... }
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam Long reportId, @RequestParam ExportFormat format) { ... }
}
```

---

## E16. Mobile Access (PWA)

### 1. Overview
- Responsive views, offline queue, PWA manifest.

### 2. Package Structure
```
com.wms.mobile
  âââ controller
  âââ service
  âââ model
  âââ dto
```

### 3. Service Layer
- Offline queue for clock events, conflict resolution logic.

### 4. Controller
```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @GetMapping("/schedule")
    public List<ShiftDto> getSchedule(@RequestParam Long employeeId) { ... }
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceEventDto> clockIn(@RequestBody ClockInDto dto) { ... }
}
```

### 5. PWA Manifest
- `/resources/static/manifest.json` with installable config.

---

## E17. Onboarding & Offboarding Workflow

### 1. Overview
- Automate provisioning, training, asset assignment, deprovisioning.

### 2. Package Structure
```
com.wms.onboarding
  âââ controller
  âââ service
  âââ model
  âââ dto
```

### 3. Service Layer
```java
@Service
public class OnboardingService {
    public void onboardEmployee(HrisEmployeeDto dto) { ... }
    public void generateTrainingTasks(Long employeeId) { ... }
    public void assignAssets(Long employeeId) { ... }
    public void offboardEmployee(Long employeeId) { ... }
}
```

### 4. Controller
```java
@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @PostMapping("/onboard")
    public ResponseEntity<Void> onboard(@RequestBody HrisEmployeeDto dto) { ... }
    @PostMapping("/offboard/{id}")
    public ResponseEntity<Void> offboard(@PathVariable Long id) { ... }
}
```

---

## E18. Localization & Multi-Tenant

### 1. Overview
- Localized templates, multi-tenant data isolation.

### 2. Package Structure
```
com.wms.localization
com.wms.tenant
```

### 3. Entity Design
```java
@Entity
@Table(name = "tenants")
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String locale;
}
```

### 4. Configuration
- Use `LocaleResolver` and tenant context filter.

### 5. Service Layer
- Data isolation via tenantId in all queries.

---

## E19. Observability

### 1. Overview
- Metrics, tracing, logging, health checks.

### 2. Package Structure
```
com.wms.observability
```

### 3. Configuration
- Spring Boot Actuator endpoints.
- Micrometer integration for Prometheus/Grafana.
- Distributed tracing via OpenTelemetry.

---

## E20. Deployment & CI/CD

### 1. Overview
- Dockerized deployment, CI/CD pipeline.

### 2. Package Structure
- `/Dockerfile`
- `/Jenkinsfile` or `/github/workflows/ci.yml`

### 3. Configuration
- Multi-stage Docker build.
- CI pipeline: build, test, lint, deploy.
- Environment variables for secrets/config.

---

# General Exception Handling & Validation

- Use `@ControllerAdvice` for global exception mapping.
- Custom exceptions for business logic errors.
- Bean validation (`@Valid`, `@NotNull`, `@Size`, etc.) on DTOs.
- Error responses standardized via `ErrorResponseDto`.

---

# Database Schema Considerations

- Use Flyway/Liquibase for migrations.
- All tables have `created_at`, `updated_at`, `deleted` (soft delete).
- Foreign keys for relationships, indexes on badgeId, asset serial, etc.

---

# API Endpoint Specifications

- All endpoints documented via OpenAPI/Swagger.
- Request/response DTOs defined per controller.
- HTTP methods: GET (read), POST (create), PUT/PATCH (update), DELETE (soft delete).
- Pagination via `Pageable` and query params.

---

# Integration Points

- External APIs: HRIS, WMS, Payroll, Email/SMS gateways.
- Secure via OAuth2/JWT, API keys.
- Webhooks for event-driven integrations.

---

# Code Patterns

- Use `@Service` for business logic, `@Repository` for data access.
- Transactional boundaries via `@Transactional`.
- DTOs for API layer, entities for persistence.
- Layered architecture: Controller â Service â Repository.

---

This document is production-ready and provides all necessary details for Spring Boot developers to implement the warehouse employee management system according to industry best practices.