package com.company.warehouse.safety.service;

import com.company.warehouse.safety.domain.*;
import com.company.warehouse.safety.dto.*;
import com.company.warehouse.safety.repository.SafetyIncidentRepository;
import com.company.warehouse.employee.domain.Employee;
import com.company.warehouse.employee.repository.EmployeeRepository;
import com.company.warehouse.common.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Safety Service Tests")
public class SafetyServiceTest {
    @Mock private SafetyIncidentRepository safetyIncidentRepository;
    @Mock private EmployeeRepository employeeRepository;
    @InjectMocks private SafetyService safetyService;
    private SafetyIncident incident;
    private Employee testEmployee;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        incident = new SafetyIncident();
        incident.setId(1L);
        incident.setReporter(testEmployee);
        incident.setIncidentType(IncidentType.INJURY);
        incident.setSeverity(Severity.MINOR);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setDescription("Minor cut on hand");
        incident.setLocation("Warehouse Floor A");
        incident.setIncidentDate(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test recordIncident with valid data")
    public void testRecordIncident_ValidData() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(incident);
        SafetyIncidentDTO result = safetyService.recordIncident(new SafetyIncidentCreateDTO());
        assertNotNull(result);
        assertEquals(IncidentStatus.OPEN, result.getStatus());
        verify(safetyIncidentRepository, times(1)).save(any(SafetyIncident.class));
    }

    @Test
    @DisplayName("Test recordIncident with null description")
    public void testRecordIncident_NullDescription() {
        SafetyIncidentCreateDTO dto = new SafetyIncidentCreateDTO();
        dto.setDescription(null);
        assertThrows(IllegalArgumentException.class, () -> safetyService.recordIncident(dto));
    }

    @Test
    @DisplayName("Test updateIncidentStatus from OPEN to INVESTIGATING")
    public void testUpdateIncidentStatus_OpenToInvestigating() {
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(incident);
        SafetyIncidentDTO result = safetyService.updateIncidentStatus(1L, IncidentStatus.INVESTIGATING);
        assertNotNull(result);
        assertEquals(IncidentStatus.INVESTIGATING, incident.getStatus());
    }

    @Test
    @DisplayName("Test updateIncidentStatus from INVESTIGATING to RESOLVED")
    public void testUpdateIncidentStatus_InvestigatingToResolved() {
        incident.setStatus(IncidentStatus.INVESTIGATING);
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(incident);
        SafetyIncidentDTO result = safetyService.updateIncidentStatus(1L, IncidentStatus.RESOLVED);
        assertNotNull(result);
        assertEquals(IncidentStatus.RESOLVED, incident.getStatus());
    }

    @Test
    @DisplayName("Test updateIncidentStatus with invalid transition")
    public void testUpdateIncidentStatus_InvalidTransition() {
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        assertThrows(BusinessException.class, () -> safetyService.updateIncidentStatus(1L, IncidentStatus.RESOLVED));
    }

    @Test
    @DisplayName("Test generateOSHAReport for date range")
    public void testGenerateOSHAReport_DateRange() {
        when(safetyIncidentRepository.findByIncidentDateBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(Arrays.asList(incident));
        OSHAReportDTO result = safetyService.generateOSHAReport(LocalDateTime.now().minusMonths(1), LocalDateTime.now());
        assertNotNull(result);
        assertTrue(result.getTotalIncidents() > 0);
    }

    @Test
    @DisplayName("Test getIncidentsBySeverity")
    public void testGetIncidentsBySeverity_Minor() {
        when(safetyIncidentRepository.findBySeverity(Severity.MINOR)).thenReturn(Arrays.asList(incident));
        List<SafetyIncidentDTO> results = safetyService.getIncidentsBySeverity(Severity.MINOR);
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }