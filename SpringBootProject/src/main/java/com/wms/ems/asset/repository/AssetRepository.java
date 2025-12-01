package com.wms.ems.asset.repository;

import com.wms.ems.asset.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for Asset entity.
 * Provides CRUD operations and custom queries for assets.
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    /**
     * Find all assets by status.
     * @param status the status of the asset (e.g., AVAILABLE, CHECKED_OUT)
     * @return List of Asset
     */
    List<Asset> findByStatus(String status);

    /**
     * Find all assets assigned to a specific employee.
     * @param assignedTo the employee's ID
     * @return List of Asset
     */
    List<Asset> findByAssignedTo(Long assignedTo);
}
