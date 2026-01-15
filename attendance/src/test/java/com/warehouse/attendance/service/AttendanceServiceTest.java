package com.warehouse.attendance.service;

import com.warehouse.attendance.entity.Attendance;
import com.warehouse.attendance.repository.AttendanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttendanceServiceTest {
    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAttendanceById() {
        Attendance att = Attendance.builder().id(1L).date(LocalDate.now()).status(Attendance.Status.PRESENT).build();
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(att));
        Optional<Attendance> result = attendanceService.getAttendanceById(1L);
        assertTrue(result.isPresent());
        assertEquals(Attendance.Status.PRESENT, result.get().getStatus());
    }
}
