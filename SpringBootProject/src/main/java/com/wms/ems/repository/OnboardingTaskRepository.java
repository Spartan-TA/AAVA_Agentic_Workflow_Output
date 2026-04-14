package com.wms.ems.repository;

import com.wms.ems.entity.OnboardingTask;
import com.wms.ems.entity.Employee;
import com.wms.ems.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Repository interface for OnboardingTask entity operations.
 * Provides CRUD and custom query methods for onboarding task management.
 */
public interface OnboardingTaskRepository extends JpaRepository<OnboardingTask, Long> {
    /**
     * Find onboarding tasks by employee and status.
     * @param employee the employee
     * @param status the task status
     * @return List of OnboardingTasks
     */
    List<OnboardingTask> findByEmployeeAndStatus(Employee employee, TaskStatus status);

    /**
     * Find all pending onboarding tasks.
     * @return List of pending OnboardingTasks
     */
    @Query("SELECT t FROM OnboardingTask t WHERE t.status = 'PENDING'")
    List<OnboardingTask> findPendingTasks();
}
