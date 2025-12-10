# Technical Design Document

## Warehouse Employee Management System - Low-Level Technical Design

---

### Epic E01: Project Scaffolding & Domain Setup

#### User Story 1: Initialize Spring Boot (Maven) Project

**Section: Spring Boot Architecture Overview**
Description: Establish a standardized Spring Boot project using Maven, with modular separation for core domains (employee, scheduling, attendance, safety).
Design Specification:
- Use Spring Boot 3.x, Java 17+, Maven build system
- Modules: employee, scheduling, attendance, safety
- Enable Spring Actuator for health checks
- Integrate Flyway/Liquibase for DB migrations
- Base package: com.wms
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
Description: Organize code into domain-driven packages for maintainability.
Design Specification:
- com.wms.employee
- com.wms.scheduling
- com.wms.attendance
- com.wms.safety
- com.wms.config
- com.wms.common
Sample Implementation:
```
src/main/java/com/wms/employee/
src/main/java/com/wms/scheduling/
src/main/java/com/wms/attendance/
src/main/java/com/wms/safety/
src/main/java/com/wms/config/
src/main/java/com/wms/common/
```

**Section: Configuration & Health Check**
Description: Enable Actuator and DB migration tools.
Design Specification:
- application.properties for port, actuator, migration config
Sample Implementation:
```properties
server.port=8080
management.endpoints.web.exposure.include=health,info
spring.flyway.enabled=true
spring.datasource.url=jdbc:postgresql://localhost:5432/wms
```

---

### Epic E02: Employee Master Data (CRUD)

#### User Story 5: Create Employee Domain with CRUD APIs

**Section: Spring Boot Architecture Overview**
Description: Implement Employee entity and RESTful CRUD endpoints.
Design Specification:
- Employee domain model
- REST controller for CRUD
- Service layer for business logic
- Repository for persistence
Sample Implementation:
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
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted = false;
    // getters/setters
}
```

**Section: Repository Layer**
Description: Use Spring Data JPA for persistence.
Design Specification:
- EmployeeRepository extends JpaRepository
Sample Implementation:
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

**Section: Service Layer**
Description: Encapsulate business logic and transaction management.
Design Specification:
- EmployeeService with CRUD methods
- Soft-delete implementation
Sample Implementation:
```java
@Service
public class EmployeeService {
    @Transactional
    public Employee create(EmployeeDto dto) { /* ... */ }
    @Transactional(readOnly = true)
    public Page<Employee> list(Pageable pageable) { /* ... */ }
    @Transactional
    public void softDelete(Long id) { /* ... */ }
}
```

**Section: Controller Layer**
Description: Expose REST endpoints for CRUD operations.
Design Specification:
- /employees endpoint
- DTOs for requests/responses
Sample Implementation:
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@RequestBody EmployeeDto dto) { /* ... */ }
    @GetMapping
    public Page<EmployeeDto> list(Pageable pageable) { /* ... */ }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { /* ... */ }
}
```

**Section: Security Configuration**
Description: Restrict access to authorized roles.
Design Specification:
- Method security with @PreAuthorize
Sample Implementation:
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
public ResponseEntity<EmployeeDto> create(@RequestBody EmployeeDto dto) { /* ... */ }
```

**Section: Integration Points**
Description: OpenAPI documentation, external HRIS sync
Design Specification:
- Swagger/OpenAPI annotations
Sample Implementation:
```java
@Operation(summary = "Create new employee", ...)
```

---

### Epic E03: Role-Based Access Control (RBAC)

#### User Story 10: Add Spring Security with Roles

**Section: Spring Boot Architecture Overview**
Description: Implement RBAC using Spring Security.
Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Endpoint/method security
- API key/OAuth2 toggle
Sample Implementation:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR")
                .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            .and()
            .oauth2Login();
    }
}
```

**Section: Package Structure**
Design Specification:
- com.wms.security
Sample Implementation:
```
src/main/java/com/wms/security/
```

**Section: Security Configuration**
Description: API key/OAuth2 toggle via config
Design Specification:
- Use @ConditionalOnProperty for toggling
Sample Implementation:
```java
@Bean
@ConditionalOnProperty(name = "security.mode", havingValue = "apikey")
public ApiKeyFilter apiKeyFilter() { /* ... */ }
```

---

### Epic E04: Time & Attendance (Clock In/Out)

#### User Story 16: Endpoints for Clock-In/Out Events

**Section: Spring Boot Architecture Overview**
Description: Track clock-in/out events, associate with shifts, handle corrections.
Design Specification:
- AttendanceEvent entity
- REST endpoints for clock-in/out
- Service for calculations
Sample Implementation:
```java
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime timestamp;
    private String type; // CLOCK_IN, CLOCK_OUT
    private String deviceId;
    private String location;
    // getters/setters
}
```

