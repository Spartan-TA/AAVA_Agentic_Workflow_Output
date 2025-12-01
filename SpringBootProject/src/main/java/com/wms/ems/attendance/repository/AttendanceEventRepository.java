package com.wms.ems.attendance.repository;

import com.wms.ems.attendance.entity.AttendanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for AttendanceEvent entity.
 * Provides CRUD operations and custom queries for attendance management.
 */
@Repository
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    /**
     * Find all attendance events for a specific employee.
     * @param employeeId the employee's ID
     * @return List of AttendanceEvent
     */
    List<AttendanceEvent> findByEmployeeId(Long employeeId);

    /**
     * Find all attendance events for a specific date.
     * @param eventDate the date of the event
     * @return List of AttendanceEvent
     */
    List<AttendanceEvent> findByEventDate(LocalDate eventDate);
}
