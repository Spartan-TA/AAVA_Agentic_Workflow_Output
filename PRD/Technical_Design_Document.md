# Warehouse Employee Management System â Low-Level Technical Design Document

This document provides a comprehensive low-level technical design for all 20 epics of the Warehouse Employee Management System, adhering to Spring Boot industry standards. Each section details the architecture, package structure, domain models, service/repository/controller specifications, configuration, integration points, and code snippets for each epic.

---

## Section: E01 â Project Scaffolding & Domain Setup

**Description:**
Initialize a standardized Spring Boot (Maven) project structure with core modules, database migration, and monitoring.

**Design Specification:**
- **Architecture:** Layered (Controller, Service, Repository, Domain, Config)
- **Package Structure:**
  - `com.wms`
    - `employee`
    - `attendance`
    - `shift`
    - `safety`
    - `config`
    - `common`
- **Modules:** Employee, Scheduling, Attendance, Safety
- **DB Migration:** Flyway/Liquibase
- **Monitoring:** Spring Boot Actuator

**Sample Implementation:**
```java
@SpringBootApplication
public class WarehouseEmployeeMgmtApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmployeeMgmtApplication.class, args);
    }
}
```
**application.yml**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wms
    username: wms
    password: secret
  flyway:
    enabled: true
    locations: classpath:db/migration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```
---

## Section: E02 â Employee Master Data (CRUD)

**Description:**
Centralized CRUD management for employee records.

**Design Specification:**
- **Entity:** `Employee` (id, badgeId, name, role, department, shiftGroup, hireDate, status, deleted)
- **Repository:** `EmployeeRepository extends JpaRepository<Employee, Long>`
- **Service:** `EmployeeService` (CRUD, filtering, soft-delete)
- **Controller:** `/employees` (RESTful endpoints)
- **DTOs:** `EmployeeDTO`, `EmployeeCreateRequest`, `EmployeeUpdateRequest`
- **Validation:** Unique badgeId, soft-delete, pagination, filtering

**Sample Implementation:**
```java
@Entity
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
    private String status;
    private boolean deleted = false;
    // getters/setters
}
```
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping public EmployeeDTO create(@RequestBody @Valid EmployeeCreateRequest req) { ... }
    @GetMapping public Page<EmployeeDTO> list(Pageable pageable, @RequestParam Map<String,String> filters) { ... }
    @GetMapping("/{id}") public EmployeeDTO get(@PathVariable Long id) { ... }
    @PutMapping("/{id}") public EmployeeDTO update(@PathVariable Long id, @RequestBody @Valid EmployeeUpdateRequest req) { ... }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { ... }
}
```
---

## Section: E03 â Role-Based Access Control (RBAC)

**Description:**
Secure endpoints and data access using Spring Security with roles and row-level constraints.

**Design Specification:**
- **Roles:** ADMIN, HR, SUPERVISOR, WORKER
- **Security:** Method/endpoint security, row-level filtering
- **Config:** API key/OAuth2 toggle via `application.yml`
- **Tests:** Security rules covered

