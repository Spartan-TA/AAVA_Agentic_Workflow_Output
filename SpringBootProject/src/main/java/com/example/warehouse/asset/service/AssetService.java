package com.example.warehouse.asset.service;

import com.example.warehouse.asset.entity.Asset;
import com.example.warehouse.asset.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AssetService {
    @Autowired
    private AssetRepository assetRepository;

    // Get all assets
    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    // Get assets by status
    public List<Asset> getAssetsByStatus(String status) {
        return assetRepository.findByStatus(status);
    }

    // Get asset by ID
    public Optional<Asset> getAssetById(Long id) {
        return assetRepository.findById(id);
    }

    // Create new asset
    @Transactional
    public Asset createAsset(Asset asset) {
        return assetRepository.save(asset);
    }

    // Update asset
    @Transactional
    public Optional<Asset> updateAsset(Long id, Asset asset) {
        return assetRepository.findById(id).map(existing -> {
            existing.setName(asset.getName());
            existing.setType(asset.getType());
            existing.setPurchaseDate(asset.getPurchaseDate());
            existing.setStatus(asset.getStatus());
            existing.setLocation(asset.getLocation());
            return assetRepository.save(existing);
        });
    }

    // Delete asset
    @Transactional
    public boolean deleteAsset(Long id) {
        if (assetRepository.existsById(id)) {
            assetRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
