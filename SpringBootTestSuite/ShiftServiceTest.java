package com.warehouse.ems.service;

import com.warehouse.ems.dto.ShiftTemplateRequestDto;
import com.warehouse.ems.entity.ShiftTemplate;
import com.warehouse.ems.entity.ShiftAssignment;
import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.exception.EntityNotFoundException;
import com.warehouse.ems.repository.ShiftTemplateRepository;
import com.warehouse.ems.repository.EmployeeRepository;
import com.warehouse.ems.repository.ShiftAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ShiftService.
 * Covers normal operation, null/invalid input, conflict detection, and exception scenarios.
 */
@ExtendWith(MockitoExtension.class)
class ShiftServiceTest {

    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;
    @InjectMocks
    private ShiftService shiftService;

    private ShiftTemplate shiftTemplate;
    private ShiftTemplateRequestDto shiftTemplateRequestDto;
    private Employee employee;
    private ShiftAssignment shiftAssignment;

    @BeforeEach
    void setUp() {
        shiftTemplate = new ShiftTemplate();
        shiftTemplate.setId(1L);
        shiftTemplate.setName("Morning Shift");
        shiftTemplate.setStartTime(LocalTime.of(8, 0));
        shiftTemplate.setEndTime(LocalTime.of(16, 0));
        shiftTemplate.setDaysOfWeek("MON,TUE,WED,THU,FRI");
        shiftTemplate.setMaxEmployees(10);

        shiftTemplateRequestDto = new ShiftTemplateRequestDto();
        shiftTemplateRequestDto.setName("Morning Shift");
        shiftTemplateRequestDto.setStartTime(LocalTime.of(8, 0));
        shiftTemplateRequestDto.setEndTime(LocalTime.of(16, 0));
        shiftTemplateRequestDto.setDaysOfWeek("MON,TUE,WED,THU,FRI");
        shiftTemplateRequestDto.setMaxEmployees(10);

        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("BADGE123");

        shiftAssignment = new ShiftAssignment();
        shiftAssignment.setId(1L);
        shiftAssignment.setEmployee(employee);
        shiftAssignment.setShiftTemplate(shiftTemplate);
        shiftAssignment.setDate(LocalDate.now());
    }

    /**
     * Test createShiftTemplate with valid input returns ShiftTemplate.
     */
    @Test
    void testCreateShiftTemplate_ValidInput_ReturnsShiftTemplate() {
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(shiftTemplate);
        ShiftTemplate result = shiftService.createShiftTemplate(shiftTemplateRequestDto);
        assertNotNull(result);
        assertEquals("Morning Shift", result.getName());
    }

    /**
     * Test createShiftTemplate with null DTO throws exception.
     */
    @Test
    void testCreateShiftTemplate_NullDto_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                shiftService.createShiftTemplate(null));
    }

    /**
     * Test assignShift with valid input returns list of ShiftAssignments.
     */
    @Test
    void testAssignShift_ValidInput_ReturnsAssignments() {
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(shiftTemplate));
        when(employeeRepository.findAllById(anyList())).thenReturn(List.of(employee));
        when(shiftAssignmentRepository.saveAll(anyList())).thenReturn(List.of(shiftAssignment));
        List<ShiftAssignment> result = shiftService.assignShift(1L, List.of(1L));
        assertEquals(1, result.size());
    }

    /**
     * Test assignShift with non-existent shift throws EntityNotFoundException.
     */
    @Test
    void testAssignShift_NonExistentShift_ThrowsEntityNotFoundException() {
        when(shiftTemplateRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                shiftService.assignShift(99L, List.of(1L)));
    }

    /**
     * Test detectConflict returns true for conflicting shift.
     */
    @Test
    void testDetectConflict_ConflictExists_ReturnsTrue() {
        when(shiftAssignmentRepository.existsByEmployeeIdAndDateAndTimeRange(
                eq(1L), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(true);
        boolean conflict = shiftService.detectConflict(1L, LocalDate.now(), LocalTime.of(8, 0), LocalTime.of(16, 0));
        assertTrue(conflict);
    }

    /**
     * Test detectConflict returns false when no conflict.
     */
    @Test
    void testDetectConflict_NoConflict_ReturnsFalse() {
        when(shiftAssignmentRepository.existsByEmployeeIdAndDateAndTimeRange(
                eq(1L), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(false);
        boolean conflict = shiftService.detectConflict(1L, LocalDate.now(), LocalTime.of(8, 0), LocalTime.of(16, 0));
        assertFalse(conflict);
    }

    /**
     * Test getEmployeeShifts with valid input returns list.
     */
    @Test
    void testGetEmployeeShifts_ValidInput_ReturnsList() {
        when(shiftAssignmentRepository.findByEmployeeIdAndDateRange(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(shiftAssignment));
        List<ShiftAssignment> result = shiftService.getEmployeeShifts(1L, LocalDate.now(), LocalDate.now().plusDays(5));
        assertEquals(1, result.size());
    }

    /**
     * Test assignShift with empty employeeIds returns empty list.
     */
    @Test
    void testAssignShift_EmptyEmployeeIds_ReturnsEmptyList() {
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(shiftTemplate));
        List<ShiftAssignment> result = shiftService.assignShift(1L, Collections.emptyList());
        assertTrue(result.isEmpty());
    }
}
