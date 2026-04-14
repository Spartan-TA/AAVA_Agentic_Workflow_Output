package com.wms.ems.service;

import com.wms.ems.dto.CertificationCreateDTO;
import com.wms.ems.dto.CertificationDTO;
import com.wms.ems.dto.EmployeeCertificationCreateDTO;
import com.wms.ems.dto.EmployeeCertificationDTO;
import com.wms.ems.entity.Certification;
import com.wms.ems.entity.EmployeeCertification;
import com.wms.ems.entity.Employee;
import com.wms.ems.exception.EntityNotFoundException;
import com.wms.ems.exception.ValidationException;
import com.wms.ems.repository.CertificationRepository;
import com.wms.ems.repository.EmployeeCertificationRepository;
import com.wms.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for CertificationService.
 * Tests cover CRUD operations, expiry alerts, assignment blocking, and all edge cases.
 * 
 * @author EMS Test Suite Generator
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Certification Service Tests")
class CertificationServiceTest {

    @Mock
    private CertificationRepository certificationRepository;

    @Mock
    private EmployeeCertificationRepository employeeCertificationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private CertificationService certificationService;

    private Certification testCertification;
    private EmployeeCertification testEmployeeCertification;
    private Employee testEmployee;
    private CertificationCreateDTO certificationCreateDTO;
    private EmployeeCertificationCreateDTO employeeCertificationCreateDTO;

