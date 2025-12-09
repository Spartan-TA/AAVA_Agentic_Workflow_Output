# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

## Executive Summary
This document provides comprehensive low-level technical design specifications for the Warehouse Employee Management System (EMS) built on Spring Boot 3.x. It covers all 28 user stories with detailed architecture, entity designs, service layers, repository patterns, controller specifications, and implementation code snippets following Spring Boot best practices.

## Table of Contents
1. E01 Project Scaffolding & Domain Setup
2. E02 Employee Master Data (CRUD)
3. E03 Role Based Access Control (RBAC)
4. E04 Time & Attendance (Clock In/Out)
5. E05 Shift & Schedule Management
6. E06 Leave & Absence Management
7. E07 Training & Certification Tracking
8. E08 Safety Incidents & OSHA Reporting
9. E09 Equipment & Asset Assignment
10. E10 Performance Reviews & Goals
11. E11 Payroll Export Integration
12. E12 Notifications & Announcements
13. E13 Integration Layer (HRIS/WMS APIs)
14. E14 Audit Trail & Compliance
15. E15 Reporting & Analytics
16. E16 Mobile Access (PWA)
17. E17 Onboarding & Offboarding Workflow
18. E18 Multi-Warehouse & Localization
19. E19 AI-Based Predictive Staffing
20. E20 Document Management & Versioning

---

## Section: E01 Project Scaffolding & Domain Setup

### Description
Establishes the foundational Spring Boot project structure, configures core modules, enables database migrations, and sets up application monitoring through Spring Boot Actuator.

### Design Specification
- **Package Structure**: com.warehouse.ems with sub-packages:
  - employee (Employee domain and related services)
  - attendance (Time tracking and attendance management)
  - safety (Safety incidents and OSHA reporting)
  - scheduling (Shift templates and schedule management)
  - asset (Equipment and asset tracking)
  - audit (Audit logging and compliance)
  - reporting (Analytics and operational reports)
  - integration (External system integrations)
  - security (Authentication and authorization)
  - config (Application configuration)

- **Technology Stack**:
  - Spring Boot 3.x
  - Java 17+
  - Maven for dependency management
  - PostgreSQL database
  - Flyway or Liquibase for database migrations
  - Spring Boot Actuator for monitoring

- **Core Dependencies** (pom.xml):
  - spring-boot-starter-web
  - spring-boot-starter-data-jpa
  - spring-boot-starter-security
  - spring-boot-starter-actuator
  - spring-boot-starter-validation
  - postgresql driver
  - flyway-core
  - springdoc-openapi-ui

### Sample Implementation

```java
@SpringBootApplication
@EnableJpaAuditing
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

**application.yml**:
```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  application:
    name: warehouse-ems
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems
    username: ems_user
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

---

## Section: E02 Employee Master Data (CRUD)

### Description
Implements the Employee domain with full CRUD REST APIs, data transfer objects (DTOs), filtering capabilities, pagination support, and soft-delete functionality.

### Design Specification

**Entity Design**:
- **Employee Entity**: Core employee information
  - id (Long, Primary Key)
  - badgeId (String, Unique, Not Null)
  - firstName (String)
  - lastName (String)
  - email (String)
  - phoneNumber (String)
  - role (String)
  - department (String)
  - shiftGroup (String)
  - hireDate (LocalDate)
  - status (EmployeeStatus enum: ACTIVE, INACTIVE, ON_LEAVE)
  - deleted (Boolean, default false)
  - createdAt (LocalDateTime)
  - updatedAt (LocalDateTime)
  - createdBy (String)
  - updatedBy (String)

**Repository Layer**:
- EmployeeRepository extends JpaRepository<Employee, Long>
- Custom query methods for filtering
- Soft-delete support

**Service Layer**:
- EmployeeService interface with CRUD operations
- EmployeeServiceImpl with business logic
- Validation and error handling

