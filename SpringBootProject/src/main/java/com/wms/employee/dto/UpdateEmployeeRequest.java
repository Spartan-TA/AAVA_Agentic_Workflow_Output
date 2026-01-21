package com.wms.employee.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO for updating an existing Employee.
 */
public class UpdateEmployeeRequest {
    @Size(max = 128)
    private String name;

    @Size(max = 64)
    private String role;

    @Size(max = 64)
    private String department;

    @Size(max = 64)
    private String shiftGroup;

    private LocalDate hireDate;

    @Size(max = 32)
    private String status;

    // Getters and setters omitted for brevity
}
