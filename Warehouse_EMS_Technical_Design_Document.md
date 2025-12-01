# Warehouse Employee Management System (EMS) - Technical Design Document

## Executive Summary
This document provides a comprehensive low-level technical design for the Warehouse Employee Management System (EMS), covering the first 17 user stories across major functional areas. The design adheres to Spring Boot industry standards and best practices, ensuring maintainability, scalability, and security.

## Architecture Overview
- **Spring Boot (Maven)**: Modular monolith with layered architecture (Controller, Service, Repository, Domain).
- **Database**: PostgreSQL with Flyway/Liquibase for migrations.
- **Security**: Spring Security with RBAC, OAuth2/API key toggle.
- **Integration**: REST APIs, SFTP, webhooks.
- **Monitoring**: Actuator endpoints.

## Technology Stack
- Java 17+
- Spring Boot 3.x
- Spring Data JPA
- Spring Security
- Flyway/Liquibase
- PostgreSQL
- OpenAPI/Swagger
- SFTP
- PWA (for mobile access)

## Design Patterns Used
- Layered Architecture
- DTO Pattern
- Repository Pattern
- Service Pattern
- Factory Pattern (for notifications)
- Observer Pattern (for audit trail)
- Strategy Pattern (for payroll export)

## Cross-Cutting Concerns
- **Security**: RBAC, endpoint/method security, OAuth2/API key toggle.
- **Logging**: Centralized audit trail, tamper-evident storage.
- **Error Handling**: Global exception handler, validation errors.
- **Testing**: Unit, integration, and security tests.

## Deployment Considerations
- Dockerized deployment.
- Profile-specific configs (dev, prod).
- Health checks via Actuator.

## Testing Strategy
- Unit tests for service and repository layers.
- Integration tests for REST endpoints.
- Security tests for RBAC and method security.
- End-to-end tests for critical flows (clock-in/out, leave, onboarding).

---

# User Story Technical Design Sections

## E01 - Project Scaffolding & Domain Setup
Section: Spring Boot Project Initialization  
Description: Establishes the foundational structure for all modules, configures Maven, sets up base packages, enables Actuator, and integrates Flyway/Liquibase for DB migrations.  
Design Specification:
- Base package: `com.warehouse.ems`
- Modules: `employee`, `scheduling`, `attendance`, `safety`
- Maven multi-module setup
- Flyway/Liquibase migration scripts in `src/main/resources/db/migration`
- Actuator enabled in `application.yml`
Sample Implementation:
```java
// application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems
    username: ems_user
    password: secret
```

---

## E02 - Employee Master Data (CRUD)
Section: Employee Domain Model & CRUD APIs  
Description: Implements the Employee entity, CRUD REST endpoints, DTOs, validation, and OpenAPI documentation.  
Design Specification:
- Package: `com.warehouse.ems.employee`
- Entity: `Employee` (fields: id, name, badgeId, role, department, shiftGroup, hireDate, status)
- Repository: `EmployeeRepository extends JpaRepository<Employee, Long>`
- Service: `EmployeeService` (CRUD, soft-delete)
- Controller: `EmployeeController` (`/employees` endpoints)
- DTOs: `EmployeeRequest`, `EmployeeResponse`
- Validation: `@NotNull`, `@Size`, unique badgeId
- OpenAPI annotations
Sample Implementation:
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
  @Id @GeneratedValue private Long id;
  @NotNull @Size(min=2) private String name;
  @NotNull @Column(name="badge_id") private String badgeId;
  @Enumerated(EnumType.STRING) private Role role;
  private String department;
  private String shiftGroup;
  private LocalDate hireDate;
  private EmployeeStatus status;
}
```

---

## E03 - Role Based Access Control (RBAC)
Section: Security Configuration & RBAC  
Description: Configures Spring Security with roles, endpoint/method security, and API key/OAuth2 toggle.  
Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Security config: `SecurityConfig.java`
- Method security: `@PreAuthorize`
- API key/OAuth2 toggle via `application.yml`
- Row-level constraints in service layer
Sample Implementation:
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
      .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
      .antMatchers("/attendance/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
      .anyRequest().authenticated();
    // API key/OAuth2 toggle logic
  }
}
```

---

## E04 - Time & Attendance (Clock In/Out)
Section: Attendance Domain & Clock In/Out APIs  
Description: Implements attendance entity, clock-in/out endpoints, geofence/device capture, missed punch workflow, and reporting.  
Design Specification:
- Package: `com.warehouse.ems.attendance`
- Entity: `AttendanceEvent` (employeeId, timestamp, type, deviceId, location)
- Repository: `AttendanceRepository`
- Service: `AttendanceService` (clock-in/out, shift association, corrections)
- Controller: `AttendanceController` (`/attendance/clock-in`, `/clock-out`)
- Geofence validation
- Correction approval workflow
Sample Implementation:
```java
@Entity
public class AttendanceEvent {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private LocalDateTime timestamp;
  @Enumerated(EnumType.STRING) private AttendanceType type; // CLOCK_IN, CLOCK_OUT
  private String deviceId;
  private String location;
}
```

