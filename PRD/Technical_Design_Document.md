# Warehouse Employee Management System â Low-Level Technical Design Document

**File:** PRD/Technical_Design_Document.md

---

## Table of Contents

- [E01 - Project Scaffolding & Domain Setup](#e01---project-scaffolding--domain-setup)
- [E02 - Employee Master Data (CRUD)](#e02---employee-master-data-crud)
- [E03 - Role-Based Access Control (RBAC)](#e03---role-based-access-control-rbac)
- [E04 - Time & Attendance (Clock In/Out)](#e04---time--attendance-clock-inout)
- [E05 - Shift & Schedule Management](#e05---shift--schedule-management)
- [E06 - Leave & Absence Management](#e06---leave--absence-management)
- [E07 - Training & Certification Tracking](#e07---training--certification-tracking)
- [E08 - Safety Incidents & OSHA Reporting](#e08---safety-incidents--osha-reporting)
- [E09 - Equipment & Asset Assignment](#e09---equipment--asset-assignment)
- [E10 - Performance Reviews & Goals](#e10---performance-reviews--goals)
- [E11 - Payroll Export Integration](#e11---payroll-export-integration)
- [E12 - Notifications & Announcements](#e12---notifications--announcements)
- [E13 - Integration Layer (HRIS/WMS APIs)](#e13---integration-layer-hriswms-apis)
- [E14 - Audit Trail & Compliance](#e14---audit-trail--compliance)
- [E15 - Reporting & Analytics](#e15---reporting--analytics)
- [E16 - Mobile Access (PWA)](#e16---mobile-access-pwa)
- [E17 - Onboarding & Offboarding Workflow](#e17---onboarding--offboarding-workflow)
- [E18 - Localization & Multi-Tenant](#e18---localization--multi-tenant)
- [E19 - Observability & Monitoring](#e19---observability--monitoring)
- [E20 - CI/CD & Deployment Automation](#e20---cicd--deployment-automation)

---

## E01 - Project Scaffolding & Domain Setup

### Section Title: Project Structure & Initialization

#### Description
Establishes the foundational Spring Boot Maven project, configures base packages, and sets up core modules (employee, scheduling, attendance, safety). Integrates Flyway/Liquibase for DB migrations and enables Actuator for health checks.

#### Design Specification

- **Spring Boot Architecture:**  Layered architecture (Controller â Service â Repository â Domain).  Modularized by business domain.
- **Package Structure:**
  ```
  com.companyname.warehouse
    âââ employee
    âââ scheduling
    âââ attendance
    âââ safety
    âââ config
    âââ common
    âââ Application.java
  ```
- **Module Definitions:**  Each domain (employee, scheduling, etc.) is a package with its own controllers, services, repositories, and models.
- **Database Migration:**  Use Flyway (default) or Liquibase for versioned schema migrations.  Place migration scripts in `src/main/resources/db/migration`.
- **Actuator:**  Enable via `spring-boot-starter-actuator`.  Expose `/actuator/health` endpoint.
- **Configuration:**  Application runs on port 8080.  Profiles: `dev`, `prod`.

#### Sample Implementation

**pom.xml (excerpt):**
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-core</artifactId>
</dependency>
```

**application.yml:**
```yaml
server:
  port: 8080

spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse
    username: warehouse_user
    password: secret
  flyway:
    enabled: true
    locations: classpath:db/migration
```

**Flyway Migration Example (V1__init.sql):**
```sql
CREATE TABLE employee (
  id SERIAL PRIMARY KEY,
  badge_id VARCHAR(32) UNIQUE NOT NULL,
  name VARCHAR(128) NOT NULL,
  role VARCHAR(32) NOT NULL,
  department VARCHAR(64),
  shift_group VARCHAR(32),
  hire_date DATE,
  status VARCHAR(16) NOT NULL,
  deleted BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT now(),
  updated_at TIMESTAMP DEFAULT now()
);
```

**Actuator Health Check:**
- Access via `GET /actuator/health`
- Response: `{ "status": "UP" }`

**README.md (excerpt):**
```
# Build & Run
mvn clean install
java -jar target/warehouse-employee-mgmt.jar
```

---

## E02 - Employee Master Data (CRUD)

### Section Title: Employee Domain Model & CRUD API

#### Description
Implements the Employee domain with full CRUD REST APIs, enforcing unique badge IDs, soft-delete, pagination, filtering, and OpenAPI documentation.

#### Design Specification

- **Spring Boot Architecture:**  Standard layered approach.
- **Package Structure:**
  ```
  com.companyname.warehouse.employee
    âââ controller
    âââ service
    âââ repository
    âââ model
    âââ dto
  ```
- **Entity Design:**
  ```java
  @Entity
  @Table(name = "employee")
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
      private Boolean deleted = false;
      // getters/setters
  }
  ```
- **Service Layer:**
  - Business logic for CRUD, soft-delete, filtering, and validation.
  - Transactional boundaries for create/update/delete.
- **Repository Layer:**
  ```java
  public interface EmployeeRepository extends JpaRepository<Employee, Long> {
      Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
      Page<Employee> findAllByDeletedFalse(Pageable pageable);
      // Filtering methods
  }
  ```
- **Controller Layer:**
  - REST endpoints:    `POST /employees`    `GET /employees` (with pagination/filtering)    `GET /employees/{id}`    `PUT /employees/{id}`    `PATCH /employees/{id}`    `DELETE /employees/{id}` (soft-delete)
  - Uses DTOs for request/response.
- **DTO Example:**
  ```java
  public class EmployeeDTO {
      private Long id;
      private String badgeId;
      private String name;
      private String role;
      private String department;
      private String shiftGroup;
      private LocalDate hireDate;
      private String status;
  }
  ```
- **OpenAPI Integration:**
  - Annotate controllers with `@Operation`, `@Parameter`, `@ApiResponse`.
  - Provide schema examples.
- **Error Handling:**
  - Use `@ControllerAdvice` for global exception handling.
  - Return 409 for duplicate badgeId, 404 for not found.

#### Sample Implementation

**Controller Example:**
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@RequestBody @Valid EmployeeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(dto));
    }

    @GetMapping
    public Page<EmployeeDTO> list(Pageable pageable, @RequestParam Map<String, String> filters) {
        return employeeService.list(pageable, filters);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        employeeService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
```

**OpenAPI Example:**
```java
@Operation(summary = "Create Employee", requestBody = @RequestBody(
    content = @Content(schema = @Schema(implementation = EmployeeDTO.class),
    examples = @ExampleObject(value = "{ "badgeId": "B123", "name": "Jane Doe", ... }"))))
```

---

## E03 - Role-Based Access Control (RBAC)

### Section Title: Security Configuration & Access Control

#### Description
Implements RBAC using Spring Security, with roles (ADMIN, HR, SUPERVISOR, WORKER), endpoint/method security, row-level constraints, and API key/OAuth2 toggle.

#### Design Specification

- **Spring Boot Architecture:**  Security layer intercepts requests before controller.
- **Package Structure:**
  ```
  com.companyname.warehouse.config
  com.companyname.warehouse.security
  ```
- **Security Configuration:**
  - Use `@EnableWebSecurity`
  - Define roles as enums/constants.
  - Configure method security with `@PreAuthorize`.
  - API key/OAuth2 toggle via `application.yml`.
- **User Entity Example:**
  ```java
  @Entity
  public class User {
      @Id @GeneratedValue
      private Long id;
      private String username;
      private String password;
      @ElementCollection(fetch = FetchType.EAGER)
      private Set<String> roles;
      // ...
  }
  ```
- **SecurityConfig Example:**
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
              .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
              .anyRequest().authenticated()
              .and()
              .httpBasic();
      }
  }
  ```
- **Row-Level Security:**
  - In service layer, filter data based on user role/team.
- **API Key/OAuth2 Toggle:**
  - Use conditional beans/configuration.
- **Error Handling:**
  - 401 for unauthenticated, 403 for forbidden.

#### Sample Implementation

**Method Security Example:**
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(#id, authentication))")
public EmployeeDTO getEmployee(Long id) { ... }
```

**application.yml (toggle):**
```yaml
security:
  auth-type: api-key # or oauth2
```

**Test Example:**
```java
@Test
@WithMockUser(roles = "WORKER")
public void testForbiddenAccess() throws Exception {
    mockMvc.perform(post("/employees")).andExpect(status().isForbidden());
}
```

---

## E04 - Time & Attendance (Clock In/Out)

### Section Title: Attendance Domain & Clocking API

#### Description
Provides endpoints for clock-in/out events, with optional geofence/device capture, shift association, missed punch correction workflow, and CSV export.

#### Design Specification

- **Spring Boot Architecture:**  Attendance module with REST endpoints, service, repository.
- **Package Structure:**
  ```
  com.companyname.warehouse.attendance
    âââ controller
    âââ service
    âââ repository
    âââ model
    âââ dto
  ```
- **Entity Design:**
  ```java
  @Entity
  public class AttendanceEvent {
      @Id @GeneratedValue
      private Long id;
      @ManyToOne
      private Employee employee;
      private LocalDateTime timestamp;
      private String type; // CLOCK_IN, CLOCK_OUT
      private String deviceId;
      private String location; // optional geofence
      private Boolean approved;
      // ...
  }
  ```
- **Service Layer:**
  - Handles clock-in/out logic, shift association, missed punch detection.
  - Correction requests create approval tasks.
- **Repository Layer:**
  - Query by employee, date, status.
- **Controller Layer:**
  - `POST /attendance/clock-in`
  - `POST /attendance/clock-out`
  - `GET /attendance/reports`
- **DTO Example:**
  ```java
  public class ClockEventDTO {
      private Long employeeId;
      private String type;
      private String deviceId;
      private String location;
      private LocalDateTime timestamp;
  }
  ```
- **CSV Export:**
  - Use OpenCSV or similar for export.
- **Error Handling:**
  - 400 for invalid state (e.g., double clock-in).

#### Sample Implementation

**Controller Example:**
```java
@PostMapping("/clock-in")
public ResponseEntity<?> clockIn(@RequestBody @Valid ClockEventDTO dto) {
    attendanceService.clockIn(dto);
    return ResponseEntity.ok().build();
}
```

**Service Example:**
```java
@Transactional
public void clockIn(ClockEventDTO dto) {
    // Validate, associate with shift, save event
}
```

**CSV Export Example:**
```java
@GetMapping("/reports")
public void exportCsv(HttpServletResponse response) {
    response.setContentType("text/csv");
    attendanceService.writeCsv(response.getWriter());
}
```

---

## E05 - Shift & Schedule Management

### Section Title: Shift Scheduling Domain

#### Description
Manages recurring shift templates, rotations, overtime rules, employee assignments, blackout dates, and operation calendars.

#### Design Specification

- **Spring Boot Architecture:**  Scheduling module with REST endpoints, service, repository.
- **Package Structure:**
  ```
  com.companyname.warehouse.scheduling
    âââ controller
    âââ service
    âââ repository
    âââ model
    âââ dto
  ```
- **Entity Design:**
  ```java
  @Entity
  public class ShiftTemplate {
      @Id @GeneratedValue
      private Long id;
      private String name;
      private LocalTime startTime;
      private LocalTime endTime;
      private String recurrence; // e.g., DAILY, WEEKLY
      // ...
  }

  @Entity
  public class ShiftAssignment {
      @Id @GeneratedValue
      private Long id;
      @ManyToOne
      private Employee employee;
      @ManyToOne
      private ShiftTemplate shiftTemplate;
      private LocalDate date;
      private Boolean overtime;
      // ...
  }
  ```
- **Service Layer:**
  - Conflict detection, bulk assignment, audit logging.
- **Repository Layer:**
  - Find assignments by employee/date.
- **Controller Layer:**
  - CRUD for shift templates: `/shifts`
  - Assignments: `/schedules`
- **Audit Logging:**
  - On assignment changes.
- **Error Handling:**
  - 409 for conflicts.

#### Sample Implementation

**Controller Example:**
```java
@PostMapping("/shifts")
public ShiftTemplateDTO createShift(@RequestBody @Valid ShiftTemplateDTO dto) {
    return schedulingService.createShift(dto);
}

@PostMapping("/schedules/bulk-assign")
public void bulkAssign(@RequestBody BulkAssignDTO dto) {
    schedulingService.bulkAssign(dto);
}
```

**Service Example:**
```java
@Transactional
public void bulkAssign(BulkAssignDTO dto) {
    // Detect conflicts, assign shifts, log audit
}
```

---

## E06 - Leave & Absence Management

### Section Title: Leave Management Domain

#### Description
Handles PTO, sick, unpaid leave requests/approvals, accruals, and integration with scheduling/payroll.

#### Design Specification

- **Spring Boot Architecture:**  Leave module with REST endpoints, service, repository.
- **Package Structure:**
  ```
  com.companyname.warehouse.leave
    âââ controller
    âââ service
    âââ repository
    âââ model
    âââ dto
  ```
- **Entity Design:**
  ```java
  @Entity
  public class LeaveRequest {
      @Id @GeneratedValue
      private Long id;
      @ManyToOne
      private Employee employee;
      private String type; // PTO, SICK, UNPAID
      private LocalDate startDate;
      private LocalDate endDate;
      private String status; // REQUESTED, APPROVED, DENIED
      private Double accrualBalance;
      // ...
  }
  ```
- **Service Layer:**
  - Request/approve/deny, update balances, flag shifts for coverage.
- **Repository Layer:**
  - Find by employee, status, date.
- **Controller Layer:**
  - `POST /leave/requests`
  - `PUT /leave/requests/{id}/approve`
  - `GET /leave/exports`
- **Integration:**
  - Exclude approved leaves from scheduling/payroll.
- **Error Handling:**
  - 400 for insufficient balance.

#### Sample Implementation

**Controller Example:**
```java
@PostMapping("/leave/requests")
public LeaveRequestDTO requestLeave(@RequestBody @Valid LeaveRequestDTO dto) {
    return leaveService.requestLeave(dto);
}

@PutMapping("/leave/requests/{id}/approve")
public void approveLeave(@PathVariable Long id) {
    leaveService.approveLeave(id);
}
```

---

## E07 - Training & Certification Tracking

### Section Title: Certification Domain

#### Description
Tracks required certifications, expirations, renewals, blocks assignments for expired certs, and supports document uploads.

#### Design Specification

- **Spring Boot Architecture:**  Certification module with REST endpoints, service, repository.
- **Package Structure:**
  ```
  com.companyname.warehouse.certification
    âââ controller
    âââ service
    âââ repository
    âââ model
    âââ dto
  ```
- **Entity Design:**
  ```java
  @Entity
  public class Certification {
      @Id @GeneratedValue
      private Long id;
      private String name;
      private LocalDate issueDate;
      private LocalDate expiryDate;
      @ManyToOne
      private Employee employee;
      private String documentUrl;
      // ...
  }
  ```
- **Service Layer:**
  - CRUD, expiry alerts, block scheduling if expired.
- **Repository Layer:**
  - Find by employee, expiry.
- **Controller Layer:**
  - `POST /certifications`
  - `GET /certifications/alerts`
- **Integration:**
  - Scheduling checks for valid certs.
- **Error Handling:**
  - 409 for assignment with expired cert.

#### Sample Implementation

**Controller Example:**
```java
@PostMapping("/certifications")
public CertificationDTO addCertification(@RequestBody @Valid CertificationDTO dto) {
    return certificationService.addCertification(dto);
}

@GetMapping("/certifications/alerts")
public List<CertificationAlertDTO> getAlerts() {
    return certificationService.getExpiringCertifications();
}
```

---

## E08 - Safety Incidents & OSHA Reporting

### Section Title: Safety Incident Domain

#### Description
Records safety incidents/near-misses, manages investigation workflow, and generates OSHA-compliant reports.

#### Design Specification

- **Spring Boot Architecture:**  Safety module with REST endpoints, service, repository.
- **Package Structure:**
  ```
  com.companyname.warehouse.safety
    âââ controller
    âââ service
    âââ repository
    âââ model
    âââ dto
  ```
- **Entity Design:**
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
      // ...
  }
  ```
- **Service Layer:**
  - Workflow transitions, corrective actions, OSHA export.
- **Repository Layer:**
  - Find by status, date.
- **Controller Layer:**
  - `POST /safety/incidents`
  - `PUT /safety/incidents/{id}/status`
  - `GET /safety/reports/osha`
- **Metrics Dashboard:**
  - Expose KPIs via `/safety/metrics`.
- **Error Handling:**
  - 400 for invalid transitions.

#### Sample Implementation

**Controller Example:**
```java
@PostMapping("/safety/incidents")
public SafetyIncidentDTO reportIncident(@RequestBody @Valid SafetyIncidentDTO dto) {
    return safetyService.reportIncident(dto);
}

@PutMapping("/safety/incidents/{id}/status")
public void updateStatus(@PathVariable Long id, @RequestBody StatusUpdateDTO dto) {
    safetyService.updateStatus(id, dto.getStatus());
}
```

---

## E09 - Equipment & Asset Assignment

### Section Title: Asset Management Domain

#### Description
Manages assignment of equipment/assets to employees, tracks check-in/out, blocks use if certification is missing, and maintains asset condition.

#### Design Specification

- **Spring Boot Architecture:**  Asset module with REST endpoints, service, repository.
- **Package Structure:**
  ```
  com.companyname.warehouse.asset
    âââ controller
    âââ service
    âââ repository
    âââ model
    âââ dto
  ```
- **Entity Design:**
  ```java
  @Entity
  public class Asset {
      @Id @GeneratedValue
      private Long id;
      private String type; // SCANNER, FORKLIFT, PPE
      private String serialNumber;
      private String condition;
      private Boolean checkedOut;
      @ManyToOne
      private Employee assignedTo;
      // ...
  }
  ```
- **Service Layer:**
  - CRUD, check-in/out, certification validation, overdue reports.
- **Repository Layer:**
  - Find by employee, status.
- **Controller Layer:**
  - `POST /assets`
  - `POST /assets/{id}/checkout`
  - `POST /assets/{id}/checkin`
  - `GET /assets/reports/overdue`
- **Error Handling:**
  - 409 for checkout without valid cert.

#### Sample Implementation

**Controller Example:**
```java
@PostMapping("/assets/{id}/checkout")
public void checkout(@PathVariable Long id, @RequestParam Long employeeId) {
    assetService.checkout(id, employeeId);
}
```

---

## E10 - Performance Reviews & Goals

### Section Title: Performance Review Domain

#### Description
Supports creation of review templates, goal tracking, ratings, comments, and supervisor/employee acknowledgements.

#### Design Specification

- **Spring Boot Architecture:**  Performance module with REST endpoints, service, repository.
- **Package Structure:**
  ```
  com.companyname.warehouse.performance
    âââ controller
    âââ service
    âââ repository
    âââ model
    âââ dto
  ```
- **Entity Design:**
  ```java
  @Entity
  public class PerformanceReview {
      @Id @GeneratedValue
      private Long id;
      @ManyToOne
      private Employee employee;
      private String cycle; // Q1, 2024, etc.
      private String template;
      private String status; // DRAFT, SUBMITTED, ACKNOWLEDGED
      private String comments;
      private Integer rating;
      private Boolean immutable;
      // ...
  }
  ```
- **Service Layer:**
  - Create/assign reviews, workflow, PDF export, immutable after sign-off.
- **Repository Layer:**
  - Find by employee, cycle.
- **Controller Layer:**
  - `POST /reviews`
  - `PUT /reviews/{id}/acknowledge`
  - `GET /reviews/export/pdf`
- **Error Handling:**
  - 409 for edits after sign-off.

#### Sample Implementation

**Controller Example:**
```java
@PostMapping("/reviews")
public PerformanceReviewDTO createReview(@RequestBody @Valid PerformanceReviewDTO dto) {
    return performanceService.createReview(dto);
}

@PutMapping("/reviews/{id}/acknowledge")
public void acknowledge(@PathVariable Long id) {
    performanceService.acknowledge(id);
}
```

---

## E11 - Payroll Export Integration

### Section Title: Payroll Integration Domain

#### Description
Generates payroll-ready files from attendance/leave, maps to provider formats, delivers securely, and logs all exports.

#### Design Specification

- **Spring Boot Architecture:**  Payroll module with REST endpoints, service, repository.
- **Package Structure:**
  ```
  com.companyname.warehouse.payroll
    âââ controller
    âââ service
    âââ repository
    âââ model
    âââ dto
  ```
- **Entity Design:**
  ```java
  @Entity
  public class PayrollExport {
      @Id @GeneratedValue
      private Long id;
      private LocalDate exportDate;
      private String status; // SUCCESS, FAILED
      private String filePath;
      private String provider;
      private String errorMessage;
      // ...
  }
  ```
- **Service Layer:**
  - Generate files, map schemas, deliver via SFTP/API, retry with backoff.
- **Repository Layer:**
  - Find by date, status.
- **Controller Layer:**
  - `POST /payroll/exports`
  - `GET /payroll/exports`
- **Audit Logging:**
  - Log every export attempt.
- **Error Handling:**
  - 500 for failed delivery, with retry.

#### Sample Implementation

**Service Example:**
```java
@Transactional
public void exportPayroll(PayrollExportRequestDTO dto) {
    // Generate file, deliver, log result, retry if failed
}
```

---

## E12 - Notifications & Announcements

### Section Title: Notification Domain

#### Description
Sends in-app/email/SMS notifications for shift changes, expiring certs, approvals, and announcements, with quiet hours and opt-in/out.

#### Design Specification

- **Spring Boot Architecture:**  Notification module with REST endpoints, service, repository.
- **Package Structure:**
  ```
  com.companyname.warehouse.notification
    âââ controller
    âââ service
    âââ repository
    âââ model
    âââ dto
  ```
- **Entity Design:**
  ```java
  @Entity
  public class Notification {
      @Id @GeneratedValue
      private Long id;
      private String type; // EMAIL, SMS, IN_APP
      private String template;
      private String status; // SENT, FAILED
      private LocalDateTime sentAt;
      private String recipient;
      // ...
  }
  ```
- **Service Layer:**
  - Send notifications, manage templates, track delivery, rate limit.
- **Repository Layer:**
  - Find by recipient, status.
- **Controller Layer:**
  - `POST /notifications`
  - `GET /announcements`
- **Error Handling:**
  - 429 for rate limit.

#### Sample Implementation

**Controller Example:**
```java
@PostMapping("/notifications")
public void sendNotification(@RequestBody NotificationDTO dto) {
    notificationService.send(dto);
}
```

---

## E13 - Integration Layer (HRIS/WMS APIs)

### Section Title: Integration APIs & Connectors

#### Description
Exposes REST APIs/connectors for HRIS/WMS/IDP, supports SSO, and webhooks for events.

#### Design Specification

- **Spring Boot Architecture:**  Integration module with REST endpoints, service, connectors.
- **Package Structure:**
  ```
  com.companyname.warehouse.integration
    âââ controller
    âââ service
    âââ connector
    âââ model
    âââ dto
  ```
- **API Security:**
  - JWT/OAuth2 for all endpoints.
- **HRIS Sync Job:**
  - Scheduled job to sync employees.
- **Webhooks:**
  - Idempotent event processing.
- **OpenAPI Documentation:**
  - All endpoints documented.
- **Controller Layer:**
  - `POST /integration/hris/sync`
  - `POST /integration/webhooks`
- **Error Handling:**
  - 401/403 for unauthorized.

#### Sample Implementation

**Controller Example:**
```java
@PostMapping("/integration/hris/sync")
@PreAuthorize("hasRole('ADMIN')")
public void syncHris() {
    hrisService.sync();
}
```

---

## E14 - Audit Trail & Compliance

### Section Title: Audit Logging Domain

#### Description
Centralizes audit logging for sensitive changes, with tamper-evident storage and export capabilities.

#### Design Specification

- **Spring Boot Architecture:**  Audit module with service, repository, and aspect.
- **Package Structure:**
  ```
  com.companyname.warehouse.audit
    âââ service
    âââ repository
    âââ model
    âââ aspect
  ```
- **Entity Design:**
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
      // ...
  }
  ```
- **Aspect-Oriented Logging:**
  - Use `@Aspect` to intercept changes.
- **Service Layer:**
  - Export by date/user/entity.
- **Repository Layer:**
  - Find by filters.
- **Error Handling:**
  - Log failures.

#### Sample Implementation

**Aspect Example:**
```java
@Aspect
@Component
public class AuditAspect {
    @AfterReturning(pointcut = "execution(* com.companyname.warehouse..*.save(..))", returning = "result")
    public void logChange(JoinPoint jp, Object result) {
        // Capture before/after, actor, etc.
    }
}
```

---

## E15 - Reporting & Analytics

### Section Title: Reporting Domain

#### Description
Provides operational reports (attendance, overtime, leave, certs, safety KPIs), CSV/PDF export, and dashboards.

#### Design Specification

- **Spring Boot Architecture:**  Reporting module with REST endpoints, service.
- **Package Structure:**
  ```
  com.companyname.warehouse.reporting
    âââ controller
    âââ service
    âââ dto
  ```
- **Service Layer:**
  - Generate reports, filter by date/department/shift.
  - Export CSV/PDF (use JasperReports/iText).
- **Controller Layer:**
  - `GET /reports/attendance`
  - `GET /reports/export`
- **Role-Based Access:**
  - Restrict by user role.
- **Error Handling:**
  - 400 for invalid filters.

#### Sample Implementation

**Controller Example:**
```java
@GetMapping("/reports/attendance")
@PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
public List<AttendanceReportDTO> getAttendanceReport(@RequestParam Map<String, String> filters) {
    return reportingService.getAttendanceReport(filters);
}
```

---

## E16 - Mobile Access (PWA)

### Section Title: Mobile/PWA Support

#### Description
Enables responsive mobile views for core flows, offline support, and PWA manifest.

#### Design Specification

- **Spring Boot Architecture:**  Serve static PWA assets from `/static`.
- **PWA Manifest:**
  - `manifest.json` in `resources/static`.
- **Offline Support:**
  - Service worker caches clock events.
  - Syncs when online.
- **Controller Layer:**
  - No backend changes; reuse existing APIs.
- **Error Handling:**
  - Conflict resolution for offline clock events.

#### Sample Implementation

**manifest.json:**
```json
{
  "name": "Warehouse Employee Management",
  "short_name": "Warehouse",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "icons": [ ... ]
}
```

**Service Worker (pseudo-code):**
```js
self.addEventListener('fetch', function(event) {
  // Cache clock events offline, sync when online
});
```

---

## E17 - Onboarding & Offboarding Workflow

### Section Title: Employee Lifecycle Automation

#### Description
Automates provisioning/deprovisioning of accounts, schedules, training, and asset assignment.

#### Design Specification

- **Spring Boot Architecture:**  Workflow module with service, scheduled jobs.
- **Package Structure:**
  ```
  com.companyname.warehouse.workflow
    âââ service
    âââ model
  ```
- **Service Layer:**
  - Onboarding: create user, assign schedule/training/assets.
  - Offboarding: revoke access, collect assets, update schedules.
- **Integration:**
  - HRIS triggers onboarding.
- **Error Handling:**
  - Log failures, retry.

#### Sample Implementation

**Service Example:**
```java
public void onboardEmployee(EmployeeDTO dto) {
    // Create user, assign tasks, notify
}
```

---

## E18 - Localization & Multi-Tenant

### Section Title: Localization & Multi-Tenancy

#### Description
Supports tenant ID in all entities, locale configuration per tenant, and timezone-aware timestamps.

#### Design Specification

- **Spring Boot Architecture:**  Multi-tenant support via Hibernate filters or schema.
- **Entity Design:**
  ```java
  @Entity
  public class Employee {
      // ...
      private String tenantId;
  }
  ```
- **Locale Config:**
  - Store locale/timezone per tenant.
- **Controller Layer:**
  - Accept tenant ID in headers or JWT.
- **Error Handling:**
  - 403 for cross-tenant access.

#### Sample Implementation

**Tenant Filter Example:**
```java
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = "string"))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
```

---

## E19 - Observability & Monitoring

### Section Title: Observability & Monitoring

#### Description
Implements structured JSON logging, distributed tracing, Prometheus metrics, and health checks.

#### Design Specification

- **Spring Boot Architecture:**  Use `spring-boot-starter-actuator`, OpenTelemetry, Micrometer.
- **Logging:**
  - JSON logs with traceId.
- **Tracing:**
  - OpenTelemetry auto-instrumentation.
- **Metrics:**
  - Expose `/actuator/prometheus`.
- **Health Checks:**
  - DB, external APIs.
- **Alerting:**
  - Integrate with Prometheus Alertmanager.

#### Sample Implementation

**application.yml:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, prometheus
  tracing:
    enabled: true
```

**Logback config:**
```xml
<encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
  <providers>
    <trace />
    <pattern>
      <pattern>
        {
          "timestamp": "%date",
          "level": "%level",
          "traceId": "%X{traceId}",
          ...
        }
      </pattern>
    </pattern>
  </providers>
</encoder>
```

---

## E20 - CI/CD & Deployment Automation

### Section Title: CI/CD Pipeline & Deployment

#### Description
Implements CI/CD pipeline for build/test, Docker image build/push, auto-deploy to staging, approval for production, and rollback.

#### Design Specification

- **CI/CD Tooling:**  Use GitHub Actions, Jenkins, or GitLab CI.
- **Pipeline Steps:**
  - Build, test, Dockerize, push image with commit SHA.
  - Deploy to staging on push.
  - Manual approval for production.
  - Rollback via tagged images.
- **Dockerfile Example:**
  ```dockerfile
  FROM openjdk:17-jdk-alpine
  COPY target/warehouse-employee-mgmt.jar app.jar
  ENTRYPOINT ["java", "-jar", "/app.jar"]
  ```
- **GitHub Actions Example:**
  ```yaml
  name: CI/CD Pipeline
  on:
    push:
      branches: [main]
  jobs:
    build:
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v2
        - name: Build
          run: mvn clean package
        - name: Docker Build
          run: docker build -t warehouse-mgmt:${{ github.sha }} .
        - name: Push Image
          run: docker push warehouse-mgmt:${{ github.sha }}
        - name: Deploy to Staging
          run: ./deploy-staging.sh
  ```
- **Rollback:**
  - Deploy previous image tag.

---

# End of Document