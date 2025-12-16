# Warehouse Employee Management System (EMS)
## Low-Level Technical Design Document
**Version:** 1.0
**Date:** 2024-06-09

---

# Table of Contents

1. [E01 - Project Scaffolding & Domain Setup](#e01)
2. [E02 - Employee Master Data (CRUD)](#e02)
3. [E03 - Role Based Access Control (RBAC)](#e03)
4. [E04 - Time & Attendance (Clock In/Out)](#e04)
5. [E05 - Shift & Schedule Management](#e05)
6. [E06 - Leave & Absence Management](#e06)
7. [E07 - Training & Certification Tracking](#e07)
8. [E08 - Safety Incidents & OSHA Reporting](#e08)
9. [E09 - Equipment & Asset Assignment](#e09)
10. [E10 - Performance Reviews & Goals](#e10)
11. [E11 - Payroll Export Integration](#e11)
12. [E12 - Notifications & Announcements](#e12)
13. [E13 - Integration Layer (HRIS/WMS APIs)](#e13)
14. [E14 - Audit Trail & Compliance](#e14)
15. [E15 - Reporting & Analytics](#e15)
16. [E16 - Mobile Access (PWA)](#e16)
17. [E17 - Onboarding & Offboarding Workflow](#e17)
18. [E18 - Localization & Multi-Tenant](#e18)
19. [E19 - Observability & Monitoring](#e19)
20. [E20 - Deployment & CI/CD](#e20)

---

<a name="e01"></a>
## E01 - Project Scaffolding & Domain Setup

Section: Spring Boot Project Structure
Description: Establishes the foundational structure for the EMS application, ensuring modularity, maintainability, and scalability.
Design Specification:
- Maven multi-module project: `core`, `employee`, `scheduling`, `attendance`, `safety`
- Base package: `com.warehouse.ems`
- Directory structure:
  - `com.warehouse.ems.employee`
  - `com.warehouse.ems.scheduling`
  - `com.warehouse.ems.attendance`
  - `com.warehouse.ems.safety`
- Flyway/Liquibase for DB migrations
- Spring Boot Actuator enabled for health checks
- README with build/run instructions

Sample Implementation:
```java
// Example Maven module structure
<modules>
  <module>core</module>
  <module>employee</module>
  <module>scheduling</module>
  <module>attendance</module>
  <module>safety</module>
</modules>

// application.properties
spring.application.name=warehouse-ems
management.endpoints.web.exposure.include=health,info,metrics
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
server.port=8080
```

---

<a name="e02"></a>
## E02 - Employee Master Data (CRUD)

Section: Domain Model
Description: Centralizes employee data with CRUD operations and ensures data integrity.
Design Specification:
- Entity: `Employee`
  - Fields: id, name, badgeId (unique), role, department, shiftGroup, hireDate, status, deleted (soft delete)
- DTOs: `EmployeeDTO`, `EmployeeCreateDTO`, `EmployeeUpdateDTO`
- Repository: `EmployeeRepository extends JpaRepository<Employee, Long>`
- Service: `EmployeeService` with CRUD methods
- Controller: `EmployeeController` with REST endpoints
  - POST `/employees`
  - GET `/employees`
  - GET `/employees/{id}`
  - PUT `/employees/{id}`
  - PATCH `/employees/{id}`
  - DELETE `/employees/{id}`
- Pagination, filtering, and OpenAPI documentation

Sample Implementation:
```java
@Entity
public class Employee {
  @Id @GeneratedValue private Long id;
  @Column(unique = true, nullable = false) private String badgeId;
  private String name;
  private String role;
  private String department;
  private String shiftGroup;
  private LocalDate hireDate;
  private String status;
  private boolean deleted = false;
  // getters/setters
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
  @PostMapping public ResponseEntity<EmployeeDTO> create(@RequestBody @Valid EmployeeCreateDTO dto) { ... }
  @GetMapping public Page<EmployeeDTO> list(Pageable pageable, @RequestParam Map<String, String> filters) { ... }
  @GetMapping("/{id}") public EmployeeDTO get(@PathVariable Long id) { ... }
  @PutMapping("/{id}") public EmployeeDTO update(@PathVariable Long id, @RequestBody EmployeeUpdateDTO dto) { ... }
  @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { ... }
}
```

---

<a name="e03"></a>
## E03 - Role Based Access Control (RBAC)

Section: Security Configuration
Description: Implements fine-grained access control using Spring Security with roles and method/endpoint security.
Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Security config: `@EnableGlobalMethodSecurity(prePostEnabled = true)`
- Row-level security in repositories/services
- API key/OAuth2 toggle via config
- Unauthorized (401) and forbidden (403) handling

Sample Implementation:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .authorizeRequests()
        .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
        .antMatchers("/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated()
      .and()
        .oauth2Login()
      .and()
        .httpBasic();
  }
}

// Method-level security
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(#id))")
public EmployeeDTO getEmployee(Long id) { ... }
```

---

<a name="e04"></a>
## E04 - Time & Attendance (Clock In/Out)

Section: Attendance Domain & Workflow
Description: Captures clock-in/out events, calculates hours, and manages corrections.
Design Specification:
- Entity: `AttendanceEvent` (id, employee, timestamp, type [IN/OUT], deviceId, location, status)
- Service: `AttendanceService` (clockIn, clockOut, calculateHours, handleCorrections)
- Controller: `AttendanceController`
  - POST `/attendance/clock-in`
  - POST `/attendance/clock-out`
  - GET `/attendance/reports`
- Geofence/device capture (optional fields)
- Correction workflow with approval tasks

Sample Implementation:
```java
@Entity
public class AttendanceEvent {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private LocalDateTime timestamp;
  private String type; // IN or OUT
  private String deviceId;
  private String location;
  private String status; // NORMAL, CORRECTION_PENDING, APPROVED
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
  @PostMapping("/clock-in") public ResponseEntity<?> clockIn(@RequestBody ClockEventDTO dto) { ... }
  @PostMapping("/clock-out") public ResponseEntity<?> clockOut(@RequestBody ClockEventDTO dto) { ... }
  @GetMapping("/reports") public List<AttendanceReportDTO> getReports(...) { ... }
}
```

---

<a name="e05"></a>
## E05 - Shift & Schedule Management

Section: Scheduling Domain
Description: Manages shift templates, rotations, assignments, and blackout dates.
Design Specification:
- Entities: `ShiftTemplate`, `ShiftAssignment`, `BlackoutDate`
- Service: `ScheduleService` (CRUD, conflict detection, bulk assignment)
- Controller: `ScheduleController`
  - CRUD endpoints for templates and assignments
  - GET `/schedules/my-shifts`
- Audit entries for changes

Sample Implementation:
```java
@Entity
public class ShiftTemplate {
  @Id @GeneratedValue private Long id;
  private String name;
  private LocalTime startTime;
  private LocalTime endTime;
  private boolean recurring;
  // ...
}

@Entity
public class ShiftAssignment {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  @ManyToOne private ShiftTemplate template;
  private LocalDate date;
  // ...
}

@RestController
@RequestMapping("/schedules")
public class ScheduleController {
  @PostMapping("/templates") public ShiftTemplate createTemplate(@RequestBody ShiftTemplateDTO dto) { ... }
  @GetMapping("/my-shifts") public List<ShiftAssignmentDTO> getMyShifts(...) { ... }
}
```

---

<a name="e06"></a>
## E06 - Leave & Absence Management

Section: Leave Domain
Description: Handles PTO, sick, unpaid leave requests, approvals, and accruals.
Design Specification:
- Entities: `LeaveRequest`, `LeaveBalance`, `LeavePolicy`
- Service: `LeaveService` (request, approve, deny, update balances)
- Controller: `LeaveController`
  - POST `/leave/request`
  - POST `/leave/approve`
  - GET `/leave/balances`
- Integration with scheduling and payroll

Sample Implementation:
```java
@Entity
public class LeaveRequest {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String type; // PTO, SICK, UNPAID
  private LocalDate startDate;
  private LocalDate endDate;
  private String status; // PENDING, APPROVED, DENIED
}

@RestController
@RequestMapping("/leave")
public class LeaveController {
  @PostMapping("/request") public LeaveRequest requestLeave(@RequestBody LeaveRequestDTO dto) { ... }
  @PostMapping("/approve") public LeaveRequest approveLeave(@RequestBody ApproveLeaveDTO dto) { ... }
  @GetMapping("/balances") public LeaveBalanceDTO getBalances(@RequestParam Long employeeId) { ... }
}
```

---

<a name="e07"></a>
## E07 - Training & Certification Tracking

Section: Certification Domain
Description: Tracks certifications, expirations, renewals, and proof documents.
Design Specification:
- Entities: `Certification`, `EmployeeCertification`
- Service: `CertificationService` (CRUD, expiry alerts, assignment checks)
- Controller: `CertificationController`
  - CRUD endpoints
  - GET `/certifications/expiring`
- File upload for proof documents

Sample Implementation:
```java
@Entity
public class Certification {
  @Id @GeneratedValue private Long id;
  private String name;
  private int validDays;
}

@Entity
public class EmployeeCertification {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  @ManyToOne private Certification certification;
  private LocalDate issueDate;
  private LocalDate expiryDate;
  private String proofDocumentUrl;
}

@RestController
@RequestMapping("/certifications")
public class CertificationController {
  @PostMapping public EmployeeCertification assignCertification(@RequestBody AssignCertificationDTO dto) { ... }
  @GetMapping("/expiring") public List<EmployeeCertificationDTO> getExpiringCerts(...) { ... }
}
```

---

<a name="e08"></a>
## E08 - Safety Incidents & OSHA Reporting

Section: Safety Domain
Description: Records incidents, manages investigation workflow, and generates OSHA reports.
Design Specification:
- Entities: `SafetyIncident`, `IncidentInvestigation`
- Service: `SafetyService` (record, workflow, reporting)
- Controller: `SafetyController`
  - POST `/safety/incidents`
  - GET `/safety/reports`
- Status workflow: OPEN, INVESTIGATING, RESOLVED

Sample Implementation:
```java
@Entity
public class SafetyIncident {
  @Id @GeneratedValue private Long id;
  private String severity;
  private String location;
  private String description;
  @ManyToMany private List<Employee> involvedEmployees;
  private String status;
}

@RestController
@RequestMapping("/safety")
public class SafetyController {
  @PostMapping("/incidents") public SafetyIncident reportIncident(@RequestBody SafetyIncidentDTO dto) { ... }
  @GetMapping("/reports") public List<SafetyReportDTO> getReports(...) { ... }
}
```

---

<a name="e09"></a>
## E09 - Equipment & Asset Assignment

Section: Asset Domain
Description: Manages assignment, checkout/return, and condition of assets.
Design Specification:
- Entities: `Asset`, `AssetAssignment`, `AssetCondition`
- Service: `AssetService` (CRUD, check-in/out, certification checks)
- Controller: `AssetController`
  - CRUD endpoints
  - POST `/assets/checkout`
  - POST `/assets/return`
- History log and overdue reports

Sample Implementation:
```java
@Entity
public class Asset {
  @Id @GeneratedValue private Long id;
  private String type; // Scanner, Forklift, PPE
  private String serialNumber;
  private String condition;
}

@Entity
public class AssetAssignment {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Asset asset;
  @ManyToOne private Employee employee;
  private LocalDateTime checkoutTime;
  private LocalDateTime returnTime;
}

@RestController
@RequestMapping("/assets")
public class AssetController {
  @PostMapping("/checkout") public AssetAssignment checkout(@RequestBody AssetCheckoutDTO dto) { ... }
  @PostMapping("/return") public AssetAssignment returnAsset(@RequestBody AssetReturnDTO dto) { ... }
}
```

---

<a name="e10"></a>
## E10 - Performance Reviews & Goals

Section: Performance Domain
Description: Manages review templates, goals, ratings, and acknowledgements.
Design Specification:
- Entities: `PerformanceReview`, `ReviewTemplate`, `Goal`
- Service: `PerformanceService` (create cycles, assign, submit, acknowledge)
- Controller: `PerformanceController`
  - CRUD endpoints
  - POST `/reviews/acknowledge`
- PDF export and immutable history

Sample Implementation:
```java
@Entity
public class PerformanceReview {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  @ManyToOne private ReviewTemplate template;
  private LocalDate reviewDate;
  private String status; // DRAFT, SUBMITTED, ACKNOWLEDGED
  private String comments;
}

@RestController
@RequestMapping("/reviews")
public class PerformanceController {
  @PostMapping public PerformanceReview createReview(@RequestBody PerformanceReviewDTO dto) { ... }
  @PostMapping("/acknowledge") public PerformanceReview acknowledge(@RequestBody AcknowledgeDTO dto) { ... }
}
```

---

<a name="e11"></a>
## E11 - Payroll Export Integration

Section: Payroll Integration
Description: Generates payroll-ready files and delivers securely to providers.
Design Specification:
- Service: `PayrollExportService` (generate, map, deliver, retry)
- Integration: SFTP/API delivery
- Audit log for exports

Sample Implementation:
```java
@Service
public class PayrollExportService {
  public File generatePayrollFile(LocalDate period) { ... }
  public void deliver(File file) { ... }
  // Retry logic, audit logging
}
```

---

<a name="e12"></a>
## E12 - Notifications & Announcements

Section: Notification Domain
Description: Sends in-app, email, and SMS notifications with localization and quiet hours.
Design Specification:
- Entities: `Notification`, `Announcement`
- Service: `NotificationService` (send, track, rate limit)
- Controller: `NotificationController`
  - POST `/notifications/subscribe`
  - GET `/announcements`
- Channel opt-in/out, delivery status, templates

Sample Implementation:
```java
@Entity
public class Notification {
  @Id @GeneratedValue private Long id;
  private String channel; // EMAIL, SMS, IN_APP
  private String message;
  private boolean delivered;
  private LocalDateTime sentAt;
}

@RestController
@RequestMapping("/notifications")
public class NotificationController {
  @PostMapping("/subscribe") public void subscribe(@RequestBody SubscriptionDTO dto) { ... }
  @GetMapping("/announcements") public List<AnnouncementDTO> getAnnouncements() { ... }
}
```

---

<a name="e13"></a>
## E13 - Integration Layer (HRIS/WMS APIs)

Section: Integration APIs
Description: Exposes and consumes APIs for HRIS, WMS, and SSO.
Design Specification:
- REST APIs with JWT/OAuth2 security
- HRIS sync job for employee data
- WMS connectors for department/location
- Webhooks for events
- OpenAPI documentation

Sample Implementation:
```java
@RestController
@RequestMapping("/api/hris")
public class HRISController {
  @PostMapping("/employees") public void syncEmployee(@RequestBody EmployeeDTO dto) { ... }
}

@Component
public class HRISSyncJob {
  @Scheduled(cron = "0 0 * * * *") public void sync() { ... }
}
```

---

<a name="e14"></a>
## E14 - Audit Trail & Compliance

Section: Audit Logging
Description: Centralizes audit logs for sensitive changes with tamper-evident storage.
Design Specification:
- Entity: `AuditLog` (actor, timestamp, entity, before/after, action)
- Service: `AuditService` (log, export, validate)
- Immutable log table

Sample Implementation:
```java
@Entity
public class AuditLog {
  @Id @GeneratedValue private Long id;
  private String actor;
  private LocalDateTime timestamp;
  private String entity;
  private String action;
  @Lob private String before;
  @Lob private String after;
}

@Service
public class AuditService {
  public void logChange(String actor, String entity, String action, Object before, Object after) { ... }
}
```

---

<a name="e15"></a>
## E15 - Reporting & Analytics

Section: Reporting Domain
Description: Provides operational reports, exports, and dashboards.
Design Specification:
- Service: `ReportingService` (attendance, overtime, leave, certifications, safety KPIs)
- Controller: `ReportingController`
  - GET `/reports/attendance`
  - GET `/reports/overtime`
  - GET `/reports/leave`
  - GET `/reports/certifications`
  - GET `/reports/safety`
- CSV/PDF export, role-based access

Sample Implementation:
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
  @GetMapping("/attendance") public List<AttendanceReportDTO> getAttendanceReport(...) { ... }
  @GetMapping("/overtime") public List<OvertimeReportDTO> getOvertimeReport(...) { ... }
}
```

---

<a name="e16"></a>
## E16 - Mobile Access (PWA)

Section: PWA Configuration
Description: Enables mobile access with responsive views and offline support.
Design Specification:
- Responsive UI for core flows
- PWA manifest and service worker
- Offline queue for clock events
- Lighthouse PWA score >90

Sample Implementation:
```javascript
// manifest.json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone",
  "icons": [...]
}

// service-worker.js
self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request).then(response => response || fetch(event.request))
  );
});
```

---

<a name="e17"></a>
## E17 - Onboarding & Offboarding Workflow

Section: Lifecycle Workflow
Description: Automates provisioning and deprovisioning of accounts and assets.
Design Specification:
- Service: `OnboardingService`, `OffboardingService`
- Triggered by HRIS sync
- Tasks: account creation, training assignment, asset collection, schedule updates

Sample Implementation:
```java
@Service
public class OnboardingService {
  public void onboard(Employee employee) {
    // Create account, assign training, schedule initial shifts
  }
}

@Service
public class OffboardingService {
  public void offboard(Employee employee) {
    // Revoke access, collect assets, update schedules
  }
}
```

---

<a name="e18"></a>
## E18 - Localization & Multi-Tenant

Section: Multi-Tenancy & Localization
Description: Supports multiple warehouses with data isolation and UI localization.
Design Specification:
- Tenant ID in all queries
- Row-level isolation
- i18n for English/Spanish
- Timezone-aware scheduling

Sample Implementation:
```java
@Entity
public class Employee {
  @Column(nullable = false) private String tenantId;
  // ...
}

@Component
public class TenantFilter implements Filter {
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
    // Set tenant context
  }
}

// messages_en.properties, messages_es.properties
```

---

<a name="e19"></a>
## E19 - Observability & Monitoring

Section: Observability Configuration
Description: Implements structured logging, metrics, tracing, and alerts.
Design Specification:
- Structured JSON logs with traceId
- Prometheus metrics export
- OpenTelemetry distributed tracing
- Health checks for dependencies
- Alerts for critical failures

Sample Implementation:
```java
// logback-spring.xml
<appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
  <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
</appender>

// application.properties
management.metrics.export.prometheus.enabled=true
management.tracing.sampling.probability=1.0
```

---

<a name="e20"></a>
## E20 - Deployment & CI/CD

Section: Deployment Configuration
Description: Dockerizes the app and sets up CI/CD pipeline with Kubernetes.
Design Specification:
- Dockerfile with multi-stage build
- Kubernetes manifests: Deployment, Service, ConfigMap, Secret
- GitHub Actions pipeline: build, test, security scan, deploy
- Zero-downtime rolling updates
- Rollback procedure

Sample Implementation:
```dockerfile
# Dockerfile
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: warehouse-ems
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
  template:
    spec:
      containers:
      - name: ems
        image: warehouse-ems:latest
        ports:
        - containerPort: 8080
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
```

```yaml
# .github/workflows/ci-cd.yml
name: CI/CD
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
      run: mvn clean package
    - name: Build Docker image
      run: docker build -t warehouse-ems:latest .
    - name: Push to registry
      run: docker push warehouse-ems:latest
    - name: Deploy to Kubernetes
      run: kubectl apply -f k8s/
```

---

## Conclusion

This low-level technical design document provides a comprehensive blueprint for implementing the Warehouse Employee Management System using Spring Boot best practices. Each epic and user story has been systematically addressed with detailed architecture, domain models, service layers, controllers, and sample implementations. The design ensures modularity, scalability, security, and compliance with industry standards.