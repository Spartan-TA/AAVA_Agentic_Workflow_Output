package com.warehouse.ems.training.service;

import com.warehouse.ems.training.dto.CertificationDto;
import com.warehouse.ems.training.entity.Certification;
import com.warehouse.ems.training.entity.CertificationStatus;
import com.warehouse.ems.training.repository.CertificationRepository;
import com.warehouse.ems.training.service.impl.CertificationServiceImpl;
import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.repository.EmployeeRepository;
import com.warehouse.ems.exception.ResourceNotFoundException;
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
 * Comprehensive unit tests for CertificationService
 * Tests certification management, expiry tracking, and validation
 */
@ExtendWith(MockitoExtension.class)
public class CertificationServiceTest {

    @Mock
    private CertificationRepository certificationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private CertificationServiceImpl certificationService;

    private Employee testEmployee;
    private Certification testCertification;
    private CertificationDto testCertificationDto;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        testCertification = new Certification();
        testCertification.setId(1L);
        testCertification.setEmployee(testEmployee);
        testCertification.setCertificationName("Forklift Operator");
        testCertification.setIssueDate(LocalDate.now().minusYears(2));
        testCertification.setExpiryDate(LocalDate.now().plusYears(1));
        testCertification.setStatus(CertificationStatus.ACTIVE);
        testCertification.setDocumentUrl("https://docs.example.com/cert123.pdf");

