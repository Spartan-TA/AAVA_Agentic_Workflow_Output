# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

## USER STORY 1: Initialize Project Scaffolding

Section: Spring Boot Architecture Overview  
Description: Establishes the foundational structure for the EMS application, ensuring modularity and maintainability. Uses Maven for build management, Flyway/Liquibase for DB migrations, and Spring Boot Actuator for health monitoring.  
Design Specification:
- Main application class in `com.warehouse.ems`
- Standardized base packages: domain, service, repository, controller, dto, config, security
- Flyway/Liquibase migration scripts in `src/main/resources/db/migration`
- Actuator enabled in `application.yml`
Sample Implementation:
```java
@SpringBootApplication
public class EmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmsApplication.class, args);
    }
}
```
```yaml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ems
    username: ems_user
    password: secret
  flyway:
    enabled: true
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

---

## USER STORY 2: Create Employee Domain Model

Section: Domain Model  
Description: Defines the Employee entity and CRUD operations, enforcing unique badgeId and supporting soft-delete, pagination, and filtering.  
Design Specification:
- Entity: `Employee` with fields: id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted
- Repository: `EmployeeRepository` extends JpaRepository
- Service: `EmployeeService` for business logic
- Controller: `EmployeeController` exposes REST endpoints
- DTOs for requests/responses
Sample Implementation:
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
    private String status;
    private boolean deleted = false;
}
```
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) {...}
    @GetMapping
    public Page<EmployeeDto> list(Pageable pageable, @RequestParam Map<String, String> filters) {...}
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) {...}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {...}
}
```

---

## USER STORY 3: Enforce Role-Based Access Control

Section: Security Settings  
Description: Implements RBAC using Spring Security, restricting endpoint access based on user roles (ADMIN, SUPERVISOR, HR, WORKER).  
Design Specification:
- Security config in `com.warehouse.ems.security`
- Method and endpoint security via annotations
- Custom access denied/unauthorized handlers
- Security tests for rules
Sample Implementation:
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR")
                .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            .and()
                .exceptionHandling()
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                    .accessDeniedHandler(new AccessDeniedHandlerImpl());
    }
}
```
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == authentication.principal.department)")
public Employee updateEmployee(Employee employee) {...}
```

---

## USER STORY 4: Employee Clock In/Out

Section: Attendance Domain & Controller  
Description: Enables employees to clock in/out, records attendance, associates with shifts, and supports missed punch correction and CSV export.  
Design Specification:
- Entity: `AttendanceRecord` with employee, clockIn, clockOut, shift, correctionRequested
- Controller: `/attendance/clock-in`, `/attendance/clock-out`, `/attendance/export`
- Service: `AttendanceService` for business logic
Sample Implementation:
```java
@Entity
public class AttendanceRecord {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    @ManyToOne
    private Shift shift;
    private boolean correctionRequested;
}
```
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<Void> clockIn(@RequestBody ClockInDto dto) {...}
    @PostMapping("/clock-out")
    public ResponseEntity<Void> clockOut(@RequestBody ClockOutDto dto) {...}
    @GetMapping("/export")
    public ResponseEntity<Resource> exportCsv(@RequestParam Map<String, String> filters) {...}
}
```

---

## USER STORY 5: Manage Shift Templates

Section: Shift Template Domain & Scheduling  
Description: Allows creation and assignment of recurring shift templates, conflict detection, and audit logging.  
Design Specification:
- Entity: `ShiftTemplate` with recurrence, start/end, assignedEmployees
- Service: `ShiftService` for assignment and conflict detection
- Audit logging for changes
Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime start;
    private LocalTime end;
    private String recurrenceRule;
    @ManyToMany
    private List<Employee> assignedEmployees;
}
```
```java
@Service
public class ShiftService {
    @Transactional
    public void assignShift(Long templateId, List<Long> employeeIds) {...}
    public boolean hasConflict(Employee employee, LocalDateTime start, LocalDateTime end) {...}
}
```

---

## USER STORY 6: Request and Approve Leave

Section: Leave Management  
Description: Supports leave requests, supervisor approval workflow, balance updates, and shift coverage flagging.  
Design Specification:
- Entity: `LeaveRequest` with employee, type, start/end, status, approvalTask
- Service: `LeaveService` for request/approval logic
- Controller: `/leave/request`, `/leave/approve`
Sample Implementation:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @Enumerated(EnumType.STRING)
    private LeaveType type;
    private LocalDate start;
    private LocalDate end;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;
    @OneToOne
    private ApprovalTask approvalTask;
}
```
```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/request")
    public ResponseEntity<Void> requestLeave(@RequestBody LeaveRequestDto dto) {...}
    @PostMapping("/approve/{id}")
    public ResponseEntity<Void> approveLeave(@PathVariable Long id) {...}
}
```

---

## USER STORY 7: Track Training & Certifications

