package com.warehouse.ems.employee.repository;

import com.warehouse.ems.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Employee entity with custom queries.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUsername(String username);

    @Query("SELECT e FROM Employee e WHERE e.active = true AND e.deleted = false")
    List<Employee> findAllActive();

    @Query("SELECT e FROM Employee e WHERE e.role = :role AND e.deleted = false")
    List<Employee> findByRole(Employee.Role role);
}
