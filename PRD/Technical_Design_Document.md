# Warehouse Employee Management System (EMS) - Low-Level Technical Design Document

## Introduction
This document provides comprehensive low-level technical design specifications for all user stories derived from the 20 epics of the Warehouse Employee Management System (EMS) project. Each section is structured according to Spring Boot best practices and industry standards, ensuring clarity and consistency for development teams.

---

### E01: Project Scaffolding & Domain Setup
Section: Spring Boot Project Initialization
Description: Establishes the foundational architecture for the EMS application, ensuring modularity, maintainability, and scalability.
Design Specification:
- Use Spring Boot (Maven) for rapid development and dependency management.
- Base package structure: `com.warehouse.ems`
  - `employee`, `scheduling`, `attendance`, `safety` modules as sub-packages.
- Integrate Flyway/Liquibase for database migrations.
- Enable Spring Boot Actuator for health and metrics endpoints.
- README with build/run instructions.
Sample Implementation:
```java
@SpringBootApplication
public class EmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmsApplication.class, args);
    }
}
```

---

### E02: Employee Master Data (CRUD)
Section: Employee Domain Model & CRUD APIs
Description: Implements core employee data management with RESTful CRUD operations and DTOs.
Design Specification:
- Entity: `Employee` (fields: id, name, badgeId, role, department, shiftGroup, hireDate, status)
- Repository: `EmployeeRepository extends JpaRepository<Employee, Long>`
- Service: `EmployeeService` for business logic
- Controller: `EmployeeController` with endpoints `/employees`
- Unique constraint on `badgeId`
- Soft-delete via `status` field
- Pagination and filtering support
- OpenAPI documentation
Sample Implementation:
```java
@Entity
public class Employee {
    @Id @GeneratedValue
    private Long id;
    private String name;
    @Column(unique = true)
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status; // ACTIVE, DELETED
}
```

---

