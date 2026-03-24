package com.example.controller;

import com.example.model.Report;
import com.example.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllReports() {
        List<Report> reports = Arrays.asList(new Report(1L, "Monthly", "Content"), new Report(2L, "Weekly", "Content2"));
        when(reportService.getAllReports()).thenReturn(reports);
        ResponseEntity<List<Report>> response = reportController.getAllReports();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testGetReportByIdFound() {
        Report report = new Report(1L, "Monthly", "Content");
        when(reportService.getReportById(1L)).thenReturn(report);
        ResponseEntity<Report> response = reportController.getReportById(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Monthly", response.getBody().getTitle());
    }

    @Test
    void testGetReportByIdNotFound() {
        when(reportService.getReportById(2L)).thenReturn(null);
        ResponseEntity<Report> response = reportController.getReportById(2L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCreateReport() {
        Report report = new Report(null, "Annual", "Content");
        Report saved = new Report(3L, "Annual", "Content");
        when(reportService.saveReport(report)).thenReturn(saved);
        ResponseEntity<Report> response = reportController.createReport(report);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Annual", response.getBody().getTitle());
    }

    @Test
    void testDeleteReport() {
        doNothing().when(reportService).deleteReport(1L);
        ResponseEntity<Void> response = reportController.deleteReport(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(reportService).deleteReport(1L);
    }
}