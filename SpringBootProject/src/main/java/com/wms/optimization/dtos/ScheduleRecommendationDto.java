package com.wms.optimization.dtos;

import lombok.Data;

/**
 * DTO for schedule recommendations.
 */
@Data
public class ScheduleRecommendationDto {
    private Long shiftId;
    private Long employeeId;
    private double score;
    private String reasoning;
}