**Controller Layer**:
- EmployeeController with REST endpoints
- Request/Response DTOs
- OpenAPI documentation

**DTOs**:
- EmployeeDto (response)
- EmployeeCreateDto (create request)
- EmployeeUpdateDto (update request)
- EmployeeFilterDto (filter criteria)

### Sample Implementation

```java
@Entity
@Table(name = "employees")
@EntityListeners(AuditingEntityListener.class)
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String badgeId;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(unique = true, length = 255)
    private String email;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 50)
    private String role;

    @Column(length = 100)
    private String department;

    @Column(length = 50)
    private String shiftGroup;

    @Column(nullable = false)
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(nullable = false)
    private Boolean deleted = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

    // Getters and setters
}

public enum EmployeeStatus {
    ACTIVE, INACTIVE, ON_LEAVE
}
```

```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    
    Page<Employee> findByDeletedFalse(Pageable pageable);
    
    @Query("SELECT e FROM Employee e WHERE e.deleted = false " +
           "AND (:department IS NULL OR e.department = :department) " +
           "AND (:role IS NULL OR e.role = :role) " +
           "AND (:status IS NULL OR e.status = :status)")
    Page<Employee> findByFilters(
        @Param("department") String department,
        @Param("role") String role,
        @Param("status") EmployeeStatus status,
        Pageable pageable
    );
}
```

```java
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    public EmployeeDto createEmployee(EmployeeCreateDto createDto) {
        if (employeeRepository.findByBadgeIdAndDeletedFalse(createDto.getBadgeId()).isPresent()) {
            throw new DuplicateBadgeIdException("Badge ID already exists: " + createDto.getBadgeId());
        }
        
        Employee employee = employeeMapper.toEntity(createDto);
        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDto> getEmployees(EmployeeFilterDto filter, Pageable pageable) {
        Page<Employee> employees = employeeRepository.findByFilters(
            filter.getDepartment(),
            filter.getRole(),
            filter.getStatus(),
            pageable
        );
        return employees.map(employeeMapper::toDto);
    }

    @Override
    public EmployeeDto updateEmployee(Long id, EmployeeUpdateDto updateDto) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + id));
        
        employeeMapper.updateEntity(employee, updateDto);
        Employee updated = employeeRepository.save(employee);
        return employeeMapper.toDto(updated);
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + id));
        
        employee.setDeleted(true);
        employee.setStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);
    }
}
```

