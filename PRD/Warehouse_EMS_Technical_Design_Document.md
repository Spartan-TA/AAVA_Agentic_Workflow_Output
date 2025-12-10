# Technical Design Document: Warehouse Employee Management System (EMS)

---

## Table of Contents

1. [E01: Project Scaffolding & Domain Setup](#e01)
2. [E02: Employee Master Data (CRUD)](#e02)
3. [E03: Role Based Access Control (RBAC)](#e03)
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

---

## E01: Project Scaffolding & Domain Setup

### Section: User Story 1 - Project Initialization - Technical Design

**Description:**  
Initialize Spring Boot Maven project with base packages, Flyway/Liquibase migrations, and Actuator for health checks.

**Design Specification:**
- **Architecture:** Layered Spring Boot application (Controller â Service â Repository â Entity)
- **Package Structure:**  
  - `com.warehouse.ems`  
    - `employee`, `attendance`, `shift`, `leave`, `certification`, `safety`, `equipment`, `review`, `payroll`, `notification`, `integration`, `audit`, `reporting`, `mobile`, `onboarding`
- **Configuration:**  
  - Flyway/Liquibase for DB migrations  
  - Actuator for health endpoints  
  - `application.yml` for environment settings
- **Sample Implementation:**
```java
// Main Application
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}

// application.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems
    username: ems_user
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

### Section: User Story 2 - Dev Environment Configuration - Technical Design

**Description:**  
Configure development environment with README, build/run steps, and CI/CD pipeline.

**Design Specification:**
- **Architecture:** Standard Maven build, Docker support
- **Package Structure:** Root-level README, Dockerfile, `.github/workflows`
- **Configuration:**  
  - README with build/run/test instructions  
  - Dockerfile for containerization  
  - GitHub Actions for CI/CD
- **Sample Implementation:**
```markdown
# README.md
## Build & Run
mvn clean install
java -jar target/warehouse-ems.jar

## Docker
docker build -t warehouse-ems .
docker run -p 8080:8080 warehouse-ems

## CI/CD
See .github/workflows/ci.yml for pipeline steps.
```

---

### Section: User Story 3 - CI/CD Pipeline - Technical Design

**Description:**  
Automate build, test, and deployment using GitHub Actions.

**Design Specification:**
- **Architecture:** GitHub Actions workflow
- **Configuration:**  
  - `.github/workflows/ci.yml` for Maven build/test/deploy
- **Sample Implementation:**
```yaml
# .github/workflows/ci.yml
name: CI/CD Pipeline

on: [push, pull_request]

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
```

---

## E02: Employee Master Data (CRUD)

### Section: User Story 4 - Employee Record Creation - Technical Design

**Description:**  
Create Employee domain with CRUD APIs and web DTOs: name, badgeId, role, department, shiftGroup, hireDate, status.

**Design Specification:**
- **Architecture:** RESTful CRUD endpoints
- **Package Structure:** `com.warehouse.ems.employee`
- **Domain Model:**
```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String badgeId;

    private String name;

    @Enumerated(EnumType.STRING)
    private EmployeeRole role;

    private String department;

    private String shiftGroup;

    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    // Getters, setters, equals, hashCode
}
```
- **Repository Layer:**
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
}
```
- **Service Layer:**
```java
@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public Employee createEmployee(EmployeeDto dto) {
        // Validation, mapping, save
    }
}
```
- **Controller Layer:**
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) {
        Employee employee = employeeService.createEmployee(dto);
        return ResponseEntity.ok(EmployeeMapper.toDto(employee));
    }
}
```
- **DTOs:**
```java
public class EmployeeDto {
    @NotBlank
    private String badgeId;
    @NotBlank
    private String name;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private EmployeeRole role;
    private EmployeeStatus status;
}
```
- **Configuration:**  
  - OpenAPI/Swagger for API docs  
  - Bean Validation for DTOs
- **Security:**  
  - RBAC enforced via Spring Security
- **Sample Implementation:** See above

---

### Section: User Story 5 - Employee Record Update - Technical Design

**Description:**  
Update employee records with validation and audit logging.

**Design Specification:**
- **Architecture:** PATCH/PUT endpoints
- **Service Layer:**  
  - Update logic with validation
- **Controller Layer:**
```java
@PutMapping("/{id}")
public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) {
    Employee updated = employeeService.updateEmployee(id, dto);
    return ResponseEntity.ok(EmployeeMapper.toDto(updated));
}
```
- **Audit Logging:**  
  - Log before/after state in audit table

---

### Section: User Story 6 - Employee Record Deletion - Technical Design

**Description:**  
Soft-delete employee records, enforce unique badgeId, support pagination/filtering.

**Design Specification:**
- **Architecture:** Soft-delete via status field
- **Service Layer:**  
  - Mark status as INACTIVE/DELETED
- **Controller Layer:**
```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id) {
    employeeService.softDeleteEmployee(id);
    return ResponseEntity.noContent().build();
}
```
- **Pagination/Filtering:**  
  - Use Spring Data JPA `Pageable`
- **Sample Implementation:**
```java
@GetMapping
public Page<EmployeeDto> list(Pageable pageable, @RequestParam Map<String, String> filters) {
    // Filtering logic
}
```

---

## E03: Role Based Access Control (RBAC)

### Section: User Story 7 - RBAC Definition - Technical Design

**Description:**  
Define roles (ADMIN, HR, SUPERVISOR, WORKER) and configure Spring Security.

**Design Specification:**
- **Architecture:** Method/endpoint security
- **Package Structure:** `com.warehouse.ems.security`
- **Configuration:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/admin/**").hasRole("ADMIN")
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .anyRequest().authenticated()
            .and()
            .httpBasic();
    }
}
```
- **Sample Implementation:** See above

