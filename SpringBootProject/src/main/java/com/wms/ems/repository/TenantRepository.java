package com.wms.ems.repository;

import com.wms.ems.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

/**
 * Repository interface for Tenant entity operations.
 * Provides CRUD and custom query methods for tenant management.
 */
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    /**
     * Find tenant by tenant ID.
     * @param tenantId the tenant ID
     * @return Optional of Tenant
     */
    Optional<Tenant> findByTenantId(String tenantId);

    /**
     * Find all active tenants.
     * @return List of active Tenants
     */
    List<Tenant> findByIsActiveTrue();
}
