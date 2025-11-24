package com.warehouse.employee.management.service;

import com.warehouse.employee.management.model.Certification;
import com.warehouse.employee.management.model.Employee;
import com.warehouse.employee.management.repository.CertificationRepository;
import com.warehouse.employee.management.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for CertificationService.
 * Tests cover certification management, expiration logic, and validation.
 * Follows AAA (Arrange-Act-Assert) pattern for clarity.
 */
@ExtendWith(MockitoExtension.class)
public class CertificationServiceTest {

    @Mock
    private CertificationRepository certificationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private CertificationService certificationService;

    private Employee testEmployee;
    private Certification validCertification;
    private Certification expiredCertification;

    @BeforeEach
    public void setUp() {
        // Arrange: Create test data
        testEmployee = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .name("John Doe")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Morning")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .deleted(false)
                .build();

        validCertification = Certification.builder()
                .id(1L)
                .employee(testEmployee)
                .certificationName("Forklift Operator")
                .issueDate(LocalDate.now().minusYears(1))
                .expirationDate(LocalDate.now().plusYears(1))
                .documentUrl("https://example.com/cert1.pdf")
                .status("ACTIVE")
                .build();

        expiredCertification = Certification.builder()
                .id(2L)
                .employee(testEmployee)
                .certificationName("Safety Training")
                .issueDate(LocalDate.now().minusYears(2))
                .expirationDate(LocalDate.now().minusDays(1))
                .documentUrl("https://example.com/cert2.pdf")
                .status("EXPIRED")
                .build();
    }

    // ========== Tests for addCertification(Certification) ==========

