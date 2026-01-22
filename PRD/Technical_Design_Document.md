# Warehouse Employee Management System (EMS) - Technical Design Document

## Table of Contents
1. Project Scaffolding & Domain Setup (E01)
2. Employee Master Data (CRUD) (E02)
3. Role Based Access Control (RBAC) (E03)
4. Time & Attendance (Clock In/Out) (E04)
5. Shift & Schedule Management (E05)
6. Leave & Absence Management (E06)
7. Training & Certification Tracking (E07)
8. Safety Incidents & OSHA Reporting (E08)
9. Equipment & Asset Assignment (E09)
10. Performance Reviews & Goals (E10)
11. Payroll Export Integration (E11)
12. Notifications & Announcements (E12)
13. Integration Layer (HRIS/WMS APIs) (E13)
14. Audit Trail & Compliance (E14)
15. Reporting & Analytics (E15)
16. Mobile Access (PWA) (E16)
17. Onboarding & Offboarding Workflow (E17)

---

## Section: Project Scaffolding & Domain Setup (E01)
### Description:
Initialize Spring Boot (Maven) project; configure base packages; set up core modules (employee, scheduling, attendance, safety); add Flyway/Liquibase for DB migrations; enable Actuator.

### Design Specification:
- **Spring Boot Architecture:** Layered architecture (Controller, Service, Repository, Domain).
- **Package Structure:**
  - `com.warehouse.ems`
    - `employee`
    - `scheduling`
    - `attendance`
    - `safety`
    - `config`
    - `common`
- **Modules:** Each domain as a package/module.
- **DB Migration:** Flyway/Liquibase for schema evolution.
- **Monitoring:** Spring Boot Actuator enabled.

### Sample Implementation:
```java
// pom.xml dependencies
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```java
// Application.java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

---

## Section: Employee Master Data (CRUD) (E02)
### Description:
Create Employee domain with CRUD APIs and web DTOs: name, badgeId, role, department, shiftGroup, hireDate, status.

### Design Specification:
- **Entity Design:**
  - Employee: id, name, badgeId (unique), role, department, shiftGroup, hireDate, status (active/inactive), soft-delete flag.
- **JPA Relationships:**
  - Department, ShiftGroup as separate entities.
- **Service Layer:**
  - EmployeeService: CRUD, filtering, pagination, soft-delete.
- **Repository Layer:**
  - EmployeeRepository: extends JpaRepository<Employee, Long>, custom queries for filtering.
- **Controller:**
  - REST endpoints: POST/GET/PUT/PATCH/DELETE `/employees`.
- **Validation:**
  - Unique badgeId, DTO validation.
- **OpenAPI:**
  - Schemas and examples.

### Sample Implementation:
```java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    private String name;
    @Column(unique = true)
    private String badgeId;
    @Enumerated(EnumType.STRING)
    private Role role;
    @ManyToOne
    private Department department;
    @ManyToOne
    private ShiftGroup shiftGroup;
    private LocalDate hireDate;
    private boolean active;
    private boolean deleted;
}
```

```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public EmployeeDto create(@Valid @RequestBody EmployeeDto dto) {...}
    @GetMapping
    public Page<EmployeeDto> list(Pageable pageable, @RequestParam Map<String, String> filters) {...}
    @PutMapping("/{id}")
    public EmployeeDto update(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) {...}
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {...}
}
```

---

## Section: Role Based Access Control (RBAC) (E03)
### Description:
Add Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER); method/endpoint security and row-level constraints; API key/OAuth2 toggle via config.

### Design Specification:
- **Spring Security:**
  - Roles: ADMIN, HR, SUPERVISOR, WORKER.
  - Method security: `@PreAuthorize` annotations.
  - Endpoint security: `/employees/**` restricted by role.
  - API key/OAuth2 toggle via `application.yml`.
- **Row-level constraints:**
  - SUPERVISOR can only access team members.
- **Security Tests:**
  - Unauthorized (401), forbidden (403).

