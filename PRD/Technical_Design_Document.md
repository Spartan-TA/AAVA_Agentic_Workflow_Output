# Warehouse Employee Management System - Technical Design Document

> **Traceability:** This document covers all 80 user stories across 20 epics for the Warehouse Employee Management System, providing low-level technical design for Spring Boot developers. Each section is mapped to a user story and includes rationale, design specification, and sample implementation.

---

## EPIC E01: Project Scaffolding & Domain Setup

---

# USER STORY E01-1: Initialize Spring Boot Project

Section: Project Initialization

Description: Establishes the foundational Spring Boot project using Maven, ensuring modularity and scalability. Uses Java 17+, Maven, and follows standard package conventions.

Design Specification:
- Spring Boot (Maven) project, Java 17+
- Base package: `com.wms`
- Modules: `employee`, `scheduling`, `attendance`, `safety`
- Directory structure: `/src/main/java/com/wms/{module}`
- Actuator enabled for health checks
- Flyway/Liquibase for DB migrations

Sample Implementation:
```java
// pom.xml (excerpt)
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.1.0</version>
</parent>
<properties>
    <java.version>17</java.version>
</properties>
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <!-- Other dependencies -->
</dependencies>

// application.yml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wms
    username: wms_user
    password: wms_pass
  flyway:
    enabled: true

// Main Application
package com.wms;
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}
```

---

# USER STORY E01-2: Configure Base Packages

Section: Package Structure

Description: Defines a clear package structure for maintainability and separation of concerns.

Design Specification:
- Base package: `com.wms`
- Sub-packages: `employee`, `scheduling`, `attendance`, `safety`
- Each module: `domain`, `repository`, `service`, `controller`, `dto`, `config`

Sample Implementation:
```java
// Example structure
com.wms.employee.domain.Employee
com.wms.employee.repository.EmployeeRepository
com.wms.employee.service.EmployeeService
com.wms.employee.controller.EmployeeController
com.wms.employee.dto.EmployeeDTO
com.wms.employee.config.EmployeeConfig
```

---

# USER STORY E01-3: Database Migration Setup

Section: Database Migration

Description: Ensures repeatable, versioned schema changes using Flyway/Liquibase.

Design Specification:
- Flyway/Liquibase configured in `application.yml`
- Migration scripts in `/src/main/resources/db/migration`
- Baseline migration for core tables

Sample Implementation:
```sql
-- V1__init.sql
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    department VARCHAR(50),
    shift_group VARCHAR(50),
    hire_date DATE,
    status VARCHAR(20) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE
);
```

---

# USER STORY E01-4: Enable Actuator Health Endpoint

Section: Monitoring & Health

Description: Enables Spring Boot Actuator for health checks and monitoring.

Design Specification:
- `spring-boot-starter-actuator` dependency
- `/actuator/health` endpoint enabled
- Custom health indicators as needed

Sample Implementation:
```java
// application.yml
management:
  endpoints:
    web:
      exposure:
        include: health, info

// Custom Health Indicator
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    @Autowired DataSource dataSource;
    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection()) {
            return Health.up().build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
```

---

## EPIC E02: Employee Master Data CRUD

---

# USER STORY E02-1: Create Employee Entity and Repository

Section: Domain Model & Repository

Description: Defines the Employee entity and repository for persistence.

Design Specification:
- Employee entity with fields: id, badgeId, name, role, department, shiftGroup, hireDate, status, deleted
- JPA annotations for mapping
- Repository extends `JpaRepository`

Sample Implementation:
```java
@Entity
@Table(name = "employee")
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String badgeId;
    @Column(nullable = false)
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @Column(nullable = false)
    private String status;
    private boolean deleted = false;
    // getters/setters
}

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    @Query("SELECT e FROM Employee e WHERE e.deleted = false")
    List<Employee> findAllActive();
}
```

---

# USER STORY E02-2: Implement Employee CRUD APIs

Section: Controller & Service Layer

Description: Implements RESTful CRUD endpoints for Employee management.

Design Specification:
- Endpoints: POST/GET/PUT/PATCH/DELETE `/employees`
- DTOs for request/response
- Service layer for business logic
- Validation and error handling

