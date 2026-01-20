package com.wms.employee.service;

import com.wms.employee.dto.EmployeeDto;
import java.util.List;

/**
 * Service interface for Employee operations.
 * Defines business logic methods for Employee management.
 */
public interface EmployeeService {
    EmployeeDto createEmployee(EmployeeDto employeeDto);
    EmployeeDto getEmployeeById(Long id);
    List<EmployeeDto> getAllEmployees();
    EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto);
    void deleteEmployee(Long id);
}
