# Warehouse Employee Management System - Low Level Technical Design Document

---

Section: E01 - Project Scaffolding & Domain Setup
Description: Establishes the foundational Spring Boot project structure, core modules, and essential configurations for all subsequent development.
Design Specification:
- Spring Boot (Maven) project initialized
- Base packages: com.company.wems (core, employee, scheduling, attendance, safety)
- Modules: core, employee, scheduling, attendance, safety
- DB migration: Flyway/Liquibase
- Monitoring: Spring Boot Actuator
- README with build/run steps
- Port: 8080
Sample Implementation:
```java
// Maven pom.xml with modules
// src/main/java/com/company/wems/Application.java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

---

Section: E02 - Employee Master Data (CRUD)
Description: Implements CRUD operations for Employee domain, ensuring unique badgeId, soft-delete, pagination, filtering, and OpenAPI documentation.
Design Specification:
- Entity: Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService (CRUD, soft-delete, filtering)
- Controller: EmployeeController (REST endpoints)
- OpenAPI schemas
Sample Implementation:
```java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    private String name;
    @Column(unique = true)
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted;
    // getters/setters
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @GetMapping
    public Page<EmployeeDTO> list(Pageable pageable, @RequestParam Map<String, String> filters) { ... }
    @PostMapping
    public EmployeeDTO create(@RequestBody EmployeeDTO dto) { ... }
    // PUT, PATCH, DELETE endpoints
}
```

---

Section: E03 - Role Based Access Control (RBAC)
Description: Secures endpoints and methods using Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER), supports API key/OAuth2 toggle, and row-level constraints.
Design Specification:
- SecurityConfig: role hierarchy, method security
- UserDetailsService for authentication
- API key/OAuth2 toggle via application.yml
- Row-level security in repositories/services
Sample Implementation:
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .anyRequest().authenticated()
            .and().oauth2Login(); // or API key
    }
}
```

---

Section: E04 - Time & Attendance (Clock In/Out)
Description: Provides endpoints for clock-in/out, geofence/device capture, shift association, missed punch correction workflow, and CSV export.
Design Specification:
- Entity: AttendanceEvent (id, employee, type, timestamp, deviceId, location, shift, status)
- Service: AttendanceService (clock-in/out, corrections, totals)
- Controller: AttendanceController (REST endpoints)
- Approval workflow for corrections
Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type; // CLOCK_IN, CLOCK_OUT
    private LocalDateTime timestamp;
    private String deviceId;
    private String location;
    @ManyToOne
    private Shift shift;
    private String status; // NORMAL, CORRECTION_PENDING
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockEventDTO dto) { ... }
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockEventDTO dto) { ... }
}
```

---

Section: E05 - Shift & Schedule Management
Description: Manages shift templates, rotations, overtime rules, employee assignments, blackout dates, and operation calendars.
Design Specification:
- Entity: ShiftTemplate, ShiftAssignment, BlackoutDate
- Service: ShiftService (CRUD, conflict detection, bulk-assign)
- Controller: ShiftController (REST endpoints)
- Audit entries on changes
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
    // ...
}

@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping
    public ShiftTemplateDTO create(@RequestBody ShiftTemplateDTO dto) { ... }
    // GET, PUT, DELETE, bulk-assign endpoints
}
```

---

Section: E06 - Leave & Absence Management
Description: Handles PTO/sick/unpaid leave requests, approvals, accruals, and integration with scheduling/payroll.
Design Specification:
- Entity: LeaveRequest (id, employee, type, startDate, endDate, status, approver)
- Service: LeaveService (request, approve/deny, update balances)
- Controller: LeaveController (REST endpoints)
- Integration hooks for scheduling/payroll
Sample Implementation:
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
    private String status; // PENDING, APPROVED, DENIED
    private String approver;
}

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping
    public LeaveRequestDTO requestLeave(@RequestBody LeaveRequestDTO dto) { ... }
    @PatchMapping("/{id}/approve")
    public void approve(@PathVariable Long id) { ... }
}
```

---

Section: E07 - Training & Certification Tracking
Description: Tracks certifications, expirations, renewals, blocks assignments for expired certs, and uploads proof documents.
Design Specification:
- Entity: Certification (id, employee, type, issueDate, expiryDate, documentUrl)
- Service: CertificationService (CRUD, expiry alerts, scheduling checks)
- Controller: CertificationController (REST endpoints)
Sample Implementation:
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
}

@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @GetMapping("/expiring-soon")
    public List<CertificationDTO> expiringSoon() { ... }
}
```

---

Section: E08 - Safety Incidents & OSHA Reporting
Description: Records safety incidents, manages investigation workflow, and generates OSHA summaries.
Design Specification:
- Entity: SafetyIncident (id, date, location, description, severity, status, involvedEmployees)
- Service: SafetyService (CRUD, workflow, reporting)
- Controller: SafetyController (REST endpoints)
Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private LocalDate date;
    private String location;
    private String description;
    private String severity;
    private String status; // OPEN, INVESTIGATING, RESOLVED
    @ManyToMany
    private List<Employee> involvedEmployees;
}

@RestController
@RequestMapping("/safety/incidents")
public class SafetyController {
    @PostMapping
    public SafetyIncidentDTO report(@RequestBody SafetyIncidentDTO dto) { ... }
}
```

---

Section: E09 - Equipment & Asset Assignment
Description: Assigns assets to employees, tracks check-in/out, blocks use if certs missing, maintains asset condition.
Design Specification:
- Entity: Asset (id, type, serial, condition, assignedTo, checkOutDate, checkInDate)
- Service: AssetService (CRUD, check-in/out, history)
- Controller: AssetController (REST endpoints)
Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serial;
    private String condition;
    @ManyToOne
    private Employee assignedTo;
    private LocalDateTime checkOutDate;
    private LocalDateTime checkInDate;
}

@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/checkout")
    public void checkout(@RequestBody AssetCheckoutDTO dto) { ... }
}
```

