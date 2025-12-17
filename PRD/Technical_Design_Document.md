# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

---

Section: User Story 1 - Initialize Spring Boot Project Scaffolding
Description: Establishes the foundational structure for the EMS application, ensuring modularity, maintainability, and adherence to Spring Boot best practices.
Design Specification:
- Spring Boot (Maven) project with parent POM
- Base package: `com.warehouse.ems`
- Modules: `employee`, `scheduling`, `attendance`, `safety`, `common`
- Directory structure:
  - `com.warehouse.ems.employee`
  - `com.warehouse.ems.scheduling`
  - `com.warehouse.ems.attendance`
  - `com.warehouse.ems.safety`
  - `com.warehouse.ems.common`
- Application entry: `EmsApplication.java`
- README with build/run instructions
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

Section: User Story 2 - Configure Flyway/Liquibase for Database Migrations
Description: Ensures all database schema changes are versioned and safely applied across environments.
Design Specification:
- Add Flyway/Liquibase dependency in `pom.xml`
- Place migration scripts in `src/main/resources/db/migration` (Flyway) or `db/changelog` (Liquibase)
- Baseline migration for initial schema
- Enable auto-run on startup
Sample Implementation:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

---

Section: User Story 3 - Integrate Spring Boot Actuator for Health Checks
Description: Provides endpoints for monitoring application health, metrics, and readiness/liveness probes.
Design Specification:
- Add Actuator dependency
- Expose `/actuator/health`, `/actuator/metrics`, `/actuator/info`
- Secure actuator endpoints (see User Story 9)
Sample Implementation:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

---

Section: User Story 4 - Create Employee Record via API
Description: Enables HR to create new employee records with all required attributes.
Design Specification:
- Entity: `Employee` (fields: id, name, badgeId, role, department, shiftGroup, hireDate, status)
- Repository: `EmployeeRepository extends JpaRepository<Employee, Long>`
- Service: `EmployeeService.createEmployee(EmployeeDto)`
- Controller: `POST /api/employees`
- Validation: Unique badgeId, required fields
Sample Implementation:
```java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    private String name;
    @Column(unique = true)
    private String badgeId;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    // getters/setters
}
```

---

Section: User Story 5 - Update Employee Record via API
Description: Allows HR to update employee details while maintaining data integrity.
Design Specification:
- Service: `EmployeeService.updateEmployee(Long id, EmployeeDto)`
- Controller: `PUT /api/employees/{id}`
- Validation: BadgeId uniqueness, partial updates
Sample Implementation:
```java
@PutMapping("/api/employees/{id}")
public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable Long id, @RequestBody EmployeeDto dto) {
    return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
}
```

---

Section: User Story 6 - Delete Employee Record via API
Description: Enables HR to remove obsolete or incorrect employee records (soft delete preferred).
Design Specification:
- Service: `EmployeeService.deleteEmployee(Long id)`
- Controller: `DELETE /api/employees/{id}`
- Soft delete: Add `deleted` boolean field
Sample Implementation:
```java
@DeleteMapping("/api/employees/{id}")
public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
    employeeService.deleteEmployee(id);
    return ResponseEntity.noContent().build();
}
```

---

Section: User Story 7 - Retrieve Employee Records via API
Description: Provides filtered and paginated access to employee records.
Design Specification:
- Controller: `GET /api/employees?name=&badgeId=&department=&status=`
- Repository: Custom query methods for filtering
- Pagination: `Pageable` support
Sample Implementation:
```java
@GetMapping("/api/employees")
public Page<EmployeeDto> getEmployees(EmployeeFilter filter, Pageable pageable) {
    return employeeService.getEmployees(filter, pageable);
}
```

---

Section: User Story 8 - Bulk Import Employee Records via CSV
Description: Allows HR to efficiently onboard multiple employees using CSV upload.
Design Specification:
- Controller: `POST /api/employees/import`
- Service: Parse CSV, validate, batch insert
- Error handling: Return import summary with errors
Sample Implementation:
```java
@PostMapping("/api/employees/import")
public ImportResult importEmployees(@RequestParam("file") MultipartFile file) {
    return employeeService.importEmployees(file);
}
```

---

