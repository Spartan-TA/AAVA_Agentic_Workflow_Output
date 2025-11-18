package com.warehouse.employee.management.service;

import com.warehouse.employee.management.domain.Employee;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Employee business logic.
 */
public interface EmployeeService {
    Employee createEmployee(Employee employee);
    Employee updateEmployee(Long id, Employee employee);
    void deleteEmployee(Long id);
    Optional<Employee> getEmployeeById(Long id);
    Optional<Employee> getEmployeeByBadgeId(String badgeId);
    List<Employee> listEmployees(String department, int page, int size);
}
