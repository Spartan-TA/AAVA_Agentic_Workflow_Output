# Warehouse EMS System â Low-Level Technical Design Document (Spring Boot)

=========================================================================

**Table of Contents**
1. E01: Project Scaffolding & Domain Setup
2. E02: Employee Master Data (CRUD)
3. E03: Role Based Access Control (RBAC)
4. E04: Time & Attendance (Clock In/Out)
5. E05: Shift & Schedule Management
6. E06: Leave & Absence Management
7. E07: Training & Certification Tracking
8. E08: Safety Incidents & OSHA Reporting
9. E09: Equipment & Asset Assignment
10. E10: Performance Reviews & Goals
11. E11: Payroll Export Integration
12. E12: Notifications & Announcements
13. E13: Integration Layer (HRIS/WMS APIs)
14. E14: Audit Trail & Compliance
15. E15: Reporting & Analytics
16. E16: Mobile Access (PWA)
17. E17: Onboarding & Offboarding Workflow

=========================================================================

# E01: Project Scaffolding & Domain Setup

**Section: Overview of Spring Boot Architecture**

Description:
The Warehouse EMS system is structured as a modular Spring Boot application using Maven for dependency management. Core modules include employee, scheduling, attendance, and safety. Flyway/Liquibase is used for database migrations, and Spring Boot Actuator is enabled for monitoring.

**Design Specification:**
- Multi-module Maven project: `core`, `attendance`, `scheduling`, `safety`, `integration`, `web`
- Base package: `com.warehouse.ems`
- Configuration for Flyway/Liquibase in `src/main/resources`
- Actuator endpoints enabled in `application.yml`

**Sample Implementation:**
```java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems
    username: ems_user
    password: secret
  flyway:
    enabled: true
    locations: classpath:db/migration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,env
```

---

# E02: Employee Master Data (CRUD)

**Section: Overview of Spring Boot Architecture**

Description:
Implements CRUD for Employee domain with RESTful APIs, DTOs, and validation.

**Design Specification:**
- Package: `com.warehouse.ems.employee`
- Entity: `Employee`
- Repository: `EmployeeRepository extends JpaRepository`
- Service: `EmployeeService`
- Controller: `EmployeeController`
- DTOs: `EmployeeDto`, `EmployeeCreateRequest`, `EmployeeUpdateRequest`

**Sample Implementation:**
```java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    @Column(nullable=false)
    private String name;
    @Column(unique=true, nullable=false)
    private String badgeId;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    // getters/setters
}
```
```java
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    @GetMapping public List<EmployeeDto> getAll() { ... }
    @PostMapping public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeCreateRequest req) { ... }
    @PutMapping("/{id}") public EmployeeDto update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateRequest req) { ... }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { ... }
}
```

---

# E03: Role Based Access Control (RBAC)

**Section: Overview of Spring Boot Architecture**

Description:
Uses Spring Security for RBAC, method/endpoint security, and row-level constraints. Supports API key/OAuth2 toggle.

**Design Specification:**
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Security config in `SecurityConfig.java`
- Method security via `@PreAuthorize`
- Row-level filtering in repositories/services
- API key/OAuth2 toggle via profiles

**Sample Implementation:**
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
          .authorizeRequests()
            .antMatchers("/api/employees/**").hasAnyRole("ADMIN", "HR")
            .antMatchers("/api/attendance/**").hasAnyRole("SUPERVISOR", "WORKER")
            .anyRequest().authenticated()
          .and()
            .oauth2Login()
          .and()
            .httpBasic();
    }
}
```
```java
@PreAuthorize("hasRole('HR')")
public Employee updateEmployee(EmployeeUpdateRequest req) { ... }
```

---

# E04: Time & Attendance (Clock In/Out)

**Section: Overview of Spring Boot Architecture**

Description:
Endpoints for clock-in/out, geofence/device capture, shift hour calculation, missed punch handling.

**Design Specification:**
- Package: `com.warehouse.ems.attendance`
- Entity: `AttendanceEvent`
- Service: `AttendanceService`
- Controller: `AttendanceController`
- Geofence validation utility

**Sample Implementation:**
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime timestamp;
    @Enumerated(EnumType.STRING)
    private EventType type; // CLOCK_IN, CLOCK_OUT
    private String deviceId;
    private double latitude;
    private double longitude;
    // getters/setters
}
```
```java
@PostMapping("/clock-in")
public ResponseEntity<?> clockIn(@RequestBody ClockEventRequest req) {
    attendanceService.clockIn(req);
    return ResponseEntity.ok().build();
}
```

---

# E05: Shift & Schedule Management

**Section: Overview of Spring Boot Architecture**

Description:
Manages shift templates, rotations, overtime, blackout dates, and warehouse calendars.

**Design Specification:**
- Package: `com.warehouse.ems.scheduling`
- Entities: `ShiftTemplate`, `ShiftAssignment`, `BlackoutDate`
- Service: `SchedulingService`
- Controller: `SchedulingController`

