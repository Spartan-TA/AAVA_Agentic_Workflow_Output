package com.example.onboarding;

import org.springframework.stereotype.Service;

@Service
public class OnboardingService {
    public String onboardEmployee(String employeeId) {
        // TODO: Implement onboarding logic
        return "Employee " + employeeId + " onboarded successfully.";
    }
}