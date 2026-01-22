package com.warehouse.ems.asset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for AssetAssignment entity.
 */
@Repository
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Long> {
    // Custom queries for overdue tracking can be added here
}
