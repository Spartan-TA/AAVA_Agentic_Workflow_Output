package com.wms.employee.dto;

import com.wms.security.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for updating an Employee.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeDto {
    @NotBlank
    private String name;
    @NotNull
    private Role role;
    @NotBlank
    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    @NotBlank
    private String status;
}
