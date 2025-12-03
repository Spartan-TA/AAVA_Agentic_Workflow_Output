package com.warehouse.employee.onboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeOnboardingRequest {
    @NotBlank
    private String badgeId;
    @NotBlank
    private String name;
    @NotBlank
    private String role;
    @NotBlank
    private String department;
    private String shiftGroup;
    @NotNull
    private LocalDate hireDate;
}
