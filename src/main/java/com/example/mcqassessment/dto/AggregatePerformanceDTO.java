package com.example.mcqassessment.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregatePerformanceDTO {
    private Long assessmentId;
    private Double averageScore;
    private int totalAttempts;
    private int totalStudents;
}
