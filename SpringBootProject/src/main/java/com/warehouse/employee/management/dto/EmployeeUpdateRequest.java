package com.warehouse.employee.management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update an employee")
public class EmployeeUpdateRequest {

    @NotBlank
    @Schema(example = "Jane Doe")
    private String name;

    @NotBlank
    @Schema(example = "WORKER")
    private String role;

    @NotBlank
    @Schema(example = "Shipping")
    private String department;

    @Schema(example = "Night")
    private String shiftGroup;

    @PastOrPresent
    @Schema(example = "2023-01-15")
    private LocalDate hireDate;

    @NotBlank
    @Schema(example = "ACTIVE")
    private String status;
}