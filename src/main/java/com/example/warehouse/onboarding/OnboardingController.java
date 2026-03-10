package com.example.warehouse.onboarding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {
    @Autowired
    private OnboardingService onboardingService;
    @Autowired
    private OffboardingService offboardingService;

    @PostMapping("/onboard/{employeeId}")
    public ResponseEntity<String> onboardEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(onboardingService.onboardEmployee(employeeId));
    }

    @PostMapping("/offboard/{employeeId}")
    public ResponseEntity<String> offboardEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(offboardingService.offboardEmployee(employeeId));
    }
}
