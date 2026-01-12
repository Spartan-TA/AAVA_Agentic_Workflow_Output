# Warehouse Employee Management System - User Stories

## Epic E01: Project Scaffolding & Domain Setup

### User Story 1.1: Initialize Spring Boot Project
**Title:** Initialize Spring Boot Maven Project with Base Configuration

**As a** DevOps Engineer  
**I want** to initialize a Spring Boot Maven project with proper base configuration  
**So that** the development team has a standardized foundation to build upon

**Priority:** High  
**Story Points:** 5  
**Dependencies:** None  

**Acceptance Criteria:**
```gherkin
Feature: Spring Boot Project Initialization
  As a DevOps Engineer
  I want to set up a Spring Boot Maven project
  So that developers can start building features immediately

  Scenario: Project builds successfully
    Given a fresh Spring Boot Maven project is created
    When I run the Maven build command
    Then the project should compile without errors
    And all dependencies should be resolved

  Scenario: Application starts on default port
    Given the Spring Boot application is built
    When I start the application
    Then it should run on port 8080
    And the application context should load successfully

  Scenario: README documentation exists
    Given the project repository
    When I check the root directory
    Then a README.md file should exist
    And it should contain build instructions
    And it should contain run instructions
```

**Notes:**
- Use Spring Boot 3.x with Java 17+
- Include spring-boot-starter-web and spring-boot-starter-data-jpa
- Configure application.yml with sensible defaults

---

### User Story 1.2: Configure Base Package Structure
**Title:** Set Up Modular Package Structure for Domain Modules

**As a** Software Architect  
**I want** to establish a clear package structure for all core modules  
**So that** the codebase remains organized and maintainable

**Priority:** High  
**Story Points:** 3  
**Dependencies:** US 1.1  

**Acceptance Criteria:**
```gherkin
Feature: Package Structure Organization
  As a Software Architect
  I want to create a modular package structure
  So that code is organized by domain

  Scenario: Core module packages exist
    Given the Spring Boot project structure
    When I examine the src/main/java directory
    Then I should see packages for employee module
    And I should see packages for scheduling module
    And I should see packages for attendance module
    And I should see packages for safety module

  Scenario: Each module follows layered architecture
    Given a domain module package
    When I examine its structure
    Then it should contain a controller package
    And it should contain a service package
    And it should contain a repository package
    And it should contain a domain/entity package
    And it should contain a dto package
```

**Notes:**
- Follow package-by-feature approach
- Each module should be independently testable
- Use common/shared package for cross-cutting concerns

---

### User Story 1.3: Configure Database Migration Tool
**Title:** Set Up Flyway for Database Schema Versioning

**As a** Database Administrator  
**I want** to configure Flyway for database migrations  
**So that** schema changes are versioned and reproducible

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 1.1  

**Acceptance Criteria:**
```gherkin
Feature: Database Migration Management
  As a Database Administrator
  I want to use Flyway for schema migrations
  So that database changes are tracked and automated

  Scenario: Flyway is configured
    Given the Spring Boot application
    When I check the application dependencies
    Then Flyway should be included in pom.xml
    And Flyway configuration should exist in application.yml

  Scenario: Baseline migration executes
    Given a fresh database
    When the application starts
    Then Flyway should execute the baseline migration
    And the flyway_schema_history table should be created
    And the baseline version should be recorded

  Scenario: Migration scripts follow naming convention
    Given the db/migration directory
    When I examine migration files
    Then they should follow V{version}__{description}.sql pattern
    And version numbers should be sequential
```

**Notes:**
- Place migration scripts in src/main/resources/db/migration
- Use V1__baseline.sql for initial schema
- Configure flyway.baseline-on-migrate=true for existing databases

---

### User Story 1.4: Enable Spring Boot Actuator
**Title:** Configure Spring Boot Actuator for Health Monitoring

**As a** DevOps Engineer  
**I want** to enable Spring Boot Actuator endpoints  
**So that** I can monitor application health and metrics

**Priority:** High  
**Story Points:** 3  
**Dependencies:** US 1.1  

**Acceptance Criteria:**
```gherkin
Feature: Application Health Monitoring
  As a DevOps Engineer
  I want to access actuator endpoints
  So that I can monitor application status

  Scenario: Health endpoint is accessible
    Given the application is running
    When I send a GET request to /actuator/health
    Then I should receive a 200 OK response
    And the response should contain status "UP"

  Scenario: Info endpoint provides application details
    Given the application is running
    When I send a GET request to /actuator/info
    Then I should receive application name and version

  Scenario: Metrics endpoint is secured
    Given the application is running
    When I send a GET request to /actuator/metrics without authentication
    Then I should receive a 401 Unauthorized response
```

**Notes:**
- Expose health and info endpoints publicly
- Secure sensitive endpoints (metrics, env) with authentication
- Configure management.endpoints.web.exposure.include

---

## Epic E02: Employee Master Data (CRUD)

### User Story 2.1: Create Employee Entity and Repository
**Title:** Implement Employee Domain Model with JPA

**As a** Backend Developer  
**I want** to create an Employee entity with all required fields  
**So that** employee data can be persisted in the database

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 1.1, US 1.2, US 1.3  

**Acceptance Criteria:**
```gherkin
Feature: Employee Entity Management
  As a Backend Developer
  I want to define the Employee entity
  So that employee records can be stored

  Scenario: Employee entity has required fields
    Given the Employee entity class
    When I examine its fields
    Then it should have id (UUID)
    And it should have name (String)
    And it should have badgeId (String, unique)
    And it should have role (Enum)
    And it should have department (String)
    And it should have shiftGroup (String)
    And it should have hireDate (LocalDate)
    And it should have status (Enum: ACTIVE, INACTIVE, TERMINATED)
    And it should have audit fields (createdAt, updatedAt, createdBy, updatedBy)

  Scenario: Badge ID uniqueness is enforced
    Given an employee with badgeId "EMP001" exists
    When I try to create another employee with badgeId "EMP001"
    Then a DataIntegrityViolationException should be thrown

  Scenario: Soft delete is supported
    Given an active employee
    When I mark the employee as deleted
    Then the status should change to TERMINATED
    And the record should remain in the database
```

**Notes:**
- Use @Entity and @Table annotations
- Add @Column(unique=true) for badgeId
- Implement soft delete using status field
- Use Lombok for boilerplate code reduction

---

### User Story 2.2: Implement Employee REST API - Create
**Title:** Create POST Endpoint for Employee Registration

**As an** HR Administrator  
**I want** to create new employee records via API  
**So that** new hires can be registered in the system

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 2.1  

**Acceptance Criteria:**
```gherkin
Feature: Employee Creation API
  As an HR Administrator
  I want to create employee records
  So that new employees are registered

  Scenario: Successfully create an employee
    Given I have valid employee data
    When I send a POST request to /api/v1/employees
    Then I should receive a 201 Created response
    And the response should contain the employee ID
    And the employee should be persisted in the database

  Scenario: Validation fails for missing required fields
    Given I have employee data without name
    When I send a POST request to /api/v1/employees
    Then I should receive a 400 Bad Request response
    And the response should contain validation error details

  Scenario: Duplicate badge ID is rejected
    Given an employee with badgeId "EMP001" exists
    When I try to create another employee with badgeId "EMP001"
    Then I should receive a 409 Conflict response
    And the error message should indicate duplicate badge ID

  Scenario: OpenAPI documentation is available
    Given the application is running
    When I access /v3/api-docs
    Then the POST /employees endpoint should be documented
    And it should include request/response examples
```

**Notes:**
- Use @Valid for request body validation
- Implement EmployeeCreateDTO with validation annotations
- Return EmployeeResponseDTO in response
- Add Swagger/OpenAPI annotations

---

### User Story 2.3: Implement Employee REST API - Read
**Title:** Create GET Endpoints for Employee Retrieval

**As a** Supervisor  
**I want** to retrieve employee information  
**So that** I can view employee details and lists

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 2.1  

**Acceptance Criteria:**
```gherkin
Feature: Employee Retrieval API
  As a Supervisor
  I want to retrieve employee data
  So that I can view employee information

  Scenario: Get employee by ID
    Given an employee with ID "123e4567-e89b-12d3-a456-426614174000" exists
    When I send a GET request to /api/v1/employees/123e4567-e89b-12d3-a456-426614174000
    Then I should receive a 200 OK response
    And the response should contain employee details

  Scenario: Get non-existent employee returns 404
    Given no employee with ID "123e4567-e89b-12d3-a456-426614174999" exists
    When I send a GET request to /api/v1/employees/123e4567-e89b-12d3-a456-426614174999
    Then I should receive a 404 Not Found response

  Scenario: List all employees with pagination
    Given 50 employees exist in the system
    When I send a GET request to /api/v1/employees?page=0&size=20
    Then I should receive a 200 OK response
    And the response should contain 20 employees
    And pagination metadata should be included

  Scenario: Filter employees by department
    Given employees exist in "Warehouse" and "Shipping" departments
    When I send a GET request to /api/v1/employees?department=Warehouse
    Then I should receive only employees from Warehouse department

  Scenario: Filter employees by status
    Given active and terminated employees exist
    When I send a GET request to /api/v1/employees?status=ACTIVE
    Then I should receive only active employees
```

**Notes:**
- Implement pagination using Spring Data Pageable
- Support filtering by department, role, status, shiftGroup
- Use Specification pattern for dynamic queries
- Return Page<EmployeeResponseDTO>

---

### User Story 2.4: Implement Employee REST API - Update
**Title:** Create PUT and PATCH Endpoints for Employee Updates

**As an** HR Administrator  
**I want** to update employee information  
**So that** employee records remain current

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 2.1  

**Acceptance Criteria:**
```gherkin
Feature: Employee Update API
  As an HR Administrator
  I want to update employee records
  So that information stays accurate

  Scenario: Full update with PUT
    Given an employee with ID "123" exists
    When I send a PUT request to /api/v1/employees/123 with complete data
    Then I should receive a 200 OK response
    And all fields should be updated

  Scenario: Partial update with PATCH
    Given an employee with ID "123" exists
    When I send a PATCH request to /api/v1/employees/123 with {"department": "Shipping"}
    Then I should receive a 200 OK response
    And only the department field should be updated
    And other fields should remain unchanged

  Scenario: Update non-existent employee returns 404
    Given no employee with ID "999" exists
    When I send a PUT request to /api/v1/employees/999
    Then I should receive a 404 Not Found response

  Scenario: Cannot update to duplicate badge ID
    Given employee "123" with badgeId "EMP001" exists
    And employee "456" with badgeId "EMP002" exists
    When I try to update employee "456" badgeId to "EMP001"
    Then I should receive a 409 Conflict response
```

**Notes:**
- PUT replaces entire resource
- PATCH updates only provided fields
- Validate badge ID uniqueness on update
- Update audit fields (updatedAt, updatedBy)

---

### User Story 2.5: Implement Employee REST API - Delete
**Title:** Create DELETE Endpoint for Employee Soft Delete

**As an** HR Administrator  
**I want** to deactivate employee records  
**So that** terminated employees are marked appropriately

**Priority:** High  
**Story Points:** 3  
**Dependencies:** US 2.1  

**Acceptance Criteria:**
```gherkin
Feature: Employee Soft Delete API
  As an HR Administrator
  I want to soft delete employees
  So that historical data is preserved

  Scenario: Soft delete an employee
    Given an active employee with ID "123" exists
    When I send a DELETE request to /api/v1/employees/123
    Then I should receive a 204 No Content response
    And the employee status should be TERMINATED
    And the record should still exist in the database

  Scenario: Soft deleted employees excluded from default queries
    Given an employee with ID "123" is soft deleted
    When I send a GET request to /api/v1/employees
    Then the employee "123" should not appear in results

  Scenario: Can retrieve soft deleted employees with filter
    Given an employee with ID "123" is soft deleted
    When I send a GET request to /api/v1/employees?includeTerminated=true
    Then the employee "123" should appear in results

  Scenario: Delete non-existent employee returns 404
    Given no employee with ID "999" exists
    When I send a DELETE request to /api/v1/employees/999
    Then I should receive a 404 Not Found response
```

**Notes:**
- Implement soft delete by setting status to TERMINATED
- Add deletedAt timestamp field
- Default queries should filter out terminated employees
- Provide option to include terminated in queries

---

## Epic E03: Role-Based Access Control (RBAC)

### User Story 3.1: Configure Spring Security
**Title:** Set Up Spring Security with Role-Based Authentication

**As a** Security Engineer  
**I want** to configure Spring Security with role-based access control  
**So that** API endpoints are protected based on user roles

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 1.1, US 2.1  

**Acceptance Criteria:**
```gherkin
Feature: Spring Security Configuration
  As a Security Engineer
  I want to enable role-based security
  So that access is controlled by user roles

  Scenario: Security configuration is active
    Given the application is running
    When I send a request to a protected endpoint without authentication
    Then I should receive a 401 Unauthorized response

  Scenario: Roles are defined
    Given the security configuration
    When I examine the role definitions
    Then ADMIN role should exist
    And HR role should exist
    And SUPERVISOR role should exist
    And WORKER role should exist

  Scenario: API key authentication is supported
    Given API key authentication is enabled
    When I send a request with valid API key in header
    Then the request should be authenticated
    And the user roles should be extracted from the key

  Scenario: OAuth2 authentication can be toggled
    Given OAuth2 is configured in application.yml
    When I set security.auth.type=oauth2
    Then OAuth2 authentication should be active
    When I set security.auth.type=apikey
    Then API key authentication should be active
```

**Notes:**
- Use Spring Security 6.x
- Support both API key and OAuth2 authentication
- Store configuration in application.yml
- Implement custom authentication filters

---

### User Story 3.2: Implement Method-Level Security
**Title:** Apply Method Security Annotations to Service Layer

**As a** Security Engineer  
**I want** to apply method-level security annotations  
**So that** business logic is protected at the service layer

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Method-Level Security
  As a Security Engineer
  I want to secure service methods
  So that unauthorized access is prevented

  Scenario: Admin-only methods are protected
    Given a method annotated with @PreAuthorize("hasRole('ADMIN')")
    When a user with WORKER role tries to invoke it
    Then an AccessDeniedException should be thrown

  Scenario: Supervisor can access team data
    Given a method to retrieve team employees
    And it's annotated with @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN')")
    When a SUPERVISOR user invokes it
    Then the method should execute successfully

  Scenario: Workers can only access own data
    Given a method to retrieve employee details
    And it includes row-level security check
    When a WORKER user requests their own data
    Then the request should succeed
    When a WORKER user requests another employee's data
    Then an AccessDeniedException should be thrown
```

**Notes:**
- Enable @EnableMethodSecurity
- Use @PreAuthorize and @PostAuthorize annotations
- Implement custom security expressions for row-level checks
- Test all security annotations

---

### User Story 3.3: Implement Endpoint-Level Security
**Title:** Configure HTTP Security for REST Endpoints

**As a** Security Engineer  
**I want** to configure endpoint-level security rules  
**So that** API access is controlled by role

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Endpoint Security Configuration
  As a Security Engineer
  I want to define security rules for endpoints
  So that access is role-based

  Scenario: Public endpoints are accessible
    Given the /actuator/health endpoint
    When an unauthenticated user accesses it
    Then the request should succeed

  Scenario: Admin endpoints require ADMIN role
    Given the POST /api/v1/employees endpoint
    When a user with WORKER role tries to access it
    Then I should receive a 403 Forbidden response
    When a user with ADMIN role accesses it
    Then the request should be authorized

  Scenario: Supervisor endpoints require SUPERVISOR or ADMIN
    Given the GET /api/v1/schedules endpoint
    When a user with WORKER role tries to access it
    Then I should receive a 403 Forbidden response
    When a user with SUPERVISOR role accesses it
    Then the request should be authorized

  Scenario: Worker endpoints are accessible to all authenticated users
    Given the GET /api/v1/employees/me endpoint
    When any authenticated user accesses it
    Then the request should succeed
```

**Notes:**
- Configure SecurityFilterChain bean
- Use requestMatchers() for endpoint patterns
- Define role requirements per endpoint
- Ensure proper ordering of security rules

---

### User Story 3.4: Implement Row-Level Security
**Title:** Add Data Access Constraints Based on User Context

**As a** Security Engineer  
**I want** to implement row-level security constraints  
**So that** users can only access data they're authorized to see

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 3.1, US 3.2  

**Acceptance Criteria:**
```gherkin
Feature: Row-Level Data Security
  As a Security Engineer
  I want to filter data based on user context
  So that users see only authorized records

  Scenario: Supervisors see only their team
    Given a SUPERVISOR user manages department "Warehouse"
    When they query employees
    Then only employees from "Warehouse" department should be returned

  Scenario: Workers see only their own data
    Given a WORKER user with ID "123"
    When they query employee details
    Then only their own record should be accessible

  Scenario: Admins see all data
    Given an ADMIN user
    When they query employees
    Then all employee records should be returned

  Scenario: Unauthorized access returns 403
    Given a WORKER user with ID "123"
    When they try to access employee "456" details
    Then I should receive a 403 Forbidden response
```

**Notes:**
- Implement custom repository methods with security filters
- Use Spring Security context to get current user
- Add @PostFilter for collection results
- Create custom SpEL expressions for complex rules

---

### User Story 3.5: Security Testing Suite
**Title:** Create Comprehensive Security Tests

