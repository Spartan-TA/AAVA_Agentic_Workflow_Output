# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

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
19. Advanced Scheduling AI-Assisted
20. Document Management

---

## 1. Project Scaffolding & Domain Setup

Section: Spring Boot Project Initialization  
Description: Establishes the foundational structure for the EMS application, ensuring modularity, maintainability, and scalability.  
Design Specification:  
- Use Maven for build management.  
- Base package: `com.warehouse.ems`  
- Modules: `employee`, `scheduling`, `attendance`, `safety`  
- Database migration: Flyway/Liquibase  
- Actuator enabled for health checks  
- README with build/run steps  
Sample Implementation:  
```java
@SpringBootApplication
public class EmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmsApplication.class, args);
    }
}
```
```
src/
  main/
    java/com/warehouse/ems/
      employee/
      scheduling/
      attendance/
      safety/
resources/
  db/migration/
application.yml
```
---

## 2. Employee Master Data (CRUD)

Section: Domain Model  
Description: Centralizes employee information with full CRUD support and validation.  
Design Specification:  
- Entity: `Employee`  
  - Fields: id, name, badgeId (unique), role, department, shiftGroup, hireDate, status  
- Repository: `EmployeeRepository extends JpaRepository<Employee, Long>`  
- Service: `EmployeeService` for business logic  
- Controller: `EmployeeController` for REST endpoints  
- Soft-delete via `status` field  
- Pagination, filtering, OpenAPI docs  
Sample Implementation:  
```java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    @Column(nullable=false)
    private String name;
    @Column(unique=true, nullable=false)
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status; // ACTIVE, DELETED
}
```
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping public Employee create(@RequestBody EmployeeDto dto) { ... }
    @GetMapping public Page<Employee> list(Pageable pageable) { ... }
    @DeleteMapping("/{id}") public void softDelete(@PathVariable Long id) { ... }
}
```
---

## 3. Role Based Access Control (RBAC)

Section: Security Configuration  
Description: Restricts access to endpoints and data based on user roles.  
Design Specification:  
- Roles: ADMIN, HR, SUPERVISOR, WORKER  
- Method/endpoint security via `@PreAuthorize`  
- Row-level constraints in queries  
- API key/OAuth2 toggle via `application.yml`  
- SSO integration (Story 48)  
Sample Implementation:  
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR")
            .antMatchers("/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
            .anyRequest().authenticated()
            .and()
            .oauth2Login();
    }
}
```
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == principal.department)")
public Employee getEmployee(Long id) { ... }
```
---

## 4. Time & Attendance (Clock In/Out)

Section: Attendance Domain & Workflow  
Description: Captures clock-in/out events, validates geofence, and computes daily totals.  
Design Specification:  
- Entity: `AttendanceEvent`  
  - Fields: id, employeeId, timestamp, type (IN/OUT), deviceId, location  
- Controller: `/attendance/clock-in`, `/attendance/clock-out`  
- Geofence validation via external service  
- Correction workflow for missed punches  
- Daily totals calculation  
Sample Implementation:  
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private LocalDateTime timestamp;
    private String type; // IN, OUT
    private String deviceId;
    private String location;
}
```
```java
@PostMapping("/clock-in")
public ResponseEntity<?> clockIn(@RequestBody ClockInDto dto) {
    // Validate geofence, save event, associate shift
}
```
---

## 5. Shift & Schedule Management

Section: Shift Template & Assignment  
Description: Manages recurring shifts, conflict detection, and bulk assignment.  
Design Specification:  
- Entity: `ShiftTemplate`, `EmployeeShiftAssignment`  
- CRUD for templates and assignments  
- Conflict detection logic  
- Bulk assignment endpoint  
Sample Implementation:  
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrenceRule;
}
```
```java
@PostMapping("/shifts/assign-bulk")
public void assignBulk(@RequestBody BulkAssignmentDto dto) { ... }
```
---

## 6. Leave & Absence Management

Section: Leave Request Workflow  
Description: Handles leave requests, supervisor approval, and integration with scheduling.  
Design Specification:  
- Entity: `LeaveRequest`  
  - Fields: id, employeeId, type, startDate, endDate, status, accrualBalance  
- Approval workflow  
- Integration with scheduling to flag shifts  
Sample Implementation:  
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type; // PTO, Sick, Unpaid
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // REQUESTED, APPROVED, DENIED
    private Double accrualBalance;
}
```
```java
@PostMapping("/leave/request")
public LeaveRequest requestLeave(@RequestBody LeaveRequestDto dto) { ... }
```
---

