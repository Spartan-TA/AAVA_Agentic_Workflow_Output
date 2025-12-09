# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

---

## USER STORY 1: Initialize Spring Boot Project

Section: Project Initialization & Architecture Overview  
Description: Establishes the foundational Spring Boot project using Maven, configures base packages, and sets up core modules (employee, scheduling, attendance, safety). Enables Actuator for health monitoring and Flyway/Liquibase for database migrations.  
Design Specification:
- Maven multi-module structure: `employee`, `scheduling`, `attendance`, `safety`
- Base package: `com.warehouse.ems`
- Spring Boot Actuator enabled
- Flyway/Liquibase configured for DB migrations
- Application runs on port 8080
Sample Implementation:
```xml
<!-- pom.xml (modules) -->
<modules>
  <module>employee</module>
  <module>scheduling</module>
  <module>attendance</module>
  <module>safety</module>
</modules>
```
```yaml
# application.yml
server:
  port: 8080
spring:
  application:
    name: warehouse-ems
  flyway:
    enabled: true
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

---

## USER STORY 2: Set Up Core Modules and Database Migration

Section: Core Modules & DB Migration  
Description: Configures core modules and enables database versioning for consistent schema management.  
Design Specification:
- Package structure: `com.warehouse.ems.employee`, `...scheduling`, `...attendance`, `...safety`
- Flyway/Liquibase migration scripts in `src/main/resources/db/migration`
- Baseline migration for all core tables
Sample Implementation:
```sql
-- V1__init.sql
CREATE TABLE employee (
  id BIGSERIAL PRIMARY KEY,
  badge_id VARCHAR(32) UNIQUE NOT NULL,
  name VARCHAR(128) NOT NULL,
  role VARCHAR(32) NOT NULL,
  department VARCHAR(64),
  shift_group VARCHAR(32),
  hire_date DATE,
  status VARCHAR(16)
);
```

---

## USER STORY 3: Enable Actuator and Health Monitoring

Section: Actuator & Health Endpoints  
Description: Enables Spring Boot Actuator endpoints for health, metrics, and info monitoring.  
Design Specification:
- `management.endpoints.web.exposure.include=health,info,metrics`
- Custom health indicators for DB, external services
Sample Implementation:
```java
// HealthIndicator example
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

## USER STORY 4: Create Employee Record via API

Section: Employee Entity & Create API  
Description: REST API to create employee records with badgeId, name, role, department, shiftGroup, hireDate, status.  
Design Specification:
- Entity: `Employee`
- Repository: `EmployeeRepository extends JpaRepository`
- Service: `EmployeeService`
- Controller: `EmployeeController`
- Endpoint: `POST /employees`
Sample Implementation:
```java
@Entity
public class Employee {
  @Id @GeneratedValue private Long id;
  @Column(unique=true) private String badgeId;
  private String name, role, department, shiftGroup;
  private LocalDate hireDate;
  private String status;
}
```
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
  @PostMapping
  public ResponseEntity<EmployeeDto> create(@RequestBody EmployeeDto dto) { ... }
}
```

---

## USER STORY 5: Update and Delete Employee Records

Section: Employee Update & Soft Delete  
Description: Supports updating and soft-deleting employee records, with filtering and pagination.  
Design Specification:
- Update: `PUT /employees/{id}`
- Soft delete: `DELETE /employees/{id}` (sets status to 'INACTIVE')
- Filtering: by role, department, status
- Pagination: Spring Data Pageable
Sample Implementation:
```java
@PatchMapping("/{id}")
public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @RequestBody EmployeeDto dto) { ... }