**As a** QA Engineer  
**I want** to create automated security tests  
**So that** security rules are verified and maintained

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 3.1, US 3.2, US 3.3, US 3.4  

**Acceptance Criteria:**
```gherkin
Feature: Security Test Coverage
  As a QA Engineer
  I want comprehensive security tests
  So that security is continuously verified

  Scenario: Unauthorized access tests
    Given security test suite
    When I run unauthorized access tests
    Then all protected endpoints should return 401 without auth

  Scenario: Forbidden access tests
    Given security test suite
    When I run forbidden access tests
    Then all role-protected endpoints should return 403 for wrong roles

  Scenario: Method security tests
    Given security test suite
    When I run method security tests
    Then all @PreAuthorize annotations should be tested
    And all @PostAuthorize annotations should be tested

  Scenario: Row-level security tests
    Given security test suite
    When I run row-level security tests
    Then data filtering should be verified for each role
```

**Notes:**
- Use @WithMockUser for testing different roles
- Test both positive and negative scenarios
- Achieve >90% coverage of security code
- Include integration tests with real security context

---

## Epic E04: Time & Attendance (Clock In/Out)

### User Story 4.1: Create Attendance Entity and Repository
**Title:** Implement Attendance Domain Model

**As a** Backend Developer  
**I want** to create an Attendance entity for clock events  
**So that** time tracking data can be persisted

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 2.1  

**Acceptance Criteria:**
```gherkin
Feature: Attendance Entity Management
  As a Backend Developer
  I want to define the Attendance entity
  So that clock events are stored

  Scenario: Attendance entity has required fields
    Given the Attendance entity class
    When I examine its fields
    Then it should have id (UUID)
    And it should have employeeId (UUID, foreign key)
    And it should have clockInTime (ZonedDateTime)
    And it should have clockOutTime (ZonedDateTime, nullable)
    And it should have shiftId (UUID, nullable)
    And it should have location (String, nullable)
    And it should have deviceInfo (String)
    And it should have status (Enum: CLOCKED_IN, CLOCKED_OUT, MISSED_PUNCH, CORRECTED)
    And it should have hoursWorked (BigDecimal, calculated)
    And it should have notes (String, nullable)

  Scenario: Hours worked is calculated on clock out
    Given an attendance record with clock in at 08:00
    When clock out time is set to 17:00
    Then hoursWorked should be calculated as 9.0

  Scenario: Attendance records are linked to employee
    Given an employee with ID "123"
    When I create an attendance record for employee "123"
    Then the record should be associated with the employee
```

**Notes:**
- Use ZonedDateTime for timezone awareness
- Store location as JSON for geofence data
- Calculate hoursWorked considering break times
- Add indexes on employeeId and clockInTime

---

### User Story 4.2: Implement Clock-In Endpoint
**Title:** Create POST Endpoint for Clock-In Events

**As a** Warehouse Worker  
**I want** to clock in at the start of my shift  
**So that** my work hours are tracked

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 4.1, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Clock-In Functionality
  As a Warehouse Worker
  I want to clock in
  So that my shift start is recorded

  Scenario: Successfully clock in
    Given I am an authenticated worker
    And I am not currently clocked in
    When I send a POST request to /api/v1/attendance/clock-in
    Then I should receive a 201 Created response
    And an attendance record should be created with current timestamp
    And the status should be CLOCKED_IN

  Scenario: Cannot clock in twice
    Given I am already clocked in
    When I try to clock in again
    Then I should receive a 400 Bad Request response
    And the error should indicate already clocked in

  Scenario: Clock in with location data
    Given I provide geofence coordinates
    When I clock in
    Then the location should be stored with the attendance record

  Scenario: Clock in captures device information
    Given I clock in from a mobile device
    When the clock-in is processed
    Then device information should be captured
    And stored in the attendance record

  Scenario: Clock in associates with scheduled shift
    Given I have a scheduled shift at 08:00
    When I clock in at 07:55
    Then the attendance should be linked to the scheduled shift
```

**Notes:**
- Validate user is not already clocked in
- Capture IP address and user agent
- Optional geofence validation
- Auto-associate with scheduled shift if within tolerance window

---

### User Story 4.3: Implement Clock-Out Endpoint
**Title:** Create POST Endpoint for Clock-Out Events

**As a** Warehouse Worker  
**I want** to clock out at the end of my shift  
**So that** my work hours are calculated

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 4.1, US 4.2  

**Acceptance Criteria:**
```gherkin
Feature: Clock-Out Functionality
  As a Warehouse Worker
  I want to clock out
  So that my shift end is recorded

  Scenario: Successfully clock out
    Given I am currently clocked in
    When I send a POST request to /api/v1/attendance/clock-out
    Then I should receive a 200 OK response
    And the attendance record should be updated with clock out time
    And the status should be CLOCKED_OUT
    And hoursWorked should be calculated

  Scenario: Cannot clock out without clocking in
    Given I am not clocked in
    When I try to clock out
    Then I should receive a 400 Bad Request response
    And the error should indicate not clocked in

  Scenario: Hours worked calculation
    Given I clocked in at 08:00
    When I clock out at 17:00
    Then hoursWorked should be 9.0
    And the calculation should account for break times

  Scenario: Clock out with location validation
    Given geofence validation is enabled
    And I clock out outside the geofence
    Then I should receive a warning
    But the clock out should still be recorded
```

**Notes:**
- Update existing attendance record
- Calculate hours worked in decimal format
- Consider configured break times
- Validate clock-out location if geofence enabled

---

### User Story 4.4: Handle Missed Punches
**Title:** Implement Missed Punch Detection and Correction Workflow

**As a** Supervisor  
**I want** to identify and correct missed clock punches  
**So that** attendance records are accurate

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 4.1, US 4.2, US 4.3  

**Acceptance Criteria:**
```gherkin
Feature: Missed Punch Management
  As a Supervisor
  I want to manage missed punches
  So that attendance is complete

  Scenario: Detect missed clock-out
    Given an employee clocked in yesterday
    And they never clocked out
    When the daily attendance job runs
    Then the record should be flagged as MISSED_PUNCH
    And a correction task should be created

  Scenario: Supervisor corrects missed punch
    Given an attendance record with MISSED_PUNCH status
    When a supervisor submits a correction with clock-out time
    Then the attendance record should be updated
    And the status should be CORRECTED
    And an audit entry should be created

  Scenario: Correction requires approval
    Given a missed punch correction is submitted
    When the correction is saved
    Then it should be in PENDING_APPROVAL status
    And a notification should be sent to the supervisor

  Scenario: Employee can request correction
    Given I am a worker with a missed punch
    When I submit a correction request
    Then a task should be created for my supervisor
    And I should receive a confirmation
```

**Notes:**
- Run scheduled job to detect missed punches
- Create approval workflow for corrections
- Maintain audit trail of all corrections
- Send notifications for pending corrections

---

### User Story 4.5: Calculate Daily Attendance Totals
**Title:** Implement Daily Hours Calculation and Reporting

**As an** HR Administrator  
**I want** to view daily attendance totals per employee  
**So that** I can verify hours worked

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 4.1, US 4.2, US 4.3  

**Acceptance Criteria:**
```gherkin
Feature: Daily Attendance Totals
  As an HR Administrator
  I want to see daily attendance summaries
  So that I can review hours worked

  Scenario: Calculate daily totals
    Given attendance records for a specific date
    When I request daily totals
    Then I should see total hours per employee
    And regular hours should be separated from overtime

  Scenario: Export daily attendance report
    Given attendance data for a date range
    When I request a CSV export
    Then I should receive a file with all attendance records
    And it should include employee name, date, clock in/out, hours

  Scenario: View attendance summary by department
    Given attendance records across multiple departments
    When I filter by department "Warehouse"
    Then I should see totals only for Warehouse employees

  Scenario: Identify attendance anomalies
    Given daily attendance data
    When I view the summary
    Then records with missed punches should be highlighted
    And overtime hours should be flagged
```

**Notes:**
- Implement aggregation queries for daily totals
- Support CSV export with proper formatting
- Calculate regular vs overtime hours
- Highlight exceptions and anomalies

---

## Epic E05: Shift & Schedule Management

### User Story 5.1: Create Shift Template Entity
**Title:** Implement Shift Template Domain Model

**As a** Backend Developer  
**I want** to create a ShiftTemplate entity  
**So that** recurring shift patterns can be defined

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 2.1  

**Acceptance Criteria:**
```gherkin
Feature: Shift Template Management
  As a Backend Developer
  I want to define shift templates
  So that schedules can be created from patterns

  Scenario: Shift template has required fields
    Given the ShiftTemplate entity
    When I examine its fields
    Then it should have id (UUID)
    And it should have name (String)
    And it should have startTime (LocalTime)
    And it should have endTime (LocalTime)
    And it should have daysOfWeek (Set<DayOfWeek>)
    And it should have department (String)
    And it should have requiredHeadcount (Integer)
    And it should have isActive (Boolean)

  Scenario: Shift template defines work hours
    Given a shift template "Morning Shift"
    When startTime is 08:00 and endTime is 16:00
    Then the shift duration should be 8 hours

  Scenario: Shift template can be recurring
    Given a shift template for Monday, Wednesday, Friday
    When I create schedules from this template
    Then shifts should be generated for those days only
```

**Notes:**
- Support multiple days of week per template
- Calculate shift duration automatically
- Allow templates to be activated/deactivated
- Link templates to departments

---

### User Story 5.2: Create Schedule Entity and Assignment
**Title:** Implement Schedule and Employee Assignment Model

**As a** Backend Developer  
**I want** to create Schedule entity with employee assignments  
**So that** employees can be assigned to specific shifts

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 2.1, US 5.1  

**Acceptance Criteria:**
```gherkin
Feature: Schedule Assignment Management
  As a Backend Developer
  I want to assign employees to shifts
  So that work schedules are defined

  Scenario: Schedule entity has required fields
    Given the Schedule entity
    When I examine its fields
    Then it should have id (UUID)
    And it should have shiftTemplateId (UUID)
    And it should have date (LocalDate)
    And it should have startDateTime (ZonedDateTime)
    And it should have endDateTime (ZonedDateTime)
    And it should have status (Enum: DRAFT, PUBLISHED, COMPLETED)

  Scenario: Employee can be assigned to schedule
    Given a schedule for a specific date and shift
    When I assign employee "123" to the schedule
    Then a ScheduleAssignment record should be created
    And it should link employee to schedule

  Scenario: Multiple employees can be assigned to one shift
    Given a shift requires 5 workers
    When I assign 5 employees to the schedule
    Then all assignments should be created
    And the shift should be fully staffed
```

**Notes:**
- Create ScheduleAssignment join entity
- Track assignment status (ASSIGNED, CONFIRMED, COMPLETED, ABSENT)
- Support many-to-many relationship between employees and schedules
- Validate headcount requirements

---

### User Story 5.3: Implement Shift Template CRUD API
**Title:** Create REST Endpoints for Shift Template Management

**As a** Scheduling Manager  
**I want** to create and manage shift templates  
**So that** I can define standard shift patterns

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 5.1, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Shift Template API
  As a Scheduling Manager
  I want to manage shift templates
  So that I can define shift patterns

  Scenario: Create shift template
    Given I am authenticated as ADMIN or SUPERVISOR
    When I POST to /api/v1/shift-templates with valid data
    Then I should receive a 201 Created response
    And the template should be persisted

  Scenario: List all shift templates
    Given multiple shift templates exist
    When I GET /api/v1/shift-templates
    Then I should receive all active templates
    And they should be sorted by name

  Scenario: Update shift template
    Given a shift template with ID "123" exists
    When I PUT /api/v1/shift-templates/123 with updated data
    Then the template should be updated
    And existing schedules should not be affected

  Scenario: Deactivate shift template
    Given an active shift template
    When I PATCH /api/v1/shift-templates/123 with {"isActive": false}
    Then the template should be deactivated
    And it should not appear in active template lists
```

**Notes:**
- Require ADMIN or SUPERVISOR role
- Validate time ranges (endTime > startTime)
- Support filtering by department
- Prevent deletion of templates with existing schedules

---

### User Story 5.4: Implement Schedule Creation and Assignment API
**Title:** Create REST Endpoints for Schedule Management

**As a** Supervisor  
**I want** to create schedules and assign employees  
**So that** shifts are properly staffed

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 5.1, US 5.2, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Schedule Management API
  As a Supervisor
  I want to create and manage schedules
  So that employees know their shifts

  Scenario: Create schedule from template
    Given a shift template "Morning Shift"
    When I POST to /api/v1/schedules with template ID and date
    Then a schedule should be created for that date
    And start/end times should match the template

  Scenario: Assign employee to schedule
    Given a schedule with ID "123" exists
    When I POST to /api/v1/schedules/123/assignments with employee ID
    Then the employee should be assigned to the shift
    And the assignment should be in ASSIGNED status

  Scenario: Bulk assign employees
    Given a schedule requiring 5 workers
    When I POST to /api/v1/schedules/123/assignments/bulk with 5 employee IDs
    Then all 5 employees should be assigned
    And the shift should be marked as fully staffed

  Scenario: Detect scheduling conflicts
    Given employee "123" is assigned to a shift from 08:00-16:00
    When I try to assign them to another shift from 14:00-22:00
    Then I should receive a 409 Conflict response
    And the error should indicate overlapping shifts

  Scenario: Publish schedule
    Given a schedule in DRAFT status
    When I PATCH /api/v1/schedules/123 with {"status": "PUBLISHED"}
    Then the schedule should be published
    And assigned employees should be notified
```

**Notes:**
- Validate no overlapping assignments for same employee
- Check employee availability and leave
- Support bulk operations for efficiency
- Send notifications when schedules are published

---

### User Story 5.5: Implement Schedule Conflict Detection
**Title:** Add Validation to Prevent Scheduling Conflicts

**As a** Supervisor  
**I want** the system to detect scheduling conflicts  
**So that** employees are not double-booked

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 5.2, US 5.4  

**Acceptance Criteria:**
```gherkin
Feature: Schedule Conflict Detection
  As a Supervisor
  I want conflicts to be detected automatically
  So that scheduling errors are prevented

  Scenario: Detect overlapping shift assignments
    Given employee "123" is assigned to shift A (08:00-16:00)
    When I try to assign them to shift B (14:00-22:00) on the same day
    Then the system should reject the assignment
    And return a conflict error with details

  Scenario: Detect conflicts with approved leave
    Given employee "123" has approved PTO on 2024-01-15
    When I try to assign them to a shift on 2024-01-15
    Then the system should reject the assignment
    And indicate the employee is on leave

  Scenario: Detect conflicts with blackout dates
    Given 2024-12-25 is marked as a blackout date
    When I try to create a schedule for 2024-12-25
    Then the system should warn about the blackout
    But allow creation with confirmation

  Scenario: Check certification requirements
    Given a shift requires forklift certification
    And employee "123" does not have valid forklift cert
    When I try to assign employee "123" to the shift
    Then the system should reject the assignment
    And indicate missing certification

  Scenario: Validate maximum hours per week
    Given employee "123" is already scheduled for 38 hours this week
    When I try to assign them to an 8-hour shift
    Then the system should warn about overtime
    But allow assignment with confirmation
```

**Notes:**
- Implement comprehensive validation service
- Check overlapping time ranges
- Integrate with leave management
- Validate certification requirements
- Enforce overtime rules with warnings

---

### User Story 5.6: Employee Schedule View
**Title:** Create API for Employees to View Their Schedules

**As a** Warehouse Worker  
**I want** to view my upcoming shifts  
**So that** I know when I'm scheduled to work

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 5.2, US 5.4, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Employee Schedule View
  As a Warehouse Worker
  I want to see my schedule
  So that I know my shifts

  Scenario: View my upcoming shifts
    Given I am an authenticated worker
    When I GET /api/v1/schedules/me
    Then I should see my assigned shifts for the next 14 days
    And each shift should show date, start time, end time, location

  Scenario: Filter schedule by date range
    Given I have shifts scheduled
    When I GET /api/v1/schedules/me?startDate=2024-01-01&endDate=2024-01-31
    Then I should see only shifts within that date range

  Scenario: View schedule in calendar format
    Given I have multiple shifts
    When I GET /api/v1/schedules/me?format=calendar
    Then I should receive data formatted for calendar display

  Scenario: See shift details
    Given I have a shift assignment
    When I GET /api/v1/schedules/assignments/123
    Then I should see full shift details
    And department, supervisor, and coworkers assigned
```

**Notes:**
- Return only published schedules
- Support calendar and list formats
- Include shift details and location
- Show coworkers assigned to same shift

---

## Epic E06: Leave & Absence Management

### User Story 6.1: Create Leave Request Entity
**Title:** Implement Leave Request Domain Model

**As a** Backend Developer  
**I want** to create a LeaveRequest entity  
**So that** time-off requests can be managed

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 2.1  

**Acceptance Criteria:**
```gherkin
Feature: Leave Request Entity
  As a Backend Developer
  I want to define leave request structure
  So that absences are tracked

  Scenario: Leave request has required fields
    Given the LeaveRequest entity
    When I examine its fields
    Then it should have id (UUID)
    And it should have employeeId (UUID)
    And it should have leaveType (Enum: PTO, SICK, UNPAID, BEREAVEMENT, etc.)
    And it should have startDate (LocalDate)
    And it should have endDate (LocalDate)
    And it should have totalDays (BigDecimal)
    And it should have status (Enum: PENDING, APPROVED, DENIED, CANCELLED)
    And it should have reason (String, optional)
    And it should have approvedBy (UUID, nullable)
    And it should have approvedAt (ZonedDateTime, nullable)

  Scenario: Calculate total days
    Given a leave request from 2024-01-15 to 2024-01-19
    When the request is created
    Then totalDays should be calculated as 5
    And weekends should be excluded if configured

  Scenario: Leave request links to employee
    Given an employee with ID "123"
    When a leave request is created for employee "123"
    Then the request should be associated with the employee
```

