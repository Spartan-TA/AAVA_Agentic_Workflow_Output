package com.wms.ems.performance;

import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<PerformanceReview> createReview(@RequestBody PerformanceReview review) {
        return ResponseEntity.ok(reviewService.createReview(review));
    }

    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<PerformanceReview> submitReview(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.submitReview(id));
    }

    @PutMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<PerformanceReview> acknowledgeReview(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.acknowledgeReview(id));
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<List<PerformanceReview>> getReviews(@PathVariable Long employeeId) {
        return ResponseEntity.ok(reviewService.getReviews(employeeId));
    }

    @GetMapping("/export/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<byte[]> exportReviewToPdf(@PathVariable Long id) throws DocumentException {
        byte[] pdf = reviewService.exportReviewToPdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "review-" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
