# Warehouse EMS - Low-Level Technical Design Document

## Table of Contents
- Epic E01: Project Scaffolding
  - User Story 1: Initialize Spring Boot Project
  - User Story 2: Configure Database Migration Tool
  - User Story 3: Enable Actuator Health Endpoint
- Epic E02: Employee Master Data
  - User Story 4: Create Employee Domain Model
  - User Story 5: Implement Employee CRUD APIs
  - User Story 6: Enforce Unique Badge ID
- Epic E03: RBAC
  - User Story 7: Implement Role-Based Endpoint Security
  - User Story 8: Add Row-Level Security Constraints
  - User Story 9: Support API Key/OAuth2 Toggle
- Epic E04: Time & Attendance
  - User Story 10: Implement Clock-In/Clock-Out Endpoints
  - User Story 11: Calculate Hours Worked Per Shift
  - User Story 12: Handle Missed Punches and Corrections
- Epic E05: Shift Management
  - User Story 13: Create Shift Templates
  - User Story 14: Assign Shifts to Employees
  - User Story 15: Detect and Prevent Scheduling Conflicts
- Epic E06: Leave Management
  - User Story 16: Request Leave (PTO, Sick, Unpaid)
  - User Story 17: Approve or Deny Leave Requests
  - User Story 18: Update Accrual Balances
- Epic E07: Training & Certification
  - User Story 19: Track Employee Certifications
  - User Story 20: Block Assignment to Unqualified Tasks
- Epic E08: Safety Incidents
  - User Story 21: Record Safety Incidents
  - User Story 22: Manage Incident Investigation Workflow
  - User Story 23: Generate OSHA Summary Reports
- Epic E09: Equipment & Asset Assignment
  - User Story 24: Assign Equipment and PPE to Employees
  - User Story 25: Track Asset Checkout and Return
  - User Story 26: Prevent Use of Assets with Invalid Certification
- Epic E10: Performance Reviews
  - User Story 27: Create Performance Review Templates
  - User Story 28: Assign and Submit Performance Reviews
  - User Story 29: Maintain Immutable Review History
- Epic E11: Payroll Integration
  - User Story 30: Generate Payroll Export Files
  - User Story 31: Retry Failed Payroll Deliveries
- Epic E12: Notifications
  - User Story 32: Send Shift Change Notifications
  - User Story 33: Notify Expiring Certifications
  - User Story 34: Announcements on Dashboard
- Epic E13: Integration Layer
  - User Story 35: Expose HRIS REST API
  - User Story 36: Integrate WMS for Department/Location
  - User Story 37: Support SSO via IDP
- Epic E14: Audit Trail
  - User Story 38: Log Sensitive Data Changes
  - User Story 39: Export Audit Logs
  - User Story 40: Validate Audit Coverage
- Epic E15: Reporting & Analytics
  - User Story 41: Attendance & Overtime Reports
  - User Story 42: Certification Status Dashboard
  - User Story 43: Safety KPIs Reporting
- Epic E16: Mobile Access (PWA)
  - User Story 44: Mobile Clock-In/Out
  - User Story 45: View Schedules on Mobile
  - User Story 46: Request Leave via Mobile
- Epic E17: Onboarding/Offboarding
  - User Story 47: Automate New Hire Provisioning
  - User Story 48: Offboarding Asset Collection
  - User Story 49: Training Task Generation for New Hires
- Epic E18: Localization
  - User Story 50: Localize UI & Notifications
  - User Story 51: Multi-Warehouse Data Segregation
  - User Story 52: Department & Location Mapping
- Epic E19: Observability
  - User Story 53: Application Health Dashboard
  - User Story 54: Alerting on Critical Failures
  - User Story 55: Metrics Endpoint for BI Integration
- Epic E20: CI/CD
  - User Story 56: Automated CI/CD Pipeline

---

## Epic E01: Project Scaffolding

### User Story 1: Initialize Spring Boot Project

**Section: Spring Boot Architecture Overview**  
Description: Establishes the foundational layered architecture for the Warehouse EMS, including base modules for employee, scheduling, attendance, and safety.  
Design Specification:
- Layered architecture: Controller, Service, Repository, Domain, DTO, Config
- Maven multi-module structure (core, api, web, integration)
- Base package: `com.warehouse.ems`
- Sub-packages: `employee`, `scheduling`, `attendance`, `safety`, `config`, `common`
Sample Implementation:
```java
// Maven pom.xml (parent)
<modules>
  <module>core</module>
  <module>api</module>
  <module>web</module>
  <module>integration</module>
</modules>

// Base package structure
src/main/java/com/warehouse/ems/
  âââ employee
  âââ scheduling
  âââ attendance
  âââ safety
  âââ config
  âââ common
```

