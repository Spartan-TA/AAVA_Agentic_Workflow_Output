# Warehouse Employee Management System - Technical Design Document
## Part 4: Epics E16-E20

### E16: Mobile Access (PWA)

**PWA Configuration:**
```json
// manifest.json
{
  "name": "Warehouse Employee App",
  "short_name": "Warehouse",
  "start_url": "/mobile",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "icons": [
    {
      "src": "/icons/icon-192.png",
      "sizes": "192x192",
      "type": "image/png"
    },
    {
      "src": "/icons/icon-512.png",
      "sizes": "512x512",
      "type": "image/png"
    }
  ]
}
```

**Service Worker:**
```javascript
// service-worker.js
self.addEventListener('sync', event => {
  if (event.tag === 'sync-attendance') {
    event.waitUntil(syncAttendanceEvents());
  }
});

async function syncAttendanceEvents() {
  const db = await openDB();
  const events = await db.getAll('pending-attendance');
  for (const event of events) {
    try {
      await fetch('/api/attendance/clock-in', {
        method: 'POST',
        body: JSON.stringify(event)
      });
      await db.delete('pending-attendance', event.id);
    } catch (error) {
      console.error('Sync failed', error);
    }
  }
}
```

**Mobile Controller:**
```java
@RestController
@RequestMapping("/api/mobile")
public class MobileController {
    @GetMapping("/dashboard")
    public MobileDashboardDTO getDashboard(@AuthenticationPrincipal User user) {
        Employee employee = employeeService.getByUserId(user.getId());
        return MobileDashboardDTO.builder()
            .upcomingShifts(schedulingService.getUpcomingShifts(employee.getId()))
            .leaveBalance(leaveService.getBalance(employee.getId()))
            .announcements(notificationService.getActiveAnnouncements())
            .certifications(certificationService.getExpiringCerts(employee.getId(), 30))
            .build();
    }
}
```

**REST Endpoints:**
- GET /api/mobile/dashboard
- POST /api/mobile/clock-in
- POST /api/mobile/clock-out
- GET /api/mobile/schedule

---

### E17: Onboarding & Offboarding Workflow

**Entity Design:**
```java
@Entity
public class OnboardingTask extends BaseEntity {
    @ManyToOne private Employee employee;
    private String taskName;
    private String taskType;
    private boolean completed;
    private LocalDateTime completedAt;
    private String assignedTo;
}

@Entity
public class OffboardingTask extends BaseEntity {
    @ManyToOne private Employee employee;
    private String taskName;
    private String taskType;
    private boolean completed;
    private LocalDateTime completedAt;
    private String assignedTo;
}
```

**Service Layer:**
```java
public interface WorkflowService {
    void onboardEmployee(Long employeeId);
    void offboardEmployee(Long employeeId);
    List<OnboardingTaskDTO> getOnboardingTasks(Long employeeId);
    void completeTask(Long taskId);
}

@Service
public class WorkflowServiceImpl implements WorkflowService {
    public void onboardEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow();
        
        // Create user account
        userService.createUser(employee);
        
        // Assign initial schedule
        schedulingService.assignInitialSchedule(employeeId);
        
        // Assign required training
        certificationService.assignRequiredTraining(employeeId);
        
        // Assign assets
        assetService.assignInitialAssets(employeeId);
        
        // Create onboarding tasks
        createOnboardingTasks(employee);
        
        // Send welcome notification
        notificationService.sendWelcomeNotification(employee);
    }
    
    public void offboardEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow();
        
        // Revoke access
        userService.revokeAccess(employee.getUserId());
        
        // Collect assets
        assetService.createAssetReturnTasks(employeeId);
        
        // Update schedules
        schedulingService.removeFromFutureSchedules(employeeId);
        
        // Create offboarding tasks
        createOffboardingTasks(employee);
        
        // Notify stakeholders
        notificationService.sendOffboardingNotifications(employee);
    }
}
```

**REST Endpoints:**
- POST /api/workflow/onboard/{employeeId}
- POST /api/workflow/offboard/{employeeId}
- GET /api/workflow/onboarding-tasks/{employeeId}
- POST /api/workflow/tasks/{taskId}/complete

---

### E18: Localization & Multi-Tenant

**Entity Design:**
```java
@Entity
public class Tenant extends BaseEntity {
    private String tenantId;
    private String name;
    private String locale;
    private String timezone;
    private String currency;
    private boolean active;
}
```

