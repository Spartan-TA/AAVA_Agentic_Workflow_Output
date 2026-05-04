package com.warehouse.management.reporting;

import com.warehouse.management.reporting.ReportService;
import org.junit.jupiter.api.*;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateAttendanceReport_Valid() {
        when(reportRepository.generateAttendanceReport(any(), any())).thenReturn("attendance_report.csv");
        String report = reportService.generateAttendanceReport("Logistics", new java.util.Date());
        assertEquals("attendance_report.csv", report);
    }

    @Test
    void testExportCSV_Valid() {
        when(reportRepository.exportCSV(anyString())).thenReturn("attendance_report.csv");
        String csv = reportService.exportCSV("attendance_report.csv");
        assertEquals("attendance_report.csv", csv);
    }

    @Test
    void testExportPDF_Valid() {
        when(reportRepository.exportPDF(anyString())).thenReturn("attendance_report.pdf");
        String pdf = reportService.exportPDF("attendance_report.csv");
        assertEquals("attendance_report.pdf", pdf);
    }
}