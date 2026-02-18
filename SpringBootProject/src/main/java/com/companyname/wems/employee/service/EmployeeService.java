package com.companyname.wems.employee.service;

import com.companyname.wems.employee.dto.EmployeeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    EmployeeDto createEmployee(EmployeeDto dto);
    EmployeeDto updateEmployee(Long id, EmployeeDto dto);
    EmployeeDto getEmployee(Long id);
    void deleteEmployee(Long id);
    Page<EmployeeDto> listEmployees(String filter, Pageable pageable);
}