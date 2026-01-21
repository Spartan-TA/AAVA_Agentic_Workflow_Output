package com.wms.attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit tests for AttendanceRecordRepository covering repository methods and edge cases.
 */
@ExtendWith(MockitoExtension.class)
public class AttendanceRecordRepositoryTest {

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    private AttendanceRecord validRecord;
    private AttendanceRecord anotherRecord;

    @BeforeEach
    public void setUp() {
        validRecord = new AttendanceRecord();
        validRecord.setId(1L);
        validRecord.setEmployeeId(1L);
        validRecord.setClockIn(LocalDateTime.of(2023, 6, 1, 8, 0));
        validRecord.setClockOut(LocalDateTime.of(2023, 6, 1, 16, 0));
        validRecord.setStatus("CLOCKED_OUT");
        validRecord.setDeviceInfo("Terminal2");

        anotherRecord = new AttendanceRecord();
        anotherRecord.setId(2L);
        anotherRecord.setEmployeeId(1L);
        anotherRecord.setClockIn(LocalDateTime.of(2023, 6, 2, 8, 0));
        anotherRecord.setClockOut(LocalDateTime.of(2023, 6, 2, 16, 0));
        anotherRecord.setStatus("CLOCKED_OUT");
        anotherRecord.setDeviceInfo("Terminal2");
    }

    @Test
    public void testFindByEmployeeId_ExistingRecords_ReturnsList() {
        when(attendanceRecordRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList(validRecord, anotherRecord));
        List<AttendanceRecord> result = attendanceRecordRepository.findByEmployeeId(1L);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindByEmployeeId_NoRecords_ReturnsEmptyList() {
        when(attendanceRecordRepository.findByEmployeeId(2L)).thenReturn(Collections.emptyList());
        List<AttendanceRecord> result = attendanceRecordRepository.findByEmployeeId(2L);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSave_ValidRecord_SavesSuccessfully() {
        when(attendanceRecordRepository.save(validRecord)).thenReturn(validRecord);
        AttendanceRecord saved = attendanceRecordRepository.save(validRecord);
        assertNotNull(saved);
        assertEquals(1L, saved.getId());
    }

    @Test
    public void testSave_RecordWithNullFields_SavesSuccessfully() {
        AttendanceRecord record = new AttendanceRecord();
        when(attendanceRecordRepository.save(record)).thenReturn(record);
        AttendanceRecord saved = attendanceRecordRepository.save(record);
        assertNotNull(saved);
    }

    @Test
    public void testFindByEmployeeId_NullEmployeeId_ReturnsEmptyList() {
        when(attendanceRecordRepository.findByEmployeeId(null)).thenReturn(Collections.emptyList());
        List<AttendanceRecord> result = attendanceRecordRepository.findByEmployeeId(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindById_ExistingId_ReturnsRecord() {
        when(attendanceRecordRepository.findById(1L)).thenReturn(java.util.Optional.of(validRecord));
        java.util.Optional<AttendanceRecord> result = attendanceRecordRepository.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    public void testFindById_NonExistentId_ReturnsEmpty() {
        when(attendanceRecordRepository.findById(99L)).thenReturn(java.util.Optional.empty());
        java.util.Optional<AttendanceRecord> result = attendanceRecordRepository.findById(99L);
        assertFalse(result.isPresent());
    }

    @Test
    public void testFindByEmployeeId_DateRange_ReturnsFilteredRecords() {
        LocalDateTime start = LocalDateTime.of(2023, 6, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2023, 6, 2, 23, 59);
        when(attendanceRecordRepository.findByEmployeeIdAndClockInBetween(1L, start, end)).thenReturn(Arrays.asList(validRecord, anotherRecord));
        List<AttendanceRecord> result = attendanceRecordRepository.findByEmployeeIdAndClockInBetween(1L, start, end);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindByEmployeeId_DateRange_NoRecords() {
        LocalDateTime start = LocalDateTime.of(2023, 7, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2023, 7, 2, 23, 59);
        when(attendanceRecordRepository.findByEmployeeIdAndClockInBetween(1L, start, end)).thenReturn(Collections.emptyList());
        List<AttendanceRecord> result = attendanceRecordRepository.findByEmployeeIdAndClockInBetween(1L, start, end);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindTopByEmployeeIdOrderByClockInDesc_ReturnsLatestRecord() {
        when(attendanceRecordRepository.findTopByEmployeeIdOrderByClockInDesc(1L)).thenReturn(anotherRecord);
        AttendanceRecord result = attendanceRecordRepository.findTopByEmployeeIdOrderByClockInDesc(1L);
        assertNotNull(result);
        assertEquals(2L, result.getId());
    }

    @Test
    public void testFindTopByEmployeeIdOrderByClockInDesc_NoRecord_ReturnsNull() {
        when(attendanceRecordRepository.findTopByEmployeeIdOrderByClockInDesc(2L)).thenReturn(null);
        AttendanceRecord result = attendanceRecordRepository.findTopByEmployeeIdOrderByClockInDesc(2L);
        assertNull(result);
    }
}
