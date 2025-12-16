package com.companyname.wems.asset.service;

import com.companyname.wems.asset.model.AssetAssignment;
import com.companyname.wems.asset.repository.AssetAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetService {
    private final AssetAssignmentRepository assetAssignmentRepository;

    // Assign asset to employee
    public AssetAssignment assignAsset(AssetAssignment assignment) {
        assignment.setStatus("ASSIGNED");
        return assetAssignmentRepository.save(assignment);
    }

    // Check-in asset
    public AssetAssignment checkInAsset(Long id, String condition) {
        AssetAssignment assignment = assetAssignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AssetAssignment not found"));
        assignment.setStatus("RETURNED");
        assignment.setCondition(condition);
        assignment.setReturnDate(LocalDate.now());
        return assetAssignmentRepository.save(assignment);
    }

    // Check-out asset (re-assign)
    public AssetAssignment checkOutAsset(Long id, Long employeeId) {
        AssetAssignment assignment = assetAssignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AssetAssignment not found"));
        assignment.setStatus("ASSIGNED");
        assignment.setEmployeeId(employeeId);
        assignment.setAssignedDate(LocalDate.now());
        assignment.setReturnDate(null);
        return assetAssignmentRepository.save(assignment);
    }

    // Track asset condition
    public List<AssetAssignment> getAssetsByCondition(String condition) {
        return assetAssignmentRepository.findByStatus(condition);
    }

    // Generate overdue return reports
    public List<AssetAssignment> getOverdueAssets() {
        LocalDate today = LocalDate.now();
        return assetAssignmentRepository.findByReturnDateBeforeAndStatus(today, "ASSIGNED");
    }
}