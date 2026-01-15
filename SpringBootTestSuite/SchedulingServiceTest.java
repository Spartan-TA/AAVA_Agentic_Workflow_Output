package com.company.wms.scheduling.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class SchedulingServiceTest {

    @Mock
    private ShiftRepository shiftRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private SchedulingService schedulingService;

    private ShiftTemplateRequest validShiftTemplateRequest;
    private Shift shift;
    private Schedule schedule;

    @BeforeEach
    public void setUp() {
        validShiftTemplateRequest = new ShiftTemplateRequest("Morning", LocalDate.now().atTime(8,0), LocalDate.now().atTime(16,0), "HR");
        shift = new Shift(1L, "Morning", LocalDate.now().atTime(8,0), LocalDate.now().atTime(16,0), "HR");
        schedule = new Schedule(1L, 1L, 1L, LocalDate.now(), LocalDate.now().plusDays(1));
    }

    @Test
    public void testCreateShiftTemplate_WithValidInput_Success() {
        when(shiftRepository.save(any(Shift.class))).thenReturn(shift);
        Shift result = schedulingService.createShiftTemplate(validShiftTemplateRequest);
        assertNotNull(result);
        assertEquals("Morning", result.getName());
    }

    @Test
    public void testCreateShiftTemplate_WithNullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> schedulingService.createShiftTemplate(null));
    }

    @Test
    public void testCreateShiftTemplate_WithInvalidTimeRange_ThrowsValidationException() {
        ShiftTemplateRequest req = new ShiftTemplateRequest("Morning", LocalDate.now().atTime(16,0), LocalDate.now().atTime(8,0), "HR");
        assertThrows(ValidationException.class, () -> schedulingService.createShiftTemplate(req));
    }

    @Test
    public void testAssignShift_WithValidInput_Success() {
        when(employeeRepository.existsById(anyLong())).thenReturn(true);
        when(shiftRepository.existsById(anyLong())).thenReturn(true);
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);
        Schedule result = schedulingService.assignShift(1L, 1L, LocalDate.now(), LocalDate.now().plusDays(1));
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
    }

    @Test
    public void testAssignShift_WithConflict_ThrowsBusinessException() {
        when(employeeRepository.existsById(anyLong())).thenReturn(true);
        when(shiftRepository.existsById(anyLong())).thenReturn(true);
        when(scheduleRepository.hasConflict(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(true);
        assertThrows(BusinessException.class, () -> schedulingService.assignShift(1L, 1L, LocalDate.now(), LocalDate.now().plusDays(1)));
    }

    @Test
    public void testAssignShift_WithNonExistentEmployee_ThrowsResourceNotFoundException() {
        when(employeeRepository.existsById(anyLong())).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> schedulingService.assignShift(99L, 1L, LocalDate.now(), LocalDate.now().plusDays(1)));
    }

    @Test
    public void testDetectConflicts_WithNoConflicts_ReturnsEmpty() {
        when(scheduleRepository.findConflicts(anyLong(), any(LocalDate.class))).thenReturn(Collections.emptyList());
        List<Schedule> result = schedulingService.detectConflicts(1L, LocalDate.now());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testDetectConflicts_WithOverlappingShifts_ReturnsConflicts() {
        when(scheduleRepository.findConflicts(anyLong(), any(LocalDate.class))).thenReturn(Arrays.asList(schedule));
        List<Schedule> result = schedulingService.detectConflicts(1L, LocalDate.now());
        assertFalse(result.isEmpty());
    }

    @Test
    public void testCalculateOvertime_WithOvertimeHours_ReturnsCorrectAmount() {
        when(scheduleRepository.calculateOvertime(anyLong(), any(LocalDate.class))).thenReturn(2.0);
        double overtime = schedulingService.calculateOvertime(1L, LocalDate.now());
        assertEquals(2.0, overtime);
    }

    @Test
    public void testCalculateOvertime_WithNoOvertime_ReturnsZero() {
        when(scheduleRepository.calculateOvertime(anyLong(), any(LocalDate.class))).thenReturn(0.0);
        double overtime = schedulingService.calculateOvertime(1L, LocalDate.now());
        assertEquals(0.0, overtime);
    }

    @Test
    public void testBulkAssignShifts_WithValidInput_AssignsAllEmployees() {
        when(employeeRepository.existsById(anyLong())).thenReturn(true);
        when(shiftRepository.existsById(anyLong())).thenReturn(true);
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);
        List<Long> employeeIds = Arrays.asList(1L, 2L, 3L);
        List<Schedule> result = schedulingService.bulkAssignShifts(employeeIds, 1L, LocalDate.now());
        assertEquals(3, result.size());
    }

    @Test
    public void testBulkAssignShifts_WithPartialFailure_RollsBackTransaction() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(employeeRepository.existsById(2L)).thenReturn(false);
        List<Long> employeeIds = Arrays.asList(1L, 2L);
        assertThrows(BulkAssignmentException.class, () -> schedulingService.bulkAssignShifts(employeeIds, 1L, LocalDate.now()));
    }

    @Test
    public void testGetEmployeeSchedule_WithValidDateRange_ReturnsSchedule() {
        when(scheduleRepository.findByEmployeeIdAndDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(Arrays.asList(schedule));
        List<Schedule> result = schedulingService.getEmployeeSchedule(1L, LocalDate.now(), LocalDate.now().plusDays(1));
        assertEquals(1, result.size());
    }

    @Test
    public void testGetEmployeeSchedule_WithInvalidDateRange_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> schedulingService.getEmployeeSchedule(1L, LocalDate.now().plusDays(1), LocalDate.now()));
    }
}
