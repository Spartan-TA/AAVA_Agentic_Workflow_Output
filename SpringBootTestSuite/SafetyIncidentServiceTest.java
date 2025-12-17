package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * SafetyIncidentServiceTest - Comprehensive unit tests for SafetyIncidentService covering recording, workflow, OSHA, boundaries, and edge cases.
 */
public class SafetyIncidentServiceTest {
    private SafetyIncidentService incidentService;

    @BeforeEach
    public void setUp() {
        incidentService = new SafetyIncidentService();
    }

    @Test
    public void testRecordIncidentValid() {
        SafetyIncident incident = new SafetyIncident("Slip", "Warehouse A", "Open", "Minor injury");
        assertDoesNotThrow(() -> incidentService.recordIncident(incident));
    }

    @Test
    public void testRecordIncidentInvalidInput() {
        SafetyIncident incident = new SafetyIncident("", "", "", null);
        assertThrows(IllegalArgumentException.class, () -> incidentService.recordIncident(incident));
    }

    @Test
    public void testGetIncidentByIdValid() {
        int id = 1;
        SafetyIncident incident = incidentService.getIncidentById(id);
        assertNotNull(incident);
    }

    @Test
    public void testGetIncidentByIdInvalid() {
        int id = -1;
        SafetyIncident incident = incidentService.getIncidentById(id);
        assertNull(incident);
    }

    @Test
    public void testUpdateIncidentStatusOpenToInvestigating() {
        int id = 2;
        assertTrue(incidentService.updateIncidentStatus(id, "Investigating"));
    }

    @Test
    public void testUpdateIncidentStatusInvalidStatus() {
        int id = 2;
        assertFalse(incidentService.updateIncidentStatus(id, "Unknown"));
    }

    @Test
    public void testAssignInvestigator() {
        int incidentId = 3;
        int investigatorId = 101;
        assertTrue(incidentService.assignInvestigator(incidentId, investigatorId));
    }

    @Test
    public void testAddInvestigationNotes() {
        int incidentId = 4;
        String notes = "Reviewed CCTV footage.";
        assertTrue(incidentService.addInvestigationNotes(incidentId, notes));
    }

    @Test
    public void testGenerateOSHA300Report() {
        String report = incidentService.generateOSHA300Report();
        assertNotNull(report);
    }

    @Test
    public void testGenerateOSHA300AReport() {
        String report = incidentService.generateOSHA300AReport();
        assertNotNull(report);
    }

    @Test
    public void testGetIncidentsBySeverity() {
        String severity = "High";
        List<SafetyIncident> incidents = incidentService.getIncidentsBySeverity(severity);
        assertNotNull(incidents);
    }

    @Test
    public void testGetIncidentsByLocation() {
        String location = "Warehouse B";
        List<SafetyIncident> incidents = incidentService.getIncidentsByLocation(location);
        assertNotNull(incidents);
    }

    @Test
    public void testCloseIncident() {
        int incidentId = 5;
        assertTrue(incidentService.closeIncident(incidentId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Low", "Medium", "High"})
    public void testBoundarySeverityLevels(String severity) {
        List<SafetyIncident> incidents = incidentService.getIncidentsBySeverity(severity);
        assertNotNull(incidents);
    }

    @Test
    public void testNullDescriptions() {
        SafetyIncident incident = new SafetyIncident("Fall", "Warehouse C", "Open", null);
        assertThrows(IllegalArgumentException.class, () -> incidentService.recordIncident(incident));
    }

    @Test
    public void testInvalidStatuses() {
        int id = 6;
        assertThrows(IllegalArgumentException.class, () -> incidentService.updateIncidentStatus(id, "INVALID"));
    }
}
