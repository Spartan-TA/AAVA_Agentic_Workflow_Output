# Warehouse Employee Management System â Low-Level Technical Design Document

---

## Table of Contents

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

---

## E01: Project Scaffolding & Domain Setup

**Description:**  
Initialize Spring Boot (Maven) project; configure base packages; set up core modules (employee, scheduling, attendance, safety); add Flyway/Liquibase for DB migrations; enable Actuator.

### Design Specification

- **Spring Boot Architecture:**  
  - Modular Maven project with layered architecture (controller, service, repository, domain, config).
  - Use of Spring Boot Starter dependencies (web, data-jpa, security, actuator).
  - Flyway or Liquibase for DB migrations.
  - Actuator enabled for health checks and monitoring.

- **Package Structure:**
  ```
  com.company.wms
    âââ config
    âââ employee
    âââ attendance
    âââ schedule
    âââ safety
    âââ training
    âââ equipment
    âââ payroll
    âââ notification
    âââ integration
    âââ audit
    âââ reporting
    âââ mobile
  ```

- **Configuration:**
  - `application.yml` with profiles (dev, prod), DB, actuator, security settings.
  - Flyway/Liquibase migration scripts in `src/main/resources/db/migration`.

- **Actuator:**
  - Expose `/actuator/health`, `/actuator/info`.

### Sample Implementation

```yaml
# application.yml
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
        include: health,info
```

```java
// Main Application
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}
```

---

## E02: Employee Master Data (CRUD)

**Description:**  
Create Employee domain with CRUD APIs and web DTOs: name, badgeId, role, department, shiftGroup, hireDate, status.

### Design Specification

- **Entity Design:**
  ```java
  @Entity
  @Table(name = "employees")
  public class Employee {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;

      @Column(nullable = false, unique = true)
      private String badgeId;

      @Column(nullable = false)
      private String name;

      @Enumerated(EnumType.STRING)
      private EmployeeRole role;

      @ManyToOne
      private Department department;

      @ManyToOne
      private ShiftGroup shiftGroup;

      @Column(nullable = false)
      private LocalDate hireDate;

      @Enumerated(EnumType.STRING)
      private EmployeeStatus status;

      @Column(nullable = false)
      private Boolean deleted = false;
  }
  ```

- **Service Layer:**
  - `EmployeeService` with methods:
    - `createEmployee(EmployeeDTO dto)`
    - `getEmployeeById(Long id)`
    - `updateEmployee(Long id, EmployeeDTO dto)`
    - `deleteEmployee(Long id)` (soft delete)
    - `listEmployees(Pageable pageable, EmployeeFilter filter)`

- **Repository Layer:**
  ```java
  public interface EmployeeRepository extends JpaRepository<Employee, Long> {
      Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
      Page<Employee> findAllByDeletedFalse(Pageable pageable);
      // Filtering methods as needed
  }
  ```

- **Controller:**
  - Endpoints:
    - `POST /employees`
    - `GET /employees/{id}`
    - `PUT /employees/{id}`
    - `PATCH /employees/{id}`
    - `DELETE /employees/{id}`
    - `GET /employees` (with pagination/filtering)
  - DTOs with validation annotations.

- **Validation:**
  - Unique badgeId, required fields, enum validation.

- **OpenAPI:**
  - Annotate endpoints with `@Operation`, provide schema examples.

### Sample Implementation

```java
// EmployeeDTO
public class EmployeeDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String badgeId;
    @NotNull
    private EmployeeRole role;
    @NotNull
    private Long departmentId;
    @NotNull
    private Long shiftGroupId;
    @NotNull
    private LocalDate hireDate;
    @NotNull
    private EmployeeStatus status;
}

// Controller
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeDTO dto) { ... }
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> get(@PathVariable Long id) { ... }
    // Other endpoints...
}
```

---

## E03: Role-Based Access Control (RBAC)

**Description:**  
Add Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER); method/endpoint security and row-level constraints; API key/OAuth2 toggle via config.

### Design Specification

