public class OffboardingServiceTest {

    @Test
    public void testValidEmployeeOffboarding() {
        // Arrange
        OffboardingService service = new OffboardingService();
        Employee employee = new Employee("John Doe", "Engineering");

        // Act
        boolean result = service.offboardEmployee(employee);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testNullEmployeeOffboarding() {
        // Arrange
        OffboardingService service = new OffboardingService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.offboardEmployee(null));
    }

    @Test
    public void testNonExistentEmployeeOffboarding() {
        // Arrange
        OffboardingService service = new OffboardingService();
        Employee employee = new Employee("Jane Doe", "HR");

        // Act & Assert
        assertThrows(EmployeeNotFoundException.class, () -> service.offboardEmployee(employee));
    }
}