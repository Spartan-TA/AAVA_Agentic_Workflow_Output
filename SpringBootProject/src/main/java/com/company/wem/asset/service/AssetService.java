package com.company.wem.asset.service;

import com.company.wem.asset.entity.Asset;
import com.company.wem.asset.entity.AssetAssignment;
import com.company.wem.asset.repository.AssetRepository;
import com.company.wem.employee.entity.Employee;
import com.company.wem.certification.service.CertificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Service for asset management, including check-in/out and certification validation.
 */
@Service
public class AssetService {
    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private CertificationService certificationService;

    /**
     * Assigns an asset to an employee after validating required certifications.
     */
    @Transactional
    public AssetAssignment checkoutAsset(Asset asset, Employee employee, String requiredCertType) {
        if (!certificationService.hasValidCertification(employee, requiredCertType)) {
            throw new IllegalArgumentException("Employee lacks required certification: " + requiredCertType);
        }
        AssetAssignment assignment = new AssetAssignment();
        assignment.setAsset(asset);
        assignment.setEmployee(employee);
        assignment.setCheckoutDate(LocalDate.now());
        asset.setAssignedTo(employee);
        assetRepository.save(asset);
        // Persist assignment (assume AssetAssignmentRepository exists)
        return assignment;
    }

    /**
     * Returns an asset from an employee.
     */
    @Transactional
    public void checkinAsset(Asset asset, Employee employee) {
        asset.setAssignedTo(null);
        assetRepository.save(asset);
        // Update assignment record (assume AssetAssignmentRepository exists)
    }

    public Optional<Asset> findBySerialNumber(String serialNumber) {
        return Optional.ofNullable(assetRepository.findBySerialNumber(serialNumber));
    }
}