# Warehouse Employee Management System - Low Level Technical Design Document

---

## Epic E01: Project Scaffolding & Domain Setup

**Description:**  
Initialize Spring Boot (Maven) project; configure base packages; set up core modules (employee, scheduling, attendance, safety); add Flyway/Liquibase for DB migrations; enable Actuator.

**Design Specification:**  
- **Spring Boot Architecture:**  
  - Layered architecture: Controller â Service â Repository â Domain  
  - Modular structure for separation of concerns
- **Package Structure:**  
  - `com.companyname.wems` (root)
    - `employee`, `scheduling`, `attendance`, `safety`, `common`, `config`
- **Core Modules:**  
  - Each module contains domain, repository, service, controller
- **DB Migration:**  
  - Use Flyway (or Liquibase) for schema versioning
- **Monitoring:**  
  - Enable Spring Boot Actuator endpoints
- **Build/Run:**  
  - Maven wrapper, README with build/run steps

**Sample Implementation:**
```java
// Main Application
@SpringBootApplication
public class WemsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WemsApplication.class, args);
    }
}

// application.properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5432/wems
spring.flyway.enabled=true
management.endpoints.web.exposure.include=health,info
```

---

## Epic E02: Employee Master Data (CRUD)

**Description:**  
Create Employee domain with CRUD APIs and web DTOs: name, badgeId, role, department, shiftGroup, hireDate, status.

**Design Specification:**  
- **Entity Design:**  
  - `Employee` entity: id, name, badgeId (unique), role, department, shiftGroup, hireDate, status, deleted (soft delete)
- **Repository:**  
  - `EmployeeRepository extends JpaRepository<Employee, Long>`
- **Service Layer:**  
  - CRUD operations, soft delete, filtering, pagination
- **Controller:**  
  - REST endpoints: POST/GET/PUT/PATCH/DELETE `/employees`
  - OpenAPI annotations for schema
- **Validation:**  
  - Unique badgeId, required fields

**Sample Implementation:**
```java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    @Column(nullable=false, unique=true)
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted = false;
    // getters/setters
}

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@RequestBody @Valid EmployeeDto dto) { ... }
    @GetMapping
    public Page<EmployeeDto> list(Pageable pageable, @RequestParam Map<String,String> filters) { ... }
    // PUT, PATCH, DELETE endpoints
}
```

---

## Epic E03: Role-Based Access Control (RBAC)

**Description:**  
Add Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER); method/endpoint security and row-level constraints; API key/OAuth2 toggle via config.

**Design Specification:**  
- **Security Config:**  
  - Spring Security with role hierarchy
  - Method-level security (`@PreAuthorize`)
  - API key or OAuth2 toggle via `application.properties`
- **Roles:**  
  - ADMIN, HR, SUPERVISOR, WORKER
- **Row-Level Security:**  
  - Service methods filter by user/team
- **Error Handling:**  
  - 401 for unauthorized, 403 for forbidden

**Sample Implementation:**
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .anyRequest().authenticated()
            .and()
            .oauth2Login(); // or API key filter
    }
}

@Service
public class EmployeeService {
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == principal.department)")
    public Employee updateEmployee(Employee employee) { ... }
}
```

---

## Epic E04: Time & Attendance (Clock In/Out)

**Description:**  
Endpoints for clock-in/out events with geofence (optional) and device capture; calculate hours worked per shift; handle missed punches and corrections workflow.

**Design Specification:**  
- **Entities:**  
  - `AttendanceEvent`: id, employee, type (IN/OUT), timestamp, deviceId, location, status
  - `AttendanceCorrection`: id, event, requestedBy, status, approver
- **Service:**  
  - Clock-in/out logic, shift association, missed punch detection, correction workflow
- **Controller:**  
  - `/attendance/clock-in`, `/attendance/clock-out`, `/attendance/corrections`
- **Reporting:**  
  - Export CSV

**Sample Implementation:**
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime timestamp;
    private String type; // IN/OUT
    private String deviceId;
    private String location;
    private String status; // NORMAL, CORRECTED
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockEventDto dto) { ... }
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockEventDto dto) { ... }
}
```

---

## Epic E05: Shift & Schedule Management

**Description:**  
Create recurring shift templates, rotations, overtime rules, and assignment to employees; handle blackout dates and warehouse operation calendars.

