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

**Section:** User Registration

**Description:** This user story implements the registration functionality allowing new users to create accounts using email and password. The implementation follows Spring Boot best practices with proper validation, security measures, and email verification workflow.

**Design Specification:**
- **Endpoint:** `POST /api/auth/register`
- **Request DTO:** `UserRegistrationDto { email, password, fullName }`
- **Validation Rules:**
  - Email format validation using `@Email` annotation
  - Email uniqueness check in database
  - Password complexity: minimum 8 characters, at least 1 uppercase letter, at least 1 number
  - Regex pattern: `^(?=.*[A-Z])(?=.*\d).{8,}$`
- **Business Logic Flow:**
  1. Validate input data
  2. Hash password using BCrypt
  3. Create user entity with enabled=false, emailVerified=false
  4. Generate email verification token with 48-hour expiry
  5. Send verification email
  6. Return success response
- **Entities Involved:** `User`, `VerificationToken`
- **Services:** `UserService.register()`, `VerificationTokenService`, `EmailService`
- **Security:** Open endpoint (no authentication required)

**Sample Implementation:**
```java
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private VerificationTokenService tokenService;
    @Autowired
    private EmailService emailService;
    
    @Override
    @Transactional
    public User register(UserRegistrationDto dto) {
        // Validate password complexity
        if (!dto.getPassword().matches("^(?=.*[A-Z])(?=.*\d).{8,}$")) {
            throw new ValidationException("Password must be at least 8 characters with 1 uppercase and 1 number");
        }
        
        // Check email uniqueness
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email already registered");
        }
        
        // Create user entity
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setEnabled(false);
        user.setEmailVerified(false);
        user.setRole(Role.USER);
        user.setAccountNonLocked(true);
        user.setFailedLoginAttempts(0);
        
        user = userRepository.save(user);
        
        // Generate verification token
        VerificationToken token = tokenService.createToken(user, TokenType.EMAIL_VERIFICATION, 48);
        
        // Send verification email
        emailService.sendVerificationEmail(user, token.getToken());
        
        return user;
    }
}
```

---

### **User Story 2: User Login - Email and Password**

**Section:** User Authentication

**Description:** This user story implements secure login functionality with account locking mechanism after failed attempts and JWT token generation for stateless authentication.

**Design Specification:**
- **Endpoint:** `POST /api/auth/login`
- **Request DTO:** `UserLoginDto { email, password }`
- **Business Logic Flow:**
  1. Find user by email
  2. Check if account is locked or deactivated
  3. Verify password using BCrypt
  4. If password incorrect, increment failedLoginAttempts
  5. Lock account after 5 failed attempts
  6. If MFA enabled, require TOTP code verification
  7. Generate JWT token
  8. Reset failed login attempts on success
- **Security Features:**
  - Account locking after 5 failed attempts
  - BCrypt password verification
  - JWT token generation
  - MFA support
- **Entities Involved:** `User`
- **Services:** `UserService.login()`, `JwtService`, `MfaService`
- **Security:** Open endpoint (no authentication required)
- **Dependencies:** User Registration (User Story 1)

**Sample Implementation:**
```java
@Override
@Transactional
public LoginResponseDto login(UserLoginDto dto) {
    // Find user
    User user = userRepository.findByEmail(dto.getEmail())
        .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    
    // Check if account is locked
    if (!user.isAccountNonLocked()) {
        if (user.getLockTime() != null && 
            LocalDateTime.now().isAfter(user.getLockTime().plusMinutes(30))) {
            // Unlock after 30 minutes
            user.setAccountNonLocked(true);
            user.setFailedLoginAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);
        } else {
            throw new AccountLockedException("Account is locked due to multiple failed login attempts");
        }
    }
    
    // Check if account is deactivated
    if (user.isDeactivated()) {
        throw new AccountDeactivatedException("Account has been deactivated");
    }
    
    // Verify password
    if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        if (user.getFailedLoginAttempts() >= 5) {
            user.setAccountNonLocked(false);
            user.setLockTime(LocalDateTime.now());
        }
        userRepository.save(user);
        throw new BadCredentialsException("Invalid credentials");
    }
    
    // Reset failed attempts on successful login
    user.setFailedLoginAttempts(0);
    userRepository.save(user);
    
    // Check MFA
    if (user.isMfaEnabled()) {
        return new LoginResponseDto(null, true, "MFA_REQUIRED");
    }
    
    // Generate JWT token
    String jwt = jwtService.generateToken(user);
    return new LoginResponseDto(jwt, false, "SUCCESS");
}
```

