package com.wms.ems.employee.service;

import com.wms.ems.employee.dto.EmployeeDTO;
import java.util.List;

public interface EmployeeService {
    EmployeeDTO createEmployee(EmployeeDTO employeeDTO);
    EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO);
    EmployeeDTO getEmployeeById(Long id);
    List<EmployeeDTO> getAllEmployees();
    void deleteEmployee(Long id);
    List<EmployeeDTO> getEmployeesByDepartment(String department);
    List<EmployeeDTO> getEmployeesByRole(String role);
    List<EmployeeDTO> getEmployeesByShiftGroup(String shiftGroup);
    List<EmployeeDTO> getEmployeesByStatus(String status);
}
