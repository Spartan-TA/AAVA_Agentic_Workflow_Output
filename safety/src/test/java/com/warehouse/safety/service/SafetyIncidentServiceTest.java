package com.warehouse.safety.service;

import com.warehouse.safety.entity.SafetyIncident;
import com.warehouse.safety.repository.SafetyIncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SafetyIncidentServiceTest {
    @Mock
    private SafetyIncidentRepository safetyIncidentRepository;

    @InjectMocks
    private SafetyIncidentService safetyIncidentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testReportIncident() {
        SafetyIncident incident = SafetyIncident.builder().id(1L).location("Dock 1").description("Slip").severity(SafetyIncident.Severity.HIGH).status(SafetyIncident.Status.REPORTED).build();
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(incident);
        SafetyIncident result = safetyIncidentService.reportIncident(incident);
        assertEquals(SafetyIncident.Status.REPORTED, result.getStatus());
        assertEquals("Dock 1", result.getLocation());
    }
}
