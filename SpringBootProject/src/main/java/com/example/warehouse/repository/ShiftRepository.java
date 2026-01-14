package com.example.warehouse.repository;

import com.example.warehouse.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Shift entity.
 */
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    @Query("SELECT s FROM Shift s WHERE s.date = :date")
    List<Shift> findByDate(@Param("date") LocalDate date);

    @Query("SELECT s FROM Shift s WHERE s.employee.id = :employeeId AND s.date BETWEEN :start AND :end")
    List<Shift> findByEmployeeAndDateRange(@Param("employeeId") Long employeeId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
