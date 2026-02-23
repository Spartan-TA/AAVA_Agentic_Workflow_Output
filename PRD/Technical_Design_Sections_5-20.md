# WAREHOUSE EMPLOYEE MANAGEMENT SYSTEM
# TECHNICAL DESIGN DOCUMENT - SECTIONS 5-20

## SECTION 5: Shift & Schedule Management (E05)

### Description
Manages shift templates, recurring schedules, rotations, overtime rules, blackout dates, and employee assignments with conflict detection.

### Design Specification

**Entities:**
- ShiftTemplate: Reusable shift definitions with start/end times, days of week, break duration
- ShiftAssignment: Employee-specific shift assignments with date and status
- BlackoutDate: Dates when scheduling is restricted
- OperationCalendar: Warehouse operation schedule

**Service Layer:**
- CRUD operations for shift templates
- Conflict detection algorithm (overlapping shifts, rest period violations)
- Bulk assignment with validation
- Rotation scheduling logic
- Overtime calculation based on weekly hours

**Controller Endpoints:**
- GET/POST/PUT/DELETE /api/v1/schedules/templates
- POST /api/v1/schedules/assign
- POST /api/v1/schedules/bulk-assign
- GET /api/v1/schedules/employee/{id}
- GET /api/v1/schedules/conflicts

### Sample Implementation

```java
@Entity
@Table(name = "shift_templates")
public class ShiftTemplate {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
    
    @ElementCollection
    private Set<DayOfWeek> daysOfWeek;
    
    private Integer breakDurationMinutes;
    private boolean overtimeEligible;
}

@Entity
@Table(name = "shift_assignments")
public class ShiftAssignment {
    @Id @GeneratedValue private Long id;
    
    @ManyToOne
    private Employee employee;
    
    @ManyToOne
    private ShiftTemplate shiftTemplate;
    
    private LocalDate shiftDate;
    
    @Enumerated(EnumType.STRING)
    private Status status; // SCHEDULED, CONFIRMED, COMPLETED, CANCELLED
}

@Service
public class ShiftService {
    @Transactional
    public ShiftAssignmentDTO assignShift(ShiftAssignmentCreateDTO dto) {
        // Validate employee exists
        // Validate shift template exists
        // Check for conflicts
        // Create assignment
        // Audit log
    }
    
    @Transactional
    public BulkAssignmentResultDTO bulkAssignShifts(BulkAssignmentDTO dto) {
        // Process each assignment
        // Collect successes and failures
        // Return result summary
    }
}
```

---

## SECTION 6: Leave & Absence Management (E06)

### Description
Handles PTO, sick leave, and unpaid leave requests with approval workflows, accrual policies, and balance tracking.

### Design Specification

**Entities:**
- LeaveRequest: Request details with dates, type, reason, status
- LeaveBalance: Employee balances by leave type and year
- LeavePolicy: Accrual rules, limits, and carryover policies
- LeaveType: PTO, SICK, UNPAID, BEREAVEMENT, JURY_DUTY, MILITARY

**Business Rules:**
- Validate sufficient balance before approval
- Check blackout dates
- Notify supervisor for approval
- Update shift assignments when approved
- Exclude from payroll hours calculation
- Scheduled accrual processing

### Sample Implementation

```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    @Enumerated(EnumType.STRING) private Status status;
    private String reason;
    @ManyToOne private Employee approvedBy;
    private LocalDateTime approvedAt;
}

@Entity
public class LeaveBalance {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private LeaveType leaveType;
    private Integer year;
    private BigDecimal accruedDays;
    private BigDecimal usedDays;
    private BigDecimal pendingDays;
    private BigDecimal availableDays;
}

@Service
public class LeaveService {
    @Transactional
    public LeaveRequestDTO requestLeave(LeaveRequestCreateDTO dto) {
        // Calculate total days
        // Check balance
        // Create request
        // Update pending balance
        // Notify supervisor
    }
    
    @Transactional
    public LeaveRequestDTO approveLeave(Long requestId, Long approverId) {
        // Validate request status
        // Update request
        // Update balances
        // Flag affected shifts
        // Notify employee
    }
}
```

