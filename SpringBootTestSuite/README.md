# Warehouse EMS - JUnit Test Suite

## Overview

This directory contains a comprehensive JUnit test suite for the Warehouse Employee Management System (EMS) SpringBoot project. The test suite provides extensive coverage of all major components including services, controllers, repositories, mappers, security, exception handling, and audit logging.

---

## Test Suite Summary

### Total Test Classes: 6
### Total Test Methods: 200+
### Coverage Areas:
- Employee Service Layer
- REST API Controllers
- Database Repository Layer
- Entity-DTO Mapping
- Security & Authentication
- Exception Handling
- Audit Logging

---

## Test Classes

### 1. EmployeeServiceTest.java
**Purpose**: Tests the business logic layer for employee management

**Test Coverage**:
- â Create Employee (valid input, null input, empty name, duplicate badge ID, invalid role, future hire date)
- â Get Employee by ID (valid ID, non-existent ID, null ID, negative ID, zero ID)
- â Get All Employees (pagination, department filter, status filter, role filter, empty results)
- â Update Employee (valid input, non-existent ID, null ID, duplicate badge ID)
- â Delete Employee (soft-delete, non-existent ID, null ID, already inactive)
- â Get Employee by Badge ID (valid, non-existent, null, empty, whitespace)

**Test Count**: 40+ test methods

**Key Features Tested**:
- Input validation
- Business rule enforcement
- Transaction management
- Error handling
- Boundary conditions
- Edge cases

---

### 2. EmployeeControllerTest.java
**Purpose**: Tests REST API endpoints and HTTP request/response handling

**Test Coverage**:
- â POST /api/employees (valid input, empty name, null badge ID, invalid role, insufficient permissions, unauthenticated, malformed JSON)
- â GET /api/employees/{id} (valid ID, non-existent ID, invalid ID format, worker accessing own record)
- â GET /api/employees (no filters, department filter, status filter, empty result, invalid page number, invalid page size)
- â PUT /api/employees/{id} (valid input, non-existent ID, insufficient permissions, empty request body)
- â DELETE /api/employees/{id} (valid ID, non-existent ID, HR role, insufficient permissions, invalid ID format)
- â GET /api/employees/badge/{badgeId} (valid badge ID, non-existent badge ID, empty badge ID)

**Test Count**: 50+ test methods

**Key Features Tested**:
- HTTP status codes (200, 201, 204, 400, 401, 403, 404)
- Request validation
- Response format
- Security annotations
- Role-based access control
- Content negotiation

---

### 3. EmployeeRepositoryTest.java
**Purpose**: Tests database operations and JPA queries

**Test Coverage**:
- â Save (valid employee, null name, duplicate badge ID)
- â Find by ID (existing ID, non-existent ID, null ID)
- â Find by Badge ID (existing, non-existent, null, empty)
- â Exists by Badge ID (existing, non-existent, null)
- â Exists by Badge ID and ID Not (different employee, same employee, non-existent)
- â Find by Department (existing, non-existent, null)
- â Find by Status (active, inactive, non-existent)
- â Find by Role (worker, supervisor, non-existent)
- â Find All with Specification (department spec, status spec, multiple specs, pagination)
- â Delete (existing employee, non-existent ID)
- â Count (all employees, with specification)
- â Update (existing employee, change status)

**Test Count**: 40+ test methods

**Key Features Tested**:
- CRUD operations
- Custom queries
- Specifications
- Pagination
- Unique constraints
- Database indexes
- Transaction management

---

### 4. EmployeeMapperTest.java
**Purpose**: Tests entity-to-DTO and DTO-to-entity conversions

**Test Coverage**:
- â Entity to DTO (valid entity, null entity, entity with null fields, minimal fields, long name, special characters)
- â DTO to Entity (valid DTO, null DTO, DTO with null fields, minimal fields, all roles, all statuses)
- â List Conversions (valid entity list, empty list, null list, valid DTO list)
- â Bidirectional Conversion (entityâDTOâentity, DTOâentityâDTO)
- â Edge Cases (future hire date, past hire date, empty strings, whitespace fields, zero ID, max long ID)

**Test Count**: 30+ test methods

**Key Features Tested**:
- Field mapping accuracy
- Null handling
- List conversions
- Bidirectional consistency
- Edge case handling
- Data integrity

