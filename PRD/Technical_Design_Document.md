# Warehouse Employee Management System - Low-Level Technical Design Document

## Document Overview

This document provides comprehensive low-level technical design specifications for the Warehouse Employee Management System built on Spring Boot 3.x. It covers all 20 epics with detailed architecture, package structure, domain models, service layers, controllers, security configurations, and sample implementations following Spring Boot best practices.

---

## Section: E01 - Project Scaffolding & Domain Setup

**Description:**
Establishes the foundational Spring Boot project structure, configures core modules, and sets up essential tools for database migration and monitoring.

**Design Specification:**
- Spring Boot 3.x Maven project with modular package structure: com.company.warehouse
- Core modules: employee, scheduling, attendance, safety
- Database migration via Flyway/Liquibase
- Spring Boot Actuator enabled for health checks
- application.yml with environment-specific profiles
- README with build/run instructions

**Sample Implementation:**
```java
// pom.xml: dependencies for spring-boot-starter, spring-boot-starter-data-jpa, spring-boot-starter-security, flyway-core, actuator
// src/main/java/com/company/warehouse/WarehouseApplication.java
@SpringBootApplication
public class WarehouseApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseApplication.class, args);
    }
}

// application.yml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse
    username: warehouse_user
    password: secret
  flyway:
    enabled: true
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

---

## Section: E02 - Employee Master Data (CRUD)

**Description:**
Manages employee records with full CRUD operations, ensuring unique badge IDs and soft deletion.

**Design Specification:**
- Package: com.company.warehouse.employee
- Entity: Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService with validation, soft-delete, pagination/filtering
- Controller: EmployeeController with REST endpoints, DTOs, OpenAPI annotations
- Security: Method-level security for role-based access
- application.yml: badgeId uniqueness constraint

**Sample Implementation:**
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue private Long id;
    @Column(nullable = false) private String name;
    @Column(name = "badge_id", nullable = false, unique = true) private String badgeId;
    @Enumerated(EnumType.STRING) private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING) private Status status;
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
    public Employee createEmployee(EmployeeDTO dto) { /* validate, map, save */ }
    @Transactional
    public void softDeleteEmployee(Long id) { /* set deleted=true */ }
    // other CRUD methods
}

@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee management APIs")
public class EmployeeController {
    @PostMapping public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeDTO dto) { /* ... */ }
    @GetMapping public Page<EmployeeDTO> list(Pageable pageable) { /* ... */ }
    // other endpoints
}
```

---

## Section: E03 - Role-Based Access Control (RBAC)

**Description:**
Implements security with roles (ADMIN, HR, SUPERVISOR, WORKER), endpoint/method security, and row-level constraints.

**Design Specification:**
- SecurityConfig: Configures Spring Security, JWT/OAuth2, method security
- Roles: Enum with ADMIN, HR, SUPERVISOR, WORKER
- @PreAuthorize annotations on service/controller methods
- API key/OAuth2 toggle via application.yml
- Row-level security in repositories/services

**Sample Implementation:**
```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .anyRequest().authenticated())
            .oauth2ResourceServer().jwt();
        return http.build();
    }
}

@Service
public class EmployeeService {
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == authentication.principal.department)")
    public Employee updateEmployee(Employee employee) { /* ... */ }
}
```

---

## Section: E04 - Time & Attendance (Clock In/Out)

**Description:**
Handles clock-in/out events, geofencing, device capture, shift association, and corrections workflow.

**Design Specification:**
- Entity: AttendanceEvent (id, employee, type, timestamp, location, deviceId, approved, correctionRequested)
- Repository: AttendanceEventRepository with custom queries for daily totals
- Service: AttendanceService with validation, shift association, correction workflow
- Controller: AttendanceController with endpoints for clock-in/out, corrections
- Integration: CSV export for reports

**Sample Implementation:**
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private EventType type; // CLOCK_IN, CLOCK_OUT
    private LocalDateTime timestamp;
    private String location;
    private String deviceId;
    private boolean approved;
    private boolean correctionRequested;
}

