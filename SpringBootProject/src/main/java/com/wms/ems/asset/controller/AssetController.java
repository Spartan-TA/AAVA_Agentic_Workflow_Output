package com.wms.ems.asset.controller;

import com.wms.ems.asset.dto.AssetDto;
import com.wms.ems.asset.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
@Tag(name = "Assets", description = "Endpoints for asset management")
public class AssetController {
    private final AssetService assetService;

    @Operation(summary = "Create asset")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAsset(@Valid @RequestBody AssetDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return ResponseEntity.ok(assetService.createAsset(dto));
    }

    @Operation(summary = "Get all assets")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<AssetDto>> getAssets() {
        return ResponseEntity.ok(assetService.getAssets());
    }

    @Operation(summary = "Update asset")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAsset(@PathVariable Long id, @Valid @RequestBody AssetDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return ResponseEntity.ok(assetService.updateAsset(id, dto));
    }

    @Operation(summary = "Delete asset")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Checkout asset")
    @PostMapping("/{id}/checkout")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public ResponseEntity<?> checkoutAsset(@PathVariable Long id, @RequestParam String employeeId) {
        assetService.checkoutAsset(id, employeeId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Return asset")
    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public ResponseEntity<?> returnAsset(@PathVariable Long id, @RequestParam String employeeId) {
        assetService.returnAsset(id, employeeId);
        return ResponseEntity.ok().build();
    }
}
