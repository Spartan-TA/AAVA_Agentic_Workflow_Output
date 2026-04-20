# Warehouse Employee Management System - Low-Level Technical Design Document

## Table of Contents
1. [E01: Project Scaffolding & Domain Setup](#e01)
2. [E02: Employee Master Data (CRUD)](#e02)
3. [E03: Role-Based Access Control (RBAC)](#e03)
4. [E04: Time & Attendance (Clock In/Out)](#e04)
5. [E05: Shift & Schedule Management](#e05)
6. [E06: Leave & Absence Management](#e06)
7. [E07: Training & Certification Tracking](#e07)
8. [E08: Safety Incidents & OSHA Reporting](#e08)
9. [E09: Equipment & Asset Assignment](#e09)
10. [E10: Performance Reviews & Goals](#e10)
11. [E11: Payroll Export Integration](#e11)
12. [E12: Notifications & Announcements](#e12)
13. [E13: Integration Layer (HRIS/WMS APIs)](#e13)
14. [E14: Audit Trail & Compliance](#e14)
15. [E15: Reporting & Analytics](#e15)
16. [E16: Mobile Access (PWA)](#e16)
17. [E17: Onboarding & Offboarding Workflow](#e17)
18. [E18: Localization & Multi-Tenant](#e18)
19. [E19: Advanced Scheduling (AI/Optimization)](#e19)
20. [E20: Continuous Deployment & Observability](#e20)

---

<a name="e01"></a>
## Section: E01 - Project Scaffolding & Domain Setup

**Description:**  
Initialize Spring Boot (Maven) project; configure base packages; set up core modules (employee, scheduling, attendance, safety); add Flyway/Liquibase for DB migrations; enable Actuator.

**Design Specification:**  
- **Architecture Overview:**  
  - Layered architecture: Controller â Service â Repository â Domain  
  - Modularized by business domain (employee, scheduling, attendance, safety)
  - Maven multi-module structure (if scale requires)
- **Package Structure:**  
  - `com.companyname.wms` (root)
    - `employee`
    - `scheduling`
    - `attendance`
    - `safety`
    - `common`
    - `config`
    - `integration`
    - `audit`
    - `reporting`
- **Entity Models:**  
  - Placeholder entities for each module (e.g., Employee, Shift, Attendance, SafetyIncident)
- **Service Layer:**  
  - Interface and implementation for each domain service
- **Repository Layer:**  
  - Spring Data JPA repositories for each entity
- **Controller Layer:**  
  - REST controllers for each module
- **Security Configuration:**  
  - Basic Spring Security setup (to be extended in E03)
- **Integration Points:**  
  - Flyway/Liquibase for DB migrations
  - Spring Boot Actuator for health checks

**Sample Implementation:**  
```java
// pom.xml (excerpt)
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

// Application.java
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}

// Directory structure
src/main/java/com/companyname/wms/
  âââ employee/
  âââ scheduling/
  âââ attendance/
  âââ safety/
  âââ common/
  âââ config/
  âââ integration/
  âââ audit/
  âââ reporting/

// Flyway migration example (V1__init.sql)
CREATE TABLE employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    badge_id VARCHAR(50) UNIQUE NOT NULL,
    role VARCHAR(50),
    department VARCHAR(100),
    shift_group VARCHAR(50),
    hire_date DATE,
    status VARCHAR(20)
);

// application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wms
    username: wms_user
    password: secret
  flyway:
    enabled: true
  profiles:
    active: dev

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

---

<a name="e02"></a>
## Section: E02 - Employee Master Data (CRUD)

**Description:**  
Create Employee domain with CRUD APIs and web DTOs: name, badgeId, role, department, shiftGroup, hireDate, status. Single source of truth for warehouse employee records.

**Design Specification:**  
- **Architecture Overview:**  
  - RESTful CRUD endpoints for Employee entity
  - DTOs for API input/output
  - Service layer for business logic
  - Repository for persistence
- **Package Structure:**  
  - `com.companyname.wms.employee`
    - `controller`
    - `service`
    - `repository`
    - `domain`
    - `dto`
- **Entity Models:**  
  - `Employee` (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)
- **Service Layer:**  
  - `EmployeeService` interface and `EmployeeServiceImpl`
  - Methods: create, get, update, delete (soft), list (with pagination/filter)
- **Repository Layer:**  
  - `EmployeeRepository` extends `JpaRepository<Employee, Long>`
  - Custom query for filtering and soft-delete
- **Controller Layer:**  
  - `EmployeeController` with endpoints:
    - `POST /employees`
    - `GET /employees`
    - `GET /employees/{id}`
    - `PUT /employees/{id}`
    - `PATCH /employees/{id}`
    - `DELETE /employees/{id}`
- **Security Configuration:**  
  - Secured endpoints (to be extended in E03)
- **Integration Points:**  
  - OpenAPI/Swagger for API docs

**Sample Implementation:**  
```java
// Employee.java
@Entity
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
    private boolean deleted = false;
    // getters/setters
}

// EmployeeRepository.java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}

// EmployeeService.java
public interface EmployeeService {
    EmployeeDto create(EmployeeDto dto);
    EmployeeDto get(Long id);
    Page<EmployeeDto> list(Pageable pageable, EmployeeFilter filter);
    EmployeeDto update(Long id, EmployeeDto dto);
    void delete(Long id); // soft delete
}

// EmployeeController.java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@RequestBody @Valid EmployeeDto dto) { ... }
    @GetMapping
    public Page<EmployeeDto> list(Pageable pageable, EmployeeFilter filter) { ... }
    @GetMapping("/{id}")
    public EmployeeDto get(@PathVariable Long id) { ... }
    @PutMapping("/{id}")
    public EmployeeDto update(@PathVariable Long id, @RequestBody @Valid EmployeeDto dto) { ... }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { ... }
}

// OpenAPI example (openapi.yaml excerpt)
paths:
  /employees:
    get:
      summary: List employees
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/EmployeeDto'
```

---

<a name="e03"></a>
## Section: E03 - Role-Based Access Control (RBAC)

**Description:**  
Add Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER); method/endpoint security and row-level constraints; API key/OAuth2 toggle.

**Design Specification:**  
- **Architecture Overview:**  
  - Spring Security for authentication/authorization
  - Role-based access at method and endpoint level
  - Row-level security via service layer checks
  - Configurable API key or OAuth2
- **Package Structure:**  
  - `com.companyname.wms.config.security`
- **Entity Models:**  
  - `User` (username, password, roles)
  - `Role` (ADMIN, HR, SUPERVISOR, WORKER)
- **Service Layer:**  
  - UserDetailsService for authentication
  - Security checks in service methods
- **Repository Layer:**  
  - `UserRepository`, `RoleRepository`
- **Controller Layer:**  
  - Secured endpoints with `@PreAuthorize`
- **Security Configuration:**  
  - `SecurityConfig` with role mappings
  - API key/OAuth2 toggle via `application.yml`
- **Integration Points:**  
  - OAuth2 provider or API key validation

**Sample Implementation:**  
```java
// SecurityConfig.java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${security.mode}")
    private String securityMode; // "apikey" or "oauth2"

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if ("apikey".equals(securityMode)) {
            http.addFilterBefore(new ApiKeyAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        } else {
            http.oauth2Login();
        }
        http
            .authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .antMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated();
    }
}

// EmployeeServiceImpl.java (row-level security)
@Override
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @securityService.isSupervisorOf(#id))")
public EmployeeDto get(Long id) { ... }

// application.yml
security:
  mode: apikey # or oauth2

// ApiKeyAuthFilter.java (simplified)
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    @Value("${security.apikey}")
    private String apiKey;
    protected void doFilterInternal(...) {
        // Validate API key from header
    }
}
```

---

<a name="e04"></a>
## Section: E04 - Time & Attendance (Clock In/Out)

**Description:**  
Endpoints for clock-in/out events with geofence (optional) and device capture; calculate hours worked per shift; handle missed punches and corrections workflow.

**Design Specification:**  
- **Architecture Overview:**  
  - REST endpoints for clock-in/out
  - Attendance event entity
  - Geofence/device validation
  - Shift association and hours calculation
  - Correction workflow
- **Package Structure:**  
  - `com.companyname.wms.attendance`
    - `controller`
    - `service`
    - `repository`
    - `domain`
    - `dto`
- **Entity Models:**  
  - `AttendanceEvent` (id, employee, type, timestamp, deviceId, location, status)
- **Service Layer:**  
  - `AttendanceService` for event handling, validation, calculation
- **Repository Layer:**  
  - `AttendanceEventRepository`
- **Controller Layer:**  
  - `POST /attendance/clock-in`
  - `POST /attendance/clock-out`
  - `POST /attendance/correction`
- **Security Configuration:**  
  - Only authenticated employees can clock in/out
- **Integration Points:**  
  - Geolocation API (optional)
  - Device registry

**Sample Implementation:**  
```java
// AttendanceEvent.java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type; // CLOCK_IN, CLOCK_OUT, CORRECTION
    private LocalDateTime timestamp;
    private String deviceId;
    private String location; // lat,long
    private String status; // APPROVED, PENDING, REJECTED
}

// AttendanceController.java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockInDto dto) { ... }
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockOutDto dto) { ... }
    @PostMapping("/correction")
    public ResponseEntity<?> requestCorrection(@RequestBody CorrectionDto dto) { ... }
}

// AttendanceService.java
public interface AttendanceService {
    void clockIn(Long employeeId, ClockInDto dto);
    void clockOut(Long employeeId, ClockOutDto dto);
    void requestCorrection(Long employeeId, CorrectionDto dto);
    List<AttendanceSummary> getDailyTotals(Long employeeId, LocalDate date);
}

// Geofence validation (pseudo-code)
if (geofenceEnabled) {
    if (!geofenceService.isWithinAllowedArea(dto.getLocation())) {
        throw new ValidationException("Outside allowed area");
    }
}
```

---

<a name="e05"></a>
## Section: E05 - Shift & Schedule Management

**Description:**  
Create recurring shift templates, rotations, overtime rules, and assignment to employees; handle blackout dates and warehouse operation calendars.

**Design Specification:**  
- **Architecture Overview:**  
  - Shift templates and schedule entities
  - Assignment logic and conflict detection
  - Overtime and blackout date handling
- **Package Structure:**  
  - `com.companyname.wms.scheduling`
    - `controller`
    - `service`
    - `repository`
    - `domain`
    - `dto`
- **Entity Models:**  
  - `ShiftTemplate` (id, name, startTime, endTime, recurrence, overtimeRules)
  - `EmployeeSchedule` (id, employee, shiftTemplate, date, status)
  - `BlackoutDate` (id, date, reason)
- **Service Layer:**  
  - `SchedulingService` for assignment, conflict detection, bulk operations
- **Repository Layer:**  
  - `ShiftTemplateRepository`, `EmployeeScheduleRepository`, `BlackoutDateRepository`
- **Controller Layer:**  
  - CRUD for shift templates, schedules, blackout dates
  - Bulk assignment endpoints
- **Security Configuration:**  
  - Supervisors/HR can assign and manage schedules
- **Integration Points:**  
  - Calendar API (optional)

**Sample Implementation:**  
```java
// ShiftTemplate.java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrence; // DAILY, WEEKLY, etc.
    private String overtimeRules;
}

// EmployeeSchedule.java
@Entity
public class EmployeeSchedule {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ShiftTemplate shiftTemplate;
    private LocalDate date;
    private String status; // ASSIGNED, COMPLETED, MISSED
}

// SchedulingController.java
@RestController
@RequestMapping("/scheduling")
public class SchedulingController {
    @PostMapping("/shifts")
    public ShiftTemplateDto createShift(@RequestBody ShiftTemplateDto dto) { ... }
    @PostMapping("/assign")
    public void assignShift(@RequestBody AssignShiftDto dto) { ... }
    @GetMapping("/conflicts")
    public List<ConflictDto> getConflicts(@RequestParam ...) { ... }
}
```

---

<a name="e06"></a>
## Section: E06 - Leave & Absence Management

**Description:**  
Request/approve PTO, sick, unpaid leave; accrual balances and policies; integration hooks to exclude from scheduling and payroll.

**Design Specification:**  
- **Architecture Overview:**  
  - Leave request and approval workflow
  - Accrual policy engine
  - Integration with scheduling and payroll
- **Package Structure:**  
  - `com.companyname.wms.leave`
    - `controller`
    - `service`
    - `repository`
    - `domain`
    - `dto`
- **Entity Models:**  
  - `LeaveRequest` (id, employee, type, startDate, endDate, status, approver, createdAt)
  - `LeaveBalance` (id, employee, type, balance)
- **Service Layer:**  
  - `LeaveService` for request, approval, accrual calculation
- **Repository Layer:**  
  - `LeaveRequestRepository`, `LeaveBalanceRepository`
- **Controller Layer:**  
  - Endpoints for request, approve/deny, view balances
- **Security Configuration:**  
  - Employees request, supervisors approve
- **Integration Points:**  
  - Scheduling (exclude from shifts)
  - Payroll (exclude from hours)

**Sample Implementation:**  
```java
// LeaveRequest.java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type; // PTO, SICK, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // PENDING, APPROVED, DENIED
    @ManyToOne
    private Employee approver;
    private LocalDateTime createdAt;
}

// LeaveController.java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/request")
    public LeaveRequestDto requestLeave(@RequestBody LeaveRequestDto dto) { ... }
    @PostMapping("/approve/{id}")
    public void approveLeave(@PathVariable Long id) { ... }
    @GetMapping("/balance")
    public LeaveBalanceDto getBalance(@RequestParam Long employeeId) { ... }
}
```

---

<a name="e07"></a>
## Section: E07 - Training & Certification Tracking

**Description:**  
Track required certifications (e.g., forklift), expirations, renewals; block assignment to tasks requiring expired certs; upload proof documents.

**Design Specification:**  
- **Architecture Overview:**  
  - Certification tracking and document upload
  - Assignment checks for valid certifications
- **Package Structure:**  
  - `com.companyname.wms.certification`
    - `controller`
    - `service`
    - `repository`
    - `domain`
    - `dto`
- **Entity Models:**  
  - `Certification` (id, employee, type, issueDate, expiryDate, documentUrl, status)
- **Service Layer:**  
  - `CertificationService` for CRUD, expiry checks, alerts
- **Repository Layer:**  
  - `CertificationRepository`
- **Controller Layer:**  
  - Endpoints for CRUD, upload, status checks
- **Security Configuration:**  
  - Supervisors/HR manage, employees view
- **Integration Points:**  
  - Scheduling (block unqualified assignments)
  - Notification (expiry alerts)

**Sample Implementation:**  
```java
// Certification.java
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
    private String status; // VALID, EXPIRED, PENDING
}

// CertificationController.java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public CertificationDto create(@RequestBody CertificationDto dto) { ... }
    @PostMapping("/{id}/upload")
    public void uploadDocument(@PathVariable Long id, @RequestParam MultipartFile file) { ... }
    @GetMapping("/status")
    public List<CertificationStatusDto> getStatus(@RequestParam Long employeeId) { ... }
}
```

---

<a name="e08"></a>
## Section: E08 - Safety Incidents & OSHA Reporting

**Description:**  
Record incidents/near-misses; severity, location, description, involved employees; workflow for investigation and corrective actions; generate OSHA summary.

**Design Specification:**  
- **Architecture Overview:**  
  - Incident reporting and workflow
  - OSHA summary generation
- **Package Structure:**  
  - `com.companyname.wms.safety`
    - `controller`
    - `service`
    - `repository`
    - `domain`
    - `dto`
- **Entity Models:**  
  - `SafetyIncident` (id, date, severity, location, description, status, involvedEmployees, correctiveActions)
- **Service Layer:**  
  - `SafetyService` for incident workflow, reporting
- **Repository Layer:**  
  - `SafetyIncidentRepository`
- **Controller Layer:**  
  - Endpoints for incident CRUD, workflow, OSHA export
- **Security Configuration:**  
  - Supervisors/HR manage, employees report
- **Integration Points:**  
  - Reporting (OSHA 300/300A)

**Sample Implementation:**  
```java
// SafetyIncident.java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private LocalDate date;
    private String severity;
    private String location;
    private String description;
    private String status; // OPEN, INVESTIGATING, RESOLVED
    @ManyToMany
    private List<Employee> involvedEmployees;
    private String correctiveActions;
}

// SafetyController.java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyController {
    @PostMapping
    public SafetyIncidentDto report(@RequestBody SafetyIncidentDto dto) { ... }
    @PostMapping("/{id}/workflow")
    public void updateStatus(@PathVariable Long id, @RequestBody StatusUpdateDto dto) { ... }
    @GetMapping("/oshasummary")
    public OSHAReportDto getOshaSummary(@RequestParam int year) { ... }
}
```

---

<a name="e09"></a>
## Section: E09 - Equipment & Asset Assignment

**Description:**  
Assign scanners, forklifts, PPE to employees; track checkout/return; prevent use if certification missing; maintain asset condition state.

**Design Specification:**  
- **Architecture Overview:**  
  - Asset registry and assignment tracking
  - Certification validation on assignment
- **Package Structure:**  
  - `com.companyname.wms.asset`
    - `controller`
    - `service`
    - `repository`
    - `domain`
    - `dto`
- **Entity Models:**  
  - `Asset` (id, type, serialNumber, condition, assignedTo, checkedOutAt, returnedAt)
  - `AssetAssignment` (id, asset, employee, checkoutDate, returnDate, status)
- **Service Layer:**  
  - `AssetService` for CRUD, assignment, validation
- **Repository Layer:**  
  - `AssetRepository`, `AssetAssignmentRepository`
- **Controller Layer:**  
  - Endpoints for asset CRUD, check-in/out, history
- **Security Configuration:**  
  - Supervisors/HR manage, employees check out/in
- **Integration Points:**  
  - Certification (block if invalid)

**Sample Implementation:**  
```java
// Asset.java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private String condition;
    @ManyToOne
    private Employee assignedTo;
    private LocalDateTime checkedOutAt;
    private LocalDateTime returnedAt;
}

// AssetController.java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping
    public AssetDto create(@RequestBody AssetDto dto) { ... }
    @PostMapping("/{id}/checkout")
    public void checkout(@PathVariable Long id, @RequestParam Long employeeId) { ... }
    @PostMapping("/{id}/return")
    public void returnAsset(@PathVariable Long id) { ... }
    @GetMapping("/history")
    public List<AssetAssignmentDto> getHistory(@RequestParam Long assetId) { ... }
}
```

---

<a name="e10"></a>
## Section: E10 - Performance Reviews & Goals

**Description:**  
Create quarterly/annual review templates; track goals, competencies, ratings, comments; supervisor/employee acknowledgements.

**Design Specification:**  
- **Architecture Overview:**  
  - Review cycles and templates
  - Goal tracking and acknowledgement workflow
- **Package Structure:**  
  - `com.companyname.wms.performance`
    - `controller`
    - `service`
    - `repository`
    - `domain`
    - `dto`
- **Entity Models:**  
  - `PerformanceReview` (id, employee, cycle, goals, competencies, ratings, comments, supervisorAck, employeeAck, status)
- **Service Layer:**  
  - `PerformanceService` for review management
- **Repository Layer:**  
  - `PerformanceReviewRepository`
- **Controller Layer:**  
  - Endpoints for review CRUD, submit, acknowledge, export
- **Security Configuration:**  
  - Supervisors/HR manage, employees view/acknowledge
- **Integration Points:**  
  - PDF export

**Sample Implementation:**  
```java
// PerformanceReview.java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String cycle; // Q1-2024, 2024, etc.
    private String goals;
    private String competencies;
    private String ratings;
    private String comments;
    private boolean supervisorAck;
    private boolean employeeAck;
    private String status; // DRAFT, SUBMITTED, SIGNED_OFF
}

// PerformanceController.java
@RestController
@RequestMapping("/performance/reviews")
public class PerformanceController {
    @PostMapping
    public PerformanceReviewDto create(@RequestBody PerformanceReviewDto dto) { ... }
    @PostMapping("/{id}/acknowledge")
    public void acknowledge(@PathVariable Long id, @RequestParam String role) { ... }
    @GetMapping("/{id}/export")
    public ResponseEntity<Resource> exportPdf(@PathVariable Long id) { ... }
}
```

---

<a name="e11"></a>
## Section: E11 - Payroll Export Integration

**Description:**  
Generate payroll-ready files from approved attendance and leave; mapping to external payroll provider formats; secure delivery (SFTP/API).

**Design Specification:**  
- **Architecture Overview:**  
  - Payroll export job
  - Mapping to provider schemas
  - Secure file delivery
- **Package Structure:**  
  - `com.companyname.wms.payroll`
    - `service`
    - `integration`
    - `dto`
- **Entity Models:**  
  - `PayrollExport` (id, period, fileUrl, status, createdAt)
- **Service Layer:**  
  - `PayrollExportService` for generation, delivery, retry
- **Repository Layer:**  
  - `PayrollExportRepository`
- **Controller Layer:**  
  - Endpoint to trigger/export payroll files
- **Security Configuration:**  
  - Only ADMIN/HR
- **Integration Points:**  
  - SFTP/API to payroll provider

**Sample Implementation:**  
```java
// PayrollExportService.java
@Service
public class PayrollExportService {
    public PayrollExportDto generatePayrollFile(LocalDate periodStart, LocalDate periodEnd) { ... }
    public void deliverToProvider(PayrollExportDto export) { ... }
}

// PayrollExportController.java
@RestController
@RequestMapping("/payroll/export")
public class PayrollExportController {
    @PostMapping
    public PayrollExportDto export(@RequestBody PayrollExportRequestDto dto) { ... }
}
```

---

<a name="e12"></a>
## Section: E12 - Notifications & Announcements

**Description:**  
In-app and email/SMS notifications for shift changes, expiring certs, approvals, announcements; quiet hours configuration.

**Design Specification:**  
- **Architecture Overview:**  
  - Notification service with multiple channels
  - Announcement management
  - Quiet hours and opt-in/out
- **Package Structure:**  
  - `com.companyname.wms.notification`
    - `service`
    - `controller`
    - `domain`
    - `repository`
    - `dto`
- **Entity Models:**  
  - `Notification` (id, employee, type, channel, content, status, sentAt)
  - `Announcement` (id, title, content, startDate, endDate, audience)
- **Service Layer:**  
  - `NotificationService` for delivery, status tracking
- **Repository Layer:**  
  - `NotificationRepository`, `AnnouncementRepository`
- **Controller Layer:**  
  - Endpoints for announcements, notification preferences
- **Security Configuration:**  
  - Role-based delivery
- **Integration Points:**  
  - Email/SMS gateway

**Sample Implementation:**  
```java
// Notification.java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type; // SHIFT_CHANGE, CERT_EXPIRY, APPROVAL, ANNOUNCEMENT
    private String channel; // IN_APP, EMAIL, SMS
    private String content;
    private String status; // SENT, FAILED, PENDING
    private LocalDateTime sentAt;
}

// NotificationService.java
@Service
public class NotificationService {
    public void sendNotification(NotificationDto dto) { ... }
    public void sendAnnouncement(AnnouncementDto dto) { ... }
}

// NotificationController.java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping("/announcement")
    public void createAnnouncement(@RequestBody AnnouncementDto dto) { ... }
    @PostMapping("/preferences")
    public void updatePreferences(@RequestBody NotificationPreferenceDto dto) { ... }
}
```

---

<a name="e13"></a>
## Section: E13 - Integration Layer (HRIS/WMS APIs)

**Description:**  
Expose REST APIs and connectors for HRIS (new hires/terms), WMS (location/department), and IDP for SSO; webhooks for events.

**Design Specification:**  
- **Architecture Overview:**  
  - REST APIs for HRIS/WMS sync
  - SSO integration (SAML/OAuth2)
  - Webhook event publishing
- **Package Structure:**  
  - `com.companyname.wms.integration`
    - `hris`
    - `wms`
    - `idp`
    - `webhook`
- **Entity Models:**  
  - Integration DTOs for HRIS/WMS
- **Service Layer:**  
  - Sync jobs, webhook publisher
- **Repository Layer:**  
  - N/A (stateless integration)
- **Controller Layer:**  
  - Endpoints for sync, webhooks
- **Security Configuration:**  
  - JWT/OAuth2 for APIs
- **Integration Points:**  
  - HRIS, WMS, IDP

**Sample Implementation:**  
```java
// HRISSyncController.java
@RestController
@RequestMapping("/integration/hris")
public class HRISSyncController {
    @PostMapping("/sync")
    public void syncEmployees(@RequestBody List<EmployeeHrisDto> dtos) { ... }
}

// WebhookController.java
@RestController
@RequestMapping("/integration/webhook")
public class WebhookController {
    @PostMapping
    public void receiveEvent(@RequestBody WebhookEventDto dto) { ... }
}

// application.yml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: https://idp.example.com/.well-known/jwks.json
```

---

<a name="e14"></a>
## Section: E14 - Audit Trail & Compliance

**Description:**  
Centralized audit logging for sensitive changes (employee PII, schedules, approvals, payroll); tamper-evident storage.

**Design Specification:**  
- **Architecture Overview:**  
  - Audit log entity and service
  - Tamper-evident storage (hash chain)
- **Package Structure:**  
  - `com.companyname.wms.audit`
    - `service`
    - `repository`
    - `domain`
- **Entity Models:**  
  - `AuditLog` (id, entity, entityId, action, actor, timestamp, before, after, hash, prevHash)
- **Service Layer:**  
  - `AuditService` for logging, export, validation
- **Repository Layer:**  
  - `AuditLogRepository`
- **Controller Layer:**  
  - Export/filter endpoints
- **Security Configuration:**  
  - Restricted to ADMIN/HR
- **Integration Points:**  
  - All modules log sensitive changes

**Sample Implementation:**  
```java
// AuditLog.java
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
    private String hash;
    private String prevHash;
}

// AuditService.java
@Service
public class AuditService {
    public void logChange(String entity, Long entityId, String action, String actor, Object before, Object after) { ... }
}

// AuditController.java
@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping
    public List<AuditLogDto> getLogs(@RequestParam ...) { ... }
}
```

---

<a name="e15"></a>
## Section: E15 - Reporting & Analytics

**Description:**  
Operational reports: attendance, overtime, leave balances, certification status, safety KPIs; export CSV/PDF; role-based dashboards.

**Design Specification:**  
- **Architecture Overview:**  
  - Reporting service with data aggregation
  - Export and dashboard endpoints
- **Package Structure:**  
  - `com.companyname.wms.reporting`
    - `service`
    - `controller`
    - `dto`
- **Entity Models:**  
  - Report DTOs (AttendanceReport, OvertimeReport, etc.)
- **Service Layer:**  
  - `ReportingService` for aggregation, export
- **Repository Layer:**  
  - Use domain repositories for data
- **Controller Layer:**  
  - Endpoints for reports, export
- **Security Configuration:**  
  - Role-based access
- **Integration Points:**  
  - BI tools (metrics endpoints)

**Sample Implementation:**  
```java
// ReportingController.java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public AttendanceReportDto getAttendanceReport(@RequestParam ...) { ... }
    @GetMapping("/overtime")
    public OvertimeReportDto getOvertimeReport(@RequestParam ...) { ... }
    @GetMapping("/export")
    public ResponseEntity<Resource> exportReport(@RequestParam String type) { ... }
}
```

---

<a name="e16"></a>
## Section: E16 - Mobile Access (PWA)

**Description:**  
Responsive views for workers to clock-in/out, view schedules, request leave, see announcements; offline-friendly via PWA.

**Design Specification:**  
- **Architecture Overview:**  
  - REST APIs for mobile/PWA frontend
  - Offline queue for clock events
- **Package Structure:**  
  - Reuse existing controllers, add PWA manifest/static resources
- **Entity Models:**  
  - N/A (frontend concern)
- **Service Layer:**  
  - Offline event queueing handled in frontend
- **Repository Layer:**  
  - N/A
- **Controller Layer:**  
  - Existing endpoints
- **Security Configuration:**  
  - JWT/OAuth2 for mobile
- **Integration Points:**  
  - Lighthouse for PWA score

**Sample Implementation:**  
```yaml
# manifest.json (static resource)
{
  "name": "Warehouse Employee Management",
  "short_name": "WMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "icons": [
    {
      "src": "/icon-192.png",
      "sizes": "192x192",
      "type": "image/png"
    }
  ]
}
```

---

<a name="e17"></a>
## Section: E17 - Onboarding & Offboarding Workflow

**Description:**  
Automate provisioning of accounts, initial schedule, required training; deprovision access and assets on termination.

**Design Specification:**  
- **Architecture Overview:**  
  - Workflow engine for onboarding/offboarding
  - Task generation for training, asset assignment
- **Package Structure:**  
  - `com.companyname.wms.onboarding`
    - `service`
    - `controller`
    - `domain`
    - `dto`
- **Entity Models:**  
  - `OnboardingTask` (id, employee, type, status, dueDate, completedAt)
- **Service Layer:**  
  - `OnboardingService` for workflow automation
- **Repository Layer:**  
  - `OnboardingTaskRepository`
- **Controller Layer:**  
  - Endpoints for workflow status, task completion
- **Security Configuration:**  
  - HR/Supervisor manage
- **Integration Points:**  
  - HRIS, asset, training modules

**Sample Implementation:**  
```java
// OnboardingTask.java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type; // ACCOUNT, SCHEDULE, TRAINING, ASSET
    private String status; // PENDING, COMPLETED
    private LocalDate dueDate;
    private LocalDateTime completedAt;
}

