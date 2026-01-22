# Warehouse Employee Management System â Low-Level Technical Design Document

**Version:** 1.0  
**Date:** 2024-06-XX  
**Authors:** Senior Software Architect Team  
**Project:** Warehouse Employee Management System  
**Tech Stack:** Spring Boot 3.x, Maven, JPA/Hibernate, Flyway/Liquibase, Spring Security, REST, OpenAPI, Docker, Prometheus, Micrometer, PWA

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
- [E19: Observability & Monitoring](#e19-observability--monitoring)
- [E20: CI/CD & Deployment](#e20-cicd--deployment)

---

## E01: Project Scaffolding & Domain Setup

### 1. Overview
- Spring Boot Maven project initialized with base modules: employee, scheduling, attendance, safety.
- Database migration managed via Flyway/Liquibase.
- Actuator enabled for health checks and monitoring.

### 2. Package Structure
```
com.wms
  âââ config
  âââ employee
  âââ attendance
  âââ scheduling
  âââ safety
  âââ asset
  âââ review
  âââ payroll
  âââ notification
  âââ integration
  âââ audit
  âââ reporting
  âââ mobile
  âââ onboarding
  âââ localization
  âââ observability
  âââ common
```

### 3. Domain Model
- Base entities with `@MappedSuperclass` for audit fields.
- Example:
```java
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    private Boolean deleted = false;
}
```

### 4. Repository Layer
- Base repository interface:
```java
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
    List<T> findByDeletedFalse();
}
```

### 5. Service Layer
- Transactional services, validation, error handling.

### 6. Controller Layer
- REST controllers with `@RestController`, `@RequestMapping`, OpenAPI annotations.

### 7. Configuration
- `application.yml` for DB, actuator, security.
- Flyway/Liquibase migration scripts in `src/main/resources/db/migration`.

### 8. Integration Points
- Actuator endpoints (`/actuator/health`, `/actuator/info`).

### 9. Sample Implementation
```java
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}
```

---

## E02: Employee Master Data (CRUD)

### 1. Overview
- Employee domain with CRUD APIs.
- Unique badgeId, soft-delete, pagination, filtering.

### 2. Package Structure
```
com.wms.employee
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee extends BaseEntity {
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
}
```

### 4. Repository Layer
```java
public interface EmployeeRepository extends BaseRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    // Filtering methods
}
```

### 5. Service Layer
```java
@Service
public class EmployeeService {
    @Transactional
    public Employee createEmployee(EmployeeDto dto) { /* ... */ }
    @Transactional
    public void softDeleteEmployee(Long id) { /* ... */ }
    // Validation, business logic
}
```

### 6. Controller Layer
```java
@RestController
@RequestMapping("/employees")
@Tag(name = "Employee", description = "Employee management APIs")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) { /* ... */ }
    @GetMapping
    public Page<EmployeeDto> list(Pageable pageable, @RequestParam Map<String, String> filters) { /* ... */ }
    // Other CRUD endpoints
}
```

### 7. Configuration
- OpenAPI config for schema generation.
- Pagination defaults in `application.yml`.

### 8. Integration Points
- HRIS sync (see E13).

### 9. Sample Implementation
```java
public class EmployeeDto {
    private String name;
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}
```

---

## E03: Role-Based Access Control (RBAC)

### 1. Overview
- Spring Security with roles: ADMIN, HR, SUPERVISOR, WORKER.
- Method/endpoint security, row-level constraints.

### 2. Package Structure
```
com.wms.config.security
com.wms.employee.security
```

### 3. Domain Model
- `User` entity with roles.
```java
@Entity
public class User extends BaseEntity {
    private String username;
    private String password;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;
}
public enum Role { ADMIN, HR, SUPERVISOR, WORKER }
```

### 4. Repository Layer
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

### 5. Service Layer
- UserDetailsService implementation.
- Row-level security via custom queries.

### 6. Controller Layer
- Auth endpoints (`/auth/login`, `/auth/logout`).

### 7. Configuration
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) {
        http
            .authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR")
            .antMatchers("/attendance/**").hasAnyRole("SUPERVISOR", "WORKER")
            .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer().jwt();
    }
}
```
- API key/OAuth2 toggle via `application.yml`.

### 8. Integration Points
- OAuth2/JWT, external IDP.

### 9. Sample Implementation
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
public EmployeeDto createEmployee(EmployeeDto dto) { /* ... */ }
```

