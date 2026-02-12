package com.example.warehouse.attendance;

import com.example.warehouse.attendance.model.AttendanceRecord;
import com.example.warehouse.attendance.model.AttendanceCorrection;
import com.example.warehouse.attendance.repository.AttendanceRepository;
import com.example.warehouse.attendance.repository.AttendanceCorrectionRepository;
import com.example.warehouse.attendance.service.AttendanceService;
import com.example.warehouse.employee.model.Employee;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {
 @Mock
 private AttendanceRepository attendanceRepository;
 @Mock
 private AttendanceCorrectionRepository correctionRepository;
 @InjectMocks
 private AttendanceService attendanceService;

 private Employee employee;
 private AttendanceRecord clockInRecord;
 private AttendanceCorrection correction;

 @BeforeEach
 void setUp() {
 employee = Employee.builder().id(1L).badgeId("EMP001").name("John Doe").build();
 clockInRecord = AttendanceRecord.builder()
 .id(1L)
 .employee(employee)
 .clockIn(LocalDateTime.now().minusHours(8))
 .clockOut(LocalDateTime.now())
 .deviceInfo("Device1")
 .geoLocation("12.34,56.78")
 .build();
 correction = AttendanceCorrection.builder()
 .id(1L)
 .employee(employee)
 .originalTime(LocalDateTime.now().minusHours(8))
 .correctedTime(LocalDateTime.now().minusHours(7))
 .reason("Missed punch")
 .status("PENDING")
 .build();
 }

 @AfterEach
 void tearDown() {
 Mockito.reset(attendanceRepository, correctionRepository);
 }

 @Test
 @DisplayName("Should clock in with valid data")
 void testClockIn_Valid() {
 when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(clockInRecord);
 AttendanceRecord result = attendanceService.clockIn(employee, "Device1", "12.34,56.78");
 assertNotNull(result);
 assertEquals(employee, result.getEmployee());
 verify(attendanceRepository).save(any(AttendanceRecord.class));
 }

 @Test
 @DisplayName("Should throw exception when employee is null on clock in")
 void testClockIn_NullEmployee() {
 assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(null, "Device1", "12.34,56.78"));
 }

 @Test
 @DisplayName("Should throw exception when device info is empty on clock in")
 void testClockIn_EmptyDeviceInfo() {
 assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(employee, "", "12.34,56.78"));
 }

 @Test
 @DisplayName("Should clock out with valid data")
 void testClockOut_Valid() {
 when(attendanceRepository.findTopByEmployeeOrderByClockInDesc(employee)).thenReturn(Optional.of(clockInRecord));
 when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(clockInRecord);
 AttendanceRecord result = attendanceService.clockOut(employee, "Device1", "12.34,56.78");
 assertNotNull(result);
 assertEquals(employee, result.getEmployee());
 verify(attendanceRepository).save(any(AttendanceRecord.class));
 }

 @Test
 @DisplayName("Should throw exception when no clock in record found on clock out")
 void testClockOut_NoClockIn() {
 when(attendanceRepository.findTopByEmployeeOrderByClockInDesc(employee)).thenReturn(Optional.empty());
 assertThrows(NoSuchElementException.class, () -> attendanceService.clockOut(employee, "Device1", "12.34,56.78"));
 }

 @Test
 @DisplayName("Should submit correction with valid data")
 void testSubmitCorrection_Valid() {
 when(correctionRepository.save(any(AttendanceCorrection.class))).thenReturn(correction);
 AttendanceCorrection result = attendanceService.submitCorrection(employee, correction.getOriginalTime(), correction.getCorrectedTime(), "Missed punch");
 assertNotNull(result);
 assertEquals("PENDING", result.getStatus());
 verify(correctionRepository).save(any(AttendanceCorrection.class));
 }

 @Test
 @DisplayName("Should throw exception when submitting correction with null employee")
 void testSubmitCorrection_NullEmployee() {
 assertThrows(IllegalArgumentException.class, () -> attendanceService.submitCorrection(null, correction.getOriginalTime(), correction.getCorrectedTime(), "Missed punch"));
 }

 @Test
 @DisplayName("Should throw exception when reason is empty in correction")
 void testSubmitCorrection_EmptyReason() {
 assertThrows(IllegalArgumentException.class, () -> attendanceService.submitCorrection(employee, correction.getOriginalTime(), correction.getCorrectedTime(), ""));
 }

 @Test
 @DisplayName("Should approve correction")
 void testApproveCorrection() {
 when(correctionRepository.findById(1L)).thenReturn(Optional.of(correction));
 when(correctionRepository.save(any(AttendanceCorrection.class))).thenReturn(correction);
 AttendanceCorrection result = attendanceService.approveCorrection(1L);
 assertNotNull(result);
 assertEquals("APPROVED", result.getStatus());
 verify(correctionRepository).save(any(AttendanceCorrection.class));
 }

 @Test
 @DisplayName("Should reject correction")
 void testRejectCorrection() {
 when(correctionRepository.findById(1L)).thenReturn(Optional.of(correction));
 when(correctionRepository.save(any(AttendanceCorrection.class))).thenReturn(correction);
 AttendanceCorrection result = attendanceService.rejectCorrection(1L);
 assertNotNull(result);
 assertEquals("REJECTED", result.getStatus());
 verify(correctionRepository).save(any(AttendanceCorrection.class));
 }

 @Test
 @DisplayName("Should throw exception when approving non-existent correction")
 void testApproveCorrection_NotFound() {
 when(correctionRepository.findById(2L)).thenReturn(Optional.empty());
 assertThrows(NoSuchElementException.class, () -> attendanceService.approveCorrection(2L));
 }

 @Test
 @DisplayName("Should throw exception when rejecting non-existent correction")
 void testRejectCorrection_NotFound() {
 when(correctionRepository.findById(2L)).thenReturn(Optional.empty());
 assertThrows(NoSuchElementException.class, () -> attendanceService.rejectCorrection(2L));
 }

 @Test
 @DisplayName("Should handle boundary conditions for deviceInfo (very long string)")
 void testClockIn_DeviceInfoBoundary() {
 String longDevice = "D" + "X".repeat(255);
 when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(clockInRecord);
 AttendanceRecord result = attendanceService.clockIn(employee, longDevice, "12.34,56.78");
 assertNotNull(result);
 }

 @Test
 @DisplayName("Should handle special characters in deviceInfo")
 void testClockIn_DeviceInfoSpecialChars() {
 String specialDevice = "DEV@#$_!";
 when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(clockInRecord);
 AttendanceRecord result = attendanceService.clockIn(employee, specialDevice, "12.34,56.78");
 assertNotNull(result);
 }
}
