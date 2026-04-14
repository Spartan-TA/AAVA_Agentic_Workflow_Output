package com.wms.ems.service;

import com.wms.ems.dto.ShiftTemplateCreateDTO;
import com.wms.ems.dto.ShiftTemplateDTO;
import com.wms.ems.dto.ShiftAssignmentCreateDTO;
import com.wms.ems.dto.ShiftAssignmentDTO;
import com.wms.ems.dto.BulkShiftAssignmentDTO;
import com.wms.ems.entity.ShiftTemplate;
import com.wms.ems.entity.ShiftAssignment;
import com.wms.ems.entity.Employee;
import com.wms.ems.exception.EntityNotFoundException;
import com.wms.ems.exception.ValidationException;
import com.wms.ems.exception.ConflictException;
import com.wms.ems.repository.ShiftTemplateRepository;
import com.wms.ems.repository.ShiftAssignmentRepository;
import com.wms.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for ShiftService.
 * Tests cover shift templates, assignments, conflict detection, and all edge cases.
 * 
 * @author EMS Test Suite Generator
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Shift Service Tests")
class ShiftServiceTest {

    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ShiftService shiftService;

    private ShiftTemplate testTemplate;
    private ShiftAssignment testAssignment;
    private Employee testEmployee;
    private ShiftTemplateCreateDTO templateCreateDTO;
    private ShiftAssignmentCreateDTO assignmentCreateDTO;

    @BeforeEach
    void setUp() {
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");

        // Setup test shift template
        testTemplate = new ShiftTemplate();
        testTemplate.setId(1L);
        testTemplate.setName("Morning Shift");
        testTemplate.setStartTime(LocalTime.of(8, 0));
        testTemplate.setEndTime(LocalTime.of(17, 0));
        testTemplate.setDaysOfWeek(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));

        // Setup test shift assignment
        testAssignment = new ShiftAssignment();
        testAssignment.setId(1L);
        testAssignment.setEmployee(testEmployee);
        testAssignment.setShiftTemplate(testTemplate);
        testAssignment.setShiftDate(LocalDate.now());
        testAssignment.setStartTime(LocalTime.of(8, 0));
        testAssignment.setEndTime(LocalTime.of(17, 0));

