# Warehouse Employee Management System (EMS)
## Low-Level Technical Design Document

---

## Section: E01 - Project Scaffolding & Domain Setup

**Description:** Initialize the Spring Boot project with Maven, configure base packages, set up core modules (employee, scheduling, attendance, safety), add Flyway/Liquibase for DB migrations, and enable Actuator for monitoring.

**Design Specification:**
- Spring Boot (Maven) project with modular package structure:
  - com.warehouse.ems.employee
  - com.warehouse.ems.scheduling
  - com.warehouse.ems.attendance
  - com.warehouse.ems.safety
- Flyway/Liquibase for database migrations (src/main/resources/db/migration)
- Spring Boot Actuator enabled in application.properties
- Standard application.yml for environment configs

**Sample Implementation:**
```java
// pom.xml dependencies
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>

// application.properties
spring.datasource.url=jdbc:postgresql://localhost/warehouse_ems
spring.datasource.username=ems_user
spring.datasource.password=secret
management.endpoints.web.exposure.include=*

// Main Application
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

---

## Section: E02 - Employee Master Data (CRUD)

**Description:** Employee domain with CRUD APIs and DTOs. Fields: name, badgeId, role, department, shiftGroup, hireDate, status.

**Design Specification:**
- Entity: Employee
- Repository: EmployeeRepository (extends JpaRepository)
- Service: EmployeeService
- Controller: EmployeeController (REST endpoints)
- DTOs: EmployeeDto, EmployeeCreateDto, EmployeeUpdateDto

**Sample Implementation:**
```java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {}

@Service
public class EmployeeService {
    // CRUD methods
}

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    // CRUD endpoints
}
```

---

## Section: E03 - Role Based Access Control (RBAC)

**Description:** Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER), method/endpoint security, row-level constraints, API key/OAuth2 toggle.

**Design Specification:**
- SecurityConfig: configure roles and endpoint access
- CustomUserDetailsService for user loading
- Row-level security via @PreAuthorize
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
            .oauth2Login();
    }
}

@Service
public class EmployeeService {
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Employee updateEmployee(EmployeeDto dto) { ... }
}
```

---

## Section: E04 - Time & Attendance (Clock In/Out)

**Description:** Endpoints for clock-in/out events with geofence/device capture, calculate hours worked, handle missed punches/corrections.

**Design Specification:**
- Entity: AttendanceEvent (employee, timestamp, type, deviceId, location)
- Service: AttendanceService (clock-in/out, corrections)
- Controller: AttendanceController
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
    private String type; // CLOCK_IN, CLOCK_OUT
    private String deviceId;
    private String location;
}

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody AttendanceDto dto) { ... }
}
```

---

## Section: E05 - Shift & Schedule Management

**Description:** Recurring shift templates, rotations, overtime rules, assignment to employees, blackout dates, operation calendars.

**Design Specification:**
- Entities: ShiftTemplate, ShiftAssignment, BlackoutDate, OperationCalendar
- Service: ShiftService (template creation, assignment)
- Controller: ShiftController

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
}

@Entity
public class ShiftAssignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ShiftTemplate shiftTemplate;
    private LocalDate date;
}

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {
    // endpoints for templates, assignments, blackout dates
}
```

---

## Section: E06 - Leave & Absence Management

**Description:** Request/approve PTO, sick, unpaid leave; accrual balances, policies; integration hooks for scheduling/payroll.

**Design Specification:**
- Entities: LeaveRequest, LeavePolicy, LeaveBalance
- Service: LeaveService (request, approve, accrual)
- Controller: LeaveController
- Integration: hooks for scheduling/payroll modules

**Sample Implementation:**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type; // PTO, SICK, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // REQUESTED, APPROVED, REJECTED
}

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {
    // endpoints for leave requests, approvals
}
```

---

## Section: E07 - Training & Certification Tracking

**Description:** Track certifications (forklift), expirations, renewals; block assignment to tasks requiring expired certs; upload proof documents.

**Design Specification:**
- Entities: Certification, CertificationAssignment, ProofDocument
- Service: CertificationService (tracking, renewal)
- Controller: CertificationController
- File upload handling for proof documents

**Sample Implementation:**
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalDate expirationDate;
    @ManyToOne
    private Employee employee;
}

@RestController
@RequestMapping("/api/certifications")
public class CertificationController {
    @PostMapping("/upload-proof")
    public ResponseEntity<?> uploadProof(@RequestParam MultipartFile file) { ... }
}
```

---

