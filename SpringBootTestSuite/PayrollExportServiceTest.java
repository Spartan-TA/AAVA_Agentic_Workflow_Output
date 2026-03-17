package com.warehouse.ems.service;

import com.warehouse.ems.dto.PayrollExportRequestDto;
import com.warehouse.ems.entity.PayrollExport;
import com.warehouse.ems.exception.EntityNotFoundException;
import com.warehouse.ems.repository.PayrollExportRepository;
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
 * Unit tests for PayrollExportService.
 * Covers normal operation, null/invalid input, and exception scenarios.
 */
@ExtendWith(MockitoExtension.class)
class PayrollExportServiceTest {

    @Mock
    private PayrollExportRepository payrollExportRepository;
    @InjectMocks
    private PayrollExportService payrollExportService;

    private PayrollExport payrollExport;
    private PayrollExportRequestDto payrollExportRequestDto;

    @BeforeEach
    void setUp() {
        payrollExport = new PayrollExport();
        payrollExport.setId(1L);
        payrollExport.setExportDate(LocalDate.now());
        payrollExport.setStatus("COMPLETED");
        payrollExport.setFileName("payroll_june.csv");

        payrollExportRequestDto = new PayrollExportRequestDto();
        payrollExportRequestDto.setStartDate(LocalDate.now().minusDays(14));
        payrollExportRequestDto.setEndDate(LocalDate.now());
    }

    /**
     * Test createPayrollExport with valid input returns PayrollExport.
     */
    @Test
    void testCreatePayrollExport_ValidInput_ReturnsPayrollExport() {
        when(payrollExportRepository.save(any(PayrollExport.class))).thenReturn(payrollExport);
        PayrollExport result = payrollExportService.createPayrollExport(payrollExportRequestDto);
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
    }

    /**
     * Test createPayrollExport with null DTO throws exception.
     */
    @Test
    void testCreatePayrollExport_NullDto_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                payrollExportService.createPayrollExport(null));
    }

    /**
     * Test getPayrollExportById with valid ID returns PayrollExport.
     */
    @Test
    void testGetPayrollExportById_ValidId_ReturnsPayrollExport() {
        when(payrollExportRepository.findById(1L)).thenReturn(Optional.of(payrollExport));
        PayrollExport result = payrollExportService.getPayrollExportById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    /**
     * Test getPayrollExportById with non-existent ID throws EntityNotFoundException.
     */
    @Test
    void testGetPayrollExportById_NonExistentId_ThrowsEntityNotFoundException() {
        when(payrollExportRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                payrollExportService.getPayrollExportById(99L));
    }

    /**
     * Test getAllPayrollExports returns list.
     */
    @Test
    void testGetAllPayrollExports_ReturnsList() {
        when(payrollExportRepository.findAll()).thenReturn(List.of(payrollExport));
        List<PayrollExport> result = payrollExportService.getAllPayrollExports();
        assertEquals(1, result.size());
    }

    /**
     * Test updatePayrollExport with valid input returns PayrollExport.
     */
    @Test
    void testUpdatePayrollExport_ValidInput_ReturnsPayrollExport() {
        when(payrollExportRepository.findById(1L)).thenReturn(Optional.of(payrollExport));
        when(payrollExportRepository.save(any(PayrollExport.class))).thenReturn(payrollExport);
        PayrollExport result = payrollExportService.updatePayrollExport(1L, payrollExportRequestDto);
        assertNotNull(result);
    }
}
