# Warehouse Employee Management System - Low-Level Technical Design Document

## Document Overview

This document provides comprehensive low-level technical design specifications for the Warehouse Employee Management System built with Spring Boot 3.x, Java 17+, and PostgreSQL. The system encompasses 20 epics covering employee management, scheduling, attendance, safety, and compliance.

---

## Section: E01 - Project Scaffolding & Domain Setup

### Description
Establishes the foundational architecture for the Warehouse Employee Management System using Spring Boot 3.x, Java 17+, and PostgreSQL. Sets up core modules, Flyway for DB migrations, and Spring Boot Actuator for monitoring.

### Design Specification
- Spring Boot 3.x (Maven) project, Java 17+
- Package-by-feature structure: com.company.wms.[feature]
- Core modules: employee, scheduling, attendance, safety
- Flyway for DB migrations
- Spring Boot Actuator enabled
- Multi-tenancy support via schema or discriminator
- Base exception handling, OpenAPI 3.0 config
- Soft delete pattern via @Where and @SQLDelete
- Audit logging via AOP

### Sample Implementation

```java
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}
```

---

## Section: E02 - Employee Master Data (CRUD)

### Description
Manages employee records with CRUD APIs, soft delete, pagination, and filtering.

### Design Specification
- Package: com.company.wms.employee
- Entity: Employee (id, badgeId, name, role, department, shiftGroup, hireDate, status, deleted, tenantId)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>, custom queries for filtering
- Service: EmployeeService with @Transactional, validation, business rules
- Controller: EmployeeController with REST endpoints, DTOs via MapStruct, OpenAPI annotations
- Soft delete via @SQLDelete, @Where
- Pagination and filtering via Pageable

### Sample Implementation

```java
@Entity
@SQLDelete(sql = "UPDATE employee SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Employee {
    @Id @GeneratedValue private Long id;
    @Column(unique = true, nullable = false) private String badgeId;
    private String name;
    @Enumerated(EnumType.STRING) private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted = false;
    private String tenantId;
    // getters/setters
}
```

---

## Section: E03 - Role-Based Access Control (RBAC)

### Description
Secures endpoints and data with Spring Security, JWT/OAuth2, and row-level constraints.

### Design Specification
- Package: com.company.wms.security
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- SecurityConfig: JWT/OAuth2, method security, row-level filtering
- API key/OAuth2 toggle via config
- @PreAuthorize annotations on service/controller methods
- Custom PermissionEvaluator for row-level access

### Sample Implementation

```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers("/api/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .anyRequest().authenticated()
            .and().oauth2ResourceServer().jwt();
    }
}
```

---

## Section: E04 - Time & Attendance (Clock In/Out)

### Description
Handles clock-in/out events, geofencing, device capture, and corrections workflow.

### Design Specification
- Package: com.company.wms.attendance
- Entity: AttendanceEvent (id, employee, type, timestamp, deviceId, geoLocation, approved, correctionRequested)
- Repository: AttendanceEventRepository with custom queries for daily totals
- Service: AttendanceService with shift association, correction workflow
- Controller: AttendanceController with endpoints for clock-in/out, corrections
- Reports exportable as CSV

### Sample Implementation

```java
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceEventDto> clockIn(@RequestBody ClockEventRequest req) {
        // validate, associate shift, save event
    }
}
```

---

## Section: E05 - Shift & Schedule Management

### Description
Manages shift templates, rotations, overtime, and assignment.

### Design Specification
- Package: com.company.wms.schedule
- Entities: ShiftTemplate, ShiftAssignment, OvertimeRule
- Repository: ShiftTemplateRepository, ShiftAssignmentRepository
- Service: ScheduleService with conflict detection, bulk assignment
- Controller: ScheduleController with CRUD endpoints
- Audit entries generated on changes

### Sample Implementation

```java
@Entity
public class ShiftAssignment {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private ShiftTemplate shiftTemplate;
    private LocalDate date;
    // ...
}
```

---

## Section: E06 - Leave & Absence Management

### Description
Handles PTO, sick, unpaid leave requests, approvals, and accruals.

### Design Specification
- Package: com.company.wms.leave
- Entities: LeaveRequest (id, employee, type, startDate, endDate, status, approver, accrualBalance)
- Repository: LeaveRequestRepository
- Service: LeaveService with accruals, policy enforcement
- Controller: LeaveController with endpoints for request/approve/deny
- Integration with scheduling and payroll

### Sample Implementation

```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private LeaveType type;
    private LocalDate startDate, endDate;
    @Enumerated(EnumType.STRING) private LeaveStatus status;
    @ManyToOne private Employee approver;
    private BigDecimal accrualBalance;
}
```

---

## Section: E07 - Training & Certification Tracking