**Notes:**
- Support multiple leave types
- Calculate business days vs calendar days
- Track approval workflow
- Link to employee entity

---

### User Story 6.2: Create Leave Balance Entity
**Title:** Implement Leave Balance and Accrual Tracking

**As a** Backend Developer  
**I want** to create a LeaveBalance entity  
**So that** accrual balances can be tracked per employee

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 2.1, US 6.1  

**Acceptance Criteria:**
```gherkin
Feature: Leave Balance Management
  As a Backend Developer
  I want to track leave balances
  So that accruals are managed

  Scenario: Leave balance has required fields
    Given the LeaveBalance entity
    When I examine its fields
    Then it should have id (UUID)
    And it should have employeeId (UUID)
    And it should have leaveType (Enum)
    And it should have year (Integer)
    And it should have accrued (BigDecimal)
    And it should have used (BigDecimal)
    And it should have available (BigDecimal, calculated)
    And it should have carryOver (BigDecimal)

  Scenario: Available balance is calculated
    Given an employee has accrued 15 days PTO
    And they have used 5 days
    When I query their balance
    Then available should be 10 days

  Scenario: Balance is updated when leave is approved
    Given an employee has 10 days available PTO
    When a 3-day PTO request is approved
    Then used should increase by 3
    And available should decrease to 7

  Scenario: Prevent negative balance
    Given an employee has 2 days available PTO
    When they request 5 days PTO
    Then the system should reject the request
    And indicate insufficient balance
```

**Notes:**
- One balance record per employee per leave type per year
- Calculate available as (accrued + carryOver - used)
- Update balances when requests are approved
- Validate sufficient balance before approval

---

### User Story 6.3: Implement Leave Request API
**Title:** Create REST Endpoints for Leave Request Management

**As a** Warehouse Worker  
**I want** to request time off via API  
**So that** my absences are formally recorded

**Priority:** Medium  
**Story Points:** 8  
**Dependencies:** US 6.1, US 6.2, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Leave Request API
  As a Warehouse Worker
  I want to request leave
  So that my time off is approved

  Scenario: Submit leave request
    Given I am an authenticated employee
    When I POST to /api/v1/leave-requests with valid data
    Then I should receive a 201 Created response
    And the request should be in PENDING status
    And my supervisor should be notified

  Scenario: View my leave requests
    Given I have submitted leave requests
    When I GET /api/v1/leave-requests/me
    Then I should see all my requests
    And they should be sorted by date descending

  Scenario: Cancel pending request
    Given I have a PENDING leave request
    When I DELETE /api/v1/leave-requests/123
    Then the request status should change to CANCELLED
    And my balance should not be affected

  Scenario: Cannot cancel approved request
    Given I have an APPROVED leave request
    When I try to DELETE /api/v1/leave-requests/123
    Then I should receive a 400 Bad Request response
    And the error should indicate request is already approved

  Scenario: Validate sufficient balance
    Given I have 2 days PTO available
    When I request 5 days PTO
    Then I should receive a 400 Bad Request response
    And the error should indicate insufficient balance
```

**Notes:**
- Employees can only manage their own requests
- Validate balance before creating request
- Send notification to supervisor on submission
- Allow cancellation only for pending requests

---

### User Story 6.4: Implement Leave Approval Workflow
**Title:** Create Supervisor Endpoints for Leave Approval

**As a** Supervisor  
**I want** to approve or deny leave requests  
**So that** team absences are managed

**Priority:** Medium  
**Story Points:** 8  
**Dependencies:** US 6.1, US 6.2, US 6.3, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Leave Approval Workflow
  As a Supervisor
  I want to approve leave requests
  So that absences are authorized

  Scenario: View pending requests for my team
    Given I am a supervisor for department "Warehouse"
    When I GET /api/v1/leave-requests/pending
    Then I should see pending requests from my team only

  Scenario: Approve leave request
    Given a pending leave request with ID "123"
    When I POST to /api/v1/leave-requests/123/approve
    Then the request status should change to APPROVED
    And the employee's leave balance should be updated
    And the employee should be notified
    And approvedBy should be set to my user ID

  Scenario: Deny leave request
    Given a pending leave request with ID "123"
    When I POST to /api/v1/leave-requests/123/deny with reason
    Then the request status should change to DENIED
    And the employee's balance should not be affected
    And the employee should be notified with reason

  Scenario: Cannot approve request with insufficient balance
    Given an employee has 0 days PTO available
    And they have a pending request for 5 days PTO
    When I try to approve the request
    Then I should receive a 400 Bad Request response
    And the error should indicate insufficient balance

  Scenario: Approved leave excludes from scheduling
    Given I approve a leave request for 2024-01-15
    When schedules are created for 2024-01-15
    Then the employee should not be available for assignment
```

**Notes:**
- Supervisors can only approve requests from their team
- Update leave balance on approval
- Send notifications to employee
- Integrate with scheduling to prevent conflicts

---

### User Story 6.5: Implement Leave Balance Accrual
**Title:** Create Automated Leave Accrual Processing

**As an** HR Administrator  
**I want** leave balances to accrue automatically  
**So that** employees receive their entitled time off

**Priority:** Medium  
**Story Points:** 8  
**Dependencies:** US 6.2  

**Acceptance Criteria:**
```gherkin
Feature: Leave Accrual Processing
  As an HR Administrator
  I want automatic leave accrual
  So that balances are updated regularly

  Scenario: Monthly accrual job runs
    Given it is the first day of the month
    When the accrual job executes
    Then all active employees should have accruals calculated
    And balances should be updated

  Scenario: Accrual based on policy
    Given an employee accrues 1.25 days PTO per month
    When the monthly accrual runs
    Then their PTO balance should increase by 1.25 days

  Scenario: Accrual respects maximum balance
    Given an employee has 28 days PTO
    And the maximum balance is 30 days
    When they would accrue 3 days
    Then only 2 days should be accrued
    And the balance should cap at 30 days

  Scenario: Carry over at year end
    Given an employee has 5 unused PTO days
    And the policy allows 5 days carry over
    When the year-end job runs
    Then 5 days should be added to next year's carryOver
    And current year balance should reset

  Scenario: Accrual audit trail
    Given accrual processing completes
    When I query accrual history
    Then I should see a record for each accrual event
    And it should include date, amount, and employee
```

**Notes:**
- Implement scheduled job for monthly accrual
- Support different accrual policies per leave type
- Enforce maximum balance caps
- Handle year-end carry over
- Maintain audit trail of all accruals

---

### User Story 6.6: Leave Balance Reporting
**Title:** Create Leave Balance and Usage Reports

**As an** HR Administrator  
**I want** to view leave balance reports  
**So that** I can monitor time-off usage

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 6.2, US 6.3  

**Acceptance Criteria:**
```gherkin
Feature: Leave Balance Reporting
  As an HR Administrator
  I want leave balance reports
  So that I can track time-off

  Scenario: View all employee balances
    Given multiple employees with leave balances
    When I GET /api/v1/leave-balances
    Then I should see balances for all employees
    And it should show accrued, used, and available for each type

  Scenario: Filter balances by department
    Given employees in multiple departments
    When I GET /api/v1/leave-balances?department=Warehouse
    Then I should see only Warehouse employee balances

  Scenario: Export leave balance report
    Given leave balance data exists
    When I GET /api/v1/leave-balances/export?format=csv
    Then I should receive a CSV file
    And it should include employee name, leave type, accrued, used, available

  Scenario: View leave usage trends
    Given historical leave data
    When I GET /api/v1/leave-requests/analytics
    Then I should see usage trends by month
    And breakdown by leave type
```

**Notes:**
- Support filtering by department, leave type, date range
- Provide CSV export functionality
- Include analytics endpoints for trends
- Show employees with low balances

---

## Epic E07: Training & Certification Tracking

### User Story 7.1: Create Certification Entity
**Title:** Implement Certification Domain Model

**As a** Backend Developer  
**I want** to create a Certification entity  
**So that** employee certifications can be tracked

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 2.1  

**Acceptance Criteria:**
```gherkin
Feature: Certification Entity Management
  As a Backend Developer
  I want to define certification structure
  So that certifications are tracked

  Scenario: Certification entity has required fields
    Given the Certification entity
    When I examine its fields
    Then it should have id (UUID)
    And it should have name (String)
    And it should have description (String)
    And it should have category (Enum: SAFETY, EQUIPMENT, COMPLIANCE, etc.)
    And it should have validityPeriodMonths (Integer)
    And it should have isRequired (Boolean)
    And it should have requiredForRoles (Set<String>)

  Scenario: Employee certification has required fields
    Given the EmployeeCertification entity
    When I examine its fields
    Then it should have id (UUID)
    And it should have employeeId (UUID)
    And it should have certificationId (UUID)
    And it should have issuedDate (LocalDate)
    And it should have expiryDate (LocalDate)
    And it should have status (Enum: ACTIVE, EXPIRED, REVOKED)
    And it should have documentUrl (String, nullable)
    And it should have issuingAuthority (String)

  Scenario: Expiry date is calculated
    Given a certification with 12 months validity
    When issued on 2024-01-01
    Then expiryDate should be 2025-01-01
```

**Notes:**
- Separate certification definition from employee certifications
- Calculate expiry date based on validity period
- Support document upload for proof
- Link certifications to roles/equipment

---

### User Story 7.2: Implement Certification CRUD API
**Title:** Create REST Endpoints for Certification Management

**As a** Safety Manager  
**I want** to manage certification definitions  
**So that** required certifications are defined

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 7.1, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Certification Management API
  As a Safety Manager
  I want to manage certifications
  So that requirements are defined

  Scenario: Create certification definition
    Given I am authenticated as ADMIN or HR
    When I POST to /api/v1/certifications with valid data
    Then I should receive a 201 Created response
    And the certification should be persisted

  Scenario: List all certifications
    Given multiple certifications exist
    When I GET /api/v1/certifications
    Then I should see all certification definitions
    And they should be grouped by category

  Scenario: Update certification
    Given a certification with ID "123" exists
    When I PUT /api/v1/certifications/123 with updated data
    Then the certification should be updated
    And existing employee certifications should not be affected

  Scenario: Mark certification as required
    Given a certification exists
    When I PATCH /api/v1/certifications/123 with {"isRequired": true}
    Then the certification should be marked as required
    And employees without it should be flagged
```

**Notes:**
- Require ADMIN or HR role
- Support categorization of certifications
- Link certifications to specific roles
- Prevent deletion if employees have the certification

---

### User Story 7.3: Implement Employee Certification Assignment
**Title:** Create Endpoints to Assign Certifications to Employees

**As an** HR Administrator  
**I want** to assign certifications to employees  
**So that** their qualifications are recorded

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 7.1, US 7.2, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Employee Certification Assignment
  As an HR Administrator
  I want to assign certifications
  So that employee qualifications are tracked

  Scenario: Assign certification to employee
    Given an employee with ID "123" exists
    And a certification "Forklift Operator" exists
    When I POST to /api/v1/employees/123/certifications with certification data
    Then the certification should be assigned
    And expiryDate should be calculated
    And status should be ACTIVE

  Scenario: Upload certification document
    Given I am assigning a certification
    When I include a document file in the request
    Then the document should be uploaded to secure storage
    And the documentUrl should be saved

  Scenario: View employee certifications
    Given an employee has multiple certifications
    When I GET /api/v1/employees/123/certifications
    Then I should see all their certifications
    And each should show status and expiry date

  Scenario: Renew expiring certification
    Given an employee has an expiring certification
    When I POST to /api/v1/employees/123/certifications/456/renew
    Then a new certification record should be created
    And the old one should be marked as superseded
    And the new expiryDate should be calculated from today
```

**Notes:**
- Support document upload for proof
- Calculate expiry date automatically
- Allow renewal of certifications
- Track certification history

---

### User Story 7.4: Implement Certification Expiry Alerts
**Title:** Create Automated Alerts for Expiring Certifications

**As a** Safety Manager  
**I want** to receive alerts for expiring certifications  
**So that** renewals can be scheduled proactively

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 7.1, US 7.3  

**Acceptance Criteria:**
```gherkin
Feature: Certification Expiry Alerts
  As a Safety Manager
  I want expiry alerts
  So that certifications are renewed on time

  Scenario: Alert 30 days before expiry
    Given a certification expires on 2024-02-01
    When the date is 2024-01-02 (30 days before)
    Then an alert should be generated
    And the employee should be notified
    And their supervisor should be notified
    And HR should be notified

  Scenario: Alert 7 days before expiry
    Given a certification expires on 2024-02-01
    When the date is 2024-01-25 (7 days before)
    Then a high-priority alert should be generated
    And notifications should be sent

  Scenario: Mark certification as expired
    Given a certification expires on 2024-02-01
    When the date is 2024-02-02
    Then the certification status should change to EXPIRED
    And the employee should be flagged

  Scenario: View expiring certifications report
    Given multiple certifications are expiring soon
    When I GET /api/v1/certifications/expiring?days=30
    Then I should see all certifications expiring in 30 days
    And they should be sorted by expiry date
```

**Notes:**
- Implement scheduled job to check expiries daily
- Send notifications at 30 and 7 days before expiry
- Auto-update status to EXPIRED after expiry date
- Provide dashboard of expiring certifications

---

### User Story 7.5: Integrate Certification with Scheduling
**Title:** Block Schedule Assignments for Expired Certifications

**As a** Supervisor  
**I want** the system to prevent assigning employees without valid certifications  
**So that** only qualified workers are assigned to equipment

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 5.4, US 7.1, US 7.3  

**Acceptance Criteria:**
```gherkin
Feature: Certification-Based Scheduling
  As a Supervisor
  I want certification validation in scheduling
  So that only qualified employees are assigned

  Scenario: Shift requires certification
    Given a shift requires "Forklift Operator" certification
    When I try to assign an employee without valid forklift cert
    Then the assignment should be rejected
    And the error should indicate missing certification

  Scenario: Employee has expired certification
    Given an employee has an EXPIRED "Forklift Operator" cert
    When I try to assign them to a forklift shift
    Then the assignment should be rejected
    And the error should indicate expired certification

  Scenario: Employee has valid certification
    Given an employee has an ACTIVE "Forklift Operator" cert
    When I assign them to a forklift shift
    Then the assignment should succeed

  Scenario: View certification status on employee profile
    Given I am viewing an employee's profile
    When I check their certifications section
    Then I should see all certifications with status
    And expired ones should be highlighted in red
    And expiring soon should be highlighted in yellow
```

**Notes:**
- Add certification requirements to shift templates
- Validate certifications during schedule assignment
- Show certification status in employee profiles
- Provide warnings for expiring certifications during assignment

---

## Epic E08: Safety Incidents & OSHA Reporting

### User Story 8.1: Create Safety Incident Entity
**Title:** Implement Safety Incident Domain Model

**As a** Backend Developer  
**I want** to create a SafetyIncident entity  
**So that** incidents can be recorded and tracked

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 2.1  

**Acceptance Criteria:**
```gherkin
Feature: Safety Incident Entity
  As a Backend Developer
  I want to define incident structure
  So that safety events are tracked

  Scenario: Safety incident has required fields
    Given the SafetyIncident entity
    When I examine its fields
    Then it should have id (UUID)
    And it should have incidentDate (ZonedDateTime)
    And it should have reportedDate (ZonedDateTime)
    And it should have reportedBy (UUID)
    And it should have incidentType (Enum: INJURY, NEAR_MISS, PROPERTY_DAMAGE, etc.)
    And it should have severity (Enum: LOW, MEDIUM, HIGH, CRITICAL)
    And it should have location (String)
    And it should have description (String)
    And it should have involvedEmployees (Set<UUID>)
    And it should have witnessEmployees (Set<UUID>)
    And it should have status (Enum: OPEN, INVESTIGATING, RESOLVED, CLOSED)
    And it should have rootCause (String, nullable)
    And it should have correctiveActions (String, nullable)
    And it should have closedDate (ZonedDateTime, nullable)

  Scenario: Incident links to employees
    Given an incident involves employee "123"
    When the incident is created
    Then employee "123" should be linked as involved

  Scenario: Incident tracks workflow status
    Given a new incident is reported
    When it is created
    Then status should be OPEN
    When investigation begins
    Then status should change to INVESTIGATING
```

**Notes:**
- Support multiple involved and witness employees
- Track complete workflow from report to closure
- Store location details for analysis
- Link to equipment if applicable

---

### User Story 8.2: Implement Incident Reporting API
**Title:** Create REST Endpoints for Incident Reporting

