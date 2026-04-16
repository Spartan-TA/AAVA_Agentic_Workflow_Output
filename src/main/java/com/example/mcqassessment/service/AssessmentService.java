package com.example.mcqassessment.service;

import com.example.mcqassessment.domain.Assessment;
import com.example.mcqassessment.dto.AssessmentDTO;
import com.example.mcqassessment.repository.AssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssessmentService {
    @Autowired
    private final AssessmentRepository assessmentRepository;

    public Assessment createAssessment(AssessmentDTO dto) {
        Assessment assessment = Assessment.builder()
                .title(dto.getTitle())
                .week(dto.getWeek())
                .topic(dto.getTopic())
                .createdBy(dto.getCreatedBy())
                .createdAt(LocalDateTime.now())
                .build();
        return assessmentRepository.save(assessment);
    }

    public List<Assessment> findByWeekAndTopic(String week, String topic) {
        return assessmentRepository.findByWeekAndTopic(week, topic);
    }
}
