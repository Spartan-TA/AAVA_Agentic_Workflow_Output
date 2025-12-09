package com.warehouse.ems.safety.service;

import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.safety.entity.Incident;
import com.warehouse.ems.safety.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for IncidentService.
 * Tests cover incident reporting, severity tracking, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
public class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @InjectMocks
    private IncidentService incidentService;

    private Incident testIncident;
    private Employee testEmployee;

    @BeforeEach
    public void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        testIncident = new Incident();
        testIncident.setId(1L);
        testIncident.setReportedBy(testEmployee);
        testIncident.setSeverity("MEDIUM");
        testIncident.setLocation("Warehouse Floor A");
        testIncident.setDescription("Minor equipment malfunction");
    }

    // ========== GET ALL INCIDENTS TESTS ==========

    @Test
    public void testGetAllIncidents_Success() {
        // Arrange
        Incident incident2 = new Incident();
        incident2.setId(2L);
        incident2.setReportedBy(testEmployee);
        incident2.setSeverity("HIGH");
        incident2.setLocation("Loading Dock");
        incident2.setDescription("Forklift collision");

        List<Incident> incidents = Arrays.asList(testIncident, incident2);
        when(incidentRepository.findAll()).thenReturn(incidents);

        // Act
        List<Incident> result = incidentService.getAllIncidents();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("MEDIUM", result.get(0).getSeverity());
        assertEquals("HIGH", result.get(1).getSeverity());
        verify(incidentRepository, times(1)).findAll();
    }

    @Test
    public void testGetAllIncidents_EmptyList() {
        // Arrange
        when(incidentRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Incident> result = incidentService.getAllIncidents();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(incidentRepository, times(1)).findAll();
    }

    @Test
    public void testGetAllIncidents_SingleIncident() {
        // Arrange
        when(incidentRepository.findAll()).thenReturn(Arrays.asList(testIncident));

        // Act
        List<Incident> result = incidentService.getAllIncidents();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Warehouse Floor A", result.get(0).getLocation());
    }

    // ========== REPORT INCIDENT TESTS ==========

    @Test
    public void testReportIncident_Success() {
        // Arrange
        when(incidentRepository.save(any(Incident.class))).thenReturn(testIncident);

        // Act
        Incident result = incidentService.reportIncident(testIncident);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
        assertEquals("OPEN", result.getStatus());
        assertEquals("MEDIUM", result.getSeverity());
        assertEquals("Warehouse Floor A", result.getLocation());
        verify(incidentRepository, times(1)).save(testIncident);
    }

    @Test
    public void testReportIncident_HighSeverity() {
        // Arrange
        testIncident.setSeverity("HIGH");
        testIncident.setDescription("Serious injury requiring medical attention");
        when(incidentRepository.save(any(Incident.class))).thenReturn(testIncident);

        // Act
        Incident result = incidentService.reportIncident(testIncident);

        // Assert
        assertNotNull(result);
        assertEquals("HIGH", result.getSeverity());
        assertEquals("OPEN", result.getStatus());
        verify(incidentRepository, times(1)).save(testIncident);
    }

    @Test
    public void testReportIncident_LowSeverity() {
        // Arrange
        testIncident.setSeverity("LOW");
        testIncident.setDescription("Near miss - no injury");
        when(incidentRepository.save(any(Incident.class))).thenReturn(testIncident);

        // Act
        Incident result = incidentService.reportIncident(testIncident);

        // Assert
        assertNotNull(result);
        assertEquals("LOW", result.getSeverity());
        assertEquals("OPEN", result.getStatus());
        verify(incidentRepository, times(1)).save(testIncident);
    }

    @Test
    public void testReportIncident_NullReporter() {
        // Arrange
        testIncident.setReportedBy(null);
        when(incidentRepository.save(any(Incident.class))).thenReturn(testIncident);

        // Act
        Incident result = incidentService.reportIncident(testIncident);

        // Assert
        assertNotNull(result);
        assertNull(result.getReportedBy());
        assertEquals("OPEN", result.getStatus());
    }

    @Test
    public void testReportIncident_EmptyDescription() {
        // Arrange
        testIncident.setDescription("");
        when(incidentRepository.save(any(Incident.class))).thenReturn(testIncident);

        // Act
        Incident result = incidentService.reportIncident(testIncident);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getDescription());
        assertEquals("OPEN", result.getStatus());
    }

    @Test
    public void testReportIncident_NullDescription() {
        // Arrange
        testIncident.setDescription(null);
        when(incidentRepository.save(any(Incident.class))).thenReturn(testIncident);

        // Act
        Incident result = incidentService.reportIncident(testIncident);

        // Assert
        assertNotNull(result);
        assertNull(result.getDescription());
        assertEquals("OPEN", result.getStatus());
    }

    @Test
    public void testReportIncident_LongDescription() {
        // Arrange
        String longDescription = "This is a very long description that contains detailed information about the incident. "
            + "It includes multiple sentences and provides comprehensive details about what happened, when it happened, "
            + "who was involved, and what actions were taken immediately following the incident. "
            + "This type of detailed reporting is important for proper incident investigation and prevention.";
        testIncident.setDescription(longDescription);
        when(incidentRepository.save(any(Incident.class))).thenReturn(testIncident);

        // Act
        Incident result = incidentService.reportIncident(testIncident);

        // Assert
        assertNotNull(result);
        assertEquals(longDescription, result.getDescription());
        assertTrue(result.getDescription().length() > 100);
    }

    @Test
    public void testReportIncident_EmptyLocation() {
        // Arrange
        testIncident.setLocation("");
        when(incidentRepository.save(any(Incident.class))).thenReturn(testIncident);

        // Act
        Incident result = incidentService.reportIncident(testIncident);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getLocation());
    }

    @Test
    public void testReportIncident_NullLocation() {
        // Arrange
        testIncident.setLocation(null);
        when(incidentRepository.save(any(Incident.class))).thenReturn(testIncident);

        // Act
        Incident result = incidentService.reportIncident(testIncident);

        // Assert
        assertNotNull(result);
        assertNull(result.getLocation());
    }

    @Test
    public void testReportIncident_SpecialCharactersInDescription() {
        // Arrange
        testIncident.setDescription("Incident with special chars: @#$%^&*()_+-=[]{}|;':,.<>?/");
        when(incidentRepository.save(any(Incident.class))).thenReturn(testIncident);

        // Act
        Incident result = incidentService.reportIncident(testIncident);

        // Assert
        assertNotNull(result);
        assertTrue(result.getDescription().contains("@#$%"));
    }

    // ========== GET INCIDENTS BY SEVERITY TESTS ==========

    @Test
    public void testGetIncidentsBySeverity_High() {
        // Arrange
        Incident highIncident1 = createIncident(1L, "HIGH", "Loading Dock");
        Incident highIncident2 = createIncident(2L, "HIGH", "Warehouse Floor B");
        List<Incident> highIncidents = Arrays.asList(highIncident1, highIncident2);
        when(incidentRepository.findBySeverity("HIGH")).thenReturn(highIncidents);

        // Act
        List<Incident> result = incidentService.getIncidentsBySeverity("HIGH");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("HIGH", result.get(0).getSeverity());
        assertEquals("HIGH", result.get(1).getSeverity());
        verify(incidentRepository, times(1)).findBySeverity("HIGH");
    }

    @Test
    public void testGetIncidentsBySeverity_Medium() {
        // Arrange
        when(incidentRepository.findBySeverity("MEDIUM")).thenReturn(Arrays.asList(testIncident));

        // Act
        List<Incident> result = incidentService.getIncidentsBySeverity("MEDIUM");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MEDIUM", result.get(0).getSeverity());
        verify(incidentRepository, times(1)).findBySeverity("MEDIUM");
    }

    @Test
    public void testGetIncidentsBySeverity_Low() {
        // Arrange
        Incident lowIncident = createIncident(3L, "LOW", "Break Room");
        when(incidentRepository.findBySeverity("LOW")).thenReturn(Arrays.asList(lowIncident));

        // Act
        List<Incident> result = incidentService.getIncidentsBySeverity("LOW");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("LOW", result.get(0).getSeverity());
        verify(incidentRepository, times(1)).findBySeverity("LOW");
    }

    @Test
    public void testGetIncidentsBySeverity_NoMatches() {
        // Arrange
        when(incidentRepository.findBySeverity("CRITICAL")).thenReturn(Arrays.asList());

        // Act
        List<Incident> result = incidentService.getIncidentsBySeverity("CRITICAL");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(incidentRepository, times(1)).findBySeverity("CRITICAL");
    }

    @Test
    public void testGetIncidentsBySeverity_NullSeverity() {
        // Arrange
        when(incidentRepository.findBySeverity(null)).thenReturn(Arrays.asList());

        // Act
        List<Incident> result = incidentService.getIncidentsBySeverity(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetIncidentsBySeverity_EmptySeverity() {
        // Arrange
        when(incidentRepository.findBySeverity("")).thenReturn(Arrays.asList());

        // Act
        List<Incident> result = incidentService.getIncidentsBySeverity("");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetIncidentsBySeverity_CaseInsensitive() {
        // Arrange
        when(incidentRepository.findBySeverity("high")).thenReturn(Arrays.asList());

        // Act
        List<Incident> result = incidentService.getIncidentsBySeverity("high");

        // Assert
        assertNotNull(result);
        verify(incidentRepository, times(1)).findBySeverity("high");
    }

    @Test
    public void testGetIncidentsBySeverity_MultipleIncidents() {
        // Arrange
        List<Incident> incidents = Arrays.asList(
            createIncident(1L, "HIGH", "Location 1"),
            createIncident(2L, "HIGH", "Location 2"),
            createIncident(3L, "HIGH", "Location 3"),
            createIncident(4L, "HIGH", "Location 4"),
            createIncident(5L, "HIGH", "Location 5")
        );
        when(incidentRepository.findBySeverity("HIGH")).thenReturn(incidents);

        // Act
        List<Incident> result = incidentService.getIncidentsBySeverity("HIGH");

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
        result.forEach(incident -> assertEquals("HIGH", incident.getSeverity()));
    }

    // ========== HELPER METHODS ==========

    private Incident createIncident(Long id, String severity, String location) {
        Incident incident = new Incident();
        incident.setId(id);
        incident.setReportedBy(testEmployee);
        incident.setSeverity(severity);
        incident.setLocation(location);
        incident.setDescription("Test incident description");
        incident.setStatus("OPEN");
        incident.setCreatedAt(LocalDateTime.now());
        return incident;
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    public void testReportIncident_MultipleIncidentsSameEmployee() {
        // Arrange
        Incident incident2 = new Incident();
        incident2.setReportedBy(testEmployee);
        incident2.setSeverity("LOW");
        incident2.setLocation("Different Location");
        incident2.setDescription("Another incident");

        when(incidentRepository.save(any(Incident.class)))
            .thenReturn(testIncident)
            .thenReturn(incident2);

        // Act
        Incident result1 = incidentService.reportIncident(testIncident);
        Incident result2 = incidentService.reportIncident(incident2);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getReportedBy().getId(), result2.getReportedBy().getId());
        verify(incidentRepository, times(2)).save(any(Incident.class));
    }

    @Test
    public void testReportIncident_TimestampVerification() {
        // Arrange
        LocalDateTime beforeReport = LocalDateTime.now();
        when(incidentRepository.save(any(Incident.class))).thenReturn(testIncident);

        // Act
        Incident result = incidentService.reportIncident(testIncident);
        LocalDateTime afterReport = LocalDateTime.now();

        // Assert
        assertNotNull(result.getCreatedAt());
        assertTrue(result.getCreatedAt().isAfter(beforeReport.minusSeconds(1)));
        assertTrue(result.getCreatedAt().isBefore(afterReport.plusSeconds(1)));
    }

    @Test
    public void testReportIncident_StatusAlwaysOpen() {
        // Arrange
        testIncident.setStatus("CLOSED"); // Try to set different status
        when(incidentRepository.save(any(Incident.class))).thenReturn(testIncident);

        // Act
        Incident result = incidentService.reportIncident(testIncident);

        // Assert
        assertEquals("OPEN", result.getStatus()); // Should be overridden to OPEN
    }
}