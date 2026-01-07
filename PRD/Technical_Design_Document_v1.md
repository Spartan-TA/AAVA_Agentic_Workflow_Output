# Technical Design Document for User Management and Reporting Platform (Spring Boot)

---

## Table of Contents
1. [User Registration](#user-registration)
2. [User Login](#user-login)
3. [Password Reset](#password-reset)
4. [Profile Management](#profile-management)
5. [Role-Based Access Control](#role-based-access-control)
6. [Data Visualization Dashboard](#data-visualization-dashboard)
7. [Export Reports](#export-reports)
8. [In-App Notifications](#in-app-notifications)
9. [Email Notifications](#email-notifications)
10. [Audit Logging](#audit-logging)
11. [Bulk User Import](#bulk-user-import)
12. [Two-Factor Authentication (2FA)](#two-factor-authentication-2fa)
13. [User Search and Filtering](#user-search-and-filtering)
14. [Scheduled Reports](#scheduled-reports)

---

## 1. User Registration

### Description
New users can register for accounts. Registration includes email confirmation, validation for duplicate emails, and invalid data handling.

### Design Decisions
- Use DTOs for request/response.
- Email confirmation via token.
- Validation via annotations.
- Duplicate email check at service layer.

### Design Specification
- **Entity:** `User`, `VerificationToken`
- **Fields:**
  - User: id, email, password, enabled, roles, createdAt
  - VerificationToken: id, token, user, expiryDate
- **Relationships:** One-to-One (User <-> VerificationToken)
- **Service:** `UserService` (registerUser, confirmEmail)
- **Repository:** `UserRepository`, `VerificationTokenRepository`
- **Controller:** `AuthController` (register, confirmEmail)
- **Validation:** `@Email`, `@NotBlank`, custom duplicate check
- **Security:** Registration endpoint is public
- **Integration:** Email service for confirmation

### Sample Implementation
```java
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    @Email @NotBlank @Column(unique=true)
    private String email;
    @NotBlank
    private String password;
    private boolean enabled;
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;
    private LocalDateTime createdAt;
}

@Entity
public class VerificationToken {
    @Id @GeneratedValue
    private Long id;
    private String token;
    @OneToOne
    private User user;
    private LocalDateTime expiryDate;
}

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest dto) {
        userService.registerUser(dto);
        return ResponseEntity.ok("Registration successful. Check your email.");
    }
    @GetMapping("/confirm")
    public ResponseEntity<?> confirmEmail(@RequestParam String token) {
        userService.confirmEmail(token);
        return ResponseEntity.ok("Email confirmed.");
    }
}
```

---

## 2. User Login

### Description
Registered users log in securely. Account is locked after 5 failed attempts. CAPTCHA is required after 3 failed attempts.

### Design Decisions
- Track failed login attempts in User entity.
- Integrate CAPTCHA (e.g., Google reCAPTCHA) after 3 attempts.
- Lock account after 5 attempts.
- Use Spring Security for authentication.

### Design Specification
- **Entity:** `User` (failedAttempts, accountLocked, lastFailedLogin)
- **Service:** `AuthService` (authenticate, handleFailedLogin)
- **Controller:** `AuthController` (login)
- **Security:** Custom authentication provider
- **Integration:** CAPTCHA service

### Sample Implementation
```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest dto) {
    if (userService.isCaptchaRequired(dto.getEmail())) {
        if (!captchaService.verify(dto.getCaptchaResponse())) {
            throw new BadCredentialsException("CAPTCHA failed");
        }
    }
    authService.authenticate(dto);
    return ResponseEntity.ok("Login successful");
}

@Service
public class AuthService {
    public void authenticate(LoginRequest dto) {
        // Check user, password, lockout, increment failedAttempts, etc.
    }
}
```

---

## 3. Password Reset

### Description
Users can reset forgotten passwords via an email link that expires after 1 hour.

### Design Decisions
- Use a `PasswordResetToken` entity.
- Email link contains token.
- Token expires after 1 hour.

### Design Specification
- **Entity:** `PasswordResetToken` (id, token, user, expiryDate)
- **Service:** `PasswordResetService` (createToken, validateToken, resetPassword)
- **Controller:** `PasswordController` (requestReset, resetPassword)
- **Integration:** Email service

### Sample Implementation
```java
@Entity
public class PasswordResetToken {
    @Id @GeneratedValue
    private Long id;
    private String token;
    @OneToOne
    private User user;
    private LocalDateTime expiryDate;
}

@PostMapping("/password/reset-request")
public ResponseEntity<?> requestReset(@RequestBody EmailDto dto) {
    passwordResetService.createToken(dto.getEmail());
    return ResponseEntity.ok("Reset email sent");
}
```

---

## 4. Profile Management

### Description
Users can update their profile information. All changes are validated and audited.

### Design Decisions
- Use DTOs for profile updates.
- Audit changes (who, what, when).
- Validation annotations.

### Design Specification
- **Entity:** `User`, `ProfileAudit`
- **Service:** `ProfileService` (updateProfile)
- **Repository:** `ProfileAuditRepository`
- **Controller:** `ProfileController` (updateProfile)
- **Security:** Only authenticated user can update own profile

### Sample Implementation
```java
@Entity
public class ProfileAudit {
    @Id @GeneratedValue
    private Long id;
    private Long userId;
    private String fieldChanged;
    private String oldValue;
    private String newValue;
    private LocalDateTime changedAt;
}

@PutMapping("/profile")
public ResponseEntity<?> updateProfile(@Valid @RequestBody ProfileUpdateDto dto) {
    profileService.updateProfile(dto);
    return ResponseEntity.ok("Profile updated");
}
```

---

## 5. Role-Based Access Control

### Description
Administrators assign roles (Admin, Manager, User) with appropriate permission restrictions.

### Design Decisions
- Use Spring Security roles/authorities.
- Role assignment via admin endpoints.

### Design Specification
- **Entity:** `Role` (id, name), `User` (roles)
- **Service:** `RoleService` (assignRole)
- **Controller:** `AdminController` (assignRole)
- **Security:** Method-level security (`@PreAuthorize`)

### Sample Implementation
```java
@Entity
public class Role {
    @Id @GeneratedValue
    private Long id;
    private String name;
}

@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/admin/assign-role")
public ResponseEntity<?> assignRole(@RequestBody RoleAssignDto dto) {
    roleService.assignRole(dto);
    return ResponseEntity.ok("Role assigned");
}
```

---

## 6. Data Visualization Dashboard

### Description
Managers view metrics in charts/graphs with date range filtering.

### Design Decisions
- Expose REST endpoints for metrics data.
- Use DTOs for chart data.
- Date range filtering via query params.

### Design Specification
- **Service:** `DashboardService` (getMetrics)
- **Controller:** `DashboardController` (getMetrics)
- **Security:** Only managers and above
- **Integration:** Frontend chart library

### Sample Implementation
```java
@GetMapping("/dashboard/metrics")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public ResponseEntity<MetricsDto> getMetrics(@RequestParam LocalDate start, @RequestParam LocalDate end) {
    return ResponseEntity.ok(dashboardService.getMetrics(start, end));
}
```

---

## 7. Export Reports

### Description
Users export reports in PDF and CSV formats with applied filters.

### Design Decisions
- Use service to generate reports.
- Use libraries (e.g., iText for PDF, OpenCSV for CSV).
- Filter parameters via query params.

### Design Specification
- **Service:** `ReportService` (generatePdf, generateCsv)
- **Controller:** `ReportController` (exportPdf, exportCsv)
- **Integration:** File download

### Sample Implementation
```java
@GetMapping("/reports/export/pdf")
public ResponseEntity<Resource> exportPdf(@RequestParam Map<String, String> filters) {
    Resource pdf = reportService.generatePdf(filters);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
        .body(pdf);
}
```

---

## 8. In-App Notifications

### Description
Users receive real-time in-app notifications with read/unread status and history.

### Design Decisions
- Use WebSocket (Spring) for real-time.
- Store notifications in DB.
- Read/unread status per user.

### Design Specification
- **Entity:** `Notification` (id, user, message, read, createdAt)
- **Service:** `NotificationService` (send, markRead, getHistory)
- **Controller:** `NotificationController` (REST + WebSocket)
- **Integration:** WebSocket config

### Sample Implementation
```java
@Entity
public class Notification {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private User user;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}

@MessageMapping("/notify")
public void sendNotification(NotificationDto dto) {
    notificationService.send(dto);
}
```

---

## 9. Email Notifications

### Description
Users receive email notifications for critical actions with unsubscribe capability.

### Design Decisions
- Use `EmailNotification` entity for tracking.
- Unsubscribe token per user.

### Design Specification
- **Entity:** `EmailNotification` (id, user, type, sentAt, unsubscribed)
- **Service:** `EmailService` (send, unsubscribe)
- **Controller:** `EmailController` (unsubscribe)
- **Integration:** Email provider

### Sample Implementation
```java
@PostMapping("/email/unsubscribe")
public ResponseEntity<?> unsubscribe(@RequestParam String token) {
    emailService.unsubscribe(token);
    return ResponseEntity.ok("Unsubscribed");
}
```

---

## 10. Audit Logging

### Description
Administrators view and export audit logs of user actions with filtering capabilities.

### Design Decisions
- Store audit logs in DB.
- Filter by user, action, date.
- Export as CSV.

### Design Specification
- **Entity:** `AuditLog` (id, user, action, details, timestamp)
- **Service:** `AuditService` (logAction, getLogs, exportLogs)
- **Controller:** `AuditController` (getLogs, exportLogs)

### Sample Implementation
```java
@Entity
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private User user;
    private String action;
    private String details;
    private LocalDateTime timestamp;
}

@GetMapping("/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<List<AuditLogDto>> getLogs(@RequestParam Map<String, String> filters) {
    return ResponseEntity.ok(auditService.getLogs(filters));
}
```

---

## 11. Bulk User Import

### Description
Administrators import multiple users via CSV with error reporting and duplicate handling.

### Design Decisions
- Parse CSV, validate each row.
- Report errors per row.
- Skip or update duplicates.

### Design Specification
- **Service:** `UserImportService` (importCsv)
- **Controller:** `AdminController` (importUsers)
- **Integration:** File upload

### Sample Implementation
```java
@PostMapping("/admin/import-users")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ImportResultDto> importUsers(@RequestParam MultipartFile file) {
    return ResponseEntity.ok(userImportService.importCsv(file));
}
```

---

## 12. Two-Factor Authentication (2FA)

### Description
Users enable 2FA for enhanced security using authenticator apps or SMS.

### Design Decisions
- Store 2FA secret in User entity.
- Support TOTP (Google Authenticator) and SMS.
- 2FA required at login if enabled.

### Design Specification
- **Entity:** `User` (twoFaEnabled, twoFaSecret, phoneNumber)
- **Service:** `TwoFaService` (enable2FA, verify2FA)
- **Controller:** `TwoFaController` (enable, verify)
- **Integration:** SMS provider

### Sample Implementation
```java
@PostMapping("/2fa/enable")
public ResponseEntity<?> enable2FA(@RequestBody TwoFaEnableDto dto) {
    twoFaService.enable2FA(dto);
    return ResponseEntity.ok("2FA enabled");
}
```

---

## 13. User Search and Filtering

### Description
Administrators search and filter users by name, email, and role with pagination.

### Design Decisions
- Use Spring Data JPA Specifications.
- Pagination via Pageable.

### Design Specification
- **Repository:** `UserRepository` (findAll(Specification, Pageable))
- **Service:** `UserService` (searchUsers)
- **Controller:** `AdminController` (searchUsers)

### Sample Implementation
```java
@GetMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Page<UserDto>> searchUsers(@RequestParam Map<String, String> filters, Pageable pageable) {
    return ResponseEntity.ok(userService.searchUsers(filters, pageable));
}
```

---

## 14. Scheduled Reports

### Description
Managers schedule automatic report generation and email delivery (daily, weekly, monthly).

### Design Decisions
- Use Spring Scheduler (`@Scheduled`).
- Store schedule in DB.
- Email reports as attachments.

### Design Specification
- **Entity:** `ReportSchedule` (id, user, frequency, nextRun, filters)
- **Service:** `ReportScheduleService` (scheduleReport, runScheduledReports)
- **Controller:** `ReportScheduleController` (createSchedule)
- **Integration:** Email service

### Sample Implementation
```java
@Entity
public class ReportSchedule {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private User user;
    private String frequency; // DAILY, WEEKLY, MONTHLY
    private LocalDateTime nextRun;
    private String filtersJson;
}

@Scheduled(cron = "0 0 0 * * *")
public void runScheduledReports() {
    reportScheduleService.runScheduledReports();
}
```

---

# Spring Boot Architecture Overview
- **Layered Architecture:** Controller -> Service -> Repository
- **DTOs:** Used for all API requests/responses
- **Exception Handling:** `@ControllerAdvice` for global error handling
- **Validation:** Bean Validation (`javax.validation`)
- **Security:** Spring Security with JWT, method-level security
- **Persistence:** Spring Data JPA
- **Configuration:** application.yml for environment settings
- **Integration:** Email, SMS, WebSocket, File export

# Package Structure
```
com.example.app
âââ config
âââ controller
âââ dto
âââ entity
âââ exception
âââ repository
âââ security
âââ service
âââ util
```

# Configuration and Security Settings
- **application.yml:** DB, email, SMS, JWT, WebSocket configs
- **SecurityConfig:**
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/api/auth/**", "/password/**").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
```

# Integration Points
- **Email:** JavaMailSender
- **SMS:** Twilio or similar
- **WebSocket:** Spring WebSocket
- **PDF/CSV:** iText, OpenCSV
- **CAPTCHA:** Google reCAPTCHA API

# Exception Handling
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        // Return validation errors
    }
    // ... other handlers
}
```

---

# End of Document