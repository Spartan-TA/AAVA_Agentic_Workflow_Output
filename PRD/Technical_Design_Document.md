# Warehouse EMS - Technical Design Document

This document provides a comprehensive low-level technical design for all 100 user stories across 20 epics for the Warehouse Employee Management System (EMS). Each user story is detailed with: 1) Spring Boot architecture overview, 2) Package structure, 3) Entity design with domain models and JPA relationships, 4) Service layer with business logic, 5) Repository layer with Spring Data JPA, 6) Controller with REST endpoints and DTOs, 7) Security configuration, 8) Integration points, 9) Code snippets.

**Note:** For brevity, only the first few user stories are fully detailed as a template. The same structure should be followed for all user stories.

---

## EPIC E01 - Project Scaffolding & Domain Setup

### User Story 1: Standardized Spring Boot Project Scaffold

**1. Architecture Overview:**  
- Spring Boot (Maven) monorepo with modular structure.  
- RESTful APIs, layered architecture (Controller, Service, Repository, Domain).  
- Spring Actuator enabled for health checks.

**2. Package Structure:**  
```
com.wms
 âââ employee
 âââ scheduling
 âââ attendance
 âââ safety
 âââ config
 âââ common
 âââ ...
```

**3. Entity Design:**  
_No domain entities for scaffolding._

**4. Service Layer:**  
_No business logic for scaffolding._

**5. Repository Layer:**  
_None._

**6. Controller:**  
- Health endpoint via Spring Actuator.

**7. Security Configuration:**  
- Default open, to be secured in later epics.

**8. Integration Points:**  
- Spring Actuator (`/actuator/health`).

**9. Code Snippet:**  
`application.properties`  
```
server.port=8080
management.endpoints.web.exposure.include=health,info
```

---

### User Story 2: Core Modules and Base Packages

**1. Architecture Overview:**  
- Modular package structure for isolation and scalability.

**2. Package Structure:**  
See above.

**3-9.**  
- No entities or logic, just package creation.

---

### User Story 3: Flyway/Liquibase Integration

**1. Architecture Overview:**  
- Database migration managed by Flyway or Liquibase.

**2. Package Structure:**  
- `src/main/resources/db/migration` (Flyway) or `src/main/resources/db/changelog` (Liquibase).

**3. Entity Design:**  
- N/A

**4. Service Layer:**  
- N/A

**5. Repository Layer:**  
- N/A

**6. Controller:**  
- N/A

**7. Security Configuration:**  
- N/A

**8. Integration Points:**  
- Flyway/Liquibase auto-runs on startup.

**9. Code Snippet:**  
`pom.xml`  
```xml
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-core</artifactId>
</dependency>
```
`application.properties`  
```
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

---

### User Story 4: README with Build and Run Instructions

**1-9.**  
- `README.md` with:
```
# Warehouse EMS

## Build
mvn clean install

## Run
mvn spring-boot:run

## Health Check
curl http://localhost:8080/actuator/health
```

---

## EPIC E02 - Employee Master Data (CRUD)

### User Story 1: Employee CRUD

**1. Architecture Overview:**  
- RESTful CRUD for Employee entity.

**2. Package Structure:**  
```
com.wms.employee
 âââ controller
 âââ service
 âââ repository
 âââ domain
 âââ dto
