package com.wms.integration.dtos;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO for employee event webhook payloads.
 */
@Data
public class EmployeeEventDto {
    private Long employeeId;
    private String eventType; // e.g., NEW_HIRE, TERMINATION, PROMOTION
    private LocalDateTime eventTimestamp;
    private String payload; // JSON or text
}
