package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.*;

/**
 * CertificationServiceTest - Comprehensive unit tests for CertificationService covering CRUD, expiry, blocking, boundaries, and edge cases.
 */
public class CertificationServiceTest {
    private CertificationService certificationService;

    @BeforeEach
    public void setUp() {
        certificationService = new CertificationService();
    }

    @Test
    public void testCreateCertificationValid() {
        Certification cert = new Certification("Forklift", LocalDate.now().plusYears(1));
        assertDoesNotThrow(() -> certificationService.createCertification(cert));
    }

    @Test
    public void testCreateCertificationInvalidInput() {
        Certification cert = new Certification("", null);
        assertThrows(IllegalArgumentException.class, () -> certificationService.createCertification(cert));
    }

    @Test
    public void testGetCertificationByIdValid() {
        int certId = 1;
        Certification cert = certificationService.getCertificationById(certId);
        assertNotNull(cert);
    }

    @Test
    public void testGetCertificationByIdInvalid() {
        int certId = -1;
        Certification cert = certificationService.getCertificationById(certId);
        assertNull(cert);
    }

    @Test
    public void testUpdateCertification() {
        Certification cert = new Certification("Safety", LocalDate.now().plusMonths(6));
        assertTrue(certificationService.updateCertification(cert));
    }

    @Test
    public void testDeleteCertification() {
        int certId = 2;
        assertTrue(certificationService.deleteCertification(certId));
    }

    @Test
    public void testGetExpiringCertifications30Days() {
        List<Certification> certs = certificationService.getExpiringCertifications(30);
        assertNotNull(certs);
    }

    @Test
    public void testGetExpiringCertifications7Days() {
        List<Certification> certs = certificationService.getExpiringCertifications(7);
        assertNotNull(certs);
    }

    @Test
    public void testCheckCertificationValidity() {
        Certification cert = new Certification("Forklift", LocalDate.now().plusDays(5));
        assertTrue(certificationService.checkCertificationValidity(cert));
    }

    @Test
    public void testBlockAssignmentForExpiredCert() {
        Certification cert = new Certification("Forklift", LocalDate.now().minusDays(1));
        assertTrue(certificationService.blockAssignmentForExpiredCert(cert));
    }

    @Test
    public void testUploadCertificationDocument() {
        Certification cert = new Certification("Forklift", LocalDate.now().plusDays(30));
        byte[] doc = new byte[]{1,2,3};
        assertTrue(certificationService.uploadCertificationDocument(cert, doc));
    }

    @Test
    public void testGetCertificationsByEmployee() {
        int empId = 101;
        List<Certification> certs = certificationService.getCertificationsByEmployee(empId);
        assertNotNull(certs);
    }

    @Test
    public void testRenewCertification() {
        Certification cert = new Certification("Forklift", LocalDate.now().minusDays(1));
        assertTrue(certificationService.renewCertification(cert));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2023-01-01", "2024-12-31"})
    public void testBoundaryExpiryDates(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        Certification cert = new Certification("Boundary", date);
        assertDoesNotThrow(() -> certificationService.createCertification(cert));
    }

    @Test
    public void testExpiredCerts() {
        Certification cert = new Certification("Expired", LocalDate.now().minusDays(10));
        assertFalse(certificationService.checkCertificationValidity(cert));
    }

    @Test
    public void testMissingDocuments() {
        Certification cert = new Certification("Forklift", LocalDate.now().plusDays(30));
        assertFalse(certificationService.uploadCertificationDocument(cert, null));
    }
}
