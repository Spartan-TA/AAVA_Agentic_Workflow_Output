package com.example.warehouse.service;

import com.example.warehouse.exception.BusinessValidationException;
import com.example.warehouse.repository.AttendanceEventRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConflictDetectionServiceTest {

    @Mock private AttendanceEventRepository attendanceEventRepository;
    @InjectMocks private ConflictDetectionService conflictDetectionService;

    @Test
    void validateNoConflicts_WithNoConflicts_ShouldNotThrow() {
        // Arrange
        when(attendanceEventRepository.existsByEmployeeIdAndEventTimestampBetween(anyLong(), any(), any())).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> conflictDetectionService.validateNoConflicts(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(8)));
    }

    @Test
    void validateNoConflicts_WithConflict_ShouldThrowBusinessValidationException() {
        // Arrange
        when(attendanceEventRepository.existsByEmployeeIdAndEventTimestampBetween(anyLong(), any(), any())).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessValidationException.class, () -> conflictDetectionService.validateNoConflicts(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(8)));
    }

    @Test
    void hasConflict_WithConflict_ShouldReturnTrue() {
        // Arrange
        when(attendanceEventRepository.existsByEmployeeIdAndEventTimestampBetween(anyLong(), any(), any())).thenReturn(true);

        // Act
        boolean result = conflictDetectionService.hasConflict(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(8));

        // Assert
        assertTrue(result);
    }

    @Test
    void hasConflict_WithNoConflict_ShouldReturnFalse() {
        // Arrange
        when(attendanceEventRepository.existsByEmployeeIdAndEventTimestampBetween(anyLong(), any(), any())).thenReturn(false);

        // Act
        boolean result = conflictDetectionService.hasConflict(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(8));

        // Assert
        assertFalse(result);
    }

    @Test
    void validateNoConflicts_WithNullEmployeeId_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> conflictDetectionService.validateNoConflicts(null, LocalDateTime.now(), LocalDateTime.now().plusHours(8)));
    }

    @Test
    void validateNoConflicts_WithNullStartDateTime_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> conflictDetectionService.validateNoConflicts(1L, null, LocalDateTime.now().plusHours(8)));
    }

    @Test
    void validateNoConflicts_WithNullEndDateTime_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> conflictDetectionService.validateNoConflicts(1L, LocalDateTime.now(), null));
    }

    @Test
    void hasConflict_WithEndTimeBeforeStartTime_ShouldThrowException() {
        // Arrange
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusHours(1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> conflictDetectionService.hasConflict(1L, start, end));
    }

    @Test
    void validateNoConflicts_WithSameStartAndEndTime_ShouldThrowException() {
        // Arrange
        LocalDateTime time = LocalDateTime.now();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> conflictDetectionService.validateNoConflicts(1L, time, time));
    }
}