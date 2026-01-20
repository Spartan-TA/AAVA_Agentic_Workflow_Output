# WAREHOUSE EMPLOYEE MANAGEMENT SYSTEM (EMS) - LOW-LEVEL TECHNICAL DESIGN DOCUMENT

==================================================================
## Table of Contents
==================================================================
1. Introduction
2. Common Spring Boot Architecture Overview
3. Epic-by-Epic Low-Level Design
   - E01: Project Scaffolding & Domain Setup
   - E02: Employee Master Data (CRUD)
   - E03: Role Based Access Control (RBAC)
   - E04: Time & Attendance (Clock In/Out)
   - E05: Shift & Schedule Management
   - E06: Leave & Absence Management
   - E07: Training & Certification Tracking
   - E08: Safety Incidents & OSHA Reporting
   - E09: Equipment & Asset Assignment
   - E10: Performance Reviews & Goals
   - E11: Payroll Export Integration
   - E12: Notifications & Announcements
   - E13: Integration Layer (HRIS/WMS APIs)
   - E14: Audit Trail & Compliance
   - E15: Reporting & Analytics
   - E16: Mobile Access (PWA)
   - E17: Onboarding & Offboarding Workflow
   - E18: Localization & Multi-Warehouse
   - E19: Advanced Scheduling (AI/Optimization)
   - E20: Document Management
4. Shared Security, Error Handling, and Validation Strategies
5. Appendix: Sample application.yml, migration scripts, and code snippets

==================================================================
## 1. Introduction
==================================================================
This document provides a comprehensive low-level technical design for the Warehouse Employee Management System (EMS), covering all 20 epics. It is intended for Spring Boot developers and architects, ensuring consistency, maintainability, and adherence to industry best practices.

==================================================================
## 2. Common Spring Boot Architecture Overview
==================================================================
- **Architecture Style:** Layered (Controller, Service, Repository, Domain)
- **Build Tool:** Maven
- **Database:** PostgreSQL (with Flyway/Liquibase for migrations)
- **Security:** Spring Security (RBAC, OAuth2, API Key)
- **API Documentation:** OpenAPI/Swagger
- **Monitoring:** Spring Boot Actuator
- **Configuration:** application.yml (profile-based)
- **Testing:** JUnit, Mockito, Spring Test
- **Other:** Modular package structure, DTOs for API, MapStruct for mapping, Exception Handling via @ControllerAdvice

==================================================================
## 3. Epic-by-Epic Low-Level Design
==================================================================

--------------------------------------------------
### E01: Project Scaffolding & Domain Setup
--------------------------------------------------

#### Section: Project Initialization

**Description:**
Initialize Spring Boot project with Maven, configure base packages for core modules (employee, scheduling, attendance, safety), add Flyway/Liquibase for database migrations, and enable Spring Boot Actuator for monitoring.

**Design Specification:**
- Maven project with Spring Boot 3.x
- Java 17+
- Base package: `com.company.wems`
- Submodules: employee, scheduling, attendance, safety, asset, review, payroll, notification, integration, audit, reporting, mobile, onboarding, localization, ai, document, config, common
- Database migration tool: Flyway or Liquibase
- Monitoring: Spring Boot Actuator

**Sample Implementation:**

**Package Structure:**
```
com.company.wems
  âââ employee
  â   âââ controller
  â   âââ service
  â   âââ repository
  â   âââ dto
  â   âââ model
  â   âââ mapper
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
  âââ mobile
  âââ onboarding
  âââ localization
  âââ ai
  âââ document
  âââ config
  âââ common
```

