# Warehouse Employee Management System (EMS) - Comprehensive Low-Level Technical Design Document

This document provides a detailed technical blueprint for the Warehouse EMS, covering all 40+ user stories across 17 epics. It is structured for easy consumption by Spring Boot developers and adheres to industry best practices.

---

## Table of Contents

1. Project Scaffolding & Foundation
2. Employee Master Data (CRUD)
3. Role-Based Access Control (RBAC)
4. Time & Attendance
5. Shift & Schedule Management
6. Leave & Absence Management
7. Training & Certification Tracking
8. Safety Incidents & OSHA Reporting
9. Equipment & Asset Assignment
10. Performance Reviews & Goals
11. Payroll Export Integration
12. Notifications & Announcements
13. Integration Layer (HRIS/WMS APIs)
14. Audit Trail & Compliance
15. Reporting & Analytics
16. Mobile Access (PWA)
17. Onboarding & Offboarding Workflow
18. Localization & Multi-Warehouse Support
19. Advanced Scheduling (AI)
20. Employee Self-Service Portal

---

## 1. Project Scaffolding & Foundation

### Section Title: Project Initialization & Base Modules

**Design Decisions:**  
- Use Spring Boot (Maven) for rapid development and convention over configuration.
- Modular package structure for scalability.
- Enable Actuator for health/metrics.
- Use Flyway for DB migrations.

**Design Specification:**  
- Package Structure:
  - `com.wms.ems` (root)
    - `employee`, `attendance`, `schedule`, `safety`, `asset`, `review`, `payroll`, `notification`, `integration`, `audit`, `reporting`, `mobile`, `onboarding`, `localization`, `ai`, `selfservice`
- Actuator enabled in `application.properties`.
- Flyway configured for baseline migration.

**Sample Implementation:**
```java
// application.properties
management.endpoints.web.exposure.include=health,info
spring.flyway.enabled=true
spring.datasource.url=jdbc:postgresql://localhost:5432/ems
spring.datasource.username=ems_user
spring.datasource.password=secret
```
---

## 2. Employee Master Data (CRUD)

### Section Title: Employee Domain Model & CRUD API

**Design Decisions:**  
- Employee is the central entity.
- Use JPA for persistence.
- DTOs for API contracts.
- Unique `badgeId` enforced.

**Design Specification:**  
- Entity: `Employee`
  - Fields: id, name, badgeId (unique), role, department, shiftGroup, hireDate, status
  - Soft-delete via `deleted` flag
- Repository: `EmployeeRepository extends JpaRepository<Employee, Long>`
- Service: `EmployeeService` for business logic
- Controller: `EmployeeController`
  - Endpoints: `/employees` (CRUD, pagination, filtering)
- DTOs: `EmployeeDTO`, `EmployeeCreateDTO`, `EmployeeUpdateDTO`
- Validation: Bean Validation (`@NotNull`, `@Size`, `@Pattern`)
- Mapping: MapStruct

