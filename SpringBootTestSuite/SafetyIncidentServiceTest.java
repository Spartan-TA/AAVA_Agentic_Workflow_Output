import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class SafetyIncidentServiceTest {
    private SafetyIncidentService service;

    @BeforeEach
    public void setUp() {
        service = new SafetyIncidentService();
    }

    @Test
    public void testRecordIncident_Valid() {
        SafetyIncident incident = new SafetyIncident("emp1", "Slip and fall", "High", "2024-07-01");
        assertDoesNotThrow(() -> service.recordIncident(incident));
    }

    @Test
    public void testRecordIncident_NullDescription() {
        SafetyIncident incident = new SafetyIncident("emp2", null, "Medium", "2024-07-02");
        assertThrows(IllegalArgumentException.class, () -> service.recordIncident(incident));
    }

    @Test
    public void testRecordIncident_InvalidSeverity() {
        SafetyIncident incident = new SafetyIncident("emp3", "Minor cut", "Extreme", "2024-07-03");
        assertThrows(InvalidSeverityException.class, () -> service.recordIncident(incident));
    }

    @Test
    public void testUpdateIncidentStatus_ValidTransition() {
        SafetyIncident incident = new SafetyIncident("emp4", "Burn", "High", "2024-07-04");
        service.recordIncident(incident);
        assertTrue(service.updateIncidentStatus(incident.getId(), "Investigating"));
    }

    @Test
    public void testUpdateIncidentStatus_InvalidTransition() {
        SafetyIncident incident = new SafetyIncident("emp5", "Fall", "Medium", "2024-07-05");
        service.recordIncident(incident);
        assertThrows(InvalidStatusTransitionException.class, () -> service.updateIncidentStatus(incident.getId(), "Closed"));
    }

    @Test
    public void testAssignInvestigator_Valid() {
        SafetyIncident incident = new SafetyIncident("emp6", "Chemical spill", "High", "2024-07-06");
        service.recordIncident(incident);
        assertTrue(service.assignInvestigator(incident.getId(), "investigator1"));
    }

    @Test
    public void testGenerateOSHAReport_Valid() {
        SafetyIncident incident = new SafetyIncident("emp7", "Forklift accident", "High", "2024-07-07");
        service.recordIncident(incident);
        assertNotNull(service.generateOSHAReport(incident.getId()));
    }

    @Test
    public void testGetIncidentsBySeverity_Valid() {
        assertNotNull(service.getIncidentsBySeverity("High"));
    }

    @Test
    public void testGetIncidentsBySeverity_InvalidSeverity() {
        assertThrows(InvalidSeverityException.class, () -> service.getIncidentsBySeverity("Extreme"));
    }

    @Test
    public void testGetIncidentsByDateRange_Valid() {
        assertNotNull(service.getIncidentsByDateRange("2024-07-01", "2024-07-31"));
    }
}