**Sample Implementation:**
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .oauth2Login();
    }
}
```
```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('SUPERVISOR') and @employeeSecurity.isTeamMember(#id, authentication))")
public EmployeeDTO getEmployee(Long id) { ... }
```
---

## Section: E04 â Time & Attendance (Clock In/Out)

**Description:**
APIs for clock-in/out, geofencing, device capture, shift association, and corrections workflow.

**Design Specification:**
- **Entity:** `AttendanceEvent` (id, employee, type, timestamp, deviceId, location, shift, status)
- **Repository:** `AttendanceEventRepository`
- **Service:** Clock-in/out logic, missed punch handling, corrections
- **Controller:** `/attendance/clock-in`, `/attendance/clock-out`, `/attendance/corrections`
- **Reports:** Export CSV

**Sample Implementation:**
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne private Employee employee;
    private String type; // CLOCK_IN, CLOCK_OUT
    private LocalDateTime timestamp;
    private String deviceId;
    private String location;
    @ManyToOne private Shift shift;
    private String status; // NORMAL, CORRECTION
}
```
```java
@PostMapping("/attendance/clock-in")
public ResponseEntity<?> clockIn(@RequestBody ClockInRequest req) { ... }
```
---

## Section: E05 â Shift & Schedule Management

**Description:**
Manage shift templates, rotations, overtime, assignments, blackout dates, and calendars.

**Design Specification:**
- **Entities:** `ShiftTemplate`, `ShiftAssignment`, `BlackoutDate`, `OperationCalendar`
- **Repository:** CRUD for all entities
- **Service:** Conflict detection, bulk assignment, audit logging
- **Controller:** `/shifts`, `/schedules`
- **Audit:** Generate entries on changes

**Sample Implementation:**
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    // ...
}
```
```java
@PostMapping("/shifts/assign")
public ResponseEntity<?> bulkAssign(@RequestBody ShiftAssignmentRequest req) { ... }
```
---

## Section: E06 â Leave & Absence Management

**Description:**
Request/approve leave, manage accruals, and integrate with scheduling/payroll.

**Design Specification:**
- **Entities:** `LeaveRequest`, `LeaveBalance`, `LeavePolicy`
- **Repository:** CRUD, filtering
- **Service:** Accrual calculation, approval workflow, integration hooks
- **Controller:** `/leaves`, `/leave-balances`
- **Integration:** Exclude from scheduling/payroll

**Sample Implementation:**
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne private Employee employee;
    private String type; // PTO, SICK, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // REQUESTED, APPROVED, DENIED
}
```
---

## Section: E07 â Training & Certification Tracking

**Description:**
Track certifications, expirations, renewals, and block unqualified assignments.

**Design Specification:**
- **Entities:** `Certification`, `EmployeeCertification`
- **Repository:** CRUD, expiry checks
- **Service:** Alerts, scheduling checks, document upload
- **Controller:** `/certifications`, `/employee-certifications`
- **Integration:** Block assignments if expired

**Sample Implementation:**
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private int validDays;
}
@Entity
public class EmployeeCertification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private Certification certification;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
}
```
---

## Section: E08 â Safety Incidents & OSHA Reporting

**Description:**
Record incidents, manage investigation workflow, and generate OSHA reports.

**Design Specification:**
- **Entities:** `SafetyIncident`, `IncidentStatus`
- **Repository:** CRUD, status workflow
- **Service:** Investigation, corrective actions, OSHA export
- **Controller:** `/safety/incidents`
- **Reporting:** OSHA 300/300A fields

**Sample Implementation:**
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String severity;
    private String location;
    private String description;
    @ManyToMany private List<Employee> involvedEmployees;
    private String status; // OPEN, INVESTIGATING, RESOLVED
}
```
---

## Section: E09 â Equipment & Asset Assignment

**Description:**
Assign and track assets, enforce certification, and maintain condition state.

**Design Specification:**
- **Entities:** `Asset`, `AssetAssignment`, `AssetCondition`
- **Repository:** CRUD, check-in/out, history
- **Service:** Assignment logic, overdue detection, certification checks
- **Controller:** `/assets`, `/asset-assignments`
- **Integration:** Block if certs invalid

**Sample Implementation:**
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type; // SCANNER, FORKLIFT, PPE
    private String serialNumber;
    private String condition;
}
@Entity
public class AssetAssignment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne private Asset asset;
    @ManyToOne private Employee employee;
    private LocalDateTime assignedAt;
    private LocalDateTime returnedAt;
}
```
---

## Section: E10 â Performance Reviews & Goals

**Description:**
Manage review templates, goals, ratings, and acknowledgements.

**Design Specification:**
- **Entities:** `PerformanceReview`, `ReviewTemplate`, `Goal`
- **Repository:** CRUD, workflow
- **Service:** Cycle management, PDF export, history
- **Controller:** `/reviews`, `/review-templates`
- **Security:** Role-based visibility

**Sample Implementation:**
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private ReviewTemplate template;
    private String status; // DRAFT, SUBMITTED, ACKNOWLEDGED
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String comments;
}
```
---

## Section: E11 â Payroll Export Integration

**Description:**
Generate payroll files, map to provider formats, and deliver securely.

