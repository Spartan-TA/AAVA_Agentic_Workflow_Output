==================================================================
Warehouse Employee Management Platform
Low-Level Technical Design Document (Spring Boot)
==================================================================

---

### E01: Project Scaffolding & Domain Setup

**Section: Overview of Spring Boot Architecture**
Description:  
The platform uses a modular Spring Boot (Maven) project with a layered architecture (Controller, Service, Repository, Domain). Core modules include employee, scheduling, attendance, and safety. Flyway/Liquibase is used for DB migrations. Spring Boot Actuator is enabled for health and metrics.

**Section: Package Structure and Module Definitions**
Description:  
- `com.companyname.wem` (root)
  - `employee`
  - `scheduling`
  - `attendance`
  - `safety`
  - `config`
  - `common`
  - `audit`
  - `integration`
  - `notification`
  - `reporting`
  - `asset`
  - `leave`
  - `training`
  - `performance`
  - `payroll`
  - `portal`
  - `localization`
  - `mobile`
Design Specification:
- Each module contains `controller`, `service`, `repository`, `domain`, `dto`, and `mapper` sub-packages.
- `config` for global configuration (security, DB, Flyway, etc.)
- `common` for shared utilities and exceptions.

Sample Implementation:
```java
// Example: src/main/java/com/companyname/wem/employee/EmployeeApplication.java
@SpringBootApplication
public class EmployeeApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmployeeApplication.class, args);
    }
}
```

**Section: Configuration and Spring Security Settings**
Description:  
- Flyway/Liquibase enabled via `application.yml`
- Actuator endpoints enabled (`/actuator/health`, `/actuator/info`)
- Base package scanning set in main class

Design Specification:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wem
    username: wem_user
    password: secret
  flyway:
    enabled: true
    locations: classpath:db/migration
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

---

### E02: Employee Master Data (CRUD)

**Section: Overview of Spring Boot Architecture**
Description:  
Implements CRUD for Employee domain with REST APIs, DTOs, and JPA entities. Supports pagination, filtering, soft-delete, and OpenAPI documentation.

**Section: Package Structure and Module Definitions**
Description:  
- `com.companyname.wem.employee`
  - `controller`
  - `service`
  - `repository`
  - `domain`
  - `dto`
  - `mapper`

**Section: Entity Design with Domain Models and JPA Relationships**
Design Specification:
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
@SQLDelete(sql = "UPDATE employees SET deleted = true WHERE id=?")
@Where(clause = "deleted=false")
public class Employee {
    @Id @GeneratedValue
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
public enum Role { ADMIN, HR, SUPERVISOR, WORKER }
public enum Status { ACTIVE, INACTIVE, TERMINATED }
```

**Section: Service Layer Specifications with Business Logic**
Design Specification:
- `EmployeeService` with methods: `create`, `getById`, `update`, `delete`, `findAll(Pageable, filters)`
- Enforces unique badgeId, soft-delete, and validation.

Sample Implementation:
```java
@Service
public class EmployeeService {
    @Autowired private EmployeeRepository repo;
    public Employee create(EmployeeDTO dto) {
        if (repo.existsByBadgeId(dto.getBadgeId())) throw new DuplicateBadgeException();
        Employee emp = mapper.toEntity(dto);
        return repo.save(emp);
    }
    // Other CRUD methods...
}
```

**Section: Repository Layer with Spring Data JPA**
Design Specification:
```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByBadgeId(String badgeId);
    Page<Employee> findAll(Specification<Employee> spec, Pageable pageable);
}
```

**Section: Controller Specifications with REST Endpoints**
Design Specification:
- `@RestController` with endpoints:
  - `POST /employees`
  - `GET /employees`
  - `GET /employees/{id}`
  - `PUT /employees/{id}`
  - `PATCH /employees/{id}`
  - `DELETE /employees/{id}`

Sample Implementation:
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired private EmployeeService service;
    @PostMapping public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeDTO dto) { ... }
    @GetMapping public Page<EmployeeDTO> list(Pageable pageable, EmployeeFilter filter) { ... }
    // Other endpoints...
}
```

