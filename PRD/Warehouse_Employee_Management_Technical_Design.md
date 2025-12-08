Section: E01 - Project Scaffolding & Domain Setup
Description: Initialize the Spring Boot project using Maven, configure base packages, set up core modules (employee, scheduling, attendance, safety), add Flyway/Liquibase for DB migrations, and enable Actuator for health monitoring.
Design Specification:
- Package structure: com.company.wms.{employee,scheduling,attendance,safety,common}
- Modules: employee, scheduling, attendance, safety, common
- Flyway/Liquibase for DB migrations
- Spring Boot Actuator enabled
- README with build/run steps
Sample Implementation:
```java
@SpringBootApplication
public class WarehouseEmployeeManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeManagementApplication.class, args);
    }
}
```

Section: E02 - Employee Master Data (CRUD)
Description: Employee domain with CRUD APIs and DTOs for name, badgeId, role, department, shiftGroup, hireDate, status. Unique badgeId enforced, soft-delete supported, pagination/filtering, OpenAPI schemas.
Design Specification:
- Entity: Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService (CRUD, soft-delete)
- Controller: EmployeeController (REST endpoints)
- DTOs: EmployeeDto, EmployeeCreateRequest, EmployeeUpdateRequest
Sample Implementation:
```java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    @Column(unique = true)
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted;
}
```

