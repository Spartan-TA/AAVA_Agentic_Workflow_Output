# Warehouse Employee Management System â Low-Level Technical Design Document (Spring Boot)

This document provides comprehensive low-level technical design specifications for all 17 epics of the warehouse employee management system. Each section includes architecture overview, package structure, domain model, repository/service/controller layers, security, database schema, configuration, integration points, and code samples following Spring Boot best practices.

---

## E01 â Project Scaffolding & Domain Setup

### 1. Overview
- Spring Boot (Maven) project with modular package structure.
- Core modules: employee, scheduling, attendance, safety.
- Flyway/Liquibase for DB migrations.
- Spring Boot Actuator enabled for health checks.

### 2. Package Structure
```
com.warehouse
  âââ employee
  âââ scheduling
  âââ attendance
  âââ safety
  âââ config
  âââ common
```

### 3. Domain Model
- No entities yet; base structure only.

### 4. Repository Layer
- No repositories yet.

### 5. Service Layer
- No services yet.

### 6. Controller Layer
- No controllers yet.

### 7. Security Configuration
- No security yet.

### 8. Database Schema
- Baseline migration script (V1__init.sql):
```sql
-- Example Flyway migration
CREATE TABLE flyway_schema_history (...);
```

### 9. Configuration
- `application.properties`:
```
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost/warehouse
spring.flyway.enabled=true
management.endpoints.web.exposure.include=health,info
```

### 10. Integration Points
- None yet.

### 11. Code Samples
```java
@SpringBootApplication
public class WarehouseApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseApplication.class, args);
    }
}
```

---

## E02 â Employee Master Data CRUD

### 1. Overview
- Employee domain with CRUD APIs.
- Pagination, filtering, soft-delete.
- OpenAPI documentation.

### 2. Package Structure
```
com.warehouse.employee
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
```java
@Entity
@Table(name = "employees", indexes = @Index(columnList = "badge_id", unique = true))
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String badgeId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String department;
    private String shiftGroup;

    @Column(nullable = false)
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private Boolean deleted = false;
    // getters/setters
}
```

### 4. Repository Layer
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND e.department = :department")
    Page<Employee> findByDepartment(@Param("department") String department, Pageable pageable);
}
```

### 5. Service Layer
```java
public interface EmployeeService {
    Employee create(EmployeeDto dto);
    Employee update(Long id, EmployeeDto dto);
    void softDelete(Long id);
    Page<Employee> list(Pageable pageable, String filter);
}

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    // Implementation with validation, mapping, and business logic
}
```

### 6. Controller Layer
```java
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee API")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { ... }

    @GetMapping
    public Page<EmployeeDto> list(@PageableDefault Pageable pageable, @RequestParam Optional<String> department) { ... }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) { ... }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
}
```

### 7. Security Configuration
- Secured in E03.

### 8. Database Schema
```sql
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    badge_id VARCHAR(32) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    shift_group VARCHAR(64),
    hire_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_employee_badge_id ON employees(badge_id);
```

### 9. Configuration
- `springdoc.api-docs.enabled=true`

### 10. Integration Points
- None yet.

### 11. Code Samples
```java
public class EmployeeDto {
    @NotBlank private String badgeId;
    @NotBlank private String name;
    @NotNull private Role role;
    private String department;
    private String shiftGroup;
    @NotNull private LocalDate hireDate;
    @NotNull private Status status;
}
```

---

## E03 â Role-Based Access Control

### 1. Overview
- Spring Security with roles: ADMIN, HR, SUPERVISOR, WORKER.
- Method/endpoint security, row-level constraints.
- API key/OAuth2 toggle.

### 2. Package Structure
```
com.warehouse.security
  âââ config
  âââ model
  âââ service
```

### 3. Domain Model
```java
public enum Role { ADMIN, HR, SUPERVISOR, WORKER }
```

### 4. Repository Layer
- UserRepository for authentication.

### 5. Service Layer
- UserDetailsService for Spring Security.

### 6. Controller Layer
- Security endpoints (login, token).

### 7. Security Configuration
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
- API key toggle via `application.properties`:
```
security.auth.type=oauth2
```

### 8. Database Schema
- `users` table with roles.

### 9. Configuration
- Profiles for API key vs OAuth2.

