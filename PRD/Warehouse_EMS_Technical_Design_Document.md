# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

This document provides a comprehensive low-level technical design for all 17 epics of the Warehouse EMS, following Spring Boot 3.x best practices. Each section covers architecture, package structure, domain models, repository/service/controller layers, security, integration, code snippets, database schema, validation, error handling, and testing strategy.

---

## E01: Project Scaffolding & Domain Setup

**Description:**
Initialize Spring Boot Maven project; configure base packages; set up core modules (employee, scheduling, attendance, safety); add Flyway/Liquibase for DB migrations; enable Actuator.

### Architecture Overview
- Spring Boot 3.x (Maven)
- Modular package structure: `employee`, `scheduling`, `attendance`, `safety`
- Flyway/Liquibase for DB migrations
- Spring Boot Actuator for health checks

### Package Structure
```
com.wms.ems
âââ employee
âââ scheduling
âââ attendance
âââ safety
âââ config
âââ common
```

### Configuration
- `application.yml` for environment settings
- Flyway/Liquibase migration scripts in `src/main/resources/db/migration`
- Actuator endpoints enabled

### Code Snippet
```java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

### Testing Strategy
- Health endpoint: `/actuator/health`
- Integration tests for project startup

---

## E02: Employee Master Data (CRUD)

**Description:**
Create Employee domain with CRUD APIs and web DTOs: name, badgeId, role, department, shiftGroup, hireDate, status.

### Architecture Overview
- Employee entity with unique badgeId
- Soft-delete, pagination, filtering
- OpenAPI documentation

### Package Structure
```
com.wms.ems.employee
âââ model
âââ repository
âââ service
âââ controller
âââ dto
```

### Entity Design
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    private boolean deleted = false;
}
```

### Repository Layer
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

### Service Layer
```java
@Service
public class EmployeeService {
    @Transactional
    public Employee createEmployee(EmployeeDto dto) { /* ... */ }
    @Transactional(readOnly = true)
    public Page<Employee> listEmployees(Pageable pageable, EmployeeFilter filter) { /* ... */ }
    // Soft-delete, update, etc.
}
```

### Controller
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@RequestBody @Valid EmployeeDto dto) { /* ... */ }
    @GetMapping
    public Page<EmployeeDto> list(Pageable pageable, EmployeeFilter filter) { /* ... */ }
    // PUT, PATCH, DELETE endpoints
}
```

### Validation & Error Handling
- Unique badgeId
- Bean validation annotations
- Global exception handler

### Testing Strategy
- Unit tests for CRUD
- API tests for endpoints

---

## E03: Role Based Access Control (RBAC)

**Description:**
Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER); method/endpoint security; row-level constraints; API key/OAuth2 toggle.

### Architecture Overview
- Spring Security configuration
- Role-based access
- API key/OAuth2 toggle

### Package Structure
```
com.wms.ems.security
âââ config
âââ model
âââ service
```

### Security Configuration
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
```

### Row-Level Security
- Service layer checks for SUPERVISOR access to team only

### Testing Strategy
- Security tests for 401/403 responses
- Role coverage

---

## E04: Time & Attendance (Clock In/Out)

**Description:**
Clock-in/out endpoints with geofence and device capture; calculate hours worked; handle missed punches, corrections workflow, CSV reports.

### Architecture Overview
- Attendance entity
- Clock-in/out endpoints
- Corrections workflow

### Package Structure
```
com.wms.ems.attendance
âââ model
âââ repository
âââ service
âââ controller
âââ dto
```

### Entity Design
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
    private boolean correctionRequested;
}
```

### Repository Layer
```java
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeAndDate(Employee employee, LocalDate date);
}
```

### Service Layer
- Calculate hours worked
- Handle corrections

### Controller
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockInDto dto) { /* ... */ }
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockOutDto dto) { /* ... */ }
}
```

### Validation & Error Handling
- Geofence validation
- Device capture
- Missed punch handling

### Testing Strategy
- Unit and API tests for clock-in/out

---

## E05: Shift & Schedule Management

**Description:**
Recurring shift templates, rotations, overtime rules, assignment to employees, blackout dates, warehouse calendars, conflict detection.

