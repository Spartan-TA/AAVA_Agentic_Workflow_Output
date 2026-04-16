package com.example.mcqassessment.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttemptFeedbackDTO {
    private Long attemptId;
    private List<AttemptedQuestionDTO> feedback;
}
