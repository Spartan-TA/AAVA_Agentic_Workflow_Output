package com.companyname.wem.shift.repository;

import com.companyname.wem.shift.domain.EmployeeShiftAssignment;
import com.companyname.wem.employee.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeShiftAssignmentRepository extends JpaRepository<EmployeeShiftAssignment, Long> {
    List<EmployeeShiftAssignment> findByEmployee(Employee employee);
    List<EmployeeShiftAssignment> findByDate(LocalDate date);
}