### 10. Integration Points
- OAuth2 provider, API key validation.

### 11. Code Samples
```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteEmployee(Long id) { ... }
```

---

## E04 â Time & Attendance

### 1. Overview
- Clock-in/out endpoints with geofence/device capture.
- Shift association, hours calculation, missed punch handling.
- CSV export.

### 2. Package Structure
```
com.warehouse.attendance
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
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
    private String geoLocation;
    private Boolean approved;
}
```

### 4. Repository Layer
```java
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeAndDate(Employee employee, LocalDate date);
}
```

### 5. Service Layer
```java
public interface AttendanceService {
    void clockIn(ClockEventDto dto);
    void clockOut(ClockEventDto dto);
    List<AttendanceEvent> getDailyEvents(Long employeeId, LocalDate date);
    void handleMissedPunch(Long eventId, CorrectionDto dto);
}
```

### 6. Controller Layer
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<Void> clockIn(@Valid @RequestBody ClockEventDto dto) { ... }

    @PostMapping("/clock-out")
    public ResponseEntity<Void> clockOut(@Valid @RequestBody ClockEventDto dto) { ... }

    @GetMapping("/report")
    public ResponseEntity<Resource> exportCsv(@RequestParam LocalDate date) { ... }
}
```

### 7. Security Configuration
- Only authenticated users can clock in/out.

### 8. Database Schema
```sql
CREATE TABLE attendance_events (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT REFERENCES employees(id),
    timestamp TIMESTAMP NOT NULL,
    type VARCHAR(16) NOT NULL,
    device_id VARCHAR(64),
    geo_location VARCHAR(128),
    approved BOOLEAN DEFAULT TRUE
);
```

### 9. Configuration
- Geofence settings in `application.yml`.

### 10. Integration Points
- CSV export, payroll integration.

### 11. Code Samples
```java
public class ClockEventDto {
    @NotNull private Long employeeId;
    @NotNull private LocalDateTime timestamp;
    private String deviceId;
    private String geoLocation;
}
```

---

## E05 â Shift & Schedule Management

### 1. Overview
- Shift templates, rotations, overtime rules, employee assignments.
- Blackout dates, conflict detection, audit logging.

### 2. Package Structure
```
com.warehouse.scheduling
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isOvertime;
}

@Entity
public class EmployeeShift {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ShiftTemplate shiftTemplate;
    private LocalDate shiftDate;
    private Boolean blackout;
}
```

### 4. Repository Layer
```java
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {}
public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, Long> {
    List<EmployeeShift> findByEmployeeAndShiftDate(Employee employee, LocalDate date);
}
```

### 5. Service Layer
```java
public interface SchedulingService {
    void assignShift(Long employeeId, Long shiftTemplateId, LocalDate date);
    List<EmployeeShift> getShifts(Long employeeId, LocalDate from, LocalDate to);
    void detectConflicts(Long employeeId, LocalDate date);
}
```

### 6. Controller Layer
```java
@RestController
@RequestMapping("/scheduling")
public class SchedulingController {
    @PostMapping("/assign")
    public ResponseEntity<Void> assignShift(@RequestBody AssignShiftDto dto) { ... }

    @GetMapping("/shifts")
    public List<EmployeeShiftDto> getShifts(@RequestParam Long employeeId, @RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
}
```

### 7. Security Configuration
- Supervisors can bulk-assign; workers see own shifts.

### 8. Database Schema
```sql
CREATE TABLE shift_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_overtime BOOLEAN DEFAULT FALSE
);

