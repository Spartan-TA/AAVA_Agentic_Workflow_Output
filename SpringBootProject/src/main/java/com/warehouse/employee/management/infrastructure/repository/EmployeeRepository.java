package com.warehouse.employee.management.infrastructure.repository;

import com.warehouse.employee.management.domain.employee.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByBadgeId(String badgeId);
    Optional<Employee> findByEmail(String email);

    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND (:search IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Employee> searchEmployees(@Param("search") String search);

    @Query("SELECT e FROM Employee e WHERE e.supervisor.id = :supervisorId AND e.deleted = false")
    List<Employee> findDirectReports(@Param("supervisorId") UUID supervisorId);
}
