package com.example.warehouse.repository;

import com.example.warehouse.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Schedule entity.
 */
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Query("SELECT s FROM Schedule s WHERE s.employee.id = :employeeId AND s.date BETWEEN :start AND :end")
    List<Schedule> findByEmployeeAndDateRange(@Param("employeeId") Long employeeId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT s FROM Schedule s WHERE s.date = :date")
    List<Schedule> findByDate(@Param("date") LocalDate date);
}
