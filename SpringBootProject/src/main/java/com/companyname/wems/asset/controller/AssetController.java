package com.companyname.wems.asset.controller;

import com.companyname.wems.asset.model.AssetAssignment;
import com.companyname.wems.asset.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {
    private final AssetService assetService;

    // Assign asset to employee
    @PostMapping("/assign")
    public ResponseEntity<AssetAssignment> assignAsset(@RequestBody AssetAssignment assignment) {
        return ResponseEntity.ok(assetService.assignAsset(assignment));
    }

    // Check-in asset
    @PutMapping("/checkin/{id}")
    public ResponseEntity<AssetAssignment> checkInAsset(@PathVariable Long id, @RequestParam String condition) {
        return ResponseEntity.ok(assetService.checkInAsset(id, condition));
    }

    // Check-out asset
    @PutMapping("/checkout/{id}")
    public ResponseEntity<AssetAssignment> checkOutAsset(@PathVariable Long id, @RequestParam Long employeeId) {
        return ResponseEntity.ok(assetService.checkOutAsset(id, employeeId));
    }

    // Get overdue assets
    @GetMapping("/overdue")
    public ResponseEntity<List<AssetAssignment>> getOverdueAssets() {
        return ResponseEntity.ok(assetService.getOverdueAssets());
    }
}