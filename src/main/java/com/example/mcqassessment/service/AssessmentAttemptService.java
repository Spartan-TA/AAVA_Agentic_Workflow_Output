package com.example.mcqassessment.service;

import com.example.mcqassessment.domain.*;
import com.example.mcqassessment.dto.AttemptDTO;
import com.example.mcqassessment.repository.AssessmentAttemptRepository;
import com.example.mcqassessment.repository.AssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssessmentAttemptService {
    @Autowired
    private final AssessmentAttemptRepository attemptRepository;
    @Autowired
    private final AssessmentRepository assessmentRepository;

    public AssessmentAttempt startAttempt(Long assessmentId, String studentUsername) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));
        AssessmentAttempt attempt = AssessmentAttempt.builder()
                .assessment(assessment)
                .studentUsername(studentUsername)
                .startedAt(LocalDateTime.now())
                .build();
        return attemptRepository.save(attempt);
    }

    public AssessmentAttempt submitAttempt(Long attemptId, List<AttemptedQuestion> attemptedQuestions) {
        AssessmentAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
        attempt.setAttemptedQuestions(attemptedQuestions);
        attempt.setCompletedAt(LocalDateTime.now());
        long correct = attemptedQuestions.stream().filter(AttemptedQuestion::isCorrect).count();
        attempt.setScore((double) correct / attemptedQuestions.size() * 100.0);
        return attemptRepository.save(attempt);
    }
}
