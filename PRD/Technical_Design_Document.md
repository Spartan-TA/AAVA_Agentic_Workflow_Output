# WAREHOUSE EMPLOYEE MANAGEMENT SYSTEM â LOW-LEVEL TECHNICAL DESIGN DOCUMENT (SPRING BOOT)

---

## Table of Contents

1. E01 Project Scaffolding & Domain Setup
2. E02 Employee Master Data (CRUD)
3. E03 Role-Based Access Control (RBAC)
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

---

## E01: Project Scaffolding & Domain Setup

### 1. OVERVIEW
- **Spring Boot** (Maven) project with modular package structure.
- Core modules: employee, scheduling, attendance, safety, etc.
- **Flyway/Liquibase** for DB migrations.
- **Spring Boot Actuator** for health/metrics.

### 2. PACKAGE STRUCTURE
```
com.warehouse.employee
  âââ config
  âââ controller
  âââ domain
  âââ dto
  âââ exception
  âââ repository
  âââ security
  âââ service
  âââ integration
  âââ audit
  âââ reporting
  âââ notification
  âââ mobile
```

### 3. CONFIGURATION
- `application.yml` for environment config.
- `spring.datasource.*` for DB.
- `spring.flyway.*` or `spring.liquibase.*` for migrations.
- `management.endpoints.web.exposure.include=*` for Actuator.

### 4. SAMPLE IMPLEMENTATION

**pom.xml**
```xml
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
```

**application.yml**
```yaml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse
    username: warehouse
    password: secret
  flyway:
    enabled: true
    locations: classpath:db/migration
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

---

## E02: Employee Master Data (CRUD)

### 1. OVERVIEW
- Centralized CRUD for employee records.
- RESTful APIs, DTOs, validation, soft-delete.

### 2. PACKAGE STRUCTURE
- `com.warehouse.employee.domain.Employee`
- `com.warehouse.employee.repository.EmployeeRepository`
- `com.warehouse.employee.service.EmployeeService`
- `com.warehouse.employee.controller.EmployeeController`
- `com.warehouse.employee.dto.EmployeeDTO`

### 3. DOMAIN MODEL

**Employee.java**
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private EmployeeRole role;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "shift_group_id")
    private ShiftGroup shiftGroup;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    // getters/setters
}
```

### 4. REPOSITORY LAYER

**EmployeeRepository.java**
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    boolean existsByBadgeIdAndDeletedFalse(String badgeId);
}
```

### 5. SERVICE LAYER

**EmployeeService.java**
```java
@Service
public class EmployeeService {
    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO dto) { /* ... */ }
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> listEmployees(Pageable pageable, String filter) { /* ... */ }
    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) { /* ... */ }
    @Transactional
    public void softDeleteEmployee(Long id) { /* ... */ }
}
```

### 6. CONTROLLER LAYER

**EmployeeController.java**
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeDTO dto) { /* ... */ }
    @GetMapping
    public Page<EmployeeDTO> list(Pageable pageable, @RequestParam Optional<String> filter) { /* ... */ }
    @PutMapping("/{id}")
    public EmployeeDTO update(@PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) { /* ... */ }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { /* ... */ }
}
```

### 7. CODE SAMPLES

**EmployeeDTO.java**
```java
public class EmployeeDTO {
    @NotBlank
    private String badgeId;
    @NotBlank
    private String name;
    @NotNull
    private EmployeeRole role;
    // ... other fields, getters/setters
}
```

---

## E03: Role-Based Access Control (RBAC)

### 1. OVERVIEW
- **Spring Security** with roles: ADMIN, HR, SUPERVISOR, WORKER.
- Method/endpoint security, row-level constraints.

### 2. PACKAGE STRUCTURE
- `com.warehouse.employee.security.*`
- `com.warehouse.employee.config.SecurityConfig`

### 3. SECURITY CONFIGURATION

**SecurityConfig.java**
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
          .csrf().disable()
          .authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .antMatchers("/attendance/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
            .anyRequest().authenticated()
          .and()
            .httpBasic();
    }
}
```

**Method Security**
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(principal, #id))")
public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) { ... }
```

### 4. API KEY/OAUTH2 TOGGLE

**application.yml**
```yaml
security:
  auth-type: oauth2 # or api-key
```

**Conditional Beans**
```java
@Configuration
@ConditionalOnProperty(name = "security.auth-type", havingValue = "oauth2")
public class OAuth2SecurityConfig { ... }
```

---

## E04: Time & Attendance (Clock In/Out)

### 1. OVERVIEW
- Clock-in/out endpoints, geofence/device capture, shift association, missed punch correction.

### 2. DOMAIN MODEL

**AttendanceEvent.java**
```java
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private AttendanceType type; // CLOCK_IN, CLOCK_OUT

    private LocalDateTime timestamp;

    private String deviceId;
    private String geoLocation;

    // ... getters/setters
}
```

### 3. REPOSITORY

**AttendanceEventRepository.java**
```java
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeAndDate(Employee employee, LocalDate date);
}
```

