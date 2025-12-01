# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

---

## E01 - Project Scaffolding

### Section: Spring Boot Project Initialization

Description: Establishes the foundational structure for the EMS application using Spring Boot, Maven, and standardized package organization.

Design Specification:
- Base package: `com.warehouse.ems`
- Core modules: `employee`, `scheduling`, `attendance`, `safety`
- Maven multi-module setup (if needed)
- README with build/run steps
- Default port: 8080

Sample Implementation:
```java
// Directory structure
com/
  warehouse/
    ems/
      employee/
      scheduling/
      attendance/
      safety/
```
README.md:
```
# Warehouse EMS
## Build
mvn clean install
## Run
mvn spring-boot:run
## Health Check
GET http://localhost:8080/actuator/health
```

---

### Section: Database Migration Setup

Description: Ensures all database schema changes are versioned and repeatable using Flyway or Liquibase.

Design Specification:
- Add Flyway/Liquibase dependency in `pom.xml`
- Place migration scripts in `src/main/resources/db/migration`
- Baseline migration for initial schema

Sample Implementation:
```xml
<!-- pom.xml -->
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-core</artifactId>
</dependency>
```
```yaml
# application.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

---

### Section: Actuator Health Endpoint

Description: Enables health monitoring for DevOps and automated systems.

Design Specification:
- Add Spring Boot Actuator dependency
- Expose `/actuator/health` endpoint

Sample Implementation:
```xml
<!-- pom.xml -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

---

## E02 - Employee Master Data

### Section: Employee Entity Design

Description: Defines the core Employee domain model with all required fields and relationships.

Design Specification:
- Entity: `Employee`
- Fields: `id`, `name`, `badgeId`, `role`, `department`, `shiftGroup`, `hireDate`, `status`, `deleted`
- Unique constraint on `badgeId`
- Soft-delete via `deleted` boolean

Sample Implementation:
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badgeId"))
public class Employee {
  @Id @GeneratedValue private Long id;
  private String name;
  private String badgeId;
  @Enumerated(EnumType.STRING) private Role role;
  private String department;
  private String shiftGroup;
  private LocalDate hireDate;
  @Enumerated(EnumType.STRING) private Status status;
  private boolean deleted = false;
  // getters/setters
}
```

---

### Section: Employee Repository

Description: Spring Data JPA repository for Employee CRUD and filtering.

Design Specification:
- Interface: `EmployeeRepository extends JpaRepository<Employee, Long>`
- Custom methods for filtering, pagination, and soft-delete

Sample Implementation:
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
  Page<Employee> findAllByDeletedFalse(Pageable pageable);
  // Filtering methods as needed
}
```

---

### Section: Employee Service Layer

Description: Business logic for employee creation, update, soft-delete, and retrieval.

Design Specification:
- Class: `EmployeeService`
- Methods: `createEmployee`, `updateEmployee`, `softDeleteEmployee`, `listEmployees`
- Annotated with `@Service` and `@Transactional`

Sample Implementation:
```java
@Service
public class EmployeeService {
  @Autowired private EmployeeRepository repo;

  @Transactional
  public Employee createEmployee(EmployeeDTO dto) { /* ... */ }

  @Transactional
  public Employee updateEmployee(Long id, EmployeeDTO dto) { /* ... */ }

  @Transactional
  public void softDeleteEmployee(Long id) { /* ... */ }

  public Page<Employee> listEmployees(Pageable pageable, EmployeeFilter filter) { /* ... */ }
}
```

---

### Section: Employee REST Controller

Description: Exposes RESTful endpoints for employee CRUD operations.

Design Specification:
- Class: `EmployeeController`
- Endpoints: `POST /employees`, `PUT /employees/{id}`, `PATCH /employees/{id}`, `DELETE /employees/{id}`, `GET /employees`
- Uses DTOs for request/response

Sample Implementation:
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
  @Autowired private EmployeeService service;

  @PostMapping public ResponseEntity<EmployeeDTO> create(@RequestBody EmployeeDTO dto) { /* ... */ }
  @PutMapping("/{id}") public ResponseEntity<EmployeeDTO> update(@PathVariable Long id, @RequestBody EmployeeDTO dto) { /* ... */ }
  @PatchMapping("/{id}") public ResponseEntity<EmployeeDTO> partialUpdate(@PathVariable Long id, @RequestBody Map<String, Object> updates) { /* ... */ }
  @DeleteMapping("/{id}") public ResponseEntity<Void> softDelete(@PathVariable Long id) { /* ... */ }
  @GetMapping public Page<EmployeeDTO> list(Pageable pageable, EmployeeFilter filter) { /* ... */ }
}
```

---

### Section: Employee DTOs and Mappers

Description: DTOs for API requests/responses and mappers for entity conversion.

Design Specification:
- DTO: `EmployeeDTO`
- Mapper: `EmployeeMapper` (MapStruct or manual)

Sample Implementation:
```java
public class EmployeeDTO {
  private Long id;
  private String name;
  private String badgeId;
  private String role;
  private String department;
  private String shiftGroup;
  private LocalDate hireDate;
  private String status;
  // getters/setters
}
```
```java
@Mapper(componentModel = "spring")
public interface EmployeeMapper {
  EmployeeDTO toDto(Employee entity);
  Employee toEntity(EmployeeDTO dto);
}
```

---

## E03 - Role-Based Access Control (RBAC)

### Section: Spring Security Configuration

Description: Implements RBAC with roles and method/endpoint security.

Design Specification:
- Roles: `ADMIN`, `HR`, `SUPERVISOR`, `WORKER`
- Security config: `SecurityConfig` class
- API key/OAuth2 toggle via `application.yml`
- Row-level security for supervisors

Sample Implementation:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .authorizeRequests()
        .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
        .antMatchers("/attendance/**").hasAnyRole("WORKER", "SUPERVISOR", "HR")
        .anyRequest().authenticated()
      .and()
        .oauth2ResourceServer().jwt();
    // API key toggle logic
  }
}
```
```yaml
security:
  auth-type: oauth2 # or apikey
```

