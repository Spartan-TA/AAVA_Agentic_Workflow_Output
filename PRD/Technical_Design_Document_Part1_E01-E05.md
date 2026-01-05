# WAREHOUSE EMPLOYEE MANAGEMENT SYSTEM â LOW-LEVEL TECHNICAL DESIGN DOCUMENT
## PART 1: EPICS E01-E05

## TABLE OF CONTENTS - PART 1
1. [Epic E01: Project Scaffolding & Domain Setup](#epic-e01)
2. [Epic E02: Employee Master Data (CRUD)](#epic-e02)
3. [Epic E03: Role-Based Access Control (RBAC)](#epic-e03)
4. [Epic E04: Time & Attendance (Clock In/Out)](#epic-e04)
5. [Epic E05: Shift & Schedule Management](#epic-e05)

---

## <a name="epic-e01"></a>EPIC E01: PROJECT SCAFFOLDING & DOMAIN SETUP

### Section: Spring Boot Architecture Overview

**Description:** Establishes the foundational structure for the application, ensuring modularity, maintainability, and scalability. Core modules (employee, scheduling, attendance, safety) are initialized. Flyway/Liquibase is used for DB migrations. Spring Boot Actuator is enabled for health monitoring.

**Design Specification:**
- Modular Maven project with multi-module support (if needed)
- Base packages: com.company.wms.[module]
- Core modules: employee, scheduling, attendance, safety
- Flyway/Liquibase for DB migrations
- Spring Boot Actuator enabled

**Sample Implementation:**
```xml
<!-- pom.xml (snippet) -->
<project>
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
</project>
```

### Section: Package Structure

**Description:** Follows standard Spring Boot conventions for separation of concerns.

**Design Specification:**
```
com.company.wms
âââ config
â   âââ DatabaseConfig.java
â   âââ SecurityConfig.java
â   âââ ActuatorConfig.java
âââ employee
â   âââ controller
â   â   âââ EmployeeController.java
â   âââ service
â   â   âââ EmployeeService.java
â   â   âââ EmployeeServiceImpl.java
â   âââ repository
â   â   âââ EmployeeRepository.java
â   âââ domain
â   â   âââ Employee.java
â   âââ dto
â       âââ EmployeeDTO.java
â       âââ EmployeeMapper.java
âââ scheduling
â   âââ controller
â   âââ service
â   âââ repository
â   âââ domain
â   âââ dto
âââ attendance
â   âââ controller
â   âââ service
â   âââ repository
â   âââ domain
â   âââ dto
âââ safety
â   âââ controller
â   âââ service
â   âââ repository
â   âââ domain
â   âââ dto
âââ common
    âââ exception
    âââ util
    âââ constants
```

### Section: Configuration

**Description:** Application properties, DB migration, and actuator setup.

**Design Specification:**
- application.properties: server.port=8080, spring.datasource.*, flyway.*
- Flyway/Liquibase migration scripts in src/main/resources/db/migration
- Actuator endpoints enabled

**Sample Implementation:**
```properties
# application.properties
server.port=8080
spring.application.name=warehouse-employee-management

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/wms
spring.datasource.username=wms_user
spring.datasource.password=${DB_PASSWORD}
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

# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.health.db.enabled=true
```

**Flyway Migration Example:**
```sql
-- V1__create_employees_table.sql
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    badge_id VARCHAR(50) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    department_id BIGINT,
    shift_group VARCHAR(50),
    hire_date DATE,
    status VARCHAR(50) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_employees_badge_id ON employees(badge_id);
CREATE INDEX idx_employees_deleted ON employees(deleted);
```

---

## <a name="epic-e02"></a>EPIC E02: EMPLOYEE MASTER DATA (CRUD)

### Section: Spring Boot Architecture Overview

**Description:** Implements the Employee domain with full CRUD REST APIs, supporting filtering, pagination, and soft-delete.

**Design Specification:**
- Employee entity with fields: id, name, badgeId (unique), role, department, shiftGroup, hireDate, status, deleted
- REST endpoints for CRUD
- Soft-delete via 'deleted' flag
- Pagination and filtering via Spring Data

**Sample Implementation:**
```java
package com.company.wms.employee.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees", 
       uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "badge_id", nullable = false, unique = true, length = 50)
    private String badgeId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    
    @Column(name = "shift_group", length = 50)
    private String shiftGroup;
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    
    @Column(nullable = false)
    private Boolean deleted = false;
    
    @Column(name = "created_at", updatable = false)
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
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    
    public String getShiftGroup() { return shiftGroup; }
    public void setShiftGroup(String shiftGroup) { this.shiftGroup = shiftGroup; }
    
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
}

public enum Role {
    ADMIN,
    HR,
    SUPERVISOR,
    WORKER
}

public enum Status {
    ACTIVE,
    INACTIVE,
    ON_LEAVE,
    TERMINATED
}
```

### Section: Repository Layer

**Description:** Spring Data JPA repository with custom queries for filtering and soft-delete.

**Design Specification:**
- EmployeeRepository extends JpaRepository<Employee, Long>
- Custom methods: findByDeletedFalse, findByBadgeIdAndDeletedFalse, etc.

**Sample Implementation:**
```java
package com.company.wms.employee.repository;

import com.company.wms.employee.domain.Employee;
import com.company.wms.employee.domain.Status;
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
    
    @Query("SELECT e FROM Employee e WHERE e.deleted = false " +
           "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Employee> findByFilters(
        @Param("departmentId") Long departmentId,
        @Param("status") Status status,
        @Param("name") String name,
        Pageable pageable
    );
    
    boolean existsByBadgeIdAndDeletedFalse(String badgeId);
}
```

### Section: Service Layer

**Description:** Handles business logic, validation, and transaction management.

**Design Specification:**
- EmployeeService with methods: create, update, delete (soft), find, filter
- Validates unique badgeId, handles soft-delete

**Sample Implementation:**
```java
package com.company.wms.employee.service;

import com.company.wms.employee.domain.Employee;
import com.company.wms.employee.domain.Status;
import com.company.wms.employee.dto.EmployeeDTO;
import com.company.wms.employee.repository.EmployeeRepository;
import com.company.wms.common.exception.DuplicateBadgeIdException;
import com.company.wms.common.exception.EmployeeNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Override
    public EmployeeDTO create(EmployeeDTO dto) {
        // Validate unique badge ID
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(dto.getBadgeId())) {
            throw new DuplicateBadgeIdException(
                "Badge ID already exists: " + dto.getBadgeId()
            );
        }
        
        Employee employee = new Employee();
        employee.setName(dto.getName());
        employee.setBadgeId(dto.getBadgeId());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(Status.ACTIVE);
        employee.setDeleted(false);
        
        Employee saved = employeeRepository.save(employee);
        return mapToDTO(saved);
    }
    
    @Override
    public EmployeeDTO update(Long id, EmployeeDTO dto) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "Employee not found with id: " + id
            ));
        
        // Check if badge ID is being changed and if new badge ID already exists
        if (!employee.getBadgeId().equals(dto.getBadgeId()) &&
            employeeRepository.existsByBadgeIdAndDeletedFalse(dto.getBadgeId())) {
            throw new DuplicateBadgeIdException(
                "Badge ID already exists: " + dto.getBadgeId()
            );
        }
        
        employee.setName(dto.getName());
        employee.setBadgeId(dto.getBadgeId());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setShiftGroup(dto.getShiftGroup());
        employee.setStatus(dto.getStatus());
        
        Employee updated = employeeRepository.save(employee);
        return mapToDTO(updated);
    }
    
    @Override
    public void softDelete(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "Employee not found with id: " + id
            ));
        
        employee.setDeleted(true);
        employee.setStatus(Status.INACTIVE);
        employeeRepository.save(employee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public EmployeeDTO findById(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "Employee not found with id: " + id
            ));
        return mapToDTO(employee);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> findAll(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable)
            .map(this::mapToDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> findByFilters(
            Long departmentId, 
            Status status, 
            String name, 
            Pageable pageable) {
        return employeeRepository.findByFilters(departmentId, status, name, pageable)
            .map(this::mapToDTO);
    }
    
    private EmployeeDTO mapToDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setName(employee.getName());
        dto.setBadgeId(employee.getBadgeId());
        dto.setRole(employee.getRole());
        dto.setDepartment(employee.getDepartment());
        dto.setShiftGroup(employee.getShiftGroup());
        dto.setHireDate(employee.getHireDate());
        dto.setStatus(employee.getStatus());
        return dto;
    }
}
```

### Section: Controller Layer

**Description:** Exposes REST endpoints with DTOs, validation, and error handling.

**Design Specification:**
- /employees [GET, POST, PUT, PATCH, DELETE]
- Uses @Valid for input validation
- Handles exceptions with @ControllerAdvice

**Sample Implementation:**
```java
package com.company.wms.employee.controller;

import com.company.wms.employee.domain.Status;
import com.company.wms.employee.dto.EmployeeDTO;
import com.company.wms.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@Validated
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<EmployeeDTO> createEmployee(
            @Valid @RequestBody EmployeeDTO employeeDTO) {
        EmployeeDTO created = employeeService.create(employeeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public ResponseEntity<Page<EmployeeDTO>> getAllEmployees(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) String name,
            Pageable pageable) {
        Page<EmployeeDTO> employees = employeeService.findByFilters(
            departmentId, status, name, pageable
        );
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        EmployeeDTO employee = employeeService.findById(id);
        return ResponseEntity.ok(employee);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDTO employeeDTO) {
        EmployeeDTO updated = employeeService.update(id, employeeDTO);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Section: DTO and Validation

**Description:** Data Transfer Objects with validation annotations.

**Sample Implementation:**
```java
package com.company.wms.employee.dto;

import com.company.wms.employee.domain.Department;
import com.company.wms.employee.domain.Role;
import com.company.wms.employee.domain.Status;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class EmployeeDTO {
    
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;
    
    @NotBlank(message = "Badge ID is required")
    @Pattern(regexp = "^[A-Z0-9]{6,10}$", 
             message = "Badge ID must be 6-10 alphanumeric characters")
    private String badgeId;
    
    @NotNull(message = "Role is required")
    private Role role;
    
    private Department department;
    
    private String shiftGroup;
    
    @Past(message = "Hire date must be in the past")
    private LocalDate hireDate;
    
    private Status status;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    
    public String getShiftGroup() { return shiftGroup; }
    public void setShiftGroup(String shiftGroup) { this.shiftGroup = shiftGroup; }
    
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
```

---

## <a name="epic-e03"></a>EPIC E03: ROLE-BASED ACCESS CONTROL (RBAC)

### Section: Spring Boot Architecture Overview

**Description:** Secures endpoints and methods using Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER). Supports API key/OAuth2 toggle.

**Design Specification:**
- SecurityConfig with role mappings
- Method-level security with @PreAuthorize
- Row-level security for SUPERVISOR

**Sample Implementation:**
```java
package com.company.wms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/api/v1/employees/**")
                    .hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/api/v1/attendance/**")
                    .hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
                .requestMatchers("/api/v1/shifts/**")
                    .hasAnyRole("ADMIN", "SUPERVISOR")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder()))
            );
        
        return http.build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation("https://issuer.example.com");
    }
}
```

### Section: Method-Level Security

**Description:** Fine-grained access control using @PreAuthorize annotations.

**Sample Implementation:**
```java
package com.company.wms.employee.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class EmployeeSecurityService {
    
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('SUPERVISOR') and #employee.department.id == authentication.principal.departmentId)")
    public Employee updateEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public void deleteEmployee(Long employeeId) {
        employeeRepository.deleteById(employeeId);
    }
    
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('SUPERVISOR') and @employeeSecurityService.isInSameDepartment(#employeeId))")
    public EmployeeDTO viewEmployee(Long employeeId) {
        return employeeService.findById(employeeId);
    }
    
    public boolean isInSameDepartment(Long employeeId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserDeptId = getCurrentUserDepartmentId(auth);
        Long targetEmployeeDeptId = getEmployeeDepartmentId(employeeId);
        return currentUserDeptId.equals(targetEmployeeDeptId);
    }
}
```

---

## <a name="epic-e04"></a>EPIC E04: TIME & ATTENDANCE (CLOCK IN/OUT)

### Section: Spring Boot Architecture Overview

**Description:** Manages clock-in/out events, geofence/device capture, hours calculation, missed punch correction workflow.

**Design Specification:**
- AttendanceEvent entity: id, employee, timestamp, type (IN/OUT), deviceId, location, approved, correctionRequested
- REST endpoints for clock-in/out
- Correction workflow with approval

**Sample Implementation:**
```java
package com.company.wms.attendance.domain;

import com.company.wms.employee.domain.Employee;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_events")
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
    private EventType type;
    
    @Column(name = "device_id")
    private String deviceId;
    
    @Column(columnDefinition = "TEXT")
    private String location; // Geofence coordinates (JSON)
    
    @Column(nullable = false)
    private Boolean approved = true;
    
    @Column(name = "correction_requested")
    private Boolean correctionRequested = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private Shift shift;
    
    @Column(name = "hours_worked")
    private Double hoursWorked;
    
    @Column(name = "correction_reason")
    private String correctionReason;
    
    // Getters and setters
}

public enum EventType {
    CLOCK_IN,
    CLOCK_OUT
}
```

### Section: Service Layer - Attendance Management

**Sample Implementation:**
```java
package com.company.wms.attendance.service;

import com.company.wms.attendance.domain.AttendanceEvent;
import com.company.wms.attendance.domain.EventType;
import com.company.wms.attendance.repository.AttendanceEventRepository;
import com.company.wms.common.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AttendanceService {
    
    @Autowired
    private AttendanceEventRepository attendanceRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private ShiftRepository shiftRepository;
    
    public AttendanceEvent clockIn(Long employeeId, String deviceId, String location) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "Employee not found with id: " + employeeId
            ));
        
        // Validate no existing clock-in without clock-out
        List<AttendanceEvent> todayEvents = attendanceRepository
            .findByEmployeeAndDate(employeeId, LocalDate.now());
        
        if (!todayEvents.isEmpty()) {
            AttendanceEvent lastEvent = todayEvents.get(todayEvents.size() - 1);
            if (lastEvent.getType() == EventType.CLOCK_IN) {
                throw new InvalidClockInException(
                    "Already clocked in. Please clock out first."
                );
            }
        }
        
        // Validate geofence if location provided
        if (location != null && !isWithinGeofence(location)) {
            throw new GeofenceViolationException(
                "Clock-in location is outside allowed geofence"
            );
        }
        
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployee(employee);
        event.setTimestamp(LocalDateTime.now());
        event.setType(EventType.CLOCK_IN);
        event.setDeviceId(deviceId);
        event.setLocation(location);
        event.setApproved(true);
        
        // Associate with current shift
        Shift currentShift = shiftRepository
            .findCurrentShiftForEmployee(employeeId, LocalDateTime.now());
        event.setShift(currentShift);
        
        return attendanceRepository.save(event);
    }
    
    public AttendanceEvent clockOut(Long employeeId, String deviceId, String location) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "Employee not found with id: " + employeeId
            ));
        
        List<AttendanceEvent> todayEvents = attendanceRepository
            .findByEmployeeAndDate(employeeId, LocalDate.now());
        
        if (todayEvents.isEmpty() || 
            todayEvents.get(todayEvents.size() - 1).getType() == EventType.CLOCK_OUT) {
            throw new InvalidClockOutException(
                "No active clock-in found. Please clock in first."
            );
        }
        
        AttendanceEvent clockInEvent = todayEvents.get(todayEvents.size() - 1);
        
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployee(employee);
        event.setTimestamp(LocalDateTime.now());
        event.setType(EventType.CLOCK_OUT);
        event.setDeviceId(deviceId);
        event.setLocation(location);
        event.setShift(clockInEvent.getShift());
        event.setApproved(true);
        
        // Calculate hours worked
        Duration duration = Duration.between(
            clockInEvent.getTimestamp(), 
            event.getTimestamp()
        );
        event.setHoursWorked(duration.toMinutes() / 60.0);
        
        return attendanceRepository.save(event);
    }
    
    public void requestCorrection(Long eventId, String reason) {
        AttendanceEvent event = attendanceRepository.findById(eventId)
            .orElseThrow(() -> new AttendanceEventNotFoundException(
                "Attendance event not found with id: " + eventId
            ));
        
        event.setCorrectionRequested(true);
        event.setApproved(false);
        event.setCorrectionReason(reason);
        attendanceRepository.save(event);
        
        // Notify supervisor for approval
        // notificationService.notifySupervisor(event.getEmployee().getSupervisor(), reason);
    }
    
    @PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
    public void approveCorrection(Long eventId) {
        AttendanceEvent event = attendanceRepository.findById(eventId)
            .orElseThrow(() -> new AttendanceEventNotFoundException(
                "Attendance event not found with id: " + eventId
            ));
        
        event.setApproved(true);
        event.setCorrectionRequested(false);
        attendanceRepository.save(event);
    }
    
    private boolean isWithinGeofence(String location) {
        // Implement geofence validation logic
        return true;
    }
}
```

---

## <a name="epic-e05"></a>EPIC E05: SHIFT & SCHEDULE MANAGEMENT

### Section: Spring Boot Architecture Overview

**Description:** Manages shift templates, rotations, overtime, blackout dates, and employee assignments.

**Design Specification:**
- ShiftTemplate, ShiftAssignment entities
- CRUD endpoints for templates and assignments
- Conflict detection logic

**Sample Implementation:**
```java
package com.company.wms.scheduling.domain;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "shift_templates")
public class ShiftTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(nullable = false)
    private Boolean recurring = false;
    
    @Column(name = "recurrence_pattern")
    private String recurrencePattern; // e.g., "WEEKLY", "DAILY"
    
    @Column(name = "overtime_eligible")
    private Boolean overtimeEligible = false;
    
    @Column(name = "break_duration_minutes")
    private Integer breakDurationMinutes;
    
    // Getters and setters
}

@Entity
@Table(name = "shift_assignments")
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
    private LocalDate date;
    
    @Enumerated(EnumType.STRING)
    private AssignmentStatus status; // SCHEDULED, COMPLETED, CANCELLED
    
    // Getters and setters
}

public enum AssignmentStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
```

### Section: Service Layer - Shift Management

**Sample Implementation:**
```java
package com.company.wms.scheduling.service;

import com.company.wms.scheduling.domain.*;
import com.company.wms.scheduling.repository.*;
import com.company.wms.common.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ShiftService {
    
    @Autowired
    private ShiftAssignmentRepository assignmentRepository;
    
    @Autowired
    private ShiftTemplateRepository templateRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    public ShiftAssignment assignShift(
            Long employeeId, 
            Long shiftTemplateId, 
            LocalDate date) {
        
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new EmployeeNotFoundException(
                "Employee not found with id: " + employeeId
            ));
        
        ShiftTemplate template = templateRepository.findById(shiftTemplateId)
            .orElseThrow(() -> new ShiftTemplateNotFoundException(
                "Shift template not found with id: " + shiftTemplateId
            ));
        
        // Check for conflicts
        if (detectConflicts(employeeId, date, template.getStartTime(), template.getEndTime())) {
            throw new ShiftConflictException(
                "Shift conflicts with existing assignment for employee on " + date
            );
        }
        
        // Check for blackout dates
        if (isBlackoutDate(date)) {
            throw new BlackoutDateException(
                "Cannot assign shift on blackout date: " + date
            );
        }
        
        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setEmployee(employee);
        assignment.setShiftTemplate(template);
        assignment.setDate(date);
        assignment.setStatus(AssignmentStatus.SCHEDULED);
        
        return assignmentRepository.save(assignment);
    }
    
    public boolean detectConflicts(
            Long employeeId, 
            LocalDate date, 
            LocalTime startTime, 
            LocalTime endTime) {
        
        List<ShiftAssignment> existingAssignments = assignmentRepository
            .findByEmployeeIdAndDate(employeeId, date);
        
        for (ShiftAssignment existing : existingAssignments) {
            LocalTime existingStart = existing.getShiftTemplate().getStartTime();
            LocalTime existingEnd = existing.getShiftTemplate().getEndTime();
            
            // Check for time overlap
            if (!(endTime.isBefore(existingStart) || startTime.isAfter(existingEnd))) {
                return true;
            }
        }
        
        return false;
    }
    
    public List<ShiftAssignment> bulkAssign(
            List<Long> employeeIds, 
            Long shiftTemplateId, 
            LocalDate startDate, 
            LocalDate endDate) {
        
        List<ShiftAssignment> assignments = new ArrayList<>();
        
        for (Long employeeId : employeeIds) {
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                try {
                    ShiftAssignment assignment = assignShift(
                        employeeId, 
                        shiftTemplateId, 
                        currentDate
                    );
                    assignments.add(assignment);
                } catch (ShiftConflictException | BlackoutDateException e) {
                    // Log and continue
                }
                currentDate = currentDate.plusDays(1);
            }
        }
        
        return assignments;
    }
    
    private boolean isBlackoutDate(LocalDate date) {
        // Check against blackout dates calendar
        return false;
    }
}
```

---

**Document Version:** 1.0 - Part 1
**Covers:** Epics E01-E05
**Next Part:** Technical_Design_Document_Part2_E06-E11.md