**Multi-Tenancy Configuration:**
```java
@Configuration
public class MultiTenancyConfig {
    @Bean
    public CurrentTenantIdentifierResolver tenantResolver() {
        return new CurrentTenantIdentifierResolver() {
            @Override
            public String resolveCurrentTenantIdentifier() {
                String tenantId = TenantContext.getCurrentTenant();
                return tenantId != null ? tenantId : "default";
            }
            
            @Override
            public boolean validateExistingCurrentSessions() {
                return true;
            }
        };
    }
    
    @Bean
    public MultiTenantConnectionProvider multiTenantConnectionProvider() {
        return new SchemaBasedMultiTenantConnectionProvider();
    }
}
```

**Tenant Filter:**
```java
@Component
public class TenantFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) {
        String tenantId = request.getHeader("X-Tenant-ID");
        if (tenantId != null) {
            TenantContext.setCurrentTenant(tenantId);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
```

**Localization:**
```java
@Configuration
public class LocalizationConfig {
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = 
            new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
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

### E19: Observability & Monitoring

**Actuator Configuration:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus,info
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

**Custom Metrics:**
```java
@Component
public class AttendanceMetrics {
    private final MeterRegistry meterRegistry;
    
    @Timed(value = "attendance.clockin.duration", description = "Time to clock in")
    public void clockIn(Long employeeId) {
        meterRegistry.counter("attendance.clockin.count").increment();
        // Clock in logic
    }
    
    @Gauge(name = "employees.active.count", description = "Number of active employees")
    public int getActiveEmployeeCount() {
        return employeeRepository.countByDeletedFalse();
    }
}
```

**Distributed Tracing:**
```java
@Configuration
public class TracingConfig {
    @Bean
    public OpenTelemetry openTelemetry() {
        return OpenTelemetrySdk.builder()
            .setTracerProvider(SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(
                    OtlpGrpcSpanExporter.builder().build()
                ).build())
                .build())
            .buildAndRegisterGlobal();
    }
}
```

**Structured Logging:**
```java
@Slf4j
@Component
public class StructuredLogger {
    public void logEvent(String event, Map<String, Object> context) {
        MDC.put("traceId", getTraceId());
        MDC.put("tenantId", TenantContext.getCurrentTenant());
        log.info("event={} context={}", event, toJson(context));
        MDC.clear();
    }
}
```

**Health Checks:**
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Health.up().withDetail("database", "available").build();
        } catch (Exception e) {
            return Health.down().withDetail("database", "unavailable").build();
        }
    }
}
```

---

### E20: CI/CD & Deployment Automation

**GitHub Actions Workflow:**
```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

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
      
      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
      
      - name: Build with Maven
        run: mvn clean verify
      
      - name: Run tests
        run: mvn test
      
      - name: Security scan
        run: mvn org.owasp:dependency-check-maven:check
      
      - name: Build Docker image
        run: |
          docker build -t warehouse-employee-app:${{ github.sha }} .
          docker tag warehouse-employee-app:${{ github.sha }} warehouse-employee-app:latest
      
      - name: Push to registry
        if: github.ref == 'refs/heads/main'
        run: |
          echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
          docker push warehouse-employee-app:${{ github.sha }}
          docker push warehouse-employee-app:latest
  
  deploy:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Deploy to Kubernetes
        run: |
          kubectl set image deployment/warehouse-employee-app             warehouse-employee-app=warehouse-employee-app:${{ github.sha }}
          kubectl rollout status deployment/warehouse-employee-app
```

**Dockerfile:**
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Kubernetes Deployment:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: warehouse-employee-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: warehouse-employee-app
  template:
    metadata:
      labels:
        app: warehouse-employee-app
    spec:
      containers:
      - name: warehouse-employee-app
        image: warehouse-employee-app:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
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

**Helm Chart Values:**
```yaml
replicaCount: 3

image:
  repository: warehouse-employee-app
  tag: latest
  pullPolicy: Always

service:
  type: LoadBalancer
  port: 80
  targetPort: 8080

ingress:
  enabled: true
  annotations:
    kubernetes.io/ingress.class: nginx
  hosts:
    - host: warehouse.example.com
      paths:
        - path: /
          pathType: Prefix

resources:
  limits:
    cpu: 1000m
    memory: 2Gi
  requests:
    cpu: 500m
    memory: 1Gi

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
  targetCPUUtilizationPercentage: 80
```

---

## Appendix: Best Practices

### Exception Handling
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }
}
```

### DTO Mapping
```java
@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeDTO toDTO(Employee entity);
    Employee toEntity(EmployeeDTO dto);
}
```

### Testing
```java
@SpringBootTest
@Testcontainers
class EmployeeServiceTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Test
    void testCreateEmployee() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("John Doe");
        EmployeeDTO created = employeeService.create(dto);
        assertNotNull(created.getId());
    }
}
```
