package com.warehouse.employee.management.asset.service;

import com.warehouse.employee.management.dto.AssetDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class AssetService {
    private final List<AssetDto> assets = new ArrayList<>();

    @Transactional
    public AssetDto assignAsset(AssetDto asset) {
        asset.setStatus("ASSIGNED");
        assets.add(asset);
        return asset;
    }

    @Transactional
    public AssetDto checkInAsset(int assetIndex) {
        if (assetIndex < 0 || assetIndex >= assets.size()) throw new IllegalArgumentException("Invalid asset index");
        AssetDto asset = assets.get(assetIndex);
        asset.setStatus("AVAILABLE");
        asset.setAssignedToId(null);
        asset.setCheckoutDate(null);
        return asset;
    }

    @Transactional
    public AssetDto checkOutAsset(int assetIndex, Long employeeId) {
        if (assetIndex < 0 || assetIndex >= assets.size()) throw new IllegalArgumentException("Invalid asset index");
        AssetDto asset = assets.get(assetIndex);
        asset.setStatus("ASSIGNED");
        asset.setAssignedToId(employeeId);
        asset.setCheckoutDate(java.time.LocalDate.now());
        return asset;
    }

    public List<AssetDto> getAllAssets() {
        return Collections.unmodifiableList(assets);
    }

    // Certification validation stub
    public boolean validateCertification(Long employeeId, String requiredCert) {
        // TODO: Integrate with CertificationService
        return true;
    }
}
