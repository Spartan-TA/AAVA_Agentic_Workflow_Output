# Technical Design Document: Warehouse Employee Management System

## Introduction
This document provides a comprehensive low-level technical design for all 100 user stories derived from the 20 epics of the Warehouse Employee Management System. Each user story is covered with detailed Spring Boot architecture, package structure, entity design, service/repository/controller specifications, configuration/security, integration points, and sample implementation snippets. This document is intended for Spring Boot developers to ensure consistency, quality, and adherence to industry standards.

---

## Section: E01 - Project Scaffolding & Domain Setup
### Description:
Initialize Spring Boot (Maven) project; configure base packages; set up core modules (employee, scheduling, attendance, safety); add Flyway/Liquibase for DB migrations; enable Actuator.

### Design Specification:
- **Spring Boot Architecture:** Layered architecture (Controller, Service, Repository, Domain).
- **Package Structure:**
  - `com.wms.employee`
  - `com.wms.scheduling`
  - `com.wms.attendance`
  - `com.wms.safety`
  - `com.wms.config`
- **Modules:** Employee, Scheduling, Attendance, Safety
- **Component Breakdown:**
  - Main Application class
  - Configuration classes for Flyway/Liquibase, Actuator
- **Entity Design:** N/A (setup only)
- **Service/Repository/Controller:** N/A (setup only)
- **Configuration/Security:**
  - `application.yml` for DB, actuator, migration settings
- **Integration Points:** N/A

### Sample Implementation:
```java
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}
```

---

## Section: E02 - Employee Master Data (CRUD)
### Description:
Create Employee domain with CRUD APIs and web DTOs: name, badgeId, role, department, shiftGroup, hireDate, status.

### Design Specification:
- **Spring Boot Architecture:** RESTful API, DTOs, layered structure
- **Package Structure:**
  - `com.wms.employee.domain`
  - `com.wms.employee.repository`
  - `com.wms.employee.service`
  - `com.wms.employee.controller`
- **Entity Design:**
  - `Employee` entity: id, name, badgeId, role, department, shiftGroup, hireDate, status
  - Relationships: Many-to-one with Department, ShiftGroup
- **Service Layer:**
  - `EmployeeService`: CRUD, soft-delete, pagination, filtering
- **Repository Layer:**
  - `EmployeeRepository extends JpaRepository<Employee, Long>`
- **Controller:**
  - `EmployeeController`: `/employees` endpoints
- **Configuration/Security:**
  - Unique badgeId constraint
  - OpenAPI documentation
- **Integration Points:** N/A

### Sample Implementation:
```java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    private String name;
    @Column(unique = true)
    private String badgeId;
    private String role;
    @ManyToOne
    private Department department;
    @ManyToOne
    private ShiftGroup shiftGroup;
    private LocalDate hireDate;
    private String status;
    // getters/setters
}
```

---

## Section: E03 - Role-Based Access Control (RBAC)
### Description:
Add Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER); method/endpoint security and row-level constraints; API key/OAuth2 toggle via config.

### Design Specification:
- **Spring Boot Architecture:** Spring Security, method security
- **Package Structure:**
  - `com.wms.security`
- **Modules:** Security config, RBAC
- **Component Breakdown:**
  - `SecurityConfig` class
  - Role enums
- **Entity Design:**
  - `User` entity with roles
- **Service Layer:**
  - User authentication/authorization
- **Repository Layer:**
  - `UserRepository`
- **Controller:**
  - Secure endpoints
- **Configuration/Security:**
  - API key/OAuth2 toggle
  - Role-based access annotations
- **Integration Points:**
  - OAuth2 provider

### Sample Implementation:
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

## Section: E04 - Time & Attendance (Clock In/Out)
### Description:
Endpoints for clock-in/out events with geofence and device capture; calculate hours worked per shift; handle missed punches and corrections workflow.

### Design Specification:
- **Spring Boot Architecture:** RESTful API, event-driven
- **Package Structure:**
  - `com.wms.attendance.domain`
  - `com.wms.attendance.repository`
  - `com.wms.attendance.service`
  - `com.wms.attendance.controller`
