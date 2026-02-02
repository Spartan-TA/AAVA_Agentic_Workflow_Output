package com.wms.asset.controllers;

import com.wms.asset.dtos.AssetAssignmentDto;
import com.wms.asset.dtos.AssetDto;
import com.wms.asset.services.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for asset management
 */
@RestController
@RequestMapping("/api/asset")
@RequiredArgsConstructor
public class AssetController {
    private final AssetService assetService;

    /**
     * Register a new asset
     */
    @PostMapping("/register")
    public ResponseEntity<AssetDto> registerAsset(@RequestBody AssetDto dto) {
        return ResponseEntity.ok(assetService.registerAsset(dto));
    }

    /**
     * Get all assets
     */
    @GetMapping("")
    public ResponseEntity<List<AssetDto>> getAllAssets() {
        return ResponseEntity.ok(assetService.getAllAssets());
    }

    /**
     * Assign an asset to an employee
     */
    @PostMapping("/assignments")
    public ResponseEntity<AssetAssignmentDto> assignAsset(@RequestBody AssetAssignmentDto dto) {
        return ResponseEntity.ok(assetService.assignAsset(dto));
    }

    /**
     * Get all assignments for an employee
     */
    @GetMapping("/assignments/employee/{employeeId}")
    public ResponseEntity<List<AssetAssignmentDto>> getAssignmentsForEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(assetService.getAssignmentsForEmployee(employeeId));
    }
}
