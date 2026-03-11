# Comprehensive Low-Level Technical Design Document

## Table of Contents
1. [Introduction](#introduction)
2. [Overall Spring Boot Architecture](#overall-spring-boot-architecture)
3. [Package Structure & Module Definitions](#package-structure--module-definitions)
4. [Entity Design & Domain Models](#entity-design--domain-models)
5. [Service, Repository, and Controller Specifications](#service-repository-and-controller-specifications)
6. [Configuration & Security Settings](#configuration--security-settings)
7. [Integration Points](#integration-points)
8. [User Story Breakdown & Design Patterns](#user-story-breakdown--design-patterns)

---

## Introduction
This document provides a detailed technical design for 20 user stories related to user registration, authentication, profile management, dashboard, notifications, and search functionalities. The design follows Spring Boot best practices, ensuring scalability, maintainability, and security.

---

## Overall Spring Boot Architecture
- **Layered Architecture**: Presentation (Controller) â Service â Repository â Data/External
- **Spring Boot Modules**: Web, Data JPA, Security, Mail, Validation, Session, File Storage, etc.
- **Security**: Spring Security with JWT, password encoding, session management, and optional MFA.
- **Persistence**: JPA/Hibernate with PostgreSQL (or other RDBMS).
- **Integration**: Email (JavaMail), external MFA service, file storage (local/cloud), notification service, search service.

![Architecture Diagram](https://i.imgur.com/6Zb5QwA.png)

---

## Package Structure & Module Definitions
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
âââ util
âââ integration
```
- **config**: Configuration classes (Security, Mail, etc.)
- **controller**: REST controllers
- **dto**: Data Transfer Objects
- **entity**: JPA entities
- **exception**: Custom exceptions & handlers
- **repository**: Spring Data JPA repositories
- **security**: Security filters, providers, utils
- **service**: Business logic
- **util**: Utility classes
- **integration**: External service clients (Email, MFA, Search, Notification)

---

## Entity Design & Domain Models
### User
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    private String firstName;
    private String lastName;
    private String profilePictureUrl;
    private boolean emailVerified;
    private boolean mfaEnabled;
    @OneToMany(mappedBy = "user")
    private List<Notification> notifications;
    @OneToMany(mappedBy = "user")
    private List<AccountActivity> activities;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserPreferences preferences;
    // getters and setters
}
```

### Notification
```java
@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
    // getters and setters
}
```

### AccountActivity
```java
@Entity
public class AccountActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;
    private String activityType;
    private String description;
    private LocalDateTime timestamp;
    // getters and setters
}
```

### UserPreferences
```java
@Entity
public class UserPreferences {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    private User user;
    private String dashboardLayout;
    private boolean emailNotifications;
    private boolean pushNotifications;
    // getters and setters
}
```

---

## Service, Repository, and Controller Specifications
### User Service
```java
public interface UserService {
    User register(UserRegistrationDto dto);
    void verifyEmail(String token);
    User getUserProfile(Long userId);
    User updateProfile(Long userId, UserProfileUpdateDto dto);
    void changePassword(Long userId, ChangePasswordDto dto);
    void uploadProfilePicture(Long userId, MultipartFile file);
    List<AccountActivity> getAccountActivities(Long userId);
}
```

### User Repository
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

### User Controller
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserRegistrationDto dto) {...}
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(Authentication auth) {...}
    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateProfile(@RequestBody @Valid UserProfileUpdateDto dto, Authentication auth) {...}
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordDto dto, Authentication auth) {...}
    @PostMapping("/profile-picture")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam MultipartFile file, Authentication auth) {...}
    @GetMapping("/activities")
    public ResponseEntity<List<AccountActivityDto>> getActivities(Authentication auth) {...}
}
```

### Notification Service
```java
public interface NotificationService {
    List<Notification> getUserNotifications(Long userId);
    void markAsRead(Long notificationId);
    void updatePreferences(Long userId, NotificationPreferencesDto dto);
}
```

### Notification Repository
```java
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
}
```

### Notification Controller
```java
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(Authentication auth) {...}
    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Authentication auth) {...}
    @PutMapping("/preferences")
    public ResponseEntity<?> updatePreferences(@RequestBody NotificationPreferencesDto dto, Authentication auth) {...}
}
```

### Authentication Service
```java
public interface AuthService {
    String login(LoginDto dto);
    void logout(String token);
    void requestPasswordReset(String email);
    void resetPassword(String token, String newPassword);
    void setupMfa(Long userId, MfaSetupDto dto);
}
```

### Auth Controller
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginDto dto) {...}
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {...}
    @PostMapping("/password-reset-request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequestDto dto) {...}
    @PostMapping("/password-reset")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetDto dto) {...}
    @PostMapping("/mfa/setup")
    public ResponseEntity<?> setupMfa(@RequestBody MfaSetupDto dto, Authentication auth) {...}
}
```

### Dashboard Service
```java
public interface DashboardService {
    DashboardOverviewDto getOverview(Long userId);
    void updateLayout(Long userId, DashboardLayoutDto dto);
}
```

### Dashboard Controller
```java
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewDto> getOverview(Authentication auth) {...}
    @PutMapping("/layout")
    public ResponseEntity<?> updateLayout(@RequestBody DashboardLayoutDto dto, Authentication auth) {...}
}
```

### Search Service
```java
public interface SearchService {
    SearchResultDto search(String query, Long userId);
}
```

### Search Controller
```java
@RestController
@RequestMapping("/api/search")
public class SearchController {
    @GetMapping
    public ResponseEntity<SearchResultDto> search(@RequestParam String q, Authentication auth) {...}
}
```

---

## Configuration & Security Settings
### Security Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers("/api/auth/**", "/api/users/register", "/api/users/verify-email").permitAll()
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### Email Configuration
```java
@Configuration
public class MailConfig {
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.example.com");
        mailSender.setPort(587);
        mailSender.setUsername("user");
        mailSender.setPassword("password");
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return mailSender;
    }
}
```

### File Storage Configuration
```java
@Configuration
public class FileStorageConfig {
    @Value("${app.file.upload-dir}")
    private String uploadDir;
    @Bean
    public FileStorageService fileStorageService() {
        return new LocalFileStorageService(uploadDir);
    }
}
```

### Session Timeout
```yaml
# application.yaml
server:
  servlet:
    session:
      timeout: 30m
```

---

## Integration Points
- **Email Service**: JavaMailSender for registration, password reset, notifications
- **MFA Service**: REST client for OTP/TOTP (e.g., Google Authenticator integration)
- **File Storage**: Local or cloud (AWS S3, Azure Blob) for profile pictures
- **Notification Service**: Internal or external push/email notification
- **Search Service**: REST or Elasticsearch integration

---

## User Story Breakdown & Design Patterns
### USER STORY 1: User Registration - Basic Account Creation
- **Flow**: User submits registration form â Validate input â Create User entity (emailVerified=false) â Send verification email
- **Patterns**: DTO, Service, Repository, Event-driven (for email)
- **Snippet**:
```java
public User register(UserRegistrationDto dto) {
    if (userRepository.existsByEmail(dto.getEmail())) {
        throw new RegistrationException("Email already in use");
    }
    User user = new User();
    user.setEmail(dto.getEmail());
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    user.setEmailVerified(false);
    userRepository.save(user);
    eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user));
    return user;
}
```

### USER STORY 2: User Registration - Email Verification
- **Flow**: User clicks verification link â Token validated â emailVerified=true
- **Patterns**: Token-based verification, Event Listener
- **Snippet**:
```java
public void verifyEmail(String token) {
    VerificationToken verificationToken = tokenRepository.findByToken(token)
        .orElseThrow(() -> new TokenNotFoundException());
    User user = verificationToken.getUser();
    user.setEmailVerified(true);
    userRepository.save(user);
    tokenRepository.delete(verificationToken);
}
```

### USER STORY 3: User Registration - Error Handling
- **Flow**: Validation errors â Custom exception â GlobalExceptionHandler returns clear messages
- **Patterns**: Exception Handling, Validation
- **Snippet**:
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
}
```

### USER STORY 4: User Profile - View Profile
- **Flow**: Authenticated user requests profile â Service fetches User entity â DTO returned
- **Patterns**: DTO, Service

### USER STORY 5: User Profile - Edit Profile Information
- **Flow**: Authenticated user submits update â Validate â Update User entity
- **Patterns**: DTO, Service, Validation

### USER STORY 6: User Profile - Change Password
- **Flow**: Authenticated user submits old/new password â Validate â Update password
- **Patterns**: Service, PasswordEncoder
- **Snippet**:
```java
public void changePassword(Long userId, ChangePasswordDto dto) {
    User user = userRepository.findById(userId).orElseThrow();
    if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
        throw new InvalidPasswordException();
    }
    user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    userRepository.save(user);
}
```

### USER STORY 7: User Profile - Profile Picture Upload
- **Flow**: User uploads image â FileStorageService saves file â User.profilePictureUrl updated
- **Patterns**: Service, File Storage Abstraction

### USER STORY 8: User Profile - View Account Activity
- **Flow**: Fetch AccountActivity entities for user
- **Patterns**: Repository, DTO

### USER STORY 9: User Authentication - Login
- **Flow**: User submits credentials â Validate â JWT issued
- **Patterns**: JWT, AuthenticationProvider
- **Snippet**:
```java
public String login(LoginDto dto) {
    Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
    User user = (User) auth.getPrincipal();
    return jwtTokenProvider.createToken(user.getEmail(), user.getRoles());
}
```

### USER STORY 10: User Authentication - Logout
- **Flow**: Invalidate JWT (client-side), optionally blacklist token
- **Patterns**: Stateless JWT, Token Blacklist (optional)

### USER STORY 11: User Authentication - Password Reset Request
- **Flow**: User submits email â Generate token â Send reset email
- **Patterns**: Token, Email, Event-driven

### USER STORY 12: User Authentication - Password Reset Completion
- **Flow**: User submits new password with token â Validate â Update password
- **Patterns**: Token, Service

### USER STORY 13: User Authentication - Multi-Factor Authentication Setup
- **Flow**: User enables MFA â Generate secret/QR â Validate OTP
- **Patterns**: External MFA integration, Service

### USER STORY 14: User Authentication - Session Timeout
- **Flow**: Session expires after inactivity (configured in application.yaml)
- **Patterns**: Session Management

### USER STORY 15: User Dashboard - View Dashboard Overview
- **Flow**: Fetch dashboard data for user
- **Patterns**: Service, DTO

### USER STORY 16: User Dashboard - Customize Dashboard Layout
- **Flow**: User submits layout preferences â Update UserPreferences
- **Patterns**: Service, DTO

### USER STORY 17: Notifications - View Notifications
- **Flow**: Fetch Notification entities for user
- **Patterns**: Repository, DTO

### USER STORY 18: Notifications - Mark as Read
- **Flow**: Update Notification.read=true
- **Patterns**: Service

### USER STORY 19: Notifications - Notification Preferences
- **Flow**: Update UserPreferences for notifications
- **Patterns**: Service, DTO

### USER STORY 20: Search - Basic Search Functionality
- **Flow**: User submits query â SearchService queries DB or external search â Return results
- **Patterns**: Service, Integration
- **Snippet**:
```java
public SearchResultDto search(String query, Long userId) {
    // Example: call Elasticsearch or DB
    return searchClient.search(query, userId);
}
```

---

## Conclusion
This document provides a detailed, production-ready technical design for all 20 user stories, following Spring Boot best practices. All layers, entities, services, controllers, configuration, and integration points are specified for easy developer consumption and implementation.
