# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

---

## User Story 1: Initialize Spring Boot Project

### Overview
As a developer, I want to initialize a Spring Boot (Maven) project to provide a standardized foundation for all modules.

### Spring Boot Architecture
- Layered architecture (Controller, Service, Repository, Domain)
- Maven-based multi-module structure for scalability

### Package Structure
```
com.warehouse.ems
âââ WarehouseEmsApplication.java
âââ config
âââ domain
âââ repository
âââ service
â   âââ impl
âââ controller
âââ dto
â   âââ request
â   âââ response
âââ security
```

### Domain Model Design
- No entities at this stage.

### Repository Layer
- No repositories at this stage.

### Service Layer
- No services at this stage.

### Controller Design
- No controllers at this stage.

### Configuration & Security
- `application.yml` with basic server port, context path, and DB connection.
- Add Flyway/Liquibase for DB migrations.

### Integration Points
- None at this stage.

### Sample Implementation
```java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```
```yaml
# application.yml
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
```

---

## User Story 2: Configure Base Packages and Core Modules

### Overview
As a developer, I want to configure base packages and set up core modules for consistent and scalable project structure.

### Spring Boot Architecture
- Modular structure for domain-driven design.
- Core modules: employee, scheduling, attendance, safety.

### Package Structure
```
com.warehouse.ems
âââ domain
â   âââ employee
â   âââ scheduling
â   âââ attendance
â   âââ safety
âââ repository
â   âââ employee
â   âââ scheduling
â   âââ attendance
â   âââ safety
âââ service
â   âââ employee
â   âââ scheduling
â   âââ attendance
â   âââ safety
â       âââ impl
âââ controller
â   âââ employee
â   âââ scheduling
â   âââ attendance
â   âââ safety
âââ dto
â   âââ employee
â   âââ scheduling
â   âââ attendance
â   âââ safety
âââ config
âââ security
```

### Domain Model Design
- Placeholder entities for each module.

### Repository Layer
- Placeholder repository interfaces.

### Service Layer
- Placeholder service interfaces and implementations.

### Controller Design
- Placeholder REST controllers.

### Configuration & Security
- No additional configuration.

### Integration Points
- None at this stage.

### Sample Implementation
```java
package com.warehouse.ems.domain.employee;
@Entity
public class Employee { /* ... */ }
```
```java
package com.warehouse.ems.repository.employee;
public interface EmployeeRepository extends JpaRepository<Employee, Long> {}
```

---

## User Story 3: Enable Actuator for Monitoring

### Overview
As an operations engineer, I want to enable Actuator endpoints for monitoring application health and metrics.

### Spring Boot Architecture
- Actuator endpoints exposed for health, metrics, info, etc.

### Package Structure
- No changes required.

### Domain Model Design
- Not applicable.

### Repository Layer
- Not applicable.

### Service Layer
- Not applicable.

### Controller Design
- Not applicable.

### Configuration & Security
- Enable actuator endpoints in `application.yml`.
- Secure sensitive endpoints.

### Integration Points
- Integration with monitoring tools (e.g., Prometheus).

### Sample Implementation
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

---

## User Story 4: Employee CRUD API

### Overview
As an HR manager, I want to create, read, update, and delete employee records to maintain accurate employee master data.

### Spring Boot Architecture
- Standard CRUD flow: Controller â Service â Repository â Domain

### Package Structure
```
com.warehouse.ems
âââ domain.employee.Employee
âââ repository.employee.EmployeeRepository
âââ service.employee.EmployeeService
â   âââ impl.EmployeeServiceImpl
âââ controller.employee.EmployeeController
âââ dto.employee.request.EmployeeRequestDto
âââ dto.employee.response.EmployeeResponseDto
```

### Domain Model Design
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String badgeId;
    @Column(nullable = false)
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    // getters and setters
}
```

### Repository Layer
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    boolean existsByBadgeId(String badgeId);
}
```

