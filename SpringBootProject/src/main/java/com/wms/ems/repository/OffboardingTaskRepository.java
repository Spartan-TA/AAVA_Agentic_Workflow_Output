package com.wms.ems.repository;

import com.wms.ems.entity.OffboardingTask;
import com.wms.ems.entity.Employee;
import com.wms.ems.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Repository interface for OffboardingTask entity operations.
 * Provides CRUD and custom query methods for offboarding task management.
 */
public interface OffboardingTaskRepository extends JpaRepository<OffboardingTask, Long> {
    /**
     * Find offboarding tasks by employee and status.
     * @param employee the employee
     * @param status the task status
     * @return List of OffboardingTasks
     */
    List<OffboardingTask> findByEmployeeAndStatus(Employee employee, TaskStatus status);

    /**
     * Find all pending offboarding tasks.
     * @return List of pending OffboardingTasks
     */
    @Query("SELECT t FROM OffboardingTask t WHERE t.status = 'PENDING'")
    List<OffboardingTask> findPendingTasks();
}
