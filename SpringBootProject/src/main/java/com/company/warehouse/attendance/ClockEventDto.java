package com.company.warehouse.attendance;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO for ClockEvent API requests/responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClockEventDto {
    private Long id;
    @NotNull
    private Long employeeId;
    @NotNull
    private LocalDateTime timestamp;
    @NotNull
    private ClockEvent.ClockEventType type;
    private Double latitude;
    private Double longitude;
    private String deviceId;
}
