package com.example.service;

import com.example.model.Metric;
import com.example.repository.MetricRepository;
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

public class MetricServiceTest {

    @Mock
    private MetricRepository metricRepository;

    @InjectMocks
    private MetricService metricService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetMetricByIdFound() {
        Metric metric = new Metric(1L, "CPU", 0.5);
        when(metricRepository.findById(1L)).thenReturn(Optional.of(metric));
        Metric found = metricService.getMetricById(1L);
        assertNotNull(found);
        assertEquals("CPU", found.getName());
    }

    @Test
    void testGetMetricByIdNotFound() {
        when(metricRepository.findById(2L)).thenReturn(Optional.empty());
        Metric found = metricService.getMetricById(2L);
        assertNull(found);
    }

    @Test
    void testGetAllMetrics() {
        List<Metric> metrics = Arrays.asList(new Metric(1L, "CPU", 0.5), new Metric(2L, "RAM", 0.7));
        when(metricRepository.findAll()).thenReturn(metrics);
        List<Metric> result = metricService.getAllMetrics();
        assertEquals(2, result.size());
        assertEquals("CPU", result.get(0).getName());
        assertEquals("RAM", result.get(1).getName());
    }

    @Test
    void testSaveMetric() {
        Metric metric = new Metric(null, "DISK", 0.9);
        when(metricRepository.save(metric)).thenReturn(new Metric(3L, "DISK", 0.9));
        Metric saved = metricService.saveMetric(metric);
        assertNotNull(saved);
        assertEquals("DISK", saved.getName());
        assertEquals(0.9, saved.getValue());
    }

    @Test
    void testDeleteMetric() {
        doNothing().when(metricRepository).deleteById(1L);
        metricService.deleteMetric(1L);
        verify(metricRepository).deleteById(1L);
    }
}
