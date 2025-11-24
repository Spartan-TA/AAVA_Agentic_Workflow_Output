# Technical Design Document: Warehouse Employee Management System (Spring Boot)

---

## Section: E01 - Project Scaffolding & Domain Setup

**Description:**  
Establishes the foundational Spring Boot project structure, base packages, and core modules. Integrates Flyway/Liquibase for DB migrations and enables Actuator for health monitoring.

**Design Specification:**  
- Spring Boot (Maven) project with layered architecture: `controller`, `service`, `repository`, `domain`, `dto`, `config`
- Core modules: `employee`, `scheduling`, `attendance`, `safety`
- Flyway/Liquibase for DB migrations
- Actuator enabled for health endpoints
- Base package: `com.warehousemgmt`
- Application runs on port 8080

**Sample Implementation:**
```java
// src/main/java/com/warehousemgmt/WarehouseEmployeeMgmtApplication.java
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}

// src/main/resources/application.yml
server:
  port: 8080

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

## Section: E02 - Employee Master Data (CRUD)

**Description:**  
Implements CRUD operations for employee records, enforcing unique badge IDs and supporting soft deletes, pagination, and filtering.

**Design Specification:**  
- Entity: `Employee` (fields: id, name, badgeId, role, department, shiftGroup, hireDate, status)
- Repository: `EmployeeRepository` extends `JpaRepository`
- Service: `EmployeeService` for business logic
- Controller: `EmployeeController` exposes REST endpoints
- DTOs for API contracts
- Validation annotations for input
- OpenAPI documentation

**Sample Implementation:**
```java
// domain/Employee.java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    @NotBlank
    private String name;
    @Column(unique = true)
    private String badgeId;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private EmployeeStatus status;
    private boolean deleted = false;
}

// repository/EmployeeRepository.java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}

// service/EmployeeService.java
@Service
public class EmployeeService {
    // CRUD methods, soft delete implementation
}

// controller/EmployeeController.java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeDTO dto) { ... }
    @GetMapping
    public Page<EmployeeDTO> listEmployees(Pageable pageable, @RequestParam Map<String, String> filters) { ... }
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable Long id, @RequestBody EmployeeDTO dto) { ... }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) { ... }
}
```

---

## Section: E03 - Role-Based Access Control (RBAC)

**Description:**  
Secures endpoints and methods using Spring Security, supporting roles (ADMIN, HR, SUPERVISOR, WORKER) and toggling between API key/OAuth2 authentication.

**Design Specification:**  
- Security config: `SecurityConfig` with role-based access
- Method-level security with `@PreAuthorize`
- API key/OAuth2 toggle via config
- Custom `UserDetailsService`
- Security tests for coverage

**Sample Implementation:**
```java
// config/SecurityConfig.java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${security.mode}")
    private String securityMode; // "API_KEY" or "OAUTH2"

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if ("API_KEY".equals(securityMode)) {
            http.authorizeRequests()
                .antMatchers("/employees/**").hasRole("ADMIN")
                .antMatchers("/attendance/**").hasAnyRole("SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
                .and().addFilter(new ApiKeyAuthFilter());
        } else {
            http.oauth2Login().and()
                .authorizeRequests()
                .antMatchers("/employees/**").hasRole("ADMIN")
                .antMatchers("/attendance/**").hasAnyRole("SUPERVISOR", "WORKER")
                .anyRequest().authenticated();
        }
    }
}

// Example method security
@PreAuthorize("hasRole('ADMIN')")
public void deleteEmployee(Long id) { ... }
```

---

## Section: E04 - Time & Attendance (Clock In/Out)

**Description:**  
Handles clock-in/out events, geofencing, device capture, shift association, missed punch corrections, and reporting.

**Design Specification:**  
- Entity: `AttendanceEvent` (employeeId, timestamp, type, deviceId, location)
- Service: `AttendanceService` for event processing
- Controller: `AttendanceController` with endpoints for clock-in/out
- Geofence validation logic
- Correction workflow with approval tasks
- CSV export endpoint

**Sample Implementation:**
```java
// domain/AttendanceEvent.java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private LocalDateTime timestamp;
    @Enumerated(EnumType.STRING)
    private AttendanceType type; // CLOCK_IN, CLOCK_OUT
    private String deviceId;
    private GeoLocation location;
}

