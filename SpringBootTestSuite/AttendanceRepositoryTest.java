package com.wms.employee.repository;

import com.wms.employee.entity.Attendance;
import com.wms.employee.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class AttendanceRepositoryTest {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee;
    private Attendance attendance;

    @BeforeEach
    void setUp() {
        employee = new Employee(null, "B123", "John Doe", "Worker", "Logistics", "A", null, "ACTIVE", false);
        employeeRepository.save(employee);

        attendance = new Attendance(null, employee, LocalDateTime.now().minusHours(2), null, "Morning", "D1", "Main Gate", "PRESENT");
        attendanceRepository.save(attendance);
    }

    @Test
    void testFindByEmployeeAndClockInBetween_ReturnsAttendance() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        List<Attendance> result = attendanceRepository.findByEmployeeAndClockInBetween(employee, start, end);
        assertEquals(1, result.size());
    }

    @Test
    void testFindByEmployeeAndClockOutIsNull_ReturnsOpenAttendance() {
        List<Attendance> result = attendanceRepository.findByEmployeeAndClockOutIsNull(employee);
        assertEquals(1, result.size());
        assertNull(result.get(0).getClockOut());
    }

    @Test
    void testFindByEmployeeAndClockOutIsNull_NoOpenAttendance_ReturnsEmpty() {
        attendance.setClockOut(LocalDateTime.now());
        attendanceRepository.save(attendance);
        List<Attendance> result = attendanceRepository.findByEmployeeAndClockOutIsNull(employee);
        assertEquals(0, result.size());
    }
}