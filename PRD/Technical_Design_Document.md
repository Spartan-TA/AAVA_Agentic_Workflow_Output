# Warehouse Employee Management System - Low-Level Technical Design Document

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
    - [E18: Localization & Multi-Tenant](#e18-localization--multi-tenant)
    - [E19: Observability & Monitoring](#e19-observability--monitoring)
    - [E20: CI/CD & Deployment Automation](#e20-cicd--deployment-automation)
5. [Appendix: Common Patterns & Best Practices](#appendix-common-patterns--best-practices)

---

## Introduction

This document provides a comprehensive low-level technical design for the Warehouse Employee Management System, covering all 20 epics and 100 user stories. It is intended for Spring Boot developers and architects, ensuring clarity, consistency, and adherence to industry standards.

---

## Spring Boot Architecture Overview

**Description of Design Decisions:**  
The system is built using Spring Boot (Maven), leveraging modular architecture for scalability and maintainability. Core modules are separated by domain (employee, scheduling, attendance, safety, etc.). The architecture follows layered principles: Controller â Service â Repository â Domain. Security, integration, and observability are cross-cutting concerns.

**Design Specification:**
- Modular Maven project structure
- Layered architecture (Controller, Service, Repository, Domain)
- RESTful APIs with OpenAPI documentation
- Spring Security for RBAC
- Spring Data JPA for persistence
- Flyway/Liquibase for DB migrations
- Actuator for health/metrics
- Integration via REST, SFTP, and webhooks

**Sample Implementation:**
```java
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}
```

---

## Package Structure & Module Definitions

**Description of Design Decisions:**  
Packages are organized by feature and layer to maximize cohesion and minimize coupling. Each epic maps to a module or package.

**Design Specification:**
- `com.company.wem`
    - `employee` (E02, E17)
    - `security` (E03)
    - `attendance` (E04)
    - `shift` (E05)
    - `leave` (E06)
    - `certification` (E07)
    - `safety` (E08)
    - `asset` (E09)
    - `review` (E10)
    - `payroll` (E11)
    - `notification` (E12)
    - `integration` (E13)
    - `audit` (E14)
    - `reporting` (E15)
    - `mobile` (E16)
    - `localization` (E18)
    - `monitoring` (E19)
    - `cicd` (E20)
    - `common` (shared utils, exceptions, config)

**Sample Implementation:**
```
src/main/java/com/company/wem/
    employee/
    security/
    attendance/
    ...
    common/
```

---

## Epic-wise Low-Level Design

### E01: Project Scaffolding & Domain Setup

**Description of Design Decisions:**  
Standardized project setup with Maven, base packages, Flyway/Liquibase for migrations, and Actuator for health checks.

**Design Specification:**
- Maven multi-module structure
- Base packages as above
- Flyway/Liquibase migration scripts in `src/main/resources/db/migration`
- Actuator enabled in `application.yml`
- README with build/run instructions

**Sample Implementation:**
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wem
    username: wem_user
    password: secret
  flyway:
    enabled: true
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

---

### E02: Employee Master Data (CRUD)

**Description of Design Decisions:**  
Centralized employee domain with CRUD operations, DTOs, validation, and soft-delete.

**Design Specification:**
- Entity: `Employee`
- Repository: `EmployeeRepository extends JpaRepository`
- Service: `EmployeeService`
- Controller: `EmployeeController`
- DTOs for API
- Unique constraint on `badgeId`
- Soft-delete via `status` field
- Pagination, filtering, OpenAPI docs

**Sample Implementation:**
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
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status; // ACTIVE, INACTIVE, DELETED
    // getters/setters
}

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
}

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository repo;
    @Transactional
    public Employee create(EmployeeDTO dto) {
        // validation, mapping, save
    }
    // other CRUD methods
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService service;
    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeDTO dto) {
        // ...
    }
    // GET, PUT, PATCH, DELETE endpoints
}
```

---

### E03: Role-Based Access Control (RBAC)

**Description of Design Decisions:**  
Spring Security with roles, method/endpoint security, row-level constraints, API key/OAuth2 toggle.

**Design Specification:**
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Security config in `SecurityConfig`
- Method-level security with `@PreAuthorize`
- API key/OAuth2 toggle via config
- Exception handling for 401/403

**Sample Implementation:**
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .anyRequest().authenticated()
            .and()
            .oauth2Login(); // or API key filter
    }
}

@Service
public class EmployeeService {
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == authentication.principal.department)")
    public Employee update(Employee employee) { ... }
}
```

