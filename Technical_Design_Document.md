# Warehouse Employee Management System â Low-Level Technical Design Document

## Table of Contents
1. [Introduction](#introduction)
2. [Spring Boot Architecture Overview](#spring-boot-architecture-overview)
3. [Module/Epic Design Specifications](#moduleepic-design-specifications)
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
    - [E18: Localization & Multi-tenant](#e18-localization--multi-tenant)
    - [E19: Observability & Monitoring](#e19-observability--monitoring)
    - [E20: CI/CD & Deployment Automation](#e20-cicd--deployment-automation)
4. [Appendix: Common Patterns, Error Handling, and Best Practices](#appendix-common-patterns-error-handling-and-best-practices)

---

## Introduction
This document provides a comprehensive low-level technical design for the Warehouse Employee Management System. It covers all 20 epics and 85+ user stories, translating requirements into actionable technical specifications for Spring Boot developers. The design adheres to industry best practices, ensuring maintainability, scalability, and security.

## Spring Boot Architecture Overview
- **Layered Architecture:**
    - Controller (REST API)
    - Service (Business Logic)
    - Repository (Persistence)
    - Domain (Entities/DTOs)
    - Configuration (Security, Integration, etc.)
- **Dependency Injection:** Managed via Spring's @Component, @Service, @Repository, @Autowired, and constructor injection.
- **Persistence:** JPA/Hibernate with Spring Data JPA repositories.
- **Security:** Spring Security for authentication/authorization, method/endpoint security, and RBAC.
- **API Design:** RESTful endpoints, OpenAPI/Swagger documentation.
- **Exception Handling:** @ControllerAdvice and custom exceptions.
- **Validation:** javax.validation annotations and global validation handling.
- **Logging:** SLF4J/Logback, structured logs.
- **Configuration:** application.yml/properties, profiles, and externalized secrets.

## Module/Epic Design Specifications

### E01: Project Scaffolding & Domain Setup
**Description:**
Initializes the Spring Boot project, configures base packages, sets up core modules, and enables DB migrations and Actuator.

**Design Decisions:**
- Use Maven for build management.
- Base package: `com.company.wms`.
- Modules: `employee`, `attendance`, `schedule`, `safety`, etc.
- Flyway for DB migrations.
- Actuator enabled for health checks.

**Design Specification:**
- Directory structure:
    - `com.company.wms`
        - `employee`
        - `attendance`
        - `schedule`
        - `safety`
        - `config`
        - `common`
- Flyway migration scripts in `src/main/resources/db/migration`.
- Actuator endpoints enabled in `application.yml`.

**Sample Implementation:**
```java
// application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wms
    username: wms_user
    password: ...
  flyway:
    enabled: true
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

---

### E02: Employee Master Data (CRUD)
**Description:**
CRUD APIs for employee records: name, badgeId, role, department, shiftGroup, hireDate, status.

**Design Decisions:**
- Employee entity as the central domain object.
- Soft-delete via `active` flag.
- Unique constraint on `badgeId`.
- Pagination and filtering.
- DTOs for API exposure.

**Design Specification:**
- **Entity:**
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue
    private Long id;
    @Column(nullable = false, unique = true)
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean active = true;
    // getters/setters
}
```
- **Repository:**
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByActiveTrue(Pageable pageable);
}
```
- **Service:**
```java
@Service
public class EmployeeService {
    // CRUD methods, soft-delete, filtering, validation
}
```
- **Controller:**
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping public ResponseEntity<EmployeeDto> create(...)
    @GetMapping public Page<EmployeeDto> list(...)
    @GetMapping("/{id}") public EmployeeDto get(...)
    @PutMapping("/{id}") public EmployeeDto update(...)
    @DeleteMapping("/{id}") public void delete(...)
}
```
- **Validation:**
    - `@Valid` on DTOs, custom exception for duplicate badgeId.
- **OpenAPI:**
    - Annotate endpoints for Swagger docs.

---

### E03: Role-Based Access Control (RBAC)
**Description:**
Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER), endpoint/method security, row-level constraints.

**Design Decisions:**
- Use Spring Security with JWT/OAuth2.
- Roles mapped to authorities.
- Method-level security with `@PreAuthorize`.
- Row-level filtering in services.

**Design Specification:**
- **Security Config:**
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers("/actuator/**").permitAll()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer().jwt();
    }
}
```
- **Method Security:**
```java
@Service
public class EmployeeService {
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Employee createEmployee(...) {...}
}
```
- **API Key/OAuth2 Toggle:**
    - Use `@ConditionalOnProperty` for switching.

---

### E04: Time & Attendance (Clock In/Out)
**Description:**
Endpoints for clock-in/out, geofence/device capture, hours calculation, missed punch correction.

**Design Decisions:**
- AttendanceEvent entity for clock events.
- Geofence/device info as optional fields.
- Correction workflow via ApprovalTask entity.

**Design Specification:**
- **Entities:**
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime timestamp;
    private AttendanceType type; // CLOCK_IN, CLOCK_OUT
    private String deviceId;
    private String geoLocation;
}
@Entity
public class ApprovalTask {
    @Id @GeneratedValue
    private Long id;
    private String type; // CORRECTION
    private String status; // PENDING, APPROVED, REJECTED
    @ManyToOne
    private Employee employee;
    private String details;
}
```
- **Service:**
    - Clock-in/out logic, shift association, missed punch detection.
- **Controller:**
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockEventDto dto) {...}
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockEventDto dto) {...}
}
```
- **Reports:**
    - Export attendance as CSV.

---

### E05: Shift & Schedule Management
**Description:**
Recurring shift templates, rotations, overtime rules, blackout dates, assignment.

**Design Decisions:**
- ShiftTemplate and ShiftAssignment entities.
- Conflict detection in service layer.
- Bulk assignment APIs.

**Design Specification:**
- **Entities:**
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrenceRule;
}
@Entity
public class ShiftAssignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ShiftTemplate template;
    private LocalDate date;
}
```
- **Service:**
    - Conflict detection, bulk assignment, audit logging.
