package com.company.wms.attendance;

import com.company.wms.employee.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AttendanceEvent entity with custom queries for daily totals.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {

    /**
     * Find all attendance events for an employee on a specific date.
     * @param employee Employee
     * @param startOfDay Start of the day
     * @param endOfDay End of the day
     * @return List of AttendanceEvent
     */
    List<AttendanceEvent> findByEmployeeAndTimestampBetween(Employee employee, LocalDateTime startOfDay, LocalDateTime endOfDay);

    /**
     * Calculate total clock-in and clock-out events for an employee on a given date.
     */
    @Query("SELECT e FROM AttendanceEvent e WHERE e.employee = :employee AND e.timestamp BETWEEN :start AND :end")
    List<AttendanceEvent> findEventsForEmployeeOnDate(@Param("employee") Employee employee, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Find all attendance events for a date range.
     */
    List<AttendanceEvent> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