**Sample Implementation:**
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue private Long id;
    @Column(nullable = false, unique = true) private String badgeId;
    @Column(nullable = false) private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted = false;
    // Auditing fields: createdBy, createdDate, etc.
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeCreateDTO dto) { ... }
    @GetMapping public Page<EmployeeDTO> list(Pageable pageable, @RequestParam Map<String, String> filters) { ... }
    @PutMapping("/{id}") public EmployeeDTO update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateDTO dto) { ... }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { ... }
}
```
---

## 3. Role-Based Access Control (RBAC)

### Section Title: Security Configuration & RBAC

**Design Decisions:**  
- Use Spring Security for authentication/authorization.
- Roles: ADMIN, HR, SUPERVISOR, WORKER.
- Method/endpoint security.
- API Key/OAuth2 toggle via config.

**Design Specification:**  
- SecurityConfig: `@EnableWebSecurity`
- Role-based access via `@PreAuthorize`
- API Key filter and OAuth2 config switchable via `application.properties`
- Row-level security in service layer

**Sample Implementation:**
```java
@Configuration
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
            .oauth2Login() // or API Key filter based on config
            .and()
            .csrf().disable();
    }
}
```
---

## 4. Time & Attendance

### Section Title: Attendance Domain & Clock-In/Out Workflow

**Design Decisions:**  
- Attendance events linked to Employee.
- Geofence/device capture optional.
- Approval workflow for corrections.

**Design Specification:**  
- Entity: `AttendanceEvent`
  - Fields: id, employee, type (CLOCK_IN/CLOCK_OUT), timestamp, deviceId, location, approved
- Repository: `AttendanceEventRepository`
- Service: `AttendanceService`
  - Business logic for shift association, totals calculation
- Controller: `AttendanceController`
  - Endpoints: `/attendance/clock-in`, `/attendance/clock-out`, `/attendance/corrections`
- DTOs: `AttendanceEventDTO`, `AttendanceCorrectionDTO`
- Validation: ensure no double clock-in/out, required fields

**Sample Implementation:**
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private AttendanceType type;
    private LocalDateTime timestamp;
    private String deviceId;
    private String location;
    private boolean approved;
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in") public ResponseEntity<?> clockIn(@RequestBody AttendanceEventDTO dto) { ... }
    @PostMapping("/clock-out") public ResponseEntity<?> clockOut(@RequestBody AttendanceEventDTO dto) { ... }
    @PostMapping("/corrections") public ResponseEntity<?> requestCorrection(@RequestBody AttendanceCorrectionDTO dto) { ... }
}
```
---

## 5. Shift & Schedule Management

### Section Title: Shift Templates & Scheduling

**Design Decisions:**  
- Recurring templates for shifts.
- Conflict detection for double-booking.
- Bulk assignment for supervisors.

**Design Specification:**  
- Entity: `ShiftTemplate`, `ShiftAssignment`
  - Fields: id, name, startTime, endTime, recurrence, blackoutDates
- Repository: `ShiftTemplateRepository`, `ShiftAssignmentRepository`
- Service: `ShiftService`
  - Conflict detection logic
- Controller: `ShiftController`
  - Endpoints: `/shifts/templates`, `/shifts/assignments`
- DTOs: `ShiftTemplateDTO`, `ShiftAssignmentDTO`
- Audit logging for assignments

**Sample Implementation:**
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrence; // e.g., "WEEKLY"
    @ElementCollection private List<LocalDate> blackoutDates;
}

@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates") public ShiftTemplateDTO createTemplate(@RequestBody ShiftTemplateDTO dto) { ... }
    @PostMapping("/assignments") public void assignShift(@RequestBody ShiftAssignmentDTO dto) { ... }
}
```
---

## 6. Leave & Absence Management

### Section Title: Leave Requests & Approval Workflow

**Design Decisions:**  
- PTO, sick, unpaid leave types.
- Supervisor approval workflow.
- Accrual balances.

**Design Specification:**  
- Entity: `LeaveRequest`
  - Fields: id, employee, type, startDate, endDate, status, balance
- Repository: `LeaveRequestRepository`
- Service: `LeaveService`
  - Business logic for accruals, coverage
- Controller: `LeaveController`
  - Endpoints: `/leave/requests`, `/leave/approvals`
- DTOs: `LeaveRequestDTO`, `LeaveApprovalDTO`
- Integration with scheduling/payroll

**Sample Implementation:**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING) private LeaveStatus status;
    private int balance;
}

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/requests") public LeaveRequestDTO requestLeave(@RequestBody LeaveRequestDTO dto) { ... }
    @PostMapping("/approvals") public void approveLeave(@RequestBody LeaveApprovalDTO dto) { ... }
}
```
---

## 7. Training & Certification Tracking

### Section Title: Certification Management & Assignment Blocking

**Design Decisions:**  
- Track certifications, expirations, renewals.
- Block assignment if expired.
- Alerts for upcoming expirations.

**Design Specification:**  
- Entity: `Certification`
  - Fields: id, employee, type, issueDate, expiryDate, documentUrl
