import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class SafetyControllerTest {
    private MockMvc mockMvc;

    @Mock
    private SafetyService safetyService;

    @InjectMocks
    private SafetyController safetyController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(safetyController).build();
    }

    @Test
    public void testRecordIncident_ValidInput_ReturnsCreated() throws Exception {
        SafetyIncidentDto dto = new SafetyIncidentDto("EMP123", "Forklift accident", "HIGH");
        when(safetyService.recordIncident(any())).thenReturn(new SafetyIncident("EMP123", "Forklift accident", "HIGH", "OPEN"));
        mockMvc.perform(post("/safety/incidents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":"EMP123","description":"Forklift accident","severity":"HIGH"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    public void testRecordIncident_NullInput_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/safety/incidents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateIncidentStatus_Valid_ReturnsOk() throws Exception {
        when(safetyService.updateStatus(any(), eq("INVESTIGATING"))).thenReturn(new SafetyIncident("EMP123", "Forklift accident", "HIGH", "INVESTIGATING"));
        mockMvc.perform(patch("/safety/incidents/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":"EMP123","status":"INVESTIGATING"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateIncidentStatus_InvalidStatus_ReturnsBadRequest() throws Exception {
        when(safetyService.updateStatus(any(), eq("INVALID"))).thenThrow(new IllegalArgumentException("Invalid status"));
        mockMvc.perform(patch("/safety/incidents/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":"EMP123","status":"INVALID"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testExportOSHAReport_ReturnsOk() throws Exception {
        OSHAReport report = new OSHAReport(2);
        when(safetyService.generateOSHAReport()).thenReturn(report);
        mockMvc.perform(get("/safety/osha/export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incidentCount").value(2));
    }
}