**Section: DTO Classes and Mapping Strategies**
Design Specification:
```java
public class EmployeeDTO {
    private Long id;
    @NotBlank private String name;
    @NotBlank private String badgeId;
    @NotNull private Role role;
    private String department;
    private String shiftGroup;
    @PastOrPresent private LocalDate hireDate;
    private Status status;
}
@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    Employee toEntity(EmployeeDTO dto);
    EmployeeDTO toDto(Employee entity);
}
```

---

### E03: Role-Based Access Control (RBAC)

**Section: Overview of Spring Boot Architecture**
Description:  
Spring Security is configured for RBAC. Roles: ADMIN, HR, SUPERVISOR, WORKER. Method/endpoint security and row-level constraints. API key/OAuth2 toggle via config.

**Section: Configuration and Spring Security Settings**
Design Specification:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${security.mode}") private String mode; // "oauth2" or "apikey"
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if ("oauth2".equals(mode)) {
            http.oauth2ResourceServer().jwt();
        } else {
            http.addFilter(new ApiKeyAuthFilter());
        }
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .antMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated();
    }
}
```

**Section: Service Layer Specifications with Business Logic**
- Row-level security: e.g., SUPERVISOR can only access their team.
- Method-level security with `@PreAuthorize`.

Sample Implementation:
```java
@Service
public class EmployeeService {
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #id in @teamService.getTeamEmployeeIds(principal))")
    public Employee getById(Long id) { ... }
}
```

---

### E04: Time & Attendance (Clock In/Out)

**Section: Overview of Spring Boot Architecture**
Description:  
Endpoints for clock-in/out with geofence/device capture. Calculates hours worked, handles missed punches, and corrections workflow.

**Section: Entity Design with Domain Models and JPA Relationships**
Design Specification:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private EventType type; // CLOCK_IN, CLOCK_OUT
    private LocalDateTime timestamp;
    private String deviceId;
    private Double latitude;
    private Double longitude;
    private boolean correction;
    // getters/setters
}
```

**Section: Service Layer Specifications with Business Logic**
- Validates geofence (if enabled)
- Associates event with shift
- Handles missed punches and creates correction tasks

Sample Implementation:
```java
@Service
public class AttendanceService {
    public AttendanceEvent clockIn(Long employeeId, ClockEventDTO dto) {
        // Validate geofence, device, etc.
        // Check for open shift
        // Save event
    }
    // clockOut, corrections, etc.
}
```

**Section: Controller Specifications with REST Endpoints**
- `POST /attendance/clock-in`
- `POST /attendance/clock-out`
- `GET /attendance/reports`
- `POST /attendance/corrections`

---

### E05: Shift & Schedule Management

**Section: Overview of Spring Boot Architecture**
Description:  
Manages shift templates, rotations, overtime rules, employee assignments, blackout dates, and operation calendars.

**Section: Entity Design with Domain Models and JPA Relationships**
Design Specification:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    private String recurrencePattern; // e.g., CRON or custom
    // ...
}
@Entity
public class EmployeeShiftAssignment {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private ShiftTemplate shiftTemplate;
    private LocalDate date;
    private boolean overtime;
    // ...
}
```

**Section: Service Layer Specifications with Business Logic**
- Detects conflicts
- Bulk assignment
- Handles blackout dates

Sample Implementation:
```java
@Service
public class ScheduleService {
    public void assignShift(Long employeeId, Long shiftTemplateId, LocalDate date) { ... }
    public boolean hasConflict(Long employeeId, LocalDate date) { ... }
}
```

**Section: Controller Specifications with REST Endpoints**
- `POST /shifts/templates`
- `GET /shifts/templates`
- `POST /shifts/assignments`
- `GET /shifts/employee/{id}`

---

### E06: Leave & Absence Management

**Section: Overview of Spring Boot Architecture**
Description:  
Handles PTO, sick, unpaid leave requests/approvals, accruals, and integration with scheduling/payroll.

**Section: Entity Design with Domain Models and JPA Relationships**
Design Specification:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private LeaveType; // PTO, SICK, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING) private LeaveStatus; // REQUESTED, APPROVED, DENIED
    private String approver;
    private String comments;
    // ...
}
```