**Section: Controller Layer**
Design Specification:
- /attendance/clock-in and /attendance/clock-out endpoints
Sample Implementation:
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<Void> clockIn(@RequestBody ClockEventDto dto) { /* ... */ }
    @PostMapping("/clock-out")
    public ResponseEntity<Void> clockOut(@RequestBody ClockEventDto dto) { /* ... */ }
}
```

**Section: Service Layer**
Design Specification:
- Calculate hours worked
- Handle missed punches
Sample Implementation:
```java
@Service
public class AttendanceService {
    @Transactional
    public void clockIn(Long employeeId, ClockEventDto dto) { /* ... */ }
    @Transactional
    public void clockOut(Long employeeId, ClockEventDto dto) { /* ... */ }
    public DailyTotals calculateTotals(Long employeeId, LocalDate date) { /* ... */ }
}
```

---

### Epic E05: Shift & Schedule Management

#### User Story 22: Create Recurring Shift Templates

**Section: Entity Design**
Description: ShiftTemplate entity for recurring shifts.
Design Specification:
- Fields: id, name, startTime, endTime, recurrencePattern, blackoutDates
Sample Implementation:
```java
@Entity
@Table(name = "shift_templates")
public class ShiftTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrencePattern; // e.g., "WEEKLY"
    @ElementCollection
    private List<LocalDate> blackoutDates;
    // getters/setters
}
```

**Section: Controller Layer**
Design Specification:
- /shifts/templates CRUD endpoints
Sample Implementation:
```java
@RestController
@RequestMapping("/shifts/templates")
public class ShiftTemplateController {
    @PostMapping
    public ResponseEntity<ShiftTemplateDto> create(@RequestBody ShiftTemplateDto dto) { /* ... */ }
    @GetMapping
    public List<ShiftTemplateDto> list() { /* ... */ }
}
```

---

### Epic E06: Leave & Absence Management

#### User Story 27: Request/Approve PTO, Sick, Unpaid Leave

**Section: Entity Design**
Description: LeaveRequest entity for PTO/sick/unpaid leave.
Design Specification:
- Fields: id, employee, type, startDate, endDate, status, accrualBalance
Sample Implementation:
```java
@Entity
@Table(name = "leave_requests")
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type; // PTO, SICK, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // REQUESTED, APPROVED, DENIED
    private int accrualBalance;
    // getters/setters
}
```

**Section: Controller Layer**
Design Specification:
- /leave/requests endpoints
Sample Implementation:
```java
@RestController
@RequestMapping("/leave/requests")
public class LeaveRequestController {
    @PostMapping
    public ResponseEntity<LeaveRequestDto> requestLeave(@RequestBody LeaveRequestDto dto) { /* ... */ }
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id) { /* ... */ }
}
```

---

### Epic E07: Training & Certification Tracking

#### User Story 31: Track Required Certifications

**Section: Entity Design**
Description: Certification entity for tracking employee certifications.
Design Specification:
- Fields: id, employee, type, expiryDate, documentUrl
Sample Implementation:
```java
@Entity
@Table(name = "certifications")
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type;
    private LocalDate expiryDate;
    private String documentUrl;
    // getters/setters
}
```

**Section: Controller Layer**
Design Specification:
- /certifications CRUD endpoints
Sample Implementation:
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<CertificationDto> create(@RequestBody CertificationDto dto) { /* ... */ }
    @GetMapping
    public List<CertificationDto> list(@RequestParam Long employeeId) { /* ... */ }
}
```

---

### Epic E08: Safety Incidents & OSHA Reporting

#### User Story 35: Record Incidents/Near-Misses

**Section: Entity Design**
Description: SafetyIncident entity for OSHA reporting.
Design Specification:
- Fields: id, severity, location, description, involvedEmployees, status
Sample Implementation:
```java
@Entity
@Table(name = "safety_incidents")
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String severity;
    private String location;
    private String description;
    @ManyToMany
    private List<Employee> involvedEmployees;
    private String status; // OPEN, INVESTIGATING, RESOLVED
    // getters/setters
}
```

**Section: Controller Layer**
Design Specification:
- /safety/incidents endpoints
Sample Implementation:
```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
    @PostMapping
    public ResponseEntity<SafetyIncidentDto> report(@RequestBody SafetyIncidentDto dto) { /* ... */ }
    @GetMapping
    public List<SafetyIncidentDto> list() { /* ... */ }
}
```

---

### Epic E09: Equipment & Asset Assignment

#### User Story 39: Assign Scanners, Forklifts, PPE

**Section: Entity Design**
Description: Asset entity for equipment assignment.
Design Specification:
- Fields: id, type, condition, assignedTo, checkoutDate, returnDate
Sample Implementation:
```java
@Entity
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type; // SCANNER, FORKLIFT, PPE
    private String condition;
    @ManyToOne
    private Employee assignedTo;
    private LocalDateTime checkoutDate;
    private LocalDateTime returnDate;
    // getters/setters
}
```

