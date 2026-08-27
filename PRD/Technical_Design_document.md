Section: Project Scaffolding & Domain Setup
Description: Establishes the foundational Spring Boot project structure, core modules, and baseline configurations to ensure consistency and maintainability.
Design Specification:
- Architecture: Modular Maven project with parent/child modules (core, employee, scheduling, attendance, safety).
- Package Structure: `com.warehouseems.{module}` (e.g., `com.warehouseems.employee`).
- Configuration: application.yml (port 8080, profiles), Flyway/Liquibase for DB migrations, Spring Boot Actuator enabled.
- Health Endpoint: `/actuator/health` returns UP.
- README: Includes build/run steps.
Sample Implementation:
```java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
# application.yml
server:
  port: 8080
spring:
  profiles:
    active: dev
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

Section: Employee Master Data (CRUD)
Description: Manages employee records with full CRUD support, enforcing unique badge IDs, soft deletes, and advanced filtering.
Design Specification:
- Entity: Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted).
- Repository: `EmployeeRepository extends JpaRepository<Employee, Long>`.
- Service: Business logic for CRUD, uniqueness, soft-delete.
- Controller: REST endpoints `/employees` with pagination/filtering.
- OpenAPI: Schemas with examples.
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
    @GetMapping public Page<EmployeeDTO> list(...) {...}
    @PostMapping public EmployeeDTO create(...) {...}
    // PUT, PATCH, DELETE endpoints
}
```

Section: Role Based Access Control (RBAC)
Description: Implements security using Spring Security with role-based and method-level access controls.
Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER.
- Security: Method/endpoint security, row-level constraints.
- Config: API key/OAuth2 toggle via properties.
- Automated tests for security rules.
Sample Implementation:
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .anyRequest().authenticated()
            .and().oauth2Login();
    }
}
@PreAuthorize("hasRole('ADMIN')")
public void deleteEmployee(Long id) {...}
```

Section: Time & Attendance (Clock In/Out)
Description: Captures clock-in/out events, validates shifts, and manages corrections with approval workflows.
Design Specification:
- Entity: AttendanceEvent (employeeId, timestamp, type, deviceId, location, status).
- Endpoints: `/attendance/clock-in`, `/attendance/clock-out`.
- Service: Validates events, calculates hours, triggers corrections.
- CSV export for reports.
Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private LocalDateTime timestamp;
    private String type; // CLOCK_IN, CLOCK_OUT
    private String deviceId, location, status;
}
@PostMapping("/attendance/clock-in")
public ResponseEntity<?> clockIn(@RequestBody ClockInDTO dto) {...}
```

Section: Shift & Schedule Management
Description: Manages shift templates, assignments, conflict detection, and audit logging.
Design Specification:
- Entities: ShiftTemplate, Schedule, Assignment.
- Endpoints: CRUD for shifts/schedules, bulk assignment.
- Service: Conflict detection, audit entries.
Sample Implementation:
```java
@Entity
public class ShiftTemplate { ... }
@Entity
public class Schedule { ... }
@PostMapping("/shifts/assign")
public ResponseEntity<?> assignShifts(@RequestBody AssignmentDTO dto) {...}
```

Section: Leave & Absence Management
Description: Handles leave requests, approvals, accruals, and integration with scheduling/payroll.
Design Specification:
- Entities: LeaveRequest, LeaveBalance.
- Endpoints: `/leave/requests` (CRUD).
- Service: Accrual logic, supervisor approval, shift flagging.
Sample Implementation:
```java
@Entity
public class LeaveRequest { ... }
@PostMapping("/leave/requests")
public ResponseEntity<?> requestLeave(@RequestBody LeaveRequestDTO dto) {...}
```

Section: Training & Certification Tracking
Description: Tracks employee certifications, expirations, and blocks unqualified assignments.
Design Specification:
- Entities: Certification, EmployeeCertification.
- Endpoints: CRUD for certifications, alerts for expiry.
- Service: Scheduling checks, document upload.
Sample Implementation:
```java
@Entity
public class Certification { ... }
@Entity
public class EmployeeCertification { ... }
```

