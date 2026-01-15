package com.company.wms.attendance.repository;

import com.company.wms.attendance.domain.AttendanceEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AttendanceEvent entity.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeIdOrderByEventTimeDesc(Long employeeId);

    @Query("SELECT a FROM AttendanceEvent a WHERE a.employeeId = :employeeId AND a.eventTime BETWEEN :start AND :end ORDER BY a.eventTime DESC")
    List<AttendanceEvent> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    Page<AttendanceEvent> findAllByEmployeeId(Long employeeId, Pageable pageable);
}
