# Technical Design Document: Warehouse Employee Management System (EMS)

## Table of Contents
- [E01: Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
- [E02: Employee Master Data CRUD](#e02-employee-master-data-crud)
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

## E01: Project Scaffolding & Domain Setup
Section: Project Initialization and Base Configuration
Description: Establishes the foundational structure for the Spring Boot application, including Maven setup, base package organization, database migration tooling, and health monitoring.
Design Specification:
- **Spring Boot Maven Project**: `pom.xml` with dependencies for Spring Boot Starter Web, Data JPA, Security, Actuator, Flyway/Liquibase, Lombok, Validation, etc.
- **Package Structure**:
  - `com.company.ems` (root)
    - `employee`, `scheduling`, `attendance`, `safety`, `config`, `common`
- **Flyway/Liquibase**: `src/main/resources/db/migration` for versioned SQL scripts
- **Actuator**: Enabled in `application.yml` with `/actuator/health` endpoint
- **README**: Build/run instructions
Sample Implementation:
```java
// src/main/java/com/company/ems/EmsApplication.java
@SpringBootApplication
public class EmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmsApplication.class, args);
    }
}
```
```yaml
# src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ems
    username: ems_user
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

---

## E02: Employee Master Data CRUD
Section: Employee Domain CRUD APIs
Description: Implements the Employee entity with full CRUD operations, enforcing unique badgeId, soft-delete, pagination, filtering, and OpenAPI documentation.
Design Specification:
- **Package Structure**: `com.company.ems.employee`
- **Domain Model**:
  - `Employee` entity: id, name, badgeId (unique), role, department, shiftGroup, hireDate, status, deleted
- **Repository Layer**:
  - `EmployeeRepository extends JpaRepository<Employee, Long>`
  - Custom: `findByBadgeId`, `findAllByDeletedFalse`, filtering methods
- **Service Layer**:
  - `EmployeeService`: CRUD, validation, soft-delete, business rules
- **Controller Layer**:
  - `EmployeeController`: REST endpoints `/employees` (GET, POST, PUT, PATCH, DELETE)
  - DTOs: `EmployeeRequest`, `EmployeeResponse`
- **Configuration**:
  - OpenAPI/Swagger config
- **Integration Points**: None for core CRUD
Sample Implementation:
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    private boolean deleted = false;
    // getters/setters
}

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}

@Service
public class EmployeeService {
    @Transactional
    public Employee createEmployee(EmployeeRequest req) { /* ... */ }
    public Page<Employee> listEmployees(Pageable pageable, String filter) { /* ... */ }
    @Transactional
    public void softDeleteEmployee(Long id) { /* ... */ }
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@RequestBody @Valid EmployeeRequest req) { /* ... */ }
    @GetMapping
    public Page<EmployeeResponse> list(Pageable pageable, @RequestParam Optional<String> filter) { /* ... */ }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { /* ... */ }
}
```

---

## E03: Role Based Access Control (RBAC)
Section: Security and Authorization
Description: Implements Spring Security with roles, endpoint/method security, row-level constraints, and API key/OAuth2 toggle.
Design Specification:
- **Package Structure**: `com.company.ems.security`
- **Domain Model**: `User`, `Role` enums (ADMIN, HR, SUPERVISOR, WORKER)
- **Repository Layer**: `UserRepository`, `RoleRepository`
- **Service Layer**: `UserDetailsServiceImpl`, RBAC logic
- **Controller Layer**: Auth endpoints `/auth/login`, `/auth/token`
- **Configuration**: `SecurityConfig` with `@EnableWebSecurity`, JWT/OAuth2 toggle
- **Integration Points**: OAuth2 provider, API key validation
Sample Implementation:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/auth/**").permitAll()
                .antMatchers(HttpMethod.DELETE, "/employees/**").hasRole("ADMIN")
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .anyRequest().authenticated()
            .and()
            .oauth2Login(); // or API key filter
    }
}

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) { /* ... */ }
}
```

---

## E04: Time & Attendance (Clock In/Out)
Section: Attendance Event Capture
Description: Endpoints for clock-in/out, geofence/device capture, shift association, missed punch correction, and reporting.
Design Specification:
- **Package Structure**: `com.company.ems.attendance`
- **Domain Model**: `AttendanceEvent` (id, employee, type, timestamp, device, location, status)
- **Repository Layer**: `AttendanceRepository`, custom queries for shift totals
- **Service Layer**: `AttendanceService` (clock-in/out, validation, correction workflow)
- **Controller Layer**: `/attendance/clock-in`, `/attendance/clock-out`, `/attendance/corrections`
- **Configuration**: Geofence config, device validation
- **Integration Points**: None (core)
Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @Enumerated(EnumType.STRING)
    private EventType type; // CLOCK_IN, CLOCK_OUT
    private LocalDateTime timestamp;
    private String deviceId;
    private String location;
    @Enumerated(EnumType.STRING)
    private Status status; // NORMAL, CORRECTION_PENDING
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody AttendanceRequest req) { /* ... */ }
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody AttendanceRequest req) { /* ... */ }
    @PostMapping("/corrections")
    public ResponseEntity<?> requestCorrection(@RequestBody CorrectionRequest req) { /* ... */ }
}
```