Section: Certification Tracking  
Description: Tracks employee certifications, expirations, and blocks assignment to equipment if expired.  
Design Specification:
- Entity: `Certification` with employee, type, expiryDate, documentUrl
- Alerts for expiry (scheduled job)
- Assignment checks in service layer
Sample Implementation:
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
```
```java
@Service
public class CertificationService {
    public void checkExpiryAlerts() {...}
    public boolean isQualified(Employee employee, String equipmentType) {...}
}
```

---

## USER STORY 8: Record Safety Incidents

Section: Safety Incident Domain  
Description: Records safety incidents, supports investigation workflow, and OSHA export.  
Design Specification:
- Entity: `SafetyIncident` with severity, location, description, involvedEmployees, status
- Controller: `/safety/incidents`
- Workflow for status progression
Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String severity;
    private String location;
    private String description;
    @ManyToMany
    private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
}
```
```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
    @PostMapping
    public ResponseEntity<Void> recordIncident(@RequestBody SafetyIncidentDto dto) {...}
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody StatusDto dto) {...}
}
```

---

## USER STORY 9: Assign Equipment & Assets

Section: Asset Assignment  
Description: Assigns equipment/assets to employees, tracks check-in/out, and enforces certification checks.  
Design Specification:
- Entity: `Asset` with assignedEmployee, status, history
- Service: `AssetService` for assignment logic
- Controller: `/assets/assign`, `/assets/check-in`, `/assets/check-out`
Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    @ManyToOne
    private Employee assignedEmployee;
    private AssetStatus status;
    @OneToMany(mappedBy = "asset")
    private List<AssetHistory> history;
}
```
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign")
    public ResponseEntity<Void> assignAsset(@RequestBody AssetAssignmentDto dto) {...}
    @PostMapping("/check-in")
    public ResponseEntity<Void> checkIn(@RequestBody AssetCheckInDto dto) {...}
    @PostMapping("/check-out")
    public ResponseEntity<Void> checkOut(@RequestBody AssetCheckOutDto dto) {...}
}
```

---

## USER STORY 10: Conduct Performance Reviews

Section: Performance Review Domain  
Description: Manages review cycles, tracks goals, supports sign-off workflow, and PDF export.  
Design Specification:
- Entity: `PerformanceReview` with employee, supervisor, cycle, goals, ratings, comments, signedOff
- Service: `PerformanceReviewService`
- Controller: `/reviews`
Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private Employee supervisor;
    private String cycle;
    @ElementCollection
    private List<Goal> goals;
    private String comments;
    private boolean signedOff;
}
```
```java
@RestController
@RequestMapping("/reviews")
public class PerformanceReviewController {
    @PostMapping
    public ResponseEntity<Void> createReview(@RequestBody PerformanceReviewDto dto) {...}
    @PostMapping("/{id}/sign-off")
    public ResponseEntity<Void> signOff(@PathVariable Long id) {...}
    @GetMapping("/export/{id}")
    public ResponseEntity<Resource> exportPdf(@PathVariable Long id) {...}
}
```

---

## USER STORY 11: Export Payroll Data

Section: Payroll Export Integration  
Description: Exports attendance and leave data in provider formats, supports retries and audit logging.  
Design Specification:
- Service: `PayrollExportService` for data mapping and delivery
- Audit log entity for export events
- Controller: `/payroll/export`
Sample Implementation:
```java
@Service
public class PayrollExportService {
    public void exportPayrollData(PayrollExportRequest request) {...}
    @Scheduled(fixedDelay = 60000)
    public void retryFailedExports() {...}
}
```
```java
@Entity
public class PayrollExportAudit {
    @Id @GeneratedValue
    private Long id;
    private LocalDateTime timestamp;
    private String eventType;
    private String status;
    private String details;
}
```

---

## USER STORY 12: Send Notifications & Announcements

Section: Notification System  
Description: Sends notifications/announcements via preferred channels, tracks delivery, supports rate limits.  
Design Specification:
- Entity: `NotificationPreference`, `Announcement`
- Service: `NotificationService` for delivery logic
- Controller: `/notifications`, `/announcements`
Sample Implementation:
```java
@Entity
public class NotificationPreference {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean inAppEnabled;
}
```
```java
@Service
public class NotificationService {
    public void sendNotification(NotificationDto dto) {...}
    public void trackDelivery(Long notificationId, DeliveryStatus status) {...}
}
```

---

## USER STORY 13: Integrate HRIS & WMS APIs

Section: Integration Layer  
Description: Exposes REST APIs and connectors for HRIS/WMS, supports JWT/OAuth2 security, idempotent webhooks.  
Design Specification:
- API endpoints in `com.warehouse.ems.integration`
- Security config for JWT/OAuth2
- Webhook event handling
Sample Implementation:
```java
@RestController
@RequestMapping("/api/hris")
public class HRISController {
    @PostMapping("/sync")
    public ResponseEntity<Void> syncEmployees(@RequestBody HRISSyncDto dto) {...}
}
```
```java
@Configuration
public class JwtSecurityConfig {
    // JWT/OAuth2 configuration beans
}
```

---

## USER STORY 14: Centralized Audit Trail

Section: Audit Trail  
Description: Logs sensitive changes with actor, timestamp, before/after, and supports immutable storage and export.  
Design Specification:
- Entity: `AuditLog` with actor, timestamp, entity, before, after
- Service: `AuditService` for logging
- Controller: `/audit/export`
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
}
```
```java
@Service
public class AuditService {
    public void logChange(String actor, String entity, Object before, Object after) {...}
}
```

