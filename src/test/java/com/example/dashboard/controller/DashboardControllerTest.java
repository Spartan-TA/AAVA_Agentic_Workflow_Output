package com.example.dashboard.controller;

import com.example.dashboard.service.MetricService;
import com.example.dashboard.entity.Metric;
import com.example.dashboard.exception.MetricException;
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

class DashboardControllerTest {

    @Mock
    private MetricService metricService;

    @InjectMocks
    private DashboardController dashboardController;

    private AutoCloseable closeable;
    private Metric testMetric;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        testMetric = new Metric();
        testMetric.setId(1L);
        testMetric.setName("activeUsers");
        testMetric.setValue(100);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testGetSummaryMetrics_ReturnsMetrics() {
        when(metricService.getAllMetrics()).thenReturn(List.of(testMetric));
        ResponseEntity<List<Metric>> result = dashboardController.getSummaryMetrics();
        assertNotNull(result);
        assertEquals(1, result.getBody().size());
        assertEquals("activeUsers", result.getBody().get(0).getName());
    }

    @Test
    void testGetSummaryMetrics_EmptyList() {
        when(metricService.getAllMetrics()).thenReturn(List.of());
        ResponseEntity<List<Metric>> result = dashboardController.getSummaryMetrics();
        assertNotNull(result);
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    void testGetSummaryMetrics_ServiceThrowsException() {
        when(metricService.getAllMetrics()).thenThrow(new MetricException("Error"));
        assertThrows(MetricException.class, () -> dashboardController.getSummaryMetrics());
    }

    @Test
    void testGetMetricByName_ValidName_ReturnsMetric() {
        when(metricService.getMetricByName("activeUsers")).thenReturn(testMetric);
        ResponseEntity<Metric> result = dashboardController.getMetricByName("activeUsers");
        assertNotNull(result);
        assertEquals("activeUsers", result.getBody().getName());
    }

    @Test
    void testGetMetricByName_InvalidName_ThrowsException() {
        when(metricService.getMetricByName("invalid")).thenThrow(new MetricException("Not found"));
        assertThrows(MetricException.class, () -> dashboardController.getMetricByName("invalid"));
    }

    @Test
    void testGetMetricByName_NullName_ThrowsException() {
        when(metricService.getMetricByName(null)).thenThrow(new MetricException("Null name"));
        assertThrows(MetricException.class, () -> dashboardController.getMetricByName(null));
    }
}
