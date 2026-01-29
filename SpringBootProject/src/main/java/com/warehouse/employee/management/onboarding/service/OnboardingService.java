package com.warehouse.employee.management.onboarding.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class OnboardingService {
    private final List<String> onboardingLogs = new ArrayList<>();

    @Transactional
    public void provisionEmployee(Long employeeId) {
        onboardingLogs.add("Provisioned employee: " + employeeId + " at " + new Date());
    }

    @Transactional
    public void deprovisionEmployee(Long employeeId) {
        onboardingLogs.add("Deprovisioned employee: " + employeeId + " at " + new Date());
    }

    public List<String> getOnboardingLogs() {
        return Collections.unmodifiableList(onboardingLogs);
    }
}
