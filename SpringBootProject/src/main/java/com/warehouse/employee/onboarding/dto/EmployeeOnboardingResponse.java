package com.warehouse.employee.onboarding.dto;

import lombok.Data;
import java.util.List;
import com.warehouse.employee.onboarding.OnboardingTask;

@Data
public class EmployeeOnboardingResponse {
    private Long employeeId;
    private String name;
    private String badgeId;
    private String status;
    private List<OnboardingTask> tasks;
}
