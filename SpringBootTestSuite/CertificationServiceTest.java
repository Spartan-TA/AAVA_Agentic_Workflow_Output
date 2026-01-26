package com.company.warehouse.certification.service;

import com.company.warehouse.certification.domain.*;
import com.company.warehouse.certification.dto.*;
import com.company.warehouse.certification.repository.*;
import com.company.warehouse.employee.domain.Employee;
import com.company.warehouse.employee.repository.EmployeeRepository;
import com.company.warehouse.common.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Certification Service Tests")
public class CertificationServiceTest {
    @Mock private CertificationRepository certificationRepository;
    @Mock private EmployeeCertificationRepository employeeCertificationRepository;
    @Mock private EmployeeRepository employeeRepository;
    @InjectMocks private CertificationService certificationService;
    private Certification forkliftCert;
    private Employee testEmployee;
    private EmployeeCertification empCert;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        forkliftCert = new Certification();
        forkliftCert.setId(1L);
        forkliftCert.setName("Forklift Operator");
        forkliftCert.setValidityPeriodDays(365);
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        empCert = new EmployeeCertification();
        empCert.setId(1L);
        empCert.setEmployee(testEmployee);
        empCert.setCertification(forkliftCert);
        empCert.setIssueDate(LocalDate.now().minusDays(300));
        empCert.setExpiryDate(LocalDate.now().plusDays(65));
    }

    @Test
    @DisplayName("Test assignCertification with valid data")
    public void testAssignCertification_ValidData() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationRepository.findById(1L)).thenReturn(Optional.of(forkliftCert));
        when(employeeCertificationRepository.save(any(EmployeeCertification.class))).thenReturn(empCert);
        EmployeeCertificationDTO result = certificationService.assignCertification(1L, 1L, LocalDate.now());
        assertNotNull(result);
        verify(employeeCertificationRepository, times(1)).save(any(EmployeeCertification.class));
    }

    @Test
    @DisplayName("Test assignCertification with non-existent employee")
    public void testAssignCertification_NonExistentEmployee() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> certificationService.assignCertification(999L, 1L, LocalDate.now()));
    }

    @Test
    @DisplayName("Test getExpiringCertifications within 30 days")
    public void testGetExpiringCertifications_Within30Days() {
        when(employeeCertificationRepository.findExpiringWithinDays(30)).thenReturn(Arrays.asList(empCert));
        List<EmployeeCertificationDTO> results = certificationService.getExpiringCertifications(30);
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    @DisplayName("Test validateCertification for valid cert")
    public void testValidateCertification_ValidCert() {
        when(employeeCertificationRepository.findByEmployeeAndCertification(anyLong(), anyLong())).thenReturn(Optional.of(empCert));
        boolean result = certificationService.validateCertification(1L, 1L);
        assertTrue(result);
    }

    @Test
    @DisplayName("Test validateCertification for expired cert")
    public void testValidateCertification_ExpiredCert() {
        empCert.setExpiryDate(LocalDate.now().minusDays(1));
        when(employeeCertificationRepository.findByEmployeeAndCertification(anyLong(), anyLong())).thenReturn(Optional.of(empCert));
        boolean result = certificationService.validateCertification(1L, 1L);
        assertFalse(result);
    }

    @Test
    @DisplayName("Test validateCertification for missing cert")
    public void testValidateCertification_MissingCert() {
        when(employeeCertificationRepository.findByEmployeeAndCertification(anyLong(), anyLong())).thenReturn(Optional.empty());
        boolean result = certificationService.validateCertification(1L, 1L);
        assertFalse(result);
    }

    @Test
    @DisplayName("Test renewCertification with valid data")
    public void testRenewCertification_ValidData() {
        when(employeeCertificationRepository.findById(1L)).thenReturn(Optional.of(empCert));
        when(employeeCertificationRepository.save(any(EmployeeCertification.class))).thenReturn(empCert);
        EmployeeCertificationDTO result = certificationService.renewCertification(1L, LocalDate.now());
        assertNotNull(result);
        assertTrue(result.getExpiryDate().isAfter(LocalDate.now()));
    }