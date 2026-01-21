package com.wms.attendance;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Attendance record DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecordDTO {
    private Long id;
    private Long employeeId;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private Long shiftId;
    private String deviceInfo;
    private String status;
    private LocalDateTime createdAt;
}