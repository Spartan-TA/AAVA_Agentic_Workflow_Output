package com.company.warehouse.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

/**
 * DTO for creating a new employee.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeDto {
    @NotBlank
    private String name;
    @NotBlank
    private String badgeId;
    @NotBlank
    private String role;
    @NotBlank
    private String department;
    private String shiftGroup;
    @NotNull
    private LocalDate hireDate;
}
