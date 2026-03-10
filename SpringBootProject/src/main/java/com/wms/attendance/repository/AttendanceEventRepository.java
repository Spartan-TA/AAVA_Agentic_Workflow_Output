package com.wms.attendance.repository;

import com.wms.attendance.entity.AttendanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AttendanceEvent entity.
 */
@Repository
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeIdAndEventTimeBetween(Long employeeId, LocalDateTime start, LocalDateTime end);
    List<AttendanceEvent> findByEmployeeId(Long employeeId);
}
