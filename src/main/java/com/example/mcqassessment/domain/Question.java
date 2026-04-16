package com.example.mcqassessment.domain;

import lombok.*;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String assertionText;

    @Column(nullable = false, length = 1000)
    private String reasonText;

    @Column(length = 2000)
    private String explanation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id")
    private Assessment assessment;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnswerChoice> answerChoices;

    @Column(nullable = false)
    private String correctChoice; // e.g., "a", "b", "c", "d"
}
