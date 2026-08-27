Section: Initialize Spring Boot Project and Domain Setup  
Description: Establishes the foundational architecture for the Warehouse EMS system, ensuring all modules follow a standardized structure and enabling future extensibility.  
Design Specification:  
- Spring Boot (Maven) project initialization  
- Base package structure: `com.warehouse.ems`  
- Core modules: `employee`, `scheduling`, `attendance`, `safety`  
- DB migration: Flyway or Liquibase integration  
- Actuator enabled for health checks  
- README with build/run instructions  
- Application runs on port 8080  
Sample Implementation:  
```java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
// application.properties
server.port=8080
management.endpoints.web.exposure.include=health,info
spring.flyway.enabled=true
```

Section: Employee Master Data CRUD APIs  
Description: Provides RESTful endpoints for managing employee records, enforcing unique badge IDs, supporting soft deletes, and enabling pagination/filtering.  
Design Specification:  
- Entity: `Employee` (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)  
- Repository: `EmployeeRepository extends JpaRepository<Employee, Long>`  
- Service: `EmployeeService` for business logic  
- Controller: `EmployeeController` with CRUD endpoints  
- OpenAPI documentation with examples  
- Soft delete via `deleted` flag  
- Pagination and filtering via Spring Data  
Sample Implementation:  
```java
@Entity
public class Employee {
    @Id @GeneratedValue private Long id;
    @Column(unique = true) private String badgeId;
    private String name, role, department, shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted;
}
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    // CRUD endpoints using EmployeeService
}
```

Section: Implement Role Based Access Control (RBAC)  
Description: Secures endpoints and methods using Spring Security, supporting roles (ADMIN, HR, SUPERVISOR, WORKER) and configurable authentication (API key/OAuth2).  
Design Specification:  
- SecurityConfig with role-based access rules  
- Method/endpoint security annotations  
- API key/OAuth2 toggle via properties  
- Test coverage for security rules  
Sample Implementation:  
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR")
            .antMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
            .and()
            .oauth2Login(); // or API key filter
    }
}
```

Section: Time & Attendance Clock In/Out  
Description: Enables employees to clock in/out, capturing device and location, calculating hours, and supporting correction workflows.  
Design Specification:  
- Entity: `AttendanceEvent` (id, employeeId, type, timestamp, deviceId, location, status)  
- Endpoints: `/attendance/clock-in`, `/attendance/clock-out`  
- Service: `AttendanceService` for validation and calculation  
- Correction workflow triggers approval tasks  
- Exportable reports (CSV)  
Sample Implementation:  
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String type; // CLOCK_IN, CLOCK_OUT
    private LocalDateTime timestamp;
    private String deviceId, location, status;
}
@PostMapping("/attendance/clock-in")
public ResponseEntity<?> clockIn(@RequestBody ClockInRequest req) { ... }
```

Section: Shift & Schedule Management  
Description: Manages recurring shift templates, assignments, conflict detection, and audit logging for schedule changes.  
Design Specification:  
- Entities: `ShiftTemplate`, `Schedule`, `AuditEntry`  
- Bulk assignment and blackout date support  
- Conflict detection logic in service layer  
- Endpoints for CRUD and assignment  
Sample Implementation:  
```java
@Entity
public class ShiftTemplate { ... }
@Entity
public class Schedule { ... }
@PostMapping("/shifts/assign")
public ResponseEntity<?> assignShifts(@RequestBody AssignRequest req) { ... }
```

Section: Leave & Absence Management  
Description: Handles leave requests, approvals, balance tracking, and integration with scheduling and payroll.  
Design Specification:  
- Entity: `LeaveRequest` (id, employeeId, type, startDate, endDate, status, balance)  
- Supervisor approval workflow  
- Scheduled shifts flagged for coverage  
- Exportable leave reports  
Sample Implementation:  
```java
@Entity
public class LeaveRequest { ... }
@PostMapping("/leave/request")
public ResponseEntity<?> requestLeave(@RequestBody LeaveRequest req) { ... }
```

Section: Training & Certification Tracking  
Description: Tracks employee certifications, expirations, and blocks assignments for unqualified staff.  
Design Specification:  
- Entity: `Certification` (id, employeeId, type, expiryDate, status, documentUrl)  
- Alerts for expiring certifications  
- Assignment checks in scheduling logic  
Sample Implementation:  
```java
@Entity
public class Certification { ... }
@Scheduled(cron = "0 0 * * * ?")
public void sendExpiryAlerts() { ... }
```

