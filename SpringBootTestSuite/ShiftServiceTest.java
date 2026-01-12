package com.warehouse.ems.service;

import com.warehouse.ems.entity.ShiftTemplate;
import com.warehouse.ems.entity.ShiftAssignment;
import com.warehouse.ems.repository.ShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftServiceTest {

    @Mock
    private ShiftRepository shiftRepository;

    @InjectMocks
    private ShiftService shiftService;

    private ShiftTemplate template;
    private ShiftAssignment assignment;

    @BeforeEach
    void setUp() {
        template = new ShiftTemplate(1L, "Morning", "08:00", "16:00");
        assignment = new ShiftAssignment(1L, 1L, 1L, LocalDate.now());
    }

    @Test
    void testCreateShiftTemplate_ValidInput_ReturnsTemplate() {
        when(shiftRepository.saveTemplate(any(ShiftTemplate.class))).thenReturn(template);

        ShiftTemplate result = shiftService.createShiftTemplate(template);

        assertNotNull(result);
        assertEquals("Morning", result.getName());
    }

    @Test
    void testCreateShiftTemplate_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> shiftService.createShiftTemplate(null));
    }

    @Test
    void testAssignShift_ValidInput_ReturnsAssignment() {
        when(shiftRepository.saveAssignment(any(ShiftAssignment.class))).thenReturn(assignment);

        ShiftAssignment result = shiftService.assignShift(assignment);

        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
    }

    @Test
    void testAssignShift_Conflict_ThrowsException() {
        when(shiftRepository.hasConflict(any(ShiftAssignment.class))).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> shiftService.assignShift(assignment));
    }

    @Test
    void testGetAssignmentsByEmployeeId_ReturnsList() {
        List<ShiftAssignment> assignments = Arrays.asList(assignment);
        when(shiftRepository.findAssignmentsByEmployeeId(1L)).thenReturn(assignments);

        List<ShiftAssignment> result = shiftService.getAssignmentsByEmployeeId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void testGetAssignmentsByEmployeeId_EmptyList() {
        when(shiftRepository.findAssignmentsByEmployeeId(2L)).thenReturn(Collections.emptyList());

        List<ShiftAssignment> result = shiftService.getAssignmentsByEmployeeId(2L);

        assertTrue(result.isEmpty());
    }
}