Sample Implementation:
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeDTO dto) {
        return ResponseEntity.ok(employeeService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> update(@PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) {
        return ResponseEntity.ok(employeeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

// DTO
public class EmployeeDTO {
    private Long id;
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    // getters/setters
}
```

---

# USER STORY E02-3: Support Soft-Delete

Section: Data Integrity

Description: Implements soft-delete for Employee records.

Design Specification:
- `deleted` flag in Employee entity
- Repository queries filter out deleted records
- DELETE endpoint sets `deleted=true`

Sample Implementation:
```java
@Service
public class EmployeeService {
    @Autowired EmployeeRepository repo;

    @Transactional
    public void delete(Long id) {
        Employee emp = repo.findById(id).orElseThrow(() -> new NotFoundException());
        emp.setDeleted(true);
        repo.save(emp);
    }
}
```

---

# USER STORY E02-4: OpenAPI Documentation

Section: API Documentation

Description: Documents Employee APIs using OpenAPI/Swagger.

Design Specification:
- `springdoc-openapi` dependency
- Annotate controllers and DTOs
- Example schemas

Sample Implementation:
```java
// pom.xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.0.2</version>
</dependency>

// Controller annotation
@Operation(summary = "Create Employee", description = "Creates a new employee record")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Employee created"),
    @ApiResponse(responseCode = "400", description = "Validation error")
})
```

---

## EPIC E03: Role-Based Access Control

---

# USER STORY E03-1: Role-Based Endpoint Security

Section: Security Configuration

Description: Secures endpoints based on user roles.

Design Specification:
- Spring Security configured
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Endpoint access controlled via `@PreAuthorize`

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
            .and().httpBasic();
    }
}

// Controller
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/employees")
public ResponseEntity<EmployeeDTO> create(@RequestBody EmployeeDTO dto) { ... }
```

---

# USER STORY E03-2: Method-Level Security

Section: Service Layer Security

Description: Enforces security at service method level.

Design Specification:
- `@PreAuthorize` on service methods
- Fine-grained access control

Sample Implementation:
```java
@Service
public class EmployeeService {
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeDTO create(EmployeeDTO dto) { ... }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public EmployeeDTO update(Long id, EmployeeDTO dto) { ... }
}
```

---

# USER STORY E03-3: API Key/OAuth2 Toggle

Section: Authentication Configuration

Description: Supports API Key and OAuth2 authentication, toggle via config.

Design Specification:
- API Key filter for simple auth
- OAuth2 for SSO
- Toggle in `application.yml`

Sample Implementation:
```java
// application.yml
security:
  auth-type: api-key # or oauth2

// API Key Filter
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    @Value("${security.api-key}")
    private String apiKey;
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        String header = req.getHeader("X-API-KEY");
        if ("api-key".equals(authType) && !apiKey.equals(header)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        chain.doFilter(req, res);
    }
}
```

---

# USER STORY E03-4: Row-Level Security

Section: Data Access Security

Description: Restricts data access at row level based on user role/team.

Design Specification:
- Service methods filter data by team/department
- Repository custom queries

Sample Implementation:
```java
@PreAuthorize("hasRole('SUPERVISOR')")
public List<EmployeeDTO> getTeamEmployees(Long supervisorId) {
    // Only employees in supervisor's team
    return repo.findBySupervisorId(supervisorId);
}
```

---

## EPIC E04: Time & Attendance

---

# USER STORY E04-1: Clock-In/Out Endpoints

Section: Attendance Controller

Description: Provides endpoints for clock-in/out events.

Design Specification:
- POST `/attendance/clock-in`, `/attendance/clock-out`
- Capture geofence, device info
- Associate with shift

Sample Implementation:
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<Void> clockIn(@RequestBody ClockEventDTO dto) {
        attendanceService.clockIn(dto);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/clock-out")
    public ResponseEntity<Void> clockOut(@RequestBody ClockEventDTO dto) {
        attendanceService.clockOut(dto);
        return ResponseEntity.ok().build();
    }
}

// DTO
public class ClockEventDTO {
    private Long employeeId;
    private LocalDateTime timestamp;
    private String deviceId;
    private String location;
    // getters/setters
}
```

---

# USER STORY E04-2: Calculate Hours Worked

Section: Attendance Service

Description: Calculates hours worked per shift.

Design Specification:
- Service computes daily totals
- Associates clock-in/out with shift

Sample Implementation:
```java
@Service
public class AttendanceService {
    public double calculateHours(Long employeeId, LocalDate date) {
        List<AttendanceEvent> events = repo.findByEmployeeIdAndDate(employeeId, date);
        // Logic to pair clock-in/out and sum durations
        return totalHours;
    }
}
```

---

# USER STORY E04-3: Handle Missed Punches

Section: Correction Workflow

Description: Handles missed punches and correction workflow.

Design Specification:
- Correction requests via endpoint
- Approval workflow for supervisors

Sample Implementation:
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/correction")
    public ResponseEntity<Void> requestCorrection(@RequestBody CorrectionDTO dto) {
        attendanceService.requestCorrection(dto);
        return ResponseEntity.ok().build();
    }
}

// DTO
public class CorrectionDTO {
    private Long employeeId;
    private LocalDate date;
    private String reason;
    // getters/setters
}
```

---

# USER STORY E04-4: Export Attendance Reports

Section: Reporting

Description: Exports attendance reports in CSV format.

Design Specification:
- Export endpoint `/attendance/report`
- CSV generation
- Pagination/filtering

Sample Implementation:
```java
@GetMapping("/report")
public ResponseEntity<Resource> exportReport(@RequestParam LocalDate start, @RequestParam LocalDate end) {
    Resource csv = attendanceService.generateReport(start, end);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance.csv")
        .body(csv);
}
```

---

## EPIC E05: Shift & Schedule Management

---

# USER STORY E05-1: Recurring Shift Templates

Section: Shift Domain & CRUD

Description: Supports recurring shift templates.

Design Specification:
- ShiftTemplate entity: id, name, startTime, endTime, recurrencePattern
- CRUD endpoints

Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String recurrencePattern; // e.g., "WEEKLY"
    // getters/setters
}

@RestController
@RequestMapping("/shifts/templates")
public class ShiftTemplateController {
    @PostMapping
    public ResponseEntity<ShiftTemplateDTO> create(@RequestBody ShiftTemplateDTO dto) {
        return ResponseEntity.ok(shiftService.createTemplate(dto));
    }
}
```

---

# USER STORY E05-2: Assign Shifts

Section: Shift Assignment

Description: Assigns shifts to employees.

Design Specification:
- ShiftAssignment entity: employeeId, shiftTemplateId, date
- Bulk assignment support

Sample Implementation:
```java
@Entity
public class ShiftAssignment {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private Long shiftTemplateId;
    private LocalDate date;
    // getters/setters
}

@RestController
@RequestMapping("/shifts/assignments")
public class ShiftAssignmentController {
    @PostMapping("/bulk")
    public ResponseEntity<Void> bulkAssign(@RequestBody List<ShiftAssignmentDTO> dtos) {
        shiftService.bulkAssign(dtos);
        return ResponseEntity.ok().build();
    }
}
```

---

# USER STORY E05-3: Detect Scheduling Conflicts

Section: Conflict Detection

Description: Prevents scheduling conflicts.

Design Specification:
- Service checks for overlapping assignments
- Validation on assignment

Sample Implementation:
```java
@Service
public class ShiftService {
    public void assignShift(ShiftAssignmentDTO dto) {
        boolean conflict = repo.existsOverlap(dto.getEmployeeId(), dto.getDate(), dto.getStartTime(), dto.getEndTime());
        if (conflict) throw new ConflictException("Scheduling conflict detected");
        // proceed with assignment
    }
}
```

---

# USER STORY E05-4: Audit Shift Assignments

Section: Audit Logging

Description: Logs shift assignment changes.

Design Specification:
- Audit log entity: actor, timestamp, action, before/after
- Audit entries on assignment

Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String action;
    private String entity;
    private String beforeState;
    private String afterState;
    // getters/setters
}

@Service
public class ShiftService {
    @Transactional
    public void assignShift(ShiftAssignmentDTO dto) {
        // ... assignment logic
        auditLogService.logAssignment(dto, actor);
    }
}
```

---

## EPIC E06: Leave & Absence Management

---

# USER STORY E06-1: Request Leave

Section: Leave Request

Description: Allows employees to request leave.

Design Specification:
- LeaveRequest entity: employeeId, type, startDate, endDate, status
- POST `/leave/request`

Sample Implementation:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type; // PTO, sick, unpaid
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // REQUESTED, APPROVED, DENIED
    // getters/setters
}

