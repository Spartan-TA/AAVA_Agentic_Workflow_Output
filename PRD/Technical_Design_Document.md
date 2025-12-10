# Technical Design Document: Warehouse Employee Management System

---

Section: E01 - Project Scaffolding & Domain Setup  
Description: Establishes the foundational Spring Boot project structure, core modules, and essential configurations for maintainability and scalability.  
Design Specification:
- Spring Boot Maven project with modular package structure: employee, scheduling, attendance, safety, common, config
- Use Flyway or Liquibase for database migrations
- Enable Spring Boot Actuator for health and metrics
- Standard application.properties/yml for environment configuration
- Base exception handling and logging setup  
Sample Implementation:  
```java
// Maven pom.xml includes spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, actuator, flyway/liquibase
// Directory structure:
com.company.wms
  âââ employee
  âââ scheduling
  âââ attendance
  âââ safety
  âââ common
  âââ config

// application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wms
    username: wms
    password: secret
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true

// Flyway migration example (V1__init.sql)
CREATE TABLE employee (...);
```

---

Section: E02 - Employee Master Data (CRUD)  
Description: Centralizes employee records with full CRUD operations, ensuring a single source of truth.  
Design Specification:
- Employee entity: id, name, badgeId, role, department, shiftGroup, hireDate, status
- EmployeeRepository: extends JpaRepository<Employee, Long>
- EmployeeService: business logic for CRUD, validation
- EmployeeController: REST endpoints for CRUD
- DTOs for request/response, validation annotations  
Sample Implementation:  
```java
@Entity
public class Employee {
  @Id @GeneratedValue private Long id;
  private String name;
  @Column(unique=true) private String badgeId;
  @Enumerated(EnumType.STRING) private Role role;
  private String department;
  private String shiftGroup;
  private LocalDate hireDate;
  @Enumerated(EnumType.STRING) private Status status;
}

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
  @GetMapping public List<EmployeeDto> list() {...}
  @PostMapping public EmployeeDto create(@Valid @RequestBody EmployeeDto dto) {...}
  // ... update, delete
}
```

---

Section: E03 - Role-Based Access Control (RBAC)  
Description: Implements granular security using Spring Security, supporting roles and row-level constraints.  
Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Method/endpoint security via @PreAuthorize
- Row-level security in repository/service
- API key or OAuth2 toggle via configuration
- SecurityConfig for endpoint protection  
Sample Implementation:  
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
      .antMatchers("/api/employees/**").hasAnyRole("ADMIN", "HR")
      .anyRequest().authenticated();
  }
}

// Row-level security in repository
@Query("SELECT e FROM Employee e WHERE e.department = :dept")
List<Employee> findByDepartment(@Param("dept") String dept);
```

---

Section: E04 - Time & Attendance (Clock In/Out)  
Description: Provides endpoints for employees to clock in/out, with geofencing, device capture, and correction workflows.  
Design Specification:
- Attendance entity: id, employee, clockIn, clockOut, location, deviceId, status
- AttendanceRepository: findByEmployeeAndDate
- AttendanceService: clockIn, clockOut, calculate hours, handle missed punches
- AttendanceController: endpoints for clock-in/out, corrections
- Geofence validation utility  
Sample Implementation:  
```java
@Entity
public class Attendance {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private LocalDateTime clockIn;
  private LocalDateTime clockOut;
  private String location;
  private String deviceId;
  @Enumerated(EnumType.STRING) private Status status;
}

@PostMapping("/api/attendance/clock-in")
public ResponseEntity<?> clockIn(@RequestBody ClockInRequest req) {
  // Validate geofence, device, create Attendance record
}
```

---

Section: E05 - Shift & Schedule Management  
Description: Manages recurring shift templates, rotations, overtime, and blackout dates.  
Design Specification:
- ShiftTemplate entity: id, name, startTime, endTime, recurrence, overtimeRules
- ShiftAssignment entity: employee, shiftTemplate, date
- ScheduleService: assign shifts, handle rotations, blackout dates
- ScheduleController: endpoints for managing templates, assignments  
Sample Implementation:  
```java
@Entity
public class ShiftTemplate {
  @Id @GeneratedValue private Long id;
  private String name;
  private LocalTime startTime;
  private LocalTime endTime;
  private String recurrence; // e.g., WEEKLY
  private String overtimeRules;
}

@PostMapping("/api/shifts/assign")
public ResponseEntity<?> assignShift(@RequestBody ShiftAssignmentDto dto) {...}
```

---

Section: E06 - Leave & Absence Management  
Description: Handles leave requests, approvals, accruals, and integration with scheduling and payroll.  
Design Specification:
- LeaveRequest entity: id, employee, type, startDate, endDate, status, approver
- LeaveBalance entity: employee, type, balance
- LeaveService: request, approve, calculate accruals
- LeaveController: endpoints for request/approval
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
  @Enumerated(EnumType.STRING) private Status status;
  @ManyToOne private Employee approver;
}

@PostMapping("/api/leave/request")
public ResponseEntity<?> requestLeave(@RequestBody LeaveRequestDto dto) {...}
```

