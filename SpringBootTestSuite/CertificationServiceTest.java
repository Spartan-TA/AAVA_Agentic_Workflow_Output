package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CertificationServiceTest {
    @Mock
    private CertificationRepository certificationRepository;
    @InjectMocks
    private CertificationService certificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetExpiringCertifications_validDays() {
        LocalDate now = LocalDate.now();
        Certification cert = new Certification();
        cert.setExpiryDate(now.plusDays(5));
        when(certificationRepository.findByExpiryDateBetween(now, now.plusDays(5))).thenReturn(Collections.singletonList(cert));
        List<Certification> result = certificationService.getExpiringCertifications(5);
        assertEquals(1, result.size());
    }

    @Test
    void testGetExpiringCertifications_zeroDays() {
        LocalDate now = LocalDate.now();
        when(certificationRepository.findByExpiryDateBetween(now, now)).thenReturn(Collections.emptyList());
        List<Certification> result = certificationService.getExpiringCertifications(0);
        assertEquals(0, result.size());
    }

    @Test
    void testGetExpiringCertifications_negativeDays() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> certificationService.getExpiringCertifications(-1));
        assertEquals("Days must be positive", ex.getMessage());
    }

    @Test
    void testAddCertification_validInput() {
        LocalDate future = LocalDate.now().plusDays(10);
        Certification cert = new Certification();
        cert.setEmployeeId(1L);
        cert.setCertificationName("Forklift");
        cert.setExpiryDate(future);
        when(certificationRepository.save(any(Certification.class))).thenReturn(cert);
        Certification result = certificationService.addCertification(1L, "Forklift", future);
        assertEquals("Forklift", result.getCertificationName());
        assertEquals(future, result.getExpiryDate());
    }

    @Test
    void testAddCertification_nullName() {
        LocalDate future = LocalDate.now().plusDays(10);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> certificationService.addCertification(1L, null, future));
        assertEquals("Certification name required", ex.getMessage());
    }

    @Test
    void testAddCertification_emptyName() {
        LocalDate future = LocalDate.now().plusDays(10);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> certificationService.addCertification(1L, "", future));
        assertEquals("Certification name required", ex.getMessage());
    }

    @Test
    void testAddCertification_blankName() {
        LocalDate future = LocalDate.now().plusDays(10);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> certificationService.addCertification(1L, "   ", future));
        assertEquals("Certification name required", ex.getMessage());
    }

    @Test
    void testAddCertification_nullExpiryDate() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> certificationService.addCertification(1L, "Forklift", null));
        assertEquals("Invalid expiry date", ex.getMessage());
    }

    @Test
    void testAddCertification_pastExpiryDate() {
        LocalDate past = LocalDate.now().minusDays(1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> certificationService.addCertification(1L, "Forklift", past));
        assertEquals("Invalid expiry date", ex.getMessage());
    }

    // DTO and Exception classes for test compilation
    static class Certification {
        private Long employeeId;
        private String certificationName;
        private LocalDate expiryDate;
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        public String getCertificationName() { return certificationName; }
        public void setCertificationName(String certificationName) { this.certificationName = certificationName; }
        public LocalDate getExpiryDate() { return expiryDate; }
        public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    }
    interface CertificationRepository {
        List<Certification> findByExpiryDateBetween(LocalDate start, LocalDate end);
        Certification save(Certification cert);
    }
    static class CertificationService {
        private CertificationRepository certificationRepository;
        public List<Certification> getExpiringCertifications(int days) {
            if (days < 0) throw new IllegalArgumentException("Days must be positive");
            LocalDate now = LocalDate.now();
            LocalDate future = now.plusDays(days);
            return certificationRepository.findByExpiryDateBetween(now, future);
        }
        public Certification addCertification(Long employeeId, String certName, LocalDate expiryDate) {
            if (certName == null || certName.trim().isEmpty()) {
                throw new IllegalArgumentException("Certification name required");
            }
            if (expiryDate == null || expiryDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Invalid expiry date");
            }
            Certification cert = new Certification();
            cert.setEmployeeId(employeeId);
            cert.setCertificationName(certName);
            cert.setExpiryDate(expiryDate);
            return certificationRepository.save(cert);
        }
    }
}
