package com.companyname.wem.employee.repository;

import com.companyname.wem.employee.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    boolean existsByBadgeId(String badgeId);
    Employee findByBadgeId(String badgeId);
}
