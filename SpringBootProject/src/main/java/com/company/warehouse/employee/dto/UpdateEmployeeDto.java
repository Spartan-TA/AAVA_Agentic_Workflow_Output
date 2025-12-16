package com.company.warehouse.employee.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDate;

/**
 * DTO for updating employee details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeDto {
    private String name;
    private String department;
    private String role;
    private String shiftGroup;
    private LocalDate hireDate;
}
