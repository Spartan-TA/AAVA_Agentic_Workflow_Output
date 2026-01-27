package com.warehouse.ems.service;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class ReportingServiceTest {
    @Autowired
    ReportingService reportingService;

    @MockBean
    ReportRepository reportRepository;
    @MockBean
    ExportService exportService;

    @BeforeEach
    void setup() {
        // Setup mocks if needed
    }

    @Test
    void testGenerateAttendanceReport() {
        Report report = new Report("attendance", Arrays.asList("row1", "row2"));
        when(reportRepository.generateAttendanceReport(any(), any())).thenReturn(report);
        Report result = reportingService.generateAttendanceReport(new Date(), new Date());
        assertEquals(report, result);
    }

    @Test
    void testGenerateOvertimeReport_FilterByDepartment() {
        Report report = new Report("overtime", Arrays.asList("rowA", "rowB"));
        when(reportRepository.generateOvertimeReport(any(), eq("Packing"))).thenReturn(report);
        Report result = reportingService.generateOvertimeReport(new Date(), new Date(), "Packing");
        assertEquals(report, result);
    }

    @Test
    void testExportCSV() {
        Report report = new Report("leave", Arrays.asList("row1"));
        when(exportService.exportReport(report, "csv")).thenReturn("csvdata");
        String csv = reportingService.exportReport(report, "csv");
        assertEquals("csvdata", csv);
    }

    @Test
    void testExportPDF() {
        Report report = new Report("certification", Arrays.asList("row1"));
        when(exportService.exportReport(report, "pdf")).thenReturn("pdfdata");
        String pdf = reportingService.exportReport(report, "pdf");
        assertEquals("pdfdata", pdf);
    }

    @Test
    void testSafetyKPIsReport() {
        Report report = new Report("safety", Arrays.asList("incident1", "incident2"));
        when(reportRepository.generateSafetyKPIs(any(), any())).thenReturn(report);
        Report result = reportingService.generateSafetyKPIs(new Date(), new Date());
        assertEquals(report, result);
    }

    @Test
    void testNullReportType_Throws() {
        assertThrows(IllegalArgumentException.class, () -> reportingService.generateReport(null, new Date(), new Date()));
    }

    @Test
    void testEmptyRows() {
        Report report = new Report("attendance", Collections.emptyList());
        when(reportRepository.generateAttendanceReport(any(), any())).thenReturn(report);
        Report result = reportingService.generateAttendanceReport(new Date(), new Date());
        assertTrue(result.getRows().isEmpty());
    }

    @Test
    void testRoleBasedAccess() {
        when(reportingService.hasAccess("ADMIN", "attendance")).thenReturn(true);
        when(reportingService.hasAccess("WORKER", "payroll")).thenReturn(false);
        assertTrue(reportingService.hasAccess("ADMIN", "attendance"));
        assertFalse(reportingService.hasAccess("WORKER", "payroll"));
    }

    @Test
    void testMetricsEndpoint() {
        Map<String, Object> metrics = Map.of("attendance", 100, "overtime", 20);
        when(reportingService.getMetrics()).thenReturn(metrics);
        Map<String, Object> result = reportingService.getMetrics();
        assertEquals(metrics, result);
    }

    @Test
    void testIntegration_MultipleReports() {
        Report r1 = new Report("attendance", Arrays.asList("row1"));
        Report r2 = new Report("leave", Arrays.asList("row2"));
        when(reportRepository.generateAttendanceReport(any(), any())).thenReturn(r1);
        when(reportRepository.generateLeaveReport(any(), any())).thenReturn(r2);
        assertEquals(r1, reportingService.generateAttendanceReport(new Date(), new Date()));
        assertEquals(r2, reportingService.generateLeaveReport(new Date(), new Date()));
    }
}
