package com.warehouse.ems.service;

import com.warehouse.ems.dto.EmployeeDTO;
import java.util.List;

public interface EmployeeService {
    EmployeeDTO createEmployee(EmployeeDTO employeeDTO);
    EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO);
    EmployeeDTO getEmployee(Long id);
    List<EmployeeDTO> getAllEmployees();
    void deleteEmployee(Long id);
}