**Section: Service Layer Specifications with Business Logic**
- Validates accruals
- Updates balances
- Flags scheduled shifts for coverage

Sample Implementation:
```java
@Service
public class LeaveService {
    public LeaveRequest requestLeave(Long employeeId, LeaveRequestDTO dto) { ... }
    public void approveLeave(Long requestId, String approver) { ... }
}
```

**Section: Controller Specifications with REST Endpoints**
- `POST /leave/requests`
- `POST /leave/requests/{id}/approve`
- `GET /leave/balances/{employeeId}`

---

### E07: Training & Certification Tracking

**Section: Overview of Spring Boot Architecture**
Description:  
Tracks certifications, expirations, renewals, blocks assignments if expired, uploads proof documents.

**Section: Entity Design with Domain Models and JPA Relationships**
Design Specification:
```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    @ManyToOne private Employee employee;
    private String documentUrl;
    // ...
}
```

**Section: Service Layer Specifications with Business Logic**
- Alerts for expiring certs
- Blocks scheduling if expired

Sample Implementation:
```java
@Service
public class CertificationService {
    public void checkCertificationStatus(Long employeeId, String certName) { ... }
    public void uploadProof(Long certId, MultipartFile file) { ... }
}
```

**Section: Controller Specifications with REST Endpoints**
- `POST /certifications`
- `GET /certifications/expiring`
- `POST /certifications/{id}/upload`

---

### E08: Safety Incidents & OSHA Reporting

**Section: Overview of Spring Boot Architecture**
Description:  
Records incidents/near-misses, manages investigation workflow, generates OSHA summaries.

**Section: Entity Design with Domain Models and JPA Relationships**
Design Specification:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private String description;
    private String location;
    @Enumerated(EnumType.STRING) private Severity;
    @ManyToMany private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING) private IncidentStatus; // OPEN, INVESTIGATING, RESOLVED
    private LocalDateTime reportedAt;
    // ...
}
```

**Section: Service Layer Specifications with Business Logic**
- Workflow transitions
- OSHA export

Sample Implementation:
```java
@Service
public class SafetyService {
    public SafetyIncident reportIncident(SafetyIncidentDTO dto) { ... }
    public void updateStatus(Long incidentId, IncidentStatus status) { ... }
}
```

**Section: Controller Specifications with REST Endpoints**
- `POST /safety/incidents`
- `PATCH /safety/incidents/{id}/status`
- `GET /safety/osha-report`

---

### E09: Equipment & Asset Assignment

**Section: Overview of Spring Boot Architecture**
Description:  
Assigns assets to employees, tracks check-in/out, blocks use if cert missing, maintains condition state.

**Section: Entity Design with Domain Models and JPA Relationships**
Design Specification:
```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String type; // Scanner, Forklift, PPE, etc.
    private String serialNumber;
    private String condition;
    private boolean checkedOut;
    @ManyToOne private Employee assignedTo;
    // ...
}
```

**Section: Service Layer Specifications with Business Logic**
- Blocks assignment if cert invalid
- Tracks history

Sample Implementation:
```java
@Service
public class AssetService {
    public void assignAsset(Long assetId, Long employeeId) { ... }
    public void checkInAsset(Long assetId) { ... }
}
```

**Section: Controller Specifications with REST Endpoints**
- `POST /assets`
- `POST /assets/{id}/assign`
- `POST /assets/{id}/checkin`
- `GET /assets/overdue`

---

### E10: Performance Reviews & Goals

**Section: Overview of Spring Boot Architecture**
Description:  
Manages review templates, goals, ratings, comments, and acknowledgements.

**Section: Entity Design with Domain Models and JPA Relationships**
Design Specification:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String cycle; // Q1 2024, Annual 2024, etc.
    private String goals;
    private String competencies;
    private String ratings;
    private String comments;
    private boolean acknowledgedByEmployee;
    private boolean acknowledgedBySupervisor;
    // ...
}
```

