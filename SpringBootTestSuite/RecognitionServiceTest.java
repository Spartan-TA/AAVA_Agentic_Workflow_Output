public class RecognitionServiceTest {

    @Test
    public void testValidRecognitionSubmission() {
        // Arrange
        RecognitionService service = new RecognitionService();
        Recognition recognition = new Recognition("John Doe", "Employee of the Month", LocalDate.now());

        // Act
        boolean result = service.submitRecognition(recognition);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testNullRecognitionSubmission() {
        // Arrange
        RecognitionService service = new RecognitionService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.submitRecognition(null));
    }

    @Test
    public void testDuplicateRecognitionSubmission() {
        // Arrange
        RecognitionService service = new RecognitionService();
        Recognition recognition = new Recognition("Jane Doe", "Best Team Player", LocalDate.now());
        service.submitRecognition(recognition);

        // Act & Assert
        assertThrows(DuplicateRecognitionException.class, () -> service.submitRecognition(recognition));
    }
}