### Service Layer
```java
public interface EmployeeService {
    EmployeeResponseDto createEmployee(EmployeeRequestDto dto);
    EmployeeResponseDto getEmployee(Long id);
    Page<EmployeeResponseDto> getAllEmployees(Pageable pageable);
    EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto dto);
    void deleteEmployee(Long id);
}
```
```java
@Service
public class EmployeeServiceImpl implements EmployeeService {
    // Autowired repository, mapping logic, exception handling
}
```

### Controller Design
```java
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeResponseDto> create(@Valid @RequestBody EmployeeRequestDto dto) { ... }
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> get(@PathVariable Long id) { ... }
    @GetMapping
    public Page<EmployeeResponseDto> list(Pageable pageable) { ... }
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDto dto) { ... }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
}
```

### Configuration & Security
- No special configuration at this stage.

### Integration Points
- None.

### Sample Implementation
```java
public class EmployeeRequestDto {
    @NotBlank
    private String badgeId;
    @NotBlank
    private String name;
    // other fields
}
```

---

## User Story 5: Employee Data Validation and OpenAPI Documentation

### Overview
As a developer, I want to enforce unique badgeId and document APIs with OpenAPI for data integrity and API usability.

### Spring Boot Architecture
- Bean validation and OpenAPI integration.

### Package Structure
- Add `config.OpenApiConfig`.

### Domain Model Design
- Add validation annotations to Employee fields.

### Repository Layer
- Already supports unique badgeId.

### Service Layer
- Throw exception if badgeId exists.

### Controller Design
- Use `@Valid` for DTOs.
- Document endpoints with Swagger/OpenAPI.

### Configuration & Security
- Add springdoc-openapi dependency.
- Configure OpenAPI.

### Integration Points
- OpenAPI UI at `/swagger-ui.html`.

### Sample Implementation
```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info().title("Warehouse EMS API").version("1.0"));
    }
}
```
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.0.2</version>
</dependency>
```

---

## User Story 6: Role-Based Access Control Setup

### Overview
As a system administrator, I want to configure roles and permissions to restrict sensitive operations to authorized users.

### Spring Boot Architecture
- Spring Security with method and endpoint security.

### Package Structure
```
com.warehouse.ems.security
âââ SecurityConfig.java
âââ CustomUserDetailsService.java
âââ JwtAuthenticationFilter.java
```

### Domain Model Design
```java
@Entity
public class Role {
    @Id
    private String name; // ADMIN, HR, SUPERVISOR, WORKER
}
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    private String username;
    private String password;
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;
}
```

### Repository Layer
- `UserRepository`, `RoleRepository`.

### Service Layer
- `UserDetailsService` implementation.

### Controller Design
- Secure endpoints with `@PreAuthorize`.

### Configuration & Security
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/api/employees/**").hasAnyRole("ADMIN", "HR")
                .anyRequest().authenticated()
            .and()
            .httpBasic();
    }
}
```

### Integration Points
- None.

### Sample Implementation
```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteEmployee(Long id) { ... }
```

---

## User Story 7: API Key/OAuth2 Security Toggle

### Overview
As a security engineer, I want to toggle between API key and OAuth2 authentication.

### Spring Boot Architecture
- Conditional security configuration.

### Package Structure
- Add `config.ApiKeyAuthFilter`, `config.OAuth2Config`.

### Domain Model Design
- Add `ApiKey` entity if needed.

### Repository Layer
- `ApiKeyRepository`.

### Service Layer
- API key validation logic.

### Controller Design
- No changes.

### Configuration & Security
```yaml
security:
  auth-type: api-key # or oauth2
```
```java
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${security.auth-type}")
    private String authType;
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if ("api-key".equals(authType)) {
            http.addFilterBefore(new ApiKeyAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        } else {
            // OAuth2 config
        }
    }
}
```

### Integration Points
- OAuth2 provider.

### Sample Implementation
```java
public class ApiKeyAuthFilter extends OncePerRequestFilter { ... }
```

---

## User Story 8: Time & Attendance Clock In/Out

### Overview
As a warehouse worker, I want to clock in and out to record my working hours.

### Spring Boot Architecture
- Attendance module: Controller â Service â Repository â Domain

