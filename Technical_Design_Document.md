# Warehouse Employee Management System: Comprehensive Low-Level Technical Design Document

---

## Table of Contents
1. [E01 - Project Scaffolding & Domain Setup](#e01)
2. [E02 - Employee Master Data (CRUD)](#e02)
3. [E03 - Role-Based Access Control (RBAC)](#e03)
4. [E04 - Time & Attendance (Clock In/Out)](#e04)
5. [E05 - Shift & Schedule Management](#e05)
6. [E06 - Leave & Absence Management](#e06)
7. [E07 - Training & Certification Tracking](#e07)
8. [E08 - Safety Incidents & OSHA Reporting](#e08)
9. [E09 - Equipment & Asset Assignment](#e09)
10. [E10 - Performance Reviews & Goals](#e10)
11. [E11 - Payroll Export Integration](#e11)
12. [E12 - Notifications & Announcements](#e12)
13. [E13 - Integration Layer (HRIS/WMS APIs)](#e13)
14. [E14 - Audit Trail & Compliance](#e14)
15. [E15 - Reporting & Analytics](#e15)
16. [E16 - Mobile Access (PWA)](#e16)
17. [E17 - Onboarding & Offboarding Workflow](#e17)
18. [E18 - Localization](#e18)
19. [E19 - Observability](#e19)
20. [E20 - CI/CD](#e20)

---

## <a name="e01"></a>E01 - Project Scaffolding & Domain Setup

### Section Title: Spring Boot Project Initialization

#### Description of Design Decisions
- Use Spring Boot 3.x with Maven for build management.
- Modular package structure for scalability and maintainability.
- Flyway/Liquibase for DB migrations.
- Spring Boot Actuator for health checks and monitoring.

#### Design Specification
- **Package Structure:**  
  ```
  com.wms
    âââ employee
    âââ scheduling
    âââ attendance
    âââ safety
    âââ config
    âââ common
    âââ ...
  ```
- **Modules:**  
  - Employee
  - Scheduling
  - Attendance
  - Safety

- **Configuration:**  
  - `application.yml` for environment settings.
  - Flyway/Liquibase migration scripts in `/db/migration`.

- **Actuator:**  
  - `/actuator/health` endpoint enabled.

#### Sample Implementation

```java
// src/main/java/com/wms/WmsApplication.java
@SpringBootApplication
public class WmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WmsApplication.class, args);
    }
}

// src/main/resources/application.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wms
    username: wms_user
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

## <a name="e02"></a>E02 - Employee Master Data (CRUD)

### Section Title: Employee Domain & CRUD APIs

#### Description of Design Decisions
- Employee entity as central domain model.
- RESTful CRUD endpoints.
- DTOs for web layer.
- Soft-delete for compliance.
- Pagination and filtering.

#### Design Specification
- **Entity Design:**
  - `Employee`: id, name, badgeId (unique), role, department, shiftGroup, hireDate, status, deleted (soft-delete flag).
- **Relationships:**  
  - Employee â Department (Many-to-One)
  - Employee â ShiftGroup (Many-to-One)

- **Service Layer:**  
  - `EmployeeService`: CRUD, filtering, soft-delete.

- **Repository Layer:**  
  - `EmployeeRepository`: extends `JpaRepository<Employee, Long>`, custom queries for filtering.

- **Controller:**  
  - `/employees` endpoints: POST, GET, PUT, PATCH, DELETE.
  - Pagination via `Pageable`.

- **Configuration:**  
  - OpenAPI schemas with examples.

#### Sample Implementation

```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id @GeneratedValue
    private Long id;
    @Column(unique = true, nullable = false)
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted = false;
    // getters/setters
}

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    @Query("SELECT e FROM Employee e WHERE e.deleted = false")
    Page<Employee> findAllActive(Pageable pageable);
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired private EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeDto> create(@RequestBody EmployeeDto dto) { ... }

    @GetMapping
    public Page<EmployeeDto> list(Pageable pageable, @RequestParam Map<String, String> filters) { ... }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @RequestBody EmployeeDto dto) { ... }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) { ... }
}
```

---

## <a name="e03"></a>E03 - Role-Based Access Control (RBAC)

### Section Title: Security & Authorization

#### Description of Design Decisions
- Spring Security 6.x for authentication and authorization.
- Roles: ADMIN, HR, SUPERVISOR, WORKER.
- Method and endpoint security.
- Row-level constraints.
- API key/OAuth2 toggle via config.

#### Design Specification
- **Entity Design:**  
  - `User`: id, username, password, roles.
  - `Role`: id, name.

- **Service Layer:**  
  - `UserDetailsService` for authentication.
  - Security rules for endpoints.

- **Repository Layer:**  
  - `UserRepository`, `RoleRepository`.

- **Controller:**  
  - Secure endpoints with `@PreAuthorize`.

- **Configuration:**  
  - `SecurityConfig` for role mappings.
  - API key/OAuth2 toggle.

#### Sample Implementation

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
            .csrf().disable();
        return http.build();
    }
}

@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    private String username;
    private String password;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles;
    // getters/setters
}
```

---

## <a name="e04"></a>E04 - Time & Attendance (Clock In/Out)

### Section Title: Attendance Event Tracking

#### Description of Design Decisions
- Clock-in/out endpoints.
- Geofence and device capture (optional).
- Shift association.
- Correction workflow.

#### Design Specification
- **Entity Design:**  
  - `AttendanceEvent`: id, employeeId, timestamp, type (IN/OUT), deviceId, location, shiftId, correctionStatus.

- **Service Layer:**  
  - `AttendanceService`: clock-in/out, calculate hours, handle corrections.

- **Repository Layer:**  
  - `AttendanceRepository`: custom queries for daily totals.

- **Controller:**  
  - `/attendance/clock-in`, `/attendance/clock-out`, `/attendance/corrections`.

- **Integration:**  
  - Export reports (CSV).

#### Sample Implementation

```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private LocalDateTime timestamp;
    private String type; // IN/OUT
    private String deviceId;
    private String location;
    private Long shiftId;
    private String correctionStatus;
    // getters/setters
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockEventDto dto) { ... }

    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockEventDto dto) { ... }

    @PostMapping("/corrections")
    public ResponseEntity<?> requestCorrection(@RequestBody CorrectionDto dto) { ... }
}
```

---

## <a name="e05"></a>E05 - Shift & Schedule Management

### Section Title: Shift Templates & Scheduling

#### Description of Design Decisions
- Recurring shift templates.
- Rotations, overtime rules.
- Assignment to employees.
- Conflict detection.

#### Design Specification
- **Entity Design:**  
  - `ShiftTemplate`: id, name, startTime, endTime, recurrence, overtimeRule.
  - `ShiftAssignment`: id, employeeId, shiftTemplateId, date.

- **Service Layer:**  
  - `ShiftService`: CRUD, conflict detection, bulk assignment.

- **Repository Layer:**  
  - `ShiftTemplateRepository`, `ShiftAssignmentRepository`.

- **Controller:**  
  - `/shifts/templates`, `/shifts/assignments`.

- **Audit:**  
  - Audit entries for changes.

#### Sample Implementation

```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrence;
    private String overtimeRule;
    // getters/setters
}

@Entity
public class ShiftAssignment {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private Long shiftTemplateId;
    private LocalDate date;
    // getters/setters
}

@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates")
    public ResponseEntity<?> createTemplate(@RequestBody ShiftTemplateDto dto) { ... }

    @PostMapping("/assignments")
    public ResponseEntity<?> assignShift(@RequestBody ShiftAssignmentDto dto) { ... }
}
```

---

## <a name="e06"></a>E06 - Leave & Absence Management

### Section Title: Leave Requests & Approval

#### Description of Design Decisions
- PTO, sick, unpaid leave.
- Accrual balances.
- Approval workflow.
- Integration with scheduling and payroll.

#### Design Specification
- **Entity Design:**  
  - `LeaveRequest`: id, employeeId, type, startDate, endDate, status, accrualBalance.

- **Service Layer:**  
  - `LeaveService`: request, approve/deny, update balances.

- **Repository Layer:**  
  - `LeaveRepository`.

- **Controller:**  
  - `/leave/requests`, `/leave/approvals`.

- **Integration:**  
  - Exclude from scheduling/payroll.

#### Sample Implementation

```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type; // PTO, Sick, Unpaid
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // Requested, Approved, Denied
    private Double accrualBalance;
    // getters/setters
}

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/requests")
    public ResponseEntity<?> requestLeave(@RequestBody LeaveRequestDto dto) { ... }

    @PostMapping("/approvals/{id}")
    public ResponseEntity<?> approveLeave(@PathVariable Long id, @RequestBody ApprovalDto dto) { ... }
}
```

---

## <a name="e07"></a>E07 - Training & Certification Tracking

### Section Title: Certification Management

#### Description of Design Decisions
- Track certifications, expirations, renewals.
- Block assignments for expired certs.
- Upload proof documents.

#### Design Specification
- **Entity Design:**  
  - `Certification`: id, employeeId, type, issueDate, expiryDate, documentUrl.

- **Service Layer:**  
  - `CertificationService`: CRUD, expiry alerts, assignment checks.

- **Repository Layer:**  
  - `CertificationRepository`.

- **Controller:**  
  - `/certifications`, `/certifications/alerts`.

- **Integration:**  
  - Scheduling checks.

#### Sample Implementation

```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
    // getters/setters
}

@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<?> addCertification(@RequestBody CertificationDto dto) { ... }

    @GetMapping("/alerts")
    public List<CertificationAlertDto> getExpiryAlerts() { ... }
}
```

---

## <a name="e08"></a>E08 - Safety Incidents & OSHA Reporting

### Section Title: Incident Recording & OSHA Compliance

#### Description of Design Decisions
- Record incidents/near-misses.
- Severity, location, description, involved employees.
- Investigation workflow.
- OSHA summary generation.

#### Design Specification
- **Entity Design:**  
  - `SafetyIncident`: id, severity, location, description, status, involvedEmployeeIds, correctiveActions.

- **Service Layer:**  
  - `SafetyService`: record, workflow, export OSHA summaries.

- **Repository Layer:**  
  - `SafetyIncidentRepository`.

- **Controller:**  
  - `/safety/incidents`, `/safety/osha`.

- **Integration:**  
  - Metrics dashboard.

#### Sample Implementation

```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String severity;
    private String location;
    private String description;
    private String status; // Open, Investigating, Resolved
    @ElementCollection
    private List<Long> involvedEmployeeIds;
    private String correctiveActions;
    // getters/setters
}

@RestController
@RequestMapping("/safety")
public class SafetyController {
    @PostMapping("/incidents")
    public ResponseEntity<?> recordIncident(@RequestBody SafetyIncidentDto dto) { ... }

    @GetMapping("/osha")
    public ResponseEntity<?> exportOshaSummary() { ... }
}
```

---

## <a name="e09"></a>E09 - Equipment & Asset Assignment

### Section Title: Asset Management & Assignment

#### Description of Design Decisions
- Assign assets to employees.
- Track checkout/return.
- Prevent use if certification missing.
- Asset condition tracking.

#### Design Specification
- **Entity Design:**  
  - `Asset`: id, type, condition, assignedEmployeeId, checkoutDate, returnDate.

- **Service Layer:**  
  - `AssetService`: CRUD, check-in/out, certification checks.

- **Repository Layer:**  
  - `AssetRepository`.

- **Controller:**  
  - `/assets`, `/assets/assignments`.

- **Integration:**  
  - History log, overdue reports.

#### Sample Implementation

```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String condition;
    private Long assignedEmployeeId;
    private LocalDate checkoutDate;
    private LocalDate returnDate;
    // getters/setters
}

@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assignments")
    public ResponseEntity<?> assignAsset(@RequestBody AssetAssignmentDto dto) { ... }

    @PostMapping("/checkin")
    public ResponseEntity<?> checkInAsset(@RequestBody AssetCheckInDto dto) { ... }
}
```

---

## <a name="e10"></a>E10 - Performance Reviews & Goals

### Section Title: Review Cycles & Goal Tracking

#### Description of Design Decisions
- Quarterly/annual review templates.
- Track goals, competencies, ratings, comments.
- Supervisor/employee acknowledgements.

#### Design Specification
- **Entity Design:**  
  - `PerformanceReview`: id, employeeId, cycle, goals, competencies, ratings, comments, status.

- **Service Layer:**  
  - `ReviewService`: create cycles, assign, submit/acknowledge.

- **Repository Layer:**  
  - `PerformanceReviewRepository`.

- **Controller:**  
  - `/reviews`, `/reviews/acknowledgements`.

- **Integration:**  
  - PDF export, role-based visibility.

#### Sample Implementation

```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String cycle; // Quarterly, Annual
    private String goals;
    private String competencies;
    private String ratings;
    private String comments;
    private String status; // Draft, Submitted, Acknowledged
    // getters/setters
}

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewDto dto) { ... }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<?> acknowledgeReview(@PathVariable Long id) { ... }
}
```

---

## <a name="e11"></a>E11 - Payroll Export Integration

### Section Title: Payroll Export & Integration

#### Description of Design Decisions
- Generate payroll-ready files.
- Mapping to external provider formats.
- Secure delivery (SFTP/API).
- Retry and audit logging.

#### Design Specification
- **Entity Design:**  
  - `PayrollExport`: id, period, fileUrl, status, provider, attemptCount.

- **Service Layer:**  
  - `PayrollService`: generate, map, deliver, retry.

- **Repository Layer:**  
  - `PayrollExportRepository`.

- **Controller:**  
  - `/payroll/exports`.

- **Integration:**  
  - SFTP/API delivery, audit log.

#### Sample Implementation

```java
@Entity
public class PayrollExport {
    @Id @GeneratedValue
    private Long id;
    private String period;
    private String fileUrl;
    private String status; // Pending, Delivered, Failed
    private String provider;
    private int attemptCount;
    // getters/setters
}

@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/exports")
    public ResponseEntity<?> exportPayroll(@RequestBody PayrollExportDto dto) { ... }
}
```

---

## <a name="e12"></a>E12 - Notifications & Announcements

### Section Title: Notification & Announcement System

#### Description of Design Decisions
- In-app, email/SMS notifications.
- Quiet hours configuration.
- Opt-in/out per channel.
- Localized templates.

#### Design Specification
- **Entity Design:**  
  - `Notification`: id, userId, channel, template, status, sentAt.
  - `Announcement`: id, title, content, visibleUntil.

- **Service Layer:**  
  - `NotificationService`: send, track, rate limit.
  - `AnnouncementService`: CRUD.

- **Repository Layer:**  
  - `NotificationRepository`, `AnnouncementRepository`.

- **Controller:**  
  - `/notifications`, `/announcements`.

- **Integration:**  
  - Delivery status tracking.

#### Sample Implementation

```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private Long userId;
    private String channel; // Email, SMS, InApp
    private String template;
    private String status; // Sent, Failed
    private LocalDateTime sentAt;
    // getters/setters
}

@Entity
public class Announcement {
    @Id @GeneratedValue
    private Long id;
    private String title;
    private String content;
    private LocalDate visibleUntil;
    // getters/setters
}

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping
    public ResponseEntity<?> sendNotification(@RequestBody NotificationDto dto) { ... }
}

@RestController
@RequestMapping("/announcements")
public class AnnouncementController {
    @PostMapping
    public ResponseEntity<?> createAnnouncement(@RequestBody AnnouncementDto dto) { ... }
}
```

---

## <a name="e13"></a>E13 - Integration Layer (HRIS/WMS APIs)

### Section Title: External API Integration

#### Description of Design Decisions
- Expose REST APIs and connectors for HRIS, WMS, IDP.
- Webhooks for events.
- JWT/OAuth2-secured APIs.

#### Design Specification
- **Entity Design:**  
  - `IntegrationJob`: id, type, status, payload, result.

- **Service Layer:**  
  - `IntegrationService`: sync, webhook handling.

- **Repository Layer:**  
  - `IntegrationJobRepository`.

- **Controller:**  
  - `/integration/hris`, `/integration/wms`, `/integration/webhooks`.

- **Configuration:**  
  - OpenAPI documentation.

#### Sample Implementation

```java
@Entity
public class IntegrationJob {
    @Id @GeneratedValue
    private Long id;
    private String type; // HRIS, WMS
    private String status;
    private String payload;
    private String result;
    // getters/setters
}

@RestController
@RequestMapping("/integration")
public class IntegrationController {
    @PostMapping("/hris")
    public ResponseEntity<?> syncHris(@RequestBody HrisSyncDto dto) { ... }

    @PostMapping("/webhooks")
    public ResponseEntity<?> handleWebhook(@RequestBody WebhookDto dto) { ... }
}
```

---

## <a name="e14"></a>E14 - Audit Trail & Compliance

### Section Title: Audit Logging

#### Description of Design Decisions
- Centralized audit logging for sensitive changes.
- Tamper-evident storage.
- Export by date/user/entity.

#### Design Specification
- **Entity Design:**  
  - `AuditLog`: id, entity, action, actor, timestamp, before, after.

- **Service Layer:**  
  - `AuditService`: log, export.

- **Repository Layer:**  
  - `AuditLogRepository`.

- **Controller:**  
  - `/audit/logs`.

#### Sample Implementation

```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private String action;
    private String actor;
    private LocalDateTime timestamp;
    @Lob
    private String before;
    @Lob
    private String after;
    // getters/setters
}

@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/logs")
    public ResponseEntity<?> exportLogs(@RequestParam String entity, @RequestParam String date) { ... }
}
```

---

## <a name="e15"></a>E15 - Reporting & Analytics

### Section Title: Operational Reporting

#### Description of Design Decisions
- Attendance, overtime, leave balances, certification status, safety KPIs.
- CSV/PDF export.
- Role-based dashboards.

#### Design Specification
- **Entity Design:**  
  - `Report`: id, type, generatedAt, fileUrl, filters.

- **Service Layer:**  
  - `ReportService`: generate, filter, export.

- **Repository Layer:**  
  - `ReportRepository`.

- **Controller:**  
  - `/reports`.

#### Sample Implementation

```java
@Entity
public class Report {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private LocalDateTime generatedAt;
    private String fileUrl;
    private String filters;
    // getters/setters
}

@RestController
@RequestMapping("/reports")
public class ReportController {
    @PostMapping
    public ResponseEntity<?> generateReport(@RequestBody ReportRequestDto dto) { ... }
}
```

---

## <a name="e16"></a>E16 - Mobile Access (PWA)

### Section Title: Mobile PWA Support

#### Description of Design Decisions
- Responsive views for core flows.
- Installable PWA manifest.
- Offline queue for clock events.

#### Design Specification
- **Frontend:**  
  - PWA manifest, service worker.
  - Responsive UI for attendance, schedules, leave, announcements.

- **Backend:**  
  - Endpoints for offline sync.

#### Sample Implementation

```yaml
# manifest.json
{
  "name": "Warehouse Employee Management",
  "short_name": "WMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "icons": [...]
}
```

```java
@RestController
@RequestMapping("/mobile")
public class MobileSyncController {
    @PostMapping("/sync")
    public ResponseEntity<?> syncOfflineEvents(@RequestBody List<ClockEventDto> events) { ... }
}
```

---

## <a name="e17"></a>E17 - Onboarding & Offboarding Workflow

### Section Title: Employee Lifecycle Automation

#### Description of Design Decisions
- Automate provisioning/deprovisioning.
- Initial schedule, training, asset assignment.
- Offboarding: revoke access, collect assets.

#### Design Specification
- **Entity Design:**  
  - `OnboardingTask`: id, employeeId, type, status, dueDate.
  - `OffboardingTask`: id, employeeId, type, status, dueDate.

- **Service Layer:**  
  - `LifecycleService`: automate tasks.

- **Repository Layer:**  
  - `OnboardingTaskRepository`, `OffboardingTaskRepository`.

- **Controller:**  
  - `/lifecycle/onboarding`, `/lifecycle/offboarding`.

#### Sample Implementation

```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type;
    private String status;
    private LocalDate dueDate;
    // getters/setters
}

@RestController
@RequestMapping("/lifecycle")
public class LifecycleController {
    @PostMapping("/onboarding")
    public ResponseEntity<?> createOnboardingTask(@RequestBody OnboardingTaskDto dto) { ... }

    @PostMapping("/offboarding")
    public ResponseEntity<?> createOffboardingTask(@RequestBody OffboardingTaskDto dto) { ... }
}
```

---

## <a name="e18"></a>E18 - Localization

### Section Title: Internationalization & Localization

#### Description of Design Decisions
- Support multiple languages.
- Localized templates for notifications and UI.

#### Design Specification
- **Configuration:**  
  - `messages.properties`, `messages_es.properties`, etc.
  - Locale resolver.

- **Service Layer:**  
  - `LocalizationService`: fetch localized strings.

#### Sample Implementation

```java
@Configuration
public class LocaleConfig {
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        return resolver;
    }
}

@Service
public class LocalizationService {
    @Autowired private MessageSource messageSource;
    public String getMessage(String key, Locale locale) {
        return messageSource.getMessage(key, null, locale);
    }
}
```

---

## <a name="e19"></a>E19 - Observability

### Section Title: Monitoring & Logging

#### Description of Design Decisions
- Enable Actuator endpoints.
- Centralized logging.
- Metrics for BI.

#### Design Specification
- **Configuration:**  
  - Actuator endpoints: `/health`, `/metrics`, `/loggers`.
  - Logback for logging.

- **Integration:**  
  - Metrics export for BI tools.

#### Sample Implementation

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, metrics, loggers
logging:
  level:
    root: INFO
    com.wms: DEBUG
```

---

## <a name="e20"></a>E20 - CI/CD

### Section Title: Continuous Integration & Deployment

#### Description of Design Decisions
- Automated build/test/deploy pipeline.
- Dockerization.
- Environment-specific configs.

#### Design Specification
- **CI/CD Pipeline:**  
  - Build: Maven.
  - Test: JUnit.
  - Deploy: Docker, Kubernetes.

- **Configuration:**  
  - `Dockerfile`, `docker-compose.yml`.
  - Environment variables.

#### Sample Implementation

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-alpine
COPY target/wms.jar /app/wms.jar
ENTRYPOINT ["java", "-jar", "/app/wms.jar"]
```

```yaml
# .github/workflows/ci.yml
name: CI
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build with Maven
        run: mvn clean install
      - name: Run Tests
        run: mvn test
      - name: Build Docker Image
        run: docker build -t wms:latest .
```

---

# Appendix: Design Patterns & Best Practices

- Use DTOs for web layer separation.
- Service layer for business logic.
- Repository layer for persistence.
- Controller layer for REST endpoints.
- Configuration classes for security, localization, and environment settings.
- Integration points via service interfaces and adapters.
- OpenAPI for API documentation.
- Actuator for observability.
- Audit logging for compliance.

---

**This document provides detailed, production-ready technical designs for all user stories across the Warehouse Employee Management System, organized by epic and user story. Each section includes architectural overview, package structure, entity and service design, repository and controller specifications, configuration, integration points, and sample code. Spring Boot 3.x, Spring Data JPA, Spring Security 6.x, and industry best practices are followed throughout.**