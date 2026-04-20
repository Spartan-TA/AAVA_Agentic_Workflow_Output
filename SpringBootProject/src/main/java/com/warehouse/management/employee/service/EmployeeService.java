package com.warehouse.management.employee.service;

import com.warehouse.management.employee.dto.EmployeeRequestDTO;
import com.warehouse.management.employee.dto.EmployeeResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto);
    EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto);
    EmployeeResponseDTO getEmployee(Long id);
    Page<EmployeeResponseDTO> listEmployees(String search, Pageable pageable);
    void deleteEmployee(Long id);
}
