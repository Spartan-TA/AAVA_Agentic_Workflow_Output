# Low-Level Technical Design Document for 7 User Stories (Spring Boot)

---

## 1. User Login with Email and Password

### 1.1 Overview

This feature enables users to authenticate using their email and password. It includes secure password storage, session management, and account locking after 5 failed attempts.

### 1.2 Design Decisions

- Use Spring Security for authentication and session management.
- Passwords are hashed using BCrypt.
- Account locking is tracked in the database.
- Stateless JWT tokens for session management.
- Layered architecture: Controller â Service â Repository.

### 1.3 Package Structure

```
com.example.auth
âââ config
âââ controller
âââ dto
âââ entity
âââ exception
âââ repository
âââ security
âââ service
```

### 1.4 Entity Design

```java
@Entity
public class User {
    @Id
    @GeneratedValue
    private Long id;
    private String email;
    private String passwordHash;
    private boolean accountLocked;
    private int failedLoginAttempts;
    // getters/setters
}
```

### 1.5 Service Layer

- `AuthService`: Handles authentication, password verification, and account locking.

```java
public interface AuthService {
    AuthResponse login(String email, String password);
}
```

### 1.6 Repository Layer

- `UserRepository extends JpaRepository<User, Long>`
- Custom method: `Optional<User> findByEmail(String email)`