**Section: Controller Layer**
Design Specification:
- /assets endpoints
Sample Implementation:
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign")
    public ResponseEntity<Void> assign(@RequestBody AssetAssignmentDto dto) { /* ... */ }
    @PostMapping("/return")
    public ResponseEntity<Void> returnAsset(@RequestBody AssetReturnDto dto) { /* ... */ }
}
```

---

### Epic E10: Performance Reviews & Goals

#### User Story 44: Create Review Templates

**Section: Entity Design**
Description: PerformanceReview entity for tracking reviews and goals.
Design Specification:
- Fields: id, employee, cycle, goals, competencies, ratings, comments, status
Sample Implementation:
```java
@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Employee employee;
    private String cycle; // Q1, Q2, Annual
    private String goals;
    private String competencies;
    private String ratings;
    private String comments;
    private String status; // DRAFT, SUBMITTED, ACKNOWLEDGED
    // getters/setters
}
```

**Section: Controller Layer**
Design Specification:
- /reviews endpoints
Sample Implementation:
```java
@RestController
@RequestMapping("/reviews")
public class PerformanceReviewController {
    @PostMapping
    public ResponseEntity<PerformanceReviewDto> create(@RequestBody PerformanceReviewDto dto) { /* ... */ }
    @GetMapping
    public List<PerformanceReviewDto> list(@RequestParam Long employeeId) { /* ... */ }
}
```

---

### Epic E11: Payroll Export Integration

#### User Story 48: Generate Payroll-Ready Files

**Section: Integration Points**
Description: Export payroll files to external provider formats via SFTP/API.
Design Specification:
- PayrollExportService for file generation
- Secure delivery via SFTP/API
Sample Implementation:
```java
@Service
public class PayrollExportService {
    public File generatePayrollFile(LocalDate period) { /* ... */ }
    public void deliver(File file) { /* ... */ }
}
```

---

### Epic E12: Notifications & Announcements

#### User Story 51: In-App and Email/SMS Notifications

**Section: Integration Points**
Description: NotificationService for multi-channel delivery.
Design Specification:
- In-app, email, SMS channels
- Opt-in/out per user
Sample Implementation:
```java
@Service
public class NotificationService {
    public void sendNotification(NotificationDto dto) { /* ... */ }
}
```

---

### Epic E13: Integration Layer (HRIS/WMS APIs)

#### User Story 56: Expose REST APIs and Connectors

**Section: Integration Points**
Description: HRIS/WMS API connectors, JWT/OAuth2 security.
Design Specification:
- HRISSyncJob for employee sync
- WMSConnector for department/location
Sample Implementation:
```java
@Service
public class HRISSyncJob {
    @Scheduled(cron = "0 0 * * * ?")
    public void syncEmployees() { /* ... */ }
}
```

---

### Epic E14: Audit Trail & Compliance

#### User Story 60: Centralized Audit Logging

**Section: Entity Design**
Description: AuditLog entity for immutable change tracking.
Design Specification:
- Fields: id, actor, timestamp, entity, before, after
Sample Implementation:
```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    @Lob
    private String before;
    @Lob
    private String after;
    // getters/setters
}
```

---

### Epic E15: Reporting & Analytics

#### User Story 63: Operational Reports

**Section: Controller Layer**
Description: Reporting endpoints for attendance, overtime, leave, certifications, safety KPIs.
Design Specification:
- /reports endpoints
Sample Implementation:
```java
@RestController
@RequestMapping("/reports")
public class ReportController {
    @GetMapping("/attendance")
    public ResponseEntity<ReportDto> attendance(@RequestParam ...) { /* ... */ }
}
```

---

### Epic E16: Mobile Access (PWA)

#### User Story 68: Responsive Views for Workers

**Section: Integration Points**
Description: PWA manifest, offline queue for clock events.
Design Specification:
- PWA manifest.json
- Service worker for offline support
Sample Implementation:
```json
{
  "name": "Warehouse Employee Management",
  "short_name": "WMS",
  "start_url": "/",
  "display": "standalone"
}
```

---

### Epic E17: Onboarding & Offboarding Workflow

#### User Story 71: Automated Provisioning

**Section: Integration Points**
Description: OnboardingService for new hire provisioning.
Design Specification:
- Triggered by HRIS sync
Sample Implementation:
```java
@Service
public class OnboardingService {
    public void provisionNewHire(Employee employee) { /* ... */ }
}
```

---

### Epic E18: Localization & Multi-Tenant

#### User Story 73: Tenant Isolation

**Section: Entity Design**
Description: Add tenantId to all entities.
Design Specification:
- Tenant ID in all entities
Sample Implementation:
```java
@Entity
public class Employee {
    private String tenantId;
    // ...
}
```

---

### Epic E19: Observability & Monitoring

#### User Story 75: Prometheus Metrics

**Section: Configuration**
Description: Enable Prometheus metrics via Actuator.
Design Specification:
- /actuator/prometheus endpoint
Sample Implementation:
```properties
management.endpoints.web.exposure.include=prometheus
```

---

### Epic E20: CI/CD & Deployment Automation

#### User Story 76: CI/CD Pipeline

**Section: Integration Points**
Description: GitHub Actions/Jenkins pipeline for build, test, deploy.
Design Specification:
- Pipeline stages: build, test, push image, deploy
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
```

---

**End of Technical Design Document**