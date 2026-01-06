package com.warehouse.management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEventDTO {
    @NotNull
    private Long id;

    @NotNull
    private Long employeeId;

    @NotNull
    private Long shiftId;

    @NotNull
    private LocalDateTime clockIn;

    private LocalDateTime clockOut;

    private Long duration; // in minutes

    @Size(max = 100)
    private String device;

    @Size(max = 255)
    private String location;
}