---

## E05 - Shift & Schedule Management
Section: Shift Templates & Scheduling APIs  
Description: Manages shift templates, rotations, assignments, conflict detection, and operation calendars.  
Design Specification:
- Package: `com.warehouse.ems.scheduling`
- Entity: `ShiftTemplate`, `ShiftAssignment`
- Repository: `ShiftTemplateRepository`, `ShiftAssignmentRepository`
- Service: `SchedulingService` (CRUD, conflict detection)
- Controller: `SchedulingController` (`/shifts`, `/schedules`)
- Bulk assignment endpoints
Sample Implementation:
```java
@Entity
public class ShiftTemplate {
  @Id @GeneratedValue private Long id;
  private String name;
  private LocalTime startTime;
  private LocalTime endTime;
  private boolean recurring;
  private String operationCalendar;
}
```

---

## E06 - Leave & Absence Management
Section: Leave Domain & PTO APIs  
Description: Implements leave request/approval, accrual balances, and integration with scheduling/payroll.  
Design Specification:
- Package: `com.warehouse.ems.leave`
- Entity: `LeaveRequest` (employee, type, startDate, endDate, status, balance)
- Repository: `LeaveRepository`
- Service: `LeaveService` (request, approve/deny, balance update)
- Controller: `LeaveController` (`/leave/requests`)
- Integration hooks for scheduling/payroll
Sample Implementation:
```java
@Entity
public class LeaveRequest {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  @Enumerated(EnumType.STRING) private LeaveType type;
  private LocalDate startDate;
  private LocalDate endDate;
  @Enumerated(EnumType.STRING) private LeaveStatus status;
  private int balance;
}
```

---

## E07 - Training & Certification Tracking
Section: Certification Domain & Tracking APIs  
Description: Tracks certifications, expirations, renewals, blocks unqualified assignments, and uploads proof documents.  
Design Specification:
- Package: `com.warehouse.ems.certification`
- Entity: `Certification` (employee, type, expiryDate, documentUrl)
- Repository: `CertificationRepository`
- Service: `CertificationService` (CRUD, expiry alerts)
- Controller: `CertificationController` (`/certifications`)
- Scheduling checks for assignment
Sample Implementation:
```java
@Entity
public class Certification {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String type;
  private LocalDate expiryDate;
  private String documentUrl;
}
```

---

## E08 - Safety Incidents & OSHA Reporting
Section: Safety Incident Domain & Reporting APIs  
Description: Records incidents, manages investigation workflow, and generates OSHA reports.  
Design Specification:
- Package: `com.warehouse.ems.safety`
- Entity: `SafetyIncident` (severity, location, description, status, involvedEmployees)
- Repository: `SafetyIncidentRepository`
- Service: `SafetyService` (record, workflow, reporting)
- Controller: `SafetyController` (`/safety/incidents`)
- OSHA export endpoints
Sample Implementation:
```java
@Entity
public class SafetyIncident {
  @Id @GeneratedValue private Long id;
  private String severity;
  private String location;
  private String description;
  @Enumerated(EnumType.STRING) private IncidentStatus status;
  @ManyToMany private List<Employee> involvedEmployees;
}
```

---

## E09 - Equipment & Asset Assignment
Section: Asset Domain & Assignment APIs  
Description: Tracks asset assignment, check-in/out, certification validation, and asset condition.  
Design Specification:
- Package: `com.warehouse.ems.asset`
- Entity: `Asset`, `AssetAssignment`
- Repository: `AssetRepository`, `AssetAssignmentRepository`
- Service: `AssetService` (CRUD, check-in/out, overdue reports)
- Controller: `AssetController` (`/assets`, `/assignments`)
- Certification checks
Sample Implementation:
```java
@Entity
public class Asset {
  @Id @GeneratedValue private Long id;
  private String type;
  private String serialNumber;
  private AssetCondition condition;
}

@Entity
public class AssetAssignment {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Asset asset;
  @ManyToOne private Employee employee;
  private LocalDateTime checkoutTime;
  private LocalDateTime returnTime;
}
```

---

## E10 - Performance Reviews & Goals
Section: Review Domain & Workflow APIs  
Description: Manages review cycles, goals, ratings, comments, and acknowledgements.  
Design Specification:
- Package: `com.warehouse.ems.review`
- Entity: `PerformanceReview` (employee, cycle, goals, ratings, comments, status)
- Repository: `PerformanceReviewRepository`
- Service: `ReviewService` (create, submit, acknowledge)
- Controller: `ReviewController` (`/reviews`)
- PDF export endpoints
Sample Implementation:
```java
@Entity
public class PerformanceReview {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String cycle;
  @ElementCollection private List<String> goals;
  @ElementCollection private List<Integer> ratings;
  private String comments;
  @Enumerated(EnumType.STRING) private ReviewStatus status;
}
```

