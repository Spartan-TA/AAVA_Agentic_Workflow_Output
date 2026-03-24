package com.example.dashboard.controller;

import com.example.dashboard.service.ReportService;
import com.example.dashboard.exception.ReportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

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
    void testExportReport_ValidData_ReturnsPdf() {
        List<String> data = List.of("row1", "row2");
        byte[] pdfBytes = new byte[]{1, 2, 3};
        when(reportService.generatePdfReport(data)).thenReturn(pdfBytes);
        ResponseEntity<byte[]> result = reportController.exportReport(data);
        assertNotNull(result);
        assertArrayEquals(pdfBytes, result.getBody());
    }

    @Test
    void testExportReport_NullData_ThrowsException() {
        when(reportService.generatePdfReport(null)).thenThrow(new ReportException("Null data"));
        assertThrows(ReportException.class, () -> reportController.exportReport(null));
    }

    @Test
    void testExportReport_EmptyData_ThrowsException() {
        when(reportService.generatePdfReport(List.of())).thenThrow(new ReportException("Empty data"));
        assertThrows(ReportException.class, () -> reportController.exportReport(List.of()));
    }
}
