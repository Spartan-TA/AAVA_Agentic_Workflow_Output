# Warehouse Employee Management System - Low Level Technical Design Document

---

Section: Localization & Multi-Tenant Support (US18, US57)
Description: Implements tenant isolation and locale configuration to support multi-tenant deployments and localization for internationalization.
Design Specification:
- Package Structure: `com.company.wms.localization`, `com.company.wms.tenant`
- Entities: `Tenant`, `LocaleConfig`
- Service Layer: `TenantService`, `LocaleService`
- Repository Layer: `TenantRepository`, `LocaleConfigRepository`
- Controller: `TenantController`, `LocaleController`
- Configuration: Spring Boot `LocaleResolver`, `MessageSource`, `TenantContext` (ThreadLocal)
- Security: Tenant context filter for request isolation
- Integration: Locale message bundles, tenant-aware DB schema
Sample Implementation:
```java
// TenantContext for isolation
public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    public static void setTenantId(String tenantId) { currentTenant.set(tenantId); }
    public static String getTenantId() { return currentTenant.get(); }
    public static void clear() { currentTenant.remove(); }
}

// LocaleConfig
@Configuration
public class LocaleConfig {
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.US);
        return slr;
    }
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding("UTF-8");
        return source;
    }
}
```

---

Section: Observability & Monitoring (US19, US58, US59)
Description: Structured logging, distributed tracing, and metrics for system health and debugging.
Design Specification:
- Package Structure: `com.company.wms.observability`
- Integration: Spring Boot Actuator, Micrometer, OpenTelemetry/Zipkin/Jaeger
- Logging: Logback with JSON encoder, MDC for tenant/user context
- Tracing: Auto-configured tracing via Spring Cloud Sleuth
- Metrics: Custom and built-in metrics endpoints
- Security: Restrict actuator endpoints to ADMIN
Sample Implementation:
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,loggers,threaddump,env
  endpoint:
    health:
      show-details: always
logging:
  pattern:
    console: '{"timestamp":"%d{yyyy-MM-dd HH:mm:ss}","level":"%p","tenant":"%X{tenant}","user":"%X{user}","logger":"%c{1}","msg":"%m"}'
```
```java
// Example custom metric
@Autowired MeterRegistry meterRegistry;
meterRegistry.counter("wms.safety.incidents", "severity", incident.getSeverity()).increment();
```

---

Section: CI/CD & Deployment Automation (US20, US60, US61)
Description: Automated build, test, deployment pipeline with rollback strategy for safe releases.
Design Specification:
- Pipeline: GitHub Actions/Jenkins pipeline YAML
- Steps: Build (Maven), Test (JUnit), Static Analysis (Sonar), Docker Build/Push, Deploy (K8s/CloudFoundry), Rollback
- Rollback: Blue/Green or Canary deployment, automated rollback on health check failure
- Secrets: Managed via Vault/GitHub Secrets
Sample Implementation:
```yaml
# .github/workflows/ci-cd.yml
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
        run: docker build -t wms:latest .
      - name: Push to Registry
        run: docker push ${{ secrets.REGISTRY_URL }}/wms:latest
      - name: Deploy to K8s
        run: kubectl apply -f k8s/deployment.yaml
      - name: Health Check
        run: curl -f http://localhost:8080/actuator/health || exit 1
      - name: Rollback
        if: failure()
        run: kubectl rollout undo deployment/wms
```

---

Section: Training & Certification Tracking (US21-24)
Description: Track required certifications, expirations, renewals, and block assignments for expired certifications.
Design Specification:
- Package Structure: `com.company.wms.training`
- Entities: `Certification`, `EmployeeCertification`
- Service Layer: `CertificationService`, `EmployeeCertificationService`
- Repository Layer: `CertificationRepository`, `EmployeeCertificationRepository`
- Controller: `CertificationController`
- Integration: Notification for expiring certs, document upload
Sample Implementation:
```java
@Entity
public class Certification {
    @Id @GeneratedValue private Long id;
    private String name;
    private LocalDate expiryDate;
    // ...
}

@Service
public class CertificationService {
    public void alertExpiringCerts() { /* ... */ }
}
```

---

Section: Safety Incidents & OSHA Reporting (US25-28)
Description: Record safety incidents, manage investigation workflow, and generate OSHA reports.
Design Specification:
- Package Structure: `com.company.wms.safety`
- Entities: `SafetyIncident`, `IncidentStatus`, `EmployeeInvolved`
- Service Layer: `SafetyIncidentService`
- Repository Layer: `SafetyIncidentRepository`
- Controller: `SafetyIncidentController`
- Integration: OSHA export, dashboard metrics
Sample Implementation:
```java
@Entity
public class SafetyIncident {
    @Id @GeneratedValue private Long id;
    private String description;
    private IncidentStatus status;
    // ...
}

@Service
public class SafetyIncidentService {
    public void resolveIncident(Long id) { /* ... */ }
}
```

---

Section: Equipment & Asset Assignment (US29-32)
Description: Assign and track equipment/assets to employees, enforce certification requirements.
Design Specification:
- Package Structure: `com.company.wms.asset`
- Entities: `Asset`, `AssetAssignment`, `AssetCondition`
- Service Layer: `AssetService`, `AssetAssignmentService`
- Repository Layer: `AssetRepository`, `AssetAssignmentRepository`
- Controller: `AssetController`
- Integration: Certification check, overdue report
Sample Implementation:
```java
@Entity
public class Asset {
    @Id @GeneratedValue private Long id;
    private String type;
    private String serialNumber;
    // ...
}

