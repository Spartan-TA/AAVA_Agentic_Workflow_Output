# Low-Level Technical Design Document
## Warehouse Employee Management System

### Document Overview
This document provides comprehensive low-level technical design specifications for all epics in the Warehouse Employee Management System. The system is built using Spring Boot 3.x with Maven, following industry best practices and standards.

---

## E01: Project Scaffolding & Domain Setup

### Section: Project Architecture Overview

**Description:**
Establish a multi-module Spring Boot Maven project with clear separation of concerns following Domain-Driven Design (DDD) principles. The architecture follows a layered approach with distinct modules for each business domain.

**Design Specification:**
- Spring Boot 3.2.x with Java 17+
- Maven multi-module project structure
- Flyway for database migrations
- Spring Boot Actuator for monitoring
- Modular architecture with domain-driven design

**Sample Implementation:**

```xml
<!-- Parent pom.xml -->
<project>
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.warehouse</groupId>
    <artifactId>employee-management</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    
    <modules>
        <module>employee-service</module>
        <module>scheduling-service</module>
        <module>attendance-service</module>
        <module>safety-service</module>
        <module>common</module>
    </modules>
    
    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>
</project>
```

### Section: Package Structure

**Description:**
Standardized package structure following Spring Boot conventions with clear separation between layers.

**Design Specification:**
```
com.warehouse.employee
âââ config/              # Configuration classes
âââ controller/          # REST controllers
âââ dto/                 # Data Transfer Objects
âââ entity/              # JPA entities
âââ repository/          # Spring Data repositories
âââ service/             # Business logic
â   âââ impl/           # Service implementations
âââ exception/           # Custom exceptions
âââ mapper/              # DTO-Entity mappers
âââ security/            # Security configurations
âââ util/                # Utility classes
```

### Section: Base Configuration

**Description:**
Core application configuration with Actuator, Flyway, and common properties.

**Sample Implementation:**

```yaml
# application.yml
spring:
  application:
    name: warehouse-employee-management
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration

server:
  port: 8080
  servlet:
    context-path: /api/v1

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

```java
// FlywayConfig.java
package com.warehouse.employee.config;

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

---

## E02: Employee Master Data (CRUD)

### Section: Domain Model

**Description:**
Employee entity represents the core domain model with comprehensive employee information including personal details, employment status, and organizational relationships.

**Design Specification:**
- JPA entity with soft delete support
- Unique constraint on badgeId
- Audit fields (createdAt, updatedAt, createdBy, updatedBy)
- Enum types for role, department, and status
- Bidirectional relationships with other entities

**Sample Implementation:**

```java
package com.warehouse.employee.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_badge_id", columnList = "badge_id", unique = true),
    @Index(name = "idx_department", columnList = "department"),
    @Index(name = "idx_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE employees SET deleted = true, deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "badge_id", nullable = false, unique = true, length = 20)
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
    
    @Enumerated(EnumType.STRING)
    @Column(name = "department", nullable = false, length = 100)
    private Department department;
    
    @Column(name = "shift_group", length = 50)
    private String shiftGroup;
    
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;
    
    @Column(name = "termination_date")
    private LocalDate terminationDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmployeeStatus status;
    
    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @Version
    private Long version;
}

// Enums
public enum EmployeeRole {
    ADMIN, HR, SUPERVISOR, WORKER
}

public enum Department {
    RECEIVING, SHIPPING, PICKING, PACKING, QUALITY_CONTROL, MAINTENANCE, ADMINISTRATION
}

public enum EmployeeStatus {
    ACTIVE, INACTIVE, ON_LEAVE, TERMINATED
}
```

### Section: Repository Layer

**Description:**
Spring Data JPA repository with custom query methods for filtering, pagination, and search capabilities.

**Sample Implementation:**

```java
package com.warehouse.employee.repository;

import com.warehouse.employee.entity.Employee;
import com.warehouse.employee.entity.EmployeeStatus;
import com.warehouse.employee.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>, 
                                            JpaSpecificationExecutor<Employee> {
    
    Optional<Employee> findByBadgeId(String badgeId);
    
    boolean existsByBadgeId(String badgeId);
    
    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);
    
    Page<Employee> findByDepartment(Department department, Pageable pageable);
    
    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.badgeId) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Employee> searchEmployees(@Param("search") String search, Pageable pageable);
}
```

### Section: Service Layer

**Description:**
Business logic layer implementing CRUD operations with validation, error handling, and business rules enforcement.

**Sample Implementation:**

```java
package com.warehouse.employee.service.impl;

import com.warehouse.employee.dto.*;
import com.warehouse.employee.entity.Employee;
import com.warehouse.employee.exception.*;
import com.warehouse.employee.mapper.EmployeeMapper;
import com.warehouse.employee.repository.EmployeeRepository;
import com.warehouse.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    
    @Override
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO request) {
        log.info("Creating employee with badgeId: {}", request.getBadgeId());
        
        if (employeeRepository.existsByBadgeId(request.getBadgeId())) {
            throw new DuplicateBadgeIdException(
                "Employee with badgeId " + request.getBadgeId() + " already exists"
            );
        }
        
        Employee employee = employeeMapper.toEntity(request);
        Employee savedEmployee = employeeRepository.save(employee);
        
        log.info("Employee created successfully with id: {}", savedEmployee.getId());
        return employeeMapper.toResponseDTO(savedEmployee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(UUID id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
        return employeeMapper.toResponseDTO(employee);
    }
    
    @Override
    public void deleteEmployee(UUID id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
        employeeRepository.delete(employee);
        log.info("Employee soft-deleted with id: {}", id);
    }
}
```

### Section: Controller Layer

**Description:**
RESTful API endpoints for employee CRUD operations with OpenAPI documentation.

**Sample Implementation:**

```java
package com.warehouse.employee.controller;

import com.warehouse.employee.dto.*;
import com.warehouse.employee.service.EmployeeService;
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

import java.util.UUID;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "APIs for managing warehouse employees")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Create new employee")
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @Valid @RequestBody EmployeeRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(employeeService.createEmployee(request));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<EmployeeResponseDTO> getEmployee(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get all employees with pagination")
    public ResponseEntity<Page<EmployeeResponseDTO>> getAllEmployees(Pageable pageable) {
        return ResponseEntity.ok(employeeService.getAllEmployees(pageable));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update employee")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeRequestDTO request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete employee")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
```