---

## USER STORY 15: Generate Reports & Analytics

Section: Reporting & Analytics  
Description: Generates reports on attendance, overtime, leave, certifications, safety, supports filtering and export.  
Design Specification:
- Service: `ReportService` for report generation
- Controller: `/reports`
- CSV/PDF export logic
Sample Implementation:
```java
@Service
public class ReportService {
    public Report generateAttendanceReport(ReportFilter filter) {...}
    public Resource exportReport(Long reportId, ExportFormat format) {...}
}
```
```java
@RestController
@RequestMapping("/reports")
public class ReportController {
    @GetMapping
    public ResponseEntity<Report> getReport(@RequestParam Map<String, String> filters) {...}
    @GetMapping("/export/{id}")
    public ResponseEntity<Resource> export(@PathVariable Long id, @RequestParam ExportFormat format) {...}
}
```

---

## USER STORY 16: Mobile Access via PWA

Section: PWA & Mobile Access  
Description: Provides responsive views, offline queue, and installable PWA manifest for mobile access.  
Design Specification:
- Frontend: PWA manifest, service worker
- Backend: Offline queue endpoints
- Controller: `/mobile`
Sample Implementation:
```json
// manifest.json (frontend)
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "icons": [{ "src": "/icon.png", "sizes": "192x192", "type": "image/png" }]
}
```
```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @PostMapping("/offline-queue")
    public ResponseEntity<Void> syncOfflineQueue(@RequestBody List<ClockEventDto> events) {...}
}
```

---

## USER STORY 17: Automate Onboarding & Offboarding

Section: Onboarding & Offboarding Workflow  
Description: Automates provisioning of accounts, schedules, training, and asset collection/revocation.  
Design Specification:
- Service: `OnboardingService`, `OffboardingService`
- Integration with HRIS for new hires
- Asset and schedule update logic
Sample Implementation:
```java
@Service
public class OnboardingService {
    public void onboardNewHire(HRISNewHireDto dto) {...}
}
@Service
public class OffboardingService {
    public void offboardEmployee(Long employeeId) {...}
}
```

---

## USER STORY 18: Localization & Multi-Warehouse Support

Section: Localization & Multi-Warehouse  
Description: Supports localization of UI/notifications and management of multiple warehouses.  
Design Specification:
- Entity: `Warehouse`, `EmployeeWarehouseScope`
- Localization config in `application.yml`
- Controller: `/warehouses`
Sample Implementation:
```java
@Entity
public class Warehouse {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String location;
    private String language;
}
```
```yaml
spring:
  messages:
    basename: messages
```
```java
@RestController
@RequestMapping("/warehouses")
public class WarehouseController {
    @GetMapping
    public List<WarehouseDto> listWarehouses() {...}
}
```

---

## USER STORY 19: Advanced AI-Assisted Scheduling

Section: AI-Assisted Scheduling  
Description: Uses AI to optimize shift assignments, flag overtime risks, and log manual overrides.  
Design Specification:
- Service: `AISchedulingService`
- Integration with ML model (external API or embedded)
- Audit log for overrides
Sample Implementation:
```java
@Service
public class AISchedulingService {
    public SchedulePlan generateOptimalSchedule(ScheduleConstraints constraints) {...}
    public List<OvertimeRisk> predictOvertimeRisks() {...}
}
```

---

## USER STORY 20: Continuous Deployment & Monitoring

Section: CI/CD & Monitoring  
Description: Enables CI/CD pipeline for automated deployment and monitoring with health checks and alerting.  
Design Specification:
- CI/CD: GitHub Actions or Jenkins pipeline
- Monitoring: Prometheus/Grafana or ELK
- Rollback procedure
Sample Implementation:
```yaml
# .github/workflows/deploy.yml
name: Deploy
on:
  push:
    branches: [main]
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build
        run: mvn clean package
      - name: Deploy
        run: ./deploy.sh
```
```yaml
# application.yml (monitoring)
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
```

---

## CONCLUSION

This document provides comprehensive low-level technical designs for all 20 user stories of the Warehouse EMS, following Spring Boot best practices and industry standards. Each section includes architecture overview, package structure, entity design, service/repository/controller specifications, configuration, security settings, integration points, and code snippets.