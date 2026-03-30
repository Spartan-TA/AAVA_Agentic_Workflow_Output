# Warehouse Employee Management System (EMS)
# Low-Level Technical Design Document

## Table of Contents
1. Introduction
2. Spring Boot Architecture Overview
3. Epics & User Stories Technical Design
   - E01: Project Scaffolding & Domain Setup
   - E02: Employee Master Data CRUD
   - E03: Role Based Access Control
   - E04: Time & Attendance Clock In/Out
   - E05: Shift & Schedule Management
   - E06: Leave & Absence Management
   - E07: Training & Certification Tracking
   - E08: Safety Incidents & OSHA Reporting
   - E09: Equipment & Asset Assignment
   - E10: Performance Reviews & Goals
   - E11: Payroll Export Integration
   - E12: Notifications & Announcements
   - E13: Integration Layer HRIS/WMS APIs
   - E14: Audit Trail & Compliance
   - E15: Reporting & Analytics
   - E16: Mobile Access PWA
   - E17: Onboarding & Offboarding Workflow
   - E18: Localization & Multi-Warehouse
   - E19: Advanced Scheduling AI-Assisted
   - E20: Document Management

---

## 1. Introduction
This document provides a comprehensive low-level technical design for the Warehouse Employee Management System (EMS), covering all user stories derived from 20 high-level epics. It is intended for Spring Boot developers and architects, ensuring clarity, consistency, and adherence to industry standards.

## 2. Spring Boot Architecture Overview
- **Layered Architecture:**
  - Presentation (Controller)
  - Service (Business Logic)
  - Repository (Persistence)
  - Domain (Entities/DTOs)
  - Configuration (Security, Integration, etc.)
- **Best Practices:**
  - Use of Spring Data JPA, Spring Security, Spring MVC, Spring Boot Actuator
  - Modular package structure per bounded context/epic
  - DTOs for API boundaries
  - Exception handling via @ControllerAdvice
  - OpenAPI/Swagger for API documentation
  - Flyway/Liquibase for DB migrations
  - Test coverage: unit, integration, security

---

## 3. Epics & User Stories Technical Design

### E01: Project Scaffolding & Domain Setup
#### Overview
- Initialize Spring Boot (Maven) project
- Configure base packages: `com.wems.{module}`
- Core modules: employee, scheduling, attendance, safety
- DB migrations: Flyway/Liquibase
- Actuator enabled

#### Package Structure
- `com.wems`
  - `employee`
  - `scheduling`
  - `attendance`
  - `safety`
  - `config`
  - `common`

#### Configuration
- `application.yml` for environment settings
- `Flyway`/`Liquibase` for DB
- `Actuator` endpoints enabled

#### Sample Implementation
```java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

---

### E02: Employee Master Data CRUD
#### Domain Model
- **Employee**
  - id (Long, PK)
  - badgeId (String, unique)
  - name (String)
  - role (Enum)
  - department (String)
  - shiftGroup (String)
  - hireDate (LocalDate)
  - status (Enum: ACTIVE, INACTIVE)
  - deleted (Boolean, soft-delete)

#### Repository
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

#### Service Layer
```java
public interface EmployeeService {
    EmployeeDTO create(EmployeeDTO dto);
    EmployeeDTO update(Long id, EmployeeDTO dto);
    void delete(Long id);
    Page<EmployeeDTO> list(Pageable pageable, EmployeeFilter filter);
}
```

#### Controller
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@RequestBody @Valid EmployeeDTO dto) {...}
    @GetMapping
    public Page<EmployeeDTO> list(...) {...}
    @PutMapping("/{id}")
    public EmployeeDTO update(@PathVariable Long id, @RequestBody EmployeeDTO dto) {...}
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {...}
}
```

#### OpenAPI
- Annotate controllers and DTOs for Swagger

---

### E03: Role Based Access Control (RBAC)
#### Security Configuration
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Method/endpoint security via `@PreAuthorize`
- API key/OAuth2 toggle via `application.yml`

#### Sample SecurityConfig
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
            .antMatchers("/actuator/**").permitAll()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer().jwt();
    }
}
```

#### Controller Example
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
@PostMapping("/employees")
public EmployeeDTO createEmployee(...) {...}
```

---

### E04: Time & Attendance (Clock In/Out)
#### Domain Model
- **AttendanceEvent**
  - id (Long)
  - employee (Employee)
  - eventType (Enum: CLOCK_IN, CLOCK_OUT)
  - timestamp (ZonedDateTime)
  - deviceId (String)
  - location (GeoPoint)
  - status (Enum: NORMAL, MISSED, CORRECTED)

