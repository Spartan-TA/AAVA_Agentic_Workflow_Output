package com.warehouse.management.safety;

import com.warehouse.management.safety.SafetyIncidentService;
import com.warehouse.management.safety.SafetyIncident;
import com.warehouse.management.employee.Employee;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SafetyIncidentServiceTest {

    @Mock
    private SafetyIncidentRepository safetyIncidentRepository;

    @InjectMocks
    private SafetyIncidentService safetyIncidentService;

    private Employee employee;
    private SafetyIncident incident;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = new Employee(1L, "John Doe", "BADGE123", "WORKER", "Logistics", "A", new Date(), "ACTIVE");
        incident = new SafetyIncident(1L, "Near Miss", "Low", "Zone A", employee, new Date(), "Open");
    }

    @Test
    void testRecordIncident_Valid() {
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(incident);
        SafetyIncident result = safetyIncidentService.recordIncident("Near Miss", "Low", "Zone A", employee, new Date());
        assertNotNull(result);
        assertEquals("Near Miss", result.getType());
    }

    @Test
    void testRecordIncident_InvalidSeverity() {
        assertThrows(IllegalArgumentException.class, () -> safetyIncidentService.recordIncident("Near Miss", "Invalid", "Zone A", employee, new Date()));
    }

    @Test
    void testGenerateOSHAReport() {
        when(safetyIncidentRepository.findAll()).thenReturn(Arrays.asList(incident));
        String report = safetyIncidentService.generateOSHAReport();
        assertNotNull(report);
        assertTrue(report.contains("Near Miss"));
    }
}