### Package Structure
```
com.warehouse.ems.domain.attendance.AttendanceEvent
com.warehouse.ems.repository.attendance.AttendanceEventRepository
com.warehouse.ems.service.attendance.AttendanceService
com.warehouse.ems.controller.attendance.AttendanceController
com.warehouse.ems.dto.attendance.request.ClockInRequestDto
com.warehouse.ems.dto.attendance.response.ClockInResponseDto
```

### Domain Model Design
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private String deviceId;
    private String location;
    // getters/setters
}
```

### Repository Layer
```java
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeAndClockInTimeBetween(Employee employee, LocalDateTime start, LocalDateTime end);
}
```

### Service Layer
- Validate clock-in/out, calculate hours, handle missed punches.

### Controller Design
```java
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<ClockInResponseDto> clockIn(@Valid @RequestBody ClockInRequestDto dto) { ... }
    @PostMapping("/clock-out")
    public ResponseEntity<ClockInResponseDto> clockOut(@Valid @RequestBody ClockOutRequestDto dto) { ... }
}
```

### Configuration & Security
- Only authenticated users can clock in/out.

### Integration Points
- Optional: Geofencing API.

### Sample Implementation
```java
public class ClockInRequestDto {
    @NotNull
    private Long employeeId;
    private String deviceId;
    private String location;
}
```

---

## User Story 9: Attendance Corrections Workflow

### Overview
As a supervisor, I want to approve or deny attendance corrections for missed punches.

### Spring Boot Architecture
- Attendance correction workflow with approval.

### Package Structure
```
com.warehouse.ems.domain.attendance.AttendanceCorrection
com.warehouse.ems.repository.attendance.AttendanceCorrectionRepository
com.warehouse.ems.service.attendance.AttendanceCorrectionService
com.warehouse.ems.controller.attendance.AttendanceCorrectionController
```

### Domain Model Design
```java
@Entity
public class AttendanceCorrection {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime originalTime;
    private LocalDateTime correctedTime;
    private String reason;
    @Enumerated(EnumType.STRING)
    private CorrectionStatus status; // PENDING, APPROVED, DENIED
    // audit fields
}
```

### Repository Layer
- Standard JPA repository.

### Service Layer
- Submit correction, supervisor approves/denies.

### Controller Design
- Endpoints for submit, approve, deny.

### Configuration & Security
- Only supervisors/HR can approve/deny.

### Integration Points
- Notification service.

### Sample Implementation
```java
@PreAuthorize("hasRole('SUPERVISOR')")
@PostMapping("/attendance/corrections/{id}/approve")
public void approveCorrection(@PathVariable Long id) { ... }
```

---

## User Story 10: Shift Template Management

### Overview
As a scheduling manager, I want to create and manage shift templates for recurring schedules.

### Spring Boot Architecture
- Scheduling module with shift templates.

### Package Structure
```
com.warehouse.ems.domain.scheduling.ShiftTemplate
com.warehouse.ems.repository.scheduling.ShiftTemplateRepository
com.warehouse.ems.service.scheduling.ShiftTemplateService
com.warehouse.ems.controller.scheduling.ShiftTemplateController
```

### Domain Model Design
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrencePattern; // e.g., "WEEKLY"
    // blackout dates, etc.
}
```

### Repository Layer
- Standard JPA repository.

### Service Layer
- CRUD for templates, conflict detection.

### Controller Design
- Endpoints for create, update, delete, list.

### Configuration & Security
- Only managers can manage templates.

### Integration Points
- Calendar API (optional).

### Sample Implementation
```java
@PostMapping("/shift-templates")
public ResponseEntity<ShiftTemplateResponseDto> create(@Valid @RequestBody ShiftTemplateRequestDto dto) { ... }
```

---

## User Story 11: Bulk Shift Assignment

### Overview
As a supervisor, I want to bulk-assign shifts to employees.

### Spring Boot Architecture
- Bulk assignment logic in scheduling service.

### Package Structure
```
com.warehouse.ems.service.scheduling.BulkAssignmentService
com.warehouse.ems.controller.scheduling.BulkAssignmentController
```

### Domain Model Design
- Use `ShiftAssignment` entity.

