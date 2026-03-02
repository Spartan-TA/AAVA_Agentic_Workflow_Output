package com.wems.employee;

import com.wems.employee.Role;
import com.wems.employee.EmploymentStatus;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Data Transfer Object for Employee API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {
    private Long id;

    @NotBlank
    private String badgeId;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private Role role;

    @NotBlank
    @Size(max = 50)
    private String department;

    @Size(max = 50)
    private String shiftGroup;

    @NotNull
    private LocalDate hireDate;

    @NotNull
    private EmploymentStatus status;
}
