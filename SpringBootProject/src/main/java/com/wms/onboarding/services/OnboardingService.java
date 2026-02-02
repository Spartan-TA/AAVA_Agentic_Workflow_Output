package com.wms.onboarding.services;

import com.wms.employee.dtos.EmployeeDto;
import org.springframework.stereotype.Service;

/**
 * Service for onboarding workflow.
 */
@Service
public class OnboardingService {
    /**
     * Onboards a new employee: triggers HRIS sync, asset assignment, training tasks.
     * @param dto EmployeeDto
     */
    public void onboardEmployee(EmployeeDto dto) {
        // TODO: Integrate with HRIS, assign assets, create training tasks
    }
}