---

### Section: Row-Level Security

Description: Restricts supervisors to only access employees in their team.

Design Specification:
- Custom repository query filtering by supervisor/team
- Method-level security with `@PreAuthorize`

Sample Implementation:
```java
@PreAuthorize("hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(authentication, #employeeId)")
public EmployeeDTO getEmployee(Long employeeId) { /* ... */ }
```

---

### Section: Security Automated Tests

Description: Automated tests for all security rules.

Design Specification:
- Use Spring Security Test
- Test unauthorized (401) and forbidden (403) scenarios

Sample Implementation:
```java
@Test
@WithMockUser(roles = "WORKER")
public void testAdminEndpointForbidden() throws Exception {
  mockMvc.perform(post("/admin/endpoint")).andExpect(status().isForbidden());
}
```

---

## E04 - Time & Attendance

### Section: Attendance Entity Design

Description: Captures clock-in/out events, hours worked, and corrections.

Design Specification:
- Entity: `AttendanceEvent`
- Fields: `id`, `employeeId`, `clockIn`, `clockOut`, `deviceId`, `location`, `hoursWorked`, `correctionStatus`

Sample Implementation:
```java
@Entity
public class AttendanceEvent {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private LocalDateTime clockIn;
  private LocalDateTime clockOut;
  private String deviceId;
  private String location;
  private Double hoursWorked;
  @Enumerated(EnumType.STRING) private CorrectionStatus correctionStatus;
}
```

---

### Section: Attendance Repository

Description: JPA repository for attendance events.

Design Specification:
- Interface: `AttendanceRepository extends JpaRepository<AttendanceEvent, Long>`
- Methods for filtering by employee, date, correction status

Sample Implementation:
```java
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {
  List<AttendanceEvent> findByEmployeeAndClockInBetween(Employee employee, LocalDateTime start, LocalDateTime end);
}
```

---

### Section: Attendance Service Layer

Description: Handles clock-in/out logic, validation, and corrections workflow.

Design Specification:
- Methods: `clockIn`, `clockOut`, `submitCorrection`, `approveCorrection`, `exportReport`
- Validation for missed punches

Sample Implementation:
```java
@Service
public class AttendanceService {
  @Transactional
  public AttendanceEvent clockIn(Long employeeId, ClockInDTO dto) { /* ... */ }
  @Transactional
  public AttendanceEvent clockOut(Long employeeId, ClockOutDTO dto) { /* ... */ }
  @Transactional
  public void submitCorrection(Long eventId, CorrectionDTO dto) { /* ... */ }
  @Transactional
  public void approveCorrection(Long eventId) { /* ... */ }
  public List<AttendanceReportDTO> exportReport(LocalDate start, LocalDate end) { /* ... */ }
}
```

---

### Section: Attendance REST Controller

Description: Endpoints for clock-in/out, corrections, and report export.

Design Specification:
- Endpoints: `POST /attendance/clock-in`, `POST /attendance/clock-out`, `POST /attendance/corrections`, `GET /attendance/report`

Sample Implementation:
```java
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
  @Autowired private AttendanceService service;

  @PostMapping("/clock-in") public ResponseEntity<?> clockIn(@RequestBody ClockInDTO dto) { /* ... */ }
  @PostMapping("/clock-out") public ResponseEntity<?> clockOut(@RequestBody ClockOutDTO dto) { /* ... */ }
  @PostMapping("/corrections") public ResponseEntity<?> submitCorrection(@RequestBody CorrectionDTO dto) { /* ... */ }
  @GetMapping("/report") public ResponseEntity<Resource> exportReport(@RequestParam LocalDate start, @RequestParam LocalDate end) { /* ... */ }
}
```

---

## E05 - Shift & Schedule Management

### Section: Shift Entity Design

Description: Models shift templates, rotations, and assignments.

Design Specification:
- Entity: `ShiftTemplate`, `ShiftAssignment`
- Fields: `id`, `name`, `startTime`, `endTime`, `rotationPattern`, `assignedEmployees`

Sample Implementation:
```java
@Entity
public class ShiftTemplate {
  @Id @GeneratedValue private Long id;
  private String name;
  private LocalTime startTime;
  private LocalTime endTime;
  private String rotationPattern;
}
@Entity
public class ShiftAssignment {
  @Id @GeneratedValue private Long id;
  @ManyToOne private ShiftTemplate template;
  @ManyToOne private Employee employee;
  private LocalDate date;
  private boolean conflictDetected;
}
```

---

### Section: Shift Repository

Description: JPA repositories for shift templates and assignments.

Design Specification:
- Interfaces: `ShiftTemplateRepository`, `ShiftAssignmentRepository`

