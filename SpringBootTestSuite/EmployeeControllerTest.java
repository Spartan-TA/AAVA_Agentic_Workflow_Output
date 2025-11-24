public class EmployeeControllerTest {

    @Test
    public void testCreateEmployeeValidInput() {
        // Arrange
        EmployeeController controller = new EmployeeController();
        EmployeeDTO input = new EmployeeDTO("John Doe", "12345", "Worker", "Warehouse", "A", LocalDate.now(), "Active");

        // Act
        ResponseEntity<EmployeeDTO> response = controller.createEmployee(input);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John Doe", response.getBody().getName());
    }

    @Test
    public void testCreateEmployeeNullInput() {
        // Arrange
        EmployeeController controller = new EmployeeController();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> controller.createEmployee(null));
    }

    @Test
    public void testGetEmployeeByIdValidId() {
        // Arrange
        EmployeeController controller = new EmployeeController();
        String validId = "12345";

        // Act
        ResponseEntity<EmployeeDTO> response = controller.getEmployeeById(validId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("12345", response.getBody().getBadgeId());
    }

    @Test
    public void testGetEmployeeByIdInvalidId() {
        // Arrange
        EmployeeController controller = new EmployeeController();
        String invalidId = "99999";

        // Act & Assert
        assertThrows(EmployeeNotFoundException.class, () -> controller.getEmployeeById(invalidId));
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