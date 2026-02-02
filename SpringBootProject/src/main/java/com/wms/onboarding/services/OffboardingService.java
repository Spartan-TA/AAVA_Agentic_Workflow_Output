package com.wms.onboarding.services;

import org.springframework.stereotype.Service;

/**
 * Service for offboarding workflow.
 */
@Service
public class OffboardingService {
    /**
     * Offboards an employee: revokes access, collects assets, updates schedules.
     * @param employeeId Employee ID
     */
    public void offboardEmployee(Long employeeId) {
        // TODO: Revoke access, collect assets, update schedules
    }
}
