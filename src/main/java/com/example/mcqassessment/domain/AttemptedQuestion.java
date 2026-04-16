package com.example.mcqassessment.domain;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "attempted_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttemptedQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(nullable = false)
    private String selectedChoice; // a, b, c, d

    @Column(nullable = false)
    private boolean isCorrect;

    @Column(length = 2000)
    private String feedback;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_attempt_id")
    private AssessmentAttempt assessmentAttempt;
}
