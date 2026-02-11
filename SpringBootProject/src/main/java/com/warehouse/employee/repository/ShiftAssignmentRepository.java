package com.warehouse.employee.repository;

import com.warehouse.employee.domain.ShiftAssignment;
import com.warehouse.employee.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository for ShiftAssignment entity with custom query methods.
 */
@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {

    /**
     * Find shift assignment by employee and assignment date.
     * @param employee Employee
     * @param assignmentDate Assignment date
     * @return Optional of ShiftAssignment
     */
    Optional<ShiftAssignment> findByEmployeeAndAssignmentDate(Employee employee, LocalDate assignmentDate);
}
