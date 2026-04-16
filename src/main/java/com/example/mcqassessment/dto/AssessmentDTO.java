package com.example.mcqassessment.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentDTO {
    private Long id;
    private String title;
    private String week;
    private String topic;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<QuestionDTO> questions;
}
