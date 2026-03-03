package com.wms.ems.asset.repository;

import com.wms.ems.asset.entity.AssetAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for AssetAssignment entity operations.
 * Provides CRUD operations and custom queries for asset assignment management.
 */
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Long> {

    /**
     * Finds asset assignments for an employee where return time is null.
     * @param employeeId the employee ID
     * @return a list of asset assignments with null return time
     */
    List<AssetAssignment> findByEmployeeIdAndReturnTimeIsNull(Long employeeId);

    /**
     * Finds asset assignments for an asset where return time is null.
     * @param assetId the asset ID
     * @return a list of asset assignments with null return time
     */
    List<AssetAssignment> findByAssetIdAndReturnTimeIsNull(Long assetId);
}