---

### **User Story 3: Password Reset**

**Section:** Password Recovery

**Description:** This user story implements password reset functionality allowing users to recover their accounts via email verification with time-limited tokens.

**Design Specification:**
- **Endpoints:**
  - `POST /api/auth/reset-password-request` - Request password reset
  - `POST /api/auth/reset-password` - Set new password
- **Request DTOs:**
  - `PasswordResetRequestDto { email }`
  - `PasswordResetDto { token, newPassword }`
- **Business Logic Flow:**
  1. User requests password reset with email
  2. Generate password reset token with 24-hour expiry
  3. Send email with reset link
  4. User clicks link and submits new password
  5. Validate token (not expired, valid)
  6. Hash and set new password
  7. Delete used token
- **Security Features:**
  - Token expires in 24 hours
  - One-time use tokens
  - Password complexity validation
- **Entities Involved:** `User`, `VerificationToken`
- **Services:** `UserService.resetPassword()`, `VerificationTokenService`, `EmailService`
- **Security:** Open endpoint
- **Dependencies:** User Registration (User Story 1)

**Sample Implementation:**
```java
@Override
@Transactional
public void requestPasswordReset(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
    
    // Delete any existing password reset tokens
    tokenService.deleteByUserAndType(user, TokenType.PASSWORD_RESET);
    
    // Generate new token with 24-hour expiry
    VerificationToken token = tokenService.createToken(user, TokenType.PASSWORD_RESET, 24);
    
    // Send password reset email
    emailService.sendPasswordResetEmail(user, token.getToken());
}

@Override
@Transactional
public void resetPassword(PasswordResetDto dto) {
    // Find and validate token
    VerificationToken token = tokenService.findByToken(dto.getToken())
        .orElseThrow(() -> new InvalidTokenException("Invalid or expired token"));
    
    if (!tokenService.isValid(token)) {
        throw new TokenExpiredException("Password reset token has expired");
    }
    
    // Validate new password
    if (!dto.getNewPassword().matches("^(?=.*[A-Z])(?=.*\d).{8,}$")) {
        throw new ValidationException("Password must be at least 8 characters with 1 uppercase and 1 number");
    }
    
    // Update password
    User user = token.getUser();
    user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    userRepository.save(user);
    
    // Delete used token
    tokenService.delete(token);
}
```

---

### **User Story 4: Profile Management**

**Section:** User Profile

**Description:** This user story implements profile management functionality allowing authenticated users to update their personal information with email re-verification when email is changed.

**Design Specification:**
- **Endpoint:** `PUT /api/user/profile`
- **Request DTO:** `UserProfileDto { fullName, email, ... }`
- **Business Logic Flow:**
  1. Authenticate user
  2. Update profile fields
  3. If email changed:
     - Set emailVerified=false
     - Generate new verification token
     - Send verification email
  4. Save changes
- **Security Features:**
  - Requires authentication
  - Email re-verification on change
- **Entities Involved:** `User`, `VerificationToken`
- **Services:** `UserService.updateProfile()`, `UserService.changeEmail()`, `VerificationTokenService`
- **Security:** Authenticated users only
- **Dependencies:** User Login (User Story 2)