**As a** Warehouse Worker  
**I want** to report safety incidents  
**So that** they are documented and investigated

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 8.1, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Incident Reporting API
  As a Warehouse Worker
  I want to report incidents
  So that safety issues are addressed

  Scenario: Report new incident
    Given I am an authenticated employee
    When I POST to /api/v1/safety/incidents with incident details
    Then I should receive a 201 Created response
    And the incident should be in OPEN status
    And safety manager should be notified
    And an incident number should be generated

  Scenario: Report near-miss
    Given I witnessed a near-miss event
    When I report it with type NEAR_MISS
    Then the incident should be recorded
    And it should be flagged for review

  Scenario: Include multiple involved employees
    Given an incident involves 3 employees
    When I report the incident
    Then all 3 employees should be linked
    And they should be notified

  Scenario: Attach photos or documents
    Given I am reporting an incident
    When I upload photos of the scene
    Then the photos should be stored securely
    And linked to the incident record

  Scenario: View my reported incidents
    Given I have reported incidents
    When I GET /api/v1/safety/incidents/me
    Then I should see all incidents I reported
    And their current status
```

**Notes:**
- All authenticated users can report incidents
- Generate unique incident numbers
- Support file attachments
- Send immediate notifications to safety team

---

### User Story 8.3: Implement Incident Investigation Workflow
**Title:** Create Workflow for Incident Investigation

**As a** Safety Manager  
**I want** to manage incident investigations  
**So that** root causes are identified and addressed

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 8.1, US 8.2, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Incident Investigation Workflow
  As a Safety Manager
  I want to investigate incidents
  So that corrective actions are taken

  Scenario: Start investigation
    Given an incident in OPEN status
    When I POST to /api/v1/safety/incidents/123/investigate
    Then the status should change to INVESTIGATING
    And I should be assigned as investigator

  Scenario: Add investigation notes
    Given I am investigating an incident
    When I PATCH /api/v1/safety/incidents/123 with notes
    Then the notes should be added to the incident
    And a timestamp should be recorded

  Scenario: Identify root cause
    Given I have completed investigation
    When I update the incident with root cause
    Then the rootCause field should be populated

  Scenario: Define corrective actions
    Given root cause is identified
    When I add corrective actions
    Then the actions should be recorded
    And tasks should be created for implementation

  Scenario: Close incident
    Given corrective actions are completed
    When I POST to /api/v1/safety/incidents/123/close
    Then the status should change to CLOSED
    And closedDate should be set
    And involved employees should be notified
```

**Notes:**
- Require ADMIN or SUPERVISOR role for investigation
- Track investigation timeline
- Link corrective actions to tasks
- Maintain complete audit trail

---

### User Story 8.4: Implement OSHA Reporting
**Title:** Generate OSHA 300/300A Reports

**As a** Safety Manager  
**I want** to generate OSHA-compliant reports  
**So that** regulatory requirements are met

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 8.1, US 8.2  

**Acceptance Criteria:**
```gherkin
Feature: OSHA Reporting
  As a Safety Manager
  I want to generate OSHA reports
  So that compliance is maintained

  Scenario: Generate OSHA 300 log
    Given incidents exist for the year 2024
    When I GET /api/v1/safety/reports/osha-300?year=2024
    Then I should receive a report with all recordable incidents
    And it should include case number, employee name, job title
    And incident date, location, description, classification

  Scenario: Generate OSHA 300A summary
    Given the OSHA 300 log for 2024
    When I GET /api/v1/safety/reports/osha-300a?year=2024
    Then I should receive a summary report
    And it should include total cases, days away, job transfer/restriction
    And injury/illness types breakdown

  Scenario: Filter recordable incidents
    Given multiple incidents exist
    When generating OSHA reports
    Then only OSHA-recordable incidents should be included
    And near-misses should be excluded

  Scenario: Export OSHA report as PDF
    Given OSHA report data
    When I request format=pdf
    Then I should receive a PDF file
    And it should match OSHA form layout
```

**Notes:**
- Implement OSHA recordability criteria
- Support both OSHA 300 and 300A formats
- Provide PDF export matching official forms
- Calculate required statistics (DART rate, etc.)

---

### User Story 8.5: Safety Metrics Dashboard
**Title:** Create Safety KPI and Metrics Endpoints

**As a** Safety Manager  
**I want** to view safety metrics and KPIs  
**So that** I can monitor safety performance

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 8.1, US 8.2  

**Acceptance Criteria:**
```gherkin
Feature: Safety Metrics Dashboard
  As a Safety Manager
  I want to see safety metrics
  So that I can track performance

  Scenario: View incident count by type
    Given incidents exist
    When I GET /api/v1/safety/metrics/by-type
    Then I should see counts for each incident type
    And percentages of total

  Scenario: View incident trend over time
    Given historical incident data
    When I GET /api/v1/safety/metrics/trend?period=monthly
    Then I should see incident counts by month
    And trend direction (increasing/decreasing)

  Scenario: Calculate incident rate
    Given total incidents and total hours worked
    When I GET /api/v1/safety/metrics/incident-rate
    Then I should see incidents per 100 employees
    Or incidents per 200,000 hours worked

  Scenario: View incidents by location
    Given incidents across multiple locations
    When I GET /api/v1/safety/metrics/by-location
    Then I should see incident counts per location
    And identify high-risk areas

  Scenario: View days since last incident
    Given the most recent incident date
    When I GET /api/v1/safety/metrics/days-since-incident
    Then I should see number of days since last recordable incident
```

**Notes:**
- Calculate standard safety KPIs (TRIR, DART, etc.)
- Support filtering by date range, department, severity
- Provide trend analysis
- Identify high-risk locations and times

---

## Epic E09: Equipment & Asset Assignment

### User Story 9.1: Create Asset Entity
**Title:** Implement Asset and Equipment Domain Model

**As a** Backend Developer  
**I want** to create an Asset entity  
**So that** equipment can be tracked and assigned

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 2.1  

**Acceptance Criteria:**
```gherkin
Feature: Asset Entity Management
  As a Backend Developer
  I want to define asset structure
  So that equipment is tracked

  Scenario: Asset entity has required fields
    Given the Asset entity
    When I examine its fields
    Then it should have id (UUID)
    And it should have assetTag (String, unique)
    And it should have assetType (Enum: SCANNER, FORKLIFT, PPE, TOOL, etc.)
    And it should have name (String)
    And it should have description (String)
    And it should have serialNumber (String, nullable)
    And it should have status (Enum: AVAILABLE, ASSIGNED, MAINTENANCE, RETIRED)
    And it should have condition (Enum: EXCELLENT, GOOD, FAIR, POOR)
    And it should have requiredCertification (UUID, nullable)
    And it should have location (String)
    And it should have purchaseDate (LocalDate)
    And it should have warrantyExpiry (LocalDate, nullable)

  Scenario: Asset assignment has required fields
    Given the AssetAssignment entity
    When I examine its fields
    Then it should have id (UUID)
    And it should have assetId (UUID)
    And it should have employeeId (UUID)
    And it should have checkOutDate (ZonedDateTime)
    And it should have expectedReturnDate (LocalDate, nullable)
    And it should have actualReturnDate (ZonedDateTime, nullable)
    And it should have checkOutCondition (Enum)
    And it should have returnCondition (Enum, nullable)
    And it should have notes (String, nullable)
```

**Notes:**
- Unique asset tags for identification
- Link assets to required certifications
- Track asset condition over time
- Support various asset types

---

### User Story 9.2: Implement Asset Registry CRUD
**Title:** Create REST Endpoints for Asset Management

**As a** Warehouse Manager  
**I want** to manage the asset registry  
**So that** all equipment is cataloged

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 9.1, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Asset Registry Management
  As a Warehouse Manager
  I want to manage assets
  So that equipment is tracked

  Scenario: Add new asset
    Given I am authenticated as ADMIN or SUPERVISOR
    When I POST to /api/v1/assets with asset details
    Then I should receive a 201 Created response
    And the asset should be in AVAILABLE status

  Scenario: List all assets
    Given multiple assets exist
    When I GET /api/v1/assets
    Then I should see all assets
    And they should be filterable by type and status

  Scenario: Update asset details
    Given an asset with ID "123" exists
    When I PUT /api/v1/assets/123 with updated data
    Then the asset should be updated

  Scenario: Mark asset for maintenance
    Given an asset in ASSIGNED status
    When I PATCH /api/v1/assets/123 with {"status": "MAINTENANCE"}
    Then the asset status should change to MAINTENANCE
    And it should not be available for assignment

  Scenario: Retire asset
    Given an asset that is no longer usable
    When I PATCH /api/v1/assets/123 with {"status": "RETIRED"}
    Then the asset should be marked as RETIRED
    And it should not appear in active asset lists
```

**Notes:**
- Require ADMIN or SUPERVISOR role
- Support filtering by type, status, location
- Track asset lifecycle from purchase to retirement
- Prevent deletion of assets with assignment history

---

### User Story 9.3: Implement Asset Check-Out
**Title:** Create Endpoint for Asset Assignment to Employees

**As a** Supervisor  
**I want** to check out assets to employees  
**So that** equipment usage is tracked

**Priority:** Medium  
**Story Points:** 8  
**Dependencies:** US 9.1, US 9.2, US 7.1, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Asset Check-Out
  As a Supervisor
  I want to assign assets to employees
  So that equipment is tracked

  Scenario: Check out asset to employee
    Given an asset with ID "123" is AVAILABLE
    And employee "456" exists
    When I POST to /api/v1/assets/123/checkout with employee ID
    Then an assignment record should be created
    And the asset status should change to ASSIGNED
    And checkOutDate should be set to now

  Scenario: Cannot check out unavailable asset
    Given an asset is already ASSIGNED
    When I try to check it out to another employee
    Then I should receive a 400 Bad Request response
    And the error should indicate asset is not available

  Scenario: Validate certification for equipment
    Given an asset requires "Forklift Operator" certification
    And employee "456" does not have valid forklift cert
    When I try to check out the asset to employee "456"
    Then the checkout should be rejected
    And the error should indicate missing certification

  Scenario: Record asset condition at checkout
    Given I am checking out an asset
    When I specify condition as GOOD
    Then the checkOutCondition should be recorded

  Scenario: Set expected return date
    Given I am checking out an asset
    When I specify expectedReturnDate as 2024-01-31
    Then the expected return date should be recorded
```

**Notes:**
- Validate employee has required certification
- Record condition at checkout
- Support optional expected return date
- Update asset status to ASSIGNED

---

### User Story 9.4: Implement Asset Check-In
**Title:** Create Endpoint for Asset Return

**As a** Supervisor  
**I want** to check in returned assets  
**So that** they become available for reassignment

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 9.1, US 9.3  

**Acceptance Criteria:**
```gherkin
Feature: Asset Check-In
  As a Supervisor
  I want to check in returned assets
  So that they are available again

  Scenario: Check in asset
    Given an asset is ASSIGNED to employee "456"
    When I POST to /api/v1/assets/123/checkin
    Then the assignment actualReturnDate should be set
    And the asset status should change to AVAILABLE

  Scenario: Record return condition
    Given I am checking in an asset
    When I specify returnCondition as FAIR
    Then the returnCondition should be recorded
    And if condition degraded, a maintenance flag should be set

  Scenario: Identify overdue returns
    Given an asset was expected back on 2024-01-31
    And it is returned on 2024-02-05
    When I check it in
    Then it should be flagged as overdue
    And the delay should be recorded

  Scenario: Asset requires maintenance after return
    Given an asset is returned in POOR condition
    When I check it in
    Then the asset status should change to MAINTENANCE
    And it should not be available for checkout
```

**Notes:**
- Update assignment record with return date
- Record return condition
- Flag overdue returns
- Auto-set to MAINTENANCE if condition is poor

---

### User Story 9.5: Asset Assignment History
**Title:** Create Endpoints to View Asset and Employee History

**As a** Warehouse Manager  
**I want** to view asset assignment history  
**So that** I can track equipment usage

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 9.1, US 9.3, US 9.4  

**Acceptance Criteria:**
```gherkin
Feature: Asset Assignment History
  As a Warehouse Manager
  I want to see assignment history
  So that I can track usage

  Scenario: View asset assignment history
    Given an asset has been assigned multiple times
    When I GET /api/v1/assets/123/history
    Then I should see all past assignments
    And each should show employee, checkout date, return date, condition

  Scenario: View employee asset history
    Given an employee has been assigned multiple assets
    When I GET /api/v1/employees/456/assets/history
    Then I should see all assets they've been assigned
    And current assignments should be highlighted

  Scenario: View currently assigned assets
    Given multiple assets are currently assigned
    When I GET /api/v1/assets/assignments/current
    Then I should see all active assignments
    And overdue returns should be highlighted

  Scenario: Generate asset utilization report
    Given historical assignment data
    When I GET /api/v1/assets/reports/utilization
    Then I should see utilization percentage per asset
    And identify underutilized equipment
```

**Notes:**
- Maintain complete assignment history
- Support filtering by date range, employee, asset type
- Calculate utilization metrics
- Identify overdue returns

---

## Epic E10: Performance Reviews & Goals

### User Story 10.1: Create Performance Review Entity
**Title:** Implement Performance Review Domain Model

**As a** Backend Developer  
**I want** to create a PerformanceReview entity  
**So that** employee evaluations can be tracked

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 2.1  

**Acceptance Criteria:**
```gherkin
Feature: Performance Review Entity
  As a Backend Developer
  I want to define review structure
  So that evaluations are tracked

  Scenario: Performance review has required fields
    Given the PerformanceReview entity
    When I examine its fields
    Then it should have id (UUID)
    And it should have employeeId (UUID)
    And it should have reviewerId (UUID)
    And it should have reviewPeriodStart (LocalDate)
    And it should have reviewPeriodEnd (LocalDate)
    And it should have reviewType (Enum: QUARTERLY, ANNUAL, PROBATION)
    And it should have status (Enum: DRAFT, SUBMITTED, ACKNOWLEDGED, FINALIZED)
    And it should have overallRating (Enum: EXCEEDS, MEETS, NEEDS_IMPROVEMENT, UNSATISFACTORY)
    And it should have reviewDate (LocalDate)
    And it should have acknowledgedDate (LocalDate, nullable)

  Scenario: Review includes competency ratings
    Given a performance review
    When I examine competency ratings
    Then it should have multiple ReviewCompetency records
    And each should have competencyName, rating, comments

  Scenario: Review includes goals
    Given a performance review
    When I examine goals
    Then it should have multiple ReviewGoal records
    And each should have description, status, achievement percentage
```

**Notes:**
- Support multiple review types and cycles
- Include competency-based ratings
- Track goals and achievements
- Maintain workflow status

---

### User Story 10.2: Create Review Template Management
**Title:** Implement Review Template Configuration

**As an** HR Administrator  
**I want** to create review templates  
**So that** evaluations are standardized

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 10.1, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Review Template Management
  As an HR Administrator
  I want to manage review templates
  So that evaluations are consistent

  Scenario: Create review template
    Given I am authenticated as ADMIN or HR
    When I POST to /api/v1/review-templates with template data
    Then I should receive a 201 Created response
    And the template should include competencies to evaluate

  Scenario: Define competencies in template
    Given I am creating a review template
    When I add competencies like "Teamwork", "Quality", "Safety"
    Then each competency should be included in the template
    And have a weight/importance factor

  Scenario: Template for different roles
    Given different roles have different requirements
    When I create templates
    Then I should be able to create role-specific templates
    And assign them to employee roles

  Scenario: List available templates
    Given multiple review templates exist
    When I GET /api/v1/review-templates
    Then I should see all templates
    And they should be filterable by review type
```

**Notes:**
- Support customizable competencies
- Allow role-specific templates
- Include rating scales and descriptions
- Version templates for historical consistency

---

### User Story 10.3: Implement Review Creation and Submission
**Title:** Create Endpoints for Performance Review Workflow

**As a** Supervisor  
**I want** to create and submit performance reviews  
**So that** employee performance is evaluated

**Priority:** Medium  
**Story Points:** 8  
**Dependencies:** US 10.1, US 10.2, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Performance Review Workflow
  As a Supervisor
  I want to conduct performance reviews
  So that employees receive feedback

  Scenario: Create review from template
    Given a review template exists
    And I supervise employee "123"
    When I POST to /api/v1/performance-reviews with employee and template
    Then a review should be created in DRAFT status
    And it should include all template competencies

  Scenario: Rate competencies
    Given I have a draft review
    When I PATCH /api/v1/performance-reviews/456/competencies
    Then I should be able to rate each competency
    And add comments for each

  Scenario: Set goals for next period
    Given I am completing a review
    When I add goals for the next review period
    Then the goals should be saved with the review
    And they should be trackable

  Scenario: Submit review
    Given I have completed all ratings
    When I POST to /api/v1/performance-reviews/456/submit
    Then the status should change to SUBMITTED
    And the employee should be notified
    And the review should be locked from editing

  Scenario: Calculate overall rating
    Given all competencies are rated
    When I submit the review
    Then an overall rating should be calculated
    Based on weighted average of competencies
```

**Notes:**
- Require all competencies to be rated before submission
- Calculate overall rating from competency ratings
- Lock review after submission
- Send notification to employee

---

### User Story 10.4: Implement Employee Acknowledgement
**Title:** Create Endpoint for Employee Review Acknowledgement

**As a** Warehouse Worker  
**I want** to view and acknowledge my performance review  
**So that** I confirm receipt of feedback

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 10.1, US 10.3, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Review Acknowledgement
  As a Warehouse Worker
  I want to acknowledge my review
  So that I confirm I've read it

  Scenario: View my submitted review
    Given a review has been submitted for me
    When I GET /api/v1/performance-reviews/me/pending
    Then I should see the review details
    And all ratings and comments

  Scenario: Acknowledge review
    Given I have reviewed my performance review
    When I POST to /api/v1/performance-reviews/456/acknowledge
    Then the status should change to ACKNOWLEDGED
    And acknowledgedDate should be set
    And my supervisor should be notified

  Scenario: Add employee comments
    Given I am acknowledging a review
    When I include my comments
    Then my comments should be saved with the review
    And visible to my supervisor and HR

  Scenario: Cannot modify review
    Given a review is submitted
    When I try to change ratings
    Then the request should be rejected
    And I should only be able to add comments
