package com.warehouse.ems.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for AttendanceEvent entity.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeId(Long employeeId);
    List<AttendanceEvent> findByShiftId(Long shiftId);
}
