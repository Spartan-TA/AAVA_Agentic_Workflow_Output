public class SafetyComplianceServiceTest {

    @Test
    public void testValidSafetyCheck() {
        // Arrange
        SafetyComplianceService service = new SafetyComplianceService();
        SafetyCheck check = new SafetyCheck("Fire Drill", LocalDate.now());

        // Act
        boolean result = service.performSafetyCheck(check);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testNullSafetyCheck() {
        // Arrange
        SafetyComplianceService service = new SafetyComplianceService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.performSafetyCheck(null));
    }

    @Test
    public void testDuplicateSafetyCheck() {
        // Arrange
        SafetyComplianceService service = new SafetyComplianceService();
        SafetyCheck check = new SafetyCheck("Fire Drill", LocalDate.now());
        service.performSafetyCheck(check);

        // Act & Assert
        assertThrows(DuplicateSafetyCheckException.class, () -> service.performSafetyCheck(check));
    }
}