### E03: Role Based Access Control (RBAC)
Section: Security Configuration
Description: Secures endpoints and data access using Spring Security with role-based and row-level constraints.
Design Specification:
- Roles: ADMIN, HR, SUPERVISOR, WORKER
- Method/endpoint security via `@PreAuthorize`
- Row-level security in service/repository
- API key/OAuth2 toggle via configuration
- Security tests for 401/403 responses
Sample Implementation:
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/employees/**").hasAnyRole("ADMIN", "HR", "SUPERVISOR")
            .anyRequest().authenticated()
            .and().oauth2Login();
    }
}
```

---

### E04: Time & Attendance (Clock In/Out)
Section: Attendance Event Model & Workflow
Description: Captures clock-in/out events, validates geofence/device, and computes shift hours.
Design Specification:
- Entity: `AttendanceEvent` (fields: id, employeeId, timestamp, type, deviceId, location)
- Controller: `/attendance/clock-in`, `/attendance/clock-out`
- Service: Validates geofence/device, associates shift, computes daily totals
- Correction workflow: Approval tasks for missed punches
- CSV export endpoint
Sample Implementation:
```java
@Entity
public class AttendanceEvent {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private LocalDateTime timestamp;
    private String type; // CLOCK_IN, CLOCK_OUT
    private String deviceId;
    private String location;
}
```

---

### E05: Shift & Schedule Management
Section: Shift Template & Scheduling
Description: Manages recurring shifts, rotations, overtime, and employee assignments.
Design Specification:
- Entity: `ShiftTemplate`, `Schedule`
- CRUD endpoints for shift templates/schedules
- Conflict detection logic in service
- Bulk assignment for supervisors
- Audit entries for changes
Sample Implementation:
```java
@Entity
public class ShiftTemplate {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean recurring;
}
```

---

### E06: Leave & Absence Management
Section: Leave Request & Approval Workflow
Description: Handles PTO, sick, unpaid leave requests, approvals, and accrual balances.
Design Specification:
- Entity: `LeaveRequest` (fields: id, employeeId, type, startDate, endDate, status, balance)
- Controller: `/leave/requests`
- Service: Approval/denial, balance updates, integration hooks
- Scheduled shifts flagged for coverage
- Export endpoint for approved leaves
Sample Implementation:
```java
@Entity
public class LeaveRequest {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type; // PTO, SICK, UNPAID
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // REQUESTED, APPROVED, DENIED
    private int balance;
}
```

---

### E07: Training & Certification Tracking
Section: Certification Model & Expiry Alerts
Description: Tracks employee certifications, expirations, renewals, and blocks assignments for expired certs.
Design Specification:
- Entity: `Certification` (fields: id, employeeId, type, expiryDate, proofDocument)
- CRUD endpoints for certifications
- Alert service for expiry notifications
- Scheduling checks for assignment blocks
Sample Implementation:
```java
@Entity
public class Certification {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String type;
    private LocalDate expiryDate;
    private String proofDocument;
}
```

---

### E08: Safety Incidents & OSHA Reporting
Section: Incident Workflow & OSHA Export
Description: Records safety incidents, manages investigation workflow, and generates OSHA reports.
Design Specification:
- Entity: `SafetyIncident` (fields: id, severity, location, description, involvedEmployees, status)
- Controller: `/safety/incidents`
- Workflow: Status transitions (Open, Investigating, Resolved)
- OSHA export endpoints
- Metrics dashboard
Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue
    private Long id;
    private String severity;
    private String location;
    private String description;
    private String involvedEmployees;
    private String status; // OPEN, INVESTIGATING, RESOLVED
}
```

---

### E09: Equipment & Asset Assignment
Section: Asset Registry & Assignment
Description: Manages asset assignment, check-in/out, and blocks usage if certifications are missing.
Design Specification:
- Entity: `Asset` (fields: id, type, condition, assignedEmployeeId, checkoutDate, returnDate)
- Controller: `/assets`, `/assets/check-in`, `/assets/check-out`
- Service: Certification validation, history log, overdue reports
Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue
    private Long id;
    private String type;
    private String condition;
    private Long assignedEmployeeId;
    private LocalDate checkoutDate;
    private LocalDate returnDate;
}
```

---

### E10: Performance Reviews & Goals
Section: Review Cycle & Goal Tracking
Description: Supports review templates, goal tracking, ratings, comments, and immutable history after sign-off.
Design Specification:
- Entity: `PerformanceReview` (fields: id, employeeId, cycle, goals, competencies, ratings, comments, status)
- Controller: `/reviews`
- Workflow: Submit/acknowledge, PDF export, role-based visibility
Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String cycle;
    private String goals;
    private String competencies;
    private String ratings;
    private String comments;
    private String status; // DRAFT, SUBMITTED, SIGNED_OFF
}
```

---

### E11: Payroll Export Integration
Section: Payroll File Generation & Delivery
Description: Generates payroll files from attendance/leave, maps to provider formats, and delivers securely.
Design Specification:
- Service: PayrollExportService
- Integration: SFTP/API delivery
- Audit log for exports
- Retry logic for failed deliveries
Sample Implementation:
```java
@Service
public class PayrollExportService {
    public void exportPayroll() {
        // Map attendance/leave to provider schema
        // Deliver via SFTP/API
        // Log export event
    }
}
```

---

### E12: Notifications & Announcements
Section: Notification Delivery & Opt-In
Description: Delivers notifications via in-app, email, SMS; supports opt-in/out, localization, and rate limits.
Design Specification:
- Entity: `Notification` (fields: id, employeeId, channel, message, status, deliveryTimestamp)
- Controller: `/notifications`, `/announcements`
- Service: Delivery status tracking, rate limiting
- Localized templates
Sample Implementation:
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private Long employeeId;
    private String channel; // IN_APP, EMAIL, SMS
    private String message;
    private String status; // SENT, FAILED
    private LocalDateTime deliveryTimestamp;
}
```

---

### E13: Integration Layer (HRIS/WMS APIs)
Section: External API Connectors & Webhooks
Description: Exposes REST APIs for HRIS/WMS, supports SSO, and event webhooks.
Design Specification:
- REST endpoints: `/api/hris`, `/api/wms`, `/api/idp`
- JWT/OAuth2 security
- HRIS sync job for new hires/terms
- Idempotent webhooks
- OpenAPI documentation
Sample Implementation:
```java
@RestController
@RequestMapping("/api/hris")
public class HrisController {
    @PostMapping("/sync")
    public ResponseEntity<?> syncHris(@RequestBody HrisPayload payload) {
        // Sync logic
        return ResponseEntity.ok().build();
    }
}
```

---

### E14: Audit Trail & Compliance
Section: Audit Logging & Tamper-Evidence
Description: Centralizes audit logs for sensitive changes, ensures immutability and exportability.
Design Specification:
- Entity: `AuditLog` (fields: id, actor, timestamp, entity, before, after, action)
- Service: AuditLogService
- Export endpoint by date/user/entity
- Tests for coverage
Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    private String actor;
    private LocalDateTime timestamp;
    private String entity;
    private String before;
    private String after;
    private String action; // CREATE, UPDATE, DELETE
}
```

---

### E15: Reporting & Analytics
Section: Report Generation & Dashboards
Description: Provides operational reports, exports, and dashboards with access control.
Design Specification:
- Service: ReportService
- Endpoints: `/reports/attendance`, `/reports/overtime`, etc.
- Export formats: CSV, PDF
- Role-based access
- Metrics endpoints for BI
Sample Implementation:
```java
@Service
public class ReportService {
    public Report generateAttendanceReport(DateRange range, String department) {
        // Query attendance, filter, export
    }
}
```

---

### E16: Mobile Access (PWA)
Section: PWA Manifest & Offline Support
Description: Enables mobile-friendly views, offline queueing, and installable PWA features.
Design Specification:
- PWA manifest file
- Offline queue for clock events
- Conflict resolution logic
- Responsive UI components
Sample Implementation:
```json
{
  "name": "Warehouse EMS",
  "short_name": "EMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2"
}
```

---

### E17: Onboarding & Offboarding Workflow
Section: Employee Lifecycle Automation
Description: Automates provisioning, training assignment, and asset deprovisioning.
Design Specification:
- Service: OnboardingService, OffboardingService
- HRIS integration for new hires
- Task generation for training/assets
- Access/asset revocation on termination
Sample Implementation:
```java
@Service
public class OnboardingService {
    public void onboardEmployee(HrisEmployee employee) {
        // Provision account, assign schedule/training/assets
    }
}
```

---

### E18: Localization & Multi-Tenant
Section: Tenant Isolation & i18n
Description: Supports multiple warehouses (tenants), localized messages, and timezone-aware timestamps.
Design Specification:
- Entity: `Tenant` (fields: id, name, locale, timezone)
- Tenant ID in queries
- Locale header for message switching
- UTC timestamps
- i18n keys for messages
Sample Implementation:
```java
@Entity
public class Tenant {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String locale;
    private String timezone;
}
```

---

### E19: Performance & Scalability
Section: Query Optimization & Caching
Description: Optimizes queries, caches frequently accessed data, and supports horizontal scaling.
Design Specification:
- Indexes on key fields (employeeId, badgeId, etc.)
- Spring Cache abstraction for hot data
- Connection pooling configuration
- Load testing scripts
Sample Implementation:
```java
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("employees", "attendance");
    }
}
```

---

### E20: Deployment & Observability
Section: Containerization, CI/CD, Logging & Tracing
Description: Ensures production-grade deployment, monitoring, and traceability.
Design Specification:
- Dockerfile for containerization
- Kubernetes manifests for deployment
- CI/CD pipeline configuration
- Structured logging (JSON)
- Prometheus metrics
- Jaeger distributed tracing
Sample Implementation:
```dockerfile
FROM openjdk:17-jdk-alpine
COPY target/ems.jar /app/ems.jar
ENTRYPOINT ["java", "-jar", "/app/ems.jar"]
```

---

## Conclusion
This document provides a detailed technical blueprint for all major user stories and epics in the Warehouse EMS project. Each section is designed to be production-ready and easily consumable by Spring Boot developers, ensuring high quality and uniformity across the system.
