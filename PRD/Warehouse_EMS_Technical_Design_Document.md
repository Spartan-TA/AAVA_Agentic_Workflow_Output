## Warehouse EMS System â Low-Level Technical Design Document (Spring Boot)

### Overview
This document provides a comprehensive technical design for the Warehouse EMS system, covering all 80 user stories grouped into 17 epics. It adheres to Spring Boot best practices and is formatted for easy consumption by developers. Each epic/module includes: architecture overview, package structure, entity design, service/repository/controller specs, configuration/security, integration points, and code snippets.

---

### E01: Project Scaffolding & Domain Setup (Stories 1-5)

**Spring Boot Architecture:**  
- Maven project, Java 17+, Spring Boot 3.x  
- Modules: employee, scheduling, attendance, safety, etc.  
- DB migration: Flyway/Liquibase  
- Monitoring: Actuator

**Package Structure:**  
```
com.warehouseems
  âââ config
  âââ employee
  âââ attendance
  âââ shift
  âââ leave
  âââ training
  âââ safety
  âââ asset
  âââ review
  âââ payroll
  âââ notification
  âââ integration
  âââ audit
  âââ report
  âââ mobile
  âââ onboarding
```

**Configuration Example:**  
```java
// src/main/resources/application.yml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouseems
    username: ems
    password: secret
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

**Actuator Health Endpoint:**  
- `/actuator/health` returns `UP`

**DB Migration:**  
- Flyway/Liquibase baseline migration scripts in `src/main/resources/db/migration`

---

### E02: Employee Master Data (Stories 6-10)

**Entity Design:**  
```java
@Entity
@Table(name = "employees")
public class Employee {
  @Id @GeneratedValue private Long id;
  @Column(unique = true, nullable = false) private String badgeId;
  private String name;
  @Enumerated(EnumType.STRING) private Role role;
  private String department;
  private String shiftGroup;
  private LocalDate hireDate;
  @Enumerated(EnumType.STRING) private Status status;
  // audit fields
}
```

**Repository:**  
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  Optional<Employee> findByBadgeId(String badgeId);
  Page<Employee> findAll(Specification<Employee> spec, Pageable pageable);
}
```

**Service Layer:**  
```java
public interface EmployeeService {
  Employee create(EmployeeDto dto);
  Employee update(Long id, EmployeeDto dto);
  void delete(Long id);
  Page<Employee> list(EmployeeFilter filter, Pageable pageable);
}
```

**Controller:**  
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
  @PostMapping public ResponseEntity<EmployeeDto> create(@RequestBody EmployeeDto dto);
  @GetMapping public Page<EmployeeDto> list(...);
  @GetMapping("/{id}") public EmployeeDto get(@PathVariable Long id);
  @PutMapping("/{id}") public EmployeeDto update(@PathVariable Long id, @RequestBody EmployeeDto dto);
  @DeleteMapping("/{id}") public void delete(@PathVariable Long id);
}
```

**Features:**  
- Unique badgeId enforced  
- Soft-delete (status = INACTIVE)  
- Pagination/filtering via Spring Data  
- OpenAPI schemas

---

### E03: Role Based Access Control (Stories 11-15)

**Spring Security Config:**  
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .authorizeRequests()
        .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
        .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
        .anyRequest().authenticated()
      .and()
        .oauth2ResourceServer().jwt();
  }
}
```

**Role Enum:**  
```java
public enum Role { ADMIN, HR, SUPERVISOR, WORKER }
```

**Method Security:**  
```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteEmployee(Long id) { ... }
```

**API Key/OAuth2 Toggle:**  
- Configurable via `application.yml`

**Row-Level Security:**  
- Supervisor can only access team members (filter by department/team)

---

### E04: Time & Attendance (Stories 16-19)

**Entity:**  
```java
@Entity
public class AttendanceEvent {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private LocalDateTime timestamp;
  @Enumerated(EnumType.STRING) private EventType type; // CLOCK_IN, CLOCK_OUT
  private String deviceId;
  private GeoLocation geoLocation;
  private boolean correctionRequested;
}
```

**Service:**  
```java
public interface AttendanceService {
  AttendanceEvent clockIn(Long employeeId, GeoLocation location, String deviceId);
  AttendanceEvent clockOut(Long employeeId, GeoLocation location, String deviceId);
  List<AttendanceEvent> getDailyEvents(Long employeeId, LocalDate date);
  CorrectionRequest requestCorrection(...);
}
```