**Design Specification:**
- **Entities:** `PayrollExport`, `PayrollMapping`
- **Repository:** Export history
- **Service:** File generation, SFTP/API delivery, retry logic
- **Controller:** `/payroll/exports`
- **Audit:** Log every export

**Sample Implementation:**
```java
@Entity
public class PayrollExport {
    @Id @GeneratedValue
    private Long id;
    private LocalDate exportDate;
    private String provider;
    private String status; // SUCCESS, FAILED
    private String fileUrl;
}
```
---

## Section: E12 â Notifications & Announcements

**Description:**
Send in-app/email/SMS notifications, manage templates, and track delivery.

**Design Specification:**
- **Entities:** `Notification`, `Announcement`, `NotificationPreference`
- **Repository:** CRUD, delivery tracking
- **Service:** Template rendering, rate limiting, localization
- **Controller:** `/notifications`, `/announcements`
- **Integration:** Email/SMS providers

**Sample Implementation:**
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne private Employee recipient;
    private String channel; // EMAIL, SMS, IN_APP
    private String template;
    private String status; // SENT, FAILED
    private LocalDateTime sentAt;
}
```
---

## Section: E13 â Integration Layer (HRIS/WMS APIs)

**Description:**
Expose REST APIs and connectors for HRIS, WMS, and SSO.

**Design Specification:**
- **Entities:** `IntegrationEvent`, `HRISMapping`, `WMSMapping`
- **Repository:** Sync jobs, webhook logs
- **Service:** JWT/OAuth2 security, idempotency, OpenAPI docs
- **Controller:** `/api/integrations`, `/api/webhooks`
- **Integration:** HRIS, WMS, IDP

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/integrations")
public class IntegrationController {
    @PostMapping("/hris/sync")
    public ResponseEntity<?> syncHris(@RequestBody HrisSyncRequest req) { ... }
}
```
---

## Section: E14 â Audit Trail & Compliance

**Description:**
Centralized, tamper-evident audit logging for sensitive changes.

**Design Specification:**
- **Entities:** `AuditLog`
- **Repository:** Append-only, immutable
- **Service:** Log create/update/delete, export, coverage tests
- **Controller:** `/audit/logs`
- **Security:** Export by date/user/entity

