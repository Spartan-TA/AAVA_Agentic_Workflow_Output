package com.example.warehouse.service;

import com.example.warehouse.dto.AssetCheckoutDTO;
import com.example.warehouse.dto.AssetDTO;
import com.example.warehouse.entity.Asset;
import com.example.warehouse.entity.Employee;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.CertificationExpiredException;
import com.example.warehouse.repository.AssetRepository;
import com.example.warehouse.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssetService {
    private final AssetRepository assetRepository;
    private final EmployeeRepository employeeRepository;
    private final CertificationService certificationService;

    public AssetService(AssetRepository assetRepository, EmployeeRepository employeeRepository, CertificationService certificationService) {
        this.assetRepository = assetRepository;
        this.employeeRepository = employeeRepository;
        this.certificationService = certificationService;
    }

    @Transactional
    public Asset checkoutAsset(Long assetId, AssetCheckoutDTO dto) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        if (asset.getRequiredCertification() != null) {
            certificationService.validateCertification(employee.getId(), asset.getRequiredCertification());
        }
        asset.setCheckedOutBy(employee);
        asset.setCheckedOutAt(LocalDateTime.now());
        asset.setCondition(dto.getCondition());
        asset.setStatus("CHECKED_OUT");
        assetRepository.save(asset);
        // History logging
        return asset;
    }

    @Transactional
    public Asset checkinAsset(Long assetId, AssetDTO dto) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        asset.setCheckedOutBy(null);
        asset.setCheckedOutAt(null);
        asset.setCondition(dto.getCondition());
        asset.setStatus("AVAILABLE");
        assetRepository.save(asset);
        // History logging
        return asset;
    }

    public List<Asset> getOverdueAssets() {
        return assetRepository.findOverdueAssets(LocalDateTime.now());
    }

    // Additional methods for history logging, alerts, etc.
}
