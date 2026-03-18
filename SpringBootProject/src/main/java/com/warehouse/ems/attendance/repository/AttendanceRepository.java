package com.warehouse.ems.attendance.repository;

import com.warehouse.ems.attendance.domain.AttendanceEvent;
import com.warehouse.ems.employee.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AttendanceEvent entity.
 * Provides CRUD operations and custom queries for attendance events.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {
    /**
     * Find attendance events for a specific employee.
     * @param employee Employee entity
     * @return List of AttendanceEvent
     */
    List<AttendanceEvent> findByEmployee(Employee employee);

    /**
     * Find attendance events for an employee within a date range.
     * @param employee Employee entity
     * @param start Start date/time
     * @param end End date/time
     * @return List of AttendanceEvent
     */
    List<AttendanceEvent> findByEmployeeAndClockInTimeBetween(Employee employee, LocalDateTime start, LocalDateTime end);
}
