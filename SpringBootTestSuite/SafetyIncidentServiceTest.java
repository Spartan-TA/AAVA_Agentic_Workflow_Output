import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SafetyIncidentServiceTest {
    @Mock
    private SafetyIncidentRepository safetyIncidentRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private SafetyIncidentService safetyIncidentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testReportIncident_Valid() {
        SafetyIncident incident = new SafetyIncident();
        incident.setIncidentDate(LocalDateTime.now());
        incident.setLocation("Warehouse A");
        incident.setSeverity("MINOR");
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(incident);
        SafetyIncident result = safetyIncidentService.reportIncident(incident);
        assertNotNull(result);
        assertEquals("OPEN", result.getInvestigationStatus());
    }

    @Test
    void testReportIncident_NullInput() {
        assertThrows(ValidationException.class, () -> safetyIncidentService.reportIncident(null));
    }

    @Test
    void testUpdateInvestigationStatus_Valid() {
        SafetyIncident incident = new SafetyIncident();
        incident.setId(1L);
        incident.setInvestigationStatus("OPEN");
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(incident);
        SafetyIncident result = safetyIncidentService.updateInvestigationStatus(1L, "INVESTIGATING");
        assertNotNull(result);
        assertEquals("INVESTIGATING", result.getInvestigationStatus());
    }

    @Test
    void testUpdateInvestigationStatus_IncidentNotFound() {
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> safetyIncidentService.updateInvestigationStatus(1L, "INVESTIGATING"));
    }

    @Test
    void testMarkAsOSHAReportable_Valid() {
        SafetyIncident incident = new SafetyIncident();
        incident.setId(1L);
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(incident);
        SafetyIncident result = safetyIncidentService.markAsOSHAReportable(1L);
        assertNotNull(result);
        assertTrue(result.getOshaReportable());
    }
}