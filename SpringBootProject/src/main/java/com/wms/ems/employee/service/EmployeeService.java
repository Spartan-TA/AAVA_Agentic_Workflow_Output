package com.wms.ems.employee.service;

import com.wms.ems.employee.dto.EmployeeDto;
import java.util.List;

/**
 * Service interface for Employee CRUD operations.
 */
public interface EmployeeService {
    EmployeeDto createEmployee(EmployeeDto dto);
    EmployeeDto getEmployeeByBadgeId(String badgeId);
    List<EmployeeDto> getAllEmployees();
    EmployeeDto updateEmployee(String badgeId, EmployeeDto dto);
    void softDeleteEmployee(String badgeId);
}
