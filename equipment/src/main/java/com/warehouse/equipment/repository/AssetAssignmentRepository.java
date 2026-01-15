package com.warehouse.equipment.repository;

import com.warehouse.equipment.entity.AssetAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Long> {
    List<AssetAssignment> findByEmployeeId(Long employeeId);
    List<AssetAssignment> findByAssetId(Long assetId);
    List<AssetAssignment> findByReturnedDateIsNull();
}
