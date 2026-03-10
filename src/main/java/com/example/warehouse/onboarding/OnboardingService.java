package com.example.warehouse.onboarding;

import org.springframework.stereotype.Service;

@Service
public class OnboardingService {
    public String onboardEmployee(Long employeeId) {
        // TODO: Implement onboarding logic
        return "Employee " + employeeId + " onboarded successfully";
    }
}
