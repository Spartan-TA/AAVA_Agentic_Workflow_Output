package com.wms.ems.certification;

import com.wms.ems.employee.Employee;
import com.wms.ems.employee.EmployeeRepository;
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
 * Comprehensive JUnit test suite for CertificationService.
 * Tests cover certification CRUD operations, expiry alerts, assignment validation,
 * document uploads, and edge cases.
 * 
 * @author Warehouse EMS Test Suite
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
public class CertificationServiceTest {

    @Mock
    private CertificationRepository certificationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DocumentStorageService documentStorageService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CertificationService certificationService;

    private Employee testEmployee;
    private Certification testCertification;
    private CertificationDto certificationDto;

    @BeforeEach
    public void setUp() {
        // Arrange: Create test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");
        testEmployee.setStatus("ACTIVE");

        // Arrange: Create test certification
        testCertification = new Certification();
        testCertification.setId(1L);
        testCertification.setEmployeeId(1L);
        testCertification.setType("FORKLIFT");
        testCertification.setIssueDate(LocalDate.now().minusYears(2));
        testCertification.setExpiryDate(LocalDate.now().plusMonths(6));
        testCertification.setProofDocumentUrl("https://storage.example.com/cert123.pdf");
        testCertification.setStatus("ACTIVE");

        // Arrange: Create certification DTO
        certificationDto = new CertificationDto();
        certificationDto.setEmployeeId(1L);
        certificationDto.setType("FORKLIFT");
        certificationDto.setIssueDate(LocalDate.now().minusYears(2));
        certificationDto.setExpiryDate(LocalDate.now().plusMonths(6));
    }

    // ==================== CERTIFICATION CREATION TESTS ====================

    @Test
    public void testAddCertification_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDto result = certificationService.addCertification(certificationDto);

