package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository for Employee entity.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    List<Employee> findByDepartment_Id(Long departmentId);
    List<Employee> findByRole_Name(String roleName);
    List<Employee> findByDeletedFalse();
}
