import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

public class SafetyServiceTest {
    @Mock
    private SafetyIncidentRepository safetyIncidentRepository;

    @InjectMocks
    private SafetyService safetyService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRecordIncident_ValidInput() {
        SafetyIncident incident = new SafetyIncident("EMP123", "Forklift accident", "HIGH", "OPEN");
        when(safetyIncidentRepository.save(any())).thenReturn(incident);
        SafetyIncident result = safetyService.recordIncident(incident);
        assertEquals("EMP123", result.getEmployeeId());
        assertEquals("HIGH", result.getSeverity());
    }

    @Test
    public void testRecordIncident_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> safetyService.recordIncident(null));
    }

    @Test
    public void testUpdateIncidentStatus_Workflow() {
        SafetyIncident incident = new SafetyIncident("EMP123", "Forklift accident", "HIGH", "OPEN");
        when(safetyIncidentRepository.save(any())).thenReturn(incident);
        SafetyIncident investigating = safetyService.updateStatus(incident, "INVESTIGATING");
        assertEquals("INVESTIGATING", investigating.getStatus());
        SafetyIncident resolved = safetyService.updateStatus(incident, "RESOLVED");
        assertEquals("RESOLVED", resolved.getStatus());
    }

    @Test
    public void testUpdateIncidentStatus_InvalidStatus() {
        SafetyIncident incident = new SafetyIncident("EMP123", "Forklift accident", "HIGH", "OPEN");
        assertThrows(IllegalArgumentException.class, () -> safetyService.updateStatus(incident, "INVALID"));
    }

    @Test
    public void testGenerateOSHAReport_ValidIncidents() {
        SafetyIncident inc1 = new SafetyIncident("EMP123", "Forklift accident", "HIGH", "RESOLVED");
        SafetyIncident inc2 = new SafetyIncident("EMP124", "Slip", "LOW", "RESOLVED");
        when(safetyIncidentRepository.findAllResolved()).thenReturn(Arrays.asList(inc1, inc2));
        OSHAReport report = safetyService.generateOSHAReport();
        assertNotNull(report);
        assertEquals(2, report.getIncidentCount());
    }

    @Test
    public void testSeverityLevelHandling_BoundaryConditions() {
        SafetyIncident inc = new SafetyIncident("EMP123", "Minor cut", "LOW", "OPEN");
        assertEquals("LOW", inc.getSeverity());
        inc.setSeverity("CRITICAL");
        assertEquals("CRITICAL", inc.getSeverity());
    }
}
