package com.wms.safety.service;

import com.wms.safety.domain.SafetyIncident;
import com.wms.safety.domain.IncidentSeverity;
import com.wms.safety.domain.IncidentStatus;
import com.wms.safety.dto.SafetyIncidentDto;
import com.wms.safety.repository.SafetyIncidentRepository;
import com.wms.employee.domain.Employee;
import com.wms.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for SafetyService
 * Tests cover incident reporting, workflow management, OSHA compliance, and edge cases
 */
@DisplayName("Safety Service Tests")
public class SafetyServiceTest {

    @Mock
    private SafetyIncidentRepository safetyIncidentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private SafetyServiceImpl safetyService;

    private Employee testEmployee;
    private SafetyIncident testIncident;
    private SafetyIncidentDto incidentDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        // Setup test incident
        testIncident = new SafetyIncident();
        testIncident.setId(1L);
        testIncident.setDate(LocalDateTime.now());
        testIncident.setLocation("Warehouse Floor A");
        testIncident.setDescription("Slip and fall incident");
        testIncident.setSeverity(IncidentSeverity.MINOR);
        testIncident.setInvolvedEmployees(Arrays.asList(testEmployee));
        testIncident.setStatus(IncidentStatus.OPEN);
        testIncident.setCorrectiveActions("Install non-slip mats");

