package com.warehouse.ems.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestDTO {
    private Long id;

    @NotNull
    private Long employeeId;

    @NotBlank
    private String type; // PTO, SICK, UNPAID

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotBlank
    private String status; // REQUESTED, APPROVED, DENIED
}