### Repository Layer
- `ShiftAssignmentRepository`.

### Service Layer
- Bulk assignment logic, transaction management.

### Controller Design
- Endpoint for bulk assignment.

### Configuration & Security
- Only supervisors/managers.

### Integration Points
- Notification service.

### Sample Implementation
```java
@PostMapping("/shifts/assign/bulk")
public ResponseEntity<Void> bulkAssign(@RequestBody BulkAssignRequestDto dto) { ... }
```

---

## User Story 12: Leave Request and Approval

### Overview
As an employee, I want to request leave and have it tracked and approved.

### Spring Boot Architecture
- Leave management module.

### Package Structure
```
com.warehouse.ems.domain.leave.LeaveRequest
com.warehouse.ems.repository.leave.LeaveRequestRepository
com.warehouse.ems.service.leave.LeaveService
com.warehouse.ems.controller.leave.LeaveController
```

### Domain Model Design
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LeaveType type; // PTO, SICK, UNPAID
    @Enumerated(EnumType.STRING)
    private LeaveStatus status; // PENDING, APPROVED, DENIED
}
```

### Repository Layer
- Standard JPA repository.

### Service Layer
- Submit, approve, deny, update balances.

### Controller Design
- Endpoints for request, approve, deny.

### Configuration & Security
- Only supervisors/HR can approve.

### Integration Points
- Notification service.

### Sample Implementation
```java
@PostMapping("/leave/requests")
public ResponseEntity<LeaveResponseDto> requestLeave(@Valid @RequestBody LeaveRequestDto dto) { ... }
```

---

## User Story 13: Accrual Balances and Policy Enforcement

### Overview
As an HR manager, I want to enforce leave accrual policies for compliance.

### Spring Boot Architecture
- Accrual logic in leave service.

### Package Structure
```
com.warehouse.ems.domain.leave.LeaveBalance
com.warehouse.ems.repository.leave.LeaveBalanceRepository
com.warehouse.ems.service.leave.AccrualService
```

### Domain Model Design
```java
@Entity
public class LeaveBalance {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @Enumerated(EnumType.STRING)
    private LeaveType type;
    private BigDecimal balance;
}
```

### Repository Layer
- Standard JPA repository.

### Service Layer
- Accrual calculation, policy enforcement.

### Controller Design
- Endpoint to view balances.

### Configuration & Security
- Employees see own balances, HR sees all.

### Integration Points
- Payroll system.

### Sample Implementation
```java
public void enforcePolicy(LeaveRequest request) {
    LeaveBalance balance = balanceRepository.findByEmployeeAndType(request.getEmployee(), request.getType());
    if (balance.getBalance().compareTo(request.getDays()) < 0) {
        throw new InsufficientBalanceException();
    }
}
```

---

## User Story 14: Certification Tracking and Alerts

### Overview
As a training coordinator, I want to track certifications and send expiry alerts.

### Spring Boot Architecture
- Certification module with scheduled alerts.

### Package Structure
```
com.warehouse.ems.domain.certification.Certification
com.warehouse.ems.repository.certification.CertificationRepository
com.warehouse.ems.service.certification.CertificationService
com.warehouse.ems.controller.certification.CertificationController
com.warehouse.ems.scheduler.CertificationExpiryScheduler
```

### Domain Model Design
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String certType; // e.g., "FORKLIFT"
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String status; // ACTIVE, EXPIRED
}
```

### Repository Layer
```java
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByExpiryDateBetween(LocalDate start, LocalDate end);
}
```

### Service Layer
- CRUD, expiry check.

### Controller Design
- Endpoints for CRUD.

### Configuration & Security
- Only training coordinators/HR.

### Integration Points
- Notification service.

### Sample Implementation
```java
@Scheduled(cron = "0 0 8 * * ?")
public void checkExpiry() {
    List<Certification> expiring = certRepo.findByExpiryDateBetween(LocalDate.now(), LocalDate.now().plusDays(30));
    expiring.forEach(cert -> notificationService.sendExpiryAlert(cert));
}
```

---

## User Story 15: Block Assignment for Expired Certifications