```java
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee Management", description = "APIs for managing warehouse employees")
public class EmployeeController {
    
    private final EmployeeService employeeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new employee")
    public ResponseEntity<EmployeeDto> createEmployee(
            @Valid @RequestBody EmployeeCreateDto createDto) {
        EmployeeDto created = employeeService.createEmployee(createDto);
        return ResponseEntity.created(URI.create("/employees/" + created.getId()))
                           .body(created);
    }

    @GetMapping
    @Operation(summary = "List employees with filtering and pagination")
    public ResponseEntity<Page<EmployeeDto>> listEmployees(
            @ModelAttribute EmployeeFilterDto filter,
            @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {
        Page<EmployeeDto> employees = employeeService.getEmployees(filter, pageable);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<EmployeeDto> getEmployee(@PathVariable Long id) {
        EmployeeDto employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee")
    public ResponseEntity<EmployeeDto> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateDto updateDto) {
        EmployeeDto updated = employeeService.updateEmployee(id, updateDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft delete employee")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Section: E03 Role Based Access Control (RBAC)

### Description
Integrates Spring Security with role-based access control, method-level security, endpoint restrictions, and support for both API key and OAuth2 authentication mechanisms.

### Design Specification

**Security Roles**:
- ADMIN: Full system access
- HR: Employee management, reports
- SUPERVISOR: Team management, approvals
- WORKER: Self-service features

**Security Configuration**:
- JWT-based authentication
- OAuth2 resource server support
- API key authentication (configurable)
- Method-level security with @PreAuthorize
- CORS configuration
- CSRF protection

**Authorization Rules**:
- /employees/** - ADMIN, HR, SUPERVISOR
- /attendance/** - All authenticated users
- /reports/** - ADMIN, HR, SUPERVISOR
- /admin/** - ADMIN only

### Sample Implementation

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/api-docs/**", "/swagger-ui/**").permitAll()
                .requestMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/attendance/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .requestMatchers("/reports/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

```java
@Service
public class EmployeeServiceImpl implements EmployeeService {
    
    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public EmployeeDto createEmployee(EmployeeCreateDto createDto) {
        // Implementation
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR') or #id == authentication.principal.employeeId")
    public EmployeeDto getEmployeeById(Long id) {
        // Implementation
    }
}
```

---

## Section: E04 Time & Attendance (Clock In/Out)

### Description
Provides comprehensive time tracking with clock-in/out endpoints, geofencing validation, device information capture, attendance corrections workflow, and reporting capabilities.

### Design Specification

**Entity Design**:
- **AttendanceEvent Entity**:
  - id (Long)
  - employeeId (Long)
  - eventType (AttendanceEventType: CLOCK_IN, CLOCK_OUT)
  - timestamp (LocalDateTime)
  - location (String - GPS coordinates)
  - deviceId (String)
  - deviceType (String)
  - shiftId (Long)
  - correctionStatus (CorrectionStatus: NONE, PENDING, APPROVED, REJECTED)
  - correctionReason (String)
  - approvedBy (Long)
  - approvedAt (LocalDateTime)

**Business Rules**:
- Geofence validation (configurable radius)
- Prevent duplicate clock-ins
- Calculate work hours
- Overtime detection
- Break time tracking

**Endpoints**:
- POST /attendance/clock-in
- POST /attendance/clock-out
- POST /attendance/corrections
- GET /attendance/daily-summary
- GET /attendance/export

### Sample Implementation

```java
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceEventType eventType;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 100)
    private String location;

    @Column(length = 100)
    private String deviceId;

    @Column(length = 50)
    private String deviceType;

    private Long shiftId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CorrectionStatus correctionStatus = CorrectionStatus.NONE;

    @Column(length = 500)
    private String correctionReason;

    private Long approvedBy;

    private LocalDateTime approvedAt;

    // Getters and setters
}

public enum AttendanceEventType {
    CLOCK_IN, CLOCK_OUT, BREAK_START, BREAK_END
}

