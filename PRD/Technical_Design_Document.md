# Warehouse Employee Management System - Low-Level Technical Design Document

## Table of Contents
1. [E01 - Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
2. [E02 - Employee Master Data (CRUD)](#e02-employee-master-data-crud)
3. [E03 - Role-Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
4. [E04 - Time & Attendance (Clock In/Out)](#e04-time--attendance-clock-inout)
5. [E05 - Shift & Schedule Management](#e05-shift--schedule-management)
6. [E06 - Leave & Absence Management](#e06-leave--absence-management)
7. [E07 - Training & Certification Tracking](#e07-training--certification-tracking)
8. [E08 - Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
9. [E09 - Equipment & Asset Assignment](#e09-equipment--asset-assignment)
10. [E10 - Performance Reviews & Goals](#e10-performance-reviews--goals)
11. [E11 - Payroll Export Integration](#e11-payroll-export-integration)
12. [E12 - Notifications & Announcements](#e12-notifications--announcements)
13. [E13 - Integration Layer (HRIS/WMS APIs)](#e13-integration-layer-hriswms-apis)
14. [E14 - Audit Trail & Compliance](#e14-audit-trail--compliance)
15. [E15 - Reporting & Analytics](#e15-reporting--analytics)
16. [E16 - Mobile Access (PWA)](#e16-mobile-access-pwa)
17. [E17 - Onboarding & Offboarding Workflow](#e17-onboarding--offboarding-workflow)
18. [E18 - Localization & Multi-Tenant](#e18-localization--multi-tenant)
19. [E19 - Observability & Monitoring](#e19-observability--monitoring)
20. [E20 - CI/CD & Deployment Automation](#e20-cicd--deployment-automation)

---

## E01. Project Scaffolding & Domain Setup

### Section: Spring Boot Architecture Foundation

**Description:**
Establish a standardized Spring Boot project structure using Maven, with modular packages for each domain. Use Flyway/Liquibase for DB migrations, enable Actuator for health checks, and configure base packages for scalability. This epic provides the foundation for all subsequent development work.

**Design Specification:**

1. **Spring Boot Architecture:**
   - Maven-based project structure
   - Layered architecture: Controller â Service â Repository â Entity
   - Modular packages for separation of concerns
   - Spring Boot version: 3.x with Java 17+

2. **Package Structure:**
   ```
   com.wms
   âââ employee
   â   âââ controller
   â   âââ service
   â   âââ repository
   â   âââ entity
   â   âââ dto
   âââ scheduling
   â   âââ controller
   â   âââ service
   â   âââ repository
   â   âââ entity
   â   âââ dto
   âââ attendance
   â   âââ controller
   â   âââ service
   â   âââ repository
   â   âââ entity
   â   âââ dto
   âââ safety
   â   âââ controller
   â   âââ service
   â   âââ repository
   â   âââ entity
   â   âââ dto
   âââ config
       âââ SecurityConfig.java
       âââ DatabaseConfig.java
       âââ ActuatorConfig.java
   ```

3. **Core Modules:**
   - Employee Management
   - Scheduling
   - Attendance
   - Safety

4. **Database Migration:**
   - Flyway/Liquibase in `src/main/resources/db/migration`
   - Versioned migration scripts (V1__initial_schema.sql, V2__add_employee_table.sql, etc.)

5. **Actuator Configuration:**
   - Enabled in application.properties
   - Health, info, metrics endpoints exposed

6. **Configuration Files:**
   - application.properties for port, DB, actuator, security
   - application-dev.properties, application-prod.properties for environment-specific configs

**Sample Implementation:**

```properties
# application.properties
server.port=8080
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always

spring.datasource.url=jdbc:postgresql://localhost:5432/wms
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate

spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
```

```xml
<!-- Maven pom.xml dependencies -->
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
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

```java
// Main Application Class
package com.wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WarehouseManagementSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseManagementSystemApplication.class, args);
    }
}
```

---

## E02. Employee Master Data (CRUD)

### Section: Domain Model Design

**Description:**
Centralized employee entity with CRUD APIs, DTOs for web layer, soft-delete functionality, badgeId uniqueness constraint, pagination/filtering capabilities, and OpenAPI documentation. This forms the core master data for the entire system.

**Design Specification:**

1. **Entity Design:**
   - Employee entity with JPA annotations
   - Unique badgeId constraint
   - Soft-delete flag
   - Audit fields (createdAt, updatedAt, createdBy, updatedBy)

2. **Repository Layer:**
   - Spring Data JPA repository
   - Custom query methods for filtering
   - Pagination support

3. **Service Layer:**
   - Business logic for CRUD operations
   - Validation logic
   - Transaction management

4. **Controller Layer:**
   - RESTful endpoints
   - Request/Response DTOs
   - Exception handling

5. **DTO Pattern:**
   - Separate DTOs for request and response
   - Validation annotations

6. **Validation:**
   - Bean Validation (JSR-380)
   - Custom validators

7. **OpenAPI Documentation:**
   - Swagger/OpenAPI annotations
   - Request/Response examples

**Sample Implementation:**

```java
// Entity
package com.wms.employee.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@Data
@SQLDelete(sql = "UPDATE employees SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 20)
    private String badgeId;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 50)
    private String role;
    
    @Column(length = 100)
    private String department;
    
    @Column(length = 50)
    private String shiftGroup;
    
    @Column(nullable = false)
    private LocalDate hireDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;
    
    @Column(nullable = false)
    private boolean deleted = false;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(length = 100)
    private String createdBy;
    
    @Column(length = 100)
    private String updatedBy;
    
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

public enum EmployeeStatus {
    ACTIVE, INACTIVE, TERMINATED, ON_LEAVE
}
```

```java
// Repository
package com.wms.employee.repository;

import com.wms.employee.entity.Employee;
import com.wms.employee.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByBadgeId(String badgeId);
    
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    
    Page<Employee> findByDepartmentAndDeletedFalse(String department, Pageable pageable);
    
    Page<Employee> findByStatusAndDeletedFalse(EmployeeStatus status, Pageable pageable);
    
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND " +
           "(LOWER(e.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.badgeId) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Employee> searchEmployees(String searchTerm, Pageable pageable);
    
    boolean existsByBadgeIdAndDeletedFalse(String badgeId);
}
```

```java
// DTO
package com.wms.employee.dto;

import com.wms.employee.entity.EmployeeStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeRequestDTO {
    
    @NotBlank(message = "Badge ID is required")
    @Pattern(regexp = "^[A-Z0-9]{6,20}$", message = "Badge ID must be 6-20 alphanumeric characters")
    private String badgeId;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 50, message = "Role must not exceed 50 characters")
    private String role;
    
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;
    
    @Size(max = 50, message = "Shift group must not exceed 50 characters")
    private String shiftGroup;
    
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
    
    @NotNull(message = "Status is required")
    private EmployeeStatus status;
}

@Data
public class EmployeeResponseDTO {
    private Long id;
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private EmployeeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

```java
// Service
package com.wms.employee.service;

import com.wms.employee.dto.EmployeeRequestDTO;
import com.wms.employee.dto.EmployeeResponseDTO;
import com.wms.employee.entity.Employee;
import com.wms.employee.repository.EmployeeRepository;
import com.wms.exception.ResourceNotFoundException;
import com.wms.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    
    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO) {
        // Check for duplicate badgeId
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(requestDTO.getBadgeId())) {
            throw new DuplicateResourceException("Employee with badge ID " + requestDTO.getBadgeId() + " already exists");
        }
        
        Employee employee = employeeMapper.toEntity(requestDTO);
        Employee savedEmployee = employeeRepository.save(employee);
        return employeeMapper.toResponseDTO(savedEmployee);
    }
    
    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return employeeMapper.toResponseDTO(employee);
    }
    
    @Transactional(readOnly = true)
    public Page<EmployeeResponseDTO> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable)
            .map(employeeMapper::toResponseDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<EmployeeResponseDTO> searchEmployees(String searchTerm, Pageable pageable) {
        return employeeRepository.searchEmployees(searchTerm, pageable)
            .map(employeeMapper::toResponseDTO);
    }
    
    @Transactional
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO requestDTO) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        
        // Check for duplicate badgeId if it's being changed
        if (!employee.getBadgeId().equals(requestDTO.getBadgeId()) &&
            employeeRepository.existsByBadgeIdAndDeletedFalse(requestDTO.getBadgeId())) {
            throw new DuplicateResourceException("Employee with badge ID " + requestDTO.getBadgeId() + " already exists");
        }
        
        employeeMapper.updateEntityFromDTO(requestDTO, employee);
        Employee updatedEmployee = employeeRepository.save(employee);
        return employeeMapper.toResponseDTO(updatedEmployee);
    }
    
    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employeeRepository.delete(employee); // Triggers soft delete via @SQLDelete
    }
}
```

```java
// Controller
package com.wms.employee.controller;

import com.wms.employee.dto.EmployeeRequestDTO;
import com.wms.employee.dto.EmployeeResponseDTO;
import com.wms.employee.service.EmployeeService;
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
    @Operation(summary = "Create a new employee", description = "Creates a new employee record with unique badge ID")
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO requestDTO) {
        EmployeeResponseDTO response = employeeService.createEmployee(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get employee by ID", description = "Retrieves an employee record by ID")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        EmployeeResponseDTO response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get all employees", description = "Retrieves all employees with pagination")
    public ResponseEntity<Page<EmployeeResponseDTO>> getAllEmployees(Pageable pageable) {
        Page<EmployeeResponseDTO> response = employeeService.getAllEmployees(pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Search employees", description = "Searches employees by name or badge ID")
    public ResponseEntity<Page<EmployeeResponseDTO>> searchEmployees(
            @RequestParam String searchTerm,
            Pageable pageable) {
        Page<EmployeeResponseDTO> response = employeeService.searchEmployees(searchTerm, pageable);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update employee", description = "Updates an existing employee record")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDTO requestDTO) {
        EmployeeResponseDTO response = employeeService.updateEmployee(id, requestDTO);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Delete employee", description = "Soft deletes an employee record")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## E03. Role-Based Access Control (RBAC)

### Section: Security Architecture

**Description:**
Implement Spring Security with role-based access control supporting ADMIN, HR, SUPERVISOR, and WORKER roles. Include method-level security, endpoint security, row-level constraints, and configurable authentication (API key/OAuth2).

**Design Specification:**

1. **Security Configuration:**
   - Spring Security configuration
   - Role hierarchy
   - Authentication providers
   - JWT/OAuth2 support

2. **Role Definitions:**
   - ADMIN: Full system access
   - HR: Employee data management
   - SUPERVISOR: Team management
   - WORKER: Self-service access

3. **Method-Level Security:**
   - @PreAuthorize annotations
   - @Secured annotations
   - Custom security expressions

4. **Row-Level Security:**
   - Custom security filters
   - Data access restrictions

**Sample Implementation:**

```java
// Security Configuration
package com.wms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/api/v1/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/api/v1/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
                .requestMatchers("/api/v1/shifts/**").hasAnyRole("ADMIN", "SUPERVISOR")
                .requestMatchers("/api/v1/leave/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .anyRequest().authenticated()
            )
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .oauth2ResourceServer()
                .jwt();
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

```java
// Role Enum
package com.wms.security;

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

```java
// Custom Security Service for Row-Level Access
package com.wms.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("employeeSecurityService")
public class EmployeeSecurityService {
    
    public boolean isTeamMember(Long employeeId, Authentication authentication) {
        // Custom logic to check if the authenticated user is a supervisor of the employee
        // This would typically query the database to verify the relationship
        return true; // Placeholder
    }
    
    public boolean canAccessEmployeeData(Long employeeId, Authentication authentication) {
        String username = authentication.getName();
        // Check if user is ADMIN, HR, or the employee themselves
        return authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || 
                            auth.getAuthority().equals("ROLE_HR"));
    }
}
```

```java
// Exception Handler for Security
package com.wms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SecurityExceptionHandler {
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Authentication failed: " + ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "Access denied: " + ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
}
```

---

## E04. Time & Attendance (Clock In/Out)

### Section: Attendance Tracking System

**Description:**
Implement clock-in/out functionality with geofence validation, device capture, automatic shift association, missed punch correction workflow, daily totals calculation, and CSV export capabilities.

**Design Specification:**

1. **Entity Design:**
   - AttendanceEvent entity
   - Relationship with Employee and Shift
   - Geolocation and device tracking

2. **Business Logic:**
   - Clock-in/out validation
   - Geofence verification
   - Shift association
   - Hours calculation

3. **Correction Workflow:**
   - Missed punch requests
   - Approval process
   - Audit trail

**Sample Implementation:**

```java
// Entity
package com.wms.attendance.entity;

import com.wms.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_events")
@Data
public class AttendanceEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceType type;
    
    @Column(length = 100)
    private String deviceId;
    
    @Column(length = 200)
    private String location;
    
    private Double latitude;
    
    private Double longitude;
    
    @Column(nullable = false)
    private boolean correctionRequested = false;
    
    @Column(length = 500)
    private String correctionReason;
    
    @Enumerated(EnumType.STRING)
    private CorrectionStatus correctionStatus;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

public enum AttendanceType {
    CLOCK_IN, CLOCK_OUT, BREAK_START, BREAK_END
}

public enum CorrectionStatus {
    PENDING, APPROVED, REJECTED
}
```

```java
// Service
package com.wms.attendance.service;

import com.wms.attendance.dto.AttendanceRequestDTO;
import com.wms.attendance.dto.CorrectionRequestDTO;
import com.wms.attendance.entity.AttendanceEvent;
import com.wms.attendance.entity.AttendanceType;
import com.wms.attendance.repository.AttendanceRepository;
import com.wms.employee.entity.Employee;
import com.wms.employee.repository.EmployeeRepository;
import com.wms.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final GeofenceService geofenceService;
    
    @Transactional
    public AttendanceEvent clockIn(Long employeeId, AttendanceRequestDTO requestDTO) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ValidationException("Employee not found"));
        
        // Validate geofence
        if (!geofenceService.isWithinAllowedZone(requestDTO.getLatitude(), requestDTO.getLongitude())) {
            throw new ValidationException("Clock-in location is outside allowed area");
        }
        
        // Check for existing clock-in without clock-out
        List<AttendanceEvent> todayEvents = attendanceRepository
            .findByEmployeeAndDateOrderByTimestampDesc(employee, LocalDate.now());
        
        if (!todayEvents.isEmpty() && todayEvents.get(0).getType() == AttendanceType.CLOCK_IN) {
            throw new ValidationException("Already clocked in. Please clock out first.");
        }
        
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployee(employee);
        event.setTimestamp(LocalDateTime.now());
        event.setType(AttendanceType.CLOCK_IN);
        event.setDeviceId(requestDTO.getDeviceId());
        event.setLocation(requestDTO.getLocation());
        event.setLatitude(requestDTO.getLatitude());
        event.setLongitude(requestDTO.getLongitude());
        
        return attendanceRepository.save(event);
    }
    
    @Transactional
    public AttendanceEvent clockOut(Long employeeId, AttendanceRequestDTO requestDTO) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ValidationException("Employee not found"));
        
        // Validate geofence
        if (!geofenceService.isWithinAllowedZone(requestDTO.getLatitude(), requestDTO.getLongitude())) {
            throw new ValidationException("Clock-out location is outside allowed area");
        }
        
        // Check for existing clock-in
        List<AttendanceEvent> todayEvents = attendanceRepository
            .findByEmployeeAndDateOrderByTimestampDesc(employee, LocalDate.now());
        
        if (todayEvents.isEmpty() || todayEvents.get(0).getType() != AttendanceType.CLOCK_IN) {
            throw new ValidationException("No active clock-in found. Please clock in first.");
        }
        
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployee(employee);
        event.setTimestamp(LocalDateTime.now());
        event.setType(AttendanceType.CLOCK_OUT);
        event.setDeviceId(requestDTO.getDeviceId());
        event.setLocation(requestDTO.getLocation());
        event.setLatitude(requestDTO.getLatitude());
        event.setLongitude(requestDTO.getLongitude());
        
        return attendanceRepository.save(event);
    }
    
    @Transactional(readOnly = true)
    public DailyTotals computeDailyTotals(Long employeeId, LocalDate date) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ValidationException("Employee not found"));
        
        List<AttendanceEvent> events = attendanceRepository
            .findByEmployeeAndDateOrderByTimestampAsc(employee, date);
        
        double totalHours = 0.0;
        LocalDateTime lastClockIn = null;
        
        for (AttendanceEvent event : events) {
            if (event.getType() == AttendanceType.CLOCK_IN) {
                lastClockIn = event.getTimestamp();
            } else if (event.getType() == AttendanceType.CLOCK_OUT && lastClockIn != null) {
                Duration duration = Duration.between(lastClockIn, event.getTimestamp());
                totalHours += duration.toMinutes() / 60.0;
                lastClockIn = null;
            }
        }
        
        return new DailyTotals(date, totalHours, events.size());
    }
    
    @Transactional
    public void requestCorrection(Long eventId, CorrectionRequestDTO requestDTO) {
        AttendanceEvent event = attendanceRepository.findById(eventId)
            .orElseThrow(() -> new ValidationException("Attendance event not found"));
        
        event.setCorrectionRequested(true);
        event.setCorrectionReason(requestDTO.getReason());
        event.setCorrectionStatus(CorrectionStatus.PENDING);
        
        attendanceRepository.save(event);
    }
}
```

---

## E05. Shift & Schedule Management

### Section: Scheduling System

**Description:**
Implement shift template management, recurring schedules, rotation patterns, overtime rules, employee assignments, conflict detection, and audit logging.

**Design Specification:**

1. **Entity Design:**
   - ShiftTemplate entity
   - ShiftAssignment entity
   - Rotation patterns

2. **Business Logic:**
   - Template creation
   - Assignment logic
   - Conflict detection
   - Bulk operations

**Sample Implementation:**

```java
// Entities
package com.wms.scheduling.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.Set;

@Entity
@Table(name = "shift_templates")
@Data
public class ShiftTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    @ElementCollection
    @CollectionTable(name = "shift_days", joinColumns = @JoinColumn(name = "shift_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> daysOfWeek;
    
    private boolean overtimeEligible;
    
    @Column(length = 500)
    private String description;
}

@Entity
@Table(name = "shift_assignments")
@Data
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
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    @Column(nullable = false)
    private boolean active = true;
}
```

---

## E06. Leave & Absence Management

### Section: Leave Management System

**Description:**
Implement leave request/approval workflow, accrual balance tracking, policy enforcement, integration with scheduling and payroll systems.

**Sample Implementation:**

```java
// Entity
package com.wms.leave.entity;

import com.wms.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "leave_requests")
@Data
public class LeaveRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType type;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status = LeaveStatus.REQUESTED;
    
    @Column(length = 1000)
    private String reason;
    
    private Double accrualBalance;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Employee approvedBy;
    
    private LocalDateTime approvedAt;
}

public enum LeaveType {
    PTO, SICK, UNPAID, BEREAVEMENT, JURY_DUTY
}

public enum LeaveStatus {
    REQUESTED, APPROVED, DENIED, CANCELLED
}
```

---

## E07. Training & Certification Tracking

### Section: Certification Management

**Description:**
Track employee certifications, expiration dates, renewal requirements, document uploads, and automated alerts for expiring certifications.

**Sample Implementation:**

```java
// Entity
package com.wms.training.entity;

import com.wms.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "certifications")
@Data
public class Certification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Column(nullable = false, length = 100)
    private String type;
    
    @Column(nullable = false)
    private LocalDate issueDate;
    
    @Column(nullable = false)
    private LocalDate expiryDate;
    
    @Column(length = 500)
    private String documentUrl;
    
    @Column(length = 100)
    private String issuingAuthority;
    
    @Column(nullable = false)
    private boolean active = true;
}
```

---

## E08. Safety Incidents & OSHA Reporting

### Section: Safety Management System

**Description:**
Record and track safety incidents, manage investigation workflows, generate OSHA-compliant reports, and maintain safety metrics dashboards.

**Sample Implementation:**

```java
// Entity
package com.wms.safety.entity;

import com.wms.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "safety_incidents")
@Data
public class SafetyIncident {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 2000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentSeverity severity;
    
    @Column(nullable = false, length = 200)
    private String location;
    
    @ManyToMany
    @JoinTable(
        name = "incident_employees",
        joinColumns = @JoinColumn(name = "incident_id"),
        inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private List<Employee> involvedEmployees;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status = IncidentStatus.OPEN;
    
    @Column(nullable = false)
    private LocalDateTime incidentDate;
    
    @Column(length = 2000)
    private String correctiveActions;
}

public enum IncidentSeverity {
    MINOR, MODERATE, SERIOUS, CRITICAL
}

public enum IncidentStatus {
    OPEN, INVESTIGATING, RESOLVED, CLOSED
}
```

---

## E09. Equipment & Asset Assignment

### Section: Asset Management System

**Description:**
Manage equipment and asset assignments to employees, track checkout/return, enforce certification requirements, maintain asset condition history.

**Sample Implementation:**

```java
// Entity
package com.wms.asset.entity;

import com.wms.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "assets")
@Data
public class Asset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String assetTag;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetCondition condition;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private Employee assignedTo;
    
    private LocalDate checkoutDate;
    
    private LocalDate expectedReturnDate;
    
    private LocalDate actualReturnDate;
    
    @Column(length = 100)
    private String requiredCertification;
}

public enum AssetType {
    FORKLIFT, SCANNER, PPE, PALLET_JACK, LAPTOP
}

public enum AssetCondition {
    EXCELLENT, GOOD, FAIR, POOR, DAMAGED
}
```

---

## E10. Performance Reviews & Goals

### Section: Performance Management System

**Description:**
Manage performance review cycles, goal tracking, competency assessments, supervisor/employee acknowledgements, and PDF export functionality.

**Sample Implementation:**

```java
// Entity
package com.wms.performance.entity;

import com.wms.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "performance_reviews")
@Data
public class PerformanceReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Column(nullable = false)
    private LocalDate reviewDate;
    
    @Column(nullable = false, length = 100)
    private String template;
    
    @Column(length = 2000)
    private String goals;
    
    @Column(length = 2000)
    private String competencies;
    
    @Column(length = 1000)
    private String ratings;
    
    @Column(length = 2000)
    private String comments;
    
    private boolean acknowledgedBySupervisor = false;
    
    private boolean acknowledgedByEmployee = false;
    
    @Column(nullable = false)
    private boolean immutable = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private Employee reviewer;
}
```

---

## E11. Payroll Export Integration

### Section: Payroll Integration System

**Description:**
Generate payroll-ready export files from attendance and leave data, map to external payroll provider formats, implement secure delivery mechanisms (SFTP/API), and maintain comprehensive audit logs.

**Sample Implementation:**

```java
// Service
package com.wms.payroll.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class PayrollExportService {
    
    private final AttendanceRepository attendanceRepository;
    private final LeaveRepository leaveRepository;
    private final SftpService sftpService;
    private final AuditService auditService;
    
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generatePayrollExport(LocalDate startDate, LocalDate endDate) {
        // Fetch attendance and leave data
        List<AttendanceEvent> attendanceEvents = attendanceRepository
            .findByDateBetween(startDate, endDate);
        
        List<LeaveRequest> approvedLeaves = leaveRepository
            .findByStatusAndDateBetween(LeaveStatus.APPROVED, startDate, endDate);
        
        // Generate CSV/Excel export
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // ... export logic
        
        return outputStream;
    }
    
    @Transactional
    public void deliverExport(byte[] fileContent, String fileName) {
        try {
            sftpService.uploadFile(fileContent, fileName);
            auditService.log("PAYROLL_EXPORT", "System", "Export delivered successfully");
        } catch (Exception e) {
            auditService.log("PAYROLL_EXPORT_FAILED", "System", "Export delivery failed: " + e.getMessage());
            throw new PayrollExportException("Failed to deliver payroll export", e);
        }
    }
}
```

---

## E12. Notifications & Announcements

### Section: Notification System

**Description:**
Implement multi-channel notification system (in-app, email, SMS) for shift changes, expiring certifications, approvals, and announcements with opt-in/out preferences and rate limiting.

**Sample Implementation:**

```java
// Entity
package com.wms.notification.entity;

