# Low-Level Technical Design Document - Warehouse EMS

## US-001: Project Scaffolding & Domain Setup

### Description
Establishes foundational Spring Boot project with standardized packages and core modules.

### Design Specification
- Spring Boot 3.2.x with Java 17+
- Layered architecture pattern
- PostgreSQL database with Flyway migrations
- Spring Actuator for health monitoring

### Package Structure
com.warehouse.ems
- config (SecurityConfig, DatabaseConfig, OpenApiConfig)
- domain (entity, dto, mapper)
- repository
- service (impl, exception)
- controller (advice)
- security (filter, provider)
- util (constants, validator)

### Sample Implementation
```java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

## US-002: Employee Master Data Management

### Description
Implements CRUD operations for employee records with validation and soft delete.

### Design Specification
- RESTful endpoints: POST /employees, GET /employees, PUT /employees/{id}, DELETE /employees/{id}
- Pagination and filtering support
- Unique badgeId constraint
- Soft delete implementation

### Entity Design
```java
@Entity
@Table(name = "employees")
public class Employee extends BaseEntity {
    @Column(unique = true)
    private String badgeId;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate hireDate;
    private String department;
    private String status;
}
```

## US-003: Role Based Access Control

### Description
Implements Spring Security with role-based permissions.

### Design Specification
- JWT or OAuth2 authentication
- Role hierarchy: ADMIN, SUPERVISOR, EMPLOYEE
- Method-level security with @PreAuthorize
- Custom access denied handlers

### Sample Implementation
```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .build();
    }
}
```

## US-004: Time & Attendance

### Description
Tracks employee clock-in/out events with shift association.

### Design Specification
- POST /attendance/clock-in, POST /attendance/clock-out
- Automatic shift matching
- Correction workflow for missed punches
- CSV export capability

### Entity Design
```java
@Entity
public class AttendanceRecord extends BaseEntity {
    @ManyToOne
    private Employee employee;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    @ManyToOne
    private Shift shift;
    private Duration totalHours;
}
```

## US-005: Shift & Schedule Management

### Description
Manages shift templates, assignments, and conflict detection.

### Design Specification
- Shift template CRUD
- Conflict detection algorithm
- Bulk assignment support
- Audit trail for all changes

### Sample Implementation
```java
@Service
public class ShiftService {
    public void assignShift(Long employeeId, Long shiftId) {
        validateNoConflict(employeeId, shiftId);
        createAssignment(employeeId, shiftId);
        auditLog.record("SHIFT_ASSIGNED", employeeId);
    }
}
```

## US-006: Leave & Absence Management

### Description
Handles leave requests with approval workflow and balance tracking.

### Design Specification
- Leave types: PTO, SICK, UNPAID
- Approval workflow with supervisor review
- Balance accrual engine
- Integration hooks for scheduling

### Entity Design
```java
@Entity
public class LeaveRequest extends BaseEntity {
    @ManyToOne
    private Employee employee;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LeaveType type;
    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;
    private String approvedBy;
}
```