---

## E04: Time & Attendance

### 1. Overview
- Clock-in/out endpoints, geofence/device capture, hours calculation, missed punch corrections.

### 2. Package Structure
```
com.wms.attendance
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
```java
@Entity
public class AttendanceEvent extends BaseEntity {
    @ManyToOne
    private Employee employee;
    private LocalDateTime timestamp;
    @Enumerated(EnumType.STRING)
    private EventType eventType; // CLOCK_IN, CLOCK_OUT
    private String deviceId;
    private Double latitude;
    private Double longitude;
    private Boolean correction;
}
```

### 4. Repository Layer
```java
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeAndDate(Employee employee, LocalDate date);
}
```

### 5. Service Layer
- Calculate hours, validate geofence, handle corrections.

### 6. Controller Layer
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody AttendanceDto dto) { /* ... */ }
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody AttendanceDto dto) { /* ... */ }
}
```

### 7. Configuration
- Geofence radius in `application.yml`.

### 8. Integration Points
- CSV export, payroll (E11).

### 9. Sample Implementation
```java
public class AttendanceDto {
    private Long employeeId;
    private LocalDateTime timestamp;
    private String deviceId;
    private Double latitude;
    private Double longitude;
}
```

---

## E05: Shift & Schedule Management

### 1. Overview
- Shift templates, rotations, overtime rules, assignment, blackout dates, conflict detection.

### 2. Package Structure
```
com.wms.scheduling
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
```java
@Entity
public class ShiftTemplate extends BaseEntity {
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean overtimeAllowed;
}
@Entity
public class EmployeeShift extends BaseEntity {
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ShiftTemplate shiftTemplate;
    private LocalDate shiftDate;
    private Boolean blackout;
}
```

### 4. Repository Layer
- Custom queries for conflict detection.

### 5. Service Layer
- Bulk assignment, conflict prevention, audit logging.

### 6. Controller Layer
- CRUD for shift templates, assignment endpoints.

### 7. Configuration
- Blackout dates in `application.yml`.

### 8. Integration Points
- Reporting, notifications.

### 9. Sample Implementation
```java
public class ShiftAssignmentDto {
    private Long employeeId;
    private Long shiftTemplateId;
    private LocalDate shiftDate;
}
```

---

## E06: Leave & Absence Management

### 1. Overview
- PTO/sick/unpaid leave requests, approval workflow, accrual balances, scheduling integration.

### 2. Package Structure
```
com.wms.leave
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
```java
@Entity
public class LeaveRequest extends BaseEntity {
    @ManyToOne
    private Employee employee;
    @Enumerated(EnumType.STRING)
    private LeaveType type; // PTO, SICK, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status; // REQUESTED, APPROVED, DENIED
    private Double accrualBalance;
}
```

### 4. Repository Layer
- Find by employee, status, date range.

### 5. Service Layer
- Accrual calculation, approval workflow.

### 6. Controller Layer
- Request/approve endpoints.

### 7. Configuration
- Leave policies in `application.yml`.

### 8. Integration Points
- Scheduling, payroll.

### 9. Sample Implementation
```java
public class LeaveRequestDto {
    private Long employeeId;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
}
```

---

## E07: Training & Certification Tracking

### 1. Overview
- Track certifications, expirations, renewals, assignment blocking, document uploads.

### 2. Package Structure
```
com.wms.certification
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
```java
@Entity
public class Certification extends BaseEntity {
    @ManyToOne
    private Employee employee;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
}
```

### 4. Repository Layer
- Find expiring certifications.

### 5. Service Layer
- Expiry alerts, assignment blocking.

### 6. Controller Layer
- CRUD, upload endpoints.

### 7. Configuration
- Alert thresholds in `application.yml`.

### 8. Integration Points
- Scheduling, notifications.

### 9. Sample Implementation
```java
public class CertificationDto {
    private Long employeeId;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
}
```

---

## E08: Safety Incidents & OSHA Reporting

### 1. Overview
- Record incidents, severity, investigation workflow, OSHA export, metrics dashboard.

### 2. Package Structure
```
com.wms.safety
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
```java
@Entity
public class SafetyIncident extends BaseEntity {
    private String description;
    private String location;
    private Severity severity;
    @ManyToMany
    private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status; // OPEN, INVESTIGATING, RESOLVED
}
```