- **Spring Security:**
  - Roles: `ADMIN`, `HR`, `SUPERVISOR`, `WORKER`.
  - Use `@PreAuthorize` for method-level security.
  - Row-level security in repositories/services.
  - API key or OAuth2 authentication, toggled via config.

- **Security Config:**
  - `SecurityConfig` class with role mappings.
  - JWT/OAuth2 resource server config.
  - API key filter (if enabled).

- **Endpoints:**
  - Unauthorized returns 401, forbidden returns 403.
  - ADMIN: full access; SUPERVISOR: team-limited; WORKER: self.

- **Tests:**
  - Security integration tests for all rules.

### Sample Implementation

```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
            .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer().jwt();
    }
}

// Method-level security
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @securityService.isTeamMember(#id))")
public EmployeeDTO getEmployee(Long id) { ... }
```

---

## E04: Time & Attendance (Clock In/Out)

**Description:**  
Endpoints for clock-in/out events with geofence (optional) and device capture; calculate hours worked per shift; handle missed punches and corrections workflow.

### Design Specification

- **Entity Design:**
  ```java
  @Entity
  public class AttendanceEvent {
      @Id @GeneratedValue
      private Long id;
      @ManyToOne
      private Employee employee;
      @Enumerated(EnumType.STRING)
      private AttendanceType type; // CLOCK_IN, CLOCK_OUT
      private LocalDateTime timestamp;
      private String deviceId;
      private GeoLocation location;
      private Boolean correctionRequested = false;
      // ... audit fields
  }
  ```

- **Service Layer:**
  - `AttendanceService`:
    - `clockIn(employeeId, deviceId, location)`
    - `clockOut(employeeId, deviceId, location)`
    - `requestCorrection(eventId, details)`
    - `calculateDailyTotals(employeeId, date)`

- **Repository Layer:**
  - Find events by employee/date/type.
  - Custom query for daily totals.

- **Controller:**
  - `POST /attendance/clock-in`
  - `POST /attendance/clock-out`
  - `POST /attendance/corrections`
  - `GET /attendance/reports`

- **Validation:**
  - Prevent duplicate clock-ins/outs.
  - Validate geofence if enabled.

- **Export:**
  - CSV export endpoint.

### Sample Implementation

```java
@PostMapping("/attendance/clock-in")
public ResponseEntity<?> clockIn(@Valid @RequestBody ClockInRequest req) {
    attendanceService.clockIn(req.getEmployeeId(), req.getDeviceId(), req.getLocation());
    return ResponseEntity.ok().build();
}
```

---

## E05: Shift & Schedule Management

**Description:**  
Create recurring shift templates, rotations, overtime rules, and assignment to employees; handle blackout dates and warehouse operation calendars.

### Design Specification

- **Entities:**
  - `ShiftTemplate`, `ShiftAssignment`, `BlackoutDate`, `WarehouseCalendar`.

- **Service Layer:**
  - CRUD for shift templates.
  - Assign shifts (bulk/single).
  - Detect/prevent conflicts.
  - Generate audit entries.

- **Repository Layer:**
  - Find assignments by employee/date.
  - Conflict detection queries.

- **Controller:**
  - `POST /shifts/templates`
  - `POST /shifts/assignments`
  - `GET /shifts/my`
  - `GET /shifts/conflicts`

- **Validation:**
  - Prevent overlapping assignments.
  - Enforce blackout dates.

### Sample Implementation

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
    // ...
}
```

---

## E06: Leave & Absence Management

**Description:**  
Request/approve PTO, sick, unpaid leave; accrual balances and policies; integration hooks to exclude from scheduling and payroll hours.

### Design Specification

- **Entities:**
  - `LeaveRequest`, `LeaveBalance`, `LeavePolicy`.

- **Service Layer:**
  - Request/approve/deny leave.
  - Update balances.
  - Exclude from scheduling/payroll.

- **Repository Layer:**
  - Find leaves by employee/date/status.

- **Controller:**
  - `POST /leaves/requests`
  - `POST /leaves/approve`
  - `GET /leaves/balances`

- **Validation:**
  - Check accruals, prevent negative balances.

### Sample Implementation

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
    // ...
}
```

---

## E07: Training & Certification Tracking

