package com.wms.attendance.repositories;

import com.wms.attendance.model.AttendanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for AttendanceEvent entity.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeIdOrderByEventTimeDesc(Long employeeId);
}
