package com.warehouse.employee.management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Employee response DTO")
public class EmployeeResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Jane Doe")
    private String name;

    @Schema(example = "B12345")
    private String badgeId;

    @Schema(example = "WORKER")
    private String role;

    @Schema(example = "Shipping")
    private String department;

    @Schema(example = "Night")
    private String shiftGroup;

    @Schema(example = "2023-01-15")
    private LocalDate hireDate;

    @Schema(example = "ACTIVE")
    private String status;
}