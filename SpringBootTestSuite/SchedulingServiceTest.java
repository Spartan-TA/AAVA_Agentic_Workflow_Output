package com.warehouse.ems.scheduling.service;

import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.scheduling.entity.ShiftAssignment;
import com.warehouse.ems.scheduling.entity.ShiftTemplate;
import com.warehouse.ems.scheduling.repository.ShiftAssignmentRepository;
import com.warehouse.ems.scheduling.repository.ShiftTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for SchedulingService.
 * Tests cover shift template management, shift assignments, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
public class SchedulingServiceTest {

    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @InjectMocks
    private SchedulingService schedulingService;

    private ShiftTemplate morningShift;
    private ShiftTemplate eveningShift;
    private ShiftTemplate nightShift;
    private ShiftAssignment testAssignment;
    private Employee testEmployee;

    @BeforeEach
    public void setUp() {
        // Create test shift templates
        morningShift = new ShiftTemplate();
        morningShift.setId(1L);
        morningShift.setName("Morning Shift");
        morningShift.setStartTime(LocalTime.of(6, 0));
        morningShift.setEndTime(LocalTime.of(14, 0));
        morningShift.setRecurrence("DAILY");

        eveningShift = new ShiftTemplate();
        eveningShift.setId(2L);
        eveningShift.setName("Evening Shift");
        eveningShift.setStartTime(LocalTime.of(14, 0));
        eveningShift.setEndTime(LocalTime.of(22, 0));
        eveningShift.setRecurrence("DAILY");

        nightShift = new ShiftTemplate();
        nightShift.setId(3L);
        nightShift.setName("Night Shift");
        nightShift.setStartTime(LocalTime.of(22, 0));
        nightShift.setEndTime(LocalTime.of(6, 0));
        nightShift.setRecurrence("DAILY");

        // Create test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        // Create test shift assignment
        testAssignment = new ShiftAssignment();
        testAssignment.setId(1L);
        testAssignment.setEmployee(testEmployee);
        testAssignment.setShiftTemplate(morningShift);
        testAssignment.setAssignedDate(LocalDate.now());
    }

    // ========== GET ALL SHIFT TEMPLATES TESTS ==========

    @Test
    public void testGetAllShiftTemplates_Success() {
        // Arrange
        List<ShiftTemplate> templates = Arrays.asList(morningShift, eveningShift, nightShift);
        when(shiftTemplateRepository.findAll()).thenReturn(templates);

        // Act
        List<ShiftTemplate> result = schedulingService.getAllShiftTemplates();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Morning Shift", result.get(0).getName());
        assertEquals("Evening Shift", result.get(1).getName());
        assertEquals("Night Shift", result.get(2).getName());
        verify(shiftTemplateRepository, times(1)).findAll();
    }

    @Test
    public void testGetAllShiftTemplates_EmptyList() {
        // Arrange
        when(shiftTemplateRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<ShiftTemplate> result = schedulingService.getAllShiftTemplates();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(shiftTemplateRepository, times(1)).findAll();
    }

    @Test
    public void testGetAllShiftTemplates_SingleTemplate() {
        // Arrange
        when(shiftTemplateRepository.findAll()).thenReturn(Arrays.asList(morningShift));

        // Act
        List<ShiftTemplate> result = schedulingService.getAllShiftTemplates();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Morning Shift", result.get(0).getName());
    }

    // ========== CREATE SHIFT TEMPLATE TESTS ==========

    @Test
    public void testCreateShiftTemplate_Success() {
        // Arrange
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(morningShift);

        // Act
        ShiftTemplate result = schedulingService.createShiftTemplate(morningShift);

        // Assert
        assertNotNull(result);
        assertEquals("Morning Shift", result.getName());
        assertEquals(LocalTime.of(6, 0), result.getStartTime());
        assertEquals(LocalTime.of(14, 0), result.getEndTime());
        verify(shiftTemplateRepository, times(1)).save(morningShift);
    }

    @Test
    public void testCreateShiftTemplate_OvernightShift() {
        // Arrange
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(nightShift);

        // Act
        ShiftTemplate result = schedulingService.createShiftTemplate(nightShift);

        // Assert
        assertNotNull(result);
        assertEquals("Night Shift", result.getName());
        assertTrue(result.getStartTime().isAfter(result.getEndTime())); // Overnight shift
        verify(shiftTemplateRepository, times(1)).save(nightShift);
    }

    @Test
    public void testCreateShiftTemplate_NullName() {
        // Arrange
        ShiftTemplate invalidTemplate = new ShiftTemplate();
        invalidTemplate.setName(null);
        invalidTemplate.setStartTime(LocalTime.of(8, 0));
        invalidTemplate.setEndTime(LocalTime.of(16, 0));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(invalidTemplate);

        // Act
        ShiftTemplate result = schedulingService.createShiftTemplate(invalidTemplate);

        // Assert
        assertNotNull(result);
        assertNull(result.getName());
    }

    @Test
    public void testCreateShiftTemplate_EmptyName() {
        // Arrange
        ShiftTemplate invalidTemplate = new ShiftTemplate();
        invalidTemplate.setName("");
        invalidTemplate.setStartTime(LocalTime.of(8, 0));
        invalidTemplate.setEndTime(LocalTime.of(16, 0));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(invalidTemplate);

        // Act
        ShiftTemplate result = schedulingService.createShiftTemplate(invalidTemplate);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getName());
    }

