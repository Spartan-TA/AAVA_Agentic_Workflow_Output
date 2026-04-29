package com.example.ems.repository;

import com.example.ems.entity.OffboardingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OffboardingTaskRepository extends JpaRepository<OffboardingTask, Long> {
    // Custom query methods if needed
}