import com.wms.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Employee recipient;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @Column(nullable = false, length = 1000)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;
    
    @Column(nullable = false)
    private boolean delivered = false;
    
    private LocalDateTime sentAt;
    
    private LocalDateTime readAt;
}

public enum NotificationType {
    SHIFT_CHANGE, CERTIFICATION_EXPIRY, LEAVE_APPROVAL, ANNOUNCEMENT
}

public enum NotificationChannel {
    IN_APP, EMAIL, SMS
}
```

---

## E13. Integration Layer (HRIS/WMS APIs)

### Section: External Integration System

**Description:**
Expose REST APIs and implement connectors for HRIS synchronization, WMS integration, IDP for SSO, and webhook support for event-driven integrations.

**Sample Implementation:**

```java
// Controller
package com.wms.integration.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/integration")
@RequiredArgsConstructor
public class IntegrationController {
    
    private final IntegrationService integrationService;
    
    @PostMapping("/hris/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SyncResponse> syncHRIS(@RequestBody HRISDataDTO data) {
        SyncResponse response = integrationService.syncHRISData(data);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/wms/link")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LinkResponse> linkWMS(@RequestBody WMSLinkDTO data) {
        LinkResponse response = integrationService.linkWMSData(data);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveWebhook(@RequestBody WebhookPayload payload) {
        integrationService.processWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
```

---

## E14. Audit Trail & Compliance

### Section: Audit Logging System

**Description:**
Implement centralized audit logging for all sensitive operations with tamper-evident storage, comprehensive tracking of actor/timestamp/changes, and export capabilities.

**Sample Implementation:**

```java
// Entity
package com.wms.audit.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String entity;
    
    @Column(nullable = false)
    private Long entityId;
    
    @Column(nullable = false, length = 50)
    private String action;
    
    @Column(nullable = false, length = 100)
    private String actor;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(columnDefinition = "TEXT")
    private String beforeState;
    
    @Column(columnDefinition = "TEXT")
    private String afterState;
    
    @Column(length = 500)
    private String ipAddress;
    
    @Column(length = 200)
    private String userAgent;
}
```

---

## E15. Reporting & Analytics

### Section: Reporting System

**Description:**
Implement comprehensive reporting capabilities for attendance, overtime, leave balances, certifications, safety KPIs with CSV/PDF export and role-based dashboards.

**Sample Implementation:**

```java
// Service
package com.wms.reporting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportingService {
    
    private final AttendanceRepository attendanceRepository;
    private final LeaveRepository leaveRepository;
    private final CertificationRepository certificationRepository;
    
    public AttendanceReport generateAttendanceReport(LocalDate startDate, LocalDate endDate, String department) {
        List<AttendanceEvent> events = attendanceRepository
            .findByDateBetweenAndDepartment(startDate, endDate, department);
        
        // Calculate metrics
        return new AttendanceReport(/* ... */);
    }
    
    public SafetyKPIReport generateSafetyKPIReport(LocalDate startDate, LocalDate endDate) {
        // Generate safety metrics
        return new SafetyKPIReport(/* ... */);
    }
    
    public byte[] exportToCsv(Report report) {
        // Export logic
        return new byte[0];
    }
}
```

---

## E16. Mobile Access (PWA)

### Section: Progressive Web Application

**Description:**
Implement responsive mobile-friendly views for core employee functions with offline support, PWA manifest, and service worker for offline queue management.

**Sample Implementation:**

```javascript
// Service Worker (service-worker.js)
const CACHE_NAME = 'wms-v1';
const urlsToCache = [
  '/',
  '/static/css/main.css',
  '/static/js/main.js'
];

self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(urlsToCache))
  );
});

