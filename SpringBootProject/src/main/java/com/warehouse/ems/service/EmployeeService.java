package com.warehouse.ems.service;

import com.warehouse.ems.entity.Employee;
import com.warehouse.ems.dto.EmployeeDto;
import com.warehouse.ems.dto.EmployeeCreateDto;
import com.warehouse.ems.dto.EmployeeUpdateDto;
import java.util.List;

public interface EmployeeService {
    EmployeeDto getEmployeeById(Long id);
    List<EmployeeDto> getAllEmployees();
    EmployeeDto createEmployee(EmployeeCreateDto employeeCreateDto);
    EmployeeDto updateEmployee(Long id, EmployeeUpdateDto employeeUpdateDto);
    void deleteEmployee(Long id);
}
