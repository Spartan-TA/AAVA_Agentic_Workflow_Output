package com.example.warehousemanagement.repository;

import com.example.warehousemanagement.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for Asset entity.
 */
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByType(Asset.AssetType type);
    List<Asset> findByStatus(Asset.Status status);
    List<Asset> findByCurrentAssigneeId(Long employeeId);
}
