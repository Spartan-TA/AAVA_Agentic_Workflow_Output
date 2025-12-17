# Warehouse Employee Management System - Comprehensive Low-Level Technical Design Document

## Document Overview
This document provides comprehensive low-level technical design specifications for all 61 user stories across 20 epics of the Warehouse Employee Management System. Each user story includes detailed Spring Boot architecture, package structure, domain models, repository/service/controller layers, security configurations, integration points, and sample implementations.

---

## EPIC E01: Project Scaffolding & Domain Setup

### User Story 1: Initialize Spring Boot (Maven) Project

**Section: Architecture Overview**

Description: Establish a modular Spring Boot application using Maven with clear separation of concerns for core modules (employee, scheduling, attendance, safety).

Design Specification:
- Spring Boot 3.x with Java 17+
- Maven multi-module project structure
- Core modules: employee, scheduling, attendance, safety
- Spring Boot Actuator for health monitoring
- Flyway/Liquibase for database migrations

Sample Implementation:
```java
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}
```

**Section: Package Structure**

Description: Organize code into feature-based packages for maintainability and scalability.

Design Specification:
- com.company.wem.employee (Employee management)
- com.company.wem.scheduling (Shift and schedule management)
- com.company.wem.attendance (Time tracking)
- com.company.wem.safety (Safety incidents)
- com.company.wem.common (Shared utilities)

Sample Implementation:
```
src/main/java/
  com/company/wem/
    employee/
      entity/
      repository/
      service/
      controller/
      dto/
    scheduling/
    attendance/
    safety/
    common/
```

**Section: Configuration**

Description: Configure application properties and database migration tools.

Design Specification:
- application.properties for environment-specific configs
- Flyway baseline migration scripts
- Server port configuration

Sample Implementation:
```properties
server.port=8080
spring.application.name=warehouse-employee-mgmt
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

---

### User Story 2: Configure Base Packages

**Section: Component Scanning**

Description: Define base packages for Spring component scanning and dependency injection.

Design Specification:
- @SpringBootApplication with scanBasePackages
- Enable auto-configuration

Sample Implementation:
```java
@SpringBootApplication(scanBasePackages = {"com.company.wem"})
public class WarehouseEmployeeMgmtApplication {
    // Application entry point
}
```

---

### User Story 3: Set Up Core Modules

**Section: Maven Module Structure**

Description: Create Maven modules for each core domain to enable modular development.

Design Specification:
- Parent POM with module definitions
- Separate modules for employee, scheduling, attendance, safety

Sample Implementation:
```xml
<modules>
    <module>employee</module>
    <module>scheduling</module>
    <module>attendance</module>
    <module>safety</module>
    <module>common</module>
</modules>
```

---

### User Story 4: Add Flyway/Liquibase for DB Migrations

**Section: Database Migration**

Description: Implement database schema versioning using Flyway for repeatable migrations.

Design Specification:
- Flyway dependency in pom.xml
- Baseline migration script
- Version-controlled SQL scripts

Sample Implementation:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

```sql
-- V1__init.sql
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
```

---

### User Story 5: Enable Actuator

**Section: Monitoring and Health Checks**

Description: Integrate Spring Boot Actuator for application health monitoring and metrics.

Design Specification:
- Actuator dependency
- Health endpoint exposure
- Metrics collection

Sample Implementation:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

---

## EPIC E02: Employee Master Data (CRUD)

### User Story 6: Create Employee Domain

**Section: Domain Model**

Description: Define Employee entity with JPA annotations and required fields.

Design Specification:
- Entity fields: id, name, badgeId, role, department, shiftGroup, hireDate, status
- Unique constraint on badgeId
- Audit fields (createdAt, updatedAt)

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
    
    @Column(name = "badge_id", unique = true, nullable = false)
    private String badgeId;
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    private String department;
    
    @Column(name = "shift_group")
    private String shiftGroup;
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Getters and setters
}

public enum Role {
    ADMIN, HR, SUPERVISOR, WORKER
}

public enum EmployeeStatus {
    ACTIVE, INACTIVE, DELETED
}
```

---

### User Story 7: Employee CRUD APIs

**Section: Controller Layer**

Description: Implement RESTful endpoints for employee CRUD operations with proper HTTP methods.

Design Specification:
- POST /employees - Create employee
- GET /employees - List employees with pagination
- GET /employees/{id} - Get employee by ID
- PUT /employees/{id} - Update employee
- DELETE /employees/{id} - Soft delete employee

Sample Implementation:
```java
@RestController
@RequestMapping("/employees")
@Validated
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeDto create(@Valid @RequestBody EmployeeDto dto) {
        return employeeService.createEmployee(dto);
    }
    
    @GetMapping
    public Page<EmployeeDto> list(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String filter) {
        return employeeService.getEmployees(pageable, filter);
    }
    
    @GetMapping("/{id}")
    public EmployeeDto getById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id);
    }
    
    @PutMapping("/{id}")
    public EmployeeDto update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDto dto) {
        return employeeService.updateEmployee(id, dto);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
    }
}
```

**Section: Service Layer**

Description: Business logic for employee operations with transaction management.

Design Specification:
- @Transactional for data consistency
- Validation logic
- DTO to entity mapping

Sample Implementation:
```java
@Service
@Transactional
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private EmployeeMapper employeeMapper;
    
    public EmployeeDto createEmployee(EmployeeDto dto) {
        if (employeeRepository.findByBadgeId(dto.getBadgeId()).isPresent()) {
            throw new DuplicateBadgeIdException("Badge ID already exists");
        }
        Employee employee = employeeMapper.toEntity(dto);
        employee.setStatus(EmployeeStatus.ACTIVE);
        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toDto(saved);
    }
    
    @Transactional(readOnly = true)
    public Page<EmployeeDto> getEmployees(Pageable pageable, String filter) {
        Page<Employee> employees;
        if (filter != null && !filter.isEmpty()) {
            employees = employeeRepository.findByNameContainingOrDepartmentContaining(
                    filter, filter, pageable);
        } else {
            employees = employeeRepository.findAllByStatusNot(
                    EmployeeStatus.DELETED, pageable);
        }
        return employees.map(employeeMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return employeeMapper.toDto(employee);
    }
    
    public EmployeeDto updateEmployee(Long id, EmployeeDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        employeeMapper.updateEntity(dto, employee);
        Employee updated = employeeRepository.save(employee);
        return employeeMapper.toDto(updated);
    }
    
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        employee.setStatus(EmployeeStatus.DELETED);
        employeeRepository.save(employee);
    }
}
```

**Section: Repository Layer**

Description: Data access layer using Spring Data JPA.

Design Specification:
- Extend JpaRepository
- Custom query methods
- Pagination support

Sample Implementation:
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByBadgeId(String badgeId);
    
    Page<Employee> findAllByStatusNot(EmployeeStatus status, Pageable pageable);
    
    Page<Employee> findByNameContainingOrDepartmentContaining(
            String name, String department, Pageable pageable);
    
    @Query("SELECT e FROM Employee e WHERE e.status <> 'DELETED'")
    List<Employee> findActiveEmployees();
}
```

---

### User Story 8: Unique badgeId Enforcement

**Section: Validation**

Description: Ensure badgeId uniqueness at database and application level.

Design Specification:
- Database unique constraint
- Application-level validation
- Custom exception handling

Sample Implementation:
```java
@Column(name = "badge_id", unique = true, nullable = false)
private String badgeId;

