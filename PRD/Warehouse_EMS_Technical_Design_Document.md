# Warehouse Employee Management System (EMS) - Technical Design Document

## Table of Contents
- [E01: Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
- [E02: Employee Master Data CRUD](#e02-employee-master-data-crud)
- [E03: Role Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
- [E04: Time & Attendance Clock In/Out](#e04-time--attendance-clock-inout)
- [E05: Shift & Schedule Management](#e05-shift--schedule-management)
- [E06: Leave & Absence Management](#e06-leave--absence-management)
- [E07: Training & Certification Tracking](#e07-training--certification-tracking)
- [E08: Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
- [E09: Equipment & Asset Assignment](#e09-equipment--asset-assignment)
- [E10: Performance Reviews & Goals](#e10-performance-reviews--goals)
- [E11: Payroll Export Integration](#e11-payroll-export-integration)
- [E12: Notifications & Announcements](#e12-notifications--announcements)
- [E13: Integration Layer HRIS/WMS APIs](#e13-integration-layer-hriswms-apis)
- [E14: Audit Trail & Compliance](#e14-audit-trail--compliance)
- [E15: Reporting & Analytics](#e15-reporting--analytics)
- [E16: Mobile Access PWA](#e16-mobile-access-pwa)
- [E17: Onboarding & Offboarding Workflow](#e17-onboarding--offboarding-workflow)
- [E18: Localization & Multi-Warehouse](#e18-localization--multi-warehouse)
- [E19: Automated Testing & CI/CD](#e19-automated-testing--cicd)
- [E20: Documentation & Runbooks](#e20-documentation--runbooks)

---

## <a name="e01-project-scaffolding--domain-setup"></a>E01: Project Scaffolding & Domain Setup

### Section: Initialize Project Repository
Description: Set up a new Spring Boot project using Maven, establish base package structure, enable Actuator, and configure Flyway/Liquibase for DB migrations.
Design Specification:
- Package Structure: `com.warehouse.ems` as root; sub-packages: `employee`, `schedule`, `attendance`, `safety`, `config`, `common`
- Maven modules: single or multi-module (if microservices planned)
- Enable Spring Boot Actuator for health checks
- Integrate Flyway/Liquibase for DB migrations
- README with build/run instructions
Sample Implementation:
```java
// pom.xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>

// src/main/java/com/warehouse/ems/WarehouseEmsApplication.java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}

// application.properties
spring.application.name=warehouse-ems
server.port=8080
management.endpoints.web.exposure.include=health,info

// README.md
# Build
mvn clean install
# Run
mvn spring-boot:run
```

### Section: Establish Domain Model
Description: Define core domain entities (Employee, Department, Shift, Attendance, SafetyIncident) and their relationships.
Design Specification:
- Entities: Employee, Department, Shift, Attendance, SafetyIncident
- Relationships: Employee-Department (ManyToOne), Employee-Shift (ManyToMany), Employee-Attendance (OneToMany)
- Use JPA annotations, validation constraints
Sample Implementation:
```java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    @Column(unique = true, nullable = false)
    private String badgeId;
    private String name;
    @ManyToOne
    private Department department;
    @ManyToMany
    private Set<Shift> shifts;
    // ...
}
```

### Section: Configure Environment Variables
Description: Use Spring Boot profiles and externalized configuration for environment-specific settings.
Design Specification:
- application.properties for defaults
- application-dev.properties, application-prod.properties for overrides
- Use @Value or @ConfigurationProperties for injection
Sample Implementation:
```properties
# application-dev.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ems_dev
spring.profiles.active=dev
```

### Section: Set Up Initial CI Pipeline
Description: Configure CI (e.g., GitHub Actions) for build, test, and code quality checks.
Design Specification:
- .github/workflows/ci.yml for Maven build/test
- Run unit tests, checkstyle, and code coverage
Sample Implementation:
```yaml
name: CI
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
```

---

## <a name="e02-employee-master-data-crud"></a>E02: Employee Master Data CRUD

### Section: Create Employee Record
Description: Implement API to create employee records with validation and unique badgeId enforcement.
Design Specification:
- Package: `com.warehouse.ems.employee`
- Entity: Employee (see above)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService with @Transactional createEmployee(EmployeeDto)
- Controller: EmployeeController POST /employees
- DTO: EmployeeDto (name, badgeId, role, department, shiftGroup, hireDate, status)
- Security: Only ADMIN/HR can create
- Configuration: OpenAPI schema
Sample Implementation:
```java
// EmployeeDto.java
public class EmployeeDto {
    @NotBlank private String name;
    @NotBlank private String badgeId;
    @NotNull private String role;
    private String department;
    private String shiftGroup;
    @NotNull private LocalDate hireDate;
    private String status;
}

// EmployeeRepository.java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByBadgeId(String badgeId);
}

// EmployeeService.java
@Service
public class EmployeeService {
    @Autowired private EmployeeRepository repo;
    @Transactional
    public Employee createEmployee(EmployeeDto dto) {
        if (repo.existsByBadgeId(dto.getBadgeId())) {
            throw new DuplicateBadgeIdException();
        }
        Employee emp = new Employee(...);
        return repo.save(emp);
    }
}

// EmployeeController.java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) {
        Employee emp = service.createEmployee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(emp));
    }
}
```

### Section: Update Employee Information
Description: Update employee details with PATCH/PUT endpoints, validation, and audit logging.
Design Specification:
- PATCH/PUT /employees/{id}
- Only ADMIN/HR can update
- Audit log before/after
Sample Implementation:
```java
@PatchMapping("/{id}")
@PreAuthorize("hasAnyRole('ADMIN','HR')")
public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @RequestBody EmployeeDto dto) {
    Employee updated = service.updateEmployee(id, dto);
    return ResponseEntity.ok(mapper.toDto(updated));
}
```

### Section: Delete Employee Record
Description: Soft-delete employee records (set status=INACTIVE), restrict to ADMIN.
Design Specification:
- DELETE /employees/{id}
- Soft-delete (do not remove from DB)
- Only ADMIN can delete
Sample Implementation:
```java
@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.softDeleteEmployee(id);
    return ResponseEntity.noContent().build();
}
```

### Section: View Employee Directory
Description: List employees with pagination, filtering, and OpenAPI documentation.
Design Specification:
- GET /employees
- Pageable, filter by department, status
- OpenAPI schema
Sample Implementation:
```java
@GetMapping
@PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
public Page<EmployeeDto> list(@PageableDefault Pageable pageable, @RequestParam Optional<String> department) {
    return service.listEmployees(pageable, department);
}
```

---

## <a name="e03-role-based-access-control-rbac"></a>E03: Role Based Access Control (RBAC)

### Section: Assign User Roles
Description: Assign roles (ADMIN, HR, SUPERVISOR, WORKER) to users; manage via API/UI.
Design Specification:
- Entity: User (username, password, roles)
- Role enum
- UserRepository, UserService
- Controller: POST /users/{id}/roles
- Only ADMIN can assign roles
Sample Implementation:
```java
@Entity
public class User {
    @Id @GeneratedValue private Long id;
    private String username;
    private String password;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;
}

public enum Role { ADMIN, HR, SUPERVISOR, WORKER }

@PostMapping("/users/{id}/roles")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> assignRoles(@PathVariable Long id, @RequestBody Set<Role> roles) {
    userService.assignRoles(id, roles);
    return ResponseEntity.ok().build();
}
```

### Section: Enforce Access Restrictions
Description: Secure endpoints and methods using Spring Security annotations and configuration.
Design Specification:
- Use @PreAuthorize, @Secured
- Method-level and endpoint-level security
- Row-level security in repository/service if needed
Sample Implementation:
```java
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN","HR","SUPERVISOR")
            .antMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
            .and()
            .httpBasic();
    }
}
```

### Section: Role Management UI
Description: UI for managing user roles (not backend, but API endpoints support UI).
Design Specification:
- GET /users, GET /users/{id}, POST /users/{id}/roles
- Only ADMIN can access
Sample Implementation:
```java
@GetMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public List<UserDto> listUsers() {
    return userService.listAll();
}
```

### Section: Audit Role Changes
Description: Log all role assignments/changes for compliance.
Design Specification:
- AuditLog entity (actor, timestamp, action, before/after)
- Service logs changes
Sample Implementation:
```java
public void assignRoles(Long userId, Set<Role> newRoles) {
    User user = repo.findById(userId).orElseThrow();
    Set<Role> before = new HashSet<>(user.getRoles());
    user.setRoles(newRoles);
    repo.save(user);
    auditService.log("ROLE_CHANGE", userId, before, newRoles);
}
```

---

## <a name="e04-time--attendance-clock-inout"></a>E04: Time & Attendance Clock In/Out

### Section: Clock In via Mobile
Description: Allow employees to clock in via mobile app/PWA, capturing device and location.
Design Specification:
- Entity: Attendance (employee, clockIn, clockOut, deviceId, location)
- POST /attendance/clock-in
- Validate geofence if enabled
Sample Implementation:
```java
@PostMapping("/attendance/clock-in")
@PreAuthorize("hasRole('WORKER')")
public ResponseEntity<?> clockIn(@RequestBody ClockInDto dto, Authentication auth) {
    service.clockIn(auth.getName(), dto);
    return ResponseEntity.ok().build();
}

public class ClockInDto {
    private String deviceId;
    private GeoLocation location;
}
```

### Section: Clock Out via Web
Description: Allow employees to clock out via web, update attendance record.
Design Specification:
- PATCH /attendance/clock-out
- Update clockOut time
Sample Implementation:
```java
@PatchMapping("/attendance/clock-out")
@PreAuthorize("hasRole('WORKER')")
public ResponseEntity<?> clockOut(Authentication auth) {
    service.clockOut(auth.getName());
    return ResponseEntity.ok().build();
}
```

### Section: View Attendance History
Description: Employees and supervisors can view attendance records, with pagination and filtering.
Design Specification:
- GET /attendance?employeeId=&from=&to=
- Pageable
Sample Implementation:
```java
@GetMapping("/attendance")
@PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
public Page<AttendanceDto> list(@RequestParam Map<String,String> params, Pageable pageable) {
    return service.listAttendance(params, pageable);
}
```

### Section: Geofence Setup
Description: Admins can configure geofences for clock-in/out validation.
Design Specification:
- Entity: Geofence (id, name, center, radius)
- CRUD endpoints for geofences
Sample Implementation:
```java
@Entity
public class Geofence {
    @Id @GeneratedValue private Long id;
    private String name;
    private double latitude;
    private double longitude;
    private double radiusMeters;
}
```

---

## <a name="e05-shift--schedule-management"></a>E05: Shift & Schedule Management

### Section: Create Shift Templates
Description: Define recurring shift templates (start/end, days, overtime rules).
Design Specification:
- Entity: ShiftTemplate (id, name, startTime, endTime, daysOfWeek, overtimeRule)
- CRUD endpoints
Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    @ElementCollection private Set<DayOfWeek> daysOfWeek;
    private String overtimeRule;
}
```

### Section: Assign Shifts to Employees
Description: Assign shifts to employees, bulk assignment supported.
Design Specification:
- ManyToMany Employee-Shift
- POST /shifts/assign
Sample Implementation:
```java
@PostMapping("/shifts/assign")
@PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
public ResponseEntity<?> assignShifts(@RequestBody ShiftAssignmentDto dto) {
    service.assignShifts(dto);
    return ResponseEntity.ok().build();
}
```

### Section: Manage Shift Rotations
Description: Support rotating schedules and blackout dates.
Design Specification:
- Entity: ShiftRotation (id, employees, pattern, blackoutDates)
Sample Implementation:
```java
@Entity
public class ShiftRotation {
    @Id @GeneratedValue private Long id;
    @ManyToMany private Set<Employee> employees;
    private String pattern;
    @ElementCollection private Set<LocalDate> blackoutDates;
}
```

### Section: View Shift Calendar
Description: Employees can view their upcoming shifts; supervisors see team schedules.
Design Specification:
- GET /shifts/calendar?employeeId=
Sample Implementation:
```java
@GetMapping("/shifts/calendar")
@PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','WORKER')")
public List<ShiftCalendarDto> getCalendar(@RequestParam Long employeeId) {
    return service.getShiftCalendar(employeeId);
}
```

---

## <a name="e06-leave--absence-management"></a>E06: Leave & Absence Management

### Section: Request Paid Time Off PTO
Description: Employees can request PTO, sick, or unpaid leave.
Design Specification:
- Entity: LeaveRequest (id, employee, type, startDate, endDate, status)
- POST /leave/requests
- Only WORKER/EMPLOYEE can request
Sample Implementation:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveStatus status;
}

@PostMapping("/leave/requests")
@PreAuthorize("hasRole('WORKER')")
public ResponseEntity<?> requestLeave(@RequestBody LeaveRequestDto dto, Authentication auth) {
    service.requestLeave(auth.getName(), dto);
    return ResponseEntity.ok().build();
}
```

### Section: Approve Leave Requests
Description: Supervisors/HR can approve or deny leave requests.
Design Specification:
- PATCH /leave/requests/{id}/approve
- Only SUPERVISOR/HR can approve/deny
Sample Implementation:
```java
@PatchMapping("/leave/requests/{id}/approve")
@PreAuthorize("hasAnyRole('SUPERVISOR','HR')")
public ResponseEntity<?> approveLeave(@PathVariable Long id, @RequestBody ApprovalDto dto) {
    service.approveLeave(id, dto);
    return ResponseEntity.ok().build();
}
```

### Section: Track Leave Balances
Description: Track accrual and usage of leave balances.
Design Specification:
- Entity: LeaveBalance (employee, leaveType, balance)
- Service updates on approval
Sample Implementation:
```java
@Entity
public class LeaveBalance {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private LeaveType type;
    private BigDecimal balance;
}
```

### Section: View Absence Calendar
Description: Employees and supervisors can view approved absences.
Design Specification:
- GET /leave/calendar?employeeId=
Sample Implementation:
```java
@GetMapping("/leave/calendar")
@PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','WORKER')")
public List<LeaveCalendarDto> getAbsenceCalendar(@RequestParam Long employeeId) {
    return service.getAbsenceCalendar(employeeId);
}
```

---

## <a name="e07-training--certification-tracking"></a>E07: Training & Certification Tracking

### Section: Record Employee Certifications
Description: Track certifications, issue/expiry dates, and upload proof documents.
Design Specification:
- Entity: Certification (id,employee,name,issueDate,expiryDate,documentUrl)
- POST /certifications
Sample Implementation:
```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String name;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
}

@PostMapping("/certifications")
@PreAuthorize("hasAnyRole('ADMIN','HR')")
public ResponseEntity<?> recordCertification(@RequestBody CertificationDto dto) {
    service.recordCertification(dto);
    return ResponseEntity.ok().build();
}
```

### Section: Notify Expiring Certifications
Description: Scheduled job to notify employees and supervisors of expiring certifications.
Design Specification:
- @Scheduled job checks expiryDate
- Send notifications via NotificationService
Sample Implementation:
```java
@Scheduled(cron = "0 0 8 * * ?")
public void checkExpiringCertifications() {
    List<Certification> expiring = repo.findByExpiryDateBetween(LocalDate.now(), LocalDate.now().plusDays(30));
    for (Certification cert : expiring) {
        notificationService.sendExpiryAlert(cert);
    }
}
```

### Section: View Certification Status
Description: Supervisors can view certification status for their team.
Design Specification:
- GET /certifications?employeeId=
Sample Implementation:
```java
@GetMapping("/certifications")
@PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
public List<CertificationDto> listCertifications(@RequestParam Long employeeId) {
    return service.listCertifications(employeeId);
}
```

### Section: Upload Certification Documents
Description: Employees can upload certification documents (PDF, images).
Design Specification:
- POST /certifications/{id}/upload
- Store in S3 or local storage
Sample Implementation:
```java
@PostMapping("/certifications/{id}/upload")
@PreAuthorize("hasRole('WORKER')")
public ResponseEntity<?> uploadDocument(@PathVariable Long id, @RequestParam MultipartFile file) {
    service.uploadDocument(id, file);
    return ResponseEntity.ok().build();
}
```

---

## <a name="e08-safety-incidents--osha-reporting"></a>E08: Safety Incidents & OSHA Reporting

### Section: Report Safety Incident
Description: Employees can report safety incidents with details and photos.
Design Specification:
- Entity: SafetyIncident (id,reporter,date,location,severity,description,status)
- POST /safety/incidents
Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee reporter;
    private LocalDateTime date;
    private String location;
    private Severity severity;
    private String description;
    private IncidentStatus status;
}

@PostMapping("/safety/incidents")
@PreAuthorize("hasRole('WORKER')")
public ResponseEntity<?> reportIncident(@RequestBody IncidentDto dto, Authentication auth) {
    service.reportIncident(auth.getName(), dto);
    return ResponseEntity.ok().build();
}
```

### Section: Review Incident Reports
Description: Safety officers can review and investigate incidents.
Design Specification:
- GET /safety/incidents
- PATCH /safety/incidents/{id}/status
Sample Implementation:
```java
@GetMapping("/safety/incidents")
@PreAuthorize("hasAnyRole('ADMIN','SAFETY_OFFICER')")
public Page<IncidentDto> listIncidents(Pageable pageable) {
    return service.listIncidents(pageable);
}

@PatchMapping("/safety/incidents/{id}/status")
@PreAuthorize("hasRole('SAFETY_OFFICER')")
public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody StatusDto dto) {
    service.updateStatus(id, dto);
    return ResponseEntity.ok().build();
}
```

### Section: Generate OSHA Reports
Description: Generate OSHA-compliant reports (300/300A) for regulatory compliance.
Design Specification:
- GET /safety/reports/osha?from=&to=
- Export as PDF
Sample Implementation:
```java
@GetMapping("/safety/reports/osha")
@PreAuthorize("hasAnyRole('ADMIN','SAFETY_OFFICER')")
public ResponseEntity<byte[]> generateOshaReport(@RequestParam LocalDate from, @RequestParam LocalDate to) {
    byte[] pdf = service.generateOshaReport(from, to);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf);
}
```

### Section: Track Corrective Actions
Description: Track corrective actions for incidents.
Design Specification:
- Entity: CorrectiveAction (id,incident,assignee,description,status)
- POST /safety/incidents/{id}/actions
Sample Implementation:
```java
@Entity
public class CorrectiveAction {
    @Id @GeneratedValue private Long id;
    @ManyToOne private SafetyIncident incident;
    @ManyToOne private Employee assignee;
    private String description;
    private ActionStatus status;
}

@PostMapping("/safety/incidents/{id}/actions")
@PreAuthorize("hasRole('SAFETY_OFFICER')")
public ResponseEntity<?> addAction(@PathVariable Long id, @RequestBody ActionDto dto) {
    service.addCorrectiveAction(id, dto);
    return ResponseEntity.ok().build();
}
```

---

## <a name="e09-equipment--asset-assignment"></a>E09: Equipment & Asset Assignment

### Section: Assign Equipment to Employees
Description: Assign equipment (scanners, forklifts, PPE) to employees.
Design Specification:
- Entity: Equipment (id,name,serialNumber,status)
- Entity: EquipmentAssignment (id,equipment,employee,assignedDate,returnedDate)
- POST /equipment/assign
Sample Implementation:
```java
@Entity
public class Equipment {
    @Id @GeneratedValue private Long id;
    private String name;
    private String serialNumber;
    private EquipmentStatus status;
}

@Entity
public class EquipmentAssignment {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Equipment equipment;
    @ManyToOne private Employee employee;
    private LocalDate assignedDate;
    private LocalDate returnedDate;
}

@PostMapping("/equipment/assign")
@PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
public ResponseEntity<?> assignEquipment(@RequestBody AssignmentDto dto) {
    service.assignEquipment(dto);
    return ResponseEntity.ok().build();
}
```

### Section: Track Equipment Status
Description: Track equipment status (in use, maintenance, available).
Design Specification:
- PATCH /equipment/{id}/status
Sample Implementation:
```java
@PatchMapping("/equipment/{id}/status")
@PreAuthorize("hasAnyRole('ADMIN','ASSET_MANAGER')")
public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody StatusDto dto) {
    service.updateEquipmentStatus(id, dto);
    return ResponseEntity.ok().build();
}
```

### Section: View Asset Assignment History
Description: View assignment history for audits.
Design Specification:
- GET /equipment/{id}/history
Sample Implementation:
```java
@GetMapping("/equipment/{id}/history")
@PreAuthorize("hasAnyRole('ADMIN','ASSET_MANAGER')")
public List<AssignmentHistoryDto> getHistory(@PathVariable Long id) {
    return service.getAssignmentHistory(id);
}
```

### Section: Return Equipment
Description: Employees can return assigned equipment.
Design Specification:
- POST /equipment/return
Sample Implementation:
```java
@PostMapping("/equipment/return")
@PreAuthorize("hasRole('WORKER')")
public ResponseEntity<?> returnEquipment(@RequestBody ReturnDto dto, Authentication auth) {
    service.returnEquipment(auth.getName(), dto);
    return ResponseEntity.ok().build();
}
```

---

## <a name="e10-performance-reviews--goals"></a>E10: Performance Reviews & Goals

### Section: Create Performance Review Templates
Description: HR can create review templates with criteria and rating scales.
Design Specification:
- Entity: ReviewTemplate (id,name,criteria,ratingScale)
- POST /reviews/templates
Sample Implementation:
```java
@Entity
public class ReviewTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    @ElementCollection private List<String> criteria;
    private String ratingScale;
}

@PostMapping("/reviews/templates")
@PreAuthorize("hasRole('HR')")
public ResponseEntity<?> createTemplate(@RequestBody TemplateDto dto) {
    service.createTemplate(dto);
    return ResponseEntity.ok().build();
}
```

### Section: Assign Reviews to Employees
Description: Supervisors can assign reviews to employees.
Design Specification:
- Entity: PerformanceReview (id,employee,template,status,dueDate)
- POST /reviews/assign
Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private ReviewTemplate template;
    private ReviewStatus status;
    private LocalDate dueDate;
}

@PostMapping("/reviews/assign")
@PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
public ResponseEntity<?> assignReview(@RequestBody AssignReviewDto dto) {
    service.assignReview(dto);
    return ResponseEntity.ok().build();
}
```

### Section: Submit Self-Assessment
Description: Employees can submit self-assessments.
Design Specification:
- PATCH /reviews/{id}/self-assessment
Sample Implementation:
```java
@PatchMapping("/reviews/{id}/self-assessment")
@PreAuthorize("hasRole('WORKER')")
public ResponseEntity<?> submitSelfAssessment(@PathVariable Long id, @RequestBody SelfAssessmentDto dto, Authentication auth) {
    service.submitSelfAssessment(id, auth.getName(), dto);
    return ResponseEntity.ok().build();
}
```

### Section: Track Goal Progress
Description: Employees can track progress on assigned goals.
Design Specification:
- Entity: Goal (id,employee,description,targetDate,progress)
- PATCH /goals/{id}/progress
Sample Implementation:
```java
@Entity
public class Goal {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String description;
    private LocalDate targetDate;
    private int progress;
}

@PatchMapping("/goals/{id}/progress")
@PreAuthorize("hasRole('WORKER')")
public ResponseEntity<?> updateProgress(@PathVariable Long id, @RequestBody ProgressDto dto, Authentication auth) {
    service.updateGoalProgress(id, auth.getName(), dto);
    return ResponseEntity.ok().build();
}
```

---

## <a name="e11-payroll-export-integration"></a>E11: Payroll Export Integration

### Section: Generate Payroll Export File
Description: Generate payroll-ready export files from approved attendance and leave data.
Design Specification:
- GET /payroll/export?from=&to=
- Export as CSV/XLSX
Sample Implementation:
```java
@GetMapping("/payroll/export")
@PreAuthorize("hasAnyRole('ADMIN','PAYROLL_ADMIN')")
public ResponseEntity<byte[]> exportPayroll(@RequestParam LocalDate from, @RequestParam LocalDate to) {
    byte[] csv = service.generatePayrollExport(from, to);
    return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv")).body(csv);
}
```

### Section: Payroll Export File Validation
Description: Validate payroll export files before delivery.
Design Specification:
- Service validates required fields, formats
Sample Implementation:
```java
public void validatePayrollExport(PayrollExport export) {
    if (export.getRecords().stream().anyMatch(r -> r.getEmployeeId() == null)) {
        throw new ValidationException("Missing employee ID");
    }
}
```

### Section: Secure Payroll File Delivery
Description: Deliver payroll files securely via SFTP or API.
Design Specification:
- Use JSch for SFTP or RestTemplate for API
- Encrypt in transit
Sample Implementation:
```java
public void deliverPayrollFile(byte[] file) {
    JSch jsch = new JSch();
    Session session = jsch.getSession(user, host, port);
    session.setPassword(password);
    session.connect();
    ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
    channel.connect();
    channel.put(new ByteArrayInputStream(file), "/remote/path/payroll.csv");
    channel.disconnect();
    session.disconnect();
}
```

### Section: Payroll Export Failure Handling
Description: Retry failed exports with exponential backoff.
Design Specification:
- Use @Retryable or custom retry logic
Sample Implementation:
```java
@Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
public void deliverPayrollFile(byte[] file) {
    // delivery logic
}
```

### Section: Payroll Export Audit Logging
Description: Log every payroll export for compliance.
Design Specification:
- AuditLog entry for each export
Sample Implementation:
```java
public void generatePayrollExport(LocalDate from, LocalDate to) {
    byte[] file = ...;
    auditService.log("PAYROLL_EXPORT", from, to, file.length);
    deliverPayrollFile(file);
}
```

---

## <a name="e12-notifications--announcements"></a>E12: Notifications & Announcements

### Section: Shift Change Notification
Description: Notify employees when their shift changes.
Design Specification:
- NotificationService sends in-app, SMS, email
- Triggered by shift update
Sample Implementation:
```java
public void updateShift(Long shiftId, ShiftDto dto) {
    Shift shift = repo.findById(shiftId).orElseThrow();
    shift.setStartTime(dto.getStartTime());
    repo.save(shift);
    notificationService.sendShiftChangeNotification(shift);
}
```

### Section: Certification Expiry Alert
Description: Notify employees and supervisors of expiring certifications.
Design Specification:
- @Scheduled job checks expiryDate
Sample Implementation:
```java
@Scheduled(cron = "0 0 8 * * ?")
public void checkExpiringCertifications() {
    List<Certification> expiring = repo.findByExpiryDateBetween(LocalDate.now(), LocalDate.now().plusDays(30));
    for (Certification cert : expiring) {
        notificationService.sendExpiryAlert(cert);
    }
}
```

### Section: Announcement Broadcast
Description: Admins can post announcements visible to all users.
Design Specification:
- Entity: Announcement (id,title,content,publishDate,expiryDate)
- POST /announcements
Sample Implementation:
```java
@Entity
public class Announcement {
    @Id @GeneratedValue private Long id;
    private String title;
    private String content;
    private LocalDateTime publishDate;
    private LocalDateTime expiryDate;
}

@PostMapping("/announcements")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> postAnnouncement(@RequestBody AnnouncementDto dto) {
    service.postAnnouncement(dto);
    return ResponseEntity.ok().build();
}
```

### Section: Notification Quiet Hours
Description: Users can configure quiet hours for notifications.
Design Specification:
- Entity: UserPreferences (userId,quietHoursStart,quietHoursEnd)
- NotificationService checks before sending
Sample Implementation:
```java
public void sendNotification(User user, Notification notification) {
    if (isQuietHours(user)) {
        queueForLater(notification);
    } else {
        send(notification);
    }
}
```

---

## <a name="e13-integration-layer-hriswms-apis"></a>E13: Integration Layer HRIS/WMS APIs

### Section: HRIS Employee Sync API
Description: Sync employee data from HRIS via API.
Design Specification:
- @Scheduled job calls HRIS API
- Create/update/terminate employees
Sample Implementation:
```java
@Scheduled(cron = "0 0 2 * * ?")
public void syncHris() {
    List<HrisEmployee> hrisEmployees = hrisClient.getEmployees();
    for (HrisEmployee hrisEmp : hrisEmployees) {
        Employee emp = repo.findByBadgeId(hrisEmp.getBadgeId()).orElse(new Employee());
        emp.setName(hrisEmp.getName());
        repo.save(emp);
        auditService.log("HRIS_SYNC", emp.getId());
    }
}
```

### Section: WMS Department/Location API
Description: Sync department and location data from WMS.
Design Specification:
- @Scheduled job calls WMS API
Sample Implementation:
```java
@Scheduled(cron = "0 0 3 * * ?")
public void syncWms() {
    List<WmsDepartment> wmsDepts = wmsClient.getDepartments();
    for (WmsDepartment wmsDept : wmsDepts) {
        Department dept = deptRepo.findByCode(wmsDept.getCode()).orElse(new Department());
        dept.setName(wmsDept.getName());
        deptRepo.save(dept);
    }
}
```

### Section: SSO Integration with IDP
Description: Support SSO via SAML or OAuth2.
Design Specification:
- Use Spring Security SAML/OAuth2
Sample Implementation:
```java
@EnableWebSecurity
public class SsoConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .oauth2Login()
            .and()
            .authorizeRequests()
            .anyRequest().authenticated();
    }
}
```

### Section: Webhook Event Publishing
Description: Publish webhooks for key events (employee created, shift assigned).
Design Specification:
- WebhookService sends POST to configured endpoints
Sample Implementation:
```java
public void publishWebhook(String event, Object payload) {
    List<WebhookEndpoint> endpoints = webhookRepo.findByEvent(event);
    for (WebhookEndpoint endpoint : endpoints) {
        restTemplate.postForEntity(endpoint.getUrl(), payload, Void.class);
    }
}
```

---

## <a name="e14-audit-trail--compliance"></a>E14: Audit Trail & Compliance

### Section: Audit Log for Sensitive Changes
Description: Log all sensitive changes (create/update/delete) with before/after values.
Design Specification:
- Entity: AuditLog (id,actor,timestamp,action,entityType,entityId,before,after)
- Service logs changes
Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String action;
    private String entityType;
    private Long entityId;
    @Lob private String before;
    @Lob private String after;
}

public void updateEmployee(Long id, EmployeeDto dto) {
    Employee emp = repo.findById(id).orElseThrow();
    String before = objectMapper.writeValueAsString(emp);
    emp.setName(dto.getName());
    repo.save(emp);
    String after = objectMapper.writeValueAsString(emp);
    auditService.log("UPDATE", "Employee", id, before, after);
}
```

### Section: Tamper-Evident Audit Storage
Description: Use cryptographic hashes or append-only storage for audit logs.
Design Specification:
- Compute hash of each log entry
- Store hash in next entry
Sample Implementation:
```java
public void log(String action, String entityType, Long entityId, String before, String after) {
    AuditLog log = new AuditLog();
    log.setAction(action);
    log.setEntityType(entityType);
    log.setEntityId(entityId);
    log.setBefore(before);
    log.setAfter(after);
    log.setHash(computeHash(log));
    repo.save(log);
}
```

### Section: Audit Log Export
Description: Export audit logs by date, user, or entity.
Design Specification:
- GET /audit/export?from=&to=&user=&entity=
Sample Implementation:
```java
@GetMapping("/audit/export")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<byte[]> exportAuditLogs(@RequestParam Map<String,String> params) {
    byte[] csv = service.exportAuditLogs(params);
    return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv")).body(csv);
}
```

---

## <a name="e15-reporting--analytics"></a>E15: Reporting & Analytics

### Section: Attendance & Overtime Report
Description: Generate attendance and overtime reports.
Design Specification:
- GET /reports/attendance?from=&to=&department=
Sample Implementation:
```java
@GetMapping("/reports/attendance")
@PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
public ResponseEntity<byte[]> attendanceReport(@RequestParam Map<String,String> params) {
    byte[] pdf = service.generateAttendanceReport(params);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf);
}
```

### Section: Certification Status Dashboard
Description: Dashboard of certification statuses.
Design Specification:
- GET /reports/certifications
Sample Implementation:
```java
@GetMapping("/reports/certifications")
@PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
public List<CertificationStatusDto> certificationDashboard() {
    return service.getCertificationStatus();
}
```

### Section: Safety KPI Analytics
Description: Analytics on incidents and near-misses.
Design Specification:
- GET /reports/safety-kpis
Sample Implementation:
```java
@GetMapping("/reports/safety-kpis")
@PreAuthorize("hasAnyRole('ADMIN','SAFETY_OFFICER')")
public SafetyKpiDto safetyKpis() {
    return service.getSafetyKpis();
}
```

### Section: Role-Based Report Access
Description: Restrict report access based on roles.
Design Specification:
- Use @PreAuthorize on report endpoints
Sample Implementation:
```java
@GetMapping("/reports/sensitive")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> sensitiveReport() {
    return ResponseEntity.ok(service.getSensitiveReport());
}
```

---

## <a name="e16-mobile-access-pwa"></a>E16: Mobile Access PWA

### Section: Mobile Clock-In/Out
Description: Allow clock-in/out from mobile app/PWA.
Design Specification:
- POST /attendance/clock-in (same as web)
- Offline queue with sync
Sample Implementation:
```java
// Same as E04, but with PWA manifest and service worker
// manifest.json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone",
  "icons": [...]
}
```

### Section: Mobile Schedule View
Description: View schedule on mobile.
Design Specification:
- GET /shifts/calendar (same as web)
- Responsive design
Sample Implementation:
```java
// Same as E05, but with responsive UI
```

### Section: Mobile Announcements
Description: Receive announcements on mobile.
Design Specification:
- GET /announcements (same as web)
- Push notifications
Sample Implementation:
```java
// Same as E12, but with push notification support
```

### Section: PWA Offline Support
Description: Support offline mode with service worker.
Design Specification:
- Service worker caches API responses
- Queue actions for sync
Sample Implementation:
```javascript
// service-worker.js
self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request).then(response => {
      return response || fetch(event.request);
    })
  );
});
```

---

## <a name="e17-onboarding--offboarding-workflow"></a>E17: Onboarding & Offboarding Workflow

### Section: Automated Onboarding Tasks
Description: Generate onboarding tasks for new hires.
Design Specification:
- Entity: OnboardingTask (id,employee,description,status)
- Triggered by employee creation
Sample Implementation:
```java
public void createEmployee(EmployeeDto dto) {
    Employee emp = new Employee(...);
    repo.save(emp);
    onboardingService.generateTasks(emp);
}

public void generateTasks(Employee emp) {
    List<String> tasks = List.of("Complete training", "Assign equipment", "Set up schedule");
    for (String task : tasks) {
        OnboardingTask t = new OnboardingTask();
        t.setEmployee(emp);
        t.setDescription(task);
        taskRepo.save(t);
    }
}
```

### Section: Automated Offboarding Tasks
Description: Generate offboarding tasks for terminations.
Design Specification:
- Entity: OffboardingTask (id,employee,description,status)
- Triggered by employee termination
Sample Implementation:
```java
public void terminateEmployee(Long id) {
    Employee emp = repo.findById(id).orElseThrow();
    emp.setStatus("TERMINATED");
    repo.save(emp);
    offboardingService.generateTasks(emp);
}

public void generateTasks(Employee emp) {
    List<String> tasks = List.of("Revoke access", "Collect equipment", "Update schedule");
    for (String task : tasks) {
        OffboardingTask t = new OffboardingTask();
        t.setEmployee(emp);
        t.setDescription(task);
        taskRepo.save(t);
    }
}
```

### Section: Onboarding/Offboarding Status Tracking
Description: Track status of onboarding/offboarding tasks.
Design Specification:
- GET /onboarding/status?employeeId=
Sample Implementation:
```java
@GetMapping("/onboarding/status")
@PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
public List<TaskStatusDto> getOnboardingStatus(@RequestParam Long employeeId) {
    return service.getOnboardingStatus(employeeId);
}
```

---

## <a name="e18-localization--multi-warehouse"></a>E18: Localization & Multi-Warehouse

### Section: Localization of UI & Notifications
Description: Support multiple languages (UI, notifications).
Design Specification:
- Use MessageSource for i18n
- User selects language
Sample Implementation:
```java
@Bean
public MessageSource messageSource() {
    ResourceBundleMessageSource source = new ResourceBundleMessageSource();
    source.setBasename("messages");
    return source;
}

// messages_en.properties
greeting=Hello

// messages_es.properties
greeting=Hola
```

### Section: Multi-Warehouse Data Segregation
Description: Segregate data by warehouse.
Design Specification:
- Entity: Warehouse (id,name)
- Employee has ManyToOne Warehouse
- Filter queries by warehouse
Sample Implementation:
```java
@Entity
public class Employee {
    @ManyToOne private Warehouse warehouse;
}

public List<Employee> listEmployees(Long warehouseId) {
    return repo.findByWarehouseId(warehouseId);
}
```

### Section: Warehouse-Specific Configuration
Description: Configure settings per warehouse.
Design Specification:
- Entity: WarehouseConfig (id,warehouse,key,value)
Sample Implementation:
```java
@Entity
public class WarehouseConfig {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Warehouse warehouse;
    private String key;
    private String value;
}
```

---

## <a name="e19-automated-testing--cicd"></a>E19: Automated Testing & CI/CD

### Section: Automated Test Suite
Description: Unit, integration, and API tests with 80%+ coverage.
Design Specification:
- Use JUnit, Mockito, Spring Boot Test
- Run tests in CI
Sample Implementation:
```java
@SpringBootTest
public class EmployeeServiceTest {
    @Autowired private EmployeeService service;
    @MockBean private EmployeeRepository repo;

    @Test
    public void testCreateEmployee() {
        EmployeeDto dto = new EmployeeDto(...);
        when(repo.save(any())).thenReturn(new Employee(...));
        Employee emp = service.createEmployee(dto);
        assertNotNull(emp);
    }
}
```

### Section: CI/CD Pipeline Integration
Description: Automate build, test, and deployment.
Design Specification:
- GitHub Actions or Jenkins
- Deploy to staging/prod
Sample Implementation:
```yaml
name: CI/CD
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build
        run: mvn clean install
      - name: Deploy
        run: ./deploy.sh
```

### Section: Test Data Management
Description: Isolate test data from production.
Design Specification:
- Use separate test DB
- Clean up after tests
Sample Implementation:
```java
@TestConfiguration
public class TestConfig {
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .build();
    }
}
```

---

## <a name="e20-documentation--runbooks"></a>E20: Documentation & Runbooks

### Section: Living Documentation Portal
Description: Auto-generate API docs from code.
Design Specification:
- Use Springdoc OpenAPI
Sample Implementation:
```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("Warehouse EMS API").version("1.0"));
    }
}
```

### Section: Runbook for Incident Response
Description: Provide runbooks for common incidents.
Design Specification:
- Markdown files in docs/runbooks
Sample Implementation:
```markdown
# Runbook: Database Connection Failure
1. Check DB status
2. Restart DB service
3. Verify connection
```

### Section: User-Facing Help Guides
Description: Provide help guides for key workflows.
Design Specification:
- Markdown files in docs/help
Sample Implementation:
```markdown
# How to Clock In
1. Open the app
2. Tap "Clock In"
3. Confirm location
```

---

**END OF TECHNICAL DESIGN DOCUMENT**