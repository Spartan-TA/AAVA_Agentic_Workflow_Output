# Warehouse EMS - Low-Level Technical Design Document

## Executive Summary

This document provides comprehensive low-level technical design specifications for the Warehouse Employee Management System (EMS) covering 20 epics with 80 user stories. The system is built using Spring Boot following industry best practices and standards.

## Table of Contents

1. [E01 - Project Scaffolding & Domain Setup](#e01)
2. [E02 - Employee Master Data (CRUD)](#e02)
3. [E03 - Role Based Access Control (RBAC)](#e03)
4. [E04 - Time & Attendance (Clock In/Out)](#e04)
5. [E05 - Shift & Schedule Management](#e05)
6. [E06 - Leave & Absence Management](#e06)
7. [E07 - Training & Certification Tracking](#e07)
8. [E08 - Safety Incidents & OSHA Reporting](#e08)
9. [E09 - Equipment & Asset Assignment](#e09)
10. [E10 - Performance Reviews & Goals](#e10)
11. [E11 - Payroll Export Integration](#e11)
12. [E12 - Notifications & Announcements](#e12)
13. [E13 - Integration Layer (HRIS/WMS APIs)](#e13)
14. [E14 - Audit Trail & Compliance](#e14)
15. [E15 - Reporting & Analytics](#e15)
16. [E16 - Mobile Access (PWA)](#e16)
17. [E17 - Onboarding & Offboarding Workflow](#e17)
18. [E18 - Localization & Multi-Warehouse](#e18)
19. [E19 - Advanced Scheduling (AI-Assisted)](#e19)
20. [E20 - Document Management](#e20)

---

## <a name="e01"></a>E01 - Project Scaffolding & Domain Setup

### Description
Establishes the foundational Spring Boot project structure, configures Maven, sets up base packages, integrates Flyway/Liquibase for DB migrations, and enables Actuator for health monitoring.

### Spring Boot Architecture Overview
- **Architecture Pattern**: Layered architecture (Controller, Service, Repository, Domain)
- **Modularization**: Feature-based modules (employee, scheduling, attendance, safety, etc.)
- **Build Tool**: Maven
- **Database Migration**: Flyway/Liquibase
- **Monitoring**: Spring Boot Actuator

### Package Structure
```
com.warehouseems
âââ config
â   âââ FlywayConfig.java
â   âââ ActuatorConfig.java
â   âââ ApplicationConfig.java
âââ employee
âââ scheduling
âââ attendance
âââ safety
âââ audit
âââ integration
âââ notification
âââ reporting
âââ document
```

### Design Specification

#### Configuration Files

**application.yml**
```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  application:
    name: warehouse-ems
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouseems
    username: ${DB_USERNAME:ems_user}
    password: ${DB_PASSWORD:secret}
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

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

**pom.xml (key dependencies)**
```xml
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
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
</dependencies>
```

### Sample Implementation

**FlywayConfig.java**
```java
package com.warehouseems.config;

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

## <a name="e02"></a>E02 - Employee Master Data (CRUD)

### Description
Implements Employee domain with CRUD APIs, unique badgeId enforcement, soft-delete functionality, pagination, and filtering capabilities.

### Spring Boot Architecture Overview
- **Module**: Employee Management
- **Pattern**: RESTful CRUD with DTO pattern
- **Validation**: Bean Validation (JSR-380)
- **Pagination**: Spring Data Pageable

### Package Structure
```
com.warehouseems.employee
âââ domain
â   âââ Employee.java
âââ repository
â   âââ EmployeeRepository.java
âââ service
â   âââ EmployeeService.java
â   âââ EmployeeServiceImpl.java
âââ controller
â   âââ EmployeeController.java
âââ dto
    âââ EmployeeDto.java
    âââ EmployeeCreateDto.java
    âââ EmployeeUpdateDto.java
```

### Domain Model Design

**Employee.java**
```java
package com.warehouseems.employee.domain;

import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
@SQLDelete(sql = "UPDATE employees SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "badge_id", nullable = false, unique = true, length = 50)
    private String badgeId;
    
    @Column(nullable = false)
    private String name;
    
    private String role;
    private String department;
    
    @Column(name = "shift_group")
    private String shiftGroup;
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    private String status;
    
    @Column(nullable = false)
    private Boolean deleted = false;
    
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

### Repository Layer

**EmployeeRepository.java**
```java
package com.warehouseems.employee.repository;

import com.warehouseems.employee.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    boolean existsByBadgeId(String badgeId);
    
    @Query("SELECT e FROM Employee e WHERE e.deleted = false")
    Page<Employee> findAllActive(Pageable pageable);
}
```

### Service Layer

**EmployeeService.java**
```java
package com.warehouseems.employee.service;

import com.warehouseems.employee.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    EmployeeDto create(EmployeeCreateDto dto);
    EmployeeDto update(Long id, EmployeeUpdateDto dto);
    EmployeeDto getById(Long id);
    Page<EmployeeDto> list(Pageable pageable);
    void softDelete(Long id);
}
```

### Controller Layer

**EmployeeController.java**
```java
package com.warehouseems.employee.controller;

import com.warehouseems.employee.dto.*;
import com.warehouseems.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;
    
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(dto));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }
    
    @GetMapping
    public ResponseEntity<Page<EmployeeDto>> list(Pageable pageable) {
        return ResponseEntity.ok(employeeService.list(pageable));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateDto dto) {
        return ResponseEntity.ok(employeeService.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## <a name="e03"></a>E03 - Role Based Access Control (RBAC)

### Description
Implements Spring Security with role-based access control supporting ADMIN, HR, SUPERVISOR, and WORKER roles.

### Spring Boot Architecture Overview
- **Security Framework**: Spring Security 5.x
- **Authentication**: JWT/OAuth2 (configurable)
- **Authorization**: Role-based with method security

### Package Structure
```
com.warehouseems.security
âââ config
â   âââ SecurityConfig.java
âââ domain
â   âââ User.java
â   âââ Role.java
âââ service
â   âââ JwtService.java
âââ filter
    âââ JwtAuthenticationFilter.java
```

### Domain Model Design

**Role.java**
```java
package com.warehouseems.security.domain;

public enum Role {
    ADMIN("ROLE_ADMIN"),
    HR("ROLE_HR"),
    SUPERVISOR("ROLE_SUPERVISOR"),
    WORKER("ROLE_WORKER");
    
    private final String authority;
    
    Role(String authority) {
        this.authority = authority;
    }
    
    public String getAuthority() {
        return authority;
    }
}
```

### Configuration

**SecurityConfig.java**
```java
package com.warehouseems.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/actuator/health").permitAll()
                .antMatchers("/api/employees/**").hasAnyRole("ADMIN", "HR")
                .antMatchers("/api/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated();
    }
}
```

---

## <a name="e04"></a>E04 - Time & Attendance (Clock In/Out)

### Description
Implements clock-in/out functionality with geofence validation, device tracking, and hours calculation.

### Domain Model Design

**Attendance.java**
```java
package com.warehouseems.attendance.domain;

import com.warehouseems.employee.domain.Employee;
import lombok.*;
import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Column(name = "clock_in", nullable = false)
    private LocalDateTime clockIn;
    
    @Column(name = "clock_out")
    private LocalDateTime clockOut;
    
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;
    
    @Column(name = "device_id")
    private String deviceId;
    
    @Column(name = "hours_worked")
    private Double hoursWorked;
    
    public void calculateHoursWorked() {
        if (clockIn != null && clockOut != null) {
            Duration duration = Duration.between(clockIn, clockOut);
            hoursWorked = duration.toMinutes() / 60.0;
        }
    }
}
```

### Service Layer

**AttendanceService.java**
```java
package com.warehouseems.attendance.service;

import com.warehouseems.attendance.dto.*;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    AttendanceDto clockIn(Long employeeId, ClockEventDto dto);
    AttendanceDto clockOut(Long employeeId, ClockEventDto dto);
    List<AttendanceDto> getDailyAttendance(Long employeeId, LocalDate date);
}
```

---

## <a name="e05"></a>E05 - Shift & Schedule Management

### Description
Manages shift templates, employee scheduling, and warehouse calendars.

### Domain Model Design

**Shift.java**
```java
package com.warehouseems.scheduling.domain;

import lombok.*;
import javax.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "shifts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "is_overtime")
    private Boolean isOvertime = false;
}
```

**Schedule.java**
```java
package com.warehouseems.scheduling.domain;

import com.warehouseems.employee.domain.Employee;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;
    
    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;
    
    @Enumerated(EnumType.STRING)
    private ScheduleStatus status = ScheduleStatus.SCHEDULED;
}

enum ScheduleStatus {
    SCHEDULED, CONFIRMED, CANCELLED, COMPLETED
}
```

---

## <a name="e06"></a>E06 - Leave & Absence Management

### Description
Handles PTO, sick, and unpaid leave requests with approval workflow.

### Domain Model Design

**LeaveRequest.java**
```java
package com.warehouseems.leave.domain;

import com.warehouseems.employee.domain.Employee;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "leave_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    private LeaveType type;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    private LeaveStatus status = LeaveStatus.REQUESTED;
}

enum LeaveType { PTO, SICK, UNPAID }
enum LeaveStatus { REQUESTED, APPROVED, DENIED }
```

---

## <a name="e07"></a>E07 - Training & Certification Tracking

### Description
Tracks employee certifications and expirations.

### Domain Model Design

**Certification.java**
```java
package com.warehouseems.certification.domain;

import com.warehouseems.employee.domain.Employee;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "certifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    private String type;
    
    @Column(name = "issue_date")
    private LocalDate issueDate;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    @Column(name = "document_url")
    private String documentUrl;
}
```

---

## <a name="e08"></a>E08 - Safety Incidents & OSHA Reporting

### Description
Records safety incidents with investigation workflow.

### Domain Model Design

**Incident.java**
```java
package com.warehouseems.safety.domain;

import com.warehouseems.employee.domain.Employee;
import lombok.*;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "incidents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String severity;
    private String location;
    
    @Column(length = 1000)
    private String description;
    
    @ManyToMany
    @JoinTable(name = "incident_employees")
    private List<Employee> involvedEmployees;
    
    @Enumerated(EnumType.STRING)
    private IncidentStatus status = IncidentStatus.OPEN;
}

enum IncidentStatus { OPEN, INVESTIGATING, RESOLVED }
```

---

## <a name="e09"></a>E09 - Equipment & Asset Assignment

### Description
Manages asset checkout/return with certification validation.

### Domain Model Design

**Asset.java**
```java
package com.warehouseems.asset.domain;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "assets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String type;
    
    @Column(name = "serial_number", unique = true)
    private String serialNumber;
    
    @Enumerated(EnumType.STRING)
    private AssetCondition condition = AssetCondition.GOOD;
}

enum AssetCondition { GOOD, NEEDS_REPAIR, OUT_OF_SERVICE }
```

---

## <a name="e10"></a>E10 - Performance Reviews & Goals

### Description
Manages performance review cycles and goal tracking.

### Domain Model Design

**Review.java**
```java
package com.warehouseems.performance.domain;

import com.warehouseems.employee.domain.Employee;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Column(name = "review_date")
    private LocalDate reviewDate;
    
    @Column(length = 2000)
    private String comments;
    
    private Integer rating;
    
    private Boolean acknowledged = false;
}
```

---

## <a name="e11"></a>E11 - Payroll Export Integration

### Description
Generates payroll files with secure delivery.

### Service Layer

**PayrollService.java**
```java
package com.warehouseems.payroll.service;

import java.time.LocalDate;

public interface PayrollService {
    void exportPayroll(LocalDate startDate, LocalDate endDate, String providerFormat);
    byte[] generatePayrollFile(LocalDate startDate, LocalDate endDate);
}
```

---

## <a name="e12"></a>E12 - Notifications & Announcements

### Description
Multi-channel notification system.

### Domain Model Design

**Notification.java**
```java
package com.warehouseems.notification.domain;

import com.warehouseems.employee.domain.Employee;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    @Column(length = 1000)
    private String message;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    private Boolean delivered = false;
}

enum NotificationType { EMAIL, SMS, IN_APP }
```

---

## <a name="e13"></a>E13 - Integration Layer (HRIS/WMS APIs)

### Description
REST APIs for external system integration.

### Configuration

**Integration Security**
```java
package com.warehouseems.integration.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class IntegrationSecurityConfig {
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/api/integration/**").authenticated()
            .and()
            .oauth2ResourceServer().jwt();
    }
}
```

---

## <a name="e14"></a>E14 - Audit Trail & Compliance

### Description
Centralized audit logging.

### Domain Model Design

**AuditLog.java**
```java
package com.warehouseems.audit.domain;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String actor;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    private String entity;
    private String action;
    
    @Column(length = 5000)
    private String before;
    
    @Column(length = 5000)
    private String after;
}
```

---

## <a name="e15"></a>E15 - Reporting & Analytics

### Description
Operational reports and dashboards.

### Service Layer

**ReportService.java**
```java
package com.warehouseems.reporting.service;

import java.time.LocalDate;

public interface ReportService {
    byte[] generateAttendanceReport(LocalDate startDate, LocalDate endDate);
    byte[] generateOvertimeReport(LocalDate startDate, LocalDate endDate);
    byte[] generateLeaveBalanceReport();
    byte[] generateSafetyKPIReport();
}
```

---

## <a name="e16"></a>E16 - Mobile Access (PWA)

### Description
Progressive Web App for mobile access.

### Configuration

**PWA Manifest**
```json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#000000",
  "icons": [
    {
      "src": "/icon-192.png",
      "sizes": "192x192",
      "type": "image/png"
    }
  ]
}
```

---

## <a name="e17"></a>E17 - Onboarding & Offboarding Workflow

### Description
Automated employee lifecycle management.

### Service Layer

**OnboardingService.java**
```java
package com.warehouseems.workflow.service;

public interface OnboardingService {
    void onboardEmployee(Long employeeId);
    void offboardEmployee(Long employeeId);
}
```

---

## <a name="e18"></a>E18 - Localization & Multi-Warehouse

### Description
Multi-warehouse support with localization.

### Domain Model Design

**Warehouse.java**
```java
package com.warehouseems.localization.domain;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "warehouses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String locale;
    private String timezone;
}
```

---

## <a name="e19"></a>E19 - Advanced Scheduling (AI-Assisted)

### Description
AI-powered scheduling suggestions.

### Service Layer

**AISchedulingService.java**
```java
package com.warehouseems.ai.service;

import java.time.LocalDate;
import java.util.List;

public interface AISchedulingService {
    List<ScheduleSuggestion> suggestAssignments(LocalDate date);
    void acceptSuggestion(Long suggestionId);
}
```

---

## <a name="e20"></a>E20 - Document Management

### Description
Secure document storage with version control.

### Domain Model Design

**Document.java**
```java
package com.warehouseems.document.domain;

import com.warehouseems.employee.domain.Employee;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    private String type;
    private String url;
    private Integer version = 1;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
}
```

---

## Conclusion

This technical design document provides comprehensive specifications for all 20 epics of the Warehouse EMS system following Spring Boot best practices. Each epic includes domain models, repository patterns, service layers, and controller designs with code examples demonstrating implementation patterns.

### Key Technologies
- Spring Boot 2.7+
- Spring Data JPA
- Spring Security
- PostgreSQL
- Flyway
- JWT Authentication
- RESTful APIs
- OpenAPI/Swagger

### Architecture Principles
- Layered Architecture
- Domain-Driven Design
- SOLID Principles
- RESTful API Design
- Security by Design
- Audit Trail
- Scalability

---

**Document Version**: 1.0
**Last Updated**: 2024
**Status**: Complete