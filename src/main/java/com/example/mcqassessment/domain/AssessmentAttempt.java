package com.example.mcqassessment.domain;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "assessment_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id")
    private Assessment assessment;

    @Column(nullable = false)
    private String studentUsername;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private Double score;

    @OneToMany(mappedBy = "assessmentAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttemptedQuestion> attemptedQuestions;
}
