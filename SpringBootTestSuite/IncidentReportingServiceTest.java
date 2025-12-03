public class IncidentReportingServiceTest {

    @Test
    public void testValidIncidentReport() {
        // Arrange
        IncidentReportingService service = new IncidentReportingService();
        Incident incident = new Incident("Fire in warehouse", "John Doe", LocalDate.now());

        // Act
        boolean result = service.reportIncident(incident);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testNullIncidentReport() {
        // Arrange
        IncidentReportingService service = new IncidentReportingService();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.reportIncident(null));
    }

    @Test
    public void testDuplicateIncidentReport() {
        // Arrange
        IncidentReportingService service = new IncidentReportingService();
        Incident incident = new Incident("Power outage", "Jane Doe", LocalDate.now());
        service.reportIncident(incident);

        // Act & Assert
        assertThrows(DuplicateIncidentException.class, () -> service.reportIncident(incident));
    }
}