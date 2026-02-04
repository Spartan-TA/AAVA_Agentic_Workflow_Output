package com.company.warehouse.employee.dto;

import com.company.warehouse.common.enums.Role;
import com.company.warehouse.common.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO for Employee API contract.
 */
@Data
public class EmployeeDTO {
    private Long id;

    @NotBlank
    private String badgeId;

    @NotBlank
    private String name;

    @NotNull
    private Role role;

    private String department;

    private String shiftGroup;

    private LocalDate hireDate;

    private Status status;
}