        // Assert
        assertNotNull(result);
        assertEquals("FORKLIFT", result.getType());
        assertEquals(1L, result.getEmployeeId());
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    public void testAddCertification_NullDto_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.addCertification(null);
        });
    }

    @Test
    public void testAddCertification_NullEmployeeId_ThrowsException() {
        // Arrange
        certificationDto.setEmployeeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.addCertification(certificationDto);
        });
    }

    @Test
    public void testAddCertification_InvalidEmployeeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        certificationDto.setEmployeeId(999L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.addCertification(certificationDto);
        });
    }

    @Test
    public void testAddCertification_NullType_ThrowsException() {
        // Arrange
        certificationDto.setType(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.addCertification(certificationDto);
        });
    }

    @Test
    public void testAddCertification_EmptyType_ThrowsException() {
        // Arrange
        certificationDto.setType("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.addCertification(certificationDto);
        });
    }

    @Test
    public void testAddCertification_InvalidType_ThrowsException() {
        // Arrange
        certificationDto.setType("INVALID_TYPE");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.addCertification(certificationDto);
        });
    }

    @Test
    public void testAddCertification_NullIssueDate_ThrowsException() {
        // Arrange
        certificationDto.setIssueDate(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.addCertification(certificationDto);
        });
    }

    @Test
    public void testAddCertification_FutureIssueDate_ThrowsException() {
        // Arrange
        certificationDto.setIssueDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.addCertification(certificationDto);
        });
    }

    @Test
    public void testAddCertification_NullExpiryDate_ThrowsException() {
        // Arrange
        certificationDto.setExpiryDate(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.addCertification(certificationDto);
        });
    }

    @Test
    public void testAddCertification_ExpiryBeforeIssue_ThrowsException() {
        // Arrange
        certificationDto.setIssueDate(LocalDate.now());
        certificationDto.setExpiryDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.addCertification(certificationDto);
        });
    }

    @Test
    public void testAddCertification_AlreadyExpired_ThrowsException() {
        // Arrange
        certificationDto.setExpiryDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.addCertification(certificationDto);
        });
    }

    // ==================== CERTIFICATION RETRIEVAL TESTS ====================

    @Test
    public void testGetCertificationById_ValidId_Success() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));

        // Act
        CertificationDto result = certificationService.getCertificationById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("FORKLIFT", result.getType());
    }

    @Test
    public void testGetCertificationById_InvalidId_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.getCertificationById(999L);
        });
    }

    @Test
    public void testGetCertificationsByEmployeeId_ValidId_Success() {
        // Arrange
        when(certificationRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList(testCertification));

        // Act
        List<CertificationDto> results = certificationService.getCertificationsByEmployeeId(1L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("FORKLIFT", results.get(0).getType());
    }

    @Test
    public void testGetCertificationsByEmployeeId_NoCertifications_ReturnsEmptyList() {
        // Arrange
        when(certificationRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList());

        // Act
        List<CertificationDto> results = certificationService.getCertificationsByEmployeeId(1L);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    // ==================== CERTIFICATION UPDATE TESTS ====================

    @Test
    public void testUpdateCertification_ValidInput_Success() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);
        certificationDto.setExpiryDate(LocalDate.now().plusYears(1));

        // Act
        CertificationDto result = certificationService.updateCertification(1L, certificationDto);

        // Assert
        assertNotNull(result);
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    public void testUpdateCertification_InvalidId_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.updateCertification(999L, certificationDto);
        });
    }

    @Test
    public void testUpdateCertification_ChangeType_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));
        certificationDto.setType("SAFETY");

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            certificationService.updateCertification(1L, certificationDto);
        });
    }

    // ==================== CERTIFICATION DELETION TESTS ====================

    @Test
    public void testDeleteCertification_ValidId_Success() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));

        // Act
        certificationService.deleteCertification(1L);

        // Assert
        verify(certificationRepository, times(1)).delete(any(Certification.class));
    }

    @Test
    public void testDeleteCertification_InvalidId_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.deleteCertification(999L);
        });
    }

    // ==================== EXPIRY ALERT TESTS ====================

    @Test
    public void testGetExpiringCertifications_30Days_Success() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().plusDays(25));
        when(certificationRepository.findExpiringCertifications(any(), any()))
            .thenReturn(Arrays.asList(testCertification));

        // Act
        List<CertificationDto> results = certificationService.getExpiringCertifications(30);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    public void testGetExpiringCertifications_7Days_Success() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().plusDays(5));
        when(certificationRepository.findExpiringCertifications(any(), any()))
            .thenReturn(Arrays.asList(testCertification));

        // Act
        List<CertificationDto> results = certificationService.getExpiringCertifications(7);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    public void testSendExpiryAlerts_30Days_Success() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().plusDays(25));
        when(certificationRepository.findExpiringCertifications(any(), any()))
            .thenReturn(Arrays.asList(testCertification));

        // Act
        certificationService.sendExpiryAlerts();

        // Assert
        verify(notificationService, atLeastOnce()).sendNotification(any());
    }

    @Test
    public void testSendExpiryAlerts_7Days_Success() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().plusDays(5));
        when(certificationRepository.findExpiringCertifications(any(), any()))
            .thenReturn(Arrays.asList(testCertification));

        // Act
        certificationService.sendExpiryAlerts();

        // Assert
        verify(notificationService, atLeastOnce()).sendNotification(any());
    }

    // ==================== CERTIFICATION VALIDATION TESTS ====================

    @Test
    public void testValidateCertification_Active_ReturnsTrue() {
        // Arrange
        when(certificationRepository.findByEmployeeIdAndType(1L, "FORKLIFT"))
            .thenReturn(Optional.of(testCertification));

        // Act
        boolean isValid = certificationService.validateCertification(1L, "FORKLIFT");

        // Assert
        assertTrue(isValid);
    }

    @Test
    public void testValidateCertification_Expired_ReturnsFalse() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().minusDays(1));
        when(certificationRepository.findByEmployeeIdAndType(1L, "FORKLIFT"))
            .thenReturn(Optional.of(testCertification));

        // Act
        boolean isValid = certificationService.validateCertification(1L, "FORKLIFT");

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateCertification_NotFound_ReturnsFalse() {
        // Arrange
        when(certificationRepository.findByEmployeeIdAndType(1L, "FORKLIFT"))
            .thenReturn(Optional.empty());

        // Act
        boolean isValid = certificationService.validateCertification(1L, "FORKLIFT");

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testBlockAssignment_ExpiredCertification_ThrowsException() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().minusDays(1));
        when(certificationRepository.findByEmployeeIdAndType(1L, "FORKLIFT"))
            .thenReturn(Optional.of(testCertification));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            certificationService.checkCertificationForAssignment(1L, "FORKLIFT");
        });
    }

    @Test
    public void testBlockAssignment_MissingCertification_ThrowsException() {
        // Arrange
        when(certificationRepository.findByEmployeeIdAndType(1L, "FORKLIFT"))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            certificationService.checkCertificationForAssignment(1L, "FORKLIFT");
        });
    }

    // ==================== DOCUMENT UPLOAD TESTS ====================

    @Test
    public void testUploadProofDocument_ValidInput_Success() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(testCertification));
        when(documentStorageService.uploadDocument(any(), anyString())).thenReturn("https://storage.example.com/cert456.pdf");
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);
        byte[] documentData = "test document".getBytes();

        // Act
        String documentUrl = certificationService.uploadProofDocument(1L, documentData, "cert.pdf");

        // Assert
        assertNotNull(documentUrl);
        assertTrue(documentUrl.contains("https://"));
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    public void testUploadProofDocument_NullData_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.uploadProofDocument(1L, null, "cert.pdf");
        });
    }

    @Test
    public void testUploadProofDocument_EmptyData_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.uploadProofDocument(1L, new byte[0], "cert.pdf");
        });
    }

    @Test
    public void testUploadProofDocument_InvalidFileType_ThrowsException() {
        // Arrange
        byte[] documentData = "test document".getBytes();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.uploadProofDocument(1L, documentData, "cert.exe");
        });
    }

    @Test
    public void testUploadProofDocument_FileTooLarge_ThrowsException() {
        // Arrange
        byte[] largeData = new byte[11 * 1024 * 1024]; // 11 MB

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.uploadProofDocument(1L, largeData, "cert.pdf");
        });
    }
}