**Section: Configuration**  
Description: Initial configuration for application properties, Flyway/Liquibase, and Actuator.  
Design Specification:
- `application.yml` with DB, actuator, and migration settings
- Flyway/Liquibase migration scripts in `src/main/resources/db/migration`
Sample Implementation:
```yaml
# application.yml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems
    username: ems_user
    password: ems_pass
  flyway:
    enabled: true
    locations: classpath:db/migration
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

**Section: Health Endpoint**  
Description: Enables actuator health endpoint for monitoring.  
Design Specification:
- Actuator dependency in pom.xml
- Health endpoint exposed at `/actuator/health`
Sample Implementation:
```xml
<!-- pom.xml -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

---

### User Story 2: Configure Database Migration Tool

**Section: Spring Boot Architecture Overview**  
Description: Integrates Flyway/Liquibase for versioned schema management.  
Design Specification:
- Migration scripts in `src/main/resources/db/migration`
- Baseline migration for all core tables
Sample Implementation:
```sql
-- V1__init_schema.sql
CREATE TABLE employee (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  badge_id VARCHAR(20) UNIQUE NOT NULL,
  role VARCHAR(50) NOT NULL,
  department VARCHAR(50),
  shift_group VARCHAR(50),
  hire_date DATE,
  status VARCHAR(20)
);
```

**Section: Configuration**  
Description: Flyway/Liquibase settings in `application.yml`  
Design Specification:
- Enable migration on startup
Sample Implementation:
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
```

---

### User Story 3: Enable Actuator Health Endpoint

**Section: Spring Boot Architecture Overview**  
Description: Exposes health and info endpoints for system monitoring.  
Design Specification:
- Actuator endpoints enabled in `application.yml`
Sample Implementation:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```
---

## Epic E02: Employee Master Data

### User Story 4: Create Employee Domain Model

**Section: Entity Design**  
Description: Defines the Employee JPA entity with all required fields and relationships.  
Design Specification:
- Entity: `Employee`
- Fields: id, name, badgeId, role, department, shiftGroup, hireDate, status
Sample Implementation:
```java
@Entity
@Table(name = "employee")
public class Employee {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(unique = true, nullable = false)
  private String badgeId;

  @Column(nullable = false)
  private String role;

  @Column
  private String department;

  @Column
  private String shiftGroup;

  @Column
  private LocalDate hireDate;

  @Column
  private String status;
}
```

**Section: Package Structure**  
Description: Employee domain in `com.warehouse.ems.employee`  
Design Specification:
- Entity: `com.warehouse.ems.employee.Employee`
- Repository: `com.warehouse.ems.employee.EmployeeRepository`
- Service: `com.warehouse.ems.employee.EmployeeService`
- Controller: `com.warehouse.ems.employee.EmployeeController`

---

### User Story 5: Implement Employee CRUD APIs

**Section: Controller Layer**  
Description: RESTful endpoints for employee CRUD operations.  
Design Specification:
- Endpoints: `/employees` (POST, GET, PUT, PATCH, DELETE)
- Pagination, filtering, soft-delete
Sample Implementation:
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
  @Autowired
  private EmployeeService employeeService;

  @PostMapping
  public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO dto) {
    return ResponseEntity.ok(employeeService.createEmployee(dto));
  }

  @GetMapping
  public Page<EmployeeResponseDTO> getEmployees(
      @RequestParam Optional<String> department,
      Pageable pageable) {
    return employeeService.getEmployees(department, pageable);
  }

  @PutMapping("/{id}")
  public ResponseEntity<EmployeeResponseDTO> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDTO dto) {
    return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
    employeeService.softDeleteEmployee(id);
    return ResponseEntity.noContent().build();
  }
}
```

**Section: DTOs**  
Description: Request and response DTOs with validation.  
Design Specification:
- `EmployeeRequestDTO`, `EmployeeResponseDTO`
Sample Implementation:
```java
public class EmployeeRequestDTO {
  @NotBlank
  private String name;
  @NotBlank
  private String badgeId;
  @NotBlank
  private String role;
  private String department;
  private String shiftGroup;
  private LocalDate hireDate;
  private String status;
}
```

---

### User Story 6: Enforce Unique Badge ID

**Section: Entity Design & Repository Layer**  
Description: Enforces uniqueness at DB and application level.  
Design Specification:
- `@Column(unique = true)` on badgeId
- Custom repository method to check existence
Sample Implementation:
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  boolean existsByBadgeId(String badgeId);
}
```