### Overview
As a scheduler, I want to block assignment of tasks to employees with expired certifications.

### Spring Boot Architecture
- Validation logic in scheduling service.

### Package Structure
- No new packages.

### Domain Model Design
- No changes.

### Repository Layer
- No changes.

### Service Layer
- Check certification status before assignment.

### Controller Design
- No changes.

### Configuration & Security
- No changes.

### Integration Points
- Certification service.

### Sample Implementation
```java
public void assignShift(Long employeeId, Long shiftId) {
    Employee emp = employeeRepo.findById(employeeId).orElseThrow();
    if (certificationService.hasExpiredCerts(emp)) {
        throw new CertificationExpiredException();
    }
    // proceed with assignment
}
```

---

## User Story 16: Safety Incident Recording

### Overview
As a safety officer, I want to record incidents and near-misses.

### Spring Boot Architecture
- Safety module with incident tracking.

### Package Structure
```
com.warehouse.ems.domain.safety.SafetyIncident
com.warehouse.ems.repository.safety.SafetyIncidentRepository
com.warehouse.ems.service.safety.SafetyIncidentService
com.warehouse.ems.controller.safety.SafetyIncidentController
```

### Domain Model Design
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private LocalDateTime incidentDate;
    private String location;
    private String description;
    @Enumerated(EnumType.STRING)
    private Severity severity; // MINOR, MAJOR, CRITICAL
    @Enumerated(EnumType.STRING)
    private IncidentStatus status; // OPEN, INVESTIGATING, RESOLVED
    @ManyToMany
    private Set<Employee> involvedEmployees;
}
```

### Repository Layer
- Standard JPA repository.

### Service Layer
- CRUD, status workflow.

### Controller Design
- Endpoints for create, update, list.

### Configuration & Security
- Only safety officers/managers.

### Integration Points
- OSHA reporting.

### Sample Implementation
```java
@PostMapping("/safety/incidents")
public ResponseEntity<SafetyIncidentResponseDto> create(@Valid @RequestBody SafetyIncidentRequestDto dto) { ... }
```

---

## User Story 17: OSHA Summary and Metrics Dashboard

### Overview
As a compliance manager, I want to generate OSHA summaries and view safety metrics.

### Spring Boot Architecture
- Reporting service for OSHA.

### Package Structure
```
com.warehouse.ems.service.safety.OshaReportingService
com.warehouse.ems.controller.safety.OshaReportingController
```

### Domain Model Design
- No new entities.

### Repository Layer
- Custom queries for OSHA data.

### Service Layer
- Generate OSHA 300/300A reports.

### Controller Design
- Endpoint for export.

### Configuration & Security
- Only compliance managers.

### Integration Points
- None.

### Sample Implementation
```java
@GetMapping("/safety/osha/summary")
public ResponseEntity<byte[]> exportOshaSummary(@RequestParam int year) {
    byte[] csv = oshaService.generateSummary(year);
    return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=osha_summary.csv").body(csv);
}
```

---

## User Story 18: Equipment & Asset Assignment

### Overview
As an asset manager, I want to assign equipment and PPE to employees.

### Spring Boot Architecture
- Asset management module.

### Package Structure
```
com.warehouse.ems.domain.asset.Asset
com.warehouse.ems.domain.asset.AssetAssignment
com.warehouse.ems.repository.asset.AssetRepository
com.warehouse.ems.repository.asset.AssetAssignmentRepository
com.warehouse.ems.service.asset.AssetService
com.warehouse.ems.controller.asset.AssetController
```

### Domain Model Design
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String assetType; // SCANNER, FORKLIFT, PPE
    private String assetId;
    private String condition; // GOOD, FAIR, POOR
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
```

### Repository Layer
- Standard JPA repositories.

### Service Layer
- Assign, return, check certification.

### Controller Design
- Endpoints for assign, return, list.

### Configuration & Security
- Only asset managers.

### Integration Points
- Certification service.

### Sample Implementation
```java
@PostMapping("/assets/assign")
public ResponseEntity<AssetAssignmentResponseDto> assign(@Valid @RequestBody AssetAssignmentRequestDto dto) {
    if (certificationService.hasExpiredCerts(dto.getEmployeeId())) {
        throw new CertificationExpiredException();
    }
    // proceed
}
```

