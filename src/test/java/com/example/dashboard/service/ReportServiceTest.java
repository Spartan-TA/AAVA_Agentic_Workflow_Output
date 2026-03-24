package com.example.dashboard.service;

import com.example.dashboard.integration.PdfGenerator;
import com.example.dashboard.exception.ReportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    @Mock
    private PdfGenerator pdfGenerator;

    @InjectMocks
    private ReportService reportService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testGeneratePdfReport_ValidData_Success() {
        List<String> data = List.of("row1", "row2");
        byte[] pdfBytes = new byte[]{1, 2, 3};
        when(pdfGenerator.generatePdf(data)).thenReturn(pdfBytes);
        byte[] result = reportService.generatePdfReport(data);
        assertNotNull(result);
        assertArrayEquals(pdfBytes, result);
    }

    @Test
    void testGeneratePdfReport_NullData_ThrowsException() {
        assertThrows(ReportException.class, () -> reportService.generatePdfReport(null));
    }

    @Test
    void testGeneratePdfReport_EmptyData_ThrowsException() {
        assertThrows(ReportException.class, () -> reportService.generatePdfReport(List.of()));
    }

    @Test
    void testGeneratePdfReport_PdfGeneratorThrowsException_ThrowsReportException() {
        List<String> data = List.of("row1");
        when(pdfGenerator.generatePdf(data)).thenThrow(new RuntimeException("PDF error"));
        assertThrows(ReportException.class, () -> reportService.generatePdfReport(data));
    }
}
