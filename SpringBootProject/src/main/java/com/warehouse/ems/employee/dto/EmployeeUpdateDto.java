package com.warehouse.ems.employee.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO for updating Employee records.
 */
public class EmployeeUpdateDto {
    @Size(max = 128)
    private String name;

    @Size(max = 32)
    private String role;

    @Size(max = 64)
    private String department;

    @Size(max = 32)
    private String shiftGroup;

    private LocalDate hireDate;

    @Size(max = 16)
    private String status;
    // Getters and setters omitted for brevity
}
