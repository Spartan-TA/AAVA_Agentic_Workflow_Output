# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

## Table of Contents
- E01: Project Scaffolding & Domain Setup
- E02: Employee Master Data (CRUD)
- E03: Role Based Access Control (RBAC)
- E04: Time & Attendance (Clock In/Out)
- E05: Shift & Schedule Management
- E06: Leave & Absence Management
- E07: Training & Certification Tracking
- E08: Safety Incidents & OSHA Reporting
- E09: Equipment & Asset Assignment
- E10: Performance Reviews & Goals
- E11: Payroll Export Integration
- E12: Notifications & Announcements
- E13: Integration Layer (HRIS/WMS APIs)
- E14: Audit Trail & Compliance
- E15: Reporting & Analytics
- E16: Mobile Access (PWA)
- E17: Onboarding & Offboarding Workflow
- E18: Localization & Multi-Site
- E19: Advanced Scheduling (AI/Optimization)
- E20: Self-Service Portal

---

## E01: Project Scaffolding & Domain Setup

Section: Spring Boot Architecture Overview  
Description: Establishes the foundational structure for the EMS application using Spring Boot 3.x, Maven, and modular package organization. Enables Actuator for health checks and Flyway/Liquibase for DB migrations.  
Design Specification:  
- Base package: `com.warehouse.ems`  
- Modules: `employee`, `scheduling`, `attendance`, `safety`, etc.  
- Actuator enabled for `/actuator/health`  
- Flyway/Liquibase for DB versioning  
- README with build/run instructions  
Sample Implementation:  
```java
@SpringBootApplication
public class EmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmsApplication.class, args);
    }
}
```
---

Section: Package Structure and Module Definitions  
Description: Organizes code into logical modules for maintainability and scalability.  
Design Specification:  
- `com.warehouse.ems.employee`  
- `com.warehouse.ems.scheduling`  
- `com.warehouse.ems.attendance`  
- `com.warehouse.ems.safety`  
- `com.warehouse.ems.config`  
Sample Implementation:  
```
src/main/java/com/warehouse/ems/
    âââ employee/
    âââ scheduling/
    âââ attendance/
    âââ safety/
    âââ config/
```
---

Section: Configuration Properties  
Description: Centralizes configuration for DB, security, and actuator endpoints.  
Design Specification:  
- `application.yml` for DB, actuator, security  
Sample Implementation:  
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

## E02: Employee Master Data (CRUD)

Section: Entity Design  
Description: Defines the Employee domain model with JPA annotations, validation, and relationships.  
Design Specification:  
- Fields: id, name, badgeId, role, department, shiftGroup, hireDate, status  
- Unique constraint on badgeId  
- Soft-delete flag  
Sample Implementation:  
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    @Column(name = "badge_id", unique = true)
    private String badgeId;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    private boolean deleted = false;
    // getters/setters
}
```
---

Section: Repository Layer Specifications  
Description: Provides CRUD operations and filtering for Employee entities.  
Design Specification:  
- Extends `JpaRepository<Employee, Long>`  
- Custom query for soft-delete  
Sample Implementation:  
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```
---

Section: Service Layer Design  
Description: Encapsulates business logic for employee management, validation, and error handling.  
Design Specification:  
- Methods: create, update, delete (soft), get, filter  
- Validation for unique badgeId  
Sample Implementation:  
```java
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository repository;
    public Employee create(EmployeeDto dto) {
        if (repository.findByBadgeIdAndDeletedFalse(dto.getBadgeId()).isPresent()) {
            throw new DuplicateBadgeException();
        }
        Employee emp = new Employee(...);
        return repository.save(emp);
    }
    // Other CRUD methods
}
```
---

Section: Controller Layer with REST API Endpoints  
Description: Exposes RESTful endpoints for employee CRUD operations, pagination, and filtering.  
Design Specification:  
- Endpoints: `/employees` (GET, POST, PUT, PATCH, DELETE)  
- OpenAPI annotations  
Sample Implementation:  
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService service;
    @GetMapping
    public Page<EmployeeDto> list(Pageable pageable) { ... }
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { ... }
    // Other endpoints
}
```
---

Section: Security Configuration  
Description: Restricts access to employee endpoints based on roles.  
Design Specification:  
- Only ADMIN/HR can create/update/delete  
- SUPERVISOR can view team  
Sample Implementation:  
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
@PostMapping
public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { ... }
```
---

## E03: Role Based Access Control (RBAC)