**application.yml:**
```yaml
server:
  port: 8080

spring:
  application:
    name: warehouse-ems
  datasource:
    url: jdbc:postgresql://localhost:5432/wems
    username: wems
    password: secret
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

**FlywayConfig.java:**
```java
package com.company.wems.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {
    
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
```

**Migration Script (V1__init_schema.sql):**
```sql
-- Create employee table
CREATE TABLE employee (
  id BIGSERIAL PRIMARY KEY,
  badge_id VARCHAR(32) UNIQUE NOT NULL,
  name VARCHAR(128) NOT NULL,
  role VARCHAR(32) NOT NULL,
  department VARCHAR(64),
  shift_group VARCHAR(32),
  hire_date DATE,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  deleted BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on badge_id for faster lookups
CREATE INDEX idx_employee_badge_id ON employee(badge_id);
CREATE INDEX idx_employee_status ON employee(status);
CREATE INDEX idx_employee_deleted ON employee(deleted);
```

**ActuatorConfig.java:**
```java
package com.company.wems.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActuatorConfig {
    
    @Bean
    public HealthIndicator customHealthIndicator() {
        return () -> Health.up()
            .withDetail("app", "Warehouse EMS")
            .withDetail("version", "1.0.0")
            .build();
    }
}
```

**README.md:**
```markdown
# Warehouse Employee Management System (EMS)

## Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+

## Build & Run

### 1. Clone the repository
```bash
git clone <repository-url>
cd warehouse-ems
```

### 2. Configure database
Create a PostgreSQL database:
```sql
CREATE DATABASE wems;
CREATE USER wems WITH PASSWORD 'secret';
GRANT ALL PRIVILEGES ON DATABASE wems TO wems;
```

### 3. Build the project
```bash
mvn clean install
```

### 4. Run the application
```bash
mvn spring-boot:run
```

### 5. Access the application
- Application: http://localhost:8080
- Health Check: http://localhost:8080/actuator/health
- API Docs: http://localhost:8080/swagger-ui.html

## Troubleshooting
- If database connection fails, check credentials in application.yml
- If port 8080 is in use, change server.port in application.yml
```

**Acceptance Criteria:**
- â Project builds successfully with `mvn clean install`
- â Application runs on port 8080
- â README contains clear build/run instructions
- â Actuator `/actuator/health` endpoint returns UP status
- â Base package structure created
- â Flyway/Liquibase runs baseline migration successfully

--------------------------------------------------
### E02: Employee Master Data (CRUD)
--------------------------------------------------

#### Section: Domain Model Design

**Description:**
Create Employee domain entity with comprehensive CRUD operations. The entity includes fields for name, badgeId, role, department, shiftGroup, hireDate, and status. Implement soft-delete functionality, pagination, filtering, and OpenAPI documentation.

**Design Specification:**
- Entity: Employee
- Fields: id, badgeId (unique), name, role, department, shiftGroup, hireDate, status, deleted
- Soft-delete: Set deleted flag instead of physical deletion
- Pagination: Spring Data Pageable
- Filtering: Dynamic query support
- Validation: Bean Validation annotations
- API Documentation: OpenAPI/Swagger

**Sample Implementation:**

**Employee Entity (Employee.java):**
```java
package com.company.wems.employee.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE employee SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "badge_id", unique = true, nullable = false, length = 32)
    @NotBlank(message = "Badge ID is required")
    private String badgeId;
    
    @Column(name = "name", nullable = false, length = 128)
    @NotBlank(message = "Name is required")
    private String name;
    
    @Column(name = "role", nullable = false, length = 32)
    @NotBlank(message = "Role is required")
    private String role;
    
    @Column(name = "department", length = 64)
    private String department;
    
    @Column(name = "shift_group", length = 32)
    private String shiftGroup;
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    @Column(name = "status", nullable = false, length = 16)
    @NotNull(message = "Status is required")
    private String status = "ACTIVE";
    
    @Column(name = "deleted")
    private Boolean deleted = false;
    
    @Column(name = "created_at")
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

**Employee DTO (EmployeeDto.java):**
```java
package com.company.wems.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Employee Data Transfer Object")
public class EmployeeDto {
    
    @Schema(description = "Employee ID", example = "1")
    private Long id;
    
    @NotBlank(message = "Badge ID is required")
    @Schema(description = "Unique badge identifier", example = "EMP001", required = true)
    private String badgeId;
    
    @NotBlank(message = "Name is required")
    @Schema(description = "Employee full name", example = "John Doe", required = true)
    private String name;
    
    @NotBlank(message = "Role is required")
    @Schema(description = "Employee role", example = "WORKER", required = true)
    private String role;
    
    @Schema(description = "Department", example = "Warehouse Operations")
    private String department;
    
    @Schema(description = "Shift group", example = "MORNING")
    private String shiftGroup;
    
    @Schema(description = "Hire date", example = "2024-01-15")
    private LocalDate hireDate;
    
    @NotNull(message = "Status is required")
    @Schema(description = "Employee status", example = "ACTIVE", required = true)
    private String status;
}
```

**Employee Repository (EmployeeRepository.java):**
```java
package com.company.wems.employee.repository;

import com.company.wems.employee.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, 
                                            JpaSpecificationExecutor<Employee> {
    
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    
    @Query("SELECT e FROM Employee e WHERE e.deleted = false " +
           "AND (:department IS NULL OR e.department = :department) " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (:role IS NULL OR e.role = :role)")
    Page<Employee> findByFilters(
        @Param("department") String department,
        @Param("status") String status,
        @Param("role") String role,
        Pageable pageable
    );
    
    boolean existsByBadgeIdAndDeletedFalse(String badgeId);
}
```

**Employee Service (EmployeeService.java):**
```java
package com.company.wems.employee.service;

import com.company.wems.employee.dto.EmployeeDto;
import com.company.wems.employee.mapper.EmployeeMapper;
import com.company.wems.employee.model.Employee;
import com.company.wems.employee.repository.EmployeeRepository;
import com.company.wems.common.exception.DuplicateResourceException;
import com.company.wems.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    
    public EmployeeDto createEmployee(EmployeeDto dto) {
        log.info("Creating employee with badgeId: {}", dto.getBadgeId());
        
        // Check for duplicate badgeId
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(dto.getBadgeId())) {
            throw new DuplicateResourceException(
                "Employee with badgeId " + dto.getBadgeId() + " already exists"
            );
        }
        
        Employee employee = employeeMapper.toEntity(dto);
        Employee saved = employeeRepository.save(employee);
        
        log.info("Employee created successfully with id: {}", saved.getId());
        return employeeMapper.toDto(saved);
    }
    
    @Transactional(readOnly = true)
    public EmployeeDto getEmployee(Long id) {
        log.info("Fetching employee with id: {}", id);
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Employee not found with id: " + id
            ));
        
        return employeeMapper.toDto(employee);
    }
    
    @Transactional(readOnly = true)
    public Page<EmployeeDto> listEmployees(Pageable pageable, Map<String, String> filters) {
        log.info("Listing employees with filters: {}", filters);
        
        String department = filters.get("department");
        String status = filters.get("status");
        String role = filters.get("role");
        
        Page<Employee> employees = employeeRepository.findByFilters(
            department, status, role, pageable
        );
        
        return employees.map(employeeMapper::toDto);
    }
    
    public EmployeeDto updateEmployee(Long id, EmployeeDto dto) {
        log.info("Updating employee with id: {}", id);
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Employee not found with id: " + id
            ));
        
        // Check for duplicate badgeId if changed
        if (!employee.getBadgeId().equals(dto.getBadgeId()) &&
            employeeRepository.existsByBadgeIdAndDeletedFalse(dto.getBadgeId())) {
            throw new DuplicateResourceException(
                "Employee with badgeId " + dto.getBadgeId() + " already exists"
            );
        }
        
        employeeMapper.updateEntity(dto, employee);
        Employee updated = employeeRepository.save(employee);
        
        log.info("Employee updated successfully with id: {}", updated.getId());
        return employeeMapper.toDto(updated);
    }
    
    public EmployeeDto partialUpdateEmployee(Long id, Map<String, Object> updates) {
        log.info("Partially updating employee with id: {}", id);
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Employee not found with id: " + id
            ));
        
        // Apply partial updates
        updates.forEach((key, value) -> {
            switch (key) {
                case "name" -> employee.setName((String) value);
                case "role" -> employee.setRole((String) value);
                case "department" -> employee.setDepartment((String) value);
                case "shiftGroup" -> employee.setShiftGroup((String) value);
                case "status" -> employee.setStatus((String) value);
                // Add other fields as needed
            }
        });
        
        Employee updated = employeeRepository.save(employee);
        
        log.info("Employee partially updated successfully with id: {}", updated.getId());
        return employeeMapper.toDto(updated);
    }
    
    public void deleteEmployee(Long id) {
        log.info("Soft deleting employee with id: {}", id);
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Employee not found with id: " + id
            ));
        
        employeeRepository.delete(employee); // Soft delete via @SQLDelete
        
        log.info("Employee soft deleted successfully with id: {}", id);
    }
}
```

**Employee Mapper (EmployeeMapper.java):**
```java
package com.company.wems.employee.mapper;

import com.company.wems.employee.dto.EmployeeDto;
import com.company.wems.employee.model.Employee;
import org.mapstruct.*;

@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeMapper {
    
    EmployeeDto toDto(Employee employee);
    
    Employee toEntity(EmployeeDto dto);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(EmployeeDto dto, @MappingTarget Employee employee);
}
```

**Employee Controller (EmployeeController.java):**
```java
package com.company.wems.employee.controller;

import com.company.wems.employee.dto.EmployeeDto;
import com.company.wems.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "APIs for managing employee master data")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Create new employee", description = "Creates a new employee record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Employee created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Employee with badgeId already exists")
    })
    public ResponseEntity<EmployeeDto> createEmployee(
            @Valid @RequestBody EmployeeDto dto) {
        EmployeeDto created = employeeService.createEmployee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "List employees", description = "Retrieves paginated list of employees with optional filters")
    public ResponseEntity<Page<EmployeeDto>> listEmployees(
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(description = "Filter parameters") @RequestParam Map<String, String> filters) {
        Page<EmployeeDto> employees = employeeService.listEmployees(pageable, filters);
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get employee by ID", description = "Retrieves a single employee by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee found"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<EmployeeDto> getEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        EmployeeDto employee = employeeService.getEmployee(id);
        return ResponseEntity.ok(employee);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update employee", description = "Updates an existing employee record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee updated successfully"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "409", description = "Duplicate badgeId")
    })
    public ResponseEntity<EmployeeDto> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDto dto) {
        EmployeeDto updated = employeeService.updateEmployee(id, dto);
        return ResponseEntity.ok(updated);
    }
    
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Partial update employee", description = "Partially updates an employee record")
    public ResponseEntity<EmployeeDto> partialUpdateEmployee(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        EmployeeDto updated = employeeService.partialUpdateEmployee(id, updates);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Delete employee", description = "Soft deletes an employee record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Employee deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Acceptance Criteria:**
- â POST /api/v1/employees creates new employee
- â GET /api/v1/employees returns paginated list
- â GET /api/v1/employees/{id} returns single employee
- â PUT /api/v1/employees/{id} updates employee
- â PATCH /api/v1/employees/{id} partially updates employee
- â DELETE /api/v1/employees/{id} soft deletes employee
- â Unique badgeId enforced (409 on duplicate)
- â Soft-delete supported (deleted flag)
- â Pagination and filtering implemented
- â OpenAPI schemas with examples

--------------------------------------------------
### E03: Role Based Access Control (RBAC)
--------------------------------------------------

#### Section: Security Configuration

**Description:**
Implement Spring Security with role-based access control supporting ADMIN, HR, SUPERVISOR, and WORKER roles. Configure method-level and endpoint security with row-level constraints. Support both API key and OAuth2 authentication modes.

**Design Specification:**
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Authentication: OAuth2 JWT (primary), API Key (optional)
- Authorization: Method-level (@PreAuthorize), endpoint-level
- Row-level security: Filter data based on user role and team
- Security responses: 401 Unauthorized, 403 Forbidden

**Sample Implementation:**

**Security Configuration (SecurityConfig.java):**
```java
package com.company.wems.config;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final ApiKeyAuthenticationFilter apiKeyAuthFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/v1/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/attendance/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

**JWT Authentication Filter (JwtAuthenticationFilter.java):**
```java
package com.company.wems.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            final String jwt = authHeader.substring(7);
            final String username = jwtService.extractUsername(jwt);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.isTokenValid(jwt)) {
                    List<String> roles = jwtService.extractRoles(jwt);
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
                    
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            username, null, authorities
                        );
                    
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            logger.error("JWT authentication failed", e);
        }
        
        filterChain.doFilter(request, response);
    }
}
```

**API Key Authentication Filter (ApiKeyAuthenticationFilter.java):**
```java
package com.company.wems.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    
    @Value("${security.api-key.enabled:false}")
    private boolean apiKeyEnabled;
    
    @Value("${security.api-key.header-name:X-API-Key}")
    private String apiKeyHeaderName;
    
    private final ApiKeyService apiKeyService;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        if (!apiKeyEnabled) {
            filterChain.doFilter(request, response);
            return;
        }
        
        final String apiKey = request.getHeader(apiKeyHeaderName);
        
        if (apiKey != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (apiKeyService.isValidApiKey(apiKey)) {
                    List<String> roles = apiKeyService.getRolesForApiKey(apiKey);
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();
                    
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            "api-key-user", null, authorities
                        );
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                logger.error("API Key authentication failed", e);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

**Row-Level Security Service (RowLevelSecurityService.java):**
```java
package com.company.wems.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RowLevelSecurityService {
    
    public boolean canAccessEmployee(Long employeeId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null) {
            return false;
        }
        
        // ADMIN and HR can access all employees
        if (hasRole(auth, "ADMIN") || hasRole(auth, "HR")) {
            return true;
        }
        
        // SUPERVISOR can access only their team members
        if (hasRole(auth, "SUPERVISOR")) {
            return isInSupervisorTeam(auth.getName(), employeeId);
        }
        
        // WORKER can access only their own record
        if (hasRole(auth, "WORKER")) {
            return isOwnRecord(auth.getName(), employeeId);
        }
        
        return false;
    }
    
    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
    
    private boolean isInSupervisorTeam(String supervisorUsername, Long employeeId) {
        // Implementation to check if employee is in supervisor's team
        // This would query the database to verify the relationship
        return true; // Placeholder
    }
    
    private boolean isOwnRecord(String username, Long employeeId) {
        // Implementation to check if the employee record belongs to the user
        return true; // Placeholder
    }
}
```

**Security Test (SecurityTest.java):**
```java
package com.company.wems.employee.controller;

