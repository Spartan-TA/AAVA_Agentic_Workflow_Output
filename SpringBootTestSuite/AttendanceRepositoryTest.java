package com.warehouse.management.repository;

import com.warehouse.management.entity.Attendance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive repository tests for AttendanceRepository
 * Tests custom queries, aggregations, and edge cases
 */
@DataJpaTest
@ActiveProfiles("test")
public class AttendanceRepositoryTest {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @BeforeEach
    void setUp() {
        attendanceRepository.deleteAll();
        Attendance att1 = new Attendance(100L, LocalDate.of(2024, 6, 1), true);
        Attendance att2 = new Attendance(100L, LocalDate.of(2024, 6, 2), false);
        Attendance att3 = new Attendance(101L, LocalDate.of(2024, 6, 1), true);
        attendanceRepository.save(att1);
        attendanceRepository.save(att2);
        attendanceRepository.save(att3);
    }

    @Test
    @DisplayName("Test findByEmployeeId returns correct attendances")
    void testFindByEmployeeId_ReturnsAttendances() {
        List<Attendance> result = attendanceRepository.findByEmployeeId(100L);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Test findByEmployeeId with no records returns empty list")
    void testFindByEmployeeId_NoRecords_ReturnsEmpty() {
        List<Attendance> result = attendanceRepository.findByEmployeeId(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test countPresentByEmployeeId returns correct count")
    void testCountPresentByEmployeeId_ReturnsCount() {
        long count = attendanceRepository.countPresentByEmployeeId(100L);
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Test countPresentByEmployeeId with no presents returns zero")
    void testCountPresentByEmployeeId_NoPresent_ReturnsZero() {
        long count = attendanceRepository.countPresentByEmployeeId(999L);
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Test findByDate returns correct attendances")
    void testFindByDate_ReturnsAttendances() {
        List<Attendance> result = attendanceRepository.findByDate(LocalDate.of(2024, 6, 1));
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Test save and retrieve attendance")
    void testSaveAndRetrieveAttendance() {
        Attendance att = new Attendance(102L, LocalDate.of(2024, 6, 3), true);
        attendanceRepository.save(att);
        List<Attendance> result = attendanceRepository.findByEmployeeId(102L);
        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2024, 6, 3), result.get(0).getDate());
    }

    @Test
    @DisplayName("Test delete attendance")
    void testDeleteAttendance() {
        List<Attendance> att = attendanceRepository.findByEmployeeId(100L);
        assertFalse(att.isEmpty());
        attendanceRepository.delete(att.get(0));
        List<Attendance> result = attendanceRepository.findByEmployeeId(100L);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test findByEmployeeId with null input throws exception")
    void testFindByEmployeeId_NullInput_ThrowsException() {
        assertThrows(Exception.class, () -> attendanceRepository.findByEmployeeId(null));
    }

    @Test
    @DisplayName("Test save attendance with null date throws exception")
    void testSaveAttendance_NullDate_ThrowsException() {
        Attendance att = new Attendance(103L, null, true);
        assertThrows(Exception.class, () -> attendanceRepository.save(att));
    }
}
