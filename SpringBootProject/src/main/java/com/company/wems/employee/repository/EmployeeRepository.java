package com.company.wems.employee.repository;

import com.company.wems.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Employee entity CRUD operations.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    @Query("SELECT e FROM Employee e WHERE e.deleted = false")
    java.util.List<Employee> findAllActive();
}
