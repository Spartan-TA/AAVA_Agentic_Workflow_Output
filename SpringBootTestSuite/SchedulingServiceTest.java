package com.company.wems.scheduling.service;

import com.company.wems.scheduling.dto.ShiftTemplateDTO;
import com.company.wems.scheduling.entity.ShiftTemplate;
import com.company.wems.scheduling.repository.ShiftTemplateRepository;
import com.company.wems.common.exception.ResourceNotFoundException;
import com.company.wems.common.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for SchedulingService
 * Tests cover shift template management, validation, and edge cases
 */
@DisplayName("Scheduling Service Tests")
public class SchedulingServiceTest {

    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;

    @InjectMocks
    private SchedulingService schedulingService;

    private ShiftTemplate validShiftTemplate;
    private ShiftTemplateDTO validShiftTemplateDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup valid shift template
        validShiftTemplate = new ShiftTemplate();
        validShiftTemplate.setId(1L);
        validShiftTemplate.setName("Morning Shift");
        validShiftTemplate.setStartTime(LocalTime.of(8, 0));
        validShiftTemplate.setEndTime(LocalTime.of(16, 0));
        validShiftTemplate.setRecurrenceRule("FREQ=DAILY");
        validShiftTemplate.setMinEmployees(5);
        validShiftTemplate.setMaxEmployees(10);
        
        Set<String> skills = new HashSet<>();
        skills.add("Forklift");
        skills.add("Packing");
        validShiftTemplate.setRequiredSkills(skills);
        
