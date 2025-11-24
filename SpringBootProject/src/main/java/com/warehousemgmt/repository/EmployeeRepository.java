package com.warehousemgmt.repository;

import com.warehousemgmt.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Employee entity with soft delete and badgeId uniqueness.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    List<Employee> findAllByDeletedFalse();
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND (:department IS NULL OR e.department = :department) AND (:role IS NULL OR e.role = :role)")
    List<Employee> filterByDepartmentAndRole(@Param("department") String department, @Param("role") String role);
}