@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PostMapping("/request")
    public ResponseEntity<LeaveRequestDTO> request(@RequestBody LeaveRequestDTO dto) {
        return ResponseEntity.ok(leaveService.requestLeave(dto));
    }
}
```

---

# USER STORY E06-2: Approve/Deny Leave

Section: Leave Approval Workflow

Description: Supervisors approve/deny leave requests.

Design Specification:
- PATCH `/leave/{id}/approve`, `/leave/{id}/deny`
- Status updates

Sample Implementation:
```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id) {
        leaveService.approveLeave(id);
        return ResponseEntity.ok().build();
    }
    @PatchMapping("/{id}/deny")
    public ResponseEntity<Void> deny(@PathVariable Long id) {
        leaveService.denyLeave(id);
        return ResponseEntity.ok().build();
    }
}
```

---

# USER STORY E06-3: Integrate Leave with Scheduling

Section: Scheduling Integration

Description: Integrates leave with shift scheduling.

Design Specification:
- Scheduled shifts auto-flagged for coverage
- Service updates assignments

Sample Implementation:
```java
@Service
public class LeaveService {
    @Transactional
    public void approveLeave(Long leaveId) {
        LeaveRequest leave = repo.findById(leaveId).orElseThrow();
        leave.setStatus("APPROVED");
        repo.save(leave);
        shiftService.flagShiftsForCoverage(leave.getEmployeeId(), leave.getStartDate(), leave.getEndDate());
    }
}
```

---

# USER STORY E06-4: Track Leave Accrual

Section: Leave Accrual

Description: Tracks leave balances and accrual policies.

Design Specification:
- LeaveBalance entity: employeeId, balance, accrualRate
- Service updates balances

Sample Implementation:
```java
@Entity
public class LeaveBalance {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private double balance;
    private double accrualRate;
    // getters/setters
}

@Service
public class LeaveService {
    public void accrueLeave(Long employeeId) {
        LeaveBalance balance = repo.findByEmployeeId(employeeId);
        balance.setBalance(balance.getBalance() + balance.getAccrualRate());
        repo.save(balance);
    }
}
```

---

## EPIC E07: Training & Certification Tracking

---

# USER STORY E07-1: Track Certifications

Section: Certification Domain

Description: Tracks employee certifications.

Design Specification:
- Certification entity: employeeId, certType, issueDate, expiryDate, documentUrl
- CRUD endpoints

Sample Implementation:
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String certType;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
    // getters/setters
}

@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping
    public ResponseEntity<CertificationDTO> create(@RequestBody CertificationDTO dto) {
        return ResponseEntity.ok(certService.create(dto));
    }
}
```

---

# USER STORY E07-2: Alert on Expiring Certs

Section: Notification Service

Description: Alerts on certifications expiring soon.

Design Specification:
- Scheduled job checks expiry
- Notification sent 30/7 days before expiry

Sample Implementation:
```java
@Service
public class CertificationService {
    @Scheduled(cron = "0 0 8 * * ?")
    public void alertExpiringCerts() {
        List<Certification> expiring = repo.findExpiringWithinDays(30);
        notificationService.sendExpiryAlerts(expiring);
    }
}
```

---

# USER STORY E07-3: Block Assignment for Expired Certs

Section: Assignment Validation

Description: Blocks shift/equipment assignment if required cert is expired.

Design Specification:
- Assignment service checks cert status
- Throws exception if expired

Sample Implementation:
```java
@Service
public class AssignmentService {
    public void assignEquipment(Long employeeId, String equipmentType) {
        Certification cert = certRepo.findByEmployeeIdAndType(employeeId, equipmentType);
        if (cert == null || cert.getExpiryDate().isBefore(LocalDate.now())) {
            throw new ForbiddenException("Certification expired");
        }
        // proceed with assignment
    }
}
```

---

