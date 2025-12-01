package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.entity.Certification;
import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.repository.CertificationRepository;
import com.warehouse.ems.employee.repository.EmployeeRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for CertificationService covering:
 * - Certification CRUD operations
 * - Expiry date validation
 * - Expiry alerts (30 days, 7 days)
 * - Document upload tracking
 * - Assignment blocking for expired certifications
 * - Edge cases and boundary conditions
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
    private Certification forkliftCert;
    private Certification safetyCert;

    @BeforeEach
    public void setUp() {
        // Arrange: Create test data
        testEmployee = Employee.builder()
                .id(1L)
                .badgeId("EMP001")
                .firstName("John")
                .lastName("Doe")
                .status("ACTIVE")
                .deleted(false)
                .build();

        forkliftCert = Certification.builder()
                .id(1L)
                .employeeId(1L)
                .name("Forklift Operator")
                .issueDate(LocalDate.of(2023, 1, 1))
                .expiryDate(LocalDate.of(2025, 1, 1))
                .documentUrl("https://docs.warehouse.com/cert001.pdf")
                .build();

        safetyCert = Certification.builder()
                .id(2L)
                .employeeId(1L)
                .name("Safety Training")
                .issueDate(LocalDate.of(2023, 6, 1))
                .expiryDate(LocalDate.of(2024, 6, 1))
                .documentUrl("https://docs.warehouse.com/cert002.pdf")
                .build();
    }

    // ========== NORMAL CASE TESTS ==========

    @Test
    public void testCreateCertification_WithValidData_ReturnsCreatedCertification() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(forkliftCert);

        // Act
        Certification result = certificationService.createCertification(forkliftCert);

        // Assert
        assertNotNull(result, "Certification should not be null");
        assertEquals("Forklift Operator", result.getName());
        assertEquals(1L, result.getEmployeeId());
        assertEquals(LocalDate.of(2025, 1, 1), result.getExpiryDate());
        verify(employeeRepository, times(1)).findById(1L);
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    public void testGetEmployeeCertifications_ReturnsAllCertifications() {
        // Arrange
        when(certificationRepository.findByEmployeeIdOrderByExpiryDateAsc(1L))
                .thenReturn(Arrays.asList(safetyCert, forkliftCert));

        // Act
        List<Certification> result = certificationService.getEmployeeCertifications(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Safety Training", result.get(0).getName());
        assertEquals("Forklift Operator", result.get(1).getName());
        verify(certificationRepository, times(1)).findByEmployeeIdOrderByExpiryDateAsc(1L);
    }

    @Test
    public void testUpdateCertification_WithValidData_ReturnsUpdatedCertification() {
        // Arrange
        Certification updatedCert = Certification.builder()
                .id(1L)
                .employeeId(1L)
                .name("Forklift Operator - Advanced")
                .issueDate(LocalDate.of(2024, 1, 1))
                .expiryDate(LocalDate.of(2026, 1, 1))
                .documentUrl("https://docs.warehouse.com/cert001_updated.pdf")
                .build();

        when(certificationRepository.findById(1L)).thenReturn(Optional.of(forkliftCert));
        when(certificationRepository.save(any(Certification.class))).thenReturn(updatedCert);

        // Act
        Certification result = certificationService.updateCertification(1L, updatedCert);

        // Assert
        assertNotNull(result);
        assertEquals("Forklift Operator - Advanced", result.getName());
        assertEquals(LocalDate.of(2026, 1, 1), result.getExpiryDate());
        verify(certificationRepository, times(1)).findById(1L);
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    public void testDeleteCertification_WithValidId_DeletesCertification() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(forkliftCert));
        doNothing().when(certificationRepository).deleteById(1L);

        // Act
        certificationService.deleteCertification(1L);

        // Assert
        verify(certificationRepository, times(1)).findById(1L);
        verify(certificationRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testIsValidCertification_WithValidCert_ReturnsTrue() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(forkliftCert));

        // Act
        boolean result = certificationService.isValidCertification(1L);

        // Assert
        assertTrue(result, "Certification should be valid");
        verify(certificationRepository, times(1)).findById(1L);
    }

    @Test
    public void testIsValidCertification_WithExpiredCert_ReturnsFalse() {
        // Arrange
        Certification expiredCert = Certification.builder()
                .id(3L)
                .employeeId(1L)
                .name("Expired Cert")
                .issueDate(LocalDate.of(2020, 1, 1))
                .expiryDate(LocalDate.of(2022, 1, 1))
                .build();

        when(certificationRepository.findById(3L)).thenReturn(Optional.of(expiredCert));

        // Act
        boolean result = certificationService.isValidCertification(3L);

        // Assert
        assertFalse(result, "Expired certification should be invalid");
        verify(certificationRepository, times(1)).findById(3L);
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    public void testCreateCertification_WithInvalidEmployeeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        forkliftCert.setEmployeeId(999L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.createCertification(forkliftCert);
        }, "Should throw exception for invalid employee ID");

        verify(employeeRepository, times(1)).findById(999L);
        verify(certificationRepository, never()).save(any(Certification.class));
    }

    @Test
    public void testCreateCertification_WithDeletedEmployee_ThrowsException() {
        // Arrange
        testEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            certificationService.createCertification(forkliftCert);
        }, "Should throw exception for deleted employee");

        verify(certificationRepository, never()).save(any(Certification.class));
    }

    @Test
    public void testCreateCertification_WithNullName_ThrowsException() {
        // Arrange
        Certification invalidCert = Certification.builder()
                .employeeId(1L)
                .issueDate(LocalDate.of(2023, 1, 1))
                .expiryDate(LocalDate.of(2025, 1, 1))
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.createCertification(invalidCert);
        }, "Should throw exception for null certification name");

        verify(certificationRepository, never()).save(any(Certification.class));
    }

    @Test
    public void testCreateCertification_WithEmptyName_ThrowsException() {
        // Arrange
        Certification invalidCert = Certification.builder()
                .employeeId(1L)
                .name("")
                .issueDate(LocalDate.of(2023, 1, 1))
                .expiryDate(LocalDate.of(2025, 1, 1))
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.createCertification(invalidCert);
        }, "Should throw exception for empty certification name");

        verify(certificationRepository, never()).save(any(Certification.class));
    }

    @Test
    public void testCreateCertification_WithExpiryBeforeIssue_ThrowsException() {
        // Arrange
        Certification invalidCert = Certification.builder()
                .employeeId(1L)
                .name("Invalid Cert")
                .issueDate(LocalDate.of(2025, 1, 1))
                .expiryDate(LocalDate.of(2023, 1, 1))
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.createCertification(invalidCert);
        }, "Should throw exception when expiry date is before issue date");

        verify(certificationRepository, never()).save(any(Certification.class));
    }

    @Test
    public void testUpdateCertification_WithInvalidId_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.updateCertification(999L, forkliftCert);
        }, "Should throw exception for invalid certification ID");

        verify(certificationRepository, times(1)).findById(999L);
        verify(certificationRepository, never()).save(any(Certification.class));
    }

    @Test
    public void testDeleteCertification_WithInvalidId_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.deleteCertification(999L);
        }, "Should throw exception for invalid certification ID");

        verify(certificationRepository, times(1)).findById(999L);
        verify(certificationRepository, never()).deleteById(anyLong());
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    public void testGetExpiringCertifications_Within30Days_ReturnsMatchingCerts() {
        // Arrange
        Certification expiringSoon = Certification.builder()
                .id(4L)
                .employeeId(1L)
                .name("Expiring Soon")
                .issueDate(LocalDate.now().minusYears(1))
                .expiryDate(LocalDate.now().plusDays(25))
                .build();

        when(certificationRepository.findExpiringWithinDays(30))
                .thenReturn(Arrays.asList(expiringSoon));

        // Act
        List<Certification> result = certificationService.getExpiringCertifications(30);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Expiring Soon", result.get(0).getName());
        verify(certificationRepository, times(1)).findExpiringWithinDays(30);
    }

    @Test
    public void testGetExpiringCertifications_Within7Days_ReturnsMatchingCerts() {
        // Arrange
        Certification expiringVeryS oon = Certification.builder()
                .id(5L)
                .employeeId(1L)
                .name("Expiring Very Soon")
                .issueDate(LocalDate.now().minusYears(1))
                .expiryDate(LocalDate.now().plusDays(5))
                .build();

        when(certificationRepository.findExpiringWithinDays(7))
                .thenReturn(Arrays.asList(expiringVeryS oon));

        // Act
        List<Certification> result = certificationService.getExpiringCertifications(7);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Expiring Very Soon", result.get(0).getName());
        verify(certificationRepository, times(1)).findExpiringWithinDays(7);
    }

    @Test
    public void testIsValidCertification_ExpiringToday_ReturnsTrue() {
        // Arrange
        Certification expiringToday = Certification.builder()
                .id(6L)
                .employeeId(1L)
                .name("Expiring Today")
                .issueDate(LocalDate.now().minusYears(1))
                .expiryDate(LocalDate.now())
                .build();

        when(certificationRepository.findById(6L)).thenReturn(Optional.of(expiringToday));

        // Act
        boolean result = certificationService.isValidCertification(6L);

        // Assert
        assertTrue(result, "Certification expiring today should still be valid");
    }

    @Test
    public void testIsValidCertification_ExpiredYesterday_ReturnsFalse() {
        // Arrange
        Certification expiredYesterday = Certification.builder()
                .id(7L)
                .employeeId(1L)
                .name("Expired Yesterday")
                .issueDate(LocalDate.now().minusYears(1))
                .expiryDate(LocalDate.now().minusDays(1))
                .build();

        when(certificationRepository.findById(7L)).thenReturn(Optional.of(expiredYesterday));

        // Act
        boolean result = certificationService.isValidCertification(7L);

        // Assert
        assertFalse(result, "Certification expired yesterday should be invalid");
    }

    @Test
    public void testCreateCertification_WithFutureIssueDate_ThrowsException() {
        // Arrange
        Certification futureCert = Certification.builder()
                .employeeId(1L)
                .name("Future Cert")
                .issueDate(LocalDate.now().plusDays(1))
                .expiryDate(LocalDate.now().plusYears(1))
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.createCertification(futureCert);
        }, "Should throw exception for future issue date");

        verify(certificationRepository, never()).save(any(Certification.class));
    }

    @Test
    public void testCreateCertification_IssuedToday_Success() {
        // Arrange
        Certification todayCert = Certification.builder()
                .employeeId(1L)
                .name("Today Cert")
                .issueDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(1))
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(todayCert);

        // Act
        Certification result = certificationService.createCertification(todayCert);

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.now(), result.getIssueDate());
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    public void testGetEmployeeCertifications_WithNoCertifications_ReturnsEmptyList() {
        // Arrange
        when(certificationRepository.findByEmployeeIdOrderByExpiryDateAsc(1L))
                .thenReturn(Arrays.asList());

        // Act
        List<Certification> result = certificationService.getEmployeeCertifications(1L);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(certificationRepository, times(1)).findByEmployeeIdOrderByExpiryDateAsc(1L);
    }

    @Test
    public void testCreateCertification_WithMaxLengthName_Success() {
        // Arrange
        String maxName = "A".repeat(64);
        Certification cert = Certification.builder()
                .employeeId(1L)
                .name(maxName)
                .issueDate(LocalDate.of(2023, 1, 1))
                .expiryDate(LocalDate.of(2025, 1, 1))
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(cert);

        // Act
        Certification result = certificationService.createCertification(cert);

        // Assert
        assertNotNull(result);
        assertEquals(64, result.getName().length());
    }

    @Test
    public void testCreateCertification_WithNullDocumentUrl_Success() {
        // Arrange
        Certification cert = Certification.builder()
                .employeeId(1L)
                .name("No Document Cert")
                .issueDate(LocalDate.of(2023, 1, 1))
                .expiryDate(LocalDate.of(2025, 1, 1))
                .documentUrl(null)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(cert);

        // Act
        Certification result = certificationService.createCertification(cert);

        // Assert
        assertNotNull(result);
        assertNull(result.getDocumentUrl());
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }
}