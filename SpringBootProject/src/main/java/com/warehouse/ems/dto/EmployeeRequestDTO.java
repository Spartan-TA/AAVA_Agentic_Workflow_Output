package com.warehouse.ems.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequestDTO {
    @NotBlank
    private String badgeId;

    @NotBlank
    @Size(max = 128)
    private String name;

    @NotNull
    private Long roleId;

    @Size(max = 64)
    private String department;

    @Size(max = 64)
    private String shiftGroup;

    private LocalDate hireDate;

    @NotBlank
    private String status;
}
