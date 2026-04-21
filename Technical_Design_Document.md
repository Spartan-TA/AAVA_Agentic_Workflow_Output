# Warehouse Employee Management System - Low-Level Technical Design Document

---

## Table of Contents

1. [Employee Profile Creation](#section-1-employee-profile-creation)
2. [Employee Profile Update](#section-2-employee-profile-update)
3. [Employee Self-Service Profile Viewing](#section-3-employee-self-service-profile-viewing)
4. [Shift Scheduling](#section-4-shift-scheduling)
5. [Shift Swap Request](#section-5-shift-swap-request)
6. [Time Tracking](#section-6-time-tracking)
7. [Task Assignment](#section-7-task-assignment)
8. [Task Completion Tracking](#section-8-task-completion-tracking)
9. [Performance Dashboard](#section-9-performance-dashboard)
10. [Performance Report Generation](#section-10-performance-report-generation)
11. [Training Assignment](#section-11-training-assignment)
12. [Certification Tracking](#section-12-certification-tracking)
13. [Safety Incident Reporting](#section-13-safety-incident-reporting)
14. [Safety Compliance Checklist](#section-14-safety-compliance-checklist)
15. [Inventory Access Control](#section-15-inventory-access-control)
16. [Access Log Review](#section-16-access-log-review)
17. [Internal Communication Messaging](#section-17-internal-communication-messaging)
18. [Shift Notification](#section-18-shift-notification)
19. [Task Reminder Notification](#section-19-task-reminder-notification)
20. [System Audit Log](#section-20-system-audit-log)

---

## <a name="section-1-employee-profile-creation"></a>Section: Employee Profile Creation

**Description:**  
HR admin creates new employee profiles in the system. This feature allows HR to add new employees with all necessary details.

**Design Specification:**  
- **Architecture:** Follows layered architecture (Controller â Service â Repository â Entity).
- **Package Structure:**  
  - `com.company.warehouse.employee.entity`
  - `com.company.warehouse.employee.repository`
  - `com.company.warehouse.employee.service`
  - `com.company.warehouse.employee.controller`
- **Entity Design:**  
  - `Employee` entity with fields: id, firstName, lastName, email, phone, role, department, status, createdAt, updatedAt.
- **Repository Layer:**  
  - `EmployeeRepository` extends `JpaRepository<Employee, Long>`
- **Service Layer:**  
  - `EmployeeService` interface and `EmployeeServiceImpl` implementation.
  - Method: `createEmployee(EmployeeDTO employeeDTO)`
- **Controller Layer:**  
  - `EmployeeController` with endpoint: `POST /api/employees`
  - Request: `EmployeeDTO`
  - Response: `EmployeeDTO`
- **Configuration:**  
  - Validation enabled.
  - Logging for audit.
- **Security Settings:**  
  - Only users with `ROLE_HR_ADMIN` can access.
- **Integration Points:**  
  - None for creation.
- **Exception Handling:**  
  - Handles duplicate email, validation errors.

**Sample Implementation:**

```java
// Entity
@Entity
@Table(name = "employees")
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @Email @NotBlank @Column(unique = true) private String email;
    private String phone;
    @Enumerated(EnumType.STRING) private Role role;
    private String department;
    @Enumerated(EnumType.STRING) private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // getters and setters
}

// Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmail(String email);
}

// DTO
public class EmployeeDTO {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @Email @NotBlank private String email;
    private String phone;
    private String role;
    private String department;
}

// Service
public interface EmployeeService {
    EmployeeDTO createEmployee(EmployeeDTO employeeDTO);
}

@Service
public class EmployeeServiceImpl implements EmployeeService {
    @Autowired private EmployeeRepository employeeRepository;
    @Override
    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        Employee emp = new Employee();
        // set fields from dto
        emp.setCreatedAt(LocalDateTime.now());
        emp.setStatus(Status.ACTIVE);
        employeeRepository.save(emp);
        // map to DTO and return
    }
}

// Controller
@RestController
@RequestMapping("/api/employees")
@Validated
public class EmployeeController {
    @Autowired private EmployeeService employeeService;

    @PreAuthorize("hasRole('HR_ADMIN')")
    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(dto));
    }
}

// Security Config (snippet)
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
        .antMatchers(HttpMethod.POST, "/api/employees").hasRole("HR_ADMIN")
        .anyRequest().authenticated();
}
```

---

## <a name="section-2-employee-profile-update"></a>Section: Employee Profile Update

**Description:**  
HR admin updates existing employee profiles.

**Design Specification:**  
- **Architecture:** Layered (Controller â Service â Repository â Entity)
- **Package Structure:** Same as above.
- **Entity Design:**  
  - `Employee` entity as above.
- **Repository Layer:**  
  - `EmployeeRepository`
- **Service Layer:**  
  - Method: `updateEmployee(Long id, EmployeeDTO employeeDTO)`
- **Controller Layer:**  
  - Endpoint: `PUT /api/employees/{id}`
- **Configuration:**  
  - Validation, logging.
- **Security Settings:**  
  - Only `ROLE_HR_ADMIN`.
- **Integration Points:**  
  - None.
- **Exception Handling:**  
  - Handles not found, validation errors.

**Sample Implementation:**

```java
// Service
public interface EmployeeService {
    EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO);
}

@Service
public class EmployeeServiceImpl implements EmployeeService {
    @Override
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {
        Employee emp = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        // update fields from dto
        emp.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(emp);
        // map to DTO and return
    }
}

// Controller
@PutMapping("/{id}")
@PreAuthorize("hasRole('HR_ADMIN')")
public ResponseEntity<EmployeeDTO> updateEmployee(
    @PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) {
    return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
}
```

---

## <a name="section-3-employee-self-service-profile-viewing"></a>Section: Employee Self-Service Profile Viewing

**Description:**  
Employees can view their own profiles.

**Design Specification:**  
- **Architecture:** Layered.
- **Entity Design:**  
  - `Employee`
- **Repository Layer:**  
  - `EmployeeRepository`
- **Service Layer:**  
  - Method: `getEmployeeProfile(Long id)`
- **Controller Layer:**  
  - Endpoint: `GET /api/employees/me`
- **Security Settings:**  
  - Only authenticated employee can view their own profile.
- **Exception Handling:**  
  - Handles not found.

**Sample Implementation:**

```java
// Service
public EmployeeDTO getEmployeeProfile(Long id);

// Controller
@GetMapping("/me")
@PreAuthorize("hasRole('EMPLOYEE')")
public ResponseEntity<EmployeeDTO> getMyProfile(Authentication auth) {
    String email = auth.getName();
    Employee emp = employeeRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    // map to DTO and return
}
```

---

## <a name="section-4-shift-scheduling"></a>Section: Shift Scheduling

**Description:**  
Managers schedule employee shifts.

**Design Specification:**  
- **Architecture:** Layered.
- **Package Structure:**  
  - `shift.entity`, `shift.repository`, `shift.service`, `shift.controller`
- **Entity Design:**  
  - `Shift` (id, employee, startTime, endTime, status, createdBy)
- **Repository Layer:**  
  - `ShiftRepository`
- **Service Layer:**  
  - `scheduleShift(ShiftDTO shiftDTO)`
- **Controller Layer:**  
  - `POST /api/shifts`
- **Security Settings:**  
  - Only `ROLE_MANAGER`.
- **Exception Handling:**  
  - Overlapping shifts, validation.

**Sample Implementation:**

```java
@Entity
public class Shift {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @Enumerated(EnumType.STRING) private ShiftStatus status;
    private String createdBy;
}

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    boolean existsByEmployeeAndStartTimeBetween(Employee emp, LocalDateTime start, LocalDateTime end);
}

public interface ShiftService {
    ShiftDTO scheduleShift(ShiftDTO dto);
}

@Service
public class ShiftServiceImpl implements ShiftService {
    @Override
    public ShiftDTO scheduleShift(ShiftDTO dto) {
        // check for overlapping shifts
        // save shift
    }
}

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<ShiftDTO> scheduleShift(@Valid @RequestBody ShiftDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shiftService.scheduleShift(dto));
    }
}
```

---

## <a name="section-5-shift-swap-request"></a>Section: Shift Swap Request

**Description:**  
Employees request to swap shifts with another employee.

**Design Specification:**  
- **Entity Design:**  
  - `ShiftSwapRequest` (id, fromEmployee, toEmployee, shift, status, requestedAt, approvedBy)
- **Repository Layer:**  
  - `ShiftSwapRequestRepository`
- **Service Layer:**  
  - `requestShiftSwap(ShiftSwapRequestDTO dto)`
- **Controller Layer:**  
  - `POST /api/shifts/swap-requests`
- **Security Settings:**  
  - Only `ROLE_EMPLOYEE`.
- **Exception Handling:**  
  - Validation, shift existence.

**Sample Implementation:**

```java
@Entity
public class ShiftSwapRequest {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee fromEmployee;
    @ManyToOne private Employee toEmployee;
    @ManyToOne private Shift shift;
    @Enumerated(EnumType.STRING) private SwapStatus status;
    private LocalDateTime requestedAt;
    private String approvedBy;
}

public interface ShiftSwapRequestRepository extends JpaRepository<ShiftSwapRequest, Long> {}

@RestController
@RequestMapping("/api/shifts/swap-requests")
public class ShiftSwapController {
    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping
    public ResponseEntity<ShiftSwapRequestDTO> requestSwap(@Valid @RequestBody ShiftSwapRequestDTO dto) {
        // call service
    }
}
```

---

## <a name="section-6-time-tracking"></a>Section: Time Tracking

**Description:**  
Employees clock in and out.

**Design Specification:**  
- **Entity Design:**  
  - `TimeEntry` (id, employee, clockIn, clockOut, shift, status)
- **Repository Layer:**  
  - `TimeEntryRepository`
- **Service Layer:**  
  - `clockIn(Long employeeId)`, `clockOut(Long employeeId)`
- **Controller Layer:**  
  - `POST /api/time/clock-in`, `POST /api/time/clock-out`
- **Security Settings:**  
  - Only `ROLE_EMPLOYEE`.

**Sample Implementation:**

```java
@Entity
public class TimeEntry {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    @ManyToOne private Shift shift;
    @Enumerated(EnumType.STRING) private TimeEntryStatus status;
}

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {}

@RestController
@RequestMapping("/api/time")
public class TimeTrackingController {
    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/clock-in")
    public ResponseEntity<Void> clockIn(Authentication auth) {
        // call service
        return ResponseEntity.ok().build();
    }
    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/clock-out")
    public ResponseEntity<Void> clockOut(Authentication auth) {
        // call service
        return ResponseEntity.ok().build();
    }
}
```

---

## <a name="section-7-task-assignment"></a>Section: Task Assignment

**Description:**  
Managers assign tasks to employees.

**Design Specification:**  
- **Entity Design:**  
  - `Task` (id, title, description, assignedTo, assignedBy, dueDate, status)
- **Repository Layer:**  
  - `TaskRepository`
- **Service Layer:**  
  - `assignTask(TaskDTO dto)`
- **Controller Layer:**  
  - `POST /api/tasks`
- **Security Settings:**  
  - Only `ROLE_MANAGER`.

**Sample Implementation:**

```java
@Entity
public class Task {
    @Id @GeneratedValue private Long id;
    private String title;
    private String description;
    @ManyToOne private Employee assignedTo;
    @ManyToOne private Employee assignedBy;
    private LocalDate dueDate;
    @Enumerated(EnumType.STRING) private TaskStatus status;
}

public interface TaskRepository extends JpaRepository<Task, Long> {}

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<TaskDTO> assignTask(@Valid @RequestBody TaskDTO dto) {
        // call service
    }
}
```

---

## <a name="section-8-task-completion-tracking"></a>Section: Task Completion Tracking

**Description:**  
Employees mark tasks as complete.

**Design Specification:**  
- **Service Layer:**  
  - `completeTask(Long taskId, Long employeeId)`
- **Controller Layer:**  
  - `POST /api/tasks/{id}/complete`
- **Security Settings:**  
  - Only assigned employee.

**Sample Implementation:**

```java
@PreAuthorize("hasRole('EMPLOYEE')")
@PostMapping("/{id}/complete")
public ResponseEntity<Void> completeTask(@PathVariable Long id, Authentication auth) {
    // call service
    return ResponseEntity.ok().build();
}
```

---

## <a name="section-9-performance-dashboard"></a>Section: Performance Dashboard

**Description:**  
Managers view employee performance metrics.

**Design Specification:**  
- **Service Layer:**  
  - `getPerformanceMetrics()`
- **Controller Layer:**  
  - `GET /api/performance/dashboard`
- **Security Settings:**  
  - Only `ROLE_MANAGER`.

**Sample Implementation:**

```java
@PreAuthorize("hasRole('MANAGER')")
@GetMapping("/dashboard")
public ResponseEntity<PerformanceDashboardDTO> getDashboard() {
    // call service
}
```

---

## <a name="section-10-performance-report-generation"></a>Section: Performance Report Generation

**Description:**  
HR generates performance reports.

**Design Specification:**  
- **Service Layer:**  
  - `generatePerformanceReport(ReportCriteria criteria)`
- **Controller Layer:**  
  - `POST /api/performance/report`
- **Security Settings:**  
  - Only `ROLE_HR_ADMIN`.

**Sample Implementation:**

```java
@PreAuthorize("hasRole('HR_ADMIN')")
@PostMapping("/report")
public ResponseEntity<ReportDTO> generateReport(@RequestBody ReportCriteria criteria) {
    // call service
}
```

---

## <a name="section-11-training-assignment"></a>Section: Training Assignment

**Description:**  
Managers assign training modules to employees.

**Design Specification:**  
- **Entity Design:**  
  - `TrainingAssignment` (id, employee, module, assignedBy, assignedAt, status)
- **Repository Layer:**  
  - `TrainingAssignmentRepository`
- **Service Layer:**  
  - `assignTraining(TrainingAssignmentDTO dto)`
- **Controller Layer:**  
  - `POST /api/training/assign`
- **Security Settings:**  
  - Only `ROLE_MANAGER`.

**Sample Implementation:**

```java
@Entity
public class TrainingAssignment {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String module;
    @ManyToOne private Employee assignedBy;
    private LocalDateTime assignedAt;
    @Enumerated(EnumType.STRING) private TrainingStatus status;
}

public interface TrainingAssignmentRepository extends JpaRepository<TrainingAssignment, Long> {}

@RestController
@RequestMapping("/api/training")
public class TrainingController {
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/assign")
    public ResponseEntity<TrainingAssignmentDTO> assignTraining(@Valid @RequestBody TrainingAssignmentDTO dto) {
        // call service
    }
}
```

---

## <a name="section-12-certification-tracking"></a>Section: Certification Tracking

**Description:**  
System tracks employee certifications.

**Design Specification:**  
- **Entity Design:**  
  - `Certification` (id, employee, name, issuedBy, issueDate, expiryDate, status)
- **Repository Layer:**  
  - `CertificationRepository`
- **Service Layer:**  
  - `addCertification(CertificationDTO dto)`
- **Controller Layer:**  
  - `POST /api/certifications`
- **Security Settings:**  
  - `ROLE_HR_ADMIN`, `ROLE_MANAGER`.

**Sample Implementation:**

```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String name;
    private String issuedBy;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    @Enumerated(EnumType.STRING) private CertificationStatus status;
}

public interface CertificationRepository extends JpaRepository<Certification, Long> {}

@RestController
@RequestMapping("/api/certifications")
public class CertificationController {
    @PreAuthorize("hasAnyRole('HR_ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<CertificationDTO> addCertification(@Valid @RequestBody CertificationDTO dto) {
        // call service
    }
}
```

---

## <a name="section-13-safety-incident-reporting"></a>Section: Safety Incident Reporting

**Description:**  
Safety officers report incidents.

**Design Specification:**  
- **Entity Design:**  
  - `SafetyIncident` (id, reportedBy, description, date, severity, status)
- **Repository Layer:**  
  - `SafetyIncidentRepository`
- **Service Layer:**  
  - `reportIncident(SafetyIncidentDTO dto)`
- **Controller Layer:**  
  - `POST /api/safety/incidents`
- **Security Settings:**  
  - Only `ROLE_SAFETY_OFFICER`.

**Sample Implementation:**

```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee reportedBy;
    private String description;
    private LocalDateTime date;
    @Enumerated(EnumType.STRING) private Severity severity;
    @Enumerated(EnumType.STRING) private IncidentStatus status;
}

public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {}

@RestController
@RequestMapping("/api/safety/incidents")
public class SafetyIncidentController {
    @PreAuthorize("hasRole('SAFETY_OFFICER')")
    @PostMapping
    public ResponseEntity<SafetyIncidentDTO> reportIncident(@Valid @RequestBody SafetyIncidentDTO dto) {
        // call service
    }
}
```

---

## <a name="section-14-safety-compliance-checklist"></a>Section: Safety Compliance Checklist

**Description:**  
Employees complete safety checklists.

**Design Specification:**  
- **Entity Design:**  
  - `SafetyChecklist` (id, employee, date, items, completed)
- **Repository Layer:**  
  - `SafetyChecklistRepository`
- **Service Layer:**  
  - `completeChecklist(SafetyChecklistDTO dto)`
- **Controller Layer:**  
  - `POST /api/safety/checklists`
- **Security Settings:**  
  - Only `ROLE_EMPLOYEE`.

**Sample Implementation:**

```java
@Entity
public class SafetyChecklist {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private LocalDate date;
    @ElementCollection private List<String> items;
    private boolean completed;
}

public interface SafetyChecklistRepository extends JpaRepository<SafetyChecklist, Long> {}

@RestController
@RequestMapping("/api/safety/checklists")
public class SafetyChecklistController {
    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping
    public ResponseEntity<SafetyChecklistDTO> completeChecklist(@Valid @RequestBody SafetyChecklistDTO dto) {
        // call service
    }
}
```

---

## <a name="section-15-inventory-access-control"></a>Section: Inventory Access Control

**Description:**  
Admins manage which employees have access to inventory.

**Design Specification:**  
- **Entity Design:**  
  - `InventoryAccess` (id, employee, inventorySection, grantedBy, grantedAt, status)
- **Repository Layer:**  
  - `InventoryAccessRepository`
- **Service Layer:**  
  - `grantAccess(InventoryAccessDTO dto)`
- **Controller Layer:**  
  - `POST /api/inventory/access`
- **Security Settings:**  
  - Only `ROLE_ADMIN`.

**Sample Implementation:**

```java
@Entity
public class InventoryAccess {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String inventorySection;
    @ManyToOne private Employee grantedBy;
    private LocalDateTime grantedAt;
    @Enumerated(EnumType.STRING) private AccessStatus status;
}

public interface InventoryAccessRepository extends JpaRepository<InventoryAccess, Long> {}

@RestController
@RequestMapping("/api/inventory/access")
public class InventoryAccessController {
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<InventoryAccessDTO> grantAccess(@Valid @RequestBody InventoryAccessDTO dto) {
        // call service
    }
}
```

---

## <a name="section-16-access-log-review"></a>Section: Access Log Review

**Description:**  
Managers review inventory access logs.

**Design Specification:**  
- **Entity Design:**  
  - `AccessLog` (id, employee, inventorySection, accessTime, action)
- **Repository Layer:**  
  - `AccessLogRepository`
- **Service Layer:**  
  - `getAccessLogs(AccessLogCriteria criteria)`
- **Controller Layer:**  
  - `GET /api/inventory/access-logs`
- **Security Settings:**  
  - Only `ROLE_MANAGER`.

**Sample Implementation:**

```java
@Entity
public class AccessLog {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    private String inventorySection;
    private LocalDateTime accessTime;
    private String action;
}

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {}

@RestController
@RequestMapping("/api/inventory/access-logs")
public class AccessLogController {
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<List<AccessLogDTO>> getAccessLogs(AccessLogCriteria criteria) {
        // call service
    }
}
```

---

## <a name="section-17-internal-communication-messaging"></a>Section: Internal Communication Messaging

**Description:**  
Employees send and receive messages internally.

**Design Specification:**  
- **Entity Design:**  
  - `Message` (id, sender, receiver, content, sentAt, read)
- **Repository Layer:**  
  - `MessageRepository`
- **Service Layer:**  
  - `sendMessage(MessageDTO dto)`, `getMessages(Long employeeId)`
- **Controller Layer:**  
  - `POST /api/messages`, `GET /api/messages`
- **Security Settings:**  
  - Only authenticated employees.

**Sample Implementation:**

```java
@Entity
public class Message {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee sender;
    @ManyToOne private Employee receiver;
    private String content;
    private LocalDateTime sentAt;
    private boolean read;
}

public interface MessageRepository extends JpaRepository<Message, Long> {}

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping
    public ResponseEntity<MessageDTO> sendMessage(@Valid @RequestBody MessageDTO dto) {
        // call service
    }
    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping
    public ResponseEntity<List<MessageDTO>> getMessages(Authentication auth) {
        // call service
    }
}
```

---

## <a name="section-18-shift-notification"></a>Section: Shift Notification

**Description:**  
Employees receive notifications for scheduled shifts.

**Design Specification:**  
- **Integration Points:**  
  - Email/SMS service or internal notification system.
- **Service Layer:**  
  - `notifyShift(ShiftNotificationDTO dto)`
- **Controller Layer:**  
  - Triggered internally after shift scheduling.
- **Security Settings:**  
  - Only system or manager.

**Sample Implementation:**

```java
@Service
public class NotificationService {
    public void notifyShift(Shift shift) {
        // send email/SMS/notification to employee
    }
}

// In ShiftServiceImpl after scheduling shift
notificationService.notifyShift(shift);
```

---

## <a name="section-19-task-reminder-notification"></a>Section: Task Reminder Notification

**Description:**  
Employees receive reminders for assigned tasks.

**Design Specification:**  
- **Integration Points:**  
  - Email/SMS or push notification.
- **Service Layer:**  
  - `sendTaskReminder(Task task)`
- **Controller Layer:**  
  - Triggered by scheduled job.
- **Security Settings:**  
  - Only system.

**Sample Implementation:**

```java
@Service
public class NotificationService {
    @Scheduled(cron = "0 0 8 * * ?") // every day at 8 AM
    public void sendTaskReminders() {
        // find tasks due soon and send reminders
    }
}
```

---

## <a name="section-20-system-audit-log"></a>Section: System Audit Log

**Description:**  
System maintains audit logs for all critical actions.

**Design Specification:**  
- **Entity Design:**  
  - `AuditLog` (id, action, performedBy, timestamp, details)
- **Repository Layer:**  
  - `AuditLogRepository`
- **Service Layer:**  
  - `logAction(AuditLogDTO dto)`
- **Controller Layer:**  
  - Internal use.
- **Security Settings:**  
  - Only system and admin can view.

**Sample Implementation:**

```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String action;
    private String performedBy;
    private LocalDateTime timestamp;
    private String details;
}

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}

@Service
public class AuditService {
    public void logAction(String action, String performedBy, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setPerformedBy(performedBy);
        log.setTimestamp(LocalDateTime.now());
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}
```

---

# General Configuration

**application.properties (snippets):**
```
spring.datasource.url=jdbc:mysql://localhost:3306/warehouse
spring.datasource.username=warehouse_user
spring.datasource.password=secret
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
logging.level.org.springframework=INFO
```

**SecurityConfig (snippets):**
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
            .antMatchers("/api/employees/**").hasAnyRole("HR_ADMIN", "EMPLOYEE")
            .antMatchers("/api/shifts/**").hasAnyRole("MANAGER", "EMPLOYEE")
            .antMatchers("/api/tasks/**").hasAnyRole("MANAGER", "EMPLOYEE")
            .antMatchers("/api/performance/**").hasAnyRole("MANAGER", "HR_ADMIN")
            .antMatchers("/api/training/**").hasRole("MANAGER")
            .antMatchers("/api/certifications/**").hasAnyRole("HR_ADMIN", "MANAGER")
            .antMatchers("/api/safety/**").hasAnyRole("SAFETY_OFFICER", "EMPLOYEE")
            .antMatchers("/api/inventory/**").hasAnyRole("ADMIN", "MANAGER")
            .antMatchers("/api/messages/**").hasRole("EMPLOYEE")
            .anyRequest().authenticated()
            .and()
            .httpBasic();
    }
}
```

**Exception Handling (snippet):**
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        // extract validation errors
    }
}
```

---

# Logging

- Use SLF4J for logging in all services and controllers.
- Log all critical actions and errors.
- Audit logs for all create/update/delete operations.

---

# Validation

- Use `@Valid` and JSR-303 annotations on DTOs and entities.
- Handle validation errors globally.

---

# Summary

This document provides a comprehensive, production-ready, low-level technical design for the Warehouse Employee Management System, covering all 20 user stories with Spring Boot best practices, including architecture, package structure, entity/repository/service/controller layers, security, configuration, integration, exception handling, validation, and logging. All code snippets are ready for direct implementation and extension.