public class AnalyticsServiceTest {

    @Test
    public void testValidDataAnalysis() {
        // Arrange
        AnalyticsService service = new AnalyticsService();
        DataSet dataSet = new DataSet("Sales Data", List.of(100, 200, 300));

        // Act
        AnalysisResult result = service.analyzeData(dataSet);

        // Assert
        assertNotNull(result);
        assertEquals("Sales Data", result.getDataSetName());
    }

    @Test
    public void testNullDataSetAnalysis() {
        // Arrange
        AnalyticsService service = new AnalyticsService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.analyzeData(null));
    }

    @Test
    public void testEmptyDataSetAnalysis() {
        // Arrange
        AnalyticsService service = new AnalyticsService();
        DataSet dataSet = new DataSet("Empty Data", List.of());

        // Act & Assert
        assertThrows(InvalidDataSetException.class, () -> service.analyzeData(dataSet));
    }
}