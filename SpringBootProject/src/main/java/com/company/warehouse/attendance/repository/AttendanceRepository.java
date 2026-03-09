package com.company.warehouse.attendance.repository;

import com.company.warehouse.attendance.entity.AttendanceEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    Page<AttendanceEvent> findByEmployeeId(Long employeeId, Pageable pageable);

    @Query("SELECT a FROM AttendanceEvent a WHERE a.employeeId = :employeeId AND a.eventTime BETWEEN :start AND :end")
    List<AttendanceEvent> findEventsForEmployeeInRange(Long employeeId, LocalDateTime start, LocalDateTime end);

    Page<AttendanceEvent> findByEventType(AttendanceEvent.EventType eventType, Pageable pageable);
}
