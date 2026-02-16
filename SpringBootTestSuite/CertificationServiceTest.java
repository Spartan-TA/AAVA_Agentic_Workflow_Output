package com.warehouse.employeemgmt.service;

import com.warehouse.employeemgmt.domain.Certification;
import com.warehouse.employeemgmt.domain.Employee;
import com.warehouse.employeemgmt.dto.CertificationDTO;
import com.warehouse.employeemgmt.exception.ResourceNotFoundException;
import com.warehouse.employeemgmt.repository.CertificationRepository;
import com.warehouse.employeemgmt.repository.EmployeeRepository;
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
 * Comprehensive JUnit test suite for CertificationService
 * Tests certification tracking, expiry alerts, renewal workflow, and assignment blocking
 * 
 * Test Coverage:
 * - Certification CRUD operations
 * - Expiry date validation and alerts (30/7 days)
 * - Renewal workflow
 * - Assignment blocking for expired certifications
 * - Document upload validation
 * - Edge cases (null inputs, past dates, boundary conditions)
 * - Certification status management
 * - Employee certification history
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Certification Service Test Suite")
public class CertificationServiceTest {

    @Mock
    private CertificationRepository certificationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private CertificationService certificationService;

    private Employee testEmployee;
    private Certification testCertification;
    private CertificationDTO testCertificationDTO;

    @BeforeEach
    public void setUp() {
        // Arrange - Setup test data
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("EMP001");

        testCertification = new Certification();
        testCertification.setId(1L);
        testCertification.setEmployee(testEmployee);
        testCertification.setType("Forklift Operator");
        testCertification.setExpiryDate(LocalDate.now().plusMonths(6));
        testCertification.setDocumentUrl("https://example.com/cert.pdf");
        testCertification.setIssueDate(LocalDate.now().minusYears(1));

        testCertificationDTO = new CertificationDTO();
        testCertificationDTO.setEmployeeId(1L);
        testCertificationDTO.setType("Forklift Operator");
        testCertificationDTO.setExpiryDate(LocalDate.now().plusMonths(6));
        testCertificationDTO.setDocumentUrl("https://example.com/cert.pdf");
        testCertificationDTO.setIssueDate(LocalDate.now().minusYears(1));
    }

    // ==================== CREATE CERTIFICATION TESTS ====================

