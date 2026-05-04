package com.warehouse.management.scheduling;

import com.warehouse.management.scheduling.SchedulingService;
import com.warehouse.management.scheduling.ShiftTemplate;
import com.warehouse.management.scheduling.ScheduleAssignment;
import com.warehouse.management.employee.Employee;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchedulingServiceTest {

    @Mock
    private SchedulingRepository schedulingRepository;

    @InjectMocks
    private SchedulingService schedulingService;

    private ShiftTemplate shiftTemplate;
    private Employee employee;
    private ScheduleAssignment assignment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        shiftTemplate = new ShiftTemplate(1L, "Morning", "08:00", "16:00", "Logistics");
        employee = new Employee(1L, "John Doe", "BADGE123", "WORKER", "Logistics", "A", new Date(), "ACTIVE");
        assignment = new ScheduleAssignment(1L, employee, shiftTemplate, new Date());
    }

    @Test
    void testCreateShiftTemplate_Valid() {
        when(schedulingRepository.save(any(ShiftTemplate.class))).thenReturn(shiftTemplate);
        ShiftTemplate result = schedulingService.createShiftTemplate("Morning", "08:00", "16:00", "Logistics");
        assertNotNull(result);
        assertEquals("Morning", result.getName());
    }

    @Test
    void testCreateShiftTemplate_InvalidTime() {
        assertThrows(IllegalArgumentException.class, () -> schedulingService.createShiftTemplate("Morning", "25:00", "16:00", "Logistics"));
    }

    @Test
    void testAssignShift_Valid() {
        when(schedulingRepository.save(any(ScheduleAssignment.class))).thenReturn(assignment);
        ScheduleAssignment result = schedulingService.assignShift(employee, shiftTemplate, new Date());
        assertNotNull(result);
        assertEquals(employee.getId(), result.getEmployee().getId());
    }

    @Test
    void testAssignShift_Conflict() {
        when(schedulingRepository.hasConflict(any(Employee.class), any(Date.class))).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> schedulingService.assignShift(employee, shiftTemplate, new Date()));
    }

    @Test
    void testDetectConflicts() {
        when(schedulingRepository.hasConflict(any(Employee.class), any(Date.class))).thenReturn(true);
        boolean result = schedulingService.detectConflicts(employee, new Date());
        assertTrue(result);
    }
}