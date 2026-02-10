# Technical Design Document

## Warehouse Employee Management System

---

## Table of Contents

1. [Introduction](#introduction)
2. [Spring Boot Architecture Overview](#spring-boot-architecture-overview)
3. [Package Structure & Module Definitions](#package-structure--module-definitions)
4. [Epic-wise Low-Level Design](#epic-wise-low-level-design)
    - [E01: Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
    - [E02: Employee Master Data (CRUD)](#e02-employee-master-data-crud)
    - [E03: Role-Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
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
5. [Configuration & Security Settings](#configuration--security-settings)
6. [Integration Points for External Services](#integration-points-for-external-services)
7. [Appendix: Code Snippets & Patterns](#appendix-code-snippets--patterns)

---

## Introduction

This document provides a comprehensive low-level technical design for the Warehouse Employee Management System, covering all 20 epics and 100 user stories. It is intended for Spring Boot developers and architects, ensuring clarity, consistency, and adherence to industry best practices.

---

## Spring Boot Architecture Overview

- **Backend:** Spring Boot (Java 17+), RESTful APIs, layered architecture (Controller, Service, Repository, Domain).
- **Persistence:** JPA/Hibernate, Flyway/Liquibase for migrations, PostgreSQL/MySQL.
- **Security:** Spring Security (RBAC, OAuth2, API Key), method and endpoint security.
- **Integration:** REST, Webhooks, SFTP, external APIs (HRIS, WMS, Payroll).
- **Observability:** Spring Actuator, centralized logging, audit trail.
- **Mobile:** PWA support, responsive endpoints.
- **Testing:** JUnit, Mockito, integration tests, security tests.

---

## Package Structure & Module Definitions

```
com.company.warehousemgmt
âââ config
âââ controller
âââ domain
â   âââ employee
â   âââ attendance
â   âââ shift
â   âââ leave
â   âââ certification
â   âââ safety
â   âââ asset
â   âââ review
â   âââ payroll
â   âââ notification
â   âââ integration
â   âââ audit
â   âââ report
â   âââ onboarding
âââ dto
âââ repository
âââ service
âââ security
âââ integration
âââ util
âââ exception
```

- **Modularization:** Each epic maps to a domain subpackage and corresponding controller, service, repository, and DTOs.
- **Configuration:** Centralized in `config` and `security`.
- **Integration:** External connectors in `integration`.

---

## Epic-wise Low-Level Design

---

### E01: Project Scaffolding & Domain Setup

**Description:**
Initialize Spring Boot project, configure base packages, set up core modules, DB migrations, and Actuator.

**Design Specification:**
- **Project Setup:** Maven, Java 17+, Spring Boot Starter Web, Data JPA, Security, Actuator, Flyway/Liquibase.
- **Base Packages:** As per [Package Structure](#package-structure--module-definitions).
- **DB Migration:** Flyway/Liquibase scripts for baseline schema.
- **Health Monitoring:** `/actuator/health` endpoint.

**Sample Implementation:**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

```yaml
# application.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse
    username: warehouse
    password: secret
  flyway:
    enabled: true
    locations: classpath:db/migration
```

---

### E02: Employee Master Data (CRUD)

**Description:**
CRUD APIs for employee records: name, badgeId, role, department, shiftGroup, hireDate, status.

**Design Specification:**
- **Entity:** `Employee`
- **Repository:** `EmployeeRepository extends JpaRepository`
- **Service:** `EmployeeService` for business logic.
- **Controller:** `EmployeeController` REST endpoints.
- **DTOs:** For API requests/responses.
- **Validation:** Unique badgeId, soft-delete, pagination, filtering.
- **OpenAPI:** Schemas with examples.

**Sample Implementation:**

```java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    @Column(unique = true, nullable = false)
    private String badgeId;
    private String name;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    private Status status; // ACTIVE, INACTIVE, TERMINATED
    private boolean deleted = false;
    // getters/setters
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
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { ... }
    @GetMapping
    public Page<EmployeeDto> list(Pageable pageable, @RequestParam Map<String, String> filters) { ... }
    @PutMapping("/{id}")
    public EmployeeDto update(@PathVariable Long id, @RequestBody EmployeeDto dto) { ... }
    @DeleteMapping("/{id}")
    public void softDelete(@PathVariable Long id) { ... }
}
```

---

### E03: Role-Based Access Control (RBAC)

**Description:**
Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER), endpoint/method security, row-level constraints, API key/OAuth2 toggle.

**Design Specification:**
- **Roles:** Enum `Role { ADMIN, HR, SUPERVISOR, WORKER }`
- **Security Config:** `@EnableWebSecurity`, JWT/OAuth2, API Key toggle.
- **Method Security:** `@PreAuthorize` on service methods.
- **Row-level Security:** Filter queries by user role/team.
- **Tests:** Security test coverage.

**Sample Implementation:**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .antMatchers("/actuator/**").hasRole("ADMIN")
            .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer().jwt();
    }
}
```

```java
@Service
public class EmployeeService {
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @securityService.isTeamMember(#id))")
    public EmployeeDto getEmployee(Long id) { ... }
}
```

---

### E04: Time & Attendance (Clock In/Out)

**Description:**
Endpoints for clock-in/out, geofence/device capture, calculate hours, handle missed punches/corrections.

**Design Specification:**
- **Entity:** `AttendanceEvent`
- **Endpoints:** `/attendance/clock-in`, `/attendance/clock-out`
- **Service:** Calculate shift hours, corrections workflow.
- **Reports:** Export CSV.

**Sample Implementation:**

```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private String deviceId;
    private String location;
    private boolean correctionPending;
    // getters/setters
}
```

```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockInDto dto) { ... }
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockOutDto dto) { ... }
}
```

---

### E05: Shift & Schedule Management

**Description:**
Recurring shift templates, rotations, overtime rules, assignment, blackout dates, operation calendars.

**Design Specification:**
- **Entities:** `ShiftTemplate`, `ShiftAssignment`
- **Conflict Detection:** Service logic.
- **Bulk Assignment:** Supervisor endpoints.
- **Audit:** Entry on changes.

**Sample Implementation:**

```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    // ...
}
```

```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates")
    public ShiftTemplateDto createTemplate(@RequestBody ShiftTemplateDto dto) { ... }
    @PostMapping("/assign")
    public void assignShifts(@RequestBody ShiftAssignmentRequest req) { ... }
}
```

---

### E06: Leave & Absence Management

**Description:**
Request/approve PTO, sick, unpaid leave; accrual balances; integration with scheduling/payroll.

**Design Specification:**
- **Entities:** `LeaveRequest`, `LeaveBalance`
- **Workflow:** Request, approve/deny, update balances.
- **Integration:** Exclude from scheduling/payroll.

**Sample Implementation:**

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
    private LeaveStatus status; // REQUESTED, APPROVED, DENIED
    // ...
}
```

---

### E07: Training & Certification Tracking

**Description:**
Track certifications, expirations, renewals; block assignment to tasks requiring expired certs.

**Design Specification:**
- **Entities:** `Certification`, `EmployeeCertification`
- **Alerts:** 30/7 days before expiry.
- **Blocking:** Scheduling checks.

**Sample Implementation:**

```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalDate expiryDate;
    private String documentUrl;
    // ...
}
```

---

### E08: Safety Incidents & OSHA Reporting

**Description:**
Record incidents, workflow for investigation, corrective actions, OSHA summary.

**Design Specification:**
- **Entities:** `SafetyIncident`
- **Workflow:** Status transitions.
- **Reporting:** OSHA export.

**Sample Implementation:**

```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String description;
    private String location;
    @ManyToMany
    private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status; // OPEN, INVESTIGATING, RESOLVED
    // ...
}
```

---

### E09: Equipment & Asset Assignment

**Description:**
Assign assets to employees, track checkout/return, prevent use if cert missing.

**Design Specification:**
- **Entities:** `Asset`, `AssetAssignment`
- **Checks:** Certification validation.
- **History:** Assignment log.

**Sample Implementation:**

```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String assetTag;
    private String type;
    private String condition;
    // ...
}
```

---

### E10: Performance Reviews & Goals

**Description:**
Review templates, goals, ratings, comments, supervisor/employee acknowledgements.

**Design Specification:**
- **Entities:** `PerformanceReview`, `ReviewGoal`
- **Workflow:** Submit, acknowledge, immutable after sign-off.

**Sample Implementation:**

```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate reviewDate;
    private String comments;
    private boolean acknowledged;
    // ...
}
```

---

### E11: Payroll Export Integration

**Description:**
Generate payroll files from attendance/leave, map to provider formats, secure delivery.

**Design Specification:**
- **Service:** PayrollExportService
- **Integration:** SFTP/API, retries, audit log.

**Sample Implementation:**

```java
@Service
public class PayrollExportService {
    public void exportPayroll(LocalDate period) {
        // Fetch approved attendance/leave, map to schema, deliver via SFTP/API
    }
}
```

---

### E12: Notifications & Announcements

**Description:**
In-app/email/SMS notifications for events, quiet hours, opt-in/out, templates.

**Design Specification:**
- **Entities:** `Notification`, `Announcement`
- **Channels:** Email, SMS, in-app.
- **Rate Limiting:** Service logic.

**Sample Implementation:**

```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private String channel; // EMAIL, SMS, IN_APP
    private String message;
    private boolean delivered;
    // ...
}
```

---

### E13: Integration Layer (HRIS/WMS APIs)

**Description:**
Expose REST APIs/connectors for HRIS, WMS, IDP; webhooks for events.

**Design Specification:**
- **APIs:** JWT/OAuth2 secured.
- **Connectors:** HRIS sync job, WMS link.
- **Webhooks:** Idempotent, documented.

**Sample Implementation:**

```java
@RestController
@RequestMapping("/api/integration/hris")
public class HRISController {
    @PostMapping("/employees")
    public ResponseEntity<?> syncEmployee(@RequestBody HRISDto dto) { ... }
}
```

---

### E14: Audit Trail & Compliance

**Description:**
Centralized audit logging for sensitive changes, tamper-evident storage.

**Design Specification:**
- **Entity:** `AuditLog`
- **Service:** Log create/update/delete with actor, timestamp, before/after.

**Sample Implementation:**

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
    @Lob
    private String before;
    @Lob
    private String after;
    // ...
}
```

---

### E15: Reporting & Analytics

**Description:**
Operational reports (attendance, overtime, leave, certifications, safety KPIs), export CSV/PDF, dashboards.

**Design Specification:**
- **Service:** ReportingService
- **Endpoints:** `/reports/*`
- **Exports:** CSV/PDF, role-based access.

**Sample Implementation:**

```java
@RestController
@RequestMapping("/reports")
public class ReportController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> exportAttendance(@RequestParam Map<String, String> filters) { ... }
}
```

---

### E16: Mobile Access (PWA)

**Description:**
Responsive views for clock-in/out, schedules, leave, announcements; offline-friendly.

**Design Specification:**
- **Endpoints:** Mobile-optimized, PWA manifest.
- **Offline Queue:** Store clock events, sync on reconnect.

**Sample Implementation:**

```java
// PWA manifest.json (frontend)
// Backend: Ensure endpoints support CORS, stateless JWT auth, and mobile-friendly DTOs.
```

---

### E17: Onboarding & Offboarding Workflow

**Description:**
Automate provisioning, initial schedule, training, deprovision access/assets on termination.

**Design Specification:**
- **Workflow:** Onboarding tasks, offboarding triggers.
- **Integration:** HRIS, asset, schedule, certification modules.

**Sample Implementation:**

```java
@Service
public class OnboardingService {
    public void onboardEmployee(Employee employee) {
        // Create user, assign schedule, trigger training, assign assets
    }
    public void offboardEmployee(Employee employee) {
        // Revoke access, collect assets, update schedules
    }
}
```

---

## Configuration & Security Settings

- **application.yml:** Centralized config for DB, security, integration endpoints.
- **Security:** JWT/OAuth2, API Key toggle, CORS, CSRF settings.
- **Actuator:** Expose health, metrics, info endpoints.

---

## Integration Points for External Services

- **HRIS:** REST API, scheduled sync job.
- **WMS:** Department/location sync.
- **Payroll:** SFTP/API export.
- **IDP:** SSO via OAuth2/JWT.
- **Notifications:** Email/SMS providers (e.g., Twilio, SendGrid).

---

## Appendix: Code Snippets & Patterns

- **DTO Pattern:** Separate domain and API models.
- **Service Layer:** Business logic, transactional boundaries.
- **Repository Layer:** JPA repositories, query methods.
- **Controller Layer:** REST endpoints, validation, exception handling.
- **Exception Handling:** `@ControllerAdvice` for global error responses.
- **Audit Logging:** Aspect or service-based logging.
- **Security:** `@PreAuthorize`, method/endpoint security.
- **Testing:** JUnit for unit/integration, Mockito for mocks, Spring Security test utilities.

---

**End of Document**