    @Test
    @DisplayName("Test create certification with valid input")
    public void testCreateCertification_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        Certification result = certificationService.create(testCertificationDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Forklift Operator", result.getType());
        assertEquals(testEmployee, result.getEmployee());
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    @DisplayName("Test create certification with null DTO")
    public void testCreateCertification_NullDTO_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.create(null);
        });
        verify(certificationRepository, never()).save(any(Certification.class));
    }

    @Test
    @DisplayName("Test create certification with null employee ID")
    public void testCreateCertification_NullEmployeeId_ThrowsException() {
        // Arrange
        testCertificationDTO.setEmployeeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.create(testCertificationDTO);
        });
    }

    @Test
    @DisplayName("Test create certification with non-existent employee")
    public void testCreateCertification_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            certificationService.create(testCertificationDTO);
        });
        verify(certificationRepository, never()).save(any(Certification.class));
    }

    @Test
    @DisplayName("Test create certification with null type")
    public void testCreateCertification_NullType_ThrowsException() {
        // Arrange
        testCertificationDTO.setType(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.create(testCertificationDTO);
        });
    }

    @Test
    @DisplayName("Test create certification with empty type")
    public void testCreateCertification_EmptyType_ThrowsException() {
        // Arrange
        testCertificationDTO.setType("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.create(testCertificationDTO);
        });
    }

    @Test
    @DisplayName("Test create certification with null expiry date")
    public void testCreateCertification_NullExpiryDate_ThrowsException() {
        // Arrange
        testCertificationDTO.setExpiryDate(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.create(testCertificationDTO);
        });
    }

    @Test
    @DisplayName("Test create certification with past expiry date")
    public void testCreateCertification_PastExpiryDate_ThrowsException() {
        // Arrange
        testCertificationDTO.setExpiryDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.create(testCertificationDTO);
        });
    }

    @Test
    @DisplayName("Test create certification with today's expiry date")
    public void testCreateCertification_TodayExpiryDate_Success() {
        // Arrange
        testCertificationDTO.setExpiryDate(LocalDate.now());
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        Certification result = certificationService.create(testCertificationDTO);

        // Assert
        assertNotNull(result);
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    @DisplayName("Test create certification with null document URL")
    public void testCreateCertification_NullDocumentUrl_Success() {
        // Arrange
        testCertificationDTO.setDocumentUrl(null);
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        Certification result = certificationService.create(testCertificationDTO);

        // Assert
        assertNotNull(result);
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    // ==================== UPDATE CERTIFICATION TESTS ====================

    @Test
    @DisplayName("Test update certification with valid input")
    public void testUpdateCertification_ValidInput_Success() {
        // Arrange
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.of(testCertification));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);
        testCertificationDTO.setExpiryDate(LocalDate.now().plusYears(1));

        // Act
        Certification result = certificationService.update(1L, testCertificationDTO);

        // Assert
        assertNotNull(result);
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    @DisplayName("Test update certification with non-existent ID")
    public void testUpdateCertification_NonExistentId_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            certificationService.update(999L, testCertificationDTO);
        });
        verify(certificationRepository, never()).save(any(Certification.class));
    }

    // ==================== RENEW CERTIFICATION TESTS ====================

    @Test
    @DisplayName("Test renew certification with valid input")
    public void testRenewCertification_ValidInput_Success() {
        // Arrange
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.of(testCertification));
        testCertification.setExpiryDate(LocalDate.now().plusYears(1));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        Certification result = certificationService.renew(1L, LocalDate.now().plusYears(1));

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.now().plusYears(1), result.getExpiryDate());
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    @DisplayName("Test renew certification with non-existent ID")
    public void testRenewCertification_NonExistentId_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            certificationService.renew(999L, LocalDate.now().plusYears(1));
        });
    }

    @Test
    @DisplayName("Test renew certification with past date")
    public void testRenewCertification_PastDate_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.of(testCertification));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.renew(1L, LocalDate.now().minusDays(1));
        });
    }

    // ==================== EXPIRY ALERT TESTS ====================

    @Test
    @DisplayName("Test get certifications expiring in 30 days")
    public void testGetCertificationsExpiringIn30Days_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(30);
        when(certificationRepository.findByExpiryDateBetween(startDate, endDate))
                .thenReturn(Arrays.asList(testCertification));

        // Act
        List<Certification> result = certificationService.getCertificationsExpiringIn30Days();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(certificationRepository, times(1)).findByExpiryDateBetween(startDate, endDate);
    }

    @Test
    @DisplayName("Test get certifications expiring in 7 days")
    public void testGetCertificationsExpiringIn7Days_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        when(certificationRepository.findByExpiryDateBetween(startDate, endDate))
                .thenReturn(Arrays.asList(testCertification));

        // Act
        List<Certification> result = certificationService.getCertificationsExpiringIn7Days();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(certificationRepository, times(1)).findByExpiryDateBetween(startDate, endDate);
    }

    @Test
    @DisplayName("Test get expired certifications")
    public void testGetExpiredCertifications_Success() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().minusDays(1));
        when(certificationRepository.findByExpiryDateBefore(any(LocalDate.class)))
                .thenReturn(Arrays.asList(testCertification));

        // Act
        List<Certification> result = certificationService.getExpiredCertifications();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getExpiryDate().isBefore(LocalDate.now()));
    }

    @Test
    @DisplayName("Test alert expiry - no certifications expiring")
    public void testAlertExpiry_NoCertificationsExpiring_Success() {
        // Arrange
        when(certificationRepository.findByExpiryDateBetween(any(), any()))
                .thenReturn(Arrays.asList());

        // Act
        List<Certification> result = certificationService.getCertificationsExpiringIn30Days();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ==================== ASSIGNMENT BLOCKING TESTS ====================

    @Test
    @DisplayName("Test check certification validity - valid certification")
    public void testCheckCertificationValidity_ValidCertification_ReturnsTrue() {
        // Arrange
        when(certificationRepository.findByEmployeeAndType(any(Employee.class), anyString()))
                .thenReturn(Optional.of(testCertification));

        // Act
        boolean result = certificationService.isCertificationValid(testEmployee, "Forklift Operator");

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Test check certification validity - expired certification")
    public void testCheckCertificationValidity_ExpiredCertification_ReturnsFalse() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().minusDays(1));
        when(certificationRepository.findByEmployeeAndType(any(Employee.class), anyString()))
                .thenReturn(Optional.of(testCertification));

        // Act
        boolean result = certificationService.isCertificationValid(testEmployee, "Forklift Operator");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Test check certification validity - no certification found")
    public void testCheckCertificationValidity_NoCertification_ReturnsFalse() {
        // Arrange
        when(certificationRepository.findByEmployeeAndType(any(Employee.class), anyString()))
                .thenReturn(Optional.empty());

        // Act
        boolean result = certificationService.isCertificationValid(testEmployee, "Forklift Operator");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Test block assignment - expired certification")
    public void testBlockAssignment_ExpiredCertification_ThrowsException() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now().minusDays(1));
        when(certificationRepository.findByEmployeeAndType(any(Employee.class), anyString()))
                .thenReturn(Optional.of(testCertification));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            certificationService.validateCertificationForAssignment(testEmployee, "Forklift Operator");
        });
    }

    @Test
    @DisplayName("Test block assignment - no certification")
    public void testBlockAssignment_NoCertification_ThrowsException() {
        // Arrange
        when(certificationRepository.findByEmployeeAndType(any(Employee.class), anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            certificationService.validateCertificationForAssignment(testEmployee, "Forklift Operator");
        });
    }

    // ==================== GET CERTIFICATION TESTS ====================

    @Test
    @DisplayName("Test get certification by ID - existing certification")
    public void testGetCertificationById_ExistingCertification_Success() {
        // Arrange
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.of(testCertification));

        // Act
        Certification result = certificationService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Forklift Operator", result.getType());
    }

    @Test
    @DisplayName("Test get certification by ID - non-existent certification")
    public void testGetCertificationById_NonExistentCertification_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            certificationService.getById(999L);
        });
    }

    @Test
    @DisplayName("Test get certifications by employee")
    public void testGetCertificationsByEmployee_Success() {
        // Arrange
        when(certificationRepository.findByEmployee(any(Employee.class)))
                .thenReturn(Arrays.asList(testCertification));

        // Act
        List<Certification> result = certificationService.getByEmployee(testEmployee);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEmployee, result.get(0).getEmployee());
    }

    @Test
    @DisplayName("Test get certifications by employee - no certifications")
    public void testGetCertificationsByEmployee_NoCertifications_ReturnsEmpty() {
        // Arrange
        when(certificationRepository.findByEmployee(any(Employee.class)))
                .thenReturn(Arrays.asList());

        // Act
        List<Certification> result = certificationService.getByEmployee(testEmployee);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ==================== DELETE CERTIFICATION TESTS ====================

    @Test
    @DisplayName("Test delete certification - existing certification")
    public void testDeleteCertification_ExistingCertification_Success() {
        // Arrange
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.of(testCertification));
        doNothing().when(certificationRepository).deleteById(anyLong());

        // Act
        certificationService.delete(1L);

        // Assert
        verify(certificationRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Test delete certification - non-existent certification")
    public void testDeleteCertification_NonExistentCertification_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            certificationService.delete(999L);
        });
        verify(certificationRepository, never()).deleteById(anyLong());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Test create certification with very long type name (boundary)")
    public void testCreateCertification_VeryLongTypeName_Success() {
        // Arrange
        testCertificationDTO.setType("A".repeat(255));
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        Certification result = certificationService.create(testCertificationDTO);

        // Assert
        assertNotNull(result);
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    @DisplayName("Test create certification with special characters in type")
    public void testCreateCertification_SpecialCharactersInType_Success() {
        // Arrange
        testCertificationDTO.setType("Forklift Operator (Class A/B)");
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        Certification result = certificationService.create(testCertificationDTO);

        // Assert
        assertNotNull(result);
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    @DisplayName("Test certification expiring today")
    public void testCertificationExpiringToday_Success() {
        // Arrange
        testCertification.setExpiryDate(LocalDate.now());
        when(certificationRepository.findByExpiryDateBetween(any(), any()))
                .thenReturn(Arrays.asList(testCertification));

        // Act
        List<Certification> result = certificationService.getCertificationsExpiringIn7Days();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test certification with very long document URL (boundary)")
    public void testCreateCertification_VeryLongDocumentUrl_Success() {
        // Arrange
        testCertificationDTO.setDocumentUrl("https://example.com/" + "a".repeat(500) + ".pdf");
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(testCertification);

        // Act
        Certification result = certificationService.create(testCertificationDTO);

        // Assert
        assertNotNull(result);
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    @Test
    @DisplayName("Test multiple certifications for same employee")
    public void testMultipleCertificationsForSameEmployee_Success() {
        // Arrange
        Certification cert2 = new Certification();
        cert2.setEmployee(testEmployee);
        cert2.setType("Safety Training");
        cert2.setExpiryDate(LocalDate.now().plusMonths(3));

        when(certificationRepository.findByEmployee(any(Employee.class)))
                .thenReturn(Arrays.asList(testCertification, cert2));

        // Act
        List<Certification> result = certificationService.getByEmployee(testEmployee);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Test certification with issue date after expiry date")
    public void testCreateCertification_IssueDateAfterExpiryDate_ThrowsException() {
        // Arrange
        testCertificationDTO.setIssueDate(LocalDate.now());
        testCertificationDTO.setExpiryDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            certificationService.create(testCertificationDTO);
        });
    }
}