Section: Safety Incidents & OSHA Reporting  
Description: Records safety incidents, supports investigation workflow, and generates OSHA-compliant exports.  
Design Specification:  
- Entity: `SafetyIncident` (id, type, severity, location, description, status, involvedEmployees)  
- Status workflow: Open, Investigating, Resolved  
- Export endpoints for OSHA reports  
Sample Implementation:  
```java
@Entity
public class SafetyIncident { ... }
@PostMapping("/safety/incidents")
public ResponseEntity<?> reportIncident(@RequestBody IncidentRequest req) { ... }
```

Section: Equipment & Asset Assignment  
Description: Assigns and tracks equipment, validates certifications, and logs asset history.  
Design Specification:  
- Entity: `Asset` (id, type, condition, assignedTo, checkoutDate, returnDate)  
- Certification validation on checkout  
- Overdue return reports  
Sample Implementation:  
```java
@Entity
public class Asset { ... }
@PostMapping("/assets/checkout")
public ResponseEntity<?> checkoutAsset(@RequestBody CheckoutRequest req) { ... }
```

Section: Performance Reviews & Goals  
Description: Supports structured performance reviews, goal tracking, and immutable history after sign-off.  
Design Specification:  
- Entity: `PerformanceReview` (id, employeeId, cycle, goals, ratings, comments, status)  
- Workflow for submission and acknowledgement  
- PDF export support  
Sample Implementation:  
```java
@Entity
public class PerformanceReview { ... }
@PostMapping("/reviews/submit")
public ResponseEntity<?> submitReview(@RequestBody ReviewRequest req) { ... }
```

Section: Payroll Export Integration  
Description: Generates payroll-ready exports from attendance and leave data, with secure delivery and audit logging.  
Design Specification:  
- Export service mapping to provider schema  
- Secure delivery (SFTP/API)  
- Retry logic for failures  
- Audit log for each export  
Sample Implementation:  
```java
@Service
public class PayrollExportService {
    public void exportPayroll() { ... }
}
```

Section: Notifications & Announcements  
Description: Delivers notifications via in-app, email, or SMS, with user preferences and quiet hours.  
Design Specification:  
- Entity: `Notification` (id, userId, type, channel, status, sentAt)  
- User preferences and opt-in/out  
- Delivery status tracking  
Sample Implementation:  
```java
@Entity
public class Notification { ... }
public void sendNotification(Notification n) { ... }
```

Section: Integration Layer for HRIS/WMS APIs  
Description: Exposes and consumes REST APIs for HRIS/WMS, supporting secure synchronization and idempotent webhooks.  
Design Specification:  
- REST controllers for HRIS/WMS endpoints  
- JWT/OAuth2 security  
- Sync jobs for employee data  
- OpenAPI documentation  
Sample Implementation:  
```java
@RestController
@RequestMapping("/api/hris")
public class HRISController { ... }
```

Section: Audit Trail & Compliance Logging  
Description: Centralized, tamper-evident logging for sensitive changes, with export and test coverage.  
Design Specification:  
- Entity: `AuditLog` (id, entity, action, actor, timestamp, before, after, immutable)  
- Export endpoints by date/user/entity  
Sample Implementation:  
```java
@Entity
public class AuditLog { ... }
public void logChange(AuditLog log) { ... }
```

Section: Reporting & Analytics  
Description: Generates operational reports and dashboards, supporting CSV/PDF export and BI integration.  
Design Specification:  
- Report generation service  
- Export endpoints with access control  
- Metrics endpoints for BI tools  
Sample Implementation:  
```java
@GetMapping("/reports/attendance")
public ResponseEntity<?> exportAttendanceReport(...) { ... }
```

Section: Mobile Access via PWA  
Description: Provides a mobile-friendly, offline-capable PWA for core workflows.  
Design Specification:  
- Responsive UI (Thymeleaf/React/Vue)  
- Service worker for offline support  
- Installable manifest  
- Offline event queue and sync logic  
Sample Implementation:  
```javascript
// manifest.json and service-worker.js for PWA
```

Section: Onboarding & Offboarding Workflow  
Description: Automates provisioning and deprovisioning of accounts, schedules, training, and asset collection.  
Design Specification:  
- Workflow engine for onboarding/offboarding  
- Integration with HRIS, scheduling, training, and asset modules  
- Task generation and tracking  
Sample Implementation:  
```java
@Service
public class OnboardingService {
    public void runOnboardingWorkflow(NewHireEvent event) { ... }
}
```

---

**GitHub Upload Status:**  
The above technical design document is ready for upload.  
Proceeding to upload as `PRD/Technical_Design_document.md` with commit message: "Initial commit of comprehensive low-level technical design document for Warehouse EMS."