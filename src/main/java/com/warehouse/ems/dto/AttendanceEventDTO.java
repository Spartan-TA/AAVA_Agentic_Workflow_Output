package com.warehouse.ems.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEventDTO {
    private Long id;

    @NotNull
    private Long employeeId;

    @NotBlank
    private String type; // CLOCK_IN or CLOCK_OUT

    @NotNull
    private LocalDateTime timestamp;

    private String deviceId;
    private String geoLocation;
}
