package com.company.warehouse.attendance.repository;

import com.company.warehouse.attendance.entity.AttendanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for AttendanceEvent entity CRUD operations.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeId(Long employeeId);
}
