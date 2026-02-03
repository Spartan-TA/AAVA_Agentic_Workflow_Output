package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.*;

@DataJpaTest
class AttendanceRepositoryTest {
    @Autowired
    private AttendanceRepository attendanceRepository;

    private Attendance attendance;

    @BeforeEach
    void setUp() {
        attendance = new Attendance();
        attendance.setEmployeeId(1L);
        attendance.setDate(LocalDate.now());
        attendance.setStatus("Present");
    }

    @AfterEach
    void tearDown() {
        attendanceRepository.deleteAll();
    }

    @Test
    void saveAttendance_ValidAttendance_Success() {
        Attendance saved = attendanceRepository.save(attendance);
        Assertions.assertNotNull(saved.getId());
    }

    @Test
    void findByEmployeeId_ExistingEmployee_ReturnsAttendanceList() {
        attendanceRepository.save(attendance);
        List<Attendance> list = attendanceRepository.findByEmployeeId(1L);
        Assertions.assertFalse(list.isEmpty());
    }

    @Test
    void saveAttendance_NullAttendance_ThrowsException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> attendanceRepository.save(null));
    }

    @Test
    void saveAttendance_DuplicateAttendance_ThrowsException() {
        attendanceRepository.save(attendance);
        Attendance duplicate = new Attendance();
        duplicate.setEmployeeId(1L);
        duplicate.setDate(attendance.getDate());
        duplicate.setStatus("Present");
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> attendanceRepository.saveAndFlush(duplicate));
    }

    @Test
    void saveAttendance_EmptyStatus_ThrowsException() {
        attendance.setStatus("");
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> attendanceRepository.saveAndFlush(attendance));
    }
}