CREATE TABLE employee_shifts (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT REFERENCES employees(id),
    shift_template_id BIGINT REFERENCES shift_templates(id),
    shift_date DATE NOT NULL,
    blackout BOOLEAN DEFAULT FALSE
);
```

### 9. Configuration
- Overtime rules in `application.yml`.

### 10. Integration Points
- Audit logging, calendar APIs.

### 11. Code Samples
```java
public class AssignShiftDto {
    @NotNull private Long employeeId;
    @NotNull private Long shiftTemplateId;
    @NotNull private LocalDate shiftDate;
}
```

---

## E06 â Leave & Absence Management

### 1. Overview
- PTO/sick/unpaid leave requests, approval workflow, accrual balances.
- Scheduling integration.

### 2. Package Structure
```
com.warehouse.leave
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
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
    private String reason;
}
```

### 4. Repository Layer
```java
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeAndStatus(Employee employee, LeaveStatus status);
}
```

### 5. Service Layer
```java
public interface LeaveService {
    LeaveRequest requestLeave(LeaveRequestDto dto);
    void approveLeave(Long requestId);
    void denyLeave(Long requestId);
    List<LeaveRequest> getEmployeeLeaves(Long employeeId);
}
```

### 6. Controller Layer
```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/request")
    public ResponseEntity<LeaveRequestDto> requestLeave(@Valid @RequestBody LeaveRequestDto dto) { ... }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id) { ... }
}
```

### 7. Security Configuration
- Employees request; supervisors approve/deny.

### 8. Database Schema
```sql
CREATE TABLE leave_requests (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT REFERENCES employees(id),
    type VARCHAR(16) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    reason VARCHAR(256)
);
```

### 9. Configuration
- Leave accrual policies in `application.yml`.

### 10. Integration Points
- Scheduling, payroll.

### 11. Code Samples
```java
public class LeaveRequestDto {
    @NotNull private Long employeeId;
    @NotNull private LeaveType type;
    @NotNull private LocalDate startDate;
    @NotNull private LocalDate endDate;
    private String reason;
}
```

---

## E07 â Training & Certification Tracking

### 1. Overview
- Track certifications, expirations, renewals.
- Block assignments for expired certs, document uploads.

### 2. Package Structure
```
com.warehouse.certification
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    @ManyToOne
    private Employee employee;
    private String documentUrl;
}
```

### 4. Repository Layer
```java
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByEmployeeAndExpiryDateAfter(Employee employee, LocalDate date);
}
```

### 5. Service Layer
```java
public interface CertificationService {
    Certification addCertification(CertificationDto dto);
    void renewCertification(Long id, LocalDate newExpiry);
    List<Certification> getExpiringCerts(int days);
}
```

### 6. Controller Layer
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<CertificationDto> add(@Valid @RequestBody CertificationDto dto) { ... }

    @GetMapping("/expiring")
    public List<CertificationDto> getExpiring(@RequestParam int days) { ... }
}
```

### 7. Security Configuration
- Only HR/admin can add/renew.

### 8. Database Schema
```sql
CREATE TABLE certifications (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    employee_id BIGINT REFERENCES employees(id),
    document_url VARCHAR(256)
);
```

### 9. Configuration
- Expiry alert thresholds in `application.yml`.

### 10. Integration Points
- Document storage, scheduling.

### 11. Code Samples
```java
public class CertificationDto {
    @NotNull private String name;
    @NotNull private LocalDate issueDate;
    @NotNull private LocalDate expiryDate;
    @NotNull private Long employeeId;
    private String documentUrl;
}
```

---

## E08 â Safety Incidents & OSHA Reporting

### 1. Overview
- Record incidents/near-misses, investigation workflow.
- OSHA 300/300A export, metrics dashboard.

### 2. Package Structure
```
com.warehouse.safety
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String description;
    private String location;
    private LocalDateTime occurredAt;
    @Enumerated(EnumType.STRING)
    private IncidentSeverity severity;
    @ManyToMany
    private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
}
```

### 4. Repository Layer
```java
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
    List<SafetyIncident> findByStatus(IncidentStatus status);
}
```

### 5. Service Layer
```java
public interface SafetyService {
    SafetyIncident reportIncident(SafetyIncidentDto dto);
    void updateStatus(Long id, IncidentStatus status);
    List<SafetyIncident> getIncidents(LocalDate from, LocalDate to);
}
```

### 6. Controller Layer
```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyController {
    @PostMapping
    public ResponseEntity<SafetyIncidentDto> report(@Valid @RequestBody SafetyIncidentDto dto) { ... }

    @GetMapping
    public List<SafetyIncidentDto> list(@RequestParam IncidentStatus status) { ... }
}
```

### 7. Security Configuration
- Only supervisors/admin can update status.

### 8. Database Schema
```sql
CREATE TABLE safety_incidents (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(512) NOT NULL,
    location VARCHAR(128),
    occurred_at TIMESTAMP NOT NULL,
    severity VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL
);
CREATE TABLE safety_incident_employees (
    incident_id BIGINT REFERENCES safety_incidents(id),
    employee_id BIGINT REFERENCES employees(id)
);
```

