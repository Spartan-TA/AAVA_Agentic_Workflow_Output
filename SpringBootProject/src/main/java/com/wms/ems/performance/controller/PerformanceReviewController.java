package com.wms.ems.performance.controller;

import com.wms.ems.performance.dto.PerformanceReviewDto;
import com.wms.ems.performance.service.PerformanceReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Performance Reviews", description = "Endpoints for employee performance reviews management")
public class PerformanceReviewController {
    private final PerformanceReviewService reviewService;

    @Operation(summary = "Create performance review")
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> createReview(@Valid @RequestBody PerformanceReviewDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return ResponseEntity.ok(reviewService.createReview(dto));
    }

    @Operation(summary = "Get all performance reviews")
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<List<PerformanceReviewDto>> getReviews(@RequestParam(required = false) String employeeId) {
        return ResponseEntity.ok(reviewService.getReviews(employeeId));
    }

    @Operation(summary = "Update performance review")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> updateReview(@PathVariable Long id, @Valid @RequestBody PerformanceReviewDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return ResponseEntity.ok(reviewService.updateReview(id, dto));
    }

    @Operation(summary = "Delete performance review")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Acknowledge performance review")
    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> acknowledgeReview(@PathVariable Long id) {
        reviewService.acknowledgeReview(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get performance review as PDF")
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','EMPLOYEE')")
    public ResponseEntity<byte[]> getReviewPdf(@PathVariable Long id) {
        byte[] pdf = reviewService.getReviewPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=review-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
