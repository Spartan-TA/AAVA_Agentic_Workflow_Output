package com.example.ems.service;

import com.example.ems.entity.OffboardingTask;
import com.example.ems.repository.OffboardingTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OffboardingTaskService {

    @Autowired
    private OffboardingTaskRepository offboardingTaskRepository;

    public List<OffboardingTask> getAllTasks() {
        return offboardingTaskRepository.findAll();
    }

    public Optional<OffboardingTask> getTaskById(Long id) {
        return offboardingTaskRepository.findById(id);
    }

    public OffboardingTask createTask(OffboardingTask task) {
        return offboardingTaskRepository.save(task);
    }

    public OffboardingTask updateTask(Long id, OffboardingTask updatedTask) {
        return offboardingTaskRepository.findById(id)
                .map(existing -> {
                    existing.setTaskName(updatedTask.getTaskName());
                    existing.setDescription(updatedTask.getDescription());
                    existing.setStatus(updatedTask.getStatus());
                    existing.setDueDate(updatedTask.getDueDate());
                    existing.setCompletedDate(updatedTask.getCompletedDate());
                    return offboardingTaskRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("OffboardingTask not found"));
    }

    public void deleteTask(Long id) {
        offboardingTaskRepository.deleteById(id);
    }
}