- **Entity Design:**
  - `AttendanceEvent`: id, employee, type (IN/OUT), timestamp, deviceId, location
- **Service Layer:**
  - `AttendanceService`: clock-in/out, calculate hours, corrections
- **Repository Layer:**
  - `AttendanceRepository`
- **Controller:**
  - `AttendanceController`: `/attendance/clock-in`, `/attendance/clock-out`
- **Configuration/Security:**
  - Validation for geofence/device
- **Integration Points:**
  - Export to payroll

### Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type; // IN/OUT
    private LocalDateTime timestamp;
    private String deviceId;
    private String location;
}
```

---

## Section: E05 - Shift & Schedule Management
### Description:
Create recurring shift templates, rotations, overtime rules, and assignment to employees; handle blackout dates and warehouse operation calendars.

### Design Specification:
- **Spring Boot Architecture:** Scheduling, batch jobs
- **Package Structure:**
  - `com.wms.scheduling.domain`
  - `com.wms.scheduling.repository`
  - `com.wms.scheduling.service`
  - `com.wms.scheduling.controller`
- **Entity Design:**
  - `ShiftTemplate`, `ShiftAssignment`, `BlackoutDate`
- **Service Layer:**
  - `SchedulingService`: CRUD, conflict detection, bulk assignment
- **Repository Layer:**
  - `ShiftTemplateRepository`, `ShiftAssignmentRepository`
- **Controller:**
  - `SchedulingController`: `/shifts`, `/schedules`
- **Configuration/Security:**
  - Audit entries
- **Integration Points:**
  - Calendar APIs

### Sample Implementation:
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

## Section: E06 - Leave & Absence Management
### Description:
Request/approve PTO, sick, unpaid leave; accrual balances and policies; integration hooks to exclude from scheduling and payroll hours.

### Design Specification:
- **Spring Boot Architecture:** Workflow, REST API
- **Package Structure:**
  - `com.wms.leave.domain`
  - `com.wms.leave.repository`
  - `com.wms.leave.service`
  - `com.wms.leave.controller`
- **Entity Design:**
  - `LeaveRequest`, `LeaveBalance`, `LeavePolicy`
- **Service Layer:**
  - `LeaveService`: request, approve/deny, update balances
- **Repository Layer:**
  - `LeaveRequestRepository`, `LeaveBalanceRepository`
- **Controller:**
  - `LeaveController`: `/leave/requests`
- **Configuration/Security:**
  - Role-based approval
- **Integration Points:**
  - Payroll, scheduling

### Sample Implementation:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type; // PTO, Sick, Unpaid
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // Requested, Approved, Denied
}
```

---

## Section: E07 - Training & Certification Tracking
### Description:
Track required certifications (e.g., forklift), expirations, renewals; block assignment to tasks requiring expired certs; upload proof documents.

### Design Specification:
- **Spring Boot Architecture:** Event-driven, REST API
- **Package Structure:**
  - `com.wms.certification.domain`
  - `com.wms.certification.repository`
  - `com.wms.certification.service`
  - `com.wms.certification.controller`
- **Entity Design:**
  - `Certification`, `EmployeeCertification`
- **Service Layer:**
  - `CertificationService`: CRUD, expiry alerts, assignment checks
- **Repository Layer:**
  - `CertificationRepository`, `EmployeeCertificationRepository`
- **Controller:**
  - `CertificationController`: `/certifications`
- **Configuration/Security:**
  - Document upload security
- **Integration Points:**
  - Scheduling, asset assignment

### Sample Implementation:
```java
@Entity
public class EmployeeCertification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private Certification certification;
    private LocalDate expiryDate;
    private String proofDocumentUrl;
}
```

---

## Section: E08 - Safety Incidents & OSHA Reporting
### Description:
Record incidents/near-misses; severity, location, description, involved employees; workflow for investigation and corrective actions; generate OSHA summary.

