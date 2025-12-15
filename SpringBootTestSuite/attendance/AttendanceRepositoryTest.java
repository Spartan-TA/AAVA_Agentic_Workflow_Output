package com.example.ems.attendance;

import com.example.ems.attendance.entity.AttendanceEntity;
import com.example.ems.attendance.repository.AttendanceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class AttendanceRepositoryTest {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Test
    @DisplayName("findByEmployeeAndDate - Normal Case")
    void testFindByEmployeeAndDate() {
        AttendanceEntity entity = new AttendanceEntity();
        entity.setEmployeeId(1L);
        entity.setDate(LocalDate.of(2023, 1, 1));
        attendanceRepository.save(entity);

        List<AttendanceEntity> results = attendanceRepository.findByEmployeeIdAndDate(1L, LocalDate.of(2023, 1, 1));
        assertFalse(results.isEmpty());
        assertEquals(1L, results.get(0).getEmployeeId());
    }

    @Test
    @DisplayName("findByEmployeeAndDate - Not Found")
    void testFindByEmployeeAndDateNotFound() {
        List<AttendanceEntity> results = attendanceRepository.findByEmployeeIdAndDate(99L, LocalDate.of(2023, 1, 1));
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("findByDateRange - Normal Case")
    void testFindByDateRange() {
        AttendanceEntity entity = new AttendanceEntity();
        entity.setEmployeeId(2L);
        entity.setDate(LocalDate.of(2023, 1, 5));
        attendanceRepository.save(entity);

        List<AttendanceEntity> results = attendanceRepository.findByDateBetween(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31));
        assertFalse(results.isEmpty());
    }

    @Test
    @DisplayName("findByDateRange - Empty Range")
    void testFindByDateRangeEmpty() {
        List<AttendanceEntity> results = attendanceRepository.findByDateBetween(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 31));
        assertTrue(results.isEmpty());
    }
}
