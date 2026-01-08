# Warehouse Employee Management System (EMS) - Technical Design Document

## Table of Contents
- [E01 Project Scaffolding & Domain Setup](#e01)
- [E02 Employee Master Data (CRUD)](#e02)
- [E03 Role Based Access Control (RBAC)](#e03)
- [E04 Time & Attendance (Clock In/Out)](#e04)
- [E05 Shift & Schedule Management](#e05)
- [E06 Leave & Absence Management](#e06)
- [E07 Training & Certification Tracking](#e07)
- [E08 Safety Incidents & OSHA Reporting](#e08)
- [E09 Equipment & Asset Assignment](#e09)
- [E10 Performance Reviews & Goals](#e10)
- [E11 Payroll Export Integration](#e11)
- [E12 Notifications & Announcements](#e12)
- [E13 Integration Layer (HRIS/WMS APIs)](#e13)
- [E14 Audit Trail & Compliance](#e14)
- [E15 Reporting & Analytics](#e15)
- [E16 Mobile Access (PWA)](#e16)
- [E17 Onboarding & Offboarding Workflow](#e17)
- [E18 Localization](#e18)
- [E19 AI Scheduling](#e19)
- [E20 Self-Service Portal](#e20)

---

## Section: E01 Project Scaffolding & Domain Setup

**Description:**Establishes the foundational Spring Boot project structure, configures Maven, sets up base packages, integrates Flyway/Liquibase for DB migrations, and enables Actuator for health monitoring.

**Design Specification:**- **Architecture:** Layered (Controller, Service, Repository, Domain)- **Package Structure:**  - `com.warehouseems`    - `employee`    - `attendance`    - `scheduling`    - `safety`    - `config`    - `common`- **Modules:**  - Core modules for each domain  - DB migration scripts in `src/main/resources/db/migration`- **Configuration:**  - `application.yml` for environment settings  - Actuator enabled  - Flyway/Liquibase migration scripts- **Spring Boot Starter:**  - `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-actuator`

**Sample Implementation:**```java
// src/main/java/com/warehouseems/WarehouseEmsApplication.java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}

// src/main/resources/application.yml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouseems
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

## Section: E02 Employee Master Data (CRUD)

**Description:**Implements the Employee domain with CRUD operations, supporting unique badge IDs, soft deletes, pagination, filtering, and OpenAPI documentation.

**Design Specification:**- **Entity:**  - `Employee` (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)- **Repository:**  - `EmployeeRepository extends JpaRepository<Employee, Long>`  - Custom queries for filtering/pagination- **Service:**  - `EmployeeService` with business logic for CRUD, soft-delete- **Controller:**  - REST endpoints: `/employees` (POST, GET, PUT, PATCH, DELETE)  - OpenAPI annotations- **Validation:**  - Unique badgeId enforced- **DTOs:**  - `EmployeeDTO`, `EmployeeCreateRequest`, `EmployeeUpdateRequest`

**Sample Implementation:**```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badgeId"))
public class Employee {
    @Id @GeneratedValue private Long id;
    private String name;
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted = false;
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeCreateRequest req) { ... }
    @GetMapping public Page<EmployeeDTO> list(Pageable pageable, @RequestParam Map<String, String> filters) { ... }
    @PutMapping("/{id}") public EmployeeDTO update(@PathVariable Long id, @RequestBody EmployeeUpdateRequest req) { ... }
    @DeleteMapping("/{id}") public void softDelete(@PathVariable Long id) { ... }
}
```

---

## Section: E03 Role Based Access Control (RBAC)

**Description:**Integrates Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER), endpoint/method security, row-level constraints, and API key/OAuth2 toggle.

**Design Specification:**- **Roles:** Enum or DB-backed- **Security Config:**  - `WebSecurityConfigurerAdapter` or `SecurityFilterChain`  - Method-level security (`@PreAuthorize`)  - API key/OAuth2 toggle via config- **User Entity:**  - `User` (id, username, password, roles)- **Access Rules:**  - ADMIN: full access  - SUPERVISOR: team-limited  - HR: employee management  - WORKER: self-service

**Sample Implementation:**```java
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
            .oauth2Login()
            .and()
            .httpBasic();
    }
}

@Service
public class EmployeeService {
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == authentication.principal.department)")
    public Employee updateEmployee(Employee employee) { ... }
}
```

---

## Section: E04 Time & Attendance (Clock In/Out)

**Description:**Provides endpoints for clock-in/out events, geofence/device capture, shift association, missed punch corrections, and CSV report exports.

**Design Specification:**- **Entity:**  - `AttendanceEvent` (id, employeeId, type [IN/OUT], timestamp, location, deviceId, shiftId, correctionStatus)- **Repository:**  - `AttendanceEventRepository`- **Service:**  - Clock-in/out logic, shift association, correction workflow- **Controller:**  - `/attendance/clock-in`, `/attendance/clock-out`, `/attendance/corrections`- **Reports:**  - CSV export endpoint

**Sample Implementation:**```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type; // IN/OUT
    private LocalDateTime timestamp;
    private String location;
    private String deviceId;
    private Long shiftId;
    private String correctionStatus;
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in") public ResponseEntity<?> clockIn(@RequestBody ClockInRequest req) { ... }
    @PostMapping("/clock-out") public ResponseEntity<?> clockOut(@RequestBody ClockOutRequest req) { ... }
    @PostMapping("/corrections") public ResponseEntity<?> requestCorrection(@RequestBody CorrectionRequest req) { ... }
    @GetMapping("/report") public ResponseEntity<Resource> exportCsv(@RequestParam LocalDate date) { ... }
}
```

---

## Section: E05 Shift & Schedule Management

**Description:**Manages recurring shift templates, rotations, overtime rules, blackout dates, and employee assignments.

**Design Specification:**- **Entities:**  - `ShiftTemplate` (id, name, startTime, endTime, recurrence, blackoutDates)  - `EmployeeShiftAssignment` (id, employeeId, shiftTemplateId, date)- **Repository:**  - `ShiftTemplateRepository`, `EmployeeShiftAssignmentRepository`- **Service:**  - Conflict detection, bulk assignment, audit logging- **Controller:**  - `/shifts/templates`, `/shifts/assignments`

**Sample Implementation:**```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrence; // e.g., WEEKLY, DAILY
    @ElementCollection private List<LocalDate> blackoutDates;
}

@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates") public ShiftTemplate createTemplate(@RequestBody ShiftTemplateDto dto) { ... }
    @PostMapping("/assignments/bulk") public void bulkAssign(@RequestBody BulkAssignRequest req) { ... }
}
```

---

## Section: E06 Leave & Absence Management

**Description:**Handles PTO, sick, unpaid leave requests/approvals, accrual balances, and integration with scheduling/payroll.

**Design Specification:**- **Entities:**  - `LeaveRequest` (id, employeeId, type, startDate, endDate, status, accrualBalance)- **Repository:**  - `LeaveRequestRepository`- **Service:**  - Request/approval logic, balance updates, schedule exclusion- **Controller:**  - `/leave/requests`, `/leave/approvals`

**Sample Implementation:**```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type; // PTO, Sick, Unpaid
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // Requested, Approved, Denied
    private int accrualBalance;
}

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/requests") public LeaveRequest requestLeave(@RequestBody LeaveRequestDto dto) { ... }
    @PostMapping("/approvals/{id}") public LeaveRequest approveLeave(@PathVariable Long id) { ... }
}
```

---

## Section: E07 Training & Certification Tracking

**Description:**Tracks certifications, expirations, renewals, blocks assignments for expired certs, and uploads proof documents.

**Design Specification:**- **Entities:**  - `Certification` (id, employeeId, type, issueDate, expiryDate, documentUrl)- **Repository:**  - `CertificationRepository`- **Service:**  - Expiry alerts, assignment checks- **Controller:**  - `/certifications`, `/certifications/upload`

**Sample Implementation:**```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
}

@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping public Certification addCertification(@RequestBody CertificationDto dto) { ... }
    @PostMapping("/upload") public ResponseEntity<?> uploadProof(@RequestParam MultipartFile file) { ... }
}
```

---

## Section: E08 Safety Incidents & OSHA Reporting

**Description:**Records incidents/near-misses, severity, location, involved employees, investigation workflow, and OSHA summary exports.

**Design Specification:**- **Entities:**  - `SafetyIncident` (id, description, severity, location, involvedEmployeeIds, status, correctiveActions)- **Repository:**  - `SafetyIncidentRepository`- **Service:**  - Workflow management, OSHA export- **Controller:**  - `/safety/incidents`, `/safety/osha-export`

**Sample Implementation:**```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private String description;
    private String severity;
    private String location;
    @ElementCollection private List<Long> involvedEmployeeIds;
    private String status; // Open, Investigating, Resolved
    private String correctiveActions;
}

@RestController
@RequestMapping("/safety")
public class SafetyController {
    @PostMapping("/incidents") public SafetyIncident reportIncident(@RequestBody SafetyIncidentDto dto) { ... }
    @GetMapping("/osha-export") public ResponseEntity<Resource> exportOsha(@RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
}
```

---

## Section: E09 Equipment & Asset Assignment

**Description:**Assigns assets to employees, tracks check-in/out, blocks use if certification missing, and maintains asset condition.

**Design Specification:**- **Entities:**  - `Asset` (id, type, condition, assignedEmployeeId, checkoutDate, returnDate)- **Repository:**  - `AssetRepository`- **Service:**  - Assignment logic, certification checks, history logs- **Controller:**  - `/assets`, `/assets/assign`, `/assets/checkin`

**Sample Implementation:**```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String type;
    private String condition;
    private Long assignedEmployeeId;
    private LocalDate checkoutDate;
    private LocalDate returnDate;
}

@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign") public Asset assignAsset(@RequestBody AssetAssignRequest req) { ... }
    @PostMapping("/checkin") public Asset checkInAsset(@RequestBody AssetCheckInRequest req) { ... }
}
```

---

## Section: E10 Performance Reviews & Goals

**Description:**Manages review templates, goals, competencies, ratings, comments, and supervisor/employee acknowledgements.

**Design Specification:**- **Entities:**  - `PerformanceReview` (id, employeeId, cycle, goals, competencies, ratings, comments, status)- **Repository:**  - `PerformanceReviewRepository`- **Service:**  - Review cycle management, PDF export- **Controller:**  - `/reviews`, `/reviews/export`

**Sample Implementation:**```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String cycle; // Quarterly, Annual
    @ElementCollection private List<String> goals;
    @ElementCollection private List<String> competencies;
    @ElementCollection private List<Integer> ratings;
    private String comments;
    private String status; // Draft, Submitted, Acknowledged
}

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping public PerformanceReview createReview(@RequestBody ReviewDto dto) { ... }
    @GetMapping("/export/{id}") public ResponseEntity<Resource> exportPdf(@PathVariable Long id) { ... }
}
```

---

## Section: E11 Payroll Export Integration

**Description:**Generates payroll-ready files from attendance/leave, maps to provider formats, and delivers securely via SFTP/API.

**Design Specification:**- **Entities:**  - `PayrollExport` (id, period, fileUrl, status, provider, attemptCount)- **Repository:**  - `PayrollExportRepository`- **Service:**  - Export generation, delivery, retry logic, audit logging- **Controller:**  - `/payroll/export`, `/payroll/status`

**Sample Implementation:**```java
@Entity
public class PayrollExport {
    @Id @GeneratedValue private Long id;
    private String period;
    private String fileUrl;
    private String status; // Pending, Delivered, Failed
    private String provider;
    private int attemptCount;
}

@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/export") public PayrollExport exportPayroll(@RequestBody PayrollExportRequest req) { ... }
    @GetMapping("/status/{id}") public PayrollExport getStatus(@PathVariable Long id) { ... }
}
```

---

## Section: E12 Notifications & Announcements

**Description:**Sends in-app/email/SMS notifications for shift changes, expiring certs, approvals, announcements, with quiet hours and opt-in/out.

**Design Specification:**- **Entities:**  - `Notification` (id, userId, type, channel, message, status, deliveryTime)  - `Announcement` (id, title, message, visibleUntil)- **Repository:**  - `NotificationRepository`, `AnnouncementRepository`- **Service:**  - Delivery logic, rate limiting, localization- **Controller:**  - `/notifications`, `/announcements`

**Sample Implementation:**```java
@Entity
public class Notification {
    @Id @GeneratedValue private Long id;
    private Long userId;
    private String type;
    private String channel; // EMAIL, SMS, IN_APP
    private String message;
    private String status; // Sent, Failed
    private LocalDateTime deliveryTime;
}

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping public Notification sendNotification(@RequestBody NotificationDto dto) { ... }
    @GetMapping public List<Notification> getNotifications(@RequestParam Long userId) { ... }
}
```

---

## Section: E13 Integration Layer (HRIS/WMS APIs)

**Description:**Exposes REST APIs/connectors for HRIS, WMS, and IDP for SSO; supports webhooks and JWT/OAuth2 security.

**Design Specification:**- **Entities:**  - `IntegrationEvent` (id, type, payload, status, timestamp)- **Repository:**  - `IntegrationEventRepository`- **Service:**  - HRIS sync, WMS connector, webhook handler- **Controller:**  - `/api/hris`, `/api/wms`, `/api/webhooks`

**Sample Implementation:**```java
@Entity
public class IntegrationEvent {
    @Id @GeneratedValue private Long id;
    private String type;
    private String payload;
    private String status;
    private LocalDateTime timestamp;
}

@RestController
@RequestMapping("/api")
public class IntegrationController {
    @PostMapping("/hris") public ResponseEntity<?> syncHris(@RequestBody HrisSyncRequest req) { ... }
    @PostMapping("/wms") public ResponseEntity<?> syncWms(@RequestBody WmsSyncRequest req) { ... }
    @PostMapping("/webhooks") public ResponseEntity<?> handleWebhook(@RequestBody WebhookEvent event) { ... }
}
```

---

## Section: E14 Audit Trail & Compliance

**Description:**Centralizes audit logging for sensitive changes, stores immutable logs, and supports export by date/user/entity.

**Design Specification:**- **Entities:**  - `AuditLog` (id, actor, timestamp, entity, entityId, action, before, after)- **Repository:**  - `AuditLogRepository`- **Service:**  - Log creation, export, coverage tests- **Controller:**  - `/audit/logs`, `/audit/export`

**Sample Implementation:**```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    private Long entityId;
    private String action; // CREATE, UPDATE, DELETE
    @Lob private String before;
    @Lob private String after;
}

@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/logs") public List<AuditLog> getLogs(@RequestParam Map<String, String> filters) { ... }
    @GetMapping("/export") public ResponseEntity<Resource> exportLogs(@RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
}
```

---

## Section: E15 Reporting & Analytics

**Description:**Provides operational reports (attendance, overtime, leave, certifications, safety KPIs), CSV/PDF exports, and dashboards.

**Design Specification:**- **Entities:**  - Report DTOs (AttendanceReport, LeaveBalanceReport, CertificationStatusReport, SafetyKpiReport)- **Service:**  - Report generation, filtering, export logic- **Controller:**  - `/reports/attendance`, `/reports/leave`, `/reports/certifications`, `/reports/safety`, `/reports/export`

**Sample Implementation:**```java
@RestController
@RequestMapping("/reports")
public class ReportController {
    @GetMapping("/attendance") public AttendanceReport getAttendanceReport(@RequestParam Map<String, String> filters) { ... }
    @GetMapping("/export") public ResponseEntity<Resource> exportReport(@RequestParam String type, @RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
}
```

---

## Section: E16 Mobile Access (PWA)

**Description:**Implements responsive views for clock-in/out, schedules, leave requests, announcements, and offline support via PWA.

**Design Specification:**- **Frontend:**  - PWA manifest, service worker  - Responsive UI (Thymeleaf/React/Vue)- **Backend:**  - REST endpoints for mobile flows  - Offline queue support for clock events- **Integration:**  - Lighthouse PWA score validation

**Sample Implementation:**```yaml
# src/main/resources/static/manifest.json
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
```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @PostMapping("/clock-in") public ResponseEntity<?> mobileClockIn(@RequestBody ClockInRequest req) { ... }
    @GetMapping("/announcements") public List<Announcement> getMobileAnnouncements() { ... }
}
```

---

## Section: E17 Onboarding & Offboarding Workflow

**Description:**Automates provisioning/deprovisioning of accounts, schedules, training, and asset assignments.

**Design Specification:**- **Entities:**  - `OnboardingTask` (id, employeeId, type, status, dueDate)  - `OffboardingTask` (id, employeeId, type, status, dueDate)- **Repository:**  - `OnboardingTaskRepository`, `OffboardingTaskRepository`- **Service:**  - Task generation, workflow automation- **Controller:**  - `/onboarding`, `/offboarding`

**Sample Implementation:**```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type; // Account, Schedule, Training, Asset
    private String status; // Pending, Completed
    private LocalDate dueDate;
}

@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @PostMapping public OnboardingTask createTask(@RequestBody OnboardingTaskDto dto) { ... }
    @GetMapping public List<OnboardingTask> getTasks(@RequestParam Long employeeId) { ... }
}
```

---

## Section: E18 Localization

**Description:**Supports multi-language UI, notification templates, and locale-aware data formatting.

**Design Specification:**- **Configuration:**  - `messages.properties`, `messages_es.properties`, etc.  - Locale resolver bean- **Service:**  - Template localization, user locale preferences- **Controller:**  - Locale switch endpoint

**Sample Implementation:**```java
@Bean
public LocaleResolver localeResolver() {
    SessionLocaleResolver slr = new SessionLocaleResolver();
    slr.setDefaultLocale(Locale.ENGLISH);
    return slr;
}

@RestController
@RequestMapping("/locale")
public class LocaleController {
    @PostMapping("/switch") public void switchLocale(@RequestParam String lang) { ... }
}
```

---

## Section: E19 AI Scheduling

**Description:**Uses AI/ML to optimize shift assignments, predict staffing needs, and resolve conflicts.

**Design Specification:**- **Service:**  - AI engine integration (Python microservice or Java ML lib)  - REST endpoint for schedule optimization- **Entities:**  - `AiScheduleRequest`, `AiScheduleResult`- **Controller:**  - `/ai/schedule/optimize`

**Sample Implementation:**```java
@RestController
@RequestMapping("/ai/schedule")
public class AiScheduleController {
    @PostMapping("/optimize") public AiScheduleResult optimize(@RequestBody AiScheduleRequest req) {
        // Call external AI microservice, return result
    }
}
```

---

## Section: E20 Self-Service Portal

**Description:**Provides employees with a portal to view schedules, request leave, see notifications, and manage personal info.

**Design Specification:**- **Frontend:**  - SPA (React/Angular/Vue) or Thymeleaf- **Backend:**  - REST endpoints for self-service flows- **Entities:**  - `SelfServiceProfile`, `SelfServiceRequest`- **Controller:**  - `/portal/profile`, `/portal/requests`, `/portal/notifications`

**Sample Implementation:**```java
@RestController
@RequestMapping("/portal")
public class PortalController {
    @GetMapping("/profile") public SelfServiceProfile getProfile(@RequestParam Long employeeId) { ... }
    @PostMapping("/requests") public SelfServiceRequest submitRequest(@RequestBody SelfServiceRequestDto dto) { ... }
    @GetMapping("/notifications") public List<Notification> getNotifications(@RequestParam Long employeeId) { ... }
}
```

---

**End of Document**

---

This document covers all 20 Warehouse EMS epics with detailed low-level technical design, following Spring Boot industry standards and best practices.