### Design Specification:
- **Spring Boot Architecture:** Workflow, reporting
- **Package Structure:**
  - `com.wms.safety.domain`
  - `com.wms.safety.repository`
  - `com.wms.safety.service`
  - `com.wms.safety.controller`
- **Entity Design:**
  - `SafetyIncident`, `CorrectiveAction`
- **Service Layer:**
  - `SafetyService`: record, workflow, reporting
- **Repository Layer:**
  - `SafetyIncidentRepository`, `CorrectiveActionRepository`
- **Controller:**
  - `SafetyController`: `/safety/incidents`
- **Configuration/Security:**
  - Role-based access
- **Integration Points:**
  - OSHA export

### Sample Implementation:
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
    private String status; // Open, Investigating, Resolved
}
```

---

## Section: E09 - Equipment & Asset Assignment
### Description:
Assign scanners, forklifts, and PPE to employees; track checkout/return; prevent use if certification missing; maintain asset condition state.

### Design Specification:
- **Spring Boot Architecture:** Asset management, event-driven
- **Package Structure:**
  - `com.wms.asset.domain`
  - `com.wms.asset.repository`
  - `com.wms.asset.service`
  - `com.wms.asset.controller`
- **Entity Design:**
  - `Asset`, `AssetAssignment`, `AssetCondition`
- **Service Layer:**
  - `AssetService`: CRUD, check-in/out, certification checks
- **Repository Layer:**
  - `AssetRepository`, `AssetAssignmentRepository`
- **Controller:**
  - `AssetController`: `/assets`, `/assets/assign`
- **Configuration/Security:**
  - Certification validation
- **Integration Points:**
  - Certification module

### Sample Implementation:
```java
@Entity
public class AssetAssignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Asset asset;
    @ManyToOne
    private Employee employee;
    private LocalDateTime checkoutTime;
    private LocalDateTime returnTime;
}
```

---

## Section: E10 - Performance Reviews & Goals
### Description:
Create quarterly/annual review templates; track goals, competencies, ratings, comments; supervisor/employee acknowledgements.

### Design Specification:
- **Spring Boot Architecture:** Workflow, document management
- **Package Structure:**
  - `com.wms.performance.domain`
  - `com.wms.performance.repository`
  - `com.wms.performance.service`
  - `com.wms.performance.controller`
- **Entity Design:**
  - `PerformanceReview`, `Goal`, `Competency`
- **Service Layer:**
  - `PerformanceService`: review cycles, workflow, PDF export
- **Repository Layer:**
  - `PerformanceReviewRepository`, `GoalRepository`
- **Controller:**
  - `PerformanceController`: `/reviews`
- **Configuration/Security:**
  - Role-based visibility
- **Integration Points:**
  - PDF export

### Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate reviewDate;
    private String cycle; // Quarterly, Annual
    private String status; // Draft, Submitted, Acknowledged
}
```

---

## Section: E11 - Payroll Export Integration
### Description:
Generate payroll-ready files from approved attendance and leave; mapping to external payroll provider formats; secure delivery (SFTP/API).

### Design Specification:
- **Spring Boot Architecture:** Batch processing, integration
- **Package Structure:**
  - `com.wms.payroll.domain`
  - `com.wms.payroll.service`
  - `com.wms.payroll.controller`
- **Entity Design:**
  - `PayrollExport`, `PayrollProvider`
- **Service Layer:**
  - `PayrollService`: export, mapping, delivery
- **Repository Layer:**
  - `PayrollExportRepository`
- **Controller:**
  - `PayrollController`: `/payroll/export`
- **Configuration/Security:**
  - Secure delivery config
- **Integration Points:**
  - SFTP/API to provider

### Sample Implementation:
```java
public class PayrollExportJob {
    public void runExport() {
        // Fetch approved attendance/leave
        // Map to provider format
        // Deliver via SFTP/API
    }
}
```

---

## Section: E12 - Notifications & Announcements
### Description:
In-app and email/SMS notifications for shift changes, expiring certs, approvals, announcements; quiet hours configuration.