### Description
Tracks employee certifications, expirations, renewals, and proof documents.

### Design Specification
- Package: com.company.wms.certification
- Entities: Certification, EmployeeCertification
- Repository: CertificationRepository, EmployeeCertificationRepository
- Service: CertificationService with expiry alerts, assignment checks
- Controller: CertificationController with CRUD endpoints
- File upload for proof documents

### Sample Implementation

```java
@Entity
public class EmployeeCertification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private Certification certification;
    private LocalDate issueDate, expiryDate;
    private String proofDocumentUrl;
}
```

---

## Section: E08 - Safety Incidents & OSHA Reporting

### Description
Records safety incidents, workflows for investigation, and OSHA reporting.

### Design Specification
- Package: com.company.wms.safety
- Entities: SafetyIncident (id, type, severity, location, description, status, involvedEmployees)
- Repository: SafetyIncidentRepository
- Service: SafetyService with workflow transitions
- Controller: SafetyController with endpoints for reporting, status updates
- OSHA 300/300A export endpoints

### Sample Implementation

```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private String type, severity, location, description;
    @Enumerated(EnumType.STRING) private IncidentStatus status;
    @ManyToMany private List<Employee> involvedEmployees;
}
```

---

## Section: E09 - Equipment & Asset Assignment

### Description
Assigns assets to employees, tracks check-in/out, and enforces certification requirements.

### Design Specification
- Package: com.company.wms.asset
- Entities: Asset, AssetAssignment
- Repository: AssetRepository, AssetAssignmentRepository
- Service: AssetService with certification checks
- Controller: AssetController with endpoints for assignment, return
- Asset condition tracking

### Sample Implementation

```java
@Entity
public class AssetAssignment {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Asset asset;
    @ManyToOne private Employee employee;
    private LocalDateTime assignedAt, returnedAt;
    private String condition;
}
```

---

## Section: E10 - Performance Reviews & Goals

### Description
Manages review cycles, goals, competencies, and feedback workflows.

### Design Specification
- Package: com.company.wms.performance
- Entities: PerformanceReview, ReviewCycle, Goal
- Repository: PerformanceReviewRepository
- Service: PerformanceService with workflow, PDF export
- Controller: PerformanceController with endpoints for review management

### Sample Implementation

```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private ReviewCycle cycle;
    private String comments;
    private int rating;
    private boolean acknowledgedByEmployee, acknowledgedBySupervisor;
}
```

---

## Section: E11 - Payroll Export Integration

### Description
Generates payroll-ready files from attendance/leave, maps to provider formats, and delivers securely.

### Design Specification
- Package: com.company.wms.payroll
- Entities: PayrollExport, PayrollProviderMapping
- Repository: PayrollExportRepository
- Service: PayrollExportService with file generation, SFTP/API delivery, retry logic
- Controller: PayrollExportController with export endpoints
- Audit log for every export

### Sample Implementation

```java
@Service
public class PayrollExportService {
    @Transactional
    public void exportPayroll(LocalDate periodStart, LocalDate periodEnd) {
        // gather attendance/leave, map to provider schema, deliver via SFTP/API
    }
}
```

---

## Section: E12 - Notifications & Announcements

### Description
Sends in-app, email, and SMS notifications for key events; supports localization and quiet hours.

### Design Specification
- Package: com.company.wms.notification
- Entities: Notification, Announcement
- Repository: NotificationRepository
- Service: NotificationService with channel opt-in/out, delivery tracking
- Controller: NotificationController with endpoints for announcements
- Rate limiting and quiet hours config

### Sample Implementation

```java
@Entity
public class Notification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee recipient;
    private String channel; // EMAIL, SMS, IN_APP
    private String message;
    private boolean delivered;
    private LocalDateTime sentAt;
}
```

---

## Section: E13 - Integration Layer (HRIS/WMS APIs)

### Description
Exposes REST APIs and connectors for HRIS, WMS, and SSO; supports webhooks.

### Design Specification
- Package: com.company.wms.integration
- Entities: IntegrationEvent, WebhookSubscription
- Repository: IntegrationEventRepository
- Service: IntegrationService with HRIS/WMS sync, webhook delivery
- Controller: IntegrationController with secured endpoints
- JWT/OAuth2 security, idempotency

### Sample Implementation

```java
@RestController
@RequestMapping("/api/integration")
@SecurityRequirement(name = "bearerAuth")
public class IntegrationController {
    @PostMapping("/hris/sync")
    public ResponseEntity<Void> syncFromHris(@RequestBody HrisEmployeeDto dto) {
        // upsert employee
    }
}
```

---

## Section: E14 - Audit Trail & Compliance

### Description
Centralized audit logging for sensitive changes, tamper-evident storage, and export.

