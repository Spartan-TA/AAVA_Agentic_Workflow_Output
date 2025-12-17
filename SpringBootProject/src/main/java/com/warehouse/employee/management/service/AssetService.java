package com.warehouse.employee.management.service;

import com.warehouse.employee.management.entity.Asset;
import com.warehouse.employee.management.repository.AssetRepository;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service class for managing Asset entities.
 */
@Service
public class AssetService {
    private final AssetRepository assetRepository;

    @Autowired
    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    /**
     * Get all assets.
     * @return List of assets
     */
    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    /**
     * Get asset by ID.
     * @param id Asset ID
     * @return Asset entity
     */
    public Asset getAssetById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));
    }

    /**
     * Create a new asset.
     * @param asset Asset entity
     * @return Created asset
     */
    @Transactional
    public Asset createAsset(Asset asset) {
        return assetRepository.save(asset);
    }

    /**
     * Update an existing asset.
     * @param id Asset ID
     * @param updatedAsset Updated asset entity
     * @return Updated asset
     */
    @Transactional
    public Asset updateAsset(Long id, Asset updatedAsset) {
        Asset existingAsset = getAssetById(id);
        existingAsset.setName(updatedAsset.getName());
        existingAsset.setType(updatedAsset.getType());
        existingAsset.setAssignedTo(updatedAsset.getAssignedTo());
        existingAsset.setStatus(updatedAsset.getStatus());
        // Add other fields as needed
        return assetRepository.save(existingAsset);
    }

    /**
     * Delete an asset by ID.
     * @param id Asset ID
     */
    @Transactional
    public void deleteAsset(Long id) {
        Asset asset = getAssetById(id);
        assetRepository.delete(asset);
    }

    /**
     * Assign asset to employee.
     * @param assetId Asset ID
     * @param employeeId Employee ID
     * @return Updated asset
     */
    @Transactional
    public Asset assignAsset(Long assetId, Long employeeId) {
        Asset asset = getAssetById(assetId);
        asset.setAssignedToId(employeeId);
        asset.setStatus("ASSIGNED");
        return assetRepository.save(asset);
    }

    /**
     * Checkout asset (mark as checked out).
     * @param assetId Asset ID
     * @return Updated asset
     */
    @Transactional
    public Asset checkoutAsset(Long assetId) {
        Asset asset = getAssetById(assetId);
        asset.setStatus("CHECKED_OUT");
        return assetRepository.save(asset);
    }
}