**Section: Service Layer**  
Description: Validates uniqueness before creation.  
Sample Implementation:
```java
@Service
public class EmployeeService {
  @Autowired
  private EmployeeRepository employeeRepository;

  public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {
    if (employeeRepository.existsByBadgeId(dto.getBadgeId())) {
      throw new DuplicateBadgeIdException("Badge ID already exists");
    }
    // ... create employee
  }
}
```

---

## Epic E03: RBAC

### User Story 7: Implement Role-Based Endpoint Security

**Section: Security**  
Description: Uses Spring Security for role-based access control.  
Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Method security with `@PreAuthorize`
Sample Implementation:
```java
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .authorizeRequests()
      .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR")
      .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
      .anyRequest().authenticated()
      .and()
      .httpBasic();
  }
}
```

---

### User Story 8: Add Row-Level Security Constraints

**Section: Service Layer & Security**  
Description: Supervisors limited to their team data.  
Design Specification:
- Filter queries by supervisor/team
- Custom repository methods
Sample Implementation:
```java
@PreAuthorize("hasRole('SUPERVISOR')")
public Page<Employee> getTeamEmployees(Long supervisorId, Pageable pageable) {
  return employeeRepository.findBySupervisorId(supervisorId, pageable);
}
```

---

### User Story 9: Support API Key/OAuth2 Toggle

**Section: Configuration & Security**  
Description: Configurable authentication methods.  
Design Specification:
- Toggle in `application.yml`
- Conditional beans for API Key or OAuth2
Sample Implementation:
```yaml
security:
  auth-method: oauth2 # or apikey
```
```java
@Configuration
public class AuthConfig {
  @Value("${security.auth-method}")
  private String authMethod;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    if ("apikey".equals(authMethod)) {
      // API Key config
    } else {
      // OAuth2 config
    }
    return http.build();
  }
}
```

---

## Epic E04: Time & Attendance

### User Story 10: Implement Clock-In/Clock-Out Endpoints

**Section: Entity Design**  
Description: Attendance entity with geofence and device capture.  
Design Specification:
- Entity: `Attendance`
- Fields: id, employeeId, clockInTime, clockOutTime, deviceId, location
Sample Implementation:
```java
@Entity
@Table(name = "attendance")
public class Attendance {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "employee_id")
  private Employee employee;

  @Column
  private LocalDateTime clockInTime;

  @Column
  private LocalDateTime clockOutTime;

  @Column
  private String deviceId;

  @Column
  private String location;
}
```

**Section: Controller Layer**  
Description: Endpoints for clock-in/out.  
Sample Implementation:
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
  @PostMapping("/clock-in")
  public ResponseEntity<AttendanceResponseDTO> clockIn(@Valid @RequestBody ClockInRequestDTO dto) {
    // ... implementation
  }

  @PostMapping("/clock-out")
  public ResponseEntity<AttendanceResponseDTO> clockOut(@Valid @RequestBody ClockOutRequestDTO dto) {
    // ... implementation
  }
}
```

---

### User Story 11: Calculate Hours Worked Per Shift

**Section: Service Layer**  
Description: Computes daily totals automatically.  
Design Specification:
- Calculate duration between clock-in and clock-out
Sample Implementation:
```java
@Service
public class AttendanceService {
  public Duration calculateHoursWorked(Long attendanceId) {
    Attendance attendance = attendanceRepository.findById(attendanceId).orElseThrow();
    return Duration.between(attendance.getClockInTime(), attendance.getClockOutTime());
  }
}
```

---

### User Story 12: Handle Missed Punches and Corrections

**Section: Workflow & Controller Layer**  
Description: Approval workflow for corrections.  
Design Specification:
- CorrectionRequest entity
- Approval endpoints
Sample Implementation:
```java
@Entity
public class CorrectionRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne
  private Attendance attendance;
  @Column
  private String reason;
  @Column
  private String status; // PENDING, APPROVED, REJECTED
}

