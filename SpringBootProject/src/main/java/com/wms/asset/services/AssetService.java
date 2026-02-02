package com.wms.asset.services;

import com.wms.asset.dtos.AssetAssignmentDto;
import com.wms.asset.dtos.AssetDto;
import com.wms.asset.model.Asset;
import com.wms.asset.model.AssetAssignment;
import com.wms.asset.repositories.AssetAssignmentRepository;
import com.wms.asset.repositories.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing assets and asset assignments
 */
@Service
@RequiredArgsConstructor
public class AssetService {
    private final AssetRepository assetRepository;
    private final AssetAssignmentRepository assetAssignmentRepository;

    /**
     * Register a new asset
     */
    public AssetDto registerAsset(AssetDto dto) {
        Asset asset = Asset.builder()
                .name(dto.getName())
                .type(dto.getType())
                .serialNumber(dto.getSerialNumber())
                .active(dto.isActive())
                .build();
        Asset saved = assetRepository.save(asset);
        dto.setId(saved.getId());
        return dto;
    }

    /**
     * Get all assets
     */
    public List<AssetDto> getAllAssets() {
        return assetRepository.findAll().stream()
                .map(a -> AssetDto.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .type(a.getType())
                        .serialNumber(a.getSerialNumber())
                        .active(a.isActive())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Assign an asset to an employee
     */
    @Transactional
    public AssetAssignmentDto assignAsset(AssetAssignmentDto dto) {
        Optional<Asset> assetOpt = assetRepository.findById(dto.getAssetId());
        if (assetOpt.isEmpty()) {
            throw new IllegalArgumentException("Asset not found");
        }
        AssetAssignment assignment = AssetAssignment.builder()
                .employeeId(dto.getEmployeeId())
                .asset(assetOpt.get())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .active(dto.isActive())
                .build();
        AssetAssignment saved = assetAssignmentRepository.save(assignment);
        dto.setId(saved.getId());
        return dto;
    }

    /**
     * Get all assignments for an employee
     */
    public List<AssetAssignmentDto> getAssignmentsForEmployee(Long employeeId) {
        return assetAssignmentRepository.findByEmployeeId(employeeId).stream()
                .map(a -> AssetAssignmentDto.builder()
                        .id(a.getId())
                        .employeeId(a.getEmployeeId())
                        .assetId(a.getAsset().getId())
                        .startDate(a.getStartDate())
                        .endDate(a.getEndDate())
                        .active(a.isActive())
                        .build())
                .collect(Collectors.toList());
    }
}
