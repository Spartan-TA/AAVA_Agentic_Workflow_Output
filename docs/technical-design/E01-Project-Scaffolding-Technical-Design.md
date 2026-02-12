# Epic E01: Project Scaffolding & Domain Setup - Low-Level Technical Design Document

## Document Information
- **Epic ID**: E01
- **Epic Title**: Project Scaffolding & Domain Setup
- **Document Version**: 1.0
- **Last Updated**: 2024
- **Author**: Senior Software Architect
- **Target Audience**: Spring Boot Developers

---

## Table of Contents
1. [Epic Overview](#epic-overview)
2. [User Stories Breakdown](#user-stories-breakdown)
3. [Technical Design per User Story](#technical-design-per-user-story)
4. [Cross-Cutting Concerns](#cross-cutting-concerns)
5. [Testing Strategy](#testing-strategy)
6. [Deployment & DevOps](#deployment--devops)

---

## Epic Overview

### Epic Description
Initialize Spring Boot (Maven) project; configure base packages; set up core modules (employee, scheduling, attendance, safety); add Flyway/Liquibase for DB migrations; enable Actuator.

### Business Value
Provide a standardized foundation to accelerate delivery and enforce consistency across modules.

### Priority
High

### Acceptance Criteria
- Project builds and runs on port 8080
- README has build/run steps
- Actuator health endpoint returns UP
- Base package structure created
- Flyway/Liquibase runs baseline migration

---

## User Stories Breakdown

### US-E01-001: Initialize Spring Boot Maven Project
**As a** developer  
**I want** a properly configured Spring Boot Maven project  
**So that** I can start building features on a solid foundation

**Acceptance Criteria:**
- Spring Boot 3.2.x project created with Maven
- Java 17 or higher configured
- Project builds successfully with `mvn clean install`
- Application starts on port 8080
- Basic application.yml/properties configured

**Story Points**: 2  
**Priority**: High

---

### US-E01-002: Configure Base Package Structure
**As a** developer  
**I want** a well-organized package structure  
**So that** code is maintainable and follows Spring Boot best practices

**Acceptance Criteria:**
- Base package: `com.warehouse.ems`
- Domain modules created: employee, scheduling, attendance, safety
- Layered architecture: controller, service, repository, domain, dto, config
- Package-info.java files with documentation

**Story Points**: 3  
**Priority**: High

---

### US-E01-003: Setup Database Migration with Flyway
**As a** developer  
**I want** database version control using Flyway  
**So that** schema changes are tracked and reproducible

**Acceptance Criteria:**
- Flyway dependency added to pom.xml
- Baseline migration V1__init.sql created
- Migrations run automatically on startup
- Flyway configuration in application.yml
- Migration history table created

**Story Points**: 3  
**Priority**: High

---

### US-E01-004: Enable Spring Boot Actuator
**As a** DevOps engineer  
**I want** health and monitoring endpoints  
**So that** I can monitor application status

**Acceptance Criteria:**
- Actuator dependency added
- /actuator/health endpoint returns UP
- /actuator/info endpoint configured
- Security configured for actuator endpoints
- Custom health indicators for database

**Story Points**: 2  
**Priority**: High

---

### US-E01-005: Create README with Build Instructions
**As a** new developer  
**I want** clear documentation on how to build and run the project  
**So that** I can quickly onboard and contribute

**Acceptance Criteria:**
- README.md with project overview
- Prerequisites listed (Java, Maven, Database)
- Build commands documented
- Run commands documented
- Environment setup instructions

**Story Points**: 1  
**Priority**: Medium

---

### US-E01-006: Configure Core Application Properties
**As a** developer  
**I want** externalized configuration  
**So that** application behavior can be customized per environment

**Acceptance Criteria:**
- application.yml with profiles (dev, test, prod)
- Database connection properties
- Server port configuration (8080)
- Logging configuration
- Profile-specific property files

**Story Points**: 2  
**Priority**: High

---

## Technical Design per User Story

---

## US-E01-001: Initialize Spring Boot Maven Project

### Technical Specifications

#### 1. Maven POM Configuration

**File**: `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.warehouse</groupId>
    <artifactId>ems</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>Warehouse Employee Management System</name>
    <description>Enterprise Employee Management System for Warehouse Operations</description>
    
    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>
    
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
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- Flyway Migration -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- MapStruct for DTO mapping -->
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
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-testcontainers</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>1.5.5.Final</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 2. Main Application Class

**File**: `src/main/java/com/warehouse/ems/WarehouseEmsApplication.java`

```java
package com.warehouse.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application for Warehouse Employee Management System.
 * 
 * This application provides comprehensive employee management capabilities including:
 * - Employee master data management
 * - Time and attendance tracking
 * - Shift scheduling and management
 * - Safety incident reporting
 * - Training and certification tracking
 * 
 * @author Warehouse EMS Team
 * @version 1.0.0
 * @since 2024
 */
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

#### 3. Implementation Guidelines

**Build Process:**
```bash
# Clean and build
mvn clean install

# Run tests
mvn test

# Run application
mvn spring-boot:run

# Package as JAR
mvn package
```

**Verification Steps:**
1. Execute `mvn clean install` - should complete without errors
2. Start application with `mvn spring-boot:run`
3. Verify application starts on port 8080
4. Check logs for successful startup message
5. Access http://localhost:8080/actuator/health (should return UP)

---

## US-E01-002: Configure Base Package Structure

### Technical Specifications

#### 1. Package Structure

```
com.warehouse.ems/
âââ WarehouseEmsApplication.java
âââ config/                          # Configuration classes
â   âââ ApplicationConfig.java
â   âââ DatabaseConfig.java
â   âââ SecurityConfig.java
â   âââ WebConfig.java
â   âââ ActuatorConfig.java
âââ common/                          # Shared components
â   âââ exception/
â   â   âââ GlobalExceptionHandler.java
â   â   âââ ResourceNotFoundException.java
â   â   âââ BusinessException.java
â   â   âââ ValidationException.java
â   âââ dto/
â   â   âââ ApiResponse.java
â   â   âââ ErrorResponse.java
â   â   âââ PageResponse.java
â   âââ util/
â   â   âââ DateTimeUtil.java
â   â   âââ ValidationUtil.java
â   âââ constants/
â       âââ AppConstants.java
â       âââ ErrorMessages.java
âââ employee/                        # Employee module
â   âââ controller/
â   â   âââ EmployeeController.java
â   âââ service/
â   â   âââ EmployeeService.java
â   â   âââ impl/
â   â       âââ EmployeeServiceImpl.java
â   âââ repository/
â   â   âââ EmployeeRepository.java
â   âââ domain/
â   â   âââ Employee.java
â   âââ dto/
â   â   âââ EmployeeRequest.java
â   â   âââ EmployeeResponse.java
â   â   âââ EmployeeMapper.java
â   âââ package-info.java
âââ scheduling/                      # Scheduling module
â   âââ controller/
â   âââ service/
â   âââ repository/
â   âââ domain/
â   âââ dto/
â   âââ package-info.java
âââ attendance/                      # Attendance module
â   âââ controller/
â   âââ service/
â   âââ repository/
â   âââ domain/
â   âââ dto/
â   âââ package-info.java
âââ safety/                          # Safety module
    âââ controller/
    âââ service/
    âââ repository/
    âââ domain/
    âââ dto/
    âââ package-info.java
```

#### 2. Base Configuration Classes

**File**: `src/main/java/com/warehouse/ems/config/ApplicationConfig.java`

```java
package com.warehouse.ems.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Main application configuration class.
 * Provides common beans and configurations used across the application.
 */
@Configuration
@EnableAspectJAutoProxy
public class ApplicationConfig {

    /**
     * Configures ObjectMapper for JSON serialization/deserialization.
     * Includes Java 8 date/time support and pretty printing.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }
}
```

**File**: `src/main/java/com/warehouse/ems/config/DatabaseConfig.java`

```java
package com.warehouse.ems.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Database configuration for JPA repositories and transaction management.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.warehouse.ems")
@EnableTransactionManagement
public class DatabaseConfig {
    // Additional database configurations can be added here
}
```

**File**: `src/main/java/com/warehouse/ems/config/WebConfig.java`

```java
package com.warehouse.ems.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration including CORS settings.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

#### 3. Common Exception Handling

**File**: `src/main/java/com/warehouse/ems/common/exception/GlobalExceptionHandler.java`

```java
package com.warehouse.ems.common.exception;

import com.warehouse.ems.common.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for consistent error responses across the application.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        log.error("Resource not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {
        log.error("Business exception: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Business Rule Violation")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Input validation failed")
                .path(request.getDescription(false).replace("uri=", ""))
                .validationErrors(errors)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

**File**: `src/main/java/com/warehouse/ems/common/exception/ResourceNotFoundException.java`

```java
package com.warehouse.ems.common.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
```

**File**: `src/main/java/com/warehouse/ems/common/exception/BusinessException.java`

```java
package com.warehouse.ems.common.exception;

/**
 * Exception thrown when a business rule is violated.
 */
public class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

#### 4. Common DTOs

**File**: `src/main/java/com/warehouse/ems/common/dto/ErrorResponse.java`

```java
package com.warehouse.ems.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response structure for API errors.
 */
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
    private Map<String, String> validationErrors;
}
```

**File**: `src/main/java/com/warehouse/ems/common/dto/ApiResponse.java`

```java
package com.warehouse.ems.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper for successful operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
```

#### 5. Package Documentation

**File**: `src/main/java/com/warehouse/ems/employee/package-info.java`

```java
/**
 * Employee module for managing warehouse employee master data.
 * 
 * This module provides:
 * - CRUD operations for employee records
 * - Employee profile management
 * - Badge ID management
 * - Department and role assignments
 * - Employee status tracking
 * 
 * Key entities:
 * - Employee: Core employee entity with personal and employment information
 * 
 * @since 1.0.0
 */
package com.warehouse.ems.employee;
```

#### 6. Implementation Guidelines

**Package Naming Conventions:**
- Use lowercase for all package names
- Use singular nouns for domain packages (employee, not employees)
- Group by feature/module, not by layer
- Keep package depth reasonable (max 4-5 levels)

**Class Naming Conventions:**
- Controllers: `{Entity}Controller`
- Services: `{Entity}Service` (interface) and `{Entity}ServiceImpl` (implementation)
- Repositories: `{Entity}Repository`
- Entities: `{Entity}` (e.g., Employee)
- DTOs: `{Entity}Request`, `{Entity}Response`
- Mappers: `{Entity}Mapper`

**Verification Steps:**
1. Verify all packages are created under `com.warehouse.ems`
2. Ensure each module has all required sub-packages
3. Check that package-info.java exists for each module
4. Verify configuration classes are in the config package
5. Confirm common utilities are properly organized

---

## US-E01-003: Setup Database Migration with Flyway

### Technical Specifications

#### 1. Flyway Configuration

**File**: `src/main/resources/application.yml` (Flyway section)

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 0
    locations: classpath:db/migration
    validate-on-migrate: true
    out-of-order: false
    placeholder-replacement: true
    placeholders:
      schema: public
    table: flyway_schema_history
```

#### 2. Database Schema Structure

**Directory Structure:**
```
src/main/resources/db/migration/
âââ V1__init_schema.sql
âââ V2__create_employee_tables.sql
âââ V3__create_scheduling_tables.sql
âââ V4__create_attendance_tables.sql
âââ V5__create_safety_tables.sql
âââ V6__create_indexes.sql
```

#### 3. Initial Migration Scripts

**File**: `src/main/resources/db/migration/V1__init_schema.sql`

```sql
-- =====================================================
-- Warehouse EMS - Initial Schema Setup
-- Version: 1.0
-- Description: Creates base schema and audit tables
-- =====================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create audit log table for tracking all changes
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    entity_name VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    action VARCHAR(20) NOT NULL, -- INSERT, UPDATE, DELETE
    actor VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    before_data JSONB,
    after_data JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    CONSTRAINT chk_action CHECK (action IN ('INSERT', 'UPDATE', 'DELETE'))
);

CREATE INDEX idx_audit_log_entity ON audit_log(entity_name, entity_id);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp DESC);
CREATE INDEX idx_audit_log_actor ON audit_log(actor);

-- Create application metadata table
CREATE TABLE app_metadata (
    key VARCHAR(100) PRIMARY KEY,
    value TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insert initial metadata
INSERT INTO app_metadata (key, value, description) VALUES
('schema_version', '1.0', 'Current database schema version'),
('app_version', '1.0.0', 'Application version'),
('initialized_at', CURRENT_TIMESTAMP::TEXT, 'Database initialization timestamp');

COMMENT ON TABLE audit_log IS 'Immutable audit trail for all entity changes';
COMMENT ON TABLE app_metadata IS 'Application-level metadata and configuration';
```

**File**: `src/main/resources/db/migration/V2__create_employee_tables.sql`

```sql
-- =====================================================
-- Employee Module Tables
-- Version: 2.0
-- Description: Core employee master data tables
-- =====================================================

-- Employee status enum type
CREATE TYPE employee_status AS ENUM ('ACTIVE', 'INACTIVE', 'ON_LEAVE', 'TERMINATED');

-- Employee role enum type
CREATE TYPE employee_role AS ENUM ('ADMIN', 'HR', 'SUPERVISOR', 'WORKER');

-- Main employee table
CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    badge_id VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20),
    role employee_role NOT NULL DEFAULT 'WORKER',
    department VARCHAR(100) NOT NULL,
    shift_group VARCHAR(50),
    hire_date DATE NOT NULL,
    termination_date DATE,
    status employee_status NOT NULL DEFAULT 'ACTIVE',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100) NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_hire_date CHECK (hire_date <= CURRENT_DATE),
    CONSTRAINT chk_termination_date CHECK (termination_date IS NULL OR termination_date >= hire_date)
);

-- Indexes for employee table
CREATE INDEX idx_employee_badge_id ON employee(badge_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_employee_email ON employee(email) WHERE is_deleted = FALSE;
CREATE INDEX idx_employee_department ON employee(department) WHERE is_deleted = FALSE;
CREATE INDEX idx_employee_status ON employee(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_employee_role ON employee(role);
CREATE INDEX idx_employee_shift_group ON employee(shift_group) WHERE is_deleted = FALSE;
CREATE INDEX idx_employee_hire_date ON employee(hire_date);

-- Employee contact information table
CREATE TABLE employee_contact (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    contact_type VARCHAR(50) NOT NULL, -- EMERGENCY, PERSONAL, WORK
    contact_name VARCHAR(200),
    relationship VARCHAR(100),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    address TEXT,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_contact_type CHECK (contact_type IN ('EMERGENCY', 'PERSONAL', 'WORK'))
);

CREATE INDEX idx_employee_contact_employee ON employee_contact(employee_id);
CREATE INDEX idx_employee_contact_type ON employee_contact(contact_type);

-- Employee address table
CREATE TABLE employee_address (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    address_type VARCHAR(50) NOT NULL, -- HOME, MAILING
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL DEFAULT 'USA',
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_address_type CHECK (address_type IN ('HOME', 'MAILING'))
);

CREATE INDEX idx_employee_address_employee ON employee_address(employee_id);

COMMENT ON TABLE employee IS 'Core employee master data with employment information';
COMMENT ON TABLE employee_contact IS 'Employee contact information including emergency contacts';
COMMENT ON TABLE employee_address IS 'Employee address information';
COMMENT ON COLUMN employee.badge_id IS 'Unique employee badge identifier for physical access';
COMMENT ON COLUMN employee.is_deleted IS 'Soft delete flag for data retention';
COMMENT ON COLUMN employee.version IS 'Optimistic locking version number';
```

**File**: `src/main/resources/db/migration/V3__create_scheduling_tables.sql`

```sql
-- =====================================================
-- Scheduling Module Tables
-- Version: 3.0
-- Description: Shift and schedule management tables
-- =====================================================

-- Shift template table
CREATE TABLE shift_template (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    duration_minutes INTEGER NOT NULL,
    break_minutes INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_shift_duration CHECK (duration_minutes > 0 AND duration_minutes <= 1440),
    CONSTRAINT chk_break_minutes CHECK (break_minutes >= 0 AND break_minutes < duration_minutes)
);

CREATE INDEX idx_shift_template_active ON shift_template(is_active);

-- Schedule table
CREATE TABLE schedule (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    shift_template_id BIGINT NOT NULL REFERENCES shift_template(id),
    scheduled_date DATE NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100) NOT NULL,
    CONSTRAINT chk_schedule_status CHECK (status IN ('SCHEDULED', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    CONSTRAINT chk_schedule_times CHECK (end_time > start_time)
);

CREATE INDEX idx_schedule_employee ON schedule(employee_id);
CREATE INDEX idx_schedule_date ON schedule(scheduled_date);
CREATE INDEX idx_schedule_status ON schedule(status);
CREATE UNIQUE INDEX idx_schedule_employee_date ON schedule(employee_id, scheduled_date) 
    WHERE status NOT IN ('CANCELLED');

COMMENT ON TABLE shift_template IS 'Reusable shift templates with timing and break information';
COMMENT ON TABLE schedule IS 'Employee shift assignments and schedules';
```

**File**: `src/main/resources/db/migration/V4__create_attendance_tables.sql`

```sql
-- =====================================================
-- Attendance Module Tables
-- Version: 4.0
-- Description: Time and attendance tracking tables
-- =====================================================

-- Attendance record table
CREATE TABLE attendance (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    schedule_id BIGINT REFERENCES schedule(id),
    clock_in_time TIMESTAMP NOT NULL,
    clock_out_time TIMESTAMP,
    clock_in_location VARCHAR(255),
    clock_out_location VARCHAR(255),
    clock_in_device VARCHAR(100),
    clock_out_device VARCHAR(100),
    total_hours DECIMAL(5,2),
    regular_hours DECIMAL(5,2),
    overtime_hours DECIMAL(5,2),
    break_minutes INTEGER DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'CLOCKED_IN',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_attendance_status CHECK (status IN ('CLOCKED_IN', 'CLOCKED_OUT', 'MISSED', 'CORRECTED')),
    CONSTRAINT chk_clock_times CHECK (clock_out_time IS NULL OR clock_out_time > clock_in_time)
);

CREATE INDEX idx_attendance_employee ON attendance(employee_id);
CREATE INDEX idx_attendance_clock_in ON attendance(clock_in_time);
CREATE INDEX idx_attendance_status ON attendance(status);
CREATE INDEX idx_attendance_schedule ON attendance(schedule_id);

-- Attendance correction table
CREATE TABLE attendance_correction (
    id BIGSERIAL PRIMARY KEY,
    attendance_id BIGINT NOT NULL REFERENCES attendance(id),
    correction_type VARCHAR(50) NOT NULL,
    original_clock_in TIMESTAMP,
    original_clock_out TIMESTAMP,
    corrected_clock_in TIMESTAMP,
    corrected_clock_out TIMESTAMP,
    reason TEXT NOT NULL,
    requested_by VARCHAR(100) NOT NULL,
    approved_by VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    CONSTRAINT chk_correction_type CHECK (correction_type IN ('MISSED_PUNCH', 'TIME_ADJUSTMENT', 'MANUAL_ENTRY')),
    CONSTRAINT chk_correction_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX idx_attendance_correction_attendance ON attendance_correction(attendance_id);
CREATE INDEX idx_attendance_correction_status ON attendance_correction(status);

COMMENT ON TABLE attendance IS 'Employee clock-in/out records with location and device tracking';
COMMENT ON TABLE attendance_correction IS 'Attendance correction requests and approvals';
```

**File**: `src/main/resources/db/migration/V5__create_safety_tables.sql`

```sql
-- =====================================================
-- Safety Module Tables
-- Version: 5.0
-- Description: Safety incident and certification tables
-- =====================================================

-- Safety incident table
CREATE TABLE safety_incident (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    incident_number VARCHAR(50) NOT NULL UNIQUE,
    incident_date TIMESTAMP NOT NULL,
    reported_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reported_by BIGINT NOT NULL REFERENCES employee(id),
    location VARCHAR(255) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    incident_type VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    immediate_action TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    investigation_notes TEXT,
    corrective_actions TEXT,
    closed_date TIMESTAMP,
    closed_by BIGINT REFERENCES employee(id),
    is_osha_recordable BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_incident_severity CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_incident_status CHECK (status IN ('OPEN', 'INVESTIGATING', 'RESOLVED', 'CLOSED'))
);

CREATE INDEX idx_safety_incident_number ON safety_incident(incident_number);
CREATE INDEX idx_safety_incident_date ON safety_incident(incident_date DESC);
CREATE INDEX idx_safety_incident_status ON safety_incident(status);
CREATE INDEX idx_safety_incident_severity ON safety_incident(severity);
CREATE INDEX idx_safety_incident_osha ON safety_incident(is_osha_recordable);

-- Incident involved employees table
CREATE TABLE incident_involved_employee (
    id BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL REFERENCES safety_incident(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    involvement_type VARCHAR(50) NOT NULL,
    injury_description TEXT,
    medical_treatment_required BOOLEAN NOT NULL DEFAULT FALSE,
    days_away_from_work INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_involvement_type CHECK (involvement_type IN ('INJURED', 'WITNESS', 'REPORTER')),
    UNIQUE(incident_id, employee_id, involvement_type)
);

CREATE INDEX idx_incident_employee_incident ON incident_involved_employee(incident_id);
CREATE INDEX idx_incident_employee_employee ON incident_involved_employee(employee_id);

-- Certification table
CREATE TABLE certification (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    certification_type VARCHAR(100) NOT NULL,
    validity_period_days INTEGER NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_validity_period CHECK (validity_period_days > 0)
);

CREATE INDEX idx_certification_type ON certification(certification_type);
CREATE INDEX idx_certification_active ON certification(is_active);

-- Employee certification table
CREATE TABLE employee_certification (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    certification_id BIGINT NOT NULL REFERENCES certification(id),
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    certification_number VARCHAR(100),
    issuing_authority VARCHAR(200),
    document_path VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_cert_dates CHECK (expiry_date > issue_date),
    CONSTRAINT chk_cert_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'REVOKED', 'PENDING'))
);

CREATE INDEX idx_employee_cert_employee ON employee_certification(employee_id);
CREATE INDEX idx_employee_cert_certification ON employee_certification(certification_id);
CREATE INDEX idx_employee_cert_expiry ON employee_certification(expiry_date);
CREATE INDEX idx_employee_cert_status ON employee_certification(status);

COMMENT ON TABLE safety_incident IS 'Safety incidents and near-miss events with OSHA tracking';
COMMENT ON TABLE incident_involved_employee IS 'Employees involved in safety incidents';
COMMENT ON TABLE certification IS 'Master list of required certifications';
COMMENT ON TABLE employee_certification IS 'Employee certification records with expiration tracking';
```

**File**: `src/main/resources/db/migration/V6__create_indexes.sql`

```sql
-- =====================================================
-- Additional Indexes and Performance Optimizations
-- Version: 6.0
-- Description: Additional indexes for query performance
-- =====================================================

-- Composite indexes for common query patterns
CREATE INDEX idx_employee_dept_status ON employee(department, status) 
    WHERE is_deleted = FALSE;

CREATE INDEX idx_schedule_date_employee ON schedule(scheduled_date, employee_id) 
    WHERE status != 'CANCELLED';

CREATE INDEX idx_attendance_employee_date ON attendance(employee_id, clock_in_time DESC);

-- Full-text search indexes
CREATE INDEX idx_employee_name_search ON employee 
    USING gin(to_tsvector('english', first_name || ' ' || last_name)) 
    WHERE is_deleted = FALSE;

CREATE INDEX idx_safety_incident_search ON safety_incident 
    USING gin(to_tsvector('english', description));

-- Partial indexes for active records
CREATE INDEX idx_active_schedules ON schedule(scheduled_date) 
    WHERE status IN ('SCHEDULED', 'CONFIRMED');

CREATE INDEX idx_open_incidents ON safety_incident(incident_date DESC) 
    WHERE status IN ('OPEN', 'INVESTIGATING');

CREATE INDEX idx_expiring_certs ON employee_certification(expiry_date) 
    WHERE status = 'ACTIVE' AND expiry_date > CURRENT_DATE;

-- Statistics for query planner
ANALYZE employee;
ANALYZE schedule;
ANALYZE attendance;
ANALYZE safety_incident;
ANALYZE certification;
ANALYZE employee_certification;
```

#### 4. Flyway Callback Scripts (Optional)

**File**: `src/main/resources/db/callback/afterMigrate.sql`

```sql
-- Post-migration callback script
-- Executed after each successful migration

-- Update metadata
UPDATE app_metadata 
SET value = CURRENT_TIMESTAMP::TEXT, 
    updated_at = CURRENT_TIMESTAMP 
WHERE key = 'last_migration_date';

-- Log migration completion
INSERT INTO audit_log (entity_name, entity_id, action, actor, timestamp, after_data)
VALUES ('MIGRATION', 'FLYWAY', 'UPDATE', 'SYSTEM', CURRENT_TIMESTAMP, 
        jsonb_build_object('status', 'completed'));
```

#### 5. Implementation Guidelines

**Migration Naming Convention:**
- Format: `V{version}__{description}.sql`
- Version: Sequential integer (V1, V2, V3...)
- Description: Snake_case, descriptive
- Example: `V1__init_schema.sql`

**Migration Best Practices:**
1. **Idempotent Scripts**: Use `IF NOT EXISTS` where possible
2. **Rollback Scripts**: Create corresponding undo scripts for complex migrations
3. **Data Migrations**: Separate from schema migrations when possible
4. **Testing**: Test migrations on a copy of production data
5. **Performance**: Add indexes after bulk data loads
6. **Comments**: Document purpose and impact of each migration

**Verification Steps:**
1. Start application and verify Flyway runs migrations
2. Check `flyway_schema_history` table for migration records
3. Verify all tables are created with correct structure
4. Check indexes are created properly
5. Validate constraints and foreign keys
6. Test rollback procedures

**Flyway Commands:**
```bash
# Validate migrations
mvn flyway:validate

# Get migration info
mvn flyway:info

# Repair migration history
mvn flyway:repair

# Clean database (development only)
mvn flyway:clean
```

---

## US-E01-004: Enable Spring Boot Actuator

### Technical Specifications

#### 1. Actuator Configuration

**File**: `src/main/resources/application.yml` (Actuator section)

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,flyway,loggers
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      show-components: when-authorized
      probes:
        enabled: true
    info:
      enabled: true
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
    db:
      enabled: true
    diskspace:
      enabled: true
      threshold: 10GB
  info:
    env:
      enabled: true
    java:
      enabled: true
    os:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}
```

#### 2. Custom Health Indicators

**File**: `src/main/java/com/warehouse/ems/config/ActuatorConfig.java`

```java
package com.warehouse.ems.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Configuration for Spring Boot Actuator custom health indicators.
 */
@Configuration
public class ActuatorConfig {

    /**
     * Custom health indicator for database connectivity and performance.
     */
    @Bean
    public HealthIndicator databaseHealthIndicator(DataSource dataSource) {
        return () -> {
            try {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                
                // Test database connectivity
                Long result = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM app_metadata", Long.class);
                
                // Check response time
                long startTime = System.currentTimeMillis();
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                long responseTime = System.currentTimeMillis() - startTime;
                
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("responseTime", responseTime + "ms")
                    .withDetail("metadataRecords", result)
                    .build();
            } catch (Exception e) {
                return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
            }
        };
    }

    /**
     * Custom health indicator for Flyway migration status.
     */
    @Bean
    public HealthIndicator flywayHealthIndicator(JdbcTemplate jdbcTemplate) {
        return () -> {
            try {
                Integer pendingMigrations = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM flyway_schema_history WHERE success = false",
                    Integer.class
                );
                
                Integer totalMigrations = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM flyway_schema_history",
                    Integer.class
                );
                
                if (pendingMigrations != null && pendingMigrations > 0) {
                    return Health.down()
                        .withDetail("failedMigrations", pendingMigrations)
                        .withDetail("totalMigrations", totalMigrations)
                        .build();
                }
                
                return Health.up()
                    .withDetail("totalMigrations", totalMigrations)
                    .withDetail("status", "All migrations successful")
                    .build();
            } catch (Exception e) {
                return Health.unknown()
                    .withDetail("error", e.getMessage())
                    .build();
            }
        };
    }
}
```

#### 3. Application Info Configuration

**File**: `src/main/resources/application.yml` (Info section)

```yaml
info:
  app:
    name: Warehouse Employee Management System
    description: Enterprise-grade employee management for warehouse operations
    version: '@project.version@'
    encoding: '@project.build.sourceEncoding@'
    java:
      version: '@java.version@'
  company:
    name: Warehouse Solutions Inc.
    email: support@warehouseems.com
  build:
    artifact: '@project.artifactId@'
    group: '@project.groupId@'
```

#### 4. Security Configuration for Actuator

**File**: `src/main/java/com/warehouse/ems/config/ActuatorSecurityConfig.java`

```java
package com.warehouse.ems.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Actuator endpoints.
 * Allows public access to health and info, restricts others.
 */
@Configuration
@EnableWebSecurity
public class ActuatorSecurityConfig {

    @Bean
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
            );
        return http.build();
    }
}
```

#### 5. Custom Metrics

**File**: `src/main/java/com/warehouse/ems/common/metrics/CustomMetrics.java`

```java
package com.warehouse.ems.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Custom application metrics for monitoring business operations.
 */
@Component
public class CustomMetrics {

    private final Counter employeeCreatedCounter;
    private final Counter attendanceClockInCounter;
    private final Counter safetyIncidentCounter;
    private final Timer databaseQueryTimer;

    public CustomMetrics(MeterRegistry meterRegistry) {
        this.employeeCreatedCounter = Counter.builder("employee.created")
                .description("Number of employees created")
                .tag("module", "employee")
                .register(meterRegistry);

        this.attendanceClockInCounter = Counter.builder("attendance.clockin")
                .description("Number of clock-in events")
                .tag("module", "attendance")
                .register(meterRegistry);

        this.safetyIncidentCounter = Counter.builder("safety.incident")
                .description("Number of safety incidents reported")
                .tag("module", "safety")
                .register(meterRegistry);

        this.databaseQueryTimer = Timer.builder("database.query")
                .description("Database query execution time")
                .register(meterRegistry);
    }

    public void incrementEmployeeCreated() {
        employeeCreatedCounter.increment();
    }

    public void incrementAttendanceClockIn() {
        attendanceClockInCounter.increment();
    }

    public void incrementSafetyIncident() {
        safetyIncidentCounter.increment();
    }

    public Timer.Sample startDatabaseQueryTimer() {
        return Timer.start();
    }

    public void stopDatabaseQueryTimer(Timer.Sample sample) {
        sample.stop(databaseQueryTimer);
    }
}
```

#### 6. Implementation Guidelines

**Actuator Endpoints:**
- `/actuator/health` - Application health status
- `/actuator/health/liveness` - Kubernetes liveness probe
- `/actuator/health/readiness` - Kubernetes readiness probe
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics endpoint
- `/actuator/flyway` - Flyway migration information

**Health Check Levels:**
1. **UP**: Application is healthy and ready
2. **DOWN**: Application has critical issues
3. **OUT_OF_SERVICE**: Application is temporarily unavailable
4. **UNKNOWN**: Health status cannot be determined

**Verification Steps:**
1. Start application
2. Access http://localhost:8080/actuator/health
3. Verify response shows "UP" status
4. Check database health indicator
5. Verify Flyway health indicator
6. Access http://localhost:8080/actuator/info
7. Verify application information is displayed
8. Test metrics endpoint: http://localhost:8080/actuator/metrics

**Testing Health Endpoints:**
```bash
# Health check
curl http://localhost:8080/actuator/health

# Detailed health (requires authentication)
curl -u admin:password http://localhost:8080/actuator/health

# Liveness probe
curl http://localhost:8080/actuator/health/liveness

# Readiness probe
curl http://localhost:8080/actuator/health/readiness

# Application info
curl http://localhost:8080/actuator/info

# Metrics
curl http://localhost:8080/actuator/metrics

# Specific metric
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

---

## US-E01-005: Create README with Build Instructions

### Technical Specifications

#### README.md Content

**File**: `README.md`

```markdown
# Warehouse Employee Management System (EMS)

## Overview

The Warehouse Employee Management System is an enterprise-grade Spring Boot application designed to manage all aspects of warehouse employee operations, including:

- Employee master data management
- Time and attendance tracking
- Shift scheduling and management
- Safety incident reporting and OSHA compliance
- Training and certification tracking
- Leave and absence management
- Performance reviews and goals
- Payroll integration

## Technology Stack

- **Framework**: Spring Boot 3.2.x
- **Language**: Java 17
- **Build Tool**: Maven 3.8+
- **Database**: PostgreSQL 14+
- **Migration**: Flyway
- **API Documentation**: OpenAPI 3.0 (Springdoc)
- **Testing**: JUnit 5, Testcontainers
- **Monitoring**: Spring Boot Actuator, Micrometer

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK) 17 or higher**
  ```bash
  java -version
  # Should show version 17 or higher
  ```

- **Apache Maven 3.8 or higher**
  ```bash
  mvn -version
  # Should show Maven 3.8+ and Java 17+
  ```

- **PostgreSQL 14 or higher**
  ```bash
  psql --version
  # Should show PostgreSQL 14+
  ```

- **Git** (for version control)
  ```bash
  git --version
  ```

## Database Setup

### 1. Create Database

```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create database
CREATE DATABASE warehouse_ems;

-- Create user (optional)
CREATE USER ems_user WITH PASSWORD 'ems_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE warehouse_ems TO ems_user;
```

### 2. Configure Database Connection

Update `src/main/resources/application.yml` with your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems
    username: ems_user
    password: ems_password
```

## Building the Application

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/warehouse-ems.git
cd warehouse-ems
```

### 2. Build with Maven

```bash
# Clean and build
mvn clean install

# Build without running tests (faster)
mvn clean install -DskipTests

# Build and create executable JAR
mvn clean package
```

The executable JAR will be created in `target/ems-1.0.0-SNAPSHOT.jar`

## Running the Application

### Option 1: Using Maven

```bash
mvn spring-boot:run
```

### Option 2: Using Java JAR

```bash
java -jar target/ems-1.0.0-SNAPSHOT.jar
```

### Option 3: With Specific Profile

```bash
# Development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production profile
java -jar target/ems-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

### Option 4: With Custom Port

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

## Verifying the Application

Once the application starts successfully, verify it's running:

### 1. Check Application Health

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

### 2. Check Application Info

```bash
curl http://localhost:8080/actuator/info
```

### 3. Access API Documentation

Open your browser and navigate to:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=EmployeeServiceTest
```

### Run Integration Tests

```bash
mvn verify
```

### Generate Test Coverage Report

```bash
mvn clean test jacoco:report
```

View report at: `target/site/jacoco/index.html`

## Database Migrations

Flyway migrations run automatically on application startup. To manage migrations manually:

### Check Migration Status

```bash
mvn flyway:info
```

### Validate Migrations

```bash
mvn flyway:validate
```

### Repair Migration History

```bash
mvn flyway:repair
```

### Clean Database (Development Only)

```bash
mvn flyway:clean
```

## Configuration Profiles

The application supports multiple profiles:

### Development Profile (`dev`)
- H2 in-memory database option
- Detailed logging
- Debug mode enabled
- Hot reload enabled

### Test Profile (`test`)
- Testcontainers for integration tests
- Isolated test database
- Fast startup

### Production Profile (`prod`)
- PostgreSQL database
- Optimized logging
- Security enabled
- Performance monitoring

## Environment Variables

Key environment variables:

```bash
# Database
export DB_URL=jdbc:postgresql://localhost:5432/warehouse_ems
export DB_USERNAME=ems_user
export DB_PASSWORD=ems_password

# Application
export SERVER_PORT=8080
export SPRING_PROFILES_ACTIVE=dev

# Logging
export LOGGING_LEVEL_ROOT=INFO
export LOGGING_LEVEL_COM_WAREHOUSE_EMS=DEBUG
```

## Docker Support

### Build Docker Image

```bash
docker build -t warehouse-ems:latest .
```

### Run with Docker Compose

```bash
docker-compose up -d
```

### Stop Services

```bash
docker-compose down
```

## Troubleshooting

### Application Won't Start

1. **Check Java Version**
   ```bash
   java -version
   # Must be 17 or higher
   ```

2. **Check Port Availability**
   ```bash
   # Linux/Mac
   lsof -i :8080
   
   # Windows
   netstat -ano | findstr :8080
   ```

3. **Check Database Connection**
   ```bash
   psql -h localhost -U ems_user -d warehouse_ems
   ```

### Build Failures

1. **Clean Maven Cache**
   ```bash
   mvn clean
   rm -rf ~/.m2/repository
   mvn install
   ```

2. **Update Dependencies**
   ```bash
   mvn clean install -U
   ```

### Database Migration Issues

1. **Check Flyway Status**
   ```bash
   mvn flyway:info
   ```

2. **Repair Failed Migrations**
   ```bash
   mvn flyway:repair
   ```

## API Documentation

API documentation is available via Swagger UI:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

## Monitoring and Metrics

Actuator endpoints for monitoring:

- **Health**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Info**: http://localhost:8080/actuator/info
- **Prometheus**: http://localhost:8080/actuator/prometheus

## Project Structure

```
warehouse-ems/
âââ src/
â   âââ main/
â   â   âââ java/
â   â   â   âââ com/warehouse/ems/
â   â   â       âââ config/          # Configuration classes
â   â   â       âââ common/          # Shared components
â   â   â       âââ employee/        # Employee module
â   â   â       âââ scheduling/      # Scheduling module
â   â   â       âââ attendance/      # Attendance module
â   â   â       âââ safety/          # Safety module
â   â   âââ resources/
â   â       âââ db/migration/        # Flyway migrations
â   â       âââ application.yml      # Main configuration
â   â       âââ application-{profile}.yml
â   âââ test/
â       âââ java/                    # Test classes
âââ target/                          # Build output
âââ pom.xml                          # Maven configuration
âââ README.md                        # This file
âââ docker-compose.yml               # Docker configuration
```

## Contributing

1. Create a feature branch
2. Make your changes
3. Write/update tests
4. Ensure all tests pass
5. Submit a pull request

## Support

For issues and questions:
- **Email**: support@warehouseems.com
- **Issue Tracker**: https://github.com/your-org/warehouse-ems/issues

## License

Copyright Â© 2024 Warehouse Solutions Inc. All rights reserved.
```

#### Implementation Guidelines

**README Best Practices:**
1. Keep instructions clear and concise
2. Include all prerequisites
3. Provide troubleshooting section
4. Add examples for common tasks
5. Keep it updated with changes
6. Include links to additional documentation

**Verification Steps:**
1. Follow README instructions on a clean machine
2. Verify all commands work as documented
3. Test troubleshooting steps
4. Ensure links are valid
5. Check formatting renders correctly

---

## US-E01-006: Configure Core Application Properties

### Technical Specifications

#### 1. Main Application Configuration

**File**: `src/main/resources/application.yml`

```yaml
# =====================================================
# Warehouse EMS - Main Application Configuration
# =====================================================

spring:
  application:
    name: warehouse-ems
  
  profiles:
    active: dev
  
  # Database Configuration
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems
    username: ${DB_USERNAME:ems_user}
    password: ${DB_PASSWORD:ems_password}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      pool-name: WarehouseEMS-HikariCP
  
  # JPA Configuration
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
      naming:
        physical-strategy: org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy
        implicit-strategy: org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
        jdbc:
          batch_size: 20
          fetch_size: 50
        order_inserts: true
        order_updates: true
        query:
          in_clause_parameter_padding: true
    show-sql: false
    open-in-view: false
  
  # Flyway Configuration
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 0
    locations: classpath:db/migration
    validate-on-migrate: true
    out-of-order: false
    placeholder-replacement: true
    placeholders:
      schema: public
    table: flyway_schema_history
  
  # Jackson Configuration
  jackson:
    serialization:
      write-dates-as-timestamps: false
      indent-output: true
    deserialization:
      fail-on-unknown-properties: false
    default-property-inclusion: non_null
    time-zone: UTC
  
  # Servlet Configuration
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 10MB

# Server Configuration
server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /
    encoding:
      charset: UTF-8
      enabled: true
      force: true
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json
    min-response-size: 1024
  error:
    include-message: always
    include-binding-errors: always
    include-stacktrace: on_param
    include-exception: false

# Actuator Configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,flyway,loggers
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      show-components: when-authorized
      probes:
        enabled: true
    info:
      enabled: true
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
    db:
      enabled: true
    diskspace:
      enabled: true
      threshold: 10GB
  info:
    env:
      enabled: true
    java:
      enabled: true
    os:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}

# Application Info
info:
  app:
    name: Warehouse Employee Management System
    description: Enterprise-grade employee management for warehouse operations
    version: '@project.version@'
    encoding: '@project.build.sourceEncoding@'
    java:
      version: '@java.version@'
  company:
    name: Warehouse Solutions Inc.
    email: support@warehouseems.com

# Logging Configuration
logging:
  level:
    root: INFO
    com.warehouse.ems: DEBUG
    org.springframework.web: INFO
    org.springframework.security: INFO
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    org.flywaydb: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/warehouse-ems.log
    max-size: 10MB
    max-history: 30
    total-size-cap: 1GB

# Application-Specific Configuration
app:
  name: Warehouse EMS
  version: 1.0.0
  timezone: UTC
  cors:
    allowed-origins:
      - http://localhost:3000
      - http://localhost:4200
    allowed-methods:
      - GET
      - POST
      - PUT
      - PATCH
      - DELETE
      - OPTIONS
    allowed-headers: "*"
    allow-credentials: true
    max-age: 3600
  security:
    jwt:
      secret: ${JWT_SECRET:changeme-this-is-not-secure}
      expiration: 86400000 # 24 hours
    api-key:
      enabled: false
  pagination:
    default-page-size: 20
    max-page-size: 100
```

#### 2. Development Profile Configuration

**File**: `src/main/resources/application-dev.yml`

```yaml
# =====================================================
# Development Profile Configuration
# =====================================================

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems_dev
    username: ems_dev
    password: dev_password
  
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  
  devtools:
    restart:
      enabled: true
    livereload:
      enabled: true

server:
  port: 8080

logging:
  level:
    root: INFO
    com.warehouse.ems: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

management:
  endpoint:
    health:
      show-details: always

app:
  security:
    jwt:
      secret: dev-secret-key-not-for-production
```

#### 3. Test Profile Configuration

**File**: `src/main/resources/application-test.yml`

```yaml
# =====================================================
# Test Profile Configuration
# =====================================================

spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: false
  
  flyway:
    enabled: false
  
  h2:
    console:
      enabled: true

server:
  port: 0 # Random port for tests

logging:
  level:
    root: WARN
    com.warehouse.ems: DEBUG

app:
  security:
    jwt:
      secret: test-secret-key
      expiration: 3600000
```

#### 4. Production Profile Configuration

**File**: `src/main/resources/application-prod.yml`

```yaml
# =====================================================
# Production Profile Configuration
# =====================================================

spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
  
  jpa:
    show-sql: false
    properties:
      hibernate:
        format_sql: false
        generate_statistics: false

server:
  port: ${SERVER_PORT:8080}
  compression:
    enabled: true
  http2:
    enabled: true

logging:
  level:
    root: WARN
    com.warehouse.ems: INFO
    org.springframework.web: WARN
    org.hibernate.SQL: WARN
  file:
    name: /var/log/warehouse-ems/application.log

management:
  endpoint:
    health:
      show-details: when-authorized
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus

app:
  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration: 86400000
    api-key:
      enabled: true
```

#### 5. Custom Configuration Properties Class

**File**: `src/main/java/com/warehouse/ems/config/AppProperties.java`

```java
package com.warehouse.ems.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Application-specific configuration properties.
 * Binds to 'app' prefix in application.yml
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {

    @NotBlank
    private String name;

    @NotBlank
    private String version;

    @NotBlank
    private String timezone;

    private Cors cors = new Cors();
    private Security security = new Security();
    private Pagination pagination = new Pagination();

    @Data
    public static class Cors {
        @NotEmpty
        private List<String> allowedOrigins;
        
        @NotEmpty
        private List<String> allowedMethods;
        
        private String allowedHeaders = "*";
        private boolean allowCredentials = true;
        
        @Min(0)
        private long maxAge = 3600;
    }

    @Data
    public static class Security {
        private Jwt jwt = new Jwt();
        private ApiKey apiKey = new ApiKey();

        @Data
        public static class Jwt {
            @NotBlank
            private String secret;
            
            @Min(1000)
            private long expiration;
        }

        @Data
        public static class ApiKey {
            private boolean enabled = false;
        }
    }

    @Data
    public static class Pagination {
        @Min(1)
        private int defaultPageSize = 20;
        
        @Min(1)
        private int maxPageSize = 100;
    }
}
```

#### 6. Implementation Guidelines

**Configuration Best Practices:**

1. **Environment Variables**: Use environment variables for sensitive data
   ```yaml
   password: ${DB_PASSWORD:default_value}
   ```

2. **Profile-Specific Files**: Separate configurations by environment
   - `application.yml` - Common configuration
   - `application-dev.yml` - Development overrides
   - `application-test.yml` - Test overrides
   - `application-prod.yml` - Production overrides

3. **Property Validation**: Use `@Validated` and JSR-303 annotations

4. **Type-Safe Configuration**: Create `@ConfigurationProperties` classes

5. **Documentation**: Add comments explaining non-obvious settings

**Verification Steps:**

1. **Validate Configuration**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

2. **Check Active Profile**
   ```bash
   curl http://localhost:8080/actuator/info | jq '.app'
   ```

3. **Verify Database Connection**
   ```bash
   curl http://localhost:8080/actuator/health | jq '.components.db'
   ```

4. **Test Different Profiles**
   ```bash
   # Development
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   
   # Test
   mvn test
   
   # Production (with env vars)
   export DB_URL=jdbc:postgresql://prod-db:5432/warehouse_ems
   export DB_USERNAME=prod_user
   export DB_PASSWORD=prod_password
   java -jar target/ems-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
   ```

---

## Cross-Cutting Concerns

### 1. Logging Strategy

**Implementation:**
- Use SLF4J with Logback
- Structured logging with MDC (Mapped Diagnostic Context)
- Log levels: ERROR, WARN, INFO, DEBUG, TRACE
- Separate log files for different modules
- Log rotation and archival

**Example Logging Configuration:**

```java
@Slf4j
@Service
public class EmployeeService {
    
    public Employee createEmployee(EmployeeRequest request) {
        log.info("Creating employee with badge ID: {}", request.getBadgeId());
        
        try {
            Employee employee = employeeMapper.toEntity(request);
            Employee saved = employeeRepository.save(employee);
            
            log.debug("Employee created successfully: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Failed to create employee: {}", request.getBadgeId(), e);
            throw new BusinessException("Failed to create employee", e);
        }
    }
}
```

### 2. Exception Handling

**Strategy:**
- Global exception handler using `@RestControllerAdvice`
- Custom exception hierarchy
- Consistent error response format
- Proper HTTP status codes
- Error logging and tracking

### 3. Validation

**Implementation:**
- Use JSR-303 Bean Validation
- Custom validators for business rules
- Validation groups for different scenarios
- Clear validation messages

**Example:**

```java
@Data
public class EmployeeRequest {
    
    @NotBlank(message = "Badge ID is required")
    @Size(min = 5, max = 20, message = "Badge ID must be between 5 and 20 characters")
    private String badgeId;
    
    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
}
```

### 4. Auditing

**Implementation:**
- JPA Auditing with `@EntityListeners`
- Track created/updated timestamps
- Track created/updated by user
- Immutable audit log table

**Example:**

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class AuditableEntity {
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @LastModifiedBy
    @Column(nullable = false)
    private String updatedBy;
    
    @Version
    private Integer version;
}
```

### 5. Transaction Management

**Strategy:**
- Use `@Transactional` at service layer
- Read-only transactions for queries
- Proper transaction boundaries
- Rollback on business exceptions

### 6. API Versioning

**Strategy:**
- URI versioning: `/api/v1/employees`
- Version in base path
- Maintain backward compatibility
- Deprecation notices

---

## Testing Strategy

### 1. Unit Tests

**Coverage:**
- Service layer business logic
- Utility classes
- Mappers and converters
- Validation logic

**Example:**

```java
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    
    @Mock
    private EmployeeRepository employeeRepository;
    
    @Mock
    private EmployeeMapper employeeMapper;
    
    @InjectMocks
    private EmployeeServiceImpl employeeService;
    
    @Test
    void createEmployee_Success() {
        // Given
        EmployeeRequest request = createEmployeeRequest();
        Employee employee = createEmployee();
        
        when(employeeMapper.toEntity(request)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        
        // When
        Employee result = employeeService.createEmployee(request);
        
        // Then
        assertNotNull(result);
        assertEquals(employee.getBadgeId(), result.getBadgeId());
        verify(employeeRepository).save(employee);
    }
}
```

### 2. Integration Tests

**Coverage:**
- Repository layer with database
- API endpoints
- Database migrations
- Configuration loading

**Example:**

```java
@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeRepositoryIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Test
    void findByBadgeId_Success() {
        // Given
        Employee employee = createAndSaveEmployee();
        
        // When
        Optional<Employee> result = employeeRepository.findByBadgeId(employee.getBadgeId());
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(employee.getBadgeId(), result.get().getBadgeId());
    }
}
```

### 3. API Tests

**Coverage:**
- REST endpoints
- Request/response validation
- Error handling
- Security

**Example:**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class EmployeeControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void createEmployee_Success() throws Exception {
        // Given
        EmployeeRequest request = createEmployeeRequest();
        
        // When & Then
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value(request.getBadgeId()))
                .andExpect(jsonPath("$.firstName").value(request.getFirstName()));
    }
}
```

