package com.wms.attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit tests for AttendanceController covering all REST endpoints and edge cases.
 */
@ExtendWith(MockitoExtension.class)
public class AttendanceControllerTest {

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private AttendanceController attendanceController;

    private AttendanceRecordDTO validClockInDTO;
    private AttendanceRecordDTO validClockOutDTO;

    @BeforeEach
    public void setUp() {
        validClockInDTO = new AttendanceRecordDTO();
        validClockInDTO.setId(1L);
        validClockInDTO.setEmployeeId(1L);
        validClockInDTO.setClockIn(LocalDateTime.now().minusHours(2));
        validClockInDTO.setStatus("CLOCKED_IN");
        validClockInDTO.setDeviceInfo("Terminal1");

        validClockOutDTO = new AttendanceRecordDTO();
        validClockOutDTO.setId(1L);
        validClockOutDTO.setEmployeeId(1L);
        validClockOutDTO.setClockIn(LocalDateTime.now().minusHours(2));
        validClockOutDTO.setClockOut(LocalDateTime.now());
        validClockOutDTO.setStatus("CLOCKED_OUT");
        validClockOutDTO.setDeviceInfo("Terminal1");
    }

    @Test
    public void testClockIn_201Created() {
        when(attendanceService.clockIn(1L, "Terminal1")).thenReturn(validClockInDTO);
        ResponseEntity<AttendanceRecordDTO> response = attendanceController.clockIn(1L, "Terminal1");
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(validClockInDTO, response.getBody());
    }

    @Test
    public void testClockIn_BadRequest_ThrowsValidationException() {
        doThrow(new ValidationException("Invalid input")).when(attendanceService).clockIn(1L, "Terminal1");
        ValidationException ex = assertThrows(ValidationException.class, () -> attendanceController.clockIn(1L, "Terminal1"));
        assertEquals("Invalid input", ex.getMessage());
    }

    @Test
    public void testClockIn_Conflict_ThrowsValidationException() {
        doThrow(new ValidationException("Duplicate clock-in")).when(attendanceService).clockIn(1L, "Terminal1");
        ValidationException ex = assertThrows(ValidationException.class, () -> attendanceController.clockIn(1L, "Terminal1"));
        assertEquals("Duplicate clock-in", ex.getMessage());
    }

    @Test
    public void testClockOut_200OK() {
        when(attendanceService.clockOut(1L)).thenReturn(validClockOutDTO);
        ResponseEntity<AttendanceRecordDTO> response = attendanceController.clockOut(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(validClockOutDTO, response.getBody());
    }

    @Test
    public void testClockOut_BadRequest_ThrowsValidationException() {
        doThrow(new ValidationException("Invalid clock-out")).when(attendanceService).clockOut(1L);
        ValidationException ex = assertThrows(ValidationException.class, () -> attendanceController.clockOut(1L));
        assertEquals("Invalid clock-out", ex.getMessage());
    }

    @Test
    public void testClockOut_404NotFound_ThrowsResourceNotFoundException() {
        doThrow(new ResourceNotFoundException("Employee not found")).when(attendanceService).clockOut(99L);
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> attendanceController.clockOut(99L));
        assertEquals("Employee not found", ex.getMessage());
    }

    @Test
    public void testGetAttendanceByEmployee_200OK() {
        List<AttendanceRecordDTO> records = Arrays.asList(validClockInDTO, validClockOutDTO);
        when(attendanceService.getAttendanceByEmployee(1L)).thenReturn(records);
        ResponseEntity<List<AttendanceRecordDTO>> response = attendanceController.getAttendanceByEmployee(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    public void testGetAttendanceByEmployee_404NotFound_ThrowsResourceNotFoundException() {
        doThrow(new ResourceNotFoundException("Employee not found")).when(attendanceService).getAttendanceByEmployee(99L);
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> attendanceController.getAttendanceByEmployee(99L));
        assertEquals("Employee not found", ex.getMessage());
    }

    @Test
    public void testClockIn_NullEmployeeId_ThrowsValidationException() {
        doThrow(new ValidationException("EmployeeId is null")).when(attendanceService).clockIn(null, "Terminal1");
        ValidationException ex = assertThrows(ValidationException.class, () -> attendanceController.clockIn(null, "Terminal1"));
        assertEquals("EmployeeId is null", ex.getMessage());
    }

    @Test
    public void testClockIn_NullDeviceInfo_ThrowsValidationException() {
        doThrow(new ValidationException("DeviceInfo is null")).when(attendanceService).clockIn(1L, null);
        ValidationException ex = assertThrows(ValidationException.class, () -> attendanceController.clockIn(1L, null));
        assertEquals("DeviceInfo is null", ex.getMessage());
    }

    @Test
    public void testClockOut_NullEmployeeId_ThrowsValidationException() {
        doThrow(new ValidationException("EmployeeId is null")).when(attendanceService).clockOut(null);
        ValidationException ex = assertThrows(ValidationException.class, () -> attendanceController.clockOut(null));
        assertEquals("EmployeeId is null", ex.getMessage());
    }

    @Test
    public void testGetAttendanceByEmployee_NullEmployeeId_ThrowsValidationException() {
        doThrow(new ValidationException("EmployeeId is null")).when(attendanceService).getAttendanceByEmployee(null);
        ValidationException ex = assertThrows(ValidationException.class, () -> attendanceController.getAttendanceByEmployee(null));
        assertEquals("EmployeeId is null", ex.getMessage());
    }

    @Test
    public void testGetAttendanceByEmployee_NoRecords_ReturnsEmptyList() {
        when(attendanceService.getAttendanceByEmployee(2L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<AttendanceRecordDTO>> response = attendanceController.getAttendanceByEmployee(2L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }
}
