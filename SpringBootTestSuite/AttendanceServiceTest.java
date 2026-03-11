package SpringBootTestSuite;

import com.example.ems.entity.Attendance;
import com.example.ems.entity.Employee;
import com.example.ems.service.AttendanceService;
import com.example.ems.repository.AttendanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AttendanceServiceTest {

    @Autowired
    private AttendanceService attendanceService;

    @MockBean
    private AttendanceRepository attendanceRepository;

    private Attendance attendance;
    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setBadgeId("BADGE123");
        employee.setName("John Doe");
        attendance = new Attendance();
        attendance.setId(1L);
        attendance.setEmployee(employee);
        attendance.setClockIn(LocalDateTime.now().minusHours(8));
        attendance.setClockOut(LocalDateTime.now());
        attendance.setShiftId(1L);
        attendance.setGeofence("ZoneA");
        attendance.setHoursWorked(8.0);
    }

    @Test
    void testGetAllAttendanceReturnsList() {
        Mockito.when(attendanceRepository.findAll()).thenReturn(Arrays.asList(attendance));
        assertEquals(1, attendanceService.getAllAttendance().size());
    }

    @Test
    void testGetAttendanceByIdSuccess() {
        Mockito.when(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance));
        Attendance found = attendanceService.getAttendanceById(1L);
        assertEquals(8.0, found.getHoursWorked());
    }

    @Test
    void testGetAttendanceByIdNotFoundThrowsException() {
        Mockito.when(attendanceRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> attendanceService.getAttendanceById(2L));
    }

    @Test
    void testCreateAttendanceSuccess() {
        Mockito.when(attendanceRepository.save(attendance)).thenReturn(attendance);
        Attendance created = attendanceService.createAttendance(attendance);
        assertEquals("ZoneA", created.getGeofence());
    }

    @Test
    void testCreateAttendanceNullFieldsThrowsException() {
        Attendance invalid = new Attendance();
        assertThrows(RuntimeException.class, () -> attendanceService.createAttendance(invalid));
    }

    @Test
    void testUpdateAttendanceSuccess() {
        Mockito.when(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance));
        attendance.setHoursWorked(7.5);
        Mockito.when(attendanceRepository.save(attendance)).thenReturn(attendance);
        Attendance updated = attendanceService.updateAttendance(1L, attendance);
        assertEquals(7.5, updated.getHoursWorked());
    }

    @Test
    void testUpdateAttendanceNotFoundThrowsException() {
        Mockito.when(attendanceRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> attendanceService.updateAttendance(2L, attendance));
    }

    @Test
    void testDeleteAttendanceSuccess() {
        Mockito.when(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance));
        assertDoesNotThrow(() -> attendanceService.deleteAttendance(1L));
    }

    @Test
    void testDeleteAttendanceNotFoundThrowsException() {
        Mockito.when(attendanceRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> attendanceService.deleteAttendance(2L));
    }

    @Test
    void testCreateAttendanceInvalidShiftIdThrowsException() {
        Attendance invalid = new Attendance();
        invalid.setShiftId(null);
        assertThrows(RuntimeException.class, () -> attendanceService.createAttendance(invalid));
    }

    @Test
    void testCreateAttendanceClockOutBeforeClockInThrowsException() {
        Attendance invalid = new Attendance();
        invalid.setClockIn(LocalDateTime.now());
        invalid.setClockOut(LocalDateTime.now().minusHours(1));
        assertThrows(RuntimeException.class, () -> attendanceService.createAttendance(invalid));
    }
}
