package com.example.warehouse.asset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssetService {
    @Autowired
    private AssetRepository assetRepository;

    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    public Optional<Asset> getAssetById(Long id) {
        return assetRepository.findById(id);
    }

    public List<Asset> getAssetsByEmployee(Long employeeId) {
        return assetRepository.findByAssignedEmployeeId(employeeId);
    }

    public Asset createAsset(AssetDto dto) {
        Asset asset = new Asset();
        asset.setName(dto.getName());
        asset.setType(dto.getType());
        asset.setSerialNumber(dto.getSerialNumber());
        asset.setStatus(dto.getStatus());
        asset.setAssignedEmployeeId(dto.getAssignedEmployeeId());
        return assetRepository.save(asset);
    }

    public void deleteAsset(Long id) {
        assetRepository.deleteById(id);
    }
}
