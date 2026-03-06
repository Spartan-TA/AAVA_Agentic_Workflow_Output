package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AttendanceServiceTest {
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testClockIn_validInput() {
        Employee emp = new Employee();
        emp.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        Attendance att = new Attendance();
        att.setId(10L);
        att.setEmployee(emp);
        att.setClockInTime(LocalDateTime.now());
        att.setDeviceInfo("DeviceA");
        att.setGeofenceLocation("Zone1");
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(att);
        Attendance result = attendanceService.clockIn(1L, "DeviceA", "Zone1");
        assertEquals("DeviceA", result.getDeviceInfo());
        assertEquals("Zone1", result.getGeofenceLocation());
        assertEquals(emp, result.getEmployee());
    }

    @Test
    void testClockIn_nullEmployeeId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(null, "DeviceA", "Zone1"));
        assertEquals("Employee ID required", ex.getMessage());
    }

    @Test
    void testClockIn_emptyGeofenceLocation() {
        Employee emp = new Employee();
        emp.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(1L, "DeviceA", ""));
        assertEquals("Geofence location required", ex.getMessage());
    }

    @Test
    void testClockIn_nullGeofenceLocation() {
        Employee emp = new Employee();
        emp.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(1L, "DeviceA", null));
        assertEquals("Geofence location required", ex.getMessage());
    }

    @Test
    void testClockIn_employeeNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.clockIn(99L, "DeviceA", "Zone1"));
    }

    @Test
    void testClockOut_validInput() {
        Attendance att = new Attendance();
        att.setId(10L);
        att.setClockInTime(LocalDateTime.now().minusHours(2));
        att.setClockOutTime(null);
        when(attendanceRepository.findById(10L)).thenReturn(Optional.of(att));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance a = invocation.getArgument(0);
            return a;
        });
        Attendance result = attendanceService.clockOut(10L);
        assertNotNull(result.getClockOutTime());
        assertTrue(result.getHoursWorked() > 0);
    }

    @Test
    void testClockOut_attendanceNotFound() {
        when(attendanceRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.clockOut(99L));
    }

    @Test
    void testClockOut_alreadyClockedOut() {
        Attendance att = new Attendance();
        att.setId(10L);
        att.setClockInTime(LocalDateTime.now().minusHours(2));
        att.setClockOutTime(LocalDateTime.now());
        when(attendanceRepository.findById(10L)).thenReturn(Optional.of(att));
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> attendanceService.clockOut(10L));
        assertEquals("Already clocked out", ex.getMessage());
    }

    @Test
    void testCalculateHours_nullStartOrEnd() {
        AttendanceService svc = new AttendanceService();
        Double hours = svc.calculateHours(null, LocalDateTime.now());
        assertEquals(0.0, hours);
        hours = svc.calculateHours(LocalDateTime.now(), null);
        assertEquals(0.0, hours);
    }

    // DTO and Exception classes for test compilation
    static class Employee {
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }
    static class Attendance {
        private Long id;
        private Employee employee;
        private LocalDateTime clockInTime;
        private LocalDateTime clockOutTime;
        private String deviceInfo;
        private String geofenceLocation;
        private Double hoursWorked;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Employee getEmployee() { return employee; }
        public void setEmployee(Employee employee) { this.employee = employee; }
        public LocalDateTime getClockInTime() { return clockInTime; }
        public void setClockInTime(LocalDateTime clockInTime) { this.clockInTime = clockInTime; }
        public LocalDateTime getClockOutTime() { return clockOutTime; }
        public void setClockOutTime(LocalDateTime clockOutTime) { this.clockOutTime = clockOutTime; }
        public String getDeviceInfo() { return deviceInfo; }
        public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
        public String getGeofenceLocation() { return geofenceLocation; }
        public void setGeofenceLocation(String geofenceLocation) { this.geofenceLocation = geofenceLocation; }
        public Double getHoursWorked() { return hoursWorked; }
        public void setHoursWorked(Double hoursWorked) { this.hoursWorked = hoursWorked; }
    }
    static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String msg) { super(msg); }
    }
    interface AttendanceRepository {
        Optional<Attendance> findById(Long id);
        Attendance save(Attendance attendance);
    }
    interface EmployeeRepository {
        Optional<Employee> findById(Long id);
    }
    static class AttendanceService {
        private AttendanceRepository attendanceRepository;
        private EmployeeRepository employeeRepository;
        public Attendance clockIn(Long employeeId, String deviceInfo, String geofenceLocation) {
            if (employeeId == null) throw new IllegalArgumentException("Employee ID required");
            if (geofenceLocation == null || geofenceLocation.isEmpty()) {
                throw new IllegalArgumentException("Geofence location required");
            }
            Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
            Attendance attendance = new Attendance();
            attendance.setEmployee(employee);
            attendance.setClockInTime(LocalDateTime.now());
            attendance.setDeviceInfo(deviceInfo);
            attendance.setGeofenceLocation(geofenceLocation);
            return attendanceRepository.save(attendance);
        }
        public Attendance clockOut(Long attendanceId) {
            Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));
            if (attendance.getClockOutTime() != null) {
                throw new IllegalStateException("Already clocked out");
            }
            attendance.setClockOutTime(LocalDateTime.now());
            attendance.setHoursWorked(calculateHours(attendance.getClockInTime(), attendance.getClockOutTime()));
            return attendanceRepository.save(attendance);
        }
        public Double calculateHours(LocalDateTime start, LocalDateTime end) {
            if (start == null || end == null) return 0.0;
            return java.time.Duration.between(start, end).toMinutes() / 60.0;
        }
    }
}