    @Test
    public void testCreateShiftTemplate_SameStartAndEndTime() {
        // Arrange
        ShiftTemplate sameTimeTemplate = new ShiftTemplate();
        sameTimeTemplate.setName("Same Time Shift");
        sameTimeTemplate.setStartTime(LocalTime.of(12, 0));
        sameTimeTemplate.setEndTime(LocalTime.of(12, 0));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(sameTimeTemplate);

        // Act
        ShiftTemplate result = schedulingService.createShiftTemplate(sameTimeTemplate);

        // Assert
        assertNotNull(result);
        assertEquals(result.getStartTime(), result.getEndTime());
    }

    @Test
    public void testCreateShiftTemplate_WithBlackoutDates() {
        // Arrange
        ShiftTemplate templateWithBlackout = new ShiftTemplate();
        templateWithBlackout.setName("Holiday Shift");
        templateWithBlackout.setStartTime(LocalTime.of(9, 0));
        templateWithBlackout.setEndTime(LocalTime.of(17, 0));
        templateWithBlackout.setBlackoutDates("2024-12-25,2024-01-01");
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(templateWithBlackout);

        // Act
        ShiftTemplate result = schedulingService.createShiftTemplate(templateWithBlackout);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getBlackoutDates());
        assertTrue(result.getBlackoutDates().contains("2024-12-25"));
    }

    // ========== ASSIGN SHIFT TESTS ==========

    @Test
    public void testAssignShift_Success() {
        // Arrange
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        ShiftAssignment result = schedulingService.assignShift(testAssignment);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getEmployee().getId());
        assertEquals("Morning Shift", result.getShiftTemplate().getName());
        assertEquals(LocalDate.now(), result.getAssignedDate());
        verify(shiftAssignmentRepository, times(1)).save(testAssignment);
    }

    @Test
    public void testAssignShift_FutureDate() {
        // Arrange
        testAssignment.setAssignedDate(LocalDate.now().plusDays(7));
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        ShiftAssignment result = schedulingService.assignShift(testAssignment);

        // Assert
        assertNotNull(result);
        assertTrue(result.getAssignedDate().isAfter(LocalDate.now()));
        verify(shiftAssignmentRepository, times(1)).save(testAssignment);
    }

    @Test
    public void testAssignShift_PastDate() {
        // Arrange
        testAssignment.setAssignedDate(LocalDate.now().minusDays(7));
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        ShiftAssignment result = schedulingService.assignShift(testAssignment);

        // Assert
        assertNotNull(result);
        assertTrue(result.getAssignedDate().isBefore(LocalDate.now()));
        verify(shiftAssignmentRepository, times(1)).save(testAssignment);
    }

    @Test
    public void testAssignShift_NullEmployee() {
        // Arrange
        testAssignment.setEmployee(null);
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        ShiftAssignment result = schedulingService.assignShift(testAssignment);

        // Assert
        assertNotNull(result);
        assertNull(result.getEmployee());
    }

    @Test
    public void testAssignShift_NullShiftTemplate() {
        // Arrange
        testAssignment.setShiftTemplate(null);
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        ShiftAssignment result = schedulingService.assignShift(testAssignment);

        // Assert
        assertNotNull(result);
        assertNull(result.getShiftTemplate());
    }

    @Test
    public void testAssignShift_MultipleShiftsToSameEmployee() {
        // Arrange
        ShiftAssignment assignment2 = new ShiftAssignment();
        assignment2.setEmployee(testEmployee);
        assignment2.setShiftTemplate(eveningShift);
        assignment2.setAssignedDate(LocalDate.now().plusDays(1));

        when(shiftAssignmentRepository.save(any(ShiftAssignment.class)))
            .thenReturn(testAssignment)
            .thenReturn(assignment2);

        // Act
        ShiftAssignment result1 = schedulingService.assignShift(testAssignment);
        ShiftAssignment result2 = schedulingService.assignShift(assignment2);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getEmployee().getId(), result2.getEmployee().getId());
        verify(shiftAssignmentRepository, times(2)).save(any(ShiftAssignment.class));
    }

    // ========== GET EMPLOYEE SHIFTS TESTS ==========

    @Test
    public void testGetEmployeeShifts_Success() {
        // Arrange
        ShiftAssignment assignment2 = new ShiftAssignment();
        assignment2.setId(2L);
        assignment2.setEmployee(testEmployee);
        assignment2.setShiftTemplate(eveningShift);
        assignment2.setAssignedDate(LocalDate.now().plusDays(1));

        List<ShiftAssignment> assignments = Arrays.asList(testAssignment, assignment2);
        when(shiftAssignmentRepository.findByEmployeeId(1L)).thenReturn(assignments);

        // Act
        List<ShiftAssignment> result = schedulingService.getEmployeeShifts(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getEmployee().getId());
        assertEquals(1L, result.get(1).getEmployee().getId());
        verify(shiftAssignmentRepository, times(1)).findByEmployeeId(1L);
    }

    @Test
    public void testGetEmployeeShifts_EmptyList() {
        // Arrange
        when(shiftAssignmentRepository.findByEmployeeId(999L)).thenReturn(Arrays.asList());

        // Act
        List<ShiftAssignment> result = schedulingService.getEmployeeShifts(999L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(shiftAssignmentRepository, times(1)).findByEmployeeId(999L);
    }

    @Test
    public void testGetEmployeeShifts_NullEmployeeId() {
        // Arrange
        when(shiftAssignmentRepository.findByEmployeeId(null)).thenReturn(Arrays.asList());

        // Act
        List<ShiftAssignment> result = schedulingService.getEmployeeShifts(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetEmployeeShifts_MultipleWeeks() {
        // Arrange
        List<ShiftAssignment> assignments = Arrays.asList(
            createAssignment(1L, LocalDate.now()),
            createAssignment(2L, LocalDate.now().plusDays(1)),
            createAssignment(3L, LocalDate.now().plusDays(2)),
            createAssignment(4L, LocalDate.now().plusDays(3)),
            createAssignment(5L, LocalDate.now().plusDays(4)),
            createAssignment(6L, LocalDate.now().plusDays(5)),
            createAssignment(7L, LocalDate.now().plusDays(6))
        );
        when(shiftAssignmentRepository.findByEmployeeId(1L)).thenReturn(assignments);

        // Act
        List<ShiftAssignment> result = schedulingService.getEmployeeShifts(1L);

        // Assert
        assertNotNull(result);
        assertEquals(7, result.size());
        verify(shiftAssignmentRepository, times(1)).findByEmployeeId(1L);
    }

    @Test
    public void testGetEmployeeShifts_MixedShiftTypes() {
        // Arrange
        List<ShiftAssignment> assignments = Arrays.asList(
            createAssignmentWithTemplate(1L, morningShift, LocalDate.now()),
            createAssignmentWithTemplate(2L, eveningShift, LocalDate.now().plusDays(1)),
            createAssignmentWithTemplate(3L, nightShift, LocalDate.now().plusDays(2))
        );
        when(shiftAssignmentRepository.findByEmployeeId(1L)).thenReturn(assignments);

        // Act
        List<ShiftAssignment> result = schedulingService.getEmployeeShifts(1L);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Morning Shift", result.get(0).getShiftTemplate().getName());
        assertEquals("Evening Shift", result.get(1).getShiftTemplate().getName());
        assertEquals("Night Shift", result.get(2).getShiftTemplate().getName());
    }

    // ========== HELPER METHODS ==========

    private ShiftAssignment createAssignment(Long id, LocalDate date) {
        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setId(id);
        assignment.setEmployee(testEmployee);
        assignment.setShiftTemplate(morningShift);
        assignment.setAssignedDate(date);
        return assignment;
    }

    private ShiftAssignment createAssignmentWithTemplate(Long id, ShiftTemplate template, LocalDate date) {
        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setId(id);
        assignment.setEmployee(testEmployee);
        assignment.setShiftTemplate(template);
        assignment.setAssignedDate(date);
        return assignment;
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    public void testCreateShiftTemplate_WeeklyRecurrence() {
        // Arrange
        ShiftTemplate weeklyTemplate = new ShiftTemplate();
        weeklyTemplate.setName("Weekly Shift");
        weeklyTemplate.setStartTime(LocalTime.of(9, 0));
        weeklyTemplate.setEndTime(LocalTime.of(17, 0));
        weeklyTemplate.setRecurrence("WEEKLY");
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(weeklyTemplate);

        // Act
        ShiftTemplate result = schedulingService.createShiftTemplate(weeklyTemplate);

        // Assert
        assertNotNull(result);
        assertEquals("WEEKLY", result.getRecurrence());
    }

    @Test
    public void testAssignShift_SameDayMultipleShifts() {
        // Arrange
        ShiftAssignment morningAssignment = new ShiftAssignment();
        morningAssignment.setEmployee(testEmployee);
        morningAssignment.setShiftTemplate(morningShift);
        morningAssignment.setAssignedDate(LocalDate.now());

        ShiftAssignment eveningAssignment = new ShiftAssignment();
        eveningAssignment.setEmployee(testEmployee);
        eveningAssignment.setShiftTemplate(eveningShift);
        eveningAssignment.setAssignedDate(LocalDate.now());

        when(shiftAssignmentRepository.save(any(ShiftAssignment.class)))
            .thenReturn(morningAssignment)
            .thenReturn(eveningAssignment);

        // Act
        ShiftAssignment result1 = schedulingService.assignShift(morningAssignment);
        ShiftAssignment result2 = schedulingService.assignShift(eveningAssignment);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getAssignedDate(), result2.getAssignedDate());
        assertNotEquals(result1.getShiftTemplate().getName(), result2.getShiftTemplate().getName());
    }
}