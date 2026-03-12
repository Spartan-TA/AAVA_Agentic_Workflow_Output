package SpringBootTestSuite;

import com.example.warehouse.model.AttendanceRecord;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AttendanceRecord entity logic.
 * Tests calculation of hours worked and update hooks.
 */
class AttendanceRecordTest {
    private AttendanceRecord record;

    @BeforeEach
    void setUp() {
        record = new AttendanceRecord();
        record.setClockInTime(LocalDateTime.of(2024, 1, 1, 8, 0));
        record.setClockOutTime(LocalDateTime.of(2024, 1, 1, 16, 0));
    }

    @Test
    void testCalculateHoursWorked_ValidTimes_ReturnsCorrectHours() {
        double hours = record.calculateHoursWorked();
        assertEquals(8.0, hours, 0.01);
    }

    @Test
    void testCalculateHoursWorked_NullClockOut_ReturnsZero() {
        record.setClockOutTime(null);
        double hours = record.calculateHoursWorked();
        assertEquals(0.0, hours, 0.01);
    }

    @Test
    void testCalculateHoursWorked_NullClockIn_ReturnsZero() {
        record.setClockInTime(null);
        double hours = record.calculateHoursWorked();
        assertEquals(0.0, hours, 0.01);
    }

    @Test
    void testOnUpdate_SetsHoursWorked() {
        record.setHoursWorked(0.0);
        record.onUpdate();
        assertEquals(8.0, record.getHoursWorked(), 0.01);
    }
}