```

**Notes:**
- Employees can only view their own reviews
- Allow employee comments but not rating changes
- Track acknowledgement date
- Notify supervisor when acknowledged

---

### User Story 10.5: Review History and Reporting
**Title:** Create Endpoints for Review History and Analytics

**As an** HR Administrator  
**I want** to view review history and analytics  
**So that** I can track performance trends

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 10.1, US 10.3, US 10.4  

**Acceptance Criteria:**
```gherkin
Feature: Review History and Analytics
  As an HR Administrator
  I want to analyze review data
  So that I can identify trends

  Scenario: View employee review history
    Given an employee has multiple reviews
    When I GET /api/v1/employees/123/performance-reviews
    Then I should see all their reviews
    And they should be sorted by date descending

  Scenario: Export review as PDF
    Given a finalized review exists
    When I GET /api/v1/performance-reviews/456/export?format=pdf
    Then I should receive a PDF document
    And it should include all ratings, comments, and signatures

  Scenario: View review completion status
    Given multiple reviews are in progress
    When I GET /api/v1/performance-reviews/status
    Then I should see completion status by department
    And identify overdue reviews

  Scenario: Performance analytics
    Given historical review data
    When I GET /api/v1/performance-reviews/analytics
    Then I should see average ratings by department
    And trends over time
    And identify high/low performers
```

**Notes:**
- Maintain immutable history after finalization
- Support PDF export with professional formatting
- Provide analytics and trend analysis
- Track review completion rates

---

## Epic E11: Payroll Export Integration

### User Story 11.1: Design Payroll Export Schema
**Title:** Define Payroll Data Export Structure

**As a** Backend Developer  
**I want** to design the payroll export data schema  
**So that** attendance data can be formatted for payroll systems

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 4.1, US 6.1  

**Acceptance Criteria:**
```gherkin
Feature: Payroll Export Schema
  As a Backend Developer
  I want to define export format
  So that payroll data is structured correctly

  Scenario: Export schema includes required fields
    Given the payroll export schema
    When I examine the structure
    Then it should include employeeId
    And employee name
    And pay period start and end dates
    And regular hours worked
    And overtime hours worked
    And leave hours (paid/unpaid)
    And department/cost center

  Scenario: Support multiple payroll provider formats
    Given different payroll providers have different formats
    When I configure export format
    Then I should be able to select provider (ADP, Paychex, etc.)
    And the export should match that provider's schema

  Scenario: Export includes only approved data
    Given attendance and leave data
    When generating payroll export
    Then only approved attendance should be included
    And only approved leave should be included
    And pending corrections should be excluded
```

**Notes:**
- Support configurable field mapping
- Include multiple payroll provider templates
- Validate data completeness before export
- Support both CSV and fixed-width formats

---

### User Story 11.2: Implement Payroll Data Aggregation
**Title:** Create Service to Aggregate Payroll Data

**As a** Backend Developer  
**I want** to aggregate attendance and leave data for payroll  
**So that** hours are calculated correctly

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 4.1, US 4.5, US 6.1, US 11.1  

**Acceptance Criteria:**
```gherkin
Feature: Payroll Data Aggregation
  As a Backend Developer
  I want to aggregate payroll data
  So that totals are accurate

  Scenario: Calculate regular hours
    Given attendance records for a pay period
    When I aggregate hours
    Then regular hours should be summed per employee
    And capped at standard hours per week

  Scenario: Calculate overtime hours
    Given an employee worked 45 hours in a week
    And standard hours are 40 per week
    When I calculate overtime
    Then overtime hours should be 5

  Scenario: Include approved leave
    Given an employee has 8 hours approved PTO
    When I aggregate payroll data
    Then PTO hours should be included
    And categorized as paid leave

  Scenario: Exclude unpaid leave from paid hours
    Given an employee has unpaid leave
    When I aggregate payroll data
    Then unpaid leave should be tracked separately
    And not included in paid hours

  Scenario: Reconcile totals
    Given aggregated payroll data
    When I validate totals
    Then total hours should match attendance records
    And leave hours should match approved requests
```

**Notes:**
- Calculate regular vs overtime based on rules
- Include all approved leave types
- Separate paid and unpaid leave
- Validate data consistency

---

### User Story 11.3: Implement Payroll Export Generation
**Title:** Create Endpoint to Generate Payroll Export Files

**As a** Payroll Administrator  
**I want** to generate payroll export files  
**So that** data can be imported into payroll system

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 11.1, US 11.2, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Payroll Export Generation
  As a Payroll Administrator
  I want to generate payroll exports
  So that payroll can be processed

  Scenario: Generate export for pay period
    Given I specify pay period 2024-01-01 to 2024-01-15
    When I POST to /api/v1/payroll/export
    Then I should receive a file with all employee hours
    And it should be in the configured format

  Scenario: Export matches provider schema
    Given payroll provider is configured as "ADP"
    When I generate an export
    Then the file should match ADP's import format
    And all required fields should be populated

  Scenario: Validate data before export
    Given payroll data for a period
    When I generate an export
    Then the system should validate completeness
    And flag any missing or invalid data
    And prevent export if validation fails

  Scenario: Export includes all active employees
    Given 100 active employees
    When I generate an export
    Then all 100 employees should be included
    Even if they have zero hours

  Scenario: Preview export before generation
    Given I am preparing an export
    When I GET /api/v1/payroll/export/preview
    Then I should see a summary of data to be exported
    And totals per employee
```

**Notes:**
- Require ADMIN or HR role
- Validate data completeness
- Support preview before final export
- Generate export in configured format

---

### User Story 11.4: Implement Secure Export Delivery
**Title:** Create SFTP/API Delivery for Payroll Exports

**As a** Payroll Administrator  
**I want** payroll exports delivered securely  
**So that** data reaches the payroll system safely

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 11.3  

**Acceptance Criteria:**
```gherkin
Feature: Secure Export Delivery
  As a Payroll Administrator
  I want secure export delivery
  So that data is transmitted safely

  Scenario: Deliver via SFTP
    Given SFTP credentials are configured
    When I generate a payroll export
    Then the file should be uploaded to SFTP server
    And placed in the configured directory

  Scenario: Deliver via API
    Given payroll provider API is configured
    When I generate a payroll export
    Then the data should be sent via API
    And I should receive a confirmation response

  Scenario: Retry failed deliveries
    Given an export delivery fails
    When the failure is detected
    Then the system should retry with exponential backoff
    And alert administrators after max retries

  Scenario: Encrypt sensitive data
    Given a payroll export contains PII
    When the file is transmitted
    Then it should be encrypted in transit
    And use secure protocols (SFTP/HTTPS)

  Scenario: Delivery confirmation
    Given an export is successfully delivered
    When I check delivery status
    Then I should see confirmation timestamp
    And delivery method used
```

**Notes:**
- Support both SFTP and API delivery
- Implement retry logic with backoff
- Encrypt data in transit
- Log all delivery attempts

---

### User Story 11.5: Payroll Export Audit and Reconciliation
**Title:** Create Audit Trail for Payroll Exports

**As a** Payroll Administrator  
**I want** to audit payroll exports  
**So that** I can verify data accuracy

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 11.3, US 11.4  

**Acceptance Criteria:**
```gherkin
Feature: Payroll Export Audit
  As a Payroll Administrator
  I want to audit exports
  So that I can verify accuracy

  Scenario: Log every export
    Given a payroll export is generated
    When the export completes
    Then an audit record should be created
    And it should include timestamp, user, pay period, record count

  Scenario: View export history
    Given multiple exports have been generated
    When I GET /api/v1/payroll/exports/history
    Then I should see all past exports
    And their delivery status

  Scenario: Reconcile export with source data
    Given an export was generated
    When I request reconciliation report
    Then I should see totals from export
    And totals from source attendance/leave data
    And any discrepancies should be highlighted

  Scenario: Re-generate previous export
    Given an export was generated for period 2024-01-01 to 2024-01-15
    When I request re-generation
    Then the same data should be exported again
    And it should match the original export

  Scenario: Export audit is immutable
    Given an export audit record exists
    When I try to modify it
    Then the modification should be rejected
    And the original record should remain unchanged
```

**Notes:**
- Create immutable audit records
- Support reconciliation reports
- Allow re-generation of historical exports
- Track all delivery attempts and outcomes

---

## Epic E12: Notifications & Announcements

### User Story 12.1: Create Notification Entity
**Title:** Implement Notification Domain Model

**As a** Backend Developer  
**I want** to create a Notification entity  
**So that** user notifications can be managed

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 2.1  

**Acceptance Criteria:**
```gherkin
Feature: Notification Entity
  As a Backend Developer
  I want to define notification structure
  So that notifications are tracked

  Scenario: Notification entity has required fields
    Given the Notification entity
    When I examine its fields
    Then it should have id (UUID)
    And it should have recipientId (UUID)
    And it should have type (Enum: SHIFT_CHANGE, CERT_EXPIRY, APPROVAL, ANNOUNCEMENT, etc.)
    And it should have channel (Enum: IN_APP, EMAIL, SMS)
    And it should have title (String)
    And it should have message (String)
    And it should have priority (Enum: LOW, MEDIUM, HIGH, URGENT)
    And it should have status (Enum: PENDING, SENT, DELIVERED, READ, FAILED)
    And it should have createdAt (ZonedDateTime)
    And it should have sentAt (ZonedDateTime, nullable)
    And it should have readAt (ZonedDateTime, nullable)
    And it should have relatedEntityType (String, nullable)
    And it should have relatedEntityId (UUID, nullable)

  Scenario: Notification links to related entity
    Given a notification about a shift change
    When the notification is created
    Then it should link to the schedule entity
    And users can navigate to the related item
```

**Notes:**
- Support multiple notification channels
- Track delivery and read status
- Link notifications to source entities
- Support priority levels

---

### User Story 12.2: Implement Notification Preferences
**Title:** Create User Notification Preference Management

**As a** Warehouse Worker  
**I want** to manage my notification preferences  
**So that** I control how I receive notifications

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 12.1, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Notification Preferences
  As a Warehouse Worker
  I want to set notification preferences
  So that I receive notifications my way

  Scenario: View my notification preferences
    Given I am an authenticated user
    When I GET /api/v1/notifications/preferences
    Then I should see my preferences for each notification type
    And available channels for each

  Scenario: Opt-in to email notifications
    Given I want to receive shift change notifications via email
    When I PATCH /api/v1/notifications/preferences
    Then email should be enabled for shift changes

  Scenario: Opt-out of SMS notifications
    Given I don't want SMS notifications
    When I disable SMS channel
    Then I should not receive SMS notifications
    But other channels should remain active

  Scenario: Set quiet hours
    Given I don't want notifications at night
    When I set quiet hours from 22:00 to 06:00
    Then non-urgent notifications should be delayed
    Until after quiet hours

  Scenario: Urgent notifications bypass quiet hours
    Given I have quiet hours configured
    When an urgent notification is sent
    Then it should be delivered immediately
    Regardless of quiet hours
```

**Notes:**
- Store preferences per user per notification type
- Support opt-in/opt-out per channel
- Implement quiet hours with timezone awareness
- Urgent notifications bypass quiet hours

---

### User Story 12.3: Implement Notification Service
**Title:** Create Service to Send Notifications

**As a** Backend Developer  
**I want** to implement a notification service  
**So that** notifications can be sent through multiple channels

**Priority:** Medium  
**Story Points:** 8  
**Dependencies:** US 12.1, US 12.2  

**Acceptance Criteria:**
```gherkin
Feature: Notification Service
  As a Backend Developer
  I want to send notifications
  So that users are informed

  Scenario: Send in-app notification
    Given a notification is created
    When the channel is IN_APP
    Then the notification should be stored in database
    And marked as SENT

  Scenario: Send email notification
    Given a notification is created
    And the user has email enabled
    When the channel is EMAIL
    Then an email should be sent
    And delivery status should be tracked

  Scenario: Send SMS notification
    Given a notification is created
    And the user has SMS enabled
    When the channel is SMS
    Then an SMS should be sent via provider
    And delivery status should be tracked

  Scenario: Respect user preferences
    Given a user has disabled email for shift changes
    When a shift change notification is sent
    Then email should not be sent
    But other enabled channels should be used

  Scenario: Apply rate limiting
    Given a user receives multiple notifications
    When notifications are sent
    Then rate limits should be applied
    And excessive notifications should be batched

  Scenario: Handle delivery failures
    Given an email delivery fails
    When the failure is detected
    Then the status should be marked as FAILED
    And a retry should be scheduled
```

**Notes:**
- Implement async notification sending
- Integrate with email provider (SendGrid, SES, etc.)
- Integrate with SMS provider (Twilio, etc.)
- Apply rate limiting to prevent spam
- Handle delivery failures gracefully

---

### User Story 12.4: Implement Notification API
**Title:** Create REST Endpoints for Notification Management

**As a** Warehouse Worker  
**I want** to view and manage my notifications  
**So that** I stay informed

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 12.1, US 12.3, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Notification Management API
  As a Warehouse Worker
  I want to manage notifications
  So that I stay informed

  Scenario: View my notifications
    Given I have unread notifications
    When I GET /api/v1/notifications/me
    Then I should see all my notifications
    And unread ones should be highlighted

  Scenario: Mark notification as read
    Given I have an unread notification
    When I POST to /api/v1/notifications/123/read
    Then the notification should be marked as read
    And readAt timestamp should be set

  Scenario: Mark all as read
    Given I have multiple unread notifications
    When I POST to /api/v1/notifications/read-all
    Then all my notifications should be marked as read

  Scenario: Delete notification
    Given I have a notification
    When I DELETE /api/v1/notifications/123
    Then the notification should be removed from my list

  Scenario: Get unread count
    Given I have 5 unread notifications
    When I GET /api/v1/notifications/unread-count
    Then I should receive count of 5
```

**Notes:**
- Users can only access their own notifications
- Support pagination for notification list
- Provide unread count for badge display
- Allow bulk operations (mark all read, delete all)

---

### User Story 12.5: Implement Announcements
**Title:** Create System-Wide Announcement Feature

**As an** Administrator  
**I want** to create system-wide announcements  
**So that** important information reaches all users

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 12.1, US 12.3, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: System Announcements
  As an Administrator
  I want to create announcements
  So that users are informed

  Scenario: Create announcement
    Given I am authenticated as ADMIN
    When I POST to /api/v1/announcements with title and message
    Then an announcement should be created
    And all users should be notified

  Scenario: Target announcement to department
    Given I am creating an announcement
    When I specify department "Warehouse"
    Then only Warehouse employees should receive it

  Scenario: Schedule announcement
    Given I am creating an announcement
    When I set publishDate to 2024-01-15
    Then the announcement should not be visible until that date

  Scenario: View active announcements
    Given multiple announcements exist
    When I GET /api/v1/announcements
    Then I should see all active announcements
    And they should be sorted by date descending

  Scenario: Expire announcement
    Given an announcement with expiry date 2024-01-31
    When the date is 2024-02-01
    Then the announcement should no longer be visible

  Scenario: Pin important announcement
    Given an important announcement
    When I mark it as pinned
    Then it should appear at the top of the list
    Until unpinned or expired
```

**Notes:**
- Require ADMIN role for creating announcements
- Support targeting by department, role, or all users
- Allow scheduling and expiry dates
- Support pinning important announcements
- Display on dashboard/home page

---

## Epic E13: Integration Layer (HRIS/WMS APIs)

### User Story 13.1: Design Integration API
**Title:** Define REST API for External System Integration

**As a** Backend Developer  
**I want** to design integration APIs  
**So that** external systems can sync data

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 1.1, US 2.1  

**Acceptance Criteria:**
```gherkin
Feature: Integration API Design
  As a Backend Developer
  I want to define integration endpoints
  So that external systems can integrate

  Scenario: API follows REST standards
    Given the integration API
    When I examine the endpoints
    Then they should follow RESTful conventions
    And use standard HTTP methods
    And return appropriate status codes

  Scenario: API is versioned
    Given the integration API
    When I check the URL structure
    Then it should include version (e.g., /api/v1/integration/)
    And support multiple versions concurrently

  Scenario: API is documented
    Given the integration API
    When I access /v3/api-docs
    Then all integration endpoints should be documented
    And include request/response examples
    And authentication requirements

  Scenario: API supports pagination
    Given endpoints that return collections
    When I query them
    Then they should support pagination
    And return page metadata
