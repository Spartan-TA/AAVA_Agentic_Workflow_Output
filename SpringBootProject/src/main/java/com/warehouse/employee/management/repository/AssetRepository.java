package com.warehouse.employee.management.repository;

import com.warehouse.employee.management.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

/**
 * Repository for Asset entity.
 * Supports CRUD, pagination, sorting, and soft-delete queries.
 */
public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {
    @Query("SELECT a FROM Asset a WHERE a.deletedAt IS NULL")
    List<Asset> findAllActive();

    @Query("SELECT a FROM Asset a WHERE a.deletedAt IS NULL")
    Page<Asset> findAllActive(Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.id = :id AND a.deletedAt IS NULL")
    Optional<Asset> findActiveById(Long id);

    // Custom query example: Find by assetType
    @Query("SELECT a FROM Asset a WHERE a.assetType = :assetType AND a.deletedAt IS NULL")
    List<Asset> findActiveByAssetType(String assetType);
}