public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    @Query("SELECT SUM(...) FROM AttendanceEvent WHERE ...") // daily totals
    List<AttendanceSummary> findDailyTotalsByEmployee(Long employeeId, LocalDate date);
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockInDTO dto) { /* ... */ }
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockOutDTO dto) { /* ... */ }
    @PostMapping("/corrections")
    public ResponseEntity<?> requestCorrection(@RequestBody CorrectionDTO dto) { /* ... */ }
}
```

---

## Section: E05 - Shift & Schedule Management

**Description:**
Manages shift templates, rotations, overtime, blackout dates, and employee assignments.

**Design Specification:**
- Entities: ShiftTemplate, ShiftAssignment, BlackoutDate
- Repositories: ShiftTemplateRepository, ShiftAssignmentRepository
- Service: ShiftService with conflict detection, bulk assignment, audit logging
- Controller: ShiftController with CRUD endpoints
- Integration: Audit entries for changes

**Sample Implementation:**
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    // ...
}

@Entity
public class ShiftAssignment {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private ShiftTemplate shiftTemplate;
    private LocalDate date;
    // ...
}

@Service
public class ShiftService {
    @Transactional
    public void assignShift(Long employeeId, Long shiftTemplateId, LocalDate date) {
        // check for conflicts, assign, log audit
    }
}
```

---

## Section: E06 - Leave & Absence Management

**Description:**
Handles PTO, sick, unpaid leave requests, approvals, accruals, and integration with scheduling/payroll.

**Design Specification:**
- Entities: LeaveRequest, LeaveBalance, LeavePolicy
- Repositories: LeaveRequestRepository, LeaveBalanceRepository
- Service: LeaveService with accrual calculation, approval workflow
- Controller: LeaveController with endpoints for request/approve/deny
- Integration: Exclude from scheduling/payroll

**Sample Implementation:**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING) private LeaveStatus status;
    // ...
}

@Service
public class LeaveService {
    @Transactional
    public void requestLeave(LeaveRequestDTO dto) { /* validate, update balances, create request */ }
    @Transactional
    public void approveLeave(Long requestId) { /* update status, notify */ }
}
```

---

## Section: E07 - Training & Certification Tracking

**Description:**
Tracks employee certifications, expirations, renewals, and blocks assignments if expired.

**Design Specification:**
- Entities: Certification, EmployeeCertification
- Repositories: CertificationRepository, EmployeeCertificationRepository
- Service: CertificationService with expiry alerts, assignment checks
- Controller: CertificationController with CRUD endpoints
- Integration: Upload proof documents, scheduling checks

**Sample Implementation:**
```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    private String name;
    private int validityMonths;
}

@Entity
public class EmployeeCertification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private Certification certification;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
}

@Service
public class CertificationService {
    public void checkCertificationValidity(Long employeeId, String certName) { /* ... */ }
}
```

---

## Section: E08 - Safety Incidents & OSHA Reporting

**Description:**
Records safety incidents, manages investigation workflow, and generates OSHA reports.

**Design Specification:**
- Entities: SafetyIncident, Investigation
- Repositories: SafetyIncidentRepository
- Service: SafetyService with workflow management
- Controller: SafetyController with endpoints for reporting, status updates
- Integration: OSHA 300/300A export, metrics dashboard

**Sample Implementation:**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private String description;
    private String location;
    private LocalDateTime occurredAt;
    @ManyToMany private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING) private IncidentStatus status;
}

@Service
public class SafetyService {
    @Transactional
    public void reportIncident(SafetyIncidentDTO dto) { /* ... */ }
    @Transactional
    public void updateStatus(Long incidentId, IncidentStatus status) { /* ... */ }
}
```

---

## Section: E09 - Equipment & Asset Assignment

**Description:**
Manages assignment and tracking of equipment/assets, ensuring only certified employees can use certain assets.