Sample Implementation:
```java
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {}
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
  List<ShiftAssignment> findByEmployeeAndDateBetween(Employee employee, LocalDate start, LocalDate end);
}
```

---

### Section: Shift Service Layer

Description: Business logic for creating templates, assigning shifts, and conflict detection.

Design Specification:
- Methods: `createTemplate`, `assignShift`, `bulkAssignShifts`, `detectConflicts`, `viewUpcomingShifts`

Sample Implementation:
```java
@Service
public class ShiftService {
  @Transactional
  public ShiftTemplate createTemplate(ShiftTemplateDTO dto) { /* ... */ }
  @Transactional
  public ShiftAssignment assignShift(Long employeeId, ShiftAssignmentDTO dto) { /* ... */ }
  @Transactional
  public List<ShiftAssignment> bulkAssignShifts(List<ShiftAssignmentDTO> dtos) { /* ... */ }
  public List<ShiftAssignmentDTO> viewUpcomingShifts(Long employeeId) { /* ... */ }
}
```

---

### Section: Shift REST Controller

Description: Endpoints for managing shifts and assignments.

Design Specification:
- Endpoints: `POST /shifts/templates`, `POST /shifts/assign`, `POST /shifts/bulk-assign`, `GET /shifts/upcoming`

Sample Implementation:
```java
@RestController
@RequestMapping("/shifts")
public class ShiftController {
  @Autowired private ShiftService service;

  @PostMapping("/templates") public ResponseEntity<?> createTemplate(@RequestBody ShiftTemplateDTO dto) { /* ... */ }
  @PostMapping("/assign") public ResponseEntity<?> assignShift(@RequestBody ShiftAssignmentDTO dto) { /* ... */ }
  @PostMapping("/bulk-assign") public ResponseEntity<?> bulkAssign(@RequestBody List<ShiftAssignmentDTO> dtos) { /* ... */ }
  @GetMapping("/upcoming") public ResponseEntity<List<ShiftAssignmentDTO>> upcoming(@RequestParam Long employeeId) { /* ... */ }
}
```

---

## E06 - Leave & Absence Management

### Section: Leave Entity Design

Description: Models leave requests, approvals, and accrual balances.

Design Specification:
- Entity: `LeaveRequest`, `LeavePolicy`
- Fields: `id`, `employee`, `type`, `startDate`, `endDate`, `status`, `accrualBalance`

Sample Implementation:
```java
@Entity
public class LeaveRequest {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  @Enumerated(EnumType.STRING) private LeaveType type;
  private LocalDate startDate;
  private LocalDate endDate;
  @Enumerated(EnumType.STRING) private LeaveStatus status;
}
@Entity
public class LeavePolicy {
  @Id @GeneratedValue private Long id;
  private LeaveType type;
  private Double accrualRate;
}
```

---

### Section: Leave Repository

Description: JPA repositories for leave requests and policies.

Design Specification:
- Interfaces: `LeaveRequestRepository`, `LeavePolicyRepository`

Sample Implementation:
```java
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
  List<LeaveRequest> findByEmployeeAndStatus(Employee employee, LeaveStatus status);
}
public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, Long> {}
```

---

### Section: Leave Service Layer

Description: Handles leave request, approval, accrual tracking, and export.

Design Specification:
- Methods: `requestLeave`, `approveLeave`, `denyLeave`, `trackAccrual`, `exportApprovedLeaves`

Sample Implementation:
```java
@Service
public class LeaveService {
  @Transactional
  public LeaveRequest requestLeave(Long employeeId, LeaveRequestDTO dto) { /* ... */ }
  @Transactional
  public void approveLeave(Long requestId) { /* ... */ }
  @Transactional
  public void denyLeave(Long requestId) { /* ... */ }
  public Double trackAccrual(Long employeeId, LeaveType type) { /* ... */ }
  public List<LeaveReportDTO> exportApprovedLeaves(LocalDate start, LocalDate end) { /* ... */ }
}
```

---

### Section: Leave REST Controller

Description: Endpoints for leave requests, approvals, and export.

Design Specification:
- Endpoints: `POST /leave/request`, `POST /leave/approve`, `POST /leave/deny`, `GET /leave/report`

Sample Implementation:
```java
@RestController
@RequestMapping("/leave")
public class LeaveController {
  @Autowired private LeaveService service;

  @PostMapping("/request") public ResponseEntity<?> requestLeave(@RequestBody LeaveRequestDTO dto) { /* ... */ }
  @PostMapping("/approve") public ResponseEntity<?> approveLeave(@RequestParam Long requestId) { /* ... */ }
  @PostMapping("/deny") public ResponseEntity<?> denyLeave(@RequestParam Long requestId) { /* ... */ }
  @GetMapping("/report") public ResponseEntity<Resource> exportReport(@RequestParam LocalDate start, @RequestParam LocalDate end) { /* ... */ }
}
```

---

## E07 - Training & Certification Tracking

### Section: Certification Entity Design

Description: Tracks employee certifications, expirations, and proof documents.

Design Specification:
- Entity: `Certification`
- Fields: `id`, `employee`, `type`, `issueDate`, `expiryDate`, `proofDocument`, `status`

Sample Implementation:
```java
@Entity
public class Certification {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  private String type;
  private LocalDate issueDate;
  private LocalDate expiryDate;
  private String proofDocumentUrl;
  @Enumerated(EnumType.STRING) private CertificationStatus status;
}
```

---

### Section: Certification Repository