        // Setup valid DTO
        validShiftTemplateDTO = new ShiftTemplateDTO();
        validShiftTemplateDTO.setId(1L);
        validShiftTemplateDTO.setName("Morning Shift");
        validShiftTemplateDTO.setStartTime("08:00");
        validShiftTemplateDTO.setEndTime("16:00");
        validShiftTemplateDTO.setRecurrenceRule("FREQ=DAILY");
        validShiftTemplateDTO.setMinEmployees(5);
        validShiftTemplateDTO.setMaxEmployees(10);
        validShiftTemplateDTO.setRequiredSkills(skills);
    }

    // ==================== CREATE SHIFT TEMPLATE TESTS ====================

    @Test
    @DisplayName("Create Shift Template - Valid Input - Should Create Successfully")
    void testCreateShiftTemplate_WithValidInput_ShouldCreateSuccessfully() {
        // Arrange
        when(shiftTemplateRepository.existsByName(validShiftTemplateDTO.getName())).thenReturn(false);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(validShiftTemplate);

        // Act
        ShiftTemplateDTO result = schedulingService.createShiftTemplate(validShiftTemplateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(validShiftTemplateDTO.getName(), result.getName());
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Create Shift Template - Duplicate Name - Should Throw DuplicateResourceException")
    void testCreateShiftTemplate_WithDuplicateName_ShouldThrowException() {
        // Arrange
        when(shiftTemplateRepository.existsByName(validShiftTemplateDTO.getName())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            schedulingService.createShiftTemplate(validShiftTemplateDTO);
        });
        verify(shiftTemplateRepository, never()).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Create Shift Template - Null Name - Should Throw Exception")
    void testCreateShiftTemplate_WithNullName_ShouldThrowException() {
        // Arrange
        validShiftTemplateDTO.setName(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            schedulingService.createShiftTemplate(validShiftTemplateDTO);
        });
    }

    @Test
    @DisplayName("Create Shift Template - Empty Name - Should Throw Exception")
    void testCreateShiftTemplate_WithEmptyName_ShouldThrowException() {
        // Arrange
        validShiftTemplateDTO.setName("");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            schedulingService.createShiftTemplate(validShiftTemplateDTO);
        });
    }

    @Test
    @DisplayName("Create Shift Template - Start Time After End Time - Should Throw Exception")
    void testCreateShiftTemplate_WithStartTimeAfterEndTime_ShouldThrowException() {
        // Arrange
        validShiftTemplateDTO.setStartTime("18:00");
        validShiftTemplateDTO.setEndTime("08:00");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            schedulingService.createShiftTemplate(validShiftTemplateDTO);
        });
    }

    @Test
    @DisplayName("Create Shift Template - Min Employees Greater Than Max - Should Throw Exception")
    void testCreateShiftTemplate_WithMinGreaterThanMax_ShouldThrowException() {
        // Arrange
        validShiftTemplateDTO.setMinEmployees(10);
        validShiftTemplateDTO.setMaxEmployees(5);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            schedulingService.createShiftTemplate(validShiftTemplateDTO);
        });
    }

    @Test
    @DisplayName("Create Shift Template - Negative Min Employees - Should Throw Exception")
    void testCreateShiftTemplate_WithNegativeMinEmployees_ShouldThrowException() {
        // Arrange
        validShiftTemplateDTO.setMinEmployees(-1);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            schedulingService.createShiftTemplate(validShiftTemplateDTO);
        });
    }

    @Test
    @DisplayName("Create Shift Template - Zero Max Employees - Should Throw Exception")
    void testCreateShiftTemplate_WithZeroMaxEmployees_ShouldThrowException() {
        // Arrange
        validShiftTemplateDTO.setMaxEmployees(0);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            schedulingService.createShiftTemplate(validShiftTemplateDTO);
        });
    }

    // ==================== UPDATE SHIFT TEMPLATE TESTS ====================

    @Test
    @DisplayName("Update Shift Template - Valid Input - Should Update Successfully")
    void testUpdateShiftTemplate_WithValidInput_ShouldUpdateSuccessfully() {
        // Arrange
        Long shiftId = 1L;
        when(shiftTemplateRepository.findById(shiftId)).thenReturn(Optional.of(validShiftTemplate));
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(validShiftTemplate);

        // Act
        ShiftTemplateDTO result = schedulingService.updateShiftTemplate(shiftId, validShiftTemplateDTO);

        // Assert
        assertNotNull(result);
        verify(shiftTemplateRepository, times(1)).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Update Shift Template - Non-Existent ID - Should Throw ResourceNotFoundException")
    void testUpdateShiftTemplate_WithNonExistentId_ShouldThrowException() {
        // Arrange
        Long shiftId = 999L;
        when(shiftTemplateRepository.findById(shiftId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            schedulingService.updateShiftTemplate(shiftId, validShiftTemplateDTO);
        });
        verify(shiftTemplateRepository, never()).save(any(ShiftTemplate.class));
    }

    @Test
    @DisplayName("Update Shift Template - Null ID - Should Throw Exception")
    void testUpdateShiftTemplate_WithNullId_ShouldThrowException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            schedulingService.updateShiftTemplate(null, validShiftTemplateDTO);
        });
    }

    // ==================== GET SHIFT TEMPLATE TESTS ====================

    @Test
    @DisplayName("Get Shift Template - Valid ID - Should Return Template")
    void testGetShiftTemplate_WithValidId_ShouldReturnTemplate() {
        // Arrange
        Long shiftId = 1L;
        when(shiftTemplateRepository.findById(shiftId)).thenReturn(Optional.of(validShiftTemplate));

        // Act
        ShiftTemplateDTO result = schedulingService.getShiftTemplateById(shiftId);

        // Assert
        assertNotNull(result);
        assertEquals(validShiftTemplate.getName(), result.getName());
    }

    @Test
    @DisplayName("Get Shift Template - Non-Existent ID - Should Throw ResourceNotFoundException")
    void testGetShiftTemplate_WithNonExistentId_ShouldThrowException() {
        // Arrange
        Long shiftId = 999L;
        when(shiftTemplateRepository.findById(shiftId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            schedulingService.getShiftTemplateById(shiftId);
        });
    }

    @Test
    @DisplayName("Get All Shift Templates - Should Return List")
    void testGetAllShiftTemplates_ShouldReturnList() {
        // Arrange
        List<ShiftTemplate> templates = Arrays.asList(validShiftTemplate);
        when(shiftTemplateRepository.findAll()).thenReturn(templates);

        // Act
        List<ShiftTemplateDTO> result = schedulingService.getAllShiftTemplates();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Get All Shift Templates - Empty Result - Should Return Empty List")
    void testGetAllShiftTemplates_WithNoTemplates_ShouldReturnEmptyList() {
        // Arrange
        when(shiftTemplateRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<ShiftTemplateDTO> result = schedulingService.getAllShiftTemplates();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== DELETE SHIFT TEMPLATE TESTS ====================

    @Test
    @DisplayName("Delete Shift Template - Valid ID - Should Delete Successfully")
    void testDeleteShiftTemplate_WithValidId_ShouldDeleteSuccessfully() {
        // Arrange
        Long shiftId = 1L;
        when(shiftTemplateRepository.existsById(shiftId)).thenReturn(true);
        doNothing().when(shiftTemplateRepository).deleteById(shiftId);

        // Act
        schedulingService.deleteShiftTemplate(shiftId);

        // Assert
        verify(shiftTemplateRepository, times(1)).deleteById(shiftId);
    }

    @Test
    @DisplayName("Delete Shift Template - Non-Existent ID - Should Throw ResourceNotFoundException")
    void testDeleteShiftTemplate_WithNonExistentId_ShouldThrowException() {
        // Arrange
        Long shiftId = 999L;
        when(shiftTemplateRepository.existsById(shiftId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            schedulingService.deleteShiftTemplate(shiftId);
        });
        verify(shiftTemplateRepository, never()).deleteById(anyLong());
    }

    // ==================== BOUNDARY AND EDGE CASE TESTS ====================

    @Test
    @DisplayName("Create Shift Template - Midnight Start Time - Should Create Successfully")
    void testCreateShiftTemplate_WithMidnightStartTime_ShouldCreateSuccessfully() {
        // Arrange
        validShiftTemplateDTO.setStartTime("00:00");
        validShiftTemplateDTO.setEndTime("08:00");
        when(shiftTemplateRepository.existsByName(validShiftTemplateDTO.getName())).thenReturn(false);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(validShiftTemplate);

        // Act
        ShiftTemplateDTO result = schedulingService.createShiftTemplate(validShiftTemplateDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Shift Template - 24 Hour Shift - Should Create Successfully")
    void testCreateShiftTemplate_With24HourShift_ShouldCreateSuccessfully() {
        // Arrange
        validShiftTemplateDTO.setStartTime("00:00");
        validShiftTemplateDTO.setEndTime("23:59");
        when(shiftTemplateRepository.existsByName(validShiftTemplateDTO.getName())).thenReturn(false);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(validShiftTemplate);

        // Act
        ShiftTemplateDTO result = schedulingService.createShiftTemplate(validShiftTemplateDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Shift Template - Empty Skills Set - Should Create Successfully")
    void testCreateShiftTemplate_WithEmptySkills_ShouldCreateSuccessfully() {
        // Arrange
        validShiftTemplateDTO.setRequiredSkills(new HashSet<>());
        when(shiftTemplateRepository.existsByName(validShiftTemplateDTO.getName())).thenReturn(false);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(validShiftTemplate);

        // Act
        ShiftTemplateDTO result = schedulingService.createShiftTemplate(validShiftTemplateDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Shift Template - Maximum Name Length - Should Create Successfully")
    void testCreateShiftTemplate_WithMaxNameLength_ShouldCreateSuccessfully() {
        // Arrange
        String maxLengthName = "S".repeat(100);
        validShiftTemplateDTO.setName(maxLengthName);
        when(shiftTemplateRepository.existsByName(maxLengthName)).thenReturn(false);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(validShiftTemplate);

        // Act
        ShiftTemplateDTO result = schedulingService.createShiftTemplate(validShiftTemplateDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Shift Template - Same Start and End Time - Should Throw Exception")
    void testCreateShiftTemplate_WithSameStartAndEndTime_ShouldThrowException() {
        // Arrange
        validShiftTemplateDTO.setStartTime("08:00");
        validShiftTemplateDTO.setEndTime("08:00");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            schedulingService.createShiftTemplate(validShiftTemplateDTO);
        });
    }

    @Test
    @DisplayName("Create Shift Template - Min Equals Max Employees - Should Create Successfully")
    void testCreateShiftTemplate_WithMinEqualsMax_ShouldCreateSuccessfully() {
        // Arrange
        validShiftTemplateDTO.setMinEmployees(5);
        validShiftTemplateDTO.setMaxEmployees(5);
        when(shiftTemplateRepository.existsByName(validShiftTemplateDTO.getName())).thenReturn(false);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(validShiftTemplate);

        // Act
        ShiftTemplateDTO result = schedulingService.createShiftTemplate(validShiftTemplateDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Shift Template - Invalid Time Format - Should Throw Exception")
    void testCreateShiftTemplate_WithInvalidTimeFormat_ShouldThrowException() {
        // Arrange
        validShiftTemplateDTO.setStartTime("25:00");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            schedulingService.createShiftTemplate(validShiftTemplateDTO);
        });
    }

    @Test
    @DisplayName("Create Shift Template - Null Recurrence Rule - Should Create Successfully")
    void testCreateShiftTemplate_WithNullRecurrenceRule_ShouldCreateSuccessfully() {
        // Arrange
        validShiftTemplateDTO.setRecurrenceRule(null);
        when(shiftTemplateRepository.existsByName(validShiftTemplateDTO.getName())).thenReturn(false);
        when(shiftTemplateRepository.save(any(ShiftTemplate.class))).thenReturn(validShiftTemplate);

        // Act
        ShiftTemplateDTO result = schedulingService.createShiftTemplate(validShiftTemplateDTO);

        // Assert
        assertNotNull(result);
    }
}