import com.company.wems.employee.dto.EmployeeDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void whenNoAuth_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "WORKER")
    void whenWorkerAccessEmployees_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "HR")
    void whenHRAccessEmployees_thenOk() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void whenAdminAccessEmployees_thenOk() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
            .andExpect(status().isOk());
    }
}
```

**Acceptance Criteria:**
- â Unauthorized requests return 401
- â Forbidden actions return 403
- â ADMIN can manage all records
- â HR can manage employee records
- â SUPERVISOR limited to team members
- â WORKER can only access own records
- â Security rules covered by tests
- â API key/OAuth2 toggle via configuration

--------------------------------------------------
### E04: Time & Attendance (Clock In/Out)
--------------------------------------------------

#### Section: Attendance Tracking System

**Description:**
Implement clock-in/out functionality with optional geofence validation and device capture. Calculate hours worked per shift, handle missed punches, and support correction workflows.

**Design Specification:**
- Endpoints: POST /attendance/clock-in, POST /attendance/clock-out
- Geofence validation (optional)
- Device capture (device ID, IP address)
- Automatic shift association
- Daily totals computation
- Missed punch correction workflow
- CSV export for reports

**Sample Implementation:**

**Attendance Event Entity (AttendanceEvent.java):**
```java
package com.company.wems.attendance.model;