Section: E03 - Role-Based Access Control (RBAC)
Description: Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER), method/endpoint security, row-level constraints, API key/OAuth2 toggle via config.
Design Specification:
- SecurityConfig: configure roles, method security
- API key/OAuth2 toggle via application.yml
- Row-level security in EmployeeRepository
- Tests for security rules
Sample Implementation:
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/admin/**").hasRole("ADMIN")
            .antMatchers("/hr/**").hasRole("HR")
            .antMatchers("/supervisor/**").hasRole("SUPERVISOR")
            .antMatchers("/worker/**").hasRole("WORKER")
            .anyRequest().authenticated();
    }
}
```

Section: E04 - Time & Attendance (Clock In/Out)
Description: Endpoints for clock-in/out events with geofence/device capture, calculate hours worked per shift, handle missed punches/corrections workflow.
Design Specification:
- Entity: AttendanceEvent (id, employeeId, type, timestamp, deviceId, location, shiftId)
- Service: AttendanceService (clock-in/out, corrections)
- Controller: AttendanceController
- Geofence validation utility
- Reports export (CSV)
Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type; // CLOCK_IN, CLOCK_OUT
    private LocalDateTime timestamp;
    private String deviceId;
    private String location;
    private Long shiftId;
}
```

Section: E05 - Shift & Schedule Management
Description: Recurring shift templates, rotations, overtime rules, assignment to employees, blackout dates, warehouse operation calendars.
Design Specification:
- Entity: ShiftTemplate, ShiftAssignment, BlackoutDate
- Service: ShiftService (CRUD, conflict detection)
- Controller: ShiftController
- Audit entries for changes
Sample Implementation:
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

Section: E06 - Leave & Absence Management
Description: Request/approve PTO, sick, unpaid leave; accrual balances/policies; integration hooks to exclude from scheduling/payroll.
Design Specification:
- Entity: LeaveRequest (id, employeeId, type, startDate, endDate, status, accrualBalance)
- Service: LeaveService (request, approve/deny, update balances)
- Controller: LeaveController
- Integration with scheduling/payroll
Sample Implementation:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type; // PTO, SICK, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // REQUESTED, APPROVED, DENIED
    private int accrualBalance;
}
```

Section: E07 - Training & Certification Tracking
Description: Track certifications (e.g., forklift), expirations, renewals; block assignment to tasks requiring expired certs; upload proof documents.
Design Specification:
- Entity: Certification (id, employeeId, type, expiryDate, documentUrl)
- Service: CertificationService (CRUD, expiry alerts)
- Controller: CertificationController
- Scheduling checks for valid certs
Sample Implementation:
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type;
    private LocalDate expiryDate;
    private String documentUrl;
}
```

Section: E08 - Safety Incidents & OSHA Reporting
Description: Record incidents/near-misses; severity, location, description, involved employees; workflow for investigation/corrective actions; generate OSHA summary.
Design Specification:
- Entity: SafetyIncident (id, severity, location, description, status, involvedEmployeeIds)
- Service: SafetyIncidentService (CRUD, workflow)
- Controller: SafetyIncidentController
- OSHA report generator
Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String severity;
    private String location;
    private String description;
    private String status; // OPEN, INVESTIGATING, RESOLVED
    @ElementCollection
    private List<Long> involvedEmployeeIds;
}
```

Section: E09 - Equipment & Asset Assignment
Description: Assign scanners, forklifts, PPE to employees; track checkout/return; prevent use if certification missing; maintain asset condition state.
Design Specification:
- Entity: Asset (id, type, condition, assignedEmployeeId, checkoutDate, returnDate)
- Service: AssetService (CRUD, check-in/out, cert validation)
- Controller: AssetController
- History log per asset/employee
Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String condition;
    private Long assignedEmployeeId;
    private LocalDateTime checkoutDate;
    private LocalDateTime returnDate;
}
```

Section: E10 - Performance Reviews & Goals
Description: Quarterly/annual review templates; track goals, competencies, ratings, comments; supervisor/employee acknowledgements.
Design Specification:
- Entity: PerformanceReview (id, employeeId, cycle, goals, competencies, ratings, comments, status)
- Service: PerformanceReviewService (CRUD, workflow)
- Controller: PerformanceReviewController
- PDF export utility
Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String cycle; // Q1, Q2, Annual
    private String goals;
    private String competencies;
    private String ratings;
    private String comments;
    private String status; // DRAFT, SUBMITTED, ACKNOWLEDGED
}
```

Section: E11 - Payroll Export Integration
Description: Generate payroll-ready files from approved attendance/leave; mapping to external payroll provider formats; secure delivery (SFTP/API).
Design Specification:
- PayrollExportService (generate, map, deliver)
- IntegrationConfig (SFTP/API credentials)
- Audit log for exports
Sample Implementation:
```java
@Service
public class PayrollExportService {
    public File generatePayrollExport(LocalDate period) {
        // Fetch attendance/leave, map to provider schema, write CSV
    }
    public void deliverExport(File exportFile) {
        // SFTP/API delivery logic
    }
}
```

Section: E12 - Notifications & Announcements
Description: In-app/email/SMS notifications for shift changes, expiring certs, approvals, announcements; quiet hours config.
Design Specification:
- NotificationService (send, track status, rate limit)
- NotificationChannelConfig (email, SMS, in-app)
- Announcement entity
- Localization of templates
Sample Implementation:
```java
@Service
public class NotificationService {
    public void sendNotification(Notification notification) {
        // Channel selection, delivery, status tracking
    }
}
```

Section: E13 - Integration Layer (HRIS/WMS APIs)
Description: Expose REST APIs/connectors for HRIS (new hires/terms), WMS (location/department), IDP for SSO; webhooks for events.
Design Specification:
- HRISConnector, WMSConnector, IDPConnector
- JWT/OAuth2-secured REST APIs
- WebhookController
- OpenAPI documentation
Sample Implementation:
```java
@RestController
@RequestMapping("/api/hris")
public class HRISController {
    @PostMapping("/sync")
    public ResponseEntity<?> syncEmployees(@RequestBody List<EmployeeDto> employees) {
        // Sync logic
    }
}
```

Section: E14 - Audit Trail & Compliance
Description: Centralized audit logging for sensitive changes (employee PII, schedules, approvals, payroll); tamper-evident storage.
Design Specification:
- AuditLog entity (id, actor, timestamp, entity, before, after)
- AuditLogService (record, export)
- Immutable log table
Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    @Lob
    private String before;
    @Lob
    private String after;
}
```

Section: E15 - Reporting & Analytics
Description: Operational reports (attendance, overtime, leave, cert status, safety KPIs); export CSV/PDF; role-based dashboards.
Design Specification:
- ReportService (generate, filter, export)
- DashboardController
- Metrics endpoints for BI
Sample Implementation:
```java
@Service
public class ReportService {
    public List<AttendanceReport> generateAttendanceReport(DateRange range, String department) {
        // Query, aggregate, filter
    }
}
```

Section: E16 - Mobile Access (PWA)
Description: Responsive views for workers to clock-in/out, view schedules, request leave, see announcements; offline-friendly via PWA.
Design Specification:
- PWA manifest (manifest.json)
- Mobile-friendly React/Vue frontend
- Offline queue for clock events
- API for mobile flows
Sample Implementation:
```json
{
  "name": "Warehouse Employee Management",
  "short_name": "WMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

Section: E17 - Onboarding & Offboarding Workflow
Description: Automate provisioning of accounts, initial schedule, required training; deprovision access/assets on termination.
Design Specification:
- OnboardingService (provision, assign tasks)
- OffboardingService (revoke, collect assets)
- Integration with HRIS, AssetService, ScheduleService
Sample Implementation:
```java
@Service
public class OnboardingService {
    public void onboardNewHire(Employee employee) {
        // Provision account, assign schedule, training tasks
    }
}
```

Section: E18 - Localization & Multi-Tenant
Description: Support multiple warehouses/tenants with data isolation; localized strings (en, es); timezone-aware scheduling.
Design Specification:
- TenantContext (ThreadLocal or filter-based)
- Entities include tenantId field
- DataSource routing for multi-tenancy
- MessageSource for i18n (messages_en.properties, messages_es.properties)
- Timezone field in warehouse/tenant entity
Sample Implementation:
```java
@Entity
public class Warehouse {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String timezone;
    private String tenantId;
}

@Bean
public MessageSource messageSource() {
    ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
    source.setBasename("classpath:messages");
    source.setDefaultEncoding("UTF-8");
    return source;
}
```

Section: E19 - Observability & Monitoring
Description: Prometheus metrics, JSON logging, distributed tracing (Zipkin/Jaeger); alerts for errors, slow queries, SLA breaches.
Design Specification:
- Spring Boot Actuator endpoints (/metrics, /health)
- Micrometer Prometheus integration
- Logback JSON logging config
- Sleuth/Zipkin/Jaeger tracing
- Alerting via Prometheus Alertmanager
Sample Implementation:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
logging:
  level:
    root: INFO
  pattern:
    console: '{"timestamp":"%d{yyyy-MM-dd HH:mm:ss}","level":"%p","logger":"%c","message":"%m"}'
spring:
  sleuth:
    enabled: true
  zipkin:
    enabled: true
    base-url: http://zipkin:9411
```

Section: E20 - CI/CD & Deployment Automation
Description: GitHub Actions or Jenkins pipeline for build, test, security scan, Docker image push, and deploy to staging/prod; rollback plan.
Design Specification:
- .github/workflows/ci.yml or Jenkinsfile
- Steps: checkout, build (Maven), test, security scan (OWASP/Trivy), Docker build/push, deploy (K8s/VM)
- Rollback: previous image redeploy
Sample Implementation:
```yaml
name: CI/CD Pipeline
on:
  push:
    branches: [ main ]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build
        run: mvn clean package
      - name: Test
        run: mvn test
      - name: Security Scan
        run: trivy fs .
      - name: Build Docker Image
        run: docker build -t wms:${{ github.sha }} .
      - name: Push Docker Image
        run: docker push wms:${{ github.sha }}
      - name: Deploy
        run: ./deploy.sh
```
