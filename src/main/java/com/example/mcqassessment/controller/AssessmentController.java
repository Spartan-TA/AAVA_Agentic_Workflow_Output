package com.example.mcqassessment.controller;

import com.example.mcqassessment.domain.Assessment;
import com.example.mcqassessment.dto.AssessmentDTO;
import com.example.mcqassessment.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class AssessmentController {
    @Autowired
    private final AssessmentService assessmentService;

    @PreAuthorize("hasAnyRole('TEACHER', 'CURRICULUM_PLANNER')")
    @PostMapping
    public ResponseEntity<AssessmentDTO> createAssessment(@Valid @RequestBody AssessmentDTO dto) {
        Assessment assessment = assessmentService.createAssessment(dto);
        dto.setId(assessment.getId());
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'CURRICULUM_PLANNER', 'STUDENT')")
    @GetMapping
    public ResponseEntity<List<AssessmentDTO>> getAssessments(@RequestParam String week, @RequestParam String topic) {
        List<Assessment> assessments = assessmentService.findByWeekAndTopic(week, topic);
        List<AssessmentDTO> dtos = assessments.stream().map(a -> AssessmentDTO.builder()
                .id(a.getId())
                .title(a.getTitle())
                .week(a.getWeek())
                .topic(a.getTopic())
                .createdBy(a.getCreatedBy())
                .createdAt(a.getCreatedAt())
                .build()).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