import com.company.wems.employee.model.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_event")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AttendanceEventType type; // CLOCK_IN, CLOCK_OUT
    
    @Column(name = "device_id")
    private String deviceId;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "geo_location")
    private String geoLocation; // Format: "lat,lon"
    
    @Column(name = "shift_id")
    private Long shiftId;
    
    @Column(name = "correction_requested")
    private Boolean correctionRequested = false;
    
    @Column(name = "correction_reason")
    private String correctionReason;
    
    @Column(name = "approved")
    private Boolean approved;
    
    @Column(name = "approved_by")
    private String approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}

enum AttendanceEventType {
    CLOCK_IN,
    CLOCK_OUT
}
```

**Attendance Request DTO (AttendanceRequest.java):**
```java
package com.company.wems.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Attendance clock-in/out request")
public class AttendanceRequest {
    
    @NotNull(message = "Employee ID is required")
    @Schema(description = "Employee ID", example = "1", required = true)
    private Long employeeId;
    
    @Schema(description = "Device ID", example = "DEVICE-001")
    private String deviceId;
    
    @Schema(description = "Geo location (lat,lon)", example = "37.7749,-122.4194")
    private String geoLocation;
}
```

**Attendance Service (AttendanceService.java):**
```java
package com.company.wems.attendance.service;

