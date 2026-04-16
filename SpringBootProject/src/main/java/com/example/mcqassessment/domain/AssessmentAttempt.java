package com.example.mcqassessment.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentUsername;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private Double score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id")
    private Assessment assessment;

    @OneToMany(mappedBy = "assessmentAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttemptedQuestion> attemptedQuestions;
}