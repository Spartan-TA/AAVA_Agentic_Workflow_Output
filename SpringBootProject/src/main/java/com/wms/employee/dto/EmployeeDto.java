package com.wms.employee.dto;

import com.wms.employee.domain.Status;
import com.wms.employee.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @Email
    @NotBlank
    private String email;

    @NotNull
    private Status status;

    @NotNull
    private Role role;

    private Long departmentId;

    private LocalDate hireDate;
}
