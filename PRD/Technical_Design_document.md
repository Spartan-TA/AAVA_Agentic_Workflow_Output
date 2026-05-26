Section: Project Scaffolding & Domain Setup
Description: Establishes the foundational Spring Boot project structure, core modules, and database migration setup for all subsequent features.
Design Specification:
- Spring Boot (Maven) project initialization
- Base package: `com.warehouse.ems`
- Core modules: `employee`, `scheduling`, `attendance`, `safety`
- DB migration: Flyway or Liquibase
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

Section: Employee Master Data (CRUD)
Description: Implements CRUD APIs for employee records, enforcing unique badge IDs, soft deletion, and OpenAPI documentation.
Design Specification:
- Entity: `Employee` (fields: id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)
- Repository: `EmployeeRepository` (extends JpaRepository)
- Service: `EmployeeService` (business logic, badgeId uniqueness)
- Controller: `EmployeeController` (REST endpoints, pagination/filtering)
- Soft-delete via `deleted` flag
- OpenAPI annotations for all endpoints
Sample Implementation:
```java
@Entity
public class Employee {
    @Id @GeneratedValue private Long id;
    @Column(unique=true) private String badgeId;
    private String name, role, department, shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted = false;
}
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    // CRUD endpoints with pagination/filtering
}
```

Section: Role Based Access Control (RBAC)
Description: Secures endpoints and methods using Spring Security with role-based and row-level access, supporting API key/OAuth2 toggle.
Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- SecurityConfig: method/endpoint security, API key/OAuth2 toggle
- Row-level constraints in service layer
- Security tests for all rules
Sample Implementation:
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .anyRequest().authenticated()
            .and().oauth2Login(); // or API key filter
    }
}
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.team == principal.team)")
public void updateEmployee(Employee employee) { ... }
```

Section: Time & Attendance (Clock In/Out)
Description: Provides endpoints for clock-in/out events, device/geofence capture, shift association, and correction workflows.
Design Specification:
- Entity: `AttendanceEvent` (employeeId, timestamp, type, deviceId, location, shiftId, status)
- Controller: `/attendance/clock-in`, `/attendance/clock-out`
- Service: validation, shift association, daily totals, correction workflow
- Exportable reports (CSV)
Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private LocalDateTime timestamp;
    private String type; // CLOCK_IN, CLOCK_OUT
    private String deviceId, location;
    private Long shiftId;
    private String status; // NORMAL, CORRECTION_PENDING
}
@PostMapping("/attendance/clock-in")
public ResponseEntity<?> clockIn(@RequestBody ClockEventDto dto) { ... }
```

Section: Shift & Schedule Management
Description: Manages recurring shift templates, assignments, blackout dates, and conflict detection.
Design Specification:
- Entities: `ShiftTemplate`, `ShiftAssignment`, `BlackoutDate`
- Controller: CRUD for shifts, bulk assignment, conflict detection
- Service: audit logging, overtime rules
Sample Implementation:
```java
@Entity
public class ShiftTemplate { ... }
@Entity
public class ShiftAssignment { ... }
@PostMapping("/shifts/assign")
public ResponseEntity<?> assignShifts(@RequestBody AssignShiftsDto dto) { ... }
```

Section: Leave & Absence Management
Description: Handles leave requests, approvals, accrual balances, and integration with scheduling/payroll.
Design Specification:
- Entity: `LeaveRequest` (employeeId, type, startDate, endDate, status, balance)
- Controller: request/approve endpoints, export
- Service: balance updates, shift coverage flagging
Sample Implementation:
```java
@Entity
public class LeaveRequest { ... }
@PostMapping("/leave/request")
public ResponseEntity<?> requestLeave(@RequestBody LeaveRequestDto dto) { ... }
```

Section: Training & Certification Tracking
Description: Tracks employee certifications, expirations, and blocks unqualified assignments.
Design Specification:
- Entity: `Certification` (employeeId, type, expiryDate, proofDocument)
- Controller: CRUD, alert endpoints
- Service: expiry alerts, assignment checks
Sample Implementation:
```java
@Entity
public class Certification { ... }
@Scheduled(cron = "0 0 * * * ?")
public void sendExpiryAlerts() { ... }
```