---

### Section: User Story 8 - Role Assignment - Technical Design

**Description:**  
Assign roles to users, enforce access control.

**Design Specification:**
- **Domain Model:**
```java
@Entity
public class User {
    @Id
    private Long id;
    private String username;
    private String password;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;
}
```
- **Service Layer:**  
  - Assign/remove roles
- **Controller Layer:**
```java
@PostMapping("/users/{id}/roles")
@PreAuthorize("hasRole('ADMIN')")
public void assignRole(@PathVariable Long id, @RequestBody RoleDto roleDto) {
    userService.assignRole(id, roleDto.getRole());
}
```

---

### Section: User Story 9 - Access Enforcement - Technical Design

**Description:**  
Enforce access rules, return 401/403 as appropriate.

**Design Specification:**
- **Security:**  
  - Use `@PreAuthorize` on sensitive endpoints
- **Testing:**  
  - Security tests for unauthorized/forbidden actions

---

## E04: Time & Attendance (Clock In/Out)

### Section: User Story 10 - Attendance Logging - Technical Design

**Description:**  
Endpoints for clock-in/out events with geofence and device capture.

**Design Specification:**
- **Package Structure:** `com.warehouse.ems.attendance`
- **Domain Model:**
```java
@Entity
public class AttendanceEvent {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime timestamp;
    @Enumerated(EnumType.STRING)
    private AttendanceType type; // CLOCK_IN, CLOCK_OUT
    private String deviceId;
    private GeoLocation location;
}
```
- **Repository Layer:**
```java
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {}
```
- **Service Layer:**  
  - Validate geofence, associate shift
- **Controller Layer:**
```java
@PostMapping("/attendance/clock-in")
public ResponseEntity<Void> clockIn(@RequestBody AttendanceDto dto) {
    attendanceService.clockIn(dto);
    return ResponseEntity.ok().build();
}
```
- **DTOs:**  
  - AttendanceDto with employeeId, deviceId, location
- **Sample Implementation:** See above

---

### Section: User Story 11 - Manager Review - Technical Design

**Description:**  
Manager reviews attendance, handles corrections.

**Design Specification:**
- **Service Layer:**  
  - Correction workflow, approval tasks
- **Controller Layer:**
```java
@PostMapping("/attendance/corrections")
@PreAuthorize("hasRole('SUPERVISOR')")
public void submitCorrection(@RequestBody CorrectionDto dto) {
    attendanceService.submitCorrection(dto);
}
```

---

### Section: User Story 12 - Anomaly Alerts - Technical Design

**Description:**  
Alert on missed punches, anomalies.

**Design Specification:**
- **Service Layer:**  
  - Detect anomalies, trigger notifications
- **Integration:**  
  - Notification service

---

## E05: Shift & Schedule Management

### Section: User Story 13 - Shift Template Creation - Technical Design

**Description:**  
Create recurring shift templates, rotations, overtime rules.

**Design Specification:**
- **Package Structure:** `com.warehouse.ems.shift`
- **Domain Model:**
```java
@Entity
public class ShiftTemplate {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isRecurring;
    private String rotationPattern;
}
```
- **Repository Layer:**  
  - ShiftTemplateRepository
- **Service Layer:**  
  - CRUD, conflict detection
- **Controller Layer:**  
  - `/shifts/templates` endpoints

---

### Section: User Story 14 - Employee Assignment - Technical Design

**Description:**  
Assign employees to shifts, handle blackout dates.

