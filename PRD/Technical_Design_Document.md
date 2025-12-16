# Warehouse Employee Management System - Low-Level Technical Design Document

## Section: E01 - Project Scaffolding & Domain Setup

**Description:** Establishes the foundational Spring Boot project structure, configures Maven, sets up core modules (employee, scheduling, attendance, safety), integrates Flyway/Liquibase for DB migrations, and enables Actuator for health monitoring.

**Design Specification:**
- Maven multi-module project: core, employee, scheduling, attendance, safety
- Base package: com.warehousemgmt
- Directory structure: src/main/java/com/warehousemgmt/{module}
- Flyway/Liquibase migration scripts in src/main/resources/db/migration
- Spring Boot Actuator enabled in application.properties
- README with build/run instructions

**Sample Implementation:**
```java
// pom.xml (modules)
<modules>
    <module>core</module>
    <module>employee</module>
    <module>scheduling</module>
    <module>attendance</module>
    <module>safety</module>
</modules>

// application.properties
management.endpoints.web.exposure.include=health,info
spring.flyway.enabled=true

// Main Application
@SpringBootApplication
public class WarehouseMgmtApplication { 
    public static void main(String[] args) { 
        SpringApplication.run(WarehouseMgmtApplication.class, args); 
    } 
}
```

---

## Section: E02 - Employee Master Data (CRUD)

**Description:** Implements the Employee domain with full CRUD APIs, enforcing unique badgeId, supporting soft-delete, pagination, and filtering.

**Design Specification:**
- Entity: Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService with CRUD, soft-delete, filter, pagination
- Controller: EmployeeController with REST endpoints
- OpenAPI documentation with schema examples

**Sample Implementation:**
```java
@Entity
public class Employee {
    @Id @GeneratedValue private Long id;
    @Column(unique=true) private String badgeId;
    private String name, role, department, shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted;
}

// Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}

// Service
public Employee create(EmployeeDto dto) { /* validate badgeId, save */ }
public void softDelete(Long id) { /* set deleted=true */ }

// Controller
@RestController
@RequestMapping("/employees")
public class EmployeeController { /* CRUD endpoints with pagination/filter */ }
```

---

## Section: E03 - Role-Based Access Control (RBAC)

**Description:** Integrates Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER), method/endpoint security, and API key/OAuth2 toggle.

**Design Specification:**
- SecurityConfig: configures roles, endpoint access, method security
- UserDetailsService: loads users/roles
- API key/OAuth2 toggle via application.properties
- Row-level security in service layer

**Sample Implementation:**
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override 
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/admin/**").hasRole("ADMIN")
            .antMatchers("/employees/**").hasAnyRole("HR", "SUPERVISOR", "ADMIN")
            .anyRequest().authenticated();
        // API key/OAuth2 toggle
    }
}

// Method security
@PreAuthorize("hasRole('ADMIN')")
public void deleteEmployee(Long id) { ... }
```

---

## Section: E04 - Time & Attendance (Clock In/Out)

**Description:** Provides endpoints for clock-in/out events, geofence/device capture, shift association, missed punch handling, and corrections workflow.

**Design Specification:**
- Entity: AttendanceEvent (id, employeeId, type, timestamp, deviceId, location, status)
- Service: AttendanceService (clockIn, clockOut, corrections)
- Controller: AttendanceController (/attendance/clock-in, /clock-out)
- Geofence validation logic
- CSV export endpoint

**Sample Implementation:**
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type; // CLOCK_IN, CLOCK_OUT
    private LocalDateTime timestamp;
    private String deviceId, location, status;
}

// Service
public AttendanceEvent clockIn(Long empId, ...) { /* validate geofence, save event */ }
public AttendanceEvent clockOut(Long empId, ...) { /* compute hours, save event */ }

// Controller
@PostMapping("/attendance/clock-in")
public ResponseEntity<?> clockIn(@RequestBody ClockInDto dto) { ... }
```

---

## Section: E05 - Shift & Schedule Management

**Description:** Manages recurring shift templates, rotations, overtime rules, employee assignments, blackout dates, and operation calendars.

**Design Specification:**
- Entity: ShiftTemplate, ShiftAssignment, BlackoutDate
- Service: ShiftService (CRUD, conflict detection, bulk assign)
- Controller: ShiftController (/shifts, /schedules)
- Audit logging for assignments

**Sample Implementation:**
```java
@Entity
public class ShiftTemplate { /* fields: id, name, startTime, endTime, recurrence, overtimeRule */ }

@Entity
public class ShiftAssignment { /* fields: id, employeeId, shiftTemplateId, date */ }

@Entity
public class BlackoutDate { /* fields: id, date, reason */ }