---

### E04: Time & Attendance (Clock In/Out)

**Description of Design Decisions:**  
Endpoints for clock-in/out, geofence/device capture, shift association, missed punch correction workflow.

**Design Specification:**
- Entity: `AttendanceEvent`
- Service: `AttendanceService`
- Controller: `AttendanceController`
- Geofence/device fields
- Correction workflow
- CSV export

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
    private String geoLocation;
    // getters/setters
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockInDTO dto) { ... }
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockOutDTO dto) { ... }
}
```

---

### E05: Shift & Schedule Management

**Description of Design Decisions:**  
Shift templates, rotations, overtime rules, blackout dates, conflict detection.

**Design Specification:**
- Entities: `ShiftTemplate`, `EmployeeShiftAssignment`
- Service: `ShiftService`
- Controller: `ShiftController`
- Bulk assignment, conflict detection

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

@Service
public class ShiftService {
    public void assignShift(Long employeeId, Long shiftTemplateId, LocalDate date) {
        // check for conflicts, assign
    }
}
```

---

### E06: Leave & Absence Management

**Description of Design Decisions:**  
Leave requests, approval workflow, accrual balances, integration with scheduling.

**Design Specification:**
- Entity: `LeaveRequest`
- Service: `LeaveService`
- Controller: `LeaveController`
- Accrual policy logic
- Integration hooks for scheduling

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
    private LeaveStatus status; // PENDING, APPROVED, DENIED
    // ...
}
```

---

### E07: Training & Certification Tracking

**Description of Design Decisions:**  
Track certifications, expirations, renewals, block assignments if expired.

**Design Specification:**
- Entity: `Certification`
- Service: `CertificationService`
- Controller: `CertificationController`
- Expiry alerts, document upload

**Sample Implementation:**
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type;
    private LocalDate expiryDate;
    private String documentUrl;
    // ...
}
```

---

### E08: Safety Incidents & OSHA Reporting

**Description of Design Decisions:**  
Incident recording, workflow, OSHA summary generation.

**Design Specification:**
- Entity: `SafetyIncident`
- Service: `SafetyService`
- Controller: `SafetyController`
- Status workflow, export endpoints

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

**Description of Design Decisions:**  
Asset registry, check-in/out, certification checks, condition tracking.

**Design Specification:**
- Entity: `Asset`, `AssetAssignment`
- Service: `AssetService`
- Controller: `AssetController`
- Certification validation

**Sample Implementation:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private AssetCondition condition;
    // ...
}
```

---

### E10: Performance Reviews & Goals

**Description of Design Decisions:**  
Review templates, goal tracking, workflow, PDF export.

**Design Specification:**
- Entity: `PerformanceReview`, `Goal`
- Service: `ReviewService`
- Controller: `ReviewController`
- Immutable history after sign-off

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

**Description of Design Decisions:**  
Payroll file generation, mapping to provider schema, SFTP/API delivery.

**Design Specification:**
- Service: `PayrollExportService`
- Integration: SFTP/API client
- Audit log for exports

**Sample Implementation:**
```java
@Service
public class PayrollExportService {
    public void exportPayroll(LocalDate period) {
        // gather attendance, leave, map to schema, deliver via SFTP/API
    }
}
```

---

### E12: Notifications & Announcements

**Description of Design Decisions:**  
In-app/email/SMS notifications, templates, opt-in/out, rate limiting.

**Design Specification:**
- Entity: `Notification`, `Announcement`
- Service: `NotificationService`
- Controller: `NotificationController`
- Channel preferences

**Sample Implementation:**
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String channel; // EMAIL, SMS, IN_APP
    private String content;
    private boolean delivered;
    // ...
}
```