- Repository: `CertificationRepository`
- Service: `CertificationService`
  - Alert logic, assignment checks
- Controller: `CertificationController`
  - Endpoints: `/certifications`, `/certifications/alerts`
- DTOs: `CertificationDTO`
- Integration with asset assignment

**Sample Implementation:**
```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
}

@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping public CertificationDTO addCertification(@RequestBody CertificationDTO dto) { ... }
    @GetMapping("/alerts") public List<CertificationDTO> getExpiringCerts() { ... }
}
```
---

## 8. Safety Incidents & OSHA Reporting

### Section Title: Safety Incident Reporting & Investigation Workflow

**Design Decisions:**  
- Record incidents/near-misses.
- Workflow for investigation.
- OSHA summary export.

**Design Specification:**  
- Entity: `SafetyIncident`
  - Fields: id, severity, location, description, involvedEmployees, status
- Repository: `SafetyIncidentRepository`
- Service: `SafetyService`
  - Investigation workflow
- Controller: `SafetyController`
  - Endpoints: `/safety/incidents`, `/safety/osha-export`
- DTOs: `SafetyIncidentDTO`
- Reporting endpoints

**Sample Implementation:**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private String severity;
    private String location;
    private String description;
    @ManyToMany private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING) private IncidentStatus status;
}

@RestController
@RequestMapping("/safety")
public class SafetyController {
    @PostMapping("/incidents") public SafetyIncidentDTO reportIncident(@RequestBody SafetyIncidentDTO dto) { ... }
    @GetMapping("/osha-export") public ResponseEntity<Resource> exportOSHA() { ... }
}
```
---

## 9. Equipment & Asset Assignment

### Section Title: Asset Assignment & Certification Validation

**Design Decisions:**  
- Assign assets to employees.
- Block usage if certification invalid.
- Track asset condition.

**Design Specification:**  
- Entity: `Asset`, `AssetAssignment`
  - Fields: id, type, condition, assignedTo, checkoutDate, returnDate
- Repository: `AssetRepository`, `AssetAssignmentRepository`
- Service: `AssetService`
  - Certification validation logic
- Controller: `AssetController`
  - Endpoints: `/assets`, `/assets/assign`, `/assets/return`
- DTOs: `AssetDTO`, `AssetAssignmentDTO`
- History log per asset/employee

**Sample Implementation:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String type;
    private String condition;
}

@Entity
public class AssetAssignment {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Asset asset;
    @ManyToOne private Employee employee;
    private LocalDateTime checkoutDate;
    private LocalDateTime returnDate;
}

@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign") public void assignAsset(@RequestBody AssetAssignmentDTO dto) { ... }
    @PostMapping("/return") public void returnAsset(@RequestBody AssetAssignmentDTO dto) { ... }
}
```
---

## 10. Performance Reviews & Goals

### Section Title: Review Cycle & Acknowledgement Workflow

**Design Decisions:**  
- Templates for reviews.
- Supervisor/employee sign-off.
- Immutable history after sign-off.

**Design Specification:**  
- Entity: `PerformanceReview`, `ReviewCycle`
  - Fields: id, employee, supervisor, template, ratings, comments, status
- Repository: `PerformanceReviewRepository`, `ReviewCycleRepository`
- Service: `ReviewService`
  - Submission/acknowledgement logic
- Controller: `ReviewController`
  - Endpoints: `/reviews/cycles`, `/reviews/submit`, `/reviews/acknowledge`
- DTOs: `PerformanceReviewDTO`, `ReviewCycleDTO`
- PDF export

