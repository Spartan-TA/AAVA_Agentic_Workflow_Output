public class EmployeeMasterDataServiceTest {

    @Test
    public void testCreateEmployee_ValidInput() {
        // Arrange
        EmployeeService service = new EmployeeService();
        EmployeeDTO employee = new EmployeeDTO("John Doe", "12345", "Worker", "Logistics", "A", LocalDate.now(), "Active");

        // Act
        Employee result = service.createEmployee(employee);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("12345", result.getBadgeId());
    }

    @Test
    public void testCreateEmployee_NullInput() {
        // Arrange
        EmployeeService service = new EmployeeService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.createEmployee(null));
    }

    @Test
    public void testGetEmployeeById_ValidId() {
        // Arrange
        EmployeeService service = new EmployeeService();
        String validId = "12345";

        // Act
        Employee result = service.getEmployeeById(validId);

        // Assert
        assertNotNull(result);
        assertEquals(validId, result.getBadgeId());
    }

    @Test
    public void testGetEmployeeById_InvalidId() {
        // Arrange
        EmployeeService service = new EmployeeService();
        String invalidId = "99999";

        // Act & Assert
        assertThrows(EmployeeNotFoundException.class, () -> service.getEmployeeById(invalidId));
    }

    @Test
    public void testUpdateEmployee_ValidInput() {
        // Arrange
        EmployeeService service = new EmployeeService();
        String badgeId = "12345";
        EmployeeDTO updatedEmployee = new EmployeeDTO("Jane Doe", badgeId, "Supervisor", "Operations", "B", LocalDate.now(), "Active");

        // Act
        Employee result = service.updateEmployee(badgeId, updatedEmployee);

        // Assert
        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        assertEquals("Supervisor", result.getRole());
    }

    @Test
    public void testDeleteEmployee_ValidId() {
        // Arrange
        EmployeeService service = new EmployeeService();
        String badgeId = "12345";

        // Act
        boolean result = service.deleteEmployee(badgeId);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testDeleteEmployee_InvalidId() {
        // Arrange
        EmployeeService service = new EmployeeService();
        String invalidId = "99999";

        // Act & Assert
        assertThrows(EmployeeNotFoundException.class, () -> service.deleteEmployee(invalidId));
    }
}