**Design Specification:**  
- **Entities:**  
  - `ShiftTemplate`, `ShiftAssignment`, `BlackoutDate`, `OperationCalendar`
- **Service:**  
  - CRUD for shifts, conflict detection, bulk assignment, audit logging
- **Controller:**  
  - `/shifts`, `/schedules`, `/blackout-dates`

**Sample Implementation:**
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

@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping
    public ResponseEntity<ShiftTemplate> create(@RequestBody ShiftTemplateDto dto) { ... }
    @GetMapping
    public List<ShiftTemplate> list() { ... }
}
```

---

## Epic E06: Leave & Absence Management

**Description:**  
Request/approve PTO, sick, unpaid leave; accrual balances and policies; integration hooks to exclude from scheduling and payroll hours.

**Design Specification:**  
- **Entities:**  
  - `LeaveRequest`, `LeaveBalance`, `LeavePolicy`
- **Service:**  
  - Request/approval workflow, balance update, integration hooks
- **Controller:**  
  - `/leaves`, `/leave-balances`, `/leave-policies`

**Sample Implementation:**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type; // PTO, Sick, Unpaid
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // REQUESTED, APPROVED, DENIED
}

@RestController
@RequestMapping("/leaves")
public class LeaveController {
    @PostMapping
    public ResponseEntity<LeaveRequest> requestLeave(@RequestBody LeaveRequestDto dto) { ... }
    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) { ... }
}
```

---

## Epic E07: Training & Certification Tracking

**Description:**  
Track required certifications (e.g., forklift), expirations, renewals; block assignment to tasks requiring expired certs; upload proof documents.

**Design Specification:**  
- **Entities:**  
  - `Certification`, `EmployeeCertification`
- **Service:**  
  - CRUD, expiry alerts, assignment checks
- **Controller:**  
  - `/certifications`, `/employee-certifications`
- **Document Upload:**  
  - Store proof documents (e.g., S3, filesystem)

**Sample Implementation:**
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private int validityMonths;
}

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
    private String proofDocumentUrl;
}
```

---

## Epic E08: Safety Incidents & OSHA Reporting

**Description:**  
Record incidents/near-misses; severity, location, description, involved employees; workflow for investigation and corrective actions; generate OSHA summary.

**Design Specification:**  
- **Entities:**  
  - `SafetyIncident`, `IncidentEmployee`, `CorrectiveAction`
- **Service:**  
  - Incident workflow, OSHA export, metrics
- **Controller:**  
  - `/safety/incidents`, `/safety/metrics`

**Sample Implementation:**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String severity;
    private String location;
    private String description;
    private String status; // OPEN, INVESTIGATING, RESOLVED
    private LocalDateTime reportedAt;
}

@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
    @PostMapping
    public ResponseEntity<SafetyIncident> report(@RequestBody SafetyIncidentDto dto) { ... }
}
```

---

## Epic E09: Equipment & Asset Assignment

**Description:**  
Assign scanners, forklifts, and PPE to employees; track checkout/return; prevent use if certification missing; maintain asset condition state.

**Design Specification:**  
- **Entities:**  
  - `Asset`, `AssetAssignment`, `AssetCondition`
- **Service:**  
  - CRUD, check-in/out, certification validation, history log
- **Controller:**  
  - `/assets`, `/asset-assignments`

**Sample Implementation:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private String condition;
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

---

## Epic E10: Performance Reviews & Goals

**Description:**  
Create quarterly/annual review templates; track goals, competencies, ratings, comments; supervisor/employee acknowledgements.

**Design Specification:**  
- **Entities:**  
  - `PerformanceReview`, `ReviewGoal`, `ReviewAcknowledgement`
- **Service:**  
  - Review cycles, workflow, PDF export, role-based visibility
- **Controller:**  
  - `/reviews`, `/review-goals`

