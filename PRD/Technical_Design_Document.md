# Warehouse Employee Management System - Low-Level Technical Design Document

---

## Table of Contents
1. [Spring Boot Architecture Overview](#architecture-overview)
2. [Package Structure & Module Definitions](#package-structure)
3. [Entity Design (JPA Domain Models & Relationships)](#entity-design)
4. [Service Layer Specifications](#service-layer)
5. [Repository Layer (Spring Data JPA)](#repository-layer)
6. [Controller Specifications (REST Endpoints & DTOs)](#controller-specs)
7. [Security Configuration (RBAC)](#security-config)
8. [Database Schema Design](#db-schema)
9. [Integration Points (External APIs, Webhooks)](#integration-points)
10. [Configuration Management (`application.yml`)](#config-management)
11. [Code Snippets & Pseudocode for Key Implementations](#code-snippets)
12. [Epic-by-Epic Technical Specifications](#epic-specs)

---

## <a name="architecture-overview"></a>1. Spring Boot Architecture Overview

- **Backend:** Spring Boot (Maven), Java 17+
- **Persistence:** Spring Data JPA, Flyway/Liquibase for migrations
- **Security:** Spring Security (RBAC, OAuth2, API Key toggle)
- **API:** RESTful endpoints, OpenAPI/Swagger documentation
- **Modules:** Employee, Scheduling, Attendance, Safety, Equipment, Leave, Certification, Reporting, Integration, Audit, Notification, Mobile, Multi-Tenant
- **Actuator:** Health, metrics, info endpoints
- **Testing:** JUnit, Mockito, Spring Test
- **Build/Run:** `mvn clean install`, `java -jar target/app.jar` (runs on port 8080)

---

## <a name="package-structure"></a>2. Complete Package Structure & Module Definitions

```
com.warehouse.ems
âââ config
âââ domain
â   âââ employee
â   âââ attendance
â   âââ schedule
â   âââ leave
â   âââ certification
â   âââ safety
â   âââ equipment
â   âââ review
â   âââ audit
â   âââ notification
â   âââ reporting
â   âââ integration
â   âââ tenant
âââ dto
â   âââ employee
â   âââ attendance
â   âââ schedule
â   âââ leave
â   âââ certification
â   âââ safety
â   âââ equipment
â   âââ review
â   âââ audit
â   âââ notification
â   âââ reporting
â   âââ integration
â   âââ tenant
âââ repository
âââ service
âââ controller
âââ security
âââ util
âââ migration
âââ Application.java
```

---

## <a name="entity-design"></a>3. Entity Design (JPA Domain Models & Relationships)

### Employee Entity

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
    private EmployeeRole role; // ADMIN, HR, SUPERVISOR, WORKER

    @ManyToOne
    private Department department;

    @ManyToOne
    private ShiftGroup shiftGroup;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status; // ACTIVE, INACTIVE, TERMINATED

    @Column(name = "deleted")
    private boolean deleted = false;

    // Relationships
    @OneToMany(mappedBy = "employee")
    private List<Attendance> attendances;

    @OneToMany(mappedBy = "employee")
    private List<Leave> leaves;

    @OneToMany(mappedBy = "employee")
    private List<Certification> certifications;

    @OneToMany(mappedBy = "employee")
    private List<AssetAssignment> assetAssignments;

    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### Attendance Entity

```java
@Entity
@Table(name = "attendance")
public class Attendance {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    @Column(name = "clock_in_time")
    private LocalDateTime clockInTime;

    @Column(name = "clock_out_time")
    private LocalDateTime clockOutTime;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "geofence_location")
    private String geofenceLocation;

    @ManyToOne
    private Shift shift;

    @Column(name = "correction_requested")
    private boolean correctionRequested;

    @Column(name = "correction_status")
    @Enumerated(EnumType.STRING)
    private CorrectionStatus correctionStatus; // PENDING, APPROVED, REJECTED

    @Column(name = "deleted")
    private boolean deleted = false;
}
```

### Shift & Schedule Entities

```java
@Entity
@Table(name = "shifts")
public class Shift {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "recurring_pattern")
    private String recurringPattern; // e.g., "WEEKLY", "ROTATION"

    @Column(name = "overtime_rule")
    private String overtimeRule;

    @ManyToOne
    private ShiftGroup shiftGroup;

    @Column(name = "blackout_date")
    private LocalDate blackoutDate;

    @Column(name = "operation_calendar")
    private String operationCalendar;
}
```

### Leave Entity

```java
@Entity
@Table(name = "leaves")
public class Leave {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private LeaveType leaveType; // PTO, SICK, UNPAID

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private LeaveStatus status; // REQUESTED, APPROVED, DENIED

    @Column(name = "accrual_balance")
    private Double accrualBalance;

    @Column(name = "policy")
    private String policy;
}
```

### Certification Entity

```java
@Entity
@Table(name = "certifications")
public class Certification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    @Column(name = "cert_type")
    private String certType; // e.g., "Forklift"

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "proof_document_url")
    private String proofDocumentUrl;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CertificationStatus status; // VALID, EXPIRED, PENDING_RENEWAL
}
```

### Safety Incident Entity

```java
@Entity
@Table(name = "safety_incidents")
public class SafetyIncident {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "severity")
    private String severity;

    @Column(name = "location")
    private String location;

    @Column(name = "description")
    private String description;

    @ManyToMany
    private List<Employee> involvedEmployees;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private IncidentStatus status; // OPEN, INVESTIGATING, RESOLVED

    @Column(name = "oscha_report_id")
    private String oschaReportId;
}
```

### Asset Assignment Entity

```java
@Entity
@Table(name = "asset_assignments")
public class AssetAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    @ManyToOne
    private Asset asset;

    @Column(name = "checkout_time")
    private LocalDateTime checkoutTime;

    @Column(name = "return_time")
    private LocalDateTime returnTime;

    @Column(name = "condition")
    private String condition;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AssetStatus status; // CHECKED_OUT, RETURNED, OVERDUE
}
```

### Audit Log Entity

```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity")
    private String entity;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "actor")
    private String actor;

    @Column(name = "action")
    private String action; // CREATE, UPDATE, DELETE

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "before_state", columnDefinition = "TEXT")
    private String beforeState;

    @Column(name = "after_state", columnDefinition = "TEXT")
    private String afterState;
}
```

### Tenant Entity

```java
@Entity
@Table(name = "tenants")
public class Tenant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "locale")
    private String locale;

    @Column(name = "region")
    private String region;

    @Column(name = "config")
    private String config; // JSON blob for tenant-specific config
}
```

---

## <a name="service-layer"></a>4. Service Layer Specifications

- **Transactional boundaries:** Annotate service methods with `@Transactional` for atomicity.
- **Business logic:** Encapsulate domain rules (e.g., badgeId uniqueness, shift conflict detection, leave accrual).
- **Audit:** Call audit logging service on sensitive changes.
- **Integration:** Use service interfaces for HRIS/WMS connectors.

### Example: EmployeeService

```java
public interface EmployeeService {
    Employee createEmployee(EmployeeDto dto);
    Employee updateEmployee(Long id, EmployeeDto dto);
    void softDeleteEmployee(Long id);
    Employee getEmployee(Long id);
    Page<Employee> listEmployees(EmployeeFilter filter, Pageable pageable);
}
```

### Example: AttendanceService

```java
public interface AttendanceService {
    Attendance clockIn(AttendanceDto dto);
    Attendance clockOut(AttendanceDto dto);
    Attendance requestCorrection(Long attendanceId, CorrectionDto dto);
    List<Attendance> getAttendanceForEmployee(Long employeeId, LocalDate date);
}
```

---

## <a name="repository-layer"></a>5. Repository Layer (Spring Data JPA)

- **Repositories:** Extend `JpaRepository` for CRUD, paging, filtering.
- **Custom queries:** Use `@Query` for complex filters (e.g., attendance by shift, asset overdue).
- **Soft delete:** Add `deleted = false` filter in queries.

### Example

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

---

## <a name="controller-specs"></a>6. Controller Specifications (REST Endpoints & DTOs)

- **DTOs:** Separate request/response DTOs for web APIs.
- **Validation:** Use `@Valid`, `@NotNull`, `@Pattern` annotations.
- **OpenAPI:** Annotate endpoints for Swagger generation.

### Example: EmployeeController

```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { ... }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public ResponseEntity<EmployeeDto> get(@PathVariable Long id) { ... }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeDto dto) { ... }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) { ... }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public ResponseEntity<Page<EmployeeDto>> list(EmployeeFilter filter, Pageable pageable) { ... }
}
```

---

## <a name="security-config"></a>7. Security Configuration (RBAC)

- **Spring Security:** Configure roles (ADMIN, HR, SUPERVISOR, WORKER)
- **Method security:** Use `@PreAuthorize` and `@PostAuthorize`
- **API key/OAuth2 toggle:** Conditional beans via `application.yml`
- **Row-level security:** Filter data by supervisor/team

### Example: SecurityConfig

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .antMatchers("/attendance/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer().jwt();
    }
}
```

---

## <a name="db-schema"></a>8. Database Schema Design

- **Flyway/Liquibase:** Baseline migration, versioned scripts per epic
- **Indexes:** On badgeId, attendance timestamps, asset status, audit logs
- **Constraints:** Unique badgeId, foreign keys, not nulls, soft delete flag

### Example: Flyway Migration

```sql
-- V1__baseline.sql
CREATE TABLE employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    badge_id VARCHAR(64) NOT NULL UNIQUE,
    role VARCHAR(32) NOT NULL,
    department_id BIGINT,
    shift_group_id BIGINT,
    hire_date DATE,
    status VARCHAR(32) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_badge_id ON employees(badge_id);
```

---

## <a name="integration-points"></a>9. Integration Points (External APIs, Webhooks)

- **HRIS:** REST connector for new hires/terms, scheduled sync job
- **WMS:** API for department/location mapping
- **IDP:** OAuth2/JWT for SSO
- **Webhooks:** POST to external URLs on events (employee created, shift assigned)

### Example: HRIS Sync Service

```java
@Service
public class HRISIntegrationService {
    @Scheduled(cron = "0 0 * * * ?")
    public void syncEmployees() {
        // Fetch from HRIS, upsert Employee records
    }
}
```

---

## <a name="config-management"></a>10. Configuration Management (`application.yml`)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/warehouse_ems
    username: ems_user
    password: secret
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
  flyway:
    enabled: true

security:
  oauth2:
    enabled: true
  api-key:
    enabled: false

notification:
  email:
    enabled: true
    quiet-hours: "22:00-06:00"
  sms:
    enabled: false

tenant:
  default-locale: en_US

logging:
  level:
    root: INFO
    com.warehouse.ems: DEBUG
```

---

## <a name="code-snippets"></a>11. Code Snippets & Pseudocode for Key Implementations

### Soft Delete Implementation

```java
public void softDeleteEmployee(Long id) {
    Employee emp = employeeRepository.findById(id).orElseThrow();
    emp.setDeleted(true);
    employeeRepository.save(emp);
    auditService.logDelete(emp);
}
```

### Shift Conflict Detection

```java
public boolean hasShiftConflict(Employee employee, LocalDate date, LocalTime start, LocalTime end) {
    List<Shift> shifts = shiftRepository.findByEmployeeAndDate(employee, date);
    for (Shift s : shifts) {
        if (start.isBefore(s.getEndTime()) && end.isAfter(s.getStartTime())) {
            return true;
        }
    }
    return false;
}
```

### Certification Expiry Alert

```java
@Scheduled(cron = "0 0 8 * * ?")
public void sendExpiryAlerts() {
    List<Certification> expiring = certificationRepository.findExpiringWithinDays(30);
    for (Certification cert : expiring) {
        notificationService.sendExpiryAlert(cert.getEmployee(), cert);
    }
}
```

### Audit Logging

```java
public void logChange(Object before, Object after, String actor, String action) {
    AuditLog log = new AuditLog();
    log.setBeforeState(serialize(before));
    log.setAfterState(serialize(after));
    log.setActor(actor);
    log.setAction(action);
    log.setTimestamp(LocalDateTime.now());
    auditLogRepository.save(log);
}
```

---

## <a name="epic-specs"></a>12. Epic-by-Epic Technical Specifications

### EPIC E01 - Project Scaffolding & Domain Setup
**Section:** Project Foundation

**Description:** Initialize the Spring Boot project with Maven, establish base package structure, configure core modules (employee, scheduling, attendance, safety), integrate Flyway/Liquibase for database migrations, and enable Spring Boot Actuator for monitoring.

**Design Specification:**
- Maven archetype: `spring-boot-starter-parent` version 2.7.x or 3.x
- Base package: `com.warehouse.ems`
- Core modules: employee, attendance, schedule, leave, certification, safety, equipment, review, audit, notification, reporting, integration, tenant
- Database migration tool: Flyway (preferred) or Liquibase
- Actuator endpoints: health, info, metrics
- Build tool: Maven with standard lifecycle

**Sample Implementation:**
```xml
<!-- pom.xml -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.1.0</version>
</parent>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
</dependencies>
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

```yaml
# application.yml
server:
  port: 8080

spring:
  application:
    name: warehouse-ems
  flyway:
    enabled: true
    baseline-on-migrate: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

---

### EPIC E02 - Employee Master Data (CRUD)
**Section:** Employee Domain Model

**Description:** Create a comprehensive Employee entity with full CRUD operations, including unique badge ID enforcement, soft delete capability, pagination, filtering, and OpenAPI documentation.

**Design Specification:**
- Entity fields: id, name, badgeId (unique), role, department, shiftGroup, hireDate, status, deleted flag
- REST endpoints: POST, GET, PUT, PATCH, DELETE `/employees`
- Soft delete: Set deleted flag instead of physical deletion
- Pagination: Spring Data Pageable support
- Filtering: By department, role, status
- Validation: JSR-303 annotations
- OpenAPI: Swagger annotations for API documentation

**Sample Implementation:**
```java
// EmployeeDto.java
public class EmployeeDto {
    private Long id;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Badge ID is required")
    @Pattern(regexp = "^[A-Z0-9]{6,10}$", message = "Badge ID must be 6-10 alphanumeric characters")
    private String badgeId;
    
    @NotNull(message = "Role is required")
    private EmployeeRole role;
    
    private Long departmentId;
    private Long shiftGroupId;
    private LocalDate hireDate;
    private EmployeeStatus status;
    // Getters and setters
}
```

```java
// EmployeeServiceImpl.java
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private AuditService auditService;
    
    @Override
    public Employee createEmployee(EmployeeDto dto) {
        // Check badge ID uniqueness
        if (employeeRepository.findByBadgeIdAndDeletedFalse(dto.getBadgeId()).isPresent()) {
            throw new DuplicateBadgeIdException("Badge ID already exists");
        }
        
        Employee employee = new Employee();
        employee.setName(dto.getName());
        employee.setBadgeId(dto.getBadgeId());
        employee.setRole(dto.getRole());
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setCreatedAt(LocalDateTime.now());
        
        Employee saved = employeeRepository.save(employee);
        auditService.logCreate(saved, SecurityContextHolder.getContext().getAuthentication().getName());
        
        return saved;
    }
    
    @Override
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        
        employee.setDeleted(true);
        employee.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(employee);
        
        auditService.logDelete(employee, SecurityContextHolder.getContext().getAuthentication().getName());
    }
    
    @Override
    public Page<Employee> listEmployees(EmployeeFilter filter, Pageable pageable) {
        // Apply filters using Specification pattern
        Specification<Employee> spec = EmployeeSpecification.withFilter(filter);
        return employeeRepository.findAll(spec, pageable);
    }
}
```

---

### EPIC E03 - Role Based Access Control (RBAC)
**Section:** Security Configuration

**Description:** Implement Spring Security with role-based access control supporting ADMIN, HR, SUPERVISOR, and WORKER roles. Include method-level security, row-level data filtering, and configurable authentication (API Key or OAuth2).

**Design Specification:**
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Method security: @PreAuthorize, @PostAuthorize annotations
- Row-level security: Filter queries by supervisor/team relationships
- Authentication modes: API Key (header-based) or OAuth2/JWT
- Configuration toggle: Via application.yml
- Security tests: Verify unauthorized access returns 401/403

**Sample Implementation:**
```java
// SecurityConfig.java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Value("${security.oauth2.enabled}")
    private boolean oauth2Enabled;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated()
            );
        
        if (oauth2Enabled) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt());
        } else {
            http.addFilterBefore(new ApiKeyAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        }
        
        return http.build();
    }
}
```

```java
// EmployeeController with RBAC
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Create new employee", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeDto dto) {
        Employee employee = employeeService.createEmployee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(employee));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public ResponseEntity<Page<EmployeeDto>> listEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) EmployeeRole role,
            Pageable pageable) {
        
        // Row-level security: Supervisors see only their team
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERVISOR"))) {
            Long supervisorId = getCurrentUserId();
            return ResponseEntity.ok(employeeService.listEmployeesForSupervisor(supervisorId, pageable));
        }
        
        EmployeeFilter filter = new EmployeeFilter(department, role);
        return ResponseEntity.ok(employeeService.listEmployees(filter, pageable).map(this::toDto));
    }
}
```

---

### EPIC E04 - Time & Attendance (Clock In/Out)
**Section:** Attendance Management

**Description:** Implement clock-in/clock-out functionality with optional geofence validation, device tracking, automatic shift association, daily hours calculation, and missed punch correction workflow.

**Design Specification:**
- Endpoints: POST `/attendance/clock-in`, POST `/attendance/clock-out`
- Capture: timestamp, device ID, geofence location (optional)
- Shift association: Automatic based on employee's schedule
- Hours calculation: Compute daily totals on clock-out
- Corrections: Workflow for missed punches requiring supervisor approval
- Export: CSV format for payroll integration

**Sample Implementation:**
```java
// AttendanceService.java
@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private ShiftRepository shiftRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Override
    public Attendance clockIn(AttendanceDto dto) {
        Employee employee = getEmployee(dto.getEmployeeId());
        
        // Check for existing open attendance
        Optional<Attendance> openAttendance = attendanceRepository
            .findByEmployeeAndClockOutTimeIsNull(employee);
        if (openAttendance.isPresent()) {
            throw new AlreadyClockedInException("Employee is already clocked in");
        }
        
        // Find matching shift
        Shift shift = shiftRepository.findActiveShiftForEmployee(employee, LocalDateTime.now())
            .orElseThrow(() -> new NoScheduledShiftException("No scheduled shift found"));
        
        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setClockInTime(LocalDateTime.now());
        attendance.setDeviceId(dto.getDeviceId());
        attendance.setGeofenceLocation(dto.getGeofenceLocation());
        attendance.setShift(shift);
        
        return attendanceRepository.save(attendance);
    }
    
    @Override
    public Attendance clockOut(AttendanceDto dto) {
        Employee employee = getEmployee(dto.getEmployeeId());
        
        Attendance attendance = attendanceRepository
            .findByEmployeeAndClockOutTimeIsNull(employee)
            .orElseThrow(() -> new NotClockedInException("Employee is not clocked in"));
        
        attendance.setClockOutTime(LocalDateTime.now());
        
        // Calculate hours worked
        Duration duration = Duration.between(attendance.getClockInTime(), attendance.getClockOutTime());
        double hoursWorked = duration.toMinutes() / 60.0;
        attendance.setHoursWorked(hoursWorked);
        
        return attendanceRepository.save(attendance);
    }
    
    @Override
    public Attendance requestCorrection(Long attendanceId, CorrectionDto dto) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
            .orElseThrow(() -> new AttendanceNotFoundException("Attendance record not found"));
        
        attendance.setCorrectionRequested(true);
        attendance.setCorrectionStatus(CorrectionStatus.PENDING);
        attendance.setCorrectionReason(dto.getReason());
        attendance.setProposedClockInTime(dto.getProposedClockInTime());
        attendance.setProposedClockOutTime(dto.getProposedClockOutTime());
        
        Attendance saved = attendanceRepository.save(attendance);
        
        // Notify supervisor
        notificationService.sendCorrectionRequest(attendance.getEmployee().getSupervisor(), saved);
        
        return saved;
    }
}
```

---

### EPIC E05 - Shift & Schedule Management
**Section:** Scheduling System

**Description:** Create a comprehensive shift management system with recurring templates, rotation support, overtime rules, conflict detection, blackout dates, and bulk assignment capabilities.

**Design Specification:**
- Shift templates: Reusable patterns (daily, weekly, rotation)
- Overtime rules: Configurable thresholds and multipliers
- Conflict detection: Prevent overlapping shifts
- Blackout dates: Company holidays, maintenance periods
- Operation calendar: Warehouse-specific schedules
- Bulk assignment: Assign shifts to multiple employees
- Audit trail: Log all schedule changes

**Sample Implementation:**
```java
// ShiftService.java
@Service
@Transactional
public class ShiftServiceImpl implements ShiftService {
    
    @Autowired
    private ShiftRepository shiftRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private AuditService auditService;
    
    @Override
    public Shift createShiftTemplate(ShiftTemplateDto dto) {
        Shift shift = new Shift();
        shift.setStartTime(dto.getStartTime());
        shift.setEndTime(dto.getEndTime());
        shift.setRecurringPattern(dto.getRecurringPattern());
        shift.setOvertimeRule(dto.getOvertimeRule());
        shift.setShiftGroup(getShiftGroup(dto.getShiftGroupId()));
        
        return shiftRepository.save(shift);
    }
    
    @Override
    public List<ScheduleAssignment> bulkAssignShift(BulkAssignmentDto dto) {
        Shift shift = shiftRepository.findById(dto.getShiftId())
            .orElseThrow(() -> new ShiftNotFoundException("Shift not found"));
        
        List<Employee> employees = employeeRepository.findAllById(dto.getEmployeeIds());
        List<ScheduleAssignment> assignments = new ArrayList<>();
        
        for (Employee employee : employees) {
            // Check for conflicts
            if (hasShiftConflict(employee, dto.getStartDate(), shift)) {
                throw new ShiftConflictException(
                    String.format("Shift conflict detected for employee %s", employee.getBadgeId())
                );
            }
            
            ScheduleAssignment assignment = new ScheduleAssignment();
            assignment.setEmployee(employee);
            assignment.setShift(shift);
            assignment.setStartDate(dto.getStartDate());
            assignment.setEndDate(dto.getEndDate());
            
            assignments.add(scheduleRepository.save(assignment));
            auditService.logShiftAssignment(assignment, getCurrentUser());
        }
        
        return assignments;
    }
    
    private boolean hasShiftConflict(Employee employee, LocalDate date, Shift newShift) {
        List<ScheduleAssignment> existingAssignments = 
            scheduleRepository.findByEmployeeAndDate(employee, date);
        
        for (ScheduleAssignment existing : existingAssignments) {
            Shift existingShift = existing.getShift();
            
            // Check time overlap
            if (newShift.getStartTime().isBefore(existingShift.getEndTime()) &&
                newShift.getEndTime().isAfter(existingShift.getStartTime())) {
                return true;
            }
        }
        
        // Check blackout dates
        if (isBlackoutDate(date)) {
            return true;
        }
        
        return false;
    }
}
```

---

### EPIC E06 - Leave & Absence Management
**Section:** Leave Management System

**Description:** Implement a comprehensive leave management system supporting PTO, sick leave, and unpaid leave with accrual tracking, approval workflows, and integration with scheduling and payroll.

**Design Specification:**
- Leave types: PTO, SICK, UNPAID
- Accrual policies: Configurable per employee type
- Approval workflow: Request â Supervisor approval â Balance update
- Schedule integration: Auto-flag affected shifts
- Payroll integration: Export approved leaves
- Balance tracking: Real-time accrual and usage

**Sample Implementation:**
```java
// LeaveService.java
@Service
@Transactional
public class LeaveServiceImpl implements LeaveService {
    
    @Autowired
    private LeaveRepository leaveRepository;
    
    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Override
    public Leave requestLeave(LeaveRequestDto dto) {
        Employee employee = getEmployee(dto.getEmployeeId());
        
        // Check balance
        double currentBalance = calculateLeaveBalance(employee, dto.getLeaveType());
        double requestedDays = calculateBusinessDays(dto.getStartDate(), dto.getEndDate());
        
        if (requestedDays > currentBalance) {
            throw new InsufficientLeaveBalanceException(
                String.format("Insufficient balance. Available: %.1f, Requested: %.1f", 
                    currentBalance, requestedDays)
            );
        }
        
        Leave leave = new Leave();
        leave.setEmployee(employee);
        leave.setLeaveType(dto.getLeaveType());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setStatus(LeaveStatus.REQUESTED);
        leave.setReason(dto.getReason());
        
        Leave saved = leaveRepository.save(leave);
        
        // Notify supervisor
        notificationService.sendLeaveRequest(employee.getSupervisor(), saved);
        
        return saved;
    }
    
    @Override
    public Leave approveLeave(Long leaveId, String approverComments) {
        Leave leave = leaveRepository.findById(leaveId)
            .orElseThrow(() -> new LeaveNotFoundException("Leave request not found"));
        
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApproverComments(approverComments);
        leave.setApprovedAt(LocalDateTime.now());
        leave.setApprovedBy(getCurrentUser());
        
        Leave saved = leaveRepository.save(leave);
        
        // Update accrual balance
        updateLeaveBalance(leave.getEmployee(), leave.getLeaveType(), 
            -calculateBusinessDays(leave.getStartDate(), leave.getEndDate()));
        
        // Flag affected shifts for coverage
        flagAffectedShifts(leave);
        
        // Notify employee
        notificationService.sendLeaveApproval(leave.getEmployee(), saved);
        
        return saved;
    }
    
    private void flagAffectedShifts(Leave leave) {
        List<ScheduleAssignment> affectedShifts = scheduleRepository
            .findByEmployeeAndDateRange(leave.getEmployee(), leave.getStartDate(), leave.getEndDate());
        
        for (ScheduleAssignment assignment : affectedShifts) {
            assignment.setNeedsCoverage(true);
            assignment.setCoverageReason("Employee on approved leave");
            scheduleRepository.save(assignment);
        }
    }
}
```

---

### EPIC E07 - Training & Certification Tracking
**Section:** Certification Management

**Description:** Track employee certifications with expiration monitoring, renewal alerts, document management, and enforcement of certification requirements for equipment and task assignments.

**Design Specification:**
- Certification types: Forklift, Safety, Equipment-specific
- Expiration tracking: Alert at 30 and 7 days before expiry
- Document upload: Store proof of certification
- Assignment blocking: Prevent unqualified assignments
- Status tracking: VALID, EXPIRED, PENDING_RENEWAL
- Renewal workflow: Request â Upload â Approval

**Sample Implementation:**
```java
// CertificationService.java
@Service
@Transactional
public class CertificationServiceImpl implements CertificationService {
    
    @Autowired
    private CertificationRepository certificationRepository;
    
    @Autowired
    private DocumentStorageService documentStorageService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Override
    public Certification addCertification(CertificationDto dto, MultipartFile proofDocument) {
        Employee employee = getEmployee(dto.getEmployeeId());
        
        // Upload proof document
        String documentUrl = documentStorageService.uploadDocument(proofDocument, 
            String.format("certifications/%s/%s", employee.getBadgeId(), dto.getCertType()));
        
        Certification cert = new Certification();
        cert.setEmployee(employee);
        cert.setCertType(dto.getCertType());
        cert.setExpiryDate(dto.getExpiryDate());
        cert.setProofDocumentUrl(documentUrl);
        cert.setStatus(CertificationStatus.VALID);
        cert.setIssuedDate(LocalDate.now());
        
        return certificationRepository.save(cert);
    }
    
    @Scheduled(cron = "0 0 8 * * ?") // Daily at 8 AM
    public void checkExpiringCertifications() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysOut = today.plusDays(30);
        LocalDate sevenDaysOut = today.plusDays(7);
        
        // 30-day alerts
        List<Certification> expiring30 = certificationRepository
            .findByExpiryDateBetweenAndStatus(today, thirtyDaysOut, CertificationStatus.VALID);
        for (Certification cert : expiring30) {
            notificationService.sendExpiryAlert(cert.getEmployee(), cert, 30);
            notificationService.sendExpiryAlert(cert.getEmployee().getSupervisor(), cert, 30);
        }
        
        // 7-day alerts
        List<Certification> expiring7 = certificationRepository
            .findByExpiryDateBetweenAndStatus(today, sevenDaysOut, CertificationStatus.VALID);
        for (Certification cert : expiring7) {
            notificationService.sendExpiryAlert(cert.getEmployee(), cert, 7);
            notificationService.sendExpiryAlert(cert.getEmployee().getSupervisor(), cert, 7);
        }
        
        // Mark expired certifications
        List<Certification> expired = certificationRepository
            .findByExpiryDateBeforeAndStatus(today, CertificationStatus.VALID);
        for (Certification cert : expired) {
            cert.setStatus(CertificationStatus.EXPIRED);
            certificationRepository.save(cert);
        }
    }
    
    @Override
    public boolean isQualifiedForTask(Long employeeId, String requiredCertType) {
        Employee employee = getEmployee(employeeId);
        
        Optional<Certification> cert = certificationRepository
            .findByEmployeeAndCertTypeAndStatus(employee, requiredCertType, CertificationStatus.VALID);
        
        return cert.isPresent() && cert.get().getExpiryDate().isAfter(LocalDate.now());
    }
}
```

---

### EPIC E08 - Safety Incidents & OSHA Reporting
**Section:** Safety Management System

**Description:** Record and manage safety incidents and near-misses with workflow tracking, OSHA reporting capabilities, and safety metrics dashboards.

**Design Specification:**
- Incident recording: Severity, location, description, involved employees
- Workflow states: OPEN, INVESTIGATING, RESOLVED
- OSHA export: 300/300A summary fields
- Metrics: Incident rates, near-miss tracking, corrective actions
- Investigation: Assign investigators, track findings
- Corrective actions: Link to incidents, track completion

**Sample Implementation:**
```java
// SafetyIncidentService.java
@Service
@Transactional
public class SafetyIncidentServiceImpl implements SafetyIncidentService {
    
    @Autowired
    private SafetyIncidentRepository incidentRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Override
    public SafetyIncident recordIncident(IncidentDto dto) {
        SafetyIncident incident = new SafetyIncident();
        incident.setSeverity(dto.getSeverity());
        incident.setLocation(dto.getLocation());
        incident.setDescription(dto.getDescription());
        incident.setIncidentDate(dto.getIncidentDate());
        incident.setStatus(IncidentStatus.OPEN);
        incident.setReportedBy(getCurrentUser());
        incident.setReportedAt(LocalDateTime.now());
        
        // Add involved employees
        List<Employee> involvedEmployees = employeeRepository.findAllById(dto.getInvolvedEmployeeIds());
        incident.setInvolvedEmployees(involvedEmployees);
        
        SafetyIncident saved = incidentRepository.save(incident);
        
        // Notify safety officer and supervisors
        notificationService.sendIncidentAlert(saved);
        
        return saved;
    }
    
    @Override
    public SafetyIncident updateIncidentStatus(Long incidentId, IncidentStatus newStatus, String notes) {
        SafetyIncident incident = incidentRepository.findById(incidentId)
            .orElseThrow(() -> new IncidentNotFoundException("Incident not found"));
        
        IncidentStatus oldStatus = incident.getStatus();
        incident.setStatus(newStatus);
        incident.setStatusNotes(notes);
        incident.setStatusUpdatedAt(LocalDateTime.now());
        incident.setStatusUpdatedBy(getCurrentUser());
        
        // Log status change
        IncidentStatusHistory history = new IncidentStatusHistory();
        history.setIncident(incident);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(getCurrentUser());
        history.setChangedAt(LocalDateTime.now());
        history.setNotes(notes);
        statusHistoryRepository.save(history);
        
        return incidentRepository.save(incident);
    }
    
    @Override
    public OshaReport generateOshaReport(int year) {
        List<SafetyIncident> incidents = incidentRepository.findByYear(year);
        
        OshaReport report = new OshaReport();
        report.setYear(year);
        report.setTotalIncidents(incidents.size());
        report.setFatalIncidents(countBySeverity(incidents, "FATAL"));
        report.setLostTimeIncidents(countBySeverity(incidents, "LOST_TIME"));
        report.setRestrictedWorkIncidents(countBySeverity(incidents, "RESTRICTED_WORK"));
        report.setMedicalTreatmentIncidents(countBySeverity(incidents, "MEDICAL_TREATMENT"));
        report.setFirstAidIncidents(countBySeverity(incidents, "FIRST_AID"));
        report.setNearMisses(countBySeverity(incidents, "NEAR_MISS"));
        
        // Calculate rates
        int totalHoursWorked = calculateTotalHoursWorked(year);
        report.setIncidentRate(calculateIncidentRate(incidents.size(), totalHoursWorked));
        
        return report;
    }
    
    @Override
    public SafetyMetrics getSafetyMetrics(LocalDate startDate, LocalDate endDate) {
        List<SafetyIncident> incidents = incidentRepository
            .findByIncidentDateBetween(startDate, endDate);
        
        SafetyMetrics metrics = new SafetyMetrics();
        metrics.setTotalIncidents(incidents.size());
        metrics.setIncidentsBySeverity(groupBySeverity(incidents));
        metrics.setIncidentsByLocation(groupByLocation(incidents));
        metrics.setAverageResolutionTime(calculateAverageResolutionTime(incidents));
        metrics.setOpenIncidents(countByStatus(incidents, IncidentStatus.OPEN));
        metrics.setInvestigatingIncidents(countByStatus(incidents, IncidentStatus.INVESTIGATING));
        metrics.setResolvedIncidents(countByStatus(incidents, IncidentStatus.RESOLVED));
        
        return metrics;
    }
}
```

---

### EPIC E09 - Equipment & Asset Assignment
**Section:** Asset Management System

**Description:** Manage equipment and asset assignments with certification validation, checkout/return tracking, condition monitoring, and overdue reporting.

**Design Specification:**
- Asset types: Scanners, forklifts, PPE, tools
- Assignment tracking: Checkout/return timestamps, condition
- Certification enforcement: Block assignment if cert missing/expired
- History logging: Complete audit trail per asset
- Overdue alerts: Notify supervisors of unreturned assets
- Maintenance tracking: Schedule and track asset maintenance

**Sample Implementation:**
```java
// AssetService.java
@Service
@Transactional
public class AssetServiceImpl implements AssetService {
    
    @Autowired
    private AssetRepository assetRepository;
    
    @Autowired
    private AssetAssignmentRepository assignmentRepository;
    
    @Autowired
    private CertificationService certificationService;
    
    @Override
    public Asset registerAsset(AssetDto dto) {
        Asset asset = new Asset();
        asset.setAssetType(dto.getAssetType());
        asset.setSerialNumber(dto.getSerialNumber());
        asset.setCondition(AssetCondition.GOOD);
        asset.setStatus(AssetStatus.AVAILABLE);
        asset.setRequiredCertification(dto.getRequiredCertification());
        asset.setPurchaseDate(dto.getPurchaseDate());
        asset.setLastMaintenanceDate(LocalDate.now());
        
        return assetRepository.save(asset);
    }
    
    @Override
    public AssetAssignment assignAsset(AssetAssignmentDto dto) {
        Asset asset = assetRepository.findById(dto.getAssetId())
            .orElseThrow(() -> new AssetNotFoundException("Asset not found"));
        
        Employee employee = getEmployee(dto.getEmployeeId());
        
        // Check asset availability
        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new AssetNotAvailableException("Asset is not available for assignment");
        }
        
        // Check certification requirement
        if (asset.getRequiredCertification() != null) {
            if (!certificationService.isQualifiedForTask(employee.getId(), asset.getRequiredCertification())) {
                throw new InsufficientCertificationException(
                    String.format("Employee %s lacks required certification: %s", 
                        employee.getBadgeId(), asset.getRequiredCertification())
                );
            }
        }
        
        AssetAssignment assignment = new AssetAssignment();
        assignment.setAsset(asset);
        assignment.setEmployee(employee);
        assignment.setCheckoutTime(LocalDateTime.now());
        assignment.setCondition(asset.getCondition().toString());
        assignment.setStatus(AssetStatus.CHECKED_OUT);
        assignment.setExpectedReturnDate(dto.getExpectedReturnDate());
        
        // Update asset status
        asset.setStatus(AssetStatus.CHECKED_OUT);
        assetRepository.save(asset);
        
        return assignmentRepository.save(assignment);
    }
    
    @Override
    public AssetAssignment returnAsset(Long assignmentId, AssetReturnDto dto) {
        AssetAssignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found"));
        
        assignment.setReturnTime(LocalDateTime.now());
        assignment.setReturnCondition(dto.getCondition());
        assignment.setReturnNotes(dto.getNotes());
        assignment.setStatus(AssetStatus.RETURNED);
        
        // Update asset
        Asset asset = assignment.getAsset();
        asset.setStatus(AssetStatus.AVAILABLE);
        asset.setCondition(dto.getCondition());
        assetRepository.save(asset);
        
        return assignmentRepository.save(assignment);
    }
    
    @Scheduled(cron = "0 0 9 * * ?") // Daily at 9 AM
    public void checkOverdueAssets() {
        LocalDate today = LocalDate.now();
        List<AssetAssignment> overdueAssignments = assignmentRepository
            .findByExpectedReturnDateBeforeAndStatus(today, AssetStatus.CHECKED_OUT);
        
        for (AssetAssignment assignment : overdueAssignments) {
            assignment.setStatus(AssetStatus.OVERDUE);
            assignmentRepository.save(assignment);
            
            // Notify employee and supervisor
            notificationService.sendOverdueAssetAlert(assignment.getEmployee(), assignment);
            notificationService.sendOverdueAssetAlert(assignment.getEmployee().getSupervisor(), assignment);
        }
    }
}
```

---

### EPIC E10 - Performance Reviews & Goals
**Section:** Performance Management

**Description:** Implement a structured performance review system with quarterly/annual cycles, goal tracking, competency ratings, and acknowledgement workflows.

**Design Specification:**
- Review cycles: Quarterly, annual, ad-hoc
- Templates: Customizable review forms
- Goal tracking: SMART goals with progress monitoring
- Ratings: Competency-based scoring
- Workflow: Create â Assign â Submit â Acknowledge
- PDF export: Generate review documents
- Immutability: Lock reviews after acknowledgement

**Sample Implementation:**
```java
// PerformanceReviewService.java
@Service
@Transactional
public class PerformanceReviewServiceImpl implements PerformanceReviewService {
    
    @Autowired
    private ReviewCycleRepository cycleRepository;
    
    @Autowired
    private PerformanceReviewRepository reviewRepository;
    
    @Override
    public ReviewCycle createReviewCycle(ReviewCycleDto dto) {
        ReviewCycle cycle = new ReviewCycle();
        cycle.setName(dto.getName());
        cycle.setPeriodType(dto.getPeriodType()); // QUARTERLY, ANNUAL
        cycle.setStartDate(dto.getStartDate());
        cycle.setEndDate(dto.getEndDate());
        cycle.setTemplate(dto.getTemplate());
        cycle.setStatus(CycleStatus.ACTIVE);
        
        ReviewCycle saved = cycleRepository.save(cycle);
        
        // Auto-assign reviews to all active employees
        List<Employee> employees = employeeRepository.findAllByStatusAndDeletedFalse(EmployeeStatus.ACTIVE);
        for (Employee employee : employees) {
            createReviewForEmployee(saved, employee);
        }
        
        return saved;
    }
    
    private PerformanceReview createReviewForEmployee(ReviewCycle cycle, Employee employee) {
        PerformanceReview review = new PerformanceReview();
        review.setCycle(cycle);
        review.setEmployee(employee);
        review.setReviewer(employee.getSupervisor());
        review.setStatus(ReviewStatus.PENDING);
        review.setDueDate(cycle.getEndDate());
        
        PerformanceReview saved = reviewRepository.save(review);
        
        // Notify supervisor
        notificationService.sendReviewAssignment(employee.getSupervisor(), saved);
        
        return saved;
    }
    
    @Override
    public PerformanceReview submitReview(Long reviewId, ReviewSubmissionDto dto) {
        PerformanceReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException("Review not found"));
        
        // Verify reviewer authorization
        if (!review.getReviewer().getId().equals(getCurrentUserId())) {
            throw new UnauthorizedReviewerException("Only assigned reviewer can submit");
        }
        
        review.setOverallRating(dto.getOverallRating());
        review.setStrengths(dto.getStrengths());
        review.setAreasForImprovement(dto.getAreasForImprovement());
        review.setGoals(dto.getGoals());
        review.setComments(dto.getComments());
        review.setStatus(ReviewStatus.SUBMITTED);
        review.setSubmittedAt(LocalDateTime.now());
        
        // Save competency ratings
        for (CompetencyRatingDto rating : dto.getCompetencyRatings()) {
            CompetencyRating competency = new CompetencyRating();
            competency.setReview(review);
            competency.setCompetencyName(rating.getCompetencyName());
            competency.setRating(rating.getRating());
            competency.setComments(rating.getComments());
            competencyRatingRepository.save(competency);
        }
        
        PerformanceReview saved = reviewRepository.save(review);
        
        // Notify employee
        notificationService.sendReviewSubmitted(review.getEmployee(), saved);
        
        return saved;
    }
    
    @Override
    public PerformanceReview acknowledgeReview(Long reviewId, String employeeComments) {
        PerformanceReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException("Review not found"));
        
        // Verify employee authorization
        if (!review.getEmployee().getId().equals(getCurrentUserId())) {
            throw new UnauthorizedAccessException("Only the reviewed employee can acknowledge");
        }
        
        review.setEmployeeComments(employeeComments);
        review.setStatus(ReviewStatus.ACKNOWLEDGED);
        review.setAcknowledgedAt(LocalDateTime.now());
        review.setImmutable(true); // Lock the review
        
        return reviewRepository.save(review);
    }
    
    @Override
    public byte[] exportReviewToPdf(Long reviewId) {
        PerformanceReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException("Review not found"));
        
        // Generate PDF using template engine
        return pdfGeneratorService.generateReviewPdf(review);
    }
}
```

---

### EPIC E11 - Payroll Export Integration
**Section:** Payroll Integration

**Description:** Generate payroll-ready files from attendance and leave data with provider-specific formatting, secure delivery, and comprehensive audit logging.

**Design Specification:**
- Data aggregation: Attendance hours + leave deductions
- Provider formats: Configurable mappings (ADP, Paychex, etc.)
- Delivery methods: SFTP, REST API, file download
- Reconciliation: Totals validation before export
- Retry logic: Exponential backoff for failed deliveries
- Audit trail: Complete export history

**Sample Implementation:**
```java
// PayrollExportService.java
@Service
@Transactional
public class PayrollExportServiceImpl implements PayrollExportService {
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private LeaveRepository leaveRepository;
    
    @Autowired
    private PayrollExportRepository exportRepository;
    
    @Autowired
    private SftpService sftpService;
    
    @Value("${payroll.provider}")
    private String payrollProvider;
    
    @Override
    public PayrollExport generatePayrollExport(LocalDate startDate, LocalDate endDate) {
        // Aggregate attendance data
        List<AttendanceRecord> attendanceRecords = aggregateAttendance(startDate, endDate);
        
        // Aggregate leave data
        List<LeaveRecord> leaveRecords = aggregateLeave(startDate, endDate);
        
        // Combine and calculate totals
        List<PayrollRecord> payrollRecords = new ArrayList<>();
        Map<Long, AttendanceRecord> attendanceMap = attendanceRecords.stream()
            .collect(Collectors.toMap(AttendanceRecord::getEmployeeId, Function.identity()));
        
        for (Employee employee : employeeRepository.findAllByStatusAndDeletedFalse(EmployeeStatus.ACTIVE)) {
            PayrollRecord record = new PayrollRecord();
            record.setEmployeeId(employee.getId());
            record.setBadgeId(employee.getBadgeId());
            record.setName(employee.getName());
            
            // Regular hours
            AttendanceRecord attendance = attendanceMap.get(employee.getId());
            if (attendance != null) {
                record.setRegularHours(attendance.getRegularHours());
                record.setOvertimeHours(attendance.getOvertimeHours());
            }
            
            // Leave deductions
            List<LeaveRecord> employeeLeaves = leaveRecords.stream()
                .filter(l -> l.getEmployeeId().equals(employee.getId()))
                .collect(Collectors.toList());
            record.setPaidLeaveHours(calculatePaidLeaveHours(employeeLeaves));
            record.setUnpaidLeaveHours(calculateUnpaidLeaveHours(employeeLeaves));
            
            // Calculate net hours
            record.setNetPayableHours(
                record.getRegularHours() + 
                record.getOvertimeHours() + 
                record.getPaidLeaveHours() - 
                record.getUnpaidLeaveHours()
            );
            
            payrollRecords.add(record);
        }
        
        // Format according to provider
        String fileContent = formatForProvider(payrollRecords, payrollProvider);
        
        // Create export record
        PayrollExport export = new PayrollExport();
        export.setStartDate(startDate);
        export.setEndDate(endDate);
        export.setProvider(payrollProvider);
        export.setRecordCount(payrollRecords.size());
        export.setTotalHours(payrollRecords.stream()
            .mapToDouble(PayrollRecord::getNetPayableHours)
            .sum());
        export.setFileContent(fileContent);
        export.setStatus(ExportStatus.PENDING);
        export.setCreatedAt(LocalDateTime.now());
        export.setCreatedBy(getCurrentUser());
        
        return exportRepository.save(export);
    }
    
    @Override
    @Async
    public void deliverPayrollExport(Long exportId) {
        PayrollExport export = exportRepository.findById(exportId)
            .orElseThrow(() -> new ExportNotFoundException("Export not found"));
        
        int maxRetries = 3;
        int retryCount = 0;
        boolean delivered = false;
        
        while (retryCount < maxRetries && !delivered) {
            try {
                // Deliver via SFTP
                String fileName = String.format("payroll_%s_%s.csv", 
                    export.getStartDate(), export.getEndDate());
                sftpService.uploadFile(fileName, export.getFileContent());
                
                export.setStatus(ExportStatus.DELIVERED);
                export.setDeliveredAt(LocalDateTime.now());
                delivered = true;
                
            } catch (Exception e) {
                retryCount++;
                export.setRetryCount(retryCount);
                export.setLastError(e.getMessage());
                
                if (retryCount >= maxRetries) {
                    export.setStatus(ExportStatus.FAILED);
                } else {
                    // Exponential backoff
                    try {
                        Thread.sleep((long) Math.pow(2, retryCount) * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        
        exportRepository.save(export);
        
        // Audit log
        auditService.logPayrollExport(export, getCurrentUser());
    }
    
    private String formatForProvider(List<PayrollRecord> records, String provider) {
        switch (provider.toUpperCase()) {
            case "ADP":
                return formatForADP(records);
            case "PAYCHEX":
                return formatForPaychex(records);
            default:
                return formatAsCSV(records);
        }
    }
}
```

---

### EPIC E12 - Notifications & Announcements
**Section:** Notification System

**Description:** Implement a multi-channel notification system with user preferences, localized templates, delivery tracking, and announcement management.

**Design Specification:**
- Channels: In-app, email, SMS
- User preferences: Opt-in/opt-out per channel
- Templates: Localized message templates
- Quiet hours: Configurable no-notification periods
- Delivery tracking: Sent, delivered, failed statuses
- Rate limiting: Prevent notification spam
- Announcements: Dashboard-visible messages with expiration

**Sample Implementation:**
```java
// NotificationService.java
@Service
public class NotificationServiceImpl implements NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private NotificationPreferenceRepository preferenceRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SmsService smsService;
    
    @Value("${notification.quiet-hours.start}")
    private LocalTime quietHoursStart;
    
    @Value("${notification.quiet-hours.end}")
    private LocalTime quietHoursEnd;
    
    @Override
    public void sendNotification(NotificationDto dto) {
        Employee recipient = getEmployee(dto.getRecipientId());
        NotificationPreference prefs = preferenceRepository.findByEmployee(recipient)
            .orElse(getDefaultPreferences());
        
        // Check quiet hours
        if (isQuietHours() && !dto.isUrgent()) {
            // Queue for later delivery
            queueNotification(dto);
            return;
        }
        
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(dto.getType());
        notification.setSubject(dto.getSubject());
        notification.setMessage(localizeMessage(dto.getMessage(), recipient.getLocale()));
        notification.setCreatedAt(LocalDateTime.now());
        
        // In-app notification (always sent)
        notification.setInAppStatus(DeliveryStatus.SENT);
        notificationRepository.save(notification);
        
        // Email notification
        if (prefs.isEmailEnabled() && recipient.getEmail() != null) {
            try {
                emailService.sendEmail(recipient.getEmail(), notification.getSubject(), notification.getMessage());
                notification.setEmailStatus(DeliveryStatus.DELIVERED);
            } catch (Exception e) {
                notification.setEmailStatus(DeliveryStatus.FAILED);
                notification.setEmailError(e.getMessage());
            }
        }
        
        // SMS notification
        if (prefs.isSmsEnabled() && recipient.getPhoneNumber() != null) {
            try {
                smsService.sendSms(recipient.getPhoneNumber(), notification.getMessage());
                notification.setSmsStatus(DeliveryStatus.DELIVERED);
            } catch (Exception e) {
                notification.setSmsStatus(DeliveryStatus.FAILED);
                notification.setSmsError(e.getMessage());
            }
        }
        
        notificationRepository.save(notification);
    }
    
    @Override
    public Announcement createAnnouncement(AnnouncementDto dto) {
        Announcement announcement = new Announcement();
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setTargetRoles(dto.getTargetRoles());
        announcement.setTargetDepartments(dto.getTargetDepartments());
        announcement.setStartDate(dto.getStartDate());
        announcement.setEndDate(dto.getEndDate());
        announcement.setPriority(dto.getPriority());
        announcement.setCreatedBy(getCurrentUser());
        announcement.setCreatedAt(LocalDateTime.now());
        
        Announcement saved = announcementRepository.save(announcement);
        
        // Notify all targeted employees
        List<Employee> targetedEmployees = findTargetedEmployees(dto.getTargetRoles(), dto.getTargetDepartments());
        for (Employee employee : targetedEmployees) {
            NotificationDto notif = new NotificationDto();
            notif.setRecipientId(employee.getId());
            notif.setType(NotificationType.ANNOUNCEMENT);
            notif.setSubject(announcement.getTitle());
            notif.setMessage(announcement.getContent());
            sendNotification(notif);
        }
        
        return saved;
    }
    
    @Scheduled(cron = "0 0 * * * ?") // Every hour
    public void expireAnnouncements() {
        LocalDate today = LocalDate.now();
        List<Announcement> expired = announcementRepository.findByEndDateBeforeAndActiveTrue(today);
        
        for (Announcement announcement : expired) {
            announcement.setActive(false);
            announcementRepository.save(announcement);
        }
    }
    
    private boolean isQuietHours() {
        LocalTime now = LocalTime.now();
        return now.isAfter(quietHoursStart) && now.isBefore(quietHoursEnd);
    }
    
    private String localizeMessage(String message, String locale) {
        // Use MessageSource for localization
        return messageSource.getMessage(message, null, new Locale(locale));
    }
}
```

---

### EPIC E13 - Integration Layer (HRIS/WMS APIs)
**Section:** External System Integration

**Description:** Expose REST APIs and implement connectors for HRIS synchronization, WMS integration, IDP-based SSO, and webhook event delivery.

**Design Specification:**
- HRIS sync: Scheduled job for new hires/terminations
- WMS integration: Department/location mapping
- SSO: OAuth2/JWT with external IDP
- Webhooks: Event-driven notifications to external systems
- API security: JWT/OAuth2 authentication
- Idempotency: Prevent duplicate processing
- OpenAPI documentation: Complete API specs

**Sample Implementation:**
```java
// HRISIntegrationService.java
@Service
public class HRISIntegrationServiceImpl implements HRISIntegrationService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private EmployeeService employeeService;
    
    @Value("${hris.api.url}")
    private String hrisApiUrl;
    
    @Value("${hris.api.key}")
    private String hrisApiKey;
    
    @Scheduled(cron = "0 0 */4 * * ?") // Every 4 hours
    @Transactional
    public void syncEmployeesFromHRIS() {
        log.info("Starting HRIS employee sync");
        
        try {
            // Fetch employees from HRIS
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + hrisApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<HRISEmployeeResponse> response = restTemplate.exchange(
                hrisApiUrl + "/employees",
                HttpMethod.GET,
                entity,
                HRISEmployeeResponse.class
            );
            
            List<HRISEmployee> hrisEmployees = response.getBody().getEmployees();
            
            for (HRISEmployee hrisEmp : hrisEmployees) {
                // Check if employee exists
                Optional<Employee> existing = employeeRepository.findByBadgeIdAndDeletedFalse(hrisEmp.getBadgeId());
                
                if (existing.isPresent()) {
                    // Update existing employee
                    Employee employee = existing.get();
                    updateEmployeeFromHRIS(employee, hrisEmp);
                } else {
                    // Create new employee
                    createEmployeeFromHRIS(hrisEmp);
                }
            }
            
            log.info("HRIS sync completed successfully. Processed {} employees", hrisEmployees.size());
            
        } catch (Exception e) {
            log.error("HRIS sync failed", e);
            // Send alert to admin
            notificationService.sendIntegrationAlert("HRIS sync failed: " + e.getMessage());
        }
    }
    
    private void createEmployeeFromHRIS(HRISEmployee hrisEmp) {
        EmployeeDto dto = new EmployeeDto();
        dto.setName(hrisEmp.getFullName());
        dto.setBadgeId(hrisEmp.getBadgeId());
        dto.setRole(mapHRISRole(hrisEmp.getJobTitle()));
        dto.setHireDate(hrisEmp.getHireDate());
        dto.setStatus(EmployeeStatus.ACTIVE);
        
        employeeService.createEmployee(dto);
        log.info("Created new employee from HRIS: {}", hrisEmp.getBadgeId());
    }
    
    private void updateEmployeeFromHRIS(Employee employee, HRISEmployee hrisEmp) {
        boolean updated = false;
        
        if (!employee.getName().equals(hrisEmp.getFullName())) {
            employee.setName(hrisEmp.getFullName());
            updated = true;
        }
        
        if (hrisEmp.getTerminationDate() != null && employee.getStatus() == EmployeeStatus.ACTIVE) {
            employee.setStatus(EmployeeStatus.TERMINATED);
            employee.setTerminationDate(hrisEmp.getTerminationDate());
            updated = true;
        }
        
        if (updated) {
            employeeRepository.save(employee);
            log.info("Updated employee from HRIS: {}", employee.getBadgeId());
        }
    }
}
```

```java
// WebhookService.java
@Service
public class WebhookServiceImpl implements WebhookService {
    
    @Autowired
    private WebhookSubscriptionRepository subscriptionRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Async
    @Override
    public void sendWebhook(WebhookEvent event) {
        List<WebhookSubscription> subscriptions = subscriptionRepository
            .findByEventTypeAndActiveTrue(event.getEventType());
        
        for (WebhookSubscription subscription : subscriptions) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-Webhook-Signature", generateSignature(event, subscription.getSecret()));
                headers.set("X-Event-Type", event.getEventType());
                headers.set("X-Event-Id", event.getId().toString());
                
                HttpEntity<WebhookEvent> entity = new HttpEntity<>(event, headers);
                
                ResponseEntity<String> response = restTemplate.postForEntity(
                    subscription.getUrl(),
                    entity,
                    String.class
                );
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("Webhook delivered successfully to {}", subscription.getUrl());
                    subscription.setLastDeliveryAt(LocalDateTime.now());
                    subscription.setLastDeliveryStatus("SUCCESS");
                } else {
                    log.warn("Webhook delivery failed with status {}", response.getStatusCode());
                    subscription.setLastDeliveryStatus("FAILED: " + response.getStatusCode());
                }
                
            } catch (Exception e) {
                log.error("Webhook delivery failed to {}", subscription.getUrl(), e);
                subscription.setLastDeliveryStatus("ERROR: " + e.getMessage());
                subscription.setFailureCount(subscription.getFailureCount() + 1);
                
                // Disable after 5 consecutive failures
                if (subscription.getFailureCount() >= 5) {
                    subscription.setActive(false);
                    log.warn("Webhook subscription disabled due to repeated failures: {}", subscription.getUrl());
                }
            }
            
            subscriptionRepository.save(subscription);
        }
    }
    
    private String generateSignature(WebhookEvent event, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(event.toString().getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate webhook signature", e);
        }
    }
}
```

---

### EPIC E14 - Audit Trail & Compliance
**Section:** Audit Logging System

**Description:** Implement comprehensive audit logging for all sensitive operations with tamper-evident storage, export capabilities, and complete test coverage.

**Design Specification:**
- Logged operations: Create, update, delete on sensitive entities
- Audit fields: Actor, timestamp, before/after state, entity type
- Immutability: Append-only audit log table
- Export: Filter by date, user, entity type
- Retention: Configurable retention policy
- Test coverage: Validate all sensitive operations are logged

**Sample Implementation:**
```java
// AuditService.java
@Service
public class AuditServiceImpl implements AuditService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public void logCreate(Object entity, String actor) {
        AuditLog log = new AuditLog();
        log.setEntity(entity.getClass().getSimpleName());
        log.setEntityId(getEntityId(entity));
        log.setActor(actor);
        log.setAction("CREATE");
        log.setTimestamp(LocalDateTime.now());
        log.setBeforeState(null);
        log.setAfterState(serializeEntity(entity));
        
        auditLogRepository.save(log);
    }
    
    @Override
    public void logUpdate(Object before, Object after, String actor) {
        AuditLog log = new AuditLog();
        log.setEntity(after.getClass().getSimpleName());
        log.setEntityId(getEntityId(after));
        log.setActor(actor);
        log.setAction("UPDATE");
        log.setTimestamp(LocalDateTime.now());
        log.setBeforeState(serializeEntity(before));
        log.setAfterState(serializeEntity(after));
        
        auditLogRepository.save(log);
    }
    
    @Override
    public void logDelete(Object entity, String actor) {
        AuditLog log = new AuditLog();
        log.setEntity(entity.getClass().getSimpleName());
        log.setEntityId(getEntityId(entity));
        log.setActor(actor);
        log.setAction("DELETE");
        log.setTimestamp(LocalDateTime.now());
        log.setBeforeState(serializeEntity(entity));
        log.setAfterState(null);
        
        auditLogRepository.save(log);
    }
    
    @Override
    public List<AuditLog> exportAuditLogs(AuditLogFilter filter) {
        Specification<AuditLog> spec = AuditLogSpecification.withFilter(filter);
        return auditLogRepository.findAll(spec);
    }
    
    private String serializeEntity(Object entity) {
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize entity for audit log", e);
            return "[Serialization failed]";
        }
    }
    
    private Long getEntityId(Object entity) {
        try {
            Method getIdMethod = entity.getClass().getMethod("getId");
            return (Long) getIdMethod.invoke(entity);
        } catch (Exception e) {
            log.error("Failed to extract entity ID", e);
            return null;
        }
    }
}
```

```java
// AuditAspect.java - Automatic audit logging via AOP
@Aspect
@Component
public class AuditAspect {
    
    @Autowired
    private AuditService auditService;
    
    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void auditMethod(JoinPoint joinPoint, Auditable auditable, Object result) {
        String actor = SecurityContextHolder.getContext().getAuthentication().getName();
        String action = auditable.action();
        
        if ("CREATE".equals(action)) {
            auditService.logCreate(result, actor);
        } else if ("UPDATE".equals(action)) {
            Object[] args = joinPoint.getArgs();
            if (args.length >= 2) {
                auditService.logUpdate(args[0], result, actor);
            }
        } else if ("DELETE".equals(action)) {
            Object[] args = joinPoint.getArgs();
            if (args.length >= 1) {
                auditService.logDelete(args[0], actor);
            }
        }
    }
}
```

---

### EPIC E15 - Reporting & Analytics
**Section:** Reporting System

**Description:** Implement operational reports with filtering, export capabilities, role-based access, and metrics endpoints for BI integration.

**Design Specification:**
- Report types: Attendance, overtime, leave balances, certifications, safety KPIs
- Filters: Date range, department, shift, employee
- Export formats: CSV, PDF
- Performance: Sub-10s for 50k rows
- Dashboards: Role-based views
- Metrics API: RESTful endpoints for BI tools

**Sample Implementation:**
```java
// ReportingService.java
@Service
public class ReportingServiceImpl implements ReportingService {
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private LeaveRepository leaveRepository;
    
    @Autowired
    private CertificationRepository certificationRepository;
    
    @Autowired
    private SafetyIncidentRepository incidentRepository;
    
    @Override
    public AttendanceReport generateAttendanceReport(ReportFilter filter) {
        LocalDate startDate = filter.getStartDate();
        LocalDate endDate = filter.getEndDate();
        
        // Use native query for performance
        List<AttendanceSummary> summaries = attendanceRepository
            .findAttendanceSummary(startDate, endDate, filter.getDepartmentId(), filter.getShiftId());
        
        AttendanceReport report = new AttendanceReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setSummaries(summaries);
        report.setTotalHours(summaries.stream().mapToDouble(AttendanceSummary::getTotalHours).sum());
        report.setTotalOvertimeHours(summaries.stream().mapToDouble(AttendanceSummary::getOvertimeHours).sum());
        report.setGeneratedAt(LocalDateTime.now());
        
        return report;
    }
    
    @Override
    public byte[] exportAttendanceReportToCsv(ReportFilter filter) {
        AttendanceReport report = generateAttendanceReport(filter);
        
        StringBuilder csv = new StringBuilder();
        csv.append("Employee ID,Badge ID,Name,Department,Regular Hours,Overtime Hours,Total Hours
");
        
        for (AttendanceSummary summary : report.getSummaries()) {
            csv.append(String.format("%d,%s,%s,%s,%.2f,%.2f,%.2f
",
                summary.getEmployeeId(),
                summary.getBadgeId(),
                summary.getEmployeeName(),
                summary.getDepartment(),
                summary.getRegularHours(),
                summary.getOvertimeHours(),
                summary.getTotalHours()
            ));
        }
        
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    @Override
    public CertificationStatusReport generateCertificationReport() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysOut = today.plusDays(30);
        
        List<Certification> allCerts = certificationRepository.findAll();
        
        CertificationStatusReport report = new CertificationStatusReport();
        report.setTotalCertifications(allCerts.size());
        report.setValidCertifications(countByStatus(allCerts, CertificationStatus.VALID));
        report.setExpiredCertifications(countByStatus(allCerts, CertificationStatus.EXPIRED));
        report.setExpiringIn30Days(certificationRepository
            .findByExpiryDateBetweenAndStatus(today, thirtyDaysOut, CertificationStatus.VALID).size());
        report.setGeneratedAt(LocalDateTime.now());
        
        return report;
    }
    
    @Override
    public SafetyMetricsReport generateSafetyMetrics(LocalDate startDate, LocalDate endDate) {
        List<SafetyIncident> incidents = incidentRepository.findByIncidentDateBetween(startDate, endDate);
        
        SafetyMetricsReport report = new SafetyMetricsReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalIncidents(incidents.size());
        report.setIncidentsBySeverity(groupBySeverity(incidents));
        report.setIncidentsByLocation(groupByLocation(incidents));
        report.setOpenIncidents(countByStatus(incidents, IncidentStatus.OPEN));
        report.setResolvedIncidents(countByStatus(incidents, IncidentStatus.RESOLVED));
        report.setAverageResolutionDays(calculateAverageResolutionDays(incidents));
        report.setGeneratedAt(LocalDateTime.now());
        
        return report;
    }
}
```

---

### EPIC E16 - Mobile Access (PWA)
**Section:** Progressive Web Application

**Description:** Implement responsive mobile views with offline capabilities for core workflows including clock-in/out, schedule viewing, leave requests, and announcements.

**Design Specification:**
- PWA manifest: Installable app
- Offline support: Service worker for caching
- Responsive design: Mobile-first approach
- Offline queue: Store clock events locally
- Conflict resolution: Sync when online
- Lighthouse score: Target 90+ PWA score

**Sample Implementation:**
```javascript
// service-worker.js
const CACHE_NAME = 'warehouse-ems-v1';
const urlsToCache = [
  '/',
  '/css/main.css',
  '/js/app.js',
  '/offline.html'
];

self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(urlsToCache))
  );
});

self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request)
      .then(response => {
        if (response) {
          return response;
        }
        return fetch(event.request).then(response => {
          if (!response || response.status !== 200) {
            return response;
          }
          const responseToCache = response.clone();
          caches.open(CACHE_NAME)
            .then(cache => {
              cache.put(event.request, responseToCache);
            });
          return response;
        });
      })
      .catch(() => caches.match('/offline.html'))
  );
});

// Handle background sync for offline clock events
self.addEventListener('sync', event => {
  if (event.tag === 'sync-attendance') {
    event.waitUntil(syncAttendanceEvents());
  }
});

async function syncAttendanceEvents() {
  const db = await openDB();
  const events = await db.getAll('pending-attendance');
  
  for (const event of events) {
    try {
      const response = await fetch('/api/attendance/clock-in', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(event)
      });
      
      if (response.ok) {
        await db.delete('pending-attendance', event.id);
      }
    } catch (error) {
      console.error('Failed to sync attendance event', error);
    }
  }
}
```

```json
// manifest.json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "description": "Warehouse Employee Management System",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#2196F3",
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

---

### EPIC E17 - Onboarding & Offboarding Workflow
**Section:** Employee Lifecycle Management

**Description:** Automate employee onboarding and offboarding processes including account provisioning, training assignment, asset allocation, and access revocation.

**Design Specification:**
- Onboarding: Auto-provision from HRIS sync
- Training tasks: Assign required certifications
- Asset assignment: Generate equipment checkout tasks
- Initial schedule: Create default shift assignments
- Offboarding: Revoke access, collect assets, update schedules
- Task tracking: Monitor completion status

**Sample Implementation:**
```java
// OnboardingService.java
@Service
@Transactional
public class OnboardingServiceImpl implements OnboardingService {
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private CertificationService certificationService;
    
    @Autowired
    private AssetService assetService;
    
    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private TaskService taskService;
    
    @Override
    public OnboardingWorkflow initiateOnboarding(Employee employee) {
        OnboardingWorkflow workflow = new OnboardingWorkflow();
        workflow.setEmployee(employee);
        workflow.setStatus(WorkflowStatus.IN_PROGRESS);
        workflow.setStartedAt(LocalDateTime.now());
        
        // Create onboarding tasks
        List<OnboardingTask> tasks = new ArrayList<>();
        
        // 1. Account setup
        tasks.add(createTask("Account Setup", "Create user account and assign initial role", 1));
        
        // 2. Required training
        List<String> requiredCerts = getRequiredCertifications(employee.getRole());
        for (String cert : requiredCerts) {
            tasks.add(createTask(
                "Complete " + cert + " Training",
                "Obtain required certification: " + cert,
                2
            ));
        }
        
        // 3. Asset assignment
        List<String> requiredAssets = getRequiredAssets(employee.getRole());
        for (String asset : requiredAssets) {
            tasks.add(createTask(
                "Assign " + asset,
                "Checkout required equipment: " + asset,
                3
            ));
        }
        
        // 4. Initial schedule
        tasks.add(createTask(
            "Create Initial Schedule",
            "Assign employee to default shift group",
            4
        ));
        
        workflow.setTasks(tasks);
        workflow = onboardingRepository.save(workflow);
        
        // Auto-complete account setup
        completeAccountSetup(employee);
        
        // Notify supervisor
        notificationService.sendOnboardingNotification(employee.getSupervisor(), workflow);
        
        return workflow;
    }
    
    @Override
    public OffboardingWorkflow initiateOffboarding(Employee employee, LocalDate terminationDate) {
        OffboardingWorkflow workflow = new OffboardingWorkflow();
        workflow.setEmployee(employee);
        workflow.setTerminationDate(terminationDate);
        workflow.setStatus(WorkflowStatus.IN_PROGRESS);
        workflow.setStartedAt(LocalDateTime.now());
        
        List<OffboardingTask> tasks = new ArrayList<>();
        
        // 1. Revoke system access
        tasks.add(createOffboardingTask(
            "Revoke System Access",
            "Disable user account and revoke all permissions",
            1
        ));
        
        // 2. Collect assets
        List<AssetAssignment> activeAssignments = assetService.getActiveAssignments(employee);
        for (AssetAssignment assignment : activeAssignments) {
            tasks.add(createOffboardingTask(
                "Collect " + assignment.getAsset().getAssetType(),
                "Return asset: " + assignment.getAsset().getSerialNumber(),
                2
            ));
        }
        
        // 3. Cancel future schedules
        tasks.add(createOffboardingTask(
            "Cancel Future Schedules",
            "Remove all shift assignments after termination date",
            3
        ));
        
        // 4. Exit interview
        tasks.add(createOffboardingTask(
            "Conduct Exit Interview",
            "Complete exit interview and documentation",
            4
        ));
        
        workflow.setTasks(tasks);
        workflow = offboardingRepository.save(workflow);
        
        // Auto-execute immediate tasks
        revokeSystemAccess(employee);
        cancelFutureSchedules(employee, terminationDate);
        
        // Notify HR and supervisor
        notificationService.sendOffboardingNotification(employee.getSupervisor(), workflow);
        
        return workflow;
    }
    
    private void revokeSystemAccess(Employee employee) {
        employee.setStatus(EmployeeStatus.TERMINATED);
        employee.setAccountEnabled(false);
        employeeRepository.save(employee);
        
        auditService.logUpdate(employee, employee, "SYSTEM");
    }
    
    private void cancelFutureSchedules(Employee employee, LocalDate terminationDate) {
        List<ScheduleAssignment> futureSchedules = scheduleRepository
            .findByEmployeeAndStartDateAfter(employee, terminationDate);
        
        for (ScheduleAssignment schedule : futureSchedules) {
            schedule.setCancelled(true);
            schedule.setCancellationReason("Employee termination");
            scheduleRepository.save(schedule);
        }
    }
}
```

---

### EPIC E18 - Localization & Multi-Tenant
**Section:** Internationalization & Multi-Tenancy

**Description:** Support multiple locales and tenant isolation for global warehouse operations with region-specific configurations.

**Design Specification:**
- Locale support: Date/time/currency/language formatting
- Tenant isolation: Data segregation per warehouse/region
- Tenant context: Automatic filtering in all queries
- Configuration: Tenant-specific settings
- Translations: UI string localization
- Tenant management: CRUD operations for tenants

**Sample Implementation:**
```java
// TenantContext.java
public class TenantContext {
    private static final ThreadLocal<Long> currentTenant = new ThreadLocal<>();
    
    public static void setTenantId(Long tenantId) {
        currentTenant.set(tenantId);
    }
    
    public static Long getTenantId() {
        return currentTenant.get();
    }
    
    public static void clear() {
        currentTenant.remove();
    }
}
```

```java
// TenantFilter.java
@Component
public class TenantFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String tenantId = httpRequest.getHeader("X-Tenant-ID");
        
        if (tenantId != null) {
            TenantContext.setTenantId(Long.parseLong(tenantId));
        }
        
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
```

```java
// TenantAwareRepository.java
public interface TenantAwareRepository<T> extends JpaRepository<T, Long> {
    
    @Override
    @Query("SELECT e FROM #{#entityName} e WHERE e.tenantId = :#{T(com.warehouse.ems.security.TenantContext).getTenantId()}")
    List<T> findAll();
    
    @Override
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.tenantId = :#{T(com.warehouse.ems.security.TenantContext).getTenantId()}")
    Optional<T> findById(Long id);
}
```

---

### EPIC E19 - Disaster Recovery & Backup
**Section:** Business Continuity

**Description:** Implement automated backup, point-in-time restore, and documented disaster recovery procedures.

**Design Specification:**
- Backup frequency: Daily automated backups
- Backup retention: 30 days rolling
- Point-in-time restore: Restore to any backup point
- DR runbook: Step-by-step recovery procedures
- RTO/RPO targets: 4 hours RTO, 24 hours RPO
- Backup monitoring: Alert on failures

**Sample Implementation:**
```java
// BackupService.java
@Service
public class BackupServiceImpl implements BackupService {
    
    @Value("${backup.storage.path}")
    private String backupStoragePath;
    
    @Autowired
    private DataSource dataSource;
    
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    @Transactional(readOnly = true)
    public void performDailyBackup() {
        log.info("Starting daily database backup");
        
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = String.format("warehouse_ems_backup_%s.sql", timestamp);
            String backupFilePath = Paths.get(backupStoragePath, backupFileName).toString();
            
            // Execute mysqldump
            ProcessBuilder processBuilder = new ProcessBuilder(
                "mysqldump",
                "-u", "ems_user",
                "-p" + "password",
                "--single-transaction",
                "--routines",
                "--triggers",
                "warehouse_ems",
                "--result-file=" + backupFilePath
            );
            
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                log.info("Backup completed successfully: {}", backupFileName);
                
                // Compress backup
                compressBackup(backupFilePath);
                
                // Upload to cloud storage
                uploadToCloudStorage(backupFilePath + ".gz");
                
                // Record backup metadata
                recordBackupMetadata(backupFileName, backupFilePath + ".gz");
                
                // Clean up old backups
                cleanupOldBackups();
                
            } else {
                log.error("Backup failed with exit code: {}", exitCode);
                notificationService.sendBackupFailureAlert("Backup failed with exit code: " + exitCode);
            }
            
        } catch (Exception e) {
            log.error("Backup failed", e);
            notificationService.sendBackupFailureAlert("Backup failed: " + e.getMessage());
        }
    }
    
    @Override
    public void restoreFromBackup(String backupFileName) {
        log.info("Starting database restore from backup: {}", backupFileName);
        
        try {
            String backupFilePath = Paths.get(backupStoragePath, backupFileName).toString();
            
            // Download from cloud storage if needed
            if (!Files.exists(Paths.get(backupFilePath))) {
                downloadFromCloudStorage(backupFileName);
            }
            
            // Decompress backup
            String decompressedPath = decompressBackup(backupFilePath);
            
            // Execute mysql restore
            ProcessBuilder processBuilder = new ProcessBuilder(
                "mysql",
                "-u", "ems_user",
                "-p" + "password",
                "warehouse_ems",
                "<", decompressedPath
            );
            
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                log.info("Restore completed successfully from: {}", backupFileName);
            } else {
                log.error("Restore failed with exit code: {}", exitCode);
                throw new RestoreFailedException("Restore failed with exit code: " + exitCode);
            }
            
        } catch (Exception e) {
            log.error("Restore failed", e);
            throw new RestoreFailedException("Restore failed: " + e.getMessage(), e);
        }
    }
    
    private void cleanupOldBackups() {
        LocalDate cutoffDate = LocalDate.now().minusDays(30);
        List<BackupMetadata> oldBackups = backupRepository.findByCreatedAtBefore(cutoffDate.atStartOfDay());
        
        for (BackupMetadata backup : oldBackups) {
            try {
                Files.deleteIfExists(Paths.get(backup.getFilePath()));
                backupRepository.delete(backup);
                log.info("Deleted old backup: {}", backup.getFileName());
            } catch (IOException e) {
                log.error("Failed to delete old backup: {}", backup.getFileName(), e);
            }
        }
    }
}
```

---

### EPIC E20 - Performance & Scalability
**Section:** Performance Optimization

**Description:** Implement caching, database indexing, async processing, and load testing to ensure system performance at scale.

**Design Specification:**
- Load testing: 10k employees, 50k attendance records/day
- Caching: Redis for frequently accessed data
- Database indexing: Optimize query performance
- Async processing: Background jobs for heavy operations
- Connection pooling: HikariCP configuration
- Query optimization: N+1 prevention, batch operations

**Sample Implementation:**
```java
// CacheConfig.java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
```

```java
// EmployeeService with caching
@Service
public class EmployeeServiceImpl implements EmployeeService {
    
    @Cacheable(value = "employees", key = "#id")
    @Override
    public Employee getEmployee(Long id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
    }
    
    @CacheEvict(value = "employees", key = "#id")
    @Override
    public Employee updateEmployee(Long id, EmployeeDto dto) {
        // Update logic
    }
}
```

```yaml
# application.yml - Performance tuning
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
```

```sql
-- Database indexes for performance
CREATE INDEX idx_employee_badge_id ON employees(badge_id);
CREATE INDEX idx_employee_department ON employees(department_id);
CREATE INDEX idx_attendance_employee_date ON attendance(employee_id, clock_in_time);
CREATE INDEX idx_attendance_shift ON attendance(shift_id);
CREATE INDEX idx_leave_employee_date ON leaves(employee_id, start_date, end_date);
CREATE INDEX idx_certification_employee ON certifications(employee_id);
CREATE INDEX idx_certification_expiry ON certifications(expiry_date, status);
CREATE INDEX idx_audit_entity ON audit_logs(entity, entity_id);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
```

---

## Conclusion

This comprehensive low-level technical design document provides complete specifications for implementing the Warehouse Employee Management System using Spring Boot. It covers all 20 epics with detailed entity designs, service implementations, REST API specifications, security configurations, database schemas, integration patterns, and code samples.

The design adheres to Spring Boot best practices including:
- Layered architecture (Controller â Service â Repository)
- JPA entity relationships and annotations
- Spring Security RBAC implementation
- RESTful API design with OpenAPI documentation
- Database migration management with Flyway
- Comprehensive audit logging
- Multi-channel notification system
- External system integration
- Performance optimization with caching and indexing
- Mobile PWA support
- Multi-tenant architecture
- Disaster recovery and backup strategies

This document serves as a complete blueprint for development teams to implement the entire system with confidence in architectural soundness and scalability.

---

**Document Version:** 1.0
**Last Updated:** 2024
**Status:** Production Ready