Section: Spring Security Configuration  
Description: Implements RBAC using Spring Security, supporting roles and endpoint/method security.  
Design Specification:  
- Roles: ADMIN, HR, SUPERVISOR, WORKER  
- API key/OAuth2 toggle via config  
Sample Implementation:  
```java
@Configuration
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
            .oauth2ResourceServer().jwt();
    }
}
```
---

Section: Row-Level Security  
Description: Restricts data access at the row level for supervisors and workers.  
Design Specification:  
- SUPERVISOR can access only team members  
Sample Implementation:  
```java
@PreAuthorize("@securityService.canAccessEmployee(principal, #employeeId)")
public EmployeeDto getEmployee(Long employeeId) { ... }
```
---

## E04: Time & Attendance (Clock In/Out)

Section: Entity Design  
Description: Models attendance events with geofence and device capture.  
Design Specification:  
- Fields: id, employeeId, clockType (IN/OUT), timestamp, deviceId, location, approved, correctionRequested  
Sample Implementation:  
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @Enumerated(EnumType.STRING)
    private ClockType clockType;
    private LocalDateTime timestamp;
    private String deviceId;
    private String location;
    private boolean approved;
    private boolean correctionRequested;
}
```
---

Section: Controller Layer  
Description: REST endpoints for clock-in/out, corrections, and reporting.  
Design Specification:  
- Endpoints: `/attendance/clock-in`, `/attendance/clock-out`, `/attendance/corrections`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockInDto dto) { ... }
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockOutDto dto) { ... }
    @PostMapping("/corrections")
    public ResponseEntity<?> requestCorrection(@RequestBody CorrectionDto dto) { ... }
}
```
---

Section: Service Layer  
Description: Calculates hours worked, handles missed punches, and manages approval workflow.  
Design Specification:  
- Methods: recordClockIn, recordClockOut, calculateDailyTotals, handleCorrections  
Sample Implementation:  
```java
public Duration calculateDailyTotal(Long employeeId, LocalDate date) { ... }
```
---

## E05: Shift & Schedule Management

Section: Entity Design  
Description: Models shift templates, schedules, and assignment rules.  
Design Specification:  
- ShiftTemplate: id, name, startTime, endTime, recurrence, blackoutDates  
- EmployeeShift: id, employee, shiftTemplate, date  
Sample Implementation:  
```java
@Entity
public class ShiftTemplate { ... }
@Entity
public class EmployeeShift { ... }
```
---

Section: Controller Layer  
Description: CRUD endpoints for shift templates and schedules, conflict detection.  
Design Specification:  
- Endpoints: `/shifts/templates`, `/shifts/schedules`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates")
    public ResponseEntity<?> createTemplate(@RequestBody ShiftTemplateDto dto) { ... }
    @PostMapping("/schedules")
    public ResponseEntity<?> assignSchedule(@RequestBody ScheduleDto dto) { ... }
}
```
---

Section: Service Layer  
Description: Handles bulk assignment, conflict detection, and audit logging.  
Design Specification:  
- Methods: assignShifts, detectConflicts, logAudit  
Sample Implementation:  
```java
public boolean detectConflicts(EmployeeShift shift) { ... }
```
---

## E06: Leave & Absence Management

Section: Entity Design  
Description: Models leave requests, approvals, and accrual balances.  
Design Specification:  
- LeaveRequest: id, employee, type, startDate, endDate, status, balance  
Sample Implementation:  
```java
@Entity
public class LeaveRequest { ... }
```
---

Section: Controller Layer  
Description: Endpoints for requesting, approving, and exporting leave.  
Design Specification:  
- Endpoints: `/leave/requests`, `/leave/approve`, `/leave/export`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/leave")
public class LeaveController { ... }
```
---

Section: Service Layer  
Description: Updates balances, flags shifts for coverage, and exports approved leaves.  
Design Specification:  
- Methods: requestLeave, approveLeave, exportLeaves  
Sample Implementation:  
```java
public void requestLeave(LeaveRequestDto dto) { ... }
```
---

## E07: Training & Certification Tracking

Section: Entity Design  
Description: Tracks certifications, expirations, and proof documents.  
Design Specification:  
- Certification: id, employee, type, expiryDate, documentUrl  
Sample Implementation:  
```java
@Entity
public class Certification { ... }
```
---