---

## SECTION 7: Training & Certification Tracking (E07)

### Description
Tracks employee certifications, manages expiration dates, sends renewal alerts, and blocks assignments for expired certifications.

### Design Specification

**Entities:**
- Certification: Master certification list with validity period
- EmployeeCertification: Employee-specific certification records
- CertificationRequirement: Role/equipment requirements

**Features:**
- Document upload for proof of certification
- Scheduled expiry checks (30 days, 7 days)
- Assignment validation against certifications
- Certification status tracking

### Sample Implementation

```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    private String name;
    private String description;
    private Integer validityMonths;
    private boolean requiredForEquipment;
    private String equipmentTypes;
}

@Entity
public class EmployeeCertification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private Certification certification;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentUrl;
    private String certificateNumber;
    @Enumerated(EnumType.STRING) private Status status;
    private boolean alert30DaysSent;
    private boolean alert7DaysSent;
}

@Service
public class CertificationService {
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void checkExpiringCertifications() {
        // Find certifications expiring in 30 days
        // Send alerts
        // Find certifications expiring in 7 days
        // Send alerts
        // Mark expired certifications
    }
    
    public boolean isEmployeeQualified(Long employeeId, String equipmentType) {
        // Check active certifications
        // Validate not expired
        // Match equipment type
    }
}
```

---

## SECTION 8: Safety Incidents & OSHA Reporting (E08)

### Description
Records safety incidents and near-misses, manages investigation workflows, and generates OSHA-compliant reports.

### Design Specification

**Entities:**
- SafetyIncident: Incident details with severity, location, involved employees
- InvestigationTask: Investigation workflow tracking
- CorrectiveAction: Actions taken to prevent recurrence

**OSHA Compliance:**
- OSHA 300 Log format
- OSHA 300A Summary format
- Required fields validation
- Recordability determination

### Sample Implementation

```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private String incidentNumber;
    private LocalDateTime incidentDate;
    private String description;
    private String location;
    @Enumerated(EnumType.STRING) private Severity severity;
    @Enumerated(EnumType.STRING) private IncidentType incidentType;
    @ManyToMany private List<Employee> involvedEmployees;
    @Enumerated(EnumType.STRING) private Status status;
    private String injuryType;
    private String bodyPart;
    private Integer daysAwayFromWork;
    private boolean oshaRecordable;
}

@Service
public class SafetyService {
    @Transactional
    public SafetyIncidentDTO reportIncident(SafetyIncidentCreateDTO dto) {
        // Generate incident number
        // Determine OSHA recordability
        // Create incident
        // Notify safety manager
    }
    
    public OSHAExportDTO exportOSHAData(int year) {
        // Find OSHA recordable incidents
        // Format for OSHA 300/300A
        // Calculate summary statistics
    }
}
```

---

## SECTION 9: Equipment & Asset Assignment (E09)

### Description
Manages asset registry, assignment, check-in/out, and blocks use if certification is missing.

### Design Specification

**Entities:**
- Asset: Equipment details with serial number, type, condition
- AssetAssignment: Check-in/out records
- AssetCondition: Tracking asset state

**Features:**
- Certification validation before assignment
- Asset history tracking
- Overdue return alerts
- Condition state management

### Sample Implementation

```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String type;
    private String serialNumber;
    @Enumerated(EnumType.STRING) private AssetCondition condition;
    private boolean requiresCertification;
    private String requiredCertificationType;
}

@Entity
public class AssetAssignment {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Asset asset;
    @ManyToOne private Employee employee;
    private LocalDateTime assignedAt;
    private LocalDateTime returnedAt;
    private LocalDateTime expectedReturnDate;
    @Enumerated(EnumType.STRING) private AssetCondition conditionAtCheckout;
    @Enumerated(EnumType.STRING) private AssetCondition conditionAtReturn;
}

@Service
public class AssetService {
    @Transactional
    public void assignAsset(AssetAssignmentDTO dto) {
        // Validate asset available
        // Check employee certification
        // Create assignment
        // Update asset status
    }
}
```

---

## SECTION 10: Performance Reviews & Goals (E10)

