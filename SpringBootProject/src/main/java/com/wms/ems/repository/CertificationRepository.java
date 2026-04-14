package com.wms.ems.repository;

import com.wms.ems.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Certification entity operations.
 * Provides CRUD and custom query methods for certification management.
 */
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    /**
     * Find all active certifications.
     * @return List of active Certifications
     */
    List<Certification> findByIsActiveTrue();

    /**
     * Find a certification by name.
     * @param name the certification name
     * @return Optional of Certification
     */
    Optional<Certification> findByName(String name);
}