// Service
public void assignShift(Long empId, Long shiftId, LocalDate date) { /* check conflicts, save */ }
```

---

## Section: E06 - Leave & Absence Management

**Description:** Enables PTO/sick/unpaid leave requests, approvals, accrual balances, and integration with scheduling/payroll.

**Design Specification:**
- Entity: LeaveRequest (id, employeeId, type, startDate, endDate, status, balance)
- Service: LeaveService (request, approve, update balances)
- Controller: LeaveController (/leave)
- Integration hooks for scheduling/payroll

**Sample Implementation:**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type; // PTO, SICK, UNPAID
    private LocalDate startDate, endDate;
    private String status; // REQUESTED, APPROVED, DENIED
    private int balance;
}

// Service
public LeaveRequest requestLeave(Long empId, LeaveDto dto) { ... }
public void approveLeave(Long leaveId) { ... }
```

---

## Section: E07 - Training & Certification Tracking

**Description:** Tracks required certifications, expirations, renewals, blocks assignment to tasks requiring expired certs, and uploads proof documents.

**Design Specification:**
- Entity: Certification (id, employeeId, type, expiryDate, documentUrl)
- Service: CertificationService (CRUD, expiry alerts, assignment checks)
- Controller: CertificationController (/certifications)
- File upload integration

**Sample Implementation:**
```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type;
    private LocalDate expiryDate;
    private String documentUrl;
}

// Service
public boolean isValid(Long empId, String certType) { /* check expiry */ }
```

---

## Section: E08 - Safety Incidents & OSHA Reporting

**Description:** Records incidents/near-misses, severity, location, involved employees, investigation workflow, and OSHA summary generation.

**Design Specification:**
- Entity: SafetyIncident (id, severity, location, description, status, involvedEmployeeIds)
- Service: SafetyService (record, workflow, export)
- Controller: SafetyController (/safety/incidents)
- OSHA report export endpoint

**Sample Implementation:**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private String severity, location, description, status;
    @ElementCollection private List<Long> involvedEmployeeIds;
}

// Service
public SafetyIncident recordIncident(SafetyIncidentDto dto) { ... }
```

---

## Section: E09 - Equipment & Asset Assignment

**Description:** Assigns assets to employees, tracks checkout/return, blocks use if certification missing, maintains asset condition.

**Design Specification:**
- Entity: Asset (id, type, condition, assignedEmployeeId, checkoutDate, returnDate)
- Service: AssetService (CRUD, check-in/out, certification validation)
- Controller: AssetController (/assets)
- History log per asset/employee

**Sample Implementation:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String type, condition;
    private Long assignedEmployeeId;
    private LocalDate checkoutDate, returnDate;
}

// Service
public void checkoutAsset(Long assetId, Long empId) { /* validate cert, assign */ }
```

---

## Section: E10 - Performance Reviews & Goals

**Description:** Supports review templates, goals, competencies, ratings, comments, supervisor/employee acknowledgements, and immutable history.

**Design Specification:**
- Entity: PerformanceReview (id, employeeId, period, goals, ratings, comments, status)
- Service: ReviewService (create, assign, submit, acknowledge)
- Controller: ReviewController (/reviews)
- PDF export integration

**Sample Implementation:**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String period, goals, ratings, comments, status;
}

// Service
public void submitReview(Long reviewId, ReviewDto dto) { ... }
```

---

## Section: E11 - Payroll Export Integration

**Description:** Generates payroll-ready files from attendance/leave, maps to provider formats, delivers securely via SFTP/API, and logs exports.

**Design Specification:**
- Service: PayrollExportService (generate, map, deliver, retry)
- Integration: SFTP/API client
- Audit logging for exports

**Sample Implementation:**
```java
public void exportPayroll(LocalDate period) {
    // Fetch approved attendance/leave, map to provider schema, deliver via SFTP/API
}
```

---

## Section: E12 - Notifications & Announcements

**Description:** Sends in-app/email/SMS notifications for shift changes, expiring certs, approvals, announcements, with quiet hours and opt-in/out.

**Design Specification:**
- Entity: Notification (id, employeeId, type, channel, content, status, timestamp)
- Service: NotificationService (send, track, rate-limit)
- Controller: NotificationController (/notifications)
- Integration: Email/SMS provider

**Sample Implementation:**
```java
@Entity
public class Notification {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type, channel, content, status;
    private LocalDateTime timestamp;
}