### 4. Repository Layer
- OSHA export queries.

### 5. Service Layer
- Investigation workflow, metrics calculation.

### 6. Controller Layer
- Incident CRUD, dashboard endpoints.

### 7. Configuration
- Severity levels in `application.yml`.

### 8. Integration Points
- OSHA export, reporting.

### 9. Sample Implementation
```java
public class SafetyIncidentDto {
    private String description;
    private String location;
    private String severity;
    private List<Long> involvedEmployeeIds;
    private String status;
}
```

---

## E09: Equipment & Asset Assignment

### 1. Overview
- Asset registry, checkout/return, certification validation, condition tracking.

### 2. Package Structure
```
com.wms.asset
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
```java
@Entity
public class Asset extends BaseEntity {
    private String assetTag;
    private String type;
    private String condition;
    @ManyToOne
    private Employee assignedTo;
    private LocalDate checkoutDate;
    private LocalDate returnDate;
}
```

### 4. Repository Layer
- Overdue assets, history log.

### 5. Service Layer
- Certification validation, checkout/return logic.

### 6. Controller Layer
- Asset CRUD, assignment endpoints.

### 7. Configuration
- Asset types in `application.yml`.

### 8. Integration Points
- Certification, reporting.

### 9. Sample Implementation
```java
public class AssetAssignmentDto {
    private Long assetId;
    private Long employeeId;
    private LocalDate checkoutDate;
    private LocalDate returnDate;
}
```

---

## E10: Performance Reviews & Goals

### 1. Overview
- Review templates, goals, competencies, ratings, acknowledgements, PDF export.

### 2. Package Structure
```
com.wms.review
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
```java
@Entity
public class PerformanceReview extends BaseEntity {
    @ManyToOne
    private Employee employee;
    private String cycle;
    private String goals;
    private String competencies;
    private Integer rating;
    private Boolean acknowledgedBySupervisor;
    private Boolean acknowledgedByEmployee;
    private String pdfUrl;
}
```

### 4. Repository Layer
- Review cycles, immutable history.

### 5. Service Layer
- PDF export, acknowledgement workflow.

### 6. Controller Layer
- Review CRUD, export endpoints.

### 7. Configuration
- Review cycles in `application.yml`.

### 8. Integration Points
- Reporting, notifications.

### 9. Sample Implementation
```java
public class PerformanceReviewDto {
    private Long employeeId;
    private String cycle;
    private String goals;
    private String competencies;
    private Integer rating;
}
```

---

## E11: Payroll Export Integration

### 1. Overview
- Generate payroll files from attendance/leave, external provider format mapping, SFTP/API delivery.

### 2. Package Structure
```
com.wms.payroll
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
```java
@Entity
public class PayrollExport extends BaseEntity {
    private LocalDate exportDate;
    private String provider;
    private String fileUrl;
    private Boolean delivered;
    private String deliveryMethod; // SFTP, API
}
```

### 4. Repository Layer
- Export history, failed deliveries.

### 5. Service Layer
- Mapping, delivery, retry logic.

### 6. Controller Layer
- Export trigger, status endpoints.

### 7. Configuration
- Provider formats, SFTP/API credentials in `application.yml`.

### 8. Integration Points
- Attendance, leave, external payroll provider.

### 9. Sample Implementation
```java
public class PayrollExportDto {
    private LocalDate exportDate;
    private String provider;
    private String deliveryMethod;
}
```

---

## E12: Notifications & Announcements

### 1. Overview
- In-app, email/SMS notifications for shifts, certs, approvals, opt-in/out, quiet hours.

### 2. Package Structure
```
com.wms.notification
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
```java
@Entity
public class Notification extends BaseEntity {
    private String channel; // IN_APP, EMAIL, SMS
    private String template;
    private String recipient;
    private Boolean delivered;
    private LocalDateTime sentAt;
}
```

### 4. Repository Layer
- Delivery status, opt-in/out.

### 5. Service Layer
- Rate limiting, quiet hours, localization.

### 6. Controller Layer
- Notification CRUD, announcement endpoints.

