package com.company.warehouse.employee.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO for Employee API requests/responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {
    private Long id;

    @NotBlank
    @Size(max = 32)
    private String badgeId;

    @NotBlank
    @Size(max = 128)
    private String name;

    @NotBlank
    @Size(max = 32)
    private String role;

    @NotBlank
    @Size(max = 64)
    private String department;

    @Size(max = 32)
    private String shiftGroup;

    @NotNull
    private LocalDate hireDate;

    @NotBlank
    @Size(max = 16)
    private String status;
}