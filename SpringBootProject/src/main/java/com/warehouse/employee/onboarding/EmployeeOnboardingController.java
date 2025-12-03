package com.warehouse.employee.onboarding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/api/onboarding")
public class EmployeeOnboardingController {
    @Autowired
    private EmployeeOnboardingService onboardingService;

    /**
     * Initiate onboarding for a new employee.
     */
    @PreAuthorize("hasRole('HR')")
    @PostMapping
    public ResponseEntity<Employee> initiateOnboarding(@RequestBody Employee employee) {
        Employee saved = onboardingService.initiateOnboarding(employee);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Get onboarding status for an employee.
     */
    @PreAuthorize("hasRole('HR')")
    @GetMapping("/{id}")
    public ResponseEntity<List<OnboardingTask>> getOnboardingStatus(@PathVariable Long id) {
        List<OnboardingTask> tasks = onboardingService.getOnboardingStatus(id);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Complete an onboarding task.
     */
    @PreAuthorize("hasRole('HR')")
    @PatchMapping("/task/{taskId}/complete")
    public ResponseEntity<OnboardingTask> completeTask(@PathVariable Long taskId) {
        OnboardingTask task = onboardingService.completeTask(taskId);
        return ResponseEntity.ok(task);
    }
}
