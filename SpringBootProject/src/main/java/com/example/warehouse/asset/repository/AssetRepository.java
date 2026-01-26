package com.example.warehouse.asset.repository;

import com.example.warehouse.asset.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    // Find assets by status
    List<Asset> findByStatus(String status);
}
