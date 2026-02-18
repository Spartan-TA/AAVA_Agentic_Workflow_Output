package com.companyname.wem.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeDTO {
    private Long id;

    @NotBlank
    @Size(max = 32)
    private String badgeId;

    @NotBlank
    @Size(max = 100)
    private String name;

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
