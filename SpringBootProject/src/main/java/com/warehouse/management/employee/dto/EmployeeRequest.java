package com.warehouse.management.employee.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

/**
 * DTO for Employee create/update requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequest {
    @NotBlank
    private String employeeCode;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    private String email;

    private String phone;

    private LocalDate dob;

    @NotNull
    private LocalDate hireDate;

    @NotBlank
    private String status;

    private String department;

    private String position;
}
