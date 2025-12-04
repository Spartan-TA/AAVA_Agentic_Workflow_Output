package com.warehousemgmt.employee;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO for Employee API requests and responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private Long id;

    @NotBlank(message = "Badge ID is required")
    private String badgeId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Department is required")
    private String department;

    private String shiftGroup;

    @PastOrPresent(message = "Hire date must be in the past or present")
    private LocalDate hireDate;

    @NotBlank(message = "Status is required")
    private String status;
}
