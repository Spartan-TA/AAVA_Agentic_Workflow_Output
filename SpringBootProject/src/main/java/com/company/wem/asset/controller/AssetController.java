package com.company.wem.asset.controller;

import com.company.wem.asset.entity.Asset;
import com.company.wem.asset.service.AssetService;
import com.company.wem.asset.dto.AssetDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

/**
 * REST controller for asset management.
 */
@RestController
@RequestMapping("/api/assets")
public class AssetController {
    @Autowired
    private AssetService assetService;

    @GetMapping("/{serialNumber}")
    public ResponseEntity<AssetDto> getAssetBySerialNumber(@PathVariable String serialNumber) {
        Optional<Asset> assetOpt = assetService.findBySerialNumber(serialNumber);
        if (assetOpt.isPresent()) {
            Asset asset = assetOpt.get();
            AssetDto dto = AssetDto.fromEntity(asset);
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Additional endpoints for check-in/out, CRUD, etc. can be added here
}