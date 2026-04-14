package com.wms.ems.repository;

import com.wms.ems.entity.ShiftAssignment;
import com.wms.ems.entity.Employee;
import com.wms.ems.entity.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for ShiftAssignment entity operations.
 * Provides CRUD and custom query methods for shift assignment management.
 */
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    /**
     * Find shift assignments for an employee within a date range.
     * @param employee the employee
     * @param start start date
     * @param end end date
     * @return List of ShiftAssignments
     */
    List<ShiftAssignment> findByEmployeeAndAssignmentDateBetween(Employee employee, LocalDate start, LocalDate end);

    /**
     * Find shift assignments by shift template.
     * @param shiftTemplate the shift template
     * @return List of ShiftAssignments
     */
    List<ShiftAssignment> findByShiftTemplate(ShiftTemplate shiftTemplate);

    /**
     * Custom query to detect shift assignment conflicts for an employee.
     * @param employeeId the employee ID
     * @param assignmentDate the assignment date
     * @return List of conflicting ShiftAssignments
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.employee.id = :employeeId AND sa.assignmentDate = :assignmentDate")
    List<ShiftAssignment> findConflicts(@Param("employeeId") Long employeeId, @Param("assignmentDate") LocalDate assignmentDate);
}
