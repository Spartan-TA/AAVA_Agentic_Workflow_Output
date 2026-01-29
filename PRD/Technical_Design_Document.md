# WAREHOUSE EMPLOYEE MANAGEMENT SYSTEM - LOW-LEVEL TECHNICAL DESIGN DOCUMENT

## Document Overview
This document provides comprehensive low-level technical design specifications for all 20 epics of the Warehouse Employee Management System (EMS), following Spring Boot best practices and industry standards.

---

## Section: E01 - Project Scaffolding & Domain Setup

### Description
Establishes the foundational architecture for the Warehouse Employee Management System using Spring Boot (Maven), including base packages, core modules, database migration, and health monitoring.

### Design Specification
- Spring Boot Maven project structure with modular packages: employee, scheduling, attendance, safety.
- Flyway/Liquibase for database migrations.
- Spring Boot Actuator for health checks.
- Standardized README with build/run steps.
- Application runs on port 8080.

### Sample Implementation
```java
// pom.xml (excerpt)
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

// application.yml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems
    username: ems_user
    password: secret
  flyway:
    enabled: true
    locations: classpath:db/migration

// Directory Structure
com.warehouseems
  âââ employee
  âââ scheduling
  âââ attendance
  âââ safety
  âââ config
  âââ Application.java

// Health check endpoint
GET /actuator/health
Response: {"status":"UP"}
```

---

## Section: E02 - Employee Master Data (CRUD)

### Description
Manages employee records with full CRUD operations, unique badgeId enforcement, soft-delete, pagination, and filtering.

### Design Specification
- Entity: Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService with transactional CRUD logic, soft-delete
- Controller: EmployeeController with REST endpoints
- DTOs: EmployeeRequest, EmployeeResponse
- OpenAPI documentation

### Sample Implementation
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue private Long id;
    @Column(nullable = false) private String name;
    @Column(name = "badge_id", nullable = false, unique = true) private String badgeId;
    @Enumerated(EnumType.STRING) private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING) private Status status;
    private boolean deleted = false;
    // getters/setters
}

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest req) { ... }
    @GetMapping public Page<EmployeeResponse> list(Pageable pageable, @RequestParam Map<String, String> filters) { ... }
    @PutMapping("/{id}") public EmployeeResponse update(@PathVariable Long id, @RequestBody EmployeeRequest req) { ... }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { ... }
}
```

---

## Section: E03 - Role Based Access Control (RBAC)

### Description
Implements security with roles (ADMIN, HR, SUPERVISOR, WORKER), endpoint/method security, and API key/OAuth2 toggle.

### Design Specification
- Spring Security configuration with role-based access.
- Method-level security using @PreAuthorize.
- API key/OAuth2 toggle via application.yml.
- Row-level security for team-based access.

### Sample Implementation
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .antMatchers("/actuator/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer().jwt();
    }
}

// Method security
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(#id, authentication))")
public EmployeeResponse getEmployee(Long id) { ... }
```

---

## Section: E04 - Time & Attendance (Clock In/Out)

### Description
Provides endpoints for clock-in/out, geofence/device validation, shift association, missed punch correction, and reporting.

### Design Specification
- Entity: AttendanceEvent (id, employeeId, type, timestamp, deviceId, location, status)
- Repository: AttendanceRepository
- Service: AttendanceService (clockIn, clockOut, correction workflow)
- Controller: AttendanceController
- Geofence validation logic
- CSV export endpoint

### Sample Implementation
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private EventType type; // CLOCK_IN, CLOCK_OUT
    private LocalDateTime timestamp;
    private String deviceId;
    private String location;
    @Enumerated(EnumType.STRING) private Status status; // NORMAL, CORRECTION_PENDING
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in") public ResponseEntity<?> clockIn(@RequestBody ClockInRequest req) { ... }
    @PostMapping("/clock-out") public ResponseEntity<?> clockOut(@RequestBody ClockOutRequest req) { ... }
    @GetMapping("/report") public ResponseEntity<Resource> exportReport(@RequestParam LocalDate date) { ... }
}
```

---

## Section: E05 - Shift & Schedule Management

### Description
Manages shift templates, rotations, overtime, employee assignments, blackout dates, and warehouse calendars.

### Design Specification
- Entities: ShiftTemplate, ShiftAssignment, WarehouseCalendar
- Repository: ShiftTemplateRepository, ShiftAssignmentRepository
- Service: ShiftService (conflict detection, bulk assignment)
- Controller: ShiftController
- Audit logging for changes

### Sample Implementation
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean overtimeAllowed;
    // ...
}

@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates") public ShiftTemplate createTemplate(@RequestBody ShiftTemplateRequest req) { ... }
    @PostMapping("/assignments/bulk") public void bulkAssign(@RequestBody BulkAssignmentRequest req) { ... }
}
```