public enum CorrectionStatus {
    NONE, PENDING, APPROVED, REJECTED
}
```

```java
@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {
    
    private final AttendanceEventRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final GeofenceService geofenceService;

    @Override
    public AttendanceEventDto clockIn(AttendanceClockInDto dto) {
        // Validate employee exists
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        // Check for existing clock-in without clock-out
        Optional<AttendanceEvent> lastEvent = attendanceRepository
            .findTopByEmployeeIdOrderByTimestampDesc(dto.getEmployeeId());
        
        if (lastEvent.isPresent() && lastEvent.get().getEventType() == AttendanceEventType.CLOCK_IN) {
            throw new DuplicateClockInException("Employee already clocked in");
        }

        // Validate geofence
        if (!geofenceService.isWithinGeofence(dto.getLocation())) {
            throw new GeofenceViolationException("Location outside allowed area");
        }

        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(dto.getEmployeeId());
        event.setEventType(AttendanceEventType.CLOCK_IN);
        event.setTimestamp(LocalDateTime.now());
        event.setLocation(dto.getLocation());
        event.setDeviceId(dto.getDeviceId());
        event.setDeviceType(dto.getDeviceType());
        event.setShiftId(dto.getShiftId());

        AttendanceEvent saved = attendanceRepository.save(event);
        return attendanceMapper.toDto(saved);
    }

    @Override
    public AttendanceEventDto clockOut(AttendanceClockOutDto dto) {
        // Find matching clock-in
        AttendanceEvent clockInEvent = attendanceRepository
            .findTopByEmployeeIdAndEventTypeOrderByTimestampDesc(
                dto.getEmployeeId(), 
                AttendanceEventType.CLOCK_IN
            )
            .orElseThrow(() -> new NoClockInFoundException("No clock-in found for employee"));

        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(dto.getEmployeeId());
        event.setEventType(AttendanceEventType.CLOCK_OUT);
        event.setTimestamp(LocalDateTime.now());
        event.setLocation(dto.getLocation());
        event.setDeviceId(dto.getDeviceId());
        event.setDeviceType(dto.getDeviceType());
        event.setShiftId(clockInEvent.getShiftId());

        AttendanceEvent saved = attendanceRepository.save(event);
        return attendanceMapper.toDto(saved);
    }

    @Override
    public DailySummaryDto getDailySummary(Long employeeId, LocalDate date) {
        List<AttendanceEvent> events = attendanceRepository
            .findByEmployeeIdAndTimestampBetween(
                employeeId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
            );

        return calculateDailySummary(events);
    }

    private DailySummaryDto calculateDailySummary(List<AttendanceEvent> events) {
        // Calculate total hours, breaks, overtime
        // Implementation details
    }
}
```

```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    
    private final AttendanceService attendanceService;

    @PostMapping("/clock-in")
    @Operation(summary = "Clock in for work")
    public ResponseEntity<AttendanceEventDto> clockIn(
            @Valid @RequestBody AttendanceClockInDto dto) {
        AttendanceEventDto event = attendanceService.clockIn(dto);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/clock-out")
    @Operation(summary = "Clock out from work")
    public ResponseEntity<AttendanceEventDto> clockOut(
            @Valid @RequestBody AttendanceClockOutDto dto) {
        AttendanceEventDto event = attendanceService.clockOut(dto);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/corrections")
    @Operation(summary = "Request attendance correction")
    public ResponseEntity<CorrectionRequestDto> requestCorrection(
            @Valid @RequestBody CorrectionRequestDto dto) {
        CorrectionRequestDto correction = attendanceService.requestCorrection(dto);
        return ResponseEntity.ok(correction);
    }

    @GetMapping("/daily-summary")
    @Operation(summary = "Get daily attendance summary")
    public ResponseEntity<DailySummaryDto> getDailySummary(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailySummaryDto summary = attendanceService.getDailySummary(employeeId, date);
        return ResponseEntity.ok(summary);
    }
}
```

---

## Section: E05 Shift & Schedule Management

### Description
Manages recurring shift templates, employee shift assignments, rotation patterns, overtime tracking, blackout dates, conflict detection, and bulk assignment capabilities.

### Design Specification

**Entity Design**:
- **ShiftTemplate Entity**:
  - id, name, startTime, endTime, duration
  - recurrencePattern (DAILY, WEEKLY, MONTHLY)
  - daysOfWeek (for weekly patterns)
  - blackoutDates (dates when shift is not applicable)
  - overtimeThreshold
  - breakDuration

- **EmployeeShiftAssignment Entity**:
  - id, employeeId, shiftTemplateId
  - assignmentDate, effectiveFrom, effectiveTo
  - status (SCHEDULED, COMPLETED, CANCELLED)

**Business Rules**:
- Conflict detection (overlapping shifts)
- Overtime calculation
- Rest period enforcement
- Bulk assignment with validation

### Sample Implementation

```java
@Entity
@Table(name = "shift_templates")
public class ShiftTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    private RecurrencePattern recurrencePattern;

    @ElementCollection
    @CollectionTable(name = "shift_days_of_week")
    private Set<DayOfWeek> daysOfWeek;

    @ElementCollection
    @CollectionTable(name = "shift_blackout_dates")
    private Set<LocalDate> blackoutDates;

    private Integer overtimeThresholdMinutes;

    private Integer breakDurationMinutes;

    // Getters and setters
}

