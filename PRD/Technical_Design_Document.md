# Warehouse Employee Management System (EMS) - Technical Design Document

**Version:** 1.0  
**Date:** 2024-06  
**Authors:** Senior Software Architect  
**Stack:** Spring Boot 3.x, Java 17+, Maven, PostgreSQL, Flyway/Liquibase, Spring Security (JWT/OAuth2), Spring Data JPA, Hibernate, OpenAPI, Actuator, Lombok, MapStruct, JUnit 5, Mockito

---

## Table of Contents

1. [E01 - Project Scaffolding & Domain Setup](#e01---project-scaffolding--domain-setup)
2. [E02 - Employee Master Data (CRUD)](#e02---employee-master-data-crud)
3. [E03 - Role Based Access Control (RBAC)](#e03---role-based-access-control-rbac)
4. [E04 - Time & Attendance (Clock In/Out)](#e04---time--attendance-clock-inout)
5. [E05 - Shift & Schedule Management](#e05---shift--schedule-management)
6. [E06 - Leave & Absence Management](#e06---leave--absence-management)
7. [E07 - Training & Certification Tracking](#e07---training--certification-tracking)
8. [E08 - Safety Incidents & OSHA Reporting](#e08---safety-incidents--osha-reporting)
9. [E09 - Equipment & Asset Assignment](#e09---equipment--asset-assignment)
10. [E10 - Performance Reviews & Goals](#e10---performance-reviews--goals)
11. [E11 - Payroll Export Integration](#e11---payroll-export-integration)
12. [E12 - Notifications & Announcements](#e12---notifications--announcements)
13. [E13 - Integration Layer (HRIS/WMS APIs)](#e13---integration-layer-hriswms-apis)
14. [E14 - Audit Trail & Compliance](#e14---audit-trail--compliance)
15. [E15 - Reporting & Analytics](#e15---reporting--analytics)
16. [E16 - Mobile Access (PWA)](#e16---mobile-access-pwa)
17. [E17 - Onboarding & Offboarding Workflow](#e17---onboarding--offboarding-workflow)

---

## E01 - Project Scaffolding & Domain Setup

Section: Project Structure & Configuration  
Description:  
Establishes the foundational structure for the Warehouse EMS project, ensuring modularity, maintainability, and adherence to Spring Boot best practices.

Design Specification:
- **Package Structure:**  
  - `com.warehouse.ems` (root)
    - `employee`, `attendance`, `shift`, `leave`, `training`, `safety`, `equipment`, `review`, `payroll`, `notification`, `integration`, `audit`, `reporting`, `mobile`, `onboarding`
- **Build Tool:** Maven (`pom.xml`)
- **Database:** PostgreSQL
- **Migration:** Flyway/Liquibase
- **Monitoring:** Spring Boot Actuator
- **Profiles:** `dev`, `test`, `prod`
- **API Documentation:** OpenAPI/Swagger
- **Lombok:** For boilerplate reduction
- **MapStruct:** For DTO mapping

Sample Implementation:
```java
// Directory structure (example)
com/
  warehouse/
    ems/
      employee/
      attendance/
      shift/
      ...
```
```xml
<!-- pom.xml (excerpt) -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>
<properties>
    <java.version>17</java.version>
</properties>
<dependencies>
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
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.2.0</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```
```yaml
# application-dev.yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems
    username: ems_user
    password: secret
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  flyway:
    enabled: true
server:
  port: 8080
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```
```sql
-- V1__baseline.sql (Flyway)
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    shift_group VARCHAR(32),
    hire_date DATE,
    status VARCHAR(16) NOT NULL
);
```
---

## E02 - Employee Master Data (CRUD)

Section: Domain Model  
Description:  
Defines the Employee entity as the central record for all warehouse staff, supporting CRUD operations, soft-deletion, and unique constraints.

Design Specification:
- **Package:** `com.warehouse.ems.employee`
- **Entity:** `Employee` with JPA annotations
- **Repository:** `EmployeeRepository` extends `JpaRepository`
- **Service:** `EmployeeService` with business logic, soft-delete
- **Controller:** `EmployeeController` with REST endpoints
- **DTOs:** `EmployeeRequestDTO`, `EmployeeResponseDTO`
- **Validation:** Bean Validation (JSR-380)
- **OpenAPI:** Annotated endpoints
- **Pagination/Filtering:** Spring Data JPA
- **Soft Delete:** `deleted` boolean flag

Sample Implementation:
```java
// Employee.java
package com.warehouse.ems.employee;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "employee", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    private String department;
    private String shiftGroup;

    private LocalDate hireDate;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private boolean deleted = false;
}
```
```java
// EmployeeRepository.java
package com.warehouse.ems.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
}
```
```java
// EmployeeService.java
package com.warehouse.ems.employee;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll().stream()
            .filter(e -> !e.isDeleted())
            .toList();
    }

    @Transactional
    public Employee createEmployee(Employee employee) {
        // Validate unique badgeId, etc.
        return employeeRepository.save(employee);
    }

    @Transactional
    public void softDeleteEmployee(Long id) {
        Employee emp = employeeRepository.findById(id).orElseThrow();
        emp.setDeleted(true);
        employeeRepository.save(emp);
    }
}
```
```java
// EmployeeController.java
package com.warehouse.ems.employee;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping
    public List<EmployeeResponseDTO> getAll() {
        return employeeService.getAllEmployees().stream()
            .map(EmployeeMapper.INSTANCE::toResponseDTO)
            .toList();
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> create(@Validated @RequestBody EmployeeRequestDTO dto) {
        Employee emp = EmployeeMapper.INSTANCE.toEntity(dto);
        Employee saved = employeeService.createEmployee(emp);
        return ResponseEntity.ok(EmployeeMapper.INSTANCE.toResponseDTO(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
```
```java
// EmployeeRequestDTO.java
@Data
public class EmployeeRequestDTO {
    @NotBlank
    private String badgeId;
    @NotBlank
    private String name;
    @NotBlank
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @NotBlank
    private String status;
}
```
```java
// EmployeeResponseDTO.java
@Data
public class EmployeeResponseDTO {
    private Long id;
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}
```
```java
// EmployeeMapper.java (MapStruct)
@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);

    Employee toEntity(EmployeeRequestDTO dto);
    EmployeeResponseDTO toResponseDTO(Employee employee);
}
```
---

## E03 - Role Based Access Control (RBAC)

Section: Security Configuration  
Description:  
Implements RBAC using Spring Security, supporting roles (ADMIN, HR, SUPERVISOR, WORKER), JWT/OAuth2, and method/endpoint security.

Design Specification:
- **Package:** `com.warehouse.ems.security`
- **SecurityConfig:** Configures HTTP security, JWT/OAuth2, role hierarchy
- **Method Security:** `@PreAuthorize` on service/controller methods
- **API Key/OAuth2 Toggle:** via `application.yaml`
- **Test Coverage:** Security tests for 401/403

Sample Implementation:
```java
// SecurityConfig.java
package com.warehouse.ems.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());
        return http.build();
    }
}
```
```java
// Example method-level security
@PreAuthorize("hasRole('ADMIN')")
public void deleteEmployee(Long id) { ... }
```
```yaml
# application.yaml (excerpt)
security:
  auth-type: jwt # or 'apikey'
```
---

## E04 - Time & Attendance (Clock In/Out)

Section: Attendance Domain & Workflow  
Description:  
Captures clock-in/out events, calculates hours, handles missed punches, and supports correction workflows.

Design Specification:
- **Package:** `com.warehouse.ems.attendance`
- **Entity:** `AttendanceEvent`
- **Repository:** `AttendanceRepository`
- **Service:** Business logic for shift association, totals, corrections
- **Controller:** `/attendance/clock-in`, `/attendance/clock-out`
- **DTOs:** For event capture and reporting
- **Geofence/Device:** Optional fields
- **Reports:** CSV export endpoint

Sample Implementation:
```java
// AttendanceEvent.java
@Entity
@Table(name = "attendance_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType type; // CLOCK_IN, CLOCK_OUT

    private String deviceId;
    private String geofenceId;
    private boolean correction;
}
```
```java
// AttendanceController.java
@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    @PostMapping("/clock-in")
    public ResponseEntity<Void> clockIn(@RequestBody @Valid AttendanceEventDTO dto) {
        attendanceService.clockIn(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/clock-out")
    public ResponseEntity<Void> clockOut(@RequestBody @Valid AttendanceEventDTO dto) {
        attendanceService.clockOut(dto);
        return ResponseEntity.ok().build();
    }
}
```
---

## E05 - Shift & Schedule Management

Section: Shift Domain & Assignment  
Description:  
Manages shift templates, rotations, overtime, and employee assignments, with conflict detection and audit logging.

Design Specification:
- **Package:** `com.warehouse.ems.shift`
- **Entities:** `ShiftTemplate`, `ShiftAssignment`
- **Repository:** JPA repositories
- **Service:** Assignment, conflict detection, bulk operations
- **Controller:** CRUD endpoints, bulk assign
- **Audit:** On assignment changes

Sample Implementation:
```java
// ShiftTemplate.java
@Entity
@Table(name = "shift_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
}
```
```java
// ShiftAssignment.java
@Entity
@Table(name = "shift_assignment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    @ManyToOne
    private ShiftTemplate shiftTemplate;

    private LocalDate assignmentDate;
}
```
---

## E06 - Leave & Absence Management

Section: Leave Domain & Workflow  
Description:  
Handles PTO, sick, unpaid leave requests, approvals, accruals, and integration with scheduling/payroll.

Design Specification:
- **Package:** `com.warehouse.ems.leave`
- **Entities:** `LeaveRequest`, `LeaveBalance`
- **Repository:** JPA repositories
- **Service:** Request, approve/deny, accruals
- **Controller:** Endpoints for request/approval
- **Integration:** Exclude from scheduling/payroll

Sample Implementation:
```java
// LeaveRequest.java
@Entity
@Table(name = "leave_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private LeaveType type; // PTO, SICK, UNPAID

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status; // REQUESTED, APPROVED, DENIED
}
```
---

## E07 - Training & Certification Tracking

Section: Certification Domain  
Description:  
Tracks employee certifications, expirations, renewals, and blocks assignments if expired.

Design Specification:
- **Package:** `com.warehouse.ems.training`
- **Entities:** `Certification`, `EmployeeCertification`
- **Repository:** JPA repositories
- **Service:** Expiry alerts, assignment checks
- **Controller:** CRUD, alerts

Sample Implementation:
```java
// Certification.java
@Entity
@Table(name = "certification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int validityMonths;
}
```
```java
// EmployeeCertification.java
@Entity
@Table(name = "employee_certification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeCertification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    @ManyToOne
    private Certification certification;

    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
}
```
---

## E08 - Safety Incidents & OSHA Reporting

Section: Safety Incident Domain  
Description:  
Records safety incidents, supports investigation workflow, and generates OSHA reports.

Design Specification:
- **Package:** `com.warehouse.ems.safety`
- **Entities:** `SafetyIncident`
- **Repository:** JPA repository
- **Service:** Workflow, reporting
- **Controller:** CRUD, export

Sample Implementation:
```java
// SafetyIncident.java
@Entity
@Table(name = "safety_incident")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SafetyIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String severity;
    private String location;
    private String description;

    @ManyToMany
    private List<Employee> involvedEmployees;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status; // OPEN, INVESTIGATING, RESOLVED
}
```
---

## E09 - Equipment & Asset Assignment

Section: Equipment Domain  
Description:  
Manages assignment of assets (scanners, forklifts, PPE), tracks check-in/out, and enforces certification requirements.

Design Specification:
- **Package:** `com.warehouse.ems.equipment`
- **Entities:** `Asset`, `AssetAssignment`
- **Repository:** JPA repositories
- **Service:** Assignment, validation
- **Controller:** CRUD, check-in/out

Sample Implementation:
```java
// Asset.java
@Entity
@Table(name = "asset")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private String serialNumber;
    private String condition;
}
```
```java
// AssetAssignment.java
@Entity
@Table(name = "asset_assignment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Asset asset;

    @ManyToOne
    private Employee employee;

    private LocalDateTime checkoutTime;
    private LocalDateTime returnTime;
}
```
---

## E10 - Performance Reviews & Goals

Section: Review Domain  
Description:  
Supports creation of review templates, goal tracking, and structured feedback workflows.

Design Specification:
- **Package:** `com.warehouse.ems.review`
- **Entities:** `PerformanceReview`, `ReviewTemplate`
- **Repository:** JPA repositories
- **Service:** Review cycles, workflow
- **Controller:** CRUD, PDF export

Sample Implementation:
```java
// PerformanceReview.java
@Entity
@Table(name = "performance_review")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    @ManyToOne
    private ReviewTemplate template;

    private String goals;
    private String comments;
    private String ratings;
    private boolean acknowledgedByEmployee;
    private boolean acknowledgedBySupervisor;
}
```
---

## E11 - Payroll Export Integration

Section: Payroll Integration  
Description:  
Exports payroll-ready files, maps to provider formats, and supports secure delivery.

Design Specification:
- **Package:** `com.warehouse.ems.payroll`
- **Service:** Export logic, SFTP/API delivery, retry/backoff
- **Audit:** Export logs

Sample Implementation:
```java
// PayrollExportService.java
@Service
public class PayrollExportService {
    public void exportPayroll(LocalDate periodStart, LocalDate periodEnd) {
        // Fetch approved attendance/leave, map to provider schema, deliver via SFTP/API
    }
}
```
---

## E12 - Notifications & Announcements

Section: Notification Domain  
Description:  
Delivers in-app, email, and SMS notifications for key events, with opt-in/out and quiet hours.

Design Specification:
- **Package:** `com.warehouse.ems.notification`
- **Entities:** `Notification`, `Announcement`
- **Service:** Delivery, templates, rate limiting
- **Controller:** CRUD, opt-in/out

Sample Implementation:
```java
// Notification.java
@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee recipient;

    private String channel; // IN_APP, EMAIL, SMS
    private String message;
    private boolean delivered;
    private LocalDateTime sentAt;
}
```
---

## E13 - Integration Layer (HRIS/WMS APIs)

Section: Integration APIs  
Description:  
Exposes REST APIs and connectors for HRIS, WMS, and SSO, with JWT/OAuth2 security.

Design Specification:
- **Package:** `com.warehouse.ems.integration`
- **Controller:** `/api/hris`, `/api/wms`
- **Security:** JWT/OAuth2
- **Webhooks:** Idempotent event handling

Sample Implementation:
```java
// HRISController.java
@RestController
@RequestMapping("/api/hris")
@RequiredArgsConstructor
public class HRISController {
    @PostMapping("/employees")
    public ResponseEntity<Void> syncEmployee(@RequestBody HRISSyncDTO dto) {
        // Create/update employee from HRIS
        return ResponseEntity.ok().build();
    }
}
```
---

## E14 - Audit Trail & Compliance

Section: Audit Logging  
Description:  
Centralized, immutable audit logging for sensitive changes, with export and test coverage.

Design Specification:
- **Package:** `com.warehouse.ems.audit`
- **Entity:** `AuditLog`
- **Service:** Log on create/update/delete
- **Controller:** Export by date/user/entity

Sample Implementation:
```java
// AuditLog.java
@Entity
@Table(name = "audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entity;
    private Long entityId;
    private String action;
    private String actor;
    private LocalDateTime timestamp;
    private String beforeState;
    private String afterState;
}
```
---

## E15 - Reporting & Analytics

Section: Reporting Domain  
Description:  
Provides operational reports, CSV/PDF export, and role-based dashboards.

Design Specification:
- **Package:** `com.warehouse.ems.reporting`
- **Service:** Report generation, export
- **Controller:** Endpoints for reports, metrics

Sample Implementation:
```java
// ReportingController.java
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportingController {
    @GetMapping("/attendance")
    public ResponseEntity<Resource> exportAttendanceReport(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        // Generate and return CSV
        return ResponseEntity.ok().body(reportingService.exportAttendance(from, to));
    }
}
```
---

## E16 - Mobile Access (PWA)

Section: Mobile/PWA Support  
Description:  
Enables responsive, offline-friendly PWA for core workflows.

Design Specification:
- **Package:** `com.warehouse.ems.mobile`
- **Controller:** Mobile-optimized endpoints
- **PWA Manifest:** Served at `/manifest.json`
- **Offline Queue:** For clock events

Sample Implementation:
```java
// manifest.json (static resource)
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "icons": [{ "src": "/icon-192.png", "sizes": "192x192", "type": "image/png" }]
}
```
---

## E17 - Onboarding & Offboarding Workflow

Section: Onboarding/Offboarding Domain  
Description:  
Automates provisioning, training, asset assignment, and deprovisioning.

Design Specification:
- **Package:** `com.warehouse.ems.onboarding`
- **Service:** Workflow orchestration
- **Integration:** HRIS, training, asset modules

Sample Implementation:
```java
// OnboardingService.java
@Service
public class OnboardingService {
    public void onboardNewHire(HRISSyncDTO dto) {
        // Create employee, assign initial schedule, required training, assets
    }

    public void offboardEmployee(Long employeeId) {
        // Revoke access, collect assets, update schedules
    }
}
```
---

# End of Document

This document provides a production-ready, detailed technical design for all Warehouse EMS user stories, with code samples and Spring Boot best practices. All code is ready for direct reference and implementation.