        testCertificationDto = new CertificationDto();
        testCertificationDto.setEmployeeId(1L);
        testCertificationDto.setCertificationName("Forklift Operator");
        testCertificationDto.setIssueDate(LocalDate.now().minusYears(2));
        testCertificationDto.setExpiryDate(LocalDate.now().plusYears(1));
        testCertificationDto.setDocumentUrl("https://docs.example.com/cert123.pdf");
    }

    // ========== CREATE CERTIFICATION TESTS ==========

    @Test
    void testCreateCertification_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDto result = certificationService.createCertification(testCertificationDto);

        // Assert
        assertNotNull(result);
        assertEquals("Forklift Operator", result.getCertificationName());
        assertEquals(CertificationStatus.ACTIVE, result.getStatus());
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    void testCreateCertification_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        testCertificationDto.setEmployeeId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> certificationService.createCertification(testCertificationDto));
        verify(certificationRepository, never()).save(any(Certification.class));
    }

    @Test
    void testCreateCertification_ExpiryBeforeIssue_ThrowsException() {
        // Arrange
        testCertificationDto.setIssueDate(LocalDate.now());
        testCertificationDto.setExpiryDate(LocalDate.now().minusDays(1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> certificationService.createCertification(testCertificationDto));
    }

    @Test
    void testCreateCertification_NullEmployeeId_ThrowsException() {
        // Arrange
        testCertificationDto.setEmployeeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> certificationService.createCertification(testCertificationDto));
    }

    @Test
    void testCreateCertification_EmptyCertificationName_ThrowsException() {
        // Arrange
        testCertificationDto.setCertificationName("");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> certificationService.createCertification(testCertificationDto));
    }

    @Test
    void testCreateCertification_NullCertificationName_ThrowsException() {
        // Arrange
        testCertificationDto.setCertificationName(null);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> certificationService.createCertification(testCertificationDto));
    }

    @Test
    void testCreateCertification_FutureIssueDate_ThrowsException() {
        // Arrange
        testCertificationDto.setIssueDate(LocalDate.now().plusDays(1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> certificationService.createCertification(testCertificationDto));
    }

    @Test
    void testCreateCertification_AlreadyExpired_SetsExpiredStatus() {
        // Arrange
        testCertificationDto.setExpiryDate(LocalDate.now().minusDays(1));
        testCertification.setStatus(CertificationStatus.EXPIRED);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDto result = certificationService.createCertification(testCertificationDto);

        // Assert
        assertNotNull(result);
        assertEquals(CertificationStatus.EXPIRED, result.getStatus());
    }

    // ========== GET CERTIFICATION TESTS ==========

    @Test
    void testGetCertificationById_ValidId_ReturnsCertification() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));

        // Act
        CertificationDto result = certificationService.getCertificationById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Forklift Operator", result.getCertificationName());
    }

    @Test
    void testGetCertificationById_NonExistentId_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> certificationService.getCertificationById(999L));
    }

    @Test
    void testGetCertificationById_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> certificationService.getCertificationById(null));
    }

    @Test
    void testGetCertificationsByEmployee_ValidEmployee_ReturnsList() {
        // Arrange
        List<Certification> certifications = Arrays.asList(testCertification);
        when(certificationRepository.findByEmployeeId(1L)).thenReturn(certifications);

        // Act
        List<CertificationDto> result = certificationService.getCertificationsByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getEmployeeId());
    }

    @Test
    void testGetCertificationsByEmployee_NoRecords_ReturnsEmptyList() {
        // Arrange
        when(certificationRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList());

        // Act
        List<CertificationDto> result = certificationService.getCertificationsByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== EXPIRY TRACKING TESTS ==========

    @Test
    void testGetExpiringCertifications_Within30Days_ReturnsMatching() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().plusDays(15));
        List<Certification> expiringCerts = Arrays.asList(testCertification);
        when(certificationRepository.findExpiringWithinDays(30)).thenReturn(expiringCerts);

        // Act
        List<CertificationDto> result = certificationService.getExpiringCertifications(30);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getExpiryDate().isBefore(LocalDate.now().plusDays(31)));
    }

    @Test
    void testGetExpiringCertifications_NoneExpiring_ReturnsEmpty() {
        // Arrange
        when(certificationRepository.findExpiringWithinDays(30)).thenReturn(Arrays.asList());

        // Act
        List<CertificationDto> result = certificationService.getExpiringCertifications(30);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetExpiredCertifications_ReturnsOnlyExpired() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().minusDays(1));
        testCertification.setStatus(CertificationStatus.EXPIRED);
        List<Certification> expiredCerts = Arrays.asList(testCertification);
        when(certificationRepository.findByStatus(CertificationStatus.EXPIRED)).thenReturn(expiredCerts);

        // Act
        List<CertificationDto> result = certificationService.getExpiredCertifications();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(CertificationStatus.EXPIRED, result.get(0).getStatus());
    }

    @Test
    void testCheckAndUpdateExpiredCertifications_UpdatesStatus() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().minusDays(1));
        testCertification.setStatus(CertificationStatus.ACTIVE);
        List<Certification> activeCerts = Arrays.asList(testCertification);
        when(certificationRepository.findByStatus(CertificationStatus.ACTIVE)).thenReturn(activeCerts);
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        certificationService.checkAndUpdateExpiredCertifications();

        // Assert
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    // ========== UPDATE CERTIFICATION TESTS ==========

    @Test
    void testUpdateCertification_ValidInput_Success() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);
        testCertificationDto.setExpiryDate(LocalDate.now().plusYears(2));

        // Act
        CertificationDto result = certificationService.updateCertification(1L, testCertificationDto);

        // Assert
        assertNotNull(result);
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    void testUpdateCertification_NonExistentId_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> certificationService.updateCertification(999L, testCertificationDto));
    }

    @Test
    void testUpdateCertification_InvalidDates_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));
        testCertificationDto.setExpiryDate(LocalDate.now().minusYears(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> certificationService.updateCertification(1L, testCertificationDto));
    }

    // ========== RENEW CERTIFICATION TESTS ==========

    @Test
    void testRenewCertification_ValidRenewal_Success() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);
        LocalDate newExpiryDate = LocalDate.now().plusYears(3);

        // Act
        CertificationDto result = certificationService.renewCertification(1L, newExpiryDate);

        // Assert
        assertNotNull(result);
        assertEquals(CertificationStatus.ACTIVE, result.getStatus());
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    void testRenewCertification_NonExistentId_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> certificationService.renewCertification(999L, LocalDate.now().plusYears(1)));
    }

    @Test
    void testRenewCertification_PastExpiryDate_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> certificationService.renewCertification(1L, LocalDate.now().minusDays(1)));
    }

    // ========== DELETE CERTIFICATION TESTS ==========

    @Test
    void testDeleteCertification_ValidId_Success() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));
        doNothing().when(certificationRepository).delete(any(Certification.class));

        // Act
        certificationService.deleteCertification(1L);

        // Assert
        verify(certificationRepository, times(1)).delete(any(Certification.class));
    }

    @Test
    void testDeleteCertification_NonExistentId_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> certificationService.deleteCertification(999L));
    }

    // ========== VALIDATION TESTS ==========

    @Test
    void testValidateCertification_ActiveAndNotExpired_ReturnsTrue() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));

        // Act
        boolean result = certificationService.isCertificationValid(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void testValidateCertification_Expired_ReturnsFalse() {
        // Arrange
        testCertification.setStatus(CertificationStatus.EXPIRED);
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));

        // Act
        boolean result = certificationService.isCertificationValid(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateCertification_PendingRenewal_ReturnsFalse() {
        // Arrange
        testCertification.setStatus(CertificationStatus.PENDING_RENEWAL);
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));

        // Act
        boolean result = certificationService.isCertificationValid(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void testHasValidCertification_EmployeeWithValidCert_ReturnsTrue() {
        // Arrange
        List<Certification> certifications = Arrays.asList(testCertification);
        when(certificationRepository.findByEmployeeIdAndCertificationName(1L, "Forklift Operator"))
            .thenReturn(certifications);

        // Act
        boolean result = certificationService.hasValidCertification(1L, "Forklift Operator");

        // Assert
        assertTrue(result);
    }

    @Test
    void testHasValidCertification_EmployeeWithoutCert_ReturnsFalse() {
        // Arrange
        when(certificationRepository.findByEmployeeIdAndCertificationName(1L, "Crane Operator"))
            .thenReturn(Arrays.asList());

        // Act
        boolean result = certificationService.hasValidCertification(1L, "Crane Operator");

        // Assert
        assertFalse(result);
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    void testCreateCertification_ExpiryTomorrow_Success() {
        // Arrange
        testCertificationDto.setExpiryDate(LocalDate.now().plusDays(1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDto result = certificationService.createCertification(testCertificationDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testCreateCertification_LongTermCertification_Success() {
        // Arrange
        testCertificationDto.setExpiryDate(LocalDate.now().plusYears(10));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDto result = certificationService.createCertification(testCertificationDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetCertificationsByEmployee_MultipleCertifications_ReturnsAll() {
        // Arrange
        Certification cert2 = new Certification();
        cert2.setId(2L);
        cert2.setEmployee(testEmployee);
        cert2.setCertificationName("Safety Training");
        List<Certification> certifications = Arrays.asList(testCertification, cert2);
        when(certificationRepository.findByEmployeeId(1L)).thenReturn(certifications);

        // Act
        List<CertificationDto> result = certificationService.getCertificationsByEmployee(1L);

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void testCreateCertification_MaxLengthCertificationName_Success() {
        // Arrange
        testCertificationDto.setCertificationName("A".repeat(255));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDto result = certificationService.createCertification(testCertificationDto);

        // Assert
        assertNotNull(result);
    }
}