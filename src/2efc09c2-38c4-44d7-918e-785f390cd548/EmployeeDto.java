package com.company.warehouse.employee;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
public class EmployeeDto {
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Badge ID is required")
    private String badgeId;

    @NotNull(message = "Role is required")
    private Role role;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Shift group is required")
    private String shiftGroup;

    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    @NotNull(message = "Status is required")
    private Status status;

    private LocalDate createdAt;
    private LocalDate updatedAt;
}
