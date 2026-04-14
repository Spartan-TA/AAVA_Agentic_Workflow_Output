package com.wms.ems.dto.request;

import com.wms.ems.enums.EmployeeRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO for creating a new Employee.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateDTO {
    /**
     * Employee name.
     */
    @NotBlank(message = "Employee name must not be blank")
    @Size(max = 100, message = "Employee name must not exceed 100 characters")
    private String name;

    /**
     * Employee badge ID (format: EMP-XXXX).
     */
    @NotBlank(message = "Badge ID must not be blank")
    @Pattern(regexp = "EMP-\d{4}", message = "Badge ID must match format EMP-XXXX")
    private String badgeId;

    /**
     * Employee role.
     */
    @NotNull(message = "Role must not be null")
    private EmployeeRole role;

    /**
     * Department name.
     */
    @NotBlank(message = "Department must not be blank")
    private String department;

    /**
     * Shift group name.
     */
    @NotBlank(message = "Shift group must not be blank")
    private String shiftGroup;

    /**
     * Hire date (must be in the past).
     */
    @NotNull(message = "Hire date must not be null")
    @Past(message = "Hire date must be in the past")
    private LocalDate hireDate;
}
