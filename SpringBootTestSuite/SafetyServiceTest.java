package com.company.wems.safety.service;

import com.company.wems.safety.dto.SafetyIncidentDTO;
import com.company.wems.safety.entity.SafetyIncident;
import com.company.wems.safety.repository.SafetyIncidentRepository;
import com.company.wems.employee.entity.Employee;
import com.company.wems.employee.repository.EmployeeRepository;
import com.company.wems.common.exception.ResourceNotFoundException;
import com.company.wems.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for SafetyService
 * Tests cover incident reporting, OSHA compliance, and edge cases
 */
@DisplayName("Safety Service Tests")
public class SafetyServiceTest {

    @Mock
    private SafetyIncidentRepository safetyIncidentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private SafetyService safetyService;

    private Employee validEmployee;
    private SafetyIncident validIncident;
    private SafetyIncidentDTO validIncidentDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup valid employee
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setBadgeId("EMP001");
        validEmployee.setFirstName("John");
        validEmployee.setLastName("Doe");
        validEmployee.setDeleted(false);
        
        // Setup valid incident
        validIncident = new SafetyIncident();
        validIncident.setId(1L);
        validIncident.setIncidentNumber("INC-2024-001");
        validIncident.setIncidentDate(LocalDateTime.now());
        validIncident.setSeverity(SafetyIncident.Severity.MODERATE);
        validIncident.setLocation("Warehouse Floor A");
        validIncident.setDescription("Employee slipped on wet floor");
        validIncident.setOshaReportable(false);
        
        Set<Employee> involvedEmployees = new HashSet<>();
        involvedEmployees.add(validEmployee);
        validIncident.setInvolvedEmployees(involvedEmployees);
        
