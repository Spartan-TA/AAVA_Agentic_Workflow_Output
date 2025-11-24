package com.warehousemgmt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO for Employee creation/update requests.
 */
@Data
public class EmployeeRequestDTO {
    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 32)
    private String badgeId;

    @NotBlank
    @Size(max = 32)
    private String role;

    @NotBlank
    @Size(max = 64)
    private String department;

    @Size(max = 32)
    private String shiftGroup;

    @NotNull
    private LocalDate hireDate;

    @NotBlank
    @Size(max = 32)
    private String status;

    private String tenantId;
}