**Sample Implementation:**
```java
@Override
@Transactional
public User updateProfile(Long userId, UserProfileDto dto) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
    
    // Update basic profile information
    user.setFullName(dto.getFullName());
    
    // Handle email change
    if (!user.getEmail().equals(dto.getEmail())) {
        // Check if new email is already in use
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email already in use");
        }
        
        // Update email and mark as unverified
        user.setEmail(dto.getEmail());
        user.setEmailVerified(false);
        
        // Generate new verification token
        tokenService.deleteByUserAndType(user, TokenType.EMAIL_VERIFICATION);
        VerificationToken token = tokenService.createToken(user, TokenType.EMAIL_VERIFICATION, 48);
        
        // Send verification email to new address
        emailService.sendVerificationEmail(user, token.getToken());
    }
    
    return userRepository.save(user);
}
```

---

### **User Story 5: View Dashboard**

**Section:** User Dashboard

**Description:** This user story implements a personalized dashboard view for authenticated users displaying relevant information and available actions.

**Design Specification:**
- **Endpoint:** `GET /api/user/dashboard`
- **Response DTO:** `DashboardDto { userInfo, recentActivity, notifications, quickActions }`
- **Business Logic Flow:**
  1. Authenticate user
  2. Fetch personalized dashboard data
  3. Return dashboard information
- **Dashboard Components:**
  - User profile summary
  - Recent activity
  - Notifications
  - Quick action links
  - Account status indicators
- **Entities Involved:** `User`
- **Services:** `UserService.getDashboard()`, `DashboardService`
- **Security:** Authenticated users only
- **Dependencies:** User Login (User Story 2)
- **Future Enhancements:** Customizable dashboard widgets

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private DashboardService dashboardService;
    
    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardDto> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        DashboardDto dashboard = dashboardService.getDashboardForUser(user);
        return ResponseEntity.ok(dashboard);
    }
}

@Service
public class DashboardServiceImpl implements DashboardService {
    @Override
    public DashboardDto getDashboardForUser(User user) {
        DashboardDto dto = new DashboardDto();
        
        // User info
        dto.setUserInfo(new UserInfoDto(
            user.getFullName(),
            user.getEmail(),
            user.isEmailVerified(),
            user.isMfaEnabled()
        ));
        
        // Account status
        dto.setAccountStatus(new AccountStatusDto(
            user.isEnabled(),
            user.isAccountNonLocked(),
            !user.isDeactivated()
        ));
        
        // Quick actions
        dto.setQuickActions(Arrays.asList(
            new QuickActionDto("Update Profile", "/profile"),
            new QuickActionDto("Change Password", "/change-password"),
            new QuickActionDto("Security Settings", "/security")
        ));
        
        return dto;
    }
}
```

---

### **User Story 6: Logout**

**Section:** Session Management

**Description:** This user story implements secure logout functionality that invalidates the user's session and JWT token.

**Design Specification:**
- **Endpoint:** `POST /api/auth/logout`
- **Business Logic Flow:**
  1. Extract JWT token from request
  2. Add token to blacklist (or rely on short expiry)
  3. Clear any server-side session data
  4. Return success response
- **Security Features:**
  - Token invalidation
  - Session cleanup
- **Services:** `UserService.logout()`, `JwtService`
- **Security:** Authenticated users only
- **Dependencies:** User Login (User Story 2)

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private JwtService jwtService;
    
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // Extract JWT token from Authorization header
        String token = extractJwtFromRequest(request);
        
        if (token != null) {
            // Invalidate token (add to blacklist)
            jwtService.invalidateToken(token);
        }
        
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }
    
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

@Service
public class JwtServiceImpl implements JwtService {
    @Autowired
    private TokenBlacklistRepository blacklistRepository;
    
    @Override
    public void invalidateToken(String token) {
        // Add token to blacklist
        TokenBlacklist blacklistedToken = new TokenBlacklist();
        blacklistedToken.setToken(token);
        blacklistedToken.setBlacklistedAt(LocalDateTime.now());
        blacklistedToken.setExpiresAt(extractExpiration(token));
        blacklistRepository.save(blacklistedToken);
    }
    
    @Override
    public boolean isTokenBlacklisted(String token) {
        return blacklistRepository.existsByToken(token);
    }
}
```