**Design Specification:**
- Entities: Asset, AssetAssignment, AssetCondition
- Repositories: AssetRepository, AssetAssignmentRepository
- Service: AssetService with check-in/out logic, certification checks
- Controller: AssetController with endpoints for assignment, return
- Integration: History log, overdue reports

**Sample Implementation:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String type;
    private String serialNumber;
    private AssetCondition condition;
}

@Entity
public class AssetAssignment {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Asset asset;
    @ManyToOne private Employee employee;
    private LocalDateTime assignedAt;
    private LocalDateTime returnedAt;
}

@Service
public class AssetService {
    public void assignAsset(Long assetId, Long employeeId) { /* check certs, assign */ }
    public void returnAsset(Long assignmentId) { /* ... */ }
}
```

---

## Section: E10 - Performance Reviews & Goals

**Description:**
Supports creation and management of performance reviews, goals, and feedback workflows.

**Design Specification:**
- Entities: PerformanceReview, ReviewCycle, Goal
- Repositories: PerformanceReviewRepository, ReviewCycleRepository
- Service: ReviewService with workflow, PDF export
- Controller: ReviewController with endpoints for review cycles, submission
- Integration: Role-based visibility, immutable history

**Sample Implementation:**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private ReviewCycle cycle;
    private String comments;
    private int rating;
    private boolean acknowledgedByEmployee;
    private boolean acknowledgedBySupervisor;
    // ...
}

@Service
public class ReviewService {
    public void submitReview(PerformanceReviewDTO dto) { /* ... */ }
}
```

---

## Section: E11 - Payroll Export Integration

**Description:**
Generates payroll-ready files from attendance/leave data, maps to provider formats, and delivers securely.

**Design Specification:**
- Service: PayrollExportService with mapping logic, SFTP/API delivery, retry/backoff
- Controller: PayrollController with export endpoints
- Integration: Audit log for exports

**Sample Implementation:**
```java
@Service
public class PayrollExportService {
    public void exportPayroll(LocalDate periodStart, LocalDate periodEnd) { 
        /* map, generate CSV/XML, deliver via SFTP/API */ 
    }
}
```

---

## Section: E12 - Notifications & Announcements

**Description:**
Sends in-app, email, and SMS notifications for key events, with opt-in/out and quiet hours.

**Design Specification:**
- Entities: Notification, Announcement, UserPreference
- Service: NotificationService with channel logic, rate limiting
- Controller: NotificationController with endpoints for announcements, preferences
- Integration: Email/SMS providers, dashboard

**Sample Implementation:**
```java
@Entity
public class Notification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee recipient;
    private String message;
    private NotificationChannel channel;
    private boolean delivered;
    private LocalDateTime sentAt;
}

@Service
public class NotificationService {
    public void sendNotification(NotificationDTO dto) { /* ... */ }
}
```

---

## Section: E13 - Integration Layer (HRIS/WMS APIs)

**Description:**
Exposes REST APIs and connectors for HRIS, WMS, and SSO; supports webhooks and JWT/OAuth2 security.

**Design Specification:**
- Controller: IntegrationController with endpoints for HRIS/WMS sync, webhooks
- Security: JWT/OAuth2 for external APIs
- Service: IntegrationService with idempotency, sync jobs

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/integrations")
public class IntegrationController {
    @PostMapping("/hris/sync")
    public ResponseEntity<?> syncHris(@RequestBody HrisSyncDTO dto) { /* ... */ }
    @PostMapping("/wms/sync")
    public ResponseEntity<?> syncWms(@RequestBody WmsSyncDTO dto) { /* ... */ }
}
```

---

## Section: E14 - Audit Trail & Compliance

**Description:**
Centralized, immutable audit logging for sensitive changes, with export and coverage validation.

**Design Specification:**
- Entity: AuditLog (id, actor, timestamp, entity, before, after, action)
- Service: AuditService with log creation, export
- Integration: Aspect for automatic audit on CRUD

**Sample Implementation:**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    @Lob private String before;
    @Lob private String after;
    private String action;
}

@Aspect
@Component
public class AuditAspect {
    @AfterReturning(pointcut = "execution(* com.company.warehouse..*.save*(..))" returning = "result")
    public void logSave(JoinPoint jp, Object result) { /* ... */ }
}
```

