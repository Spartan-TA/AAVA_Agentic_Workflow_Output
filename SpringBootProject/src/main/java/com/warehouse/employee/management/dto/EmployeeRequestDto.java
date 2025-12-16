package com.warehouse.employee.management.dto;

import lombok.Data;
import javax.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO for employee creation and update requests.
 */
@Data
public class EmployeeRequestDto {
    @NotBlank
    private String name;
    @NotBlank
    private String badgeId;
    @NotBlank
    private String role;
    @NotBlank
    private String department;
    @NotBlank
    private String shiftGroup;
    @PastOrPresent
    private LocalDate hireDate;
    @NotBlank
    private String status;
}
