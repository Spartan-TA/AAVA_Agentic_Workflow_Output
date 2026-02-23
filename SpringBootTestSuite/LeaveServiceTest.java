package com.example.service;

import com.example.entity.Leave;
import com.example.repository.LeaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    @InjectMocks
    private LeaveService leaveService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void approveLeave_found_success() {
        Leave leave = new Leave();
        leave.setId(1L);
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));
        when(leaveRepository.save(any())).thenReturn(leave);

        Leave result = leaveService.approveLeave(1L);

        assertEquals(1L, result.getId());
        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    void approveLeave_notFound_throwsException() {
        when(leaveRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> leaveService.approveLeave(1L));
    }
}