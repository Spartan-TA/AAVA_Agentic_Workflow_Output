public class PerformanceReviewServiceTest {

    @Test
    public void testValidPerformanceReview() {
        // Arrange
        PerformanceReviewService service = new PerformanceReviewService();
        Employee employee = new Employee("John Doe", "Engineering");
        PerformanceReview review = new PerformanceReview(employee, "Excellent", "Keep up the good work");

        // Act
        boolean result = service.submitReview(review);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testNullPerformanceReview() {
        // Arrange
        PerformanceReviewService service = new PerformanceReviewService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.submitReview(null));
    }

    @Test
    public void testInvalidRatingPerformanceReview() {
        // Arrange
        PerformanceReviewService service = new PerformanceReviewService();
        Employee employee = new Employee("Jane Doe", "HR");
        PerformanceReview review = new PerformanceReview(employee, "Terrible", "Needs improvement");

        // Act & Assert
        assertThrows(InvalidRatingException.class, () -> service.submitReview(review));
    }
}