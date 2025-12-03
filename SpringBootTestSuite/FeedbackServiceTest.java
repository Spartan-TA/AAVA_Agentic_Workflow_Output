public class FeedbackServiceTest {

    @Test
    public void testValidFeedbackSubmission() {
        // Arrange
        FeedbackService service = new FeedbackService();
        Feedback feedback = new Feedback("John Doe", "Great service!");

        // Act
        boolean result = service.submitFeedback(feedback);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testNullFeedbackSubmission() {
        // Arrange
        FeedbackService service = new FeedbackService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.submitFeedback(null));
    }

    @Test
    public void testEmptyFeedbackMessage() {
        // Arrange
        FeedbackService service = new FeedbackService();
        Feedback feedback = new Feedback("Jane Doe", "");

        // Act & Assert
        assertThrows(InvalidFeedbackException.class, () -> service.submitFeedback(feedback));
    }
}