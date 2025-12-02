package com.wms.ems.asset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assets")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<List<Asset>> getAllAssets() {
        return ResponseEntity.ok(assetService.getAllAssets());
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR')")
    public ResponseEntity<Asset> checkoutAsset(@RequestParam Long assetId, @RequestParam Long employeeId, @RequestParam String certType) {
        return ResponseEntity.ok(assetService.assignAsset(assetId, employeeId, certType));
    }

    @PostMapping("/return")
    @PreAuthorize("hasAnyRole('ADMIN','HR','SUPERVISOR','WORKER')")
    public ResponseEntity<Asset> returnAsset(@RequestParam Long assetId, @RequestParam Long employeeId) {
        return ResponseEntity.ok(assetService.returnAsset(assetId, employeeId));
    }
}