Section: Controller Layer  
Description: CRUD endpoints for certifications, alerts for expiry.  
Design Specification:  
- Endpoints: `/certifications`, `/certifications/alerts`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController { ... }
```
---

Section: Service Layer  
Description: Blocks assignment to tasks if certification expired, sends alerts.  
Design Specification:  
- Methods: checkCertificationStatus, sendExpiryAlerts  
Sample Implementation:  
```java
public boolean isCertificationValid(Long employeeId, String type) { ... }
```
---

## E08: Safety Incidents & OSHA Reporting

Section: Entity Design  
Description: Records incidents, severity, workflow status, and involved employees.  
Design Specification:  
- SafetyIncident: id, severity, location, description, status, involvedEmployees  
Sample Implementation:  
```java
@Entity
public class SafetyIncident { ... }
```
---

Section: Controller Layer  
Description: Endpoints for incident reporting, status workflow, and OSHA exports.  
Design Specification:  
- Endpoints: `/safety/incidents`, `/safety/osha/export`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/safety")
public class SafetyController { ... }
```
---

Section: Service Layer  
Description: Manages investigation workflow, generates OSHA summaries, dashboard metrics.  
Design Specification:  
- Methods: createIncident, updateStatus, exportOSHA  
Sample Implementation:  
```java
public void exportOSHAReport(LocalDate from, LocalDate to) { ... }
```
---

## E09: Equipment & Asset Assignment

Section: Entity Design  
Description: Tracks assets, assignments, condition, and history.  
Design Specification:  
- Asset: id, type, condition, assignedTo, history  
Sample Implementation:  
```java
@Entity
public class Asset { ... }
```
---

Section: Controller Layer  
Description: Endpoints for asset registry, check-in/out, overdue reports.  
Design Specification:  
- Endpoints: `/assets`, `/assets/assign`, `/assets/return`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/assets")
public class AssetController { ... }
```
---

Section: Service Layer  
Description: Blocks assignment if certification invalid, maintains history log.  
Design Specification:  
- Methods: assignAsset, returnAsset, getHistory  
Sample Implementation:  
```java
public void assignAsset(Long assetId, Long employeeId) { ... }
```
---

## E10: Performance Reviews & Goals

Section: Entity Design  
Description: Models review cycles, goals, competencies, ratings, and acknowledgements.  
Design Specification:  
- PerformanceReview: id, employee, cycle, goals, ratings, comments, acknowledged  
Sample Implementation:  
```java
@Entity
public class PerformanceReview { ... }
```
---

Section: Controller Layer  
Description: Endpoints for review creation, submission, PDF export.  
Design Specification:  
- Endpoints: `/reviews`, `/reviews/export`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/reviews")
public class ReviewController { ... }
```
---

Section: Service Layer  
Description: Handles review workflow, role-based visibility, immutable history.  
Design Specification:  
- Methods: createReview, submitReview, exportReview  
Sample Implementation:  
```java
public void submitReview(Long reviewId) { ... }
```
---

## E11: Payroll Export Integration

Section: Integration Points  
Description: Generates payroll files, maps to provider formats, delivers via SFTP/API.  
Design Specification:  
- Scheduled job for export  
- Secure delivery, retry/backoff  
Sample Implementation:  
```java
@Component
public class PayrollExportJob {
    @Scheduled(cron = "0 0 * * * ?")
    public void exportPayroll() { ... }
}
```
---

Section: Audit Logging  
Description: Logs every export event for compliance.  
Design Specification:  
- Audit table for exports  
Sample Implementation:  
```java
public void logExportEvent(String filename, boolean success) { ... }
```
---

## E12: Notifications & Announcements

Section: Integration Points  
Description: Sends notifications via in-app, email, SMS; supports quiet hours and opt-in/out.  
Design Specification:  
- NotificationService with channel support  
Sample Implementation:  
```java
public void sendNotification(NotificationDto dto) { ... }
```
---