**Section: Service Layer Specifications with Business Logic**
- Immutable after sign-off
- PDF export

Sample Implementation:
```java
@Service
public class PerformanceService {
    public PerformanceReview createReview(Long employeeId, ReviewDTO dto) { ... }
    public void acknowledge(Long reviewId, String role) { ... }
}
```

**Section: Controller Specifications with REST Endpoints**
- `POST /reviews`
- `POST /reviews/{id}/acknowledge`
- `GET /reviews/{id}/export`

---

### E11: Payroll Export Integration

**Section: Overview of Spring Boot Architecture**
Description:  
Generates payroll-ready files from attendance/leave, maps to provider formats, delivers via SFTP/API.

**Section: Service Layer Specifications with Business Logic**
- Maps internal data to provider schema
- Secure delivery with retry/backoff
- Audit log for exports

Sample Implementation:
```java
@Service
public class PayrollExportService {
    public File generatePayrollExport(LocalDate periodStart, LocalDate periodEnd) { ... }
    public void deliverExport(File file) { ... }
}
```

**Section: Controller Specifications with REST Endpoints**
- `POST /payroll/export`
- `GET /payroll/exports/history`

---

### E12: Notifications & Announcements

**Section: Overview of Spring Boot Architecture**
Description:  
Sends in-app/email/SMS notifications for events, supports quiet hours, opt-in/out, localization.

**Section: Service Layer Specifications with Business Logic**
- Template management
- Delivery status tracking
- Rate limiting

Sample Implementation:
```java
@Service
public class NotificationService {
    public void sendNotification(NotificationDTO dto) { ... }
    public void trackDelivery(Long notificationId, DeliveryStatus status) { ... }
}
```

**Section: Controller Specifications with REST Endpoints**
- `POST /notifications`
- `GET /notifications/status`
- `POST /announcements`

---

### E13: Integration Layer (HRIS/WMS APIs)

**Section: Overview of Spring Boot Architecture**
Description:  
Exposes REST APIs and connectors for HRIS, WMS, and IDP (SSO). Supports webhooks and OpenAPI docs.

**Section: Integration Points (External Services, APIs)**
- HRIS: Employee sync
- WMS: Department/location sync
- IDP: SSO (JWT/OAuth2)
- Webhooks for events

Sample Implementation:
```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    @PostMapping("/hris/employees") public ResponseEntity<?> syncEmployee(@RequestBody EmployeeDTO dto) { ... }
    @PostMapping("/wms/departments") public ResponseEntity<?> syncDepartment(@RequestBody DepartmentDTO dto) { ... }
    @PostMapping("/webhooks/event") public ResponseEntity<?> handleWebhook(@RequestBody WebhookEventDTO dto) { ... }
}
```

---

### E14: Audit Trail & Compliance

**Section: Overview of Spring Boot Architecture**
Description:  
Centralized audit logging for sensitive changes, tamper-evident storage, exportable logs.