        // Setup DTO
        incidentDto = new SafetyIncidentDto();
        incidentDto.setDate(LocalDateTime.now());
        incidentDto.setLocation("Warehouse Floor A");
        incidentDto.setDescription("Slip and fall incident");
        incidentDto.setSeverity("MINOR");
        incidentDto.setInvolvedEmployeeIds(Arrays.asList(1L));
    }

    // ========== REPORT INCIDENT TESTS ==========

    @Test
    @DisplayName("Test report incident with valid data")
    public void testReportIncident_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        // Act
        SafetyIncidentDto result = safetyService.reportIncident(incidentDto);

        // Assert
        assertNotNull(result);
        assertEquals("Warehouse Floor A", result.getLocation());
        assertEquals(IncidentStatus.OPEN.name(), result.getStatus());
        verify(safetyIncidentRepository, times(1)).save(any(SafetyIncident.class));
    }

    @Test
    @DisplayName("Test report incident with null date throws exception")
    public void testReportIncident_NullDate_ThrowsException() {
        // Arrange
        incidentDto.setDate(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.reportIncident(incidentDto);
        });
    }

    @Test
    @DisplayName("Test report incident with null location throws exception")
    public void testReportIncident_NullLocation_ThrowsException() {
        // Arrange
        incidentDto.setLocation(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.reportIncident(incidentDto);
        });
    }

    @Test
    @DisplayName("Test report incident with empty location throws exception")
    public void testReportIncident_EmptyLocation_ThrowsException() {
        // Arrange
        incidentDto.setLocation("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.reportIncident(incidentDto);
        });
    }

    @Test
    @DisplayName("Test report incident with null description throws exception")
    public void testReportIncident_NullDescription_ThrowsException() {
        // Arrange
        incidentDto.setDescription(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.reportIncident(incidentDto);
        });
    }

    @Test
    @DisplayName("Test report incident with null severity throws exception")
    public void testReportIncident_NullSeverity_ThrowsException() {
        // Arrange
        incidentDto.setSeverity(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.reportIncident(incidentDto);
        });
    }

    @Test
    @DisplayName("Test report incident with invalid severity throws exception")
    public void testReportIncident_InvalidSeverity_ThrowsException() {
        // Arrange
        incidentDto.setSeverity("INVALID");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.reportIncident(incidentDto);
        });
    }

    @Test
    @DisplayName("Test report incident with null involved employees throws exception")
    public void testReportIncident_NullInvolvedEmployees_ThrowsException() {
        // Arrange
        incidentDto.setInvolvedEmployeeIds(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.reportIncident(incidentDto);
        });
    }

    @Test
    @DisplayName("Test report incident with empty involved employees throws exception")
    public void testReportIncident_EmptyInvolvedEmployees_ThrowsException() {
        // Arrange
        incidentDto.setInvolvedEmployeeIds(Arrays.asList());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.reportIncident(incidentDto);
        });
    }

    @Test
    @DisplayName("Test report incident with non-existent employee throws exception")
    public void testReportIncident_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.reportIncident(incidentDto);
        });
    }

    @Test
    @DisplayName("Test report incident with future date throws exception")
    public void testReportIncident_FutureDate_ThrowsException() {
        // Arrange
        incidentDto.setDate(LocalDateTime.now().plusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.reportIncident(incidentDto);
        });
    }

    // ========== UPDATE STATUS TESTS ==========

    @Test
    @DisplayName("Test update incident status from OPEN to INVESTIGATING")
    public void testUpdateStatus_OpenToInvestigating_Success() {
        // Arrange
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        // Act
        SafetyIncidentDto result = safetyService.updateStatus(1L, IncidentStatus.INVESTIGATING);

        // Assert
        assertNotNull(result);
        assertEquals(IncidentStatus.INVESTIGATING.name(), result.getStatus());
        verify(safetyIncidentRepository, times(1)).save(any(SafetyIncident.class));
    }

    @Test
    @DisplayName("Test update incident status from INVESTIGATING to RESOLVED")
    public void testUpdateStatus_InvestigatingToResolved_Success() {
        // Arrange
        testIncident.setStatus(IncidentStatus.INVESTIGATING);
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        // Act
        SafetyIncidentDto result = safetyService.updateStatus(1L, IncidentStatus.RESOLVED);

        // Assert
        assertNotNull(result);
        assertEquals(IncidentStatus.RESOLVED.name(), result.getStatus());
    }

    @Test
    @DisplayName("Test update status with null incident ID throws exception")
    public void testUpdateStatus_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.updateStatus(null, IncidentStatus.INVESTIGATING);
        });
    }

    @Test
    @DisplayName("Test update status with null status throws exception")
    public void testUpdateStatus_NullStatus_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.updateStatus(1L, null);
        });
    }

    @Test
    @DisplayName("Test update status of non-existent incident throws exception")
    public void testUpdateStatus_NonExistentIncident_ThrowsException() {
        // Arrange
        when(safetyIncidentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.updateStatus(999L, IncidentStatus.INVESTIGATING);
        });
    }

    @Test
    @DisplayName("Test update status from RESOLVED to OPEN throws exception")
    public void testUpdateStatus_ResolvedToOpen_ThrowsException() {
        // Arrange
        testIncident.setStatus(IncidentStatus.RESOLVED);
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            safetyService.updateStatus(1L, IncidentStatus.OPEN);
        });
    }

    // ========== ADD CORRECTIVE ACTIONS TESTS ==========

    @Test
    @DisplayName("Test add corrective actions with valid data")
    public void testAddCorrectiveActions_ValidData_Success() {
        // Arrange
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        // Act
        SafetyIncidentDto result = safetyService.addCorrectiveActions(1L, "Additional safety training");

        // Assert
        assertNotNull(result);
        verify(safetyIncidentRepository, times(1)).save(any(SafetyIncident.class));
    }

    @Test
    @DisplayName("Test add corrective actions with null ID throws exception")
    public void testAddCorrectiveActions_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.addCorrectiveActions(null, "Actions");
        });
    }

    @Test
    @DisplayName("Test add corrective actions with null actions throws exception")
    public void testAddCorrectiveActions_NullActions_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.addCorrectiveActions(1L, null);
        });
    }

    @Test
    @DisplayName("Test add corrective actions with empty actions throws exception")
    public void testAddCorrectiveActions_EmptyActions_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.addCorrectiveActions(1L, "");
        });
    }

    // ========== OSHA EXPORT TESTS ==========

    @Test
    @DisplayName("Test export OSHA report for date range")
    public void testExportOSHAReport_ValidDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();
        when(safetyIncidentRepository.findByDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testIncident));

        // Act
        byte[] result = safetyService.exportOSHAReport(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    @DisplayName("Test export OSHA report with null start date throws exception")
    public void testExportOSHAReport_NullStartDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.exportOSHAReport(null, LocalDate.now());
        });
    }

    @Test
    @DisplayName("Test export OSHA report with null end date throws exception")
    public void testExportOSHAReport_NullEndDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.exportOSHAReport(LocalDate.now(), null);
        });
    }

    @Test
    @DisplayName("Test export OSHA report with end date before start date throws exception")
    public void testExportOSHAReport_EndBeforeStart_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.exportOSHAReport(LocalDate.now(), LocalDate.now().minusDays(1));
        });
    }

    // ========== GET INCIDENTS TESTS ==========

    @Test
    @DisplayName("Test get incidents by status")
    public void testGetIncidentsByStatus_Success() {
        // Arrange
        when(safetyIncidentRepository.findByStatus(IncidentStatus.OPEN))
                .thenReturn(Arrays.asList(testIncident));

        // Act
        List<SafetyIncidentDto> result = safetyService.getIncidentsByStatus(IncidentStatus.OPEN);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test get incidents by status with null status throws exception")
    public void testGetIncidentsByStatus_NullStatus_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.getIncidentsByStatus(null);
        });
    }

    @Test
    @DisplayName("Test get incidents by severity")
    public void testGetIncidentsBySeverity_Success() {
        // Arrange
        when(safetyIncidentRepository.findBySeverity(IncidentSeverity.MINOR))
                .thenReturn(Arrays.asList(testIncident));

        // Act
        List<SafetyIncidentDto> result = safetyService.getIncidentsBySeverity(IncidentSeverity.MINOR);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test get incidents by date range")
    public void testGetIncidentsByDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        when(safetyIncidentRepository.findByDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testIncident));

        // Act
        List<SafetyIncidentDto> result = safetyService.getIncidentsByDateRange(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test get incidents by employee")
    public void testGetIncidentsByEmployee_Success() {
        // Arrange
        when(safetyIncidentRepository.findByInvolvedEmployeesContaining(any(Employee.class)))
                .thenReturn(Arrays.asList(testIncident));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        List<SafetyIncidentDto> result = safetyService.getIncidentsByEmployee(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== METRICS TESTS ==========

    @Test
    @DisplayName("Test get safety metrics for date range")
    public void testGetSafetyMetrics_ValidDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();
        when(safetyIncidentRepository.findByDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testIncident));

        // Act
        Map<String, Object> result = safetyService.getSafetyMetrics(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("totalIncidents"));
        assertTrue(result.containsKey("incidentsBySeverity"));
        assertTrue(result.containsKey("incidentsByStatus"));
    }

    @Test
    @DisplayName("Test get safety metrics with null start date throws exception")
    public void testGetSafetyMetrics_NullStartDate_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.getSafetyMetrics(null, LocalDate.now());
        });
    }

    // ========== DELETE INCIDENT TESTS ==========

    @Test
    @DisplayName("Test delete incident with valid ID")
    public void testDeleteIncident_ValidId_Success() {
        // Arrange
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        doNothing().when(safetyIncidentRepository).delete(any(SafetyIncident.class));

        // Act
        safetyService.deleteIncident(1L);

        // Assert
        verify(safetyIncidentRepository, times(1)).delete(any(SafetyIncident.class));
    }

    @Test
    @DisplayName("Test delete incident with null ID throws exception")
    public void testDeleteIncident_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            safetyService.deleteIncident(null);
        });
    }

    // ========== BOUNDARY AND EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test report incident with all severity levels")
    public void testReportIncident_AllSeverityLevels_Success() {
        // Test each severity level
        String[] severities = {"MINOR", "MODERATE", "MAJOR", "CRITICAL"};
        
        for (String severity : severities) {
            // Arrange
            incidentDto.setSeverity(severity);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
            when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

            // Act
            SafetyIncidentDto result = safetyService.reportIncident(incidentDto);

            // Assert
            assertNotNull(result);
        }
    }

    @Test
    @DisplayName("Test report incident with multiple involved employees")
    public void testReportIncident_MultipleEmployees_Success() {
        // Arrange
        Employee employee2 = new Employee();
        employee2.setId(2L);
        incidentDto.setInvolvedEmployeeIds(Arrays.asList(1L, 2L));
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee2));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        // Act
        SafetyIncidentDto result = safetyService.reportIncident(incidentDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test report incident with very long description")
    public void testReportIncident_LongDescription_Success() {
        // Arrange
        String longDescription = "A".repeat(5000);
        incidentDto.setDescription(longDescription);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        // Act
        SafetyIncidentDto result = safetyService.reportIncident(incidentDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test export OSHA report for full year")
    public void testExportOSHAReport_FullYear_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusYears(1);
        LocalDate endDate = LocalDate.now();
        when(safetyIncidentRepository.findByDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testIncident));

        // Act
        byte[] result = safetyService.exportOSHAReport(startDate, endDate);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test get incidents returns empty list when no incidents")
    public void testGetIncidentsByStatus_NoIncidents_ReturnsEmptyList() {
        // Arrange
        when(safetyIncidentRepository.findByStatus(IncidentStatus.OPEN))
                .thenReturn(Arrays.asList());

        // Act
        List<SafetyIncidentDto> result = safetyService.getIncidentsByStatus(IncidentStatus.OPEN);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Test report incident with special characters in location")
    public void testReportIncident_SpecialCharactersInLocation_Success() {
        // Arrange
        incidentDto.setLocation("Warehouse Floor A - Section 1/2");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        // Act
        SafetyIncidentDto result = safetyService.reportIncident(incidentDto);

        // Assert
        assertNotNull(result);
    }
}