package com.wms.ems.attendance.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CorrectionDto {
    private Long employeeId;
    private LocalDateTime originalTimestamp;
    private LocalDateTime correctedTimestamp;
    private String reason;
}