---

## E05: Shift & Schedule Management
Section: Shift Templates, Rotations, and Assignment
Description: Recurring shift templates, rotations, overtime, blackout dates, and assignment APIs.
Design Specification:
- **Package Structure**: `com.company.ems.scheduling`
- **Domain Model**: `ShiftTemplate`, `ShiftAssignment`, `WarehouseCalendar`
- **Repository Layer**: `ShiftTemplateRepository`, `ShiftAssignmentRepository`
- **Service Layer**: `SchedulingService` (conflict detection, bulk assignment)
- **Controller Layer**: `/shifts`, `/schedules`, `/calendars`
- **Configuration**: Overtime rules, blackout dates
- **Integration Points**: None (core)
Sample Implementation:
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

@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping
    public ResponseEntity<?> createShift(@RequestBody ShiftTemplateRequest req) { /* ... */ }
    @GetMapping
    public List<ShiftTemplateResponse> listShifts() { /* ... */ }
}
```

---

## E06: Leave & Absence Management
Section: Leave Requests, Approvals, and Balances
Description: PTO/sick/unpaid leave requests, approval workflow, accruals, and integration with scheduling/payroll.
Design Specification:
- **Package Structure**: `com.company.ems.leave`
- **Domain Model**: `LeaveRequest`, `LeaveBalance`, `LeavePolicy`
- **Repository Layer**: `LeaveRequestRepository`, `LeaveBalanceRepository`
- **Service Layer**: `LeaveService` (request, approve, accruals)
- **Controller Layer**: `/leave/requests`, `/leave/balances`
- **Configuration**: Leave policies
- **Integration Points**: Scheduling, payroll
Sample Implementation:
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
    private Status status; // REQUESTED, APPROVED, DENIED
}

@RestController
@RequestMapping("/leave/requests")
public class LeaveController {
    @PostMapping
    public ResponseEntity<?> requestLeave(@RequestBody LeaveRequestDto req) { /* ... */ }
    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveLeave(@PathVariable Long id) { /* ... */ }
}
```

---

## E07: Training & Certification Tracking
Section: Certification Management
Description: Track certifications, expirations, renewals, and block assignments for expired certs. Upload proof documents.
Design Specification:
- **Package Structure**: `com.company.ems.certification`
- **Domain Model**: `Certification`, `EmployeeCertification`
- **Repository Layer**: `CertificationRepository`, `EmployeeCertificationRepository`
- **Service Layer**: `CertificationService` (alerts, assignment checks)
- **Controller Layer**: `/certifications`, `/employee-certifications`
- **Configuration**: Expiry alerts
- **Integration Points**: Scheduling, asset assignment
Sample Implementation:
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

