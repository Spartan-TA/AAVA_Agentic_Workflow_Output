package com.wms.site.repositories;

import com.wms.site.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Site entities.
 */
@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
    // Additional query methods can be defined here if needed
}