---

## User Story 19: Asset Condition and Overdue Return Reporting

### Overview
As an asset manager, I want to track asset condition and report overdue returns.

### Spring Boot Architecture
- Reporting logic in asset service.

### Package Structure
- No new packages.

### Domain Model Design
- No changes.

### Repository Layer
- Custom queries for overdue assets.

### Service Layer
- Update condition, generate overdue report.

### Controller Design
- Endpoint for overdue report.

### Configuration & Security
- Only asset managers.

### Integration Points
- None.

### Sample Implementation
```java
@GetMapping("/assets/overdue")
public List<AssetAssignmentResponseDto> getOverdueAssets() {
    return assetService.findOverdueAssignments();
}
```

---

## User Story 20: Performance Review Cycle Creation

### Overview
As an HR manager, I want to create quarterly and annual review cycles.

### Spring Boot Architecture
- Performance review module.

### Package Structure
```
com.warehouse.ems.domain.performance.PerformanceReview
com.warehouse.ems.repository.performance.PerformanceReviewRepository
com.warehouse.ems.service.performance.PerformanceReviewService
com.warehouse.ems.controller.performance.PerformanceReviewController
```

### Domain Model Design
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String reviewCycle; // Q1, Q2, ANNUAL
    private LocalDate reviewDate;
    private String comments;
    private Integer rating;
    @Enumerated(EnumType.STRING)
    private ReviewStatus status; // DRAFT, SUBMITTED, ACKNOWLEDGED
}
```

### Repository Layer
- Standard JPA repository.

### Service Layer
- Create cycle, submit, acknowledge.

### Controller Design
- Endpoints for CRUD, submit, acknowledge.

### Configuration & Security
- Only HR/supervisors.

### Integration Points
- PDF export service.

### Sample Implementation
```java
@PostMapping("/performance/reviews")
public ResponseEntity<PerformanceReviewResponseDto> create(@Valid @RequestBody PerformanceReviewRequestDto dto) { ... }
```

---

## User Story 21: Payroll Export Generation

### Overview
As a payroll administrator, I want to generate payroll-ready files.

### Spring Boot Architecture
- Payroll export service.

### Package Structure
```
com.warehouse.ems.service.payroll.PayrollExportService
com.warehouse.ems.controller.payroll.PayrollExportController
```

### Domain Model Design
- No new entities.

### Repository Layer
- Custom queries for attendance and leave.

### Service Layer
- Generate export file, secure delivery.

### Controller Design
- Endpoint for export.

### Configuration & Security
- Only payroll admins.

### Integration Points
- SFTP/API for payroll provider.

### Sample Implementation
```java
@GetMapping("/payroll/export")
public ResponseEntity<byte[]> exportPayroll(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
    byte[] csv = payrollService.generateExport(startDate, endDate);
    return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=payroll.csv").body(csv);
}
```

---

## User Story 22: Notifications & Announcements Delivery

### Overview
As a communications manager, I want to send notifications and announcements.

### Spring Boot Architecture
- Notification service with multi-channel support.

### Package Structure
```
com.warehouse.ems.service.notification.NotificationService
com.warehouse.ems.controller.notification.NotificationController
```

### Domain Model Design
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee recipient;
    private String message;
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel; // IN_APP, EMAIL, SMS
    private LocalDateTime sentAt;
}
```

### Repository Layer
- Standard JPA repository.

### Service Layer
- Send notification, respect quiet hours, opt-in/out.

### Controller Design
- Endpoint for announcements.

### Configuration & Security
- Only managers/admins.

### Integration Points
- Email/SMS gateway.

### Sample Implementation
```java
public void sendNotification(Employee emp, String message, NotificationChannel channel) {
    if (isQuietHours()) {
        // defer
    } else {
        // send
    }
}
```

---

## User Story 23: Integration Layer API Exposure

### Overview
As an integration engineer, I want to expose REST APIs and connectors for master data synchronization.

### Spring Boot Architecture
- Integration layer with external APIs.

