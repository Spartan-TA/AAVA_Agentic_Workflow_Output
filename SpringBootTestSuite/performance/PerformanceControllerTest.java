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

public class PerformanceControllerTest {
    private MockMvc mockMvc;

    @Mock
    private PerformanceService performanceService;

    @InjectMocks
    private PerformanceController performanceController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(performanceController).build();
    }

    @Test
    public void testCreateReview_ValidInput_ReturnsCreated() throws Exception {
        PerformanceReviewDto dto = new PerformanceReviewDto("EMP123", "Q1", "Supervisor");
        when(performanceService.createReview(any())).thenReturn(new PerformanceReview("EMP123", "Q1", "Supervisor", "Pending"));
        mockMvc.perform(post("/performance/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":"EMP123","cycle":"Q1","reviewer":"Supervisor"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("Pending"));
    }

    @Test
    public void testCreateReview_NullInput_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/performance/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSubmitReview_Valid_ReturnsOk() throws Exception {
        when(performanceService.submitReview(any())).thenReturn(new PerformanceReview("EMP123", "Q1", "Supervisor", "Submitted"));
        mockMvc.perform(post("/performance/review/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":"EMP123","cycle":"Q1"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Submitted"));
    }

    @Test
    public void testAcknowledgeReview_Valid_ReturnsOk() throws Exception {
        when(performanceService.acknowledgeReview(any())).thenReturn(new PerformanceReview("EMP123", "Q1", "Supervisor", "Acknowledged"));
        mockMvc.perform(post("/performance/review/acknowledge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":"EMP123","cycle":"Q1"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Acknowledged"));
    }

    @Test
    public void testExportReviewPDF_Valid_ReturnsOk() throws Exception {
        byte[] pdf = new byte[]{1,2,3};
        when(performanceService.exportReviewPDF(anyLong())).thenReturn(pdf);
        mockMvc.perform(get("/performance/review/export/1"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(pdf));
    }
}
