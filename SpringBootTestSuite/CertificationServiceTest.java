package SpringBootTestSuite;

import com.example.dto.CertificationDTO;
import com.example.entity.Certification;
import com.example.repository.CertificationRepository;
import com.example.service.CertificationService;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CertificationServiceTest {

    @Mock
    private CertificationRepository certificationRepository;

    @InjectMocks
    private CertificationService certificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_ShouldSaveCertification() {
        CertificationDTO dto = new CertificationDTO(1L, "Forklift", LocalDate.now(), LocalDate.now().plusYears(1));
        Certification cert = new Certification(1L, 1L, "Forklift", LocalDate.now(), LocalDate.now().plusYears(1));

        when(certificationRepository.save(any(Certification.class))).thenReturn(cert);

        Certification result = certificationService.create(dto);

        assertNotNull(result);
        assertEquals("Forklift", result.getName());
    }

    @Test
    void getExpiringCertifications_ShouldReturnList() {
        List<Certification> certs = Arrays.asList(
            new Certification(1L, 1L, "Forklift", LocalDate.now(), LocalDate.now().plusDays(10))
        );
        when(certificationRepository.findExpiringWithinDays(30)).thenReturn(certs);

        List<Certification> result = certificationService.getExpiringCertifications(30);

        assertEquals(1, result.size());
    }

    @Test
    void getEmployeeCertifications_ShouldReturnList() {
        List<Certification> certs = Arrays.asList(
            new Certification(1L, 1L, "Forklift", LocalDate.now(), LocalDate.now().plusYears(1))
        );
        when(certificationRepository.findByEmployeeId(1L)).thenReturn(certs);

        List<Certification> result = certificationService.getEmployeeCertifications(1L);

        assertEquals(1, result.size());
    }
}