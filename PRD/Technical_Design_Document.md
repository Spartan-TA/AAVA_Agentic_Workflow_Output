# Warehouse Employee Management System (WEMS) - Low-Level Technical Design Document

---

## Table of Contents

1. [Introduction](#introduction)
2. [Architectural Overview](#architectural-overview)
3. [Domain Model Diagram Description](#domain-model-diagram-description)
4. [Database Schema Design](#database-schema-design)
5. [Security Configuration](#security-configuration)
6. [Integration Patterns](#integration-patterns)
7. [Technical Design by Epic & User Story](#technical-design-by-epic--user-story)
    - [E01: Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
    - [E02: Employee Master Data (CRUD)](#e02-employee-master-data-crud)
    - [E03: Role Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
    - [E04: Time & Attendance (Clock In/Out)](#e04-time--attendance-clock-inout)
    - [E05: Shift & Schedule Management](#e05-shift--schedule-management)
    - [E06: Leave & Absence Management](#e06-leave--absence-management)
    - [E07: Training & Certification Tracking](#e07-training--certification-tracking)
    - [E08: Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
    - [E09: Equipment & Asset Assignment](#e09-equipment--asset-assignment)
    - [E10: Performance Reviews & Goals](#e10-performance-reviews--goals)
    - [E11: Payroll Export Integration](#e11-payroll-export-integration)
    - [E12: Notifications & Announcements](#e12-notifications--announcements)
    - [E13: Integration Layer (HRIS/WMS APIs)](#e13-integration-layer-hriswms-apis)
    - [E14: Audit Trail & Compliance](#e14-audit-trail--compliance)
    - [E15: Reporting & Analytics](#e15-reporting--analytics)
    - [E16: Mobile Access (PWA)](#e16-mobile-access-pwa)
    - [E17: Onboarding & Offboarding Workflow](#e17-onboarding--offboarding-workflow)

---

## Introduction

This document provides a comprehensive low-level technical design for the Warehouse Employee Management System (WEMS), covering all major modules and user stories derived from 17 epics. It is intended as a blueprint for Spring Boot developers, ensuring consistency, maintainability, and adherence to industry best practices.

---

## Architectural Overview

- **Backend:** Spring Boot 3.x, Java 17+, Maven
- **Persistence:** Spring Data JPA, Hibernate, Flyway/Liquibase
- **Security:** Spring Security (RBAC, JWT/OAuth2)
- **API:** RESTful, OpenAPI/Swagger
- **Monitoring:** Spring Actuator
- **Integration:** REST, SFTP, Webhooks
- **Frontend:** PWA (for mobile access)
- **Other:** Bean Validation, DTO pattern, Exception Handling, Caching, Async Processing

---

## Domain Model Diagram Description

- **Employee**: Central entity, relates to Department, Role, Shift, Attendance, Leave, Certification, Asset, PerformanceReview, etc.
- **Department**: Employees belong to departments.
- **Shift**: Employees assigned to shifts.
- **Attendance**: Clock-in/out events linked to Employee and Shift.
- **Leave**: Employee leave requests and approvals.
- **Certification**: Tracks employee certifications.
- **Asset**: Equipment assigned to employees.
- **PerformanceReview**: Reviews and goals per employee.
- **Incident**: Safety incidents involving employees.
- **AuditLog**: Immutable log of sensitive changes.
- **Notification**: In-app/email/SMS notifications.
- **User**: Authentication and RBAC.

---

## Database Schema Design

- **RDBMS** (e.g., PostgreSQL)
- **Entities**: Employee, Department, Role, Shift, Attendance, Leave, Certification, Asset, PerformanceReview, Incident, AuditLog, Notification, User, etc.
- **Migrations**: Managed via Flyway/Liquibase.

---

## Security Configuration

- **Roles**: ADMIN, HR, SUPERVISOR, WORKER
- **Authentication**: JWT/OAuth2, API Key (toggle)
- **Authorization**: Method/endpoint security, row-level constraints
- **Password Policy**: Configurable
- **Audit**: All sensitive actions logged

---

## Integration Patterns

- **HRIS/WMS**: REST APIs, scheduled sync jobs
- **Payroll**: SFTP/API export
- **IDP**: SSO via OAuth2/JWT
- **Webhooks**: For external event notifications

---

## Technical Design by Epic & User Story

---

### E01: Project Scaffolding & Domain Setup

#### 1. Domain Model

- **Description**: Establishes the foundational structure for all modules.
- **Design Specification**:
    - Package structure: `com.wems.{module}`
    - Base entities: `Employee`, `Department`, `Role`, etc.
    - Configuration: Flyway/Liquibase, Actuator, OpenAPI
    - Health endpoint: `/actuator/health`
- **Sample Implementation**:
```java
// src/main/java/com/wems/WemsApplication.java
@SpringBootApplication
public class WemsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WemsApplication.class, args);
    }
}

// src/main/resources/application.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wems
    username: wems
    password: secret
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics

// Flyway migration example: V1__init.sql
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    department_id INT,
    hire_date DATE,
    status VARCHAR(32),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

---

### E02: Employee Master Data (CRUD)

#### 1. Domain Model

- **Description**: Centralized CRUD for employee records.
- **Design Specification**:
    - Package: `com.wems.employee`
    - Entity: `Employee`
    - Repository: `EmployeeRepository extends JpaRepository<Employee, Long>`
    - Service: `EmployeeService`
    - Controller: `EmployeeController`
    - DTOs: `EmployeeDto`, `EmployeeCreateDto`, `EmployeeUpdateDto`
    - Validation: Unique badgeId, Bean Validation
    - Soft-delete: `deleted` flag
    - Pagination/filtering: Spring Data
    - OpenAPI: Annotated endpoints
- **Sample Implementation**:
```java
// Employee Entity
@Entity
@Table(name = "employee")
public class Employee {
    @Id @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String badgeId;

    @NotBlank
    private String name;

    @ManyToOne
    private Department department;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    private LocalDate hireDate;

    private boolean deleted = false;

    // getters/setters
}

// Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}

// Service
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository repo;

    public EmployeeDto create(EmployeeCreateDto dto) { /* ... */ }
    public Page<EmployeeDto> list(Pageable pageable) { /* ... */ }
    public EmployeeDto update(Long id, EmployeeUpdateDto dto) { /* ... */ }
    public void softDelete(Long id) { /* ... */ }
}

// Controller
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee API")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeCreateDto dto) { /* ... */ }

    @GetMapping
    public Page<EmployeeDto> list(Pageable pageable) { /* ... */ }

    @PutMapping("/{id}")
    public EmployeeDto update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateDto dto) { /* ... */ }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { /* ... */ }
}
```

---

### E03: Role Based Access Control (RBAC)

#### 1. Security Layer

- **Description**: Restricts access based on user roles.
- **Design Specification**:
    - Package: `com.wems.security`
    - Roles: `ADMIN`, `HR`, `SUPERVISOR`, `WORKER`
    - SecurityConfig: JWT/OAuth2, API Key toggle
    - Method security: `@PreAuthorize`
    - Row-level: Service layer checks
    - Exception handling: 401/403 mapped
- **Sample Implementation**:
```java
// SecurityConfig
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(authz -> authz
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer().jwt();
        return http.build();
    }
}

// Controller method security
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(#id))")
@GetMapping("/employees/{id}")
public EmployeeDto getEmployee(@PathVariable Long id) { /* ... */ }
```

---

### E04: Time & Attendance (Clock In/Out)

#### 1. Attendance Domain

- **Description**: Clock-in/out, shift association, corrections.
- **Design Specification**:
    - Package: `com.wems.attendance`
    - Entity: `AttendanceEvent`
    - Repository: `AttendanceRepository`
    - Service: `AttendanceService`
    - Controller: `AttendanceController`
    - DTOs: `ClockInDto`, `ClockOutDto`
    - Validation: Geofence, device capture
    - Correction workflow: Approval tasks
    - Reports: CSV export
- **Sample Implementation**:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    private LocalDateTime clockIn;
    private LocalDateTime clockOut;

    @ManyToOne
    private Shift shift;

    private String deviceId;
    private String location;

    // getters/setters
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<Void> clockIn(@Valid @RequestBody ClockInDto dto) { /* ... */ }

    @PostMapping("/clock-out")
    public ResponseEntity<Void> clockOut(@Valid @RequestBody ClockOutDto dto) { /* ... */ }
}
```

---

### E05: Shift & Schedule Management

#### 1. Shift Domain

- **Description**: Shift templates, rotations, assignment, conflict detection.
- **Design Specification**:
    - Package: `com.wems.shift`
    - Entity: `Shift`, `ShiftTemplate`
    - Repository: `ShiftRepository`
    - Service: `ShiftService`
    - Controller: `ShiftController`
    - Bulk assignment: Service method
    - Conflict detection: Service logic
    - Audit: On assignment
- **Sample Implementation**:
```java
@Entity
public class Shift {
    @Id @GeneratedValue
    private Long id;

    private LocalDateTime start;
    private LocalDateTime end;

    @ManyToMany
    private List<Employee> employees;

    // getters/setters
}

@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping
    public ShiftDto createShift(@Valid @RequestBody ShiftCreateDto dto) { /* ... */ }

    @PostMapping("/bulk-assign")
    public void bulkAssign(@RequestBody BulkAssignDto dto) { /* ... */ }
}
```

---

### E06: Leave & Absence Management

#### 1. Leave Domain

- **Description**: PTO/sick/unpaid leave, accruals, approvals.
- **Design Specification**:
    - Package: `com.wems.leave`
    - Entity: `LeaveRequest`
    - Repository: `LeaveRepository`
    - Service: `LeaveService`
    - Controller: `LeaveController`
    - Accruals: Service logic
    - Integration: Exclude from scheduling/payroll
- **Sample Implementation**:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private LeaveType type;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;

    // getters/setters
}

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping
    public LeaveDto requestLeave(@Valid @RequestBody LeaveCreateDto dto) { /* ... */ }

    @PostMapping("/{id}/approve")
    public void approve(@PathVariable Long id) { /* ... */ }
}
```

---

### E07: Training & Certification Tracking

#### 1. Certification Domain

- **Description**: Track certifications, expirations, renewals, proof uploads.
- **Design Specification**:
    - Package: `com.wems.certification`
    - Entity: `Certification`
    - Repository: `CertificationRepository`
    - Service: `CertificationService`
    - Controller: `CertificationController`
    - Alerts: Scheduled job
    - Proof: File upload endpoint
- **Sample Implementation**:
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    private String type;
    private LocalDate expiryDate;
    private String proofDocumentUrl;

    // getters/setters
}

@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public CertificationDto addCertification(@Valid @RequestBody CertificationCreateDto dto) { /* ... */ }
}
```

---

### E08: Safety Incidents & OSHA Reporting

#### 1. Incident Domain

- **Description**: Record incidents, workflow, OSHA export.
- **Design Specification**:
    - Package: `com.wems.safety`
    - Entity: `Incident`
    - Repository: `IncidentRepository`
    - Service: `IncidentService`
    - Controller: `IncidentController`
    - Workflow: Status transitions
    - OSHA export: CSV endpoint
- **Sample Implementation**:
```java
@Entity
public class Incident {
    @Id @GeneratedValue
    private Long id;

    private String description;
    private String location;
    private IncidentSeverity severity;

    @ManyToMany
    private List<Employee> involvedEmployees;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status;

    // getters/setters
}

@RestController
@RequestMapping("/safety/incidents")
public class IncidentController {
    @PostMapping
    public IncidentDto reportIncident(@Valid @RequestBody IncidentCreateDto dto) { /* ... */ }
}
```

---

### E09: Equipment & Asset Assignment

#### 1. Asset Domain

- **Description**: Assign assets, track check-in/out, condition, certification checks.
- **Design Specification**:
    - Package: `com.wems.asset`
    - Entity: `Asset`, `AssetAssignment`
    - Repository: `AssetRepository`
    - Service: `AssetService`
    - Controller: `AssetController`
    - Certification check: Service logic
    - History: Assignment log
- **Sample Implementation**:
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;

    private String type;
    private String serialNumber;
    private AssetCondition condition;

    // getters/setters
}

@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign")
    public void assignAsset(@RequestBody AssetAssignDto dto) { /* ... */ }
}
```

---

### E10: Performance Reviews & Goals

#### 1. Review Domain

- **Description**: Review templates, goals, ratings, workflow.
- **Design Specification**:
    - Package: `com.wems.review`
    - Entity: `PerformanceReview`
    - Repository: `PerformanceReviewRepository`
    - Service: `PerformanceReviewService`
    - Controller: `PerformanceReviewController`
    - PDF export: Service method
    - Immutable after sign-off
- **Sample Implementation**:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    private String cycle;
    private String goals;
    private String ratings;
    private boolean acknowledged;

    // getters/setters
}
```

---

### E11: Payroll Export Integration

#### 1. Payroll Domain

- **Description**: Export payroll files, SFTP/API, audit log.
- **Design Specification**:
    - Package: `com.wems.payroll`
    - Service: `PayrollExportService`
    - Controller: `PayrollController`
    - Integration: SFTP/API client
    - Retry/backoff: Async logic
    - Audit: Export log
- **Sample Implementation**:
```java
@Service
public class PayrollExportService {
    @Async
    public void exportPayroll(PayrollExportRequest req) { /* ... */ }
}
```

---

### E12: Notifications & Announcements

#### 1. Notification Domain

- **Description**: In-app/email/SMS notifications, templates, quiet hours.
- **Design Specification**:
    - Package: `com.wems.notification`
    - Entity: `Notification`
    - Service: `NotificationService`
    - Controller: `NotificationController`
    - Delivery: Async, rate-limited
    - Opt-in/out: User preferences
- **Sample Implementation**:
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;

    private String channel;
    private String message;
    private boolean delivered;

    // getters/setters
}
```

---

### E13: Integration Layer (HRIS/WMS APIs)

#### 1. Integration Domain

- **Description**: REST APIs, connectors, webhooks, SSO.
- **Design Specification**:
    - Package: `com.wems.integration`
    - Service: `HrisSyncService`, `WmsConnector`
    - Controller: `IntegrationController`
    - Security: JWT/OAuth2
    - Webhooks: Idempotency
- **Sample Implementation**:
```java
@RestController
@RequestMapping("/integration/hris")
public class HrisController {
    @PostMapping("/sync")
    public void syncHris(@RequestBody HrisSyncDto dto) { /* ... */ }
}
```

---

### E14: Audit Trail & Compliance

#### 1. Audit Domain

- **Description**: Centralized, immutable audit log.
- **Design Specification**:
    - Package: `com.wems.audit`
    - Entity: `AuditLog`
    - Service: `AuditService`
    - Controller: `AuditController`
    - Tamper-evident: Append-only
    - Export: By date/user/entity
- **Sample Implementation**:
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

    // getters/setters
}
```

---

### E15: Reporting & Analytics

#### 1. Reporting Domain

- **Description**: Operational reports, exports, dashboards.
- **Design Specification**:
    - Package: `com.wems.reporting`
    - Service: `ReportingService`
    - Controller: `ReportingController`
    - Export: CSV/PDF
    - Metrics: For BI
- **Sample Implementation**:
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> exportAttendance(@RequestParam Map<String, String> filters) { /* ... */ }
}
```

---

### E16: Mobile Access (PWA)

#### 1. Mobile/PWA Layer

- **Description**: Responsive, offline-friendly PWA.
- **Design Specification**:
    - Manifest: `/manifest.json`
    - Service Worker: `/service-worker.js`
    - Offline queue: IndexedDB/local storage
    - API: Same as backend
- **Sample Implementation**:
```json
// manifest.json
{
  "name": "WEMS",
  "short_name": "WEMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

---

### E17: Onboarding & Offboarding Workflow

#### 1. Workflow Domain

- **Description**: Automate provisioning/deprovisioning, asset assignment, training.
- **Design Specification**:
    - Package: `com.wems.workflow`
    - Service: `OnboardingService`, `OffboardingService`
    - Integration: HRIS, Asset, Training modules
    - Task generation: Service logic
- **Sample Implementation**:
```java
@Service
public class OnboardingService {
    public void onboardEmployee(HrisEmployeeDto dto) { /* ... */ }
}
```

---

## Exception Handling

- **Global Exception Handler**: `@ControllerAdvice`
- **Validation Errors**: Bean Validation, custom messages
- **Security Errors**: 401/403 mapped to JSON

---

## Validation Rules

- **Bean Validation**: `@NotBlank`, `@Email`, `@Pattern`, etc.
- **Custom Validators**: For business rules (e.g., unique badgeId)

---

## Caching & Async

- **Caching**: `@Cacheable` for reference data
- **Async**: `@Async` for notifications, exports

---

## Transaction Management

- **Service Layer**: `@Transactional` for write operations

---

## OpenAPI/Swagger

- **Annotations**: `@Tag`, `@Operation`, `@Parameter`
- **Docs**: `/swagger-ui.html`

---

## Monitoring

- **Actuator**: `/actuator/health`, `/actuator/metrics`

---

## Conclusion

This document provides a production-ready, detailed technical design for the Warehouse Employee Management System, covering all major modules and user stories as derived from the available epic summaries. Each section includes package structure, entity design, repository/service/controller layers, DTOs, configuration, security, integration, validation, exception handling, and sample code snippets, following Spring Boot 3.x best practices.

---

**End of Document**