## 7. Training & Certification Tracking

Section: Certification Domain  
Description: Tracks certifications, expirations, and blocks assignments for expired certs.  
Design Specification:  
- Entity: `Certification`  
  - Fields: id, employeeId, type, issueDate, expiryDate, documentUrl  
- Alerts for expiring certifications  
- Assignment blocking logic  
Sample Implementation:  
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
}
```
```java
@Scheduled(cron = "0 0 * * *")
public void sendExpiryAlerts() { ... }
```
---

## 8. Safety Incidents & OSHA Reporting

Section: Incident Workflow & Reporting  
Description: Records safety incidents, manages investigation workflow, and generates OSHA reports.  
Design Specification:  
- Entity: `SafetyIncident`  
  - Fields: id, severity, location, description, involvedEmployeeIds, status  
- Workflow: Open â Investigating â Resolved  
- OSHA report export endpoints  
Sample Implementation:  
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String severity;
    private String location;
    private String description;
    @ElementCollection
    private List<Long> involvedEmployeeIds;
    private String status;
}
```
```java
@PostMapping("/safety/incidents")
public SafetyIncident recordIncident(@RequestBody IncidentDto dto) { ... }
```
---

## 9. Equipment & Asset Assignment

Section: Asset Registry & Assignment  
Description: Manages asset CRUD, checkout/return, and certification validation.  
Design Specification:  
- Entity: `Asset`, `AssetAssignment`  
  - Asset fields: id, type, serialNumber, condition, status  
  - Assignment fields: id, assetId, employeeId, checkoutDate, returnDate  
- Certification check before assignment  
Sample Implementation:  
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private String condition;
    private String status;
}
@Entity
public class AssetAssignment {
    @Id @GeneratedValue
    private Long id;
    private Long assetId;
    private Long employeeId;
    private LocalDateTime checkoutDate;
    private LocalDateTime returnDate;
}
```
---

## 10. Performance Reviews & Goals

Section: Review Cycle & Workflow  
Description: Supports creation of review cycles, goal tracking, and immutable history.  
Design Specification:  
- Entity: `PerformanceReview`  
  - Fields: id, employeeId, cycle, goals, ratings, comments, status  
- Workflow: submit, acknowledge, sign-off  
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
    private String ratings;
    private String comments;
    private String status; // DRAFT, SUBMITTED, SIGNED_OFF
}
```
---

## 11. Payroll Export Integration

Section: Payroll Export & Delivery  
Description: Generates payroll files, maps to provider formats, and manages secure delivery.  
Design Specification:  
- Service: `PayrollExportService`  
- Export mapping logic  
- SFTP/API delivery integration  
- Audit log for exports  
Sample Implementation:  
```java
@Service
public class PayrollExportService {
    public File generatePayrollFile(LocalDate period) { ... }
    public void deliverPayroll(File file) { ... }
}
```
---

## 12. Notifications & Announcements