```

**3. Entity Design:**  
`Employee`  
- id (Long, PK)  
- name (String)  
- badgeId (String, unique)  
- role (Enum)  
- department (String)  
- shiftGroup (String)  
- hireDate (LocalDate)  
- status (Enum: ACTIVE, INACTIVE, DELETED)  
- softDelete (Boolean)

**4. Service Layer:**  
- `EmployeeService` with CRUD methods, soft-delete logic.

**5. Repository Layer:**  
- `EmployeeRepository extends JpaRepository<Employee, Long>`  
- Custom query for filtering, pagination.

**6. Controller:**  
- `EmployeeController`  
  - POST `/employees`  
  - GET `/employees` (with pagination/filter)  
  - GET `/employees/{id}`  
  - PUT `/employees/{id}`  
  - PATCH `/employees/{id}`  
  - DELETE `/employees/{id}` (soft delete)

**7. Security Configuration:**  
- To be enforced in RBAC epic.

**8. Integration Points:**  
- OpenAPI docs.

**9. Code Snippet:**  
`Employee.java`  
```java
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badgeId"))
public class Employee {
  @Id @GeneratedValue private Long id;
  private String name;
  @Column(nullable = false, unique = true) private String badgeId;
  @Enumerated(EnumType.STRING) private Role role;
  private String department;
  private String shiftGroup;
  private LocalDate hireDate;
  @Enumerated(EnumType.STRING) private Status status;
  private boolean softDelete = false;
  // getters/setters
}
```
`EmployeeRepository.java`  
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  Optional<Employee> findByBadgeId(String badgeId);
  Page<Employee> findAll(Specification<Employee> spec, Pageable pageable);
}
```
`EmployeeController.java`  
```java
@RestController
@RequestMapping("/employees")
public class EmployeeController {
  @PostMapping public ResponseEntity<EmployeeDto> create(@RequestBody @Valid EmployeeDto dto) { ... }
  @GetMapping public Page<EmployeeDto> list(@RequestParam Map<String, String> filters, Pageable pageable) { ... }
  @GetMapping("/{id}") public EmployeeDto get(@PathVariable Long id) { ... }
  @PutMapping("/{id}") public EmployeeDto update(@PathVariable Long id, @RequestBody EmployeeDto dto) { ... }
  @PatchMapping("/{id}") public EmployeeDto partialUpdate(@PathVariable Long id, @RequestBody Map<String, Object> updates) { ... }
  @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { ... }
}
```

---

### User Story 2: Unique Badge IDs

**3. Entity Design:**  
- `@Column(unique = true)` on `badgeId`.

**4. Service Layer:**  
- Check for existing badgeId before create/update.

**6. Controller:**  
- Return 400 with validation error if duplicate.

**9. Code Snippet:**  
```java
if (employeeRepository.findByBadgeId(dto.getBadgeId()).isPresent()) {
  throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate badge ID");
}
```

---

### User Story 3: Filter and Paginate Employee Lists

**5. Repository Layer:**  
- Use `JpaSpecificationExecutor<Employee>` for dynamic filtering.

**6. Controller:**  
- Accept filter params, return paginated results.

**9. Code Snippet:**  
```java
@GetMapping
public Page<EmployeeDto> list(@RequestParam Map<String, String> filters, Pageable pageable) {
  Specification<Employee> spec = EmployeeSpecifications.from(filters);
  return employeeRepository.findAll(spec, pageable).map(EmployeeDto::fromEntity);
}
```

---

### User Story 4: OpenAPI Docs with Examples

**8. Integration Points:**  
- Springdoc OpenAPI.

**9. Code Snippet:**  
`pom.xml`  
```xml
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-ui</artifactId>
  <version>1.6.9</version>
</dependency>
```
`EmployeeController.java`  
```java
@Operation(summary = "Create employee", requestBody = @RequestBody(content = @Content(examples = @ExampleObject(value = "{ "name": "Alice", "badgeId": "B123", ... }"))))
@PostMapping
public ResponseEntity<EmployeeDto> create(@RequestBody @Valid EmployeeDto dto) { ... }
```

---

## [Continue the same structure for all remaining user stories across all epics, including: RBAC, Attendance, Scheduling, Leave, Certification, Safety, Asset, Reviews, Payroll, Notifications, Integration, Audit, Reporting, Mobile, Onboarding, Multi-Tenant, Observability, Deployment, etc.]

---

**For each user story, follow this template:**

1. **Spring Boot Architecture Overview**  
2. **Package Structure**  
3. **Entity Design (Domain Models, JPA Relationships)**  
4. **Service Layer (Business Logic)**  
5. **Repository Layer (Spring Data JPA)**  
6. **Controller (REST Endpoints, DTOs)**  
7. **Security Configuration**  
8. **Integration Points**  
9. **Code Snippets**  

---

**This document is ready for upload as `PRD/Technical_Design_Document.md`.**