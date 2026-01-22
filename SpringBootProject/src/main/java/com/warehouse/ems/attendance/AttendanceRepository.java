package com.warehouse.ems.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for AttendanceEvent entity.
 * Provides basic CRUD and custom query methods for attendance management.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {

    /**
     * Find all attendance events for a given employee within a date range.
     * @param employeeId Employee ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of AttendanceEvent
     */
    @Query("SELECT a FROM AttendanceEvent a WHERE a.employeeId = :employeeId AND a.eventDate BETWEEN :startDate AND :endDate")
    List<AttendanceEvent> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    /**
     * Find all attendance events for a given date.
     * @param eventDate Date
     * @return List of AttendanceEvent
     */
    List<AttendanceEvent> findByEventDate(LocalDate eventDate);

    /**
     * Find all attendance events for a given employee.
     * @param employeeId Employee ID
     * @return List of AttendanceEvent
     */
    List<AttendanceEvent> findByEmployeeId(Long employeeId);
}
