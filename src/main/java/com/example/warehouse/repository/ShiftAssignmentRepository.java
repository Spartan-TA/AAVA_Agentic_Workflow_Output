package com.example.warehouse.repository;

import com.example.warehouse.entity.ShiftAssignment;
import com.example.warehouse.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    List<ShiftAssignment> findByEmployeeAndDate(Employee employee, LocalDate date);

    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.employee = :employee AND sa.date = :date AND sa.conflict = true")
    List<ShiftAssignment> detectConflicts(@Param("employee") Employee employee, @Param("date") LocalDate date);
}
