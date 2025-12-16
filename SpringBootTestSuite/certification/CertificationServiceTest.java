import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class CertificationServiceTest {
    @Mock
    private CertificationRepository certificationRepository;

    @InjectMocks
    private CertificationService certificationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddCertification_ValidInput() {
        Certification cert = new Certification("EMP123", "Forklift", LocalDate.now().plusYears(1));
        when(certificationRepository.save(any())).thenReturn(cert);
        Certification result = certificationService.addCertification(cert);
        assertEquals("EMP123", result.getEmployeeId());
        assertEquals("Forklift", result.getType());
    }

    @Test
    public void testAddCertification_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> certificationService.addCertification(null));
    }

    @Test
    public void testValidateExpiryDate_Expired() {
        Certification cert = new Certification("EMP123", "Forklift", LocalDate.now().minusDays(1));
        assertTrue(certificationService.isExpired(cert));
    }

    @Test
    public void testValidateExpiryDate_NotExpired() {
        Certification cert = new Certification("EMP123", "Forklift", LocalDate.now().plusDays(10));
        assertFalse(certificationService.isExpired(cert));
    }

    @Test
    public void testGetExpiringCertifications_Within30Days() {
        Certification cert1 = new Certification("EMP123", "Forklift", LocalDate.now().plusDays(10));
        Certification cert2 = new Certification("EMP124", "Pallet", LocalDate.now().plusDays(40));
        when(certificationRepository.findAll()).thenReturn(Arrays.asList(cert1, cert2));
        List<Certification> expiring = certificationService.getExpiringCertifications(30);
        assertEquals(1, expiring.size());
        assertEquals("EMP123", expiring.get(0).getEmployeeId());
    }

    @Test
    public void testUpdateCertificationStatus_Valid() {
        Certification cert = new Certification("EMP123", "Forklift", LocalDate.now().plusDays(5));
        when(certificationRepository.save(any())).thenReturn(cert);
        Certification updated = certificationService.updateStatus(cert, "EXPIRED");
        assertEquals("EXPIRED", updated.getStatus());
    }

    @Test
    public void testUpdateCertificationStatus_InvalidStatus() {
        Certification cert = new Certification("EMP123", "Forklift", LocalDate.now().plusDays(5));
        assertThrows(IllegalArgumentException.class, () -> certificationService.updateStatus(cert, "INVALID_STATUS"));
    }
}
