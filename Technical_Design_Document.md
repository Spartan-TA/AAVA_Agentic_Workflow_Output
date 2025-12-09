# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

## Introduction
This document provides a comprehensive low-level technical design for the Warehouse Employee Management System (EMS), covering all 60 user stories derived from 20 epics. Each section details the Spring Boot architecture, package structure, domain models, service/repository/controller specifications, configuration, security, integration points, and sample code snippets, following industry best practices.

---

Section: Project Scaffolding & Domain Setup  
Description: Establishes the foundational Spring Boot project structure, base packages, database migration, and monitoring.  
Design Specification:  
- Package Structure: `com.wms.ems` (subpackages: employee, schedule, attendance, safety, config)  
- Domain Model: N/A (foundation only)  
- Repository Layer: N/A  
- Service Layer: N/A  
- Controller Layer: N/A  
- Security Configuration: N/A  
- Integration Points: Flyway/Liquibase, Actuator  
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
Description: Implements CRUD operations for Employee entities, including soft delete, filtering, and unique badge enforcement.  
Design Specification:  
- Package Structure: `com.wms.ems.employee`  
- Domain Model: Employee {id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted}  
- Repository Layer: EmployeeRepository extends JpaRepository<Employee, Long>  
- Service Layer: EmployeeService (create, update, delete, filter, softDelete)  
- Controller Layer: EmployeeController (REST endpoints for CRUD)  
- Security Configuration: Method-level security for role-based access  
- Integration Points: OpenAPI/Swagger  
Sample Implementation:  
```java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    @Column(unique=true)
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted;
}

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) {...}
    @GetMapping
    public Page<EmployeeDto> list(@RequestParam Map<String,String> filters, Pageable pageable) {...}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {...}
}
```

---

Section: Role Based Access Control (RBAC)  
Description: Secures endpoints and methods using Spring Security, with roles ADMIN, HR, SUPERVISOR, WORKER, and row-level constraints.  
Design Specification:  
- Package Structure: `com.wms.ems.security`  
- Domain Model: Role (enum), User (linked to Employee)  
- Repository Layer: UserRepository  
- Service Layer: UserService, SecurityService  
- Controller Layer: SecurityController (login, role management)  
- Security Configuration: @EnableWebSecurity, method/endpoint security, OAuth2/API key toggle  
- Integration Points: Security tests  
Sample Implementation:  
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
            .anyRequest().authenticated()
            .and().oauth2Login();
    }
}
```

---

Section: Time & Attendance (Clock In/Out)  
Description: Handles clock-in/out events, geofence/device capture, missed punches, corrections workflow, and attendance reports.  
Design Specification:  
- Package Structure: `com.wms.ems.attendance`  
- Domain Model: AttendanceEvent {id, employee, timestamp, type, deviceId, location, correctionStatus}  
- Repository Layer: AttendanceRepository  
- Service Layer: AttendanceService (clockIn, clockOut, corrections, report generation)  
- Controller Layer: AttendanceController (REST endpoints)  
- Security Configuration: Role-based access  
- Integration Points: CSV export  
Sample Implementation:  
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
    private String correctionStatus;
}

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockEventDto dto) {...}
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockEventDto dto) {...}
}
```

---

Section: Shift & Schedule Management  
Description: Manages shift templates, rotations, overtime rules, assignments, blackout dates, and conflict detection.  
Design Specification:  
- Package Structure: `com.wms.ems.schedule`  
- Domain Model: ShiftTemplate, ShiftAssignment, BlackoutDate  
- Repository Layer: ShiftRepository, AssignmentRepository  
- Service Layer: ShiftService, AssignmentService  
- Controller Layer: ShiftController  
- Security Configuration: Role-based access  
- Integration Points: Audit logging  
Sample Implementation:  
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isOvertime;
}