Description: JPA repository for certifications.

Design Specification:
- Interface: `CertificationRepository extends JpaRepository<Certification, Long>`
- Methods for alerts and status checks

Sample Implementation:
```java
public interface CertificationRepository extends JpaRepository<Certification, Long> {
  List<Certification> findByExpiryDateBetween(LocalDate start, LocalDate end);
  List<Certification> findByEmployeeAndStatus(Employee employee, CertificationStatus status);
}
```

---

### Section: Certification Service Layer

Description: Handles CRUD, alerts, and assignment blocking.

Design Specification:
- Methods: `createCertification`, `updateCertification`, `alertExpiring`, `blockAssignment`

Sample Implementation:
```java
@Service
public class CertificationService {
  @Transactional
  public Certification createCertification(CertificationDTO dto) { /* ... */ }
  @Transactional
  public void updateCertification(Long id, CertificationDTO dto) { /* ... */ }
  public List<Certification> alertExpiring(int days) { /* ... */ }
  public boolean blockAssignment(Long employeeId, String certType) { /* ... */ }
}
```

---

### Section: Certification REST Controller

Description: Endpoints for certification management and alerts.

Design Specification:
- Endpoints: `POST /certifications`, `PUT /certifications/{id}`, `GET /certifications/expiring`

Sample Implementation:
```java
@RestController
@RequestMapping("/certifications")
public class CertificationController {
  @Autowired private CertificationService service;

  @PostMapping public ResponseEntity<?> create(@RequestBody CertificationDTO dto) { /* ... */ }
  @PutMapping("/{id}") public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CertificationDTO dto) { /* ... */ }
  @GetMapping("/expiring") public ResponseEntity<List<CertificationDTO>> expiring(@RequestParam int days) { /* ... */ }
}
```

---

## E08 - Safety Incidents & OSHA Reporting

### Section: Incident Entity Design

Description: Models safety incidents, near-misses, and investigation workflow.

Design Specification:
- Entity: `SafetyIncident`
- Fields: `id`, `severity`, `location`, `description`, `involvedEmployees`, `status`, `investigationNotes`

Sample Implementation:
```java
@Entity
public class SafetyIncident {
  @Id @GeneratedValue private Long id;
  @Enumerated(EnumType.STRING) private Severity severity;
  private String location;
  private String description;
  @ManyToMany private List<Employee> involvedEmployees;
  @Enumerated(EnumType.STRING) private IncidentStatus status;
  private String investigationNotes;
}
```

---

### Section: Incident Repository

Description: JPA repository for safety incidents.

Design Specification:
- Interface: `SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long>`

Sample Implementation:
```java
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, Long> {
  List<SafetyIncident> findByStatus(IncidentStatus status);
}
```

---

### Section: Incident Service Layer

Description: Handles incident recording, investigation, and OSHA reporting.

Design Specification:
- Methods: `recordIncident`, `updateStatus`, `generateOSHAReport`, `getMetrics`

Sample Implementation:
```java
@Service
public class SafetyService {
  @Transactional
  public SafetyIncident recordIncident(IncidentDTO dto) { /* ... */ }
  @Transactional
  public void updateStatus(Long id, IncidentStatus status, String notes) { /* ... */ }
  public OSHAReportDTO generateOSHAReport(int year) { /* ... */ }
  public SafetyMetricsDTO getMetrics(LocalDate start, LocalDate end) { /* ... */ }
}
```

---

### Section: Incident REST Controller

Description: Endpoints for incident management and OSHA reporting.

Design Specification:
- Endpoints: `POST /safety/incidents`, `PUT /safety/incidents/{id}`, `GET /safety/osha-report`, `GET /safety/metrics`

Sample Implementation:
```java
@RestController
@RequestMapping("/safety")
public class SafetyController {
  @Autowired private SafetyService service;

  @PostMapping("/incidents") public ResponseEntity<?> recordIncident(@RequestBody IncidentDTO dto) { /* ... */ }
  @PutMapping("/incidents/{id}") public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateDTO dto) { /* ... */ }
  @GetMapping("/osha-report") public ResponseEntity<OSHAReportDTO> oshaReport(@RequestParam int year) { /* ... */ }
  @GetMapping("/metrics") public ResponseEntity<SafetyMetricsDTO> metrics(@RequestParam LocalDate start, @RequestParam LocalDate end) { /* ... */ }
}
```

---

## E09 - Equipment & Asset Assignment

### Section: Asset Entity Design

Description: Models equipment/asset registry and checkout/return tracking.

Design Specification:
- Entity: `Asset`, `AssetCheckout`
- Fields: `id`, `type`, `serialNumber`, `condition`, `assignedEmployee`, `checkoutDate`, `returnDate`

Sample Implementation:
```java
@Entity
public class Asset {
  @Id @GeneratedValue private Long id;
  private String type;
  private String serialNumber;
  @Enumerated(EnumType.STRING) private AssetCondition condition;
}
@Entity
public class AssetCheckout {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Asset asset;
  @ManyToOne private Employee employee;
  private LocalDateTime checkoutDate;
  private LocalDateTime returnDate;
  private String conditionNotes;
}
```

---

### Section: Asset Repository

Description: JPA repositories for assets and checkouts.

Design Specification:
- Interfaces: `AssetRepository`, `AssetCheckoutRepository`

