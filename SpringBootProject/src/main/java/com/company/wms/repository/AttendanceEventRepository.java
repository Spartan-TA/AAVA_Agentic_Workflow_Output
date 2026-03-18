package com.company.wms.repository;

import com.company.wms.domain.AttendanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for AttendanceEvent entity.
 */
@Repository
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeIdOrderByEventTimeDesc(Long employeeId);
}