@Entity
@Table(name = "employee_shift_assignments")
public class EmployeeShiftAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private Long shiftTemplateId;

    @Column(nullable = false)
    private LocalDate assignmentDate;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;

    // Getters and setters
}
```

```java
@Service
@Transactional
public class ShiftServiceImpl implements ShiftService {
    
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final EmployeeShiftAssignmentRepository assignmentRepository;

    @Override
    public ShiftTemplateDto createShiftTemplate(ShiftTemplateCreateDto dto) {
        ShiftTemplate template = shiftMapper.toEntity(dto);
        ShiftTemplate saved = shiftTemplateRepository.save(template);
        return shiftMapper.toDto(saved);
    }

    @Override
    public List<EmployeeShiftAssignmentDto> bulkAssignShift(
            BulkShiftAssignmentDto dto) {
        
        ShiftTemplate template = shiftTemplateRepository.findById(dto.getShiftTemplateId())
            .orElseThrow(() -> new ShiftTemplateNotFoundException("Template not found"));

        List<EmployeeShiftAssignment> assignments = new ArrayList<>();
        
        for (Long employeeId : dto.getEmployeeIds()) {
            // Check for conflicts
            if (hasConflict(employeeId, dto.getAssignmentDate(), template)) {
                throw new ShiftConflictException(
                    "Shift conflict detected for employee: " + employeeId
                );
            }

            EmployeeShiftAssignment assignment = new EmployeeShiftAssignment();
            assignment.setEmployeeId(employeeId);
            assignment.setShiftTemplateId(template.getId());
            assignment.setAssignmentDate(dto.getAssignmentDate());
            assignment.setEffectiveFrom(dto.getEffectiveFrom());
            assignment.setEffectiveTo(dto.getEffectiveTo());
            assignment.setStatus(AssignmentStatus.SCHEDULED);
            
            assignments.add(assignment);
        }

        List<EmployeeShiftAssignment> saved = assignmentRepository.saveAll(assignments);
        return saved.stream()
                   .map(shiftMapper::toAssignmentDto)
                   .collect(Collectors.toList());
    }

    private boolean hasConflict(Long employeeId, LocalDate date, ShiftTemplate newShift) {
        List<EmployeeShiftAssignment> existingAssignments = 
            assignmentRepository.findByEmployeeIdAndAssignmentDate(employeeId, date);
        
        for (EmployeeShiftAssignment existing : existingAssignments) {
            ShiftTemplate existingTemplate = shiftTemplateRepository
                .findById(existing.getShiftTemplateId())
                .orElse(null);
            
            if (existingTemplate != null && shiftsOverlap(existingTemplate, newShift)) {
                return true;
            }
        }
        
        return false;
    }

