package com.example.mcqassessment.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttemptedQuestionDTO {
    private Long id;
    private Long questionId;
    private String selectedChoice;
    private boolean isCorrect;
    private String feedback;
}
