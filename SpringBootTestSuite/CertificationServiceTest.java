import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CertificationServiceTest {
    @Mock
    private CertificationRepository certificationRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private CertificationService certificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddCertification_Valid() {
        Employee employee = new Employee();
        employee.setId(1L);
        Certification cert = new Certification();
        cert.setEmployee(employee);
        cert.setCertificationName("Forklift");
        cert.setIssueDate(LocalDate.now());
        cert.setExpiryDate(LocalDate.now().plusYears(1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(cert);
        Certification result = certificationService.addCertification(1L, cert);
        assertNotNull(result);
        assertEquals("Forklift", result.getCertificationName());
    }

    @Test
    void testAddCertification_EmployeeNotFound() {
        Certification cert = new Certification();
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> certificationService.addCertification(1L, cert));
    }

    @Test
    void testCheckExpiry_Expired() {
        Certification cert = new Certification();
        cert.setExpiryDate(LocalDate.now().minusDays(1));
        assertTrue(certificationService.isExpired(cert));
    }

    @Test
    void testCheckExpiry_NotExpired() {
        Certification cert = new Certification();
        cert.setExpiryDate(LocalDate.now().plusDays(1));
        assertFalse(certificationService.isExpired(cert));
    }

    @Test
    void testCheckExpiry_ExpiringIn30Days() {
        Certification cert = new Certification();
        cert.setExpiryDate(LocalDate.now().plusDays(25));
        assertTrue(certificationService.isExpiringSoon(cert, 30));
    }

    @Test
    void testUpdateCertification_Valid() {
        Certification existing = new Certification();
        existing.setId(1L);
        Certification update = new Certification();
        update.setExpiryDate(LocalDate.now().plusYears(2));
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(certificationRepository.save(any(Certification.class))).thenReturn(existing);
        Certification result = certificationService.updateCertification(1L, update);
        assertNotNull(result);
    }
}