package com.wms.ems.repository;

import com.wms.ems.entity.AttendanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AttendanceEvent entity operations.
 * Provides CRUD and custom query methods for attendance event management.
 */
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    /**
     * Find attendance events by employee ID and timestamp range.
     * @param employeeId the employee ID
     * @param start start timestamp
     * @param end end timestamp
     * @return List of AttendanceEvents
     */
    List<AttendanceEvent> findByEmployeeIdAndTimestampBetween(Long employeeId, LocalDateTime start, LocalDateTime end);

    /**
     * Find attendance events by shift ID.
     * @param shiftId the shift ID
     * @return List of AttendanceEvents
     */
    List<AttendanceEvent> findByShiftId(Long shiftId);

    /**
     * Custom query to get daily totals for an employee.
     * @param employeeId the employee ID
     * @param date the date (start of day)
     * @param nextDate the next date (start of next day)
     * @return List of AttendanceEvents
     */
    @Query("SELECT ae FROM AttendanceEvent ae WHERE ae.employee.id = :employeeId AND ae.timestamp >= :date AND ae.timestamp < :nextDate")
    List<AttendanceEvent> findDailyEvents(@Param("employeeId") Long employeeId, @Param("date") LocalDateTime date, @Param("nextDate") LocalDateTime nextDate);
}
