package com.example.mcqassessment.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentPerformanceDTO {
    private String studentUsername;
    private Long assessmentId;
    private Double score;
    private int totalQuestions;
    private int correctAnswers;
    private int incorrectAnswers;
}
