package com.example.warehouse.service;

import com.example.warehouse.entity.AssetAssignment;
import com.example.warehouse.repository.AssetAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for AssetAssignment operations.
 */
@Service
public class AssetService {
    @Autowired
    private AssetAssignmentRepository assetAssignmentRepository;

    public List<AssetAssignment> getAssignmentsForEmployee(Long employeeId) {
        return assetAssignmentRepository.findByEmployee(employeeId);
    }

    public List<AssetAssignment> getActiveAssignmentsByAsset(Long assetId) {
        return assetAssignmentRepository.findActiveAssignmentsByAsset(assetId);
    }

    @Transactional
    public AssetAssignment checkOutAsset(AssetAssignment assignment) {
        // Check if asset is already assigned
        List<AssetAssignment> active = assetAssignmentRepository.findActiveAssignmentsByAsset(assignment.getAsset().getId());
        if (!active.isEmpty()) {
            throw new IllegalStateException("Asset is already checked out.");
        }
        assignment.setReturned(false);
        return assetAssignmentRepository.save(assignment);
    }

    @Transactional
    public AssetAssignment checkInAsset(Long assignmentId) {
        AssetAssignment assignment = assetAssignmentRepository.findById(assignmentId).orElseThrow();
        assignment.setReturned(true);
        return assetAssignmentRepository.save(assignment);
    }
}
