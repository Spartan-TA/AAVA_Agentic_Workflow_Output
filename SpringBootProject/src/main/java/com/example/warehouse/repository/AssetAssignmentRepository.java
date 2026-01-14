package com.example.warehouse.repository;

import com.example.warehouse.entity.AssetAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Repository for AssetAssignment entity.
 */
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Long> {
    @Query("SELECT a FROM AssetAssignment a WHERE a.employee.id = :employeeId")
    List<AssetAssignment> findByEmployee(@Param("employeeId") Long employeeId);

    @Query("SELECT a FROM AssetAssignment a WHERE a.asset.id = :assetId AND a.returned = false")
    List<AssetAssignment> findActiveAssignmentsByAsset(@Param("assetId") Long assetId);
}
