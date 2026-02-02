package com.wms.optimization.dtos;

import lombok.Data;
import java.time.LocalDate;
import java.util.Map;

/**
 * DTO for scheduling optimization requests.
 */
@Data
public class SchedulingRequestDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<String, Object> constraints; // e.g., maxHours, minRest, skillRequirements
}
