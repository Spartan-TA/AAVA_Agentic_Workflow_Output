package com.warehouse.management.employee.mapper;

import com.warehouse.management.employee.dto.EmployeeCreateRequest;
import com.warehouse.management.employee.dto.EmployeeDTO;
import com.warehouse.management.employee.dto.EmployeeUpdateRequest;
import com.warehouse.management.employee.entity.Employee;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Employee entity and DTOs.
 */
@Component
public class EmployeeMapper {
    public EmployeeDTO toDTO(Employee employee) {
        if (employee == null) return null;
        return EmployeeDTO.builder()
                .id(employee.getId())
                .name(employee.getName())
                .badgeId(employee.getBadgeId())
                .role(employee.getRole())
                .department(employee.getDepartment())
                .shiftGroup(employee.getShiftGroup())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .createdBy(employee.getCreatedBy())
                .updatedBy(employee.getUpdatedBy())
                .build();
    }

    public Employee toEntity(EmployeeCreateRequest request) {
        if (request == null) return null;
        return Employee.builder()
                .name(request.getName())
                .badgeId(request.getBadgeId())
                .role(request.getRole())
                .department(request.getDepartment())
                .shiftGroup(request.getShiftGroup())
                .hireDate(request.getHireDate())
                .status(Employee.Status.ACTIVE)
                .build();
    }

    public void updateEntity(Employee employee, EmployeeUpdateRequest request) {
        if (request == null || employee == null) return;
        if (request.getName() != null) employee.setName(request.getName());
        if (request.getBadgeId() != null) employee.setBadgeId(request.getBadgeId());
        if (request.getRole() != null) employee.setRole(request.getRole());
        if (request.getDepartment() != null) employee.setDepartment(request.getDepartment());
        if (request.getShiftGroup() != null) employee.setShiftGroup(request.getShiftGroup());
        if (request.getHireDate() != null) employee.setHireDate(request.getHireDate());
        if (request.getStatus() != null) employee.setStatus(request.getStatus());
    }
}
