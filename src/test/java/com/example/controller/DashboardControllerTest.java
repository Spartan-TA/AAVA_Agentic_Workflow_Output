package com.example.controller;

import com.example.service.MetricService;
import com.example.model.Metric;
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

public class DashboardControllerTest {

    @Mock
    private MetricService metricService;

    @InjectMocks
    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllMetrics() {
        List<Metric> metrics = Arrays.asList(new Metric(1L, "CPU", 0.5), new Metric(2L, "RAM", 0.7));
        when(metricService.getAllMetrics()).thenReturn(metrics);
        ResponseEntity<List<Metric>> response = dashboardController.getAllMetrics();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testGetMetricByIdFound() {
        Metric metric = new Metric(1L, "CPU", 0.5);
        when(metricService.getMetricById(1L)).thenReturn(metric);
        ResponseEntity<Metric> response = dashboardController.getMetricById(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("CPU", response.getBody().getName());
    }

    @Test
    void testGetMetricByIdNotFound() {
        when(metricService.getMetricById(2L)).thenReturn(null);
        ResponseEntity<Metric> response = dashboardController.getMetricById(2L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
