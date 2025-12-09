import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class CertificationServiceTest {
    private CertificationService service;

    @BeforeEach
    public void setUp() {
        service = new CertificationService();
    }

    @Test
    public void testAddCertification_Valid() {
        Certification cert = new Certification("emp1", "Forklift", "2024-08-01");
        assertDoesNotThrow(() -> service.addCertification(cert));
    }

    @Test
    public void testGetCertificationById_Valid() {
        Certification cert = new Certification("emp2", "Safety", "2024-09-01");
        service.addCertification(cert);
        assertNotNull(service.getCertificationById(cert.getId()));
    }

    @Test
    public void testUpdateCertification_Expired() {
        Certification cert = new Certification("emp3", "Hazmat", "2023-01-01");
        service.addCertification(cert);
        cert.setExpiryDate("2024-12-01");
        assertTrue(service.updateCertification(cert));
    }

    @Test
    public void testCheckExpiration_ExpiredCert() {
        Certification cert = new Certification("emp4", "CPR", "2023-01-01");
        service.addCertification(cert);
        assertTrue(service.checkExpiration(cert.getId()));
    }

    @Test
    public void testCheckExpiration_NullExpiryDate() {
        Certification cert = new Certification("emp5", "FirstAid", null);
        service.addCertification(cert);
        assertThrows(IllegalArgumentException.class, () -> service.checkExpiration(cert.getId()));
    }

    @Test
    public void testBlockAssignment_ExpiredCert() {
        Certification cert = new Certification("emp6", "Forklift", "2023-01-01");
        service.addCertification(cert);
        assertTrue(service.blockAssignment(cert.getId()));
    }

    @Test
    public void testRenewCertification_Valid() {
        Certification cert = new Certification("emp7", "Hazmat", "2023-01-01");
        service.addCertification(cert);
        assertTrue(service.renewCertification(cert.getId(), "2025-01-01"));
    }

    @Test
    public void testRenewCertification_NullDate() {
        Certification cert = new Certification("emp8", "CPR", "2023-01-01");
        service.addCertification(cert);
        assertThrows(IllegalArgumentException.class, () -> service.renewCertification(cert.getId(), null));
    }

    @Test
    public void testRenewCertification_30DayAlert() {
        Certification cert = new Certification("emp9", "FirstAid", "2024-07-15");
        service.addCertification(cert);
        assertTrue(service.check30DayAlert(cert.getId()));
    }
}