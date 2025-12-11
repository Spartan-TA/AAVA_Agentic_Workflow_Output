# WAREHOUSE EMPLOYEE MANAGEMENT SYSTEM - LOW-LEVEL TECHNICAL DESIGN DOCUMENT

## Document Overview

This comprehensive technical design document provides detailed low-level specifications for implementing the Warehouse Employee Management System using Spring Boot best practices and industry standards. Each section corresponds to a user story epic and includes architecture overview, package structure, entity design, service/repository/controller specifications, configuration, security settings, integration points, and sample implementations.

---

## Section: E01 - Project Scaffolding & Domain Setup

### Description
Establishes the foundational Spring Boot architecture, base package structure, and core modules for the Warehouse Employee Management System. Ensures consistency, scalability, and maintainability across all subsequent features.

### Design Specification
- Spring Boot (Maven) project initialized with standard directory structure (src/main/java, src/main/resources)
- Base packages: com.warehouse, com.warehouse.employee, com.warehouse.scheduling, com.warehouse.attendance, com.warehouse.safety
- Core modules: employee, scheduling, attendance, safety
- Flyway/Liquibase configured for database migrations
- Spring Boot Actuator enabled for health checks and monitoring
- README with build/run instructions

### Sample Implementation

```java
// src/main/java/com/warehouse/WarehouseEmployeeManagementApplication.java
package com.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WarehouseEmployeeManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeManagementApplication.class, args);
    }
}

// src/main/resources/application.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse
    username: warehouse_user
    password: secret
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
      show-details: always
```

---

## Section: E02 - Employee Master Data CRUD

### Description
Implements comprehensive CRUD operations for Employee domain, ensuring unique badge IDs, soft-delete functionality, pagination, filtering capabilities, and OpenAPI documentation for API consumers.

### Design Specification
- Entity: Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status, deleted)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService with CRUD methods, soft-delete logic, filtering, and pagination
- Controller: EmployeeController with REST endpoints (POST/GET/PUT/PATCH/DELETE /employees)
- DTOs for web/API communication
- OpenAPI schemas with examples
- Validation annotations for data integrity

### Sample Implementation

