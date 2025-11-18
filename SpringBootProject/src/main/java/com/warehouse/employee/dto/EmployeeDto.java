package com.warehouse.employee.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO for Employee API requests and responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String badgeId;

    @NotNull
    private String role;

    private String department;
    private String shiftGroup;
    private LocalDate hireDate;

    @NotNull
    private String status;

    private boolean deleted;
}