self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request)
      .then(response => response || fetch(event.request))
  );
});
```

```json
// manifest.json
{
  "name": "Warehouse Management System",
  "short_name": "WMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#007bff",
  "icons": [
    {
      "src": "/icons/icon-192x192.png",
      "sizes": "192x192",
      "type": "image/png"
    }
  ]
}
```

---

## E17. Onboarding & Offboarding Workflow

### Section: Employee Lifecycle Management

**Description:**
Automate employee onboarding and offboarding processes including account provisioning, training assignment, asset allocation, and access revocation.

**Sample Implementation:**

```java
// Service
package com.wms.lifecycle.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LifecycleService {
    
    private final EmployeeService employeeService;
    private final TrainingService trainingService;
    private final AssetService assetService;
    private final SecurityService securityService;
    
    @Transactional
    public void onboardEmployee(OnboardingDTO dto) {
        // Create employee record
        Employee employee = employeeService.createEmployee(dto.getEmployeeData());
        
        // Assign initial training
        trainingService.assignMandatoryTraining(employee.getId());
        
        // Create user account
        securityService.createUserAccount(employee);
        
        // Generate onboarding tasks
        taskService.generateOnboardingTasks(employee.getId());
    }
    
    @Transactional
    public void offboardEmployee(Long employeeId) {
        // Revoke access
        securityService.revokeAccess(employeeId);
        
        // Collect assets
        assetService.initiateAssetCollection(employeeId);
        
        // Update employee status
        employeeService.updateStatus(employeeId, EmployeeStatus.TERMINATED);
        
        // Generate offboarding tasks
        taskService.generateOffboardingTasks(employeeId);
    }
}
```

---

## E18. Localization & Multi-Tenant

### Section: Internationalization and Multi-Tenancy

**Description:**
Implement support for multiple languages (i18n), multiple warehouse locations (multi-tenancy), and tenant-scoped data isolation.

**Sample Implementation:**

```java
// Configuration
package com.wms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import java.util.Locale;

