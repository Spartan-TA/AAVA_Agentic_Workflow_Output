Section: Project Scaffolding & Domain Setup
Description: Establishes the foundational Spring Boot project structure, configures core modules, sets up database migration, and enables monitoring endpoints.
Design Specification:
- Spring Boot Maven project with parent pom.xml
- Base package: com.company.wems
- Modules: employee, scheduling, attendance, safety
- Flyway/Liquibase for DB migrations (src/main/resources/db/migration)
- Spring Boot Actuator enabled (management.endpoints.web.exposure.include=*)
- Application runs on port 8080
- README with build/run steps
Sample Implementation:
```java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

Section: Employee Master Data (CRUD)
Description: Implements CRUD APIs for employee records, including validation, soft-delete, and OpenAPI documentation.
Design Specification:
- Entity: Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService (CRUD, bulk import/export, soft-delete)
- Controller: EmployeeController (REST endpoints)
- DTOs for API requests/responses
- OpenAPI annotations for schema
Sample Implementation:
```java
@Entity
public class Employee {
    @Id @GeneratedValue private Long id;
    @Column(unique = true) private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted = false;
}
```

Section: Role Based Access Control (RBAC)
Description: Secures endpoints and data access using Spring Security with role-based and row-level constraints.
Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Method security: @PreAuthorize annotations
- API key/OAuth2 toggle via application.yml
- Row-level security in repositories/services
- Security tests for all rules
Sample Implementation:
```java
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    // ...
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .anyRequest().authenticated();
    }
}
```

Section: Time & Attendance (Clock In/Out)
Description: Provides endpoints for clock-in/out, calculates hours, and manages corrections workflow.
Design Specification:
- Entity: AttendanceEvent (id, employee, timestamp, type, device, location)
- Service: AttendanceService (clock-in/out, calculate totals, corrections)
- Controller: AttendanceController (REST endpoints)
- Approval workflow for corrections
- CSV export endpoint
Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private LocalDateTime timestamp;
    private String type; // CLOCK_IN, CLOCK_OUT
    private String device;
    private String location;
}
```

Section: Shift & Schedule Management
Description: Manages shift templates, assignments, rotations, and conflict detection.
Design Specification:
- Entity: ShiftTemplate, ShiftAssignment
- Service: ShiftService (CRUD, assign, detect conflicts)
- Controller: ShiftController (REST endpoints)
- Audit logging for changes
Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrenceRule;
}
```

Section: Leave & Absence Management
Description: Handles PTO, sick, unpaid leave requests, approvals, and accruals.
Design Specification:
- Entity: LeaveRequest (id, employee, type, startDate, endDate, status)
- Service: LeaveService (request, approve, update balances)
- Controller: LeaveController
- Integration with scheduling and payroll
Sample Implementation:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
```

Section: Training & Certification Tracking
Description: Tracks employee certifications, expirations, and blocks unqualified assignments.
Design Specification:
- Entity: Certification (id, employee, type, expiryDate, documentUrl)
- Service: CertificationService (CRUD, alerts)
- Controller: CertificationController
- Scheduling checks for valid certifications
Sample Implementation:
```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String type;
    private LocalDate expiryDate;
    private String documentUrl;
}
```

Section: Safety Incidents & OSHA Reporting
Description: Records safety incidents, manages investigation workflow, and generates OSHA reports.
Design Specification:
- Entity: SafetyIncident (id, severity, location, description, status, involvedEmployees)
- Service: SafetyService (record, workflow, export)
- Controller: SafetyController
- OSHA export endpoints
Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private String severity;
    private String location;
    private String description;
    private String status;
    @ManyToMany private List<Employee> involvedEmployees;
}
```

Section: Equipment & Asset Assignment
Description: Assigns assets to employees, tracks check-in/out, and enforces certification requirements.
Design Specification:
- Entity: Asset (id, type, condition, assignedTo, checkoutHistory)
- Service: AssetService (CRUD, check-in/out, block if cert missing)
- Controller: AssetController
Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String type;
    private String condition;
    @ManyToOne private Employee assignedTo;
    @OneToMany private List<AssetHistory> checkoutHistory;
}
```

Section: Performance Reviews & Goals
Description: Manages review cycles, goals, ratings, and immutable history after sign-off.
Design Specification:
- Entity: PerformanceReview (id, employee, cycle, goals, ratings, comments, status)
- Service: ReviewService (create, assign, submit, acknowledge)
- Controller: ReviewController
- PDF export endpoint
Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String cycle;
    private String goals;
    private String ratings;
    private String comments;
    private String status;
}
```

Section: Payroll Export Integration
Description: Generates payroll-ready files and delivers securely to external providers.
Design Specification:
- Service: PayrollExportService (generate, map, deliver, retry)
- SFTP/API integration
- Audit log for exports
Sample Implementation:
```java
@Service
public class PayrollExportService {
    public void exportPayrollData() {
        // Generate file, map schema, deliver via SFTP/API
    }
}
```

Section: Notifications & Announcements
Description: Sends notifications via in-app, email, or SMS for key events and announcements.
Design Specification:
- Entity: Notification (id, user, type, channel, status, message)
- Service: NotificationService (send, track, rate limit)
- Controller: NotificationController
- Localization and quiet hours config
Sample Implementation:
```java
@Entity
public class Notification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private User user;
    private String type;
    private String channel;
    private String status;
    private String message;
}
```

Section: Integration Layer (HRIS/WMS APIs)
Description: Exposes REST APIs and connectors for HRIS, WMS, and SSO integration.
Design Specification:
- REST controllers for HRIS/WMS endpoints
- JWT/OAuth2 security
- Webhook endpoints (idempotent)
- OpenAPI documentation
Sample Implementation:
```java
@RestController
@RequestMapping("/api/hris")
public class HrisController {
    @PostMapping("/employees")
    public ResponseEntity<?> syncEmployee(@RequestBody EmployeeDto dto) {
        // Sync logic
    }
}
```

Section: Audit Trail & Compliance
Description: Centralized audit logging for sensitive changes with tamper-evident storage.
Design Specification:
- Entity: AuditLog (id, actor, timestamp, entity, before, after)
- Service: AuditService (log, export)
- Immutable log table
Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    private String before;
    private String after;
}
```

Section: Reporting & Analytics
Description: Provides operational reports, exports, and dashboards with role-based access.
Design Specification:
- Service: ReportingService (generate, filter, export)
- Controller: ReportingController
- CSV/PDF export endpoints
Sample Implementation:
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> exportAttendanceReport(...) {
        // Export logic
    }
}
```

Section: Mobile Access (PWA)
Description: Enables responsive, offline-friendly mobile access for core workflows.
Design Specification:
- PWA manifest and service worker
- Mobile-optimized views for clock-in/out, schedules, leave
- Offline queue for events
Sample Implementation:
```json
{
  "short_name": "WEMS",
  "name": "Warehouse EMS",
  "start_url": ".",
  "display": "standalone",
  "background_color": "#fff",
  "theme_color": "#1976d2"
}
```

Section: Onboarding & Offboarding Workflow
Description: Automates provisioning and deprovisioning of accounts, schedules, and assets.
Design Specification:
- Service: OnboardingService (provision, assign tasks)
- Service: OffboardingService (revoke, collect assets)
- Integration with HRIS and asset modules
Sample Implementation:
```java
@Service
public class OnboardingService {
    public void onboardEmployee(Employee employee) {
        // Provision account, assign schedule, training, assets
    }
}
```