@DeleteMapping("/{id}")
public ResponseEntity<Void> softDelete(@PathVariable Long id) {
  employeeService.softDelete(id);
  return ResponseEntity.noContent().build();
}
```

---

## USER STORY 6: Retrieve Employee Records and API Schema

Section: Employee Retrieval & OpenAPI  
Description: GET employees with filtering, pagination, and OpenAPI documentation.  
Design Specification:
- Endpoint: `GET /employees`
- Query params: `role`, `department`, `status`, `page`, `size`
- OpenAPI/Swagger annotations
Sample Implementation:
```java
@GetMapping
@Operation(summary = "Get employees", description = "Retrieve employees with filters and pagination")
public Page<EmployeeDto> getEmployees(@RequestParam Map<String,String> params, Pageable pageable) { ... }
```

---

## USER STORY 7: Implement Role-Based Access Control (RBAC)

Section: Security & RBAC  
Description: Enforces RBAC for all endpoints with roles: ADMIN, HR, SUPERVISOR, WORKER.  
Design Specification:
- Spring Security config with roles
- Method-level security: `@PreAuthorize`
- API key/OAuth2 toggle via config
Sample Implementation:
```java
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
      .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR")
      .antMatchers("/attendance/**").hasAnyRole("SUPERVISOR", "WORKER")
      .anyRequest().authenticated();
  }
}
```
```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteEmployee(Long id) { ... }
```

---

## USER STORY 8: Employee Clock In

Section: Attendance Clock-In  
Description: Records clock-in time with shift validation.  
Design Specification:
- Entity: `AttendanceEvent`
- Endpoint: `POST /attendance/clock-in`
- Validates shift assignment
Sample Implementation:
```java
@Entity
public class AttendanceEvent {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private LocalDateTime clockInTime;
  private String shiftId;
}
```
```java
@PostMapping("/clock-in")
public ResponseEntity<?> clockIn(@RequestBody ClockInDto dto) { ... }
```

---

## USER STORY 9: Employee Clock Out

Section: Attendance Clock-Out  
Description: Records clock-out time with validation.  
Design Specification:
- Endpoint: `POST /attendance/clock-out`
- Validates clock-in exists for the shift
Sample Implementation:
```java
@PostMapping("/clock-out")
public ResponseEntity<?> clockOut(@RequestBody ClockOutDto dto) { ... }
```

---

## USER STORY 10: Supervisor Attendance Review

Section: Attendance Dashboard  
Description: Supervisor dashboard to review employee attendance with filtering.  
Design Specification:
- Endpoint: `GET /attendance/review`
- Filters: employee, date, shift
- Role-based access: SUPERVISOR
Sample Implementation:
```java
@GetMapping("/review")
@PreAuthorize("hasRole('SUPERVISOR')")
public Page<AttendanceEventDto> reviewAttendance(@RequestParam Map<String,String> params, Pageable pageable) { ... }
```

---

## USER STORY 11: Create Shift Schedule

Section: Shift Scheduling  
Description: Create shift schedules with overlap validation.  
Design Specification:
- Entity: `ShiftSchedule`
- Endpoint: `POST /shifts`
- Validates no overlap for employee
Sample Implementation:
```java
@Entity
public class ShiftSchedule {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private LocalDateTime start, end;
  private String shiftType;
}
```
```java
@PostMapping("/shifts")
public ResponseEntity<ShiftScheduleDto> createShift(@RequestBody ShiftScheduleDto dto) { ... }
```

---

## USER STORY 12: Edit Shift Schedule

Section: Shift Edit & Notification  
Description: Edit existing shifts with notifications to affected employees.  
Design Specification:
- Endpoint: `PUT /shifts/{id}`
- Notification service integration
Sample Implementation:
```java
@PutMapping("/shifts/{id}")
public ResponseEntity<ShiftScheduleDto> editShift(@PathVariable Long id, @RequestBody ShiftScheduleDto dto) {
  shiftService.editShift(id, dto);
  notificationService.notifyShiftChange(dto.getEmployeeId());
  return ResponseEntity.ok(dto);
}
```

---

## USER STORY 13: Employee Shift View

Section: Shift Calendar View  
Description: Employees view assigned shifts in calendar format.  
Design Specification:
- Endpoint: `GET /shifts/my`
- Returns calendar data for logged-in employee
Sample Implementation:
```java
@GetMapping("/shifts/my")
@PreAuthorize("hasRole('WORKER')")
public List<ShiftScheduleDto> getMyShifts(Authentication auth) { ... }
```

---

## USER STORY 14: Submit Leave Request

Section: Leave Request Submission  
Description: Employees submit leave requests with balance validation.  
Design Specification:
- Entity: `LeaveRequest`
- Endpoint: `POST /leave`
- Validates leave balance
Sample Implementation:
```java
@Entity
public class LeaveRequest {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private LocalDate start, end;
  private String type, status;
}
```
```java
@PostMapping("/leave")
public ResponseEntity<LeaveRequestDto> submitLeave(@RequestBody LeaveRequestDto dto) { ... }
```

---

## USER STORY 15: Approve/Reject Leave Request

Section: Leave Approval Workflow  
Description: Supervisor approval workflow with notifications.  
Design Specification:
- Endpoint: `PUT /leave/{id}/approve`, `PUT /leave/{id}/reject`
- Notification service integration
Sample Implementation:
```java
@PutMapping("/leave/{id}/approve")
@PreAuthorize("hasRole('SUPERVISOR')")
public ResponseEntity<?> approveLeave(@PathVariable Long id) {
  leaveService.approve(id);
  notificationService.notifyLeaveStatus(id, "APPROVED");
  return ResponseEntity.ok().build();
}
```

---

## USER STORY 16: View Leave Balance

Section: Leave Balance Display  
Description: Displays current leave balance for employee.  
Design Specification:
- Endpoint: `GET /leave/balance`
Sample Implementation:
```java
@GetMapping("/leave/balance")
@PreAuthorize("hasRole('WORKER')")
public LeaveBalanceDto getBalance(Authentication auth) { ... }
```

---

## USER STORY 17: Record Training Completion

Section: Training Completion  
Description: Records employee training with duplicate prevention.  
Design Specification:
- Entity: `TrainingRecord`
- Endpoint: `POST /training/complete`
- Checks for duplicate
Sample Implementation:
```java
@Entity
public class TrainingRecord {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String trainingType;
  private LocalDate completionDate;
}
```
```java
@PostMapping("/training/complete")
public ResponseEntity<?> recordTraining(@RequestBody TrainingRecordDto dto) { ... }
```

---

## USER STORY 18: Certification Expiry Notification

Section: Certification Expiry Alerts  
Description: Notifies employees of expiring certifications.  
Design Specification:
- Scheduled job checks for expiring certifications
- Notification service integration
Sample Implementation:
```java
@Scheduled(cron = "0 0 8 * * ?")
public void notifyExpiringCerts() {
  List<Certification> expiring = certService.findExpiringWithinDays(30);
  expiring.forEach(cert -> notificationService.notifyCertificationExpiry(cert.getEmployeeId(), cert.getExpiryDate()));
}
```

---

## USER STORY 19: Supervisor Training Dashboard

Section: Training Dashboard  
Description: Supervisors view team training status.  
Design Specification:
- Endpoint: `GET /training/dashboard`
- Filters by supervisor/team
Sample Implementation:
```java
@GetMapping("/training/dashboard")
@PreAuthorize("hasRole('SUPERVISOR')")
public List<TrainingStatusDto> getTeamTrainingStatus(Authentication auth) { ... }
```

---

## USER STORY 20: Report Safety Incident

Section: Safety Incident Reporting  
Description: Submit safety incident reports.  
Design Specification:
- Entity: `SafetyIncident`
- Endpoint: `POST /safety/incidents`
Sample Implementation:
```java
@Entity
public class SafetyIncident {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee reportedBy;
  private String description, severity, location;
  private LocalDateTime reportedAt;
}
```
```java
@PostMapping("/safety/incidents")
public ResponseEntity<SafetyIncidentDto> reportIncident(@RequestBody SafetyIncidentDto dto) { ... }
```

---

## USER STORY 21: OSHA Compliance Export

Section: OSHA Export  
Description: Export incident data in OSHA format.  
Design Specification:
- Endpoint: `GET /safety/incidents/export?format=osha`
- Generates OSHA 300/300A CSV
Sample Implementation:
```java
@GetMapping("/safety/incidents/export")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Resource> exportOsha(@RequestParam String format) { ... }
```

---

## USER STORY 22: Incident Investigation Workflow

Section: Incident Investigation  
Description: Manage incident investigations.  
Design Specification:
- Entity: `IncidentInvestigation`
- Endpoint: `POST /safety/incidents/{id}/investigate`
- Workflow: Open â Investigating â Resolved
Sample Implementation:
```java
@Entity
public class IncidentInvestigation {
  @Id @GeneratedValue private Long id;
  @ManyToOne private SafetyIncident incident;
  private String status;
  private String findings;
  private LocalDateTime startedAt, resolvedAt;
}
```
```java
@PostMapping("/safety/incidents/{id}/investigate")
public ResponseEntity<?> startInvestigation(@PathVariable Long id, @RequestBody InvestigationDto dto) { ... }
```

---

## USER STORY 23: Assign Equipment to Employee

Section: Equipment Assignment  
Description: Track equipment assignments to employees.  
Design Specification:
- Entity: `EquipmentAssignment`
- Endpoint: `POST /equipment/assign`
Sample Implementation:
```java
@Entity
public class EquipmentAssignment {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  @ManyToOne private Equipment equipment;
  private LocalDateTime assignedAt;
  private String condition;
}
```
```java
@PostMapping("/equipment/assign")
public ResponseEntity<EquipmentAssignmentDto> assign(@RequestBody EquipmentAssignmentDto dto) { ... }
```

---

## USER STORY 24: Return Equipment

Section: Equipment Return  
Description: Process equipment returns.  
Design Specification:
- Endpoint: `POST /equipment/return`
- Updates assignment status and equipment condition
Sample Implementation:
```java
@PostMapping("/equipment/return")
public ResponseEntity<?> returnEquipment(@RequestBody EquipmentReturnDto dto) { ... }
```

---

# General Configuration & Integration Points

Section: Configuration & Security  
Description: Centralized configuration for DB, security, notifications, and external integrations.  
Design Specification:
- `application.yml` for environment-specific settings
- Spring Security for RBAC
- Notification service (email/SMS)
- Integration with HRIS, WMS, SSO via REST connectors
Sample Implementation:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ems
    username: ems_user
    password: secret
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://idp.example.com
notification:
  email:
    enabled: true
  sms:
    enabled: true
```

