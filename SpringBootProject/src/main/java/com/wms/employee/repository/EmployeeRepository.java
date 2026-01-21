package com.wms.employee.repository;

import com.wms.employee.entity.Employee;
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
    Optional<Employee> findByBadgeIdAndSoftDeleteFalse(String badgeId);
    Page<Employee> findAllBySoftDeleteFalse(Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.softDelete = false AND (:department IS NULL OR e.department = :department)")
    Page<Employee> findByDepartment(String department, Pageable pageable);
}
