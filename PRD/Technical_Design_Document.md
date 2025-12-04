# Warehouse Employee Management Platform â Low-Level Technical Design Document

---

## Table of Contents

1. [Project Scaffolding & Domain Setup](#project-scaffolding--domain-setup)
2. [Employee Master Data (CRUD)](#employee-master-data-crud)
3. [Role-Based Access Control (RBAC)](#role-based-access-control-rbac)
4. [Time & Attendance (Clock In/Out)](#time--attendance-clock-inout)
5. [Shift & Schedule Management](#shift--schedule-management)
6. [Leave & Absence Management](#leave--absence-management)
7. [Training & Certification Tracking](#training--certification-tracking)
8. [Safety Incidents & OSHA Reporting](#safety-incidents--osha-reporting)
9. [Equipment & Asset Assignment](#equipment--asset-assignment)
10. [Performance Reviews & Goals](#performance-reviews--goals)
11. [Payroll Export Integration](#payroll-export-integration)
12. [Notifications & Announcements](#notifications--announcements)
13. [Integration Layer (HRIS/WMS APIs)](#integration-layer-hriswms-apis)
14. [Audit Trail & Compliance](#audit-trail--compliance)
15. [Reporting & Analytics](#reporting--analytics)
16. [Mobile Access (PWA)](#mobile-access-pwa)
17. [Onboarding & Offboarding Workflow](#onboarding--offboarding-workflow)
18. [Localization & Multi-Tenant](#localization--multi-tenant)
19. [Observability & Monitoring](#observability--monitoring)
20. [CI/CD & Deployment Automation](#cicd--deployment-automation)

---

## Section: Project Scaffolding & Domain Setup

**Description:**  
Establishes the foundational Spring Boot project structure, base packages, and core modules. Integrates Flyway/Liquibase for DB migrations and enables Actuator for health monitoring.

**Design Specification:**
- Spring Boot (Maven) initialization
- Base package: `com.warehousemgmt`
- Modules: `employee`, `scheduling`, `attendance`, `safety`
- DB migration: Flyway/Liquibase
- Monitoring: Spring Boot Actuator

**Sample Implementation:**
```bash
mvn archetype:generate -DgroupId=com.warehousemgmt -DartifactId=warehouse-employee-mgmt
```
```java
// Main Application
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}
```
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse
    username: warehouse_user
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

## Section: Employee Master Data (CRUD)

**Description:**  
Defines the Employee domain, CRUD APIs, and web DTOs. Enforces unique badgeId, supports soft-delete, pagination, and filtering.

**Design Specification:**
- Package: `com.warehousemgmt.employee`
- Entity: `Employee`
- Fields: `id`, `name`, `badgeId`, `role`, `department`, `shiftGroup`, `hireDate`, `status`, `deleted`
- Repository: `EmployeeRepository extends JpaRepository`
- Service: `EmployeeService`
- Controller: `EmployeeController`
- DTOs: `EmployeeRequestDto`, `EmployeeResponseDto`
- Unique constraint on `badgeId`
- Soft-delete via `deleted` boolean
- Pagination via Spring Data

**Sample Implementation:**
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badgeId"))
public class Employee {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String badgeId;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    private boolean deleted;
    // getters/setters
}
```
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeResponseDto> createEmployee(@Valid @RequestBody EmployeeRequestDto dto) { ... }
    @GetMapping
    public Page<EmployeeResponseDto> listEmployees(Pageable pageable) { ... }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteEmployee(@PathVariable Long id) { ... }
}
```

---

## Section: Role-Based Access Control (RBAC)

**Description:**  
Implements Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER), method/endpoint security, and API key/OAuth2 toggle.

**Design Specification:**
- Package: `com.warehousemgmt.security`
- Roles: Enum `Role { ADMIN, HR, SUPERVISOR, WORKER }`
- SecurityConfig: Spring Security configuration
- API key/OAuth2 toggle via config
- Method-level security: `@PreAuthorize`
- Endpoint-level security: `antMatchers`

**Sample Implementation:**
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
            .oauth2Login()
            .and()
            .httpBasic();
    }
}
```
```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteEmployee(Long id) { ... }
```

---

## Section: Time & Attendance (Clock In/Out)

**Description:**  
Endpoints for clock-in/out events with geofence and device capture. Calculates hours worked per shift and handles missed punches.

**Design Specification:**
- Package: `com.warehousemgmt.attendance`
- Entity: `AttendanceEvent`
- Fields: `id`, `employeeId`, `timestamp`, `type`, `deviceId`, `location`, `shiftId`, `approved`
- Repository: `AttendanceRepository`
- Service: `AttendanceService`
- Controller: `AttendanceController`
- Geofence validation
- Correction workflow

**Sample Implementation:**
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private LocalDateTime timestamp;
    @Enumerated(EnumType.STRING)
    private EventType type; // CLOCK_IN, CLOCK_OUT
    private String deviceId;
    private String location;
    private Long shiftId;
    private boolean approved;
}
```
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockEventDto dto) { ... }
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockEventDto dto) { ... }
}
```

---

## Section: Shift & Schedule Management

**Description:**  
Manages recurring shift templates, rotations, overtime rules, employee assignments, blackout dates, and operation calendars.

**Design Specification:**
- Package: `com.warehousemgmt.scheduling`
- Entities: `ShiftTemplate`, `ShiftAssignment`, `BlackoutDate`
- Repository: `ShiftTemplateRepository`, `ShiftAssignmentRepository`
- Service: `SchedulingService`
- Controller: `SchedulingController`
- Conflict detection logic
- Bulk assignment endpoints

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
    private String rotationPattern;
}
```
```java
@RestController
@RequestMapping("/scheduling")
public class SchedulingController {
    @PostMapping("/templates")
    public ResponseEntity<ShiftTemplateDto> createTemplate(@RequestBody ShiftTemplateDto dto) { ... }
    @PostMapping("/assignments/bulk")
    public ResponseEntity<?> bulkAssign(@RequestBody BulkAssignmentDto dto) { ... }
}
```

---

## Section: Leave & Absence Management

**Description:**  
Handles PTO, sick, unpaid leave requests/approvals, accrual balances, and integration with scheduling/payroll.

**Design Specification:**
- Package: `com.warehousemgmt.leave`
- Entity: `LeaveRequest`
- Fields: `id`, `employeeId`, `type`, `startDate`, `endDate`, `status`, `balance`
- Repository: `LeaveRepository`
- Service: `LeaveService`
- Controller: `LeaveController`
- Accrual calculation logic

**Sample Implementation:**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    @Enumerated(EnumType.STRING)
    private LeaveType type; // PTO, SICK, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status; // REQUESTED, APPROVED, DENIED
    private int balance;
}
```
```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/request")
    public ResponseEntity<LeaveResponseDto> requestLeave(@RequestBody LeaveRequestDto dto) { ... }
    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approveLeave(@PathVariable Long id) { ... }
}
```

---

## Section: Training & Certification Tracking

**Description:**  
Tracks certifications, expirations, renewals, blocks assignments for expired certs, and uploads proof documents.

**Design Specification:**
- Package: `com.warehousemgmt.certification`
- Entity: `Certification`
- Fields: `id`, `employeeId`, `type`, `expiryDate`, `documentUrl`, `status`
- Repository: `CertificationRepository`
- Service: `CertificationService`
- Controller: `CertificationController`
- Expiry alert logic

**Sample Implementation:**
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type;
    private LocalDate expiryDate;
    private String documentUrl;
    @Enumerated(EnumType.STRING)
    private CertificationStatus status; // VALID, EXPIRED
}
```
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<CertificationDto> addCertification(@RequestBody CertificationDto dto) { ... }
    @GetMapping("/alerts")
    public List<CertificationAlertDto> getExpiringCerts() { ... }
}
```

---

## Section: Safety Incidents & OSHA Reporting

**Description:**  
Records incidents/near-misses, severity, location, description, involved employees, investigation workflow, and OSHA reporting.

**Design Specification:**
- Package: `com.warehousemgmt.safety`
- Entity: `SafetyIncident`
- Fields: `id`, `description`, `severity`, `location`, `status`, `involvedEmployeeIds`, `oshasummary`
- Repository: `SafetyIncidentRepository`
- Service: `SafetyService`
- Controller: `SafetyController`
- Workflow status logic

**Sample Implementation:**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String description;
    @Enumerated(EnumType.STRING)
    private Severity severity;
    private String location;
    @Enumerated(EnumType.STRING)
    private IncidentStatus status; // OPEN, INVESTIGATING, RESOLVED
    @ElementCollection
    private List<Long> involvedEmployeeIds;
    private String oshasummary;
}
```
```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyController {
    @PostMapping
    public ResponseEntity<SafetyIncidentDto> reportIncident(@RequestBody SafetyIncidentDto dto) { ... }
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateDto dto) { ... }
}
```

---

## Section: Equipment & Asset Assignment

**Description:**  
Assigns scanners, forklifts, PPE to employees, tracks checkout/return, prevents use if certification missing, maintains asset condition.

**Design Specification:**
- Package: `com.warehousemgmt.asset`
- Entity: `Asset`, `AssetAssignment`
- Fields: `id`, `type`, `condition`, `assignedEmployeeId`, `checkoutDate`, `returnDate`
- Repository: `AssetRepository`, `AssetAssignmentRepository`
- Service: `AssetService`
- Controller: `AssetController`
- Certification check logic

**Sample Implementation:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String condition;
    private Long assignedEmployeeId;
    private LocalDate checkoutDate;
    private LocalDate returnDate;
}
```
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign")
    public ResponseEntity<?> assignAsset(@RequestBody AssetAssignmentDto dto) { ... }
    @PostMapping("/return")
    public ResponseEntity<?> returnAsset(@RequestBody AssetReturnDto dto) { ... }
}
```

---

## Section: Performance Reviews & Goals

**Description:**  
Creates review templates, tracks goals, competencies, ratings, comments, and supervisor/employee acknowledgements.

**Design Specification:**
- Package: `com.warehousemgmt.performance`
- Entity: `PerformanceReview`, `Goal`
- Fields: `id`, `employeeId`, `reviewPeriod`, `competencies`, `ratings`, `comments`, `acknowledged`
- Repository: `PerformanceReviewRepository`
- Service: `PerformanceService`
- Controller: `PerformanceController`
- PDF export logic

**Sample Implementation:**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String reviewPeriod;
    @ElementCollection
    private Map<String, Integer> competencies;
    private String comments;
    private boolean acknowledged;
}
```
```java
@RestController
@RequestMapping("/performance/reviews")
public class PerformanceController {
    @PostMapping
    public ResponseEntity<PerformanceReviewDto> createReview(@RequestBody PerformanceReviewDto dto) { ... }
    @GetMapping("/export/{id}")
    public ResponseEntity<Resource> exportReviewPdf(@PathVariable Long id) { ... }
}
```

---

## Section: Payroll Export Integration

**Description:**  
Generates payroll-ready files from attendance and leave, maps to external formats, and delivers securely via SFTP/API.

**Design Specification:**
- Package: `com.warehousemgmt.payroll`
- Entity: `PayrollExport`
- Fields: `id`, `period`, `fileUrl`, `status`, `attempts`
- Repository: `PayrollExportRepository`
- Service: `PayrollService`
- Controller: `PayrollController`
- SFTP/API integration logic

**Sample Implementation:**
```java
@Entity
public class PayrollExport {
    @Id @GeneratedValue
    private Long id;
    private String period;
    private String fileUrl;
    @Enumerated(EnumType.STRING)
    private ExportStatus status; // PENDING, SUCCESS, FAILED
    private int attempts;
}
```
```java
@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/export")
    public ResponseEntity<PayrollExportDto> exportPayroll(@RequestBody PayrollExportRequestDto dto) { ... }
}
```

---

## Section: Notifications & Announcements

**Description:**  
Sends in-app, email, and SMS notifications for shift changes, expiring certs, approvals, and announcements. Supports quiet hours.

**Design Specification:**
- Package: `com.warehousemgmt.notification`
- Entity: `Notification`, `Announcement`
- Fields: `id`, `recipientId`, `type`, `channel`, `content`, `status`, `timestamp`
- Repository: `NotificationRepository`
- Service: `NotificationService`
- Controller: `NotificationController`
- Channel opt-in/out logic

**Sample Implementation:**
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private Long recipientId;
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    @Enumerated(EnumType.STRING)
    private Channel channel; // IN_APP, EMAIL, SMS
    private String content;
    @Enumerated(EnumType.STRING)
    private NotificationStatus status; // SENT, FAILED
    private LocalDateTime timestamp;
}
```
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping
    public ResponseEntity<?> sendNotification(@RequestBody NotificationDto dto) { ... }
    @GetMapping("/announcements")
    public List<AnnouncementDto> getAnnouncements() { ... }
}
```

---

## Section: Integration Layer (HRIS/WMS APIs)

**Description:**  
Exposes REST APIs and connectors for HRIS (new hires/terms), WMS (location/department), and IDP for SSO. Supports webhooks.

**Design Specification:**
- Package: `com.warehousemgmt.integration`
- Entity: `IntegrationEvent`
- Fields: `id`, `source`, `eventType`, `payload`, `timestamp`
- Repository: `IntegrationEventRepository`
- Service: `IntegrationService`
- Controller: `IntegrationController`
- JWT/OAuth2 security
- Webhook endpoints

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    @PostMapping("/hris/webhook")
    public ResponseEntity<?> handleHrisEvent(@RequestBody HrisEventDto dto) { ... }
    @PostMapping("/wms/webhook")
    public ResponseEntity<?> handleWmsEvent(@RequestBody WmsEventDto dto) { ... }
}
```
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://idp.example.com
```

---

## Section: Audit Trail &  Compliance

**Description:**  
Centralized audit logging for sensitive changes (employee PII, schedules, approvals, payroll). Tamper-evident storage.

**Design Specification:**
- Package: `com.warehousemgmt.audit`
- Entity: `AuditLog`
- Fields: `id`, `actor`, `timestamp`, `entity`, `action`, `before`, `after`
- Repository: `AuditLogRepository`
- Service: `AuditService`
- Immutable log table

**Sample Implementation:**
```java
@Entity
@Immutable
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    private String action;
    @Column(columnDefinition = "TEXT")
    private String before;
    @Column(columnDefinition = "TEXT")
    private String after;
}
```
```java
@Aspect
@Component
public class AuditAspect {
    @AfterReturning("@annotation(Auditable)")
    public void logAudit(JoinPoint joinPoint) { ... }
}
```

---

## Section: Reporting & Analytics

**Description:**  
Operational reports for attendance, overtime, leave, certifications, safety KPIs. CSV/PDF export with role-based access.

**Design Specification:**
- Package: `com.warehousemgmt.reporting`
- Service: `ReportingService`
- Controller: `ReportingController`
- Export formats: CSV, PDF
- Filtering by date, department, shift

**Sample Implementation:**
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> exportAttendanceReport(@RequestParam String format) { ... }
    @GetMapping("/safety-kpis")
    public ResponseEntity<SafetyKpiDto> getSafetyKpis() { ... }
}
```

---

## Section: Mobile Access (PWA)

**Description:**  
Responsive PWA for workers to clock-in/out, view schedules, request leave, see announcements. Offline-friendly.

**Design Specification:**
- Package: `com.warehousemgmt.mobile`
- PWA manifest
- Service worker for offline support
- Responsive UI components

**Sample Implementation:**
```json
{
  "name": "Warehouse Employee Mgmt",
  "short_name": "WarehouseMgmt",
  "start_url": "/",
  "display": "standalone",
  "icons": [...]
}
```
```javascript
// Service Worker
self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request).then(response => response || fetch(event.request))
  );
});
```

---

## Section: Onboarding & Offboarding Workflow

**Description:**  
Automates provisioning of accounts, schedules, training, and deprovisions access/assets on termination.

**Design Specification:**
- Package: `com.warehousemgmt.lifecycle`
- Entity: `OnboardingTask`, `OffboardingTask`
- Service: `LifecycleService`
- Controller: `LifecycleController`
- Task generation logic

**Sample Implementation:**
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String taskType;
    private boolean completed;
}
```
```java
@RestController
@RequestMapping("/lifecycle")
public class LifecycleController {
    @PostMapping("/onboard")
    public ResponseEntity<?> onboardEmployee(@RequestBody OnboardingDto dto) { ... }
    @PostMapping("/offboard")
    public ResponseEntity<?> offboardEmployee(@RequestBody OffboardingDto dto) { ... }
}
```

---

## Section: Localization & Multi-Tenant

**Description:**  
Tenant ID in all queries for data isolation. Locale-aware formatting and translations. Timezone handling.

**Design Specification:**
- Package: `com.warehousemgmt.tenant`
- Entity: `Tenant`
- Tenant context filter
- Locale resolver
- Timezone configuration

**Sample Implementation:**
```java
@Component
public class TenantFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        String tenantId = request.getHeader("X-Tenant-ID");
        TenantContext.setCurrentTenant(tenantId);
        chain.doFilter(request, response);
    }
}
```
```yaml
spring:
  messages:
    basename: i18n/messages
```

---

## Section: Observability & Monitoring

**Description:**  
Structured logging with TraceID, Prometheus metrics export, health checks, and alerting rules.

**Design Specification:**
- Package: `com.warehousemgmt.observability`
- Logging: Logback with MDC
- Metrics: Micrometer + Prometheus
- Health checks: Actuator

**Sample Implementation:**
```java
@Component
public class LoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        MDC.put("traceId", UUID.randomUUID().toString());
        chain.doFilter(request, response);
        MDC.clear();
    }
}
```
```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
```

---

## Section: CI/CD & Deployment Automation

**Description:**  
GitHub Actions pipeline for build/test/scan, Docker image push with commit SHA tagging, automated staging deploy, and prod approval workflow.

**Design Specification:**
- CI/CD: GitHub Actions
- Docker image tagging
- Staging/Prod deployment

**Sample Implementation:**
```yaml
name: CI/CD Pipeline
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build
        run: mvn clean package
      - name: Docker Build
        run: docker build -t warehouse-mgmt:${{ github.sha }} .
      - name: Push to Registry
        run: docker push warehouse-mgmt:${{ github.sha }}
```

---

**End of Document**