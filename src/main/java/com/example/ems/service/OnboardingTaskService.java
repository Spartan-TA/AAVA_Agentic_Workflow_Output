package com.example.ems.service;

import com.example.ems.entity.OnboardingTask;
import com.example.ems.repository.OnboardingTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OnboardingTaskService {

    @Autowired
    private OnboardingTaskRepository onboardingTaskRepository;

    public List<OnboardingTask> getAllTasks() {
        return onboardingTaskRepository.findAll();
    }

    public Optional<OnboardingTask> getTaskById(Long id) {
        return onboardingTaskRepository.findById(id);
    }

    public OnboardingTask createTask(OnboardingTask task) {
        return onboardingTaskRepository.save(task);
    }

    public OnboardingTask updateTask(Long id, OnboardingTask updatedTask) {
        return onboardingTaskRepository.findById(id)
                .map(existing -> {
                    existing.setTaskName(updatedTask.getTaskName());
                    existing.setDescription(updatedTask.getDescription());
                    existing.setStatus(updatedTask.getStatus());
                    existing.setDueDate(updatedTask.getDueDate());
                    existing.setCompletedDate(updatedTask.getCompletedDate());
                    return onboardingTaskRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("OnboardingTask not found"));
    }

    public void deleteTask(Long id) {
        onboardingTaskRepository.deleteById(id);
    }
}
