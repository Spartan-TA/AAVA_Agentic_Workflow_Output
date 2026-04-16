package com.example.mcqassessment.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerChoiceDTO {
    private Long id;
    private String label;
    private String text;
}
