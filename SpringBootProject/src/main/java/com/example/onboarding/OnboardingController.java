package com.example.onboarding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    @Autowired
    private OnboardingService onboardingService;

    @PostMapping("/{employeeId}")
    public String onboardEmployee(@PathVariable String employeeId) {
        return onboardingService.onboardEmployee(employeeId);
    }
}