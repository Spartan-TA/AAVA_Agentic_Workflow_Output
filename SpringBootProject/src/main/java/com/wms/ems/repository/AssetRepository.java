package com.wms.ems.repository;

import com.wms.ems.entity.Asset;
import com.wms.ems.enums.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Asset entity operations.
 * Provides CRUD and custom query methods for asset management.
 */
public interface AssetRepository extends JpaRepository<Asset, Long> {
    /**
     * Find assets by asset type.
     * @param assetType the asset type
     * @return List of Assets
     */
    List<Asset> findByAssetType(AssetType assetType);

    /**
     * Find all active assets.
     * @return List of active Assets
     */
    List<Asset> findByIsActiveTrue();

    /**
     * Find asset by serial number.
     * @param serialNumber the serial number
     * @return Optional of Asset
     */
    Optional<Asset> findBySerialNumber(String serialNumber);
}