---

### **User Story 7: Admin - View User List**

**Section:** Admin User Management

**Description:** This user story implements admin functionality to view paginated list of all registered users for management purposes.

**Design Specification:**
- **Endpoint:** `GET /api/admin/users?page=0&size=20&sort=email,asc`
- **Response DTO:** `UserListDto { List<UserSummaryDto>, page, size, totalElements, totalPages }`
- **UserSummaryDto Fields:**
  - id
  - email
  - fullName
  - role
  - enabled
  - emailVerified
  - accountNonLocked
  - deactivated
  - createdAt
- **Business Logic Flow:**
  1. Verify admin role
  2. Fetch paginated user list
  3. Map to DTOs
  4. Return paginated response
- **Features:**
  - Pagination support
  - Sorting support
  - Filtering capabilities (future)
- **Entities Involved:** `User`
- **Services:** `AdminService.getUserList(Pageable)`
- **Security:** Admin role required (`@PreAuthorize("hasRole('ADMIN')")`)
- **Dependencies:** User Registration (User Story 1), Admin Role

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private AdminService adminService;
    
    @GetMapping("/users")
    public ResponseEntity<Page<UserSummaryDto>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "email,asc") String[] sort) {
        
        // Parse sort parameters
        Sort.Direction direction = sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        
        Page<UserSummaryDto> users = adminService.getUserList(pageable);
        return ResponseEntity.ok(users);
    }
}

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public Page<UserSummaryDto> getUserList(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::mapToUserSummaryDto);
    }
    
    private UserSummaryDto mapToUserSummaryDto(User user) {
        UserSummaryDto dto = new UserSummaryDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setEnabled(user.isEnabled());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setAccountNonLocked(user.isAccountNonLocked());
        dto.setDeactivated(user.isDeactivated());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
```

---

### **User Story 8: Admin - Deactivate User Account**

**Section:** Admin Account Management

**Description:** This user story implements admin functionality to deactivate and reactivate user accounts for policy enforcement.

**Design Specification:**
- **Endpoints:**
  - `POST /api/admin/users/{id}/deactivate` - Deactivate user account
  - `POST /api/admin/users/{id}/activate` - Reactivate user account
- **Request Body:** `AccountActionDto { reason }`
- **Business Logic Flow:**
  1. Verify admin role
  2. Find user by ID
  3. Set deactivated flag
  4. Log action with reason
  5. Optionally notify user
  6. Return success response
- **Features:**
  - Reversible deactivation
  - Audit logging
  - Reason tracking
- **Entities Involved:** `User`, `AdminAction` (audit log)
- **Services:** `AdminService.deactivateUser(id, reason)`, `AdminService.activateUser(id, reason)`
- **Security:** Admin role required
- **Dependencies:** Admin - View User List (User Story 7)

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private AdminService adminService;
    
    @PostMapping("/users/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(
            @PathVariable Long id,
            @RequestBody AccountActionDto dto,
            @AuthenticationPrincipal UserDetails adminUser) {
        
        adminService.deactivateUser(id, dto.getReason(), ((User) adminUser).getId());
        return ResponseEntity.ok(new MessageResponse("User account deactivated successfully"));
    }
    
    @PostMapping("/users/{id}/activate")
    public ResponseEntity<?> activateUser(
            @PathVariable Long id,
            @RequestBody AccountActionDto dto,
            @AuthenticationPrincipal UserDetails adminUser) {
        
        adminService.activateUser(id, dto.getReason(), ((User) adminUser).getId());
        return ResponseEntity.ok(new MessageResponse("User account activated successfully"));
    }
}

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AdminActionRepository adminActionRepository;
    @Autowired
    private EmailService emailService;
    
    @Override
    @Transactional
    public void deactivateUser(Long userId, String reason, Long adminId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        if (user.isDeactivated()) {
            throw new IllegalStateException("User account is already deactivated");
        }
        
        // Deactivate user
        user.setDeactivated(true);
        userRepository.save(user);
        
        // Log admin action
        AdminAction action = new AdminAction();
        action.setAdminId(adminId);
        action.setTargetUserId(userId);
        action.setAction("DEACTIVATE_USER");
        action.setReason(reason);
        action.setTimestamp(LocalDateTime.now());
        adminActionRepository.save(action);
        
        // Notify user
        emailService.sendAccountDeactivationEmail(user, reason);
    }
    
    @Override
    @Transactional
    public void activateUser(Long userId, String reason, Long adminId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        if (!user.isDeactivated()) {
            throw new IllegalStateException("User account is not deactivated");
        }
        
        // Activate user
        user.setDeactivated(false);
        userRepository.save(user);
        
        // Log admin action
        AdminAction action = new AdminAction();
        action.setAdminId(adminId);
        action.setTargetUserId(userId);
        action.setAction("ACTIVATE_USER");
        action.setReason(reason);
        action.setTimestamp(LocalDateTime.now());
        adminActionRepository.save(action);
        
        // Notify user
        emailService.sendAccountActivationEmail(user);
    }
}
```

---

### **User Story 9: Email Verification**

**Section:** Email Verification

**Description:** This user story implements email verification functionality to confirm user email addresses after registration.

**Design Specification:**
- **Endpoint:** `GET /api/auth/verify-email?token={token}`
- **Business Logic Flow:**
  1. Extract token from query parameter
  2. Find verification token in database
  3. Validate token (not expired, correct type)
  4. Set user emailVerified=true and enabled=true
  5. Delete used token
  6. Return success response or redirect to login
- **Security Features:**
  - Token expires in 48 hours
  - One-time use tokens
  - Token type validation
- **Entities Involved:** `VerificationToken`, `User`
- **Services:** `VerificationTokenService.verifyEmail(token)`
- **Security:** Open endpoint
- **Dependencies:** User Registration (User Story 1)

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private VerificationTokenService tokenService;
    
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            tokenService.verifyEmail(token);
            return ResponseEntity.ok(new MessageResponse("Email verified successfully. You can now log in."));
        } catch (TokenExpiredException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Verification link has expired"));
        } catch (InvalidTokenException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid verification link"));
        }
    }
    
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody ResendVerificationDto dto) {
        tokenService.resendVerificationEmail(dto.getEmail());
        return ResponseEntity.ok(new MessageResponse("Verification email sent"));
    }
}

