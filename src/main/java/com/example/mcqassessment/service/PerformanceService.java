package com.example.mcqassessment.service;

import com.example.mcqassessment.domain.AssessmentAttempt;
import com.example.mcqassessment.dto.AggregatePerformanceDTO;
import com.example.mcqassessment.dto.StudentPerformanceDTO;
import com.example.mcqassessment.repository.AssessmentAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerformanceService {
    @Autowired
    private final AssessmentAttemptRepository attemptRepository;

    public StudentPerformanceDTO getStudentPerformance(Long assessmentId, String studentUsername) {
        List<AssessmentAttempt> attempts = attemptRepository.findAll().stream()
                .filter(a -> a.getAssessment().getId().equals(assessmentId) && a.getStudentUsername().equals(studentUsername))
                .collect(Collectors.toList());
        if (attempts.isEmpty()) return null;
        AssessmentAttempt attempt = attempts.get(attempts.size() - 1);
        int totalQuestions = attempt.getAttemptedQuestions() != null ? attempt.getAttemptedQuestions().size() : 0;
        int correct = (int) attempt.getAttemptedQuestions().stream().filter(aq -> aq.isCorrect()).count();
        int incorrect = totalQuestions - correct;
        return StudentPerformanceDTO.builder()
                .studentUsername(studentUsername)
                .assessmentId(assessmentId)
                .score(attempt.getScore())
                .totalQuestions(totalQuestions)
                .correctAnswers(correct)
                .incorrectAnswers(incorrect)
                .build();
    }

    public AggregatePerformanceDTO getAggregatePerformance(Long assessmentId) {
        List<AssessmentAttempt> attempts = attemptRepository.findAll().stream()
                .filter(a -> a.getAssessment().getId().equals(assessmentId))
                .collect(Collectors.toList());
        double avg = attempts.stream().mapToDouble(a -> a.getScore() != null ? a.getScore() : 0.0).average().orElse(0.0);
        long students = attempts.stream().map(AssessmentAttempt::getStudentUsername).distinct().count();
        return AggregatePerformanceDTO.builder()
                .assessmentId(assessmentId)
                .averageScore(avg)
                .totalAttempts(attempts.size())
                .totalStudents((int) students)
                .build();
    }
}