---

## Section: E06 - Leave & Absence Management

### Description
Handles PTO, sick, unpaid leave requests, approvals, accruals, and integration with scheduling/payroll.

### Design Specification
- Entities: LeaveRequest, LeaveBalance
- Repository: LeaveRequestRepository
- Service: LeaveService (request, approve, accrual update)
- Controller: LeaveController
- Integration hooks for scheduling/payroll

### Sample Implementation
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING) private Status status; // PENDING, APPROVED, DENIED
}

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping public LeaveRequest submit(@RequestBody LeaveRequestDto req) { ... }
    @PostMapping("/{id}/approve") public void approve(@PathVariable Long id) { ... }
}
```

---

## Section: E07 - Training & Certification Tracking

### Description
Tracks employee certifications, expirations, renewals, and blocks assignments for expired certs.

### Design Specification
- Entities: Certification, EmployeeCertification
- Repository: CertificationRepository
- Service: CertificationService (expiry alerts, assignment checks)
- Controller: CertificationController
- Document upload for proof

### Sample Implementation
```java
@Entity
public class EmployeeCertification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private Certification certification;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
}

@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping("/upload") public void uploadProof(@RequestParam MultipartFile file) { ... }
}
```

---

## Section: E08 - Safety Incidents & OSHA Reporting

### Description
Records safety incidents, manages investigation workflow, and generates OSHA reports.

### Design Specification
- Entities: SafetyIncident, Investigation
- Repository: SafetyIncidentRepository
- Service: SafetyService (workflow, reporting)
- Controller: SafetyController
- OSHA 300/300A export

### Sample Implementation
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private String description;
    private String location;
    private LocalDateTime occurredAt;
    @Enumerated(EnumType.STRING) private Status status; // OPEN, INVESTIGATING, RESOLVED
}

@RestController
@RequestMapping("/safety/incidents")
public class SafetyController {
    @PostMapping public SafetyIncident report(@RequestBody SafetyIncidentDto req) { ... }
    @GetMapping("/oshasummary") public ResponseEntity<Resource> exportOSHA() { ... }
}
```

---

## Section: E09 - Equipment & Asset Assignment

### Description
Assigns assets to employees, tracks check-in/out, validates certifications, and maintains asset condition.

### Design Specification
- Entities: Asset, AssetAssignment
- Repository: AssetRepository, AssetAssignmentRepository
- Service: AssetService (cert validation, overdue tracking)
- Controller: AssetController

### Sample Implementation
```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String type;
    private String serialNumber;
    private String condition;
    private boolean available;
}

@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign") public void assign(@RequestBody AssetAssignmentRequest req) { ... }
    @PostMapping("/return") public void returnAsset(@RequestBody AssetReturnRequest req) { ... }
}
```

---

## Section: E10 - Performance Reviews & Goals

### Description
Manages review cycles, goals, competencies, ratings, and immutable history after sign-off.

### Design Specification
- Entities: PerformanceReview, Goal
- Repository: PerformanceReviewRepository
- Service: ReviewService (workflow, PDF export)
- Controller: ReviewController

### Sample Implementation
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private LocalDate reviewDate;
    private String comments;
    private boolean acknowledged;
    private boolean locked;
}

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping public PerformanceReview create(@RequestBody ReviewRequest req) { ... }
    @PostMapping("/{id}/acknowledge") public void acknowledge(@PathVariable Long id) { ... }
}
```

---

## Section: E11 - Payroll Export Integration

### Description
Generates payroll files from attendance/leave, maps to provider formats, and delivers securely.

### Design Specification
- Service: PayrollExportService (file generation, SFTP/API delivery, retry logic)
- Controller: PayrollController
- Audit logging for exports

### Sample Implementation
```java
@Service
public class PayrollExportService {
    public File generatePayrollExport(LocalDate period) { ... }
    public void deliver(File file) { ... }
}

