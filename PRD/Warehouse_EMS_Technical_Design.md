# Warehouse EMS Technical Design Document

## Executive Summary
This document provides low-level technical design for Warehouse EMS built on Spring Boot 3.2.0 with Java 17, following industry best practices.

## 1. PROJECT FOUNDATION

### Architecture
- Multi-module Maven project
- Spring Boot 3.2.0, Java 17
- PostgreSQL database
- Flyway migrations
- Spring Security with JWT

### Modules
- warehouse-ems-parent
- warehouse-ems-common
- warehouse-ems-domain
- warehouse-ems-repository
- warehouse-ems-service
- warehouse-ems-api
- warehouse-ems-security
- Feature modules: employee, attendance, scheduling, safety, asset, performance

### Base Configuration
```java
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

## 2. EMPLOYEE MANAGEMENT

### Entity
```java
@Entity
@Table(name = "employees")
public class Employee extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String badgeId;
    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String email;
    private String department;
    private String position;
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    private EmploymentStatus status;
}
```

### Repository
```java
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    Page<Employee> findAllActive(Pageable pageable);
}
```

### Service
```java
public interface EmployeeService {
    EmployeeResponse createEmployee(CreateEmployeeRequest request);
    EmployeeResponse updateEmployee(UUID id, UpdateEmployeeRequest request);
    EmployeeResponse getEmployeeById(UUID id);
    Page<EmployeeResponse> getAllEmployees(Pageable pageable);
    void deleteEmployee(UUID id);
}
```

### Controller
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request);
    
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable UUID id);
}
```

## 3. SECURITY

### Configuration
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
```

## 4. ATTENDANCE

### Entity
```java
@Entity
public class AttendanceRecord extends BaseEntity {
    @ManyToOne
    private Employee employee;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private String deviceId;
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;
}
```

### Service
```java
public AttendanceResponse clockIn(ClockInRequest request) {
    Employee employee = employeeRepository.findByBadgeId(request.getBadgeId())
        .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    AttendanceRecord record = new AttendanceRecord();
    record.setEmployee(employee);
    record.setClockInTime(LocalDateTime.now());
    record.setStatus(AttendanceStatus.CLOCKED_IN);
    return mapper.toResponse(repository.save(record));
}
```

## 5. SCHEDULING

### Shift Template
```java
@Entity
public class ShiftTemplate extends BaseEntity {
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String department;
}
```

### Conflict Detection
```java
public void validateNoConflict(Employee employee, LocalDate date) {
    List<ShiftAssignment> existing = repository.findByEmployeeAndDate(employee, date);
    if (!existing.isEmpty()) {
        throw new ShiftConflictException("Employee already assigned");
    }
}
```

## 6. LEAVE MANAGEMENT

### Entity
```java
@Entity
public class LeaveRequest extends BaseEntity {
    @ManyToOne
    private Employee employee;
    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;
}
```

## 7. SAFETY

### Certification
```java
@Entity
public class Certification extends BaseEntity {
    @ManyToOne
    private Employee employee;
    private String certificationType;
    private LocalDate expiryDate;
    @Enumerated(EnumType.STRING)
    private CertificationStatus status;
}
```

### Incident
```java
@Entity
public class SafetyIncident extends BaseEntity {
    @ManyToOne
    private Employee reporter;
    private LocalDateTime incidentDateTime;
    private String description;
    @Enumerated(EnumType.STRING)
    private IncidentSeverity severity;
}
```

## 8. ASSET MANAGEMENT

### Entity
```java
@Entity
public class Asset extends BaseEntity {
    private String assetTag;
    private String assetType;
    @ManyToOne
    private Employee assignedTo;
    private LocalDateTime assignedAt;
}
```

## 9. INTEGRATION

### HRIS Sync
```java
@Service
public class HrisIntegrationService {
    public void syncEmployees() {
        List<HrisEmployee> hrisEmployees = hrisClient.fetchEmployees();
        hrisEmployees.forEach(this::syncEmployee);
    }
}
```

## 10. AUDIT

### Audit Log
```java
@Entity
public class AuditLog extends BaseEntity {
    private String entityType;
    private UUID entityId;
    private String action;
    private String actor;
    private String beforeState;
    private String afterState;
}
```

## 11. REPORTING

### Export Service
```java
public interface ReportService {
    byte[] exportAttendanceToCsv(LocalDate start, LocalDate end);
    byte[] exportPayrollData(LocalDate start, LocalDate end);
}
```

## 12. NOTIFICATIONS

### Service
```java
@Service
public class NotificationService {
    public void sendNotification(Employee employee, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setRecipient(employee);
        notification.setMessage(message);
        notification.setType(type);
        repository.save(notification);
    }
}
```

## Configuration Files

### application.yml
```yaml
spring:
  application:
    name: warehouse-ems
  datasource:
    url: jdbc:postgresql://localhost:5432/warehouse_ems
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

## Database Schema

### Core Tables
- employees
- attendance_records
- shift_templates
- shift_assignments
- leave_requests
- certifications
- safety_incidents
- assets
- audit_logs

## API Endpoints

### Employee
- POST /employees
- GET /employees/{id}
- GET /employees
- PUT /employees/{id}
- DELETE /employees/{id}

### Attendance
- POST /attendance/clock-in
- POST /attendance/clock-out
- GET /attendance/records

### Scheduling
- POST /shifts/templates
- POST /shifts/assignments
- GET /shifts/employee/{id}

### Leave
- POST /leave/requests
- PUT /leave/requests/{id}/approve
- GET /leave/requests

### Safety
- POST /certifications
- GET /certifications/expiring
- POST /incidents

### Assets
- POST /assets/checkout
- POST /assets/checkin
- GET /assets

End of Document