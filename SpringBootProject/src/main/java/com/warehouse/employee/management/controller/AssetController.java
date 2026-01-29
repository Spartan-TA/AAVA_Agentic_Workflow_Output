package com.warehouse.employee.management.controller;

import com.warehouse.employee.management.dto.AssetDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/assets")
@Validated
public class AssetController {
    private final List<AssetDto> assets = new ArrayList<>();

    @PreAuthorize("hasAuthority('ASSET_ASSIGN')")
    @PostMapping("/assign")
    public AssetDto assignAsset(@Valid @RequestBody AssetDto assetDto) {
        assetDto.setStatus("ASSIGNED");
        assets.add(assetDto);
        return assetDto;
    }

    @PreAuthorize("hasAuthority('ASSET_CHECKIN')")
    @PutMapping("/checkin/{index}")
    public AssetDto checkInAsset(@PathVariable int index) {
        if (index < 0 || index >= assets.size()) throw new IllegalArgumentException("Invalid index");
        AssetDto asset = assets.get(index);
        asset.setStatus("AVAILABLE");
        asset.setAssignedToId(null);
        asset.setCheckoutDate(null);
        return asset;
    }

    @PreAuthorize("hasAuthority('ASSET_VALIDATE')")
    @GetMapping("/validate/{index}")
    public boolean validateAsset(@PathVariable int index, @RequestParam String requiredCert) {
        if (index < 0 || index >= assets.size()) throw new IllegalArgumentException("Invalid index");
        // Stub for certification validation
        return true;
    }

    @PreAuthorize("hasAuthority('ASSET_READ')")
    @GetMapping
    public List<AssetDto> getAssets() {
        return Collections.unmodifiableList(assets);
    }
}
