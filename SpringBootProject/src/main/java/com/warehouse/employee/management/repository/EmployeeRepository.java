package com.warehouse.employee.management.repository;

import com.warehouse.employee.management.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

/**
 * Repository for Employee entity.
 * Supports CRUD, pagination, sorting, and soft-delete queries.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL")
    List<Employee> findAllActive();

    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL")
    Page<Employee> findAllActive(Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.id = :id AND e.deletedAt IS NULL")
    Optional<Employee> findActiveById(Long id);

    // Custom query example: Find by email
    @Query("SELECT e FROM Employee e WHERE e.email = :email AND e.deletedAt IS NULL")
    Optional<Employee> findActiveByEmail(String email);
}
