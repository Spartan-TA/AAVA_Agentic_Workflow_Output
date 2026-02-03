# Warehouse Employee Management System - Technical Design Document
## Part 1: Epics E01-E05

### E01: Project Scaffolding & Domain Setup

**Overview:** Maven project with Spring Boot 3.x, Java 17+, base package com.warehouse.employee

**Package Structure:**
- com.warehouse.employee.core
- com.warehouse.employee.employee
- com.warehouse.employee.attendance
- com.warehouse.employee.scheduling
- com.warehouse.employee.safety

**Entity Design:**
```java
@MappedSuperclass
public abstract class BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;
}
```

**Configuration:** application.yml with server.port=8080, DB config, Flyway migrations in /db/migration

---

### E02: Employee Master Data (CRUD)

**Entity Design:**
```java
@Entity
@Table(name = "employees")
public class Employee extends BaseEntity {
    @Column(nullable = false) private String name;
    @Column(nullable = false, unique = true) private String badgeId;
    @Enumerated(EnumType.STRING) private Role role;
    @ManyToOne private Department department;
    private LocalDate hireDate;
    private boolean deleted = false;
}
```

**Service Layer:**
```java
public interface EmployeeService {
    EmployeeDTO create(EmployeeDTO dto);
    EmployeeDTO update(Long id, EmployeeDTO dto);
    EmployeeDTO get(Long id);
    Page<EmployeeDTO> list(Pageable pageable);
    void delete(Long id);
}
```

**REST Endpoints:**
- POST /api/employees
- GET /api/employees/{id}
- PUT /api/employees/{id}
- DELETE /api/employees/{id}
- GET /api/employees

---

### E03: Role-Based Access Control

**Security Configuration:**
```java
@EnableWebSecurity
public class SecurityConfig {
    protected void configure(HttpSecurity http) {
        http.authorizeRequests()
            .antMatchers("/api/employees/**").hasAnyRole("ADMIN", "HR")
            .antMatchers("/api/**").authenticated()
            .and().oauth2ResourceServer().jwt();
    }
}
```

**Roles:** ADMIN, HR, SUPERVISOR, WORKER

---

### E04: Time & Attendance

**Entity Design:**
```java
@Entity
public class AttendanceEvent extends BaseEntity {
    @ManyToOne private Employee employee;
    @Enumerated(EnumType.STRING) private AttendanceType type;
    private LocalDateTime eventTime;
    private String deviceId;
    private String geoLocation;
}
```

**REST Endpoints:**
- POST /api/attendance/clock-in
- POST /api/attendance/clock-out
- GET /api/attendance/summary

---

### E05: Shift & Schedule Management

**Entity Design:**
```java
@Entity
public class ShiftTemplate extends BaseEntity {
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
}

@Entity
public class EmployeeSchedule extends BaseEntity {
    @ManyToOne private Employee employee;
    @ManyToOne private ShiftTemplate shiftTemplate;
    private LocalDate date;
}
```

**REST Endpoints:**
- POST /api/scheduling/templates
- POST /api/scheduling/assign
- GET /api/scheduling/employee/{id}