---

## E11 - Payroll Export Integration
Section: Payroll Export APIs & Integration  
Description: Generates payroll files, maps to provider formats, delivers via SFTP/API, and logs exports.  
Design Specification:
- Package: `com.warehouse.ems.payroll`
- Entity: `PayrollExport` (employee, period, totalHours, exportStatus)
- Repository: `PayrollExportRepository`
- Service: `PayrollService` (generate, deliver, retry)
- Controller: `PayrollController` (`/payroll/exports`)
- SFTP/API integration
Sample Implementation:
```java
@Entity
public class PayrollExport {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String period;
  private double totalHours;
  @Enumerated(EnumType.STRING) private ExportStatus exportStatus;
}
```

---

## E12 - Notifications & Announcements
Section: Notification Domain & Delivery APIs  
Description: Sends in-app, email, SMS notifications for events, supports quiet hours, and tracks delivery status.  
Design Specification:
- Package: `com.warehouse.ems.notification`
- Entity: `Notification` (recipient, type, message, status, channel)
- Repository: `NotificationRepository`
- Service: `NotificationService` (send, opt-in/out, rate limit)
- Controller: `NotificationController` (`/notifications`, `/announcements`)
- Factory pattern for channel delivery
Sample Implementation:
```java
@Entity
public class Notification {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee recipient;
  private NotificationType type;
  private String message;
  private NotificationStatus status;
  private NotificationChannel channel;
}
```

---

## E13 - Integration Layer (HRIS/WMS APIs)
Section: External API Integration & Webhooks  
Description: Exposes REST APIs for HRIS/WMS, supports SSO, and webhooks for events.  
Design Specification:
- Package: `com.warehouse.ems.integration`
- REST endpoints: `/api/hris`, `/api/wms`, `/api/webhooks`
- JWT/OAuth2 security
- Sync jobs for HRIS/WMS
- Webhook event publishing
- OpenAPI documentation
Sample Implementation:
```java
@RestController
@RequestMapping("/api/hris")
public class HRISController {
  @PostMapping("/sync")
  public ResponseEntity<?> syncEmployees(@RequestBody List<EmployeeDTO> employees) {
    // Sync logic
    return ResponseEntity.ok().build();
  }
}
```

---

## E14 - Audit Trail & Compliance
Section: Audit Logging & Tamper-Evident Storage  
Description: Centralized logging for sensitive changes, immutable log table, export capabilities.  
Design Specification:
- Package: `com.warehouse.ems.audit`
- Entity: `AuditLog` (actor, timestamp, entity, before, after, action)
- Repository: `AuditLogRepository`
- Service: `AuditService` (log, export)
- Observer pattern for entity changes
Sample Implementation:
```java
@Entity
public class AuditLog {
  @Id @GeneratedValue private Long id;
  private String actor;
  private LocalDateTime timestamp;
  private String entity;
  @Lob private String before;
  @Lob private String after;
  private String action;
}
```

---

## E15 - Reporting & Analytics
Section: Reporting APIs & Dashboards  
Description: Provides operational reports, exports, dashboards, and metrics endpoints.  
Design Specification:
- Package: `com.warehouse.ems.reporting`
- Entity: `Report` (type, filters, generatedAt, fileUrl)
- Repository: `ReportRepository`
- Service: `ReportingService` (generate, export)
- Controller: `ReportingController` (`/reports`)
- Metrics endpoints for BI
Sample Implementation:
```java
@Entity
public class Report {
  @Id @GeneratedValue private Long id;
  private ReportType type;
  private String filters;
  private LocalDateTime generatedAt;
  private String fileUrl;
}
```

---

## E16 - Mobile Access (PWA)
Section: PWA Configuration & Mobile APIs  
Description: Enables mobile-friendly views, offline support, and installable PWA manifest.  
Design Specification:
- Package: `com.warehouse.ems.mobile`
- PWA manifest in `src/main/resources/static/manifest.json`
- Offline queue for clock events
- Mobile endpoints for schedules, leave, announcements
Sample Implementation:
```json
// manifest.json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

---

## E17 - Onboarding & Offboarding Workflow
Section: Employee Lifecycle Automation  
Description: Automates provisioning, initial schedule, training tasks, asset assignment, and deprovisioning.  
Design Specification:
- Package: `com.warehouse.ems.lifecycle`
- Entity: `LifecycleTask` (employee, type, status, dueDate)
- Repository: `LifecycleTaskRepository`
- Service: `LifecycleService` (provision, deprovision, task generation)
- Controller: `LifecycleController` (`/lifecycle/onboard`, `/lifecycle/offboard`)
Sample Implementation:
```java
@Entity
public class LifecycleTask {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private LifecycleTaskType type;
  private LifecycleTaskStatus status;
  private LocalDate dueDate;
}
```

---

**Note:** This document covers the first 17 user stories as provided. For the full set of 73 user stories, please provide the complete breakdown for further documentation.