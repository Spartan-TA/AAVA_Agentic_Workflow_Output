public class TaskServiceTest {

    @Test
    public void testValidTaskCreation() {
        // Arrange
        TaskService service = new TaskService();
        Task task = new Task("Complete Report", "John Doe", LocalDate.now().plusDays(3));

        // Act
        boolean result = service.createTask(task);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testNullTaskCreation() {
        // Arrange
        TaskService service = new TaskService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.createTask(null));
    }

    @Test
    public void testPastDueDateTaskCreation() {
        // Arrange
        TaskService service = new TaskService();
        Task task = new Task("Submit Assignment", "Jane Doe", LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(InvalidDueDateException.class, () -> service.createTask(task));
    }
}