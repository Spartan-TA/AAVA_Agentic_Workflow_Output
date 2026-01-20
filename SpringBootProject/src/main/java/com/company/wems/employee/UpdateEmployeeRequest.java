package com.company.wems.employee;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateEmployeeRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String role;

    @NotBlank
    private String department;

    private String shiftGroup;

    @PastOrPresent
    private LocalDate hireDate;

    @NotBlank
    private String status;
}