### 9. Configuration
- OSHA export settings in `application.yml`.

### 10. Integration Points
- Metrics dashboard, OSHA export.

### 11. Code Samples
```java
public class SafetyIncidentDto {
    @NotBlank private String description;
    private String location;
    @NotNull private LocalDateTime occurredAt;
    @NotNull private IncidentSeverity severity;
    private List<Long> involvedEmployeeIds;
    @NotNull private IncidentStatus status;
}
```

---

## E09 â Equipment & Asset Assignment

### 1. Overview
- Assign assets to employees, track checkout/return, certification validation, condition tracking.

### 2. Package Structure
```
com.warehouse.asset
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private String condition;
    @ManyToOne
    private Employee assignedTo;
    private LocalDateTime checkedOutAt;
    private LocalDateTime returnedAt;
}
```

### 4. Repository Layer
```java
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByAssignedTo(Employee employee);
}
```

### 5. Service Layer
```java
public interface AssetService {
    void assignAsset(Long assetId, Long employeeId);
    void returnAsset(Long assetId);
    List<Asset> getAssetsByEmployee(Long employeeId);
}
```

### 6. Controller Layer
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign")
    public ResponseEntity<Void> assign(@RequestBody AssignAssetDto dto) { ... }

    @PostMapping("/return")
    public ResponseEntity<Void> returnAsset(@RequestBody ReturnAssetDto dto) { ... }
}
```

### 7. Security Configuration
- Only supervisors/admin can assign/return.

### 8. Database Schema
```sql
CREATE TABLE assets (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(32) NOT NULL,
    serial_number VARCHAR(64) UNIQUE NOT NULL,
    condition VARCHAR(32),
    assigned_to BIGINT REFERENCES employees(id),
    checked_out_at TIMESTAMP,
    returned_at TIMESTAMP
);
```

### 9. Configuration
- Asset types in `application.yml`.

### 10. Integration Points
- Certification validation.

### 11. Code Samples
```java
public class AssignAssetDto {
    @NotNull private Long assetId;
    @NotNull private Long employeeId;
}
```

---

## E10 â Performance Reviews & Goals

### 1. Overview
- Review templates, goals tracking, competencies, ratings, acknowledgements, PDF export.

### 2. Package Structure
```
com.warehouse.performance
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate reviewDate;
    private String template;
    private String goals;
    private String competencies;
    private Integer rating;
    private Boolean supervisorAcknowledged;
    private Boolean employeeAcknowledged;
    private String pdfUrl;
}
```

### 4. Repository Layer
```java
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    List<PerformanceReview> findByEmployee(Employee employee);
}
```

### 5. Service Layer
```java
public interface PerformanceService {
    PerformanceReview createReview(PerformanceReviewDto dto);
    void acknowledgeReview(Long reviewId, boolean supervisor);
    List<PerformanceReview> getReviews(Long employeeId);
}
```

### 6. Controller Layer
```java
@RestController
@RequestMapping("/performance")
public class PerformanceController {
    @PostMapping("/review")
    public ResponseEntity<PerformanceReviewDto> create(@Valid @RequestBody PerformanceReviewDto dto) { ... }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<Void> acknowledge(@PathVariable Long id, @RequestParam boolean supervisor) { ... }
}
```

### 7. Security Configuration
- Role-based visibility.

### 8. Database Schema
```sql
CREATE TABLE performance_reviews (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT REFERENCES employees(id),
    review_date DATE NOT NULL,
    template VARCHAR(128),
    goals TEXT,
    competencies TEXT,
    rating INTEGER,
    supervisor_acknowledged BOOLEAN DEFAULT FALSE,
    employee_acknowledged BOOLEAN DEFAULT FALSE,
    pdf_url VARCHAR(256)
);
```

### 9. Configuration
- Review templates in `application.yml`.

### 10. Integration Points
- PDF export service.

### 11. Code Samples
```java
public class PerformanceReviewDto {
    @NotNull private Long employeeId;
    @NotNull private LocalDate reviewDate;
    private String template;
    private String goals;
    private String competencies;
    private Integer rating;
}
```

---

## E11 â Payroll Export Integration

### 1. Overview
- Generate payroll files from attendance/leave, map to provider formats, secure SFTP/API delivery.

### 2. Package Structure
```
com.warehouse.payroll
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
- No new entities; uses attendance/leave.