**Sample Implementation:**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private Employee supervisor;
    private String template;
    private String ratings;
    private String comments;
    @Enumerated(EnumType.STRING) private ReviewStatus status;
}

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping("/cycles") public ReviewCycleDTO createCycle(@RequestBody ReviewCycleDTO dto) { ... }
    @PostMapping("/submit") public void submitReview(@RequestBody PerformanceReviewDTO dto) { ... }
    @PostMapping("/acknowledge") public void acknowledgeReview(@RequestBody PerformanceReviewDTO dto) { ... }
}
```
---

## 11. Payroll Export Integration

### Section Title: Payroll Export & Audit Logging

**Design Decisions:**  
- Generate payroll files from attendance/leave.
- Map to provider formats.
- Secure delivery (SFTP/API).
- Audit every export.

**Design Specification:**  
- Service: `PayrollExportService`
  - File generation logic
  - Delivery via SFTP/API
  - Retry/backoff on failure
- Entity: `PayrollExportLog`
  - Fields: id, exportDate, status, details
- Controller: `PayrollController`
  - Endpoints: `/payroll/export`, `/payroll/logs`
- DTOs: `PayrollExportDTO`, `PayrollExportLogDTO`

**Sample Implementation:**
```java
@Service
public class PayrollExportService {
    @Transactional
    public PayrollExportDTO generateExport(LocalDate period) { ... }
    public void deliverExport(PayrollExportDTO export) { ... }
}

@Entity
public class PayrollExportLog {
    @Id @GeneratedValue private Long id;
    private LocalDate exportDate;
    private String status;
    private String details;
}

@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/export") public PayrollExportDTO exportPayroll(@RequestBody PayrollExportDTO dto) { ... }
    @GetMapping("/logs") public List<PayrollExportLogDTO> getExportLogs() { ... }
}
```
---

## 12. Notifications & Announcements

### Section Title: Notification Preferences & Delivery

**Design Decisions:**  
- In-app, email, SMS channels.
- Opt-in/out per user.
- Delivery status tracking.
- Rate limits, quiet hours.

**Design Specification:**  
- Entity: `NotificationPreference`, `Announcement`, `NotificationDelivery`
- Service: `NotificationService`
  - Async delivery via `@Async`
  - Channel selection logic
- Controller: `NotificationController`
  - Endpoints: `/notifications/preferences`, `/announcements`
- DTOs: `NotificationPreferenceDTO`, `AnnouncementDTO`
- Integration with email/SMS providers

**Sample Implementation:**
```java
@Entity
public class NotificationPreference {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private boolean emailOptIn;
    private boolean smsOptIn;
    private boolean inAppOptIn;
}

@Service
public class NotificationService {
    @Async
    public void sendNotification(NotificationDTO dto) { ... }
}

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping("/preferences") public void setPreferences(@RequestBody NotificationPreferenceDTO dto) { ... }
    @PostMapping("/announcements") public void sendAnnouncement(@RequestBody AnnouncementDTO dto) { ... }
}
```
---

## 13. Integration Layer (HRIS/WMS APIs)

### Section Title: External API Integration & Webhooks

**Design Decisions:**  
- REST APIs for HRIS/WMS.
- JWT/OAuth2 security.
- Webhooks for event delivery.
- Idempotency.

**Design Specification:**  
- Service: `IntegrationService`
  - HRIS sync job
  - WMS department/location sync
  - Webhook delivery
- Controller: `IntegrationController`
  - Endpoints: `/integration/hris`, `/integration/wms`, `/integration/webhooks`
- DTOs: `HRISSyncDTO`, `WMSDepartmentDTO`, `WebhookEventDTO`
- OpenAPI documentation

**Sample Implementation:**
```java
@Service
public class IntegrationService {
    public void syncHRIS(HRISSyncDTO dto) { ... }
    public void syncWMS(WMSDepartmentDTO dto) { ... }
    public void deliverWebhook(WebhookEventDTO dto) { ... }
}