```

**Notes:**
- Follow OpenAPI 3.0 specification
- Version all integration endpoints
- Provide comprehensive documentation
- Support standard pagination and filtering

---

### User Story 13.2: Implement OAuth2/JWT Security
**Title:** Secure Integration APIs with OAuth2 or JWT

**As a** Security Engineer  
**I want** to secure integration APIs  
**So that** only authorized systems can access data

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 3.1, US 13.1  

**Acceptance Criteria:**
```gherkin
Feature: Integration API Security
  As a Security Engineer
  I want secure API authentication
  So that integrations are protected

  Scenario: Support OAuth2 authentication
    Given OAuth2 is configured
    When an external system requests access
    Then they should obtain an access token
    And use it for API requests

  Scenario: Support JWT authentication
    Given JWT is configured
    When an external system has a JWT
    Then they should include it in Authorization header
    And the API should validate the token

  Scenario: Validate token expiry
    Given an expired access token
    When it is used for API request
    Then the request should be rejected with 401
    And indicate token is expired

  Scenario: Support API key authentication
    Given an API key is issued
    When it is included in X-API-Key header
    Then the request should be authenticated

  Scenario: Rate limit API requests
    Given an external system makes requests
    When they exceed rate limit
    Then requests should be throttled
    And return 429 Too Many Requests
```

**Notes:**
- Support multiple authentication methods
- Implement token validation and refresh
- Apply rate limiting per client
- Log all API access attempts

---

### User Story 13.3: Implement HRIS Sync Endpoints
**Title:** Create Endpoints for HRIS Data Synchronization

**As an** Integration Developer  
**I want** to sync employee data from HRIS  
**So that** employee records are up-to-date

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 2.1, US 13.1, US 13.2  

**Acceptance Criteria:**
```gherkin
Feature: HRIS Integration
  As an Integration Developer
  I want to sync HRIS data
  So that employee records are current

  Scenario: Receive new hire from HRIS
    Given HRIS sends new employee data
    When I POST to /api/v1/integration/hris/employees
    Then a new employee should be created
    And initial onboarding tasks should be triggered

  Scenario: Update existing employee
    Given HRIS sends updated employee data
    When I PUT to /api/v1/integration/hris/employees/123
    Then the employee record should be updated
    And changes should be audited

  Scenario: Receive termination from HRIS
    Given HRIS sends termination notice
    When I POST to /api/v1/integration/hris/employees/123/terminate
    Then the employee should be marked as TERMINATED
    And offboarding workflow should be triggered

  Scenario: Sync is idempotent
    Given the same employee data is sent twice
    When I process both requests
    Then only one employee should be created/updated
    And no duplicate records should exist

  Scenario: Handle sync errors gracefully
    Given invalid employee data is received
    When I process the sync
    Then validation errors should be returned
    And the error should be logged
    But the system should remain stable
```

**Notes:**
- Implement idempotent operations
- Validate all incoming data
- Trigger onboarding/offboarding workflows
- Maintain audit trail of all syncs

---

### User Story 13.4: Implement WMS Integration
**Title:** Create Endpoints for WMS Data Synchronization

**As an** Integration Developer  
**I want** to sync location and department data from WMS  
**So that** organizational structure is current

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 2.1, US 13.1, US 13.2  

**Acceptance Criteria:**
```gherkin
Feature: WMS Integration
  As an Integration Developer
  I want to sync WMS data
  So that locations are current

  Scenario: Sync department data
    Given WMS sends department information
    When I POST to /api/v1/integration/wms/departments
    Then departments should be created/updated
    And employees should be linked to correct departments

  Scenario: Sync location data
    Given WMS sends warehouse location data
    When I POST to /api/v1/integration/wms/locations
    Then locations should be created/updated
    And available for shift assignments

  Scenario: Link employees to locations
    Given an employee is assigned to a location in WMS
    When location data is synced
    Then the employee's location should be updated

  Scenario: Handle location changes
    Given an employee moves to a different location
    When WMS sends the update
    Then the employee's location should be updated
    And their schedule should be reviewed for conflicts
```

**Notes:**
- Sync department and location master data
- Update employee assignments based on WMS data
- Handle organizational changes
- Validate data consistency

---

### User Story 13.5: Implement Webhook Support
**Title:** Create Webhook Endpoints for Event Notifications

**As an** Integration Developer  
**I want** to send webhooks for important events  
**So that** external systems are notified in real-time

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 13.1, US 13.2  

**Acceptance Criteria:**
```gherkin
Feature: Webhook Event Notifications
  As an Integration Developer
  I want to send webhooks
  So that external systems are notified

  Scenario: Register webhook endpoint
    Given an external system wants to receive events
    When they POST to /api/v1/integration/webhooks with URL and events
    Then a webhook subscription should be created

  Scenario: Send webhook on employee creation
    Given a webhook is subscribed to employee.created event
    When a new employee is created
    Then a webhook should be sent to the registered URL
    And include employee data in payload

  Scenario: Send webhook on schedule change
    Given a webhook is subscribed to schedule.updated event
    When a schedule is modified
    Then a webhook should be sent
    And include schedule details

  Scenario: Retry failed webhooks
    Given a webhook delivery fails
    When the failure is detected
    Then the system should retry with exponential backoff
    And mark as failed after max retries

  Scenario: Webhooks are idempotent
    Given a webhook is sent
    When it is retried
    Then the payload should include an idempotency key
    And the receiver can detect duplicates

  Scenario: Secure webhook delivery
    Given a webhook is sent
    When it is transmitted
    Then it should include a signature header
    And the receiver can verify authenticity
```

**Notes:**
- Support multiple event types
- Implement retry logic with backoff
- Include HMAC signature for verification
- Provide idempotency keys
- Log all webhook deliveries

---

## Epic E14: Audit Trail & Compliance

### User Story 14.1: Create Audit Log Entity
**Title:** Implement Audit Log Domain Model

**As a** Backend Developer  
**I want** to create an AuditLog entity  
**So that** all changes are tracked

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 1.1  

**Acceptance Criteria:**
```gherkin
Feature: Audit Log Entity
  As a Backend Developer
  I want to define audit log structure
  So that changes are tracked

  Scenario: Audit log has required fields
    Given the AuditLog entity
    When I examine its fields
    Then it should have id (UUID)
    And it should have timestamp (ZonedDateTime)
    And it should have userId (UUID)
    And it should have username (String)
    And it should have action (Enum: CREATE, UPDATE, DELETE, LOGIN, LOGOUT, etc.)
    And it should have entityType (String)
    And it should have entityId (UUID)
    And it should have beforeState (JSON, nullable)
    And it should have afterState (JSON, nullable)
    And it should have ipAddress (String)
    And it should have userAgent (String)

  Scenario: Audit log is immutable
    Given an audit log entry exists
    When I try to modify it
    Then the modification should be rejected
    And the original entry should remain unchanged

  Scenario: Audit log captures before/after state
    Given an employee record is updated
    When the audit log is created
    Then beforeState should contain original values
    And afterState should contain new values
```

**Notes:**
- Store before/after state as JSON
- Make audit logs immutable
- Capture user context (IP, user agent)
- Index by timestamp, userId, entityType

---

### User Story 14.2: Implement Audit Logging Aspect
**Title:** Create AOP Aspect for Automatic Audit Logging

**As a** Backend Developer  
**I want** to implement automatic audit logging  
**So that** changes are captured without manual code

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 14.1  

**Acceptance Criteria:**
```gherkin
Feature: Automatic Audit Logging
  As a Backend Developer
  I want automatic audit capture
  So that all changes are logged

  Scenario: Audit create operations
    Given a new entity is created
    When the save operation completes
    Then an audit log should be created
    And action should be CREATE
    And afterState should contain the new entity

  Scenario: Audit update operations
    Given an entity is updated
    When the update operation completes
    Then an audit log should be created
    And action should be UPDATE
    And beforeState should contain original values
    And afterState should contain updated values

  Scenario: Audit delete operations
    Given an entity is deleted
    When the delete operation completes
    Then an audit log should be created
    And action should be DELETE
    And beforeState should contain the deleted entity

  Scenario: Capture user context
    Given a user performs an action
    When the audit log is created
    Then userId should be captured from security context
    And ipAddress should be captured from request
    And userAgent should be captured from request

  Scenario: Audit sensitive operations
    Given a sensitive operation like payroll export
    When the operation is performed
    Then it should be audited
    And marked as sensitive
```

**Notes:**
- Use Spring AOP for automatic logging
- Capture before/after state for updates
- Extract user context from Spring Security
- Mark sensitive operations

---

### User Story 14.3: Implement Audit Query API
**Title:** Create REST Endpoints for Audit Log Queries

**As a** Compliance Officer  
**I want** to query audit logs  
**So that** I can review system activity

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 14.1, US 14.2, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Audit Log Query API
  As a Compliance Officer
  I want to query audit logs
  So that I can review activity

  Scenario: Query audit logs by date range
    Given audit logs exist
    When I GET /api/v1/audit-logs?startDate=2024-01-01&endDate=2024-01-31
    Then I should see all logs within that range

  Scenario: Filter by user
    Given multiple users have performed actions
    When I GET /api/v1/audit-logs?userId=123
    Then I should see only actions by user 123

  Scenario: Filter by entity type
    Given audit logs for multiple entity types
    When I GET /api/v1/audit-logs?entityType=Employee
    Then I should see only Employee-related logs

  Scenario: Filter by action
    Given audit logs with various actions
    When I GET /api/v1/audit-logs?action=DELETE
    Then I should see only DELETE actions

  Scenario: View audit log details
    Given an audit log entry
    When I GET /api/v1/audit-logs/123
    Then I should see full details
    And before/after state comparison

  Scenario: Export audit logs
    Given audit logs for a date range
    When I GET /api/v1/audit-logs/export?format=csv
    Then I should receive a CSV file
    And it should include all audit fields
```

**Notes:**
- Require ADMIN role for audit log access
- Support multiple filter criteria
- Provide CSV export for compliance
- Paginate results for large datasets

---

### User Story 14.4: Implement Tamper-Evident Storage
**Title:** Ensure Audit Logs Cannot Be Modified

**As a** Security Engineer  
**I want** audit logs to be tamper-evident  
**So that** they can be trusted for compliance

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 14.1, US 14.2  

**Acceptance Criteria:**
```gherkin
Feature: Tamper-Evident Audit Logs
  As a Security Engineer
  I want tamper-evident logs
  So that they are trustworthy

  Scenario: Audit logs are write-only
    Given an audit log entry is created
    When I try to update it
    Then the update should be rejected
    And an error should be returned

  Scenario: Audit logs cannot be deleted
    Given an audit log entry exists
    When I try to delete it
    Then the deletion should be rejected
    And the entry should remain

  Scenario: Audit log table has no update/delete permissions
    Given the database schema
    When I examine audit_log table permissions
    Then only INSERT and SELECT should be allowed
    And UPDATE and DELETE should be denied

  Scenario: Audit logs are cryptographically signed
    Given an audit log entry is created
    When I examine the entry
    Then it should include a cryptographic hash
    And the hash should be verifiable

  Scenario: Detect tampering attempts
    Given audit logs with cryptographic hashes
    When I verify the hash chain
    Then any tampering should be detected
    And flagged for investigation
```

**Notes:**
- Use database constraints to prevent updates/deletes
- Implement cryptographic hashing for tamper detection
- Consider blockchain or hash chain for high-security needs
- Regular integrity verification

---

### User Story 14.5: Audit Coverage Testing
**Title:** Create Tests to Verify Audit Coverage

**As a** QA Engineer  
**I want** to verify audit coverage  
**So that** all sensitive operations are logged

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 14.1, US 14.2  

**Acceptance Criteria:**
```gherkin
Feature: Audit Coverage Testing
  As a QA Engineer
  I want to verify audit coverage
  So that logging is complete

  Scenario: Test employee CRUD auditing
    Given I perform employee create/update/delete operations
    When I check the audit logs
    Then all operations should be logged
    And include before/after state

  Scenario: Test schedule auditing
    Given I create and modify schedules
    When I check the audit logs
    Then all schedule changes should be logged

  Scenario: Test payroll export auditing
    Given I generate a payroll export
    When I check the audit logs
    Then the export should be logged
    And marked as sensitive

  Scenario: Test authentication auditing
    Given I log in and log out
    When I check the audit logs
    Then login and logout should be logged

  Scenario: Verify audit coverage metrics
    Given the test suite runs
    When I check coverage
    Then all sensitive operations should have audit tests
    And coverage should be >95%
```

**Notes:**
- Test all CRUD operations
- Verify sensitive operations are audited
- Achieve high test coverage for audit code
- Include integration tests with real database

---

## Epic E15: Reporting & Analytics

### User Story 15.1: Implement Attendance Reports
**Title:** Create Attendance Reporting Endpoints

**As an** HR Administrator  
**I want** to generate attendance reports  
**So that** I can analyze workforce attendance

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 4.1, US 4.5  

**Acceptance Criteria:**
```gherkin
Feature: Attendance Reporting
  As an HR Administrator
  I want attendance reports
  So that I can analyze attendance

  Scenario: Generate daily attendance report
    Given attendance data for a specific date
    When I GET /api/v1/reports/attendance/daily?date=2024-01-15
    Then I should see all employees' attendance for that day
    And it should include clock in/out times and hours worked

  Scenario: Generate weekly attendance summary
    Given attendance data for a week
    When I GET /api/v1/reports/attendance/weekly?startDate=2024-01-15
    Then I should see weekly totals per employee
    And regular vs overtime hours

  Scenario: Filter by department
    Given attendance across multiple departments
    When I filter by department "Warehouse"
    Then I should see only Warehouse attendance

  Scenario: Export attendance report as CSV
    Given attendance report data
    When I request format=csv
    Then I should receive a CSV file
    And it should complete within 10 seconds for 50k rows

  Scenario: Identify attendance issues
    Given attendance data
    When I generate the report
    Then missed punches should be highlighted
    And excessive absences should be flagged
```

**Notes:**
- Support daily, weekly, monthly views
- Filter by department, employee, date range
- Export to CSV and PDF
- Optimize for large datasets (50k+ rows)

---

### User Story 15.2: Implement Overtime Reports
**Title:** Create Overtime Analysis Endpoints

**As a** Supervisor  
**I want** to view overtime reports  
**So that** I can manage labor costs

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 4.1, US 4.5  

**Acceptance Criteria:**
```gherkin
Feature: Overtime Reporting
  As a Supervisor
  I want overtime reports
  So that I can control costs

  Scenario: View overtime by employee
    Given attendance data with overtime
    When I GET /api/v1/reports/overtime/by-employee
    Then I should see overtime hours per employee
    And sorted by highest overtime first

  Scenario: View overtime trends
    Given historical overtime data
    When I GET /api/v1/reports/overtime/trends?period=monthly
    Then I should see overtime trends by month
    And identify increasing/decreasing patterns

  Scenario: Overtime by department
    Given overtime across departments
    When I GET /api/v1/reports/overtime/by-department
    Then I should see total overtime per department
    And percentage of total hours

  Scenario: Identify excessive overtime
    Given employees with high overtime
    When I generate the report
    Then employees exceeding threshold should be flagged
    And recommendations should be provided
```

**Notes:**
- Calculate overtime based on configured rules
- Support trend analysis over time
- Identify employees with excessive overtime
- Provide cost analysis if pay rates available

---

### User Story 15.3: Implement Leave Balance Reports
**Title:** Create Leave Balance Reporting Endpoints

**As an** HR Administrator  
**I want** to view leave balance reports  
**So that** I can monitor time-off usage

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 6.2, US 6.6  

**Acceptance Criteria:**
```gherkin
Feature: Leave Balance Reporting
  As an HR Administrator
  I want leave balance reports
  So that I can monitor time-off

  Scenario: View all employee balances
    Given employees with leave balances
    When I GET /api/v1/reports/leave-balances
    Then I should see balances for all employees
    And accrued, used, and available for each type

  Scenario: Identify low balances
    Given employees with various balances
    When I generate the report
    Then employees with low balances should be highlighted
    And those at risk of losing time should be flagged

  Scenario: Leave usage trends
    Given historical leave data
    When I GET /api/v1/reports/leave-usage/trends
    Then I should see usage patterns by month
    And identify peak leave periods

  Scenario: Export leave balance report
    Given leave balance data
    When I request CSV export
    Then I should receive a file with all balances
```

**Notes:**
- Show all leave types and balances
- Identify employees at risk of losing accrued time
- Analyze usage trends and patterns
- Support CSV export

---

### User Story 15.4: Implement Certification Status Reports
**Title:** Create Certification Reporting Endpoints

**As a** Safety Manager  
**I want** to view certification status reports  
**So that** I can ensure compliance

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 7.1, US 7.3, US 7.4  

**Acceptance Criteria:**
```gherkin
Feature: Certification Status Reporting
  As a Safety Manager
  I want certification reports
  So that I can ensure compliance

  Scenario: View all employee certifications
    Given employees with certifications
    When I GET /api/v1/reports/certifications
    Then I should see all certifications by employee
    And their status and expiry dates

  Scenario: View expiring certifications
    Given certifications expiring soon
    When I GET /api/v1/reports/certifications/expiring?days=30
    Then I should see certifications expiring in 30 days
    And sorted by expiry date

  Scenario: View expired certifications
    Given employees with expired certifications
    When I GET /api/v1/reports/certifications/expired
    Then I should see all expired certifications
    And affected employees

  Scenario: Certification compliance by department
    Given certification requirements by department
    When I GET /api/v1/reports/certifications/compliance
    Then I should see compliance percentage per department
    And identify non-compliant employees
```

**Notes:**
- Show certification status for all employees
- Highlight expiring and expired certifications
- Calculate compliance rates by department
- Support filtering by certification type

---

### User Story 15.5: Implement Safety KPI Dashboard
**Title:** Create Safety Metrics Dashboard Endpoints

**As a** Safety Manager  
**I want** to view safety KPIs  
**So that** I can monitor safety performance

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 8.1, US 8.5  

