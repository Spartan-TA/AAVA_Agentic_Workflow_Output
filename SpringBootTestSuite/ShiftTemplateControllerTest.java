package SpringBootTestSuite;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.Collections;

@WebMvcTest(ShiftTemplateController.class)
public class ShiftTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShiftTemplateService shiftTemplateService;

    private ShiftTemplateDTO validTemplateDTO;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        validTemplateDTO = new ShiftTemplateDTO();
        validTemplateDTO.setId(1L);
        validTemplateDTO.setName("Morning Shift");
        validTemplateDTO.setStartTime(LocalTime.of(8, 0));
        validTemplateDTO.setEndTime(LocalTime.of(16, 0));
        validTemplateDTO.setRecurring(true);
    }

    @Test
    public void testCreateTemplate_ValidRequest_Returns201() throws Exception {
        when(shiftTemplateService.createTemplate(any(ShiftTemplateDTO.class))).thenReturn(validTemplateDTO);

        mockMvc.perform(post("/api/scheduling/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTemplateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Morning Shift"));
    }

    @Test
    public void testGetAllTemplates_ReturnsTemplateList() throws Exception {
        when(shiftTemplateService.getAllTemplates()).thenReturn(Collections.singletonList(validTemplateDTO));

        mockMvc.perform(get("/api/scheduling/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Morning Shift"));
    }
}