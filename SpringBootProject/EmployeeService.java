package com.example.warehousemanagement.service;

import com.example.warehousemanagement.entity.Employee;
import java.util.List;

/**
 * Service interface for Employee business logic.
 */
public interface EmployeeService {
    Employee getEmployeeById(Long id);
    List<Employee> getAllEmployees();
    Employee createEmployee(Employee employee);
    Employee updateEmployee(Long id, Employee employee);
    void deleteEmployee(Long id);
}