**Controller:**  
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
  @PostMapping("/clock-in") public AttendanceEvent clockIn(...);
  @PostMapping("/clock-out") public AttendanceEvent clockOut(...);
  @GetMapping("/report") public List<AttendanceSummary> getReport(...);
}
```

**Features:**  
- Geofence validation  
- Automatic shift association  
- Correction workflow (approval tasks)  
- CSV export endpoint

---

### E05: Shift & Schedule Management (Stories 20-24)

**Entity:**  
```java
@Entity
public class ShiftTemplate {
  @Id @GeneratedValue private Long id;
  private String name;
  private LocalTime startTime;
  private LocalTime endTime;
  private Set<DayOfWeek> days;
  private boolean recurring;
}
@Entity
public class ShiftAssignment {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  @ManyToOne private ShiftTemplate template;
  private LocalDate date;
}
```

**Service:**  
```java
public interface ShiftService {
  ShiftTemplate createTemplate(...);
  ShiftAssignment assignShift(...);
  boolean detectConflicts(...);
}
```

**Controller:**  
```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
  @PostMapping("/templates") public ShiftTemplate createTemplate(...);
  @PostMapping("/assign") public ShiftAssignment assign(...);
  @GetMapping("/my") public List<ShiftAssignment> getMyShifts(...);
}
```

**Features:**  
- Conflict detection  
- Bulk assignment  
- Audit entries on changes

---

### E06: Leave & Absence Management (Stories 25-29)

**Entity:**  
```java
@Entity
public class LeaveRequest {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private LocalDate startDate;
  private LocalDate endDate;
  @Enumerated(EnumType.STRING) private LeaveType type;
  @Enumerated(EnumType.STRING) private LeaveStatus status;
  private String approver;
}
```

**Service:**  
```java
public interface LeaveService {
  LeaveRequest requestLeave(...);
  LeaveRequest approveLeave(Long requestId, ...);
  List<LeaveRequest> getEmployeeLeaves(Long employeeId);
}
```

**Controller:**  
```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
  @PostMapping public LeaveRequest request(...);
  @PostMapping("/{id}/approve") public LeaveRequest approve(...);
  @GetMapping("/my") public List<LeaveRequest> getMyLeaves(...);
}
```

**Features:**  
- Accrual balances  
- Integration with scheduling/payroll  
- Auto-flag scheduled shifts for coverage

---

### E07: Training & Certification Tracking (Stories 30-33)

**Entity:**  
```java
@Entity
public class Certification {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String type;
  private LocalDate issueDate;
  private LocalDate expiryDate;
  private String documentUrl;
}
```

**Service:**  
```java
public interface CertificationService {
  Certification addCertification(...);
  List<Certification> getExpiringCerts(int days);
}
```

**Controller:**  
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
  @PostMapping public Certification add(...);
  @GetMapping("/expiring") public List<Certification> getExpiring(...);
}
```

**Features:**  
- Expiry alerts (30/7 days)  
- Scheduling checks block unqualified assignments  
- Proof document upload

---

### E08: Safety Incidents & OSHA Reporting (Stories 34-37)

**Entity:**  
```java
@Entity
public class SafetyIncident {
  @Id @GeneratedValue private Long id;
  private String description;
  private Severity severity;
  private String location;
  @ManyToMany private List<Employee> involvedEmployees;
  @Enumerated(EnumType.STRING) private IncidentStatus status;
}
```

**Service:**  
```java
public interface SafetyService {
  SafetyIncident recordIncident(...);
  SafetyIncident updateStatus(...);
  List<SafetyIncident> exportOSHA(...);
}
```

**Controller:**  
```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyController {
  @PostMapping public SafetyIncident record(...);
  @PostMapping("/{id}/status") public SafetyIncident updateStatus(...);
  @GetMapping("/osha-export") public ResponseEntity<Resource> exportOSHA(...);
}
```

**Features:**  
- Status workflow (Open, Investigating, Resolved)  
- OSHA 300/300A export  
- Metrics dashboard endpoints

---

### E09: Equipment & Asset Assignment (Stories 38-42)

**Entity:**  
```java
@Entity
public class Asset {
  @Id @GeneratedValue private Long id;
  private String assetTag;
  private String type;
  private AssetStatus status;
  @ManyToOne private Employee assignedTo;
  private LocalDate checkOutDate;
  private LocalDate checkInDate;
}
```

