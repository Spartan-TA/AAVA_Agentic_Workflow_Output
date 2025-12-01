package com.warehouse.ems.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository for Employee entity.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    List<Employee> findAllByDeletedFalse();
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND e.department = ?1")
    List<Employee> findByDepartment(String department);
}
