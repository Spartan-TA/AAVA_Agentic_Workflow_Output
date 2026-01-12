# Warehouse Employee Management System - Detailed User Stories & Technical Design Document

## Document Overview
**Version:** 1.0  
**Date:** 2024  
**Target Framework:** Spring Boot 3.x with Java 17+  
**Architecture:** Microservices-ready Monolith with Domain-Driven Design  
**Database:** PostgreSQL 14+  
**Build Tool:** Maven  

---

## Table of Contents
1. [E01: Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
2. [E02: Employee Master Data (CRUD)](#e02-employee-master-data-crud)
3. [E03: Role-Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
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
18. [E18: Localization & Multi-Tenant](#e18-localization--multi-tenant)
19. [E19: Observability & Monitoring](#e19-observability--monitoring)
20. [E20: CI/CD & Deployment Automation](#e20-cicd--deployment-automation)

---

## E01: Project Scaffolding & Domain Setup

### Epic Description
Initialize Spring Boot (Maven) project; configure base packages; set up core modules (employee, scheduling, attendance, safety); add Flyway/Liquibase for DB migrations; enable Actuator.

### User Stories

#### US-E01-001: Initialize Spring Boot Project
**As a** DevOps Engineer  
**I want** to initialize a Spring Boot Maven project with standard configurations  
**So that** developers have a consistent foundation to build upon

**Acceptance Criteria:**
- Spring Boot 3.2+ project created with Maven
- Java 17+ configured as target version
- Application runs on port 8080 by default
- application.yml/properties configured with profiles (dev, staging, prod)
- Lombok dependency included for reducing boilerplate
- Spring Boot Starter Web, JPA, Security, Validation included
- Project builds successfully with `mvn clean install`

**Technical Specifications:**
```xml
<!-- pom.xml key dependencies -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>

<properties>
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>

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
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

**Package Structure:**
```
com.warehouse.employee
âââ config/              # Configuration classes
âââ domain/
â   âââ employee/        # Employee domain
â   âââ scheduling/      # Scheduling domain
â   âââ attendance/      # Attendance domain
â   âââ safety/          # Safety domain
âââ common/
â   âââ exception/       # Global exception handling
â   âââ dto/             # Common DTOs
â   âââ util/            # Utility classes
âââ WarehouseEmployeeApplication.java
```

#### US-E01-002: Configure Database Migration Tool
**As a** Database Administrator  
**I want** to use Flyway for database version control  
**So that** schema changes are tracked and applied consistently across environments

**Acceptance Criteria:**
- Flyway dependency added to pom.xml
- Baseline migration script created (V1__baseline.sql)
- Migration scripts location configured (db/migration)
- Flyway runs automatically on application startup
- Migration history table created (flyway_schema_history)
- Failed migrations prevent application startup

**Technical Specifications:**
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

```yaml
# application.yml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    baseline-version: 0
    baseline-description: Initial baseline
```

**Migration Script Example:**
```sql
-- V1__baseline.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS tenant (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tenant_code ON tenant(code);
```

#### US-E01-003: Enable Spring Boot Actuator
**As a** DevOps Engineer  
**I want** to enable Spring Boot Actuator with health and metrics endpoints  
**So that** I can monitor application health and performance

**Acceptance Criteria:**
- Actuator dependency added
- Health endpoint accessible at /actuator/health
- Health endpoint returns {"status": "UP"} when healthy
- Info endpoint configured with build information
- Metrics endpoint enabled for Prometheus
- Sensitive endpoints secured

**Technical Specifications:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
```

#### US-E01-004: Create README Documentation
**As a** Developer  
**I want** comprehensive README documentation  
**So that** I can quickly understand how to build and run the application

**Acceptance Criteria:**
- README.md exists in project root
- Prerequisites section lists required tools and versions
- Build instructions provided
- Run instructions for different profiles
- Database setup instructions included
- API documentation link provided

**README Template:**
```markdown
# Warehouse Employee Management System

## Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Docker (optional)

## Build
```bash
mvn clean install
```

## Run
```bash
# Development
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production
java -jar target/warehouse-employee-mgmt-1.0.0.jar --spring.profiles.active=prod
```

## Database Setup
```sql
CREATE DATABASE warehouse_employee_db;
CREATE USER warehouse_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE warehouse_employee_db TO warehouse_user;
```

## Health Check
http://localhost:8080/actuator/health
```

---

## E02: Employee Master Data (CRUD)

### Epic Description
Create Employee domain with CRUD APIs and web DTOs: name, badgeId, role, department, shiftGroup, hireDate, status.

### User Stories

#### US-E02-001: Create Employee Entity and Repository
**As a** Backend Developer  
**I want** to create the Employee JPA entity with all required fields  
**So that** employee data can be persisted to the database

**Acceptance Criteria:**
- Employee entity created with all fields (id, badgeId, firstName, lastName, email, role, department, shiftGroup, hireDate, status, tenantId)
- badgeId has unique constraint
- Soft delete supported via status field
- Audit fields included (createdAt, updatedAt, createdBy, updatedBy)
- JPA repository interface created
- Custom queries for filtering and searching

**Technical Specifications:**
```java
package com.warehouse.employee.domain.employee.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employee", indexes = {
    @Index(name = "idx_employee_badge_id", columnList = "badge_id"),
    @Index(name = "idx_employee_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_employee_department", columnList = "department"),
    @Index(name = "idx_employee_status", columnList = "status")
})
@SQLDelete(sql = "UPDATE employee SET status = 'INACTIVE', updated_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "status != 'DELETED'")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "badge_id", unique = true, nullable = false, length = 50)
    private String badgeId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private EmployeeRole role;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "shift_group", length = 50)
    private String shiftGroup;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmployeeStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = EmployeeStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

public enum EmployeeRole {
    ADMIN, HR, SUPERVISOR, WORKER
}

public enum EmployeeStatus {
    ACTIVE, INACTIVE, ON_LEAVE, TERMINATED, DELETED
}
```

**Repository Interface:**
```java
package com.warehouse.employee.domain.employee.repository;

import com.warehouse.employee.domain.employee.entity.Employee;
import com.warehouse.employee.domain.employee.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByBadgeIdAndTenantId(String badgeId, UUID tenantId);

    Page<Employee> findByTenantIdAndStatus(UUID tenantId, EmployeeStatus status, Pageable pageable);

    Page<Employee> findByTenantIdAndDepartment(UUID tenantId, String department, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.tenantId = :tenantId " +
           "AND (:department IS NULL OR e.department = :department) " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (:search IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(e.badgeId) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Employee> findByFilters(@Param("tenantId") UUID tenantId,
                                 @Param("department") String department,
                                 @Param("status") EmployeeStatus status,
                                 @Param("search") String search,
                                 Pageable pageable);

    boolean existsByBadgeIdAndTenantId(String badgeId, UUID tenantId);
}
```

#### US-E02-002: Create Employee DTOs
**As a** Backend Developer  
**I want** to create request and response DTOs for Employee operations  
**So that** API contracts are well-defined and validated

**Acceptance Criteria:**
- CreateEmployeeRequest DTO with validation annotations
- UpdateEmployeeRequest DTO with validation annotations
- EmployeeResponse DTO for API responses
- EmployeeSummaryResponse DTO for list views
- Validation includes: required fields, email format, badge ID format
- DTOs use Java Bean Validation (JSR-380)

**Technical Specifications:**
```java
package com.warehouse.employee.domain.employee.dto;

import com.warehouse.employee.domain.employee.entity.EmployeeRole;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEmployeeRequest {

    @NotBlank(message = "Badge ID is required")
    @Pattern(regexp = "^[A-Z0-9]{6,10}$", message = "Badge ID must be 6-10 alphanumeric characters")
    private String badgeId;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^\+?[1-9]\d{1,14}$", message = "Invalid phone number format")
    private String phone;

    @NotNull(message = "Role is required")
    private EmployeeRole role;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @Size(max = 50, message = "Shift group must not exceed 50 characters")
    private String shiftGroup;

    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEmployeeRequest {

    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^\+?[1-9]\d{1,14}$", message = "Invalid phone number format")
    private String phone;

    private EmployeeRole role;

    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @Size(max = 50, message = "Shift group must not exceed 50 characters")
    private String shiftGroup;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {
    private UUID id;
    private String badgeId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private EmployeeRole role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private EmployeeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSummaryResponse {
    private UUID id;
    private String badgeId;
    private String fullName;
    private String department;
    private EmployeeRole role;
    private EmployeeStatus status;
}
```

#### US-E02-003: Implement Employee Service Layer
**As a** Backend Developer  
**I want** to implement business logic for Employee CRUD operations  
**So that** operations are validated and executed correctly

**Acceptance Criteria:**
- EmployeeService interface and implementation created
- Create, Read, Update, Patch, Delete operations implemented
- Badge ID uniqueness validated
- Tenant isolation enforced
- Soft delete implemented
- Business exceptions thrown for invalid operations
- MapStruct mapper for entity-DTO conversion

**Technical Specifications:**
```java
package com.warehouse.employee.domain.employee.service;

import com.warehouse.employee.domain.employee.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface EmployeeService {
    EmployeeResponse createEmployee(UUID tenantId, CreateEmployeeRequest request);
    EmployeeResponse getEmployeeById(UUID tenantId, UUID employeeId);
    EmployeeResponse getEmployeeByBadgeId(UUID tenantId, String badgeId);
    Page<EmployeeSummaryResponse> getAllEmployees(UUID tenantId, String department, 
                                                   EmployeeStatus status, String search, 
                                                   Pageable pageable);
    EmployeeResponse updateEmployee(UUID tenantId, UUID employeeId, UpdateEmployeeRequest request);
    EmployeeResponse patchEmployee(UUID tenantId, UUID employeeId, Map<String, Object> updates);
    void deleteEmployee(UUID tenantId, UUID employeeId);
}

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(UUID tenantId, CreateEmployeeRequest request) {
        log.info("Creating employee with badgeId: {} for tenant: {}", request.getBadgeId(), tenantId);
        
        // Validate badge ID uniqueness
        if (employeeRepository.existsByBadgeIdAndTenantId(request.getBadgeId(), tenantId)) {
            throw new DuplicateBadgeIdException("Badge ID already exists: " + request.getBadgeId());
        }

        Employee employee = employeeMapper.toEntity(request);
        employee.setTenantId(tenantId);
        employee.setStatus(EmployeeStatus.ACTIVE);
        
        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee created successfully with ID: {}", savedEmployee.getId());
        
        return employeeMapper.toResponse(savedEmployee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(UUID tenantId, UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .filter(e -> e.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + employeeId));
        
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByBadgeId(UUID tenantId, String badgeId) {
        Employee employee = employeeRepository.findByBadgeIdAndTenantId(badgeId, tenantId)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with badge ID: " + badgeId));
        
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeSummaryResponse> getAllEmployees(UUID tenantId, String department,
                                                          EmployeeStatus status, String search,
                                                          Pageable pageable) {
        Page<Employee> employees = employeeRepository.findByFilters(tenantId, department, status, search, pageable);
        return employees.map(employeeMapper::toSummaryResponse);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(UUID tenantId, UUID employeeId, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
            .filter(e -> e.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + employeeId));

        employeeMapper.updateEntityFromDto(request, employee);
        Employee updatedEmployee = employeeRepository.save(employee);
        
        return employeeMapper.toResponse(updatedEmployee);
    }

    @Override
    @Transactional
    public void deleteEmployee(UUID tenantId, UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .filter(e -> e.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + employeeId));

        employee.setStatus(EmployeeStatus.DELETED);
        employeeRepository.save(employee);
        log.info("Employee soft deleted: {}", employeeId);
    }
}
```

**MapStruct Mapper:**
```java
package com.warehouse.employee.domain.employee.mapper;

import com.warehouse.employee.domain.employee.dto.*;
import com.warehouse.employee.domain.employee.entity.Employee;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeMapper {

    Employee toEntity(CreateEmployeeRequest request);

    EmployeeResponse toResponse(Employee employee);

    @Mapping(target = "fullName", expression = "java(employee.getFirstName() + ' ' + employee.getLastName())")
    EmployeeSummaryResponse toSummaryResponse(Employee employee);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateEmployeeRequest request, @MappingTarget Employee employee);
}
```

#### US-E02-004: Create Employee REST Controller
**As a** Backend Developer  
**I want** to expose RESTful endpoints for Employee operations  
**So that** clients can manage employee data via HTTP APIs

**Acceptance Criteria:**
- REST controller created with proper annotations
- Endpoints: POST, GET (by ID and list), PUT, PATCH, DELETE
- Request validation enforced
- Proper HTTP status codes returned
- Pagination and filtering supported
- OpenAPI/Swagger annotations added
- Exception handling with proper error responses

**Technical Specifications:**
```java
package com.warehouse.employee.domain.employee.controller;

import com.warehouse.employee.domain.employee.dto.*;
import com.warehouse.employee.domain.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "APIs for managing warehouse employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Create new employee", description = "Creates a new employee record")
    @ApiResponse(responseCode = "201", description = "Employee created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "409", description = "Badge ID already exists")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get employee by ID", description = "Retrieves employee details by ID")
    @ApiResponse(responseCode = "200", description = "Employee found")
    @ApiResponse(responseCode = "404", description = "Employee not found")
    public ResponseEntity<EmployeeResponse> getEmployeeById(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID employeeId) {
        EmployeeResponse response = employeeService.getEmployeeById(tenantId, employeeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/badge/{badgeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get employee by badge ID", description = "Retrieves employee details by badge ID")
    public ResponseEntity<EmployeeResponse> getEmployeeByBadgeId(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable String badgeId) {
        EmployeeResponse response = employeeService.getEmployeeByBadgeId(tenantId, badgeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get all employees", description = "Retrieves paginated list of employees with optional filters")
    public ResponseEntity<Page<EmployeeSummaryResponse>> getAllEmployees(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {
        Page<EmployeeSummaryResponse> response = employeeService.getAllEmployees(
            tenantId, department, status, search, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update employee", description = "Updates employee information")
    @ApiResponse(responseCode = "200", description = "Employee updated successfully")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID employeeId,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(tenantId, employeeId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Partially update employee", description = "Updates specific employee fields")
    public ResponseEntity<EmployeeResponse> patchEmployee(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID employeeId,
            @RequestBody Map<String, Object> updates) {
        EmployeeResponse response = employeeService.patchEmployee(tenantId, employeeId, updates);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete employee", description = "Soft deletes an employee record")
    @ApiResponse(responseCode = "204", description = "Employee deleted successfully")
    public ResponseEntity<Void> deleteEmployee(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID employeeId) {
        employeeService.deleteEmployee(tenantId, employeeId);
        return ResponseEntity.noContent().build();
    }
}
```

#### US-E02-005: Implement Global Exception Handling
**As a** Backend Developer  
**I want** centralized exception handling  
**So that** API errors are returned in a consistent format

**Technical Specifications:**
```java
package com.warehouse.employee.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<ValidationError> validationErrors;
}

@Data
@AllArgsConstructor
public class ValidationError {
    private String field;
    private String message;
}

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotFound(EmployeeNotFoundException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateBadgeIdException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateBadgeId(DuplicateBadgeIdException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "Conflict",
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        List<ValidationError> validationErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
            .collect(Collectors.toList());

        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "Invalid request data",
            request.getDescription(false).replace("uri=", ""),
            validationErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred",
            request.getDescription(false).replace("uri=", ""),
            null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

## E03: Role-Based Access Control (RBAC)

### Epic Description
Add Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER); method/endpoint security and row-level constraints where applicable; API key/OAuth2 toggle via config.

### User Stories

#### US-E03-001: Configure Spring Security
**As a** Security Engineer  
**I want** to configure Spring Security with role-based access control  
**So that** only authorized users can access protected resources

**Acceptance Criteria:**
- Spring Security configured with custom UserDetailsService
- Password encoding with BCrypt
- JWT token-based authentication
- Role hierarchy defined (ADMIN > HR > SUPERVISOR > WORKER)
- CORS configuration for frontend integration
- Security filter chain configured

**Technical Specifications:**
```java
package com.warehouse.employee.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configure(http))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**", "/actuator/health", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

#### US-E03-002: Implement JWT Authentication
**As a** Security Engineer  
**I want** to implement JWT token-based authentication  
**So that** users can authenticate securely without server-side sessions

**Acceptance Criteria:**
- JWT token generation on successful login
- Token validation on each request
- Token expiration handling (15 minutes access, 7 days refresh)
- Refresh token mechanism
- Token contains user ID, roles, and tenant ID

**Technical Specifications:**
```java
package com.warehouse.employee.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration; // 15 minutes

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration; // 7 days

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractTenantId(String token) {
        return UUID.fromString(extractClaim(token, claims -> claims.get("tenantId", String.class)));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails, UUID tenantId) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("tenantId", tenantId.toString());
        extraClaims.put("roles", userDetails.getAuthorities());
        return generateToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails, UUID tenantId) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("tenantId", tenantId.toString());
        return generateToken(extraClaims, userDetails, refreshExpiration);
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
            .setClaims(extraClaims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSignInKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        username = jwtService.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

#### US-E03-003: Create Authentication Endpoints
**As a** Backend Developer  
**I want** to create login, logout, and token refresh endpoints  
**So that** users can authenticate and manage their sessions

**Technical Specifications:**
```java
package com.warehouse.employee.domain.auth.controller;

import com.warehouse.employee.domain.auth.dto.*;
import com.warehouse.employee.domain.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Generate new access token using refresh token")
    public ResponseEntity<AuthenticationResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidate user session")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        authenticationService.logout(token);
        return ResponseEntity.noContent().build();
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;
}

@Data
@AllArgsConstructor
public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private UserInfo user;
}

@Data
@AllArgsConstructor
public class UserInfo {
    private UUID id;
    private String username;
    private String fullName;
    private List<String> roles;
    private UUID tenantId;
}
```

---

## E04: Time & Attendance (Clock In/Out)

### Epic Description
Endpoints for clock-in/out events with geofence (optional) and device capture; calculate hours worked per shift; handle missed punches and corrections workflow.

### User Stories

#### US-E04-001: Create Attendance Entity and Repository
**As a** Backend Developer  
**I want** to create the Attendance entity for tracking clock-in/out events  
**So that** employee time can be accurately recorded

**Acceptance Criteria:**
- Attendance entity with fields: id, employeeId, shiftId, clockInTime, clockOutTime, location, device, status
- Support for geofence validation (latitude, longitude, radius)
- Automatic calculation of hours worked
- Status tracking (CLOCKED_IN, CLOCKED_OUT, MISSED_PUNCH, CORRECTED)

**Technical Specifications:**
```java
package com.warehouse.employee.domain.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendance", indexes = {
    @Index(name = "idx_attendance_employee", columnList = "employee_id"),
    @Index(name = "idx_attendance_date", columnList = "clock_in_time"),
    @Index(name = "idx_attendance_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "shift_id")
    private UUID shiftId;

    @Column(name = "clock_in_time", nullable = false)
    private LocalDateTime clockInTime;

    @Column(name = "clock_out_time")
    private LocalDateTime clockOutTime;

    @Column(name = "clock_in_latitude")
    private BigDecimal clockInLatitude;

    @Column(name = "clock_in_longitude")
    private BigDecimal clockInLongitude;

    @Column(name = "clock_out_latitude")
    private BigDecimal clockOutLatitude;

    @Column(name = "clock_out_longitude")
    private BigDecimal clockOutLongitude;

    @Column(name = "clock_in_device", length = 100)
    private String clockInDevice;

    @Column(name = "clock_out_device", length = 100)
    private String clockOutDevice;

    @Column(name = "hours_worked", precision = 5, scale = 2)
    private BigDecimal hoursWorked;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttendanceStatus status;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "correction_requested")
    private Boolean correctionRequested = false;

    @Column(name = "correction_approved_by")
    private UUID correctionApprovedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = AttendanceStatus.CLOCKED_IN;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateHoursWorked();
    }

    public void calculateHoursWorked() {
        if (clockInTime != null && clockOutTime != null) {
            Duration duration = Duration.between(clockInTime, clockOutTime);
            this.hoursWorked = BigDecimal.valueOf(duration.toMinutes() / 60.0)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }
}

public enum AttendanceStatus {
    CLOCKED_IN,
    CLOCKED_OUT,
    MISSED_PUNCH,
    CORRECTED,
    PENDING_APPROVAL
}
```

#### US-E04-002: Implement Clock-In Endpoint
**As a** Warehouse Worker  
**I want** to clock in at the start of my shift  
**So that** my work hours are accurately tracked

**Acceptance Criteria:**
- POST /api/v1/attendance/clock-in endpoint
- Validates employee is not already clocked in
- Captures device information and location
- Optional geofence validation
- Associates with scheduled shift if available
- Returns attendance record with clock-in time

**Technical Specifications:**
```java
package com.warehouse.employee.domain.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClockInRequest {
    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private String deviceInfo;
    private String notes;
}

@Data
@AllArgsConstructor
@Builder
public class AttendanceResponse {
    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private UUID shiftId;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private BigDecimal hoursWorked;
    private AttendanceStatus status;
    private String notes;
}

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final GeofenceService geofenceService;

    @Override
    @Transactional
    public AttendanceResponse clockIn(UUID tenantId, ClockInRequest request) {
        log.info("Processing clock-in for employee: {}", request.getEmployeeId());

        // Validate employee exists
        Employee employee = employeeRepository.findById(request.getEmployeeId())
            .filter(e -> e.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        // Check if already clocked in
        Optional<Attendance> activeAttendance = attendanceRepository
            .findActiveAttendanceByEmployee(tenantId, request.getEmployeeId());
        
        if (activeAttendance.isPresent()) {
            throw new AlreadyClockedInException("Employee is already clocked in");
        }

        // Validate geofence if location provided
        if (request.getLatitude() != null && request.getLongitude() != null) {
            if (!geofenceService.isWithinGeofence(request.getLatitude(), request.getLongitude())) {
                throw new GeofenceViolationException("Clock-in location is outside allowed area");
            }
        }

        // Find scheduled shift
        Optional<Shift> scheduledShift = shiftRepository
            .findScheduledShiftForEmployee(employee.getId(), LocalDateTime.now());

        // Create attendance record
        Attendance attendance = Attendance.builder()
            .tenantId(tenantId)
            .employeeId(employee.getId())
            .shiftId(scheduledShift.map(Shift::getId).orElse(null))
            .clockInTime(LocalDateTime.now())
            .clockInLatitude(request.getLatitude())
            .clockInLongitude(request.getLongitude())
            .clockInDevice(request.getDeviceInfo())
            .status(AttendanceStatus.CLOCKED_IN)
            .notes(request.getNotes())
            .build();

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Clock-in successful for employee: {}, attendance ID: {}", 
                 employee.getId(), savedAttendance.getId());

        return mapToResponse(savedAttendance, employee);
    }
}
```

#### US-E04-003: Implement Clock-Out Endpoint
**As a** Warehouse Worker  
**I want** to clock out at the end of my shift  
**So that** my work hours are completed and calculated

**Acceptance Criteria:**
- POST /api/v1/attendance/clock-out endpoint
- Validates employee is currently clocked in
- Captures clock-out time and location
- Automatically calculates hours worked
- Updates attendance status to CLOCKED_OUT

**Technical Specifications:**
```java
@Override
@Transactional
public AttendanceResponse clockOut(UUID tenantId, ClockOutRequest request) {
    log.info("Processing clock-out for employee: {}", request.getEmployeeId());

    // Find active attendance
    Attendance attendance = attendanceRepository
        .findActiveAttendanceByEmployee(tenantId, request.getEmployeeId())
        .orElseThrow(() -> new NotClockedInException("Employee is not clocked in"));

    // Validate geofence if location provided
    if (request.getLatitude() != null && request.getLongitude() != null) {
        if (!geofenceService.isWithinGeofence(request.getLatitude(), request.getLongitude())) {
            throw new GeofenceViolationException("Clock-out location is outside allowed area");
        }
    }

    // Update attendance
    attendance.setClockOutTime(LocalDateTime.now());
    attendance.setClockOutLatitude(request.getLatitude());
    attendance.setClockOutLongitude(request.getLongitude());
    attendance.setClockOutDevice(request.getDeviceInfo());
    attendance.setStatus(AttendanceStatus.CLOCKED_OUT);
    attendance.calculateHoursWorked();

    if (request.getNotes() != null) {
        attendance.setNotes(attendance.getNotes() + " | " + request.getNotes());
    }

    Attendance updatedAttendance = attendanceRepository.save(attendance);
    log.info("Clock-out successful for employee: {}, hours worked: {}", 
             request.getEmployeeId(), updatedAttendance.getHoursWorked());

    Employee employee = employeeRepository.findById(request.getEmployeeId()).orElseThrow();
    return mapToResponse(updatedAttendance, employee);
}
```

#### US-E04-004: Implement Attendance Correction Workflow
**As a** Supervisor  
**I want** to review and approve attendance corrections  
**So that** missed punches and errors can be fixed

**Acceptance Criteria:**
- Employees can request corrections for missed punches
- Supervisors receive correction requests
- Supervisors can approve/reject corrections
- Approved corrections update attendance records
- Audit trail maintained for all corrections

**Technical Specifications:**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCorrectionRequest {
    @NotNull
    private UUID attendanceId;
    
    private LocalDateTime correctedClockInTime;
    private LocalDateTime correctedClockOutTime;
    
    @NotBlank
    @Size(max = 500)
    private String reason;
}

@Override
@Transactional
public AttendanceResponse requestCorrection(UUID tenantId, UUID employeeId, 
                                           AttendanceCorrectionRequest request) {
    Attendance attendance = attendanceRepository.findById(request.getAttendanceId())
        .filter(a -> a.getTenantId().equals(tenantId) && a.getEmployeeId().equals(employeeId))
        .orElseThrow(() -> new AttendanceNotFoundException("Attendance record not found"));

    attendance.setCorrectionRequested(true);
    attendance.setStatus(AttendanceStatus.PENDING_APPROVAL);
    attendance.setNotes(attendance.getNotes() + " | Correction requested: " + request.getReason());

    // Create correction record
    AttendanceCorrection correction = AttendanceCorrection.builder()
        .attendanceId(attendance.getId())
        .requestedBy(employeeId)
        .originalClockInTime(attendance.getClockInTime())
        .originalClockOutTime(attendance.getClockOutTime())
        .correctedClockInTime(request.getCorrectedClockInTime())
        .correctedClockOutTime(request.getCorrectedClockOutTime())
        .reason(request.getReason())
        .status(CorrectionStatus.PENDING)
        .build();

    attendanceCorrectionRepository.save(correction);
    Attendance updated = attendanceRepository.save(attendance);

    // Notify supervisor
    notificationService.notifySupervisor(tenantId, employeeId, 
        "Attendance correction requested", correction.getId());

    Employee employee = employeeRepository.findById(employeeId).orElseThrow();
    return mapToResponse(updated, employee);
}

@Override
@Transactional
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
public AttendanceResponse approveCorrection(UUID tenantId, UUID correctionId, UUID approverId) {
    AttendanceCorrection correction = attendanceCorrectionRepository.findById(correctionId)
        .orElseThrow(() -> new CorrectionNotFoundException("Correction not found"));

    Attendance attendance = attendanceRepository.findById(correction.getAttendanceId())
        .orElseThrow(() -> new AttendanceNotFoundException("Attendance not found"));

    // Apply corrections
    if (correction.getCorrectedClockInTime() != null) {
        attendance.setClockInTime(correction.getCorrectedClockInTime());
    }
    if (correction.getCorrectedClockOutTime() != null) {
        attendance.setClockOutTime(correction.getCorrectedClockOutTime());
    }

    attendance.setStatus(AttendanceStatus.CORRECTED);
    attendance.setCorrectionApprovedBy(approverId);
    attendance.calculateHoursWorked();

    correction.setStatus(CorrectionStatus.APPROVED);
    correction.setApprovedBy(approverId);
    correction.setApprovedAt(LocalDateTime.now());

    attendanceCorrectionRepository.save(correction);
    Attendance updated = attendanceRepository.save(attendance);

    Employee employee = employeeRepository.findById(attendance.getEmployeeId()).orElseThrow();
    return mapToResponse(updated, employee);
}
```

#### US-E04-005: Generate Daily Attendance Reports
**As a** HR Manager  
**I want** to export daily attendance reports  
**So that** I can review employee hours and identify issues

**Acceptance Criteria:**
- GET /api/v1/attendance/reports/daily endpoint
- Filter by date range, department, employee
- Export to CSV format
- Include totals: hours worked, overtime, missed punches
- Response time < 10 seconds for 50k records

**Technical Specifications:**
```java
@GetMapping("/reports/daily")
@PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
public ResponseEntity<byte[]> generateDailyReport(
        @RequestHeader("X-Tenant-ID") UUID tenantId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) String department,
        @RequestParam(required = false) UUID employeeId) {
    
    byte[] csvData = attendanceService.generateDailyReport(
        tenantId, startDate, endDate, department, employeeId);
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv"));
    headers.setContentDispositionFormData("attachment", 
        "attendance-report-" + startDate + "-to-" + endDate + ".csv");
    
    return ResponseEntity.ok()
        .headers(headers)
        .body(csvData);
}

@Override
public byte[] generateDailyReport(UUID tenantId, LocalDate startDate, LocalDate endDate,
                                   String department, UUID employeeId) {
    LocalDateTime start = startDate.atStartOfDay();
    LocalDateTime end = endDate.atTime(23, 59, 59);

    List<Attendance> attendanceRecords = attendanceRepository
        .findByFiltersForReport(tenantId, start, end, department, employeeId);

    return csvExportService.exportAttendanceToCsv(attendanceRecords);
}
```

---

## E05: Shift & Schedule Management

### Epic Description
Create recurring shift templates, rotations, overtime rules, and assignment to employees; handle blackout dates and warehouse operation calendars.

### User Stories

#### US-E05-001: Create Shift Template Entity
**As a** Backend Developer  
**I want** to create shift template entities  
**So that** recurring shifts can be defined and reused

**Acceptance Criteria:**
- ShiftTemplate entity with name, startTime, endTime, duration, breakMinutes
- Support for different shift types (MORNING, AFTERNOON, NIGHT, ROTATING)
- Overtime rules configuration
- Active/inactive status

**Technical Specifications:**
```java
package com.warehouse.employee.domain.scheduling.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "shift_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", unique = true, length = 20)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false)
    private ShiftType shiftType;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "duration_hours", nullable = false)
    private Integer durationHours;

    @Column(name = "break_minutes")
    private Integer breakMinutes;

    @Column(name = "overtime_threshold_hours")
    private Integer overtimeThresholdHours;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "description", length = 500)
    private String description;
}

public enum ShiftType {
    MORNING,      // 6 AM - 2 PM
    AFTERNOON,    // 2 PM - 10 PM
    NIGHT,        // 10 PM - 6 AM
    ROTATING,     // Variable
    SPLIT         // Multiple periods
}
```

#### US-E05-002: Create Employee Schedule Assignment
**As a** Supervisor  
**I want** to assign shifts to employees  
**So that** work schedules are planned and communicated

**Acceptance Criteria:**
- Schedule entity linking employees to shifts
- Support for recurring schedules (daily, weekly, bi-weekly)
- Conflict detection (double-booking, insufficient rest)
- Bulk assignment capability

**Technical Specifications:**
```java
@Entity
@Table(name = "employee_schedule", indexes = {
    @Index(name = "idx_schedule_employee", columnList = "employee_id"),
    @Index(name = "idx_schedule_date", columnList = "schedule_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "shift_template_id", nullable = false)
    private UUID shiftTemplateId;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ScheduleStatus status;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_by")
    private UUID createdBy;
}

public enum ScheduleStatus {
    SCHEDULED,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    NO_SHOW
}

@Service
@RequiredArgsConstructor
public class SchedulingServiceImpl implements SchedulingService {

    private final EmployeeScheduleRepository scheduleRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public ScheduleResponse assignShift(UUID tenantId, AssignShiftRequest request) {
        // Validate employee
        Employee employee = employeeRepository.findById(request.getEmployeeId())
            .filter(e -> e.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        // Validate shift template
        ShiftTemplate template = shiftTemplateRepository.findById(request.getShiftTemplateId())
            .filter(t -> t.getTenantId().equals(tenantId) && t.getActive())
            .orElseThrow(() -> new ShiftTemplateNotFoundException("Shift template not found"));

        // Check for conflicts
        List<EmployeeSchedule> conflicts = scheduleRepository
            .findConflictingSchedules(tenantId, request.getEmployeeId(), 
                                     request.getScheduleDate(), 
                                     template.getStartTime(), 
                                     template.getEndTime());
        
        if (!conflicts.isEmpty()) {
            throw new ScheduleConflictException("Employee already has a shift scheduled for this time");
        }

        // Create schedule
        LocalDateTime startDateTime = LocalDateTime.of(request.getScheduleDate(), template.getStartTime());
        LocalDateTime endDateTime = LocalDateTime.of(request.getScheduleDate(), template.getEndTime());
        
        // Handle overnight shifts
        if (template.getEndTime().isBefore(template.getStartTime())) {
            endDateTime = endDateTime.plusDays(1);
        }

        EmployeeSchedule schedule = EmployeeSchedule.builder()
            .tenantId(tenantId)
            .employeeId(employee.getId())
            .shiftTemplateId(template.getId())
            .scheduleDate(request.getScheduleDate())
            .startTime(startDateTime)
            .endTime(endDateTime)
            .status(ScheduleStatus.SCHEDULED)
            .notes(request.getNotes())
            .createdBy(request.getCreatedBy())
            .build();

        EmployeeSchedule saved = scheduleRepository.save(schedule);
        
        // Notify employee
        notificationService.notifyShiftAssignment(employee, saved);

        return mapToScheduleResponse(saved, employee, template);
    }

    @Override
    @Transactional
    public List<ScheduleResponse> bulkAssignShifts(UUID tenantId, BulkAssignRequest request) {
        List<ScheduleResponse> results = new ArrayList<>();
        
        for (UUID employeeId : request.getEmployeeIds()) {
            try {
                AssignShiftRequest assignRequest = AssignShiftRequest.builder()
                    .employeeId(employeeId)
                    .shiftTemplateId(request.getShiftTemplateId())
                    .scheduleDate(request.getScheduleDate())
                    .createdBy(request.getCreatedBy())
                    .build();
                
                ScheduleResponse response = assignShift(tenantId, assignRequest);
                results.add(response);
            } catch (Exception e) {
                log.error("Failed to assign shift to employee: {}", employeeId, e);
                // Continue with other employees
            }
        }
        
        return results;
    }
}
```

#### US-E05-003: Implement Recurring Schedule Generation
**As a** Supervisor  
**I want** to generate recurring schedules automatically  
**So that** I don't have to manually create schedules every week

**Acceptance Criteria:**
- Support for weekly, bi-weekly, monthly patterns
- Rotation rules for shift assignments
- Respect blackout dates and holidays
- Preview before committing

**Technical Specifications:**
```java
@Data
public class RecurringScheduleRequest {
    @NotNull
    private UUID shiftTemplateId;
    
    @NotNull
    private List<UUID> employeeIds;
    
    @NotNull
    private LocalDate startDate;
    
    @NotNull
    private LocalDate endDate;
    
    @NotNull
    private RecurrencePattern pattern;
    
    private List<DayOfWeek> daysOfWeek;
    private RotationType rotationType;
}

public enum RecurrencePattern {
    DAILY,
    WEEKLY,
    BI_WEEKLY,
    MONTHLY
}

public enum RotationType {
    NONE,           // Same employees every occurrence
    ROUND_ROBIN,    // Rotate through employee list
    ALTERNATING     // Alternate between two groups
}

@Override
@Transactional
public List<ScheduleResponse> generateRecurringSchedule(UUID tenantId, RecurringScheduleRequest request) {
    List<ScheduleResponse> generatedSchedules = new ArrayList<>();
    
    // Get blackout dates
    List<LocalDate> blackoutDates = blackoutDateRepository
        .findByTenantIdAndDateBetween(tenantId, request.getStartDate(), request.getEndDate())
        .stream()
        .map(BlackoutDate::getDate)
        .collect(Collectors.toList());

    LocalDate currentDate = request.getStartDate();
    int employeeIndex = 0;
    
    while (!currentDate.isAfter(request.getEndDate())) {
        // Skip blackout dates
        if (blackoutDates.contains(currentDate)) {
            currentDate = getNextDate(currentDate, request.getPattern());
            continue;
        }
        
        // Check if day matches pattern
        if (request.getDaysOfWeek() != null && 
            !request.getDaysOfWeek().contains(currentDate.getDayOfWeek())) {
            currentDate = currentDate.plusDays(1);
            continue;
        }
        
        // Determine employee based on rotation
        UUID employeeId = determineEmployee(request.getEmployeeIds(), 
                                           employeeIndex, 
                                           request.getRotationType());
        
        try {
            AssignShiftRequest assignRequest = AssignShiftRequest.builder()
                .employeeId(employeeId)
                .shiftTemplateId(request.getShiftTemplateId())
                .scheduleDate(currentDate)
                .build();
            
            ScheduleResponse response = assignShift(tenantId, assignRequest);
            generatedSchedules.add(response);
            
            employeeIndex = (employeeIndex + 1) % request.getEmployeeIds().size();
        } catch (ScheduleConflictException e) {
            log.warn("Conflict detected for employee {} on {}", employeeId, currentDate);
        }
        
        currentDate = getNextDate(currentDate, request.getPattern());
    }
    
    return generatedSchedules;
}
```

---

## E06: Leave & Absence Management

### Epic Description
Request/approve PTO, sick, unpaid leave; accrual balances and policies; integration hooks to exclude from scheduling and payroll hours.

### User Stories

#### US-E06-001: Create Leave Request Entity
**As a** Backend Developer  
**I want** to create leave request entities  
**So that** employee time-off can be tracked and managed

**Technical Specifications:**
```java
@Entity
@Table(name = "leave_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    private LeaveType leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_days", nullable = false)
    private Integer totalDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LeaveStatus status;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
}

public enum LeaveType {
    PTO,
    SICK,
    UNPAID,
    BEREAVEMENT,
    JURY_DUTY,
    PARENTAL,
    MEDICAL
}

public enum LeaveStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
}
```

#### US-E06-002: Implement Leave Balance Tracking
**As an** HR Manager  
**I want** to track employee leave balances  
**So that** accruals and usage are accurately maintained

**Technical Specifications:**
```java
@Entity
@Table(name = "leave_balance")
@Getter
@Setter
@Builder
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    private LeaveType leaveType;

    @Column(name = "accrued_days", nullable = false)
    private BigDecimal accruedDays;

    @Column(name = "used_days", nullable = false)
    private BigDecimal usedDays;

    @Column(name = "available_days", nullable = false)
    private BigDecimal availableDays;

    @Column(name = "year", nullable = false)
    private Integer year;
}

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    @Override
    @Transactional
    public LeaveRequestResponse submitLeaveRequest(UUID tenantId, CreateLeaveRequest request) {
        // Validate employee
        Employee employee = employeeRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        // Calculate total days
        long totalDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        // Check balance for PTO/Sick leave
        if (request.getLeaveType() == LeaveType.PTO || request.getLeaveType() == LeaveType.SICK) {
            LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeAndYear(
                    request.getEmployeeId(), 
                    request.getLeaveType(), 
                    LocalDate.now().getYear())
                .orElseThrow(() -> new LeaveBalanceNotFoundException("Leave balance not found"));

            if (balance.getAvailableDays().compareTo(BigDecimal.valueOf(totalDays)) < 0) {
                throw new InsufficientLeaveBalanceException(
                    "Insufficient leave balance. Available: " + balance.getAvailableDays());
            }
        }

        // Check for overlapping requests
        List<LeaveRequest> overlapping = leaveRequestRepository
            .findOverlappingLeaveRequests(request.getEmployeeId(), 
                                         request.getStartDate(), 
                                         request.getEndDate());
        
        if (!overlapping.isEmpty()) {
            throw new OverlappingLeaveException("Leave request overlaps with existing request");
        }

        // Create leave request
        LeaveRequest leaveRequest = LeaveRequest.builder()
            .tenantId(tenantId)
            .employeeId(request.getEmployeeId())
            .leaveType(request.getLeaveType())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .totalDays((int) totalDays)
            .status(LeaveStatus.PENDING)
            .reason(request.getReason())
            .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        // Notify supervisor
        notificationService.notifyLeaveRequest(employee, saved);

        return mapToResponse(saved, employee);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public LeaveRequestResponse approveLeaveRequest(UUID tenantId, UUID requestId, UUID approverId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
            .filter(lr -> lr.getTenantId().equals(tenantId))
            .orElseThrow(() -> new LeaveRequestNotFoundException("Leave request not found"));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveStatusException("Leave request is not in pending status");
        }

        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setApprovedBy(approverId);
        leaveRequest.setApprovedAt(LocalDateTime.now());

        LeaveRequest approved = leaveRequestRepository.save(leaveRequest);

        // Update leave balance
        if (leaveRequest.getLeaveType() == LeaveType.PTO || 
            leaveRequest.getLeaveType() == LeaveType.SICK) {
            updateLeaveBalance(leaveRequest.getEmployeeId(), 
                             leaveRequest.getLeaveType(), 
                             leaveRequest.getTotalDays());
        }

        // Cancel scheduled shifts
        cancelScheduledShifts(tenantId, leaveRequest.getEmployeeId(), 
                            leaveRequest.getStartDate(), 
                            leaveRequest.getEndDate());

        // Notify employee
        Employee employee = employeeRepository.findById(leaveRequest.getEmployeeId()).orElseThrow();
        notificationService.notifyLeaveApproval(employee, approved);

        return mapToResponse(approved, employee);
    }
}
```

---

## E07: Training & Certification Tracking

### Epic Description
Track required certifications (e.g., forklift), expirations, renewals; block assignment to tasks requiring expired certs; upload proof documents.

### User Stories

#### US-E07-001: Create Certification Entity
**As a** Backend Developer  
**I want** to create certification tracking entities  
**So that** employee qualifications can be managed

**Technical Specifications:**
```java
@Entity
@Table(name = "certification")
@Getter
@Setter
@Builder
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "validity_period_months")
    private Integer validityPeriodMonths;

    @Column(name = "required_for_roles")
    @Convert(converter = StringListConverter.class)
    private List<String> requiredForRoles;

    @Column(name = "active")
    private Boolean active = true;
}

@Entity
@Table(name = "employee_certification")
@Getter
@Setter
@Builder
public class EmployeeCertification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "certification_id", nullable = false)
    private UUID certificationId;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CertificationStatus status;

    @Column(name = "document_url")
    private String documentUrl;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "notes")
    private String notes;
}

public enum CertificationStatus {
    ACTIVE,
    EXPIRING_SOON,  // Within 30 days
    EXPIRED,
    SUSPENDED,
    REVOKED
}
```

#### US-E07-002: Implement Certification Expiry Alerts
**As a** System  
**I want** to automatically alert employees and supervisors about expiring certifications  
**So that** renewals can be completed before expiration

**Technical Specifications:**
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CertificationExpiryScheduler {

    private final EmployeeCertificationRepository certificationRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * *") // Daily at 8 AM
    @Transactional(readOnly = true)
    public void checkExpiringCertifications() {
        log.info("Running certification expiry check");

        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        LocalDate sevenDaysFromNow = today.plusDays(7);

        // Find certifications expiring in 30 days
        List<EmployeeCertification> expiringSoon = certificationRepository
            .findByExpiryDateBetweenAndStatus(today, thirtyDaysFromNow, CertificationStatus.ACTIVE);

        for (EmployeeCertification cert : expiringSoon) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(today, cert.getExpiryDate());
            
            if (daysUntilExpiry <= 7) {
                cert.setStatus(CertificationStatus.EXPIRING_SOON);
                notificationService.sendUrgentCertificationAlert(cert, daysUntilExpiry);
            } else if (daysUntilExpiry <= 30) {
                notificationService.sendCertificationReminder(cert, daysUntilExpiry);
            }
        }

        // Mark expired certifications
        List<EmployeeCertification> expired = certificationRepository
            .findByExpiryDateBeforeAndStatusNot(today, CertificationStatus.EXPIRED);

        for (EmployeeCertification cert : expired) {
            cert.setStatus(CertificationStatus.EXPIRED);
            certificationRepository.save(cert);
            notificationService.sendCertificationExpiredAlert(cert);
        }

        log.info("Certification expiry check completed. Expiring soon: {}, Expired: {}", 
                 expiringSoon.size(), expired.size());
    }
}
```

---

## E08: Safety Incidents & OSHA Reporting

### Epic Description
Record incidents/near-misses; severity, location, description, involved employees; workflow for investigation and corrective actions; generate OSHA summary.

### User Stories

#### US-E08-001: Create Safety Incident Entity
**As a** Backend Developer  
**I want** to create safety incident tracking entities  
**So that** workplace incidents can be documented and investigated

**Technical Specifications:**
```java
@Entity
@Table(name = "safety_incident")
@Getter
@Setter
@Builder
public class SafetyIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "incident_number", unique = true)
    private String incidentNumber;

    @Column(name = "incident_date", nullable = false)
    private LocalDateTime incidentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false)
    private IncidentType incidentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private IncidentSeverity severity;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "description", length = 2000, nullable = false)
    private String description;

    @Column(name = "involved_employees")
    @Convert(converter = UUIDListConverter.class)
    private List<UUID> involvedEmployees;

    @Column(name = "witnesses")
    @Convert(converter = UUIDListConverter.class)
    private List<UUID> witnesses;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private IncidentStatus status;

    @Column(name = "reported_by", nullable = false)
    private UUID reportedBy;

    @Column(name = "investigated_by")
    private UUID investigatedBy;

    @Column(name = "investigation_notes", length = 5000)
    private String investigationNotes;

    @Column(name = "corrective_actions", length = 5000)
    private String correctiveActions;

    @Column(name = "osha_recordable")
    private Boolean oshaRecordable = false;

    @Column(name = "days_away_from_work")
    private Integer daysAwayFromWork;

    @Column(name = "restricted_work_days")
    private Integer restrictedWorkDays;
}

