package com.example.warehouse.attendance.repository;

import com.example.warehouse.attendance.entity.AttendanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {
    // Find all events for an employee
    List<AttendanceEvent> findByEmployeeId(Long employeeId);

    // Find all events for an employee between two dates
    List<AttendanceEvent> findByEmployeeIdAndEventTimeBetween(Long employeeId, LocalDateTime start, LocalDateTime end);
}