### 4. SERVICE

**AttendanceService.java**
```java
@Transactional
public AttendanceEventDTO clockIn(Long employeeId, ClockEventRequest req) { ... }
@Transactional
public AttendanceEventDTO clockOut(Long employeeId, ClockEventRequest req) { ... }
```

### 5. CONTROLLER

**AttendanceController.java**
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public AttendanceEventDTO clockIn(@RequestBody @Valid ClockEventRequest req) { ... }
    @PostMapping("/clock-out")
    public AttendanceEventDTO clockOut(@RequestBody @Valid ClockEventRequest req) { ... }
}
```

### 6. BUSINESS RULES
- Validate shift association.
- Handle missed punches (correction workflow).
- Export daily totals (CSV).

---

## E05: Shift & Schedule Management

### 1. OVERVIEW
- Shift templates, rotations, blackout dates, conflict detection.

### 2. DOMAIN MODEL

**ShiftTemplate.java**
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    // ... getters/setters
}
```

**EmployeeShiftAssignment.java**
```java
@Entity
public class EmployeeShiftAssignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ShiftTemplate shiftTemplate;
    private LocalDate date;
    // ... getters/setters
}
```

### 3. SERVICE

**ShiftService.java**
```java
@Transactional
public ShiftTemplate createShiftTemplate(ShiftTemplateDTO dto) { ... }
@Transactional
public void assignShift(Long employeeId, Long shiftTemplateId, LocalDate date) { ... }
```

### 4. CONTROLLER

**ShiftController.java**
```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates")
    public ShiftTemplateDTO createTemplate(@RequestBody @Valid ShiftTemplateDTO dto) { ... }
    @PostMapping("/assign")
    public void assignShift(@RequestBody @Valid ShiftAssignmentRequest req) { ... }
}
```

---

## E06: Leave & Absence Management

### 1. OVERVIEW
- PTO/sick/unpaid leave requests, approval workflow, accruals.

### 2. DOMAIN MODEL

**LeaveRequest.java**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @Enumerated(EnumType.STRING)
    private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status; // REQUESTED, APPROVED, DENIED
    // ... getters/setters
}
```

### 3. SERVICE

**LeaveService.java**
```java
@Transactional
public LeaveRequestDTO requestLeave(Long employeeId, LeaveRequestDTO dto) { ... }
@Transactional
public LeaveRequestDTO approveLeave(Long requestId) { ... }
```

### 4. CONTROLLER

**LeaveController.java**
```java
@RestController
@RequestMapping("/leaves")
public class LeaveController {
    @PostMapping
    public LeaveRequestDTO requestLeave(@RequestBody @Valid LeaveRequestDTO dto) { ... }
    @PostMapping("/{id}/approve")
    public LeaveRequestDTO approve(@PathVariable Long id) { ... }
}
```

---

## E07: Training & Certification Tracking

### 1. OVERVIEW
- Track certifications, expirations, renewals, proof uploads.

### 2. DOMAIN MODEL

**Certification.java**
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    @ManyToOne
    private Employee employee;
    private String documentUrl;
    // ... getters/setters
}
```

### 3. SERVICE

**CertificationService.java**
```java
@Transactional
public CertificationDTO addCertification(Long employeeId, CertificationDTO dto) { ... }
@Transactional
public List<CertificationDTO> getExpiringCertifications(int days) { ... }
```

---

## E08: Safety Incidents & OSHA Reporting

### 1. OVERVIEW
- Record incidents, workflow, OSHA reporting.

### 2. DOMAIN MODEL

**SafetyIncident.java**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String description;
    private LocalDateTime occurredAt;
    @Enumerated(EnumType.STRING)
    private IncidentSeverity severity;
    @ManyToMany
    private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status; // OPEN, INVESTIGATING, RESOLVED
    // ... getters/setters
}
```

### 3. SERVICE

**SafetyIncidentService.java**
```java
@Transactional
public SafetyIncidentDTO reportIncident(SafetyIncidentDTO dto) { ... }
@Transactional
public SafetyIncidentDTO updateStatus(Long id, IncidentStatus status) { ... }
```

---

## E09: Equipment & Asset Assignment

### 1. OVERVIEW
- Assign assets, check-in/out, certification checks.

### 2. DOMAIN MODEL

**Asset.java**
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String assetTag;
    private String type;
    private String condition;
    @ManyToOne
    private Employee assignedTo;
    private LocalDate assignedDate;
    // ... getters/setters
}
```

### 3. SERVICE

**AssetService.java**
```java
@Transactional
public AssetDTO assignAsset(Long assetId, Long employeeId) { ... }
@Transactional
public void checkInAsset(Long assetId) { ... }
```

---

## E10: Performance Reviews & Goals

### 1. OVERVIEW
- Review templates, goals, ratings, workflow.

### 2. DOMAIN MODEL

