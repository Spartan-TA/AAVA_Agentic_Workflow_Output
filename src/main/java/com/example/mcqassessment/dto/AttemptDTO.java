package com.example.mcqassessment.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttemptDTO {
    private Long id;
    private Long assessmentId;
    private String studentUsername;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Double score;
    private List<AttemptedQuestionDTO> attemptedQuestions;
}