## Section: E08 - Safety Incidents & OSHA Reporting

**Description:** Record incidents/near-misses; severity, location, description, involved employees; workflow for investigation/corrective actions; OSHA summary.

**Design Specification:**
- Entities: SafetyIncident, Investigation, CorrectiveAction
- Service: SafetyService (incident recording, reporting)
- Controller: SafetyController
- OSHA summary generation utility

**Sample Implementation:**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String severity;
    private String location;
    private String description;
    @ManyToMany
    private List<Employee> involvedEmployees;
    private LocalDateTime incidentDate;
}

@RestController
@RequestMapping("/api/safety")
public class SafetyController {
    // endpoints for incident reporting, investigation workflow
}
```

---

## Section: E09 - Equipment & Asset Assignment

**Description:** Assign scanners, forklifts, PPE to employees; track checkout/return; prevent use if certification missing; maintain asset condition.

**Design Specification:**
- Entities: Asset, AssetAssignment, AssetCondition
- Service: AssetService (assignment, condition tracking)
- Controller: AssetController
- Certification check before assignment

**Sample Implementation:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type; // SCANNER, FORKLIFT, PPE
    private String serialNumber;
    private String condition;
}

@Entity
public class AssetAssignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private Asset asset;
    private LocalDate checkoutDate;
    private LocalDate returnDate;
}

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    // endpoints for assignment, return, condition update
}
```

---

## Section: E10 - Performance Reviews & Goals

**Description:** Quarterly/annual review templates; track goals, competencies, ratings, comments; supervisor/employee acknowledgements.

**Design Specification:**
- Entities: PerformanceReview, Goal, Competency
- Service: PerformanceService (review creation, tracking)
- Controller: PerformanceController

**Sample Implementation:**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate reviewDate;
    private String period; // QUARTERLY, ANNUAL
    private String supervisorComments;
    private String employeeAcknowledgement;
}

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {
    // endpoints for reviews, goals, competencies
}
```

---

## Section: E11 - Payroll Export Integration

**Description:** Generate payroll-ready files from approved attendance/leave; mapping to external payroll formats; secure delivery (SFTP/API).

**Design Specification:**
- Service: PayrollExportService (file generation, mapping)
- Controller: PayrollExportController
- Integration: SFTP/API delivery utility

**Sample Implementation:**
```java
@Service
public class PayrollExportService {
    public File generatePayrollFile(List<AttendanceEvent> events, List<LeaveRequest> leaves) { ... }
    public void deliverPayrollFile(File file) { ... }
}

@RestController
@RequestMapping("/api/payroll")
public class PayrollExportController {
    @PostMapping("/export")
    public ResponseEntity<?> exportPayroll() { ... }
}
```

---

## Section: E12 - Notifications & Announcements

**Description:** In-app/email/SMS notifications for shift changes, expiring certs, approvals, announcements; quiet hours config.

**Design Specification:**
- Entities: Notification, Announcement
- Service: NotificationService (delivery, quiet hours)
- Controller: NotificationController
- Integration: Email/SMS providers

**Sample Implementation:**
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private String type; // EMAIL, SMS, IN_APP
    private String message;
    private LocalDateTime sentAt;
    @ManyToOne
    private Employee recipient;
}

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    // endpoints for sending, configuring notifications
}
```

---

## Section: E13 - Integration Layer (HRIS/WMS APIs)

**Description:** Expose REST APIs/connectors for HRIS (new hires/terms), WMS (location/department), IDP for SSO; webhooks for events.

**Design Specification:**
- Service: IntegrationService (HRIS, WMS, IDP connectors)
- Controller: IntegrationController (REST endpoints)
- Webhook event publisher

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    @PostMapping("/hris/new-hire")
    public ResponseEntity<?> handleNewHire(@RequestBody EmployeeDto dto) { ... }
    @PostMapping("/wms/location-update")
    public ResponseEntity<?> handleLocationUpdate(@RequestBody LocationDto dto) { ... }
}
```

---

## Section: E14 - Audit Trail & Compliance

**Description:** Centralized audit logging for sensitive changes (employee PII, schedules, approvals, payroll); tamper-evident storage.

**Design Specification:**
- Entity: AuditLog
- Service: AuditService (logging, retrieval)
- Controller: AuditController
- Tamper-evident storage (hash chaining)

**Sample Implementation:**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entityType;
    private String entityId;
    private String action;
    private String performedBy;
    private LocalDateTime timestamp;
    private String hash;
    private String previousHash;
}

@Service
public class AuditService {
    public void logChange(String entityType, String entityId, String action, String performedBy) { ... }
}
```