@Service
public class VerificationTokenServiceImpl implements VerificationTokenService {
    @Autowired
    private VerificationTokenRepository tokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    
    @Override
    @Transactional
    public void verifyEmail(String tokenStr) {
        // Find token
        VerificationToken token = tokenRepository.findByToken(tokenStr)
            .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));
        
        // Validate token type
        if (token.getType() != TokenType.EMAIL_VERIFICATION) {
            throw new InvalidTokenException("Invalid token type");
        }
        
        // Check expiration
        if (LocalDateTime.now().isAfter(token.getExpiryDate())) {
            throw new TokenExpiredException("Verification token has expired");
        }
        
        // Update user
        User user = token.getUser();
        user.setEmailVerified(true);
        user.setEnabled(true);
        userRepository.save(user);
        
        // Delete used token
        tokenRepository.delete(token);
    }
    
    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email is already verified");
        }
        
        // Delete old tokens
        deleteByUserAndType(user, TokenType.EMAIL_VERIFICATION);
        
        // Create new token
        VerificationToken token = createToken(user, TokenType.EMAIL_VERIFICATION, 48);
        
        // Send email
        emailService.sendVerificationEmail(user, token.getToken());
    }
    
    @Override
    public VerificationToken createToken(User user, TokenType type, int expiryHours) {
        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setType(type);
        token.setExpiryDate(LocalDateTime.now().plusHours(expiryHours));
        return tokenRepository.save(token);
    }
}
```

---

### **User Story 10: Multi-Factor Authentication (MFA)**

**Section:** Multi-Factor Authentication

**Description:** This user story implements TOTP-based multi-factor authentication for enhanced account security using authenticator apps.

**Design Specification:**
- **Endpoints:**
  - `POST /api/user/mfa/enable` - Enable MFA and get QR code
  - `POST /api/user/mfa/verify` - Verify TOTP code and activate MFA
  - `POST /api/user/mfa/disable` - Disable MFA
  - `POST /api/auth/mfa/validate` - Validate TOTP during login
- **Request/Response DTOs:**
  - `MfaEnableResponseDto { secret, qrCodeUrl, backupCodes }`
  - `MfaVerifyDto { code }`
  - `MfaValidateDto { email, password, code }`
- **Business Logic Flow:**
  1. User requests MFA enablement
  2. Generate TOTP secret
  3. Generate QR code for authenticator app
  4. Generate backup codes
  5. User scans QR code with authenticator app
  6. User submits TOTP code for verification
  7. Validate code
  8. Save secret and enable MFA
  9. On subsequent logins, require TOTP code
- **Security Features:**
  - TOTP algorithm (RFC 6238)
  - 30-second time window
  - Backup codes for recovery
  - QR code generation
- **Entities Involved:** `User`, `BackupCode`
- **Services:** `MfaService.generateSecret()`, `MfaService.verifyCode()`, `MfaService.generateQrCode()`
- **Security:** Authenticated users only
- **Dependencies:** User Login (User Story 2)
- **Third-party Libraries:** Google Authenticator compatible (e.g., java-totp)

**Sample Implementation:**
```java
@RestController
@RequestMapping("/api/user/mfa")
@PreAuthorize("isAuthenticated()")
public class MfaController {
    @Autowired
    private MfaService mfaService;
    
