package com.wms.core.repository;

import com.wms.core.domain.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEmployeeIdAndClockInBetween(Long employeeId, LocalDateTime start, LocalDateTime end);
    List<Attendance> findByEmployeeIdAndApprovedFalse(Long employeeId);
}