#### Service Layer
- Validate geofence/device
- Calculate hours per shift
- Handle missed/corrected punches

#### Controller
```java
@PostMapping("/attendance/clock-in")
public AttendanceEventDTO clockIn(@RequestBody ClockEventRequest req) {...}
```

#### Integration
- Export attendance as CSV

---

### E05: Shift & Schedule Management
#### Domain Model
- **ShiftTemplate** (recurring pattern)
- **Schedule** (employee, shift, date)
- **OvertimeRule**

#### Service Layer
- CRUD for templates/schedules
- Conflict detection
- Bulk assignment

#### Controller
```java
@PostMapping("/shifts/templates")
public ShiftTemplateDTO createTemplate(...) {...}
```

---

### E06: Leave & Absence Management
#### Domain Model
- **LeaveRequest**
  - employee
  - type (PTO, SICK, UNPAID)
  - startDate, endDate
  - status (PENDING, APPROVED, DENIED)
  - accrualBalance

#### Service Layer
- Request, approve, update balances
- Integration with scheduling/payroll

---

### E07: Training & Certification Tracking
#### Domain Model
- **Certification**
  - employee
  - type
  - issueDate, expiryDate
  - documentUrl

#### Service Layer
- CRUD, expiry alerts, scheduling checks

---

### E08: Safety Incidents & OSHA Reporting
#### Domain Model
- **SafetyIncident**
  - id, date, location, description, severity, involvedEmployees, status

#### Service Layer
- Workflow: Open â Investigating â Resolved
- OSHA export

---

### E09: Equipment & Asset Assignment
#### Domain Model
- **Asset**
  - id, type, serial, condition, assignedTo, checkInOutHistory

#### Service Layer
- CRUD, check-in/out, certification checks

---

### E10: Performance Reviews & Goals
#### Domain Model
- **PerformanceReview**
  - employee, period, goals, ratings, comments, status

#### Service Layer
- Review cycles, workflow, PDF export

---

### E11: Payroll Export Integration
#### Integration
- Attendance/leave â payroll file
- Mapping to provider schema
- SFTP/API delivery
- Audit log

---

### E12: Notifications & Announcements
#### Domain Model
- **Notification**
  - user, type, channel, content, status
- **Announcement**

#### Service Layer
- Opt-in/out, templates, delivery tracking

---

### E13: Integration Layer (HRIS/WMS APIs)
#### Integration
- REST APIs for HRIS/WMS
- JWT/OAuth2 security
- Webhooks

---

### E14: Audit Trail & Compliance
#### Domain Model
- **AuditLog**
  - entity, action, actor, timestamp, before, after

#### Service Layer
- Centralized logging, immutable storage

---

### E15: Reporting & Analytics
#### Service Layer
- Attendance, overtime, leave, safety, certification reports
- CSV/PDF export
- Role-based dashboards

---

### E16: Mobile Access (PWA)
#### Frontend
- Responsive views for clock-in/out, schedules, leave, announcements
- Offline queue for clock events
- PWA manifest

---

### E17: Onboarding & Offboarding Workflow
#### Workflow
- New hire: provision accounts, schedule, training, assets
- Offboarding: revoke access, collect assets

---

### E18: Localization & Multi-Warehouse
#### Domain Model
- **Warehouse**
  - id, name, location, locale
- Employee/shift/asset linked to warehouse

#### Service Layer
- Locale-aware templates, multi-warehouse filtering

---

### E19: Advanced Scheduling AI-Assisted
#### Integration
- AI module for shift optimization
- REST endpoint for schedule suggestions

---

### E20: Document Management
#### Domain Model
- **Document**
  - id, type, owner, url, uploadedAt

#### Service Layer
- Upload, link to entities, access control

---

## Appendix
- Exception Handling: `@ControllerAdvice`, custom exceptions
- Validation: `@Valid`, custom validators
- Testing: JUnit, Mockito, MockMvc
- CI/CD: Maven, Docker, GitHub Actions
- Monitoring: Actuator, Prometheus, Grafana

---

## Notes
- All endpoints documented with OpenAPI
- Security rules enforced at method and endpoint level
- All entities use JPA annotations, relationships mapped (e.g., @OneToMany, @ManyToOne)
- Service layer is transactional
- Repository layer uses Spring Data JPA
- Integration points use Feign/WebClient for external APIs
- Sensitive actions are audited
- All modules follow SOLID and DRY principles

---

# END OF DOCUMENT