Sample Implementation:
```java
public interface AssetRepository extends JpaRepository<Asset, Long> {}
public interface AssetCheckoutRepository extends JpaRepository<AssetCheckout, Long> {
  List<AssetCheckout> findByEmployeeAndReturnDateIsNull(Employee employee);
  List<AssetCheckout> findByAsset(Asset asset);
}
```

---

### Section: Asset Service Layer

Description: Handles asset CRUD, checkout/return, and history tracking.

Design Specification:
- Methods: `createAsset`, `checkoutAsset`, `returnAsset`, `viewHistory`, `generateOverdueReport`

Sample Implementation:
```java
@Service
public class AssetService {
  @Transactional
  public Asset createAsset(AssetDTO dto) { /* ... */ }
  @Transactional
  public AssetCheckout checkoutAsset(Long assetId, Long employeeId) { /* ... */ }
  @Transactional
  public void returnAsset(Long checkoutId, String conditionNotes) { /* ... */ }
  public List<AssetCheckoutDTO> viewHistory(Long assetId) { /* ... */ }
  public List<AssetCheckoutDTO> generateOverdueReport() { /* ... */ }
}
```

---

### Section: Asset REST Controller

Description: Endpoints for asset management and checkout/return.

Design Specification:
- Endpoints: `POST /assets`, `POST /assets/checkout`, `POST /assets/return`, `GET /assets/history`, `GET /assets/overdue`

Sample Implementation:
```java
@RestController
@RequestMapping("/assets")
public class AssetController {
  @Autowired private AssetService service;

  @PostMapping public ResponseEntity<?> createAsset(@RequestBody AssetDTO dto) { /* ... */ }
  @PostMapping("/checkout") public ResponseEntity<?> checkout(@RequestBody CheckoutDTO dto) { /* ... */ }
  @PostMapping("/return") public ResponseEntity<?> returnAsset(@RequestBody ReturnDTO dto) { /* ... */ }
  @GetMapping("/history") public ResponseEntity<List<AssetCheckoutDTO>> history(@RequestParam Long assetId) { /* ... */ }
  @GetMapping("/overdue") public ResponseEntity<List<AssetCheckoutDTO>> overdue() { /* ... */ }
}
```

---

## E10 - Performance Reviews & Goals

### Section: Review Entity Design

Description: Models performance review templates, cycles, and acknowledgements.

Design Specification:
- Entity: `ReviewTemplate`, `PerformanceReview`
- Fields: `id`, `employee`, `template`, `ratings`, `comments`, `status`, `acknowledgedDate`

Sample Implementation:
```java
@Entity
public class ReviewTemplate {
  @Id @GeneratedValue private Long id;
  private String name;
  private String competencies;
  private String ratingScale;
}
@Entity
public class PerformanceReview {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  @ManyToOne private ReviewTemplate template;
  private String ratings;
  private String comments;
  @Enumerated(EnumType.STRING) private ReviewStatus status;
  private LocalDateTime acknowledgedDate;
}
```

---

### Section: Review Repository

Description: JPA repositories for review templates and performance reviews.

Design Specification:
- Interfaces: `ReviewTemplateRepository`, `PerformanceReviewRepository`

Sample Implementation:
```java
public interface ReviewTemplateRepository extends JpaRepository<ReviewTemplate, Long> {}
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
  List<PerformanceReview> findByEmployeeAndStatus(Employee employee, ReviewStatus status);
}
```

---

### Section: Review Service Layer

Description: Handles review creation, assignment, submission, and export.

Design Specification:
- Methods: `createTemplate`, `assignReview`, `submitReview`, `acknowledgeReview`, `exportPDF`

Sample Implementation:
```java
@Service
public class ReviewService {
  @Transactional
  public ReviewTemplate createTemplate(ReviewTemplateDTO dto) { /* ... */ }
  @Transactional
  public PerformanceReview assignReview(Long employeeId, Long templateId) { /* ... */ }
  @Transactional
  public void submitReview(Long reviewId, ReviewSubmissionDTO dto) { /* ... */ }
  @Transactional
  public void acknowledgeReview(Long reviewId) { /* ... */ }
  public byte[] exportPDF(Long reviewId) { /* ... */ }
}
```

---

### Section: Review REST Controller

Description: Endpoints for review management and export.

Design Specification:
- Endpoints: `POST /reviews/templates`, `POST /reviews/assign`, `POST /reviews/submit`, `POST /reviews/acknowledge`, `GET /reviews/export`

Sample Implementation:
```java
@RestController
@RequestMapping("/reviews")
public class ReviewController {
  @Autowired private ReviewService service;

  @PostMapping("/templates") public ResponseEntity<?> createTemplate(@RequestBody ReviewTemplateDTO dto) { /* ... */ }
  @PostMapping("/assign") public ResponseEntity<?> assignReview(@RequestBody AssignReviewDTO dto) { /* ... */ }
  @PostMapping("/submit") public ResponseEntity<?> submitReview(@RequestBody ReviewSubmissionDTO dto) { /* ... */ }
  @PostMapping("/acknowledge") public ResponseEntity<?> acknowledge(@RequestParam Long reviewId) { /* ... */ }
  @GetMapping("/export") public ResponseEntity<Resource> exportPDF(@RequestParam Long reviewId) { /* ... */ }
}
```

---

## E11 - Payroll Export Integration

### Section: Payroll Export Entity Design

Description: Models payroll export files and delivery status.

