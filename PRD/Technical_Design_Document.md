# WAREHOUSE EMPLOYEE MANAGEMENT SYSTEM - LOW-LEVEL TECHNICAL DESIGN DOCUMENT

## DOCUMENT OVERVIEW

This document provides comprehensive low-level technical design specifications for all 20 epics of the Warehouse Employee Management System. Each epic includes Spring Boot architecture, package structure, entity design, service/repository/controller specifications, configuration, security settings, integration points, and code samples.

---

## E01 - PROJECT SCAFFOLDING & DOMAIN SETUP

### Section: Spring Boot Architecture Overview

**Description**: Establishes the foundational architecture for the warehouse employee management system using Spring Boot 3.x with Maven, Flyway for database migrations, and Spring Boot Actuator for monitoring and health checks.

**Design Specification**:
- Spring Boot 3.x with Java 17+
- Maven multi-module project structure
- Base package: `com.company.warehouse`
- Submodules: employee, shift, attendance, safety, asset, review, payroll, notification, integration
- Flyway for versioned database migrations
- Spring Boot Actuator for health endpoints and metrics
- PostgreSQL as primary database

**Sample Implementation**:
```java
// pom.xml (parent)
<project>
    <groupId>com.company.warehouse</groupId>
    <artifactId>warehouse-management-parent</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    
    <modules>
        <module>warehouse-core</module>
        <module>warehouse-api</module>
        <module>warehouse-integration</module>
        <module>warehouse-web</module>
    </modules>
    
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

// Application.java
package com.company.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WarehouseManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseManagementApplication.class, args);
    }
}

// application.yml
spring:
  application:
    name: warehouse-management-system
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

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized

server:
  port: 8080
```

### Section: Package Structure

**Description**: Organized package structure following domain-driven design principles with clear separation of concerns.

**Design Specification**:
```
com.company.warehouse/
âââ employee/
â   âââ domain/
â   âââ repository/
â   âââ service/
â   âââ controller/
â   âââ dto/
âââ shift/
â   âââ domain/
â   âââ repository/
â   âââ service/
â   âââ controller/
â   âââ dto/
âââ attendance/
â   âââ domain/
â   âââ repository/
â   âââ service/
â   âââ controller/
â   âââ dto/
âââ safety/
â   âââ domain/
â   âââ repository/
â   âââ service/
â   âââ controller/
â   âââ dto/
âââ common/
â   âââ config/
â   âââ exception/
â   âââ security/
â   âââ util/
âââ integration/
    âââ hris/
    âââ wms/
    âââ payroll/
```

---

## E02 - EMPLOYEE MASTER DATA (CRUD)

### Section: Domain Model Design

**Description**: Core employee entity with comprehensive fields for warehouse employee management, supporting unique badge identification, soft-delete, and audit trails.

**Design Specification**:
- Entity: Employee
- Fields: id, badgeId (unique), firstName, lastName, email, phone, role, department, shiftGroup, hireDate, status, deleted, createdAt, updatedAt, createdBy, updatedBy
- JPA annotations: @Entity, @Table, @UniqueConstraint, @Where, @SQLDelete
- Soft-delete implementation
- Audit fields with @CreatedDate, @LastModifiedDate

**Sample Implementation**:
```java
package com.company.warehouse.employee.domain;

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

@Entity
@Table(name = "employees", 
       uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE employees SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "badge_id", unique = true, nullable = false, length = 32)
    private String badgeId;
    
    @Column(name = "first_name", nullable = false, length = 64)
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 64)
    private String lastName;
    
    @Column(name = "email", nullable = false, length = 128)
    private String email;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private EmployeeRole role;
    
    @Column(name = "department", length = 64)
    private String department;
    
    @Column(name = "shift_group", length = 32)
    private String shiftGroup;
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EmployeeStatus status;
    
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;
    
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;
}

public enum EmployeeRole {
    ADMIN, HR, SUPERVISOR, WORKER
}

public enum EmployeeStatus {
    ACTIVE, INACTIVE, ON_LEAVE, TERMINATED
}
```

### Section: Repository Layer

**Description**: Spring Data JPA repository with custom query methods for filtering and searching employees.

**Design Specification**:
- Interface: EmployeeRepository extends JpaRepository
- Custom queries: findByBadgeId, findByDepartment, findByStatus, searchEmployees
- Pagination and sorting support
- Specification API for dynamic filtering

**Sample Implementation**:
```java
package com.company.warehouse.employee.repository;

import com.company.warehouse.employee.domain.Employee;
import com.company.warehouse.employee.domain.EmployeeStatus;
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
    
    Optional<Employee> findByBadgeId(String badgeId);
    
    Page<Employee> findByDepartment(String department, Pageable pageable);
    
    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);
    
    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.badgeId) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Employee> searchEmployees(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    boolean existsByBadgeId(String badgeId);
}
```

### Section: Service Layer

**Description**: Business logic layer handling employee CRUD operations, validation, and business rules.

**Design Specification**:
- Service: EmployeeService
- Methods: createEmployee, updateEmployee, getEmployee, getAllEmployees, deleteEmployee (soft), validateBadgeId
- Transaction management with @Transactional
- Exception handling for duplicate badgeId, not found scenarios

**Sample Implementation**:
```java
package com.company.warehouse.employee.service;

import com.company.warehouse.employee.domain.Employee;
import com.company.warehouse.employee.dto.EmployeeCreateDto;
import com.company.warehouse.employee.dto.EmployeeDto;
import com.company.warehouse.employee.dto.EmployeeUpdateDto;
import com.company.warehouse.employee.repository.EmployeeRepository;
import com.company.warehouse.common.exception.DuplicateResourceException;
import com.company.warehouse.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    
    @Transactional
    public EmployeeDto createEmployee(EmployeeCreateDto createDto) {
        if (employeeRepository.existsByBadgeId(createDto.getBadgeId())) {
            throw new DuplicateResourceException(
                "Employee with badgeId " + createDto.getBadgeId() + " already exists");
        }
        
        Employee employee = employeeMapper.toEntity(createDto);
        employee.setDeleted(false);
        Employee savedEmployee = employeeRepository.save(employee);
        return employeeMapper.toDto(savedEmployee);
    }
    
    @Transactional
    public EmployeeDto updateEmployee(Long id, EmployeeUpdateDto updateDto) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        
        employeeMapper.updateEntity(updateDto, employee);
        Employee updatedEmployee = employeeRepository.save(employee);
        return employeeMapper.toDto(updatedEmployee);
    }
    
    public EmployeeDto getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return employeeMapper.toDto(employee);
    }
    
    public Page<EmployeeDto> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable)
            .map(employeeMapper::toDto);
    }
    
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employeeRepository.delete(employee); // Soft delete via @SQLDelete
    }
}
```

