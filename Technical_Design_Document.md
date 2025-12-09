Section: Project Scaffolding & Domain Setup
Description: Establishes the foundational Spring Boot architecture, base package structure, and core modules for the Warehouse EMS system. Ensures consistency and maintainability across all modules.
Design Specification:
- Spring Boot (Maven) project initialization
- Base packages: com.wms, com.wms.employee, com.wms.scheduling, com.wms.attendance, com.wms.safety
- Core modules: Employee, Scheduling, Attendance, Safety
- Database migration: Flyway/Liquibase
- Monitoring: Spring Boot Actuator
- README with build/run steps
Sample Implementation:
```java
@SpringBootApplication
public class WarehouseEmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseEmsApplication.class, args);
    }
}
```

Section: Employee Master Data (CRUD)
Description: Implements CRUD operations for Employee domain, enforcing unique badgeId, supporting soft-delete, pagination, filtering, and OpenAPI documentation.
Design Specification:
- Entity: Employee (id, name, badgeId, role, department, shiftGroup, hireDate, status)
- Repository: EmployeeRepository extends JpaRepository<Employee, Long>
- Service: EmployeeService (CRUD, soft-delete, filtering)
- Controller: EmployeeController (REST endpoints)
- DTOs: EmployeeDto, EmployeeCreateDto, EmployeeUpdateDto
- OpenAPI schemas
Sample Implementation:
```java
@Entity
public class Employee {
    @Id @GeneratedValue private Long id;
    private String name;
    @Column(unique = true) private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
    private boolean deleted;
}
```