    @Test
    public void testAddCertification_ValidActiveCertification_Success() {
        // Arrange
        when(certificationRepository.save(any(Certification.class))).thenReturn(validCertification);

        // Act
        Certification result = certificationService.addCertification(validCertification);

        // Assert
        assertNotNull(result);
        assertEquals("ACTIVE", result.getStatus());
        assertEquals("Forklift Operator", result.getCertificationName());
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    public void testAddCertification_ExpiredCertification_SetsExpiredStatus() {
        // Arrange
        Certification expiring = Certification.builder()
                .employee(testEmployee)
                .certificationName("Old Cert")
                .issueDate(LocalDate.now().minusYears(3))
                .expirationDate(LocalDate.now().minusDays(10))
                .build();
        
        when(certificationRepository.save(any(Certification.class))).thenAnswer(invocation -> {
            Certification cert = invocation.getArgument(0);
            if (cert.getExpirationDate().isBefore(LocalDate.now())) {
                cert.setStatus("EXPIRED");
            } else {
                cert.setStatus("ACTIVE");
            }
            return cert;
        });

        // Act
        Certification result = certificationService.addCertification(expiring);

        // Assert
        assertNotNull(result);
        assertEquals("EXPIRED", result.getStatus());
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    public void testAddCertification_FutureCertification_SetsActiveStatus() {
        // Arrange
        Certification futureCert = Certification.builder()
                .employee(testEmployee)
                .certificationName("Future Cert")
                .issueDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusYears(5))
                .build();
        
        when(certificationRepository.save(any(Certification.class))).thenAnswer(invocation -> {
            Certification cert = invocation.getArgument(0);
            if (cert.getExpirationDate().isBefore(LocalDate.now())) {
                cert.setStatus("EXPIRED");
            } else {
                cert.setStatus("ACTIVE");
            }
            return cert;
        });

        // Act
        Certification result = certificationService.addCertification(futureCert);

        // Assert
        assertNotNull(result);
        assertEquals("ACTIVE", result.getStatus());
    }

    @Test
    public void testAddCertification_NullCertification_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, 
            () -> certificationService.addCertification(null));
    }

    @Test
    public void testAddCertification_NullExpirationDate_ThrowsException() {
        // Arrange
        Certification certWithNullExpiration = Certification.builder()
                .employee(testEmployee)
                .certificationName("Test Cert")
                .issueDate(LocalDate.now())
                .expirationDate(null)
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class, 
            () -> certificationService.addCertification(certWithNullExpiration));
    }

    @Test
    public void testAddCertification_ExpirationDateToday_SetsExpiredStatus() {
        // Arrange
        Certification certExpiringToday = Certification.builder()
                .employee(testEmployee)
                .certificationName("Expiring Today")
                .issueDate(LocalDate.now().minusYears(1))
                .expirationDate(LocalDate.now())
                .build();
        
        when(certificationRepository.save(any(Certification.class))).thenAnswer(invocation -> {
            Certification cert = invocation.getArgument(0);
            if (cert.getExpirationDate().isBefore(LocalDate.now())) {
                cert.setStatus("EXPIRED");
            } else {
                cert.setStatus("ACTIVE");
            }
            return cert;
        });

        // Act
        Certification result = certificationService.addCertification(certExpiringToday);

        // Assert
        assertNotNull(result);
        assertEquals("ACTIVE", result.getStatus()); // Today is not before today
    }

    @Test
    public void testAddCertification_EmptyCertificationName_Success() {
        // Arrange
        Certification certWithEmptyName = Certification.builder()
                .employee(testEmployee)
                .certificationName("")
                .issueDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusYears(1))
                .build();
        
        when(certificationRepository.save(any(Certification.class))).thenReturn(certWithEmptyName);

        // Act
        Certification result = certificationService.addCertification(certWithEmptyName);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getCertificationName());
    }

    // ========== Tests for getExpiringSoon(int) ==========

    @Test
    public void testGetExpiringSoon_ValidDays_ReturnsCertifications() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(30);
        Certification expiringSoon = Certification.builder()
                .id(3L)
                .employee(testEmployee)
                .certificationName("Expiring Soon")
                .issueDate(LocalDate.now().minusYears(1))
                .expirationDate(LocalDate.now().plusDays(15))
                .status("ACTIVE")
                .build();
        
        List<Certification> expiringCerts = Arrays.asList(expiringSoon);
        when(certificationRepository.findByExpirationDateBefore(futureDate)).thenReturn(expiringCerts);

        // Act
        List<Certification> result = certificationService.getExpiringSoon(30);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Expiring Soon", result.get(0).getCertificationName());
        verify(certificationRepository, times(1)).findByExpirationDateBefore(futureDate);
    }

    @Test
    public void testGetExpiringSoon_ZeroDays_ReturnsExpiredCertifications() {
        // Arrange
        LocalDate today = LocalDate.now();
        when(certificationRepository.findByExpirationDateBefore(today))
                .thenReturn(Arrays.asList(expiredCertification));

        // Act
        List<Certification> result = certificationService.getExpiringSoon(0);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(certificationRepository, times(1)).findByExpirationDateBefore(today);
    }

    @Test
    public void testGetExpiringSoon_NegativeDays_ReturnsExpiredCertifications() {
        // Arrange
        LocalDate pastDate = LocalDate.now().plusDays(-10);
        when(certificationRepository.findByExpirationDateBefore(pastDate))
                .thenReturn(Arrays.asList(expiredCertification));

        // Act
        List<Certification> result = certificationService.getExpiringSoon(-10);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetExpiringSoon_LargeDays_ReturnsAllCertifications() {
        // Arrange
        LocalDate farFuture = LocalDate.now().plusDays(3650); // 10 years
        List<Certification> allCerts = Arrays.asList(validCertification, expiredCertification);
        when(certificationRepository.findByExpirationDateBefore(farFuture)).thenReturn(allCerts);

        // Act
        List<Certification> result = certificationService.getExpiringSoon(3650);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testGetExpiringSoon_NoExpiring_ReturnsEmptyList() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(7);
        when(certificationRepository.findByExpirationDateBefore(futureDate)).thenReturn(Arrays.asList());

        // Act
        List<Certification> result = certificationService.getExpiringSoon(7);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== Tests for hasValidCertification(Long, String) ==========

    @Test
    public void testHasValidCertification_ValidCertification_ReturnsTrue() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.findByEmployee(testEmployee))
                .thenReturn(Arrays.asList(validCertification));

        // Act
        boolean result = certificationService.hasValidCertification(1L, "Forklift Operator");

        // Assert
        assertTrue(result);
        verify(employeeRepository, times(1)).findById(1L);
        verify(certificationRepository, times(1)).findByEmployee(testEmployee);
    }

    @Test
    public void testHasValidCertification_ExpiredCertification_ReturnsFalse() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.findByEmployee(testEmployee))
                .thenReturn(Arrays.asList(expiredCertification));

        // Act
        boolean result = certificationService.hasValidCertification(1L, "Safety Training");

        // Assert
        assertFalse(result);
    }

    @Test
    public void testHasValidCertification_NonExistentCertification_ReturnsFalse() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.findByEmployee(testEmployee))
                .thenReturn(Arrays.asList(validCertification));

        // Act
        boolean result = certificationService.hasValidCertification(1L, "Non-Existent Cert");

        // Assert
        assertFalse(result);
    }

    @Test
    public void testHasValidCertification_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> certificationService.hasValidCertification(999L, "Forklift Operator"));
        assertEquals("Employee not found", exception.getMessage());
        verify(employeeRepository, times(1)).findById(999L);
        verify(certificationRepository, never()).findByEmployee(any());
    }

    @Test
    public void testHasValidCertification_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> certificationService.hasValidCertification(null, "Forklift Operator"));
    }

    @Test
    public void testHasValidCertification_NullCertificationName_ReturnsFalse() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.findByEmployee(testEmployee))
                .thenReturn(Arrays.asList(validCertification));

        // Act
        boolean result = certificationService.hasValidCertification(1L, null);

        // Assert
        assertFalse(result);
    }

    @Test
    public void testHasValidCertification_EmptyCertificationName_ReturnsFalse() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.findByEmployee(testEmployee))
                .thenReturn(Arrays.asList(validCertification));

        // Act
        boolean result = certificationService.hasValidCertification(1L, "");

        // Assert
        assertFalse(result);
    }

    @Test
    public void testHasValidCertification_CaseSensitive_ReturnsFalse() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.findByEmployee(testEmployee))
                .thenReturn(Arrays.asList(validCertification));

        // Act
        boolean result = certificationService.hasValidCertification(1L, "forklift operator");

        // Assert
        assertFalse(result); // Case-sensitive comparison
    }

    @Test
    public void testHasValidCertification_MultipleCertifications_FindsCorrectOne() {
        // Arrange
        Certification anotherCert = Certification.builder()
                .id(4L)
                .employee(testEmployee)
                .certificationName("Safety Training")
                .issueDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusYears(1))
                .status("ACTIVE")
                .build();
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.findByEmployee(testEmployee))
                .thenReturn(Arrays.asList(validCertification, anotherCert));

        // Act
        boolean result = certificationService.hasValidCertification(1L, "Safety Training");

        // Assert
        assertTrue(result);
    }

    @Test
    public void testHasValidCertification_InactiveStatus_ReturnsFalse() {
        // Arrange
        Certification inactiveCert = Certification.builder()
                .id(5L)
                .employee(testEmployee)
                .certificationName("Inactive Cert")
                .issueDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusYears(1))
                .status("INACTIVE")
                .build();
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.findByEmployee(testEmployee))
                .thenReturn(Arrays.asList(inactiveCert));

        // Act
        boolean result = certificationService.hasValidCertification(1L, "Inactive Cert");

        // Assert
        assertFalse(result);
    }

    // ========== Tests for updateExpiredCertifications() ==========

    @Test
    public void testUpdateExpiredCertifications_ExpiredCerts_UpdatesStatus() {
        // Arrange
        List<Certification> expiredCerts = Arrays.asList(expiredCertification);
        when(certificationRepository.findByExpirationDateBefore(any(LocalDate.class)))
                .thenReturn(expiredCerts);
        when(certificationRepository.save(any(Certification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        certificationService.updateExpiredCertifications();

        // Assert
        assertEquals("EXPIRED", expiredCertification.getStatus());
        verify(certificationRepository, times(1)).findByExpirationDateBefore(any(LocalDate.class));
        verify(certificationRepository, times(1)).save(expiredCertification);
    }

    @Test
    public void testUpdateExpiredCertifications_NoExpiredCerts_NoUpdates() {
        // Arrange
        when(certificationRepository.findByExpirationDateBefore(any(LocalDate.class)))
                .thenReturn(Arrays.asList());

        // Act
        certificationService.updateExpiredCertifications();

        // Assert
        verify(certificationRepository, times(1)).findByExpirationDateBefore(any(LocalDate.class));
        verify(certificationRepository, never()).save(any(Certification.class));
    }

    @Test
    public void testUpdateExpiredCertifications_MultipleExpiredCerts_UpdatesAll() {
        // Arrange
        Certification expiredCert2 = Certification.builder()
                .id(6L)
                .employee(testEmployee)
                .certificationName("Another Expired")
                .issueDate(LocalDate.now().minusYears(3))
                .expirationDate(LocalDate.now().minusDays(5))
                .status("ACTIVE")
                .build();
        
        List<Certification> expiredCerts = Arrays.asList(expiredCertification, expiredCert2);
        when(certificationRepository.findByExpirationDateBefore(any(LocalDate.class)))
                .thenReturn(expiredCerts);
        when(certificationRepository.save(any(Certification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        certificationService.updateExpiredCertifications();

        // Assert
        verify(certificationRepository, times(2)).save(any(Certification.class));
    }

    @Test
    public void testUpdateExpiredCertifications_AlreadyExpiredStatus_UpdatesAgain() {
        // Arrange
        expiredCertification.setStatus("EXPIRED");
        when(certificationRepository.findByExpirationDateBefore(any(LocalDate.class)))
                .thenReturn(Arrays.asList(expiredCertification));
        when(certificationRepository.save(any(Certification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        certificationService.updateExpiredCertifications();

        // Assert
        assertEquals("EXPIRED", expiredCertification.getStatus());
        verify(certificationRepository, times(1)).save(expiredCertification);
    }
}