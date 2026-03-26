package com.warehouse.ems.employee.repository;

import com.warehouse.ems.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeId(String badgeId);
    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);
    Page<Employee> findByStatus(String status, Pageable pageable);
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND (:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Employee> searchByName(@Param("name") String name, Pageable pageable);
}
