import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SafetyIncidentController.class)
public class SafetyIncidentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SafetyIncidentService safetyIncidentService;
    @Autowired
    private ObjectMapper objectMapper;
    private SafetyIncidentDto validIncidentDto;
    private SafetyIncident validIncident;

    @BeforeEach
    void setUp() {
        validIncidentDto = new SafetyIncidentDto();
        validIncidentDto.setDescription("Spill in aisle 3");
        validIncidentDto.setSeverity("High");
        validIncidentDto.setReportedBy(1L);
        validIncident = new SafetyIncident();
        validIncident.setId(1L);
        validIncident.setDescription("Spill in aisle 3");
        validIncident.setSeverity("High");
        validIncident.setReportedBy(1L);
    }

    @Test
    void testReportIncident_ValidRequest() throws Exception {
        when(safetyIncidentService.report(any(SafetyIncidentDto.class))).thenReturn(validIncident);
        mockMvc.perform(post("/incidents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validIncidentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Spill in aisle 3"));
    }

    @Test
    void testReportIncident_InvalidRequest() throws Exception {
        SafetyIncidentDto invalidDto = new SafetyIncidentDto();
        invalidDto.setDescription("");
        when(safetyIncidentService.report(any(SafetyIncidentDto.class))).thenThrow(new javax.validation.ValidationException("Invalid input"));
        mockMvc.perform(post("/incidents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateIncident_ValidRequest() throws Exception {
        when(safetyIncidentService.update(eq(1L), any(SafetyIncidentDto.class))).thenReturn(validIncident);
        mockMvc.perform(put("/incidents/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validIncidentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Spill in aisle 3"));
    }

    @Test
    void testUpdateIncident_NonExistentId() throws Exception {
        when(safetyIncidentService.update(eq(2L), any(SafetyIncidentDto.class))).thenThrow(new ResourceNotFoundException("Not found"));
        mockMvc.perform(put("/incidents/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validIncidentDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testListIncidents_WithResults() throws Exception {
        when(safetyIncidentService.list()).thenReturn(Arrays.asList(validIncident));
        mockMvc.perform(get("/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Spill in aisle 3"));
    }

    @Test
    void testListIncidents_EmptyResult() throws Exception {
        when(safetyIncidentService.list()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}