### Section: Controller Layer

**Description**: REST API endpoints for employee management with validation, pagination, and OpenAPI documentation.

**Design Specification**:
- Controller: EmployeeController
- Endpoints: POST /api/employees, GET /api/employees, GET /api/employees/{id}, PUT /api/employees/{id}, DELETE /api/employees/{id}
- Request/Response DTOs with validation annotations
- OpenAPI annotations for documentation
- Pagination and filtering support

**Sample Implementation**:
```java
package com.company.warehouse.employee.controller;

import com.company.warehouse.employee.dto.EmployeeCreateDto;
import com.company.warehouse.employee.dto.EmployeeDto;
import com.company.warehouse.employee.dto.EmployeeUpdateDto;
import com.company.warehouse.employee.service.EmployeeService;
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
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "APIs for managing warehouse employees")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Create new employee", description = "Creates a new employee record")
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeCreateDto createDto) {
        EmployeeDto created = employeeService.createEmployee(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get all employees", description = "Retrieves paginated list of employees")
    public ResponseEntity<Page<EmployeeDto>> getAllEmployees(Pageable pageable) {
        Page<EmployeeDto> employees = employeeService.getAllEmployees(pageable);
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @Operation(summary = "Get employee by ID", description = "Retrieves employee details by ID")
    public ResponseEntity<EmployeeDto> getEmployee(@PathVariable Long id) {
        EmployeeDto employee = employeeService.getEmployee(id);
        return ResponseEntity.ok(employee);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update employee", description = "Updates employee details")
    public ResponseEntity<EmployeeDto> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateDto updateDto) {
        EmployeeDto updated = employeeService.updateEmployee(id, updateDto);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete employee", description = "Soft deletes an employee")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Section: DTOs and Validation

**Description**: Data Transfer Objects with validation constraints for API requests and responses.

**Design Specification**:
- DTOs: EmployeeCreateDto, EmployeeUpdateDto, EmployeeDto
- Validation: @NotNull, @NotBlank, @Email, @Pattern, @Size
- MapStruct for entity-DTO mapping

**Sample Implementation**:
```java
package com.company.warehouse.employee.dto;