Section: User Story 9 - Define User Roles in System
Description: Establishes RBAC for ADMIN, HR, SUPERVISOR, WORKER roles.
Design Specification:
- Enum: `Role { ADMIN, HR, SUPERVISOR, WORKER }`
- Spring Security config: Method/endpoint security
- Role assignment in `Employee` entity
Sample Implementation:
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
public void createEmployee(EmployeeDto dto) { ... }
```

---

Section: User Story 10 - Time & Attendance Clock In/Out
Description: Enables workers to clock in/out, records shift association, and supports missed punch correction.
Design Specification:
- Entity: `AttendanceEvent` (employee, timestamp, type, shift, deviceId, status)
- Controller: `POST /api/attendance/clock-in`, `POST /api/attendance/clock-out`
- Service: Validates shift, handles missed punch workflow
Sample Implementation:
```java
@PostMapping("/api/attendance/clock-in")
public AttendanceEvent clockIn(@RequestBody ClockEventDto dto) {
    return attendanceService.clockIn(dto);
}
```

---

Section: User Story 11 - Shift & Schedule Management
Description: Allows supervisors to create recurring shift templates and assign schedules in bulk.
Design Specification:
- Entity: `ShiftTemplate`, `ShiftAssignment`
- Controller: `POST /api/shifts/templates`, `POST /api/shifts/assignments/bulk`
- Service: Conflict detection, bulk assignment
Sample Implementation:
```java
@PostMapping("/api/shifts/assignments/bulk")
public BulkAssignResult assignShifts(@RequestBody BulkAssignDto dto) {
    return shiftService.bulkAssign(dto);
}
```

---

Section: User Story 12 - Leave & Absence Management
Description: Manages employee leave requests, approvals, and integration with scheduling.
Design Specification:
- Entity: `LeaveRequest` (employee, type, startDate, endDate, status, balance)
- Controller: `POST /api/leaves`, `PUT /api/leaves/{id}/approve`
- Service: Balance check, auto-flag scheduled shifts
Sample Implementation:
```java
@PostMapping("/api/leaves")
public LeaveRequest requestLeave(@RequestBody LeaveRequestDto dto) {
    return leaveService.requestLeave(dto);
}
```

---

Section: User Story 13 - Training & Certification Tracking
Description: Tracks employee certifications, expirations, and blocks unqualified assignments.
Design Specification:
- Entity: `Certification` (employee, type, expiryDate, documentUrl)
- Controller: `POST /api/certifications`, `GET /api/certifications/expiring`
- Service: Expiry alerts, assignment validation
Sample Implementation:
```java
@GetMapping("/api/certifications/expiring")
public List<Certification> getExpiringCerts(@RequestParam LocalDate before) {
    return certService.findExpiring(before);
}
```

---

Section: User Story 14 - Safety Incidents & OSHA Reporting
Description: Records safety incidents, supports investigation workflow, and generates OSHA reports.
Design Specification:
- Entity: `SafetyIncident` (id, date, location, description, severity, status, involvedEmployees)
- Controller: `POST /api/safety/incidents`, `GET /api/safety/reports/osha`
- Service: Investigation workflow, export OSHA fields
Sample Implementation:
```java
@PostMapping("/api/safety/incidents")
public SafetyIncident reportIncident(@RequestBody SafetyIncidentDto dto) {
    return safetyService.reportIncident(dto);
}
```

---

Section: User Story 15 - Equipment & Asset Assignment
Description: Assigns and tracks equipment/assets, blocks use if certification is missing.
Design Specification:
- Entity: `Asset`, `AssetAssignment`
- Controller: `POST /api/assets/assign`, `POST /api/assets/return`
- Service: Certification check, overdue report
Sample Implementation:
```java
@PostMapping("/api/assets/assign")
public AssetAssignment assignAsset(@RequestBody AssetAssignDto dto) {
    return assetService.assignAsset(dto);
}
```

---

Section: User Story 16 - Performance Reviews & Goals
Description: Supports structured performance reviews, goal tracking, and immutable history.
Design Specification:
- Entity: `PerformanceReview` (employee, cycle, goals, ratings, comments, status)
- Controller: `POST /api/reviews`, `PUT /api/reviews/{id}/acknowledge`
- Service: Review cycle, PDF export
Sample Implementation:
```java
@PostMapping("/api/reviews")
public PerformanceReview createReview(@RequestBody ReviewDto dto) {
    return reviewService.createReview(dto);
}
```

---

Section: User Story 17 - Payroll Export Integration
Description: Generates payroll-ready exports from attendance/leave data and delivers securely.
Design Specification:
- Service: `PayrollExportService.generateExport(period)`
- Controller: `POST /api/payroll/export`
- Integration: SFTP/API delivery, retry logic
Sample Implementation:
```java
@PostMapping("/api/payroll/export")
public ResponseEntity<?> exportPayroll(@RequestBody ExportRequest req) {
    return payrollExportService.generateExport(req);
}
```

---

Section: User Story 18 - Notifications & Announcements
Description: Delivers notifications for shift changes, expiring certs, and announcements via multiple channels.
Design Specification:
- Entity: `Notification`, `Announcement`
- Controller: `POST /api/notifications/subscribe`, `POST /api/announcements`
- Service: Channel opt-in/out, delivery status, rate limiting
Sample Implementation:
```java
@PostMapping("/api/notifications/subscribe")
public void subscribe(@RequestBody NotificationSubscriptionDto dto) {
    notificationService.subscribe(dto);
}
```

---

Section: User Story 19 - Integration Layer (HRIS/WMS APIs)
Description: Synchronizes employee and warehouse data with external HRIS/WMS systems, supports SSO.
Design Specification:
- REST APIs: `/api/integrations/hris`, `/api/integrations/wms`
- Security: JWT/OAuth2
- Webhooks for events
Sample Implementation:
```java
@RestController
@RequestMapping("/api/integrations/hris")
public class HrisIntegrationController {
    @PostMapping
    public void syncEmployee(@RequestBody HrisEmployeeDto dto) {
        hrisService.syncEmployee(dto);
    }
}
```

---

Section: User Story 20 - Audit Trail & Compliance
Description: Centralized, immutable audit logging for sensitive changes.
Design Specification:
- Entity: `AuditLog` (actor, timestamp, entity, before, after, action)
- Service: Log on create/update/delete
- Controller: `GET /api/audit-logs`
Sample Implementation:
```java
public void logChange(String actor, String entity, Object before, Object after, String action) {
    auditLogRepository.save(new AuditLog(actor, LocalDateTime.now(), entity, before, after, action));
}
```

---

Section: User Story 21 - Reporting & Analytics
Description: Generates and exports operational reports, supports dashboards and BI integration.
Design Specification:
- Controller: `GET /api/reports/attendance`, `GET /api/reports/overtime`, etc.
- Service: Filtering, CSV/PDF export, metrics endpoints
Sample Implementation:
```java
@GetMapping("/api/reports/attendance")
public ReportResult getAttendanceReport(@RequestParam ReportFilter filter) {
    return reportService.getAttendanceReport(filter);
}
```

---

Section: User Story 22 - Mobile Access (PWA)
Description: Provides mobile-friendly, offline-capable access to core EMS features.
Design Specification:
- PWA manifest and service worker
- Responsive UI for clock-in/out, schedules, leave, announcements
- Offline queue for clock events
Sample Implementation:
```json
{
  "short_name": "EMS",
  "name": "Warehouse EMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff"
}
```

---

Section: User Story 23 - Onboarding & Offboarding Workflow
Description: Automates onboarding/offboarding tasks, including account provisioning, training, and asset management.
Design Specification:
- Service: Onboarding task generator, offboarding workflow
- Integration: HRIS triggers, asset collection
Sample Implementation:
```java
public void onboardEmployee(Employee employee) {
    trainingService.assignRequiredTraining(employee);
    assetService.assignInitialAssets(employee);
}
```

---

Section: User Story 24 - Localization & Multi-Warehouse
Description: Supports multiple warehouse sites and localized settings/languages.
Design Specification:
- Entity: `Warehouse` (id, name, location, settings)
- Controller: `POST /api/warehouses`, `GET /api/warehouses`
- i18n: Message bundles, Accept-Language header
Sample Implementation:
```java
@GetMapping("/api/warehouses")
public List<Warehouse> getWarehouses() {
    return warehouseService.getAll();
}
```

---

Section: User Story 25 - Advanced Scheduling (AI-Assisted)
Description: AI-assisted shift assignment and conflict resolution for optimized staffing.
Design Specification:
- Service: `AiSchedulingService.suggestAssignments()`
- Integration: AI/ML engine (external or embedded)
- Controller: `POST /api/shifts/ai-assign`
Sample Implementation:
```java
@PostMapping("/api/shifts/ai-assign")
public ShiftAssignmentResult aiAssign(@RequestBody AiAssignRequest req) {
    return aiSchedulingService.suggestAssignments(req);
}
```

---

Section: User Story 26 - Document Management
Description: Securely uploads, manages, and stores employee/compliance documents with access control.
Design Specification:
- Entity: `Document` (id, owner, type, url, accessLevel)
- Controller: `POST /api/documents/upload`, `GET /api/documents/{id}`
- Service: Storage integration (S3, filesystem), access checks
Sample Implementation:
```java
@PostMapping("/api/documents/upload")
public Document uploadDocument(@RequestParam MultipartFile file, @RequestParam Long ownerId) {
    return documentService.upload(file, ownerId);
}
```

---

# End of Document