Design Specification:
- Entity: `PayrollExport`
- Fields: `id`, `exportDate`, `fileContent`, `deliveryStatus`, `retryCount`, `auditLog`

Sample Implementation:
```java
@Entity
public class PayrollExport {
  @Id @GeneratedValue private Long id;
  private LocalDateTime exportDate;
  @Lob private String fileContent;
  @Enumerated(EnumType.STRING) private DeliveryStatus deliveryStatus;
  private int retryCount;
  private String auditLog;
}
```

---

### Section: Payroll Export Repository

Description: JPA repository for payroll exports.

Design Specification:
- Interface: `PayrollExportRepository extends JpaRepository<PayrollExport, Long>`

Sample Implementation:
```java
public interface PayrollExportRepository extends JpaRepository<PayrollExport, Long> {
  List<PayrollExport> findByDeliveryStatus(DeliveryStatus status);
}
```

---

### Section: Payroll Export Service Layer

Description: Handles export generation, secure delivery, retry logic, and audit logging.

Design Specification:
- Methods: `generateExport`, `deliverExport`, `retryFailedDeliveries`, `auditLog`

Sample Implementation:
```java
@Service
public class PayrollExportService {
  @Transactional
  public PayrollExport generateExport(LocalDate start, LocalDate end) { /* ... */ }
  @Transactional
  public void deliverExport(Long exportId, DeliveryMethod method) { /* ... */ }
  @Scheduled(fixedDelay = 3600000)
  public void retryFailedDeliveries() { /* ... */ }
  public List<AuditLogDTO> auditLog(LocalDate start, LocalDate end) { /* ... */ }
}
```

---

### Section: Payroll Export REST Controller

Description: Endpoints for payroll export generation and delivery.

Design Specification:
- Endpoints: `POST /payroll/export`, `POST /payroll/deliver`, `GET /payroll/audit`

Sample Implementation:
```java
@RestController
@RequestMapping("/payroll")
public class PayrollController {
  @Autowired private PayrollExportService service;

  @PostMapping("/export") public ResponseEntity<?> generateExport(@RequestBody ExportRequestDTO dto) { /* ... */ }
  @PostMapping("/deliver") public ResponseEntity<?> deliver(@RequestBody DeliveryRequestDTO dto) { /* ... */ }
  @GetMapping("/audit") public ResponseEntity<List<AuditLogDTO>> audit(@RequestParam LocalDate start, @RequestParam LocalDate end) { /* ... */ }
}
```

---

## E12 - Notifications & Announcements

### Section: Notification Entity Design

Description: Models notifications, channels, and delivery status.

Design Specification:
- Entity: `Notification`, `NotificationPreference`
- Fields: `id`, `recipient`, `channel`, `message`, `deliveryStatus`, `quietHours`

Sample Implementation:
```java
@Entity
public class Notification {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee recipient;
  @Enumerated(EnumType.STRING) private NotificationChannel channel;
  private String message;
  @Enumerated(EnumType.STRING) private DeliveryStatus deliveryStatus;
}
@Entity
public class NotificationPreference {
  @Id @GeneratedValue private Long id;
  @ManyToOne private Employee employee;
  @Enumerated(EnumType.STRING) private NotificationChannel channel;
  private LocalTime quietHoursStart;
  private LocalTime quietHoursEnd;
}
```

---

### Section: Notification Repository

Description: JPA repositories for notifications and preferences.

Design Specification:
- Interfaces: `NotificationRepository`, `NotificationPreferenceRepository`

Sample Implementation:
```java
public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findByRecipientAndDeliveryStatus(Employee recipient, DeliveryStatus status);
}
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
  Optional<NotificationPreference> findByEmployeeAndChannel(Employee employee, NotificationChannel channel);
}
```

---

### Section: Notification Service Layer

Description: Handles notification delivery, localization, quiet hours, and rate limiting.

Design Specification:
- Methods: `sendNotification`, `localizeMessage`, `respectQuietHours`, `rateLimit`

Sample Implementation:
```java
@Service
public class NotificationService {
  @Async
  public void sendNotification(Long employeeId, String message, NotificationChannel channel) { /* ... */ }
  private String localizeMessage(String message, Locale locale) { /* ... */ }
  private boolean respectQuietHours(Long employeeId, NotificationChannel channel) { /* ... */ }
  private void rateLimit(Long employeeId) { /* ... */ }
}
```

---

### Section: Notification REST Controller

Description: Endpoints for notification management and preferences.

Design Specification:
- Endpoints: `POST /notifications/send`, `PUT /notifications/preferences`

Sample Implementation:
```java
@RestController
@RequestMapping("/notifications")
public class NotificationController {
  @Autowired private NotificationService service;

  @PostMapping("/send") public ResponseEntity<?> send(@RequestBody NotificationDTO dto) { /* ... */ }
  @PutMapping("/preferences") public ResponseEntity<?> updatePreferences(@RequestBody PreferenceDTO dto) { /* ... */ }
}
```

---

## E13 - Integration Layer (HRIS/WMS APIs)

### Section: Integration API Design

Description: Exposes secure REST APIs for HRIS and WMS integration.

Design Specification:
- Endpoints: `POST /api/hris/sync`, `POST /api/wms/sync`, `POST /api/webhooks`
- JWT/OAuth2 authentication