@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping
    public ResponseEntity<ShiftTemplateDto> create(@RequestBody ShiftTemplateDto dto) {...}
    @GetMapping("/assignments")
    public List<AssignmentDto> getAssignments(@RequestParam Long employeeId) {...}
}
```

---

Section: Leave & Absence Management  
Description: Implements PTO/sick/unpaid leave requests, approvals, accrual balances, and scheduling integration.  
Design Specification:  
- Package Structure: `com.wms.ems.leave`  
- Domain Model: LeaveRequest, LeaveBalance  
- Repository Layer: LeaveRepository, BalanceRepository  
- Service Layer: LeaveService  
- Controller Layer: LeaveController  
- Security Configuration: Role-based access  
- Integration Points: Scheduling, Payroll  
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
    private String status; // REQUESTED, APPROVED, DENIED
}

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping
    public ResponseEntity<LeaveRequestDto> requestLeave(@RequestBody LeaveRequestDto dto) {...}
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id) {...}
}
```

---

Section: Training & Certification Tracking  
Description: Tracks required certifications, expirations, renewals, assignment blocking, and document uploads.  
Design Specification:  
- Package Structure: `com.wms.ems.certification`  
- Domain Model: Certification, CertificationDocument  
- Repository Layer: CertificationRepository  
- Service Layer: CertificationService  
- Controller Layer: CertificationController  
- Security Configuration: Role-based access  
- Integration Points: Scheduling  
Sample Implementation:  
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type;
    private LocalDate expiryDate;
    private boolean isActive;
}

@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<CertificationDto> add(@RequestBody CertificationDto dto) {...}
    @GetMapping("/alerts")
    public List<CertificationAlertDto> getAlerts() {...}
}
```

---

Section: Safety Incidents & OSHA Reporting  
Description: Records safety incidents, investigation workflow, OSHA reporting, and dashboard metrics.  
Design Specification:  
- Package Structure: `com.wms.ems.safety`  
- Domain Model: SafetyIncident, Investigation  
- Repository Layer: IncidentRepository  
- Service Layer: SafetyService  
- Controller Layer: SafetyController  
- Security Configuration: Role-based access  
- Integration Points: OSHA export  
Sample Implementation:  
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
    private String status; // OPEN, INVESTIGATING, RESOLVED
}

@RestController
@RequestMapping("/safety/incidents")
public class SafetyController {
    @PostMapping
    public ResponseEntity<SafetyIncidentDto> record(@RequestBody SafetyIncidentDto dto) {...}
    @GetMapping("/oshalog")
    public ResponseEntity<Resource> exportOshaLog() {...}
}
```

---

Section: Equipment & Asset Assignment  
Description: Manages asset registry, checkout/return tracking, certification validation, and overdue reports.  
Design Specification:  
- Package Structure: `com.wms.ems.asset`  
- Domain Model: Asset, AssetAssignment  
- Repository Layer: AssetRepository, AssignmentRepository  
- Service Layer: AssetService  
- Controller Layer: AssetController  
- Security Configuration: Role-based access  
- Integration Points: Certification  
Sample Implementation:  
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String condition;
    private boolean isCheckedOut;
}

@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/checkout")
    public ResponseEntity<Void> checkout(@RequestBody AssetAssignmentDto dto) {...}
    @PostMapping("/return")
    public ResponseEntity<Void> returnAsset(@RequestBody AssetAssignmentDto dto) {...}
}
```

---

Section: Performance Reviews & Goals  
Description: Implements review templates, goal tracking, competencies, ratings, acknowledgements, and PDF export.  
Design Specification:  
- Package Structure: `com.wms.ems.review`  
- Domain Model: PerformanceReview, Goal  
- Repository Layer: ReviewRepository, GoalRepository  
- Service Layer: ReviewService  
- Controller Layer: ReviewController  
- Security Configuration: Role-based access  
- Integration Points: PDF export  
Sample Implementation:  
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String cycle;
    private String status;
    private String comments;
}

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping
    public ResponseEntity<PerformanceReviewDto> submit(@RequestBody PerformanceReviewDto dto) {...}
    @GetMapping("/pdf/{id}")
    public ResponseEntity<Resource> exportPdf(@PathVariable Long id) {...}
}
```

---

