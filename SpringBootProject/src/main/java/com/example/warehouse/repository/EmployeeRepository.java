package com.example.warehouse.repository;

import com.example.warehouse.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

/**
 * Repository for Employee entity with custom queries.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);

    @Query("SELECT e FROM Employee e WHERE e.status <> 'DELETED'")
    Page<Employee> findAllActive(Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.department = :department AND e.status <> 'DELETED'")
    Page<Employee> findByDepartment(@Param("department") String department, Pageable pageable);
}