---

### E13: Integration Layer (HRIS/WMS APIs)

**Description of Design Decisions:**  
REST APIs, connectors for HRIS/WMS, SSO via IDP, webhooks.

**Design Specification:**
- REST controllers for integration
- JWT/OAuth2 security
- Idempotent webhooks

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/integration/hris")
public class HRISController {
    @PostMapping("/employee")
    public ResponseEntity<?> syncEmployee(@RequestBody EmployeeDTO dto) { ... }
}
```

---

### E14: Audit Trail & Compliance

**Description of Design Decisions:**  
Centralized audit logging, immutable log table, export capability.

**Design Specification:**
- Entity: `AuditLog`
- Service: `AuditService`
- Aspect for logging changes

**Sample Implementation:**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private Long entityId;
    private String actor;
    private LocalDateTime timestamp;
    private String before;
    private String after;
    // ...
}
```

---

### E15: Reporting & Analytics

**Description of Design Decisions:**  
Operational reports, CSV/PDF export, role-based dashboards.

**Design Specification:**
- Service: `ReportingService`
- Controller: `ReportingController`
- Metrics endpoints

**Sample Implementation:**
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> exportAttendance(@RequestParam ...) { ... }
}
```

---

### E16: Mobile Access (PWA)

**Description of Design Decisions:**  
Responsive views, offline support, PWA manifest.

**Design Specification:**
- REST endpoints for mobile flows
- PWA manifest/static resources
- Offline queue for clock events

**Sample Implementation:**
```json
// manifest.json
{
  "name": "Warehouse Employee Management",
  "short_name": "WEM",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff"
}
```

---

### E17: Onboarding & Offboarding Workflow

**Description of Design Decisions:**  
Automated provisioning, training/asset assignment, deprovisioning.

**Design Specification:**
- Service: `OnboardingService`, `OffboardingService`
- Integration with HRIS, asset, schedule modules

**Sample Implementation:**
```java
@Service
public class OnboardingService {
    public void onboard(EmployeeDTO dto) {
        // create employee, assign training, assets, schedule
    }
}
```

---

### E18: Localization & Multi-Tenant

**Description of Design Decisions:**  
i18n support, tenant isolation, locale-aware templates.

**Design Specification:**
- Message bundles in `resources/i18n`
- Tenant context via filter/interceptor

**Sample Implementation:**
```java
@Bean
public LocaleResolver localeResolver() {
    SessionLocaleResolver slr = new SessionLocaleResolver();
    slr.setDefaultLocale(Locale.US);
    return slr;
}
```

---

### E19: Observability & Monitoring

**Description of Design Decisions:**  
Spring Boot Actuator, custom metrics, distributed tracing.

**Design Specification:**
- Actuator endpoints enabled
- Custom metrics via `@Timed`
- Integration with Prometheus/Grafana

**Sample Implementation:**
```java
@Timed("attendance.clockin")
public void clockIn(...) { ... }
```

---

### E20: CI/CD & Deployment Automation

**Description of Design Decisions:**  
Automated build/test/deploy pipelines, Docker, K8s manifests.

**Design Specification:**
- Dockerfile, docker-compose.yml
- Jenkins/GitHub Actions pipeline
- K8s manifests in `/deploy`

**Sample Implementation:**
```dockerfile
FROM openjdk:17-jdk
COPY target/wem.jar /wem.jar
ENTRYPOINT ["java", "-jar", "/wem.jar"]
```

---

## Appendix: Common Patterns & Best Practices

- **Dependency Injection:** All services/components use `@Autowired` or constructor injection.
- **Exception Handling:** Global handler with `@ControllerAdvice`.
- **Validation:** DTOs annotated with `@Valid`, `@NotNull`, etc.
- **Security:** Use `@PreAuthorize`, method/endpoint security.
- **Transactions:** Service methods annotated with `@Transactional`.
- **OpenAPI:** Annotate controllers for Swagger docs.
- **Testing:** Unit and integration tests with JUnit, Mockito, Testcontainers.

---

**End of Document**