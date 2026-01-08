package com.example.warehouse.service;

import com.example.warehouse.entity.Certification;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.repository.CertificationRepository;
import com.example.warehouse.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for CertificationService.
 * 
 * Tests cover:
 * - Certification creation and retrieval
 * - Certification deletion
 * - Filtering by employee
 * - Normal cases, boundary conditions, and edge cases
 * - Exception handling for non-existent certifications and employees
 * - Date validation scenarios (expiration tracking)
 * 
 * @author Warehouse Test Team
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
    private Certification expiringCertification;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    public void setUp() {
        testEmployee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@warehouse.com")
                .position("Warehouse Associate")
                .hireDate(LocalDate.of(2024, 1, 15))
                .active(true)
                .build();

        validCertification = Certification.builder()
                .id(1L)
                .employee(testEmployee)
                .name("Forklift Operator")
                .dateIssued(LocalDate.now().minusYears(1))
                .validUntil(LocalDate.now().plusYears(1))
                .build();

        expiredCertification = Certification.builder()
                .id(2L)
                .employee(testEmployee)
                .name("Safety Training")
                .dateIssued(LocalDate.now().minusYears(2))
                .validUntil(LocalDate.now().minusDays(30))
                .build();

        expiringCertification = Certification.builder()
                .id(3L)
                .employee(testEmployee)
                .name("First Aid")
                .dateIssued(LocalDate.now().minusMonths(6))
                .validUntil(LocalDate.now().plusDays(15))
                .build();
    }

    // ==================== GET ALL CERTIFICATIONS TESTS ====================

    /**
     * Test getAllCertifications with multiple certifications - Normal case.
     */
    @Test
    public void testGetAllCertifications_WithMultipleCertifications_Success() {
        // Arrange
        List<Certification> certifications = Arrays.asList(validCertification, expiredCertification, expiringCertification);
        when(certificationRepository.findAll()).thenReturn(certifications);

        // Act
        List<Certification> result = certificationService.getAllCertifications();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Forklift Operator", result.get(0).getName());
        assertEquals("Safety Training", result.get(1).getName());
        assertEquals("First Aid", result.get(2).getName());
        verify(certificationRepository, times(1)).findAll();
    }

    /**
     * Test getAllCertifications with empty list - Boundary condition.
     */
    @Test
    public void testGetAllCertifications_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(certificationRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Certification> result = certificationService.getAllCertifications();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(certificationRepository, times(1)).findAll();
    }

    /**
     * Test getAllCertifications with single certification - Edge case.
     */
    @Test
    public void testGetAllCertifications_SingleCertification_Success() {
        // Arrange
        when(certificationRepository.findAll()).thenReturn(Collections.singletonList(validCertification));

        // Act
        List<Certification> result = certificationService.getAllCertifications();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Forklift Operator", result.get(0).getName());
        verify(certificationRepository, times(1)).findAll();
    }

    // ==================== GET CERTIFICATIONS BY EMPLOYEE TESTS ====================

    /**
     * Test getCertificationsByEmployee with valid employee ID - Normal case.
     */
    @Test
    public void testGetCertificationsByEmployee_ValidEmployeeId_Success() {
        // Arrange
        List<Certification> certifications = Arrays.asList(validCertification, expiredCertification);
        when(certificationRepository.findByEmployeeId(1L)).thenReturn(certifications);

        // Act
        List<Certification> result = certificationService.getCertificationsByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getEmployee().getId());
        verify(certificationRepository, times(1)).findByEmployeeId(1L);
    }

    /**
     * Test getCertificationsByEmployee with no certifications - Boundary condition.
     */
    @Test
    public void testGetCertificationsByEmployee_NoCertifications_ReturnsEmptyList() {
        // Arrange
        when(certificationRepository.findByEmployeeId(1L)).thenReturn(Collections.emptyList());

        // Act
        List<Certification> result = certificationService.getCertificationsByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(certificationRepository, times(1)).findByEmployeeId(1L);
    }

    /**
     * Test getCertificationsByEmployee with non-existent employee - Edge case.
     */
    @Test
    public void testGetCertificationsByEmployee_NonExistentEmployee_ReturnsEmptyList() {
        // Arrange
        when(certificationRepository.findByEmployeeId(999L)).thenReturn(Collections.emptyList());

        // Act
        List<Certification> result = certificationService.getCertificationsByEmployee(999L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(certificationRepository, times(1)).findByEmployeeId(999L);
    }

    /**
     * Test getCertificationsByEmployee with null employee ID - Boundary condition.
     */
    @Test
    public void testGetCertificationsByEmployee_NullEmployeeId_ReturnsEmptyList() {
        // Arrange
        when(certificationRepository.findByEmployeeId(null)).thenReturn(Collections.emptyList());

        // Act
        List<Certification> result = certificationService.getCertificationsByEmployee(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(certificationRepository, times(1)).findByEmployeeId(null);
    }

    // ==================== GET CERTIFICATION BY ID TESTS ====================

    /**
     * Test getCertificationById with valid ID - Normal case.
     */
    @Test
    public void testGetCertificationById_ValidId_Success() {
        // Arrange
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(validCertification));

        // Act
        Certification result = certificationService.getCertificationById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Forklift Operator", result.getName());
        assertTrue(result.getValidUntil().isAfter(LocalDate.now()));
        verify(certificationRepository, times(1)).findById(1L);
    }

    /**
     * Test getCertificationById with non-existent ID - Edge case.
     */
    @Test
    public void testGetCertificationById_NonExistentId_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            certificationService.getCertificationById(999L);
        });
        assertEquals("Certification not found", exception.getMessage());
        verify(certificationRepository, times(1)).findById(999L);
    }

    /**
     * Test getCertificationById with null ID - Boundary condition.
     */
    @Test
    public void testGetCertificationById_NullId_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            certificationService.getCertificationById(null);
        });
    }

    /**
     * Test getCertificationById with negative ID - Edge case.
     */
    @Test
    public void testGetCertificationById_NegativeId_ThrowsException() {
        // Arrange
        when(certificationRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            certificationService.getCertificationById(-1L);
        });
    }

    // ==================== CREATE CERTIFICATION TESTS ====================

    /**
     * Test createCertification with valid data - Normal case.
     */
    @Test
    public void testCreateCertification_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(validCertification);

        // Act
        Certification result = certificationService.createCertification(1L, validCertification);

        // Assert
        assertNotNull(result);
        assertEquals(testEmployee, result.getEmployee());
        assertEquals("Forklift Operator", result.getName());
        assertNotNull(result.getDateIssued());
        assertNotNull(result.getValidUntil());
        verify(employeeRepository, times(1)).findById(1L);
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    /**
     * Test createCertification with non-existent employee - Edge case.
     */
    @Test
    public void testCreateCertification_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            certificationService.createCertification(999L, validCertification);
        });
        assertEquals("Employee not found", exception.getMessage());
        verify(employeeRepository, times(1)).findById(999L);
        verify(certificationRepository, never()).save(any(Certification.class));
    }

    /**
     * Test createCertification with null employee ID - Boundary condition.
     */
    @Test
    public void testCreateCertification_NullEmployeeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            certificationService.createCertification(null, validCertification);
        });
        verify(certificationRepository, never()).save(any(Certification.class));
    }

    /**
     * Test createCertification with expired certification - Edge case.
     */
    @Test
    public void testCreateCertification_ExpiredCertification_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(expiredCertification);

        // Act
        Certification result = certificationService.createCertification(1L, expiredCertification);

        // Assert
        assertNotNull(result);
        assertTrue(result.getValidUntil().isBefore(LocalDate.now()));
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    /**
     * Test createCertification with same issue and expiry date - Edge case.
     */
    @Test
    public void testCreateCertification_SameIssuedAndExpiryDate_Success() {
        // Arrange
        LocalDate sameDate = LocalDate.now();
        Certification sameDateCert = Certification.builder()
                .employee(testEmployee)
                .name("Test Certification")
                .dateIssued(sameDate)
                .validUntil(sameDate)
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(sameDateCert);

        // Act
        Certification result = certificationService.createCertification(1L, sameDateCert);

        // Assert
        assertNotNull(result);
        assertEquals(result.getDateIssued(), result.getValidUntil());
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    /**
     * Test createCertification with expiry before issue date - Edge case.
     */
    @Test
    public void testCreateCertification_ExpiryBeforeIssueDate_Success() {
        // Arrange
        Certification invalidDateCert = Certification.builder()
                .employee(testEmployee)
                .name("Invalid Date Cert")
                .dateIssued(LocalDate.now())
                .validUntil(LocalDate.now().minusDays(30))
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(invalidDateCert);

        // Act
        Certification result = certificationService.createCertification(1L, invalidDateCert);

        // Assert
        assertNotNull(result);
        assertTrue(result.getValidUntil().isBefore(result.getDateIssued()));
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    /**
     * Test createCertification with null name - Boundary condition.
     */
    @Test
    public void testCreateCertification_NullName_Success() {
        // Arrange
        Certification nullNameCert = Certification.builder()
                .employee(testEmployee)
                .name(null)
                .dateIssued(LocalDate.now())
                .validUntil(LocalDate.now().plusYears(1))
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(nullNameCert);

        // Act
        Certification result = certificationService.createCertification(1L, nullNameCert);

        // Assert
        assertNotNull(result);
        assertNull(result.getName());
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    /**
     * Test createCertification with very long validity period - Edge case.
     */
    @Test
    public void testCreateCertification_LongValidityPeriod_Success() {
        // Arrange
        Certification longValidityCert = Certification.builder()
                .employee(testEmployee)
                .name("Lifetime Certification")
                .dateIssued(LocalDate.now())
                .validUntil(LocalDate.now().plusYears(50))
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(longValidityCert);

        // Act
        Certification result = certificationService.createCertification(1L, longValidityCert);

        // Assert
        assertNotNull(result);
        long yearsBetween = java.time.temporal.ChronoUnit.YEARS.between(result.getDateIssued(), result.getValidUntil());
        assertEquals(50, yearsBetween);
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    // ==================== DELETE CERTIFICATION TESTS ====================

    /**
     * Test deleteCertification with valid ID - Normal case.
     */
    @Test
    public void testDeleteCertification_ValidId_Success() {
        // Arrange
        when(certificationRepository.existsById(1L)).thenReturn(true);
        doNothing().when(certificationRepository).deleteById(1L);

        // Act
        certificationService.deleteCertification(1L);

        // Assert
        verify(certificationRepository, times(1)).existsById(1L);
        verify(certificationRepository, times(1)).deleteById(1L);
    }

    /**
     * Test deleteCertification with non-existent ID - Edge case.
     */
    @Test
    public void testDeleteCertification_NonExistentId_ThrowsException() {
        // Arrange
        when(certificationRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            certificationService.deleteCertification(999L);
        });
        assertEquals("Certification not found", exception.getMessage());
        verify(certificationRepository, times(1)).existsById(999L);
        verify(certificationRepository, never()).deleteById(anyLong());
    }

    /**
     * Test deleteCertification with null ID - Boundary condition.
     */
    @Test
    public void testDeleteCertification_NullId_ThrowsException() {
        // Arrange
        when(certificationRepository.existsById(null)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            certificationService.deleteCertification(null);
        });
        verify(certificationRepository, never()).deleteById(anyLong());
    }

    /**
     * Test deleteCertification with negative ID - Edge case.
     */
    @Test
    public void testDeleteCertification_NegativeId_ThrowsException() {
        // Arrange
        when(certificationRepository.existsById(-1L)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            certificationService.deleteCertification(-1L);
        });
        verify(certificationRepository, times(1)).existsById(-1L);
        verify(certificationRepository, never()).deleteById(anyLong());
    }

    /**
     * Test createCertification for inactive employee - Edge case.
     */
    @Test
    public void testCreateCertification_InactiveEmployee_Success() {
        // Arrange
        testEmployee.setActive(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(validCertification);

        // Act
        Certification result = certificationService.createCertification(1L, validCertification);

        // Assert
        assertNotNull(result);
        assertFalse(result.getEmployee().isActive());
        verify(certificationRepository, times(1)).save(any(Certification.class));
    }

    /**
     * Test getCertificationsByEmployee with multiple certifications of different statuses - Edge case.
     */
    @Test
    public void testGetCertificationsByEmployee_MixedExpiryStatuses_Success() {
        // Arrange
        List<Certification> mixedCerts = Arrays.asList(validCertification, expiredCertification, expiringCertification);
        when(certificationRepository.findByEmployeeId(1L)).thenReturn(mixedCerts);

        // Act
        List<Certification> result = certificationService.getCertificationsByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.get(0).getValidUntil().isAfter(LocalDate.now())); // Valid
        assertTrue(result.get(1).getValidUntil().isBefore(LocalDate.now())); // Expired
        assertTrue(result.get(2).getValidUntil().isAfter(LocalDate.now())); // Expiring soon
        verify(certificationRepository, times(1)).findByEmployeeId(1L);
    }
}