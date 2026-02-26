package com.warehouse.employee.repository;

import com.warehouse.employee.entity.OffboardingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OffboardingTaskRepository extends JpaRepository<OffboardingTask, Long> {
}
