package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class CertificationServiceImplTest {

    @Mock
    private CertificationRepository certificationRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private CertificationServiceImpl certificationService;

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
    @DisplayName("createCertification - valid input - certification created")
    void testCreateCertification_ValidInput_CertificationCreated() {
        Certification cert = new Certification(null, 1L, "Forklift", "2024-12-31");
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(certificationRepository.save(any())).thenAnswer(i -> {
            Certification c = i.getArgument(0);
            c.setId(1L);
            return c;
        });
        Certification result = certificationService.createCertification(cert);
        assertNotNull(result.getId());
        assertEquals("Forklift", result.getName());
    }

    @Test
    @DisplayName("createCertification - employee not found - throws exception")
    void testCreateCertification_EmployeeNotFound_ThrowsException() {
        Certification cert = new Certification(null, 99L, "Forklift", "2024-12-31");
        when(employeeRepository.existsById(99L)).thenReturn(false);
        assertThrows(EmployeeNotFoundException.class, () -> certificationService.createCertification(cert));
    }

    @Test
    @DisplayName("getCertificationById - found - returns certification")
    void testGetCertificationById_Found_ReturnsCertification() {
        Certification cert = new Certification(1L, 1L, "Forklift", "2024-12-31");
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(cert));
        Certification result = certificationService.getCertificationById(1L);
        assertEquals("Forklift", result.getName());
    }

    @Test
    @DisplayName("getCertificationById - not found - throws exception")
    void testGetCertificationById_NotFound_ThrowsException() {
        when(certificationRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(CertificationNotFoundException.class, () -> certificationService.getCertificationById(2L));
    }

    @Test
    @DisplayName("getExpiringCertifications - returns list")
    void testGetExpiringCertifications_ReturnsList() {
        List<Certification> certs = Arrays.asList(new Certification(1L, 1L, "Forklift", "2024-06-30"));
        when(certificationRepository.findExpiringWithinDays(30)).thenReturn(certs);
        List<Certification> result = certificationService.getExpiringCertifications(30);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("validateCertification - valid - returns true")
    void testValidateCertification_Valid_ReturnsTrue() {
        Certification cert = new Certification(1L, 1L, "Forklift", "2099-12-31");
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(cert));
        assertTrue(certificationService.validateCertification(1L));
    }

    @Test
    @DisplayName("validateCertification - expired - returns false")
    void testValidateCertification_Expired_ReturnsFalse() {
        Certification cert = new Certification(1L, 1L, "Forklift", "2020-01-01");
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(cert));
        assertFalse(certificationService.validateCertification(1L));
    }

    @Test
    @DisplayName("uploadDocument - valid - document uploaded")
    void testUploadDocument_Valid_DocumentUploaded() {
        Certification cert = new Certification(1L, 1L, "Forklift", "2024-12-31");
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(cert));
        byte[] doc = new byte[]{1,2,3};
        certificationService.uploadDocument(1L, doc);
        assertArrayEquals(doc, cert.getDocument());
    }

    @Test
    @DisplayName("uploadDocument - certification not found - throws exception")
    void testUploadDocument_CertificationNotFound_ThrowsException() {
        when(certificationRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(CertificationNotFoundException.class, () -> certificationService.uploadDocument(2L, new byte[]{1}));
    }

    @Test
    @DisplayName("getExpiringCertifications - no expiring - returns empty list")
    void testGetExpiringCertifications_NoExpiring_ReturnsEmptyList() {
        when(certificationRepository.findExpiringWithinDays(7)).thenReturn(Collections.emptyList());
        List<Certification> result = certificationService.getExpiringCertifications(7);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("validateCertification - not found - throws exception")
    void testValidateCertification_NotFound_ThrowsException() {
        when(certificationRepository.findById(3L)).thenReturn(Optional.empty());
        assertThrows(CertificationNotFoundException.class, () -> certificationService.validateCertification(3L));
    }
}