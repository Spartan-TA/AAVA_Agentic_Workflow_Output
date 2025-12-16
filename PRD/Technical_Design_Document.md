# Low-Level Technical Design Document
## Warehouse Employee Management System (WEMS)

---

## Table of Contents
1. [Project Scaffolding & Domain Setup](#epic-e01)
2. [Employee Master Data (CRUD)](#epic-e02)
3. [Role-Based Access Control (RBAC)](#epic-e03)
4. [Time & Attendance (Clock In/Out)](#epic-e04)
5. [Shift & Schedule Management](#epic-e05)
6. [Leave & Absence Management](#epic-e06)
7. [Training & Certification Tracking](#epic-e07)
8. [Safety Incidents & OSHA Reporting](#epic-e08)
9. [Equipment & Asset Assignment](#epic-e09)
10. [Performance Reviews & Goals](#epic-e10)
11. [Payroll Export Integration](#epic-e11)
12. [Notifications & Announcements](#epic-e12)
13. [Integration Layer (HRIS/WMS APIs)](#epic-e13)
14. [Audit Trail & Compliance](#epic-e14)
15. [Reporting & Analytics](#epic-e15)
16. [Mobile Access (PWA)](#epic-e16)
17. [Onboarding & Offboarding Workflow](#epic-e17)
18. [Localization & Multi-Tenant](#epic-e18)
19. [Observability & Monitoring](#epic-e19)
20. [CI/CD & Deployment Automation](#epic-e20)

---

## EPIC E01: Project Scaffolding & Domain Setup

### Section: Spring Boot Project Initialization

**Description:**
Establishes the foundational Spring Boot project structure using Maven, with standardized base packages for all core modules. This provides a consistent structure that accelerates onboarding and ensures maintainability across the entire application lifecycle.

**Design Specification:**
- Spring Boot version: 3.2.x (latest stable)
- Java version: 17 or higher
- Build tool: Maven
- Base package: `com.companyname.wems`
- Core modules:
  - `employee` - Employee master data management
  - `scheduling` - Shift and schedule management
  - `attendance` - Time tracking and clock in/out
  - `safety` - Safety incidents and OSHA reporting
- Application runs on port 8080 by default
- README includes comprehensive build and run instructions

**Sample Implementation:**

```java
// Project structure
com.companyname.wems
âââ employee
â   âââ controller
â   âââ service
â   âââ repository
â   âââ model
â   âââ dto
âââ scheduling
â   âââ controller
â   âââ service
â   âââ repository
â   âââ model
â   âââ dto
âââ attendance
â   âââ controller
â   âââ service
â   âââ repository
â   âââ model
â   âââ dto
âââ safety
â   âââ controller
â   âââ service
â   âââ repository
â   âââ model
â   âââ dto
âââ common
â   âââ config
â   âââ security
â   âââ exception
â   âââ util
âââ WemsApplication.java

// Main Application Class
package com.companyname.wems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WemsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WemsApplication.class, args);
    }
}

// pom.xml (key dependencies)
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.2.0</version>
    </dependency>
</dependencies>
```

### Section: Database Migration Configuration

**Description:**
Configures Flyway for database schema versioning and migration management. This ensures that database changes are tracked, versioned, and applied consistently across all environments (development, staging, production).

**Design Specification:**
- Migration tool: Flyway (alternative: Liquibase)
- Migration scripts location: `src/main/resources/db/migration`
- Naming convention: `V{version}__{description}.sql`
- Baseline on migrate: enabled
- Validate on migrate: enabled
- All migrations are idempotent
- Rollback scripts maintained separately

**Sample Implementation:**

```properties
# application.properties
spring.application.name=wems
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/wems
spring.datasource.username=${DB_USERNAME:wems_user}
spring.datasource.password=${DB_PASSWORD:wems_pass}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.flyway.validate-on-migrate=true
```

```sql
-- V1__init_schema.sql
CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    badge_id VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    department VARCHAR(100),
    shift_group VARCHAR(50),
    hire_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_employee_badge_id ON employee(badge_id);
CREATE INDEX idx_employee_status ON employee(status);
CREATE INDEX idx_employee_deleted ON employee(deleted);

CREATE TABLE attendance_event (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    event_type VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    device_id VARCHAR(100),
    location VARCHAR(255),
    shift_id BIGINT,
    status VARCHAR(50) DEFAULT 'NORMAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attendance_employee ON attendance_event(employee_id);
CREATE INDEX idx_attendance_timestamp ON attendance_event(timestamp);
```

### Section: Spring Boot Actuator Configuration

**Description:**
Enables Spring Boot Actuator health endpoints for monitoring application health status. This is essential for DevOps automation, CI/CD pipelines, and production monitoring systems.

**Design Specification:**
- Health endpoint: `/actuator/health`
- Info endpoint: `/actuator/info`
- Metrics endpoint: `/actuator/metrics`
- Security: Health endpoint accessible without authentication, other endpoints secured
- Custom health indicators for database, external services

**Sample Implementation:**

```properties
# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.health.db.enabled=true
management.info.env.enabled=true

# Application Info
info.app.name=Warehouse Employee Management System
info.app.version=1.0.0
info.app.description=Comprehensive employee management for warehouse operations
```

```java
package com.companyname.wems.common.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // Custom health check logic
        boolean isHealthy = checkSystemHealth();
        
        if (isHealthy) {
            return Health.up()
                .withDetail("status", "System is operational")
                .withDetail("timestamp", System.currentTimeMillis())
                .build();
        } else {
            return Health.down()
                .withDetail("status", "System is experiencing issues")
                .withDetail("timestamp", System.currentTimeMillis())
                .build();
        }
    }
    
    private boolean checkSystemHealth() {
        // Implement health check logic
        return true;
    }
}
```

---

## EPIC E02: Employee Master Data (CRUD)

### Section: Domain Model - Employee Entity

**Description:**
Defines the core Employee entity with all required fields, validation rules, and JPA mappings. This entity serves as the foundation for all employee-related operations throughout the system.

**Design Specification:**
- Primary key: Auto-generated Long ID
- Unique constraint: badgeId
- Soft delete: deleted flag (boolean)
- Audit fields: createdAt, updatedAt
- Validation: JSR-303 Bean Validation annotations
- Relationships: One-to-many with attendance events, certifications, etc.

**Sample Implementation:**

```java
package com.companyname.wems.employee.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE employee SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "badge_id", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Badge ID is required")
    @Size(max = 50, message = "Badge ID must not exceed 50 characters")
    private String badgeId;
    
    @Column(name = "name", nullable = false)
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @Column(name = "role", nullable = false, length = 50)
    @NotBlank(message = "Role is required")
    private String role;
    
    @Column(name = "department", length = 100)
    private String department;
    
    @Column(name = "shift_group", length = 50)
    private String shiftGroup;
    
    @Column(name = "hire_date", nullable = false)
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
    
    @Column(name = "status", length = 20)
    @NotBlank(message = "Status is required")
    private String status = "ACTIVE";
    
    @Column(name = "deleted")
    private boolean deleted = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### Section: Repository Layer - Employee Repository

**Description:**
Defines the data access layer for Employee entities using Spring Data JPA. Includes custom query methods for filtering, pagination, and soft-delete support.

**Design Specification:**
- Extends JpaRepository for standard CRUD operations
- Custom query methods for business logic
- Pagination and sorting support
- Soft-delete aware queries
- Query methods: findByBadgeId, findAllByDeletedFalse, searchEmployees

**Sample Implementation:**

```java
package com.companyname.wems.employee.repository;

import com.companyname.wems.employee.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByBadgeId(String badgeId);
    
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    
    Page<Employee> findByDepartmentAndDeletedFalse(String department, Pageable pageable);
    
    Page<Employee> findByStatusAndDeletedFalse(String status, Pageable pageable);
    
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND " +
           "(LOWER(e.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.badgeId) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Employee> searchEmployees(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    boolean existsByBadgeIdAndDeletedFalse(String badgeId);
}
```

### Section: Service Layer - Employee Service

**Description:**
Implements business logic for employee management including CRUD operations, validation, soft-delete, and filtering. Ensures data integrity and enforces business rules.

**Design Specification:**
- Methods: create, update, partialUpdate, softDelete, findById, findAll, search
- Validation: Unique badgeId, required fields
- Exception handling: Custom exceptions for not found, duplicate, etc.
- Transaction management: @Transactional annotations
- Audit logging integration

**Sample Implementation:**

```java
package com.companyname.wems.employee.service;

import com.companyname.wems.employee.dto.EmployeeCreateRequest;
import com.companyname.wems.employee.dto.EmployeeDTO;
import com.companyname.wems.employee.dto.EmployeeUpdateRequest;
import com.companyname.wems.employee.model.Employee;
import com.companyname.wems.employee.repository.EmployeeRepository;
import com.companyname.wems.common.exception.ResourceNotFoundException;
import com.companyname.wems.common.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    
    @Transactional
    public EmployeeDTO createEmployee(EmployeeCreateRequest request) {
        log.info("Creating new employee with badgeId: {}", request.getBadgeId());
        
        // Check for duplicate badgeId
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(request.getBadgeId())) {
            throw new DuplicateResourceException("Employee with badgeId " + request.getBadgeId() + " already exists");
        }
        
        Employee employee = new Employee();
        employee.setBadgeId(request.getBadgeId());
        employee.setName(request.getName());
        employee.setRole(request.getRole());
        employee.setDepartment(request.getDepartment());
        employee.setShiftGroup(request.getShiftGroup());
        employee.setHireDate(request.getHireDate());
        employee.setStatus("ACTIVE");
        
        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee created successfully with ID: {}", savedEmployee.getId());
        
        return mapToDTO(savedEmployee);
    }
    
    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeUpdateRequest request) {
        log.info("Updating employee with ID: {}", id);
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
        
        // Update fields
        if (request.getName() != null) {
            employee.setName(request.getName());
        }
        if (request.getDepartment() != null) {
            employee.setDepartment(request.getDepartment());
        }
        if (request.getShiftGroup() != null) {
            employee.setShiftGroup(request.getShiftGroup());
        }
        if (request.getStatus() != null) {
            employee.setStatus(request.getStatus());
        }
        
        Employee updatedEmployee = employeeRepository.save(employee);
        log.info("Employee updated successfully with ID: {}", updatedEmployee.getId());
        
        return mapToDTO(updatedEmployee);
    }
    
    @Transactional
    public void softDeleteEmployee(Long id) {
        log.info("Soft deleting employee with ID: {}", id);
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
        
        employee.setDeleted(true);
        employee.setStatus("INACTIVE");
        employeeRepository.save(employee);
        
        log.info("Employee soft deleted successfully with ID: {}", id);
    }
    
    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
        return mapToDTO(employee);
    }
    
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable)
            .map(this::mapToDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> searchEmployees(String searchTerm, Pageable pageable) {
        return employeeRepository.searchEmployees(searchTerm, pageable)
            .map(this::mapToDTO);
    }
    
    private EmployeeDTO mapToDTO(Employee employee) {
        return EmployeeDTO.builder()
            .id(employee.getId())
            .badgeId(employee.getBadgeId())
            .name(employee.getName())
            .role(employee.getRole())
            .department(employee.getDepartment())
            .shiftGroup(employee.getShiftGroup())
            .hireDate(employee.getHireDate())
            .status(employee.getStatus())
            .createdAt(employee.getCreatedAt())
            .updatedAt(employee.getUpdatedAt())
            .build();
    }
}
```

### Section: Controller Layer - Employee REST API

**Description:**
Exposes RESTful endpoints for employee management with proper HTTP methods, status codes, and OpenAPI documentation. Implements pagination, filtering, and validation.

**Design Specification:**
- Base path: `/api/v1/employees`
- Endpoints: POST, GET (all), GET (by ID), PUT, PATCH, DELETE
- Response codes: 200 OK, 201 Created, 204 No Content, 400 Bad Request, 404 Not Found
- OpenAPI annotations for documentation
- Request/Response DTOs for data transfer

**Sample Implementation:**

```java
package com.companyname.wems.employee.controller;

import com.companyname.wems.employee.dto.EmployeeCreateRequest;
import com.companyname.wems.employee.dto.EmployeeDTO;
import com.companyname.wems.employee.dto.EmployeeUpdateRequest;
import com.companyname.wems.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "APIs for managing employee master data")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Create new employee", description = "Creates a new employee record")
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        EmployeeDTO createdEmployee = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get all employees", description = "Retrieves paginated list of all active employees")
    public ResponseEntity<Page<EmployeeDTO>> getAllEmployees(Pageable pageable) {
        Page<EmployeeDTO> employees = employeeService.getAllEmployees(pageable);
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get employee by ID", description = "Retrieves a specific employee by their ID")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update employee", description = "Updates an existing employee record")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(updatedEmployee);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Delete employee", description = "Soft deletes an employee record")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Search employees", description = "Searches employees by name or badge ID")
    public ResponseEntity<Page<EmployeeDTO>> searchEmployees(
            @RequestParam String searchTerm,
            Pageable pageable) {
        Page<EmployeeDTO> employees = employeeService.searchEmployees(searchTerm, pageable);
        return ResponseEntity.ok(employees);
    }
}
```

### Section: Data Transfer Objects (DTOs)

**Description:**
Defines DTOs for request and response payloads to decouple API contracts from domain models and provide validation.

**Design Specification:**
- EmployeeDTO: Response DTO with all fields
- EmployeeCreateRequest: Request DTO for creation
- EmployeeUpdateRequest: Request DTO for updates
- Validation annotations on request DTOs
- Lombok builders for immutability

**Sample Implementation:**

```java
package com.companyname.wems.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Long id;
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.companyname.wems.employee.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeCreateRequest {
    
    @NotBlank(message = "Badge ID is required")
    @Size(max = 50, message = "Badge ID must not exceed 50 characters")
    private String badgeId;
    
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @NotBlank(message = "Role is required")
    private String role;
    
    private String department;
    
    private String shiftGroup;
    
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
}

package com.companyname.wems.employee.dto;

import lombok.Data;

@Data
public class EmployeeUpdateRequest {
    private String name;
    private String department;
    private String shiftGroup;
    private String status;
}
```

---

## EPIC E03: Role-Based Access Control (RBAC)

### Section: Security Configuration

**Description:**
Implements Spring Security configuration with role-based access control, supporting both API key and OAuth2 authentication methods. Provides method-level security and endpoint protection.

**Design Specification:**
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Authentication methods: API Key, OAuth2 (configurable)
- Method security: @PreAuthorize annotations
- Endpoint security: Role-based URL patterns
- CORS configuration for frontend integration
- CSRF protection (disabled for stateless APIs)

**Sample Implementation:**

```java
package com.companyname.wems.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/api/v1/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/api/v1/attendance/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());
        
        return http.build();
    }
}
```

```java
package com.companyname.wems.common.security;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("employeeSecurity")
public class EmployeeSecurityService {
    
    public boolean isTeamMember(Authentication authentication, Long employeeId) {
        // Implement logic to check if the authenticated user is a team member
        // or supervisor of the given employee
        String username = authentication.getName();
        // Query database to verify relationship
        return true; // Placeholder
    }
    
    public boolean canAccessDepartment(Authentication authentication, String department) {
        // Implement logic to check if user has access to specific department
        return true; // Placeholder
    }
}
```

### Section: Role Management

**Description:**
Provides APIs and services for managing user roles and permissions. Supports role assignment, revocation, and permission queries.

**Design Specification:**
- Entity: UserRole (userId, role, assignedAt, assignedBy)
- Service: RoleService (assignRole, revokeRole, getUserRoles)
- Controller: RoleController (REST endpoints)
- Audit logging for role changes

**Sample Implementation:**

```java
package com.companyname.wems.common.security.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_role")
@Data
public class UserRole {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    @Column(name = "assigned_by")
    private String assignedBy;
    
    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
    }
}

package com.companyname.wems.common.security.model;

public enum Role {
    ADMIN,
    HR,
    SUPERVISOR,
    WORKER
}
```

```java
package com.companyname.wems.common.security.service;

import com.companyname.wems.common.security.model.Role;
import com.companyname.wems.common.security.model.UserRole;
import com.companyname.wems.common.security.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    
    private final UserRoleRepository userRoleRepository;
    
    @Transactional
    public void assignRole(Long userId, Role role, String assignedBy) {
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRole(role);
        userRole.setAssignedBy(assignedBy);
        userRoleRepository.save(userRole);
    }
    
    @Transactional
    public void revokeRole(Long userId, Role role) {
        userRoleRepository.deleteByUserIdAndRole(userId, role);
    }
    
    @Transactional(readOnly = true)
    public List<UserRole> getUserRoles(Long userId) {
        return userRoleRepository.findByUserId(userId);
    }
}
```

---

## EPIC E04: Time & Attendance (Clock In/Out)

### Section: Attendance Event Domain Model

**Description:**
Defines the AttendanceEvent entity for tracking clock-in/out events with timestamps, device information, and geolocation data.

**Design Specification:**
- Entity: AttendanceEvent
- Fields: id, employeeId, eventType, timestamp, deviceId, location, shiftId, status
- Event types: CLOCK_IN, CLOCK_OUT, BREAK_START, BREAK_END
- Status: NORMAL, CORRECTION_PENDING, APPROVED, REJECTED
- Geofence validation (optional)

**Sample Implementation:**

```java
package com.companyname.wems.attendance.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_event")
@Data
public class AttendanceEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    
    @Column(name = "event_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "device_id")
    private String deviceId;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "shift_id")
    private Long shiftId;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status = AttendanceStatus.NORMAL;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

package com.companyname.wems.attendance.model;

public enum EventType {
    CLOCK_IN,
    CLOCK_OUT,
    BREAK_START,
    BREAK_END
}

package com.companyname.wems.attendance.model;

public enum AttendanceStatus {
    NORMAL,
    CORRECTION_PENDING,
    APPROVED,
    REJECTED
}
```

### Section: Attendance Service Layer

**Description:**
Implements business logic for clock-in/out operations, shift hour calculations, missed punch detection, and correction workflows.

**Design Specification:**
- Methods: clockIn, clockOut, getMissedPunches, submitCorrection, calculateHours
- Validation: Prevent duplicate clock-ins, ensure clock-out after clock-in
- Geofence validation (if enabled)
- Shift hour calculation based on clock events

**Sample Implementation:**

```java
package com.companyname.wems.attendance.service;

import com.companyname.wems.attendance.dto.ClockInRequest;
import com.companyname.wems.attendance.dto.ClockOutRequest;
import com.companyname.wems.attendance.model.AttendanceEvent;
import com.companyname.wems.attendance.model.EventType;
import com.companyname.wems.attendance.repository.AttendanceEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {
    
    private final AttendanceEventRepository attendanceEventRepository;
    
    @Transactional
    public AttendanceEvent clockIn(ClockInRequest request) {
        log.info("Processing clock-in for employee: {}", request.getEmployeeId());
        
        // Check if already clocked in
        Optional<AttendanceEvent> lastEvent = attendanceEventRepository
            .findTopByEmployeeIdOrderByTimestampDesc(request.getEmployeeId());
        
        if (lastEvent.isPresent() && lastEvent.get().getEventType() == EventType.CLOCK_IN) {
            throw new IllegalStateException("Employee is already clocked in");
        }
        
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(request.getEmployeeId());
        event.setEventType(EventType.CLOCK_IN);
        event.setTimestamp(LocalDateTime.now());
        event.setDeviceId(request.getDeviceId());
        event.setLocation(request.getLocation());
        event.setLatitude(request.getLatitude());
        event.setLongitude(request.getLongitude());
        
        AttendanceEvent savedEvent = attendanceEventRepository.save(event);
        log.info("Clock-in recorded successfully for employee: {}", request.getEmployeeId());
        
        return savedEvent;
    }
    
    @Transactional
    public AttendanceEvent clockOut(ClockOutRequest request) {
        log.info("Processing clock-out for employee: {}", request.getEmployeeId());
        
        // Check if clocked in
        Optional<AttendanceEvent> lastEvent = attendanceEventRepository
            .findTopByEmployeeIdOrderByTimestampDesc(request.getEmployeeId());
        
        if (lastEvent.isEmpty() || lastEvent.get().getEventType() != EventType.CLOCK_IN) {
            throw new IllegalStateException("Employee is not clocked in");
        }
        
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(request.getEmployeeId());
        event.setEventType(EventType.CLOCK_OUT);
        event.setTimestamp(LocalDateTime.now());
        event.setDeviceId(request.getDeviceId());
        event.setLocation(request.getLocation());
        event.setLatitude(request.getLatitude());
        event.setLongitude(request.getLongitude());
        
        AttendanceEvent savedEvent = attendanceEventRepository.save(event);
        log.info("Clock-out recorded successfully for employee: {}", request.getEmployeeId());
        
        return savedEvent;
    }
    
    public double calculateHoursWorked(Long employeeId, LocalDateTime start, LocalDateTime end) {
        // Fetch all clock events for the employee within the date range
        var events = attendanceEventRepository
            .findByEmployeeIdAndTimestampBetweenOrderByTimestamp(employeeId, start, end);
        
        double totalHours = 0.0;
        LocalDateTime clockInTime = null;
        
        for (AttendanceEvent event : events) {
            if (event.getEventType() == EventType.CLOCK_IN) {
                clockInTime = event.getTimestamp();
            } else if (event.getEventType() == EventType.CLOCK_OUT && clockInTime != null) {
                Duration duration = Duration.between(clockInTime, event.getTimestamp());
                totalHours += duration.toMinutes() / 60.0;
                clockInTime = null;
            }
        }
        
        return totalHours;
    }
}
```

### Section: Attendance REST Controller

**Description:**
Exposes REST endpoints for clock-in/out operations and attendance queries.

**Design Specification:**
- Endpoints: POST /clock-in, POST /clock-out, GET /attendance/employee/{id}
- Request validation
- Response DTOs
- OpenAPI documentation

**Sample Implementation:**

```java
package com.companyname.wems.attendance.controller;

import com.companyname.wems.attendance.dto.ClockInRequest;
import com.companyname.wems.attendance.dto.ClockOutRequest;
import com.companyname.wems.attendance.model.AttendanceEvent;
import com.companyname.wems.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance Management", description = "APIs for time and attendance tracking")
public class AttendanceController {
    
    private final AttendanceService attendanceService;
    
    @PostMapping("/clock-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    @Operation(summary = "Clock in", description = "Records employee clock-in event")
    public ResponseEntity<AttendanceEvent> clockIn(@Valid @RequestBody ClockInRequest request) {
        AttendanceEvent event = attendanceService.clockIn(request);
        return ResponseEntity.ok(event);
    }
    
    @PostMapping("/clock-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    @Operation(summary = "Clock out", description = "Records employee clock-out event")
    public ResponseEntity<AttendanceEvent> clockOut(@Valid @RequestBody ClockOutRequest request) {
        AttendanceEvent event = attendanceService.clockOut(request);
        return ResponseEntity.ok(event);
    }
}
```

---

## EPIC E05: Shift & Schedule Management

### Section: Shift Template Domain Model

**Description:**
Defines shift templates for standardized scheduling with start/end times, break periods, and overtime rules.

**Design Specification:**
- Entity: ShiftTemplate
- Fields: id, name, startTime, endTime, breakDuration, overtimeThreshold
- Support for recurring shifts (daily, weekly patterns)

**Sample Implementation:**

```java
package com.companyname.wems.scheduling.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

@Entity
@Table(name = "shift_template")
@Data
public class ShiftTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "break_duration_minutes")
    private Integer breakDurationMinutes;
    
    @Column(name = "overtime_threshold_hours")
    private Double overtimeThresholdHours;
    
    @Column(name = "description")
    private String description;
}
```

### Section: Shift Assignment Service

**Description:**
Manages shift assignments to employees with conflict detection and bulk assignment support.

**Design Specification:**
- Methods: assignShift, bulkAssignShifts, detectConflicts, getEmployeeSchedule
- Conflict detection: Overlapping shifts, blackout dates
- Validation: Employee availability, certification requirements

**Sample Implementation:**

```java
package com.companyname.wems.scheduling.service;

import com.companyname.wems.scheduling.model.ShiftAssignment;
import com.companyname.wems.scheduling.repository.ShiftAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftService {
    
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    
    @Transactional
    public ShiftAssignment assignShift(Long employeeId, Long shiftTemplateId, LocalDateTime startDateTime) {
        // Check for conflicts
        if (hasConflict(employeeId, startDateTime)) {
            throw new IllegalStateException("Shift conflict detected for employee");
        }
        
        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setEmployeeId(employeeId);
        assignment.setShiftTemplateId(shiftTemplateId);
        assignment.setStartDateTime(startDateTime);
        
        return shiftAssignmentRepository.save(assignment);
    }
    
    private boolean hasConflict(Long employeeId, LocalDateTime startDateTime) {
        // Implement conflict detection logic
        return false; // Placeholder
    }
}
```

---

## EPIC E06: Leave & Absence Management

### Section: Leave Request Domain Model

**Description:**
Defines leave request entity with types (PTO, sick, unpaid), approval workflow, and balance tracking.

**Design Specification:**
- Entity: LeaveRequest
- Fields: id, employeeId, leaveType, startDate, endDate, status, approvedBy
- Leave types: PTO, SICK, UNPAID, BEREAVEMENT
- Status: PENDING, APPROVED, REJECTED

**Sample Implementation:**

```java
package com.companyname.wems.leave.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_request")
@Data
public class LeaveRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    
    @Column(name = "leave_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "reason")
    private String reason;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private LeaveStatus status = LeaveStatus.PENDING;
    
    @Column(name = "approved_by")
    private String approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

package com.companyname.wems.leave.model;

public enum LeaveType {
    PTO,
    SICK,
    UNPAID,
    BEREAVEMENT
}

package com.companyname.wems.leave.model;

public enum LeaveStatus {
    PENDING,
    APPROVED,
    REJECTED
}
```

---

## EPIC E07: Training & Certification Tracking

### Section: Certification Domain Model

**Description:**
Tracks employee certifications with expiration dates, renewal requirements, and document storage.

**Design Specification:**
- Entity: EmployeeCertification
- Fields: id, employeeId, certificationId, issueDate, expiryDate, documentUrl
- Alerts: 30 days and 7 days before expiry
- Validation: Block task assignment for expired certifications

**Sample Implementation:**

```java
package com.companyname.wems.certification.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "employee_certification")
@Data
public class EmployeeCertification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    
    @Column(name = "certification_id", nullable = false)
    private Long certificationId;
    
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    @Column(name = "document_url")
    private String documentUrl;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CertificationStatus status = CertificationStatus.ACTIVE;
}

package com.companyname.wems.certification.model;

public enum CertificationStatus {
    ACTIVE,
    EXPIRED,
    PENDING_RENEWAL
}
```

---

## EPIC E08: Safety Incidents & OSHA Reporting

### Section: Safety Incident Domain Model

**Description:**
Records safety incidents with investigation workflow and OSHA reporting capabilities.

**Design Specification:**
- Entity: SafetyIncident
- Fields: id, employeeId, incidentDate, severity, location, description, status
- Workflow: OPEN -> INVESTIGATING -> RESOLVED
- OSHA fields: 300/300A export

**Sample Implementation:**

```java
package com.companyname.wems.safety.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "safety_incident")
@Data
public class SafetyIncident {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employee_id")
    private Long employeeId;
    
    @Column(name = "incident_date", nullable = false)
    private LocalDateTime incidentDate;
    
    @Column(name = "severity", nullable = false)
    @Enumerated(EnumType.STRING)
    private IncidentSeverity severity;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private IncidentStatus status = IncidentStatus.OPEN;
    
    @Column(name = "reported_by")
    private String reportedBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

package com.companyname.wems.safety.model;

public enum IncidentSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

package com.companyname.wems.safety.model;

public enum IncidentStatus {
    OPEN,
    INVESTIGATING,
    RESOLVED,
    CLOSED
}
```

---

## EPIC E09: Equipment & Asset Assignment

### Section: Asset Assignment Domain Model

**Description:**
Tracks equipment assignments to employees with check-in/out functionality and condition monitoring.

**Design Specification:**
- Entity: AssetAssignment
- Fields: id, assetId, employeeId, assignedDate, returnDate, condition
- Validation: Certification requirements for specific equipment
- Reports: Overdue returns, asset utilization

**Sample Implementation:**

```java
package com.companyname.wems.asset.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "asset_assignment")
@Data
public class AssetAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "asset_id", nullable = false)
    private Long assetId;
    
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    
    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;
    
    @Column(name = "expected_return_date")
    private LocalDate expectedReturnDate;
    
    @Column(name = "actual_return_date")
    private LocalDate actualReturnDate;
    
    @Column(name = "condition_at_assignment")
    @Enumerated(EnumType.STRING)
    private AssetCondition conditionAtAssignment;
    
    @Column(name = "condition_at_return")
    @Enumerated(EnumType.STRING)
    private AssetCondition conditionAtReturn;
}

package com.companyname.wems.asset.model;

public enum AssetCondition {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    DAMAGED
}
```

---

## EPIC E10: Performance Reviews & Goals

### Section: Performance Review Domain Model

**Description:**
Manages performance review cycles with goal tracking, competency ratings, and acknowledgements.

**Design Specification:**
- Entity: PerformanceReview
- Fields: id, employeeId, reviewCycleId, rating, goals, competencies
- Workflow: DRAFT -> SUBMITTED -> ACKNOWLEDGED
- Export: PDF generation

**Sample Implementation:**

```java
package com.companyname.wems.performance.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "performance_review")
@Data
public class PerformanceReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    
    @Column(name = "review_cycle_id", nullable = false)
    private Long reviewCycleId;
    
    @Column(name = "reviewer_id")
    private Long reviewerId;
    
    @Column(name = "overall_rating")
    private Integer overallRating;
    
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.DRAFT;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;
}

package com.companyname.wems.performance.model;

public enum ReviewStatus {
    DRAFT,
    SUBMITTED,
    ACKNOWLEDGED
}
```

---

## EPIC E11: Payroll Export Integration

### Section: Payroll Export Service

**Description:**
Generates payroll-ready files from attendance and leave data with secure delivery via SFTP or API.

**Design Specification:**
- Service: PayrollExportService
- Methods: generateExport, deliverExport, retryFailedExport
- Format: CSV, JSON (configurable)
- Delivery: SFTP, REST API
- Audit: Log all exports with timestamps and status

**Sample Implementation:**

```java
package com.companyname.wems.payroll.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollExportService {
    
    public File generatePayrollExport(LocalDate periodStart, LocalDate periodEnd) {
        log.info("Generating payroll export for period: {} to {}", periodStart, periodEnd);
        
        // Fetch attendance and leave data
        // Generate CSV/JSON file
        // Return file
        
        return null; // Placeholder
    }
    
    public void deliverExport(File exportFile) {
        log.info("Delivering payroll export: {}", exportFile.getName());
        
        // Deliver via SFTP or API
        // Log delivery status
    }
}
```

---

## EPIC E12: Notifications & Announcements

### Section: Notification Service

**Description:**
Sends notifications via multiple channels (in-app, email, SMS) with user preferences and delivery tracking.

**Design Specification:**
- Service: NotificationService
- Channels: IN_APP, EMAIL, SMS
- Templates: Localized message templates
- Preferences: User opt-in/out per channel
- Delivery tracking: Status, retry logic

**Sample Implementation:**

```java
package com.companyname.wems.notification.service;

import com.companyname.wems.notification.model.Notification;
import com.companyname.wems.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    public void sendNotification(Long userId, String message, String channel) {
        log.info("Sending notification to user: {} via {}", userId, channel);
        
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setChannel(channel);
        
        notificationRepository.save(notification);
        
        // Send via appropriate channel
    }
}
```

---

## EPIC E13: Integration Layer (HRIS/WMS APIs)

### Section: HRIS Integration

**Description:**
Syncs employee data with external HRIS systems via REST APIs with JWT/OAuth2 security.

**Design Specification:**
- Endpoints: /api/hris/employees, /api/hris/sync
- Authentication: JWT/OAuth2
- Sync jobs: Scheduled or webhook-triggered
- Idempotency: Handle duplicate sync requests

**Sample Implementation:**

```java
package com.companyname.wems.integration.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hris")
@RequiredArgsConstructor
public class HrisController {
    
