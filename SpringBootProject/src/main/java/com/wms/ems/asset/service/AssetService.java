package com.wms.ems.asset.service;

import com.wms.ems.asset.entity.Asset;
import com.wms.ems.asset.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service class for Asset management.
 * Handles checkout/return and certification validation.
 */
@Service
@Transactional
public class AssetService {
    private final AssetRepository assetRepository;

    @Autowired
    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    /**
     * Checkout an asset to an employee.
     * @param assetId the asset ID
     * @param employeeId the employee's ID
     * @return the updated Asset
     */
    public Asset checkoutAsset(Long assetId, Long employeeId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found"));
        // Certification validation logic can be added here
        asset.setStatus("CHECKED_OUT");
        asset.setAssignedTo(employeeId);
        return assetRepository.save(asset);
    }

    /**
     * Return an asset.
     * @param assetId the asset ID
     * @return the updated Asset
     */
    public Asset returnAsset(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found"));
        asset.setStatus("AVAILABLE");
        asset.setAssignedTo(null);
        return assetRepository.save(asset);
    }

    /**
     * Get all assets by status.
     * @param status the status
     * @return List of Asset
     */
    public List<Asset> getAssetsByStatus(String status) {
        return assetRepository.findByStatus(status);
    }

    /**
     * Get all assets assigned to an employee.
     * @param employeeId the employee's ID
     * @return List of Asset
     */
    public List<Asset> getAssetsForEmployee(Long employeeId) {
        return assetRepository.findByAssignedTo(employeeId);
    }

    /**
     * Validate certification for asset usage (stub).
     * @param asset the asset
     * @param employeeId the employee's ID
     * @return true if valid, false otherwise
     */
    public boolean validateCertification(Asset asset, Long employeeId) {
        // Implement certification validation logic here
        return true;
    }
}