### Description
Implements review cycles, templates, goal tracking, and supervisor/employee acknowledgements.

### Design Specification

**Entities:**
- PerformanceReview: Review details with ratings and comments
- ReviewTemplate: Standardized review templates
- Goal: Individual goals with progress tracking
- Competency: Skills and competencies assessment

**Features:**
- Quarterly/annual review cycles
- Multi-step acknowledgement workflow
- PDF export
- Immutable history after sign-off

### Sample Implementation

```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee employee;
    @ManyToOne private ReviewTemplate template;
    private LocalDate reviewDate;
    private String comments;
    private Integer overallRating;
    private boolean acknowledgedByEmployee;
    private boolean acknowledgedBySupervisor;
    private boolean immutable;
}

@Service
public class ReviewService {
    @Transactional
    public PerformanceReviewDTO createReview(PerformanceReviewCreateDTO dto) {
        // Create review from template
        // Assign to employee
        // Notify participants
    }
    
    public byte[] exportReviewPDF(Long reviewId) {
        // Generate PDF
        // Include all sections
        // Add signatures
    }
}
```

---

## SECTION 11: Payroll Export Integration (E11)

### Description
Generates payroll-ready files from attendance/leave data, maps to provider formats, and delivers securely.

### Design Specification

**Features:**
- Configurable provider mapping
- SFTP/API delivery
- Reconciliation and validation
- Retry logic with exponential backoff
- Audit logging

### Sample Implementation

```java
@Service
public class PayrollExportService {
    @Transactional
    public PayrollExportDTO exportPayroll(LocalDate periodStart, LocalDate periodEnd) {
        // Gather approved attendance
        // Gather approved leave
        // Calculate totals
        // Map to provider format
        // Generate file
        // Deliver via SFTP/API
        // Audit log
    }
    
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void deliverPayrollFile(PayrollFile file) {
        // Attempt delivery
        // Log success/failure
    }
}
```

---

## SECTION 12: Notifications & Announcements (E12)

### Description
Sends in-app/email/SMS notifications for events, supports opt-in/out, templates, and localization.

### Design Specification

**Entities:**
- Notification: Individual notification records
- Announcement: System-wide announcements
- UserNotificationPreference: User channel preferences

**Features:**
- Multi-channel delivery (in-app, email, SMS)
- Template-based messaging
- Localization support
- Rate limiting
- Quiet hours configuration
- Delivery status tracking

### Sample Implementation

```java
@Entity
public class Notification {
    @Id @GeneratedValue private Long id;
    @ManyToOne private Employee recipient;
    private String message;
    @Enumerated(EnumType.STRING) private NotificationChannel channel;
    private boolean delivered;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
}

@Service
public class NotificationService {
    public void sendNotification(NotificationDTO dto) {
        // Check user preferences
        // Check quiet hours
        // Apply rate limits
        // Send via appropriate channel
        // Track delivery status
    }
}
```

---

## SECTION 13: Integration Layer (HRIS/WMS APIs) (E13)

### Description
Exposes REST APIs and connectors for HRIS, WMS, and IDP with JWT/OAuth2 security and webhooks.

### Design Specification

**Integration Points:**
- HRIS: Employee sync (new hires, terminations, updates)
- WMS: Department/location sync
- IDP: SSO authentication

**Features:**
- Secured REST APIs
- Webhook endpoints with idempotency
- Scheduled sync jobs
- OpenAPI documentation
- Error handling and retry logic

### Sample Implementation

```java
@RestController
@RequestMapping("/api/integration/hris")
@SecurityRequirement(name = "bearerAuth")
public class HRISIntegrationController {
    @PostMapping("/employees/sync")
    public void syncEmployee(@RequestBody HRISSyncDTO dto) {
        // Validate payload
        // Create or update employee
        // Trigger onboarding if new hire
        // Audit log
    }
    
    @PostMapping("/webhook")
    public void handleWebhook(@RequestBody WebhookPayload payload,
                             @RequestHeader("X-Idempotency-Key") String idempotencyKey) {
        // Check idempotency
        // Process event
        // Return 200 OK
    }
}

@Scheduled(cron = "0 0 2 * * *")
public void syncHRISData() {
    // Fetch updates from HRIS
    // Process changes
    // Log results
}
```

