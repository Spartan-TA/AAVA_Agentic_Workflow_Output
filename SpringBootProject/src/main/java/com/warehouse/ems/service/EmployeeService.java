package com.warehouse.ems.service;

import com.warehouse.ems.dto.EmployeeRequestDTO;
import com.warehouse.ems.dto.EmployeeResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto);
    EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO dto);
    EmployeeResponseDTO getEmployee(Long id);
    void deleteEmployee(Long id);
    Page<EmployeeResponseDTO> listEmployees(Pageable pageable);
}