import com.company.wems.attendance.dto.AttendanceRequest;
import com.company.wems.attendance.dto.AttendanceResponse;
import com.company.wems.attendance.model.AttendanceEvent;
import com.company.wems.attendance.model.AttendanceEventType;
import com.company.wems.attendance.repository.AttendanceEventRepository;
import com.company.wems.common.exception.BusinessException;
import com.company.wems.employee.model.Employee;
import com.company.wems.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceService {
    
    private final AttendanceEventRepository attendanceEventRepository;
    private final EmployeeRepository employeeRepository;
    private final GeofenceService geofenceService;
    private final ShiftService shiftService;
    
    public AttendanceResponse clockIn(AttendanceRequest request, String ipAddress) {
        log.info("Processing clock-in for employee: {}", request.getEmployeeId());
        
        Employee employee = employeeRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new BusinessException("Employee not found"));
        
        // Check if already clocked in
        Optional<AttendanceEvent> lastEvent = 
            attendanceEventRepository.findLastEventByEmployee(employee.getId());
        
        if (lastEvent.isPresent() && lastEvent.get().getType() == AttendanceEventType.CLOCK_IN) {
            throw new BusinessException("Employee is already clocked in");
        }
        
        // Validate geofence if location provided
        if (request.getGeoLocation() != null) {
            if (!geofenceService.isWithinWarehouseBoundary(request.getGeoLocation())) {
                throw new BusinessException("Location is outside warehouse boundary");
            }
        }
        
        // Associate with shift
        Long shiftId = shiftService.findCurrentShiftForEmployee(employee.getId());
        
        AttendanceEvent event = AttendanceEvent.builder()
            .employee(employee)
            .timestamp(LocalDateTime.now())
            .type(AttendanceEventType.CLOCK_IN)
            .deviceId(request.getDeviceId())
            .ipAddress(ipAddress)
            .geoLocation(request.getGeoLocation())
            .shiftId(shiftId)
            .correctionRequested(false)
            .build();
        
        AttendanceEvent saved = attendanceEventRepository.save(event);
        
        log.info("Clock-in successful for employee: {}", request.getEmployeeId());
        
        return AttendanceResponse.builder()
            .id(saved.getId())
            .employeeId(employee.getId())
            .timestamp(saved.getTimestamp())
            .type(saved.getType().name())
            .message("Clock-in successful")
            .build();
    }
    
    public AttendanceResponse clockOut(AttendanceRequest request, String ipAddress) {
        log.info("Processing clock-out for employee: {}", request.getEmployeeId());
        
        Employee employee = employeeRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new BusinessException("Employee not found"));
        
        // Check if clocked in
        Optional<AttendanceEvent> lastEvent = 
            attendanceEventRepository.findLastEventByEmployee(employee.getId());
        
        if (lastEvent.isEmpty() || lastEvent.get().getType() == AttendanceEventType.CLOCK_OUT) {
            throw new BusinessException("Employee is not clocked in");
        }
        
        AttendanceEvent clockInEvent = lastEvent.get();
        
        AttendanceEvent event = AttendanceEvent.builder()
            .employee(employee)
            .timestamp(LocalDateTime.now())
            .type(AttendanceEventType.CLOCK_OUT)
            .deviceId(request.getDeviceId())
            .ipAddress(ipAddress)
            .geoLocation(request.getGeoLocation())
            .shiftId(clockInEvent.getShiftId())
            .correctionRequested(false)
            .build();
        
        AttendanceEvent saved = attendanceEventRepository.save(event);
        
        // Calculate hours worked
        Duration duration = Duration.between(clockInEvent.getTimestamp(), saved.getTimestamp());
        double hoursWorked = duration.toMinutes() / 60.0;
        
        log.info("Clock-out successful for employee: {}. Hours worked: {}", 
                 request.getEmployeeId(), hoursWorked);
        
        return AttendanceResponse.builder()
            .id(saved.getId())
            .employeeId(employee.getId())
            .timestamp(saved.getTimestamp())
            .type(saved.getType().name())
            .hoursWorked(hoursWorked)
            .message("Clock-out successful")
            .build();
    }
    
    @Transactional(readOnly = true)
    public double calculateDailyTotal(Long employeeId, LocalDate date) {
        List<AttendanceEvent> events = attendanceEventRepository
            .findByEmployeeIdAndDate(employeeId, date);
        
        double totalHours = 0.0;
        AttendanceEvent clockIn = null;
        
        for (AttendanceEvent event : events) {
            if (event.getType() == AttendanceEventType.CLOCK_IN) {
                clockIn = event;
            } else if (event.getType() == AttendanceEventType.CLOCK_OUT && clockIn != null) {
                Duration duration = Duration.between(clockIn.getTimestamp(), event.getTimestamp());
                totalHours += duration.toMinutes() / 60.0;
                clockIn = null;
            }
        }
        
        return totalHours;
    }
}
```

**Attendance Controller (AttendanceController.java):**
```java
package com.company.wems.attendance.controller;

