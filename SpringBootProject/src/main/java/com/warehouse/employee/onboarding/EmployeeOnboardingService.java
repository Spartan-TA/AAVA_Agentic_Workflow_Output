package com.warehouse.employee.onboarding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class EmployeeOnboardingService {
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private OnboardingTaskRepository onboardingTaskRepository;

    /**
     * Initiates onboarding for a new employee.
     */
    @Transactional
    public Employee initiateOnboarding(Employee employee) {
        if (employeeRepository.existsByBadgeId(employee.getBadgeId())) {
            throw new IllegalArgumentException("Badge ID already exists");
        }
        employee.setStatus("ONBOARDING");
        Employee saved = employeeRepository.save(employee);
        // Create default onboarding tasks
        onboardingTaskRepository.save(OnboardingTask.builder()
                .employeeId(saved.getId())
                .taskName("Complete paperwork")
                .status("PENDING")
                .build());
        onboardingTaskRepository.save(OnboardingTask.builder()
                .employeeId(saved.getId())
                .taskName("Orientation training")
                .status("PENDING")
                .build());
        return saved;
    }

    /**
     * Completes an onboarding task for an employee.
     */
    @Transactional
    public OnboardingTask completeTask(Long taskId) {
        OnboardingTask task = onboardingTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        task.setStatus("COMPLETED");
        return onboardingTaskRepository.save(task);
    }

    /**
     * Gets onboarding status for an employee.
     */
    public List<OnboardingTask> getOnboardingStatus(Long employeeId) {
        return onboardingTaskRepository.findByEmployeeId(employeeId);
    }
}
