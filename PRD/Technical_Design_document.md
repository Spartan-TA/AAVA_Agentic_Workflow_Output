# Low-Level Technical Design Document

## User Story: Enable User Login

**Title:** Enable User Login

**Description:**
As a registered user, I want to log in using my credentials so that I can access my personalized dashboard.

**Acceptance Criteria:**
- Successful login redirects user to dashboard with a success notification.
- Secure session management is enforced.

---

Section: Overview
Description: This section outlines the Spring Boot architecture for implementing secure user login, leveraging Spring Security for authentication and session management. The design ensures modularity, maintainability, and adherence to industry standards.
Design Specification:
- Use Spring Boot 3.x with Spring Security 6.x
- Layered architecture: Controller, Service, Repository, Domain
- JWT-based authentication (stateless sessions)
- Exception handling and validation

---

Section: Package Structure & Module Definitions
Description: Defines the logical organization of code to promote separation of concerns and scalability.
Design Specification:
- `com.example.auth` (root package)
  - `controller` (REST endpoints)
  - `service` (business logic)
  - `repository` (data access)
  - `model` (domain entities & DTOs)
  - `config` (security and app configuration)
  - `exception` (custom exceptions)
Sample Implementation:
```
com.example.auth
âââ controller
â   âââ AuthController.java
âââ service
â   âââ AuthService.java
âââ repository
â   âââ UserRepository.java
âââ model
â   âââ User.java
â   âââ LoginRequest.java
â   âââ LoginResponse.java
âââ config
â   âââ SecurityConfig.java
âââ exception
    âââ AuthException.java
```

---

Section: Domain Model
Description: Represents the core entities and their relationships for authentication.
Design Specification:
- `User` entity with fields: id, username, password (hashed), roles, enabled
- DTOs: `LoginRequest`, `LoginResponse`
Sample Implementation:
```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String roles; // e.g., "ROLE_USER"
    private boolean enabled;
    // getters and setters
}

public class LoginRequest {
    private String username;
    private String password;
    // getters and setters
}

public class LoginResponse {
    private String token;
    private String message;
    // getters and setters
}
```

---

Section: Repository Layer
Description: Data access layer for user authentication.
Design Specification:
- `UserRepository` extends `JpaRepository<User, Long>`
- Custom query: `findByUsername(String username)`
Sample Implementation:
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

---

Section: Service Layer
Description: Handles authentication logic, user validation, and token generation.
Design Specification:
- `AuthService` with methods:
  - `authenticate(LoginRequest request)`: Validates credentials, returns JWT
  - `loadUserByUsername(String username)`: For Spring Security integration
Sample Implementation:
```java
@Service
public class AuthService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponse authenticate(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new AuthException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(user);
        return new LoginResponse(token, "Login successful");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(), user.getPassword(),
            AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRoles()));
    }
}
```

---

Section: Controller Design
Description: REST API endpoint for login, input validation, and response handling.
Design Specification:
- `POST /api/auth/login` endpoint
- Accepts `LoginRequest`, returns `LoginResponse`
- Handles authentication exceptions
Sample Implementation:
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<String> handleAuthException(AuthException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }
}
```

---

Section: Security & Configuration
Description: Secure the login endpoint, configure JWT, and enforce session management.
Design Specification:
- `SecurityConfig` to configure authentication manager, password encoder, JWT filter
- Stateless session management
- Permit `/api/auth/login`, secure other endpoints
Sample Implementation:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests()
                .requestMatchers("/api/auth/login").permitAll()
                .anyRequest().authenticated()
            .and()
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(authService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

Section: Integration Points
Description: Outlines integration with external systems and modules.
Design Specification:
- JWT utility for token generation/validation
- Optional: Integration with external identity providers (future scope)
Sample Implementation:
```java
@Component
public class JwtUtil {
    // Methods for generateToken(User user), validateToken(String token), extractUsername(String token)
}
```

---

Section: Exception Handling
Description: Custom exceptions for authentication errors.
Design Specification:
- `AuthException` extends `RuntimeException`
- Global exception handler in controller
Sample Implementation:
```java
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
```

---

Section: Summary
Description: This design ensures a secure, maintainable, and scalable login mechanism using Spring Boot and Spring Security, with clear separation of concerns and extensibility for future authentication features.
