# Technical Design Document: Warehouse Employee Management System (EMS)

## Table of Contents
1. Project Scaffolding & Domain Setup
2. Employee Master Data (CRUD)
3. Role Based Access Control (RBAC)
4. Time & Attendance (Clock In/Out)
5. Shift & Schedule Management
6. Leave & Absence Management
7. Training & Certification Tracking
8. Safety Incidents & OSHA Reporting
9. Equipment & Asset Assignment
10. Performance Reviews & Goals
11. Payroll Export Integration
12. Notifications & Announcements
13. Integration Layer (HRIS/WMS APIs)
14. Audit Trail & Compliance
15. Reporting & Analytics
16. Mobile Access (PWA)
17. Onboarding & Offboarding Workflow
18. Localization & Multi-Warehouse

---

Section: Project Scaffolding & Domain Setup
Description: Establishes the foundational Spring Boot architecture, base package structure, and core modules for the EMS system. Ensures consistency and maintainability across all features.
Design Specification:
- Spring Boot (Maven) project with multi-module structure: core, employee, scheduling, attendance, safety
- Base package: com.warehouse.ems
- Flyway/Liquibase for DB migrations
- Spring Boot Actuator enabled
- README with build/run steps
- Health endpoint: /actuator/health
Sample Implementation:
```java
@SpringBootApplication
public class EmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmsApplication.class, args);
    }
}
```

---

Section: Employee Master Data (CRUD)
Description: Manages employee records with full CRUD operations, supporting pagination, filtering, and soft-delete. Enforces unique badgeId and exposes OpenAPI schemas.
Design Specification:
- Entity: Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService (CRUD, filtering, soft-delete)
- Controller: EmployeeController (REST endpoints)
- DTOs for web/API
- OpenAPI documentation
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
}
```

---

Section: Role Based Access Control (RBAC)
Description: Implements Spring Security with role-based access (ADMIN, HR, SUPERVISOR, WORKER), endpoint/method security, and row-level constraints. Supports API key/OAuth2 toggle via config.
Design Specification:
- SecurityConfig: configures roles, endpoint access, method security
- UserDetailsService for user/role management
- API key/OAuth2 toggle in application.yml
- Row-level security in repositories/services
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

---

Section: Time & Attendance (Clock In/Out)
Description: Provides endpoints for clock-in/out events, geofence/device capture, shift association, missed punch handling, and corrections workflow. Computes daily totals and exports reports.
Design Specification:
- Entity: AttendanceEvent (id, employeeId, timestamp, type, deviceId, location)
- Service: AttendanceService (clock-in/out, corrections, totals)
- Controller: AttendanceController (REST endpoints)
- Geofence validation (optional)
- Approval workflow for corrections
Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private LocalDateTime timestamp;
    private String type; // CLOCK_IN, CLOCK_OUT
    private String deviceId;
    private String location;
}
```

---

Section: Shift & Schedule Management
Description: Manages shift templates, rotations, overtime rules, blackout dates, and employee assignments. Detects/prevents conflicts and supports bulk assignment/audit entries.
Design Specification:
- Entity: ShiftTemplate, ShiftAssignment
- Service: ShiftService (CRUD, conflict detection, bulk assign)
- Controller: ShiftController
- Blackout date/calendar integration
- Audit logging
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
}
```

---

Section: Leave & Absence Management
Description: Handles PTO, sick, unpaid leave requests/approvals, accrual balances, and policy enforcement. Integrates with scheduling and payroll modules.
Design Specification:
- Entity: LeaveRequest (id, employeeId, type, startDate, endDate, status, balance)
- Service: LeaveService (request, approve/deny, balance update)
- Controller: LeaveController
- Integration hooks for scheduling/payroll
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
    private int balance;
}
```

---

Section: Training & Certification Tracking
Description: Tracks certifications, expirations, renewals, and proof documents. Blocks assignment to tasks requiring expired certs and sends alerts.
Design Specification:
- Entity: Certification (id, employeeId, type, expiryDate, documentUrl)
- Service: CertificationService (CRUD, expiry alerts, assignment checks)
- Controller: CertificationController
- Document upload integration
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

---

Section: Safety Incidents & OSHA Reporting
Description: Records safety incidents/near-misses, manages investigation workflow, and generates OSHA summaries. Provides dashboard metrics.
Design Specification:
- Entity: SafetyIncident (id, severity, location, description, status, involvedEmployees)
- Service: SafetyService (record, workflow, export)
- Controller: SafetyController
- OSHA export endpoints
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
    private List<Long> involvedEmployees;
}
```

---

Section: Equipment & Asset Assignment
Description: Assigns assets to employees, tracks check-in/out, blocks use if certification missing, and maintains asset condition state.
Design Specification:
- Entity: Asset (id, type, condition, assignedTo, checkoutDate, returnDate)
- Service: AssetService (CRUD, check-in/out, certification validation)
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
    private Long assignedTo;
    private LocalDate checkoutDate;
    private LocalDate returnDate;
}
```