---

Section: E07 - Training & Certification Tracking  
Description: Tracks employee certifications, expirations, renewals, and blocks assignments if expired.  
Design Specification:
- Certification entity: id, name, description, validForMonths
- EmployeeCertification: employee, certification, issueDate, expiryDate, proofDocumentUrl
- CertificationService: check validity, renewals
- CertificationController: endpoints for upload, validation  
Sample Implementation:  
```java
@Entity
public class EmployeeCertification {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  @ManyToOne private Certification certification;
  private LocalDate issueDate;
  private LocalDate expiryDate;
  private String proofDocumentUrl;
}

@PostMapping("/api/certifications/upload")
public ResponseEntity<?> uploadProof(@RequestParam MultipartFile file, ...) {...}
```

---

Section: E08 - Safety Incidents & OSHA Reporting  
Description: Records safety incidents, manages investigation workflow, and generates OSHA summaries.  
Design Specification:
- SafetyIncident entity: id, employee, date, severity, location, description, status
- Investigation entity: incident, investigator, actions, status
- SafetyService: record, investigate, generate reports
- SafetyController: endpoints for incident reporting, investigation  
Sample Implementation:  
```java
@Entity
public class SafetyIncident {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private LocalDate date;
  private String severity;
  private String location;
  private String description;
  @Enumerated(EnumType.STRING) private Status status;
}

@PostMapping("/api/safety/report")
public ResponseEntity<?> reportIncident(@RequestBody SafetyIncidentDto dto) {...}
```

---

Section: E09 - Equipment & Asset Assignment  
Description: Assigns assets to employees, tracks checkouts/returns, and enforces certification requirements.  
Design Specification:
- Asset entity: id, type, serialNumber, condition, assignedTo
- AssetAssignment entity: asset, employee, checkoutDate, returnDate
- AssetService: assign, return, check certification
- AssetController: endpoints for assignment, return  
Sample Implementation:  
```java
@Entity
public class Asset {
  @Id @GeneratedValue private Long id;
  private String type;
  private String serialNumber;
  private String condition;
  @ManyToOne private Employee assignedTo;
}

@PostMapping("/api/assets/assign")
public ResponseEntity<?> assignAsset(@RequestBody AssetAssignmentDto dto) {...}
```

---

Section: E10 - Performance Reviews & Goals  
Description: Manages review templates, goals, competencies, and supervisor/employee acknowledgements.  
Design Specification:
- ReviewTemplate entity: id, name, period, competencies
- PerformanceReview entity: employee, template, period, ratings, comments, supervisor, acknowledged
- ReviewService: create, update, acknowledge
- ReviewController: endpoints for review management  
Sample Implementation:  
```java
@Entity
public class PerformanceReview {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  @ManyToOne private ReviewTemplate template;
  private String period;
  private String ratings;
  private String comments;
  @ManyToOne private Employee supervisor;
  private boolean acknowledged;
}

@PostMapping("/api/reviews/submit")
public ResponseEntity<?> submitReview(@RequestBody PerformanceReviewDto dto) {...}
```

---

Section: E11 - Payroll Export Integration  
Description: Generates payroll-ready files from attendance/leave, mapping to provider formats, and delivers securely.  
Design Specification:
- PayrollExportService: aggregate data, map to provider format (CSV, XML, JSON)
- Secure delivery via SFTP/API
- PayrollExportController: endpoints to trigger/export payroll files
- Integration configuration for provider endpoints  
Sample Implementation:  
```java
@Service
public class PayrollExportService {
  public File generatePayrollFile(PayrollExportRequest req) {
    // Aggregate attendance/leave, map to format, write file
  }
  public void deliverToProvider(File file) {
    // SFTP/API upload logic
  }
}

@PostMapping("/api/payroll/export")
public ResponseEntity<?> exportPayroll(@RequestBody PayrollExportRequest req) {...}
```

---

Section: E12 - Notifications & Announcements  
Description: Sends in-app, email, and SMS notifications for key events, with quiet hours configuration.  
Design Specification:
- Notification entity: id, type, recipient, message, sentAt, channel
- NotificationService: send, schedule, enforce quiet hours
- NotificationController: endpoints for announcements
- Integration with email/SMS providers  
Sample Implementation:  
```java
@Entity
public class Notification {
  @Id @GeneratedValue private Long id;
  private String type;
  private String recipient;
  private String message;
  private LocalDateTime sentAt;
  private String channel;
}

@Service
public class NotificationService {
  public void sendNotification(NotificationDto dto) {
    // Send via email/SMS/in-app
  }
}
```

---

