package com.example.warehouse.asset.controller;

import com.example.warehouse.asset.entity.Asset;
import com.example.warehouse.asset.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    @Autowired
    private AssetService assetService;

    // Get all assets
    @GetMapping
    public List<Asset> getAllAssets() {
        return assetService.getAllAssets();
    }

    // Get assets by status
    @GetMapping("/status/{status}")
    public List<Asset> getAssetsByStatus(@PathVariable String status) {
        return assetService.getAssetsByStatus(status);
    }

    // Get asset by ID
    @GetMapping("/{id}")
    public ResponseEntity<Asset> getAssetById(@PathVariable Long id) {
        Optional<Asset> asset = assetService.getAssetById(id);
        return asset.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create new asset
    @PostMapping
    public ResponseEntity<Asset> createAsset(@RequestBody Asset asset) {
        Asset created = assetService.createAsset(asset);
        return ResponseEntity.ok(created);
    }

    // Update asset
    @PutMapping("/{id}")
    public ResponseEntity<Asset> updateAsset(@PathVariable Long id, @RequestBody Asset asset) {
        Optional<Asset> updated = assetService.updateAsset(id, asset);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete asset
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        boolean deleted = assetService.deleteAsset(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
