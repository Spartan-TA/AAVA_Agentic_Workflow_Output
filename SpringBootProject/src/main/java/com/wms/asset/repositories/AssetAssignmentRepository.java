package com.wms.asset.repositories;

import com.wms.asset.model.AssetAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing AssetAssignment entities
 */
@Repository
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Long> {
    /**
     * Find all assignments for an employee
     * @param employeeId Employee ID
     * @return List of AssetAssignment
     */
    List<AssetAssignment> findByEmployeeId(Long employeeId);
}
