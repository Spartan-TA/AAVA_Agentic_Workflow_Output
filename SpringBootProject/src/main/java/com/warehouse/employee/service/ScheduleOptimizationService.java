package com.warehouse.employee.service;

import com.warehouse.employee.entity.ScheduleOptimization;
import com.warehouse.employee.repository.ScheduleOptimizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ScheduleOptimizationService {
    @Autowired
    private ScheduleOptimizationRepository scheduleOptimizationRepository;

    public List<ScheduleOptimization> getAllScheduleOptimizations() {
        return scheduleOptimizationRepository.findAll();
    }

    public Optional<ScheduleOptimization> getScheduleOptimizationById(Long id) {
        return scheduleOptimizationRepository.findById(id);
    }

    public ScheduleOptimization saveScheduleOptimization(ScheduleOptimization scheduleOptimization) {
        return scheduleOptimizationRepository.save(scheduleOptimization);
    }

    public void deleteScheduleOptimization(Long id) {
        scheduleOptimizationRepository.deleteById(id);
    }
}
