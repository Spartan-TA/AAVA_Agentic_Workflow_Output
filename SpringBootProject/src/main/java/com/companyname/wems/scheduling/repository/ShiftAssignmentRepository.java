package com.companyname.wems.scheduling.repository;

import com.companyname.wems.scheduling.model.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    List<ShiftAssignment> findByEmployeeId(Long employeeId);
    List<ShiftAssignment> findByAssignmentDate(LocalDate assignmentDate);
    // Add custom queries for conflict detection if needed
}