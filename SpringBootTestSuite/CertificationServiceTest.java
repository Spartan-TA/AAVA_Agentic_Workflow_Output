package com.wms.certification.service;

import com.wms.certification.domain.Certification;
import com.wms.certification.domain.CertificationStatus;
import com.wms.certification.dto.CertificationDto;
import com.wms.certification.repository.CertificationRepository;
import com.wms.employee.domain.Employee;
import com.wms.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for CertificationService
 * Tests cover certification tracking, expiry alerts, validation, and edge cases
 */
@DisplayName("Certification Service Tests")
public class CertificationServiceTest {

    @Mock
    private CertificationRepository certificationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private CertificationServiceImpl certificationService;

    private Employee testEmployee;
    private Certification testCertification;
    private CertificationDto certificationDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        // Setup test certification
        testCertification = new Certification();
        testCertification.setId(1L);
        testCertification.setEmployee(testEmployee);
        testCertification.setType("Forklift Operator");
        testCertification.setIssueDate(LocalDate.now().minusYears(1));
        testCertification.setExpiryDate(LocalDate.now().plusYears(2));
        testCertification.setProofDocumentUrl("https://example.com/cert.pdf");
        testCertification.setStatus(CertificationStatus.ACTIVE);

        // Setup DTO
        certificationDto = new CertificationDto();
        certificationDto.setEmployeeId(1L);
        certificationDto.setType("Forklift Operator");
        certificationDto.setIssueDate(LocalDate.now().minusYears(1));
        certificationDto.setExpiryDate(LocalDate.now().plusYears(2));
        certificationDto.setProofDocumentUrl("https://example.com/cert.pdf");
    }

    // ========== CREATE CERTIFICATION TESTS ==========

    @Test
    @DisplayName("Test create certification with valid data")
    public void testCreateCertification_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDto result = certificationService.createCertification(certificationDto);

        // Assert
        assertNotNull(result);
        assertEquals("Forklift Operator", result.getType());
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    @DisplayName("Test create certification with null employee ID throws exception")
    public void testCreateCertification_NullEmployeeId_ThrowsException() {
        // Arrange
        certificationDto.setEmployeeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.createCertification(certificationDto);
        });
    }

    @Test
    @DisplayName("Test create certification with non-existent employee throws exception")
    public void testCreateCertification_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.createCertification(certificationDto);
        });
    }

    @Test
    @DisplayName("Test create certification with null type throws exception")
    public void testCreateCertification_NullType_ThrowsException() {
        // Arrange
        certificationDto.setType(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.createCertification(certificationDto);
        });
    }

    @Test
    @DisplayName("Test create certification with empty type throws exception")
    public void testCreateCertification_EmptyType_ThrowsException() {
        // Arrange
        certificationDto.setType("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.createCertification(certificationDto);
        });
    }

    @Test
    @DisplayName("Test create certification with null issue date throws exception")
    public void testCreateCertification_NullIssueDate_ThrowsException() {
        // Arrange
        certificationDto.setIssueDate(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.createCertification(certificationDto);
        });
    }

    @Test
    @DisplayName("Test create certification with null expiry date throws exception")
    public void testCreateCertification_NullExpiryDate_ThrowsException() {
        // Arrange
        certificationDto.setExpiryDate(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.createCertification(certificationDto);
        });
    }

    @Test
    @DisplayName("Test create certification with expiry before issue throws exception")
    public void testCreateCertification_ExpiryBeforeIssue_ThrowsException() {
        // Arrange
        certificationDto.setIssueDate(LocalDate.now());
        certificationDto.setExpiryDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.createCertification(certificationDto);
        });
    }

    @Test
    @DisplayName("Test create certification with future issue date throws exception")
    public void testCreateCertification_FutureIssueDate_ThrowsException() {
        // Arrange
        certificationDto.setIssueDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.createCertification(certificationDto);
        });
    }

    // ========== RENEW CERTIFICATION TESTS ==========

    @Test
    @DisplayName("Test renew certification with valid data")
    public void testRenewCertification_ValidData_Success() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDto result = certificationService.renewCertification(1L, LocalDate.now().plusYears(3));

        // Assert
        assertNotNull(result);
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    @DisplayName("Test renew certification with null ID throws exception")
    public void testRenewCertification_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.renewCertification(null, LocalDate.now().plusYears(3));
        });
    }

    @Test
    @DisplayName("Test renew certification with null expiry date throws exception")
    public void testRenewCertification_NullExpiryDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.renewCertification(1L, null);
        });
    }

    @Test
    @DisplayName("Test renew non-existent certification throws exception")
    public void testRenewCertification_NonExistent_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.renewCertification(999L, LocalDate.now().plusYears(3));
        });
    }

    @Test
    @DisplayName("Test renew certification with past expiry date throws exception")
    public void testRenewCertification_PastExpiryDate_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.renewCertification(1L, LocalDate.now().minusDays(1));
        });
    }

    // ========== EXPIRY ALERT TESTS ==========

    @Test
    @DisplayName("Test get certifications expiring in 30 days")
    public void testGetCertificationsExpiringIn30Days_Success() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().plusDays(25));
        when(certificationRepository.findExpiringBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(testCertification));

        // Act
        List<CertificationDto> result = certificationService.getCertificationsExpiringIn30Days();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test get certifications expiring in 7 days")
    public void testGetCertificationsExpiringIn7Days_Success() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().plusDays(5));
        when(certificationRepository.findExpiringBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(testCertification));

        // Act
        List<CertificationDto> result = certificationService.getCertificationsExpiringIn7Days();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test get expired certifications")
    public void testGetExpiredCertifications_Success() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().minusDays(1));
        testCertification.setStatus(CertificationStatus.EXPIRED);
        when(certificationRepository.findByStatus(CertificationStatus.EXPIRED))
                .thenReturn(Arrays.asList(testCertification));

        // Act
        List<CertificationDto> result = certificationService.getExpiredCertifications();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== VALIDATION TESTS ==========

    @Test
    @DisplayName("Test is certification valid for active certification")
    public void testIsCertificationValid_ActiveCertification_ReturnsTrue() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));

        // Act
        boolean result = certificationService.isCertificationValid(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Test is certification valid for expired certification")
    public void testIsCertificationValid_ExpiredCertification_ReturnsFalse() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().minusDays(1));
        testCertification.setStatus(CertificationStatus.EXPIRED);
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));

        // Act
        boolean result = certificationService.isCertificationValid(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Test is certification valid with null ID throws exception")
    public void testIsCertificationValid_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.isCertificationValid(null);
        });
    }

    @Test
    @DisplayName("Test has valid certification for employee with type")
    public void testHasValidCertification_ValidCertification_ReturnsTrue() {
        // Arrange
        when(certificationRepository.findByEmployeeIdAndType(1L, "Forklift Operator"))
                .thenReturn(Arrays.asList(testCertification));

        // Act
        boolean result = certificationService.hasValidCertification(1L, "Forklift Operator");

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Test has valid certification with null employee ID throws exception")
    public void testHasValidCertification_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.hasValidCertification(null, "Forklift Operator");
        });
    }

    @Test
    @DisplayName("Test has valid certification with null type throws exception")
    public void testHasValidCertification_NullType_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.hasValidCertification(1L, null);
        });
    }

    // ========== GET CERTIFICATIONS TESTS ==========

    @Test
    @DisplayName("Test get certifications for employee")
    public void testGetCertificationsForEmployee_Success() {
        // Arrange
        when(certificationRepository.findByEmployeeId(1L))
                .thenReturn(Arrays.asList(testCertification));

        // Act
        List<CertificationDto> result = certificationService.getCertificationsForEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test get certifications with null employee ID throws exception")
    public void testGetCertificationsForEmployee_NullEmployeeId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.getCertificationsForEmployee(null);
        });
    }

    @Test
    @DisplayName("Test get certifications returns empty list when no certifications")
    public void testGetCertificationsForEmployee_NoCertifications_ReturnsEmptyList() {
        // Arrange
        when(certificationRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList());

        // Act
        List<CertificationDto> result = certificationService.getCertificationsForEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ========== UPDATE CERTIFICATION TESTS ==========

    @Test
    @DisplayName("Test update certification status")
    public void testUpdateCertificationStatus_ValidData_Success() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDto result = certificationService.updateCertificationStatus(1L, CertificationStatus.EXPIRED);

        // Assert
        assertNotNull(result);
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    @DisplayName("Test update certification status with null ID throws exception")
    public void testUpdateCertificationStatus_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.updateCertificationStatus(null, CertificationStatus.EXPIRED);
        });
    }

    @Test
    @DisplayName("Test update certification status with null status throws exception")
    public void testUpdateCertificationStatus_NullStatus_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.updateCertificationStatus(1L, null);
        });
    }

    // ========== DELETE CERTIFICATION TESTS ==========

    @Test
    @DisplayName("Test delete certification with valid ID")
    public void testDeleteCertification_ValidId_Success() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));
        doNothing().when(certificationRepository).delete(any(Certification.class));

        // Act
        certificationService.deleteCertification(1L);

        // Assert
        verify(certificationRepository, times(1)).delete(any(Certification.class));
    }

    @Test
    @DisplayName("Test delete certification with null ID throws exception")
    public void testDeleteCertification_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.deleteCertification(null);
        });
    }

    @Test
    @DisplayName("Test delete non-existent certification throws exception")
    public void testDeleteCertification_NonExistent_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.deleteCertification(999L);
        });
    }

    // ========== BOUNDARY AND EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test create certification with issue date today")
    public void testCreateCertification_IssueDateToday_Success() {
        // Arrange
        certificationDto.setIssueDate(LocalDate.now());
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDto result = certificationService.createCertification(certificationDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create certification with very old issue date")
    public void testCreateCertification_VeryOldIssueDate_Success() {
        // Arrange
        certificationDto.setIssueDate(LocalDate.of(2000, 1, 1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDto result = certificationService.createCertification(certificationDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create certification expiring tomorrow")
    public void testCreateCertification_ExpiringTomorrow_Success() {
        // Arrange
        certificationDto.setIssueDate(LocalDate.now().minusYears(1));
        certificationDto.setExpiryDate(LocalDate.now().plusDays(1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDto result = certificationService.createCertification(certificationDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test renew certification with same expiry date")
    public void testRenewCertification_SameExpiryDate_Success() {
        // Arrange
        LocalDate currentExpiry = testCertification.getExpiryDate();
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDto result = certificationService.renewCertification(1L, currentExpiry);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test get certifications expiring today")
    public void testGetCertificationsExpiringToday_Success() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now());
        when(certificationRepository.findExpiringBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(testCertification));

        // Act
        List<CertificationDto> result = certificationService.getCertificationsExpiringIn7Days();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test create certification with special characters in type")
    public void testCreateCertification_SpecialCharactersInType_Success() {
        // Arrange
        certificationDto.setType("Forklift Operator - Class A/B");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDto result = certificationService.createCertification(certificationDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create certification with maximum length type")
    public void testCreateCertification_MaxLengthType_Success() {
        // Arrange
        String maxLengthType = "A".repeat(255);
        certificationDto.setType(maxLengthType);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDto result = certificationService.createCertification(certificationDto);

        // Assert
        assertNotNull(result);
    }
}