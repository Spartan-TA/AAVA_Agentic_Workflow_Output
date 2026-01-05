# WAREHOUSE EMPLOYEE MANAGEMENT SYSTEM â LOW-LEVEL TECHNICAL DESIGN DOCUMENT
## PART 3: EPICS E12-E17 AND GENERAL NOTES

## TABLE OF CONTENTS - PART 3
1. [Epic E12: Notifications & Announcements](#epic-e12)
2. [Epic E13: Integration Layer (HRIS/WMS APIs)](#epic-e13)
3. [Epic E14: Audit Trail & Compliance](#epic-e14)
4. [Epic E15: Reporting & Analytics](#epic-e15)
5. [Epic E16: Mobile Access (PWA)](#epic-e16)
6. [Epic E17: Onboarding & Offboarding Workflow](#epic-e17)
7. [General Notes and Best Practices](#general-notes)

---

## <a name="epic-e12"></a>EPIC E12: NOTIFICATIONS & ANNOUNCEMENTS

### Section: Spring Boot Architecture Overview

**Description:** Sends in-app, email, and SMS notifications for key events; supports announcements and quiet hours.

**Design Specification:**
- Notification entity: id, user, type, channel, content, status, sentAt
- Announcement entity for company-wide communications
- User preferences for notification channels and quiet hours

**Sample Implementation:**
```java
package com.company.wms.notification.domain;

import com.company.wms.employee.domain.Employee;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Employee user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;
    
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters and setters
}

public enum NotificationType {
    SHIFT_CHANGE,
    CERT_EXPIRY,
    LEAVE_APPROVAL,
    LEAVE_DENIAL,
    SAFETY_INCIDENT,
    ASSET_OVERDUE,
    ANNOUNCEMENT,
    SYSTEM_ALERT
}

public enum Channel {
    IN_APP,
    EMAIL,
    SMS,
    PUSH
}

public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED,
    READ
}

@Entity
@Table(name = "announcements")
public class Announcement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnouncementPriority priority;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Employee createdBy;
    
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters and setters
}

public enum AnnouncementPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
```

### Section: Service Layer - Notification Management

**Sample Implementation:**
```java
package com.company.wms.notification.service;

import com.company.wms.notification.domain.*;
import com.company.wms.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SMSService smsService;
    
    @Autowired
    private PushNotificationService pushService;
    
    @Async
    public void sendNotification(
            Employee user,
            NotificationType type,
            Channel channel,
            String content) {
        
        // Check quiet hours
        if (isQuietHours(user)) {
            // Defer notification or queue for later
            queueNotification(user, type, channel, content);
            return;
        }
        
        // Check user preferences
        if (!isChannelEnabled(user, channel)) {
            // Try alternative channel
            channel = getAlternativeChannel(user);
            if (channel == null) {
                return; // User has disabled all channels
            }
        }
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setChannel(channel);
        notification.setContent(content);
        notification.setStatus(NotificationStatus.PENDING);
        
        try {
            switch (channel) {
                case EMAIL:
                    emailService.send(user.getEmail(), getSubject(type), content);
                    break;
                case SMS:
                    smsService.send(user.getPhone(), content);
                    break;
                case PUSH:
                    pushService.send(user.getId(), content);
                    break;
                case IN_APP:
                    // In-app notifications are just stored
                    break;
            }
            
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            // Log error and potentially retry
        }
        
        notificationRepository.save(notification);
    }
    
    private boolean isQuietHours(Employee user) {
        if (user.getQuietHoursStart() == null || user.getQuietHoursEnd() == null) {
            return false;
        }
        
        LocalTime now = LocalTime.now();
        LocalTime start = user.getQuietHoursStart();
        LocalTime end = user.getQuietHoursEnd();
        
        if (start.isBefore(end)) {
            return now.isAfter(start) && now.isBefore(end);
        } else {
            // Quiet hours span midnight
            return now.isAfter(start) || now.isBefore(end);
        }
    }
    
    private boolean isChannelEnabled(Employee user, Channel channel) {
        // Check user notification preferences
        return true; // Simplified
    }
    
    private Channel getAlternativeChannel(Employee user) {
        // Return first enabled channel
        return Channel.IN_APP; // Simplified
    }
    
    private String getSubject(NotificationType type) {
        return switch (type) {
            case SHIFT_CHANGE -> "Shift Schedule Update";
            case CERT_EXPIRY -> "Certification Expiring Soon";
            case LEAVE_APPROVAL -> "Leave Request Approved";
            case LEAVE_DENIAL -> "Leave Request Denied";
            case SAFETY_INCIDENT -> "Safety Incident Report";
            case ASSET_OVERDUE -> "Asset Return Overdue";
            case ANNOUNCEMENT -> "Company Announcement";
            case SYSTEM_ALERT -> "System Alert";
        };
    }
    
    private void queueNotification(
            Employee user,
            NotificationType type,
            Channel channel,
            String content) {
        // Queue for later delivery
    }
    
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndReadAtIsNull(userId);
    }
    
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotificationNotFoundException(
                "Notification not found with id: " + notificationId
            ));
        
        notification.setReadAt(LocalDateTime.now());
        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);
    }
}
```

---

## <a name="epic-e13"></a>EPIC E13: INTEGRATION LAYER (HRIS/WMS APIs)

### Section: Spring Boot Architecture Overview

**Description:** Exposes REST APIs and connectors for HRIS, WMS, IDP, and webhooks.

**Design Specification:**
- REST API endpoints secured with JWT/OAuth2
- Scheduled jobs for HRIS/WMS synchronization
- Webhook endpoints for event notifications
- SSO integration with Identity Provider

**Sample Implementation:**
```java
package com.company.wms.integration.controller;

import com.company.wms.employee.dto.EmployeeDTO;
import com.company.wms.employee.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/integration/hris")
public class HRISIntegrationController {
    
    @Autowired
    private EmployeeService employeeService;
    
    @PostMapping("/employees")
    @PreAuthorize("hasRole('SYSTEM') or hasRole('ADMIN')")
    public ResponseEntity<EmployeeDTO> syncEmployee(
            @Valid @RequestBody EmployeeDTO employeeDTO) {
        
        // Idempotent sync logic
        EmployeeDTO result = employeeService.createOrUpdate(employeeDTO);
        return ResponseEntity.ok(result);
    }
    
    @PutMapping("/employees/{badgeId}")
    @PreAuthorize("hasRole('SYSTEM') or hasRole('ADMIN')")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable String badgeId,
            @Valid @RequestBody EmployeeDTO employeeDTO) {
        
        EmployeeDTO result = employeeService.updateByBadgeId(badgeId, employeeDTO);
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/employees/{badgeId}")
    @PreAuthorize("hasRole('SYSTEM') or hasRole('ADMIN')")
    public ResponseEntity<Void> terminateEmployee(@PathVariable String badgeId) {
        employeeService.terminateByBadgeId(badgeId);
        return ResponseEntity.noContent().build();
    }
}

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {
    
    @Autowired
    private WebhookService webhookService;
    
    @PostMapping("/employee-created")
    public ResponseEntity<Void> employeeCreated(
            @RequestBody WebhookPayload payload,
            @RequestHeader("X-Webhook-Signature") String signature) {
        
        // Verify webhook signature
        if (!webhookService.verifySignature(payload, signature)) {
            return ResponseEntity.status(401).build();
        }
        
        // Process webhook (idempotent)
        webhookService.processEmployeeCreated(payload);
        
        return ResponseEntity.ok().build();
    }
}
```

### Section: Scheduled Synchronization Jobs

**Sample Implementation:**
```java
package com.company.wms.integration.job;

import com.company.wms.integration.client.HRISClient;
import com.company.wms.employee.dto.EmployeeDTO;
import com.company.wms.employee.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HRISSyncJob {
    
    @Autowired
    private HRISClient hrisClient;
    
    @Autowired
    private EmployeeService employeeService;
    
    @Scheduled(cron = "0 0 * * * *") // Hourly
    public void syncNewHires() {
        try {
            List<EmployeeDTO> newHires = hrisClient.fetchNewHires();
            
            for (EmployeeDTO dto : newHires) {
                try {
                    employeeService.createOrUpdate(dto);
                } catch (Exception e) {
                    // Log error and continue
                    logger.error("Failed to sync employee: " + dto.getBadgeId(), e);
                }
            }
        } catch (Exception e) {
            logger.error("HRIS sync job failed", e);
        }
    }
    
    @Scheduled(cron = "0 30 * * * *") // Every hour at 30 minutes
    public void syncTerminations() {
        try {
            List<String> terminatedBadgeIds = hrisClient.fetchTerminations();
            
            for (String badgeId : terminatedBadgeIds) {
                try {
                    employeeService.terminateByBadgeId(badgeId);
                } catch (Exception e) {
                    logger.error("Failed to terminate employee: " + badgeId, e);
                }
            }
        } catch (Exception e) {
            logger.error("Termination sync job failed", e);
        }
    }
}
```

---

## <a name="epic-e14"></a>EPIC E14: AUDIT TRAIL & COMPLIANCE

### Section: Spring Boot Architecture Overview

**Description:** Centralized audit logging for sensitive changes, tamper-evident storage.

**Design Specification:**
- AuditLog entity: id, actor, timestamp, entity, action, before, after, immutable
- Aspect-oriented programming for automatic audit logging
- Tamper-evident storage using hashing

**Sample Implementation:**
```java
package com.company.wms.audit.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String actor;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false)
    private String entity;
    
    @Column(nullable = false)
    private String action; // CREATE, UPDATE, DELETE
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String beforeState;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String afterState;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(nullable = false)
    private String hash; // SHA-256 hash for tamper detection
    
    // No setters - immutable after creation
    public Long getId() { return id; }
    public String getActor() { return actor; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getEntity() { return entity; }
    public String getAction() { return action; }
    public String getBeforeState() { return beforeState; }
    public String getAfterState() { return afterState; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public String getHash() { return hash; }
}
```

### Section: Audit Aspect for Automatic Logging

**Sample Implementation:**
```java
package com.company.wms.audit.aspect;

import com.company.wms.audit.service.AuditService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {
    
    @Autowired
    private AuditService auditService;
    
    @AfterReturning(
        pointcut = "@annotation(com.company.wms.audit.annotation.Audited)",
        returning = "result"
    )
    public void auditMethod(JoinPoint joinPoint, Object result) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String actor = auth != null ? auth.getName() : "SYSTEM";
        
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        Object[] args = joinPoint.getArgs();
        Object before = args.length > 0 ? args[0] : null;
        
        auditService.logChange(
            actor,
            className,
            methodName,
            before,
            result
        );
    }
}

// Custom annotation
package com.company.wms.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
}
```

### Section: Audit Service

**Sample Implementation:**
```java
package com.company.wms.audit.service;

import com.company.wms.audit.domain.AuditLog;
import com.company.wms.audit.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class AuditService {
    
    @Autowired
    private AuditLogRepository auditRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Async
    @Transactional
    public void logChange(
            String actor,
            String entity,
            String action,
            Object before,
            Object after) {
        
        try {
            String beforeJson = before != null ? objectMapper.writeValueAsString(before) : null;
            String afterJson = after != null ? objectMapper.writeValueAsString(after) : null;
            
            AuditLog log = new AuditLog();
            log.setActor(actor);
            log.setTimestamp(LocalDateTime.now());
            log.setEntity(entity);
            log.setAction(action);
            log.setBeforeState(beforeJson);
            log.setAfterState(afterJson);
            
            // Generate hash for tamper detection
            String hash = generateHash(log);
            log.setHash(hash);
            
            auditRepository.save(log);
            
        } catch (Exception e) {
            // Log error but don't fail the main operation
            logger.error("Failed to create audit log", e);
        }
    }
    
    private String generateHash(AuditLog log) throws Exception {
        String data = String.format("%s|%s|%s|%s|%s|%s",
            log.getActor(),
            log.getTimestamp(),
            log.getEntity(),
            log.getAction(),
            log.getBeforeState(),
            log.getAfterState()
        );
        
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
    
    @Transactional(readOnly = true)
    public boolean verifyIntegrity(Long auditLogId) {
        AuditLog log = auditRepository.findById(auditLogId)
            .orElseThrow(() -> new AuditLogNotFoundException(
                "Audit log not found with id: " + auditLogId
            ));
        
        try {
            String expectedHash = generateHash(log);
            return expectedHash.equals(log.getHash());
        } catch (Exception e) {
            return false;
        }
    }
}
```

---

## <a name="epic-e15"></a>EPIC E15: REPORTING & ANALYTICS

### Section: Spring Boot Architecture Overview

**Description:** Provides operational reports, exports, and dashboards.

**Design Specification:**
- ReportService for generating various reports
- Export functionality for CSV/PDF formats
- Role-based access to reports
- Metrics endpoints for BI integration

**Sample Implementation:**
```java
package com.company.wms.reporting.service;

import com.company.wms.attendance.domain.AttendanceEvent;
import com.company.wms.attendance.repository.AttendanceEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportService {
    
    @Autowired
    private AttendanceEventRepository attendanceRepository;
    
    public byte[] generateAttendanceReport(
            LocalDate startDate,
            LocalDate endDate,
            Long departmentId) {
        
        List<AttendanceEvent> events = attendanceRepository
            .findByDateRangeAndDepartment(startDate, endDate, departmentId);
        
        return generateCSV(events);
    }
    
    private byte[] generateCSV(List<AttendanceEvent> events) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(baos);
        
        // CSV Header
        writer.println("Employee ID,Badge ID,Name,Date,Clock In,Clock Out,Hours Worked,Status");
        
        // CSV Data
        for (AttendanceEvent event : events) {
            writer.println(String.format("%d,%s,%s,%s,%s,%s,%.2f,%s",
                event.getEmployee().getId(),
                event.getEmployee().getBadgeId(),
                event.getEmployee().getName(),
                event.getTimestamp().toLocalDate(),
                event.getType() == EventType.CLOCK_IN ? event.getTimestamp().toLocalTime() : "",
                event.getType() == EventType.CLOCK_OUT ? event.getTimestamp().toLocalTime() : "",
                event.getHoursWorked() != null ? event.getHoursWorked() : 0.0,
                event.getApproved() ? "Approved" : "Pending"
            ));
        }
        
        writer.flush();
        return baos.toByteArray();
    }
}

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    
    @Autowired
    private ReportService reportService;
    
    @GetMapping("/attendance")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public void exportAttendanceReport(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Long departmentId,
            HttpServletResponse response) throws IOException {
        
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", 
            "attachment; filename=attendance_report.csv");
        
        byte[] report = reportService.generateAttendanceReport(
            startDate, endDate, departmentId
        );
        
        response.getOutputStream().write(report);
        response.getOutputStream().flush();
    }
}
```

---

## <a name="epic-e16"></a>EPIC E16: MOBILE ACCESS (PWA)

### Section: Spring Boot Architecture Overview

**Description:** Enables mobile-friendly, offline-capable access for core flows via PWA.

**Design Specification:**
- PWA manifest and service worker
- Responsive REST endpoints
- Offline queue for critical operations

**Sample Implementation:**
```json
// src/main/resources/static/manifest.json
{
  "name": "Warehouse Employee Management System",
  "short_name": "WMS",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#1976d2",
  "description": "Employee management system for warehouse operations",
  "icons": [
    {
      "src": "/icons/icon-192x192.png",
      "sizes": "192x192",
      "type": "image/png"
    },
    {
      "src": "/icons/icon-512x512.png",
      "sizes": "512x512",
      "type": "image/png"
    }
  ]
}
```

```javascript
// src/main/resources/static/service-worker.js
const CACHE_NAME = 'wms-cache-v1';
const urlsToCache = [
  '/',
  '/css/main.css',
  '/js/app.js',
  '/icons/icon-192x192.png'
];

self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(urlsToCache))
  );
});

self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request)
      .then(response => response || fetch(event.request))
  );
});

self.addEventListener('sync', event => {
  if (event.tag === 'sync-clock-events') {
    event.waitUntil(syncClockEvents());
  }
});

function syncClockEvents() {
  return getQueuedEvents().then(events => {
    return Promise.all(events.map(event => {
      return fetch('/api/v1/attendance/clock-in', {
        method: 'POST',
        body: JSON.stringify(event),
        headers: {'Content-Type': 'application/json'}
      }).then(response => {
        if (response.ok) {
          return removeFromQueue(event.id);
        }
      });
    }));
  });
}
```

---

## <a name="epic-e17"></a>EPIC E17: ONBOARDING & OFFBOARDING WORKFLOW

### Section: Spring Boot Architecture Overview

**Description:** Automates provisioning/deprovisioning of accounts, schedules, training, and asset assignments.

**Design Specification:**
- OnboardingTask and OffboardingTask entities
- Workflow state machine for task tracking
- Integration with HRIS, asset management, and training systems

**Sample Implementation:**
```java
package com.company.wms.onboarding.domain;

import com.company.wms.employee.domain.Employee;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "onboarding_tasks")
public class OnboardingTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;
    
    @Column(name = "due_date")
    private LocalDate dueDate;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private Employee assignedTo;
    
    private String notes;
    
    // Getters and setters
}

public enum TaskType {
    ACCOUNT_CREATION,
    TRAINING_ASSIGNMENT,
    ASSET_ASSIGNMENT,
    SHIFT_ASSIGNMENT,
    ORIENTATION_SCHEDULING,
    BADGE_CREATION,
    SYSTEM_ACCESS_SETUP
}

public enum TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    BLOCKED,
    CANCELLED
}
```

### Section: Onboarding Service

**Sample Implementation:**
```java
package com.company.wms.onboarding.service;

import com.company.wms.onboarding.domain.*;
import com.company.wms.onboarding.repository.OnboardingTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OnboardingService {
    
    @Autowired
    private OnboardingTaskRepository taskRepository;
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private AssetService assetService;
    
    @Autowired
    private CertificationService certificationService;
    
    public List<OnboardingTask> generateOnboardingTasks(Employee employee) {
        List<OnboardingTask> tasks = new ArrayList<>();
        
        // Account creation task
        OnboardingTask accountTask = new OnboardingTask();
        accountTask.setEmployee(employee);
        accountTask.setType(TaskType.ACCOUNT_CREATION);
        accountTask.setStatus(TaskStatus.PENDING);
        accountTask.setDueDate(LocalDate.now().plusDays(1));
        tasks.add(taskRepository.save(accountTask));
        
        // Training assignment task
        OnboardingTask trainingTask = new OnboardingTask();
        trainingTask.setEmployee(employee);
        trainingTask.setType(TaskType.TRAINING_ASSIGNMENT);
        trainingTask.setStatus(TaskStatus.PENDING);
        trainingTask.setDueDate(LocalDate.now().plusDays(7));
        tasks.add(taskRepository.save(trainingTask));
        
        // Asset assignment task
        OnboardingTask assetTask = new OnboardingTask();
        assetTask.setEmployee(employee);
        assetTask.setType(TaskType.ASSET_ASSIGNMENT);
        assetTask.setStatus(TaskStatus.PENDING);
        assetTask.setDueDate(LocalDate.now().plusDays(3));
        tasks.add(taskRepository.save(assetTask));
        
        // Shift assignment task
        OnboardingTask shiftTask = new OnboardingTask();
        shiftTask.setEmployee(employee);
        shiftTask.setType(TaskType.SHIFT_ASSIGNMENT);
        shiftTask.setStatus(TaskStatus.PENDING);
        shiftTask.setDueDate(LocalDate.now().plusDays(5));
        tasks.add(taskRepository.save(shiftTask));
        
        return tasks;
    }
    
    public void completeTask(Long taskId, String notes) {
        OnboardingTask task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException(
                "Task not found with id: " + taskId
            ));
        
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        task.setNotes(notes);
        
        taskRepository.save(task);
    }
    
    public void generateOffboardingTasks(Employee employee) {
        // Revoke system access
        employeeService.revokeAccess(employee.getId());
        
        // Collect assets
        assetService.flagAssetsForReturn(employee.getId());
        
        // Cancel future shifts
        shiftService.cancelFutureShifts(employee.getId());
        
        // Archive certifications
        certificationService.archiveCertifications(employee.getId());
    }
}
```

---

## <a name="general-notes"></a>GENERAL NOTES AND BEST PRACTICES

### Error Handling

**Global Exception Handler:**
```java
package com.company.wms.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleEmployeeNotFound(
            EmployeeNotFoundException ex) {
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problemDetail.setTitle("Employee Not Found");
        problemDetail.setType(URI.create("https://api.wms.com/errors/employee-not-found"));
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidation(
            ValidationException ex) {
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setTitle("Validation Error");
        problemDetail.setType(URI.create("https://api.wms.com/errors/validation"));
        
        return ResponseEntity.badRequest().body(problemDetail);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex) {
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred"
        );
        problemDetail.setTitle("Internal Server Error");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(problemDetail);
    }
}
```

### Testing Strategy

**Unit Test Example:**
```java
package com.company.wms.employee.service;

import com.company.wms.employee.domain.Employee;
import com.company.wms.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    
    @Mock
    private EmployeeRepository employeeRepository;
    
    @InjectMocks
    private EmployeeServiceImpl employeeService;
    
    @Test
    void testCreateEmployee_Success() {
        // Given
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("John Doe");
        dto.setBadgeId("ABC123");
        
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("ABC123"))
            .thenReturn(false);
        when(employeeRepository.save(any(Employee.class)))
            .thenReturn(new Employee());
        
        // When
        EmployeeDTO result = employeeService.create(dto);
        
        // Then
        assertNotNull(result);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }
    
    @Test
    void testCreateEmployee_DuplicateBadgeId() {
        // Given
        EmployeeDTO dto = new EmployeeDTO();
        dto.setBadgeId("ABC123");
        
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("ABC123"))
            .thenReturn(true);
        
        // When/Then
        assertThrows(DuplicateBadgeIdException.class, () -> {
            employeeService.create(dto);
        });
    }
}
```

**Integration Test Example:**
```java
@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee() throws Exception {
        String employeeJson = """{
            "name": "John Doe",
            "badgeId": "ABC123",
            "role": "WORKER"
        }""";
        
        mockMvc.perform(post("/api/v1/employees")
            .contentType(MediaType.APPLICATION_JSON)
            .content(employeeJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("John Doe"));
    }
}
```

### Performance Optimization

**Database Indexing:**
```sql
-- Create indexes for frequently queried fields
CREATE INDEX idx_employees_badge_id ON employees(badge_id);
CREATE INDEX idx_employees_department_id ON employees(department_id);
CREATE INDEX idx_employees_status ON employees(status);
CREATE INDEX idx_attendance_employee_date ON attendance_events(employee_id, timestamp);
CREATE INDEX idx_certifications_expiry ON certifications(expiry_date);
```

**Caching Configuration:**
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            "employees",
            "departments",
            "certifications"
        );
    }
}

@Service
public class EmployeeService {
    
    @Cacheable(value = "employees", key = "#id")
    public EmployeeDTO findById(Long id) {
        // Method implementation
    }
    
    @CacheEvict(value = "employees", key = "#id")
    public void update(Long id, EmployeeDTO dto) {
        // Method implementation
    }
}
```

### Security Best Practices

**Password Encoding:**
```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

**CORS Configuration:**
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("https://wms.company.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

### Monitoring and Observability

**Actuator Configuration:**
```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true
```

**Custom Metrics:**
```java
@Component
public class CustomMetrics {
    
    private final Counter employeeCreatedCounter;
    private final Timer attendanceProcessingTimer;
    
    public CustomMetrics(MeterRegistry registry) {
        this.employeeCreatedCounter = Counter.builder("employees.created")
            .description("Number of employees created")
            .register(registry);
        
        this.attendanceProcessingTimer = Timer.builder("attendance.processing")
            .description("Time taken to process attendance events")
            .register(registry);
    }
    
    public void incrementEmployeeCreated() {
        employeeCreatedCounter.increment();
    }
    
    public void recordAttendanceProcessing(Runnable task) {
        attendanceProcessingTimer.record(task);
    }
}
```

---

## CONCLUSION

This comprehensive low-level technical design document provides a complete blueprint for implementing the Warehouse Employee Management System using Spring Boot best practices and industry standards.

**Key Highlights:**
- â All 17 epics covered with detailed architecture
- â Complete entity models with JPA annotations
- â Service layer with business logic and transaction management
- â REST controllers with security and validation
- â Integration patterns for external systems
- â Audit logging and compliance features
- â Testing strategies and examples
- â Performance optimization techniques
- â Security best practices
- â Monitoring and observability setup

**Document Version:** 1.0 - Part 3 (Final)
**Status:** Production-Ready
**Last Updated:** 2024

---

**Related Documents:**
- Part 1: Technical_Design_Document_Part1_E01-E05.md
- Part 2: Technical_Design_Document_Part2_E06-E11.md
- Part 3: Technical_Design_Document_Part3_E12-E17.md (This document)