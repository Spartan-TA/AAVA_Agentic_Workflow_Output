package com.wms.ems.scheduling.repository;

import com.wms.ems.scheduling.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for ShiftAssignment entity operations.
 * Provides CRUD operations and custom queries for shift assignment management.
 */
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {

    /**
     * Finds shift assignments for an employee between two dates.
     * @param employeeId the employee ID
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of shift assignments
     */
    List<ShiftAssignment> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * Finds shift assignments for a specific date.
     * @param date the date
     * @return a list of shift assignments
     */
    List<ShiftAssignment> findByDate(LocalDate date);
}