**Description:**  
Track required certifications (e.g., forklift), expirations, renewals; block assignment to tasks requiring expired certs; upload proof documents.

### Design Specification

- **Entities:**
  - `Certification`, `EmployeeCertification`.

- **Service Layer:**
  - CRUD for certifications.
  - Alert on expiry.
  - Block unqualified assignments.

- **Repository Layer:**
  - Find certifications by employee/status.

- **Controller:**
  - `POST /certifications`
  - `GET /certifications/alerts`

- **Validation:**
  - Prevent expired certs from assignment.

### Sample Implementation

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
    private String documentUrl;
    // ...
}
```

---

## E08: Safety Incidents & OSHA Reporting

**Description:**  
Record incidents/near-misses; severity, location, description, involved employees; workflow for investigation and corrective actions; generate OSHA summary.

### Design Specification

- **Entities:**
  - `SafetyIncident`, `IncidentStatus`, `CorrectiveAction`.

- **Service Layer:**
  - Record incident.
  - Workflow: Open â Investigating â Resolved.
  - OSHA export.

- **Repository Layer:**
  - Find by status/date.

- **Controller:**
  - `POST /safety/incidents`
  - `GET /safety/osha-report`

- **Validation:**
  - Required fields, status transitions.

### Sample Implementation

```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String description;
    private IncidentSeverity severity;
    private String location;
    @ManyToMany
    private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
    // ...
}
```

---

## E09: Equipment & Asset Assignment

**Description:**  
Assign scanners, forklifts, and PPE to employees; track checkout/return; prevent use if certification missing; maintain asset condition state.

### Design Specification

- **Entities:**
  - `Asset`, `AssetAssignment`, `AssetCondition`.

- **Service Layer:**
  - CRUD for assets.
  - Check-in/out.
  - Block if cert missing.

- **Repository Layer:**
  - Find assignments by asset/employee.

- **Controller:**
  - `POST /assets/assign`
  - `POST /assets/return`
  - `GET /assets/history`

- **Validation:**
  - Cert check, overdue returns.

### Sample Implementation

```java
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
    // ...
}
```

---

## E10: Performance Reviews & Goals

**Description:**  
Create quarterly/annual review templates; track goals, competencies, ratings, comments; supervisor/employee acknowledgements.

### Design Specification

- **Entities:**
  - `PerformanceReview`, `ReviewTemplate`, `Goal`.

- **Service Layer:**
  - Create review cycles.
  - Assign to employees.
  - Submit/acknowledge.

- **Repository Layer:**
  - Find reviews by employee/cycle.

- **Controller:**
  - `POST /reviews/templates`
  - `POST /reviews/submit`
  - `GET /reviews/history`

- **Validation:**
  - Immutable after sign-off.

### Sample Implementation

```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ReviewTemplate template;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String comments;
    private Boolean acknowledged;
    // ...
}
```

---

## E11: Payroll Export Integration

**Description:**  
Generate payroll-ready files from approved attendance and leave; mapping to external payroll provider formats; secure delivery (SFTP/API).

### Design Specification

- **Service Layer:**
  - Generate export files.
  - Map to provider schema.
  - Secure delivery (SFTP/API).
  - Retry failed deliveries.

- **Repository Layer:**
  - Log exports.

- **Controller:**
  - `POST /payroll/export`
  - `GET /payroll/exports`

- **Validation:**
  - Totals reconcile, audit log.

### Sample Implementation

```java
@Service
public class PayrollExportService {
    public File generateExport(LocalDate periodStart, LocalDate periodEnd) { ... }
    public void deliverExport(File file) { ... }
}
```

---

## E12: Notifications & Announcements

**Description:**  
In-app and email/SMS notifications for shift changes, expiring certs, approvals, announcements; quiet hours configuration.

### Design Specification

- **Entities:**
  - `Notification`, `Announcement`, `UserPreference`.

- **Service Layer:**
  - Send notifications (in-app, email, SMS).
  - Track delivery status.
  - Rate limiting.

- **Repository Layer:**
  - Find notifications by user/status.

- **Controller:**
  - `POST /notifications/send`
  - `GET /announcements`

- **Validation:**
  - Opt-in/out, quiet hours.

### Sample Implementation

```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee recipient;
    private String message;
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;
    private NotificationStatus status;
    // ...
}
```

---

## E13: Integration Layer (HRIS/WMS APIs)

**Description:**  
Expose REST APIs and connectors for HRIS (new hires/terms), WMS (location/department), and IDP for SSO; webhooks for events.

### Design Specification

- **Service Layer:**
  - HRIS sync job.
  - WMS connector.
  - Webhook publisher/consumer.

- **Security:**
  - JWT/OAuth2 for APIs.

- **Controller:**
  - `POST /integration/hris/sync`
  - `POST /integration/webhooks`

- **OpenAPI:**
  - Documented endpoints.

### Sample Implementation

```java
@RestController
@RequestMapping("/integration/hris")
public class HRISController {
    @PostMapping("/sync")
    public ResponseEntity<?> sync(@RequestBody HRISSyncRequest req) { ... }
}
```

---

## E14: Audit Trail & Compliance

**Description:**  
Centralized audit logging for sensitive changes (employee PII, schedules, approvals, payroll); tamper-evident storage.

### Design Specification

- **Entities:**
  - `AuditLog`

- **Service Layer:**
  - Log create/update/delete with actor, timestamp, before/after.
  - Immutable storage.

- **Repository Layer:**
  - Query by date/user/entity.

- **Controller:**
  - `GET /audit/logs`

- **Validation:**
  - Coverage tests.

### Sample Implementation

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

## E15: Reporting & Analytics

**Description:**  
Operational reports: attendance, overtime, leave balances, certification status, safety KPIs; export CSV/PDF; basic role-based dashboards.

### Design Specification

- **Service Layer:**
  - Generate reports (attendance, overtime, etc).
  - Export CSV/PDF.
  - Metrics endpoints.

- **Repository Layer:**
  - Aggregate queries.

- **Controller:**
  - `GET /reports/attendance`
  - `GET /reports/overtime`
  - `GET /reports/certifications`
  - `GET /reports/safety`

- **Validation:**
  - Access control, export performance.

### Sample Implementation

```java
@RestController
@RequestMapping("/reports")
public class ReportsController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> attendanceReport(@RequestParam ...) { ... }
}
```

---

## E16: Mobile Access (PWA)

**Description:**  
Responsive views for workers to clock-in/out, view schedules, request leave, see announcements; offline-friendly via PWA.

### Design Specification

- **Frontend:**
  - PWA manifest, service worker.
  - Responsive UI for core flows.

- **Backend:**
  - REST APIs for mobile.
  - Offline queue for clock events.

- **Security:**
  - JWT/OAuth2 for mobile.

### Sample Implementation

```json
// manifest.json
{
  "name": "Warehouse Employee App",
  "short_name": "WMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

---

## E17: Onboarding & Offboarding Workflow

**Description:**  
Automate provisioning of accounts, initial schedule, required training; deprovision access and assets on termination.

### Design Specification

- **Service Layer:**
  - Detect new hires from HRIS.
  - Generate onboarding tasks (training, asset assignment).
  - Offboarding: revoke access, collect assets, update schedules.

- **Repository Layer:**
  - Track onboarding/offboarding status.

- **Controller:**
  - `POST /onboarding/start`
  - `POST /offboarding/start`

- **Validation:**
  - All tasks completed before status change.

### Sample Implementation

```java
@Service
public class OnboardingService {
    public void startOnboarding(Long employeeId) { ... }
    public void startOffboarding(Long employeeId) { ... }
}
```

---

# General Best Practices

- **Error Handling:**  
  Use `@ControllerAdvice` for global exception handling. Return meaningful error messages and HTTP status codes.

- **Validation:**  
  Use `@Valid` and custom validators for DTOs.

- **Transaction Management:**  
  Use `@Transactional` on service methods that modify data.

- **Security:**  
  Secure all endpoints, use principle of least privilege.

- **Testing:**  
  Write unit and integration tests for all layers.

- **Documentation:**  
  Use OpenAPI/Swagger for API documentation.

---

**End of Document**