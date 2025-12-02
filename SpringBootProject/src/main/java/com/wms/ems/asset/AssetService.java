package com.wms.ems.asset;

import com.wms.ems.certification.CertificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssetService {

    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private CertificationService certificationService;

    // Assign asset to employee with certification validation
    @Transactional
    public Asset assignAsset(Long assetId, Long employeeId, String certType) {
        Asset asset = assetRepository.findById(assetId).orElseThrow();
        if (!certificationService.isCertificationValid(employeeId, certType)) {
            throw new IllegalArgumentException("Employee does not have valid certification: " + certType);
        }
        asset.setAssignedEmployeeId(employeeId);
        Asset.CheckoutEvent event = new Asset.CheckoutEvent();
        event.setEmployeeId(employeeId);
        event.setAction("CHECKOUT");
        event.setTimestamp(LocalDateTime.now());
        asset.getCheckoutHistory().add(event);
        return assetRepository.save(asset);
    }

    // Return asset
    @Transactional
    public Asset returnAsset(Long assetId, Long employeeId) {
        Asset asset = assetRepository.findById(assetId).orElseThrow();
        asset.setAssignedEmployeeId(null);
        Asset.CheckoutEvent event = new Asset.CheckoutEvent();
        event.setEmployeeId(employeeId);
        event.setAction("RETURN");
        event.setTimestamp(LocalDateTime.now());
        asset.getCheckoutHistory().add(event);
        return assetRepository.save(asset);
    }

    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }
}
