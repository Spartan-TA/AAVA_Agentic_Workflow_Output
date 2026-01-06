package com.warehouse.management.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClockOutDTO {
    @NotNull
    private Long employeeId;

    @NotNull
    private Long shiftId;

    @NotNull
    private Long attendanceEventId;

    @NotBlank
    @Size(max = 100)
    private String device;

    @Size(max = 255)
    private String location;
}