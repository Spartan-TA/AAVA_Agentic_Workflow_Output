package com.wms.ems.asset.repository;

import com.wms.ems.asset.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Asset entity operations.
 * Provides CRUD operations and custom queries for asset management.
 */
public interface AssetRepository extends JpaRepository<Asset, Long> {

    /**
     * Finds an asset by its serial number.
     * @param serialNumber the asset serial number
     * @return an Optional containing the Asset if found
     */
    Optional<Asset> findBySerialNumber(String serialNumber);

    /**
     * Finds assets by type.
     * @param type the asset type
     * @return a list of assets
     */
    List<Asset> findByType(String type);
}