**Sample Implementation:**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String period; // Q1-2024, 2024
    private String status; // DRAFT, SUBMITTED, SIGNED_OFF
    private LocalDateTime submittedAt;
}
```

---

## Epic E11: Payroll Export Integration

**Description:**  
Generate payroll-ready files from approved attendance and leave; mapping to external payroll provider formats; secure delivery (SFTP/API).

**Design Specification:**  
- **Service:**  
  - Export generator, provider mapping, delivery (SFTP/API), retry logic, audit log
- **Controller:**  
  - `/payroll/exports`
- **Integration:**  
  - SFTP client, REST API client

**Sample Implementation:**
```java
@Service
public class PayrollExportService {
    public File generatePayrollExport(LocalDate periodStart, LocalDate periodEnd) { ... }
    public void deliverExport(File exportFile) { ... }
}
```

---

## Epic E12: Notifications & Announcements

**Description:**  
In-app and email/SMS notifications for shift changes, expiring certs, approvals, announcements; quiet hours configuration.

**Design Specification:**  
- **Entities:**  
  - `Notification`, `Announcement`, `UserNotificationPreference`
- **Service:**  
  - Notification delivery, template localization, rate limiting
- **Controller:**  
  - `/notifications`, `/announcements`
- **Integration:**  
  - Email/SMS gateway

**Sample Implementation:**
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private String channel; // EMAIL, SMS, IN_APP
    private String message;
    private String status; // SENT, FAILED
    private LocalDateTime sentAt;
}
```

---

## Epic E13: Integration Layer (HRIS/WMS APIs)

**Description:**  
Expose REST APIs and connectors for HRIS (new hires/terms), WMS (location/department), and IDP for SSO; webhooks for events.

**Design Specification:**  
- **API Layer:**  
  - REST endpoints for HRIS, WMS, IDP
  - JWT/OAuth2 security
  - Webhook endpoints
- **Service:**  
  - Sync jobs, idempotency, OpenAPI docs

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/hris")
public class HrisController {
    @PostMapping("/employees")
    public ResponseEntity<?> syncEmployee(@RequestBody HrisEmployeeDto dto) { ... }
}
```

---

## Epic E14: Audit Trail & Compliance

**Description:**  
Centralized audit logging for sensitive changes (employee PII, schedules, approvals, payroll); tamper-evident storage.

**Design Specification:**  
- **Entities:**  
  - `AuditLog`: id, entity, entityId, actor, timestamp, before, after, action
- **Service:**  
  - Log on create/update/delete, immutable storage, export
- **Controller:**  
  - `/audit-logs`

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
    @Lob
    private String before;
    @Lob
    private String after;
    private String action;
}
```

---

## Epic E15: Reporting & Analytics

**Description:**  
Operational reports: attendance, overtime, leave balances, certification status, safety KPIs; export CSV/PDF; basic role-based dashboards.

**Design Specification:**  
- **Service:**  
  - Report generation, filtering, export, metrics endpoints
- **Controller:**  
  - `/reports`, `/metrics`
- **Integration:**  
  - CSV/PDF export libraries

**Sample Implementation:**
```java
@RestController
@RequestMapping("/reports")
public class ReportController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> attendanceReport(@RequestParam Map<String,String> filters) { ... }
}
```

---

## Epic E16: Mobile Access (PWA)

**Description:**  
Responsive views for workers to clock-in/out, view schedules, request leave, see announcements; offline-friendly via PWA.

**Design Specification:**  
- **Frontend:**  
  - PWA manifest, service worker for offline, responsive UI (e.g., Thymeleaf, React, or Angular)
- **Backend:**  
  - REST APIs for all mobile flows
- **Offline Handling:**  
  - Queue clock events, sync on reconnect, conflict resolution

**Sample Implementation:**
```json
// manifest.json
{
  "short_name": "WEMS",
  "name": "Warehouse Employee Management System",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "icons": [{ "src": "/icon-192.png", "sizes": "192x192", "type": "image/png" }]
}
```

---

## Epic E17: Onboarding & Offboarding Workflow

**Description:**  
Automate provisioning of accounts, initial schedule, required training; deprovision access and assets on termination.

**Design Specification:**  
- **Service:**  
  - HRIS integration for new hires, task generation for training/assets, offboarding workflow
- **Entities:**  
  - `OnboardingTask`, `OffboardingTask`
- **Controller:**  
  - `/onboarding`, `/offboarding`
- **Integration:**  
  - HRIS, asset management, access control

**Sample Implementation:**
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type; // TRAINING, ASSET_ASSIGNMENT, ACCOUNT_PROVISION
    private String status; // PENDING, COMPLETED
    private LocalDateTime dueDate;
}
```

---

**End of Document**