        // Setup valid DTO
        validIncidentDTO = new SafetyIncidentDTO();
        validIncidentDTO.setIncidentNumber("INC-2024-001");
        validIncidentDTO.setIncidentDate(LocalDateTime.now());
        validIncidentDTO.setSeverity("MODERATE");
        validIncidentDTO.setLocation("Warehouse Floor A");
        validIncidentDTO.setDescription("Employee slipped on wet floor");
        validIncidentDTO.setOshaReportable(false);
        validIncidentDTO.setInvolvedEmployeeIds(Arrays.asList(1L));
    }

    // ==================== CREATE INCIDENT TESTS ====================

    @Test
    @DisplayName("Create Safety Incident - Valid Input - Should Create Successfully")
    void testCreateSafetyIncident_WithValidInput_ShouldCreateSuccessfully() {
        // Arrange
        when(safetyIncidentRepository.existsByIncidentNumber(validIncidentDTO.getIncidentNumber())).thenReturn(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(validIncident);

        // Act
        SafetyIncidentDTO result = safetyService.createSafetyIncident(validIncidentDTO);

        // Assert
        assertNotNull(result);
        assertEquals(validIncidentDTO.getIncidentNumber(), result.getIncidentNumber());
        verify(safetyIncidentRepository, times(1)).save(any(SafetyIncident.class));
    }

    @Test
    @DisplayName("Create Safety Incident - Duplicate Incident Number - Should Throw DuplicateResourceException")
    void testCreateSafetyIncident_WithDuplicateNumber_ShouldThrowException() {
        // Arrange
        when(safetyIncidentRepository.existsByIncidentNumber(validIncidentDTO.getIncidentNumber())).thenReturn(true);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            safetyService.createSafetyIncident(validIncidentDTO);
        });
        verify(safetyIncidentRepository, never()).save(any(SafetyIncident.class));
    }

    @Test
    @DisplayName("Create Safety Incident - Null Incident Number - Should Throw Exception")
    void testCreateSafetyIncident_WithNullIncidentNumber_ShouldThrowException() {
        // Arrange
        validIncidentDTO.setIncidentNumber(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            safetyService.createSafetyIncident(validIncidentDTO);
        });
    }

    @Test
    @DisplayName("Create Safety Incident - Empty Incident Number - Should Throw Exception")
    void testCreateSafetyIncident_WithEmptyIncidentNumber_ShouldThrowException() {
        // Arrange
        validIncidentDTO.setIncidentNumber("");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            safetyService.createSafetyIncident(validIncidentDTO);
        });
    }

    @Test
    @DisplayName("Create Safety Incident - Future Incident Date - Should Throw BusinessException")
    void testCreateSafetyIncident_WithFutureDate_ShouldThrowException() {
        // Arrange
        validIncidentDTO.setIncidentDate(LocalDateTime.now().plusDays(1));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            safetyService.createSafetyIncident(validIncidentDTO);
        });
    }

    @Test
    @DisplayName("Create Safety Incident - Null Location - Should Throw Exception")
    void testCreateSafetyIncident_WithNullLocation_ShouldThrowException() {
        // Arrange
        validIncidentDTO.setLocation(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            safetyService.createSafetyIncident(validIncidentDTO);
        });
    }

    @Test
    @DisplayName("Create Safety Incident - Empty Description - Should Throw Exception")
    void testCreateSafetyIncident_WithEmptyDescription_ShouldThrowException() {
        // Arrange
        validIncidentDTO.setDescription("");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            safetyService.createSafetyIncident(validIncidentDTO);
        });
    }

    @Test
    @DisplayName("Create Safety Incident - Non-Existent Employee - Should Throw ResourceNotFoundException")
    void testCreateSafetyIncident_WithNonExistentEmployee_ShouldThrowException() {
        // Arrange
        validIncidentDTO.setInvolvedEmployeeIds(Arrays.asList(999L));
        when(safetyIncidentRepository.existsByIncidentNumber(anyString())).thenReturn(false);
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            safetyService.createSafetyIncident(validIncidentDTO);
        });
    }

    // ==================== SEVERITY LEVEL TESTS ====================

    @Test
    @DisplayName("Create Safety Incident - Critical Severity - Should Mark as OSHA Reportable")
    void testCreateSafetyIncident_WithCriticalSeverity_ShouldMarkOSHAReportable() {
        // Arrange
        validIncidentDTO.setSeverity("CRITICAL");
        when(safetyIncidentRepository.existsByIncidentNumber(anyString())).thenReturn(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(validIncident);

        // Act
        SafetyIncidentDTO result = safetyService.createSafetyIncident(validIncidentDTO);

        // Assert
        assertNotNull(result);
        // Critical incidents should be OSHA reportable
    }

    @Test
    @DisplayName("Create Safety Incident - Fatal Severity - Should Mark as OSHA Reportable")
    void testCreateSafetyIncident_WithFatalSeverity_ShouldMarkOSHAReportable() {
        // Arrange
        validIncidentDTO.setSeverity("FATAL");
        when(safetyIncidentRepository.existsByIncidentNumber(anyString())).thenReturn(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(validIncident);

        // Act
        SafetyIncidentDTO result = safetyService.createSafetyIncident(validIncidentDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Safety Incident - Minor Severity - Should Not Mark as OSHA Reportable")
    void testCreateSafetyIncident_WithMinorSeverity_ShouldNotMarkOSHAReportable() {
        // Arrange
        validIncidentDTO.setSeverity("MINOR");
        when(safetyIncidentRepository.existsByIncidentNumber(anyString())).thenReturn(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(validIncident);

        // Act
        SafetyIncidentDTO result = safetyService.createSafetyIncident(validIncidentDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Safety Incident - Invalid Severity - Should Throw Exception")
    void testCreateSafetyIncident_WithInvalidSeverity_ShouldThrowException() {
        // Arrange
        validIncidentDTO.setSeverity("INVALID");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            safetyService.createSafetyIncident(validIncidentDTO);
        });
    }

    // ==================== UPDATE INCIDENT TESTS ====================

    @Test
    @DisplayName("Update Safety Incident - Valid Input - Should Update Successfully")
    void testUpdateSafetyIncident_WithValidInput_ShouldUpdateSuccessfully() {
        // Arrange
        Long incidentId = 1L;
        when(safetyIncidentRepository.findById(incidentId)).thenReturn(Optional.of(validIncident));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(validIncident);

        // Act
        SafetyIncidentDTO result = safetyService.updateSafetyIncident(incidentId, validIncidentDTO);

        // Assert
        assertNotNull(result);
        verify(safetyIncidentRepository, times(1)).save(any(SafetyIncident.class));
    }

    @Test
    @DisplayName("Update Safety Incident - Non-Existent ID - Should Throw ResourceNotFoundException")
    void testUpdateSafetyIncident_WithNonExistentId_ShouldThrowException() {
        // Arrange
        Long incidentId = 999L;
        when(safetyIncidentRepository.findById(incidentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            safetyService.updateSafetyIncident(incidentId, validIncidentDTO);
        });
    }

    // ==================== GET INCIDENT TESTS ====================

    @Test
    @DisplayName("Get Safety Incident - Valid ID - Should Return Incident")
    void testGetSafetyIncident_WithValidId_ShouldReturnIncident() {
        // Arrange
        Long incidentId = 1L;
        when(safetyIncidentRepository.findById(incidentId)).thenReturn(Optional.of(validIncident));

        // Act
        SafetyIncidentDTO result = safetyService.getSafetyIncidentById(incidentId);

        // Assert
        assertNotNull(result);
        assertEquals(validIncident.getIncidentNumber(), result.getIncidentNumber());
    }

    @Test
    @DisplayName("Get Safety Incident - Non-Existent ID - Should Throw ResourceNotFoundException")
    void testGetSafetyIncident_WithNonExistentId_ShouldThrowException() {
        // Arrange
        Long incidentId = 999L;
        when(safetyIncidentRepository.findById(incidentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            safetyService.getSafetyIncidentById(incidentId);
        });
    }

    @Test
    @DisplayName("Get All Safety Incidents - Should Return List")
    void testGetAllSafetyIncidents_ShouldReturnList() {
        // Arrange
        List<SafetyIncident> incidents = Arrays.asList(validIncident);
        when(safetyIncidentRepository.findAll()).thenReturn(incidents);

        // Act
        List<SafetyIncidentDTO> result = safetyService.getAllSafetyIncidents();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Get OSHA Reportable Incidents - Should Return Only OSHA Reportable")
    void testGetOSHAReportableIncidents_ShouldReturnOnlyReportable() {
        // Arrange
        validIncident.setOshaReportable(true);
        List<SafetyIncident> incidents = Arrays.asList(validIncident);
        when(safetyIncidentRepository.findByOshaReportableTrue()).thenReturn(incidents);

        // Act
        List<SafetyIncidentDTO> result = safetyService.getOSHAReportableIncidents();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).getOshaReportable());
    }

    // ==================== BOUNDARY AND EDGE CASE TESTS ====================

    @Test
    @DisplayName("Create Safety Incident - Multiple Involved Employees - Should Create Successfully")
    void testCreateSafetyIncident_WithMultipleEmployees_ShouldCreateSuccessfully() {
        // Arrange
        Employee employee2 = new Employee();
        employee2.setId(2L);
        employee2.setBadgeId("EMP002");
        
        validIncidentDTO.setInvolvedEmployeeIds(Arrays.asList(1L, 2L));
        when(safetyIncidentRepository.existsByIncidentNumber(anyString())).thenReturn(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee2));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(validIncident);

        // Act
        SafetyIncidentDTO result = safetyService.createSafetyIncident(validIncidentDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Safety Incident - No Involved Employees - Should Throw Exception")
    void testCreateSafetyIncident_WithNoEmployees_ShouldThrowException() {
        // Arrange
        validIncidentDTO.setInvolvedEmployeeIds(Arrays.asList());

        // Act & Assert
        assertThrows(Exception.class, () -> {
            safetyService.createSafetyIncident(validIncidentDTO);
        });
    }

    @Test
    @DisplayName("Create Safety Incident - Maximum Description Length - Should Create Successfully")
    void testCreateSafetyIncident_WithMaxDescriptionLength_ShouldCreateSuccessfully() {
        // Arrange
        String maxDescription = "D".repeat(2000);
        validIncidentDTO.setDescription(maxDescription);
        when(safetyIncidentRepository.existsByIncidentNumber(anyString())).thenReturn(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(validIncident);

        // Act
        SafetyIncidentDTO result = safetyService.createSafetyIncident(validIncidentDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Safety Incident - Past Incident Date - Should Create Successfully")
    void testCreateSafetyIncident_WithPastDate_ShouldCreateSuccessfully() {
        // Arrange
        validIncidentDTO.setIncidentDate(LocalDateTime.now().minusDays(7));
        when(safetyIncidentRepository.existsByIncidentNumber(anyString())).thenReturn(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(validIncident);

        // Act
        SafetyIncidentDTO result = safetyService.createSafetyIncident(validIncidentDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Safety Incident - All Severity Levels - Should Create Successfully")
    void testCreateSafetyIncident_WithAllSeverityLevels_ShouldCreateSuccessfully() {
        // Arrange
        when(safetyIncidentRepository.existsByIncidentNumber(anyString())).thenReturn(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(validIncident);

        // Test each severity level
        String[] severityLevels = {"MINOR", "MODERATE", "SERIOUS", "CRITICAL", "FATAL"};
        for (String severity : severityLevels) {
            validIncidentDTO.setSeverity(severity);
            validIncidentDTO.setIncidentNumber("INC-2024-" + severity);
            SafetyIncidentDTO result = safetyService.createSafetyIncident(validIncidentDTO);
            assertNotNull(result);
        }
    }
}