# USER STORY E07-4: Certification Status on Profile

Section: Employee Profile

Description: Shows certification status on employee profile.

Design Specification:
- GET `/employees/{id}/certifications`
- DTO includes cert status

Sample Implementation:
```java
@GetMapping("/{id}/certifications")
public ResponseEntity<List<CertificationDTO>> getCerts(@PathVariable Long id) {
    return ResponseEntity.ok(certService.getCertificationsForEmployee(id));
}
```

---

## EPIC E08: Safety Incidents & OSHA Reporting

---

# USER STORY E08-1: Record Safety Incidents

Section: Safety Incident Domain

Description: Records safety incidents and near-misses.

Design Specification:
- SafetyIncident entity: id, severity, location, description, involvedEmployees, status
- POST `/safety/incidents`

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
    // getters/setters
}

@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
    @PostMapping
    public ResponseEntity<SafetyIncidentDTO> record(@RequestBody SafetyIncidentDTO dto) {
        return ResponseEntity.ok(safetyService.recordIncident(dto));
    }
}
```

---

# USER STORY E08-2: Incident Investigation Workflow

Section: Workflow Management

Description: Manages investigation workflow for incidents.

Design Specification:
- Status transitions: OPEN â INVESTIGATING â RESOLVED
- PATCH endpoints for status updates

Sample Implementation:
```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyIncidentController {
    @PatchMapping("/{id}/investigate")
    public ResponseEntity<Void> investigate(@PathVariable Long id) {
        safetyService.updateStatus(id, "INVESTIGATING");
        return ResponseEntity.ok().build();
    }
    @PatchMapping("/{id}/resolve")
    public ResponseEntity<Void> resolve(@PathVariable Long id) {
        safetyService.updateStatus(id, "RESOLVED");
        return ResponseEntity.ok().build();
    }
}
```

---

# USER STORY E08-3: Generate OSHA Reports

Section: OSHA Reporting

Description: Generates OSHA summary reports.

Design Specification:
- Export endpoint `/safety/reports/osha`
- CSV/PDF generation

Sample Implementation:
```java
@GetMapping("/reports/osha")
public ResponseEntity<Resource> exportOSHAReport(@RequestParam int year) {
    Resource report = safetyService.generateOSHAReport(year);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=osha_report.pdf")
        .body(report);
}
```

---

# USER STORY E08-4: Safety Metrics Dashboard

Section: Dashboard Endpoints

Description: Provides safety metrics dashboard.

Design Specification:
- GET `/safety/metrics`
- Aggregated KPIs

Sample Implementation:
```java
@GetMapping("/metrics")
public ResponseEntity<SafetyMetricsDTO> getMetrics() {
    return ResponseEntity.ok(safetyService.getMetrics());
}
```

---

## EPIC E09: Equipment & Asset Assignment

---

# USER STORY E09-1: Asset Registry CRUD

Section: Asset Domain & CRUD

Description: Manages asset registry.

Design Specification:
- Asset entity: id, type, serialNumber, condition, assignedTo
- CRUD endpoints

Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private String condition;
    private Long assignedTo;
    // getters/setters
}

@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping
    public ResponseEntity<AssetDTO> create(@RequestBody AssetDTO dto) {
        return ResponseEntity.ok(assetService.create(dto));
    }
}
```

---

# USER STORY E09-2: Assign and Track Assets

Section: Asset Assignment

Description: Assigns assets to employees and tracks usage.

Design Specification:
- Assignment endpoints
- Asset history log

Sample Implementation:
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping("/{id}/assign")
    public ResponseEntity<Void> assign(@PathVariable Long id, @RequestParam Long employeeId) {
        assetService.assignAsset(id, employeeId);
        return ResponseEntity.ok().build();
    }
}
```

---

# USER STORY E09-3: Block Asset Use if Cert Missing

Section: Assignment Validation

Description: Blocks asset use if required certification is missing.

Design Specification:
- Service checks cert before assignment

Sample Implementation:
```java
@Service
public class AssetService {
    public void assignAsset(Long assetId, Long employeeId) {
        Asset asset = repo.findById(assetId).orElseThrow();
        Certification cert = certRepo.findByEmployeeIdAndType(employeeId, asset.getType());
        if (cert == null || cert.getExpiryDate().isBefore(LocalDate.now())) {
            throw new ForbiddenException("Certification required");
        }
        asset.setAssignedTo(employeeId);
        repo.save(asset);
    }
}
```

---

# USER STORY E09-4: Asset History Log

Section: Asset History

Description: Maintains asset assignment history.

Design Specification:
- AssetHistory entity: assetId, employeeId, action, timestamp
- Log on assignment/check-in/out

Sample Implementation:
```java
@Entity
public class AssetHistory {
    @Id @GeneratedValue
    private Long id;
    private Long assetId;
    private Long employeeId;
    private String action; // ASSIGNED, RETURNED
    private LocalDateTime timestamp;
    // getters/setters
}

@Service
public class AssetService {
    public void assignAsset(Long assetId, Long employeeId) {
        // ... assignment logic
        historyRepo.save(new AssetHistory(assetId, employeeId, "ASSIGNED", LocalDateTime.now()));
    }
}
```

---

## EPIC E10: Performance Reviews & Goals

---

# USER STORY E10-1: Create Review Templates

Section: Review Template Domain

Description: Supports creation of review templates.

Design Specification:
- ReviewTemplate entity: id, name, competencies, goals
- CRUD endpoints

Sample Implementation:
```java
@Entity
public class ReviewTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    @ElementCollection
    private List<String> competencies;
    @ElementCollection
    private List<String> goals;
    // getters/setters
}

