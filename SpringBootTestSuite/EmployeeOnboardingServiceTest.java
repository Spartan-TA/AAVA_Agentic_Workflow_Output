public class EmployeeOnboardingServiceTest {

    @Test
    public void testValidEmployeeOnboarding() {
        // Arrange
        EmployeeOnboardingService service = new EmployeeOnboardingService();
        Employee employee = new Employee("John Doe", "Engineering");

        // Act
        boolean result = service.onboardEmployee(employee);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testNullEmployeeOnboarding() {
        // Arrange
        EmployeeOnboardingService service = new EmployeeOnboardingService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.onboardEmployee(null));
    }

    @Test
    public void testDuplicateEmployeeOnboarding() {
        // Arrange
        EmployeeOnboardingService service = new EmployeeOnboardingService();
        Employee employee = new Employee("Jane Doe", "HR");
        service.onboardEmployee(employee);

        // Act & Assert
        assertThrows(DuplicateEmployeeException.class, () -> service.onboardEmployee(employee));
    }
}