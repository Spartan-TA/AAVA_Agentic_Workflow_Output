public class SelfServicePortalServiceTest {

    @Test
    public void testValidPortalAccess() {
        // Arrange
        SelfServicePortalService service = new SelfServicePortalService();
        User user = new User("John Doe", "john.doe@example.com");

        // Act
        boolean result = service.grantAccess(user);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testNullUserPortalAccess() {
        // Arrange
        SelfServicePortalService service = new SelfServicePortalService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.grantAccess(null));
    }

    @Test
    public void testUnauthorizedUserPortalAccess() {
        // Arrange
        SelfServicePortalService service = new SelfServicePortalService();
        User user = new User("Jane Doe", "jane.doe@example.com");

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () -> service.grantAccess(user));
    }
}