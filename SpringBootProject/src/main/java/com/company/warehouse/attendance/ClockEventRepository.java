package com.company.warehouse.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ClockEvent entity.
 */
public interface ClockEventRepository extends JpaRepository<ClockEvent, Long> {
    List<ClockEvent> findByEmployeeIdAndTimestampBetween(Long employeeId, LocalDateTime start, LocalDateTime end);
    @Query("SELECT ce FROM ClockEvent ce WHERE ce.employeeId = :employeeId AND ce.timestamp >= :start AND ce.timestamp <= :end ORDER BY ce.timestamp ASC")
    List<ClockEvent> findEventsForPeriod(@Param("employeeId") Long employeeId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
