package com.warehouse.ems.domain.shift;

import com.warehouse.ems.domain.employee.Employee;
import com.warehouse.ems.domain.employee.EmployeeRepository;
import com.warehouse.ems.exception.BusinessException;
import com.warehouse.ems.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Shift Service Test Suite")
public class ShiftServiceTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ShiftServiceImpl shiftService;

    private ShiftTemplate testShiftTemplate;
    private Shift testShift;
    private Employee testEmployee;
    private ShiftAssignment testShiftAssignment;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testShiftTemplate = new ShiftTemplate();
        testShiftTemplate.setId(1L);
        testShiftTemplate.setName("Morning Shift");
        testShiftTemplate.setStartTime(LocalTime.of(8, 0));
        testShiftTemplate.setEndTime(LocalTime.of(16, 0));
        testShiftTemplate.setDurationHours(8.0);

        testShift = new Shift();
        testShift.setId(1L);
        testShift.setTemplate(testShiftTemplate);
        testShift.setShiftDate(LocalDate.now());
        testShift.setStartTime(LocalTime.of(8, 0));
        testShift.setEndTime(LocalTime.of(16, 0));

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        testShiftAssignment = new ShiftAssignment();
        testShiftAssignment.setId(1L);
        testShiftAssignment.setShift(testShift);
        testShiftAssignment.setEmployee(testEmployee);
        testShiftAssignment.setStatus(ShiftAssignmentStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Test create shift template with valid data")
    public void testCreateShiftTemplateWithValidData() {
        // Arrange
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testShiftTemplate);

        // Act
        ShiftTemplateDto result = shiftService.createShiftTemplate(new ShiftTemplateDto());

        // Assert
        assertNotNull(result);
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Test create shift template with null name")
    public void testCreateShiftTemplateWithNullName() {
        // Arrange
        ShiftTemplateDto dto = new ShiftTemplateDto();
        dto.setName(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            shiftService.createShiftTemplate(dto);
        });
    }

    @Test
    @DisplayName("Test create shift template with empty name")
    public void testCreateShiftTemplateWithEmptyName() {
        // Arrange
        ShiftTemplateDto dto = new ShiftTemplateDto();
        dto.setName("");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            shiftService.createShiftTemplate(dto);
        });
    }

    @Test
    @DisplayName("Test create shift template with invalid time range")
    public void testCreateShiftTemplateWithInvalidTimeRange() {
        // Arrange
        ShiftTemplateDto dto = new ShiftTemplateDto();
        dto.setStartTime(LocalTime.of(16, 0));
        dto.setEndTime(LocalTime.of(8, 0));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.createShiftTemplate(dto);
        });
    }

    @Test
    @DisplayName("Test create shift template with overnight shift")
    public void testCreateShiftTemplateWithOvernightShift() {
        // Arrange
        ShiftTemplateDto dto = new ShiftTemplateDto();
        dto.setName("Night Shift");
        dto.setStartTime(LocalTime.of(22, 0));
        dto.setEndTime(LocalTime.of(6, 0));
        dto.setOvernightShift(true);
        
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testShiftTemplate);

        // Act
        ShiftTemplateDto result = shiftService.createShiftTemplate(dto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test assign shift to employee - success")
    public void testAssignShiftToEmployeeSuccess() {
        // Arrange
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(testShift));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftAssignmentRepository.findConflictingAssignments(any(), any(), any())).thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        ShiftAssignmentDto result = shiftService.assignShift(1L, 1L);

        // Assert
        assertNotNull(result);
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("Test assign shift with non-existent shift")
    public void testAssignShiftWithNonExistentShift() {
        // Arrange
        when(shiftRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.assignShift(999L, 1L);
        });
    }

    @Test
    @DisplayName("Test assign shift with non-existent employee")
    public void testAssignShiftWithNonExistentEmployee() {
        // Arrange
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(testShift));
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.assignShift(1L, 999L);
        });
    }

    @Test
    @DisplayName("Test assign shift with conflict detection")
    public void testAssignShiftWithConflictDetection() {
        // Arrange
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(testShift));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftAssignmentRepository.findConflictingAssignments(any(), any(), any()))
            .thenReturn(Arrays.asList(testShiftAssignment));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            shiftService.assignShift(1L, 1L);
        });
        assertTrue(exception.getMessage().contains("conflict"));
    }

    @Test
    @DisplayName("Test bulk assign shifts - success")
    public void testBulkAssignShiftsSuccess() {
        // Arrange
        List<Long> employeeIds = Arrays.asList(1L, 2L, 3L);
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(testShift));
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(shiftAssignmentRepository.findConflictingAssignments(any(), any(), any())).thenReturn(Arrays.asList());
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        List<ShiftAssignmentDto> results = shiftService.bulkAssignShifts(1L, employeeIds);

        // Assert
        assertNotNull(results);
        assertEquals(3, results.size());
        verify(shiftAssignmentRepository, times(3)).save(any(ShiftAssignment.class));
    }

    @Test
    @DisplayName("Test bulk assign shifts with empty employee list")
    public void testBulkAssignShiftsWithEmptyList() {
        // Arrange
        List<Long> employeeIds = Arrays.asList();

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.bulkAssignShifts(1L, employeeIds);
        });
    }

    @Test
    @DisplayName("Test bulk assign shifts with null employee list")
    public void testBulkAssignShiftsWithNullList() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            shiftService.bulkAssignShifts(1L, null);
        });
    }

    @Test
    @DisplayName("Test get upcoming shifts for employee")
    public void testGetUpcomingShiftsForEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftAssignmentRepository.findUpcomingShiftsByEmployee(anyLong(), any()))
            .thenReturn(Arrays.asList(testShiftAssignment));

        // Act
        List<ShiftAssignmentDto> results = shiftService.getUpcomingShifts(1L);

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    @DisplayName("Test get upcoming shifts for non-existent employee")
    public void testGetUpcomingShiftsForNonExistentEmployee() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.getUpcomingShifts(999L);
        });
    }

    @Test
    @DisplayName("Test cancel shift assignment - success")
    public void testCancelShiftAssignmentSuccess() {
        // Arrange
        when(shiftAssignmentRepository.findById(1L)).thenReturn(Optional.of(testShiftAssignment));
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(testShiftAssignment);

        // Act
        shiftService.cancelShiftAssignment(1L);

        // Assert
        verify(shiftAssignmentRepository, times(1)).save(any(ShiftAssignment.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("Test cancel non-existent shift assignment")
    public void testCancelNonExistentShiftAssignment() {
        // Arrange
        when(shiftAssignmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            shiftService.cancelShiftAssignment(999L);
        });
    }

    @Test
    @DisplayName("Test create shift template with maximum duration")
    public void testCreateShiftTemplateWithMaxDuration() {
        // Arrange
        ShiftTemplateDto dto = new ShiftTemplateDto();
        dto.setName("Extended Shift");
        dto.setStartTime(LocalTime.of(0, 0));
        dto.setEndTime(LocalTime.of(23, 59));
        dto.setDurationHours(24.0);
        
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testShiftTemplate);

        // Act
        ShiftTemplateDto result = shiftService.createShiftTemplate(dto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create shift template with minimum duration")
    public void testCreateShiftTemplateWithMinDuration() {
        // Arrange
        ShiftTemplateDto dto = new ShiftTemplateDto();
        dto.setName("Short Shift");
        dto.setStartTime(LocalTime.of(8, 0));
        dto.setEndTime(LocalTime.of(9, 0));
        dto.setDurationHours(1.0);
        
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testShiftTemplate);

        // Act
        ShiftTemplateDto result = shiftService.createShiftTemplate(dto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test assign shift on blackout date")
    public void testAssignShiftOnBlackoutDate() {
        // Arrange
        LocalDate blackoutDate = LocalDate.now().plusDays(7);
        testShift.setShiftDate(blackoutDate);
        
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(testShift));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftService.isBlackoutDate(blackoutDate)).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.assignShift(1L, 1L);
        });
    }

    @Test
    @DisplayName("Test update shift template - success")
    public void testUpdateShiftTemplateSuccess() {
        // Arrange
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(testShiftTemplate);

        ShiftTemplateDto updateDto = new ShiftTemplateDto();
        updateDto.setName("Updated Morning Shift");

        // Act
        ShiftTemplateDto result = shiftService.updateShiftTemplate(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Test delete shift template - success")
    public void testDeleteShiftTemplateSuccess() {
        // Arrange
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftRepository.countByTemplate(1L)).thenReturn(0L);

        // Act
        shiftService.deleteShiftTemplate(1L);

        // Assert
        verify(shiftTemplateRepository, times(1)).delete(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Test delete shift template with active shifts")
    public void testDeleteShiftTemplateWithActiveShifts() {
        // Arrange
        when(shiftTemplateRepository.findById(1L)).thenReturn(Optional.of(testShiftTemplate));
        when(shiftRepository.countByTemplate(1L)).thenReturn(5L);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            shiftService.deleteShiftTemplate(1L);
        });
    }

    @Test
    @DisplayName("Test assign shift with null shift ID")
    public void testAssignShiftWithNullShiftId() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            shiftService.assignShift(null, 1L);
        });
    }

    @Test
    @DisplayName("Test assign shift with null employee ID")
    public void testAssignShiftWithNullEmployeeId() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            shiftService.assignShift(1L, null);
        });
    }
}