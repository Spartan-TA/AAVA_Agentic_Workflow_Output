# Comprehensive Low-Level Technical Design Document for Spring Boot User Stories
"""
EPIC 1: USER AUTHENTICATION AND AUTHORIZATION

USER STORY 1.1: User Authentication - Login with Email and Password
Section Title: User Authentication - Login with Email and Password
Description: Enables registered users to log in securely using their email and password. Implements authentication, session management, and redirects to dashboard upon success.

Spring Boot Architecture Overview:
- Uses Spring Security for authentication.
- Follows layered architecture: Controller -> Service -> Repository -> Entity.

Package Structure:
- com.example.auth.entity
- com.example.auth.repository
- com.example.auth.service
- com.example.auth.controller
- com.example.auth.config

Entity Design:
class User:
    id: Long
    email: String
    password: String (hashed)
    roles: Set<Role>

class Role:
    id: Long
    name: String (ADMIN, EDITOR, VIEWER)

Service Layer:
- AuthService: authenticateUser(email, password)

Repository Layer:
- UserRepository: findByEmail(email)

Controller Specifications:
- AuthController: POST /login

Configuration and Security Settings:
- Spring Security config: PasswordEncoder, AuthenticationManager, session management.

Integration Points:
- None for login.

Sample Implementation:

# com/example/auth/entity/User.java
class User:
    """
    @Entity
    public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String email;
        private String password;
        @ManyToMany(fetch = FetchType.EAGER)
        private Set<Role> roles;
    }
    """

# com/example/auth/entity/Role.java
class Role:
    """
    @Entity
    public class Role {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String name;
    }
    """

# com/example/auth/repository/UserRepository.java
class UserRepository:
    """
    public interface UserRepository extends JpaRepository<User, Long> {
        Optional<User> findByEmail(String email);
    }
    """

# com/example/auth/service/AuthService.java
class AuthService:
    """
    @Service
    public class AuthService {
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private PasswordEncoder passwordEncoder;

        public User authenticateUser(String email, String password) {
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
            throw new BadCredentialsException("Invalid credentials");
        }
    }
    """

# com/example/auth/controller/AuthController.java
class AuthController:
    """
    @RestController
    @RequestMapping("/auth")
    public class AuthController {
        @Autowired
        private AuthService authService;

        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody LoginRequest request) {
            User user = authService.authenticateUser(request.getEmail(), request.getPassword());
            // Generate JWT or session
            return ResponseEntity.ok(new LoginResponse(user));
        }
    }
    """

# com/example/auth/config/SecurityConfig.java
class SecurityConfig:
    """
    @Configuration
    @EnableWebSecurity
    public class SecurityConfig extends WebSecurityConfigurerAdapter {
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .csrf().disable()
                .authorizeRequests()
                    .antMatchers("/auth/login").permitAll()
                    .anyRequest().authenticated()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        }
    }
    """

---

USER STORY 1.2: Password Reset
Section Title: Password Reset
Description: Allows users to reset their password by submitting their email. Sends a reset link that expires in 1 hour via email.

Spring Boot Architecture Overview:
- Uses Spring Security for password management.
- Integrates with email service.

Package Structure:
- com.example.auth.entity
- com.example.auth.repository
- com.example.auth.service
- com.example.auth.controller
- com.example.auth.config
- com.example.auth.integration

Entity Design:
class PasswordResetToken:
    id: Long
    user: User
    token: String
    expiryDate: DateTime

Service Layer:
- PasswordResetService: createToken(email), validateToken(token), resetPassword(token, newPassword)

Repository Layer:
- PasswordResetTokenRepository: findByToken(token)

Controller Specifications:
- PasswordResetController: POST /forgot-password, POST /reset-password

Configuration and Security Settings:
- Token expiry (1 hour), secure token generation.

Integration Points:
- EmailService: sendResetLink(email, token)

Sample Implementation:

# com/example/auth/entity/PasswordResetToken.java
class PasswordResetToken:
    """
    @Entity
    public class PasswordResetToken {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @OneToOne
        private User user;
        private String token;
        private LocalDateTime expiryDate;
    }
    """

# com/example/auth/repository/PasswordResetTokenRepository.java
class PasswordResetTokenRepository:
    """
    public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
        Optional<PasswordResetToken> findByToken(String token);
    }
    """

# com/example/auth/service/PasswordResetService.java
class PasswordResetService:
    """
    @Service
    public class PasswordResetService {
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private PasswordResetTokenRepository tokenRepository;
        @Autowired
        private EmailService emailService;
        @Autowired
        private PasswordEncoder passwordEncoder;

        public void createToken(String email) {
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(user, token, LocalDateTime.now().plusHours(1));
            tokenRepository.save(resetToken);
            emailService.sendResetLink(email, token);
        }

        public boolean validateToken(String token) {
            PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token not found"));
            return resetToken.getExpiryDate().isAfter(LocalDateTime.now());
        }

        public void resetPassword(String token, String newPassword) {
            PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token not found"));
            if (!validateToken(token)) throw new TokenExpiredException("Token expired");
            User user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            tokenRepository.delete(resetToken);
        }
    }
    """

# com/example/auth/controller/PasswordResetController.java
class PasswordResetController:
    """
    @RestController
    @RequestMapping("/auth")
    public class PasswordResetController {
        @Autowired
        private PasswordResetService passwordResetService;

        @PostMapping("/forgot-password")
        public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
            passwordResetService.createToken(request.getEmail());
            return ResponseEntity.ok("Reset link sent");
        }

        @PostMapping("/reset-password")
        public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok("Password reset successful");
        }
    }
    """

# com/example/auth/integration/EmailService.java
class EmailService:
    """
    @Service
    public class EmailService {
        public void sendResetLink(String email, String token) {
            // Implementation using JavaMailSender
        }
    }
    """

---

USER STORY 1.3: Role-Based Access Control
Section Title: Role-Based Access Control
Description: Enables admins to assign roles to users, controlling access to system features. Roles: Admin, Editor, Viewer.

Spring Boot Architecture Overview:
- Uses Spring Security for RBAC.
- Role management via admin endpoints.

Package Structure:
- com.example.auth.entity
- com.example.auth.repository
- com.example.auth.service
- com.example.auth.controller
- com.example.auth.config

Entity Design:
class User (as above)
class Role (as above)

Service Layer:
- RoleService: assignRole(userId, roleName)

Repository Layer:
- RoleRepository: findByName(name)

Controller Specifications:
- RoleController: POST /users/{id}/roles

Configuration and Security Settings:
- Spring Security config: method-level security, @PreAuthorize

Integration Points:
- User management module

Sample Implementation:

# com/example/auth/repository/RoleRepository.java
class RoleRepository:
    """
    public interface RoleRepository extends JpaRepository<Role, Long> {
        Optional<Role> findByName(String name);
    }
    """

# com/example/auth/service/RoleService.java
class RoleService:
    """
    @Service
    public class RoleService {
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private RoleRepository roleRepository;

        @PreAuthorize("hasRole('ADMIN')")
        public void assignRole(Long userId, String roleName) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));
            user.getRoles().add(role);
            userRepository.save(user);
        }
    }
    """

# com/example/auth/controller/RoleController.java
class RoleController:
    """
    @RestController
    @RequestMapping("/admin")
    public class RoleController {
        @Autowired
        private RoleService roleService;

        @PostMapping("/users/{id}/roles")
        public ResponseEntity<?> assignRole(@PathVariable Long id, @RequestBody AssignRoleRequest request) {
            roleService.assignRole(id, request.getRoleName());
            return ResponseEntity.ok("Role assigned");
        }
    }
    """

# com/example/auth/config/SecurityConfig.java (additional)
class SecurityConfigRBAC:
    """
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    """

---

EPIC 2: DASHBOARD ANALYTICS

USER STORY 2.1: View Summary Metrics
Section Title: View Summary Metrics
Description: Displays summary metrics (sales, active users) on dashboard, updated every 15 minutes.

Spring Boot Architecture Overview:
- RESTful API for dashboard data.
- Scheduled tasks for metric updates.

Package Structure:
- com.example.dashboard.entity
- com.example.dashboard.repository
- com.example.dashboard.service
- com.example.dashboard.controller
- com.example.dashboard.config

Entity Design:
class Metric:
    id: Long
    name: String
    value: Double
    lastUpdated: DateTime

Service Layer:
- MetricService: getSummaryMetrics(), updateMetrics()

Repository Layer:
- MetricRepository: findAll(), save()

Controller Specifications:
- DashboardController: GET /dashboard/metrics

Configuration and Security Settings:
- Secure endpoints, scheduled updates.

Integration Points:
- Data pipeline for metric updates.

Sample Implementation:

# com/example/dashboard/entity/Metric.java
class Metric:
    """
    @Entity
    public class Metric {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String name;
        private Double value;
        private LocalDateTime lastUpdated;
    }
    """

# com/example/dashboard/repository/MetricRepository.java
class MetricRepository:
    """
    public interface MetricRepository extends JpaRepository<Metric, Long> {
        List<Metric> findAll();
    }
    """

# com/example/dashboard/service/MetricService.java
class MetricService:
    """
    @Service
    public class MetricService {
        @Autowired
        private MetricRepository metricRepository;

        public List<Metric> getSummaryMetrics() {
            return metricRepository.findAll();
        }

        @Scheduled(fixedRate = 900000) // 15 minutes
        public void updateMetrics() {
            // Fetch from data pipeline and update metrics
        }
    }
    """

# com/example/dashboard/controller/DashboardController.java
class DashboardController:
    """
    @RestController
    @RequestMapping("/dashboard")
    public class DashboardController {
        @Autowired
        private MetricService metricService;

        @GetMapping("/metrics")
        public ResponseEntity<List<Metric>> getMetrics() {
            return ResponseEntity.ok(metricService.getSummaryMetrics());
        }
    }
    """

---

USER STORY 2.2: Export Reports
Section Title: Export Reports
Description: Allows users to export dashboard reports as PDF, including all visible metrics.

Spring Boot Architecture Overview:
- RESTful API for report export.
- PDF generation library integration.

Package Structure:
- com.example.dashboard.entity
- com.example.dashboard.service
- com.example.dashboard.controller
- com.example.dashboard.integration

Entity Design:
class Metric (as above)

Service Layer:
- ReportService: generatePdfReport(metrics)

Repository Layer:
- MetricRepository (as above)

Controller Specifications:
- ReportController: GET /dashboard/export

Configuration and Security Settings:
- Secure export endpoint.

Integration Points:
- PDF generation library (e.g., iText)

Sample Implementation:

# com/example/dashboard/service/ReportService.java
class ReportService:
    """
    @Service
    public class ReportService {
        public byte[] generatePdfReport(List<Metric> metrics) {
            // Use iText or similar to generate PDF
        }
    }
    """

# com/example/dashboard/controller/ReportController.java
class ReportController:
    """
    @RestController
    @RequestMapping("/dashboard")
    public class ReportController {
        @Autowired
        private MetricService metricService;
        @Autowired
        private ReportService reportService;

        @GetMapping("/export")
        public ResponseEntity<byte[]> exportReport() {
            List<Metric> metrics = metricService.getSummaryMetrics();
            byte[] pdf = reportService.generatePdfReport(metrics);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "report.pdf");
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        }
    }
    """

# com/example/dashboard/integration/PdfGenerator.java
class PdfGenerator:
    """
    public class PdfGenerator {
        public static byte[] createPdf(List<Metric> metrics) {
            // Implementation using iText
        }
    }
    """

"""
