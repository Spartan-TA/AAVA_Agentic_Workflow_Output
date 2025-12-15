package com.warehouseems.security.repository;

import com.warehouseems.security.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for ApiKey entity.
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByKeyAndEnabledTrue(String key);
}
