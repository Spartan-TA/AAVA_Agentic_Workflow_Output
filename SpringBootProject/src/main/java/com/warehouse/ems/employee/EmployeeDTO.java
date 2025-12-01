package com.warehouse.ems.employee;

import lombok.*;
import javax.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO for Employee API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
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
    private LocalDate hireDate;
    @NotNull
    private Employee.Status status;
}
