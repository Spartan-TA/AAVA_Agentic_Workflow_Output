# Warehouse EMS - Low-Level Technical Design Document

## Document Information
- **Project**: Warehouse Employee Management System (EMS)
- **Version**: 1.0
- **Date**: 2024
- **Framework**: Spring Boot 3.x
- **Java Version**: 17+
- **Build Tool**: Maven

---

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [User Story Technical Designs](#user-story-technical-designs)
5. [Cross-Cutting Concerns](#cross-cutting-concerns)
6. [Deployment & Configuration](#deployment-configuration)

---

## Architecture Overview

### System Architecture
The Warehouse EMS follows a layered architecture pattern:

```
âââââââââââââââââââââââââââââââââââââââââââ
â         Presentation Layer              â
â  (REST Controllers, DTOs, Validators)   â
âââââââââââââââââââââââââââââââââââââââââââ
                  â
âââââââââââââââââââââââââââââââââââââââââââ
â          Service Layer                  â
â  (Business Logic, Orchestration)        â
âââââââââââââââââââââââââââââââââââââââââââ
                  â
âââââââââââââââââââââââââââââââââââââââââââ
â         Repository Layer                â
â  (Data Access, JPA Repositories)        â
âââââââââââââââââââââââââââââââââââââââââââ
                  â
âââââââââââââââââââââââââââââââââââââââââââ
â          Database Layer                 â
â  (PostgreSQL/MySQL with Flyway)         â
âââââââââââââââââââââââââââââââââââââââââââ
```

### Design Principles
- **SOLID Principles**: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
- **DRY (Don't Repeat Yourself)**: Reusable components and utilities
- **Separation of Concerns**: Clear boundaries between layers
- **Domain-Driven Design**: Rich domain models with business logic

---

## Technology Stack

### Core Framework
- **Spring Boot**: 3.2.x
- **Spring Data JPA**: For data persistence
- **Spring Security**: For authentication and authorization
- **Spring Validation**: For input validation
- **Spring Actuator**: For monitoring and health checks

### Database
- **Primary**: PostgreSQL 15+ (recommended) or MySQL 8+
- **Migration**: Flyway or Liquibase
- **Connection Pool**: HikariCP

### Security
- **JWT**: For stateless authentication
- **OAuth2**: Optional integration
- **BCrypt**: For password hashing

### Documentation
- **SpringDoc OpenAPI**: API documentation
- **Swagger UI**: Interactive API testing

### Testing
- **JUnit 5**: Unit testing
- **Mockito**: Mocking framework
- **Spring Boot Test**: Integration testing
- **TestContainers**: Database testing

### Build & Deployment
- **Maven**: Build automation
- **Docker**: Containerization
- **Spring Boot DevTools**: Development productivity

---

## Project Structure

```
warehouse-ems/
âââ src/
â   âââ main/
â   â   âââ java/
â   â   â   âââ com/
â   â   â       âââ warehouse/
â   â   â           âââ ems/
â   â   â               âââ WarehouseEmsApplication.java
â   â   â               âââ config/
â   â   â               â   âââ SecurityConfig.java
â   â   â               â   âââ JpaConfig.java
â   â   â               â   âââ OpenApiConfig.java
â   â   â               â   âââ AsyncConfig.java
â   â   â               âââ common/
â   â   â               â   âââ dto/
â   â   â               â   â   âââ PageResponse.java
â   â   â               â   â   âââ ErrorResponse.java
â   â   â               â   âââ exception/
â   â   â               â   â   âââ GlobalExceptionHandler.java
â   â   â               â   â   âââ ResourceNotFoundException.java
â   â   â               â   â   âââ BusinessException.java
â   â   â               â   âââ util/
â   â   â               â       âââ DateTimeUtil.java
â   â   â               â       âââ ValidationUtil.java
â   â   â               âââ employee/
â   â   â               â   âââ domain/
â   â   â               â   â   âââ Employee.java
â   â   â               â   âââ dto/
â   â   â               â   â   âââ EmployeeRequest.java
â   â   â               â   â   âââ EmployeeResponse.java
â   â   â               â   âââ repository/
â   â   â               â   â   âââ EmployeeRepository.java
â   â   â               â   âââ service/
â   â   â               â   â   âââ EmployeeService.java
â   â   â               â   â   âââ EmployeeServiceImpl.java
â   â   â               â   âââ controller/
â   â   â               â       âââ EmployeeController.java
â   â   â               âââ attendance/
â   â   â               â   âââ domain/
â   â   â               â   âââ dto/
â   â   â               â   âââ repository/
â   â   â               â   âââ service/
â   â   â               â   âââ controller/
â   â   â               âââ scheduling/
â   â   â               âââ leave/
â   â   â               âââ training/
â   â   â               âââ safety/
â   â   â               âââ asset/
â   â   â               âââ performance/
â   â   â               âââ payroll/
â   â   â               âââ notification/
â   â   â               âââ integration/
â   â   â               âââ audit/
â   â   â               âââ reporting/
â   â   â               âââ security/
â   â   â                   âââ jwt/
â   â   â                   âââ service/
â   â   â                   âââ filter/
â   â   âââ resources/
â   â       âââ application.yml
â   â       âââ application-dev.yml
â   â       âââ application-prod.yml
â   â       âââ db/
â   â           âââ migration/
â   â               âââ V1__init_schema.sql
â   â               âââ V2__employee_tables.sql
â   â               âââ V3__attendance_tables.sql
â   âââ test/
â       âââ java/
â           âââ com/
â               âââ warehouse/
â                   âââ ems/
â                       âââ employee/
â                       âââ attendance/
â                       âââ integration/
âââ pom.xml
âââ Dockerfile
âââ docker-compose.yml
âââ README.md
```

---

## User Story Technical Designs

---

## E01: Project Scaffolding & Domain Setup

### Section: Project Initialization

**Description**: 
Establish a standardized Spring Boot project with Maven, configured with essential dependencies, base package structure, and development tools. This foundation ensures consistency across all modules and accelerates feature delivery.

**Design Specification**:

#### Maven Dependencies (pom.xml)
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>

<dependencies>
    <!-- Core Spring Boot -->
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
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
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
    
    <!-- Documentation -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.2.0</version>
    </dependency>
    
    <!-- Utilities -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

#### Application Configuration (application.yml)
```yaml
spring:
  application:
    name: warehouse-ems
  
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
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
  
  security:
    user:
      name: admin
      password: ${ADMIN_PASSWORD:changeme}

server:
  port: 8080
  servlet:
    context-path: /api
  error:
    include-message: always
    include-binding-errors: always

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method

logging:
  level:
    com.warehouse.ems: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

**Sample Implementation**:

```java
package com.warehouse.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class WarehouseEmsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

#### Base Entity Class
```java
package com.warehouse.ems.common.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @CreatedBy
    @Column(updatable = false)
    private String createdBy;
    
    @LastModifiedBy
    private String lastModifiedBy;
    
    @Version
    private Long version;
    
    @Column(nullable = false)
    private Boolean deleted = false;
}
```

#### Initial Database Migration (V1__init_schema.sql)
```sql
-- Create audit log table
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    before_state JSONB,
    after_state JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT
);

CREATE INDEX idx_audit_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_timestamp ON audit_log(timestamp);
CREATE INDEX idx_audit_actor ON audit_log(actor);
```

---

## E02: Employee Master Data (CRUD)

### Section: Domain Model Design

**Description**: 
The Employee entity serves as the core domain model representing warehouse employees. It includes comprehensive employee information with support for soft deletes, audit trails, and role-based access control.

**Design Specification**:

#### Entity Design
- **Table Name**: employees
- **Primary Key**: id (Long, auto-generated)
- **Unique Constraints**: badgeId, email
- **Soft Delete**: deleted flag
- **Audit Fields**: createdAt, updatedAt, createdBy, lastModifiedBy

#### Domain Model
```java
package com.warehouse.ems.employee.domain;

import com.warehouse.ems.common.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_badge_id", columnList = "badge_id"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_department", columnList = "department"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {
    
    @Column(name = "badge_id", unique = true, nullable = false, length = 20)
    private String badgeId;
    
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    
    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private EmployeeRole role;
    
    @Column(name = "department", nullable = false, length = 100)
    private String department;
    
    @Column(name = "shift_group", length = 50)
    private String shiftGroup;
    
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;
    
    @Column(name = "termination_date")
    private LocalDate terminationDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmployeeStatus status;
    
    @Column(name = "supervisor_id")
    private Long supervisorId;
    
    @Column(name = "location", length = 100)
    private String location;
    
    @Column(name = "job_title", length = 100)
    private String jobTitle;
    
    @Column(name = "emergency_contact_name", length = 200)
    private String emergencyContactName;
    
    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;
    
    // Computed field
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
```

#### Enumerations
```java
package com.warehouse.ems.employee.domain;

public enum EmployeeRole {
    ADMIN,
    HR,
    SUPERVISOR,
    WORKER,
    SAFETY_OFFICER,
    ASSET_MANAGER
}

public enum EmployeeStatus {
    ACTIVE,
    INACTIVE,
    ON_LEAVE,
    TERMINATED
}
```

### Section: Data Transfer Objects (DTOs)

**Description**: 
DTOs provide a clean separation between the domain model and API contracts, enabling validation, versioning, and security.

**Design Specification**:

```java
package com.warehouse.ems.employee.dto;

import com.warehouse.ems.employee.domain.EmployeeRole;
import com.warehouse.ems.employee.domain.EmployeeStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {
    
    @NotBlank(message = "Badge ID is required")
    @Size(max = 20, message = "Badge ID must not exceed 20 characters")
    private String badgeId;
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @Pattern(regexp = "^\+?[1-9]\d{1,14}$", message = "Phone number must be valid")
    private String phoneNumber;
    
    @NotNull(message = "Role is required")
    private EmployeeRole role;
    
    @NotBlank(message = "Department is required")
    private String department;
    
    private String shiftGroup;
    
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
    
    private LocalDate terminationDate;
    
    @NotNull(message = "Status is required")
    private EmployeeStatus status;
    
    private Long supervisorId;
    
    private String location;
    
    private String jobTitle;
    
    private String emergencyContactName;
    
    private String emergencyContactPhone;
}

@Data
@Builder
public class EmployeeResponse {
    private Long id;
    private String badgeId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private EmployeeRole role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private LocalDate terminationDate;
    private EmployeeStatus status;
    private Long supervisorId;
    private String supervisorName;
    private String location;
    private String jobTitle;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
@Builder
public class EmployeeFilterRequest {
    private String badgeId;
    private String name;
    private String email;
    private EmployeeRole role;
    private String department;
    private EmployeeStatus status;
    private LocalDate hireDateFrom;
    private LocalDate hireDateTo;
    private int page = 0;
    private int size = 20;
    private String sortBy = "lastName";
    private String sortDirection = "ASC";
}
```

### Section: Repository Layer

**Description**: 
Spring Data JPA repositories provide database access with custom query methods for complex filtering and searching.

**Design Specification**:

```java
package com.warehouse.ems.employee.repository;

import com.warehouse.ems.employee.domain.Employee;
import com.warehouse.ems.employee.domain.EmployeeRole;
import com.warehouse.ems.employee.domain.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, 
                                            JpaSpecificationExecutor<Employee> {
    
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    
    Optional<Employee> findByEmailAndDeletedFalse(String email);
    
    boolean existsByBadgeIdAndDeletedFalse(String badgeId);
    
    boolean existsByEmailAndDeletedFalse(String email);
    
    Page<Employee> findByDeletedFalse(Pageable pageable);
    
    List<Employee> findByDepartmentAndStatusAndDeletedFalse(
        String department, 
        EmployeeStatus status
    );
    
    List<Employee> findBySupervisorIdAndDeletedFalse(Long supervisorId);
    
    @Query("SELECT e FROM Employee e WHERE e.deleted = false " +
           "AND (:role IS NULL OR e.role = :role) " +
           "AND (:department IS NULL OR e.department = :department) " +
           "AND (:status IS NULL OR e.status = :status)")
    Page<Employee> findByFilters(
        @Param("role") EmployeeRole role,
        @Param("department") String department,
        @Param("status") EmployeeStatus status,
        Pageable pageable
    );
    
    @Query("SELECT e FROM Employee e WHERE e.deleted = false " +
           "AND (LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(e.badgeId) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Employee> searchEmployees(@Param("search") String search, Pageable pageable);
}
```

### Section: Service Layer

**Description**: 
The service layer encapsulates business logic, validation, and orchestration between repositories and controllers.

**Design Specification**:

```java
package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface EmployeeService {
    
    EmployeeResponse createEmployee(EmployeeRequest request);
    
    EmployeeResponse getEmployeeById(Long id);
    
    EmployeeResponse getEmployeeByBadgeId(String badgeId);
    
    Page<EmployeeResponse> getAllEmployees(EmployeeFilterRequest filter);
    
    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);
    
    EmployeeResponse partialUpdateEmployee(Long id, EmployeeRequest request);
    
    void deleteEmployee(Long id);
    
    void softDeleteEmployee(Long id);
    
    List<EmployeeResponse> getEmployeesByDepartment(String department);
    
    List<EmployeeResponse> getEmployeesBySupervisor(Long supervisorId);
    
    Page<EmployeeResponse> searchEmployees(String search, int page, int size);
}
```

**Sample Implementation**:

```java
package com.warehouse.ems.employee.service;

import com.warehouse.ems.common.exception.ResourceNotFoundException;
import com.warehouse.ems.common.exception.BusinessException;
import com.warehouse.ems.employee.domain.Employee;
import com.warehouse.ems.employee.dto.*;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    
    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        log.info("Creating employee with badge ID: {}", request.getBadgeId());
        
        // Validate unique constraints
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(request.getBadgeId())) {
            throw new BusinessException("Employee with badge ID " + 
                request.getBadgeId() + " already exists");
        }
        
        if (employeeRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new BusinessException("Employee with email " + 
                request.getEmail() + " already exists");
        }
        
        // Validate supervisor exists if provided
        if (request.getSupervisorId() != null) {
            employeeRepository.findById(request.getSupervisorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Supervisor not found with ID: " + request.getSupervisorId()));
        }
        
        Employee employee = employeeMapper.toEntity(request);
        Employee savedEmployee = employeeRepository.save(employee);
        
        log.info("Employee created successfully with ID: {}", savedEmployee.getId());
        return employeeMapper.toResponse(savedEmployee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        log.debug("Fetching employee with ID: {}", id);
        
        Employee employee = employeeRepository.findById(id)
            .filter(e -> !e.getDeleted())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Employee not found with ID: " + id));
        
        return employeeMapper.toResponse(employee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByBadgeId(String badgeId) {
        log.debug("Fetching employee with badge ID: {}", badgeId);
        
        Employee employee = employeeRepository.findByBadgeIdAndDeletedFalse(badgeId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Employee not found with badge ID: " + badgeId));
        
        return employeeMapper.toResponse(employee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAllEmployees(EmployeeFilterRequest filter) {
        log.debug("Fetching employees with filters: {}", filter);
        
        Sort sort = Sort.by(
            filter.getSortDirection().equalsIgnoreCase("DESC") 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC,
            filter.getSortBy()
        );
        
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        
        Page<Employee> employees = employeeRepository.findByFilters(
            filter.getRole(),
            filter.getDepartment(),
            filter.getStatus(),
            pageable
        );
        
        return employees.map(employeeMapper::toResponse);
    }
    
    @Override
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        log.info("Updating employee with ID: {}", id);
        
        Employee employee = employeeRepository.findById(id)
            .filter(e -> !e.getDeleted())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Employee not found with ID: " + id));
        
        // Validate unique constraints if changed
        if (!employee.getBadgeId().equals(request.getBadgeId()) &&
            employeeRepository.existsByBadgeIdAndDeletedFalse(request.getBadgeId())) {
            throw new BusinessException("Badge ID already in use");
        }
        
        if (!employee.getEmail().equals(request.getEmail()) &&
            employeeRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new BusinessException("Email already in use");
        }
        
        employeeMapper.updateEntityFromRequest(request, employee);
        Employee updatedEmployee = employeeRepository.save(employee);
        
        log.info("Employee updated successfully with ID: {}", id);
        return employeeMapper.toResponse(updatedEmployee);
    }
    
    @Override
    public void softDeleteEmployee(Long id) {
        log.info("Soft deleting employee with ID: {}", id);
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Employee not found with ID: " + id));
        
        employee.setDeleted(true);
        employeeRepository.save(employee);
        
        log.info("Employee soft deleted successfully with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> searchEmployees(String search, int page, int size) {
        log.debug("Searching employees with term: {}", search);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Employee> employees = employeeRepository.searchEmployees(search, pageable);
        
        return employees.map(employeeMapper::toResponse);
    }
}
```

### Section: Controller Layer

**Description**: 
REST controllers expose employee management endpoints with comprehensive OpenAPI documentation.

**Design Specification**:

```java
package com.warehouse.ems.employee.controller;

import com.warehouse.ems.employee.dto.*;
import com.warehouse.ems.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "APIs for managing warehouse employees")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(
        summary = "Create new employee",
        description = "Creates a new employee record. Requires ADMIN or HR role."
    )
    @ApiResponse(responseCode = "201", description = "Employee created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "409", description = "Employee already exists")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get employee by ID")
    @ApiResponse(responseCode = "200", description = "Employee found")
    @ApiResponse(responseCode = "404", description = "Employee not found")
    public ResponseEntity<EmployeeResponse> getEmployeeById(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/badge/{badgeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get employee by badge ID")
    public ResponseEntity<EmployeeResponse> getEmployeeByBadgeId(
            @Parameter(description = "Badge ID") @PathVariable String badgeId) {
        EmployeeResponse response = employeeService.getEmployeeByBadgeId(badgeId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get all employees with filtering and pagination")
    public ResponseEntity<Page<EmployeeResponse>> getAllEmployees(
            @ModelAttribute EmployeeFilterRequest filter) {
        Page<EmployeeResponse> response = employeeService.getAllEmployees(filter);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Search employees by name, email, or badge ID")
    public ResponseEntity<Page<EmployeeResponse>> searchEmployees(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<EmployeeResponse> response = employeeService.searchEmployees(query, page, size);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update employee (full update)")
    @ApiResponse(responseCode = "200", description = "Employee updated")
    @ApiResponse(responseCode = "404", description = "Employee not found")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Partially update employee")
    public ResponseEntity<EmployeeResponse> partialUpdateEmployee(
            @PathVariable Long id,
            @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.partialUpdateEmployee(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete employee")
    @ApiResponse(responseCode = "204", description = "Employee deleted")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## E03: Role Based Access Control (RBAC)

### Section: Security Configuration

**Description**: 
Implement comprehensive security using Spring Security with JWT authentication, role-based authorization, and method-level security.

**Design Specification**:

#### Security Configuration
```java
package com.warehouse.ems.config;

import com.warehouse.ems.security.jwt.JwtAuthenticationEntryPoint;
import com.warehouse.ems.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthEntryPoint;
    private final UserDetailsService userDetailsService;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/api/v1/auth/**",
                    "/api/v3/api-docs/**",
                    "/api/swagger-ui/**",
                    "/api/swagger-ui.html",
                    "/actuator/health"
                ).permitAll()
                
                // Employee endpoints
                .requestMatchers(HttpMethod.POST, "/api/v1/employees/**")
                    .hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.PUT, "/api/v1/employees/**")
                    .hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/employees/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/employees/**")
                    .hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                
                // Attendance endpoints
                .requestMatchers("/api/v1/attendance/clock-in", "/api/v1/attendance/clock-out")
                    .hasAnyRole("WORKER", "SUPERVISOR", "ADMIN")
                .requestMatchers("/api/v1/attendance/reports/**")
                    .hasAnyRole("SUPERVISOR", "HR", "ADMIN")
                
                // Safety endpoints
                .requestMatchers("/api/v1/safety/**")
                    .hasAnyRole("SAFETY_OFFICER", "ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthEntryPoint)
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
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

#### JWT Token Provider
```java
package com.warehouse.ems.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400000}") // 24 hours default
    private long jwtExpirationMs;
    
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        String roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));
        
        return Jwts.builder()
            .setSubject(username)
            .claim("roles", roles)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        return claims.getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

#### JWT Authentication Filter
```java
package com.warehouse.ems.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromToken(jwt);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

---

## E04: Time & Attendance (Clock In/Out)

### Section: Domain Model

**Description**: 
Track employee clock-in/out events with geolocation, device information, and automatic shift association.

**Design Specification**:

```java
package com.warehouse.ems.attendance.domain;

import com.warehouse.ems.common.domain.BaseEntity;
import com.warehouse.ems.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_events", indexes = {
    @Index(name = "idx_employee_date", columnList = "employee_id, event_date"),
    @Index(name = "idx_event_type", columnList = "event_type"),
    @Index(name = "idx_shift_id", columnList = "shift_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEvent extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private AttendanceEventType eventType;
    
    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;
    
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;
    
    @Column(name = "shift_id")
    private Long shiftId;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "device_id", length = 100)
    private String deviceId;
    
    @Column(name = "device_type", length = 50)
    private String deviceType;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}

@Entity
@Table(name = "attendance_summaries", indexes = {
    @Index(name = "idx_summary_employee_date", columnList = "employee_id, work_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSummary extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;
    
    @Column(name = "clock_in_time")
    private LocalDateTime clockInTime;
    
    @Column(name = "clock_out_time")
    private LocalDateTime clockOutTime;
    
    @Column(name = "total_hours")
    private Double totalHours;
    
    @Column(name = "regular_hours")
    private Double regularHours;
    
    @Column(name = "overtime_hours")
    private Double overtimeHours;
    
    @Column(name = "break_hours")
    private Double breakHours;
    
    @Column(name = "shift_id")
    private Long shiftId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;
    
    @Column(name = "has_missed_punch")
    private Boolean hasMissedPunch = false;
    
    @Column(name = "correction_requested")
    private Boolean correctionRequested = false;
}

public enum AttendanceEventType {
    CLOCK_IN,
    CLOCK_OUT,
    BREAK_START,
    BREAK_END
}

public enum AttendanceStatus {
    PENDING,
    APPROVED,
    REJECTED,
    REQUIRES_CORRECTION
}
```

### Section: Service Layer

**Description**: 
Business logic for clock-in/out operations, validation, and attendance calculations.

**Sample Implementation**:

```java
package com.warehouse.ems.attendance.service;

import com.warehouse.ems.attendance.domain.*;
import com.warehouse.ems.attendance.dto.*;
import com.warehouse.ems.attendance.repository.*;
import com.warehouse.ems.common.exception.BusinessException;
import com.warehouse.ems.employee.domain.Employee;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceServiceImpl implements AttendanceService {
    
    private final AttendanceEventRepository eventRepository;
    private final AttendanceSummaryRepository summaryRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftService shiftService;
    private final GeofenceService geofenceService;
    
    @Override
    public AttendanceEventResponse clockIn(ClockInRequest request) {
        log.info("Processing clock-in for employee: {}", request.getEmployeeId());
        
        Employee employee = employeeRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new BusinessException("Employee not found"));
        
        // Validate no active clock-in
        Optional<AttendanceEvent> activeClockIn = eventRepository
            .findActiveClockIn(employee.getId(), LocalDate.now());
        
        if (activeClockIn.isPresent()) {
            throw new BusinessException("Employee already clocked in");
        }
        
        // Validate geofence if enabled
        if (request.getLatitude() != null && request.getLongitude() != null) {
            if (!geofenceService.isWithinWarehouseBoundary(
                    request.getLatitude(), request.getLongitude())) {
                throw new BusinessException("Clock-in location outside warehouse boundary");
            }
        }
        
        // Determine shift
        Long shiftId = shiftService.determineShiftForTime(
            employee.getId(), LocalDateTime.now());
        
        // Create clock-in event
        AttendanceEvent event = AttendanceEvent.builder()
            .employee(employee)
            .eventType(AttendanceEventType.CLOCK_IN)
            .eventTimestamp(LocalDateTime.now())
            .eventDate(LocalDate.now())
            .shiftId(shiftId)
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .deviceId(request.getDeviceId())
            .deviceType(request.getDeviceType())
            .ipAddress(request.getIpAddress())
            .status(AttendanceStatus.APPROVED)
            .build();
        
        AttendanceEvent savedEvent = eventRepository.save(event);
        
        // Create or update daily summary
        updateDailySummary(employee, LocalDate.now());
        
        log.info("Clock-in successful for employee: {}", employee.getId());
        return mapToResponse(savedEvent);
    }
    
    @Override
    public AttendanceEventResponse clockOut(ClockOutRequest request) {
        log.info("Processing clock-out for employee: {}", request.getEmployeeId());
        
        Employee employee = employeeRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new BusinessException("Employee not found"));
        
        // Validate active clock-in exists
        AttendanceEvent clockInEvent = eventRepository
            .findActiveClockIn(employee.getId(), LocalDate.now())
            .orElseThrow(() -> new BusinessException("No active clock-in found"));
        
        // Create clock-out event
        AttendanceEvent event = AttendanceEvent.builder()
            .employee(employee)
            .eventType(AttendanceEventType.CLOCK_OUT)
            .eventTimestamp(LocalDateTime.now())
            .eventDate(LocalDate.now())
            .shiftId(clockInEvent.getShiftId())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .deviceId(request.getDeviceId())
            .deviceType(request.getDeviceType())
            .ipAddress(request.getIpAddress())
            .status(AttendanceStatus.APPROVED)
            .build();
        
        AttendanceEvent savedEvent = eventRepository.save(event);
        
        // Update daily summary with hours worked
        updateDailySummary(employee, LocalDate.now());
        
        log.info("Clock-out successful for employee: {}", employee.getId());
        return mapToResponse(savedEvent);
    }
    
    private void updateDailySummary(Employee employee, LocalDate date) {
        AttendanceSummary summary = summaryRepository
            .findByEmployeeAndWorkDate(employee.getId(), date)
            .orElse(AttendanceSummary.builder()
                .employee(employee)
                .workDate(date)
                .status(AttendanceStatus.PENDING)
                .build());
        
        // Get all events for the day
        List<AttendanceEvent> events = eventRepository
            .findByEmployeeAndEventDate(employee.getId(), date);
        
        // Calculate hours
        Optional<AttendanceEvent> clockIn = events.stream()
            .filter(e -> e.getEventType() == AttendanceEventType.CLOCK_IN)
            .findFirst();
        
        Optional<AttendanceEvent> clockOut = events.stream()
            .filter(e -> e.getEventType() == AttendanceEventType.CLOCK_OUT)
            .findFirst();
        
        if (clockIn.isPresent()) {
            summary.setClockInTime(clockIn.get().getEventTimestamp());
            summary.setShiftId(clockIn.get().getShiftId());
            
            if (clockOut.isPresent()) {
                summary.setClockOutTime(clockOut.get().getEventTimestamp());
                
                Duration duration = Duration.between(
                    clockIn.get().getEventTimestamp(),
                    clockOut.get().getEventTimestamp()
                );
                
                double totalHours = duration.toMinutes() / 60.0;
                summary.setTotalHours(totalHours);
                
                // Calculate regular vs overtime (assuming 8 hour standard)
                if (totalHours <= 8.0) {
                    summary.setRegularHours(totalHours);
                    summary.setOvertimeHours(0.0);
                } else {
                    summary.setRegularHours(8.0);
                    summary.setOvertimeHours(totalHours - 8.0);
                }
                
                summary.setStatus(AttendanceStatus.APPROVED);
            }
        }
        
        summaryRepository.save(summary);
    }
}
```

### Section: Controller Layer

**Sample Implementation**:

```java
package com.warehouse.ems.attendance.controller;

import com.warehouse.ems.attendance.dto.*;
import com.warehouse.ems.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Time & Attendance", description = "Clock in/out and attendance tracking")
public class AttendanceController {
    
    private final AttendanceService attendanceService;
    
    @PostMapping("/clock-in")
    @PreAuthorize("hasAnyRole('WORKER', 'SUPERVISOR', 'ADMIN')")
    @Operation(summary = "Clock in", description = "Record employee clock-in event")
    public ResponseEntity<AttendanceEventResponse> clockIn(
            @Valid @RequestBody ClockInRequest request) {
        AttendanceEventResponse response = attendanceService.clockIn(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/clock-out")
    @PreAuthorize("hasAnyRole('WORKER', 'SUPERVISOR', 'ADMIN')")
    @Operation(summary = "Clock out", description = "Record employee clock-out event")
    public ResponseEntity<AttendanceEventResponse> clockOut(
            @Valid @RequestBody ClockOutRequest request) {
        AttendanceEventResponse response = attendanceService.clockOut(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/summary/{employeeId}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'HR', 'ADMIN')")
    @Operation(summary = "Get attendance summary")
    public ResponseEntity<Page<AttendanceSummaryResponse>> getAttendanceSummary(
            @PathVariable Long employeeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AttendanceSummaryResponse> response = attendanceService
            .getAttendanceSummary(employeeId, startDate, endDate, page, size);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/corrections")
    @PreAuthorize("hasAnyRole('WORKER', 'SUPERVISOR')")
    @Operation(summary = "Request attendance correction")
    public ResponseEntity<CorrectionRequestResponse> requestCorrection(
            @Valid @RequestBody CorrectionRequest request) {
        CorrectionRequestResponse response = attendanceService.requestCorrection(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Export attendance data to CSV")
    public ResponseEntity<byte[]> exportAttendance(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Long departmentId) {
        byte[] csvData = attendanceService.exportAttendanceToCsv(
            startDate, endDate, departmentId);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=attendance.csv")
            .header("Content-Type", "text/csv")
            .body(csvData);
    }
}
```

---

## E05: Shift & Schedule Management

### Section: Domain Model

**Description**: 
Manage shift templates, rotations, and employee assignments with conflict detection.

**Design Specification**:

```java
package com.warehouse.ems.scheduling.domain;

import com.warehouse.ems.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Table(name = "shift_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftTemplate extends BaseEntity {
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "duration_hours", nullable = false)
    private Double durationHours;
    
    @ElementCollection
    @CollectionTable(name = "shift_template_days", 
        joinColumns = @JoinColumn(name = "shift_template_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> daysOfWeek;
    
    @Column(name = "break_duration_minutes")
    private Integer breakDurationMinutes;
    
    @Column(name = "grace_period_minutes")
    private Integer gracePeriodMinutes = 15;
    
    @Column(name = "overtime_threshold_hours")
    private Double overtimeThresholdHours = 8.0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "department", length = 100)
    private String department;
}

@Entity
@Table(name = "shift_assignments", indexes = {
    @Index(name = "idx_assignment_employee", columnList = "employee_id"),
    @Index(name = "idx_assignment_date", columnList = "shift_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftAssignment extends BaseEntity {
    
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_template_id", nullable = false)
    private ShiftTemplate shiftTemplate;
    
    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;
    
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;
    
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShiftStatus status;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @Column(name = "assigned_by")
    private Long assignedBy;
}

public enum ShiftStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
```

---

## E06: Leave & Absence Management

### Section: Domain Model

**Description**: 
Manage employee leave requests, approvals, and accrual balances.

**Design Specification**:

```java
package com.warehouse.ems.leave.domain;

import com.warehouse.ems.common.domain.BaseEntity;
import com.warehouse.ems.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "leave_requests", indexes = {
    @Index(name = "idx_leave_employee", columnList = "employee_id"),
    @Index(name = "idx_leave_status", columnList = "status"),
    @Index(name = "idx_leave_dates", columnList = "start_date, end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    private LeaveType leaveType;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "total_days", nullable = false)
    private Integer totalDays;
    
    @Column(name = "reason", length = 1000)
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LeaveStatus status;
    
    @Column(name = "reviewed_by")
    private Long reviewedBy;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "review_comments", length = 500)
    private String reviewComments;
    
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;
}

@Entity
@Table(name = "leave_balances", indexes = {
    @Index(name = "idx_balance_employee", columnList = "employee_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    private LeaveType leaveType;
    
    @Column(name = "year", nullable = false)
    private Integer year;
    
    @Column(name = "total_allocated")
    private Double totalAllocated;
    
    @Column(name = "used")
    private Double used = 0.0;
    
    @Column(name = "pending")
    private Double pending = 0.0;
    
    @Column(name = "available")
    private Double available;
    
    @Column(name = "carried_forward")
    private Double carriedForward = 0.0;
}

public enum LeaveType {
    PAID_TIME_OFF,
    SICK_LEAVE,
    UNPAID_LEAVE,
    BEREAVEMENT,
    JURY_DUTY,
    PARENTAL_LEAVE
}

public enum LeaveStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
}
```

---

## E07: Training & Certification Tracking

### Section: Domain Model

**Description**: 
Track employee certifications, expirations, and training requirements.

**Design Specification**:

```java
package com.warehouse.ems.training.domain;

import com.warehouse.ems.common.domain.BaseEntity;
import com.warehouse.ems.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "certifications", indexes = {
    @Index(name = "idx_cert_employee", columnList = "employee_id"),
    @Index(name = "idx_cert_expiry", columnList = "expiry_date"),
    @Index(name = "idx_cert_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certification extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_type_id", nullable = false)
    private CertificationType certificationType;
    
    @Column(name = "certification_number", length = 100)
    private String certificationNumber;
    
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CertificationStatus status;
    
    @Column(name = "issuing_authority", length = 200)
    private String issuingAuthority;
    
    @Column(name = "proof_document_url", length = 500)
    private String proofDocumentUrl;
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @Column(name = "reminder_sent")
    private Boolean reminderSent = false;
}

@Entity
@Table(name = "certification_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificationType extends BaseEntity {
    
    @Column(name = "name", nullable = false, unique = true, length = 200)
    private String name;
    
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "validity_period_months")
    private Integer validityPeriodMonths;
    
    @Column(name = "is_mandatory")
    private Boolean isMandatory = false;
    
    @Column(name = "required_for_equipment")
    private Boolean requiredForEquipment = false;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
}

public enum CertificationStatus {
    ACTIVE,
    EXPIRED,
    EXPIRING_SOON,
    REVOKED,
    PENDING_RENEWAL
}
```

---

## E08: Safety Incidents & OSHA Reporting

### Section: Domain Model

**Description**: 
Record and manage safety incidents with OSHA compliance reporting.

**Design Specification**:

```java
package com.warehouse.ems.safety.domain;

import com.warehouse.ems.common.domain.BaseEntity;
import com.warehouse.ems.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "safety_incidents", indexes = {
    @Index(name = "idx_incident_date", columnList = "incident_date"),
    @Index(name = "idx_incident_severity", columnList = "severity"),
    @Index(name = "idx_incident_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SafetyIncident extends BaseEntity {
    
    @Column(name = "incident_number", unique = true, nullable = false, length = 50)
    private String incidentNumber;
    
    @Column(name = "incident_date", nullable = false)
    private LocalDateTime incidentDate;
    
    @Column(name = "location", nullable = false, length = 200)
    private String location;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false)
    private IncidentType incidentType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private IncidentSeverity severity;
    
    @Column(name = "description", nullable = false, length = 2000)
    private String description;
    
    @ManyToMany
    @JoinTable(
        name = "incident_involved_employees",
        joinColumns = @JoinColumn(name = "incident_id"),
        inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private Set<Employee> involvedEmployees = new HashSet<>();
    
    @Column(name = "witness_names", length = 500)
    private String witnessNames;
    
    @Column(name = "immediate_action_taken", length = 1000)
    private String immediateActionTaken;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private IncidentStatus status;
    
    @Column(name = "reported_by")
    private Long reportedBy;
    
    @Column(name = "investigated_by")
    private Long investigatedBy;
    
    @Column(name = "investigation_notes", length = 2000)
    private String investigationNotes;
    
    @Column(name = "root_cause", length = 1000)
    private String rootCause;
    
    @Column(name = "corrective_actions", length = 2000)
    private String correctiveActions;
    
    @Column(name = "is_osha_recordable")
    private Boolean isOshaRecordable = false;
    
    @Column(name = "days_away_from_work")
    private Integer daysAwayFromWork;
    
    @Column(name = "days_restricted_work")
    private Integer daysRestrictedWork;
    
    @Column(name = "medical_treatment_required")
    private Boolean medicalTreatmentRequired = false;
}

public enum IncidentType {
    INJURY,
    NEAR_MISS,
    PROPERTY_DAMAGE,
    ENVIRONMENTAL,
    EQUIPMENT_FAILURE
}

public enum IncidentSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL,
    FATAL
}

public enum IncidentStatus {
    OPEN,
    INVESTIGATING,
    RESOLVED,
    CLOSED
}
```

---

## E09: Equipment & Asset Assignment

### Section: Domain Model

**Description**: 
Track equipment inventory and assignments to employees with certification validation.

**Design Specification**:

```java
package com.warehouse.ems.asset.domain;

import com.warehouse.ems.common.domain.BaseEntity;
import com.warehouse.ems.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "assets", indexes = {
    @Index(name = "idx_asset_number", columnList = "asset_number"),
    @Index(name = "idx_asset_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset extends BaseEntity {
    
    @Column(name = "asset_number", unique = true, nullable = false, length = 50)
    private String assetNumber;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType;
    
    @Column(name = "manufacturer", length = 100)
    private String manufacturer;
    
    @Column(name = "model", length = 100)
    private String model;
    
    @Column(name = "serial_number", length = 100)
    private String serialNumber;
    
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;
    
    @Column(name = "purchase_cost")
    private BigDecimal purchaseCost;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssetStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_state", nullable = false)
    private AssetCondition conditionState;
    
    @Column(name = "location", length = 200)
    private String location;
    
    @Column(name = "requires_certification")
    private Boolean requiresCertification = false;
    
    @Column(name = "required_certification_type_id")
    private Long requiredCertificationTypeId;
    
    @Column(name = "maintenance_schedule", length = 500)
    private String maintenanceSchedule;
    
    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;
    
    @Column(name = "next_maintenance_date")
    private LocalDate nextMaintenanceDate;
}

@Entity
@Table(name = "asset_assignments", indexes = {
    @Index(name = "idx_assignment_asset", columnList = "asset_id"),
    @Index(name = "idx_assignment_employee", columnList = "employee_id"),
    @Index(name = "idx_assignment_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetAssignment extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Column(name = "checkout_date", nullable = false)
    private LocalDateTime checkoutDate;
    
    @Column(name = "expected_return_date")
    private LocalDateTime expectedReturnDate;
    
    @Column(name = "actual_return_date")
    private LocalDateTime actualReturnDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssignmentStatus status;
    
    @Column(name = "checkout_condition")
    @Enumerated(EnumType.STRING)
    private AssetCondition checkoutCondition;
    
    @Column(name = "return_condition")
    @Enumerated(EnumType.STRING)
    private AssetCondition returnCondition;
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @Column(name = "assigned_by")
    private Long assignedBy;
}

public enum AssetType {
    FORKLIFT,
    PALLET_JACK,
    SCANNER,
    RADIO,
    SAFETY_EQUIPMENT,
    TOOLS,
    VEHICLE,
    OTHER
}

public enum AssetStatus {
    AVAILABLE,
    ASSIGNED,
    IN_MAINTENANCE,
    OUT_OF_SERVICE,
    RETIRED
}

public enum AssetCondition {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    DAMAGED
}

public enum AssignmentStatus {
    ACTIVE,
    RETURNED,
    OVERDUE,
    LOST
}
```

---

## E10: Performance Reviews & Goals

### Section: Domain Model

**Description**: 
Structured performance review system with goal tracking and competency assessment.

**Design Specification**:

```java
package com.warehouse.ems.performance.domain;

import com.warehouse.ems.common.domain.BaseEntity;
import com.warehouse.ems.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review_cycles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCycle extends BaseEntity {
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    private ReviewType reviewType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewCycleStatus status;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
}

@Entity
@Table(name = "performance_reviews", indexes = {
    @Index(name = "idx_review_employee", columnList = "employee_id"),
    @Index(name = "idx_review_cycle", columnList = "review_cycle_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReview extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_cycle_id", nullable = false)
    private ReviewCycle reviewCycle;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private Employee reviewer;
    
    @Column(name = "review_date")
    private LocalDate reviewDate;
    
    @Column(name = "overall_rating")
    private Integer overallRating;
    
    @Column(name = "strengths", length = 2000)
    private String strengths;
    
    @Column(name = "areas_for_improvement", length = 2000)
    private String areasForImprovement;
    
    @Column(name = "achievements", length = 2000)
    private String achievements;
    
    @Column(name = "goals_for_next_period", length = 2000)
    private String goalsForNextPeriod;
    
    @Column(name = "reviewer_comments", length = 2000)
    private String reviewerComments;
    
    @Column(name = "employee_comments", length = 2000)
    private String employeeComments;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatus status;
    
    @Column(name = "employee_acknowledged_at")
    private LocalDateTime employeeAcknowledgedAt;
    
    @Column(name = "reviewer_signed_at")
    private LocalDateTime reviewerSignedAt;
    
    @OneToMany(mappedBy = "performanceReview", cascade = CascadeType.ALL)
    private List<CompetencyRating> competencyRatings = new ArrayList<>();
}

@Entity
@Table(name = "competency_ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetencyRating extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_review_id", nullable = false)
    private PerformanceReview performanceReview;
    
    @Column(name = "competency_name", nullable = false, length = 200)
    private String competencyName;
    
    @Column(name = "rating", nullable = false)
    private Integer rating;
    
    @Column(name = "comments", length = 1000)
    private String comments;
}

public enum ReviewType {
    QUARTERLY,
    ANNUAL,
    PROBATION,
    PROJECT_BASED
}

public enum ReviewCycleStatus {
    PLANNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

public enum ReviewStatus {
    DRAFT,
    SUBMITTED,
    ACKNOWLEDGED,
    COMPLETED
}
```

---

## E11: Payroll Export Integration

### Section: Service Layer

**Description**: 
Generate payroll-ready export files with attendance and leave data.

**Sample Implementation**:

```java
package com.warehouse.ems.payroll.service;

import com.warehouse.ems.attendance.repository.AttendanceSummaryRepository;
import com.warehouse.ems.leave.repository.LeaveRequestRepository;
import com.warehouse.ems.payroll.dto.PayrollExportRequest;
import com.warehouse.ems.payroll.dto.PayrollRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PayrollExportServiceImpl implements PayrollExportService {
    
    private final AttendanceSummaryRepository attendanceRepository;
    private final LeaveRequestRepository leaveRepository;
    private final PayrollProviderAdapter payrollAdapter;
    
    @Override
    public byte[] generatePayrollExport(PayrollExportRequest request) {
        log.info("Generating payroll export for period: {} to {}",
            request.getStartDate(), request.getEndDate());
        
        // Fetch attendance data
        List<AttendanceSummary> attendanceData = attendanceRepository
            .findByDateRangeAndStatus(
                request.getStartDate(),
                request.getEndDate(),
                AttendanceStatus.APPROVED
            );
        
        // Fetch approved leave data
        List<LeaveRequest> leaveData = leaveRepository
            .findByDateRangeAndStatus(
                request.getStartDate(),
                request.getEndDate(),
                LeaveStatus.APPROVED
            );
        
        // Transform to payroll records
        List<PayrollRecord> payrollRecords = transformToPayrollRecords(
            attendanceData, leaveData);
        
        // Generate export file based on provider format
        return payrollAdapter.generateExportFile(payrollRecords, request.getProviderFormat());
    }
    
    private List<PayrollRecord> transformToPayrollRecords(
            List<AttendanceSummary> attendance,
            List<LeaveRequest> leaves) {
        
        Map<Long, PayrollRecord> recordMap = new HashMap<>();
        
        // Process attendance
        for (AttendanceSummary summary : attendance) {
            Long employeeId = summary.getEmployee().getId();
            PayrollRecord record = recordMap.computeIfAbsent(
                employeeId,
                id -> PayrollRecord.builder()
                    .employeeId(id)
                    .badgeId(summary.getEmployee().getBadgeId())
                    .regularHours(0.0)
                    .overtimeHours(0.0)
                    .paidLeaveHours(0.0)
                    .unpaidLeaveHours(0.0)
                    .build()
            );
            
            record.setRegularHours(record.getRegularHours() + summary.getRegularHours());
            record.setOvertimeHours(record.getOvertimeHours() + summary.getOvertimeHours());
        }
        
        // Process leaves
        for (LeaveRequest leave : leaves) {
            Long employeeId = leave.getEmployee().getId();
            PayrollRecord record = recordMap.get(employeeId);
            
            if (record != null) {
                double leaveHours = leave.getTotalDays() * 8.0; // Assuming 8-hour days
                
                if (leave.getLeaveType() == LeaveType.PAID_TIME_OFF ||
                    leave.getLeaveType() == LeaveType.SICK_LEAVE) {
                    record.setPaidLeaveHours(record.getPaidLeaveHours() + leaveHours);
                } else {
                    record.setUnpaidLeaveHours(record.getUnpaidLeaveHours() + leaveHours);
                }
            }
        }
        
        return new ArrayList<>(recordMap.values());
    }
}
```

---

## E12: Notifications & Announcements

### Section: Service Layer

**Description**: 
Multi-channel notification system with templates and user preferences.

**Sample Implementation**:

```java
package com.warehouse.ems.notification.service;

import com.warehouse.ems.notification.domain.*;
import com.warehouse.ems.notification.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final PushNotificationService pushService;
    private final NotificationTemplateService templateService;
    
    @Override
    @Async
    @Transactional
    public void sendNotification(NotificationRequest request) {
        log.info("Sending notification: {} to user: {}",
            request.getType(), request.getRecipientId());
        
        // Get user preferences
        NotificationPreference preferences = getPreferences(request.getRecipientId());
        
        // Render template
        String content = templateService.renderTemplate(
            request.getTemplateCode(),
            request.getTemplateData(),
            preferences.getPreferredLanguage()
        );
        
        // Create notification record
        Notification notification = Notification.builder()
            .recipientId(request.getRecipientId())
            .type(request.getType())
            .title(request.getTitle())
            .content(content)
            .priority(request.getPriority())
            .status(NotificationStatus.PENDING)
            .build();
        
        notification = notificationRepository.save(notification);
        
        // Send via enabled channels
        if (preferences.isEmailEnabled() && request.getChannels().contains(NotificationChannel.EMAIL)) {
            sendEmail(notification, preferences.getEmail());
        }
        
        if (preferences.isSmsEnabled() && request.getChannels().contains(NotificationChannel.SMS)) {
            sendSms(notification, preferences.getPhoneNumber());
        }
        
        if (preferences.isPushEnabled() && request.getChannels().contains(NotificationChannel.PUSH)) {
            sendPush(notification, request.getRecipientId());
        }
        
        // Always create in-app notification
        notification.setStatus(NotificationStatus.SENT);
        notificationRepository.save(notification);
    }
    
    private void sendEmail(Notification notification, String email) {
        try {
            emailService.send(email, notification.getTitle(), notification.getContent());
            log.info("Email sent successfully for notification: {}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to send email for notification: {}", notification.getId(), e);
        }
    }
    
    private void sendSms(Notification notification, String phoneNumber) {
        try {
            smsService.send(phoneNumber, notification.getContent());
            log.info("SMS sent successfully for notification: {}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to send SMS for notification: {}", notification.getId(), e);
        }
    }
    
    private void sendPush(Notification notification, Long userId) {
        try {
            pushService.send(userId, notification.getTitle(), notification.getContent());
            log.info("Push notification sent successfully: {}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to send push notification: {}", notification.getId(), e);
        }
    }
}
```

---

## E13: Integration Layer (HRIS/WMS APIs)

### Section: API Design

**Description**: 
Expose and consume REST APIs for external system integration.

**Sample Implementation**:

```java
package com.warehouse.ems.integration.controller;

import com.warehouse.ems.integration.dto.*;
import com.warehouse.ems.integration.service.HrisIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/integration/hris")
@RequiredArgsConstructor
@Tag(name = "HRIS Integration", description = "APIs for HRIS system integration")
@SecurityRequirement(name = "apiKey")
public class HrisIntegrationController {
    
    private final HrisIntegrationService hrisService;
    
    @PostMapping("/employees/sync")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sync employee data from HRIS")
    public ResponseEntity<SyncResponse> syncEmployees(
            @Valid @RequestBody HrisSyncRequest request) {
        SyncResponse response = hrisService.syncEmployees(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/webhooks/employee-created")
    @Operation(summary = "Webhook for new employee creation")
    public ResponseEntity<Void> handleEmployeeCreated(
            @Valid @RequestBody EmployeeWebhookPayload payload,
            @RequestHeader("X-Webhook-Signature") String signature) {
        
        // Validate webhook signature
        if (!hrisService.validateWebhookSignature(payload, signature)) {
            return ResponseEntity.status(401).build();
        }
        
        hrisService.processNewEmployee(payload);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/webhooks/employee-terminated")
    @Operation(summary = "Webhook for employee termination")
    public ResponseEntity<Void> handleEmployeeTerminated(
            @Valid @RequestBody EmployeeWebhookPayload payload,
            @RequestHeader("X-Webhook-Signature") String signature) {
        
        if (!hrisService.validateWebhookSignature(payload, signature)) {
            return ResponseEntity.status(401).build();
        }
        
        hrisService.processTermination(payload);
        return ResponseEntity.ok().build();
    }
}
```

---

## E14: Audit Trail & Compliance

### Section: Audit Logging Implementation

**Description**: 
Centralized audit logging with JPA entity listeners and AOP.

**Sample Implementation**:

```java
package com.warehouse.ems.audit.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.ems.audit.domain.AuditLog;
import com.warehouse.ems.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {
    
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;
    
    @AfterReturning(
        pointcut = "@annotation(auditable)",
        returning = "result"
    )
    public void logAuditableAction(
            JoinPoint joinPoint,
            Auditable auditable,
            Object result) {
        
        try {
            String actor = getCurrentUser();
            String entityType = auditable.entityType();
            String action = auditable.action();
            
            AuditLog auditLog = AuditLog.builder()
                .entityType(entityType)
                .action(action)
                .actor(actor)
                .timestamp(LocalDateTime.now())
                .ipAddress(getClientIpAddress())
                .userAgent(request.getHeader("User-Agent"))
                .afterState(objectMapper.writeValueAsString(result))
                .build();
            
            auditLogRepository.save(auditLog);
            
            log.debug("Audit log created: {} {} by {}", action, entityType, actor);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }
    
    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();
        
        return authentication != null ? authentication.getName() : "SYSTEM";
    }
    
    private String getClientIpAddress() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String entityType();
    String action();
}
```

---

## E15: Reporting & Analytics

### Section: Report Service

**Description**: 
Generate operational reports with filtering and export capabilities.

**Sample Implementation**:

```java
package com.warehouse.ems.reporting.service;

import com.warehouse.ems.reporting.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportingServiceImpl implements ReportingService {
    
    private final AttendanceReportGenerator attendanceReportGenerator;
    private final LeaveReportGenerator leaveReportGenerator;
    private final SafetyReportGenerator safetyReportGenerator;
    private final CsvExportService csvExportService;
    private final PdfExportService pdfExportService;
    
    @Override
    public ReportResponse generateAttendanceReport(ReportRequest request) {
        log.info("Generating attendance report for period: {} to {}",
            request.getStartDate(), request.getEndDate());
        
        List<AttendanceReportData> data = attendanceReportGenerator
            .generateReport(request);
        
        return ReportResponse.builder()
            .reportType("ATTENDANCE")
            .generatedAt(LocalDateTime.now())
            .recordCount(data.size())
            .data(data)
            .build();
    }
    
    @Override
    public byte[] exportReportToCsv(ReportRequest request) {
        log.info("Exporting report to CSV");
        
        ReportResponse report = generateReport(request);
        return csvExportService.export(report);
    }
    
    @Override
    public byte[] exportReportToPdf(ReportRequest request) {
        log.info("Exporting report to PDF");
        
        ReportResponse report = generateReport(request);
        return pdfExportService.export(report);
    }
    
    private ReportResponse generateReport(ReportRequest request) {
        switch (request.getReportType()) {
            case ATTENDANCE:
                return generateAttendanceReport(request);
            case LEAVE:
                return generateLeaveReport(request);
            case SAFETY:
                return generateSafetyReport(request);
            default:
                throw new IllegalArgumentException("Unsupported report type");
        }
    }
}
```

---

## E16: Mobile Access (PWA)

### Section: PWA Configuration

**Description**: 
Progressive Web App configuration for mobile access.

**Sample Implementation**:

```json
// public/manifest.json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "description": "Warehouse Employee Management System",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#2196F3",
  "orientation": "portrait",
  "icons": [
    {
      "src": "/icons/icon-72x72.png",
      "sizes": "72x72",
      "type": "image/png"
    },
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

```javascript
// Service Worker for offline support
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open('ems-v1').then((cache) => {
      return cache.addAll([
        '/',
        '/index.html',
        '/static/css/main.css',
        '/static/js/main.js'
      ]);
    })
  );
});

self.addEventListener('fetch', (event) => {
  event.respondWith(
    caches.match(event.request).then((response) => {
      return response || fetch(event.request);
    })
  );
});
```

---

## E17: Onboarding & Offboarding Workflow

### Section: Workflow Service

**Description**: 
Automated employee lifecycle management.

**Sample Implementation**:

```java
package com.warehouse.ems.workflow.service;

import com.warehouse.ems.employee.domain.Employee;
import com.warehouse.ems.workflow.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OnboardingServiceImpl implements OnboardingService {
    
    private final WorkflowTaskRepository taskRepository;
    private final EmployeeService employeeService;
    private final TrainingService trainingService;
    private final AssetService assetService;
    private final SchedulingService schedulingService;
    
    @Override
    public void initiateOnboarding(Employee employee) {
        log.info("Initiating onboarding for employee: {}", employee.getId());
        
        List<WorkflowTask> tasks = new ArrayList<>();
        
        // Create account setup task
        tasks.add(createTask(
            employee.getId(),
            "Account Setup",
            "Create system accounts and access credentials",
            WorkflowTaskType.ACCOUNT_SETUP,
            1
        ));
        
        // Create training tasks
        tasks.add(createTask(
            employee.getId(),
            "Safety Training",
            "Complete mandatory safety training",
            WorkflowTaskType.TRAINING,
            2
        ));
        
        // Create asset assignment task
        tasks.add(createTask(
            employee.getId(),
            "Equipment Assignment",
            "Assign required equipment and PPE",
            WorkflowTaskType.ASSET_ASSIGNMENT,
            3
        ));
        
        // Create schedule assignment task
        tasks.add(createTask(
            employee.getId(),
            "Schedule Assignment",
            "Assign to initial shift schedule",
            WorkflowTaskType.SCHEDULE_ASSIGNMENT,
            4
        ));
        
        taskRepository.saveAll(tasks);
        
        log.info("Onboarding tasks created: {} tasks", tasks.size());
    }
    
    @Override
    public void initiateOffboarding(Employee employee) {
        log.info("Initiating offboarding for employee: {}", employee.getId());
        
        List<WorkflowTask> tasks = new ArrayList<>();
        
        // Revoke access
        tasks.add(createTask(
            employee.getId(),
            "Revoke Access",
            "Disable system accounts and access",
            WorkflowTaskType.ACCESS_REVOCATION,
            1
        ));
        
        // Collect assets
        tasks.add(createTask(
            employee.getId(),
            "Asset Collection",
            "Collect all assigned equipment",
            WorkflowTaskType.ASSET_COLLECTION,
            2
        ));
        
        // Remove from schedules
        tasks.add(createTask(
            employee.getId(),
            "Schedule Removal",
            "Remove from all future schedules",
            WorkflowTaskType.SCHEDULE_REMOVAL,
            3
        ));
        
        // Exit interview
        tasks.add(createTask(
            employee.getId(),
            "Exit Interview",
            "Conduct exit interview",
            WorkflowTaskType.EXIT_INTERVIEW,
            4
        ));
        
        taskRepository.saveAll(tasks);
        
        log.info("Offboarding tasks created: {} tasks", tasks.size());
    }
    
    private WorkflowTask createTask(
            Long employeeId,
            String title,
            String description,
            WorkflowTaskType type,
            int order) {
        
        return WorkflowTask.builder()
            .employeeId(employeeId)
            .title(title)
            .description(description)
            .taskType(type)
            .status(WorkflowTaskStatus.PENDING)
            .order(order)
            .build();
    }
}
```

---

## Cross-Cutting Concerns

### Exception Handling

```java
package com.warehouse.ems.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex) {
        log.error("Business exception: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Business Rule Violation")
            .message(ex.getMessage())
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Input validation failed")
            .validationErrors(errors)
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.FORBIDDEN.value())
            .error("Forbidden")
            .message("Access denied")
            .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex) {
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred")
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### Validation Utilities

```java
package com.warehouse.ems.common.util;

import com.warehouse.ems.common.exception.BusinessException;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;

@UtilityClass
public class ValidationUtil {
    
    public static void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException("Start date and end date are required");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date");
        }
    }
    
    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new BusinessException(fieldName + " is required");
        }
    }
    
    public static void validatePositive(Number value, String fieldName) {
        if (value == null || value.doubleValue() <= 0) {
            throw new BusinessException(fieldName + " must be positive");
        }
    }
}
```

---

## Deployment & Configuration

### Docker Configuration

```dockerfile
# Dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java","-cp","app:app/lib/*","com.warehouse.ems.WarehouseEmsApplication"]
```

```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: warehouse_ems
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
  
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/warehouse_ems
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
    depends_on:
      - postgres

volumes:
  postgres_data:
```

### Application Properties (Production)

```yaml
# application-prod.yml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
  
  jpa:
    show-sql: false
    properties:
      hibernate:
        generate_statistics: false
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI}

server:
  port: ${PORT:8080}
  compression:
    enabled: true
  http2:
    enabled: true

logging:
  level:
    root: INFO
    com.warehouse.ems: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

---

## Testing Strategy

### Unit Test Example

```java
package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.domain.Employee;
import com.warehouse.ems.employee.dto.EmployeeRequest;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {
    
    @Mock
    private EmployeeRepository employeeRepository;
    
    @Mock
    private EmployeeMapper employeeMapper;
    
    @InjectMocks
    private EmployeeServiceImpl employeeService;
    
    @Test
    void createEmployee_Success() {
        // Given
        EmployeeRequest request = EmployeeRequest.builder()
            .badgeId("EMP001")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();
        
        Employee employee = new Employee();
        employee.setId(1L);
        
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(anyString()))
            .thenReturn(false);
        when(employeeRepository.existsByEmailAndDeletedFalse(anyString()))
            .thenReturn(false);
        when(employeeMapper.toEntity(any())).thenReturn(employee);
        when(employeeRepository.save(any())).thenReturn(employee);
        
        // When
        EmployeeResponse response = employeeService.createEmployee(request);
        
        // Then
        assertThat(response).isNotNull();
        verify(employeeRepository).save(any(Employee.class));
    }
    
    @Test
    void createEmployee_DuplicateBadgeId_ThrowsException() {
        // Given
        EmployeeRequest request = EmployeeRequest.builder()
            .badgeId("EMP001")
            .build();
        
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001"))
            .thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> employeeService.createEmployee(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("already exists");
    }
}
```

### Integration Test Example

```java
package com.warehouse.ems.employee.controller;

import com.warehouse.ems.employee.dto.EmployeeRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class EmployeeControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmployee_Success() throws Exception {
        String requestBody = """{
            "badgeId": "EMP001",
            "firstName": "John",
            "lastName": "Doe",
            "email": "john.doe@example.com",
            "role": "WORKER",
            "department": "Warehouse",
            "hireDate": "2024-01-01",
            "status": "ACTIVE"
        }""";
        
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.badgeId").value("EMP001"))
            .andExpect(jsonPath("$.firstName").value("John"));
    }
}
```

---

## Conclusion

This comprehensive low-level technical design document provides Spring Boot developers with:

1. **Clear Architecture**: Layered architecture with separation of concerns
2. **Domain Models**: Complete entity designs with relationships and constraints
3. **Service Layer**: Business logic implementation patterns
4. **API Design**: RESTful endpoints with OpenAPI documentation
5. **Security**: JWT-based authentication and role-based authorization
6. **Data Access**: JPA repositories with custom queries
7. **Cross-Cutting Concerns**: Exception handling, validation, and audit logging
8. **Testing**: Unit and integration test examples
9. **Deployment**: Docker and production configuration
10. **Best Practices**: Spring Boot conventions and industry standards

All user stories from E01 to E17 have been covered with detailed technical specifications, enabling developers to implement the Warehouse EMS system consistently and efficiently.

---

**Document Version**: 1.0  
**Last Updated**: 2024  
**Status**: Ready for Implementation
