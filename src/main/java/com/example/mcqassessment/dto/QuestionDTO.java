package com.example.mcqassessment.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDTO {
    private Long id;
    private String assertionText;
    private String reasonText;
    private String explanation;
    private List<AnswerChoiceDTO> answerChoices;
    private String correctChoice;
}