// OnboardingController.java
@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @GetMapping("/tasks")
    public List<OnboardingTaskDto> getTasks(@RequestParam Long employeeId) { ... }
    @PostMapping("/tasks/{id}/complete")
    public void completeTask(@PathVariable Long id) { ... }
}
```

---

<a name="e18"></a>
## Section: E18 - Localization & Multi-Tenant

**Description:**  
Support multiple warehouses/regions with isolated data; localize UI and date/time formats; tenant-specific calendars and policies.

**Design Specification:**  
- **Architecture Overview:**  
  - Multi-tenant data isolation (schema or discriminator)
  - Locale-aware formatting
- **Package Structure:**  
  - `com.companyname.wms.tenant`
    - `config`
    - `service`
    - `domain`
- **Entity Models:**  
  - `Tenant` (id, name, region, config)
  - Add `tenantId` to all domain entities
- **Service Layer:**  
  - Tenant context resolver
- **Repository Layer:**  
  - Tenant-aware repositories (e.g., `@FilterDef`)
- **Controller Layer:**  
  - Tenant context in requests
- **Security Configuration:**  
  - Tenant isolation enforced
- **Integration Points:**  
  - Locale resolver

**Sample Implementation:**  
```java
// Tenant.java
@Entity
public class Tenant {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String region;
    private String config;
}

