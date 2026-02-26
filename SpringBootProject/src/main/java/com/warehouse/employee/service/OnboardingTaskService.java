package com.warehouse.employee.service;

import com.warehouse.employee.entity.OnboardingTask;
import com.warehouse.employee.repository.OnboardingTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OnboardingTaskService {
    @Autowired
    private OnboardingTaskRepository onboardingTaskRepository;

    public List<OnboardingTask> getAllOnboardingTasks() {
        return onboardingTaskRepository.findAll();
    }

    public Optional<OnboardingTask> getOnboardingTaskById(Long id) {
        return onboardingTaskRepository.findById(id);
    }

    public OnboardingTask saveOnboardingTask(OnboardingTask onboardingTask) {
        return onboardingTaskRepository.save(onboardingTask);
    }

    public void deleteOnboardingTask(Long id) {
        onboardingTaskRepository.deleteById(id);
    }
}
