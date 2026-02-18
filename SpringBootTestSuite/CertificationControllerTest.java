package SpringBootTestSuite;

import com.example.controller.CertificationController;
import com.example.dto.CertificationDTO;
import com.example.entity.Certification;
import com.example.service.CertificationService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CertificationControllerTest {

    @Mock
    private CertificationService certificationService;

    @InjectMocks
    private CertificationController certificationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCertification_ShouldReturnCertification() {
        CertificationDTO dto = new CertificationDTO(1L, "Forklift", LocalDate.now(), LocalDate.now().plusYears(1));
        Certification cert = new Certification(1L, 1L, "Forklift", LocalDate.now(), LocalDate.now().plusYears(1));

        when(certificationService.create(dto)).thenReturn(cert);

        ResponseEntity<Certification> response = certificationController.createCertification(dto);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Forklift", response.getBody().getName());
    }

    @Test
    void getExpiringCertifications_ShouldReturnList() {
        List<Certification> certs = Arrays.asList(
            new Certification(1L, 1L, "Forklift", LocalDate.now(), LocalDate.now().plusDays(10))
        );
        when(certificationService.getExpiringCertifications(30)).thenReturn(certs);

        ResponseEntity<List<Certification>> response = certificationController.getExpiringCertifications(30);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getEmployeeCertifications_ShouldReturnList() {
        List<Certification> certs = Arrays.asList(
            new Certification(1L, 1L, "Forklift", LocalDate.now(), LocalDate.now().plusYears(1))
        );
        when(certificationService.getEmployeeCertifications(1L)).thenReturn(certs);

        ResponseEntity<List<Certification>> response = certificationController.getEmployeeCertifications(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }
}