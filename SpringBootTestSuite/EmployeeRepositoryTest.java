public class EmployeeRepositoryTest {

    @Test
    public void testFindByBadgeIdValidId() {
        // Arrange
        EmployeeRepository repository = new EmployeeRepository();
        String validId = "12345";

        // Act
        Optional<Employee> result = repository.findByBadgeId(validId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("12345", result.get().getBadgeId());
    }

    @Test
    public void testFindByBadgeIdInvalidId() {
        // Arrange
        EmployeeRepository repository = new EmployeeRepository();
        String invalidId = "99999";

        // Act
        Optional<Employee> result = repository.findByBadgeId(invalidId);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    public void testSaveEmployeeValidInput() {
        // Arrange
        EmployeeRepository repository = new EmployeeRepository();
        Employee input = new Employee("John Doe", "12345", "Worker", "Warehouse", "A", LocalDate.now(), "Active");

        // Act
        Employee savedEmployee = repository.save(input);

        // Assert
        assertNotNull(savedEmployee);
        assertEquals("John Doe", savedEmployee.getName());
    }

    @Test
    public void testSaveEmployeeNullInput() {
        // Arrange
        EmployeeRepository repository = new EmployeeRepository();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }

    @BeforeEach
    public void setup() {
        // Initialize mock dependencies and setup test data
    }

    @AfterEach
    public void teardown() {
        // Cleanup resources
    }
}