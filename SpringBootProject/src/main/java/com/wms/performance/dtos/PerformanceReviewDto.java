package com.wms.performance.dtos;

import lombok.*;
import java.time.LocalDate;

/**
 * Data Transfer Object for PerformanceReview
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReviewDto {
    private Long id;
    private Long employeeId;
    private Long reviewerId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer rating;
    private String comments;
    private String goals;
}