### Sample Implementation:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .anyRequest().authenticated()
            .and().oauth2Login();
    }
}
```

```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == principal.department)")
public Employee getEmployee(Long id) {...}
```

---

## Section: Time & Attendance (Clock In/Out) (E04)
### Description:
Endpoints for clock-in/out events with geofence (optional) and device capture; calculate hours worked per shift; handle missed punches and corrections workflow.

### Design Specification:
- **Entity Design:**
  - AttendanceEvent: id, employee, timestamp, type (IN/OUT), deviceId, location.
- **Service Layer:**
  - AttendanceService: clock-in/out, calculate hours, corrections.
- **Controller:**
  - POST `/attendance/clock-in`, `/attendance/clock-out`.
- **Workflow:**
  - Missed punches create approval tasks.
- **Reporting:**
  - Daily totals, CSV export.

### Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime timestamp;
    @Enumerated(EnumType.STRING)
    private EventType type; // IN, OUT
    private String deviceId;
    private String location;
}
```

```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public void clockIn(@RequestBody ClockEventDto dto) {...}
    @PostMapping("/clock-out")
    public void clockOut(@RequestBody ClockEventDto dto) {...}
}
```

---

## Section: Shift & Schedule Management (E05)
### Description:
Create recurring shift templates, rotations, overtime rules, and assignment to employees; handle blackout dates and warehouse operation calendars.

### Design Specification:
- **Entity Design:**
  - ShiftTemplate, ShiftAssignment, BlackoutDate.
- **Service Layer:**
  - ShiftService: CRUD, conflict detection, bulk assignment.
- **Controller:**
  - `/shifts/templates`, `/shifts/assignments` endpoints.
- **Audit:**
  - Audit entries for changes.

### Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime start;
    private LocalTime end;
    private boolean recurring;
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

---

## Section: Leave & Absence Management (E06)
### Description:
Request/approve PTO, sick, unpaid leave; accrual balances and policies; integration hooks to exclude from scheduling and payroll hours.

### Design Specification:
- **Entity Design:**
  - LeaveRequest: id, employee, type, startDate, endDate, status, balance.
- **Service Layer:**
  - LeaveService: request, approve/deny, update balances.
- **Controller:**
  - `/leave/requests` endpoints.
- **Integration:**
  - Exclude from scheduling/payroll.

### Sample Implementation:
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
    private int balance;
}
```

---

## Section: Training & Certification Tracking (E07)
### Description:
Track required certifications (e.g., forklift), expirations, renewals; block assignment to tasks requiring expired certs; upload proof documents.

### Design Specification:
- **Entity Design:**
  - Certification: id, employee, type, expiryDate, documentUrl.
- **Service Layer:**
  - CertificationService: CRUD, expiry alerts, assignment checks.
- **Controller:**
  - `/certifications` endpoints.
- **Integration:**
  - Block assignments if expired.

### Sample Implementation:
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
}
```

---

## Section: Safety Incidents & OSHA Reporting (E08)
### Description:
Record incidents/near-misses; severity, location, description, involved employees; workflow for investigation and corrective actions; generate OSHA summary.

### Design Specification:
- **Entity Design:**
  - SafetyIncident: id, severity, location, description, status, involvedEmployees.
- **Service Layer:**
  - SafetyService: record, workflow, reporting.
- **Controller:**
  - `/safety/incidents` endpoints.
- **Reporting:**
  - OSHA export.

### Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String severity;
    private String location;
    private String description;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
    @ManyToMany
    private List<Employee> involvedEmployees;
}
```

---

## Section: Equipment & Asset Assignment (E09)
### Description:
Assign scanners, forklifts, and PPE to employees; track checkout/return; prevent use if certification missing; maintain asset condition state.

### Design Specification:
- **Entity Design:**
  - Asset, AssetAssignment, AssetCondition.
- **Service Layer:**
  - AssetService: CRUD, check-in/out, certification checks.
- **Controller:**
  - `/assets`, `/assets/assignments` endpoints.
- **Integration:**
  - Block if certs invalid.

### Sample Implementation:
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

---

## Section: Performance Reviews & Goals (E10)
### Description:
Create quarterly/annual review templates; track goals, competencies, ratings, comments; supervisor/employee acknowledgements.

### Design Specification:
- **Entity Design:**
  - PerformanceReview, Goal, Competency.
- **Service Layer:**
  - ReviewService: create cycles, assign, submit, acknowledge.
- **Controller:**
  - `/reviews` endpoints.
- **Export:**
  - PDF export.

### Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String comments;
    private boolean acknowledged;
}
```

---

## Section: Payroll Export Integration (E11)
### Description:
Generate payroll-ready files from approved attendance and leave; mapping to external payroll provider formats; secure delivery (SFTP/API).

