package com.warehouse.management.employee.dto;

import com.warehouse.management.employee.entity.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO for updating an Employee (partial update supported).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Employee update request DTO")
public class EmployeeUpdateRequest {
    @Schema(example = "John Doe")
    private String name;

    @Size(max = 50)
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Badge ID must be alphanumeric and uppercase.")
    @Schema(example = "BADGE1234")
    private String badgeId;

    @Schema(example = "WORKER")
    private Employee.Role role;

    @Schema(example = "Logistics")
    private String department;

    @Schema(example = "A")
    private String shiftGroup;

    @PastOrPresent
    @Schema(example = "2023-01-01")
    private LocalDate hireDate;

    @Schema(example = "ACTIVE")
    private Employee.Status status;
}
