package com.warehouse.ems.service;

import com.warehouse.ems.dto.ReportingRequestDto;
import com.warehouse.ems.entity.ReportingResult;
import com.warehouse.ems.exception.EntityNotFoundException;
import com.warehouse.ems.repository.ReportingRepository;
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
 * Unit tests for ReportingService.
 * Covers normal operation, null/invalid input, and exception scenarios.
 */
@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    @Mock
    private ReportingRepository reportingRepository;
    @InjectMocks
    private ReportingService reportingService;

    private ReportingResult reportingResult;
    private ReportingRequestDto reportingRequestDto;

    @BeforeEach
    void setUp() {
        reportingResult = new ReportingResult();
        reportingResult.setId(1L);
        reportingResult.setReportType("ATTENDANCE");
        reportingResult.setGeneratedDate(LocalDate.now());
        reportingResult.setStatus("COMPLETED");

        reportingRequestDto = new ReportingRequestDto();
        reportingRequestDto.setReportType("ATTENDANCE");
        reportingRequestDto.setStartDate(LocalDate.now().minusDays(7));
        reportingRequestDto.setEndDate(LocalDate.now());
    }

    /**
     * Test createReport with valid input returns ReportingResult.
     */
    @Test
    void testCreateReport_ValidInput_ReturnsReportingResult() {
        when(reportingRepository.save(any(ReportingResult.class))).thenReturn(reportingResult);
        ReportingResult result = reportingService.createReport(reportingRequestDto);
        assertNotNull(result);
        assertEquals("ATTENDANCE", result.getReportType());
    }

    /**
     * Test createReport with null DTO throws exception.
     */
    @Test
    void testCreateReport_NullDto_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                reportingService.createReport(null));
    }

    /**
     * Test getReportById with valid ID returns ReportingResult.
     */
    @Test
    void testGetReportById_ValidId_ReturnsReportingResult() {
        when(reportingRepository.findById(1L)).thenReturn(Optional.of(reportingResult));
        ReportingResult result = reportingService.getReportById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    /**
     * Test getReportById with non-existent ID throws EntityNotFoundException.
     */
    @Test
    void testGetReportById_NonExistentId_ThrowsEntityNotFoundException() {
        when(reportingRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                reportingService.getReportById(99L));
    }

    /**
     * Test getAllReports returns list.
     */
    @Test
    void testGetAllReports_ReturnsList() {
        when(reportingRepository.findAll()).thenReturn(List.of(reportingResult));
        List<ReportingResult> result = reportingService.getAllReports();
        assertEquals(1, result.size());
    }

    /**
     * Test updateReport with valid input returns ReportingResult.
     */
    @Test
    void testUpdateReport_ValidInput_ReturnsReportingResult() {
        when(reportingRepository.findById(1L)).thenReturn(Optional.of(reportingResult));
        when(reportingRepository.save(any(ReportingResult.class))).thenReturn(reportingResult);
        ReportingResult result = reportingService.updateReport(1L, reportingRequestDto);
        assertNotNull(result);
    }
}