### Design Specification:
- **Service Layer:**
  - PayrollExportService: generate, map, deliver, retry.
- **Integration:**
  - SFTP/API delivery, audit log.
- **Controller:**
  - `/payroll/export` endpoint.

### Sample Implementation:
```java
@Service
public class PayrollExportService {
    public void exportPayroll(LocalDate period) {
        // gather attendance/leave, map to provider format, deliver via SFTP/API
    }
}
```

---

## Section: Notifications & Announcements (E12)
### Description:
In-app and email/SMS notifications for shift changes, expiring certs, approvals, announcements; quiet hours configuration.

### Design Specification:
- **Entity Design:**
  - Notification, Announcement.
- **Service Layer:**
  - NotificationService: send, track, rate-limit.
- **Controller:**
  - `/notifications`, `/announcements` endpoints.
- **Integration:**
  - Email/SMS providers.

### Sample Implementation:
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private String channel;
    private String message;
    private LocalDateTime sentAt;
    private boolean delivered;
}
```

---

## Section: Integration Layer (HRIS/WMS APIs) (E13)
### Description:
Expose REST APIs and connectors for HRIS (new hires/terms), WMS (location/department), and IDP for SSO; webhooks for events.

### Design Specification:
- **API Layer:**
  - JWT/OAuth2-secured endpoints.
  - HRIS sync job.
  - WMS department/location mapping.
  - Webhooks for events.
- **OpenAPI:**
  - Documented endpoints.

### Sample Implementation:
```java
@RestController
@RequestMapping("/api/hris")
public class HrisController {
    @PostMapping("/sync")
    public void syncEmployees(@RequestBody List<EmployeeDto> dtos) {...}
}
```

---

## Section: Audit Trail & Compliance (E14)
### Description:
Centralized audit logging for sensitive changes (employee PII, schedules, approvals, payroll); tamper-evident storage.

### Design Specification:
- **Entity Design:**
  - AuditLog: id, actor, timestamp, entity, before, after.
- **Service Layer:**
  - AuditService: log create/update/delete.
- **Controller:**
  - `/audit/logs` endpoint.
- **Storage:**
  - Immutable log table.

### Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    @Lob
    private String before;
    @Lob
    private String after;
}
```

---

## Section: Reporting & Analytics (E15)
### Description:
Operational reports: attendance, overtime, leave balances, certification status, safety KPIs; export CSV/PDF; basic role-based dashboards.

### Design Specification:
- **Service Layer:**
  - ReportingService: filter, aggregate, export.
- **Controller:**
  - `/reports` endpoints.
- **Export:**
  - CSV/PDF generation.

### Sample Implementation:
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> exportAttendance(@RequestParam LocalDate from, @RequestParam LocalDate to) {...}
}
```

---

## Section: Mobile Access (PWA) (E16)
### Description:
Responsive views for workers to clock-in/out, view schedules, request leave, see announcements; offline-friendly via PWA.

### Design Specification:
- **Frontend:**
  - PWA manifest, service worker.
  - Offline queue for clock events.
- **Backend:**
  - Endpoints compatible with mobile flows.

### Sample Implementation:
```json
// manifest.json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

---

## Section: Onboarding & Offboarding Workflow (E17)
### Description:
Automate provisioning of accounts, initial schedule, required training; deprovision access and assets on termination.

### Design Specification:
- **Service Layer:**
  - OnboardingService: provision accounts, assign training/assets.
  - OffboardingService: revoke access, collect assets, update schedules.
- **Integration:**
  - HRIS triggers, asset management.

### Sample Implementation:
```java
@Service
public class OnboardingService {
    public void onboard(Employee employee) {
        // create account, assign schedule, training, assets
    }
    public void offboard(Employee employee) {
        // revoke access, collect assets, update schedule
    }
}
```

---

# [Further sections for E18-E20 and additional stories can be added following the above structure.]

---

## General Configuration & Best Practices
- Use `application.yml` for environment-specific settings.
- Enable CORS for APIs.
- Use DTOs for all external API communication.
- Exception handling via `@ControllerAdvice`.
- Unit and integration tests for all service and controller layers.
- OpenAPI/Swagger documentation auto-generated.
- Use Spring Profiles for dev/test/prod separation.

---

## End of Document
