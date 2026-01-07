package com.example.usermanagement.repository;

import com.example.usermanagement.entity.ProfileAudit;
import com.example.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ProfileAudit entity.
 * Provides CRUD operations and custom queries for profile audits.
 */
@Repository
public interface ProfileAuditRepository extends JpaRepository<ProfileAudit, Long> {
    /**
     * Find all profile audits for a user, ordered by date descending.
     * @param user User entity
     * @return List of profile audits
     */
    List<ProfileAudit> findByUserOrderByChangedAtDesc(User user);
}
