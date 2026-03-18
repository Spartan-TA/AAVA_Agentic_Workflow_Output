# Warehouse Employee Management System

## Technical Design Document

---

### Table of Contents
- [E01 Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
- [E02 Employee Master Data (CRUD)](#e02-employee-master-data-crud)
- [E03 Role-Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
- [E04 Time & Attendance (Clock InOut)](#e04-time--attendance-clock-inout)
- [E05 Shift & Schedule Management](#e05-shift--schedule-management)
- [E06 Leave & Absence Management](#e06-leave--absence-management)
- [E07 Training & Certification Tracking](#e07-training--certification-tracking)
- [E08 Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
- [E09 Equipment & Asset Assignment](#e09-equipment--asset-assignment)
- [E10 Performance Reviews & Goals](#e10-performance-reviews--goals)
- [E11 Payroll Export Integration](#e11-payroll-export-integration)
- [E12 Notifications & Announcements](#e12-notifications--announcements)
- [E13 Integration Layer (HRIS/WMS APIs)](#e13-integration-layer-hriswms-apis)
- [E14 Audit Trail & Compliance](#e14-audit-trail--compliance)
- [E15 Reporting & Analytics](#e15-reporting--analytics)
- [E16 Mobile Access (PWA)](#e16-mobile-access-pwa)
- [E17 Onboarding & Offboarding Workflow](#e17-onboarding--offboarding-workflow)

---

## E01 Project Scaffolding & Domain Setup

### Spring Boot Architecture Overview
- Maven-based Spring Boot project
- Modular package structure: employee, scheduling, attendance, safety, etc.
- Flyway/Liquibase for DB migrations
- Spring Boot Actuator enabled

### Package Structure
```
com.wms
  âââ employee
  âââ scheduling
  âââ attendance
  âââ safety
  âââ asset
  âââ review
  âââ payroll
  âââ notification
  âââ integration
  âââ audit
  âââ reporting
  âââ config
  âââ security
```

### Configuration Classes
- `application.yml` example:
```yaml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wms
    username: wms_user
    password: secret
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
  flyway:
    enabled: true
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

---

## E02 Employee Master Data (CRUD)

### Entity Design
```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String badgeId;

    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted = false;
}
```

### Repository Layer
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

### Service Layer
```java
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository repo;

    @Transactional
    public Employee create(EmployeeDTO dto) {
        // validation, mapping, save
    }

    public Page<EmployeeDTO> list(Pageable pageable) {
        // mapping, filtering
    }

    @Transactional
    public void softDelete(Long id) {
        // mark as deleted
    }
}
```

### Controller Layer
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeDTO dto) {...}

    @GetMapping
    public Page<EmployeeDTO> list(Pageable pageable) {...}

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> update(@PathVariable Long id, @RequestBody EmployeeDTO dto) {...}

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {...}
}
```

### DTOs & Mappers
```java
public class EmployeeDTO {
    private Long id;
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}
```

### Validation Rules
- badgeId: unique, not null
- name: not null
- role: must be valid enum
- hireDate: not in future

### Exception Handling
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {...}
}
```

---

## E03 Role-Based Access Control (RBAC)

### Security Configuration
```java
@Configuration
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
            .oauth2ResourceServer().jwt();
    }
}
```

### Role Definitions
- ADMIN: Full access
- HR: Manage employee records
- SUPERVISOR: Team-level access
- WORKER: Self-service

### Exception Handling
- 401 Unauthorized for unauthenticated
- 403 Forbidden for unauthorized

---

## E04 Time & Attendance (Clock In/Out)

### Entity Design
```java
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    private LocalDateTime eventTime;
    private String eventType; // CLOCK_IN, CLOCK_OUT
    private String deviceId;
    private String location;
    private boolean correctionRequested;
}
```

### Service Layer
- Calculate hours worked per shift
- Handle missed punches and corrections

### Controller Layer
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody AttendanceDTO dto) {...}

    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody AttendanceDTO dto) {...}
}
```

### Validation Rules
- Event time must be within shift
- Device/location must match geofence

---

## E05 Shift & Schedule Management

### Entity Design
```java
@Entity
public class ShiftTemplate {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private LocalTime start;
    private LocalTime end;
    private boolean recurring;
}

@Entity
public class EmployeeSchedule {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ShiftTemplate shiftTemplate;
    private LocalDate date;
}
```

### Service Layer
- Conflict detection
- Bulk assignment

### Controller Layer
```java
@RestController
@RequestMapping("/schedules")
public class ScheduleController {
    @PostMapping
    public ResponseEntity<?> assign(@RequestBody ScheduleDTO dto) {...}
}
```

---

## E06 Leave & Absence Management

### Entity Design
```java
@Entity
public class LeaveRequest {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String leaveType;
    private LocalDate start;
    private LocalDate end;
    private String status; // REQUESTED, APPROVED, DENIED
    private int accrualBalance;
}
```

### Service Layer
- PTO accruals
- Integration with scheduling

---

## E07 Training & Certification Tracking

### Entity Design
```java
@Entity
public class Certification {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private LocalDate expiryDate;
    private String documentUrl;
    @ManyToOne
    private Employee employee;
}
```

### Service Layer
- Expiry alerts
- Scheduling checks

---

## E08 Safety Incidents & OSHA Reporting

### Entity Design
```java
@Entity
public class SafetyIncident {
    @Id
    @GeneratedValue
    private Long id;
    private String severity;
    private String location;
    private String description;
    private String status; // Open, Investigating, Resolved
    @ManyToMany
    private List<Employee> involvedEmployees;
}
```

### Controller Layer
- Incident reporting
- OSHA export

---

## E09 Equipment & Asset Assignment

### Entity Design
```java
@Entity
public class Asset {
    @Id
    @GeneratedValue
    private Long id;
    private String assetType;
    private String condition;
    private boolean checkedOut;
    @ManyToOne
    private Employee assignedTo;
}
```

### Service Layer
- Certification checks
- History logs

---

## E10 Performance Reviews & Goals

### Entity Design
```java
@Entity
public class PerformanceReview {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String cycle;
    private String goals;
    private String competencies;
    private String ratings;
    private String comments;
    private boolean acknowledged;
}
```

---

## E11 Payroll Export Integration

### Integration Points
- SFTP/API delivery
- Mapping attendance/leave to payroll schema

---

## E12 Notifications & Announcements

### Entity Design
```java
@Entity
public class Notification {
    @Id
    @GeneratedValue
    private Long id;
    private String channel; // EMAIL, SMS, IN_APP
    private String template;
    private String status;
    private LocalDateTime sentAt;
    @ManyToOne
    private Employee recipient;
}
```

### Service Layer
- Opt-in/out
- Delivery tracking

---

## E13 Integration Layer (HRIS/WMS APIs)

### REST API Endpoints
- JWT/OAuth2 secured
- Webhooks for events

---

## E14 Audit Trail & Compliance

### Entity Design
```java
@Entity
public class AuditLog {
    @Id
    @GeneratedValue
    private Long id;
    private String entity;
    private String action;
    private String actor;
    private LocalDateTime timestamp;
    private String before;
    private String after;
}
```

---

## E15 Reporting & Analytics

### Controller Layer
- CSV/PDF export endpoints
- Metrics dashboard

---

## E16 Mobile Access (PWA)

### PWA Manifest
- Responsive views
- Offline queue for clock events

---

## E17 Onboarding & Offboarding Workflow

### Service Layer
- HRIS sync
- Asset provisioning/deprovisioning
- Schedule updates

---

## Exception Handling Strategies
- Global exception handler
- Custom exceptions for business logic

## Validation Rules
- Bean validation annotations
- Custom validators for business constraints

## Transaction Management
- `@Transactional` on service methods

## Dependency Injection
- `@Autowired` for services/repositories

## Industry Standards
- RESTful endpoints
- OpenAPI documentation
- Spring Security best practices
- JPA entity relationships

---

## Example: Employee CRUD Flow

1. **POST /employees**
   - Validates DTO
   - Maps to entity
   - Saves via repository
   - Returns DTO

2. **GET /employees**
   - Paginates, filters
   - Maps entities to DTOs

3. **PUT /employees/{id}**
   - Validates
   - Updates entity
   - Returns DTO

4. **DELETE /employees/{id}**
   - Marks as deleted

---

## Code Snippets

### Entity Relationship Example
```java
@Entity
public class Employee {
    ...
    @OneToMany(mappedBy = "employee")
    private List<AttendanceEvent> attendanceEvents;
}
```

### DTO Mapper Example
```java
@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeDTO toDto(Employee entity);
    Employee toEntity(EmployeeDTO dto);
}
```

---

## Integration Points
- HRIS: REST connector, scheduled sync
- WMS: Department/location mapping
- Payroll: Export job, SFTP/API

---

## Security
- Spring Security config
- OAuth2/JWT
- API key toggle via config

---

## Application.yml Structure
- DB, security, notification, integration configs

---

## Exception Handling
- Global handler, custom exceptions

---

## Validation
- Bean validation, custom rules

---

## Transaction Management
- Service layer `@Transactional`

---

## Dependency Injection
- `@Autowired`, constructor injection

---

## OpenAPI Documentation
- Annotate controllers with `@Operation`, `@ApiResponse`

---

## Testing
- Unit/integration tests for endpoints, security, business logic

---

## Summary
This technical design document provides a comprehensive, production-ready blueprint for implementing the Warehouse Employee Management System in Spring Boot, covering all epics, domain models, service and repository layers, REST endpoints, security, integration, exception handling, validation, and code examples. Developers can follow this guide to ensure uniformity, quality, and compliance across the system.
