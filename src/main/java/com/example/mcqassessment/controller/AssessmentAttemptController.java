package com.example.mcqassessment.controller;

import com.example.mcqassessment.domain.AssessmentAttempt;
import com.example.mcqassessment.domain.AttemptedQuestion;
import com.example.mcqassessment.dto.AttemptDTO;
import com.example.mcqassessment.dto.AttemptFeedbackDTO;
import com.example.mcqassessment.dto.AttemptedQuestionDTO;
import com.example.mcqassessment.service.AssessmentAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attempts")
@RequiredArgsConstructor
public class AssessmentAttemptController {
    @Autowired
    private final AssessmentAttemptService attemptService;

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping
    public ResponseEntity<AttemptDTO> startAttempt(@RequestParam Long assessmentId, @RequestParam String studentUsername) {
        AssessmentAttempt attempt = attemptService.startAttempt(assessmentId, studentUsername);
        AttemptDTO dto = AttemptDTO.builder()
                .id(attempt.getId())
                .assessmentId(attempt.getAssessment().getId())
                .studentUsername(attempt.getStudentUsername())
                .startedAt(attempt.getStartedAt())
                .build();
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{attemptId}/submit")
    public ResponseEntity<AttemptDTO> submitAttempt(@PathVariable Long attemptId, @RequestBody List<AttemptedQuestionDTO> attemptedQuestions) {
        List<AttemptedQuestion> attempted = attemptedQuestions.stream().map(dto -> AttemptedQuestion.builder()
                .question(null) // Should be fetched from DB in real implementation
                .selectedChoice(dto.getSelectedChoice())
                .isCorrect(dto.isCorrect())
                .feedback(dto.getFeedback())
                .build()).collect(Collectors.toList());
        AssessmentAttempt attempt = attemptService.submitAttempt(attemptId, attempted);
        AttemptDTO dto = AttemptDTO.builder()
                .id(attempt.getId())
                .assessmentId(attempt.getAssessment().getId())
                .studentUsername(attempt.getStudentUsername())
                .startedAt(attempt.getStartedAt())
                .completedAt(attempt.getCompletedAt())
                .score(attempt.getScore())
                .build();
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/{attemptId}/feedback")
    public ResponseEntity<AttemptFeedbackDTO> getAttemptFeedback(@PathVariable Long attemptId) {
        // In real implementation, fetch AttemptedQuestions from DB
        AttemptFeedbackDTO feedbackDTO = AttemptFeedbackDTO.builder()
                .attemptId(attemptId)
                .feedback(List.of())
                .build();
        return ResponseEntity.ok(feedbackDTO);
    }
}
