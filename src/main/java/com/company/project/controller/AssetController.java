package com.company.project.controller;

import com.company.project.dto.AssetDto;
import com.company.project.service.AssetService;
import com.company.project.mapper.AssetMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/assets")
@Tag(name = "Asset Management", description = "Manage equipment and asset assignments")
public class AssetController {

    private final AssetService assetService;
    private final AssetMapper assetMapper;

    @Autowired
    public AssetController(AssetService assetService, AssetMapper assetMapper) {
        this.assetService = assetService;
        this.assetMapper = assetMapper;
    }

    @Operation(summary = "Assign asset to employee", responses = {
            @ApiResponse(responseCode = "201", description = "Asset assigned successfully")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @PostMapping("/assign")
    public ResponseEntity<AssetDto> assignAsset(@Valid @RequestBody AssetDto request) {
        var asset = assetService.assignAsset(request);
        return ResponseEntity.status(201).body(assetMapper.toDto(asset));
    }

    @Operation(summary = "Get all assets", responses = {
            @ApiResponse(responseCode = "200", description = "List of assets")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @GetMapping
    public ResponseEntity<List<AssetDto>> getAllAssets() {
        var assets = assetService.getAllAssets();
        return ResponseEntity.ok(assetMapper.toDtoList(assets));
    }
}
