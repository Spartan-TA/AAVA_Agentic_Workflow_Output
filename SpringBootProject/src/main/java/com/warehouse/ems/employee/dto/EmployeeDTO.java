package com.warehouse.ems.employee.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private Long id;

    @NotBlank
    private String badgeId;

    @NotBlank
    private String name;

    @NotBlank
    private String role;

    @NotBlank
    private String department;

    private String shiftGroup;

    @PastOrPresent
    private LocalDate hireDate;

    @NotBlank
    private String status;
}