@RestController
@RequestMapping("/integration")
public class IntegrationController {
    @PostMapping("/hris") public void syncHRIS(@RequestBody HRISSyncDTO dto) { ... }
    @PostMapping("/wms") public void syncWMS(@RequestBody WMSDepartmentDTO dto) { ... }
    @PostMapping("/webhooks") public void receiveWebhook(@RequestBody WebhookEventDTO dto) { ... }
}
```
---

## 14. Audit Trail & Compliance

### Section Title: Centralized Audit Logging

**Design Decisions:**  
- Immutable log for sensitive changes.
- Actor, timestamp, before/after state.
- Tamper-evident storage.

**Design Specification:**  
- Entity: `AuditLog`
  - Fields: id, entityType, entityId, actor, action, timestamp, before, after
- Repository: `AuditLogRepository`
- Service: `AuditService`
  - Log creation on every change
- Controller: `AuditController`
  - Endpoints: `/audit/logs`
- DTOs: `AuditLogDTO`
- Coverage validation via tests

**Sample Implementation:**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String entityType;
    private Long entityId;
    private String actor;
    private String action;
    private LocalDateTime timestamp;
    @Lob private String before;
    @Lob private String after;
}

@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/logs") public List<AuditLogDTO> getLogs(@RequestParam Map<String, String> filters) { ... }
}
```
---

## 15. Reporting & Analytics

### Section Title: Operational Reports & Dashboards

**Design Decisions:**  
- Attendance, overtime, leave, certification, safety KPIs.
- CSV/PDF export.
- Role-based dashboards.

**Design Specification:**  
- Service: `ReportingService`
  - Report generation logic
- Controller: `ReportingController`
  - Endpoints: `/reports/attendance`, `/reports/overtime`, `/reports/certification`, `/reports/safety`
- DTOs: `ReportDTO`
- Metrics endpoints for BI

**Sample Implementation:**
```java
@Service
public class ReportingService {
    public ReportDTO generateAttendanceReport(DateRange range, String department) { ... }
    public ReportDTO generateOvertimeReport(DateRange range) { ... }
}

@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance") public ResponseEntity<Resource> attendanceReport(@RequestParam Map<String, String> params) { ... }
    @GetMapping("/overtime") public ResponseEntity<Resource> overtimeReport(@RequestParam Map<String, String> params) { ... }
}
```
---

## 16. Mobile Access (PWA)

### Section Title: Mobile PWA & Offline Support

**Design Decisions:**  
- Responsive views for core flows.
- Offline queue for clock events.
- PWA manifest.

**Design Specification:**  
- Frontend: PWA manifest, service worker
- Backend: `/mobile/clock-in`, `/mobile/leave-request`
- Offline queue sync logic
- Lighthouse PWA compliance

**Sample Implementation:**
```javascript
// manifest.json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/mobile",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "icons": [ ... ]
}

// Service Worker (pseudo-code)
self.addEventListener('fetch', function(event) {
  // Cache API requests, queue offline clock-ins
});
```
---

## 17. Onboarding & Offboarding Workflow

### Section Title: Automated Provisioning & Deprovisioning

**Design Decisions:**  
- Automate account creation, initial schedule, training tasks.
- Deprovision assets/access on termination.

**Design Specification:**  
- Service: `OnboardingService`, `OffboardingService`
  - HRIS triggers new hire
  - Task generation for training/assets
  - Offboarding revokes access, collects assets
- Controller: `OnboardingController`
  - Endpoints: `/onboarding/start`, `/offboarding/start`
- DTOs: `OnboardingTaskDTO`, `OffboardingTaskDTO`

**Sample Implementation:**
```java
@Service
public class OnboardingService {
    public void startOnboarding(EmployeeDTO dto) { ... }
}

@Service
public class OffboardingService {
    public void startOffboarding(EmployeeDTO dto) { ... }
}

@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @PostMapping("/start") public void startOnboarding(@RequestBody EmployeeDTO dto) { ... }
    @PostMapping("/offboarding/start") public void startOffboarding(@RequestBody EmployeeDTO dto) { ... }
}
```
---

## 18. Localization & Multi-Warehouse Support

### Section Title: Multi-Tenant Configuration

**Design Decisions:**  
- Support multiple warehouses.
- Tenant isolation via configuration.

**Design Specification:**  
- Entity: `Warehouse`
  - Fields: id, name, location, config
- Tenant context via Spring filters/interceptors
- Repository: `WarehouseRepository`
- Service: `WarehouseService`
- Controller: `WarehouseController`
  - Endpoints: `/warehouses`
