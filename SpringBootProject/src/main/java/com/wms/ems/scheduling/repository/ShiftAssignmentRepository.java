package com.wms.ems.scheduling.repository;

import com.wms.ems.scheduling.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for ShiftAssignment entity.
 * Provides CRUD operations and custom queries for shift assignments.
 */
@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    /**
     * Find all shift assignments for a specific employee.
     * @param employeeId the employee's ID
     * @return List of ShiftAssignment
     */
    List<ShiftAssignment> findByEmployeeId(Long employeeId);

    /**
     * Find all shift assignments for a specific date.
     * @param shiftDate the date of the shift
     * @return List of ShiftAssignment
     */
    List<ShiftAssignment> findByShiftDate(LocalDate shiftDate);
}
