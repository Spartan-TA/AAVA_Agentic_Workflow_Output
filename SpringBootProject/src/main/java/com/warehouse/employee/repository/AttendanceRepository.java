package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Attendance;
import com.warehouse.employee.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Attendance entity.
 */
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    /**
     * Find attendance records for an employee between two times.
     */
    List<Attendance> findByEmployeeAndClockInTimeBetween(Employee employee, LocalDateTime start, LocalDateTime end);

    /**
     * Find attendance records for an employee by status.
     */
    List<Attendance> findByEmployeeAndStatus(Employee employee, String status);

    /**
     * Calculate total hours worked by employee in a date range.
     */
    @Query("SELECT SUM(a.hoursWorked) FROM Attendance a WHERE a.employee = :employee AND a.clockInTime BETWEEN :start AND :end")
    Double calculateTotalHoursByEmployeeAndDateRange(@Param("employee") Employee employee, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