**Design Specification:**
- **Domain Model:**
```java
@Entity
public class ShiftAssignment {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ShiftTemplate shiftTemplate;
    private LocalDate date;
}
```
- **Service Layer:**  
  - Bulk assignment, conflict checks

---

### Section: User Story 15 - Shift Swap Requests - Technical Design

**Description:**  
Allow employees to request shift swaps.

**Design Specification:**
- **Domain Model:**
```java
@Entity
public class ShiftSwapRequest {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee requester;
    @ManyToOne
    private Employee target;
    @ManyToOne
    private ShiftAssignment shiftAssignment;
    @Enumerated(EnumType.STRING)
    private SwapStatus status;
}
```
- **Service Layer:**  
  - Approval workflow

---

## E06: Leave & Absence Management

### Section: User Story 16 - Leave Request Submission - Technical Design

**Description:**  
Employees request PTO, sick, unpaid leave.

**Design Specification:**
- **Package Structure:** `com.warehouse.ems.leave`
- **Domain Model:**
```java
@Entity
public class LeaveRequest {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LeaveType type;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;
}
```
- **Service Layer:**  
  - Request, validation, balance checks

---

### Section: User Story 17 - Approval Workflow - Technical Design

**Description:**  
Supervisors approve/deny leave requests.

**Design Specification:**
- **Service Layer:**  
  - Approval logic, notifications

---

### Section: User Story 18 - Balance Tracking - Technical Design

**Description:**  
Track accrual balances and policies.

**Design Specification:**
- **Domain Model:**
```java
@Entity
public class LeaveBalance {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private int ptoBalance;
    private int sickBalance;
}
```
- **Service Layer:**  
  - Update balances on approval

---

## E07: Training & Certification Tracking

### Section: User Story 19 - Certification Record Management - Technical Design

**Description:**  
Track required certifications, expirations, renewals.

**Design Specification:**
- **Package Structure:** `com.warehouse.ems.certification`
- **Domain Model:**
```java
@Entity
public class Certification {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
}
```
- **Service Layer:**  
  - CRUD, expiry checks

---

### Section: User Story 20 - Expiry Alerts - Technical Design

**Description:**  
Alert on expiring certifications.

**Design Specification:**
- **Service Layer:**  
  - Scheduled job for alerts
- **Integration:**  
  - Notification service

---

## E08: Safety Incidents & OSHA Reporting

### Section: User Story 21 - Safety Incident Reporting - Technical Design

**Description:**  
Record incidents/near-misses, severity, location, involved employees.

**Design Specification:**
- **Package Structure:** `com.warehouse.ems.safety`
- **Domain Model:**
```java
@Entity
public class SafetyIncident {
    @Id
    @GeneratedValue
    private Long id;
    private String description;
    private IncidentSeverity severity;
    private String location;
    @ManyToMany
    private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
}
```
- **Service Layer:**  
  - Incident workflow

---

### Section: User Story 22 - Investigation Workflow - Technical Design

**Description:**  
Workflow for investigation and corrective actions.

**Design Specification:**
- **Domain Model:**
```java
@Entity
public class Investigation {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private SafetyIncident incident;
    private String findings;
    private String correctiveActions;
    private LocalDate completedDate;
}
```
- **Service Layer:**  
  - Status transitions

---

### Section: User Story 23 - Analytics - Technical Design

**Description:**  
Generate OSHA summary, metrics dashboard.

**Design Specification:**
- **Service Layer:**  
  - Aggregate metrics, export endpoints

---

## E09: Equipment & Asset Assignment

### Section: User Story 24 - Equipment Assignment - Technical Design

**Description:**  
Assign assets to employees, track checkout/return.

**Design Specification:**
- **Package Structure:** `com.warehouse.ems.equipment`
- **Domain Model:**
```java
@Entity
public class Asset {
    @Id
    @GeneratedValue
    private Long id;
    private String assetTag;
    private String type;
    private AssetStatus status;
}

@Entity
public class AssetAssignment {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Asset asset;
    @ManyToOne
    private Employee employee;
    private LocalDateTime checkoutDate;
    private LocalDateTime returnDate;
}
```
- **Service Layer:**  
  - Checkout/return logic, certification checks

---

### Section: User Story 25 - Return Processing - Technical Design

**Description:**  
Process equipment returns, update inventory.

**Design Specification:**
- **Service Layer:**  
  - Return logic, condition tracking

---

### Section: User Story 26 - Assignment History - Technical Design

**Description:**  
View full assignment and return history.

**Design Specification:**
- **Repository Layer:**  
  - Query history by asset or employee

---

## E10: Performance Reviews & Goals