Sample Implementation:
```java
@RestController
@RequestMapping("/api")
public class IntegrationController {
  @Autowired private IntegrationService service;

  @PostMapping("/hris/sync") public ResponseEntity<?> hrisSync(@RequestBody HRISSyncDTO dto) { /* ... */ }
  @PostMapping("/wms/sync") public ResponseEntity<?> wmsSync(@RequestBody WMSSyncDTO dto) { /* ... */ }
  @PostMapping("/webhooks") public ResponseEntity<?> webhook(@RequestBody WebhookDTO dto) { /* ... */ }
}
```

---

### Section: Integration Service Layer

Description: Handles HRIS/WMS sync jobs and webhook delivery.

Design Specification:
- Methods: `syncHRIS`, `syncWMS`, `deliverWebhook`
- Idempotency for webhooks

Sample Implementation:
```java
@Service
public class IntegrationService {
  @Scheduled(cron = "0 0 2 * * ?")
  public void syncHRIS() { /* ... */ }
  @Scheduled(cron = "0 0 3 * * ?")
  public void syncWMS() { /* ... */ }
  @Async
  public void deliverWebhook(WebhookEvent event) { /* ... */ }
}
```

---

## E14 - Audit Trail & Compliance

### Section: Audit Log Entity Design

Description: Models immutable audit logs for sensitive changes.

Design Specification:
- Entity: `AuditLog`
- Fields: `id`, `actor`, `timestamp`, `entity`, `action`, `beforeValue`, `afterValue`

Sample Implementation:
```java
@Entity
@Immutable
public class AuditLog {
  @Id @GeneratedValue private Long id;
  private String actor;
  private LocalDateTime timestamp;
  private String entity;
  private String action;
  @Lob private String beforeValue;
  @Lob private String afterValue;
}
```

---

### Section: Audit Log Repository

Description: JPA repository for audit logs.

Design Specification:
- Interface: `AuditLogRepository extends JpaRepository<AuditLog, Long>`
- Methods for filtering and export

Sample Implementation:
```java
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
  List<AuditLog> findByActorAndTimestampBetween(String actor, LocalDateTime start, LocalDateTime end);
  List<AuditLog> findByEntityAndTimestampBetween(String entity, LocalDateTime start, LocalDateTime end);
}
```

---

### Section: Audit Log Service Layer

Description: Handles audit log creation and export.

Design Specification:
- Methods: `logChange`, `exportLogs`
- Annotated with `@Async` for non-blocking logging

Sample Implementation:
```java
@Service
public class AuditLogService {
  @Async
  public void logChange(String actor, String entity, String action, String before, String after) { /* ... */ }
  public List<AuditLogDTO> exportLogs(AuditLogFilter filter) { /* ... */ }
}
```

---

### Section: Audit Log REST Controller

Description: Endpoints for audit log export.

Design Specification:
- Endpoints: `GET /audit/logs`

Sample Implementation:
```java
@RestController
@RequestMapping("/audit")
public class AuditController {
  @Autowired private AuditLogService service;

  @GetMapping("/logs") public ResponseEntity<List<AuditLogDTO>> exportLogs(@RequestParam AuditLogFilter filter) { /* ... */ }
}
```

---

## E15 - Reporting & Analytics

### Section: Reporting Service Layer

Description: Generates operational reports and metrics.

Design Specification:
- Methods: `generateAttendanceReport`, `generateOvertimeReport`, `getCertificationStatus`, `getSafetyKPIs`, `exportLargeDataset`

Sample Implementation:
```java
@Service
public class ReportingService {
  public List<AttendanceReportDTO> generateAttendanceReport(ReportFilter filter) { /* ... */ }
  public List<OvertimeReportDTO> generateOvertimeReport(ReportFilter filter) { /* ... */ }
  public CertificationStatusDTO getCertificationStatus() { /* ... */ }
  public SafetyKPIDTO getSafetyKPIs(LocalDate start, LocalDate end) { /* ... */ }
  public byte[] exportLargeDataset(ReportFilter filter) { /* ... */ }
}
```

---

### Section: Reporting REST Controller

Description: Endpoints for report generation and export.

Design Specification:
- Endpoints: `GET /reports/attendance`, `GET /reports/overtime`, `GET /reports/certifications`, `GET /reports/safety`, `GET /reports/export`

Sample Implementation:
```java
@RestController
@RequestMapping("/reports")
public class ReportingController {
  @Autowired private ReportingService service;

  @GetMapping("/attendance") public ResponseEntity<List<AttendanceReportDTO>> attendance(@RequestParam ReportFilter filter) { /* ... */ }
  @GetMapping("/overtime") public ResponseEntity<List<OvertimeReportDTO>> overtime(@RequestParam ReportFilter filter) { /* ... */ }
  @GetMapping("/certifications") public ResponseEntity<CertificationStatusDTO> certifications() { /* ... */ }
  @GetMapping("/safety") public ResponseEntity<SafetyKPIDTO> safety(@RequestParam LocalDate start, @RequestParam LocalDate end) { /* ... */ }
  @GetMapping("/export") public ResponseEntity<Resource> export(@RequestParam ReportFilter filter) { /* ... */ }
}
```

---

## E16 - Mobile Access (PWA)

### Section: PWA Configuration

Description: Configures Progressive Web App manifest and service worker.

Design Specification:
- File: `manifest.json`
- Service worker for offline support

Sample Implementation:
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
```javascript
// service-worker.js
self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request).then(response => {
      return response || fetch(event.request);
    })
  );
});
```

---

### Section: Offline Queue Service