Section: Safety Incidents & OSHA Reporting
Description: Records safety incidents, manages workflow, and supports OSHA reporting.
Design Specification:
- Entities: SafetyIncident (status, severity, location, involvedEmployees).
- Endpoints: `/safety/incidents` (CRUD, status workflow).
- Dashboard endpoints for metrics.
Sample Implementation:
```java
@Entity
public class SafetyIncident { ... }
@PostMapping("/safety/incidents")
public ResponseEntity<?> reportIncident(@RequestBody IncidentDTO dto) {...}
```

Section: Equipment & Asset Assignment
Description: Assigns and tracks equipment usage, enforces certification requirements, and logs history.
Design Specification:
- Entities: Asset, AssetAssignment.
- Endpoints: `/assets/checkout`, `/assets/checkin`.
- Service: Validates certifications, tracks overdue returns.
Sample Implementation:
```java
@Entity
public class Asset { ... }
@Entity
public class AssetAssignment { ... }
```

Section: Performance Reviews & Goals
Description: Manages performance review cycles, goal tracking, and immutable history after sign-off.
Design Specification:
- Entities: PerformanceReview, Goal.
- Endpoints: `/reviews` (CRUD, assign, submit, acknowledge).
- PDF export.
Sample Implementation:
```java
@Entity
public class PerformanceReview { ... }
@Entity
public class Goal { ... }
```

Section: Payroll Export Integration
Description: Generates payroll-ready files, reconciles totals, and logs exports for audit.
Design Specification:
- Service: Export logic, provider schema mapping, SFTP/API delivery, retry/backoff.
- Audit log for each export.
Sample Implementation:
```java
@Service
public class PayrollExportService { ... }
```

Section: Notifications & Announcements
Description: Sends notifications via multiple channels, tracks delivery, and enforces rate limits.
Design Specification:
- Entities: Notification, Announcement.
- Endpoints: `/notifications`, `/announcements`.
- Service: Channel opt-in/out, delivery tracking.
Sample Implementation:
```java
@Entity
public class Notification { ... }
@Entity
public class Announcement { ... }
```

Section: Integration Layer (HRIS/WMS APIs)
Description: Exposes and consumes APIs for HRIS/WMS, secures endpoints, and documents via OpenAPI.
Design Specification:
- REST APIs: `/api/hris`, `/api/wms`.
- Security: JWT/OAuth2.
- Webhooks: Idempotent event handling.
Sample Implementation:
```java
@RestController
@RequestMapping("/api/hris")
public class HRISController { ... }
```

Section: Audit Trail & Compliance
Description: Centralized, immutable audit logging for sensitive changes, exportable by filters.
Design Specification:
- Entity: AuditLog (actor, timestamp, entity, before/after).
- Service: Log on create/update/delete.
- Export endpoints.
Sample Implementation:
```java
@Entity
public class AuditLog { ... }
```

Section: Reporting & Analytics
Description: Provides operational reports, dashboards, and metrics endpoints for BI.
Design Specification:
- Endpoints: `/reports`, `/metrics`.
- Service: Filtering, export (CSV/PDF), access control.
Sample Implementation:
```java
@RestController
@RequestMapping("/reports")
public class ReportController { ... }
```

Section: Mobile Access (PWA)
Description: Enables mobile-friendly, offline-capable access to core features via PWA.
Design Specification:
- Frontend: Responsive views, PWA manifest, offline queue for clock events.
- Backend: Endpoints compatible with mobile flows.
Sample Implementation:
```yaml
# manifest.json for PWA
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone"
}
```

Section: Onboarding & Offboarding Workflow
Description: Automates onboarding/offboarding, triggers tasks, and updates schedules/assets.
Design Specification:
- Service: Listens for HRIS events, generates tasks, revokes access, updates schedules/assets.
- Endpoints: `/onboarding`, `/offboarding`.
Sample Implementation:
```java
@Service
public class OnboardingService { ... }
```
