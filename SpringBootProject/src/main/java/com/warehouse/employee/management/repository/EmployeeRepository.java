package com.warehouse.employee.management.repository;

import com.warehouse.employee.management.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);

    boolean existsByBadgeIdAndDeletedFalse(String badgeId);

    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND " +
            "(LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.department) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.role) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Employee> search(String search, Pageable pageable);

    Page<Employee> findAllByDeletedFalse(Pageable pageable);
}