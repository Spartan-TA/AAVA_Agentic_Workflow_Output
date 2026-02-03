package com.wms.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeDto {
    private Long id;

    @NotBlank(message = "Badge ID is required")
    private String badgeId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Department is required")
    private String department;

    private String shiftGroup;

    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    @NotBlank(message = "Status is required")
    private String status;
}