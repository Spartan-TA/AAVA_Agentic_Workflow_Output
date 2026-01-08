package com.example.warehouse.service;

import com.example.warehouse.entity.Employee;
import com.example.warehouse.entity.SafetyIncident;
import com.example.warehouse.repository.EmployeeRepository;
import com.example.warehouse.repository.SafetyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for SafetyService.
 * 
 * Tests cover:
 * - Safety incident reporting and retrieval
 * - Resolution workflow
 * - Filtering by resolution status
 * - Normal cases, boundary conditions, and edge cases
 * - Exception handling for non-existent incidents and employees
 * 
 * @author Warehouse Test Team
 */
@ExtendWith(MockitoExtension.class)
public class SafetyServiceTest {

    @Mock
    private SafetyRepository safetyRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private SafetyService safetyService;

    private Employee testEmployee;
    private SafetyIncident unresolvedIncident;
    private SafetyIncident resolvedIncident;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    public void setUp() {
        testEmployee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@warehouse.com")
                .position("Warehouse Associate")
                .hireDate(LocalDate.of(2024, 1, 15))
                .active(true)
                .build();

        unresolvedIncident = SafetyIncident.builder()
                .id(1L)
                .reportedBy(testEmployee)
                .description("Near-miss incident in loading dock area")
                .incidentTime(LocalDateTime.now())
                .resolved(false)
                .build();

        resolvedIncident = SafetyIncident.builder()
                .id(2L)
                .reportedBy(testEmployee)
                .description("Spill cleaned up in aisle 5")
                .incidentTime(LocalDateTime.now().minusDays(1))
                .resolved(true)
                .build();
    }

    // ==================== GET ALL INCIDENTS TESTS ====================

    /**
     * Test getAllIncidents with multiple incidents - Normal case.
     */
    @Test
    public void testGetAllIncidents_WithMultipleIncidents_Success() {
        // Arrange
        List<SafetyIncident> incidents = Arrays.asList(unresolvedIncident, resolvedIncident);
        when(safetyRepository.findAll()).thenReturn(incidents);

        // Act
        List<SafetyIncident> result = safetyService.getAllIncidents();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertFalse(result.get(0).isResolved());
        assertTrue(result.get(1).isResolved());
        verify(safetyRepository, times(1)).findAll();
    }

    /**
     * Test getAllIncidents with empty list - Boundary condition.
     */
    @Test
    public void testGetAllIncidents_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(safetyRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<SafetyIncident> result = safetyService.getAllIncidents();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(safetyRepository, times(1)).findAll();
    }

