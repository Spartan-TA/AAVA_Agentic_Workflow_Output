package com.example.service;

import com.example.model.Report;
import com.example.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetReportByIdFound() {
        Report report = new Report(1L, "Monthly", "Content");
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        Report found = reportService.getReportById(1L);
        assertNotNull(found);
        assertEquals("Monthly", found.getTitle());
    }

    @Test
    void testGetReportByIdNotFound() {
        when(reportRepository.findById(2L)).thenReturn(Optional.empty());
        Report found = reportService.getReportById(2L);
        assertNull(found);
    }

    @Test
    void testGetAllReports() {
        List<Report> reports = Arrays.asList(new Report(1L, "Monthly", "Content"), new Report(2L, "Weekly", "Content2"));
        when(reportRepository.findAll()).thenReturn(reports);
        List<Report> result = reportService.getAllReports();
        assertEquals(2, result.size());
        assertEquals("Monthly", result.get(0).getTitle());
        assertEquals("Weekly", result.get(1).getTitle());
    }

    @Test
    void testSaveReport() {
        Report report = new Report(null, "Annual", "Content");
        when(reportRepository.save(report)).thenReturn(new Report(3L, "Annual", "Content"));
        Report saved = reportService.saveReport(report);
        assertNotNull(saved);
        assertEquals("Annual", saved.getTitle());
    }

    @Test
    void testDeleteReport() {
        doNothing().when(reportRepository).deleteById(1L);
        reportService.deleteReport(1L);
        verify(reportRepository).deleteById(1L);
    }
}
