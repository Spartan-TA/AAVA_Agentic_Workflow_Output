package com.warehouse.employee.management.employee.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

/**
 * DTO for employee create/update requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequestDTO {
    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 32)
    private String badgeId;

    @NotBlank
    @Size(max = 50)
    private String role;

    @NotBlank
    @Size(max = 50)
    private String department;

    @Size(max = 50)
    private String shiftGroup;

    @PastOrPresent
    private LocalDate hireDate;

    @NotBlank
    @Size(max = 20)
    private String status;
}
