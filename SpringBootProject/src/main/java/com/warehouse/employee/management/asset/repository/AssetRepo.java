package com.warehouse.employee.management.asset.repository;

import com.warehouse.employee.management.asset.domain.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepo extends JpaRepository<Asset, Long> {
    // Custom query methods if needed
}