@RestController
@RequestMapping("/attendance/corrections")
public class CorrectionController {
  @PostMapping
  public ResponseEntity<CorrectionResponseDTO> requestCorrection(@Valid @RequestBody CorrectionRequestDTO dto) {
    // ... implementation
  }

  @PatchMapping("/{id}/approve")
  public ResponseEntity<Void> approveCorrection(@PathVariable Long id) {
    // ... implementation
  }
}
```

---

## Epic E05: Shift Management

### User Story 13: Create Shift Templates

**Section: Entity Design**  
Description: ShiftTemplate entity for recurring shifts.  
Design Specification:
- Entity: `ShiftTemplate`
- Fields: id, name, startTime, endTime, recurrence, overtimeRules
Sample Implementation:
```java
@Entity
public class ShiftTemplate {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column
  private String name;
  @Column
  private LocalTime startTime;
  @Column
  private LocalTime endTime;
  @Column
  private String recurrence; // e.g., DAILY, WEEKLY
  @Column
  private String overtimeRules;
}
```

---

### User Story 14: Assign Shifts to Employees

**Section: Service Layer & Controller Layer**  
Description: Bulk assignment of shifts.  
Design Specification:
- Assignment entity
- Bulk assignment endpoint
Sample Implementation:
```java
@Entity
public class ShiftAssignment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne
  private Employee employee;
  @ManyToOne
  private ShiftTemplate shiftTemplate;
  @Column
  private LocalDate assignmentDate;
}

@RestController
@RequestMapping("/shifts/assignments")
public class ShiftAssignmentController {
  @PostMapping("/bulk")
  public ResponseEntity<Void> bulkAssign(@Valid @RequestBody BulkAssignmentDTO dto) {
    // ... implementation
  }
}
```

---

### User Story 15: Detect and Prevent Scheduling Conflicts

**Section: Service Layer**  
Description: Validation logic for overlapping shifts.  
Design Specification:
- Check for overlapping assignments before saving
Sample Implementation:
```java
@Service
public class ShiftAssignmentService {
  public void assignShift(ShiftAssignment assignment) {
    if (hasConflict(assignment)) {
      throw new SchedulingConflictException("Shift conflict detected");
    }
    shiftAssignmentRepository.save(assignment);
  }

  private boolean hasConflict(ShiftAssignment assignment) {
    // ... check for overlapping shifts
  }
}
```

---

## Epic E06: Leave Management

### User Story 16: Request Leave (PTO, Sick, Unpaid)

**Section: Entity Design**  
Description: LeaveRequest entity.  
Design Specification:
- Entity: `LeaveRequest`
- Fields: id, employeeId, leaveType, startDate, endDate, status
Sample Implementation:
```java
@Entity
public class LeaveRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne
  private Employee employee;
  @Column
  private String leaveType; // PTO, SICK, UNPAID
  @Column
  private LocalDate startDate;
  @Column
  private LocalDate endDate;
  @Column
  private String status; // PENDING, APPROVED, DENIED
}
```

---

### User Story 17: Approve or Deny Leave Requests

**Section: Controller Layer**  
Description: Supervisor approval workflow.  
Sample Implementation:
```java
@RestController
@RequestMapping("/leave/requests")
public class LeaveRequestController {
  @PatchMapping("/{id}/approve")
  public ResponseEntity<Void> approveLeave(@PathVariable Long id) {
    // ... implementation
  }

  @PatchMapping("/{id}/deny")
  public ResponseEntity<Void> denyLeave(@PathVariable Long id) {
    // ... implementation
  }
}
```

---

### User Story 18: Update Accrual Balances

**Section: Service Layer**  
Description: Automatic balance calculations.  
Design Specification:
- Update balances on approval
Sample Implementation:
```java
@Service
public class LeaveService {
  public void approveLeave(Long requestId) {
    LeaveRequest request = leaveRequestRepository.findById(requestId).orElseThrow();
    request.setStatus("APPROVED");
    leaveRequestRepository.save(request);
    updateAccrualBalance(request.getEmployee(), request.getLeaveType(), calculateDays(request));
  }