**Sample Implementation:**
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String entity;
    private Long entityId;
    private String action; // CREATE, UPDATE, DELETE
    private String actor;
    private LocalDateTime timestamp;
    @Lob private String before;
    @Lob private String after;
}
```
---

## Section: E15 â Reporting & Analytics

**Description:**
Operational reports, exports, and dashboards for key metrics.

**Design Specification:**
- **Entities:** (Virtual, or materialized views)
- **Service:** Report generation, filtering, export (CSV/PDF), metrics endpoints
- **Controller:** `/reports`, `/metrics`
- **Security:** Role-based access

**Sample Implementation:**
```java
@GetMapping("/reports/attendance")
public ResponseEntity<Resource> exportAttendance(@RequestParam Map<String,String> filters) { ... }
```
---

## Section: E16 â Mobile Access (PWA)

**Description:**
Responsive, offline-friendly PWA for core worker flows.

**Design Specification:**
- **Frontend:** Thymeleaf/React/Vue (served from `/mobile`)
- **Backend:** REST APIs for clock-in/out, schedules, leave, announcements
- **Service:** Offline queue, conflict resolution
- **Manifest:** `/manifest.json`
- **Controller:** `/mobile/**`

**Sample Implementation:**
```java
@GetMapping("/mobile/schedule")
public ScheduleDTO getMySchedule(Authentication auth) { ... }
```
---

## Section: E17 â Onboarding & Offboarding Workflow

**Description:**
Automate provisioning, training, asset assignment, and deprovisioning.

**Design Specification:**
- **Entities:** `OnboardingTask`, `OffboardingTask`
- **Repository:** CRUD, workflow
- **Service:** Task generation, HRIS sync, asset collection
- **Controller:** `/onboarding`, `/offboarding`
- **Integration:** HRIS, asset, schedule modules

**Sample Implementation:**
```java
@Entity
public class OnboardingTask {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne private Employee employee;
    private String type; // TRAINING, ASSET_ASSIGNMENT
    private String status; // PENDING, COMPLETED
}
```
---

## Section: E18 â Localization & Multi-Language

**Description:**
Support English, Spanish, and configurable additional languages; date/time/number formats; currency for payroll exports.

**Design Specification:**
- **Config:** `messages_{lang}.properties`
- **Service:** Locale resolution, template translation
- **Controller:** Locale endpoints, Accept-Language header support
- **Fallback:** English as default
- **Testing:** Cover at least 2 languages

**Sample Implementation:**
```java
@Bean
public LocaleResolver localeResolver() {
    SessionLocaleResolver slr = new SessionLocaleResolver();
    slr.setDefaultLocale(Locale.ENGLISH);
    return slr;
}

@Bean
public MessageSource messageSource() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasename("messages");
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
}
```

**messages_en.properties:**
```properties
employee.name=Name
employee.badge=Badge ID
employee.department=Department
```

**messages_es.properties:**
```properties
employee.name=Nombre
employee.badge=ID de Placa
employee.department=Departamento
```
---

## Section: E19 â AI-Powered Scheduling Optimization

**Description:**
Suggest optimal shift assignments based on historical attendance, skills, certifications, and forecasted demand; explain recommendations.

**Design Specification:**
- **Service:** `AiSchedulingService` (Spring Bean)
- **Integration:** Shift, leave, certification, attendance data
- **Controller:** `/scheduling/ai/suggestions`
- **ML Model:** Trained on historical data, monthly retraining
- **Output:** Ranked suggestions with confidence scores
- **Workflow:** Supervisors can accept/reject suggestions

**Sample Implementation:**
```java
@Service
public class AiSchedulingService {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private ShiftTemplateRepository shiftTemplateRepository;
    
    @Autowired
    private AttendanceEventRepository attendanceRepository;
    
    @Autowired
    private CertificationRepository certificationRepository;
    
    public List<ScheduleSuggestion> suggestOptimalSchedule(
            LocalDate startDate, 
            LocalDate endDate,
            String department) {
        
        // Fetch historical data
        List<Employee> employees = employeeRepository.findByDepartment(department);
        List<AttendanceEvent> historicalAttendance = 
            attendanceRepository.findByDateRange(startDate.minusMonths(6), startDate);
        
        // ML model inference
        List<ScheduleSuggestion> suggestions = mlModel.predict(
            employees, 
            historicalAttendance,
            startDate,
            endDate
        );
        
        // Rank by confidence and constraints
        return suggestions.stream()
            .filter(s -> s.getConfidence() > 0.7)
            .sorted(Comparator.comparing(ScheduleSuggestion::getConfidence).reversed())
            .collect(Collectors.toList());
    }
}
```

```java
@RestController
@RequestMapping("/scheduling/ai")
public class AiSchedulingController {
    
    @Autowired
    private AiSchedulingService aiSchedulingService;
    
    @GetMapping("/suggestions")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<List<ScheduleSuggestion>> getSuggestions(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam String department) {
        
        List<ScheduleSuggestion> suggestions = 
            aiSchedulingService.suggestOptimalSchedule(startDate, endDate, department);
        
        return ResponseEntity.ok(suggestions);
    }
    
    @PostMapping("/suggestions/{id}/accept")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<?> acceptSuggestion(@PathVariable Long id) {
        // Apply the suggested schedule
        return ResponseEntity.ok().build();
    }
}
```

**ScheduleSuggestion DTO:**
```java
public class ScheduleSuggestion {
    private Long id;
    private Employee employee;
    private ShiftTemplate shift;
    private LocalDate date;
    private Double confidence;
    private String explanation;
    private Map<String, Object> metrics;
    
    // getters/setters
}
```
---

## Section: E20 â CI/CD & Observability

**Description:**
GitHub Actions for build/test/deploy; SonarQube quality gates; Prometheus/Grafana metrics; structured logging (JSON); distributed tracing.

**Design Specification:**
- **CI/CD:** GitHub Actions/Jenkins
- **Quality:** SonarQube, code coverage â¥ 80%
- **Monitoring:** Prometheus, Grafana, Spring Boot Actuator
- **Logging:** Logback with JSON format
- **Tracing:** Spring Cloud Sleuth + Zipkin
- **Deployment:** Docker, Kubernetes
- **Secrets:** GitHub Secrets, Vault

**Sample Implementation:**

**.github/workflows/ci.yml:**
```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

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
        cache: maven
    
    - name: Build with Maven
      run: mvn clean install -DskipTests
    
    - name: Run Tests
      run: mvn test
    
    - name: Code Coverage
      run: mvn jacoco:report
    
    - name: SonarQube Scan
      env:
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
      run: |
        mvn sonar:sonar           -Dsonar.projectKey=warehouse-mgmt           -Dsonar.host.url=${{ secrets.SONAR_HOST_URL }}           -Dsonar.login=${{ secrets.SONAR_TOKEN }}
    
    - name: Build Docker Image
      run: docker build -t wms:${{ github.sha }} .
    
    - name: Push to Registry
      if: github.ref == 'refs/heads/main'
      run: |
        echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
        docker tag wms:${{ github.sha }} ${{ secrets.DOCKER_REGISTRY }}/wms:latest
        docker push ${{ secrets.DOCKER_REGISTRY }}/wms:latest
    
    - name: Deploy to Staging
      if: github.ref == 'refs/heads/develop'
      run: |
        kubectl set image deployment/wms wms=${{ secrets.DOCKER_REGISTRY }}/wms:${{ github.sha }} -n staging
```

**Dockerfile:**
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/warehouse-mgmt-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**application-prod.yml:**
```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    root: INFO
    com.wms: DEBUG
  pattern:
    console: '{"timestamp":"%d{ISO8601}","level":"%p","thread":"%t","class":"%c{1}","message":"%m"}%n'
```

**Prometheus Configuration (prometheus.yml):**
```yaml
scrape_configs:
  - job_name: 'warehouse-mgmt'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['wms:8080']
```

**Distributed Tracing (pom.xml):**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-sleuth-zipkin</artifactId>
</dependency>
```

---

## Architecture Overview

### System Architecture Diagram

```
âââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ
â                         Presentation Layer                       â
â  ââââââââââââââââ  ââââââââââââââââ  ââââââââââââââââ         â
â  â   Web UI     â  â  Mobile PWA  â  â  REST APIs   â         â
â  ââââââââââââââââ  ââââââââââââââââ  ââââââââââââââââ         â
âââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ
                              â
âââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ
â                      Security Layer (E03)                        â
â         Spring Security + OAuth2 + RBAC + Row-Level             â
âââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ
                              â
âââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ
â                        Controller Layer                          â
â  ââââââââââââ ââââââââââââ ââââââââââââ ââââââââââââ          â
â  â Employee â âAttendanceâ â  Shift   â â  Safety  â          â
â  âControllerâ âControllerâ âControllerâ âControllerâ  ...     â
â  ââââââââââââ ââââââââââââ ââââââââââââ ââââââââââââ          â
âââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ
                              â
âââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ
â                         Service Layer                            â
â  ââââââââââââ ââââââââââââ ââââââââââââ ââââââââââââ          â
â  â Employee â âAttendanceâ â  Shift   â â  Safety  â          â
â  â Service  â â Service  â â Service  â â Service  â  ...     â
â  ââââââââââââ ââââââââââââ ââââââââââââ ââââââââââââ          â
âââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ
                              â
âââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ
â                       Repository Layer                           â
â  ââââââââââââ ââââââââââââ ââââââââââââ ââââââââââââ          â
â  â Employee â âAttendanceâ â  Shift   â â  Safety  â          â
â  â   Repo   â â   Repo   â â   Repo   â â   Repo   â  ...     â
â  ââââââââââââ ââââââââââââ ââââââââââââ ââââââââââââ          â
âââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ
                              â
âââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ
â                         Domain Layer                             â
â  ââââââââââââ ââââââââââââ ââââââââââââ ââââââââââââ          â
â  â Employee â âAttendanceâ â  Shift   â â  Safety  â          â
â  â  Entity  â â  Event   â â Template â â Incident â  ...     â
â  ââââââââââââ ââââââââââââ ââââââââââââ ââââââââââââ          â
âââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ
                              â
âââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ
â                    Data Persistence Layer                        â
â              PostgreSQL + Flyway/Liquibase                       â
âââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ

âââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ
â                    Cross-Cutting Concerns                        â
â  ââââââââââââ ââââââââââââ ââââââââââââ ââââââââââââ          â
â  â  Audit   â âNotificationâ âIntegrationâ â   AI    â          â
â  â  (E14)   â â   (E12)   â â   (E13)   â â  (E19)  â          â
â  ââââââââââââ ââââââââââââ ââââââââââââ ââââââââââââ          â
âââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ

âââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ
â                    External Integrations                         â
â  ââââââââââââ ââââââââââââ ââââââââââââ ââââââââââââ          â
â  â   HRIS   â â   WMS    â â  Payroll â âEmail/SMS â          â
â  â  System  â â  System  â â Provider â â Gateway  â          â
â  ââââââââââââ ââââââââââââ ââââââââââââ ââââââââââââ          â
âââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââââ
```

### Package Structure

```
com.wms
âââ config
â   âââ SecurityConfig.java
â   âââ DatabaseConfig.java
â   âââ ActuatorConfig.java
â   âââ LocaleConfig.java
âââ common
â   âââ dto
â   âââ exception
â   âââ util
â   âââ constants
âââ employee
â   âââ domain
â   â   âââ Employee.java
â   âââ repository
â   â   âââ EmployeeRepository.java
â   âââ service
â   â   âââ EmployeeService.java
â   âââ controller
â   â   âââ EmployeeController.java
â   âââ dto
â       âââ EmployeeDTO.java
â       âââ EmployeeCreateRequest.java
â       âââ EmployeeUpdateRequest.java
âââ attendance
â   âââ domain
â   â   âââ AttendanceEvent.java
â   âââ repository
â   â   âââ AttendanceEventRepository.java
â   âââ service
â   â   âââ AttendanceService.java
â   âââ controller
â       âââ AttendanceController.java
âââ shift
â   âââ domain
â   â   âââ ShiftTemplate.java
â   â   âââ ShiftAssignment.java
â   â   âââ BlackoutDate.java
â   âââ repository
â   âââ service
â   â   âââ ShiftService.java
â   âââ controller
â       âââ ShiftController.java
âââ leave
â   âââ domain
â   â   âââ LeaveRequest.java
â   â   âââ LeaveBalance.java
â   âââ repository
â   âââ service
â   âââ controller
âââ certification
â   âââ domain
â   â   âââ Certification.java
â   â   âââ EmployeeCertification.java
â   âââ repository
â   âââ service
â   âââ controller
âââ safety
â   âââ domain
â   â   âââ SafetyIncident.java
â   âââ repository
â   âââ service
â   âââ controller
âââ asset
â   âââ domain
â   â   âââ Asset.java
â   â   âââ AssetAssignment.java
â   âââ repository
â   âââ service
â   âââ controller
âââ performance
â   âââ domain
â   â   âââ PerformanceReview.java
â   âââ repository
â   âââ service
â   âââ controller
âââ payroll
â   âââ domain
â   â   âââ PayrollExport.java
â   âââ repository
â   âââ service
â   âââ controller
âââ notification
â   âââ domain
â   â   âââ Notification.java
â   âââ repository
â   âââ service
â   âââ controller
âââ integration
â   âââ hris
â   âââ wms
â   âââ payroll
âââ audit
â   âââ domain
â   â   âââ AuditLog.java
â   âââ repository
â   âââ service
â   âââ controller
âââ reporting
â   âââ service
â   âââ controller
âââ mobile
â   âââ controller
âââ onboarding
â   âââ domain
â   âââ service
â   âââ controller
âââ ai
    âââ service
    â   âââ AiSchedulingService.java
    âââ controller
        âââ AiSchedulingController.java
```

---

## Database Schema Overview

### Core Tables

**employees**
- id (PK)
- badge_id (UNIQUE)
- name
- role
- department
- shift_group
- hire_date
- status
- deleted
- created_at
- updated_at

**attendance_events**
- id (PK)
- employee_id (FK)
- type (CLOCK_IN, CLOCK_OUT)
- timestamp
- device_id
- location
- shift_id (FK)
- status
- created_at

**shift_templates**
- id (PK)
- name
- start_time
- end_time
- recurring
- created_at
- updated_at

**shift_assignments**
- id (PK)
- employee_id (FK)
- shift_template_id (FK)
- date
- status
- created_at

**leave_requests**
- id (PK)
- employee_id (FK)
- type
- start_date
- end_date
- status
- created_at
- updated_at

**certifications**
- id (PK)
- name
- valid_days
- created_at

**employee_certifications**
- id (PK)
- employee_id (FK)
- certification_id (FK)
- issue_date
- expiry_date
- document_url
- created_at

**safety_incidents**
- id (PK)
- severity
- location
- description
- status
- created_at
- updated_at

**assets**
- id (PK)
- type
- serial_number
- condition
- created_at

**asset_assignments**
- id (PK)
- asset_id (FK)
- employee_id (FK)
- assigned_at
- returned_at

**performance_reviews**
- id (PK)
- employee_id (FK)
- template_id (FK)
- status
- period_start
- period_end
- comments
- created_at
- updated_at

**audit_logs**
- id (PK)
- entity
- entity_id
- action
- actor
- timestamp
- before (JSON)
- after (JSON)

---

## Security Implementation Details

### Authentication Flow

1. **API Key Authentication:**
```java
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) {
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && validateApiKey(apiKey)) {
            // Set authentication
        }
        filterChain.doFilter(request, response);
    }
}
```

2. **OAuth2 Authentication:**
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${OAUTH_CLIENT_ID}
            client-secret: ${OAUTH_CLIENT_SECRET}
            scope: openid,profile,email
```

### Authorization Rules

| Endpoint | ADMIN | HR | SUPERVISOR | WORKER |
|----------|-------|----|-----------:|--------|
| POST /employees | â | â | â | â |
| GET /employees | â | â | â (team) | â |
| PUT /employees/{id} | â | â | â | â |
| DELETE /employees/{id} | â | â | â | â |
| POST /attendance/clock-in | â | â | â | â |
| GET /attendance | â | â | â (team) | â (self) |
| POST /shifts/assign | â | â | â | â |
| GET /shifts | â | â | â | â (assigned) |
| POST /leaves | â | â | â | â |
| PUT /leaves/{id}/approve | â | â | â | â |
| GET /safety/incidents | â | â | â | â |
| POST /safety/incidents | â | â | â | â |
| GET /audit/logs | â | â | â | â |
| GET /reports | â | â | â (team) | â |

---

## API Documentation Standards

### OpenAPI Specification

All endpoints must be documented using OpenAPI 3.0 with:
- Request/response schemas
- Example payloads
- Error responses
- Security requirements

**Example:**
```java
@Operation(
    summary = "Create a new employee",
    description = "Creates a new employee record with the provided details",
    security = @SecurityRequirement(name = "bearer-jwt")
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "201",
        description = "Employee created successfully",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = EmployeeDTO.class)
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid input",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )
    ),
    @ApiResponse(
        responseCode = "409",
        description = "Badge ID already exists"
    )
})
@PostMapping("/employees")
public ResponseEntity<EmployeeDTO> createEmployee(
    @Valid @RequestBody EmployeeCreateRequest request) {
    // Implementation
}
```

---

## Testing Strategy

### Unit Tests
- Service layer logic
- Validation rules
- Business calculations
- Coverage target: â¥ 80%

### Integration Tests
- API endpoints
- Database operations
- Security rules
- External integrations

### Example Test:
```java
@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "HR")
    void testCreateEmployee() throws Exception {
        mockMvc.perform(post("/employees")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{"name":"John Doe","badgeId":"12345"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("John Doe"));
    }
    
    @Test
    @WithMockUser(roles = "WORKER")
    void testCreateEmployee_Forbidden() throws Exception {
        mockMvc.perform(post("/employees")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{"name":"John Doe","badgeId":"12345"}"))
            .andExpect(status().isForbidden());
    }
}
```

---

## Performance Considerations

### Database Optimization
- Indexes on frequently queried columns (badge_id, employee_id, date ranges)
- Connection pooling (HikariCP)
- Query optimization with JPA Criteria API
- Pagination for large result sets

### Caching Strategy
```java
@Cacheable(value = "employees", key = "#id")
public EmployeeDTO getEmployee(Long id) {
    // Implementation
}

@CacheEvict(value = "employees", key = "#id")
public void updateEmployee(Long id, EmployeeUpdateRequest request) {
    // Implementation
}
```

### Async Processing
```java
@Async
public CompletableFuture<PayrollExport> generatePayrollExport(LocalDate date) {
    // Long-running export process
    return CompletableFuture.completedFuture(export);
}
```

---

## Deployment Architecture

### Container Orchestration (Kubernetes)

**deployment.yaml:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: wms-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: wms-api
  template:
    metadata:
      labels:
        app: wms-api
    spec:
      containers:
      - name: wms-api
        image: registry.example.com/wms:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_URL
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: url
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
```

---

## Monitoring & Alerting

### Key Metrics to Monitor

1. **Application Metrics:**
   - Request rate
   - Response time (p50, p95, p99)
   - Error rate
   - Active sessions

2. **Business Metrics:**
   - Clock-in/out events per hour
   - Failed authentication attempts
   - Certification expiration alerts
   - Safety incident reports

3. **Infrastructure Metrics:**
   - CPU usage
   - Memory usage
   - Database connections
   - Disk I/O

### Grafana Dashboard Example
```json
{
  "dashboard": {
    "title": "WMS Application Metrics",
    "panels": [
      {
        "title": "Request Rate",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count[5m])"
          }
        ]
      },
      {
        "title": "Response Time (p95)",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, http_server_requests_seconds_bucket)"
          }
        ]
      }
    ]
  }
}
```

---

## Disaster Recovery & Business Continuity

### Backup Strategy
- **Database:** Daily full backups, hourly incremental
- **Retention:** 30 days
- **Testing:** Monthly restore tests

### High Availability
- Multi-region deployment
- Database replication (primary-replica)
- Load balancing across instances
- Auto-scaling based on load

---

## Compliance & Regulatory Requirements

### Data Privacy (GDPR/CCPA)
- PII encryption at rest and in transit
- Right to access/delete personal data
- Data retention policies
- Consent management

### OSHA Compliance
- Incident reporting within 24 hours
- Annual summary generation (300A)
- Record retention for 5 years

### SOC 2 Compliance
- Audit logging for all sensitive operations
- Access control reviews
- Encryption standards
- Incident response procedures

---

## Migration Strategy

### Phase 1: Foundation (Sprints 1-2)
- E01: Project scaffolding
- E02: Employee CRUD
- E03: RBAC

### Phase 2: Core Features (Sprints 3-7)
- E04: Time & Attendance
- E05: Shift Management
- E06: Leave Management
- E07: Training & Certification
- E08: Safety Incidents

### Phase 3: Advanced Features (Sprints 8-10)
- E09: Equipment Management
- E10: Performance Reviews
- E11: Payroll Integration
- E12: Notifications
- E13: Integration Layer

### Phase 4: Compliance & Analytics (Sprints 11-13)
- E14: Audit Trail
- E15: Reporting & Analytics
- E16: Mobile PWA
- E17: Onboarding/Offboarding

### Phase 5: Optimization (Sprints 14-15)
- E18: Localization
- E19: AI Scheduling
- E20: CI/CD & Observability

---

## Conclusion

This comprehensive low-level technical design document provides a complete blueprint for implementing the Warehouse Employee Management System using Spring Boot best practices. Each epic has been detailed with:

â Architecture overview
â Package structure
â Domain models and relationships
â Service layer specifications
â Repository layer specifications
â Controller specifications with REST endpoints
â Configuration and security settings
â Integration points
â Code snippets and implementation examples

The design follows industry standards including:
- Layered architecture
- Domain-Driven Design (DDD)
- RESTful API conventions
- Security best practices
- Observability and monitoring
- CI/CD automation
- Compliance requirements

**Total Estimated Effort:** 220 story points (~11 sprints, 22 weeks)

**Next Steps:**
1. Review and approve technical design
2. Set up development environment
3. Begin Sprint 1 implementation
4. Establish CI/CD pipeline
5. Configure monitoring and alerting

---

**Document Version:** 1.0
**Last Updated:** 2024
**Status:** Ready for Implementation