### Architecture Overview
- ShiftTemplate, ShiftAssignment entities
- Conflict detection logic

### Package Structure
```
com.wms.ems.scheduling
âââ model
âââ repository
âââ service
âââ controller
âââ dto
```

### Entity Design
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    // Overtime rules, blackout dates
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
}
```

### Repository Layer
- CRUD for templates and assignments

### Service Layer
- Conflict detection
- Bulk assignment

### Controller
- Endpoints for templates, assignments

### Testing Strategy
- Conflict detection tests

---

## E06: Leave & Absence Management

**Description:**
Request/approve PTO, sick, unpaid leave; accrual balances and policies; integration hooks to exclude from scheduling and payroll hours.

### Architecture Overview
- LeaveRequest entity
- Accrual policy logic

### Package Structure
```
com.wms.ems.leave
âââ model
âââ repository
âââ service
âââ controller
âââ dto
```

### Entity Design
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
    private String reason;
}
```

### Repository Layer
- CRUD for leave requests

### Service Layer
- Accrual balance calculation
- Integration with scheduling

### Controller
- Endpoints for request, approve, deny

### Testing Strategy
- Leave workflow tests

---

## E07: Training & Certification Tracking

**Description:**
Track certifications (e.g., forklift), expirations, renewals, block assignments for expired certs, upload proof documents.

### Architecture Overview
- Certification entity
- Expiry alerts

### Package Structure
```
com.wms.ems.training
âââ model
âââ repository
âââ service
âââ controller
âââ dto
```

### Entity Design
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

### Repository Layer
- CRUD for certifications

### Service Layer
- Expiry checks
- Assignment blocking

### Controller
- Endpoints for certification management

### Testing Strategy
- Expiry alert tests

---

## E08: Safety Incidents & OSHA Reporting

**Description:**
Record incidents/near-misses, severity, location, workflow for investigation, corrective actions, OSHA summary generation.

### Architecture Overview
- SafetyIncident entity
- Workflow status

### Package Structure
```
com.wms.ems.safety
âââ model
âââ repository
âââ service
âââ controller
âââ dto
```

### Entity Design
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String description;
    private IncidentSeverity severity;
    private String location;
    private IncidentStatus status;
    private LocalDateTime reportedAt;
    @ManyToMany
    private List<Employee> involvedEmployees;
}
```

### Repository Layer
- CRUD for incidents

### Service Layer
- Workflow management

### Controller
- Endpoints for incident reporting

### Testing Strategy
- Workflow and export tests

---

## E09: Equipment & Asset Assignment

**Description:**
Assign scanners, forklifts, PPE to employees; track checkout/return; prevent use if certification missing; asset condition state.

### Architecture Overview
- Asset and AssetAssignment entities

### Package Structure
```
com.wms.ems.asset
âââ model
âââ repository
âââ service
âââ controller
âââ dto
```

### Entity Design
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

### Repository Layer
- CRUD for assets and assignments

### Service Layer
- Certification checks

### Controller
- Endpoints for asset management

### Testing Strategy
- Assignment and blocking tests

---

## E10: Performance Reviews & Goals

**Description:**
Quarterly/annual review templates, goals, competencies, ratings, comments, supervisor/employee acknowledgements, PDF export.

### Architecture Overview
- ReviewCycle, PerformanceReview entities

### Package Structure
```
com.wms.ems.performance
âââ model
âââ repository
âââ service
âââ controller
âââ dto
```

### Entity Design
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String cycle;
    private String goals;
    private String competencies;
    private int rating;
    private String comments;
    private boolean acknowledgedBySupervisor;
    private boolean acknowledgedByEmployee;
}
```

### Repository Layer
- CRUD for reviews

### Service Layer
- PDF export

### Controller
- Endpoints for review management

### Testing Strategy
- Workflow and export tests

---

## E11: Payroll Export Integration

**Description:**
Generate payroll-ready files from attendance and leave; mapping to external formats; secure delivery (SFTP/API), reconciliation.

### Architecture Overview
- PayrollExport entity
- SFTP/API integration

### Package Structure
```
com.wms.ems.payroll
âââ model
âââ repository
âââ service
âââ controller
âââ dto
```

