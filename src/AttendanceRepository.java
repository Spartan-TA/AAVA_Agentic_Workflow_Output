package com.company.wms.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for AttendanceEvent entity with custom queries.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {
    /**
     * Find all attendance events for an employee on a specific date.
     * @param employeeId Employee ID
     * @param date Date
     * @return List of AttendanceEvent
     */
    @Query("SELECT a FROM AttendanceEvent a WHERE a.employeeId = :employeeId AND DATE(a.timestamp) = :date")
    List<AttendanceEvent> findByEmployeeIdAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    /**
     * Get daily attendance totals for all employees.
     * @param date Date
     * @return List of AttendanceEvent
     */
    @Query("SELECT a FROM AttendanceEvent a WHERE DATE(a.timestamp) = :date")
    List<AttendanceEvent> findAllByDate(@Param("date") LocalDate date);
}