    @PostMapping("/enable")
    public ResponseEntity<MfaEnableResponseDto> enableMfa(@AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        
        if (user.isMfaEnabled()) {
            throw new IllegalStateException("MFA is already enabled");
        }
        
        // Generate secret and QR code
        String secret = mfaService.generateSecret();
        String qrCodeUrl = mfaService.generateQrCodeUrl(secret, user.getEmail());
        List<String> backupCodes = mfaService.generateBackupCodes(user);
        
        // Store secret temporarily (not yet enabled)
        user.setMfaSecret(secret);
        
        MfaEnableResponseDto response = new MfaEnableResponseDto();
        response.setSecret(secret);
        response.setQrCodeUrl(qrCodeUrl);
        response.setBackupCodes(backupCodes);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verify")
    public ResponseEntity<?> verifyMfa(
            @RequestBody MfaVerifyDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = (User) userDetails;
        
        // Verify TOTP code
        if (!mfaService.verifyCode(user.getMfaSecret(), dto.getCode())) {
            throw new InvalidMfaCodeException("Invalid verification code");
        }
        
        // Enable MFA
        user.setMfaEnabled(true);
        mfaService.saveUser(user);
        
        return ResponseEntity.ok(new MessageResponse("MFA enabled successfully"));
    }
    
    @PostMapping("/disable")
    public ResponseEntity<?> disableMfa(
            @RequestBody MfaVerifyDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = (User) userDetails;
        
        // Verify TOTP code before disabling
        if (!mfaService.verifyCode(user.getMfaSecret(), dto.getCode())) {
            throw new InvalidMfaCodeException("Invalid verification code");
        }
        
        // Disable MFA
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        mfaService.deleteBackupCodes(user);
        mfaService.saveUser(user);
        
        return ResponseEntity.ok(new MessageResponse("MFA disabled successfully"));
    }
}

@RestController
@RequestMapping("/api/auth/mfa")
public class MfaAuthController {
    @Autowired
    private MfaService mfaService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;
    
