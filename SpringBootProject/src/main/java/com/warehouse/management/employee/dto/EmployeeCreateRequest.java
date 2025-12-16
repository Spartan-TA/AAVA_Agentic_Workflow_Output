package com.warehouse.management.employee.dto;

import com.warehouse.management.employee.entity.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO for creating a new Employee.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Employee creation request DTO")
public class EmployeeCreateRequest {
    @NotBlank
    @Schema(example = "John Doe", required = true)
    private String name;

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Badge ID must be alphanumeric and uppercase.")
    @Schema(example = "BADGE1234", required = true)
    private String badgeId;

    @NotNull
    @Schema(example = "WORKER", required = true)
    private Employee.Role role;

    @NotBlank
    @Schema(example = "Logistics", required = true)
    private String department;

    @NotBlank
    @Schema(example = "A", required = true)
    private String shiftGroup;

    @PastOrPresent
    @Schema(example = "2023-01-01", required = true)
    private LocalDate hireDate;
}
