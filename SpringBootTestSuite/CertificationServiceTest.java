package com.wms.certification.service;

import com.wms.certification.entity.Certification;
import com.wms.certification.repository.CertificationRepository;
import com.wms.certification.dto.CertificationDto;
import com.wms.certification.dto.CreateCertificationRequest;
import com.wms.employee.entity.Employee;
import com.wms.employee.repository.EmployeeRepository;
import com.wms.exception.ResourceNotFoundException;
import com.wms.exception.BadRequestException;
import com.wms.exception.ConflictException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for CertificationService
 * Covers CRUD operations, expiry alerts, validation, and document management
 */
public class CertificationServiceTest {

    @Mock
    private CertificationRepository certificationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private CertificationService certificationService;

    private Employee testEmployee;
    private Certification testCertification;
    private CreateCertificationRequest createRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("BADGE001");
        
        // Setup test certification
        testCertification = new Certification();
        testCertification.setId(1L);
        testCertification.setEmployeeId(1L);
        testCertification.setCertificationType("FORKLIFT");
        testCertification.setCertificationNumber("FL-2023-001");
        testCertification.setIssueDate(LocalDate.now().minusYears(1));
        testCertification.setExpiryDate(LocalDate.now().plusYears(2));
        testCertification.setStatus("ACTIVE");
        testCertification.setIssuingAuthority("OSHA");
        
