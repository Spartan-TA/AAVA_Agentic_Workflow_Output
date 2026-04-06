# Technical Design Document

## Table of Contents
1. User Registration â Email Signup
2. User Login â Secure Authentication
3. Password Reset â Forgot Password Flow
4. Profile Management â Update Personal Information
5. Dashboard â View Recent Activity
6. Admin Panel â Manage Users
7. Notifications â Email Alerts for Important Events
8. Search â Find Content by Keyword
9. Mobile Responsiveness â Adaptive UI
10. Data Export â Download User Data

---

# 1. User Registration â Email Signup

**Description:**
New users register using email to create personalized accounts. Includes email service integration, password security, and confirmation email.

### Spring Boot Architecture Overview
- Follows layered architecture: Controller â Service â Repository â Entity
- Uses Spring Security for password encryption
- Integrates with external email service (e.g., SendGrid)

### Package Structure
```
com.example.app
âââ controller
âââ service
âââ repository
âââ model
âââ config
âââ util
```

### Entity Design
- `User` entity with fields: id, email, password, isActive, isEmailVerified, createdAt, updatedAt
- Relationship: One-to-Many with `UserRole` (for future extensibility)

```java
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    private boolean isActive;
    private boolean isEmailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // getters/setters
}
```

### Service Layer Specifications
- `UserService.registerUser(UserRegistrationDto dto)`
    - Validates email uniqueness
    - Encrypts password (BCrypt)
    - Persists user with isActive=false, isEmailVerified=false
    - Sends confirmation email with token

### Repository Layer Specifications
- `UserRepository extends JpaRepository<User, Long>`
    - `Optional<User> findByEmail(String email)`

### Controller Specifications
- `POST /api/auth/register`
    - Accepts registration payload
    - Returns success/failure

### Configuration and Security Settings
- Password encoding bean (BCryptPasswordEncoder)
- Email service configuration (API keys, SMTP)

### Integration Points
- Email service for confirmation
- Token generation utility for email verification

### Sample Implementation
```java
// UserService.java
public void registerUser(UserRegistrationDto dto) {
    if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
        throw new EmailAlreadyUsedException();
    }
    User user = new User();
    user.setEmail(dto.getEmail());
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    user.setIsActive(false);
    user.setIsEmailVerified(false);
    user.setCreatedAt(LocalDateTime.now());
    userRepository.save(user);
    String token = tokenUtil.generateToken(user.getEmail());
    emailService.sendConfirmationEmail(user.getEmail(), token);
}
```

---

# 2. User Login â Secure Authentication

**Description:**
Registered users log in securely to access dashboard. Includes authentication backend and account lockout after 5 failed attempts.

### Spring Boot Architecture Overview
- Uses Spring Security for authentication
- Custom authentication provider for lockout logic

### Package Structure
- `security` package for custom authentication logic

### Entity Design
- `User` entity extended with `failedLoginAttempts`, `lastFailedLogin`, `accountLockedUntil`

```java
private int failedLoginAttempts;
private LocalDateTime lastFailedLogin;
private LocalDateTime accountLockedUntil;
```

### Service Layer Specifications
- `AuthenticationService.authenticate(email, password)`
    - Checks lockout status
    - Validates credentials
    - Increments failed attempts or resets on success
    - Locks account for 15 minutes after 5 failures

### Repository Layer Specifications
- `UserRepository` methods for updating login attempts

### Controller Specifications
- `POST /api/auth/login`
    - Accepts credentials
    - Returns JWT token or error

### Configuration and Security Settings
- JWT-based authentication
- Custom authentication filter

### Integration Points
- None external

### Sample Implementation
```java
if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
    throw new AccountLockedException();
}
if (!passwordEncoder.matches(password, user.getPassword())) {
    user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
    if (user.getFailedLoginAttempts() >= 5) {
        user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(15));
    }
    userRepository.save(user);
    throw new BadCredentialsException();
}
user.setFailedLoginAttempts(0);
user.setAccountLockedUntil(null);
userRepository.save(user);
```

---

# 3. Password Reset â Forgot Password Flow

