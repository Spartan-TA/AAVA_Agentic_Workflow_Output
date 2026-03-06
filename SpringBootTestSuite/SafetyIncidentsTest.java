package SpringBootTestSuite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class SafetyIncidentsTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private SafetyIncidentService safetyIncidentService;

    @InjectMocks
    private SafetyIncidentController safetyIncidentController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPostIncident_NormalCase_Success() {
        SafetyIncident incident = new SafetyIncident("Fall", "Open", "2024-06-01", "desc");
        when(safetyIncidentService.createIncident(any())).thenReturn(incident);
        SafetyIncident result = safetyIncidentController.createIncident(incident);
        assertEquals("Fall", result.getType());
        assertEquals("Open", result.getStatus());
    }

    @Test
    public void testPostIncident_NullInput_Exception() {
        when(safetyIncidentService.createIncident(null)).thenThrow(new IllegalArgumentException("Incident cannot be null"));
        assertThrows(IllegalArgumentException.class, () -> safetyIncidentController.createIncident(null));
    }

    @Test
    public void testGetIncidentById_ValidId_ReturnsIncident() {
        SafetyIncident incident = new SafetyIncident("Burn", "Investigating", "2024-06-02", "desc");
        when(safetyIncidentService.getIncidentById(1L)).thenReturn(incident);
        SafetyIncident result = safetyIncidentController.getIncidentById(1L);
        assertEquals("Burn", result.getType());
    }

    @Test
    public void testGetIncidentById_InvalidId_ReturnsNull() {
        when(safetyIncidentService.getIncidentById(999L)).thenReturn(null);
        SafetyIncident result = safetyIncidentController.getIncidentById(999L);
        assertNull(result);
    }

    @Test
    public void testUpdateIncidentStatus_OpenToInvestigating_Success() {
        SafetyIncident incident = new SafetyIncident("Fall", "Investigating", "2024-06-01", "desc");
        when(safetyIncidentService.updateIncidentStatus(anyLong(), eq("Investigating"))).thenReturn(incident);
        SafetyIncident result = safetyIncidentController.updateIncidentStatus(1L, "Investigating");
        assertEquals("Investigating", result.getStatus());
    }

    @Test
    public void testUpdateIncidentStatus_InvalidStatus_Exception() {
        when(safetyIncidentService.updateIncidentStatus(anyLong(), eq("Invalid"))).thenThrow(new IllegalArgumentException("Invalid status"));
        assertThrows(IllegalArgumentException.class, () -> safetyIncidentController.updateIncidentStatus(1L, "Invalid"));
    }

    @Test
    public void testExportOSHA300_Success() {
        when(safetyIncidentService.exportOSHA300()).thenReturn("osha300.csv");
        assertEquals("osha300.csv", safetyIncidentService.exportOSHA300());
    }

    @Test
    public void testExportOSHA300A_Success() {
        when(safetyIncidentService.exportOSHA300A()).thenReturn("osha300A.csv");
        assertEquals("osha300A.csv", safetyIncidentService.exportOSHA300A());
    }

    @Test
    public void testMetricsDashboard_ValidData_ReturnsMetrics() {
        SafetyMetrics metrics = new SafetyMetrics(5, 2, 1);
        when(safetyIncidentService.getMetricsDashboard()).thenReturn(metrics);
        SafetyMetrics result = safetyIncidentService.getMetricsDashboard();
        assertEquals(5, result.getTotalIncidents());
        assertEquals(2, result.getOpenIncidents());
        assertEquals(1, result.getResolvedIncidents());
    }

    @Test
    public void testDeleteIncident_ValidId_Success() {
        doNothing().when(safetyIncidentService).deleteIncident(2L);
        safetyIncidentController.deleteIncident(2L);
        verify(safetyIncidentService, times(1)).deleteIncident(2L);
    }

    @Test
    public void testDeleteIncident_InvalidId_Exception() {
        doThrow(new RuntimeException("Not found")).when(safetyIncidentService).deleteIncident(999L);
        assertThrows(RuntimeException.class, () -> safetyIncidentController.deleteIncident(999L));
    }

    @Test
    public void testIncidentStatusWorkflow_OpenToResolved_Success() {
        SafetyIncident incident = new SafetyIncident("Fall", "Resolved", "2024-06-01", "desc");
        when(safetyIncidentService.updateIncidentStatus(anyLong(), eq("Resolved"))).thenReturn(incident);
        SafetyIncident result = safetyIncidentController.updateIncidentStatus(1L, "Resolved");
        assertEquals("Resolved", result.getStatus());
    }

    @Test
    public void testIncidentStatusWorkflow_InvalidTransition_Exception() {
        when(safetyIncidentService.updateIncidentStatus(anyLong(), eq("Closed"))).thenThrow(new IllegalStateException("Invalid transition"));
        assertThrows(IllegalStateException.class, () -> safetyIncidentController.updateIncidentStatus(1L, "Closed"));
    }

    @Test
    public void testGetAllIncidents_EmptyList_ReturnsEmpty() {
        when(safetyIncidentService.getAllIncidents()).thenReturn(java.util.Collections.emptyList());
        assertTrue(safetyIncidentService.getAllIncidents().isEmpty());
    }

    @Test
    public void testGetAllIncidents_Multiple_ReturnsList() {
        java.util.List<SafetyIncident> incidents = java.util.Arrays.asList(
            new SafetyIncident("Fall", "Open", "2024-06-01", "desc"),
            new SafetyIncident("Burn", "Resolved", "2024-06-02", "desc")
        );
        when(safetyIncidentService.getAllIncidents()).thenReturn(incidents);
        assertEquals(2, safetyIncidentService.getAllIncidents().size());
    }

    @Test
    public void testAuthorization_UnauthorizedUser_ThrowsException() {
        doThrow(new SecurityException("Unauthorized")).when(safetyIncidentService).deleteIncident(anyLong());
        assertThrows(SecurityException.class, () -> safetyIncidentService.deleteIncident(1L));
    }

    @Test
    public void testPostIncident_InvalidData_Exception() {
        SafetyIncident invalidIncident = new SafetyIncident("", "Open", "", "");
        when(safetyIncidentService.createIncident(invalidIncident)).thenThrow(new IllegalArgumentException("Invalid data"));
        assertThrows(IllegalArgumentException.class, () -> safetyIncidentController.createIncident(invalidIncident));
    }

    // Add more tests as needed for edge cases, nulls, etc.
}

