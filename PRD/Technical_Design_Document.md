# Warehouse Employee Management System â Comprehensive Low-Level Technical Design (Spring Boot)

---

## Table of Contents

1. [EPIC 1 - Project Scaffolding & Domain Setup](#epic-1)
2. [EPIC 2 - Employee Master Data (CRUD)](#epic-2)
3. [EPIC 3 - Role-Based Access Control (RBAC)](#epic-3)
4. [EPIC 4 - Time & Attendance (Clock In/Out)](#epic-4)
5. [EPIC 5 - Shift & Schedule Management](#epic-5)
6. [EPIC 6 - Leave & Absence Management](#epic-6)
7. [EPIC 7 - Training & Certification Tracking](#epic-7)
8. [EPIC 8 - Safety Incidents & OSHA Reporting](#epic-8)
9. [EPIC 9 - Equipment & Asset Assignment](#epic-9)
10. [EPIC 10 - Performance Reviews & Goals](#epic-10)
11. [EPIC 11 - Payroll Export Integration](#epic-11)
12. [EPIC 12 - Notifications & Announcements](#epic-12)
13. [EPIC 13 - Integration Layer (HRIS/WMS APIs)](#epic-13)
14. [EPIC 14 - Audit Trail & Compliance](#epic-14)
15. [EPIC 15 - Reporting & Analytics](#epic-15)
16. [EPIC 16 - Mobile Access (PWA)](#epic-16)
17. [EPIC 17 - Onboarding & Offboarding Workflow](#epic-17)
18. [EPIC 18 - Localization & Multi-Site Support](#epic-18)
19. [EPIC 19 - Advanced Scheduling (AI/Optimization)](#epic-19)
20. [EPIC 20 - Self-Service Portal & Chatbot](#epic-20)

---

## <a name="epic-1"></a>EPIC 1 - Project Scaffolding & Domain Setup

### 1. Overview

- **Spring Boot (Maven) project** with modular structure.
- Core modules: `employee`, `scheduling`, `attendance`, `safety`.
- **Flyway** (or **Liquibase**) for DB migrations.
- **Spring Boot Actuator** for health checks and monitoring.

### 2. Package Structure

```
com.wms
âââ employee
âââ scheduling
âââ attendance
âââ safety
âââ common
âââ config
âââ audit
âââ Application.java
```

### 3. Module Definitions

- **employee**: Employee domain, CRUD, DTOs.
- **scheduling**: Shift templates, assignments.
- **attendance**: Clock in/out, timesheets.
- **safety**: Incidents, certifications.
- **common**: Shared utilities, enums, exceptions.
- **config**: Security, DB, actuator, Flyway.
- **audit**: Audit trail.

### 4. Core Configuration

**application.yml**
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wms
    username: wms_user
    password: secret
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  flyway:
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### 5. Flyway Baseline Migration

**V1__baseline.sql**
```sql
CREATE TABLE employee (...);
CREATE TABLE department (...);
-- etc.
```

### 6. Actuator Health Endpoint

- `/actuator/health` returns `{"status":"UP"}`

### 7. README

- Build/run steps using Maven:
  ```
  mvn clean install
  mvn spring-boot:run
  ```

---

## <a name="epic-2"></a>EPIC 2 - Employee Master Data (CRUD)

### 1. Overview

- Centralized employee record management.
- RESTful CRUD endpoints.
- Soft-delete, pagination, filtering.

### 2. Package Structure

```
com.wms.employee
âââ controller
âââ service
âââ repository
âââ dto
âââ model
```

### 3. Entity Design

**Employee.java**
```java
@Entity
@Table(name = "employee", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
@SQLDelete(sql = "UPDATE employee SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Employee {
    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne
    private Department department;

    private String shiftGroup;

    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    private boolean deleted = false;
    // getters/setters
}
```

### 4. Service Layer

**EmployeeService.java**
```java
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository repo;

    public Employee create(EmployeeDto dto) { ... }
    public Page<Employee> list(Pageable pageable, EmployeeFilter filter) { ... }
    public Employee update(Long id, EmployeeDto dto) { ... }
    public void softDelete(Long id) { ... }
    // etc.
}
```

### 5. Repository Layer

**EmployeeRepository.java**
```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    Optional<Employee> findByBadgeId(String badgeId);
}
```

### 6. Controller

**EmployeeController.java**
```java
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee CRUD APIs")
public class EmployeeController {
    @Autowired
    private EmployeeService service;

    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { ... }

    @GetMapping
    public Page<EmployeeDto> list(@ParameterObject Pageable pageable, EmployeeFilter filter) { ... }

    @GetMapping("/{id}")
    public EmployeeDto get(@PathVariable Long id) { ... }

    @PutMapping("/{id}")
    public EmployeeDto update(@PathVariable Long id, @RequestBody EmployeeDto dto) { ... }

    @PatchMapping("/{id}")
    public EmployeeDto patch(@PathVariable Long id, @RequestBody Map<String, Object> updates) { ... }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { ... }
}
```

### 7. OpenAPI Example

```yaml
paths:
  /employees:
    post:
      requestBody:
        content:
          application/json:
            example:
              name: "Jane Doe"
              badgeId: "B12345"
              role: "WORKER"
              departmentId: 1
              shiftGroup: "A"
              hireDate: "2023-01-01"
              status: "ACTIVE"
```

### 8. Database Schema

| Column      | Type        | Constraints           |
|-------------|-------------|----------------------|
| id          | BIGINT      | PK, auto-increment   |
| name        | VARCHAR     | not null             |
| badge_id    | VARCHAR     | unique, not null     |
| role        | VARCHAR     | not null             |
| department_id| BIGINT     | FK                   |
| shift_group | VARCHAR     |                      |
| hire_date   | DATE        |                      |
| status      | VARCHAR     | not null             |
| deleted     | BOOLEAN     | default false        |

---

## <a name="epic-3"></a>EPIC 3 - Role-Based Access Control (RBAC)

### 1. Overview

- **Spring Security** with roles: ADMIN, HR, SUPERVISOR, WORKER.
- Method/endpoint security, row-level constraints.
- API key/OAuth2 toggle.

### 2. Package Structure

```
com.wms.config
com.wms.security
```

### 3. Security Configuration

**SecurityConfig.java**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${security.mode:oauth2}")
    private String securityMode;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if ("apikey".equals(securityMode)) {
            http.addFilterBefore(new ApiKeyAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        } else {
            http.oauth2ResourceServer().jwt();
        }
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .antMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated();
    }
}
```

### 4. Row-Level Security Example

**EmployeeService.java**
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department.id == principal.departmentId)")
public Employee getEmployee(Long id) { ... }
```

### 5. API Key Filter Example

**ApiKeyAuthFilter.java**
```java
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) {
        // Validate API key from header
    }
}
```

### 6. Security Tests

- Test unauthorized (401), forbidden (403), and allowed access for each role.

---

## <a name="epic-4"></a>EPIC 4 - Time & Attendance (Clock In/Out)

### 1. Overview

- Endpoints for clock-in/out, geofence/device capture.
- Calculate hours worked, handle missed punches, corrections.

### 2. Package Structure

```
com.wms.attendance
âââ controller
âââ service
âââ repository
âââ model
```

### 3. Entity Design

**AttendanceEvent.java**
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private EventType type; // CLOCK_IN, CLOCK_OUT

    private LocalDateTime timestamp;

    private String deviceId;
    private String geoLocation;

    private boolean correctionRequested = false;
    // getters/setters
}
```

### 4. Service Layer

**AttendanceService.java**
```java
@Service
public class AttendanceService {
    public AttendanceEvent clockIn(Long employeeId, String deviceId, String geoLocation) { ... }
    public AttendanceEvent clockOut(Long employeeId, String deviceId, String geoLocation) { ... }
    public List<AttendanceEvent> getDailyEvents(Long employeeId, LocalDate date) { ... }
    public void requestCorrection(Long eventId, CorrectionRequestDto dto) { ... }
}
```

### 5. Controller

**AttendanceController.java**
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public AttendanceEventDto clockIn(@RequestBody ClockEventDto dto) { ... }

    @PostMapping("/clock-out")
    public AttendanceEventDto clockOut(@RequestBody ClockEventDto dto) { ... }

    @PostMapping("/corrections/{eventId}")
    public void requestCorrection(@PathVariable Long eventId, @RequestBody CorrectionRequestDto dto) { ... }
}
```

### 6. Reports

- Export attendance as CSV via `/attendance/report?from=...&to=...`

### 7. Database Schema

| Column         | Type        | Constraints           |
|----------------|-------------|----------------------|
| id             | BIGINT      | PK                   |
| employee_id    | BIGINT      | FK                   |
| type           | VARCHAR     |                      |
| timestamp      | TIMESTAMP   |                      |
| device_id      | VARCHAR     |                      |
| geo_location   | VARCHAR     |                      |
| correction_requested | BOOLEAN | default false       |

---

## <a name="epic-5"></a>EPIC 5 - Shift & Schedule Management

### 1. Overview

- Shift templates, rotations, overtime rules, assignments.
- Blackout dates, operation calendars.

### 2. Package Structure

```
com.wms.scheduling
âââ controller
âââ service
âââ repository
âââ model
```

### 3. Entity Design

**ShiftTemplate.java**
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;

    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    private String recurrencePattern; // e.g., "WEEKLY"
    // etc.
}
```

**ShiftAssignment.java**
```java
@Entity
public class ShiftAssignment {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    @ManyToOne
    private ShiftTemplate shiftTemplate;

    private LocalDate date;
    private boolean overtime;
    // etc.
}
```

### 4. Service Layer

**SchedulingService.java**
```java
@Service
public class SchedulingService {
    public ShiftTemplate createTemplate(ShiftTemplateDto dto) { ... }
    public void assignShift(Long employeeId, Long templateId, LocalDate date) { ... }
    public List<ShiftAssignment> getAssignments(Long employeeId, LocalDate from, LocalDate to) { ... }
    public void detectConflicts(...) { ... }
}
```

### 5. Controller

**SchedulingController.java**
```java
@RestController
@RequestMapping("/scheduling")
public class SchedulingController {
    @PostMapping("/templates")
    public ShiftTemplateDto createTemplate(@RequestBody ShiftTemplateDto dto) { ... }

    @PostMapping("/assignments")
    public void assignShift(@RequestBody ShiftAssignmentDto dto) { ... }

    @GetMapping("/assignments")
    public List<ShiftAssignmentDto> getAssignments(...) { ... }
}
```

### 6. Database Schema

| Column         | Type        | Constraints           |
|----------------|-------------|----------------------|
| id             | BIGINT      | PK                   |
| employee_id    | BIGINT      | FK                   |
| shift_template_id | BIGINT   | FK                   |
| date           | DATE        |                      |
| overtime       | BOOLEAN     |                      |

---

## <a name="epic-6"></a>EPIC 6 - Leave & Absence Management

### 1. Overview

- PTO, sick, unpaid leave requests/approvals.
- Accrual balances, policies.

### 2. Package Structure

```
com.wms.leave
âââ controller
âââ service
âââ repository
âââ model
```

### 3. Entity Design

**LeaveRequest.java**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private LeaveType type; // PTO, SICK, UNPAID

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status; // REQUESTED, APPROVED, DENIED

    private String reason;
    // etc.
}
```

**LeaveBalance.java**
```java
@Entity
public class LeaveBalance {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private LeaveType type;

    private Double accrued;
    private Double used;
}
```

### 4. Service Layer

**LeaveService.java**
```java
@Service
public class LeaveService {
    public LeaveRequest requestLeave(LeaveRequestDto dto) { ... }
    public void approveLeave(Long requestId) { ... }
    public void denyLeave(Long requestId) { ... }
    public LeaveBalance getBalance(Long employeeId, LeaveType type) { ... }
}
```

### 5. Controller

**LeaveController.java**
```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/requests")
    public LeaveRequestDto requestLeave(@RequestBody LeaveRequestDto dto) { ... }

    @PostMapping("/requests/{id}/approve")
    public void approve(@PathVariable Long id) { ... }

    @PostMapping("/requests/{id}/deny")
    public void deny(@PathVariable Long id) { ... }

    @GetMapping("/balances/{employeeId}")
    public List<LeaveBalanceDto> getBalances(@PathVariable Long employeeId) { ... }
}
```

### 6. Database Schema

| Column         | Type        | Constraints           |
|----------------|-------------|----------------------|
| id             | BIGINT      | PK                   |
| employee_id    | BIGINT      | FK                   |
| type           | VARCHAR     |                      |
| start_date     | DATE        |                      |
| end_date       | DATE        |                      |
| status         | VARCHAR     |                      |
| reason         | VARCHAR     |                      |

---

## <a name="epic-7"></a>EPIC 7 - Training & Certification Tracking

### 1. Overview

- Track certifications, expirations, renewals.
- Block assignments if expired.

### 2. Package Structure

```
com.wms.certification
âââ controller
âââ service
âââ repository
âââ model
```

### 3. Entity Design

**Certification.java**
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;

    private String name;
    private String description;
    private boolean required;

    // etc.
}
```

**EmployeeCertification.java**
```java
@Entity
public class EmployeeCertification {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    @ManyToOne
    private Certification certification;

    private LocalDate issueDate;
    private LocalDate expiryDate;

    private String proofDocumentUrl;
}
```

### 4. Service Layer

**CertificationService.java**
```java
@Service
public class CertificationService {
    public EmployeeCertification assignCertification(Long employeeId, Long certId, LocalDate issue, LocalDate expiry) { ... }
    public List<EmployeeCertification> getExpiringCerts(int days) { ... }
    public boolean isQualified(Long employeeId, Long certId) { ... }
}
```

### 5. Controller

**CertificationController.java**
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping("/assign")
    public EmployeeCertificationDto assign(@RequestBody AssignCertificationDto dto) { ... }

    @GetMapping("/expiring")
    public List<EmployeeCertificationDto> getExpiring(@RequestParam int days) { ... }
}
```

### 6. Database Schema

| Column         | Type        | Constraints           |
|----------------|-------------|----------------------|
| id             | BIGINT      | PK                   |
| employee_id    | BIGINT      | FK                   |
| certification_id| BIGINT     | FK                   |
| issue_date     | DATE        |                      |
| expiry_date    | DATE        |                      |
| proof_document_url | VARCHAR |                      |

---

## <a name="epic-8"></a>EPIC 8 - Safety Incidents & OSHA Reporting

### 1. Overview

- Record incidents, workflow for investigation/corrective actions.
- OSHA summary generation.

### 2. Package Structure

```
com.wms.safety
âââ controller
âââ service
âââ repository
âââ model
```

### 3. Entity Design

**SafetyIncident.java**
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
    private IncidentStatus status; // OPEN, INVESTIGATING, RESOLVED

    private LocalDateTime reportedAt;
    // etc.
}
```

### 4. Service Layer

**SafetyService.java**
```java
@Service
public class SafetyService {
    public SafetyIncident reportIncident(SafetyIncidentDto dto) { ... }
    public void updateStatus(Long id, IncidentStatus status) { ... }
    public List<SafetyIncident> getIncidents(...) { ... }
    public OSHAReport generateOSHAReport(...) { ... }
}
```

### 5. Controller

**SafetyController.java**
```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyController {
    @PostMapping
    public SafetyIncidentDto report(@RequestBody SafetyIncidentDto dto) { ... }

    @PatchMapping("/{id}/status")
    public void updateStatus(@PathVariable Long id, @RequestParam IncidentStatus status) { ... }

    @GetMapping("/oshasummary")
    public OSHAReportDto getOSHAReport(...) { ... }
}
```

### 6. Database Schema

| Column         | Type        | Constraints           |
|----------------|-------------|----------------------|
| id             | BIGINT      | PK                   |
| severity       | VARCHAR     |                      |
| location       | VARCHAR     |                      |
| description    | TEXT        |                      |
| status         | VARCHAR     |                      |
| reported_at    | TIMESTAMP   |                      |

---

## <a name="epic-9"></a>EPIC 9 - Equipment & Asset Assignment

### 1. Overview

- Assign assets (scanners, forklifts, PPE) to employees.
- Track check-in/out, asset condition.

### 2. Package Structure

```
com.wms.asset
âââ controller
âââ service
âââ repository
âââ model
```

### 3. Entity Design

**Asset.java**
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;

    private String assetTag;
    private String type; // SCANNER, FORKLIFT, PPE
    private String condition;
    private boolean available;
}
```

**AssetAssignment.java**
```java
@Entity
public class AssetAssignment {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Asset asset;

    @ManyToOne
    private Employee employee;

    private LocalDateTime checkedOutAt;
    private LocalDateTime returnedAt;
    private String conditionOnReturn;
}
```

### 4. Service Layer

**AssetService.java**
```java
@Service
public class AssetService {
    public AssetAssignment checkOut(Long assetId, Long employeeId) { ... }
    public void checkIn(Long assignmentId, String condition) { ... }
    public List<AssetAssignment> getHistory(Long assetId) { ... }
}
```

### 5. Controller

**AssetController.java**
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/checkout")
    public AssetAssignmentDto checkOut(@RequestBody AssetCheckOutDto dto) { ... }

    @PostMapping("/checkin/{assignmentId}")
    public void checkIn(@PathVariable Long assignmentId, @RequestBody CheckInDto dto) { ... }

    @GetMapping("/{assetId}/history")
    public List<AssetAssignmentDto> getHistory(@PathVariable Long assetId) { ... }
}
```

### 6. Database Schema

| Column         | Type        | Constraints           |
|----------------|-------------|----------------------|
| id             | BIGINT      | PK                   |
| asset_id       | BIGINT      | FK                   |
| employee_id    | BIGINT      | FK                   |
| checked_out_at | TIMESTAMP   |                      |
| returned_at    | TIMESTAMP   |                      |
| condition_on_return | VARCHAR|                      |

---

## <a name="epic-10"></a>EPIC 10 - Performance Reviews & Goals

### 1. Overview

- Review templates, goals, ratings, comments.
- Supervisor/employee acknowledgements.

### 2. Package Structure

```
com.wms.performance
âââ controller
âââ service
âââ repository
âââ model
```

### 3. Entity Design

**PerformanceReview.java**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    private String cycle; // Q1-2024, 2024-Annual, etc.

    @ElementCollection
    private Map<String, Integer> competencies; // e.g., "Teamwork": 4

    private String goals;
    private String comments;

    private boolean employeeAcknowledged;
    private boolean supervisorAcknowledged;

    private LocalDateTime signedOffAt;
}
```

### 4. Service Layer

**PerformanceService.java**
```java
@Service
public class PerformanceService {
    public PerformanceReview createReview(PerformanceReviewDto dto) { ... }
    public void acknowledge(Long reviewId, boolean byEmployee) { ... }
    public PerformanceReview exportPdf(Long reviewId) { ... }
}
```

### 5. Controller

**PerformanceController.java**
```java
@RestController
@RequestMapping("/performance")
public class PerformanceController {
    @PostMapping("/reviews")
    public PerformanceReviewDto create(@RequestBody PerformanceReviewDto dto) { ... }

    @PostMapping("/reviews/{id}/acknowledge")
    public void acknowledge(@PathVariable Long id, @RequestParam boolean byEmployee) { ... }

    @GetMapping("/reviews/{id}/export")
    public ResponseEntity<Resource> exportPdf(@PathVariable Long id) { ... }
}
```

### 6. Database Schema

| Column         | Type        | Constraints           |
|----------------|-------------|----------------------|
| id             | BIGINT      | PK                   |
| employee_id    | BIGINT      | FK                   |
| cycle          | VARCHAR     |                      |
| competencies   | JSONB       |                      |
| goals          | TEXT        |                      |
| comments       | TEXT        |                      |
| employee_acknowledged | BOOLEAN |                  |
| supervisor_acknowledged | BOOLEAN |                |
| signed_off_at  | TIMESTAMP   |                      |

---

## <a name="epic-11"></a>EPIC 11 - Payroll Export Integration

### 1. Overview

- Generate payroll files from attendance/leave.
- Map to provider formats, secure delivery.

### 2. Package Structure

```
com.wms.payroll
âââ service
âââ integration
âââ model
```

### 3. Service Layer

**PayrollExportService.java**
```java
@Service
public class PayrollExportService {
    public PayrollFile generatePayroll(LocalDate from, LocalDate to) { ... }
    public void deliverPayroll(PayrollFile file) { ... }
    public void retryFailedDeliveries() { ... }
}
```

### 4. Integration

- SFTP delivery via `JSch` or Spring Integration.
- API delivery via `RestTemplate` or `WebClient`.

### 5. Audit Logging

- Every export is logged with status, timestamp, actor.

### 6. Database Schema

| Column         | Type        | Constraints           |
|----------------|-------------|----------------------|
| id             | BIGINT      | PK                   |
| file_name      | VARCHAR     |                      |
| status         | VARCHAR     |                      |
| delivered_at   | TIMESTAMP   |                      |
| retry_count    | INT         |                      |

---

## <a name="epic-12"></a>EPIC 12 - Notifications & Announcements

### 1. Overview

- In-app, email, SMS notifications.
- Quiet hours, opt-in/out, templates.

### 2. Package Structure

```
com.wms.notification
âââ controller
âââ service
âââ repository
âââ model
```

### 3. Entity Design

**Notification.java**
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee recipient;

    private String channel; // EMAIL, SMS, IN_APP
    private String template;
    private String payload;
    private boolean delivered;
    private LocalDateTime deliveredAt;
}
```

### 4. Service Layer

**NotificationService.java**
```java
@Service
public class NotificationService {
    public void sendNotification(NotificationDto dto) { ... }
    public void trackDelivery(Long notificationId, boolean delivered) { ... }
    public void applyRateLimits(...) { ... }
}
```

### 5. Controller

**NotificationController.java**
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping
    public void send(@RequestBody NotificationDto dto) { ... }

    @GetMapping("/announcements")
    public List<AnnouncementDto> getAnnouncements() { ... }
}
```

### 6. Database Schema

| Column         | Type        | Constraints           |
|----------------|-------------|----------------------|
| id             | BIGINT      | PK                   |
| recipient_id   | BIGINT      | FK                   |
| channel        | VARCHAR     |                      |
| template       | VARCHAR     |                      |
| payload        | TEXT        |                      |
| delivered      | BOOLEAN     |                      |
| delivered_at   | TIMESTAMP   |                      |

---

## <a name="epic-13"></a>EPIC 13 - Integration Layer (HRIS/WMS APIs)

### 1. Overview

- REST APIs/connectors for HRIS, WMS, IDP (SSO).
- Webhooks for events.

### 2. Package Structure

```
com.wms.integration
âââ controller
âââ service
âââ client
```

### 3. API Security

- JWT/OAuth2 for all integration endpoints.

### 4. HRIS Sync Example

**HRISClient.java**
```java
@Component
public class HRISClient {
    public EmployeeDto fetchNewHires() { ... }
    public void syncEmployee(EmployeeDto dto) { ... }
}
```

### 5. Webhook Controller

**WebhookController.java**
```java
@RestController
@RequestMapping("/webhooks")
public class WebhookController {
    @PostMapping("/employee")
    public void onEmployeeEvent(@RequestBody EmployeeEventDto dto) { ... }
}
```

### 6. OpenAPI Documentation

- All endpoints documented with OpenAPI annotations.

---

## <a name="epic-14"></a>EPIC 14 - Audit Trail & Compliance

### 1. Overview

- Centralized audit logging for sensitive changes.
- Tamper-evident storage.

### 2. Package Structure

```
com.wms.audit
âââ service
âââ repository
âââ model
```

### 3. Entity Design

**AuditLog.java**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;

    private String entity;
    private Long entityId;
    private String action; // CREATE, UPDATE, DELETE
    private String actor;
    private LocalDateTime timestamp;
    private String beforeState;
    private String afterState;
}
```

### 4. Service Layer

**AuditService.java**
```java
@Service
public class AuditService {
    public void logChange(String entity, Long id, String action, String actor, Object before, Object after) { ... }
}
```

### 5. Tamper-Evident Storage

- Hash chain or append-only log table.

### 6. Export

- Export logs by date/user/entity.

---

## <a name="epic-15"></a>EPIC 15 - Reporting & Analytics

### 1. Overview

- Operational reports: attendance, overtime, leave, certs, safety KPIs.
- CSV/PDF export, dashboards.

### 2. Package Structure

```
com.wms.reporting
âââ controller
âââ service
```

### 3. Service Layer

**ReportingService.java**
```java
@Service
public class ReportingService {
    public Report generateAttendanceReport(...) { ... }
    public Report generateOvertimeReport(...) { ... }
    public Report generateLeaveBalanceReport(...) { ... }
    public Report generateCertificationStatusReport(...) { ... }
    public Report generateSafetyKPIReport(...) { ... }
}
```

### 4. Controller

**ReportingController.java**
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> attendanceReport(...) { ... }

    @GetMapping("/overtime")
    public ResponseEntity<Resource> overtimeReport(...) { ... }
    // etc.
}
```

---

## <a name="epic-16"></a>EPIC 16 - Mobile Access (PWA)

### 1. Overview

- Responsive views for clock-in/out, schedules, leave, announcements.
- Offline-friendly via PWA.

### 2. PWA Manifest

- `manifest.json` with app name, icons, start_url.

### 3. Service Worker

- `service-worker.js` caches core flows, queues offline clock events.

### 4. Backend Support

- Endpoints for mobile flows (clock-in/out, schedules, leave, announcements).
- Conflict resolution for offline events.

---

## <a name="epic-17"></a>EPIC 17 - Onboarding & Offboarding Workflow

### 1. Overview

- Automate provisioning, initial schedule, training.
- Deprovision access/assets on termination.

### 2. Package Structure

```
com.wms.onboarding
âââ service
```

### 3. Service Layer

**OnboardingService.java**
```java
@Service
public class OnboardingService {
    public void onboardEmployee(EmployeeDto dto) { ... }
    public void offboardEmployee(Long employeeId) { ... }
}
```

### 4. Integration

- Triggers HRIS sync, asset assignment, training tasks.

---

## <a name="epic-18"></a>EPIC 18 - Localization & Multi-Site Support

### 1. Overview

- Multiple warehouses/sites, i18n for UI/notifications.

### 2. Entity Design

**Site.java**
```java
@Entity
public class Site {
    @Id @GeneratedValue
    private Long id;

    private String name;
    private String timeZone;
    private String locale;
    // etc.
}
```

**Employee.java** (add site)
```java
@ManyToOne
private Site site;
```

### 3. i18n

- UI strings externalized in `messages_{locale}.properties`.
- Date/time formatting locale-aware.

---

## <a name="epic-19"></a>EPIC 19 - Advanced Scheduling (AI/Optimization)

### 1. Overview

- ML-based shift recommendations, constraint solver.

### 2. Service Layer

**SchedulingOptimizationService.java**
```java
@Service
public class SchedulingOptimizationService {
    public List<ScheduleRecommendation> recommendShifts(SchedulingRequestDto dto) { ... }
}
```

### 3. Controller

**OptimizationController.java**
```java
@RestController
@RequestMapping("/scheduling/optimization")
public class OptimizationController {
    @PostMapping("/recommend")
    public List<ScheduleRecommendationDto> recommend(@RequestBody SchedulingRequestDto dto) { ... }
}
```

### 4. Fallback

- If ML model unavailable, fallback to manual scheduling.

---

## <a name="epic-20"></a>EPIC 20 - Self-Service Portal & Chatbot

### 1. Overview

- Employee portal for profile, schedule swaps, FAQs.
- Chatbot for common queries.

### 2. Portal

- Accessible via SSO.
- Endpoints for profile update, swap requests, FAQs.

### 3. Chatbot

- `/chatbot/ask` endpoint.
- Integrate with NLP service (e.g., Dialogflow).

**ChatbotController.java**
```java
@RestController
@RequestMapping("/chatbot")
public class ChatbotController {
    @PostMapping("/ask")
    public ChatbotResponseDto ask(@RequestBody ChatbotRequestDto dto) { ... }
}
```

### 4. Analytics

- Track usage, fallback to human support.

---

# Appendix

- All code snippets use Spring Boot 2.x/3.x conventions.
- All entities use JPA annotations.
- All services are annotated with `@Service`, repositories with `@Repository`.
- All controllers use `@RestController` and are documented with OpenAPI annotations.
- Security is enforced via `@PreAuthorize` and endpoint configuration.
- Database schema is normalized, with foreign keys and constraints as described.

---

**This document provides a detailed, actionable low-level technical design for all 20 epics of the Warehouse Employee Management System, ready for Spring Boot developer consumption.**