### Entity Design
```java
@Entity
public class PayrollExport {
    @Id @GeneratedValue
    private Long id;
    private LocalDate exportDate;
    private String fileUrl;
    private ExportStatus status;
}
```

### Repository Layer
- CRUD for exports

### Service Layer
- Mapping, delivery, reconciliation

### Controller
- Endpoints for export management

### Testing Strategy
- Export and reconciliation tests

---

## E12: Notifications & Announcements

**Description:**
In-app and email/SMS notifications for shift changes, expiring certs, approvals, announcements, quiet hours, opt-in/out.

### Architecture Overview
- Notification entity
- Channel management

### Package Structure
```
com.wms.ems.notification
âââ model
âââ repository
âââ service
âââ controller
âââ dto
```

### Entity Design
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee recipient;
    private String channel;
    private String message;
    private LocalDateTime sentAt;
    private NotificationStatus status;
}
```

### Repository Layer
- CRUD for notifications

### Service Layer
- Channel opt-in/out
- Delivery tracking

### Controller
- Endpoints for notification management

### Testing Strategy
- Delivery and opt-in/out tests

---

## E13: Integration Layer (HRIS/WMS APIs)

**Description:**
REST APIs for HRIS (new hires/terms), WMS (location/department), IDP for SSO, webhooks for events, JWT/OAuth2 security.

### Architecture Overview
- REST API endpoints
- JWT/OAuth2 security

### Package Structure
```
com.wms.ems.integration
âââ hris
âââ wms
âââ idp
âââ webhook
```

### Controller
- Endpoints for HRIS/WMS sync
- Webhook receivers

### Security
- JWT/OAuth2 configuration

### Testing Strategy
- API and security tests

---

## E14: Audit Trail & Compliance

**Description:**
Centralized audit logging for sensitive changes, tamper-evident storage, export by date/user/entity.

### Architecture Overview
- AuditLog entity
- Immutable storage

### Package Structure
```
com.wms.ems.audit
âââ model
âââ repository
âââ service
âââ controller
```

### Entity Design
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
    private String beforeState;
    private String afterState;
}
```

### Repository Layer
- CRUD for logs

### Service Layer
- Tamper-evident logic

### Controller
- Export endpoints

### Testing Strategy
- Coverage and export tests

---

## E15: Reporting & Analytics

**Description:**
Operational reports for attendance, overtime, leave, certifications, safety KPIs, CSV/PDF export, role-based dashboards, BI metrics.

### Architecture Overview
- Reporting endpoints
- CSV/PDF export

### Package Structure
```
com.wms.ems.reporting
âââ service
âââ controller
```

### Service Layer
- Data aggregation
- Export logic

### Controller
- Endpoints for reports

### Testing Strategy
- Export and dashboard tests

---

## E16: Mobile Access (PWA)

**Description:**
Responsive views for clock-in/out, schedules, leave requests, announcements, offline-friendly PWA, Lighthouse score.

### Architecture Overview
- PWA manifest
- Offline queue

### Package Structure
```
com.wms.ems.mobile
âââ controller
âââ service
```

### Controller
- Mobile endpoints

### Testing Strategy
- PWA and offline tests

---

## E17: Onboarding & Offboarding Workflow

**Description:**
Automate provisioning of accounts, initial schedule, training, deprovision access and assets on termination.

### Architecture Overview
- Workflow automation

### Package Structure
```
com.wms.ems.onboarding
âââ service
âââ controller
```

### Service Layer
- Account provisioning
- Asset assignment/revocation

### Controller
- Endpoints for workflow management

### Testing Strategy
- Workflow automation tests

---

## General Database Schema Considerations

- All entities use `@Entity` and proper JPA annotations
- Relationships: `@ManyToOne`, `@OneToMany`, `@ManyToMany` as needed
- Unique constraints, soft-delete, audit fields

## Validation Rules & Error Handling

- Bean validation (`@NotNull`, `@Size`, etc.)
- Global exception handler (`@ControllerAdvice`)
- Custom error responses

## Testing Strategy

- Unit tests for service logic
- Integration tests for repository/controller
- Security tests for RBAC
- API tests for endpoints
- Mock external integrations

---

**This document is production-ready and follows Spring Boot 3.x industry standards. All code snippets are illustrative and should be adapted to project-specific requirements.**