### 4. Repository Layer
- Uses attendance/leave repositories.

### 5. Service Layer
```java
public interface PayrollService {
    Resource generatePayrollFile(LocalDate periodStart, LocalDate periodEnd);
    void deliverPayroll(Resource file);
}
```

### 6. Controller Layer
```java
@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @GetMapping("/export")
    public ResponseEntity<Resource> export(@RequestParam LocalDate start, @RequestParam LocalDate end) { ... }
}
```

### 7. Security Configuration
- Only HR/admin can export.

### 8. Database Schema
- No new tables.

### 9. Configuration
- SFTP/API credentials in `application.yml`.

### 10. Integration Points
- External payroll provider.

### 11. Code Samples
```java
@Service
public class PayrollServiceImpl implements PayrollService {
    @Override
    public Resource generatePayrollFile(LocalDate start, LocalDate end) { ... }
    @Override
    public void deliverPayroll(Resource file) { ... }
}
```

---

## E12 â Notifications & Announcements

### 1. Overview
- In-app/email/SMS notifications for shift changes, cert expirations, approvals, announcements.
- Opt-in/out, quiet hours.

### 2. Package Structure
```
com.warehouse.notification
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private String channel; // EMAIL, SMS, IN_APP
    private String message;
    private LocalDateTime sentAt;
    private Boolean delivered;
    @ManyToOne
    private Employee recipient;
}
```

### 4. Repository Layer
```java
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipient(Employee employee);
}
```

### 5. Service Layer
```java
public interface NotificationService {
    void sendNotification(NotificationDto dto);
    List<Notification> getNotifications(Long employeeId);
}
```

### 6. Controller Layer
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping
    public ResponseEntity<Void> send(@RequestBody NotificationDto dto) { ... }

    @GetMapping
    public List<NotificationDto> list(@RequestParam Long employeeId) { ... }
}
```

### 7. Security Configuration
- Opt-in/out per channel.

### 8. Database Schema
```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    channel VARCHAR(16) NOT NULL,
    message TEXT NOT NULL,
    sent_at TIMESTAMP,
    delivered BOOLEAN DEFAULT FALSE,
    recipient_id BIGINT REFERENCES employees(id)
);
```

### 9. Configuration
- Quiet hours, rate limits in `application.yml`.

### 10. Integration Points
- Email/SMS providers.

### 11. Code Samples
```java
public class NotificationDto {
    @NotNull private String channel;
    @NotBlank private String message;
    @NotNull private Long recipientId;
}
```

---

## E13 â Integration Layer (HRIS/WMS APIs)

### 1. Overview
- REST APIs/connectors for HRIS, WMS, IDP SSO, webhooks.

### 2. Package Structure
```
com.warehouse.integration
  âââ hris
  âââ wms
  âââ idp
  âââ webhook
  âââ controller
```

### 3. Domain Model
- Uses employee/department/location.

### 4. Repository Layer
- Uses existing repositories.

### 5. Service Layer
```java
public interface HRISService {
    void syncEmployees();
}
public interface WMSService {
    void syncDepartments();
}
```

### 6. Controller Layer
```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    @PostMapping("/hris/sync")
    public ResponseEntity<Void> syncHRIS() { ... }
}
```

### 7. Security Configuration
- JWT/OAuth2-secured APIs.

### 8. Database Schema
- No new tables.

### 9. Configuration
- External API endpoints in `application.yml`.

### 10. Integration Points
- HRIS, WMS, IDP, webhooks.

### 11. Code Samples
```java
@Component
public class HRISSyncJob {
    @Scheduled(cron = "0 0 * * * *")
    public void sync() { ... }
}
```

---

## E14 â Audit Trail & Compliance

### 1. Overview
- Centralized audit logging for sensitive changes.
- Tamper-evident storage.

### 2. Package Structure
```
com.warehouse.audit
  âââ model
  âââ repository
  âââ service
