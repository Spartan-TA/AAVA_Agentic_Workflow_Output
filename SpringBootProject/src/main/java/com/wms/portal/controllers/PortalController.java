package com.wms.portal.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * REST controller for self-service portal endpoints.
 */
@RestController
@RequestMapping("/api/portal")
public class PortalController {

    /**
     * Endpoint for updating employee profile.
     */
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(@RequestBody Map<String, Object> profile) {
        // TODO: Implement profile update logic
        return ResponseEntity.ok("Profile updated successfully.");
    }

    /**
     * Endpoint for requesting a schedule swap.
     */
    @PostMapping("/schedule/swap")
    public ResponseEntity<String> requestScheduleSwap(@RequestBody Map<String, Object> swapRequest) {
        // TODO: Implement schedule swap logic
        return ResponseEntity.ok("Schedule swap requested.");
    }

    /**
     * Endpoint for retrieving FAQs.
     */
    @GetMapping("/faqs")
    public ResponseEntity<List<String>> getFaqs() {
        // TODO: Return actual FAQs
        return ResponseEntity.ok(List.of("How do I reset my password?", "How do I request leave?"));
    }
}
