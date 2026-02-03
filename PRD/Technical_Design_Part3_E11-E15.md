# Warehouse Employee Management System - Technical Design Document
## Part 3: Epics E11-E15

### E11: Payroll Export Integration

**Service Layer:**
```java
public interface PayrollExportService {
    PayrollExportDTO generateExport(LocalDate from, LocalDate to);
    void deliverExport(PayrollExportDTO dto);
}

@Service
public class PayrollExportServiceImpl implements PayrollExportService {
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public void deliverExport(PayrollExportDTO dto) {
        // SFTP or API delivery
        sftpClient.upload(dto.getFilePath(), dto.getContent());
        auditService.logExport(dto);
    }
}
```

**DTO Design:**
```java
public class PayrollExportDTO {
    private String employeeId;
    private String badgeId;
    private BigDecimal regularHours;
    private BigDecimal overtimeHours;
    private BigDecimal leaveHours;
    private LocalDate periodStart;
    private LocalDate periodEnd;
}
```

**REST Endpoints:**
- POST /api/payroll/export
- GET /api/payroll/reconcile

**Configuration:**
```yaml
payroll:
  sftp:
    host: ${PAYROLL_SFTP_HOST}
    username: ${PAYROLL_SFTP_USER}
    password: ${PAYROLL_SFTP_PASS}
  format: ADP_V2
```

---

### E12: Notifications & Announcements

**Entity Design:**
```java
@Entity
public class Notification extends BaseEntity {
    private String message;
    @Enumerated(EnumType.STRING) private NotificationChannel channel;
    private boolean delivered;
    private LocalDateTime deliveredAt;
    @ManyToOne private Employee recipient;
    private String metadata;
}

@Entity
public class Announcement extends BaseEntity {
    private String title;
    @Lob private String content;
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    private boolean active;
}
```

**Service Layer:**
```java
public interface NotificationService {
    void sendNotification(NotificationDTO dto);
    void sendAnnouncement(AnnouncementDTO dto);
    List<NotificationDTO> getNotifications(Long employeeId);
}

@Service
public class NotificationServiceImpl implements NotificationService {
    public void sendNotification(NotificationDTO dto) {
        if (isQuietHours()) {
            queueForLater(dto);
            return;
        }
        switch (dto.getChannel()) {
            case EMAIL -> emailService.send(dto);
            case SMS -> smsService.send(dto);
            case IN_APP -> saveInApp(dto);
        }
    }
}
```

**REST Endpoints:**
- POST /api/notifications
- GET /api/notifications/employee/{employeeId}
- POST /api/announcements
- GET /api/announcements/active

---

### E13: Integration Layer (HRIS/WMS APIs)

**Entity Design:**
```java
@Entity
public class IntegrationEvent extends BaseEntity {
    private String eventId;
    private String eventType;
    private String source;
    @Lob private String payload;
    private boolean processed;
    private LocalDateTime processedAt;
}
```

**Service Layer:**
```java
public interface HRISService {
    void syncEmployees();
    void handleNewHire(HRISEmployeeDTO dto);
    void handleTermination(String employeeId);
}

public interface WMSService {
    void syncDepartments();
    void syncLocations();
}

public interface WebhookService {
    void handleEvent(WebhookEventDTO dto);
}

@Service
public class WebhookServiceImpl implements WebhookService {
    public void handleEvent(WebhookEventDTO dto) {
        if (integrationEventRepository.existsByEventId(dto.getEventId())) {
            return; // Idempotent
        }
        IntegrationEvent event = new IntegrationEvent();
        event.setEventId(dto.getEventId());
        event.setPayload(dto.getPayload());
        integrationEventRepository.save(event);
        processEvent(event);
    }
}
```

**REST Endpoints:**
- POST /api/integration/webhook
- POST /api/integration/hris/sync
- POST /api/integration/wms/sync
- GET /api/integration/events

**Security:**
```java
@Configuration
public class IntegrationSecurityConfig {
    @Bean
    public SecurityFilterChain integrationFilterChain(HttpSecurity http) {
        http.securityMatcher("/api/integration/**")
            .authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer().jwt();
        return http.build();
    }
}
```

---

### E14: Audit Trail & Compliance

**Entity Design:**
```java
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_entity", columnList = "entityName,entityId"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
public class AuditLog extends BaseEntity {
    private String entityName;
    private Long entityId;
    private String action;
    private String actor;
    private Instant timestamp;
    @Lob private String beforeState;
    @Lob private String afterState;
    private String ipAddress;
    private String userAgent;
}
```

**Service Layer:**
```java
public interface AuditService {
    void logChange(String entity, Long id, String action, String actor, Object before, Object after);
    List<AuditLogDTO> getLogs(String entity, LocalDate from, LocalDate to);
    byte[] exportLogs(AuditFilter filter);
}
```

**AOP Aspect:**
```java
@Aspect
@Component
public class AuditAspect {
    @AfterReturning(pointcut = "@annotation(Auditable)", returning = "result")
    public void audit(JoinPoint jp, Object result) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        Auditable auditable = signature.getMethod().getAnnotation(Auditable.class);
        String entity = auditable.entity();
        String action = auditable.action();
        String actor = SecurityContextHolder.getContext().getAuthentication().getName();
        auditService.logChange(entity, extractId(result), action, actor, null, result);
    }
}
```

**REST Endpoints:**
- GET /api/audit
- GET /api/audit/export

---

### E15: Reporting & Analytics

**Service Layer:**
```java
public interface ReportingService {
    ReportDTO generateAttendanceReport(ReportFilter filter);
    ReportDTO generateOvertimeReport(ReportFilter filter);
    ReportDTO generateLeaveBalanceReport(ReportFilter filter);
    ReportDTO generateCertificationStatusReport(ReportFilter filter);
    ReportDTO generateSafetyKPIReport(ReportFilter filter);
    byte[] exportCSV(ReportDTO report);
    byte[] exportPDF(ReportDTO report);
}
```

**DTO Design:**
```java
public class ReportFilter {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Long> departmentIds;
    private List<Long> employeeIds;
    private String groupBy;
}

public class ReportDTO {
    private String title;
    private LocalDateTime generatedAt;
    private List<String> columns;
    private List<Map<String, Object>> rows;
    private Map<String, Object> summary;
}
```

**Repository Queries:**
```java
public interface AttendanceReportRepository extends JpaRepository<AttendanceEvent, Long> {
    @Query("SELECT new com.warehouse.employee.reporting.dto.AttendanceReportRow(" +
           "e.id, e.name, COUNT(a), SUM(a.hours)) " +
           "FROM AttendanceEvent a JOIN a.employee e " +
           "WHERE a.eventTime BETWEEN :start AND :end " +
           "GROUP BY e.id, e.name")
    List<AttendanceReportRow> generateAttendanceReport(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}
```

**REST Endpoints:**
- GET /api/reports/attendance
- GET /api/reports/overtime
- GET /api/reports/leave-balance
- GET /api/reports/certification-status
- GET /api/reports/safety-kpi
- GET /api/reports/{reportId}/export/csv
- GET /api/reports/{reportId}/export/pdf

**Security:**
```java
@PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
public ReportDTO generateAttendanceReport(ReportFilter filter) {
    if (hasRole("SUPERVISOR")) {
        filter.setEmployeeIds(getDirectReports(getCurrentUser()));
    }
    return reportingService.generateAttendanceReport(filter);
}
```
