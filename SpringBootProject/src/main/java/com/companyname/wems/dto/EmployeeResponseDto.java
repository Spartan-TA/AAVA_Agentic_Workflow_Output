package com.companyname.wems.dto;

import com.companyname.wems.employee.Employee;
import com.companyname.wems.employee.Role;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class EmployeeResponseDto {
    private Long id;
    private String name;
    private String badgeId;
    private Set<String> roles;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;

    public static EmployeeResponseDto fromEntity(Employee employee) {
        EmployeeResponseDto dto = new EmployeeResponseDto();
        dto.setId(employee.getId());
        dto.setName(employee.getName());
        dto.setBadgeId(employee.getBadgeId());
        dto.setRoles(employee.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        dto.setDepartment(employee.getDepartment());
        dto.setShiftGroup(employee.getShiftGroup());
        dto.setHireDate(employee.getHireDate());
        dto.setStatus(employee.getStatus());
        return dto;
    }
}