- **Controller:**
    - CRUD for templates, assignments, conflict endpoints.

---

### E06: Leave & Absence Management
**Description:**
Request/approve PTO, sick, unpaid leave; accruals; integration with scheduling/payroll.

**Design Decisions:**
- LeaveRequest entity with status.
- Accrual policy service.
- Integration hooks for schedule/payroll.

**Design Specification:**
- **Entity:**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate startDate;
    private LocalDate endDate;
    private String type; // PTO, SICK, UNPAID
    private String status; // REQUESTED, APPROVED, DENIED
    private String reason;
}
```
- **Service:**
    - Request, approve/deny, update balances, flag shifts.
- **Controller:**
    - Endpoints for request, approval, export.

---

### E07: Training & Certification Tracking
**Description:**
Track certifications, expirations, renewals, proof docs, assignment blocking.

**Design Decisions:**
- Certification entity with expiry.
- Document upload via S3/minio.
- Assignment checks in schedule logic.

**Design Specification:**
- **Entity:**
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
- **Service:**
    - CRUD, expiry alerts, assignment checks.
- **Controller:**
    - Endpoints for CRUD, upload, alerts.

---

### E08: Safety Incidents & OSHA Reporting
**Description:**
Record incidents, workflow, OSHA summary, dashboard.

**Design Decisions:**
- SafetyIncident entity with workflow status.
- OSHA export logic.

**Design Specification:**
- **Entity:**
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
    private String status; // OPEN, INVESTIGATING, RESOLVED
    private LocalDateTime reportedAt;
}
```
- **Service:**
    - Workflow, export, dashboard metrics.
- **Controller:**
    - Endpoints for CRUD, workflow, export.

---

### E09: Equipment & Asset Assignment
**Description:**
Assign assets, track check-in/out, block if cert missing, asset condition.

**Design Decisions:**
- Asset and AssetAssignment entities.
- Certification check in assignment logic.

**Design Specification:**
- **Entities:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private String condition;
    private boolean available;
}
@Entity
public class AssetAssignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Asset asset;
    @ManyToOne
    private Employee employee;
    private LocalDateTime assignedAt;
    private LocalDateTime returnedAt;
}
```
- **Service:**
    - Assignment, check-in/out, overdue logic.
- **Controller:**
    - Endpoints for asset CRUD, assignment, history.

---

### E10: Performance Reviews & Goals
**Description:**
Review templates, goals, ratings, comments, workflow, PDF export.

**Design Decisions:**
- ReviewCycle and PerformanceReview entities.
- Workflow and PDF export logic.

**Design Specification:**
- **Entities:**
```java
@Entity
public class ReviewCycle {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
}
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ReviewCycle cycle;
    private String goals;
    private String competencies;
    private String ratings;
    private String comments;
    private String status; // DRAFT, SUBMITTED, ACKNOWLEDGED
}
```
- **Service:**
    - Workflow, PDF export, role-based access.
- **Controller:**
    - Endpoints for CRUD, workflow, export.

---

### E11: Payroll Export Integration
**Description:**
Generate payroll files, map to provider schema, secure delivery, audit.

**Design Decisions:**
- PayrollExport entity for tracking.
- SFTP/API integration.
- Retry and audit logic.

**Design Specification:**
- **Entity:**
```java
@Entity
public class PayrollExport {
    @Id @GeneratedValue
    private Long id;
    private LocalDate exportDate;
    private String provider;
    private String status; // SUCCESS, FAILED
    private String fileUrl;
    private String errorDetails;
}
```
- **Service:**
    - Export logic, mapping, retry, audit.
- **Controller:**
    - Endpoint to trigger export, status.

---

### E12: Notifications & Announcements
**Description:**
In-app/email/SMS notifications, templates, delivery tracking, rate limiting.

**Design Decisions:**
- Notification and Announcement entities.
- Integration with email/SMS providers.
- Rate limiting and opt-in/out logic.

**Design Specification:**
- **Entities:**
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee recipient;
    private String channel; // EMAIL, SMS, IN_APP
    private String template;
    private String status; // SENT, FAILED
    private LocalDateTime sentAt;
}
@Entity
public class Announcement {
    @Id @GeneratedValue
    private Long id;
    private String message;
    private LocalDateTime createdAt;
    private String locale;
}
```
- **Service:**
    - Delivery, opt-in/out, rate limiting.
