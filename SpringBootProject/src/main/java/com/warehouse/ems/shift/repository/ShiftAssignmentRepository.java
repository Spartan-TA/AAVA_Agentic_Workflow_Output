package com.warehouse.ems.shift.repository;

import com.warehouse.ems.shift.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for ShiftAssignment entity.
 */
@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    List<ShiftAssignment> findByEmployeeId(Long employeeId);

    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.shiftDate = :date AND sa.shiftTemplate.id = :templateId")
    List<ShiftAssignment> findAssignmentsForTemplateOnDate(Long templateId, LocalDate date);
}
