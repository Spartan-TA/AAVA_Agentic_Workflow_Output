package com.warehouse.ems.attendance.repository;

import com.warehouse.ems.attendance.entity.AttendanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AttendanceEvent entity.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeId(Long employeeId);

    @Query("SELECT a FROM AttendanceEvent a WHERE a.employeeId = :employeeId AND a.eventTime BETWEEN :start AND :end")
    List<AttendanceEvent> findEventsForEmployeeInPeriod(Long employeeId, LocalDateTime start, LocalDateTime end);
}