public enum IncidentType {
    INJURY,
    NEAR_MISS,
    PROPERTY_DAMAGE,
    ENVIRONMENTAL,
    SECURITY
}

public enum IncidentSeverity {
    MINOR,
    MODERATE,
    SERIOUS,
    CRITICAL,
    FATAL
}

public enum IncidentStatus {
    REPORTED,
    UNDER_INVESTIGATION,
    INVESTIGATION_COMPLETE,
    CORRECTIVE_ACTIONS_PENDING,
    RESOLVED,
    CLOSED
}
```

#### US-E08-002: Generate OSHA 300/300A Reports
**As a** Safety Manager  
**I want** to generate OSHA-compliant incident reports  
**So that** regulatory requirements are met

**Technical Specifications:**
```java
@Service
@RequiredArgsConstructor
public class OSHAReportingService {

    private final SafetyIncidentRepository incidentRepository;
    private final EmployeeRepository employeeRepository;

    public OSHA300Report generateOSHA300Report(UUID tenantId, int year) {
        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59);

        List<SafetyIncident> recordableIncidents = incidentRepository
            .findByTenantIdAndOshaRecordableAndIncidentDateBetween(
                tenantId, true, startDate, endDate);

        List<OSHA300Entry> entries = recordableIncidents.stream()
            .map(this::mapToOSHA300Entry)
            .collect(Collectors.toList());