    /**
     * Test getAllIncidents with single incident - Edge case.
     */
    @Test
    public void testGetAllIncidents_SingleIncident_Success() {
        // Arrange
        when(safetyRepository.findAll()).thenReturn(Collections.singletonList(unresolvedIncident));

        // Act
        List<SafetyIncident> result = safetyService.getAllIncidents();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isResolved());
        verify(safetyRepository, times(1)).findAll();
    }

    // ==================== GET INCIDENTS BY RESOLVED STATUS TESTS ====================

    /**
     * Test getIncidentsByResolved for unresolved incidents - Normal case.
     */
    @Test
    public void testGetIncidentsByResolved_UnresolvedIncidents_Success() {
        // Arrange
        when(safetyRepository.findByResolved(false)).thenReturn(Collections.singletonList(unresolvedIncident));

        // Act
        List<SafetyIncident> result = safetyService.getIncidentsByResolved(false);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isResolved());
        verify(safetyRepository, times(1)).findByResolved(false);
    }

    /**
     * Test getIncidentsByResolved for resolved incidents - Normal case.
     */
    @Test
    public void testGetIncidentsByResolved_ResolvedIncidents_Success() {
        // Arrange
        when(safetyRepository.findByResolved(true)).thenReturn(Collections.singletonList(resolvedIncident));

        // Act
        List<SafetyIncident> result = safetyService.getIncidentsByResolved(true);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isResolved());
        verify(safetyRepository, times(1)).findByResolved(true);
    }

    /**
     * Test getIncidentsByResolved with no matching incidents - Boundary condition.
     */
    @Test
    public void testGetIncidentsByResolved_NoMatchingIncidents_ReturnsEmptyList() {
        // Arrange
        when(safetyRepository.findByResolved(anyBoolean())).thenReturn(Collections.emptyList());

        // Act
        List<SafetyIncident> result = safetyService.getIncidentsByResolved(true);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(safetyRepository, times(1)).findByResolved(true);
    }

    /**
     * Test getIncidentsByResolved with multiple resolved incidents - Edge case.
     */
    @Test
    public void testGetIncidentsByResolved_MultipleResolvedIncidents_Success() {
        // Arrange
        SafetyIncident incident2 = SafetyIncident.builder()
                .id(3L)
                .reportedBy(testEmployee)
                .description("Equipment malfunction resolved")
                .incidentTime(LocalDateTime.now().minusDays(2))
                .resolved(true)
                .build();
        List<SafetyIncident> resolvedIncidents = Arrays.asList(resolvedIncident, incident2);
        when(safetyRepository.findByResolved(true)).thenReturn(resolvedIncidents);

        // Act
        List<SafetyIncident> result = safetyService.getIncidentsByResolved(true);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(SafetyIncident::isResolved));
        verify(safetyRepository, times(1)).findByResolved(true);
    }

    // ==================== GET INCIDENT BY ID TESTS ====================

    /**
     * Test getIncidentById with valid ID - Normal case.
     */
    @Test
    public void testGetIncidentById_ValidId_Success() {
        // Arrange
        when(safetyRepository.findById(1L)).thenReturn(Optional.of(unresolvedIncident));

        // Act
        SafetyIncident result = safetyService.getIncidentById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Near-miss incident in loading dock area", result.getDescription());
        assertFalse(result.isResolved());
        verify(safetyRepository, times(1)).findById(1L);
    }

    /**
     * Test getIncidentById with non-existent ID - Edge case.
     */
    @Test
    public void testGetIncidentById_NonExistentId_ThrowsException() {
        // Arrange
        when(safetyRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            safetyService.getIncidentById(999L);
        });
        assertEquals("Incident not found", exception.getMessage());
        verify(safetyRepository, times(1)).findById(999L);
    }

    /**
     * Test getIncidentById with null ID - Boundary condition.
     */
    @Test
    public void testGetIncidentById_NullId_ThrowsException() {
        // Arrange
        when(safetyRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            safetyService.getIncidentById(null);
        });
    }

    /**
     * Test getIncidentById with negative ID - Edge case.
     */
    @Test
    public void testGetIncidentById_NegativeId_ThrowsException() {
        // Arrange
        when(safetyRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            safetyService.getIncidentById(-1L);
        });
    }

    // ==================== REPORT INCIDENT TESTS ====================

    /**
     * Test reportIncident with valid data - Normal case.
     */
    @Test
    public void testReportIncident_ValidData_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(safetyRepository.save(any(SafetyIncident.class))).thenReturn(unresolvedIncident);

        // Act
        SafetyIncident result = safetyService.reportIncident(1L, "Near-miss incident in loading dock area");

        // Assert
        assertNotNull(result);
        assertEquals(testEmployee, result.getReportedBy());
        assertEquals("Near-miss incident in loading dock area", result.getDescription());
        assertFalse(result.isResolved());
        assertNotNull(result.getIncidentTime());
        verify(employeeRepository, times(1)).findById(1L);
        verify(safetyRepository, times(1)).save(any(SafetyIncident.class));
    }

    /**
     * Test reportIncident with non-existent employee - Edge case.
     */
    @Test
    public void testReportIncident_NonExistentEmployee_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            safetyService.reportIncident(999L, "Test incident");
        });
        assertEquals("Employee not found", exception.getMessage());
        verify(employeeRepository, times(1)).findById(999L);
        verify(safetyRepository, never()).save(any(SafetyIncident.class));
    }

    /**
     * Test reportIncident with null employee ID - Boundary condition.
     */
    @Test
    public void testReportIncident_NullEmployeeId_ThrowsException() {
        // Arrange
        when(employeeRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            safetyService.reportIncident(null, "Test incident");
        });
        verify(safetyRepository, never()).save(any(SafetyIncident.class));
    }

    /**
     * Test reportIncident with empty description - Boundary condition.
     */
    @Test
    public void testReportIncident_EmptyDescription_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        SafetyIncident incidentWithEmptyDesc = SafetyIncident.builder()
                .id(1L)
                .reportedBy(testEmployee)
                .description("")
                .incidentTime(LocalDateTime.now())
                .resolved(false)
                .build();
        when(safetyRepository.save(any(SafetyIncident.class))).thenReturn(incidentWithEmptyDesc);

        // Act
        SafetyIncident result = safetyService.reportIncident(1L, "");

        // Assert
        assertNotNull(result);
        assertEquals("", result.getDescription());
        verify(safetyRepository, times(1)).save(any(SafetyIncident.class));
    }

    /**
     * Test reportIncident with null description - Boundary condition.
     */
    @Test
    public void testReportIncident_NullDescription_Success() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        SafetyIncident incidentWithNullDesc = SafetyIncident.builder()
                .id(1L)
                .reportedBy(testEmployee)
                .description(null)
                .incidentTime(LocalDateTime.now())
                .resolved(false)
                .build();
        when(safetyRepository.save(any(SafetyIncident.class))).thenReturn(incidentWithNullDesc);

        // Act
        SafetyIncident result = safetyService.reportIncident(1L, null);

        // Assert
        assertNotNull(result);
        assertNull(result.getDescription());
        verify(safetyRepository, times(1)).save(any(SafetyIncident.class));
    }

    /**
     * Test reportIncident with very long description - Edge case.
     */
    @Test
    public void testReportIncident_LongDescription_Success() {
        // Arrange
        String longDescription = "A".repeat(1000);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        SafetyIncident incidentWithLongDesc = SafetyIncident.builder()
                .id(1L)
                .reportedBy(testEmployee)
                .description(longDescription)
                .incidentTime(LocalDateTime.now())
                .resolved(false)
                .build();
        when(safetyRepository.save(any(SafetyIncident.class))).thenReturn(incidentWithLongDesc);

        // Act
        SafetyIncident result = safetyService.reportIncident(1L, longDescription);

        // Assert
        assertNotNull(result);
        assertEquals(1000, result.getDescription().length());
        verify(safetyRepository, times(1)).save(any(SafetyIncident.class));
    }

    // ==================== RESOLVE INCIDENT TESTS ====================

    /**
     * Test resolveIncident with valid ID - Normal case.
     */
    @Test
    public void testResolveIncident_ValidId_Success() {
        // Arrange
        when(safetyRepository.findById(1L)).thenReturn(Optional.of(unresolvedIncident));
        SafetyIncident resolvedVersion = SafetyIncident.builder()
                .id(1L)
                .reportedBy(testEmployee)
                .description("Near-miss incident in loading dock area")
                .incidentTime(unresolvedIncident.getIncidentTime())
                .resolved(true)
                .build();
        when(safetyRepository.save(any(SafetyIncident.class))).thenReturn(resolvedVersion);

        // Act
        SafetyIncident result = safetyService.resolveIncident(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isResolved());
        verify(safetyRepository, times(1)).findById(1L);
        verify(safetyRepository, times(1)).save(any(SafetyIncident.class));
    }

    /**
     * Test resolveIncident with non-existent ID - Edge case.
     */
    @Test
    public void testResolveIncident_NonExistentId_ThrowsException() {
        // Arrange
        when(safetyRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            safetyService.resolveIncident(999L);
        });
        assertEquals("Incident not found", exception.getMessage());
        verify(safetyRepository, times(1)).findById(999L);
        verify(safetyRepository, never()).save(any(SafetyIncident.class));
    }

    /**
     * Test resolveIncident with already resolved incident - Edge case.
     */
    @Test
    public void testResolveIncident_AlreadyResolved_Success() {
        // Arrange
        when(safetyRepository.findById(2L)).thenReturn(Optional.of(resolvedIncident));
        when(safetyRepository.save(any(SafetyIncident.class))).thenReturn(resolvedIncident);

        // Act
        SafetyIncident result = safetyService.resolveIncident(2L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isResolved());
        verify(safetyRepository, times(1)).save(any(SafetyIncident.class));
    }

    /**
     * Test resolveIncident with null ID - Boundary condition.
     */
    @Test
    public void testResolveIncident_NullId_ThrowsException() {
        // Arrange
        when(safetyRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            safetyService.resolveIncident(null);
        });
        verify(safetyRepository, never()).save(any(SafetyIncident.class));
    }

    /**
     * Test resolveIncident with negative ID - Edge case.
     */
    @Test
    public void testResolveIncident_NegativeId_ThrowsException() {
        // Arrange
        when(safetyRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            safetyService.resolveIncident(-1L);
        });
        verify(safetyRepository, never()).save(any(SafetyIncident.class));
    }

    /**
     * Test reportIncident by inactive employee - Edge case.
     */
    @Test
    public void testReportIncident_InactiveEmployee_Success() {
        // Arrange
        testEmployee.setActive(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(safetyRepository.save(any(SafetyIncident.class))).thenReturn(unresolvedIncident);

        // Act
        SafetyIncident result = safetyService.reportIncident(1L, "Test incident");

        // Assert
        assertNotNull(result);
        assertFalse(result.getReportedBy().isActive());
        verify(safetyRepository, times(1)).save(any(SafetyIncident.class));
    }
}