---

## Section: E15 - Reporting & Analytics

**Description:** Operational reports: attendance, overtime, leave balances, certification status, safety KPIs; export CSV/PDF; role-based dashboards.

**Design Specification:**
- Service: ReportingService (report generation)
- Controller: ReportingController
- CSV/PDF export utility
- Dashboard endpoints with role-based access

**Sample Implementation:**
```java
@Service
public class ReportingService {
    public Report generateAttendanceReport(LocalDate from, LocalDate to) { ... }
    public File exportReportAsCsv(Report report) { ... }
}

@RestController
@RequestMapping("/api/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<?> getAttendanceReport(@RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
}
```

---

## Section: E16 - Mobile Access (PWA)

**Description:** Responsive views for workers to clock-in/out, view schedules, request leave, see announcements; offline-friendly via PWA.

**Design Specification:**
- Controller: MobileController (REST endpoints for mobile)
- Service: MobileService (business logic)
- PWA manifest and service worker in static resources

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/mobile")
public class MobileController {
    @GetMapping("/schedule")
    public ResponseEntity<?> getSchedule(@RequestParam Long employeeId) { ... }
}

// src/main/resources/static/manifest.json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone"
}
```

---

## Section: E17 - Onboarding & Offboarding Workflow

**Description:** Automate provisioning of accounts, initial schedule, required training; deprovision access/assets on termination.

**Design Specification:**
- Service: OnboardingService, OffboardingService
- Controller: OnboardingController, OffboardingController
- Integration: HRIS, asset modules

**Sample Implementation:**
```java
@Service
public class OnboardingService {
    public void provisionAccount(Employee employee) { ... }
    public void assignInitialSchedule(Employee employee) { ... }
}

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {
    @PostMapping("/provision")
    public ResponseEntity<?> provision(@RequestBody EmployeeDto dto) { ... }
}
```

---

## Section: E18 - Localization & Multi-Warehouse

**Description:** Support multiple warehouses with distinct calendars, policies, shift templates; UI localization (en, es).

**Design Specification:**
- Entities: Warehouse, WarehousePolicy, WarehouseCalendar
- Service: LocalizationService, WarehouseService
- Controller: WarehouseController
- MessageSource bean for localization

**Sample Implementation:**
```java
@Entity
public class Warehouse {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String location;
}

@Bean
public MessageSource messageSource() {
    ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
    source.setBasename("classpath:messages");
    source.setDefaultEncoding("UTF-8");
    return source;
}
```

---

## Section: E19 - Observability & Monitoring

**Description:** Integrate Prometheus metrics, structured logging (JSON), distributed tracing (Jaeger/Zipkin), alerting for critical failures.

**Design Specification:**
- Dependencies: micrometer-prometheus, logstash-logback-encoder, opentracing
- Configuration: application.yml for metrics, logging, tracing
- Actuator endpoints for health/metrics

**Sample Implementation:**
```yaml
# application.yml
management.endpoints.web.exposure.include=health,metrics,prometheus
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
logging.level.root=INFO
spring.zipkin.enabled=true
spring.zipkin.base-url=http://localhost:9411
```

---

## Section: E20 - Deployment & CI/CD

**Description:** Containerize with Docker; Kubernetes manifests; GitHub Actions pipeline for build/test/deploy; environment-specific configs.

**Design Specification:**
- Dockerfile for Spring Boot app
- k8s manifests: deployment, service, configmap, secret
- .github/workflows/ci.yml for CI/CD pipeline
- application.yml for environment configs

**Sample Implementation:**
```dockerfile
# Dockerfile
FROM openjdk:17-jdk-alpine
COPY target/warehouse-ems.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]

# .github/workflows/ci.yml
name: CI/CD Pipeline
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build with Maven
        run: mvn clean package
      - name: Build Docker image
        run: docker build -t warehouse-ems .
      - name: Deploy to Kubernetes
        run: kubectl apply -f k8s/
```

---

## Document Summary

This low-level technical design document provides comprehensive Spring Boot architecture specifications for all 20 epics of the Warehouse Employee Management System. Each section includes:

- Detailed architectural overview
- Package and component structure
- Entity designs with relationships
- Service, repository, and controller specifications
- Configuration and security settings
- Integration points
- Sample code implementations

The design follows Spring Boot best practices and industry standards, ensuring a scalable, maintainable, and secure enterprise application.
