package com.example.warehouse.attendance;

import com.example.warehouse.attendance.model.AttendanceRecord;
import com.example.warehouse.attendance.model.AttendanceCorrection;
import com.example.warehouse.attendance.repository.AttendanceRepository;
import com.example.warehouse.attendance.repository.AttendanceCorrectionRepository;
import com.example.warehouse.employee.model.Employee;
import com.example.warehouse.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AttendanceRepositoryTest {
 @Autowired
 private AttendanceRepository attendanceRepository;
 @Autowired
 private AttendanceCorrectionRepository correctionRepository;
 @Autowired
 private EmployeeRepository employeeRepository;

 private Employee employee;
 private AttendanceRecord record1, record2;
 private AttendanceCorrection correction1, correction2;

 @BeforeEach
 void setUp() {
 employee = Employee.builder().badgeId("EMP001").name("John Doe").deleted(false).build();
 employeeRepository.save(employee);
 record1 = AttendanceRecord.builder()
 .employee(employee)
 .clockIn(LocalDateTime.now().minusHours(8))
 .clockOut(LocalDateTime.now().minusHours(4))
 .deviceInfo("Device1")
 .geoLocation("12.34,56.78")
 .build();
 record2 = AttendanceRecord.builder()
 .employee(employee)
 .clockIn(LocalDateTime.now().minusHours(3))
 .clockOut(LocalDateTime.now())
 .deviceInfo("Device2")
 .geoLocation("12.34,56.78")
 .build();
 attendanceRepository.saveAll(Arrays.asList(record1, record2));
 correction1 = AttendanceCorrection.builder()
 .employee(employee)
 .originalTime(record1.getClockIn())
 .correctedTime(record1.getClockIn().plusMinutes(5))
 .reason("Missed punch")
 .status("PENDING")
 .build();
 correction2 = AttendanceCorrection.builder()
 .employee(employee)
 .originalTime(record2.getClockIn())
 .correctedTime(record2.getClockIn().plusMinutes(10))
 .reason("Late arrival")
 .status("APPROVED")
 .build();
 correctionRepository.saveAll(Arrays.asList(correction1, correction2));
 }

 @AfterEach
 void tearDown() {
 correctionRepository.deleteAll();
 attendanceRepository.deleteAll();
 employeeRepository.deleteAll();
 }

 @Test
 @DisplayName("findTopByEmployeeOrderByClockInDesc returns latest record")
 void testFindTopByEmployeeOrderByClockInDesc() {
 Optional<AttendanceRecord> result = attendanceRepository.findTopByEmployeeOrderByClockInDesc(employee);
 assertTrue(result.isPresent());
 assertEquals(record2.getClockIn(), result.get().getClockIn());
 }

 @Test
 @DisplayName("findAllByEmployee returns all records for employee")
 void testFindAllByEmployee() {
 List<AttendanceRecord> records = attendanceRepository.findAllByEmployee(employee);
 assertEquals(2, records.size());
 }

 @Test
 @DisplayName("findByEmployeeAndClockInBetween returns records in range")
 void testFindByEmployeeAndClockInBetween() {
 LocalDateTime start = LocalDateTime.now().minusHours(9);
 LocalDateTime end = LocalDateTime.now();
 List<AttendanceRecord> records = attendanceRepository.findByEmployeeAndClockInBetween(employee, start, end);
 assertEquals(2, records.size());
 }

 @Test
 @DisplayName("findByEmployeeAndClockInBetween returns empty for out of range")
 void testFindByEmployeeAndClockInBetween_Empty() {
 LocalDateTime start = LocalDateTime.now().minusDays(2);
 LocalDateTime end = LocalDateTime.now().minusDays(1);
 List<AttendanceRecord> records = attendanceRepository.findByEmployeeAndClockInBetween(employee, start, end);
 assertTrue(records.isEmpty());
 }

 @Test
 @DisplayName("findByEmployee returns corrections for employee")
 void testFindCorrectionsByEmployee() {
 List<AttendanceCorrection> corrections = correctionRepository.findByEmployee(employee);
 assertEquals(2, corrections.size());
 }

 @Test
 @DisplayName("findByStatus returns corrections by status")
 void testFindCorrectionsByStatus() {
 List<AttendanceCorrection> pending = correctionRepository.findByStatus("PENDING");
 assertEquals(1, pending.size());
 assertEquals("Missed punch", pending.get(0).getReason());
 }

 @Test
 @DisplayName("findByEmployeeAndStatus returns corrections by employee and status")
 void testFindCorrectionsByEmployeeAndStatus() {
 List<AttendanceCorrection> approved = correctionRepository.findByEmployeeAndStatus(employee, "APPROVED");
 assertEquals(1, approved.size());
 assertEquals("Late arrival", approved.get(0).getReason());
 }

 @Test
 @DisplayName("Handles null and empty inputs gracefully")
 void testRepositoryMethods_NullAndEmptyInputs() {
 assertThrows(Exception.class, () -> attendanceRepository.findTopByEmployeeOrderByClockInDesc(null));
 assertThrows(Exception.class, () -> attendanceRepository.findAllByEmployee(null));
 assertThrows(Exception.class, () -> attendanceRepository.findByEmployeeAndClockInBetween(null, null, null));
 assertThrows(Exception.class, () -> correctionRepository.findByEmployee(null));
 assertThrows(Exception.class, () -> correctionRepository.findByStatus(null));
 assertThrows(Exception.class, () -> correctionRepository.findByEmployeeAndStatus(null, null));
 }

 @Test
 @DisplayName("Handles boundary conditions for deviceInfo (very long string)")
 void testAttendanceRecord_DeviceInfoBoundary() {
 String longDevice = "D" + "X".repeat(255);
 AttendanceRecord rec = AttendanceRecord.builder().employee(employee).clockIn(LocalDateTime.now()).deviceInfo(longDevice).geoLocation("0,0").build();
 attendanceRepository.save(rec);
 List<AttendanceRecord> records = attendanceRepository.findAllByEmployee(employee);
 assertTrue(records.stream().anyMatch(r -> longDevice.equals(r.getDeviceInfo())));
 }

 @Test
 @DisplayName("Handles special characters in deviceInfo")
 void testAttendanceRecord_DeviceInfoSpecialChars() {
 String specialDevice = "DEV@#$_!";
 AttendanceRecord rec = AttendanceRecord.builder().employee(employee).clockIn(LocalDateTime.now()).deviceInfo(specialDevice).geoLocation("0,0").build();
 attendanceRepository.save(rec);
 List<AttendanceRecord> records = attendanceRepository.findAllByEmployee(employee);
 assertTrue(records.stream().anyMatch(r -> specialDevice.equals(r.getDeviceInfo())));
 }
}