@RestController
@RequestMapping("/reviews/templates")
public class ReviewTemplateController {
    @PostMapping
    public ResponseEntity<ReviewTemplateDTO> create(@RequestBody ReviewTemplateDTO dto) {
        return ResponseEntity.ok(reviewService.createTemplate(dto));
    }
}
```

---

# USER STORY E10-2: Assign Reviews

Section: Review Assignment

Description: Assigns reviews to employees.

Design Specification:
- ReviewAssignment entity: employeeId, templateId, cycle, status
- Assignment endpoints

Sample Implementation:
```java
@Entity
public class ReviewAssignment {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private Long templateId;
    private String cycle; // Q1, Q2, Annual
    private String status; // ASSIGNED, SUBMITTED, ACKNOWLEDGED
    // getters/setters
}

@RestController
@RequestMapping("/reviews/assignments")
public class ReviewAssignmentController {
    @PostMapping
    public ResponseEntity<ReviewAssignmentDTO> assign(@RequestBody ReviewAssignmentDTO dto) {
        return ResponseEntity.ok(reviewService.assignReview(dto));
    }
}
```

---

# USER STORY E10-3: Submit and Acknowledge Reviews

Section: Review Submission

Description: Supports review submission and acknowledgement.

Design Specification:
- PATCH `/reviews/{id}/submit`, `/reviews/{id}/acknowledge`
- Status updates

Sample Implementation:
```java
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PatchMapping("/{id}/submit")
    public ResponseEntity<Void> submit(@PathVariable Long id) {
        reviewService.submitReview(id);
        return ResponseEntity.ok().build();
    }
    @PatchMapping("/{id}/acknowledge")
    public ResponseEntity<Void> acknowledge(@PathVariable Long id) {
        reviewService.acknowledgeReview(id);
        return ResponseEntity.ok().build();
    }
}
```

---

# USER STORY E10-4: Role-Based Review Visibility

Section: Review Visibility

Description: Controls review visibility based on role.

Design Specification:
- Service filters reviews by role
- ADMIN/HR see all, SUPERVISOR sees team, EMPLOYEE sees own

Sample Implementation:
```java
@Service
public class ReviewService {
    public List<ReviewAssignmentDTO> getReviewsForUser(Long userId, String role) {
        if ("ADMIN".equals(role) || "HR".equals(role)) {
            return repo.findAll();
        } else if ("SUPERVISOR".equals(role)) {
            return repo.findBySupervisorId(userId);
        } else {
            return repo.findByEmployeeId(userId);
        }
    }
}
```

---

## EPIC E11: Payroll Export Integration

---

# USER STORY E11-1: Generate Payroll Export Files

Section: Payroll Export

Description: Generates payroll-ready files from attendance and leave.

Design Specification:
- Export endpoint `/payroll/export`
- CSV generation

Sample Implementation:
```java
@GetMapping("/payroll/export")
public ResponseEntity<Resource> exportPayroll(@RequestParam LocalDate start, @RequestParam LocalDate end) {
    Resource csv = payrollService.generateExport(start, end);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payroll.csv")
        .body(csv);
}
```

---

# USER STORY E11-2: Map to External Formats

Section: Format Mapping

Description: Maps payroll data to external provider formats.

Design Specification:
- Service maps fields to provider schema
- Configurable mapping

Sample Implementation:
```java
@Service
public class PayrollService {
    public Resource generateExport(LocalDate start, LocalDate end) {
        // Map attendance/leave to provider format
        // Generate CSV
        return csvResource;
    }
}
```

---

# USER STORY E11-3: Secure Delivery

Section: Secure Integration

Description: Delivers payroll files securely via SFTP/API.

Design Specification:
- SFTP/API integration
- Retry/backoff on failure

Sample Implementation:
```java
@Service
public class PayrollService {
    public void deliverExport(Resource file) {
        try {
            sftpClient.upload(file);
        } catch (Exception e) {
            // retry logic
        }
    }
}
```

---

# USER STORY E11-4: Audit Payroll Exports

Section: Export Auditing

Description: Audits payroll exports.

Design Specification:
- Audit log for each export
- Export status tracked

Sample Implementation:
```java
@Entity
public class PayrollExportAudit {
    @Id @GeneratedValue
    private Long id;
    private LocalDateTime timestamp;
    private String status; // SUCCESS, FAILED
    private String actor;
    // getters/setters
}

@Service
public class PayrollService {
    public void logExport(String status, String actor) {
        auditRepo.save(new PayrollExportAudit(LocalDateTime.now(), status, actor));
    }
}
```

---

## EPIC E12: Notifications & Announcements

---

# USER STORY E12-1: In-App and Email/SMS Notifications

Section: Notification Service

Description: Sends notifications via in-app, email, SMS.

Design Specification:
- Notification entity: userId, channel, message, status
- Service sends notifications

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
    // getters/setters
}

@Service
public class NotificationService {
    public void sendNotification(NotificationDTO dto) {
        // send via channel
    }
}
```

---

# USER STORY E12-2: Localized Templates

Section: Template Management

Description: Supports localized notification templates.

Design Specification:
- Templates stored per locale
- Service selects template based on user locale

Sample Implementation:
```java
@Service
public class NotificationService {
    public String getTemplate(String type, String locale) {
        // fetch template from DB or resource bundle
        return template;
    }
}
```

---

# USER STORY E12-3: Track Delivery Status

Section: Delivery Tracking

Description: Tracks notification delivery status.

Design Specification:
- Status updated on send
- Retry on failure

