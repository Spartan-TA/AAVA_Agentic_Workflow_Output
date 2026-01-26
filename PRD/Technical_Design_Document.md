# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

---

## Table of Contents

1. [E01: Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
2. [E02: Employee Master Data (CRUD)](#e02-employee-master-data-crud)
3. [E03: Role Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
4. [E04: Time & Attendance (Clock In/Out)](#e04-time--attendance-clock-inout)
5. [E05: Shift & Schedule Management](#e05-shift--schedule-management)
6. [E06: Leave & Absence Management](#e06-leave--absence-management)
7. [E07: Training & Certification Tracking](#e07-training--certification-tracking)
8. [E08: Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
9. [E09: Equipment & Asset Assignment](#e09-equipment--asset-assignment)
10. [E10: Performance Reviews & Goals](#e10-performance-reviews--goals)
11. [E11: Payroll Export Integration](#e11-payroll-export-integration)
12. [E12: Notifications & Announcements](#e12-notifications--announcements)
13. [E13: Integration Layer (HRIS/WMS APIs)](#e13-integration-layer-hriswms-apis)
14. [E14: Audit Trail & Compliance](#e14-audit-trail--compliance)
15. [E15: Reporting & Analytics](#e15-reporting--analytics)
16. [E16: Mobile Access (PWA)](#e16-mobile-access-pwa)
17. [E17: Onboarding & Offboarding Workflow](#e17-onboarding--offboarding-workflow)

---

## <a name="e01-project-scaffolding--domain-setup"></a>E01: Project Scaffolding & Domain Setup

### Description
Initialize Spring Boot (Maven) project with standardized base packages, core modules (employee, scheduling, attendance, safety), Flyway/Liquibase for DB migrations, and Actuator for health monitoring.

### Architecture Overview
- **Layered Architecture:** Controller â Service â Repository â Entity
- **Modules:** employee, schedule, attendance, safety, common, config, integration, notification, audit, reporting
- **Build Tool:** Maven
- **Database Migration:** Flyway or Liquibase
- **Monitoring:** Spring Boot Actuator

### Package Structure
```
com.wms.ems
âââ config
âââ employee
âââ schedule
âââ attendance
âââ safety
âââ training
âââ asset
âââ performance
âââ payroll
âââ notification
âââ integration
âââ audit
âââ reporting
âââ mobile
âââ onboarding
```

### Configuration & Security
- `application.yml` for environment configs
- Flyway/Liquibase migration scripts in `src/main/resources/db/migration`
- Actuator endpoints enabled (`/actuator/health`, `/actuator/info`)
- Profiles: `dev`, `test`, `prod`

### Sample Code Snippet
```java
@SpringBootApplication
public class EmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmsApplication.class, args);
    }
}
```
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ems
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

## <a name="e02-employee-master-data-crud"></a>E02: Employee Master Data (CRUD)

### Description
CRUD APIs for Employee domain: name, badgeId, role, department, shiftGroup, hireDate, status. Supports pagination, filtering, and soft-delete.

### Architecture Overview
- **Domain Model:** Employee
- **DTOs:** EmployeeRequestDTO, EmployeeResponseDTO
- **Repository:** Spring Data JPA
- **Service:** Business logic, validation, soft-delete
- **Controller:** REST endpoints

### Package Structure
```
com.wms.ems.employee
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### Entity Design
```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String badgeId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne
    private Department department;

    @ManyToOne
    private ShiftGroup shiftGroup;

    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    private boolean deleted = false;
    // getters/setters
}
```

### Service Layer
```java
@Service
public class EmployeeService {
    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) { ... }
    public Page<EmployeeResponseDTO> getEmployees(Pageable pageable, EmployeeFilter filter) { ... }
    @Transactional
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto) { ... }
    @Transactional
    public void softDeleteEmployee(Long id) { ... }
}
```

### Repository Layer
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> create(@Valid @RequestBody EmployeeRequestDTO dto) { ... }
    @GetMapping
    public Page<EmployeeResponseDTO> list(Pageable pageable, EmployeeFilter filter) { ... }
    @PutMapping("/{id}")
    public EmployeeResponseDTO update(@PathVariable Long id, @RequestBody EmployeeRequestDTO dto) { ... }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { ... }
}
```

### Validation & Exception Handling
- Use `@Valid` and validation annotations in DTOs
- Global exception handler with `@ControllerAdvice`

### OpenAPI Integration
- Annotate controllers with Swagger/OpenAPI for schema generation

---

## <a name="e03-role-based-access-control-rbac"></a>E03: Role Based Access Control (RBAC)

### Description
Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER), method/endpoint security, API key/OAuth2 toggle.

### Architecture Overview
- **SecurityConfig:** Configures authentication, authorization, and role hierarchy
- **UserDetailsService:** Loads user and roles
- **API Key/OAuth2:** Toggle via config

### Package Structure
```
com.wms.ems.config
âââ SecurityConfig.java
âââ ApiKeyAuthFilter.java
âââ OAuth2Config.java
```

### Security Configuration
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            .and()
            .oauth2Login()
            .and()
            .addFilterBefore(new ApiKeyAuthFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
```

### Role Enum
```java
public enum Role {
    ADMIN, HR, SUPERVISOR, WORKER
}
```

### Method Security
```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteEmployee(Long id) { ... }
```

### Exception Handling
- 401 for unauthorized, 403 for forbidden
- Custom `AccessDeniedHandler`

---

## <a name="e04-time--attendance-clock-inout"></a>E04: Time & Attendance (Clock In/Out)

### Description
Endpoints for clock-in/out events with geofence/device capture, calculate hours, handle missed punches and corrections.

### Architecture Overview
- **Domain Model:** AttendanceEvent
- **Service:** Clock-in/out logic, geofence validation, correction workflow
- **Controller:** REST endpoints

### Package Structure
```
com.wms.ems.attendance
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### Entity Design
```java
@Entity
public class AttendanceEvent {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Employee employee;

    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private String deviceId;
    private String location;
    private boolean correctionRequested;
    // getters/setters
}
```

### Service Layer
```java
@Service
public class AttendanceService {
    public AttendanceEvent clockIn(Long employeeId, ClockInRequestDTO dto) { ... }
    public AttendanceEvent clockOut(Long employeeId, ClockOutRequestDTO dto) { ... }
    public void requestCorrection(Long eventId, CorrectionRequestDTO dto) { ... }
    public Duration calculateHours(Long employeeId, LocalDate date) { ... }
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockInRequestDTO dto) { ... }
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockOutRequestDTO dto) { ... }
    @PostMapping("/correction")
    public ResponseEntity<?> requestCorrection(@RequestBody CorrectionRequestDTO dto) { ... }
}
```

### Geofence Validation (Pseudo-code)
```java
if (!geofenceService.isWithinAllowedArea(dto.getLocation())) {
    throw new GeofenceViolationException();
}
```

---

## <a name="e05-shift--schedule-management"></a>E05: Shift & Schedule Management

### Description
Shift templates, rotations, overtime rules, assignment to employees, conflict detection.

### Architecture Overview
- **Domain Models:** ShiftTemplate, ShiftAssignment
- **Service:** Shift creation, assignment, conflict detection
- **Controller:** CRUD endpoints

### Package Structure
```
com.wms.ems.schedule
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### Entity Design
```java
@Entity
public class ShiftTemplate {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isOvertimeAllowed;
    // getters/setters
}

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
    // getters/setters
}
```

### Service Layer
```java
@Service
public class ShiftService {
    public ShiftTemplate createTemplate(ShiftTemplateDTO dto) { ... }
    public void assignShift(Long employeeId, ShiftAssignmentDTO dto) { ... }
    public boolean hasConflict(Long employeeId, LocalDate date) { ... }
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates")
    public ShiftTemplate createTemplate(@RequestBody ShiftTemplateDTO dto) { ... }
    @PostMapping("/assign")
    public void assignShift(@RequestBody ShiftAssignmentDTO dto) { ... }
}
```

---

## <a name="e06-leave--absence-management"></a>E06: Leave & Absence Management

### Description
Request/approve PTO, sick, unpaid leave; accrual balances; scheduling integration.

### Architecture Overview
- **Domain Models:** LeaveRequest, LeaveBalance
- **Service:** Leave request, approval, accrual calculation
- **Controller:** Endpoints for request/approval

### Package Structure
```
com.wms.ems.leave
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### Entity Design
```java
@Entity
public class LeaveRequest {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @Enumerated(EnumType.STRING)
    private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;
    // getters/setters
}

@Entity
public class LeaveBalance {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private int ptoBalance;
    private int sickBalance;
    private int unpaidBalance;
    // getters/setters
}
```

### Service Layer
```java
@Service
public class LeaveService {
    public LeaveRequest requestLeave(LeaveRequestDTO dto) { ... }
    public void approveLeave(Long requestId) { ... }
    public void denyLeave(Long requestId) { ... }
    public void updateAccruals(Long employeeId) { ... }
}
```

---

## <a name="e07-training--certification-tracking"></a>E07: Training & Certification Tracking

### Description
Track certifications (e.g., forklift), expirations, renewals, block assignments for expired certs.

### Architecture Overview
- **Domain Models:** Certification, EmployeeCertification
- **Service:** Certification tracking, expiry alerts
- **Controller:** CRUD endpoints

### Package Structure
```
com.wms.ems.training
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### Entity Design
```java
@Entity
public class Certification {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private int validityMonths;
    // getters/setters
}

@Entity
public class EmployeeCertification {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private Certification certification;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
    // getters/setters
}
```

### Service Layer
```java
@Service
public class CertificationService {
    public void assignCertification(Long employeeId, CertificationDTO dto) { ... }
    public List<EmployeeCertification> getExpiringCerts(int days) { ... }
}
```

---

## <a name="e08-safety-incidents--osha-reporting"></a>E08: Safety Incidents & OSHA Reporting

### Description
Record incidents/near-misses, workflow for investigation, OSHA reporting.

### Architecture Overview
- **Domain Models:** SafetyIncident
- **Service:** Incident workflow, OSHA export
- **Controller:** Incident endpoints

### Package Structure
```
com.wms.ems.safety
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### Entity Design
```java
@Entity
public class SafetyIncident {
    @Id
    @GeneratedValue
    private Long id;
    private String description;
    private String location;
    private IncidentSeverity severity;
    @ManyToMany
    private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
    private LocalDateTime reportedAt;
    // getters/setters
}
```

### Service Layer
```java
@Service
public class SafetyService {
    public SafetyIncident reportIncident(SafetyIncidentDTO dto) { ... }
    public void updateStatus(Long incidentId, IncidentStatus status) { ... }
    public File exportOshaReport(LocalDate from, LocalDate to) { ... }
}
```

---

## <a name="e09-equipment--asset-assignment"></a>E09: Equipment & Asset Assignment

### Description
Assign scanners, forklifts, PPE to employees; track checkout/return; certification validation.

### Architecture Overview
- **Domain Models:** Asset, AssetAssignment
- **Service:** Asset assignment, return, validation
- **Controller:** Asset endpoints

### Package Structure
```
com.wms.ems.asset
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### Entity Design
```java
@Entity
public class Asset {
    @Id
    @GeneratedValue
    private Long id;
    private String assetTag;
    private String type;
    private AssetCondition condition;
    // getters/setters
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
    private LocalDateTime checkoutTime;
    private LocalDateTime returnTime;
    // getters/setters
}
```

### Service Layer
```java
@Service
public class AssetService {
    public void assignAsset(Long assetId, Long employeeId) { ... }
    public void returnAsset(Long assignmentId) { ... }
    public List<AssetAssignment> getOverdueAssets() { ... }
}
```

---

## <a name="e10-performance-reviews--goals"></a>E10: Performance Reviews & Goals

### Description
Quarterly/annual review templates, goals, competencies, ratings, supervisor/employee acknowledgements.

### Architecture Overview
- **Domain Models:** PerformanceReview, ReviewTemplate, Goal
- **Service:** Review workflow, goal tracking
- **Controller:** Review endpoints

### Package Structure
```
com.wms.ems.performance
âââ controller
âââ service
âââ repository
âââ model
âââ dto
```

### Entity Design
```java
@Entity
public class PerformanceReview {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ReviewTemplate template;
    private LocalDate reviewDate;
    private String supervisorComments;
    private String employeeComments;
    private boolean acknowledgedByEmployee;
    private boolean acknowledgedBySupervisor;
    // getters/setters
}

@Entity
public class Goal {
    @Id
    @GeneratedValue
    private Long id;
    private String description;
    private GoalStatus status;
    @ManyToOne
    private PerformanceReview review;
    // getters/setters
}
```

---

## <a name="e11-payroll-export-integration"></a>E11: Payroll Export Integration

### Description
Generate payroll files from attendance/leave, map to external formats, secure SFTP/API delivery.

### Architecture Overview
- **Service:** Payroll export, file generation, SFTP/API delivery
- **Integration:** External payroll provider

### Package Structure
```
com.wms.ems.payroll
âââ service
âââ integration
âââ dto
```

### Service Layer
```java
@Service
public class PayrollExportService {
    public File generatePayrollExport(LocalDate from, LocalDate to) { ... }
    public void deliverExport(File exportFile) { ... }
}
```

### Integration (Pseudo-code)
```java
public void deliverExport(File exportFile) {
    if (config.isSftpEnabled()) {
        sftpClient.upload(exportFile);
    } else {
        payrollApiClient.send(exportFile);
    }
}
```

---

## <a name="e12-notifications--announcements"></a>E12: Notifications & Announcements

### Description
In-app and email/SMS notifications for shift changes, expiring certs, approvals, announcements.

### Architecture Overview
- **Domain Models:** Notification, Announcement
- **Service:** Notification delivery, template management
- **Integration:** Email/SMS providers

### Package Structure
```
com.wms.ems.notification
âââ controller
âââ service
âââ integration
âââ model
âââ dto
```

### Entity Design
```java
@Entity
public class Notification {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee recipient;
    private String channel; // IN_APP, EMAIL, SMS
    private String message;
    private boolean delivered;
    private LocalDateTime sentAt;
    // getters/setters
}
```

### Service Layer
```java
@Service
public class NotificationService {
    public void sendNotification(NotificationDTO dto) { ... }
    public void sendAnnouncement(AnnouncementDTO dto) { ... }
}
```

---

## <a name="e13-integration-layer-hriswms-apis"></a>E13: Integration Layer (HRIS/WMS APIs)

### Description
Expose REST APIs and connectors for HRIS (new hires/terms), WMS (location/department), IDP for SSO, webhooks for events.

### Architecture Overview
- **Integration:** REST APIs, webhooks, SSO
- **Security:** JWT/OAuth2

### Package Structure
```
com.wms.ems.integration
âââ controller
âââ service
âââ client
âââ dto
```

### Controller Layer
```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    @PostMapping("/hris/webhook")
    public void handleHrisEvent(@RequestBody HrisEventDTO dto) { ... }
    @GetMapping("/wms/departments")
    public List<DepartmentDTO> getDepartments() { ... }
}
```

### Security
- JWT/OAuth2 for all integration endpoints

---

## <a name="e14-audit-trail--compliance"></a>E14: Audit Trail & Compliance

### Description
Centralized audit logging for sensitive changes with tamper-evident storage.

### Architecture Overview
- **Domain Model:** AuditLog
- **Service:** Audit logging, export

### Package Structure
```
com.wms.ems.audit
âââ service
âââ repository
âââ model
```

### Entity Design
```java
@Entity
public class AuditLog {
    @Id
    @GeneratedValue
    private Long id;
    private String entityName;
    private Long entityId;
    private String action;
    private String actor;
    private LocalDateTime timestamp;
    @Lob
    private String beforeState;
    @Lob
    private String afterState;
    // getters/setters
}
```

### Service Layer
```java
@Service
public class AuditService {
    public void logChange(String entity, Long id, String action, Object before, Object after) { ... }
    public List<AuditLog> exportLogs(LocalDate from, LocalDate to) { ... }
}
```

---

## <a name="e15-reporting--analytics"></a>E15: Reporting & Analytics

### Description
Operational reports for attendance, overtime, leave, certifications, safety KPIs with CSV/PDF export.

### Architecture Overview
- **Service:** Report generation, export
- **Controller:** Report endpoints

### Package Structure
```
com.wms.ems.reporting
âââ controller
âââ service
âââ dto
```

### Service Layer
```java
@Service
public class ReportingService {
    public File generateAttendanceReport(ReportFilterDTO filter) { ... }
    public File generateCertificationReport(ReportFilterDTO filter) { ... }
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> downloadAttendanceReport(ReportFilterDTO filter) { ... }
}
```

---

## <a name="e16-mobile-access-pwa"></a>E16: Mobile Access (PWA)

### Description
Responsive views for workers to clock-in/out, view schedules, request leave, see announcements; offline-friendly via PWA.

### Architecture Overview
- **Frontend:** PWA (React/Vue/Angular)
- **Backend:** REST APIs for mobile flows

### Backend Endpoints
- `/attendance/clock-in`
- `/attendance/clock-out`
- `/shifts/my`
- `/leave/request`
- `/notifications/my`

### PWA Features
- Manifest and service worker for offline support
- API endpoints queue offline actions and sync on reconnect

---

## <a name="e17-onboarding--offboarding-workflow"></a>E17: Onboarding & Offboarding Workflow

### Description
Automate provisioning/deprovisioning with HRIS integration, training tasks, asset collection.

### Architecture Overview
- **Service:** Onboarding/offboarding workflow
- **Integration:** HRIS, asset, training modules

### Package Structure
```
com.wms.ems.onboarding
âââ service
âââ dto
```

### Service Layer
```java
@Service
public class OnboardingService {
    public void onboardEmployee(HrisEventDTO dto) { ... }
    public void offboardEmployee(Long employeeId) { ... }
}
```

---

## General Spring Boot Best Practices

- **Layered Architecture:** Controller â Service â Repository â Entity
- **SOLID Principles:** Single Responsibility, Open/Closed, etc.
- **Spring Annotations:** `@Entity`, `@Service`, `@RestController`, `@Repository`, `@Component`
- **Exception Handling:** Global handler with `@ControllerAdvice`
- **DTOs:** For all API requests/responses
- **Validation:** `@Valid`, `@NotNull`, etc. in DTOs
- **Security:** Proper configuration, role-based access, method security
- **Spring Data JPA:** For all DB operations
- **Transaction Management:** `@Transactional` on service methods
- **OpenAPI/Swagger:** For API documentation

---

## Example Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiError("NOT_FOUND", ex.getMessage()));
    }
    // ... other handlers
}
```

---

## Example DTO with Validation

```java
public class EmployeeRequestDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String badgeId;
    @NotNull
    private Role role;
    // ... other fields, getters/setters
}
```

---

## Example OpenAPI Annotation

```java
@Operation(summary = "Create Employee", description = "Creates a new employee record")
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "Employee created"),
    @ApiResponse(responseCode = "400", description = "Validation error")
})
```

---

## Conclusion

This document provides a comprehensive low-level technical design for the Warehouse Employee Management System (EMS), covering all 17 epics with detailed architecture, package structure, domain models, service/repository/controller specifications, configuration, security, integration points, and code snippets. All design follows Spring Boot industry standards and best practices to ensure maintainability, scalability, and developer productivity.