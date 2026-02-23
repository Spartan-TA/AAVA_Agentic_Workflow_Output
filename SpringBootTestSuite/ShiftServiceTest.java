package com.example.service;

import com.example.entity.Shift;
import com.example.repository.ShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShiftServiceTest {

    @Mock
    private ShiftRepository shiftRepository;

    @InjectMocks
    private ShiftService shiftService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getShift_found_returnsShift() {
        Shift shift = new Shift();
        shift.setId(1L);
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));

        Shift result = shiftService.getShift(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getShift_notFound_throwsException() {
        when(shiftRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> shiftService.getShift(1L));
    }
}