        // Setup create request
        createRequest = new CreateCertificationRequest();
        createRequest.setEmployeeId(1L);
        createRequest.setCertificationType("FORKLIFT");
        createRequest.setCertificationNumber("FL-2023-002");
        createRequest.setIssueDate(LocalDate.now());
        createRequest.setExpiryDate(LocalDate.now().plusYears(3));
        createRequest.setIssuingAuthority("OSHA");
    }

    // ========== CREATE CERTIFICATION TESTS ==========

    @Test
    @DisplayName("Test create certification with valid data")
    public void testCreateCertification_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.findByEmployeeIdAndCertificationTypeAndStatus(1L, "FORKLIFT", "ACTIVE"))
            .thenReturn(Optional.empty());
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDto result = certificationService.createCertification(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals("FORKLIFT", result.getCertificationType());
        assertEquals("ACTIVE", result.getStatus());
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    @DisplayName("Test create certification for non-existent employee")
    public void testCreateCertification_NonExistentEmployee_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        createRequest.setEmployeeId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            certificationService.createCertification(createRequest);
        });
    }

    @Test
    @DisplayName("Test create duplicate active certification")
    public void testCreateCertification_DuplicateActive_ThrowsConflictException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.findByEmployeeIdAndCertificationTypeAndStatus(1L, "FORKLIFT", "ACTIVE"))
            .thenReturn(Optional.of(testCertification));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            certificationService.createCertification(createRequest);
        });
    }

    @Test
    @DisplayName("Test create certification with expiry before issue date")
    public void testCreateCertification_ExpiryBeforeIssue_ThrowsBadRequestException() {
        // Arrange
        createRequest.setIssueDate(LocalDate.now());
        createRequest.setExpiryDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            certificationService.createCertification(createRequest);
        });
    }

    @Test
    @DisplayName("Test create certification with null certification type")
    public void testCreateCertification_NullType_ThrowsBadRequestException() {
        // Arrange
        createRequest.setCertificationType(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            certificationService.createCertification(createRequest);
        });
    }

    @Test
    @DisplayName("Test create certification with empty certification number")
    public void testCreateCertification_EmptyCertNumber_ThrowsBadRequestException() {
        // Arrange
        createRequest.setCertificationNumber("");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            certificationService.createCertification(createRequest);
        });
    }

    // ========== READ CERTIFICATION TESTS ==========

    @Test
    @DisplayName("Test get certification by ID")
    public void testGetCertificationById_ValidId_Success() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));

        // Act
        CertificationDto result = certificationService.getCertificationById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("FORKLIFT", result.getCertificationType());
    }

    @Test
    @DisplayName("Test get certification by non-existent ID")
    public void testGetCertificationById_NonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        when(certificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            certificationService.getCertificationById(999L);
        });
    }

    @Test
    @DisplayName("Test get all certifications for employee")
    public void testGetCertificationsByEmployee_ValidEmployee_Success() {
        // Arrange
        when(certificationRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList(testCertification));

        // Act
        List<CertificationDto> result = certificationService.getCertificationsByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getEmployeeId());
    }

    @Test
    @DisplayName("Test get active certifications for employee")
    public void testGetActiveCertifications_ValidEmployee_Success() {
        // Arrange
        when(certificationRepository.findByEmployeeIdAndStatus(1L, "ACTIVE"))
            .thenReturn(Arrays.asList(testCertification));

        // Act
        List<CertificationDto> result = certificationService.getActiveCertifications(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.get(0).getStatus());
    }

    // ========== UPDATE CERTIFICATION TESTS ==========

    @Test
    @DisplayName("Test update certification")
    public void testUpdateCertification_ValidData_Success() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);
        createRequest.setExpiryDate(LocalDate.now().plusYears(4));

        // Act
        CertificationDto result = certificationService.updateCertification(1L, createRequest);

        // Assert
        assertNotNull(result);
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    @DisplayName("Test update non-existent certification")
    public void testUpdateCertification_NonExistentId_ThrowsResourceNotFoundException() {
        // Arrange
        when(certificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            certificationService.updateCertification(999L, createRequest);
        });
    }

    // ========== DELETE/REVOKE CERTIFICATION TESTS ==========

    @Test
    @DisplayName("Test revoke certification")
    public void testRevokeCertification_ValidId_Success() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        certificationService.revokeCertification(1L, "Failed safety inspection");

        // Assert
        assertEquals("REVOKED", testCertification.getStatus());
        verify(certificationRepository, times(1)).save(testCertification);
    }

    @Test
    @DisplayName("Test revoke already revoked certification")
    public void testRevokeCertification_AlreadyRevoked_ThrowsConflictException() {
        // Arrange
        testCertification.setStatus("REVOKED");
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            certificationService.revokeCertification(1L, "Reason");
        });
    }

    // ========== EXPIRY ALERT TESTS ==========

    @Test
    @DisplayName("Test get certifications expiring in 30 days")
    public void testGetCertificationsExpiringIn30Days_ReturnsExpiring() {
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
    public void testGetCertificationsExpiringIn7Days_ReturnsExpiring() {
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
    public void testGetExpiredCertifications_ReturnsExpired() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().minusDays(1));
        testCertification.setStatus("EXPIRED");
        when(certificationRepository.findByStatus("EXPIRED"))
            .thenReturn(Arrays.asList(testCertification));

        // Act
        List<CertificationDto> result = certificationService.getExpiredCertifications();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("EXPIRED", result.get(0).getStatus());
    }

    @Test
    @DisplayName("Test auto-expire certifications")
    public void testAutoExpireCertifications_UpdatesExpired() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().minusDays(1));
        when(certificationRepository.findByExpiryDateBeforeAndStatus(any(LocalDate.class), anyString()))
            .thenReturn(Arrays.asList(testCertification));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        certificationService.autoExpireCertifications();

        // Assert
        assertEquals("EXPIRED", testCertification.getStatus());
        verify(certificationRepository, times(1)).save(testCertification);
    }

    // ========== VALIDATION TESTS ==========

    @Test
    @DisplayName("Test validate certification for task assignment - valid")
    public void testValidateCertificationForTask_ValidCert_ReturnsTrue() {
        // Arrange
        when(certificationRepository.findByEmployeeIdAndCertificationTypeAndStatus(1L, "FORKLIFT", "ACTIVE"))
            .thenReturn(Optional.of(testCertification));

        // Act
        boolean result = certificationService.hasValidCertification(1L, "FORKLIFT");

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Test validate certification for task assignment - expired")
    public void testValidateCertificationForTask_ExpiredCert_ReturnsFalse() {
        // Arrange
        testCertification.setStatus("EXPIRED");
        when(certificationRepository.findByEmployeeIdAndCertificationTypeAndStatus(1L, "FORKLIFT", "ACTIVE"))
            .thenReturn(Optional.empty());

        // Act
        boolean result = certificationService.hasValidCertification(1L, "FORKLIFT");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Test validate certification for task assignment - not found")
    public void testValidateCertificationForTask_NotFound_ReturnsFalse() {
        // Arrange
        when(certificationRepository.findByEmployeeIdAndCertificationTypeAndStatus(1L, "FORKLIFT", "ACTIVE"))
            .thenReturn(Optional.empty());

        // Act
        boolean result = certificationService.hasValidCertification(1L, "FORKLIFT");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Test block task assignment without valid certification")
    public void testBlockTaskAssignment_NoCert_ThrowsBadRequestException() {
        // Arrange
        when(certificationRepository.findByEmployeeIdAndCertificationTypeAndStatus(1L, "FORKLIFT", "ACTIVE"))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            certificationService.validateCertificationForTaskAssignment(1L, "FORKLIFT");
        });
    }

    // ========== DOCUMENT MANAGEMENT TESTS ==========

    @Test
    @DisplayName("Test upload certification document")
    public void testUploadCertificationDocument_ValidData_Success() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);
        String documentUrl = "https://storage.example.com/certs/FL-2023-001.pdf";

        // Act
        certificationService.uploadDocument(1L, documentUrl);

        // Assert
        assertEquals(documentUrl, testCertification.getDocumentUrl());
        verify(certificationRepository, times(1)).save(testCertification);
    }

    @Test
    @DisplayName("Test upload document for non-existent certification")
    public void testUploadCertificationDocument_NonExistentCert_ThrowsResourceNotFoundException() {
        // Arrange
        when(certificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            certificationService.uploadDocument(999L, "https://example.com/doc.pdf");
        });
    }

    @Test
    @DisplayName("Test upload document with invalid URL")
    public void testUploadCertificationDocument_InvalidUrl_ThrowsBadRequestException() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            certificationService.uploadDocument(1L, "invalid-url");
        });
    }

    // ========== CERTIFICATION STATUS TESTS ==========

    @Test
    @DisplayName("Test get certification status on employee profile")
    public void testGetCertificationStatus_ValidEmployee_ReturnsStatus() {
        // Arrange
        when(certificationRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList(testCertification));

        // Act
        List<CertificationDto> result = certificationService.getCertificationStatus(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.get(0).getStatus());
    }

    @Test
    @DisplayName("Test certification status includes expiry warning")
    public void testGetCertificationStatus_ExpiringCert_IncludesWarning() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().plusDays(20));
        when(certificationRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList(testCertification));

        // Act
        List<CertificationDto> result = certificationService.getCertificationStatus(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.get(0).isExpiringWithin30Days());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test create certification with same-day issue and expiry")
    public void testCreateCertification_SameDayExpiry_ThrowsBadRequestException() {
        // Arrange
        createRequest.setIssueDate(LocalDate.now());
        createRequest.setExpiryDate(LocalDate.now());

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            certificationService.createCertification(createRequest);
        });
    }

    @Test
    @DisplayName("Test create certification with maximum validity period")
    public void testCreateCertification_MaxValidityPeriod_Success() {
        // Arrange
        createRequest.setIssueDate(LocalDate.now());
        createRequest.setExpiryDate(LocalDate.now().plusYears(10));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.findByEmployeeIdAndCertificationTypeAndStatus(1L, "FORKLIFT", "ACTIVE"))
            .thenReturn(Optional.empty());
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDto result = certificationService.createCertification(createRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test get certifications by type")
    public void testGetCertificationsByType_ValidType_Success() {
        // Arrange
        when(certificationRepository.findByCertificationType("FORKLIFT"))
            .thenReturn(Arrays.asList(testCertification));

        // Act
        List<CertificationDto> result = certificationService.getCertificationsByType("FORKLIFT");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("FORKLIFT", result.get(0).getCertificationType());
    }

    @Test
    @DisplayName("Test renew certification")
    public void testRenewCertification_ValidCert_Success() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);
        LocalDate newExpiryDate = LocalDate.now().plusYears(3);

        // Act
        CertificationDto result = certificationService.renewCertification(1L, newExpiryDate);

        // Assert
        assertNotNull(result);
        assertEquals(newExpiryDate, result.getExpiryDate());
        assertEquals("ACTIVE", result.getStatus());
    }

    @Test
    @DisplayName("Test get certification history for employee")
    public void testGetCertificationHistory_ValidEmployee_Success() {
        // Arrange
        Certification expiredCert = new Certification();
        expiredCert.setId(2L);
        expiredCert.setEmployeeId(1L);
        expiredCert.setCertificationType("FORKLIFT");
        expiredCert.setStatus("EXPIRED");
        
        when(certificationRepository.findByEmployeeIdOrderByIssueDateDesc(1L))
            .thenReturn(Arrays.asList(testCertification, expiredCert));

        // Act
        List<CertificationDto> result = certificationService.getCertificationHistory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Test send expiry notification")
    public void testSendExpiryNotification_ExpiringCerts_SendsNotifications() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().plusDays(25));
        when(certificationRepository.findExpiringBetween(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Arrays.asList(testCertification));

        // Act
        certificationService.sendExpiryNotifications();

        // Assert
        // Verify notification service was called (would need notification service mock)
        verify(certificationRepository, times(1)).findExpiringBetween(any(LocalDate.class), any(LocalDate.class));
    }
}