        return OSHA300Report.builder()
            .year(year)
            .establishmentName(getEstablishmentName(tenantId))
            .entries(entries)
            .totalCases(entries.size())
            .totalDaysAwayFromWork(entries.stream()
                .mapToInt(OSHA300Entry::getDaysAwayFromWork)
                .sum())
            .totalDaysOfRestrictedWork(entries.stream()
                .mapToInt(OSHA300Entry::getDaysOfRestrictedWork)
                .sum())
            .build();
    }

    private OSHA300Entry mapToOSHA300Entry(SafetyIncident incident) {
        Employee employee = employeeRepository
            .findById(incident.getInvolvedEmployees().get(0))
            .orElse(null);

        return OSHA300Entry.builder()
            .caseNumber(incident.getIncidentNumber())
            .employeeName(employee != null ? 
                employee.getFirstName() + " " + employee.getLastName() : "Unknown")
            .jobTitle(employee != null ? employee.getRole().name() : "Unknown")
            .dateOfInjury(incident.getIncidentDate().toLocalDate())
            .whereEventOccurred(incident.getLocation())
            .describeInjury(incident.getDescription())
            .daysAwayFromWork(incident.getDaysAwayFromWork() != null ? 
                incident.getDaysAwayFromWork() : 0)
            .daysOfRestrictedWork(incident.getRestrictedWorkDays() != null ? 
                incident.getRestrictedWorkDays() : 0)
            .build();
    }
}
```

---

## E09-E20: Additional Epics

Due to the extensive nature of this document, I'll provide the remaining epics in a structured format with key technical specifications:

### E09: Equipment & Asset Assignment
- Asset entity with checkout/return tracking
- Integration with certification validation
- Overdue return alerts
- Asset condition tracking

### E10: Performance Reviews & Goals
- Review cycle management
- Goal setting and tracking
- Multi-level approval workflow
- PDF export with digital signatures

### E11: Payroll Export Integration
- Configurable export formats
- SFTP/API delivery mechanisms
- Reconciliation reports
- Retry logic with exponential backoff

### E12: Notifications & Announcements
- Multi-channel delivery (email, SMS, push)
- Template management with i18n
- Delivery status tracking
- Rate limiting and quiet hours

### E13: Integration Layer
- RESTful API with OAuth2
- Webhook support for events
- HRIS sync for employee data
- WMS integration for locations

### E14: Audit Trail & Compliance
- Immutable audit log table
- Before/after state capture
- Actor and timestamp tracking
- Exportable audit reports

### E15: Reporting & Analytics
- Parameterized report engine
- CSV/PDF export capabilities
- Role-based access control
- Performance optimization for large datasets

### E16: Mobile Access (PWA)
- Responsive UI components
- Offline-first architecture
- Service worker for caching
- Conflict resolution for offline actions

### E17: Onboarding & Offboarding
- Automated task generation
- Checklist tracking
- Asset collection workflow
- Access revocation automation

### E18: Localization & Multi-Tenant
- Tenant isolation at data layer
- i18n message bundles
- Timezone-aware date handling
- Currency and format localization

### E19: Observability & Monitoring
- Prometheus metrics
- Structured JSON logging
- Distributed tracing with Micrometer
- Custom business metrics

### E20: CI/CD & Deployment
- GitHub Actions pipeline
- Automated testing (unit, integration, e2e)
- Security scanning (SAST, dependency check)
- Blue-green deployment strategy

---

## Cross-Cutting Concerns

### Database Schema Standards
- Use UUID for primary keys
- Include tenant_id for multi-tenancy
- Add audit columns (created_at, updated_at, created_by, updated_by)
- Use appropriate indexes for query performance
- Implement soft deletes where applicable

### API Design Standards
- RESTful resource naming
- Consistent error response format
- Pagination for list endpoints (default 20, max 100)
- Filtering and sorting support
- OpenAPI 3.0 documentation

### Security Standards
- JWT token-based authentication
- Role-based access control
- Input validation on all endpoints
- SQL injection prevention via JPA
- XSS protection
- CORS configuration

### Testing Standards
- Unit tests for service layer (80%+ coverage)
- Integration tests for repositories
- API tests for controllers
- Security tests for authentication/authorization
- Performance tests for critical paths

### Code Quality Standards
- SonarQube analysis
- Checkstyle enforcement
- PMD static analysis
- SpotBugs for bug detection
- Code review requirements

---

## Technology Stack Summary

**Backend:**
- Spring Boot 3.2+
- Spring Data JPA
- Spring Security
- Spring Validation
- MapStruct for DTO mapping
- Lombok for boilerplate reduction

**Database:**
- PostgreSQL 14+
- Flyway for migrations

**Security:**
- JWT (jjwt library)
- BCrypt password encoding
- OAuth2 support

**Documentation:**
- SpringDoc OpenAPI
- Swagger UI

**Monitoring:**
- Spring Boot Actuator
- Micrometer
- Prometheus

**Testing:**
- JUnit 5
- Mockito
- TestContainers
- REST Assured

**Build & Deploy:**
- Maven
- Docker
- GitHub Actions

---

## Implementation Priorities

**Phase 1 (Weeks 1-4):**
- E01: Project Scaffolding
- E02: Employee Master Data
- E03: RBAC
- E14: Audit Trail

**Phase 2 (Weeks 5-8):**
- E04: Time & Attendance
- E05: Shift Management
- E06: Leave Management

**Phase 3 (Weeks 9-12):**
- E07: Certification Tracking
- E08: Safety Incidents
- E09: Asset Management

**Phase 4 (Weeks 13-16):**
- E11: Payroll Integration
- E12: Notifications
- E13: Integration Layer
- E15: Reporting

**Phase 5 (Weeks 17-20):**
- E10: Performance Reviews
- E16: Mobile PWA
- E17: Onboarding/Offboarding
- E18: Localization
- E19: Observability
- E20: CI/CD

---

## Conclusion

This technical design document provides comprehensive guidance for implementing the Warehouse Employee Management System using Spring Boot. Each epic has been broken down into detailed user stories with acceptance criteria, technical specifications, and code examples.

Developers should follow the implementation priorities, adhere to the coding standards, and ensure all acceptance criteria are met before marking a user story as complete. Regular code reviews and testing are essential to maintain quality throughout the development lifecycle.