**Service:**  
```java
public interface AssetService {
  Asset registerAsset(...);
  Asset checkOut(Long assetId, Long employeeId);
  Asset checkIn(Long assetId);
}
```

**Controller:**  
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
  @PostMapping public Asset register(...);
  @PostMapping("/{id}/checkout") public Asset checkOut(...);
  @PostMapping("/{id}/checkin") public Asset checkIn(...);
}
```

**Features:**  
- Certification checks before assignment  
- History log per asset/employee  
- Overdue return reports

---

### E10: Performance Reviews & Goals (Stories 43-47)

**Entity:**  
```java
@Entity
public class PerformanceReview {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private LocalDate reviewDate;
  private String template;
  private String goals;
  private String competencies;
  private int rating;
  private String comments;
  private boolean acknowledgedByEmployee;
  private boolean acknowledgedBySupervisor;
}
```

**Service:**  
```java
public interface ReviewService {
  PerformanceReview createReview(...);
  PerformanceReview acknowledge(Long reviewId, ...);
}
```

**Controller:**  
```java
@RestController
@RequestMapping("/reviews")
public class ReviewController {
  @PostMapping public PerformanceReview create(...);
  @PostMapping("/{id}/acknowledge") public PerformanceReview acknowledge(...);
  @GetMapping("/pdf/{id}") public ResponseEntity<Resource> exportPdf(...);
}
```

**Features:**  
- Immutable history after sign-off  
- PDF export  
- Role-based visibility

---

### E11: Payroll Export Integration (Stories 48-51)

**Service:**  
```java
public interface PayrollService {
  PayrollExport generateExport(LocalDate periodStart, LocalDate periodEnd);
  void deliverExport(PayrollExport export);
}
```

**Controller:**  
```java
@RestController
@RequestMapping("/payroll")
public class PayrollController {
  @PostMapping("/export") public PayrollExport generateExport(...);
}
```

**Features:**  
- Mapping to provider schema  
- SFTP/API delivery  
- Retry with backoff  
- Audit log for exports

---

### E12: Notifications & Announcements (Stories 52-56)

**Entity:**  
```java
@Entity
public class Notification {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee recipient;
  private String channel; // EMAIL, SMS, IN_APP
  private String template;
  private String content;
  private LocalDateTime sentAt;
  private boolean delivered;
}
```

**Service:**  
```java
public interface NotificationService {
  void sendNotification(NotificationDto dto);
  List<Notification> getUserNotifications(Long employeeId);
}
```

**Controller:**  
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
  @PostMapping public void send(...);
  @GetMapping("/my") public List<Notification> getMyNotifications(...);
}
```

**Features:**  
- Opt-in/out per channel  
- Localized templates  
- Delivery status tracking  
- Rate limiting

---

### E13: Integration Layer (HRIS/WMS APIs) (Stories 57-61)

**HRIS Connector:**  
```java
@Component
public class HRISConnector {
  public void syncEmployees() { ... }
}
```

**API Security:**  
- JWT/OAuth2-secured endpoints

**Controller:**  
```java
@RestController
@RequestMapping("/integration/hris")
public class HRISController {
  @PostMapping("/sync") public void syncEmployees();
}
```

**Features:**  
- Idempotent webhooks  
- OpenAPI documentation

---

### E14: Audit Trail & Compliance (Stories 62-65)

**Entity:**  
```java
@Entity
public class AuditLog {
  @Id @GeneratedValue private Long id;
  private String entity;
  private Long entityId;
  private String actor;
  private LocalDateTime timestamp;
  private String action;
  private String before;
  private String after;
}
```

**Service:**  
```java
public interface AuditService {
  void logChange(String entity, Long entityId, String actor, String action, Object before, Object after);
  List<AuditLog> exportLogs(...);
}
```

**Controller:**  
```java
@RestController
@RequestMapping("/audit")
public class AuditController {
  @GetMapping public List<AuditLog> getLogs(...);
  @GetMapping("/export") public ResponseEntity<Resource> exportLogs(...);
}
```

**Features:**  
- Tamper-evident storage  
- Export by date/user/entity  
- Coverage tests

---

### E15: Reporting & Analytics (Stories 66-70)

**Service:**  
```java
public interface ReportService {
  Report generateAttendanceReport(...);
  Report generateOvertimeReport(...);
  Report generateLeaveBalanceReport(...);
  Report generateCertificationStatusReport(...);
  Report generateSafetyKPIReport(...);
}
```

