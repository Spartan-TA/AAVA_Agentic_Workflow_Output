package com.wms;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for AttendanceEvent.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceDto {
    private Long id;

    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDateTime eventTime;

    @NotBlank
    @Size(max = 20)
    private String eventType;

    public static AttendanceDto fromEntity(AttendanceEvent event) {
        return AttendanceDto.builder()
                .id(event.getId())
                .employeeId(event.getEmployeeId())
                .eventTime(event.getEventTime())
                .eventType(event.getEventType())
                .build();
    }

    public AttendanceEvent toEntity() {
        return AttendanceEvent.builder()
                .id(id)
                .employeeId(employeeId)
                .eventTime(eventTime)
                .eventType(eventType)
                .build();
    }
}