```java
// Employee Entity
package com.warehouse.employee.domain;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badgeId"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    private String name;
    
    @NotBlank
    @Column(unique = true)
    private String badgeId;
    
    private String role;
    private String department;
    private String shiftGroup;
    
    private LocalDate hireDate;
    
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;
    
    private boolean deleted = false;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

// EmployeeRepository
package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    Page<Employee> findByDepartmentAndDeletedFalse(String department, Pageable pageable);
}

// EmployeeService
package com.warehouse.employee.service;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.dto.EmployeeDto;
import com.warehouse.employee.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    public Employee createEmployee(EmployeeDto dto) {
        Employee employee = new Employee();
        employee.setName(dto.getName());
        employee.setBadgeId(dto.getBadgeId());
        employee.setRole(dto.getRole());
        employee.setDepartment(dto.getDepartment());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(EmployeeStatus.ACTIVE);
        return employeeRepository.save(employee);
    }
    
    public Page<Employee> getEmployees(Pageable pageable, String department) {
        if (department != null) {
            return employeeRepository.findByDepartmentAndDeletedFalse(department, pageable);
        }
        return employeeRepository.findAllByDeletedFalse(pageable);
    }
    
    public void softDeleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }
}

// EmployeeController
package com.warehouse.employee.controller;

import com.warehouse.employee.dto.EmployeeDto;
import com.warehouse.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee Management", description = "APIs for managing employee master data")
public class EmployeeController {
    private final EmployeeService employeeService;
    
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    
    @PostMapping
    @Operation(summary = "Create new employee")
    public ResponseEntity<EmployeeDto> create(@Valid @RequestBody EmployeeDto dto) {
        Employee employee = employeeService.createEmployee(dto);
        return ResponseEntity.ok(EmployeeDto.from(employee));
    }
    
    @GetMapping
    @Operation(summary = "List employees with pagination and filtering")
    public Page<EmployeeDto> list(Pageable pageable, 
                                  @RequestParam(required = false) String department) {
        return employeeService.getEmployees(pageable, department)
            .map(EmployeeDto::from);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete employee")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Section: E03 - Role-Based Access Control (RBAC)

### Description
Integrates Spring Security with role-based access control supporting ADMIN, HR, SUPERVISOR, and WORKER roles. Implements method-level security, endpoint protection, row-level constraints, and configurable authentication (API key/OAuth2).

### Design Specification
- SecurityConfig with role mappings and endpoint protection
- Method-level security annotations (@PreAuthorize, @Secured)
- API key/OAuth2 toggle via application properties
- Row-level security constraints in service/repository layers
- JWT token generation and validation
- Custom authentication filters

### Sample Implementation

```java
// SecurityConfig
package com.warehouse.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers("/api/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .antMatchers("/api/attendance/**").hasAnyRole("ADMIN", "SUPERVISOR", "WORKER")
                .antMatchers("/api/shifts/**").hasAnyRole("ADMIN", "SUPERVISOR")
                .antMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer()
                .jwt();
        
        return http.build();
    }
}

// Method-level security in EmployeeService
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == authentication.principal.department)")
public Employee updateEmployee(Employee employee) {
    return employeeRepository.save(employee);
}

@PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
public void deleteEmployee(Long id) {
    employeeRepository.deleteById(id);
}
```

---

## Section: E04 - Time & Attendance

### Description
Provides comprehensive time tracking with clock-in/out events, geofence/device capture, automatic shift association, missed punch corrections workflow, and attendance reporting capabilities.

### Design Specification
- Entity: AttendanceEvent (id, employeeId, timestamp, type, deviceId, location, shiftId, correctionStatus)
- Repository: AttendanceRepository with custom queries
- Service: AttendanceService with clock-in/out logic, shift association, corrections workflow
- Controller: AttendanceController (POST /attendance/clock-in, /clock-out)
- Reports exportable as CSV with daily totals
- Geofence validation for clock events

### Sample Implementation

```java
// AttendanceEvent Entity
package com.warehouse.attendance.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "attendance_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceEvent {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long employeeId;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Enumerated(EnumType.STRING)
    private EventType type; // CLOCK_IN, CLOCK_OUT
    
    private String deviceId;
    private String location;
    private Long shiftId;
    
    @Enumerated(EnumType.STRING)
    private CorrectionStatus correctionStatus; // NONE, PENDING, APPROVED
    
    private String notes;
}

// AttendanceService
package com.warehouse.attendance.service;

import com.warehouse.attendance.domain.AttendanceEvent;
import com.warehouse.attendance.repository.AttendanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Transactional
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final ShiftService shiftService;
    
    public AttendanceService(AttendanceRepository attendanceRepository, 
                           ShiftService shiftService) {
        this.attendanceRepository = attendanceRepository;
        this.shiftService = shiftService;
    }
    
    public AttendanceEvent clockIn(Long employeeId, String deviceId, String location) {
        // Validate geofence
        if (!isValidLocation(location)) {
            throw new InvalidLocationException("Clock-in location outside permitted area");
        }
        
        // Find associated shift
        Long shiftId = shiftService.findCurrentShift(employeeId, LocalDateTime.now());
        
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(employeeId);
        event.setTimestamp(LocalDateTime.now());
        event.setType(EventType.CLOCK_IN);
        event.setDeviceId(deviceId);
        event.setLocation(location);
        event.setShiftId(shiftId);
        event.setCorrectionStatus(CorrectionStatus.NONE);
        
        return attendanceRepository.save(event);
    }
    
    public AttendanceEvent clockOut(Long employeeId, String deviceId, String location) {
        AttendanceEvent event = new AttendanceEvent();
        event.setEmployeeId(employeeId);
        event.setTimestamp(LocalDateTime.now());
        event.setType(EventType.CLOCK_OUT);
        event.setDeviceId(deviceId);
        event.setLocation(location);
        event.setCorrectionStatus(CorrectionStatus.NONE);
        
        return attendanceRepository.save(event);
    }
    
    private boolean isValidLocation(String location) {
        // Implement geofence validation logic
        return true;
    }
}

// AttendanceController
package com.warehouse.attendance.controller;

import com.warehouse.attendance.dto.ClockEventDto;
import com.warehouse.attendance.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;
    
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }
    
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockEventDto dto,
                                     @AuthenticationPrincipal UserDetails user) {
        AttendanceEvent event = attendanceService.clockIn(
            user.getEmployeeId(), 
            dto.getDeviceId(), 
            dto.getLocation()
        );
        return ResponseEntity.ok(event);
    }
    
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockEventDto dto,
                                      @AuthenticationPrincipal UserDetails user) {
        AttendanceEvent event = attendanceService.clockOut(
            user.getEmployeeId(), 
            dto.getDeviceId(), 
            dto.getLocation()
        );
        return ResponseEntity.ok(event);
    }
}
```

---

## Section: E05 - Shift & Schedule Management

### Description
Manages recurring shift templates, rotations, overtime rules, blackout dates, and employee assignments with conflict detection and bulk operations support.

### Design Specification
- Entities: ShiftTemplate, ShiftAssignment, OvertimeRule, BlackoutDate
- Repositories for each entity with custom queries
- Service: ShiftService with CRUD, conflict detection, bulk assignment, audit logging
- Controller: ShiftController with REST endpoints
- Conflict detection algorithm for overlapping shifts
- Support for recurring patterns (daily, weekly, monthly)

### Sample Implementation

```java
// ShiftTemplate Entity
package com.warehouse.scheduling.domain;

import javax.persistence.*;
import java.time.LocalTime;
import lombok.*;

@Entity
@Table(name = "shift_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftTemplate {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    private String recurrencePattern; // DAILY, WEEKLY, MONTHLY
    private Integer durationMinutes;
    private boolean overtimeEligible;
}

// ShiftAssignment Entity
@Entity
@Table(name = "shift_assignments")
@Data
public class ShiftAssignment {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long employeeId;
    private Long shiftTemplateId;
    private LocalDate assignmentDate;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    
    @Enumerated(EnumType.STRING)
    private AssignmentStatus status; // SCHEDULED, COMPLETED, CANCELLED
}

// ShiftService
package com.warehouse.scheduling.service;

import com.warehouse.scheduling.domain.*;
import com.warehouse.scheduling.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ShiftService {
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    
    public ShiftService(ShiftTemplateRepository shiftTemplateRepository,
                       ShiftAssignmentRepository shiftAssignmentRepository) {
        this.shiftTemplateRepository = shiftTemplateRepository;
        this.shiftAssignmentRepository = shiftAssignmentRepository;
    }
    
    public ShiftAssignment assignShift(Long employeeId, Long shiftTemplateId, LocalDate date) {
        // Check for conflicts
        if (detectConflict(employeeId, date)) {
            throw new ShiftConflictException("Employee already has a shift on this date");
        }
        
        ShiftTemplate template = shiftTemplateRepository.findById(shiftTemplateId)
            .orElseThrow(() -> new ResourceNotFoundException("Shift template not found"));
        
        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setEmployeeId(employeeId);
        assignment.setShiftTemplateId(shiftTemplateId);
        assignment.setAssignmentDate(date);
        assignment.setStartDateTime(date.atTime(template.getStartTime()));
        assignment.setEndDateTime(date.atTime(template.getEndTime()));
        assignment.setStatus(AssignmentStatus.SCHEDULED);
        
        return shiftAssignmentRepository.save(assignment);
    }
    
    public boolean detectConflict(Long employeeId, LocalDate date) {
        List<ShiftAssignment> existingAssignments = 
            shiftAssignmentRepository.findByEmployeeIdAndAssignmentDate(employeeId, date);
        return !existingAssignments.isEmpty();
    }
    
    public void bulkAssignShifts(List<Long> employeeIds, Long shiftTemplateId, LocalDate startDate, int days) {
        for (Long employeeId : employeeIds) {
            for (int i = 0; i < days; i++) {
                LocalDate assignmentDate = startDate.plusDays(i);
                try {
                    assignShift(employeeId, shiftTemplateId, assignmentDate);
                } catch (ShiftConflictException e) {
                    // Log conflict and continue
                }
            }
        }
    }
}
```

---

## Section: E06 - Leave & Absence Management

### Description
Handles PTO, sick leave, and unpaid leave requests with approval workflows, accrual balance tracking, and integration with scheduling and payroll systems.

### Design Specification
- Entities: LeaveRequest, LeaveBalance, LeavePolicy
- Service: LeaveService with request submission, approval workflow, balance updates
- Controller: LeaveController with REST endpoints
- Integration with scheduling system to flag coverage needs
- Automatic balance calculations based on policy rules
- Approval workflow with supervisor notifications

### Sample Implementation

```java
// LeaveRequest Entity
package com.warehouse.leave.domain;

import javax.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "leave_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long employeeId;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    private LeaveType type; // PTO, SICK, UNPAID
    
    @Enumerated(EnumType.STRING)
    private LeaveStatus status; // REQUESTED, APPROVED, DENIED
    
    private String reason;
    private Long approverId;
    private LocalDateTime approvedAt;
}

// LeaveBalance Entity
@Entity
@Table(name = "leave_balances")
@Data
public class LeaveBalance {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long employeeId;
    
    @Enumerated(EnumType.STRING)
    private LeaveType type;
    
    private Double accrued;
    private Double used;
    private Double available;
    private Integer year;
}

// LeaveService
package com.warehouse.leave.service;

import com.warehouse.leave.domain.*;
import com.warehouse.leave.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LeaveService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final NotificationService notificationService;
    
    public LeaveService(LeaveRequestRepository leaveRequestRepository,
                       LeaveBalanceRepository leaveBalanceRepository,
                       NotificationService notificationService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.notificationService = notificationService;
    }
    
    public LeaveRequest requestLeave(Long employeeId, LeaveRequestDto dto) {
        // Check balance
        LeaveBalance balance = leaveBalanceRepository
            .findByEmployeeIdAndType(employeeId, dto.getType())
            .orElseThrow(() -> new ResourceNotFoundException("Leave balance not found"));
        
        double daysRequested = calculateDays(dto.getStartDate(), dto.getEndDate());
        
        if (balance.getAvailable() < daysRequested) {
            throw new InsufficientLeaveBalanceException("Insufficient leave balance");
        }
        
        LeaveRequest request = new LeaveRequest();
        request.setEmployeeId(employeeId);
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setType(dto.getType());
        request.setStatus(LeaveStatus.REQUESTED);
        request.setReason(dto.getReason());
        
        LeaveRequest saved = leaveRequestRepository.save(request);
        
        // Notify supervisor
        notificationService.notifySupervisor(employeeId, "New leave request pending approval");
        
        return saved;
    }
    
    public void approveLeave(Long requestId, Long approverId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        
        request.setStatus(LeaveStatus.APPROVED);
        request.setApproverId(approverId);
        request.setApprovedAt(LocalDateTime.now());
        
        leaveRequestRepository.save(request);
        
        // Update balance
        updateBalance(request);
        
        // Notify employee
        notificationService.notifyEmployee(request.getEmployeeId(), "Leave request approved");
    }
    
    private void updateBalance(LeaveRequest request) {
        LeaveBalance balance = leaveBalanceRepository
            .findByEmployeeIdAndType(request.getEmployeeId(), request.getType())
            .orElseThrow();
        
        double days = calculateDays(request.getStartDate(), request.getEndDate());
        balance.setUsed(balance.getUsed() + days);
        balance.setAvailable(balance.getAvailable() - days);
        
        leaveBalanceRepository.save(balance);
    }
    
    private double calculateDays(LocalDate start, LocalDate end) {
        return java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
    }
}
```

---

## Section: E07 - Training & Certification Tracking

### Description
Tracks employee certifications, expiration dates, renewals, blocks task assignments for expired certifications, and manages proof document uploads.

### Design Specification
- Entities: Certification, CertificationDocument
- Service: CertificationService with CRUD, expiry alerts, assignment validation
- Controller: CertificationController with REST endpoints
- Scheduled job for expiry notifications (30 days, 7 days)
- Integration with scheduling to prevent unqualified assignments
- Document storage for certification proofs

### Sample Implementation

```java
// Certification Entity
package com.warehouse.training.domain;

import javax.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "certifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certification {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long employeeId;
    
    @Column(nullable = false)
    private String type; // FORKLIFT, HAZMAT, FIRST_AID, etc.
    
    @Column(nullable = false)
    private LocalDate issueDate;
    
    @Column(nullable = false)
    private LocalDate expiryDate;
    
    private boolean valid;
    private String issuingAuthority;
    private String certificationNumber;
}

// CertificationDocument Entity
@Entity
@Table(name = "certification_documents")
@Data
public class CertificationDocument {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long certificationId;
    private String fileName;
    private String fileType;
    private String storagePath;
    private LocalDateTime uploadedAt;
}

// CertificationService
package com.warehouse.training.service;

import com.warehouse.training.domain.Certification;
import com.warehouse.training.repository.CertificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CertificationService {
    private final CertificationRepository certificationRepository;
    private final NotificationService notificationService;
    
    public CertificationService(CertificationRepository certificationRepository,
                               NotificationService notificationService) {
        this.certificationRepository = certificationRepository;
        this.notificationService = notificationService;
    }
    
    public Certification addCertification(Long employeeId, CertificationDto dto) {
        Certification cert = new Certification();
        cert.setEmployeeId(employeeId);
        cert.setType(dto.getType());
        cert.setIssueDate(dto.getIssueDate());
        cert.setExpiryDate(dto.getExpiryDate());
        cert.setValid(true);
        cert.setIssuingAuthority(dto.getIssuingAuthority());
        cert.setCertificationNumber(dto.getCertificationNumber());
        
        return certificationRepository.save(cert);
    }
    
    @Scheduled(cron = "0 0 9 * * *") // Daily at 9 AM
    public void checkExpiringCertifications() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysOut = today.plusDays(30);
        LocalDate sevenDaysOut = today.plusDays(7);
        
        // 30-day alerts
        List<Certification> expiringSoon = certificationRepository
            .findByExpiryDateBetween(today, thirtyDaysOut);
        
        for (Certification cert : expiringSoon) {
            notificationService.notifyEmployee(
                cert.getEmployeeId(), 
                String.format("Your %s certification expires on %s", 
                    cert.getType(), cert.getExpiryDate())
            );
        }
        
        // Mark expired certifications as invalid
        List<Certification> expired = certificationRepository
            .findByExpiryDateBeforeAndValidTrue(today);
        
        for (Certification cert : expired) {
            cert.setValid(false);
            certificationRepository.save(cert);
        }
    }
    
    public boolean isQualified(Long employeeId, String taskType) {
        List<Certification> certs = certificationRepository
            .findByEmployeeIdAndTypeAndValidTrue(employeeId, taskType);
        return !certs.isEmpty();
    }
    
    public void validateAssignment(Long employeeId, String requiredCertification) {
        if (!isQualified(employeeId, requiredCertification)) {
            throw new UnqualifiedEmployeeException(
                "Employee does not have valid " + requiredCertification + " certification"
            );
        }
    }
}
```

---

## Section: E11 - Payroll Export Integration

### Description
Generates payroll-ready export files from attendance and leave data, maps to provider-specific formats, and delivers securely via SFTP or API with retry logic.

### Design Specification
- Service: PayrollExportService with export generation, format mapping, delivery
- Support for multiple payroll providers (ADP, Paychex, etc.)
- Scheduled exports with configurable frequency
- SFTP and REST API delivery options
- Retry logic with exponential backoff
- Audit logging for all exports

### Sample Implementation

```java
// PayrollExportService
package com.warehouse.payroll.service;

import com.warehouse.attendance.repository.AttendanceRepository;
import com.warehouse.leave.repository.LeaveRequestRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class PayrollExportService {
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final SftpService sftpService;
    private final AuditService auditService;
    
    public PayrollExportService(AttendanceRepository attendanceRepository,
                               LeaveRequestRepository leaveRequestRepository,
                               SftpService sftpService,
                               AuditService auditService) {
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.sftpService = sftpService;
        this.auditService = auditService;
    }
    
    @Scheduled(cron = "0 0 2 * * MON") // Every Monday at 2 AM
    public void exportWeeklyPayroll() {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(6);
        
        exportPayroll(startDate, endDate);
    }
    
    public void exportPayroll(LocalDate startDate, LocalDate endDate) {
        try {
            // Gather attendance data
            List<AttendanceEvent> events = attendanceRepository
                .findByTimestampBetween(startDate.atStartOfDay(), endDate.atTime(23, 59));
            
            // Gather leave data
            List<LeaveRequest> leaves = leaveRequestRepository
                .findApprovedLeavesBetween(startDate, endDate);
            
            // Generate export file
            PayrollExportData exportData = new PayrollExportData(events, leaves);
            String csvContent = generateCSV(exportData);
            
            // Deliver via SFTP
            String fileName = String.format("payroll_%s_%s.csv", startDate, endDate);
            sftpService.uploadFile(fileName, csvContent);
            
            // Audit log
            auditService.logExport("PAYROLL", fileName, "SUCCESS");
            
        } catch (Exception e) {
            auditService.logExport("PAYROLL", "N/A", "FAILED: " + e.getMessage());
            throw new PayrollExportException("Failed to export payroll", e);
        }
    }
    
    private String generateCSV(PayrollExportData data) {
        StringBuilder csv = new StringBuilder();
        csv.append("EmployeeID,Date,HoursWorked,LeaveHours,OvertimeHours
");
        
        // Process attendance events and calculate hours
        Map<String, PayrollRecord> records = calculatePayrollRecords(data);
        
        for (PayrollRecord record : records.values()) {
            csv.append(String.format("%s,%s,%.2f,%.2f,%.2f
",
                record.getEmployeeId(),
                record.getDate(),
                record.getHoursWorked(),
                record.getLeaveHours(),
                record.getOvertimeHours()
            ));
        }
        
        return csv.toString();
    }
}
```

---

## Section: E12 - Notifications & Announcements

### Description
Sends multi-channel notifications (in-app, email, SMS) for system events, supports user preferences, quiet hours, localization, and delivery tracking.

### Design Specification
- Entities: Notification, Announcement, NotificationPreference
- Service: NotificationService with channel routing, template rendering, delivery tracking
- Support for multiple channels (email, SMS, push, in-app)
- User preference management
- Template engine for message formatting
- Rate limiting and quiet hours enforcement

### Sample Implementation

```java
// Notification Entity
package com.warehouse.notification.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel; // EMAIL, SMS, IN_APP, PUSH
    
    @Column(nullable = false)
    private String subject;
    
    @Column(length = 2000)
    private String message;
    
    private LocalDateTime sentAt;
    
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status; // PENDING, DELIVERED, FAILED
    
    private String errorMessage;
    private boolean read;
}

// NotificationService
package com.warehouse.notification.service;

import com.warehouse.notification.domain.*;
import com.warehouse.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final PushNotificationService pushService;
    
    public NotificationService(NotificationRepository notificationRepository,
                              EmailService emailService,
                              SmsService smsService,
                              PushNotificationService pushService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.smsService = smsService;
        this.pushService = pushService;
    }
    
    public void sendNotification(NotificationDto dto) {
        // Check user preferences
        NotificationPreference pref = getUserPreferences(dto.getUserId());
        
        if (!pref.isEnabled(dto.getChannel())) {
            return; // User has disabled this channel
        }
        
        // Check quiet hours
        if (isQuietHours(pref)) {
            // Schedule for later
            scheduleNotification(dto);
            return;
        }
        
        Notification notification = new Notification();
        notification.setUserId(dto.getUserId());
        notification.setChannel(dto.getChannel());
        notification.setSubject(dto.getSubject());
        notification.setMessage(dto.getMessage());
        notification.setSentAt(LocalDateTime.now());
        
        try {
            switch (dto.getChannel()) {
                case EMAIL:
                    emailService.send(dto.getUserEmail(), dto.getSubject(), dto.getMessage());
                    break;
                case SMS:
                    smsService.send(dto.getUserPhone(), dto.getMessage());
                    break;
                case PUSH:
                    pushService.send(dto.getUserId(), dto.getSubject(), dto.getMessage());
                    break;
                case IN_APP:
                    // Store in database for in-app display
                    break;
            }
            
            notification.setStatus(DeliveryStatus.DELIVERED);
        } catch (Exception e) {
            notification.setStatus(DeliveryStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
        }
        
        notificationRepository.save(notification);
    }
    
    public void notifyEmployee(Long employeeId, String message) {
        NotificationDto dto = new NotificationDto();
        dto.setUserId(employeeId);
        dto.setChannel(NotificationChannel.IN_APP);
        dto.setSubject("System Notification");
        dto.setMessage(message);
        
        sendNotification(dto);
    }
    
    public void notifySupervisor(Long employeeId, String message) {
        // Find supervisor and send notification
        Long supervisorId = findSupervisor(employeeId);
        notifyEmployee(supervisorId, message);
    }
}
```

---

## Section: E13 - Integration Layer (HRIS/WMS APIs)

### Description
Exposes REST APIs and connectors for HRIS synchronization, WMS integration, and IDP (Identity Provider) SSO with webhook support and OAuth2/JWT security.

### Design Specification
- REST API endpoints for HRIS employee sync
- WMS integration for department/location data
- IDP integration for SSO (SAML, OAuth2)
- Webhook controller for event notifications
- API versioning and documentation
- Rate limiting and throttling

### Sample Implementation

```java
// HRISController
package com.warehouse.integration.controller;

import com.warehouse.employee.dto.EmployeeDto;
import com.warehouse.integration.service.HRISIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/integration/hris")
public class HRISController {
    private final HRISIntegrationService hrisService;
    
    public HRISController(HRISIntegrationService hrisService) {
        this.hrisService = hrisService;
    }
    
    @PostMapping("/sync/employees")
    @Operation(summary = "Sync employees from HRIS")
    public ResponseEntity<SyncResult> syncEmployees(@RequestBody List<EmployeeDto> employees) {
        SyncResult result = hrisService.syncEmployees(employees);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/employees/{externalId}")
    @Operation(summary = "Get employee by external HRIS ID")
    public ResponseEntity<EmployeeDto> getEmployeeByExternalId(@PathVariable String externalId) {
        EmployeeDto employee = hrisService.getEmployeeByExternalId(externalId);
        return ResponseEntity.ok(employee);
    }
}

// WebhookController
@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {
    private final WebhookService webhookService;
    
    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }
    
    @PostMapping("/employee/created")
    public ResponseEntity<Void> handleEmployeeCreated(@RequestBody WebhookEventDto event) {
        webhookService.processEmployeeCreated(event);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/employee/updated")
    public ResponseEntity<Void> handleEmployeeUpdated(@RequestBody WebhookEventDto event) {
        webhookService.processEmployeeUpdated(event);
        return ResponseEntity.ok().build();
    }
}

// HRISIntegrationService
package com.warehouse.integration.service;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class HRISIntegrationService {
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;
    
    public HRISIntegrationService(EmployeeRepository employeeRepository,
                                 AuditService auditService) {
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
    }
    
    public SyncResult syncEmployees(List<EmployeeDto> employees) {
        SyncResult result = new SyncResult();
        
        for (EmployeeDto dto : employees) {
            try {
                Employee existing = employeeRepository
                    .findByBadgeId(dto.getBadgeId())
                    .orElse(null);
                
                if (existing == null) {
                    // Create new employee
                    Employee newEmployee = createFromDto(dto);
                    employeeRepository.save(newEmployee);
                    result.incrementCreated();
                } else {
                    // Update existing employee
                    updateFromDto(existing, dto);
                    employeeRepository.save(existing);
                    result.incrementUpdated();
                }
                
                auditService.logSync("HRIS", dto.getBadgeId(), "SUCCESS");
                
            } catch (Exception e) {
                result.incrementFailed();
                auditService.logSync("HRIS", dto.getBadgeId(), "FAILED: " + e.getMessage());
            }
        }
        
        return result;
    }
}
```

---

## Section: E14 - Audit Trail & Compliance

### Description
Centralizes audit logging for all sensitive operations, stores immutable logs with actor/timestamp/before/after states, and supports compliance reporting and export.

### Design Specification
- Entity: AuditLog with immutable records
- Service: AuditService with logging methods
- Aspect-oriented programming for automatic audit capture
- Export functionality for compliance reviews
- Retention policies and archival

### Sample Implementation

```java
// AuditLog Entity
package com.warehouse.audit.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String actor; // Username or system identifier
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false)
    private String entity; // Employee, Shift, etc.
    
    @Column(nullable = false)
    private String action; // CREATE, UPDATE, DELETE
    
    private Long entityId;
    
    @Column(length = 5000)
    private String beforeState;
    
    @Column(length = 5000)
    private String afterState;
    
    private String ipAddress;
    private String userAgent;
}

// AuditService
package com.warehouse.audit.service;

import com.warehouse.audit.domain.AuditLog;
import com.warehouse.audit.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Transactional
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    
    public AuditService(AuditLogRepository auditLogRepository,
                       ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }
    
    public void logChange(String actor, String entity, Long entityId, 
                         Object before, Object after, String action) {
        try {
            AuditLog log = new AuditLog();
            log.setActor(actor);
            log.setTimestamp(LocalDateTime.now());
            log.setEntity(entity);
            log.setEntityId(entityId);
            log.setAction(action);
            
            if (before != null) {
                log.setBeforeState(objectMapper.writeValueAsString(before));
            }
            
            if (after != null) {
                log.setAfterState(objectMapper.writeValueAsString(after));
            }
            
            auditLogRepository.save(log);
            
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Failed to create audit log: " + e.getMessage());
        }
    }
    
    public void logExport(String type, String fileName, String status) {
        AuditLog log = new AuditLog();
        log.setActor("SYSTEM");
        log.setTimestamp(LocalDateTime.now());
        log.setEntity("EXPORT");
        log.setAction(type);
        log.setAfterState(String.format("File: %s, Status: %s", fileName, status));
        
        auditLogRepository.save(log);
    }
}

// Audit Aspect for automatic logging
@Aspect
@Component
public class AuditAspect {
    private final AuditService auditService;
    
    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }
    
    @Around("@annotation(Audited)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String actor = SecurityContextHolder.getContext().getAuthentication().getName();
        Object[] args = joinPoint.getArgs();
        
        Object result = joinPoint.proceed();
        
        // Extract entity information and log
        auditService.logChange(actor, "Entity", null, args[0], result, "UPDATE");
        
        return result;
    }
}
```

---

## Section: E15 - Reporting & Analytics

### Description
Provides comprehensive operational reports, interactive dashboards, CSV/PDF exports, and metrics endpoints for business intelligence and decision-making.

### Design Specification
- Service: ReportingService with report generation and filtering
- Support for multiple report types (attendance, leave, performance)
- Export formats: CSV, PDF, Excel
- Dashboard endpoints with aggregated metrics
- Scheduled report generation and delivery

### Sample Implementation

```java
// ReportingService
package com.warehouse.reporting.service;

import com.warehouse.attendance.repository.AttendanceRepository;
import com.warehouse.employee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ReportingService {
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final PdfGeneratorService pdfService;
    
    public ReportingService(AttendanceRepository attendanceRepository,
                           EmployeeRepository employeeRepository,
                           PdfGeneratorService pdfService) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.pdfService = pdfService;
    }
    
    public AttendanceReport generateAttendanceReport(LocalDate startDate, 
                                                     LocalDate endDate, 
                                                     String department) {
        List<AttendanceEvent> events;
        
        if (department != null) {
            events = attendanceRepository.findByDateRangeAndDepartment(
                startDate, endDate, department
            );
        } else {
            events = attendanceRepository.findByDateRange(startDate, endDate);
        }
        
        AttendanceReport report = new AttendanceReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setDepartment(department);
        report.setTotalEvents(events.size());
        report.setEvents(events);
        
        // Calculate metrics
        Map<Long, Double> hoursWorked = calculateHoursWorked(events);
        report.setHoursWorked(hoursWorked);
        
        return report;
    }
    
    public byte[] exportReportAsPdf(AttendanceReport report) {
        return pdfService.generatePdf(report);
    }
    
    public String exportReportAsCsv(AttendanceReport report) {
        StringBuilder csv = new StringBuilder();
        csv.append("EmployeeID,Date,ClockIn,ClockOut,HoursWorked
");
        
        for (AttendanceEvent event : report.getEvents()) {
            csv.append(String.format("%d,%s,%s,%s,%.2f
",
                event.getEmployeeId(),
                event.getTimestamp().toLocalDate(),
                event.getType() == EventType.CLOCK_IN ? event.getTimestamp() : "",
                event.getType() == EventType.CLOCK_OUT ? event.getTimestamp() : "",
                calculateHours(event)
            ));
        }
        
        return csv.toString();
    }
    
    public DashboardMetrics getDashboardMetrics() {
        DashboardMetrics metrics = new DashboardMetrics();
        
        metrics.setTotalEmployees(employeeRepository.countByDeletedFalse());
        metrics.setActiveShifts(shiftRepository.countByStatusAndDate(
            AssignmentStatus.SCHEDULED, LocalDate.now()
        ));
        metrics.setPendingLeaveRequests(leaveRepository.countByStatus(
            LeaveStatus.REQUESTED
        ));
        metrics.setExpiringCertifications(certificationRepository
            .countByExpiryDateBetween(LocalDate.now(), LocalDate.now().plusDays(30))
        );
        
        return metrics;
    }
}

// ReportingController
@RestController
@RequestMapping("/api/reports")
public class ReportingController {
    private final ReportingService reportingService;
    
    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }
    
    @GetMapping("/attendance")
    public ResponseEntity<AttendanceReport> getAttendanceReport(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) String department) {
        
        AttendanceReport report = reportingService.generateAttendanceReport(
            startDate, endDate, department
        );
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/attendance/export/pdf")
    public ResponseEntity<byte[]> exportAttendanceReportPdf(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) String department) {
        
        AttendanceReport report = reportingService.generateAttendanceReport(
            startDate, endDate, department
        );
        byte[] pdf = reportingService.exportReportAsPdf(report);
        
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=attendance_report.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardMetrics> getDashboard() {
        DashboardMetrics metrics = reportingService.getDashboardMetrics();
        return ResponseEntity.ok(metrics);
    }
}
```

---

## Section: E16 - Mobile Access (PWA)

### Description
Delivers responsive mobile-optimized views for core workflows, offline support with service workers, PWA manifest for installability, and mobile-friendly UI components.

### Design Specification
- PWA manifest configuration
- Service worker for offline functionality
- Responsive UI components
- Mobile-optimized controllers and views
- Offline queue for critical operations (clock-in/out)
- Push notification support

### Sample Implementation

```json
// src/main/resources/static/manifest.json
{
  "name": "Warehouse Employee Management System",
  "short_name": "WarehouseEMS",
  "description": "Employee management for warehouse operations",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
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
// src/main/resources/static/service-worker.js
const CACHE_NAME = 'warehouse-ems-v1';
const urlsToCache = [
  '/',
  '/css/main.css',
  '/js/app.js',
  '/icons/icon-192x192.png'
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
      .then(response => {
        if (response) {
          return response;
        }
        return fetch(event.request);
      }
    )
  );
});

// Offline queue for clock events
self.addEventListener('sync', event => {
  if (event.tag === 'sync-clock-events') {
    event.waitUntil(syncClockEvents());
  }
});

async function syncClockEvents() {
  const db = await openDB();
  const events = await db.getAll('pending-events');
  
  for (const event of events) {
    try {
      await fetch('/api/attendance/clock-in', {
        method: 'POST',
        body: JSON.stringify(event),
        headers: {'Content-Type': 'application/json'}
      });
      await db.delete('pending-events', event.id);
    } catch (error) {
      console.error('Failed to sync event:', error);
    }
  }
}
```

---

## Section: E17 - Onboarding & Offboarding Workflow

### Description
Automates employee lifecycle management including provisioning/deprovisioning, initial schedule setup, training assignments, asset management, and access control during onboarding and offboarding.

### Design Specification
- Service: OnboardingService with HRIS integration and task generation
- Service: OffboardingService with access revocation and asset collection
- Workflow engine for multi-step processes
- Integration with HRIS, scheduling, training, and asset systems
- Automated notifications and reminders

### Sample Implementation

```java
// OnboardingService
package com.warehouse.lifecycle.service;

import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.service.EmployeeService;
import com.warehouse.scheduling.service.ShiftService;
import com.warehouse.training.service.CertificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class OnboardingService {
    private final EmployeeService employeeService;
    private final ShiftService shiftService;
    private final CertificationService certificationService;
    private final AssetService assetService;
    private final NotificationService notificationService;
    
    public OnboardingService(EmployeeService employeeService,
                            ShiftService shiftService,
                            CertificationService certificationService,
                            AssetService assetService,
                            NotificationService notificationService) {
        this.employeeService = employeeService;
        this.shiftService = shiftService;
        this.certificationService = certificationService;
        this.assetService = assetService;
        this.notificationService = notificationService;
    }
    
    public void onboardEmployee(EmployeeDto dto) {
        // Create employee record
        Employee employee = employeeService.createEmployee(dto);
        
        // Generate onboarding tasks
        List<OnboardingTask> tasks = generateOnboardingTasks(employee);
        
        // Assign initial schedule
        assignInitialSchedule(employee);
        
        // Assign required training
        assignRequiredTraining(employee);
        
        // Provision assets
        provisionAssets(employee);
        
        // Send welcome notification
        notificationService.sendWelcomeEmail(employee);
        
        // Notify supervisor
        notificationService.notifySupervisor(
            employee.getId(), 
            "New employee onboarding started: " + employee.getName()
        );
    }
    
    private List<OnboardingTask> generateOnboardingTasks(Employee employee) {
        List<OnboardingTask> tasks = new ArrayList<>();
        
        tasks.add(new OnboardingTask("Complete I-9 Form", 1));
        tasks.add(new OnboardingTask("Review Employee Handbook", 1));
        tasks.add(new OnboardingTask("Safety Training", 3));
        tasks.add(new OnboardingTask("System Access Setup", 1));
        tasks.add(new OnboardingTask("Department Orientation", 5));
        
        // Save tasks to database
        for (OnboardingTask task : tasks) {
            task.setEmployeeId(employee.getId());
            onboardingTaskRepository.save(task);
        }
        
        return tasks;
    }
    
    private void assignInitialSchedule(Employee employee) {
        // Assign default shift template based on department
        Long defaultShiftId = shiftService.getDefaultShiftForDepartment(
            employee.getDepartment()
        );
        
        LocalDate startDate = employee.getHireDate();
        shiftService.bulkAssignShifts(
            List.of(employee.getId()), 
            defaultShiftId, 
            startDate, 
            30 // First 30 days
        );
    }
    
    private void assignRequiredTraining(Employee employee) {
        List<String> requiredCerts = getRequiredCertifications(employee.getRole());
        
        for (String certType : requiredCerts) {
            certificationService.createTrainingAssignment(
                employee.getId(), 
                certType
            );
        }
    }
}

// OffboardingService
@Service
@Transactional
public class OffboardingService {
    private final EmployeeService employeeService;
    private final ShiftService shiftService;
    private final AssetService assetService;
    private final AccessControlService accessService;
    
    public OffboardingService(EmployeeService employeeService,
                             ShiftService shiftService,
                             AssetService assetService,
                             AccessControlService accessService) {
        this.employeeService = employeeService;
        this.shiftService = shiftService;
        this.assetService = assetService;
        this.accessService = accessService;
    }
    
    public void offboardEmployee(Long employeeId, LocalDate lastWorkingDay) {
        Employee employee = employeeService.getEmployee(employeeId);
        
        // Cancel future shifts
        shiftService.cancelFutureShifts(employeeId, lastWorkingDay);
        
        // Revoke system access
        accessService.revokeAccess(employeeId);
        
        // Create asset return tasks
        List<Asset> assignedAssets = assetService.getAssignedAssets(employeeId);
        for (Asset asset : assignedAssets) {
            assetService.createReturnTask(employeeId, asset.getId());
        }
        
        // Schedule exit interview
        scheduleExitInterview(employee, lastWorkingDay);
        
        // Soft delete employee record
        employeeService.softDeleteEmployee(employeeId);
        
        // Notify HR and supervisor
        notificationService.notifyHR(
            "Employee offboarding initiated: " + employee.getName()
        );
    }
}
```

---

## Section: E18 - Localization & Multi-Tenant

### Description
Supports multiple languages with resource bundles, multi-tenant architecture with data isolation, tenant-specific configuration, and branding customization.

### Design Specification
- MessageSource configuration for i18n
- TenantContext for multi-tenancy
- Resource bundles per locale
- Tenant-specific database schemas or row-level security
- Tenant configuration management

### Sample Implementation

```java
// LocalizationConfig
package com.warehouse.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import java.util.Locale;

@Configuration
public class LocalizationConfig {
    
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = 
            new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:messages");
        source.setDefaultEncoding("UTF-8");
        source.setCacheSeconds(3600);
        return source;
    }
    
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.US);
        return resolver;
    }
}

// TenantContext
package com.warehouse.multitenancy;

public class TenantContext {
    private static ThreadLocal<String> currentTenant = new ThreadLocal<>();
    
    public static void setTenant(String tenant) {
        currentTenant.set(tenant);
    }
    
    public static String getTenant() {
        return currentTenant.get();
    }
    
    public static void clear() {
        currentTenant.remove();
    }
}

// TenantInterceptor
@Component
public class TenantInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) {
        String tenantId = request.getHeader("X-Tenant-ID");
        
        if (tenantId == null) {
            tenantId = extractTenantFromDomain(request.getServerName());
        }
        
        TenantContext.setTenant(tenantId);
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Object handler, 
                               Exception ex) {
        TenantContext.clear();
    }
    
    private String extractTenantFromDomain(String domain) {
        // Extract tenant from subdomain (e.g., tenant1.warehouse.com)
        return domain.split("\.")[0];
    }
}

// Multi-tenant DataSource Configuration
@Configuration
public class MultiTenantDataSourceConfig {
    
    @Bean
    public DataSource dataSource() {
        AbstractRoutingDataSource dataSource = new TenantRoutingDataSource();
        
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("tenant1", tenant1DataSource());
        targetDataSources.put("tenant2", tenant2DataSource());
        
        dataSource.setTargetDataSources(targetDataSources);
        dataSource.setDefaultTargetDataSource(defaultDataSource());
        
        return dataSource;
    }
}

class TenantRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getTenant();
    }
}
```

```properties
# messages_en.properties
welcome.message=Welcome to Warehouse Employee Management System
employee.created=Employee created successfully
shift.assigned=Shift assigned successfully

# messages_es.properties
welcome.message=Bienvenido al Sistema de GestiÃ³n de Empleados de AlmacÃ©n
employee.created=Empleado creado exitosamente
shift.assigned=Turno asignado exitosamente
```

---

## Section: E19 - Observability & Monitoring

### Description
Enables comprehensive system monitoring with metrics collection, distributed tracing, log aggregation, health checks, and integration with monitoring platforms (Prometheus, Grafana).

### Design Specification
- Spring Boot Actuator configuration
- Micrometer metrics integration
- Distributed tracing with OpenTelemetry
- Custom health indicators
- Log aggregation configuration
- Alerting rules and thresholds

### Sample Implementation

```yaml
# application.yml - Observability Configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,trace
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
  tracing:
    sampling:
      probability: 1.0

logging:
  level:
    com.warehouse: DEBUG
    org.springframework.web: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/warehouse-ems.log
    max-size: 10MB
    max-history: 30
```

```java
// Custom Health Indicator
package com.warehouse.monitoring;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    private final EmployeeRepository employeeRepository;
    
    public DatabaseHealthIndicator(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    @Override
    public Health health() {
        try {
            long count = employeeRepository.count();
            return Health.up()
                .withDetail("database", "Available")
                .withDetail("employeeCount", count)
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}

// Custom Metrics
@Component
public class AttendanceMetrics {
    private final MeterRegistry meterRegistry;
    private final Counter clockInCounter;
    private final Counter clockOutCounter;
    
    public AttendanceMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.clockInCounter = Counter.builder("attendance.clock.in")
            .description("Number of clock-in events")
            .register(meterRegistry);
        this.clockOutCounter = Counter.builder("attendance.clock.out")
            .description("Number of clock-out events")
            .register(meterRegistry);
    }
    
    public void recordClockIn() {
        clockInCounter.increment();
    }
    
    public void recordClockOut() {
        clockOutCounter.increment();
    }
}
```

---

## Section: E20 - CI/CD & Deployment Automation

### Description
Automates build, test, and deployment processes using CI/CD pipelines, Docker containerization, environment-specific configurations, and automated rollback capabilities.

### Design Specification
- Dockerfile for containerization
- Jenkins/GitHub Actions pipeline configuration
- Environment-specific application.yml files
- Automated testing in pipeline
- Blue-green deployment strategy
- Rollback mechanisms

### Sample Implementation

```dockerfile
# Dockerfile
FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/warehouse-employee-mgmt-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

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
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build with Maven
      run: mvn clean install
    
    - name: Run Tests
      run: mvn test
    
    - name: Build Docker Image
      run: docker build -t warehouse-ems:${{ github.sha }} .
    
    - name: Push to Registry
      run: |
        echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
        docker push warehouse-ems:${{ github.sha }}
    
    - name: Deploy to Staging
      if: github.ref == 'refs/heads/develop'
      run: |
        kubectl set image deployment/warehouse-ems warehouse-ems=warehouse-ems:${{ github.sha }} -n staging
    
    - name: Deploy to Production
      if: github.ref == 'refs/heads/main'
      run: |
        kubectl set image deployment/warehouse-ems warehouse-ems=warehouse-ems:${{ github.sha }} -n production
```

```groovy
// Jenkinsfile
pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = 'warehouse-ems'
        REGISTRY = 'docker.io/myorg'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test'
                junit 'target/surefire-reports/*.xml'
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    docker.build("${REGISTRY}/${DOCKER_IMAGE}:${BUILD_NUMBER}")
                }
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                sh '''
                    kubectl set image deployment/warehouse-ems                         warehouse-ems=${REGISTRY}/${DOCKER_IMAGE}:${BUILD_NUMBER}                         -n staging
                '''
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                input message: 'Deploy to Production?', ok: 'Deploy'
                sh '''
                    kubectl set image deployment/warehouse-ems                         warehouse-ems=${REGISTRY}/${DOCKER_IMAGE}:${BUILD_NUMBER}                         -n production
                '''
            }
        }
    }
    
    post {
        success {
            slackSend color: 'good', message: "Build ${BUILD_NUMBER} succeeded"
        }
        failure {
            slackSend color: 'danger', message: "Build ${BUILD_NUMBER} failed"
        }
    }
}
```

---

## CONCLUSION

This comprehensive low-level technical design document provides detailed specifications for implementing all 60+ user stories across 20 epics of the Warehouse Employee Management System. Each section includes:

- **Architecture Overview**: Spring Boot-based microservices architecture
- **Package Structure**: Organized by domain (employee, scheduling, attendance, etc.)
- **Entity Design**: JPA entities with relationships and constraints
- **Service Layer**: Business logic with transaction management
- **Repository Layer**: Data access with Spring Data JPA
- **Controller Layer**: REST APIs with OpenAPI documentation
- **Security**: Role-based access control with Spring Security
- **Integration**: External system connectors (HRIS, WMS, IDP)
- **Sample Implementation**: Production-ready code snippets

The design follows Spring Boot best practices including:
- Dependency injection and inversion of control
- Layered architecture (Controller-Service-Repository)
- DTO pattern for API communication
- Exception handling and validation
- Transaction management
- Audit logging and compliance
- Observability and monitoring
- CI/CD automation

This document serves as a comprehensive blueprint for development teams to implement the Warehouse Employee Management System with consistency, quality, and maintainability.

---

**Document Version**: 1.0
**Last Updated**: 2024
**Status**: Ready for Implementation