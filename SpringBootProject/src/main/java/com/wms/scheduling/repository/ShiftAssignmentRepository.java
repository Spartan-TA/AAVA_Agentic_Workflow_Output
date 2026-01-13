package com.wms.scheduling.repository;

import com.wms.scheduling.entity.ShiftAssignment;
import com.wms.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for ShiftAssignment entity.
 */
@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    List<ShiftAssignment> findByEmployeeAndDate(Employee employee, LocalDate date);
}
