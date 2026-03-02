package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Asset;
import com.warehouse.employee.domain.Asset.AssetType;
import com.warehouse.employee.domain.Asset.Status;
import com.warehouse.employee.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Asset entity.
 */
public interface AssetRepository extends JpaRepository<Asset, Long> {
    /**
     * Find assets assigned to an employee.
     */
    List<Asset> findByAssignedTo(Employee employee);

    /**
     * Find assets by status.
     */
    List<Asset> findByStatus(Status status);

    /**
     * Find assets by asset type.
     */
    List<Asset> findByAssetType(AssetType assetType);

    /**
     * Find overdue assets (dueDate before now and status ASSIGNED).
     */
    @Query("SELECT a FROM Asset a WHERE a.dueDate < :now AND a.status = 'ASSIGNED'")
    List<Asset> findOverdueAssets(@Param("now") LocalDateTime now);
}