## E08: Safety Incidents & OSHA Reporting
Section: Incident Reporting and Workflow
Description: Record incidents, workflow for investigation, corrective actions, and OSHA summary generation.
Design Specification:
- **Package Structure**: `com.company.ems.safety`
- **Domain Model**: `SafetyIncident`, `IncidentStatus`, `CorrectiveAction`
- **Repository Layer**: `SafetyIncidentRepository`, `CorrectiveActionRepository`
- **Service Layer**: `SafetyService` (workflow, reporting)
- **Controller Layer**: `/safety/incidents`, `/safety/reports`
- **Configuration**: OSHA fields
- **Integration Points**: Reporting
Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String description;
    private String location;
    private LocalDateTime occurredAt;
    @Enumerated(EnumType.STRING)
    private IncidentSeverity severity;
    @ManyToMany
    private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
}
```

---

## E09: Equipment & Asset Assignment
Section: Asset Tracking and Assignment
Description: Assign assets to employees, track check-in/out, block use if certification missing, maintain asset condition.
Design Specification:
- **Package Structure**: `com.company.ems.asset`
- **Domain Model**: `Asset`, `AssetAssignment`, `AssetCondition`
- **Repository Layer**: `AssetRepository`, `AssetAssignmentRepository`
- **Service Layer**: `AssetService` (assignment, validation)
- **Controller Layer**: `/assets`, `/asset-assignments`
- **Configuration**: Asset policies
- **Integration Points**: Certification, scheduling
Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    @Enumerated(EnumType.STRING)
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
    private LocalDateTime checkedOutAt;
    private LocalDateTime returnedAt;
}
```

---

## E10: Performance Reviews & Goals
Section: Review Cycles and Goal Tracking
Description: Create review templates, assign to employees, workflow for submission/acknowledgement, PDF export, role-based visibility.
Design Specification:
- **Package Structure**: `com.company.ems.performance`
- **Domain Model**: `PerformanceReview`, `Goal`, `Competency`
- **Repository Layer**: `PerformanceReviewRepository`, `GoalRepository`
- **Service Layer**: `PerformanceService` (review cycles, workflow)
- **Controller Layer**: `/reviews`, `/goals`
- **Configuration**: Review templates
- **Integration Points**: None (core)
Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate reviewDate;
    private String comments;
    private boolean acknowledgedByEmployee;
    private boolean acknowledgedBySupervisor;
}
```

---

## E11: Payroll Export Integration
Section: Payroll File Generation and Delivery
Description: Generate payroll-ready files from attendance/leave, map to provider formats, secure delivery (SFTP/API), audit log.
Design Specification:
- **Package Structure**: `com.company.ems.payroll`
- **Domain Model**: `PayrollExport`, `PayrollProvider`
- **Repository Layer**: `PayrollExportRepository`
- **Service Layer**: `PayrollService` (file generation, delivery, retry)
- **Controller Layer**: `/payroll/exports`
- **Configuration**: Provider mapping, SFTP/API config
- **Integration Points**: Attendance, leave, external payroll
Sample Implementation:
```java
@Entity
public class PayrollExport {
    @Id @GeneratedValue
    private Long id;
    private LocalDate exportDate;
    private String provider;
    private String filePath;
    private boolean delivered;
    private int retryCount;
}
```

---

## E12: Notifications & Announcements
Section: Notification Delivery and Management
Description: In-app/email/SMS notifications for events, quiet hours, opt-in/out, delivery status, rate limiting.
Design Specification:
- **Package Structure**: `com.company.ems.notification`
- **Domain Model**: `Notification`, `Announcement`, `UserNotificationPreference`
- **Repository Layer**: `NotificationRepository`, `AnnouncementRepository`
- **Service Layer**: `NotificationService` (delivery, templates, status)
- **Controller Layer**: `/notifications`, `/announcements`
- **Configuration**: Channel config, quiet hours
- **Integration Points**: Email/SMS providers
Sample Implementation:
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private String message;
    private String channel;
    private LocalDateTime sentAt;
    private boolean delivered;
}
```

