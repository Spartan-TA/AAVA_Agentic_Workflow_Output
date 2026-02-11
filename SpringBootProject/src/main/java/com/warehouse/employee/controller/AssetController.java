package com.warehouse.employee.controller;

import com.warehouse.employee.dto.AssetDto;
import com.warehouse.employee.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for asset checkout and return.
 */
@RestController
@RequestMapping("/api/assets")
@Validated
public class AssetController {

    private final AssetService assetService;

    @Autowired
    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @Operation(summary = "Checkout an asset to an employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asset checked out successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/checkout")
    public ResponseEntity<AssetDto> checkoutAsset(@RequestParam String assetTag, @RequestParam Long employeeId) {
        AssetDto response = assetService.checkoutAsset(assetTag, employeeId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Return an asset")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asset returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/return")
    public ResponseEntity<AssetDto> returnAsset(@RequestParam String assetTag) {
        AssetDto response = assetService.returnAsset(assetTag);
        return ResponseEntity.ok(response);
    }
}
