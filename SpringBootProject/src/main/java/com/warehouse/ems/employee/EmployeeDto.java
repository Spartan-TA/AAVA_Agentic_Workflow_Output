package com.warehouse.ems.employee;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Data Transfer Object for Employee API requests and responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Badge ID is required")
    @Size(max = 50)
    private String badgeId;

    @NotNull(message = "Role is required")
    private String role;

    @Size(max = 100)
    private String department;

    @Size(max = 50)
    private String shiftGroup;

    private LocalDate hireDate;

    @Size(max = 30)
    private String status;

    private Boolean active;
    private Boolean deleted;
}
