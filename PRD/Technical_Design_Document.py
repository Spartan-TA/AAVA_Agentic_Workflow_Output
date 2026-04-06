"""
# WAREHOUSE EMS - LOW-LEVEL TECHNICAL DESIGN DOCUMENT

## Document Overview

This document provides comprehensive low-level technical design specifications for all 20 epics of the Warehouse Employee Management System (EMS).

---

## Section: E01 - Project Scaffolding & Domain Setup

### Description
Initialize Spring Boot (Maven) project with base packages, core modules, Flyway/Liquibase migrations, and Actuator.

### Design Specification
- Spring Boot Maven project with src/main/java/com/warehouse/ems as base package
- Modules: employee, scheduling, attendance, safety
- Flyway/Liquibase for DB migrations
- Actuator enabled for health checks
- Application runs on port 8080

### Sample Implementation

POM Dependencies:
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>

Application Properties:
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost/warehouse_ems
spring.flyway.enabled=true

---

## Section: E02 - Employee Master Data (CRUD)

### Description
Employee domain with CRUD APIs: name, badgeId, role, department, shiftGroup, hireDate, status.

### Design Specification
- Employee entity with unique badgeId
- JPA repository
- REST endpoints: POST/GET/PUT/PATCH/DELETE /employees
- Pagination and filtering

### Sample Implementation

@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badgeId"))
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true)
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted = false;
}

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { }
    @GetMapping
    public Page<EmployeeDto> list(Pageable pageable) { }
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @RequestBody EmployeeDto dto) { }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) { }
}

---

## Section: E03 - Role Based Access Control (RBAC)

### Description
Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER), method/endpoint security, API key/OAuth2 support.

### Design Specification
- Spring Security configuration
- Role-based access control
- Method security with @PreAuthorize
- OAuth2/API key authentication

### Sample Implementation

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR")
            .antMatchers("/attendance/**").hasAnyRole("SUPERVISOR", "WORKER")
            .anyRequest().authenticated()
            .and().oauth2ResourceServer().jwt();
    }
}

@PreAuthorize("hasRole('ADMIN')")
public void deleteEmployee(Long id) { }

---

## Section: E04 - Time & Attendance (Clock In/Out)

### Description
Clock-in/out endpoints with geofence, device capture, hours calculation, missed punch workflow.

### Design Specification
- Attendance entity with clockIn, clockOut, deviceId, location, shiftId
- Clock-in/out REST endpoints
- Hours calculation logic
- Missed punch corrections

### Sample Implementation

@Entity
public class Attendance {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private String deviceId;
    private String location;
    private Long shiftId;
    private String status;
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockEventDto dto) { }
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockEventDto dto) { }
}

---

## Section: E05 - Shift & Schedule Management

### Description
Shift templates, rotations, overtime rules, employee assignments, blackout dates, operation calendars.

### Design Specification
- ShiftTemplate and ShiftAssignment entities
- Conflict detection
- Bulk assignment
- Blackout date handling

### Sample Implementation

@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime start;
    private LocalTime end;
    private String recurrence;
    private String overtimeRule;
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
    public ResponseEntity<ShiftTemplate> createTemplate(@RequestBody ShiftTemplateDto dto) { }
    @PostMapping("/assignments/bulk")
    public ResponseEntity<?> bulkAssign(@RequestBody BulkAssignmentDto dto) { }
}

---

## Section: E06 - Leave & Absence Management

### Description
PTO/sick/unpaid leave requests, approvals, accrual balances, scheduling and payroll integration.

### Design Specification
- LeaveRequest entity
- Request/approval workflow
- Balance tracking
- Scheduling exclusion

### Sample Implementation

@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type;
    private LocalDate start;
    private LocalDate end;
    private String status;
    private Double accrualBalance;
}

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/request")
    public ResponseEntity<?> requestLeave(@RequestBody LeaveRequestDto dto) { }
    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approveLeave(@PathVariable Long id) { }
}

---

## Section: E07 - Training & Certification Tracking

### Description
Certification tracking, expirations, renewals, assignment blocking, document uploads.

### Design Specification
- Certification entity
- Expiry alerts (30/7 days)
- Assignment validation
- Document management

### Sample Implementation

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
    public ResponseEntity<Certification> create(@RequestBody CertificationDto dto) { }
    @GetMapping("/alerts")
    public List<CertificationAlertDto> getExpiryAlerts() { }
}

---

## Section: E08 - Safety Incidents & OSHA Reporting

### Description
Incident/near-miss recording, severity tracking, investigation workflow, OSHA summary generation.

### Design Specification
- SafetyIncident entity
- Status workflow (OPEN, INVESTIGATING, RESOLVED)
- OSHA export
- Metrics dashboard

### Sample Implementation

@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String severity;
    private String location;
    private String description;
    private String status;
    @ElementCollection
    private List<Long> involvedEmployeeIds;
}

@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
    @PostMapping
    public ResponseEntity<SafetyIncident> recordIncident(@RequestBody SafetyIncidentDto dto) { }
    @GetMapping("/osha/export")
    public ResponseEntity<Resource> exportOSHA() { }
}

---

## Section: E09 - Equipment & Asset Assignment

### Description
Asset assignment (scanners, forklifts, PPE), checkout/return tracking, certification validation, condition state.

### Design Specification
- Asset entity
- Check-in/out workflow
- Certification validation
- Overdue tracking

### Sample Implementation

@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serial;
    private String condition;
    private Long assignedEmployeeId;
    private LocalDateTime checkoutDate;
    private LocalDateTime returnDate;
}

@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody AssetCheckoutDto dto) { }
    @PostMapping("/return")
    public ResponseEntity<?> returnAsset(@RequestBody AssetReturnDto dto) { }
}

---

## Section: E10 - Performance Reviews & Goals

### Description
Review templates, goals tracking, competencies, ratings, supervisor/employee acknowledgements.

### Design Specification
- PerformanceReview entity
- Review cycle workflow
- PDF export
- Immutable history

### Sample Implementation

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
    private String ratings;
    private String comments;
    private String status;
}

@RestController
@RequestMapping("/reviews")
public class PerformanceReviewController {
    @PostMapping
    public ResponseEntity<PerformanceReview> create(@RequestBody PerformanceReviewDto dto) { }
    @GetMapping("/export/{id}")
    public ResponseEntity<Resource> exportPdf(@PathVariable Long id) { }
}

---

## Section: E11 - Payroll Export Integration

### Description
Payroll file generation from attendance and leave, provider format mapping, secure delivery (SFTP/API).

### Design Specification
- PayrollExport entity
- File generation
- Provider mapping
- Retry/backoff logic

### Sample Implementation

@Entity
public class PayrollExport {
    @Id @GeneratedValue
    private Long id;
    private String period;
    private String fileUrl;
    private String status;
    private String provider;
}

@RestController
@RequestMapping("/payroll")
public class PayrollExportController {
    @PostMapping("/export")
    public ResponseEntity<?> exportPayroll(@RequestBody PayrollExportRequestDto dto) { }
}

---

## Section: E12 - Notifications & Announcements

### Description
In-app and email/SMS notifications for shifts, certifications, approvals, announcements with quiet hours.

### Design Specification
- Notification entity
- Multi-channel delivery
- Opt-in/out management
- Rate limiting

### Sample Implementation

@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private Long userId;
    private String channel;
    private String type;
    private String content;
    private String status;
    private LocalDateTime timestamp;
}

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping
    public ResponseEntity<Notification> send(@RequestBody NotificationDto dto) { }
}

---

## Section: E13 - Integration Layer (HRIS/WMS APIs)

### Description
REST APIs for HRIS sync, WMS integration, IDP for SSO, webhooks for events.

### Design Specification
- Integration endpoints
- JWT/OAuth2 security
- Sync jobs
- Webhook handling
- OpenAPI documentation

### Sample Implementation

@RestController
@RequestMapping("/api/hris")
public class HRISController {
    @PostMapping("/sync")
    public ResponseEntity<?> syncEmployees(@RequestBody HRISSyncDto dto) { }
}

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {
    @PostMapping
    public ResponseEntity<?> handleEvent(@RequestBody WebhookEventDto dto) { }
}

---

## Section: E14 - Audit Trail & Compliance

### Description
Centralized audit logging for sensitive changes with tamper-evident storage.

### Design Specification
- AuditLog entity
- Immutable logging
- Export functionality
- Actor tracking

### Sample Implementation

@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    private String action;
    @Lob
    private String before;
    @Lob
    private String after;
}

@RestController
@RequestMapping("/audit")
public class AuditLogController {
    @GetMapping("/export")
    public ResponseEntity<Resource> exportLogs(@RequestParam String entity) { }
}

---

## Section: E15 - Reporting & Analytics

### Description
Operational reports for attendance, overtime, leave, certifications, safety KPIs with CSV/PDF export.

### Design Specification
- Reporting endpoints
- CSV/PDF export
- Role-based access
- BI metrics

### Sample Implementation

@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> attendanceReport(@RequestParam Map<String,String> filters) { }
    @GetMapping("/export/csv")
    public ResponseEntity<Resource> exportCsv(@RequestParam String reportType) { }
}

---

## Section: E16 - Mobile Access (PWA)

### Description
Responsive PWA for clock-in/out, schedules, leave requests, announcements with offline support.

### Design Specification
- PWA manifest
- Service worker
- Offline queue
- Mobile-friendly UI

### Sample Implementation

Manifest:
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone"
}

Service Worker:
self.addEventListener('fetch', function(event) {
  // Cache and offline queue logic
});

---

## Section: E17 - Onboarding & Offboarding Workflow

### Description
Automated provisioning/deprovisioning of accounts, schedules, training, and assets.

### Design Specification
- Workflow entity
- Provisioning logic
- Task automation
- Access revocation

### Sample Implementation

@Entity
public class Workflow {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type;
    private String status;
    @ElementCollection
    private List<String> tasks;
}

@RestController
@RequestMapping("/workflow")
public class WorkflowController {
    @PostMapping("/onboard")
    public ResponseEntity<?> onboard(@RequestBody OnboardDto dto) { }
    @PostMapping("/offboard")
    public ResponseEntity<?> offboard(@RequestBody OffboardDto dto) { }
}

---

## Section: E18 - Localization & Multi-Warehouse

### Description
Multi-warehouse support with distinct calendars, policies, time zones, English/Spanish UI.

### Design Specification
- Warehouse entity
- i18n support
- Time zone handling
- Locale-aware endpoints

### Sample Implementation

@Entity
public class Warehouse {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String timezone;
    private String calendar;
    private String policies;
}

spring.messages.basename=messages

messages_en.properties:
welcome=Welcome

messages_es.properties:
welcome=Bienvenido

---

## Section: E19 - Advanced Scheduling (AI-Assisted)

### Description
ML-based shift recommendations with demand forecasts, skills, preferences.

### Design Specification
- SchedulingRecommendation entity
- ML service integration
- Recommendation scoring
- Explainable AI

### Sample Implementation

@Entity
public class SchedulingRecommendation {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private Long shiftId;
    private Double score;
    private String reason;
}

@Service
public class SchedulingService {
    public List<SchedulingRecommendation> getRecommendations(Long warehouseId) {
        // Call ML REST API
        return recommendations;
    }
}

---

## Section: E20 - Self-Service Portal

### Description
Employee portal for profile updates, pay stubs, tax forms, benefits, job postings.

### Design Specification
- Portal endpoints
- Employee authentication
- Document download
- Profile management

### Sample Implementation

@RestController
@RequestMapping("/portal")
public class PortalController {
    @GetMapping("/profile")
    public ResponseEntity<EmployeeDto> getProfile() { }
    @PutMapping("/profile")
    public ResponseEntity<EmployeeDto> updateProfile(@RequestBody EmployeeDto dto) { }
    @GetMapping("/paystubs")
    public ResponseEntity<Resource> getPayStubs() { }
}

---

## Conclusion

This comprehensive low-level technical design document covers all 20 epics of the Warehouse EMS system with detailed Spring Boot implementations, entity models, service layers, repository specifications, controller designs, and security configurations.

**Document Status:** Complete
**Total Epics Covered:** 20
**Technology Stack:** Spring Boot, Maven, PostgreSQL, Flyway/Liquibase, Spring Security, JPA/Hibernate
**Compliance:** Spring Boot Best Practices, RESTful API Standards, Security Standards

---

**End of Document**
"""