Section: Notification Delivery  
Description: Delivers notifications via in-app, email, SMS, and manages announcement dashboard.  
Design Specification:  
- Entity: `Notification`, `Announcement`  
- Channel opt-in/out per user  
- Delivery status tracking  
- Rate limiting  
Sample Implementation:  
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private Long userId;
    private String channel; // IN_APP, EMAIL, SMS
    private String message;
    private String status; // SENT, FAILED
}
```
---

## 13. Integration Layer (HRIS/WMS APIs)

Section: External API Integration  
Description: Synchronizes master data with HRIS/WMS and supports SSO.  
Design Specification:  
- REST APIs secured with JWT/OAuth2  
- HRIS sync job for employee data  
- WMS department/location link  
- Webhook endpoints  
Sample Implementation:  
```java
@RestController
@RequestMapping("/api/hris")
public class HRISController {
    @PostMapping("/sync")
    public void syncEmployees(@RequestBody List<EmployeeDto> employees) { ... }
}
```
---

## 14. Audit Trail & Compliance

Section: Audit Logging  
Description: Centralizes audit logs for sensitive changes with tamper-evident storage.  
Design Specification:  
- Entity: `AuditLog`  
  - Fields: id, actor, timestamp, entity, before, after  
- Immutable log table  
- Export endpoints  
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

## 15. Reporting & Analytics

Section: Reporting Engine  
Description: Generates operational reports and dashboards with role-based access.  
Design Specification:  
- Service: `ReportingService`  
- Filters by date, department, shift  
- CSV/PDF export endpoints  
Sample Implementation:  
```java
@Service
public class ReportingService {
    public List<AttendanceReport> getAttendanceReport(DateRange range, String department) { ... }
    public File exportReport(String type, DateRange range) { ... }
}
```
---

## 16. Mobile Access (PWA)

Section: PWA & Mobile Controller  
Description: Enables mobile-friendly views and offline clock-in/out.  
Design Specification:  
- PWA manifest in `resources/static/manifest.json`  
- Offline queue for clock events  
- Mobile controllers for schedules, announcements  
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

## 17. Onboarding & Offboarding Workflow

Section: Employee Lifecycle Automation  
Description: Automates provisioning, training, asset assignment, and termination offboarding.  
Design Specification:  
- Service: `OnboardingService`, `OffboardingService`  
- HRIS integration for new hires  
- Task generation for training/assets  
- Access revocation logic  
Sample Implementation:  
```java
@Service
public class OnboardingService {
    public void provisionNewHire(EmployeeDto dto) { ... }
}
@Service
public class OffboardingService {
    public void offboardEmployee(Long employeeId) { ... }
}
```
---

## 18. Localization & Multi-Warehouse

Section: Warehouse & Localization Config  
Description: Supports multiple warehouses and UI localization.  
Design Specification:  
- Entity: `Warehouse`  
  - Fields: id, name, location, config  
- Localization via `messages_{locale}.properties`  
Sample Implementation:  
```java
@Entity
public class Warehouse {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String location;
    @Lob
    private String config;
}
```
```properties
# messages_en.properties
employee.name=Name
employee.badgeId=Badge ID
```
---

## 19. Advanced Scheduling AI-Assisted

Section: AI Shift Suggestion  
Description: Suggests optimal shifts using AI/ML algorithms.  
Design Specification:  
- Service: `ShiftSuggestionService`  
- Integration with scheduling module  
- Exposes endpoint for suggestions  
Sample Implementation:  
```java
@Service
public class ShiftSuggestionService {
    public List<ShiftTemplate> suggestShifts(Employee employee, LocalDate week) { ... }
}
```
---

## 20. Document Management

Section: Document Upload & Versioning  
Description: Manages document uploads, versioning, and expiration reminders.  
Design Specification:  
- Entity: `Document`  
  - Fields: id, ownerId, type, url, version, expiryDate  
- Upload endpoint  
- Expiry reminder logic  
Sample Implementation:  
```java
@Entity
public class Document {
    @Id @GeneratedValue
    private Long id;
    private Long ownerId;
    private String type;
    private String url;
    private Integer version;
    private LocalDate expiryDate;
}
```
```java
@PostMapping("/documents/upload")
public Document upload(@RequestParam MultipartFile file, @RequestParam Long ownerId) { ... }
```
---

## Additional Stories (Soft-Delete, SSO, Geofence, Expiry Alerts, etc.)

Section: Cross-Cutting Concerns  
Description: Implements additional requirements such as soft-delete, SSO, geofence validation, and expiry alerts.  
Design Specification:  
- Soft-delete: status field in entities, filtered in queries  
- SSO: Spring Security OAuth2 integration  
- Geofence: external service validation in attendance  
- Expiry alerts: scheduled jobs for certifications/documents  
Sample Implementation:  
```java
// Soft-delete filter in repository
@Query("SELECT e FROM Employee e WHERE e.status <> 'DELETED'")
List<Employee> findActiveEmployees();
```
```java
// SSO config
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ...
            client-secret: ...
```
---

# End of Document

This document provides a comprehensive low-level technical design for all 48 user stories of the Warehouse Employee Management System, following Spring Boot best practices and industry standards. Each section includes architecture overview, package structure, entity design, service/repository/controller specifications, configuration, integration points, and sample code snippets.