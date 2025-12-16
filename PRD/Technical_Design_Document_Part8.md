# Technical Design Document â Part 8 (User Stories 36-40)

---

Section: E11-Payroll Export Integration (User Story 36)

Description: Continuation of Payroll Export, focusing on failed delivery retries with backoff and audit log for every export.

Design Specification:
- **Services:**
  - `PayrollExportService` (retry logic, exponential backoff, audit log entry per attempt)
- **Integration:**
  - Audit trail module integration for export events.

Sample Implementation:
```java
@Service
public class PayrollExportService {
    public void deliverExport(PayrollExport export) {
        int attempts = 0;
        while (attempts < MAX_ATTEMPTS && !export.isDelivered()) {
            try {
                // deliver via SFTP/API
                export.setDeliveredAt(LocalDateTime.now());
                export.setStatus("DELIVERED");
            } catch (Exception e) {
                attempts++;
                export.setAttempts(attempts);
                export.setErrorLog(e.getMessage());
                Thread.sleep((long) Math.pow(2, attempts) * 1000); // backoff
            }
            auditTrailService.logExportAttempt(export);
        }
    }
}
```

---

Section: E12-Notifications & Announcements (User Stories 37-39)

Description: This module manages in-app, email, and SMS notifications for shift changes, expiring certifications, approvals, and announcements. It supports quiet hours, opt-in/out, localization, and delivery tracking.

Design Specification:
- **Package Structure:**
  - `com.company.wms.notification` (domain, repository, service, controller, dto)
- **Entities:**
  - `Notification` (id, userId, type, channel, content, status, sentAt, deliveryStatus)
  - `Announcement` (id, title, content, createdBy, createdAt, visibleUntil)
- **Repositories:**
  - `NotificationRepository`, `AnnouncementRepository`
- **Services:**
  - `NotificationService` (send, opt-in/out, track delivery, rate limit, quiet hours)
  - `AnnouncementService` (CRUD, dashboard display)
- **Controllers:**
  - `NotificationController`, `AnnouncementController`
- **Security:**
  - Users manage preferences; ADMIN/HR/SUPERVISOR can send announcements.
- **Integration:**
  - Localized templates; hooks for shift/cert/leave events.

Sample Implementation:
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    private Long userId;
    private String type; // SHIFT, CERT, LEAVE, ANNOUNCEMENT
    private String channel; // IN_APP, EMAIL, SMS
    private String content;
    private String status; // PENDING, SENT, FAILED
    private LocalDateTime sentAt;
    private String deliveryStatus;
}

@Service
public class NotificationService {
    public void sendNotification(NotificationDto dto) { ... }
    public void trackDelivery(Long notificationId, String status) { ... }
}
```

---

Section: E13-Integration Layer (HRIS/WMS APIs) (User Story 40)

Description: This module exposes REST APIs and connectors for HRIS (new hires/terms), WMS (location/department), and IDP for SSO. It supports webhooks for events and JWT/OAuth2 security.

Design Specification:
- **Package Structure:**
  - `com.company.wms.integration` (domain, service, controller, dto)
- **Entities:**
  - `IntegrationEvent` (id, type, payload, status, createdAt, deliveredAt)
- **Services:**
  - `IntegrationService` (sync jobs, webhook delivery, idempotency)
- **Controllers:**
  - `IntegrationController` (REST endpoints for HRIS/WMS sync, webhooks)
- **Security:**
  - JWT/OAuth2 for all endpoints.
- **Integration:**
  - OpenAPI documentation; idempotent webhooks.

Sample Implementation:
```java
@Entity
public class IntegrationEvent {
    @Id @GeneratedValue
    private Long id;
    private String type; // HRIS_NEW_HIRE, WMS_UPDATE
    private String payload;
    private String status; // PENDING, DELIVERED, FAILED
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;
}

@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    @PostMapping("/hris/sync")
    @PreAuthorize("hasAuthority('SCOPE_integration:write')")
    public ResponseEntity<?> syncHris(@RequestBody HrisSyncDto dto) { ... }

    @PostMapping("/webhook")
    @PreAuthorize("hasAuthority('SCOPE_integration:write')")
    public ResponseEntity<?> receiveWebhook(@RequestBody IntegrationEventDto dto) { ... }
}
```
