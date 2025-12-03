public class CommunicationServiceTest {

    @Test
    public void testValidMessageSending() {
        // Arrange
        CommunicationService service = new CommunicationService();
        Message message = new Message("John Doe", "Hello, team!");

        // Act
        boolean result = service.sendMessage(message);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testNullMessageSending() {
        // Arrange
        CommunicationService service = new CommunicationService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.sendMessage(null));
    }

    @Test
    public void testEmptyMessageContent() {
        // Arrange
        CommunicationService service = new CommunicationService();
        Message message = new Message("Jane Doe", "");

        // Act & Assert
        assertThrows(InvalidMessageException.class, () -> service.sendMessage(message));
    }
}