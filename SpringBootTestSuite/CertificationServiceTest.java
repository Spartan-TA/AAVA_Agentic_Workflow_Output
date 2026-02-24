package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDate;
import java.util.*;

class CertificationServiceTest {

    @Mock
    private CertificationRepository certificationRepository;

    @InjectMocks
    private CertificationService certificationService;

    private Certification validCertification;
    private EmployeeCertification validEmployeeCert;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validCertification = new Certification(1L, "Forklift Operator", "Safety certification for forklift operation", 365);
        validEmployeeCert = new EmployeeCertification(1L, 1L, 1L, LocalDate.now(), LocalDate.now().plusDays(365), "doc.pdf");
    }

    @AfterEach
    void tearDown() {
        // Clean up resources if needed
    }

    @Test
    void testAddCertification_ValidInput() {
        when(certificationRepository.save(any(Certification.class))).thenReturn(validCertification);
        Certification result = certificationService.addCertification(validCertification);
        assertNotNull(result);
        assertEquals("Forklift Operator", result.getName());
    }

    @Test
    void testAddCertification_NullName() {
        Certification nullCert = new Certification(null, null, "Description", 365);
        assertThrows(IllegalArgumentException.class, () -> certificationService.addCertification(nullCert));
    }

    @Test
    void testAssignCertificationToEmployee_ValidInput() {
        when(certificationRepository.saveEmployeeCertification(any(EmployeeCertification.class))).thenReturn(validEmployeeCert);
        EmployeeCertification result = certificationService.assignCertificationToEmployee(1L, 1L, LocalDate.now(), LocalDate.now().plusDays(365), "doc.pdf");
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
    }

    @Test
    void testAssignCertificationToEmployee_ExpiredDate() {
        assertThrows(IllegalArgumentException.class, () -> certificationService.assignCertificationToEmployee(1L, 1L, LocalDate.now().minusDays(365), LocalDate.now().minusDays(1), "doc.pdf"));
    }

    @Test
    void testGetExpiringCertifications_ValidInput() {
        when(certificationRepository.findExpiringCertifications(any(), any()))
            .thenReturn(Arrays.asList(validEmployeeCert));
        List<EmployeeCertification> results = certificationService.getExpiringCertifications(30);
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void testGetExpiringCertifications_NoDays() {
        when(certificationRepository.findExpiringCertifications(any(), any()))
            .thenReturn(Collections.emptyList());
        List<EmployeeCertification> results = certificationService.getExpiringCertifications(0);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testUploadDocument_ValidInput() {
        when(certificationRepository.findEmployeeCertificationById(1L)).thenReturn(Optional.of(validEmployeeCert));
        when(certificationRepository.saveEmployeeCertification(any(EmployeeCertification.class))).thenReturn(validEmployeeCert);
        EmployeeCertification result = certificationService.uploadDocument(1L, "new_doc.pdf");
        assertNotNull(result);
        assertEquals("new_doc.pdf", result.getDocumentUrl());
    }

    @Test
    void testUploadDocument_InvalidId() {
        when(certificationRepository.findEmployeeCertificationById(999L)).thenReturn(Optional.empty());
        assertThrows(CertificationNotFoundException.class, () -> certificationService.uploadDocument(999L, "doc.pdf"));
    }

    @Test
    void testBlockAssignmentForExpiredCert_ValidInput() {
        EmployeeCertification expiredCert = new EmployeeCertification(1L, 1L, 1L, LocalDate.now().minusDays(400), LocalDate.now().minusDays(35), "doc.pdf");
        when(certificationRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList(expiredCert));
        boolean result = certificationService.hasValidCertification(1L, 1L);
        assertFalse(result);
    }

    @Test
    void testUploadDocument_NullUrl() {
        assertThrows(IllegalArgumentException.class, () -> certificationService.uploadDocument(1L, null));
    }

    @Test
    void testAssignCertificationToEmployee_Duplicate() {
        when(certificationRepository.findByEmployeeIdAndCertificationId(1L, 1L))
            .thenReturn(Optional.of(validEmployeeCert));
        assertThrows(IllegalStateException.class, () -> certificationService.assignCertificationToEmployee(1L, 1L, LocalDate.now(), LocalDate.now().plusDays(365), "doc.pdf"));
    }
}