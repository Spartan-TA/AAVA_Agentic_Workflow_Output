package com.warehouse.management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestDTO {
    @NotNull
    private Long id;

    @NotNull
    private Long employeeId;

    @NotBlank
    @Size(max = 50)
    private String leaveType;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotBlank
    @Size(max = 50)
    private String status;

    private Long approverId;

    @NotNull
    private Double balance;
}