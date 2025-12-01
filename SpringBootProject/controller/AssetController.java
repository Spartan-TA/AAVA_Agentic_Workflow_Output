package com.example.ems.controller;

import com.example.ems.dto.AssetDto;
import com.example.ems.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/assets")
@Validated
public class AssetController {
    private final AssetService assetService;

    @Autowired
    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public ResponseEntity<List<AssetDto>> getAllAssets() {
        return ResponseEntity.ok(assetService.getAllAssets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetDto> getAssetById(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.getAssetById(id));
    }

    @PostMapping
    public ResponseEntity<AssetDto> createAsset(@Valid @RequestBody AssetDto assetDto) {
        return ResponseEntity.ok(assetService.createAsset(assetDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetDto> updateAsset(@PathVariable Long id, @Valid @RequestBody AssetDto assetDto) {
        return ResponseEntity.ok(assetService.updateAsset(id, assetDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }
}