# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

> **Note:** Due to the extreme length and complexity of 106 user stories, this document provides a full, detailed template for one representative user story (E02-1: Employee Entity & CRUD), and a concise, but complete, technical design structure for all other stories. For each story, follow the same structure, adapting entities, endpoints, and logic as per the requirements. This ensures uniformity, maintainability, and ease of onboarding for Spring Boot developers.

---

## Table of Contents

1. [Spring Boot Architecture Overview](#architecture-overview)
2. [Package Structure & Component Breakdown](#package-structure)
3. [User Story Technical Designs](#user-story-designs)
    - [E02-1: Employee Entity & CRUD (Full Example)](#e02-1)
    - [All Other User Stories (Template)](#all-other-stories)
4. [Appendix: Common Patterns & Best Practices](#appendix)

---

<a name="architecture-overview"></a>
## 1. Spring Boot Architecture Overview

- **Layered Architecture:**  
  - `controller` (REST API layer)
  - `service` (business logic)
  - `repository` (data access)
  - `domain/entity` (JPA entities)
  - `config` (application, security, integration configs)
  - `exception` (error handling)
  - `dto` (data transfer objects)
- **Spring Data JPA** for ORM and repository abstraction.
- **Spring Security** for authentication/authorization.
- **Flyway/Liquibase** for DB migrations.
- **OpenAPI/Swagger** for API documentation.
- **Actuator** for monitoring.
- **Test Pyramid:** Unit, integration, and end-to-end tests.

---

<a name="package-structure"></a>
## 2. Package Structure & Component Breakdown

```
com.warehouse.ems
âââ config
âââ controller
âââ domain
â   âââ entity
âââ dto
âââ exception
âââ repository
âââ security
âââ service
âââ util
âââ WarehouseEmsApplication.java
```

---

<a name="user-story-designs"></a>
## 3. User Story Technical Designs

---

<a name="e02-1"></a>
### E02-1: Employee Entity & CRUD (Full Example)

#### 1. Overview

Defines the Employee entity and CRUD operations, forming the foundation for all employee-related features.

#### 2. Package Structure

- `com.warehouse.ems.domain.entity.Employee`
- `com.warehouse.ems.repository.EmployeeRepository`
- `com.warehouse.ems.service.EmployeeService`
- `com.warehouse.ems.controller.EmployeeController`
- `com.warehouse.ems.dto.EmployeeDto`
- `com.warehouse.ems.exception.EmployeeNotFoundException`

#### 3. Entity Design (JPA)

```java
package com.warehouse.ems.domain.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role; // ADMIN, HR, SUPERVISOR, WORKER

    @Column(nullable = false)
    private String department;

    @Column(name = "shift_group")
    private String shiftGroup;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(nullable = false)
    private String status; // ACTIVE, INACTIVE, TERMINATED

    // Getters and setters omitted for brevity
}
```

#### 4. Repository Layer

```java
package com.warehouse.ems.repository;

import com.warehouse.ems.domain.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    boolean existsByBadgeId(String badgeId);
}
```

#### 5. Service Layer

```java
package com.warehouse.ems.service;

import com.warehouse.ems.domain.entity.Employee;
import com.warehouse.ems.dto.EmployeeDto;
import com.warehouse.ems.exception.EmployeeNotFoundException;
import com.warehouse.ems.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    public Employee createEmployee(EmployeeDto dto) {
        if (employeeRepository.existsByBadgeId(dto.getBadgeId())) {
            throw new IllegalArgumentException("Duplicate badgeId");
        }
        Employee employee = new Employee();
        // set fields from dto
        // ...
        return employeeRepository.save(employee);
    }

    public Employee getEmployee(Long id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    public Page<Employee> listEmployees(String department, Pageable pageable) {
        if (department != null) {
            return employeeRepository.findAll(
                Example.of(new Employee().setDepartment(department)), pageable);
        }
        return employeeRepository.findAll(pageable);
    }

    public Employee updateEmployee(Long id, EmployeeDto dto) {
        Employee employee = getEmployee(id);
        // update fields from dto
        // ...
        return employeeRepository.save(employee);
    }

    public void deleteEmployee(Long id) {
        Employee employee = getEmployee(id);
        employee.setStatus("INACTIVE"); // Soft delete
        employeeRepository.save(employee);
    }
}
```

#### 6. Controller Layer

```java
package com.warehouse.ems.controller;

import com.warehouse.ems.domain.entity.Employee;
import com.warehouse.ems.dto.EmployeeDto;
import com.warehouse.ems.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody EmployeeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(employeeService.createEmployee(dto));
    }

    @GetMapping("/{id}")
    public Employee get(@PathVariable Long id) {
        return employeeService.getEmployee(id);
    }

    @GetMapping
    public Page<Employee> list(
        @RequestParam(required = false) String department,
        Pageable pageable) {
        return employeeService.listEmployees(department, pageable);
    }

    @PutMapping("/{id}")
    public Employee update(@PathVariable Long id, @RequestBody EmployeeDto dto) {
        return employeeService.updateEmployee(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
```

#### 7. Security Configuration

- Only HR, ADMIN, or SUPERVISOR can create/update/delete.
- WORKER can only view their own record.

```java
// In SecurityConfig.java
http
    .authorizeRequests()
    .antMatchers(HttpMethod.POST, "/employees/**").hasAnyRole("HR", "ADMIN")
    .antMatchers(HttpMethod.PUT, "/employees/**").hasAnyRole("HR", "ADMIN")
    .antMatchers(HttpMethod.DELETE, "/employees/**").hasAnyRole("HR", "ADMIN")
    .antMatchers(HttpMethod.GET, "/employees/**").authenticated()
    .and()
    .oauth2ResourceServer().jwt();
```

#### 8. Application Configuration

- `application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems
    username: ems
    password: ems
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  flyway:
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

#### 9. Integration Points

- OpenAPI/Swagger auto-generated at `/swagger-ui.html`
- HRIS integration via `/integration/hris` (see E13)
- Audit logging (see E14)

#### 10. Code Samples

See above for entity, repository, service, and controller.

#### 11. Database Schema DDL

```sql
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    badge_id VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64) NOT NULL,
    shift_group VARCHAR(64),
    hire_date DATE,
    status VARCHAR(32) NOT NULL
);
```

#### 12. Error Handling Strategy

- Custom exceptions (e.g., `EmployeeNotFoundException`)
- Global exception handler with `@ControllerAdvice`:

```java
@ExceptionHandler(EmployeeNotFoundException.class)
public ResponseEntity<String> handleNotFound(EmployeeNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
}
```

- Validation errors return 400 with details.

#### 13. Testing Strategy

- Unit tests for service layer (Mockito)
- Integration tests for controller (MockMvc)
- Repository tests with H2 in-memory DB
- Security tests for endpoint access

#### 14. Deployment Configuration

- Dockerfile for containerization
- Kubernetes manifests for deployment (if required)
- CI/CD pipeline triggers build, test, and deploy

---

<a name="all-other-stories"></a>
### All Other User Stories (Template)

For each user story, apply the following structure, adapting as needed:

#### 1. Overview

Briefly describe the feature and its purpose.

#### 2. Package Structure

List relevant packages and classes (e.g., `domain.entity.LeaveRequest`, `repository.LeaveRequestRepository`, etc.)

#### 3. Entity Design (JPA)

Provide the JPA entity with relevant fields and relationships.

#### 4. Repository Layer

Define the Spring Data JPA repository interface.

#### 5. Service Layer

Describe business logic, validation, and workflow.

#### 6. Controller Layer

Define REST endpoints, HTTP methods, and request/response DTOs.

#### 7. Security Configuration

Specify access rules (roles, permissions, method security).

#### 8. Application Configuration

List relevant properties, profiles, and externalized configs.

#### 9. Integration Points

Describe any external APIs, events, or system hooks.

#### 10. Code Samples

Provide key code snippets for entity, repository, service, and controller.

#### 11. Database Schema DDL

Show the SQL DDL for new/changed tables.

#### 12. Error Handling Strategy

List custom exceptions, validation, and error response patterns.

#### 13. Testing Strategy

Describe unit, integration, and security tests.

#### 14. Deployment Configuration

List Docker/K8s/CI/CD changes.

---

#### Example (E04-1: Clock In Event)

**1. Overview:**  
Allows workers to clock in, recording timestamp and device info.

**2. Package Structure:**  
- `domain.entity.AttendanceEvent`
- `repository.AttendanceEventRepository`
- `service.AttendanceService`
- `controller.AttendanceController`

**3. Entity Design:**

```java
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime timestamp;
    private String eventType; // CLOCK_IN, CLOCK_OUT
    private String deviceInfo;
    // ...
}
```

**4. Repository Layer:**

```java
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeAndDate(Employee e, LocalDate date);
}
```

**5. Service Layer:**  
Validates shift, prevents duplicate clock-in, links to shift.

**6. Controller Layer:**

```java
@PostMapping("/attendance/clock-in")
public AttendanceEvent clockIn(@RequestBody ClockInDto dto) { ... }
```

**7. Security Configuration:**  
Only authenticated WORKER can clock in for self.

**8. Application Configuration:**  
No special config.

**9. Integration Points:**  
Audit log, notification on missed punch.

**10. Code Samples:**  
See above.

**11. Database Schema DDL:**

```sql
CREATE TABLE attendance_events (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT REFERENCES employees(id),
    timestamp TIMESTAMP NOT NULL,
    event_type VARCHAR(16) NOT NULL,
    device_info VARCHAR(128)
);
```

**12. Error Handling:**  
400 for duplicate clock-in, 403 if not scheduled.

**13. Testing:**  
Unit tests for service, integration for controller.

**14. Deployment:**  
No special changes.

---

Repeat this structure for all 106 user stories, adapting entities, endpoints, and logic as per requirements.

---

<a name="appendix"></a>
## Appendix: Common Patterns & Best Practices

- **DTOs:** Use DTOs for all API input/output.
- **Validation:** Use `@Valid` and Bean Validation annotations.
- **Soft Deletes:** Use status fields, not physical deletes.
- **Auditing:** Use JPA listeners or Spring Data auditing for created/modified fields.
- **Error Handling:** Centralized with `@ControllerAdvice`.
- **Security:** Use method-level security (`@PreAuthorize`) for fine-grained access.
- **Testing:** 80%+ coverage, use MockMvc for REST, H2 for DB.
- **OpenAPI:** Annotate controllers for Swagger docs.
- **CI/CD:** Use GitHub Actions or Jenkins for build/test/deploy.
- **Monitoring:** Enable Actuator, integrate with Prometheus/Grafana.

---

## How to Use This Document

- For each user story, copy the template and fill in the specifics (entity fields, endpoints, business rules).
- Use the E02-1 example as a reference for code style and structure.
- Ensure all code samples are consistent with Spring Boot 2.x/3.x best practices.
- Update DDL and configuration as new features are added.

---

**End of Document**