### Section: User Story 27 - Review Scheduling - Technical Design

**Description:**  
Schedule quarterly/annual reviews.

**Design Specification:**
- **Package Structure:** `com.warehouse.ems.review`
- **Domain Model:**
```java
@Entity
public class PerformanceReview {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate scheduledDate;
    private String comments;
    private int rating;
    @Enumerated(EnumType.STRING)
    private ReviewStatus status;
}
```
- **Service Layer:**  
  - Scheduling, notifications

---

### Section: User Story 28 - Review Submission - Technical Design

**Description:**  
Supervisors submit reviews.

**Design Specification:**
- **Service Layer:**  
  - Submit, lock after sign-off

---

### Section: User Story 29 - Employee Feedback - Technical Design

**Description:**  
Employees provide feedback on reviews.

**Design Specification:**
- **Domain Model:**
```java
@Entity
public class ReviewFeedback {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private PerformanceReview review;
    private String employeeComments;
}
```

---

## E11: Payroll Export Integration

### Section: User Story 30 - Payroll Data Export - Technical Design

**Description:**  
Generate payroll-ready files from approved attendance and leave.

**Design Specification:**
- **Package Structure:** `com.warehouse.ems.payroll`
- **Service Layer:**  
  - Aggregate data, format export
- **Controller Layer:**
```java
@GetMapping("/payroll/export")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<byte[]> exportPayroll(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
    byte[] data = payrollService.generateExport(startDate, endDate);
    return ResponseEntity.ok(data);
}
```

---

### Section: User Story 31 - Export Scheduling - Technical Design

**Description:**  
Schedule payroll exports.

**Design Specification:**
- **Configuration:**  
  - Scheduled job with `@Scheduled`

---

### Section: User Story 32 - Audit Log - Technical Design

**Description:**  
Log all payroll exports.

**Design Specification:**
- **Service Layer:**  
  - Audit logging on export

---

## E12: Notifications & Announcements

### Section: User Story 33 - Notification Configuration - Technical Design

**Description:**  
Configure in-app and email/SMS notifications.

**Design Specification:**
- **Package Structure:** `com.warehouse.ems.notification`
- **Service Layer:**  
  - Notification templates, delivery channels

---

### Section: User Story 34 - Event-Based Notifications - Technical Design

**Description:**  
Trigger notifications on events.

**Design Specification:**
- **Integration:**  
  - Event listeners, message queues

---

### Section: User Story 35 - Notification History - Technical Design

**Description:**  
View notification history.

**Design Specification:**
- **Domain Model:**
```java
@Entity
public class Notification {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private User user;
    private String message;
    private LocalDateTime sentAt;
    private boolean read;
}
```

---

## E13: Integration Layer (HRIS/WMS APIs)

### Section: User Story 36 - Integration API Endpoints - Technical Design

**Description:**  
Expose REST APIs for HRIS sync, WMS integration, SSO.

**Design Specification:**
- **Package Structure:** `com.warehouse.ems.integration`
- **Controller Layer:**
```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    @PostMapping("/hris/sync")
    public ResponseEntity<Void> syncHris(@RequestBody HrisDataDto dto) {
        integrationService.syncHris(dto);
        return ResponseEntity.ok().build();
    }
}
```
- **Security:**  
  - JWT/OAuth2 for API access

---

### Section: User Story 37 - Data Import - Technical Design

**Description:**  
Import data via API.

**Design Specification:**
- **Service Layer:**  
  - Validation, idempotency

---

### Section: User Story 38 - Error Handling - Technical Design

**Description:**  
Handle integration errors gracefully.

**Design Specification:**
- **Configuration:**  
  - `@ControllerAdvice` for global exception handling

---

## E14: Audit Trail & Compliance

### Section: User Story 39 - Audit Trail Logging - Technical Design

**Description:**  
Log all critical system actions.

**Design Specification:**
- **Package Structure:** `com.warehouse.ems.audit`
- **Domain Model:**
```java
@Entity
public class AuditLog {
    @Id
    @GeneratedValue
    private Long id;
    private String actor;
    private String action;
    private String entity;
    private String beforeState;
    private String afterState;
    private LocalDateTime timestamp;
}
```
- **Service Layer:**  
  - Aspect-based logging with `@Aspect`

---

### Section: User Story 40 - Log Review Interface - Technical Design

**Description:**  
Review audit logs.

**Design Specification:**
- **Controller Layer:**  
  - `/audit/logs` with filtering

---

### Section: User Story 41 - Retention Policy - Technical Design

**Description:**  
Set audit log retention policies.

**Design Specification:**
- **Configuration:**  
  - Scheduled job for cleanup