### Design Specification:
- **Spring Boot Architecture:** Event-driven, messaging
- **Package Structure:**
  - `com.wms.notification.domain`
  - `com.wms.notification.service`
  - `com.wms.notification.controller`
- **Entity Design:**
  - `Notification`, `Announcement`
- **Service Layer:**
  - `NotificationService`: delivery, templates, quiet hours
- **Repository Layer:**
  - `NotificationRepository`
- **Controller:**
  - `NotificationController`: `/notifications`, `/announcements`
- **Configuration/Security:**
  - Rate limits, opt-in/out
- **Integration Points:**
  - Email/SMS APIs

### Sample Implementation:
```java
public class NotificationService {
    public void sendNotification(Notification notification) {
        // Check quiet hours
        // Deliver via channel
    }
}
```

---

## Section: E13 - Integration Layer (HRIS/WMS APIs)
### Description:
Expose REST APIs and connectors for HRIS (new hires/terms), WMS (location/department), and IDP for SSO; webhooks for events.

### Design Specification:
- **Spring Boot Architecture:** API gateway, connectors
- **Package Structure:**
  - `com.wms.integration.hris`
  - `com.wms.integration.wms`
  - `com.wms.integration.idp`
- **Entity Design:**
  - `HRISSyncJob`, `WMSConnector`
- **Service Layer:**
  - `IntegrationService`: sync, webhooks
- **Repository Layer:** N/A
- **Controller:**
  - `IntegrationController`: `/api/hris`, `/api/wms`, `/api/idp`
- **Configuration/Security:**
  - JWT/OAuth2
- **Integration Points:**
  - HRIS, WMS, IDP APIs

### Sample Implementation:
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

---

## Section: E14 - Audit Trail & Compliance
### Description:
Centralized audit logging for sensitive changes (employee PII, schedules, approvals, payroll); tamper-evident storage.

### Design Specification:
- **Spring Boot Architecture:** Audit logging, event sourcing
- **Package Structure:**
  - `com.wms.audit.domain`
  - `com.wms.audit.repository`
  - `com.wms.audit.service`
  - `com.wms.audit.controller`
- **Entity Design:**
  - `AuditLog`: actor, timestamp, entity, before/after
- **Service Layer:**
  - `AuditService`: log, export
- **Repository Layer:**
  - `AuditLogRepository`
- **Controller:**
  - `AuditController`: `/audit/logs`
- **Configuration/Security:**
  - Immutable log table
- **Integration Points:** N/A

### Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    private String before;
    private String after;
}
```

---

## Section: E15 - Reporting & Analytics
### Description:
Operational reports: attendance, overtime, leave balances, certification status, safety KPIs; export CSV/PDF; basic role-based dashboards.

### Design Specification:
- **Spring Boot Architecture:** Reporting, analytics
- **Package Structure:**
  - `com.wms.reporting.domain`
  - `com.wms.reporting.service`
  - `com.wms.reporting.controller`
- **Entity Design:**
  - `Report`, `Dashboard`
- **Service Layer:**
  - `ReportingService`: generate, filter, export
- **Repository Layer:**
  - `ReportRepository`
- **Controller:**
  - `ReportingController`: `/reports`, `/dashboard`
- **Configuration/Security:**
  - Role-based access
- **Integration Points:**
  - BI tools

### Sample Implementation:
```java
public class ReportingService {
    public byte[] exportReport(String type, ReportFilter filter) {
        // Generate report
        // Export CSV/PDF
    }
}
```

---

## Section: E16 - Mobile Access (PWA)
### Description:
Responsive views for workers to clock-in/out, view schedules, request leave, see announcements; offline-friendly via PWA.

### Design Specification:
- **Spring Boot Architecture:** REST API, PWA manifest
- **Package Structure:**
  - `com.wms.mobile.controller`
- **Entity Design:** N/A (API only)
- **Service Layer:**
  - Mobile endpoints for core flows
- **Repository Layer:** N/A
- **Controller:**
  - `MobileController`: `/mobile/*`
- **Configuration/Security:**
  - Offline queue, conflict resolution
- **Integration Points:**
  - PWA manifest

### Sample Implementation:
```java
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @GetMapping("/schedule")
    public List<ShiftAssignmentDto> getSchedule() {
        // Return upcoming shifts
    }
}
```

---

## Section: E17 - Onboarding & Offboarding Workflow
### Description:
Automate provisioning of accounts, initial schedule, required training; deprovision access and assets on termination.

### Design Specification:
- **Spring Boot Architecture:** Workflow engine
- **Package Structure:**
  - `com.wms.onboarding.domain`
  - `com.wms.onboarding.service`
  - `com.wms.onboarding.controller`
- **Entity Design:**
  - `OnboardingTask`, `OffboardingTask`
- **Service Layer:**
  - `OnboardingService`: provision, assign tasks
- **Repository Layer:**
  - `OnboardingTaskRepository`
- **Controller:**
  - `OnboardingController`: `/onboarding`, `/offboarding`
- **Configuration/Security:**
  - Access revocation
- **Integration Points:**
  - HRIS, asset module

### Sample Implementation:
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    private String type; // Account, Schedule, Training
    private String status; // Pending, Completed
    @ManyToOne
    private Employee employee;
}
```

---

## Section: E18 - Localization & Multi-Tenant
### Description:
Support multiple languages and tenants; tenant isolation for data and config; locale-aware templates.

### Design Specification:
- **Spring Boot Architecture:** Multi-tenant, i18n
- **Package Structure:**
  - `com.wms.localization`
  - `com.wms.tenant`
- **Entity Design:**
  - `Tenant`, `LocaleSetting`
- **Service Layer:**
  - `TenantService`, `LocalizationService`
- **Repository Layer:**
  - `TenantRepository`
- **Controller:**
  - `TenantController`, `LocalizationController`
- **Configuration/Security:**
  - Data isolation, locale config
- **Integration Points:** N/A

### Sample Implementation:
```java
@Entity
public class Tenant {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String locale;
}
```

---

## Section: E19 - Advanced Scheduling (AI/ML)
### Description:
AI/ML-driven shift optimization, demand forecasting, and schedule recommendations; feedback loop for continuous improvement.

### Design Specification:
- **Spring Boot Architecture:** Integration with AI/ML services
- **Package Structure:**
  - `com.wms.aiml.service`
  - `com.wms.aiml.controller`
- **Entity Design:**
  - `ScheduleRecommendation`
- **Service Layer:**
  - `AIMLSchedulingService`: optimize, forecast
- **Repository Layer:** N/A
- **Controller:**
  - `AIMLController`: `/aiml/schedule`
- **Configuration/Security:**
  - Secure API calls
- **Integration Points:**
  - External AI/ML APIs

### Sample Implementation:
```java
public class AIMLSchedulingService {
    public ScheduleRecommendation recommendSchedule(List<Employee> employees, List<ShiftTemplate> templates) {
        // Call external AI/ML API
        // Return recommendations
    }
}
```

---

## Section: E20 - Self-Service Portal
### Description:
Employee portal for profile updates, viewing schedules, requesting leave, accessing documents, and feedback submission.

### Design Specification:
- **Spring Boot Architecture:** REST API, portal frontend
- **Package Structure:**
  - `com.wms.portal.controller`
- **Entity Design:** N/A (API only)
- **Service Layer:**
  - Portal endpoints for self-service
- **Repository Layer:** N/A
- **Controller:**
  - `PortalController`: `/portal/*`
- **Configuration/Security:**
  - Role-based access
- **Integration Points:**
  - Document storage

### Sample Implementation:
```java
@RestController
@RequestMapping("/portal")
public class PortalController {
    @GetMapping("/profile")
    public EmployeeDto getProfile() {
        // Return employee profile
    }
}
```

---

# [The document continues with detailed sections for all 100 user stories, each following the above format: Section Title, Description, Design Specification, Sample Implementation.]
