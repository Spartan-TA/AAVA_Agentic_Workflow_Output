package com.company.warehousemgmt.util;

import com.company.warehousemgmt.domain.Employee;
import com.company.warehousemgmt.domain.Role;
import com.company.warehousemgmt.dto.EmployeeRequestDTO;
import com.company.warehousemgmt.dto.EmployeeResponseDTO;

public class EmployeeMapper {
    public static Employee toEntity(EmployeeRequestDTO dto, Role role) {
        return Employee.builder()
                .badgeId(dto.getBadgeId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .department(dto.getDepartment())
                .shiftGroup(dto.getShiftGroup())
                .hireDate(dto.getHireDate())
                .status(dto.getStatus())
                .role(role)
                .build();
    }

    public static EmployeeResponseDTO toResponseDTO(Employee employee) {
        return EmployeeResponseDTO.builder()
                .id(employee.getId())
                .badgeId(employee.getBadgeId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .department(employee.getDepartment())
                .shiftGroup(employee.getShiftGroup())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .roleName(employee.getRole() != null ? employee.getRole().getName() : null)
                .build();
    }
}
