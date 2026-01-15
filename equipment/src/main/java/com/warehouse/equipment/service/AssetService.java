package com.warehouse.equipment.service;

import com.warehouse.equipment.entity.Asset;
import com.warehouse.equipment.entity.AssetAssignment;
import com.warehouse.equipment.repository.AssetRepository;
import com.warehouse.equipment.repository.AssetAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AssetService {
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private AssetAssignmentRepository assetAssignmentRepository;

    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    public Optional<Asset> getAssetById(Long id) {
        return assetRepository.findById(id);
    }

    public Asset addAsset(Asset asset) {
        asset.setStatus(Asset.Status.AVAILABLE);
        return assetRepository.save(asset);
    }

    @Transactional
    public AssetAssignment assignAsset(Long assetId, Long employeeId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found"));
        if (asset.getStatus() != Asset.Status.AVAILABLE) {
            throw new IllegalStateException("Asset not available for assignment");
        }
        asset.setStatus(Asset.Status.ASSIGNED);
        assetRepository.save(asset);
        AssetAssignment assignment = AssetAssignment.builder()
                .assetId(assetId)
                .employeeId(employeeId)
                .assignedDate(LocalDate.now())
                .build();
        return assetAssignmentRepository.save(assignment);
    }

    @Transactional
    public AssetAssignment checkInAsset(Long assignmentId) {
        AssetAssignment assignment = assetAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        Asset asset = assetRepository.findById(assignment.getAssetId())
                .orElseThrow(() -> new IllegalArgumentException("Asset not found"));
        asset.setStatus(Asset.Status.AVAILABLE);
        assetRepository.save(asset);
        assignment.setReturnedDate(LocalDate.now());
        return assetAssignmentRepository.save(assignment);
    }

    public boolean validateCertification(Long employeeId, String assetType) {
        // Simulate certification validation logic
        // In real implementation, query CertificationRepository
        return true;
    }
}