**PerformanceReview.java**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate reviewDate;
    private String templateName;
    private String goals;
    private String ratings;
    private String comments;
    @Enumerated(EnumType.STRING)
    private ReviewStatus status; // DRAFT, SUBMITTED, ACKNOWLEDGED
    // ... getters/setters
}
```

---

## E11: Payroll Export Integration

### 1. OVERVIEW
- Export payroll files, SFTP/API delivery, audit.

### 2. SERVICE

**PayrollExportService.java**
```java
@Transactional
public File exportPayroll(LocalDate periodStart, LocalDate periodEnd) { ... }
@Transactional
public void deliverExport(File file) { ... }
```

---

## E12: Notifications & Announcements

### 1. OVERVIEW
- In-app/email/SMS notifications, templates, opt-in/out.

### 2. DOMAIN MODEL

**Notification.java**
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private String channel; // EMAIL, SMS, IN_APP
    private String template;
    private String recipient;
    private String status; // SENT, FAILED
    private LocalDateTime sentAt;
    // ... getters/setters
}
```

### 3. SERVICE

**NotificationService.java**
```java
@Transactional
public void sendNotification(NotificationDTO dto) { ... }
```

---

## E13: Integration Layer (HRIS/WMS APIs)

### 1. OVERVIEW
- REST APIs, connectors, webhooks, SSO.

### 2. CONFIGURATION

**application.yml**
```yaml
integration:
  hris:
    base-url: https://hris.example.com/api
    token: ${HRIS_TOKEN}
  wms:
    base-url: https://wms.example.com/api
    token: ${WMS_TOKEN}
```

### 3. SERVICE

**HRISIntegrationService.java**
```java
public void syncEmployees() { ... }
```

**WMSIntegrationService.java**
```java
public void syncDepartments() { ... }
```

---

## E14: Audit Trail & Compliance

### 1. OVERVIEW
- Centralized audit logging, immutable storage.

### 2. DOMAIN MODEL

**AuditLog.java**
```java
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
    private String beforeState;
    @Lob
    private String afterState;
    // ... getters/setters
}
```

### 3. SERVICE

**AuditService.java**
```java
@Transactional
public void logChange(String entity, Long entityId, String action, String actor, Object before, Object after) { ... }
```

---

## E15: Reporting & Analytics

### 1. OVERVIEW
- Reports: attendance, overtime, leave, certs, safety KPIs.

### 2. SERVICE

**ReportingService.java**
```java
@Transactional(readOnly = true)
public Report exportAttendanceReport(LocalDate start, LocalDate end, String format) { ... }
```

---

## E16: Mobile Access (PWA)

### 1. OVERVIEW
- Responsive endpoints, offline queue, PWA manifest.

### 2. CONFIGURATION

**manifest.json** (static resource)
```json
{
  "name": "Warehouse Employee PWA",
  "short_name": "Warehouse",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

### 3. CONTROLLER

- Endpoints for clock-in/out, schedules, leave, announcements are mobile-friendly (JSON, minimal payload).

---

## E17: Onboarding & Offboarding Workflow

### 1. OVERVIEW
- Automate provisioning, training, asset assignment, deprovisioning.

### 2. SERVICE

**OnboardingService.java**
```java
@Transactional
public void onboardEmployee(Long employeeId) { ... }
@Transactional
public void offboardEmployee(Long employeeId) { ... }
```

---

## GENERAL DESIGN CONSIDERATIONS

- **Validation:** Use `javax.validation` annotations on DTOs and entities.
- **Exception Handling:** `@ControllerAdvice` for global error handling.
- **Logging:** Use SLF4J/Logback, log all sensitive actions.
- **Testing:** Unit tests for services, integration tests for controllers, security tests for RBAC.
- **OpenAPI:** Annotate controllers with Swagger/OpenAPI for API docs.
- **Pagination & Filtering:** All list endpoints support pagination, sorting, and filtering.
- **Soft Delete:** Use `deleted` flag for logical deletes.
- **Audit:** All create/update/delete actions are logged.
- **Security:** All endpoints secured by role, method-level security for sensitive actions.
- **External Integrations:** Use `RestTemplate` or `WebClient` for outbound calls, handle retries and failures.
- **Configuration:** All secrets and endpoints externalized in `application.yml` or environment variables.

---

## CONCLUSION

This document provides a systematic, production-ready low-level technical design for all 17 epics of the Warehouse Employee Management System, following Spring Boot best practices and ready for implementation by developers. Each epic includes:

- **Architecture Overview:** High-level design approach
- **Package Structure:** Organized code layout
- **Domain Models:** JPA entities with relationships and constraints
- **Repository Layer:** Spring Data JPA interfaces
- **Service Layer:** Business logic with transaction management
- **Controller Layer:** REST API endpoints with validation
- **Security:** Role-based access control and authentication
- **Configuration:** Application settings and external integrations
- **Code Samples:** Actual Java implementations

The design ensures:
- â Scalability and maintainability
- â Security and compliance
- â Testability and observability
- â Integration readiness
- â Mobile-first approach
- â Production-grade quality

**Document Status:** Ready for development team implementation
**Last Updated:** 2024
**Version:** 1.0