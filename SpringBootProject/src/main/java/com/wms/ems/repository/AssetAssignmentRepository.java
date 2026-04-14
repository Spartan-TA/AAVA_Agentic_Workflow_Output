package com.wms.ems.repository;

import com.wms.ems.entity.AssetAssignment;
import com.wms.ems.entity.Employee;
import com.wms.ems.entity.Asset;
import com.wms.ems.enums.AssetAssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.time.LocalDate;

/**
 * Repository interface for AssetAssignment entity operations.
 * Provides CRUD and custom query methods for asset assignment management.
 */
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Long> {
    /**
     * Find asset assignments by employee and status.
     * @param employee the employee
     * @param status the assignment status
     * @return List of AssetAssignments
     */
    List<AssetAssignment> findByEmployeeAndStatus(Employee employee, AssetAssignmentStatus status);

    /**
     * Find asset assignments by asset.
     * @param asset the asset
     * @return List of AssetAssignments
     */
    List<AssetAssignment> findByAsset(Asset asset);

    /**
     * Custom query to find overdue asset returns.
     * @param date the current date
     * @return List of overdue AssetAssignments
     */
    @Query("SELECT aa FROM AssetAssignment aa WHERE aa.expectedReturnDate < :date AND aa.actualReturnDate IS NULL AND aa.status = 'ASSIGNED'")
    List<AssetAssignment> findOverdueReturns(@Param("date") LocalDate date);
}
