package com.warehouse.ems.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

/**
 * DTO for employee creation and update requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequestDTO {
    @NotBlank(message = "Badge ID is required")
    private String badgeId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Role is required")
    private String role;

    private String department;
    private String shiftGroup;
    private LocalDate hireDate;

    @NotBlank(message = "Status is required")
    private String status;
}