// In service
if (employeeRepository.findByBadgeId(dto.getBadgeId()).isPresent()) {
    throw new DuplicateBadgeIdException("Badge ID " + dto.getBadgeId() + " already exists");
}

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateBadgeIdException extends RuntimeException {
    public DuplicateBadgeIdException(String message) {
        super(message);
    }
}
```

---

### User Story 9: Soft-Delete Support

**Section: Soft Delete Implementation**

Description: Implement soft-delete using status field to preserve historical records.

Design Specification:
- Status field (ACTIVE, INACTIVE, DELETED)
- Filter deleted records in queries
- Audit trail preservation

Sample Implementation:
```java
public void softDeleteEmployee(Long id) {
    Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
    employee.setStatus(EmployeeStatus.DELETED);
    employeeRepository.save(employee);
}

@Query("SELECT e FROM Employee e WHERE e.status <> 'DELETED'")
List<Employee> findActiveEmployees();
```

---

### User Story 10: OpenAPI Schemas

**Section: API Documentation**

Description: Document APIs using OpenAPI/Swagger for discoverability.

Design Specification:
- springdoc-openapi dependency
- @Schema annotations on DTOs
- @Operation annotations on endpoints

Sample Implementation:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.0.0</version>
</dependency>
```

```java
@Schema(description = "Employee Data Transfer Object")
public class EmployeeDto {
    @Schema(description = "Employee ID", example = "1")
    private Long id;
    
    @Schema(description = "Employee name", example = "John Doe", required = true)
    @NotBlank
    private String name;
    
    @Schema(description = "Unique badge ID", example = "EMP001", required = true)
    @NotBlank
    private String badgeId;
}

@Operation(summary = "Create employee", description = "Creates a new employee record")
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "Employee created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input"),
    @ApiResponse(responseCode = "409", description = "Badge ID already exists")
})
@PostMapping
public EmployeeDto create(@Valid @RequestBody EmployeeDto dto) {
    return employeeService.createEmployee(dto);
}
```

---

## EPIC E03: Role-Based Access Control (RBAC)

### User Story 11: Add Spring Security with Roles

**Section: Security Configuration**

Description: Configure Spring Security with role-based access control.

Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Method-level security
- JWT or OAuth2 authentication

