package com.wms.asset.repositories;

import com.wms.asset.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing Asset entities
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    /**
     * Find asset by serial number
     * @param serialNumber Serial number
     * @return Optional Asset
     */
    Optional<Asset> findBySerialNumber(String serialNumber);
}
