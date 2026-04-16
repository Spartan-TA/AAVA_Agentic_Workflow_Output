package com.example.mcqassessment.domain;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "answer_choices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerChoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String label; // a, b, c, d

    @Column(nullable = false, length = 1000)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;
}