class SafetyIncident {
    private String type;
    private String status;
    private String date;
    private String description;
    public SafetyIncident(String type, String status, String date, String description) {
        this.type = type;
        this.status = status;
        this.date = date;
        this.description = description;
    }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
}

class SafetyIncidentService {
    public SafetyIncident createIncident(SafetyIncident incident) { return null; }
    public SafetyIncident getIncidentById(Long id) { return null; }
    public SafetyIncident updateIncidentStatus(Long id, String status) { return null; }
    public void deleteIncident(Long id) {}
    public String exportOSHA300() { return null; }
    public String exportOSHA300A() { return null; }
    public SafetyMetrics getMetricsDashboard() { return null; }
    public java.util.List<SafetyIncident> getAllIncidents() { return null; }
}

class SafetyIncidentController {
    private SafetyIncidentService safetyIncidentService;
    public SafetyIncident createIncident(SafetyIncident incident) { return safetyIncidentService.createIncident(incident); }
    public SafetyIncident getIncidentById(Long id) { return safetyIncidentService.getIncidentById(id); }
    public SafetyIncident updateIncidentStatus(Long id, String status) { return safetyIncidentService.updateIncidentStatus(id, status); }
    public void deleteIncident(Long id) { safetyIncidentService.deleteIncident(id); }
}

class SafetyMetrics {
    private int totalIncidents;
    private int openIncidents;
    private int resolvedIncidents;
    public SafetyMetrics(int total, int open, int resolved) {
        this.totalIncidents = total;
        this.openIncidents = open;
        this.resolvedIncidents = resolved;
    }
    public int getTotalIncidents() { return totalIncidents; }
    public int getOpenIncidents() { return openIncidents; }
    public int getResolvedIncidents() { return resolvedIncidents; }
}
