package com.warehouse.employee.service;

import com.warehouse.employee.domain.Asset;
import com.warehouse.employee.domain.Employee;
import com.warehouse.employee.dto.AssetDto;
import com.warehouse.employee.exception.EmployeeNotFoundException;
import com.warehouse.employee.mapper.AssetMapper;
import com.warehouse.employee.repository.AssetRepository;
import com.warehouse.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for asset checkout and return.
 */
@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final EmployeeRepository employeeRepository;
    private final AssetMapper assetMapper;

    @Autowired
    public AssetService(AssetRepository assetRepository,
                        EmployeeRepository employeeRepository,
                        AssetMapper assetMapper) {
        this.assetRepository = assetRepository;
        this.employeeRepository = employeeRepository;
        this.assetMapper = assetMapper;
    }

    /**
     * Checkout an asset to an employee.
     * @param assetTag Asset tag
     * @param employeeId Employee ID
     * @return AssetDto
     */
    @Transactional
    public AssetDto checkoutAsset(String assetTag, Long employeeId) {
        Asset asset = assetRepository.findByAssetTag(assetTag)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + assetTag));
        Employee employee = employeeRepository.findById(employeeId)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found: " + employeeId));
        asset.setCheckedOut(true);
        asset.setCheckedOutTo(employee);
        Asset saved = assetRepository.save(asset);
        return assetMapper.toDto(saved);
    }

    /**
     * Return an asset.
     * @param assetTag Asset tag
     * @return AssetDto
     */
    @Transactional
    public AssetDto returnAsset(String assetTag) {
        Asset asset = assetRepository.findByAssetTag(assetTag)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + assetTag));
        asset.setCheckedOut(false);
        asset.setCheckedOutTo(null);
        Asset saved = assetRepository.save(asset);
        return assetMapper.toDto(saved);
    }
}
