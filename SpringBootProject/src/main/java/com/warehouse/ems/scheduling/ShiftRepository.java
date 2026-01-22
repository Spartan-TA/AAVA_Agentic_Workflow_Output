package com.warehouse.ems.scheduling;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for managing ShiftAssignment entities.
 */
@Repository
public interface ShiftRepository extends JpaRepository<ShiftAssignment, Long> {

    /**
     * Find all shift assignments for a given employee.
     * @param employeeId Employee ID
     * @return List of ShiftAssignment
     */
    List<ShiftAssignment> findByEmployeeId(Long employeeId);

    /**
     * Find all shift assignments for a given date.
     * @param assignmentDate Assignment date
     * @return List of ShiftAssignment
     */
    List<ShiftAssignment> findByAssignmentDate(LocalDate assignmentDate);

    /**
     * Custom query to find assignments by shift and date.
     */
    @Query("SELECT s FROM ShiftAssignment s WHERE s.shiftId = :shiftId AND s.assignmentDate = :assignmentDate")
    List<ShiftAssignment> findByShiftIdAndAssignmentDate(Long shiftId, LocalDate assignmentDate);
}