  private void updateAccrualBalance(Employee employee, String leaveType, int days) {
    // ... update balance
  }
}
```

---

## Epic E07: Training & Certification

### User Story 19: Track Employee Certifications

**Section: Entity Design**  
Description: Certification entity with expiration tracking.  
Design Specification:
- Entity: `Certification`
- Fields: id, employeeId, certType, issueDate, expiryDate
Sample Implementation:
```java
@Entity
public class Certification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne
  private Employee employee;
  @Column
  private String certType;
  @Column
  private LocalDate issueDate;
  @Column
  private LocalDate expiryDate;
}
```

**Section: Notification Integration**  
Description: Alerts 30/7 days before expiry.  
Sample Implementation:
```java
@Scheduled(cron = "0 0 9 * * ?")
public void checkExpiringCertifications() {
  List<Certification> expiring = certificationRepository.findExpiringWithin(30);
  expiring.forEach(cert -> notificationService.sendExpiryAlert(cert));
}
```

---

### User Story 20: Block Assignment to Unqualified Tasks

**Section: Service Layer**  
Description: Validation checks for required certifications.  
Sample Implementation:
```java
@Service
public class TaskAssignmentService {
  public void assignTask(Long employeeId, Long taskId) {
    Task task = taskRepository.findById(taskId).orElseThrow();
    if (!hasRequiredCertification(employeeId, task.getRequiredCertType())) {
      throw new UnqualifiedAssignmentException("Employee lacks required certification");
    }
    // ... assign task
  }
}
```

---

## Epic E08: Safety Incidents

### User Story 21: Record Safety Incidents

**Section: Entity Design**  
Description: SafetyIncident entity.  
Design Specification:
- Entity: `SafetyIncident`
- Fields: id, severity, location, description, involvedEmployees, status
Sample Implementation:
```java
@Entity
public class SafetyIncident {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column
  private String severity;
  @Column
  private String location;
  @Column
  private String description;
  @ManyToMany
  private List<Employee> involvedEmployees;
  @Column
  private String status; // OPEN, INVESTIGATING, RESOLVED
}
```

---

### User Story 22: Manage Incident Investigation Workflow

**Section: Controller Layer**  
Description: Status transitions.  
Sample Implementation:
```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
  @PatchMapping("/{id}/status")
  public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
    // ... implementation
  }
}
```

---

### User Story 23: Generate OSHA Summary Reports

**Section: Reporting & Service Layer**  
Description: Export OSHA 300/300A compliant reports.  
Sample Implementation:
```java
@Service
public class OSHAReportService {
  public byte[] generateOSHA300Report(int year) {
    List<SafetyIncident> incidents = incidentRepository.findByYear(year);
    // ... generate report
  }
}
```

---

## Epic E09: Equipment & Asset Assignment

### User Story 24: Assign Equipment and PPE to Employees

**Section: Entity Design**  
Description: Asset and AssetAssignment entities.  
Design Specification:
- Entity: `Asset`, `AssetAssignment`
Sample Implementation:
```java
@Entity
public class Asset {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column
  private String assetType; // SCANNER, FORKLIFT, PPE
  @Column
  private String assetId;
}

@Entity
public class AssetAssignment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne
  private Employee employee;
  @ManyToOne
  private Asset asset;
  @Column
  private LocalDateTime checkoutTime;
  @Column
  private LocalDateTime returnTime;
}
```

---

### User Story 25: Track Asset Checkout and Return

**Section: Controller Layer**  
Description: Check-in/out workflow.  
Sample Implementation:
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
  @PostMapping("/checkout")
  public ResponseEntity<AssetAssignmentDTO> checkout(@Valid @RequestBody CheckoutRequestDTO dto) {
    // ... implementation
  }

  @PostMapping("/return")
  public ResponseEntity<Void> returnAsset(@Valid @RequestBody ReturnRequestDTO dto) {
    // ... implementation
  }
}
```

---

### User Story 26: Prevent Use of Assets with Invalid Certification

**Section: Service Layer**  
Description: Certification validation on checkout.  
Sample Implementation:
```java
@Service
public class AssetService {
  public void checkout(Long employeeId, Long assetId) {
    Asset asset = assetRepository.findById(assetId).orElseThrow();
    if (!hasValidCertification(employeeId, asset.getRequiredCertType())) {
      throw new InvalidCertificationException("Employee lacks valid certification");
    }
    // ... checkout asset
  }
}
```

---

## Epic E10: Performance Reviews

### User Story 27: Create Performance Review Templates