// controller/AttendanceController.java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockEventDTO dto) { ... }
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockEventDTO dto) { ... }
    @GetMapping("/report")
    public ResponseEntity<Resource> exportAttendanceReport(@RequestParam LocalDate date) { ... }
}
```

---

## Section: E05 - Shift & Schedule Management

**Description:**  
Manages shift templates, rotations, overtime rules, blackout dates, and employee assignments.

**Design Specification:**  
- Entity: `ShiftTemplate`, `ShiftAssignment`
- Service: `ShiftService` for scheduling logic
- Controller: `ShiftController` for CRUD and bulk assignment
- Conflict detection logic
- Audit entries for changes

**Sample Implementation:**
```java
// domain/ShiftTemplate.java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    private Set<DayOfWeek> days;
}

// controller/ShiftController.java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping
    public ResponseEntity<ShiftTemplateDTO> createShiftTemplate(@RequestBody ShiftTemplateDTO dto) { ... }
    @PostMapping("/assign")
    public ResponseEntity<?> bulkAssign(@RequestBody BulkAssignDTO dto) { ... }
}
```

---

## Section: E06 - Leave & Absence Management

**Description:**  
Supports leave requests, approvals, accrual balances, and integration with scheduling/payroll.

**Design Specification:**  
- Entity: `LeaveRequest`, `LeaveBalance`
- Service: `LeaveService` for request/approval logic
- Controller: `LeaveController`
- Integration hooks for scheduling/payroll
- Scheduled jobs for accrual updates

**Sample Implementation:**
```java
// domain/LeaveRequest.java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveStatus status;
    private String approver;
}

// controller/LeaveController.java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/request")
    public ResponseEntity<LeaveRequestDTO> requestLeave(@RequestBody LeaveRequestDTO dto) { ... }
    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approveLeave(@PathVariable Long id) { ... }
}
```

---

## Section: E07 - Training & Certification Tracking

**Description:**  
Tracks employee certifications, expirations, renewals, and blocks assignments for expired certifications.

**Design Specification:**  
- Entity: `Certification`, `EmployeeCertification`
- Service: `CertificationService`
- Controller: `CertificationController`
- Alerts for expiring certifications
- Document upload for proof

**Sample Implementation:**
```java
// domain/Certification.java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalDate expiryDate;
    private String documentUrl;
}

// controller/CertificationController.java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<CertificationDTO> addCertification(@RequestBody CertificationDTO dto) { ... }
    @GetMapping("/alerts")
    public List<CertificationAlertDTO> getExpiringCerts() { ... }
}
```

---

## Section: E08 - Safety Incidents & OSHA Reporting

**Description:**  
Records safety incidents, manages investigation workflow, and generates OSHA reports.

**Design Specification:**  
- Entity: `SafetyIncident`
- Service: `SafetyService`
- Controller: `SafetyController`
- Workflow status management
- OSHA report export

**Sample Implementation:**
```java
// domain/SafetyIncident.java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String description;
    private IncidentSeverity severity;
    private String location;
    private List<Long> involvedEmployeeIds;
    private IncidentStatus status;
}

// controller/SafetyController.java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyController {
    @PostMapping
    public ResponseEntity<SafetyIncidentDTO> reportIncident(@RequestBody SafetyIncidentDTO dto) { ... }
    @GetMapping("/oshasummary")
    public ResponseEntity<Resource> exportOSHAReport() { ... }
}
```

---

## Section: E09 - Equipment & Asset Assignment

**Description:**  
Assigns assets to employees, tracks check-in/out, and enforces certification requirements.

**Design Specification:**  
- Entity: `Asset`, `AssetAssignment`
- Service: `AssetService`
- Controller: `AssetController`
- Certification validation logic
- Asset condition tracking

**Sample Implementation:**
```java
// domain/Asset.java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private AssetCondition condition;
}

// controller/AssetController.java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/assign")
    public ResponseEntity<?> assignAsset(@RequestBody AssetAssignmentDTO dto) { ... }
    @PostMapping("/checkin")
    public ResponseEntity<?> checkInAsset(@RequestBody AssetAssignmentDTO dto) { ... }
}
```

---

## Section: E10 - Performance Reviews & Goals

**Description:**  
Manages review cycles, goals, competencies, ratings, and immutable history after sign-off.

**Design Specification:**  
- Entity: `PerformanceReview`, `Goal`
- Service: `ReviewService`
- Controller: `ReviewController`
- PDF export endpoint
- Role-based visibility

**Sample Implementation:**
```java
// domain/PerformanceReview.java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private ReviewCycle cycle;
    private List<Goal> goals;
    private ReviewStatus status;
    private String supervisorComments;
}