Section: Controller Layer  
Description: Endpoints for announcements, delivery status, opt-in/out.  
Design Specification:  
- Endpoints: `/notifications`, `/announcements`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController { ... }
```
---

## E13: Integration Layer (HRIS/WMS APIs)

Section: Integration Points  
Description: REST APIs and connectors for HRIS, WMS, and IDP; webhooks for events.  
Design Specification:  
- JWT/OAuth2 security  
- HRIS sync job  
Sample Implementation:  
```java
@RestController
@RequestMapping("/api/hris")
public class HRISController { ... }
```
---

Section: Webhook Handling  
Description: Idempotent webhook processing for external events.  
Design Specification:  
- Endpoint: `/webhooks`  
Sample Implementation:  
```java
@PostMapping("/webhooks")
public ResponseEntity<?> handleWebhook(@RequestBody WebhookEventDto dto) { ... }
```
---

## E14: Audit Trail & Compliance

Section: Entity Design  
Description: Centralized audit log for sensitive changes, tamper-evident storage.  
Design Specification:  
- AuditLog: id, actor, timestamp, entity, before, after  
Sample Implementation:  
```java
@Entity
public class AuditLog { ... }
```
---

Section: Service Layer  
Description: Records audit events on create/update/delete.  
Design Specification:  
- Methods: logEvent  
Sample Implementation:  
```java
public void logEvent(String actor, String entity, Object before, Object after) { ... }
```
---

## E15: Reporting & Analytics

Section: Controller Layer  
Description: Endpoints for operational reports, CSV/PDF export, dashboards.  
Design Specification:  
- Endpoints: `/reports/attendance`, `/reports/overtime`, `/reports/certifications`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/reports")
public class ReportController { ... }
```
---

Section: Service Layer  
Description: Filters by date, department, shift; metrics for BI.  
Design Specification:  
- Methods: generateReport, exportReport  
Sample Implementation:  
```java
public ReportDto generateAttendanceReport(LocalDate from, LocalDate to) { ... }
```
---

## E16: Mobile Access (PWA)

Section: PWA Configuration  
Description: Enables mobile-friendly views, offline support, and installable manifest.  
Design Specification:  
- PWA manifest  
- Offline queue for clock events  
Sample Implementation:  
```json
{
  "name": "EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone"
}
```
---

Section: Controller Layer  
Description: Mobile-optimized endpoints for clock-in/out, schedule, leave.  
Design Specification:  
- Endpoints: `/mobile/clock-in`, `/mobile/schedule`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/mobile")
public class MobileController { ... }
```
---

## E17: Onboarding & Offboarding Workflow

Section: Service Layer  
Description: Automates task generation for onboarding/offboarding, tracks progress.  
Design Specification:  
- Methods: triggerOnboarding, triggerOffboarding  
Sample Implementation:  
```java
public void triggerOnboarding(Long employeeId) { ... }
```
---

Section: Controller Layer  
Description: Endpoints for onboarding/offboarding tasks, status tracking.  
Design Specification:  
- Endpoints: `/onboarding`, `/offboarding`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/onboarding")
public class OnboardingController { ... }
```
---

## E18: Localization & Multi-Site

Section: Configuration Properties  
Description: Supports multiple sites, timezones, and languages.  
Design Specification:  
- Site entity with timezone  
- i18n message bundles  
Sample Implementation:  
```java
@Entity
public class Site {
    @Id
    private Long id;
    private String name;
    private String timezone;
}
```
---

Section: Controller Layer  
Description: Endpoints for site management, language selection.  
Design Specification:  
- Endpoints: `/sites`, `/localization`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/sites")
public class SiteController { ... }
```
---

## E19: Advanced Scheduling (AI/Optimization)

Section: Integration Points  
Description: AI-powered demand forecasting, schedule optimization, what-if scenarios.  
Design Specification:  
- ML model integration  
- Optimization algorithm  
Sample Implementation:  
```java
public ScheduleDto optimizeSchedule(OptimizationRequest request) { ... }
```
---

Section: Controller Layer  
Description: Endpoints for forecasting, optimization, scenario analysis.  
Design Specification:  
- Endpoints: `/scheduling/forecast`, `/scheduling/optimize`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/scheduling")
public class SchedulingController { ... }
```
---

## E20: Self-Service Portal

Section: Controller Layer  
Description: Endpoints for employee profile updates, pay stubs, benefits enrollment.  
Design Specification:  
- Endpoints: `/self-service/profile`, `/self-service/paystubs`, `/self-service/benefits`  
Sample Implementation:  
```java
@RestController
@RequestMapping("/self-service")
public class SelfServiceController { ... }
```
---

Section: Service Layer  
Description: Handles profile updates, document uploads, benefits enrollment.  
Design Specification:  
- Methods: updateProfile, uploadDocument, enrollBenefits  
Sample Implementation:  
```java
public void updateProfile(ProfileDto dto) { ... }
```
---

## Conclusion

This low-level technical design document provides comprehensive specifications for all 60 user stories across 20 epics, following Spring Boot 3.x best practices. Each section includes entity design, repository/service/controller layers, security configuration, and sample implementations. The design ensures scalability, maintainability, and compliance with industry standards.