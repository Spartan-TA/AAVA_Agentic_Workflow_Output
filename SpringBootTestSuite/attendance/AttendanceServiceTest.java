package com.companyname.wems.attendance.service;

import com.companyname.wems.attendance.model.AttendanceRecord;
import com.companyname.wems.attendance.repository.AttendanceRepository;
import com.companyname.wems.attendance.exception.AttendanceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AttendanceServiceTest {
    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private AttendanceRecord record;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        record = new AttendanceRecord();
        record.setId(1L);
        record.setEmployeeId(100L);
        record.setClockIn(LocalDateTime.of(2023, 6, 1, 8, 0));
        record.setClockOut(LocalDateTime.of(2023, 6, 1, 16, 0));
    }

    @Test
    void testClockIn_ValidInput_Success() {
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(record);
        AttendanceRecord result = attendanceService.clockIn(100L, LocalDateTime.of(2023, 6, 1, 8, 0));
        assertNotNull(result);
        assertEquals(100L, result.getEmployeeId());
    }

    @Test
    void testClockIn_NullEmployeeId_ThrowsException() {
        assertThrows(AttendanceException.class, () -> attendanceService.clockIn(null, LocalDateTime.now()));
    }

    @Test
    void testClockOut_ValidInput_Success() {
        when(attendanceRepository.findActiveByEmployeeId(100L)).thenReturn(Optional.of(record));
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(record);
        AttendanceRecord result = attendanceService.clockOut(100L, LocalDateTime.of(2023, 6, 1, 16, 0));
        assertNotNull(result);
        assertEquals(LocalDateTime.of(2023, 6, 1, 16, 0), result.getClockOut());
    }

    @Test
    void testClockOut_NoActiveRecord_ThrowsException() {
        when(attendanceRepository.findActiveByEmployeeId(101L)).thenReturn(Optional.empty());
        assertThrows(AttendanceException.class, () -> attendanceService.clockOut(101L, LocalDateTime.now()));
    }

    @Test
    void testClockIn_InvalidTime_ThrowsException() {
        assertThrows(AttendanceException.class, () -> attendanceService.clockIn(100L, null));
    }
}
