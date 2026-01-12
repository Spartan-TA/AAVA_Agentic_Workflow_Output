# WAREHOUSE EMPLOYEE MANAGEMENT PLATFORM - LOW-LEVEL TECHNICAL DESIGN DOCUMENT

## Part 1: Foundation & Core Modules (E01-E04)

### Document Information
- Version: 1.0
- Date: 2024
- Status: Production Ready
- Framework: Spring Boot 3.2.0
- Java Version: 17+

---

## E01 - PROJECT SCAFFOLDING & DOMAIN SETUP

### Overview
Establishes the foundational Spring Boot project structure using Maven, configures core domain modules following Domain-Driven Design principles, sets up database migration tooling with Flyway, and enables comprehensive health monitoring.

### Package Structure
```
com.company.warehouse
âââ WarehouseApplication.java
âââ config
â   âââ DatabaseConfig.java
â   âââ ActuatorConfig.java
â   âââ OpenApiConfig.java
âââ employee (domain module)
âââ scheduling (domain module)
âââ attendance (domain module)
âââ safety (domain module)
```

### Design Specification
- Build Tool: Maven with Spring Boot parent
- Java Version: 17+ LTS
- Spring Boot: 3.2.0
- Database: PostgreSQL with Flyway migrations
- Health Monitoring: Spring Boot Actuator
- API Documentation: OpenAPI 3.0

### Sample Implementation

```java
// pom.xml dependencies
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>

// application.properties
spring.application.name=warehouse-employee-mgmt
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5432/warehouse_db
spring.flyway.enabled=true
management.endpoints.web.exposure.include=health,info,metrics
```

---

## E02 - EMPLOYEE MASTER DATA (CRUD)

### Overview
Implements the core Employee domain with comprehensive CRUD REST APIs, enforcing unique badge IDs, supporting soft-deletes, implementing pagination and filtering, and providing complete OpenAPI documentation.

### Domain Model

```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @NotBlank
    @Column(name = "badge_id", unique = true, nullable = false)
    private String badgeId;
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;
    
    private Boolean deleted = false;
}
```

### Repository Layer

```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    boolean existsByBadgeIdAndDeletedFalse(String badgeId);
}
```

### Service Layer

```java
@Service
@Transactional(readOnly = true)
public class EmployeeService {
    
    @Transactional
    public EmployeeDTO createEmployee(CreateEmployeeRequest request) {
        if (employeeRepository.existsByBadgeIdAndDeletedFalse(request.getBadgeId())) {
            throw new DuplicateResourceException("Badge ID already exists");
        }
        Employee employee = employeeMapper.toEntity(request);
        employee.setStatus(EmployeeStatus.ACTIVE);
        return employeeMapper.toDTO(employeeRepository.save(employee));
    }
    
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = findEmployeeById(id);
        employee.softDelete();
        employeeRepository.save(employee);
    }
}
```

### Controller Layer

```java
@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee Management")
public class EmployeeController {
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<EmployeeDTO> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(employeeService.createEmployee(request));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    public ResponseEntity<Page<EmployeeDTO>> getAllEmployees(
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(employeeService.getAllEmployees(pageable));
    }
}
```

---

## E03 - ROLE-BASED ACCESS CONTROL (RBAC)

### Overview
Implements comprehensive security using Spring Security with role-based access control, method and endpoint-level security, and row-level data access constraints.

### Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/swagger-ui/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/employees/**")
                    .hasAnyRole("ADMIN", "HR")
                .requestMatchers("/api/v1/attendance/clock-in")
                    .hasAnyRole("WORKER", "SUPERVISOR", "ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }
}
```

### JWT Authentication Filter

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            String username = jwtService.extractUsername(jwt);
            
            if (username != null && SecurityContextHolder.getContext()
                    .getAuthentication() == null) {
                UserDetails userDetails = userDetailsService
                    .loadUserByUsername(username);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

---

## E04 - TIME & ATTENDANCE (CLOCK IN/OUT)

### Overview
Provides comprehensive time and attendance tracking with clock-in/out endpoints, geofence validation, device capture, automatic shift association, hours calculation, and correction workflow.

### Domain Model

```java
@Entity
@Table(name = "attendance_events")
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    private AttendanceEventType eventType; // CLOCK_IN, CLOCK_OUT
    
    private LocalDateTime eventTimestamp;
    private String deviceId;
    private String ipAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean locationVerified;
    
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;
}

@Entity
@Table(name = "daily_attendance_summary")
public class DailyAttendanceSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private Employee employee;
    
    private LocalDate workDate;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private BigDecimal totalHours;
    private BigDecimal regularHours;
    private BigDecimal overtimeHours;
    private Boolean isComplete;
}
```

### Service Layer

```java
@Service
public class AttendanceService {
    
    @Transactional
    public AttendanceEventDTO clockIn(ClockEventRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new BusinessException("Employee not found"));
        
        // Validate no active clock-in
        Optional<AttendanceEvent> lastEvent = 
            eventRepository.findLastEventByEmployee(employee.getId());
        if (lastEvent.isPresent() && 
            lastEvent.get().getEventType() == AttendanceEventType.CLOCK_IN) {
            throw new BusinessException("Already clocked in");
        }
        
        // Validate geofence
        boolean locationVerified = geofenceService.isWithinWarehouseBoundary(
            request.getLatitude(), request.getLongitude());
        