**Controller:**  
```java
@RestController
@RequestMapping("/reports")
public class ReportController {
  @GetMapping("/attendance") public Report getAttendance(...);
  @GetMapping("/overtime") public Report getOvertime(...);
  @GetMapping("/leave") public Report getLeaveBalance(...);
  @GetMapping("/certifications") public Report getCertStatus(...);
  @GetMapping("/safety") public Report getSafetyKPI(...);
  @GetMapping("/export") public ResponseEntity<Resource> exportReport(...);
}
```

**Features:**  
- Filter by date, department, shift  
- CSV/PDF export  
- Metrics endpoints for BI  
- Role-based dashboards

---

### E16: Mobile Access (PWA) (Stories 71-75)

**Mobile Controller:**  
```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
  @PostMapping("/clock-in") public AttendanceEvent clockIn(...);
  @GetMapping("/shifts") public List<ShiftAssignment> getShifts(...);
  @PostMapping("/leave-request") public LeaveRequest requestLeave(...);
  @GetMapping("/announcements") public List<Notification> getAnnouncements(...);
}
```

**Features:**  
- Responsive views (Thymeleaf/React)  
- PWA manifest  
- Offline queue for clock events  
- Conflict resolution logic  
- Lighthouse PWA score validation

---

### E17: Onboarding & Offboarding Workflow (Stories 76-80)

**Service:**  
```java
public interface OnboardingService {
  void onboardEmployee(HRISNewHireDto dto);
  void offboardEmployee(Long employeeId);
}
```

**Controller:**  
```java
@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
  @PostMapping("/hris-sync") public void onboardFromHRIS(...);
  @PostMapping("/offboard/{id}") public void offboardEmployee(@PathVariable Long id);
}
```

**Features:**  
- HRIS sync triggers onboarding  
- Tasks for training/asset assignment  
- Offboarding revokes access, collects assets, updates schedules

---

## General Configuration & Security

- All endpoints documented via OpenAPI/Swagger
- Global exception handling (`@ControllerAdvice`)
- Centralized validation (`javax.validation`)
- Security: JWT/OAuth2, method/endpoint/row-level
- Integration: REST clients (WebClient), SFTP, webhooks
- Audit: Aspect-oriented logging for sensitive changes

---

## Example Code Snippet: Employee CRUD

```java
// EmployeeController.java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
  @Autowired private EmployeeService employeeService;

  @PostMapping
  public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) {
    return ResponseEntity.ok(employeeService.create(dto));
  }

  @GetMapping
  public Page<EmployeeDto> list(EmployeeFilter filter, Pageable pageable) {
    return employeeService.list(filter, pageable);
  }
}
```

---

## Integration Points

- HRIS: REST API connector, scheduled sync
- WMS: Department/location mapping
- Payroll: SFTP/API export
- Notifications: Email/SMS gateways
- Mobile: PWA endpoints

---

## Summary

This document provides a production-ready, detailed technical design for all 80 user stories in the Warehouse EMS system, grouped by epic/module, with Spring Boot best practices, package structure, entity/service/repository/controller specs, configuration/security, integration points, and code snippets. All multi-line text is escaped with `
` for developer readability.

---

## Document Metadata

**Document Version:** 1.0  
**Last Updated:** 2024  
**Total User Stories Covered:** 80  
**Total Epics:** 17  
**Technology Stack:** Spring Boot 3.x, Java 17+, PostgreSQL, Maven  
**Architecture Pattern:** Layered Architecture (Controller â Service â Repository â Entity)  
**Security:** Spring Security with JWT/OAuth2  
**API Documentation:** OpenAPI 3.0 / Swagger  
**Database Migration:** Flyway/Liquibase  
**Monitoring:** Spring Boot Actuator  

---

## Appendix: User Story Reference

### Epic E01: Project Scaffolding (Stories 1-5)
- Story 1: Initialize Spring Boot Project
- Story 2: Configure Base Package Structure
- Story 3: Database Migration Setup
- Story 4: Enable Actuator Health Endpoint
- Story 5: Document Build and Run Steps

### Epic E02: Employee Master Data (Stories 6-10)
- Story 6: Employee CRUD API
- Story 7: Enforce Unique Badge ID
- Story 8: Employee Soft Delete
- Story 9: Employee API Pagination and Filtering
- Story 10: OpenAPI Documentation for Employee API