### 4. Test Coverage Goals

- **Overall Coverage**: Minimum 80%
- **Service Layer**: Minimum 90%
- **Controller Layer**: Minimum 85%
- **Repository Layer**: Minimum 70%
- **Utility Classes**: 100%

---

## Deployment & DevOps

### 1. Build Pipeline

```yaml
# .github/workflows/build.yml
name: Build and Test

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build with Maven
      run: mvn clean install
    
    - name: Run tests
      run: mvn test
    
    - name: Generate coverage report
      run: mvn jacoco:report
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
```

### 2. Docker Configuration

**File**: `Dockerfile`

```dockerfile
# Multi-stage build
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**File**: `docker-compose.yml`

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: warehouse_ems
      POSTGRES_USER: ems_user
      POSTGRES_PASSWORD: ems_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
  
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_URL: jdbc:postgresql://postgres:5432/warehouse_ems
      DB_USERNAME: ems_user
      DB_PASSWORD: ems_password
    depends_on:
      - postgres

volumes:
  postgres_data:
```

### 3. Kubernetes Deployment

**File**: `k8s/deployment.yml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: warehouse-ems
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
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
```

---

## Summary

This comprehensive technical design document provides:

1. **6 User Stories** breaking down Epic E01 into manageable development tasks
2. **Detailed Technical Specifications** for each user story including:
   - Complete code examples
   - Configuration files
   - Database schemas
   - Implementation guidelines
   - Verification steps

3. **Spring Boot Best Practices** including:
   - Proper package structure
   - Configuration management
   - Exception handling
   - Logging strategy
   - Testing approach

4. **Production-Ready Features**:
   - Database migrations with Flyway
   - Health monitoring with Actuator
   - Comprehensive documentation
   - Multi-environment configuration
   - Docker and Kubernetes support

All specifications follow Spring Boot industry standards and are designed for easy consumption by Spring Boot developers. The document ensures consistency, maintainability, and scalability of the Warehouse EMS application.