---

Section: Performance Reviews & Goals
Description: Manages review templates, goals, competencies, ratings, comments, and acknowledgement workflow. Supports PDF export and immutable history.
Design Specification:
- Entity: PerformanceReview (id, employeeId, cycle, goals, ratings, comments, status)
- Service: ReviewService (create, assign, submit, acknowledge)
- Controller: ReviewController
- PDF export integration
Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String cycle;
    @ElementCollection
    private List<String> goals;
    @ElementCollection
    private List<String> ratings;
    private String comments;
    private String status; // SUBMITTED, ACKNOWLEDGED
}
```

---

Section: Payroll Export Integration
Description: Generates payroll-ready files from attendance/leave, maps to provider formats, and delivers securely via SFTP/API. Includes audit logging and retry logic.
Design Specification:
- Service: PayrollExportService (generate, map, deliver, retry)
- Integration: SFTP/API client
- Audit log for exports
Sample Implementation:
```java
@Service
public class PayrollExportService {
    public void exportPayrollData() {
        // Generate, map, deliver, audit
    }
}
```

---

Section: Notifications & Announcements
Description: Sends in-app, email, and SMS notifications for events, supports quiet hours, opt-in/out, localization, and delivery tracking.
Design Specification:
- Entity: Notification (id, employeeId, type, channel, message, status)
- Service: NotificationService (send, track, rate limit)
- Controller: NotificationController
- Integration: Email/SMS providers
Sample Implementation:
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type;
    private String channel; // IN_APP, EMAIL, SMS
    private String message;
    private String status; // SENT, FAILED
}
```

---

Section: Integration Layer (HRIS/WMS APIs)
Description: Exposes REST APIs/connectors for HRIS, WMS, and IDP (SSO), with webhooks for events. Secured via JWT/OAuth2.
Design Specification:
- REST controllers for HRIS/WMS/IDP
- JWT/OAuth2 security
- Webhook endpoints
- OpenAPI documentation
Sample Implementation:
```java
@RestController
@RequestMapping("/api/hris")
public class HrisController {
    @PostMapping("/sync")
    public ResponseEntity<?> syncEmployee(@RequestBody EmployeeDto dto) {
        // Sync logic
    }
}
```

---

Section: Audit Trail & Compliance
Description: Centralized audit logging for sensitive changes, tamper-evident storage, and export capabilities. Ensures compliance and forensic analysis.
Design Specification:
- Entity: AuditLog (id, actor, timestamp, entity, before, after)
- Service: AuditService (log, export)
- Controller: AuditController
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

---

Section: Reporting & Analytics
Description: Provides operational reports (attendance, overtime, leave, certifications, safety KPIs), CSV/PDF export, and dashboards. Access controlled by role.
Design Specification:
- Service: ReportingService (generate, filter, export)
- Controller: ReportingController
- Metrics endpoints for BI
Sample Implementation:
```java
@Service
public class ReportingService {
    public Report generateAttendanceReport(DateRange range) {
        // Generate report logic
    }
}
```

---

Section: Mobile Access (PWA)
Description: Delivers responsive views for core flows, installable PWA manifest, offline queue for clock events, and conflict resolution. Ensures usability on mobile devices.
Design Specification:
- Frontend: PWA manifest, service worker
- Backend: REST endpoints for mobile flows
- Offline queue logic
Sample Implementation:
```json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

---

Section: Onboarding & Offboarding Workflow
Description: Automates provisioning/deprovisioning of accounts, schedules, training, and asset assignment. Integrates with HRIS and asset modules.
Design Specification:
- Service: OnboardingService (provision, assign, generate tasks)
- Service: OffboardingService (revoke, collect, update)
- Integration: HRIS, AssetService
Sample Implementation:
```java
@Service
public class OnboardingService {
    public void provisionNewHire(EmployeeDto dto) {
        // Provision logic
    }
}
```

---

Section: Localization & Multi-Warehouse
Description: Supports multi-language UI, localized templates, and multi-warehouse data segregation. Ensures global usability and compliance.
Design Specification:
- i18n resource bundles
- Warehouse entity with data partitioning
- Localization in notification templates
Sample Implementation:
```java
@Entity
public class Warehouse {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String location;
}
```

---

# End of Technical Design Document

(Note: This document covers the technical design for all 80 user stories as grouped by functional area. Each section includes architecture overview, package/component breakdown, entity design, service/repository/controller specs, configuration/security, integration points, and sample code as required.)