Sample Implementation:
```java
@Service
public class NotificationService {
    public void sendNotification(NotificationDTO dto) {
        try {
            // send logic
            notification.setStatus("SENT");
        } catch (Exception e) {
            notification.setStatus("FAILED");
            // retry logic
        }
        repo.save(notification);
    }
}
```

---

# USER STORY E12-4: Announcements on Dashboard

Section: Dashboard Announcements

Description: Displays announcements on dashboard.

Design Specification:
- Announcement entity: message, startDate, endDate
- GET `/dashboard/announcements`

Sample Implementation:
```java
@Entity
public class Announcement {
    @Id @GeneratedValue
    private Long id;
    private String message;
    private LocalDate startDate;
    private LocalDate endDate;
    // getters/setters
}

@GetMapping("/dashboard/announcements")
public ResponseEntity<List<AnnouncementDTO>> getAnnouncements() {
    return ResponseEntity.ok(announcementService.getActiveAnnouncements());
}
```

---

## EPIC E13: Integration Layer

---

# USER STORY E13-1: Expose REST APIs

Section: API Exposure

Description: Exposes REST APIs for integration.

Design Specification:
- JWT/OAuth2-secured endpoints
- OpenAPI documentation

Sample Implementation:
```java
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
public class IntegrationController {
    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeDTO>> getEmployees() { ... }
}
```

---

# USER STORY E13-2: HRIS Sync Job

Section: HRIS Integration

Description: Syncs employees from HRIS.

Design Specification:
- Scheduled job fetches HRIS data
- Creates/updates employees

Sample Implementation:
```java
@Service
public class HRISSyncService {
    @Scheduled(cron = "0 0 * * * ?")
    public void syncEmployees() {
        List<EmployeeDTO> hrisEmployees = hrisClient.fetchEmployees();
        employeeService.sync(hrisEmployees);
    }
}
```

---

# USER STORY E13-3: WMS Link

Section: WMS Integration

Description: Links to WMS for department/location sync.

Design Specification:
- REST client for WMS
- Sync job updates departments/locations

Sample Implementation:
```java
@Service
public class WMSIntegrationService {
    public void syncDepartments() {
        List<DepartmentDTO> wmsDepartments = wmsClient.fetchDepartments();
        departmentService.sync(wmsDepartments);
    }
}
```

---

# USER STORY E13-4: Webhooks for Events

Section: Webhook Management

Description: Sends webhooks for system events.

Design Specification:
- Webhook entity: url, eventType
- Service posts to webhook on event

Sample Implementation:
```java
@Service
public class WebhookService {
    public void sendEvent(String eventType, Object payload) {
        List<Webhook> webhooks = repo.findByEventType(eventType);
        for (Webhook webhook : webhooks) {
            restTemplate.postForEntity(webhook.getUrl(), payload, Void.class);
        }
    }
}
```

---

## EPIC E14: Audit Trail & Compliance

---

# USER STORY E14-1: Centralized Audit Logging

Section: Audit Logging

Description: Logs sensitive changes centrally.

Design Specification:
- AuditLog entity: actor, timestamp, action, before/after
- Service logs on create/update/delete

Sample Implementation:
```java
@Service
public class AuditLogService {
    public void logChange(String actor, String action, String entity, String before, String after) {
        auditRepo.save(new AuditLog(actor, LocalDateTime.now(), action, entity, before, after));
    }
}
```

---

# USER STORY E14-2: Immutable Audit Log

Section: Data Integrity

Description: Ensures audit log is immutable.

Design Specification:
- No update/delete on audit log table
- DB constraints

Sample Implementation:
```sql
-- Migration script
CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    actor VARCHAR(100),
    timestamp TIMESTAMP,
    action VARCHAR(50),
    entity VARCHAR(50),
    before_state TEXT,
    after_state TEXT
);
-- No update/delete allowed via application
```

---

# USER STORY E14-3: Export Audit Logs

Section: Audit Export

Description: Exports audit logs by date/user/entity.

Design Specification:
- Export endpoint `/audit/export`
- CSV generation

Sample Implementation:
```java
@GetMapping("/audit/export")
public ResponseEntity<Resource> exportAudit(@RequestParam String entity, @RequestParam LocalDate start, @RequestParam LocalDate end) {
    Resource csv = auditService.export(entity, start, end);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit.csv")
        .body(csv);
}
```

---

# USER STORY E14-4: Test Coverage for Auditing

Section: Test Coverage

Description: Ensures audit logging is covered by tests.

Design Specification:
- Unit/integration tests for audit logging
- Mock actor/context

Sample Implementation:
```java
@SpringBootTest
public class AuditLogServiceTest {
    @Autowired AuditLogService auditLogService;
    @Test
    public void testLogChange() {
        auditLogService.logChange("admin", "UPDATE", "employee", "before", "after");
        // assert audit log entry exists
    }
}
```

---

## EPIC E15: Reporting & Analytics

---

# USER STORY E15-1: Operational Reports

Section: Reporting Service

Description: Generates operational reports.

Design Specification:
- Reports: attendance, overtime, leave balances
- Filter by date, department, shift

Sample Implementation:
```java
@GetMapping("/reports/operational")
public ResponseEntity<OperationalReportDTO> getReport(@RequestParam LocalDate start, @RequestParam LocalDate end, @RequestParam String department) {
    return ResponseEntity.ok(reportService.generateOperationalReport(start, end, department));
}
```

---

# USER STORY E15-2: Certification and Safety KPIs

Section: KPI Reporting

Description: Provides certification and safety KPIs.