- DTOs: `WarehouseDTO`

**Sample Implementation:**
```java
@Entity
public class Warehouse {
    @Id @GeneratedValue private Long id;
    private String name;
    private String location;
    private String config;
}

@RestController
@RequestMapping("/warehouses")
public class WarehouseController {
    @GetMapping public List<WarehouseDTO> listWarehouses() { ... }
}
```
---

## 19. Advanced Scheduling (AI)

### Section Title: AI-Based Schedule Optimization

**Design Decisions:**  
- Use AI/ML for optimal shift scheduling.
- Integrate with existing schedule module.

**Design Specification:**  
- Service: `AISchedulingService`
  - Optimization algorithms
- Controller: `AISchedulingController`
  - Endpoints: `/ai/schedule/optimize`
- DTOs: `AIScheduleRequestDTO`, `AIScheduleResultDTO`

**Sample Implementation:**
```java
@Service
public class AISchedulingService {
    public AIScheduleResultDTO optimizeSchedule(AIScheduleRequestDTO dto) { ... }
}

@RestController
@RequestMapping("/ai/schedule")
public class AISchedulingController {
    @PostMapping("/optimize") public AIScheduleResultDTO optimize(@RequestBody AIScheduleRequestDTO dto) { ... }
}
```
---

## 20. Employee Self-Service Portal

### Section Title: Self-Service Portal Features

**Design Decisions:**  
- Employees view pay stubs, update profile.
- Secure, role-based access.

**Design Specification:**  
- Controller: `SelfServiceController`
  - Endpoints: `/selfservice/paystubs`, `/selfservice/profile`
- DTOs: `PayStubDTO`, `ProfileUpdateDTO`
- Integration with payroll and employee modules

**Sample Implementation:**
```java
@RestController
@RequestMapping("/selfservice")
public class SelfServiceController {
    @GetMapping("/paystubs") public List<PayStubDTO> getPayStubs(@AuthenticationPrincipal Employee employee) { ... }
    @PutMapping("/profile") public void updateProfile(@AuthenticationPrincipal Employee employee, @RequestBody ProfileUpdateDTO dto) { ... }
}
```
---

## Cross-Cutting Concerns

### Exception Handling & Validation

- Use `@ControllerAdvice` for global exception handling.
- Bean Validation (`@Valid`, `@NotNull`, etc.) on DTOs.
- Custom exceptions for business logic errors.

### Auditing

- Spring Data JPA Auditing (`@CreatedBy`, `@CreatedDate`, etc.)
- Audit logs for all sensitive operations.

### Transaction Management

- Use `@Transactional` on service methods for atomicity.

### Caching

- Use Spring Cache (`@Cacheable`) for frequently accessed data (e.g., employee profiles, schedules).

### Async Processing

- Use `@Async` for notifications, exports, integrations.

### OpenAPI/Swagger

- Annotate controllers with Swagger/OpenAPI for API documentation.

### Monitoring

- Spring Boot Actuator for health, metrics, and custom endpoints.

---

## Integration Points

- HRIS: REST API, scheduled sync job.
- WMS: REST API, department/location sync.
- Payroll: SFTP/API file delivery.
- Email/SMS: External provider integration.
- Webhooks: Event delivery to external systems.

---

## Sample Directory Structure

```
com.wms.ems
âââ employee
â   âââ Employee.java
â   âââ EmployeeRepository.java
â   âââ EmployeeService.java
â   âââ EmployeeController.java
â   âââ dto
âââ attendance
âââ schedule
âââ leave
âââ certification
âââ safety
âââ asset
âââ review
âââ payroll
âââ notification
âââ integration
âââ audit
âââ reporting
âââ mobile
âââ onboarding
âââ localization
âââ ai
âââ selfservice
```

---

## Conclusion

This document provides a production-ready, detailed technical design for all user stories in the Warehouse EMS. Each module follows Spring Boot best practices, ensuring maintainability, scalability, and security. Developers should use this as the blueprint for implementation, with code samples, package structure, and integration patterns clearly defined.