---

# Package Structure Example

```
com.warehouse.ems
  âââ employee
  â     âââ Employee.java
  â     âââ EmployeeRepository.java
  â     âââ EmployeeService.java
  â     âââ EmployeeController.java
  âââ attendance
  â     âââ AttendanceEvent.java
  â     âââ AttendanceRepository.java
  â     âââ AttendanceService.java
  â     âââ AttendanceController.java
  âââ scheduling
  â     âââ ShiftSchedule.java
  â     âââ ShiftRepository.java
  â     âââ ShiftService.java
  â     âââ ShiftController.java
  âââ safety
  â     âââ SafetyIncident.java
  â     âââ SafetyIncidentRepository.java
  â     âââ SafetyIncidentService.java
  â     âââ SafetyIncidentController.java
  âââ config
        âââ SecurityConfig.java
        âââ NotificationConfig.java
        âââ IntegrationConfig.java
```

---

# Notes

- All endpoints are documented with OpenAPI annotations.
- Entities use JPA annotations and relationships.
- Services encapsulate business logic and validation.
- Repositories use Spring Data JPA.
- Controllers expose REST endpoints with RBAC enforced.
- Notification and integration points are abstracted for extensibility.
- All modules follow Spring Boot best practices for maintainability and scalability.

---

**End of Technical Design Document**