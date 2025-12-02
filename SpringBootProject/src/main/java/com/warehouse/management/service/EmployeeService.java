package com.warehouse.management.service;

import com.warehouse.management.entity.Employee;
import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    Employee createEmployee(Employee employee);
    Employee updateEmployee(Long id, Employee employee);
    void deleteEmployee(Long id);
    Optional<Employee> getEmployeeById(Long id);
    List<Employee> getAllEmployees();
}
