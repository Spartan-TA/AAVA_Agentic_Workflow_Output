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

public class CertificationControllerTest {
    private MockMvc mockMvc;

    @Mock
    private CertificationService certificationService;

    @InjectMocks
    private CertificationController certificationController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(certificationController).build();
    }

    @Test
    public void testAddCertification_ValidInput_ReturnsCreated() throws Exception {
        CertificationDto dto = new CertificationDto("EMP123", "Forklift", "2025-01-01");
        when(certificationService.addCertification(any())).thenReturn(new Certification("EMP123", "Forklift", java.time.LocalDate.parse("2025-01-01")));
        mockMvc.perform(post("/certification/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":"EMP123","type":"Forklift","expiryDate":"2025-01-01"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeId").value("EMP123"));
    }

    @Test
    public void testAddCertification_NullInput_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/certification/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetExpiringCertifications_Within30Days_ReturnsOk() throws Exception {
        when(certificationService.getExpiringCertifications(30)).thenReturn(java.util.Collections.emptyList());
        mockMvc.perform(get("/certification/expiring/30"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void testUpdateCertificationStatus_Valid_ReturnsOk() throws Exception {
        when(certificationService.updateStatus(any(), eq("EXPIRED"))).thenReturn(new Certification("EMP123", "Forklift", java.time.LocalDate.now()));
        mockMvc.perform(patch("/certification/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":"EMP123","type":"Forklift","status":"EXPIRED"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateCertificationStatus_InvalidStatus_ReturnsBadRequest() throws Exception {
        when(certificationService.updateStatus(any(), eq("INVALID_STATUS"))).thenThrow(new IllegalArgumentException("Invalid status"));
        mockMvc.perform(patch("/certification/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"employeeId":"EMP123","type":"Forklift","status":"INVALID_STATUS"}"))
                .andExpect(status().isBadRequest());
    }
}
