package com.wms.attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit tests for AttendanceService covering all service methods and edge cases.
 */
@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private AttendanceRecordDTO validClockInDTO;
    private AttendanceRecord validClockInRecord;

    @BeforeEach
    public void setUp() {
        validClockInRecord = new AttendanceRecord();
        validClockInRecord.setId(1L);
        validClockInRecord.setEmployeeId(1L);
        validClockInRecord.setClockIn(LocalDateTime.now().minusHours(2));
        validClockInRecord.setStatus("CLOCKED_IN");
        validClockInRecord.setDeviceInfo("Terminal1");

        validClockInDTO = new AttendanceRecordDTO();
        validClockInDTO.setId(1L);
        validClockInDTO.setEmployeeId(1L);
        validClockInDTO.setClockIn(validClockInRecord.getClockIn());
        validClockInDTO.setStatus("CLOCKED_IN");
        validClockInDTO.setDeviceInfo("Terminal1");
    }

    @Test
    public void testClockIn_ValidClockIn_ReturnsDTO() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(attendanceRecordRepository.save(any(AttendanceRecord.class))).thenReturn(validClockInRecord);
        AttendanceRecordDTO result = attendanceService.clockIn(1L, "Terminal1");
        assertNotNull(result);
        assertEquals("CLOCKED_IN", result.getStatus());
    }

    @Test
    public void testClockIn_DuplicateClockIn_ThrowsValidationException() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(attendanceRecordRepository.findTopByEmployeeIdOrderByClockInDesc(1L)).thenReturn(validClockInRecord);
        assertThrows(ValidationException.class, () -> attendanceService.clockIn(1L, "Terminal1"));
    }

    @Test
    public void testClockIn_NonExistentEmployee_ThrowsResourceNotFoundException() {
        when(employeeRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.clockIn(99L, "Terminal1"));
    }

    @Test
    public void testClockIn_NullEmployeeId_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> attendanceService.clockIn(null, "Terminal1"));
    }

    @Test
    public void testClockOut_ValidClockOut_ReturnsDTO() {
        validClockInRecord.setClockOut(LocalDateTime.now());
        validClockInRecord.setStatus("CLOCKED_OUT");
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(attendanceRecordRepository.findTopByEmployeeIdOrderByClockInDesc(1L)).thenReturn(validClockInRecord);
        when(attendanceRecordRepository.save(any(AttendanceRecord.class))).thenReturn(validClockInRecord);
        AttendanceRecordDTO result = attendanceService.clockOut(1L);
        assertNotNull(result);
        assertEquals("CLOCKED_OUT", result.getStatus());
    }

    @Test
    public void testClockOut_WithoutClockIn_ThrowsValidationException() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(attendanceRecordRepository.findTopByEmployeeIdOrderByClockInDesc(1L)).thenReturn(null);
        assertThrows(ValidationException.class, () -> attendanceService.clockOut(1L));
    }

    @Test
    public void testClockOut_NonExistentEmployee_ThrowsResourceNotFoundException() {
        when(employeeRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.clockOut(99L));
    }

    @Test
    public void testGetAttendanceByEmployee_ExistingRecords_ReturnsList() {
        when(attendanceRecordRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList(validClockInRecord));
        List<AttendanceRecordDTO> result = attendanceService.getAttendanceByEmployee(1L);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetAttendanceByEmployee_NoRecords_ReturnsEmptyList() {
        when(attendanceRecordRepository.findByEmployeeId(2L)).thenReturn(Collections.emptyList());
        List<AttendanceRecordDTO> result = attendanceService.getAttendanceByEmployee(2L);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAttendanceByEmployee_NullEmployeeId_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> attendanceService.getAttendanceByEmployee(null));
    }

    @Test
    public void testCalculateHoursWorked_ValidCalculation_ReturnsHours() {
        validClockInRecord.setClockOut(validClockInRecord.getClockIn().plusHours(8));
        when(attendanceRecordRepository.findById(1L)).thenReturn(java.util.Optional.of(validClockInRecord));
        Double hours = attendanceService.calculateHoursWorked(1L);
        assertEquals(8.0, hours);
    }

    @Test
    public void testCalculateHoursWorked_MissingClockOut_ThrowsValidationException() {
        validClockInRecord.setClockOut(null);
        when(attendanceRecordRepository.findById(1L)).thenReturn(java.util.Optional.of(validClockInRecord));
        assertThrows(ValidationException.class, () -> attendanceService.calculateHoursWorked(1L));
    }

    @Test
    public void testCalculateHoursWorked_InvalidRecordId_ThrowsResourceNotFoundException() {
        when(attendanceRecordRepository.findById(99L)).thenReturn(java.util.Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.calculateHoursWorked(99L));
    }

    @Test
    public void testClockIn_NullDeviceInfo_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> attendanceService.clockIn(1L, null));
    }

    @Test
    public void testClockOut_AlreadyClockedOut_ThrowsValidationException() {
        validClockInRecord.setStatus("CLOCKED_OUT");
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(attendanceRecordRepository.findTopByEmployeeIdOrderByClockInDesc(1L)).thenReturn(validClockInRecord);
        assertThrows(ValidationException.class, () -> attendanceService.clockOut(1L));
    }
}
