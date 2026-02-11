package com.wms.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Data Transfer Object for Employee.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
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

    @NotNull
    @PastOrPresent
    private LocalDate hireDate;

    @NotBlank
    private String status;
}
