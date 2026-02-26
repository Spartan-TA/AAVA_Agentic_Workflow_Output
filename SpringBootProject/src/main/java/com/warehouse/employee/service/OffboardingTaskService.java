package com.warehouse.employee.service;

import com.warehouse.employee.entity.OffboardingTask;
import com.warehouse.employee.repository.OffboardingTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OffboardingTaskService {
    @Autowired
    private OffboardingTaskRepository offboardingTaskRepository;

    public List<OffboardingTask> getAllOffboardingTasks() {
        return offboardingTaskRepository.findAll();
    }

    public Optional<OffboardingTask> getOffboardingTaskById(Long id) {
        return offboardingTaskRepository.findById(id);
    }

    public OffboardingTask saveOffboardingTask(OffboardingTask offboardingTask) {
        return offboardingTaskRepository.save(offboardingTask);
    }

    public void deleteOffboardingTask(Long id) {
        offboardingTaskRepository.deleteById(id);
    }
}
