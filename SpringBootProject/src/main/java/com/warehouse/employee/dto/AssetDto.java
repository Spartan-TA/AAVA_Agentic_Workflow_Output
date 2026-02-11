package com.warehouse.employee.dto;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO for warehouse asset.
 */
public class AssetDto {
    private Long assetId;
    @NotNull
    private String assetName;
    private String assetType;
    private LocalDate purchaseDate;
    private LocalDate warrantyExpiryDate;
    private String status;

    public AssetDto() {}

    public AssetDto(Long assetId, String assetName, String assetType, LocalDate purchaseDate, LocalDate warrantyExpiryDate, String status) {
        this.assetId = assetId;
        this.assetName = assetName;
        this.assetType = assetType;
        this.purchaseDate = purchaseDate;
        this.warrantyExpiryDate = warrantyExpiryDate;
        this.status = status;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDate getWarrantyExpiryDate() {
        return warrantyExpiryDate;
    }

    public void setWarrantyExpiryDate(LocalDate warrantyExpiryDate) {
        this.warrantyExpiryDate = warrantyExpiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
