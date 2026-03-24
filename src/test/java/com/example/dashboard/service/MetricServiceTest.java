package com.example.dashboard.service;

import com.example.dashboard.entity.Metric;
import com.example.dashboard.repository.MetricRepository;
import com.example.dashboard.exception.MetricException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.CacheManager;
import org.springframework.cache.Cache;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MetricServiceTest {

    @Mock
    private MetricRepository metricRepository;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cache;
    @InjectMocks
    private MetricService metricService;
    private AutoCloseable closeable;
    private Metric testMetric;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        testMetric = new Metric();
        testMetric.setId(1L);
        testMetric.setName("activeUsers");
        testMetric.setValue(100);
        when(cacheManager.getCache(anyString())).thenReturn(cache);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testGetMetricByName_Cached_ReturnsMetric() {
        when(cache.get("activeUsers", Metric.class)).thenReturn(testMetric);
        Metric metric = metricService.getMetricByName("activeUsers");
        assertNotNull(metric);
        assertEquals("activeUsers", metric.getName());
    }

    @Test
    void testGetMetricByName_NotCached_FetchesFromRepository() {
        when(cache.get("activeUsers", Metric.class)).thenReturn(null);
        when(metricRepository.findByName("activeUsers")).thenReturn(Optional.of(testMetric));
        Metric metric = metricService.getMetricByName("activeUsers");
        assertNotNull(metric);
        assertEquals("activeUsers", metric.getName());
    }

    @Test
    void testGetMetricByName_NonExisting_ThrowsException() {
        when(cache.get("inactiveUsers", Metric.class)).thenReturn(null);
        when(metricRepository.findByName("inactiveUsers")).thenReturn(Optional.empty());
        assertThrows(MetricException.class, () -> metricService.getMetricByName("inactiveUsers"));
    }

    @Test
    void testGetMetricByName_NullName_ThrowsException() {
        assertThrows(MetricException.class, () -> metricService.getMetricByName(null));
    }

    @Test
    void testGetMetricByName_EmptyName_ThrowsException() {
        assertThrows(MetricException.class, () -> metricService.getMetricByName(""));
    }

    @Test
    void testGetAllMetrics_ReturnsList() {
        when(metricRepository.findAll()).thenReturn(List.of(testMetric));
        List<Metric> metrics = metricService.getAllMetrics();
        assertNotNull(metrics);
        assertEquals(1, metrics.size());
        assertEquals("activeUsers", metrics.get(0).getName());
    }

    @Test
    void testUpdateMetric_ValidMetric_Success() {
        when(metricRepository.save(any(Metric.class))).thenReturn(testMetric);
        Metric updated = metricService.updateMetric(testMetric);
        assertNotNull(updated);
        assertEquals("activeUsers", updated.getName());
    }

    @Test
    void testUpdateMetric_NullMetric_ThrowsException() {
        assertThrows(MetricException.class, () -> metricService.updateMetric(null));
    }

    @Test
    void testUpdateMetric_InvalidMetric_ThrowsException() {
        Metric invalid = new Metric();
        assertThrows(MetricException.class, () -> metricService.updateMetric(invalid));
    }
}