---

Section: E10 - Performance Reviews & Goals
Description: Manages review templates, goals, competencies, ratings, comments, and supervisor/employee acknowledgements.
Design Specification:
- Entity: PerformanceReview (id, employee, period, goals, ratings, comments, status)
- Service: ReviewService (CRUD, workflow, PDF export)
- Controller: ReviewController (REST endpoints)
Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String period;
    private String goals;
    private String ratings;
    private String comments;
    private String status; // DRAFT, SUBMITTED, ACKNOWLEDGED
}

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping
    public PerformanceReviewDTO create(@RequestBody PerformanceReviewDTO dto) { ... }
}
```

---

Section: E11 - Payroll Export Integration
Description: Generates payroll-ready files from attendance/leave, maps to provider formats, and delivers securely.
Design Specification:
- Service: PayrollExportService (generate, map, deliver, retry)
- Integration: SFTP/API delivery
- Audit log for exports
Sample Implementation:
```java
@Service
public class PayrollExportService {
    public void exportPayroll(LocalDate period) { ... }
}
```

---

Section: E12 - Notifications & Announcements
Description: Sends in-app/email/SMS notifications for events, supports opt-in/out, templates, localization, and rate limits.
Design Specification:
- Entity: Notification (id, user, type, channel, content, status, deliveryDate)
- Service: NotificationService (send, track, rate limit)
- Controller: NotificationController (REST endpoints)
Sample Implementation:
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee user;
    private String type;
    private String channel;
    private String content;
    private String status;
    private LocalDateTime deliveryDate;
}

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping
    public void send(@RequestBody NotificationDTO dto) { ... }
}
```

---

Section: E13 - Integration Layer (HRIS/WMS APIs)
Description: Exposes REST APIs and connectors for HRIS, WMS, and IDP; supports webhooks and SSO.
Design Specification:
- Integration: HRISConnector, WMSConnector, IDPConnector
- Security: JWT/OAuth2
- Webhook endpoints
- OpenAPI documentation
Sample Implementation:
```java
@RestController
@RequestMapping("/api/integrations")
public class IntegrationController {
    @PostMapping("/hris/webhook")
    public void hrisWebhook(@RequestBody HRISPayload payload) { ... }
}
```

---

Section: E14 - Audit Trail & Compliance
Description: Centralized audit logging for sensitive changes, tamper-evident storage, export, and coverage tests.
Design Specification:
- Entity: AuditLog (id, entity, entityId, actor, timestamp, before, after)
- Service: AuditService (log, export)
- Immutable log table
Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private Long entityId;
    private String actor;
    private LocalDateTime timestamp;
    @Lob
    private String before;
    @Lob
    private String after;
}
```

---

Section: E15 - Reporting & Analytics
Description: Provides operational reports, exports, dashboards, and metrics endpoints for BI.
Design Specification:
- Service: ReportingService (attendance, overtime, leave, certs, safety)
- Controller: ReportingController (REST endpoints, CSV/PDF export)
- Role-based access
Sample Implementation:
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> attendanceReport(...) { ... }
}
```

---

Section: E16 - Mobile Access (PWA)
Description: Delivers responsive PWA views for clock-in/out, schedules, leave, announcements, and offline support.
Design Specification:
- Frontend: PWA manifest, service worker
- Backend: REST APIs for mobile flows
- Offline queue for clock events
Sample Implementation:
```javascript
// manifest.json
{
  "short_name": "WEMS",
  "start_url": "/",
  "display": "standalone",
  ...
}
```

---

Section: E17 - Onboarding & Offboarding Workflow
Description: Automates provisioning, initial schedule, training, asset assignment, and deprovisioning on termination.
Design Specification:
- Service: OnboardingService, OffboardingService
- Integration: HRIS, Asset, Schedule, Training modules
- Task generation and tracking
Sample Implementation:
```java
@Service
public class OnboardingService {
    public void onboardNewHire(Employee employee) { ... }
}
```

---

Section: E18 - Localization & Multi-Warehouse
Description: Supports multiple warehouses, localization of UI/notifications, and warehouse-specific data segregation.
Design Specification:
- Entity: Warehouse (id, name, location)
- Employee, Shift, Asset reference Warehouse
- Locale settings in user profile
Sample Implementation:
```java
@Entity
public class Warehouse {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String location;
}

// Employee references Warehouse
```

---

Section: E19 - AI-Assisted Scheduling
Description: Integrates AI/ML for shift optimization, demand prediction, and conflict resolution.
Design Specification:
- Service: SchedulingAIService (suggest, optimize)
- Integration: External AI/ML API
- Controller: SchedulingAIController
Sample Implementation:
```java
@Service
public class SchedulingAIService {
    public List<ShiftAssignment> suggestOptimalSchedule(...) { ... }
}
```

---

Section: E20 - Document Management
Description: Manages upload, storage, and retrieval of employee-related documents (certs, reviews, incidents).
Design Specification:
- Entity: Document (id, owner, type, url, uploadedAt)
- Service: DocumentService (upload, retrieve, delete)
- Controller: DocumentController (REST endpoints)
Sample Implementation:
```java
@Entity
public class Document {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee owner;
    private String type;
    private String url;
    private LocalDateTime uploadedAt;
}

@RestController
@RequestMapping("/documents")
public class DocumentController {
    @PostMapping
    public void upload(@RequestBody MultipartFile file) { ... }
}
```

---

# End of Document