package com.warehouse.management.employee.repository;

import com.warehouse.management.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Employee entity with custom query methods.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findByStatus(Employee.Status status, Pageable pageable);
    Page<Employee> findByDepartment(String department, Pageable pageable);
    Page<Employee> findByRole(Employee.Role role, Pageable pageable);
    Page<Employee> findByStatusAndDepartment(Employee.Status status, String department, Pageable pageable);
    Page<Employee> findByStatusAndRole(Employee.Status status, Employee.Role role, Pageable pageable);
    Page<Employee> findByDepartmentAndRole(String department, Employee.Role role, Pageable pageable);
    Page<Employee> findByStatusAndDepartmentAndRole(Employee.Status status, String department, Employee.Role role, Pageable pageable);
}