### 7. Configuration
- Channel settings, quiet hours in `application.yml`.

### 8. Integration Points
- Email/SMS providers.

### 9. Sample Implementation
```java
public class NotificationDto {
    private String channel;
    private String template;
    private String recipient;
}
```

---

## E13: Integration Layer

### 1. Overview
- REST APIs for HRIS sync, WMS integration, SSO/IDP, webhooks, JWT/OAuth2 security.

### 2. Package Structure
```
com.wms.integration
  âââ hris
  âââ wms
  âââ idp
  âââ webhook
  âââ controller
  âââ dto
```

### 3. Domain Model
- Sync job entities, webhook event logs.

### 4. Repository Layer
- Sync history, webhook logs.

### 5. Service Layer
- HRIS/WMS connectors, SSO integration.

### 6. Controller Layer
- API endpoints, webhook receivers.

### 7. Configuration
- JWT/OAuth2, external API credentials.

### 8. Integration Points
- HRIS, WMS, IDP, webhooks.

### 9. Sample Implementation
```java
@RestController
@RequestMapping("/api/integration/hris")
public class HrisController {
    @PostMapping("/sync")
    public ResponseEntity<?> syncEmployees(@RequestBody List<EmployeeDto> employees) { /* ... */ }
}
```

---

## E14: Audit Trail & Compliance

### 1. Overview
- Centralized audit logging for PII, schedules, approvals, payroll.

### 2. Package Structure
```
com.wms.audit
  âââ model
  âââ repository
  âââ service
  âââ controller
```

### 3. Domain Model
```java
@Entity
public class AuditLog extends BaseEntity {
    private String entity;
    private Long entityId;
    private String actor;
    private LocalDateTime timestamp;
    private String action; // CREATE, UPDATE, DELETE
    @Lob
    private String before;
    @Lob
    private String after;
}
```

### 4. Repository Layer
- Export by date/user/entity.

### 5. Service Layer
- Tamper-evident storage, coverage tests.

### 6. Controller Layer
- Audit log export endpoints.

### 7. Configuration
- Audit settings in `application.yml`.

### 8. Integration Points
- All modules.

### 9. Sample Implementation
```java
public void logChange(String entity, Long entityId, String actor, String action, Object before, Object after) { /* ... */ }
```

---

## E15: Reporting & Analytics

### 1. Overview
- Operational reports for attendance, overtime, leave, certifications, safety KPIs, CSV/PDF export.

### 2. Package Structure
```
com.wms.reporting
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
- Report entities, export logs.

### 4. Repository Layer
- Filtering by date, department, shift.

### 5. Service Layer
- Metrics calculation, export logic.

### 6. Controller Layer
- Report endpoints, export triggers.

### 7. Configuration
- Export settings in `application.yml`.

### 8. Integration Points
- BI tools, metrics endpoints.

### 9. Sample Implementation
```java
@RestController
@RequestMapping("/reports")
public class ReportController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> exportAttendance(@RequestParam LocalDate from, @RequestParam LocalDate to) { /* ... */ }
}
```

---

## E16: Mobile Access (PWA)

### 1. Overview
- Responsive views for clock-in/out, schedules, leave requests, offline-friendly PWA.

### 2. Package Structure
```
com.wms.mobile
  âââ controller
  âââ service
  âââ pwa
```

### 3. Domain Model
- Mobile-specific DTOs.

### 4. Repository Layer
- N/A (uses existing repositories).

### 5. Service Layer
- Offline queue, conflict resolution.

### 6. Controller Layer
- Mobile endpoints, manifest.

### 7. Configuration
- PWA manifest, Lighthouse score.

### 8. Integration Points
- Attendance, scheduling, notification.

### 9. Sample Implementation
```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> mobileClockIn(@RequestBody AttendanceDto dto) { /* ... */ }
}
```

---

## E17: Onboarding & Offboarding

### 1. Overview
- Automated provisioning/deprovisioning, training tasks, asset collection, access revocation.

### 2. Package Structure
```
com.wms.onboarding
  âââ model
  âââ repository
  âââ service
  âââ controller
  âââ dto
