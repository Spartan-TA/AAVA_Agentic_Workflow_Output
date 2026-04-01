package com.warehouse.service;

import com.warehouse.entity.ShiftTemplate;
import com.warehouse.entity.ShiftSchedule;
import com.warehouse.entity.ShiftAssignment;
import com.warehouse.entity.Warehouse;
import com.warehouse.repository.ShiftTemplateRepository;
import com.warehouse.repository.ShiftScheduleRepository;
import com.warehouse.repository.ShiftAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for ShiftService.
 * Tests cover shift template management, scheduling, and conflict detection.
 * 
 * @author Warehouse EMS Test Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShiftService Tests")
public class ShiftServiceTest {

    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;

    @Mock
    private ShiftScheduleRepository shiftScheduleRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @InjectMocks
    private ShiftService shiftService;

    private ShiftTemplate dayShiftTemplate;
    private ShiftTemplate nightShiftTemplate;
    private ShiftSchedule shiftSchedule;
    private ShiftAssignment shiftAssignment;
    private Warehouse warehouse;

    @BeforeEach
    public void setUp() {
        warehouse = Warehouse.builder()
                .id(1L)
                .name("Main Warehouse")
                .timezone("America/New_York")
                .build();

        dayShiftTemplate = ShiftTemplate.builder()
                .id(1L)
                .name("Day Shift")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(16, 0))
                .breakDuration(30)
                .warehouse(warehouse)
                .build();

        nightShiftTemplate = ShiftTemplate.builder()
                .id(2L)
                .name("Night Shift")
                .startTime(LocalTime.of(20, 0))
                .endTime(LocalTime.of(4, 0))
                .breakDuration(30)
                .warehouse(warehouse)
                .build();

        shiftSchedule = ShiftSchedule.builder()
                .id(1L)
                .template(dayShiftTemplate)
                .date(LocalDate.now())
                .warehouse(warehouse)
                .build();