```

### 3. Domain Model
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private Long entityId;
    private String actor;
    private LocalDateTime timestamp;
    private String before;
    private String after;
    private String action;
}
```

### 4. Repository Layer
```java
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityAndEntityId(String entity, Long entityId);
}
```

### 5. Service Layer
```java
public interface AuditService {
    void logChange(String entity, Long entityId, String actor, String before, String after, String action);
}
```

### 6. Controller Layer
- Export endpoint.

### 7. Security Configuration
- Only admin can export.

### 8. Database Schema
```sql
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    entity VARCHAR(64) NOT NULL,
    entity_id BIGINT NOT NULL,
    actor VARCHAR(64) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    before TEXT,
    after TEXT,
    action VARCHAR(32) NOT NULL
);
```

### 9. Configuration
- Immutable storage settings.

### 10. Integration Points
- None.

### 11. Code Samples
```java
@Service
public class AuditServiceImpl implements AuditService {
    @Transactional
    public void logChange(...) { ... }
}
```

---

## E15 â Reporting & Analytics

### 1. Overview
- Reports for attendance, overtime, leave, certifications, safety KPIs.
- CSV/PDF export, role-based dashboards.

### 2. Package Structure
```
com.warehouse.reporting
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
- Uses existing entities.

### 4. Repository Layer
- Custom queries for reports.

### 5. Service Layer
```java
public interface ReportingService {
    Resource generateReport(ReportType type, LocalDate from, LocalDate to, String filter);
}
```

### 6. Controller Layer
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping
    public ResponseEntity<Resource> getReport(@RequestParam ReportType type, @RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
}
```

### 7. Security Configuration
- Access controlled by role.

### 8. Database Schema
- No new tables.

### 9. Configuration
- Export settings in `application.yml`.

### 10. Integration Points
- BI tools.

### 11. Code Samples
```java
public enum ReportType { ATTENDANCE, OVERTIME, LEAVE, CERTIFICATION, SAFETY }
```

---

## E16 â Mobile Access (PWA)

### 1. Overview
- Responsive views for clock-in/out, schedules, leave requests, announcements.
- Offline-friendly, PWA manifest.

### 2. Package Structure
```
com.warehouse.mobile
  âââ controller
  âââ service
  âââ dto
```

### 3. Domain Model
- Uses existing entities.

### 4. Repository Layer
- Uses existing repositories.

### 5. Service Layer
- Mobile-specific logic (offline queue).

### 6. Controller Layer
```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @PostMapping("/clock-in")
    public ResponseEntity<Void> clockIn(@RequestBody ClockEventDto dto) { ... }
}
```

### 7. Security Configuration
- JWT tokens for mobile.

### 8. Database Schema
- No new tables.

### 9. Configuration
- PWA manifest, offline queue settings.

### 10. Integration Points
- None.

### 11. Code Samples
```json
// manifest.json
{
  "name": "Warehouse Employee PWA",
  "short_name": "WarehousePWA",
  "start_url": "/mobile",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

---

## E17 â Onboarding & Offboarding Workflow

### 1. Overview
- Automate account provisioning, initial schedule, training assignments, deprovision on termination.

### 2. Package Structure
```
com.warehouse.onboarding
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
- Uses employee, schedule, certification, asset.

### 4. Repository Layer
- Uses existing repositories.

### 5. Service Layer
```java
public interface OnboardingService {
    void onboardEmployee(OnboardingDto dto);
    void offboardEmployee(Long employeeId);
}
```

### 6. Controller Layer
```java
@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @PostMapping
    public ResponseEntity<Void> onboard(@RequestBody OnboardingDto dto) { ... }

    @PostMapping("/{id}/offboard")
    public ResponseEntity<Void> offboard(@PathVariable Long id) { ... }
}
```

### 7. Security Configuration
- Only HR/admin.

### 8. Database Schema
- No new tables.

### 9. Configuration
- Onboarding/offboarding tasks in `application.yml`.

### 10. Integration Points
- HRIS, asset management, training.

### 11. Code Samples
```java
public class OnboardingDto {
    @NotNull private Long employeeId;
    private List<Long> assetIds;
    private List<Long> trainingIds;
}
```

---

# End of Document

This technical design document is ready for Spring Boot development teams to implement each epic, ensuring high quality, uniformity, and compliance with industry standards.