// Example: Add tenantId to Employee
@Column(nullable = false)
private Long tenantId;

// LocaleConfig.java
@Configuration
public class LocaleConfig {
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.US);
        return slr;
    }
}
```

---

<a name="e19"></a>
## Section: E19 - Advanced Scheduling (AI/Optimization)

**Description:**  
Predict staffing needs using historical data; optimize shift assignments for coverage and overtime; suggest shift swaps.

**Design Specification:**  
- **Architecture Overview:**  
  - AI/ML module for prediction and optimization
  - Integration with scheduling service
- **Package Structure:**  
  - `com.companyname.wms.optimization`
    - `service`
    - `controller`
    - `dto`
- **Entity Models:**  
  - N/A (uses attendance/schedule data)
- **Service Layer:**  
  - `OptimizationService` for prediction, optimization, swap suggestions
- **Repository Layer:**  
  - Use attendance/schedule repositories
- **Controller Layer:**  
  - Endpoints for suggestions, optimization runs
- **Security Configuration:**  
  - Supervisors/HR access
- **Integration Points:**  
  - ML model (external or embedded)

**Sample Implementation:**  
```java
// OptimizationService.java
@Service
public class OptimizationService {
    public StaffingPredictionDto predictStaffing(LocalDate period) { ... }
    public List<ShiftAssignmentDto> optimizeShifts(List<EmployeeSchedule> schedules) { ... }
    public List<ShiftSwapSuggestionDto> suggestSwaps(Long employeeId) { ... }
}