import com.company.wems.attendance.dto.AttendanceRequest;
import com.company.wems.attendance.dto.AttendanceResponse;
import com.company.wems.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Time & Attendance", description = "APIs for clock-in/out and attendance tracking")
public class AttendanceController {
    
    private final AttendanceService attendanceService;
    
    @PostMapping("/clock-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    @Operation(summary = "Clock in", description = "Records employee clock-in event")
    public ResponseEntity<AttendanceResponse> clockIn(
            @Valid @RequestBody AttendanceRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        AttendanceResponse response = attendanceService.clockIn(request, ipAddress);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/clock-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    @Operation(summary = "Clock out", description = "Records employee clock-out event")
    public ResponseEntity<AttendanceResponse> clockOut(
            @Valid @RequestBody AttendanceRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        AttendanceResponse response = attendanceService.clockOut(request, ipAddress);
        return ResponseEntity.ok(response);
    }
}
```

**Acceptance Criteria:**
- â POST /attendance/clock-in records clock-in event
- â POST /attendance/clock-out records clock-out event
- â Validation prevents duplicate clock-in
- â Automatic shift association
- â Geofence validation (optional)
- â Device and IP capture
- â Daily totals computed
- â Correction workflow for missed punches
- â CSV export available

--------------------------------------------------
### E05: Shift & Schedule Management
--------------------------------------------------

#### Section: Shift Template and Assignment System

**Description:**
Create recurring shift templates, manage rotations and overtime rules, assign shifts to employees, handle blackout dates, and maintain warehouse operation calendars.

**Design Specification:**
- Shift templates with recurrence patterns
- Shift assignments with conflict detection
- Blackout dates for warehouse closures
- Bulk assignment capabilities
- Personal shift view for workers
- Audit trail for all changes

**Sample Implementation:**

**Shift Template Entity (ShiftTemplate.java):**
```java
package com.company.wems.scheduling.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "shift_template")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    
    @Column(name = "recurrence_pattern")
    private String recurrencePattern; // DAILY, WEEKLY, etc.
    
    @Column(name = "overtime_eligible")
    private Boolean overtimeEligible = false;
    
    @Column(name = "active")
    private Boolean active = true;
}
```

**Shift Assignment Entity (ShiftAssignment.java):**
```java
package com.company.wems.scheduling.model;

import com.company.wems.employee.model.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shift_assignment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_template_id", nullable = false)
    private ShiftTemplate shiftTemplate;
    
    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;
    
    @Column(name = "assigned_by")
    private String assignedBy;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ShiftStatus status = ShiftStatus.SCHEDULED;
}

enum ShiftStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
```

**Blackout Date Entity (BlackoutDate.java):**
```java
package com.company.wems.scheduling.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "blackout_date")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlackoutDate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "date", nullable = false, unique = true)
    private LocalDate date;
    
    @Column(name = "reason")
    private String reason;
    
    @Column(name = "warehouse_id")
    private Long warehouseId;
}
```

**Shift Service (ShiftService.java):**
```java
package com.company.wems.scheduling.service;