**Acceptance Criteria:**
```gherkin
Feature: Safety KPI Dashboard
  As a Safety Manager
  I want safety KPIs
  So that I can monitor performance

  Scenario: View incident summary
    Given incident data
    When I GET /api/v1/reports/safety/summary
    Then I should see total incidents
    And breakdown by type and severity
    And comparison to previous period

  Scenario: Calculate incident rates
    Given incidents and hours worked
    When I GET /api/v1/reports/safety/rates
    Then I should see TRIR (Total Recordable Incident Rate)
    And DART (Days Away, Restricted, or Transferred)
    And other standard safety metrics

  Scenario: View incidents by location
    Given incidents across locations
    When I GET /api/v1/reports/safety/by-location
    Then I should see incident counts per location
    And identify high-risk areas

  Scenario: Safety trends over time
    Given historical incident data
    When I GET /api/v1/reports/safety/trends
    Then I should see incident trends by month
    And identify improving/worsening trends
```

**Notes:**
- Calculate standard safety KPIs (TRIR, DART, etc.)
- Provide trend analysis
- Identify high-risk locations and times
- Support comparison to previous periods

---

## Epic E16: Mobile Access (PWA)

### User Story 16.1: Configure PWA Manifest
**Title:** Create Progressive Web App Manifest

**As a** Frontend Developer  
**I want** to configure PWA manifest  
**So that** the app can be installed on mobile devices

**Priority:** Medium  
**Story Points:** 3  
**Dependencies:** US 1.1  

**Acceptance Criteria:**
```gherkin
Feature: PWA Manifest Configuration
  As a Frontend Developer
  I want PWA manifest
  So that app is installable

  Scenario: Manifest file exists
    Given the application
    When I check for manifest.json
    Then it should exist in the public directory
    And be accessible at /manifest.json

  Scenario: Manifest has required fields
    Given the manifest.json file
    When I examine its contents
    Then it should have name and short_name
    And icons in multiple sizes (192x192, 512x512)
    And start_url
    And display mode set to "standalone"
    And theme_color and background_color

  Scenario: App is installable
    Given the PWA manifest is configured
    When I access the app on mobile
    Then the browser should prompt to install
    And the app should be installable to home screen

  Scenario: Lighthouse PWA score
    Given the application is deployed
    When I run Lighthouse audit
    Then the PWA score should be â¥ 80
```

**Notes:**
- Include icons in multiple sizes
- Set appropriate display mode and colors
- Ensure HTTPS for PWA features
- Test on multiple mobile browsers

---

### User Story 16.2: Implement Service Worker
**Title:** Create Service Worker for Offline Support

**As a** Frontend Developer  
**I want** to implement a service worker  
**So that** the app works offline

**Priority:** Medium  
**Story Points:** 8  
**Dependencies:** US 16.1  

**Acceptance Criteria:**
```gherkin
Feature: Service Worker for Offline Support
  As a Frontend Developer
  I want offline support
  So that app works without connection

  Scenario: Service worker is registered
    Given the application loads
    When I check the browser
    Then a service worker should be registered
    And active

  Scenario: Cache static assets
    Given the service worker is active
    When the app loads
    Then static assets should be cached
    And available offline

  Scenario: Cache API responses
    Given the user has accessed data
    When they go offline
    Then previously loaded data should be available
    From cache

  Scenario: Queue offline actions
    Given the user is offline
    When they clock in
    Then the action should be queued
    And synced when connection is restored

  Scenario: Handle cache updates
    Given a new version is deployed
    When the service worker updates
    Then old cache should be cleared
    And new assets should be cached
```

**Notes:**
- Cache static assets for offline access
- Implement cache-first strategy for API responses
- Queue write operations when offline
- Handle cache versioning and updates

---

### User Story 16.3: Implement Mobile-Friendly Clock In/Out
**Title:** Create Mobile-Optimized Clock In/Out Interface

**As a** Warehouse Worker  
**I want** to clock in/out from my mobile device  
**So that** I can track time easily

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 4.2, US 4.3, US 16.1  

**Acceptance Criteria:**
```gherkin
Feature: Mobile Clock In/Out
  As a Warehouse Worker
  I want mobile clock in/out
  So that I can track time easily

  Scenario: Clock in from mobile
    Given I am on the mobile app
    And I am not clocked in
    When I tap the "Clock In" button
    Then I should be clocked in
    And see confirmation

  Scenario: Clock out from mobile
    Given I am clocked in
    When I tap the "Clock Out" button
    Then I should be clocked out
    And see my hours worked

  Scenario: Offline clock in
    Given I am offline
    When I clock in
    Then the action should be queued
    And synced when online
    And I should see offline indicator

  Scenario: Geolocation capture
    Given location services are enabled
    When I clock in
    Then my location should be captured
    And validated against geofence if configured

  Scenario: Large touch targets
    Given the mobile interface
    When I examine the clock buttons
    Then they should be large enough for easy tapping
    And meet accessibility guidelines
```

**Notes:**
- Design for touch interfaces
- Large, easy-to-tap buttons
- Support offline queueing
- Capture geolocation if available

---

### User Story 16.4: Implement Mobile Schedule View
**Title:** Create Mobile-Optimized Schedule Interface

**As a** Warehouse Worker  
**I want** to view my schedule on mobile  
**So that** I know when I work

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 5.6, US 16.1  

**Acceptance Criteria:**
```gherkin
Feature: Mobile Schedule View
  As a Warehouse Worker
  I want mobile schedule view
  So that I can see my shifts

  Scenario: View upcoming shifts
    Given I am on the mobile app
    When I navigate to schedule
    Then I should see my upcoming shifts
    In a mobile-friendly format

  Scenario: Calendar view
    Given I am viewing my schedule
    When I switch to calendar view
    Then I should see a mobile-optimized calendar
    With my shifts highlighted

  Scenario: Shift details
    Given I see a shift in my schedule
    When I tap on it
    Then I should see full shift details
    Including time, location, and coworkers

  Scenario: Offline schedule access
    Given I have viewed my schedule while online
    When I go offline
    Then I should still be able to view my schedule
    From cached data

  Scenario: Responsive design
    Given the schedule interface
    When I view it on different screen sizes
    Then it should adapt appropriately
    And remain usable
```

**Notes:**
- Optimize for small screens
- Support both list and calendar views
- Cache schedule data for offline access
- Responsive design for various devices

---

### User Story 16.5: Implement Mobile Leave Request
**Title:** Create Mobile-Optimized Leave Request Interface

**As a** Warehouse Worker  
**I want** to request leave from mobile  
**So that** I can manage time off easily

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 6.3, US 16.1  

**Acceptance Criteria:**
```gherkin
Feature: Mobile Leave Request
  As a Warehouse Worker
  I want mobile leave requests
  So that I can request time off easily

  Scenario: Submit leave request from mobile
    Given I am on the mobile app
    When I navigate to leave requests
    And fill out the request form
    Then I should be able to submit
    And receive confirmation

  Scenario: View leave balance
    Given I am requesting leave
    When I view my balances
    Then I should see available days per leave type
    In a mobile-friendly format

  Scenario: View request status
    Given I have submitted leave requests
    When I view my requests
    Then I should see their status
    And any supervisor comments

  Scenario: Mobile-friendly date picker
    Given I am selecting leave dates
    When I tap the date field
    Then a mobile-optimized date picker should appear
    And be easy to use

  Scenario: Form validation
    Given I am submitting a leave request
    When I have invalid data
    Then validation errors should be clear
    And easy to correct on mobile
```

**Notes:**
- Optimize forms for mobile input
- Use mobile-friendly date pickers
- Show clear validation messages
- Display leave balances prominently

---

## Epic E17: Onboarding & Offboarding Workflow

### User Story 17.1: Create Onboarding Workflow Entity
**Title:** Implement Onboarding Workflow Domain Model

**As a** Backend Developer  
**I want** to create an OnboardingWorkflow entity  
**So that** new hire processes can be tracked

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 2.1  

**Acceptance Criteria:**
```gherkin
Feature: Onboarding Workflow Entity
  As a Backend Developer
  I want to define onboarding structure
  So that new hires are tracked

  Scenario: Onboarding workflow has required fields
    Given the OnboardingWorkflow entity
    When I examine its fields
    Then it should have id (UUID)
    And it should have employeeId (UUID)
    And it should have startDate (LocalDate)
    And it should have status (Enum: NOT_STARTED, IN_PROGRESS, COMPLETED)
    And it should have completionDate (LocalDate, nullable)

  Scenario: Onboarding includes tasks
    Given an onboarding workflow
    When I examine its tasks
    Then it should have multiple OnboardingTask records
    And each should have description, assignee, dueDate, status

  Scenario: Tasks can be categorized
    Given onboarding tasks
    When I examine them
    Then they should be categorized (HR, IT, Training, etc.)
    And have dependencies between tasks
```

**Notes:**
- Track overall workflow status
- Include multiple tasks per workflow
- Support task dependencies
- Link to employee record

---

### User Story 17.2: Implement Automated Onboarding Trigger
**Title:** Create Automatic Onboarding Workflow on New Hire

**As an** HR Administrator  
**I want** onboarding to start automatically  
**So that** new hires are processed consistently

**Priority:** Medium  
**Story Points:** 8  
**Dependencies:** US 17.1, US 13.3  

**Acceptance Criteria:**
```gherkin
Feature: Automated Onboarding
  As an HR Administrator
  I want automatic onboarding
  So that new hires are processed

  Scenario: Trigger onboarding on new employee
    Given a new employee is created
    When the employee record is saved
    Then an onboarding workflow should be created
    And initial tasks should be generated

  Scenario: Trigger from HRIS sync
    Given HRIS sends new hire data
    When the employee is created via integration
    Then onboarding should be triggered automatically

  Scenario: Generate standard tasks
    Given onboarding is triggered
    When tasks are generated
    Then standard tasks should be created
    Including account setup, training enrollment, asset assignment

  Scenario: Assign tasks to appropriate roles
    Given onboarding tasks are created
    When tasks are assigned
    Then HR tasks should go to HR team
    And IT tasks should go to IT team
    And training tasks should go to training coordinator

  Scenario: Set due dates
    Given onboarding tasks are created
    When due dates are set
    Then they should be based on start date
    And follow configured timelines
```

**Notes:**
- Trigger on employee creation
- Generate tasks from template
- Assign to appropriate teams
- Calculate due dates based on start date

---

### User Story 17.3: Implement Onboarding Task Management
**Title:** Create Endpoints for Onboarding Task Tracking

**As an** HR Administrator  
**I want** to manage onboarding tasks  
**So that** new hire progress is tracked

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 17.1, US 17.2, US 3.1  

**Acceptance Criteria:**
```gherkin
Feature: Onboarding Task Management
  As an HR Administrator
  I want to manage onboarding tasks
  So that progress is tracked

  Scenario: View onboarding tasks
    Given an employee has onboarding in progress
    When I GET /api/v1/onboarding/123/tasks
    Then I should see all onboarding tasks
    And their status and assignees

  Scenario: Complete a task
    Given an onboarding task is assigned to me
    When I POST to /api/v1/onboarding/tasks/456/complete
    Then the task status should change to COMPLETED
    And completion date should be set

  Scenario: Add notes to task
    Given I am completing a task
    When I add notes
    Then the notes should be saved
    And visible to other team members

  Scenario: Reassign task
    Given a task is assigned to user A
    When I reassign it to user B
    Then user B should be notified
    And the task should appear in their queue

  Scenario: Track overdue tasks
    Given a task is past its due date
    When I view onboarding status
    Then the task should be flagged as overdue
    And escalation should be triggered
```

**Notes:**
- Support task completion with notes
- Allow task reassignment
- Track overdue tasks
- Send notifications to assignees

---

### User Story 17.4: Implement Offboarding Workflow
**Title:** Create Automated Offboarding Process

**As an** HR Administrator  
**I want** automated offboarding  
**So that** terminations are processed consistently

**Priority:** Medium  
**Story Points:** 8  
**Dependencies:** US 17.1, US 13.3  

**Acceptance Criteria:**
```gherkin
Feature: Automated Offboarding
  As an HR Administrator
  I want automatic offboarding
  So that terminations are processed

  Scenario: Trigger offboarding on termination
    Given an employee is terminated
    When the termination is processed
    Then an offboarding workflow should be created
    And offboarding tasks should be generated

  Scenario: Revoke system access
    Given offboarding is triggered
    When access revocation task is completed
    Then the employee's account should be disabled
    And they should not be able to log in

  Scenario: Collect assets
    Given offboarding is in progress
    When asset collection task is created
    Then all assigned assets should be listed
    And marked for return

  Scenario: Update schedules
    Given an employee is being offboarded
    When offboarding is triggered
    Then future schedules should be updated
    And the employee should be removed from assignments

  Scenario: Final payroll processing
    Given offboarding is complete
    When final payroll task is completed
    Then final hours should be calculated
    And included in payroll export
```

**Notes:**
- Trigger on employee termination
- Revoke access automatically
- Generate asset return tasks
- Update future schedules
- Calculate final pay

---

### User Story 17.5: Onboarding/Offboarding Reporting
**Title:** Create Reports for Onboarding/Offboarding Status

**As an** HR Administrator  
**I want** to view onboarding/offboarding reports  
**So that** I can monitor progress

**Priority:** Medium  
**Story Points:** 5  
**Dependencies:** US 17.1, US 17.2, US 17.4  

**Acceptance Criteria:**
```gherkin
Feature: Onboarding/Offboarding Reporting
  As an HR Administrator
  I want workflow reports
  So that I can monitor progress

  Scenario: View active onboardings
    Given multiple employees are onboarding
    When I GET /api/v1/onboarding/active
    Then I should see all in-progress onboardings
    And their completion percentage

  Scenario: View overdue tasks
    Given some onboarding tasks are overdue
    When I GET /api/v1/onboarding/overdue-tasks
    Then I should see all overdue tasks
    And the responsible assignees

  Scenario: Onboarding completion metrics
    Given historical onboarding data
    When I GET /api/v1/onboarding/metrics
    Then I should see average completion time
    And identify bottlenecks

  Scenario: View active offboardings
    Given employees are being offboarded
    When I GET /api/v1/offboarding/active
    Then I should see all in-progress offboardings
    And their status

  Scenario: Offboarding checklist
    Given an employee is being offboarded
    When I view their offboarding
    Then I should see a checklist of all tasks
    And what's completed vs pending
```

**Notes:**
- Show active workflows and their status
- Identify overdue tasks and bottlenecks
- Calculate completion metrics
- Provide checklist view for easy tracking

---

## Epic E18: Localization & Multi-Tenant

### User Story 18.1: Implement Tenant Entity
**Title:** Create Multi-Tenant Domain Model

**As a** Backend Developer  
**I want** to implement tenant isolation  
**So that** multiple warehouses can use the system

**Priority:** Low  
**Story Points:** 8  
**Dependencies:** US 1.1  

**Acceptance Criteria:**
```gherkin
Feature: Multi-Tenant Support
  As a Backend Developer
  I want tenant isolation
  So that data is separated

  Scenario: Tenant entity has required fields
    Given the Tenant entity
    When I examine its fields
    Then it should have id (UUID)
    And it should have name (String)
    And it should have subdomain (String, unique)
    And it should have timezone (String)
    And it should have locale (String)
    And it should have isActive (Boolean)

  Scenario: All entities include tenant ID
    Given any domain entity
    When I examine its fields
    Then it should have tenantId (UUID)
    And it should be indexed

  Scenario: Queries filter by tenant
    Given I query for employees
    When the query executes
    Then it should automatically filter by current tenant
    And only return data for that tenant

  Scenario: Tenant isolation is enforced
    Given I am authenticated for tenant A
    When I try to access data from tenant B
    Then the request should be denied
    And no data should be leaked
```

**Notes:**
- Add tenantId to all entities
- Implement tenant context filter
- Ensure queries always include tenant filter
- Test tenant isolation thoroughly

---

### User Story 18.2: Implement Tenant Context
**Title:** Create Tenant Context Management

**As a** Backend Developer  
**I want** to manage tenant context  
**So that** the correct tenant is always used

**Priority:** Low  
**Story Points:** 5  
**Dependencies:** US 18.1  

**Acceptance Criteria:**
```gherkin
Feature: Tenant Context Management
  As a Backend Developer
  I want tenant context
  So that correct tenant is used

  Scenario: Extract tenant from request
    Given a request with tenant identifier
    When the request is processed
    Then the tenant should be extracted
    And set in context

  Scenario: Tenant from subdomain
    Given a request to tenant1.example.com
    When the request is processed
    Then tenant should be identified as "tenant1"

  Scenario: Tenant from header
    Given a request with X-Tenant-ID header
    When the request is processed
    Then tenant should be extracted from header

  Scenario: Tenant context in thread
    Given tenant is set in context
    When I access it from any layer
    Then the correct tenant should be available

  Scenario: Clear context after request
    Given a request is processed
    When the request completes
    Then tenant context should be cleared
    To prevent leakage to next request
```

**Notes:**
- Support subdomain-based tenant identification
- Support header-based tenant identification
- Use ThreadLocal for context storage
- Clear context after each request

---

### User Story 18.3: Implement Localization Support
**Title:** Add Multi-Language Support

**As a** Backend Developer  
**I want** to support multiple languages  
**So that** the system can be used internationally

**Priority:** Low  
**Story Points:** 8  
**Dependencies:** US 1.1  