- **Controller:**
    - Endpoints for CRUD, delivery status.

---

### E13: Integration Layer (HRIS/WMS APIs)
**Description:**
Expose REST APIs, connectors for HRIS/WMS, SSO, webhooks.

**Design Decisions:**
- IntegrationService for sync jobs.
- JWT/OAuth2 security.
- OpenAPI documentation.

**Design Specification:**
- **Service:**
    - HRIS sync, WMS link, webhook handlers.
- **Controller:**
    - Endpoints for sync, webhooks, SSO.

---

### E14: Audit Trail & Compliance
**Description:**
Centralized audit logging for sensitive changes, immutable storage.

**Design Decisions:**
- AuditLog entity, append-only.
- Service interceptors for logging.

**Design Specification:**
- **Entity:**
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
- **Service:**
    - Interceptor/aspect for logging.
- **Controller:**
    - Export/search endpoints.

---

### E15: Reporting & Analytics
**Description:**
Operational reports, CSV/PDF export, dashboards, metrics endpoints.

**Design Decisions:**
- ReportService for aggregation.
- Role-based access.

**Design Specification:**
- **Service:**
    - Attendance, overtime, leave, certs, safety KPIs.
- **Controller:**
    - Endpoints for report generation, export.

---

### E16: Mobile Access (PWA)
**Description:**
Responsive views, offline support, PWA manifest, conflict resolution.

**Design Decisions:**
- PWA manifest and service worker.
- Offline queue for clock events.

**Design Specification:**
- **Config:**
    - `manifest.json`, service worker in static resources.
- **Controller:**
    - Endpoints optimized for mobile.

---

### E17: Onboarding & Offboarding Workflow
**Description:**
Automate provisioning, initial schedule, training, deprovisioning.

**Design Decisions:**
- WorkflowService for tasks.
- Integration with HRIS, asset, schedule modules.

**Design Specification:**
- **Service:**
    - Onboarding/offboarding logic, task generation.
- **Controller:**
    - Endpoints for workflow triggers, status.

---

### E18: Localization & Multi-tenant
**Description:**
Support for multiple locales, tenants, and data isolation.

**Design Decisions:**
- TenantContext for data filtering.
- MessageSource for i18n.

**Design Specification:**
- **Config:**
    - `LocaleResolver`, `MessageSource` beans.
- **Entities:**
    - `tenant_id` field for multi-tenancy.

---

### E19: Observability & Monitoring
**Description:**
Metrics, logs, distributed tracing, alerting.

**Design Decisions:**
- Actuator for metrics.
- Integration with Prometheus/Grafana.
- Centralized logging.

**Design Specification:**
- **Config:**
    - Actuator endpoints, logback config.
- **Service:**
    - Custom metrics, tracing.

---

### E20: CI/CD & Deployment Automation
**Description:**
Automated build, test, deploy pipelines; environment config.

**Design Decisions:**
- Use GitHub Actions/Jenkins.
- Docker for containerization.
- Profiles for dev/stage/prod.

**Design Specification:**
- **Config:**
    - `Dockerfile`, `docker-compose.yml`, pipeline YAML.

---

## Appendix: Common Patterns, Error Handling, and Best Practices
- **Exception Handling:**
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleNotFound(...) {...}
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(...) {...}
}
```
- **Validation:** Use `@Valid`, custom validators for business rules.
- **Logging:** Use SLF4J, log context (user, requestId).
- **DTO Mapping:** Use MapStruct or manual mapping for DTOs.
- **Testing:** Unit and integration tests for all layers.
- **Documentation:** OpenAPI annotations, Swagger UI enabled.

---

**End of Document**