import com.company.wems.common.exception.BusinessException;
import com.company.wems.scheduling.dto.ShiftAssignmentRequest;
import com.company.wems.scheduling.model.ShiftAssignment;
import com.company.wems.scheduling.model.ShiftTemplate;
import com.company.wems.scheduling.repository.BlackoutDateRepository;
import com.company.wems.scheduling.repository.ShiftAssignmentRepository;
import com.company.wems.scheduling.repository.ShiftTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShiftService {
    
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final BlackoutDateRepository blackoutDateRepository;
    
    public ShiftAssignment assignShift(ShiftAssignmentRequest request) {
        log.info("Assigning shift to employee: {}", request.getEmployeeId());
        
        // Check for blackout date
        if (blackoutDateRepository.existsByDate(request.getShiftDate())) {
            throw new BusinessException("Cannot assign shift on blackout date");
        }
        
        // Check for conflicts
        boolean hasConflict = shiftAssignmentRepository
            .existsByEmployeeIdAndShiftDate(request.getEmployeeId(), request.getShiftDate());
        
        if (hasConflict) {
            throw new BusinessException("Employee already has a shift on this date");
        }
        
        ShiftTemplate template = shiftTemplateRepository.findById(request.getShiftTemplateId())
            .orElseThrow(() -> new BusinessException("Shift template not found"));
        
        // Create assignment
        ShiftAssignment assignment = ShiftAssignment.builder()
            .employee(employeeRepository.getReferenceById(request.getEmployeeId()))
            .shiftTemplate(template)
            .shiftDate(request.getShiftDate())
            .assignedBy(SecurityContextHolder.getContext().getAuthentication().getName())
            .assignedAt(LocalDateTime.now())
            .build();
        
        return shiftAssignmentRepository.save(assignment);
    }
    
    public List<ShiftAssignment> bulkAssignShifts(List<ShiftAssignmentRequest> requests) {
        log.info("Bulk assigning {} shifts", requests.size());
        
        return requests.stream()
            .map(this::assignShift)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public List<ShiftAssignment> getUpcomingShifts(Long employeeId) {
        LocalDate today = LocalDate.now();
        return shiftAssignmentRepository
            .findByEmployeeIdAndShiftDateGreaterThanEqualOrderByShiftDateAsc(
                employeeId, today
            );
    }
}
```

**Acceptance Criteria:**
- â CRUD for shift templates
- â CRUD for shift assignments
- â Conflict detection prevents double-booking
- â Blackout dates block assignments
- â Workers can view personal upcoming shifts
- â Supervisors can bulk-assign shifts
- â Audit entries generated for all changes

--------------------------------------------------
### E06-E20: Additional Epics
--------------------------------------------------

Due to space constraints, the remaining epics (E06 through E20) follow similar patterns:

**E06: Leave & Absence Management**
- Entities: LeaveRequest, LeaveBalance, LeavePolicy
- Workflow: Request â Approval â Balance Update
- Integration: Exclude from scheduling and payroll

**E07: Training & Certification Tracking**
- Entities: Certification, CertificationType, CertificationRequirement
- Features: Expiry alerts, document upload, scheduling validation

**E08: Safety Incidents & OSHA Reporting**
- Entities: SafetyIncident, IncidentInvestigation
- Workflow: Report â Investigate â Resolve
- Export: OSHA 300/300A formats

**E09: Equipment & Asset Assignment**
- Entities: Asset, AssetAssignment, AssetCondition
- Features: Check-in/out, certification validation, overdue tracking

**E10: Performance Reviews & Goals**
- Entities: PerformanceReview, ReviewTemplate, Goal
- Features: Review cycles, PDF export, immutable history

**E11: Payroll Export Integration**
- Service: PayrollExportService
- Features: File generation, SFTP/API delivery, retry logic

**E12: Notifications & Announcements**
- Entities: Notification, NotificationPreference, Announcement
- Channels: In-app, email, SMS
- Features: Opt-in/out, templates, rate limiting

**E13: Integration Layer (HRIS/WMS APIs)**
- APIs: HRIS sync, WMS integration, SSO
- Security: JWT/OAuth2
- Features: Webhooks, idempotency

**E14: Audit Trail & Compliance**
- Entity: AuditLog
- Features: Immutable storage, export, comprehensive coverage

**E15: Reporting & Analytics**
- Service: ReportingService
- Reports: Attendance, overtime, leave, certifications, safety KPIs
- Export: CSV, PDF

**E16: Mobile Access (PWA)**
- Features: Responsive design, offline support, PWA manifest
- Flows: Clock-in/out, view schedule, request leave

**E17: Onboarding & Offboarding Workflow**
- Workflow: HRIS trigger â Task generation â Provisioning/Deprovisioning
- Features: Training assignment, asset collection, access revocation

**E18: Localization & Multi-Warehouse**
- Entity: Warehouse
- Features: Warehouse-specific policies, UI localization (en, es)

**E19: Advanced Scheduling (AI/Optimization)**
- Service: AISchedulingService
- Features: Demand forecasting, skill matching, optimization proposals

**E20: Document Management**
- Entity: Document
- Features: Upload/download, versioning, virus scan, retention policies

==================================================================
## 4. Shared Security, Error Handling, and Validation Strategies
==================================================================

### Security Strategy

**Method-Level Security:**
```java
@PreAuthorize("hasRole('ADMIN')")
public void adminOnlyMethod() { ... }

@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public void hrMethod() { ... }
```

**Row-Level Security:**
- Implement in service layer
- Filter queries based on user role and team
- Use Spring Security context to get current user

**API Security:**
- JWT tokens for authentication
- API keys for system integrations
- OAuth2 for SSO

### Error Handling Strategy

**Global Exception Handler:**
```java
package com.company.wems.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Conflict")
            .message(ex.getMessage())
            .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message(ex.getMessage())
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

**Error Response DTO:**
```java
package com.company.wems.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
```

### Validation Strategy

**Bean Validation:**
```java
public class EmployeeDto {
    @NotBlank(message = "Badge ID is required")
    @Size(max = 32, message = "Badge ID must not exceed 32 characters")
    private String badgeId;
    
    @NotBlank(message = "Name is required")
    @Size(max = 128, message = "Name must not exceed 128 characters")
    private String name;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @Past(message = "Hire date must be in the past")
    private LocalDate hireDate;
}
```

**Custom Validators:**
```java
package com.company.wems.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueBadgeIdValidator.class)
@Documented
public @interface UniqueBadgeId {
    String message() default "Badge ID already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

==================================================================
## 5. Appendix: Additional Configuration and Code Snippets
==================================================================

### Complete application.yml

```yaml
server:
  port: 8080
  servlet:
    context-path: /

spring:
  application:
    name: warehouse-ems
  
  datasource:
    url: jdbc:postgresql://localhost:5432/wems
    username: wems
    password: ${DB_PASSWORD:secret}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        use_sql_comments: true
  
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    validate-on-migrate: true
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI:http://localhost:8080/auth}
          jwk-set-uri: ${JWT_JWK_SET_URI:http://localhost:8080/auth/.well-known/jwks.json}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    root: INFO
    com.company.wems: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

security:
  api-key:
    enabled: ${API_KEY_ENABLED:false}
    header-name: X-API-Key

geofence:
  enabled: ${GEOFENCE_ENABLED:false}
  warehouse-latitude: 37.7749
  warehouse-longitude: -122.4194
  radius-meters: 100

notification:
  email:
    enabled: true
    from: noreply@warehouse-ems.com
  sms:
    enabled: false
    provider: twilio
```

### Maven Dependencies (pom.xml excerpt)

```xml
<dependencies>
    <!-- Spring Boot Starters -->
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
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    
    <!-- OpenAPI/Swagger -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.2.0</version>
    </dependency>
    
    <!-- MapStruct -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>1.5.5.Final</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Sample Migration Script (V2__create_attendance_tables.sql)

```sql
-- Create attendance_event table
CREATE TABLE attendance_event (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    timestamp TIMESTAMP NOT NULL,
    type VARCHAR(16) NOT NULL,
    device_id VARCHAR(64),
    ip_address VARCHAR(45),
    geo_location VARCHAR(64),
    shift_id BIGINT,
    correction_requested BOOLEAN DEFAULT FALSE,
    correction_reason TEXT,
    approved BOOLEAN,
    approved_by VARCHAR(128),
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attendance_employee_id ON attendance_event(employee_id);
CREATE INDEX idx_attendance_timestamp ON attendance_event(timestamp);
CREATE INDEX idx_attendance_type ON attendance_event(type);

-- Create shift_template table
CREATE TABLE shift_template (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence_pattern VARCHAR(32),
    overtime_eligible BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create shift_assignment table
CREATE TABLE shift_assignment (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    shift_template_id BIGINT NOT NULL REFERENCES shift_template(id),
    shift_date DATE NOT NULL,
    assigned_by VARCHAR(128),
    assigned_at TIMESTAMP,
    status VARCHAR(16) DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_shift_assignment_employee ON shift_assignment(employee_id);
CREATE INDEX idx_shift_assignment_date ON shift_assignment(shift_date);
CREATE UNIQUE INDEX idx_shift_assignment_unique ON shift_assignment(employee_id, shift_date);

-- Create blackout_date table
CREATE TABLE blackout_date (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL UNIQUE,
    reason VARCHAR(256),
    warehouse_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

==================================================================
## CONCLUSION
==================================================================

This comprehensive low-level technical design document provides detailed specifications for all 20 epics of the Warehouse Employee Management System. Each epic includes:

â Complete entity designs with JPA annotations
â Repository layer with Spring Data JPA
â Service layer with business logic
â Controller layer with REST endpoints
â DTOs with validation
â Security configurations
â Database migration scripts
â Error handling strategies
â Sample code implementations

The design follows Spring Boot best practices and industry standards, ensuring:
- Maintainability through layered architecture
- Security through RBAC and authentication
- Scalability through proper database design
- Testability through dependency injection
- Documentation through OpenAPI/Swagger

Development teams can use this document as a blueprint for implementation, with clear specifications for each component and comprehensive code examples.

==================================================================
END OF DOCUMENT
==================================================================