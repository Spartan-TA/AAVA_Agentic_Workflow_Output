# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

---

## Table of Contents

- [E01 Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
- [E02 Employee Master Data (CRUD)](#e02-employee-master-data-crud)
- [E03 Role Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
- [E04 Time & Attendance (Clock In/Out)](#e04-time--attendance-clock-inout)
- [E05 Shift & Schedule Management](#e05-shift--schedule-management)
- [E06 Leave & Absence Management](#e06-leave--absence-management)
- [E07 Training & Certification Tracking](#e07-training--certification-tracking)
- [E08 Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
- [E09 Equipment & Asset Assignment](#e09-equipment--asset-assignment)
- [E10 Performance Reviews & Goals](#e10-performance-reviews--goals)
- [E11 Payroll Export Integration](#e11-payroll-export-integration)
- [E12 Notifications & Announcements](#e12-notifications--announcements)
- [E13 Integration Layer (HRIS/WMS APIs)](#e13-integration-layer-hriswms-apis)
- [E14 Audit Trail & Compliance](#e14-audit-trail--compliance)
- [E15 Reporting & Analytics](#e15-reporting--analytics)
- [E16 Mobile Access (PWA)](#e16-mobile-access-pwa)
- [E17 Onboarding & Offboarding Workflow](#e17-onboarding--offboarding-workflow)
- [E18 Localization](#e18-localization)
- [E19 AI Scheduling](#e19-ai-scheduling)
- [E20 Document Management](#e20-document-management)

---

## Naming Conventions

- **Base Package:** `com.warehouse.ems`
- **Modules:** `employee`, `attendance`, `shift`, `leave`, `training`, `safety`, `equipment`, `review`, `payroll`, `notification`, `integration`, `audit`, `reporting`, `mobile`, `onboarding`, `localization`, `scheduling`, `document`
- **DTOs:** `*Dto`
- **Repositories:** `*Repository`
- **Services:** `*Service`
- **Controllers:** `*Controller`
- **Entities:** `*Entity`

---

## E01 Project Scaffolding & Domain Setup

### Section Title: Project Initialization & Base Architecture

#### Description

- Spring Boot 3.x (Maven) project setup.
- Base package structure for all modules.
- Flyway/Liquibase for DB migrations.
- Spring Actuator for health checks.
- README with build/run steps.

#### Design Specification

- **Packages:**  
  - `com.warehouse.ems`  
  - Subpackages for each domain module.
- **Dependencies:**  
  - Spring Boot Starter Web, Data JPA, Security, Validation, Actuator, Flyway/Liquibase, OpenAPI/Swagger.
- **Configuration:**  
  - `application.yml` for DB, security, actuator, etc.
- **Health Endpoint:**  
  - `/actuator/health`
- **Migration:**  
  - Baseline migration script in `src/main/resources/db/migration`
- **README:**  
  - Build/run instructions.

#### Sample Implementation

```java
// src/main/java/com/warehouse/ems/WarehouseEmsApplication.java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

```yaml
# src/main/resources/application.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems
    username: ems_user
    password: secret
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
  flyway:
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

```sql
-- src/main/resources/db/migration/V1__baseline.sql
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    shift_group VARCHAR(32),
    hire_date DATE,
    status VARCHAR(16) NOT NULL
);
```

#### Integration Points & Dependencies

- Spring Boot 3.x
- Flyway/Liquibase
- Spring Actuator
- OpenAPI/Swagger

---

## E02 Employee Master Data (CRUD)

### Section Title: Employee Domain Model, Service Layer, Controller Design

#### Description

- Employee entity with CRUD APIs.
- Unique badgeId, soft-delete, pagination, filtering.
- OpenAPI schemas.

#### Design Specification

- **Entity:** `EmployeeEntity`
- **Repository:** `EmployeeRepository`
- **Service:** `EmployeeService`
- **Controller:** `EmployeeController`
- **DTOs:** `EmployeeDto`, `EmployeeCreateDto`, `EmployeeUpdateDto`
- **Validation:** Bean Validation annotations.
- **Soft Delete:** `deleted` boolean field.
- **Endpoints:**
  - `POST /employees`
  - `GET /employees`
  - `GET /employees/{id}`
  - `PUT /employees/{id}`
  - `PATCH /employees/{id}`
  - `DELETE /employees/{id}`

#### Sample Implementation

```java
// com.warehouse.ems.employee.entity.EmployeeEntity.java
@Entity
@Table(name = "employee")
@SQLDelete(sql = "UPDATE employee SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class EmployeeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String badgeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    private String department;
    private String shiftGroup;
    private LocalDate hireDate;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private boolean deleted = false;
    // getters/setters
}
```

```java
// com.warehouse.ems.employee.repository.EmployeeRepository.java
@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {
    Optional<EmployeeEntity> findByBadgeId(String badgeId);
    Page<EmployeeEntity> findAllByDepartment(String department, Pageable pageable);
}
```

```java
// com.warehouse.ems.employee.service.EmployeeService.java
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository repository;

    public EmployeeDto createEmployee(@Valid EmployeeCreateDto dto) { /* ... */ }
    public Page<EmployeeDto> getEmployees(Pageable pageable, String department) { /* ... */ }
    public EmployeeDto getEmployee(Long id) { /* ... */ }
    public EmployeeDto updateEmployee(Long id, @Valid EmployeeUpdateDto dto) { /* ... */ }
    public void deleteEmployee(Long id) { /* ... */ }
}
```

```java
// com.warehouse.ems.employee.controller.EmployeeController.java
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee CRUD APIs")
public class EmployeeController {
    @Autowired
    private EmployeeService service;

    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeCreateDto dto) { /* ... */ }

    @GetMapping
    public Page<EmployeeDto> list(@RequestParam Optional<String> department, Pageable pageable) { /* ... */ }

    @GetMapping("/{id}")
    public EmployeeDto get(@PathVariable Long id) { /* ... */ }

    @PutMapping("/{id}")
    public EmployeeDto update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateDto dto) { /* ... */ }

    @PatchMapping("/{id}")
    public EmployeeDto partialUpdate(@PathVariable Long id, @RequestBody Map<String, Object> updates) { /* ... */ }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { /* ... */ }
}
```

#### Integration Points & Dependencies

- Spring Data JPA
- Bean Validation
- OpenAPI/Swagger
- MapStruct for DTO mapping

---

## E03 Role Based Access Control (RBAC)

### Section Title: Security Configuration, Method/Endpoint Security

#### Description

- Spring Security with roles: ADMIN, HR, SUPERVISOR, WORKER.
- Method/endpoint security, row-level constraints.
- API key/OAuth2 toggle via config.

#### Design Specification

- **Security Config:** `SecurityConfig`
- **Roles:** Enum or constants.
- **Annotations:** `@PreAuthorize`, `@Secured`
- **API Key/OAuth2:** Conditional beans via `@ConditionalOnProperty`
- **User Entity:** `UserEntity` with roles.
- **Endpoints:** Secured as per role.

#### Sample Implementation

```java
// com.warehouse.ems.security.SecurityConfig.java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .antMatchers("/attendance/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            )
            .httpBasic()
            .csrf().disable();
        return http.build();
    }
}
```

```java
// com.warehouse.ems.employee.service.EmployeeService.java
@PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
public EmployeeDto createEmployee(@Valid EmployeeCreateDto dto) { /* ... */ }
```

#### Integration Points & Dependencies

- Spring Security
- OAuth2 Client/Resource Server (optional)
- API Key filter (custom)
- User/Role management

---

## E04 Time & Attendance (Clock In/Out)

### Section Title: Attendance Domain, Service, Controller

#### Description

- Clock-in/out endpoints with geofence/device capture.
- Calculate hours worked per shift.
- Handle missed punches, corrections workflow.

#### Design Specification

- **Entity:** `AttendanceEventEntity`
- **Repository:** `AttendanceEventRepository`
- **Service:** `AttendanceService`
- **Controller:** `AttendanceController`
- **DTOs:** `AttendanceEventDto`, `AttendanceCorrectionDto`
- **Endpoints:**
  - `POST /attendance/clock-in`
  - `POST /attendance/clock-out`
  - `GET /attendance/daily-totals`
  - `POST /attendance/corrections`

#### Sample Implementation

```java
// com.warehouse.ems.attendance.entity.AttendanceEventEntity.java
@Entity
@Table(name = "attendance_event")
public class AttendanceEventEntity {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private EmployeeEntity employee;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String type; // CLOCK_IN, CLOCK_OUT

    private String deviceId;
    private String location;
    // getters/setters
}
```

```java
// com.warehouse.ems.attendance.controller.AttendanceController.java
@RestController
@RequestMapping("/attendance")
@Tag(name = "Attendance", description = "Clock In/Out APIs")
public class AttendanceController {
    @Autowired
    private AttendanceService service;

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceEventDto> clockIn(@Valid @RequestBody AttendanceEventDto dto) { /* ... */ }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceEventDto> clockOut(@Valid @RequestBody AttendanceEventDto dto) { /* ... */ }

    @GetMapping("/daily-totals")
    public List<AttendanceDailyTotalDto> getDailyTotals(@RequestParam Long employeeId, @RequestParam LocalDate date) { /* ... */ }

    @PostMapping("/corrections")
    public ResponseEntity<AttendanceCorrectionDto> requestCorrection(@Valid @RequestBody AttendanceCorrectionDto dto) { /* ... */ }
}
```

#### Integration Points & Dependencies

- Spring Data JPA
- Bean Validation
- Device/geofence integration (optional)
- CSV export utility

---

## E05 Shift & Schedule Management

### Section Title: Shift Template, Schedule Assignment, Controller

#### Description

- Recurring shift templates, rotations, overtime rules.
- Assignment to employees, blackout dates, operation calendars.

#### Design Specification

- **Entity:** `ShiftTemplateEntity`, `ShiftAssignmentEntity`
- **Repository:** `ShiftTemplateRepository`, `ShiftAssignmentRepository`
- **Service:** `ShiftService`
- **Controller:** `ShiftController`
- **DTOs:** `ShiftTemplateDto`, `ShiftAssignmentDto`
- **Endpoints:**
  - `POST /shifts/templates`
  - `GET /shifts/templates`
  - `POST /shifts/assignments`
  - `GET /shifts/assignments`
  - `POST /shifts/bulk-assign`

#### Sample Implementation

```java
// com.warehouse.ems.shift.entity.ShiftTemplateEntity.java
@Entity
@Table(name = "shift_template")
public class ShiftTemplateEntity {
    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private boolean recurring;
    private String rotationPattern;
    // getters/setters
}
```

```java
// com.warehouse.ems.shift.controller.ShiftController.java
@RestController
@RequestMapping("/shifts")
@Tag(name = "Shift", description = "Shift Management APIs")
public class ShiftController {
    @Autowired
    private ShiftService service;

    @PostMapping("/templates")
    public ShiftTemplateDto createTemplate(@Valid @RequestBody ShiftTemplateDto dto) { /* ... */ }

    @GetMapping("/templates")
    public List<ShiftTemplateDto> listTemplates() { /* ... */ }

    @PostMapping("/assignments")
    public ShiftAssignmentDto assignShift(@Valid @RequestBody ShiftAssignmentDto dto) { /* ... */ }

    @GetMapping("/assignments")
    public List<ShiftAssignmentDto> listAssignments(@RequestParam Long employeeId) { /* ... */ }

    @PostMapping("/bulk-assign")
    public void bulkAssign(@Valid @RequestBody BulkAssignDto dto) { /* ... */ }
}
```

#### Integration Points & Dependencies

- Spring Data JPA
- Calendar API (optional)
- Audit logging

---

## E06 Leave & Absence Management

### Section Title: Leave Request, Approval Workflow

#### Description

- PTO, sick, unpaid leave requests/approvals.
- Accrual balances, policies, scheduling/payroll integration.

#### Design Specification

- **Entity:** `LeaveRequestEntity`
- **Repository:** `LeaveRequestRepository`
- **Service:** `LeaveService`
- **Controller:** `LeaveController`
- **DTOs:** `LeaveRequestDto`, `LeaveApprovalDto`
- **Endpoints:**
  - `POST /leave/requests`
  - `GET /leave/requests`
  - `POST /leave/approve`
  - `POST /leave/deny`

#### Sample Implementation

```java
// com.warehouse.ems.leave.entity.LeaveRequestEntity.java
@Entity
@Table(name = "leave_request")
public class LeaveRequestEntity {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private EmployeeEntity employee;

    @Column(nullable = false)
    private String type; // PTO, SICK, UNPAID

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String status; // REQUESTED, APPROVED, DENIED

    private String approver;
    // getters/setters
}
```

```java
// com.warehouse.ems.leave.controller.LeaveController.java
@RestController
@RequestMapping("/leave")
@Tag(name = "Leave", description = "Leave Management APIs")
public class LeaveController {
    @Autowired
    private LeaveService service;

    @PostMapping("/requests")
    public LeaveRequestDto requestLeave(@Valid @RequestBody LeaveRequestDto dto) { /* ... */ }

    @GetMapping("/requests")
    public List<LeaveRequestDto> listRequests(@RequestParam Long employeeId) { /* ... */ }

    @PostMapping("/approve")
    public LeaveApprovalDto approve(@Valid @RequestBody LeaveApprovalDto dto) { /* ... */ }

    @PostMapping("/deny")
    public LeaveApprovalDto deny(@Valid @RequestBody LeaveApprovalDto dto) { /* ... */ }
}
```

#### Integration Points & Dependencies

- Spring Data JPA
- Scheduling/Payroll hooks
- Notification service

---

## E07 Training & Certification Tracking

### Section Title: Certification Entity, Expiry Alerts, Assignment Checks

#### Description

- Track certifications, expirations, renewals.
- Block assignment to tasks requiring expired certs.
- Upload proof documents.

#### Design Specification

- **Entity:** `CertificationEntity`
- **Repository:** `CertificationRepository`
- **Service:** `CertificationService`
- **Controller:** `CertificationController`
- **DTOs:** `CertificationDto`
- **Endpoints:**
  - `POST /certifications`
  - `GET /certifications`
  - `GET /certifications/expiring`
  - `POST /certifications/upload-proof`

#### Sample Implementation

```java
// com.warehouse.ems.training.entity.CertificationEntity.java
@Entity
@Table(name = "certification")
public class CertificationEntity {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private EmployeeEntity employee;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private LocalDate expiryDate;

    private String proofDocumentUrl;
    // getters/setters
}
```

```java
// com.warehouse.ems.training.controller.CertificationController.java
@RestController
@RequestMapping("/certifications")
@Tag(name = "Certification", description = "Certification Tracking APIs")
public class CertificationController {
    @Autowired
    private CertificationService service;

    @PostMapping
    public CertificationDto addCertification(@Valid @RequestBody CertificationDto dto) { /* ... */ }

    @GetMapping
    public List<CertificationDto> listCertifications(@RequestParam Long employeeId) { /* ... */ }

    @GetMapping("/expiring")
    public List<CertificationDto> expiringCerts(@RequestParam int days) { /* ... */ }

    @PostMapping("/upload-proof")
    public void uploadProof(@RequestParam Long certId, @RequestParam MultipartFile file) { /* ... */ }
}
```

#### Integration Points & Dependencies

- Spring Data JPA
- Document storage service
- Notification service

---

## E08 Safety Incidents & OSHA Reporting

### Section Title: Safety Incident Entity, Workflow, Reporting

#### Description

- Record incidents/near-misses, severity, location, involved employees.
- Investigation workflow, OSHA summary generation.

#### Design Specification

- **Entity:** `SafetyIncidentEntity`
- **Repository:** `SafetyIncidentRepository`
- **Service:** `SafetyService`
- **Controller:** `SafetyController`
- **DTOs:** `SafetyIncidentDto`
- **Endpoints:**
  - `POST /safety/incidents`
  - `GET /safety/incidents`
  - `POST /safety/incidents/{id}/status`
  - `GET /safety/osha-summary`

#### Sample Implementation

```java
// com.warehouse.ems.safety.entity.SafetyIncidentEntity.java
@Entity
@Table(name = "safety_incident")
public class SafetyIncidentEntity {
    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String severity;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String description;

    @ManyToMany
    private List<EmployeeEntity> involvedEmployees;

    @Column(nullable = false)
    private String status; // OPEN, INVESTIGATING, RESOLVED

    private LocalDateTime reportedAt;
    // getters/setters
}
```

```java
// com.warehouse.ems.safety.controller.SafetyController.java
@RestController
@RequestMapping("/safety")
@Tag(name = "Safety", description = "Safety Incident APIs")
public class SafetyController {
    @Autowired
    private SafetyService service;

    @PostMapping("/incidents")
    public SafetyIncidentDto reportIncident(@Valid @RequestBody SafetyIncidentDto dto) { /* ... */ }

    @GetMapping("/incidents")
    public List<SafetyIncidentDto> listIncidents(@RequestParam Optional<String> status) { /* ... */ }

    @PostMapping("/incidents/{id}/status")
    public SafetyIncidentDto updateStatus(@PathVariable Long id, @RequestParam String status) { /* ... */ }

    @GetMapping("/osha-summary")
    public OSHAReportDto getOshaSummary(@RequestParam int year) { /* ... */ }
}
```

#### Integration Points & Dependencies

- Spring Data JPA
- OSHA reporting format
- Metrics dashboard

---

## E09 Equipment & Asset Assignment

### Section Title: Asset Registry, Assignment, Condition Tracking

#### Description

- Assign assets to employees, track checkout/return.
- Prevent use if certification missing, maintain asset condition.

#### Design Specification

- **Entity:** `AssetEntity`, `AssetAssignmentEntity`
- **Repository:** `AssetRepository`, `AssetAssignmentRepository`
- **Service:** `AssetService`
- **Controller:** `AssetController`
- **DTOs:** `AssetDto`, `AssetAssignmentDto`
- **Endpoints:**
  - `POST /assets`
  - `GET /assets`
  - `POST /assets/assign`
  - `POST /assets/return`
  - `GET /assets/history`

#### Sample Implementation

```java
// com.warehouse.ems.equipment.entity.AssetEntity.java
@Entity
@Table(name = "asset")
public class AssetEntity {
    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String serialNumber;

    @Column(nullable = false)
    private String condition;

    private boolean assigned;
    // getters/setters
}
```

```java
// com.warehouse.ems.equipment.controller.AssetController.java
@RestController
@RequestMapping("/assets")
@Tag(name = "Asset", description = "Asset Assignment APIs")
public class AssetController {
    @Autowired
    private AssetService service;

    @PostMapping
    public AssetDto addAsset(@Valid @RequestBody AssetDto dto) { /* ... */ }

    @GetMapping
    public List<AssetDto> listAssets() { /* ... */ }

    @PostMapping("/assign")
    public AssetAssignmentDto assignAsset(@Valid @RequestBody AssetAssignmentDto dto) { /* ... */ }

    @PostMapping("/return")
    public AssetAssignmentDto returnAsset(@Valid @RequestBody AssetAssignmentDto dto) { /* ... */ }

    @GetMapping("/history")
    public List<AssetAssignmentDto> assetHistory(@RequestParam Long assetId) { /* ... */ }
}
```

#### Integration Points & Dependencies

- Spring Data JPA
- Certification check service
- Asset condition tracking

---

## E10 Performance Reviews & Goals

### Section Title: Review Cycle, Goal Tracking, PDF Export

#### Description

- Quarterly/annual review templates, goals, competencies, ratings, comments.
- Supervisor/employee acknowledgements, immutable history.

#### Design Specification

- **Entity:** `PerformanceReviewEntity`, `GoalEntity`
- **Repository:** `PerformanceReviewRepository`, `GoalRepository`
- **Service:** `ReviewService`
- **Controller:** `ReviewController`
- **DTOs:** `PerformanceReviewDto`, `GoalDto`
- **Endpoints:**
  - `POST /reviews`
  - `GET /reviews`
  - `POST /reviews/acknowledge`
  - `GET /reviews/export-pdf`

#### Sample Implementation

```java
// com.warehouse.ems.review.entity.PerformanceReviewEntity.java
@Entity
@Table(name = "performance_review")
public class PerformanceReviewEntity {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private EmployeeEntity employee;

    @Column(nullable = false)
    private String cycle; // Q1, Q2, Annual

    @Column(nullable = false)
    private String status; // DRAFT, SUBMITTED, ACKNOWLEDGED

    @OneToMany(mappedBy = "review")
    private List<GoalEntity> goals;

    private String supervisorComments;
    private LocalDateTime submittedAt;
    // getters/setters
}
```

```java
// com.warehouse.ems.review.controller.ReviewController.java
@RestController
@RequestMapping("/reviews")
@Tag(name = "Review", description = "Performance Review APIs")
public class ReviewController {
    @Autowired
    private ReviewService service;

    @PostMapping
    public PerformanceReviewDto createReview(@Valid @RequestBody PerformanceReviewDto dto) { /* ... */ }

    @GetMapping
    public List<PerformanceReviewDto> listReviews(@RequestParam Long employeeId) { /* ... */ }

    @PostMapping("/acknowledge")
    public PerformanceReviewDto acknowledge(@RequestParam Long reviewId) { /* ... */ }

    @GetMapping("/export-pdf")
    public ResponseEntity<Resource> exportPdf(@RequestParam Long reviewId) { /* ... */ }
}
```

#### Integration Points & Dependencies

- Spring Data JPA
- PDF export utility
- Immutable history (audit)

---

## E11 Payroll Export Integration

### Section Title: Payroll Export, Provider Mapping, Secure Delivery

#### Description

- Generate payroll-ready files from attendance/leave.
- Mapping to external provider formats, secure SFTP/API delivery.

#### Design Specification

- **Service:** `PayrollExportService`
- **Controller:** `PayrollController`
- **DTOs:** `PayrollExportDto`
- **Endpoints:**
  - `POST /payroll/export`
  - `GET /payroll/exports`
- **Integration:** SFTP/API delivery, retry/backoff, audit log.

#### Sample Implementation

```java
// com.warehouse.ems.payroll.service.PayrollExportService.java
@Service
public class PayrollExportService {
    @Async
    public void exportPayroll(PayrollExportDto dto) { /* ... */ }
}
```

```java
// com.warehouse.ems.payroll.controller.PayrollController.java
@RestController
@RequestMapping("/payroll")
@Tag(name = "Payroll", description = "Payroll Export APIs")
public class PayrollController {
    @Autowired
    private PayrollExportService service;

    @PostMapping("/export")
    public void export(@Valid @RequestBody PayrollExportDto dto) { /* ... */ }

    @GetMapping("/exports")
    public List<PayrollExportDto> listExports() { /* ... */ }
}
```

#### Integration Points & Dependencies

- SFTP/API client
- Attendance/leave modules
- Audit logging

---

## E12 Notifications & Announcements

### Section Title: Notification Service, Channel Management

#### Description

- In-app, email/SMS notifications for events.
- Quiet hours, opt-in/out, delivery status, rate limits.

#### Design Specification

- **Entity:** `NotificationEntity`, `AnnouncementEntity`
- **Service:** `NotificationService`
- **Controller:** `NotificationController`
- **DTOs:** `NotificationDto`, `AnnouncementDto`
- **Endpoints:**
  - `POST /notifications`
  - `GET /notifications`
  - `POST /announcements`
  - `GET /announcements`

#### Sample Implementation

```java
// com.warehouse.ems.notification.entity.NotificationEntity.java
@Entity
@Table(name = "notification")
public class NotificationEntity {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private EmployeeEntity recipient;

    @Column(nullable = false)
    private String channel; // IN_APP, EMAIL, SMS

    @Column(nullable = false)
    private String message;

    private boolean delivered;
    private LocalDateTime sentAt;
    // getters/setters
}
```

```java
// com.warehouse.ems.notification.controller.NotificationController.java
@RestController
@RequestMapping("/notifications")
@Tag(name = "Notification", description = "Notification APIs")
public class NotificationController {
    @Autowired
    private NotificationService service;

    @PostMapping
    public NotificationDto send(@Valid @RequestBody NotificationDto dto) { /* ... */ }

    @GetMapping
    public List<NotificationDto> list(@RequestParam Long employeeId) { /* ... */ }
}
```

#### Integration Points & Dependencies

- Email/SMS gateway
- Rate limiter
- Localization service

---

## E13 Integration Layer (HRIS/WMS APIs)

### Section Title: External API Connectors, SSO, Webhooks

#### Description

- REST APIs/connectors for HRIS, WMS, IDP SSO.
- Webhooks for events, JWT/OAuth2 security.

#### Design Specification

- **Service:** `IntegrationService`
- **Controller:** `IntegrationController`
- **DTOs:** `HrisSyncDto`, `WmsSyncDto`
- **Endpoints:**
  - `POST /integration/hris-sync`
  - `POST /integration/wms-sync`
  - `POST /integration/webhook`
- **Security:** JWT/OAuth2

#### Sample Implementation

```java
// com.warehouse.ems.integration.controller.IntegrationController.java
@RestController
@RequestMapping("/integration")
@Tag(name = "Integration", description = "Integration APIs")
public class IntegrationController {
    @Autowired
    private IntegrationService service;

    @PostMapping("/hris-sync")
    public void hrisSync(@Valid @RequestBody HrisSyncDto dto) { /* ... */ }

    @PostMapping("/wms-sync")
    public void wmsSync(@Valid @RequestBody WmsSyncDto dto) { /* ... */ }

    @PostMapping("/webhook")
    public void webhook(@RequestBody WebhookEventDto dto) { /* ... */ }
}
```

#### Integration Points & Dependencies

- HRIS/WMS API clients
- JWT/OAuth2
- OpenAPI documentation

---

## E14 Audit Trail & Compliance

### Section Title: Audit Logging, Immutable Storage

#### Description

- Centralized audit logging for sensitive changes.
- Tamper-evident, exportable log table.

#### Design Specification

- **Entity:** `AuditLogEntity`
- **Repository:** `AuditLogRepository`
- **Service:** `AuditService`
- **Controller:** `AuditController`
- **AOP:** Custom aspect for logging changes.
- **Endpoints:**
  - `GET /audit/logs`
  - `GET /audit/logs/export`

#### Sample Implementation

```java
// com.warehouse.ems.audit.entity.AuditLogEntity.java
@Entity
@Table(name = "audit_log")
public class AuditLogEntity {
    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String actor;

    @Column(nullable = false)
    private String entity;

    @Column(nullable = false)
    private String action; // CREATE, UPDATE, DELETE

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String before;

    @Column(columnDefinition = "TEXT")
    private String after;
    // getters/setters
}
```

```java
// com.warehouse.ems.audit.aspect.AuditAspect.java
@Aspect
@Component
public class AuditAspect {
    @AfterReturning(pointcut = "execution(* com.warehouse.ems..*Service.*(..))", returning = "result")
    public void logChange(JoinPoint joinPoint, Object result) { /* ... */ }
}
```

#### Integration Points & Dependencies

- Spring Data JPA
- AOP
- Export utility

---

## E15 Reporting & Analytics

### Section Title: Reporting Service, Dashboard Endpoints

#### Description

- Operational reports: attendance, overtime, leave, certifications, safety KPIs.
- CSV/PDF export, role-based dashboards.

#### Design Specification

- **Service:** `ReportingService`
- **Controller:** `ReportingController`
- **DTOs:** `ReportDto`
- **Endpoints:**
  - `GET /reports/attendance`
  - `GET /reports/overtime`
  - `GET /reports/leave`
  - `GET /reports/certifications`
  - `GET /reports/safety`
  - `GET /reports/export`

#### Sample Implementation

```java
// com.warehouse.ems.reporting.controller.ReportingController.java
@RestController
@RequestMapping("/reports")
@Tag(name = "Reporting", description = "Reporting APIs")
public class ReportingController {
    @Autowired
    private ReportingService service;

    @GetMapping("/attendance")
    public List<AttendanceReportDto> attendance(@RequestParam LocalDate start, @RequestParam LocalDate end) { /* ... */ }

    @GetMapping("/export")
    public ResponseEntity<Resource> export(@RequestParam String type, @RequestParam LocalDate start, @RequestParam LocalDate end) { /* ... */ }
}
```

#### Integration Points & Dependencies

- Attendance, leave, certification, safety modules
- CSV/PDF export utility

---

## E16 Mobile Access (PWA)

### Section Title: Mobile Controller, PWA Manifest, Offline Support

#### Description

- Responsive views for core flows.
- Offline queue for clock events, conflict resolution.

#### Design Specification

- **Controller:** `MobileController`
- **Manifest:** `manifest.json`
- **Service Worker:** `service-worker.js`
- **Endpoints:**
  - `GET /mobile/schedule`
  - `POST /mobile/clock-in`
  - `POST /mobile/clock-out`
  - `GET /mobile/announcements`

#### Sample Implementation

```json
// src/main/resources/static/manifest.json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/mobile",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "icons": [ /* ... */ ]
}
```

```javascript
// src/main/resources/static/service-worker.js
self.addEventListener('fetch', function(event) {
  // Offline queue logic
});
```

#### Integration Points & Dependencies

- Spring Web
- PWA manifest
- Service worker

---

## E17 Onboarding & Offboarding Workflow

### Section Title: Workflow Service, Task Generation

#### Description

- Automate provisioning, training, asset assignment.
- Deprovision access/assets on termination.

#### Design Specification

- **Service:** `OnboardingService`
- **Controller:** `OnboardingController`
- **DTOs:** `OnboardingTaskDto`
- **Endpoints:**
  - `POST /onboarding/new-hire`
  - `POST /offboarding/terminate`
  - `GET /onboarding/tasks`

#### Sample Implementation

```java
// com.warehouse.ems.onboarding.controller.OnboardingController.java
@RestController
@RequestMapping("/onboarding")
@Tag(name = "Onboarding", description = "Onboarding APIs")
public class OnboardingController {
    @Autowired
    private OnboardingService service;

    @PostMapping("/new-hire")
    public void newHire(@Valid @RequestBody NewHireDto dto) { /* ... */ }

    @PostMapping("/terminate")
    public void terminate(@Valid @RequestBody TerminationDto dto) { /* ... */ }

    @GetMapping("/tasks")
    public List<OnboardingTaskDto> listTasks(@RequestParam Long employeeId) { /* ... */ }
}
```

#### Integration Points & Dependencies

- HRIS integration
- Training, asset, schedule modules

---

## E18 Localization

### Section Title: Localization Service, Message Source

#### Description

- Localize templates, notifications, UI.
- Support for multiple languages.

#### Design Specification

- **Service:** `LocalizationService`
- **Config:** `messages.properties`, `messages_es.properties`, etc.
- **Endpoints:** N/A (used by other modules)

#### Sample Implementation

```java
// com.warehouse.ems.localization.service.LocalizationService.java
@Service
public class LocalizationService {
    @Autowired
    private MessageSource messageSource;

    public String getMessage(String code, Locale locale) {
        return messageSource.getMessage(code, null, locale);
    }
}
```

```properties
# src/main/resources/messages.properties
notification.shiftChange=Your shift has changed.
```

#### Integration Points & Dependencies

- Spring MessageSource
- Notification, announcement modules

---

## E19 AI Scheduling

### Section Title: AI Scheduler Service, Optimization

#### Description

- AI-driven shift assignment, conflict resolution.
- Optimize staffing based on constraints.

#### Design Specification

- **Service:** `AiSchedulingService`
- **Controller:** `SchedulingController`
- **DTOs:** `SchedulingRequestDto`, `SchedulingResultDto`
- **Endpoints:**
  - `POST /scheduling/optimize`
  - `GET /scheduling/results`

#### Sample Implementation

```java
// com.warehouse.ems.scheduling.service.AiSchedulingService.java
@Service
public class AiSchedulingService {
    @Async
    public SchedulingResultDto optimizeSchedule(SchedulingRequestDto dto) { /* ... */ }
}
```

```java
// com.warehouse.ems.scheduling.controller.SchedulingController.java
@RestController
@RequestMapping("/scheduling")
@Tag(name = "Scheduling", description = "AI Scheduling APIs")
public class SchedulingController {
    @Autowired
    private AiSchedulingService service;

    @PostMapping("/optimize")
    public SchedulingResultDto optimize(@Valid @RequestBody SchedulingRequestDto dto) { /* ... */ }

    @GetMapping("/results")
    public SchedulingResultDto getResult(@RequestParam Long requestId) { /* ... */ }
}
```

#### Integration Points & Dependencies

- Shift, employee, leave modules
- AI/ML library (external)

---

## E20 Document Management

### Section Title: Document Storage, Upload/Download APIs

#### Description

- Manage employee documents, certifications, incident reports.
- Secure upload/download, access control.

#### Design Specification

- **Entity:** `DocumentEntity`
- **Repository:** `DocumentRepository`
- **Service:** `DocumentService`
- **Controller:** `DocumentController`
- **DTOs:** `DocumentDto`
- **Endpoints:**
  - `POST /documents/upload`
  - `GET /documents/download`
  - `GET /documents/list`

#### Sample Implementation

```java
// com.warehouse.ems.document.entity.DocumentEntity.java
@Entity
@Table(name = "document")
public class DocumentEntity {
    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String url;

    @ManyToOne
    private EmployeeEntity owner;

    private LocalDateTime uploadedAt;
    // getters/setters
}
```

```java
// com.warehouse.ems.document.controller.DocumentController.java
@RestController
@RequestMapping("/documents")
@Tag(name = "Document", description = "Document Management APIs")
public class DocumentController {
    @Autowired
    private DocumentService service;

    @PostMapping("/upload")
    public DocumentDto upload(@RequestParam MultipartFile file, @RequestParam String type, @RequestParam Long ownerId) { /* ... */ }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam Long documentId) { /* ... */ }

    @GetMapping("/list")
    public List<DocumentDto> list(@RequestParam Long ownerId) { /* ... */ }
}
```

#### Integration Points & Dependencies

- Document storage (S3, local, etc.)
- Security (access control)
- Certification, safety modules

---

# Exception Handling & Validation

- Use `@ControllerAdvice` for global exception handling.
- Use `@Valid` and Bean Validation for request DTOs.
- Custom exceptions for business logic errors.

```java
// com.warehouse.ems.common.exception.GlobalExceptionHandler.java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException ex) { /* ... */ }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException ex) { /* ... */ }
}
```

---

# Testing Patterns

- Use `@SpringBootTest` for integration tests.
- Use `@WebMvcTest` for controller layer tests.
- Mock repositories/services as needed.

```java
// com.warehouse.ems.employee.EmployeeControllerTest.java
@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService service;

    @Test
    void testCreateEmployee() throws Exception {
        // test implementation
    }
}
```

---

# Caching & Transaction Management

- Use `@Cacheable` for frequently accessed queries.
- Use `@Transactional` for service methods that modify data.

---

# Audit Logging

- Use Spring Data Envers or custom AOP for entity change tracking.

---

# OpenAPI/Swagger Documentation

- Annotate controllers with `@Tag`, `@Operation`, etc.
- Generate docs at `/swagger-ui.html`.

---

# Database Schema Considerations

- Use Flyway/Liquibase for migrations.
- Normalize tables, enforce foreign keys, indexes on search fields.

---

# Configuration Properties

- Use `application.yml` for environment-specific settings.
- Externalize secrets and sensitive configs.

---

# Integration Points

- HRIS, WMS, Payroll, Email/SMS, Document Storage, AI/ML, SSO.

---

# Package Structure Example

```
com.warehouse.ems
  âââ employee
  âââ attendance
  âââ shift
  âââ leave
  âââ training
  âââ safety
  âââ equipment
  âââ review
  âââ payroll
  âââ notification
  âââ integration
  âââ audit
  âââ reporting
  âââ mobile
  âââ onboarding
  âââ localization
  âââ scheduling
  âââ document
  âââ common
```

---

This document provides a comprehensive, low-level technical design for all 20 epics and 60+ user stories of the Warehouse Employee Management System, following Spring Boot 3.x best practices. Each section includes domain models, service and controller design, sample code, integration points, and configuration details, enabling a development team to implement the entire system with consistency and quality.