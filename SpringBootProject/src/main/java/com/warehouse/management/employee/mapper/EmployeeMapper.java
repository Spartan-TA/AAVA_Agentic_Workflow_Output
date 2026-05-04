package com.warehouse.management.employee.mapper;

import com.warehouse.management.employee.dto.EmployeeDTO;
import com.warehouse.management.employee.entity.Employee;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Employee and EmployeeDTO.
 */
@Component
public class EmployeeMapper {
    /**
     * Converts EmployeeDTO to Employee entity.
     * @param dto EmployeeDTO
     * @return Employee entity
     */
    public Employee toEntity(EmployeeDTO dto) {
        if (dto == null) return null;
        Employee employee = new Employee();
        employee.setId(dto.getId());
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setDepartment(dto.getDepartment());
        employee.setPosition(dto.getPosition());
        employee.setHireDate(dto.getHireDate());
        employee.setActive(dto.isActive());
        return employee;
    }

    /**
     * Converts Employee entity to EmployeeDTO.
     * @param employee Employee entity
     * @return EmployeeDTO
     */
    public EmployeeDTO toDTO(Employee employee) {
        if (employee == null) return null;
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setDepartment(employee.getDepartment());
        dto.setPosition(employee.getPosition());
        dto.setHireDate(employee.getHireDate());
        dto.setActive(employee.isActive());
        return dto;
    }

    /**
     * Updates an existing Employee entity with values from EmployeeDTO.
     * @param employee Employee entity
     * @param dto EmployeeDTO
     */
    public void updateEntity(Employee employee, EmployeeDTO dto) {
        if (employee == null || dto == null) return;
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setDepartment(dto.getDepartment());
        employee.setPosition(dto.getPosition());
        employee.setHireDate(dto.getHireDate());
        employee.setActive(dto.isActive());
    }
}
