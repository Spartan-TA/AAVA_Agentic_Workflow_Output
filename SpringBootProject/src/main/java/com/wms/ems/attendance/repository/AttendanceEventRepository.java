package com.wms.ems.attendance.repository;

import com.wms.ems.attendance.entity.AttendanceEvent;
import com.wms.ems.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AttendanceEvent entity operations.
 * Provides CRUD operations and custom queries for attendance event management.
 */
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {

    /**
     * Finds attendance events for an employee between two clock-in times.
     * @param employee the employee
     * @param start the start time
     * @param end the end time
     * @return a list of attendance events
     */
    List<AttendanceEvent> findByEmployeeAndClockInBetween(Employee employee, LocalDateTime start, LocalDateTime end);

    /**
     * Finds attendance events for an employee where clock-out is null.
     * @param employeeId the employee ID
     * @return a list of attendance events with null clock-out
     */
    List<AttendanceEvent> findByEmployeeIdAndClockOutIsNull(Long employeeId);
}