---

## Section: E15 - Reporting & Analytics

**Description:**
Provides operational reports, exports, and dashboards with role-based access.

**Design Specification:**
- Service: ReportingService with report generation, CSV/PDF export
- Controller: ReportingController with endpoints for reports, metrics
- Integration: BI endpoints, access control

**Sample Implementation:**
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> attendanceReport(@RequestParam ...) { /* ... */ }
}
```

---

## Section: E16 - Mobile Access (PWA)

**Description:**
Delivers responsive, offline-capable PWA for core workflows.

**Design Specification:**
- Frontend: PWA manifest, service worker, responsive UI
- Backend: REST APIs for mobile flows, offline queue support
- Integration: Lighthouse PWA compliance

**Sample Implementation:**
```yaml
# manifest.json (frontend)
{
  "name": "Warehouse Employee Portal",
  "short_name": "Warehouse",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

---

## Section: E17 - Onboarding & Offboarding Workflow

**Description:**
Automates provisioning/deprovisioning, initial schedules, and asset/training assignment.

**Design Specification:**
- Service: OnboardingService with HRIS triggers, task generation
- Controller: OnboardingController with endpoints for workflow status
- Integration: Asset, training, schedule modules

**Sample Implementation:**
```java
@Service
public class OnboardingService {
    public void onboardEmployee(Long employeeId) { 
        /* create tasks, assign schedule/assets */ 
    }
    public void offboardEmployee(Long employeeId) { 
        /* revoke access, collect assets */ 
    }
}
```

---

## Section: E18 - Localization & Internationalization

**Description:**
Supports multiple languages/locales for UI, notifications, and templates.

**Design Specification:**
- application.yml: supported locales
- MessageSource bean for i18n
- DTOs and templates with locale support

**Sample Implementation:**
```java
@Bean
public MessageSource messageSource() {
    ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
    ms.setBasename("classpath:messages");
    ms.setDefaultEncoding("UTF-8");
    return ms;
}
```

---

## Section: E19 - Advanced Scheduling & Optimization

**Description:**
Provides advanced scheduling algorithms, shift swaps, and optimization for coverage.

**Design Specification:**
- Service: SchedulingService with optimization logic
- Controller: SchedulingController with endpoints for swaps, suggestions
- Integration: Conflict resolution, audit

**Sample Implementation:**
```java
@Service
public class SchedulingService {
    public List<ShiftAssignment> optimizeSchedule(LocalDate weekStart) { /* ... */ }
    public void requestShiftSwap(Long fromEmployeeId, Long toEmployeeId, LocalDate date) { /* ... */ }
}
```

---

## Section: E20 - Self-Service Portal

**Description:**
Enables employees to manage their data, requests, and view schedules via a secure portal.

**Design Specification:**
- Controller: SelfServiceController with endpoints for profile, requests, schedules
- Security: Employee-only access, JWT authentication
- Integration: All relevant modules

**Sample Implementation:**
```java
@RestController
@RequestMapping("/self-service")
@PreAuthorize("hasRole('WORKER')")
public class SelfServiceController {
    @GetMapping("/profile")
    public EmployeeDTO getProfile(Authentication auth) { /* ... */ }
    @PostMapping("/leave-request")
    public ResponseEntity<?> requestLeave(@RequestBody LeaveRequestDTO dto) { /* ... */ }
}
```

---

## Conclusion

This document provides a production-ready, detailed low-level technical design for all 20 epics of the Warehouse Employee Management System, following Spring Boot 3.x best practices, with clear package structure, domain models, repository/service/controller layers, configuration, security, integration points, and sample code for each epic. The design ensures scalability, maintainability, security, and compliance with industry standards.