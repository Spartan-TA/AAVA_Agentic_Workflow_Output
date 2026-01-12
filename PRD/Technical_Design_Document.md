# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

## Overview
This document provides comprehensive low-level technical design specifications for all 20 user stories (E01-E20) of the Warehouse Employee Management System built on Spring Boot framework.

---

## Section: E01 - Project Scaffolding & Domain Setup

**Description:** Establishes the foundational Spring Boot architecture, including Maven setup, base package structure, core modules, DB migration tooling, and health monitoring.

**Design Specification:**
- Maven project initialized with Spring Boot 3.x parent
- Base packages: com.warehouse.ems.{employee, scheduling, attendance, safety, config, common}
- Core modules: Employee, Scheduling, Attendance, Safety (each as a package with domain, service, repository, controller sub-packages)
- Flyway/Liquibase configured for DB migrations (src/main/resources/db/migration)
- Spring Boot Actuator enabled for health, metrics endpoints
- README with build/run instructions

**Sample Implementation:**
```xml
<!-- pom.xml -->
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
</dependencies>
```

```yaml
# application.yml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  flyway:
    enabled: true
    baseline-on-migrate: true
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

```java
// src/main/java/com/warehouse/ems/EmployeeApplication.java
package com.warehouse.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EmployeeApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmployeeApplication.class, args);
    }
}
```

---

## Section: E02 - Employee Master Data (CRUD)

**Description:** Implements Employee domain with full CRUD REST APIs, DTOs, unique badgeId, soft-delete, pagination/filtering, and OpenAPI documentation.

**Design Specification:**
- Entity: Employee (id, name, badgeId [unique], role, department, shiftGroup, hireDate, status, deleted)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService (CRUD, soft-delete, filtering)
- Controller: EmployeeController (REST endpoints, pagination, filtering)
- DTOs: EmployeeDto, EmployeeCreateDto, EmployeeUpdateDto
- OpenAPI annotations for schema/examples

**Sample Implementation:**
```java
package com.warehouse.ems.employee.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badgeId"))
public class Employee {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String badgeId;
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    private String department;
    private String shiftGroup;
    
    @Column(nullable = false)
    private LocalDate hireDate;
    
    @Enumerated(EnumType.STRING)
    private Status status;
    
    @Column(nullable = false)
    private boolean deleted = false;
    
    // Getters and setters
}

enum Role {
    ADMIN, HR, SUPERVISOR, WORKER
}