### Epic E03: RBAC (Stories 11-15)
- Story 11: Implement Role-Based Access Control
- Story 12: Row-Level Security for Employee Data
- Story 13: API Key/OAuth2 Security Toggle
- Story 14: Unauthorized and Forbidden Response Handling
- Story 15: RBAC Automated Tests

### Epic E04: Time & Attendance (Stories 16-19)
- Story 16: Clock In/Out Endpoints
- Story 17: Automatic Shift Association for Attendance
- Story 18: Missed Punches and Corrections Workflow
- Story 19: Attendance Reports Export

### Epic E05: Shift Management (Stories 20-24)
- Story 20: Shift Template CRUD
- Story 21: Schedule Conflict Detection
- Story 22: Personal Upcoming Shifts View
- Story 23: Bulk Shift Assignment
- Story 24: Shift Assignment Audit Logging

### Epic E06: Leave Management (Stories 25-29)
- Story 25: Leave Request Workflow
- Story 26: Supervisor Leave Approval/Deny
- Story 27: Leave Accrual Balance Update
- Story 28: Scheduled Shift Coverage Flagging
- Story 29: Leave Export Report

### Epic E07: Training & Certification (Stories 30-33)
- Story 30: Certification CRUD
- Story 31: Certification Expiry Alerts
- Story 32: Block Unqualified Task Assignment
- Story 33: Certification Status on Employee Profile

### Epic E08: Safety & OSHA (Stories 34-37)
- Story 34: Safety Incident Recording
- Story 35: Incident Investigation Workflow
- Story 36: OSHA Summary Export
- Story 37: Safety Metrics Dashboard

### Epic E09: Equipment & Assets (Stories 38-42)
- Story 38: Asset Registry CRUD
- Story 39: Asset Check-In/Out Endpoints
- Story 40: Block Asset Use Without Valid Certification
- Story 41: Asset History Log
- Story 42: Overdue Asset Return Report

### Epic E10: Performance Reviews (Stories 43-47)
- Story 43: Performance Review Cycle Creation
- Story 44: Assign Performance Reviews to Employees
- Story 45: Submit and Acknowledge Performance Reviews
- Story 46: Performance Review PDF Export
- Story 47: Role-Based Review Visibility

### Epic E11: Payroll Integration (Stories 48-51)
- Story 48: Payroll Export File Generation
- Story 49: Payroll Export Reconciliation
- Story 50: Secure Payroll Delivery (SFTP/API)
- Story 51: Payroll Export Audit Logging

### Epic E12: Notifications (Stories 52-56)
- Story 52: Notification Channel Opt-In/Out
- Story 53: Localized Notification Templates
- Story 54: Notification Delivery Status Tracking
- Story 55: Rate Limiting for Notifications
- Story 56: Announcements on Dashboard

### Epic E13: Integration Layer (Stories 57-61)
- Story 57: HRIS API Connector
- Story 58: WMS Department/Location Link
- Story 59: JWT/OAuth2 Secured APIs
- Story 60: Webhook Event Delivery
- Story 61: OpenAPI Documentation for Integration APIs

### Epic E14: Audit Trail (Stories 62-65)
- Story 62: Centralized Audit Logging
- Story 63: Immutable Audit Log Table
- Story 64: Audit Log Export by Date/User/Entity
- Story 65: Audit Log Test Coverage

### Epic E15: Reporting & Analytics (Stories 66-70)
- Story 66: Attendance and Overtime Reporting
- Story 67: Leave Balance and Certification Status Reporting
- Story 68: Safety KPI Reporting
- Story 69: Role-Based Dashboards
- Story 70: Metrics Endpoints for BI Integration

### Epic E16: Mobile Access (Stories 71-75)
- Story 71: Mobile Clock In/Out Flow
- Story 72: Mobile Schedule and Leave Request
- Story 73: Mobile Announcements View
- Story 74: Installable PWA Manifest
- Story 75: Offline Queue for Clock Events

### Epic E17: Onboarding/Offboarding (Stories 76-80)
- Story 76: Onboarding from HRIS Sync
- Story 77: Training and Asset Assignment Tasks for Onboarding
- Story 78: Offboarding Workflow for Terminations
- Story 79: Offboarding Asset Collection and Schedule Update
- Story 80: Offboarding Access Revocation

---

**END OF DOCUMENT**