### Package Structure
```
com.warehouse.ems.integration.hris.HrisIntegrationService
com.warehouse.ems.integration.wms.WmsIntegrationService
com.warehouse.ems.controller.integration.IntegrationController
```

### Domain Model Design
- No new entities.

### Repository Layer
- No changes.

### Service Layer
- Sync logic for HRIS/WMS.

### Controller Design
- Webhooks for external events.

### Configuration & Security
- JWT/OAuth2 secured.

### Integration Points
- HRIS, WMS, IDP.

### Sample Implementation
```java
@PostMapping("/integration/hris/sync")
public ResponseEntity<Void> syncHris(@RequestBody HrisSyncDto dto) {
    hrisService.syncEmployees(dto);
    return ResponseEntity.ok().build();
}
```

---

## User Story 24: Audit Trail Logging

### Overview
As a compliance auditor, I want to log all sensitive changes.

### Spring Boot Architecture
- Audit logging with JPA Auditing or custom interceptor.

### Package Structure
```
com.warehouse.ems.domain.audit.AuditLog
com.warehouse.ems.repository.audit.AuditLogRepository
com.warehouse.ems.service.audit.AuditService
```

### Domain Model Design
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String actor;
    private String action; // CREATE, UPDATE, DELETE
    private String entityType;
    private Long entityId;
    private String beforeState;
    private String afterState;
    private LocalDateTime timestamp;
}
```

### Repository Layer
- Standard JPA repository.

### Service Layer
- Log changes, export logs.

### Controller Design
- Endpoint for export.

### Configuration & Security
- Only auditors.

### Integration Points
- None.

### Sample Implementation
```java
@Aspect
@Component
public class AuditAspect {
    @AfterReturning(pointcut = "@annotation(Auditable)", returning = "result")
    public void logAudit(JoinPoint joinPoint, Object result) {
        // log to AuditLog
    }
}
```

---

## User Story 25: Reporting & Analytics Dashboard

### Overview
As a business analyst, I want to view operational reports and analytics.

### Spring Boot Architecture
- Reporting service with aggregation queries.

### Package Structure
```
com.warehouse.ems.service.reporting.ReportingService
com.warehouse.ems.controller.reporting.ReportingController
```

### Domain Model Design
- No new entities.

### Repository Layer
- Custom queries for reports.

### Service Layer
- Generate reports, export CSV/PDF.

### Controller Design
- Endpoints for various reports.

### Configuration & Security
- Role-based access.

### Integration Points
- BI tools.

### Sample Implementation
```java
@GetMapping("/reports/attendance")
public ResponseEntity<byte[]> attendanceReport(@RequestParam LocalDate start, @RequestParam LocalDate end) {
    byte[] csv = reportingService.generateAttendanceReport(start, end);
    return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=attendance.csv").body(csv);
}
```

---

## User Story 26: Mobile Access via PWA

### Overview
As a warehouse worker, I want to access core flows on mobile devices.

### Spring Boot Architecture
- PWA with responsive frontend.

### Package Structure
- No backend changes.

### Domain Model Design
- No changes.

### Repository Layer
- No changes.

### Service Layer
- No changes.

### Controller Design
- No changes.

### Configuration & Security
- No changes.

### Integration Points
- Frontend PWA.

### Sample Implementation
- Frontend: manifest.json, service worker for offline support.

---

## User Story 27: Onboarding Workflow Automation

### Overview
As an HR manager, I want to automate onboarding tasks.

### Spring Boot Architecture
- Workflow engine (e.g., Camunda) or custom state machine.

### Package Structure
```
com.warehouse.ems.service.workflow.OnboardingWorkflowService
com.warehouse.ems.controller.workflow.OnboardingController
```

### Domain Model Design
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String taskType; // TRAINING, ASSET_ASSIGNMENT
    @Enumerated(EnumType.STRING)
    private TaskStatus status; // PENDING, COMPLETED
}
```

### Repository Layer
- Standard JPA repository.

### Service Layer
- Trigger onboarding, complete tasks.

### Controller Design
- Endpoint to start onboarding.

