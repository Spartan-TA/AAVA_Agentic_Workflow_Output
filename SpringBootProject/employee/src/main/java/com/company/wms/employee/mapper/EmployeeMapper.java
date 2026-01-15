package com.company.wms.employee.mapper;

import com.company.wms.employee.dto.CreateEmployeeRequest;
import com.company.wms.employee.dto.EmployeeDTO;
import com.company.wms.employee.dto.UpdateEmployeeRequest;
import com.company.wms.employee.entity.Employee;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Employee entity and DTOs.
 */
@Component
public class EmployeeMapper {
    public EmployeeDTO toDTO(Employee employee) {
        if (employee == null) return null;
        return new EmployeeDTO(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getDepartment(),
                employee.getPosition(),
                employee.getHireDate(),
                employee.isActive()
        );
    }

    public Employee toEntity(CreateEmployeeRequest request) {
        Employee employee = new Employee();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setDepartment(request.getDepartment());
        employee.setPosition(request.getPosition());
        employee.setHireDate(request.getHireDate());
        return employee;
    }

    public void updateEntity(Employee employee, UpdateEmployeeRequest request) {
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setDepartment(request.getDepartment());
        employee.setPosition(request.getPosition());
        employee.setHireDate(request.getHireDate());
    }
}