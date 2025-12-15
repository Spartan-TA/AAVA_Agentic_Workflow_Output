package com.warehouse.ems.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO for Employee create/update requests.
 */
@Data
public class EmployeeRequestDTO {
    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 20)
    private String badgeId;

    @NotBlank
    private String role;

    @NotBlank
    private String department;

    private String shiftGroup;

    @NotNull
    private LocalDate hireDate;

    @NotBlank
    private String status;
}
