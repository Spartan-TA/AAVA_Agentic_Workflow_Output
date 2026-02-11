package com.wms.employee;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Data Transfer Object for Employee.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {
    private Long id;

    @NotBlank
    @Size(max = 32)
    private String badgeId;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 50)
    private String role;

    @NotBlank
    @Size(max = 50)
    private String department;

    @NotBlank
    @Size(max = 50)
    private String shiftGroup;

    @NotNull
    private LocalDate hireDate;

    @NotBlank
    @Size(max = 20)
    private String status;

    private boolean deleted;

    public static EmployeeDto fromEntity(Employee employee) {
        return EmployeeDto.builder()
                .id(employee.getId())
                .badgeId(employee.getBadgeId())
                .name(employee.getName())
                .role(employee.getRole())
                .department(employee.getDepartment())
                .shiftGroup(employee.getShiftGroup())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .deleted(employee.isDeleted())
                .build();
    }

    public Employee toEntity() {
        return Employee.builder()
                .id(id)
                .badgeId(badgeId)
                .name(name)
                .role(role)
                .department(department)
                .shiftGroup(shiftGroup)
                .hireDate(hireDate)
                .status(status)
                .deleted(deleted)
                .build();
    }
}
