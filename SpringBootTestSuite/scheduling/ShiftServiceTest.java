package com.companyname.wems.scheduling.service;

import com.companyname.wems.scheduling.model.Shift;
import com.companyname.wems.scheduling.repository.ShiftRepository;
import com.companyname.wems.scheduling.exception.ShiftConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ShiftServiceTest {
    @Mock
    private ShiftRepository shiftRepository;

    @InjectMocks
    private ShiftService shiftService;

    private Shift testShift;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testShift = new Shift();
        testShift.setId(1L);
        testShift.setEmployeeId(100L);
        testShift.setStart(LocalDateTime.of(2023, 6, 1, 8, 0));
        testShift.setEnd(LocalDateTime.of(2023, 6, 1, 16, 0));
    }

    @Test
    void testAssignShift_NoConflict_Success() {
        when(shiftRepository.findConflictingShifts(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());
        when(shiftRepository.save(any(Shift.class))).thenReturn(testShift);
        Shift result = shiftService.assignShift(100L, LocalDateTime.of(2023, 6, 1, 8, 0), LocalDateTime.of(2023, 6, 1, 16, 0));
        assertNotNull(result);
        assertEquals(100L, result.getEmployeeId());
    }

    @Test
    void testAssignShift_WithConflict_ThrowsException() {
        when(shiftRepository.findConflictingShifts(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Collections.singletonList(testShift));
        assertThrows(ShiftConflictException.class, () -> shiftService.assignShift(100L, LocalDateTime.of(2023, 6, 1, 8, 0), LocalDateTime.of(2023, 6, 1, 16, 0)));
    }

    @Test
    void testAssignShift_NullEmployeeId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> shiftService.assignShift(null, LocalDateTime.of(2023, 6, 1, 8, 0), LocalDateTime.of(2023, 6, 1, 16, 0)));
    }

    @Test
    void testAssignShift_InvalidTimes_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> shiftService.assignShift(100L, null, LocalDateTime.of(2023, 6, 1, 16, 0)));
        assertThrows(IllegalArgumentException.class, () -> shiftService.assignShift(100L, LocalDateTime.of(2023, 6, 1, 8, 0), null));
    }
}
