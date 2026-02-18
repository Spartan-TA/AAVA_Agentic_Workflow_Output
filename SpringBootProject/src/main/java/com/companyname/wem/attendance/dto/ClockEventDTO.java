package com.companyname.wem.attendance.dto;

import com.companyname.wem.attendance.domain.EventType;
import jakarta.validation.constraints.NotNull;
lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClockEventDTO {
    @NotNull
    private Long employeeId;
    
    @NotNull
    private EventType type;
    
    @NotNull
    private LocalDateTime timestamp;
    
    private String deviceId;
    private Double latitude;
    private Double longitude;
}