@Service
public class AssetService {
    public void assignAsset(Long assetId, Long employeeId) { /* ... */ }
}
```

---

Section: Performance Reviews & Goals (US33-36)
Description: Manage performance review cycles, goals, ratings, and acknowledgements.
Design Specification:
- Package Structure: `com.company.wms.performance`
- Entities: `PerformanceReview`, `Goal`, `Competency`
- Service Layer: `PerformanceReviewService`
- Repository Layer: `PerformanceReviewRepository`
- Controller: `PerformanceReviewController`
- Integration: PDF export, immutable history
Sample Implementation:
```java
@Entity
public class PerformanceReview {
    @Id @GeneratedValue private Long id;
    private Long employeeId;
    private String cycle;
    // ...
}

@Service
public class PerformanceReviewService {
    public void submitReview(Long reviewId) { /* ... */ }
}
```

---

Section: Payroll Export Integration (US37-40)
Description: Generate payroll-ready files, map to provider formats, and deliver securely.
Design Specification:
- Package Structure: `com.company.wms.payroll`
- Entities: `PayrollExport`, `PayrollProvider`
- Service Layer: `PayrollExportService`
- Repository Layer: `PayrollExportRepository`
- Controller: `PayrollExportController`
- Integration: SFTP/API delivery, audit log
Sample Implementation:
```java
@Service
public class PayrollExportService {
    public void exportPayroll(Long periodId) { /* ... */ }
}
```

---

Section: Notifications & Announcements (US41-44)
Description: In-app, email, and SMS notifications for events and announcements.
Design Specification:
- Package Structure: `com.company.wms.notification`
- Entities: `Notification`, `Announcement`
- Service Layer: `NotificationService`, `AnnouncementService`
- Repository Layer: `NotificationRepository`, `AnnouncementRepository`
- Controller: `NotificationController`, `AnnouncementController`
- Integration: Email/SMS providers, localization
Sample Implementation:
```java
@Service
public class NotificationService {
    public void sendNotification(Long userId, String message) { /* ... */ }
}
```

---

Section: Integration Layer (HRIS/WMS APIs) (US45-48)
Description: REST APIs and connectors for HRIS, WMS, and SSO integration.
Design Specification:
- Package Structure: `com.company.wms.integration`
- Entities: `HRISSyncJob`, `WMSLink`
- Service Layer: `HRISIntegrationService`, `WMSIntegrationService`
- Controller: `IntegrationController`
- Security: JWT/OAuth2
- Integration: Webhooks, OpenAPI docs
Sample Implementation:
```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    @PostMapping("/hris/sync")
    public ResponseEntity<?> syncHRIS() { /* ... */ }
}
```

---

Section: Audit Trail & Compliance (US49-50)
Description: Centralized audit logging for sensitive changes with tamper-evident storage.
Design Specification:
- Package Structure: `com.company.wms.audit`
- Entities: `AuditLog`
- Service Layer: `AuditService`
- Repository Layer: `AuditLogRepository`
- Integration: Export, immutable log
Sample Implementation:
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue private Long id;
    private String entity;
    private String action;
    private String actor;
    private LocalDateTime timestamp;
    // ...
}
```

---

Section: Reporting & Analytics (US51-52)
Description: Operational reports, dashboards, and export capabilities.
Design Specification:
- Package Structure: `com.company.wms.reporting`
- Entities: `Report`, `Metric`
- Service Layer: `ReportingService`
- Repository Layer: `ReportRepository`
- Controller: `ReportingController`
- Integration: CSV/PDF export, BI endpoints
Sample Implementation:
```java
@Service
public class ReportingService {
    public Report generateAttendanceReport(LocalDate from, LocalDate to) { /* ... */ }
}
```

---

Section: Mobile Access (PWA) (US53-54)
Description: Responsive, offline-friendly PWA for core workflows.
Design Specification:
- Package Structure: `com.company.wms.mobile`
- Controller: `MobileController`
- Integration: Service Worker, offline queue, manifest.json
Sample Implementation:
```javascript
// service-worker.js
self.addEventListener('fetch', function(event) {
  event.respondWith(
    caches.match(event.request).then(function(response) {
      return response || fetch(event.request);
    })
  );
});
```

---

Section: Onboarding & Offboarding Workflow (US55-56)
Description: Automate provisioning and deprovisioning of accounts, schedules, and assets.
Design Specification:
- Package Structure: `com.company.wms.onboarding`
- Entities: `OnboardingTask`, `OffboardingTask`
- Service Layer: `OnboardingService`, `OffboardingService`
- Repository Layer: `OnboardingTaskRepository`, `OffboardingTaskRepository`
- Integration: HRIS, asset collection
Sample Implementation:
```java
@Service
public class OnboardingService {
    public void onboardEmployee(Long employeeId) { /* ... */ }
}
```

---

# End of Document
