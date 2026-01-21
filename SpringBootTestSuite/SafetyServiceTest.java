package com.wms.safety.service;

import com.wms.safety.entity.SafetyIncident;
import com.wms.safety.repository.SafetyIncidentRepository;
import com.wms.safety.dto.SafetyIncidentDto;
import com.wms.safety.dto.OSHAReportDto;
import com.wms.employee.entity.Employee;
import com.wms.employee.repository.EmployeeRepository;
import com.wms.exception.ResourceNotFoundException;
import com.wms.exception.BadRequestException;
import com.wms.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for SafetyService
 * Covers incident reporting, status workflow, OSHA compliance, and metrics
 */
public class SafetyServiceTest {

    @Mock
    private SafetyIncidentRepository safetyIncidentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private SafetyService safetyService;

    private Employee testEmployee;
    private SafetyIncident testIncident;
    private SafetyIncidentDto incidentDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("John Doe");
        testEmployee.setBadgeId("BADGE001");
        
        // Setup test incident
        testIncident = new SafetyIncident();
        testIncident.setId(1L);
        testIncident.setReportedBy(1L);
        testIncident.setIncidentDate(LocalDateTime.now().minusHours(2));
        testIncident.setLocation("Warehouse Aisle 5");
        testIncident.setDescription("Forklift collision with shelving unit");
        testIncident.setSeverity("MEDIUM");
        testIncident.setStatus("REPORTED");
        testIncident.setInjuryOccurred(false);
        testIncident.setPropertyDamage(true);
        