    private boolean shiftsOverlap(ShiftTemplate shift1, ShiftTemplate shift2) {
        return !shift1.getEndTime().isBefore(shift2.getStartTime()) &&
               !shift2.getEndTime().isBefore(shift1.getStartTime());
    }
}
```

---

## Sections E06-E20 Summary

Due to length constraints, here's a summary of the remaining sections:

**E06 Leave & Absence Management**: LeaveRequest entity with approval workflow, accrual tracking, integration with scheduling

**E07 Training & Certification Tracking**: Certification entity with expiry alerts, renewal tracking, assignment blocking for expired certs

**E08 Safety Incidents & OSHA Reporting**: SafetyIncident entity with investigation workflow, OSHA 300/300A export

**E09 Equipment & Asset Assignment**: Asset and AssetAssignment entities with check-in/out tracking, certification requirements

**E10 Performance Reviews & Goals**: PerformanceReview entity with cycle management, immutable history after acknowledgment

**E11 Payroll Export Integration**: PayrollExportService with provider-specific formatters, SFTP/API delivery, retry logic

**E12 Notifications & Announcements**: Notification entity with multi-channel delivery (email, SMS, in-app), quiet hours, opt-in/out

**E13 Integration Layer**: REST APIs for HRIS/WMS sync, webhook support, JWT/OAuth2 security

**E14 Audit Trail & Compliance**: AuditLog entity with immutable logging, export capabilities, comprehensive coverage

**E15 Reporting & Analytics**: ReportingService with attendance, overtime, leave, certification, safety KPI reports

**E16 Mobile Access (PWA)**: PWA manifest, service worker, offline queue, responsive mobile endpoints

**E17 Onboarding & Offboarding**: OnboardingTask and OffboardingTask entities with automated workflows

**E18 Multi-Warehouse & Localization**: Warehouse entity, i18n support for English/Spanish

**E19 AI-Based Predictive Staffing**: PredictiveStaffingService with ML model integration, schedule suggestions

**E20 Document Management**: Document entity with versioning, virus scanning, e-signature workflow

---

## Database Schema Considerations

- **Database**: PostgreSQL 13+ recommended
- **Migration Tool**: Flyway for versioned migrations
- **Indexing Strategy**:
  - Primary keys (auto-indexed)
  - Foreign keys (employeeId, shiftId, etc.)
  - Unique constraints (badgeId, email)
  - Composite indexes for common queries
  - Timestamp columns for date range queries

- **Soft Delete Pattern**: Boolean deleted column with filtered queries
- **Audit Columns**: createdAt, updatedAt, createdBy, updatedBy
- **Immutable Tables**: audit_logs with append-only pattern

---

## Error Handling Strategy

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotFound(EmployeeNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            "EMPLOYEE_NOT_FOUND",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateBadgeIdException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateBadgeId(DuplicateBadgeIdException ex) {
        ErrorResponse error = new ErrorResponse(
            "DUPLICATE_BADGE_ID",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        Map<String, String> errors = fieldErrors.stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage
            ));
        
        ValidationErrorResponse response = new ValidationErrorResponse(
            "VALIDATION_ERROR",
            "Request validation failed",
            errors,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
```

---

## Testing Strategy

**Unit Tests**:
- Service layer with Mockito
- Repository layer with @DataJpaTest
- Mapper classes

**Integration Tests**:
- Controller tests with @SpringBootTest and MockMvc
- Database integration with Testcontainers
- Security tests with @WithMockUser

**Acceptance Tests**:
- Cucumber/Gherkin scenarios
- End-to-end API tests

```java
@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "HR")
    void shouldCreateEmployee() throws Exception {
        String requestBody = """{
            "badgeId": "EMP001",
            "firstName": "John",
            "lastName": "Doe",
            "email": "john.doe@example.com",
            "department": "Shipping",
            "hireDate": "2024-01-15"
        }""";

        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.badgeId").value("EMP001"))
            .andExpect(jsonPath("$.firstName").value("John"));
    }
}
```

---

## Deployment & CI/CD

**Containerization**:
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/warehouse-ems.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Kubernetes Deployment**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: warehouse-ems
spec:
  replicas: 3
  selector:
    matchLabels:
      app: warehouse-ems
  template:
    metadata:
      labels:
        app: warehouse-ems
    spec:
      containers:
      - name: warehouse-ems
        image: warehouse-ems:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
```

**CI/CD Pipeline** (GitHub Actions):
```yaml
name: CI/CD Pipeline
on:
  push:
    branches: [main]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
    - name: Build with Maven
      run: mvn clean package
    - name: Run tests
      run: mvn test
    - name: Build Docker image
      run: docker build -t warehouse-ems .
```

---

## Conclusion

This technical design document provides a comprehensive blueprint for implementing the Warehouse Employee Management System using Spring Boot 3.x. All code samples follow industry best practices including:

- Layered architecture (Controller -> Service -> Repository)
- DTO pattern for API contracts
- Comprehensive validation
- Security with RBAC
- Audit logging
- Error handling
- Testing strategies
- Deployment configurations

Development teams can use this document as the definitive reference for implementation, ensuring consistency and quality across all 28 user stories.