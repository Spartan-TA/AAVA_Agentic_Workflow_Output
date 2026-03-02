# Low-Level Technical Design Document: User Management Platform (Spring Boot)

## Table of Contents
1. [Spring Boot Architecture Overview](#architecture-overview)
2. [Package Structure](#package-structure)
3. [Entity Design & Domain Models](#entity-design)
4. [Service Layer Specifications](#service-layer)
5. [Repository Layer Specifications](#repository-layer)
6. [Controller Specifications](#controller-specs)
7. [Configuration & Security Settings](#config-security)
8. [Integration Points](#integration)
9. [User Story Design Details](#user-story-details)

---

## <a name="architecture-overview"></a>1. Spring Boot Architecture Overview
- **Layered Architecture:**
  - Controller (REST API)
  - Service (Business Logic)
  - Repository (Data Access)
  - Domain (Entities/DTOs)
  - Security (JWT, Spring Security, MFA)
  - Configuration (App, Security, Email)
- **Persistence:** JPA/Hibernate, MySQL/PostgreSQL
- **Security:** Spring Security, JWT, BCrypt, MFA (TOTP)
- **Email:** Spring Mail
- **Testing:** JUnit, Mockito

---

## <a name="package-structure"></a>2. Package Structure
```
com.example.usermanagement
âââ config
âââ controller
âââ dto
âââ entity
âââ exception
âââ repository
âââ security
âââ service
âââ util
âââ Application.java
```

---

## <a name="entity-design"></a>3. Entity Design & Domain Models
### 3.1 User
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
    private String fullName;
    private boolean enabled;
    private boolean emailVerified;
    private boolean mfaEnabled;
    private String mfaSecret;
    private boolean accountNonLocked = true;
    private int failedLoginAttempts = 0;
    private LocalDateTime lockTime;
    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean deactivated = false;
    // Getters/Setters
}
```

### 3.2 Role (Enum)
```java
public enum Role {
    USER, ADMIN
}
```

### 3.3 VerificationToken
```java
@Entity
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    @OneToOne(fetch = FetchType.EAGER)
    private User user;
    private LocalDateTime expiryDate;
    private TokenType type; // EMAIL_VERIFICATION, PASSWORD_RESET
    // Getters/Setters
}
```

### 3.4 TokenType (Enum)
```java
public enum TokenType {
    EMAIL_VERIFICATION, PASSWORD_RESET
}
```

---

## <a name="service-layer"></a>4. Service Layer Specifications
- **UserService**: Registration, login, profile, dashboard, MFA, etc.
- **AdminService**: User list, deactivate/reactivate.
- **VerificationTokenService**: Token generation/validation.
- **EmailService**: Send emails (verification, reset).
- **MfaService**: TOTP secret generation/validation.

**Example: UserService Interface**
```java
public interface UserService {
    User register(UserRegistrationDto dto);
    User login(UserLoginDto dto);
    void resetPassword(String email);
    void updateProfile(Long userId, UserProfileDto dto);
    void changeEmail(Long userId, String newEmail);
    void enableMfa(Long userId);
    void verifyMfa(Long userId, String code);
    // ...
}
```

---

## <a name="repository-layer"></a>5. Repository Layer Specifications
- **UserRepository**: `findByEmail`, `findAll(Pageable)`, `findById`, etc.
- **VerificationTokenRepository**: `findByToken`, `deleteByUser`, etc.

**Example:**
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Page<User> findAll(Pageable pageable);
}
```

---

## <a name="controller-specs"></a>6. Controller Specifications
- **AuthController**: `/register`, `/login`, `/logout`, `/verify-email`, `/reset-password`, `/mfa` endpoints.
- **UserController**: `/profile`, `/dashboard` endpoints.
- **AdminController**: `/admin/users`, `/admin/users/{id}/deactivate`, `/admin/users/{id}/activate` endpoints.

**Example:**
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDto dto) { ... }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto dto) { ... }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) { ... }
    // ...
}
```

---

## <a name="config-security"></a>7. Configuration & Security Settings
- **Password Encoding:** BCryptPasswordEncoder
- **JWT Authentication:** Stateless session, token in Authorization header
- **Account Locking:** After 5 failed attempts, lock for X minutes
- **Email Verification:** Token expires in 48h
- **Password Reset:** Token expires in 24h
- **MFA:** TOTP (Google Authenticator compatible)
- **Role-based Access:** `@PreAuthorize`/`@Secured`

**Example: SecurityConfig**
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .addFilter(new JwtAuthenticationFilter(authenticationManager()));
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## <a name="integration"></a>8. Integration Points
- **Email Service:** Spring Mail for sending verification and reset emails
- **MFA:** TOTP secret generation, QR code for authenticator apps
- **JWT:** Token generation/validation for stateless auth

---

## <a name="user-story-details"></a>9. User Story Design Details

### **User Story 1: User Registration - Email and Password**
- **Endpoint:** `POST /api/auth/register`
- **DTO:** `UserRegistrationDto { email, password, fullName }`
- **Validation:**
  - Email format, uniqueness
  - Password: min 8 chars, 1 uppercase, 1 number (regex)
- **Flow:**
  1. Validate input
  2. Hash password (BCrypt)
  3. Create user (enabled=false, emailVerified=false)
  4. Generate email verification token (48h expiry)
  5. Send verification email
- **Entity:** `User`, `VerificationToken`
- **Service:** `UserService.register()`
- **Security:** Open endpoint
- **Code Snippet:**
```java
if (!password.matches("^(?=.*[A-Z])(?=.*\d).{8,}$")) throw new ValidationException();
User user = new User(email, passwordEncoder.encode(password), fullName, false, false, ...);
userRepository.save(user);
VerificationToken token = tokenService.createToken(user, TokenType.EMAIL_VERIFICATION, 48);
emailService.sendVerificationEmail(user, token.getToken());
```

---

### **User Story 2: User Login - Email and Password**
- **Endpoint:** `POST /api/auth/login`
- **DTO:** `UserLoginDto { email, password }`
- **Flow:**
  1. Find user by email
  2. Check if account is locked or deactivated
  3. Verify password
  4. If failed, increment failedLoginAttempts; lock after 5
  5. If MFA enabled, require TOTP code
  6. Generate JWT token
- **Entity:** `User`
- **Service:** `UserService.login()`
- **Security:** Open endpoint
- **Code Snippet:**
```java
User user = userRepository.findByEmail(email).orElseThrow(...);
if (!user.isAccountNonLocked()) throw new LockedException();
if (!passwordEncoder.matches(password, user.getPassword())) {
    user.incrementFailedLoginAttempts();
    if (user.getFailedLoginAttempts() >= 5) user.lock();
    userRepository.save(user);
    throw new BadCredentialsException();
}
user.resetFailedLoginAttempts();
userRepository.save(user);
if (user.isMfaEnabled()) { /* prompt for TOTP */ }
String jwt = jwtService.generateToken(user);
```

---

### **User Story 3: Password Reset**
- **Endpoints:**
  - `POST /api/auth/reset-password-request` (send email)
  - `POST /api/auth/reset-password` (set new password)
- **DTOs:** `PasswordResetRequestDto { email }`, `PasswordResetDto { token, newPassword }`
- **Flow:**
  1. Generate password reset token (24h expiry)
  2. Send email with reset link
  3. On reset, validate token, set new password (BCrypt)
- **Entity:** `VerificationToken`
- **Service:** `UserService.resetPassword()`, `VerificationTokenService`
- **Security:** Open endpoint
- **Code Snippet:**
```java
VerificationToken token = tokenService.createToken(user, TokenType.PASSWORD_RESET, 24);
emailService.sendPasswordResetEmail(user, token.getToken());
// On reset
if (!tokenService.isValid(token)) throw new TokenExpiredException();
user.setPassword(passwordEncoder.encode(newPassword));
userRepository.save(user);
tokenService.delete(token);
```

---

### **User Story 4: Profile Management**
- **Endpoint:** `PUT /api/user/profile`
- **DTO:** `UserProfileDto { fullName, email, ... }`
- **Flow:**
  1. Authenticated user
  2. Update profile fields
  3. If email changed, set emailVerified=false, send new verification email
- **Entity:** `User`
- **Service:** `UserService.updateProfile()`, `UserService.changeEmail()`
- **Security:** Authenticated
- **Code Snippet:**
```java
User user = getCurrentUser();
user.setFullName(dto.getFullName());
if (!user.getEmail().equals(dto.getEmail())) {
    user.setEmail(dto.getEmail());
    user.setEmailVerified(false);
    VerificationToken token = tokenService.createToken(user, TokenType.EMAIL_VERIFICATION, 48);
    emailService.sendVerificationEmail(user, token.getToken());
}
userRepository.save(user);
```

---

### **User Story 5: View Dashboard**
- **Endpoint:** `GET /api/user/dashboard`
- **DTO:** `DashboardDto { ... }`
- **Flow:**
  1. Authenticated user
  2. Fetch personalized data (future extensible)
- **Entity:** `User`
- **Service:** `UserService.getDashboard()`
- **Security:** Authenticated
- **Code Snippet:**
```java
User user = getCurrentUser();
DashboardDto dto = dashboardService.getDashboardForUser(user);
return ResponseEntity.ok(dto);
```

---

### **User Story 6: Logout**
- **Endpoint:** `POST /api/auth/logout`
- **Flow:**
  1. Invalidate JWT token (add to blacklist or rely on short expiry)
- **Service:** `UserService.logout()`
- **Security:** Authenticated
- **Code Snippet:**
```java
String token = extractJwtFromRequest(request);
jwtService.invalidateToken(token); // if using blacklist
```

---

### **User Story 7: Admin - View User List**
- **Endpoint:** `GET /api/admin/users?page=0&size=20`
- **DTO:** `UserListDto { List<UserSummaryDto>, page, size, total }`
- **Flow:**
  1. Admin only
  2. Paginated user list
- **Entity:** `User`
- **Service:** `AdminService.getUserList(Pageable)`
- **Security:** `@PreAuthorize("hasRole('ADMIN')")`
- **Code Snippet:**
```java
Page<User> users = userRepository.findAll(pageable);
List<UserSummaryDto> dtos = users.map(UserSummaryDto::fromEntity);
return new UserListDto(dtos, ...);
```

---

### **User Story 8: Admin - Deactivate User Account**
- **Endpoints:**
  - `POST /api/admin/users/{id}/deactivate`
  - `POST /api/admin/users/{id}/activate`
- **Flow:**
  1. Admin only
  2. Set `deactivated=true` or `false`
- **Entity:** `User`
- **Service:** `AdminService.deactivateUser(id)`, `AdminService.activateUser(id)`
- **Security:** `@PreAuthorize("hasRole('ADMIN')")`
- **Code Snippet:**
```java
User user = userRepository.findById(id).orElseThrow(...);
user.setDeactivated(true); // or false
userRepository.save(user);
```

---

### **User Story 9: Email Verification**
- **Endpoint:** `GET /api/auth/verify-email?token=...`
- **Flow:**
  1. Validate token (48h expiry)
  2. Set `emailVerified=true`, `enabled=true`
  3. Delete token
- **Entity:** `VerificationToken`, `User`
- **Service:** `VerificationTokenService.verifyEmail(token)`
- **Security:** Open endpoint
- **Code Snippet:**
```java
VerificationToken token = tokenService.findByToken(tokenStr);
if (!tokenService.isValid(token)) throw new TokenExpiredException();
User user = token.getUser();
user.setEmailVerified(true);
user.setEnabled(true);
userRepository.save(user);
tokenService.delete(token);
```

---

### **User Story 10: Multi-Factor Authentication (MFA)**
- **Endpoints:**
  - `POST /api/user/mfa/enable`
  - `POST /api/user/mfa/verify`
- **Flow:**
  1. Generate TOTP secret, show QR code
  2. User scans with authenticator app
  3. User submits TOTP code for verification
  4. On success, set `mfaEnabled=true`, store secret
- **Entity:** `User`
- **Service:** `MfaService.generateSecret()`, `MfaService.verifyCode()`
- **Security:** Authenticated
- **Code Snippet:**
```java
String secret = mfaService.generateSecret();
String qr = mfaService.getQrCode(secret, user.getEmail());
// On verify
if (!mfaService.verifyCode(secret, code)) throw new InvalidMfaCodeException();
user.setMfaEnabled(true);
user.setMfaSecret(secret);
userRepository.save(user);
```

---

## Notes
- All DTOs should use validation annotations (`@Email`, `@NotBlank`, etc.)
- Exception handling via `@ControllerAdvice`
- Logging and auditing for admin actions
- Unit and integration tests for all endpoints

---

**End of Document**
