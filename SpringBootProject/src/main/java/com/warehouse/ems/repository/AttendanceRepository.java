package com.warehouse.ems.repository;

import com.warehouse.ems.domain.AttendanceRecord;
import com.warehouse.ems.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AttendanceRecord entity.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByEmployeeAndClockInBetween(Employee employee, LocalDateTime start, LocalDateTime end);
    List<AttendanceRecord> findByCorrectionRequestedTrue();
}
