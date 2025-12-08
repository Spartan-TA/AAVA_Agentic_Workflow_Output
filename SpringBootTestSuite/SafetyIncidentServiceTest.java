import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;

public class SafetyIncidentServiceTest {
    @Mock
    private SafetyIncidentRepository safetyIncidentRepository;
    @InjectMocks
    private SafetyIncidentService safetyIncidentService;
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
    void testRecordIncident_ValidInput() {
        SafetyIncident incident = new SafetyIncident("Slip", "High", "Dock 1", "John Doe", new Date(), "Open");
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(incident);
        SafetyIncident result = safetyIncidentService.recordIncident("Slip", "High", "Dock 1", "John Doe", new Date());
        assertEquals("Slip", result.getType());
        assertEquals("High", result.getSeverity());
    }

    @Test
    void testRecordIncident_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> safetyIncidentService.recordIncident(null, "High", "Dock 1", "John Doe", new Date()));
    }

    @Test
    void testRecordIncident_EmptyType() {
        assertThrows(ValidationException.class, () -> safetyIncidentService.recordIncident("", "High", "Dock 1", "John Doe", new Date()));
    }

    @Test
    void testUpdateIncidentStatus_ValidWorkflow() {
        SafetyIncident incident = new SafetyIncident("Slip", "High", "Dock 1", "John Doe", new Date(), "Open");
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(incident);
        SafetyIncident result = safetyIncidentService.updateIncidentStatus(1L, "Investigating");
        assertEquals("Investigating", result.getStatus());
    }

    @Test
    void testUpdateIncidentStatus_InvalidId() {
        when(safetyIncidentRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> safetyIncidentService.updateIncidentStatus(999L, "Investigating"));
    }

    @Test
    void testExportOSHAReport_Valid() {
        SafetyIncident incident = new SafetyIncident("Slip", "High", "Dock 1", "John Doe", new Date(), "Resolved");
        List<SafetyIncident> incidents = Arrays.asList(incident);
        when(safetyIncidentRepository.findAllResolvedIncidents(any(Date.class), any(Date.class))).thenReturn(incidents);
        List<SafetyIncident> result = safetyIncidentService.exportOSHAReport(new Date(), new Date());
        assertEquals(1, result.size());
    }

    @Test
    void testRecordIncident_BoundaryValues() {
        SafetyIncident minIncident = new SafetyIncident("A", "Low", "Dock 1", "A", new Date(), "Open");
        SafetyIncident maxIncident = new SafetyIncident("A very long incident type exceeding normal limits", "High", "Dock 1", "John Doe", new Date(), "Open");
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(minIncident).thenReturn(maxIncident);
        assertDoesNotThrow(() -> safetyIncidentService.recordIncident("A", "Low", "Dock 1", "A", new Date()));
        assertDoesNotThrow(() -> safetyIncidentService.recordIncident("A very long incident type exceeding normal limits", "High", "Dock 1", "John Doe", new Date()));
    }

    @Test
    void testUpdateIncidentStatus_InvalidStatus() {
        SafetyIncident incident = new SafetyIncident("Slip", "High", "Dock 1", "John Doe", new Date(), "Open");
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        assertThrows(ValidationException.class, () -> safetyIncidentService.updateIncidentStatus(1L, "InvalidStatus"));
    }
}
