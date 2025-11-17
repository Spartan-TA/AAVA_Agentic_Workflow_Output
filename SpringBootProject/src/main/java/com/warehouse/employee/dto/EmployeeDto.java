package com.warehouse.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDate;

/**
 * DTO for Employee API responses and requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Employee DTO")
public class EmployeeDto {
    @Schema(example = "12345", description = "Unique badge ID")
    private String badgeId;
    @Schema(example = "Jane Doe", description = "Employee name")
    private String name;
    @Schema(example = "WORKER", description = "Role")
    private String role;
    @Schema(example = "Shipping", description = "Department")
    private String department;
    @Schema(example = "A", description = "Shift group")
    private String shiftGroup;
    @Schema(example = "2023-01-15", description = "Hire date")
    private LocalDate hireDate;
    @Schema(example = "ACTIVE", description = "Status")
    private String status;
}
