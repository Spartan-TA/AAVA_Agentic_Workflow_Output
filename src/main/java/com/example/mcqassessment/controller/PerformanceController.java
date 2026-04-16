package com.example.mcqassessment.controller;

import com.example.mcqassessment.dto.AggregatePerformanceDTO;
import com.example.mcqassessment.dto.StudentPerformanceDTO;
import com.example.mcqassessment.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceController {
    @Autowired
    private final PerformanceService performanceService;

    @PreAuthorize("hasAnyRole('TEACHER', 'CURRICULUM_PLANNER')")
    @GetMapping("/assessment/{id}/student/{username}")
    public ResponseEntity<StudentPerformanceDTO> getStudentPerformance(@PathVariable Long id, @PathVariable String username) {
        StudentPerformanceDTO dto = performanceService.getStudentPerformance(id, username);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'CURRICULUM_PLANNER')")
    @GetMapping("/assessment/{id}/aggregate")
    public ResponseEntity<AggregatePerformanceDTO> getAggregatePerformance(@PathVariable Long id) {
        AggregatePerformanceDTO dto = performanceService.getAggregatePerformance(id);
        return ResponseEntity.ok(dto);
    }
}