**Acceptance Criteria:**
```gherkin
Feature: Localization Support
  As a Backend Developer
  I want multi-language support
  So that system is international

  Scenario: Support English and Spanish
    Given the application
    When I check supported locales
    Then English (en) should be supported
    And Spanish (es) should be supported

  Scenario: Externalize UI strings
    Given UI text in the application
    When I examine the code
    Then all strings should be externalized
    And stored in message properties files

  Scenario: Locale from Accept-Language header
    Given a request with Accept-Language: es
    When the request is processed
    Then responses should be in Spanish

  Scenario: Locale from user preference
    Given a user has locale preference set to Spanish
    When they make requests
    Then responses should be in Spanish
    Regardless of Accept-Language header

  Scenario: Fallback to default locale
    Given a request for unsupported locale
    When the request is processed
    Then it should fallback to English
```

**Notes:**
- Support en and es locales initially
- Externalize all user-facing strings
- Use Spring MessageSource
- Respect Accept-Language header
- Allow user locale preference override

---

### User Story 18.4: Implement Timezone Handling
**Title:** Add Timezone-Aware Date/Time Handling

**As a** Backend Developer  
**I want** timezone-aware date handling  
**So that** times are correct for each location

**Priority:** Low  
**Story Points:** 5  
**Dependencies:** US 18.1  

**Acceptance Criteria:**
```gherkin
Feature: Timezone-Aware Date Handling
  As a Backend Developer
  I want timezone support
  So that times are correct

  Scenario: Store dates in UTC
    Given a date/time is saved
    When it is stored in database
    Then it should be in UTC

  Scenario: Convert to tenant timezone
    Given a tenant in America/New_York timezone
    When dates are returned in API
    Then they should be converted to tenant timezone

  Scenario: Accept dates in any timezone
    Given a request with date in ISO 8601 format
    When the date is parsed
    Then the timezone should be respected
    And converted to UTC for storage

  Scenario: Schedule respects timezone
    Given a shift scheduled for 08:00 in New York
    When viewed from a different timezone
    Then it should display in local time
    But represent the same moment

  Scenario: Daylight saving time handling
    Given a schedule across DST transition
    When DST changes
    Then schedules should remain correct
    In local time
```

**Notes:**
- Store all dates in UTC
- Use ZonedDateTime for timezone awareness
- Convert to tenant timezone for display
- Handle DST transitions correctly

---

### User Story 18.5: Test Tenant Isolation
**Title:** Create Tests to Verify Tenant Data Isolation

**As a** QA Engineer  
**I want** to verify tenant isolation  
**So that** data leakage is prevented

**Priority:** Low  
**Story Points:** 5  
**Dependencies:** US 18.1, US 18.2  

**Acceptance Criteria:**
```gherkin
Feature: Tenant Isolation Testing
  As a QA Engineer
  I want to verify isolation
  So that data is protected

  Scenario: Cannot access other tenant's data
    Given I am authenticated for tenant A
    And data exists for tenant B
    When I query for data
    Then I should only see tenant A data
    And tenant B data should not be accessible

  Scenario: Queries always include tenant filter
    Given any database query
    When I examine the SQL
    Then it should include tenant_id filter

  Scenario: Cannot create data for other tenant
    Given I am authenticated for tenant A
    When I try to create data with tenant B ID
    Then the request should be rejected

  Scenario: Tenant context is consistent
    Given a request for tenant A
    When the request is processed
    Then tenant context should be A throughout
    And never change to another tenant

  Scenario: Test coverage for tenant isolation
    Given the test suite
    When I check coverage
    Then all multi-tenant code should be tested
    And isolation should be verified
```

**Notes:**
- Test all CRUD operations for isolation
- Verify queries include tenant filter
- Test cross-tenant access attempts
- Achieve high coverage of tenant code

---

## Epic E19: Observability & Monitoring

### User Story 19.1: Implement Structured Logging
**Title:** Configure JSON Structured Logging

**As a** DevOps Engineer  
**I want** structured JSON logging  
**So that** logs are easily parsed and analyzed

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 1.1  

**Acceptance Criteria:**
```gherkin
Feature: Structured Logging
  As a DevOps Engineer
  I want JSON logs
  So that they are parseable

  Scenario: Logs are in JSON format
    Given the application is running
    When I examine the logs
    Then they should be in JSON format
    And include standard fields (timestamp, level, message, logger)

  Scenario: Include trace ID in logs
    Given a request is being processed
    When logs are written
    Then they should include traceId
    And spanId for distributed tracing

  Scenario: Include user context
    Given an authenticated user makes a request
    When logs are written
    Then they should include userId
    And username

  Scenario: Include tenant context
    Given a multi-tenant request
    When logs are written
    Then they should include tenantId

  Scenario: Structured exception logging
    Given an exception occurs
    When it is logged
    Then the stack trace should be structured
    And include exception type and message
```

**Notes:**
- Use Logback with JSON encoder
- Include MDC context (traceId, userId, tenantId)
- Structure exception information
- Configure appropriate log levels

---

### User Story 19.2: Implement Distributed Tracing
**Title:** Configure Spring Cloud Sleuth for Tracing

**As a** DevOps Engineer  
**I want** distributed tracing  
**So that** I can track requests across services

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 1.1, US 19.1  

**Acceptance Criteria:**
```gherkin
Feature: Distributed Tracing
  As a DevOps Engineer
  I want distributed tracing
  So that I can track requests

  Scenario: Trace ID is generated
    Given a new request arrives
    When it is processed
    Then a unique trace ID should be generated
    And included in all logs

  Scenario: Trace ID propagates
    Given a request with trace ID
    When it calls another service
    Then the trace ID should be propagated
    In request headers

  Scenario: Spans are created
    Given a request is being processed
    When different components are invoked
    Then spans should be created for each
    And linked to the trace

  Scenario: Export to Zipkin
    Given tracing is configured
    When spans are completed
    Then they should be exported to Zipkin
    For visualization

  Scenario: Sampling rate is configurable
    Given high request volume
    When tracing is active
    Then sampling rate should be configurable
    To control overhead
```

**Notes:**
- Use Spring Cloud Sleuth
- Configure Zipkin export
- Set appropriate sampling rate
- Propagate trace context in headers

---

### User Story 19.3: Implement Prometheus Metrics
**Title:** Configure Prometheus Metrics Export

**As a** DevOps Engineer  
**I want** Prometheus metrics  
**So that** I can monitor application performance

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 1.4  

**Acceptance Criteria:**
```gherkin
Feature: Prometheus Metrics
  As a DevOps Engineer
  I want Prometheus metrics
  So that I can monitor performance

  Scenario: Metrics endpoint is available
    Given the application is running
    When I access /actuator/prometheus
    Then I should receive metrics in Prometheus format

  Scenario: JVM metrics are exported
    Given the metrics endpoint
    When I query it
    Then JVM metrics should be included
    Including memory, threads, GC

  Scenario: HTTP metrics are exported
    Given the metrics endpoint
    When I query it
    Then HTTP request metrics should be included
    Including count, duration, status codes

  Scenario: Database metrics are exported
    Given the metrics endpoint
    When I query it
    Then database connection pool metrics should be included

  Scenario: Custom business metrics
    Given business operations
    When they execute
    Then custom metrics should be recorded
    Like clock-ins per hour, schedules created
```

**Notes:**
- Enable Micrometer Prometheus registry
- Export standard JVM and HTTP metrics
- Add custom business metrics
- Secure metrics endpoint

---

### User Story 19.4: Implement Health Checks
**Title:** Configure Comprehensive Health Checks

**As a** DevOps Engineer  
**I want** detailed health checks  
**So that** I can monitor system health

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 1.4  

**Acceptance Criteria:**
```gherkin
Feature: Health Checks
  As a DevOps Engineer
  I want health checks
  So that I can monitor health

  Scenario: Basic health check
    Given the application is running
    When I GET /actuator/health
    Then I should receive status UP

  Scenario: Database health check
    Given the database is accessible
    When I check health
    Then database component should be UP
    When database is down
    Then database component should be DOWN
    And overall status should be DOWN

  Scenario: Disk space health check
    Given sufficient disk space
    When I check health
    Then disk space component should be UP
    When disk is full
    Then disk space component should be DOWN

  Scenario: Custom health indicators
    Given external dependencies
    When I check health
    Then custom health indicators should be included
    Like HRIS connectivity, payroll system

  Scenario: Readiness vs liveness
    Given Kubernetes deployment
    When I check /actuator/health/readiness
    Then it should indicate if app is ready for traffic
    When I check /actuator/health/liveness
    Then it should indicate if app is alive
```

**Notes:**
- Implement database health indicator
- Add custom health indicators for integrations
- Support readiness and liveness probes
- Configure health check details visibility

---

### User Story 19.5: Implement Alerting Rules
**Title:** Define Alerting Rules and Runbooks

**As a** DevOps Engineer  
**I want** alerting rules  
**So that** I am notified of issues

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 19.3, US 19.4  

**Acceptance Criteria:**
```gherkin
Feature: Alerting Rules
  As a DevOps Engineer
  I want alerting rules
  So that I am notified of issues

  Scenario: Alert on high error rate
    Given error rate exceeds threshold
    When the condition persists for 5 minutes
    Then an alert should be triggered
    And sent to on-call engineer

  Scenario: Alert on high response time
    Given API response time exceeds SLO
    When the condition persists
    Then an alert should be triggered

  Scenario: Alert on health check failure
    Given health check returns DOWN
    When the condition persists for 2 minutes
    Then a critical alert should be triggered

  Scenario: Alert on database connection issues
    Given database connection pool is exhausted
    When the condition is detected
    Then an alert should be triggered

  Scenario: Runbook links in alerts
    Given an alert is triggered
    When I receive the alert
    Then it should include a runbook link
    With troubleshooting steps
```

**Notes:**
- Define SLOs for critical endpoints
- Configure Prometheus alerting rules
- Create runbooks for common issues
- Set up alert routing (PagerDuty, Slack, etc.)

---

## Epic E20: CI/CD & Deployment Automation

### User Story 20.1: Configure CI Pipeline
**Title:** Set Up Continuous Integration Pipeline

**As a** DevOps Engineer  
**I want** a CI pipeline  
**So that** code is automatically built and tested

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 1.1  

**Acceptance Criteria:**
```gherkin
Feature: CI Pipeline
  As a DevOps Engineer
  I want CI pipeline
  So that code is validated

  Scenario: Pipeline runs on every PR
    Given a pull request is created
    When the PR is submitted
    Then the CI pipeline should run automatically

  Scenario: Build step compiles code
    Given the CI pipeline runs
    When the build step executes
    Then the code should compile successfully
    And all dependencies should be resolved

  Scenario: Test step runs all tests
    Given the build succeeds
    When the test step executes
    Then all unit tests should run
    And all integration tests should run
    And test results should be reported

  Scenario: SAST scan detects vulnerabilities
    Given the tests pass
    When the SAST step executes
    Then code should be scanned for security issues
    And vulnerabilities should be reported

  Scenario: Pipeline fails on test failure
    Given a test fails
    When the pipeline runs
    Then the pipeline should fail
    And prevent merge
```

**Notes:**
- Use GitHub Actions or Jenkins
- Run on every PR and push to main
- Include build, test, SAST steps
- Fail fast on errors
- Report results in PR

---

### User Story 20.2: Configure Docker Build
**Title:** Create Docker Image Build Process

**As a** DevOps Engineer  
**I want** Docker images  
**So that** the app can be deployed consistently

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 20.1  

**Acceptance Criteria:**
```gherkin
Feature: Docker Image Build
  As a DevOps Engineer
  I want Docker images
  So that deployment is consistent

  Scenario: Dockerfile exists
    Given the project repository
    When I check the root directory
    Then a Dockerfile should exist
    And follow best practices

  Scenario: Multi-stage build
    Given the Dockerfile
    When I examine it
    Then it should use multi-stage build
    To minimize image size

  Scenario: Image is tagged with commit SHA
    Given a commit is pushed
    When the Docker image is built
    Then it should be tagged with commit SHA
    And also with branch name

  Scenario: Image is pushed to registry
    Given the Docker image is built
    When the build succeeds
    Then the image should be pushed to container registry

  Scenario: Image includes health check
    Given the Docker image
    When I examine it
    Then it should include a HEALTHCHECK instruction
```

**Notes:**
- Use multi-stage build for smaller images
- Tag with commit SHA and branch
- Push to container registry (Docker Hub, ECR, etc.)
- Include HEALTHCHECK in Dockerfile
- Use non-root user

---

### User Story 20.3: Configure Database Migrations in CD
**Title:** Automate Database Migrations in Deployment

**As a** DevOps Engineer  
**I want** automated database migrations  
**So that** schema changes are applied safely

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 1.3, US 20.1  

**Acceptance Criteria:**
```gherkin
Feature: Automated Database Migrations
  As a DevOps Engineer
  I want automated migrations
  So that schema is updated

  Scenario: Migrations run before app starts
    Given a new version is deployed
    When the deployment process runs
    Then database migrations should execute first
    Before the application starts

  Scenario: Migration failure prevents deployment
    Given a migration fails
    When the deployment runs
    Then the deployment should be aborted
    And the application should not start

  Scenario: Migrations are idempotent
    Given a migration has already run
    When the deployment runs again
    Then the migration should not re-execute
    And should be skipped

  Scenario: Migration rollback plan exists
    Given a migration is deployed
    When issues are detected
    Then a rollback migration should be available
    And documented

  Scenario: Migration status is tracked
    Given migrations have run
    When I check the database
    Then migration history should be recorded
    In flyway_schema_history table
```

**Notes:**
- Run Flyway migrations before app startup
- Fail deployment if migration fails
- Ensure migrations are idempotent
- Document rollback procedures
- Test migrations in staging first

---

### User Story 20.4: Implement Zero-Downtime Deployment
**Title:** Configure Rolling Deployment Strategy

**As a** DevOps Engineer  
**I want** zero-downtime deployments  
**So that** users are not impacted

**Priority:** High  
**Story Points:** 8  
**Dependencies:** US 20.2, US 20.3  

**Acceptance Criteria:**
```gherkin
Feature: Zero-Downtime Deployment
  As a DevOps Engineer
  I want zero-downtime deploys
  So that users are not impacted

  Scenario: Rolling deployment strategy
    Given a new version is ready
    When deployment starts
    Then instances should be updated one at a time
    And old instances should remain until new ones are healthy

  Scenario: Health check before traffic
    Given a new instance is deployed
    When it starts
    Then health checks should pass
    Before it receives traffic

  Scenario: Gradual traffic shift
    Given new instances are healthy
    When traffic is shifted
    Then it should be gradual
    And monitored for errors

  Scenario: Automatic rollback on failure
    Given a deployment is in progress
    When error rate increases
    Then deployment should automatically rollback
    To previous version

  Scenario: Database backward compatibility
    Given schema changes are deployed
    When old and new versions run simultaneously
    Then both should work with the schema
    During rolling deployment
```

**Notes:**
- Use Kubernetes rolling update or blue-green deployment
- Configure readiness probes
- Monitor error rates during deployment
- Implement automatic rollback
- Ensure database changes are backward compatible

---

### User Story 20.5: Test Rollback Procedures
**Title:** Verify Rollback Process Works

**As a** DevOps Engineer  
**I want** tested rollback procedures  
**So that** I can recover from bad deployments

**Priority:** High  
**Story Points:** 5  
**Dependencies:** US 20.4  

**Acceptance Criteria:**
```gherkin
Feature: Rollback Procedures
  As a DevOps Engineer
  I want tested rollback
  So that I can recover quickly

  Scenario: Rollback to previous version
    Given a bad deployment is detected
    When I trigger rollback
    Then the previous version should be deployed
    And traffic should shift back

  Scenario: Rollback completes quickly
    Given rollback is triggered
    When the process runs
    Then it should complete within 5 minutes

  Scenario: Database rollback if needed
    Given a schema change needs rollback
    When rollback is triggered
    Then database rollback migration should run
    And schema should revert

  Scenario: Rollback is tested quarterly
    Given the rollback procedure
    When the quarter ends
    Then rollback should be tested in staging
    And documented

  Scenario: Rollback runbook exists
    Given the deployment documentation
    When I check for rollback procedures
    Then a detailed runbook should exist
    With step-by-step instructions
```

**Notes:**
- Document rollback procedures
- Test rollback quarterly
- Ensure rollback is fast (<5 minutes)
- Include database rollback if needed
- Maintain rollback runbook

---

# Summary

This comprehensive set of user stories covers all 20 epics for the Warehouse Employee Management System. Each user story follows the standard agile format with:

- **Title**: Clear, descriptive title
- **Narrative**: "As a [role] I want [action] so that [benefit]"
- **Priority**: High, Medium, or Low
- **Story Points**: Estimated effort (Fibonacci scale)
- **Dependencies**: References to prerequisite user stories
- **Acceptance Criteria**: Gherkin-formatted scenarios
- **Notes**: Additional implementation details

The user stories are designed to be:
- **Independent**: Can be developed in any order (respecting dependencies)
- **Negotiable**: Details can be refined during sprint planning
- **Valuable**: Each delivers business value
- **Estimable**: Sized appropriately for sprint planning
- **Small**: Completable within a sprint
- **Testable**: Clear acceptance criteria

Total User Stories: 100
Total Story Points: ~550

This represents approximately 6-9 months of development work for a team of 4-6 developers, depending on velocity and complexity.