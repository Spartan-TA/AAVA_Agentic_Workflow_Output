import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.*;
import javax.validation.ValidationException;

@ExtendWith(MockitoExtension.class)
public class CertificationServiceTest {
    @Mock
    private CertificationRepository certificationRepository;
    @InjectMocks
    private CertificationServiceImpl certificationService;

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
    void testAddCertification_ValidInput() {
        when(certificationRepository.save(any(Certification.class))).thenReturn(validCertification);
        Certification result = certificationService.addCertification(validCertificationDto);
        assertNotNull(result);
        assertEquals("Forklift", result.getType());
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    void testAddCertification_NullType() {
        validCertificationDto.setType(null);
        assertThrows(ValidationException.class, () -> certificationService.addCertification(validCertificationDto));
    }

    @Test
    void testAddCertification_ExpiryBeforeIssue() {
        validCertificationDto.setExpiryDate(LocalDate.now().minusYears(2));
        assertThrows(ValidationException.class, () -> certificationService.addCertification(validCertificationDto));
    }

    @Test
    void testRenewCertification_ValidInput() {
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(validCertification));
        LocalDate newExpiry = LocalDate.now().plusYears(2);
        validCertification.setExpiryDate(newExpiry);
        when(certificationRepository.save(any(Certification.class))).thenReturn(validCertification);
        Certification result = certificationService.renewCertification(1L, newExpiry);
        assertEquals(newExpiry, result.getExpiryDate());
    }

    @Test
    void testRenewCertification_NonExistentId() {
        when(certificationRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> certificationService.renewCertification(2L, LocalDate.now().plusYears(1)));
    }

    @Test
    void testRenewCertification_PastDate() {
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(validCertification));
        LocalDate pastDate = LocalDate.now().minusDays(1);
        assertThrows(ValidationException.class, () -> certificationService.renewCertification(1L, pastDate));
    }

    @Test
    void testGetExpiringCertifications_30Days() {
        List<Certification> expiring = Arrays.asList(validCertification);
        when(certificationRepository.findExpiringWithinDays(30)).thenReturn(expiring);
        List<Certification> result = certificationService.getExpiringCertifications(30);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetExpiringCertifications_NegativeDays() {
        assertThrows(IllegalArgumentException.class, () -> certificationService.getExpiringCertifications(-5));
    }

    @Test
    void testGetExpiringCertifications_EmptyResult() {
        when(certificationRepository.findExpiringWithinDays(10)).thenReturn(Collections.emptyList());
        List<Certification> result = certificationService.getExpiringCertifications(10);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}