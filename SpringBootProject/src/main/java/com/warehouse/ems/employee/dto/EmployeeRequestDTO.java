package com.warehouse.ems.employee.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

/**
 * DTO for employee create/update requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequestDTO {
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
