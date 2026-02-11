package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.service.CertificationService;
import com.example.repository.EmployeeRepository;
import com.example.repository.CertificationRepository;
import com.example.model.Employee;
import com.example.model.Certification;
import com.example.exception.EmployeeNotFoundException;

@ExtendWith(MockitoExtension.class)
public class CertificationServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private CertificationRepository certificationRepository;
    @InjectMocks
    private CertificationService certificationService;

    private Employee employee;
    private Certification cert;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        cert = new Certification();
        cert.setId(1L);
        cert.setEmployee(employee);
        cert.setExpiryDate(LocalDate.now().plusDays(20));
    }

    @Test
    void testCreateCertification_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(cert);
        Certification result = certificationService.createCertification(1L, "Forklift", LocalDate.now().plusDays(20));
        assertNotNull(result);
        assertEquals("Forklift", result.getType());
        verify(certificationRepository).save(any(Certification.class));
    }

    @Test
    void testCreateCertification_EmployeeNotFound_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> certificationService.createCertification(2L, "Forklift", LocalDate.now().plusDays(20)));
    }

    @Test
    void testUpdateCertification_Success() {
        cert.setType("Forklift");
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(cert));
        when(certificationRepository.save(any(Certification.class))).thenReturn(cert);
        Certification result = certificationService.updateCertification(1L, "Forklift", LocalDate.now().plusDays(30));
        assertEquals("Forklift", result.getType());
        assertEquals(LocalDate.now().plusDays(30), result.getExpiryDate());
    }

    @Test
    void testGetExpiringCertifications_Returns30DayAlert() {
        Certification expiring = new Certification();
        expiring.setExpiryDate(LocalDate.now().plusDays(29));
        List<Certification> certs = Arrays.asList(expiring);
        when(certificationRepository.findAll()).thenReturn(certs);
        List<Certification> result = certificationService.getExpiringCertifications(30);
        assertEquals(1, result.size());
    }

    @Test
    void testGetExpiringCertifications_Returns7DayAlert() {
        Certification expiring = new Certification();
        expiring.setExpiryDate(LocalDate.now().plusDays(6));
        List<Certification> certs = Arrays.asList(expiring);
        when(certificationRepository.findAll()).thenReturn(certs);
        List<Certification> result = certificationService.getExpiringCertifications(7);
        assertEquals(1, result.size());
    }

    @Test
    void testGetExpiringCertifications_NoExpiring_ReturnsEmpty() {
        Certification notExpiring = new Certification();
        notExpiring.setExpiryDate(LocalDate.now().plusDays(40));
        List<Certification> certs = Arrays.asList(notExpiring);
        when(certificationRepository.findAll()).thenReturn(certs);
        List<Certification> result = certificationService.getExpiringCertifications(7);
        assertTrue(result.isEmpty());
    }

    @Test
    void testValidateCertification_Valid_ReturnsTrue() {
        cert.setExpiryDate(LocalDate.now().plusDays(10));
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(cert));
        boolean valid = certificationService.validateCertification(1L);
        assertTrue(valid);
    }

    @Test
    void testValidateCertification_Expired_ReturnsFalse() {
        cert.setExpiryDate(LocalDate.now().minusDays(1));
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(cert));
        boolean valid = certificationService.validateCertification(1L);
        assertFalse(valid);
    }

    @Test
    void testDeleteCertification_Success() {
        when(certificationRepository.existsById(1L)).thenReturn(true);
        certificationService.deleteCertification(1L);
        verify(certificationRepository).deleteById(1L);
    }

    @Test
    void testGetCertificationsByEmployee_ReturnsCorrectList() {
        List<Certification> certs = Arrays.asList(cert);
        when(certificationRepository.findByEmployeeId(1L)).thenReturn(certs);
        List<Certification> result = certificationService.getCertificationsByEmployee(1L);
        assertEquals(1, result.size());
        assertEquals(employee, result.get(0).getEmployee());
    }
}