### 1.7 Controller

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // delegate to AuthService
    }
}
```

### 1.8 Configuration & Security

- Configure Spring Security for JWT.
- PasswordEncoder bean (BCrypt).
- Lock account after 5 failed attempts.

### 1.9 Integration Points

- Email service for account lock notification (optional).

### 1.10 Sample Implementation

```java
@Service
public class AuthServiceImpl implements AuthService {
    // Autowire UserRepository, PasswordEncoder, JWT utility
    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException());
        if (user.isAccountLocked()) throw new LockedException();
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            user.incrementFailedAttempts();
            if (user.getFailedLoginAttempts() >= 5) user.setAccountLocked(true);
            userRepository.save(user);
            throw new BadCredentialsException();
        }
        user.resetFailedAttempts();
        userRepository.save(user);
        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token);
    }
}
```

---

## 2. Edit User Profile Information

### 2.1 Overview

Allows users to update their name, email, and phone number with validation and email verification.

### 2.2 Design Decisions

- Use DTOs for update requests.
- Email changes require verification.
- Validation via Hibernate Validator.

### 2.3 Package Structure

```
com.example.profile
âââ controller
âââ dto
âââ entity
âââ repository
âââ service
```

### 2.4 Entity Design

```java
@Entity
public class UserProfile {
    @Id
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private boolean emailVerified;
}
```

### 2.5 Service Layer

- `ProfileService`: Handles update logic and triggers email verification.

```java
public interface ProfileService {
    void updateProfile(Long userId, ProfileUpdateRequest request);
}
```

### 2.6 Repository Layer

- `UserProfileRepository extends JpaRepository<UserProfile, Long>`

### 2.7 Controller

```java
@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    @PutMapping
    public ResponseEntity<Void> updateProfile(@RequestBody @Valid ProfileUpdateRequest request) {
        // delegate to ProfileService
    }
}
```

### 2.8 Configuration & Security

- Endpoints secured for authenticated users.

### 2.9 Integration Points

- Email service for verification.

### 2.10 Sample Implementation

```java
@Service
public class ProfileServiceImpl implements ProfileService {
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        // Validate and update fields
        // If email changed, set emailVerified=false and send verification email
    }
}
```

---

## 3. Display Personalized Dashboard Metrics

### 3.1 Overview

Displays user-specific metrics: total usage, recent activity, notifications, with real-time updates.

### 3.2 Design Decisions

- Use WebSocket (Spring Messaging) for real-time updates.
- Dashboard aggregates data from multiple sources.

### 3.3 Package Structure

```
com.example.dashboard
âââ controller
âââ dto
âââ service
âââ websocket
```

### 3.4 Entity Design

- No new entities; uses existing User, Activity, Notification.

### 3.5 Service Layer

- `DashboardService`: Aggregates metrics.

```java
public interface DashboardService {
    DashboardMetrics getMetrics(Long userId);
}
```

### 3.6 Repository Layer

- Use existing repositories: ActivityRepository, NotificationRepository.

### 3.7 Controller

```java
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    @GetMapping
    public ResponseEntity<DashboardMetrics> getMetrics() {
        // delegate to DashboardService
    }
}
```

### 3.8 Configuration & Security

- WebSocket configuration for `/topic/dashboard`.

### 3.9 Integration Points

- WebSocket for real-time push.

### 3.10 Sample Implementation

```java
@Service
public class DashboardServiceImpl implements DashboardService {
    public DashboardMetrics getMetrics(Long userId) {
        // Fetch and aggregate data from repositories
    }
}
```

```java
@MessageMapping("/dashboard")
@SendTo("/topic/dashboard")
public DashboardMetrics sendMetrics(Long userId) {
    // Real-time push
}
```

---

## 4. Export User Data to CSV

### 4.1 Overview

Allows users to download their data as CSV with error handling.

### 4.2 Design Decisions

- Use OpenCSV or similar for CSV generation.
- Stream response for large datasets.

### 4.3 Package Structure

```
com.example.export
âââ controller
âââ service
```

### 4.4 Entity Design

- Uses existing User and related entities.

### 4.5 Service Layer

- `ExportService`: Generates CSV.

```java
public interface ExportService {
    void writeUserDataToCsv(Long userId, OutputStream os) throws IOException;
}
```

### 4.6 Repository Layer

- Use existing repositories.

### 4.7 Controller

```java
@RestController
@RequestMapping("/api/export")
public class ExportController {
    @GetMapping("/csv")
    public void exportCsv(HttpServletResponse response) {
        // delegate to ExportService
    }
}
```

### 4.8 Configuration & Security

- Endpoint secured for authenticated users.

### 4.9 Integration Points

- None.

### 4.10 Sample Implementation

```java
@Service
public class ExportServiceImpl implements ExportService {
    public void writeUserDataToCsv(Long userId, OutputStream os) throws IOException {
        // Fetch user data, write CSV using OpenCSV
    }
}
```

---

## 5. Real-Time Notification Delivery

### 5.1 Overview

Delivers in-app and email notifications with read status and user preferences.

### 5.2 Design Decisions

- Use WebSocket for in-app notifications.
- Email via SMTP integration.
- Notification preferences stored per user.

### 5.3 Package Structure

```
com.example.notification
âââ controller
âââ dto
âââ entity
âââ repository
âââ service
âââ websocket
```

### 5.4 Entity Design

```java
@Entity
public class Notification {
    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private String message;
    private boolean read;
    private NotificationType type; // IN_APP, EMAIL
    private LocalDateTime createdAt;
}
@Entity
public class NotificationPreference {
    @Id
    private Long userId;
    private boolean inAppEnabled;
    private boolean emailEnabled;
}
```

### 5.5 Service Layer

- `NotificationService`: Sends and manages notifications.

```java
public interface NotificationService {
    void sendNotification(Long userId, String message, NotificationType type);
    void markAsRead(Long notificationId);
}
```

### 5.6 Repository Layer

- `NotificationRepository`, `NotificationPreferenceRepository`

### 5.7 Controller

```java
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @GetMapping
    public List<Notification> getNotifications();
    @PostMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id);
}
```

### 5.8 Configuration & Security

- WebSocket `/topic/notifications`.

### 5.9 Integration Points

- SMTP for email.

### 5.10 Sample Implementation

```java
@Service
public class NotificationServiceImpl implements NotificationService {
    public void sendNotification(Long userId, String message, NotificationType type) {
        // Save notification, send via WebSocket or email based on preferences
    }
}
```

---

## 6. Password Reset via Email

### 6.1 Overview

Enables users to reset their password via email, with link expiration and strong password requirements.

### 6.2 Design Decisions

- Generate secure, time-limited tokens.
- Validate password strength.
- Use Spring Security for password encoding.

### 6.3 Package Structure

```
com.example.passwordreset
âââ controller
âââ dto
âââ entity
âââ repository
âââ service
```

### 6.4 Entity Design

```java
@Entity
public class PasswordResetToken {
    @Id
    @GeneratedValue
    private Long id;
    private String token;
    private Long userId;
    private LocalDateTime expiryDate;
    private boolean used;
}
```

### 6.5 Service Layer

- `PasswordResetService`: Manages token generation and password reset.

```java
public interface PasswordResetService {
    void requestReset(String email);
    void resetPassword(String token, String newPassword);
}
```

### 6.6 Repository Layer

- `PasswordResetTokenRepository`

### 6.7 Controller

```java
@RestController
@RequestMapping("/api/password-reset")
public class PasswordResetController {
    @PostMapping("/request")
    public void requestReset(@RequestBody PasswordResetRequest request);
    @PostMapping("/confirm")
    public void confirmReset(@RequestBody PasswordResetConfirmRequest request);
}
```

### 6.8 Configuration & Security

- Secure endpoints, validate tokens.

### 6.9 Integration Points

- Email service for sending reset links.

### 6.10 Sample Implementation

```java
@Service
public class PasswordResetServiceImpl implements PasswordResetService {
    public void requestReset(String email) {
        // Generate token, save, send email
    }
    public void resetPassword(String token, String newPassword) {
        // Validate token, check expiry, validate password strength, update password
    }
}
```

---

## 7. Delete User Data Permanently

### 7.1 Overview

Implements GDPR-compliant permanent user data deletion with confirmation.

### 7.2 Design Decisions

- Soft delete with scheduled hard delete, or immediate hard delete.
- Cascade delete related data.
- Confirmation required.

### 7.3 Package Structure

```
com.example.deletion
âââ controller
âââ service
```

### 7.4 Entity Design

- Use existing User entity; add `deleted` flag if soft delete.

### 7.5 Service Layer

- `UserDeletionService`: Handles deletion logic.

```java
public interface UserDeletionService {
    void deleteUser(Long userId, String confirmation);
}
```

### 7.6 Repository Layer

- Use existing repositories.

### 7.7 Controller

```java
@RestController
@RequestMapping("/api/delete")
public class UserDeletionController {
    @DeleteMapping
    public void deleteUser(@RequestBody DeleteRequest request);
}
```

### 7.8 Configuration & Security

- Endpoint secured, confirmation required.

### 7.9 Integration Points

- None.

### 7.10 Sample Implementation

```java
@Service
public class UserDeletionServiceImpl implements UserDeletionService {
    public void deleteUser(Long userId, String confirmation) {
        // Validate confirmation, delete user and related data
    }
}
```

---

# End of Document