```

### 3. Domain Model
```java
@Entity
public class OnboardingTask extends BaseEntity {
    @ManyToOne
    private Employee employee;
    private String taskType;
    private Boolean completed;
    private LocalDate dueDate;
}
```

### 4. Repository Layer
- Task tracking, asset collection.

### 5. Service Layer
- HRIS sync, training assignment, access revocation.

### 6. Controller Layer
- Onboarding/offboarding endpoints.

### 7. Configuration
- Task templates in `application.yml`.

### 8. Integration Points
- HRIS, asset, certification, scheduling.

### 9. Sample Implementation
```java
public class OnboardingTaskDto {
    private Long employeeId;
    private String taskType;
    private Boolean completed;
    private LocalDate dueDate;
}
```

---

## E18: Localization & Multi-Tenant

### 1. Overview
- Multiple warehouse support with data isolation, UI localization (en, es), timezone-aware.

### 2. Package Structure
```
com.wms.localization
com.wms.tenant
```

### 3. Domain Model
```java
@Entity
public class Warehouse extends BaseEntity {
    private String name;
    private String locale;
    private String timezone;
}
@Entity
public class Tenant extends BaseEntity {
    private String name;
    private String schema;
}
```

### 4. Repository Layer
- Tenant/warehouse isolation queries.

### 5. Service Layer
- Locale resolution, timezone conversion.

### 6. Controller Layer
- Locale endpoints, warehouse CRUD.

### 7. Configuration
- Supported locales, tenant schemas in `application.yml`.

### 8. Integration Points
- All modules.

### 9. Sample Implementation
```java
public class LocaleDto {
    private String locale;
    private String timezone;
}
```

---

## E19: Observability & Monitoring

### 1. Overview
- Micrometer metrics, Prometheus/Grafana, JSON logging, distributed tracing, health checks.

### 2. Package Structure
```
com.wms.observability
  âââ config
  âââ metrics
  âââ tracing
```

### 3. Domain Model
- N/A (metrics/tracing config).

### 4. Repository Layer
- N/A.

### 5. Service Layer
- Custom metrics, tracing spans.

### 6. Controller Layer
- Health endpoints via actuator.

### 7. Configuration
- Micrometer, Prometheus, logging in `application.yml`.

### 8. Integration Points
- Grafana, ELK, tracing systems.

### 9. Sample Implementation
```java
@Bean
public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
    return registry -> registry.config().commonTags("application", "warehouse-employee-mgmt");
}
```

---

## E20: CI/CD & Deployment

### 1. Overview
- GitHub Actions/Jenkins pipeline, Docker images, staging/prod deployment, rollback capability.

### 2. Package Structure
```
.github/workflows
Jenkinsfile
Dockerfile
src/main/resources/prd
```

### 3. Domain Model
- N/A.

### 4. Repository Layer
- N/A.

### 5. Service Layer
- N/A.

### 6. Controller Layer
- N/A.

### 7. Configuration
- Pipeline scripts, Dockerfile, deployment manifests.

### 8. Integration Points
- GitHub, Jenkins, Docker, Kubernetes.

### 9. Sample Implementation
**Dockerfile**
```dockerfile
FROM eclipse-temurin:17-jre
COPY target/warehouse-employee-mgmt.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```
**GitHub Actions Workflow**
```yaml
name: CI/CD Pipeline
on:
  push:
    branches: [ main ]
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
      - name: Build Docker image
        run: docker build -t warehouse-employee-mgmt .
      - name: Push Docker image
        run: echo "Push to registry"
```

---

# Design Decisions

- **Modularization:** Each epic is mapped to a dedicated package/module for separation of concerns and maintainability.
- **Spring Boot Best Practices:** Use of JPA entities, DTOs, service/repository/controller layers, OpenAPI annotations, configuration via `application.yml`.
- **Security:** RBAC enforced at both endpoint and method levels; row-level security for sensitive data.
- **Observability:** Actuator, Micrometer, Prometheus, and distributed tracing for production readiness.
- **Extensibility:** Integration points for HRIS, WMS, payroll, notifications, and webhooks.
- **Compliance:** Audit logging, immutable history, and export capabilities for regulatory needs.
- **Mobile & Localization:** PWA support and locale/timezone-aware design for global warehouses.
- **CI/CD:** Automated pipelines, Dockerization, and rollback strategies for robust deployments.

---

# End of Document

**This technical design document is ready for upload as 'Technical_Design_Document.md' in the PRD directory.**