// OptimizationController.java
@RestController
@RequestMapping("/optimization")
public class OptimizationController {
    @GetMapping("/predict")
    public StaffingPredictionDto predict(@RequestParam LocalDate period) { ... }
    @PostMapping("/optimize")
    public List<ShiftAssignmentDto> optimize(@RequestBody List<EmployeeScheduleDto> schedules) { ... }
}
```

---

<a name="e20"></a>
## Section: E20 - Continuous Deployment & Observability

**Description:**  
CI/CD pipeline for automated builds, tests, deployments; monitoring (Prometheus/Grafana); alerting; rollback on failure.

**Design Specification:**  
- **Architecture Overview:**  
  - CI/CD pipeline (Jenkins/GitHub Actions)
  - Monitoring and alerting integration
- **Package Structure:**  
  - N/A (infrastructure)
- **Entity Models:**  
  - N/A
- **Service Layer:**  
  - N/A
- **Repository Layer:**  
  - N/A
- **Controller Layer:**  
  - N/A
- **Security Configuration:**  
  - N/A
- **Integration Points:**  
  - Prometheus, Grafana, Alertmanager

**Sample Implementation:**  
```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline
on:
  push:
    branches: [ main ]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Build with Maven
        run: mvn clean install
      - name: Run Tests
        run: mvn test
      - name: Docker Build & Push
        run: |
          docker build -t company/wms:${{ github.sha }} .
          docker push company/wms:${{ github.sha }}
      - name: Deploy to Kubernetes
        run: kubectl apply -f k8s/deployment.yaml

# application.yml (monitoring)
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

---

**End of Document**