package com.example.warehouse.repository;

import com.example.warehouse.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Attendance entity with clock-in/out queries.
 */
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.date = :date")
    List<Attendance> findByEmployeeAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE a.clockOutTime IS NULL AND a.employee.id = :employeeId")
    List<Attendance> findOpenClockInByEmployee(@Param("employeeId") Long employeeId);
}
