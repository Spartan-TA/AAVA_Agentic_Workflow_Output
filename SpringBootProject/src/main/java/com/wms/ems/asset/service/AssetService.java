package com.wms.ems.asset.service;

import com.wms.ems.asset.repository.AssetRepository;
import com.wms.ems.asset.repository.AssetAssignmentRepository;
import com.wms.ems.employee.repository.EmployeeRepository;
import com.wms.ems.certification.repository.CertificationRepository;
import com.wms.ems.asset.entity.Asset;
import com.wms.ems.asset.entity.AssetAssignment;
import com.wms.ems.asset.dto.AssetDto;
import com.wms.ems.employee.entity.Employee;
import com.wms.ems.certification.entity.Certification;
import com.wms.ems.common.exception.ResourceNotFoundException;
import com.wms.ems.common.exception.ValidationException;
import com.wms.ems.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing assets and assignments.
 */
@Service
@Transactional
@Slf4j
public class AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetAssignmentRepository assetAssignmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CertificationRepository certificationRepository;

    /**
     * Creates a new asset after validating unique serial number.
     * @param dto AssetDto
     * @return Asset
     */
    public Asset createAsset(AssetDto dto) {
        if (dto == null || dto.getSerialNumber() == null || dto.getSerialNumber().isEmpty()) {
            log.error("Validation failed: Serial number is required");
            throw new ValidationException("Serial number is required");
        }
        if (assetRepository.existsBySerialNumber(dto.getSerialNumber())) {
            log.error("Asset with serial number {} already exists", dto.getSerialNumber());
            throw new ValidationException("Asset with this serial number already exists");
        }
        Asset asset = new Asset();
        asset.setSerialNumber(dto.getSerialNumber());
        asset.setType(dto.getType());
        asset.setDescription(dto.getDescription());
        asset.setPurchaseDate(dto.getPurchaseDate());
        try {
            Asset saved = assetRepository.save(asset);
            log.info("Asset created: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Failed to create asset", e);
            throw new BusinessException("Failed to create asset");
        }
    }

    /**
     * Checks out an asset to an employee after validating certification.
     * @param assetId Asset ID
     * @param employeeId Employee ID
     * @return AssetAssignment
     */
    public AssetAssignment checkoutAsset(Long assetId, Long employeeId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        if (!hasRequiredCertification(employeeId, asset.getType())) {
            log.error("Employee {} lacks required certification for asset type {}", employeeId, asset.getType());
            throw new ValidationException("Employee lacks required certification for this asset type");
        }
        AssetAssignment assignment = new AssetAssignment();
        assignment.setAsset(asset);
        assignment.setEmployee(employee);
        assignment.setCheckoutTime(LocalDateTime.now());
        assignment.setExpectedReturnTime(LocalDateTime.now().plusDays(7)); // Example: 7 days
        try {
            AssetAssignment saved = assetAssignmentRepository.save(assignment);
            log.info("Asset {} checked out to employee {}", assetId, employeeId);
            return saved;
        } catch (Exception e) {
            log.error("Failed to checkout asset", e);
            throw new BusinessException("Failed to checkout asset");
        }
    }

    /**
     * Checks in an asset assignment by setting return time.
     * @param assignmentId Assignment ID
     * @return AssetAssignment
     */
    public AssetAssignment checkinAsset(Long assignmentId) {
        AssetAssignment assignment = assetAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset assignment not found"));
        if (assignment.getReturnTime() != null) {
            log.error("Asset already checked in");
            throw new ValidationException("Asset already checked in");
        }
        assignment.setReturnTime(LocalDateTime.now());
        try {
            AssetAssignment updated = assetAssignmentRepository.save(assignment);
            log.info("Asset assignment {} checked in", assignmentId);
            return updated;
        } catch (Exception e) {
            log.error("Failed to checkin asset", e);
            throw new BusinessException("Failed to checkin asset");
        }
    }

    /**
     * Gets current asset assignments for an employee.
     * @param employeeId Employee ID
     * @return List of AssetAssignment
     */
    @Transactional(readOnly = true)
    public List<AssetAssignment> getEmployeeAssets(Long employeeId) {
        try {
            return assetAssignmentRepository.findByEmployeeIdAndReturnTimeIsNull(employeeId);
        } catch (Exception e) {
            log.error("Failed to fetch employee assets", e);
            throw new BusinessException("Failed to fetch employee assets");
        }
    }

    /**
     * Finds overdue asset assignments (no return time, past expected date).
     * @return List of AssetAssignment
     */
    @Transactional(readOnly = true)
    public List<AssetAssignment> getOverdueAssets() {
        try {
            LocalDateTime now = LocalDateTime.now();
            return assetAssignmentRepository.findByReturnTimeIsNullAndExpectedReturnTimeBefore(now);
        } catch (Exception e) {
            log.error("Failed to fetch overdue assets", e);
            throw new BusinessException("Failed to fetch overdue assets");
        }
    }

    /**
     * Checks if employee has required certification for asset type.
     * @param employeeId Employee ID
     * @param assetType Asset type
     * @return boolean
     */
    @Transactional(readOnly = true)
    public boolean hasRequiredCertification(Long employeeId, String assetType) {
        try {
            List<Certification> certs = certificationRepository.findByEmployeeId(employeeId);
            return certs.stream().anyMatch(c -> c.getAssetType().equalsIgnoreCase(assetType) && c.isValid());
        } catch (Exception e) {
            log.error("Failed to check certification", e);
            throw new BusinessException("Failed to check certification");
        }
    }
}
