# Low-Level Technical Design Document
# Warehouse Employee Management System

## Document Information
- **Version**: 1.0
- **Date**: 2024
- **Framework**: Spring Boot 3.x
- **Java Version**: 17+
- **Build Tool**: Maven
- **Database**: PostgreSQL

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

---

## E01: Project Scaffolding & Domain Setup

### Overview
Initialize a Spring Boot Maven project with a modular architecture supporting employee management, scheduling, attendance, and safety modules. Configure database migrations and monitoring.

### Section: Project Structure

**Description**: Establish a clean, maintainable package structure following Spring Boot conventions and domain-driven design principles.

**Design Specification**:
- Root package: `com.warehouse.employee`
- Module packages:
  - `com.warehouse.employee.domain` - Domain entities
  - `com.warehouse.employee.repository` - Data access layer
  - `com.warehouse.employee.service` - Business logic
  - `com.warehouse.employee.controller` - REST controllers
  - `com.warehouse.employee.dto` - Data transfer objects
  - `com.warehouse.employee.config` - Configuration classes
  - `com.warehouse.employee.security` - Security components
  - `com.warehouse.employee.exception` - Exception handling
  - `com.warehouse.employee.util` - Utility classes

**Sample Implementation**:

```xml
<!-- pom.xml -->
<project>
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.warehouse</groupId>
    <artifactId>employee-management</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
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
            <artifactId>spring-boot-starter-security</artifactId>
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
        
        <!-- Flyway for migrations -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        
        <!-- OpenAPI Documentation -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.2.0</version>
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
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### Section: Application Configuration

**Description**: Configure application properties for database, server, and monitoring.

**Design Specification**:
- Server port: 8080
- Database connection pooling with HikariCP
- Flyway migration enabled
- Actuator health endpoint exposed
- Logging configuration

**Sample Implementation**:

```yaml
# application.yml
spring:
  application:
    name: warehouse-employee-management
  
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_db
    username: ${DB_USERNAME:warehouse_user}
    password: ${DB_PASSWORD:warehouse_pass}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
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
    baseline-version: 0

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
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized

logging:
  level:
    com.warehouse.employee: INFO
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

### Section: Main Application Class

**Description**: Bootstrap Spring Boot application with necessary configurations.

**Sample Implementation**:

```java
package com.warehouse.employee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class WarehouseEmployeeManagementApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeManagementApplication.class, args);
    }
}
```

### Section: Database Migration Setup

**Description**: Initialize Flyway migration scripts for baseline schema.

**Sample Implementation**:

```sql
-- db/migration/V1__baseline_schema.sql
CREATE TABLE IF NOT EXISTS employees (
    id BIGSERIAL PRIMARY KEY,
    badge_id VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(50) NOT NULL,
    department VARCHAR(100),
    shift_group VARCHAR(50),
    hire_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_employees_badge_id ON employees(badge_id);
CREATE INDEX idx_employees_status ON employees(status);
CREATE INDEX idx_employees_department ON employees(department);
```

---

## E02: Employee Master Data (CRUD)

### Overview
Implement complete CRUD operations for employee master data with validation, pagination, filtering, and soft delete support.

### Section: Domain Model

**Description**: Define the Employee entity with JPA annotations, audit fields, and soft delete support.

**Design Specification**:
- Entity name: Employee
- Primary key: Long id (auto-generated)
- Unique constraints: badgeId, email
- Soft delete: deletedAt timestamp
- Audit fields: createdAt, updatedAt, createdBy, updatedBy
- Enums: EmployeeRole, EmployeeStatus

**Sample Implementation**:

```java
package com.warehouse.employee.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "badge_id", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Badge ID is required")
    private String badgeId;
    
    @Column(name = "first_name", nullable = false, length = 100)
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 100)
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @Column(name = "email", unique = true, nullable = false)
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    @NotNull(message = "Role is required")
    private EmployeeRole role;
    
    @Column(name = "department", length = 100)
    private String department;
    
    @Column(name = "shift_group", length = 50)
    private String shiftGroup;
    
    @Column(name = "hire_date", nullable = false)
    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVE;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @Transient
    public boolean isDeleted() {
        return deletedAt != null;
    }
    
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.status = EmployeeStatus.TERMINATED;
    }
}

// Enums
public enum EmployeeRole {
    ADMIN, HR, SUPERVISOR, WORKER
}

public enum EmployeeStatus {
    ACTIVE, INACTIVE, ON_LEAVE, TERMINATED
}
```

### Section: Data Transfer Objects (DTOs)

**Description**: Define request and response DTOs for API operations with validation.

**Sample Implementation**:

```java
package com.warehouse.employee.dto;

import com.warehouse.employee.domain.EmployeeRole;
import com.warehouse.employee.domain.EmployeeStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequestDTO {
    
    @NotBlank(message = "Badge ID is required")
    @Size(max = 50)
    private String badgeId;
    
    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @Size(max = 20)
    private String phone;
    
    @NotNull(message = "Role is required")
    private EmployeeRole role;
    
    @Size(max = 100)
    private String department;
    
    @Size(max = 50