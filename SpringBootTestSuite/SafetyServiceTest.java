package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;

public class SafetyServiceTest {
    @Mock
    private SafetyRepository safetyRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private SafetyService safetyService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testRecordIncident_Valid_Success() {
        SafetyIncident incident = new SafetyIncident(1L, "Slip", "High", "Dock", "John Doe", new Date(), "OPEN");
        when(safetyRepository.save(any(SafetyIncident.class))).thenReturn(incident);
        SafetyIncident result = safetyService.recordIncident(incident);
        assertNotNull(result);
        assertEquals("Slip", result.getDescription());
    }

    @Test
    void testRecordIncident_NullDescription_ThrowsException() {
        SafetyIncident incident = new SafetyIncident(1L, null, "High", "Dock", "John Doe", new Date(), "OPEN");
        assertThrows(InvalidIncidentException.class, () -> safetyService.recordIncident(incident));
    }

    @Test
    void testGetIncidentById_Valid_Success() {
        SafetyIncident incident = new SafetyIncident(1L, "Slip", "High", "Dock", "John Doe", new Date(), "OPEN");
        when(safetyRepository.findById(1L)).thenReturn(Optional.of(incident));
        SafetyIncident result = safetyService.getIncidentById(1L);
        assertNotNull(result);
        assertEquals("Slip", result.getDescription());
    }

    @Test
    void testGetIncidentById_InvalidId_ThrowsException() {
        when(safetyRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IncidentNotFoundException.class, () -> safetyService.getIncidentById(99L));
    }

    @Test
    void testUpdateIncidentStatus_Valid_Success() {
        SafetyIncident incident = new SafetyIncident(1L, "Slip", "High", "Dock", "John Doe", new Date(), "OPEN");
        when(safetyRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(safetyRepository.save(any(SafetyIncident.class))).thenReturn(incident);
        SafetyIncident result = safetyService.updateIncidentStatus(1L, "RESOLVED");
        assertEquals("RESOLVED", result.getStatus());
    }

    @Test
    void testUpdateIncidentStatus_InvalidStatus_ThrowsException() {
        SafetyIncident incident = new SafetyIncident(1L, "Slip", "High", "Dock", "John Doe", new Date(), "OPEN");
        when(safetyRepository.findById(1L)).thenReturn(Optional.of(incident));
        assertThrows(InvalidIncidentStatusException.class, () -> safetyService.updateIncidentStatus(1L, "INVALID"));
    }

    @Test
    void testGenerateOSHAReport_Valid_Success() {
        List<SafetyIncident> incidents = Arrays.asList(
            new SafetyIncident(1L, "Slip", "High", "Dock", "John Doe", new Date(), "RESOLVED"),
            new SafetyIncident(2L, "Fall", "Medium", "Warehouse", "Jane Doe", new Date(), "RESOLVED")
        );
        when(safetyRepository.findAllResolved()).thenReturn(incidents);
        OSHAReport report = safetyService.generateOSHAReport();
        assertNotNull(report);
        assertEquals(2, report.getIncidents().size());
    }

    // Integration scenario: Incident triggers corrective action workflow
    @Test
    void testIncidentTriggersCorrectiveActionWorkflow_Success() {
        SafetyIncident incident = new SafetyIncident(1L, "Slip", "High", "Dock", "John Doe", new Date(), "OPEN");
        when(safetyRepository.findById(1L)).thenReturn(Optional.of(incident));
        doNothing().when(correctiveActionService).startWorkflow(incident);
        safetyService.startCorrectiveActionWorkflow(1L);
        verify(correctiveActionService).startWorkflow(incident);
    }
}