import com.company.warehouse.employee.domain.EmployeeRole;
import com.company.warehouse.employee.domain.EmployeeStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateDto {
    
    @NotBlank(message = "Badge ID is required")
    @Size(max = 32, message = "Badge ID must not exceed 32 characters")
    private String badgeId;
    
    @NotBlank(message = "First name is required")
    @Size(max = 64, message = "First name must not exceed 64 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 64, message = "Last name must not exceed 64 characters")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @Pattern(regexp = "^\+?[1-9]\d{1,14}$", message = "Phone number must be valid")
    private String phone;
    
    @NotNull(message = "Role is required")
    private EmployeeRole role;
    
    private String department;
    
    private String shiftGroup;
    
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
    
    @NotNull(message = "Status is required")
    private EmployeeStatus status;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    private Long id;
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
```

---

## E03 - ROLE-BASED ACCESS CONTROL (RBAC)

### Section: Security Configuration

**Description**: Spring Security configuration with role-based access control, JWT/OAuth2 authentication, and method-level security.

**Design Specification**:
- SecurityConfig with HttpSecurity configuration
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- JWT token authentication
- OAuth2 resource server configuration
- Method security with @PreAuthorize
- CORS configuration

**Sample Implementation**:
```java
package com.company.warehouse.common.security;

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
    private final JwtAuthenticationEntryPoint jwtAuthEntryPoint;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/actuator/health", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/api/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
                .requestMatchers("/api/shifts/**").hasAnyRole("ADMIN", "SUPERVISOR")
                .requestMatchers("/api/leaves/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR", "WORKER")
                .requestMatchers("/api/certifications/**").hasAnyRole("ADMIN", "SUPERVISOR")
                .requestMatchers("/api/incidents/**").hasAnyRole("ADMIN", "SUPERVISOR")
                .requestMatchers("/api/equipment/**").hasAnyRole("ADMIN", "SUPERVISOR")
                .requestMatchers("/api/reviews/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .requestMatchers("/api/payroll/**").hasRole("ADMIN")
                .requestMatchers("/api/audit/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthEntryPoint))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://warehouse.company.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### Section: JWT Authentication

**Description**: JWT token generation, validation, and authentication filter implementation.

**Design Specification**:
- JwtService for token generation and validation
- JwtAuthenticationFilter for request interception
- UserDetailsService implementation
- Token expiration and refresh logic

**Sample Implementation**:
```java
package com.company.warehouse.common.security;

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
import java.util.function.Function;

@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }
    
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
            .setClaims(extraClaims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
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
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

---

## E04 - TIME & ATTENDANCE (CLOCK IN/OUT)

### Section: Domain Model Design

**Description**: Attendance tracking entity with clock-in/out events, geofence validation, device capture, and hours calculation.

**Design Specification**:
- Entity: Attendance
- Fields: id, employeeId, clockIn, clockOut, deviceId, location (latitude, longitude), status, hoursWorked, notes
- Relationships: ManyToOne with Employee
- Validation: clockOut must be after clockIn
- Automatic hours calculation

**Sample Implementation**:
```java
package com.company.warehouse.attendance.domain;

import com.company.warehouse.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    
    @Column(name = "device_id", length = 64)
    private String deviceId;
    
    @Column(name = "latitude")
    private BigDecimal latitude;
    
    @Column(name = "longitude")
    private BigDecimal longitude;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;
    
    @Column(name = "hours_worked")
    private BigDecimal hoursWorked;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PreUpdate
    @PrePersist
    public void calculateHours() {
        if (clockIn != null && clockOut != null) {
            Duration duration = Duration.between(clockIn, clockOut);
            this.hoursWorked = BigDecimal.valueOf(duration.toMinutes() / 60.0)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }
}

public enum AttendanceStatus {
    CLOCKED_IN, CLOCKED_OUT, MISSED_PUNCH, CORRECTED
}
```

### Section: Service Layer

**Description**: Business logic for clock-in/out operations, geofence validation, missed punch handling, and hours calculation.

**Design Specification**:
- Service: AttendanceService
- Methods: clockIn, clockOut, getMissedPunches, correctPunch, calculateDailyHours
- Geofence validation logic
- Duplicate clock-in prevention
- Missed punch detection

**Sample Implementation**:
```java
package com.company.warehouse.attendance.service;

import com.company.warehouse.attendance.domain.Attendance;
import com.company.warehouse.attendance.domain.AttendanceStatus;
import com.company.warehouse.attendance.dto.ClockInDto;
import com.company.warehouse.attendance.dto.ClockOutDto;
import com.company.warehouse.attendance.dto.AttendanceDto;
import com.company.warehouse.attendance.repository.AttendanceRepository;
import com.company.warehouse.employee.repository.EmployeeRepository;
import com.company.warehouse.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {
    
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final GeofenceService geofenceService;
    private final AttendanceMapper attendanceMapper;
    
    @Transactional
    public AttendanceDto clockIn(ClockInDto clockInDto) {
        // Validate employee exists
        var employee = employeeRepository.findById(clockInDto.getEmployeeId())
            .orElseThrow(() -> new BusinessException("Employee not found"));
        
        // Check for existing active clock-in
        var existingClockIn = attendanceRepository
            .findActiveClockInByEmployee(clockInDto.getEmployeeId());
        if (existingClockIn.isPresent()) {
            throw new BusinessException("Employee already clocked in");
        }
        
        // Validate geofence if coordinates provided
        if (clockInDto.getLatitude() != null && clockInDto.getLongitude() != null) {
            if (!geofenceService.isWithinWarehouseBoundary(
                    clockInDto.getLatitude(), clockInDto.getLongitude())) {
                throw new BusinessException("Clock-in location outside warehouse boundary");
            }
        }
        
        // Create attendance record
        Attendance attendance = Attendance.builder()
            .employee(employee)
            .clockIn(LocalDateTime.now())
            .deviceId(clockInDto.getDeviceId())
            .latitude(clockInDto.getLatitude())
            .longitude(clockInDto.getLongitude())
            .status(AttendanceStatus.CLOCKED_IN)
            .build();
        
        Attendance saved = attendanceRepository.save(attendance);
        return attendanceMapper.toDto(saved);
    }
    
    @Transactional
    public AttendanceDto clockOut(ClockOutDto clockOutDto) {
        // Find active clock-in
        var attendance = attendanceRepository
            .findActiveClockInByEmployee(clockOutDto.getEmployeeId())
            .orElseThrow(() -> new BusinessException("No active clock-in found"));
        
        // Validate geofence if coordinates provided
        if (clockOutDto.getLatitude() != null && clockOutDto.getLongitude() != null) {
            if (!geofenceService.isWithinWarehouseBoundary(
                    clockOutDto.getLatitude(), clockOutDto.getLongitude())) {
                throw new BusinessException("Clock-out location outside warehouse boundary");
            }
        }
        
        // Update attendance record
        attendance.setClockOut(LocalDateTime.now());
        attendance.setStatus(AttendanceStatus.CLOCKED_OUT);
        attendance.calculateHours();
        
        Attendance updated = attendanceRepository.save(attendance);
        return attendanceMapper.toDto(updated);
    }
    
    public List<AttendanceDto> getDailyAttendance(Long employeeId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        
        return attendanceRepository
            .findByEmployeeIdAndClockInBetween(employeeId, startOfDay, endOfDay)
            .stream()
            .map(attendanceMapper::toDto)
            .toList();
    }
    
    public BigDecimal calculateDailyHours(Long employeeId, LocalDate date) {
        List<Attendance> records = attendanceRepository
            .findByEmployeeIdAndDate(employeeId, date);
        
        return records.stream()
            .map(Attendance::getHoursWorked)
            .filter(hours -> hours != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

---

## E05 - SHIFT & SCHEDULE MANAGEMENT

### Section: Domain Model Design

**Description**: Shift templates, schedules, and assignments with support for rotations, overtime rules, and warehouse calendars.

**Design Specification**:
- Entities: ShiftTemplate, ShiftSchedule, ShiftAssignment, OvertimeRule, WarehouseCalendar
- Relationships: ManyToOne, OneToMany
- Validation: shift times, conflict detection
- Recurring shift support

**Sample Implementation**:
```java
package com.company.warehouse.shift.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.Set;

@Entity
@Table(name = "shift_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 64)
    private String name;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @ElementCollection
    @CollectionTable(name = "shift_template_days", 
                     joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> daysOfWeek;
    
    @Column(name = "is_recurring")
    private Boolean isRecurring = false;
    
    @Column(name = "description", length = 255)
    private String description;
}

@Entity
@Table(name = "shift_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ShiftTemplate template;
    
    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShiftStatus status;
}

public enum ShiftStatus {
    SCHEDULED, COMPLETED, MISSED, CANCELLED
}
```

---

## E06 - LEAVE & ABSENCE MANAGEMENT

### Section: Domain Model Design

**Description**: Leave request management with accrual balances, approval workflows, and policy enforcement.

**Design Specification**:
- Entities: LeaveRequest, LeaveBalance, LeavePolicy
- Leave types: PTO, SICK, UNPAID
- Approval workflow states
- Balance calculation and validation

**Sample Implementation**:
```java
package com.company.warehouse.leave.domain;

import com.company.warehouse.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
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
    
    @Column(name = "days_requested", nullable = false)
    private Integer daysRequested;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LeaveStatus status;
    
    @Column(name = "reason", length = 500)
    private String reason;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Employee approvedBy;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
}

public enum LeaveType {
    PTO, SICK, UNPAID, BEREAVEMENT, JURY_DUTY
}

public enum LeaveStatus {
    PENDING, APPROVED, DENIED, CANCELLED
}
```

---

## E07 - TRAINING & CERTIFICATION TRACKING

### Section: Domain Model Design

**Description**: Certification tracking with expiration alerts, renewal workflows, and assignment validation.

**Design Specification**:
- Entities: Certification, EmployeeCertification, CertificationDocument
- Expiration tracking and alerts
- Proof document upload
- Assignment blocking logic

**Sample Implementation**:
```java
package com.company.warehouse.certification.domain;

import com.company.warehouse.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "certifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 128)
    private String name;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "validity_period_days")
    private Integer validityPeriodDays;
    
    @Column(name = "is_required_for_equipment")
    private Boolean isRequiredForEquipment = false;
}

@Entity
@Table(name = "employee_certifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeCertification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_id", nullable = false)
    private Certification certification;
    
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;
    
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CertificationStatus status;
    
    @Column(name = "document_url", length = 255)
    private String documentUrl;
}

public enum CertificationStatus {
    ACTIVE, EXPIRING_SOON, EXPIRED, RENEWED
}
```

---

## E08 - SAFETY INCIDENTS & OSHA REPORTING

### Section: Domain Model Design

**Description**: Safety incident tracking with investigation workflows and OSHA compliance reporting.

**Design Specification**:
- Entities: SafetyIncident, Investigation, OSHAReport
- Severity levels and incident types
- Investigation workflow states
- OSHA 300/300A export

**Sample Implementation**:
```java
package com.company.warehouse.safety.domain;

import com.company.warehouse.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "safety_incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SafetyIncident {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "incident_date", nullable = false)
    private LocalDateTime incidentDate;
    
    @Column(name = "location", nullable = false, length = 128)
    private String location;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private IncidentSeverity severity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private IncidentType type;
    
    @Column(name = "description", nullable = false, length = 2000)
    private String description;
    
    @ManyToMany
    @JoinTable(
        name = "incident_employees",
        joinColumns = @JoinColumn(name = "incident_id"),
        inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private Set<Employee> involvedEmployees;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private IncidentStatus status;
    
    @Column(name = "corrective_actions", length = 2000)
    private String correctiveActions;
}

public enum IncidentSeverity {
    MINOR, MODERATE, SERIOUS, CRITICAL, FATAL
}

public enum IncidentType {
    INJURY, NEAR_MISS, PROPERTY_DAMAGE, ENVIRONMENTAL
}

public enum IncidentStatus {
    OPEN, INVESTIGATING, RESOLVED, CLOSED
}
```

---

## E09 - EQUIPMENT & ASSET ASSIGNMENT

### Section: Domain Model Design

**Description**: Equipment and asset management with checkout/return tracking and certification validation.

**Design Specification**:
- Entities: Equipment, EquipmentAssignment, AssetCondition
- Checkout/return workflow
- Certification requirement validation
- Asset history tracking

**Sample Implementation**:
```java
package com.company.warehouse.equipment.domain;

import com.company.warehouse.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "asset_id", unique = true, nullable = false, length = 32)
    private String assetId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private EquipmentType type;
    
    @Column(name = "serial_number", length = 64)
    private String serialNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EquipmentStatus status;
    
    @Column(name = "requires_certification")
    private Boolean requiresCertification = false;
    
    @Column(name = "certification_id")
    private Long requiredCertificationId;
}

@Entity
@Table(name = "equipment_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Column(name = "checkout_time", nullable = false)
    private LocalDateTime checkoutTime;
    
    @Column(name = "return_time")
    private LocalDateTime returnTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_at_checkout")
    private AssetCondition conditionAtCheckout;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_at_return")
    private AssetCondition conditionAtReturn;
}

public enum EquipmentType {
    FORKLIFT, SCANNER, PPE, PALLET_JACK, RADIO
}

public enum EquipmentStatus {
    AVAILABLE, CHECKED_OUT, MAINTENANCE, RETIRED
}

public enum AssetCondition {
    EXCELLENT, GOOD, FAIR, POOR, DAMAGED
}
```

---

## E10 - PERFORMANCE REVIEWS & GOALS

### Section: Domain Model Design

**Description**: Performance review management with goal tracking, competency ratings, and acknowledgement workflows.

**Design Specification**:
- Entities: PerformanceReview, Goal, Competency, ReviewCycle
- Review templates and cycles
- Rating scales
- Acknowledgement workflow

**Sample Implementation**:
```java
package com.company.warehouse.review.domain;

import com.company.warehouse.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "performance_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private Employee reviewer;
    
    @Column(name = "review_period_start", nullable = false)
    private LocalDate reviewPeriodStart;
    
    @Column(name = "review_period_end", nullable = false)
    private LocalDate reviewPeriodEnd;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "overall_rating")
    private Rating overallRating;
    
    @Column(name = "comments", length = 2000)
    private String comments;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatus status;
    
    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;
    
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
    private Set<Goal> goals;
}

public enum Rating {
    EXCEEDS_EXPECTATIONS, MEETS_EXPECTATIONS, NEEDS_IMPROVEMENT, UNSATISFACTORY
}

public enum ReviewStatus {
    DRAFT, SUBMITTED, ACKNOWLEDGED, FINALIZED
}
```

---

## E11 - PAYROLL EXPORT INTEGRATION

### Section: Integration Design

**Description**: Payroll file generation and secure delivery to external payroll systems via SFTP or REST API.

**Design Specification**:
- Service: PayrollExportService
- File formats: CSV, JSON, XML
- Delivery methods: SFTP, REST API
- Reconciliation and audit logging

**Sample Implementation**:
```java
package com.company.warehouse.payroll.service;

import com.company.warehouse.attendance.repository.AttendanceRepository;
import com.company.warehouse.leave.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PayrollExportService {
    
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PayrollFileGenerator fileGenerator;
    private final SftpClient sftpClient;
    
    @Transactional
    public PayrollExportResult exportPayroll(LocalDate startDate, LocalDate endDate) {
        // Gather attendance data
        List<AttendanceRecord> attendanceRecords = 
            attendanceRepository.findApprovedRecordsBetween(startDate, endDate);
        
        // Gather leave data
        List<LeaveRecord> leaveRecords = 
            leaveRequestRepository.findApprovedLeavesBetween(startDate, endDate);
        
        // Generate payroll file
        File payrollFile = fileGenerator.generatePayrollFile(
            attendanceRecords, leaveRecords, startDate, endDate);
        
        // Deliver file via SFTP
        boolean delivered = sftpClient.uploadFile(payrollFile);
        
        // Create audit log
        PayrollExportLog log = PayrollExportLog.builder()
            .exportDate(LocalDateTime.now())
            .periodStart(startDate)
            .periodEnd(endDate)
            .fileName(payrollFile.getName())
            .recordCount(attendanceRecords.size() + leaveRecords.size())
            .status(delivered ? ExportStatus.SUCCESS : ExportStatus.FAILED)
            .build();
        
        return PayrollExportResult.builder()
            .success(delivered)
            .fileName(payrollFile.getName())
            .recordCount(log.getRecordCount())
            .build();
    }
}
```

---

## E12 - NOTIFICATIONS & ANNOUNCEMENTS

### Section: Notification System Design

**Description**: Multi-channel notification system with in-app, email, and SMS delivery.

**Design Specification**:
- Service: NotificationService
- Channels: IN_APP, EMAIL, SMS
- Templates with localization
- Delivery status tracking
- User preferences

**Sample Implementation**:
```java
package com.company.warehouse.notification.service;

import com.company.warehouse.notification.domain.Notification;
import com.company.warehouse.notification.domain.NotificationChannel;
import com.company.warehouse.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final NotificationTemplateService templateService;
    
    @Transactional
    public void sendNotification(NotificationRequest request) {
        // Get user preferences
        UserNotificationPreferences prefs = 
            getUserPreferences(request.getUserId());
        
        // Create notification record
        Notification notification = Notification.builder()
            .userId(request.getUserId())
            .type(request.getType())
            .title(request.getTitle())
            .message(request.getMessage())
            .build();
        
        notificationRepository.save(notification);
        
        // Send via enabled channels
        if (prefs.isEmailEnabled() && request.getChannels().contains(NotificationChannel.EMAIL)) {
            emailService.sendEmail(
                request.getUserEmail(),
                request.getTitle(),
                templateService.renderEmailTemplate(request)
            );
        }
        
        if (prefs.isSmsEnabled() && request.getChannels().contains(NotificationChannel.SMS)) {
            smsService.sendSms(
                request.getUserPhone(),
                templateService.renderSmsTemplate(request)
            );
        }
    }
}
```

---

## E13 - INTEGRATION LAYER (HRIS/WMS APIs)

### Section: Integration Architecture

**Description**: REST API integration layer for HRIS, WMS, and IDP systems with webhook support.

**Design Specification**:
- REST clients for external systems
- Webhook endpoints for event notifications
- OAuth2 authentication
- Idempotent operations
- Retry logic with exponential backoff

**Sample Implementation**:
```java
package com.company.warehouse.integration.hris;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Service
@RequiredArgsConstructor
public class HRISIntegrationService {
    
    private final RestTemplate restTemplate;
    private final OAuth2TokenService tokenService;
    
    @Value("${hris.api.base-url}")
    private String hrisBaseUrl;
    
    public void syncNewHire(NewHireDto newHire) {
        String url = hrisBaseUrl + "/api/employees";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(tokenService.getAccessToken());
        
        HttpEntity<NewHireDto> request = new HttpEntity<>(newHire, headers);
        
        ResponseEntity<EmployeeResponse> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            EmployeeResponse.class
        );
        
        if (response.getStatusCode().is2xxSuccessful()) {
            // Process successful sync
            createEmployeeFromHRIS(response.getBody());
        }
    }
    
    @PostMapping("/api/webhooks/hris/newhire")
    public ResponseEntity<Void> handleNewHireWebhook(@RequestBody NewHireWebhookDto webhook) {
        // Validate webhook signature
        if (!webhookValidator.isValid(webhook)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Process new hire
        employeeService.createFromHRIS(webhook.getEmployeeData());
        
        return ResponseEntity.ok().build();
    }
}
```

---

## E14 - AUDIT TRAIL & COMPLIANCE

### Section: Audit Logging Design

**Description**: Centralized audit logging with tamper-evident storage for compliance and forensic analysis.

**Design Specification**:
- Entity: AuditLog
- Fields: id, entityType, entityId, action, actor, timestamp, beforeValue, afterValue, ipAddress
- Immutable storage
- Aspect-based logging
- Export capabilities

**Sample Implementation**:
```java
package com.company.warehouse.audit.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "entity_type", nullable = false, length = 64)
    private String entityType;
    
    @Column(name = "entity_id", nullable = false)
    private Long entityId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private AuditAction action;
    
    @Column(name = "actor", nullable = false, length = 128)
    private String actor;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "before_value", columnDefinition = "TEXT")
    private String beforeValue;
    
    @Column(name = "after_value", columnDefinition = "TEXT")
    private String afterValue;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
}

public enum AuditAction {
    CREATE, UPDATE, DELETE, VIEW
}

// Aspect for automatic audit logging
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {
    
    private final AuditLogRepository auditLogRepository;
    
    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void logAudit(JoinPoint joinPoint, Auditable auditable, Object result) {
        AuditLog log = AuditLog.builder()
            .entityType(auditable.entityType())
            .action(auditable.action())
            .actor(SecurityContextHolder.getContext().getAuthentication().getName())
            .timestamp(LocalDateTime.now())
            .build();
        
        auditLogRepository.save(log);
    }
}
```

---

## E15 - REPORTING & ANALYTICS

### Section: Reporting System Design

**Description**: Comprehensive reporting system with operational reports, dashboards, and BI integration.

**Design Specification**:
- Service: ReportingService
- Report types: Attendance, Overtime, Leave, Certification, Safety
- Export formats: CSV, PDF, Excel
- Role-based access control
- Scheduled report generation

**Sample Implementation**:
```java
package com.company.warehouse.reporting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportingService {
    
    private final AttendanceRepository attendanceRepository;
    private final ReportExportService exportService;
    
    public AttendanceReport generateAttendanceReport(
            LocalDate startDate, 
            LocalDate endDate,
            String department) {
        
        List<AttendanceSummary> summaries = 
            attendanceRepository.getAttendanceSummary(startDate, endDate, department);
        
        return AttendanceReport.builder()
            .periodStart(startDate)
            .periodEnd(endDate)
            .department(department)
            .summaries(summaries)
            .totalHours(calculateTotalHours(summaries))
            .overtimeHours(calculateOvertimeHours(summaries))
            .build();
    }
    
    public File exportReportToCsv(AttendanceReport report) {
        return exportService.exportToCsv(report);
    }
    
    public File exportReportToPdf(AttendanceReport report) {
        return exportService.exportToPdf(report);
    }
}
```

---

## E16 - MOBILE ACCESS (PWA)

### Section: PWA Configuration

**Description**: Progressive Web App configuration with offline support and mobile-optimized views.

**Design Specification**:
- PWA manifest configuration
- Service worker for offline caching
- Responsive UI components
- Offline queue for critical operations

**Sample Implementation**:
```javascript
// manifest.json
{
  "name": "Warehouse Employee Management",
  "short_name": "Warehouse",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#2196F3",
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

// service-worker.js
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open('warehouse-v1').then((cache) => {
      return cache.addAll([
        '/',
        '/index.html',
        '/styles.css',
        '/app.js',
        '/api/attendance/clock-in',
        '/api/attendance/clock-out'
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

## E17 - ONBOARDING & OFFBOARDING WORKFLOW

### Section: Workflow Automation

**Description**: Automated onboarding and offboarding workflows with task generation and tracking.

**Design Specification**:
- Service: OnboardingService, OffboardingService
- Task generation for training, asset assignment, access provisioning
- Workflow state management
- Integration with HRIS and IDP

**Sample Implementation**:
```java
package com.company.warehouse.workflow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingService {
    
    private final EmployeeService employeeService;
    private final TrainingService trainingService;
    private final EquipmentService equipmentService;
    private final AccessProvisioningService accessService;
    
    @Transactional
    public OnboardingResult onboardEmployee(OnboardingRequest request) {
        // Create employee record
        Employee employee = employeeService.createFromHRIS(request.getEmployeeData());
        
        // Generate training tasks
        List<TrainingTask> trainingTasks = trainingService.generateRequiredTraining(employee);
        
        // Assign initial equipment
        equipmentService.assignInitialEquipment(employee);
        
        // Provision system access
        accessService.provisionAccess(employee);
        
        // Create initial schedule
        shiftService.assignInitialSchedule(employee);
        
        return OnboardingResult.builder()
            .employeeId(employee.getId())
            .trainingTasks(trainingTasks)
            .status(OnboardingStatus.IN_PROGRESS)
            .build();
    }
    
    @Transactional
    public OffboardingResult offboardEmployee(Long employeeId) {
        Employee employee = employeeService.getEmployee(employeeId);
        
        // Revoke system access
        accessService.revokeAccess(employee);
        
        // Collect assigned equipment
        equipmentService.flagEquipmentForReturn(employee);
        
        // Update schedules
        shiftService.removeFromFutureSchedules(employee);
        
        // Update employee status
        employee.setStatus(EmployeeStatus.TERMINATED);
        employeeService.updateEmployee(employee);
        
        return OffboardingResult.builder()
            .employeeId(employeeId)
            .status(OffboardingStatus.COMPLETED)
            .build();
    }
}
```

---

## E18 - LOCALIZATION & MULTI-WAREHOUSE

### Section: Multi-Warehouse Support

**Description**: Multi-warehouse configuration with distinct calendars, policies, and localization.

**Design Specification**:
- Entity: Warehouse
- Warehouse-specific calendars and policies
- UI localization with i18n
- Warehouse selection and filtering

**Sample Implementation**:
```java
package com.company.warehouse.warehouse.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code", unique = true, nullable = false, length = 16)
    private String code;
    
    @Column(name = "name", nullable = false, length = 128)
    private String name;
    
    @Column(name = "address", length = 255)
    private String address;
    
    @Column(name = "timezone", length = 64)
    private String timezone;
    
    @Column(name = "locale", length = 10)
    private String locale;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
}

// application.yml for localization
spring:
  messages:
    basename: i18n/messages
    encoding: UTF-8
    cache-duration: 3600
```

---

## E19 - ADVANCED SCHEDULING (AI-ASSISTED)

### Section: AI Scheduling Integration

**Description**: AI-powered scheduling optimization with preference learning and conflict resolution.

**Design Specification**:
- Integration with ML service
- Preference-based scheduling
- Conflict detection and resolution
- Explainable recommendations

**Sample Implementation**:
```java
package com.company.warehouse.scheduling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AISchedulingService {
    
    private final RestTemplate restTemplate;
    private final ShiftService shiftService;
    
    @Value("${ai.scheduling.service.url}")
    private String aiServiceUrl;
    
    public ScheduleRecommendation generateOptimalSchedule(SchedulingRequest request) {
        // Prepare input data
        AISchedulingInput input = AISchedulingInput.builder()
            .employees(request.getEmployees())
            .shiftRequirements(request.getShiftRequirements())
            .historicalDemand(getHistoricalDemand(request.getPeriod()))
            .employeePreferences(getEmployeePreferences(request.getEmployees()))
            .certifications(getCertifications(request.getEmployees()))
            .build();
        
        // Call AI service
        ResponseEntity<AISchedulingOutput> response = restTemplate.postForEntity(
            aiServiceUrl + "/api/schedule/optimize",
            input,
            AISchedulingOutput.class
        );
        
        AISchedulingOutput output = response.getBody();
        
        // Convert to schedule recommendation
        return ScheduleRecommendation.builder()
            .assignments(output.getAssignments())
            .conflicts(output.getConflicts())
            .explanation(output.getExplanation())
            .confidenceScore(output.getConfidenceScore())
            .build();
    }
}
```

---

## E20 - SELF-SERVICE PORTAL

### Section: Employee Portal Design

**Description**: Self-service portal for employees to access pay stubs, update information, and request documents.

**Design Specification**:
- Controller: PortalController
- Features: Pay stubs, contact info, document requests, policies
- Role-based access
- Mobile-friendly UI

**Sample Implementation**:
```java
package com.company.warehouse.portal.controller;

import com.company.warehouse.employee.domain.Employee;
import com.company.warehouse.portal.dto.*;
import com.company.warehouse.portal.service.PortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class PortalController {
    
    private final PortalService portalService;
    
    @GetMapping("/paystubs")
    public List<PayStubDto> getPayStubs(@AuthenticationPrincipal Employee employee) {
        return portalService.getPayStubs(employee.getId());
    }
    
    @GetMapping("/contact-info")
    public ContactInfoDto getContactInfo(@AuthenticationPrincipal Employee employee) {
        return portalService.getContactInfo(employee.getId());
    }
    
    @PutMapping("/contact-info")
    public ContactInfoDto updateContactInfo(
            @AuthenticationPrincipal Employee employee,
            @Valid @RequestBody ContactInfoUpdateDto updateDto) {
        return portalService.updateContactInfo(employee.getId(), updateDto);
    }
    
    @PostMapping("/document-requests")
    public DocumentRequestDto requestDocument(
            @AuthenticationPrincipal Employee employee,
            @Valid @RequestBody DocumentRequestDto requestDto) {
        return portalService.createDocumentRequest(employee.getId(), requestDto);
    }
    
    @GetMapping("/policies")
    public List<PolicyDto> getPolicies() {
        return portalService.getCompanyPolicies();
    }
}
```

---

## EXCEPTION HANDLING STRATEGY

### Section: Global Exception Handling

**Description**: Centralized exception handling with custom exceptions and standardized error responses.

**Design Specification**:
- @ControllerAdvice for global exception handling
- Custom exceptions: ResourceNotFoundException, DuplicateResourceException, BusinessException
- ErrorResponse DTO with error codes and messages
- HTTP status code mapping

**Sample Implementation**:
```java
package com.company.warehouse.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Resource Not Found")
            .message(ex.getMessage())
            .path(request.getDescription(false))
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Duplicate Resource")
            .message(ex.getMessage())
            .path(request.getDescription(false))
            .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Business Rule Violation")
            .message(ex.getMessage())
            .path(request.getDescription(false))
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            validationErrors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Input validation failed")
            .validationErrors(validationErrors)
            .path(request.getDescription(false))
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}

@Data
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors;
}
```

---

## TESTING APPROACH

### Section: Comprehensive Testing Strategy

**Description**: Multi-layered testing approach with unit, integration, and end-to-end tests.

**Design Specification**:
- Unit tests: JUnit 5, Mockito for service layer
- Integration tests: @SpringBootTest, Testcontainers for database
- Controller tests: MockMvc for REST endpoints
- E2E tests: Selenium/Playwright for critical workflows
- Test coverage: Minimum 80% code coverage

**Sample Implementation**:
```java
package com.company.warehouse.employee.service;

import com.company.warehouse.employee.domain.Employee;
import com.company.warehouse.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    
    @Mock
    private EmployeeRepository employeeRepository;
    
    @Mock
    private EmployeeMapper employeeMapper;
    
    @InjectMocks
    private EmployeeService employeeService;
    
    @Test
    void createEmployee_WithValidData_ShouldCreateEmployee() {
        // Given
        EmployeeCreateDto createDto = EmployeeCreateDto.builder()
            .badgeId("EMP001")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@company.com")
            .build();
        
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("EMP001");
        
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(false);
        when(employeeMapper.toEntity(createDto)).thenReturn(employee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeMapper.toDto(employee)).thenReturn(new EmployeeDto());
        
        // When
        EmployeeDto result = employeeService.createEmployee(createDto);
        
        // Then
        assertThat(result).isNotNull();
        verify(employeeRepository).save(any(Employee.class));
    }
    
    @Test
    void createEmployee_WithDuplicateBadgeId_ShouldThrowException() {
        // Given
        EmployeeCreateDto createDto = EmployeeCreateDto.builder()
            .badgeId("EMP001")
            .build();
        
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> employeeService.createEmployee(createDto))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("EMP001");
    }
}

// Integration Test
@SpringBootTest
@Testcontainers
class EmployeeControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("warehouse_test")
        .withUsername("test")
        .withPassword("test");
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Test
    void createEmployee_ShouldReturnCreated() throws Exception {
        String requestBody = """{
            "badgeId": "EMP001",
            "firstName": "John",
            "lastName": "Doe",
            "email": "john.doe@company.com",
            "role": "WORKER",
            "status": "ACTIVE",
            "hireDate": "2024-01-01"
        }""";
        
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.badgeId").value("EMP001"))
            .andExpect(jsonPath("$.firstName").value("John"));
    }
}
```

---

## DATABASE SCHEMA DESIGN

### Section: Complete Database Schema

**Description**: Normalized database schema with proper indexes, constraints, and relationships.

**Design Specification**:
- Tables for all entities
- Primary keys, foreign keys, unique constraints
- Indexes for performance optimization
- Audit columns (created_at, updated_at, created_by, updated_by)

**Sample Implementation**:
```sql
-- Employees Table
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    badge_id VARCHAR(32) UNIQUE NOT NULL,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    email VARCHAR(128) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    shift_group VARCHAR(32),
    hire_date DATE,
    status VARCHAR(32) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128)
);

CREATE INDEX idx_employees_badge_id ON employees(badge_id);
CREATE INDEX idx_employees_department ON employees(department);
CREATE INDEX idx_employees_status ON employees(status);

-- Attendance Table
CREATE TABLE attendance (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    clock_in TIMESTAMP NOT NULL,
    clock_out TIMESTAMP,
    device_id VARCHAR(64),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    status VARCHAR(32) NOT NULL,
    hours_worked DECIMAL(5, 2),
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attendance_employee_id ON attendance(employee_id);
CREATE INDEX idx_attendance_clock_in ON attendance(clock_in);
CREATE INDEX idx_attendance_status ON attendance(status);

-- Shift Templates Table
CREATE TABLE shift_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_recurring BOOLEAN DEFAULT FALSE,
    description VARCHAR(255)
);

-- Shift Assignments Table
CREATE TABLE shift_assignments (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    template_id BIGINT NOT NULL REFERENCES shift_templates(id),
    shift_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL
);

CREATE INDEX idx_shift_assignments_employee_id ON shift_assignments(employee_id);
CREATE INDEX idx_shift_assignments_shift_date ON shift_assignments(shift_date);

-- Leave Requests Table
CREATE TABLE leave_requests (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    leave_type VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    days_requested INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL,
    reason VARCHAR(500),
    approved_by BIGINT REFERENCES employees(id),
    approval_date TIMESTAMP
);

CREATE INDEX idx_leave_requests_employee_id ON leave_requests(employee_id);
CREATE INDEX idx_leave_requests_status ON leave_requests(status);

-- Certifications Table
CREATE TABLE certifications (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(500),
    validity_period_days INTEGER,
    is_required_for_equipment BOOLEAN DEFAULT FALSE
);

-- Employee Certifications Table
CREATE TABLE employee_certifications (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    certification_id BIGINT NOT NULL REFERENCES certifications(id),
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    document_url VARCHAR(255)
);

CREATE INDEX idx_employee_certifications_employee_id ON employee_certifications(employee_id);
CREATE INDEX idx_employee_certifications_expiry_date ON employee_certifications(expiry_date);

-- Safety Incidents Table
CREATE TABLE safety_incidents (
    id BIGSERIAL PRIMARY KEY,
    incident_date TIMESTAMP NOT NULL,
    location VARCHAR(128) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    type VARCHAR(32) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    corrective_actions TEXT
);

CREATE INDEX idx_safety_incidents_incident_date ON safety_incidents(incident_date);
CREATE INDEX idx_safety_incidents_severity ON safety_incidents(severity);

-- Equipment Table
CREATE TABLE equipment (
    id BIGSERIAL PRIMARY KEY,
    asset_id VARCHAR(32) UNIQUE NOT NULL,
    type VARCHAR(32) NOT NULL,
    serial_number VARCHAR(64),
    status VARCHAR(32) NOT NULL,
    requires_certification BOOLEAN DEFAULT FALSE,
    required_certification_id BIGINT REFERENCES certifications(id)
);

CREATE INDEX idx_equipment_asset_id ON equipment(asset_id);
CREATE INDEX idx_equipment_status ON equipment(status);

-- Equipment Assignments Table
CREATE TABLE equipment_assignments (
    id BIGSERIAL PRIMARY KEY,
    equipment_id BIGINT NOT NULL REFERENCES equipment(id),
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    checkout_time TIMESTAMP NOT NULL,
    return_time TIMESTAMP,
    condition_at_checkout VARCHAR(32),
    condition_at_return VARCHAR(32)
);

CREATE INDEX idx_equipment_assignments_equipment_id ON equipment_assignments(equipment_id);
CREATE INDEX idx_equipment_assignments_employee_id ON equipment_assignments(employee_id);

-- Performance Reviews Table
CREATE TABLE performance_reviews (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    reviewer_id BIGINT NOT NULL REFERENCES employees(id),
    review_period_start DATE NOT NULL,
    review_period_end DATE NOT NULL,
    overall_rating VARCHAR(32),
    comments TEXT,
    status VARCHAR(32) NOT NULL,
    acknowledged_at TIMESTAMP
);

CREATE INDEX idx_performance_reviews_employee_id ON performance_reviews(employee_id);

-- Audit Logs Table
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(64) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(32) NOT NULL,
    actor VARCHAR(128) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    before_value TEXT,
    after_value TEXT,
    ip_address VARCHAR(45)
);

CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_entity_id ON audit_logs(entity_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);

-- Warehouses Table
CREATE TABLE warehouses (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(16) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    address VARCHAR(255),
    timezone VARCHAR(64),
    locale VARCHAR(10),
    is_active BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_warehouses_code ON warehouses(code);
```

---

## CONFIGURATION FILES

### Section: Application Configuration

**Description**: Complete application.yml configuration for all modules and integrations.

**Sample Implementation**:
```yaml
spring:
  application:
    name: warehouse-management-system
  
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:warehouse_db}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
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
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
  
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    validate-on-migrate: true
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI}
          jwk-set-uri: ${JWT_JWK_SET_URI}
  
  messages:
    basename: i18n/messages
    encoding: UTF-8
    cache-duration: 3600

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

server:
  port: 8080
  compression:
    enabled: true
  error:
    include-message: always
    include-binding-errors: always

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000 # 24 hours

hris:
  api:
    base-url: ${HRIS_API_URL}
    client-id: ${HRIS_CLIENT_ID}
    client-secret: ${HRIS_CLIENT_SECRET}

wms:
  api:
    base-url: ${WMS_API_URL}
    api-key: ${WMS_API_KEY}

payroll:
  sftp:
    host: ${PAYROLL_SFTP_HOST}
    port: ${PAYROLL_SFTP_PORT:22}
    username: ${PAYROLL_SFTP_USERNAME}
    password: ${PAYROLL_SFTP_PASSWORD}

notification:
  email:
    enabled: true
    smtp:
      host: ${SMTP_HOST}
      port: ${SMTP_PORT:587}
      username: ${SMTP_USERNAME}
      password: ${SMTP_PASSWORD}
  sms:
    enabled: true
    provider: twilio
    account-sid: ${TWILIO_ACCOUNT_SID}
    auth-token: ${TWILIO_AUTH_TOKEN}

ai:
  scheduling:
    service:
      url: ${AI_SCHEDULING_SERVICE_URL}
      api-key: ${AI_SCHEDULING_API_KEY}

logging:
  level:
    root: INFO
    com.company.warehouse: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

---

## CONCLUSION

This comprehensive low-level technical design document covers all 20 epics of the Warehouse Employee Management System with detailed Spring Boot architecture, entity designs, service/repository/controller specifications, security configurations, integration patterns, exception handling, testing strategies, and database schemas. Each section provides production-ready code samples and follows Spring Boot best practices and industry standards.

The document is structured to enable development teams to immediately begin implementation with clear technical specifications, code examples, and architectural guidance for building a robust, scalable, and maintainable warehouse employee management system.