**Sample Implementation:**
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    // getters/setters
}
```
```java
@PostMapping("/shifts/assign")
public ResponseEntity<?> assignShift(@RequestBody ShiftAssignmentRequest req) { ... }
```

---

# E06: Leave & Absence Management

**Section: Overview of Spring Boot Architecture**

Description:
Handles PTO, sick, unpaid leave requests/approvals, accruals, and integration with scheduling/payroll.

**Design Specification:**
- Package: `com.warehouse.ems.leave`
- Entities: `LeaveRequest`, `LeavePolicy`, `LeaveBalance`
- Service: `LeaveService`
- Controller: `LeaveController`
- Integration hooks for scheduling/payroll

**Sample Implementation:**
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
    private LeaveStatus status;
    // getters/setters
}
```
```java
@PostMapping("/leave/request")
public ResponseEntity<?> requestLeave(@RequestBody LeaveRequestDto req) { ... }
```

---

# E07: Training & Certification Tracking

**Section: Overview of Spring Boot Architecture**

Description:
Tracks certifications, expirations, renewals, and blocks assignments if expired. Supports document uploads.

**Design Specification:**
- Package: `com.warehouse.ems.training`
- Entities: `Certification`, `EmployeeCertification`
- Service: `CertificationService`
- Controller: `CertificationController`
- File storage for proof documents

**Sample Implementation:**
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalDate expirationDate;
    private String documentUrl;
    // getters/setters
}
```
```java
@PostMapping("/certifications/upload")
public ResponseEntity<?> uploadProof(@RequestParam MultipartFile file) { ... }
```

---

# E08: Safety Incidents & OSHA Reporting

**Section: Overview of Spring Boot Architecture**

Description:
Records incidents/near-misses, manages investigation workflow, and generates OSHA summaries.

**Design Specification:**
- Package: `com.warehouse.ems.safety`
- Entities: `SafetyIncident`, `CorrectiveAction`
- Service: `SafetyService`
- Controller: `SafetyController`
- OSHA report generator utility

**Sample Implementation:**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String description;
    private String location;
    private Severity severity;
    @ManyToMany
    private List<Employee> involvedEmployees;
    private IncidentStatus status;
    // getters/setters
}
```
```java
@GetMapping("/safety/osha-summary")
public ResponseEntity<OSHASummaryDto> getOshaSummary() { ... }
```

---

# E09: Equipment & Asset Assignment

**Section: Overview of Spring Boot Architecture**

Description:
Assigns assets (scanners, forklifts, PPE), tracks checkouts/returns, and enforces certification requirements.

**Design Specification:**
- Package: `com.warehouse.ems.asset`
- Entities: `Asset`, `AssetAssignment`
- Service: `AssetService`
- Controller: `AssetController`

**Sample Implementation:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private AssetCondition condition;
    // getters/setters
}
```
```java
@PostMapping("/assets/assign")
public ResponseEntity<?> assignAsset(@RequestBody AssetAssignmentRequest req) { ... }
```

---

# E10: Performance Reviews & Goals

**Section: Overview of Spring Boot Architecture**

Description:
Manages review templates, goals, competencies, ratings, and acknowledgements.

**Design Specification:**
- Package: `com.warehouse.ems.performance`
- Entities: `PerformanceReview`, `Goal`, `Competency`
- Service: `PerformanceService`
- Controller: `PerformanceController`

**Sample Implementation:**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate reviewDate;
    private String comments;
    private int rating;
    private boolean acknowledgedByEmployee;
    private boolean acknowledgedBySupervisor;
    // getters/setters
}
```
```java
@PostMapping("/performance/review")
public ResponseEntity<?> createReview(@RequestBody PerformanceReviewDto req) { ... }
```

---

# E11: Payroll Export Integration

**Section: Overview of Spring Boot Architecture**

Description:
Generates payroll-ready files, maps to provider formats, and delivers securely via SFTP/API.

**Design Specification:**
- Package: `com.warehouse.ems.payroll`
- Service: `PayrollExportService`
- Controller: `PayrollController`
- Integration: SFTP/API client utilities

**Sample Implementation:**
```java
@PostMapping("/payroll/export")
public ResponseEntity<?> exportPayroll(@RequestBody PayrollExportRequest req) {
    payrollExportService.export(req);
    return ResponseEntity.ok().build();
}
```
```java
@Service
public class PayrollExportService {
    public void export(PayrollExportRequest req) {
        // Generate file, map fields, send via SFTP/API
    }
}
```

---

# E12: Notifications & Announcements

**Section: Overview of Spring Boot Architecture**

Description:
Sends in-app, email, and SMS notifications for events; supports quiet hours.

**Design Specification:**
- Package: `com.warehouse.ems.notification`
- Entities: `Notification`, `Announcement`
- Service: `NotificationService`
- Controller: `NotificationController`
- Integration: Email/SMS providers

