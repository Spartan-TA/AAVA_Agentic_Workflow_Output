package com.companyname.wem.attendance.dto;

import com.companyname.wem.attendance.domain.EventType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClockEventDTO {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Event type is required")
    private EventType type;

    private String deviceId;
    private Double latitude;
    private Double longitude;
    private Boolean correction;
}
