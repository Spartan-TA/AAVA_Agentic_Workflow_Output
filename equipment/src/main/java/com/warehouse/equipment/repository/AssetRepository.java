package com.warehouse.equipment.repository;

import com.warehouse.equipment.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByStatus(Asset.Status status);
    Asset findBySerialNumber(String serialNumber);
}
