package com.company.wems.employee.repository;

import com.company.wems.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Employee entity.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Query("SELECT e FROM Employee e WHERE e.deleted = false")
    List<Employee> findAllActive();

    @Query("SELECT e FROM Employee e WHERE e.badgeId = :badgeId AND e.deleted = false")
    Optional<Employee> findByBadgeId(@Param("badgeId") String badgeId);
}