        AttendanceEvent event = AttendanceEvent.builder()
            .employee(employee)
            .eventType(AttendanceEventType.CLOCK_IN)
            .eventTimestamp(LocalDateTime.now())
            .deviceId(request.getDeviceId())
            .locationVerified(locationVerified)
            .build();
        
        return mapToDTO(eventRepository.save(event));
    }
    
    @Transactional
    public AttendanceEventDTO clockOut(ClockEventRequest request) {
        // Similar implementation with clock-out logic
        // Calculate and update daily summary
    }
    
    private void updateDailySummary(
            Employee employee, 
            AttendanceEvent clockIn, 
            AttendanceEvent clockOut) {
        
        Duration duration = Duration.between(
            clockIn.getEventTimestamp(), 
            clockOut.getEventTimestamp());
        
        BigDecimal totalHours = BigDecimal.valueOf(duration.toMinutes())
            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        
        DailyAttendanceSummary summary = DailyAttendanceSummary.builder()
            .employee(employee)
            .workDate(clockIn.getEventTimestamp().toLocalDate())
            .clockInTime(clockIn.getEventTimestamp())
            .clockOutTime(clockOut.getEventTimestamp())
            .totalHours(totalHours)
            .regularHours(totalHours.min(BigDecimal.valueOf(8)))
            .overtimeHours(totalHours.subtract(BigDecimal.valueOf(8))
                .max(BigDecimal.ZERO))
            .isComplete(true)
            .build();
        
        summaryRepository.save(summary);
    }
}
```

### Controller Layer

```java
@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {
    
    @PostMapping("/clock-in")
    @PreAuthorize("hasAnyRole('WORKER', 'SUPERVISOR', 'ADMIN')")
    public ResponseEntity<AttendanceEventDTO> clockIn(
            @Valid @RequestBody ClockEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(attendanceService.clockIn(request));
    }
    
    @PostMapping("/clock-out")
    @PreAuthorize("hasAnyRole('WORKER', 'SUPERVISOR', 'ADMIN')")
    public ResponseEntity<AttendanceEventDTO> clockOut(
            @Valid @RequestBody ClockEventRequest request) {
        return ResponseEntity.ok(attendanceService.clockOut(request));
    }
    
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN', 'HR')")
    public ResponseEntity<List<DailyAttendanceSummaryDTO>> getEmployeeAttendance(
            @PathVariable Long employeeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(
            attendanceService.getEmployeeAttendance(employeeId, startDate, endDate));
    }
}
```

---

## DATABASE SCHEMA (Flyway Migrations)

### V1__initial_schema.sql

```sql
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    badge_id VARCHAR(50) UNIQUE NOT NULL,
    role VARCHAR(50) NOT NULL,
    department VARCHAR(100),
    shift_group VARCHAR(50),
    hire_date DATE,
    status VARCHAR(50) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_employees_badge_id ON employees(badge_id);
CREATE INDEX idx_employees_department ON employees(department);

CREATE TABLE attendance_events (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    event_type VARCHAR(50) NOT NULL,
    event_timestamp TIMESTAMP NOT NULL,
    device_id VARCHAR(100),
    ip_address VARCHAR(45),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    location_verified BOOLEAN DEFAULT FALSE,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attendance_employee_date 
    ON attendance_events(employee_id, event_timestamp);

CREATE TABLE daily_attendance_summary (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    work_date DATE NOT NULL,
    clock_in_time TIMESTAMP,
    clock_out_time TIMESTAMP,
    total_hours DECIMAL(5, 2),
    regular_hours DECIMAL(5, 2),
    overtime_hours DECIMAL(5, 2),
    is_complete BOOLEAN DEFAULT FALSE,
    calculated_at TIMESTAMP,
    UNIQUE(employee_id, work_date)
);
```

---

## TESTING STRATEGY

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    
    @Mock
    private EmployeeRepository employeeRepository;
    
    @InjectMocks
    private EmployeeService employeeService;
    
    @Test
    void createEmployee_Success() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
            .name("John Doe")
            .badgeId("EMP001")
            .role(Role.WORKER)
            .build();
        
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001"))
            .thenReturn(false);
        
        EmployeeDTO result = employeeService.createEmployee(request);
        
        assertNotNull(result);
        verify(employeeRepository).save(any(Employee.class));
    }
    
    @Test
    void createEmployee_DuplicateBadgeId_ThrowsException() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
            .badgeId("EMP001")
            .build();
        
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001"))
            .thenReturn(true);
        
        assertThrows(DuplicateResourceException.class, 
            () -> employeeService.createEmployee(request));
    }
}
```

### Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmployee_Success() throws Exception {
        String requestBody = """{
            "name": "John Doe",
            "badgeId": "EMP001",
            "role": "WORKER"
        }""";
        
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("John Doe"));
    }
}
```

---

## CONCLUSION - PART 1

This document provides comprehensive technical design for the foundation modules (E01-E04) of the Warehouse Employee Management Platform. Each module follows Spring Boot best practices with:

- Clean architecture and separation of concerns
- Comprehensive security with RBAC
- Complete CRUD operations with validation
- Proper error handling and logging
- Database schema with migrations
- Unit and integration testing

**Next:** Part 2 will cover E05-E10 (Scheduling, Leave, Training, Safety, Equipment, Performance)
**Next:** Part 3 will cover E11-E17 (Payroll, Notifications, Integration, Audit, Reporting, Mobile, Onboarding)