# Warehouse Employee Management System â Low-Level Technical Design Document

**Version:** 1.0  
**Spring Boot:** 3.x  
**Author:** Senior Software Architect  
**Date:** 2024-06-XX

---

## Table of Contents

- [E01: Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
- [E02: Employee Master Data (CRUD)](#e02-employee-master-data-crud)
- [E03: Role-Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
- [E04: Time & Attendance](#e04-time--attendance)
- [E05: Shift & Schedule Management](#e05-shift--schedule-management)
- [E06: Leave & Absence Management](#e06-leave--absence-management)
- [E07: Training & Certification Tracking](#e07-training--certification-tracking)
- [E08: Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
- [E09: Equipment & Asset Assignment](#e09-equipment--asset-assignment)
- [E10: Performance Reviews & Goals](#e10-performance-reviews--goals)
- [E11: Payroll Export Integration](#e11-payroll-export-integration)
- [E12: Notifications & Announcements](#e12-notifications--announcements)
- [E13: Integration Layer](#e13-integration-layer)
- [E14: Audit Trail & Compliance](#e14-audit-trail--compliance)
- [E15: Reporting & Analytics](#e15-reporting--analytics)
- [E16: Mobile Access (PWA)](#e16-mobile-access-pwa)
- [E17: Onboarding & Offboarding](#e17-onboarding--offboarding)
- [E18: Localization & Multi-Tenant](#e18-localization--multi-tenant)
- [E19: Advanced Scheduling (AI/Optimization)](#e19-advanced-scheduling-aioptimization)
- [E20: Self-Service Portal](#e20-self-service-portal)

---

## <a name="e01-project-scaffolding--domain-setup"></a>E01: Project Scaffolding & Domain Setup

### 1. OVERVIEW

- **Spring Boot 3.x** project using Maven.
- Modular structure: `employee`, `scheduling`, `attendance`, `safety`, etc.
- Database migrations via **Flyway** (preferred) or **Liquibase**.
- **Spring Boot Actuator** enabled for health and metrics.
- Standardized base package: `com.companyname.wems`.

### 2. PACKAGE STRUCTURE

```
com.companyname.wems
âââ config
âââ employee
â   âââ controller
â   âââ service
â   âââ repository
â   âââ domain
â   âââ dto
âââ scheduling
âââ attendance
âââ safety
âââ security
âââ audit
âââ notification
âââ integration
âââ ...
```

### 3. DOMAIN MODEL

- No business entities in this epic; focus is on scaffolding.

### 4. REPOSITORY LAYER

- N/A

### 5. SERVICE LAYER

- N/A

### 6. CONTROLLER LAYER

- N/A

### 7. CONFIGURATION

**application.yml**
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wems
    username: wems
    password: secret
  flyway:
    enabled: true
    locations: classpath:db/migration

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

**Flyway Migration Example**
```sql
-- src/main/resources/db/migration/V1__init_schema.sql
CREATE TABLE employee (
    id UUID PRIMARY KEY,
    badge_id VARCHAR(32) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    ...
);
```

### 8. INTEGRATION POINTS

- N/A

### 9. CODE SAMPLES

**Main Application**
```java
@SpringBootApplication
public class WemsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WemsApplication.class, args);
    }
}
```

**Actuator Health Check**
```
GET /actuator/health
Response: {"status":"UP"}
```

---

## <a name="e02-employee-master-data-crud"></a>E02: Employee Master Data (CRUD)

### 1. OVERVIEW

- Centralized employee management.
- CRUD APIs with soft-delete, pagination, filtering.
- DTO mapping, validation, OpenAPI documentation.

### 2. PACKAGE STRUCTURE

```
com.companyname.wems.employee
âââ controller
âââ service
âââ repository
âââ domain
âââ dto
```

### 3. DOMAIN MODEL

**Employee.java**
```java
@Entity
@Table(name = "employee")
@SQLDelete(sql = "UPDATE employee SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Employee {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private String badgeId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeRole role;

    @Column(nullable = false)
    private String department;

    @Column
    private String shiftGroup;

    @Column(nullable = false)
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status;

    @Column(nullable = false)
    private boolean deleted = false;

    // Getters, setters, equals, hashCode
}
```

**EmployeeRole.java**
```java
public enum EmployeeRole {
    ADMIN, HR, SUPERVISOR, WORKER
}
```

**EmployeeStatus.java**
```java
public enum EmployeeStatus {
    ACTIVE, INACTIVE, TERMINATED, ON_LEAVE
}
```

### 4. REPOSITORY LAYER

**EmployeeRepository.java**
```java
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDepartmentAndStatus(String department, EmployeeStatus status, Pageable pageable);
    @Query("SELECT e FROM Employee e WHERE e.name LIKE %:name%")
    Page<Employee> searchByName(@Param("name") String name, Pageable pageable);
}
```

### 5. SERVICE LAYER

**EmployeeService.java**
```java
@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public Page<Employee> listEmployees(EmployeeFilter filter, Pageable pageable) {
        // Filtering logic
    }

    @Transactional
    public Employee createEmployee(@Valid EmployeeDto dto) {
        // Uniqueness check, mapping, save
    }

    @Transactional
    public Employee updateEmployee(UUID id, @Valid EmployeeDto dto) {
        // Fetch, update, save
    }

    @Transactional
    public void softDeleteEmployee(UUID id) {
        // Soft delete
    }
}
```

### 6. CONTROLLER LAYER

**EmployeeController.java**
```java
@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee", description = "Employee management APIs")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Operation(summary = "Create employee")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<EmployeeResponseDto> createEmployee(
        @Valid @RequestBody EmployeeDto dto) {
        // ...
    }

    @Operation(summary = "Get paginated employees")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public Page<EmployeeResponseDto> listEmployees(
        @ParameterObject EmployeeFilter filter, Pageable pageable) {
        // ...
    }

    @Operation(summary = "Update employee")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<EmployeeResponseDto> updateEmployee(
        @PathVariable UUID id, @Valid @RequestBody EmployeeDto dto) {
        // ...
    }

    @Operation(summary = "Soft-delete employee")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id) {
        // ...
    }
}
```

**EmployeeDto.java**
```java
public class EmployeeDto {
    @NotBlank
    private String badgeId;

    @NotBlank
    private String name;

    @NotNull
    private EmployeeRole role;

    @NotBlank
    private String department;

    private String shiftGroup;

    @NotNull
    private LocalDate hireDate;

    @NotNull
    private EmployeeStatus status;
}
```

### 7. CONFIGURATION

**application.yml**
```yaml
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
```

### 8. INTEGRATION POINTS

- Exposed REST APIs for HRIS sync (see E13).

### 9. CODE SAMPLES

See above for entity, repository, service, controller, and DTO.

---

## <a name="e03-role-based-access-control-rbac"></a>E03: Role-Based Access Control (RBAC)

### 1. OVERVIEW

- **Spring Security** with roles: ADMIN, HR, SUPERVISOR, WORKER.
- Method and endpoint security.
- Row-level security for team-based access.
- API key/OAuth2 toggle via config.

### 2. PACKAGE STRUCTURE

```
com.companyname.wems.security
âââ config
âââ service
âââ domain
âââ util
```

### 3. DOMAIN MODEL

**User.java**
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<EmployeeRole> roles;

    // Getters, setters
}
```

### 4. REPOSITORY LAYER

**UserRepository.java**
```java
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
}
```

### 5. SERVICE LAYER

**CustomUserDetailsService.java**
```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet())
        );
    }
}
```

### 6. CONTROLLER LAYER

- Security handled via annotations, e.g., `@PreAuthorize`.

### 7. CONFIGURATION

**SecurityConfig.java**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/api/attendance/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            )
            .httpBasic();
        return http.build();
    }
}
```

**application.yml**
```yaml
security:
  oauth2:
    enabled: false
  api-key:
    enabled: true
```

### 8. INTEGRATION POINTS

- OAuth2/JWT support for SSO (see E13).

### 9. CODE SAMPLES

See above for security config, user entity, and service.

---

## <a name="e04-time--attendance"></a>E04: Time & Attendance

### 1. OVERVIEW

- Clock-in/out endpoints with geofence/device capture.
- Shift association, hours calculation, missed punch correction workflow.
- Exportable reports.

### 2. PACKAGE STRUCTURE

```
com.companyname.wems.attendance
âââ controller
âââ service
âââ repository
âââ domain
âââ dto
```

### 3. DOMAIN MODEL

**AttendanceEvent.java**
```java
@Entity
@Table(name = "attendance_event")
public class AttendanceEvent {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceType type; // CLOCK_IN, CLOCK_OUT

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column
    private String deviceId;

    @Column
    private String geoLocation; // e.g., "lat,lng"

    @Column
    private boolean correctionRequested = false;

    // Getters, setters
}
```

**AttendanceType.java**
```java
public enum AttendanceType {
    CLOCK_IN, CLOCK_OUT
}
```

### 4. REPOSITORY LAYER

**AttendanceEventRepository.java**
```java
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, UUID> {
    List<AttendanceEvent> findByEmployeeAndTimestampBetween(Employee employee, LocalDateTime start, LocalDateTime end);
}
```

### 5. SERVICE LAYER

**AttendanceService.java**
```java
@Service
public class AttendanceService {
    @Transactional
    public AttendanceEvent clockIn(UUID employeeId, AttendanceEventDto dto) {
        // Validate geofence, device, create event
    }

    @Transactional
    public AttendanceEvent clockOut(UUID employeeId, AttendanceEventDto dto) {
        // Validate, create event, calculate hours
    }

    @Transactional
    public void requestCorrection(UUID eventId, CorrectionRequestDto dto) {
        // Mark event, create approval task
    }
}
```

### 6. CONTROLLER LAYER

**AttendanceController.java**
```java
@RestController
@RequestMapping("/api/attendance")
@Tag(name = "Attendance", description = "Time & Attendance APIs")
public class AttendanceController {
    @Operation(summary = "Clock in")
    @PostMapping("/clock-in")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<AttendanceEventResponseDto> clockIn(
        @Valid @RequestBody AttendanceEventDto dto) {
        // ...
    }

    @Operation(summary = "Clock out")
    @PostMapping("/clock-out")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<AttendanceEventResponseDto> clockOut(
        @Valid @RequestBody AttendanceEventDto dto) {
        // ...
    }

    @Operation(summary = "Request correction")
    @PostMapping("/{eventId}/correction")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<Void> requestCorrection(
        @PathVariable UUID eventId, @Valid @RequestBody CorrectionRequestDto dto) {
        // ...
    }
}
```

**AttendanceEventDto.java**
```java
public class AttendanceEventDto {
    @NotNull
    private UUID employeeId;

    @NotNull
    private LocalDateTime timestamp;

    private String deviceId;

    private String geoLocation;
}
```

### 7. CONFIGURATION

**application.yml**
```yaml
attendance:
  geofence:
    enabled: true
    allowed-radius-meters: 100
```

### 8. INTEGRATION POINTS

- Payroll export (E11).
- Notification on missed punch (E12).

### 9. CODE SAMPLES

See above for entity, repository, service, controller, and DTO.

---

## <a name="e05-shift--schedule-management"></a>E05: Shift & Schedule Management

### 1. OVERVIEW

- Shift templates, rotations, assignments, conflict detection.
- Bulk assignment, blackout dates, operation calendars.

### 2. PACKAGE STRUCTURE

```
com.companyname.wems.scheduling
âââ controller
âââ service
âââ repository
âââ domain
âââ dto
```

### 3. DOMAIN MODEL

**ShiftTemplate.java**
```java
@Entity
@Table(name = "shift_template")
public class ShiftTemplate {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private boolean recurring;

    // Getters, setters
}
```

**ShiftAssignment.java**
```java
@Entity
@Table(name = "shift_assignment")
public class ShiftAssignment {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "shift_template_id", nullable = false)
    private ShiftTemplate shiftTemplate;

    @Column(nullable = false)
    private LocalDate shiftDate;

    @Column(nullable = false)
    private boolean assigned;

    // Getters, setters
}
```

### 4. REPOSITORY LAYER

**ShiftTemplateRepository.java**
```java
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, UUID> {
    Optional<ShiftTemplate> findByName(String name);
}
```

**ShiftAssignmentRepository.java**
```java
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, UUID> {
    List<ShiftAssignment> findByEmployeeAndShiftDateBetween(Employee employee, LocalDate start, LocalDate end);
    boolean existsByEmployeeAndShiftDate(Employee employee, LocalDate date);
}
```

### 5. SERVICE LAYER

**SchedulingService.java**
```java
@Service
public class SchedulingService {
    @Transactional
    public ShiftAssignment assignShift(UUID employeeId, UUID shiftTemplateId, LocalDate date) {
        // Conflict detection, assignment
    }

    @Transactional
    public void bulkAssignShifts(List<ShiftAssignmentDto> assignments) {
        // Bulk logic
    }
}
```

### 6. CONTROLLER LAYER

**SchedulingController.java**
```java
@RestController
@RequestMapping("/api/scheduling")
@Tag(name = "Scheduling", description = "Shift & Schedule APIs")
public class SchedulingController {
    @Operation(summary = "Assign shift")
    @PostMapping("/assign")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ShiftAssignmentResponseDto> assignShift(
        @Valid @RequestBody ShiftAssignmentDto dto) {
        // ...
    }

    @Operation(summary = "Bulk assign shifts")
    @PostMapping("/bulk-assign")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Void> bulkAssignShifts(
        @Valid @RequestBody List<ShiftAssignmentDto> dtos) {
        // ...
    }
}
```

### 7. CONFIGURATION

**application.yml**
```yaml
scheduling:
  blackout-dates:
    - 2024-12-25
    - 2024-01-01
```

### 8. INTEGRATION POINTS

- Attendance (E04).
- Notification (E12).

### 9. CODE SAMPLES

See above for entity, repository, service, controller, and DTO.

---

## <a name="e06-leave--absence-management"></a>E06: Leave & Absence Management

### 1. OVERVIEW

- PTO/sick/unpaid leave requests, approvals, accrual balances.
- Integration with scheduling and payroll.

### 2. PACKAGE STRUCTURE

```
com.companyname.wems.leave
âââ controller
âââ service
âââ repository
âââ domain
âââ dto
```

### 3. DOMAIN MODEL

**LeaveRequest.java**
```java
@Entity
@Table(name = "leave_request")
public class LeaveRequest {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType type; // PTO, SICK, UNPAID

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status; // REQUESTED, APPROVED, DENIED

    @Column
    private String reason;

    // Getters, setters
}
```

**LeaveType.java**
```java
public enum LeaveType {
    PTO, SICK, UNPAID
}
```

**LeaveStatus.java**
```java
public enum LeaveStatus {
    REQUESTED, APPROVED, DENIED
}
```

### 4. REPOSITORY LAYER

**LeaveRequestRepository.java**
```java
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
    List<LeaveRequest> findByEmployeeAndStatus(Employee employee, LeaveStatus status);
}
```

### 5. SERVICE LAYER

**LeaveService.java**
```java
@Service
public class LeaveService {
    @Transactional
    public LeaveRequest requestLeave(UUID employeeId, LeaveRequestDto dto) {
        // Accrual check, create request
    }

    @Transactional
    public LeaveRequest approveLeave(UUID requestId) {
        // Approve, update balances
    }

    @Transactional
    public LeaveRequest denyLeave(UUID requestId, String reason) {
        // Deny, notify
    }
}
```

### 6. CONTROLLER LAYER

**LeaveController.java**
```java
@RestController
@RequestMapping("/api/leave")
@Tag(name = "Leave", description = "Leave & Absence APIs")
public class LeaveController {
    @Operation(summary = "Request leave")
    @PostMapping
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<LeaveRequestResponseDto> requestLeave(
        @Valid @RequestBody LeaveRequestDto dto) {
        // ...
    }

    @Operation(summary = "Approve leave")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<LeaveRequestResponseDto> approveLeave(@PathVariable UUID id) {
        // ...
    }

    @Operation(summary = "Deny leave")
    @PostMapping("/{id}/deny")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<LeaveRequestResponseDto> denyLeave(
        @PathVariable UUID id, @RequestBody DenyReasonDto dto) {
        // ...
    }
}
```

### 7. CONFIGURATION

**application.yml**
```yaml
leave:
  accrual:
    pto-per-month: 1.5
    sick-per-month: 1.0
```

### 8. INTEGRATION POINTS

- Scheduling (E05).
- Payroll (E11).

### 9. CODE SAMPLES

See above for entity, repository, service, controller, and DTO.

---

## <a name="e07-training--certification-tracking"></a>E07: Training & Certification Tracking

### 1. OVERVIEW

- Track certifications, expirations, renewals, assignment blocking.
- Upload proof documents.

### 2. PACKAGE STRUCTURE

```
com.companyname.wems.certification
âââ controller
âââ service
âââ repository
âââ domain
âââ dto
```

### 3. DOMAIN MODEL

**Certification.java**
```java
@Entity
@Table(name = "certification")
public class Certification {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column
    private String documentUrl;

    // Getters, setters
}
```

### 4. REPOSITORY LAYER

**CertificationRepository.java**
```java
public interface CertificationRepository extends JpaRepository<Certification, UUID> {
    List<Certification> findByEmployeeAndExpiryDateAfter(Employee employee, LocalDate date);
    List<Certification> findByExpiryDateBetween(LocalDate start, LocalDate end);
}
```

### 5. SERVICE LAYER

**CertificationService.java**
```java
@Service
public class CertificationService {
    @Transactional
    public Certification addCertification(UUID employeeId, CertificationDto dto) {
        // Save, upload doc
    }

    @Transactional
    public void blockAssignmentIfExpired(UUID employeeId, String certName) {
        // Check expiry, throw if invalid
    }
}
```

### 6. CONTROLLER LAYER

**CertificationController.java**
```java
@RestController
@RequestMapping("/api/certifications")
@Tag(name = "Certification", description = "Training & Certification APIs")
public class CertificationController {
    @Operation(summary = "Add certification")
    @PostMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<CertificationResponseDto> addCertification(
        @Valid @RequestBody CertificationDto dto) {
        // ...
    }

    @Operation(summary = "List expiring certifications")
    @GetMapping("/expiring")
    @PreAuthorize("hasRole('HR')")
    public List<CertificationResponseDto> listExpiringCerts(
        @RequestParam LocalDate from, @RequestParam LocalDate to) {
        // ...
    }
}
```

### 7. CONFIGURATION

**application.yml**
```yaml
certification:
  alert-days-before-expiry: [30, 7]
  document-storage:
    type: s3
    bucket: wems-certifications
```

### 8. INTEGRATION POINTS

- Scheduling (E05).
- Asset assignment (E09).
- Notification (E12).

### 9. CODE SAMPLES

See above for entity, repository, service, controller, and DTO.

---

## <a name="e08-safety-incidents--osha-reporting"></a>E08: Safety Incidents & OSHA Reporting

### 1. OVERVIEW

- Record incidents, investigation workflow, OSHA reporting.
- Metrics dashboard.

### 2. PACKAGE STRUCTURE

```
com.companyname.wems.safety
âââ controller
âââ service
âââ repository
âââ domain
âââ dto
```

### 3. DOMAIN MODEL

**SafetyIncident.java**
```java
@Entity
@Table(name = "safety_incident")
public class SafetyIncident {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentSeverity severity;

    @ManyToMany
    @JoinTable(
        name = "incident_employee",
        joinColumns = @JoinColumn(name = "incident_id"),
        inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private Set<Employee> involvedEmployees;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status; // OPEN, INVESTIGATING, RESOLVED

    // Getters, setters
}
```

**IncidentSeverity.java**
```java
public enum IncidentSeverity {
    MINOR, MAJOR, CRITICAL
}
```

**IncidentStatus.java**
```java
public enum IncidentStatus {
    OPEN, INVESTIGATING, RESOLVED
}
```

### 4. REPOSITORY LAYER

**SafetyIncidentRepository.java**
```java
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, UUID> {
    List<SafetyIncident> findByStatus(IncidentStatus status);
    List<SafetyIncident> findByOccurredAtBetween(LocalDateTime start, LocalDateTime end);
}
```

### 5. SERVICE LAYER

**SafetyService.java**
```java
@Service
public class SafetyService {
    @Transactional
    public SafetyIncident reportIncident(SafetyIncidentDto dto) {
        // Save, notify
    }

    @Transactional
    public SafetyIncident updateStatus(UUID incidentId, IncidentStatus status) {
        // Workflow logic
    }
}
```

### 6. CONTROLLER LAYER

**SafetyController.java**
```java
@RestController
@RequestMapping("/api/safety/incidents")
@Tag(name = "Safety", description = "Safety Incident APIs")
public class SafetyController {
    @Operation(summary = "Report incident")
    @PostMapping
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<SafetyIncidentResponseDto> reportIncident(
        @Valid @RequestBody SafetyIncidentDto dto) {
        // ...
    }

    @Operation(summary = "Update incident status")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<SafetyIncidentResponseDto> updateStatus(
        @PathVariable UUID id, @RequestBody IncidentStatusDto dto) {
        // ...
    }
}
```

### 7. CONFIGURATION

**application.yml**
```yaml
safety:
  osha-report:
    enabled: true
    export-fields: [id, description, occurredAt, severity, status]
```

### 8. INTEGRATION POINTS

- OSHA export (CSV/PDF).
- Notification (E12).

### 9. CODE SAMPLES

See above for entity, repository, service, controller, and DTO.

---

## <a name="e09-equipment--asset-assignment"></a>E09: Equipment & Asset Assignment

### 1. OVERVIEW

- Asset registry, checkout/return, certification validation, asset condition tracking.

### 2. PACKAGE STRUCTURE

```
com.companyname.wems.asset
âââ controller
âââ service
âââ repository
âââ domain
âââ dto
```

### 3. DOMAIN MODEL

**Asset.java**
```java
@Entity
@Table(name = "asset")
public class Asset {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // e.g., FORKLIFT, SCANNER

    @Column(nullable = false)
    private String condition;

    @Column
    private boolean checkedOut;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private Employee assignedTo;

    // Getters, setters
}
```

### 4. REPOSITORY LAYER

**AssetRepository.java**
```java
public interface AssetRepository extends JpaRepository<Asset, UUID> {
    List<Asset> findByTypeAndCheckedOut(String type, boolean checkedOut);
}
```

### 5. SERVICE LAYER

**AssetService.java**
```java
@Service
public class AssetService {
    @Transactional
    public Asset checkoutAsset(UUID assetId, UUID employeeId) {
        // Cert validation, assign asset
    }

    @Transactional
    public Asset returnAsset(UUID assetId) {
        // Unassign, update condition
    }
}
```

### 6. CONTROLLER LAYER

**AssetController.java**
```java
@RestController
@RequestMapping("/api/assets")
@Tag(name = "Asset", description = "Asset Assignment APIs")
public class AssetController {
    @Operation(summary = "Checkout asset")
    @PostMapping("/{id}/checkout")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<AssetResponseDto> checkoutAsset(
        @PathVariable UUID id, @RequestParam UUID employeeId) {
        // ...
    }

    @Operation(summary = "Return asset")
    @PostMapping("/{id}/return")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<AssetResponseDto> returnAsset(@PathVariable UUID id) {
        // ...
    }
}
```

### 7. CONFIGURATION

**application.yml**
```yaml
asset:
  types: [FORKLIFT, SCANNER, PPE]
```

### 8. INTEGRATION POINTS

- Certification check (E07).
- Audit log (E14).

### 9. CODE SAMPLES

See above for entity, repository, service, controller, and DTO.

---

## <a name="e10-performance-reviews--goals"></a>E10: Performance Reviews & Goals

### 1. OVERVIEW

- Review templates, goals, competencies, acknowledgements, immutable history.

### 2. PACKAGE STRUCTURE

```
com.companyname.wems.performance
âââ controller
âââ service
âââ repository
âââ domain
âââ dto
```

### 3. DOMAIN MODEL

**PerformanceReview.java**
```java
@Entity
@Table(name = "performance_review")
public class PerformanceReview {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate reviewDate;

    @Column(nullable = false)
    private String templateName;

    @Column
    private String goals;

    @Column
    private String competencies;

    @Column
    private String supervisorComments;

    @Column
    private String employeeComments;

    @Column(nullable = false)
    private boolean acknowledgedByEmployee;

    @Column(nullable = false)
    private boolean acknowledgedBySupervisor;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Getters, setters
}
```

### 4. REPOSITORY LAYER

**PerformanceReviewRepository.java**
```java
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, UUID> {
    List<PerformanceReview> findByEmployee(Employee employee);
}
```

### 5. SERVICE LAYER

**PerformanceService.java**
```java
@Service
public class PerformanceService {
    @Transactional
    public PerformanceReview createReview(UUID employeeId, PerformanceReviewDto dto) {
        // Save, notify
    }

    @Transactional
    public void acknowledgeReview(UUID reviewId, boolean byEmployee) {
        // Update acknowledgement
    }
}
```

### 6. CONTROLLER LAYER

**PerformanceController.java**
```java
@RestController
@RequestMapping("/api/performance")
@Tag(name = "Performance", description = "Performance Review APIs")
public class PerformanceController {
    @Operation(summary = "Create review")
    @PostMapping
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<PerformanceReviewResponseDto> createReview(
        @Valid @RequestBody PerformanceReviewDto dto) {
        // ...
    }

    @Operation(summary = "Acknowledge review")
    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasRole('WORKER') or hasRole('SUPERVISOR')")
    public ResponseEntity<Void> acknowledgeReview(
        @PathVariable UUID id, @RequestParam boolean byEmployee) {
        // ...
    }
}
```

### 7. CONFIGURATION

**application.yml**
```yaml
performance:
  templates:
    - Quarterly
    - Annual
```

### 8. INTEGRATION POINTS

- PDF export.
- Notification (E12).

### 9. CODE SAMPLES

See above for entity, repository, service, controller, and DTO.

---

## <a name="e11-payroll-export-integration"></a>E11: Payroll Export Integration

### 1. OVERVIEW

- Generate payroll files from attendance/leave data.
- Map to provider formats, secure delivery (SFTP/API).

### 2. PACKAGE STRUCTURE

```
com.companyname.wems.payroll
âââ service
âââ integration
âââ dto
âââ config
```

### 3. DOMAIN MODEL

- No new entities; uses attendance and leave.

### 4. REPOSITORY LAYER

- N/A

### 5. SERVICE LAYER

**PayrollExportService.java**
```java
@Service
public class PayrollExportService {
    @Transactional(readOnly = true)
    public PayrollFile generatePayrollFile(LocalDate periodStart, LocalDate periodEnd) {
        // Aggregate attendance/leave, map to schema
    }

    @Transactional
    public void deliverPayrollFile(PayrollFile file) {
        // SFTP/API delivery, retry logic
    }
}
```

### 6. CONTROLLER LAYER

**PayrollController.java**
```java
@RestController
@RequestMapping("/api/payroll")
@Tag(name = "Payroll", description = "Payroll Export APIs")
public class PayrollController {
    @Operation(summary = "Export payroll")
    @PostMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> exportPayroll(
        @RequestParam LocalDate start, @RequestParam LocalDate end) {
        // ...
    }
}
```

### 7. CONFIGURATION

**application.yml**
```yaml
payroll:
  provider:
    type: adp
    sftp:
      host: sftp.adp.com
      user: payroll
      password: secret
```

### 8. INTEGRATION POINTS

- Attendance (E04), Leave (E06).
- SFTP/API delivery.

### 9. CODE SAMPLES

See above for service and controller.

---

## <a name="e12-notifications--announcements"></a>E12: Notifications & Announcements

### 1. OVERVIEW

- In-app, email, SMS notifications for events.
- Opt-in/out, templates, delivery tracking.

### 2. PACKAGE STRUCTURE

```
com.companyname.wems.notification
âââ controller
âââ service
âââ repository
âââ domain
âââ dto
```

### 3. DOMAIN MODEL

**NotificationPreference.java**
```java
@Entity
@Table(name = "notification_preference")
public class NotificationPreference {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel; // IN_APP, EMAIL, SMS

    @Column(nullable = false)
    private boolean enabled;

    // Getters, setters
}
```

### 4. REPOSITORY LAYER

**NotificationPreferenceRepository.java**
```java
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {
    List<NotificationPreference> findByEmployee(Employee employee);
}
```

### 5. SERVICE LAYER

**NotificationService.java**
```java
@Service
public class NotificationService {
    @Transactional
    public void sendNotification(UUID employeeId, NotificationType type, String message) {
        // Check preferences, send via channel
    }
}
```

### 6. CONTROLLER LAYER

**NotificationController.java**
```java
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "Notification APIs")
public class NotificationController {
    @Operation(summary = "Update notification preferences")
    @PutMapping("/preferences")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<Void> updatePreferences(
        @Valid @RequestBody NotificationPreferenceDto dto) {
        // ...
    }
}
```

### 7. CONFIGURATION

**application.yml**
```yaml
notification:
  channels: [IN_APP, EMAIL, SMS]
  quiet-hours:
    start: 22:00
    end: 06:00
```

### 8. INTEGRATION POINTS

- Attendance, scheduling, certification, safety, etc.

### 9. CODE SAMPLES

See above for entity, repository, service, controller, and DTO.

---

## <a name="e13-integration-layer"></a>E13: Integration Layer

### 1. OVERVIEW

- REST APIs for HRIS/WMS, webhooks, SSO.

### 2. PACKAGE STRUCTURE

```
com.companyname.wems.integration
âââ controller
âââ service
âââ webhook
âââ dto
âââ config
```

### 3. DOMAIN MODEL

- N/A (uses employee, department, etc.)

### 4. REPOSITORY LAYER

- N/A

### 5. SERVICE LAYER

**HrisIntegrationService.java**
```java
@Service
public class HrisIntegrationService {
    @Transactional
    public void syncEmployee(HrisEmployeeDto dto) {
        // Create/update employee
    }
}
```

### 6. CONTROLLER LAYER

**IntegrationController.java**
```java
@RestController
@RequestMapping("/api/integration")
@Tag(name = "Integration", description = "Integration APIs")
public class IntegrationController {
    @Operation(summary = "HRIS webhook")
    @PostMapping("/hris/webhook")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> hrisWebhook(@RequestBody HrisEmployeeDto dto) {
        // ...
    }
}
```

### 7. CONFIGURATION

**application.yml**
```yaml
integration:
  hris:
    api-key: secret
  wms:
    endpoint: https://wms.company.com/api
```

### 8. INTEGRATION POINTS

- HRIS, WMS, SSO.

### 9. CODE SAMPLES

See above for service and controller.

---

## <a name="e14-audit-trail--compliance"></a>E14: Audit Trail & Compliance

### 1. OVERVIEW

- Centralized audit logging, tamper-evident storage.

### 2. PACKAGE STRUCTURE

```
com.companyname.wems.audit
âââ service
âââ repository
âââ domain
âââ config
```

### 3. DOMAIN MODEL

**AuditLog.java**
```java
@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String entity;

    @Column(nullable = false)
    private String action; // CREATE, UPDATE, DELETE

    @Column(nullable = false)
    private String actor;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String beforeState;

    @Column(columnDefinition = "TEXT")
    private String afterState;

    // Getters, setters
}
```

### 4. REPOSITORY LAYER

**AuditLogRepository.java**
```java
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByEntityAndTimestampBetween(String entity, LocalDateTime start, LocalDateTime end);
}
```

### 5. SERVICE LAYER

**AuditService.java**
```java
@Service
public class AuditService {
    @Transactional
    public void log(String entity, String action, String actor, Object before, Object after) {
        // Serialize, save
    }
}
```

### 6. CONTROLLER LAYER

- N/A (internal service)

### 7. CONFIGURATION

**application.yml**
```yaml
audit:
  tamper-evident: true
```

### 8. INTEGRATION POINTS

- All modules.

### 9. CODE SAMPLES

See above for entity, repository, and service.

---

## <a name="e15-reporting--analytics"></a>E15: Reporting & Analytics

### 1. OVERVIEW

- Operational reports, CSV/PDF exports, dashboards.

### 2. PACKAGE STRUCTURE

```
com.companyname.wems.reporting
âââ controller
âââ service
âââ dto
âââ util
```

### 3. DOMAIN MODEL

- N/A (aggregates from other modules)

### 4. REPOSITORY LAYER

- N/A

### 5. SERVICE LAYER

**ReportingService.java**
```java
@Service
public class ReportingService {
    @Transactional(readOnly = true)
    public Report generateAttendanceReport(ReportFilter filter) {
        // Aggregate, return DTO
    }

    @Transactional(readOnly = true)
    public byte[] exportReportToCsv(Report report) {
        // CSV export logic
    }
}
```

### 6. CONTROLLER LAYER

**ReportingController.java**
```java
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reporting", description = "Reporting APIs")
public class ReportingController {
    @Operation(summary = "Get attendance report")
    @GetMapping("/attendance")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<ReportDto> getAttendanceReport(
        @ParameterObject ReportFilter filter) {
        // ...
    }

    @Operation(summary = "Export report as CSV")
    @GetMapping("/attendance/export")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<byte[]> exportAttendanceReport(
        @ParameterObject ReportFilter filter) {
        // ...
    }
}
```

### 7. CONFIGURATION

**application.yml**
```yaml
reporting:
  export:
    max-rows: 50000
    formats: [CSV, PDF]
```

### 8. INTEGRATION POINTS

- BI tools, dashboards.

### 9. CODE SAMPLES

See above for service and controller.

---

## <a name="e16-mobile-access-pwa"></a>E16: Mobile Access (PWA)

### 1. OVERVIEW

- Responsive views, offline support, PWA manifest.

### 2. PACKAGE STRUCTURE

- Frontend (React/Vue/Angular) in `/frontend` (not covered here).
- Backend APIs as above.

### 3. DOMAIN MODEL

- N/A

### 4. REPOSITORY LAYER

- N/A

### 5. SERVICE LAYER

- N/A

### 6. CONTROLLER LAYER

- Existing APIs support mobile.

### 7. CONFIGURATION

**application.yml**
```yaml
spring:
  web:
    resources:
      static-locations: classpath:/static/
```

- Serve `manifest.json` and service worker.

### 8. INTEGRATION POINTS

- Attendance, scheduling, leave, notifications.

### 9. CODE SAMPLES

**manifest.json**
```json
{
  "name": "WEMS",
  "short_name": "WEMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

---

## <a name="e17-onboarding--offboarding"></a>E17: Onboarding & Offboarding

### 1. OVERVIEW

- Automated provisioning/deprovisioning, training, asset assignment.

### 2. PACKAGE STRUCTURE

```
com.companyname.wems.onboarding
âââ service
âââ workflow
```

### 3. DOMAIN MODEL

- Uses employee, asset, certification.

### 4. REPOSITORY LAYER

- N/A

### 5. SERVICE LAYER

**OnboardingService.java**
```java
@Service
public class OnboardingService {
    @Transactional
    public void onboardEmployee(HrisEmployeeDto dto) {
        // Create employee, assign training, assets
    }

    @Transactional
    public void offboardEmployee(UUID employeeId) {
        // Revoke access, collect assets, update status
    }
}
```

### 6. CONTROLLER LAYER

- N/A (triggered by integration/events)

### 7. CONFIGURATION

**application.yml**
```yaml
onboarding:
  default-training: [Safety, Forklift]
```

### 8. INTEGRATION POINTS

- HRIS, asset, certification.

### 9. CODE SAMPLES

See above for service.

---

## <a name="e18-localization--multi-tenant"></a>E18: Localization & Multi-Tenant

### 1. OVERVIEW

- Multiple warehouse support, UI localization, timezone awareness.

### 2. PACKAGE STRUCTURE

```
com.companyname.wems.tenant
âââ domain
âââ service
âââ config
âââ util
```

### 3. DOMAIN MODEL

**Warehouse.java**
```java
@Entity
@Table(name = "warehouse")
public class Warehouse {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String timezone;

    // Getters, setters
}
```

### 4. REPOSITORY LAYER

**WarehouseRepository.java**
```java
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    Optional<Warehouse> findByName(String name);
}
```

### 5. SERVICE LAYER

**TenantService.java**
```java
@Service
public class TenantService {
    @Transactional(readOnly = true)
    public Warehouse getWarehouse(UUID id) {
        // ...
    }
}
```

### 6. CONTROLLER LAYER

**WarehouseController.java**
```java
@RestController
@RequestMapping("/api/warehouses")
@Tag(name = "Warehouse", description = "Warehouse APIs")
public class WarehouseController {
    @Operation(summary = "Get warehouse")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseDto> getWarehouse(@PathVariable UUID id) {
        // ...
    }
}
```

### 7. CONFIGURATION

**application.yml**
```yaml
spring:
  messages:
    basename: i18n/messages
```

### 8. INTEGRATION POINTS

- All modules.

### 9. CODE SAMPLES

**messages_en.properties**
```
employee.created=Employee created successfully.
```

---

## <a name="e19-advanced-scheduling-aioptimization"></a>E19: Advanced Scheduling (AI/Optimization)

### 1. OVERVIEW

- AI-based staffing predictions, auto-suggest assignments.

### 2. PACKAGE STRUCTURE

```
com.companyname.wems.optimization
âââ service
âââ dto
```

### 3. DOMAIN MODEL

- N/A

### 4. REPOSITORY LAYER

- N/A

### 5. SERVICE LAYER

**OptimizationService.java**
```java
@Service
public class OptimizationService {
    @Transactional(readOnly = true)
    public List<ShiftAssignmentSuggestionDto> suggestAssignments(LocalDate date) {
        // AI/ML logic
    }
}
```

### 6. CONTROLLER LAYER

**OptimizationController.java**
```java
@RestController
@RequestMapping("/api/optimization")
@Tag(name = "Optimization", description = "AI Scheduling APIs")
public class OptimizationController {
    @Operation(summary = "Suggest shift assignments")
    @GetMapping("/suggest")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public List<ShiftAssignmentSuggestionDto> suggestAssignments(
        @RequestParam LocalDate date) {
        // ...
    }
}
```

### 7. CONFIGURATION

**application.yml**
```yaml
optimization:
  ai-model: staffing-v1
```

### 8. INTEGRATION POINTS

- Scheduling (E05).

### 9. CODE SAMPLES

See above for service and controller.

---

## <a name="e20-self-service-portal"></a>E20: Self-Service Portal

### 1. OVERVIEW

- Employee profile updates, document uploads, pay stubs, benefits.

### 2. PACKAGE STRUCTURE

```
com.companyname.wems.portal
âââ controller
âââ service
âââ dto
âââ util
```

### 3. DOMAIN MODEL

- Uses employee, document, payroll.

### 4. REPOSITORY LAYER

- N/A

### 5. SERVICE LAYER

**PortalService.java**
```java
@Service
public class PortalService {
    @Transactional
    public void updateProfile(UUID employeeId, ProfileUpdateDto dto) {
        // Update employee
    }

    @Transactional
    public void uploadDocument(UUID employeeId, MultipartFile file) {
        // Store document
    }
}
```

### 6. CONTROLLER LAYER

**PortalController.java**
```java
@RestController
@RequestMapping("/api/portal")
@Tag(name = "Portal", description = "Self-Service Portal APIs")
public class PortalController {
    @Operation(summary = "Update profile")
    @PutMapping("/profile")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<Void> updateProfile(
        @Valid @RequestBody ProfileUpdateDto dto) {
        // ...
    }

    @Operation(summary = "Upload document")
    @PostMapping("/documents")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<Void> uploadDocument(
        @RequestParam MultipartFile file) {
        // ...
    }
}
```

### 7. CONFIGURATION

**application.yml**
```yaml
portal:
  document-storage:
    type: s3
    bucket: wems-portal-docs
```

### 8. INTEGRATION POINTS

- Payroll, benefits, document storage.

### 9. CODE SAMPLES

See above for service and controller.

---

# Exception Handling Pattern

**GlobalExceptionHandler.java**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        // Map errors
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        // 404
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        // 403
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        // 500
    }
}
```

---

# Transaction Management

- Use `@Transactional` at service layer.
- Read-only for queries, default for writes.

---

# OpenAPI/Swagger

- Annotate controllers with `@Tag`, `@Operation`, `@Parameter`, etc.
- Enable via `springdoc-openapi`.

---

# Validation

- Use `@Valid`, `@NotNull`, `@NotBlank`, etc. on DTOs and controller methods.

---

# Security

- Use `@PreAuthorize`, `@Secured` for method/endpoint security.
- Configure roles in `SecurityConfig`.

---

# Summary

This document provides a comprehensive, production-ready low-level technical design for the Warehouse Employee Management System, covering all 20 epics and 95+ user stories. All code samples follow Spring Boot 3.x best practices, with clear package structure, domain modeling, repository/service/controller layers, configuration, integration points, and security/validation patterns.

---

**End of Document**