Design Specification:
- GET `/reports/kpi`
- Aggregated metrics

Sample Implementation:
```java
@GetMapping("/reports/kpi")
public ResponseEntity<KPIDTO> getKPIs() {
    return ResponseEntity.ok(reportService.getKPIs());
}
```

---

# USER STORY E15-3: Role-Based Dashboards

Section: Dashboard Service

Description: Provides dashboards based on user role.

Design Specification:
- Service filters dashboard data by role

Sample Implementation:
```java
@Service
public class DashboardService {
    public DashboardDTO getDashboard(Long userId, String role) {
        // filter data based on role
        return dashboardDTO;
    }
}
```

---

# USER STORY E15-4: Fast Export for Large Datasets

Section: Export Performance

Description: Supports fast export for large datasets.

Design Specification:
- Streaming CSV export
- Pagination for >50k rows

Sample Implementation:
```java
@GetMapping("/reports/export")
public void exportLargeDataset(HttpServletResponse response) {
    response.setContentType("text/csv");
    reportService.streamExport(response.getWriter());
}
```

---

## EPIC E16: Mobile Access PWA

---

# USER STORY E16-1: Responsive Mobile Views

Section: Mobile Controller

Description: Provides responsive views for mobile access.

Design Specification:
- Controller returns mobile-friendly DTOs
- UI adapts to device

Sample Implementation:
```java
@GetMapping("/mobile/schedule")
public ResponseEntity<MobileScheduleDTO> getMobileSchedule(@RequestParam Long employeeId) {
    return ResponseEntity.ok(mobileService.getSchedule(employeeId));
}
```

---

# USER STORY E16-2: Installable PWA Manifest

Section: PWA Configuration

Description: Supports installable PWA manifest.

Design Specification:
- `manifest.json` served at `/manifest.json`
- Icons, theme color, start URL

Sample Implementation:
```json
// manifest.json
{
  "name": "Warehouse Employee Management",
  "short_name": "WMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "icons": [
    { "src": "/icon-192.png", "sizes": "192x192", "type": "image/png" }
  ]
}
```

---

# USER STORY E16-3: Offline Queue

Section: Offline Support

Description: Queues clock events offline and syncs when online.

Design Specification:
- Service Worker caches requests
- Syncs queued events

Sample Implementation:
```javascript
// service-worker.js (pseudo-code)
self.addEventListener('fetch', event => {
    if (event.request.url.includes('/attendance/clock-in')) {
        // queue request if offline
    }
});
self.addEventListener('sync', event => {
    // send queued clock events
});
```

---

# USER STORY E16-4: Lighthouse PWA Score

Section: PWA Optimization

Description: Ensures Lighthouse PWA score â¥ 80.

Design Specification:
- Optimize manifest, service worker, caching
- Test with Lighthouse

Sample Implementation:
```json
// manifest.json and service-worker.js optimized for performance
// Run Lighthouse and address recommendations
```

---

## EPIC E17: Onboarding & Offboarding

---

# USER STORY E17-1: Automate New Hire Provisioning

Section: Onboarding Workflow

Description: Automates provisioning for new hires.

Design Specification:
- OnboardingTask entity: employeeId, taskType, status
- Tasks generated on new hire

Sample Implementation:
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String taskType;
    private String status; // PENDING, COMPLETED
    // getters/setters
}

@Service
public class OnboardingService {
    public void provisionNewHire(Long employeeId) {
        // generate tasks for training, asset assignment
    }
}
```

---

# USER STORY E17-2: Automate Offboarding

Section: Offboarding Workflow

Description: Automates offboarding for terminated employees.

Design Specification:
- OffboardingTask entity: employeeId, taskType, status
- Tasks generated on termination

Sample Implementation:
```java
@Entity
public class OffboardingTask {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String taskType;
    private String status; // PENDING, COMPLETED
    // getters/setters
}

@Service
public class OffboardingService {
    public void offboardEmployee(Long employeeId) {
        // revoke access, collect assets, update schedules
    }
}
```

---

# USER STORY E17-3: Track Tasks

Section: Task Tracking

Description: Tracks onboarding/offboarding tasks.

Design Specification:
- GET `/onboarding/tasks`, `/offboarding/tasks`
- Task status updates

Sample Implementation:
```java
@GetMapping("/onboarding/tasks")
public ResponseEntity<List<OnboardingTaskDTO>> getOnboardingTasks(@RequestParam Long employeeId) {
    return ResponseEntity.ok(onboardingService.getTasks(employeeId));
}
```

---

# USER STORY E17-4: Sync from HRIS

Section: HRIS Sync

Description: Syncs new hires/terms from HRIS.

Design Specification:
- Scheduled job fetches HRIS changes
- Updates onboarding/offboarding tasks

Sample Implementation:
```java
@Service
public class HRISSyncService {
    @Scheduled(cron = "0 0 * * * ?")
    public void syncLifecycleChanges() {
        List<EmployeeDTO> changes = hrisClient.fetchLifecycleChanges();
        onboardingService.handleNewHires(changes);
        offboardingService.handleTerms(changes);
    }
}
```

---

## EPIC E18: Document Management

---

# USER STORY E18-1: Upload and Store Documents

Section: Document Storage

Description: Supports document upload and storage.

Design Specification:
- Document entity: id, employeeId, url, type, expiryDate
- POST `/documents/upload`

Sample Implementation:
```java
@Entity
public class Document {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String url;
    private String type;
    private LocalDate expiryDate;
    // getters/setters
}