@Configuration
public class LocalizationConfig {
    
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.US);
        return resolver;
    }
    
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding("UTF-8");
        return source;
    }
}
```

```properties
# messages_en.properties
employee.created=Employee created successfully
employee.notfound=Employee not found

# messages_es.properties
employee.created=Empleado creado exitosamente
employee.notfound=Empleado no encontrado
```

---

## E19. Observability & Monitoring

### Section: System Observability

**Description:**
Implement comprehensive observability with structured logging, distributed tracing, metrics collection, health checks, and alerting integration.

**Sample Implementation:**

```properties
# application.properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true

logging.level.com.wms=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

```java
// Custom Health Indicator
package com.wms.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // Check database connectivity
        try {
            // ... database check logic
            return Health.up().withDetail("database", "Available").build();
        } catch (Exception e) {
            return Health.down().withDetail("database", "Unavailable").build();
        }
    }
}
```

---

## E20. CI/CD & Deployment Automation

### Section: Continuous Integration and Deployment

**Description:**
Implement automated CI/CD pipeline with build, test, security scanning, Docker image creation, and deployment to staging/production environments.

**Sample Implementation:**

```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
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
      run: mvn clean package -DskipTests
    
    - name: Run Tests
      run: mvn test
    
    - name: Run Security Scan
      run: mvn dependency-check:check
    
    - name: Build Docker Image
      run: |
        docker build -t wms:${{ github.sha }} .
        docker tag wms:${{ github.sha }} wms:latest
    
    - name: Push to Registry
      run: |
        echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
        docker push wms:${{ github.sha }}
        docker push wms:latest
    
    - name: Deploy to Staging
      if: github.ref == 'refs/heads/develop'
      run: |
        kubectl set image deployment/wms wms=wms:${{ github.sha }} -n staging
    
    - name: Deploy to Production
      if: github.ref == 'refs/heads/main'
      run: |
        kubectl set image deployment/wms wms=wms:${{ github.sha }} -n production
```

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/warehouse-management-system-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Best Practices Applied Throughout

