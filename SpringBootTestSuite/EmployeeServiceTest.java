public class EmployeeServiceTest {

    @BeforeEach
    public void setUp() {
        // Initialize mocks and test data
    }

    @Test
    public void testCreateEmployeeValidInput() {
        // Arrange
        EmployeeRequest request = new EmployeeRequest("John", "Doe", "john.doe@example.com", "1234567890", "12345", "WORKER", "ACTIVE", LocalDate.now(), null, 1L, null, 1L);
        // Act
        EmployeeResponse response = employeeService.createEmployee(request);
        // Assert
        assertNotNull(response);
        assertEquals("John", response.getFirstName());
    }

    @Test
    public void testCreateEmployeeInvalidEmail() {
        // Arrange
        EmployeeRequest request = new EmployeeRequest("John", "Doe", "invalid-email", "1234567890", "12345", "WORKER", "ACTIVE", LocalDate.now(), null, 1L, null, 1L);
        // Act & Assert
        assertThrows(ValidationException.class, () -> employeeService.createEmployee(request));
    }

    @Test
    public void testGetEmployeeById() {
        // Arrange
        Long employeeId = 1L;
        // Act
        EmployeeResponse response = employeeService.getEmployeeById(employeeId);
        // Assert
        assertNotNull(response);
        assertEquals(employeeId, response.getId());
    }

    @Test
    public void testSoftDeleteEmployee() {
        // Arrange
        Long employeeId = 1L;
        // Act
        employeeService.softDeleteEmployee(employeeId);
        // Assert
        EmployeeResponse response = employeeService.getEmployeeById(employeeId);
        assertEquals("INACTIVE", response.getStatus());
    }

    @Test
    public void testPagination() {
        // Arrange
        int page = 0;
        int size = 10;
        // Act
        Page<EmployeeResponse> employees = employeeService.getEmployees(page, size);
        // Assert
        assertNotNull(employees);
        assertEquals(size, employees.getSize());
    }

    @AfterEach
    public void tearDown() {
        // Clean up resources
    }
}