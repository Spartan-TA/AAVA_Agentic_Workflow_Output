package com.company.wem.asset.repository;

import com.company.wem.asset.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Asset entity.
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    Asset findBySerialNumber(String serialNumber);
}