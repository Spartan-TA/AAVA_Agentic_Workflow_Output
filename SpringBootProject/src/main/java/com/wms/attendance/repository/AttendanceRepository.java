package com.wms.attendance.repository;

import com.wms.attendance.domain.AttendanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for AttendanceEvent entity.
 * Provides CRUD operations and custom queries for attendance management.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeId(Long employeeId);
}
