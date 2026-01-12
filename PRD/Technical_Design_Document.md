# Warehouse Employee Management System â Low-Level Technical Design Document

> **Filename:** PRD/Technical_Design_Document.md  
> **Purpose:** Comprehensive low-level technical design for all user stories, adhering to Spring Boot industry standards, for easy consumption by Spring Boot developers.

---

## Table of Contents

1. [Introduction](#introduction)
2. [Global Spring Boot Architecture Overview](#global-spring-boot-architecture-overview)
3. [Package Structure & Module Definitions](#package-structure--module-definitions)
4. [Epic-by-Epic Technical Design](#epic-by-epic-technical-design)
    - [E01. Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
    - [E02. Employee Master Data (CRUD)](#e02-employee-master-data-crud)
    - [E03. Role-Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
    - [E04. Time & Attendance (Clock In/Out)](#e04-time--attendance-clock-inout)
    - [E05. Shift & Schedule Management](#e05-shift--schedule-management)
    - [E06. Leave & Absence Management](#e06-leave--absence-management)
    - [E07. Training & Certification Tracking](#e07-training--certification-tracking)
    - [E08. Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
    - [E09. Equipment & Asset Assignment](#e09-equipment--asset-assignment)
    - [E10. Performance Reviews & Goals](#e10-performance-reviews--goals)
    - [E11. Payroll Export Integration](#e11-payroll-export-integration)
    - [E12. Notifications & Announcements](#e12-notifications--announcements)
    - [E13. Integration Layer (HRIS/WMS APIs)](#e13-integration-layer-hriswms-apis)
    - [E14. Audit Trail & Compliance](#e14-audit-trail--compliance)
    - [E15. Reporting & Analytics](#e15-reporting--analytics)
    - [E16. Mobile Access (PWA)](#e16-mobile-access-pwa)
    - [E17. Onboarding & Offboarding Workflow](#e17-onboarding--offboarding-workflow)
    - [E18. Localization & Multi-Tenant](#e18-localization--multi-tenant)
    - [E19. Observability & Monitoring](#e19-observability--monitoring)
    - [E20. CI/CD](#e20-cicd)
5. [Appendix: Sample Code Snippets & Patterns](#appendix-sample-code-snippets--patterns)

---

## Introduction

This document provides a comprehensive low-level technical design for the Warehouse Employee Management System, covering all 20 epics and their 85+ user stories. It is structured for easy consumption by Spring Boot developers, ensuring uniformity, maintainability, and adherence to industry best practices.

---

## Global Spring Boot Architecture Overview

- **Architecture Pattern:** Layered (Hexagonal/Clean Architecture influences)
- **Core Layers:**
    - `controller` (REST API, Web)
    - `service` (Business logic)
    - `repository` (Persistence)
    - `domain/model` (Entities, Value Objects, Aggregates)
    - `config` (Configuration, Security, Integration)
    - `integration` (External APIs, Messaging)
    - `util` (Helpers, Mappers)
- **Tech Stack:** Spring Boot, Spring Data JPA, Spring Security, Spring Web, Flyway/Liquibase, Actuator, OpenAPI, OAuth2/JWT, MapStruct, Lombok, PostgreSQL/MySQL, Docker, Maven, JUnit, Mockito, Testcontainers.

---

## Package Structure & Module Definitions

```
com.company.warehousemgmt
âââ config
âââ controller
â   âââ employee
â   âââ attendance
â   âââ shift
â   âââ leave
â   âââ training
â   âââ safety
â   âââ equipment
â   âââ review
â   âââ payroll
â   âââ notification
â   âââ integration
â   âââ audit
â   âââ report
â   âââ mobile
âââ domain
â   âââ employee
â   âââ attendance
â   âââ shift
â   âââ leave
â   âââ training
â   âââ safety
â   âââ equipment
â   âââ review
â   âââ payroll
â   âââ notification
â   âââ integration
â   âââ audit
â   âââ report
âââ repository
âââ service
âââ integration
âââ util
âââ Application.java
```

- **Modules:** Each epic maps to a logical module/package, with clear separation of concerns.

---

## Epic-by-Epic Technical Design

---

### E01. Project Scaffolding & Domain Setup

**Overview:**  
- Initialize Spring Boot (Maven) project.
- Configure base packages.
- Set up core modules.
- Add Flyway/Liquibase for DB migrations.
- Enable Actuator.

**Design Specifications:**
- **Base Package:** `com.company.warehousemgmt`
- **DB Migration:** Use Flyway (`src/main/resources/db/migration`)
- **Actuator:** Expose `/actuator/health`, `/actuator/info`
- **README:** Build/run steps, environment variables, Docker Compose for local DB.

**Sample Implementation:**
```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```
- **application.yml:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse
    username: warehouse
    password: secret
  flyway:
    enabled: true
    locations: classpath:db/migration
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

---

### E02. Employee Master Data (CRUD)

**Overview:**  
- Employee domain with CRUD APIs and DTOs.

**Entity Design:**
```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id @GeneratedValue
    private Long id;
    @Column(unique = true, nullable = false)
    private String badgeId;
    private String name;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status; // ACTIVE, INACTIVE, TERMINATED
    private boolean deleted = false; // Soft delete
    // getters/setters
}
```

**Repository:**
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```

**Service:**
```java
@Service
public class EmployeeService {
    // CRUD methods, soft-delete, filtering, validation
}
```

**Controller:**
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping public ResponseEntity<EmployeeDTO> create(@RequestBody @Valid EmployeeDTO dto) { ... }
    @GetMapping public Page<EmployeeDTO> list(Pageable pageable, ...) { ... }
    @GetMapping("/{id}") public EmployeeDTO get(@PathVariable Long id) { ... }
    @PutMapping("/{id}") public EmployeeDTO update(@PathVariable Long id, @RequestBody EmployeeDTO dto) { ... }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { ... }
}
```

**Best Practices:**
- Use DTOs for API, MapStruct for mapping.
- OpenAPI annotations for schema.
- Pagination, filtering, soft-delete.

---

### E03. Role-Based Access Control (RBAC)

**Overview:**  
- Spring Security with roles: ADMIN, HR, SUPERVISOR, WORKER.
- Method/endpoint security, row-level constraints.

**Configuration:**
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
          .csrf().disable()
          .authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .antMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
          .and()
            .oauth2ResourceServer().jwt();
    }
}
```

**Role Enforcement:**
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(#id, authentication))")
public EmployeeDTO getEmployee(Long id) { ... }
```

**API Key/OAuth2 Toggle:**
- Use `@ConditionalOnProperty` for switching.

**Tests:**
- Use `@WithMockUser` for role-based tests.

---

### E04. Time & Attendance (Clock In/Out)

**Overview:**  
- Endpoints for clock-in/out, geofence/device capture, hours calculation, missed punches, corrections.

**Entity:**
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime timestamp;
    private AttendanceType type; // CLOCK_IN, CLOCK_OUT
    private String deviceId;
    private GeoLocation geoLocation;
    private boolean correction;
    // getters/setters
}
```

**Service:**
- Validate clock-in/out sequence.
- Associate with shift.
- Compute daily totals.
- Handle corrections (approval workflow).

**Controller:**
```java
@PostMapping("/attendance/clock-in")
public ResponseEntity<?> clockIn(@RequestBody AttendanceDTO dto) { ... }
@PostMapping("/attendance/clock-out")
public ResponseEntity<?> clockOut(@RequestBody AttendanceDTO dto) { ... }
```

**Reports:**
- Export CSV endpoint.

---

### E05. Shift & Schedule Management

**Overview:**  
- Shift templates, rotations, overtime, blackout dates, assignment.

**Entities:**
```java
@Entity
public class ShiftTemplate { ... }
@Entity
public class ShiftAssignment { ... }
```

**Service:**
- CRUD for templates.
- Conflict detection.
- Bulk assignment.
- Audit entries.

**Controller:**
- `/shifts/templates`, `/shifts/assignments`

---

### E06. Leave & Absence Management

**Overview:**  
- PTO, sick, unpaid leave; accruals; scheduling/payroll integration.

**Entities:**
```java
@Entity
public class LeaveRequest { ... }
@Entity
public class LeaveBalance { ... }
```

**Service:**
- Request/approve workflow.
- Update balances.
- Exclude from scheduling/payroll.

**Controller:**
- `/leave/requests`, `/leave/balances`

---

### E07. Training & Certification Tracking

**Overview:**  
- Track certifications, expirations, renewals, proof uploads.

**Entities:**
```java
@Entity
public class Certification { ... }
@Entity
public class EmployeeCertification { ... }
```

**Service:**
- CRUD, expiry alerts, scheduling checks.

**Controller:**
- `/certifications`, `/employees/{id}/certifications`

---

### E08. Safety Incidents & OSHA Reporting

**Overview:**  
- Record incidents, workflow, OSHA summary.

**Entities:**
```java
@Entity
public class SafetyIncident { ... }
```

**Service:**
- Status workflow.
- OSHA export.

**Controller:**
- `/safety/incidents`

---

### E09. Equipment & Asset Assignment

**Overview:**  
- Assign assets, track check-in/out, block if cert missing.

**Entities:**
```java
@Entity
public class Asset { ... }
@Entity
public class AssetAssignment { ... }
```

**Service:**
- CRUD, check-in/out, validation.

**Controller:**
- `/assets`, `/assets/assignments`

---

### E10. Performance Reviews & Goals

**Overview:**  
- Review templates, goals, ratings, acknowledgements.

**Entities:**
```java
@Entity
public class PerformanceReview { ... }
@Entity
public class ReviewTemplate { ... }
```

**Service:**
- Review cycles, workflow, PDF export.

**Controller:**
- `/reviews`, `/review-templates`

---

### E11. Payroll Export Integration

**Overview:**  
- Generate payroll files, map to provider, secure delivery.

**Service:**
- Export jobs, SFTP/API integration, retries, audit log.

**Controller:**
- `/payroll/exports`

---

### E12. Notifications & Announcements

**Overview:**  
- In-app/email/SMS notifications, quiet hours.

**Entities:**
```java
@Entity
public class Notification { ... }
@Entity
public class Announcement { ... }
```

**Service:**
- Delivery, opt-in/out, templates, localization.

**Controller:**
- `/notifications`, `/announcements`

---

### E13. Integration Layer (HRIS/WMS APIs)

**Overview:**  
- REST APIs, connectors, SSO, webhooks.

**Service:**
- HRIS sync, WMS link, JWT/OAuth2 security.

**Controller:**
- `/api/integration/hris`, `/api/integration/wms`

---

### E14. Audit Trail & Compliance

**Overview:**  
- Centralized audit logging, tamper-evident storage.

**Entities:**
```java
@Entity
public class AuditLog { ... }
```

**Service:**
- Log create/update/delete, immutable storage.

**Controller:**
- `/audit/logs`

---

### E15. Reporting & Analytics

**Overview:**  
- Operational reports, CSV/PDF export, dashboards.

**Service:**
- Report generation, filtering, metrics endpoints.

**Controller:**
- `/reports`, `/metrics`

---

### E16. Mobile Access (PWA)

**Overview:**  
- Responsive views, offline support.

**Design:**
- Spring Boot serves PWA assets.
- REST APIs for mobile flows.
- Offline queue for clock events.

---

### E17. Onboarding & Offboarding Workflow

**Overview:**  
- Automate provisioning/deprovisioning, training, asset assignment.

**Service:**
- HRIS triggers, task generation, access/asset revocation.

**Controller:**
- `/onboarding`, `/offboarding`

---

### E18. Localization & Multi-Tenant

**Overview:**  
- i18n, tenant isolation.

**Design:**
- Use Spring `MessageSource` for localization.
- Tenant context via filter/interceptor.

---

### E19. Observability & Monitoring

**Overview:**  
- Metrics, logging, tracing.

**Design:**
- Spring Boot Actuator, Micrometer, centralized logging (ELK/Prometheus).

---

### E20. CI/CD

**Overview:**  
- Automated build, test, deploy.

**Design:**
- Maven, Docker, GitHub Actions/Jenkins, test coverage, static analysis.

---

## Appendix: Sample Code Snippets & Patterns

**DTO Example:**
```java
@Data
public class EmployeeDTO {
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

**MapStruct Mapper:**
```java
@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeDTO toDto(Employee entity);
    Employee toEntity(EmployeeDTO dto);
}
```

**Exception Handling:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleNotFound(EntityNotFoundException ex) { ... }
}
```

**Testing:**
```java
@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {
    @Autowired MockMvc mockMvc;
    @Test void testCreateEmployee() { ... }
}
```

---

## Best Practices & Standards

- Use DTOs for all API boundaries.
- Validate input with `@Valid` and custom validators.
- Use `@Transactional` for service methods.
- Secure endpoints with method and endpoint security.
- Use OpenAPI/Swagger for API documentation.
- Write unit/integration tests for all layers.
- Use Flyway/Liquibase for DB migrations.
- Log all sensitive changes in audit trail.
- Use environment variables for secrets/config.
- Follow SOLID and DRY principles.

---

**End of Document**