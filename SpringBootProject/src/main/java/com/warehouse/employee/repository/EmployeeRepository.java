package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Employee entity with custom queries.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);

    @Query("SELECT e FROM Employee e WHERE e.status = 'ACTIVE' AND e.deleted = false")
    Page<Employee> findActiveEmployees(Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.deleted = false")
    Page<Employee> findAllNotDeleted(Pageable pageable);
}