Sample Implementation:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/employees/**").hasAnyRole("ADMIN", "HR")
                .requestMatchers("/attendance/**").hasAnyRole("SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());
        return http.build();
    }
}
```

---

### User Story 12: Method/Endpoint Security

**Section: Method-Level Security**

Description: Restrict service methods by role using @PreAuthorize.

Design Specification:
- @PreAuthorize annotations
- Role-based method access

Sample Implementation:
```java
@Service
public class EmployeeService {
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public EmployeeDto createEmployee(EmployeeDto dto) {
        // Implementation
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteEmployee(Long id) {
        // Implementation
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public Page<EmployeeDto> getEmployees(Pageable pageable) {
        // Implementation
    }
}
```

---

### User Story 13: API Key/OAuth2 Toggle

**Section: Authentication Configuration**

Description: Support both API key and OAuth2 authentication via configuration.

Design Specification:
- Conditional security beans
- Configuration properties

Sample Implementation:
```java
@Configuration
public class AuthConfig {
    
    @Value("${auth.type}")
    private String authType;
    
    @Bean
    @ConditionalOnProperty(name = "auth.type", havingValue = "oauth2")
    public SecurityFilterChain oauth2FilterChain(HttpSecurity http) throws Exception {
        // OAuth2 configuration
        return http.build();
    }
    
    @Bean
    @ConditionalOnProperty(name = "auth.type", havingValue = "apikey")
    public SecurityFilterChain apiKeyFilterChain(HttpSecurity http) throws Exception {
        // API Key configuration
        return http.build();
    }
}
```

---

## EPIC E04: Time & Attendance (Clock In/Out)

### User Story 14: Clock-In/Out Endpoints

**Section: Attendance Domain Model**

Description: Model attendance events with clock-in/out timestamps.

Design Specification:
- AttendanceEvent entity
- Employee relationship
- Timestamp tracking

Sample Implementation:
```java
@Entity
@Table(name = "attendance_event")
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Column(name = "clock_in_time", nullable = false)
    private LocalDateTime clockInTime;
    
    @Column(name = "clock_out_time")
    private LocalDateTime clockOutTime;
    
    private Double latitude;
    private Double longitude;
    
    @Column(name = "device_id")
    private String deviceId;
    
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;
    
    // Getters and setters
}

public enum AttendanceStatus {
    CLOCKED_IN, CLOCKED_OUT, PENDING_APPROVAL, APPROVED, CORRECTED
}
```

**Section: Controller Layer**

Description: REST endpoints for clock-in/out operations.

Design Specification:
- POST /attendance/clock-in
- POST /attendance/clock-out
- GET /attendance/my-records

Sample Implementation:
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    
    @Autowired
    private AttendanceService attendanceService;
    
    @PostMapping("/clock-in")
    @PreAuthorize("hasAnyRole('WORKER', 'SUPERVISOR')")
    public AttendanceEventDto clockIn(@Valid @RequestBody ClockInRequest request) {
        return attendanceService.clockIn(request);
    }
    
    @PostMapping("/clock-out")
    @PreAuthorize("hasAnyRole('WORKER', 'SUPERVISOR')")
    public AttendanceEventDto clockOut(@Valid @RequestBody ClockOutRequest request) {
        return attendanceService.clockOut(request);
    }
    
    @GetMapping("/my-records")
    @PreAuthorize("hasAnyRole('WORKER', 'SUPERVISOR')")
    public Page<AttendanceEventDto> getMyRecords(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails user) {
        return attendanceService.getEmployeeRecords(user.getUsername(), pageable);
    }
}
```

---

### User Story 15: Geofence & Device Capture

**Section: Geolocation Validation**

Description: Capture and validate geolocation for clock events.

Design Specification:
- Latitude/longitude fields
- Device ID tracking
- Optional geofence validation

Sample Implementation:
```java
public class ClockInRequest {
    @NotNull
    private Long employeeId;
    
    private Double latitude;
    private Double longitude;
    
    @NotBlank
    private String deviceId;
}

@Service
public class AttendanceService {
    
    @Value("${geofence.enabled}")
    private boolean geofenceEnabled;
    
    @Value("${geofence.radius}")
    private double geofenceRadius;
    
    public AttendanceEventDto clockIn(ClockInRequest request) {
        if (geofenceEnabled && !isWithinGeofence(request.getLatitude(), request.getLongitude())) {
            throw new GeofenceViolationException("Clock-in location outside allowed area");
        }
        // Process clock-in
    }
    
    private boolean isWithinGeofence(Double lat, Double lon) {
        // Geofence validation logic
        return true;
    }
}
```

---

### User Story 16: Missed Punches & Corrections Workflow

**Section: Correction Request Model**

Description: Handle missed punches with approval workflow.

Design Specification:
- CorrectionRequest entity
- Approval workflow
- Supervisor review

Sample Implementation:
```java
@Entity
@Table(name = "correction_request")
public class CorrectionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    @Column(name = "missed_time")
    private LocalDateTime missedTime;
    
    private String reason;
    
    @Enumerated(EnumType.STRING)
    private CorrectionStatus status;
    
    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Employee approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}

public enum CorrectionStatus {
    PENDING, APPROVED, DENIED
}
```

---

## EPIC E05: Shift & Schedule Management

### User Story 17: Shift Templates & Rotations

**Section: Shift Template Model**

Description: Define reusable shift templates with recurrence patterns.

Design Specification:
- ShiftTemplate entity
- Start/end times
- Recurrence rules

Sample Implementation:
```java
@Entity
@Table(name = "shift_template")
public class ShiftTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Enumerated(EnumType.STRING)
    private RecurrenceType recurrence;
    
    private String description;
}

public enum RecurrenceType {
    DAILY, WEEKLY, BIWEEKLY, MONTHLY
}
```

---

### User Story 18: Overtime Rules

**Section: Overtime Configuration**

Description: Model overtime rules and calculations.

Design Specification:
- OvertimeRule entity
- Max hours threshold
- Rate multiplier

Sample Implementation:
```java
@Entity
@Table(name = "overtime_rule")
public class OvertimeRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "max_regular_hours")
    private Integer maxRegularHours;
    
    @Column(name = "overtime_rate")
    private BigDecimal overtimeRate;
    
    @Column(name = "double_time_threshold")
    private Integer doubleTimeThreshold;
    
    @Column(name = "double_time_rate")
    private BigDecimal doubleTimeRate;
}
```

---

### User Story 19: Blackout Dates & Operation Calendars

**Section: Calendar Management**

Description: Store blackout dates and operation calendars.

Design Specification:
- BlackoutDate entity
- Holiday tracking
- Operational calendar

Sample Implementation:
```java
@Entity
@Table(name = "blackout_date")
public class BlackoutDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private String reason;
    
    @Enumerated(EnumType.STRING)
    private BlackoutType type;
}

public enum BlackoutType {
    HOLIDAY, MAINTENANCE, EMERGENCY
}
```

---

## EPIC E06: Leave & Absence Management

### User Story 20: PTO, Sick, Unpaid Leave Request

**Section: Leave Request Model**

Description: Model leave requests with approval workflow.

Design Specification:
- LeaveRequest entity
- Leave types (PTO, SICK, UNPAID)
- Approval status

Sample Implementation:
```java
@Entity
@Table(name = "leave_request")
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type")
    private LeaveType leaveType;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    private String reason;
    
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;
    
    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Employee approvedBy;
}

public enum LeaveType {
    PTO, SICK, UNPAID, BEREAVEMENT, JURY_DUTY
}

public enum LeaveStatus {
    REQUESTED, APPROVED, DENIED, CANCELLED
}
```

---

### User Story 21: Accrual Balances & Policies

**Section: Leave Balance Tracking**

Description: Track leave accrual balances and apply policies.

Design Specification:
- AccrualBalance entity
- Policy rules
- Balance calculations

Sample Implementation:
```java
@Entity
@Table(name = "accrual_balance")
public class AccrualBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type")
    private LeaveType leaveType;
    
    @Column(name = "balance_hours")
    private BigDecimal balanceHours;
    
    @Column(name = "accrual_rate")
    private BigDecimal accrualRate;
    
    @Column(name = "max_balance")
    private BigDecimal maxBalance;
}
```

---

### User Story 22: Integration Hooks for Scheduling/Payroll

**Section: Event-Driven Integration**

Description: Integrate leave approvals with scheduling and payroll systems.

Design Specification:
- Spring Events
- Event listeners
- Async processing

Sample Implementation:
```java
public class LeaveApprovedEvent extends ApplicationEvent {
    private final LeaveRequest leaveRequest;
    
    public LeaveApprovedEvent(Object source, LeaveRequest leaveRequest) {
        super(source);
        this.leaveRequest = leaveRequest;
    }
}

@Component
public class LeaveEventListener {
    
    @Autowired
    private SchedulingService schedulingService;
    
    @Autowired
    private PayrollService payrollService;
    
    @EventListener
    @Async
    public void handleLeaveApproved(LeaveApprovedEvent event) {
        LeaveRequest leave = event.getLeaveRequest();
        schedulingService.excludeFromSchedule(leave);
        payrollService.adjustPayroll(leave);
    }
}
```

---

## EPIC E07: Training & Certification Tracking

### User Story 23: Certification CRUD

**Section: Certification Model**

Description: Track employee certifications with expiry dates.

Design Specification:
- Certification entity
- Expiry tracking
- Document storage

Sample Implementation:
```java
@Entity
@Table(name = "certification")
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    @Column(name = "certification_type")
    private String certificationType;
    
    @Column(name = "issue_date")
    private LocalDate issueDate;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    @Column(name = "document_url")
    private String documentUrl;
    
    @Enumerated(EnumType.STRING)
    private CertificationStatus status;
}

public enum CertificationStatus {
    ACTIVE, EXPIRED, PENDING_RENEWAL
}
```

---

### User Story 24: Expiry Alerts

**Section: Scheduled Notifications**

Description: Send alerts before certification expiry.

Design Specification:
- Scheduled job
- Notification service
- Alert thresholds (30/7 days)

Sample Implementation:
```java
@Component
public class CertificationExpiryScheduler {
    
    @Autowired
    private CertificationRepository certificationRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Scheduled(cron = "0 0 8 * * *") // Daily at 8 AM
    public void checkExpiringCertifications() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysOut = today.plusDays(30);
        LocalDate sevenDaysOut = today.plusDays(7);
        
        List<Certification> expiringSoon = certificationRepository
                .findByExpiryDateBetween(today, thirtyDaysOut);
        
        for (Certification cert : expiringSoon) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(today, cert.getExpiryDate());
            if (daysUntilExpiry == 30 || daysUntilExpiry == 7) {
                notificationService.sendExpiryAlert(cert);
            }
        }
    }
}
```

---

### User Story 25: Block Assignment for Expired Certs

**Section: Validation Logic**

Description: Prevent task/equipment assignment without valid certification.

Design Specification:
- Pre-assignment validation
- Certification check
- Exception handling

Sample Implementation:
```java
@Service
public class SchedulingService {
    
    @Autowired
    private CertificationRepository certificationRepository;
    
    public void assignShift(Long employeeId, ShiftAssignment assignment) {
        if (assignment.requiresCertification()) {
            validateCertification(employeeId, assignment.getRequiredCertificationType());
        }
        // Proceed with assignment
    }
    
    private void validateCertification(Long employeeId, String certType) {
        Optional<Certification> cert = certificationRepository
                .findByEmployeeIdAndCertificationTypeAndStatus(
                        employeeId, certType, CertificationStatus.ACTIVE);
        
        if (cert.isEmpty() || cert.get().getExpiryDate().isBefore(LocalDate.now())) {
            throw new InvalidCertificationException(
                    "Employee does not have valid " + certType + " certification");
        }
    }
}
```

---

## EPIC E08: Safety Incidents & OSHA Reporting

### User Story 26: Incident Recording

**Section: Safety Incident Model**

Description: Record safety incidents with detailed information.

Design Specification:
- SafetyIncident entity
- Severity levels
- Investigation workflow

Sample Implementation:
```java
@Entity
@Table(name = "safety_incident")
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "incident_date")
    private LocalDateTime incidentDate;
    
    @Enumerated(EnumType.STRING)
    private IncidentSeverity severity;
    
    private String location;
    
    @Column(length = 2000)
    private String description;
    
    @ManyToMany
    @JoinTable(
        name = "incident_employees",
        joinColumns = @JoinColumn(name = "incident_id"),
        inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private List<Employee> involvedEmployees;
    
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
    
    @Column(name = "corrective_action")
    private String correctiveAction;
}

public enum IncidentSeverity {
    MINOR, MODERATE, SERIOUS, CRITICAL
}

public enum IncidentStatus {
    OPEN, INVESTIGATING, RESOLVED, CLOSED
}
```

---

### User Story 27: OSHA Summary Generation

**Section: Reporting Service**

Description: Generate OSHA 300/300A summary reports.

Design Specification:
- Report generation service
- OSHA format compliance
- CSV export

Sample Implementation:
```java
@Service
public class OshaReportingService {
    
    @Autowired
    private SafetyIncidentRepository incidentRepository;
    
    public File generateOsha300Report(int year) {
        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59);
        
        List<SafetyIncident> incidents = incidentRepository
                .findByIncidentDateBetween(startDate, endDate);
        
        return exportToOshaFormat(incidents);
    }
    
    private File exportToOshaFormat(List<SafetyIncident> incidents) {
        // Generate OSHA 300 format CSV
        // Include: Case number, Employee name, Job title, Date of injury,
        // Where event occurred, Description, Classification
        return null; // Implementation details
    }
}
```

---

## EPIC E09: Equipment & Asset Assignment

### User Story 28: Asset Registry CRUD

**Section: Asset Model**

Description: Track equipment and assets with assignment history.

Design Specification:
- Asset entity
- Asset types
- Condition tracking

Sample Implementation:
```java
@Entity
@Table(name = "asset")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type")
    private AssetType assetType;
    
    @Column(name = "serial_number", unique = true)
    private String serialNumber;
    
    @Enumerated(EnumType.STRING)
    private AssetCondition condition;
    
    @ManyToOne
    @JoinColumn(name = "assigned_employee_id")
    private Employee assignedEmployee;
    
    @Column(name = "assignment_date")
    private LocalDateTime assignmentDate;
}

public enum AssetType {
    SCANNER, FORKLIFT, PPE, TABLET, RADIO
}

public enum AssetCondition {
    NEW, GOOD, FAIR, POOR, DAMAGED
}
```

---

### User Story 29: Check-In/Out Endpoints

**Section: Asset Management Controller**

Description: Endpoints for asset check-in/out operations.

Design Specification:
- POST /assets/check-in
- POST /assets/check-out
- Asset history tracking

Sample Implementation:
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    
    @Autowired
    private AssetService assetService;
    
    @PostMapping("/check-out")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN')")
    public AssetDto checkOut(@Valid @RequestBody AssetCheckOutRequest request) {
        return assetService.checkOutAsset(request);
    }
    
    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN', 'WORKER')")
    public AssetDto checkIn(@Valid @RequestBody AssetCheckInRequest request) {
        return assetService.checkInAsset(request);
    }
}
```

---

### User Story 30: Certification Validation for Asset Use

**Section: Asset Assignment Validation**

Description: Validate certification before asset assignment.

Design Specification:
- Pre-assignment checks
- Certification validation
- Exception handling

Sample Implementation:
```java
@Service
public class AssetService {
    
    @Autowired
    private CertificationRepository certificationRepository;
    
    public AssetDto checkOutAsset(AssetCheckOutRequest request) {
        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new AssetNotFoundException(request.getAssetId()));
        
        if (asset.getAssetType().requiresCertification()) {
            validateEmployeeCertification(
                    request.getEmployeeId(),
                    asset.getAssetType().getRequiredCertification());
        }
        
        asset.setAssignedEmployee(employeeRepository.getReferenceById(request.getEmployeeId()));
        asset.setAssignmentDate(LocalDateTime.now());
        
        Asset saved = assetRepository.save(asset);
        return assetMapper.toDto(saved);
    }
    
    private void validateEmployeeCertification(Long employeeId, String certType) {
        Optional<Certification> cert = certificationRepository
                .findValidCertification(employeeId, certType, LocalDate.now());
        
        if (cert.isEmpty()) {
            throw new AssetAssignmentException(
                    "Employee lacks required certification: " + certType);
        }
    }
}
```

---

### User Story 31: Asset History Log

**Section: Asset History Tracking**

Description: Maintain complete history of asset assignments.

Design Specification:
- AssetHistory entity
- Audit trail
- History queries

Sample Implementation:
```java
@Entity
@Table(name = "asset_history")
public class AssetHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "asset_id")
    private Asset asset;
    
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    private AssetAction action;
    
    private LocalDateTime timestamp;
    
    private String notes;
}

public enum AssetAction {
    CHECK_OUT, CHECK_IN, MAINTENANCE, REPAIR, RETIRE
}
```

---

## EPIC E10: Performance Reviews & Goals

### User Story 32: Review Templates

**Section: Review Template Model**

Description: Create reusable performance review templates.

Design Specification:
- ReviewTemplate entity
- Competency definitions
- Review periods

Sample Implementation:
```java
@Entity
@Table(name = "review_template")
public class ReviewTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @ElementCollection
    @CollectionTable(name = "template_competencies")
    @Column(name = "competency")
    private List<String> competencies;
    
    @Enumerated(EnumType.STRING)
    private ReviewPeriod period;
    
    @Column(length = 1000)
    private String description;
}

public enum ReviewPeriod {
    QUARTERLY, SEMI_ANNUAL, ANNUAL
}
```

---

### User Story 33: Assign Reviews to Employees

**Section: Review Assignment**

Description: Assign review cycles to employees.

Design Specification:
- ReviewAssignment entity
- Status workflow
- Due dates

Sample Implementation:
```java
@Entity
@Table(name = "review_assignment")
public class ReviewAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    @ManyToOne
    @JoinColumn(name = "template_id")
    private ReviewTemplate template;
    
    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private Employee reviewer;
    
    @Enumerated(EnumType.STRING)
    private ReviewStatus status;
    
    @Column(name = "due_date")
    private LocalDate dueDate;
    
    @Column(name = "completed_date")
    private LocalDateTime completedDate;
}

public enum ReviewStatus {
    ASSIGNED, IN_PROGRESS, SUBMITTED, ACKNOWLEDGED, CLOSED
}
```

---

### User Story 34: Submit/Acknowledge Workflow

**Section: Review Workflow**

Description: Implement submission and acknowledgement workflow.

Design Specification:
- Status transitions
- Approval flow
- Notifications

Sample Implementation:
```java
@Service
public class ReviewService {
    
    @Autowired
    private ReviewAssignmentRepository reviewRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Transactional
    public ReviewAssignmentDto submitReview(Long reviewId, ReviewSubmissionDto submission) {
        ReviewAssignment review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        
        if (review.getStatus() != ReviewStatus.IN_PROGRESS) {
            throw new InvalidReviewStateException("Review must be in progress to submit");
        }
        
        review.setStatus(ReviewStatus.SUBMITTED);
        review.setCompletedDate(LocalDateTime.now());
        
        ReviewAssignment saved = reviewRepository.save(review);
        notificationService.notifyReviewSubmitted(saved);
        
        return reviewMapper.toDto(saved);
    }
    
    @Transactional
    public ReviewAssignmentDto acknowledgeReview(Long reviewId) {
        ReviewAssignment review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        
        if (review.getStatus() != ReviewStatus.SUBMITTED) {
            throw new InvalidReviewStateException("Review must be submitted to acknowledge");
        }
        
        review.setStatus(ReviewStatus.ACKNOWLEDGED);
        return reviewMapper.toDto(reviewRepository.save(review));
    }
}
```

---

### User Story 35: PDF Export & Immutable History

**Section: Report Generation**

Description: Export reviews to PDF and maintain immutable history.

Design Specification:
- PDF generation service
- Version control
- Immutable storage

Sample Implementation:
```java
@Service
public class ReviewReportService {
    
    @Autowired
    private ReviewAssignmentRepository reviewRepository;
    
    public byte[] exportReviewPdf(Long reviewId) {
        ReviewAssignment review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        
        // Generate PDF using library like iText or Apache PDFBox
        return generatePdf(review);
    }
    
    private byte[] generatePdf(ReviewAssignment review) {
        // PDF generation logic
        return new byte[0];
    }
}

@Entity
@Table(name = "review_history")
public class ReviewHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "review_id")
    private ReviewAssignment review;
    
    @Column(name = "version_number")
    private Integer versionNumber;
    
    @Column(length = 5000)
    private String snapshot;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
```

---

## EPIC E11: Payroll Export Integration

### User Story 36: Payroll-Ready File Generation

**Section: Payroll Export Service**

Description: Generate payroll files from attendance and leave data.

Design Specification:
- Data aggregation
- Format mapping
- Export generation

Sample Implementation:
```java
@Service
public class PayrollExportService {
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private LeaveRequestRepository leaveRepository;
    
    public File generatePayrollExport(PayrollExportRequest request) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        
        List<AttendanceEvent> attendance = attendanceRepository
                .findByClockInTimeBetween(
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59));
        
        List<LeaveRequest> approvedLeave = leaveRepository
                .findApprovedLeaveInPeriod(startDate, endDate);
        
        return generateExportFile(attendance, approvedLeave, request.getFormat());
    }
    
    private File generateExportFile(
            List<AttendanceEvent> attendance,
            List<LeaveRequest> leave,
            PayrollFormat format) {
        // Generate file in specified format
        return null;
    }
}
```

---

### User Story 37: Secure Delivery (SFTP/API)

**Section: Secure File Transfer**

Description: Deliver payroll files securely via SFTP or API.

Design Specification:
- SFTP client integration
- API endpoint for providers
- Encryption

Sample Implementation:
```java
@Service
public class PayrollDeliveryService {
    
    @Value("${payroll.delivery.method}")
    private String deliveryMethod;
    
    @Autowired
    private SftpClient sftpClient;
    
    public void deliverPayrollFile(File file) {
        if ("sftp".equals(deliveryMethod)) {
            deliverViaSftp(file);
        } else if ("api".equals(deliveryMethod)) {
            deliverViaApi(file);
        }
    }
    
    private void deliverViaSftp(File file) {
        try {
            sftpClient.upload(file, "/payroll/incoming/");
        } catch (Exception e) {
            throw new PayrollDeliveryException("SFTP upload failed", e);
        }
    }
    
    private void deliverViaApi(File file) {
        // API delivery implementation
    }
}
```

---

### User Story 38: Audit Log for Exports

**Section: Export Audit Trail**

Description: Log every payroll export event for compliance.

Design Specification:
- PayrollExportLog entity
- Audit details
- Status tracking

Sample Implementation:
```java
@Entity
@Table(name = "payroll_export_log")
public class PayrollExportLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "export_date")
    private LocalDateTime exportDate;
    
    @Column(name = "period_start")
    private LocalDate periodStart;
    
    @Column(name = "period_end")
    private LocalDate periodEnd;
    
    @Column(name = "record_count")
    private Integer recordCount;
    
    @Enumerated(EnumType.STRING)
    private ExportStatus status;
    
    @Column(name = "file_name")
    private String fileName;
    
    @Column(length = 1000)
    private String details;
}

public enum ExportStatus {
    GENERATED, DELIVERED, FAILED, RETRYING
}
```

---

## EPIC E12: Notifications & Announcements

### User Story 39: In-App & Email/SMS Notifications

**Section: Notification System**

Description: Multi-channel notification system for events.

Design Specification:
- Notification entity
- Multiple channels (EMAIL, SMS, IN_APP)
- Template system

Sample Implementation:
```java
@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private NotificationType notificationType;
    
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;
    
    @Column(length = 1000)
    private String message;
    
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}

public enum NotificationType {
    SHIFT_CHANGE, CERT_EXPIRY, LEAVE_APPROVAL, ANNOUNCEMENT
}

public enum NotificationChannel {
    EMAIL, SMS, IN_APP, PUSH
}

public enum NotificationStatus {
    PENDING, SENT, FAILED, READ
}
```

---

### User Story 40: Quiet Hours Configuration

**Section: Notification Scheduling**

Description: Configure quiet hours to prevent notifications during off-hours.

Design Specification:
- Configuration properties
- Time-based filtering
- Delayed delivery

Sample Implementation:
```properties
notifications.quietHoursStart=22:00
notifications.quietHoursEnd=06:00
notifications.timezone=America/New_York
```

```java
@Service
public class NotificationService {
    
    @Value("${notifications.quietHoursStart}")
    private LocalTime quietHoursStart;
    
    @Value("${notifications.quietHoursEnd}")
    private LocalTime quietHoursEnd;
    
    public void sendNotification(Notification notification) {
        if (isQuietHours()) {
            scheduleForLater(notification);
        } else {
            sendImmediately(notification);
        }
    }
    
    private boolean isQuietHours() {
        LocalTime now = LocalTime.now();
        return now.isAfter(quietHoursStart) || now.isBefore(quietHoursEnd);
    }
}
```

---

### User Story 41: Opt-In/Out Per Channel

**Section: Notification Preferences**

Description: Allow users to manage notification preferences per channel.

Design Specification:
- NotificationPreference entity
- User preferences
- Channel-specific settings

Sample Implementation:
```java
@Entity
@Table(name = "notification_preference")
public class NotificationPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private NotificationType notificationType;
    
    private Boolean enabled;
}
```

---

### User Story 42: Announcements Dashboard

**Section: Announcement System**

Description: Display company announcements on dashboard.

Design Specification:
- Announcement entity
- Priority levels
- Expiry dates

Sample Implementation:
```java
@Entity
@Table(name = "announcement")
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 2000)
    private String content;
    
    @Enumerated(EnumType.STRING)
    private AnnouncementPriority priority;
    
    @Column(name = "publish_date")
    private LocalDateTime publishDate;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @ManyToOne
    @JoinColumn(name = "created_by")
    private Employee createdBy;
}

public enum AnnouncementPriority {
    LOW, MEDIUM, HIGH, URGENT
}

@RestController
@RequestMapping("/announcements")
public class AnnouncementController {
    
    @GetMapping
    public List<AnnouncementDto> getActiveAnnouncements() {
        return announcementService.getActiveAnnouncements();
    }
}
```

---

## EPIC E13: Integration Layer (HRIS/WMS APIs)

### User Story 43: HRIS Sync Job

**Section: HRIS Integration**

Description: Scheduled job to sync employee data from external HRIS.

Design Specification:
- Scheduled task
- REST client
- Data mapping

Sample Implementation:
```java
@Component
public class HrisIntegrationScheduler {
    
    @Autowired
    private HrisClient hrisClient;
    
    @Autowired
    private EmployeeService employeeService;
    
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void syncEmployeesFromHris() {
        try {
            List<HrisEmployeeDto> hrisEmployees = hrisClient.fetchEmployees();
            
            for (HrisEmployeeDto hrisEmp : hrisEmployees) {
                syncEmployee(hrisEmp);
            }
        } catch (Exception e) {
            log.error("HRIS sync failed", e);
        }
    }
    
    private void syncEmployee(HrisEmployeeDto hrisEmp) {
        Optional<Employee> existing = employeeRepository
                .findByBadgeId(hrisEmp.getBadgeId());
        
        if (existing.isPresent()) {
            updateEmployee(existing.get(), hrisEmp);
        } else {
            createEmployee(hrisEmp);
        }
    }
}
```

---

### User Story 44: WMS Link for Department/Location

**Section: WMS Integration**

Description: Integrate with Warehouse Management System for department/location data.

Design Specification:
- REST client
- Data synchronization
- Caching

Sample Implementation:
```java
@Service
public class WmsIntegrationService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${wms.api.url}")
    private String wmsApiUrl;
    
    @Cacheable("departments")
    public Department fetchDepartmentFromWms(String departmentId) {
        String url = wmsApiUrl + "/departments/" + departmentId;
        return restTemplate.getForObject(url, Department.class);
    }
    
    @Cacheable("locations")
    public Location fetchLocationFromWms(String locationId) {
        String url = wmsApiUrl + "/locations/" + locationId;
        return restTemplate.getForObject(url, Location.class);
    }
}
```

---

### User Story 45: SSO via IDP

**Section: Single Sign-On**

Description: Integrate SSO via Identity Provider using OAuth2/SAML.

Design Specification:
- OAuth2 client configuration
- JWT token validation
- User mapping

Sample Implementation:
```java
@Configuration
@EnableOAuth2Sso
public class SsoConfig {
    
    @Bean
    public SecurityFilterChain ssoFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/oauth2/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService())
                )
            );
        return http.build();
    }
    
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService() {
        return new CustomOAuth2UserService();
    }
}
```

---

### User Story 46: Webhooks for Events

**Section: Webhook System**

Description: Expose webhooks for external systems to subscribe to events.

Design Specification:
- Webhook registration
- Event publishing
- Retry mechanism

Sample Implementation:
```java
@Entity
@Table(name = "webhook_subscription")
public class WebhookSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "callback_url")
    private String callbackUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private WebhookEventType eventType;
    
    private Boolean active;
    
    @Column(name = "secret_key")
    private String secretKey;
}

public enum WebhookEventType {
    EMPLOYEE_CREATED, EMPLOYEE_UPDATED, SHIFT_ASSIGNED, LEAVE_APPROVED
}

@Service
public class WebhookService {
    
    @Autowired
    private WebhookSubscriptionRepository subscriptionRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Async
    public void publishEvent(WebhookEventType eventType, Object payload) {
        List<WebhookSubscription> subscriptions = subscriptionRepository
                .findByEventTypeAndActiveTrue(eventType);
        
        for (WebhookSubscription sub : subscriptions) {
            sendWebhook(sub, payload);
        }
    }
    
    private void sendWebhook(WebhookSubscription subscription, Object payload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Webhook-Signature", generateSignature(payload, subscription.getSecretKey()));
            
            HttpEntity<Object> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(subscription.getCallbackUrl(), request, String.class);
        } catch (Exception e) {
            log.error("Webhook delivery failed", e);
            // Implement retry logic
        }
    }
}
```

---

## EPIC E14: Audit Trail & Compliance

### User Story 47: Centralized Audit Logging

**Section: Audit System**

Description: Comprehensive audit logging for all sensitive operations.

Design Specification:
- AuditLog entity
- Before/after snapshots
- Actor tracking

Sample Implementation:
```java
@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String actor;
    
    @Column(nullable = false)
    private String action;
    
    @Column(name = "entity_type")
    private String entityType;
    
    @Column(name = "entity_id")
    private Long entityId;
    
    @Column(length = 5000)
    private String beforeState;
    
    @Column(length = 5000)
    private String afterState;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "ip_address")
    private String ipAddress;
}

@Aspect
@Component
public class AuditAspect {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Around("@annotation(Audited)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String actor = SecurityContextHolder.getContext().getAuthentication().getName();
        Object[] args = joinPoint.getArgs();
        
        Object before = captureState(args);
        Object result = joinPoint.proceed();
        Object after = captureState(result);
        
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setAction(joinPoint.getSignature().getName());
        log.setBeforeState(toJson(before));
        log.setAfterState(toJson(after));
        log.setTimestamp(LocalDateTime.now());
        
        auditLogRepository.save(log);
        
        return result;
    }
}
```

---

### User Story 48: Tamper-Evident Storage

**Section: Immutable Audit Trail**

Description: Ensure audit logs are tamper-evident using hash verification.

Design Specification:
- Hash chain
- Append-only storage
- Verification mechanism

Sample Implementation:
```java
@Service
public class TamperEvidentAuditService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    public void appendAuditLog(AuditLog log) {
        AuditLog previousLog = auditLogRepository.findTopByOrderByIdDesc();
        
        String previousHash = previousLog != null ? previousLog.getHash() : "0";
        String currentHash = computeHash(log, previousHash);
        
        log.setHash(currentHash);
        log.setPreviousHash(previousHash);
        
        auditLogRepository.save(log);
    }
    
    private String computeHash(AuditLog log, String previousHash) {
        String data = log.getActor() + log.getAction() + 
                     log.getTimestamp() + previousHash;
        return DigestUtils.sha256Hex(data);
    }
    
    public boolean verifyIntegrity() {
        List<AuditLog> logs = auditLogRepository.findAllByOrderByIdAsc();
        
        for (int i = 1; i < logs.size(); i++) {
            AuditLog current = logs.get(i);
            AuditLog previous = logs.get(i - 1);
            
            String expectedHash = computeHash(current, previous.getHash());
            if (!expectedHash.equals(current.getHash())) {
                return false;
            }
        }
        return true;
    }
}
```

---

## EPIC E15: Reporting & Analytics

### User Story 49: Operational Reports

**Section: Report Generation**

Description: Generate operational reports for various metrics.

Design Specification:
- Report service
- Multiple formats (CSV, PDF)
- Scheduled reports

Sample Implementation:
```java
@Service
public class ReportingService {
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private LeaveRequestRepository leaveRepository;
    
    public File generateAttendanceReport(ReportRequest request) {
        List<AttendanceEvent> events = attendanceRepository
                .findByClockInTimeBetween(
                        request.getStartDate().atStartOfDay(),
                        request.getEndDate().atTime(23, 59, 59));
        
        return exportToCsv(events, "attendance_report.csv");
    }
    
    public File generateOvertimeReport(ReportRequest request) {
        // Calculate overtime hours per employee
        return null;
    }
    
    public File generateLeaveBalanceReport() {
        List<AccrualBalance> balances = accrualBalanceRepository.findAll();
        return exportToCsv(balances, "leave_balance_report.csv");
    }
    
    private File exportToCsv(List<?> data, String filename) {
        // CSV export implementation
        return null;
    }
}
```

---

### User Story 50: Role-Based Dashboards

**Section: Dashboard Service**

Description: Provide role-specific dashboards with relevant metrics.

Design Specification:
- Role-based data filtering
- Aggregated metrics
- Real-time updates

Sample Implementation:
```java
@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;
    
    @GetMapping
    public DashboardDto getDashboard(@AuthenticationPrincipal UserDetails user) {
        Employee employee = employeeRepository.findByUsername(user.getUsername())
                .orElseThrow();
        
        return dashboardService.getDashboardForRole(employee.getRole());
    }
}

@Service
public class DashboardService {
    
    public DashboardDto getDashboardForRole(Role role) {
        switch (role) {
            case ADMIN:
                return getAdminDashboard();
            case HR:
                return getHrDashboard();
            case SUPERVISOR:
                return getSupervisorDashboard();
            case WORKER:
                return getWorkerDashboard();
            default:
                throw new IllegalArgumentException("Unknown role");
        }
    }
    
    private DashboardDto getAdminDashboard() {
        DashboardDto dashboard = new DashboardDto();
        dashboard.setTotalEmployees(employeeRepository.count());
        dashboard.setActiveIncidents(incidentRepository.countByStatus(IncidentStatus.OPEN));
        dashboard.setPendingLeaveRequests(leaveRepository.countByStatus(LeaveStatus.REQUESTED));
        return dashboard;
    }
}
```

---

## EPIC E16: Mobile Access (PWA)

### User Story 51: Responsive Views for Clock-In/Out

**Section: Progressive Web App**

Description: Mobile-friendly PWA for clock-in/out and schedule viewing.

Design Specification:
- PWA manifest
- Service worker
- Responsive design

Sample Implementation:
```json
// manifest.json
{
  "name": "Warehouse Employee Management",
  "short_name": "WEM",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#007bff",
  "icons": [
    {
      "src": "/icons/icon-192x192.png",
      "sizes": "192x192",
      "type": "image/png"
    },
    {
      "src": "/icons/icon-512x512.png",
      "sizes": "512x512",
      "type": "image/png"
    }
  ]
}
```

```javascript
// service-worker.js
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open('wem-v1').then((cache) => {
      return cache.addAll([
        '/',
        '/index.html',
        '/styles.css',
        '/app.js'
      ]);
    })
  );
});

self.addEventListener('fetch', (event) => {
  event.respondWith(
    caches.match(event.request).then((response) => {
      return response || fetch(event.request);
    })
  );
});
```

---

### User Story 52: Offline Queue for Clock Events

**Section: Offline Support**

Description: Queue clock events offline and sync when connection is restored.

Design Specification:
- IndexedDB storage
- Background sync
- Conflict resolution

Sample Implementation:
```javascript
// offline-queue.js
class OfflineQueue {
  constructor() {
    this.dbName = 'wem-offline';
    this.storeName = 'clock-events';
  }
  
  async saveClockEvent(event) {
    const db = await this.openDB();
    const tx = db.transaction(this.storeName, 'readwrite');
    await tx.objectStore(this.storeName).add(event);
  }
  
  async syncPendingEvents() {
    if (!navigator.onLine) return;
    
    const db = await this.openDB();
    const tx = db.transaction(this.storeName, 'readonly');
    const events = await tx.objectStore(this.storeName).getAll();
    
    for (const event of events) {
      try {
        await this.sendToServer(event);
        await this.removeEvent(event.id);
      } catch (error) {
        console.error('Sync failed for event', event.id, error);
      }
    }
  }
  
  async sendToServer(event) {
    const response = await fetch('/attendance/clock-in', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(event)
    });
    
    if (!response.ok) {
      throw new Error('Server error');
    }
  }
}

// Register background sync
if ('serviceWorker' in navigator && 'sync' in ServiceWorkerRegistration.prototype) {
  navigator.serviceWorker.ready.then((registration) => {
    return registration.sync.register('sync-clock-events');
  });
}
```

---

## EPIC E17: Onboarding & Offboarding Workflow

### User Story 53: Provisioning Automation

**Section: Onboarding Automation**

Description: Automate account provisioning for new hires.

Design Specification:
- Event-driven workflow
- Task automation
- Integration with HRIS

Sample Implementation:
```java
public class NewHireEvent extends ApplicationEvent {
    private final Employee employee;
    
    public NewHireEvent(Object source, Employee employee) {
        super(source);
        this.employee = employee;
    }
}

@Component
public class OnboardingEventListener {
    
    @Autowired
    private UserAccountService userAccountService;
    
    @Autowired
    private TrainingService trainingService;
    
    @Autowired
    private SchedulingService schedulingService;
    
    @EventListener
    @Async
    public void handleNewHire(NewHireEvent event) {
        Employee employee = event.getEmployee();
        
        // Create user account
        userAccountService.createAccount(employee);
        
        // Assign mandatory training
        trainingService.assignOnboardingTraining(employee);
        
        // Create initial schedule
        schedulingService.createInitialSchedule(employee);
        
        // Send welcome notification
        notificationService.sendWelcomeEmail(employee);
    }
}
```

---

### User Story 54: Deprovisioning Automation

**Section: Offboarding Automation**

Description: Automate deprovisioning on employee termination.

Design Specification:
- Event-driven workflow
- Access revocation
- Asset collection

Sample Implementation:
```java
public class TerminationEvent extends ApplicationEvent {
    private final Employee employee;
    private final LocalDate terminationDate;
    
    public TerminationEvent(Object source, Employee employee, LocalDate terminationDate) {
        super(source);
        this.employee = employee;
        this.terminationDate = terminationDate;
    }
}

@Component
public class OffboardingEventListener {
    
    @Autowired
    private UserAccountService userAccountService;
    
    @Autowired
    private AssetService assetService;
    
    @Autowired
    private SchedulingService schedulingService;
    
    @EventListener
    @Async
    public void handleTermination(TerminationEvent event) {
        Employee employee = event.getEmployee();
        
        // Revoke system access
        userAccountService.revokeAccess(employee);
        
        // Collect assigned assets
        assetService.collectAllAssets(employee);
        
        // Remove from future schedules
        schedulingService.removeFromFutureSchedules(employee, event.getTerminationDate());
        
        // Update employee status
        employee.setStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);
    }
}
```

---

## EPIC E18: Localization & Multi-Tenant

### User Story 55: Multi-Tenant Support

**Section: Multi-Tenancy Architecture**

Description: Support multiple warehouses with tenant isolation.

Design Specification:
- Tenant ID in all entities
- Tenant-aware queries
- Data isolation

Sample Implementation:
```java
@MappedSuperclass
public abstract class TenantAwareEntity {
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    // Getters and setters
}

@Entity
public class Employee extends TenantAwareEntity {
    // Employee fields
}

@Aspect
@Component
public class TenantAspect {
    
    @Around("execution(* com.company.wem..repository.*Repository+.*(..))")
    public Object addTenantFilter(ProceedingJoinPoint joinPoint) throws Throwable {
        Long tenantId = TenantContext.getCurrentTenantId();
        
        // Add tenant filter to query
        Object result = joinPoint.proceed();
        
        return result;
    }
}

public class TenantContext {
    private static final ThreadLocal<Long> currentTenant = new ThreadLocal<>();
    
    public static void setCurrentTenantId(Long tenantId) {
        currentTenant.set(tenantId);
    }
    
    public static Long getCurrentTenantId() {
        return currentTenant.get();
    }
    
    public static void clear() {
        currentTenant.remove();
    }
}
```

---

### User Story 56: UI Localization

**Section: Internationalization**

Description: Support multiple languages in the UI.

Design Specification:
- i18n resource bundles
- Locale detection
- Message formatting

Sample Implementation:
```properties
# messages_en.properties
employee.name=Name
employee.badgeId=Badge ID
employee.department=Department
button.save=Save
button.cancel=Cancel

# messages_es.properties
employee.name=Nombre
employee.badgeId=ID de Placa
employee.department=Departamento
button.save=Guardar
button.cancel=Cancelar
```

```java
@Configuration
public class LocaleConfig {
    
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.US);
        return resolver;
    }
    
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
```

---

### User Story 57: Timezone-Aware Scheduling

**Section: Timezone Handling**

Description: Handle scheduling across multiple timezones.

Design Specification:
- Store timestamps in UTC
- Convert to user timezone
- Timezone configuration

Sample Implementation:
```java
@Entity
public class ShiftAssignment {
    @Column(name = "start_time")
    private Instant startTime; // Stored in UTC
    
    @Column(name = "end_time")
    private Instant endTime; // Stored in UTC
    
    @Column(name = "timezone")
    private String timezone;
}

@Service
public class TimezoneService {
    
    public ZonedDateTime convertToUserTimezone(Instant utcTime, String timezone) {
        ZoneId zoneId = ZoneId.of(timezone);
        return utcTime.atZone(zoneId);
    }
    
    public Instant convertToUtc(LocalDateTime localTime, String timezone) {
        ZoneId zoneId = ZoneId.of(timezone);
        return localTime.atZone(zoneId).toInstant();
    }
}
```

---

## EPIC E19: Observability & Monitoring

### User Story 58: Structured Logging & Distributed Tracing

**Section: Observability Infrastructure**

Description: Implement structured logging and distributed tracing.

Design Specification:
- Logback with JSON format
- Spring Cloud Sleuth
- Correlation IDs

Sample Implementation:
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
</dependency>
```

```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdcKeyName>traceId</includeMdcKeyName>
            <includeMdcKeyName>spanId</includeMdcKeyName>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="JSON" />
    </root>
</configuration>
```

```java
@Service
public class EmployeeService {
    
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
    
    public EmployeeDto createEmployee(EmployeeDto dto) {
        log.info("Creating employee", 
                kv("badgeId", dto.getBadgeId()),
                kv("department", dto.getDepartment()));
        
        // Implementation
    }
}
```

---

### User Story 59: Metrics & Alerting

**Section: Metrics Collection**

Description: Collect application metrics and set up alerting.

Design Specification:
- Micrometer for metrics
- Prometheus integration
- Custom metrics

Sample Implementation:
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.metrics.export.prometheus.enabled=true
```

```java
@Service
public class EmployeeService {
    
    private final MeterRegistry meterRegistry;
    private final Counter employeeCreatedCounter;
    private final Timer employeeCreationTimer;
    
    public EmployeeService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.employeeCreatedCounter = Counter.builder("employees.created")
                .description("Number of employees created")
                .register(meterRegistry);
        this.employeeCreationTimer = Timer.builder("employees.creation.time")
                .description("Time to create employee")
                .register(meterRegistry);
    }
    
    @Timed("employee.create")
    public EmployeeDto createEmployee(EmployeeDto dto) {
        return employeeCreationTimer.record(() -> {
            Employee employee = employeeMapper.toEntity(dto);
            Employee saved = employeeRepository.save(employee);
            employeeCreatedCounter.increment();
            return employeeMapper.toDto(saved);
        });
    }
}
```

---

## EPIC E20: CI/CD & Deployment Automation

### User Story 60: CI/CD Pipeline

**Section: Continuous Integration**

Description: Set up CI/CD pipeline for automated builds and tests.

Design Specification:
- GitHub Actions / Jenkins
- Maven build
- Unit and integration tests

Sample Implementation:
```yaml
# .github/workflows/ci.yml
name: CI Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
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
        distribution: 'temurin'
    
    - name: Cache Maven packages
      uses: actions/cache@v2
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Build with Maven
      run: mvn clean install
    
    - name: Run tests
      run: mvn test
    
    - name: Generate coverage report
      run: mvn jacoco:report
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v2
```

---

### User Story 61: Deployment Automation & Rollback

**Section: Continuous Deployment**

Description: Automate deployment with rollback capability.

Design Specification:
- Blue-green deployment
- Kubernetes deployment
- Rollback mechanism

Sample Implementation:
```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: wem-app
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: wem
  template:
    metadata:
      labels:
        app: wem
    spec:
      containers:
      - name: wem
        image: wem:latest
        ports:
        - containerPort: 8080
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
```

```bash
# deploy.sh
#!/bin/bash

# Build Docker image
docker build -t wem:$VERSION .

# Push to registry
docker push wem:$VERSION

# Deploy to Kubernetes
kubectl set image deployment/wem-app wem=wem:$VERSION

# Wait for rollout
kubectl rollout status deployment/wem-app

# Rollback if deployment fails
if [ $? -ne 0 ]; then
    echo "Deployment failed, rolling back..."
    kubectl rollout undo deployment/wem-app
fi
```

---

## Conclusion

This comprehensive low-level technical design document covers all 61 user stories across 20 epics for the Warehouse Employee Management System. Each user story includes:

- **Architecture Overview**: Spring Boot layered architecture with clear separation of concerns
- **Package Structure**: Feature-based package organization following Spring Boot conventions
- **Domain Models**: JPA entities with proper annotations, relationships, and constraints
- **Repository Layer**: Spring Data JPA repositories with custom queries
- **Service Layer**: Business logic with transaction management and validation
- **Controller Layer**: RESTful APIs with proper HTTP methods and status codes
- **Security Configuration**: Spring Security with role-based access control
- **Integration Points**: External system integrations (HRIS, WMS, IDP)
- **Sample Implementation**: Production-ready Java code examples

The design follows Spring Boot best practices including:
- Dependency injection
- Exception handling with @ControllerAdvice
- Bean Validation
- Transaction management
- RESTful API design
- OpenAPI documentation
- DTO pattern
- Database migrations with Flyway
- Observability with structured logging and metrics
- CI/CD automation

This document serves as a complete technical blueprint for implementing the Warehouse Employee Management System.