package com.warehouse.employee.management.infrastructure.repository;

import com.warehouse.employee.management.domain.employee.Employee;
import com.warehouse.employee.management.domain.employee.EmployeeStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class EmployeeSpecifications {
    public static Specification<Employee> hasStatus(EmployeeStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Employee> hasDepartment(UUID departmentId) {
        return (root, query, cb) -> cb.equal(root.get("department").get("id"), departmentId);
    }

    public static Specification<Employee> hasPosition(UUID positionId) {
        return (root, query, cb) -> cb.equal(root.get("position").get("id"), positionId);
    }

    public static Specification<Employee> hasSupervisor(UUID supervisorId) {
        return (root, query, cb) -> cb.equal(root.get("supervisor").get("id"), supervisorId);
    }

    public static Specification<Employee> isNotDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Employee> hasTenant(String tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
    }
}
