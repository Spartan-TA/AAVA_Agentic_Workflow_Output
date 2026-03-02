package com.warehouse.employee.repository;

import com.warehouse.employee.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Tenant entity.
 */
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    /**
     * Find tenant by tenant code.
     */
    Optional<Tenant> findByTenantCode(String tenantCode);

    /**
     * Find tenants by active status.
     */
    List<Tenant> findByIsActive(Boolean isActive);
}
