package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.entity.Employee;
import com.warehouse.ems.employee.dto.EmployeeRequestDTO;
import com.warehouse.ems.employee.dto.EmployeeResponseDTO;

import java.util.List;

/**
 * Service interface for Employee business logic.
 */
public interface EmployeeService {
    EmployeeResponseDTO createEmployee(EmployeeRequestDTO request);
    EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO request);
    EmployeeResponseDTO getEmployee(Long id);
    List<EmployeeResponseDTO> getAllEmployees();
    void deleteEmployee(Long id);
}