@RestController
@RequestMapping("/documents")
public class DocumentController {
    @PostMapping("/upload")
    public ResponseEntity<DocumentDTO> upload(@RequestParam MultipartFile file, @RequestParam Long employeeId) {
        return ResponseEntity.ok(documentService.upload(file, employeeId));
    }
}
```

---

# USER STORY E18-2: Secure Access Control

Section: Document Security

Description: Secures document access.

Design Specification:
- Access controlled via roles
- Service checks permissions

Sample Implementation:
```java
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
@GetMapping("/{id}")
public ResponseEntity<DocumentDTO> getDocument(@PathVariable Long id) {
    return ResponseEntity.ok(documentService.getDocument(id));
}
```

---

# USER STORY E18-3: Document Expiry and Retention

Section: Retention Policy

Description: Manages document expiry and retention.

Design Specification:
- Scheduled job flags expired documents
- Retention policy enforced

Sample Implementation:
```java
@Service
public class DocumentService {
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkExpiry() {
        List<Document> expired = repo.findExpiredDocuments();
        // flag or delete as per policy
    }
}
```

---

# USER STORY E18-4: Document Audit Logging

Section: Document Auditing

Description: Logs document actions.

Design Specification:
- Audit log for uploads, downloads, deletions

Sample Implementation:
```java
@Service
public class DocumentService {
    public void upload(MultipartFile file, Long employeeId) {
        // ... upload logic
        auditLogService.logChange(actor, "UPLOAD", "document", null, file.getOriginalFilename());
    }
}
```

---

## EPIC E19: Multi-Site & Multi-Tenant

---

# USER STORY E19-1: Support Multiple Sites

Section: Site Domain

Description: Supports multiple warehouse sites.

Design Specification:
- Site entity: id, name, location
- Employee assigned to site

Sample Implementation:
```java
@Entity
public class Site {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String location;
    // getters/setters
}

public class Employee {
    // ...
    @ManyToOne
    private Site site;
}
```

---

# USER STORY E19-2: Tenant Isolation

Section: Tenant Isolation

Description: Ensures tenant data isolation.

Design Specification:
- Tenant entity: id, name
- Data filtered by tenantId

Sample Implementation:
```java
@Entity
public class Tenant {
    @Id @GeneratedValue
    private Long id;
    private String name;
    // getters/setters
}

public class Employee {
    // ...
    @ManyToOne
    private Tenant tenant;
}
```

---

# USER STORY E19-3: Tenant-Specific Branding

Section: Branding Configuration

Description: Supports tenant-specific branding.

Design Specification:
- Branding config per tenant
- UI adapts branding

Sample Implementation:
```java
@Entity
public class BrandingConfig {
    @Id @GeneratedValue
    private Long id;
    private Long tenantId;
    private String logoUrl;
    private String themeColor;
    // getters/setters
}

@Service
public class BrandingService {
    public BrandingConfig getBranding(Long tenantId) {
        return repo.findByTenantId(tenantId);
    }
}
```

---

# USER STORY E19-4: Cross-Site Reporting

Section: Cross-Site Reports

Description: Provides cross-site reporting.

Design Specification:
- Reports aggregate data across sites
- Filter by site

Sample Implementation:
```java
@GetMapping("/reports/cross-site")
public ResponseEntity<CrossSiteReportDTO> getCrossSiteReport(@RequestParam List<Long> siteIds) {
    return ResponseEntity.ok(reportService.generateCrossSiteReport(siteIds));
}
```

---

## EPIC E20: API Documentation & Developer Portal

---

# USER STORY E20-1: Auto-Generate OpenAPI Docs

Section: OpenAPI Generation

Description: Auto-generates OpenAPI docs.

Design Specification:
- `springdoc-openapi` dependency
- `/swagger-ui.html` endpoint

Sample Implementation:
```java
// pom.xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.0.2</version>
</dependency>
```

---

# USER STORY E20-2: Developer Portal with API Keys

Section: Developer Portal

Description: Provides developer portal for API key management.

Design Specification:
- Portal UI for API key generation
- API key entity: userId, key, status

Sample Implementation:
```java
@Entity
public class ApiKey {
    @Id @GeneratedValue
    private Long id;
    private Long userId;
    private String key;
    private String status; // ACTIVE, REVOKED
    // getters/setters
}

@RestController
@RequestMapping("/developer/api-keys")
public class ApiKeyController {
    @PostMapping
    public ResponseEntity<ApiKeyDTO> generate(@RequestParam Long userId) {
        return ResponseEntity.ok(apiKeyService.generate(userId));
    }
}
```

---

# USER STORY E20-3: API Usage Analytics

Section: Analytics Service

Description: Tracks API usage analytics.

Design Specification:
- APIUsage entity: userId, endpoint, timestamp, status
- Analytics endpoints

Sample Implementation:
```java
@Entity
public class APIUsage {
    @Id @GeneratedValue
    private Long id;
    private Long userId;
    private String endpoint;
    private LocalDateTime timestamp;
    private String status; // SUCCESS, ERROR
    // getters/setters
}

@GetMapping("/developer/api-usage")
public ResponseEntity<List<APIUsageDTO>> getUsage(@RequestParam Long userId) {
    return ResponseEntity.ok(analyticsService.getUsage(userId));
}
```

---

# USER STORY E20-4: API Versioning

Section: API Versioning

Description: Supports API versioning.

Design Specification:
- Versioned endpoints: `/v1/`, `/v2/`
- Controllers per version

Sample Implementation:
```java
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeV1Controller { ... }

@RestController
@RequestMapping("/api/v2/employees")
public class EmployeeV2Controller { ... }
```

---

> **End of Technical Design Document**