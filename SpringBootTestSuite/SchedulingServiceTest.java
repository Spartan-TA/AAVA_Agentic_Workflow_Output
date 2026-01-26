package com.company.warehouse.scheduling.service;

import com.company.warehouse.scheduling.domain.*;
import com.company.warehouse.scheduling.dto.*;
import com.company.warehouse.scheduling.repository.*;
import com.company.warehouse.employee.domain.Employee;
import com.company.warehouse.employee.repository.EmployeeRepository;
import com.company.warehouse.common.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Scheduling Service Tests")
public class SchedulingServiceTest {
    @Mock private ShiftTemplateRepository shiftTemplateRepository;
    @Mock private ShiftAssignmentRepository shiftAssignmentRepository;
    @Mock private EmployeeRepository employeeRepository;
    @InjectMocks private SchedulingService schedulingService;
    private ShiftTemplate dayShift;
    private Employee testEmployee;
    private ShiftAssignment assignment;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        dayShift = new ShiftTemplate();
        dayShift.setId(1L);
        dayShift.setName("Day Shift");
        dayShift.setStartTime(LocalTime.of(9, 0));
        dayShift.setEndTime(LocalTime.of(17, 0));
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        assignment = new ShiftAssignment();
        assignment.setId(1L);
        assignment.setEmployee(testEmployee);
        assignment.setShiftTemplate(dayShift);
        assignment.setDate(LocalDate.now());
    }

    @Test
    @DisplayName("Test createShiftTemplate with valid data")
    public void testCreateShiftTemplate_ValidData() {
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(dayShift);
        ShiftTemplateDTO result = schedulingService.createShiftTemplate(new ShiftTemplateCreateDTO());
        assertNotNull(result);
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Test createShiftTemplate with null name")
    public void testCreateShiftTemplate_NullName() {
        ShiftTemplateCreateDTO dto = new ShiftTemplateCreateDTO();
        dto.setName(null);
        assertThrows(IllegalArgumentException.class, () -> schedulingService.createShiftTemplate(dto));
    }

    @Test
    @DisplayName("Test assignShift with valid employee and shift")
    public void testAssignShift_ValidData() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(dayShift));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(assignment);
        ShiftAssignmentDTO result = schedulingService.assignShift(1L, 1L, LocalDate.now());
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test assignShift with conflicting assignment")
    public void testAssignShift_ConflictDetected() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(dayShift));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class))).thenReturn(Arrays.asList(assignment));
        assertThrows(BusinessException.class, () -> schedulingService.assignShift(1L, 1L, LocalDate.now()));
    }

    @Test
    @DisplayName("Test assignShift with non-existent employee")
    public void testAssignShift_NonExistentEmployee() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> schedulingService.assignShift(999L, 1L, LocalDate.now()));
    }

    @Test
    @DisplayName("Test bulkAssignShift to multiple employees")
    public void testBulkAssignShift_MultipleEmployees() {
        List<Long> employeeIds = Arrays.asList(1L, 2L, 3L);
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(dayShift));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(assignment);
        List<ShiftAssignmentDTO> results = schedulingService.bulkAssignShift(employeeIds, 1L, LocalDate.now());
        assertEquals(3, results.size());
    }

    @Test
    @DisplayName("Test getEmployeeSchedule for date range")
    public void testGetEmployeeSchedule_DateRange() {
        when(shiftAssignmentRepository.findByEmployeeAndDateBetween(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(Arrays.asList(assignment));
        List<ShiftAssignmentDTO> results = schedulingService.getEmployeeSchedule(1L, LocalDate.now(), LocalDate.now().plusDays(7));
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }