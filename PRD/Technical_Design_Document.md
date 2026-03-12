# Warehouse Employee Management System - Low-Level Technical Design Document

---

## Table of Contents

1. [E01: Project Scaffolding & Domain Setup](#e01-project-scaffolding--domain-setup)
2. [E02: Employee Master Data (CRUD)](#e02-employee-master-data-crud)
3. [E03: Role-Based Access Control (RBAC)](#e03-role-based-access-control-rbac)
4. [E04: Time & Attendance](#e04-time--attendance)
5. [E05: Shift & Schedule Management](#e05-shift--schedule-management)
6. [E06: Leave & Absence Management](#e06-leave--absence-management)
7. [E07: Training & Certification Tracking](#e07-training--certification-tracking)
8. [E08: Safety Incidents & OSHA Reporting](#e08-safety-incidents--osha-reporting)
9. [E09: Equipment & Asset Assignment](#e09-equipment--asset-assignment)
10. [E10: Performance Reviews & Goals](#e10-performance-reviews--goals)
11. [E11: Payroll Export Integration](#e11-payroll-export-integration)
12. [E12: Notifications & Announcements](#e12-notifications--announcements)
13. [E13: Integration Layer](#e13-integration-layer)
14. [E14: Audit Trail & Compliance](#e14-audit-trail--compliance)
15. [E15: Reporting & Analytics](#e15-reporting--analytics)
16. [E16: Mobile Access (PWA)](#e16-mobile-access-pwa)
17. [E17: Onboarding & Offboarding](#e17-onboarding--offboarding)

---

## E01: Project Scaffolding & Domain Setup

**Section:** Project Scaffolding & Domain Setup  
**Description:**  
Initializes the Spring Boot Maven project with standardized base packages, database migration support, and monitoring endpoints.

**Design Specification:**
- **Architecture:** Layered (Controller, Service, Repository, Domain)
- **Packages:**  
  - `com.company.wems`  
    - `config`  
    - `controller`  
    - `service`  
    - `repository`  
    - `domain`  
    - `dto`  
    - `exception`  
    - `security`  
    - `integration`  
    - `audit`  
    - `util`
- **Maven Modules:** Single module or multi-module (core, api, integration)
- **Database Migration:** Flyway or Liquibase for schema versioning
- **Monitoring:** Spring Boot Actuator enabled
- **Application Properties:** `application.yml` for environment configs

**Sample Implementation:**
```java
// pom.xml (excerpt)
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wems
    username: wems
    password: secret
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  flyway:
    enabled: true
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

---

## E02: Employee Master Data (CRUD)

**Section:** Employee Master Data (CRUD)  
**Description:**  
Manages employee records with CRUD operations, unique badge IDs, soft-delete, and pagination.

**Design Specification:**
- **Entity:** `Employee`
  - Fields: id, badgeId (unique), firstName, lastName, email, phone, status, deleted, createdAt, updatedAt
  - Soft-delete via `deleted` boolean
- **Repository:** `EmployeeRepository` extends `JpaRepository<Employee, Long>`
- **Service:** `EmployeeService` for business logic (CRUD, soft-delete)
- **Controller:** `EmployeeController` with REST endpoints
- **DTOs:** `EmployeeRequestDTO`, `EmployeeResponseDTO`
- **Validation:** Bean Validation (e.g., `@NotNull`, `@Email`)
- **Pagination:** Spring Data JPA `Pageable`
- **Exception Handling:** Custom exceptions, `@ControllerAdvice`
- **Security:** Method-level security (ADMIN, HR)
- **Database:** Unique constraint on badgeId

**Sample Implementation:**
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue
    private Long id;
    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;
    @NotNull private String firstName;
    @NotNull private String lastName;
    @Email private String email;
    private String phone;
    private String status;
    private boolean deleted = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // getters/setters
}
```
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}
```
```java
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    @GetMapping
    public Page<EmployeeResponseDTO> list(Pageable pageable) { ... }
    @PostMapping
    public EmployeeResponseDTO create(@Valid @RequestBody EmployeeRequestDTO dto) { ... }
    @DeleteMapping("/{id}")
    public void softDelete(@PathVariable Long id) { ... }
}
```

---

## E03: Role-Based Access Control (RBAC)

**Section:** Role-Based Access Control (RBAC)  
**Description:**  
Implements authentication and authorization using Spring Security with roles: ADMIN, HR, SUPERVISOR, WORKER.

**Design Specification:**
- **Entity:** `User`, `Role` (Many-to-Many)
- **Security Config:** `WebSecurityConfig` with JWT or session-based auth
- **Method Security:** `@PreAuthorize`, `@Secured`
- **Role Hierarchy:** ADMIN > HR > SUPERVISOR > WORKER
- **Password Encoding:** BCrypt
- **UserDetailsService:** Custom implementation
- **Endpoints:** `/api/auth/**` for login, `/api/users/**` for user management

**Sample Implementation:**
```java
@Entity
public class Role {
    @Id @GeneratedValue
    private Long id;
    @Enumerated(EnumType.STRING)
    private RoleName name; // ADMIN, HR, SUPERVISOR, WORKER
}
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    private String username;
    private String password;
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;
}
```
```java
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
          .csrf().disable()
          .authorizeRequests()
          .antMatchers("/api/auth/**").permitAll()
          .antMatchers("/api/employees/**").hasAnyRole("ADMIN", "HR")
          .anyRequest().authenticated();
    }
}
```
```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long id) { ... }
```

---

## E04: Time & Attendance

**Section:** Time & Attendance  
**Description:**  
Handles employee clock in/out, geofencing, hour calculation, and missed punch management.

**Design Specification:**
- **Entity:** `AttendanceRecord`
  - Fields: id, employee, clockIn, clockOut, location, status, createdAt
- **Service:** `AttendanceService` (clock in/out, calculate hours, handle missed punches)
- **Repository:** `AttendanceRepository`
- **Controller:** `AttendanceController`
- **Geofencing:** Validate location within allowed area
- **DTOs:** `ClockInRequestDTO`, `ClockOutRequestDTO`, `AttendanceResponseDTO`
- **Validation:** Location, time constraints
- **Exception Handling:** Custom exceptions for invalid punches

**Sample Implementation:**
```java
@Entity
public class AttendanceRecord {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private String location;
    private String status; // e.g., PRESENT, MISSED_PUNCH
}
```
```java
@PostMapping("/clock-in")
public AttendanceResponseDTO clockIn(@Valid @RequestBody ClockInRequestDTO dto) { ... }
```
```java
public boolean isWithinGeofence(String location) { ... }
```

---

## E05: Shift & Schedule Management

**Section:** Shift & Schedule Management  
**Description:**  
Manages shift templates, rotations, overtime rules, and conflict detection.

**Design Specification:**
- **Entities:** `ShiftTemplate`, `ShiftAssignment`
  - `ShiftTemplate`: id, name, startTime, endTime, overtimeRule
  - `ShiftAssignment`: id, employee, shiftTemplate, date, status
- **Service:** `ShiftService` (assign, rotate, detect conflicts)
- **Repository:** `ShiftTemplateRepository`, `ShiftAssignmentRepository`
- **Controller:** `ShiftController`
- **DTOs:** `ShiftAssignmentRequestDTO`, `ShiftAssignmentResponseDTO`
- **Validation:** No overlapping shifts, overtime calculation
- **Exception Handling:** Shift conflict exceptions

**Sample Implementation:**
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String overtimeRule;
}
@Entity
public class ShiftAssignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private ShiftTemplate shiftTemplate;
    private LocalDate date;
    private String status;
}
```
```java
public boolean hasShiftConflict(Employee emp, LocalDate date, LocalTime start, LocalTime end) { ... }
```

---

## E06: Leave & Absence Management

**Section:** Leave & Absence Management  
**Description:**  
Handles PTO, sick, unpaid leave requests, approvals, and accrual balances.

**Design Specification:**
- **Entities:** `LeaveRequest`, `LeaveBalance`
  - `LeaveRequest`: id, employee, type, startDate, endDate, status, approver, createdAt
  - `LeaveBalance`: id, employee, type, balance
- **Service:** `LeaveService` (request, approve, update balances)
- **Repository:** `LeaveRequestRepository`, `LeaveBalanceRepository`
- **Controller:** `LeaveController`
- **DTOs:** `LeaveRequestDTO`, `LeaveResponseDTO`
- **Validation:** Sufficient balance, overlapping leaves
- **Exception Handling:** Insufficient balance, invalid dates

**Sample Implementation:**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type; // PTO, SICK, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // PENDING, APPROVED, REJECTED
    @ManyToOne
    private Employee approver;
}
```
```java
@PostMapping("/leave-requests")
public LeaveResponseDTO requestLeave(@Valid @RequestBody LeaveRequestDTO dto) { ... }
```

---

## E07: Training & Certification Tracking

**Section:** Training & Certification Tracking  
**Description:**  
Tracks employee certifications, expirations, and blocks assignments for expired certifications.

**Design Specification:**
- **Entities:** `Certification`, `EmployeeCertification`
  - `Certification`: id, name, description, validPeriod
  - `EmployeeCertification`: id, employee, certification, issueDate, expiryDate, status
- **Service:** `CertificationService` (assign, validate, block expired)
- **Repository:** `CertificationRepository`, `EmployeeCertificationRepository`
- **Controller:** `CertificationController`
- **DTOs:** `CertificationDTO`, `EmployeeCertificationDTO`
- **Validation:** Expiry checks before assignments
- **Exception Handling:** Assignment blocked for expired certs

**Sample Implementation:**
```java
@Entity
public class EmployeeCertification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private Certification certification;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String status; // ACTIVE, EXPIRED
}
```
```java
public boolean isCertificationValid(Employee emp, Certification cert) { ... }
```

---

## E08: Safety Incidents & OSHA Reporting

**Section:** Safety Incidents & OSHA Reporting  
**Description:**  
Records safety incidents, manages investigation workflow, and supports OSHA exports.

**Design Specification:**
- **Entities:** `SafetyIncident`, `IncidentInvestigation`
  - `SafetyIncident`: id, employee, date, description, severity, status
  - `IncidentInvestigation`: id, incident, investigator, findings, actions, completedAt
- **Service:** `IncidentService` (record, investigate, export)
- **Repository:** `SafetyIncidentRepository`, `IncidentInvestigationRepository`
- **Controller:** `IncidentController`
- **DTOs:** `IncidentDTO`, `InvestigationDTO`
- **Integration:** OSHA export (CSV/PDF)
- **Validation:** Required fields, status transitions

**Sample Implementation:**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private LocalDate date;
    private String description;
    private String severity;
    private String status; // OPEN, UNDER_INVESTIGATION, CLOSED
}
```
```java
@GetMapping("/incidents/export")
public ResponseEntity<Resource> exportIncidents(@RequestParam String format) { ... }
```

---

## E09: Equipment & Asset Assignment

**Section:** Equipment & Asset Assignment  
**Description:**  
Assigns and tracks equipment (scanners, forklifts, PPE) checkout/return.

**Design Specification:**
- **Entities:** `Asset`, `AssetAssignment`
  - `Asset`: id, type, serialNumber, status
  - `AssetAssignment`: id, asset, employee, checkoutDate, returnDate, status
- **Service:** `AssetService` (assign, return, track)
- **Repository:** `AssetRepository`, `AssetAssignmentRepository`
- **Controller:** `AssetController`
- **DTOs:** `AssetAssignmentDTO`, `AssetDTO`
- **Validation:** Asset availability, return status

**Sample Implementation:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String serialNumber;
    private String status; // AVAILABLE, ASSIGNED, MAINTENANCE
}
@Entity
public class AssetAssignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Asset asset;
    @ManyToOne
    private Employee employee;
    private LocalDateTime checkoutDate;
    private LocalDateTime returnDate;
    private String status; // CHECKED_OUT, RETURNED
}
```
```java
@PostMapping("/assets/assign")
public AssetAssignmentDTO assignAsset(@Valid @RequestBody AssetAssignmentDTO dto) { ... }
```

---

## E10: Performance Reviews & Goals

**Section:** Performance Reviews & Goals  
**Description:**  
Manages review templates, goals, competencies, and ratings.

**Design Specification:**
- **Entities:** `PerformanceReview`, `Goal`, `Competency`
  - `PerformanceReview`: id, employee, reviewer, period, rating, comments
  - `Goal`: id, review, description, status
  - `Competency`: id, review, name, rating
- **Service:** `ReviewService` (create, rate, track goals)
- **Repository:** `PerformanceReviewRepository`, `GoalRepository`, `CompetencyRepository`
- **Controller:** `ReviewController`
- **DTOs:** `ReviewDTO`, `GoalDTO`, `CompetencyDTO`
- **Validation:** Review period, rating scale

**Sample Implementation:**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    @ManyToOne
    private Employee reviewer;
    private String period;
    private Integer rating;
    private String comments;
}
```
```java
@PostMapping("/reviews")
public ReviewDTO createReview(@Valid @RequestBody ReviewDTO dto) { ... }
```

---

## E11: Payroll Export Integration

**Section:** Payroll Export Integration  
**Description:**  
Generates payroll files from attendance/leave and delivers via SFTP/API.

**Design Specification:**
- **Service:** `PayrollExportService` (generate, deliver)
- **Integration:** SFTP client, REST API client
- **DTOs:** `PayrollExportDTO`
- **Controller:** `PayrollExportController`
- **Validation:** Data completeness, export format
- **Security:** Secure credentials for SFTP/API

**Sample Implementation:**
```java
@Service
public class PayrollExportService {
    @Transactional(readOnly = true)
    public File generatePayrollExport(LocalDate periodStart, LocalDate periodEnd) { ... }
    public void deliverViaSftp(File file) { ... }
}
```
```java
@PostMapping("/payroll/export")
public ResponseEntity<?> exportPayroll(@RequestBody PayrollExportDTO dto) { ... }
```

---

## E12: Notifications & Announcements

**Section:** Notifications & Announcements  
**Description:**  
Sends in-app, email, and SMS notifications for shifts, certifications, and approvals.

**Design Specification:**
- **Entities:** `Notification`
  - id, employee, type, message, status, sentAt
- **Service:** `NotificationService` (send, schedule)
- **Integration:** Email/SMS providers
- **Controller:** `NotificationController`
- **DTOs:** `NotificationDTO`
- **Validation:** Contact info, notification type

**Sample Implementation:**
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Employee employee;
    private String type; // IN_APP, EMAIL, SMS
    private String message;
    private String status; // SENT, FAILED
    private LocalDateTime sentAt;
}
```
```java
public void sendNotification(NotificationDTO dto) { ... }
```

---

## E13: Integration Layer

**Section:** Integration Layer  
**Description:**  
Provides REST APIs for HRIS/WMS/IDP, webhooks, and SSO.

**Design Specification:**
- **Service:** `IntegrationService` (HRIS, WMS, IDP)
- **Controller:** `IntegrationController`
- **Endpoints:** `/api/integrations/**`
- **Security:** OAuth2/OpenID for SSO
- **Webhooks:** Event-driven notifications
- **DTOs:** Integration-specific

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/integrations")
public class IntegrationController {
    @PostMapping("/hris")
    public ResponseEntity<?> receiveHrisData(@RequestBody HrisDTO dto) { ... }
}
```
```java
@Configuration
@EnableWebSecurity
public class OAuth2SecurityConfig { ... }
```

---

## E14: Audit Trail & Compliance

**Section:** Audit Trail & Compliance  
**Description:**  
Centralized audit logging with tamper-evident storage.

**Design Specification:**
- **Entity:** `AuditLog`
  - id, entity, entityId, action, user, timestamp, details, hash
- **Service:** `AuditService` (log, verify)
- **Repository:** `AuditLogRepository`
- **Aspect:** `@Audit` annotation, AOP for logging
- **Tamper-Evidence:** Hash chaining
- **Controller:** `AuditController` (admin access)

**Sample Implementation:**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private Long entityId;
    private String action;
    private String user;
    private LocalDateTime timestamp;
    private String details;
    private String hash;
}
```
```java
@Aspect
@Component
public class AuditAspect {
    @AfterReturning(...)
    public void logAudit(JoinPoint jp, Object result) { ... }
}
```

---

## E15: Reporting & Analytics

**Section:** Reporting & Analytics  
**Description:**  
Provides operational reports, CSV/PDF exports, and dashboards.

**Design Specification:**
- **Service:** `ReportingService` (generate, export)
- **Controller:** `ReportingController`
- **DTOs:** `ReportRequestDTO`, `ReportResponseDTO`
- **Integration:** PDF/CSV libraries
- **Security:** Role-based access to reports

**Sample Implementation:**
```java
@PostMapping("/reports")
public ResponseEntity<Resource> generateReport(@RequestBody ReportRequestDTO dto) { ... }
```
```java
public byte[] exportToCsv(List<?> data) { ... }
```

---

## E16: Mobile Access (PWA)

**Section:** Mobile Access (PWA)  
**Description:**  
Enables responsive views, offline queue, and PWA manifest for mobile access.

**Design Specification:**
- **Frontend:** PWA manifest, service worker
- **Backend:** REST APIs, offline queue endpoints
- **Security:** Token-based auth for mobile
- **Controller:** `MobileController`
- **DTOs:** Mobile-specific

**Sample Implementation:**
```yaml
# static/manifest.json
{
  "name": "Warehouse EMS",
  "short_name": "WEMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff"
}
```
```java
@PostMapping("/mobile/offline-queue")
public ResponseEntity<?> syncOfflineData(@RequestBody List<OfflineDTO> dtos) { ... }
```

---

## E17: Onboarding & Offboarding

**Section:** Onboarding & Offboarding  
**Description:**  
Automates provisioning and deprovisioning of employees.

**Design Specification:**
- **Service:** `OnboardingService`, `OffboardingService`
- **Integration:** HRIS, IDP for account creation/removal
- **Controller:** `OnboardingController`
- **DTOs:** `OnboardingRequestDTO`, `OffboardingRequestDTO`
- **Validation:** Required fields, status checks

**Sample Implementation:**
```java
@PostMapping("/onboarding")
public ResponseEntity<?> onboardEmployee(@Valid @RequestBody OnboardingRequestDTO dto) { ... }
```
```java
@Service
public class OnboardingService {
    @Transactional
    public void onboardEmployee(OnboardingRequestDTO dto) { ... }
}
```

---

# Common Patterns & Best Practices

- **Exception Handling:**  
  - Use `@ControllerAdvice` for global exception handling.
  - Custom exceptions for domain errors.
  - Return meaningful error responses.

- **Validation:**  
  - Use Bean Validation (`@NotNull`, `@Email`, etc.) on DTOs.
  - Custom validators for complex rules.

- **Transaction Management:**  
  - Annotate service methods with `@Transactional`.
  - Use read-only transactions where applicable.

- **DTO Pattern:**  
  - Use DTOs for all API requests/responses.
  - Map entities to DTOs in service layer.

- **Security:**  
  - Secure endpoints with Spring Security.
  - Use method-level security for sensitive operations.

- **Monitoring:**  
  - Enable Actuator endpoints for health, metrics, etc.

- **Database Migrations:**  
  - Use Flyway/Liquibase for schema changes.

---

# End of Document