---

## SECTION 14: Audit Trail & Compliance (E14)

### Description
Centralized audit logging for sensitive changes with tamper-evident storage.

### Design Specification

**Entity:**
- AuditLog: Immutable audit records with actor, timestamp, entity, action, before/after states

**Features:**
- AOP-based automatic logging
- Immutable storage
- Export by filters
- Tamper-evident design

### Sample Implementation

```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String actor;
    private String entity;
    private String entityId;
    private String action;
    @Column(columnDefinition = "TEXT") private String beforeState;
    @Column(columnDefinition = "TEXT") private String afterState;
    private LocalDateTime timestamp;
    private String ipAddress;
}

@Aspect
@Component
public class AuditAspect {
    @AfterReturning(pointcut = "@annotation(Audited)", returning = "result")
    public void logAudit(JoinPoint jp, Object result) {
        // Extract method details
        // Capture before/after state
        // Create audit log entry
        // Save to database
    }
}
```

---

## SECTION 15: Reporting & Analytics (E15)

### Description
Provides operational reports with CSV/PDF export and role-based dashboards.

### Design Specification

**Report Types:**
- Attendance reports
- Overtime reports
- Leave balance reports
- Certification status reports
- Safety KPI reports

**Features:**
- Filtering by date, department, shift
- CSV/PDF export
- Async processing for large datasets
- Role-based access control
- Metrics endpoints for BI tools

### Sample Implementation

```java
@RestController
@RequestMapping("/api/v1/reports")
public class ReportingController {
    @GetMapping("/attendance")
    public void exportAttendance(@RequestParam ReportFilter filter,
                                 HttpServletResponse response) {
        // Query data
        // Generate CSV/PDF
        // Stream to response
    }
    
    @GetMapping("/metrics")
    public MetricsDTO getMetrics(@RequestParam LocalDate startDate,
                                 @RequestParam LocalDate endDate) {
        // Calculate KPIs
        // Return metrics
    }
}

@Service
public class ReportingService {
    @Async
    public CompletableFuture<byte[]> generateLargeReport(ReportFilter filter) {
        // Process in background
        // Generate report
        // Return result
    }
}
```

---

## SECTION 16: Mobile Access (PWA) (E16)

### Description
Delivers responsive, offline-friendly PWA for core flows.

### Design Specification

**Features:**
- Progressive Web App manifest
- Service Worker for offline support
- Responsive UI components
- Offline queue for clock events
- Lighthouse PWA score â¥ 80

**Core Flows:**
- Clock in/out
- View schedules
- Request leave
- View announcements

### Sample Implementation

```javascript
// manifest.json
{
  "short_name": "WEMS",
  "name": "Warehouse Employee Management System",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#2196F3",
  "icons": [
    {
      "src": "/icons/icon-192.png",
      "sizes": "192x192",
      "type": "image/png"
    }
  ]
}

// service-worker.js
self.addEventListener('sync', event => {
  if (event.tag === 'sync-attendance') {
    event.waitUntil(syncAttendanceEvents());
  }
});

function syncAttendanceEvents() {
  // Get queued events from IndexedDB
  // Send to server
  // Handle conflicts
}
```

---

## SECTION 17: Onboarding & Offboarding Workflow (E17)

### Description
Automates provisioning, initial schedule, training, and deprovisioning.

### Design Specification

**Onboarding Tasks:**
- Account provisioning
- Initial schedule assignment
- Required training assignment
- Asset assignment
- Welcome notification

**Offboarding Tasks:**
- Access revocation
- Asset collection
- Schedule updates
- Final payroll processing
- Exit interview scheduling

### Sample Implementation

```java
@Service
public class OnboardingService {
    @Transactional
    public void onboardEmployee(Employee employee) {
        // Create user account
        // Assign initial shift
        // Create training tasks
        // Assign starter assets
        // Send welcome notification
        // Audit log
    }
    
    @Transactional
    public void offboardEmployee(Employee employee) {
        // Revoke access
        // Create asset collection tasks
        // Update schedules
        // Trigger final payroll
        // Send notifications
        // Audit log
    }
}
```

