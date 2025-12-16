package com.warehouse.management.employee.dto;

import com.warehouse.management.employee.entity.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Employee responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Employee response DTO")
public class EmployeeDTO {
    @Schema(example = "1")
    private Long id;

    @Schema(example = "John Doe")
    private String name;

    @Schema(example = "BADGE1234")
    private String badgeId;

    @Schema(example = "WORKER")
    private Employee.Role role;

    @Schema(example = "Logistics")
    private String department;

    @Schema(example = "A")
    private String shiftGroup;

    @Schema(example = "2023-01-01")
    private LocalDate hireDate;

    @Schema(example = "ACTIVE")
    private Employee.Status status;

    @Schema(example = "2023-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(example = "2023-01-01T10:00:00")
    private LocalDateTime updatedAt;

    @Schema(example = "system")
    private String createdBy;

    @Schema(example = "admin")
    private String updatedBy;
}