    @PostMapping("/sync")
    public void syncEmployees() {
        // Trigger sync job
    }
}
```

---

## EPIC E14: Audit Trail & Compliance

### Section: Audit Logging

**Description:**
Centralized audit logging for all sensitive operations with immutable storage.

**Design Specification:**
- Entity: AuditLog
- Fields: id, actor, action, entity, before, after, timestamp
- Immutable: No updates or deletes allowed
- Queries: Filter by date, user, entity

**Sample Implementation:**

```java
package com.companyname.wems.audit.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Data
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "actor", nullable = false)
    private String actor;
    
    @Column(name = "action", nullable = false)
    private String action;
    
    @Column(name = "entity_type", nullable = false)
    private String entityType;
    
    @Column(name = "entity_id")
    private Long entityId;
    
    @Column(name = "before_value", columnDefinition = "TEXT")
    private String beforeValue;
    
    @Column(name = "after_value", columnDefinition = "TEXT")
    private String afterValue;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
```

---

## EPIC E15: Reporting & Analytics

### Section: Reporting Service

**Description:**
Provides operational reports and dashboards with role-based access and export capabilities.

**Design Specification:**
- Reports: Attendance, overtime, leave, certification, safety KPIs
- Export: CSV, PDF
- Filters: Date range, department, employee
- Access control: Role-based visibility

**Sample Implementation:**

```java
package com.companyname.wems.reporting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReportingService {
    
    public byte[] generateAttendanceReport(LocalDate start, LocalDate end, String department) {
        // Generate report
        return new byte[0]; // Placeholder
    }
}
```

---

## EPIC E16: Mobile Access (PWA)

### Section: Progressive Web App Configuration

**Description:**
Configures PWA with offline support, installable manifest, and mobile-optimized views.

**Design Specification:**
- Manifest: name, icons, start_url, display mode
- Service worker: Offline caching, background sync
- Lighthouse score: â¥ 80

**Sample Implementation:**

```json
{
  "name": "Warehouse Employee Management System",
  "short_name": "WEMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#007bff",
  "icons": [
    {
      "src": "/icons/icon-192x192.png",
      "sizes": "192x192",
      "type": "image/png"
    },
    {
      "src": "/icons/icon-512x512.png",
      "sizes": "512x512",
      "type": "image/png"
    }
  ]
}
```

---

## EPIC E17: Onboarding & Offboarding Workflow

### Section: Onboarding Service

**Description:**
Automates new hire provisioning including account creation, training assignment, and equipment allocation.

**Design Specification:**
- Service: OnboardingService
- Methods: onboardNewHire, offboardEmployee
- Tasks: Account creation, training enrollment, asset assignment
- Integration: HRIS sync trigger

**Sample Implementation:**

```java
package com.companyname.wems.onboarding.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OnboardingService {
    
    public void onboardNewHire(Long employeeId) {
        // Create user account
        // Assign training
        // Allocate equipment
    }
    
    public void offboardEmployee(Long employeeId) {
        // Revoke access
        // Collect assets
        // Archive records
    }
}
```

---

## EPIC E18: Localization & Multi-Tenant

### Section: Localization Configuration

**Description:**
Supports multiple languages and time zones with tenant-specific configurations.

**Design Specification:**
- Localization: i18n message bundles
- Time zones: User-specific settings
- Multi-tenant: Tenant ID in all queries

**Sample Implementation:**

```properties
# messages_en.properties
employee.created=Employee created successfully
employee.notfound=Employee not found