// Service
public void sendNotification(NotificationDto dto) { ... }
```

---

## Section: E13 - Integration Layer (HRIS/WMS APIs)

**Description:** Exposes REST APIs/connectors for HRIS, WMS, IDP for SSO, and webhooks for events.

**Design Specification:**
- REST controllers: HRISController, WMSController, WebhookController
- Security: JWT/OAuth2
- Sync jobs for HRIS/WMS
- OpenAPI documentation

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/hris")
public class HRISController { /* endpoints for new hires/terms */ }

@RestController
@RequestMapping("/api/wms")
public class WMSController { /* endpoints for department/location */ }
```

---

## Section: E14 - Audit Trail & Compliance

**Description:** Centralized audit logging for sensitive changes, tamper-evident storage, export by date/user/entity.

**Design Specification:**
- Entity: AuditLog (id, actor, timestamp, entity, before, after, action)
- Service: AuditService (log, export)
- Controller: AuditController (/audit)
- Immutable log table

**Sample Implementation:**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String actor, entity, action;
    private LocalDateTime timestamp;
    @Lob private String before, after;
}

// Service
public void logChange(String actor, String entity, Object before, Object after, String action) { ... }
```

---

## Section: E15 - Reporting & Analytics

**Description:** Provides operational reports (attendance, overtime, leave, certs, safety KPIs), CSV/PDF export, role-based dashboards, metrics endpoints.

**Design Specification:**
- Service: ReportingService (generate, filter, export)
- Controller: ReportingController (/reports)
- Metrics endpoints for BI

**Sample Implementation:**
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance") 
    public ResponseEntity<?> getAttendanceReport(...) { ... }
}
```

---

## Section: E16 - Mobile Access (PWA)

**Description:** Delivers responsive views for workers to clock-in/out, view schedules, request leave, see announcements, with offline support via PWA.

**Design Specification:**
- Frontend: PWA manifest, service worker, offline queue
- Backend: REST endpoints for mobile flows
- Conflict resolution logic for offline events

**Sample Implementation:**
```javascript
// manifest.json
{
  "name": "Warehouse Employee PWA",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#fff"
}

// Service Worker (offline queue)
self.addEventListener('fetch', function(event) { /* cache and queue logic */ });
```

---

## Section: E17 - Onboarding & Offboarding Workflow

**Description:** Automates provisioning of accounts, initial schedule, required training, deprovisioning access/assets on termination.

**Design Specification:**
- Service: OnboardingService, OffboardingService (provision, assign, revoke)
- Integration: HRIS sync, asset assignment, training tasks
- Controller: OnboardingController (/onboarding)

**Sample Implementation:**
```java
public void onboardEmployee(HRISNewHireDto dto) { /* create account, assign schedule/training/assets */ }
public void offboardEmployee(Long empId) { /* revoke access, collect assets, update schedules */ }
```

---

## Section: E18 - Localization & Multi-Tenant

**Description:** Supports multiple warehouses/regions with locale-specific date/time, currency, language, and tenant isolation.

**Design Specification:**
- Entity: Tenant (id, name, locale, timezone)
- Service: TenantService (resolve, isolate data)
- Configuration: MessageSource for i18n
- Multi-tenant DB schema

**Sample Implementation:**
```java
@Entity
public class Tenant {
    @Id @GeneratedValue private Long id;
    private String name, locale, timezone;
}

// Configuration
@Bean
public MessageSource messageSource() { /* i18n config */ }
```

---

## Section: E19 - Observability & Monitoring

**Description:** Implements structured logging (JSON), distributed tracing (OpenTelemetry), metrics (Micrometer), health checks, and alerting integrations.

**Design Specification:**
- Logging: Logback JSON config
- Tracing: OpenTelemetry integration
- Metrics: Micrometer, custom metrics
- Health: Actuator endpoints

**Sample Implementation:**
```yaml
# logback-spring.xml
<encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder" />

// application.properties
management.endpoints.web.exposure.include=health,metrics,info
```

---

## Section: E20 - Automated Testing & CI/CD

**Description:** Establishes unit, integration, contract, E2E tests, CI pipeline with quality gates, automated deployments, and rollback capability.

**Design Specification:**
- Test packages: unit (JUnit), integration (SpringBootTest), contract (Spring Cloud Contract), E2E (Selenium/Cypress)
- CI/CD: GitHub Actions/Jenkins pipeline config
- Quality gates: code coverage, static analysis

**Sample Implementation:**
```yaml
# .github/workflows/ci.yml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build & Test
        run: mvn clean verify
      - name: Deploy
        run: ./deploy.sh
```

---

## Conclusion

This document provides a comprehensive, production-ready low-level technical design for all 20 user stories of the warehouse employee management system, following Spring Boot best practices and industry standards. Each section is labeled, described, and includes design specifications and sample implementations for easy consumption by Spring Boot developers.