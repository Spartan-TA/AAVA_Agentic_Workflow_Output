package com.example.warehouse.onboarding;

import org.springframework.stereotype.Service;

@Service
public class OffboardingService {
    public String offboardEmployee(Long employeeId) {
        // TODO: Implement offboarding logic
        return "Employee " + employeeId + " offboarded successfully";
    }
}
