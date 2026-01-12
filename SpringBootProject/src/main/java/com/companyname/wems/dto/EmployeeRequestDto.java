package com.companyname.wems.dto;

import com.companyname.wems.employee.Employee;
import com.companyname.wems.employee.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class EmployeeRequestDto {
    @NotBlank
    private String name;
    @NotBlank
    private String badgeId;
    @NotNull
    private Set<Role> roles;
    @NotBlank
    private String department;
    @NotBlank
    private String shiftGroup;
    @NotNull
    private LocalDate hireDate;
    @NotBlank
    private String status;

    public Employee toEntity() {
        return Employee.builder()
                .name(name)
                .badgeId(badgeId)
                .roles(roles)
                .department(department)
                .shiftGroup(shiftGroup)
                .hireDate(hireDate)
                .status(status)
                .deleted(false)
                .build();
    }
}
