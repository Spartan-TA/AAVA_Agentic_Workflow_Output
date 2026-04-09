package com.warehouse.ems.attendance.dto;

import com.warehouse.ems.attendance.AttendanceEvent;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO for AttendanceEvent entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceDto {
    private Long id;
    private Long employeeId;
    private LocalDateTime timestamp;
    private String type;
    private String deviceId;
    private String location;

    public static AttendanceDto fromEntity(AttendanceEvent event) {
        return AttendanceDto.builder()
                .id(event.getId())
                .employeeId(event.getEmployee().getId())
                .timestamp(event.getTimestamp())
                .type(event.getType().name())
                .deviceId(event.getDeviceId())
                .location(event.getLocation())
                .build();
    }
}
