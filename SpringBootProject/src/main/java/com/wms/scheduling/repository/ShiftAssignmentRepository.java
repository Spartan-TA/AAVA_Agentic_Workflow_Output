package com.wms.scheduling.repository;

import com.wms.scheduling.domain.ShiftAssignment;
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
    List<ShiftAssignment> findByEmployeeId(Long employeeId);
    List<ShiftAssignment> findByAssignmentDate(LocalDate assignmentDate);
}
