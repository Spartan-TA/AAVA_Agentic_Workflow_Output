# WAREHOUSE EMPLOYEE MANAGEMENT SYSTEM (EMS)
# LOW-LEVEL TECHNICAL DESIGN DOCUMENT

## DOCUMENT OVERVIEW

This document provides comprehensive low-level technical design specifications for the Warehouse Employee Management System (EMS) built with Spring Boot. The system encompasses 20 epics covering employee management, scheduling, attendance, safety, compliance, and advanced features.

## TABLE OF CONTENTS

1. [E01: Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
2. [E02: Employee Master Data (CRUD)](#e02-employee-master-data-crud)
3. [E03: Role Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
4. [E04: Time & Attendance (Clock In/Out)](#e04-time--attendance-clock-inout)
5. [E05: Shift & Schedule Management](#e05-shift--schedule-management)
6. [E06: Leave & Absence Management](#e06-leave--absence-management)
7. [E07: Training & Certification Tracking](#e07-training--certification-tracking)
8. [E08: Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
9. [E09: Equipment & Asset Assignment](#e09-equipment--asset-assignment)
10. [E10: Performance Reviews & Goals](#e10-performance-reviews--goals)
11. [E11: Payroll Export Integration](#e11-payroll-export-integration)
12. [E12: Notifications & Announcements](#e12-notifications--announcements)
13. [E13: Integration Layer (HRIS/WMS APIs)](#e13-integration-layer-hriswms-apis)
14. [E14: Audit Trail & Compliance](#e14-audit-trail--compliance)
15. [E15: Reporting & Analytics](#e15-reporting--analytics)
16. [E16: Mobile Access (PWA)](#e16-mobile-access-pwa)
17. [E17: Onboarding & Offboarding Workflow](#e17-onboarding--offboarding-workflow)
18. [E18: User Preferences & Localization](#e18-user-preferences--localization)
19. [E19: Advanced Scheduling (AI/ML)](#e19-advanced-scheduling-aiml)
20. [E20: DevOps & Deployment](#e20-devops--deployment)

---

## E01: Project Scaffolding & Domain Setup

### Section: Architecture Overview

**Description:**
Establishes the foundational Spring Boot project structure using Maven, with modular package organization supporting core business domains (employee, scheduling, attendance, safety). Implements database migration tooling and application monitoring capabilities.

**Design Specification:**
- Spring Boot 3.2.0 with Maven build system
- Base package: `com.company.warehouse`
- Core modules: `core`, `employee`, `scheduling`, `attendance`, `safety`
- Database migration: Flyway
- Monitoring: Spring Boot Actuator
- Application runs on port 8080

**Sample Implementation:**

```java
// Maven pom.xml (excerpt)
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>

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
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
</dependencies>
```

### Section: Application Configuration

**Description:**
Centralized configuration for database connectivity, JPA settings, Flyway migrations, and Actuator endpoints.

**Design Specification:**
- PostgreSQL database connection
- JPA with Hibernate validation mode
- Flyway baseline migration
- Actuator health and metrics endpoints exposed

**Sample Implementation:**

```yaml
# application.yml
server:
  port: 8080

spring:
  application:
    name: warehouse-ems
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse
    username: warehouse_user
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

### Section: Main Application Class

**Description:**
Spring Boot application entry point with component scanning enabled.

**Sample Implementation:**

```java
// WarehouseApplication.java
package com.company.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class WarehouseApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseApplication.class, args);
    }
}
```

### Section: Database Migration

**Description:**
Flyway baseline migration creating initial employee table schema.

**Sample Implementation:**

```sql
-- db/migration/V1__init_employee_schema.sql
CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    badge_id VARCHAR(50) UNIQUE NOT NULL,
    role VARCHAR(50) NOT NULL,
    department VARCHAR(50),
    shift_group VARCHAR(50),
    hire_date DATE,
    status VARCHAR(20) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_employee_badge_id ON employee(badge_id);
CREATE INDEX idx_employee_deleted ON employee(deleted);
```

---

## E02: Employee Master Data (CRUD)

### Section: Domain Model

**Description:**
Employee entity representing warehouse workers with comprehensive attributes including identification, role, department, and employment status. Implements soft-delete pattern for data retention.

**Design Specification:**
- Package: `com.company.warehouse.employee.domain`
- Entity: Employee
- Fields: id, name, badgeId (unique), role, department, shiftGroup, hireDate, status, deleted
- Auditing: createdAt, updatedAt
- Validation: @NotBlank, @Size, @PastOrPresent
- Soft-delete support via deleted flag

**Sample Implementation:**

```java
// Employee.java
package com.company.warehouse.employee.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee", 
       uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
@EntityListeners(AuditingEntityListener.class)
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Badge ID is required")
    @Size(max = 50, message = "Badge ID must not exceed 50 characters")
    @Column(name = "badge_id", unique = true, nullable = false, length = 50)
    private String badgeId;

    @NotBlank(message = "Role is required")
    @Column(nullable = false, length = 50)
    private String role;

    @Column(length = 50)
    private String department;

    @Column(name = "shift_group", length = 50)
    private String shiftGroup;

    @PastOrPresent(message = "Hire date cannot be in the future")
    @Column(name = "hire_date")
    private LocalDate hireDate;

    @NotBlank(message = "Status is required")
    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private boolean deleted = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Employee() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getShiftGroup() { return shiftGroup; }
    public void setShiftGroup(String shiftGroup) { this.shiftGroup = shiftGroup; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

### Section: Repository Layer

**Description:**
Spring Data JPA repository providing database access with custom query methods for badge ID lookup and soft-delete filtering.

**Design Specification:**
- Package: `com.company.warehouse.employee.repository`
- Interface: EmployeeRepository extends JpaRepository
- Custom queries for badgeId lookup and deleted filtering
- Pagination support

**Sample Implementation:**

```java
// EmployeeRepository.java
package com.company.warehouse.employee.repository;

import com.company.warehouse.employee.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND " +
           "(LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.badgeId) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Employee> searchEmployees(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND e.department = :department")
    Page<Employee> findByDepartment(@Param("department") String department, Pageable pageable);
    
    boolean existsByBadgeIdAndDeletedFalse(String badgeId);
}
```

### Section: Service Layer

**Description:**
Business logic layer handling employee CRUD operations, validation, and soft-delete functionality with transaction management.

**Design Specification:**
- Package: `com.company.warehouse.employee.service`
- Class: EmployeeService
- Methods: create, update, get, list, softDelete
- Validation: unique badgeId enforcement
- Transaction management via @Transactional

**Sample Implementation:**

```java
// EmployeeService.java
package com.company.warehouse.employee.service;

import com.company.warehouse.employee.domain.Employee;
import com.company.warehouse.employee.dto.EmployeeRequest;
import com.company.warehouse.employee.dto.EmployeeResponse;
import com.company.warehouse.employee.exception.DuplicateBadgeIdException;
import com.company.warehouse.employee.exception.EmployeeNotFoundException;
import com.company.warehouse.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        // Validate unique badgeId
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(request.getBadgeId())) {
            throw new DuplicateBadgeIdException(
                "Employee with badge ID " + request.getBadgeId() + " already exists");
        }

        Employee employee = new Employee();
        employee.setName(request.getName());
        employee.setBadgeId(request.getBadgeId());
        employee.setRole(request.getRole());
        employee.setDepartment(request.getDepartment());
        employee.setShiftGroup(request.getShiftGroup());
        employee.setHireDate(request.getHireDate());
        employee.setStatus(request.getStatus());

        Employee saved = employeeRepository.save(employee);
        return EmployeeResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        Employee employee = employeeRepository.findById(id)
            .filter(e -> !e.isDeleted())
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
        return EmployeeResponse.from(employee);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> list(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable)
            .map(EmployeeResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> search(String searchTerm, Pageable pageable) {
        return employeeRepository.searchEmployees(searchTerm, pageable)
            .map(EmployeeResponse::from);
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
            .filter(e -> !e.isDeleted())
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));

        // Check badgeId uniqueness if changed
        if (!employee.getBadgeId().equals(request.getBadgeId()) &&
            employeeRepository.existsByBadgeIdAndDeletedFalse(request.getBadgeId())) {
            throw new DuplicateBadgeIdException(
                "Employee with badge ID " + request.getBadgeId() + " already exists");
        }

        employee.setName(request.getName());
        employee.setBadgeId(request.getBadgeId());
        employee.setRole(request.getRole());
        employee.setDepartment(request.getDepartment());
        employee.setShiftGroup(request.getShiftGroup());
        employee.setHireDate(request.getHireDate());
        employee.setStatus(request.getStatus());

        Employee updated = employeeRepository.save(employee);
        return EmployeeResponse.from(updated);
    }

    @Transactional
    public void softDelete(Long id) {
        Employee employee = employeeRepository.findById(id)
            .filter(e -> !e.isDeleted())
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
        
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}
```

### Section: REST Controller

**Description:**
RESTful API endpoints for employee management with OpenAPI documentation, validation, and proper HTTP status codes.

**Design Specification:**
- Package: `com.company.warehouse.employee.controller`
- Class: EmployeeController
- Endpoints: POST /employees, GET /employees, GET /employees/{id}, PUT /employees/{id}, DELETE /employees/{id}
- OpenAPI annotations for API documentation
- Request/Response DTOs for data transfer

**Sample Implementation:**

```java
// EmployeeController.java
package com.company.warehouse.employee.controller;

import com.company.warehouse.employee.dto.EmployeeRequest;
import com.company.warehouse.employee.dto.EmployeeResponse;
import com.company.warehouse.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee Management", description = "APIs for managing warehouse employees")
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    @Operation(summary = "Create new employee", description = "Creates a new employee record")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all employees", description = "Retrieves paginated list of employees")
    public ResponseEntity<Page<EmployeeResponse>> listEmployees(Pageable pageable) {
        Page<EmployeeResponse> employees = employeeService.list(pageable);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/search")
    @Operation(summary = "Search employees", description = "Search employees by name or badge ID")
    public ResponseEntity<Page<EmployeeResponse>> searchEmployees(
            @RequestParam String q, Pageable pageable) {
        Page<EmployeeResponse> employees = employeeService.search(q, pageable);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID", description = "Retrieves a single employee by ID")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long id) {
        EmployeeResponse response = employeeService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee", description = "Updates an existing employee record")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete employee", description = "Soft-deletes an employee record")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Section: Data Transfer Objects

**Description:**
Request and response DTOs for API data transfer with validation annotations.

**Sample Implementation:**

```java
// EmployeeRequest.java
package com.company.warehouse.employee.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class EmployeeRequest {
    
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Badge ID is required")
    @Size(max = 50, message = "Badge ID must not exceed 50 characters")
    private String badgeId;

    @NotBlank(message = "Role is required")
    private String role;

    private String department;
    private String shiftGroup;

    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;

    @NotBlank(message = "Status is required")
    private String status;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getShiftGroup() { return shiftGroup; }
    public void setShiftGroup(String shiftGroup) { this.shiftGroup = shiftGroup; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

// EmployeeResponse.java
package com.company.warehouse.employee.dto;

import com.company.warehouse.employee.domain.Employee;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmployeeResponse {
    
    private Long id;
    private String name;
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EmployeeResponse from(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setName(employee.getName());
        response.setBadgeId(employee.getBadgeId());
        response.setRole(employee.getRole());
        response.setDepartment(employee.getDepartment());
        response.setShiftGroup(employee.getShiftGroup());
        response.setHireDate(employee.getHireDate());
        response.setStatus(employee.getStatus());
        response.setCreatedAt(employee.getCreatedAt());
        response.setUpdatedAt(employee.getUpdatedAt());
        return response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getShiftGroup() { return shiftGroup; }
    public void setShiftGroup(String shiftGroup) { this.shiftGroup = shiftGroup; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

---

## E03: Role Based Access Control (RBAC)

### Section: Security Configuration

**Description:**
Spring Security configuration implementing role-based access control with support for API key and OAuth2 authentication modes.

**Design Specification:**
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Authentication modes: API Key, OAuth2/JWT
- Method-level security with @PreAuthorize
- Row-level security for data isolation
- Configurable security mode via application properties

**Sample Implementation:**

```java
// SecurityConfig.java
package com.company.warehouse.security.config;

import com.company.warehouse.security.filter.ApiKeyAuthFilter;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${security.mode:oauth2}")
    private String securityMode;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/api/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
                .requestMatchers("/api/shifts/**").hasAnyRole("ADMIN", "SUPERVISOR")
                .anyRequest().authenticated()
            );

        if ("apikey".equalsIgnoreCase(securityMode)) {
            http.addFilterBefore(new ApiKeyAuthFilter(), 
                UsernamePasswordAuthenticationFilter.class);
        } else {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt());
        }

        return http.build();
    }
}
```

### Section: API Key Authentication Filter

**Description:**
Custom authentication filter for API key-based authentication.

**Sample Implementation:**

```java
// ApiKeyAuthFilter.java
package com.company.warehouse.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain)
            throws ServletException, IOException {
        
        String apiKey = request.getHeader(API_KEY_HEADER);
        
        if (apiKey != null && validateApiKey(apiKey)) {
            // Extract user and roles from API key (simplified)
            String username = extractUsername(apiKey);
            List<SimpleGrantedAuthority> authorities = extractAuthorities(apiKey);
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(username, null, authorities);
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }

    private boolean validateApiKey(String apiKey) {
        // Implement API key validation logic
        return true; // Simplified
    }

    private String extractUsername(String apiKey) {
        // Extract username from API key
        return "api-user"; // Simplified
    }

    private List<SimpleGrantedAuthority> extractAuthorities(String apiKey) {
        // Extract roles from API key
        return List.of(new SimpleGrantedAuthority("ROLE_ADMIN")); // Simplified
    }
}
```

### Section: Method-Level Security

**Description:**
Row-level security implementation using @PreAuthorize annotations for data isolation.

**Sample Implementation:**

```java
// SecuredEmployeeService.java
package com.company.warehouse.employee.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class SecuredEmployeeService {

    @PreAuthorize("hasRole('ADMIN')")
    public void adminOnlyOperation() {
        // Admin-only logic
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @teamService.isInTeam(#employeeId, authentication.name))")
    public Employee getEmployeeWithRowLevelSecurity(Long employeeId) {
        // Row-level security check
        return null; // Implementation
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public void hrOperation() {
        // HR operation
    }
}
```

---

## E04: Time & Attendance (Clock In/Out)

### Section: Domain Model

**Description:**
Attendance event entity capturing clock-in/out records with geofence validation and device tracking.

**Design Specification:**
- Package: `com.company.warehouse.attendance.domain`
- Entity: AttendanceEvent
- Fields: id, employeeId, eventType (IN/OUT), timestamp, deviceId, location, status
- Geofence validation support
- Correction workflow support

**Sample Implementation:**

```java
// AttendanceEvent.java
package com.company.warehouse.attendance.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_event")
public class AttendanceEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EventType eventType;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(length = 200)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AttendanceStatus status = AttendanceStatus.NORMAL;

    @Column(name = "correction_reason")
    private String correctionReason;

    @Column(name = "approved_by")
    private Long approvedBy;

    // Constructors, Getters, Setters
    public enum EventType {
        CLOCK_IN, CLOCK_OUT
    }

    public enum AttendanceStatus {
        NORMAL, CORRECTION_PENDING, APPROVED, REJECTED
    }

    // Getters and Setters omitted for brevity
}
```

### Section: Service Layer

**Description:**
Business logic for clock-in/out operations with geofence validation and hours calculation.

**Sample Implementation:**

```java
// AttendanceService.java
package com.company.warehouse.attendance.service;

import com.company.warehouse.attendance.domain.AttendanceEvent;
import com.company.warehouse.attendance.dto.ClockInRequest;
import com.company.warehouse.attendance.dto.ClockOutRequest;
import com.company.warehouse.attendance.repository.AttendanceEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceService {
    
    @Autowired
    private AttendanceEventRepository attendanceRepository;

    @Autowired
    private GeofenceService geofenceService;

    @Transactional
    public AttendanceEvent clockIn(ClockInRequest request) {
        // Validate geofence
        if (!geofenceService.isWithinGeofence(request.getLocation())) {
            throw new GeofenceViolationException("Location is outside allowed geofence");
        }

        // Check for existing clock-in without clock-out
        List<AttendanceEvent> openEvents = attendanceRepository
            .findOpenClockInEvents(request.getEmployeeId());
        
        if (!openEvents.isEmpty()) {
            throw new IllegalStateException("Employee already clocked in");
        }

        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(request.getEmployeeId());
        event.setEventType(AttendanceEvent.EventType.CLOCK_IN);
        event.setTimestamp(LocalDateTime.now());
        event.setDeviceId(request.getDeviceId());
        event.setLocation(request.getLocation());
        event.setStatus(AttendanceEvent.AttendanceStatus.NORMAL);

        return attendanceRepository.save(event);
    }

    @Transactional
    public AttendanceEvent clockOut(ClockOutRequest request) {
        // Validate geofence
        if (!geofenceService.isWithinGeofence(request.getLocation())) {
            throw new GeofenceViolationException("Location is outside allowed geofence");
        }

        // Find matching clock-in
        List<AttendanceEvent> openEvents = attendanceRepository
            .findOpenClockInEvents(request.getEmployeeId());
        
        if (openEvents.isEmpty()) {
            throw new IllegalStateException("No open clock-in found for employee");
        }

        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(request.getEmployeeId());
        event.setEventType(AttendanceEvent.EventType.CLOCK_OUT);
        event.setTimestamp(LocalDateTime.now());
        event.setDeviceId(request.getDeviceId());
        event.setLocation(request.getLocation());
        event.setStatus(AttendanceEvent.AttendanceStatus.NORMAL);

        return attendanceRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Duration calculateHoursWorked(Long employeeId, LocalDate date) {
        List<AttendanceEvent> events = attendanceRepository
            .findByEmployeeIdAndDate(employeeId, date);
        
        Duration totalHours = Duration.ZERO;
        AttendanceEvent clockIn = null;

        for (AttendanceEvent event : events) {
            if (event.getEventType() == AttendanceEvent.EventType.CLOCK_IN) {
                clockIn = event;
            } else if (event.getEventType() == AttendanceEvent.EventType.CLOCK_OUT && clockIn != null) {
                Duration shift = Duration.between(clockIn.getTimestamp(), event.getTimestamp());
                totalHours = totalHours.plus(shift);
                clockIn = null;
            }
        }

        return totalHours;
    }

    @Transactional
    public AttendanceEvent requestCorrection(Long eventId, String reason) {
        AttendanceEvent event = attendanceRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        
        event.setStatus(AttendanceEvent.AttendanceStatus.CORRECTION_PENDING);
        event.setCorrectionReason(reason);
        
        return attendanceRepository.save(event);
    }
}
```

---

## E05: Shift & Schedule Management

### Section: Domain Model

**Description:**
Shift template and assignment entities for managing recurring schedules and employee assignments.

**Sample Implementation:**

```java
// ShiftTemplate.java
package com.company.warehouse.shift.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "shift_template")
public class ShiftTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_pattern", length = 20)
    private RecurrencePattern recurrencePattern;

    @Column(name = "overtime_rule")
    private String overtimeRule;

    @ElementCollection
    @CollectionTable(name = "shift_blackout_dates", 
                     joinColumns = @JoinColumn(name = "shift_template_id"))
    @Column(name = "blackout_date")
    private List<LocalDate> blackoutDates;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    public enum RecurrencePattern {
        DAILY, WEEKLY, BIWEEKLY, MONTHLY
    }

    // Getters and Setters
}

// ShiftAssignment.java
@Entity
@Table(name = "shift_assignment")
public class ShiftAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "shift_template_id", nullable = false)
    private Long shiftTemplateId;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status;

    public enum AssignmentStatus {
        SCHEDULED, COMPLETED, MISSED, CANCELLED
    }

    // Getters and Setters
}
```

---

## E06: Leave & Absence Management

### Section: Domain Model

**Description:**
Leave request and balance tracking entities with approval workflow support.

**Sample Implementation:**

```java
// LeaveRequest.java
package com.company.warehouse.leave.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "leave_request")
public class LeaveRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 20)
    private LeaveType leaveType;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaveStatus status = LeaveStatus.PENDING;

    @Column(length = 500)
    private String reason;

    @Column(name = "approved_by")
    private Long approvedBy;

    public enum LeaveType {
        PTO, SICK, UNPAID, BEREAVEMENT, JURY_DUTY
    }

    public enum LeaveStatus {
        PENDING, APPROVED, REJECTED, CANCELLED
    }

    // Getters and Setters
}

// LeaveBalance.java
@Entity
@Table(name = "leave_balance")
public class LeaveBalance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 20)
    private LeaveRequest.LeaveType leaveType;

    @Column(nullable = false)
    private Double balance;

    @Column(name = "accrual_rate")
    private Double accrualRate;

    // Getters and Setters
}
```

---

## E07: Training & Certification Tracking

### Section: Domain Model

**Description:**
Certification tracking with expiration alerts and assignment validation.

**Sample Implementation:**

```java
// Certification.java
package com.company.warehouse.certification.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "certification")
public class Certification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "certification_type", nullable = false, length = 50)
    private CertificationType certificationType;

    @NotNull
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @NotNull
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "proof_document_url", length = 500)
    private String proofDocumentUrl;

    @Column(name = "issuing_authority", length = 200)
    private String issuingAuthority;

    public enum CertificationType {
        FORKLIFT, REACH_TRUCK, ORDER_PICKER, 
        HAZMAT, FIRST_AID, SAFETY_TRAINING
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public boolean isExpiringSoon(int days) {
        return LocalDate.now().plusDays(days).isAfter(expiryDate);
    }

    // Getters and Setters
}
```

---

## E08: Safety Incidents & OSHA Reporting

### Section: Domain Model

**Description:**
Safety incident tracking with investigation workflow and OSHA reporting support.

**Sample Implementation:**

```java
// SafetyIncident.java
package com.company.warehouse.safety.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "safety_incident")
public class SafetyIncident {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 200)
    private String location;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IncidentStatus status = IncidentStatus.OPEN;

    @ElementCollection
    @CollectionTable(name = "incident_involved_employees",
                     joinColumns = @JoinColumn(name = "incident_id"))
    @Column(name = "employee_id")
    private List<Long> involvedEmployeeIds;

    @ElementCollection
    @CollectionTable(name = "incident_corrective_actions",
                     joinColumns = @JoinColumn(name = "incident_id"))
    @Column(name = "action", length = 500)
    private List<String> correctiveActions;

    @Column(name = "reported_by")
    private Long reportedBy;

    @Column(name = "investigated_by")
    private Long investigatedBy;

    public enum Severity {
        MINOR, MODERATE, SERIOUS, CRITICAL, FATAL
    }

    public enum IncidentStatus {
        OPEN, INVESTIGATING, RESOLVED, CLOSED
    }

    // Getters and Setters
}
```

---

## E09: Equipment & Asset Assignment

### Section: Domain Model

**Description:**
Asset tracking with checkout/return workflow and certification validation.

**Sample Implementation:**

```java
// Asset.java
package com.company.warehouse.asset.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Entity
@Table(name = "asset")
public class Asset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 50)
    private AssetType assetType;

    @NotBlank
    @Column(name = "serial_number", unique = true, nullable = false, length = 100)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetCondition condition = AssetCondition.GOOD;

    @Column(name = "assigned_employee_id")
    private Long assignedEmployeeId;

    @Column(name = "checkout_date")
    private LocalDate checkoutDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "required_certification")
    private String requiredCertification;

    public enum AssetType {
        SCANNER, FORKLIFT, REACH_TRUCK, 
        ORDER_PICKER, PPE, RADIO
    }

    public enum AssetCondition {
        EXCELLENT, GOOD, FAIR, POOR, OUT_OF_SERVICE
    }

    public boolean isAvailable() {
        return assignedEmployeeId == null;
    }

    // Getters and Setters
}
```

---

## E10: Performance Reviews & Goals

### Section: Domain Model

**Description:**
Performance review tracking with goals, competencies, and acknowledgement workflow.

**Sample Implementation:**

```java
// PerformanceReview.java
package com.company.warehouse.performance.domain;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "performance_review")
public class PerformanceReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(nullable = false, length = 50)
    private String cycle;

    @ElementCollection
    @CollectionTable(name = "review_goals",
                     joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "goal", length = 500)
    private List<String> goals;

    @ElementCollection
    @CollectionTable(name = "review_competencies",
                     joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "competency", length = 200)
    private List<String> competencies;

    @ElementCollection
    @CollectionTable(name = "review_ratings",
                     joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "rating")
    private List<Integer> ratings;

    @Column(length = 2000)
    private String comments;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewStatus status = ReviewStatus.DRAFT;

    @Column(name = "supervisor_id")
    private Long supervisorId;

    @Column(nullable = false)
    private boolean acknowledged = false;

    public enum ReviewStatus {
        DRAFT, SUBMITTED, ACKNOWLEDGED, FINALIZED
    }

    // Getters and Setters
}
```

---

## E11: Payroll Export Integration

### Section: Domain Model

**Description:**
Payroll export tracking with delivery status and audit trail.

**Sample Implementation:**

```java
// PayrollExport.java
package com.company.warehouse.payroll.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_export")
public class PayrollExport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExportStatus status = ExportStatus.PENDING;

    @Column(name = "delivery_method", length = 20)
    private String deliveryMethod;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    public enum ExportStatus {
        PENDING, GENERATED, DELIVERED, FAILED
    }

    // Getters and Setters
}
```

---

## E12: Notifications & Announcements

### Section: Domain Model

**Description:**
Notification and announcement entities with multi-channel delivery support.

**Sample Implementation:**

```java
// Notification.java
package com.company.warehouse.notification.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    @Column(nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    public enum NotificationType {
        SHIFT_CHANGE, CERT_EXPIRING, APPROVAL_REQUIRED, 
        ANNOUNCEMENT, SAFETY_ALERT
    }

    public enum NotificationChannel {
        IN_APP, EMAIL, SMS
    }

    public enum NotificationStatus {
        PENDING, SENT, FAILED, READ
    }

    // Getters and Setters
}

// Announcement.java
@Entity
@Table(name = "announcement")
public class Announcement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(length = 100)
    private String audience;

    // Getters and Setters
}
```

---

## E13: Integration Layer (HRIS/WMS APIs)

### Section: Domain Model

**Description:**
Integration event tracking for external system synchronization.

**Sample Implementation:**

```java
// IntegrationEvent.java
package com.company.warehouse.integration.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "integration_event")
public class IntegrationEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private IntegrationEventType eventType;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IntegrationStatus status = IntegrationStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    public enum IntegrationEventType {
        HRIS_EMPLOYEE_SYNC, WMS_LOCATION_SYNC, 
        WEBHOOK_DELIVERY, SSO_LOGIN
    }

    public enum IntegrationStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }

    // Getters and Setters
}
```

---

## E14: Audit Trail & Compliance

### Section: Domain Model

**Description:**
Immutable audit log for compliance and forensic analysis.

**Sample Implementation:**

```java
// AuditLog.java
package com.company.warehouse.audit.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 20)
    private String action;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Lob
    @Column(name = "before_state")
    private String beforeState;

    @Lob
    @Column(name = "after_state")
    private String afterState;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    // Getters only (immutable)
    public Long getId() { return id; }
    public String getEntityName() { return entityName; }
    public Long getEntityId() { return entityId; }
    public String getAction() { return action; }
    public Long getActorId() { return actorId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getBeforeState() { return beforeState; }
    public String getAfterState() { return afterState; }
    public String getIpAddress() { return ipAddress; }
}
```

---

## E15: Reporting & Analytics

### Section: Domain Model

**Description:**
Report generation and storage with export capabilities.

**Sample Implementation:**

```java
// Report.java
package com.company.warehouse.reporting.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report")
public class Report {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private ReportType reportType;

    @Lob
    @Column(nullable = false)
    private String parameters;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "generated_by")
    private Long generatedBy;

    public enum ReportType {
        ATTENDANCE, OVERTIME, LEAVE_BALANCE, 
        CERTIFICATION_STATUS, SAFETY_KPI, 
        PAYROLL_SUMMARY
    }

    // Getters and Setters
}
```

---

## E16: Mobile Access (PWA)

### Section: Configuration

**Description:**
PWA manifest and service worker configuration for offline support.

**Sample Implementation:**

```json
// manifest.json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "description": "Warehouse Employee Management System",
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

## E17: Onboarding & Offboarding Workflow

### Section: Domain Model

**Description:**
Task tracking for employee lifecycle management.

**Sample Implementation:**

```java
// OnboardingTask.java
package com.company.warehouse.lifecycle.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "onboarding_task")
public class OnboardingTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 50)
    private OnboardingTaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "completed_by")
    private Long completedBy;

    public enum OnboardingTaskType {
        ACCOUNT_CREATION, INITIAL_SCHEDULE, 
        TRAINING_ASSIGNMENT, ASSET_ASSIGNMENT, 
        ORIENTATION
    }

    public enum TaskStatus {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }

    // Getters and Setters
}
```

---

## E18: User Preferences & Localization

### Section: Domain Model

**Description:**
User preference storage for personalization and localization.

**Sample Implementation:**

```java
// UserPreference.java
package com.company.warehouse.preferences.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "user_preference")
public class UserPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", unique = true, nullable = false)
    private Long employeeId;

    @Column(length = 10)
    private String language = "en";

    @Column(length = 50)
    private String timezone = "UTC";

    @Column(name = "notification_settings", length = 500)
    private String notificationSettings;

    @Column(name = "date_format", length = 20)
    private String dateFormat = "MM/dd/yyyy";

    // Getters and Setters
}
```

---

## E19: Advanced Scheduling (AI/ML)

### Section: Domain Model

**Description:**
ML-based scheduling predictions and anomaly detection.

**Sample Implementation:**

```java
// SchedulingPrediction.java
package com.company.warehouse.scheduling.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "scheduling_prediction")
public class SchedulingPrediction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "predicted_staffing")
    private Integer predictedStaffing;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @ElementCollection
    @CollectionTable(name = "prediction_anomalies",
                     joinColumns = @JoinColumn(name = "prediction_id"))
    @Column(name = "anomaly", length = 500)
    private List<String> anomalies;

    // Getters and Setters
}
```

---

## E20: DevOps & Deployment

### Section: Docker Configuration

**Description:**
Containerization configuration for deployment.

**Sample Implementation:**

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY target/warehouse-ems-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Section: Kubernetes Deployment

**Sample Implementation:**

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: warehouse-ems
  labels:
    app: warehouse-ems
spec:
  replicas: 3
  selector:
    matchLabels:
      app: warehouse-ems
  template:
    metadata:
      labels:
        app: warehouse-ems
    spec:
      containers:
      - name: warehouse-ems
        image: warehouse-ems:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: warehouse-secrets
              key: db-url
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: warehouse-secrets
              key: db-password
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
```

### Section: CI/CD Pipeline

**Sample Implementation:**

```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline

on:
  push:
    branches: [ main ]
  pull_request:
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
        distribution: 'adopt'
    
    - name: Build with Maven
      run: mvn clean install
    
    - name: Run tests
      run: mvn test
    
    - name: Build Docker image
      run: docker build -t warehouse-ems:${{ github.sha }} .
    
    - name: Push to registry
      run: |
        echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
        docker push warehouse-ems:${{ github.sha }}
```

---

## CONCLUSION

This comprehensive low-level technical design document provides detailed specifications for implementing the Warehouse Employee Management System using Spring Boot best practices. Each epic includes:

- **Domain Models**: JPA entities with proper annotations and relationships
- **Repository Layer**: Spring Data JPA repositories with custom queries
- **Service Layer**: Business logic with transaction management
- **Controller Layer**: RESTful APIs with validation and documentation
- **Security**: Role-based access control and authentication
- **Configuration**: Application settings and deployment specifications

The design follows industry standards including:
- Clean architecture with separation of concerns
- Dependency injection via Spring
- Transaction management
- Validation and error handling
- API documentation with OpenAPI
- Security best practices
- Containerization and orchestration
- CI/CD automation

Developers can use this document as a blueprint for implementing the complete system with confidence in architectural soundness and maintainability.