        // Setup incident DTO
        incidentDto = new SafetyIncidentDto();
        incidentDto.setReportedBy(1L);
        incidentDto.setIncidentDate(LocalDateTime.now().minusHours(2));
        incidentDto.setLocation("Warehouse Aisle 5");
        incidentDto.setDescription("Forklift collision with shelving unit");
        incidentDto.setSeverity("MEDIUM");
        incidentDto.setInjuryOccurred(false);
        incidentDto.setPropertyDamage(true);
    }

    // ========== INCIDENT REPORTING TESTS ==========

    @Test
    @DisplayName("Test report safety incident with valid data")
    public void testReportIncident_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        // Act
        SafetyIncidentDto result = safetyService.reportIncident(incidentDto);

        // Assert
        assertNotNull(result);
        assertEquals("REPORTED", result.getStatus());
        assertEquals("MEDIUM", result.getSeverity());
        verify(safetyIncidentRepository, times(1)).save(any(SafetyIncident.class));
    }

    @Test
    @DisplayName("Test report incident for non-existent employee")
    public void testReportIncident_NonExistentEmployee_ThrowsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        incidentDto.setReportedBy(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            safetyService.reportIncident(incidentDto);
        });
    }

    @Test
    @DisplayName("Test report incident with null description")
    public void testReportIncident_NullDescription_ThrowsBadRequestException() {
        // Arrange
        incidentDto.setDescription(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            safetyService.reportIncident(incidentDto);
        });
    }

    @Test
    @DisplayName("Test report incident with empty location")
    public void testReportIncident_EmptyLocation_ThrowsBadRequestException() {
        // Arrange
        incidentDto.setLocation("");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            safetyService.reportIncident(incidentDto);
        });
    }

    @Test
    @DisplayName("Test report incident with future date")
    public void testReportIncident_FutureDate_ThrowsBadRequestException() {
        // Arrange
        incidentDto.setIncidentDate(LocalDateTime.now().plusHours(1));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            safetyService.reportIncident(incidentDto);
        });
    }

    @Test
    @DisplayName("Test report incident with invalid severity")
    public void testReportIncident_InvalidSeverity_ThrowsBadRequestException() {
        // Arrange
        incidentDto.setSeverity("INVALID");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            safetyService.reportIncident(incidentDto);
        });
    }

    @Test
    @DisplayName("Test report near-miss incident")
    public void testReportIncident_NearMiss_Success() {
        // Arrange
        incidentDto.setDescription("Near-miss: Almost struck by forklift");
        incidentDto.setSeverity("LOW");
        incidentDto.setInjuryOccurred(false);
        incidentDto.setPropertyDamage(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        // Act
        SafetyIncidentDto result = safetyService.reportIncident(incidentDto);

        // Assert
        assertNotNull(result);
        assertFalse(result.isInjuryOccurred());
        assertFalse(result.isPropertyDamage());
    }

    @Test
    @DisplayName("Test report critical incident with injury")
    public void testReportIncident_CriticalWithInjury_Success() {
        // Arrange
        incidentDto.setSeverity("CRITICAL");
        incidentDto.setInjuryOccurred(true);
        incidentDto.setInjuryDescription("Broken arm from fall");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        // Act
        SafetyIncidentDto result = safetyService.reportIncident(incidentDto);

        // Assert
        assertNotNull(result);
        assertEquals("CRITICAL", result.getSeverity());
        assertTrue(result.isInjuryOccurred());
    }

    // ========== INCIDENT STATUS WORKFLOW TESTS ==========

    @Test
    @DisplayName("Test update incident status to INVESTIGATING")
    public void testUpdateIncidentStatus_ToInvestigating_Success() {
        // Arrange
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        // Act
        SafetyIncidentDto result = safetyService.updateIncidentStatus(1L, "INVESTIGATING", "Investigation started");

        // Assert
        assertNotNull(result);
        assertEquals("INVESTIGATING", result.getStatus());
        verify(safetyIncidentRepository, times(1)).save(any(SafetyIncident.class));
    }

    @Test
    @DisplayName("Test update incident status to RESOLVED")
    public void testUpdateIncidentStatus_ToResolved_Success() {
        // Arrange
        testIncident.setStatus("INVESTIGATING");
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        // Act
        SafetyIncidentDto result = safetyService.updateIncidentStatus(1L, "RESOLVED", "Corrective actions completed");

        // Assert
        assertNotNull(result);
        assertEquals("RESOLVED", result.getStatus());
    }

    @Test
    @DisplayName("Test update incident status to CLOSED")
    public void testUpdateIncidentStatus_ToClosed_Success() {
        // Arrange
        testIncident.setStatus("RESOLVED");
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        // Act
        SafetyIncidentDto result = safetyService.updateIncidentStatus(1L, "CLOSED", "Case closed");

        // Assert
        assertNotNull(result);
        assertEquals("CLOSED", result.getStatus());
    }

    @Test
    @DisplayName("Test update non-existent incident")
    public void testUpdateIncidentStatus_NonExistentIncident_ThrowsResourceNotFoundException() {
        // Arrange
        when(safetyIncidentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            safetyService.updateIncidentStatus(999L, "INVESTIGATING", "Notes");
        });
    }

    @Test
    @DisplayName("Test invalid status transition")
    public void testUpdateIncidentStatus_InvalidTransition_ThrowsBadRequestException() {
        // Arrange
        testIncident.setStatus("REPORTED");
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            safetyService.updateIncidentStatus(1L, "CLOSED", "Skip to closed");
        });
    }

    @Test
    @DisplayName("Test update already closed incident")
    public void testUpdateIncidentStatus_AlreadyClosed_ThrowsConflictException() {
        // Arrange
        testIncident.setStatus("CLOSED");
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            safetyService.updateIncidentStatus(1L, "INVESTIGATING", "Reopen");
        });
    }

    // ========== INCIDENT QUERY TESTS ==========

    @Test
    @DisplayName("Test get incident by ID")
    public void testGetIncidentById_ValidId_Success() {
        // Arrange
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));

        // Act
        SafetyIncidentDto result = safetyService.getIncidentById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("REPORTED", result.getStatus());
    }

    @Test
    @DisplayName("Test get all incidents by status")
    public void testGetIncidentsByStatus_ValidStatus_Success() {
        // Arrange
        when(safetyIncidentRepository.findByStatus("REPORTED"))
            .thenReturn(Arrays.asList(testIncident));

        // Act
        List<SafetyIncidentDto> result = safetyService.getIncidentsByStatus("REPORTED");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("REPORTED", result.get(0).getStatus());
    }

    @Test
    @DisplayName("Test get incidents by date range")
    public void testGetIncidentsByDateRange_ValidRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        when(safetyIncidentRepository.findByIncidentDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(testIncident));

        // Act
        List<SafetyIncidentDto> result = safetyService.getIncidentsByDateRange(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test get incidents by severity")
    public void testGetIncidentsBySeverity_ValidSeverity_Success() {
        // Arrange
        when(safetyIncidentRepository.findBySeverity("MEDIUM"))
            .thenReturn(Arrays.asList(testIncident));

        // Act
        List<SafetyIncidentDto> result = safetyService.getIncidentsBySeverity("MEDIUM");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MEDIUM", result.get(0).getSeverity());
    }

    @Test
    @DisplayName("Test get incidents by location")
    public void testGetIncidentsByLocation_ValidLocation_Success() {
        // Arrange
        when(safetyIncidentRepository.findByLocationContaining("Aisle 5"))
            .thenReturn(Arrays.asList(testIncident));

        // Act
        List<SafetyIncidentDto> result = safetyService.getIncidentsByLocation("Aisle 5");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== OSHA REPORTING TESTS ==========

    @Test
    @DisplayName("Test generate OSHA 300 report")
    public void testGenerateOSHA300Report_ValidDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        testIncident.setInjuryOccurred(true);
        when(safetyIncidentRepository.findByIncidentDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(testIncident));

        // Act
        OSHAReportDto result = safetyService.generateOSHA300Report(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalIncidents());
        assertTrue(result.getIncidents().size() > 0);
    }

    @Test
    @DisplayName("Test generate OSHA 300A summary")
    public void testGenerateOSHA300ASummary_ValidYear_Success() {
        // Arrange
        int year = 2023;
        testIncident.setInjuryOccurred(true);
        when(safetyIncidentRepository.findByYear(year))
            .thenReturn(Arrays.asList(testIncident));

        // Act
        OSHAReportDto result = safetyService.generateOSHA300ASummary(year);

        // Assert
        assertNotNull(result);
        assertEquals(year, result.getYear());
        assertTrue(result.getTotalIncidents() > 0);
    }

    @Test
    @DisplayName("Test OSHA report includes required fields")
    public void testOSHAReport_IncludesRequiredFields_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusYears(1);
        LocalDate endDate = LocalDate.now();
        testIncident.setInjuryOccurred(true);
        testIncident.setInjuryDescription("Broken arm");
        testIncident.setDaysAwayFromWork(5);
        when(safetyIncidentRepository.findByIncidentDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(testIncident));

        // Act
        OSHAReportDto result = safetyService.generateOSHA300Report(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIncidents().stream()
            .anyMatch(i -> i.getInjuryDescription() != null && i.getDaysAwayFromWork() != null));
    }

    @Test
    @DisplayName("Test export OSHA report as CSV")
    public void testExportOSHAReportAsCSV_ValidData_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusYears(1);
        LocalDate endDate = LocalDate.now();
        when(safetyIncidentRepository.findByIncidentDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(testIncident));

        // Act
        String csvContent = safetyService.exportOSHAReportAsCSV(startDate, endDate);

        // Assert
        assertNotNull(csvContent);
        assertTrue(csvContent.contains("Incident Date"));
        assertTrue(csvContent.contains("Location"));
        assertTrue(csvContent.contains("Severity"));
    }

    // ========== METRICS AND DASHBOARD TESTS ==========

    @Test
    @DisplayName("Test get safety metrics")
    public void testGetSafetyMetrics_ValidDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();
        when(safetyIncidentRepository.findByIncidentDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(testIncident));

        // Act
        Map<String, Object> metrics = safetyService.getSafetyMetrics(startDate, endDate);

        // Assert
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("totalIncidents"));
        assertTrue(metrics.containsKey("incidentsBySeverity"));
        assertTrue(metrics.containsKey("incidentsByStatus"));
    }

    @Test
    @DisplayName("Test calculate incident rate")
    public void testCalculateIncidentRate_ValidData_Success() {
        // Arrange
        int totalIncidents = 5;
        int totalEmployees = 100;
        int totalHoursWorked = 200000;

        // Act
        double incidentRate = safetyService.calculateIncidentRate(totalIncidents, totalHoursWorked);

        // Assert
        assertTrue(incidentRate > 0);
    }

    @Test
    @DisplayName("Test get incidents by severity distribution")
    public void testGetIncidentsBySeverityDistribution_ValidData_Success() {
        // Arrange
        SafetyIncident lowIncident = new SafetyIncident();
        lowIncident.setSeverity("LOW");
        SafetyIncident highIncident = new SafetyIncident();
        highIncident.setSeverity("HIGH");
        
        when(safetyIncidentRepository.findAll())
            .thenReturn(Arrays.asList(testIncident, lowIncident, highIncident));

        // Act
        Map<String, Long> distribution = safetyService.getIncidentsBySeverityDistribution();

        // Assert
        assertNotNull(distribution);
        assertTrue(distribution.containsKey("LOW"));
        assertTrue(distribution.containsKey("MEDIUM"));
        assertTrue(distribution.containsKey("HIGH"));
    }

    @Test
    @DisplayName("Test get trending incidents")
    public void testGetTrendingIncidents_ValidPeriod_Success() {
        // Arrange
        when(safetyIncidentRepository.findByIncidentDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(testIncident));

        // Act
        Map<String, Integer> trends = safetyService.getIncidentTrends(12); // Last 12 months

        // Assert
        assertNotNull(trends);
        assertTrue(trends.size() > 0);
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test report incident with maximum description length")
    public void testReportIncident_MaxDescriptionLength_Success() {
        // Arrange
        String maxDescription = "A".repeat(2000);
        incidentDto.setDescription(maxDescription);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        // Act
        SafetyIncidentDto result = safetyService.reportIncident(incidentDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test report incident with special characters in location")
    public void testReportIncident_SpecialCharactersInLocation_Success() {
        // Arrange
        incidentDto.setLocation("Aisle 5-B (Section #3)");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        // Act
        SafetyIncidentDto result = safetyService.reportIncident(incidentDto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test get incidents with empty result")
    public void testGetIncidentsByStatus_EmptyResult_ReturnsEmptyList() {
        // Arrange
        when(safetyIncidentRepository.findByStatus("CLOSED"))
            .thenReturn(Arrays.asList());

        // Act
        List<SafetyIncidentDto> result = safetyService.getIncidentsByStatus("CLOSED");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Test add corrective action to incident")
    public void testAddCorrectiveAction_ValidIncident_Success() {
        // Arrange
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);
        String correctiveAction = "Installed additional safety barriers";

        // Act
        SafetyIncidentDto result = safetyService.addCorrectiveAction(1L, correctiveAction);

        // Assert
        assertNotNull(result);
        assertTrue(result.getCorrectiveActions().contains(correctiveAction));
    }

    @Test
    @DisplayName("Test assign investigator to incident")
    public void testAssignInvestigator_ValidIncident_Success() {
        // Arrange
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(testEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        // Act
        SafetyIncidentDto result = safetyService.assignInvestigator(1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getInvestigatorId());
    }

    @Test
    @DisplayName("Test get open incidents count")
    public void testGetOpenIncidentsCount_ValidData_Success() {
        // Arrange
        when(safetyIncidentRepository.countByStatusIn(Arrays.asList("REPORTED", "INVESTIGATING")))
            .thenReturn(5L);

        // Act
        long count = safetyService.getOpenIncidentsCount();

        // Assert
        assertEquals(5L, count);
    }

    @Test
    @DisplayName("Test get average resolution time")
    public void testGetAverageResolutionTime_ValidData_Success() {
        // Arrange
        testIncident.setStatus("CLOSED");
        testIncident.setResolvedDate(LocalDateTime.now());
        when(safetyIncidentRepository.findByStatus("CLOSED"))
            .thenReturn(Arrays.asList(testIncident));

        // Act
        double avgDays = safetyService.getAverageResolutionTime();

        // Assert
        assertTrue(avgDays >= 0);
    }
}