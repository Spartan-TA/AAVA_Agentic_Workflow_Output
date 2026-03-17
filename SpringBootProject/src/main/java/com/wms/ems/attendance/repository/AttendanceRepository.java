package com.wms.ems.attendance.repository;

import com.wms.ems.attendance.model.AttendanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeIdAndTimestampBetween(Long employeeId, LocalDateTime start, LocalDateTime end);
    List<AttendanceEvent> findByShiftId(Long shiftId);
}