enum Status {
    ACTIVE, INACTIVE, ON_LEAVE
}
```

```java
package com.warehouse.ems.employee.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    
    @PostMapping
    public EmployeeDto create(@RequestBody EmployeeCreateDto dto) {
        return employeeService.create(dto);
    }
    
    @GetMapping
    public Page<EmployeeDto> list(
        @PageableDefault Pageable pageable, 
        @RequestParam Map<String,String> filters
    ) {
        return employeeService.findAll(pageable, filters);
    }
    
    @GetMapping("/{id}")
    public EmployeeDto getById(@PathVariable Long id) {
        return employeeService.findById(id);
    }
    
    @PutMapping("/{id}")
    public EmployeeDto update(
        @PathVariable Long id, 
        @RequestBody EmployeeUpdateDto dto
    ) {
        return employeeService.update(id, dto);
    }
    
    @PatchMapping("/{id}")
    public EmployeeDto patch(
        @PathVariable Long id, 
        @RequestBody Map<String,Object> updates
    ) {
        return employeeService.patch(id, updates);
    }
    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        employeeService.softDelete(id);
    }
}
```

---

## Section: E03 - Role Based Access Control (RBAC)

**Description:** Integrates Spring Security with roles (ADMIN, HR, SUPERVISOR, WORKER), endpoint/method security, row-level constraints, and API key/OAuth2 toggle.

**Design Specification:**
- SecurityConfig: configures HttpSecurity, method security, role mappings
- UserDetailsService: loads users/roles from DB
- RBAC: @PreAuthorize on service/controller methods
- API key/OAuth2 toggle via application.yml
- Row-level filtering in EmployeeService (e.g., SUPERVISOR sees only team)

**Sample Implementation:**
```java
package com.warehouse.ems.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/employees/**").hasAnyRole("ADMIN","HR","SUPERVISOR")
                .requestMatchers("/attendance/**").hasAnyRole("ADMIN","SUPERVISOR","WORKER")
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());
        return http.build();
    }
}
```

```java
package com.warehouse.ems.employee.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {
    
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and #employee.department == authentication.principal.department)")
    public Employee updateEmployee(Employee employee) {
        // Update logic
        return employeeRepository.save(employee);
    }
}
```

---

## Section: E04 - Time & Attendance (Clock In/Out)

**Description:** Provides endpoints for clock-in/out events, geofence/device capture, shift association, missed punch correction workflow, and CSV reporting.

**Design Specification:**
- Entity: AttendanceEvent (id, employeeId, type [IN/OUT], timestamp, deviceId, location, shiftId, correctionStatus)
- Repository: AttendanceRepository
- Service: AttendanceService (clockIn, clockOut, computeTotals, corrections)
- Controller: AttendanceController (POST /clock-in, /clock-out, /corrections, /report)
- Geofence/device validation logic
- CSV export utility

**Sample Implementation:**
```java
package com.warehouse.ems.attendance.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long employeeId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    private String deviceId;
    private String location;
    private Long shiftId;
    
    @Enumerated(EnumType.STRING)
    private CorrectionStatus correctionStatus;
    
    // Getters and setters
}

enum EventType {
    CLOCK_IN, CLOCK_OUT
}

enum CorrectionStatus {
    NONE, PENDING, APPROVED, REJECTED
}
```

```java
package com.warehouse.ems.attendance.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    
    private final AttendanceService attendanceService;
    
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }
    
    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockInDto dto) {
        return ResponseEntity.ok(attendanceService.clockIn(dto));
    }
    
    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@RequestBody ClockOutDto dto) {
        return ResponseEntity.ok(attendanceService.clockOut(dto));
    }
    
    @PostMapping("/corrections")
    public ResponseEntity<?> requestCorrection(@RequestBody CorrectionDto dto) {
        return ResponseEntity.ok(attendanceService.requestCorrection(dto));
    }
    
    @GetMapping("/report")
    public ResponseEntity<Resource> exportReport(
        @RequestParam String startDate,
        @RequestParam String endDate
    ) {
        return ResponseEntity.ok(attendanceService.exportReport(startDate, endDate));
    }
}
```

---

## Section: E05 - Shift & Schedule Management

**Description:** Manages shift templates, rotations, overtime rules, employee assignments, blackout dates, and operation calendars.

**Design Specification:**
- Entities: ShiftTemplate, ShiftAssignment, BlackoutDate, OperationCalendar
- Repositories: ShiftTemplateRepository, ShiftAssignmentRepository
- Service: ShiftService (CRUD, conflict detection, bulk assignment, audit)
- Controller: ShiftController (CRUD endpoints, bulk assign, personal shifts)
- Audit logging on assignments

**Sample Implementation:**
```java
package com.warehouse.ems.scheduling.domain;

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
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    private boolean overtimeEligible;
    private String rotationPattern;
    
    // Getters and setters
}

@Entity
@Table(name = "shift_assignments")
public class ShiftAssignment {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long employeeId;
    
    @Column(nullable = false)
    private Long shiftTemplateId;
    
    @Column(nullable = false)
    private LocalDate assignmentDate;
    
    // Getters and setters
}
```

```java
package com.warehouse.ems.scheduling.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/shifts")
public class ShiftController {
    
    private final ShiftService shiftService;
    
    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }
    
    @PostMapping("/templates")
    public ShiftTemplate createTemplate(@RequestBody ShiftTemplateDto dto) {
        return shiftService.createTemplate(dto);
    }
    
    @PostMapping("/assignments/bulk")
    public void bulkAssign(@RequestBody BulkAssignDto dto) {
        shiftService.bulkAssign(dto);
    }
    
    @GetMapping("/personal")
    public List<ShiftAssignment> getPersonalShifts(@AuthenticationPrincipal User user) {
        return shiftService.getPersonalShifts(user.getId());
    }
}
```

---

## Section: E06 - Leave & Absence Management

**Description:** Implements PTO/sick/unpaid leave requests, approval workflow, accrual balances, and integration hooks for scheduling/payroll.

**Design Specification:**
- Entities: LeaveRequest, LeaveBalance, LeavePolicy
- Repositories: LeaveRequestRepository, LeaveBalanceRepository
- Service: LeaveService (request, approve/deny, update balances, flag shifts)
- Controller: LeaveController (request, approve/deny, export)
- Integration hooks for scheduling/payroll modules

**Sample Implementation:**
```java
package com.warehouse.ems.leave.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long employeeId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType type;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status;
    
    private String reason;
    private Long approverId;
    
    // Getters and setters
}

enum LeaveType {
    PTO, SICK, UNPAID
}

enum LeaveStatus {
    PENDING, APPROVED, DENIED
}
```

```java
package com.warehouse.ems.leave.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/leave")
public class LeaveController {
    
    private final LeaveService leaveService;
    
    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }
    
    @PostMapping("/request")
    public LeaveRequestDto requestLeave(@RequestBody LeaveRequestDto dto) {
        return leaveService.requestLeave(dto);
    }
    
    @PostMapping("/approve")
    public void approveLeave(@RequestBody ApproveDto dto) {
        leaveService.approveLeave(dto);
    }
    
    @GetMapping("/export")
    public ResponseEntity<Resource> exportApprovedLeaves(
        @RequestParam String startDate,
        @RequestParam String endDate
    ) {
        return ResponseEntity.ok(leaveService.exportApprovedLeaves(startDate, endDate));
    }
}
```

---

## Section: E07 - Training & Certification Tracking

**Description:** Tracks employee certifications, expirations, renewals, blocks assignments for expired certs, and supports proof document uploads.

**Design Specification:**
- Entities: Certification, CertificationDocument
- Repositories: CertificationRepository, CertificationDocumentRepository
- Service: CertificationService (CRUD, expiry alerts, assignment checks)
- Controller: CertificationController (CRUD, upload, status)
- Scheduling integration for assignment blocking

**Sample Implementation:**
```java
package com.warehouse.ems.training.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "certifications")
public class Certification {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long employeeId;
    
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false)
    private LocalDate issueDate;
    
    @Column(nullable = false)
    private LocalDate expiryDate;
    
    @Column(nullable = false)
    private boolean valid;
    
    // Getters and setters
}
```

```java
package com.warehouse.ems.training.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/certifications")
public class CertificationController {
    
    private final CertificationService certificationService;
    
    public CertificationController(CertificationService certificationService) {
        this.certificationService = certificationService;
    }
    
    @PostMapping
    public CertificationDto create(@RequestBody CertificationDto dto) {
        return certificationService.create(dto);
    }
    
    @GetMapping("/alerts")
    public List<CertificationAlertDto> getAlerts(
        @RequestParam(required = false) Integer daysBeforeExpiry
    ) {
        return certificationService.getExpiryAlerts(daysBeforeExpiry);
    }
    
    @PostMapping("/{id}/documents")
    public void uploadProof(
        @PathVariable Long id, 
        @RequestParam MultipartFile file
    ) {
        certificationService.uploadProofDocument(id, file);
    }
}
```

---

## Section: E08 - Safety Incidents & OSHA Reporting

**Description:** Records safety incidents/near-misses, manages investigation workflow, and generates OSHA-compliant reports and dashboards.

**Design Specification:**
- Entities: SafetyIncident, IncidentStatus, CorrectiveAction
- Repositories: SafetyIncidentRepository, CorrectiveActionRepository
- Service: SafetyService (record, workflow, export, metrics)
- Controller: SafetyController (POST /incidents, status workflow, export, dashboard)

**Sample Implementation:**
```java
package com.warehouse.ems.safety.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "safety_incidents")
public class SafetyIncident {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String severity;
    
    @Column(nullable = false)
    private String location;
    
    @Column(nullable = false, length = 2000)
    private String description;
    
    @ElementCollection
    private List<Long> involvedEmployeeIds;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;
    
    @Column(nullable = false)
    private LocalDateTime incidentDate;
    
    // Getters and setters
}

enum IncidentStatus {
    OPEN, INVESTIGATING, RESOLVED
}
```

```java
package com.warehouse.ems.safety.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/safety")
public class SafetyController {
    
    private final SafetyService safetyService;
    
    public SafetyController(SafetyService safetyService) {
        this.safetyService = safetyService;
    }
    
    @PostMapping("/incidents")
    public SafetyIncidentDto recordIncident(@RequestBody SafetyIncidentDto dto) {
        return safetyService.recordIncident(dto);
    }
    
    @PostMapping("/incidents/{id}/status")
    public void updateStatus(
        @PathVariable Long id, 
        @RequestBody StatusDto dto
    ) {
        safetyService.updateStatus(id, dto);
    }
    
    @GetMapping("/osha/export")
    public ResponseEntity<Resource> exportOSHA(
        @RequestParam String year
    ) {
        return ResponseEntity.ok(safetyService.exportOSHA(year));
    }
    
    @GetMapping("/dashboard")
    public SafetyMetricsDto getMetrics(
        @RequestParam(required = false) String period
    ) {
        return safetyService.getMetrics(period);
    }
}
```

---

## Section: E09 - Equipment & Asset Assignment

**Description:** Assigns assets to employees, tracks check-in/out, blocks use if certs missing, maintains asset condition, and logs history.

**Design Specification:**
- Entities: Asset, AssetAssignment, AssetCondition, AssetHistory
- Repositories: AssetRepository, AssetAssignmentRepository
- Service: AssetService (CRUD, check-in/out, cert validation, history)
- Controller: AssetController (CRUD, check-in/out, overdue reports)

**Sample Implementation:**
```java
package com.warehouse.ems.asset.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assets")
public class Asset {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false, unique = true)
    private String serialNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetCondition condition;
    
    private String requiredCertification;
    
    // Getters and setters
}

@Entity
@Table(name = "asset_assignments")
public class AssetAssignment {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long assetId;
    
    @Column(nullable = false)
    private Long employeeId;
    
    @Column(nullable = false)
    private LocalDateTime checkoutTime;
    
    private LocalDateTime returnTime;
    
    // Getters and setters
}

enum AssetCondition {
    EXCELLENT, GOOD, FAIR, POOR, OUT_OF_SERVICE
}
```

```java
package com.warehouse.ems.asset.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/assets")
public class AssetController {
    
    private final AssetService assetService;
    
    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }
    
    @PostMapping("/assign")
    public void assignAsset(@RequestBody AssetAssignmentDto dto) {
        assetService.assignAsset(dto);
    }
    
    @PostMapping("/return")
    public void returnAsset(@RequestBody AssetAssignmentDto dto) {
        assetService.returnAsset(dto);
    }
    
    @GetMapping("/overdue")
    public List<AssetAssignmentDto> getOverdueAssets(
        @RequestParam(required = false) Integer daysOverdue
    ) {
        return assetService.getOverdueAssets(daysOverdue);
    }
}
```

---

## Section: E10 - Performance Reviews & Goals

**Description:** Manages review templates, goals, competencies, ratings, comments, and supervisor/employee acknowledgements with immutable history.

**Design Specification:**
- Entities: PerformanceReview, ReviewCycle, Goal, Competency, ReviewAcknowledgement
- Repositories: PerformanceReviewRepository, ReviewCycleRepository
- Service: ReviewService (create cycles, assign, submit, acknowledge, export)
- Controller: ReviewController (CRUD, workflow, PDF export)

**Sample Implementation:**
```java
package com.warehouse.ems.performance.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long employeeId;
    
    @Column(nullable = false)
    private Long cycleId;
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<Goal> goals;
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<Competency> competencies;
    
    @Column(length = 5000)
    private String comments;
    
    @Column(nullable = false)
    private boolean acknowledged = false;
    
    private LocalDateTime signOffDate;
    
    // Getters and setters
}
```

```java
package com.warehouse.ems.performance.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    
    private final ReviewService reviewService;
    
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }
    
    @PostMapping("/cycles")
    public ReviewCycleDto createCycle(@RequestBody ReviewCycleDto dto) {
        return reviewService.createCycle(dto);
    }
    
    @PostMapping("/submit")
    public void submitReview(@RequestBody PerformanceReviewDto dto) {
        reviewService.submitReview(dto);
    }
    
    @PostMapping("/acknowledge")
    public void acknowledgeReview(@RequestBody AcknowledgeDto dto) {
        reviewService.acknowledgeReview(dto);
    }
    
    @GetMapping("/export/pdf")
    public ResponseEntity<Resource> exportPdf(
        @RequestParam Long reviewId
    ) {
        return ResponseEntity.ok(reviewService.exportPdf(reviewId));
    }
}
```

---

## Section: E11 - Payroll Export Integration

**Description:** Generates payroll-ready files from attendance/leave, maps to provider formats, delivers securely, retries failures, and audits exports.

**Design Specification:**
- Service: PayrollExportService (generate, map, deliver, retry, audit)
- Integration: SFTP/API client for delivery
- Audit: PayrollExportLog entity/repository
- Controller: PayrollController (export endpoint)

**Sample Implementation:**
```java
package com.warehouse.ems.payroll.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class PayrollExportService {
    
    public Resource generatePayrollFile(LocalDate period) {
        // Generate payroll file from attendance and leave data
        return null;
    }
    
    public void deliverPayroll(Resource file) {
        // Deliver via SFTP or API
    }
    
    public void retryFailedExports() {
        // Retry logic with exponential backoff
    }
}
```

```java
package com.warehouse.ems.payroll.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_export_logs")
public class PayrollExportLog {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDate exportDate;
    
    @Column(nullable = false)
    private String status;
    
    @Column(nullable = false)
    private String provider;
    
    private String filePath;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // Getters and setters
}
```

```java
package com.warehouse.ems.payroll.controller;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/payroll")
public class PayrollController {
    
    private final PayrollExportService payrollExportService;
    
    public PayrollController(PayrollExportService payrollExportService) {
        this.payrollExportService = payrollExportService;
    }
    
    @PostMapping("/export")
    public void exportPayroll(@RequestParam LocalDate period) {
        payrollExportService.generatePayrollFile(period);
        payrollExportService.deliverPayroll(null);
    }
}
```

---

## Section: E12 - Notifications & Announcements

**Description:** Delivers in-app/email/SMS notifications for events, supports opt-in/out, localized templates, delivery tracking, rate limits, and dashboard announcements.

**Design Specification:**
- Entities: Notification, Announcement, NotificationPreference
- Service: NotificationService (send, track, rate limit, localize)
- Integration: Email/SMS providers
- Controller: NotificationController (opt-in/out, dashboard)

**Sample Implementation:**
```java
package com.warehouse.ems.notification.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String channel;
    
    @Column(nullable = false)
    private String templateKey;
    
    @Column(nullable = false)
    private String locale;
    
    @Column(nullable = false)
    private String status;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // Getters and setters
}

@Entity
@Table(name = "announcements")
public class Announcement {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 2000)
    private String message;
    
    @Column(nullable = false)
    private LocalDateTime publishDate;
    
    @Column(nullable = false)
    private String locale;
    
    // Getters and setters
}
```

```java
package com.warehouse.ems.notification.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    @PostMapping("/preferences")
    public void setPreferences(@RequestBody NotificationPreferenceDto dto) {
        notificationService.setPreferences(dto);
    }
    
    @GetMapping("/dashboard")
    public List<AnnouncementDto> getAnnouncements(
        @RequestParam(required = false) String locale
    ) {
        return notificationService.getAnnouncements(locale);
    }
}
```

---

## Section: E13 - Integration Layer (HRIS/WMS APIs)

**Description:** Exposes REST APIs/connectors for HRIS/WMS/IDP, supports webhooks, JWT/OAuth2 security, idempotency, and OpenAPI documentation.

**Design Specification:**
- API: /api/hris, /api/wms, /api/idp endpoints
- Security: JWT/OAuth2
- Service: IntegrationService (sync, webhook, idempotency)
- OpenAPI annotations

**Sample Implementation:**
```java
package com.warehouse.ems.integration.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/hris")
@SecurityRequirement(name = "bearerAuth")
public class HRISController {
    
    private final IntegrationService integrationService;
    
    public HRISController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }
    
    @PostMapping("/sync")
    public void syncEmployees(@RequestBody List<EmployeeDto> dtos) {
        integrationService.syncEmployees(dtos);
    }
    
    @PostMapping("/webhook")
    public void handleWebhook(@RequestBody WebhookDto dto) {
        integrationService.handleWebhook(dto);
    }
}

@RestController
@RequestMapping("/api/wms")
@SecurityRequirement(name = "bearerAuth")
public class WMSController {
    // WMS integration endpoints
}

@RestController
@RequestMapping("/api/idp")
@SecurityRequirement(name = "bearerAuth")
public class IDPController {
    // IDP integration endpoints
}
```

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth.example.com
```

---

## Section: E14 - Audit Trail & Compliance

**Description:** Centralizes audit logging for sensitive changes, stores immutable logs, supports export, and validates coverage via tests.

**Design Specification:**
- Entity: AuditLog (id, actor, timestamp, entity, before, after, action)
- Service: AuditService (log, export)
- Repository: AuditLogRepository
- Controller: AuditController (export endpoint)

**Sample Implementation:**
```java
package com.warehouse.ems.audit.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String actor;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false)
    private String entity;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String before;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String after;
    
    @Column(nullable = false)
    private String action;
    
    // Getters and setters
}
```

```java
package com.warehouse.ems.audit.service;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class AuditService {
    
    public void logChange(
        String actor, 
        String entity, 
        Object before, 
        Object after, 
        String action
    ) {
        // Log the change
    }
    
    public List<AuditLog> exportLogs(
        LocalDate from, 
        LocalDate to, 
        String entity
    ) {
        // Export audit logs
        return null;
    }
}
```

---

## Section: E15 - Reporting & Analytics

**Description:** Provides operational reports (attendance, overtime, leave, certs, safety KPIs), CSV/PDF export, dashboards, and metrics endpoints.

**Design Specification:**
- Service: ReportingService (generate, filter, export)
- Controller: ReportingController (report endpoints, export)
- Metrics: /metrics endpoints for BI

**Sample Implementation:**
```java
package com.warehouse.ems.reporting.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportingController {
    
    private final ReportingService reportingService;
    
    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }
    
    @GetMapping("/attendance")
    public List<AttendanceReportDto> getAttendanceReport(
        @RequestParam String startDate,
        @RequestParam String endDate,
        @RequestParam(required = false) String department
    ) {
        return reportingService.getAttendanceReport(startDate, endDate, department);
    }
    
    @GetMapping("/export/csv")
    public ResponseEntity<Resource> exportCsv(
        @RequestParam String reportType,
        @RequestParam String startDate,
        @RequestParam String endDate
    ) {
        return ResponseEntity.ok(reportingService.exportCsv(reportType, startDate, endDate));
    }
    
    @GetMapping("/metrics")
    public MetricsDto getMetrics(
        @RequestParam(required = false) String period
    ) {
        return reportingService.getMetrics(period);
    }
}
```

---

## Section: E16 - Mobile Access (PWA)

**Description:** Enables responsive mobile views for core flows, installable PWA manifest, offline queue for clock events, and conflict resolution.

**Design Specification:**
- Frontend: PWA manifest, service worker, responsive UI (Thymeleaf/React)
- Backend: Offline event queue API, conflict resolution logic
- Controller: MobileController (clock-in/out, schedules, leave, announcements)

**Sample Implementation:**
```json
// src/main/resources/static/manifest.json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
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
```

```java
package com.warehouse.ems.mobile.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/mobile")
public class MobileController {
    
    private final MobileService mobileService;
    
    public MobileController(MobileService mobileService) {
        this.mobileService = mobileService;
    }
    
    @PostMapping("/clock-events")
    public void queueClockEvent(@RequestBody ClockEventDto dto) {
        mobileService.queueClockEvent(dto);
    }
    
    @GetMapping("/conflicts")
    public List<ConflictDto> getConflicts(
        @RequestParam Long employeeId
    ) {
        return mobileService.getConflicts(employeeId);
    }
}
```

---

## Section: E17 - Onboarding & Offboarding Workflow

**Description:** Automates account provisioning, initial schedule, training tasks, asset assignment, and deprovisioning on termination.

**Design Specification:**
- Service: OnboardingService (provision, assign tasks/assets, schedule)
- Service: OffboardingService (revoke access, collect assets, update schedules)
- Controller: LifecycleController (onboard/offboard endpoints)

**Sample Implementation:**
```java
package com.warehouse.ems.lifecycle.service;

import org.springframework.stereotype.Service;

@Service
public class OnboardingService {
    
    public void onboardEmployee(EmployeeDto dto) {
        // Create account
        // Assign initial schedule
        // Create training tasks
        // Assign assets
    }
}

@Service
public class OffboardingService {
    
    public void offboardEmployee(Long employeeId) {
        // Revoke access
        // Collect assets
        // Update schedules
        // Archive data
    }
}
```

```java
package com.warehouse.ems.lifecycle.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lifecycle")
public class LifecycleController {
    
    private final OnboardingService onboardingService;
    private final OffboardingService offboardingService;
    
    public LifecycleController(
        OnboardingService onboardingService,
        OffboardingService offboardingService
    ) {
        this.onboardingService = onboardingService;
        this.offboardingService = offboardingService;
    }
    
    @PostMapping("/onboard")
    public void onboard(@RequestBody EmployeeDto dto) {
        onboardingService.onboardEmployee(dto);
    }
    
    @PostMapping("/offboard")
    public void offboard(@RequestParam Long employeeId) {
        offboardingService.offboardEmployee(employeeId);
    }
}
```

---

## Section: E18 - Localization & Multi-Tenant

**Description:** Supports multiple tenants (warehouses) with isolated data, UI localization (en, es), and timezone-aware scheduling.

**Design Specification:**
- Entity: Tenant (id, name, locale, timezone)
- All queries filter by tenantId
- Locale switcher in UI
- Dates/times stored in UTC, displayed in user TZ
- Translations via messages_{locale}.properties

**Sample Implementation:**
```java
package com.warehouse.ems.tenant.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "tenants")
public class Tenant {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private String locale;
    
    @Column(nullable = false)
    private String timezone;
    
    // Getters and setters
}
```

```java
// Example repository method with tenant filtering
package com.warehouse.ems.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    @Query("SELECT e FROM Employee e WHERE e.tenantId = :tenantId")
    List<Employee> findByTenantId(Long tenantId);
}
```

```properties
# src/main/resources/messages_es.properties
employee.name=Nombre del empleado
employee.badgeId=ID de placa
employee.department=Departamento
```

---

## Section: E19 - Observability & Monitoring

**Description:** Implements structured JSON logging, Micrometer/Prometheus metrics, distributed tracing (Zipkin/Jaeger), and alerting for failures.

**Design Specification:**
- Logging: Logback config for JSON logs, traceId
- Metrics: Micrometer/Prometheus integration
- Tracing: Spring Cloud Sleuth, Zipkin/Jaeger config
- Health checks: DB, external APIs
- Alerting: Grafana dashboards, error/latency alerts

**Sample Implementation:**
```xml
<!-- src/main/resources/logback-spring.xml -->
<configuration>
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <loggerName/>
                <message/>
                <mdc/>
                <stackTrace/>
            </providers>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="JSON"/>
    </root>
</configuration>
```

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
  health:
    db:
      enabled: true
spring:
  zipkin:
    enabled: true
    base-url: http://zipkin:9411
  sleuth:
    sampler:
      probability: 1.0
```

---

## Section: E20 - Deployment & CI/CD

**Description:** Dockerizes the app, provides Kubernetes manifests, CI pipeline for build/test/scan/deploy, blue-green/canary strategy, and automated rollback.

**Design Specification:**
- Dockerfile for Spring Boot app
- K8s manifests: deployment, service, ingress, configmap, secret
- CI pipeline: build, test, scan, deploy, smoke test, rollback
- Secrets managed via K8s secrets/external vault

**Sample Implementation:**
```dockerfile
# Dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/warehouse-ems.jar /app/warehouse-ems.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/warehouse-ems.jar"]
```

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: warehouse-ems
  namespace: production
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
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
        envFrom:
        - secretRef:
            name: warehouse-ems-secrets
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
```

```yaml
# .github/workflows/ci.yml
name: CI/CD Pipeline

on:
  push:
    branches: [ main ]
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
        run: mvn clean package
      
      - name: Run tests
        run: mvn test
      
      - name: Build Docker image
        run: docker build -t warehouse-ems:${{ github.sha }} .
      
      - name: Scan Docker image
        run: docker scan warehouse-ems:${{ github.sha }}
      
      - name: Deploy to Kubernetes
        run: |
          kubectl apply -f k8s/
          kubectl set image deployment/warehouse-ems warehouse-ems=warehouse-ems:${{ github.sha }}
      
      - name: Run smoke tests
        run: ./smoke-test.sh
      
      - name: Rollback on failure
        if: failure()
        run: kubectl rollout undo deployment/warehouse-ems
```

---

## Conclusion

This comprehensive low-level technical design document covers all 20 user stories (E01-E20) for the Warehouse Employee Management System. Each section provides:

1. **Detailed descriptions** of the technical requirements
2. **Design specifications** including entities, services, repositories, and controllers
3. **Sample implementations** with code snippets demonstrating Spring Boot best practices
4. **Integration points** and dependencies between modules
5. **Security considerations** and compliance requirements
6. **Deployment and operational** aspects

The design follows Spring Boot 3.x conventions, uses modern Java features, implements proper separation of concerns, and ensures scalability, maintainability, and security throughout the system.

**Key Architectural Principles:**
- Layered architecture (Controller â Service â Repository â Entity)
- RESTful API design with proper HTTP methods and status codes
- Spring Security for authentication and authorization
- JPA/Hibernate for data persistence
- Flyway/Liquibase for database migrations
- Actuator for monitoring and health checks
- Docker and Kubernetes for containerization and orchestration
- CI/CD pipeline for automated testing and deployment

**Technology Stack:**
- Spring Boot 3.2.0
- Java 17
- PostgreSQL
- Maven
- Docker
- Kubernetes
- Prometheus/Grafana
- Zipkin/Jaeger

This document serves as the foundation for implementation and should be referenced throughout the development lifecycle.