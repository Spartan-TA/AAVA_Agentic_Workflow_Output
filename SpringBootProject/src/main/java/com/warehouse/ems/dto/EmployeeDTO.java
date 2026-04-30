package com.warehouse.ems.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class EmployeeDTO {

    private Long id;

    @NotBlank
    @Size(max = 32)
    private String badgeId;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Pattern(regexp = "ADMIN|HR|SUPERVISOR|WORKER")
    private String role;

    @Size(max = 64)
    private String department;

    @Size(max = 64)
    private String shiftGroup;

    @NotNull
    private LocalDate hireDate;

    @NotBlank
    @Pattern(regexp = "ACTIVE|INACTIVE|TERMINATED")
    private String status;

    @NotNull
    private Integer warehouseId;
}