**Description:**
Users reset forgotten passwords via email. Reset link expires in 24 hours.

### Spring Boot Architecture Overview
- Stateless reset token stored in DB
- Email integration for reset link

### Package Structure
- `model.PasswordResetToken`

### Entity Design
- `PasswordResetToken` with fields: id, user, token, expiryDate

```java
@Entity
public class PasswordResetToken {
    @Id @GeneratedValue
    private Long id;
    @OneToOne
    private User user;
    private String token;
    private LocalDateTime expiryDate;
}
```

### Service Layer Specifications
- `PasswordResetService.createResetToken(email)`
- `PasswordResetService.resetPassword(token, newPassword)`

### Repository Layer Specifications
- `PasswordResetTokenRepository` with `findByToken(String token)`

### Controller Specifications
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`

### Configuration and Security Settings
- Token expiry validation (24 hours)

### Integration Points
- Email service

### Sample Implementation
```java
PasswordResetToken token = new PasswordResetToken();
token.setUser(user);
token.setToken(UUID.randomUUID().toString());
token.setExpiryDate(LocalDateTime.now().plusHours(24));
passwordResetTokenRepository.save(token);
emailService.sendResetLink(user.getEmail(), token.getToken());
```

---

# 4. Profile Management â Update Personal Information

**Description:**
Users update profile information with immediate reflection. Email changes require re-verification.

### Spring Boot Architecture Overview
- RESTful endpoints for profile update
- Event-driven email re-verification

### Package Structure
- `event` package for profile update events

### Entity Design
- `User` entity with profile fields: firstName, lastName, phone, etc.

### Service Layer Specifications
- `UserService.updateProfile(userId, ProfileUpdateDto)`
    - If email changes, set isEmailVerified=false and send verification

### Repository Layer Specifications
- Standard CRUD

### Controller Specifications
- `PUT /api/user/profile`

### Configuration and Security Settings
- Secured endpoint (authenticated users only)

### Integration Points
- Email service for re-verification

### Sample Implementation
```java
if (!user.getEmail().equals(dto.getEmail())) {
    user.setEmail(dto.getEmail());
    user.setIsEmailVerified(false);
    emailService.sendConfirmationEmail(user.getEmail(), tokenUtil.generateToken(user.getEmail()));
}
user.setFirstName(dto.getFirstName());
user.setLastName(dto.getLastName());
userRepository.save(user);
```

---

# 5. Dashboard â View Recent Activity

**Description:**
Users view last 30 days of recent activity on dashboard. Activity tracking module required.

### Spring Boot Architecture Overview
- Activity logging via aspect or service calls

### Package Structure
- `model.ActivityLog`
- `service.ActivityLogService`

### Entity Design
- `ActivityLog` with fields: id, user, action, timestamp, metadata

```java
@Entity
public class ActivityLog {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private User user;
    private String action;
    private LocalDateTime timestamp;
    private String metadata;
}
```

### Service Layer Specifications
- `ActivityLogService.logAction(user, action, metadata)`
- `ActivityLogService.getRecentActivities(userId, from, to)`

### Repository Layer Specifications
- `findByUserAndTimestampBetween(User user, LocalDateTime from, LocalDateTime to)`

### Controller Specifications
- `GET /api/dashboard/activity`

### Configuration and Security Settings
- Secured endpoint

### Integration Points
- None

### Sample Implementation
```java
List<ActivityLog> activities = activityLogRepository.findByUserAndTimestampBetween(
    user, LocalDateTime.now().minusDays(30), LocalDateTime.now()
);
```

---

# 6. Admin Panel â Manage Users

**Description:**
Admins view and manage all user accounts with edit/deactivate options. Only super admins can delete accounts.

### Spring Boot Architecture Overview
- Role-based access control (RBAC) with Spring Security

### Package Structure
- `controller.AdminUserController`

### Entity Design
- `UserRole` entity: id, user, role (ADMIN, SUPER_ADMIN, USER)

### Service Layer Specifications
- `AdminUserService.listUsers()`
- `AdminUserService.editUser(userId, dto)`
- `AdminUserService.deactivateUser(userId)`
- `AdminUserService.deleteUser(userId)` (super admin only)

### Repository Layer Specifications
- Standard CRUD

### Controller Specifications
- `GET /api/admin/users`
- `PUT /api/admin/users/{id}`
- `DELETE /api/admin/users/{id}`

### Configuration and Security Settings
- Method-level security: `@PreAuthorize("hasRole('SUPER_ADMIN')")` for delete

### Integration Points
- None

### Sample Implementation
```java
@PreAuthorize("hasRole('SUPER_ADMIN')")
public void deleteUser(Long userId) {
    userRepository.deleteById(userId);
}
```

---

# 7. Notifications â Email Alerts for Important Events

**Description:**
Users receive email notifications for important events with opt-out option for non-critical alerts.

### Spring Boot Architecture Overview
- Event-driven notification system

### Package Structure
- `model.NotificationPreference`
- `service.NotificationService`

### Entity Design
- `NotificationPreference`: id, user, criticalOptIn, nonCriticalOptIn

### Service Layer Specifications
- `NotificationService.sendNotification(user, eventType, data)`
    - Checks user preferences
    - Sends email if opted in

### Repository Layer Specifications
- `NotificationPreferenceRepository`

### Controller Specifications
- `PUT /api/user/notification-preferences`

### Configuration and Security Settings
- Secured endpoints

### Integration Points
- Email service

### Sample Implementation
```java
if (event.isCritical() || preference.isNonCriticalOptIn()) {
    emailService.sendNotification(user.getEmail(), event.getMessage());
}
```

---

# 8. Search â Find Content by Keyword

**Description:**
Users search content using keywords with partial matches and filters. Requires search backend and content indexing.

### Spring Boot Architecture Overview
- Integrates with Elasticsearch or uses JPA Specifications for search

### Package Structure
- `service.SearchService`

### Entity Design
- `Content` entity: id, title, body, tags, createdAt

### Service Layer Specifications
- `SearchService.search(keyword, filters)`
    - Partial match on title/body/tags

### Repository Layer Specifications
- `ContentRepository` with custom search methods

### Controller Specifications
- `GET /api/search?keyword=...&filters=...`

### Configuration and Security Settings
- Public endpoint or authenticated as needed

### Integration Points
- Elasticsearch (optional)

### Sample Implementation
```java
@Query("SELECT c FROM Content c WHERE c.title LIKE %:keyword% OR c.body LIKE %:keyword%")
List<Content> search(@Param("keyword") String keyword);
```

---

# 9. Mobile Responsiveness â Adaptive UI

**Description:**
Site adapts to mobile device screen sizes for iOS and Android.

### Spring Boot Architecture Overview
- Backend serves REST APIs; UI handled by frontend (e.g., React, Angular)
- Backend provides device detection for adaptive responses if needed

### Package Structure
- `controller.DeviceController` (optional)

### Entity Design
- N/A (backend only)

### Service Layer Specifications
- Device detection utility (optional)

### Repository Layer Specifications
- N/A

### Controller Specifications
- `GET /api/device/info` (optional)

### Configuration and Security Settings
- CORS configuration for mobile clients

### Integration Points
- None

### Sample Implementation
```java
// Example: Add CORS mapping for mobile clients
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**").allowedOrigins("*");
}
```

---

# 10. Data Export â Download User Data

**Description:**
Users export their data in CSV format.

### Spring Boot Architecture Overview
- REST endpoint generates CSV on demand

### Package Structure
- `service.DataExportService`

### Entity Design
- Uses existing `User` and related entities

### Service Layer Specifications
- `DataExportService.exportUserData(userId)`
    - Aggregates user data
    - Generates CSV

### Repository Layer Specifications
- Standard CRUD

### Controller Specifications
- `GET /api/user/export`
    - Returns CSV file

### Configuration and Security Settings
- Secured endpoint

### Integration Points
- None

### Sample Implementation
```java
@GetMapping("/api/user/export")
public ResponseEntity<Resource> exportUserData() {
    String csv = dataExportService.exportUserData(currentUserId);
    InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(csv.getBytes()));
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user_data.csv")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(resource);
}
```

---

# End of Document