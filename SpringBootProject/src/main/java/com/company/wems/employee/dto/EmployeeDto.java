package com.company.wems.employee.dto;

import com.company.wems.employee.entity.Employee;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

/**
 * Data Transfer Object for Employee API requests and responses.
 */
public class EmployeeDto {
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String badgeId;
    @NotBlank
    private String role;
    @NotBlank
    private String department;
    private String shiftGroup;
    @PastOrPresent
    private LocalDate hireDate;
    @NotNull
    private Employee.Status status;
    // Getters and setters omitted for brevity
    // ...
}
