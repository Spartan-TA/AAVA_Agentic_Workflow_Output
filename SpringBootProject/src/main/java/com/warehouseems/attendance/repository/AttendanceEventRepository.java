package com.warehouseems.attendance.repository;

import com.warehouseems.attendance.entity.AttendanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for AttendanceEvent entity.
 */
@Repository
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    @Query("SELECT a FROM AttendanceEvent a WHERE a.employeeId = :employeeId AND a.timestamp BETWEEN :startOfDay AND :endOfDay")
    List<AttendanceEvent> findTodayEvents(@Param("employeeId") Long employeeId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT a FROM AttendanceEvent a WHERE a.employeeId = :employeeId ORDER BY a.timestamp DESC")
    List<AttendanceEvent> findLastEvent(@Param("employeeId") Long employeeId);

    List<AttendanceEvent> findByEmployeeIdAndTimestampBetween(Long employeeId, LocalDateTime start, LocalDateTime end);
}
