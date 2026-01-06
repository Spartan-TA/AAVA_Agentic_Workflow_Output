package com.example.warehouse.service;

import com.example.warehouse.dto.AssetDTO;
import com.example.warehouse.entity.Asset;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;
import com.example.warehouse.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing assets.
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
     * @return List of AssetDTO
     */
    @Transactional(readOnly = true)
    public List<AssetDTO> getAllAssets() {
        return assetRepository.findAll().stream()
                .map(AssetDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get asset by ID.
     * @param id Asset ID
     * @return AssetDTO
     */
    @Transactional(readOnly = true)
    public AssetDTO getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));
        return AssetDTO.fromEntity(asset);
    }

    /**
     * Create a new asset.
     * @param dto AssetDTO
     * @return AssetDTO
     */
    @Transactional
    public AssetDTO createAsset(AssetDTO dto) {
        if (dto.getName() == null || dto.getName().isEmpty()) {
            throw new ValidationException("Asset name is required");
        }
        Asset asset = new Asset();
        asset.setName(dto.getName());
        asset.setType(dto.getType());
        asset.setSerialNumber(dto.getSerialNumber());
        asset.setStatus(dto.getStatus());
        assetRepository.save(asset);
        return AssetDTO.fromEntity(asset);
    }

    /**
     * Update asset status.
     * @param id Asset ID
     * @param status New status
     * @return AssetDTO
     */
    @Transactional
    public AssetDTO updateAssetStatus(Long id, String status) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));
        asset.setStatus(status);
        assetRepository.save(asset);
        return AssetDTO.fromEntity(asset);
    }
}
