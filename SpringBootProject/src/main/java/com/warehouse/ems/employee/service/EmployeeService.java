package com.warehouse.ems.employee.service;

import com.warehouse.ems.employee.model.dto.EmployeeRequestDto;
import com.warehouse.ems.employee.model.dto.EmployeeResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EmployeeService {
    EmployeeResponseDto createEmployee(EmployeeRequestDto requestDto);
    EmployeeResponseDto getEmployeeById(UUID id);
    EmployeeResponseDto updateEmployee(UUID id, EmployeeRequestDto requestDto);
    void softDeleteEmployee(UUID id);
    Page<EmployeeResponseDto> getAllEmployees(String search, Pageable pageable);
}