---

### 5. SecurityConfigTest.java
**Purpose**: Tests security configuration, authentication, and authorization

**Test Coverage**:
- â Authentication (unauthenticated access, valid token, invalid token, missing header, malformed header)
- â Role-Based Access Control (ADMIN, HR, SUPERVISOR, WORKER, invalid role, multiple roles)
- â CSRF Protection (with token, without token, invalid token)
- â Method-Level Security (admin access, worker access)
- â Actuator Endpoints (health, info, metrics)
- â CORS (allowed origin)
- â Session Management (stateless)
- â HTTP Methods (GET, POST, PUT, PATCH, DELETE)
- â Edge Cases (empty username, email username, special characters username, anonymous user)

**Test Count**: 40+ test methods

**Key Features Tested**:
- JWT authentication
- OAuth2 support
- Role hierarchy
- Endpoint security
- CSRF protection
- Session management
- HTTP status codes (401, 403)

---

### 6. GlobalExceptionHandlerTest.java
**Purpose**: Tests centralized exception handling and error responses

**Test Coverage**:
- â NotFoundException (with message, empty message, null message)
- â IllegalArgumentException (invalid ID, null input, duplicate badge ID)
- â Validation Exceptions (empty name, null badge ID, invalid email, multiple errors, invalid role)
- â MethodArgumentNotValidException (field errors)
- â HttpMessageNotReadableException (malformed JSON, empty body, invalid date format)
- â MethodNotAllowedException (unsupported HTTP method)
- â UnsupportedMediaTypeException (text/plain, XML content)
- â Internal Server Error (unexpected exception, null pointer, database exception)
- â ConstraintViolationException (unique constraint)
- â TypeMismatchException (invalid ID type, non-numeric ID)
- â Error Response Format (timestamp, status, message, path)
- â Edge Cases (very long message, special characters in message)

**Test Count**: 40+ test methods

**Key Features Tested**:
- Exception mapping
- HTTP status codes
- Error response format
- Validation error details
- Stack trace handling
- Custom error messages

---

### 7. AuditAspectTest.java
**Purpose**: Tests aspect-oriented audit logging functionality

**Test Coverage**:
- â Aspect Execution (method execution, null args, empty args, exception handling)
- â Security Context (authenticated user, null authentication, anonymous user)
- â Auditable Annotation (on method, with action)
- â Audit Log Entry (timestamp, actor, action, entity type)
- â Before/After Values (capture before value, capture after value)
- â CRUD Operations (create, update, delete)
- â Edge Cases (multiple arguments, null result, large object, concurrent execution, special characters, performance impact)

**Test Count**: 30+ test methods

**Key Features Tested**:
- Aspect weaving
- Audit log creation
- Security context integration
- Before/after value capture
- Exception handling
- Thread safety
- Performance impact

---

## Running the Tests

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+ (for integration tests)
- Docker (optional, for TestContainers)

### Run All Tests
```bash
mvn clean test
```

### Run Specific Test Class
```bash
mvn test -Dtest=EmployeeServiceTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=EmployeeServiceTest#testCreateEmployee_ValidInput_Success
```

### Run Tests with Coverage Report
```bash
mvn clean test jacoco:report
```

### Run Integration Tests Only
```bash
mvn verify -P integration-tests
```

### Run Unit Tests Only
```bash
mvn test -P unit-tests
```

---

## Test Naming Convention

All tests follow the naming pattern:
```
test<MethodName>_<Scenario>_<ExpectedResult>
```

**Examples**:
- `testCreateEmployee_ValidInput_Success`
- `testGetEmployeeById_NonExistentId_ThrowsNotFoundException`
- `testUpdateEmployee_NullDTO_ThrowsException`

---

## Test Annotations Used

### JUnit 5
- `@Test` - Marks a test method
- `@BeforeEach` - Setup before each test
- `@AfterEach` - Cleanup after each test
- `@ExtendWith(MockitoExtension.class)` - Mockito integration

### Spring Boot Test
- `@SpringBootTest` - Full application context
- `@WebMvcTest` - Controller layer testing
- `@DataJpaTest` - Repository layer testing
- `@AutoConfigureMockMvc` - MockMvc configuration

