package com.warehouse.equipment.controller;

import com.warehouse.equipment.entity.Asset;
import com.warehouse.equipment.entity.AssetAssignment;
import com.warehouse.equipment.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    @Autowired
    private AssetService assetService;

    @GetMapping
    public ResponseEntity<List<Asset>> getAllAssets() {
        return ResponseEntity.ok(assetService.getAllAssets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Asset> getAssetById(@PathVariable Long id) {
        return assetService.getAssetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Asset> addAsset(@Valid @RequestBody Asset asset) {
        Asset created = assetService.addAsset(asset);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/{assetId}/assign/{employeeId}")
    public ResponseEntity<AssetAssignment> assignAsset(@PathVariable Long assetId, @PathVariable Long employeeId) {
        AssetAssignment assignment = assetService.assignAsset(assetId, employeeId);
        return ResponseEntity.ok(assignment);
    }

    @PostMapping("/checkin/{assignmentId}")
    public ResponseEntity<AssetAssignment> checkInAsset(@PathVariable Long assignmentId) {
        AssetAssignment assignment = assetService.checkInAsset(assignmentId);
        return ResponseEntity.ok(assignment);
    }

    @GetMapping("/validate-certification")
    public ResponseEntity<Boolean> validateCertification(@RequestParam Long employeeId, @RequestParam String assetType) {
        boolean valid = assetService.validateCertification(employeeId, assetType);
        return ResponseEntity.ok(valid);
    }
}
