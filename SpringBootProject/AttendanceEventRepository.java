package com.example.warehousemanagement.repository;

import com.example.warehousemanagement.entity.AttendanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for AttendanceEvent entity.
 */
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    List<AttendanceEvent> findByEmployeeId(Long employeeId);
    List<AttendanceEvent> findByEventDate(LocalDate eventDate);
    @Query("SELECT a FROM AttendanceEvent a WHERE a.eventDate BETWEEN :start AND :end")
    List<AttendanceEvent> findByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
