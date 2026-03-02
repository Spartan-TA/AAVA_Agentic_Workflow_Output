package com.wems.scheduling.service;

import com.wems.scheduling.domain.Schedule;
import com.wems.scheduling.domain.ShiftTemplate;
import com.wems.scheduling.domain.ScheduleStatus;
import com.wems.scheduling.domain.BlackoutDate;
import com.wems.scheduling.dto.ScheduleDto;
import com.wems.scheduling.dto.BulkAssignDto;
import com.wems.scheduling.repository.ScheduleRepository;
import com.wems.scheduling.repository.ShiftTemplateRepository;
import com.wems.scheduling.repository.BlackoutDateRepository;
import com.wems.employee.domain.Employee;
import com.wems.employee.repository.EmployeeRepository;
import com.wems.common.exception.BusinessValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Schedule Service Tests")
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;
    @Mock
    private BlackoutDateRepository blackoutDateRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private ScheduleService scheduleService;

    private Employee validEmployee;
    private ShiftTemplate validShiftTemplate;
    private Schedule validSchedule;
    private LocalDate validDate;

    @BeforeEach
    void setUp() {
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setBadgeId("EMP001");
        validEmployee.setDepartment("Warehouse");

        validShiftTemplate = new ShiftTemplate();
        validShiftTemplate.setId(1L);
        validShiftTemplate.setName("Morning Shift");

        validDate = LocalDate.of(2024, 1, 15);

        validSchedule = new Schedule();
        validSchedule.setId(1L);
        validSchedule.setEmployee(validEmployee);
        validSchedule.setShiftTemplate(validShiftTemplate);
        validSchedule.setScheduleDate(validDate);
        validSchedule.setStatus(ScheduleStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Test createSchedule with valid input creates schedule")
    void testCreateSchedule_WithValidInput_CreatesSchedule() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(validShiftTemplate));
        when(blackoutDateRepository.existsByDateAndDepartment(validDate, "Warehouse")).thenReturn(false);
        when(scheduleRepository.existsByEmployeeAndScheduleDateAndStatus(validEmployee, validDate, ScheduleStatus.SCHEDULED)).thenReturn(false);
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(validSchedule);

        Schedule result = scheduleService.createSchedule(1L, 1L, validDate, validEmployee);

        assertNotNull(result);
        assertEquals(validDate, result.getScheduleDate());
        verify(scheduleRepository, times(1)).save(any(Schedule.class));
    }

    @Test
    @DisplayName("Test createSchedule on blackout date throws BusinessValidationException")
    void testCreateSchedule_OnBlackoutDate_ThrowsBusinessValidationException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(validShiftTemplate));
        when(blackoutDateRepository.existsByDateAndDepartment(validDate, "Warehouse")).thenReturn(true);

        BusinessValidationException exception = assertThrows(
            BusinessValidationException.class,
            () -> scheduleService.createSchedule(1L, 1L, validDate, validEmployee)
        );
        assertTrue(exception.getMessage().contains("blackout date"));
        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    @DisplayName("Test createSchedule with conflict throws BusinessValidationException")
    void testCreateSchedule_WithConflict_ThrowsBusinessValidationException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(validShiftTemplate));
        when(blackoutDateRepository.existsByDateAndDepartment(validDate, "Warehouse")).thenReturn(false);
        when(scheduleRepository.existsByEmployeeAndScheduleDateAndStatus(validEmployee, validDate, ScheduleStatus.SCHEDULED)).thenReturn(true);

        BusinessValidationException exception = assertThrows(
            BusinessValidationException.class,
            () -> scheduleService.createSchedule(1L, 1L, validDate, validEmployee)
        );
        assertTrue(exception.getMessage().contains("already scheduled"));
    }

    @Test
    @DisplayName("Test bulkAssign with valid input creates multiple schedules")
    void testBulkAssign_WithValidInput_CreatesMultipleSchedules() {
        BulkAssignDto dto = new BulkAssignDto();
        dto.setEmployeeIds(Arrays.asList(1L, 2L));
        dto.setDates(Arrays.asList(validDate, validDate.plusDays(1)));
        dto.setShiftTemplateId(1L);

        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(validEmployee));
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(validShiftTemplate));
        when(blackoutDateRepository.existsByDateAndDepartment(any(LocalDate.class), anyString())).thenReturn(false);
        when(scheduleRepository.existsByEmployeeAndScheduleDateAndStatus(any(Employee.class), any(LocalDate.class), any(ScheduleStatus.class))).thenReturn(false);
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(validSchedule);

        List<Schedule> results = scheduleService.bulkAssign(dto, validEmployee);

        assertNotNull(results);
        assertEquals(4, results.size());
        verify(scheduleRepository, times(4)).save(any(Schedule.class));
    }

    @Test
    @DisplayName("Test getEmployeeSchedules returns schedules for date range")
    void testGetEmployeeSchedules_WithDateRange_ReturnsSchedules() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(scheduleRepository.findByEmployeeAndScheduleDateBetween(validEmployee, startDate, endDate))
            .thenReturn(Arrays.asList(validSchedule));

        List<Schedule> results = scheduleService.getEmployeeSchedules(1L, startDate, endDate);

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Test cancelSchedule with valid ID updates status")
    void testCancelSchedule_WithValidId_UpdatesStatus() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(validSchedule));
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(validSchedule);

        scheduleService.cancelSchedule(1L, "Employee requested cancellation");

        assertEquals(ScheduleStatus.CANCELLED, validSchedule.getStatus());
        verify(scheduleRepository, times(1)).save(validSchedule);
    }