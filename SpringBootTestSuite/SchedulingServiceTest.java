package com.warehouse.ems.scheduling.service;

import com.warehouse.ems.scheduling.entity.*;
import com.warehouse.ems.scheduling.repository.*;
import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulingServiceTest {
    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;
    @Mock
    private ScheduledShiftRepository scheduledShiftRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private SchedulingService schedulingService;
    private ShiftTemplate shiftTemplate;
    private ScheduledShift scheduledShift;
    private Employee employee;

    @BeforeEach
    void setUp() {
        shiftTemplate = new ShiftTemplate();
        shiftTemplate.setId(1L);
        shiftTemplate.setName("Morning Shift");
        shiftTemplate.setStartTime(LocalTime.of(8, 0));
        shiftTemplate.setEndTime(LocalTime.of(16, 0));
        shiftTemplate.setDaysOfWeek(Arrays.asList("MON", "TUE", "WED", "THU", "FRI"));
        
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("B12345");
        employee.setName("John Doe");
        
        scheduledShift = new ScheduledShift();
        scheduledShift.setId(1L);
        scheduledShift.setEmployeeId(1L);
        scheduledShift.setShiftTemplateId(1L);
        scheduledShift.setShiftDate(LocalDate.now());
        scheduledShift.setStartTime(LocalTime.of(8, 0));
        scheduledShift.setEndTime(LocalTime.of(16, 0));
    }

    @AfterEach
    void tearDown() {
        shiftTemplate = null;
        scheduledShift = null;
        employee = null;
    }

    @Test
    void testCreateTemplate_ValidData_Success() {
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(shiftTemplate);
        ShiftTemplate created = schedulingService.createTemplate(shiftTemplate);
        assertNotNull(created);
        assertEquals("Morning Shift", created.getName());
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    void testCreateTemplate_NullData_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> schedulingService.createTemplate(null));
    }

    @Test
    void testCreateTemplate_InvalidTimeRange_ThrowsException() {
        shiftTemplate.setStartTime(LocalTime.of(16, 0));
        shiftTemplate.setEndTime(LocalTime.of(8, 0));
        assertThrows(IllegalArgumentException.class, () -> schedulingService.createTemplate(shiftTemplate));
    }

    @Test
    void testAssignShift_ValidEmployee_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(shiftTemplate));
        when(scheduledShiftRepository.save(any(ScheduledShift.class))).thenReturn(scheduledShift);
        ScheduledShift assigned = schedulingService.assignShift(1L, 1L, LocalDate.now());
        assertNotNull(assigned);
        assertEquals(1L, assigned.getEmployeeId());
    }

    @Test
    void testAssignShift_WithConflict_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(shiftTemplate));
        when(scheduledShiftRepository.findConflictingShifts(1L, LocalDate.now())).thenReturn(Arrays.asList(scheduledShift));
        assertThrows(IllegalStateException.class, () -> schedulingService.assignShift(1L, 1L, LocalDate.now()));
    }

    @Test
    void testDetectConflicts_OverlappingShifts_ReturnsTrue() {
        ScheduledShift existingShift = new ScheduledShift();
        existingShift.setShiftDate(LocalDate.now());
        existingShift.setStartTime(LocalTime.of(7, 0));
        existingShift.setEndTime(LocalTime.of(15, 0));
        when(scheduledShiftRepository.findByEmployeeIdAndDate(1L, LocalDate.now())).thenReturn(Arrays.asList(existingShift));
        boolean hasConflict = schedulingService.detectConflicts(1L, LocalDate.now(), LocalTime.of(8, 0), LocalTime.of(16, 0));
        assertTrue(hasConflict);
    }

    @Test
    void testDetectConflicts_BlackoutDate_ReturnsTrue() {
        when(schedulingService.isBlackoutDate(LocalDate.now())).thenReturn(true);
        boolean hasConflict = schedulingService.detectConflicts(1L, LocalDate.now(), LocalTime.of(8, 0), LocalTime.of(16, 0));
        assertTrue(hasConflict);
    }

    @Test
    void testBulkAssign_MultipleEmployees_Success() {
        List<Long> employeeIds = Arrays.asList(1L, 2L, 3L);
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(shiftTemplate));
        when(scheduledShiftRepository.save(any(ScheduledShift.class))).thenReturn(scheduledShift);
        List<ScheduledShift> assigned = schedulingService.bulkAssign(employeeIds, 1L, LocalDate.now());
        assertEquals(3, assigned.size());
    }

    @Test
    void testBulkAssign_PartialFailures_ReturnsSuccessful() {
        List<Long> employeeIds = Arrays.asList(1L, 999L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(shiftTemplate));
        when(scheduledShiftRepository.save(any(ScheduledShift.class))).thenReturn(scheduledShift);
        List<ScheduledShift> assigned = schedulingService.bulkAssign(employeeIds, 1L, LocalDate.now());
        assertEquals(1, assigned.size());
    }

    @Test
    void testRotationScheduleCreation_GeneratesCorrectly() {
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(shiftTemplate));
        when(scheduledShiftRepository.save(any(ScheduledShift.class))).thenReturn(scheduledShift);
        List<ScheduledShift> rotation = schedulingService.createRotation(1L, 1L, LocalDate.now(), 4);
        assertEquals(4, rotation.size());
    }

    @Test
    void testOvertimeRulesValidation_ExceedsLimit_ThrowsException() {
        when(scheduledShiftRepository.calculateWeeklyHours(1L, LocalDate.now())).thenReturn(40.0);
        assertThrows(IllegalStateException.class, () -> schedulingService.validateOvertimeRules(1L, LocalDate.now(), 10.0));
    }

    @Test
    void testWarehouseOperationCalendar_HolidayCheck_ReturnsTrue() {
        boolean isHoliday = schedulingService.isWarehouseHoliday(LocalDate.of(2024, 12, 25));
        assertTrue(isHoliday);
    }

    @Test
    void testPersonalShiftView_WorkerRole_ReturnsOwnShifts() {
        when(scheduledShiftRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList(scheduledShift));
        List<ScheduledShift> shifts = schedulingService.getPersonalShifts(1L);
        assertFalse(shifts.isEmpty());
        assertEquals(1L, shifts.get(0).getEmployeeId());
    }

    @Test
    void testAuditEntryGeneration_OnAssignment_CreatesLog() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(shiftTemplate));
        when(scheduledShiftRepository.save(any(ScheduledShift.class))).thenReturn(scheduledShift);
        schedulingService.assignShift(1L, 1L, LocalDate.now());
        verify(schedulingService.auditService, times(1)).logChange(anyString(), anyLong(), anyString());
    }

    @Test
    void testShiftTemplateUpdate_ValidChanges_Success() {
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(shiftTemplate));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(shiftTemplate);
        shiftTemplate.setName("Updated Morning Shift");
        ShiftTemplate updated = schedulingService.updateTemplate(1L, shiftTemplate);
        assertEquals("Updated Morning Shift", updated.getName());
    }

    @Test
    void testShiftDeletion_WithCascade_RemovesScheduledShifts() {
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(shiftTemplate));
        doNothing().when(scheduledShiftRepository).deleteByShiftTemplateId(1L);
        schedulingService.deleteTemplate(1L);
        verify(scheduledShiftRepository, times(1)).deleteByShiftTemplateId(1L);
        verify(shiftTemplateRepository, times(1)).deleteById(1L);
    }
}