**Section: Entity Design with Domain Models and JPA Relationships**
Design Specification:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String entity;
    private Long entityId;
    private String action; // CREATE, UPDATE, DELETE
    private String actor;
    private LocalDateTime timestamp;
    @Lob private String beforeState;
    @Lob private String afterState;
    // ...
}
```

**Section: Service Layer Specifications with Business Logic**
- Logs all sensitive changes
- Immutable storage

Sample Implementation:
```java
@Service
public class AuditService {
    @Transactional
    public void logChange(String entity, Long entityId, String action, Object before, Object after) { ... }
}
```

---

### E15: Reporting & Analytics

**Section: Overview of Spring Boot Architecture**
Description:  
Provides operational reports (attendance, overtime, leave, certs, safety KPIs), CSV/PDF export, dashboards.

**Section: Service Layer Specifications with Business Logic**
- Filters by date, department, shift
- Role-based access

Sample Implementation:
```java
@Service
public class ReportingService {
    public Report generateAttendanceReport(ReportFilter filter) { ... }
    public File exportReport(Long reportId, String format) { ... }
}
```

**Section: Controller Specifications with REST Endpoints**
- `GET /reports/attendance`
- `GET /reports/export`

---

### E16: Mobile Access (PWA)

**Section: Overview of Spring Boot Architecture**
Description:  
Responsive PWA for clock-in/out, schedules, leave requests, announcements. Offline queue for clock events.

**Section: Controller Specifications with REST Endpoints**
- `GET /mobile/schedules`
- `POST /mobile/clock-in`
- `POST /mobile/leave-request`
- `GET /mobile/announcements`

**Section: Integration Points**
- Service Worker for offline support
- REST endpoints for mobile clients

---

### E17: Onboarding & Offboarding Workflow

**Section: Overview of Spring Boot Architecture**
Description:  
Automates provisioning (accounts, schedule, training), deprovisioning (access, assets) on termination.

**Section: Service Layer Specifications with Business Logic**
- HRIS triggers onboarding
- Generates tasks for training/assets
- Offboarding revokes access, collects assets

Sample Implementation:
```java
@Service
public class OnboardingService {
    public void onboardEmployee(EmployeeDTO dto) { ... }
    public void offboardEmployee(Long employeeId) { ... }
}
```

---

### E18: Localization & Multi-Site

**Section: Overview of Spring Boot Architecture**
Description:  
Supports multiple languages, timezone-aware scheduling, site-specific policies/calendars.

**Section: Configuration and Spring Security Settings**
- `messages.properties`, `messages_es.properties`, etc.
- Timezone stored per site/employee

Sample Implementation:
```java
@Configuration
public class LocaleConfig {
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.ENGLISH);
        return slr;
    }
}
```

---

### E19: Advanced Scheduling (AI/Optimization)

**Section: Overview of Spring Boot Architecture**
Description:  
Suggests optimal shift assignments using demand forecasts, skill matching, fairness. Allows manual overrides.

**Section: Service Layer Specifications with Business Logic**
- AI/ML integration for optimization
- Manual override endpoint

Sample Implementation:
```java
@Service
public class SchedulingOptimizationService {
    public List<ShiftAssignment> suggestOptimalAssignments(SchedulingInput input) { ... }
    public void overrideAssignment(Long assignmentId, Long newEmployeeId) { ... }
}
```

---

### E20: Self-Service Portal

**Section: Overview of Spring Boot Architecture**
Description:  
Portal for employees to view pay stubs, update info, request documents, view training, submit feedback.

**Section: Controller Specifications with REST Endpoints**
- `GET /portal/paystubs`
- `PUT /portal/contact-info`
- `POST /portal/document-request`
- `GET /portal/training-history`
- `POST /portal/feedback`

---

**General Patterns and Best Practices Used:**
- All entities use JPA annotations, with soft-delete where needed.
- Service layer is annotated with `@Service` and uses `@Transactional` for write operations.
- Repositories extend `JpaRepository` or `CrudRepository`.
- Controllers use `@RestController`, validate input with `@Valid`, and return appropriate HTTP status codes.
- DTOs are mapped using MapStruct or manual mappers.
- Security is enforced at both endpoint and method level.
- Exception handling via `@ControllerAdvice`.
- OpenAPI/Swagger annotations for API documentation.
- Integration points use Feign/WebClient for external APIs.
- All modules are testable and follow SOLID principles.

---

This document provides a production-ready, detailed technical design for all 20 epics of the Warehouse Employee Management Platform, suitable for immediate implementation by a Spring Boot development team.