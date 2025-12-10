package com.company.warehouse.employee;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Data Transfer Object for Employee API requests/responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 50)
    private String badgeId;

    @NotBlank
    @Size(max = 50)
    private String role;

    @NotBlank
    @Size(max = 100)
    private String department;

    @Size(max = 50)
    private String shiftGroup;

    @NotNull
    private LocalDate hireDate;

    @NotBlank
    @Size(max = 20)
    private String status;
}
