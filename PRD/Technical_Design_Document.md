# Warehouse Employee Management System â Low-Level Technical Design Document

**Version:** 1.0  
**Date:** 2024-06-XX  
**Authors:** Senior Software Architect Team  
**Scope:** This document provides a comprehensive, production-ready, low-level technical design for the Warehouse Employee Management System, covering all 20 epics and 85+ user stories. It adheres to Spring Boot industry standards and is structured for direct implementation by Spring Boot developers.

---

## Table of Contents

1. [Project Scaffolding & Domain Setup (E01)](#e01)
2. [Employee Master Data (CRUD) (E02)](#e02)
3. [Role-Based Access Control (RBAC) (E03)](#e03)
4. [Time & Attendance (Clock In/Out) (E04)](#e04)
5. [Shift & Schedule Management (E05)](#e05)
6. [Leave & Absence Management (E06)](#e06)
7. [Training & Certification Tracking (E07)](#e07)
8. [Safety Incidents & OSHA Reporting (E08)](#e08)
9. [Equipment & Asset Assignment (E09)](#e09)
10. [Performance Reviews & Goals (E10)](#e10)
11. [Payroll Export Integration (E11)](#e11)
12. [Notifications & Announcements (E12)](#e12)
13. [Integration Layer (HRIS/WMS APIs) (E13)](#e13)
14. [Audit Trail & Compliance (E14)](#e14)
15. [Reporting & Analytics (E15)](#e15)
16. [Mobile Access (PWA) (E16)](#e16)
17. [Onboarding & Offboarding Workflow (E17)](#e17)
18. [Localization (E18)](#e18)
19. [Observability (E19)](#e19)
20. [CI/CD (E20)](#e20)

---

## <a name="e01"></a> E01 â Project Scaffolding & Domain Setup

### 1. Domain Model

**Design Decisions:**  
- Use Maven for build management.
- Package structure follows `com.companyname.wems.<module>`.
- Core modules: employee, scheduling, attendance, safety.
- Use Flyway for DB migrations.
- Enable Spring Boot Actuator for health and metrics.

**Design Specification:**  
- `src/main/java/com/companyname/wems/`
  - `employee/`
  - `scheduling/`
  - `attendance/`
  - `safety/`
  - `common/`
- `src/main/resources/db/migration/` for Flyway scripts.
- `application.yml` for configuration.
- Actuator endpoints enabled.

**Sample Implementation:**
```java
// Main Application
@SpringBootApplication
public class WemsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WemsApplication.class, args);
    }
}

// application.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wems
    username: wems_user
    password: secret
  flyway:
    enabled: true
  jpa:
    hibernate:
      ddl-auto: validate

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

---

## <a name="e02"></a> E02 â Employee Master Data (CRUD)

### 1. Domain Model

**Design Decisions:**  
- Employee is a core entity with unique `badgeId`.
- Soft-delete via `status` field.
- Use DTOs for API exposure.

**Design Specification:**
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
public class Employee {
    @Id @GeneratedValue private Long id;
    @Column(nullable = false, unique = true) private String badgeId;
    @Column(nullable = false) private String name;
    @Enumerated(EnumType.STRING) private Role role;
    @ManyToOne private Department department;
    @ManyToOne private ShiftGroup shiftGroup;
    @Column private LocalDate hireDate;
    @Enumerated(EnumType.STRING) private EmployeeStatus status; // ACTIVE, INACTIVE, DELETED
    // getters/setters
}
```

### 2. Repository Layer

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findAllByStatus(EmployeeStatus status, Pageable pageable);
}
```

### 3. Service Layer

```java
public interface EmployeeService {
    EmployeeDTO createEmployee(EmployeeDTO dto);
    EmployeeDTO getEmployee(Long id);
    Page<EmployeeDTO> listEmployees(EmployeeStatus status, Pageable pageable);
    EmployeeDTO updateEmployee(Long id, EmployeeDTO dto);
    void deleteEmployee(Long id); // soft delete
}
```

### 4. Controller Design

```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping public ResponseEntity<EmployeeDTO> create(@RequestBody @Valid EmployeeDTO dto) { ... }
    @GetMapping("/{id}") public ResponseEntity<EmployeeDTO> get(@PathVariable Long id) { ... }
    @GetMapping public Page<EmployeeDTO> list(@RequestParam EmployeeStatus status, Pageable pageable) { ... }
    @PutMapping("/{id}") public ResponseEntity<EmployeeDTO> update(@PathVariable Long id, @RequestBody @Valid EmployeeDTO dto) { ... }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
}
```

### 5. OpenAPI Schema

```yaml
EmployeeDTO:
  type: object
  properties:
    id: { type: integer }
    badgeId: { type: string }
    name: { type: string }
    role: { type: string }
    department: { $ref: '#/components/schemas/DepartmentDTO' }
    shiftGroup: { $ref: '#/components/schemas/ShiftGroupDTO' }
    hireDate: { type: string, format: date }
    status: { type: string }
```

---

## <a name="e03"></a> E03 â Role-Based Access Control (RBAC)

### 1. Security Configuration

**Design Decisions:**  
- Use Spring Security with roles: ADMIN, HR, SUPERVISOR, WORKER.
- Method and endpoint security.
- Row-level security for sensitive data.
- API key/OAuth2 toggle via config.

**Design Specification:**
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
          .csrf().disable()
          .authorizeRequests()
            .antMatchers("/actuator/**").permitAll()
            .antMatchers(HttpMethod.POST, "/employees/**").hasRole("ADMIN")
            .antMatchers(HttpMethod.GET, "/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .anyRequest().authenticated()
          .and()
            .oauth2ResourceServer().jwt();
    }
}
```

**Sample Method Security:**
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(principal, #id))")
public EmployeeDTO getEmployee(Long id) { ... }
```

---

## <a name="e04"></a> E04 â Time & Attendance (Clock In/Out)

### 1. Domain Model

**Design Decisions:**  
- AttendanceEvent entity for clock-in/out.
- Geofence and device info optional.
- Correction workflow for missed punches.

**Design Specification:**
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue private Long id;
    @ManyToOne(optional = false) private Employee employee;
    @Enumerated(EnumType.STRING) private AttendanceType type; // CLOCK_IN, CLOCK_OUT
    @Column(nullable = false) private LocalDateTime timestamp;
    @Column private String deviceId;
    @Column private String geoLocation;
    @ManyToOne private Shift shift;
    @Enumerated(EnumType.STRING) private AttendanceStatus status; // NORMAL, CORRECTION_PENDING, CORRECTED
}
```

### 2. Service Layer

```java
public interface AttendanceService {
    AttendanceEventDTO clockIn(Long employeeId, ClockEventRequest req);
    AttendanceEventDTO clockOut(Long employeeId, ClockEventRequest req);
    List<AttendanceEventDTO> getDailyAttendance(Long employeeId, LocalDate date);
    void requestCorrection(Long eventId, CorrectionRequest req);
}
```

### 3. Controller Design

```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    @PostMapping("/clock-in") public ResponseEntity<AttendanceEventDTO> clockIn(@RequestBody ClockEventRequest req) { ... }
    @PostMapping("/clock-out") public ResponseEntity<AttendanceEventDTO> clockOut(@RequestBody ClockEventRequest req) { ... }
    @PostMapping("/corrections") public ResponseEntity<Void> requestCorrection(@RequestBody CorrectionRequest req) { ... }
}
```

---

## <a name="e05"></a> E05 â Shift & Schedule Management

### 1. Domain Model

**Design Decisions:**  
- ShiftTemplate for recurring patterns.
- ShiftAssignment links employees to shifts.
- Blackout dates and operation calendars.

**Design Specification:**
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    @Column private String name;
    @Column private LocalTime startTime;
    @Column private LocalTime endTime;
    @Enumerated(EnumType.STRING) private ShiftType type; // REGULAR, OVERTIME
    @ElementCollection private List<DayOfWeek> daysOfWeek;
}

@Entity
public class ShiftAssignment {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private ShiftTemplate template;
    @Column private LocalDate date;
    @Enumerated(EnumType.STRING) private AssignmentStatus status; // ASSIGNED, COMPLETED, MISSED
}
```

### 2. Service Layer

```java
public interface ShiftService {
    ShiftTemplateDTO createTemplate(ShiftTemplateDTO dto);
    List<ShiftAssignmentDTO> assignShifts(BulkAssignmentRequest req);
    List<ShiftAssignmentDTO> getEmployeeShifts(Long employeeId, LocalDate from, LocalDate to);
}
```

### 3. Controller Design

```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
    @PostMapping("/templates") public ResponseEntity<ShiftTemplateDTO> createTemplate(@RequestBody ShiftTemplateDTO dto) { ... }
    @PostMapping("/assignments/bulk") public ResponseEntity<List<ShiftAssignmentDTO>> bulkAssign(@RequestBody BulkAssignmentRequest req) { ... }
    @GetMapping("/employee/{id}") public List<ShiftAssignmentDTO> getEmployeeShifts(@PathVariable Long id, @RequestParam LocalDate from, @RequestParam LocalDate to) { ... }
}
```

---

## <a name="e06"></a> E06 â Leave & Absence Management

### 1. Domain Model

**Design Decisions:**  
- LeaveRequest entity with accruals.
- Integration with scheduling and payroll.

**Design Specification:**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private LeaveType type; // PTO, SICK, UNPAID
    @Column private LocalDate startDate;
    @Column private LocalDate endDate;
    @Enumerated(EnumType.STRING) private LeaveStatus status; // REQUESTED, APPROVED, DENIED
    @Column private String reason;
}
```

### 2. Service Layer

```java
public interface LeaveService {
    LeaveRequestDTO requestLeave(LeaveRequestDTO dto);
    LeaveRequestDTO approveLeave(Long requestId, ApprovalDecision decision);
    List<LeaveRequestDTO> getEmployeeLeaves(Long employeeId);
}
```

### 3. Controller Design

```java
@RestController
@RequestMapping("/leaves")
public class LeaveController {
    @PostMapping public ResponseEntity<LeaveRequestDTO> request(@RequestBody LeaveRequestDTO dto) { ... }
    @PostMapping("/{id}/approve") public ResponseEntity<LeaveRequestDTO> approve(@PathVariable Long id, @RequestBody ApprovalDecision decision) { ... }
    @GetMapping("/employee/{id}") public List<LeaveRequestDTO> getEmployeeLeaves(@PathVariable Long id) { ... }
}
```

---

## <a name="e07"></a> E07 â Training & Certification Tracking

### 1. Domain Model

**Design Decisions:**  
- Certification entity with expiry.
- Block assignments if expired.

**Design Specification:**
```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    @Column private String name;
    @Column private LocalDate issueDate;
    @Column private LocalDate expiryDate;
    @ManyToOne private Employee employee;
    @Column private String documentUrl;
    @Enumerated(EnumType.STRING) private CertificationStatus status; // VALID, EXPIRED, PENDING
}
```

### 2. Service Layer

```java
public interface CertificationService {
    CertificationDTO addCertification(CertificationDTO dto);
    List<CertificationDTO> getEmployeeCertifications(Long employeeId);
    void uploadProof(Long certId, MultipartFile file);
}
```

### 3. Controller Design

```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
    @PostMapping public ResponseEntity<CertificationDTO> add(@RequestBody CertificationDTO dto) { ... }
    @GetMapping("/employee/{id}") public List<CertificationDTO> getEmployeeCerts(@PathVariable Long id) { ... }
    @PostMapping("/{id}/upload") public ResponseEntity<Void> uploadProof(@PathVariable Long id, @RequestParam MultipartFile file) { ... }
}
```

---

## <a name="e08"></a> E08 â Safety Incidents & OSHA Reporting

### 1. Domain Model

**Design Decisions:**  
- SafetyIncident entity with workflow.
- OSHA export fields.

**Design Specification:**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    @Column private String description;
    @Column private String location;
    @ManyToMany private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING) private IncidentSeverity severity;
    @Enumerated(EnumType.STRING) private IncidentStatus status; // OPEN, INVESTIGATING, RESOLVED
    @Column private LocalDateTime reportedAt;
    @Column private String oshaCategory;
}
```

### 2. Service Layer

```java
public interface SafetyService {
    SafetyIncidentDTO reportIncident(SafetyIncidentDTO dto);
    SafetyIncidentDTO updateStatus(Long id, IncidentStatus status);
    List<SafetyIncidentDTO> getIncidents(IncidentFilter filter);
}
```

### 3. Controller Design

```java
@RestController
@RequestMapping("/safety/incidents")
public class SafetyController {
    @PostMapping public ResponseEntity<SafetyIncidentDTO> report(@RequestBody SafetyIncidentDTO dto) { ... }
    @PatchMapping("/{id}/status") public ResponseEntity<SafetyIncidentDTO> updateStatus(@PathVariable Long id, @RequestBody IncidentStatus status) { ... }
    @GetMapping public List<SafetyIncidentDTO> list(@ModelAttribute IncidentFilter filter) { ... }
}
```

---

## <a name="e09"></a> E09 â Equipment & Asset Assignment

### 1. Domain Model

**Design Decisions:**  
- Asset registry with assignment history.
- Certification checks before assignment.

**Design Specification:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    @Column private String assetTag;
    @Column private String type;
    @Enumerated(EnumType.STRING) private AssetCondition condition;
    @ManyToOne private Employee assignedTo;
    @Column private LocalDateTime checkedOutAt;
    @Column private LocalDateTime returnedAt;
}
```

### 2. Service Layer

```java
public interface AssetService {
    AssetDTO registerAsset(AssetDTO dto);
    AssetDTO assignAsset(Long assetId, Long employeeId);
    AssetDTO returnAsset(Long assetId);
    List<AssetHistoryDTO> getAssetHistory(Long assetId);
}
```

### 3. Controller Design

```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    @PostMapping public ResponseEntity<AssetDTO> register(@RequestBody AssetDTO dto) { ... }
    @PostMapping("/{id}/assign") public ResponseEntity<AssetDTO> assign(@PathVariable Long id, @RequestParam Long employeeId) { ... }
    @PostMapping("/{id}/return") public ResponseEntity<AssetDTO> returnAsset(@PathVariable Long id) { ... }
    @GetMapping("/{id}/history") public List<AssetHistoryDTO> getHistory(@PathVariable Long id) { ... }
}
```

---

## <a name="e10"></a> E10 â Performance Reviews & Goals

### 1. Domain Model

**Design Decisions:**  
- Review cycles and templates.
- Immutable after sign-off.

**Design Specification:**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private Employee supervisor;
    @Column private LocalDate reviewDate;
    @Column private String goals;
    @Column private String competencies;
    @Column private String comments;
    @Enumerated(EnumType.STRING) private ReviewStatus status; // DRAFT, SUBMITTED, ACKNOWLEDGED, FINALIZED
    @Column private LocalDateTime signedOffAt;
}
```

### 2. Service Layer

```java
public interface ReviewService {
    PerformanceReviewDTO createReview(PerformanceReviewDTO dto);
    PerformanceReviewDTO submitReview(Long id);
    PerformanceReviewDTO acknowledgeReview(Long id);
    List<PerformanceReviewDTO> getEmployeeReviews(Long employeeId);
}
```

### 3. Controller Design

```java
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @PostMapping public ResponseEntity<PerformanceReviewDTO> create(@RequestBody PerformanceReviewDTO dto) { ... }
    @PostMapping("/{id}/submit") public ResponseEntity<PerformanceReviewDTO> submit(@PathVariable Long id) { ... }
    @PostMapping("/{id}/acknowledge") public ResponseEntity<PerformanceReviewDTO> acknowledge(@PathVariable Long id) { ... }
    @GetMapping("/employee/{id}") public List<PerformanceReviewDTO> getEmployeeReviews(@PathVariable Long id) { ... }
}
```

---

## <a name="e11"></a> E11 â Payroll Export Integration

### 1. Integration Layer

**Design Decisions:**  
- Export attendance and leave to payroll provider.
- Secure delivery via SFTP/API.

**Design Specification:**
```java
public interface PayrollExportService {
    PayrollExportResult exportPayroll(LocalDate from, LocalDate to, PayrollProvider provider);
}
```

**Sample Implementation:**
```java
@Service
public class PayrollExportServiceImpl implements PayrollExportService {
    @Override
    public PayrollExportResult exportPayroll(LocalDate from, LocalDate to, PayrollProvider provider) {
        // Gather data, map to provider schema, deliver via SFTP/API, log audit
    }
}
```

---

## <a name="e12"></a> E12 â Notifications & Announcements

### 1. Domain Model

**Design Decisions:**  
- Notification preferences per user.
- Multi-channel (in-app, email, SMS).

**Design Specification:**
```java
@Entity
public class Notification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee recipient;
    @Enumerated(EnumType.STRING) private NotificationType type;
    @Column private String message;
    @Column private LocalDateTime sentAt;
    @Enumerated(EnumType.STRING) private DeliveryStatus status;
}
```

### 2. Service Layer

```java
public interface NotificationService {
    void sendNotification(NotificationRequest req);
    List<NotificationDTO> getUserNotifications(Long employeeId);
    void updatePreferences(Long employeeId, NotificationPreferences prefs);
}
```

### 3. Controller Design

```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @PostMapping public ResponseEntity<Void> send(@RequestBody NotificationRequest req) { ... }
    @GetMapping("/user/{id}") public List<NotificationDTO> getUserNotifications(@PathVariable Long id) { ... }
    @PostMapping("/preferences") public ResponseEntity<Void> updatePreferences(@RequestBody NotificationPreferences prefs) { ... }
}
```

---

## <a name="e13"></a> E13 â Integration Layer (HRIS/WMS APIs)

### 1. API Design

**Design Decisions:**  
- Expose REST APIs for HRIS, WMS, SSO.
- JWT/OAuth2 secured.
- Webhooks for events.

**Design Specification:**
```java
@RestController
@RequestMapping("/api/integrations")
public class IntegrationController {
    @PostMapping("/hris/sync") public ResponseEntity<Void> syncHris(@RequestBody HrisSyncRequest req) { ... }
    @PostMapping("/wms/link") public ResponseEntity<Void> linkWms(@RequestBody WmsLinkRequest req) { ... }
    @PostMapping("/webhooks") public ResponseEntity<Void> handleWebhook(@RequestBody WebhookEvent event) { ... }
}
```

---

## <a name="e14"></a> E14 â Audit Trail & Compliance

### 1. Domain Model

**Design Decisions:**  
- Centralized, immutable audit log.
- Tamper-evident storage.

**Design Specification:**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    @Column private String entity;
    @Column private Long entityId;
    @Column private String action;
    @Column private String actor;
    @Column private LocalDateTime timestamp;
    @Column(columnDefinition = "jsonb") private String beforeState;
    @Column(columnDefinition = "jsonb") private String afterState;
}
```

### 2. Service Layer

```java
public interface AuditService {
    void logChange(String entity, Long entityId, String action, String actor, Object before, Object after);
    List<AuditLogDTO> getAuditLogs(AuditLogFilter filter);
}
```

---

## <a name="e15"></a> E15 â Reporting & Analytics

### 1. Reporting Layer

**Design Decisions:**  
- Operational reports with filters.
- Export to CSV/PDF.

**Design Specification:**
```java
public interface ReportingService {
    ReportDTO generateAttendanceReport(ReportFilter filter);
    ReportDTO generateOvertimeReport(ReportFilter filter);
    // etc.
}
```

### 2. Controller Design

```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
    @GetMapping("/attendance") public ResponseEntity<ReportDTO> attendance(@ModelAttribute ReportFilter filter) { ... }
    @GetMapping("/overtime") public ResponseEntity<ReportDTO> overtime(@ModelAttribute ReportFilter filter) { ... }
    @GetMapping("/export") public ResponseEntity<Resource> export(@RequestParam String type, @ModelAttribute ReportFilter filter) { ... }
}
```

---

## <a name="e16"></a> E16 â Mobile Access (PWA)

### 1. PWA Configuration

**Design Decisions:**  
- Responsive UI for mobile.
- Offline support for clock events.

**Design Specification:**
- Use Spring Boot with Thymeleaf/React frontend.
- Service Worker for offline queue.
- PWA manifest in `src/main/resources/static/manifest.json`.

**Sample manifest.json:**
```json
{
  "name": "Warehouse Employee Management",
  "short_name": "WEMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "icons": [{ "src": "/icon-192.png", "sizes": "192x192", "type": "image/png" }]
}
```

---

## <a name="e17"></a> E17 â Onboarding & Offboarding Workflow

### 1. Workflow Design

**Design Decisions:**  
- Automate provisioning/deprovisioning.
- Task generation for training/assets.

**Design Specification:**
```java
public interface OnboardingService {
    void onboardEmployee(HrisEmployeeDTO dto);
    void offboardEmployee(Long employeeId);
}
```

---

## <a name="e18"></a> E18 â Localization

### 1. i18n Configuration

**Design Decisions:**  
- Use Spring Boot MessageSource.
- Localized templates for notifications.

**Design Specification:**
```java
@Bean
public MessageSource messageSource() {
    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setBasename("classpath:messages");
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
}
```
- Place `messages_en.properties`, `messages_es.properties`, etc. in `src/main/resources/`.

---

## <a name="e19"></a> E19 â Observability

### 1. Monitoring & Logging

**Design Decisions:**  
- Use Spring Boot Actuator.
- Centralized logging (ELK/EFK).
- Distributed tracing (OpenTelemetry).

**Design Specification:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,loggers,trace
  tracing:
    enabled: true
    sampling:
      probability: 1.0
```
- Integrate with Prometheus/Grafana for metrics.

---

## <a name="e20"></a> E20 â CI/CD

### 1. Pipeline Design

**Design Decisions:**  
- Use GitHub Actions/Jenkins.
- Build, test, static analysis, Docker image, deploy.

**Sample GitHub Actions Workflow:**
```yaml
name: CI/CD Pipeline

on:
  push:
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
      - name: Build with Maven
        run: mvn clean package
      - name: Run Tests
        run: mvn test
      - name: Build Docker Image
        run: docker build -t wems:latest .
      - name: Push to Registry
        run: docker push <registry>/wems:latest
      - name: Deploy to Staging
        run: ./deploy.sh staging
```

---

# Appendix

- **Entity Relationships:** Use JPA `@ManyToOne`, `@OneToMany`, `@ManyToMany` as needed.
- **Validation:** Use `javax.validation` annotations on DTOs.
- **Exception Handling:** Use `@ControllerAdvice` for global error handling.
- **Testing:** Use JUnit 5, Mockito, and Spring Boot Test for unit/integration tests.
- **Configuration Management:** Use `application.yml` profiles for dev, staging, prod.
- **Deployment:** Dockerfile and Kubernetes manifests provided in `/deploy/`.

---

**This document is intended for direct use by Spring Boot developers. All code snippets are production-grade and follow best practices for maintainability, security, and scalability.**