### Design Specification
- Package: com.company.wms.audit
- Entity: AuditLog (id, actor, timestamp, entity, entityId, action, before, after)
- Repository: AuditLogRepository
- Service: AuditService with AOP for create/update/delete
- Controller: AuditController with export endpoints

### Sample Implementation

```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String actor, entity, entityId, action;
    private LocalDateTime timestamp;
    @Lob private String before, after;
}

@Aspect
@Component
public class AuditAspect {
    @AfterReturning(...)
    public void logChange(...) { /* ... */ }
}
```

---

## Section: E15 - Reporting & Analytics

### Description
Provides operational reports, dashboards, and export capabilities.

### Design Specification
- Package: com.company.wms.reporting
- Service: ReportingService with attendance, overtime, leave, certification, safety KPIs
- Controller: ReportingController with endpoints for CSV/PDF export, dashboards
- Role-based access

### Sample Implementation

```java
@RestController
@RequestMapping("/api/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> exportAttendance(@RequestParam ...) {
        // generate and return CSV
    }
}
```

---

## Section: E16 - Mobile Access (PWA)

### Description
Responsive, offline-friendly PWA for core worker flows.

### Design Specification
- Package: com.company.wms.mobile
- Controller: MobileController with endpoints optimized for mobile
- PWA manifest, service worker for offline support
- Offline queue for clock events

### Sample Implementation

```java
@RestController
@RequestMapping("/api/mobile")
public class MobileController {
    @GetMapping("/shifts")
    public List<ShiftAssignmentDto> getUpcomingShifts(...) { ... }
}
```

---

## Section: E17 - Onboarding & Offboarding Workflow

### Description
Automates provisioning, initial schedule, training, and deprovisioning.

### Design Specification
- Package: com.company.wms.onboarding
- Entities: OnboardingTask, OffboardingTask
- Service: OnboardingService with HRIS triggers, task generation
- Controller: OnboardingController with workflow endpoints

### Sample Implementation

```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String type; // TRAINING, ASSET_ASSIGNMENT, etc.
    private boolean completed;
}
```

---

## Section: E18 - Localization & Internationalization

### Description
Supports multiple languages and regional formats.

### Design Specification
- Package: com.company.wms.i18n
- MessageSource bean for resource bundles
- DTOs and notifications localized
- LocaleResolver for user preferences

### Sample Implementation

```java
@Bean
public MessageSource messageSource() {
    ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
    ms.setBasename("classpath:messages");
    ms.setDefaultEncoding("UTF-8");
    return ms;
}
```

---

## Section: E19 - Observability & Monitoring

### Description
Enables monitoring, metrics, and health checks.

### Design Specification
- Package: com.company.wms.monitoring
- Spring Boot Actuator endpoints enabled
- Custom health indicators
- Metrics for key business events

### Sample Implementation

```java
@Component
public class AttendanceHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // check attendance event lag, return status
    }
}
```

---

## Section: E20 - CI/CD Pipeline

### Description
Automates build, test, and deployment with best practices.

### Design Specification
- Package: com.company.wms.cicd
- Dockerfile, GitHub Actions/Jenkins pipeline config
- Flyway migrations on deploy
- Automated tests, code quality gates

### Sample Implementation

```yaml
# .github/workflows/ci.yml
name: CI
on: [push]
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
        run: mvn clean verify
      - name: Run Flyway migrations
        run: mvn flyway:migrate
```

---

## Conclusion

This document provides a production-ready, detailed low-level technical design for all 20 epics of the Warehouse Employee Management System, following Spring Boot industry standards and best practices. Each section includes architecture, package structure, domain model, repository/service/controller layers, configuration, integration points, and code samples for easy developer consumption.

### Key Technical Highlights

- **Architecture**: Spring Boot 3.x with Java 17+, PostgreSQL database
- **Security**: JWT/OAuth2 authentication, role-based access control, row-level security
- **Data Management**: JPA/Hibernate with Flyway migrations, soft delete pattern
- **API Design**: RESTful endpoints with OpenAPI 3.0 documentation
- **Observability**: Spring Boot Actuator, structured logging, custom metrics
- **Integration**: HRIS/WMS connectors, webhooks, secure file delivery
- **Compliance**: Comprehensive audit logging, OSHA reporting, tamper-evident storage
- **Mobile**: PWA with offline support, responsive design
- **DevOps**: CI/CD pipeline with automated testing and deployment

### Next Steps

1. Review and approve technical design
2. Set up development environment
3. Initialize Spring Boot project structure
4. Begin implementation starting with E01 (Project Scaffolding)
5. Implement epics in dependency order
6. Conduct code reviews and testing
7. Deploy to staging and production environments

---

**Document Version**: 1.0  
**Last Updated**: 2024  
**Status**: Ready for Implementation