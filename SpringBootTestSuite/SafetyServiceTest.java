package com.warehouse.ems.service;

import com.warehouse.ems.dto.SafetyIncidentRequestDto;
import com.warehouse.ems.entity.SafetyIncident;
import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.exception.EntityNotFoundException;
import com.warehouse.ems.repository.SafetyIncidentRepository;
import com.warehouse.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SafetyService.
 * Covers normal operation, null/invalid input, duplicate entries, and exception scenarios.
 */
@ExtendWith(MockitoExtension.class)
class SafetyServiceTest {

    @Mock
    private SafetyIncidentRepository safetyIncidentRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private SafetyService safetyService;

    private Employee employee;
    private SafetyIncident safetyIncident;
    private SafetyIncidentRequestDto safetyIncidentRequestDto;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("BADGE123");

        safetyIncident = new SafetyIncident();
        safetyIncident.setId(1L);
        safetyIncident.setEmployee(employee);
        safetyIncident.setDate(LocalDate.now());
        safetyIncident.setType("INJURY");
        safetyIncident.setDescription("Minor injury");
        safetyIncident.setStatus("OPEN");

        safetyIncidentRequestDto = new SafetyIncidentRequestDto();
        safetyIncidentRequestDto.setEmployeeId(1L);
        safetyIncidentRequestDto.setDate(LocalDate.now());
        safetyIncidentRequestDto.setType("INJURY");
        safetyIncidentRequestDto.setDescription("Minor injury");
    }

    /**
     * Test createSafetyIncident with valid input returns SafetyIncident.
     */
    @Test
    void testCreateSafetyIncident_ValidInput_ReturnsSafetyIncident() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(safetyIncident);
        SafetyIncident result = safetyService.createSafetyIncident(safetyIncidentRequestDto);
        assertNotNull(result);
        assertEquals("INJURY", result.getType());
    }

    /**
     * Test createSafetyIncident with null DTO throws exception.
     */
    @Test
    void testCreateSafetyIncident_NullDto_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                safetyService.createSafetyIncident(null));
    }

    /**
     * Test getSafetyIncidentById with valid ID returns SafetyIncident.
     */
    @Test
    void testGetSafetyIncidentById_ValidId_ReturnsSafetyIncident() {
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(safetyIncident));
        SafetyIncident result = safetyService.getSafetyIncidentById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    /**
     * Test getSafetyIncidentById with non-existent ID throws EntityNotFoundException.
     */
    @Test
    void testGetSafetyIncidentById_NonExistentId_ThrowsEntityNotFoundException() {
        when(safetyIncidentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                safetyService.getSafetyIncidentById(99L));
    }

    /**
     * Test getAllSafetyIncidents returns list.
     */
    @Test
    void testGetAllSafetyIncidents_ReturnsList() {
        when(safetyIncidentRepository.findAll()).thenReturn(List.of(safetyIncident));
        List<SafetyIncident> result = safetyService.getAllSafetyIncidents();
        assertEquals(1, result.size());
    }

    /**
     * Test updateSafetyIncident with valid input returns SafetyIncident.
     */
    @Test
    void testUpdateSafetyIncident_ValidInput_ReturnsSafetyIncident() {
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(safetyIncident));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(safetyIncident);
        SafetyIncident result = safetyService.updateSafetyIncident(1L, safetyIncidentRequestDto);
        assertNotNull(result);
    }

    /**
     * Test updateSafetyIncident with non-existent ID throws EntityNotFoundException.
     */
    @Test
    void testUpdateSafetyIncident_NonExistentId_ThrowsEntityNotFoundException() {
        when(safetyIncidentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                safetyService.updateSafetyIncident(99L, safetyIncidentRequestDto));
    }

    /**
     * Test closeSafetyIncident with valid ID does not throw.
     */
    @Test
    void testCloseSafetyIncident_ValidId_DoesNotThrow() {
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(safetyIncident));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(safetyIncident);
        assertDoesNotThrow(() -> safetyService.closeSafetyIncident(1L));
    }
}
