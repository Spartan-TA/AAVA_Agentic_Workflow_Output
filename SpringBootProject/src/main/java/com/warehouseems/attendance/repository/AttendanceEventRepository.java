package com.warehouseems.attendance.repository;

import com.warehouseems.attendance.model.AttendanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA repository for AttendanceEvent entity.
 */
@Repository
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeIdAndTimestampBetween(Long employeeId, LocalDateTime start, LocalDateTime end);
    List<AttendanceEvent> findByEmployeeId(Long employeeId);
    List<AttendanceEvent> findByType(AttendanceEvent.EventType type);
}