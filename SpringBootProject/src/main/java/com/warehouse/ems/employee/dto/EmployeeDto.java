package com.warehouse.ems.employee.dto;

import com.warehouse.ems.employee.Employee;
import lombok.*;
import java.time.LocalDate;

/**
 * Data Transfer Object for Employee entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {
    private Long id;
    private String name;
    private String badgeId;
    private String role;
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;

    public static EmployeeDto fromEntity(Employee employee) {
        return EmployeeDto.builder()
                .id(employee.getId())
                .name(employee.getName())
                .badgeId(employee.getBadgeId())
                .role(employee.getRole())
                .department(employee.getDepartment())
                .shiftGroup(employee.getShiftGroup())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .build();
    }
}
