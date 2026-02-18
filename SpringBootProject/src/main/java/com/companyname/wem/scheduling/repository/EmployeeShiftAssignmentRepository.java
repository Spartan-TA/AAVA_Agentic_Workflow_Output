package com.companyname.wem.scheduling.repository;

import com.companyname.wem.scheduling.domain.EmployeeShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeShiftAssignmentRepository extends JpaRepository<EmployeeShiftAssignment, Long> {
    List<EmployeeShiftAssignment> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    List<EmployeeShiftAssignment> findByEmployeeId(Long employeeId);
}
