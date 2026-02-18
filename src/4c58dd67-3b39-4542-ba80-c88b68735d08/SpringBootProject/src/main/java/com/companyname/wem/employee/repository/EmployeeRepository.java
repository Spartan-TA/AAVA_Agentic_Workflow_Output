package com.companyname.wem.employee.repository;

import com.companyname.wem.employee.domain.Employee;
import com.companyname.wem.employee.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    List<Employee> findByStatus(Status status);
    List<Employee> findByDepartment(String department);
    List<Employee> findByDeletedFalse();
}