**Section: Entity Design**  
Description: ReviewTemplate entity.  
Design Specification:
- Entity: `ReviewTemplate`
- Fields: id, name, cycle, competencies
Sample Implementation:
```java
@Entity
public class ReviewTemplate {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column
  private String name;
  @Column
  private String cycle; // QUARTERLY, ANNUAL
  @ElementCollection
  private List<String> competencies;
}
```

---

### User Story 28: Assign and Submit Performance Reviews

**Section: Entity Design & Controller Layer**  
Description: PerformanceReview entity and workflow.  
Sample Implementation:
```java
@Entity
public class PerformanceReview {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne
  private Employee employee;
  @ManyToOne
  private ReviewTemplate template;
  @Column
  private String status; // DRAFT, SUBMITTED, ACKNOWLEDGED
}

@RestController
@RequestMapping("/reviews")
public class ReviewController {
  @PostMapping
  public ResponseEntity<ReviewDTO> createReview(@Valid @RequestBody ReviewRequestDTO dto) {
    // ... implementation
  }

  @PatchMapping("/{id}/submit")
  public ResponseEntity<Void> submitReview(@PathVariable Long id) {
    // ... implementation
  }
}
```

---

### User Story 29: Maintain Immutable Review History

**Section: Audit & Service Layer**  
Description: Immutable history after sign-off.  
Sample Implementation:
```java
@Service
public class ReviewService {
  public void signOffReview(Long reviewId) {
    PerformanceReview review = reviewRepository.findById(reviewId).orElseThrow();
    review.setStatus("SIGNED_OFF");
    review.setImmutable(true);
    reviewRepository.save(review);
  }
}
```

---

## Epic E11: Payroll Integration

### User Story 30: Generate Payroll Export Files

**Section: Service Layer & Integration**  
Description: Export attendance and leave data in provider format.  
Sample Implementation:
```java
@Service
public class PayrollExportService {
  public byte[] generatePayrollExport(LocalDate startDate, LocalDate endDate) {
    List<Attendance> attendances = attendanceRepository.findByDateRange(startDate, endDate);
    List<LeaveRequest> leaves = leaveRequestRepository.findApprovedByDateRange(startDate, endDate);
    // ... generate export file
  }
}
```

---

### User Story 31: Retry Failed Payroll Deliveries

**Section: Integration & Retry Logic**  
Description: Automatic retry with exponential backoff.  
Sample Implementation:
```java
@Service
public class PayrollDeliveryService {
  @Retryable(value = PayrollDeliveryException.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
  public void deliverPayrollExport(byte[] exportData) {
    // ... deliver via SFTP/API
  }
}
```

---

## Epic E12: Notifications

### User Story 32: Send Shift Change Notifications

**Section: Notification Service**  
Description: In-app, email, SMS notifications.  
Sample Implementation:
```java
@Service
public class NotificationService {
  public void sendShiftChangeNotification(Employee employee, ShiftAssignment newShift) {
    // ... send in-app notification
    // ... send email
    // ... send SMS
  }
}
```

---

### User Story 33: Notify Expiring Certifications

**Section: Scheduled Task**  
Description: Alerts 30/7 days before expiry.  
Sample Implementation:
```java
@Scheduled(cron = "0 0 9 * * ?")
public void notifyExpiringCertifications() {
  List<Certification> expiring = certificationRepository.findExpiringWithin(30);
  expiring.forEach(cert -> notificationService.sendExpiryAlert(cert.getEmployee(), cert));
}
```

---

### User Story 34: Announcements on Dashboard

**Section: Entity Design & Controller Layer**  
Description: Broadcast messages to users.  
Sample Implementation:
```java
@Entity
public class Announcement {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column
  private String title;
  @Column
  private String content;
  @Column
  private LocalDateTime publishDate;
}

@RestController
@RequestMapping("/announcements")
public class AnnouncementController {
  @GetMapping
  public List<AnnouncementDTO> getAnnouncements() {
    // ... implementation
  }
}
```

---

## Epic E13: Integration Layer

### User Story 35: Expose HRIS REST API

**Section: Integration & Controller Layer**  
Description: Sync new hires and terminations.  
Sample Implementation:
```java
@RestController
@RequestMapping("/api/hris")
public class HRISIntegrationController {
  @PostMapping("/employees")
  public ResponseEntity<Void> syncEmployee(@Valid @RequestBody HRISEmployeeDTO dto) {
    // ... sync employee
  }
}
```

---

### User Story 36: Integrate WMS for Department/Location

