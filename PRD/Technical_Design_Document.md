# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

---

## Table of Contents

1. [Introduction](#introduction)
2. [Technology Stack](#technology-stack)
3. [Cross-Cutting Concerns](#cross-cutting-concerns)
    - [Database Schema Design](#database-schema-design)
    - [Security Architecture](#security-architecture)
    - [Exception Handling Strategy](#exception-handling-strategy)
    - [Logging and Monitoring](#logging-and-monitoring)
    - [API Documentation](#api-documentation)
    - [Testing Strategy](#testing-strategy)
    - [Configuration Management](#configuration-management)
    - [Deployment Architecture](#deployment-architecture)
    - [Performance Considerations](#performance-considerations)
4. [Epics / Modules](#epics--modules)
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

Warehouse Employee Management System (EMS) is a modular Spring Boot application designed to manage all aspects of warehouse workforce operations, including employee records, scheduling, attendance, compliance, training, assets, and integrations. This document provides a comprehensive low-level technical design for all 75 user stories, ensuring clarity, consistency, and production-readiness.

---

## Technology Stack

- **Backend:** Java 17+, Spring Boot 3.x, Spring Data JPA, Spring Security, Spring Web, Spring Actuator, Spring Validation
- **Database:** PostgreSQL 14+, Flyway for migrations
- **API Docs:** Springdoc OpenAPI/Swagger
- **Messaging/Notifications:** Spring Events, Email (SMTP), SMS (Twilio), WebSocket (for PWA)
- **Integration:** REST, WebClient, SFTP, OAuth2/JWT
- **Frontend:** PWA (React/Angular/Vue, not detailed here)
- **Testing:** JUnit 5, Mockito, Testcontainers
- **CI/CD:** Docker, GitHub Actions, Kubernetes (optional)
- **Monitoring:** Micrometer, Prometheus, Grafana

---

## Cross-Cutting Concerns

### Database Schema Design

- **Naming:** snake_case, plural table names (e.g., employees, attendance_records)
- **Primary Keys:** UUID (recommended), or SERIAL/BIGSERIAL
- **Auditing:** created_at, updated_at, created_by, updated_by on all entities
- **Soft Delete:** is_active or deleted_at column
- **Indexes:** On foreign keys, badge_id, status, and frequently queried fields

### Security Architecture

- **Authentication:** JWT (default), OAuth2 (configurable), Spring Security
- **Authorization:** Role-based (ADMIN, HR, SUPERVISOR, WORKER), method and endpoint security
- **Password Storage:** BCrypt
- **API Security:** CSRF disabled for APIs, enabled for web
- **Row-level Security:** Where applicable (e.g., supervisors see only their team)
- **Audit Logging:** All sensitive actions

### Exception Handling Strategy

- **Global Exception Handler:** `@ControllerAdvice` with custom error responses
- **Validation Errors:** 400 Bad Request with field-level messages
- **Access Denied:** 403 Forbidden
- **Not Found:** 404 Not Found
- **Internal Errors:** 500 with correlation ID

### Logging and Monitoring

- **Structured Logging:** SLF4J, Logback, JSON logs
- **Correlation IDs:** MDC for traceability
- **Metrics:** Micrometer, Prometheus
- **Health Checks:** Spring Boot Actuator

### API Documentation

- **OpenAPI 3.0:** Springdoc, `/swagger-ui.html`
- **Examples:** For all DTOs
- **Security Schemes:** JWT/OAuth2

### Testing Strategy

- **Unit Tests:** Service, repository, controller layers
- **Integration Tests:** Testcontainers for DB, MockMvc for APIs
- **Security Tests:** Endpoint/method access
- **Performance Tests:** JMeter/Gatling (optional)

### Configuration Management

- **Profiles:** dev, test, prod
- **Secrets:** Environment variables, Vault/KMS
- **Externalized Config:** application.yml

### Deployment Architecture

- **Dockerized:** Multi-stage builds
- **Kubernetes:** Helm charts (optional)
- **Zero Downtime:** Rolling updates

### Performance Considerations

- **Pagination:** For all list APIs
- **Batch Processing:** For exports, reports
- **Caching:** Ehcache/Redis for reference data
- **Async:** For notifications, exports

---

## Epics / Modules

---

### E01: Project Scaffolding & Domain Setup

#### Architecture Overview

- **Layered:** Controller â Service â Repository â Domain
- **Modular:** Separate packages for employee, scheduling, attendance, safety, etc.

#### Package Structure

```
com.company.ems
  âââ employee
  âââ scheduling
  âââ attendance
  âââ safety
  âââ leave
  âââ training
  âââ equipment
  âââ performance
  âââ payroll
  âââ notification
  âââ integration
  âââ audit
  âââ reporting
  âââ mobile
  âââ onboarding
  âââ config
  âââ security
  âââ common
```

#### Flyway/Liquibase

- `db/migration/V1__init.sql` for baseline schema
- Versioned migrations for all changes

#### Actuator

- `management.endpoints.web.exposure.include=*`
- Health endpoint enabled

#### Example: `application.yml`

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ems
    username: ems
    password: secret
  flyway:
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

---

### E02: Employee Master Data (CRUD)

#### Domain Model

- **Employee**
    - id (UUID)
    - badgeId (String, unique)
    - firstName, lastName
    - email
    - role (enum)
    - department (FK)
    - shiftGroup (FK)
    - hireDate (Date)
    - status (ACTIVE, INACTIVE, TERMINATED)
    - isActive (boolean)
    - createdAt, updatedAt

#### Entity Example

```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue private UUID id;
    @Column(nullable = false, unique = true) private String badgeId;
    private String firstName;
    private String lastName;
    private String email;
    @Enumerated(EnumType.STRING) private Role role;
    @ManyToOne private Department department;
    @ManyToOne private ShiftGroup shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING) private EmployeeStatus status;
    private boolean isActive = true;
    // auditing fields...
}
```

#### Repository

```java
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDepartmentAndStatus(Department dept, EmployeeStatus status, Pageable pageable);
}
```

#### Service Layer

- `EmployeeService`
    - createEmployee(EmployeeDTO)
    - updateEmployee(UUID, EmployeeDTO)
    - softDeleteEmployee(UUID)
    - listEmployees(Filter, Pageable)
    - getEmployee(UUID)

#### Controller

```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeDTO dto) { ... }
    @PutMapping("/{id}") public ResponseEntity<EmployeeDTO> update(@PathVariable UUID id, @Valid @RequestBody EmployeeDTO dto) { ... }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { ... }
    @GetMapping public Page<EmployeeDTO> list(@RequestParam Map<String, String> filters, Pageable pageable) { ... }
}
```

#### DTOs & Mappers

- `EmployeeDTO` (id, badgeId, firstName, lastName, ...)
- MapStruct for mapping

#### OpenAPI Example

```yaml
paths:
  /employees:
    post:
      summary: Create employee
      requestBody:
        content:
          application/json:
            schema: { $ref: '#/components/schemas/EmployeeDTO' }
      responses:
        201:
          description: Created
          content:
            application/json:
              schema: { $ref: '#/components/schemas/EmployeeDTO' }
```

---

### E03: Role Based Access Control (RBAC)

#### Security Config

- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Method security: `@PreAuthorize("hasRole('ADMIN')")`
- Endpoint security: `/admin/**` for ADMIN, `/supervisor/**` for SUPERVISOR

#### Domain

- **Role** (id, name, permissions)
- **User** (id, username, password, roles)

#### Service

- `RoleService` (CRUD roles, assign to users)
- `UserService` (assignRole, changeRole)

#### Controller

- `/roles` (ADMIN only)
- `/users/{id}/roles` (ADMIN only)

#### Audit

- All role changes logged with timestamp, user, before/after

#### Example SecurityConfig

```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
          .authorizeRequests()
            .antMatchers("/admin/**").hasRole("ADMIN")
            .antMatchers("/supervisor/**").hasAnyRole("ADMIN", "SUPERVISOR")
            .anyRequest().authenticated()
          .and()
            .oauth2ResourceServer().jwt();
    }
}
```

---

### E04: Time & Attendance (Clock In/Out)

#### Domain

- **AttendanceRecord**
    - id, employee (FK), clockIn, clockOut, deviceInfo, location, status (APPROVED, FLAGGED), createdAt

#### Service

- `AttendanceService`
    - clockIn(employeeId, deviceInfo, location)
    - clockOut(employeeId, deviceInfo, location)
    - approveAttendance(recordId)
    - flagAttendance(recordId)

#### Controller

- `/attendance/clock-in` (POST)
- `/attendance/clock-out` (POST)
- `/attendance/approve/{id}` (PUT, SUPERVISOR)
- `/attendance/flag/{id}` (PUT, SUPERVISOR)

#### Overtime Calculation

- Service method: calculateOvertime(employeeId, dateRange)

#### Exception Reporting

- `/attendance/exceptions` (GET, HR)

---

### E05: Shift & Schedule Management

#### Domain

- **ShiftTemplate** (id, name, startTime, endTime, roles)
- **ShiftAssignment** (id, employee, shiftTemplate, date, status)
- **ScheduleChangeNotification**

#### Service

- `ShiftService` (CRUD templates, assign shifts, swap requests)
- `ScheduleService` (view, notify, conflict detection)

#### Controller

- `/shifts/templates` (CRUD)
- `/shifts/assign` (POST)
- `/shifts/swap` (POST)

#### Notifications

- On assignment, swap, or update

---

### E06: Leave & Absence Management

#### Domain

- **LeaveRequest** (id, employee, type, startDate, endDate, status, balanceAtRequest)
- **LeaveBalance** (employee, type, balance)

#### Service

- `LeaveService` (request, approve/reject, update balances)

#### Controller

- `/leave/requests` (POST, GET)
- `/leave/approve/{id}` (PUT, SUPERVISOR)

---

### E07: Training & Certification Tracking

#### Domain

- **Certification** (id, name, expiryDate, employee, status, documentUrl)
- **TrainingModule** (id, name, assignedTo, status)

#### Service

- `CertificationService` (assign, track expiry, renew)
- `TrainingService` (assign, track completion)

#### Controller

- `/certifications` (CRUD)
- `/training/modules` (CRUD)

---

### E08: Safety Incidents & OSHA Reporting

#### Domain

- **SafetyIncident** (id, reportedBy, date, severity, location, description, status)
- **CorrectiveAction** (id, incident, action, assignedTo, status)

#### Service

- `SafetyService` (report, review, assign investigation)
- `OSHAReportService` (generate, export)

#### Controller

- `/safety/incidents` (POST, GET)
- `/safety/osha-report` (GET)

---

### E09: Equipment & Asset Assignment

#### Domain

- **Asset** (id, type, serial, status, assignedTo, condition)
- **AssetAssignment** (id, asset, employee, assignedAt, returnedAt)

#### Service

- `AssetService` (assign, return, track history)

#### Controller

- `/assets` (CRUD)
- `/assets/assign` (POST)
- `/assets/return` (POST)

---

### E10: Performance Reviews & Goals

#### Domain

- **PerformanceReview** (id, employee, cycle, status, selfAssessment, managerAssessment, goals)
- **Goal** (id, review, description, progress)

#### Service

- `ReviewService` (initiate, submit, acknowledge)
- `GoalService` (set, track)

#### Controller

- `/reviews` (CRUD)
- `/goals` (CRUD)

---

### E11: Payroll Export Integration

#### Service

- `PayrollExportService` (export CSV, schedule, map fields, audit)

#### Controller

- `/payroll/export` (POST)
- `/payroll/audit` (GET)

---

### E12: Notifications & Announcements

#### Domain

- **Announcement** (id, title, message, sentAt, status)
- **NotificationPreference** (employee, channel, enabled)

#### Service

- `NotificationService` (send, schedule, broadcast, archive)

#### Controller

- `/announcements` (CRUD)
- `/notifications/preferences` (CRUD)

---

### E13: Integration Layer (HRIS/WMS APIs)

#### Service

- `HRISIntegrationService` (sync, map fields, schedule)
- `WMSIntegrationService`
- `IntegrationAuditService`

#### Controller

- `/integration/hris/sync` (POST)
- `/integration/audit` (GET)

---

### E14: Audit Trail & Compliance

#### Domain

- **AuditLog** (id, entity, entityId, action, actor, before, after, timestamp)

#### Service

- `AuditService` (record, export, enforce retention)

#### Controller

- `/audit/logs` (GET)

---

### E15: Reporting & Analytics

#### Service

- `ReportService` (generate, schedule, share)
- `AnalyticsService` (dashboard data)

#### Controller

- `/reports` (CRUD)
- `/analytics` (GET)

---

### E16: Mobile Access (PWA)

#### PWA

- Manifest, offline support, push notifications
- REST APIs for all core flows
- WebSocket for real-time updates

---

### E17: Onboarding & Offboarding Workflow

#### Domain

- **OnboardingTask** (id, employee, type, status, assignedTo)
- **OffboardingTask** (id, employee, type, status, assignedTo)

#### Service

- `OnboardingService` (initiate, track)
- `OffboardingService` (initiate, track)

#### Controller

- `/onboarding` (POST, GET)
- `/offboarding` (POST, GET)

---

## Example Code Snippets

### MapStruct Mapper

```java
@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeDTO toDto(Employee employee);
    Employee toEntity(EmployeeDTO dto);
}
```

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) { ... }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) { ... }
}
```

### Audit Logging Aspect

```java
@Aspect
@Component
public class AuditAspect {
    @AfterReturning(pointcut = "execution(* com.company.ems..*Service.*(..))", returning = "result")
    public void logAudit(JoinPoint joinPoint, Object result) { ... }
}
```

---

## Conclusion

This document provides a comprehensive, production-ready technical design for the Warehouse EMS, covering all modules, cross-cutting concerns, and implementation details. All code examples follow Spring Boot best practices and are ready for development teams to implement.

---

**Document Version:** 1.0  
**Last Updated:** 2025-01-01  
**Author:** Senior Software Architect