Description: Queues clock-in/out events offline and syncs when online.

Design Specification:
- IndexedDB for offline storage
- Sync on reconnection

Sample Implementation:
```javascript
// offline-queue.js
function queueClockEvent(event) {
  const db = await openDB('ems-offline', 1);
  await db.add('clock-events', event);
}
function syncQueue() {
  const db = await openDB('ems-offline', 1);
  const events = await db.getAll('clock-events');
  events.forEach(event => {
    fetch('/attendance/clock-in', { method: 'POST', body: JSON.stringify(event) })
      .then(() => db.delete('clock-events', event.id));
  });
}
```

---

## E17 - Onboarding & Offboarding Workflow

### Section: Onboarding Service Layer

Description: Automates provisioning for new hires.

Design Specification:
- Methods: `provisionNewHire`, `generateOnboardingTasks`

Sample Implementation:
```java
@Service
public class OnboardingService {
  @Transactional
  public void provisionNewHire(Long employeeId) { /* ... */ }
  @Transactional
  public List<Task> generateOnboardingTasks(Long employeeId) { /* ... */ }
}
```

---

### Section: Offboarding Service Layer

Description: Automates deprovisioning for terminations.

Design Specification:
- Methods: `deprovisionEmployee`, `completeOffboardingChecklist`

Sample Implementation:
```java
@Service
public class OffboardingService {
  @Transactional
  public void deprovisionEmployee(Long employeeId) { /* ... */ }
  @Transactional
  public void completeOffboardingChecklist(Long employeeId) { /* ... */ }
}
```

---

## E18 - Localization & Multi-Warehouse

### Section: Warehouse Entity Design

Description: Models multiple warehouses with distinct policies.

Design Specification:
- Entity: `Warehouse`
- Fields: `id`, `name`, `location`, `policies`, `calendar`

Sample Implementation:
```java
@Entity
public class Warehouse {
  @Id @GeneratedValue private Long id;
  private String name;
  private String location;
  @Lob private String policies;
  @Lob private String calendar;
}
```

---

### Section: Localization Configuration

Description: Configures UI language toggle and locale-specific formats.

Design Specification:
- MessageSource for localized strings
- LocaleResolver for language toggle

Sample Implementation:
```java
@Configuration
public class LocalizationConfig {
  @Bean
  public MessageSource messageSource() {
    ResourceBundleMessageSource source = new ResourceBundleMessageSource();
    source.setBasename("messages");
    return source;
  }
  @Bean
  public LocaleResolver localeResolver() {
    SessionLocaleResolver resolver = new SessionLocaleResolver();
    resolver.setDefaultLocale(Locale.US);
    return resolver;
  }
}
```

---

## E19 - Automated Testing & CI/CD

### Section: GitHub Actions Pipeline

Description: CI/CD pipeline for build, test, security scan, and deployment.

Design Specification:
- File: `.github/workflows/ci-cd.yml`
- Steps: checkout, build, test, security scan, deploy

Sample Implementation:
```yaml
name: CI/CD
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          java-version: '11'
      - name: Build with Maven
        run: mvn clean install
      - name: Run Tests
        run: mvn test
      - name: Security Scan
        run: mvn dependency-check:check
      - name: Deploy to Staging
        if: github.ref == 'refs/heads/main'
        run: ./deploy-staging.sh
```

---

### Section: Test Coverage Configuration

Description: Configures JaCoCo for 80% code coverage.

Design Specification:
- Plugin: JaCoCo Maven Plugin
- Coverage threshold: 80%

Sample Implementation:
```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.7</version>
  <executions>
    <execution>
      <goals>
        <goal>prepare-agent</goal>
      </goals>
    </execution>
    <execution>
      <id>report</id>
      <phase>test</phase>
      <goals>
        <goal>report</goal>
      </goals>
    </execution>
    <execution>
      <id>check</id>
      <goals>
        <goal>check</goal>
      </goals>
      <configuration>
        <rules>
          <rule>
            <element>BUNDLE</element>
            <limits>
              <limit>
                <counter>LINE</counter>
                <value>COVEREDRATIO</value>
                <minimum>0.80</minimum>
              </limit>
            </limits>
          </rule>
        </rules>
      </configuration>
    </execution>
  </executions>
</plugin>
```

---

## E20 - Documentation & Runbooks

### Section: OpenAPI Specification

Description: Auto-generates and publishes OpenAPI specs.

Design Specification:
- Dependency: SpringDoc OpenAPI
- Endpoint: `/v3/api-docs`

Sample Implementation:
```xml
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-ui</artifactId>
  <version>1.6.9</version>
</dependency>
```
```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

---

### Section: Architecture Diagrams

Description: Provides architecture diagrams in docs/ folder.

Design Specification:
- Diagrams: C4 model, UML
- Tools: PlantUML, Draw.io

Sample Implementation:
```
docs/
  architecture/
    context-diagram.png
    container-diagram.png
    component-diagram.png
```

---

### Section: Operational Runbooks

Description: Provides runbooks for common operational tasks.

Design Specification:
- Runbooks: deployment, rollback, troubleshooting
- Format: Markdown

Sample Implementation:
```markdown
# Deployment Runbook
## Steps
1. Build: `mvn clean package`
2. Deploy: `./deploy.sh`
3. Verify: `curl http://localhost:8080/actuator/health`
```

---

**END OF TECHNICAL DESIGN DOCUMENT**