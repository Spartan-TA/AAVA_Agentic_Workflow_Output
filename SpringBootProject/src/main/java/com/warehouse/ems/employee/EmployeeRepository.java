package com.warehouse.ems.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * Repository for Employee entity operations.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    List<Employee> findByDepartment(String department);
    @Query("SELECT e FROM Employee e WHERE e.deleted = false")
    List<Employee> findAllActive();
}
