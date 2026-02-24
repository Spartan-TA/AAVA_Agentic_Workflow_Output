package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.time.*;

class SafetyIncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;
    @Mock
    private CorrectiveActionRepository correctiveActionRepository;
    @InjectMocks
    private SafetyIncidentService safetyIncidentService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void testReportIncident_Valid() {
        Incident incident = new Incident("HIGH", "Warehouse", "Spill");
        when(incidentRepository.save(any(Incident.class))).thenReturn(incident);
        Incident result = safetyIncidentService.reportIncident("HIGH", "Warehouse", "Spill");
        assertNotNull(result);
        assertEquals("HIGH", result.getSeverity());
    }

    @Test
    void testReportIncident_NullSeverity() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            safetyIncidentService.reportIncident(null, "Warehouse", "Spill"));
        assertEquals("Severity cannot be null", ex.getMessage());
    }

    @Test
    void testReportIncident_EmptyDescription() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            safetyIncidentService.reportIncident("LOW", "Office", ""));
        assertEquals("Description cannot be empty", ex.getMessage());
    }

    @Test
    void testUpdateIncidentStatus_InvalidTransition() {
        Incident incident = new Incident("MEDIUM", "Lab", "Leak");
        incident.setStatus("CLOSED");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        Exception ex = assertThrows(IllegalStateException.class, () ->
            safetyIncidentService.updateIncidentStatus(1L, "OPEN"));
        assertEquals("Cannot reopen a closed incident", ex.getMessage());
    }

    @Test
    void testExportOshaReport_NoData() {
        when(incidentRepository.findByDateRange(any(), any())).thenReturn(Collections.emptyList());
        List<OshaReport> reports = safetyIncidentService.exportOshaReport(LocalDate.now(), LocalDate.now());
        assertTrue(reports.isEmpty());
    }

    @Test
    void testAddCorrectiveAction_NonExistentIncident() {
        when(incidentRepository.findById(99L)).thenReturn(Optional.empty());
        Exception ex = assertThrows(NoSuchElementException.class, () ->
            safetyIncidentService.addCorrectiveAction(99L, "Fix signage"));
        assertEquals("Incident not found", ex.getMessage());
    }
}