// controller/ReviewController.java
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping
    public ResponseEntity<PerformanceReviewDTO> createReview(@RequestBody PerformanceReviewDTO dto) { ... }
    @GetMapping("/export/{id}")
    public ResponseEntity<Resource> exportReviewPdf(@PathVariable Long id) { ... }
}
```

---

## Section: E11 - Payroll Export Integration

**Description:**  
Generates payroll-ready files from attendance and leave, mapping to provider formats and secure delivery.

**Design Specification:**  
- Service: `PayrollExportService`
- Controller: `PayrollController`
- SFTP/API integration for delivery
- Audit log for exports
- Retry logic for failed deliveries

**Sample Implementation:**
```java
// service/PayrollExportService.java
@Service
public class PayrollExportService {
    public Resource generatePayrollFile(LocalDate period) { ... }
    public void deliverPayrollFile(Resource file) { ... }
}

// controller/PayrollController.java
@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @GetMapping("/export")
    public ResponseEntity<Resource> exportPayroll(@RequestParam LocalDate period) { ... }
}
```

---

## Section: E12 - Notifications & Announcements

**Description:**  
Delivers notifications via in-app, email, SMS; supports opt-in/out, templates, and quiet hours.

**Design Specification:**  
- Entity: `Notification`, `Announcement`
- Service: `NotificationService`
- Controller: `NotificationController`
- Channel management
- Delivery status tracking

**Sample Implementation:**
```java
// domain/Notification.java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private Long recipientId;
    private NotificationType type;
    private String message;
    private NotificationChannel channel;
    private NotificationStatus status;
}

// controller/NotificationController.java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationDTO dto) { ... }
}
```

---

## Section: E13 - Integration Layer (HRIS/WMS APIs)

**Description:**  
Exposes REST APIs and connectors for HRIS, WMS, and IDP; supports webhooks and SSO.

**Design Specification:**  
- REST endpoints for HRIS/WMS sync
- JWT/OAuth2 security
- Webhook event handling
- OpenAPI documentation

**Sample Implementation:**
```java
// controller/IntegrationController.java
@RestController
@RequestMapping("/integration")
public class IntegrationController {
    @PostMapping("/hris/sync")
    public ResponseEntity<?> syncHris(@RequestBody HrisSyncDTO dto) { ... }
    @PostMapping("/wms/link")
    public ResponseEntity<?> linkWms(@RequestBody WmsLinkDTO dto) { ... }
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody WebhookEventDTO dto) { ... }
}
```

---

## Section: E14 - Audit Trail & Compliance

**Description:**  
Centralizes audit logging for sensitive changes, with tamper-evident storage and export capabilities.

**Design Specification:**  
- Entity: `AuditLog`
- Service: `AuditService`
- Controller: `AuditController`
- Immutable log table
- Export endpoints

**Sample Implementation:**
```java
// domain/AuditLog.java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private Long entityId;
    private String actor;
    private LocalDateTime timestamp;
    private String before;
    private String after;
}

// controller/AuditController.java
@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/export")
    public ResponseEntity<Resource> exportAuditLogs(@RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
}
```

---

## Section: E15 - Reporting & Analytics

**Description:**  
Provides operational reports and dashboards for attendance, overtime, leave, certifications, safety KPIs.

**Design Specification:**  
- Service: `ReportingService`
- Controller: `ReportingController`
- CSV/PDF export endpoints
- Role-based dashboard access

**Sample Implementation:**
```java
// controller/ReportingController.java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> attendanceReport(@RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
    @GetMapping("/safety")
    public ResponseEntity<Resource> safetyKpiReport(@RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
}
```

---

## Section: E16 - Mobile Access (PWA)

**Description:**  
Enables responsive, offline-friendly mobile access for core workflows via PWA.

**Design Specification:**  
- PWA manifest and service worker
- Mobile-optimized views for clock-in/out, schedules, leave, announcements
- Offline queue for clock events
- Lighthouse score â¥ 80

**Sample Implementation:**
```javascript
// public/manifest.json
{
  "name": "Warehouse Employee Mgmt",
  "short_name": "WarehouseMgmt",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "icons": [ ... ]
}

