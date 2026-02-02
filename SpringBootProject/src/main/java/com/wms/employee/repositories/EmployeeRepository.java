package com.wms.employee.repositories;

import com.wms.employee.model.Employee;
import com.wms.common.enums.Status;
import com.wms.common.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Employee entity with custom queries.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByBadgeIdAndDeletedFalse(String badgeId);
    List<Employee> findByDepartmentIdAndDeletedFalse(Long departmentId);
    List<Employee> findByRoleAndDeletedFalse(Role role);
    List<Employee> findByStatusAndDeletedFalse(Status status);
    List<Employee> findByDeletedFalse();
}