**Section: Integration Service**  
Description: Master data synchronization.  
Sample Implementation:
```java
@Service
public class WMSIntegrationService {
  public void syncDepartments() {
    List<DepartmentDTO> departments = wmsClient.getDepartments();
    departments.forEach(dept -> departmentRepository.save(mapToEntity(dept)));
  }
}
```

---

### User Story 37: Support SSO via IDP

**Section: Security Configuration**  
Description: SAML/OAuth2 single sign-on.  
Sample Implementation:
```java
@Configuration
public class SSOConfig {
  @Bean
  public SecurityFilterChain ssoFilterChain(HttpSecurity http) throws Exception {
    http.oauth2Login();
    return http.build();
  }
}
```

---

## Epic E14: Audit Trail

### User Story 38: Log Sensitive Data Changes

**Section: Audit Entity & Listener**  
Description: Immutable audit log for PII changes.  
Sample Implementation:
```java
@Entity
public class AuditLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column
  private String actor;
  @Column
  private LocalDateTime timestamp;
  @Column
  private String entityType;
  @Column
  private Long entityId;
  @Column
  private String beforeValue;
  @Column
  private String afterValue;
}

@EntityListeners(AuditListener.class)
@Entity
public class Employee {
  // ... fields
}

public class AuditListener {
  @PreUpdate
  public void onUpdate(Employee employee) {
    // ... log changes
  }
}
```

---

### User Story 39: Export Audit Logs

**Section: Service Layer**  
Description: Tamper-evident exports by date/user/entity.  
Sample Implementation:
```java
@Service
public class AuditExportService {
  public byte[] exportAuditLogs(LocalDate startDate, LocalDate endDate) {
    List<AuditLog> logs = auditLogRepository.findByDateRange(startDate, endDate);
    // ... generate CSV with hash
  }
}
```

---

### User Story 40: Validate Audit Coverage

**Section: Test Framework**  
Description: Automated tests for audit completeness.  
Sample Implementation:
```java
@Test
public void testAuditCoverage() {
  Employee employee = new Employee();
  employee.setName("John Doe");
  employeeRepository.save(employee);
  
  employee.setName("Jane Doe");
  employeeRepository.save(employee);
  
  List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId("Employee", employee.getId());
  assertFalse(logs.isEmpty());
}
```

---

## Epic E15: Reporting & Analytics

### User Story 41: Attendance & Overtime Reports

**Section: Reporting Service**  
Description: Filterable reports with CSV/PDF export.  
Sample Implementation:
```java
@Service
public class AttendanceReportService {
  public byte[] generateAttendanceReport(LocalDate startDate, LocalDate endDate, String department) {
    List<Attendance> attendances = attendanceRepository.findByDateRangeAndDepartment(startDate, endDate, department);
    // ... generate CSV/PDF
  }
}
```

---

### User Story 42: Certification Status Dashboard

**Section: Controller Layer**  
Description: Real-time certification compliance view.  
Sample Implementation:
```java
@RestController
@RequestMapping("/reports/certifications")
public class CertificationReportController {
  @GetMapping("/status")
  public CertificationStatusDTO getCertificationStatus() {
    // ... implementation
  }
}
```

---

### User Story 43: Safety KPIs Reporting

**Section: Reporting Service**  
Description: Incident metrics and trends.  
Sample Implementation:
```java
@Service
public class SafetyKPIService {
  public SafetyKPIDTO getSafetyKPIs(int year) {
    List<SafetyIncident> incidents = incidentRepository.findByYear(year);
    // ... calculate KPIs
  }
}
```

---

## Epic E16: Mobile Access (PWA)

### User Story 44: Mobile Clock-In/Out

**Section: PWA Configuration**  
Description: PWA with offline queue support.  
Sample Implementation:
```javascript
// service-worker.js
self.addEventListener('sync', event => {
  if (event.tag === 'sync-attendance') {
    event.waitUntil(syncAttendance());
  }
});

async function syncAttendance() {
  const queue = await getQueuedAttendance();
  for (const attendance of queue) {
    await fetch('/attendance/clock-in', {
      method: 'POST',
      body: JSON.stringify(attendance)
    });
  }
}
```

---

### User Story 45: View Schedules on Mobile

**Section: Responsive UI**  
Description: Responsive schedule display.  
Sample Implementation:
```html
<!-- schedule.html -->
<div class="schedule-container">
  <div class="shift-card" *ngFor="let shift of shifts">
    <h3>{{ shift.date }}</h3>
    <p>{{ shift.startTime }} - {{ shift.endTime }}</p>
  </div>
</div>
```

