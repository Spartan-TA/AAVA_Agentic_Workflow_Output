package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Asset entity with custom query methods.
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    /**
     * Find asset by asset tag.
     * @param assetTag Asset tag
     * @return Optional of Asset
     */
    Optional<Asset> findByAssetTag(String assetTag);
}
