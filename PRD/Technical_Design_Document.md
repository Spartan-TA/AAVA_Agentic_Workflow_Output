# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

**Version:** 1.0  
**Target Stack:** Spring Boot (Java), Maven, REST, JPA/Hibernate, Spring Security, Flyway/Liquibase, Actuator  
**Audience:** Spring Boot Developers  
**Formatting:** All code blocks are in Java unless otherwise noted. Diagrams are described in Markdown.  
**Note:** All sections use escaped newlines (`
`) for multi-line text formatting.

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
18. [E18: Localization & Multi-Tenant](#e18-localization--multi-tenant)
19. [E19: Observability & Monitoring](#e19-observability--monitoring)
20. [E20: Deployment & CI/CD](#e20-deployment--cicd)

---

## <a name="e01-project-scaffolding--domain-setup"></a>E01: Project Scaffolding & Domain Setup

### 1. Overview of Spring Boot Architecture

- **Modules:** Core, Employee, Scheduling, Attendance, Safety, Integration, Notification, Reporting, Security, Audit, Mobile, Config
- **Base Packages:**  
  - `com.wems` (root)  
  - `com.wems.employee`, `com.wems.scheduling`, `com.wems.attendance`, `com.wems.safety`, etc.
- **DB Migration:** Flyway/Liquibase for schema versioning
- **Actuator:** Enabled for health, metrics, info endpoints

### 2. Package Structure & Module Definitions

```
com.wems
  âââ config
  âââ employee
  âââ scheduling
  âââ attendance
  âââ safety
  âââ equipment
  âââ performance
  âââ payroll
  âââ notification
  âââ integration
  âââ audit
  âââ reporting
  âââ mobile
  âââ security
```

### 3. Entity Design

- No domain entities in this epic; focus is on scaffolding.

### 4. Service Layer Specifications

- No business logic; only base services for health checks.

### 5. Repository Layer Specifications

- N/A

### 6. Controller Specifications

- Health check via Actuator: `/actuator/health`

### 7. Configuration & Security

- `application.yml` for environment config
- Enable Actuator endpoints

### 8. Integration Points

- None at this stage

### 9. Code Snippets

**pom.xml (excerpt):**
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-core</artifactId>
</dependency>
```

**application.yml:**
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

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

---

## <a name="e02-employee-master-data-crud"></a>E02: Employee Master Data (CRUD)

### 1. Overview

- Centralized employee record management
- CRUD APIs, DTO pattern, soft-delete, pagination/filtering

### 2. Package Structure

- `com.wems.employee`
  - `entity`, `dto`, `repository`, `service`, `controller`

### 3. Entity Design

**Employee.java**
```java
@Entity
@Table(name = "employees")
public class Employee {
  @Id @GeneratedValue
  private Long id;

  @Column(unique = true, nullable = false)
  private String badgeId;

  private String name;
  private String role;
  private String department;
  private String shiftGroup;
  private LocalDate hireDate;
  private String status; // ACTIVE, INACTIVE, TERMINATED

  private Boolean deleted = false;

  // Getters/Setters
}
```

### 4. Service Layer

- `EmployeeService` interface + `EmployeeServiceImpl`
- Methods: create, get, update, delete (soft), list (with filters)

### 5. Repository Layer

- `EmployeeRepository extends JpaRepository<Employee, Long>`
- Custom: `findByBadgeId`, `findAllByDeletedFalse`, etc.

### 6. Controller

- `EmployeeController`
- Endpoints:
  - `POST /employees`
  - `GET /employees`
  - `GET /employees/{id}`
  - `PUT /employees/{id}`
  - `PATCH /employees/{id}`
  - `DELETE /employees/{id}` (soft delete)
- Pagination: `GET /employees?page=0&size=20`
- Filtering: `GET /employees?department=Shipping&status=ACTIVE`

### 7. Configuration & Security

- RBAC enforced (see E03)
- OpenAPI/Swagger enabled

### 8. Integration Points

- HRIS sync (see E13)

### 9. Code Snippets

**EmployeeDTO.java**
```java
public class EmployeeDTO {
  private Long id;
  private String badgeId;
  private String name;
  private String role;
  private String department;
  private String shiftGroup;
  private LocalDate hireDate;
  private String status;
}
```

**EmployeeController.java (excerpt)**
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
  @Autowired
  private EmployeeService employeeService;

  @PostMapping
  public ResponseEntity<EmployeeDTO> create(@RequestBody @Valid EmployeeDTO dto) {
    return ResponseEntity.ok(employeeService.create(dto));
  }

  @GetMapping
  public Page<EmployeeDTO> list(@RequestParam Map<String, String> filters, Pageable pageable) {
    return employeeService.list(filters, pageable);
  }

  // ... other endpoints
}
```

---

## <a name="e03-role-based-access-control-rbac"></a>E03: Role Based Access Control (RBAC)

### 1. Overview

- Spring Security for endpoint/method security
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Row-level constraints (e.g., SUPERVISOR sees only their team)
- API key/OAuth2 toggle

### 2. Package Structure

- `com.wems.security`
  - `config`, `service`, `model`

### 3. Entity Design

**User.java**
```java
@Entity
public class User {
  @Id @GeneratedValue
  private Long id;
  private String username;
  private String password;
  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> roles;
  // ... link to Employee
}
```

### 4. Service Layer

- `UserDetailsServiceImpl` for authentication
- `PermissionEvaluator` for row-level security

### 5. Repository Layer

- `UserRepository extends JpaRepository<User, Long>`

### 6. Controller

- `/auth/login`, `/auth/logout`, `/auth/me`

### 7. Configuration & Security

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
        .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
        .anyRequest().authenticated()
      .and()
      .oauth2Login() // or .apiKey() toggle
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
  }
}
```

### 8. Integration Points

- OAuth2/JWT provider
- API key config

### 9. Code Snippets

**Method Security**
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(#id, authentication))")
public EmployeeDTO getEmployee(Long id) { ... }
```

---

## <a name="e04-time--attendance-clock-inout"></a>E04: Time & Attendance (Clock In/Out)

### 1. Overview

- Clock-in/out endpoints, geofence/device capture, missed punch correction
- Shift association, daily totals, approval workflow for corrections

### 2. Package Structure

- `com.wems.attendance`
  - `entity`, `dto`, `repository`, `service`, `controller`

### 3. Entity Design

**AttendanceEvent.java**
```java
@Entity
public class AttendanceEvent {
  @Id @GeneratedValue
  private Long id;
  @ManyToOne
  private Employee employee;
  private LocalDateTime timestamp;
  private String type; // CLOCK_IN, CLOCK_OUT
  private String deviceId;
  private String location; // optional geofence
  private Boolean approved = true;
  private String correctionReason;
}
```

### 4. Service Layer

- `AttendanceService`
  - `clockIn`, `clockOut`, `getDailyTotals`, `submitCorrection`

### 5. Repository Layer

- `AttendanceRepository extends JpaRepository<AttendanceEvent, Long>`

### 6. Controller

- `POST /attendance/clock-in`
- `POST /attendance/clock-out`
- `GET /attendance/daily-totals?employeeId=...&date=...`
- `POST /attendance/correction`

### 7. Configuration & Security

- Only authenticated users
- SUPERVISOR/HR approve corrections

### 8. Integration Points

- Payroll (E11)
- Reporting (E15)

### 9. Code Snippets

**AttendanceController.java (excerpt)**
```java
@PostMapping("/clock-in")
public ResponseEntity<?> clockIn(@RequestBody ClockEventDTO dto) {
  attendanceService.clockIn(dto);
  return ResponseEntity.ok().build();
}
```

---

## <a name="e05-shift--schedule-management"></a>E05: Shift & Schedule Management

### 1. Overview

- Shift templates, rotations, overtime, blackout dates, calendars

### 2. Package Structure

- `com.wems.scheduling`
  - `entity`, `dto`, `repository`, `service`, `controller`

### 3. Entity Design

**ShiftTemplate.java**
```java
@Entity
public class ShiftTemplate {
  @Id @GeneratedValue
  private Long id;
  private String name;
  private LocalTime startTime;
  private LocalTime endTime;
  private Boolean recurring;
  private String recurrencePattern; // e.g., CRON or custom
}
```

**EmployeeShift.java**
```java
@Entity
public class EmployeeShift {
  @Id @GeneratedValue
  private Long id;
  @ManyToOne
  private Employee employee;
  @ManyToOne
  private ShiftTemplate shiftTemplate;
  private LocalDate date;
  private Boolean overtime;
}
```

### 4. Service Layer

- `ShiftService`
  - CRUD for templates, assign shifts, detect conflicts

### 5. Repository Layer

- `ShiftTemplateRepository`
- `EmployeeShiftRepository`

### 6. Controller

- `POST /shifts/templates`
- `GET /shifts/templates`
- `POST /shifts/assign`
- `GET /shifts/my`

### 7. Configuration & Security

- Only SUPERVISOR/ADMIN can assign/bulk-assign

### 8. Integration Points

- Calendar APIs (optional)

### 9. Code Snippets

**ShiftService.java (conflict detection)**
```java
public boolean hasConflict(Employee employee, LocalDate date, LocalTime start, LocalTime end) {
  // Query for overlapping EmployeeShift records
}
```

---

## <a name="e06-leave--absence-management"></a>E06: Leave & Absence Management

### 1. Overview

- PTO/sick/unpaid leave requests, approval, accruals, integration with scheduling/payroll

### 2. Package Structure

- `com.wems.leave`
  - `entity`, `dto`, `repository`, `service`, `controller`

### 3. Entity Design

**LeaveRequest.java**
```java
@Entity
public class LeaveRequest {
  @Id @GeneratedValue
  private Long id;
  @ManyToOne
  private Employee employee;
  private LocalDate startDate;
  private LocalDate endDate;
  private String type; // PTO, SICK, UNPAID
  private String status; // PENDING, APPROVED, DENIED
  private String approver;
  private String comments;
}
```

### 4. Service Layer

- `LeaveService`
  - requestLeave, approveLeave, updateBalances

### 5. Repository Layer

- `LeaveRequestRepository`

### 6. Controller

- `POST /leave/requests`
- `GET /leave/requests`
- `POST /leave/approve`

### 7. Configuration & Security

- Only SUPERVISOR/HR can approve

### 8. Integration Points

- Scheduling (auto-flag shifts)
- Payroll (E11)

### 9. Code Snippets

**LeaveService.java (accrual update)**
```java
public void updateAccrual(Employee employee, String type, int days) { ... }
```

---

## <a name="e07-training--certification-tracking"></a>E07: Training & Certification Tracking

### 1. Overview

- Track certifications, expirations, renewals, block assignments, upload proof

### 2. Package Structure

- `com.wems.certification`
  - `entity`, `dto`, `repository`, `service`, `controller`

### 3. Entity Design

**Certification.java**
```java
@Entity
public class Certification {
  @Id @GeneratedValue
  private Long id;
  private String name;
  private LocalDate issueDate;
  private LocalDate expiryDate;
  private String documentUrl;
  @ManyToOne
  private Employee employee;
}
```

### 4. Service Layer

- `CertificationService`
  - CRUD, alerting, assignment checks

### 5. Repository Layer

- `CertificationRepository`

### 6. Controller

- `POST /certifications`
- `GET /certifications`
- `GET /certifications/expiring`

### 7. Configuration & Security

- Alerts 30/7 days before expiry

### 8. Integration Points

- Scheduling (block unqualified)
- Notification (E12)

### 9. Code Snippets

**CertificationService.java (expiry alert)**
```java
public List<Certification> findExpiringSoon(int days) { ... }
```

---

## <a name="e08-safety-incidents--osha-reporting"></a>E08: Safety Incidents & OSHA Reporting

### 1. Overview

- Record incidents, workflow for investigation, OSHA summary

### 2. Package Structure

- `com.wems.safety`
  - `entity`, `dto`, `repository`, `service`, `controller`

### 3. Entity Design

**SafetyIncident.java**
```java
@Entity
public class SafetyIncident {
  @Id @GeneratedValue
  private Long id;
  private String severity;
  private String location;
  private String description;
  private String status; // OPEN, INVESTIGATING, RESOLVED
  @ManyToMany
  private List<Employee> involvedEmployees;
  private LocalDateTime reportedAt;
}
```

### 4. Service Layer

- `SafetyService`
  - recordIncident, updateStatus, generateOSHAReport

### 5. Repository Layer

- `SafetyIncidentRepository`

### 6. Controller

- `POST /safety/incidents`
- `GET /safety/incidents`
- `POST /safety/incidents/{id}/status`

### 7. Configuration & Security

- Only SUPERVISOR/HR can update status

### 8. Integration Points

- Reporting (E15)

### 9. Code Snippets

**SafetyService.java (OSHA export)**
```java
public File exportOSHAReport(LocalDate from, LocalDate to) { ... }
```

---

## <a name="e09-equipment--asset-assignment"></a>E09: Equipment & Asset Assignment

### 1. Overview

- Assign assets, track check-in/out, block if cert missing, asset condition

### 2. Package Structure

- `com.wems.equipment`
  - `entity`, `dto`, `repository`, `service`, `controller`

### 3. Entity Design

**Asset.java**
```java
@Entity
public class Asset {
  @Id @GeneratedValue
  private Long id;
  private String type; // SCANNER, FORKLIFT, PPE
  private String serialNumber;
  private String condition;
  private Boolean checkedOut;
  @ManyToOne
  private Employee assignedTo;
}
```

### 4. Service Layer

- `AssetService`
  - assignAsset, checkIn, checkOut, validateCertification

### 5. Repository Layer

- `AssetRepository`

### 6. Controller

- `POST /assets/assign`
- `POST /assets/check-in`
- `POST /assets/check-out`
- `GET /assets/overdue`

### 7. Configuration & Security

- Block if cert invalid

### 8. Integration Points

- Certification (E07)

### 9. Code Snippets

**AssetService.java (cert validation)**
```java
public boolean canAssignAsset(Employee employee, String assetType) {
  // Check employee certifications
}
```

---

## <a name="e10-performance-reviews--goals"></a>E10: Performance Reviews & Goals

### 1. Overview

- Review templates, goals, ratings, comments, workflow

### 2. Package Structure

- `com.wems.performance`
  - `entity`, `dto`, `repository`, `service`, `controller`

### 3. Entity Design

**PerformanceReview.java**
```java
@Entity
public class PerformanceReview {
  @Id @GeneratedValue
  private Long id;
  @ManyToOne
  private Employee employee;
  private String cycle; // Q1-2024, 2024-Annual
  private String goals;
  private String competencies;
  private String ratings;
  private String comments;
  private String status; // DRAFT, SUBMITTED, ACKNOWLEDGED
  private LocalDateTime submittedAt;
}
```

### 4. Service Layer

- `PerformanceService`
  - createReview, submitReview, acknowledgeReview

### 5. Repository Layer

- `PerformanceReviewRepository`

### 6. Controller

- `POST /performance/reviews`
- `GET /performance/reviews`
- `POST /performance/reviews/{id}/acknowledge`

### 7. Configuration & Security

- Role-based visibility

### 8. Integration Points

- PDF export

### 9. Code Snippets

**PerformanceService.java (immutable after sign-off)**
```java
@Transactional
public void acknowledgeReview(Long reviewId) {
  // Lock and mark as immutable
}
```

---

## <a name="e11-payroll-export-integration"></a>E11: Payroll Export Integration

### 1. Overview

- Generate payroll files from attendance/leave, map to provider, secure delivery

### 2. Package Structure

- `com.wems.payroll`
  - `service`, `controller`, `integration`

### 3. Entity Design

- No new entities; uses Attendance, Leave

### 4. Service Layer

- `PayrollExportService`
  - generateExport, deliverExport, retryOnFailure

### 5. Repository Layer

- N/A

### 6. Controller

- `POST /payroll/export`
- `GET /payroll/exports`

### 7. Configuration & Security

- SFTP/API credentials in config

### 8. Integration Points

- External payroll provider

### 9. Code Snippets

**PayrollExportService.java (retry logic)**
```java
@Scheduled(fixedDelay = 60000)
public void retryFailedExports() { ... }
```

---

## <a name="e12-notifications--announcements"></a>E12: Notifications & Announcements

### 1. Overview

- In-app/email/SMS notifications, templates, quiet hours

### 2. Package Structure

- `com.wems.notification`
  - `entity`, `service`, `controller`, `integration`

### 3. Entity Design

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
}
```

### 4. Service Layer

- `NotificationService`
  - sendNotification, trackDelivery, applyRateLimit

### 5. Repository Layer

- `NotificationRepository`

### 6. Controller

- `POST /notifications`
- `GET /notifications`
- `POST /announcements`

### 7. Configuration & Security

- User opt-in/out, quiet hours

### 8. Integration Points

- Email/SMS providers

### 9. Code Snippets

**NotificationService.java (rate limiting)**
```java
public boolean canSend(String recipient, String channel) {
  // Check last sent timestamp
}
```

---

## <a name="e13-integration-layer-hriswms-apis"></a>E13: Integration Layer (HRIS/WMS APIs)

### 1. Overview

- REST APIs/connectors for HRIS, WMS, IDP; webhooks

### 2. Package Structure

- `com.wems.integration`
  - `hris`, `wms`, `idp`, `webhook`, `controller`

### 3. Entity Design

- Use Employee, Department, etc.

### 4. Service Layer

- `HRISSyncService`, `WMSConnector`, `IDPService`

### 5. Repository Layer

- N/A

### 6. Controller

- `POST /integration/hris/sync`
- `POST /integration/webhook`

### 7. Configuration & Security

- JWT/OAuth2-secured APIs

### 8. Integration Points

- HRIS, WMS, IDP

### 9. Code Snippets

**HRISSyncService.java**
```java
public void syncEmployees(List<EmployeeDTO> hrisEmployees) {
  // Upsert logic
}
```

---

## <a name="e14-audit-trail--compliance"></a>E14: Audit Trail & Compliance

### 1. Overview

- Centralized audit logging, tamper-evident storage

### 2. Package Structure

- `com.wems.audit`
  - `entity`, `service`, `repository`

### 3. Entity Design

**AuditLog.java**
```java
@Entity
public class AuditLog {
  @Id @GeneratedValue
  private Long id;
  private String entityName;
  private Long entityId;
  private String action; // CREATE, UPDATE, DELETE
  private String actor;
  private LocalDateTime timestamp;
  @Lob
  private String before;
  @Lob
  private String after;
}
```

### 4. Service Layer

- `AuditService`
  - logChange, exportLogs

### 5. Repository Layer

- `AuditLogRepository`

### 6. Controller

- `GET /audit/logs`

### 7. Configuration & Security

- Only ADMIN/HR can export

### 8. Integration Points

- All modules

### 9. Code Snippets

**AuditAspect.java (AOP for logging)**
```java
@AfterReturning(...)
public void logAudit(JoinPoint jp, Object result) { ... }
```

---

## <a name="e15-reporting--analytics"></a>E15: Reporting & Analytics

### 1. Overview

- Attendance, overtime, leave, certs, safety KPIs; CSV/PDF export

### 2. Package Structure

- `com.wems.reporting`
  - `service`, `controller`

### 3. Entity Design

- Uses existing entities

### 4. Service Layer

- `ReportingService`
  - generateReport, exportCSV, exportPDF

### 5. Repository Layer

- N/A

### 6. Controller

- `GET /reports/attendance`
- `GET /reports/overtime`
- `GET /reports/leave`
- `GET /reports/certifications`
- `GET /reports/safety`

### 7. Configuration & Security

- Role-based access

### 8. Integration Points

- BI tools (metrics endpoints)

### 9. Code Snippets

**ReportingService.java (CSV export)**
```java
public File exportAttendanceReport(LocalDate from, LocalDate to) { ... }
```

---

## <a name="e16-mobile-access-pwa"></a>E16: Mobile Access (PWA)

### 1. Overview

- Responsive views, offline queue, PWA manifest

### 2. Package Structure

- `com.wems.mobile`
  - `controller`, `service`, `pwa`

### 3. Entity Design

- Uses core entities

### 4. Service Layer

- `MobileService`
  - offlineQueue, syncOnReconnect

### 5. Repository Layer

- N/A

### 6. Controller

- `GET /mobile/manifest.json`
- `POST /mobile/clock-events`

### 7. Configuration & Security

- JWT tokens for mobile

### 8. Integration Points

- Attendance, Notification

### 9. Code Snippets

**manifest.json**
```json
{
  "name": "Warehouse EMS",
  "short_name": "WEMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

---

## <a name="e17-onboarding--offboarding-workflow"></a>E17: Onboarding & Offboarding Workflow

### 1. Overview

- Automate provisioning, initial schedule, training, deprovisioning

### 2. Package Structure

- `com.wems.onboarding`
  - `service`, `controller`

### 3. Entity Design

- Uses Employee, Certification, Asset

### 4. Service Layer

- `OnboardingService`
  - provisionAccount, assignInitialSchedule, assignTraining, deprovision

### 5. Repository Layer

- N/A

### 6. Controller

- `POST /onboarding/new-hire`
- `POST /offboarding/terminate`

### 7. Configuration & Security

- Only HR/ADMIN

### 8. Integration Points

- HRIS, Asset, Certification

### 9. Code Snippets

**OnboardingService.java**
```java
public void onboardEmployee(EmployeeDTO dto) {
  // Create user, assign schedule, training, assets
}
```

---

## <a name="e18-localization--multi-tenant"></a>E18: Localization & Multi-Tenant

### 1. Overview

- Multi-language support, tenant isolation

### 2. Package Structure

- `com.wems.localization`
- `com.wems.tenant`

### 3. Entity Design

**Tenant.java**
```java
@Entity
public class Tenant {
  @Id @GeneratedValue
  private Long id;
  private String name;
  private String locale;
}
```

### 4. Service Layer

- `TenantService`, `LocalizationService`

### 5. Repository Layer

- `TenantRepository`

### 6. Controller

- `GET /tenants`
- `POST /tenants`
- `GET /localization/messages`

### 7. Configuration & Security

- Tenant context filter/interceptor

### 8. Integration Points

- All modules

### 9. Code Snippets

**LocaleResolverConfig.java**
```java
@Bean
public LocaleResolver localeResolver() {
  SessionLocaleResolver slr = new SessionLocaleResolver();
  slr.setDefaultLocale(Locale.US);
  return slr;
}
```

---

## <a name="e19-observability--monitoring"></a>E19: Observability & Monitoring

### 1. Overview

- Metrics, logs, traces, health checks

### 2. Package Structure

- `com.wems.monitoring`

### 3. Entity Design

- N/A

### 4. Service Layer

- `MonitoringService` (custom metrics)

### 5. Repository Layer

- N/A

### 6. Controller

- Expose via `/actuator`

### 7. Configuration & Security

- Actuator endpoints, logback config

### 8. Integration Points

- Prometheus, ELK, Grafana

### 9. Code Snippets

**application.yml (metrics)**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

---

## <a name="e20-deployment--cicd"></a>E20: Deployment & CI/CD

### 1. Overview

- Automated build, test, deploy pipelines

### 2. Package Structure

- `.github/workflows/`, `Dockerfile`, `k8s/`

### 3. Entity Design

- N/A

### 4. Service Layer

- N/A

### 5. Repository Layer

- N/A

### 6. Controller

- N/A

### 7. Configuration & Security

- Secrets in CI/CD, image scanning

### 8. Integration Points

- Docker, Kubernetes, GitHub Actions

### 9. Code Snippets

**.github/workflows/ci.yml**
```yaml
name: CI
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Build with Maven
        run: mvn clean install
      - name: Run Tests
        run: mvn test
      - name: Build Docker Image
        run: docker build -t wems:${{ github.sha }} .
```

---

# Appendix

- **Diagrams:**  
  - Entity Relationship Diagrams (ERDs) for each module (see entity sections)
  - Sequence diagrams for onboarding, attendance, payroll export (not shown here)
- **OpenAPI/Swagger:**  
  - All REST endpoints documented via Springdoc/OpenAPI annotations

---

**End of Document**