---

### User Story 46: Request Leave via Mobile

**Section: Mobile-Optimized Form**  
Description: Mobile-optimized leave request flow.  
Sample Implementation:
```html
<!-- leave-request.html -->
<form (submit)="submitLeaveRequest()">
  <input type="date" [(ngModel)]="leaveRequest.startDate" />
  <input type="date" [(ngModel)]="leaveRequest.endDate" />
  <select [(ngModel)]="leaveRequest.leaveType">
    <option value="PTO">PTO</option>
    <option value="SICK">Sick</option>
    <option value="UNPAID">Unpaid</option>
  </select>
  <button type="submit">Submit</button>
</form>
```

---

## Epic E17: Onboarding/Offboarding

### User Story 47: Automate New Hire Provisioning

**Section: Integration & Workflow**  
Description: Account creation, schedule, training tasks.  
Sample Implementation:
```java
@Service
public class OnboardingService {
  public void provisionNewHire(HRISEmployeeDTO hrisEmployee) {
    Employee employee = createEmployee(hrisEmployee);
    createInitialSchedule(employee);
    assignRequiredTraining(employee);
  }
}
```

---

### User Story 48: Offboarding Asset Collection

**Section: Workflow**  
Description: Asset return and access revocation.  
Sample Implementation:
```java
@Service
public class OffboardingService {
  public void offboardEmployee(Long employeeId) {
    flagAssetsForCollection(employeeId);
    revokeAccess(employeeId);
    updateSchedules(employeeId);
  }
}
```

---

### User Story 49: Training Task Generation for New Hires

**Section: Workflow**  
Description: Automatic training assignment.  
Sample Implementation:
```java
@Service
public class TrainingService {
  public void assignRequiredTraining(Employee employee) {
    List<TrainingModule> required = trainingRepository.findRequiredForRole(employee.getRole());
    required.forEach(module -> createTrainingTask(employee, module));
  }
}
```

---

## Epic E18: Localization

### User Story 50: Localize UI & Notifications

**Section: Localization Configuration**  
Description: Multi-language support.  
Sample Implementation:
```yaml
# messages_en.properties
welcome.message=Welcome to Warehouse EMS

# messages_es.properties
welcome.message=Bienvenido a Warehouse EMS
```

---

### User Story 51: Multi-Warehouse Data Segregation

**Section: Row-Level Security**  
Description: Row-level security by warehouse.  
Sample Implementation:
```java
@PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
public List<Employee> getWarehouseEmployees(Long warehouseId) {
  return employeeRepository.findByWarehouseId(warehouseId);
}
```

---

### User Story 52: Department & Location Mapping

**Section: Entity Design**  
Description: Warehouse-specific organizational structure.  
Sample Implementation:
```java
@Entity
public class Department {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column
  private String name;
  @ManyToOne
  private Warehouse warehouse;
}
```

---

## Epic E19: Observability

### User Story 53: Application Health Dashboard

**Section: Actuator & Monitoring**  
Description: Real-time health monitoring.  
Sample Implementation:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
```

---

### User Story 54: Alerting on Critical Failures

**Section: Monitoring & Alerting**  
Description: Automated alert system.  
Sample Implementation:
```java
@Service
public class AlertService {
  public void sendCriticalAlert(String message) {
    // ... send alert via email/SMS
  }
}
```

---

### User Story 55: Metrics Endpoint for BI Integration

**Section: Metrics Endpoint**  
Description: Prometheus/BI-compatible metrics.  
Sample Implementation:
```java
@RestController
@RequestMapping("/metrics")
public class MetricsController {
  @GetMapping("/attendance")
  public AttendanceMetricsDTO getAttendanceMetrics() {
    // ... implementation
  }
}
```

---

## Epic E20: CI/CD

### User Story 56: Automated CI/CD Pipeline

**Section: CI/CD Configuration**  
Description: Build, test, deploy automation with rollback.  
Sample Implementation:
```yaml
# .gitlab-ci.yml
stages:
  - build
  - test
  - deploy

build:
  stage: build
  script:
    - mvn clean package

test:
  stage: test
  script:
    - mvn test

deploy:
  stage: deploy
  script:
    - kubectl apply -f k8s/deployment.yml
  only:
    - main
```

---

**End of Document**