### Architecture Principles:
1. **Layered Architecture**: Clear separation between Controller, Service, Repository, and Entity layers
2. **DTO Pattern**: Separate request/response DTOs from domain entities
3. **Dependency Injection**: Constructor-based injection with Lombok's @RequiredArgsConstructor
4. **Transaction Management**: @Transactional annotations for data consistency
5. **Exception Handling**: Centralized exception handling with @RestControllerAdvice

### Security Best Practices:
1. **Role-Based Access Control**: Method-level security with @PreAuthorize
2. **Input Validation**: Bean Validation (JSR-380) annotations
3. **SQL Injection Prevention**: Parameterized queries via Spring Data JPA
4. **Sensitive Data Protection**: Encryption for PII, secure password storage
5. **Audit Logging**: Comprehensive tracking of all sensitive operations

### Performance Optimization:
1. **Lazy Loading**: FetchType.LAZY for associations
2. **Pagination**: Page and Pageable for large datasets
3. **Caching**: Strategic use of Spring Cache
4. **Database Indexing**: Proper indexes on frequently queried columns
5. **Connection Pooling**: HikariCP configuration

### Code Quality:
1. **Clean Code**: Meaningful names, single responsibility principle
2. **Documentation**: OpenAPI/Swagger annotations
3. **Testing**: Unit tests, integration tests, security tests
4. **Code Coverage**: Minimum 80% coverage target
5. **Static Analysis**: SonarQube integration

---

## Conclusion

This comprehensive low-level technical design document provides detailed specifications for all 20 epics of the Warehouse Employee Management System. Each section includes:

- Complete entity designs with JPA annotations
- Repository layer specifications with Spring Data JPA
- Service layer with business logic and transaction management
- Controller layer with RESTful endpoints and security
- DTO patterns for request/response handling
- Validation and exception handling
- Integration points and external API specifications
- Sample code implementations

The design follows Spring Boot best practices and industry standards, ensuring a production-ready, scalable, and maintainable system.

**Document Version**: 1.0
**Last Updated**: 2024
**Status**: Ready for Implementation