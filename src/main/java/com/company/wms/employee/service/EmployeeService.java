package com.company.wms.employee.service;

import com.company.wms.employee.entity.Employee;
import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    Employee createEmployee(Employee employee);
    Optional<Employee> getEmployeeById(Long id);
    Optional<Employee> getEmployeeByBadgeId(String badgeId);
    List<Employee> getAllEmployees(int page, int size);
    Employee updateEmployee(Long id, Employee employee);
    void softDeleteEmployee(Long id);
}
