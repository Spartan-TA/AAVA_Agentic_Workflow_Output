package com.wms.scheduling.repositories;

import com.wms.scheduling.model.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for managing ShiftAssignment entities
 */
@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    /**
     * Find all assignments for a given employee
     * @param employeeId Employee ID
     * @return List of ShiftAssignment
     */
    List<ShiftAssignment> findByEmployeeId(Long employeeId);

    /**
     * Find all assignments for a given date
     * @param shiftDate Date of shift
     * @return List of ShiftAssignment
     */
    List<ShiftAssignment> findByShiftDate(LocalDate shiftDate);
}
