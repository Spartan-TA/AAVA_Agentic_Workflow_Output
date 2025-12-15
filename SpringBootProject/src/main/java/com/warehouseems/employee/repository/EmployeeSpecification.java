package com.warehouseems.employee.repository;

import com.warehouseems.employee.entity.Employee;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;

/**
 * Specifications for filtering Employee entities by department, role, status, and hireDate range.
 */
public class EmployeeSpecification {
    public static Specification<Employee> hasDepartment(String department) {
        return (root, query, cb) -> department == null ? null : cb.equal(root.get("department"), department);
    }
    public static Specification<Employee> hasRole(String role) {
        return (root, query, cb) -> role == null ? null : cb.equal(root.get("role"), role);
    }
    public static Specification<Employee> hasStatus(String status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }
    public static Specification<Employee> hireDateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start != null && end != null) {
                return cb.between(root.get("hireDate"), start, end);
            } else if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("hireDate"), start);
            } else if (end != null) {
                return cb.lessThanOrEqualTo(root.get("hireDate"), end);
            } else {
                return null;
            }
        };
    }
}