Section: Safety Incidents & OSHA Reporting
Description: Records safety incidents, manages workflow, and generates OSHA reports.
Design Specification:
- Entity: `SafetyIncident` (fields: id, date, location, description, severity, status, involvedEmployees)
- Controller: incident CRUD, export endpoints
- Service: workflow, metrics dashboard
Sample Implementation:
```java
@Entity
public class SafetyIncident { ... }
@PostMapping("/safety/incidents")
public ResponseEntity<?> reportIncident(@RequestBody IncidentDto dto) { ... }
```

Section: Equipment & Asset Assignment
Description: Assigns and tracks equipment/assets, validates certifications, and logs history.
Design Specification:
- Entity: `Asset`, `AssetAssignment`, `AssetHistory`
- Controller: asset CRUD, check-in/out endpoints
- Service: overdue reporting, condition tracking
Sample Implementation:
```java
@Entity
public class Asset { ... }
@Entity
public class AssetAssignment { ... }
@PostMapping("/assets/check-out")
public ResponseEntity<?> checkOutAsset(@RequestBody AssetAssignmentDto dto) { ... }
```

Section: Performance Reviews & Goals
Description: Manages performance review cycles, goals, and immutable history after sign-off.
Design Specification:
- Entity: `PerformanceReview` (employeeId, cycle, goals, ratings, comments, status)
- Controller: review CRUD, PDF export
- Service: workflow, role-based visibility
Sample Implementation:
```java
@Entity
public class PerformanceReview { ... }
@PostMapping("/reviews/submit")
public ResponseEntity<?> submitReview(@RequestBody ReviewDto dto) { ... }
```

Section: Payroll Export Integration
Description: Exports payroll-ready files, reconciles totals, and supports secure delivery.
Design Specification:
- Service: payroll export, SFTP/API delivery, audit log, retry logic
- Controller: export endpoints
Sample Implementation:
```java
@PostMapping("/payroll/export")
public ResponseEntity<?> exportPayroll(@RequestBody ExportRequestDto dto) { ... }
```

Section: Notifications & Announcements
Description: Sends notifications/announcements, supports opt-in/out, templates, and rate limiting.
Design Specification:
- Entity: `Notification`, `Announcement`
- Controller: send/opt-in-out endpoints
- Service: delivery tracking, quiet hours config
Sample Implementation:
```java
@Entity
public class Notification { ... }
@PostMapping("/notifications/send")
public ResponseEntity<?> sendNotification(@RequestBody NotificationDto dto) { ... }
```

Section: Integration Layer (HRIS/WMS APIs)
Description: Exposes REST APIs and connectors for HRIS, WMS, and SSO; supports webhooks and OpenAPI docs.
Design Specification:
- Controller: `/api/hris`, `/api/wms`, `/api/webhooks`
- Security: JWT/OAuth2
- Service: sync jobs, idempotency
Sample Implementation:
```java
@RestController
@RequestMapping("/api/hris")
public class HrisController { ... }
```

Section: Audit Trail & Compliance
Description: Centralized audit logging for sensitive changes, tamper-evident storage, and export capabilities.
Design Specification:
- Entity: `AuditLog` (actor, timestamp, entity, before, after)
- Service: log creation, export endpoints, immutability enforcement
Sample Implementation:
```java
@Entity
public class AuditLog { ... }
public void logChange(String actor, Object before, Object after) { ... }
```

Section: Reporting & Analytics
Description: Provides operational reports, dashboards, and export features with access control.
Design Specification:
- Controller: report generation/export endpoints
- Service: metrics, CSV/PDF export, role-based dashboards
Sample Implementation:
```java
@GetMapping("/reports/attendance")
public ResponseEntity<?> getAttendanceReport(@RequestParam Map<String, String> filters) { ... }
```

Section: Mobile Access (PWA)
Description: Delivers responsive mobile views for core flows, offline queue for clock events, and PWA manifest.
Design Specification:
- Frontend: PWA manifest, service worker for offline support
- Backend: endpoints for mobile flows, conflict resolution
Sample Implementation:
```json
// manifest.json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone"
}
```

Section: Onboarding & Offboarding Workflow
Description: Automates onboarding/offboarding tasks, asset/training assignment, and access provisioning.
Design Specification:
- Service: HRIS integration, task generation, access/asset management
- Controller: onboarding/offboarding endpoints
Sample Implementation:
```java
@PostMapping("/onboarding/start")
public ResponseEntity<?> startOnboarding(@RequestBody OnboardingDto dto) { ... }
```
