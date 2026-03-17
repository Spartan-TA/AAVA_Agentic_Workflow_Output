package com.warehouse.ems.service;

import com.warehouse.ems.dto.CertificationRequestDto;
import com.warehouse.ems.entity.Certification;
import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.exception.EntityNotFoundException;
import com.warehouse.ems.repository.CertificationRepository;
import com.warehouse.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CertificationService.
 * Covers normal operation, null/invalid input, duplicate entries, and exception scenarios.
 */
@ExtendWith(MockitoExtension.class)
class CertificationServiceTest {

    @Mock
    private CertificationRepository certificationRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private CertificationService certificationService;

    private Employee employee;
    private Certification certification;
    private CertificationRequestDto certificationRequestDto;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("BADGE123");

        certification = new Certification();
        certification.setId(1L);
        certification.setEmployee(employee);
        certification.setName("Forklift Operator");
        certification.setIssueDate(LocalDate.now().minusYears(1));
        certification.setExpiryDate(LocalDate.now().plusYears(1));

        certificationRequestDto = new CertificationRequestDto();
        certificationRequestDto.setEmployeeId(1L);
        certificationRequestDto.setName("Forklift Operator");
        certificationRequestDto.setIssueDate(LocalDate.now().minusYears(1));
        certificationRequestDto.setExpiryDate(LocalDate.now().plusYears(1));
    }

    /**
     * Test createCertification with valid input returns Certification.
     */
    @Test
    void testCreateCertification_ValidInput_ReturnsCertification() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(certificationRepository.save(any(Certification.class))).thenReturn(certification);
        Certification result = certificationService.createCertification(certificationRequestDto);
        assertNotNull(result);
        assertEquals("Forklift Operator", result.getName());
    }

    /**
     * Test createCertification with null DTO throws exception.
     */
    @Test
    void testCreateCertification_NullDto_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                certificationService.createCertification(null));
    }

    /**
     * Test getCertificationById with valid ID returns Certification.
     */
    @Test
    void testGetCertificationById_ValidId_ReturnsCertification() {
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(certification));
        Certification result = certificationService.getCertificationById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    /**
     * Test getCertificationById with non-existent ID throws EntityNotFoundException.
     */
    @Test
    void testGetCertificationById_NonExistentId_ThrowsEntityNotFoundException() {
        when(certificationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                certificationService.getCertificationById(99L));
    }

    /**
     * Test getAllCertifications returns list.
     */
    @Test
    void testGetAllCertifications_ReturnsList() {
        when(certificationRepository.findAll()).thenReturn(List.of(certification));
        List<Certification> result = certificationService.getAllCertifications();
        assertEquals(1, result.size());
    }

    /**
     * Test updateCertification with valid input returns Certification.
     */
    @Test
    void testUpdateCertification_ValidInput_ReturnsCertification() {
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(certification));
        when(certificationRepository.save(any(Certification.class))).thenReturn(certification);
        Certification result = certificationService.updateCertification(1L, certificationRequestDto);
        assertNotNull(result);
    }

    /**
     * Test updateCertification with non-existent ID throws EntityNotFoundException.
     */
    @Test
    void testUpdateCertification_NonExistentId_ThrowsEntityNotFoundException() {
        when(certificationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                certificationService.updateCertification(99L, certificationRequestDto));
    }

    /**
     * Test deleteCertification with valid ID does not throw.
     */
    @Test
    void testDeleteCertification_ValidId_DoesNotThrow() {
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(certification));
        doNothing().when(certificationRepository).delete(certification);
        assertDoesNotThrow(() -> certificationService.deleteCertification(1L));
    }
}