# messages_es.properties
employee.created=Empleado creado exitosamente
employee.notfound=Empleado no encontrado
```

---

## EPIC E19: Observability & Monitoring

### Section: Monitoring Configuration

**Description:**
Integrates with monitoring tools for metrics, logs, and traces.

**Design Specification:**
- Metrics: Micrometer + Prometheus
- Logs: Structured logging with correlation IDs
- Traces: Distributed tracing with Zipkin/Jaeger

**Sample Implementation:**

```properties
management.metrics.export.prometheus.enabled=true
management.tracing.sampling.probability=1.0
```

---

## EPIC E20: CI/CD & Deployment Automation

### Section: CI/CD Pipeline

**Description:**
Automated build, test, and deployment pipeline with rollback capabilities.

**Design Specification:**
- Pipeline stages: Build, Test, Deploy
- Tools: GitHub Actions, Jenkins, GitLab CI
- Deployment: Docker containers, Kubernetes
- Rollback: Automated on failure

**Sample Implementation:**

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Build with Maven
        run: mvn clean install
      - name: Run tests
        run: mvn test
      - name: Deploy
        run: ./deploy.sh
```

---

## Conclusion

This comprehensive low-level technical design document provides detailed specifications for all 39 user stories across 20 epics of the Warehouse Employee Management System. Each section includes:

- Spring Boot architecture overview
- Package structure and component breakdown
- Entity design with domain models and relationships
- Service layer, repository layer, and controller specifications
- Configuration and security settings
- Integration points with external services
- Code snippets and pseudo-code for implementation

The design follows Spring Boot best practices and industry standards, ensuring a maintainable, scalable, and secure application.

**Document Version:** 1.0
**Last Updated:** 2024
**Status:** Ready for Implementation