    @BeforeEach
    void setUp() {
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");

        // Setup test certification
        testCertification = new Certification();
        testCertification.setId(1L);
        testCertification.setName("Forklift Operator");
        testCertification.setDescription("Certification for operating forklifts");
        testCertification.setValidityPeriodDays(365);
        testCertification.setRequired(true);

        // Setup test employee certification
        testEmployeeCertification = new EmployeeCertification();
        testEmployeeCertification.setId(1L);
        testEmployeeCertification.setEmployee(testEmployee);
        testEmployeeCertification.setCertification(testCertification);
        testEmployeeCertification.setIssueDate(LocalDate.now().minusDays(300));
        testEmployeeCertification.setExpiryDate(LocalDate.now().plusDays(65));
        testEmployeeCertification.setDocumentUrl("https://example.com/cert.pdf");

        // Setup certification create DTO
        certificationCreateDTO = CertificationCreateDTO.builder()
                .name("Safety Training")
                .description("Basic safety training certification")
                .validityPeriodDays(180)
                .required(true)
                .build();

        // Setup employee certification create DTO
        employeeCertificationCreateDTO = EmployeeCertificationCreateDTO.builder()
                .employeeId(1L)
                .certificationId(1L)
                .issueDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusDays(365))
                .documentUrl("https://example.com/cert.pdf")
                .build();
    }

    // ==================== CREATE CERTIFICATION TESTS ====================

    @Test
    @DisplayName("Create Certification - Valid Input - Success")
    void testCreateCertification_ValidInput_Success() {
        // Arrange
        when(certificationRepository.existsByName(anyString())).thenReturn(false);
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDTO result = certificationService.createCertification(certificationCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Forklift Operator", result.getName());
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    @DisplayName("Create Certification - Duplicate Name - Throws ValidationException")
    void testCreateCertification_DuplicateName_ThrowsValidationException() {
        // Arrange
        when(certificationRepository.existsByName(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            certificationService.createCertification(certificationCreateDTO);
        });
        verify(certificationRepository, never()).save(any(Certification.class));
    }

    @Test
    @DisplayName("Create Certification - Null Name - Throws ValidationException")
    void testCreateCertification_NullName_ThrowsValidationException() {
        // Arrange
        certificationCreateDTO.setName(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            certificationService.createCertification(certificationCreateDTO);
        });
    }

    @Test
    @DisplayName("Create Certification - Empty Name - Throws ValidationException")
    void testCreateCertification_EmptyName_ThrowsValidationException() {
        // Arrange
        certificationCreateDTO.setName("");

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            certificationService.createCertification(certificationCreateDTO);
        });
    }

    @Test
    @DisplayName("Create Certification - Negative Validity Period - Throws ValidationException")
    void testCreateCertification_NegativeValidityPeriod_ThrowsValidationException() {
        // Arrange
        certificationCreateDTO.setValidityPeriodDays(-1);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            certificationService.createCertification(certificationCreateDTO);
        });
    }

    @Test
    @DisplayName("Create Certification - Zero Validity Period - Throws ValidationException")
    void testCreateCertification_ZeroValidityPeriod_ThrowsValidationException() {
        // Arrange
        certificationCreateDTO.setValidityPeriodDays(0);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            certificationService.createCertification(certificationCreateDTO);
        });
    }

    // ==================== CREATE EMPLOYEE CERTIFICATION TESTS ====================

    @Test
    @DisplayName("Create Employee Certification - Valid Input - Success")
    void testCreateEmployeeCertification_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.of(testCertification));
        when(employeeCertificationRepository.existsByEmployeeAndCertification(anyLong(), anyLong())).thenReturn(false);
        when(employeeCertificationRepository.save(any(EmployeeCertification.class))).thenReturn(testEmployeeCertification);

        // Act
        EmployeeCertificationDTO result = certificationService.createEmployeeCertification(employeeCertificationCreateDTO);

        // Assert
        assertNotNull(result);
        verify(employeeCertificationRepository, times(1)).save(any(EmployeeCertification.class));
    }

    @Test
    @DisplayName("Create Employee Certification - Invalid Employee ID - Throws EntityNotFoundException")
    void testCreateEmployeeCertification_InvalidEmployeeId_ThrowsEntityNotFoundException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            certificationService.createEmployeeCertification(employeeCertificationCreateDTO);
        });
        verify(employeeCertificationRepository, never()).save(any(EmployeeCertification.class));
    }

    @Test
    @DisplayName("Create Employee Certification - Invalid Certification ID - Throws EntityNotFoundException")
    void testCreateEmployeeCertification_InvalidCertificationId_ThrowsEntityNotFoundException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            certificationService.createEmployeeCertification(employeeCertificationCreateDTO);
        });
    }

    @Test
    @DisplayName("Create Employee Certification - Duplicate Certification - Throws ValidationException")
    void testCreateEmployeeCertification_DuplicateCertification_ThrowsValidationException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.of(testCertification));
        when(employeeCertificationRepository.existsByEmployeeAndCertification(anyLong(), anyLong())).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            certificationService.createEmployeeCertification(employeeCertificationCreateDTO);
        });
    }

    @Test
    @DisplayName("Create Employee Certification - Null Issue Date - Throws ValidationException")
    void testCreateEmployeeCertification_NullIssueDate_ThrowsValidationException() {
        // Arrange
        employeeCertificationCreateDTO.setIssueDate(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            certificationService.createEmployeeCertification(employeeCertificationCreateDTO);
        });
    }

    @Test
    @DisplayName("Create Employee Certification - Null Expiry Date - Throws ValidationException")
    void testCreateEmployeeCertification_NullExpiryDate_ThrowsValidationException() {
        // Arrange
        employeeCertificationCreateDTO.setExpiryDate(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            certificationService.createEmployeeCertification(employeeCertificationCreateDTO);
        });
    }

    @Test
    @DisplayName("Create Employee Certification - Expiry Before Issue - Throws ValidationException")
    void testCreateEmployeeCertification_ExpiryBeforeIssue_ThrowsValidationException() {
        // Arrange
        employeeCertificationCreateDTO.setIssueDate(LocalDate.now());
        employeeCertificationCreateDTO.setExpiryDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            certificationService.createEmployeeCertification(employeeCertificationCreateDTO);
        });
    }

    @Test
    @DisplayName("Create Employee Certification - Future Issue Date - Throws ValidationException")
    void testCreateEmployeeCertification_FutureIssueDate_ThrowsValidationException() {
        // Arrange
        employeeCertificationCreateDTO.setIssueDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            certificationService.createEmployeeCertification(employeeCertificationCreateDTO);
        });
    }

    // ==================== GET CERTIFICATION TESTS ====================

    @Test
    @DisplayName("Get Certification By ID - Valid ID - Success")
    void testGetCertificationById_ValidId_Success() {
        // Arrange
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.of(testCertification));

        // Act
        CertificationDTO result = certificationService.getCertificationById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Forklift Operator", result.getName());
    }

    @Test
    @DisplayName("Get Certification By ID - Invalid ID - Throws EntityNotFoundException")
    void testGetCertificationById_InvalidId_ThrowsEntityNotFoundException() {
        // Arrange
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            certificationService.getCertificationById(999L);
        });
    }

    @Test
    @DisplayName("Get All Certifications - Returns All")
    void testGetAllCertifications_ReturnsAll() {
        // Arrange
        when(certificationRepository.findAll()).thenReturn(Arrays.asList(testCertification));

        // Act
        List<CertificationDTO> results = certificationService.getAllCertifications();

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Get Employee Certifications - Valid Employee - Success")
    void testGetEmployeeCertifications_ValidEmployee_Success() {
        // Arrange
        when(employeeCertificationRepository.findByEmployee(anyLong()))
                .thenReturn(Arrays.asList(testEmployeeCertification));

        // Act
        List<EmployeeCertificationDTO> results = certificationService.getEmployeeCertifications(1L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    // ==================== EXPIRY ALERT TESTS ====================

    @Test
    @DisplayName("Get Expiring Certifications - 30 Days - Returns Expiring")
    void testGetExpiringCertifications_30Days_ReturnsExpiring() {
        // Arrange
        when(employeeCertificationRepository.findExpiringCertifications(any(LocalDate.class)))
                .thenReturn(Arrays.asList(testEmployeeCertification));

        // Act
        List<EmployeeCertificationDTO> results = certificationService.getExpiringCertifications(30);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Get Expiring Certifications - 7 Days - Returns Expiring")
    void testGetExpiringCertifications_7Days_ReturnsExpiring() {
        // Arrange
        testEmployeeCertification.setExpiryDate(LocalDate.now().plusDays(5));
        when(employeeCertificationRepository.findExpiringCertifications(any(LocalDate.class)))
                .thenReturn(Arrays.asList(testEmployeeCertification));

        // Act
        List<EmployeeCertificationDTO> results = certificationService.getExpiringCertifications(7);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Get Expired Certifications - Returns Expired")
    void testGetExpiredCertifications_ReturnsExpired() {
        // Arrange
        testEmployeeCertification.setExpiryDate(LocalDate.now().minusDays(1));
        when(employeeCertificationRepository.findExpiredCertifications(any(LocalDate.class)))
                .thenReturn(Arrays.asList(testEmployeeCertification));

        // Act
        List<EmployeeCertificationDTO> results = certificationService.getExpiredCertifications();

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Get Expiring Certifications - No Expiring - Returns Empty")
    void testGetExpiringCertifications_NoExpiring_ReturnsEmpty() {
        // Arrange
        when(employeeCertificationRepository.findExpiringCertifications(any(LocalDate.class)))
                .thenReturn(Arrays.asList());

        // Act
        List<EmployeeCertificationDTO> results = certificationService.getExpiringCertifications(30);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    // ==================== ASSIGNMENT BLOCKING TESTS ====================

    @Test
    @DisplayName("Check Employee Has Valid Certification - Valid Certification - Returns True")
    void testCheckEmployeeHasValidCertification_ValidCertification_ReturnsTrue() {
        // Arrange
        when(employeeCertificationRepository.findByEmployeeAndCertification(anyLong(), anyLong()))
                .thenReturn(Optional.of(testEmployeeCertification));

        // Act
        boolean result = certificationService.checkEmployeeHasValidCertification(1L, 1L);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Check Employee Has Valid Certification - Expired Certification - Returns False")
    void testCheckEmployeeHasValidCertification_ExpiredCertification_ReturnsFalse() {
        // Arrange
        testEmployeeCertification.setExpiryDate(LocalDate.now().minusDays(1));
        when(employeeCertificationRepository.findByEmployeeAndCertification(anyLong(), anyLong()))
                .thenReturn(Optional.of(testEmployeeCertification));

        // Act
        boolean result = certificationService.checkEmployeeHasValidCertification(1L, 1L);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Check Employee Has Valid Certification - No Certification - Returns False")
    void testCheckEmployeeHasValidCertification_NoCertification_ReturnsFalse() {
        // Arrange
        when(employeeCertificationRepository.findByEmployeeAndCertification(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        // Act
        boolean result = certificationService.checkEmployeeHasValidCertification(1L, 1L);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Check Employee Has All Required Certifications - All Valid - Returns True")
    void testCheckEmployeeHasAllRequiredCertifications_AllValid_ReturnsTrue() {
        // Arrange
        when(certificationRepository.findRequiredCertifications()).thenReturn(Arrays.asList(testCertification));
        when(employeeCertificationRepository.findByEmployeeAndCertification(anyLong(), anyLong()))
                .thenReturn(Optional.of(testEmployeeCertification));

        // Act
        boolean result = certificationService.checkEmployeeHasAllRequiredCertifications(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Check Employee Has All Required Certifications - Missing Certification - Returns False")
    void testCheckEmployeeHasAllRequiredCertifications_MissingCertification_ReturnsFalse() {
        // Arrange
        when(certificationRepository.findRequiredCertifications()).thenReturn(Arrays.asList(testCertification));
        when(employeeCertificationRepository.findByEmployeeAndCertification(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        // Act
        boolean result = certificationService.checkEmployeeHasAllRequiredCertifications(1L);

        // Assert
        assertFalse(result);
    }

    // ==================== UPDATE CERTIFICATION TESTS ====================

    @Test
    @DisplayName("Update Certification - Valid Input - Success")
    void testUpdateCertification_ValidInput_Success() {
        // Arrange
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.of(testCertification));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDTO result = certificationService.updateCertification(1L, certificationCreateDTO);

        // Assert
        assertNotNull(result);
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    @DisplayName("Update Certification - Invalid ID - Throws EntityNotFoundException")
    void testUpdateCertification_InvalidId_ThrowsEntityNotFoundException() {
        // Arrange
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            certificationService.updateCertification(999L, certificationCreateDTO);
        });
    }

    @Test
    @DisplayName("Renew Employee Certification - Valid Input - Success")
    void testRenewEmployeeCertification_ValidInput_Success() {
        // Arrange
        when(employeeCertificationRepository.findById(anyLong())).thenReturn(Optional.of(testEmployeeCertification));
        when(employeeCertificationRepository.save(any(EmployeeCertification.class))).thenReturn(testEmployeeCertification);

        // Act
        EmployeeCertificationDTO result = certificationService.renewEmployeeCertification(1L, LocalDate.now().plusYears(1));

        // Assert
        assertNotNull(result);
        verify(employeeCertificationRepository, times(1)).save(any(EmployeeCertification.class));
    }

    // ==================== DELETE CERTIFICATION TESTS ====================

    @Test
    @DisplayName("Delete Certification - Valid ID - Success")
    void testDeleteCertification_ValidId_Success() {
        // Arrange
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.of(testCertification));
        when(employeeCertificationRepository.existsByCertification(anyLong())).thenReturn(false);
        doNothing().when(certificationRepository).delete(any(Certification.class));

        // Act
        certificationService.deleteCertification(1L);

        // Assert
        verify(certificationRepository, times(1)).delete(any(Certification.class));
    }

    @Test
    @DisplayName("Delete Certification - Has Employee Certifications - Throws ValidationException")
    void testDeleteCertification_HasEmployeeCertifications_ThrowsValidationException() {
        // Arrange
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.of(testCertification));
        when(employeeCertificationRepository.existsByCertification(anyLong())).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            certificationService.deleteCertification(1L);
        });
    }

    // ==================== BOUNDARY CONDITION TESTS ====================

    @Test
    @DisplayName("Create Certification - Maximum Validity Period - Success")
    void testCreateCertification_MaximumValidityPeriod_Success() {
        // Arrange
        certificationCreateDTO.setValidityPeriodDays(3650); // 10 years
        when(certificationRepository.existsByName(anyString())).thenReturn(false);
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDTO result = certificationService.createCertification(certificationCreateDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Certification - Minimum Validity Period - Success")
    void testCreateCertification_MinimumValidityPeriod_Success() {
        // Arrange
        certificationCreateDTO.setValidityPeriodDays(1);
        when(certificationRepository.existsByName(anyString())).thenReturn(false);
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDTO result = certificationService.createCertification(certificationCreateDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Get Expiring Certifications - Expiring Today - Returns Expiring")
    void testGetExpiringCertifications_ExpiringToday_ReturnsExpiring() {
        // Arrange
        testEmployeeCertification.setExpiryDate(LocalDate.now());
        when(employeeCertificationRepository.findExpiringCertifications(any(LocalDate.class)))
                .thenReturn(Arrays.asList(testEmployeeCertification));

        // Act
        List<EmployeeCertificationDTO> results = certificationService.getExpiringCertifications(0);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Create Certification - Name With Special Characters - Success")
    void testCreateCertification_NameWithSpecialCharacters_Success() {
        // Arrange
        certificationCreateDTO.setName("Forklift-A/B (Class-1)");
        when(certificationRepository.existsByName(anyString())).thenReturn(false);
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        CertificationDTO result = certificationService.createCertification(certificationCreateDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Employee Certification - Same Day Issue and Expiry - Success")
    void testCreateEmployeeCertification_SameDayIssueAndExpiry_Success() {
        // Arrange
        employeeCertificationCreateDTO.setIssueDate(LocalDate.now());
        employeeCertificationCreateDTO.setExpiryDate(LocalDate.now());
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.of(testCertification));
        when(employeeCertificationRepository.existsByEmployeeAndCertification(anyLong(), anyLong())).thenReturn(false);
        when(employeeCertificationRepository.save(any(EmployeeCertification.class))).thenReturn(testEmployeeCertification);

        // Act
        EmployeeCertificationDTO result = certificationService.createEmployeeCertification(employeeCertificationCreateDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Employee Certification - Very Long Document URL - Success")
    void testCreateEmployeeCertification_VeryLongDocumentUrl_Success() {
        // Arrange
        String longUrl = "https://example.com/" + "a".repeat(500) + ".pdf";
        employeeCertificationCreateDTO.setDocumentUrl(longUrl);
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.of(testCertification));
        when(employeeCertificationRepository.existsByEmployeeAndCertification(anyLong(), anyLong())).thenReturn(false);
        when(employeeCertificationRepository.save(any(EmployeeCertification.class))).thenReturn(testEmployeeCertification);

        // Act
        EmployeeCertificationDTO result = certificationService.createEmployeeCertification(employeeCertificationCreateDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Get Expiring Certifications - Large Days Value - Success")
    void testGetExpiringCertifications_LargeDaysValue_Success() {
        // Arrange
        when(employeeCertificationRepository.findExpiringCertifications(any(LocalDate.class)))
                .thenReturn(Arrays.asList(testEmployeeCertification));

        // Act
        List<EmployeeCertificationDTO> results = certificationService.getExpiringCertifications(365);

        // Assert
        assertNotNull(results);
    }
}