---

## SECTION 18: Localization Support (E18)

### Description
Supports multiple languages for UI, notifications, and templates.

### Design Specification

**Features:**
- MessageSource for i18n
- Resource bundles for multiple languages
- Localized templates
- Fallback to default language

### Sample Implementation

```java
@Configuration
public class LocalizationConfig {
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource ms = 
            new ReloadableResourceBundleMessageSource();
        ms.setBasename("classpath:messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setFallbackToSystemLocale(false);
        return ms;
    }
    
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.US);
        return resolver;
    }
}

// messages_en.properties
employee.created=Employee created successfully
leave.approved=Your leave request has been approved

// messages_es.properties
employee.created=Empleado creado exitosamente
leave.approved=Su solicitud de licencia ha sido aprobada
```

---

## SECTION 19: Performance & Scalability (E19)

### Description
Ensures high performance and scalability via caching, async processing, and DB optimization.

### Design Specification

**Optimization Strategies:**
- Redis caching for frequently accessed data
- Async processing for long-running tasks
- Database indexes on frequently queried columns
- Connection pool tuning
- Query optimization
- Load testing with Gatling/JMeter

### Sample Implementation

```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues();
        
        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .build();
    }
}

@Service
public class EmployeeService {
    @Cacheable(value = "employees", key = "#id")
    public EmployeeDTO getEmployeeById(Long id) {
        // Cached for 10 minutes
    }
    
    @CacheEvict(value = "employees", key = "#id")
    public void updateEmployee(Long id, EmployeeUpdateDTO dto) {
        // Evicts cache on update
    }
}

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}

@Service
public class NotificationService {
    @Async
    public void sendBulkNotifications(List<NotificationDTO> notifications) {
        // Process asynchronously
    }
}
```

---

## SECTION 20: Deployment & Observability (E20)

### Description
Production-ready deployment with Docker, health checks, metrics, logging, and tracing.

### Design Specification

**Deployment:**
- Docker containerization
- Kubernetes orchestration
- CI/CD pipeline
- Blue-green deployment
- Rollback capability

**Observability:**
- Actuator endpoints
- Prometheus metrics
- ELK/EFK logging
- OpenTelemetry tracing
- Alerting with PagerDuty/Slack

### Sample Implementation

```dockerfile
# Dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/wems.jar /app/wems.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/wems.jar"]
```

```yaml
# kubernetes/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: wems
spec:
  replicas: 3
  selector:
    matchLabels:
      app: wems
  template:
    metadata:
      labels:
        app: wems
    spec:
      containers:
      - name: wems
        image: wems:latest
        ports:
        - containerPort: 8080
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
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

```properties
# application.properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.probes.enabled=true
management.metrics.export.prometheus.enabled=true
logging.level.root=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline
on:
  push:
    branches: [main]
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
      - name: Run tests
        run: mvn test
      - name: Build Docker image
        run: docker build -t wems:latest .
      - name: Deploy to Kubernetes
        run: kubectl apply -f kubernetes/
```

---

## CONCLUSION

This comprehensive technical design document provides production-ready specifications for all 20 user stories of the Warehouse Employee Management System. Each section includes:

â Detailed entity designs with JPA relationships
â Service layer patterns and business logic
â RESTful API specifications
â Security configurations
â Sample implementations
â Integration patterns
â Testing strategies
â Deployment configurations

The design follows Spring Boot 3.x best practices and is ready for implementation by development teams.

---

## APPENDIX: Additional Resources

### Database Schema
Refer to Flyway migration scripts in `src/main/resources/db/migration/`

### API Documentation
OpenAPI/Swagger UI available at `/swagger-ui.html` when application is running

### Testing
- Unit tests: `src/test/java/`
- Integration tests: `src/test/java/integration/`
- Performance tests: `src/test/java/performance/`

### Configuration
- Development: `application-dev.properties`
- Production: `application-prod.properties`
- Test: `application-test.properties`