@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @GetMapping("/export") public ResponseEntity<Resource> export(@RequestParam LocalDate period) { ... }
}
```

---

## Section: E12 - Notifications & Announcements

### Description
Sends in-app, email, and SMS notifications for key events, with opt-in/out and localization.

### Design Specification
- Entities: Notification, Announcement
- Service: NotificationService (delivery, rate limiting)
- Controller: NotificationController
- Localization support

### Sample Implementation
```java
@Entity
public class Notification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee recipient;
    private String channel; // EMAIL, SMS, IN_APP
    private String message;
    private LocalDateTime sentAt;
    private boolean delivered;
}

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping("/announce") public void announce(@RequestBody AnnouncementRequest req) { ... }
}
```

---

## Section: E13 - Integration Layer (HRIS/WMS APIs)

### Description
Exposes REST APIs and connectors for HRIS, WMS, SSO, and webhooks.

### Design Specification
- REST endpoints for HRIS sync, WMS data, SSO integration.
- JWT/OAuth2 security.
- Webhook event publishing.
- OpenAPI documentation.

### Sample Implementation
```java
@RestController
@RequestMapping("/api/integrations")
public class IntegrationController {
    @PostMapping("/hris/sync") public void syncHRIS(@RequestBody HRISSyncRequest req) { ... }
    @PostMapping("/wms/link") public void linkWMS(@RequestBody WMSLinkRequest req) { ... }
    @PostMapping("/webhook") public void webhook(@RequestBody WebhookEvent event) { ... }
}
```

---

## Section: E14 - Audit Trail & Compliance

### Description
Centralized audit logging for sensitive changes, immutable storage, and export.

### Design Specification
- Entity: AuditLog (actor, timestamp, entity, before/after, action)
- Service: AuditService (log, export)
- Controller: AuditController
- Tamper-evident storage

### Sample Implementation
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    private String action;
    @Lob private String before;
    @Lob private String after;
}

@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping public List<AuditLog> list(@RequestParam Map<String, String> filters) { ... }
}
```

---

## Section: E15 - Reporting & Analytics

### Description
Provides operational reports, CSV/PDF export, and role-based dashboards.

### Design Specification
- Service: ReportingService (attendance, overtime, leave, safety KPIs)
- Controller: ReportingController
- CSV/PDF export endpoints

### Sample Implementation
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance") public ResponseEntity<Resource> attendanceReport(@RequestParam Map<String, String> filters) { ... }
    @GetMapping("/overtime") public ResponseEntity<Resource> overtimeReport(@RequestParam Map<String, String> filters) { ... }
}
```

---

## Section: E16 - Mobile Access (PWA)

### Description
Responsive PWA for core workflows, offline support, and installable manifest.

### Design Specification
- Spring Boot serves static PWA assets.
- REST APIs for mobile flows.
- Offline queue for clock events.
- Lighthouse PWA compliance.

### Sample Implementation
```yaml
# manifest.json (static resources)
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

---

## Section: E17 - Onboarding & Offboarding Workflow

### Description
Automates provisioning/deprovisioning of accounts, schedules, training, and assets.

### Design Specification
- Service: OnboardingService (HRIS triggers, task generation)
- Controller: OnboardingController
- Integration with training, asset, and schedule modules

### Sample Implementation
```java
@Service
public class OnboardingService {
    public void onboardEmployee(Employee employee) { ... }
    public void offboardEmployee(Employee employee) { ... }
}

@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @PostMapping("/start") public void start(@RequestBody EmployeeDto req) { ... }
}
```

---

## Section: E18 - Localization & Multi-Warehouse

### Description
Supports multiple warehouses, calendars, policies, and UI localization.

### Design Specification
- Entities: Warehouse, WarehousePolicy
- Service: LocalizationService
- Controller: WarehouseController
- i18n message bundles

### Sample Implementation
```java
@Entity
public class Warehouse {
    @Id @GeneratedValue private Long id;
    private String name;
    private String locale;
}

@RestController
@RequestMapping("/warehouses")
public class WarehouseController {
    @GetMapping public List<Warehouse> list() { ... }
}
```

---

## Section: E19 - Disaster Recovery & Backup

### Description
Automated backups, restore procedures, and failover planning.

### Design Specification
- Scheduled backup jobs (Spring @Scheduled)
- Restore scripts and documentation
- Failover configuration

### Sample Implementation
```java
@Component
public class BackupJob {
    @Scheduled(cron = "0 0 2 * * *")
    public void backupDatabase() { ... }
}
```

---

## Section: E20 - Performance & Scalability

### Description
Optimizes for large data volumes, caching, pagination, and load testing.

### Design Specification
- Query optimization (indexes, projections)
- Redis caching for hot data
- Pageable endpoints
- Load test scripts

### Sample Implementation
```java
@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {
    @Query("SELECT a FROM AttendanceEvent a WHERE a.employee.id = :employeeId")
    Page<AttendanceEvent> findByEmployeeId(@Param("employeeId") Long employeeId, Pageable pageable);
}

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        return RedisCacheManager.create(factory);
    }
}
```

---

## Conclusion

This document provides a production-ready, detailed low-level technical design for all 20 epics of the Warehouse Employee Management System, following Spring Boot best practices and industry standards. Each section includes comprehensive design specifications and sample implementations to guide development teams in building a robust, scalable, and maintainable system.