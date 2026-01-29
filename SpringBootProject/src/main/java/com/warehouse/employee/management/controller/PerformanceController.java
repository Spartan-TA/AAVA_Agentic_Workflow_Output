package com.warehouse.employee.management.controller;

import com.warehouse.employee.management.dto.PerformanceReviewDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/reviews")
@Validated
public class PerformanceController {
    private final List<PerformanceReviewDto> reviews = new ArrayList<>();

    @PreAuthorize("hasAuthority('PERFORMANCE_ADD')")
    @PostMapping
    public PerformanceReviewDto addReview(@Valid @RequestBody PerformanceReviewDto reviewDto) {
        reviews.add(reviewDto);
        return reviewDto;
    }

    @PreAuthorize("hasAuthority('PERFORMANCE_READ')")
    @GetMapping
    public List<PerformanceReviewDto> getReviews() {
        return Collections.unmodifiableList(reviews);
    }

    @PreAuthorize("hasAuthority('PERFORMANCE_CYCLE')")
    @GetMapping("/cycle/{period}")
    public List<PerformanceReviewDto> getReviewsByPeriod(@PathVariable String period) {
        List<PerformanceReviewDto> result = new ArrayList<>();
        for (PerformanceReviewDto r : reviews) {
            if (r.getPeriod().equalsIgnoreCase(period)) {
                result.add(r);
            }
        }
        return result;
    }
}
