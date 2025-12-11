package com.warehouse.employee.management.employee.service;

import com.warehouse.employee.management.employee.dto.EmployeeRequestDTO;
import com.warehouse.employee.management.employee.dto.EmployeeResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto);
    EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto);
    EmployeeResponseDTO patchEmployee(Long id, EmployeeRequestDTO dto);
    void deleteEmployee(Long id);
    EmployeeResponseDTO getEmployee(Long id);
    Page<EmployeeResponseDTO> getAllEmployees(String department, Pageable pageable);
}