Section: Payroll Export Integration  
Description: Generates payroll files from attendance/leave, maps to provider formats, and delivers securely via SFTP/API.  
Design Specification:  
- Package Structure: `com.wms.ems.payroll`  
- Domain Model: PayrollExport  
- Repository Layer: PayrollRepository  
- Service Layer: PayrollService  
- Controller Layer: PayrollController  
- Security Configuration: Role-based access  
- Integration Points: SFTP/API  
Sample Implementation:  
```java
@Entity
public class PayrollExport {
    @Id @GeneratedValue
    private Long id;
    private LocalDate exportDate;
    private String status;
    private String filePath;
}

@RestController
@RequestMapping("/payroll")
public class PayrollController {
    @PostMapping("/export")
    public ResponseEntity<Void> exportPayroll(@RequestBody PayrollExportRequestDto dto) {...}
}
```

---

Section: Notifications & Announcements  
Description: Sends in-app, email/SMS notifications for shifts, certifications, approvals, and announcements with quiet hours.  
Design Specification:  
- Package Structure: `com.wms.ems.notification`  
- Domain Model: Notification, Announcement  
- Repository Layer: NotificationRepository  
- Service Layer: NotificationService  
- Controller Layer: NotificationController  
- Security Configuration: Role-based access  
- Integration Points: Email/SMS APIs  
Sample Implementation:  
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private String channel; // IN_APP, EMAIL, SMS
    private String message;
    private LocalDateTime sentAt;
    private boolean delivered;
}

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping
    public ResponseEntity<Void> send(@RequestBody NotificationDto dto) {...}
}
```

---

Section: Integration Layer (HRIS/WMS APIs)  
Description: Exposes REST APIs/connectors for HRIS sync, WMS integration, SSO with IDP, and webhooks.  
Design Specification:  
- Package Structure: `com.wms.ems.integration`  
- Domain Model: IntegrationEvent  
- Repository Layer: IntegrationRepository  
- Service Layer: IntegrationService  
- Controller Layer: IntegrationController  
- Security Configuration: JWT/OAuth2  
- Integration Points: HRIS, WMS, IDP  
Sample Implementation:  
```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    @PostMapping("/hris/sync")
    public ResponseEntity<Void> syncHris(@RequestBody HrisSyncDto dto) {...}
    @PostMapping("/wms/link")
    public ResponseEntity<Void> linkWms(@RequestBody WmsLinkDto dto) {...}
}
```

---

Section: Audit Trail & Compliance  
Description: Centralized audit logging for sensitive changes, tamper-evident storage, and export functionality.  
Design Specification:  
- Package Structure: `com.wms.ems.audit`  
- Domain Model: AuditLog  
- Repository Layer: AuditRepository  
- Service Layer: AuditService  
- Controller Layer: AuditController  
- Security Configuration: Role-based access  
- Integration Points: Export  
Sample Implementation:  
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private String action;
    private String actor;
    private LocalDateTime timestamp;
    private String before;
    private String after;
}

@RestController
@RequestMapping("/audit")
public class AuditController {
    @GetMapping("/export")
    public ResponseEntity<Resource> exportAudit(@RequestParam Map<String,String> filters) {...}
}
```

---

