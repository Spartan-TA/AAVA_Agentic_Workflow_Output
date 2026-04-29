package com.example.ems.repository;

import com.example.ems.entity.OnboardingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OnboardingTaskRepository extends JpaRepository<OnboardingTask, Long> {
    // Custom query methods if needed
}
