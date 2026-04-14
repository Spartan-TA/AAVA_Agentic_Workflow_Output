package com.wms.ems.repository;

import com.wms.ems.entity.Goal;
import com.wms.ems.entity.Employee;
import com.wms.ems.entity.PerformanceReview;
import com.wms.ems.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for Goal entity operations.
 * Provides CRUD and custom query methods for goal management.
 */
public interface GoalRepository extends JpaRepository<Goal, Long> {
    /**
     * Find goals by employee and status.
     * @param employee the employee
     * @param status the goal status
     * @return List of Goals
     */
    List<Goal> findByEmployeeAndStatus(Employee employee, GoalStatus status);

    /**
     * Find goals by performance review.
     * @param review the performance review
     * @return List of Goals
     */
    List<Goal> findByReview(PerformanceReview review);
}
