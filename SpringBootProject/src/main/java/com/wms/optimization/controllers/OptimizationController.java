package com.wms.optimization.controllers;

import com.wms.optimization.dtos.SchedulingRequestDto;
import com.wms.optimization.dtos.ScheduleRecommendationDto;
import com.wms.optimization.services.SchedulingOptimizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for scheduling optimization endpoints.
 */
@RestController
@RequestMapping("/api/scheduling/optimization")
@RequiredArgsConstructor
public class OptimizationController {
    private final SchedulingOptimizationService optimizationService;

    /**
     * POST endpoint for shift recommendations.
     * @param dto SchedulingRequestDto
     * @return List of ScheduleRecommendationDto
     */
    @PostMapping("/recommend")
    public ResponseEntity<List<ScheduleRecommendationDto>> recommendShifts(@RequestBody SchedulingRequestDto dto) {
        return ResponseEntity.ok(optimizationService.recommendShifts(dto));
    }
}
