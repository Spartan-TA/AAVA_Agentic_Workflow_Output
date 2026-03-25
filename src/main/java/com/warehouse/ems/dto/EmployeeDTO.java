package com.warehouse.ems.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String badgeId;

    @NotNull
    private Long roleId;

    private String department;
    private String shiftGroup;
    private LocalDate hireDate;
    private String status;
}
