package com.example.usermanagement.repository;

import com.example.usermanagement.entity.ReportSchedule;
import com.example.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ReportSchedule entity.
 * Provides CRUD operations and custom queries for report schedules.
 */
@Repository
public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, Long> {
    /**
     * Find all report schedules for a user.
     * @param user User entity
     * @return List of report schedules
     */
    List<ReportSchedule> findByUser(User user);
}
