package com.companyname.wems.employee.repository;

import com.companyname.wems.employee.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * EmployeeRepository for Employee Master Data CRUD (E02)
 * Includes custom queries and pagination support
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    List<Employee> findByStatus(String status);
    List<Employee> findByDepartment(String department);

    Page<Employee> findAll(Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.status = :status")
    Page<Employee> findByStatus(String status, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.department = :department")
    Page<Employee> findByDepartment(String department, Pageable pageable);
}