---

## E13: Integration Layer (HRIS/WMS APIs)
Section: External API Integration
Description: Expose REST APIs and connectors for HRIS, WMS, IDP for SSO, webhooks for events.
Design Specification:
- **Package Structure**: `com.company.ems.integration`
- **Domain Model**: `IntegrationEvent`, `HrisSyncJob`, `WmsConnector`
- **Repository Layer**: `IntegrationEventRepository`
- **Service Layer**: `IntegrationService` (sync, webhooks)
- **Controller Layer**: `/api/hris`, `/api/wms`, `/api/webhooks`
- **Configuration**: JWT/OAuth2, endpoint config
- **Integration Points**: HRIS, WMS, IDP
Sample Implementation:
```java
@RestController
@RequestMapping("/api/hris")
public class HrisController {
    @PostMapping("/employees")
    public ResponseEntity<?> syncEmployee(@RequestBody HrisEmployeeDto dto) { /* ... */ }
}
```

---

## E14: Audit Trail & Compliance
Section: Audit Logging and Tamper-Evidence
Description: Centralized audit logging for sensitive changes, immutable log table, export, and test coverage.
Design Specification:
- **Package Structure**: `com.company.ems.audit`
- **Domain Model**: `AuditLog`
- **Repository Layer**: `AuditLogRepository`
- **Service Layer**: `AuditService` (log, export)
- **Controller Layer**: `/audit/logs`
- **Configuration**: Tamper-evident storage
- **Integration Points**: All modules
Sample Implementation:
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

---

## E15: Reporting & Analytics
Section: Operational Reporting and Dashboards
Description: Attendance, overtime, leave, certification, safety KPIs, CSV/PDF export, dashboards.
Design Specification:
- **Package Structure**: `com.company.ems.reporting`
- **Domain Model**: `Report`, `DashboardMetric`
- **Repository Layer**: `ReportRepository`
- **Service Layer**: `ReportingService` (generate, export)
- **Controller Layer**: `/reports`, `/metrics`
- **Configuration**: Export config
- **Integration Points**: All modules
Sample Implementation:
```java
@RestController
@RequestMapping("/reports")
public class ReportController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> exportAttendance(@RequestParam LocalDate from, @RequestParam LocalDate to) { /* ... */ }
}
```

---

## E16: Mobile Access (PWA)
Section: Mobile/PWA Support
Description: Responsive views for clock-in/out, schedules, leave, announcements, offline support.
Design Specification:
- **Package Structure**: `com.company.ems.mobile`
- **Domain Model**: N/A (front-end)
- **Repository Layer**: N/A
- **Service Layer**: Mobile API endpoints
- **Controller Layer**: `/mobile/attendance`, `/mobile/schedules`, etc.
- **Configuration**: PWA manifest, offline queue
- **Integration Points**: Attendance, scheduling, notification
Sample Implementation:
```java
@RestController
@RequestMapping("/mobile/attendance")
public class MobileAttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockInMobile(@RequestBody AttendanceRequest req) { /* ... */ }
}
```

---

## E17: Onboarding & Offboarding Workflow
Section: Employee Lifecycle Automation
Description: Automate account provisioning, initial schedule, training, asset assignment, deprovisioning on termination.
Design Specification:
- **Package Structure**: `com.company.ems.onboarding`
- **Domain Model**: `OnboardingTask`, `OffboardingTask`
- **Repository Layer**: `OnboardingTaskRepository`, `OffboardingTaskRepository`
- **Service Layer**: `OnboardingService`, `OffboardingService`
- **Controller Layer**: `/onboarding`, `/offboarding`
- **Configuration**: Workflow config
- **Integration Points**: HRIS, asset, scheduling, certification
Sample Implementation:
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String taskType;
    private boolean completed;
}
```

---

# (Repeat similar structure for all user stories under each epic as needed)

---

# End of Document