Section: E13 - Integration Layer (HRIS/WMS APIs)  
Description: Provides REST APIs and connectors for HRIS, WMS, IDP, and webhooks for event-driven integration.  
Design Specification:
- IntegrationService: sync employees, departments, SSO
- REST clients using WebClient/RestTemplate
- WebhookController: endpoints for event notifications
- Security for external API access  
Sample Implementation:  
```java
@Service
public class IntegrationService {
  public void syncFromHRIS() {
    // Call HRIS API, map to Employee
  }
}

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {
  @PostMapping("/employee-event")
  public ResponseEntity<?> handleEmployeeEvent(@RequestBody WebhookEventDto dto) {...}
}
```

---

Section: E14 - Audit Trail & Compliance  
Description: Centralizes audit logging for sensitive changes, with tamper-evident storage.  
Design Specification:
- AuditLog entity: id, entityType, entityId, action, user, timestamp, details
- AuditAspect: @Aspect for logging changes
- Secure, append-only storage (e.g., write-once DB table)
- AuditController: endpoints for audit queries (admin only)  
Sample Implementation:  
```java
@Entity
public class AuditLog {
  @Id @GeneratedValue private Long id;
  private String entityType;
  private Long entityId;
  private String action;
  private String user;
  private LocalDateTime timestamp;
  private String details;
}

@Aspect
@Component
public class AuditAspect {
  @AfterReturning(...)
  public void logChange(JoinPoint jp, ...) {
    // Persist AuditLog
  }
}
```

---

Section: E15 - Reporting & Analytics  
Description: Provides operational reports, dashboards, and exports for key warehouse KPIs.  
Design Specification:
- ReportService: generate attendance, overtime, leave, certification, safety reports
- ReportController: endpoints for report generation, export (CSV/PDF)
- Role-based dashboard views  
Sample Implementation:  
```java
@Service
public class ReportService {
  public Report generateAttendanceReport(ReportRequest req) {
    // Aggregate data, format for output
  }
}

@GetMapping("/api/reports/attendance")
public ResponseEntity<?> getAttendanceReport(@RequestParam ...) {...}
```

---

Section: E16 - Mobile Access (PWA)  
Description: Enables responsive, offline-friendly mobile access for workers using PWA standards.  
Design Specification:
- Spring Boot serves static PWA assets (React/Vue/Angular)
- REST APIs for clock-in/out, schedules, leave, announcements
- Service Worker for offline support
- JWT-based authentication for mobile  
Sample Implementation:  
```java
// application.yml
spring:
  resources:
    static-locations: classpath:/static/

// Controller endpoints as above
// PWA assets in /static, service-worker.js for offline
```

---

Section: E17 - Onboarding & Offboarding Workflow  
Description: Automates provisioning/deprovisioning of accounts, schedules, training, and asset access.  
Design Specification:
- OnboardingTask entity: id, employee, type, status, dueDate
- OnboardingService: create tasks, track completion
- OffboardingService: revoke access, reclaim assets
- WorkflowController: endpoints for onboarding/offboarding  
Sample Implementation:  
```java
@Entity
public class OnboardingTask {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String type;
  @Enumerated(EnumType.STRING) private Status status;
  private LocalDate dueDate;
}

@PostMapping("/api/onboarding/start")
public ResponseEntity<?> startOnboarding(@RequestBody OnboardingRequest req) {...}
```

---

Section: E18 - Localization & Multi-Tenant Support  
Description: Supports multiple warehouses/tenants with data isolation and i18n.  
Design Specification:
- Tenant entity: id, name, timezone, locale
- All entities reference tenantId
- Spring MessageSource for i18n
- TenantContext for data isolation (via Hibernate filters or schema)  
Sample Implementation:  
```java
@Entity
public class Tenant {
  @Id @GeneratedValue private Long id;
  private String name;
  private String timezone;
  private String locale;
}

@Bean
public MessageSource messageSource() {
  ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
  ms.setBasename("classpath:messages");
  ms.setDefaultEncoding("UTF-8");
  return ms;
}
```

---

Section: E19 - Observability & Monitoring  
Description: Integrates metrics, structured logging, and distributed tracing for system health and alerting.  
Design Specification:
- Micrometer metrics with Prometheus/Grafana
- Structured JSON logging (Logback config)
- Distributed tracing with Zipkin/Jaeger
- SLO alerting via Prometheus rules  
Sample Implementation:  
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  tracing:
    enabled: true
    sampling:
      probability: 1.0

# logback-spring.xml for JSON logs
```

---

Section: E20 - CI/CD & Deployment Automation  
Description: Automates build, test, Docker image creation, deployment, and DB migrations.  
Design Specification:
- GitHub Actions or Jenkins pipeline for build, test, Docker image, deploy
- Automated Flyway/Liquibase migrations
- Rollback plan for failed deployments
- Environment-specific configuration via profiles  
Sample Implementation:  
```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline
on:
  push:
    branches: [main]
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
        run: docker build -t wms-app .
      - name: Deploy to Staging
        run: ./deploy.sh staging
      - name: Run DB migrations
        run: mvn flyway:migrate
```

---

**End of Technical Design Document**