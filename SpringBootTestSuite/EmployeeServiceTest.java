public class EmployeeServiceTest {
    @Test
    public void testCreateEmployeeWithValidData() {
        // Arrange
        EmployeeService service = new EmployeeService();
        EmployeeRequest request = new EmployeeRequest("John", "Doe", "john.doe@example.com", "HR", "ACTIVE");

        // Act
        EmployeeResponse response = service.createEmployee(request);

        // Assert
        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
    }

    @Test
    public void testCreateEmployeeWithNullData() {
        // Arrange
        EmployeeService service = new EmployeeService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.createEmployee(null));
    }

    @Test
    public void testUpdateEmployeeWithValidData() {
        // Arrange
        EmployeeService service = new EmployeeService();
        EmployeeRequest request = new EmployeeRequest("Jane", "Smith", "jane.smith@example.com", "Finance", "ACTIVE");

        // Act
        EmployeeResponse response = service.updateEmployee(1L, request);

        // Assert
        assertNotNull(response);
        assertEquals("Jane", response.getFirstName());
        assertEquals("Smith", response.getLastName());
    }

    @Test
    public void testUpdateEmployeeWithInvalidId() {
        // Arrange
        EmployeeService service = new EmployeeService();
        EmployeeRequest request = new EmployeeRequest("Jane", "Smith", "jane.smith@example.com", "Finance", "ACTIVE");

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> service.updateEmployee(-1L, request));
    }

    @Test
    public void testDeleteEmployeeWithValidId() {
        // Arrange
        EmployeeService service = new EmployeeService();

        // Act
        service.deleteEmployee(1L);

        // Assert
        assertTrue(service.isDeleted(1L));
    }

    @Test
    public void testDeleteEmployeeWithInvalidId() {
        // Arrange
        EmployeeService service = new EmployeeService();

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> service.deleteEmployee(-1L));
    }
}