Section: Reporting & Analytics  
Description: Provides operational reports for attendance, overtime, leave, certifications, safety KPIs, and dashboards.  
Design Specification:  
- Package Structure: `com.wms.ems.reporting`  
- Domain Model: Report  
- Repository Layer: ReportRepository  
- Service Layer: ReportingService  
- Controller Layer: ReportingController  
- Security Configuration: Role-based access  
- Integration Points: CSV/PDF export  
Sample Implementation:  
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> attendanceReport(@RequestParam Map<String,String> filters) {...}
    @GetMapping("/certifications")
    public ResponseEntity<Resource> certificationReport(@RequestParam Map<String,String> filters) {...}
}
```

---

Section: Mobile Access (PWA)  
Description: Enables responsive PWA for clock-in/out, schedules, leave requests, announcements, and offline support.  
Design Specification:  
- Package Structure: `com.wms.ems.mobile`  
- Domain Model: N/A (mobile DTOs)  
- Repository Layer: N/A  
- Service Layer: MobileService  
- Controller Layer: MobileController  
- Security Configuration: JWT/OAuth2  
- Integration Points: PWA manifest, offline queue  
Sample Implementation:  
```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> mobileClockIn(@RequestBody ClockEventDto dto) {...}
    @PostMapping("/leave")
    public ResponseEntity<?> mobileLeaveRequest(@RequestBody LeaveRequestDto dto) {...}
}
```

---

Section: Onboarding & Offboarding Workflow  
Description: Automates provisioning/deprovisioning, training, asset management, and schedule updates for employee lifecycle changes.  
Design Specification:  
- Package Structure: `com.wms.ems.lifecycle`  
- Domain Model: OnboardingTask, OffboardingTask  
- Repository Layer: LifecycleRepository  
- Service Layer: LifecycleService  
- Controller Layer: LifecycleController  
- Security Configuration: Role-based access  
- Integration Points: HRIS, Asset, Schedule, Certification  
Sample Implementation:  
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type; // TRAINING, ASSET_ASSIGNMENT, SCHEDULE
    private String status; // PENDING, COMPLETED
}

@RestController
@RequestMapping("/lifecycle")
public class LifecycleController {
    @PostMapping("/onboard")
    public ResponseEntity<Void> onboard(@RequestBody OnboardingRequestDto dto) {...}
    @PostMapping("/offboard")
    public ResponseEntity<Void> offboard(@RequestBody OffboardingRequestDto dto) {...}
}
```

---

Section: Localization & Multi-Warehouse  
Description: Supports multi-site operations with localization and data segmentation.  
Design Specification:  
- Package Structure: `com.wms.ems.localization`  
- Domain Model: Warehouse, LocaleSetting  
- Repository Layer: WarehouseRepository, LocaleRepository  
- Service Layer: LocalizationService  
- Controller Layer: LocalizationController  
- Security Configuration: Role-based access  
- Integration Points: None  
Sample Implementation:  
```java
@Entity
public class Warehouse {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String location;
}

@RestController
@RequestMapping("/localization")
public class LocalizationController {
    @PostMapping("/settings")
    public ResponseEntity<Void> setLocale(@RequestBody LocaleSettingDto dto) {...}
    @GetMapping("/warehouses")
    public List<WarehouseDto> getWarehouses() {...}
}
```

---

Section: Advanced Scheduling (AI/Optimization)  
Description: AI-driven shift optimization considering availability, skills, costs.  
Design Specification:  
- Package Structure: `com.wms.ems.optim`  
- Domain Model: OptimizationRequest, OptimizationResult  
- Repository Layer: OptimizationRepository  
- Service Layer: OptimizationService  
- Controller Layer: OptimizationController  
- Security Configuration: Role-based access  
- Integration Points: AI engine  
Sample Implementation:  
```java
@RestController
@RequestMapping("/optimization")
public class OptimizationController {
    @PostMapping("/optimize")
    public ResponseEntity<OptimizationResultDto> optimize(@RequestBody OptimizationRequestDto dto) {...}
}
```

---

Section: Self-Service Portal  
Description: Workers update profile, view records, request leave, see certifications.  
Design Specification:  
- Package Structure: `com.wms.ems.selfservice`  
- Domain Model: N/A (uses existing entities)  
- Repository Layer: N/A  
- Service Layer: SelfServiceService  
- Controller Layer: SelfServiceController  
- Security Configuration: Role-based access  
- Integration Points: Employee, Leave, Certification  
Sample Implementation:  
```java
@RestController
@RequestMapping("/selfservice")
public class SelfServiceController {
    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileUpdateDto dto) {...}
    @GetMapping("/records")
    public ResponseEntity<EmployeeRecordsDto> getRecords() {...}
}
```

---

## Conclusion
This document provides a comprehensive low-level technical design for all 60 user stories, following Spring Boot best practices. Each section includes package structure, domain models, service/repository/controller specifications, security configuration, integration points, and sample code snippets. The design ensures modularity, scalability, and maintainability for the Warehouse EMS.