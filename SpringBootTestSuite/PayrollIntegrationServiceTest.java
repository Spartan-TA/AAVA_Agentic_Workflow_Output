public class PayrollIntegrationServiceTest {

    @Test
    public void testValidPayrollProcessing() {
        // Arrange
        PayrollIntegrationService service = new PayrollIntegrationService();
        Employee employee = new Employee("John Doe", "Engineering");
        Payroll payroll = new Payroll(employee, 5000.00);

        // Act
        boolean result = service.processPayroll(payroll);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testNullPayrollProcessing() {
        // Arrange
        PayrollIntegrationService service = new PayrollIntegrationService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.processPayroll(null));
    }

    @Test
    public void testNegativeSalaryPayrollProcessing() {
        // Arrange
        PayrollIntegrationService service = new PayrollIntegrationService();
        Employee employee = new Employee("Jane Doe", "HR");
        Payroll payroll = new Payroll(employee, -1000.00);

        // Act & Assert
        assertThrows(InvalidSalaryException.class, () -> service.processPayroll(payroll));
    }
}