// src/main/resources/static/service-worker.js
self.addEventListener('fetch', function(event) {
  // Offline queue logic
});
```

---

## Section: E17 - Onboarding & Offboarding Workflow

**Description:**  
Automates provisioning/deprovisioning of accounts, schedules, training, and asset assignments.

**Design Specification:**  
- Service: `OnboardingService`, `OffboardingService`
- Controller: `LifecycleController`
- HRIS integration for new hires
- Task generation for training/assets
- Access/asset revocation logic

**Sample Implementation:**
```java
// service/OnboardingService.java
@Service
public class OnboardingService {
    public void onboardEmployee(HrisEmployeeDTO dto) { ... }
}

// controller/LifecycleController.java
@RestController
@RequestMapping("/lifecycle")
public class LifecycleController {
    @PostMapping("/onboard")
    public ResponseEntity<?> onboard(@RequestBody HrisEmployeeDTO dto) { ... }
    @PostMapping("/offboard")
    public ResponseEntity<?> offboard(@RequestBody OffboardDTO dto) { ... }
}
```

---

## Section: E18 - Localization & Multi-Tenant

**Description:**  
Supports multiple warehouses/regions with tenant isolation and locale-specific settings.

**Design Specification:**  
- Tenant ID in all entities
- Tenant-aware repositories
- Locale configuration per tenant
- Timezone-aware timestamps

**Sample Implementation:**
```java
// domain/TenantAwareEntity.java
@MappedSuperclass
public abstract class TenantAwareEntity {
    @Column(nullable = false)
    private String tenantId;
}

// config/TenantConfig.java
@Configuration
public class TenantConfig {
    @Bean
    public TenantInterceptor tenantInterceptor() { ... }
}
```

---

## Section: E19 - Observability & Monitoring

**Description:**  
Integrates Prometheus/Grafana, structured logging, distributed tracing, and alerting.

**Design Specification:**  
- Actuator with Prometheus endpoint
- JSON structured logging
- Jaeger/Zipkin integration
- Alert rules for 5xx, DB latency

**Sample Implementation:**
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  level:
    root: INFO
```

---

## Section: E20 - CI/CD & Deployment Automation

**Description:**  
Automates build, test, security scan, and deployment via GitHub Actions.

**Design Specification:**  
- GitHub Actions workflow for PR and merge
- Docker image build and push
- Staging auto-deploy
- Production deploy with approval
- Rollback capability

**Sample Implementation:**
```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline
on:
  pull_request:
  push:
    branches: [main]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          java-version: '11'
      - name: Build with Maven
        run: mvn clean install
      - name: Security Scan
        run: mvn dependency-check:check
      - name: Build Docker Image
        run: docker build -t warehouse-mgmt:${{ github.sha }} .
      - name: Push to Registry
        run: docker push warehouse-mgmt:${{ github.sha }}
  deploy-staging:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Staging
        run: kubectl set image deployment/warehouse-mgmt warehouse-mgmt=warehouse-mgmt:${{ github.sha }}
  deploy-production:
    needs: deploy-staging
    runs-on: ubuntu-latest
    environment: production
    steps:
      - name: Deploy to Production
        run: kubectl set image deployment/warehouse-mgmt warehouse-mgmt=warehouse-mgmt:${{ github.sha }}
```

---

## Summary

This comprehensive low-level technical design document covers all 20 epics of the warehouse employee management system, following Spring Boot best practices. Each section includes:

- **Architecture Overview**: Layered architecture with clear separation of concerns
- **Package Structure**: Organized by domain modules (employee, scheduling, attendance, safety)
- **Entity Design**: JPA entities with relationships and validation
- **Service Layer**: Business logic encapsulation
- **Repository Layer**: Spring Data JPA repositories
- **Controller Layer**: RESTful endpoints with proper HTTP methods
- **Security**: Spring Security with role-based access control
- **Configuration**: Externalized configuration with profiles
- **Integration**: REST APIs, webhooks, SSO
- **Observability**: Actuator, Prometheus, structured logging
- **CI/CD**: Automated pipelines with GitHub Actions

The design ensures scalability, maintainability, security, and compliance with industry standards.