    @PostMapping("/validate")
    public ResponseEntity<?> validateMfa(@RequestBody MfaValidateDto dto) {
        // First validate credentials
        User user = userService.validateCredentials(dto.getEmail(), dto.getPassword());
        
        if (!user.isMfaEnabled()) {
            throw new IllegalStateException("MFA is not enabled for this account");
        }
        
        // Verify TOTP code or backup code
        boolean isValid = mfaService.verifyCode(user.getMfaSecret(), dto.getCode()) ||
                         mfaService.verifyBackupCode(user, dto.getCode());
        
        if (!isValid) {
            throw new InvalidMfaCodeException("Invalid MFA code");
        }
        
        // Generate JWT token
        String jwt = jwtService.generateToken(user);
        return ResponseEntity.ok(new LoginResponseDto(jwt, false, "SUCCESS"));
    }
}

@Service
public class MfaServiceImpl implements MfaService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BackupCodeRepository backupCodeRepository;
    
    @Override
    public String generateSecret() {
        // Generate random 32-character base32 secret
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return new Base32().encodeToString(bytes);
    }
    
    @Override
    public String generateQrCodeUrl(String secret, String email) {
        String issuer = "UserManagementApp";
        String format = "otpauth://totp/%s:%s?secret=%s&issuer=%s";
        return String.format(format, issuer, email, secret, issuer);
    }
    
    @Override
    public boolean verifyCode(String secret, String code) {
        try {
            long timeWindow = System.currentTimeMillis() / 1000 / 30;
            String expectedCode = generateTotpCode(secret, timeWindow);
            return code.equals(expectedCode);
        } catch (Exception e) {
            return false;
        }
    }
    
    private String generateTotpCode(String secret, long timeWindow) throws Exception {
        byte[] key = new Base32().decode(secret);
        byte[] data = ByteBuffer.allocate(8).putLong(timeWindow).array();
        
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, "HmacSHA1"));
        byte[] hash = mac.doFinal(data);
        
        int offset = hash[hash.length - 1] & 0xF;
        int binary = ((hash[offset] & 0x7F) << 24) |
                    ((hash[offset + 1] & 0xFF) << 16) |
                    ((hash[offset + 2] & 0xFF) << 8) |
                    (hash[offset + 3] & 0xFF);
        
        int otp = binary % 1000000;
        return String.format("%06d", otp);
    }
    
    @Override
    public List<String> generateBackupCodes(User user) {
        List<String> codes = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        
        for (int i = 0; i < 10; i++) {
            String code = String.format("%08d", random.nextInt(100000000));
            codes.add(code);
            
            BackupCode backupCode = new BackupCode();
            backupCode.setUser(user);
            backupCode.setCode(new BCryptPasswordEncoder().encode(code));
            backupCode.setUsed(false);
            backupCodeRepository.save(backupCode);
        }
        
        return codes;
    }
    
    @Override
    public boolean verifyBackupCode(User user, String code) {
        List<BackupCode> backupCodes = backupCodeRepository.findByUserAndUsedFalse(user);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        for (BackupCode backupCode : backupCodes) {
            if (encoder.matches(code, backupCode.getCode())) {
                backupCode.setUsed(true);
                backupCodeRepository.save(backupCode);
                return true;
            }
        }
        
        return false;
    }
}
```

---

## Additional Implementation Notes

### Exception Handling
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage()));
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("Invalid credentials"));
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ex.getMessage()));
    }
    
    // Additional exception handlers...
}
```

### Application Properties
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/usermanagement
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Application
app.name=User Management Platform
app.url=http://localhost:8080
```

### Testing Strategy
- **Unit Tests:** Service layer with Mockito
- **Integration Tests:** Controller layer with MockMvc
- **Security Tests:** Authentication and authorization
- **Repository Tests:** JPA queries

---

**Document Version:** 1.0  
**Last Updated:** 2024  
**Status:** Ready for Implementation

---

This comprehensive technical design document provides all necessary specifications for implementing the user management platform using Spring Boot best practices. Each user story includes detailed design specifications, sample implementations, and integration points to ensure successful development.