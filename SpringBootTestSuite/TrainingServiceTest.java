public class TrainingServiceTest {

    @Test
    public void testValidTrainingSessionCreation() {
        // Arrange
        TrainingService service = new TrainingService();
        TrainingSession session = new TrainingSession("Java Basics", LocalDate.now(), "John Doe");

        // Act
        boolean result = service.createTrainingSession(session);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testNullTrainingSessionCreation() {
        // Arrange
        TrainingService service = new TrainingService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.createTrainingSession(null));
    }

    @Test
    public void testDuplicateTrainingSessionCreation() {
        // Arrange
        TrainingService service = new TrainingService();
        TrainingSession session = new TrainingSession("Java Basics", LocalDate.now(), "John Doe");
        service.createTrainingSession(session);

        // Act & Assert
        assertThrows(DuplicateSessionException.class, () -> service.createTrainingSession(session));
    }
}