### Spring Security Test
- `@WithMockUser` - Mock authenticated user
- `@WithAnonymousUser` - Mock anonymous user

### Mockito
- `@Mock` - Create mock object
- `@InjectMocks` - Inject mocks into test subject
- `@MockBean` - Spring Boot mock bean

---

## Assertions Used

### JUnit Assertions
- `assertEquals(expected, actual)` - Value equality
- `assertNotNull(object)` - Non-null check
- `assertNull(object)` - Null check
- `assertTrue(condition)` - Boolean true
- `assertFalse(condition)` - Boolean false
- `assertThrows(Exception.class, executable)` - Exception throwing
- `assertDoesNotThrow(executable)` - No exception

### MockMvc Assertions
- `status().isOk()` - HTTP 200
- `status().isCreated()` - HTTP 201
- `status().isNoContent()` - HTTP 204
- `status().isBadRequest()` - HTTP 400
- `status().isUnauthorized()` - HTTP 401
- `status().isForbidden()` - HTTP 403
- `status().isNotFound()` - HTTP 404
- `jsonPath("$.field", is(value))` - JSON response validation

---

## Test Data Setup

### Sample Employee
```java
Employee testEmployee = Employee.builder()
    .id(1L)
    .name("John Doe")
    .badgeId("EMP001")
    .role("WORKER")
    .department("Warehouse")
    .shiftGroup("Day Shift")
    .hireDate(LocalDate.of(2023, 1, 15))
    .status("ACTIVE")
    .build();
```

### Sample DTO
```java
EmployeeDTO testEmployeeDTO = EmployeeDTO.builder()
    .id(1L)
    .name("John Doe")
    .badgeId("EMP001")
    .role("WORKER")
    .department("Warehouse")
    .shiftGroup("Day Shift")
    .hireDate(LocalDate.of(2023, 1, 15))
    .status("ACTIVE")
    .build();
```

---

## Coverage Report

### Expected Coverage
- **Line Coverage**: 85%+
- **Branch Coverage**: 80%+
- **Method Coverage**: 90%+
- **Class Coverage**: 95%+

### Generate Coverage Report
```bash
mvn clean test jacoco:report
```

Report location: `target/site/jacoco/index.html`

---

## Continuous Integration

### GitHub Actions
```yaml
name: Run Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run tests
        run: mvn clean test
      - name: Generate coverage report
        run: mvn jacoco:report
```

---

## Best Practices Followed

â **Arrange-Act-Assert (AAA) Pattern**
- Clear separation of test phases
- Readable and maintainable tests

â **Test Independence**
- Each test can run independently
- No shared state between tests

â **Descriptive Test Names**
- Clear indication of what is being tested
- Easy to identify failing tests

â **Comprehensive Coverage**
- Normal cases
- Boundary conditions
- Edge cases
- Error scenarios

â **Mock External Dependencies**
- Isolated unit tests
- Fast execution
- Predictable results

â **Integration Tests**
- Real database interactions
- End-to-end scenarios
- TestContainers for isolation

---

## Troubleshooting

### Tests Failing Due to Database Connection
**Solution**: Ensure PostgreSQL is running or use TestContainers
```bash
docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=password postgres:14
```

### Tests Failing Due to Security Context
**Solution**: Use `@WithMockUser` annotation
```java
@Test
@WithMockUser(roles = "ADMIN")
void testMethod() {
    // Test code
}
```

### Tests Failing Due to Missing Dependencies
**Solution**: Run Maven install
```bash
mvn clean install
```

### Slow Test Execution
**Solution**: Run tests in parallel
```bash
mvn test -T 4
```

---

## Future Enhancements

- [ ] Add performance tests
- [ ] Add load tests
- [ ] Add mutation testing
- [ ] Add contract tests
- [ ] Add end-to-end tests
- [ ] Add visual regression tests
- [ ] Increase coverage to 95%+

---

## Contributing

When adding new tests:
1. Follow the naming convention
2. Include normal, boundary, and edge cases
3. Add descriptive comments
4. Update this README
5. Ensure all tests pass
6. Maintain coverage above 85%

---

## License

This test suite is part of the Warehouse EMS project.

---

## Contact

For questions or issues, please contact the development team.

---

**Last Updated**: 2026-01-15
**Version**: 1.0.0
**Status**: â All Tests Passing