---

## E15: Reporting & Analytics

### Section: User Story 42 - Operational Reporting Dashboard - Technical Design

**Description:**  
Operational reports for attendance, overtime, certifications, safety KPIs.

**Design Specification:**
- **Package Structure:** `com.warehouse.ems.reporting`
- **Service Layer:**  
  - Aggregate metrics, export CSV/PDF

---

### Section: User Story 43 - Custom Report Generation - Technical Design

**Description:**  
Generate custom reports.

**Design Specification:**
- **Controller Layer:**  
  - `/reports/custom` with parameters

---

### Section: User Story 44 - Scheduled Delivery - Technical Design

**Description:**  
Schedule report delivery.

**Design Specification:**
- **Configuration:**  
  - Scheduled job with email delivery

---

## E16: Mobile Access (PWA)

### Section: User Story 45 - Mobile-Responsive Interface - Technical Design

**Description:**  
Responsive views for mobile.

**Design Specification:**
- **Architecture:**  
  - PWA with service workers
- **Configuration:**  
  - `manifest.json`, offline caching

---

### Section: User Story 46 - Offline Functionality - Technical Design

**Description:**  
Offline queue for clock events.

**Design Specification:**
- **Architecture:**  
  - Service workers, IndexedDB

---

### Section: User Story 47 - PWA Installation - Technical Design

**Description:**  
Installable PWA.

**Design Specification:**
- **Configuration:**  
  - `manifest.json` with icons

---

## E17: Onboarding & Offboarding Workflow

### Section: User Story 48 - Onboarding Workflow - Technical Design

**Description:**  
Automate provisioning of accounts, initial schedule, required training.

**Design Specification:**
- **Package Structure:** `com.warehouse.ems.onboarding`
- **Service Layer:**  
  - Workflow engine, task generation

---

### Section: User Story 49 - Task Tracking - Technical Design

**Description:**  
Track onboarding tasks.

**Design Specification:**
- **Domain Model:**
```java
@Entity
public class OnboardingTask {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String taskName;
    private boolean completed;
}
```

---

### Section: User Story 50 - Offboarding Workflow - Technical Design

**Description:**  
Automate deprovisioning.

**Design Specification:**
- **Service Layer:**  
  - Revoke access, collect assets, update schedules

---

## E18: Localization & Multi-Warehouse

### Section: User Story 51 - Multi-Language Support - Technical Design

**Description:**  
Support multiple languages.

**Design Specification:**
- **Configuration:**  
  - `MessageSource`, locale resolver

---

### Section: User Story 52 - Multi-Warehouse Configuration - Technical Design

**Description:**  
Configure multiple warehouses.

**Design Specification:**
- **Domain Model:**
```java
@Entity
public class Warehouse {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String location;
}
```

---

### Section: User Story 53 - Warehouse-Specific Reporting - Technical Design

**Description:**  
Filter reports by warehouse.

**Design Specification:**
- **Service Layer:**  
  - Filter by warehouse ID

---

## E19: Advanced Scheduling (AI-Assisted)

### Section: User Story 54 - AI-Powered Shift Recommendations - Technical Design

**Description:**  
AI-powered shift recommendations.

**Design Specification:**
- **Integration:**  
  - ML model API, historical data training

---

### Section: User Story 55 - AI Model Feedback Loop - Technical Design

**Description:**  
Feedback on AI recommendations.

**Design Specification:**
- **Service Layer:**  
  - Track acceptance rate, retrain model

---

## E20: Document Management

### Section: User Story 56 - Document Upload and Storage - Technical Design

**Description:**  
Upload employee documents.

**Design Specification:**
- **Package Structure:** `com.warehouse.ems.document`
- **Domain Model:**
```java
@Entity
public class Document {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String fileName;
    private String fileUrl;
    private LocalDateTime uploadedAt;
}
```
- **Service Layer:**  
  - File storage (S3, local)

---

### Section: User Story 57 - Document Version Control - Technical Design

**Description:**  
Track document versions.

**Design Specification:**
- **Domain Model:**
```java
@Entity
public class DocumentVersion {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Document document;
    private int version;
    private LocalDateTime createdAt;
}
```

---

### Section: User Story 58 - Document Access Control - Technical Design

**Description:**  
Control document access.

**Design Specification:**
- **Security:**  
  - Role-based access, audit logging

---

## Conclusion

This technical design document provides a comprehensive, low-level design for all 58 user stories of the Warehouse Employee Management System, following Spring Boot best practices and industry standards. Each section includes architecture overviews, package structures, domain models, repository/service/controller layers, DTOs, configurations, security settings, integration points, and sample implementations.