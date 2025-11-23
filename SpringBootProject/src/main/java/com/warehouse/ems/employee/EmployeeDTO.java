package com.warehouse.ems.employee;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO for Employee API requests/responses.
 */
@Data
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