**Sample Implementation:**
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private String message;
    private NotificationType type;
    private LocalDateTime sentAt;
    private boolean read;
    // getters/setters
}
```
```java
@PostMapping("/notifications/send")
public ResponseEntity<?> sendNotification(@RequestBody NotificationRequest req) { ... }
```

---

# E13: Integration Layer (HRIS/WMS APIs)

**Section: Overview of Spring Boot Architecture**

Description:
Exposes REST APIs and connectors for HRIS, WMS, and IDP; supports webhooks.

**Design Specification:**
- Package: `com.warehouse.ems.integration`
- Service: `IntegrationService`
- Controller: `IntegrationController`
- REST clients for HRIS/WMS/IDP

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    @PostMapping("/hris/webhook")
    public ResponseEntity<?> handleHrisEvent(@RequestBody HrisEventDto event) { ... }
}
```
```java
@Service
public class HrisClient {
    public void syncEmployee(EmployeeDto employee) { ... }
}
```

---

# E14: Audit Trail & Compliance

**Section: Overview of Spring Boot Architecture**

Description:
Centralized audit logging for sensitive changes, tamper-evident storage.

**Design Specification:**
- Package: `com.warehouse.ems.audit`
- Entity: `AuditLog`
- Service: `AuditService`
- Aspect for logging sensitive operations

**Sample Implementation:**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private String action;
    private String performedBy;
    private LocalDateTime timestamp;
    private String details;
    // getters/setters
}
```
```java
@Aspect
@Component
public class AuditAspect {
    @AfterReturning(pointcut = "...", returning = "result")
    public void logChange(JoinPoint jp, Object result) { ... }
}
```

---

# E15: Reporting & Analytics

**Section: Overview of Spring Boot Architecture**

Description:
Provides operational reports, exports, and dashboards with role-based access.

**Design Specification:**
- Package: `com.warehouse.ems.reporting`
- Service: `ReportingService`
- Controller: `ReportingController`
- CSV/PDF export utilities

**Sample Implementation:**
```java
@GetMapping("/reports/attendance")
public ResponseEntity<Resource> exportAttendanceReport(@RequestParam ReportParams params) { ... }
```
```java
@Service
public class ReportingService {
    public byte[] generateAttendanceReport(ReportParams params) { ... }
}
```

---

# E16: Mobile Access (PWA)

**Section: Overview of Spring Boot Architecture**

Description:
Provides responsive endpoints for mobile/PWA, offline support.

**Design Specification:**
- Package: `com.warehouse.ems.mobile`
- Controller: `MobileController`
- Service: `MobileService`
- PWA manifest and service worker in `static/`

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/mobile")
public class MobileController {
    @GetMapping("/schedule")
    public List<ScheduleDto> getScheduleForMobile(@AuthenticationPrincipal User user) { ... }
}
```
```json
// static/manifest.json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff"
}
```

---

# E17: Onboarding & Offboarding Workflow

**Section: Overview of Spring Boot Architecture**

Description:
Automates provisioning/deprovisioning of accounts, schedules, training, and assets.

**Design Specification:**
- Package: `com.warehouse.ems.onboarding`
- Entities: `OnboardingTask`, `OffboardingTask`
- Service: `OnboardingService`
- Controller: `OnboardingController`
- Integration with HRIS, asset, and training modules

**Sample Implementation:**
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    private String description;
    private TaskStatus status;
    @ManyToOne
    private Employee employee;
    // getters/setters
}
```
```java
@PostMapping("/onboarding/start")
public ResponseEntity<?> startOnboarding(@RequestBody OnboardingRequest req) { ... }
```

---

# General Spring Boot Best Practices Applied

- **Dependency Injection:** All services/components are injected via `@Autowired` or constructor injection.
- **Exception Handling:** Centralized via `@ControllerAdvice` and custom exceptions.
- **Validation:** DTOs use `javax.validation` annotations, validated in controllers.
- **RESTful API Design:** Follows standard HTTP verbs, status codes, and resource-oriented URIs.
- **Security:** Uses Spring Security for authentication/authorization, method-level security, and secure configuration.
- **Configuration:** Sensitive values in `application.yml` or environment variables.
- **Testing:** Each module includes unit and integration tests (not shown here).

---

## Document Summary

**Total Epics Covered:** 17
**Total Modules Designed:** 17
**Architecture Pattern:** Layered Architecture (Controller â Service â Repository â Entity)
**Framework:** Spring Boot 2.x/3.x
**Database:** PostgreSQL with Flyway/Liquibase migrations
**Security:** Spring Security with RBAC, OAuth2/API Key support
**API Documentation:** OpenAPI/Swagger
**Testing:** JUnit 5, Mockito, Spring Boot Test

This document provides a production-ready, low-level technical design for all 17 Warehouse EMS epics, following Spring Boot industry standards and best practices. Each epic is modular, extensible, and ready for implementation by Spring Boot developers.

---

**Document Version:** 1.0
**Last Updated:** 2024
**Status:** Ready for Implementation