### Configuration & Security
- Only HR.

### Integration Points
- HRIS, training, asset services.

### Sample Implementation
```java
@PostMapping("/onboarding/start")
public ResponseEntity<Void> startOnboarding(@RequestParam Long employeeId) {
    onboardingService.initiateOnboarding(employeeId);
    return ResponseEntity.ok().build();
}
```

---

## User Story 28: Offboarding Workflow Automation

### Overview
As an HR manager, I want to automate offboarding tasks.

### Spring Boot Architecture
- Similar to onboarding workflow.

### Package Structure
```
com.warehouse.ems.service.workflow.OffboardingWorkflowService
com.warehouse.ems.controller.workflow.OffboardingController
```

### Domain Model Design
```java
@Entity
public class OffboardingTask {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String taskType; // ACCESS_REVOCATION, ASSET_COLLECTION
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
}
```

### Repository Layer
- Standard JPA repository.

### Service Layer
- Trigger offboarding, complete tasks.

### Controller Design
- Endpoint to start offboarding.

### Configuration & Security
- Only HR.

### Integration Points
- Asset, scheduling, security services.

### Sample Implementation
```java
@PostMapping("/offboarding/start")
public ResponseEntity<Void> startOffboarding(@RequestParam Long employeeId) {
    offboardingService.initiateOffboarding(employeeId);
    return ResponseEntity.ok().build();
}
```

---

## User Story 29: Localization & Multi-Warehouse Support

### Overview
As a global admin, I want to support multiple warehouses and languages.

### Spring Boot Architecture
- Multi-tenancy and i18n support.

### Package Structure
```
com.warehouse.ems.domain.warehouse.Warehouse
com.warehouse.ems.repository.warehouse.WarehouseRepository
com.warehouse.ems.config.LocalizationConfig
```

### Domain Model Design
```java
@Entity
public class Warehouse {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String location;
    private String timezone;
}
```

### Repository Layer
- Standard JPA repository.

### Service Layer
- Filter by warehouse, localize messages.

### Controller Design
- Accept warehouse context in requests.

### Configuration & Security
- Row-level security by warehouse.

### Integration Points
- None.

### Sample Implementation
```java
@Configuration
public class LocalizationConfig {
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding("UTF-8");
        return source;
    }
}
```

---

## User Story 30: Observability & Monitoring Setup

### Overview
As a DevOps engineer, I want to enable structured logging, tracing, and metrics.

### Spring Boot Architecture
- Logging, tracing (Sleuth/Zipkin), metrics (Micrometer).

### Package Structure
- No new packages.

### Domain Model Design
- No changes.

### Repository Layer
- No changes.

### Service Layer
- No changes.

### Controller Design
- No changes.

### Configuration & Security
```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  level:
    com.warehouse.ems: DEBUG

management:
  endpoints:
    web:
      exposure:
        include: prometheus,health,info
  metrics:
    export:
      prometheus:
        enabled: true
```

### Integration Points
- Prometheus, Grafana, Zipkin.

### Sample Implementation
- Add dependencies for Micrometer, Sleuth.

---

## User Story 31: Deployment & CI/CD Pipeline

### Overview
As a release engineer, I want to containerize the app and automate deployments.

### Spring Boot Architecture
- Dockerized Spring Boot app.

### Package Structure
- No changes.

### Domain Model Design
- No changes.

### Repository Layer
- No changes.

### Service Layer
- No changes.

### Controller Design
- No changes.

### Configuration & Security
- Externalized config via environment variables.

### Integration Points
- GitHub Actions, Kubernetes.

### Sample Implementation
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/warehouse-ems.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```
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
      - name: Build Docker image
        run: docker build -t warehouse-ems .
      - name: Deploy to Kubernetes
        run: kubectl apply -f k8s/
```

---

## Conclusion

This comprehensive low-level technical design document covers all 31 user stories for the Warehouse Employee Management System. Each section provides detailed Spring Boot architecture, package structure, domain models, repository/service/controller layers, configuration, security, integration points, and sample implementations. The design follows Spring Boot 3.x best practices with Java 17+ and is production-ready for agile development teams.