        shiftAssignment = ShiftAssignment.builder()
                .id(1L)
                .employeeId(100L)
                .schedule(shiftSchedule)
                .date(LocalDate.now())
                .status("ASSIGNED")
                .build();
    }

    // ========== SHIFT TEMPLATE TESTS ==========

    @Test
    @DisplayName("Test create shift template with valid data")
    public void testCreateShiftTemplateWithValidData() {
        // Arrange
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(dayShiftTemplate);

        // Act
        ShiftTemplate result = shiftService.createShiftTemplate(dayShiftTemplate);

        // Assert
        assertNotNull(result);
        assertEquals("Day Shift", result.getName());
        assertEquals(LocalTime.of(8, 0), result.getStartTime());
        assertEquals(LocalTime.of(16, 0), result.getEndTime());
        assertEquals(30, result.getBreakDuration());
        verify(shiftTemplateRepository, times(1)).save(dayShiftTemplate);
    }

    @Test
    @DisplayName("Test create shift template with null name throws exception")
    public void testCreateShiftTemplateWithNullName() {
        // Arrange
        dayShiftTemplate.setName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(dayShiftTemplate);
        });
        verify(shiftTemplateRepository, never()).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Test create shift template with null start time throws exception")
    public void testCreateShiftTemplateWithNullStartTime() {
        // Arrange
        dayShiftTemplate.setStartTime(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(dayShiftTemplate);
        });
        verify(shiftTemplateRepository, never()).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Test create shift template with null end time throws exception")
    public void testCreateShiftTemplateWithNullEndTime() {
        // Arrange
        dayShiftTemplate.setEndTime(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(dayShiftTemplate);
        });
        verify(shiftTemplateRepository, never()).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Test create shift template with end time before start time throws exception")
    public void testCreateShiftTemplateWithEndTimeBeforeStartTime() {
        // Arrange
        dayShiftTemplate.setStartTime(LocalTime.of(16, 0));
        dayShiftTemplate.setEndTime(LocalTime.of(8, 0));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftTemplate(dayShiftTemplate);
        });
        verify(shiftTemplateRepository, never()).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Test create overnight shift template")
    public void testCreateOvernightShiftTemplate() {
        // Arrange
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(nightShiftTemplate);

        // Act
        ShiftTemplate result = shiftService.createShiftTemplate(nightShiftTemplate);

        // Assert
        assertNotNull(result);
        assertEquals("Night Shift", result.getName());
        assertEquals(LocalTime.of(20, 0), result.getStartTime());
        assertEquals(LocalTime.of(4, 0), result.getEndTime());
        verify(shiftTemplateRepository, times(1)).save(nightShiftTemplate);
    }

    @Test
    @DisplayName("Test find shift template by ID")
    public void testFindShiftTemplateById() {
        // Arrange
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(dayShiftTemplate));

        // Act
        Optional<ShiftTemplate> result = shiftService.findShiftTemplateById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Day Shift", result.get().getName());
        verify(shiftTemplateRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test list all shift templates")
    public void testListAllShiftTemplates() {
        // Arrange
        List<ShiftTemplate> templates = Arrays.asList(dayShiftTemplate, nightShiftTemplate);
        when(shiftTemplateRepository.findAll()).thenReturn(templates);

        // Act
        List<ShiftTemplate> result = shiftService.listAllShiftTemplates();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(shiftTemplateRepository, times(1)).findAll();
    }

    // ========== SHIFT SCHEDULE TESTS ==========

    @Test
    @DisplayName("Test create shift schedule with valid data")
    public void testCreateShiftScheduleWithValidData() {
        // Arrange
        when(shiftScheduleRepository.save(any(ShiftSchedule.class))).thenReturn(shiftSchedule);

        // Act
        ShiftSchedule result = shiftService.createShiftSchedule(shiftSchedule);

        // Assert
        assertNotNull(result);
        assertEquals(dayShiftTemplate, result.getTemplate());
        assertEquals(LocalDate.now(), result.getDate());
        verify(shiftScheduleRepository, times(1)).save(shiftSchedule);
    }

    @Test
    @DisplayName("Test create shift schedule with null template throws exception")
    public void testCreateShiftScheduleWithNullTemplate() {
        // Arrange
        shiftSchedule.setTemplate(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftSchedule(shiftSchedule);
        });
        verify(shiftScheduleRepository, never()).save(any(ShiftSchedule.class));
    }

    @Test
    @DisplayName("Test create shift schedule with null date throws exception")
    public void testCreateShiftScheduleWithNullDate() {
        // Arrange
        shiftSchedule.setDate(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftSchedule(shiftSchedule);
        });
        verify(shiftScheduleRepository, never()).save(any(ShiftSchedule.class));
    }

    @Test
    @DisplayName("Test create shift schedule for past date throws exception")
    public void testCreateShiftScheduleForPastDate() {
        // Arrange
        shiftSchedule.setDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.createShiftSchedule(shiftSchedule);
        });
        verify(shiftScheduleRepository, never()).save(any(ShiftSchedule.class));
    }

    @Test
    @DisplayName("Test find shift schedules by date")
    public void testFindShiftSchedulesByDate() {
        // Arrange
        List<ShiftSchedule> schedules = Arrays.asList(shiftSchedule);
        when(shiftScheduleRepository.findByDate(LocalDate.now())).thenReturn(schedules);

        // Act
        List<ShiftSchedule> result = shiftService.findShiftSchedulesByDate(LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(shiftScheduleRepository, times(1)).findByDate(LocalDate.now());
    }

    // ========== SHIFT ASSIGNMENT TESTS ==========

    @Test
    @DisplayName("Test assign shift to employee with valid data")
    public void testAssignShiftToEmployeeWithValidData() {
        // Arrange
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(shiftAssignment);

        // Act
        ShiftAssignment result = shiftService.assignShiftToEmployee(100L, shiftSchedule);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getEmployeeId());
        assertEquals(shiftSchedule, result.getSchedule());
        assertEquals("ASSIGNED", result.getStatus());
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Test assign shift with null employee ID throws exception")
    public void testAssignShiftWithNullEmployeeId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShiftToEmployee(null, shiftSchedule);
        });
        verify(shiftAssignmentRepository, never()).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Test assign shift with null schedule throws exception")
    public void testAssignShiftWithNullSchedule() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.assignShiftToEmployee(100L, null);
        });
        verify(shiftAssignmentRepository, never()).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Test assign shift with conflicting assignment throws exception")
    public void testAssignShiftWithConflictingAssignment() {
        // Arrange
        ShiftAssignment conflictingAssignment = ShiftAssignment.builder()
                .employeeId(100L)
                .date(LocalDate.now())
                .status("ASSIGNED")
                .build();

        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList(conflictingAssignment));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            shiftService.assignShiftToEmployee(100L, shiftSchedule);
        });
        verify(shiftAssignmentRepository, never()).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Test bulk assign shifts to multiple employees")
    public void testBulkAssignShiftsToMultipleEmployees() {
        // Arrange
        List<Long> employeeIds = Arrays.asList(100L, 101L, 102L);
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(shiftAssignment);

        // Act
        List<ShiftAssignment> results = shiftService.bulkAssignShifts(employeeIds, shiftSchedule);

        // Assert
        assertNotNull(results);
        assertEquals(3, results.size());
        verify(shiftAssignmentRepository, times(3)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Test find upcoming shifts for employee")
    public void testFindUpcomingShiftsForEmployee() {
        // Arrange
        List<ShiftAssignment> assignments = Arrays.asList(shiftAssignment);
        when(shiftAssignmentRepository.findByEmployeeIdAndDateAfter(anyLong(), any(LocalDate.class)))
                .thenReturn(assignments);

        // Act
        List<ShiftAssignment> result = shiftService.findUpcomingShifts(100L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(shiftAssignmentRepository, times(1)).findByEmployeeIdAndDateAfter(anyLong(), any(LocalDate.class));
    }

    // ========== CONFLICT DETECTION TESTS ==========

    @Test
    @DisplayName("Test detect scheduling conflict with overlapping shifts")
    public void testDetectSchedulingConflictWithOverlappingShifts() {
        // Arrange
        ShiftAssignment existingAssignment = ShiftAssignment.builder()
                .employeeId(100L)
                .date(LocalDate.now())
                .status("ASSIGNED")
                .build();

        when(shiftAssignmentRepository.findConflictingAssignments(100L, LocalDate.now()))
                .thenReturn(Arrays.asList(existingAssignment));

        // Act
        boolean hasConflict = shiftService.hasSchedulingConflict(100L, LocalDate.now());

        // Assert
        assertTrue(hasConflict);
        verify(shiftAssignmentRepository, times(1)).findConflictingAssignments(100L, LocalDate.now());
    }

    @Test
    @DisplayName("Test detect no scheduling conflict")
    public void testDetectNoSchedulingConflict() {
        // Arrange
        when(shiftAssignmentRepository.findConflictingAssignments(100L, LocalDate.now()))
                .thenReturn(Arrays.asList());

        // Act
        boolean hasConflict = shiftService.hasSchedulingConflict(100L, LocalDate.now());

        // Assert
        assertFalse(hasConflict);
        verify(shiftAssignmentRepository, times(1)).findConflictingAssignments(100L, LocalDate.now());
    }

    @Test
    @DisplayName("Test resolve scheduling conflict by reassignment")
    public void testResolveSchedulingConflictByReassignment() {
        // Arrange
        ShiftAssignment conflictingAssignment = ShiftAssignment.builder()
                .id(1L)
                .employeeId(100L)
                .date(LocalDate.now())
                .status("ASSIGNED")
                .build();

        when(shiftAssignmentRepository.findById(1L)).thenReturn(Optional.of(conflictingAssignment));
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(conflictingAssignment);

        // Act
        shiftService.cancelShiftAssignment(1L);

        // Assert
        assertEquals("CANCELLED", conflictingAssignment.getStatus());
        verify(shiftAssignmentRepository, times(1)).save(conflictingAssignment);
    }

    // ========== SHIFT ROTATION TESTS ==========

    @Test
    @DisplayName("Test create weekly shift rotation")
    public void testCreateWeeklyShiftRotation() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        when(shiftScheduleRepository.save(any(ShiftSchedule.class))).thenReturn(shiftSchedule);

        // Act
        List<ShiftSchedule> rotation = shiftService.createWeeklyRotation(dayShiftTemplate, startDate);

        // Assert
        assertNotNull(rotation);
        assertEquals(7, rotation.size());
        verify(shiftScheduleRepository, times(7)).save(any(ShiftSchedule.class));
    }

    @Test
    @DisplayName("Test create monthly shift rotation")
    public void testCreateMonthlyShiftRotation() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        when(shiftScheduleRepository.save(any(ShiftSchedule.class))).thenReturn(shiftSchedule);

        // Act
        List<ShiftSchedule> rotation = shiftService.createMonthlyRotation(dayShiftTemplate, startDate);

        // Assert
        assertNotNull(rotation);
        assertTrue(rotation.size() >= 28 && rotation.size() <= 31);
        verify(shiftScheduleRepository, atLeast(28)).save(any(ShiftSchedule.class));
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test assign shift on weekend")
    public void testAssignShiftOnWeekend() {
        // Arrange
        LocalDate saturday = LocalDate.now().plusDays(6 - LocalDate.now().getDayOfWeek().getValue());
        shiftSchedule.setDate(saturday);

        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(shiftAssignment);

        // Act
        ShiftAssignment result = shiftService.assignShiftToEmployee(100L, shiftSchedule);

        // Assert
        assertNotNull(result);
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Test assign shift on holiday")
    public void testAssignShiftOnHoliday() {
        // Arrange
        LocalDate holiday = LocalDate.of(2024, 12, 25);
        shiftSchedule.setDate(holiday);

        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(shiftAssignment);

        // Act
        ShiftAssignment result = shiftService.assignShiftToEmployee(100L, shiftSchedule);

        // Assert
        assertNotNull(result);
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Test calculate shift duration")
    public void testCalculateShiftDuration() {
        // Act
        double duration = shiftService.calculateShiftDuration(dayShiftTemplate);

        // Assert
        assertEquals(7.5, duration, 0.01); // 8 hours - 0.5 hour break
    }

    @Test
    @DisplayName("Test calculate overnight shift duration")
    public void testCalculateOvernightShiftDuration() {
        // Act
        double duration = shiftService.calculateShiftDuration(nightShiftTemplate);

        // Assert
        assertEquals(7.5, duration, 0.01); // 8 hours - 0.5 hour break (spanning midnight)
    }

    @Test
    @DisplayName("Test find available employees for shift")
    public void testFindAvailableEmployeesForShift() {
        // Arrange
        List<Long> allEmployees = Arrays.asList(100L, 101L, 102L, 103L);
        List<ShiftAssignment> existingAssignments = Arrays.asList(
            ShiftAssignment.builder().employeeId(100L).date(LocalDate.now()).build()
        );

        when(shiftAssignmentRepository.findByDate(LocalDate.now())).thenReturn(existingAssignments);

        // Act
        List<Long> availableEmployees = shiftService.findAvailableEmployees(allEmployees, LocalDate.now());

        // Assert
        assertNotNull(availableEmployees);
        assertEquals(3, availableEmployees.size());
        assertFalse(availableEmployees.contains(100L));
    }

    @Test
    @DisplayName("Test swap shift assignments between employees")
    public void testSwapShiftAssignmentsBetweenEmployees() {
        // Arrange
        ShiftAssignment assignment1 = ShiftAssignment.builder()
                .id(1L)
                .employeeId(100L)
                .schedule(shiftSchedule)
                .date(LocalDate.now())
                .build();

        ShiftAssignment assignment2 = ShiftAssignment.builder()
                .id(2L)
                .employeeId(101L)
                .schedule(shiftSchedule)
                .date(LocalDate.now().plusDays(1))
                .build();

        when(shiftAssignmentRepository.findById(1L)).thenReturn(Optional.of(assignment1));
        when(shiftAssignmentRepository.findById(2L)).thenReturn(Optional.of(assignment2));
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(assignment1);

        // Act
        shiftService.swapShiftAssignments(1L, 2L);

        // Assert
        assertEquals(101L, assignment1.getEmployeeId());
        assertEquals(100L, assignment2.getEmployeeId());
        verify(shiftAssignmentRepository, times(2)).save(any(ShiftAssignment.class));
    }
}
