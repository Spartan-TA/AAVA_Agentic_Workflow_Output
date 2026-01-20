# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

## Table of Contents

- [1. Spring Boot Architecture Overview](#1-spring-boot-architecture-overview)
- [2. Package Structure & Module Definitions](#2-package-structure--module-definitions)
- [3. Epic-by-Epic Technical Design](#3-epic-by-epic-technical-design)
- [4. Security Configurations](#4-security-configurations)
- [5. Integration Points](#5-integration-points)
- [6. Exception Handling & Validation](#6-exception-handling--validation)
- [7. Monitoring & Migration](#7-monitoring--migration)
- [8. Sample Code Snippets](#8-sample-code-snippets)

## 1. Spring Boot Architecture Overview

**Frameworks:** Spring Boot 3.x, Spring Data JPA, Spring Security, Spring Web, Spring Validation, Spring Actuator, Flyway/Liquibase, MapStruct, Lombok

**Layers:** Controller (REST), Service, Repository, Domain/Entity, DTO, Configuration, Exception Handling

**Patterns:** DTO, Service, Repository, Factory, Observer (notifications), Strategy (scheduling)

**Persistence:** PostgreSQL, JPA entities, Flyway/Liquibase migrations

**Security:** RBAC via Spring Security, JWT/OAuth2, API Key toggle

**Monitoring:** Spring Actuator endpoints

**Integration:** REST APIs, SFTP, Webhooks, HRIS/WMS connectors

## 2. Package Structure & Module Definitions

```
com.warehouse.ems
âââ config
âââ domain (employee, attendance, shift, leave, training, safety, equipment, review, payroll, notification, integration, audit, report, feedback)
âââ dto
âââ repository
âââ service
âââ controller
âââ exception
âââ util
âââ security
âââ migration
âââ pwa
```

## 3. Epic-by-Epic Technical Design

### E01 Project Scaffolding & Domain Setup

**Description:** Initialize Spring Boot project with Maven, configure base packages, add Flyway/Liquibase, enable Actuator

**Design Specifications:**
- Maven structure with Spring Boot parent
- Dependencies: JPA, Security, Actuator, Flyway
- Actuator health endpoint at /actuator/health
- Migration scripts in db/migration folder
- README with build/run instructions

**Sample Implementation:**
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-core</artifactId>
</dependency>
```

### E02 Employee Master Data (CRUD)

**Description:** Employee domain with CRUD APIs and DTOs

**Entity Design:**
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
    private EmployeeStatus status;
}
```

**Repository:**
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByStatus(EmployeeStatus status, Pageable pageable);
}
```

**Service Layer:**
```java
public interface EmployeeService {
    EmployeeDto create(EmployeeDto dto);
    EmployeeDto update(Long id, EmployeeDto dto);
    void delete(Long id);
    EmployeeDto get(Long id);
    Page<EmployeeDto> list(Pageable pageable, EmployeeFilter filter);
}
```

**Controller:**
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) {...}
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> get(@PathVariable Long id) {...}
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) {...}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {...}
    @GetMapping
    public ResponseEntity<Page<EmployeeDto>> list(...) {...}
}
```

### E03 Role Based Access Control (RBAC)

**Description:** Spring Security with roles, endpoint/method security

**Security Config:**
```java
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
            .anyRequest().authenticated()
            .and().oauth2ResourceServer().jwt();
        return http.build();
    }
}
```

**Method Security:**
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == authentication.principal.department)")
public EmployeeDto updateEmployee(Long id, EmployeeDto dto) {...}
```

### E04 Time & Attendance (Clock In/Out)

**Description:** Clock-in/out endpoints with geofence/device capture, hours calculation

**Entity:**
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime timestamp;
    private AttendanceType type;
    private String deviceId;
    private GeoLocation location;
    private boolean correction;
}
```

**Service:**
```java
public interface AttendanceService {
    AttendanceEventDto clockIn(Long employeeId, ClockInRequest req);
    AttendanceEventDto clockOut(Long employeeId, ClockOutRequest req);
    List<AttendanceSummaryDto> getDailyTotals(Long employeeId, LocalDate date);
    CorrectionTaskDto requestCorrection(Long eventId, CorrectionRequest req);
}
```

**Controller:**
```java
@PostMapping("/attendance/clock-in")
public ResponseEntity<AttendanceEventDto> clockIn(@RequestBody ClockInRequest req) {...}
@PostMapping("/attendance/clock-out")
public ResponseEntity<AttendanceEventDto> clockOut(@RequestBody ClockOutRequest req) {...}
```

### E05 Shift & Schedule Management

**Description:** Shift templates, rotations, overtime, assignment

**Entity:**
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    private Set<DayOfWeek> days;
    private OvertimeRule overtimeRule;
}

@Entity
public class EmployeeShiftAssignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ShiftTemplate shiftTemplate;
    private LocalDate date;
}
```

**Service:**
```java
public interface ShiftService {
    ShiftTemplateDto createTemplate(ShiftTemplateDto dto);
    void assignShift(Long employeeId, Long shiftTemplateId, LocalDate date);
    List<ShiftAssignmentDto> getUpcomingShifts(Long employeeId);
    void bulkAssignShifts(BulkAssignRequest req);
}
```

### E06 Leave & Absence Management

**Description:** PTO/sick/unpaid leave requests, approval workflow, accrual balances

**Entity:**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveStatus status;
    private String approver;
}

@Entity
public class LeaveBalance {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LeaveType type;
    private int accrued;
    private int used;
}
```

**Service:**
```java
public interface LeaveService {
    LeaveRequestDto requestLeave(Long employeeId, LeaveRequestDto dto);
    LeaveRequestDto approveLeave(Long requestId, String approver);
    LeaveBalanceDto getBalance(Long employeeId, LeaveType type);
}
```

### E07 Training & Certification Tracking

**Description:** Track certifications, expirations, renewals, block assignments

**Entity:**
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
}
```

**Service:**
```java
public interface CertificationService {
    CertificationDto addCertification(Long employeeId, CertificationDto dto);
    List<CertificationDto> getExpiringCerts(int days);
    boolean isQualified(Long employeeId, String certType);
}
```

### E08 Safety Incidents & OSHA Reporting

**Description:** Record incidents, investigation workflow, OSHA summary

**Entity:**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String severity;
    private String location;
    private String description;
    @ManyToMany
    private Set<Employee> involvedEmployees;
    private IncidentStatus status;
}
```

**Service:**
```java
public interface SafetyService {
    SafetyIncidentDto reportIncident(SafetyIncidentDto dto);
    SafetyIncidentDto updateStatus(Long incidentId, IncidentStatus status);
    List<OSHAReportDto> exportOSHAReports(LocalDate from, LocalDate to);
}
```

### E09 Equipment & Asset Assignment

**Description:** Assign assets, track checkout/return, block use if cert missing

**Entity:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private AssetCondition condition;
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

**Service:**
```java
public interface AssetService {
    AssetDto registerAsset(AssetDto dto);
    AssetAssignmentDto checkoutAsset(Long assetId, Long employeeId);
    AssetAssignmentDto returnAsset(Long assignmentId);
    List<AssetAssignmentDto> getOverdueReturns();
}
```

### E10 Performance Reviews & Goals

**Description:** Review templates, goals, ratings, comments, workflow

**Entity:**
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
    private int rating;
    private String comments;
    private boolean acknowledgedByEmployee;
    private boolean acknowledgedBySupervisor;
}
```

### E11 Payroll Export Integration

**Description:** Generate payroll files, map to provider formats, secure delivery

**Service:**
```java
public interface PayrollService {
    PayrollExportDto generatePayrollExport(LocalDate from, LocalDate to, PayrollProvider provider);
    void deliverExport(PayrollExportDto export, DeliveryMethod method);
}
```

### E12 Notifications & Announcements

**Description:** In-app/email/SMS notifications, quiet hours, opt-in/out

**Entity:**
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private String channel;
    private String template;
    private String recipient;
    private LocalDateTime sentAt;
    private NotificationStatus status;
}
```

### E13 Integration Layer (HRIS/WMS APIs)

**Description:** REST APIs/connectors for HRIS, WMS, webhooks

**Service:**
```java
public interface IntegrationService {
    void syncHRIS(HRISSyncRequest req);
    void syncWMS(WMSSyncRequest req);
    void handleWebhook(WebhookEvent event);
}
```

### E14 Audit Trail & Compliance

**Description:** Centralized audit logging for sensitive changes

**Entity:**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private Long entityId;
    private String action;
    private String actor;
    private LocalDateTime timestamp;
    private String before;
    private String after;
}
```

### E15 Reporting & Analytics

**Description:** Attendance, overtime, leave, certification, safety KPIs, export CSV/PDF

**Service:**
```java
public interface ReportService {
    ReportDto generateReport(ReportRequest req);
    byte[] exportReport(ReportRequest req, ExportFormat format);
}
```

### E16 Mobile Access (PWA)

**Description:** Responsive views, offline queue, PWA manifest

**PWA Manifest:** src/main/resources/static/manifest.json for installable PWA with Service Worker for offline queue

### E17 Onboarding & Offboarding Workflow

**Description:** Automate provisioning, training, asset assignment, deprovisioning

**Service:**
```java
public interface LifecycleService {
    void onboardEmployee(OnboardRequest req);
    void offboardEmployee(Long employeeId);
}
```

### E18 Localization

**Description:** Localized templates, multi-language support

**Config:** messages.properties, messages_es.properties, etc. Use @MessageSource for i18n

### E19 Advanced Scheduling

**Description:** Complex scheduling, shift swaps, rules engine

**Service:**
```java
public interface AdvancedSchedulingService {
    void proposeShiftSwap(Long fromEmployeeId, Long toEmployeeId, LocalDate date);
    List<ScheduleConflictDto> detectConflicts(ScheduleRequest req);
}
```

### E20 Feedback System

**Description:** Employee feedback, surveys, anonymous option

**Entity:**
```java
@Entity
public class Feedback {
    @Id @GeneratedValue
    private Long id;
    private String employeeId;
    private String content;
    private boolean anonymous;
    private LocalDateTime submittedAt;
}
```

## 4. Security Configurations

- Spring Security: RBAC, JWT/OAuth2, API Key toggle
- Endpoint Protection: @PreAuthorize, @Secured
- Row-level Security: Service layer checks for department/team constraints
- Password Policies: Configurable via application.yml

## 5. Integration Points

- HRIS: REST API, scheduled sync job
- WMS: REST API, department/location mapping
- Payroll: SFTP/API, export job
- Webhooks: Event-driven integration, idempotency checks

## 6. Exception Handling & Validation

- Global Exception Handler: @ControllerAdvice for REST errors
- Validation: Bean Validation (@NotNull, @Size, etc.), custom validators
- Error DTOs: Standardized error responses

## 7. Monitoring & Migration

- Actuator: /actuator/health, /actuator/metrics, /actuator/info
- Flyway/Liquibase: Versioned DB migrations, baseline script in db/migration
- Logging: SLF4J, centralized audit logs

## 8. Sample Code Snippets

### Employee Creation (Service Layer)
```java
public EmployeeDto create(EmployeeDto dto) {
    if (employeeRepository.existsByBadgeId(dto.getBadgeId())) {
        throw new DuplicateBadgeException();
    }
    Employee employee = mapper.toEntity(dto);
    employee.setStatus(EmployeeStatus.ACTIVE);
    employeeRepository.save(employee);
    auditService.logChange(...);
    return mapper.toDto(employee);
}
```

### Exception Handling
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateBadgeException.class)
    public ResponseEntity<ErrorDto> handleDuplicateBadge(DuplicateBadgeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorDto("Badge ID already exists."));
    }
}
```

### Security Configuration (JWT)
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .antMatchers("/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated()
        .and().oauth2ResourceServer().jwt();
    return http.build();
}
```

## Appendix: Acceptance Criteria Mapping

- All endpoints, entities, and workflows are mapped to acceptance criteria from user stories
- OpenAPI schemas generated via springdoc-openapi
- Pagination, filtering, and export endpoints provided where specified
- Audit, reporting, and notification modules are cross-cutting and integrated with relevant domains

---

**End of Document**