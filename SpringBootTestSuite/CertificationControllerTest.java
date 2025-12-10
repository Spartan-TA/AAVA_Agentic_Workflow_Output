import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CertificationController.class)
public class CertificationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CertificationService certificationService;
    @Autowired
    private ObjectMapper objectMapper;
    private CertificationDto validCertificationDto;
    private Certification validCertification;

    @BeforeEach
    void setUp() {
        validCertificationDto = new CertificationDto();
        validCertificationDto.setType("Forklift");
        validCertificationDto.setIssueDate(LocalDate.now().minusYears(1));
        validCertificationDto.setExpiryDate(LocalDate.now().plusYears(1));
        validCertificationDto.setEmployeeId(1L);
        validCertification = new Certification();
        validCertification.setId(1L);
        validCertification.setType("Forklift");
        validCertification.setIssueDate(validCertificationDto.getIssueDate());
        validCertification.setExpiryDate(validCertificationDto.getExpiryDate());
        validCertification.setEmployeeId(1L);
    }

    @Test
    void testAddCertification_ValidRequest() throws Exception {
        when(certificationService.addCertification(any(CertificationDto.class))).thenReturn(validCertification);
        mockMvc.perform(post("/certifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCertificationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("Forklift"));
    }

    @Test
    void testAddCertification_InvalidRequest() throws Exception {
        CertificationDto invalidDto = new CertificationDto();
        invalidDto.setType("");
        when(certificationService.addCertification(any(CertificationDto.class))).thenThrow(new javax.validation.ValidationException("Invalid input"));
        mockMvc.perform(post("/certifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRenewCertification_ValidRequest() throws Exception {
        LocalDate newExpiry = LocalDate.now().plusYears(2);
        validCertification.setExpiryDate(newExpiry);
        when(certificationService.renewCertification(eq(1L), any(LocalDate.class))).thenReturn(validCertification);
        mockMvc.perform(patch("/certifications/1/renew")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newExpiry)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiryDate").exists());
    }

    @Test
    void testRenewCertification_NonExistentId() throws Exception {
        LocalDate newExpiry = LocalDate.now().plusYears(2);
        when(certificationService.renewCertification(eq(2L), any(LocalDate.class))).thenThrow(new ResourceNotFoundException("Not found"));
        mockMvc.perform(patch("/certifications/2/renew")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newExpiry)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetExpiringCertifications_ValidRequest() throws Exception {
        when(certificationService.getExpiringCertifications(30)).thenReturn(Arrays.asList(validCertification));
        mockMvc.perform(get("/certifications/expiring?days=30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("Forklift"));
    }

    @Test
    void testGetExpiringCertifications_EmptyResult() throws Exception {
        when(certificationService.getExpiringCertifications(10)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/certifications/expiring?days=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}