        // Setup template create DTO
        templateCreateDTO = ShiftTemplateCreateDTO.builder()
                .name("Evening Shift")
                .startTime(LocalTime.of(17, 0))
                .endTime(LocalTime.of(1, 0))
                .daysOfWeek(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY))
                .build();

        // Setup assignment create DTO
        assignmentCreateDTO = ShiftAssignmentCreateDTO.builder()
                .employeeId(1L)
                .shiftTemplateId(1L)
                .shiftDate(LocalDate.now().plusDays(1))
                .build();
    }

    // ==================== CREATE SHIFT TEMPLATE TESTS ====================

    @Test
    @DisplayName("Create Shift Template - Valid Input - Success")
    void testCreateShiftTemplate_ValidInput_Success() {
        // Arrange
        when(shiftTemplateRepository.existsByName(anyString())).thenReturn(false);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testTemplate);

        // Act
        ShiftTemplateDTO result = shiftService.createShiftTemplate(templateCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Morning Shift", result.getName());
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Create Shift Template - Duplicate Name - Throws ValidationException")
    void testCreateShiftTemplate_DuplicateName_ThrowsValidationException() {
        // Arrange
        when(shiftTemplateRepository.existsByName(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            shiftService.createShiftTemplate(templateCreateDTO);
        });
        verify(shiftTemplateRepository, never()).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Create Shift Template - Null Name - Throws ValidationException")
    void testCreateShiftTemplate_NullName_ThrowsValidationException() {
        // Arrange
        templateCreateDTO.setName(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            shiftService.createShiftTemplate(templateCreateDTO);
        });
    }

    @Test
    @DisplayName("Create Shift Template - Empty Name - Throws ValidationException")
    void testCreateShiftTemplate_EmptyName_ThrowsValidationException() {
        // Arrange
        templateCreateDTO.setName("");

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            shiftService.createShiftTemplate(templateCreateDTO);
        });
    }

    @Test
    @DisplayName("Create Shift Template - Null Start Time - Throws ValidationException")
    void testCreateShiftTemplate_NullStartTime_ThrowsValidationException() {
        // Arrange
        templateCreateDTO.setStartTime(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            shiftService.createShiftTemplate(templateCreateDTO);
        });
    }

    @Test
    @DisplayName("Create Shift Template - Null End Time - Throws ValidationException")
    void testCreateShiftTemplate_NullEndTime_ThrowsValidationException() {
        // Arrange
        templateCreateDTO.setEndTime(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            shiftService.createShiftTemplate(templateCreateDTO);
        });
    }

    @Test
    @DisplayName("Create Shift Template - End Time Before Start Time - Success (Overnight Shift)")
    void testCreateShiftTemplate_EndTimeBeforeStartTime_Success() {
        // Arrange
        templateCreateDTO.setStartTime(LocalTime.of(22, 0));
        templateCreateDTO.setEndTime(LocalTime.of(6, 0));
        when(shiftTemplateRepository.existsByName(anyString())).thenReturn(false);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testTemplate);

        // Act
        ShiftTemplateDTO result = shiftService.createShiftTemplate(templateCreateDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Shift Template - Empty Days Of Week - Throws ValidationException")
    void testCreateShiftTemplate_EmptyDaysOfWeek_ThrowsValidationException() {
        // Arrange
        templateCreateDTO.setDaysOfWeek(Arrays.asList());

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            shiftService.createShiftTemplate(templateCreateDTO);
        });
    }

    @Test
    @DisplayName("Create Shift Template - All Days Of Week - Success")
    void testCreateShiftTemplate_AllDaysOfWeek_Success() {
        // Arrange
        templateCreateDTO.setDaysOfWeek(Arrays.asList(DayOfWeek.values()));
        when(shiftTemplateRepository.existsByName(anyString())).thenReturn(false);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testTemplate);

        // Act
        ShiftTemplateDTO result = shiftService.createShiftTemplate(templateCreateDTO);

        // Assert
        assertNotNull(result);
    }

    // ==================== CREATE SHIFT ASSIGNMENT TESTS ====================

    @Test
    @DisplayName("Create Shift Assignment - Valid Input - Success")
    void testCreateShiftAssignment_ValidInput_Success() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(anyLong())).thenReturn(Optional.of(testTemplate));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        ShiftAssignmentDTO result = shiftService.createShiftAssignment(assignmentCreateDTO);

        // Assert
        assertNotNull(result);
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Create Shift Assignment - Invalid Employee ID - Throws EntityNotFoundException")
    void testCreateShiftAssignment_InvalidEmployeeId_ThrowsEntityNotFoundException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            shiftService.createShiftAssignment(assignmentCreateDTO);
        });
        verify(shiftAssignmentRepository, never()).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Create Shift Assignment - Invalid Template ID - Throws EntityNotFoundException")
    void testCreateShiftAssignment_InvalidTemplateId_ThrowsEntityNotFoundException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            shiftService.createShiftAssignment(assignmentCreateDTO);
        });
    }

    @Test
    @DisplayName("Create Shift Assignment - Conflicting Assignment - Throws ConflictException")
    void testCreateShiftAssignment_ConflictingAssignment_ThrowsConflictException() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(anyLong())).thenReturn(Optional.of(testTemplate));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(Arrays.asList(testAssignment));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            shiftService.createShiftAssignment(assignmentCreateDTO);
        });
    }

    @Test
    @DisplayName("Create Shift Assignment - Past Date - Throws ValidationException")
    void testCreateShiftAssignment_PastDate_ThrowsValidationException() {
        // Arrange
        assignmentCreateDTO.setShiftDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            shiftService.createShiftAssignment(assignmentCreateDTO);
        });
    }

    @Test
    @DisplayName("Create Shift Assignment - Null Employee ID - Throws ValidationException")
    void testCreateShiftAssignment_NullEmployeeId_ThrowsValidationException() {
        // Arrange
        assignmentCreateDTO.setEmployeeId(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            shiftService.createShiftAssignment(assignmentCreateDTO);
        });
    }

    @Test
    @DisplayName("Create Shift Assignment - Null Template ID - Throws ValidationException")
    void testCreateShiftAssignment_NullTemplateId_ThrowsValidationException() {
        // Arrange
        assignmentCreateDTO.setShiftTemplateId(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            shiftService.createShiftAssignment(assignmentCreateDTO);
        });
    }

    @Test
    @DisplayName("Create Shift Assignment - Null Shift Date - Throws ValidationException")
    void testCreateShiftAssignment_NullShiftDate_ThrowsValidationException() {
        // Arrange
        assignmentCreateDTO.setShiftDate(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            shiftService.createShiftAssignment(assignmentCreateDTO);
        });
    }

    // ==================== BULK ASSIGNMENT TESTS ====================

    @Test
    @DisplayName("Bulk Assign Shifts - Valid Input - Success")
    void testBulkAssignShifts_ValidInput_Success() {
        // Arrange
        BulkShiftAssignmentDTO bulkDTO = BulkShiftAssignmentDTO.builder()
                .employeeIds(Arrays.asList(1L, 2L))
                .shiftTemplateId(1L)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .build();

        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(anyLong())).thenReturn(Optional.of(testTemplate));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        List<ShiftAssignmentDTO> results = shiftService.bulkAssignShifts(bulkDTO);

        // Assert
        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    @Test
    @DisplayName("Bulk Assign Shifts - Empty Employee List - Throws ValidationException")
    void testBulkAssignShifts_EmptyEmployeeList_ThrowsValidationException() {
        // Arrange
        BulkShiftAssignmentDTO bulkDTO = BulkShiftAssignmentDTO.builder()
                .employeeIds(Arrays.asList())
                .shiftTemplateId(1L)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .build();

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            shiftService.bulkAssignShifts(bulkDTO);
        });
    }

    @Test
    @DisplayName("Bulk Assign Shifts - End Date Before Start Date - Throws ValidationException")
    void testBulkAssignShifts_EndDateBeforeStartDate_ThrowsValidationException() {
        // Arrange
        BulkShiftAssignmentDTO bulkDTO = BulkShiftAssignmentDTO.builder()
                .employeeIds(Arrays.asList(1L))
                .shiftTemplateId(1L)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().minusDays(1))
                .build();

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            shiftService.bulkAssignShifts(bulkDTO);
        });
    }

    @Test
    @DisplayName("Bulk Assign Shifts - Partial Conflicts - Returns Successful Assignments")
    void testBulkAssignShifts_PartialConflicts_ReturnsSuccessfulAssignments() {
        // Arrange
        BulkShiftAssignmentDTO bulkDTO = BulkShiftAssignmentDTO.builder()
                .employeeIds(Arrays.asList(1L, 2L))
                .shiftTemplateId(1L)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(2))
                .build();

        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(anyLong())).thenReturn(Optional.of(testTemplate));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(Arrays.asList())
                .thenReturn(Arrays.asList(testAssignment));
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        List<ShiftAssignmentDTO> results = shiftService.bulkAssignShifts(bulkDTO);

        // Assert
        assertNotNull(results);
    }

    // ==================== GET SHIFT TESTS ====================

    @Test
    @DisplayName("Get Shift Template By ID - Valid ID - Success")
    void testGetShiftTemplateById_ValidId_Success() {
        // Arrange
        when(shiftTemplateRepository.findById(anyLong())).thenReturn(Optional.of(testTemplate));

        // Act
        ShiftTemplateDTO result = shiftService.getShiftTemplateById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Morning Shift", result.getName());
    }

    @Test
    @DisplayName("Get Shift Template By ID - Invalid ID - Throws EntityNotFoundException")
    void testGetShiftTemplateById_InvalidId_ThrowsEntityNotFoundException() {
        // Arrange
        when(shiftTemplateRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            shiftService.getShiftTemplateById(999L);
        });
    }

    @Test
    @DisplayName("Get Upcoming Shifts For Employee - Valid Employee - Success")
    void testGetUpcomingShiftsForEmployee_ValidEmployee_Success() {
        // Arrange
        when(shiftAssignmentRepository.findUpcomingShiftsByEmployee(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList(testAssignment));

        // Act
        List<ShiftAssignmentDTO> results = shiftService.getUpcomingShiftsForEmployee(1L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Get Upcoming Shifts For Employee - No Shifts - Returns Empty List")
    void testGetUpcomingShiftsForEmployee_NoShifts_ReturnsEmptyList() {
        // Arrange
        when(shiftAssignmentRepository.findUpcomingShiftsByEmployee(anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());

        // Act
        List<ShiftAssignmentDTO> results = shiftService.getUpcomingShiftsForEmployee(1L);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    // ==================== UPDATE SHIFT TESTS ====================

    @Test
    @DisplayName("Update Shift Assignment - Valid Input - Success")
    void testUpdateShiftAssignment_ValidInput_Success() {
        // Arrange
        when(shiftAssignmentRepository.findById(anyLong())).thenReturn(Optional.of(testAssignment));
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        ShiftAssignmentDTO result = shiftService.updateShiftAssignment(1L, assignmentCreateDTO);

        // Assert
        assertNotNull(result);
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Update Shift Assignment - Invalid ID - Throws EntityNotFoundException")
    void testUpdateShiftAssignment_InvalidId_ThrowsEntityNotFoundException() {
        // Arrange
        when(shiftAssignmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            shiftService.updateShiftAssignment(999L, assignmentCreateDTO);
        });
    }

    // ==================== DELETE SHIFT TESTS ====================

    @Test
    @DisplayName("Delete Shift Assignment - Valid ID - Success")
    void testDeleteShiftAssignment_ValidId_Success() {
        // Arrange
        when(shiftAssignmentRepository.findById(anyLong())).thenReturn(Optional.of(testAssignment));
        doNothing().when(shiftAssignmentRepository).delete(any(ShiftAssignment.class));

        // Act
        shiftService.deleteShiftAssignment(1L);

        // Assert
        verify(shiftAssignmentRepository, times(1)).delete(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Delete Shift Assignment - Invalid ID - Throws EntityNotFoundException")
    void testDeleteShiftAssignment_InvalidId_ThrowsEntityNotFoundException() {
        // Arrange
        when(shiftAssignmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            shiftService.deleteShiftAssignment(999L);
        });
    }

    // ==================== BOUNDARY CONDITION TESTS ====================

    @Test
    @DisplayName("Create Shift Template - 24 Hour Shift - Success")
    void testCreateShiftTemplate_24HourShift_Success() {
        // Arrange
        templateCreateDTO.setStartTime(LocalTime.of(0, 0));
        templateCreateDTO.setEndTime(LocalTime.of(23, 59));
        when(shiftTemplateRepository.existsByName(anyString())).thenReturn(false);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testTemplate);

        // Act
        ShiftTemplateDTO result = shiftService.createShiftTemplate(templateCreateDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Shift Template - Midnight Start - Success")
    void testCreateShiftTemplate_MidnightStart_Success() {
        // Arrange
        templateCreateDTO.setStartTime(LocalTime.of(0, 0));
        templateCreateDTO.setEndTime(LocalTime.of(8, 0));
        when(shiftTemplateRepository.existsByName(anyString())).thenReturn(false);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testTemplate);

        // Act
        ShiftTemplateDTO result = shiftService.createShiftTemplate(templateCreateDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Bulk Assign Shifts - Maximum Date Range - Success")
    void testBulkAssignShifts_MaximumDateRange_Success() {
        // Arrange
        BulkShiftAssignmentDTO bulkDTO = BulkShiftAssignmentDTO.builder()
                .employeeIds(Arrays.asList(1L))
                .shiftTemplateId(1L)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .build();

        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(anyLong())).thenReturn(Optional.of(testTemplate));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        List<ShiftAssignmentDTO> results = shiftService.bulkAssignShifts(bulkDTO);

        // Assert
        assertNotNull(results);
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Create Shift Template - Name With Special Characters - Success")
    void testCreateShiftTemplate_NameWithSpecialCharacters_Success() {
        // Arrange
        templateCreateDTO.setName("Shift-A/B (Weekend)");
        when(shiftTemplateRepository.existsByName(anyString())).thenReturn(false);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testTemplate);

        // Act
        ShiftTemplateDTO result = shiftService.createShiftTemplate(templateCreateDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Shift Assignment - Same Day Multiple Shifts - Detects Conflict")
    void testCreateShiftAssignment_SameDayMultipleShifts_DetectsConflict() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(anyLong())).thenReturn(Optional.of(testTemplate));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(Arrays.asList(testAssignment));

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            shiftService.createShiftAssignment(assignmentCreateDTO);
        });
    }

    @Test
    @DisplayName("Bulk Assign Shifts - Large Employee List - Success")
    void testBulkAssignShifts_LargeEmployeeList_Success() {
        // Arrange
        List<Long> largeEmployeeList = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
        BulkShiftAssignmentDTO bulkDTO = BulkShiftAssignmentDTO.builder()
                .employeeIds(largeEmployeeList)
                .shiftTemplateId(1L)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build();

        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(shiftTemplateRepository.findById(anyLong())).thenReturn(Optional.of(testTemplate));
        when(shiftAssignmentRepository.findConflictingAssignments(anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testAssignment);

        // Act
        List<ShiftAssignmentDTO> results = shiftService.bulkAssignShifts(bulkDTO);

        // Assert
        assertNotNull(results);
    }
}